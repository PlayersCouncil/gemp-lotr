#!/usr/bin/env node
// Puts the throwaway local server (local-server.mjs) on the internet for a while, to try the deck builder from a phone
// or another computer: a Cloudflare quick tunnel (an https://<random>.trycloudflare.com address, no Cloudflare account)
// to a password gate on this machine, which passes requests on to the server. Nothing listens beyond 127.0.0.1.
//
//   npm run tunnel                 starts the local server first if none is kept running (check:local -- --keep)
//
// Prints the address, the gate's user and password (new each time, HTTP basic authentication: the browser asks once),
// and the test player's Gemp login. Ctrl+C closes the tunnel; `npm run check:local:down` then stops the server.
// The test player is an administrator of a server whose database lives in memory: everything is gone when it stops.
import { spawn, spawnSync } from 'node:child_process';
import crypto from 'node:crypto';
import fs from 'node:fs';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const LOGIN_FILE = path.resolve(HERE, '../.local-server/login.json');

/** The kept server's address and test login; starts one (with a quick check) when none is running. */
async function keptServer() {
  const running = async () => {
    if (!fs.existsSync(LOGIN_FILE)) return null;
    const login = JSON.parse(fs.readFileSync(LOGIN_FILE, 'utf8'));
    try {
      await fetch(login.server, { signal: AbortSignal.timeout(5000) });
      return login;
    } catch {
      return null;
    }
  };
  const login = await running();
  if (login) return login;
  console.log('Starting the local server (npm run check:local -- --keep)...');
  spawnSync('node', ['local-server.mjs', '--keep', '--only', 'start', '--sizes', '1366x768'], {
    cwd: path.resolve(HERE, '..'),
    stdio: 'inherit',
  });
  const started = await running();
  if (!started) throw new Error('The local server did not start: see the output above');
  return started;
}

/** A gate on 127.0.0.1 that lets through only requests with the user and password, passed on to `target`. */
function startGate(target, user, password) {
  const expected = Buffer.from(`Basic ${Buffer.from(`${user}:${password}`).toString('base64')}`);
  const allowed = (header) => {
    const given = Buffer.from(header ?? '');
    return given.length === expected.length && crypto.timingSafeEqual(given, expected);
  };
  const to = new URL(target);
  const gate = http.createServer((request, response) => {
    if (!allowed(request.headers.authorization)) {
      response.writeHead(401, { 'WWW-Authenticate': 'Basic realm="Gemp test server", charset="UTF-8"' });
      response.end('A password is needed.');
      return;
    }
    const headers = { ...request.headers, host: to.host };
    delete headers.authorization;
    const upstream = http.request(
      { host: to.hostname, port: to.port, method: request.method, path: request.url, headers },
      (answer) => {
        response.writeHead(answer.statusCode ?? 502, answer.headers);
        answer.pipe(response);
      },
    );
    upstream.on('error', () => {
      if (!response.headersSent) response.writeHead(502);
      response.end('The local server does not answer.');
    });
    request.pipe(upstream);
  });
  return new Promise((resolve) => gate.listen(0, '127.0.0.1', () => resolve(gate)));
}

/** A Cloudflare quick tunnel to the gate; resolves with its public address. */
function startTunnel(port) {
  const tunnel = spawn('cloudflared', ['tunnel', '--no-autoupdate', '--url', `http://127.0.0.1:${port}`], {
    stdio: ['ignore', 'ignore', 'pipe'],
  });
  const address = new Promise((resolve, reject) => {
    let log = '';
    tunnel.stderr.on('data', (chunk) => {
      log += chunk;
      const found = /https:\/\/[a-z0-9-]+\.trycloudflare\.com/.exec(log);
      if (found) resolve(found[0]);
    });
    tunnel.on('exit', (code) => reject(new Error(`cloudflared stopped (exit ${code}):\n${log.slice(-2000)}`)));
    tunnel.on('error', (error) => reject(new Error(`cloudflared could not start: ${error.message}`)));
  });
  return { tunnel, address };
}

const login = await keptServer();
const user = 'gemp';
const password = crypto.randomBytes(18).toString('base64url');
const gate = await startGate(login.server, user, password);
const { tunnel, address } = startTunnel(gate.address().port);
const url = await address;
const stop = () => {
  tunnel.kill();
  gate.close();
  process.exit(0);
};
process.on('SIGINT', stop);
process.on('SIGTERM', stop);
tunnel.on('exit', () => {
  console.log('The tunnel closed.');
  gate.close();
  process.exit(1);
});

console.log(`
The deck builder:  ${url}/gemp-lotr/ng/deckbuilder/
The legacy one:    ${url}/gemp-lotr/deckBuild.html
The game hall:     ${url}/gemp-lotr/hall.html
Gate (the browser asks first):  user ${user}, password ${password}
Gemp login:        ${login.user} / ${login.password}

Ctrl+C closes the tunnel; npm run check:local:down stops the server.`);
