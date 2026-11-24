// Authoring checks: the Scenario tab's endings (see ../author.mjs). The outcome is sent whole; what is written there
// is read back from the server's draft and file, and what a person ending the game is told is seen on the table.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';
import { changed, reloaded } from './puzzles.mjs';

/** Frodo, three wounds on him, alone against a Goblin Runner the opponent plays and assigns to him. */
const DEATH = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2' },
    shadow: { oppFrodo: '10_121', oppRing: '1_2', orc: '1_178' },
  },
  setup: { shadow: { hand: 'orc' }, wounds: { frodo: 3 }, twilight: 4 },
  opponent: {
    fallback: 'pass',
    leftovers: 'ringBearer',
    orders: [
      { when: 'shadow, action, phase(shadow)', answer: 'play(orc)' },
      { when: 'shadow, action', answer: 'pass' },
    ],
  },
};

/** The player passes all along; Frodo loses his skirmish and dies: the game is lost in its first turn. */
const TO_DEATH = [
  { when: 'freeps, action, phase(fellowship), turn(1)', answer: 'pass' },
  ...['maneuver', 'archery', 'assignment'].map((phase) => ({
    when: `freeps, action, phase(${phase})`,
    answer: 'pass',
  })),
  { when: 'freeps, assign', answer: 'pass' },
  { when: 'freeps, cards, nextSkirmish', answer: 'choose(frodo)' },
  { when: 'freeps, action, phase(skirmish)', answer: 'pass' },
  { when: 'freeps, action, text(about to)', answer: 'pass' },
];

/** An outcome with every part set. */
const FULL = {
  win: { type: 'canSpot', filter: 'name(Sam)' },
  lose: { type: 'hasCardInDeadPile', filter: 'companion' },
  loseMessage: 'Alas.',
  offScript: { message: 'Off the path.', result: 'fail' },
  winMessages: ['Won.'],
  loseMessages: [{ header: 'Lost', text: 'Lost.' }],
  gameWon: { result: 'complete', messages: ['Hurrah.', { header: 'Well done', text: 'Again?' }] },
  gameLost: 'complete',
};

const tab = {
  ending: (kind) => `${ui.scenario} .gd-author-ending[data-ending="${kind}"]`,
  message: (kind, index, part) =>
    `${ui.scenario} .gd-author-ending[data-ending="${kind}"] li[data-index="${index}"] [data-part="${part}"]`,
  add: (kind) => `${ui.scenario} .gd-author-ending[data-ending="${kind}"] button[data-author="add-message"]`,
  endPanel: '.gd-scenario-panel[data-panel="end"]',
};

async function source(seat) {
  return (await (await seat.who.get('/scenarios/author/source')).json()).source;
}

/** Does `change` (which brings a fresh game) and waits for the panel on the new game, back on the Scenario tab. */
async function freshGame(seat, what, change) {
  const before = seat.gameId();
  await change();
  await until(() => seat.gameId() !== before, `a fresh game after ${what}`, 20000);
  await seat.settled();
  if ((await seat.page.locator(`${ui.tab('scenario')}[aria-selected="true"]`).count()) !== 1) {
    throw new Error(`the fresh game after ${what} did not come back on the Scenario tab`);
  }
  seat.log(`fresh game after ${what}`);
}

const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);

