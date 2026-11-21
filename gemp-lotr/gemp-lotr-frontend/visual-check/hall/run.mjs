#!/usr/bin/env node
// Compares the Angular hall and login page with the legacy pages they replace (hall-legacy.html, login-legacy.html),
// without a server: both are served from disk and the server's answers come from src/testing/server-responses
// (stub-server.mjs with hall/stubs.mjs). Each scenario (hall/scenarios.mjs) opens both pages at the same place and
// takes them through the same steps; the screens are compared after loading and after each step. Then the old
// addresses (hall.html#..., the start page) are followed as a player and as a visitor (hall/navigation.mjs). Writes
// <out>/index.html; exits with 1 when something differs. See README.md.
//
//   npm run check:hall
//   npm run check:hall -- --only events-calendar,login --sizes 1366x768,1280x800 --tolerance 60 --out report-hall
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { gameEntryApplies, HALL_ACCEPTED } from '../accepted.mjs';
import { BROWSER_NAME, launchBrowser } from '../browser.mjs';
import { compareScreens } from '../compare.mjs';
import { GESTURES, pause } from '../gestures.mjs';
import { routeToStub, STUB_ORIGIN } from '../stub-server.mjs';
import { checkNavigation } from './navigation.mjs';
import { SCENARIOS } from './scenarios.mjs';
import { hallAnswers } from './stubs.mjs';

const { values: options } = parseArgs({
  options: {
    out: { type: 'string', default: 'report-hall' },
    only: { type: 'string' },
    sizes: { type: 'string', default: '1366x768' },
    tolerance: { type: 'string', default: '60' },
    workers: { type: 'string', default: '2' },
    'no-navigation': { type: 'boolean', default: false },
  },
});

/** When the pages think it starts: the fixtures' November 2026 (the calendar's month and today, the tables' ages). */
export const CHECK_TIME = new Date('2026-11-20T12:00:00Z');
const tolerance = Number(options.tolerance);
const outDir = path.resolve(options.out);
const sizes = options.sizes.split(',').map((label) => {
  const [width, height] = label.split('x').map(Number);
  return { label, width, height };
});
const only = options.only ? options.only.split(',') : null;
const scenarios = SCENARIOS.filter((s) => !only || only.includes(s.name));
if (only && scenarios.length !== only.length) {
  const unknown = only.filter((name) => !SCENARIOS.some((s) => s.name === name));
  if (unknown.length) {
    console.error(`Unknown scenario: ${unknown.join(', ')}`);
    process.exit(2);
  }
}
fs.rmSync(outDir, { recursive: true, force: true });
fs.mkdirSync(outDir, { recursive: true });

const browser = await launchBrowser();

/** A page of the check: its own context, the stub server, the fixed time, the built-in chat (not Discord's embed). */
export async function openPage(size, stubs, log) {
  const context = await browser.newContext({ viewport: { width: size.width, height: size.height } });
  await context.addInitScript(() => localStorage.setItem('gemp.hallChat', 'builtin'));
  await routeToStub(context, log, { answer: hallAnswers(stubs) });
  const page = await context.newPage();
  // The clock starts at CHECK_TIME and runs on: a stopped clock would stop jQuery's animations (the legacy dialogs)
  await page.clock.install({ time: CHECK_TIME });
  await page.clock.resume();
  const errors = [];
  page.on('pageerror', (error) => errors.push(String(error.message ?? error).slice(0, 200)));
  // A game the hall opens (hall.xml's <newGame>) gets a tab of its own: closed, it changes nothing here
  context.on('page', (opened) => opened !== page && opened.close().catch(() => {}));
  return { context, page, errors };
}

/** One action of a step (scenarios.mjs): a gesture of gestures.mjs, or a form field, a key or a wait. */
async function act(page, [gesture, selector, value]) {
  if (gesture === 'wait') return page.waitForTimeout(Number(selector));
  if (gesture === 'key') return page.keyboard.press(selector);
  const target = page.locator(selector).filter({ visible: true }).first();
  await target.waitFor({ state: 'visible', timeout: 5000 });
  if (gesture === 'fill') return target.fill(value);
  if (gesture === 'select') return target.selectOption(value);
  if (gesture === 'scrollTo') return target.scrollIntoViewIfNeeded();
  const run = GESTURES[gesture];
  if (!run) throw new Error(`Unknown gesture ${gesture}`);
  return run(page, target);
}

