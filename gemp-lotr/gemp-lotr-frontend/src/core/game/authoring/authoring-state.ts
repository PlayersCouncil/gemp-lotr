/**
 * What the server's scenario authoring tool (ScenarioAuthoring.java, under /scenarios/author/) answers with: the state
 * of the author's draft and of the game it is being written in (`describe`), the draft itself as its file has it
 * (ScenarioWriter.toJson), and what a change comes back with (`result`). Types only: the server builds every answer
 * from the same few methods, so the panel takes them as they come.
 *
 * The draft's own shapes are as loose as the file format is: a step's When can be a line of terms or an object, a
 * message a string or an object, and so on. Each such part is typed as the union it is.
 */

/** A seat as the draft names it. */
export type DraftSeat = 'freeps' | 'shadow';
export type ScenarioKind = 'tutorial' | 'puzzle' | 'sandbox';
/** What became of a step in the game in hand (StepStatus, lower-cased). */
export type StepStatus = 'unreached' | 'matched' | 'skipped' | 'bypassed';
/** What a decision is, in the scenario's words (AnswerCompiler.typeName). */
export type DecisionKind = 'action' | 'actionChoice' | 'cards' | 'arbitrary' | 'choice' | 'number' | 'assign';
/** The panel's tabs. */
export type AuthoringTab = 'timeline' | 'step' | 'opponent' | 'board' | 'scenario';

/* ------------------------------------------------------------------ the draft (ScenarioWriter.toJson) */

/** A requirement of the game's own (`{type: canSpot, filter: ...}`): whatever its type takes. */
export type DraftRequirement = Readonly<Record<string, unknown>>;

/**
 * A When (or an Until, a Skip when) written out in full. The server never writes `terms` back; the panel's copy of a
 * When it has split into terms and requirements may have it.
 */
export interface DraftWhen {
  seat?: string;
  decision?: string;
  text?: string;
  phase?: string;
  turn?: number | string;
  skirmish?: number | string;
  offers?: string;
  inSkirmish?: string;
  fighting?: string;
  after?: string;
  terms?: string;
  require?: DraftRequirement | readonly DraftRequirement[];
  /** Anything else a When can say, which the panel shows as `{...}`. */
  readonly [part: string]: unknown;
}

/** A When, Until or Skip when: a line of terms ("freeps, action, phase(fellowship)") or written out in full. */
export type DraftCondition = string | DraftWhen | readonly DraftRequirement[];

export interface DraftMessage {
  note?: string;
  header?: string;
  text: string;
  anchor?: string;
  highlight?: string;
  /** Absent: gold. */
  highlightStyle?: 'gold' | 'red' | 'green' | 'blue';
  avoid?: string;
  /** Absent: wherever fits best. */
  side?: 'above' | 'below' | 'left' | 'right';
  gap?: string | number;
  /** Only ever false (true is the default). */
  once?: boolean;
  advance?: string;
  on?: string;
  zoom?: string;
  focus?: string;
  magnify?: string | number;
  /** A pause's last message ends in these buttons: name to label. */
  choices?: Readonly<Record<string, string>>;
  /** Said only while these hold (requirements as a card's, as `when.require`): one, or a list of them all. */
  require?: DraftRequirement | readonly DraftRequirement[];
}

/** A message: just its text, or an object. */
export type DraftMessageValue = string | DraftMessage;

export interface DraftAllow {
  answers?: string;
  passWhen?: DraftCondition;
  otherwise?: string;
  hints?: Readonly<Record<string, string>>;
}

export interface DraftEnd {
  result: 'complete' | 'fail';
  messages?: readonly DraftMessageValue[] | DraftMessageValue;
}

export interface DraftStep {
  id?: string;
  note?: string;
  checkpoint?: boolean;
  pause?: boolean;
  once?: boolean;
  /** Comma-separated flags. */
  if?: string;
  unless?: string;
  when?: DraftCondition;
  until?: DraftCondition;
  skipWhen?: DraftCondition;
  /** Comma-separated `do` terms. */
  do?: string;
  messages?: readonly DraftMessageValue[] | DraftMessageValue;
  allow?: string | DraftAllow;
  /** Comma-separated answers, in the order the script plays them. */
  answer?: string;
  end?: 'complete' | 'fail' | DraftEnd;
  skirmishes?: boolean;
  /** A block: its steps. */
  anyOrder?: readonly DraftStep[];
  /** Answers its `allow` must refuse (the runner proves it): a line of terms, or lines; "2: x" is its 2nd decision. */
  refuses?: string | readonly string[];
}

