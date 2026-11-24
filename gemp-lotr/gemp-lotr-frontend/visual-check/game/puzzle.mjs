// The puzzle player's page on the Angular game page: a puzzle opened held with its slots on the table, the slots
// filled from the chooser and the bounded search window (each pick checked by the server), Submit, the game played
// through to its end panel (solved, failed, scored, an unlisted solution), Start again and Change cards, Undo absent
// or present as the file says, and the author's "Find… / Use as bound". A real browser plays copies of Decipher's
// puzzles 10, 08 and 03 (the engine's fixtures, gemp-lotr-server/src/test/resources/scenarios/puzzles/), put in the
// player's own scenarios folder under an id starting `check-` and deleted afterwards. See "The puzzle checks" in
// README.md.
//
//   npm run check:game:puzzle                                     every check, in both themes
//   npm run check:game:puzzle -- --only held,solve --theme modern --out report-puzzle
//   GEMP_PUZZLE_PLAYER=GamePlayer2 npm run check:game:puzzle      as another player (default GamePlayer3)

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { launchBrowser } from '../browser.mjs';
import { session } from '../local-server/seed.mjs';
import { DEVICES } from './device-profiles.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const { server, password } = JSON.parse(fs.readFileSync(path.join(CHECK_DIR, '.local-server/game/login.json'), 'utf8'));

const argument = (name, fallback) => {
  const at = process.argv.indexOf(`--${name}`);
  return at < 0 ? fallback : process.argv[at + 1];
};
const OUT = path.resolve(CHECK_DIR, argument('out', 'report-puzzle'));
const ONLY = argument('only', '').split(',').filter(Boolean);
const THEMES = argument('theme', 'classic,modern').split(',').filter(Boolean);
const VERBOSE = process.argv.includes('--verbose');
/** The player the checks play as (their own scenarios folder holds the puzzles' copies while a check runs). */
const PLAYER = process.env.GEMP_PUZZLE_PLAYER ?? 'GamePlayer3';
/** Where the server keeps its scenarios (this machine's), and the engine's puzzle fixtures. */
const DATA = process.env.GEMP_SCENARIOS ?? '/etc/gemp-lotr/scenarios';
const FIXTURES = path.resolve(CHECK_DIR, '../../gemp-lotr-server/src/test/resources/scenarios/puzzles');

const PUZZLES = {
  10: '10-defending-the-keep.hjson',
  8: '08-ambush-of-the-southrons.hjson',
  3: '03-like-this-would-happen-2b.hjson',
};
const PARAPET = '5_87';
const ARMORY = '5_79';
const ALDOR = '4_262';
/** A Rohan condition of sets 1–6 other than Parapet and Armory: the fifth one the bounded search lists. */
const TRAP = '4_126';
const BE_BACK_SOON = '5_21';
const DEFEND_IT = '4_115';
const STURDY = '1_315';

/** The page's DOM contract for the puzzle page (ui/game/table-edit/puzzle-*, the scenario's menu and panels). */
const ui = {
  path: '/gemp-lotr/ng/game',
  game: 'gemp-game',
  bar: 'gd-puzzle-bar',
  barMode: 'gd-puzzle-bar .gd-edit-bar__mode',
  submit: 'gd-puzzle-bar button[data-puzzle-bar="submit"]',
  change: 'gd-puzzle-bar button[data-puzzle-bar="change"]',
  restart: 'gd-puzzle-bar button[data-scenario-menu="restart"]',
  barProblem: 'gd-puzzle-bar .gd-edit-bar__problem',
  slot: (id) => `.gd-puzzle-slot[data-slot="${id}"]`,
  slotButton: (id) => `.gd-puzzle-slot[data-slot="${id}"] > .gd-puzzle-slot__face > button`,
  slots: '.gd-puzzle-slot',
  strip: 'section.gd-puzzle-strip',
  spots: 'gd-open-spot button.gd-open-spot',
  chooser: 'gd-puzzle-chooser .gd-edit-chooser',
  chooserItem: (kind, blueprint) =>
    `gd-puzzle-chooser [data-chooser="${kind}"]${blueprint ? `[data-blueprint="${blueprint}"]` : ''}`,
  search: 'dialog[data-dialog="edit-search"][open]',
  searchName: 'dialog[data-dialog="edit-search"][open] .gd-field[data-field="name"] input',
  searchField: (name) => `dialog[data-dialog="edit-search"][open] .gd-field[data-field="${name}"]`,
  searchCultures: 'dialog[data-dialog="edit-search"][open] .gd-cultures',
  searchCard: (blueprint) =>
    `dialog[data-dialog="edit-search"][open] .gd-edit-search__cards button.gd-card[data-blueprint="${blueprint}"]`,
  searchCards: 'dialog[data-dialog="edit-search"][open] .gd-edit-search__cards button.gd-card',
  searchCount: 'dialog[data-dialog="edit-search"][open] .gd-edit-search__count',
  searchBound: 'dialog[data-dialog="edit-search"][open] .gd-edit-search__bound',
  searchProblem: 'dialog[data-dialog="edit-search"][open] .gd-edit-search__problem',
  searchUse: 'dialog[data-dialog="edit-search"][open] button[data-action="use"]',
  searchClose: 'dialog[data-dialog="edit-search"][open] .gd-dialog__close',
  problem: '.gd-edit-problem',
  card: (blueprint) => `.gd-game__table [data-blueprint-id="${blueprint}"]`,
  offered: (blueprint) => `.gd-game__table [data-blueprint-id="${blueprint}"][data-mark]`,
  alert: '[data-game="alert"]',
  decisionButtons: 'button[data-decision-button]',
  decisionButton: (label) => `button[data-decision-button="${label}"]`,
  choice: 'dialog[open] button[data-decision-choice]',
  actionMenu: 'ul.gd-game-menu button[data-action-id]',
  cardInfo: 'dialog.gd-card-info[open]',
  editMenu: '.gd-edit-menu',
  confirm: 'dialog.gd-dialog[open][data-dialog="confirm"]',
  confirmButton: (action) => `dialog.gd-dialog[open][data-dialog="confirm"] button[data-action="${action}"]`,
  panel: '.gd-scenario-panel',
  panelButton: (label) => `.gd-scenario-panel button[data-panel-button="${label}"]`,
  panelScore: '.gd-scenario-panel__score',
  panelUnlisted: '.gd-scenario-panel__unlisted',
  undoButton: 'button[data-undo="last"]',
  tab: (name) => `[data-tab="${name}"]`,
  revert: '[data-button="undoGame"]',
  author: 'gd-game-author',
  authorTab: (name) => `gd-game-author [data-tab="${name}"]`,
  authorSlot: (id) => `gd-game-author .gd-author-slot[data-slot="${id}"]`,
  find: (part) => `gd-game-author button[data-action="find"][data-for="${part}"]`,
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
    await sleep(150);
  }
}

