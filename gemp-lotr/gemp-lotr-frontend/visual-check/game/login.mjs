// The Angular game page only for players who are logged in: a visitor who isn't is sent to the site's login page
// (/gemp-lotr/), with no popup, before the page asks the server for anything but who is logged in (loginRequired).
// For a game and a game watched as an admin acting for a player. A replay's link opens for anybody it is shared with
// (the owner, 2026-11-20; the server serves replays without a login): the page stays, asking only for the replay.
// Runs against any server; part of the game check (run.mjs), and on its own:
//
//   node game/login.mjs --server http://127.0.0.1:17001
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';

/** The page's addresses a visitor may follow (the hall's, the game history's), none of them logged in. */
export const LOGGED_OUT_ADDRESSES = [
  '/gemp-lotr/ng/game?gameId=1',
  '/gemp-lotr/ng/game?gameId=1&participantId=somebody',
];

/** A replay's address, which a visitor who isn't logged in may open (loginRequiredUnlessReplay). */
export const PUBLIC_ADDRESSES = ['/gemp-lotr/ng/game?replayId=somebody$abc123'];

/** What the page may ask the server for at a public address: who is logged in, and the replay. */
const PUBLIC_ASKS = (asked) => asked.endsWith('/player') || asked.startsWith('/gemp-lotr-server/replay/');
/** Where a page that needs a logged-in player sends a visitor (core/platform/app-paths.ts LOGIN_PAGE_URL). */
const LOGIN_PAGE = '/gemp-lotr/ng/login';

/** The problems of each address, for a browser that isn't logged in to `server`. */
export async function loginProblems(browser, server) {
  const problems = [];
  for (const address of LOGGED_OUT_ADDRESSES) {
    const context = await browser.newContext();
    const page = await context.newPage();
    page.on('dialog', (dialog) => {
      problems.push(`${address}: a popup appeared: ${dialog.message()}`);
      void dialog.dismiss();
    });
    page.on('request', (request) => {
      const asked = new URL(request.url()).pathname;
      // The login page it is sent to asks for the hall's counts (GET /status), which needs no login
      const onLoginPage = new URL(page.url()).pathname === LOGIN_PAGE;
      if (
        page.url().includes('/ng/') &&
        asked.startsWith('/gemp-lotr-server/') &&
        !asked.endsWith('/player') &&
        !(onLoginPage && asked === '/gemp-lotr-server/status')
      ) {
        problems.push(`${address}: the page asked for ${asked}`);
      }
    });
    await page.goto(server + address);
    await page
      .waitForURL((url) => url.pathname === LOGIN_PAGE, { timeout: 10000 })
      .catch(() => {
        problems.push(`${address}: was not sent to the login page, stayed on ${page.url()}`);
      });
    await context.close();
  }
  for (const address of PUBLIC_ADDRESSES) {
    const context = await browser.newContext();
    const page = await context.newPage();
    page.on('dialog', (dialog) => {
      problems.push(`${address}: a popup appeared: ${dialog.message()}`);
      void dialog.dismiss();
    });
    let replayAsked = false;
    page.on('request', (request) => {
      const asked = new URL(request.url()).pathname;
      if (!page.url().includes('/ng/') || !asked.startsWith('/gemp-lotr-server/')) return;
      if (asked.startsWith('/gemp-lotr-server/replay/')) replayAsked = true;
      else if (!PUBLIC_ASKS(asked)) problems.push(`${address}: the page asked for ${asked}`);
    });
    await page.goto(server + address);
    await page.waitForTimeout(3000);
    if (new URL(page.url()).pathname === LOGIN_PAGE) problems.push(`${address}: was sent to the login page`);
    if (!replayAsked) problems.push(`${address}: the page didn't ask for the replay`);
    await context.close();
  }
  return problems;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const { values: options } = parseArgs({ options: { server: { type: 'string' } } });
  if (!options.server) {
    console.error('Usage: node game/login.mjs --server <url>');
    process.exit(2);
  }
  const browser = await launchBrowser();
  try {
    const problems = await loginProblems(browser, options.server);
    for (const problem of problems) console.log(`  ✗ ${problem}`);
    const failing = new Set(problems.map((p) => p.split(':')[0]));
    const all = [...LOGGED_OUT_ADDRESSES, ...PUBLIC_ADDRESSES];
    console.log(
      `${all.length - failing.size} of ${all.length} addresses as expected for a visitor who isn't logged in ` +
        '(the login page for a game, the replay itself for a replay)',
    );
    process.exitCode = problems.length ? 1 : 0;
  } finally {
    await browser.close();
  }
}
