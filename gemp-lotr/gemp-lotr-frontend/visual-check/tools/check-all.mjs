#!/usr/bin/env node
// Everything that proves the deck builder and its deployment, in order, on throwaway copies and containers:
//
//   npm run check:all                 (about an hour)
//   npm run check:all -- --quick      without the control-and-gesture matrix
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

const npm = (cwd, ...args) => ({ cwd, args });
const PARTS = [
  ['Frontend: lint, types, formatting, unit tests', npm(FRONTEND, 'run', 'verify')],
  ['The legacy page and the Angular page side by side, 200 generated sessions', npm(FRONTEND, 'run', 'test:parity')],
  ['Server: build and web tests', npm(VISUAL_CHECK, 'run', 'check:server-build')],
  ['The check catches deliberate bugs', npm(VISUAL_CHECK, 'run', 'check:selftest')],
  ['Decks saved by the legacy page keep working', npm(VISUAL_CHECK, 'run', 'check:legacy-decks')],
  ['Comparison with the legacy page, every scenario', npm(VISUAL_CHECK, 'run', 'check:local', '--', '--mutating')],
  ...(quick ? [] : [['Every control against every gesture', npm(VISUAL_CHECK, 'run', 'check:matrix')]]),
  ['Deploying with git pull', npm(VISUAL_CHECK, 'run', 'check:deploy')],
];

const results = PARTS.map(([name, { cwd, args }]) => {
  console.log(`\n=== ${name}`);
  const started = Date.now();
  const { status } = spawnSync('npm', args, {
    cwd,
    stdio: 'inherit',
    env: { ...process.env, CHECK_WEB_JAR: BUILT_JAR },
  });
  return { name, ok: status === 0, minutes: Math.round((Date.now() - started) / 60000) };
});

console.log('\n=== Summary');
for (const { name, ok, minutes } of results) {
  console.log(`${ok ? 'pass' : 'FAIL'}  ${name} (${minutes} min)`);
}
process.exitCode = results.every(({ ok }) => ok) ? 0 : 1;
