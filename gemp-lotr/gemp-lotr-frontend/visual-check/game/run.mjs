#!/usr/bin/env node
// The game check: plays each scenario of game/scenarios.mjs as a real game on two pages, one after the other, and
// compares them after every step: the screen, what the page sent the server, its browser dialogs and the game state it
// shows. The server seeds every game the same (GameRandom), the opponent answers by a fixed policy over HTTP
// (protocol.mjs), and the player in the browser makes the same gestures on both pages, so both pages see the same game.
//
// Until the Angular game page exists (G2 of trymarz/gemp-lotr-private#27) both pages are the legacy game.html: that
// proves the games are the same and the check is stable (and `--inject` proves it catches bugs, see injections.mjs).
//
//   node game/local.mjs                     a throwaway server, then this (npm run check:game)
//   node game/run.mjs --server http://127.0.0.1:17201 --only start   with GEMP_GAME_PASSWORD set
import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { compareScreens } from '../compare.mjs';
import { placeholder } from '../stub-server.mjs';
import { actionMenuPoint, animating, ANGULAR_CONTRACT, cardPoint, LEGACY, readGameState } from './controls.mjs';
import { GAME_INJECTIONS } from './injections.mjs';
import { MAX_WORKERS, pairOf } from './players.mjs';
import { endGame, foeAnswer, Foe, parseGameXml, readDecision, startGame } from './protocol.mjs';
import { GAME_SCENARIOS } from './scenarios.mjs';

const { values: options } = parseArgs({
  options: {
    server: { type: 'string', default: process.env.GEMP_SERVER ?? 'http://127.0.0.1:17201' },
    // The two pages compared: legacy (game.html) or angular (the Angular game page)
    pages: { type: 'string', default: 'legacy,legacy' },
    legacy: { type: 'string', default: '/gemp-lotr/game.html' },
    angular: { type: 'string', default: '/gemp-lotr/ng/game' },
    sizes: { type: 'string', default: '1280x800,1366x768,1920x1080' },
    only: { type: 'string' },
    workers: { type: 'string', default: String(MAX_WORKERS) },
    tolerance: { type: 'string', default: '60' },
    out: { type: 'string', default: 'report-game' },
    inject: { type: 'string' },
    // Real card pictures from the image server instead of placeholders
    'real-images': { type: 'boolean', default: false },
    // Prints every decision the player gets and every answer sent
    verbose: { type: 'boolean', default: false },
  },
});

const password = process.env.GEMP_GAME_PASSWORD;
if (!password) {
  console.error('GEMP_GAME_PASSWORD is not set: run it through game/local.mjs (npm run check:game)');
  process.exit(2);
}
const [pageA, pageB] = options.pages.split(',');
const sizes = options.sizes.split(',').map((label) => {
  const [width, height] = label.split('x').map(Number);
  return { label, width, height };
});
const only = options.only?.split(',');
const scenarios = GAME_SCENARIOS.filter((scenario) => !only || only.includes(scenario.name));
if (only && scenarios.length !== only.length) {
  console.error(`Unknown scenario in --only: ${only.filter((name) => !GAME_SCENARIOS.some((s) => s.name === name))}`);
  process.exit(2);
}
const injection = options.inject ? GAME_INJECTIONS[options.inject] : null;
if (options.inject && !injection) {
  console.error(`Unknown injection: ${options.inject}`);
  process.exit(2);
}
const tolerance = Number(options.tolerance);
const outDir = path.resolve(options.out);
fs.rmSync(outDir, { recursive: true, force: true });
fs.mkdirSync(outDir, { recursive: true });

const browser = await launchBrowser();
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** The cookies a player's settings are kept in, the same on both pages: the fastest animations, everything else default. */
function settingsCookies(server) {
  const { hostname } = new URL(server);
  return [{ name: 'animation-speed', value: '4', domain: hostname, path: '/' }];
}

/**
 * Follows what the page and the server say to each other: every game answer (for the decision waiting for the
 * player), and every request that matters for the comparison (decisions answered, card information, concede,
 * cancel, chat messages). The long poll's own requests don't count: when they are sent depends on timing.
 */
