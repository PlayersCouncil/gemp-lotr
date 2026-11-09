#!/usr/bin/env node
// Tests the way a frontend change reaches a live server (docker/frontend-watch.sh): with the Java server running, a real
// `git pull` into the checkout must make the `frontend` container rebuild the app and swap it in, without a restart and
// without a moment in which players can't load the page. Everything happens on a throwaway copy of the repository, with
// its own containers, so the development server and its files are never touched.
//
//   npm run check:deploy
//   npm run check:deploy -- --keep      leaves the copy and the containers up afterwards, to look around
import { spawnSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import fs from 'node:fs';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromium } from 'playwright';
import { playersSql, TEST_PLAYERS, waitForLogin } from './local-server/seed.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const REPOSITORY = path.resolve(HERE, '../../..');
const STATE_DIR = path.join(HERE, '.local-server', 'deploy');
const COMPOSE_FILE = path.join(HERE, 'local-server', 'deploy-compose.yml');
const ORIGIN = path.join(STATE_DIR, 'origin.git');
const CHECKOUT = path.join(STATE_DIR, 'checkout');
const DEVELOPER = path.join(STATE_DIR, 'developer');
const PORT = process.env.GEMP_DEPLOY_PORT ?? '17102';
const SERVER = `http://127.0.0.1:${PORT}`;
const APP_BASE = `${SERVER}/gemp-lotr/ng/`;
const PAGE = `${APP_BASE}deckbuilder/`;

const ROUTES_FILE = 'gemp-lotr/gemp-lotr-frontend/src/ui/app/app.routes.ts';
const PAGE_TITLE = 'GEMP: LotR-TCG - Deckbuilder';
const LOCK_FILE = 'gemp-lotr/gemp-lotr-frontend/package-lock.json';
const LEGACY_CARD_DATA = 'gemp-lotr/gemp-lotr-async/src/main/web/js/gemp-022/cards/PackImages.js';

const MINUTE = 60 * 1000;
const INSTALL_TIMEOUT = 8 * MINUTE;
const BUILD_TIMEOUT = 4 * MINUTE;
/** Longer than the watcher's poll, twice over, and its wait for a pull to finish. */
const NOTHING_HAPPENS_MS = 20 * 1000;

const composeEnv = { ...process.env, CHECK_PORT: PORT, CHECK_STATE_DIR: STATE_DIR, CHECK_CHECKOUT: CHECKOUT };

function run(command, args, options = {}) {
  const result = spawnSync(command, args, { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, ...options });
  if (result.status !== 0) {
    throw new Error(`${command} ${args.join(' ')} failed:\n${result.stderr ?? ''}`);
  }
  return result.stdout ?? '';
}

const compose = (...args) => run('docker', ['compose', '-f', COMPOSE_FILE, ...args], { env: composeEnv });
const git = (cwd, ...args) =>
  run('git', ['-c', 'user.name=Deploy test', '-c', 'user.email=deploy-test@example.invalid', ...args], { cwd });

/** The frontend container leaves files owned by root behind, so the copy is removed from inside a container too. */
function removeState() {
  if (!fs.existsSync(STATE_DIR)) {
    return;
  }
  run('docker', [
    'run',
    '--rm',
    '-v',
    `${path.dirname(STATE_DIR)}:/parent`,
    'node:24-alpine',
    'rm',
    '-rf',
    '/parent/deploy',
  ]);
}

function stopEverything() {
  fs.mkdirSync(path.join(STATE_DIR, 'checkout', 'gemp-lotr', 'gemp-lotr-async', 'src', 'main', 'web'), {
    recursive: true,
  });
  compose('down', '--volumes', '--remove-orphans');
  removeState();
}

/**
 * A repository of its own, made from the files of this one as they are now, new ones not yet committed included (card
 * images left out), with a remote to pull from.
 */
