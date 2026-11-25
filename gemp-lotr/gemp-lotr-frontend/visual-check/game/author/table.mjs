// Authoring checks: the author's own table (see ../author.mjs). With the panel's Edit table on, the on-table edit
// controls (the open spots, their chooser and search window, the card menu, the path's +, the counters) change the
// draft through the Board tab's own service: each change is asserted on the server's draft (GET scenarios/author/state)
// and on the file's text (GET source) after a reload, and the text is compared with the same change made through the
// Board tab's forms in a second draft. Then a puzzle's slot opened on the table, and the panel's loose ends from the
// engine's last round (a verdict per solution, "Write down what I do" in a puzzle, a bound checked as it is typed,
// who plays a sandbox's other seat, the Ring-bearer's and the Ring's rows).

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';
import { changed, PUZZLE_10, puzzleDraft, reloaded, same, source } from './puzzles.mjs';

const edit = {
  toggle: `${ui.panel} button[data-author="edit-table"]`,
  bar: 'gd-author-edit-bar',
  barMode: 'gd-author-edit-bar .gd-edit-bar__mode',
  chip: (where) => `gd-author-edit-bar button[data-where="${where}"]`,
  spot: (seat, zone) => `gd-open-spot button.gd-open-spot[data-seat="${seat}"][data-zone="${zone}"]`,
  spots: 'gd-open-spot button.gd-open-spot',
  chooserItem: (kind, blueprint) =>
    `.gd-edit-chooser [data-chooser="${kind}"]${blueprint ? `[data-blueprint="${blueprint}"]` : ''}`,
  search: 'dialog[data-dialog="edit-search"][open]',
  menu: '.gd-edit-menu:not([data-submenu])',
  menuEntry: (name) => `.gd-edit-menu:not([data-submenu]) [data-menu="${name}"]`,
  submenu: (name) => `.gd-edit-menu[data-submenu="${name}"]`,
  card: (cardId) => `.gd-game__table [data-card-id="${cardId}"]`,
  decisionButtons: 'button[data-decision-button]',
  alert: '[data-game="alert"]',
  problem: '.gd-edit-problem',
  siteSpot: 'gd-game-path gd-site-spot button.gd-open-spot',
  siteItem: (kind, extra = '') => `.gd-edit-chooser[data-site-chooser] [data-site-chooser="${kind}"]${extra}`,
  editCounter: (name) => `gd-game-state button.gd-edit-counter[data-edit-counter="${name}"]`,
  counterBox: (name) => `.gd-edit-counter__box[data-counter="${name}"]`,
  slot: (id) => `gd-puzzle-slot .gd-puzzle-slot[data-slot="${id}"]`,
};

const tab = {
  submit: `${ui.board} button[data-action=submit]`,
  where: (where) => `${ui.board} [data-where=${where}] input`,
  card: (alias) => `${ui.board} .gd-author-board-card[data-alias="${alias}"]`,
  add: `${ui.board} gd-author-board-add`,
  counter: (name) => `${ui.board} [data-counter=${name}]`,
  pathSite: (number) => `${ui.board} .gd-author-board-start__row[data-part=path][data-site="${number}"] select`,
  startFold: `${ui.board} details.gd-author-board-start__fold`,
  slotForm: (id) => `${ui.board} .gd-author-slot[data-slot="${id}"]`,
  newSlotZone: `${ui.board} gd-author-select[data-part=new-slot-zone] select`,
  newSlotId: `${ui.board} input[data-part=new-slot-id]`,
  addSlot: `${ui.board} button[data-action=add-slot]`,
};

const GANDALF = '1_72';
const STING = '1_313';
/** The draft every card check starts from: Frodo and the Ring for each seat, Sting in support, Gandalf in hand. */
const CARDS = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', sting: STING, gandalf: GANDALF },
    shadow: { oppFrodo: '10_121', oppRing: '1_2' },
  },
  setup: { freeps: { support: 'sting', hand: 'gandalf' } },
};
/** A step that waits at the first decision: what is done "now" goes into its `do`. */
const IN_HAND = [{ when: 'freeps, action, phase(fellowship), turn(1)', answer: 'pass' }];

