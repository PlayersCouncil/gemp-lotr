// Compares the Angular deck builder with the legacy deckBuild.html: every scenario of scenarios.mjs runs on both
// pages in a real browser; the screenshots must match and both pages must ask the server for the same collection
// page and deck stats. Writes report/index.html; exits with 1 when something differs. See README.md.
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { chromium } from 'playwright';
import { CONTROLS } from './controls.mjs';
import { acceptedFor, withoutLegacyBugs } from './accepted.mjs';
import { MY_CARDS } from './local-server/seed.mjs';
import {
  compareRequests,
  compareStats,
  compareScreens,
  compareStates,
  fixedControls,
  MOUSE_TRACKER,
  readPageState,
} from './compare.mjs';
import { INJECTIONS } from './injections.mjs';
import { coverageGaps, matrixScenarios } from './matrix.mjs';
import { FIXTURE_DECK, SCENARIOS, selectors } from './scenarios.mjs';
import { routeToStub, STUB_ORIGIN } from './stub-server.mjs';

const { values: options } = parseArgs({
  options: {
    server: { type: 'string', default: process.env.GEMP_SERVER ?? 'http://localhost:17001' },
    legacy: { type: 'string', default: '/gemp-lotr/deckBuild.html' },
    angular: { type: 'string', default: '/gemp-lotr/ng/deckbuilder/' },
    // The Angular page's theme (its ?theme= parameter, unless --angular has one)
    theme: { type: 'string', default: 'classic' },
    sizes: { type: 'string', default: '1280x800,1366x768,1920x1080' },
    only: { type: 'string' },
    mutating: { type: 'boolean', default: false },
    // No server and no login: both pages run against stub-server.mjs, with placeholder card art
    stubbed: { type: 'boolean', default: false },
    workers: { type: 'string', default: '4' },
    // Pixels allowed to differ by more than 2 colour levels (anti-aliasing of rounded corners and SVG edges varies by
    // a few pixels between two loads of the same page)
    tolerance: { type: 'string', default: '60' },
    out: { type: 'string', default: 'report' },
    // The self-test (selftest.mjs): a deliberate bug put into the Angular page
    inject: { type: 'string' },
    // Every control against every gesture (matrix.mjs), and what is on the pages against what is tested
    matrix: { type: 'boolean', default: false },
    // With --matrix: only the scenarios whose name contains this
    filter: { type: 'string' },
  },
});

if (!['classic', 'modern'].includes(options.theme)) {
  console.error(`Unknown theme ${options.theme}: use --theme=classic or --theme=modern.`);
  process.exit(2);
}
const angularUrl = new URL(options.angular, 'http://server');
if (!angularUrl.searchParams.has('theme')) angularUrl.searchParams.set('theme', options.theme);
const angularPath = angularUrl.pathname + angularUrl.search;

if (options.stubbed) options.server = STUB_ORIGIN;

const user = process.env.GEMP_USER;
const password = process.env.GEMP_PASSWORD;
if (!options.stubbed && (!user || !password)) {
  console.error('Set GEMP_USER and GEMP_PASSWORD to a player on the server (see README.md).');
  process.exit(2);
}

const sizes = options.sizes.split(',').map((s) => {
  const [width, height] = s.split('x').map(Number);
  return { width, height, label: s };
});
const only = options.only ? new Set(options.only.split(',')) : null;
const allScenarios = options.matrix ? matrixScenarios() : SCENARIOS;
const scenarios = allScenarios.filter((s) =>
  only ? only.has(s.name) : (!s.mutating || options.mutating) && (!options.filter || s.name.includes(options.filter)),
);
const tolerance = Number(options.tolerance);
const outDir = path.resolve(options.out);
fs.rmSync(outDir, { recursive: true, force: true });
fs.mkdirSync(outDir, { recursive: true });

const browser = await chromium.launch();

/**
 * Logs in once; every page then uses that session. (Logging the same player in several times at once can hang the
 * server: each login holds a database connection while asking for another, see README.md.)
 */
