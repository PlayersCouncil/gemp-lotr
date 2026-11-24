// Authoring checks: the Board tab (see ../author.mjs). Every check starts from a new draft's board (Frodo and the
// Ring for each seat, the stock path), gathers changes in the tab and submits them.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';
import { changed, reloaded, same, source } from './puzzles.mjs';

const board = {
  submit: `${ui.board} button[data-action=submit]`,
  reset: `${ui.board} button[data-action=reset]`,
  lines: `${ui.board} .gd-author-board-gathered__text`,
  where: (where) => `${ui.board} [data-where=${where}] input`,
  counter: (name) => `${ui.board} [data-counter=${name}]`,
  card: (alias) => `${ui.board} .gd-author-board-card[data-alias="${alias}"]`,
  site: (number) => `${ui.board} .gd-author-board-path__site[data-seat=freeps][data-site="${number}"]`,
  add: `${ui.board} gd-author-board-add`,
  problem: `${ui.notices} .gd-author-notice[data-notice=problem]`,
};

/** Whether `read()` comes to be so within a few seconds (the page draws a change on its next frame). */
const soon = (read) =>
  until(read, '', 3000).then(
    () => true,
    () => false,
  );

/** A new draft, its game's page on the Board tab. */
async function onBoard(seat) {
  await newDraft(seat);
  await seat.showTab('board');
  await seat.page.locator(board.submit).waitFor({ timeout: 10000 });
}

/** What the gathered list says, a line each. */
async function gathered(page) {
  return (await page.locator(board.lines).allInnerTexts()).map((line) => line.trim());
}

/** Waits for a fresh game (the page loads again on it), and for the panel to be back on it. */
async function freshGame(seat, from, what) {
  await until(() => seat.gameId() !== from, `a fresh game after ${what}`, 25000);
  await seat.settled();
  await seat.page.locator(board.submit).waitFor({ timeout: 10000 });
}

/** Finds a card by name in the "Add a card" search and picks the first found. */
async function pickToAdd(page, query) {
  await page.locator(`${board.add} gd-author-card-search input`).fill(query);
  await page.locator(`${board.add} .gd-author-search__result`).first().click();
  await page.locator(`${board.add} .gd-author-board-add__chosen`).waitFor({ timeout: 5000 });
}

