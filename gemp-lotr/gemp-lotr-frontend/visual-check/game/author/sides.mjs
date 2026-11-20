// Authoring checks: two-sided scenarios (A3), the game's own opening (B10), and the loose ends of the earlier passes
// (the When composer in an opponent's order, closing messages' conditions, a refusal's decision number); see
// ../author.mjs. After each change the draft on the server and the file's text are read, and again once the page
// has loaded again.

import { closeDraft, newDraft, sleep, ui, until } from './lib.mjs';

/** The draft's own cards, and Gandalf (1_72) in the Free Peoples hand. */
const GANDALF = {
  freeps: { frodo: '10_121', ring: '1_2', gandalf: '1_72' },
  shadow: { oppFrodo: '10_121', oppRing: '1_2' },
};

const at = {
  field: (path) => `gd-game-author-step [data-field="${path}"]`,
  stepName: '.gd-author-step-head__name',
  role: (path) => `gd-game-author-step gd-author-select[data-picker="role"][data-path="${path}"] select`,
  asked: 'gd-author-when gd-author-select[data-picker="asked"] select',
  row: (step) => `${ui.timeline} .gd-author-timeline__row[data-step="${step}"]`,
  group: `${ui.timeline} .gd-author-timeline__group`,
  ticks: (role) => `${ui.scenario} .gd-author-scenario__ticks[data-part="auto-pass"][data-role="${role}"]`,
  byRole: `${ui.scenario} gd-author-check[data-part="by-role"] input`,
  opening: `${ui.board} gd-author-board-opening select`,
  order: (index) => `${ui.opponent} li.gd-author-order[data-index="${index}"]`,
};

/** The draft's file as the server would write it. */
async function source(seat) {
  return (await (await seat.who.get('/scenarios/author/source')).json()).source ?? '';
}

/** The Step tab on step `number` (from 1), picked on the Timeline. */
async function turnTo(seat, number) {
  await seat.showTab('timeline');
  await seat.page.locator(`${at.row(number)} .gd-author-timeline__pick`).click();
  const count = (await seat.state()).draft.steps.length;
  await seat.page.locator(`${at.stepName}:text-is("Step ${number} of ${count}")`).waitFor({ timeout: 5000 });
}

/** Waits until the server's draft says what `read` looks for, then for the panel on the (perhaps fresh) game. */
async function onServer(seat, read, what) {
  const found = await until(async () => read(await seat.state()), what, 25000);
  await seat.settled();
  return found;
}

/** Loads the page again, and waits for the panel. */
async function reload(seat) {
  await seat.page.reload();
  await seat.settled();
}

/** Answers the page's own question with its OK, saying what it asked. */
async function confirm(seat) {
  const dialog = seat.page.locator(ui.confirm);
  await dialog.waitFor({ timeout: 5000 });
  const asked = (await dialog.innerText()).replace(/\s+/g, ' ');
  await seat.page.locator(ui.confirmButton('ok')).click();
  return asked;
}

const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const whenOf = (step) => (typeof step?.when === 'string' ? step.when : JSON.stringify(step?.when));

/**
 * The person answers what the page asks them in the game's own opening as an author would (Go first, no mulligan,
 * no starting fellowship but the Ring-bearer); the opponent's held decisions are answered from the Opponent tab (a
 * bid of 0, no mulligan); up to the first turn's first decision. Answers the state then.
 */
