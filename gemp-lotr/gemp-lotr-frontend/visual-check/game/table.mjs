// The table check: the Angular game page's table against game.html, without a server. Each recorded game of
// ../../src/testing/game-recordings is shown on both pages as it stood after each step, and the pages are compared:
// the screen (pixels) and the game as shown (controls.mjs readGameState).
//
// Both pages are served from disk (../stub-server.mjs) and get the game as the recording's server sent it up to that
// step, answer by answer, without the decisions (so each page asks for the next answer at once). So
// no dialog is open and no card is offered: what is compared is the table itself (G2 of trymarz/gemp-lotr-private#27). The decisions are compared by the game check
// (run.mjs), which plays real games.
//
//   node game/table.mjs [--only <recording,...>] [--sizes 1366x768,...] [--steps last|all] [--out report-game-table]
//                       [--tolerance 60] [--theme classic] [--step <n>: only that step of each recording]
//
// To look into a difference: `--eval "<expression>"` prints what the expression gives in both pages instead of
// comparing them (at the last step of each chosen recording), e.g.
//   node game/table.mjs --only opening --sizes 1366x768 --eval "document.querySelector('.phase, [data-game=phase]').getBoundingClientRect().toJSON()"
// and `--boxes "<legacy selector> | <angular selector>"` prints the box of every match of each selector side by side.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { compareScreens } from '../compare.mjs';
import { routeToStub, STUB_ORIGIN } from '../stub-server.mjs';
import { ANGULAR_CONTRACT, LEGACY, readGameState } from './controls.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const RECORDINGS = path.resolve(HERE, '../../src/testing/game-recordings');

const { values: options } = parseArgs({
  options: {
    only: { type: 'string' },
    sizes: { type: 'string', default: '1280x800,1366x768,1920x1080' },
    steps: { type: 'string', default: 'all' },
    out: { type: 'string', default: 'report-game-table' },
    tolerance: { type: 'string', default: '60' },
    theme: { type: 'string', default: 'classic' },
    workers: { type: 'string', default: '4' },
    step: { type: 'string' },
    eval: { type: 'string' },
    boxes: { type: 'string' },
  },
});
const OUT = path.resolve(HERE, '..', options.out);
const TOLERANCE = Number(options.tolerance);
const GAME_ID = '77';
/** How long the stand-in server holds a question for news before answering that there is none. */
const HELD_MS = 8000;

/**
 * The game's answers of the first `count` exchanges, one by one as the server gave them, without their decisions:
 * the first as the page opens the game, each of the others to the page's next question for news.
 */
function gameUpTo(recording, count) {
  return recording.exchanges
    .slice(0, count)
    .filter((exchange) => exchange.path === '/game/<id>' && exchange.status === 200)
    .map((exchange) =>
      exchange.body.replace(/<ge\b[^>]*?(?:\/>|>[\s\S]*?<\/ge>)/g, (event) =>
        /\stype="D"/.test(event.slice(0, event.indexOf('>'))) ? '' : event,
      ),
    );
}

/**
 * Answers the game's calls from the recording: the game as it opened, then one recorded answer to each of the page's
 * questions for news, and the room's first answer; later questions are held, then answered with no news.
 */
