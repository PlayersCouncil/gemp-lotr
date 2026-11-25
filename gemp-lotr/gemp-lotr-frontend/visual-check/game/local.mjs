#!/usr/bin/env node
// Runs the game check against a throwaway Gemp server of its own (../local-server/compose.yml under the project name
// gemp-game-check, port 17201 or GEMP_GAME_CHECK_PORT): an empty in-memory database, the game check's players with a
// fresh random password that is never written down, the test deck saved for each of them, and every game seeded the
// same (-Dgemp.game.randomSeed), so the same moves give the same game. Then it runs game/run.mjs and takes it all down.
// The development server and the deck builder's check server are never touched.
//
//   npm run check:game                         every scenario, the legacy page against itself (see README.md)
//   npm run check:game -- --only start --sizes 1366x768
//   npm run check:game -- --keep               leaves the server up, its login in .local-server/game/login.json
//   npm run check:game:down                    stops a kept server
//   npm run check:game:selftest                proves the check catches bugs: see game/injections.mjs
//
// Any other option goes to game/run.mjs. The server is CHECK_WEB_JAR, or the jar of npm run check:server-build (built
// from this checkout without touching the development jar), or gemp-lotr-async/target/web.jar. It must have GameRandom
// (the seed), or the two games differ.
import { spawn, spawnSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { playersSql, session, waitForLogin } from '../local-server/seed.mjs';
import { DECKS, deckContents, GAME_PLAYERS } from './players.mjs';
import { BUILT_JAR } from '../tools/build-server.mjs';
import { GAME_INJECTIONS } from './injections.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const STATE_DIR = path.join(CHECK_DIR, '.local-server', 'game');
const COMPOSE_FILE = path.join(CHECK_DIR, 'local-server', 'compose.yml');
const PORT = process.env.GEMP_GAME_CHECK_PORT ?? '17201';
const SERVER = `http://127.0.0.1:${PORT}`;
const ANGULAR_BUILD = path.join(CHECK_DIR, '.local-server', 'ng', 'browser');

// The seed needs a server with GameRandom: the jar npm run check:server-build made, unless CHECK_WEB_JAR names one
const composeEnv = {
  ...process.env,
  ...(!process.env.CHECK_WEB_JAR && fs.existsSync(BUILT_JAR) ? { CHECK_WEB_JAR: BUILT_JAR } : {}),
  CHECK_PORT: PORT,
  CHECK_STATE_DIR: STATE_DIR,
  COMPOSE_PROJECT_NAME: 'gemp-game-check',
};

function compose(...args) {
  const result = spawnSync('docker', ['compose', '-f', COMPOSE_FILE, '-p', 'gemp-game-check', ...args], {
    env: composeEnv,
    stdio: ['ignore', 'ignore', 'inherit'],
  });
  if (result.status !== 0) {
    throw new Error(`docker compose ${args[0]} failed (exit ${result.status})`);
  }
}

function stopServer() {
  fs.mkdirSync(path.join(STATE_DIR, 'ng', 'browser'), { recursive: true });
  fs.rmSync(path.join(STATE_DIR, 'login.json'), { force: true });
  compose('down', '--volumes', '--remove-orphans');
}

/** The Angular build the deck builder's check made last, if any; the game check doesn't need one for the legacy page. */
function useAngularBuild() {
  const target = path.join(STATE_DIR, 'ng', 'browser');
  fs.rmSync(target, { recursive: true, force: true });
  fs.mkdirSync(target, { recursive: true });
  if (fs.existsSync(path.join(ANGULAR_BUILD, 'index.html'))) {
    fs.cpSync(ANGULAR_BUILD, target, { recursive: true });
  }
}

/** Saves the test deck for every player, one login at a time (see "One login for all pages" in ../README.md). */
async function seedDecks(password) {
  for (const { name } of GAME_PLAYERS) {
    const player = await session(SERVER, name, password);
    for (const deck of Object.values(DECKS)) {
      await player.post('/deck', {
        deckName: deck.name,
        targetFormat: deck.format,
        notes: '',
        deckContents: deckContents(deck),
      });
    }
  }
}

function runScript(script, args, password) {
  return new Promise((resolve) => {
    const child = spawn(process.execPath, [path.join(HERE, script), '--server', SERVER, ...args], {
      cwd: CHECK_DIR,
      stdio: 'inherit',
      env: { ...process.env, GEMP_GAME_PASSWORD: password },
    });
    child.on('exit', (code) => resolve(code ?? 1));
  });
}

/**
 * Each deliberate bug must be reported in the way it should be, and that report must not be there without the bug.
 * Returns the number of bugs that went unnoticed.
 */
function runSelfTest(password, extra) {
  const check = (args) =>
    spawnSync(
      process.execPath,
      [
        path.join(HERE, 'run.mjs'),
        '--server',
        SERVER,
        '--sizes',
        '1366x768',
        '--out',
        'report-game-selftest',
        ...extra,
        ...args,
      ],
      {
        cwd: CHECK_DIR,
        encoding: 'utf8',
        maxBuffer: 64 * 1024 * 1024,
        env: { ...process.env, GEMP_GAME_PASSWORD: password },
      },
    ).stdout;
  const scenarios = [...new Set(Object.values(GAME_INJECTIONS).map((injection) => injection.scenario))];
  console.log(`Without any bug (${scenarios.length} scenarios)...`);
  const clean = check(['--only', scenarios.join(',')]);
  const lineOf = (output, injection) =>
    output.split('\n').find((line) => line.includes(` ${injection.scenario}-1366x768`)) ?? '';

  let missed = 0;
  for (const [name, injection] of Object.entries(GAME_INJECTIONS)) {
    const line = lineOf(check(['--only', injection.scenario, '--inject', name]), injection);
    const before = lineOf(clean, injection);
    const caught = injection.expect.test(line) && !injection.expect.test(before);
    if (!caught) {
      missed++;
    }
    console.log(
      `${caught ? 'caught' : 'MISSED'}  ${name}: ${injection.what}${caught ? '' : `\n        reported: ${line.slice(0, 500)}\n        without it: ${before.slice(0, 300)}`}`,
    );
  }
  const total = Object.keys(GAME_INJECTIONS).length;
  console.log(`\n${total - missed} of ${total} deliberate bugs caught.`);
  return missed;
}

async function main() {
  const args = process.argv.slice(2);
  if (args[0] === 'down') {
    stopServer();
    return 0;
  }
  const keep = args.includes('--keep');
  const selfTest = args.includes('--selftest');
  const runArgs = args.filter((arg) => !['--keep', '--selftest'].includes(arg));
  if (!selfTest && !runArgs.some((arg) => arg === '--out' || arg.startsWith('--out='))) {
    runArgs.push('--out', 'report-game');
  }

  stopServer();
  useAngularBuild();
  const password = randomBytes(24).toString('hex');
  fs.writeFileSync(path.join(STATE_DIR, 'init.sql'), playersSql(password, GAME_PLAYERS));
  try {
    console.log(`Starting the game check server on ${SERVER}...`);
    compose('up', '--detach');
    await waitForLogin(SERVER, GAME_PLAYERS[0].name, password);
    console.log('Saving the test decks...');
    await seedDecks(password);
    if (keep) {
      // For trying things on the kept server by hand (the folder is not committed); removed when it stops
      fs.writeFileSync(path.join(STATE_DIR, 'login.json'), JSON.stringify({ server: SERVER, password }));
    }
    if (selfTest) {
      return runSelfTest(password, runArgs) === 0 ? 0 : 1;
    }
    return await runScript('run.mjs', runArgs, password);
  } catch (error) {
    console.error(error.message);
    spawnSync('docker', ['compose', '-f', COMPOSE_FILE, '-p', 'gemp-game-check', 'logs', '--tail', '40', 'app'], {
      env: composeEnv,
      stdio: 'inherit',
    });
    return 2;
  } finally {
    if (!keep) {
      console.log('Stopping the game check server...');
      stopServer();
    }
  }
}

process.exitCode = await main();
