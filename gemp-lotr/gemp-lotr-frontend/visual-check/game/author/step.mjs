// Authoring checks: the Step tab (see ../author.mjs).

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

const THREE_STEPS = [
  { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass', messages: [{ text: 'First, pass.' }] },
  { when: 'freeps, action, phase(fellowship), turn(2)', allow: 'pass' },
  { when: 'freeps, action, phase(fellowship), turn(3)', allow: 'pass' },
];

/** The draft's own cards, and Gandalf (1_72) in the Free Peoples hand: something to play in the Fellowship phase. */
const GANDALF_IN_HAND = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', gandalf: '1_72' },
    shadow: { oppFrodo: '10_121', oppRing: '1_2' },
  },
  setup: { freeps: { hand: 'gandalf' } },
};

/** The draft's file as the server would write it (GET scenarios/author/source). */
const sourceOf = async (seat) => (await seat.who.get('/scenarios/author/source')).json();

const step = {
  head: 'gd-author-step-head',
  name: '.gd-author-step-head__name',
  field: (path) => `gd-game-author-step [data-field="${path}"]`,
  act: (name) => `gd-author-step-actions button[data-act="${name}"]`,
  problem: '.gd-author-notice[data-notice="problem"]',
};

/** The Step tab on step `number` (from 1), picked on the Timeline. */
async function turnTo(seat, number) {
  await seat.showTab('timeline');
  await seat.page
    .locator(`${ui.timeline} .gd-author-timeline__row[data-step="${number}"] .gd-author-timeline__pick`)
    .click();
  await seat.page
    .locator(`${step.name}:text-is("Step ${number} of ${(await seat.state()).draft.steps.length}")`)
    .waitFor({
      timeout: 5000,
    });
}

/** Where the focus is: the field's path, its text, the caret. */
const focusOf = (page) =>
  page.evaluate(() => {
    const active = document.activeElement;
    return {
      field: active?.dataset?.field ?? null,
      value: active?.value ?? null,
      caret: active?.selectionStart ?? null,
    };
  });

const textOf = (message) => (typeof message === 'string' ? message : message?.text);

/** The page went to a fresh game (loaded again); waits until the panel is back with nothing on its way. */
async function freshGame(seat, from) {
  await until(() => seat.gameId() !== from, 'a fresh game', 20000);
  await seat.settled();
}

