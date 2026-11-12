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
import {
  actionMenuPoint,
  animating,
  ANGULAR_CONTRACT,
  cardPoint,
  LEGACY,
  readGameState,
  readLegacyModel,
} from './controls.mjs';
import { GAME_INJECTIONS } from './injections.mjs';
import { MAX_WORKERS, pairOf } from './players.mjs';
import { session } from '../local-server/seed.mjs';
import { coveredBy, gaps as coverageGaps } from './coverage.mjs';
import { endGame, foeAnswer, Foe, parseGameXml, readDecision, replayIdOf, startGame } from './protocol.mjs';
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
    // Saves what page A and the server said to each other, and the game the page held after each step, as test data
    // for the jsdom tests (src/testing/game-recordings, see game/README.md); page B isn't run
    record: { type: 'boolean', default: false },
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
  const traffic = { participant, events: [], answeredUpTo: -1, requests: [], ended: false, pending: 0, exchanges: [] };
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
    if (options.record && url.pathname.startsWith('/gemp-lotr-server/') && !url.pathname.includes('/chat/')) {
      traffic.exchanges.push(await exchangeOf(response, gameId));
    }
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

/**
 * One request and its answer as the recording keeps it: the address with the game's id as <id>, the parameters
 * without the participant, the channel number and jQuery's cache buster (they depend on the run), and the answer.
 */
async function exchangeOf(response, gameId) {
  const request = response.request();
  const url = new URL(request.url());
  const params = Object.fromEntries(
    [...new URLSearchParams(url.search), ...new URLSearchParams(request.postData() ?? '')]
      .filter(([name]) => !['participantId', 'channelNumber', '_'].includes(name))
      .sort(([a], [b]) => a.localeCompare(b)),
  );
  return {
    method: request.method(),
    path: url.pathname.replace('/gemp-lotr-server', '').replace(`/game/${gameId}`, '/game/<id>'),
    params,
    status: response.status(),
    contentType: response.headers()['content-type'] ?? '',
    body: await response.text().catch(() => ''),
  };
}