async function routeGame(context, recording, count, log) {
  const player = recording.exchanges.find((exchange) => exchange.path === '/player');
  const [opening, ...rest] = gameUpTo(recording, count);
  // Who is in the room: in every answer of the chat
  const users = `<user>${recording.participant}</user><user>${recording.opponent ?? 'GameFoe1'}</user>`;
  await context.route(`${STUB_ORIGIN}/gemp-lotr-server/**`, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const call = `${request.method()} ${url.pathname.replace('/gemp-lotr-server', '')}`;
    if (call === 'GET /player') {
      return route.fulfill({ contentType: 'application/json; charset=UTF-8', body: player?.body ?? '{}' });
    }
    if (call === `GET /game/${GAME_ID}`) {
      return route.fulfill({ contentType: 'application/xml; charset=UTF-8', body: opening });
    }
    if (call === `POST /game/${GAME_ID}` && rest.length > 0) {
      return route.fulfill({ contentType: 'application/xml; charset=UTF-8', body: rest.shift() });
    }
    if (call === `GET /chat/Game${GAME_ID}`) {
      return route.fulfill({
        contentType: 'application/xml; charset=UTF-8',
        body:
          '<?xml version="1.0" encoding="UTF-8"?><chat roomName="Game#">' +
          '<message date="1794571436000" from="System">Welcome to room: Game#</message>' +
          `${users}</chat>`,
      });
    }
    // The long poll and the chat's next question: the server holds them a while, then says nothing happened
    if (call === `POST /game/${GAME_ID}` || call === `POST /chat/Game${GAME_ID}`) {
      const nothing = call.includes('/chat/') ? `<chat>${users}</chat>` : '<update cn="1"/>';
      await new Promise((resolve) => setTimeout(resolve, HELD_MS));
      return route
        .fulfill({
          contentType: 'application/xml; charset=UTF-8',
          body: `<?xml version="1.0" encoding="UTF-8"?>${nothing}`,
        })
        .catch(() => undefined);
    }
    log.push(`unanswered ${call}`);
    return route.fulfill({ status: 404, body: '' });
  });
  return () => rest.length;
}

function inPage(page, fn, arg) {
  return page.evaluate(([source, arg]) => new Function(`return (${source})`)()(arg), [fn.toString(), arg]);
}

/** Waits until the page has drawn what it has and stands still. */
async function settled(page, waiting = () => 0) {
  // Every picture has loaded, nothing moves, no answer is still to come, and nothing has changed for a while
  let last = '';
  let same = 0;
  for (let i = 0; i < 200 && same < 4; i++) {
    await page.waitForTimeout(150);
    const now = await page.evaluate(() => {
      const loaded = Array.from(document.images).every((img) => img.complete) && document.getAnimations().length === 0;
      return loaded ? document.body.innerHTML.length + ':' + document.querySelectorAll('img').length : '';
    });
    same = now !== '' && now === last && waiting() === 0 ? same + 1 : 0;
    last = now;
  }
}

/** Opens one page on the game as it stood after `count` exchanges; `snapshot()` reads it once it stands still. */
async function open(browser, kind, recording, count, size) {
  const [width, height] = size.split('x').map(Number);
  const context = await browser.newContext({ viewport: { width, height } });
  const log = [];
  const errors = [];
  await routeToStub(context, log);
  let waiting = await routeGame(context, recording, count, log);
  // The same settings on both pages (cookies of the stub's origin)
  await context.addCookies(
    [
      ['animation-speed', '4'],
      ['gempGameTheme', options.theme],
    ].map(([name, value]) => ({ name, value, domain: new URL(STUB_ORIGIN).hostname, path: '/gemp-lotr' })),
  );
  const page = await context.newPage();
  page.on('pageerror', (error) => errors.push(String(error)));
  page.on('console', (message) => {
    if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) errors.push(message.text());
  });
  const pagePath = kind === 'legacy' ? '/gemp-lotr/game.html' : '/gemp-lotr/ng/game';
  await page.goto(`${STUB_ORIGIN}${pagePath}?gameId=${GAME_ID}`);
  await page.mouse.move(1, 1);
  const select = kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT;
  return {
    page,
    select,
    async snapshot() {
      await page.mouse.move(1, 1);
      await settled(page, waiting);
      return {
        screen: await page.screenshot({
          mask: select.masks.map((selector) => page.locator(selector)),
          maskColor: '#ff00ff',
        }),
        state: await inPage(page, readGameState, { kind }),
        problems: [
          ...errors.splice(0),
          ...log.splice(0).filter((line) => !/favicon|site\.webmanifest|apple-touch/.test(line)),
        ],
      };
    },
    /** The page loaded again: the server gives it the same game from the start. */
    async reload() {
      await context.unroute(`${STUB_ORIGIN}/gemp-lotr-server/**`);
      waiting = await routeGame(context, recording, count, log);
      await page.reload();
    },
    close: () => context.close(),
  };
}

/** Opens one page on the game and reads it (or, with `expression`, what the expression gives in it). */
async function show(browser, kind, recording, count, size, expression = null) {
  const shown = await open(browser, kind, recording, count, size);
  let result;
  if (expression !== null) {
    await settled(shown.page);
    result = await shown.page.evaluate(expression).catch((error) => `failed: ${error.message.split('\n')[0]}`);
  } else {
    result = await shown.snapshot();
  }
  await shown.close();
  return result;
}

