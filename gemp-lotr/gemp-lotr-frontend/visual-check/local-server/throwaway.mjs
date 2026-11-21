// A throwaway Gemp server for a check (compose.yml): its own compose project, port and in-memory database, with the
// players a check names and a password that is only ever in memory. Shared by the checks that start one; the deck
// builder's and the game's (deckbuilder/local-server.mjs, game/local.mjs) were written before this and still start
// theirs themselves.
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { BUILT_JAR } from '../tools/build-server.mjs';
import { playersSql, waitForLogin } from './seed.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const COMPOSE_FILE = path.join(HERE, 'compose.yml');

/**
 * `project` is the compose project name, `port` the local port, `stateDir` where its state is kept (under
 * .local-server, which is not committed). The server is CHECK_WEB_JAR, or the jar of npm run check:server-build, or
 * gemp-lotr-async/target/web.jar.
 */
export function throwawayServer({ project, port, stateDir }) {
  const server = `http://127.0.0.1:${port}`;
  const env = {
    ...process.env,
    ...(!process.env.CHECK_WEB_JAR && fs.existsSync(BUILT_JAR) ? { CHECK_WEB_JAR: BUILT_JAR } : {}),
    CHECK_PORT: String(port),
    CHECK_STATE_DIR: stateDir,
    COMPOSE_PROJECT_NAME: project,
  };
  const compose = (args, stdio = ['ignore', 'ignore', 'inherit']) => {
    const result = spawnSync('docker', ['compose', '-f', COMPOSE_FILE, '-p', project, ...args], { env, stdio });
    if (result.status !== 0) {
      throw new Error(`docker compose ${args[0]} failed (exit ${result.status})`);
    }
  };
  const stop = () => {
    // compose needs the folders it mounts and the variables even to take the project down
    fs.mkdirSync(path.join(stateDir, 'ng', 'browser'), { recursive: true });
    fs.rmSync(path.join(stateDir, 'login.json'), { force: true });
    compose(['down', '--volumes', '--remove-orphans']);
  };
  return {
    server,
    stop,
    /** Starts it on an empty database holding `players` ([{name, type}]), and waits until the first can log in. */
    async start(players, password) {
      stop();
      fs.writeFileSync(path.join(stateDir, 'init.sql'), playersSql(password, players));
      compose(['up', '--detach']);
      await waitForLogin(server, players[0].name, password);
    },
    /** For trying things on a kept server by hand; removed when it stops. */
    writeLogin(password) {
      fs.writeFileSync(path.join(stateDir, 'login.json'), JSON.stringify({ server, password }));
    },
    /** The end of the server's log, to the console. */
    showLog(lines = 60) {
      spawnSync('docker', ['compose', '-f', COMPOSE_FILE, '-p', project, 'logs', '--tail', String(lines), 'app'], {
        env,
        stdio: 'inherit',
      });
    },
  };
}
