// Authoring checks: the Scenario tab's endings (see ../author.mjs). The outcome is sent whole; what is written there
// is read back from the server's draft and file, and what a person ending the game is told is seen on the table.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

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
};
