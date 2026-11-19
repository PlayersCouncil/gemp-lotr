// Tutorials (scripted games) on the Angular game page: real games on a game check server kept up, the player on a
// page in a real browser and the opponent played by the server's script. The server sends messages the page shows in
// a bubble joined to cards and pieces of the page, takes only the clicks a step allows, and ends with a panel. Each
// check plays its own game and says what it found; a screenshot of every step worth seeing is saved in the report
// folder, with a text file per check (what it found and every move it made).
//
//   npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
//   npm run check:game:tutorial                                    every check, in both themes
//   npm run check:game:tutorial -- --only t1,refuse --theme modern --out report-tutorial
//   npm run check:game:down
//
// The checks follow the bubbles as a player would (`follow` below): the bubble's Next, a choice (the guided path),
// a click on what is outlined as next, a right click on the card a message waits to have opened, Escape to close it,
// and, where a step takes any answer, the first card or button the game offers.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { launchBrowser } from '../browser.mjs';
import { session } from '../local-server/seed.mjs';
import { DEVICES } from './device-profiles.mjs';
import { pairOf } from './players.mjs';
import { parseGameXml } from './protocol.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const { server, password } = JSON.parse(fs.readFileSync(path.join(CHECK_DIR, '.local-server/game/login.json'), 'utf8'));

const argument = (name, fallback) => {
  const at = process.argv.indexOf(`--${name}`);
  return at < 0 ? fallback : process.argv[at + 1];
};
const KIND = argument('page', 'angular');
const OUT = path.resolve(CHECK_DIR, argument('out', 'report-tutorial'));
const ONLY = argument('only', '').split(',').filter(Boolean);
const THEMES = argument('theme', 'classic,modern').split(',').filter(Boolean);
const VERBOSE = process.argv.includes('--verbose');

/** The tutorials the checks play (the server's `scenarios/tutorials/*.hjson`), by the id `scenarios/start` takes. */
const TUTORIAL_1 = 'basics-01';
const TUTORIAL_2 = 'basics-02-items-damage';
/** Tutorial 1's one choice: `help` is the guided path, `plan` leaves the player to it. */
const GUIDED = 'help';

/** Where a page keeps what a scripted game shows, per kind of page (only the Angular page has this DOM). */
const PAGES = {
  angular: {
    path: '/gemp-lotr/ng/game',
    bubble: '.gd-scenario-bubble',
    bubbleHead: '.gd-scenario-bubble__head',
    bubbleBody: '.gd-scenario-bubble__body',
    bubbleFoot: '.gd-scenario-bubble__foot',
    bubbleFold: '.gd-scenario-bubble__fold',
    next: 'button[data-scenario="next"]',
    choice: 'button[data-choice]',
    outline: '.gd-scenario-outline',
    hint: '.gd-scenario-hint',
    zoom: '.gd-scenario-zoom',
    menu: 'nav.gd-scenario-menu',
    menuButton: (name) => `[data-scenario-menu="${name}"]`,
    panel: '.gd-scenario-panel',
    panelButton: (label) => `.gd-scenario-panel button[data-panel-button="${label}"]`,
    report: '.gd-scenario-panel__report a',
    /** The page's own question dialog (not the browser's). */
    confirm: 'dialog.gd-dialog[open][data-dialog="confirm"]',
    confirmText: 'dialog.gd-dialog[open][data-dialog="confirm"] .gd-notifier__message',
    confirmButton: (action) => `dialog.gd-dialog[open][data-dialog="confirm"] button[data-action="${action}"]`,
    cardInfo: 'dialog.gd-card-info[open]',
    decisionText: '[data-game="alert"]',
    decisionButton: (label) => `button[data-decision-button="${label}"]`,
    players: '.gd-game-player',
    chat: 'gd-game-chat',
    game: 'gemp-game',
    replayPlay: 'gd-game-replay button[data-replay="play"]',
    replayFaster: 'gd-game-replay button[data-replay="faster"]',
  },
};
const ui = PAGES[KIND];
if (!ui) {
  throw new Error(`--page ${KIND}: only ${Object.keys(PAGES).join(', ')} shows scripted games this way`);
}

/** The page's selectors that are plain strings, for reading the page from inside it. */
const SELECTORS = Object.fromEntries(Object.entries(ui).filter(([, value]) => typeof value === 'string'));

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** Waits until `read()` gives something (not null, undefined or false), and returns it; throws saying `what` if not. */
async function until(read, what, max = 15000) {
  const deadline = Date.now() + max;
  for (;;) {
    const value = await read().catch(() => undefined);
    if (value !== null && value !== undefined && value !== false) return value;
    if (Date.now() > deadline) throw new Error(`waited ${max / 1000}s for ${what}`);
    await sleep(100);
  }
}

/**
 * What the page shows of the scripted game at this moment, read in one go (in the page). Boxes are `{ x, y, w, h }`
 * in the window's pixels.
 */
