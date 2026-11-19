// Taking a game back (undo): real games on a game check server kept up, each player on a page of their own in a real
// browser. A player takes back a choice of their own at once; asks the opponent for anything more, who approves,
// declines or declines everything; withdraws a request; and sees the opponent concede while a decision of their own
// is still waiting. Each check plays its own game and says what it found; a screenshot of every step worth seeing is
// saved in the report folder.
//
//   npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
//   npm run check:game:undo                                        every check, on the legacy page
//   npm run check:game:undo -- --only own,approve --out report-undo
//   npm run check:game:down
//
// The page is game.html: the Angular game page has no undo yet. `UNDO` below is what a page must offer for these
// checks (its `legacy` entry is game.html's); the Angular page gets an entry of its own, and `--page angular`.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { launchBrowser } from '../browser.mjs';
import { LEGACY, readGameState } from './controls.mjs';
import { pairOf } from './players.mjs';
import { endGame, startGame } from './protocol.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const CHECK_DIR = path.resolve(HERE, '..');
const { server, password } = JSON.parse(fs.readFileSync(path.join(CHECK_DIR, '.local-server/game/login.json'), 'utf8'));

const argument = (name, fallback) => {
  const at = process.argv.indexOf(`--${name}`);
  return at < 0 ? fallback : process.argv[at + 1];
};
const KIND = argument('page', 'legacy');
const OUT = path.resolve(CHECK_DIR, argument('out', 'report-undo'));
const ONLY = argument('only', '').split(',').filter(Boolean);

/** Where a page keeps what undo shows, per kind of page. */
const UNDO = {
  legacy: {
    path: '/gemp-lotr/game.html',
    /** Opens the list of places the game can be taken back to. */
    openList: async (page) => {
      await page.locator(LEGACY.tab('options')).click();
      await page.locator('#undoGame').click();
    },
    list: '.undoList',
    /** A place in the list: its button, its tier, what it is and why it is as it is. */
    point: '.undoPoint',
    pointButton: 'button',
    pointTier: '.undoTier',
    pointLabel: '.undoLabel',
    pointWhy: '.undoWhy',
    listNote: '.undoNote',
    declineAll: '#undoDeclineAll',
    closeList: '.ui-dialog:has(.undoList) .ui-dialog-titlebar-close',
    /** A request waiting for an answer: the asker's and the asked player's dialog. */
    request: '.undoRequest',
    requestButton: (label) => `.ui-dialog:has(.undoRequest) .ui-dialog-buttonpane button:has(:text-is("${label}"))`,
    requestButtons: '.ui-dialog:has(.undoRequest) .ui-dialog-buttonpane button',
    banner: '.undoBanner',
    /** The decision's own dialog (not undo's). */
    decisionDialog: '.ui-dialog:visible:not(:has(.undoList)):not(:has(.undoRequest))',
    decisionCard: '.ui-dialog:visible:not(:has(.undoList)):not(:has(.undoRequest)) .card',
    decisionButton: (label) =>
      `.ui-dialog:visible:not(:has(.undoList)):not(:has(.undoRequest)) :is(button:text-is("${label}"), button:has(:text-is("${label}")))`,
    chat: '#chatBox',
  },
};
const ui = UNDO[KIND];
if (!ui) {
  throw new Error(`--page ${KIND}: only ${Object.keys(UNDO).join(', ')} has undo`);
}

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

/** One player's page: the page, how often it was loaded, and what went wrong on it. */
class Seat {
  constructor(name, page) {
    this.name = name;
    this.page = page;
    this.loads = 0;
    this.errors = [];
    page.on('load', () => this.loads++);
    page.on('pageerror', (error) => this.errors.push(`${name}: page error: ${error.message}`));
    // The browser's own dialogs (the page asks before conceding, say) are accepted
    page.on('dialog', (dialog) => dialog.accept());
  }

  state() {
    return this.page.evaluate(readGameState, { kind: KIND });
  }

  /** The cards on the table (not the ones a decision's dialog offers, whose ids are temp0, temp1, ...). */
  async cards() {
    return (await this.state()).cards.filter((card) => !card.startsWith('temp'));
  }

  /** Clicks a card the decision offers and waits for it to be on the table. */
  async play(index = 0) {
    const before = (await this.cards()).length;
    await this.pick(index);
    await until(async () => (await this.cards()).length > before, `${this.name}'s card to go down`);
    await sleep(500);
  }

