// The sandbox on the Angular game page: the table changed by hand (the edit bar, the open spots, the chooser, the
// search window, the card menu), Start and Stop, taking back across edits, Reset and a sandbox's end. A real browser
// plays the default sandbox (`sandbox`, which ships with the server) as GamePlayer0, against a game check server kept
// up (as the tutorial check). Each check starts a game of its own; what it changes is asserted both on the page and in
// the server's own view of the table (`GET scenarios/sandbox/state`). See "The sandbox checks" in README.md.
//
//   npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
//   npm run check:game:sandbox                                     every check, in both themes
//   npm run check:game:sandbox -- --only menu,play --theme modern --out report-sandbox
//   npm run check:game:down

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { launchBrowser } from '../browser.mjs';
import { session } from '../local-server/seed.mjs';
import { DEVICES } from './device-profiles.mjs';
import { pairOf } from './players.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const { server, password } = JSON.parse(fs.readFileSync(path.join(CHECK_DIR, '.local-server/game/login.json'), 'utf8'));

const argument = (name, fallback) => {
  const at = process.argv.indexOf(`--${name}`);
  return at < 0 ? fallback : process.argv[at + 1];
};
const OUT = path.resolve(CHECK_DIR, argument('out', 'report-sandbox'));
const ONLY = argument('only', '').split(',').filter(Boolean);
const THEMES = argument('theme', 'classic,modern').split(',').filter(Boolean);
const VERBOSE = process.argv.includes('--verbose');

/** The default sandbox: Frodo and the Ring on each side, the Fellowship block's sites, every tool. */
const SANDBOX = 'sandbox';
const LEGOLAS = '1_50';
const ARMOR = '1_92';
const BOROMIR = '1_97';
const RUNNER = '1_178';
/** Where a player's own scenarios are kept (the tools check writes a sandbox of its own there, and deletes it). */
const DATA = process.env.GEMP_SCENARIOS ?? '/etc/gemp-lotr/scenarios';

/** The page's DOM contract for the edit controls (ui/game/table-edit, the game page, the scenario's menu). */
const ui = {
  path: '/gemp-lotr/ng/game',
  game: 'gemp-game',
  bar: 'gd-edit-bar',
  barMode: 'gd-edit-bar .gd-edit-bar__mode',
  barButton: (name) => `gd-edit-bar button[data-edit-bar="${name}"]`,
  spot: (seat, zone) => `gd-open-spot button.gd-open-spot[data-seat="${seat}"][data-zone="${zone}"]`,
  spots: 'gd-open-spot button.gd-open-spot',
  chooser: '.gd-edit-chooser',
  chooserItem: (kind, blueprint) =>
    `.gd-edit-chooser [data-chooser="${kind}"]${blueprint ? `[data-blueprint="${blueprint}"]` : ''}`,
  search: 'dialog[data-dialog="edit-search"][open]',
  searchName: 'dialog[data-dialog="edit-search"] .gd-field[data-field="name"] input',
  searchCard: (blueprint) =>
    `dialog[data-dialog="edit-search"] .gd-edit-search__cards button.gd-card[data-blueprint="${blueprint}"]`,
  searchCards: 'dialog[data-dialog="edit-search"] .gd-edit-search__cards button.gd-card',
  searchBound: 'dialog[data-dialog="edit-search"] .gd-edit-search__bound',
  searchClear: 'dialog[data-dialog="edit-search"] button[data-action="clear"]',
  searchClose: 'dialog[data-dialog="edit-search"] .gd-dialog__close',
  menu: '.gd-edit-menu:not([data-submenu])',
  menuEntry: (name) => `.gd-edit-menu:not([data-submenu]) [data-menu="${name}"]`,
  submenu: (name) => `.gd-edit-menu[data-submenu="${name}"]`,
  problem: '.gd-edit-problem',
  pickBox: 'gd-table-edit .gd-author-pickbox',
  card: (cardId) => `.gd-game__table [data-card-id="${cardId}"]`,
  alert: '[data-game="alert"]',
  decisionButtons: 'button[data-decision-button]',
  cardInfo: 'dialog.gd-card-info[open]',
  confirm: 'dialog.gd-dialog[open][data-dialog="confirm"]',
  confirmButton: (action) => `dialog.gd-dialog[open][data-dialog="confirm"] button[data-action="${action}"]`,
  panel: '.gd-scenario-panel',
  panelButton: (label) => `.gd-scenario-panel button[data-panel-button="${label}"]`,
  menuRestart: '[data-scenario-menu="restart"]',
  tab: (name) => `[data-tab="${name}"]`,
  revert: '[data-button="undoGame"]',
  undoPoint: '[data-undo="point"]',
  undoLabel: '[data-undo="label"]',
  undoGo: 'button[data-undo="go"]',
  // The piles, the path and the counters (u2)
  stat: (name) => `gd-game-state [data-stat="${name}"]`,
  statsToggle: (index) => `gd-game-state [data-stats-toggle="${index}"]`,
  players: 'gd-game-state .gd-game-player',
  pileWindow: (pile, seat) =>
    `dialog[data-dialog="pile"][open] .gd-game-pile[data-pile="${pile}"]${seat ? `[data-seat="${seat}"]` : ''}`,
  pileCards: (pile, seat) =>
    `dialog[data-dialog="pile"][open] .gd-game-pile[data-pile="${pile}"][data-seat="${seat}"] button.gd-card`,
  pileSpot: (pile, seat, zone) =>
    `dialog[data-dialog="pile"][open] .gd-game-pile[data-pile="${pile}"][data-seat="${seat}"] gd-open-spot button[data-zone="${zone}"]`,
  pileClose: (pile, seat) =>
    `dialog[data-dialog="pile"][open]:has(.gd-game-pile[data-pile="${pile}"][data-seat="${seat}"]) .gd-dialog__close`,
  pileCard: (cardId) => `dialog[data-dialog="pile"][open] button.gd-card[data-card-id="${cardId}"]`,
  siteSpot: 'gd-game-path gd-site-spot button.gd-open-spot',
  siteChooser: '.gd-edit-chooser[data-site-chooser]',
  siteItem: (kind, extra = '') => `.gd-edit-chooser[data-site-chooser] [data-site-chooser="${kind}"]${extra}`,
  pathCard: (cardId) => `gd-game-path [data-card-id="${cardId}"]`,
  pathCards: 'gd-game-path button.gd-card',
  siteAction: (action, seat) => `.gd-edit-menu:not([data-submenu]) [data-site-action="${action}"][data-seat="${seat}"]`,
  editCounter: (name) => `gd-game-state button.gd-edit-counter[data-edit-counter="${name}"]`,
  counterBox: (name) => `.gd-edit-counter__box[data-counter="${name}"]`,
  burdens: 'gd-game-state [data-game="burdens"]',
};

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** Waits until `read()` gives something (not null, undefined or false), and returns it; throws saying `what` if not. */
async function until(read, what, max = 15000) {
  const deadline = Date.now() + max;
  for (;;) {
    const value = await Promise.resolve()
      .then(read)
      .catch(() => undefined);
    if (value !== null && value !== undefined && value !== false) return value;
    if (Date.now() > deadline) throw new Error(`waited ${max / 1000}s for ${what}`);
    await sleep(100);
  }
}