/**
 * What the table itself doesn't show, on the game's last state: each tab of the tab box, and each player's other
 * piles opened. One gesture after the other on a page; the same on both.
 */
const VIEWS = [
  { name: 'settings tab', act: (shown) => shown.page.locator(shown.select.tab('settings')).click() },
  { name: 'options tab', act: (shown) => shown.page.locator(shown.select.tab('options')).click() },
  { name: 'players tab', act: (shown) => shown.page.locator(shown.select.tab('players')).click() },
  { name: 'chat tab again', act: (shown) => shown.page.locator(shown.select.tab('chat')).click() },
  { name: "the first player's other piles", act: (shown) => shown.page.locator(shown.select.statsToggle(0)).click() },
  { name: "the second player's other piles", act: (shown) => shown.page.locator(shown.select.statsToggle(1)).click() },
  { name: 'settings tab, piles open', act: (shown) => shown.page.locator(shown.select.tab('settings')).click() },
];

/** The recordings whose last state is also compared view by view (VIEWS) and, on the Angular page, after a reload. */
const WITH_VIEWS = ['fellowship-play', 'spectator'];

function stateDifferences(a, b) {
  const differences = [];
  for (const key of new Set([...Object.keys(a), ...Object.keys(b)])) {
    const [left, right] = [JSON.stringify(a[key]), JSON.stringify(b[key])];
    if (left !== right) differences.push(`${key}: legacy ${left} / angular ${right}`);
  }
  return differences;
}

const names = fs
  .readdirSync(RECORDINGS)
  .filter((file) => file.endsWith('.json'))
  .map((file) => file.replace(/\.json$/, ''))
  .filter((name) => !options.only || options.only.split(',').includes(name))
  .sort();

const jobs = [];
for (const name of names) {
  const recording = JSON.parse(fs.readFileSync(path.join(RECORDINGS, `${name}.json`), 'utf8'));
  // Steps that sent nothing since the one before show the same table: once is enough
  const steps = recording.steps.filter((step, i, all) => i === 0 || step.exchanges !== all[i - 1].exchanges);
  const chosen = options.step
    ? [recording.steps[Number(options.step)]].filter(Boolean)
    : options.steps === 'last'
      ? steps.slice(-1)
      : steps;
  for (const size of options.sizes.split(',')) {
    for (const step of chosen) {
      const views = WITH_VIEWS.includes(name) && step === steps.at(-1) && !options.eval && !options.boxes;
      jobs.push({ name, recording, step, size, views });
    }
  }
}

if (options.eval || options.boxes) {
  const boxesOf = (selector) =>
    `Array.from(document.querySelectorAll(${JSON.stringify(selector)})).map((el) => { const b = el.getBoundingClientRect(); return [b.left, b.top, b.width, b.height].map((n) => Math.round(n * 100) / 100).join(' ') + ' ' + el.textContent.trim().slice(0, 20); })`;
  const [legacySelector, angularSelector] = (options.boxes ?? '').split('|').map((part) => part.trim());
  const browser = await launchBrowser();
  for (const job of jobs.filter((j, i, all) => all[i + 1]?.name !== j.name || all[i + 1]?.size !== j.size)) {
    for (const kind of ['legacy', 'angular']) {
      const expression =
        options.eval ?? boxesOf(kind === 'legacy' ? legacySelector : (angularSelector ?? legacySelector));
      const value = await show(browser, kind, job.recording, job.step.exchanges, job.size, expression);
      console.log(`${job.name} ${job.size} ${kind}:`, JSON.stringify(value, null, 1));
    }
  }
  await browser.close();
  process.exit(0);
}

fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(OUT, { recursive: true });
const browser = await launchBrowser();
const results = [];
let next = 0;
async function worker() {
  while (next < jobs.length) {
    const job = jobs[next++];
    const { name, recording, step, size } = job;
    const legacy = await show(browser, 'legacy', recording, step.exchanges, size);
    const angular = await show(browser, 'angular', recording, step.exchanges, size);
    const base = `${name}-${size}-${String(recording.steps.indexOf(step)).padStart(2, '0')}`;
    record(name, size, step.name, base, legacy, angular);
    if (job.views) await compareViews(job);
  }
}