/** One seat's cards: alias to blueprint id. */
export type DraftCards = Readonly<Record<string, string>>;

export interface DraftOptions {
  /** Comma-separated phases the player is passed through. */
  autoPass?: string | boolean;
  /** Only ever false. */
  autoAccept?: boolean;
  autoDismissDeckReveal?: boolean;
  /** -4 to 4. */
  animationSpeed?: number;
  rngDefault?: unknown;
  opponentDelayMs?: number;
  runnerLevel?: unknown;
  bubbleGap?: string;
}

/** The scenario as its file has it (defaults left out). */
export interface Draft {
  id: string;
  version?: number;
  kind?: ScenarioKind;
  title?: string;
  summary?: string;
  author?: string;
  tags?: string;
  note?: string;
  format?: string;
  human?: DraftSeat;
  opponent?: {
    name?: string;
    /** Absent: fail. */
    fallback?: 'pass';
    orders?: readonly { when: DraftCondition; answer: string }[];
  };
  coverage?: 'strict' | 'guided';
  options?: DraftOptions;
  cards?: Partial<Record<DraftSeat, DraftCards>>;
  ringBearer?: Partial<Record<DraftSeat, string>>;
  ring?: Partial<Record<DraftSeat, string>>;
  /** Each seat's path: "id, id, ..." or, where a site has a name of its own, alias to id. */
  sites?: Partial<Record<DraftSeat, string | Readonly<Record<string, string>>>>;
  rules?: {
    oneSided?: boolean;
    freeps?: readonly DraftRequirement[];
    shadow?: readonly DraftRequirement[];
  };
  setup?: {
    startAt?: { turn?: number; phase?: string };
    freeps?: Readonly<Record<string, string>>;
    shadow?: Readonly<Record<string, string>>;
    attach?: Readonly<Record<string, string>>;
    stack?: Readonly<Record<string, string>>;
    wounds?: Readonly<Record<string, number>>;
    tokens?: Readonly<Record<string, number>>;
    burdens?: number;
    threats?: number;
  };
  rng?: string | readonly string[];
  outcome?: {
    win?: DraftRequirement | readonly DraftRequirement[];
    lose?: DraftRequirement | readonly DraftRequirement[];
    loseMessage?: string;
    offScript?: { message?: string; result?: string };
  };
  /** The server always sends it; the panel treats a missing one as none. */
  steps?: readonly DraftStep[];
}

/* ------------------------------------------------------------------ the state (describe) */

/** Where the game in hand stands in the script. */
export interface AuthoringPosition {
  /** The script's position, from 1; the number of steps + 1 past the end. */
  step: number;
  /** Which of a block's steps is in hand, from 1; 0 for none. */
  sub: number;
  /** A step has the waiting decision. */
  inHand: boolean;
  /** No step in hand, and the current one has applied at least once: one that repeats, or a block, between decisions. */
  underWay: boolean;
  /** At the end of the script, with nothing in hand. */
  frontier: boolean;
  over: boolean;
  /** How the game ended ("complete", "failed", ...), lower-cased; null while it goes on. */
  result: string | null;
  /** 0 before the table is laid out. */
  turn: number;
  /** The phase, lower-cased ("fellowship", "between_turns"); null before the table is laid out. */
  phase: string | null;
  /** The flags chosen in this game. */
  chosen: readonly string[];
}

/** One of the answers a decision offers. */
export interface DecisionOffer {
  /** The answer in the scenario's words (an action, a choice); null for a card of a choice of cards, an assignment. */
  term: string | null;
  label: string;
  /** The card's name in the scenario, if it has one. */
  alias: string | null;
  /** The card's id, or its temporary id. */
  id: string | null;
}