export const checks = {
  /**
   * A message's text typed: on the server soon after the typing stops, the caret and the focus where they were, and
   * the bubble on the table saying it.
   */
  async 'step-type'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 1);
    const text = page.locator(step.field('messages.0.text'));
    await text.click();
    await page.keyboard.press('End');
    await page.keyboard.type(' Then press Pass.', { delay: 30 });
    const want = 'First, pass. Then press Pass.';
    await until(
      async () => textOf((await seat.state()).draft.steps[0].messages?.[0]) === want,
      'the text on the server',
    );
    await sleep(400);
    const focus = await focusOf(page);
    if (focus.field !== 'messages.0.text') problems.push(`the focus went to ${focus.field} after the send`);
    if (focus.caret !== want.length) problems.push(`the caret is at ${focus.caret}, not at the end (${want.length})`);
    if (focus.value !== want) problems.push(`the field says "${focus.value}"`);
    await until(
      async () => (await page.locator(ui.bubble).innerText()).includes('Then press Pass.'),
      'the bubble saying it',
    );
    await seat.shot('typed');
    await closeDraft(seat);
  },

  /** Typing goes on while a send is on its way (a slow server): nothing typed meanwhile is lost or written over. */
  async 'step-inflight'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 1);
    await page.route('**/scenarios/author/edit', async (route) => {
      await sleep(1500);
      await route.continue().catch(() => undefined);
    });
    await page.locator(step.field('messages.0.text')).click();
    await page.keyboard.press('End');
    await page.keyboard.type(' One', { delay: 30 });
    await sleep(1000); // the send is on its way
    await page.keyboard.type(' two', { delay: 30 });
    await sleep(900); // the server has answered the first, the second is on its way
    await page.keyboard.type(' three', { delay: 30 });
    const want = 'First, pass. One two three';
    const mid = await focusOf(page);
    if (mid.value !== want || mid.caret !== want.length) {
      problems.push(`while sends were on their way the field said "${mid.value}" (caret ${mid.caret})`);
    }
    await until(
      async () => textOf((await seat.state()).draft.steps[0].messages?.[0]) === want,
      'everything typed on the server',
      20000,
    );
    await until(async () => (await page.locator(ui.busy).count()) === 0, 'nothing on its way');
    const end = await focusOf(page);
    if (end.field !== 'messages.0.text' || end.value !== want || end.caret !== want.length) {
      problems.push(`once the sends were back the field had ${JSON.stringify(end)}`);
    }
    await page.unroute('**/scenarios/author/edit');
    await closeDraft(seat);
  },

  /** The When composer: a phase picked replaces the When's phase, in its place, and goes to the server. */
  async 'step-when'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 3);
    await page.locator('gd-author-when gd-author-select[data-picker="phase"] select').selectOption('shadow');
    await page.locator('gd-author-when input[data-picker="turn"]').fill('4');
    await page.locator('gd-author-when input[data-picker="turn"]').press('Enter');
    const want = 'freeps, action, phase(shadow), turn(4)';
    await until(async () => (await seat.state()).draft.steps[2].when === want, `the When "${want}" on the server`);
    const shown = await page.locator(step.field('when')).inputValue();
    if (shown !== want) problems.push(`the When field says "${shown}"`);
    await seat.shot('when');
    await closeDraft(seat);
  },

  /**
   * A part the server refuses, sent with another it takes: the other goes in, the refusal is said, and what was typed
   * in the refused part stays in its field.
   */
  async 'step-refuse'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    seat.refusalsExpected.push(/\/scenarios\/author\/edit/);
    const page = seat.page;
    await turnTo(seat, 1);
    // (typed in Allow without leaving it, then a message typed: the two go together, after the typing stops)
    await page.locator(step.field('allow.answers')).fill('play(nobody)');
    await page.locator(step.field('messages.0.text')).click();
    await page.keyboard.press('End');
    await page.keyboard.type(' Go on.', { delay: 30 });
    await page.locator(step.problem).waitFor({ timeout: 10000 });
    await until(
      async () => textOf((await seat.state()).draft.steps[0].messages?.[0]) === 'First, pass. Go on.',
      'the message on the server, though Allow was refused',
    );
    const state = await seat.state();
    if (state.draft.steps[0].allow !== 'pass')
      problems.push(`the server took Allow: ${JSON.stringify(state.draft.steps[0].allow)}`);
    const allow = await page.locator(step.field('allow.answers')).inputValue();
    if (allow !== 'play(nobody)') problems.push(`the refused Allow field now says "${allow}"`);
    if (!/nobody/.test(await page.locator(step.problem).innerText()))
      problems.push('the refusal does not say what is wrong');
    await seat.shot('refused');
    // Put right, it goes
    await page.locator(step.field('allow.answers')).fill('pass, pass');
    await page.locator(step.field('allow.answers')).press('Enter');
    await until(
      async () => (await seat.state()).draft.steps[0].allow === 'pass, pass',
      'Allow put right on the server',
    );
    await closeDraft(seat);
  },

  /** A step moved later, and another taken out (asked first, in the page's own dialog). */
  async 'step-order'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 2);
    let from = seat.gameId();
    await page.locator(step.act('later')).click();
    await until(async () => (await seat.state()).draft.steps[2].when.includes('turn(2)'), 'step 2 moved to 3');
    if (seat.gameId() !== from) await seat.settled();
    await page.locator(`${step.name}:text-is("Step 3 of 3")`).waitFor({ timeout: 10000 });
    from = seat.gameId();
    await page.locator(step.act('delete')).click();
    await page.locator(ui.confirm).waitFor({ timeout: 5000 });
    await seat.shot('delete-asked');
    await page.locator(ui.confirmButton('ok')).click();
    await until(async () => (await seat.state()).draft.steps.length === 2, 'step 3 taken out');
    if (seat.gameId() !== from) await seat.settled();
    const whens = (await seat.state()).draft.steps.map((one) => one.when);
    if (!whens[1].includes('turn(3)')) problems.push(`left with ${JSON.stringify(whens)}`);
    await page.locator(`${step.name}:text-is("Step 2 of 2")`).waitFor({ timeout: 10000 });
    await closeDraft(seat);
  },

  /** A step turned into a block, and a step added to the block, on the block's page. */
  async 'step-block'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 3);
    let from = seat.gameId();
    await page.locator(step.act('convert')).click();
    await until(async () => (await seat.state()).draft.steps[2].anyOrder?.length === 1, 'step 3 a block of one step');
    if (seat.gameId() !== from) await seat.settled();
    await page.locator('gd-author-block-page').waitFor({ timeout: 10000 });
    from = seat.gameId();
    await page.locator('gd-author-block-page button[data-add="step"]').click();
    await until(async () => (await seat.state()).draft.steps[2].anyOrder?.length === 2, 'a second step in the block');
    if (seat.gameId() !== from) await seat.settled();
    await page.locator('gd-author-block-panel .gd-author-panel[data-sub="1"][data-open]').waitFor({ timeout: 10000 });
    const block = (await seat.state()).draft.steps[2];
    if (block.until !== 'phase(shadow)') problems.push(`the block's Until is ${JSON.stringify(block.until)}`);
    await seat.shot('block');
    await closeDraft(seat);
  },

  /**
   * A change the game in hand cannot take (a step it has been through) brings a fresh game: the page loads again,
   * on the same step, in the Step tab, with the field that had the focus having it again.
   */
  async 'step-fresh'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    const went = await seat.call('goto', { step: '3' });
    await seat.openGame(String(went.gameId));
    await turnTo(seat, 1);
    const from = seat.gameId();
    await page.locator(step.field('when')).fill('freeps, action, turn(1)');
    await page.locator(step.field('when')).press('Tab');
    await page.locator(step.field('messages.0.text')).click();
    await freshGame(seat, from);
    await page.locator(`${ui.tab('step')}[aria-selected="true"]`).waitFor({ timeout: 5000 });
    const name = await page.locator(step.name).innerText();
    if (name !== 'Step 1 of 3') problems.push(`the fresh game came back on "${name}"`);
    const when = await page.locator(step.field('when')).inputValue();
    if (when !== 'freeps, action, turn(1)') problems.push(`the When field says "${when}"`);
    const focus = await focusOf(page);
    if (focus.field !== 'messages.0.text') problems.push(`the focus came back on ${focus.field}`);
    await seat.shot('fresh');
    await closeDraft(seat);
  },

  /**
   * B5: "what is being asked" picked in the When composer and negated: the When on the server and in the file's text
   * exactly as the server writes a new step's (seat, kind, what is asked, phase, turn), and still so after a reload.
   */
  async 'step-asked'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 3);
    await page.locator('gd-author-when gd-author-select[data-picker="asked"] select').selectOption('nextSkirmish');
    await until(
      async () =>
        (await seat.state()).draft.steps[2].when === 'freeps, action, nextSkirmish, phase(fellowship), turn(3)',
      'nextSkirmish in the When on the server',
    );
    await page.locator('gd-author-when input[data-picker="not"]').check();
    const want = 'freeps, action, not(nextSkirmish), phase(fellowship), turn(3)';
    await until(async () => (await seat.state()).draft.steps[2].when === want, `the When "${want}" on the server`);
    const { source } = await sourceOf(seat);
    if (!source.includes(`when: ${want}\n`)) problems.push(`the file's text has no "when: ${want}" line`);
    await seat.shot('asked');
    await seat.settled();
    await seat.openGame(seat.gameId());
    await turnTo(seat, 3);
    const shown = await page.locator(step.field('when')).inputValue();
    if (shown !== want) problems.push(`after a reload the When field says "${shown}"`);
    const picked = await page.locator('gd-author-when gd-author-select[data-picker="asked"] select').inputValue();
    if (picked !== 'nextSkirmish') problems.push(`after a reload the picker says "${picked}"`);
    if (!(await page.locator('gd-author-when input[data-picker="not"]').isChecked()))
      problems.push('after a reload "not" is not ticked');
    await closeDraft(seat);
  },

  /**
   * B6: a condition typed in a message's "Say only if": on the server and in the file; the step on the table put up
   * again without that message (its condition does not hold), the bubble not saying it, the card saying it was left
   * out; the condition still there after a reload.
   */
  async 'step-say-if'({ seat }, problems) {
    const steps = [
      { ...THREE_STEPS[0], messages: [{ text: 'Always said.' }, { text: 'Only with Gandalf.' }] },
      ...THREE_STEPS.slice(1),
    ];
    await newDraft(seat, { steps });
    const page = seat.page;
    await turnTo(seat, 1);
    const card = page.locator('.gd-author-message[data-index="1"]');
    await card.locator('details.gd-author-message__more > summary').click();
    const condition = page.locator(step.field('messages.1.require'));
    await condition.fill('{\n\ttype: canSpot\n\tfilter: name(Gandalf)\n}');
    // (Tab types a tab in a field of more than one line: the field is left by a click elsewhere)
    await page.locator(step.field('messages.1.header')).click();
    await until(async () => (await seat.state()).draft.steps[0].messages?.[1]?.require, 'the condition on the server');
    await seat.settled();
    await until(async () => (await card.getAttribute('data-said')) === 'no', 'the card saying it was left out');
    const bubble = await page.locator(ui.bubble).innerText();
    if (!bubble.includes('Always said.')) problems.push(`the bubble says "${bubble}"`);
    if (bubble.includes('Only with Gandalf')) problems.push('the bubble says the message whose condition fails');
    const { source } = await sourceOf(seat);
    if (!/text: Only with Gandalf\.\s+require:/.test(source)) problems.push('the file has no require on the message');
    await seat.shot('left-out');
    await seat.openGame(seat.gameId());
    await turnTo(seat, 1);
    const kept = await page.locator(step.field('messages.1.require')).inputValue();
    if (!/name\(Gandalf\)/.test(kept)) problems.push(`after a reload Say only if says "${kept}"`);
    await closeDraft(seat);
  },

  /**
   * B6: a mistake in "Say only if" typed with a change to the message's text: the condition refused in the server's
   * words and kept in its field, the text on the server all the same.
   */
  async 'step-say-if-bad'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    seat.refusalsExpected.push(/\/scenarios\/author\/edit/);
    const page = seat.page;
    await turnTo(seat, 1);
    await page.locator('.gd-author-message[data-index="0"] details.gd-author-message__more > summary').click();
    const bad = '{ type: canSpot, filter: ';
    await page.locator(step.field('messages.0.require')).fill(bad);
    await page.locator(step.field('messages.0.text')).click();
    await page.keyboard.press('End');
    await page.keyboard.type(' Go on.', { delay: 30 });
    await until(
      async () => textOf((await seat.state()).draft.steps[0].messages?.[0]) === 'First, pass. Go on.',
      'the text on the server, though the condition was refused',
    );
    await page.locator(step.problem).waitFor({ timeout: 10000 });
    const said = await page.locator(step.problem).innerText();
    if (!/require/i.test(said)) problems.push(`the refusal says "${said}"`);
    const message = (await seat.state()).draft.steps[0].messages?.[0];
    if (message?.require !== undefined)
      problems.push(`the server took a condition: ${JSON.stringify(message.require)}`);
    const kept = await page.locator(step.field('messages.0.require')).inputValue();
    if (kept !== bad) problems.push(`the refused field now says "${kept}"`);
    await seat.shot('refused');
    await closeDraft(seat);
  },

  /**
   * B3: a click on Pass the step does not take offers "+ refuses pass"; taken, the refusal is on the server and in the
   * file, and its badge for the step in hand says it is refused.
   */
  async 'step-refuses'({ seat }, problems) {
    await newDraft(seat, {
      steps: [{ ...THREE_STEPS[0], allow: 'play(gandalf)' }, ...THREE_STEPS.slice(1)],
      top: GANDALF_IN_HAND,
    });
    const page = seat.page;
    await turnTo(seat, 1);
    await page.locator(ui.decisionButton('Pass')).click();
    const add = page.locator('gd-author-allow button[data-act="add-refusal"]');
    await add.waitFor({ timeout: 5000 });
    if ((await add.innerText()).trim() !== '+ refuses pass')
      problems.push(`the button says "${await add.innerText()}"`);
    await add.click();
    await until(async () => (await seat.state()).draft.steps[0].refuses === 'pass', 'the refusal on the server');
    await seat.settled();
    const badge = page.locator('.gd-author-allow__refusal[data-now="refused"]');
    await badge.waitFor({ timeout: 10000 });
    if (!(await badge.innerText()).includes('pass')) problems.push(`the badge says "${await badge.innerText()}"`);
    const { source } = await sourceOf(seat);
    if (!/\n\t+refuses: pass\n/.test(source)) problems.push('the file has no "refuses: pass"');
    await seat.shot('refuses');
    // Typed a line each: the server writes refusals for every decision on one line, and the field takes that back
    await page.locator(step.field('refuses')).fill('pass\nplay(frodo)');
    // (Tab types a tab in a field of more than one line: the field is left by a click elsewhere)
    await page.locator(step.field('answer')).click();
    await until(
      async () => (await seat.state()).draft.steps[0].refuses === 'pass, play(frodo)',
      'two refusals on the server',
    );
    await until(
      async () => (await page.locator(step.field('refuses')).inputValue()) === 'pass, play(frodo)',
      "the field showing the server's way of writing them",
    );
    await page.locator('.gd-author-allow__refusal[data-now="notOffered"]').waitFor({ timeout: 10000 });
    await closeDraft(seat);
  },

  /**
   * B9: a number from the game put in at the caret in the middle of typed text from the menu beside it: the focus and
   * the caret kept, the text sent once with it, the bubble on the table showing the number.
   */
  async 'step-template'({ seat }, problems) {
    await newDraft(seat, { steps: THREE_STEPS });
    const page = seat.page;
    await turnTo(seat, 1);
    const sends = [];
    page.on('request', (request) => {
      if (request.url().includes('/scenarios/author/edit') && (request.postData() ?? '').includes('%7Btwilight%7D'))
        sends.push(request.postData());
    });
    await page.locator(step.field('messages.0.text')).click();
    await page.keyboard.press('End');
    await page.keyboard.type(' Pool:  now.', { delay: 20 });
    for (let i = 0; i < 5; i++) await page.keyboard.press('ArrowLeft');
    await page.locator('.gd-author-message[data-index="0"] button[data-act="templates"]').click();
    await page.locator('.gd-author-message[data-index="0"] button[data-template="twilight"]').click();
    const want = 'First, pass. Pool: {twilight} now.';
    const focus = await focusOf(page);
    if (focus.field !== 'messages.0.text') problems.push(`the focus went to ${focus.field}`);
    if (focus.value !== want) problems.push(`the field says "${focus.value}"`);
    if (focus.caret !== want.indexOf(' now.')) problems.push(`the caret is at ${focus.caret}`);
    await until(
      async () => textOf((await seat.state()).draft.steps[0].messages?.[0]) === want,
      'the text on the server',
    );
    await sleep(1500);
    if (sends.length !== 1) problems.push(`the template went to the server ${sends.length} times`);
    await until(
      async () => /Pool: \d+ now\./.test(await page.locator(ui.bubble).innerText()),
      'the bubble showing the pool as a number',
    );
    await seat.shot('template');
    await closeDraft(seat);
  },
};