export const checks = {
  /** A card found by name and added to the hand of the setup: a fresh game, the card in it and in the draft. */
  async 'board-add'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(board.where('setup')).check();
    await pickToAdd(page, 'gandalf friend of the shirefolk');
    const alias = await page.locator(`${board.add} [data-field=alias]`).inputValue();
    if (alias !== 'gandalf') problems.push(`the name suggested is "${alias}", not gandalf`);
    await page.locator(`${board.add} button[data-action=add]`).click();
    await soon(async () => (await gathered(page)).length === 1);
    const lines = await gathered(page);
    if (lines[0] !== 'add Gandalf, Friend of the Shirefolk as gandalf (freeps, hand)') {
      problems.push(`gathered: ${JSON.stringify(lines)}`);
    }
    if ((await page.locator(`${board.card('gandalf')}[data-added]`).count()) !== 1)
      problems.push('no new row for gandalf');
    await seat.shot('gathered');
    const from = seat.gameId();
    await page.locator(board.submit).click();
    await freshGame(seat, from, 'submitting a card to the setup');
    const state = await seat.state();
    if (state.draft.cards?.freeps?.gandalf !== '1_72')
      problems.push(`the draft's cards: ${JSON.stringify(state.draft.cards)}`);
    if (!/\bgandalf\b/.test(state.draft.setup?.freeps?.hand ?? '')) {
      problems.push(`the draft's setup: ${JSON.stringify(state.draft.setup)}`);
    }
    const there = state.board.cards.find((card) => card.alias === 'gandalf');
    if (there?.zone !== 'hand') problems.push(`gandalf is ${there ? `in ${there.zone}` : 'not on the board'}`);
    const row = page.locator(board.card('gandalf'));
    if ((await row.count()) !== 1 || (await row.getAttribute('data-added')) !== null) {
      problems.push('the fresh game does not list gandalf as one of the cards');
    }
    if ((await gathered(page)).length !== 0) problems.push('the changes are still gathered after the submit');
    await seat.shot('added');
    await closeDraft(seat);
  },

  /** The twilight set on the board as it stands: the live table shows it, in the same game. */
  async 'board-now'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(`${board.counter('twilight')} input`).fill('3');
    await page.locator(`${board.counter('twilight')} button[data-action=set]`).click();
    if (!(await soon(async () => (await page.locator(`${board.counter('twilight')}[data-changed]`).count()) === 1))) {
      problems.push('the twilight is not marked changed once set');
    }
    const from = seat.gameId();
    await page.locator(board.submit).click();
    await until(
      async () => (await page.locator('[data-game=twilight]').innerText()).trim() === '3',
      'the twilight pool showing 3',
    );
    await seat.settled();
    if (seat.gameId() !== from) problems.push('a change to the board as it stands brought a fresh game');
    const state = await seat.state();
    if (state.board.twilight !== 3) problems.push(`the server's twilight is ${state.board.twilight}`);
    if (!state.pending.includes('twilight(3)')) problems.push(`pending: ${JSON.stringify(state.pending)}`);
    await seat.shot('twilight');
    await closeDraft(seat);
  },

  /** A card renamed (a fresh game): what was gathered about it says the new name. */
  async 'board-rename'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(`${board.card('frodo')} button[data-action=wound]`).click();
    await page.locator(`${board.card('frodo')} button[data-action=rename]`).click();
    const field = page.locator(`${board.card('frodo')} gd-author-board-rename input`);
    await field.fill('ringo bearer');
    await field.press('Enter');
    if (!(await soon(async () => (await field.getAttribute('data-bad')) !== null))) {
      problems.push('a name with a space was not marked bad');
    }
    await field.fill('hobbit');
    const from = seat.gameId();
    await field.press('Enter');
    await freshGame(seat, from, 'renaming a card');
    const state = await seat.state();
    if (!state.board.cards.some((card) => card.alias === 'hobbit'))
      problems.push('the board has no card called hobbit');
    if (state.draft.ringBearer?.freeps !== 'hobbit') {
      problems.push(`the Ring-bearer is ${JSON.stringify(state.draft.ringBearer)}`);
    }
    const lines = await gathered(page);
    if (JSON.stringify(lines) !== JSON.stringify(['wound(hobbit, 1)']))
      problems.push(`gathered: ${JSON.stringify(lines)}`);
    await seat.shot('renamed');
    await closeDraft(seat);
  },

  /** A site of the path changed by name: both seats' paths, at once, in a fresh game. */
  async 'board-path'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(`${board.site(2)} button[data-action=change]`).click();
    await page.locator(`${board.site(2)} gd-author-card-search input`).fill('bree streets');
    const found = page.locator(`${board.site(2)} .gd-author-search__result`).first();
    await found.waitFor({ timeout: 5000 });
    await seat.shot('site-search');
    const from = seat.gameId();
    await found.click();
    await freshGame(seat, from, 'changing a site');
    const { sites } = (await seat.state()).board;
    if (sites.freeps[1]?.id !== '1_328' || sites.shadow[1]?.id !== '1_328') {
      problems.push(`site 2 is ${sites.freeps[1]?.id} / ${sites.shadow[1]?.id}, not 1_328 for both`);
    }
    const name = await page.locator(`${board.site(2)} .cardHint`).innerText();
    if (name.trim() !== 'Bree Streets') problems.push(`the tab says site 2 is "${name.trim()}"`);
    await closeDraft(seat);
  },

  /** Changes gathered are kept when the page loads again, and Reset lets go of them. */
  async 'board-keep'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    await page.locator(`${board.card('frodo')} button[data-action=wound]`).click();
    await page.locator(`${board.counter('burdens')} input`).fill('2');
    await page.locator(`${board.counter('burdens')} button[data-action=set]`).click();
    await page.locator(`${board.card('oppRing')} button[data-action=drop]`).click();
    await soon(async () => (await gathered(page)).length === 3);
    const before = await gathered(page);
    await page.reload();
    await seat.settled();
    await seat.page.locator(board.submit).waitFor({ timeout: 10000 });
    await soon(async () => (await gathered(page)).length === 3);
    const after = await gathered(page);
    if (before.length !== 3 || JSON.stringify(after) !== JSON.stringify(before)) {
      problems.push(`gathered ${JSON.stringify(before)}, after loading again ${JSON.stringify(after)}`);
    }
    if ((await page.locator(board.submit).innerText()).trim() !== 'Submit changes (3)')
      problems.push('Submit does not count 3');
    if ((await page.locator(`${board.card('oppRing')}[data-removing]`).count()) !== 1) {
      problems.push('oppRing is not shown as to be taken out after loading again');
    }
    await seat.shot('kept');
    await page.locator(board.reset).click();
    if (!(await soon(async () => (await gathered(page)).length === 0))) problems.push('Reset left changes gathered');
    if (!(await page.locator(board.submit).isDisabled())) problems.push('Submit is not disabled after Reset');
    const kept = await page.evaluate(() => JSON.parse(sessionStorage.getItem('gemp-author-ui') ?? '{}').board ?? null);
    if (kept !== null) problems.push(`the panel still remembers ${JSON.stringify(kept)}`);
    await closeDraft(seat);
  },

  /**
   * A change refused part-way through a submit. To the setup: the card went (a fresh game), the board's changes were
   * refused and stay gathered, the problem said on the fresh game's page. To the board as it stands: the rest is let
   * go of, and the panel says some were made.
   */
  async 'board-refused'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    seat.refusalsExpected.push(/\/scenarios\/author\/do$/);
    const refuse = (route) =>
      route.fulfill({
        status: 400,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Refused by the check.' }),
      });
    await page.route('**/scenarios/author/do', refuse, { times: 1 });
    await page.locator(board.where('setup')).check();
    await pickToAdd(page, 'gandalf friend of the shirefolk');
    await page.locator(`${board.add} button[data-action=add]`).click();
    await page.locator(`${board.counter('burdens')} input`).fill('3');
    await page.locator(`${board.counter('burdens')} button[data-action=set]`).click();
    const from = seat.gameId();
    await page.locator(board.submit).click();
    await freshGame(seat, from, 'the cards of a submit');
    const said = await page
      .locator(board.problem)
      .innerText()
      .catch(() => '');
    if (!said.includes('Refused by the check.')) problems.push(`the fresh game's page says "${said.trim()}"`);
    const lines = await gathered(page);
    if (JSON.stringify(lines) !== JSON.stringify(['burdens(3)']))
      problems.push(`gathered after the refusal: ${JSON.stringify(lines)}`);
    if ((await seat.state()).draft.cards?.freeps?.gandalf !== '1_72') problems.push('the card did not go in');
    await seat.shot('setup-refused');

    await page.locator(board.reset).click();
    await page.locator(board.where('now')).check();
    await page.route('**/scenarios/author/do', refuse, { times: 1 });
    await page.locator(`${board.card('frodo')} button[data-action=wound]`).click();
    await page.locator(`${board.counter('burdens')} input`).fill('2');
    await page.locator(`${board.counter('burdens')} button[data-action=set]`).click();
    const here = seat.gameId();
    await page.locator(board.submit).click();
    await until(async () => (await page.locator(board.problem).count()) === 1, 'the problem said');
    await sleep(300);
    const now = await page.locator(board.problem).innerText();
    if (!now.includes('Some changes to the board were made, and the rest were not: Refused by the check.')) {
      problems.push(`refused now, the panel says "${now.trim()}"`);
    }
    if (seat.gameId() !== here) problems.push('a refused change to the board as it stands went to another game');
    if ((await gathered(page)).length !== 0) problems.push('the changes to the board as it stands stayed gathered');
    await seat.shot('now-refused');
    await closeDraft(seat);
  },

  /**
   * A modifier put on Frodo with mod… to the board as it stands (in force at once, carried by a step), one to the
   * setup (setup.modifiers, a fresh game), shown after a reload, and taken off again with its ×.
   */
  async 'board-modifiers'({ seat }, problems) {
    await onBoard(seat);
    const page = seat.page;
    const frodo = board.card('frodo');
    await page.locator(`${frodo} button[data-action=modify]`).click();
    await page.locator(`${frodo} gd-author-select[data-control=what] select`).selectOption('strength');
    await page.locator(`${frodo} input[data-control=amount]`).fill('+2');
    await page.locator(`${frodo} gd-author-select[data-control=until] select`).selectOption('turn');
    await page.locator(`${frodo} button[data-action=add]`).click();
    const lines = await gathered(page);
    if (!same(lines, ['modify(frodo, strength, +2, turn)'])) problems.push(`gathered: ${JSON.stringify(lines)}`);
    await seat.shot('gathered');
    let state = await changed(
      seat,
      'the modifier to the board as it stands',
      () => page.locator(board.submit).click(),
      (now) => now.board?.cards.find((card) => card.alias === 'frodo')?.modifiers?.length === 1,
    );
    const on = state.board.cards.find((card) => card.alias === 'frodo').modifiers[0];
    if (on.written !== 'strength +2 turn') problems.push(`Frodo has ${JSON.stringify(on)}`);
    if (
      !JSON.stringify([...state.pending, ...state.draft.steps.map((step) => step.do ?? '')]).includes(
        'modify(frodo, strength, +2, turn)',
      )
    ) {
      problems.push('no step carries the modifier');
    }
    await page.locator(board.where('setup')).check();
    await page.locator(`${frodo} button[data-action=modify]`).click();
    await page.locator(`${frodo} gd-author-select[data-control=what] select`).selectOption('archer');
    await page.locator(`${frodo} button[data-action=add]`).click();
    state = await changed(
      seat,
      'the modifier to the setup',
      () => page.locator(board.submit).click(),
      (now) => now.draft.setup?.modifiers?.frodo === 'archer',
    );
    if (!/frodo: archer/.test(await source(seat))) problems.push("the file's text lacks setup.modifiers");
    await reloaded(seat, 'board');
    const chips = await page
      .locator(`${frodo} .gd-author-mods__chip`)
      .evaluateAll((all) => all.map((one) => one.getAttribute('data-what')));
    if (!same(chips, ['archer'])) problems.push(`at the setup, Frodo shows ${JSON.stringify(chips)}`);
    await seat.shot('setup');
    state = await changed(
      seat,
      'the setup modifier taken off',
      async () => {
        await page.locator(`${frodo} .gd-author-mods__chip[data-what=archer] button[data-action=unmodify]`).click();
        await page.locator(board.submit).click();
      },
      (now) => now.draft.setup?.modifiers === undefined,
    );
    await page.locator(board.where('now')).check();
    await closeDraft(seat);
  },
};