function watchTraffic(page, participant, gameId) {
  // Decision ids are reused, so a decision is known by its place among the events: `answeredUpTo` is the place of the
  // last one the page answered
  const traffic = { participant, events: [], answeredUpTo: -1, requests: [], ended: false, pending: 0 };
  const lastDecisionIndex = () =>
    traffic.events.findLastIndex((event) => event.type === 'D' && event.participantId === participant);
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (!url.pathname.startsWith('/gemp-lotr-server/')) return;
    traffic.pending++;
    const body = new URLSearchParams(request.postData() ?? '');
    const where = url.pathname
      .replace('/gemp-lotr-server', '')
      .replace(`/game/${gameId}`, '/game/<id>')
      .replace(`/chat/Game${gameId}`, '/chat/Game<id>');
    if (body.has('decisionId')) {
      if (options.verbose)
        console.log(`  ${participant} answers ${body.get('decisionId')} = ${body.get('decisionValue')}`);
      traffic.answeredUpTo = lastDecisionIndex();
      traffic.requests.push(`decision ${body.get('decisionId')} = ${body.get('decisionValue')}`);
    } else if (where.startsWith('/game/<id>/') || (where.startsWith('/chat/') && body.has('message'))) {
      const query = new URLSearchParams(url.search);
      query.delete('participantId');
      query.delete('_');
      body.delete('participantId');
      traffic.requests.push(`${request.method()} ${where} ${query} ${body}`.trim());
    }
  });
  const done = async (response) => {
    traffic.pending = Math.max(0, traffic.pending - 1);
    const url = new URL(response.url());
    if (!url.pathname.startsWith(`/gemp-lotr-server/game/${gameId}`) || url.pathname.endsWith('/cardInfo')) return;
    const text = await response.text().catch(() => '');
    const { events } = parseGameXml(text);
    traffic.events.push(...events);
    if (options.verbose) {
      for (const event of events.filter((e) => e.type === 'D' && e.participantId === participant)) {
        const actions = event.params.filter((param) => param.name === 'actionText').map((param) => param.value);
        console.log(
          `  ${participant} is asked ${event.id}: ${event.decisionType} ${event.text}${actions.length ? ` [${actions.join(' | ')}]` : ''}`,
        );
      }
    }
    if (events.some((event) => event.type === 'EG')) traffic.ended = true;
  };
  page.on('response', (response) => done(response));
  page.on('requestfailed', () => (traffic.pending = Math.max(0, traffic.pending - 1)));
  /** The decision waiting for the player that it hasn't answered yet, or null. */
  traffic.decision = () => {
    const index = lastDecisionIndex();
    if (index < 0 || index <= traffic.answeredUpTo || traffic.ended) return null;
    return { ...readDecision(traffic.events[index]), index };
  };
  traffic.answered = (decision) => traffic.answeredUpTo >= decision.index;
  return traffic;
}

/** Waits until nothing moves: no request but the long poll, no animation, no change to the page for `quiet` ms. */
async function settle(page, kind, traffic, foe, quiet = 500, max = 20000) {
  const start = Date.now();
  await foe.idle();
  await page.evaluate(() => {
    window.__changed = Date.now();
    new MutationObserver(() => (window.__changed = Date.now())).observe(document.body, {
      subtree: true,
      childList: true,
      attributes: true,
      characterData: true,
    });
  });
  while (Date.now() - start < max) {
    const [moving, changed] = await page.evaluate(
      ([animatingSource, kind]) => [new Function(`return (${animatingSource})`)()({ kind }), window.__changed],
      [animating.toString(), kind],
    );
    // The long poll is always open: one request may be outstanding
    if (!moving && traffic.pending <= 1 && Date.now() - changed >= quiet && !foe.busy) break;
    await sleep(100);
  }
  await page
    .waitForFunction(() => Array.from(document.images).every((img) => img.complete), null, { timeout: 10000 })
    .catch(() => undefined);
}