async function playTheOpening(seat) {
  const page = seat.page;
  for (let i = 0; i < 12; i++) {
    const state = await seat.state();
    const human = state.waiting.human;
    const opponent = state.waiting.opponent;
    if (state.position.turn >= 1 && human) return state;
    if (human?.kind === 'bid') {
      await page.locator('[data-decision="integer-up"]').click();
      await page.locator(ui.decisionButton('OK')).click();
    } else if (human?.kind === 'goFirst') {
      await choose(page, /go first/i);
    } else if (human?.kind === 'mulligan') {
      await choose(page, /^\s*no/i);
    } else if (human?.kind === 'startingFellowship') {
      await page.locator(ui.decisionButton('DONE')).click();
    } else if (opponent?.held) {
      await seat.showTab('opponent');
      if (opponent.kind === 'bid') {
        await page.locator(`${ui.opponent} input.gd-author-opponent__number`).fill('0');
        await page.locator(`${ui.opponent} button[data-author="number"]`).click();
      } else if (opponent.kind === 'mulligan') {
        await page.locator(`${ui.opponent} button.gd-author-opponent__answer[data-term="option(No)"]`).click();
      } else {
        await page.locator(`${ui.opponent} button[data-author="least"]`).click();
      }
    } else if (human) {
      throw new Error(`the opening asked the person something unlooked for: ${human.text}`);
    }
    const before = JSON.stringify(state.waiting);
    await until(async () => JSON.stringify((await seat.state()).waiting) !== before, 'the opening to go on');
    seat.log(`opening: ${human?.kind ?? `opponent ${opponent?.kind}`} answered`);
  }
  throw new Error('the opening did not come to the first turn');
}

/** Clicks the choice on the page's decision whose words fit, once the page shows it. */
async function choose(page, words) {
  const index = await until(async () => {
    const found = (await page.locator('[data-decision-choice]').allInnerTexts()).findIndex((one) => words.test(one));
    return found < 0 ? null : String(found);
  }, `a choice "${words.source}" on the page`);
  await page.locator(`[data-decision-choice="${index}"]`).click();
}

/** Waits until the text of `locator` fits `words`; answers whether it did (and says what it was if not). */
async function says(locator, words, problems, what) {
  const fits = await until(async () => words.test(await locator.innerText()), what, 10000).catch(() => false);
  if (!fits) problems.push(`${what}: it says "${(await locator.innerText()).replace(/\s+/g, ' ')}"`);
  return fits;
}

/** Passes the person's decisions (the opponent playing by its orders) until `done(state)`; answers that state. */
async function passUntil(seat, done, what) {
  const page = seat.page;
  for (let i = 0; i < 40; i++) {
    const state = await seat.state();
    if (done(state)) return state;
    const human = state.waiting.human;
    if (human) {
      if (human.pass) await page.locator(ui.decisionButton('Pass')).click();
      else if (human.type === 'choice') await choose(page, /^\s*no/i);
      else throw new Error(`passing to ${what}: the person is asked ${human.text}`);
      const before = JSON.stringify(human);
      await until(async () => JSON.stringify((await seat.state()).waiting.human) !== before, 'the game to go on');
    } else {
      await sleep(300);
    }
  }
  throw new Error(`passing never came to ${what}`);
}

