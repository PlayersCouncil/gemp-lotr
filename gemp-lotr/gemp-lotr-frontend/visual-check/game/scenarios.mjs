// The game check's scenarios (run.mjs). Each one is a real game of the test deck (players.mjs) from its start; the
// server's seed makes it the same game every time. A step is one gesture of the player in the browser, made on the
// decision waiting for them, then a snapshot of both pages (unless `quiet`):
//
//   { integer: 0 }                 a number (the bid)
//   { choice: 'Go first' }         one of a multiple choice, by its text
//   { pick: [0, 2] }               the decision's cards by their place in its list (then Done if still waiting)
//   { pass: true }                 Pass (or Done with no card); `wait: false` on an answer: the next decision for the
//                                  player isn't waited for (the opponent decides next)
//   { done: true }                 the Done button
//   { assign: [0, 1] }             a minion assigned to a character (their places in the decision's lists)
//   { play: /Gimli/ }              the action whose text matches: its card is clicked, then the action in its menu
//   { auto: 20, until: /text/ }    up to 20 decisions answered as the opponent would, until one's text matches
//   { cardInfo: id }               right click on a card (an id, or { blueprint, nth, owner, zone } among the cards put
//                                  into play: owner 'player' by default, 'foe' or 'any'; see run.mjs cardIdOf)
//   { hover: id }                  the mouse rests on a card (the hover zoom)
//   { tab: 'settings' }            a tab of the box at the bottom left (chat, settings, options, players)
//   { pile: 'deadPile', player: 0 } a pile counter of the game state panel (discard, deadPile, adventureDeck, removedPile)
//   { stats: 0 }                   the + button that shows a player's other counters
//   { setting: 'autoAccept' }      a control of the settings tab, by its id
//   { pick: [0], then: 'Done' }    cards clicked, then a button (Done, Clear selection, Reset choice) or a key (Escape);
//                                  `answers: true` when that answers the decision
//   { unassign: 0 }                a minion clicked twice (by its place in the decision's list)
//   { press: 'Reset choice' }      a button of the decision
//   { cardInfo: id, by }           `by`: 'right click' (the default), 'shift click' or 'drag up'
//   { drag: id, by: 250 }          a card dragged sideways by 250px
//   { setting: id, value }         a setting's box clicked, or its list set to `value`
//   { slider: id, key }            a slider's handle clicked, then a key
//   { timerAlert: field, value }   a field of the timer alert rules (timerAlertBelow, timerAlertEvery), the first row
//   { toggle: 'auto-zoom' }        the auto-zoom or the timer-mute switch
//   { chat: 'text' }               a chat message typed and sent
//   { replay: 'play' }             a replay's button: play (then waits for the end), slower, faster
//   { key: 'Escape' }              a key
//   { click: 'body' }              a click on the page's background (closes card information)
//   { button: 'concedeGame' }      a button of the options tab (concedeGame, cancelGame)
//
// `foe`: rules for the opponent's answers, before its fixed policy (protocol.mjs foeAnswer); `foeAnswers`: it stops
// after that many. `mode`: 'spectator' or
// 'replay' (run.mjs play), with `answers` given by both sides over HTTP before the page opens.

/** The opponent lets the player go first (with the server's seed, the opponent chooses the seats). */
const PLAYER_FIRST = [
  {
    when: /^Choose one$/,
    answer: (decision) => {
      const index = decision.values.results?.indexOf('Go second') ?? -1;
      return index >= 0 ? String(index) : undefined;
    },
  },
];

/** From the start to the first fellowship phase of the player, who goes first. */
const OPENING = [
  { integer: 0, name: 'bid' },
  { pick: [0], name: 'starting fellowship: Gimli' },
  { pass: true, name: 'starting fellowship: done' },
  { choice: 'No', name: 'no mulligan' },
];

const quietly = (steps) => steps.map((step) => ({ ...step, quiet: true }));

/**
 * The opponent plays the first card it can from its hand in its shadow phase: minions come, so there are assignments
 * and skirmishes. (Only cards: a site's action may be used again and again.)
 */
const FOE_PLAYS_MINIONS = [
  {
    when: /^Play Shadow action or Pass$/,
    answer: (decision) => {
      const index = (decision.values.actionText ?? []).findIndex((text) => text.startsWith('Play '));
      return index >= 0 ? decision.values.actionId[index] : '';
    },
  },
];

const GIMLI = { blueprint: '1_12' };
const FRODO = { blueprint: '1_290' };

/** The player's first fellowship phase reached without snapshots. */
const TO_FELLOWSHIP = quietly(OPENING);

