// The game page's gesture matrix (`npm run check:game:matrix`): every control of the game page against every gesture
// it can get, in each state it appears in, on both pages (G4 of trymarz/gemp-lotr-private#27). Like the deck builder's
// (../matrix.mjs, whose gestures it uses), the manifest below is generated into scenarios that run.mjs plays as real
// games and compares after each gesture. The gestures are made one after the other in one game per state, so a page
// that reacts differently to one shows it at that step and in every step after.
//
// Gestures that would change the game itself are left out where other scenarios make them (NOT_APPLICABLE says why):
// a card that answers the decision when pressed, the decision's own buttons, conceding.
import { GESTURES } from '../matrix.mjs';
import { ANGULAR_CONTRACT, LEGACY } from './controls.mjs';
import { FOE_PLAYS_MINIONS, FRODO, GIMLI, PLAYER_FIRST, TO_FELLOWSHIP } from './scenarios.mjs';

/**
 * What can be done to each kind of control. A checkbox and a switch are pressed an even number of times, so each one
 * is left as it was found (the next control is tested in the same game).
 */
export const GAME_KINDS = {
  button: ['hover', 'leave', 'press', 'rightClick', 'shiftClick', 'doubleClick', 'focus', 'enter', 'space'],
  checkbox: ['hover', 'leave', 'press', 'rightClick', 'shiftClick', 'doubleClick', 'focus', 'enter', 'space', 'press'],
  // A counter of the game state panel that opens a pile: each gesture then closes what it opened
  counter: ['hover', 'leave', 'press', 'rightClick', 'shiftClick', 'doubleClick', 'focus', 'enter'],
  select: ['hover', 'focus', 'rightClick', 'eachOption'],
  range: ['hover', 'focus', 'arrows'],
  text: ['hover', 'press', 'focus', 'type', 'escape'],
  card: ['hover', 'leave', 'rightClick', 'shiftClick', 'dragUp', 'hoverThenShift'],
  // Only looked at: pressing it is tested by a scenario of its own
  passive: ['hover', 'leave', 'focus'],
};

/** Why a gesture is left out of a kind, or a control out of the matrix. */
export const NOT_APPLICABLE = {
  'card press/doubleClick':
    'a card the decision offers (a site too: its action) answers it when pressed: card click, action menu and the effects scenarios press them',
  'decision buttons': 'Pass, Done, OK and the choices answer the decision: every scenario presses them',
  'concedeGame/cancelGame press': 'ends the game / asks the opponent: the concede scenarios press them',
  'replay play press': 'plays the replay on a clock: the replay scenario presses it and waits for the end',
  'range dragSideways': "the legacy slider's handle and the range input move by different steps for the same distance",
  'counter space': 'the legacy counters are not buttons: Space scrolls the page there',
};

const both = (name, pick) => ({ name, legacy: pick(LEGACY), angular: pick(ANGULAR_CONTRACT) });

const DIALOG_CLOSE = {
  name: 'dialog close',
  legacy: '.ui-dialog-titlebar-close:visible',
  angular: 'dialog[open] :is(.gd-dialog__close, .gd-card-info__close)',
};
const CARD_INFO_SLIDER = { name: 'card info size', legacy: '#card-info-slider', angular: '.gd-card-info__zoom' };

/** The steps testing one control: each gesture of its kind, then (`after`) what puts the page back, unseen. */
function stepsFor(control, kind, { after = [], card } = {}) {
  return GAME_KINDS[kind].flatMap((gesture) => [
    { gesture, ...(card ? { card } : { control }), name: `${(control ?? card).name}: ${gesture}` },
    ...after.map((step) => ({ ...step, quiet: true })),
  ]);
}

const CLOSE_DIALOGS = { closeDialogs: true };
const AWAY = { click: 'body' };
const PHASES = ['FELLOWSHIP', 'SHADOW', 'MANEUVER', 'ARCHERY', 'ASSIGNMENT', 'SKIRMISH', 'REGROUP'];

const card = (name, which) => ({ name, ...which });