function readTutorial(ui) {
  const q = (selector) => document.querySelector(selector);
  const all = (selector) => Array.from(document.querySelectorAll(selector));
  const box = (el) => {
    const r = el.getBoundingClientRect();
    return { x: Math.round(r.left), y: Math.round(r.top), w: Math.round(r.width), h: Math.round(r.height) };
  };
  const text = (el) => (el?.innerText ?? '').replace(/\s+/g, ' ').trim();
  const bubble = q(ui.bubble);
  const panel = q(ui.panel);
  const hint = q(ui.hint);
  const decision = q(ui.decisionText);
  return {
    panel: panel?.getAttribute('data-panel') ?? null,
    result: panel?.getAttribute('data-result') ?? null,
    heading: text(panel?.querySelector('h2')),
    panelText: text(panel),
    bubble: bubble
      ? {
          placed: bubble.hasAttribute('data-placed'),
          collapsed: bubble.hasAttribute('data-collapsed'),
          moved: bubble.hasAttribute('data-moved'),
          title: text(bubble.querySelector('.gd-scenario-bubble__title')),
          count: text(bubble.querySelector('.gd-scenario-bubble__count')),
          text: text(bubble.querySelector(ui.bubbleBody)),
          foot: Boolean(bubble.querySelector(ui.bubbleFoot)),
          buttons: bubble.querySelectorAll('button.gd-scenario-button').length,
          nudge: bubble.querySelector(ui.bubbleFoot)?.getAttribute('data-nudge') ?? null,
          box: box(bubble),
        }
      : null,
    next: text(q(ui.next)) || null,
    choices: all(ui.choice).map((button) => button.getAttribute('data-choice')),
    nextOutlines: all(`${ui.outline}[data-look="next"]`).map(box),
    outlines: all(`${ui.outline}:not([data-look="next"])`).map(box),
    hint: hint ? { text: text(hint), box: box(hint) } : null,
    zoom: Boolean(q(ui.zoom)),
    info: Boolean(q(ui.cardInfo)),
    dialog: q('dialog.gd-dialog[open]')?.getAttribute('data-dialog') ?? null,
    decision: decision ? text(decision) : null,
    animating: Boolean(q(ui.game)?.hasAttribute('data-animating')),
    live: all('[data-scenario="live"]').length,
    notnow: all('[data-scenario="notnow"]').length,
    menu: Boolean(q(ui.menu)),
    players: all(ui.players).map((el) => text(el)),
    scenarioName: (() => {
      // Where the page names the scripted opponent by its seat's name (in a text or a title), if anywhere
      const game = q(ui.game);
      if (!game) return null;
      const walk = document.createTreeWalker(game, NodeFilter.SHOW_TEXT);
      while (walk.nextNode()) {
        if (walk.currentNode.textContent.includes('~Scenario')) {
          const el = walk.currentNode.parentElement;
          const where = el.closest('[class]')?.className ?? el.tagName.toLowerCase();
          return `"${walk.currentNode.textContent.trim().slice(0, 80)}" in .${String(where).split(' ')[0]}`;
        }
      }
      const titled = game.querySelector('[title*="~Scenario"]');
      return titled ? `title "${titled.getAttribute('title').slice(0, 80)}" of .${titled.className}` : null;
    })(),
    window: { w: window.innerWidth, h: window.innerHeight },
  };
}

/** The part of `a` that `b` covers, as a share of `a`'s area (0 to 1). */
function covered(a, b) {
  const w = Math.min(a.x + a.w, b.x + b.w) - Math.max(a.x, b.x);
  const h = Math.min(a.y + a.h, b.y + b.h) - Math.max(a.y, b.y);
  return w <= 0 || h <= 0 || a.w * a.h === 0 ? 0 : (w * h) / (a.w * a.h);
}

const centre = (b) => ({ x: b.x + b.w / 2, y: b.y + b.h / 2 });
const short = (text, length = 90) => (text && text.length > length ? `${text.slice(0, length - 1)}…` : (text ?? ''));

/** A tutorial the check is stuck in: where it was and what it last did. */
class Stuck extends Error {}

/** The player's page in one check: what went wrong on it, what it sent the server, and its screenshots. */
class Seat {
  constructor(check, page, player) {
    this.check = check;
    this.page = page;
    this.player = player;
    this.errors = [];
    /** Every request the page sent about the game: `{ url, decisionId, ack }`. */
    this.sent = [];
    this.moves = [];
    this.shots = 0;
    page.on('pageerror', (error) => this.errors.push(`page error: ${error.message.split('\n')[0]}`));
    page.on('console', (message) => {
      // A picture the image server didn't send is the image server's business; the page's own errors count
      if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) {
        this.errors.push(`console error: ${short(message.text(), 200)}`);
      }
    });
    page.on('response', (response) => {
      if (response.status() >= 500 && response.url().includes('/gemp-lotr-server/')) {
        this.errors.push(`HTTP ${response.status()} from ${new URL(response.url()).pathname}`);
      }
    });
    page.on('request', (request) => {
      const url = new URL(request.url());
      if (request.method() !== 'POST' || !/\/gemp-lotr-server\/game\//.test(url.pathname)) return;
      const body = new URLSearchParams(request.postData() ?? '');
      this.sent.push({
        url: url.pathname,
        decisionId: body.get('decisionId'),
        ack: url.pathname.endsWith('/scenarioAck'),
      });
    });
    // The browser's own dialogs would stop the page: the scripted game has none of its own, so any is reported
    page.on('dialog', (dialog) => {
      this.errors.push(`the browser's own dialog: ${dialog.message()}`);
      dialog.accept();
    });
  }

  /** The decisions answered so far (POST /game/<id> with a decisionId). */
  answers() {
    return this.sent.filter((request) => request.decisionId !== null && request.decisionId !== '').length;
  }

  read() {
    return this.page.evaluate(readTutorial, SELECTORS);
  }

  gameId() {
    return new URL(this.page.url()).searchParams.get('gameId');
  }

  log(line) {
    this.moves.push(line);
    if (VERBOSE) console.log(`      ${this.check}: ${line}`);
  }

  async shot(step) {
    fs.mkdirSync(OUT, { recursive: true });
    this.shots++;
    const name = `${this.check}-${String(this.shots).padStart(2, '0')}-${step}.png`;
    await this.page.screenshot({ path: path.join(OUT, name) }).catch(() => undefined);
    return name;
  }

  /** Waits for the first message of a game to be up and placed. */
  async firstMessage() {
    await this.page.locator(`${ui.bubble}[data-placed]`).waitFor({ timeout: 20000 });
    await sleep(300);
    return this.read();
  }
}

let browser;

/** A page logged in as the player, with the check's settings: the fastest animations and the theme. */
async function openSeat(check, who, player, theme, device = DEVICES.pc) {
  const { hostname } = new URL(server);
  const context = await browser.newContext({ ...device });
  const cookies = who.cookie.split('; ').map((text) => {
    const [name, ...value] = text.split('=');
    return { name, value: value.join('='), domain: hostname, path: '/' };
  });
  await context.addCookies([
    ...cookies,
    { name: 'animation-speed', value: '4', domain: hostname, path: '/' },
    { name: 'gempGameTheme', value: theme, domain: hostname, path: '/' },
  ]);
  return new Seat(check, await context.newPage(), player);
}

/** Starts a scripted game for the player; returns its id and what the server says of it (the info event). */
async function startTutorial(who, id) {
  const started = await (await who.post('/scenarios/start', { id })).json();
  if (!started.gameId) throw new Error(`scenarios/start ${id} answered ${JSON.stringify(started)}`);
  return { gameId: String(started.gameId), info: await scenarioEvents(who, started.gameId).then((s) => s.info) };
}

/**
 * The scripted game's events as the server sends them to the player (`<ge type="S" scenario="…">`): its info, the
 * last step and its end, if any. Read once over a new request: only before the page joins, or once it is done with.
 */