/** Runs a function of controls.mjs in the page. */
function inPage(page, fn, arg) {
  return page.evaluate(([source, arg]) => new Function(`return (${source})`)()(arg), [fn.toString(), arg]);
}

/** The gestures a step is made of, on one kind of page. */
function gestures(page, kind) {
  const select = kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT;
  const locator = (selector) => page.locator(selector).filter({ visible: true }).last();
  /** Where a card can be reached; a decision's dialog may still be opening, so it waits a moment for the card. */
  const pointOf = async (id) => {
    let point = null;
    for (let attempt = 0; attempt < 50 && !point?.x; attempt++) {
      point = await inPage(page, cardPoint, { kind, id });
      if (!point?.x) await sleep(100);
    }
    return point ?? {};
  };
  return {
    pointOf,
    async clickCard(id, button = 'left') {
      // A decision's dialog may still be opening
      const point = await pointOf(id);
      if (!point.x)
        throw new Error(`card ${id} can't be clicked${point.blocked ? `: ${point.blocked} lies over it` : ''}`);
      await page.mouse.click(point.x, point.y, { button });
    },
    async clickButton(label) {
      await locator(kind === 'legacy' ? select.decisionButton(label) : ANGULAR_CONTRACT.decisionButton(label)).click();
    },
    hasButton: (label) =>
      locator(kind === 'legacy' ? select.decisionButton(label) : ANGULAR_CONTRACT.decisionButton(label))
        .count()
        .then((n) => n > 0),
    async integer(value) {
      if (kind === 'legacy') {
        // The legacy field is a spinner (jquery.spinnercontrol): its arrows change the number shown
        const shown = Number(await locator('.SpinnerControl .ValueDisplay').textContent());
        const arrow = locator(value > shown ? '.SpinnerControl .RightButton' : '.SpinnerControl .LeftButton');
        for (let i = 0; i < Math.abs(value - shown); i++) await arrow.click();
      } else {
        await locator(select.integerInput).fill(String(value));
      }
      await this.clickButton('OK');
    },
    async choice(decision, index) {
      const choices = await locator(select.choiceSelect).count();
      if (choices > 0) {
        await locator(select.choiceSelect).selectOption(String(index));
        await this.clickButton('OK');
      } else if (kind === 'legacy') {
        // Two answers or fewer: a button each, in order, in the small dialog
        await page.locator('.ui-dialog:visible .ui-dialog-content button').nth(index).click();
      } else {
        await locator(ANGULAR_CONTRACT.choiceButton(index)).click();
      }
    },
    async actionMenu(actionId) {
      const point = await inPage(page, actionMenuPoint, { kind, actionId });
      if (point) await page.mouse.click(point.x, point.y);
      return Boolean(point);
    },
    async tab(name) {
      await locator(select.tab(name)).click();
    },
    async pile(name, playerIndex) {
      await locator(select.pile(name, playerIndex)).click();
    },
    async statsToggle(playerIndex) {
      await locator(select.statsToggle(playerIndex)).click();
    },
    async setting(name) {
      await locator(select.setting(name)).click();
    },
    /** A button of the box at the bottom left (concede, cancel), by its legacy id. */
    async button(name) {
      await locator(kind === 'legacy' ? `#${name}` : `[data-button="${name}"]`).click();
    },
  };
}

/** The answer the player gives when a step says `auto`: the same policy as the opponent's, made with gestures. */
async function answerByPolicy(act, decision) {
  const value = foeAnswer(decision);
  switch (decision.type) {
    case 'INTEGER':
      return act.integer(value);
    case 'MULTIPLE_CHOICE':
      return act.choice(decision, Number(value));
    case 'ACTION_CHOICE':
      return pickCards(act, decision, [0]);
    default:
      return pickCards(act, decision, value ? value.split(',').map((id) => decision.values.cardId.indexOf(id)) : []);
  }
}