/** Compares two snapshots and keeps the result, with their pictures. */
function record(name, size, step, base, legacy, angular, labels = ['legacy', 'angular']) {
  const screens = compareScreens(legacy.screen, angular.screen, [], true);
  const differences = [
    ...(screens.outside > TOLERANCE ? [`screen: ${screens.outside} pixels differ`] : []),
    ...stateDifferences(legacy.state, angular.state),
    ...legacy.problems.map((problem) => `${labels[0]}: ${problem}`),
    ...angular.problems.map((problem) => `${labels[1]}: ${problem}`),
  ];
  fs.writeFileSync(path.join(OUT, `${base}-legacy.png`), legacy.screen);
  fs.writeFileSync(path.join(OUT, `${base}-angular.png`), angular.screen);
  if (screens.image) fs.writeFileSync(path.join(OUT, `${base}-diff.png`), screens.image);
  results.push({ name, size, step, base, pixels: screens.outside, differences });
  console.log(
    `${differences.length ? 'DIFFER' : 'same  '} ${name} ${size} "${step}"${differences.length ? `: ${differences[0]}` : ''}`,
  );
}

/**
 * The views of VIEWS on both pages, one after the other; then the Angular page loaded again, which must show what it
 * showed before (the open tab and piles are the page's own: D10 of trymarz/gemp-lotr-private#27).
 */
async function compareViews({ name, recording, step, size }) {
  const legacy = await open(browser, 'legacy', recording, step.exchanges, size);
  const angular = await open(browser, 'angular', recording, step.exchanges, size);
  await legacy.snapshot();
  await angular.snapshot();
  // A dialog over the page (the end of the game's) is closed first
  for (const shown of [legacy, angular]) {
    const close = shown.page.locator('.ui-dialog-titlebar-close, .gd-dialog__close').filter({ visible: true });
    for (let n = await close.count(); n > 0; n--) await close.last().click();
  }
  let before = null;
  for (const [index, view] of VIEWS.entries()) {
    await view.act(legacy);
    await view.act(angular);
    before = await angular.snapshot();
    record(name, size, view.name, `${name}-${size}-view${index}`, await legacy.snapshot(), before);
  }
  await angular.reload();
  const after = await angular.snapshot();
  record(name, size, 'the Angular page before and after a reload', `${name}-${size}-reload`, before, after, [
    'before',
    'after',
  ]);
  await legacy.close();
  await angular.close();
}

await Promise.all(Array.from({ length: Math.min(Number(options.workers), jobs.length) }, worker));
await browser.close();

results.sort((a, b) => a.base.localeCompare(b.base));
const escape = (text) => String(text).replace(/[&<>]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[c]);
fs.writeFileSync(
  path.join(OUT, 'index.html'),
  `<!doctype html><meta charset="utf-8"><title>Game table check</title>
<style>body{font:14px sans-serif;background:#222;color:#eee}img{max-width:32%;border:1px solid #555}
.differ{color:#f88}.same{color:#8f8}li{white-space:pre-wrap}</style>
<h1>Game table check: ${results.filter((r) => !r.differences.length).length} of ${results.length} the same</h1>
${results
  .map(
    (r) => `<h3 class="${r.differences.length ? 'differ' : 'same'}">${escape(r.name)} ${r.size} "${escape(r.step)}": ${
      r.differences.length ? 'DIFFERS' : 'same'
    } (${r.pixels} px)</h3>
<ul>${r.differences.map((d) => `<li>${escape(d)}</li>`).join('')}</ul>
${r.differences.length ? `<img src="${r.base}-legacy.png" title="legacy"><img src="${r.base}-angular.png" title="angular"><img src="${r.base}-diff.png" title="difference">` : ''}`,
  )
  .join('\n')}`,
);
fs.writeFileSync(path.join(OUT, 'results.json'), JSON.stringify(results, null, 1));
const differing = results.filter((r) => r.differences.length);
console.log(
  `\n${results.length - differing.length} of ${results.length} the same; report: ${path.join(OUT, 'index.html')}`,
);
process.exit(differing.length ? 1 : 0);