/**
 * Puts a copy of a puzzle fixture in the player's own scenarios folder, under an id of the check's (`check-…`),
 * changed by `edit` (its HJSON text in, out); waits until the server offers it, and answers the id `scenarios/start`
 * takes. The copy is deleted when the check is done (`table.cleanup`).
 */
async function bring(table, number, name, edit = (text) => text) {
  const id = `check-puzzle-${name}-${Date.now().toString(36)}`;
  const text = fs.readFileSync(path.join(FIXTURES, PUZZLES[number]), 'utf8');
  const changed = edit(text.replace(/^\tid: .*$/m, `\tid: ${id}`));
  const folder = path.join(DATA, 'users', PLAYER, 'puzzles');
  fs.mkdirSync(folder, { recursive: true });
  const file = path.join(folder, `${id}.hjson`);
  fs.writeFileSync(file, changed);
  table.cleanup.push(() => fs.rmSync(file, { force: true }));
  const full = `${PLAYER}/${id}`;
  await until(
    async () => {
      const listed = await (await table.who.get('/scenarios')).json();
      return listed.scenarios.some((scenario) => scenario.id === full);
    },
    `the server offering ${full}`,
    60000,
  );
  return full;
}

/** The person's page on a puzzle, and what went wrong on it by itself. */
class Seat {
  constructor(check, page, who) {
    this.check = check;
    this.page = page;
    this.who = who;
    this.errors = [];
    this.moves = [];
    this.shots = 0;
    /** Picks the server is expected to refuse (a check that makes a refusal says so). */
    this.refusals = 0;
    page.on('pageerror', (error) => this.errors.push(`page error: ${error.message.split('\n')[0]}`));
    page.on('console', (message) => {
      if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) {
        this.errors.push(`console error: ${message.text().slice(0, 200)}`);
      }
    });
    page.on('response', (response) => {
      const { pathname } = new URL(response.url());
      if (!pathname.includes('/gemp-lotr-server/') || response.status() < 400) return;
      if (pathname.endsWith('/scenarios/start') && response.status() === 400 && this.refusals > 0) {
        this.refusals--;
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

  /** Starts a game of the puzzle (with picks: played; without: held) and opens the page on it. */
  async open(id, picks = null) {
    const started = await (
      await this.who.post('/scenarios/start', { id, ...(picks ? { picks: JSON.stringify(picks) } : {}) })
    ).json();
    if (!started.gameId) throw new Error(`scenarios/start ${id} answered ${JSON.stringify(started)}`);
    this.log(`started ${id}: game ${started.gameId} (${started.mode})`);
    await this.page.goto(`${server}${ui.path}?gameId=${started.gameId}`);
    await this.mode(started.mode);
  }

  /** Waits for the puzzle bar to say the mode, and the page to be at rest. */
  async mode(mode) {
    await until(
      async () => (await this.page.locator(ui.bar).getAttribute('data-mode')) === mode,
      `the puzzle bar in ${mode} mode`,
      20000,
    );
    await sleep(400);
  }

  /** Opens a slot's chooser and waits for the server's word on its candidates. */
  async chooser(slot) {
    await this.page.locator(ui.slotButton(slot)).click();
    await this.page.locator(ui.chooser).waitFor();
    await until(
      async () => (await this.page.locator(`${ui.chooser} [data-checking]`).count()) === 0,
      "the chooser's cards checked",
    );
  }

  /** Opens a slot's search window, types a name (if any), and waits for the results. */
  async search(slot, name = null) {
    await this.chooser(slot);
    await this.page.locator(ui.chooserItem('search')).click();
    await this.page.locator(ui.search).waitFor();
    if (name !== null) {
      const field = this.page.locator(ui.searchName);
      await field.fill(name);
      await field.dispatchEvent('change');
    }
    await until(async () => (await this.page.locator(ui.searchCount).textContent())?.trim(), 'the search counted');
    await sleep(300);
  }

  /** Fills a slot from its search window with a card found by a name. */
  async fill(slot, name, blueprint) {
    this.log(`fill ${slot} with ${blueprint} from the search`);
    await this.search(slot, name);
    await this.page.locator(ui.searchCard(blueprint)).click();
    await until(async () => (await this.page.locator(ui.search).count()) === 0, 'the search window closed');
    await this.filled(slot, blueprint);
  }

  /** Waits for a slot to hold a card (null: to be empty). */
  async filled(slot, blueprint) {
    await until(
      async () =>
        blueprint === null
          ? (await this.page.locator(`${ui.slot(slot)}[data-filled]`).count()) === 0
          : (await this.page.locator(`${ui.slot(slot)} button[data-blueprint="${blueprint}"]`).count()) === 1,
      `slot ${slot} ${blueprint === null ? 'empty' : `holding ${blueprint}`}`,
    );
  }

  /** Submit, and waits for the fresh game in play mode. */
  async submit() {
    const before = this.gameId();
    await this.page.locator(ui.submit).click();
    await until(() => this.gameId() !== before, 'a fresh game after Submit', 20000);
    await this.mode('play');
    this.log(`submitted: game ${before} → ${this.gameId()}`);
  }

  /** The scripted game's info event for the game on the page, as the server sends it to the player. */
  async info() {
    const xml = await (await this.who.get(`/game/${this.gameId()}`)).text();
    const said = [...xml.matchAll(/scenario="([^"]*)"/g)]
      .map((match) =>
        JSON.parse(
          match[1]
            .replace(/&quot;/g, '"')
            .replace(/&lt;/g, '<')
            .replace(/&gt;/g, '>')
            .replace(/&apos;/g, "'")
            .replace(/&amp;/g, '&'),
        ),
      )
      .filter((event) => event.type === 'info');
    return said.at(-1) ?? null;
  }

  /** Waits for the game to ask something of the person (or to end): the alert's text, or a dialog. */
  async asked() {
    return until(
      async () => {
        if ((await this.page.locator(ui.panel).count()) > 0) return 'ended';
        if ((await this.page.locator(ui.choice).count()) > 0) return 'choice';
        const alert = (await this.page.locator(ui.alert).textContent())?.trim();
        return alert ? alert : null;
      },
      'a decision or the end',
      20000,
    );
  }

  /**
   * Gives the person's answers as a solution's `answers` write them (`use(x)`, `play(x)`, `choose(x)`, `option(text)`,
   * `assign(a: b, c: d)`, `pass`), each card by its blueprint (`cards`: name → blueprint), on the page as a player
   * would: a click on the card offered, the choice's button, the minion then its character.
   */
  async play(answers, cards) {
    for (const answer of answers) {
      await this.asked();
      this.log(`answer ${answer}`);
      const [, word, inside] = /^(\w+)(?:\((.*)\))?$/.exec(answer.trim()) ?? [];
      if (word === 'pass') {
        await this.page.locator(ui.decisionButton('Pass')).click();
      } else if (word === 'option') {
        await this.page.locator(ui.choice).filter({ hasText: inside }).first().click();
      } else if (word === 'assign') {
        for (const pair of inside.split(',')) {
          const [character, minion] = pair.split(':').map((name) => name.trim());
          await this.page.locator(ui.offered(cards[minion])).first().click();
          await this.page.mouse.move(2, 2);
          await this.page.locator(ui.offered(cards[character])).first().click();
          await this.page.mouse.move(2, 2);
        }
        await this.page.locator(ui.decisionButton('Done')).click();
      } else {
        const blueprint = cards[inside];
        await until(async () => (await this.page.locator(ui.offered(blueprint)).count()) > 0, `${inside} offered`);
        await this.page.locator(ui.offered(blueprint)).first().click();
        await this.page.mouse.move(2, 2);
        // (a card with several actions: the first; a card chosen in a choice that waits for Done: Done)
        await sleep(300);
        if ((await this.page.locator(ui.actionMenu).count()) > 0) await this.page.locator(ui.actionMenu).first().click();
        if ((await this.page.locator(`${ui.card(blueprint)}[data-mark="selected"]`).count()) > 0) {
          await this.page.locator(ui.decisionButton('Done')).click();
        }
      }
      await sleep(700);
    }
  }

  /** Passes, and gives each wound or choice to the first card offered, until the end panel. */
  async playOn(max = 40) {
    for (let moves = 0; moves < max; moves++) {
      const asked = await this.asked();
      if (asked === 'ended') return;
      if (asked === 'choice') {
        await this.page.locator(ui.choice).first().click();
      } else if ((await this.page.locator(ui.decisionButton('Pass')).count()) > 0) {
        await this.page.locator(ui.decisionButton('Pass')).click();
      } else if ((await this.page.locator('.gd-game__table [data-mark="selectable"]').count()) > 0) {
        await this.page.locator('.gd-game__table [data-mark="selectable"]').first().click();
        await sleep(300);
        if ((await this.page.locator('.gd-game__table [data-mark="selected"]').count()) > 0) {
          await this.page.locator(ui.decisionButton('Done')).click();
        }
      } else if ((await this.page.locator(ui.decisionButtons).count()) > 0) {
        await this.page.locator(ui.decisionButtons).first().click();
      }
      this.log(`played on at "${asked}"`);
      await sleep(700);
    }
    throw new Error(`no end panel after ${max} moves`);
  }

  /** The end panel: its result, heading, body text and buttons. */
  async ending() {
    const panel = this.page.locator(ui.panel);
    await panel.waitFor({ timeout: 30000 });
    return {
      result: await panel.getAttribute('data-result'),
      heading: (await panel.locator('h2').textContent()).trim(),
      body: (await panel.locator('.gd-scenario-panel__body').textContent()).trim(),
      buttons: (await panel.locator('button[data-panel-button]').allTextContents()).map((text) => text.trim()),
    };
  }
}

/** Puzzle 10's published key, as its solution's answers have it, and its cards. */
const KEY_10 = ['use(condition)', 'option(Make the minion archery total -3)', 'choose(ally)', 'choose(ally)'];
const CARDS_10 = { condition: PARAPET, ally: ALDOR };

const CHECKS = {
  /** Puzzle 10 opened held: its slots on the table, the bar, no decision, Submit off, Undo absent, right-click info. */
  async held(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open(await bring(table, 10, 'held'));
    await seat.shot('held');
    if ((await page.locator(ui.game).getAttribute('data-edit')) !== 'edit')
      problems.push('the page is not in edit mode');
    if ((await page.locator(ui.barMode).textContent()).trim() !== 'Choosing')
      problems.push('the bar does not say Choosing');
    const slots = await page.locator(ui.slots).evaluateAll((all) =>
      all.map((slot) => `${slot.dataset.slot}:${slot.closest('[data-row]')?.getAttribute('data-row')}`),
    );
    if (slots.join() !== 'condition:support-player,ally:support-player')
      problems.push(`the slots are ${slots.join()}, not both in the support row`);
    const labels = await page.locator(`${ui.slots} .gd-puzzle-slot__label`).allTextContents();
    if (labels.map((text) => text.trim()).join('|') !== 'a Rohan condition|a Rohan ally')
      problems.push(`the slots are labelled ${labels.join('|')}`);
    if ((await page.locator(ui.spots).count()) > 0) problems.push("the sandbox's open spots show in a puzzle");
    if ((await page.locator(ui.strip).count()) > 0) problems.push('a slots strip with no slot away from its place');
    if (!(await page.locator(ui.submit).isDisabled())) problems.push('Submit is enabled with the slots empty');
    const title = await page.locator(ui.submit).getAttribute('title');
    if (title !== 'Still empty: a Rohan condition, a Rohan ally') problems.push(`Submit's title is "${title}"`);
    if ((await page.locator(ui.restart).count()) > 0) problems.push('Start again is offered while choosing');
    // Nobody is asked anything: no decision, no buttons, no hand card offered, no bubble
    const alert = (await page.locator(ui.alert).textContent()).trim();
    if (alert !== '') problems.push(`the held puzzle shows a decision: "${alert}"`);
    if ((await page.locator(ui.decisionButtons).count()) > 0) problems.push('the held puzzle shows decision buttons');
    if ((await page.locator('.gd-game__table [data-mark]').count()) > 0) problems.push('cards are offered to be played');
    if ((await page.locator('.gd-scenario-bubble').count()) > 0) problems.push('a bubble is up');
    // Undo is off in a puzzle (as the file leaves it)
    if ((await page.locator(ui.undoButton).count()) > 0) problems.push('the Undo button shows');
    await page.locator(ui.tab('options')).click();
    if ((await page.locator(ui.revert).count()) > 0) problems.push('Revert is offered in a puzzle');
    // Right-click is a card's details here: the cards are not changed by hand
    await page.locator(ui.card('4_267')).first().click({ button: 'right' });
    await page
      .locator(ui.cardInfo)
      .waitFor()
      .catch(() => problems.push('right-click showed no card information'));
    if ((await page.locator(ui.editMenu).count()) > 0) problems.push("right-click opened the sandbox's card menu");
    await page.keyboard.press('Escape');
    const info = await seat.info();
    if (info?.mode !== 'edit' || info.level !== 'puzzle' || info.slots?.length !== 2)
      problems.push(`the server's info says ${JSON.stringify({ mode: info?.mode, level: info?.level })}`);
  },

  /** The bounded search: its bound shown and fixed; a card outside it is not found, and refused in the server's words. */
  async bound(table, problems) {
    const { seat } = table;
    const { page } = seat;
    const id = await bring(table, 10, 'bound');
    await seat.open(id);
    await seat.search('condition');
    await seat.shot('search');
    const bound = page.locator(ui.searchBound);
    if ((await bound.textContent()).trim() !== 'a Rohan condition') problems.push('the bound is not labelled');
    const filter = await bound.getAttribute('title');
    if (filter !== 'set:1-6 culture:ROHAN cardtype:CONDITION') problems.push(`the search's bound is "${filter}"`);
    // The fields the bound sets are not offered: they could only widen it
    for (const field of ['cardType', 'set', 'block']) {
      if ((await page.locator(ui.searchField(field)).count()) > 0) problems.push(`the ${field} field is offered`);
    }
    if ((await page.locator(ui.searchCultures).count()) > 0) problems.push('the culture buttons are offered');
    const count = (await page.locator(ui.searchCount).textContent()).trim();
    if (!/ of \d+$/.test(count)) problems.push(`the bounded search found "${count}"`);
    // Aldor (a Rohan ally) cannot be found by the condition slot's search
    await page.locator(ui.searchName).fill('aldor');
    await page.locator(ui.searchName).dispatchEvent('change');
    await until(async () => (await page.locator(ui.searchCount).textContent()).trim() === 'No cards', 'no Aldor');
    await seat.shot('aldor-not-found');
    // Sent anyway (the page's own request carrying Aldor, as an old page could): refused in the server's words
    const asked = await (
      await seat.who.post('/scenarios/picks', { id, picks: JSON.stringify({ condition: ALDOR }) })
    ).json();
    const words = asked.picks?.condition?.why;
    if (!words || asked.picks.condition.fits) problems.push(`the server took Aldor as a condition (${JSON.stringify(asked)})`);
    await page.locator(ui.searchName).fill('parapet');
    await page.locator(ui.searchName).dispatchEvent('change');
    await page.locator(ui.searchCard(PARAPET)).waitFor();
    await page.route('**/scenarios/picks', (route) => {
      const body = new URLSearchParams(route.request().postData() ?? '');
      body.set('picks', JSON.stringify({ condition: ALDOR }));
      return route.continue({ postData: body.toString() });
    });
    await page.locator(ui.searchCard(PARAPET)).click();
    const said = await until(async () => (await page.locator(ui.searchProblem).textContent())?.trim(), 'the refusal');
    if (said !== words) problems.push(`the search says "${said}", not the server's "${words}"`);
    await seat.shot('refused');
    await page.unroute('**/scenarios/picks');
    if ((await page.locator(`${ui.slot('condition')}[data-filled]`).count()) > 0) problems.push('the refused card was put in');
    // The chooser's Recent holds only what the server says the slot takes (Aldor is remembered, not offered here)
    await page.locator(ui.searchClose).click();
    await page.evaluate(
      ([aldor, parapet]) =>
        localStorage.setItem(
          'gemp-edit-recent',
          JSON.stringify([
            { blueprintId: aldor, name: 'Aldor', type: 'ALLY' },
            { blueprintId: parapet, name: 'Parapet', type: 'CONDITION' },
          ]),
        ),
      [ALDOR, PARAPET],
    );
    await page.reload();
    await seat.mode('edit');
    await seat.chooser('condition');
    const recent = await page.locator(`${ui.chooser} [data-chooser="recent"]`).evaluateAll((items) =>
      items.map((item) => item.dataset.blueprint),
    );
    if (recent.join() !== PARAPET) problems.push(`the condition slot's Recent offers ${recent.join() || 'nothing'}`);
    await seat.shot('recent-held');
    await page.keyboard.press('Escape');
  },

  /** Fill from the search, from Recent; replace; empty; Submit only once all are filled; Submit plays with the picks. */
  async fill(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open(await bring(table, 10, 'fill'));
    await seat.fill('ally', 'aldor', ALDOR);
    if (!(await page.locator(ui.submit).isDisabled())) problems.push('Submit is enabled with a slot empty');
    if ((await page.locator(ui.submit).getAttribute('title')) !== 'Still empty: a Rohan condition')
      problems.push('Submit does not say the condition is missing');
    // Replace: Armory, then Parapet over it
    await seat.fill('condition', 'armory', ARMORY);
    await seat.shot('filled');
    if (await page.locator(ui.submit).isDisabled()) problems.push('Submit is off with every slot filled');
    await seat.fill('condition', 'parapet', PARAPET);
    // Empty the ally, then fill it again from Recent
    await seat.chooser('ally');
    await seat.shot('chooser');
    await page.locator(ui.chooserItem('empty')).click();
    await seat.filled('ally', null);
    if (!(await page.locator(ui.submit).isDisabled())) problems.push('Submit is enabled after emptying a slot');
    await seat.chooser('ally');
    const recent = await page.locator(`${ui.chooser} [data-chooser="recent"]`).evaluateAll((items) =>
      items.map((item) => item.dataset.blueprint),
    );
    if (recent.join() !== ALDOR) problems.push(`the ally slot's Recent offers ${recent.join()}, only Aldor fits`);
    await page.locator(ui.chooserItem('recent', ALDOR)).click();
    await seat.filled('ally', ALDOR);
    // The filled card has the page's hover zoom and its details on right-click
    await page.locator(`${ui.slot('ally')} button.gd-card`).click({ button: 'right' });
    await page
      .locator(ui.cardInfo)
      .waitFor()
      .catch(() => problems.push('right-click on a filled slot showed no card information'));
    await page.keyboard.press('Escape');
    await seat.submit();
    await seat.shot('submitted');
    for (const blueprint of [PARAPET, ALDOR]) {
      if ((await page.locator(ui.card(blueprint)).count()) === 0) problems.push(`${blueprint} is not on the table`);
    }
    if ((await page.locator(ui.slots).count()) > 0) problems.push('the slots still show in play');
    const info = await seat.info();
    const picks = Object.fromEntries((info?.slots ?? []).map((slot) => [slot.id, slot.pick]));
    if (picks.condition !== PARAPET || picks.ally !== ALDOR) problems.push(`the game was built with ${JSON.stringify(picks)}`);
    if ((await page.locator(ui.barMode).count()) > 0) problems.push('the bar shows a mode in play');
    if (!(await page.locator(ui.change).isVisible())) problems.push('Change cards is not offered in play');
    // Undo stays off in play (the server's policy for a puzzle)
    if ((await page.locator(ui.undoButton).count()) > 0) problems.push('the Undo button shows in play');
  },

  /** The published key played through to "solved"; Play it again keeps the picks; Change cards returns to the slots. */
  async solve(table, problems) {
    const { seat } = table;
    const { page } = seat;
    const id = await bring(table, 10, 'solve');
    await seat.open(id, CARDS_10);
    await seat.play(KEY_10, CARDS_10);
    const end = await seat.ending();
    await seat.shot('solved');
    if (end.result !== 'complete') problems.push(`the key ended ${end.result}`);
    if (!end.body.includes('Every companion came through the archery.'))
      problems.push(`the panel says "${end.body}", not the puzzle's own words`);
    if ((await page.locator(ui.panelScore).count()) > 0) problems.push('an unscored puzzle shows a score');
    if ((await page.locator(ui.panelUnlisted).count()) > 0) problems.push('the published key is said to be unlisted');
    if (end.buttons.join() !== 'Play it again,Change cards,Back to the Game Hall')
      problems.push(`the panel offers ${end.buttons.join(', ')}`);
    // Play it again: the same cards
    let before = seat.gameId();
    await page.locator(ui.panelButton('Play it again')).click();
    await until(() => seat.gameId() !== before, 'a fresh game', 20000);
    await seat.mode('play');
    for (const blueprint of [PARAPET, ALDOR]) {
      if ((await page.locator(ui.card(blueprint)).count()) === 0) problems.push(`Play it again lost ${blueprint}`);
    }
    // Change cards, from the menu: asked first, then held with the slots empty
    before = seat.gameId();
    await page.locator(ui.change).click();
    await page.locator(ui.confirm).waitFor();
    await seat.shot('change-asked');
    await page.locator(ui.confirmButton('ok')).click();
    await until(() => seat.gameId() !== before, 'a held game', 20000);
    await seat.mode('edit');
    if ((await page.locator(`${ui.slots}[data-filled]`).count()) > 0) problems.push('Change cards kept a card in a slot');
    if ((await page.locator(ui.slots).count()) !== 2) problems.push('Change cards did not bring the slots back');
  },

  /** A wrong pick (Armory: a third arrow is left) played through to "failed"; Start again keeps the picks. */
  async fail(table, problems) {
    const { seat } = table;
    const { page } = seat;
    await seat.open(await bring(table, 10, 'fail'), { condition: ARMORY, ally: ALDOR });
    await seat.playOn();
    const end = await seat.ending();
    await seat.shot('failed');
    if (end.result !== 'failed') problems.push(`the wrong pick ended ${end.result}`);
    // (Frodo dies to the third arrow: the game ends by its own rules before the judge, so the stock words for a failure)
    if (end.body === '') problems.push('the panel says nothing');
    table.note = `"${end.body}"`;
    if (end.buttons.join() !== 'Start again,Change cards,Back to the Game Hall')
      problems.push(`the panel offers ${end.buttons.join(', ')}`);
    const before = seat.gameId();
    await page.locator(ui.panelButton('Start again')).click();
    await until(() => seat.gameId() !== before, 'a fresh game', 20000);
    await seat.mode('play');
    if ((await page.locator(ui.card(ARMORY)).count()) === 0) problems.push('Start again lost Armory');
  },

  /** Puzzle 08 with one of its two solutions left out: the other, played, is said to be one the author did not list. */
  async unlisted(table, problems) {
    const { seat } = table;
    const { page } = seat;
    // (the second solution, trap-and-defend-it, taken out of the copy)
    const id = await bring(table, 8, 'unlisted', (text) => {
      const at = text.indexOf('\t\t{\n\t\t\tname: trap-and-defend-it');
      const end = text.indexOf('\n\t\t}\n', at) + 4;
      return text.slice(0, at) + text.slice(end);
    });
    await seat.open(id);
    // The hand slot is a row's (the hand); the support slot comes with tokens, said on it
    const comes = (await page.locator(`${ui.slot('support')} .gd-puzzle-slot__comes`).textContent())?.trim();
    if (comes !== '3 tokens') problems.push(`the support slot says it comes with "${comes}"`);
    const hand = await page.locator(ui.slot('hand')).evaluate((slot) => slot.closest('[data-row]')?.dataset.row);
    if (hand !== 'hand') problems.push(`the hand slot is in ${hand}`);
    await seat.fill('support', 'ithilien trap', TRAP);
    await seat.fill('hand', 'defend it', DEFEND_IT);
    await seat.shot('filled');
    await seat.submit();
    const cards = {
      support: TRAP,
      hand: DEFEND_IT,
      commander: '4_249',
      lancers: '4_217',
      legion: '4_218',
      faramir: '6_121',
      ranger: '4_130',
    };
    await seat.play(['use(support)', 'choose(commander)'], cards);
    // (undo is off in a puzzle: no Undo button, even with choices made)
    if ((await page.locator(ui.undoButton).count()) > 0) problems.push('the Undo button shows in a puzzle');
    await seat.play(
      [
        'choose(commander)',
        'choose(lancers)',
        'play(hand)',
        'choose(faramir)',
        'choose(commander)',
        'assign(faramir: lancers, ranger: legion)',
      ],
      cards,
    );
    await seat.playOn();
    const end = await seat.ending();
    await seat.shot('unlisted');
    if (end.result !== 'complete') problems.push(`the solution ended ${end.result}`);
    const unlisted = (await page.locator(ui.panelUnlisted).textContent().catch(() => null))?.trim();
    if (unlisted !== 'You found a solution the author did not list.')
      problems.push(`the panel does not say the solution is unlisted (${unlisted})`);
  },

  /** Puzzle 03's score: a short line (the troll on Sméagol, then nothing) to the skirmish; the best and New best. */
  async score(table, problems) {
    const { seat } = table;
    const { page } = seat;
    const id = await bring(table, 3, 'score');
    await seat.open(id);
    // Nine slots: eight in the hand's row, the site in the strip, labelled with its place
    if ((await page.locator(`[data-row="hand"] ${ui.slots}`).count()) !== 8) problems.push('the hand slots are not in the hand');
    const site = (await page.locator(`${ui.strip} ${ui.slot('site2')} .gd-puzzle-slot__place`).textContent())?.trim();
    if (site !== 'Site 2') problems.push(`the site slot's place says "${site}"`);
    // The site's search is held to site 2 of set 4
    await seat.search('site2');
    const filter = await page.locator(ui.searchBound).getAttribute('title');
    if (filter !== 'set:4 cardtype:SITE sitenumber:2') problems.push(`the site slot's search is "${filter}"`);
    // (every site 2 of set 4: Derndingle and Fangorn Forest, the published answers, among them)
    const found = await page.locator(ui.searchCards).evaluateAll((cards) => cards.map((card) => card.dataset.blueprint));
    if (found.length !== 7 || !found.includes('4_330') || !found.includes('4_332'))
      problems.push(`the site search finds ${found.join(', ')}`);
    await seat.shot('site-search');
    await page.locator(ui.searchCard('4_330')).click();
    await seat.filled('site2', '4_330');
    for (let slot = 1; slot <= 8; slot++) await seat.fill(`hand${slot}`, 'stout and sturdy', STURDY);
    await seat.shot('filled');
    await seat.submit();
    const cards = { troll: '1_165', smeagol: '5_29' };
    const playOut = async () => {
      // (the troll must skirmish Sméagol: the step allows nothing else; then nothing more to the skirmish)
      for (let moves = 0; moves < 30; moves++) {
        const asked = await seat.asked();
        if (asked === 'ended') return;
        if ((await page.locator(ui.offered(cards.troll)).count()) > 0 && /assign/i.test(asked)) {
          await seat.play(['assign(smeagol: troll)'], cards);
        } else if ((await page.locator(ui.decisionButton('Pass')).count()) > 0) {
          await page.locator(ui.decisionButton('Pass')).click();
        } else {
          await seat.playOn(1).catch(() => undefined);
        }
        await sleep(600);
      }
    };
    await playOut();
    let end = await seat.ending();
    await seat.shot('scored');
    if (end.result !== 'complete') problems.push(`the scored line ended ${end.result}`);
    const score = page.locator(ui.panelScore);
    const label = (await score.locator('.gd-scenario-panel__score-label').textContent())?.trim();
    if (label !== "Sméagol's strength") problems.push(`the score is labelled "${label}"`);
    const value = (await score.locator('.gd-scenario-panel__score-value').textContent())?.trim();
    if (!/^\d+$/.test(value ?? '')) problems.push(`the score is "${value}"`);
    if ((await score.locator('.gd-scenario-panel__new-best').count()) !== 1) problems.push('the first score is not a new best');
    if ((await score.locator('.gd-scenario-panel__best').textContent())?.trim() !== `Best ${value}`)
      problems.push('the best is not this score');
    if ((await score.getAttribute('title')) !== 'Higher is better') problems.push('the score does not say which way is better');
    // Again with the same cards: the same score, the best kept, no New best
    const before = seat.gameId();
    await page.locator(ui.panelButton('Play it again')).click();
    await until(() => seat.gameId() !== before, 'a fresh game', 20000);
    await seat.mode('play');
    await playOut();
    end = await seat.ending();
    await seat.shot('scored-again');
    if ((await score.locator('.gd-scenario-panel__new-best').count()) > 0) problems.push('the same score is a new best');
    if ((await score.locator('.gd-scenario-panel__best').textContent())?.trim() !== `Best ${value}`)
      problems.push('the best is not kept');
    const listed = (await (await seat.who.get('/scenarios')).json()).scenarios.find((one) => one.id === id);
    if (String(listed?.progress?.bestScore) !== value) problems.push(`GET scenarios says the best is ${listed?.progress?.bestScore}`);
    table.note = `score ${value}`;
  },

  /** A puzzle whose file says `undo: on`: the Undo button and Revert are offered in its game. */
  async undo(table, problems) {
    const { seat } = table;
    const { page } = seat;
    const id = await bring(table, 10, 'undo', (text) => text.replace('\t\tautoPass: true\n', '\t\tautoPass: true\n\t\tundo: on\n'));
    await seat.open(id, CARDS_10);
    await page.locator(ui.tab('options')).click();
    await page
      .locator(ui.revert)
      .waitFor({ timeout: 10000 })
      .catch(() => problems.push('Revert is not offered with undo on'));
    await seat.play(KEY_10.slice(0, 2), CARDS_10);
    await seat.asked();
    await page.locator(ui.revert).click();
    await page
      .locator('[data-undo="point"]')
      .first()
      .waitFor({ timeout: 10000 })
      .catch(() => problems.push('the undo list has nothing to go back to with undo on'));
    await seat.shot('undo-on');
  },

  /** The author's Find… beside a slot's bound and the pool: the card search's "Use as bound" sends its filters. */
  async author(table, problems) {
    const { seat, who } = table;
    const { page } = seat;
    const call = async (what, fields) => (await who.post(`/scenarios/author/${what}`, fields)).json();
    await call('close', {}).catch(() => undefined);
    const id = `check-puzzle-author-${Date.now().toString(36)}`;
    const opened = await call('open', { new: 'true', id, title: 'Find', canonical: 'false' });
    const slots = { ally: { zone: 'support', filter: 'culture:ROHAN cardtype:ALLY', label: 'a Rohan ally' } };
    await call('edit', { patch: JSON.stringify({ top: { kind: 'puzzle', pool: 'set:1-6', slots } }) });
    const state = await (await who.get('/scenarios/author/state')).json();
    const gameId = state.gameId ?? opened.gameId;
    await page.goto(`${server}${ui.path}?gameId=${gameId}`);
    await page.locator(ui.author).waitFor({ timeout: 20000 });
    await page.locator(ui.authorTab('board')).click();
    await page.locator(ui.find('ally')).click();
    await page.locator(ui.search).waitFor();
    const field = page.locator(ui.searchField('cardType')).locator('select');
    await field.selectOption('ALLY');
    await page.locator(`dialog[data-dialog="edit-search"][open] label.gd-culture[data-culture="GONDOR"]`).click();
    await until(async () => (await page.locator(ui.searchUse).getAttribute('title'))?.includes('cardType:ALLY'), 'the filter');
    const filter = await page.locator(ui.searchUse).getAttribute('title');
    await seat.shot('find');
    await page.locator(ui.searchUse).click();
    await until(async () => (await page.locator(ui.search).count()) === 0, 'the search window closed');
    const sent = await until(async () => {
      const now = await (await who.get('/scenarios/author/state')).json();
      return now.draft?.slots?.ally?.filter === filter ? now : null;
    }, `the slot's bound "${filter}" on the server`);
    if (!sent) problems.push('the bound was not sent');
    await seat.shot('used');
    // The pool, from the Scenario tab
    await page.locator(ui.authorTab('scenario')).click();
    await page.locator(ui.find('pool')).click();
    await page.locator(ui.search).waitFor();
    await page.locator(ui.searchField('set')).locator('select').selectOption({ index: 2 }).catch(() => undefined);
    const pool = await page.locator(ui.searchUse).getAttribute('title');
    await page.locator(ui.searchUse).click();
    await until(async () => {
      const now = await (await who.get('/scenarios/author/state')).json();
      return now.draft?.pool === pool || now.puzzle?.pool === pool;
    }, `the pool "${pool}" on the server`).catch(() => problems.push(`the pool "${pool}" was not sent`));
    table.note = `bound "${filter}", pool "${pool}"`;
    await call('close', {}).catch(() => undefined);
  },
};

let browser;

async function run(name, theme) {
  const check = `${name}-${theme}`;
  const problems = [];
  const started = Date.now();
  const table = { theme, note: '', cleanup: [] };
  try {
    table.who = await session(server, PLAYER, password);
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
  for (const clean of table.cleanup) await Promise.resolve().then(clean).catch(() => undefined);
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
const summary = `${ok} of ${runs.length} puzzle checks passed in ${Math.round((Date.now() - began) / 1000)}s. Screenshots: ${OUT}`;
console.log(summary);
fs.writeFileSync(
  path.join(OUT, 'summary.txt'),
  [...results.flatMap((result) => result.lines), summary].join('\n') + '\n',
);
process.exitCode = ok === runs.length ? 0 : 1;