/** Waits until nothing moves: no request but the long poll, no animation, no change to the page for `quiet` ms. */
async function settle(page, kind, traffic, bots, quiet = 500, max = 20000) {
  const start = Date.now();
  await idle(bots);
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
    if (!moving && traffic.pending <= 1 && Date.now() - changed >= quiet && !bots.some((bot) => bot.busy)) break;
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
function gestures(page, kind, used) {
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
  const reach = async (id) => {
    const point = await pointOf(id);
    if (!point.x) {
      throw new Error(`card ${id} can't be reached${point.blocked ? `: ${point.blocked} lies over it` : ''}`);
    }
    return point;
  };
  const decisionButton = (label) => locator(select.decisionButton(label));
  return {
    pointOf,
    async clickCard(id, { button = 'left', shift = false } = {}) {
      // As a person would: the legacy page ignores a second tap on the same card within 200 ms (a bounce)
      // (counted from when the last click happened: reaching a card can wait for its dialog)
      const point = await reach(id);
      const last = page.lastCardClick;
      if (last?.id === id) await sleep(Math.max(0, 250 - (Date.now() - last.at)));
      if (shift) await page.keyboard.down('Shift');
      await page.mouse.click(point.x, point.y, { button });
      page.lastCardClick = { id, at: Date.now() };
      if (shift) await page.keyboard.up('Shift');
    },
    /** A card pressed, moved by (dx, dy) in steps, and released: up opens its information, sideways moves it. */
    async dragCard(id, dx, dy) {
      const point = await reach(id);
      await page.mouse.move(point.x, point.y);
      await page.mouse.down();
      for (let i = 1; i <= 10; i++) await page.mouse.move(point.x + (dx * i) / 10, point.y + (dy * i) / 10);
      await page.mouse.up();
    },
    async clickButton(label) {
      used.add(label === 'OK' ? 'OK button' : label);
      await decisionButton(label).click();
    },
    hasButton: (label) =>
      decisionButton(label)
        .count()
        .then((n) => n > 0),
    async integer(value) {
      if (kind === 'legacy') {
        // The legacy field is a spinner (jquery.spinnercontrol): its arrows change the number shown
        const shown = Number(await locator('.SpinnerControl .ValueDisplay').textContent());
        const arrow = locator(value > shown ? '.SpinnerControl .RightButton' : '.SpinnerControl .LeftButton');
        for (let i = 0; i < Math.abs(value - shown); i++) {
          used.add('bid spinner');
          await arrow.click();
        }
      } else {
        await locator(select.integerInput).fill(String(value));
      }
      await this.clickButton('OK');
    },
    async choice(decision, index) {
      const choices = await locator(select.choiceSelect).count();
      if (choices > 0) {
        used.add('choice list');
        await locator(select.choiceSelect).selectOption(String(index));
        await this.clickButton('OK');
      } else if (kind === 'legacy') {
        used.add('choice button');
        // Two answers or fewer: a button each, in order, in the small dialog
        await page.locator('.ui-dialog:visible .ui-dialog-content button').nth(index).click();
      } else {
        used.add('choice button');
        await locator(ANGULAR_CONTRACT.choiceButton(index)).click();
      }
    },
    async actionMenu(actionId) {
      const point = await inPage(page, actionMenuPoint, { kind, actionId });
      if (point) {
        used.add('action menu');
        await page.mouse.click(point.x, point.y);
      }
      return Boolean(point);
    },
    async click(selector, name) {
      used.add(name);
      await locator(selector).click();
    },
  };
}

/** The answer the player gives when a step says `auto`: the same policy as the opponent's, made with gestures. */
async function answerByPolicy(act, decision, traffic, used) {
  const value = foeAnswer(decision);
  switch (decision.type) {
    case 'INTEGER':
      return act.integer(value);
    case 'MULTIPLE_CHOICE':
      return act.choice(decision, Number(value));
    case 'ACTION_CHOICE':
      return pickCards(act, decision, [0], traffic, used);
    case 'ASSIGN_MINIONS':
      // Done with nothing assigned: the browser asks to confirm when there are minions
      return act.clickButton('Done');
    default:
      return pickCards(
        act,
        decision,
        value ? value.split(',').map((id) => decision.values.cardId.indexOf(id)) : [],
        traffic,
        used,
      );
  }
}

/** Clicks the decision's cards by their place in its list, then Done if the decision is still waiting. */
async function pickCards(act, decision, indexes, traffic, used) {
  for (const index of indexes) {
    used.add('card click');
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
async function doStep(page, kind, step, traffic, used) {
  const act = gestures(page, kind, used);
  const select = kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT;
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
  if ('pick' in step) {
    const d = needs('cards');
    if (step.then) {
      // Each card, then the way to finish: a button (Done) or a key (Escape)
      for (const index of step.pick) {
        used.add('card click');
        await act.clickCard(d.values.cardId[index]);
        await sleep(200);
      }
      if (step.then === 'Escape') {
        used.add('Escape');
        return page.keyboard.press('Escape');
      }
      return act.clickButton(step.then);
    }
    return pickCards(act, d, step.pick, traffic, used);
  }
  if (step.pass) return pickCards(act, needs('a pass'), [], traffic, used);
  if (step.done) return act.clickButton('Done');
  if ('press' in step) return act.clickButton(step.press);
  if ('assign' in step) {
    // A minion, then the character it goes to (both by their place in the decision's lists)
    const d = needs('an assignment');
    const [minion, character] = step.assign;
    used.add('assign minion');
    await act.clickCard(d.values.minions[minion]);
    await sleep(250);
    return act.clickCard(d.values.freeCharacters[character]);
  }
  if ('unassign' in step) {
    // The minion clicked, then clicked again
    const d = needs('an assignment');
    used.add('unassign minion');
    await act.clickCard(d.values.minions[step.unassign]);
    await sleep(250);
    return act.clickCard(d.values.minions[step.unassign]);
  }
  if ('play' in step) {
    const d = needs('an action');
    const index = d.values.actionText.findIndex((text) => step.play.test(text));
    if (index < 0) throw new Error(`no action ${step.play} in ${JSON.stringify(d.values.actionText)}`);
    used.add('card click');
    await act.clickCard(d.values.cardId[index]);
    await sleep(300);
    // A card with several actions opens a menu of them
    if (!traffic.answered(d)) await act.actionMenu(d.values.actionId[index]);
    return;
  }
  if ('auto' in step) {
    for (let count = 0; count < step.auto; count++) {
      const d = traffic.decision();
      if (!d || (step.until && step.until.test(d.text))) return;
      await answerByPolicy(act, d, traffic, used);
      await waitForNextDecision(traffic, d);
    }
    return;
  }
  if ('cardInfo' in step) {
    const id = cardIdOf(traffic, step.cardInfo);
    const by = step.by ?? 'right click';
    used.add(`card info: ${by}`);
    if (by === 'shift click') return act.clickCard(id, { shift: true });
    if (by === 'drag up') return act.dragCard(id, 0, -40);
    return act.clickCard(id, { button: 'right' });
  }
  if ('drag' in step) {
    used.add('drag to reorder');
    return act.dragCard(cardIdOf(traffic, step.drag), step.by, 0);
  }
  if ('hover' in step) {
    const point = await act.pointOf(cardIdOf(traffic, step.hover));
    if (!point.x) {
      throw new Error(
        `card ${JSON.stringify(step.hover)} can't be hovered${point.blocked ? `: ${point.blocked} lies over it` : ''}`,
      );
    }
    used.add('hover zoom');
    await page.mouse.move(point.x, point.y);
    return sleep(1200);
  }
  if ('tab' in step) return act.click(select.tab(step.tab), `tab ${step.tab}`);
  if ('pile' in step) {
    const name = step.pile === 'deck' ? 'pile deck after the game' : `pile ${step.pile}`;
    return act.click(select.pile(step.pile, step.player ?? 0), name);
  }
  if ('stats' in step) return act.click(select.statsToggle(step.stats), 'counters +');
  if ('setting' in step) {
    const name = step.setting.startsWith('autoPass') ? 'setting autoPass' : `setting ${step.setting}`;
    if ('value' in step) {
      used.add(name);
      return page.locator(select.setting(step.setting)).selectOption(step.value);
    }
    return act.click(select.setting(step.setting), name);
  }
  if ('slider' in step) {
    // Its handle focused by a click, then a key
    await act.click(select.sliderHandle(step.slider), `setting ${step.slider}`);
    return page.keyboard.press(step.key ?? 'ArrowRight');
  }
  if ('timerAlert' in step) {
    used.add('setting timer alerts');
    const field = page.locator(select.timerAlertField(step.timerAlert)).first();
    await field.fill(String(step.value));
    return field.press('Tab');
  }
  if ('toggle' in step)
    return act.click(select.toggle(step.toggle), step.toggle === 'auto-zoom' ? 'auto-zoom toggle' : 'timer mute');
  if ('chat' in step) {
    used.add('chat message');
    const input = page.locator(select.chatInput).first();
    await input.fill(step.chat);
    return input.press('Enter');
  }
  if ('replay' in step) {
    await act.click(select.replay(step.replay), `replay ${step.replay}`);
    if (step.replay === 'play') return waitForReplay(page, kind);
    return;
  }
  if ('key' in step) {
    if (step.key === 'Escape') used.add('Escape');
    return page.keyboard.press(step.key);
  }
  if ('click' in step) {
    used.add('card info closed');
    return page.mouse.click(5, 5);
  }
  if ('button' in step) {
    return act.click(select.button(step.button), step.button === 'concedeGame' ? 'concede' : 'request cancel');
  }
  throw new Error(`unknown step ${JSON.stringify(step)}`);
}

/** Waits until a replay has played its last event (at most 2 minutes). */
async function waitForReplay(page, kind) {
  await page.waitForFunction(
    (kind) =>
      kind === 'legacy'
        ? window.ui?.replayGameEvents != null && window.ui.replayGameEventNextIndex >= window.ui.replayGameEvents.length
        : document.querySelector('[data-replay-done]') !== null,
    kind,
    { timeout: 120000 },
  );
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
        (!which.blueprint || event.blueprintId === which.blueprint) &&
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

/**
 * Plays a scenario on one kind of page: a snapshot after the page loads and after each step that isn't `quiet`.
 * `scenario.mode`: 'player' (the default: the player in the browser, the opponent over HTTP), 'spectator' (both sides
 * over HTTP, a third player watches in the browser) or 'replay' (both sides over HTTP, then the player concedes and
 * opens the game's replay in the browser). In the last two, `scenario.answers` is how many answers both sides give.
 */
async function play(kind, scenario, size, pair, label) {
  const mode = scenario.mode ?? 'player';
  const used = new Set(mode === 'player' ? [] : [mode]);
  let game;
  try {
    game = await startGame(options.server, pair, password);
  } catch (error) {
    return { snapshots: [], errors: [], problems: [`${label}: ${error.message}`], used, events: [] };
  }
  const context = await browser.newContext({ viewport: { width: size.width, height: size.height } });
  const page = await context.newPage();
  const errors = [];
  const dialogs = [];
  const snapshots = [];
  const recorded = [];
  const gestures = [];
  let bots = [];
  let traffic;
  try {
    const viewer = mode === 'spectator' ? await session(options.server, pair.watcher, password) : game.player;
    const cookie = viewer.cookie.split('; ').map((pairText) => {
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
      if (dialog.type() === 'confirm') used.add('browser confirm');
      dialogs.push(`${dialog.type()}: ${dialog.message()}`);
      await dialog.accept();
    });
    traffic = watchTraffic(page, mode === 'spectator' ? pair.watcher : pair.player, game.gameId);
    // Each joins the game only after the one before has (protocol.mjs Foe.ready), the page last
    if (mode === 'player') {
      // `foeAnswers`: the opponent stops answering after that many (it then waits, and the page polls)
      const budget = scenario.foeAnswers ? { left: scenario.foeAnswers } : null;
      bots = [new Foe(game, pair.foe, scenario.foe ?? [], () => traffic.decision() === null, { budget })];
      await bots[0].ready;
    } else {
      // Both sides over HTTP, the player first when both are asked, with a shared number of answers
      const budget = { left: scenario.answers ?? 20 };
      const player = new Foe(game, pair.player, scenario.playerRules ?? [], () => true, {
        session: game.player,
        budget,
      });
      await player.ready;
      bots = [player, new Foe(game, pair.foe, scenario.foe ?? [], () => !player.waiting, { budget })];
      await bots[1].ready;
    }
    if (options.verbose) bots.forEach((bot) => (bot.log = console.log));
    if (label === 'B' && injection?.apply) await injection.apply(page, kind);

    const pagePath = kind === 'legacy' ? options.legacy : options.angular;
    let query = `gameId=${encodeURIComponent(game.gameId)}`;
    if (mode === 'replay') {
      await idle(bots);
      bots.forEach((bot) => bot.stop());
      await endGame(game);
      // As the game history links it: the server takes the "$" as it is
      query = `replayId=${await replayIdOf(game, pair.player)}`;
    }
    await page.goto(`${options.server}${pagePath}?${query}`);
    const masks = (kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT).masks;
    // The mouse leaves the page before each snapshot (no hover state), except after a step that rests it on something
    const snapshot = async (name, keepMouse = false) => {
      if (!keepMouse) await page.mouse.move(1, 1);
      await settle(page, kind, traffic, bots);
      if (label === 'B' && injection?.afterSettle) await injection.afterSettle(page, kind);
      if (options.record) {
        recorded.push({
          name,
          exchanges: traffic.exchanges.length,
          // Gestures since the last snapshot that sent nothing but change the game the page holds
          gestures: gestures.splice(0),
          model: await inPage(page, readLegacyModel, null),
        });
      }
      snapshots.push({
        name,
        screen: await page.screenshot({ mask: masks.map((selector) => page.locator(selector)), maskColor: '#ff00ff' }),
        state: await inPage(page, readGameState, { kind }),
        requests: traffic.requests.splice(0),
        dialogs: dialogs.splice(0),
      });
    };
    if (mode === 'replay') {
      await page.waitForFunction(() => window.ui?.replayGameEvents != null, null, { timeout: 20000 });
    } else {
      await waitForFirst(traffic);
    }
    await snapshot('start');
    for (const [index, step] of scenario.steps.entries()) {
      const before = traffic.decision();
      // Assignments stay in the page until Done: the recording keeps them, with how many exchanges came before
      if ('assign' in step && before) {
        const [minion, character] = step.assign;
        gestures.push({
          after: traffic.exchanges.length,
          assign: [before.values.minions[minion], before.values.freeCharacters[character]],
        });
      }
      if ('unassign' in step && before) {
        gestures.push({ after: traffic.exchanges.length, unassign: before.values.minions[step.unassign] });
      }
      await doStep(page, kind, step, traffic, used);
      if (before && !('auto' in step) && isAnswer(step)) await waitForNextDecision(traffic, before);
      if (!step.quiet) await snapshot(step.name ?? `step ${index + 1}`, 'hover' in step);
    }
    if (scenario.steps.at(-1)?.quiet) await snapshot('end');
    // A replay reads /replay, which the jsdom tests don't serve (they play games)
    if (options.record && mode !== 'replay') writeRecording(scenario, traffic, recorded, size);
    return { snapshots, errors, problems: bots.flatMap((bot) => bot.problems), used, events: traffic.events };
  } catch (error) {
    return {
      snapshots,
      errors,
      problems: [...bots.flatMap((bot) => bot.problems), `${label}: ${error.message.split('\n')[0]}`],
      used,
      events: traffic?.events ?? [],
    };
  } finally {
    bots.forEach((bot) => bot.stop());
    await endGame(game);
    await context.unrouteAll({ behavior: 'ignoreErrors' });
    await context.close();
  }
}

/** Waits until every bot has nothing to answer and no answer on the way. */
async function idle(bots) {
  for (let round = 0; round < 3; round++) {
    for (const bot of bots) await bot.idle();
  }
}

const RECORDINGS = path.resolve(import.meta.dirname, '../../src/testing/game-recordings');

/** The test data of the jsdom tests: one file per scenario (game/README.md "Recordings"). */
function writeRecording(scenario, traffic, steps, size) {
  fs.mkdirSync(RECORDINGS, { recursive: true });
  const recording = {
    scenario: scenario.name,
    note: scenario.note ?? '',
    participant: traffic.participant,
    mode: scenario.mode ?? 'player',
    size: size.label,
    exchanges: traffic.exchanges,
    steps,
  };
  fs.writeFileSync(path.join(RECORDINGS, `${scenario.name}.json`), `${JSON.stringify(recording, null, 1)}\n`);
}

/** Whether a step answers the decision (then the next one is waited for). A `pick` with `then` only says so itself. */
const isAnswer = (step) =>
  step.wait !== false &&
  (step.answers ||
    (!('then' in step) && ['integer', 'choice', 'pick', 'pass', 'done', 'play'].some((key) => key in step)));

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

/** What page A used and was sent, over every scenario (coverage.mjs). */
const covered = new Set();

async function check(scenario, size, worker) {
  const pair = pairOf(worker);
  const a = await play(pageA, scenario, size, pair, 'A');
  for (const key of coveredBy(a.events, a.used)) covered.add(key);
  if (options.record) {
    const problems = [...a.problems, ...a.errors.map((error) => `page error: ${error}`)];
    if (scenario.mode === 'replay') {
      console.log(`skipped  ${scenario.name}: a replay isn't recorded (the jsdom tests play games)`);
      return { scenario, size, problems: [], pixels: 0, images: [] };
    }
    console.log(
      `${problems.length ? 'FAILED ' : 'recorded'} ${scenario.name}${problems.length ? `: ${problems.join(' | ')}` : ''}`,
    );
    return { scenario, size, problems, pixels: 0, images: [] };
  }
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

function writeReport(results, gaps) {
  fs.writeFileSync(
    path.join(outDir, 'results.json'),
    JSON.stringify({
      checks: results.map((r) => ({ name: `${r.scenario.name}-${r.size.label}`, problems: r.problems })),
      gaps,
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
${gaps.length ? `<h2>Not covered (${gaps.length})</h2><ul>${gaps.map((gap) => `<li>${escape(gap)}</li>`).join('')}</ul>` : ''}
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
  // Only a run of every scenario says what is covered
  const gaps = only || injection || options.record ? [] : coverageGaps(covered);
  for (const gap of gaps) console.log(`GAP  ${gap}`);
  writeReport(results, gaps);
  const failed = results.filter((r) => r.problems.length).length;
  const notCovered = only || injection || options.record ? '' : `, ${gaps.length} not covered`;
  console.log(`\n${results.length} checks, ${failed} differ${notCovered}. Report: ${path.join(outDir, 'index.html')}`);
  process.exitCode = failed || gaps.length ? 1 : 0;
} finally {
  await browser.close();
}
