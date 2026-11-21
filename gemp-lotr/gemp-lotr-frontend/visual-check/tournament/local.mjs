#!/usr/bin/env node
// Runs the tournament check (run.mjs) against a throwaway Gemp server of its own (compose project
// gemp-tournament-check, port 17301 or GEMP_TOURNAMENT_CHECK_PORT): an empty in-memory database, the check's players
// with a fresh random password, and the game check's deck saved for those who need one. Then takes it all down.
//
//   npm run check:tournament
//   npm run check:tournament -- --keep     leaves the server up, its login in .local-server/tournament/login.json
//   npm run check:tournament:down          stops a kept server
//
// The check waits for a scheduled tournament's start time and for a round's pairing delay: about four minutes.
import { spawn } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { DECKS, deckContents } from '../game/players.mjs';
import { session } from '../local-server/seed.mjs';
import { throwawayServer } from '../local-server/throwaway.mjs';
import { DECK_PLAYERS, TOURNAMENT_PLAYERS } from './players.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const check = throwawayServer({
  project: 'gemp-tournament-check',
  port: process.env.GEMP_TOURNAMENT_CHECK_PORT ?? '17301',
  stateDir: path.join(CHECK_DIR, '.local-server', 'tournament'),
});

async function seedDecks(password) {
  for (const name of DECK_PLAYERS) {
    const player = await session(check.server, name, password);
    await player.post('/deck', {
      deckName: DECKS.main.name,
      targetFormat: DECKS.main.format,
      notes: '',
      deckContents: deckContents(DECKS.main),
    });
  }
}

function runCheck(args, password) {
  return new Promise((resolve) => {
    const child = spawn(process.execPath, [path.join(HERE, 'run.mjs'), '--server', check.server, ...args], {
      cwd: CHECK_DIR,
      stdio: 'inherit',
      env: { ...process.env, GEMP_TOURNAMENT_PASSWORD: password },
    });
    child.on('exit', (code) => resolve(code ?? 1));
  });
}

async function main() {
  const args = process.argv.slice(2);
  if (args[0] === 'down') {
    check.stop();
    return 0;
  }
  const keep = args.includes('--keep');
  const runArgs = args.filter((arg) => arg !== '--keep');
  const password = randomBytes(24).toString('hex');
  try {
    console.log(`Starting the tournament check server on ${check.server}...`);
    await check.start(TOURNAMENT_PLAYERS, password);
    console.log('Saving the test decks...');
    await seedDecks(password);
    if (keep) {
      check.writeLogin(password);
    }
    const code = await runCheck(runArgs, password);
    if (code !== 0) {
      check.showLog(120);
    }
    return code;
  } catch (error) {
    console.error(error.message);
    check.showLog();
    return 2;
  } finally {
    if (!keep) {
      console.log('Stopping the tournament check server...');
      check.stop();
    }
  }
}

process.exitCode = await main();