function createRepositories() {
  fs.mkdirSync(STATE_DIR, { recursive: true });
  const seed = path.join(STATE_DIR, 'seed');
  fs.mkdirSync(seed);
  const listed = run(
    'git',
    [
      'ls-files',
      '-z',
      '--cached',
      '--others',
      '--exclude-standard',
      '--',
      '.gitignore',
      'gemp-lotr/docker/frontend-watch.sh',
      'gemp-lotr/gemp-lotr-frontend',
      'gemp-lotr/gemp-lotr-async/src/main/web',
      ':!gemp-lotr/gemp-lotr-async/src/main/web/images',
    ],
    { cwd: REPOSITORY },
  );
  // A tracked file deleted but not yet committed is still listed
  const files = String(listed)
    .split('\0')
    .filter((file) => file && fs.existsSync(path.join(REPOSITORY, file)))
    .join('\0');
  run('tar', ['--null', '-T', '-', '-cf', path.join(STATE_DIR, 'seed.tar')], { cwd: REPOSITORY, input: files });
  run('tar', ['-xf', path.join(STATE_DIR, 'seed.tar'), '-C', seed]);
  fs.rmSync(path.join(STATE_DIR, 'seed.tar'));
  git(seed, 'init', '--quiet', '--initial-branch=main');
  git(seed, 'add', '-A');
  git(seed, 'commit', '--quiet', '-m', 'The starting point');
  git(STATE_DIR, 'clone', '--quiet', '--bare', seed, ORIGIN);
  git(STATE_DIR, 'clone', '--quiet', ORIGIN, CHECKOUT);
  git(STATE_DIR, 'clone', '--quiet', ORIGIN, DEVELOPER);
  fs.rmSync(seed, { recursive: true });
}

/** A developer pushes a change; the server's checkout doesn't have it until pull(). */
function pushChange(message, change) {
  change(DEVELOPER);
  git(DEVELOPER, 'add', '-A');
  git(DEVELOPER, 'commit', '--quiet', '-m', message);
  git(DEVELOPER, 'push', '--quiet', 'origin', 'HEAD');
}

const pull = () => git(CHECKOUT, 'pull', '--quiet', '--ff-only');

function editFile(root, file, edit) {
  const full = path.join(root, file);
  fs.writeFileSync(full, edit(fs.readFileSync(full, 'utf8')));
}