async function login() {
  const context = await browser.newContext();
  const response = await context.request.post(`${options.server}/gemp-lotr-server/login`, {
    form: { login: user, password },
  });
  if (!response.ok()) {
    throw new Error(`Login as ${user} failed: HTTP ${response.status()}`);
  }
  return context;
}

let session;

/** The deck the deck list scenarios open, saved (again) under the test player. */
async function createFixtureDeck(context) {
  const response = await context.request.post(`${options.server}/gemp-lotr-server/deck`, {
    form: {
      deckName: FIXTURE_DECK.name,
      targetFormat: FIXTURE_DECK.targetFormat,
      notes: FIXTURE_DECK.notes,
      deckContents: FIXTURE_DECK.contents,
    },
  });
  if (!response.ok()) {
    throw new Error(`Could not save the fixture deck: HTTP ${response.status()}`);
  }
}

/** Waits until no server request has been pending for `quiet` ms (at most `max` ms). */
async function settle(page, pending, quiet = 700, max = 15000) {
  const start = Date.now();
  let calmSince = Date.now();
  while (Date.now() - start < max) {
    if (pending.size > 0) {
      calmSince = Date.now();
    } else if (Date.now() - calmSince >= quiet) {
      break;
    }
    await page.waitForTimeout(100);
  }
  // Card images
  await page
    .waitForFunction(() => Array.from(document.images).every((img) => img.complete), null, {
      timeout: 15000,
    })
    .catch(() => undefined);
}

/** Request data as one comparable string: decoded, sorted, without the session and cache-busting parameters. */
function normalize(query) {
  return (
    query
      .replace(/\+/g, ' ')
      .split('&')
      .map((part) => {
        try {
          return decodeURIComponent(part);
        } catch {
          return part;
        }
      })
      // The legacy page also sent the literal strings "null"/"undefined" for missing values
      .filter((p) => p && !/^(participantId|_)=/.test(p) && p !== 'leagueCode=null' && p !== 'collectionName=undefined')
      .sort()
      .join('&')
  );
}

/**
 * Answers collection requests in the order they were sent. The legacy page showed whichever answer arrived last,
 * so when the server answered an older request later, it showed an outdated page (fixed in the Angular page, which
 * keeps only the newest). Without this, the comparison would depend on the server's timing.
 */
async function deliverCollectionsInOrder(page) {
  let previous = Promise.resolve();
  await page.route('**/gemp-lotr-server/collection/**', async (route) => {
    const response = route.fetch().catch(() => null);
    const turn = previous.then(async () => {
      const fetched = await response;
      if (fetched) {
        await route.fulfill({ response: fetched }).catch(() => undefined);
      } else {
        await route.abort().catch(() => undefined);
      }
    });
    previous = turn;
    await turn;
  });
}

/**
 * Answers the Angular page's questions (its own alert, confirm and prompt dialogs, `gd-notifier`) from `page.answers`,
 * as the dialog handler below does for the legacy page's browser dialogs, until `stop()` is called.
 */
function answerNotifier(page) {
  let running = true;
  const loop = (async () => {
    while (running) {
      const dialog = page.locator(
        'dialog.gd-dialog[open]:is([data-dialog=alert],[data-dialog=confirm],[data-dialog=prompt])',
      );
      if ((await dialog.count().catch(() => 0)) > 0) {
        const kind = await dialog
          .first()
          .getAttribute('data-dialog')
          .catch(() => null);
        const answer = kind === 'alert' ? true : page.answers.shift();
        const step = async () => {
          if (answer === undefined || answer === false) {
            await dialog.first().locator('[data-action=cancel]').click();
            return;
          }
          if (typeof answer === 'string') await dialog.first().locator('.gd-notifier__input').fill(answer);
          await dialog.first().locator('[data-action=ok]').click();
        };
        await step().catch(() => undefined);
      }
      await page.waitForTimeout(100).catch(() => undefined);
    }
  })();
  return async () => {
    running = false;
    await loop;
  };
}