/** A decision that is waiting (describeDecision). */
export interface AuthoringDecision {
  seat: DraftSeat;
  type: DecisionKind;
  /** The decision's text, its markup taken out. */
  text: string;
  /** "turn 3, fellowship". */
  where: string;
  min?: number;
  max?: number;
  options: readonly DecisionOffer[];
  /** Present (true) where Pass is one of the answers: an action, an assignment. */
  pass?: boolean;
  /** An assignment's characters. */
  characters?: readonly DecisionOffer[];
  /** The number of the step whose ruling has it, or null. */
  step: number | null;
  /** Which of a block's steps (from 1), or 0. */
  sub: number;
  /** The opponent's, held for the author to answer. */
  held: boolean;
  /** What it is for, as a When names it (nextSkirmish, phaseActions, ...), or null for none of those. */
  kind?: string | null;
  /** The player's, with a step in hand: how each of the step's refusals stands at it. */
  refuses?: readonly DecisionRefusal[];
}

/** One of a step's refusals, at the decision in hand (state.waiting.human.refuses). */
export interface DecisionRefusal {
  /** As the file has it: "2: choose(boromir)". */
  refusal: string;
  /** The step's decision it is about, from 1; 0 for every one. */
  at: number;
  /** refused (as it should be), allowed (the runner would fail), notOffered, elsewhere (another decision's). */
  now: 'refused' | 'allowed' | 'notOffered' | 'elsewhere';
}

/** The numbers from the game a message's text can quote ({twilight}, {strength:alias}, {hand:shadow}). */
export interface AuthoringTemplates {
  totals: readonly string[];
  perCard: readonly string[];
  perSeat: readonly string[];
}

/** A waiting decision that no step covers, and what adding a step for it would do. */
export interface UncoveredDecision {
  seat: 'human' | 'opponent';
  /** Where among the steps the new one would go (0: at the start); for a block, the block's place. */
  after: number;
  /** The new step's When. */
  when: string;
  /** The step that repeats and would give way to it, or null. */
  repeating: number | null;
  /** That step's Until as it is, and what it would become. */
  until: string | null;
  becomes: string | null;
  /** Why no step can be written for it, or null. */
  problem: string | null;
  /** The block the step would go into, or null; and the number it would have there (0: no block). */
  block: number | null;
  sub: number;
  /** A step that waits for an answer and says nothing yet, which would be the one, or null. */
  reuses: number | null;
}

/** A card of the scenario on the table, by its name in the scenario. */
export interface BoardCard {
  alias: string;
  blueprintId: string;
  name: string;
  /** The card type's enum name ("COMPANION", "MINION", "THE_ONE_RING"). */
  type: string;
  owner: DraftSeat;
  cardId: number;
  /** Lower-cased: characters, support, minions, hand, deck, discard, deadPile, removed, attached, stacked, ... */
  zone: string;
  /** Its place in a hand or a pile (0: the top); null on the table. */
  at: number | null;
  wounds: number;
  /** Culture tokens of its own culture. */
  tokens: number;
  /** The card it is attached to or stacked on, by alias. */
  on: string | null;
}

export interface BoardSite {
  number: number;
  id: string;
  alias: string | null;
  name: string | null;
}

/** The table as the scenario names it (describeBoard). */
export interface AuthoringBoard {
  cards: readonly BoardCard[];
  twilight: number;
  burdens: number;
  threats: number;
  sites: Readonly<Record<DraftSeat, readonly BoardSite[]>>;
}

/** A part of the file that was left out so that it could be opened at all. */
export interface LeftOutPart {
  /** "step 12 (...)", "cards", ... */
  where: string;
  message: string;
  /** What it said. */
  was: string;
  /** The step of the file it was in, from 1; 0 for none. */
  step: number;
  /** That step's name now, or null (no step, or the whole step went). */
  uid: string | null;
}

/** Where a step had its first decision, as far as it has been seen. */
export interface StepPlace {
  turn: number;
  phase: string;
}

/** The parts of a step written out in full ({ ... }), as the file has them (HJSON text). */
export interface StepTexts {
  when?: string;
  until?: string;
  skipWhen?: string;
  whenRequire?: string;
  passWhen?: string;
  /** A message's condition, by its place among the step's messages (from 0). */
  readonly [message: `messages.${number}.require`]: string | undefined;
}