async function scenarioEvents(who, gameId) {
  const { events } = parseGameXml(await (await who.get(`/game/${gameId}`)).text());
  const said = events.filter((event) => event.type === 'S').map((event) => JSON.parse(event.scenario));
  return {
    info: said.find((event) => event.type === 'info') ?? null,
    step: said.filter((event) => event.type === 'step').at(-1) ?? null,
    end: said.find((event) => event.type === 'end') ?? null,
    all: said,
  };
}

/**
 * Plays the scripted game on as a player following the bubbles would, until its end panel (or until `stop(state)`
 * says so). Checks what must always hold on the way (into `problems`, each once). Throws `Stuck` saying where if
 * nothing changes for `patience` ms.
 */
async function follow(seat, problems, { opponentName, stop = null, max = 8 * 60000, patience = 25000 } = {}) {
  const page = seat.page;
  const said = new Set();
  const problem = (text) => {
    if (!said.has(text)) {
      said.add(text);
      problems.push(text);
    }
  };
  const start = Date.now();
  let lastKey = '';
  let changedAt = Date.now();
  let actedAt = 0;
  let lastText = null;
  const inspected = new Set();
  const seenShots = new Set();
  let messages = 0;
  for (;;) {
    await sleep(120);
    const st = await seat.read();
    // What changes when the game moves on (not the hint, which comes and goes by itself)
    const key = JSON.stringify([
      st.panel,
      st.bubble?.text,
      st.bubble?.count,
      st.next,
      st.choices,
      st.nextOutlines,
      st.info,
      st.decision,
      st.dialog,
      st.live,
      st.notnow,
    ]);
    if (key !== lastKey) {
      lastKey = key;
      changedAt = Date.now();
      continue;
    }
    if (st.panel === 'end') {
      await sleep(500);
      seat.log(`end panel: ${st.result} "${st.heading}"`);
      return { end: await seat.read(), messages };
    }
    if (st.panel) throw new Error(`the "${st.panel}" panel came up: ${short(st.panelText)}`);
    if (stop && (await stop(st))) return { stopped: st, messages };
    const where = () =>
      `decision "${short(st.decision ?? '(none)', 70)}", bubble "${short(st.bubble?.text ?? '(none)', 110)}"` +
      `${st.next ? `, button ${st.next}` : ''}${st.nextOutlines.length ? `, ${st.nextOutlines.length} outlined as next` : ''}` +
      `${st.notnow ? `, ${st.notnow} not now` : ''}${st.live ? `, ${st.live} live` : ''}`;
    if (Date.now() - changedAt > patience) throw new Stuck(`stuck for ${patience / 1000}s at ${where()}`);
    if (Date.now() - start > max) throw new Stuck(`not finished after ${max / 60000} minutes, at ${where()}`);
    // Settled: nothing moving and the same for a moment
    if (st.animating || Date.now() - changedAt < 350) continue;
    // Once a gesture was made, the page is given time to answer it before another
    if (Date.now() - actedAt < 1500 && changedAt < actedAt) continue;

    // What must always hold
    if (st.next && st.nextOutlines.length > 0) {
      problem(
        `a message with "${st.next}" is up and something is outlined as next anyway: "${short(st.bubble?.text)}"`,
      );
    }
    if (st.scenarioName) problem(`the page names the scripted opponent "~Scenario": ${st.scenarioName}`);
    if (opponentName && st.players.length && !st.players.some((name) => name.includes(opponentName))) {
      problem(`the opponent is not shown as "${opponentName}": ${JSON.stringify(st.players)}`);
    }
    if (st.bubble?.placed && !st.bubble.moved) {
      const b = st.bubble.box;
      if (b.x < -1 || b.y < -1 || b.x + b.w > st.window.w + 1 || b.y + b.h > st.window.h + 1) {
        problem(`the bubble is partly off the window (${JSON.stringify(b)}): "${short(st.bubble.text)}"`);
      }
      for (const target of st.nextOutlines) {
        if (covered(target, b) > 0.25) {
          problem(`the bubble covers what is to be clicked next: "${short(st.bubble.text)}"`);
        }
      }
    }
    if (st.bubble && !st.bubble.placed) continue;

    // A screenshot of each step worth seeing: the first message, choices, a card shown large, what to click next,
    // what a message points at, every headed message, the page's own dialogs, card information opened for a message
    if (st.bubble && st.bubble.text !== lastText) {
      lastText = st.bubble.text;
      messages++;
      seat.log(`message${st.bubble.count ? ` ${st.bubble.count}` : ''}: "${short(st.bubble.text, 120)}"`);
      const worth =
        messages === 1
          ? 'first'
          : st.choices.length
            ? 'choice'
            : st.zoom
              ? 'zoom'
              : st.nextOutlines.length
                ? 'click'
                : st.outlines.length
                  ? 'pointing'
                  : st.bubble.title
                    ? 'headed'
                    : null;
      if (worth && !seenShots.has(st.bubble.text)) {
        seenShots.add(st.bubble.text);
        await seat.shot(worth);
      }
    }
    if (st.dialog && !seenShots.has(`dialog ${st.dialog}`)) {
      seenShots.add(`dialog ${st.dialog}`);
      await seat.shot(`dialog-${st.dialog}`);
    }

    actedAt = Date.now();
    if (st.next) {
      seat.log(`  presses ${st.next}`);
      await page.locator(ui.next).click();
      continue;
    }
    if (st.choices.length) {
      const pick = st.choices.includes(GUIDED) ? GUIDED : st.choices[0];
      seat.log(`  chooses ${pick} of ${st.choices.join(', ')}`);
      await page.locator(`${ui.choice}[data-choice="${pick}"]`).click();
      continue;
    }
    if (st.info) {
      seat.log('  closes the card information (Escape)');
      if (!seenShots.has(`info ${st.bubble?.text}`)) {
        seenShots.add(`info ${st.bubble?.text}`);
        await seat.shot('card-info');
      }
      await page.keyboard.press('Escape');
      continue;
    }
    if (st.dialog === 'confirm' && !st.nextOutlines.length) {
      // The page's own question in a free step (a minion left unassigned): yes
      seat.log(`  answers the page's question with OK (${where()})`);
      await page.locator(ui.confirmButton('ok')).click();
      continue;
    }
    if (st.nextOutlines.length) {
      const at = await page.evaluate(whereToClick, { outline: st.nextOutlines[0], bubble: ui.bubble });
      if (at.onBubble) problem(`what is outlined as next is under the bubble: "${short(st.bubble?.text)}"`);
      if (at.coveredBy) {
        problem(
          `what is outlined as next (${at.target}) has ${at.coveredBy} at the centre of its outline, so the outline ` +
            `is drawn round that instead: clicked the visible part (at "${short(st.decision, 50)}", bubble "${short(st.bubble?.text, 70)}")`,
        );
      }
      seat.log(
        `  clicks what is outlined as next (${at.target}) at ${Math.round(at.x)},${Math.round(at.y)} (${where()})`,
      );
      await page.mouse.click(at.x, at.y);
      // Off the cards again, so the hover zoom is not in the way of the next click
      await page.mouse.move(2, 2);
      continue;
    }
    if (st.bubble && st.outlines.length && !inspected.has(st.bubble.text)) {
      // A message about a card with no button waits for its information to be opened (a right click)
      inspected.add(st.bubble.text);
      const { x, y } = centre(st.outlines[0]);
      seat.log(`  opens the information of what the message outlines at ${Math.round(x)},${Math.round(y)}`);
      await page.mouse.click(x, y, { button: 'right' });
      continue;
    }
    if (Date.now() - changedAt > 1500 && /^Assign minions/.test(st.decision ?? '')) {
      // An assignment the step leaves to the player: each minion to a companion of its own, then Done
      const assigned = await assignFreely(seat);
      seat.log(`  ${assigned} (${where()})`);
      continue;
    }
    // A step that takes any answer: the first card the game offers that the step doesn't refuse, or the decision's
    // button
    const free = await page.evaluate(() => {
      const card = document.querySelector(
        '[data-mark="selectable"]:not([data-scenario="notnow"]), [data-mark="actionable"]:not([data-scenario="notnow"])',
      );
      const el =
        card ??
        document.querySelector(
          'button[data-decision-button="Done"], button[data-decision-button="Pass"], button[data-decision-button="OK"], button[data-decision-choice]',
        );
      if (!el) return null;
      const r = el.getBoundingClientRect();
      return {
        x: r.left + r.width / 2,
        y: r.top + r.height / 2,
        what: el.getAttribute('data-decision-button') ?? 'a card',
      };
    });
    if (free && Date.now() - changedAt > 1500) {
      seat.log(`  free click on ${free.what} (${where()})`);
      await page.mouse.click(free.x, free.y);
      await page.mouse.move(2, 2);
      continue;
    }
    actedAt = 0;
  }
}