export const checks = {
  /**
   * A3: the role in the When composer of a two-sided draft: "as Shadow" picked for a step on the opponent's turn goes
   * into its When after the turn; a When written out with `as` shows it in the picker; the Timeline says whose turn
   * each group is and the role of a step whose seat's name does not say it; all of it again after a reload.
   */
  async 'step-role'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Role check',
      top: { rules: null },
      steps: [
        { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass' },
        { when: 'human, action, phase(regroup), turn(2)', allow: 'pass' },
        {
          when: { seat: 'human', as: 'shadow', require: { type: 'canSpot', filter: 'name(Frodo)' } },
          allow: 'pass',
        },
      ],
    });
    const page = seat.page;
    await turnTo(seat, 2);
    await page.locator(at.role('when')).selectOption('shadow');
    const want = 'human, action, phase(regroup), turn(2), as(shadow)';
    await onServer(seat, (state) => state.draft.steps[1].when === want, `"${want}" on the server`);
    if (!(await source(seat)).includes(`when: ${want}\n`)) problems.push(`the file has no "when: ${want}"`);
    await turnTo(seat, 3);
    if ((await page.locator(at.role('when')).inputValue()) !== 'shadow')
      problems.push('a When written out with as: shadow does not show it in the picker');
    await seat.shot('object-form');
    await seat.showTab('timeline');
    const groups = (await page.locator(at.group).allInnerTexts()).map((one) => one.trim().toLowerCase());
    if (!groups.some((one) => one.startsWith('turn 1 (yours)'))) problems.push(`the groups are ${groups.join(' | ')}`);
    if (!groups.some((one) => one.startsWith('turn 2 (theirs)'))) problems.push(`the groups are ${groups.join(' | ')}`);
    const tag = page.locator(`${at.row(2)} .gd-author-timeline__tag[data-tag="role"]`);
    if ((await tag.count()) !== 1 || (await tag.innerText()).trim() !== 'as Shadow')
      problems.push('row 2 does not say "as Shadow"');
    if ((await page.locator(`${at.row(1)} .gd-author-timeline__tag[data-tag="role"]`).count()) !== 0)
      problems.push('row 1 (the person as Free Peoples on their own turn) says a role');
    await seat.shot('timeline');
    await reload(seat);
    await turnTo(seat, 2);
    if ((await page.locator(at.role('when')).inputValue()) !== 'shadow')
      problems.push('after a reload the role picker does not say as Shadow');
    if ((await page.locator(at.field('when')).inputValue()) !== want) problems.push('after a reload the When differs');
    await closeDraft(seat);
  },

  /**
   * A3: the phases the player is passed through, for each role: ticked, both roles start from the one list; a tick
   * in a role's row sends the object form; the file says it; a reload shows it; unticked after the page's question,
   * one list again.
   */
  async 'scenario-auto-pass-roles'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Auto-pass check',
      top: { rules: null, options: { autoPass: 'fellowship, regroup' } },
    });
    const page = seat.page;
    await seat.showTab('scenario');
    await page.locator(at.byRole).scrollIntoViewIfNeeded();
    await page.locator(at.byRole).check();
    await onServer(
      seat,
      (state) => same(state.draft.options?.autoPass, { freeps: 'fellowship, regroup', shadow: 'fellowship, regroup' }),
      'the object form on the server',
    );
    await seat.showTab('scenario');
    await page.locator(`${at.ticks('shadow')} gd-author-check[data-phase="maneuver"] input`).check();
    await onServer(
      seat,
      (state) => state.draft.options?.autoPass?.shadow === 'fellowship, maneuver, regroup',
      'maneuver in the Shadow role',
    );
    await seat.showTab('scenario');
    await page.locator(`${at.ticks('freeps')} gd-author-check[data-phase="fellowship"] input`).uncheck();
    const want = { freeps: 'regroup', shadow: 'fellowship, maneuver, regroup' };
    await onServer(seat, (state) => same(state.draft.options?.autoPass, want), `${JSON.stringify(want)} on the server`);
    const text = await source(seat);
    if (!/autoPass: \{\s+freeps: regroup\s+shadow: fellowship, maneuver, regroup\s+\}/.test(text))
      problems.push("the file's text does not say autoPass for each role");
    await seat.showTab('scenario');
    await page.locator(at.ticks('shadow')).scrollIntoViewIfNeeded();
    await seat.shot('by-role');
    await reload(seat);
    await seat.showTab('scenario');
    if (!(await page.locator(at.byRole).isChecked())) problems.push('after a reload "for each role" is not ticked');
    if (!(await page.locator(`${at.ticks('shadow')} gd-author-check[data-phase="maneuver"] input`).isChecked()))
      problems.push('after a reload Shadow maneuver is not ticked');
    if (await page.locator(`${at.ticks('freeps')} gd-author-check[data-phase="fellowship"] input`).isChecked())
      problems.push('after a reload Free Peoples fellowship is ticked');
    await page.locator(at.byRole).uncheck();
    const asked = await confirm(seat);
    if (!asked.includes('fellowship, maneuver, regroup')) problems.push(`the question was "${asked}"`);
    await onServer(
      seat,
      (state) => state.draft.options?.autoPass === 'fellowship, maneuver, regroup',
      'one list on the server',
    );
    await closeDraft(seat);
  },

  /**
   * A3 played: a two-sided draft written by playing it ("Write down what I do", the opponent by its orders) from the
   * person's turn into the opponent's: the step written there says `as(shadow)`, the where-line and the Opponent tab
   * say whose turn it is and the person's role, the Timeline says it; a fresh game played to that step is on the
   * opponent's turn.
   */
  async 'sides-play'({ seat }, problems) {
    await newDraft(seat, { title: 'Two sides played', top: { rules: null } });
    const page = seat.page;
    await seat.call('settings', { recording: 'true', askOpponent: 'false' });
    await seat.settled();
    const turn2 = await passUntil(
      seat,
      (state) => state.position.turn === 2 && state.waiting.human && state.position.phase === 'regroup',
      'the regroup of turn 2',
    );
    if (turn2.position.turnOf !== 'opponent' || turn2.position.roles?.human !== 'shadow')
      problems.push(`turn 2 is ${JSON.stringify(turn2.position)}`);
    if (turn2.waiting.human.role !== 'shadow')
      problems.push(`the person's decision's role is ${turn2.waiting.human.role}`);
    await says(page.locator(ui.where), /their turn, you as Shadow/, problems, 'the where-line');
    await seat.showTab('opponent');
    const yours = page.locator(`${ui.opponent} section[data-part="yours"] .gd-author-opponent__asked`);
    await says(yours, /regroup · as Shadow/, problems, "the Opponent tab of the person's decision");
    await seat.shot('opponent-turn');
    await page.locator(ui.decisionButton('Pass')).click();
    const written = await onServer(
      seat,
      (state) =>
        state.draft.steps.findIndex((step) => /phase\(regroup\), turn\(2\), as\(shadow\)/.test(whenOf(step))) + 1,
      'a step on turn 2 written with as(shadow)',
    );
    await seat.showTab('timeline');
    const tag = page.locator(`${at.row(written)} .gd-author-timeline__tag[data-tag="role"]`);
    await until(async () => (await tag.count()) === 1, `row ${written} saying its role`).catch(() =>
      problems.push(`row ${written} says no role`),
    );
    await seat.shot('timeline');
    const before = seat.gameId();
    await page.locator(`${at.row(written)} .gd-author-timeline__go`).click();
    await until(() => seat.gameId() !== before, 'a fresh game played to the step', 30000);
    await seat.settled();
    const fresh = await seat.state();
    if (fresh.position.step !== written || fresh.position.turnOf !== 'opponent')
      problems.push(`the fresh game is at ${JSON.stringify(fresh.position)}`);
    if (fresh.problems.length) problems.push(`problems: ${fresh.problems.join('; ')}`);
    await closeDraft(seat);
  },

  /**
   * B10: how the game opens, in the Board tab: to the game's own opening after the page's question saying what goes
   * (a card in hand, a wound); the setup keeps only the decks; before the cards are dealt the tab says so; a reload
   * shows it; back to a board laid out.
   */
  async 'board-opening'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Opening check',
      top: { cards: GANDALF, setup: { freeps: { hand: 'gandalf', deck: 'ring' }, wounds: { frodo: 1 } } },
    });
    const page = seat.page;
    await seat.showTab('board');
    await page.locator(at.opening).selectOption('game');
    const asked = await confirm(seat);
    if (!asked.includes('Free Peoples hand: gandalf') || !asked.includes('wounds'))
      problems.push(`it asked "${asked}"`);
    await onServer(
      seat,
      (state) => same(state.draft.setup, { opening: 'game', freeps: { deck: 'ring' } }),
      'the setup with the game’s own opening',
    );
    if (!/\n\t+opening: game\n/.test(await source(seat))) problems.push('the file does not say opening: game');
    await seat.showTab('board');
    await page.locator(`${ui.board} [data-note="undealt"]`).waitFor({ timeout: 5000 });
    if ((await page.locator(`${ui.board} gd-author-board-start`).count()) !== 0)
      problems.push('where the story starts is offered before the cards are dealt');
    await seat.shot('opening-game');
    await reload(seat);
    await seat.showTab('board');
    if ((await page.locator(at.opening).inputValue()) !== 'game')
      problems.push('after a reload it opens with the board');
    await page.locator(at.opening).selectOption('board');
    await onServer(seat, (state) => same(state.draft.setup, { freeps: { deck: 'ring' } }), 'the board laid out again');
    await seat.showTab('board');
    await page.locator(`${ui.board} gd-author-board-start .gd-author-board-start__row[data-part="site"]`).waitFor({
      timeout: 10000,
    });
    await closeDraft(seat);
  },

  /**
   * B10: the opening's decisions in "what is being asked": offered for a draft that opens with the game, picked into
   * the When, read by the Timeline as a group of their own before turn 1.
   */
  async 'step-opening-asked'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Opening asked check',
      top: { setup: { opening: 'game' } },
      steps: [{ when: 'human, number', allow: 'number(1)' }],
    });
    const page = seat.page;
    await turnTo(seat, 1);
    const offered = await page.locator(`${at.asked} option`).evaluateAll((all) => all.map((one) => one.value));
    for (const kind of ['bid', 'goFirst', 'startingFellowship', 'mulligan']) {
      if (!offered.includes(kind)) problems.push(`"${kind}" is not offered`);
    }
    await page.locator(at.asked).selectOption('bid');
    const want = 'human, number, bid';
    await onServer(seat, (state) => state.draft.steps[0].when === want, `"${want}" on the server`);
    if (!(await source(seat)).includes(`when: ${want}\n`)) problems.push(`the file has no "when: ${want}"`);
    await seat.showTab('timeline');
    const groups = await page.locator(at.group).allInnerTexts();
    if (
      !same(
        groups.map((one) => one.trim().toLowerCase()),
        ['the game’s opening'],
      )
    )
      problems.push(`the groups are ${groups.join(' | ')}`);
    const row = (await page.locator(`${at.row(1)} .gd-author-timeline__when`).innerText()).trim();
    if (row !== 'the bid') problems.push(`row 1 reads "${row}"`);
    if ((await page.locator(`${ui.timeline} button[data-add="opening"]`).count()) !== 0)
      problems.push('+ Opening is offered with the game’s own opening');
    await seat.shot('timeline');
    await reload(seat);
    await turnTo(seat, 1);
    if ((await page.locator(at.asked).inputValue()) !== 'bid')
      problems.push('after a reload the picker does not say bid');
    await closeDraft(seat);
  },

  /**
   * B10 played: a new draft opening with the game, its opening written as steps by playing it (the person on the
   * table, the opponent from the Opponent tab), up to the first turn; then a fresh game played by the script to a
   * step after the opening. To the board the scenario starts with, only a deck's order is offered.
   */
  async 'opening-play'({ seat }, problems) {
    await newDraft(seat, { title: 'Opening played', top: { setup: { opening: 'game' } } });
    const page = seat.page;
    await seat.call('settings', { recording: 'true', askOpponent: 'true' });
    await seat.settled();
    await says(page.locator(ui.where), /the game’s opening/, problems, 'the where-line');
    await playTheOpening(seat);
    await page.locator(ui.decisionButton('Pass')).click();
    const state = await onServer(
      seat,
      (one) => (one.draft.steps.some((step) => /phase\(fellowship\), turn\(1\)/.test(whenOf(step))) ? one : null),
      'the first turn written',
    );
    const whens = state.draft.steps.map(whenOf);
    const opening = ['freeps, number, bid', 'shadow, number, bid', 'freeps, choice, goFirst'];
    if (!same(whens.slice(0, 3), opening)) problems.push(`the opening was written as ${whens.join(' | ')}`);
    const first = whens.findIndex((when) => /phase\(fellowship\), turn\(1\)/.test(when)) + 1;
    await seat.showTab('timeline');
    const groups = (await page.locator(at.group).allInnerTexts()).map((one) => one.trim().toLowerCase());
    if (groups[0] !== 'the game’s opening') problems.push(`the groups are ${groups.join(' | ')}`);
    await seat.shot('opening-written');
    const before = seat.gameId();
    await page.locator(`${at.row(first)} .gd-author-timeline__go`).click();
    await until(() => seat.gameId() !== before, 'a fresh game played to the first turn', 30000);
    await seat.settled();
    const fresh = await seat.state();
    if (fresh.position.step !== first || fresh.position.turn !== 1 || fresh.problems.length)
      problems.push(`the fresh game is at ${JSON.stringify(fresh.position)}, problems ${fresh.problems.join('; ')}`);
    await seat.showTab('board');
    await page.locator(`${ui.board} label[data-where="setup"] input`).click();
    await until(
      async () => (await page.locator(`${ui.board} gd-author-board-counters`).count()) === 0,
      'no totals',
    ).catch(() => problems.push('the totals are offered for the setup of the game’s own opening'));
    const markers = page.locator(`${ui.board} gd-author-board-laid button[data-action="marker"]`);
    if ((await markers.count()) === 0 || !(await markers.first().isDisabled()))
      problems.push('a marker can be put for the setup of the game’s own opening');
    const moves = await page
      .locator(`${ui.board} gd-author-board-card select[data-control="move"]`)
      .first()
      .evaluate((select) => [...select.options].slice(1).map((one) => one.textContent.trim()));
    if (!same(moves, ['deck.top', 'deck.bottom'])) problems.push(`a card can be moved to ${moves.join(', ')}`);
    await seat.shot('setup-decks');
    await page.locator(`${ui.board} label[data-where="now"] input`).click();
    await closeDraft(seat);
  },

  /**
   * The When composer in an opponent's order: its pickers change the order's When as typing does, nothing sent
   * until Apply; applied, the order on the server and in the file; a reload shows it. A new order for an assignment
   * is offered "the rest to the Ring-bearer" for its answer.
   */
  async 'opponent-order-when'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Order When check',
      top: { opponent: { fallback: 'pass', orders: [{ when: 'opponent, action, phase(shadow)', answer: 'pass' }] } },
    });
    const page = seat.page;
    await seat.showTab('opponent');
    await page.locator(`${at.order(0)} button.gd-author-order__summary`).click();
    const form = `${at.order(0)} .gd-author-order__form`;
    await page.locator(`${form} gd-author-select[data-picker="phase"] select`).selectOption('maneuver');
    const want = 'opponent, action, phase(maneuver)';
    if ((await page.locator(`${form} [data-part="when"]`).inputValue()) !== want)
      problems.push(`the When field says "${await page.locator(`${form} [data-part="when"]`).inputValue()}"`);
    await sleep(500);
    if ((await seat.state()).draft.opponent.orders[0].when !== 'opponent, action, phase(shadow)')
      problems.push('a picker sent the order before Apply');
    await seat.shot('picked');
    await page.locator(`${form} button[data-author="apply"]`).click();
    await onServer(seat, (state) => state.draft.opponent.orders[0].when === want, `"${want}" on the server`);
    if (!(await source(seat)).includes(`when: ${want}\n`)) problems.push(`the file has no "when: ${want}"`);
    await seat.showTab('opponent');
    await page.locator(`${ui.opponent} button[data-author="add-order"]`).click();
    const fresh = `${ui.opponent} li.gd-author-order[data-new] .gd-author-order__form`;
    await page.locator(`${fresh} gd-author-select[data-picker="kind"] select`).selectOption('assign');
    await page.locator(`${fresh} button[data-author="answer-rest"]`).click();
    await until(
      async () => (await page.locator(`${fresh} [data-part="answer"]`).inputValue()) === 'assign(ringBearer: rest)',
      'the rest to the Ring-bearer in the answer',
    ).catch(async () => {
      await seat.shot('no-rest');
      problems.push('the answer is not the rest to the Ring-bearer');
    });
    await page.locator(`${fresh} button[data-author="apply"]`).click();
    await onServer(
      seat,
      (state) =>
        state.draft.opponent.orders[1]?.when === 'assign' &&
        same(state.draft.opponent.orders[1]?.answer, 'assign(ringBearer: rest)'),
      'the new order on the server',
    );
    await reload(seat);
    await seat.showTab('opponent');
    const whens = await page.locator(`${ui.opponent} .gd-author-order__when`).allInnerTexts();
    if (!same(whens, [want, 'assign'])) problems.push(`after a reload the orders are ${whens.join(' | ')}`);
    await closeDraft(seat);
  },

  /**
   * Closing messages' conditions: "Say only if" on a step's message that ends the scenario and on one of the
   * Scenario tab's endings, typed as HJSON; on the server, in the file, and shown again as the file has it (not as
   * JSON) after a reload.
   */
  async 'closing-say-if'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Closing conditions check',
      steps: [
        {
          when: 'freeps, action, phase(fellowship), turn(1)',
          allow: 'pass',
          end: { result: 'complete', messages: ['Done.', 'With Frodo.'] },
        },
      ],
      top: { outcome: { winMessages: ['Won.'] } },
    });
    const page = seat.page;
    await turnTo(seat, 1);
    const condition = '{\n\ttype: canSpot\n\tfilter: name(Frodo)\n}';
    await page.locator(at.field('end.messages.1.require')).fill(condition);
    await page.locator(at.field('end.messages.1.text')).click();
    await onServer(
      seat,
      (state) => state.draft.steps[0].end?.messages?.[1]?.require?.type === 'canSpot',
      "the closing message's condition on the server",
    );
    if (!/text: With Frodo\.\s+require: \{\s+type: canSpot/.test(await source(seat)))
      problems.push('the file has no condition on the closing message');
    await seat.showTab('scenario');
    const win = `${ui.scenario} .gd-author-ending[data-ending="winMessages"] li[data-index="0"] [data-part="require"]`;
    await page.locator(win).scrollIntoViewIfNeeded();
    await page.locator(win).fill(condition);
    await page
      .locator(`${ui.scenario} .gd-author-ending[data-ending="winMessages"] li[data-index="0"] [data-part="text"]`)
      .click();
    await onServer(
      seat,
      (state) => state.draft.outcome?.winMessages?.[0]?.require?.type === 'canSpot',
      "the ending's condition on the server",
    );
    if (!/winMessages: \[\s+\{\s+text: Won\.\s+require: \{\s+type: canSpot/.test(await source(seat)))
      problems.push('the file has no condition on the ending');
    await seat.showTab('scenario');
    await page.locator(win).scrollIntoViewIfNeeded();
    await seat.shot('ending');
    await reload(seat);
    await seat.showTab('scenario');
    const shown = await page.locator(win).inputValue();
    if (!/type: canSpot/.test(shown) || shown.includes('"type"'))
      problems.push(`after a reload the ending's says "${shown}"`);
    await turnTo(seat, 1);
    const kept = await page.locator(at.field('end.messages.1.require')).inputValue();
    if (!/type: canSpot/.test(kept) || kept.includes('"type"'))
      problems.push(`after a reload the step's says "${kept}"`);
    await closeDraft(seat);
  },

  /**
   * A refusal's decision number: on a step that repeats, at its second decision, a click the step does not take
   * offers "+ refuses 2: pass"; taken, the refusal on the server and in the file says the number.
   */
  async 'step-refuses-at'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Refusal number check',
      top: {
        cards: { ...GANDALF, freeps: { ...GANDALF.freeps, aragorn: '1_89' } },
        setup: { freeps: { hand: 'gandalf, aragorn' } },
      },
      steps: [
        {
          when: 'freeps, action, phase(fellowship), turn(1)',
          until: 'phase(shadow)',
          allow: 'play(gandalf), play(aragorn)',
        },
      ],
    });
    const page = seat.page;
    await seat.call('settings', { recording: 'false' });
    await turnTo(seat, 1);
    const board = (await seat.state()).board;
    const gandalf = String(board.cards.find((card) => card.alias === 'gandalf').cardId);
    await page.locator(`[data-card-id="${gandalf}"]`).first().click();
    if (await page.locator('[data-action-id]').count()) await page.locator('[data-action-id]').first().click();
    await until(async () => (await seat.state()).waiting.human?.at === 2, "the step's second decision");
    await seat.settled();
    await turnTo(seat, 1);
    await page.locator(ui.decisionButton('Pass')).click();
    const add = page.locator('gd-author-allow button[data-act="add-refusal"]');
    await add.waitFor({ timeout: 5000 }).catch(async (error) => {
      await seat.shot('no-refusal');
      throw error;
    });
    if ((await add.innerText()).trim() !== '+ refuses 2: pass')
      problems.push(`the button says "${await add.innerText()}"`);
    await add.click();
    await onServer(
      seat,
      (state) => [state.draft.steps[0].refuses ?? []].flat().join('\n') === '2: pass',
      'the numbered refusal on the server',
    );
    const text = await source(seat);
    // (the server writes a numbered refusal as a list of one line)
    if (!/\n\t+refuses: \[\s+"?2: pass"?\s+\]/.test(text))
      problems.push(
        `the file has no refuses "2: pass": ${text.slice(text.indexOf('refuses'), text.indexOf('refuses') + 40)}`,
      );
    await seat.shot('refuses-at');
    await reload(seat);
    await turnTo(seat, 1);
    if ((await page.locator(at.field('refuses')).inputValue()) !== '2: pass')
      problems.push('after a reload Refuses does not say 2: pass');
    await closeDraft(seat);
  },

  /**
   * The opponent's decision held for the author, let go: "it plays by its orders" chosen in the Opponent tab while
   * the opponent waits, the opponent answers by itself at once, in the same game, and the game goes on.
   */
  async 'opponent-let-go'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Let go check',
      top: {
        cards: { ...GANDALF, shadow: { ...GANDALF.shadow, orc: '1_178' } },
        setup: { shadow: { hand: 'orc' }, twilight: 4 },
        opponent: { fallback: 'pass' },
      },
    });
    const page = seat.page;
    await seat.call('settings', { recording: 'false', askOpponent: 'true' });
    await seat.settled();
    await page.locator(ui.decisionButton('Pass')).click();
    await until(async () => (await seat.state()).waiting.opponent?.held, "the opponent's decision held");
    await seat.showTab('opponent');
    const game = seat.gameId();
    await page.locator(`${ui.opponent} input[data-ask="orders"]`).check();
    const state = await until(async () => {
      const now = await seat.state();
      return !now.waiting.opponent?.held && now.waiting.human ? now : null;
    }, 'the opponent answering by itself, and the person asked');
    if (seat.gameId() !== game) problems.push('letting the opponent go brought a fresh game');
    if (state.askOpponent) problems.push('"Ask me for the opponent" is still on');
    await seat.settled();
    await says(
      page.locator(`${ui.opponent} section[data-part="asked"]`),
      /not being asked/,
      problems,
      'the Opponent tab once let go',
    );
    await closeDraft(seat);
  },
};