/** Clicks the decision's cards by their place in its list, then Done if the decision is still waiting. */
async function pickCards(act, decision, indexes, traffic) {
  for (const index of indexes) {
    await act.clickCard(decision.values.cardId[index]);
    await sleep(150);
  }
  // With auto-accept (on by default) the last card answers the decision itself
  await sleep(300);
  if (traffic && traffic.answered(decision)) return;
  if (indexes.length === 0 && decision.type === 'CARD_ACTION_CHOICE') return act.clickButton('Pass');
  if (await act.hasButton('Done')) await act.clickButton('Done');
}

/** One step of a scenario on a page: what it does, by the decision waiting for the player. */
async function doStep(page, kind, step, traffic) {
  const act = gestures(page, kind);
  const decision = traffic.decision();
  const needs = (what) => {
    if (!decision) throw new Error(`step ${JSON.stringify(step)}: no decision is waiting for ${what}`);
    return decision;
  };
  if ('integer' in step) {
    needs('a number');
    return act.integer(step.integer);
  }
  if ('choice' in step) {
    const d = needs('a choice');
    const index = d.values.results.findIndex((result) => result.replace(/<[^>]*>/g, '') === step.choice);
    if (index < 0) throw new Error(`no choice "${step.choice}" in ${JSON.stringify(d.values.results)}`);
    return act.choice(d, index);
  }
  if ('pick' in step) return pickCards(act, needs('cards'), step.pick, traffic);
  if (step.pass) return pickCards(act, needs('a pass'), [], traffic);
  if (step.done) return act.clickButton('Done');
  if ('assign' in step) {
    // A minion, then the character it goes to (both by their place in the decision's lists)
    const d = needs('an assignment');
    const [minion, character] = step.assign;
    await act.clickCard(d.values.minions[minion]);
    await sleep(250);
    return act.clickCard(d.values.freeCharacters[character]);
  }
  if ('play' in step) {
    const d = needs('an action');
    const index = d.values.actionText.findIndex((text) => step.play.test(text));
    if (index < 0) throw new Error(`no action ${step.play} in ${JSON.stringify(d.values.actionText)}`);
    const cardId = d.values.cardId[index];
    await act.clickCard(cardId);
    await sleep(300);
    // A card with several actions opens a menu of them
    if (!traffic.answered(d)) await act.actionMenu(d.values.actionId[index]);
    return;
  }
  if ('auto' in step) {
    for (let count = 0; count < step.auto; count++) {
      const d = traffic.decision();
      if (!d || (step.until && step.until.test(d.text))) return;
      await answerByPolicy(act, d);
      await waitForNextDecision(traffic, d);
    }
    return;
  }
  if ('cardInfo' in step) return act.clickCard(cardIdOf(traffic, step.cardInfo), 'right');
  if ('tab' in step) return act.tab(step.tab);
  if ('pile' in step) return act.pile(step.pile, step.player ?? 0);
  if ('stats' in step) return act.statsToggle(step.stats);
  if ('setting' in step) return act.setting(step.setting);
  if ('key' in step) return page.keyboard.press(step.key);
  if ('click' in step) return page.mouse.click(5, 5);
  if ('button' in step) return act.button(step.button);
  if ('hover' in step) {
    const point = await act.pointOf(cardIdOf(traffic, step.hover));
    if (!point.x) {
      throw new Error(
        `card ${JSON.stringify(step.hover)} can't be hovered${point.blocked ? `: ${point.blocked} lies over it` : ''}`,
      );
    }
    await page.mouse.move(point.x, point.y);
    return sleep(1200);
  }
  throw new Error(`unknown step ${JSON.stringify(step)}`);
}

/**
 * A card named by a step: its id, or `{ blueprint, nth, owner, zone }` among the cards put into play so far. `owner`
 * is 'player' (the default: both players play the same deck), 'foe' or 'any'; `zone` limits it to cards put into that
 * zone (e.g. ADVENTURE_PATH for a site).
 */