/** The accepted areas on the Angular page for this screenshot. */
async function acceptedAreas(page, scenario, size, step) {
  const entries = HALL_ACCEPTED.filter((entry) => gameEntryApplies(entry, scenario.name, size.label, step));
  return Promise.all(
    entries.map(async (entry) => {
      const box = await page
        .locator(entry.selector)
        .filter({ visible: true })
        .first()
        .boundingBox({ timeout: 500 })
        .catch(() => null);
      const margin = entry.margin ?? 0;
      return (
        box && { x: box.x - margin, y: box.y - margin, width: box.width + 2 * margin, height: box.height + 2 * margin }
      );
    }),
  );
}

async function check(scenario, size) {
  const log = [];
  const base = `${scenario.name}-${size.label}`;
  const legacy = await openPage(size, scenario.stubs ?? {}, log);
  const angular = await openPage(size, scenario.stubs ?? {}, log);
  const problems = [];
  const shots = [];
  try {
    await Promise.all([
      legacy.page.goto(STUB_ORIGIN + scenario.legacy),
      angular.page.goto(`${STUB_ORIGIN}${scenario.angular}${scenario.angular.includes('?') ? '&' : '?'}theme=classic`),
    ]);
    const settle = scenario.settle ?? 2500;
    await Promise.all([pause(legacy.page, settle), pause(angular.page, settle)]);
    const compare = async (label) => {
      const [a, b] = await Promise.all([legacy.page.screenshot(), angular.page.screenshot()]);
      const areas = await acceptedAreas(angular.page, scenario, size, label);
      const result = compareScreens(a, b, areas, true);
      const file = `${base}-${shots.length}`;
      fs.writeFileSync(path.join(outDir, `${file}-legacy.png`), a);
      fs.writeFileSync(path.join(outDir, `${file}-angular.png`), b);
      if (result.image) fs.writeFileSync(path.join(outDir, `${file}-diff.png`), result.image);
      const pixels = result.outside;
      shots.push({ label, file, pixels });
      if (pixels > tolerance) {
        problems.push(`${label}: ${pixels === Infinity ? 'the screens differ in size' : `${pixels} px differ`}`);
      }
    };
    await compare('loaded');
    for (const [label, legacySteps, angularSteps] of scenario.steps) {
      const run = async (side, steps) => {
        try {
          for (const action of steps) await act(side.page, action);
        } catch (error) {
          problems.push(
            `${label}: the ${side === legacy ? 'legacy' : 'Angular'} page could not: ${String(error.message).split('\n')[0]}`,
          );
        }
      };
      await Promise.all([run(legacy, legacySteps), run(angular, angularSteps)]);
      await Promise.all([pause(legacy.page, 1000), pause(angular.page, 1000)]);
      await compare(label);
    }
  } catch (error) {
    problems.push(`could not run: ${String(error.message).split('\n')[0]}`);
  } finally {
    await legacy.context.close();
    await angular.context.close();
  }
  for (const error of angular.errors) problems.push(`Angular page error: ${error}`);
  const unanswered = [...new Set(log)];
  const result = { scenario, size, problems, shots, legacyErrors: legacy.errors, unanswered };
  console.log(`${problems.length ? 'DIFF' : 'SAME'} ${base}${problems.length ? `: ${problems.join('; ')}` : ''}`);
  return result;
}

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

