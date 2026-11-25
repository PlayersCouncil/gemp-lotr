// What the scenario authoring checks share (../author.mjs runs them; see "The authoring checks" in ../README.md):
// the panel's selectors, an author's page on a draft of their own, and the waiting helpers. A check is
// `async (table, problems) => …`: `table.seat` is the author's page, `table.who` the same player over HTTP,
// `table.theme` the theme; what is wrong goes into `problems` (a thrown error counts as one).

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
export const CHECK_DIR = path.resolve(HERE, '../..');

export function argument(name, fallback) {
  const at = process.argv.indexOf(`--${name}`);
  return at < 0 ? fallback : process.argv[at + 1];
}
export const OUT = path.resolve(CHECK_DIR, argument('out', 'report-author'));
export const VERBOSE = process.argv.includes('--verbose');
export const { server, password } = JSON.parse(
  fs.readFileSync(path.join(CHECK_DIR, '.local-server/game/login.json'), 'utf8'),
);

/** Where the Angular game page keeps the authoring panel (its DOM contract: authoring-panel.component.ts). */
export const ui = {
  path: '/gemp-lotr/ng/game',
  game: 'gemp-game',
  panel: 'gd-game-author section.gd-author',
  panelIn: (view) => `gd-game-author section.gd-author[data-view="${view}"]`,
  fold: 'button.gd-author__fold',
  title: '.gd-author__title',
  busy: '.gd-author__busy',
  save: 'button.gd-author__save',
  where: '.gd-author__where',
  notices: 'gd-game-author-notices',
  tab: (name) => `button.gd-author__tab[data-tab="${name}"]`,
  body: '.gd-author__body',
  timeline: 'gd-game-author-timeline',
  step: 'gd-game-author-step',
  opponent: 'gd-game-author-opponent',
  board: 'gd-game-author-board',
  scenario: 'gd-game-author-scenario',
  leave: 'dialog.gd-dialog[open][data-dialog="author-leave"]',
  leaveButton: (action) => `dialog.gd-dialog[open][data-dialog="author-leave"] button[data-action="${action}"]`,
  /** The page's own question dialog (not the browser's). */
  confirm: 'dialog.gd-dialog[open][data-dialog="confirm"]',
  confirmButton: (action) => `dialog.gd-dialog[open][data-dialog="confirm"] button[data-action="${action}"]`,
  bubble: '.gd-scenario-bubble',
  menuButton: (name) => `[data-scenario-menu="${name}"]`,
  decisionButton: (label) => `button[data-decision-button="${label}"]`,
};

export const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** Waits until `read()` gives something (not null, undefined or false), and returns it; throws saying `what` if not. */
export async function until(read, what, max = 15000) {
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

const short = (text, max) => (text.length > max ? `${text.slice(0, max)}…` : text);

/** The author's page, and what went wrong on it by itself (`errors`). */
export class Seat {
  constructor(check, page, who) {
    this.check = check;
    this.page = page;
    this.who = who;
    this.errors = [];
    this.moves = [];
    this.shots = 0;
    /** Authoring requests the server is allowed to refuse (a check that forces a refusal adds a pattern). */
    this.refusalsExpected = [];
    page.on('pageerror', (error) => this.errors.push(`page error: ${error.message.split('\n')[0]}`));
    page.on('console', (message) => {
      // A picture the image server didn't send, or a call the server refused, is in the network's own words; the
      // page's own errors count
      if (message.type() === 'error' && !/Failed to load resource/.test(message.text())) {
        this.errors.push(`console error: ${short(message.text(), 200)}`);
      }
    });
    page.on('response', (response) => {
      const { pathname } = new URL(response.url());
      if (!pathname.includes('/gemp-lotr-server/')) return;
      const expected = this.refusalsExpected.some((pattern) => pattern.test(pathname));
      if (response.status() >= 500 || (response.status() >= 400 && pathname.includes('/scenarios/author/'))) {
        if (!expected) this.errors.push(`HTTP ${response.status()} from ${pathname}`);
      }
    });
    // The panel asks its questions in the page; the browser's own dialog is a mistake
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

  /** The draft as the server has it (GET scenarios/author/state). */
  async state() {
    return (await this.who.get('/scenarios/author/state')).json();
  }

  /** An authoring call over HTTP, beside the page (to set a draft up quickly); answers the server's JSON. */
  async call(name, fields = {}) {
    return (await this.who.post(`/scenarios/author/${name}`, fields)).json();
  }

  /** Opens the game page on the draft's game and waits for the docked panel with its state. */
  async openGame(gameId) {
    await this.page.goto(`${server}${ui.path}?gameId=${gameId}`);
    await this.page.locator(ui.panelIn('author')).waitFor({ timeout: 20000 });
    await this.settled();
  }

  /**
   * Waits until the panel has nothing on its way, and the page is on the draft's game: every change that replays
   * the script is a fresh game, which the page goes to by loading again.
   */
  async settled(max = 20000) {
    await until(
      async () => {
        const state = await this.state();
        if (!state.draft || String(state.gameId) !== this.gameId()) return false;
        const panel = this.page.locator(ui.panelIn('author'));
        return (await panel.count()) === 1 && (await this.page.locator(ui.busy).count()) === 0;
      },
      "the panel on the draft's game, with nothing on its way",
      max,
    );
    await sleep(150);
  }

  async showTab(name) {
    await this.page.locator(ui.tab(name)).click();
    await this.page.locator(`${ui.tab(name)}[aria-selected="true"]`).waitFor({ timeout: 5000 });
  }

  whereLine() {
    return this.page.locator(ui.where).innerText();
  }
}

let drafts = 0;

/**
 * A new draft of the player's own (`open` with `new=true`), with the script given: `steps` are the file's steps
 * (objects as an HJSON file has them), `top` the file's other parts, `goto` the step the script is played to (from
 * 1). Answers its id; the page is on its game.
 */
export async function newDraft(seat, { title = 'Authoring check', steps = [], top = {}, goto = 1 } = {}) {
  await seat.call('close').catch(() => undefined);
  const id = `check-${Date.now().toString(36)}-${drafts++}`;
  const opened = await seat.call('open', { new: 'true', id, title, canonical: 'false' });
  if (steps.length || Object.keys(top).length) {
    const patch = {
      ...(Object.keys(top).length ? { top } : {}),
      ...(steps.length ? { steps: steps.map((step) => ({ op: 'insert', step })) } : {}),
    };
    // (`goto` plays the new script from its start, in a fresh game)
    await seat.call('edit', { patch: JSON.stringify(patch), goto: String(goto) });
  }
  const state = await seat.state();
  await seat.openGame(String(state.gameId ?? opened.gameId));
  return id;
}

/** Closes the player's draft without saving it (what a check leaves behind). */
export async function closeDraft(seat) {
  await seat.call('close').catch(() => undefined);
}