/** The states and what is tested in each: one game each. */
export const GAME_MATRIX = [
  {
    name: 'matrix-bid',
    note: 'The bid: the arrows of its number',
    foe: PLAYER_FIRST,
    steps: [
      ...stepsFor(
        both('bid down', (s) => s.integerDown),
        'button',
      ),
      ...stepsFor(
        both('bid up', (s) => s.integerUp),
        'button',
      ),
    ],
  },
  {
    name: 'matrix-table',
    note: "The player's fellowship phase: the tabs, the switches, the counters and the cards",
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      ...['settings', 'options', 'players', 'chat'].flatMap((tab) =>
        stepsFor(
          both(`tab ${tab}`, (s) => s.tab(tab)),
          'button',
        ),
      ),
      ...stepsFor(
        both('chat input', (s) => s.chatInput),
        'text',
      ),
      ...['auto-zoom', 'timer-mute'].flatMap((name) =>
        stepsFor(
          both(name, (s) => s.toggle(name)),
          'button',
        ),
      ),
      ...[0, 1].flatMap((player) =>
        stepsFor(
          both(`counters + ${player}`, (s) => s.statsToggle(player)),
          'button',
        ),
      ),
      { stats: 0, quiet: true },
      { stats: 1, quiet: true },
      ...[
        ['adventureDeck', 0],
        ['discard', 0],
        ['deadPile', 0],
        ['removedPile', 0],
        ['deadPile', 1],
        ['removedPile', 1],
      ].flatMap(([pile, player]) =>
        stepsFor(
          both(`pile ${pile} ${player}`, (s) => s.pile(pile, player)),
          'counter',
          { after: [CLOSE_DIALOGS] },
        ),
      ),
      ...stepsFor(null, 'card', {
        card: card('site 1', { blueprint: '1_319', owner: 'any', zone: 'ADVENTURE_PATH' }),
        after: [AWAY],
      }),
      ...stepsFor(null, 'card', { card: card('Frodo', FRODO), after: [AWAY] }),
      ...stepsFor(null, 'card', { card: card('Gimli', GIMLI), after: [AWAY] }),
      ...stepsFor(null, 'card', { card: card('first card of the hand', { zone: 'HAND', nth: 0 }), after: [AWAY] }),
    ],
  },
  {
    name: 'matrix-settings',
    note: 'The settings tab: every box, the list, the slider and the timer alert fields',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { tab: 'settings', quiet: true },
      ...['autoAccept', 'autoDismissDeckReveal', 'use-old-stack-checkbox', 'alwaysDropDown']
        .concat(PHASES.map((phase) => `autoPass${phase}`))
        .flatMap((name) =>
          stepsFor(
            both(name, (s) => s.setting(name)),
            'checkbox',
          ),
        ),
      ...stepsFor(
        both('animation-slider', (s) => s.sliderHandle('animation-slider')),
        'range',
      ),
      ...['timerAlertBelow', 'timerAlertEvery'].flatMap((field) =>
        stepsFor(
          both(field, (s) => `${s.timerAlertField(field)} >> nth=0`),
          'text',
        ),
      ),
      // Last: the foil presentation changes how every card looks
      ...stepsFor(
        both('foilPresentation', (s) => s.setting('foilPresentation')),
        'select',
      ),
    ],
  },
  {
    name: 'matrix-options',
    note: 'The options tab: the buttons only looked at (pressing them ends the game)',
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { tab: 'options', quiet: true },
      ...['concedeGame', 'cancelGame'].flatMap((name) =>
        stepsFor(
          both(name, (s) => s.button(name)),
          'passive',
        ),
      ),
    ],
  },
  {
    name: 'matrix-dialogs',
    note: "A pile's dialog and the card information: their close buttons and the card size slider",
    foe: PLAYER_FIRST,
    steps: [
      ...TO_FELLOWSHIP,
      { stats: 0, quiet: true },
      // The close button: looked at, then each way of pressing it, the dialog opened again before each
      ...['pile', 'cardInfo'].flatMap((dialog) => {
        const open = dialog === 'pile' ? { pile: 'adventureDeck', player: 0 } : { cardInfo: FRODO };
        const control = { ...DIALOG_CLOSE, name: `${dialog} close` };
        return [
          { ...open, name: `${dialog} open` },
          ...['hover', 'leave', 'focus', 'press'].map((gesture) => ({
            gesture,
            control,
            name: `${control.name}: ${gesture}`,
          })),
          ...['enter', 'space', 'escape', 'doubleClick'].flatMap((gesture) => [
            // From the same page on both, whatever the gesture before did
            { ...CLOSE_DIALOGS, quiet: true },
            { ...open, quiet: true },
            { gesture, control, name: `${control.name}: ${gesture}` },
          ]),
        ];
      }),
      { cardInfo: FRODO, quiet: true },
      ...stepsFor(CARD_INFO_SLIDER, 'range'),
    ],
  },
  {
    name: 'matrix-replay',
    note: "A replay's buttons, before it plays",
    mode: 'replay',
    answers: 40,
    foe: [...PLAYER_FIRST, ...FOE_PLAYS_MINIONS],
    steps: [
      ...['slower', 'faster'].flatMap((name) =>
        stepsFor(
          both(`replay ${name}`, (s) => s.replay(name)),
          'button',
        ),
      ),
      ...stepsFor(
        both('replay play', (s) => s.replay('play')),
        'passive',
      ),
    ],
  },
];

/** Makes one gesture of a matrix step. `target` is a locator, or a card's point as one (cards are found by run.mjs). */
export async function makeGesture(page, gesture, target) {
  const make = GESTURES[gesture];
  if (!make) throw new Error(`unknown gesture ${gesture}`);
  await make(page, target);
}

/** A point as a locator for the gestures that only move the mouse there (the card gestures). */
export const pointTarget = ({ x, y }) => ({
  boundingBox: async () => ({ x: x - 0.5, y: y - 0.5, width: 1, height: 1 }),
});
