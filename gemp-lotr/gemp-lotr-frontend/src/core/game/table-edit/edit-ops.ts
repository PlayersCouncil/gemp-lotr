/**
 * The changes the on-table edit controls make, exactly as the sandbox takes them (`POST scenarios/sandbox/edit`, its
 * `ops`; ScenarioSandbox.java): one vocabulary for every level. A card is named by its current card id (`cardId`,
 * the number the page's game view has) or by its name in the scenario (`card`, its alias). A backend that is not the
 * sandbox's translates from these. Pure.
 */

export type EditSeat = 'freeps' | 'shadow';

/** The kinds of change a level may allow (`tools` in the scenario's info), in the server's order. */
export const EDIT_TOOLS = [
  'add',
  'copy',
  'delete',
  'move',
  'attach',
  'stack',
  'wounds',
  'tokens',
  'modify',
  'counters',
  'markers',
  'sites',
  'piles',
] as const;
export type EditTool = (typeof EDIT_TOOLS)[number];

/**
 * Where a card can be put by `add` or `move`: a zone of the table, a hand, or a pile (the top or the bottom of the draw
 * deck).
 */
export const EDIT_ZONES = [
  'characters',
  'support',
  'minions',
  'hand',
  'deck.top',
  'deck.bottom',
  'discard',
  'deadPile',
  'removed',
] as const;
export type EditZone = (typeof EDIT_ZONES)[number];

/** A place a card can be added to: a seat's zone or pile (an open spot's "where"). */
export interface EditPlace {
  seat: EditSeat;
  zone: EditZone;
}

/** How a card is named: by its card id (the page's), or by its name in the scenario. */
export type CardName = { cardId: number; card?: never } | { card: string; cardId?: never };

/** How long a hand-put modifier lasts: `phase`, `turn`, or until it is taken off (left out). */
export type ModifierUntil = 'phase' | 'turn' | 'removed';

export type EditOp =
  | { op: 'add'; blueprint: string; to: string }
  | { op: 'add'; blueprint: string; attachId: number; seat?: EditSeat }
  | { op: 'add'; blueprint: string; stackId: number; seat?: EditSeat }
  | ({ op: 'copy' } & CardName)
  | ({ op: 'delete' } & CardName)
  | ({ op: 'move'; to: string } & CardName)
  | ({ op: 'attach'; toId: number } & CardName)
  | ({ op: 'stack'; toId: number } & CardName)
  | ({ op: 'wounds'; amount: number } & CardName)
  | ({ op: 'tokens'; amount: number; culture?: string } & CardName)
  | { op: 'twilight' | 'burdens' | 'threats'; set: number }
  | { op: 'twilight' | 'burdens' | 'threats'; amount: number }
  | { op: 'marker'; seat: EditSeat; site: number }
  | { op: 'control'; site: number; seat: EditSeat }
  | { op: 'liberate'; site: number }
  | { op: 'site'; number: number; from: EditSeat }
  | { op: 'site'; number: number; blueprint: string; seat?: EditSeat }
  | { op: 'site'; number: number; card: string }
  | ({ op: 'modify'; what: string; amount?: number; until?: 'phase' | 'turn' } & CardName)
  | ({ op: 'unmodify'; what?: string } & CardName)
  | { op: 'unmodify'; id: number }
  // A puzzle's slot filled with a card (a blueprint), or emptied (null): held on the page until Submit (PuzzleBackend)
  | { op: 'pick'; slot: string; blueprint: string | null };

/** What a place is called in an edit: `freeps.characters`, `shadow.deck.bottom`. */
export function placeTarget(place: EditPlace): string {
  return `${place.seat}.${place.zone}`;
}

/** The other seat. */
export function otherSeat(seat: EditSeat): EditSeat {
  return seat === 'freeps' ? 'shadow' : 'freeps';
}

/** A card of the page (its id is text there) as an edit names it; null for an id that is not a card's number. */
export function byId(cardId: string): { cardId: number } | null {
  const id = Number(cardId);
  return /^\d+$/.test(cardId) && Number.isSafeInteger(id) ? { cardId: id } : null;
}

/** Puts a card of the blueprint in the place. */
export function addTo(blueprint: string, place: EditPlace): EditOp {
  return { op: 'add', blueprint, to: placeTarget(place) };
}

/** The tool an edit needs (as the server checks it). */
export function toolOf(op: EditOp): EditTool {
  switch (op.op) {
    case 'add':
    case 'copy':
    case 'delete':
    case 'move':
    case 'attach':
    case 'stack':
    case 'wounds':
    case 'tokens':
      return op.op;
    case 'twilight':
    case 'burdens':
    case 'threats':
      return 'counters';
    case 'marker':
    case 'control':
    case 'liberate':
      return 'markers';
    case 'site':
      return 'sites';
    case 'modify':
    case 'unmodify':
      return 'modify';
    case 'pick':
      // (a card put in a slot is a card added, to its slot's place)
      return 'add';
  }
}