const injection = options.inject ? INJECTIONS[options.inject] : null;
if (options.inject && !injection) {
  console.error(`No injection ${options.inject} in injections.mjs`);
  process.exit(2);
}

/** The deck stats come from a request sent on a timer, so between steps they can be a moment behind: not compared then. */
const STATS_AREA = { selector: '.gd-stats', margin: 2, maxPixels: Infinity };

/** Runs in the page: where each accepted area is at the moment. */
function readAreas(entries) {
  return entries.map(({ selector, margin }) => {
    const box = document.querySelector(selector)?.getBoundingClientRect();
    return box
      ? { left: box.left - margin, top: box.top - margin, right: box.right + margin, bottom: box.bottom + margin }
      : null;
  });
}

/**
 * Random results of opening a pack: the first page's answers for collections and opened contents, given back to the
 * second page in the same order, so both show the same thing. What each page asks the server is still compared.
 */
const REPLAYED = /\/gemp-lotr-server\/(collection\/|delivery\/packContents)/;

function replayKey(request) {
  const url = request.url();
  return `${request.method()} ${url.replace(/\?.*/, '')} ${normalize(url.replace(/^[^?]*\??/, ''))}`;
}

function recordAnswers(page, answers) {
  page.on('response', async (response) => {
    const request = response.request();
    if (!REPLAYED.test(request.url()) || request.method() !== 'GET') return;
    const body = await response.body().catch(() => null);
    if (body === null) return;
    const key = replayKey(request);
    answers.set(key, [...(answers.get(key) ?? []), { status: response.status(), headers: response.headers(), body }]);
  });
}

async function replayAnswers(page, answers) {
  await page.route(REPLAYED, async (route) => {
    const request = route.request();
    const saved = request.method() === 'GET' ? answers.get(replayKey(request)) : null;
    if (!saved?.length) {
      await route.fallback();
      return;
    }
    // The last answer also serves any later asking of the same thing
    const answer = saved.length > 1 ? saved.shift() : saved[0];
    await route.fulfill(answer).catch(() => undefined);
  });
}

