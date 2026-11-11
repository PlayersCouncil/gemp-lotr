#!/usr/bin/env node
// Everything that proves the deck builder and its deployment, in order, on throwaway copies and containers:
//
//   npm run check:all                 (about an hour)
//   npm run check:all -- --quick      without the control-and-gesture matrix
//   npm run check:all -- --browser firefox
//                                     the parts that use a browser, in Firefox (or webkit); the reports of
//                                     the Chromium run are replaced
//
// Each part runs even if an earlier one failed; the summary at the end says which did.
import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { BUILT_JAR } from './build-server.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const VISUAL_CHECK = path.resolve(HERE, '..');
const FRONTEND = path.resolve(VISUAL_CHECK, '..');
const quick = process.argv.includes('--quick');
const browserAt = process.argv.indexOf('--browser');
const browser = browserAt === -1 ? null : process.argv[browserAt + 1];

const npm = (cwd, ...args) => ({ cwd, args });
const PARTS = [
  // In jsdom, not in a browser
  ...(browser
    ? []
    : [
        ['Frontend: lint, types, formatting, unit tests', npm(FRONTEND, 'run', 'verify')],
        [
          'The legacy page and the Angular page side by side, 200 generated sessions',
          npm(FRONTEND, 'run', 'test:parity'),
        ],
      ]),
  ['Server: build and web tests', npm(VISUAL_CHECK, 'run', 'check:server-build')],
  ['The check catches deliberate bugs', npm(VISUAL_CHECK, 'run', 'check:selftest')],
  ['Decks saved by the legacy page keep working', npm(VISUAL_CHECK, 'run', 'check:legacy-decks')],
  ['Comparison with the legacy page, every scenario', npm(VISUAL_CHECK, 'run', 'check:local', '--', '--mutating')],
  [
    'The modern theme laid out as the legacy page on a computer',
    npm(
      VISUAL_CHECK,
      'run',
      'check:local',
      '--',
      '--mutating',
      '--theme=modern',
      '--geometry',
      '--out',
      'report-local-modern',
    ),
  ],
  ...(quick ? [] : [['Every control against every gesture', npm(VISUAL_CHECK, 'run', 'check:matrix')]]),
  ['Deploying with git pull', npm(VISUAL_CHECK, 'run', 'check:deploy')],
];

const results = PARTS.map(([name, { cwd, args }]) => {
  console.log(`\n=== ${name}`);
  const started = Date.now();
  const { status } = spawnSync('npm', args, {
    cwd,
    stdio: 'inherit',
    env: { ...process.env, CHECK_WEB_JAR: BUILT_JAR, ...(browser ? { CHECK_BROWSER: browser } : {}) },
  });
  return { name, ok: status === 0, minutes: Math.round((Date.now() - started) / 60000) };
});

console.log(`\n=== Summary${browser ? ` (${browser})` : ''}`);
for (const { name, ok, minutes } of results) {
  console.log(`${ok ? 'pass' : 'FAIL'}  ${name} (${minutes} min)`);
}
process.exitCode = results.every(({ ok }) => ok) ? 0 : 1;