/** The file's text without what tells two drafts apart (their ids and titles). */
const plain = (text) =>
  text
    .split('\n')
    .filter((line) => !/^\s*(id|title):/.test(line))
    .join('\n');

/** Whether `read()` comes to be so within a few seconds. */
const soon = (read, max = 4000) =>
  until(read, '', max).then(
    () => true,
    () => false,
  );

/** Turns the panel's Edit table on (or off), and waits for the page to say so. */
async function editTable(seat, on = true) {
  const { page } = seat;
  if ((await page.locator(edit.toggle).getAttribute('aria-pressed')) !== String(on))
    await page.locator(edit.toggle).click();
  await until(
    async () => (await page.locator(ui.game).getAttribute('data-edit')) === (on ? 'edit' : 'play'),
    `the page ${on ? 'in' : 'out of'} edit mode`,
  );
}

/** Which board changes go to, with the table's chip (the same switch as the Board tab's). */
async function chooseWhere(seat, where) {
  await seat.page.locator(edit.chip(where)).click();
  await until(
    async () => (await seat.page.locator(edit.chip(where)).getAttribute('aria-pressed')) === 'true',
    `the chip on ${where}`,
  );
}

/** A card's id on the table now, by its alias. */
async function idOf(seat, alias) {
  const state = await seat.state();
  const card = state.board.cards.find((one) => one.alias === alias);
  if (!card) throw new Error(`the board has no ${alias}`);
  return card.cardId;
}

/** Right-clicks a card of the table by its alias and waits for its menu, placed. */
async function menuOf(seat, alias) {
  const { page } = seat;
  // (an attached card shows only its edge, beside its bearer: it is pressed there)
  await page
    .locator(edit.card(await idOf(seat, alias)))
    .first()
    .click({ button: 'right', force: true, position: { x: 4, y: 30 } });
  await page.locator(edit.menu).waitFor();
  await until(async () => (await page.locator(edit.menu).getAttribute('data-placing')) === null, 'the menu placed');
}

/** Opens a submenu of the open card menu by its opener. */
async function submenu(seat, name) {
  await seat.page.locator(edit.menuEntry(name)).click();
  await seat.page.locator(edit.submenu(name)).waitFor();
}

/**
 * Does a change on the table and waits for the server's draft to say it (`done(state)`), the page back on the draft's
 * game (a fresh one for a change to the start) with the table edited; answers the file's text.
 */
async function onTable(seat, what, act, done) {
  await changed(seat, what, act, done);
  await seat.page.locator(edit.toggle).waitFor();
  await until(async () => (await seat.page.locator(ui.game).getAttribute('data-edit')) === 'edit', 'edit mode back');
  return source(seat);
}

/** The same through the Board tab's forms: a change gathered, then Submit (`gathers` false: made at once). */
async function inTab(seat, what, act, done, gathers = true) {
  await changed(
    seat,
    what,
    async () => {
      await act();
      if (!gathers) return;
      await seat.page.locator(tab.submit).waitFor({ timeout: 5000 });
      await seat.page.locator(tab.submit).click();
    },
    done,
  );
  await seat.showTab('board');
  return source(seat);
}

/** Compares, change by change, the file's text the table made with the one the tab made. */
function compare(problems, names, fromTable, fromTab) {
  names.forEach((name, at) => {
    if (plain(fromTable[at] ?? '') !== plain(fromTab[at] ?? '')) {
      const a = plain(fromTable[at] ?? '').split('\n');
      const b = plain(fromTab[at] ?? '').split('\n');
      const line = a.findIndex((text, n) => text !== b[n]);
      problems.push(`${name}: the table wrote "${a[line]?.trim()}" where the tab writes "${b[line]?.trim()}"`);
    }
  });
}

/** The tab's "Add a card": found by name, put in a place of the seat, gathered. */
async function tabAdd(page, query, zone) {
  await page.locator(`${tab.add} gd-author-card-search input`).fill(query);
  await page.locator(`${tab.add} .gd-author-search__result`).first().click();
  await page.locator(`${tab.add} .gd-author-board-add__chosen`).waitFor({ timeout: 5000 });
  await page.locator(`${tab.add} gd-author-select[data-field=zone] select`).selectOption(zone);
  await page.locator(`${tab.add} button[data-action=add]`).click();
}