/**
 * What lies under each "next" outline, for a check that is stuck: the cards the step wants clicked (`live`) with
 * their boxes, and what the page has at the centre of each outline.
 */
function lookUnder(ui) {
  const box = (el) => {
    const r = el.getBoundingClientRect();
    return { x: Math.round(r.left), y: Math.round(r.top), w: Math.round(r.width), h: Math.round(r.height) };
  };
  const describe = (el) =>
    el
      ? `${el.tagName.toLowerCase()}${el.closest('[data-card-id]') ? ` in card ${el.closest('[data-card-id]').getAttribute('data-card-id')}` : ''}`
      : null;
  return {
    live: Array.from(document.querySelectorAll('[data-scenario="live"]'), (el) => ({
      id: el.getAttribute('data-card-id'),
      zone: el.getAttribute('data-zone'),
      under: el.getAttribute('data-under'),
      mark: el.getAttribute('data-mark'),
      box: box(el),
    })),
    outlines: Array.from(document.querySelectorAll(`${ui.outline}[data-look="next"]`), (el) => {
      const b = box(el);
      return { box: b, atCentre: describe(document.elementFromPoint(b.x + b.w / 2, b.y + b.h / 2)) };
    }),
  };
}

/**
 * Where to click what an outline marks as next: the thing it is round (a card the step wants, a decision's button
 * or option, the one whose box the outline fits best), at the outline's centre if that is where the thing is, else
 * at a part of it that shows. Says what lies at the centre instead, if anything.
 */
function whereToClick({ outline, bubble }) {
  const pad = 4;
  const fit = (el) => {
    const r = el.getBoundingClientRect();
    return (
      Math.abs(r.left - (outline.x + pad)) +
      Math.abs(r.top - (outline.y + pad)) +
      Math.abs(r.width - (outline.w - 2 * pad)) +
      Math.abs(r.height - (outline.h - 2 * pad))
    );
  };
  const candidates = Array.from(
    document.querySelectorAll('[data-scenario="live"], button[data-decision-button], button[data-decision-choice]'),
  ).filter((el) => el.getBoundingClientRect().width > 0);
  const target = candidates.sort((a, b) => fit(a) - fit(b))[0];
  const cx = outline.x + outline.w / 2;
  const cy = outline.y + outline.h / 2;
  const name = (el) =>
    !el
      ? 'nothing'
      : el.closest('[data-card-id]')
        ? `card ${el.closest('[data-card-id]').getAttribute('data-card-id')}${el.closest('[data-under]') ? ` (${el.closest('[data-under]').getAttribute('data-under')})` : ''}`
        : el.closest('button')
          ? `button "${el.closest('button').textContent.trim()}"`
          : el.tagName.toLowerCase();
  const atCentre = document.elementFromPoint(cx, cy);
  const onBubble = Boolean(atCentre?.closest(bubble));
  if (!target || fit(target) > 12 || (atCentre && target.contains(atCentre))) {
    return { x: cx, y: cy, target: name(target ?? atCentre), coveredBy: null, onBubble };
  }
  // The thing's box, searched for a point where it is the one on top
  const r = target.getBoundingClientRect();
  for (let i = 1; i < 12; i++) {
    for (let j = 1; j < 12; j++) {
      const x = r.left + (r.width * i) / 12;
      const y = r.top + (r.height * j) / 12;
      if (target.contains(document.elementFromPoint(x, y))) {
        return { x, y, target: name(target), coveredBy: name(atCentre), onBubble };
      }
    }
  }
  return { x: cx, y: cy, target: name(target), coveredBy: `${name(atCentre)} (all of it)`, onBubble };
}

/** The cards the waiting decision offers, where they are and how they stand. */
function offeredCards(page) {
  return page.evaluate(() =>
    Array.from(document.querySelectorAll('[data-card-id][data-mark]'), (el) => {
      const r = el.getBoundingClientRect();
      return {
        id: el.getAttribute('data-card-id'),
        zone: el.getAttribute('data-zone'),
        assign: el.getAttribute('data-assign'),
        scenario: el.getAttribute('data-scenario'),
        x: r.left + r.width / 2,
        y: r.top + r.height / 2,
      };
    }).filter((card) => card.x > 0 && card.y > 0),
  );
}

/**
 * One move of an assignment the step leaves to the player: the next minion not yet assigned goes to a companion no
 * minion has been sent to yet (else the first); with every minion assigned, Done. Says what it did.
 */
