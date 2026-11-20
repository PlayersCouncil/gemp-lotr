// The scenario authoring checks: a real browser writes scenarios in the panel docked beside the Angular game page,
// against a kept server (`npm run check:game -- --only start --sizes 1366x768 --keep`, then
// `npm run check:game:author`, then `npm run check:game:down`). See "The authoring checks" in README.md.
//
// Options: `--only dock,timeline` (the checks to run), `--theme classic` (default: both), `--out <dir>`
// (default report-author), `--verbose` (every move as it happens).
//
// The checks are in author/*.mjs, one file per part of the panel, each exporting `checks`: a check is
// `async (table, problems) => …` (see author/lib.mjs). Every check makes a new draft of its own and closes it; none
// saves over a scenario the server ships.

import fs from 'node:fs';
import path from 'node:path';
import { launchBrowser } from '../browser.mjs';
import { session } from '../local-server/seed.mjs';
import { argument, closeDraft, OUT, password, Seat, server } from './author/lib.mjs';
import { pairOf } from './players.mjs';
import { checks as shell } from './author/shell.mjs';
import { checks as step } from './author/step.mjs';
import { checks as board } from './author/board.mjs';
import { checks as scenario } from './author/scenario.mjs';
import { checks as opponent } from './author/opponent.mjs';
import { checks as picking } from './author/picking.mjs';
import { checks as start } from './author/start.mjs';
import { checks as opponentOrders } from './author/opponent-orders.mjs';
import { checks as endings } from './author/endings.mjs';

/** Every check by name; a part's file adds its own here. */
const CHECKS = {
  ...shell,
  ...step,
  ...board,
  ...scenario,
  ...opponent,
  ...picking,
  ...start,
  ...opponentOrders,
  ...endings,
};

const ONLY = argument('only', '').split(',').filter(Boolean);
const THEMES = argument('theme', 'classic,modern').split(',').filter(Boolean);
const SIZE = argument('size', '1600x1000').split('x').map(Number);

let browser;

async function run(name, theme) {
  const check = `${name}-${theme}`;
  const problems = [];
  const started = Date.now();
  const player = pairOf(0).player;
  const table = { theme, note: '' };
  try {
    table.who = await session(server, player, password);
    const { hostname } = new URL(server);
    const context = await browser.newContext({ viewport: { width: SIZE[0], height: SIZE[1] } });
    const cookies = table.who.cookie.split('; ').map((text) => {
      const [cookie, ...value] = text.split('=');
      return { name: cookie, value: value.join('='), domain: hostname, path: '/' };
    });
    await context.addCookies([
      ...cookies,
      { name: 'animation-speed', value: '4', domain: hostname, path: '/' },
      { name: 'gempGameTheme', value: theme, domain: hostname, path: '/' },
    ]);
    table.seat = new Seat(check, await context.newPage(), table.who);
    await CHECKS[name](table, problems);
  } catch (error) {
    problems.push(error.message.split('\n')[0]);
    await table.seat?.shot('failed');
  }
  if (table.seat) {
    problems.push(...new Set(table.seat.errors));
    await closeDraft(table.seat);
    await table.seat.page.context().close();
  }
  const seconds = Math.round((Date.now() - started) / 1000);
  const lines = [
    `${problems.length ? '  ✗' : '  ✓'} ${name} (${theme}, ${seconds}s)${table.note ? ` ${table.note}` : ''}`,
    ...problems.map((problem) => `      ${problem}`),
  ];
  console.log(lines.join('\n'));
  fs.writeFileSync(
    path.join(OUT, `${check}.txt`),
    [...lines, '', 'Moves:', ...(table.seat?.moves ?? [])].join('\n') + '\n',
  );
  return { ok: problems.length === 0, lines };
}

const names = Object.keys(CHECKS).filter((name) => ONLY.length === 0 || ONLY.includes(name));
const unknown = ONLY.filter((name) => !CHECKS[name]);
if (unknown.length) throw new Error(`--only ${unknown.join(',')}: the checks are ${Object.keys(CHECKS).join(', ')}`);
fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(OUT, { recursive: true });
const runs = names.flatMap((name) => THEMES.map((theme) => [name, theme]));
browser = await launchBrowser();
const results = [];
const began = Date.now();
try {
  for (const [name, theme] of runs) results.push(await run(name, theme));
} finally {
  await browser.close();
}
const ok = results.filter((result) => result.ok).length;
const summary = `${ok} of ${runs.length} authoring checks passed in ${Math.round((Date.now() - began) / 1000)}s. Screenshots: ${OUT}`;
console.log(summary);
fs.writeFileSync(
  path.join(OUT, 'summary.txt'),
  [...results.flatMap((result) => result.lines), summary].join('\n') + '\n',
);
process.exitCode = ok === runs.length ? 0 : 1;
