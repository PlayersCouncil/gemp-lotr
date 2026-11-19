#!/usr/bin/env node
// Everything that proves the deck builder, the game screen and their deployment, in order, on throwaway copies and
// containers:
//
//   npm run check:all                 (about an hour)
//   npm run check:all -- --quick      without the control-and-gesture matrices, the phone and the game scenarios
//   npm run check:all -- --browser firefox
//                                     the parts that use a browser, in Firefox (or webkit); the reports of
//                                     the Chromium run are replaced
//   npm run check:all -- --failed     again only what failed in the last run (with the same --quick and --browser):
//                                     the parts that passed are left out, and a part with a report runs only its
//                                     failed scenarios (tools/failed-checks.mjs); the Angular app is built first.
//                                     It doesn't build the server again: after a Java change, the whole run.
//                                     Its narrowed reports hold only what ran again.
//
// Each part runs even if an earlier one failed; the summary at the end says which did. What each part did is kept in
// .local-server/check-all-last[-<browser>].json for --failed.
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { BUILT_JAR } from './build-server.mjs';
import { rerunArgs } from './failed-checks.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const VISUAL_CHECK = path.resolve(HERE, '..');
const FRONTEND = path.resolve(VISUAL_CHECK, '..');
const quick = process.argv.includes('--quick');
const browserAt = process.argv.indexOf('--browser');
const browser = browserAt === -1 ? null : process.argv[browserAt + 1];
const failedOnly = process.argv.includes('--failed');
const STATE = path.join(VISUAL_CHECK, '.local-server', `check-all-last${browser ? `-${browser}` : ''}.json`);

const npm = (cwd, ...args) => ({ cwd, args });
/** A part whose report (its --out folder) says which of its checks failed, so --failed can run only those. */
const reported = (part, report) => ({ ...part, report });
const PARTS = [
  // In jsdom, not in a browser
  ...(browser
    ? []
    : [
        ['Frontend: lint, types, formatting, unit tests', npm(FRONTEND, 'run', 'verify')],
        [
          'The legacy page and the Angular page side by side, 200 generated sessions and 200 generated games',
          npm(FRONTEND, 'run', 'test:parity'),
        ],
      ]),
  ['Server: build and web tests', npm(VISUAL_CHECK, 'run', 'check:server-build')],
  ['The check catches deliberate bugs', npm(VISUAL_CHECK, 'run', 'check:selftest')],
  ['Decks saved by the legacy page keep working', npm(VISUAL_CHECK, 'run', 'check:legacy-decks')],
  [
    'Comparison with the legacy page, every scenario',
    reported(npm(VISUAL_CHECK, 'run', 'check:local', '--', '--mutating'), 'report-local'),
  ],
  [
    'The modern theme laid out as the legacy page on a computer',
    reported(
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
      'report-local-modern',
    ),
  ],
  ...(quick
    ? []
    : [['Every control against every gesture', reported(npm(VISUAL_CHECK, 'run', 'check:matrix'), 'report-matrix')]]),
  // Chromium's touch input holds and moves a finger
  ...(quick || browser
    ? []
    : [['The modern theme on a phone', reported(npm(VISUAL_CHECK, 'run', 'check:phone'), 'report-phone')]]),
  ['Deploying with git pull', npm(VISUAL_CHECK, 'run', 'check:deploy')],
  // The game screen (game/README.md): seeded games played on two pages
  // The Angular game page's table against game.html on the recorded games, without a server (the app as built in the web root)
  [
    'Game screen: the table as the legacy page draws it',
    reported(
      npm(VISUAL_CHECK, 'run', 'check:game:table', ...(quick ? ['--', '--sizes', '1366x768'] : [])),
      'report-game-table',
    ),
  ],
  // Games made up from a seed, their last state drawn on both pages (src/testing/game/generated-game.ts)
  [
    'Game screen: the table of 100 generated games',
    reported(
      npm(VISUAL_CHECK, 'run', 'check:game:generated', ...(quick ? ['--', '--sizes', '1366x768'] : [])),
      'report-game-generated',
    ),
  ],
  ['Game screen: the check catches deliberate bugs', npm(VISUAL_CHECK, 'run', 'check:game:selftest')],
  ...(quick
    ? []
    : [['Game screen: every scenario on both pages', reported(npm(VISUAL_CHECK, 'run', 'check:game'), 'report-game')]]),
  ...(quick
    ? []
    : [
        [
          'Game screen: every control against every gesture',
          reported(npm(VISUAL_CHECK, 'run', 'check:game:matrix'), 'report-game-matrix'),
        ],
      ]),
  // The modern theme: the same games, laid out as the legacy page (where every card and panel is, within 2px)
  ...(quick
    ? []
    : [
        [
          'Game screen: the modern theme laid out as the legacy page',
          reported(npm(VISUAL_CHECK, 'run', 'check:game:modern'), 'report-game-modern'),
        ],
      ]),
  // The classic theme as game.html on every device: the recorded games' screens, zooming (game/devices.mjs), and every
  // scenario played with a finger on tablets and phones, upright and sideways (Chromium's touch input)
  ...(browser
    ? []
    : [
        [
          'Game screen: classic as game.html on a computer, tablets and phones',
          reported(npm(VISUAL_CHECK, 'run', 'check:game:devices'), 'report-game-devices'),
        ],
        ...(quick
          ? []
          : [
              [
                'Game screen: every scenario with a finger on tablets and phones',
                reported(npm(VISUAL_CHECK, 'run', 'check:game:phones'), 'report-game-phones'),
              ],
            ]),
      ]),
  // The phone layout (G7): back from card details, reloads, touch; Chromium's touch input holds and moves a finger
  ...(browser
    ? []
    : [
        [
          'Game screen: the phone layout',
          reported(
            npm(VISUAL_CHECK, 'run', 'check:game:phone', ...(quick ? ['--', '--devices', 'phone'] : [])),
            'report-game-phone',
          ),
        ],
      ]),
];

