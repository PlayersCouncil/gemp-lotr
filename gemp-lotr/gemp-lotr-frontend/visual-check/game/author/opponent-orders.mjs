// Authoring checks: the opponent's standing orders and rules in the Opponent tab, and its fallback in the Scenario
// tab (see ../author.mjs). Every change to the opponent brings a fresh game; each check reads the draft and the
// file's text from the server after each change, and the orders it did not touch must be exactly as they were.

import { closeDraft, newDraft, server, ui, until } from './lib.mjs';

/** Sam beside Frodo, three Goblin Runners in the opponent's hand, and twilight to play them with. */
const TABLE = {
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', sam: '1_311' },
    shadow: { oppFrodo: '10_121', oppRing: '1_2', orc: '1_178', orc2: '1_178', orc3: '1_178' },
  },
  setup: { freeps: { characters: 'sam' }, shadow: { hand: 'orc, orc2, orc3' }, twilight: 6 },
};

/** Two orders that the shadow phase's decision does not fit (so nothing answers it). */
const ORDERS = [
  { id: 'early', when: 'shadow, action, phase(maneuver)', answer: 'pass' },
  { note: 'Kept as it is.', when: 'shadow, action, phase(regroup)', answer: 'pass' },
];

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
  card: (index) => `${ui.opponent} li.gd-author-order[data-index="${index}"]`,
  fresh: `${ui.opponent} li.gd-author-order[data-new]`,
  part: (within, name) => `${within} [data-part="${name}"]`,
  button: (within, name) => `${within} button[data-author="${name}"]`,
  answering: `${ui.opponent} .gd-author-opponent__answering`,
  ask: (how) => `${ui.opponent} input[data-ask="${how}"]`,
  fallback: (where) => `${where} gd-author-select[data-part="fallback"] select`,
  leftovers: `${ui.opponent} gd-author-select[data-part="leftovers"] select`,
  play: (where, card) => `${where} .gd-author-plays gd-author-check[data-card="${card}"] input`,
};

/** The file's text as the server has it (GET scenarios/author/source). */
async function source(seat) {
  return (await (await seat.who.get('/scenarios/author/source')).json()).source;
}

/** Whether the file's text has an order's text, line by line (it is indented there). */
function hasOrder(text, order) {
  const lines = text.split('\n').map((line) => line.trim());
  const wanted = order.text.split('\n').map((line) => line.trim());
  return lines.some((_, at) => wanted.every((line, i) => lines[at + i] === line));
}

/** Does `change` (which brings a fresh game) and waits for the panel on the new game, back on `tabName`. */
async function freshGame(seat, what, change, tabName = 'opponent') {
  const before = seat.gameId();
  await change();
  await until(() => seat.gameId() !== before, `a fresh game after ${what}`, 20000);
  await seat.settled();
  if ((await seat.page.locator(`${ui.tab(tabName)}[aria-selected="true"]`).count()) !== 1) {
    throw new Error(`the fresh game after ${what} did not come back on the ${tabName} tab`);
  }
  seat.log(`fresh game after ${what}`);
  return seat.state();
}

/**
 * Checks the orders after a change: `expect(orders)` says what is wrong with the changed one(s), and every order
 * in `kept` (as the state described it before) must be there with the same text, in the state and in the file.
 */
async function checkOrders(seat, problems, what, kept, expect) {
  const state = await seat.state();
  const orders = state.opponent?.orders ?? [];
  const text = await source(seat);
  const wrong = expect(orders, text);
  if (wrong) problems.push(`after ${what}: ${wrong}`);
  for (const order of kept) {
    const now = orders.find((one) => one.text === order.text);
    if (!now) problems.push(`after ${what}: an order not touched changed (${order.text.replace(/\s+/g, ' ')})`);
    if (!hasOrder(text, order)) problems.push(`after ${what}: the file's text lost an order not touched`);
  }
  return state;
}

/** A draft with the table and the two orders, played to the opponent's shadow phase decision, held. */
async function heldDraft(seat, extra = {}) {
  await newDraft(seat, {
    title: 'Orders check',
    top: { ...TABLE, opponent: { fallback: 'pass', orders: ORDERS }, ...extra },
    steps: TO_SHADOW,
    goto: 2,
  });
  const state = await until(async () => {
    const now = await seat.state();
    return now.waiting?.opponent?.held ? now : null;
  }, "the opponent's decision held for the author");
  await seat.showTab('opponent');
  return state;
}