/** The person's page on a sandbox game, and what went wrong on it by itself. */
class Seat {
  constructor(check, page, who) {
    this.check = check;
    this.page = page;
    this.who = who;
    this.errors = [];
    this.moves = [];
    this.shots = 0;
    /** Sandbox edits the server is expected to refuse (a check that makes a refusal says so). */
    this.refusals = 0;
    /** Answers of the game's old channel after such a refusal took the game back (the markers check says so). */
    this.resyncs = 0;
    page.on('pageerror', (error) => this.errors.push(`page error: ${error.message.split('\n')[0]}`));
    page.on('console', (message) => {
      if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) {
        this.errors.push(`console error: ${message.text().slice(0, 200)}`);
      }
    });
    page.on('response', (response) => {
      const { pathname } = new URL(response.url());
      if (!pathname.includes('/gemp-lotr-server/') || response.status() < 400) return;
      if (pathname.endsWith('/scenarios/sandbox/edit') && response.status() === 400 && this.refusals > 0) {
        this.refusals--;
        return;
      }
      // (an edit refused once the table had begun to change takes the game back: the page's old channel is gone)
      if (pathname.includes('/game/') && response.status() === 409 && this.resyncs > 0) {
        this.resyncs--;
        return;
      }
      this.errors.push(`HTTP ${response.status()} from ${pathname}`);
    });
    page.on('dialog', (dialog) => {
      this.errors.push(`the browser's own dialog: ${dialog.message()}`);
      dialog.accept();
    });
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

  /** The table as the server has it for the person (GET scenarios/sandbox/state). */
  async state() {
    return (await this.who.get(`/scenarios/sandbox/state?gameId=${this.gameId()}`)).json();
  }

  async card(alias) {
    return (await this.state()).cards.find((card) => card.alias === alias) ?? null;
  }

  /** Starts a game of the default sandbox and opens the page on it, in edit mode. */
  async open() {
    const started = await (await this.who.post('/scenarios/start', { id: SANDBOX })).json();
    if (!started.gameId) throw new Error(`scenarios/start ${SANDBOX} answered ${JSON.stringify(started)}`);
    this.log(`started sandbox game ${started.gameId}`);
    await this.page.goto(`${server}${ui.path}?gameId=${started.gameId}`);
    await this.editing('edit');
  }

  /** Waits for the edit bar to say the mode, and the page to be at rest. */
  async editing(mode) {
    await until(
      async () => (await this.page.locator(ui.bar).getAttribute('data-mode')) === mode,
      `the edit bar in ${mode} mode`,
      20000,
    );
    await until(
      async () => (await this.page.locator(ui.bar).getAttribute('data-busy')) === null,
      'the edit bar at rest',
    );
    await sleep(300);
  }

  /** Right-clicks a card on the table and waits for its menu. */
  async menuOf(cardId) {
    await this.page.locator(ui.card(cardId)).click({ button: 'right' });
    await this.page.locator(ui.menu).waitFor();
    await until(
      async () => (await this.page.locator(ui.menu).getAttribute('data-placing')) === null,
      'the menu placed',
    );
  }

  /** Opens a submenu of the open card menu (the pointer onto its entry). */
  async submenu(name) {
    await this.page.locator(ui.menuEntry(name)).hover();
    await this.page.locator(ui.submenu(name)).waitFor();
    return this.page.locator(ui.submenu(name));
  }

  /** Does something that changes the table, and waits until the server's state passes `done`. */
  async edit(what, act, done) {
    this.log(what);
    await act();
    return until(async () => {
      const state = await this.state();
      return done(state) ? state : null;
    }, `the server's table after: ${what}`);
  }

  /** Opens the search window from an open spot, types a name, and waits for that card among the results. */
  async searchFrom(seat, zone, name, blueprint) {
    await this.page.locator(ui.spot(seat, zone)).click();
    await this.page.locator(ui.chooserItem('search')).click();
    await this.page.locator(ui.search).waitFor();
    const field = this.page.locator(ui.searchName);
    await field.fill(name);
    await field.dispatchEvent('change');
    await this.page.locator(ui.searchCard(blueprint)).waitFor();
  }
}

/** The cards added by hand, as the server has them: alias → zone. */
const addedZones = (state) =>
  Object.fromEntries(state.cards.filter((card) => card.added).map((card) => [card.alias, card.zone]));

/** A seat's pile as the server has it, in its order (a pile's top first, a hand as it lies): the cards' ids. */
const pileOf = (state, zone, seat) =>
  state.cards
    .filter((card) => card.zone === zone && card.owner === seat)
    .sort((a, b) => (a.at ?? 1e9) - (b.at ?? 1e9))
    .map((card) => String(card.cardId));

/** The board's zone of each pile window (`data-pile`), and the state panel's counter of it (`data-stat`, less the index). */
const PILES = [
  { pile: 'deck', zone: 'deck' },
  { pile: 'hand', zone: 'hand' },
  { pile: 'discard', zone: 'discard' },
  { pile: 'deadPile', zone: 'deadPile' },
  { pile: 'removedPile', zone: 'removed' },
  { pile: 'adventureDeck', zone: 'adventureDeck' },
];

/** Edits made straight through the server, to set a check's table up (not what it checks); the page is read again. */
async function setUp(seat, ops) {
  const answer = await (
    await seat.who.post('/scenarios/sandbox/edit', { gameId: seat.gameId(), ops: JSON.stringify(ops) })
  ).json();
  if (!answer.ok) throw new Error(`setting the table up: ${JSON.stringify(answer)}`);
  seat.log(`set up: ${ops.map((op) => `${op.op} ${op.blueprint ?? ''} ${op.to ?? ''}`.trim()).join(', ')}`);
  await seat.page.reload();
  await seat.editing('edit');
}

/** The state panel's row of each seat (the person plays freeps, the panel lists the players in the game's order). */
async function seatRows(seat) {
  const names = await seat.page.locator(ui.players).allTextContents();
  const mine = names.findIndex((text) => text.includes(pairOf(0).player));
  if (mine < 0) throw new Error(`no row of the state panel is the person's: ${names.join(' | ')}`);
  return { freeps: mine, shadow: 1 - mine };
}

/** The ids of the cards a pile window shows, in its order. */
const windowIds = (seat, pile, owner) =>
  seat.page.locator(ui.pileCards(pile, owner)).evaluateAll((cards) => cards.map((card) => card.dataset.cardId));

/** Waits until a pile window shows the pile as the server has it; says what it shows when it doesn't. */
async function windowShows(seat, pile, owner, wanted, problems, what) {
  await until(
    async () => (await windowIds(seat, pile, owner)).join() === wanted.join(),
    `${what}: the window showing ${wanted.join(',')}`,
    8000,
  ).catch(async () =>
    problems.push(
      `${what}: the ${owner} ${pile} window shows ${(await windowIds(seat, pile, owner)).join(',')}, not ${wanted.join(',')}`,
    ),
  );
}

/** Opens a pile's window from its counter in the state panel. */
async function openPile(seat, rows, pile, owner) {
  await seat.page.locator(ui.stat(`${pile}${rows[owner]}`)).click();
  await seat.page.locator(ui.pileWindow(pile, owner)).waitFor({ timeout: 5000 });
}

/** Right-clicks a card (any locator) and waits for its menu, placed. */
async function menuAt(seat, locator) {
  await locator.click({ button: 'right' });
  await seat.page.locator(ui.menu).waitFor();
  await until(async () => (await seat.page.locator(ui.menu).getAttribute('data-placing')) === null, 'the menu placed');
}

/** The entries of the open card menu (its `data-menu` and `data-site-action seat`). */
const menuEntries = (seat) =>
  seat.page
    .locator(`${ui.menu} > button`)
    .evaluateAll((items) =>
      items.map((item) =>
        item.dataset.siteAction ? `${item.dataset.siteAction} ${item.dataset.seat}` : item.dataset.menu,
      ),
    );

/** From an open spot (any locator): Search…, a name typed, and the card found clicked. */
async function searchAt(seat, spot, name, blueprint) {
  await spot.click();
  await seat.page.locator(ui.chooserItem('search')).click();
  await seat.page.locator(ui.search).waitFor();
  const field = seat.page.locator(`${ui.search} .gd-field[data-field="name"] input`);
  await field.fill(name);
  await field.dispatchEvent('change');
  await seat.page.locator(`${ui.search} .gd-edit-search__cards button.gd-card[data-blueprint="${blueprint}"]`).click();
}