async function capture(which, scenario, size, file, answers) {
  const pagePath = which === 'legacy' ? options.legacy : angularPath;
  const context = await browser.newContext({
    viewport: { width: size.width, height: size.height },
    storageState: session,
    hasTouch: scenario.hasTouch ?? false,
  });
  const page = await context.newPage();
  const pending = new Set();
  const errors = [];
  const accepted = acceptedFor(scenario.name, size.label);
  const controls = fixedControls(which);
  const steps = [];
  const entries = new Map();
  let requests = [];
  let dialogs = [];

  page.answers = [...(scenario.answers ?? [])];
  page.on('pageerror', (e) => errors.push(e.message));
  page.on('dialog', async (dialog) => {
    dialogs.push(`${dialog.type()}: ${dialog.message()}`);
    if (dialog.type() === 'alert') {
      await dialog.accept();
      return;
    }
    const answer = page.answers.shift();
    if (answer === undefined || answer === false) {
      await dialog.dismiss();
    } else {
      await dialog.accept(answer === true ? undefined : answer);
    }
  });
  page.on('request', (request) => {
    const url = request.url();
    // Every request counts as pending, including card images: the Angular page shows a new collection page only once
    // its images have loaded
    pending.add(request);
    if (!url.includes('/gemp-lotr-server/')) return;
    const route = url.replace(/.*\/gemp-lotr-server/, '').replace(/\?.*/, '');
    const data = normalize(request.method() === 'GET' ? url.replace(/^[^?]*\??/, '') : (request.postData() ?? ''));
    const entry = {
      method: request.method(),
      route,
      data: which === 'legacy' ? withoutLegacyBugs(data, 'request') : data,
    };
    entries.set(request, entry);
    requests.push(entry);
  });
  const done = (request) => pending.delete(request);
  page.on('requestfinished', done);
  page.on('requestfailed', (request) => {
    done(request);
    // Cancelled by the page (a newer request replaced it): the server's answer never mattered
    if (request.failure()?.errorText.includes('ABORTED') && entries.has(request)) {
      entries.get(request).aborted = true;
    }
  });

  const snapshot = async ({ screen }) => {
    const state = await page.evaluate(readPageState, controls);
    const areas = which === 'angular' ? await page.evaluate(readAreas, [...accepted, STATS_AREA]) : [];
    const shot = screen ? await page.screenshot() : null;
    steps.push({ state, areas, shot, requests, dialogs });
    requests = [];
    dialogs = [];
  };
  page.checkpoint = async () => {
    if (!scenario.timing) {
      await settle(page, pending, 400);
    }
    await snapshot({ screen: !scenario.timing });
  };

  await page.addInitScript(MOUSE_TRACKER);
  if (options.stubbed) {
    await routeToStub(context, errors);
  } else {
    await deliverCollectionsInOrder(page);
  }
  if (which === 'angular' && injection) {
    await injection.apply(page);
  }
  if (scenario.replay && answers) {
    if (which === 'legacy') recordAnswers(page, answers);
    else await replayAnswers(page, answers);
  }
  await page.goto(options.server + pagePath);
  await page.waitForSelector(CONTROLS.firstCard[which], { timeout: 20000 }).catch(() => undefined);
  await settle(page, pending);
  const stopAnswering = which === 'angular' ? answerNotifier(page) : async () => {};
  page.settleNow = () => settle(page, pending, 400);
  if (scenario.setup) {
    try {
      await scenario.setup(page, selectors(which), which);
    } catch (error) {
      errors.push(`Could not reach the state: ${error.message.split('\n')[0]}`);
    }
    await page.settleNow();
  }
  await page.checkpoint();
  try {
    await scenario.run(page, selectors(which), which);
  } catch (error) {
    errors.push(`Scenario failed: ${error.message.split('\n')[0]}`);
  }
  await settle(page, pending);
  await stopAnswering();
  if (!scenario.keepMouse) {
    await page.mouse.move(1, 1);
    await page.waitForTimeout(300);
  }
  await snapshot({ screen: true });
  fs.writeFileSync(file, steps.at(-1).shot);
  await context.close();
  return { steps, errors };
}

/** Every saved deck of the test player, in full: what the server holds after a scenario. */
async function accountState() {
  const context = await browser.newContext({ storageState: session });
  try {
    const server = `${options.server}/gemp-lotr-server`;
    const list = await (await context.request.get(`${server}/deck/list`)).text();
    const names = [...list.matchAll(/<deck\b[^>]*>([^<]*)<\/deck>/g)].map((m) => m[1]).sort();
    const decks = {};
    for (const name of names) {
      const deck = await context.request.get(`${server}/deck`, { params: { deckName: decodeXml(name) } });
      decks[name] = await deck.text();
    }
    return decks;
  } finally {
    await context.close();
  }
}

const decodeXml = (text) =>
  text
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'");

/** Back to only the fixture deck, so both pages start a scenario from the same saved decks. */
async function resetAccount() {
  const context = await browser.newContext({ storageState: session });
  try {
    for (const name of Object.keys(await accountState())) {
      await context.request.post(`${options.server}/gemp-lotr-server/deck/delete`, {
        form: { deckName: decodeXml(name) },
      });
    }
    await createFixtureDeck(context);
  } finally {
    await context.close();
  }
}

/** The matrix opens packs: back to the seeded number of each (the throwaway server only). */
async function topUpCollection() {
  if (!options.matrix) return;
  const context = await browser.newContext({ storageState: session });
  try {
    const server = `${options.server}/gemp-lotr-server`;
    const answer = await context.request.get(`${server}/collection/permanent`, {
      params: { filter: '', start: 0, count: 1000 },
    });
    const xml = await answer.text();
    for (const line of MY_CARDS) {
      const [, wanted, blueprintId] = line.match(/^(\d+)x(.*)$/);
      const tag = xml.match(
        new RegExp(`<[^>]*blueprintId="${blueprintId.replace(/[.*+?^\${}()|[\]\\]/g, '\\$&')}"[^>]*>`),
      )?.[0];
      const have = Number(tag?.match(/count="(\d+)"/)?.[1] ?? 0);
      if (have < Number(wanted)) {
        await context.request.post(`${server}/admin/addItems`, {
          form: { players: user, collectionType: 'permanent', product: `${Number(wanted) - have}x${blueprintId}` },
        });
      }
    }
  } finally {
    await context.close();
  }
}