export const checks = {
  /**
   * An order for the waiting decision added from the tab (it then answers it: the runner is played), made once and
   * given a note, moved up, deleted: the draft and the file's text after each, the other two orders untouched.
   */
  async 'opponent-orders'({ seat }, problems) {
    const held = await heldDraft(seat);
    const page = seat.page;
    const original = held.opponent.orders;
    if ((await page.locator(tab.answering).getAttribute('data-rule')) !== 'nothing') {
      problems.push(`the tab says the decision is answered by ${await page.locator(tab.answering).innerText()}`);
    }
    await seat.shot('waiting');
    // Add: an order for this decision, its When from the decision, its answer typed
    await page.locator(tab.button(ui.opponent, 'order-for')).click();
    const when = await page.locator(tab.part(tab.fresh, 'when')).inputValue();
    if (when !== held.opponent.answering.when) problems.push(`the new order starts with the When "${when}"`);
    await page.locator(tab.part(tab.fresh, 'answer')).fill('play(orc)');
    await seat.shot('writing');
    await freshGame(seat, 'Add', () => page.locator(tab.button(tab.fresh, 'apply')).click());
    await checkOrders(seat, problems, 'Add', original, (orders, text) =>
      orders.length !== 3 || orders[2].answers.join() !== 'play(orc)' || orders[2].when !== when
        ? `the orders are ${JSON.stringify(orders.map((one) => one.text))}`
        : !text.includes('answer: play(orc)')
          ? "the file's text has no play(orc)"
          : null,
    );
    // (the opponent answers a moment after it is asked)
    const zone = async () => (await seat.state()).board?.cards.find((card) => card.alias === 'orc')?.zone;
    await until(async () => (await zone()) === 'minions', 'the new order to play the runner').catch(async () =>
      problems.push(`the new order did not play the runner (it is in ${await zone()})`),
    );
    if ((await page.locator(`${tab.card(2)}[data-open]`).count()) !== 1) {
      problems.push('the new order is not open after the fresh game');
    }
    // Once, and a note: one Apply
    await page.locator(tab.part(tab.card(2), 'once')).check();
    await page.locator(tab.part(tab.card(2), 'note')).fill('Just the once.');
    await freshGame(seat, 'Apply', () => page.locator(tab.button(tab.card(2), 'apply')).click());
    await checkOrders(seat, problems, 'once and a note', original, (orders, text) =>
      !orders[2]?.once || orders[2].note !== 'Just the once.'
        ? `order 3 is ${orders[2]?.text}`
        : !text.includes('note: Just the once.') || !text.includes('once: true')
          ? "the file's text lacks the note or once"
          : null,
    );
    await until(async () => (await seat.state()).opponent.orders[2].spent, 'the once order used up').catch(() =>
      problems.push('the once order is not used up after answering'),
    );
    await page
      .locator(`${tab.card(2)} .gd-author-order__tag[data-tag="spent"]`)
      .waitFor({ timeout: 5000 })
      .catch(() => problems.push('the once order is not shown used up'));
    await seat.shot('once');
    // Up one place
    await freshGame(seat, 'up', () => page.locator(tab.button(tab.card(2), 'up')).click());
    await checkOrders(seat, problems, 'moving it up', original, (orders) =>
      orders[1]?.answers.join() !== 'play(orc)' || orders[0]?.id !== 'early' || orders[2]?.note !== 'Kept as it is.'
        ? `the orders are ${orders.map((one) => one.answers.join()).join(' | ')}`
        : null,
    );
    if ((await page.locator(`${tab.card(1)}[data-open]`).count()) !== 1) problems.push('the moved order is not open');
    // Delete, asked first in the page
    await page.locator(tab.button(tab.card(1), 'delete')).click();
    await page.locator(ui.confirm).waitFor({ timeout: 5000 });
    await freshGame(seat, 'delete', () => page.locator(ui.confirmButton('ok')).click());
    await checkOrders(seat, problems, 'delete', original, (orders) =>
      orders.length !== 2 ? `${orders.length} orders are left` : null,
    );
    // The page loaded again shows the same
    await page.reload();
    await seat.settled();
    await seat.showTab('opponent');
    const count = await page.locator(`${ui.opponent} ol.gd-author-orders`).getAttribute('data-count');
    if (count !== '2') problems.push(`after a reload the tab lists ${count} orders`);
    await seat.shot('deleted');
    await closeDraft(seat);
  },

  /** An order the server refuses: its words on the order, what was typed kept, the draft as it was. */
  async 'opponent-order-refused'({ seat }, problems) {
    await heldDraft(seat);
    seat.refusalsExpected.push(/scenarios\/author\/edit/);
    const page = seat.page;
    const game = seat.gameId();
    await page.locator(`${tab.card(1)} button.gd-author-order__summary`).click();
    await page.locator(tab.part(tab.card(1), 'answer')).fill('play(nobody)');
    await page.locator(tab.button(tab.card(1), 'apply')).click();
    const said = await until(async () => {
      const refusal = page.locator(`${tab.card(1)} .gd-author-order__refusal`);
      return (await refusal.count()) === 1 ? refusal.innerText() : null;
    }, 'the refusal on the order');
    if (!/opponent\.orders\[2\]\.answer: .*nobody/.test(said)) problems.push(`the order says "${said}"`);
    const typed = await page.locator(tab.part(tab.card(1), 'answer')).inputValue();
    if (typed !== 'play(nobody)') problems.push(`the answer field says "${typed}" after the refusal`);
    if (seat.gameId() !== game) problems.push('a refused change brought a fresh game');
    const state = await seat.state();
    if (state.opponent.orders[1].answers.join() !== 'pass') problems.push('the refused change is in the draft');
    await seat.shot('refused');
    await closeDraft(seat);
  },

  /**
   * Leftovers: a player who assigns nobody still gets the fight (the runner goes to the character chosen); with
   * none, there is no fight.
   */
  async 'opponent-leftovers'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Leftovers check',
      top: { ...TABLE, opponent: { fallback: 'pass' } },
      steps: TO_ASSIGNMENT,
      goto: TO_ASSIGNMENT.length + 1,
    });
    const page = seat.page;
    await seat.showTab('opponent');
    // The opponent plays by its orders and fallback (not held for the author: from the next fresh game)
    await page.locator(tab.ask('orders')).check();
    await until(async () => !(await seat.state()).askOpponent, 'the opponent playing by itself');
    await seat.settled();
    /** The player's next decision once the opponent has answered its assignment by itself. */
    const next = () =>
      until(async () => {
        const now = await seat.state();
        return now.waiting?.human && !now.waiting.opponent ? now : null;
      }, "the player's next decision");
    for (const [leftovers, fight] of [
      ['sam', true],
      ['', false],
      ['ringBearer', true],
    ]) {
      await freshGame(seat, `leftovers "${leftovers}"`, () => page.locator(tab.leftovers).selectOption(leftovers));
      const state = await next();
      const fought = state.waiting.human.kind === 'nextSkirmish';
      seat.log(`leftovers "${leftovers}": ${state.waiting.human.text} (${state.waiting.human.where})`);
      if (fought !== fight) {
        problems.push(`with leftovers "${leftovers}" the player is asked "${state.waiting.human.text}"`);
      }
      if ((state.draft.opponent?.leftovers ?? '') !== leftovers) {
        problems.push(`the draft's leftovers are ${state.draft.opponent?.leftovers}, not "${leftovers}"`);
      }
      const text = await source(seat);
      if (leftovers !== '' && !text.includes(`leftovers: ${leftovers}`))
        problems.push("the file's text has no leftovers");
      if (leftovers === '' && text.includes('leftovers:')) problems.push("the file's text still has leftovers");
      if ((await page.locator(tab.leftovers).inputValue()) !== leftovers) problems.push('the select is not shown set');
    }
    await seat.shot('leftovers');
    await closeDraft(seat);
  },

  /**
   * The sandbox fallback chosen in the Scenario tab with two of the three runners: the opponent plays the first of
   * them, never the one not ticked; the Opponent tab shows the same fallback.
   */
  async 'opponent-sandbox'({ seat }, problems) {
    await newDraft(seat, {
      title: 'Sandbox check',
      top: { ...TABLE, opponent: { fallback: 'pass', orders: ORDERS } },
      steps: TO_SHADOW,
      goto: 2,
    });
    const page = seat.page;
    await seat.showTab('opponent');
    await page.locator(tab.ask('orders')).check();
    await until(async () => !(await seat.state()).askOpponent, 'the opponent playing by itself');
    await seat.settled();
    await seat.showTab('scenario');
    const plays = `${ui.scenario} section[data-section="plays"]`;
    const original = (await seat.state()).opponent.orders;
    await freshGame(seat, 'sandbox', () => page.locator(tab.fallback(plays)).selectOption('sandbox'), 'scenario');
    await freshGame(seat, 'orc3 ticked', () => page.locator(tab.play(plays, 'orc3')).check(), 'scenario');
    await freshGame(seat, 'orc2 ticked', () => page.locator(tab.play(plays, 'orc2')).check(), 'scenario');
    const state = await checkOrders(seat, problems, 'the sandbox', original, (orders, text) =>
      !text.includes('fallback: sandbox') || !text.includes('plays: orc3, orc2')
        ? "the file's text lacks the sandbox or its cards"
        : null,
    );
    if (JSON.stringify(state.opponent.plays) !== '["orc3","orc2"]') {
      problems.push(`the sandbox plays ${state.opponent.plays}`);
    }
    // (the opponent plays a moment after it is asked)
    const zonesNow = async () =>
      Object.fromEntries((await seat.state()).board.cards.map((card) => [card.alias, card.zone]));
    const zones = await until(async () => {
      const now = await zonesNow();
      return now.orc3 === 'minions' ? now : null;
    }, 'the sandbox to play orc3').catch(async () => zonesNow());
    if (zones.orc3 !== 'minions') problems.push(`orc3 was not played (${zones.orc3})`);
    if (zones.orc !== 'hand') problems.push(`orc, not one the sandbox may play, is in ${zones.orc}`);
    seat.log(`zones: ${JSON.stringify(zones)}`);
    await seat.shot('sandbox');
    await seat.showTab('opponent');
    if ((await page.locator(tab.fallback(ui.opponent)).inputValue()) !== 'sandbox') {
      problems.push('the Opponent tab does not show the sandbox');
    }
    await page.goto(`${server}${ui.path}?gameId=${seat.gameId()}`);
    await seat.settled();
    if (JSON.stringify((await seat.state()).opponent.plays) !== '["orc3","orc2"]') {
      problems.push('the sandbox cards are gone after a reload');
    }
    await closeDraft(seat);
  },
};