export const checks = {
  /** "Game lost" given a heading and a text in the tab: the draft has them, and the end panel says them. */
  async 'scenario-game-lost'({ seat }, problems) {
    await newDraft(seat, { title: 'Endings check', top: DEATH, steps: TO_DEATH, goto: TO_DEATH.length + 1 });
    const page = seat.page;
    const over = await seat.state();
    if (over.position.result !== 'failed') throw new Error(`the draft's game is ${over.position.result}, not lost`);
    await seat.showTab('scenario');
    await page.locator(tab.ending('gameLost')).scrollIntoViewIfNeeded();
    await page.locator(tab.add('gameLost')).click();
    // (a heading alone is not sent: the message goes with its text)
    await page.locator(tab.message('gameLost', 0, 'header')).fill('The end');
    await freshGame(seat, 'the closing message', async () => {
      await page.locator(tab.message('gameLost', 0, 'text')).fill('Frodo has fallen.');
      await page.locator(tab.message('gameLost', 0, 'text')).blur();
    });
    const state = await seat.state();
    const wanted = { result: 'fail', messages: [{ header: 'The end', text: 'Frodo has fallen.' }] };
    if (!same(state.draft.outcome?.gameLost, wanted)) {
      problems.push(`the draft's gameLost is ${JSON.stringify(state.draft.outcome?.gameLost)}`);
    }
    const text = await source(seat);
    if (!text.includes('Frodo has fallen.') || !text.includes('gameLost')) problems.push("the file's text lacks it");
    const panel = page.locator(tab.endPanel);
    await panel.waitFor({ timeout: 15000 });
    const said = await panel.innerText();
    if (!said.includes('The end') || !said.includes('Frodo has fallen.')) {
      problems.push(`the end panel says "${said.replace(/\s+/g, ' ')}"`);
    }
    if ((await panel.getAttribute('data-result')) !== 'failed') problems.push('the end panel is not a failure');
    await seat.shot('end-panel');
    await page.reload();
    await seat.settled();
    await seat.showTab('scenario');
    if ((await page.locator(tab.message('gameLost', 0, 'text')).inputValue()) !== 'Frodo has fallen.') {
      problems.push('the closing message is not in the tab after a reload');
    }
    await closeDraft(seat);
  },

  /** An outcome with every part set keeps them all when one message is changed in the tab. */
  async 'scenario-outcome-kept'({ seat }, problems) {
    await newDraft(seat, { title: 'Outcome check', top: { outcome: FULL } });
    const page = seat.page;
    const before = (await seat.state()).draft.outcome;
    await seat.showTab('scenario');
    await page.locator(tab.ending('gameWon')).scrollIntoViewIfNeeded();
    if ((await page.locator(`${tab.ending('gameLost')} select`).inputValue()) !== 'complete') {
      problems.push('Game lost is not shown counting as complete');
    }
    await seat.shot('full');
    await freshGame(seat, 'a message changed', async () => {
      await page.locator(tab.message('gameWon', 1, 'text')).fill('Once more?');
      await page.locator(tab.message('gameWon', 1, 'text')).press('Tab');
    });
    const after = (await seat.state()).draft.outcome;
    const wanted = {
      ...before,
      gameWon: { result: 'complete', messages: ['Hurrah.', { header: 'Well done', text: 'Once more?' }] },
    };
    if (!same(after, wanted)) problems.push(`the outcome is ${JSON.stringify(after)}`);
    for (const part of ['win', 'lose', 'loseMessage', 'offScript', 'winMessages', 'loseMessages', 'gameLost']) {
      if (!same(after?.[part], before?.[part]))
        problems.push(`outcome.${part} changed: ${JSON.stringify(after?.[part])}`);
    }
    await sleep(200);
    await closeDraft(seat);
  },

  /**
   * Judged at a moment (end of archery, turn 2) with its requirements typed, then scored by a card's strength, lower
   * being better, with a label, then by a custom value: on the server, in the file, and in the tab after a reload.
   */
  async 'scenario-judge-score'({ seat }, problems) {
    await newDraft(seat, { title: 'Judge check', top: { kind: 'puzzle', outcome: { winMessages: ['Won.'] } } });
    const page = seat.page;
    await seat.showTab('scenario');
    const judging = `${ui.scenario} gd-game-author-scenario-judging`;
    await page.locator(judging).scrollIntoViewIfNeeded();
    await changed(
      seat,
      'a moment chosen',
      () => page.locator(`${judging} [data-part="judge-moment"] select`).selectOption('endOf'),
      (now) => now.draft.outcome?.judge === 'endOf(fellowship)',
    );
    await changed(
      seat,
      'its phase',
      () => page.locator(`${judging} [data-part="judge-phase"] select`).selectOption('archery'),
      (now) => now.draft.outcome?.judge === 'endOf(archery)',
    );
    await changed(
      seat,
      'its turn',
      async () => {
        await page.locator(`${judging} input[data-part="judge-turn"]`).fill('2');
        await page.locator(`${judging} input[data-part="judge-turn"]`).press('Enter');
      },
      (now) => now.draft.outcome?.judge === 'endOf(archery), turn(2)',
    );
    let state = await changed(
      seat,
      'its requirements',
      async () => {
        const win = page.locator(`${judging} textarea[data-part="judge-win"]`);
        await win.fill('{\n\ttype: canSpot\n\tfilter: alias(frodo)\n}');
        await win.blur();
      },
      (now) => now.puzzle?.judge?.win !== null && now.puzzle?.judge?.win !== undefined,
    );
    if (
      !same(state.draft.outcome?.judge, {
        at: 'endOf(archery), turn(2)',
        win: { type: 'canSpot', filter: 'alias(frodo)' },
      })
    ) {
      problems.push(`the judge is ${JSON.stringify(state.draft.outcome?.judge)}`);
    }
    if (!same(state.draft.outcome?.winMessages, ['Won.'])) problems.push('the closing message went');
    await page.locator(`${judging} [data-part="score-value"] select`).selectOption('strength');
    await sleep(300);
    if ((await seat.state()).draft.outcome?.score !== undefined) problems.push("a card's value with no card was sent");
    await changed(
      seat,
      'scored by its card',
      () => page.locator(`${judging} [data-part="score-of"] select`).selectOption('frodo'),
      (now) => now.draft.outcome?.score?.value === 'strength:frodo',
    );
    await changed(
      seat,
      'lower is better',
      () => page.locator(`${judging} [data-part="score-better"] select`).selectOption('lower'),
      (now) => now.draft.outcome?.score?.better === 'lower',
    );
    state = await changed(
      seat,
      'its label',
      async () => {
        await page.locator(`${judging} input[data-part="score-label"]`).fill("Frodo's strength");
        await page.locator(`${judging} input[data-part="score-label"]`).press('Enter');
      },
      (now) => now.draft.outcome?.score?.label === "Frodo's strength",
    );
    if (!same(state.draft.outcome.score, { value: 'strength:frodo', better: 'lower', label: "Frodo's strength" })) {
      problems.push(`the score is ${JSON.stringify(state.draft.outcome.score)}`);
    }
    const text = await source(seat);
    if (!/at: endOf\(archery\), turn\(2\)/.test(text) || !/value: strength:frodo/.test(text)) {
      problems.push("the file's text lacks the judge or the score");
    }
    await seat.shot('judged');
    await reloaded(seat, 'scenario');
    if ((await page.locator(`${judging} [data-part="judge-phase"] select`).inputValue()) !== 'archery') {
      problems.push('the judge is not shown after a reload');
    }
    if ((await page.locator(`${judging} [data-part="score-of"] select`).inputValue()) !== 'frodo') {
      problems.push('the score is not shown after a reload');
    }
    await page.locator(`${judging} [data-part="score-value"] select`).selectOption('custom');
    state = await changed(
      seat,
      'a custom value',
      async () => {
        const value = page.locator(`${judging} textarea[data-part="score-text"]`);
        await value.fill('{\n\ttype: forEachWound\n\tfilter: companion\n}');
        await value.blur();
      },
      (now) => now.puzzle?.score?.value === null,
    );
    if (!same(state.draft.outcome.score?.value, { type: 'forEachWound', filter: 'companion' })) {
      problems.push(`the custom value is ${JSON.stringify(state.draft.outcome.score?.value)}`);
    }
    if (state.draft.outcome.judge?.at !== 'endOf(archery), turn(2)')
      problems.push('the judge went with the custom value');
    await seat.shot('custom');
    await closeDraft(seat);
  },
};