/** The app's page title, which is compiled into its JavaScript: a change only shows if the new code is served. */
function setTitle(root, title) {
  editFile(root, ROUTES_FILE, (text) => text.replace(/title: '[^']*'/, `title: '${title}'`));
}

const watcherLog = () => compose('logs', '--no-log-prefix', '--no-color', 'frontend');
const countInLog = (text) => watcherLog().split(text).length - 1;

async function waitFor(what, condition, timeout) {
  const deadline = Date.now() + timeout;
  while (Date.now() < deadline) {
    if (await condition()) {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
  throw new Error(
    `Timed out waiting for ${what}. The watcher's last lines:\n${watcherLog().split('\n').slice(-25).join('\n')}`,
  );
}

const waitForLogCount = (text, count, timeout = BUILD_TIMEOUT) =>
  waitFor(`"${text}" (${count} times)`, () => countInLog(text) >= count, timeout);

/** A GET over the given connections: `false` for a new connection each time. */
function get(url, agent) {
  return new Promise((resolve, reject) => {
    http
      .get(url, { agent }, (response) => {
        const chunks = [];
        response.on('data', (chunk) => chunks.push(chunk));
        response.on('end', () =>
          resolve({ status: response.statusCode, body: Buffer.concat(chunks).toString('utf8') }),
        );
      })
      .on('error', reject);
  });
}

/** The loads still running: a step that fails never reaches its own stop, and they would run for ever. */
const activeLoads = new Set();

/**
 * Loads the page and the script it names as fast as it can, as players do during a deploy, and records every failure
 * and the order in which builds were served. With `keepConnections`, over connections that stay open, as a browser's
 * do; otherwise a new connection each time, as a player arriving later.
 */
function startLoading({ keepConnections }) {
  const agent = keepConnections ? new http.Agent({ keepAlive: true, maxSockets: 6 }) : false;
  const failures = [];
  const builds = [];
  let requests = 0;
  let running = true;
  const fail = (what) => failures.push(what);
  const loader = async () => {
    while (running) {
      try {
        const page = await get(PAGE, agent);
        requests++;
        if (page.status !== 200) {
          fail(`page: HTTP ${page.status}`);
          continue;
        }
        const main = page.body.match(/src="(main-[^"]+\.js)"/)?.[1];
        if (!main) {
          fail('page: names no script');
          continue;
        }
        const script = await get(`${APP_BASE}${main}`, agent);
        requests++;
        if (script.status !== 200) {
          fail(`${main}: HTTP ${script.status}`);
        } else if (builds.at(-1) !== main) {
          builds.push(main);
        }
      } catch (error) {
        requests++;
        fail(`connection: ${error.code ?? error.message}`);
      }
    }
  };
  const loaders = Array.from({ length: 6 }, loader);
  const stop = async () => {
    activeLoads.delete(stop);
    running = false;
    await Promise.all(loaders);
    if (agent) {
      agent.destroy();
    }
    return { failures, builds, requests };
  };
  activeLoads.add(stop);
  return stop;
}

function expect(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function expectNoGaps({ failures, builds, requests }, expectedBuilds) {
  const counts = {};
  for (const failure of failures) {
    counts[failure] = (counts[failure] ?? 0) + 1;
  }
  expect(failures.length === 0, `${failures.length} of ${requests} loads failed: ${JSON.stringify(counts)}`);
  // Loads under way at the swap can finish in any order, so old and new may alternate for a moment
  const served = new Set(builds);
  expect(served.size === expectedBuilds, `Expected ${expectedBuilds} build(s), served: ${[...served].join(', ')}`);
  return `${requests} loads, none failed`;
}

async function main() {
  const keep = process.argv.includes('--keep');
  const password = randomBytes(24).toString('hex');
  const [player] = TEST_PLAYERS;
  const results = [];
  const failed = [];
  let browser;

  const step = async (name, test) => {
    const started = Date.now();
    const took = () => `[${Math.round((Date.now() - started) / 1000)}s]`;
    try {
      const detail = await test();
      results.push(name);
      console.log(`ok   ${name}${detail ? ` (${detail})` : ''} ${took()}`);
    } catch (error) {
      // The later steps don't depend on this one having passed, and it matters what else holds
      failed.push(name);
      console.log(`FAIL ${name}: ${error.message} ${took()}`);
    }
  };

  try {
    stopEverything();
    fs.mkdirSync(STATE_DIR, { recursive: true });
    createRepositories();
    fs.writeFileSync(path.join(STATE_DIR, 'init.sql'), playersSql(password));
    console.log(`Starting the live server on ${SERVER}...`);
    compose('up', '--detach', 'db', 'app');
    await waitForLogin(SERVER, player.name, password);

    browser = await chromium.launch();
    const context = await browser.newContext({ viewport: { width: 1366, height: 768 } });
    const login = await context.request.post(`${SERVER}/gemp-lotr-server/login`, {
      form: { login: player.name, password },
    });
    expect(login.ok(), `Login failed: HTTP ${login.status()}`);

    const session = await context.storageState();

    /** What a player opening the page now gets: it must really work, not only load. */
    const openPage = async (browserContext = context) => {
      const page = await browserContext.newPage();
      const errors = [];
      page.on('pageerror', (error) => errors.push(error.message));
      await page.goto(PAGE);
      await page.waitForSelector('button.gd-card', { timeout: 30000 });
      return { page, errors };
    };
    /** A new browser has no connections open yet. */
    const servedTitle = async () => {
      const newBrowser = await browser.newContext({ viewport: { width: 1366, height: 768 }, storageState: session });
      const { page, errors } = await openPage(newBrowser);
      const title = await page.title();
      await newBrowser.close();
      expect(errors.length === 0, `Page errors: ${errors.join('; ')}`);
      return title;
    };
    const expectServing = async (title) => {
      const served = await servedTitle();
      expect(served === title, `Serving "${served}", expected "${title}"`);
    };

    await step('a brand-new container installs, builds and starts serving', async () => {
      expect(
        !fs.existsSync(path.join(CHECKOUT, 'gemp-lotr/gemp-lotr-async/src/main/web/ng')),
        'A build is already there',
      );
      compose('up', '--detach', 'frontend');
      await waitForLogCount('Now serving the new build', 1, INSTALL_TIMEOUT);
      expect(countInLog('Installing dependencies (npm ci)') === 1, 'npm ci did not run once');
      await expectServing(PAGE_TITLE);
    });

    let openTab;
    let stopReturning;
    await step('a pull with a frontend change is rebuilt and served, with no gap and no server restart', async () => {
      openTab = await openPage();
      const serverStarted = run('docker', [
        'inspect',
        '--format',
        '{{.State.StartedAt}}',
        'gemp-visual-check-deploy-app-1',
      ]);
      const builds = countInLog('Now serving the new build');
      const stopLoading = startLoading({ keepConnections: false });
      stopReturning = startLoading({ keepConnections: true });
      await new Promise((resolve) => setTimeout(resolve, 1000));
      pushChange('Change the page title', (root) => setTitle(root, 'Deploy test two'));
      pull();
      await waitForLogCount('Now serving the new build', builds + 1);
      await new Promise((resolve) => setTimeout(resolve, 1000));
      const loads = await stopLoading();
      await expectServing('Deploy test two');
      expect(
        run('docker', ['inspect', '--format', '{{.State.StartedAt}}', 'gemp-visual-check-deploy-app-1']) ===
          serverStarted,
        'The server was restarted',
      );
      return expectNoGaps(loads, 2);
    });

    await step('a browser with connections already open during the swap can still load the page', async () => {
      await new Promise((resolve) => setTimeout(resolve, 3000));
      return expectNoGaps(await stopReturning(), 2);
    });

    await step("the previous build's files stay until the next deploy, for pages loaded just before it", async () => {
      const page = await get(PAGE, false);
      const current = page.body.match(/src="(main-[^"]+\.js)"/)?.[1];
      const live = path.join(CHECKOUT, 'gemp-lotr/gemp-lotr-async/src/main/web/ng');
      const scripts = fs.readdirSync(live).filter((file) => file.startsWith('main-'));
      const previous = scripts.filter((file) => file !== current);
      expect(previous.length === 1, `Expected one previous script, found: ${scripts.join(', ')}`);
      const script = await get(`${APP_BASE}${previous[0]}`, false);
      expect(script.status === 200, `The previous script answers HTTP ${script.status}`);
    });

    await step('a tab left open during the swap keeps working', async () => {
      const { page, errors } = openTab;
      const before = await page.title();
      await page.locator('button.gd-card').first().click();
      await page.waitForSelector('.gd-pile .gd-card, .gd-slot .gd-card', { timeout: 10000 });
      await page.locator('button.gd-card').first().click({ button: 'right' });
      await page.waitForSelector('dialog.gd-card-info[open]', { timeout: 10000 });
      expect(errors.length === 0, `Page errors in the open tab: ${errors.join('; ')}`);
      expect(before === PAGE_TITLE, `The open tab changed by itself to "${before}"`);
    });

    await step('reloading that tab gets the new code', async () => {
      const { page } = openTab;
      await page.reload();
      await page.waitForSelector('button.gd-card', { timeout: 30000 });
      const title = await page.title();
      expect(title === 'Deploy test two', `The reload got "${title}"`);
    });

    await step('a pull that changes nothing in the frontend does not rebuild', async () => {
      const builds = countInLog('Building the app');
      pushChange('Change something else', (root) =>
        fs.writeFileSync(path.join(root, 'gemp-lotr/notes.txt'), 'not the frontend\n'),
      );
      pull();
      await new Promise((resolve) => setTimeout(resolve, NOTHING_HAPPENS_MS));
      expect(countInLog('Building the app') === builds, 'It rebuilt');
    });

    await step('a pull that breaks the build leaves the previous version live', async () => {
      const failed = countInLog('Build FAILED, still serving the previous build');
      const stopLoading = startLoading({ keepConnections: false });
      pushChange('Break the build', (root) =>
        editFile(root, ROUTES_FILE, (text) => `${text}\nconst broken: number = 'not a number';\n`),
      );
      pull();
      await waitForLogCount('Build FAILED, still serving the previous build', failed + 1);
      const loads = await stopLoading();
      await expectServing('Deploy test two');
      expect(
        !fs.existsSync(path.join(CHECKOUT, 'gemp-lotr/gemp-lotr-async/src/main/web/.ng-build')),
        'The failed build was left behind',
      );
      return expectNoGaps(loads, 1);
    });

    await step('the next good pull recovers', async () => {
      const builds = countInLog('Now serving the new build');
      pushChange('Fix the build', (root) => {
        editFile(root, ROUTES_FILE, (text) => text.replace("\nconst broken: number = 'not a number';\n", ''));
        setTitle(root, 'Deploy test three');
      });
      pull();
      await waitForLogCount('Now serving the new build', builds + 1);
      await expectServing('Deploy test three');
    });

    await step('a changed package-lock.json reinstalls before building', async () => {
      const installs = countInLog('Installing dependencies (npm ci)');
      const builds = countInLog('Now serving the new build');
      pushChange('Touch the lock file', (root) => editFile(root, LOCK_FILE, (text) => `${text}\n`));
      pull();
      await waitForLogCount('Now serving the new build', builds + 1, INSTALL_TIMEOUT);
      expect(countInLog('Installing dependencies (npm ci)') === installs + 1, 'npm ci did not run');
      await expectServing('Deploy test three');
    });

    await step('a change to the legacy card data rebuilds', async () => {
      const builds = countInLog('Now serving the new build');
      pushChange('Change the card data', (root) =>
        editFile(root, LEGACY_CARD_DATA, (text) => `${text}\n// deploy test\n`),
      );
      pull();
      await waitForLogCount('Now serving the new build', builds + 1);
      await expectServing('Deploy test three');
    });

    await step('a recreated container clears leftovers, rebuilds and keeps the live version up meanwhile', async () => {
      const web = 'gemp-lotr/gemp-lotr-async/src/main/web';
      // As an interrupted build leaves them: the container owns the web folder, so they are made from inside it
      compose('exec', '-T', 'frontend', 'mkdir', '-p', `/src/gemp-lotr-async/src/main/web/.ng-build/leftover`);
      const stopLoading = startLoading({ keepConnections: false });
      compose('up', '--detach', '--force-recreate', 'frontend');
      await waitForLogCount('Now serving the new build', 1);
      const loads = await stopLoading();
      expect(!fs.existsSync(path.join(CHECKOUT, web, '.ng-build')), 'The leftovers are still there');
      await expectServing('Deploy test three');
      return expectNoGaps(loads, 1);
    });

    console.log(`\n${results.length} deploy steps passed, ${failed.length} failed.`);
    return failed.length ? 1 : 0;
  } catch (error) {
    console.error(`\nFAILED after ${results.length} steps: ${error.message}`);
    return 1;
  } finally {
    await Promise.all([...activeLoads].map((stop) => stop()));
    await browser?.close();
    if (!keep) {
      console.log('Stopping and removing the copy...');
      stopEverything();
    }
  }
}

process.exitCode = await main();