/** What differs in one step, and the images to keep for it. */
function compareStep(legacy, angular, areas, accepted, label, last) {
  const problems = [];
  const add = (text) => problems.push(`${label}: ${text}`);
  let pixels = null;
  if (legacy.shot && angular.shot) {
    const screens = compareScreens(legacy.shot, angular.shot, areas, false);
    pixels = screens.outside + screens.areas.reduce((sum, count) => sum + count, 0);
    if (screens.outside > tolerance) add(`${screens.outside} pixels differ`);
    screens.areas.forEach((count, i) => {
      if (count > accepted[i].maxPixels) {
        add(`${count} pixels differ in the accepted area ${accepted[i].selector} (limit ${accepted[i].maxPixels})`);
      }
    });
  }
  for (const line of compareRequests(legacy.requests, angular.requests)) add(`request, ${line}`);
  if (JSON.stringify(legacy.dialogs) !== JSON.stringify(angular.dialogs)) {
    add(`browser dialogs: legacy ${JSON.stringify(legacy.dialogs)}, Angular ${JSON.stringify(angular.dialogs)}`);
  }
  for (const line of compareStates(legacy.state, angular.state, last)) add(line);
  return { problems, pixels };
}

async function check(scenario, size) {
  const base = `${scenario.name}-${size.label}`;
  const files = {
    legacy: `${base}-legacy.png`,
    angular: `${base}-angular.png`,
    diff: `${base}-diff.png`,
  };
  const legacyFile = path.join(outDir, files.legacy);
  const angularFile = path.join(outDir, files.angular);
  let legacy;
  let angular;
  const problems = [];
  if (scenario.mutating && !options.stubbed) {
    // One after the other, from the same saved decks: both change the same account
    const answers = new Map();
    await resetAccount();
    await topUpCollection();
    legacy = await capture('legacy', scenario, size, legacyFile, answers);
    const legacyDecks = JSON.parse(withoutLegacyBugs(JSON.stringify(await accountState()), 'saved'));
    await resetAccount();
    await topUpCollection();
    angular = await capture('angular', scenario, size, angularFile, answers);
    const angularDecks = await accountState();
    await resetAccount();
    const legacyJson = JSON.stringify(legacyDecks);
    const angularJson = JSON.stringify(angularDecks);
    if (legacyJson !== angularJson) {
      let at = 0;
      while (legacyJson[at] === angularJson[at]) at++;
      const around = (text) => text.slice(Math.max(0, at - 70), at + 70);
      problems.push(
        `what the server holds differs: legacy ...${around(legacyJson)}..., Angular ...${around(angularJson)}...`,
      );
    }
  } else {
    [legacy, angular] = await Promise.all([
      capture('legacy', scenario, size, legacyFile),
      capture('angular', scenario, size, angularFile),
    ]);
  }

  const accepted = acceptedFor(scenario.name, size.label);
  const stepImages = [];
  let pixels = 0;
  if (legacy.steps.length !== angular.steps.length) {
    problems.push(`the legacy page took ${legacy.steps.length} steps, the Angular page ${angular.steps.length}`);
  }
  const count = Math.min(legacy.steps.length, angular.steps.length);
  for (let i = 0; i < count; i++) {
    const last = i === count - 1;
    const label = last ? 'end' : i === 0 ? 'on load' : `step ${i}`;
    const a = legacy.steps[i];
    const b = angular.steps[i];
    // At the end the stats have arrived, and their area counts like any other
    const stepAccepted = last ? accepted : [...accepted, STATS_AREA];
    const stepAreas = b.areas.slice(0, stepAccepted.length);
    const step = compareStep(a, b, stepAreas, stepAccepted, label, last);
    problems.push(...step.problems);
    if (last) pixels = step.pixels;
    if (a.shot && b.shot && (last || step.problems.length)) {
      const { image } = compareScreens(a.shot, b.shot, b.areas, true);
      if (last) {
        fs.writeFileSync(path.join(outDir, files.diff), image);
      } else {
        const stepBase = `${base}-step${i}`;
        fs.writeFileSync(path.join(outDir, `${stepBase}-legacy.png`), a.shot);
        fs.writeFileSync(path.join(outDir, `${stepBase}-angular.png`), b.shot);
        fs.writeFileSync(path.join(outDir, `${stepBase}-diff.png`), image);
        stepImages.push({ label, base: stepBase });
      }
    }
  }
  problems.push(...compareStats(legacy.steps, angular.steps));
  for (const e of angular.errors) problems.push(`Angular page: ${e}`);
  for (const e of legacy.errors.filter((text) => /^(Scenario failed|Could not reach)/.test(text))) {
    problems.push(`Legacy page: ${e}`);
  }
  if (options.matrix && problems.length === 0) {
    // A long list of scenarios: only the ones that differ keep their pictures
    for (const file of Object.values(files)) fs.rmSync(path.join(outDir, file), { force: true });
  }
  const result = { scenario, size, files, pixels, problems, stepImages, steps: count, legacy, angular };
  console.log(`${problems.length ? 'DIFF' : 'ok  '} ${base}${problems.length ? ': ' + problems.join('; ') : ''}`);
  return result;
}