/** The path's sites as the server has them: number → alias. */
const pathOf = (state) => Object.fromEntries(state.path.map((site) => [site.number, site.alias]));

/** The path's open spot: its chooser, From adventure deck…, a seat's deck, and one of its sites. */
async function siteFromDeck(seat, seatKey, alias) {
  await seat.page.locator(ui.siteItem('decks')).click();
  await seat.page.locator(ui.siteItem('deck', `[data-seat="${seatKey}"]`)).click();
  await seat.page.locator(ui.siteItem('site', `[data-alias="${alias}"]`)).click();
}

/** A counter's box: opened from its number, a number typed and set (or Enter). */
async function setCounter(seat, name, value, how = 'set') {
  const { page } = seat;
  if ((await page.locator(ui.counterBox(name)).count()) === 0) await page.locator(ui.editCounter(name)).click();
  const field = page.locator(`${ui.counterBox(name)} input[data-field="set"]`);
  await field.fill(String(value));
  if (how === 'enter') await field.press('Enter');
  else await page.locator(`${ui.counterBox(name)} button[data-action="set"]`).click();
}

const CHECKS = {
  /** Edit mode as the sandbox opens: no decision, the bar, an open spot in every row, the card menu by the keyboard. */
  async open(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await seat.shot('editing');
    if ((await page.locator(ui.game).getAttribute('data-edit')) !== 'edit')
      problems.push('the page is not in edit mode');
    if ((await page.locator(ui.barMode).textContent()).trim() !== 'Editing')
      problems.push('the bar does not say Editing');
    if (!(await page.locator(ui.barButton('start')).isEnabled())) problems.push('Start is not offered');
    if ((await page.locator(ui.barButton('stop')).count()) > 0) problems.push('Stop is offered in edit mode');
    if ((await page.locator(ui.menuRestart).count()) > 0) problems.push('the menu has Start again beside Reset');
    const alert = (await page.locator(ui.alert).textContent()).trim();
    if (alert !== '') problems.push(`edit mode shows the decision: "${alert}"`);
    if ((await page.locator(ui.decisionButtons).count()) > 0) problems.push('edit mode shows the decision buttons');
    const spots = await page
      .locator(ui.spots)
      .evaluateAll((buttons) => buttons.map((button) => `${button.dataset.seat}.${button.dataset.zone}`));
    const wanted = [
      'shadow.support',
      'shadow.characters',
      'shadow.minions',
      'freeps.characters',
      'freeps.support',
      'freeps.hand',
    ];
    if (spots.join() !== wanted.join()) problems.push(`open spots ${spots.join()}, not ${wanted.join()}`);
    // The context-menu key on a focused card opens its menu; arrows move, Escape closes and gives the focus back
    const frodo = await seat.card('frodo');
    await page.locator(ui.card(frodo.cardId)).focus();
    await page.keyboard.press('ContextMenu');
    await page
      .locator(ui.menu)
      .waitFor()
      .catch(() => problems.push('the context-menu key opened no menu'));
    const focused = () =>
      page.evaluate(
        () => document.activeElement?.getAttribute('data-menu') ?? document.activeElement?.getAttribute('data-card-id'),
      );
    if ((await focused()) !== 'info') problems.push(`the menu's first entry is not focused (${await focused()})`);
    await page.keyboard.press('ArrowDown');
    if ((await focused()) !== 'send') problems.push(`Down went to ${await focused()}, not Send to`);
    await page.keyboard.press('ArrowRight');
    const inSubmenu = () =>
      page.evaluate(() => document.activeElement?.closest('[data-submenu]')?.getAttribute('data-submenu'));
    await until(async () => (await inSubmenu()) === 'send', 'Right into the Send to submenu').catch(() =>
      problems.push('Right did not go into the submenu'),
    );
    await page.keyboard.press('ArrowLeft');
    await until(
      async () => (await page.locator(ui.submenu('send')).count()) === 0 && (await focused()) === 'send',
      'Left back',
    );
    await page.keyboard.press('Escape');
    await until(async () => (await page.locator(ui.menu).count()) === 0, 'the menu closed by Escape');
    if ((await focused()) !== String(frodo.cardId)) problems.push('Escape did not give the focus back to the card');
    // Info shows the card's details; the menu goes
    await seat.menuOf(frodo.cardId);
    await seat.shot('menu');
    await page.locator(ui.menuEntry('info')).click();
    await page
      .locator(ui.cardInfo)
      .waitFor()
      .catch(() => problems.push('Info showed no card information'));
    await page.keyboard.press('Escape');
    // A decision answered anyway (a stray key) is not taken: the server's state is still edit mode
    if ((await seat.state()).mode !== 'edit') problems.push('the game left edit mode by itself');
  },

  /** Adding: by a search with a filter (remembered, cleared), from Recent, from In this scenario. */
  async add(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await seat.searchFrom('freeps', 'characters', 'greenleaf', LEGOLAS);
    const bound = (await page.locator(ui.searchBound).textContent()).trim();
    if (bound !== 'Companions') problems.push(`the characters' search is bound by "${bound}"`);
    await seat.shot('search');
    let state = await seat.edit(
      'Legolas from the search into the characters',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'characters',
    );
    await until(async () => (await page.locator(ui.search).count()) === 0, 'the search window closed');
    const legolas = state.cards.find((card) => card.alias === 'legolas');
    await page
      .locator(ui.card(legolas.cardId))
      .waitFor()
      .catch(() => problems.push('the added card is not on the table'));
    // Recent: the card used last, at the head, where it fits (the hand)
    await page.locator(ui.spot('freeps', 'hand')).click();
    await page.locator(ui.chooser).waitFor();
    await seat.shot('chooser');
    const recent = await page
      .locator('.gd-edit-chooser [data-chooser="recent"]')
      .evaluateAll((items) => items.map((item) => item.dataset.blueprint));
    if (recent[0] !== LEGOLAS) problems.push(`Recent starts with ${recent[0]}, not Legolas`);
    state = await seat.edit(
      'Legolas from Recent into the hand',
      () => page.locator(ui.chooserItem('recent', LEGOLAS)).click(),
      (now) => addedZones(now).legolas2 === 'hand',
    );
    // In this scenario: one of each card in the game, into the other seat's characters
    await page.locator(ui.spot('shadow', 'characters')).click();
    await page.locator(ui.chooserItem('scenario', LEGOLAS)).waitFor();
    const inGame = await page
      .locator('.gd-edit-chooser [data-chooser="scenario"]')
      .evaluateAll((items) => items.map((item) => item.textContent.trim()));
    if (!inGame.includes('Legolas, Greenleaf') || !inGame.includes('Frodo, Son of Drogo')) {
      problems.push(`In this scenario lists ${inGame.join(', ')}`);
    }
    await seat.edit(
      "Legolas from In this scenario into the shadow seat's characters",
      () => page.locator(ui.chooserItem('scenario', LEGOLAS)).click(),
      (now) => now.cards.some((card) => card.added && card.owner === 'shadow' && card.zone === 'characters'),
    );
    // The search remembers its filters (across a reload too), and Clear filters clears them
    await page.reload();
    await seat.editing('edit');
    await page.locator(ui.spot('freeps', 'support')).click();
    await page.locator(ui.chooserItem('search')).click();
    await page.locator(ui.search).waitFor();
    if ((await page.locator(ui.searchName).inputValue()) !== 'greenleaf')
      problems.push('the search forgot its filters');
    const support = (await page.locator(ui.searchBound).textContent()).trim();
    if (support !== 'Support cards') problems.push(`the support's search is bound by "${support}"`);
    await page.locator(ui.searchClear).click();
    await until(async () => (await page.locator(ui.searchName).inputValue()) === '', 'the name cleared');
    await page.locator(ui.searchCards).first().waitFor();
    await seat.shot('search-cleared');
    await page.locator(ui.searchClose).click();
    // Recent is remembered on the device, after a reload
    await page.locator(ui.spot('freeps', 'hand')).click();
    await page.locator(ui.chooser).waitFor();
    if ((await page.locator(ui.chooserItem('recent', LEGOLAS)).count()) === 0) problems.push('Recent forgot Legolas');
    await page.keyboard.press('Escape');
  },

  /** Every entry of the card menu does what it says: on the page and in the server's table. */
  async menu(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await seat.searchFrom('freeps', 'characters', 'greenleaf', LEGOLAS);
    let state = await seat.edit(
      'Legolas',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'characters',
    );
    const legolas = () => state.cards.find((card) => card.alias === 'legolas');
    await seat.searchFrom('freeps', 'support', 'armor', ARMOR);
    state = await seat.edit(
      'Armor into the support area',
      () => page.locator(ui.searchCard(ARMOR)).click(),
      (now) => addedZones(now).armor === 'support',
    );
    const armor = state.cards.find((card) => card.alias === 'armor');

    // Attach: Armor onto Legolas, picked on the table
    await seat.menuOf(armor.cardId);
    await page.locator(ui.menuEntry('attach')).click();
    await until(
      async () => (await page.locator(ui.barMode).textContent()).includes('Attach Armor to'),
      'the bar saying what to pick',
    ).catch(async () => problems.push(`while picking the bar says "${await page.locator(ui.barMode).textContent()}"`));
    await page.locator(ui.card(legolas().cardId)).hover();
    await page
      .locator(ui.pickBox)
      .waitFor()
      .catch(() => problems.push('no box round the card under the pointer'));
    await seat.shot('picking');
    state = await seat.edit(
      'Attach Armor to Legolas',
      () => page.locator(ui.card(legolas().cardId)).click(),
      (now) => now.cards.find((card) => card.alias === 'armor')?.on === 'legolas',
    );

    // Add: wounds by the stepper, a heal, tokens of his culture and another's, one taken off
    await seat.menuOf(legolas().cardId);
    let add = await seat.submenu('add');
    await add.locator('[data-step="up"]').click();
    await seat.shot('add');
    state = await seat.edit(
      'two wounds',
      () => add.locator('[data-add="wound"]').click(),
      (now) => now.cards.find((card) => card.alias === 'legolas').wounds === 2,
    );
    await seat.menuOf(legolas().cardId);
    add = await seat.submenu('add');
    state = await seat.edit(
      'a heal',
      () => add.locator('[data-add="heal"]').click(),
      (now) => now.cards.find((card) => card.alias === 'legolas').wounds === 1,
    );
    const tokens = (now) => now.cards.find((card) => card.alias === 'legolas').cultureTokens;
    await seat.menuOf(legolas().cardId);
    add = await seat.submenu('add');
    const first = await add.locator('.gd-edit-menu__token').first().getAttribute('data-culture');
    if (first !== 'elven') problems.push(`the tokens start with ${first}, not his own culture`);
    await add.locator('input[type="number"]').fill('3');
    await add.locator('input[type="number"]').dispatchEvent('change');
    state = await seat.edit(
      'three Elven tokens',
      () => add.locator('[data-culture="elven"] [data-token="add"]').click(),
      (now) => tokens(now).elven === 3,
    );
    await seat.menuOf(legolas().cardId);
    add = await seat.submenu('add');
    state = await seat.edit(
      'two Sauron tokens',
      async () => {
        await add.locator('[data-step="up"]').click();
        await add.locator('[data-culture="sauron"] [data-token="add"]').click();
      },
      (now) => tokens(now).sauron === 2,
    );
    await seat.menuOf(legolas().cardId);
    add = await seat.submenu('add');
    state = await seat.edit(
      'one Sauron token off',
      () => add.locator('[data-culture="sauron"] [data-token="remove"]').click(),
      (now) => tokens(now).sauron === 1,
    );

    // Modify: strength +2 this turn, then archer until removed; one taken off by its ×
    await seat.menuOf(legolas().cardId);
    let modify = await seat.submenu('modify');
    await modify.locator('select[data-field="what"]').selectOption('strength');
    await modify.locator('input[data-field="amount"]').fill('2');
    await modify.locator('input[data-field="amount"]').dispatchEvent('change');
    await modify.locator('select[data-field="until"]').selectOption('turn');
    await seat.shot('modify');
    const mods = (now) => now.cards.find((card) => card.alias === 'legolas').modifiers;
    state = await seat.edit(
      'Strength +2 this turn',
      () => modify.locator('[data-action="put"]').click(),
      (now) => mods(now).some((m) => m.what === 'strength' && m.amount === 2 && m.until === 'turn'),
    );
    await seat.menuOf(legolas().cardId);
    modify = await seat.submenu('modify');
    await modify.locator('select[data-field="what"]').selectOption('archer');
    if ((await modify.locator('input[data-field="amount"]').count()) > 0) problems.push('Archer asks for a number');
    state = await seat.edit(
      'Archer until removed',
      () => modify.locator('[data-action="put"]').click(),
      (now) => mods(now).some((m) => m.what === 'archer' && m.until === 'removed'),
    );
    await seat.menuOf(legolas().cardId);
    modify = await seat.submenu('modify');
    const listed = await modify.locator('.gd-edit-menu__modifier').count();
    if (listed !== 2) problems.push(`Modify lists ${listed} modifiers on him, not 2`);
    await seat.shot('modifiers');
    const strength = mods(state).find((m) => m.what === 'strength');
    state = await seat.edit(
      'Strength +2 taken off',
      () => modify.locator(`[data-modifier="${strength.id}"] [data-action="unmodify"]`).click(),
      (now) => !mods(now).some((m) => m.what === 'strength'),
    );

    // Copy, Send to, Stack, Delete
    await seat.menuOf(legolas().cardId);
    state = await seat.edit(
      'Copy',
      () => page.locator(ui.menuEntry('copy')).click(),
      (now) => addedZones(now).legolas2 === 'characters',
    );
    const copy = state.cards.find((card) => card.alias === 'legolas2');
    await seat.menuOf(copy.cardId);
    await seat.submenu('send');
    state = await seat.edit(
      'Send the copy to the discard pile',
      () => page.locator(`${ui.submenu('send')} [data-zone="discard"]`).click(),
      (now) => addedZones(now).legolas2 === 'discard',
    );
    await seat.searchFrom('shadow', 'minions', 'goblin runner', '1_178');
    state = await seat.edit(
      'a Goblin Runner among the minions',
      () => page.locator(ui.searchCard('1_178')).click(),
      (now) => addedZones(now).goblinrunner === 'minions',
    );
    const runner = state.cards.find((card) => card.alias === 'goblinrunner');
    await seat.menuOf(runner.cardId);
    await page.locator(ui.menuEntry('stack')).click();
    state = await seat.edit(
      'Stack the Goblin Runner on Legolas',
      () => page.locator(ui.card(legolas().cardId)).click(),
      (now) => now.cards.find((card) => card.alias === 'goblinrunner')?.zone === 'stacked',
    );
    await seat.shot('stacked');
    // Delete: Legolas out of the game; what he bore and what lay on him go to the discard pile (es-notes 3)
    await page.mouse.move(5, 5);
    await seat.menuOf(legolas().cardId);
    const gone = legolas().cardId;
    state = await seat.edit(
      'Delete Legolas',
      () => page.locator(ui.menuEntry('delete')).click(),
      (now) => !now.cards.some((card) => card.alias === 'legolas'),
    );
    if (addedZones(state).armor !== 'discard')
      problems.push(`the Armor he bore is in ${addedZones(state).armor}, not the discard pile`);
    await until(async () => (await page.locator(ui.card(gone)).count()) === 0, 'Legolas gone from the table');
  },

  /** A refusal is said where it was asked for, in the server's own words. */
  async refuse(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const frodo = await seat.card('frodo');
    const said = await seat.who
      .post('/scenarios/sandbox/edit', {
        gameId: seat.gameId(),
        ops: JSON.stringify([{ op: 'delete', card: 'frodo' }]),
      })
      .then(() => null)
      .catch((error) => error.message);
    const words = /"error":"([^"]+)"/.exec(said ?? '')?.[1] ?? null;
    if (!words) problems.push(`the server did not refuse deleting the Ring-bearer (${said})`);
    seat.refusals++;
    await seat.menuOf(frodo.cardId);
    await page.locator(ui.menuEntry('delete')).click();
    const shown = await until(async () => (await page.locator(ui.problem).textContent())?.trim(), 'the refusal');
    if (words && shown !== words) problems.push(`the refusal says "${shown}", not the server's "${words}"`);
    await seat.shot('refused');
    if (!(await seat.card('frodo'))) problems.push('Frodo was deleted');
  },

  /** Start → the prompt offers the card added → play it → Stop → edit → Start. */
  async play(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await seat.searchFrom('freeps', 'hand', 'greenleaf', LEGOLAS);
    let state = await seat.edit(
      'Legolas into the hand',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'hand',
    );
    const legolas = state.cards.find((card) => card.alias === 'legolas');
    await page.locator(ui.barButton('start')).click();
    await seat.editing('play');
    if ((await page.locator(ui.spots).count()) > 0) problems.push('open spots in play mode');
    await until(
      async () => (await page.locator(ui.alert).textContent()).includes('Fellowship'),
      'the fellowship prompt',
    );
    const mark = await until(
      async () => page.locator(ui.card(legolas.cardId)).getAttribute('data-mark'),
      'Legolas offered',
    );
    if (mark !== 'actionable') problems.push(`Legolas in hand is ${mark}, not offered`);
    await seat.shot('started');
    state = await seat.edit(
      'play Legolas',
      () => page.locator(ui.card(legolas.cardId)).click(),
      (now) => now.cards.find((card) => card.alias === 'legolas').zone === 'characters',
    );
    // In play mode right-click is the card's details, as always
    const played = state.cards.find((card) => card.alias === 'legolas');
    await page.locator(ui.card(played.cardId)).click({ button: 'right' });
    await page
      .locator(ui.cardInfo)
      .waitFor()
      .catch(() => problems.push('right-click in play mode showed no details'));
    if ((await page.locator(ui.menu).count()) > 0) problems.push('right-click in play mode opened the edit menu');
    await page.keyboard.press('Escape');
    await until(() => page.locator(ui.barButton('stop')).isEnabled(), 'Stop enabled');
    await page.locator(ui.barButton('stop')).click();
    await seat.editing('edit');
    await seat.shot('stopped');
    await seat.searchFrom('freeps', 'support', 'armor', ARMOR);
    await seat.edit(
      'Armor after Stop',
      () => page.locator(ui.searchCard(ARMOR)).click(),
      (now) => addedZones(now).armor === 'support',
    );
    await page.locator(ui.barButton('start')).click();
    await seat.editing('play');
    if ((await seat.state()).mode !== 'play') problems.push('Start did not start play on the server');
  },

  /** Taking back across an edit: the undo list reads well, and the page is right after it. */
  async undo(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await seat.searchFrom('freeps', 'hand', 'greenleaf', LEGOLAS);
    await seat.edit(
      'Legolas into the hand',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'hand',
    );
    await page.locator(ui.barButton('start')).click();
    await seat.editing('play');
    const legolas = await seat.card('legolas');
    await seat.edit(
      'play Legolas',
      () => page.locator(ui.card(legolas.cardId)).click(),
      (now) => now.cards.find((card) => card.alias === 'legolas').zone === 'characters',
    );
    await until(() => page.locator(ui.barButton('stop')).isEnabled(), 'Stop enabled');
    await page.locator(ui.barButton('stop')).click();
    await seat.editing('edit');
    await page.locator(ui.tab('options')).click();
    await page.locator(ui.revert).click();
    await page.locator(ui.undoPoint).first().waitFor();
    const labels = await page.locator(ui.undoLabel).allTextContents();
    const wanted = ['Stopped play', 'Played Legolas, Greenleaf', 'Started play', 'Added Legolas, Greenleaf'];
    if (labels.slice(0, 4).join('|') !== wanted.join('|')) problems.push(`the undo list says ${labels.join(' | ')}`);
    await seat.shot('undo-list');
    // Back to before the edit: stopped, nothing added, the controls live
    const point = page.locator(ui.undoPoint).filter({ hasText: 'Added Legolas, Greenleaf' });
    await point.locator(ui.undoGo).click();
    await until(
      async () => (await seat.state()).cards.every((card) => !card.added),
      'the game taken back before the edit',
      20000,
    );
    await seat.editing('edit');
    await until(async () => (await page.locator(ui.card(legolas.cardId)).count()) === 0, 'Legolas gone from the page');
    if ((await page.locator(ui.spots).count()) !== 6) problems.push('the open spots are not back after taking back');
    if ((await page.locator(ui.alert).textContent()).trim() !== '')
      problems.push('the decision shows after taking back');
    await seat.shot('taken-back');
    // The controls still work on the rebuilt game
    await seat.searchFrom('freeps', 'characters', 'greenleaf', LEGOLAS);
    await seat.edit(
      'Legolas again',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'characters',
    );
  },

  /** Reset asks in the page's own dialog, then the page goes to a fresh game of the sandbox. */
  async reset(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const before = seat.gameId();
    await seat.searchFrom('freeps', 'characters', 'greenleaf', LEGOLAS);
    await seat.edit(
      'Legolas',
      () => page.locator(ui.searchCard(LEGOLAS)).click(),
      (now) => addedZones(now).legolas === 'characters',
    );
    await page.locator(ui.barButton('reset')).click();
    await page.locator(ui.confirm).waitFor();
    await seat.shot('reset-asked');
    await page.locator(ui.confirmButton('cancel')).click();
    if (seat.gameId() !== before || !(await seat.card('legolas'))) problems.push('Cancel reset the table');
    await page.locator(ui.barButton('reset')).click();
    await page.locator(ui.confirmButton('ok')).click();
    await until(() => seat.gameId() !== before, 'a fresh game', 20000);
    await seat.editing('edit');
    if ((await seat.state()).cards.some((card) => card.added)) problems.push('the fresh game has the added card');
    table.note = `game ${before} → ${seat.gameId()}`;
  },

  /** A sandbox's end: the Ring-bearer wounded to death and Start; the panel says who won and why; Reset. */
  async ended(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const frodo = await seat.card('frodo');
    await seat.menuOf(frodo.cardId);
    const add = await seat.submenu('add');
    await add.locator('input[type="number"]').fill('4');
    await add.locator('input[type="number"]').dispatchEvent('change');
    await seat.edit(
      'Frodo given four wounds',
      () => add.locator('[data-add="wound"]').click(),
      (now) => now.cards.find((card) => card.alias === 'frodo').wounds === 4,
    );
    await page.locator(ui.barButton('start')).click();
    const panel = page.locator(ui.panel);
    await panel.waitFor({ timeout: 20000 });
    const result = await panel.getAttribute('data-result');
    if (result !== 'ended') problems.push(`the end panel's result is ${result}`);
    const heading = (await panel.locator('h2').textContent()).trim();
    if (heading !== 'You lost') problems.push(`the end panel says "${heading}"`);
    const body = (await panel.locator('.gd-scenario-panel__body').textContent()).trim();
    if (!/Ring-Bearer is dead/i.test(body)) problems.push(`the end panel does not say why: "${body}"`);
    const buttons = await panel.locator('button[data-panel-button]').allTextContents();
    if (buttons.map((text) => text.trim()).join() !== 'Reset,Leave')
      problems.push(`the end panel offers ${buttons.join(', ')}`);
    await seat.shot('ended');
    const before = seat.gameId();
    await page.locator(ui.panelButton('Reset')).click();
    await until(() => seat.gameId() !== before, 'a fresh game after Reset', 20000);
    await seat.editing('edit');
  },
  /** Every pile of both seats opens from its count (a link), showing what the server's table has, in its order. */
  async piles(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await setUp(seat, [
      { op: 'add', blueprint: LEGOLAS, to: 'freeps.deck.top' },
      { op: 'add', blueprint: BOROMIR, to: 'freeps.deck.bottom' },
      { op: 'add', blueprint: ARMOR, to: 'freeps.deck.top' },
      { op: 'add', blueprint: ARMOR, to: 'freeps.hand' },
      { op: 'add', blueprint: ARMOR, to: 'freeps.discard' },
      { op: 'add', blueprint: LEGOLAS, to: 'freeps.discard' },
      { op: 'add', blueprint: RUNNER, to: 'freeps.deadPile' },
      { op: 'add', blueprint: LEGOLAS, to: 'freeps.removed' },
      { op: 'add', blueprint: RUNNER, to: 'shadow.hand' },
      { op: 'add', blueprint: LEGOLAS, to: 'shadow.hand' },
      { op: 'add', blueprint: ARMOR, to: 'shadow.deck' },
      { op: 'add', blueprint: BOROMIR, to: 'shadow.discard' },
    ]);
    const rows = await seatRows(seat);
    for (const index of [0, 1]) await page.locator(ui.statsToggle(index)).click();
    await page.locator(ui.stat(`removedPile${rows.shadow}`)).waitFor();
    await seat.shot('links');
    const state = await seat.state();
    for (const owner of ['freeps', 'shadow']) {
      for (const { pile, zone } of PILES) {
        const counter = page.locator(ui.stat(`${pile}${rows[owner]}`));
        if ((await counter.getAttribute('data-link')) === null)
          problems.push(`the ${owner} ${pile}'s count is no link`);
        const count = (await counter.textContent()).trim();
        if (count !== String(state.piles[owner][zone]))
          problems.push(`the ${owner} ${pile}'s count is ${count}, not the server's ${state.piles[owner][zone]}`);
        await openPile(seat, rows, pile, owner);
        await windowShows(seat, pile, owner, pileOf(state, zone, owner), problems, `the ${owner} ${pile}`);
        if (`${owner} ${pile}` === 'freeps deck' || `${owner} ${pile}` === 'shadow hand')
          await seat.shot(`${owner}-${pile}`);
        await page.locator(ui.pileClose(pile, owner)).click();
      }
    }
    // A window open stays right as the game moves: a card put on the deck from elsewhere shows once play goes on
    await openPile(seat, rows, 'deck', 'freeps');
    await seat.who.post('/scenarios/sandbox/edit', {
      gameId: seat.gameId(),
      ops: JSON.stringify([{ op: 'add', blueprint: RUNNER, to: 'freeps.deck.top' }]),
    });
    await page.locator(ui.barButton('start')).click();
    await seat.editing('play');
    const moved = pileOf(await seat.state(), 'deck', 'freeps');
    if (moved.length !== 4) problems.push(`the freeps deck has ${moved.length} cards on the server, not 4`);
    await windowShows(seat, 'deck', 'freeps', moved, problems, 'the deck after play went on');
    if ((await page.locator(`${ui.pileWindow('deck', 'freeps')} gd-open-spot`).count()) > 0)
      problems.push('a pile window has open spots in play mode');
    await seat.shot('played');
  },

  /** A pile window's open spots: a draw deck's top and bottom, the other seat's hand, a discard pile. */
  async pileadd(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const rows = await seatRows(seat);
    await openPile(seat, rows, 'deck', 'freeps');
    const spots = await page
      .locator(`${ui.pileWindow('deck', 'freeps')} gd-open-spot button`)
      .evaluateAll((buttons) => buttons.map((button) => button.dataset.zone));
    if (spots.join() !== 'deck.top,deck.bottom') problems.push(`the deck window's spots are ${spots.join()}`);
    await seat.shot('deck-spots');
    let state = await seat.edit(
      'Legolas on the deck, by the search',
      () => searchAt(seat, page.locator(ui.pileSpot('deck', 'freeps', 'deck.top')), 'greenleaf', LEGOLAS),
      (now) => addedZones(now).legolas === 'deck',
    );
    await until(async () => (await page.locator(ui.search).count()) === 0, 'the search window closed');
    state = await seat.edit(
      'Boromir at the bottom of the deck, by the search',
      () => searchAt(seat, page.locator(ui.pileSpot('deck', 'freeps', 'deck.bottom')), 'boromir', BOROMIR),
      (now) => addedZones(now).boromir === 'deck',
    );
    state = await seat.edit(
      'Legolas on top of the deck, from Recent',
      async () => {
        await page.locator(ui.pileSpot('deck', 'freeps', 'deck.top')).click();
        await page.locator(ui.chooserItem('recent', LEGOLAS)).click();
      },
      (now) => addedZones(now).legolas2 === 'deck',
    );
    const deck = pileOf(state, 'deck', 'freeps');
    const alias = (id) => state.cards.find((card) => String(card.cardId) === id)?.alias;
    if (deck.map(alias).join() !== 'legolas2,legolas,boromir') problems.push(`the deck is ${deck.map(alias).join()}`);
    await windowShows(seat, 'deck', 'freeps', deck, problems, 'the deck after adding');
    await seat.shot('deck-added');
    await page.locator(ui.pileClose('deck', 'freeps')).click();
    // The other seat's hand
    await openPile(seat, rows, 'hand', 'shadow');
    state = await seat.edit(
      "Legolas into the other seat's hand, from Recent",
      async () => {
        await page.locator(ui.pileSpot('hand', 'shadow', 'hand')).click();
        await page.locator(ui.chooserItem('recent', LEGOLAS)).click();
      },
      (now) => now.cards.some((card) => card.added && card.owner === 'shadow' && card.zone === 'hand'),
    );
    await windowShows(seat, 'hand', 'shadow', pileOf(state, 'hand', 'shadow'), problems, "the other seat's hand");
    await seat.shot('other-hand');
    await page.locator(ui.pileClose('hand', 'shadow')).click();
    // A discard pile
    await page.locator(ui.statsToggle(rows.freeps)).click();
    await openPile(seat, rows, 'discard', 'freeps');
    state = await seat.edit(
      'Armor into the discard pile, by the search',
      () => searchAt(seat, page.locator(ui.pileSpot('discard', 'freeps', 'discard')), 'armor', ARMOR),
      (now) => addedZones(now).armor === 'discard',
    );
    await windowShows(seat, 'discard', 'freeps', pileOf(state, 'discard', 'freeps'), problems, 'the discard pile');
  },

  /** A pile card's menu: no wounds or tokens; Send to the hand, Copy, Delete, each seen in the window. */
  async pilemenu(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    await setUp(seat, [
      { op: 'add', blueprint: LEGOLAS, to: 'freeps.deck.top' },
      { op: 'add', blueprint: BOROMIR, to: 'freeps.deck.bottom' },
    ]);
    const rows = await seatRows(seat);
    await openPile(seat, rows, 'deck', 'freeps');
    let state = await seat.state();
    const id = (alias) => String(state.cards.find((card) => card.alias === alias).cardId);
    await menuAt(seat, page.locator(ui.pileCard(id('legolas'))));
    const entries = await menuEntries(seat);
    if (entries.join() !== 'info,send,stack,copy,delete') problems.push(`a deck card's menu is ${entries.join()}`);
    const send = await seat.submenu('send');
    const places = await send.locator('[data-zone]').evaluateAll((items) => items.map((item) => item.dataset.zone));
    if (!places.includes('deck.top') || !places.includes('hand')) problems.push(`Send to offers ${places.join()}`);
    await seat.shot('menu');
    state = await seat.edit(
      'Legolas sent from the deck to the hand',
      () => send.locator('[data-zone="hand"]').click(),
      (now) => addedZones(now).legolas === 'hand',
    );
    await windowShows(seat, 'deck', 'freeps', pileOf(state, 'deck', 'freeps'), problems, 'the deck after Send to');
    await until(async () => (await page.locator(ui.card(id('legolas'))).count()) === 1, 'Legolas in the hand row');
    await menuAt(seat, page.locator(ui.pileCard(id('boromir'))));
    state = await seat.edit(
      'Boromir copied in the deck',
      () => page.locator(ui.menuEntry('copy')).click(),
      (now) => addedZones(now).boromir2 === 'deck',
    );
    await windowShows(seat, 'deck', 'freeps', pileOf(state, 'deck', 'freeps'), problems, 'the deck after Copy');
    await menuAt(seat, page.locator(ui.pileCard(id('boromir2'))));
    state = await seat.edit(
      'the copy deleted',
      () => page.locator(ui.menuEntry('delete')).click(),
      (now) => !now.cards.some((card) => card.alias === 'boromir2'),
    );
    await windowShows(seat, 'deck', 'freeps', pileOf(state, 'deck', 'freeps'), problems, 'the deck after Delete');
    await seat.shot('deck-after');
  },

  /** The path's +: a site by the search (held to its number), from each seat's adventure deck; a site replaced. */
  async path(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const spotAt = async () => page.locator(ui.siteSpot).getAttribute('data-site');
    if ((await spotAt()) !== '2') problems.push(`the path's + is for site ${await spotAt()}, not 2`);
    await page.locator(ui.siteSpot).click();
    await page.locator(ui.siteChooser).waitFor();
    await seat.shot('chooser');
    await page.locator(ui.siteItem('search')).click();
    await page.locator(ui.search).waitFor();
    const bound = (await page.locator(`${ui.search} .gd-edit-search__bound`).textContent()).trim();
    if (bound !== 'Site 2') problems.push(`the site search is bound by "${bound}"`);
    const field = page.locator(`${ui.search} .gd-field[data-field="name"] input`);
    await field.fill('ettenmoors');
    await field.dispatchEvent('change');
    await seat.shot('search');
    let state = await seat.edit(
      'Ettenmoors by the search at site 2',
      () => page.locator(`${ui.search} .gd-edit-search__cards button.gd-card[data-blueprint="1_331"]`).click(),
      (now) => now.path.find((site) => site.number === 2)?.blueprintId === '1_331',
    );
    const searched = pathOf(state)[2];
    if (searched === 'site2') problems.push('the search laid the deck’s own site, not a new card');
    await until(async () => (await page.locator(ui.search).count()) === 0, 'the search window closed');
    await until(async () => (await spotAt()) === '3', "the path's + at site 3");
    await page.locator(ui.siteSpot).click();
    await page.locator(ui.siteItem('decks')).click();
    await page.locator(ui.siteItem('deck', '[data-seat="shadow"]')).click();
    await seat.shot('shadow-deck');
    state = await seat.edit(
      "the other seat's site 3 from its adventure deck",
      () => page.locator(ui.siteItem('site', '[data-alias="site3opp"]')).click(),
      (now) => pathOf(now)[3] === 'site3opp',
    );
    await until(async () => (await spotAt()) === '4', "the path's + at site 4");
    await page.locator(ui.siteSpot).click();
    state = await seat.edit(
      'the own site 4 from the adventure deck',
      () => siteFromDeck(seat, 'freeps', 'site4'),
      (now) => pathOf(now)[4] === 'site4',
    );
    const shown = async () =>
      page.locator(ui.pathCards).evaluateAll((cards) => cards.map((card) => card.dataset.blueprintId));
    await until(async () => (await shown()).join() === '1_319,1_331,1_341,1_343', 'four sites on the page').catch(
      async () => problems.push(`the page's path is ${(await shown()).join()}`),
    );
    await seat.shot('laid');
    // Replace site 2 (the card the search made) with the person's own Ettenmoors, from the site's menu
    const laid = state.cards.find((card) => card.alias === searched);
    await menuAt(seat, page.locator(ui.pathCard(laid.cardId)));
    await page.locator(ui.menuEntry('replace')).click();
    await page.locator(ui.siteChooser).waitFor();
    const heading = (await page.locator(`${ui.siteChooser} .gd-edit-chooser__heading`).textContent()).trim();
    if (heading !== 'Replace site 2') problems.push(`Replace… opens "${heading}"`);
    state = await seat.edit(
      'site 2 replaced from the adventure deck',
      () => siteFromDeck(seat, 'freeps', 'site2'),
      (now) => pathOf(now)[2] === 'site2',
    );
    if (state.cards.find((card) => card.alias === searched)?.zone !== 'adventureDeck')
      problems.push('the site replaced did not go back to its adventure deck');
    await until(async () => (await page.locator(ui.pathCard(laid.cardId)).count()) === 0, 'the replaced site gone');
  },

  /** A site's menu: a marker moved, control refused in the server's words, control given, liberated. */
  async markers(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    let state = await seat.state();
    const site1 = String(state.cards.find((card) => card.alias === 'site1').cardId);
    await menuAt(seat, page.locator(ui.pathCard(site1)));
    const entries = await menuEntries(seat);
    const wanted = 'info,marker freeps,marker shadow,control freeps,control shadow,replace';
    if (entries.join() !== wanted) problems.push(`a site's menu is ${entries.join()}`);
    if (!(await page.locator(ui.siteAction('marker', 'freeps')).isDisabled()))
      problems.push('the marker already at the site is offered');
    await seat.shot('menu');
    // Control while the markers are at it: refused, in the server's words
    seat.refusals++;
    seat.resyncs++;
    await page.locator(ui.siteAction('control', 'shadow')).click();
    const said = await until(async () => (await page.locator(ui.problem).textContent())?.trim(), 'the refusal');
    if (!/cannot be controlled/.test(said)) problems.push(`the refusal says "${said}"`);
    await seat.shot('refused');
    // Site 2 laid, both markers moved onto it
    await page.locator(ui.siteSpot).click();
    state = await seat.edit(
      'site 2 laid',
      () => siteFromDeck(seat, 'freeps', 'site2'),
      (now) => pathOf(now)[2] === 'site2',
    );
    const site2 = String(state.cards.find((card) => card.alias === 'site2').cardId);
    for (const who of ['freeps', 'shadow']) {
      await menuAt(seat, page.locator(ui.pathCard(site2)));
      state = await seat.edit(
        `the ${who} marker to site 2`,
        () => page.locator(ui.siteAction('marker', who)).click(),
        (now) => now.markers[who] === 2,
      );
    }
    await menuAt(seat, page.locator(ui.pathCard(site1)));
    state = await seat.edit(
      'site 1 given to the shadow seat',
      () => page.locator(ui.siteAction('control', 'shadow')).click(),
      (now) => now.path[0].controlledBy === 'shadow',
    );
    // (a controlled site lies in its controller's support area)
    await page
      .locator(ui.card(site1))
      .waitFor({ timeout: 8000 })
      .catch(() => problems.push('the controlled site is not in the support area'));
    await seat.shot('controlled');
    await menuAt(seat, page.locator(ui.card(site1)));
    state = await seat.edit(
      'site 1 liberated',
      () => page.locator(`${ui.menu} [data-site-action="liberate"]`).click(),
      (now) => now.path[0].controlledBy === null,
    );
    await until(async () => (await page.locator(ui.pathCard(site1)).count()) === 1, 'site 1 back on the path');
  },

  /** The twilight pool, the burdens and the threats: set (Set, Enter) and stepped where they are shown. */
  async counters(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open();
    const rows = await seatRows(seat);
    const text = async (selector) => (await page.locator(selector).textContent()).trim();
    await seat.edit(
      'twilight set to 5',
      () => setCounter(seat, 'twilight', 5),
      (now) => now.twilight === 5,
    );
    await until(async () => (await text(ui.editCounter('twilight'))) === '5', 'the pool showing 5').catch(async () =>
      problems.push(`the pool shows ${await text(ui.editCounter('twilight'))}`),
    );
    await seat.shot('twilight');
    await seat.edit(
      'twilight one up',
      () => page.locator(`${ui.counterBox('twilight')} [data-step="up"]`).click(),
      (now) => now.twilight === 6,
    );
    await seat.edit(
      'twilight one down',
      () => page.locator(`${ui.counterBox('twilight')} [data-step="down"]`).click(),
      (now) => now.twilight === 5,
    );
    await page.keyboard.press('Escape');
    await until(async () => (await page.locator(ui.counterBox('twilight')).count()) === 0, 'the box closed');
    await seat.edit(
      'burdens set to 2',
      () => setCounter(seat, 'burdens', 2, 'enter'),
      (now) => now.burdens === 2,
    );
    await seat.edit(
      'a burden off',
      () => page.locator(`${ui.counterBox('burdens')} [data-step="down"]`).click(),
      (now) => now.burdens === 1,
    );
    await until(async () => (await text(ui.editCounter('burdens'))) === '1', 'the burdens showing 1');
    await page.keyboard.press('Escape');
    await seat.edit(
      'threats set to 1',
      () => setCounter(seat, 'threats', 1),
      (now) => now.threats === 1,
    );
    await until(async () => (await text(ui.editCounter('threats'))).startsWith('1'), 'the threats showing 1');
    await seat.shot('threats');
    await seat.edit(
      'a threat off',
      () => page.locator(`${ui.counterBox('threats')} [data-step="down"]`).click(),
      (now) => now.threats === 0,
    );
    await page.keyboard.press('Escape');
    // In play mode the counters are as they always were
    await page.locator(ui.barButton('start')).click();
    await seat.editing('play');
    if ((await page.locator('gd-edit-counter').count()) > 0) problems.push('a counter is editable in play mode');
    if ((await page.locator(ui.burdens).count()) > 0) problems.push('the burdens line shows in play mode');
  },

  /** A sandbox whose tools leave out piles, sites, counters and markers offers none of them (a sandbox of its own). */
  async tools(table, problems) {
    const { seat } = table;
    const { page } = seat;
    const player = pairOf(0).player;
    const id = `check-u2-tools-${Date.now().toString(36)}`;
    const folder = path.join(DATA, 'users', player, 'sandboxes');
    const file = path.join(folder, `${id}.hjson`);
    const made = !fs.existsSync(folder);
    const source = fs
      .readFileSync(
        path.join(HERE, '../../../gemp-lotr-cards/src/main/resources/scenarios/sandboxes/sandbox.hjson'),
        'utf8',
      )
      .replace(/^\tid: sandbox$/m, `\tid: ${id}`)
      .replace(/^\tformat: scenario$/m, '\tformat: scenario\n\toptions: {\n\t\ttools: add, move, copy, delete\n\t}');
    fs.mkdirSync(folder, { recursive: true });
    fs.writeFileSync(file, source);
    try {
      // (the server's library looks at the folder again only every so often)
      const started = await until(
        async () => (await seat.who.post('/scenarios/start', { id: `${player}/${id}` })).json(),
        `the server finding ${player}/${id}`,
        20000,
      );
      if (!started.gameId) throw new Error(`scenarios/start ${player}/${id} answered ${JSON.stringify(started)}`);
      seat.log(`started ${player}/${id}: game ${started.gameId}`);
      await page.goto(`${server}${ui.path}?gameId=${started.gameId}`);
      await seat.editing('edit');
      const state = await seat.state();
      if (state.tools.join() !== 'add,copy,delete,move') problems.push(`its tools are ${state.tools.join()}`);
      const rows = await seatRows(seat);
      for (const index of [0, 1]) await page.locator(ui.statsToggle(index)).click();
      await page.locator(ui.stat(`removedPile${rows.shadow}`)).waitFor();
      const links = await page
        .locator('gd-game-state [data-link]')
        .evaluateAll((counters) => counters.map((counter) => counter.dataset.stat));
      const wanted = [
        ...['hand', 'discard', 'deadPile', 'adventureDeck', 'removedPile'].map((pile) => `${pile}${rows.freeps}`),
        ...['discard', 'deadPile', 'removedPile'].map((pile) => `${pile}${rows.shadow}`),
      ];
      if ([...links].sort().join() !== [...wanted].sort().join()) problems.push(`the links are ${links.join()}`);
      for (const stat of [
        `deck${rows.freeps}`,
        `deck${rows.shadow}`,
        `hand${rows.shadow}`,
        `adventureDeck${rows.shadow}`,
      ]) {
        if ((await page.locator(ui.stat(stat)).getAttribute('data-clickable')) !== null)
          problems.push(`the hidden pile ${stat} opens`);
      }
      if ((await page.locator(ui.siteSpot).count()) > 0) problems.push("the path has a + without 'sites'");
      if ((await page.locator('gd-edit-counter').count()) > 0)
        problems.push("a counter is editable without 'counters'");
      if ((await page.locator(ui.burdens).count()) > 0) problems.push("the burdens show without 'counters'");
      const site1 = String(state.cards.find((card) => card.alias === 'site1').cardId);
      await menuAt(seat, page.locator(ui.pathCard(site1)));
      const entries = await menuEntries(seat);
      if (entries.join() !== 'info') problems.push(`a site's menu without markers or sites is ${entries.join()}`);
      await page.keyboard.press('Escape');
      // What it allows still works: its own discard pile's window has its open spot
      await openPile(seat, rows, 'discard', 'freeps');
      if ((await page.locator(ui.pileSpot('discard', 'freeps', 'discard')).count()) !== 1)
        problems.push('the discard window has no open spot');
      await seat.shot('tools');
    } finally {
      fs.rmSync(file, { force: true });
      if (made && fs.readdirSync(folder).length === 0) fs.rmdirSync(folder);
    }
  },
};