  /** The title of the decision's dialog, if one is open. */
  async decision() {
    const dialog = this.page.locator(ui.decisionDialog).first();
    if (!(await dialog.isVisible().catch(() => false))) return null;
    return ((await dialog.locator('.ui-dialog-title').textContent()) ?? '').trim();
  }

  hasButton(label) {
    return this.page.locator(ui.decisionButton(label)).first().isVisible();
  }

  async click(label) {
    await this.page.locator(ui.decisionButton(label)).first().click();
  }

  offered() {
    return this.page.locator(ui.decisionCard).count();
  }

  /** Clicks a card the decision offers (with auto-accept, on by default, that answers a choice of one card). */
  async pick(index = 0) {
    await this.page.locator(ui.decisionCard).nth(index).click();
  }

  /** The places the list offers, the latest first: `{ button, enabled, tier, label, why }`. */
  async openList() {
    await ui.openList(this.page);
    await this.page.locator(ui.list).waitFor({ timeout: 10000 });
    return this.page.locator(`${ui.list} ${ui.point}`).evaluateAll(
      (rows, sel) =>
        rows.map((row) => ({
          button: row.querySelector(sel.pointButton)?.textContent.trim(),
          enabled: !row.querySelector(sel.pointButton)?.disabled,
          tier: row.querySelector(sel.pointTier)?.textContent.trim(),
          label: row.querySelector(sel.pointLabel)?.textContent.trim(),
          why: row.querySelector(sel.pointWhy)?.textContent.trim() ?? '',
        })),
      { pointButton: ui.pointButton, pointTier: ui.pointTier, pointLabel: ui.pointLabel, pointWhy: ui.pointWhy },
    );
  }

  async takeBack(index = 0) {
    await this.page.locator(`${ui.list} ${ui.point}`).nth(index).locator(ui.pointButton).click();
  }

  async closeList() {
    await this.page.locator(ui.closeList).click();
  }

  /** The request on the screen, if any: `{ text, tier, buttons }`. Read in one go: the dialog may close meanwhile. */
  request() {
    return this.page.evaluate(
      ({ request, tier, buttons }) => {
        const shown = (el) => el && el.getBoundingClientRect().width > 0;
        const dialog = Array.from(document.querySelectorAll(request)).find(shown);
        if (!dialog) return null;
        return {
          text: dialog.textContent.replace(/\s+/g, ' ').trim(),
          tier: dialog.querySelector(tier)?.textContent.trim() ?? '',
          buttons: Array.from(document.querySelectorAll(buttons), (button) => button.textContent.trim()),
        };
      },
      { request: ui.request, tier: ui.pointTier, buttons: ui.requestButtons },
    );
  }

  async answerRequest(label) {
    await this.page.locator(ui.requestButton(label)).click();
  }

  async banner() {
    const banner = this.page.locator(ui.banner).first();
    if (!(await banner.isVisible().catch(() => false))) return null;
    return ((await banner.textContent()) ?? '').replace(/\s+/g, ' ').trim();
  }

  async shot(check, step) {
    fs.mkdirSync(OUT, { recursive: true });
    await this.page.screenshot({ path: path.join(OUT, `${check}-${step}-${this.name}.png`) });
  }
}

let browser;

async function seat(game, who, name) {
  const { hostname } = new URL(server);
  const context = await browser.newContext({ viewport: { width: 1366, height: 768 } });
  const cookies = who.cookie.split('; ').map((text) => {
    const [cookie, ...value] = text.split('=');
    return { name: cookie, value: value.join('='), domain: hostname, path: '/' };
  });
  await context.addCookies([...cookies, { name: 'animation-speed', value: '4', domain: hostname, path: '/' }]);
  const page = await context.newPage();
  const made = new Seat(name, page);
  await page.goto(`${server}${ui.path}?gameId=${game.gameId}`);
  return made;
}

/**
 * A game up to the first starting fellowship: both bid nothing, whoever is asked goes first. Returns the player
 * choosing a fellowship (`first`), the other one (`second`), and what both pages showed just before any card was chosen.
 */
async function toFellowship(check) {
  const pair = pairOf(0);
  const game = await startGame(server, pair, password, 'main');
  // One page at a time: the players join in the same order every run
  const a = await seat(game, game.player, pair.player);
  await until(() => a.hasButton('OK'), `${pair.player}'s bid`);
  const b = await seat(game, game.foe, pair.foe);
  await until(() => b.hasButton('OK'), `${pair.foe}'s bid`);
  await a.click('OK');
  await b.click('OK');
  const first = await until(
    async () => ((await a.hasButton('Go first')) ? a : (await b.hasButton('Go first')) ? b : null),
    'the choice of who goes first',
  );
  const second = first === a ? b : a;
  await first.click('Go first');
  await until(async () => (await first.decision())?.startsWith('Starting fellowship'), 'the first starting fellowship');
  await sleep(500);
  return { check, game, first, second, start: { first: await first.cards(), second: await second.cards() } };
}

