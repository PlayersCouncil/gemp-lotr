// What the game check must cover, and what it did: every gesture and control of the game page, every kind of game
// event and decision the server sends, every zone a card can be shown in. run.mjs records what each run used (page A),
// and a run of every scenario fails on anything not covered (like the deck builder's matrix, ../matrix.mjs).
import fs from 'node:fs';
import path from 'node:path';

const LOGIC = path.resolve(import.meta.dirname, '../../../gemp-lotr-logic/src/main/java/com/gempukku/lotro');
const COMMON = path.resolve(import.meta.dirname, '../../../gemp-lotr-common/src/main/java/com/gempukku/lotro');

/** The game page's gestures and controls, by the names run.mjs records when it uses them. */
export const GAME_CONTROLS = {
  // Decisions
  'bid spinner': 'the arrows of the number field (INTEGER)',
  'OK button': "the OK of a dialog's decision",
  'choice button': 'one answer of a multiple choice, as a button (two answers or fewer)',
  'choice list': 'a multiple choice as a drop-down list (more than two answers, or "always display drop-down")',
  'card click': 'a card clicked to choose it or play it',
  'action menu': "an action chosen from a card's menu of several",
  Pass: 'the Pass button',
  Done: 'the Done button',
  'Reset choice': 'the Reset choice button (choosing cards without auto-accept)',
  'Clear selection': 'the Clear selection button (choosing in a dialog without auto-accept)',
  'assign minion': 'a minion, then the character it goes to',
  'unassign minion': 'a minion clicked twice',
  'browser confirm': 'the browser asking to confirm (Done with a minion left unassigned)',
  // Cards
  'card info: right click': 'card information by a right click',
  'card info: shift click': 'card information by a shift click',
  'card info: drag up': 'card information by dragging a card up',
  'card info closed': 'card information closed by a click',
  'hover zoom': 'the zoom of a card the mouse rests on',
  'drag to reorder': 'a card dragged sideways in its row',
  // The box at the bottom left
  'tab chat': 'the chat tab',
  'tab settings': 'the settings tab',
  'tab options': 'the options tab',
  'tab players': 'the players tab',
  'chat message': 'a chat message typed and sent',
  'auto-zoom toggle': 'the auto-zoom switch',
  'timer mute': 'the mute switch of the timer sounds',
  'setting foilPresentation': 'the foil presentation list',
  'setting animation-slider': 'the animation speed slider',
  'setting autoAccept': 'the auto-accept box',
  'setting autoDismissDeckReveal': 'the auto-dismiss deck reveal box',
  'setting use-old-stack-checkbox': 'the old stacked cards box',
  'setting alwaysDropDown': 'the always display drop-down box',
  'setting autoPass': 'a phase box of auto-pass',
  'setting timer alerts': 'a field of the timer alert rules',
  concede: 'the Concede game button',
  'request cancel': 'the Request game cancel button',
  // The game state panel
  'counters +': "the + that shows a player's other counters",
  'pile discard': 'the discard pile',
  'pile deadPile': 'the dead pile',
  'pile adventureDeck': 'the adventure deck',
  'pile removedPile': 'the removed pile',
  'pile deck after the game': 'the own deck, once the game has ended',
  // Keys
  Escape: 'the Escape key',
  // Modes
  spectator: 'the page of a player who watches the game',
  replay: 'the page replaying a finished game',
  'replay play': 'the play button of a replay',
  'replay slower': 'the slower button of a replay',
  'replay faster': 'the faster button of a replay',
};

/**
 * Not expected, each with its reason (only what can't happen; anything the check merely doesn't do yet is a gap).
 * Kind: 'event', 'decision', 'zone' or 'control'.
 */
export const NOT_EXPECTED = {
  // GameCommunicationChannel sends a card only from a public zone, to its owner from a zone the owner sees, or when
  // forced (a revealed hand) or at the end of the game (the draw deck): never from the void
  'zone VOID': 'never sent to a page: no player sees the void',
  'zone VOID_FROM_HAND': 'never sent to a page: no player sees the void',
  'event CM': 'declared in GameEvent.Type but never created by the server',
};

function javaEnum(file, pattern) {
  const text = fs.readFileSync(file, 'utf8');
  return [...text.matchAll(pattern)].map((match) => match[1]);
}

/** Everything the check must cover, as "kind name" keys with what each is. */
export function everything() {
  const items = {};
  const events = javaEnum(path.join(LOGIC, 'game/state/GameEvent.java'), /\b[A-Z_]+\("([A-Z]+)"\)/g);
  for (const code of events) items[`event ${code}`] = `game event ${code}`;
  const decisions = fs
    .readFileSync(path.join(LOGIC, 'logic/decisions/AwaitingDecisionType.java'), 'utf8')
    .match(/\{([^}]*)\}/)[1]
    .split(',')
    .map((name) => name.trim())
    .filter(Boolean);
  for (const type of decisions) items[`decision ${type}`] = `decision ${type}`;
  const zones = javaEnum(path.join(COMMON, 'common/Zone.java'), /^\s{4}([A-Z_]+)\(/gm);
  for (const zone of zones) items[`zone ${zone}`] = `a card in ${zone}`;
  for (const [name, what] of Object.entries(GAME_CONTROLS)) items[`control ${name}`] = what;
  return items;
}

/** What a run of page A covered: the game's events, decisions and zones it received, and the controls it used. */
export function coveredBy(events, controls) {
  const covered = new Set();
  for (const event of events) {
    covered.add(`event ${event.type}`);
    if (event.type === 'D') covered.add(`decision ${event.decisionType}`);
    if (event.zone) covered.add(`zone ${event.zone}`);
  }
  for (const control of controls) covered.add(`control ${control}`);
  return covered;
}

/** What every scenario together didn't cover, apart from what isn't expected: "kind name: what it is". */
export function gaps(covered) {
  return Object.entries(everything())
    .filter(([key]) => !covered.has(key) && !(key in NOT_EXPECTED))
    .map(([key, what]) => `${key}: ${what}`);
}
