// Authoring checks: what puzzles and sandboxes brought to the panel (see ../author.mjs), and what the other checks
// of them share: Decipher's puzzle 10 as a draft's parts, and the wait for a change to be in the draft. Each change
// made in the panel is read back from the server's draft (`state`) and the file's text (`source`), and the panel is
// looked at again after the page loads again. Nothing is saved.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

/** Decipher's puzzle 10, Defending the Keep (the engine's fixture), as the draft's parts: slots, pool, a solution. */
export const PUZZLE_10 = {
  kind: 'puzzle',
  opponent: { fallback: 'eager' },
  coverage: 'guided',
  options: { autoPass: 'fellowship, maneuver, archery, assignment, regroup', opponentDelayMs: 0 },
  cards: {
    freeps: {
      frodo: '2_102',
      ring: '4_1',
      eomer: '4_267',
      spear: '4_268',
      theoden: '4_365',
      mail: '4_284',
      pahs1: '1_308',
      pahs2: '1_308',
      lemenya: '1_232',
      twilightWorld: '1_228',
    },
    shadow: {
      oppFrodo: '2_102',
      troop1: '4_182',
      troop2: '4_182',
      troop3: '4_182',
      weapons: '4_211',
      engine: '5_60',
    },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  ring: { freeps: 'ring' },
  sites: { freeps: '4_326, 4_330, 4_339', shadow: '4_326, 4_330, 4_339' },
  pool: 'set:1-6',
  slots: {
    condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', label: 'a Rohan condition' },
    ally: { zone: 'support', filter: 'culture:ROHAN cardtype:ALLY', label: 'a Rohan ally' },
  },
  rules: { oneSided: true, forbid: 'shuffle' },
  setup: {
    startAt: { site: 3, phase: 'maneuver' },
    freeps: { characters: 'eomer, theoden', hand: 'pahs1, pahs2, lemenya, twilightWorld' },
    shadow: { support: 'weapons, engine', minions: 'troop1, troop2, troop3' },
    attach: { spear: 'eomer', mail: 'theoden' },
    wounds: { frodo: 5, eomer: 2, theoden: 1 },
    tokens: { weapons: 2 },
  },
  outcome: {
    winMessages: ['Every companion came through the archery.'],
    loseMessages: ['A companion fell to the crossbows.'],
    judge: {
      at: 'endOf(archery)',
      win: [
        { type: 'canSpot', filter: 'alias(frodo)' },
        { type: 'canSpot', filter: 'alias(eomer)' },
        { type: 'canSpot', filter: 'alias(theoden)' },
      ],
    },
  },
  solutions: [
    {
      name: 'published',
      note: 'Parapet makes the minion archery total -3; the two arrows left go on Aldor, whose home is site 3.',
      picks: { condition: '5_87', ally: '4_262' },
      answers: ['use(condition)', 'option(Make the minion archery total -3)', 'choose(ally)', 'choose(ally)'],
    },
  ],
};

/**
 * A new draft with a puzzle's parts (puzzle 10's unless said), its game played from the start with "Write down what I
 * do" off: on, the passes the game makes are written into the script as steps, which then answer the decisions the
 * solutions' answers are for (Check would say the published solution no longer solves it).
 */
export async function puzzleDraft(seat, title, top = PUZZLE_10) {
  await newDraft(seat, { title });
  await seat.call('settings', { recording: 'false' });
  await seat.call('edit', { patch: JSON.stringify({ top }), goto: '1' });
  await seat.openGame(String((await seat.state()).gameId));
}

/** The draft as its file would be written (GET source). */
export async function source(seat) {
  return (await (await seat.who.get('/scenarios/author/source')).json()).source;
}

/**
 * Does `act` in the panel and waits until the server's draft says what `done(state)` looks for (a fresh game or
 * not), then for the panel with nothing on its way; answers the state. `what` names it in a failure.
 */
export async function changed(seat, what, act, done) {
  await act();
  const state = await until(
    async () => {
      const now = await seat.state();
      return done(now) ? now : null;
    },
    what,
    20000,
  );
  await seat.settled();
  seat.log(what);
  return state;
}

/** Loads the page again and turns to a tab (the panel comes back on the tab it was on). */
export async function reloaded(seat, tab) {
  await seat.page.reload();
  await seat.settled();
  await seat.showTab(tab);
}

/** Whether two parts are the same. */
export const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);