function cardIdOf(traffic, which) {
  if (typeof which === 'string') return which;
  const owner = which.owner ?? 'player';
  const owned = (event) => owner === 'any' || (owner === 'foe') === (event.participantId !== traffic.participant);
  const ids = traffic.events
    .filter(
      (event) =>
        event.type === 'PCIP' &&
        event.blueprintId === which.blueprint &&
        owned(event) &&
        (!which.zone || event.zone === which.zone),
    )
    .map((event) => event.cardId);
  const id = [...new Set(ids)][which.nth ?? 0];
  if (!id) throw new Error(`no card ${JSON.stringify(which)} in play`);
  return id;
}

async function waitForNextDecision(traffic, answered, max = 30000) {
  const start = Date.now();
  while (Date.now() - start < max) {
    const next = traffic.decision();
    if (traffic.ended || (next && next.index > answered.index)) return;
    await sleep(100);
  }
  throw new Error(`no decision came after "${answered.text}"`);
}

/** Plays a scenario on one kind of page: a snapshot after the page loads and after each step that isn't `quiet`. */
async function play(kind, scenario, size, pair, label) {
  let game;
  try {
    game = await startGame(options.server, pair, password);
  } catch (error) {
    return { snapshots: [], errors: [], problems: [`${label}: ${error.message}`] };
  }
  const context = await browser.newContext({ viewport: { width: size.width, height: size.height } });
  const page = await context.newPage();
  const errors = [];
  const dialogs = [];
  const snapshots = [];
  let foe;
  try {
    const cookie = game.player.cookie.split('; ').map((pairText) => {
      const [name, ...value] = pairText.split('=');
      return { name, value: value.join('='), domain: new URL(options.server).hostname, path: '/' };
    });
    await context.addCookies([...cookie, ...settingsCookies(options.server)]);
    if (!options['real-images']) {
      await context.route(/\.(jpe?g|png|gif)(\?.*)?$/i, (route) => {
        const url = new URL(route.request().url());
        // The page's own pictures (icons, buttons) are served as they are; card pictures from other servers aren't
        if (url.origin === new URL(options.server).origin) return route.continue();
        return route.fulfill({ contentType: 'image/svg+xml', body: placeholder(url) });
      });
    }
    // The game's chat room is named after the game ("Welcome to room: Game12"), and the two pages play two games:
    // both are shown the same name
    await context.route(/\/gemp-lotr-server\/chat\//, async (route) => {
      try {
        const response = await route.fetch();
        const body = (await response.text()).replaceAll(`Game${game.gameId}`, 'Game#');
        await route.fulfill({ response, body });
      } catch {
        // The page closed while the chat's long poll was open
        await route.abort().catch(() => undefined);
      }
    });
    // The legacy chat marks a message naming the player only once it knows the player (GET /player), and the game's
    // first answer can come before that: the check lets the player's information arrive first, on both pages
    let playerKnown;
    const known = new Promise((resolve) => (playerKnown = resolve));
    setTimeout(() => playerKnown(), 10000);
    await context.route(/\/gemp-lotr-server\/player(\?|$)/, async (route) => {
      try {
        const response = await route.fetch();
        await route.fulfill({ response });
      } finally {
        playerKnown();
      }
    });
    await context.route(new RegExp(`/gemp-lotr-server/game/${game.gameId}(\\?|$)`), async (route) => {
      if (route.request().method() === 'GET') await known;
      await route.continue().catch(() => undefined);
    });
    page.on('pageerror', (error) => errors.push(error.message));
    page.on('dialog', async (dialog) => {
      dialogs.push(`${dialog.type()}: ${dialog.message()}`);
      await dialog.accept();
    });
    const traffic = watchTraffic(page, pair.player, game.gameId);
    foe = new Foe(game, pair.foe, scenario.foe ?? [], () => traffic.decision() === null);
    if (options.verbose) foe.log = console.log;
    if (label === 'B' && injection?.apply) await injection.apply(page, kind);

    const pagePath = kind === 'legacy' ? options.legacy : options.angular;
    await page.goto(`${options.server}${pagePath}?gameId=${encodeURIComponent(game.gameId)}`);
    const masks = (kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT).masks;
    // The mouse leaves the page before each snapshot (no hover state), except after a step that rests it on something
    const snapshot = async (name, keepMouse = false) => {
      if (!keepMouse) await page.mouse.move(1, 1);
      await settle(page, kind, traffic, foe);
      if (label === 'B' && injection?.afterSettle) await injection.afterSettle(page, kind);
      snapshots.push({
        name,
        screen: await page.screenshot({ mask: masks.map((selector) => page.locator(selector)), maskColor: '#ff00ff' }),
        state: await inPage(page, readGameState, { kind }),
        requests: traffic.requests.splice(0),
        dialogs: dialogs.splice(0),
      });
    };
    await waitForFirst(traffic);
    await snapshot('start');
    for (const [index, step] of scenario.steps.entries()) {
      const before = traffic.decision();
      await doStep(page, kind, step, traffic);
      if (before && !('auto' in step) && isAnswer(step)) await waitForNextDecision(traffic, before);
      if (!step.quiet) await snapshot(step.name ?? `step ${index + 1}`, 'hover' in step);
    }
    if (scenario.steps.at(-1)?.quiet) await snapshot('end');
    return { snapshots, errors, problems: foe.problems };
  } catch (error) {
    return { snapshots, errors, problems: [...(foe?.problems ?? []), `${label}: ${error.message.split('\n')[0]}`] };
  } finally {
    foe?.stop();
    await endGame(game);
    await context.unrouteAll({ behavior: 'ignoreErrors' });
    await context.close();
  }
}

