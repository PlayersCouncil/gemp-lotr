#!/usr/bin/env node
// Runs the visual check against a throwaway Gemp server (local-server/compose.yml): builds the Angular page, starts the
// server on an empty in-memory database, creates the test players with a fresh random password, adds the test data, runs
// run.mjs and takes everything down again. Needs no login from anyone and never touches the development server.
//
//   npm run check:local -- --mutating
//   npm run check:local -- --only start,deck-list --sizes 1366x768
//   npm run check:local -- --keep           leaves the server up afterwards (npm run check:local:down stops it)
//   npm run check:local -- --skip-build     uses the Angular build of the previous run
//   npm run check:selftest                  proves the check catches bugs: see injections.mjs
//   npm run check:legacy-decks              decks saved by the legacy page keep working: see legacy-decks.mjs
//
// Any other option goes to run.mjs. See README.md.
import { spawn, spawnSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { INJECTIONS } from './injections.mjs';
import { playersSql, seedTestData, TEST_PLAYERS, waitForLogin } from './local-server/seed.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const FRONTEND = path.resolve(HERE, '..');
const STATE_DIR = path.join(HERE, '.local-server');
const COMPOSE_FILE = path.join(HERE, 'local-server', 'compose.yml');
const PORT = process.env.GEMP_CHECK_PORT ?? '17101';
const SERVER = `http://127.0.0.1:${PORT}`;

const composeEnv = { ...process.env, CHECK_PORT: PORT, CHECK_STATE_DIR: STATE_DIR };

function run(command, args, options = {}) {
  const result = spawnSync(command, args, { stdio: 'inherit', ...options });
  if (result.status !== 0) {
    throw new Error(`${command} ${args[0]} failed (exit ${result.status})`);
  }
}

function compose(...args) {
  run('docker', ['compose', '-f', COMPOSE_FILE, ...args], { env: composeEnv, stdio: ['ignore', 'ignore', 'inherit'] });
}

function stopServer() {
  // A first run has no state folder yet, and compose needs the variables either way
  fs.mkdirSync(STATE_DIR, { recursive: true });
  compose('down', '--volumes', '--remove-orphans');
}

function buildAngular() {
  console.log('Building the Angular page...');
  run('npm', ['run', 'build', '--', '--output-path', path.join(STATE_DIR, 'ng')], {
    cwd: FRONTEND,
    stdio: ['ignore', 'ignore', 'inherit'],
  });
}

function runCheck(args, user, password, script = 'run.mjs') {
  return new Promise((resolve) => {
    const child = spawn(process.execPath, [path.join(HERE, script), '--server', SERVER, ...args], {
      cwd: HERE,
      stdio: 'inherit',
      // The compose variables too, for a script that reaches the database (legacy-decks.mjs)
      env: { ...composeEnv, GEMP_USER: user, GEMP_PASSWORD: password },
    });
    child.on('exit', (code) => resolve(code ?? 1));
  });
}

/**
 * Each deliberate bug must be reported in the way it should be, and that report must not be there without the bug.
 * Returns the number of bugs that went unnoticed.
 */
function runSelfTest(user, password) {
  const check = (extra) =>
    spawnSync(
      process.execPath,
      [path.join(HERE, 'run.mjs'), '--server', SERVER, '--sizes', '1366x768', '--out', 'report-selftest', ...extra],
      {
        cwd: HERE,
        encoding: 'utf8',
        maxBuffer: 64 * 1024 * 1024,
        env: { ...process.env, GEMP_USER: user, GEMP_PASSWORD: password },
      },
    ).stdout;
  const scenarios = [...new Set(Object.values(INJECTIONS).map((injection) => injection.scenario))];
  console.log(`Without any bug (${scenarios.length} scenarios)...`);
  const clean = check(['--only', scenarios.join(',')]);
  const lineOf = (output, scenario) => output.split('\n').find((line) => line.includes(` ${scenario}-1366x768`)) ?? '';

  let missed = 0;
  for (const [name, injection] of Object.entries(INJECTIONS)) {
    const line = lineOf(check(['--only', injection.scenario, '--inject', name]), injection.scenario);
    const before = lineOf(clean, injection.scenario);
    const caught = injection.expect.test(line) && !injection.expect.test(before);
    if (!caught) {
      missed++;
    }
    console.log(
      `${caught ? 'caught' : 'MISSED'}  ${name}: ${injection.what}${caught ? '' : `\n        reported: ${line.slice(0, 500)}\n        without it: ${before.slice(0, 300)}`}`,
    );
  }
  console.log(
    `\n${Object.keys(INJECTIONS).length - missed} of ${Object.keys(INJECTIONS).length} deliberate bugs caught.`,
  );
  return missed;
}

function serverLogs() {
  spawnSync('docker', ['compose', '-f', COMPOSE_FILE, 'logs', '--tail', '40', 'app'], {
    env: composeEnv,
    stdio: 'inherit',
  });
}

async function main() {
  const args = process.argv.slice(2);
  if (args[0] === 'down') {
    stopServer();
    return 0;
  }
  const keep = args.includes('--keep');
  const skipBuild = args.includes('--skip-build');
  const selfTest = args.includes('--selftest');
  const legacyDecks = args.includes('--legacy-decks');
  const checkArgs = args.filter((arg) => !['--keep', '--skip-build', '--selftest', '--legacy-decks'].includes(arg));
  // Apart from the report of a check against another server
  if (!legacyDecks && !checkArgs.some((arg) => arg === '--out' || arg.startsWith('--out='))) {
    checkArgs.push('--out', 'report-local');
  }

  fs.mkdirSync(STATE_DIR, { recursive: true });
  if (!skipBuild || !fs.existsSync(path.join(STATE_DIR, 'ng', 'browser', 'index.html'))) {
    buildAngular();
  }
  stopServer();
  const password = randomBytes(24).toString('hex');
  fs.writeFileSync(path.join(STATE_DIR, 'init.sql'), playersSql(password));

  try {
    console.log(`Starting the test server on ${SERVER}...`);
    compose('up', '--detach');
    const [player] = TEST_PLAYERS;
    await waitForLogin(SERVER, player.name, password);
    console.log('Adding the test data...');
    await seedTestData(SERVER, password);
    if (selfTest) {
      return runSelfTest(player.name, password) === 0 ? 0 : 1;
    }
    if (legacyDecks) {
      return await runCheck(checkArgs, player.name, password, 'legacy-decks.mjs');
    }
    return await runCheck(checkArgs, player.name, password);
  } catch (error) {
    console.error(error.message);
    serverLogs();
    return 2;
  } finally {
    if (!keep) {
      console.log('Stopping the test server...');
      stopServer();
    }
  }
}

process.exitCode = await main();