async function assignFreely(seat) {
  const page = seat.page;
  const minion = (await offeredCards(page)).find(
    (card) => card.zone === 'SHADOW_CHARACTERS' && !card.assign && card.scenario !== 'notnow',
  );
  if (!minion) {
    await page.locator(ui.decisionButton('Done')).click();
    return 'presses Done, every minion assigned';
  }
  await page.mouse.click(minion.x, minion.y);
  await page.mouse.move(2, 2);
  const characters = await until(async () => {
    const free = (await offeredCards(page)).filter(
      (card) => card.zone === 'FREE_CHARACTERS' && card.scenario !== 'notnow',
    );
    return free.length ? free : null;
  }, 'the characters a minion may go to');
  const taken = await page.evaluate(() =>
    Array.from(document.querySelectorAll('[data-card-id][data-assign]'), (el) => el.getAttribute('data-assign')),
  );
  const to = characters.find((card) => !taken.includes(card.id)) ?? characters[0];
  await page.mouse.click(to.x, to.y);
  await page.mouse.move(2, 2);
  return `assigns minion ${minion.id} to ${to.id}`;
}

/** What every finished playthrough must show: the end panel `Complete`. */
function expectComplete(result, problems) {
  if (!result.end) return;
  if (result.end.result !== 'complete' || result.end.heading !== 'Complete') {
    problems.push(`it ended "${result.end.heading}" (${result.end.result}): ${short(result.end.panelText, 200)}`);
  }
}

/** The finished tutorial 1 game of each theme (its replay's id), for the replay check. */
const finished = {};