const isAnswer = (step) => ['integer', 'choice', 'pick', 'pass', 'done', 'play'].some((key) => key in step);

async function waitForFirst(traffic, max = 20000) {
  const start = Date.now();
  while (Date.now() - start < max && traffic.events.length === 0) await sleep(100);
}

function compareRuns(a, b, base) {
  const problems = [...a.problems, ...b.problems];
  if (a.errors.length || b.errors.length) {
    const same = JSON.stringify(a.errors) === JSON.stringify(b.errors);
    if (!same) problems.push(`page errors: A ${JSON.stringify(a.errors)}, B ${JSON.stringify(b.errors)}`);
  }
  if (a.snapshots.length !== b.snapshots.length) {
    problems.push(`steps: A ${a.snapshots.length}, B ${b.snapshots.length}`);
  }
  let pixels = 0;
  const images = [];
  for (let i = 0; i < Math.min(a.snapshots.length, b.snapshots.length); i++) {
    const sa = a.snapshots[i];
    const sb = b.snapshots[i];
    const at = `${sa.name}`;
    const screens = compareScreens(sa.screen, sb.screen, [], false);
    const show = (value) => JSON.stringify(value).slice(0, 300);
    for (const key of Object.keys({ ...sa.state, ...sb.state })) {
      if (JSON.stringify(sa.state[key]) !== JSON.stringify(sb.state[key])) {
        problems.push(`${at}: ${key}: A ${show(sa.state[key])}, B ${show(sb.state[key])}`);
      }
    }
    if (JSON.stringify(sa.requests) !== JSON.stringify(sb.requests)) {
      problems.push(`${at}: requests: A ${show(sa.requests)}, B ${show(sb.requests)}`);
    }
    if (JSON.stringify(sa.dialogs) !== JSON.stringify(sb.dialogs)) {
      problems.push(`${at}: dialogs: A ${show(sa.dialogs)}, B ${show(sb.dialogs)}`);
    }
    pixels = Math.max(pixels, screens.outside);
    const file = `${base}-${String(i).padStart(2, '0')}`;
    fs.writeFileSync(path.join(outDir, `${file}-A.png`), sa.screen);
    fs.writeFileSync(path.join(outDir, `${file}-B.png`), sb.screen);
    if (screens.outside > tolerance) {
      problems.push(
        `${at}: screen: ${screens.outside === Infinity ? 'different sizes' : `${screens.outside} px differ`}`,
      );
      const diff = compareScreens(sa.screen, sb.screen, [], true).image;
      if (diff) fs.writeFileSync(path.join(outDir, `${file}-diff.png`), diff);
    }
    images.push({ name: sa.name, file, differs: screens.outside > tolerance, pixels: screens.outside });
  }
  return { problems, pixels, images };
}