/** Whether two maps have the same entries, in whatever order. */
const sameEntries = (a, b) => same(Object.entries(a ?? {}).sort(), Object.entries(b ?? {}).sort());

const tab = {
  forbid: (what) => `${ui.scenario} gd-author-check[data-forbid="${what}"] input`,
  pool: `${ui.scenario} input[data-part="pool"]`,
  poolVerdict: `${ui.scenario} [data-part="pool"] + .gd-author-filter__verdict`,
  poolWhy: `${ui.scenario} .gd-author-filter__why`,
  slot: (id) => `${ui.board} .gd-author-slot[data-slot="${id}"]`,
  solutions: `${ui.scenario} section[data-section="solutions"]`,
  solution: (name) => `${ui.scenario} .gd-author-solution[data-name="${name}"]`,
};

export const checks = {
  /** What the player may not do, and the pool: ticked and typed, on the server and in the file; one refused. */
  async 'puzzle-rules'({ seat }, problems) {
    await puzzleDraft(seat, 'Puzzle rules check');
    const page = seat.page;
    await seat.showTab('scenario');
    await page.locator(tab.pool).scrollIntoViewIfNeeded();
    const counted = await until(
      async () => /\d+ cards/.test(await page.locator(tab.poolVerdict).innerText()),
      "the pool's count",
    );
    if (!counted) problems.push('the pool has no count');
    let state = await changed(
      seat,
      'no revealing ticked',
      () => page.locator(tab.forbid('reveal')).check(),
      (now) => now.draft.rules?.forbid === 'shuffle, reveal',
    );
    if (!same(state.puzzle.forbid, ['shuffle', 'reveal'])) problems.push(`forbid is ${state.puzzle.forbid}`);
    if (!/forbid: shuffle, reveal/.test(await source(seat))) problems.push("the file's text lacks forbid");
    state = await changed(
      seat,
      'the pool typed',
      async () => {
        await page.locator(tab.pool).fill('set:1-5');
        await page.locator(tab.pool).press('Enter');
      },
      (now) => now.draft.pool === 'set:1-5',
    );
    if (!/pool: set:1-5/.test(await source(seat))) problems.push("the file's text lacks the pool");
    await page.locator(tab.pool).scrollIntoViewIfNeeded();
    await seat.shot('pool');
    // (a pool the server refuses: its words beside it, what was typed kept, the draft as it was)
    seat.refusalsExpected.push(/\/scenarios\/author\/edit/);
    await page.locator(tab.pool).fill('colour:red');
    await page.locator(tab.pool).press('Enter');
    await until(async () => (await page.locator(tab.poolWhy).count()) === 1, "the pool's refusal said");
    if (!/has no 'colour'/.test(await page.locator(tab.poolWhy).innerText())) problems.push('the refusal is not said');
    if ((await page.locator(tab.pool).inputValue()) !== 'colour:red') problems.push('the pool typed was not kept');
    if ((await seat.state()).draft.pool !== 'set:1-5') problems.push('the refused pool changed the draft');
    await seat.shot('refused');
    // (left, the field lets go of what was typed in it; with the focus in it, a reload would bring it back)
    await page.locator(tab.pool).blur();
    await reloaded(seat, 'scenario');
    if ((await page.locator(tab.pool).inputValue()) !== 'set:1-5')
      problems.push('the pool is not shown after a reload');
    if (!(await page.locator(tab.forbid('reveal')).isChecked())) problems.push('reveal is not ticked after a reload');
    await closeDraft(seat);
  },

  /** A slot's parts changed in the Board tab, one added with the solution's card for it and taken out again. */
  async 'puzzle-slots'({ seat }, problems) {
    await puzzleDraft(seat, 'Puzzle slots check');
    const page = seat.page;
    await seat.showTab('board');
    await page.locator(tab.slot('ally')).scrollIntoViewIfNeeded();
    if (!/Aldor/.test(await page.locator(`${tab.slot('ally')} .gd-author-slot__pick`).innerText())) {
      problems.push("the ally slot does not show the game's card");
    }
    await seat.shot('slots');
    let state = await changed(
      seat,
      'a label changed',
      async () => {
        await page.locator(`${tab.slot('ally')} input[data-part="slot-label"]`).fill('an ally of Rohan');
        await page.locator(`${tab.slot('ally')} input[data-part="slot-label"]`).press('Enter');
      },
      (now) => now.draft.slots?.ally?.label === 'an ally of Rohan',
    );
    state = await changed(
      seat,
      'the bound narrowed',
      async () => {
        const field = page.locator(`${tab.slot('condition')} input[data-part="slot-filter"]`);
        await field.fill('culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE');
        await field.press('Enter');
      },
      (now) => now.draft.slots?.condition?.filter === 'culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE',
    );
    const info = state.puzzle.slots.find((slot) => slot.id === 'condition');
    if (info?.bound !== 'set:1-6 culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE') {
      problems.push(`the slot's bound is ${info?.bound}`);
    }
    await until(
      async () =>
        /\d+ cards?/.test(await page.locator(`${tab.slot('condition')} .gd-author-filter__verdict`).innerText()),
      "the bound's count",
    );
    state = await changed(
      seat,
      'tokens it comes with',
      async () => {
        await page.locator(`${tab.slot('condition')} input[data-part="slot-tokens"]`).fill('2');
        await page.locator(`${tab.slot('condition')} input[data-part="slot-tokens"]`).press('Enter');
      },
      (now) => now.draft.slots?.condition?.tokens === 2,
    );
    if (state.puzzle.slots.find((slot) => slot.id === 'condition')?.comes.tokens !== 2) {
      problems.push('the slot does not come with its tokens');
    }
    // (a new slot in the hand, the solution's card for it found by name)
    await page.locator(`${ui.board} [data-part="new-slot-id"]`).fill('extra');
    await page.locator(`${ui.board} button[data-action="new-slot-pick"]`).click();
    await page.locator(`${ui.board} .gd-author-slots__add gd-author-card-search input`).fill('Gandalf, Friend');
    await page.locator(`${ui.board} .gd-author-search__result[data-id="1_72"]`).click();
    state = await changed(
      seat,
      'a slot added',
      () => page.locator(`${ui.board} button[data-action="add-slot"]`).click(),
      (now) => now.draft.slots?.extra === 'hand',
    );
    if (state.draft.solutions[0]?.picks?.extra !== '1_72') problems.push('the solution has no card for the new slot');
    const text = await source(seat);
    if (!/extra: hand/.test(text) || !/label: an ally of Rohan/.test(text) || !/extra: 1_72/.test(text)) {
      problems.push("the file's text lacks the slots' changes");
    }
    await reloaded(seat, 'board');
    await page.locator(tab.slot('extra')).scrollIntoViewIfNeeded();
    if ((await page.locator(`${tab.slot('ally')} input[data-part="slot-label"]`).inputValue()) !== 'an ally of Rohan') {
      problems.push('the label is not shown after a reload');
    }
    await seat.shot('added');
    state = await changed(
      seat,
      'the slot taken out',
      async () => {
        await page.locator(`${tab.slot('extra')} button[data-action="remove"]`).click();
        await page.locator(ui.confirmButton('ok')).click();
      },
      (now) => now.draft.slots?.extra === undefined,
    );
    if (state.draft.solutions[0]?.picks?.extra !== undefined) problems.push("the slot's card is still in the solution");
    await closeDraft(seat);
  },

  /** Solutions: one recorded and stopped, the published one played (judged won), renamed, used and deleted. */
  async 'puzzle-solutions'({ seat }, problems) {
    await puzzleDraft(seat, 'Puzzle solutions check');
    const page = seat.page;
    await seat.showTab('scenario');
    await page.locator(tab.solutions).scrollIntoViewIfNeeded();
    await seat.shot('listed');
    let state = await changed(
      seat,
      'a solution recorded',
      async () => {
        await page.locator(`${tab.solutions} input[data-part="solution-name"]`).fill('mine');
        await page.locator(`${tab.solutions} button[data-author="record"]`).click();
      },
      (now) => now.puzzle?.recording === 'mine',
    );
    if (
      !sameEntries(state.puzzle.solutions.find((one) => one.name === 'mine')?.picks, {
        condition: '5_87',
        ally: '4_262',
      })
    ) {
      problems.push("the recorded solution does not have the game's cards");
    }
    if ((await page.locator(`${ui.notices} [data-notice="recording"]`).count()) !== 1) {
      problems.push('the panel does not say it is recording');
    }
    await seat.shot('recording');
    state = await changed(
      seat,
      'recording stopped',
      () => page.locator(`${ui.notices} button[data-author="stop-recording"]`).click(),
      (now) => now.puzzle?.recording === null,
    );
    // (the engine's play goes as far as the opponent answering by its fallback: puzzle 10's, two of four answers)
    state = await changed(
      seat,
      'the published solution played',
      () => page.locator(`${tab.solution('published')} button[data-author="play"]`).click(),
      (now) => /^Played solution 'published'/.test(now.notice ?? '') && now.puzzle?.playingWith === 'published',
    );
    // (Check: the runner plays each solution, and the list says what it said of each)
    await page.locator(`${ui.scenario} button[data-author="check"]`).click();
    await until(
      async () => (await page.locator(`${tab.solution('published')} .gd-author-solution__verdict`).count()) === 1,
      "the runner's verdicts",
      60000,
    );
    await page.locator(tab.solution('published')).scrollIntoViewIfNeeded();
    const verdict = await page.locator(`${tab.solution('published')} .gd-author-solution__verdict`).innerText();
    if (!/^solved \(judged at endOf\(archery\): won\)/.test(verdict))
      problems.push(`the solution's verdict says "${verdict}"`);
    if ((await page.locator(`${tab.solution('mine')}[data-solved="no"]`).count()) !== 1) {
      problems.push('the solution with no answers is not said to fail');
    }
    await seat.shot('checked');
    state = await changed(
      seat,
      'a solution renamed',
      async () => {
        await page.locator(`${tab.solution('mine')} button[data-author="rename"]`).click();
        await page.locator(`${tab.solution('mine')} input[data-part="solution-rename"]`).fill('mine2');
        await page.locator(`${tab.solution('mine')} input[data-part="solution-rename"]`).press('Enter');
      },
      (now) => now.draft.solutions?.some((one) => one.name === 'mine2'),
    );
    if (!/name: mine2/.test(await source(seat))) problems.push("the file's text lacks the new name");
    state = await changed(
      seat,
      "a solution's answers written",
      async () => {
        await page.locator(`${tab.solution('mine2')} button[data-author="details"]`).click();
        const answers = page.locator(`${tab.solution('mine2')} textarea[data-part="solution-answers"]`);
        await answers.fill('use(condition)\nchoose(ally)');
        await answers.blur();
      },
      (now) =>
        same(now.draft.solutions?.find((one) => one.name === 'mine2')?.answers, ['use(condition)', 'choose(ally)']),
    );
    await changed(
      seat,
      "a solution's cards used",
      () => page.locator(`${tab.solution('mine2')} button[data-author="use"]`).click(),
      (now) => now.puzzle?.playingWith === 'mine2',
    );
    await reloaded(seat, 'scenario');
    if ((await page.locator(`${tab.solution('mine2')}[data-playing]`).count()) !== 1) {
      problems.push('the solution in use is not marked after a reload');
    }
    state = await changed(
      seat,
      'a solution deleted',
      async () => {
        await page.locator(`${tab.solution('mine2')} button[data-author="delete"]`).click();
        await page.locator(ui.confirmButton('ok')).click();
      },
      (now) =>
        same(
          now.draft.solutions?.map((one) => one.name),
          ['published'],
        ),
    );
    if (/mine2/.test(await source(seat))) problems.push("the file's text still has the deleted solution");
    await sleep(100);
    await closeDraft(seat);
  },
};