/** A spot's Search…: a name typed, the card found clicked. */
async function searchAt(seat, spot, name, blueprint) {
  const { page } = seat;
  await page.locator(spot).click();
  await page.locator(edit.chooserItem('search')).click();
  await page.locator(edit.search).waitFor();
  const field = page.locator(`${edit.search} .gd-field[data-field="name"] input`);
  await field.fill(name);
  await field.dispatchEvent('change');
  await page.locator(`${edit.search} .gd-edit-search__cards button.gd-card[data-blueprint="${blueprint}"]`).click();
}

const cardOf = (state, alias) => state.board?.cards.find((card) => card.alias === alias);
const handOf = (state) => state.draft.setup?.freeps?.hand ?? '';

/** The board changes, by the table's controls and by the tab's forms, for a draft at `where` (setup, now). */
function boardChanges(where) {
  const inSetup = where === 'setup';
  /** Where a change about a card is written: the setup's part, or the step in hand's `do`. */
  const did = (state, term) =>
    inSetup ? true : JSON.stringify(state.draft.steps.map((step) => step.do ?? '')).includes(term);
  return [
    {
      name: 'move',
      table: async (seat) => {
        await menuOf(seat, 'gandalf');
        await submenu(seat, 'send');
        await seat.page.locator(`${edit.submenu('send')} button[data-zone="characters"]`).click();
      },
      tab: (page) => page.locator(`${tab.card('gandalf')} select[data-control=move]`).selectOption('freeps.characters'),
      done: (state) =>
        inSetup
          ? /\bgandalf\b/.test(state.draft.setup?.freeps?.characters ?? '')
          : did(state, 'move(gandalf, freeps.characters)'),
    },
    {
      name: 'tokens',
      table: async (seat) => {
        await menuOf(seat, 'sting');
        await submenu(seat, 'add');
        await seat.page.locator(`${edit.submenu('add')} [data-culture="tokens"] button[data-token="add"]`).click();
      },
      tab: (page) => page.locator(`${tab.card('sting')} button[data-action=token]`).click(),
      done: (state) => (inSetup ? state.draft.setup?.tokens?.sting === 1 : did(state, 'tokens(sting, 1)')),
    },
    {
      name: 'attach',
      table: async (seat) => {
        await menuOf(seat, 'sting');
        await seat.page.locator(edit.menuEntry('attach')).click();
        await seat.page
          .locator(edit.card(await idOf(seat, 'frodo')))
          .first()
          .click();
      },
      tab: (page) => page.locator(`${tab.card('sting')} select[data-control=put]`).selectOption('attach|frodo'),
      done: (state) => (inSetup ? state.draft.setup?.attach?.sting === 'frodo' : did(state, 'attach(sting, frodo)')),
    },
    {
      name: 'wounds',
      table: async (seat) => {
        await menuOf(seat, 'frodo');
        await submenu(seat, 'add');
        await seat.page.locator(`${edit.submenu('add')} button[data-add="wound"]`).click();
      },
      tab: (page) => page.locator(`${tab.card('frodo')} button[data-action=wound]`).click(),
      done: (state) => (inSetup ? state.draft.setup?.wounds?.frodo === 1 : did(state, 'wound(frodo, 1)')),
    },
    {
      name: 'modify',
      table: async (seat) => {
        await menuOf(seat, 'frodo');
        await submenu(seat, 'modify');
        const menu = edit.submenu('modify');
        await seat.page.locator(`${menu} select[data-field="what"]`).selectOption('strength');
        await seat.page.locator(`${menu} input[data-field="amount"]`).fill('2');
        await seat.page.locator(`${menu} input[data-field="amount"]`).dispatchEvent('change');
        await seat.page.locator(`${menu} select[data-field="until"]`).selectOption('turn');
        await seat.page.locator(`${menu} button[data-action="put"]`).click();
      },
      tab: async (page) => {
        const frodo = tab.card('frodo');
        await page.locator(`${frodo} button[data-action=modify]`).click();
        await page.locator(`${frodo} gd-author-select[data-control=what] select`).selectOption('strength');
        await page.locator(`${frodo} input[data-control=amount]`).fill('+2');
        await page.locator(`${frodo} gd-author-select[data-control=until] select`).selectOption('turn');
        await page.locator(`${frodo} button[data-action=add]`).click();
      },
      done: (state) =>
        inSetup
          ? /strength \+2 turn/.test(JSON.stringify(state.draft.setup?.modifiers ?? {}))
          : did(state, 'modify(frodo, strength, +2, turn)'),
    },
    {
      name: 'counter',
      table: async (seat) => {
        const { page } = seat;
        await page.locator(edit.editCounter('twilight')).click();
        const field = page.locator(`${edit.counterBox('twilight')} input[data-field="set"]`);
        await field.fill('3');
        await page.locator(`${edit.counterBox('twilight')} button[data-action="set"]`).click();
      },
      tab: async (page) => {
        await page.locator(`${tab.counter('twilight')} input`).fill('3');
        await page.locator(`${tab.counter('twilight')} button[data-action=set]`).click();
      },
      done: (state) => (inSetup ? state.draft.setup?.twilight === 3 : did(state, 'twilight(3)')),
    },
  ];
}