/** `args` with more arguments for the script npm runs (after its `--`). */
const withArgs = (args, more) => (args.includes('--') ? [...args, ...more] : [...args, '--', ...more]);

const run = (name, cwd, args) => {
  console.log(`\n=== ${name}${args.includes('--only') || args.includes('--checks') ? ' (only what failed)' : ''}`);
  const startedAt = Date.now();
  const { status } = spawnSync('npm', args, {
    cwd,
    stdio: 'inherit',
    env: { ...process.env, CHECK_WEB_JAR: BUILT_JAR, ...(browser ? { CHECK_BROWSER: browser } : {}) },
  });
  return { name, ok: status === 0, startedAt, minutes: Math.round((Date.now() - startedAt) / 60000) };
};

const earlier = fs.existsSync(STATE) ? JSON.parse(fs.readFileSync(STATE, 'utf8')).parts : {};
const again = failedOnly ? PARTS.filter(([name]) => !earlier[name]?.ok) : PARTS;
if (failedOnly) {
  if (again.length === 0) console.log('Nothing failed in the last run.');
  // The fixes since: in the web root (game/table.mjs) and for the checks' throwaway server (game/local.mjs)
  else {
    for (const outputPath of [[], ['--', '--output-path', 'visual-check/.local-server/ng']]) {
      const built = run('Build the Angular app', FRONTEND, ['run', 'build', ...outputPath]);
      if (!built.ok) process.exit(1);
    }
  }
}

const results = PARTS.map(([name, { cwd, args, report }]) => {
  if (!again.some(([other]) => other === name)) return { ...earlier[name], name, skipped: true };
  const narrowed = failedOnly && report && earlier[name] ? rerunArgs(report, earlier[name].startedAt) : null;
  return run(name, cwd, narrowed ? withArgs(args, narrowed) : args);
});

fs.mkdirSync(path.dirname(STATE), { recursive: true });
fs.writeFileSync(
  STATE,
  JSON.stringify(
    { parts: Object.fromEntries(results.map(({ skipped, ...result }) => [result.name, result])) },
    null,
    1,
  ),
);

console.log(`\n=== Summary${browser ? ` (${browser})` : ''}${failedOnly ? ' (passed before: not run again)' : ''}`);
for (const { name, ok, minutes, skipped } of results) {
  console.log(`${ok ? 'pass' : 'FAIL'}  ${name} (${skipped ? 'earlier run' : `${minutes} min`})`);
}
process.exitCode = results.every(({ ok }) => ok) ? 0 : 1;