/** No draft is open. */
export interface NoDraft {
  session: false;
}

/** A draft is open: it, and the game it is being written in. */
export interface DraftState {
  session: true;
  /** The draft's current game (the server sends it as a string; a number would do as well). */
  gameId: string | number;
  /** Bumped on each change to the draft (not to where its game stands). */
  revision: number;
  /** A game for writing in, or a player's game of the draft. */
  mode: 'author' | 'test';
  /** The step a test game began at. */
  testFrom: number | null;
  /** Changes that are in no file. */
  dirty: boolean;
  /** Its file as shown: users/<owner>/<path> or <path>. */
  path: string | null;
  /** Its file without the owner's folder. */
  file: string | null;
  kindFolder: string;
  fileOnSave: string;
  mayPublish: boolean;
  onDisk: boolean;
  own: boolean;
  canonical: boolean;
  canSaveAnywhere: boolean;
  /** Lines of comment in the file that saving would drop. */
  commentsLost: number;
  leftOut: readonly LeftOutPart[];
  /** "Write down what I do". */
  recording: boolean;
  /** "Ask me for the opponent". */
  askOpponent: boolean;
  /** The forks' choices taken when the script plays itself. */
  way: readonly string[];
  /** Every flag the draft's pauses can set. */
  flags: readonly string[];
  /** The last thing the server said (where the script stopped, ...). */
  notice: string | null;
  /** Why the script stopped, while that is still the notice. */
  stopped: 'uncovered' | 'step' | null;
  draft: Draft;
  /** Each top-level part but the steps, as the file writes it. */
  parts: Readonly<Record<string, string>>;
  /** Per step. */
  statuses: readonly StepStatus[];
  places: readonly (StepPlace | null)[];
  /** The steps' names, stable across changes. */
  uids: readonly string[];
  /** Per step, its block's steps' names ([] for a step that is no block); and what became of each. */
  kids: readonly (readonly string[])[];
  substatuses: readonly (readonly StepStatus[])[];
  /** Steps that wait for an answer: what is done at them becomes it. */
  awaiting: readonly string[];
  /** By step name. */
  texts: Readonly<Record<string, StepTexts>>;
  position: AuthoringPosition;
  waiting: { human: AuthoringDecision | null; opponent: AuthoringDecision | null };
  uncovered: UncoveredDecision | null;
  /** `do` terms waiting for a step to carry them. */
  pending: readonly string[];
  /** Null until the table is laid out. */
  board: AuthoringBoard | null;
  /** What has gone wrong with the script in this game, oldest first. */
  problems: readonly string[];
  /** What a message's text can quote from the game (Templates.names). */
  templates?: AuthoringTemplates;
}

export type AuthoringState = NoDraft | DraftState;

/* ------------------------------------------------------------------ what calls answer */

/** What a save says of the file it wrote. */
export interface SaveReport {
  path: string;
  id: string;
  playable: boolean;
  verified: boolean;
  errors: readonly string[];
  warnings: readonly string[];
  report: string | null;
  /** A plain save that moved the file to where its kind is kept. */
  movedFrom?: string;
}

/**
 * What a change answers with: a fresh game to go to (`rebuilt`), or the game in hand was sent something it must ask
 * for (`events`), and the state.
 */
export interface AuthoringResult {
  ok: true;
  gameId: string | number;
  events: boolean;
  rebuilt: boolean;
  state: DraftState;
  /** Save, Save as. */
  saved?: SaveReport;
  /** Rename: the file's new path as shown. */
  renamed?: string;
}

/** A card found by name (findCards). */
export interface FoundCard {
  id: string;
  name: string;
  /** Lower-case card type. */
  type: string;
  culture: string | null;
  side: string | null;
  cost: number | null;
  /** Sites only. */
  site?: number;
}

export interface CheckReport {
  passed: boolean;
  report: string | null;
  warnings: readonly string[];
}

export interface OffScriptReport {
  tries: number;
  handled: boolean;
  report: string | null;
}

/** Whether a draft is open. */
export function hasDraft(state: AuthoringState | null): state is DraftState {
  return state?.session === true;
}