/** Runs the board changes on the table, then in the tab of a second draft, and compares the file's text. */
async function boardRound(seat, problems, where) {
  const { page } = seat;
  const steps = boardChanges(where);
  const top = CARDS;
  await newDraft(seat, { title: 'Table check', top, steps: where === 'now' ? IN_HAND : [], goto: 1 });
  await editTable(seat);
  await chooseWhere(seat, where);
  if (where === 'setup') {
    await seat.showTab('board');
    if (!(await page.locator(tab.where('setup')).isChecked()))
      problems.push("the chip's Start is not the Board tab's choice too");
  }
  const fromTable = [];
  for (const step of steps) {
    fromTable.push(await onTable(seat, `${step.name} on the table`, () => step.table(seat), step.done));
    if (where === 'now' && (await seat.state()).pending.length > 0)
      problems.push(`${step.name}: left waiting for a step, not in the step in hand`);
  }
  await seat.shot(`table-${where}`);
  await reloaded(seat, 'board');
  const state = await seat.state();
  if (!steps.every((step) => step.done(state))) problems.push('after a reload the draft lacks a change');
  if ((await page.locator(edit.toggle).getAttribute('aria-pressed')) !== 'true')
    problems.push('after a reload Edit table is off');
  if ((await page.locator(edit.chip(where)).getAttribute('aria-pressed')) !== 'true')
    problems.push(`after a reload the chip is not on ${where}`);
  if (plain(await source(seat)) !== plain(fromTable.at(-1))) problems.push("after a reload the file's text differs");
  // The same through the Board tab's forms
  await newDraft(seat, { title: 'Table check (tab)', top, steps: where === 'now' ? IN_HAND : [], goto: 1 });
  await seat.showTab('board');
  await page.locator(tab.where(where)).check();
  const fromTab = [];
  for (const step of steps) fromTab.push(await inTab(seat, `${step.name} in the tab`, () => step.tab(page), step.done));
  compare(
    problems,
    steps.map((step) => step.name),
    fromTable,
    fromTab,
  );
  await page.locator(tab.where('now')).check();
}

