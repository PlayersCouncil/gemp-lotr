/**
 * What the authoring state says of the parts of a scenario that puzzles, sandboxes and modifiers brought (the engine
 * notes ep-notes.md, es-notes.md sections 4 and 5, em-notes.md): `state.puzzle` (ScenarioAuthoring.describePuzzle),
 * `state.kinds` and `state.sandboxTools`, `state.modify` (the Modify… vocabulary), a board card's `modifiers` and the
 * setup's `start.modifiers`. Types only.
 */

/** When a judge looks (P1): the moment as the state splits it. */
export type JudgeMomentKind = 'startOf' | 'endOf' | 'endOfTurn' | 'skirmishResolution';

/** outcome.judge, read (null with none). */
export interface PuzzleJudge {
  /** As the file writes it: "endOf(archery)", "endOf(archery), turn(2)", "endOfTurn". */
  at: string;
  moment: JudgeMomentKind;
  /** For startOf / endOf; null for the others. */
  phase: string | null;
  turn: number | null;
  /** Its requirements as the file writes them (HJSON text), or null for none. */
  win: string | null;
}

/** outcome.score, read (null with none). */
export interface PuzzleScore {
  /** What message text quotes ("strength:smeagol"), or null when the value is a card definition's object... */
  value: string | null;
  /** ...which is then this (its HJSON text). */
  valueText: string | null;
  better: 'higher' | 'lower';
  label: string | null;
}

/** A slot as the info event and the state list it (P2, P3). */
export interface PuzzleSlot {
  id: string;
  label: string | null;
  zone: string;
  seat: 'human' | 'opponent';
  /** The declared card it is attached to or stacked on. */
  on: string | null;
  position: number | null;
  /** The pool and the filter as one filter text. */
  bound: string;
  pool: string | null;
  filter: string | null;
  /** Keys the pool and the filter both say (one text would find either for them). */
  overlap: readonly string[];
  comes: { readonly tokens: number; readonly wounds: number };
  /** The blueprint of the game in hand's card for it, and its name; null while it is empty. */
  pick: string | null;
  pickName: string | null;
}

/** One of the author's solutions (P4). */
export interface PuzzleSolution {
  name: string;
  note: string | null;
  /** Slot id to blueprint. */
  picks: Readonly<Record<string, string>>;
  /** Slot id to the card's name. */
  pickNames: Readonly<Record<string, string>>;
  answers: readonly string[];
  /** The whole solution as the file has it (HJSON). */
  text: string;
}

/** How the game in hand was judged (null before it has been). */
export interface PuzzleVerdict {
  won: boolean;
  score: number | null;
  moment: string;
}

/** `state.puzzle` (always sent by a server that knows puzzles). */
export interface PuzzleState {
  /** Whether take-backs apply... */
  undo: boolean;
  /** ...and what the file says (null: as the kind has it). */
  undoSaid: 'on' | 'off' | null;
  judge: PuzzleJudge | null;
  moments: { readonly moments: readonly JudgeMomentKind[]; readonly phases: readonly string[] };
  score: PuzzleScore | null;
  scoreValues: { readonly totals: readonly string[]; readonly perCard: readonly string[] };
  forbid: readonly string[];
  forbidChoices: readonly string[];
  pool: string | null;
  slots: readonly PuzzleSlot[];
  slotZones: readonly string[];
  solutions: readonly PuzzleSolution[];
  /** The game in hand's picks. */
  picks: Readonly<Record<string, string>>;
  /** The solution whose picks those are (null: none, or they differ). */
  playingWith: string | null;
  /** The solution being recorded. */
  recording: string | null;
  verdict: PuzzleVerdict | null;
}

/** `state.sandboxTools`: what a sandbox's player may be allowed to change (options.tools). */
export interface SandboxTools {
  names: readonly string[];
  notBuilt: readonly string[];
  /** Whether the file says tools (false: every tool). */
  said: boolean;
  allowed: readonly string[];
}

/** One word of the Modify… vocabulary. */
export interface ModifyWord {
  what: string;
  label: string;
  kind: 'stat' | 'keyword';
  /** signed: a stat's non-zero change; positive: a numbered keyword's 1 or more; none. */
  number: 'signed' | 'positive' | 'none';
  /** The card types (CardType names) it suits. */
  types: readonly string[];
}

/** `state.modify`. */
export interface ModifyVocabulary {
  words: readonly ModifyWord[];
  until: readonly string[];
}

/** A modifier put on a card by hand, as a card of the board lists it (still in force). */
export interface CardModifier {
  id: number;
  what: string;
  /** Null for a keyword that takes no number. */
  amount: number | null;
  until: string;
  /** "Strength +2". */
  text: string;
  /** "strength +2 turn": as setup.modifiers writes it. */
  written: string;
}

/** One of the setup's modifiers on a card (`start.modifiers[alias]`). */
export interface StartModifier {
  what: string;
  amount: number | null;
  until: string;
  written: string;
}
