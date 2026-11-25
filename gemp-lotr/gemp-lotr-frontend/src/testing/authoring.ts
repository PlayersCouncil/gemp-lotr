/**
 * The scenario authoring server's answers, for tests: a draft's state as ScenarioAuthoring.describe builds it, with
 * what a test cares about changed, and the result a change answers with.
 */
import {
  AuthoringBoard,
  AuthoringDecision,
  AuthoringPosition,
  AuthoringResult,
  BoardCard,
  DraftState,
  DraftStep,
  OpponentOrder,
  OpponentState,
} from '../core/game/authoring/authoring-state';
import { ModifyVocabulary, PuzzleSlot, PuzzleState } from '../core/game/authoring/puzzle-state';

/** Steps 1 (the player's, in turn 1's fellowship), 2 (the opponent's, shadow) and 3 (a block with two steps). */
export const STEPS: DraftStep[] = [
  { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass', messages: [{ text: 'Hello' }, 'Two'] },
  { when: 'shadow, action, phase(shadow), turn(1)', answer: 'play(runner)' },
  {
    when: 'turn(1)',
    until: 'phase(regroup)',
    anyOrder: [
      { when: 'freeps, assign', once: true, answer: 'pass' },
      { when: 'freeps, action, inSkirmish(frodo)', allow: { answers: 'play(sword)' } },
    ],
  },
];

export function position(extra: Partial<AuthoringPosition> = {}): AuthoringPosition {
  return {
    step: 1,
    sub: 0,
    inHand: true,
    underWay: false,
    frontier: false,
    over: false,
    result: null,
    turn: 1,
    phase: 'fellowship',
    chosen: [],
    ...extra,
  };
}

export function decision(extra: Partial<AuthoringDecision> = {}): AuthoringDecision {
  return {
    seat: 'freeps',
    type: 'action',
    text: 'Play Fellowship action or Pass',
    where: 'turn 1, fellowship',
    pass: true,
    options: [],
    step: 1,
    sub: 0,
    held: false,
    ...extra,
  };
}

/** A draft's state: game 7, the steps above (or those given), at step 1 with the player's decision in hand. */
export function draftState(extra: Partial<DraftState> = {}): DraftState {
  const steps = extra.draft?.steps ?? STEPS;
  const uids = steps.map((_, i) => `s${i + 1}`);
  const kids = steps.map((step, i) => (step.anyOrder ?? []).map((_, k) => `s${i + 1}k${k + 1}`));
  return {
    session: true,
    gameId: '7',
    revision: 1,
    mode: 'author',
    testFrom: null,
    dirty: false,
    path: 'tutorials/test.hjson',
    file: 'tutorials/test.hjson',
    kindFolder: 'tutorials',
    fileOnSave: 'tutorials/test.hjson',
    mayPublish: false,
    onDisk: true,
    own: false,
    canonical: true,
    canSaveAnywhere: true,
    commentsLost: 0,
    leftOut: [],
    recording: true,
    askOpponent: true,
    way: [],
    flags: [],
    notice: null,
    stopped: null,
    draft: { id: 'test', title: 'A Test', kind: 'tutorial', human: 'freeps', steps },
    parts: {},
    statuses: steps.map(() => 'unreached'),
    places: steps.map(() => null),
    uids,
    kids,
    substatuses: kids.map((names) => names.map(() => 'unreached')),
    awaiting: [],
    texts: {},
    position: position(),
    waiting: { human: decision(), opponent: null },
    uncovered: null,
    pending: [],
    board: null,
    problems: [],
    ...extra,
  };
}

/** What a change answers with: the state, in the same game unless `rebuilt` names another. */
export function result(state: DraftState, extra: Partial<AuthoringResult> = {}): AuthoringResult {
  return { ok: true, gameId: state.gameId, events: false, rebuilt: false, state, ...extra };
}

/** The scenario info of a draft's game as the server sends it (its player writes it). */
export const AUTHOR_INFO = {
  type: 'info',
  id: 'test',
  kind: 'tutorial',
  title: 'A Test',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Tutorial',
  autoAccept: true,
  autoDismissDeckReveal: true,
  animationSpeed: null,
  bubbleGap: null,
  resumedAt: null,
  campaign: null,
  author: true,
  draftTest: false,
};

const xmlJson = (value: unknown) =>
  JSON.stringify(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

/** Game 7 of a draft, as the server's game state: the players, Frodo, the scenario info, and an action to choose. */
export function draftGameXml(info: Partial<typeof AUTHOR_INFO> = {}): string {
  return (
    '<gameState cn="1">' +
    '<ge type="P" participantId="asdf" allParticipantIds="asdf,~Scenario" discardPublic="false"/>' +
    '<ge type="PCIP" cardId="7" blueprintId="1_290" participantId="asdf" zone="FREE_CHARACTERS"/>' +
    `<ge type="S" scenario="${xmlJson({ ...AUTHOR_INFO, ...info })}"/>` +
    '<ge type="D" id="3" decisionType="CARD_ACTION_CHOICE" participantId="asdf" text="Play">' +
    '<parameter name="cardId" value="7"/><parameter name="blueprintId" value="inPlay"/>' +
    '<parameter name="actionId" value="12"/><parameter name="actionText" value="Use"/></ge>' +
    '</gameState>'
  );
}

/** A card of the board (Frodo, in the Free Peoples' characters, unless said otherwise). */
export function boardCard(alias: string, extra: Partial<BoardCard> = {}): BoardCard {
  return {
    alias,
    blueprintId: '10_121',
    name: 'Frodo, Resolute Hobbit',
    type: 'COMPANION',
    owner: 'freeps',
    cardId: 25,
    zone: 'characters',
    at: null,
    wounds: 0,
    tokens: 0,
    on: null,
    ...extra,
  };
}

/** The board of a new draft (Frodo and the Ring for each seat, the stock path of 3 sites), and two cards in hand. */
export function draftBoard(extra: Partial<AuthoringBoard> = {}): AuthoringBoard {
  const path = (opp: string) =>
    ['1_323', '1_329', '1_341'].map((id, at) => ({
      number: at + 1,
      id,
      alias: `site${at + 1}${opp}`,
      name: `Site ${id}`,
    }));
  return {
    cards: [
      boardCard('frodo'),
      boardCard('ring', {
        blueprintId: '1_2',
        name: 'The One Ring, The Ruling Ring',
        type: 'THE_ONE_RING',
        cardId: 26,
        zone: 'attached',
        on: 'frodo',
      }),
      boardCard('sam', { blueprintId: '1_311', name: 'Sam, Son of Hamfast', cardId: 30, zone: 'hand', at: 0 }),
      boardCard('merry', { blueprintId: '1_303', name: 'Merry, Friend to Sam', cardId: 31, zone: 'hand', at: 1 }),
      boardCard('oppFrodo', { owner: 'shadow', cardId: 27 }),
      boardCard('orc', {
        blueprintId: '1_191',
        name: 'Goblin Runner',
        type: 'MINION',
        owner: 'shadow',
        cardId: 32,
        zone: 'hand',
        at: 0,
      }),
    ],
    twilight: 0,
    burdens: 1,
    threats: 0,
    sites: { freeps: path(''), shadow: path('opp') },
    ...extra,
  };
}

/** The opponent as describe gives it: fallback pass, no orders, Frodo and Sam for leftovers, two runners to play. */
export function opponentState(extra: Partial<OpponentState> = {}): OpponentState {
  return {
    fallback: 'pass',
    fallbacks: ['fail', 'pass', 'bot', 'sandbox'],
    leftovers: null,
    leftoversChoices: ['ringBearer', 'frodo', 'sam'],
    plays: [],
    playsChoices: ['orc', 'orc2'],
    orders: [],
    answering: null,
    ...extra,
  };
}

/** One of the opponent's standing orders as describe gives it (unnamed, at `index`, playing orc in its shadow phase). */
export function opponentOrder(index: number, extra: Partial<OpponentOrder> = {}): OpponentOrder {
  return {
    index,
    id: null,
    note: null,
    once: false,
    when: 'shadow, action, phase(shadow)',
    answers: ['play(orc)'],
    spent: false,
    text: '',
    ...extra,
  };
}

/** A puzzle's slot as the state lists it (puzzle 10's Rohan condition, Parapet in it). */
export function puzzleSlot(id: string, extra: Partial<PuzzleSlot> = {}): PuzzleSlot {
  return {
    id,
    label: 'a Rohan condition',
    zone: 'support',
    seat: 'human',
    on: null,
    position: null,
    bound: 'set:1-6 culture:ROHAN cardtype:CONDITION',
    pool: 'set:1-6',
    filter: 'culture:ROHAN cardtype:CONDITION',
    overlap: [],
    comes: { tokens: 0, wounds: 0 },
    pick: '5_87',
    pickName: 'Parapet',
    ...extra,
  };
}

/** `state.puzzle` as describe gives it: nothing said (no judge, score, slots or solutions), the server's choices. */
export function puzzleState(extra: Partial<PuzzleState> = {}): PuzzleState {
  return {
    undo: true,
    undoSaid: null,
    judge: null,
    moments: {
      moments: ['startOf', 'endOf', 'endOfTurn', 'skirmishResolution'],
      phases: ['fellowship', 'shadow', 'maneuver', 'archery', 'assignment', 'skirmish', 'regroup'],
    },
    score: null,
    scoreValues: {
      totals: ['twilight', 'burdens', 'threats', 'site', 'turn', 'hand'],
      perCard: ['strength', 'vitality', 'resistance', 'wounds', 'tokens', 'cost', 'playCost'],
    },
    forbid: [],
    forbidChoices: ['shuffle', 'reveal'],
    pool: null,
    slots: [],
    slotZones: [
      'characters',
      'support',
      'minions',
      'hand',
      'deck',
      'discard',
      'deadPile',
      'attached',
      'stacked',
      'site',
    ],
    solutions: [],
    picks: {},
    playingWith: null,
    recording: null,
    verdict: null,
    ...extra,
  };
}

/** `state.modify`, the Modify… vocabulary: three stats and three keywords (one of each kind of number). */
export const MODIFY_WORDS: ModifyVocabulary = {
  words: [
    { what: 'strength', label: 'Strength', kind: 'stat', number: 'signed', types: ['COMPANION', 'ALLY', 'MINION'] },
    { what: 'resistance', label: 'Resistance', kind: 'stat', number: 'signed', types: ['COMPANION'] },
    {
      what: 'cost',
      label: 'Twilight cost',
      kind: 'stat',
      number: 'signed',
      types: ['COMPANION', 'ALLY', 'MINION', 'POSSESSION', 'EVENT', 'CONDITION'],
    },
    { what: 'damage', label: 'Damage', kind: 'keyword', number: 'positive', types: ['COMPANION', 'ALLY', 'MINION'] },
    { what: 'archer', label: 'Archer', kind: 'keyword', number: 'none', types: ['COMPANION', 'ALLY', 'MINION'] },
    { what: 'fierce', label: 'Fierce', kind: 'keyword', number: 'none', types: ['MINION'] },
  ],
  until: ['phase', 'turn', 'removed'],
};