async function check(scenario, size, worker) {
  const pair = pairOf(worker);
  const a = await play(pageA, scenario, size, pair, 'A');
  const b = await play(pageB, scenario, size, pair, 'B');
  const name = `${scenario.name}-${size.label}`;
  const result = { scenario, size, ...compareRuns(a, b, name) };
  console.log(
    `${result.problems.length ? 'DIFFERS' : 'same   '} ${name}${result.problems.length ? `: ${result.problems.join(' | ').slice(0, 2000)}` : ''}`,
  );
  return result;
}

async function runAll(jobs, workers) {
  const results = [];
  let next = 0;
  await Promise.all(
    Array.from({ length: Math.min(workers, jobs.length) }, async (_, worker) => {
      while (next < jobs.length) {
        const job = jobs[next++];
        results.push(await job(worker));
      }
    }),
  );
  return results;
}

const escape = (text) =>
  String(text).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);

function writeReport(results) {
  fs.writeFileSync(
    path.join(outDir, 'results.json'),
    JSON.stringify({
      checks: results.map((r) => ({ name: `${r.scenario.name}-${r.size.label}`, problems: r.problems })),
    }),
  );
  const rows = results
    .map(
      (r) => `<tr class="${r.problems.length ? 'bad' : ''}">
  <td><b>${escape(r.scenario.name)}</b><br>${escape(r.size.label)}<br><small>${escape(r.scenario.note ?? '')}</small></td>
  <td>${r.problems.length ? `<b class="bad">differs</b><br>${r.problems.map(escape).join('<br>')}` : 'same'}
  <br><small>${r.images.length} steps, at most ${r.pixels} px differ</small></td>
  <td>${r.images
    .map(
      (img) =>
        `<div class="${img.differs ? 'bad' : ''}">${escape(img.name)}: <a href="${img.file}-A.png">A</a> <a href="${img.file}-B.png">B</a>${img.differs ? ` <a href="${img.file}-diff.png">differences</a> (${img.pixels} px)` : ''}</div>`,
    )
    .join('')}</td>
  <td>${r.images.length ? `<a href="${r.images.at(-1).file}-A.png"><img src="${r.images.at(-1).file}-A.png"></a>` : ''}</td>
</tr>`,
    )
    .join('\n');
  fs.writeFileSync(
    path.join(outDir, 'index.html'),
    `<!doctype html><meta charset="utf-8"><title>Game screen check</title>
<style>body{font:14px sans-serif;margin:16px}table{border-collapse:collapse}td{border:1px solid #ccc;padding:6px;vertical-align:top}
img{width:320px}tr.bad{background:#fee}.bad{color:#b00}</style>
<h1>Game screen check</h1>
<p>${escape(options.server)}: A = <code>${escape(pageA)}</code>, B = <code>${escape(pageB)}</code>${injection ? `, injected into B: <code>${escape(options.inject)}</code>` : ''};
${results.length} checks, <b>${results.filter((r) => r.problems.length).length} differ</b> (tolerance ${tolerance} px).
Clocks and chat times are masked in magenta.</p>
<table><tr><th>Scenario</th><th>Result</th><th>Steps</th><th>Last step (A)</th></tr>
${rows}</table>`,
  );
}

try {
  const jobs = scenarios.flatMap((scenario) => sizes.map((size) => (worker) => check(scenario, size, worker)));
  const results = await runAll(jobs, Math.min(Number(options.workers), MAX_WORKERS));
  results.sort(
    (a, b) =>
      scenarios.indexOf(a.scenario) - scenarios.indexOf(b.scenario) || sizes.indexOf(a.size) - sizes.indexOf(b.size),
  );
  writeReport(results);
  const failed = results.filter((r) => r.problems.length).length;
  console.log(`\n${results.length} checks, ${failed} differ. Report: ${path.join(outDir, 'index.html')}`);
  process.exitCode = failed ? 1 : 0;
} finally {
  await browser.close();
}
