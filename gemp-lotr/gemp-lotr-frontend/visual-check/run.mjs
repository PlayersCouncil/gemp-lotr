// Compares the Angular deck builder with the legacy deckBuild.html: every scenario of scenarios.mjs runs on both
// pages in a real browser; the screenshots must match and both pages must ask the server for the same collection
// page and deck stats. Writes report/index.html; exits with 1 when something differs. See README.md.
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { chromium } from 'playwright';
import { PNG } from 'pngjs';
import { CONTROLS } from './controls.mjs';
import { FIXTURE_DECK, SCENARIOS, selectors } from './scenarios.mjs';

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
    workers: { type: 'string', default: '4' },
    // Pixels allowed to differ by more than 2 colour levels (anti-aliasing of rounded corners and SVG edges varies by
    // a few pixels between two loads of the same page)
    tolerance: { type: 'string', default: '60' },
    out: { type: 'string', default: 'report' },
  },
});

if (!['classic', 'modern'].includes(options.theme)) {
  console.error(`Unknown theme ${options.theme}: use --theme=classic or --theme=modern.`);
  process.exit(2);
}
const angularUrl = new URL(options.angular, 'http://server');
if (!angularUrl.searchParams.has('theme')) angularUrl.searchParams.set('theme', options.theme);
const angularPath = angularUrl.pathname + angularUrl.search;

const user = process.env.GEMP_USER;
const password = process.env.GEMP_PASSWORD;
if (!user || !password) {
  console.error('Set GEMP_USER and GEMP_PASSWORD to a player on the server (see README.md).');
  process.exit(2);
}

const sizes = options.sizes.split(',').map((s) => {
  const [width, height] = s.split('x').map(Number);
  return { width, height, label: s };
});
const only = options.only ? new Set(options.only.split(',')) : null;
const scenarios = SCENARIOS.filter((s) => (only ? only.has(s.name) : !s.mutating || options.mutating));
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

async function capture(which, scenario, size, file) {
  const pagePath = which === 'legacy' ? options.legacy : angularPath;
  const context = await browser.newContext({
    viewport: { width: size.width, height: size.height },
    storageState: session,
  });
  const page = await context.newPage();
  const pending = new Set();
  const errors = [];
  const requests = { collection: null, stats: null };
  page.answers = [...(scenario.answers ?? [])];
  page.on('pageerror', (e) => errors.push(e.message));
  page.on('dialog', async (dialog) => {
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
    if (route.startsWith('/collection/') && request.method() === 'GET') {
      requests.collection = `${route}?${normalize(url.replace(/^[^?]*\??/, ''))}`;
    } else if (route === '/deck/stats') {
      requests.stats = normalize(request.postData() ?? '');
    }
  });
  const done = (request) => pending.delete(request);
  page.on('requestfinished', done);
  page.on('requestfailed', done);

  await deliverCollectionsInOrder(page);
  await page.goto(options.server + pagePath);
  await page.waitForSelector(CONTROLS.firstCard[which], { timeout: 20000 }).catch(() => undefined);
  await settle(page, pending);
  const stopAnswering = which === 'angular' ? answerNotifier(page) : async () => {};
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
  await page.screenshot({ path: file });
  await context.close();
  return { requests, errors };
}

/** Pixels differing by more than 2 levels in any channel; writes a dimmed copy with them in red. */
function compareImages(fileA, fileB, diffFile) {
  const a = PNG.sync.read(fs.readFileSync(fileA));
  const b = PNG.sync.read(fs.readFileSync(fileB));
  if (a.width !== b.width || a.height !== b.height) {
    return Infinity;
  }
  const diff = new PNG({ width: a.width, height: a.height });
  let count = 0;
  for (let i = 0; i < a.data.length; i += 4) {
    const d = Math.max(
      Math.abs(a.data[i] - b.data[i]),
      Math.abs(a.data[i + 1] - b.data[i + 1]),
      Math.abs(a.data[i + 2] - b.data[i + 2]),
    );
    if (d > 2) {
      count++;
      diff.data.set([255, 0, 0, 255], i);
    } else {
      diff.data.set([a.data[i] / 3, a.data[i + 1] / 3, a.data[i + 2] / 3, 255], i);
    }
  }
  fs.writeFileSync(diffFile, PNG.sync.write(diff));
  return count;
}

async function check(scenario, size) {
  const base = `${scenario.name}-${size.label}`;
  const files = {
    legacy: `${base}-legacy.png`,
    angular: `${base}-angular.png`,
    diff: `${base}-diff.png`,
  };
  const [legacy, angular] = await Promise.all([
    capture('legacy', scenario, size, path.join(outDir, files.legacy)),
    capture('angular', scenario, size, path.join(outDir, files.angular)),
  ]);
  const pixels = compareImages(
    path.join(outDir, files.legacy),
    path.join(outDir, files.angular),
    path.join(outDir, files.diff),
  );
  const problems = [];
  if (pixels > tolerance) problems.push(`${pixels} pixels differ`);
  if (legacy.requests.collection !== angular.requests.collection) problems.push('different collection request');
  if (legacy.requests.stats !== angular.requests.stats) problems.push('different deck stats request');
  for (const e of angular.errors) problems.push(`Angular page: ${e}`);
  const result = { scenario, size, files, pixels, legacy, angular, problems };
  console.log(`${problems.length ? 'DIFF' : 'ok  '} ${base}${problems.length ? ': ' + problems.join('; ') : ''}`);
  return result;
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

function writeReport(results) {
  const failed = results.filter((r) => r.problems.length);
  const rows = results
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
    <br><small>collection: ${escape(r.angular.requests.collection)}</small></td>
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
<table><tr><th>Scenario</th><th>Result</th><th>Legacy</th><th>Angular</th><th>Diff</th></tr>
${rows}</table>`,
  );
}

try {
  const loggedIn = await login();
  await createFixtureDeck(loggedIn);
  session = await loggedIn.storageState();
  await loggedIn.close();
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
  writeReport(results);
  const failed = results.filter((r) => r.problems.length).length;
  console.log(`\n${results.length} checks, ${failed} differ. Report: ${path.join(outDir, 'index.html')}`);
  process.exitCode = failed ? 1 : 0;
} finally {
  await browser.close();
}
