// Authoring checks: the Opponent tab, answering for the opponent where the script holds it (see ../author.mjs).

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

/** Sam beside Frodo, two Goblin Runners in the opponent's hand. */
const TABLE = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', sam: '1_311' },
    shadow: { oppFrodo: '10_121', oppRing: '1_2', orc: '1_178', orc2: '1_178' },
  },
  setup: { freeps: { characters: 'sam' }, shadow: { hand: 'orc, orc2' } },
};

/** The player passes the fellowship phase: the opponent's shadow phase is next. */
const TO_SHADOW = [{ when: 'freeps, action, phase(fellowship), turn(1)', answer: 'pass' }];

/** On to the opponent's assignment, a Goblin Runner in play and nobody assigned by the player. */
const TO_ASSIGNMENT = [
  ...TO_SHADOW,
  { when: 'shadow, action, phase(shadow)', answer: 'play(orc)' },
  ...['maneuver', 'archery', 'assignment'].flatMap((phase) => [
    { when: `freeps, action, phase(${phase})`, answer: 'pass' },
    { when: `shadow, action, phase(${phase})`, answer: 'pass' },
  ]),
  { when: 'freeps, assign', answer: 'pass' },
];

const tab = {
  answer: (term) => `${ui.opponent} button.gd-author-opponent__answer[data-term="${term}"]`,
  record: `${ui.opponent} gd-author-check[data-option="record"] input`,
  minion: (key) => `${ui.opponent} select[data-minion="${key}"]`,
  assign: `${ui.opponent} button[data-author="assign"]`,
};

/** A draft with the table above and the script given, played to its end: the opponent's decision is held. */
async function heldAt(seat, steps) {
  await newDraft(seat, { title: 'Opponent check', top: TABLE, steps, goto: steps.length + 1 });
  return until(async () => {
    const state = await seat.state();
    return state.waiting?.opponent?.held ? state : null;
  }, "the opponent's decision held for the author");
}

/** The opponent's decision held: the tab asks for attention, which showing it puts away. */
async function attended(seat, problems) {
  const page = seat.page;
  const button = page.locator(ui.tab('opponent'));
  await until(
    async () => (await button.getAttribute('data-attention')) !== null,
    'the Opponent tab asking for attention',
  );
  if ((await page.locator(`${ui.panel}[data-wanted]`).count()) !== 1) problems.push('the panel is not marked wanted');
  await seat.shot('attention');
  await seat.showTab('opponent');
  if ((await button.getAttribute('data-attention')) !== null) problems.push('the Opponent tab still asks once shown');
}

/** The game goes on: the opponent's decision held is answered, and something else is asked. */
async function wentOn(seat, before) {
  return until(async () => {
    const state = await seat.state();
    const now = state.waiting?.opponent;
    return now?.text !== before.text || now?.where !== before.where || !now?.held ? state : null;
  }, 'the game to go on after the answer');
}

export const checks = {
  /** Held for the author, the opponent plays a card from the tab; written into the script, it becomes a step. */
  async opponent({ seat }, problems) {
    const held = await heldAt(seat, TO_SHADOW);
    await attended(seat, problems);
    const page = seat.page;
    const asked = await page.locator(`${ui.opponent} .gd-author-opponent__asked`).first().innerText();
    if (!asked.includes(held.waiting.opponent.text)) problems.push(`the tab says "${asked}" of the decision`);
    if (!(await page.locator(tab.record).isChecked())) problems.push('"write it into the script" is not ticked');
    await seat.shot('held');
    await page.locator(tab.answer('play(orc)')).click();
    const after = await wentOn(seat, held.waiting.opponent);
    const written = after.draft.steps.find((step) => step.answer === 'play(orc)');
    if (!written) problems.push(`no step answers play(orc): ${JSON.stringify(after.draft.steps)}`);
    else if (!/^shadow/.test(written.when ?? '')) problems.push(`the step written is for "${written.when}"`);
    await seat.settled();
    await seat.shot('played');
    await closeDraft(seat);
  },

  /** Unticked, Pass is the opponent's this once: the game goes on, and the script is left as it was. */
  async 'opponent-pass'({ seat }, problems) {
    const held = await heldAt(seat, TO_SHADOW);
    await attended(seat, problems);
    const page = seat.page;
    await page.locator(tab.record).uncheck();
    await page.locator(tab.answer('pass')).click();
    const after = await wentOn(seat, held.waiting.opponent);
    if (after.draft.steps.length !== TO_SHADOW.length) {
      problems.push(`the script has ${after.draft.steps.length} steps after an answer not written down`);
    }
    await seat.settled();
    await seat.shot('passed');
    await closeDraft(seat);
  },

  /** The opponent's assignment, made in the tab: a minion to a character, written into the script. */
  async 'opponent-assign'({ seat }, problems) {
    const held = await heldAt(seat, TO_ASSIGNMENT);
    if (held.waiting.opponent.type !== 'assign') {
      throw new Error(`the opponent is asked "${held.waiting.opponent.text}", not to assign`);
    }
    await attended(seat, problems);
    const page = seat.page;
    await page.locator(tab.minion('orc')).selectOption('frodo');
    await seat.shot('assigning');
    await page.locator(tab.assign).click();
    const after = await wentOn(seat, held.waiting.opponent);
    const last = after.draft.steps.at(-1);
    if (last?.answer !== 'assign(frodo: orc)') problems.push(`the last step answers "${last?.answer}"`);
    await sleep(300);
    await seat.settled();
    await seat.shot('assigned');
    await closeDraft(seat);
  },
};