export const GAME_SCENARIOS = [
  { name: 'start', note: 'The page as it opens: the bid', steps: [] },
  { name: 'opening', note: 'Bid, starting fellowship, mulligan', foe: PLAYER_FIRST, steps: OPENING },
  {
    name: 'fellowship-play',
    note: 'Playing a companion, a possession on a bearer, a card action, then moving',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { play: /^Play Dwarf Guard$/, name: 'Dwarf Guard played' },
      { play: /^Play Dwarven Axe$/, name: 'Dwarven Axe: choose its bearer' },
      { pick: [0], name: 'Dwarven Axe on its bearer' },
      { play: /^Use Gimli/, name: "Gimli's action" },
      { auto: 3, until: /^Play Fellowship action or Pass$/, quiet: true },
      { pass: true, name: 'fellowship phase passed: moving' },
    ],
  },
  {
    name: 'minions-and-skirmish',
    note: 'The opponent plays a minion; the player assigns it to Frodo, chooses the skirmish, Frodo is wounded',
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      ...TO_FELLOWSHIP,
      { pass: true, quiet: true },
      { choice: 'Yes', name: 'another move' },
      { auto: 10, until: /^Assign minions/, name: 'minions to assign' },
      { assign: [0, 0], name: 'the minion assigned to the first character' },
      { done: true, name: 'assignments done: which skirmish first' },
      { pick: [0], name: 'the skirmish chosen' },
      { pass: true, name: 'skirmish action passed' },
      { pass: true, name: "no response to Frodo's wound" },
    ],
  },
  {
    name: 'minions-unassigned',
    note: 'Done with a minion left unassigned: the browser asks to confirm',
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      ...TO_FELLOWSHIP,
      { pass: true, quiet: true },
      { choice: 'Yes', quiet: true },
      { auto: 10, until: /^Assign minions/, quiet: true },
      { done: true, name: 'assignments done, confirmed' },
    ],
  },
  {
    name: 'shadow-turn',
    note: "The player's shadow phase in the opponent's turn: playing a minion, then reconciling",
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      ...TO_FELLOWSHIP,
      { auto: 12, until: /^Play Shadow action or Pass$/, quiet: true },
      { play: /^Play Goblin Sneak$/, name: 'Goblin Sneak played' },
      { pass: true, name: 'shadow phase passed' },
      { pass: true, name: 'reconcile: no discard' },
    ],
  },
  {
    name: 'card-info',
    note: 'Card information with its modifiers (right click), closed by a click',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { cardInfo: GIMLI, name: 'Gimli: card information' },
      { click: 'body', name: 'closed' },
      { cardInfo: FRODO, name: 'Frodo: card information' },
    ],
  },
  {
    name: 'hover-zoom',
    note: 'The hover zoom on a card in play and on a site',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { hover: FRODO, name: 'Frodo zoomed' },
      { hover: { blueprint: '1_319', owner: 'any', zone: 'ADVENTURE_PATH' }, name: 'site 1 zoomed' },
    ],
  },
  {
    name: 'tabs',
    note: 'The tabs of the box at the bottom left',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { tab: 'settings', name: 'settings' },
      { tab: 'options', name: 'options' },
      { tab: 'players', name: 'players' },
      { tab: 'chat', name: 'chat' },
    ],
  },
  {
    name: 'piles',
    note: "The players' other counters and their piles",
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { stats: 0, name: "the player's counters" },
      { stats: 1, name: "the opponent's counters" },
      { pile: 'adventureDeck', player: 0, name: 'own adventure deck' },
      { key: 'Escape', quiet: true },
      { pile: 'deadPile', player: 1, name: "the opponent's dead pile" },
      { key: 'Escape', quiet: true },
      { pile: 'discard', player: 0, name: 'own discard pile' },
      { key: 'Escape', quiet: true },
      { pile: 'removedPile', player: 0, name: 'own removed pile' },
    ],
  },
  {
    name: 'concede',
    note: 'Cancel and concede while a decision waits: the legacy page does not poll then, so it never learns the game ended',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { tab: 'options', quiet: true },
      { button: 'cancelGame', name: 'cancel requested' },
      { button: 'concedeGame', name: 'conceded' },
      { pile: 'deck', player: 0, name: 'the deck counter clicked: nothing, the page does not know the game ended' },
    ],
  },
  {
    name: 'concede-while-opponent-decides',
    note: 'The opponent stops in its shadow phase, so the page polls: cancel (the server warns), concede, the deck shown',
    foe: PLAYER_FIRST,
    // The bid, the seats, the starting fellowship, the mulligan
    foeAnswers: 4,
    steps: [
      ...TO_FELLOWSHIP,
      { pass: true, wait: false, name: "passed: the opponent's shadow phase" },
      { tab: 'options', quiet: true },
      { button: 'cancelGame', name: 'cancel requested: the warning' },
      { button: 'concedeGame', name: 'conceded: the game has ended, the feedback dialog' },
      { key: 'Escape', quiet: true },
      { pile: 'deck', player: 0, name: 'own deck, shown once the game has ended' },
    ],
  },
  {
    name: 'settings',
    note: 'Every setting changed; a game played with them: a bid of 1, choices as lists, no auto-accept; a chat message',
    foe: PLAYER_FIRST,
    steps: [
      // What the opening uses first; the timer's controls show once the game knows its players (after the bids)
      { tab: 'settings', name: 'settings' },
      { setting: 'alwaysDropDown', quiet: true },
      { setting: 'autoAccept', name: 'drop-down lists on, auto-accept off' },
      { integer: 1, name: 'a bid of 1 (the arrows): the player chooses the seats' },
      { choice: 'Go first', name: 'going first, from the list' },
      { pick: [0], then: 'Clear selection', name: 'starting fellowship: a card chosen, then cleared' },
      { pick: [0], then: 'Done', answers: true, name: 'starting fellowship: chosen again, then Done' },
      { pick: [], then: 'Escape', answers: true, name: 'starting fellowship: finished with Escape' },
      { choice: 'No', name: 'no mulligan, from the list' },
      { setting: 'autoDismissDeckReveal', quiet: true },
      { setting: 'use-old-stack-checkbox', quiet: true },
      { setting: 'autoPassSHADOW', quiet: true },
      { setting: 'foilPresentation', value: 'animated', quiet: true },
      { slider: 'animation-slider', key: 'ArrowRight', quiet: true },
      { timerAlert: 'timerAlertEvery', value: 20, quiet: true },
      { toggle: 'timer-mute', quiet: true },
      { toggle: 'auto-zoom', name: 'every other setting changed' },
      { tab: 'chat', quiet: true },
      { chat: 'Good game!', name: 'a chat message sent' },
    ],
  },
  {
    name: 'reconcile-discard',
    note: 'Without auto-accept: a card chosen in reconcile, reset, chosen again and discarded; the discard pile',
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      { tab: 'settings', quiet: true },
      { setting: 'autoAccept', quiet: true },
      { tab: 'chat', quiet: true },
      { integer: 0, quiet: true },
      { pick: [0], then: 'Done', answers: true, quiet: true },
      { pick: [], then: 'Done', answers: true, quiet: true },
      { choice: 'No', quiet: true },
      { auto: 20, until: /^Reconcile/, name: 'reconcile' },
      { pick: [0], then: 'Reset choice', name: 'a card chosen, then reset' },
      { pick: [0], then: 'Done', answers: true, name: 'a card discarded' },
      { stats: 0, quiet: true },
      { pile: 'discard', player: 0, name: 'own discard pile' },
    ],
  },
  {
    name: 'card-gestures',
    note: 'Card information by shift click and by dragging up; a card of the hand dragged to another place',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { cardInfo: GIMLI, by: 'shift click', name: 'Gimli: shift click' },
      { click: 'body', quiet: true },
      { cardInfo: FRODO, by: 'drag up', name: 'Frodo: dragged up' },
      { click: 'body', quiet: true },
      { drag: { zone: 'HAND', nth: 0 }, by: 250, name: 'the first card of the hand dragged to the right' },
    ],
  },
  {
    name: 'skirmish-event',
    note: 'A minion assigned, unassigned and assigned again; an event played in the skirmish',
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      ...TO_FELLOWSHIP,
      { pass: true, quiet: true },
      { choice: 'Yes', quiet: true },
      { auto: 10, until: /^Assign minions/, quiet: true },
      { assign: [0, 0], quiet: true },
      { unassign: 0, name: 'the minion unassigned' },
      { assign: [0, 0], quiet: true },
      { done: true, quiet: true },
      { pick: [0], name: 'the skirmish' },
      { play: /^Play Axe Strike$/, name: 'Axe Strike played' },
      { auto: 3, name: 'the skirmish resolved' },
    ],
  },
  {
    name: 'spectator',
    note: 'A third player watches: both sides played over HTTP for 40 answers',
    mode: 'spectator',
    answers: 40,
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      { stats: 0, name: 'counters' },
      { tab: 'players', name: 'players' },
    ],
  },
  {
    name: 'replay',
    note: 'The replay of a short game (40 answers, then conceded): slower, faster, played to the end',
    mode: 'replay',
    answers: 40,
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      { replay: 'slower', quiet: true },
      { replay: 'faster', quiet: true },
      { replay: 'faster', quiet: true },
      { replay: 'play', name: 'played to the end' },
    ],
  },
];