/** What is on both pages in each state, against the list of what is tested. */
function findCoverageGaps() {
  return coverageGaps(async (which, setup) => {
    const context = await browser.newContext({ viewport: { width: 1366, height: 768 }, storageState: session });
    const page = await context.newPage();
    const pending = new Set();
    page.on('request', (request) => pending.add(request));
    page.on('requestfinished', (request) => pending.delete(request));
    page.on('requestfailed', (request) => pending.delete(request));
    page.on('dialog', (dialog) => void dialog.dismiss());
    page.settleNow = () => settle(page, pending, 400);
    await deliverCollectionsInOrder(page);
    await page.goto(options.server + (which === 'legacy' ? options.legacy : angularPath));
    await page.waitForSelector(CONTROLS.firstCard[which], { timeout: 20000 }).catch(() => undefined);
    await settle(page, pending);
    await setup(page, selectors(which), which);
    await settle(page, pending);
    return { page, close: () => context.close() };
  });
}

/** A visitor who isn't logged in must land on the login page with no popup, and the page must not start loading its data. */
async function findLoginProblems() {
  const context = await browser.newContext();
  const page = await context.newPage();
  const problems = [];
  page.on('dialog', (dialog) => {
    problems.push(`a popup appeared: ${dialog.message()}`);
    void dialog.dismiss();
  });
  page.on('request', (request) => {
    const address = new URL(request.url()).pathname;
    if (page.url().includes('/ng/') && address.startsWith('/gemp-lotr-server/') && !address.endsWith('/player')) {
      problems.push(`the page asked for ${address}`);
    }
  });
  await page.goto(options.server + angularPath);
  await page
    .waitForURL((url) => url.pathname === '/gemp-lotr/', { timeout: 10000 })
    .catch(() => {
      problems.push(`was not sent to the login page, stayed on ${page.url()}`);
    });
  await context.close();
  return problems;
}

/** Runs the jobs, `workers` at a time. */
async function runAll(jobs, workers) {
  const results = [];
  let next = 0;
  await Promise.all(
    Array.from({ length: workers }, async () => {
      while (next < jobs.length) {
        const job = jobs[next++];
        results.push(await job());
      }
    }),
  );
  return results;
}

const escape = (text) =>
  String(text ?? '').replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);