export const checks = {
  /** Edit table: on, the page is in edit mode and nothing can be answered; it stays on across a reload; off again. */
  async 'table-toggle'({ seat }, problems) {
    const { page } = seat;
    await newDraft(seat, { title: 'Table toggle' });
    const before = await seat.state();
    const text = await source(seat);
    if ((await page.locator(edit.toggle).getAttribute('aria-pressed')) !== 'false')
      problems.push('Edit table starts on');
    if ((await page.locator(edit.decisionButtons).count()) === 0) problems.push('playing, no decision is shown');
    if ((await page.locator(edit.spots).count()) > 0) problems.push('playing, the table has open spots');
    await editTable(seat);
    await seat.shot('on');
    if ((await page.locator(edit.decisionButtons).count()) > 0) problems.push('editing, the decision buttons show');
    if ((await page.locator(edit.alert).innerText()).trim() !== '') problems.push('editing, the alert asks');
    if ((await page.locator(edit.spots).count()) < 4) problems.push('editing, the table has no open spots');
    if ((await page.locator(edit.barMode).innerText()).trim() !== 'Editing')
      problems.push('the bar does not say Editing');
    if ((await page.locator(edit.chip('now')).getAttribute('aria-pressed')) !== 'true')
      problems.push('the chip is not on Now (the Board tab’s default)');
    // (a decision's key does nothing while editing)
    await page.evaluate(() => document.activeElement?.blur());
    await page.keyboard.press('Enter');
    await sleep(500);
    const after = await seat.state();
    if (!same(after.position, before.position) || !same(after.waiting.human, before.waiting.human))
      problems.push('editing, the decision was answered');
    if ((await source(seat)) !== text) problems.push('turning Edit table on changed the file');
    await reloaded(seat, 'timeline');
    if ((await page.locator(ui.game).getAttribute('data-edit')) !== 'edit')
      problems.push('after a reload, not editing');
    if ((await page.locator(edit.toggle).getAttribute('aria-pressed')) !== 'true')
      problems.push('after a reload, Edit table is off');
    await editTable(seat, false);
    if (!(await soon(async () => (await page.locator(edit.decisionButtons).count()) > 0)))
      problems.push('off again, the decision does not come back');
    if ((await page.locator(edit.spots).count()) > 0) problems.push('off again, the open spots stay');
    await seat.shot('off');
    await closeDraft(seat);
  },

  /**
   * At the start: a card added by search, one added by the chooser (Recent), a copy, a card deleted; each in the
   * draft's cards and setup as the Board tab's Add a card and × write them.
   */
  async 'table-cards'({ seat }, problems) {
    const { page } = seat;
    await newDraft(seat, { title: 'Table cards' });
    await editTable(seat);
    await chooseWhere(seat, 'setup');
    const fromTable = [];
    fromTable.push(
      await onTable(
        seat,
        'Gandalf added to the hand by search',
        () => searchAt(seat, edit.spot('freeps', 'hand'), 'Friend of the Shirefolk', GANDALF),
        (state) => state.draft.cards?.freeps?.gandalf === GANDALF && /\bgandalf\b/.test(handOf(state)),
      ),
    );
    fromTable.push(
      await onTable(
        seat,
        'Gandalf added again, from the chooser',
        async () => {
          await page.locator(edit.spot('freeps', 'hand')).click();
          await page.locator(edit.chooserItem('recent', GANDALF)).click();
        },
        (state) => state.draft.cards?.freeps?.gandalf2 === GANDALF && /\bgandalf2\b/.test(handOf(state)),
      ),
    );
    fromTable.push(
      await onTable(
        seat,
        'Gandalf copied',
        async () => {
          await menuOf(seat, 'gandalf');
          await page.locator(edit.menuEntry('copy')).click();
        },
        (state) => state.draft.cards?.freeps?.gandalf3 === GANDALF && /\bgandalf3\b/.test(handOf(state)),
      ),
    );
    fromTable.push(
      await onTable(
        seat,
        'the second Gandalf deleted',
        async () => {
          await menuOf(seat, 'gandalf2');
          await page.locator(edit.menuEntry('delete')).click();
        },
        (state) => state.draft.cards?.freeps?.gandalf2 === undefined && !/\bgandalf2\b/.test(handOf(state)),
      ),
    );
    await seat.shot('cards');
    await reloaded(seat, 'board');
    const state = await seat.state();
    if (!cardOf(state, 'gandalf') || !cardOf(state, 'gandalf3') || cardOf(state, 'gandalf2'))
      problems.push(`after a reload the board has ${state.board.cards.map((card) => card.alias).join(', ')}`);
    if (plain(await source(seat)) !== plain(fromTable.at(-1))) problems.push("after a reload the file's text differs");
    // The same through the tab: Add a card (the copy is a card added where it is), and ×
    await newDraft(seat, { title: 'Table cards (tab)' });
    await seat.showTab('board');
    await page.locator(tab.where('setup')).check();
    const fromTab = [];
    for (const alias of ['gandalf', 'gandalf2', 'gandalf3']) {
      fromTab.push(
        await inTab(
          seat,
          `${alias} added in the tab`,
          () => tabAdd(page, 'Friend of the Shirefolk', 'hand'),
          (now) => now.draft.cards?.freeps?.[alias] === GANDALF,
        ),
      );
    }
    fromTab.push(
      await inTab(
        seat,
        'gandalf2 taken out in the tab',
        () => page.locator(`${tab.card('gandalf2')} button[data-action=drop]`).click(),
        (now) => now.draft.cards?.freeps?.gandalf2 === undefined,
      ),
    );
    compare(problems, ['add by search', 'add by chooser', 'copy', 'delete'], fromTable, fromTab);
    await page.locator(tab.where('now')).check();
    await closeDraft(seat);
  },

  /** At the start: a move, a token (the tab offers one in the support area), an attach, a wound, a modifier, the twilight, each in the setup as the tab writes it. */
  async 'table-start'({ seat }, problems) {
    await boardRound(seat, problems, 'setup');
    await closeDraft(seat);
  },

  /** Now: the same changes, each in the step in hand's `do` as the tab writes it. */
  async 'table-now'({ seat }, problems) {
    await boardRound(seat, problems, 'now');
    await closeDraft(seat);
  },

  /**
   * The path at the start: site 2 replaced by the Shadow seat's (setup.path), and the Free Peoples marker put on it
   * (setup.startAt), from the laid site's menu, as the tab's "Where the story starts" and "On the table" write them;
   * and a site laid by the path's + refused now, said where it was asked for.
   */
  async 'table-site'({ seat }, problems) {
    const { page } = seat;
    // (the start lays sites 1 and 2: the tab lists the places its path says)
    const top = { setup: { path: 'site1, site2' } };
    await newDraft(seat, { title: 'Table site', top });
    await editTable(seat);
    await page.locator(edit.siteSpot).click();
    await page.locator(edit.siteItem('decks')).click();
    await page.locator(edit.siteItem('deck', '[data-seat="shadow"]')).click();
    await page.locator(edit.siteItem('site', '[data-alias="site3opp"]')).click();
    if (!(await soon(async () => (await page.locator(edit.problem).count()) > 0)))
      problems.push('now, laying a site is not refused where it was asked');
    await page.keyboard.press('Escape');
    await chooseWhere(seat, 'setup');
    const site2 = page.locator('gd-game-path button.gd-card').nth(1);
    const siteMenu = async () => {
      await site2.click({ button: 'right' });
      await page.locator(edit.menu).waitFor();
    };
    const fromTable = [];
    fromTable.push(
      await onTable(
        seat,
        "site 2 replaced by the Shadow seat's at the start",
        async () => {
          await siteMenu();
          await page.locator(edit.menuEntry('replace')).click();
          await page.locator(edit.siteItem('decks')).click();
          await page.locator(edit.siteItem('deck', '[data-seat="shadow"]')).click();
          await page.locator(edit.siteItem('site', '[data-alias="site2opp"]')).click();
        },
        (state) => state.draft.setup?.path === 'site1, site2opp',
      ),
    );
    fromTable.push(
      await onTable(
        seat,
        'the Free Peoples marker on site 2 at the start',
        async () => {
          await siteMenu();
          await page.locator(`${edit.menu} [data-site-action="marker"][data-seat="freeps"]`).click();
        },
        (state) => state.draft.setup?.startAt?.site === 2,
      ),
    );
    await reloaded(seat, 'board');
    if (plain(await source(seat)) !== plain(fromTable.at(-1))) problems.push("after a reload the file's text differs");
    await seat.shot('site');
    await newDraft(seat, { title: 'Table site (tab)', top });
    await seat.showTab('board');
    await page.locator(tab.where('setup')).check();
    const fromTab = [];
    fromTab.push(
      await inTab(
        seat,
        'site 2 replaced in the tab',
        async () => {
          await page.locator(tab.startFold).evaluate((fold) => (fold.open = true));
          await page.locator(tab.pathSite(2)).selectOption('site2opp');
        },
        (state) => state.draft.setup?.path === 'site1, site2opp',
      ),
    );
    fromTab.push(
      await inTab(
        seat,
        'the Free Peoples marker on site 2 in the tab',
        () =>
          page
            .locator(
              `${ui.board} .gd-author-board-laid__site[data-site="2"] button[data-action=marker][data-seat=freeps]`,
            )
            .click(),
        (state) => state.draft.setup?.startAt?.site === 2,
      ),
    );
    compare(problems, ['site', 'marker'], fromTable, fromTab);
    await page.locator(tab.where('now')).check();
    await closeDraft(seat);
  },

  /**
   * A puzzle's slot opened on the table (Open slot in a hand's chooser): in the draft as the tab's Add slot writes it,
   * its form shown in the Board tab; given a bound there (the server's count beside it); drawn on the table as an open
   * slot, whose press shows its form again.
   */
  async 'table-slot'({ seat }, problems) {
    const { page } = seat;
    await newDraft(seat, { title: 'Table slot' });
    await seat.call('edit', { patch: JSON.stringify({ top: { kind: 'puzzle' } }), goto: '1' });
    await seat.openGame(String((await seat.state()).gameId));
    await editTable(seat);
    const fromTable = await onTable(
      seat,
      'a slot opened in the hand',
      async () => {
        await page.locator(edit.spot('freeps', 'hand')).click();
        await page.locator(edit.chooserItem('slot')).click();
      },
      (state) => state.draft.slots?.slot1 === 'hand',
    );
    await until(
      async () => (await page.locator(tab.slotForm('slot1')).count()) === 1,
      "the slot's form in the Board tab",
    );
    if (
      !(await soon(async () =>
        page
          .locator(`${tab.slotForm('slot1')} input[data-part=slot-label]`)
          .evaluate((field) => field === document.activeElement),
      ))
    )
      problems.push("the slot's label is not focused");
    if ((await page.locator(edit.slot('slot1')).count()) !== 1)
      problems.push('the open slot is not drawn on the table');
    await seat.shot('opened');
    const bound = page.locator(`${tab.slotForm('slot1')} input[data-part=slot-filter]`);
    await bound.fill('cardtype:COMPANION culture:GANDALF');
    const expected = await (
      await seat.who.get('/scenarios/author/bound?filter=cardtype%3ACOMPANION%20culture%3AGANDALF')
    ).json();
    const verdict = page.locator(`${tab.slotForm('slot1')} .gd-author-filter__verdict`);
    if (!(await soon(async () => (await verdict.innerText()).startsWith(`${expected.total} card`))))
      problems.push(`while typed, the bound's count is not the server's ${expected.total}`);
    await changed(
      seat,
      'the bound sent',
      () => bound.press('Enter'),
      (state) => state.draft.slots?.slot1?.filter === 'cardtype:COMPANION culture:GANDALF',
    );
    await reloaded(seat, 'timeline');
    await editTable(seat);
    await page
      .locator(`${edit.slot('slot1')} button`)
      .first()
      .click();
    if (!(await soon(async () => (await page.locator(`${ui.tab('board')}[aria-selected="true"]`).count()) === 1)))
      problems.push("the open slot's press does not show its form");
    const text = await source(seat);
    if (!/slot1: \{\s*zone: hand\s*filter: cardtype:COMPANION culture:GANDALF\s*\}/.test(text))
      problems.push("the file's text lacks the slot with its bound");
    await seat.shot('slot');
    // The tab's own Add slot writes the same slot
    await newDraft(seat, { title: 'Table slot (tab)' });
    await seat.call('edit', { patch: JSON.stringify({ top: { kind: 'puzzle' } }), goto: '1' });
    await seat.openGame(String((await seat.state()).gameId));
    await seat.showTab('board');
    await page.locator(tab.newSlotZone).selectOption('hand');
    await page.locator(tab.newSlotId).fill('slot1');
    const fromTab = await inTab(
      seat,
      'slot1 added in the tab',
      () => page.locator(tab.addSlot).click(),
      (state) => Boolean(state.draft.slots?.slot1),
      false,
    );
    compare(problems, ['open slot'], [fromTable], [fromTab]);
    await closeDraft(seat);
  },

  /**
   * The panel's loose ends: Check's verdict per solution; "Write down what I do" off in a puzzle until ticked; a
   * bound refused as it is typed, in the server's words; the Ring-bearer's and the Ring's rows; a sandbox's other
   * seat.
   */
  async 'table-loose'({ seat }, problems) {
    const { page } = seat;
    // "Write down what I do": off in a puzzle until said
    await newDraft(seat, { title: 'Loose ends', top: { kind: 'puzzle' } });
    let state = await seat.state();
    if (state.recording !== false || state.recordingSaid !== false)
      problems.push(`a new puzzle draft: recording ${state.recording}, said ${state.recordingSaid}`);
    if ((await page.locator('gd-game-author-timeline [data-note=recording]').count()) !== 1)
      problems.push('a puzzle does not say why "Write down what I do" is off');
    await changed(
      seat,
      'Write down what I do ticked',
      () => page.locator('gd-game-author-timeline gd-author-check[data-option=recording] input').check(),
      (now) => now.recording === true && now.recordingSaid === true,
    );
    if (!(await soon(async () => (await page.locator('gd-game-author-timeline [data-note=recording]').count()) === 0)))
      problems.push('ticked, the note stays');
    await reloaded(seat, 'timeline');
    if (!(await page.locator('gd-game-author-timeline gd-author-check[data-option=recording] input').isChecked()))
      problems.push('after a reload "Write down what I do" is unticked');
    await puzzleDraft(seat, 'Loose ends');
    // Check: a verdict per solution from its `solutions`
    await seat.showTab('scenario');
    state = await seat.state();
    const names = (state.puzzle?.solutions ?? []).map((one) => one.name);
    const checked = page.waitForResponse((response) => response.url().includes('/scenarios/author/check'), {
      timeout: 120000,
    });
    await page.locator(`${ui.scenario} button[data-author=check]`).click();
    const answer = await (await checked).json();
    await seat.settled(120000);
    for (const name of names) {
      const said = answer.solutions?.[name];
      const row = page.locator(`${ui.scenario} .gd-author-solution[data-name="${name}"] .gd-author-solution__verdict`);
      const shown = (await row.count()) === 1 ? (await row.innerText()).trim() : null;
      const playing = state.puzzle?.playingWith === name && state.puzzle?.verdict;
      if (!playing && shown !== (said === null ? 'solved' : said))
        problems.push(`solution ${name}: shows "${shown}", Check said ${JSON.stringify(said)}`);
    }
    // A bound typed: refused in the server's words before it is sent
    await seat.showTab('board');
    const slot = state.puzzle?.slots?.[0]?.id;
    if (slot) {
      const field = page.locator(`${tab.slotForm(slot)} input[data-part=slot-filter]`);
      await field.fill('colour:red');
      const why = page.locator(`${tab.slotForm(slot)} .gd-author-filter__why`);
      if (!(await soon(async () => /no 'colour'/.test(await why.innerText()))))
        problems.push('a bound with an unknown key is not refused as it is typed');
      await field.fill(state.draft.slots?.[slot]?.filter ?? '');
      await field.press('Escape');
    }
    // The Ring-bearer's and the Ring's rows
    if ((await page.locator(`${tab.card('frodo')} [data-tag=ring-bearer]`).count()) !== 1)
      problems.push("Frodo's row does not say Ring-bearer");
    const why = await page.locator(`${tab.card('frodo')} button[data-action=drop]`).getAttribute('title');
    if (!/Ring-bearer: the server takes it out only/.test(why ?? ''))
      problems.push("the Ring-bearer's × does not say why the server would refuse it");
    if ((await page.locator(`${tab.card('ring')} [data-tag=ring]`).count()) !== 1)
      problems.push("the Ring's row does not say Ring");
    await seat.shot('loose');
    // A sandbox's other seat
    await newDraft(seat, { title: 'Other seat', top: { kind: 'sandbox' } });
    await seat.showTab('scenario');
    const select = page.locator(`${ui.scenario} gd-author-select[data-part=other-seat] select`);
    await changed(
      seat,
      'the other seat played by the person',
      () => select.selectOption('person'),
      (now) => now.draft.options?.otherSeat === 'person' && now.sandboxOtherSeat?.value === 'person',
    );
    if (!/otherSeat: person/.test(await source(seat))) problems.push("the file's text lacks otherSeat: person");
    await reloaded(seat, 'scenario');
    if ((await select.inputValue()) !== 'person') problems.push('after a reload the other seat is not the person');
    await seat.shot('other-seat');
    await changed(
      seat,
      'the other seat played by the script',
      () => select.selectOption('script'),
      (now) => now.draft.options?.otherSeat === undefined,
    );
    await closeDraft(seat);
  },
};