/** The first player's fellowship of one card, said to be complete; the second player has chosen a card too. */
async function bothHaveChosen(table) {
  const { first, second } = table;
  await first.play(0);
  table.beforeDone = { first: await first.cards(), second: await second.cards() };
  await first.click('Done');
  await until(
    async () => (await second.decision())?.startsWith('Starting fellowship'),
    "the second player's turn to choose",
  );
  await second.play(0);
}

const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);

const CHECKS = {
  /** A choice nobody else has seen or answered is taken back at once, without asking anybody. */
  async own(table, problems) {
    const { first, second, start } = table;
    const offered = await first.offered();
    await first.play(0);
    const points = await first.openList();
    await first.shot('own', '1-list');
    if (points.length !== 1) problems.push(`one place to go back to was expected, the list has ${points.length}`);
    const point = points[0];
    if (point && !(point.tier === 'Green' && point.button === 'Take back' && point.enabled)) {
      problems.push(`the player's own last choice should be Green with "Take back": ${JSON.stringify(point)}`);
    }
    const loads = { first: first.loads, second: second.loads };
    await first.takeBack(0);
    await until(async () => first.banner(), 'the page to say the game was taken back');
    await until(
      async () => (await first.decision())?.startsWith('Starting fellowship'),
      'the choice to be asked again',
    );
    await sleep(500);
    await first.shot('own', '2-taken-back');
    if (await first.request()) problems.push('a request was shown for a choice that needs nobody asked');
    if (!same(await first.cards(), start.first)) {
      problems.push(`the cards are not as before the choice: ${JSON.stringify(await first.cards())}`);
    }
    if ((await first.offered()) !== offered) {
      problems.push(`${offered} cards were offered before; now ${await first.offered()}`);
    }
    await until(async () => second.banner(), 'the other page to say the game was taken back');
    if (!same(await second.cards(), start.second)) problems.push("the other player's cards are not as before");
    table.note = `${KIND === 'legacy' ? 'page loads' : 'loads'}: ${first.loads - loads.first} (asker), ${second.loads - loads.second} (other)`;
    // ...and the game goes on from there
    await first.play(1);
  },

  /** Anything the opponent has answered since needs the opponent's leave: asked, shown with its tier, approved. */
  async approve(table, problems) {
    const { first, second } = table;
    await bothHaveChosen(table);
    const points = await first.openList();
    await first.shot('approve', '1-list');
    if (points.length !== 2) problems.push(`two places to go back to were expected, the list has ${points.length}`);
    for (const point of points) {
      if (point.tier === 'Green' || point.button !== 'Ask' || !point.enabled) {
        problems.push(`after the opponent's card, no place should be Green or taken at once: ${JSON.stringify(point)}`);
      }
    }
    await first.takeBack(0);
    const mine = await until(() => first.request(), "the asker's waiting dialog");
    const theirs = await until(() => second.request(), "the opponent's request dialog");
    await first.shot('approve', '2-waiting');
    await second.shot('approve', '2-asked');
    if (!same(mine.buttons, ['Withdraw the request'])) problems.push(`the asker's buttons: ${mine.buttons}`);
    if (!same(theirs.buttons, ['Approve', 'Decline', 'Decline all for this game'])) {
      problems.push(`the opponent's buttons: ${theirs.buttons}`);
    }
    if (theirs.tier !== points[0]?.tier) {
      problems.push(`the opponent is shown ${theirs.tier}, the asker was shown ${points[0]?.tier}`);
    }
    table.note = `tier ${theirs.tier}`;
    await second.answerRequest('Approve');
    await until(async () => first.banner(), 'the asker to be told the game was taken back');
    await until(async () => second.banner(), 'the opponent to be told the game was taken back');
    await until(
      async () => (await first.decision())?.startsWith('Starting fellowship'),
      'the choice to be asked again',
    );
    await sleep(500);
    await first.shot('approve', '3-taken-back');
    await second.shot('approve', '3-taken-back');
    if (!same(await first.cards(), table.beforeDone.first)) problems.push("the asker's cards are not as at that point");
    if (!same(await second.cards(), table.beforeDone.second))
      problems.push("the opponent's cards are not as at that point");
    if ((await first.request()) || (await second.request())) problems.push('a request is still on a screen');
  },

  /** Declined: the asker is told, nothing is taken back, the game goes on. */
  async decline(table, problems) {
    const { first, second } = table;
    await bothHaveChosen(table);
    const before = { first: await first.cards(), second: await second.cards() };
    const loads = { first: first.loads, second: second.loads };
    await first.openList();
    await first.takeBack(0);
    await until(() => second.request(), "the opponent's request dialog");
    await second.answerRequest('Decline');
    const banner = await until(() => first.banner(), 'the asker to be told');
    await first.shot('decline', '1-declined');
    if (!/declin/i.test(banner)) problems.push(`the asker is told: "${banner}"`);
    await until(async () => !(await first.request()) && !(await second.request()), 'the request to leave both screens');
    if (first.loads !== loads.first || second.loads !== loads.second) problems.push('a page was loaded again');
    if (!same(await first.cards(), before.first) || !same(await second.cards(), before.second)) {
      problems.push('the cards changed though the request was declined');
    }
    // The opponent's choice is still waiting, and is answered as ever
    await second.play(0);
  },

  /** "Decline all": this request and every later one that would need asking; the asker's list says so. */
  async 'decline-all'(table, problems) {
    const { first, second } = table;
    await bothHaveChosen(table);
    await first.openList();
    await first.takeBack(0);
    await until(() => second.request(), "the opponent's request dialog");
    await second.answerRequest('Decline all for this game');
    await until(() => first.banner(), 'the asker to be told');
    await until(async () => !(await first.request()), "the asker's waiting dialog to close");
    const points = await first.openList();
    await first.shot('decline-all', '1-list');
    for (const point of points) {
      if (point.enabled || !point.why) {
        problems.push(
          `with every request declined, a place that needs asking should be off, with the reason: ${JSON.stringify(point)}`,
        );
      }
    }
    table.note = points[0]?.why;
  },

  /** The asker thinks better of it: the opponent's dialog goes away, nothing is taken back. */
  async withdraw(table, problems) {
    const { first, second } = table;
    await bothHaveChosen(table);
    const before = await second.cards();
    await first.openList();
    await first.takeBack(0);
    await until(() => second.request(), "the opponent's request dialog");
    await until(() => first.request(), "the asker's waiting dialog");
    await first.answerRequest('Withdraw the request');
    await until(async () => !(await second.request()), "the opponent's request dialog to close");
    await until(async () => !(await first.request()), "the asker's waiting dialog to close");
    await second.shot('withdraw', '1-withdrawn');
    if (!same(await second.cards(), before)) problems.push('the cards changed though the request was withdrawn');
  },

  /** The opponent concedes while this player's decision waits: the page shows the end without being answered first. */
  async concede(table, problems) {
    const { first, game } = table;
    const who = first.name === pairOf(0).player ? game.foe : game.player;
    await who.post(`/game/${game.gameId}/concede`, {});
    try {
      await until(
        async () => /concession/i.test((await first.page.locator(ui.chat).textContent()) ?? ''),
        'the concession to show',
        10000,
      );
    } catch (error) {
      problems.push(`with a decision waiting, the page did not show the opponent conceding: ${error.message}`);
    }
    await first.shot('concede', '1-conceded');
    if ((await first.decision())?.startsWith('Starting fellowship')) {
      problems.push('the decision is still open though the game is over');
    }
  },
};

async function run(name) {
  const problems = [];
  let table;
  try {
    table = await toFellowship(name);
    await CHECKS[name](table, problems);
  } catch (error) {
    problems.push(error.message.split('\n')[0]);
    for (const who of [table?.first, table?.second]) {
      await who?.shot(name, 'failed').catch(() => undefined);
    }
  }
  if (table) {
    problems.push(...table.first.errors, ...table.second.errors);
    await endGame(table.game);
    await table.first.page.context().close();
    await table.second.page.context().close();
  }
  console.log(`${problems.length ? '  ✗' : '  ✓'} ${name}${table?.note ? ` (${table.note})` : ''}`);
  for (const problem of problems) console.log(`      ${problem}`);
  return problems.length === 0;
}

const names = Object.keys(CHECKS).filter((name) => ONLY.length === 0 || ONLY.includes(name));
browser = await launchBrowser();
let ok = 0;
try {
  for (const name of names) {
    if (await run(name)) ok++;
  }
} finally {
  await browser.close();
}
console.log(`${ok} of ${names.length} undo checks passed on the ${KIND} page. Screenshots: ${OUT}`);
process.exitCode = ok === names.length ? 0 : 1;