function writeReport(results, navigation) {
  const failed = results.filter((r) => r.problems.length);
  const navigationFailed = navigation.filter((n) => n.problems.length);
  fs.writeFileSync(
    path.join(outDir, 'results.json'),
    JSON.stringify({
      checks: [
        ...results.map((r) => ({
          name: `${r.scenario.name}-${r.size.label}`,
          only: r.scenario.name,
          problems: r.problems,
        })),
        ...navigation.map((n) => ({ name: `navigation ${n.label}`, problems: n.problems })),
      ],
    }),
  );
  const img = (file, label) => `<a href="${file}"><img src="${file}" alt="${label}"></a>`;
  const rows = results
    .map((r) => {
      const status = r.problems.length
        ? `<b class="bad">differs</b><br>${r.problems.map(escape).join('<br>')}`
        : 'same';
      const notes = [
        r.legacyErrors.length ? `legacy page errors: ${r.legacyErrors.map(escape).join('; ')}` : '',
        r.unanswered.length ? `not answered by the stubs: ${r.unanswered.map(escape).join('; ')}` : '',
      ]
        .filter(Boolean)
        .map((note) => `<br><small>${note}</small>`)
        .join('');
      const last = r.shots.at(-1);
      const steps = r.shots
        .map(
          (s) =>
            `<br><small>${escape(s.label)} (${s.pixels === Infinity ? 'size' : s.pixels} px): <a href="${s.file}-legacy.png">legacy</a>, <a href="${s.file}-angular.png">Angular</a>, <a href="${s.file}-diff.png">differences</a></small>`,
        )
        .join('');
      const pictures = last
        ? `<td>${img(`${last.file}-legacy.png`, 'legacy')}</td><td>${img(`${last.file}-angular.png`, 'Angular')}</td><td>${img(`${last.file}-diff.png`, 'diff')}</td>`
        : '<td></td><td></td><td></td>';
      return `<tr class="${r.problems.length ? 'bad' : ''}">
  <td><b>${escape(r.scenario.name)}</b><br>${escape(r.size.label)}<br><small>${escape(r.scenario.note)}</small></td>
  <td>${status}${notes}${steps}</td>${pictures}
</tr>`;
    })
    .join('\n');
  const navigationRows = navigation
    .map(
      (n) => `<tr class="${n.problems.length ? 'bad' : ''}"><td><code>${escape(n.label)}</code></td>
  <td>${escape(n.legacy)}</td><td>${escape(n.angular)}</td>
  <td>${n.problems.length ? `<b class="bad">${n.problems.map(escape).join('<br>')}</b>` : 'same'}</td></tr>`,
    )
    .join('\n');
  fs.writeFileSync(
    path.join(outDir, 'index.html'),
    `<!doctype html><meta charset="utf-8"><title>Hall visual check</title>
<style>body{font:14px sans-serif;margin:16px}table{border-collapse:collapse;margin-bottom:24px}td,th{border:1px solid #ccc;padding:6px;vertical-align:top;text-align:left}
img{width:300px}tr.bad{background:#fee}.bad{color:#b00}</style>
<h1>Hall visual check</h1>
<p>${escape(BROWSER_NAME)}, stubbed server: the legacy hall and login page against the Angular ones, ${results.length}
scenarios, <b>${failed.length} differ</b> (tolerance ${tolerance} px per screenshot); ${navigation.length} old addresses,
<b>${navigationFailed.length} lead elsewhere</b>. Diff images show differing pixels in red; each row's pictures are its
last step.</p>
<h2>Screens</h2>
<table><tr><th>Scenario</th><th>Result</th><th>Legacy</th><th>Angular</th><th>Diff</th></tr>
${rows}</table>
<h2>Old addresses</h2>
<table><tr><th>Address</th><th>Legacy hall</th><th>Angular hall</th><th>Result</th></tr>
${navigationRows}</table>`,
  );
}

try {
  const jobs = scenarios.flatMap((scenario) => sizes.map((size) => () => check(scenario, size)));
  const results = await runAll(jobs, Number(options.workers));
  results.sort(
    (a, b) =>
      scenarios.indexOf(a.scenario) - scenarios.indexOf(b.scenario) || sizes.indexOf(a.size) - sizes.indexOf(b.size),
  );
  const navigation = options['no-navigation'] || only ? [] : await checkNavigation(openPage, sizes[0]);
  writeReport(results, navigation);
  const failed = results.filter((r) => r.problems.length).length;
  const navigationFailed = navigation.filter((n) => n.problems.length).length;
  console.log(
    `\n${results.length} scenarios, ${failed} differ; ${navigation.length} old addresses, ${navigationFailed} lead elsewhere. Report: ${path.join(outDir, 'index.html')}`,
  );
  process.exitCode = failed || navigationFailed ? 1 : 0;
} finally {
  await browser.close();
}