let browser;

async function run(name, theme) {
  const check = `${name}-${theme}`;
  const problems = [];
  const started = Date.now();
  const player = pairOf(0).player;
  const table = { theme, note: '' };
  try {
    table.who = await session(server, player, password);
    const { hostname } = new URL(server);
    const context = await browser.newContext({ ...DEVICES.pc });
    const cookies = table.who.cookie.split('; ').map((text) => {
      const [cookie, ...value] = text.split('=');
      return { name: cookie, value: value.join('='), domain: hostname, path: '/' };
    });
    await context.addCookies([
      ...cookies,
      { name: 'animation-speed', value: '4', domain: hostname, path: '/' },
      { name: 'gempGameTheme', value: theme, domain: hostname, path: '/' },
    ]);
    table.seat = new Seat(check, await context.newPage(), table.who);
    await CHECKS[name](table, problems);
  } catch (error) {
    problems.push(error.message.split('\n')[0]);
    await table.seat?.shot('failed');
  }
  if (table.seat) {
    problems.push(...new Set(table.seat.errors));
    const gameId = table.seat.gameId();
    if (gameId) await table.who.post(`/game/${gameId}/concede`, {}).catch(() => undefined);
    await table.seat.page.context().close();
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
const runs = names.flatMap((name) => THEMES.map((theme) => [name, theme]));
browser = await launchBrowser();
const results = [];
const began = Date.now();
try {
  for (const [name, theme] of runs) results.push(await run(name, theme));
} finally {
  await browser.close();
}
const ok = results.filter((result) => result.ok).length;
const summary = `${ok} of ${runs.length} sandbox checks passed in ${Math.round((Date.now() - began) / 1000)}s. Screenshots: ${OUT}`;
console.log(summary);
fs.writeFileSync(
  path.join(OUT, 'summary.txt'),
  [...results.flatMap((result) => result.lines), summary].join('\n') + '\n',
);
process.exitCode = ok === runs.length ? 0 : 1;