function writeReport(results, gaps = []) {
  const failed = results.filter((r) => r.problems.length);
  // For `npm run inspect -- summary <report>`
  fs.writeFileSync(
    path.join(outDir, 'results.json'),
    JSON.stringify({
      checks: results.map((r) => ({ name: `${r.scenario.name}-${r.size.label}`, problems: r.problems })),
      gaps,
    }),
  );
  const rows = (options.matrix ? failed : results)
    .map((r) => {
      const status = r.problems.length
        ? `<b class="bad">differs</b><br>${r.problems.map(escape).join('<br>')}`
        : 'same';
      const legacyErrors = r.legacy.errors.length
        ? `<br><small>legacy page errors: ${r.legacy.errors.map(escape).join('; ')}</small>`
        : '';
      const img = (file, label) => `<a href="${file}"><img src="${file}" alt="${label}"></a>`;
      return `<tr class="${r.problems.length ? 'bad' : ''}">
  <td><b>${escape(r.scenario.name)}</b><br>${escape(r.size.label)}<br><small>${escape(r.scenario.note)}</small></td>
  <td>${status}<br><small>${r.pixels} px differ</small>${legacyErrors}
    <br><small>${r.steps} steps compared</small>${r.stepImages
      .map(
        (s) =>
          `<br><small>${escape(s.label)}: <a href="${s.base}-legacy.png">legacy</a>, <a href="${s.base}-angular.png">Angular</a>, <a href="${s.base}-diff.png">differences</a></small>`,
      )
      .join('')}</td>
  <td>${img(r.files.legacy, 'legacy')}</td><td>${img(r.files.angular, 'angular')}</td><td>${img(r.files.diff, 'diff')}</td>
</tr>`;
    })
    .join('\n');
  fs.writeFileSync(
    path.join(outDir, 'index.html'),
    `<!doctype html><meta charset="utf-8"><title>Deck builder visual check</title>
<style>body{font:14px sans-serif;margin:16px}table{border-collapse:collapse}td{border:1px solid #ccc;padding:6px;vertical-align:top}
img{width:300px}tr.bad{background:#fee}.bad{color:#b00}</style>
<h1>Deck builder visual check</h1>
<p>${escape(options.server)}: <code>${escape(options.legacy)}</code> vs <code>${escape(angularPath)}</code>,
${results.length} checks, <b>${failed.length} differ</b> (tolerance ${tolerance} px). Diff images show differing
pixels in red.</p>
${gaps.length ? `<h2>Not tested (${gaps.length})</h2><ul>${gaps.map((gap) => `<li>${escape(gap)}</li>`).join('')}</ul>` : ''}
<table><tr><th>Scenario</th><th>Result</th><th>Legacy</th><th>Angular</th><th>Diff</th></tr>
${rows}</table>`,
  );
}

try {
  if (!options.stubbed) {
    const loggedIn = await login();
    await createFixtureDeck(loggedIn);
    session = await loggedIn.storageState();
    await loggedIn.close();
  }
  const jobs = (list) => list.flatMap((scenario) => sizes.map((size) => () => check(scenario, size)));
  // Scenarios that change the account run one at a time, after the others
  const results = [
    ...(await runAll(jobs(scenarios.filter((s) => !s.mutating)), Number(options.workers))),
    ...(await runAll(jobs(scenarios.filter((s) => s.mutating)), 1)),
  ];
  results.sort(
    (a, b) =>
      scenarios.indexOf(a.scenario) - scenarios.indexOf(b.scenario) || sizes.indexOf(a.size) - sizes.indexOf(b.size),
  );
  const gaps = options.matrix && !options.filter ? await findCoverageGaps() : [];
  for (const gap of gaps) console.log(`GAP  ${gap}`);
  const loginProblems = options.stubbed ? [] : await findLoginProblems();
  for (const problem of loginProblems) console.log(`LOGIN not logged in: ${problem}`);
  writeReport(results, gaps);
  const failed = results.filter((r) => r.problems.length).length;
  console.log(
    `\n${results.length} checks, ${failed} differ${options.matrix ? `, ${gaps.length} not tested` : ''}. Report: ${path.join(outDir, 'index.html')}`,
  );
  process.exitCode = failed || gaps.length || loginProblems.length ? 1 : 0;
} finally {
  await browser.close();
}
