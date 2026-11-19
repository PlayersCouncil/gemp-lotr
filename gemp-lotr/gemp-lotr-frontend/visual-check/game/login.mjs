// The Angular game page only for players who are logged in: a visitor who isn't is sent to the site's login page
// (/gemp-lotr/), with no popup, before the page asks the server for anything but who is logged in (loginRequired).
// For a game, a replay and a game watched as an admin acting for a player. Runs against any server; part of the game
// check (run.mjs), and on its own:
//
//   node game/login.mjs --server http://127.0.0.1:17001
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';

/** The page's addresses a visitor may follow (the hall's, the game history's), none of them logged in. */
export const LOGGED_OUT_ADDRESSES = [
  '/gemp-lotr/ng/game?gameId=1',
  '/gemp-lotr/ng/game?replayId=somebody$abc123',
  '/gemp-lotr/ng/game?gameId=1&participantId=somebody',
];

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
      if (page.url().includes('/ng/') && asked.startsWith('/gemp-lotr-server/') && !asked.endsWith('/player')) {
        problems.push(`${address}: the page asked for ${asked}`);
      }
    });
    await page.goto(server + address);
    await page
      .waitForURL((url) => url.pathname === '/gemp-lotr/', { timeout: 10000 })
      .catch(() => {
        problems.push(`${address}: was not sent to the login page, stayed on ${page.url()}`);
      });
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
    console.log(
      `${LOGGED_OUT_ADDRESSES.length - new Set(problems.map((p) => p.split(':')[0])).size} of ${LOGGED_OUT_ADDRESSES.length} addresses send a visitor who isn't logged in to the login page`,
    );
    process.exitCode = problems.length ? 1 : 0;
  } finally {
    await browser.close();
  }
}