/** The replay's id in a link's address (the feedback form's, which carries it in its query string), or null. */
function replayIdIn(href, player) {
  if (!href) return null;
  const query = decodeURIComponent(href);
  return query.match(/replayId=([^&#\s'"]+)/)?.[1] ?? query.match(new RegExp(`${player}\\$[\\w-]+`))?.[0] ?? null;
}

/** The end panel's "Report it" link and the replay it names. */
async function replayIdFromPanel(seat) {
  const href = await seat.page
    .locator(ui.report)
    .first()
    .getAttribute('href', { timeout: 5000 })
    .catch(() => null);
  return { href, replayId: replayIdIn(href, seat.player) };
}

/** The replay named by the server's thanks at the end of the game, as the game's chat shows it. */
async function replayIdFromChat(seat) {
  const hrefs = await seat.page
    .locator(`${ui.chat} a[href]`)
    .evaluateAll((links) => links.map((link) => link.getAttribute('href')));
  return hrefs.map((href) => replayIdIn(href, seat.player)).find(Boolean) ?? null;
}

/** Tutorial `id` played through to its end on the guided path. */
async function playThrough(table, problems, id) {
  const { seat, who } = table;
  const game = await startTutorial(who, id);
  table.gameId = game.gameId;
  await seat.page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
  await seat.firstMessage();
  const opponentName = game.info?.opponentName ?? null;
  if (!opponentName) problems.push('the server named no opponent for the scripted game');
  let result;
  try {
    result = await follow(seat, problems, { opponentName });
  } catch (error) {
    if (error instanceof Stuck) {
      // What the server had asked at that point, for working out why
      await seat.shot('stuck');
      const { step } = await scenarioEvents(who, game.gameId).catch(() => ({ step: null }));
      const page = await seat.page.evaluate(lookUnder, SELECTORS).catch((e) => String(e));
      if (step) {
        fs.writeFileSync(path.join(OUT, `${seat.check}-stuck-step.json`), JSON.stringify({ step, page }, null, 2));
        error.message += ` (server's step ${step.step}.${step.sub}${step.name ? ` "${step.name}"` : ''}, ${seat.check}-stuck-step.json)`;
      }
    }
    throw error;
  }
  await seat.shot('end');
  expectComplete(result, problems);
  table.note = `${result.messages} messages, ${seat.answers()} decisions answered`;
  return result;
}

const CHECKS = {
  /** Tutorial 1 played through on the guided path to "Complete". */
  async t1(table, problems) {
    await playThrough(table, problems, TUTORIAL_1);
    const { href, replayId } = await replayIdFromPanel(table.seat);
    const said = await replayIdFromChat(table.seat);
    if (!replayId) {
      problems.push(
        `the end panel has no "Report it" link naming the replay (${href ? `its link: ${href}` : 'no link'})` +
          `${said ? `, though the server's thanks in the chat names ${said}` : ''}`,
      );
    }
    if (replayId ?? said) finished[table.theme] = replayId ?? said;
  },

  /** Tutorial 2 played through the same way. */
  async t2(table, problems) {
    await playThrough(table, problems, TUTORIAL_2);
  },

  /**
   * A click the step doesn't take: while a message is read, the buttons under it shake and nothing is sent; while
   * the step is answered, a card the game would take but the step doesn't (`notnow`) shows why, by the card, and
   * nothing is sent.
   */
  async refuse(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    const game = await startTutorial(who, TUTORIAL_1);
    table.gameId = game.gameId;
    await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
    const first = await seat.firstMessage();
    if (!first.next) throw new Error(`the first message has no Next: "${short(first.bubble?.text)}"`);
    const sentBefore = seat.sent.length;
    const pass = page.locator(ui.decisionButton('Pass'));
    if (!(await pass.isVisible()))
      throw new Error(`no Pass button under the first message (decision "${first.decision}")`);
    await pass.click();
    const nudged = await until(
      async () => (await seat.read()).bubble?.nudge ?? null,
      'the buttons to shake',
      3000,
    ).catch(() => null);
    await seat.shot('reading-refused');
    if (nudged === null)
      problems.push('Pass clicked while a message is read: the buttons did not shake (no data-nudge)');
    // Another refused click shakes them again (the other of the two animations)
    await pass.click();
    await sleep(300);
    const again = (await seat.read()).bubble?.nudge ?? null;
    if (again === nudged) problems.push(`a second refused click did not shake the buttons again (data-nudge ${again})`);
    await sleep(700);
    const after = await seat.read();
    if (seat.sent.length !== sentBefore) {
      problems.push(`refused clicks while reading sent ${JSON.stringify(seat.sent.slice(sentBefore))}`);
    }
    if (after.bubble?.text !== first.bubble?.text) problems.push('a refused click moved the messages on');

    // On to a step that is answered with a card the step doesn't take among those offered
    const { stopped } = await follow(seat, problems, {
      opponentName: game.info?.opponentName,
      stop: (st) => !st.next && st.choices.length === 0 && !st.info && st.notnow > 0,
    });
    if (!stopped) throw new Error('tutorial 1 ended without a card the step does not take');
    table.note = `refused card at "${short(stopped.decision, 50)}"`;
    const card = page.locator('[data-scenario="notnow"]').first();
    const cardBox = await card.boundingBox();
    const answered = seat.answers();
    const acks = seat.sent.filter((request) => request.ack).length;
    await card.click();
    const hint = await until(async () => (await seat.read()).hint, 'the hint for the refused card', 3000).catch(
      () => null,
    );
    await seat.shot('answering-refused');
    if (!hint) {
      problems.push('a card marked notnow was clicked: no .gd-scenario-hint came up');
    } else {
      table.note += `, hint "${short(hint.text, 60)}"`;
      const near =
        Math.max(0, Math.max(hint.box.x - (cardBox.x + cardBox.width), cardBox.x - (hint.box.x + hint.box.w))) +
        Math.max(0, Math.max(hint.box.y - (cardBox.y + cardBox.height), cardBox.y - (hint.box.y + hint.box.h)));
      if (near > 150) problems.push(`the hint is ${Math.round(near)}px away from the card it is about`);
      if (!hint.text) problems.push('the hint says nothing');
    }
    await sleep(1500);
    if (seat.answers() !== answered) problems.push('the refused card was sent to the server as an answer');
    if (seat.sent.filter((request) => request.ack).length !== acks)
      problems.push('the refused card acknowledged a step');
    const now = await seat.read();
    if (now.decision !== stopped.decision) problems.push(`the decision changed: "${now.decision}"`);
  },

  /**
   * The bubble: Space and Escape read on; dragged by its head it stays where it is put; folded and unfolded; the
   * next message is unfolded again and in its own place.
   */
  async bubble(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    const game = await startTutorial(who, TUTORIAL_1);
    table.gameId = game.gameId;
    await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
    const first = await seat.firstMessage();
    const count = async () => (await seat.read()).bubble?.count;
    if (!/^1 \/ \d+$/.test(first.bubble.count)) problems.push(`the first message is counted "${first.bubble.count}"`);
    const of = first.bubble.count.split('/')[1]?.trim();
    await page.keyboard.press(' ');
    await until(async () => (await count()) === `2 / ${of}`, 'Space to read on', 3000).catch(() =>
      problems.push(`Space did not read on (count ${first.bubble.count})`),
    );
    await sleep(300);
    await page.keyboard.press('Escape');
    await until(async () => (await count()) === `3 / ${of}`, 'Escape to read on', 3000).catch(async () =>
      problems.push(`Escape did not read on (count ${await count()})`),
    );
    await sleep(400);
    const placed = await seat.read();

    // Dragged by its head
    const head = await page.locator(ui.bubbleHead).boundingBox();
    const by = { x: -220, y: 140 };
    await page.mouse.move(head.x + 30, head.y + head.height / 2);
    await page.mouse.down();
    await page.mouse.move(head.x + 30 + by.x, head.y + head.height / 2 + by.y, { steps: 8 });
    await page.mouse.up();
    await sleep(300);
    const dragged = await seat.read();
    await seat.shot('dragged');
    const dx = dragged.bubble.box.x - placed.bubble.box.x;
    const dy = dragged.bubble.box.y - placed.bubble.box.y;
    if (!dragged.bubble.moved) problems.push('dragged by its head, the bubble is not marked moved');
    if (Math.abs(dx - by.x) > 6 || Math.abs(dy - by.y) > 6) {
      problems.push(`dragged by ${by.x},${by.y}, the bubble moved by ${dx},${dy}`);
    }
    if (dragged.bubble.count !== placed.bubble.count) problems.push('dragging the bubble read on');
    await sleep(800);
    const still = await seat.read();
    if (still.bubble.box.x !== dragged.bubble.box.x || still.bubble.box.y !== dragged.bubble.box.y) {
      problems.push('the bubble did not stay where it was put');
    }

    // Folded and unfolded
    await page.locator(ui.bubbleFold).click();
    await sleep(300);
    const folded = await seat.read();
    const bodyShown = () =>
      page
        .locator(ui.bubbleBody)
        .boundingBox()
        .then((b) => Boolean(b && b.height > 0));
    await seat.shot('folded');
    if (!folded.bubble.collapsed) problems.push('the fold button did not fold the bubble (no data-collapsed)');
    if (await bodyShown()) problems.push("folded, the bubble still shows its message's text");
    if (folded.bubble.box.h >= dragged.bubble.box.h) problems.push('folded, the bubble is no smaller');
    await page.locator(ui.bubbleFold).click();
    await sleep(300);
    const unfolded = await seat.read();
    if (unfolded.bubble.collapsed || !(await bodyShown())) problems.push('the fold button did not unfold the bubble');
    if (unfolded.bubble.box.x !== dragged.bubble.box.x || unfolded.bubble.box.y !== dragged.bubble.box.y) {
      problems.push('folding and unfolding moved the bubble');
    }

    // Folded, Space and Escape leave it be (the keys read on only a message that shows)
    // (the fold button keeps the focus after a click, and Space on it would press it: the key goes to the page)
    await page.locator(ui.bubbleFold).click();
    await page.evaluate(() => document.activeElement?.blur());
    await sleep(200);
    await page.keyboard.press(' ');
    await sleep(400);
    const keyed = await seat.read();
    if (keyed.bubble?.text !== placed.bubble.text) problems.push('Space read on a folded message');
    if (!keyed.bubble?.collapsed) problems.push('Space unfolded a folded message');
    await page.locator(ui.bubbleFold).click();
    await sleep(200);

    // Moved, then read on: the next message is where the page puts it
    const next = await nextMessage(seat, placed.bubble.text, () => page.keyboard.press(' '));
    await seat.shot('next-message');
    if (next.bubble.collapsed) problems.push('the next message is folded');
    if (next.bubble.moved) problems.push('the next message is still marked moved');
    if (next.bubble.box.x === dragged.bubble.box.x && next.bubble.box.y === dragged.bubble.box.y) {
      problems.push('the next message is where the last one was dragged to');
    }

    // Folded and moved while the step is answered, then answered: the step's next message is whole again, in its
    // own place
    const { stopped } = await follow(seat, problems, {
      opponentName: game.info?.opponentName,
      stop: (st) => st.bubble?.placed && !st.next && !st.choices.length && st.nextOutlines.length > 0 && !st.info,
    });
    if (!stopped) throw new Error('tutorial 1 ended without a message over a step to answer');
    const answering = await seat.read();
    await page.locator(ui.bubbleFold).click();
    const at = await page.locator(ui.bubbleHead).boundingBox();
    await page.mouse.move(at.x + 30, at.y + at.height / 2);
    await page.mouse.down();
    await page.mouse.move(at.x + 30 - 150, at.y + at.height / 2 + 60, { steps: 6 });
    await page.mouse.up();
    await sleep(300);
    const put = await seat.read();
    await seat.shot('answering-folded');
    if (!put.bubble.collapsed || !put.bubble.moved)
      problems.push('the bubble over a step to answer did not fold and move');
    const target = await page.evaluate(whereToClick, { outline: put.nextOutlines[0], bubble: ui.bubble });
    const later = await nextMessage(seat, answering.bubble.text, async () => {
      await page.mouse.click(target.x, target.y);
      await page.mouse.move(2, 2);
    });
    await seat.shot('answered-next-message');
    if (later.bubble.collapsed) problems.push(`the message after the answer is folded: "${short(later.bubble.text)}"`);
    if (later.bubble.moved) problems.push(`the message after the answer is still marked moved`);
    if (later.bubble.box.x === put.bubble.box.x && later.bubble.box.y === put.bubble.box.y) {
      problems.push('the message after the answer is where the last one was dragged to');
    }
    table.note = `count ${first.bubble.count} → ${next.bubble.count}, dragged ${dx},${dy}`;
  },

  /**
   * The scripted game's menu: "Start again" asks in the page's own dialog; Cancel changes nothing, OK starts a new
   * game at its first message. "Leave" asks, then the page goes to the Game Hall.
   */
  async menu(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    const game = await startTutorial(who, TUTORIAL_1);
    table.gameId = game.gameId;
    await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
    await seat.firstMessage();
    if (!(await page.locator(ui.menu).isVisible())) throw new Error('no scenario menu');
    await page.keyboard.press(' ');
    await sleep(400);
    const before = await seat.read();

    await page.locator(ui.menuButton('restart')).click();
    const question = await until(
      () => page.locator(ui.confirmText).textContent({ timeout: 500 }),
      'the question in the page’s own dialog',
      3000,
    );
    await seat.shot('restart-asked');
    if (!/start/i.test(question)) problems.push(`"Start again" asks: "${question}"`);
    await page.locator(ui.confirmButton('cancel')).click();
    await sleep(800);
    const cancelled = await seat.read();
    if (await page.locator(ui.confirm).count()) problems.push('Cancel left the dialog open');
    if (seat.gameId() !== game.gameId) problems.push(`Cancel went to another game (${seat.gameId()})`);
    if (cancelled.bubble?.text !== before.bubble?.text || cancelled.bubble?.count !== before.bubble?.count) {
      problems.push(`Cancel changed the message: ${cancelled.bubble?.count} "${short(cancelled.bubble?.text)}"`);
    }

    await page.locator(ui.menuButton('restart')).click();
    await page.locator(ui.confirm).waitFor({ timeout: 3000 });
    await page.locator(ui.confirmButton('ok')).click();
    const newId = await until(
      async () => (seat.gameId() && seat.gameId() !== game.gameId ? seat.gameId() : null),
      'a new game after OK',
      15000,
    );
    table.gameId = newId;
    const restarted = await seat.firstMessage();
    await seat.shot('restarted');
    if (!/^1 \//.test(restarted.bubble.count)) {
      problems.push(`the new game opens at message ${restarted.bubble.count}, not the first`);
    }
    if (restarted.bubble.text === before.bubble.text)
      problems.push('the new game shows the message the old one was at');

    await page.locator(ui.menuButton('leave')).click();
    const leaving = await until(
      () => page.locator(ui.confirmText).textContent({ timeout: 500 }),
      'the question before leaving',
      3000,
    );
    await seat.shot('leave-asked');
    if (!/leave/i.test(leaving)) problems.push(`"Leave" asks: "${leaving}"`);
    await page.locator(ui.confirmButton('ok')).click();
    await page.waitForURL(/\/gemp-lotr\/hall\.html/, { timeout: 15000 }).catch(() => undefined);
    if (!/\/gemp-lotr\/hall\.html/.test(page.url())) problems.push(`after Leave the page is at ${page.url()}`);
    table.note = `game ${game.gameId} → ${newId}, then the hall`;
    // The hall's own errors are not the scripted game's
    seat.errors = seat.errors.filter((error) => !/hall/i.test(error));
  },

  /** A window under 1000×600 shows the "bigger screen" panel; "Carry on anyway" takes it away. */
  async small(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    const game = await startTutorial(who, TUTORIAL_1);
    table.gameId = game.gameId;
    await page.setViewportSize({ width: 1000, height: 600 });
    await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
    await seat.firstMessage();
    if ((await seat.read()).panel) problems.push(`at 1000×600 a panel is up: ${(await seat.read()).panel}`);
    for (const size of [
      { width: 999, height: 700 },
      { width: 1200, height: 599 },
      { width: 900, height: 550 },
    ]) {
      await page.setViewportSize(size);
      await sleep(400);
      const st = await seat.read();
      if (st.panel !== 'small') problems.push(`at ${size.width}×${size.height} no "small" panel (${st.panel})`);
    }
    await seat.shot('small');
    const panel = await seat.read();
    if (!/bigger screen/i.test(panel.heading)) problems.push(`the panel says "${panel.heading}"`);
    if (!(await page.locator(ui.panelButton('Back to the Game Hall')).isVisible())) {
      problems.push('the panel has no way back to the Game Hall');
    }
    await page.locator(ui.panelButton('Carry on anyway')).click();
    await sleep(400);
    const carried = await seat.read();
    await seat.shot('carry-on');
    if (carried.panel) problems.push(`"Carry on anyway" left the "${carried.panel}" panel up`);
    if (!carried.bubble) problems.push('after "Carry on anyway" no message is up');
    table.note = `"${panel.heading}"`;
  },

  /** The modern theme on a phone: only the "cannot be played on mobile devices" panel, with its way back. */
  async phone(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    const game = await startTutorial(who, TUTORIAL_1);
    table.gameId = game.gameId;
    await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
    await until(async () => (await seat.read()).panel, 'a panel', 20000).catch(() => undefined);
    await sleep(1000);
    const st = await seat.read();
    await seat.shot('phone');
    if (st.panel !== 'phone') problems.push(`no "phone" panel (${st.panel})`);
    if (!/cannot be played on mobile devices/i.test(st.panelText)) problems.push(`the panel says "${st.panelText}"`);
    if (st.bubble) problems.push(`a message is up behind it: "${short(st.bubble.text)}"`);
    if (st.menu) problems.push('the scenario menu is shown');
    if (st.scenarioName) problems.push(`the page names the scripted opponent "~Scenario": ${st.scenarioName}`);
    if (st.nextOutlines.length || st.outlines.length) problems.push('outlines are drawn');
    const back = page.locator(ui.panelButton('Back to the Game Hall'));
    if (!(await back.isVisible())) {
      problems.push('the panel has no way back to the Game Hall');
    } else {
      await back.tap();
      await page.waitForURL(/\/gemp-lotr\/hall\.html/, { timeout: 15000 }).catch(() => undefined);
      if (!/\/gemp-lotr\/hall\.html/.test(page.url())) problems.push(`its way back led to ${page.url()}`);
    }
    seat.errors = seat.errors.filter((error) => !/hall/i.test(error));
    table.note = `"${st.heading}"`;
  },

  /**
   * The replay of a finished tutorial 1 game (this run's t1 game, or one played here): its messages come up in turn
   * as it plays, with no buttons, and there is no scenario menu.
   */
  async replay(table, problems) {
    const { seat, who } = table;
    const page = seat.page;
    let replayId = finished[table.theme];
    let from = 'the t1 game of this run';
    if (!replayId) {
      await playThrough(table, problems, TUTORIAL_1);
      from = 'a tutorial 1 game played here';
      replayId = (await replayIdFromPanel(seat)).replayId;
      if (!replayId) {
        // (t1 says when the end panel lacks the link; the server's thanks in the chat names the same replay)
        replayId = await replayIdFromChat(seat);
        from += ', its replay named in the chat';
      }
      if (!replayId) throw new Error('neither the end panel nor the chat names the replay');
    }
    if (!replayId.startsWith(`${seat.player}$`)) problems.push(`the replay id is ${replayId}`);
    await page.goto(`${server}${ui.path}?replayId=${encodeURIComponent(replayId)}`);
    await page.locator(ui.replayPlay).waitFor({ timeout: 20000 });
    await sleep(1000);
    const texts = [];
    let shots = 0;
    const look = async () => {
      const st = await seat.read();
      if (st.menu && !problems.includes('the replay shows the scenario menu')) {
        problems.push('the replay shows the scenario menu');
      }
      if (st.bubble) {
        if (st.bubble.foot || st.bubble.buttons) {
          const said = `a message of the replay has buttons: "${short(st.bubble.text)}"`;
          if (!problems.includes(said)) problems.push(said);
        }
        if (st.bubble.text && st.bubble.text !== texts.at(-1)) {
          texts.push(st.bubble.text);
          if (st.bubble.placed && shots < 3) {
            shots++;
            await seat.shot('replay-message');
          }
        }
      }
      if (st.panel) {
        const said = `the replay shows the "${st.panel}" panel`;
        if (!problems.includes(said)) problems.push(said);
      }
    };
    await look();
    await page.locator(ui.replayPlay).click();
    const deadline = Date.now() + 90000;
    while (Date.now() < deadline && texts.length < 4) {
      await look();
      await sleep(150);
    }
    await seat.shot('replay');
    if (texts.length < 2)
      problems.push(`the replay showed ${texts.length} message(s) as it played, not one after another`);
    table.note = `${from}: ${texts.length} messages in turn, first "${short(texts[0], 40)}"`;
  },
};

/** Does `act()` and waits for a message other than `text` to be up and placed; returns the page then. */
async function nextMessage(seat, text, act) {
  await act();
  await until(
    async () => {
      const st = await seat.read();
      return st.bubble?.placed && st.bubble.text !== text ? st : null;
    },
    'the next message',
    10000,
  );
  await sleep(400);
  return seat.read();
}

/** Which themes each check runs in: the phone layout is the modern theme's only. */
const THEMES_OF = { phone: ['modern'] };

async function run(name, theme) {
  const check = `${name}-${theme}`;
  const problems = [];
  const started = Date.now();
  const player = pairOf(0).player;
  const table = { theme, note: '' };
  try {
    table.who = await session(server, player, password);
    table.seat = await openSeat(check, table.who, player, theme, name === 'phone' ? DEVICES.phone : DEVICES.pc);
    await CHECKS[name](table, problems);
  } catch (error) {
    problems.push(error.message.split('\n')[0]);
    await table.seat?.shot('failed');
  }
  if (table.seat) {
    problems.push(...new Set(table.seat.errors));
    await table.seat.page.context().close();
  }
  if (table.gameId && table.who) {
    await table.who.post(`/game/${table.gameId}/concede`, {}).catch(() => undefined);
  }
  const seconds = Math.round((Date.now() - started) / 1000);
  const lines = [
    `${problems.length ? '  ✗' : '  ✓'} ${name} (${theme}, ${seconds}s)${table.note ? ` ${table.note}` : ''}`,
    ...problems.map((problem) => `      ${problem}`),
  ];
  console.log(lines.join('\n'));
  fs.writeFileSync(
    path.join(OUT, `${check}.txt`),
    [...lines, '', 'Moves:', ...(table.seat?.moves ?? [])].join('\n') + '\n',
  );
  return { ok: problems.length === 0, lines };
}

const names = Object.keys(CHECKS).filter((name) => ONLY.length === 0 || ONLY.includes(name));
const unknown = ONLY.filter((name) => !CHECKS[name]);
if (unknown.length) throw new Error(`--only ${unknown.join(',')}: the checks are ${Object.keys(CHECKS).join(', ')}`);
fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(OUT, { recursive: true });
const runs = names.flatMap((name) =>
  THEMES.filter((theme) => (THEMES_OF[name] ?? THEMES).includes(theme)).map((theme) => [name, theme]),
);
browser = await launchBrowser();
const results = [];
const began = Date.now();
try {
  for (const [name, theme] of runs) results.push(await run(name, theme));
} finally {
  await browser.close();
}
const ok = results.filter((result) => result.ok).length;
const summary = `${ok} of ${runs.length} tutorial checks passed on the ${KIND} page in ${Math.round((Date.now() - began) / 1000)}s. Screenshots: ${OUT}`;
console.log(summary);
fs.writeFileSync(
  path.join(OUT, 'summary.txt'),
  [...results.flatMap((result) => result.lines), summary].join('\n') + '\n',
);
process.exitCode = ok === runs.length ? 0 : 1;
