/**
 * A puzzle's slots as the scenario's info event tells the page (ep-notes.md P3: LiveScenario.slotInfo), and what the
 * puzzle page works out from them: where on the table each one is shown, what it is called there, its search, and
 * which are still empty. Read defensively: a slot without an id or a zone is left out. Pure.
 */
import { SearchBound } from './card-search';
import { slotFilter } from './slot-filter';

/** What a slot's card comes with when it is put in place. */
export interface SlotComes {
  tokens: number;
  wounds: number;
}

export interface PuzzleSlot {
  /** The name its card goes by in the scenario. */
  id: string;
  /** What the page calls it ("a Rohan condition"), or null. */
  label: string | null;
  /** characters, support, minions, hand, deck, discard, deadPile, attached, stacked, site. */
  zone: string;
  seat: 'human' | 'opponent';
  /** For attached / stacked: the declared card (its alias) it goes on. */
  on: string | null;
  /** For deck: its place from the top (1 = the top). */
  position: number | null;
  /** The pool and the filter as one text (the server's; the page searches with `pool` and `filter`: slotFilter). */
  bound: string;
  pool: string | null;
  filter: string | null;
  /** The keys the pool and the filter both say. */
  overlap: readonly string[];
  comes: SlotComes;
  /** The card in it as the game was built (null while held, waiting for picks), and its name. */
  pick: string | null;
  pickName: string | null;
  /** A site slot's number on the path, where the server says it (or its id says it: `site2`); else null. */
  siteNumber: number | null;
}

type Json = Record<string, unknown>;
const object = (value: unknown): Json =>
  typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as Json) : {};
const text = (value: unknown): string | null => (typeof value === 'string' && value !== '' ? value : null);
const count = (value: unknown): number =>
  typeof value === 'number' && Number.isFinite(value) && value > 0 ? Math.floor(value) : 0;
const whole = (value: unknown): number | null =>
  typeof value === 'number' && Number.isInteger(value) && value > 0 ? value : null;

function slot(value: unknown): PuzzleSlot | null {
  const json = object(value);
  const id = text(json['id']);
  const zone = text(json['zone']);
  if (id === null || zone === null) return null;
  const comes = object(json['comes']);
  const pool = text(json['pool']);
  const filter = text(json['filter']);
  // (a site slot is one of the scenario's sites by its name: `site2` unless the file named it otherwise)
  const named = zone === 'site' ? /^site(\d)$/.exec(id) : null;
  return {
    id,
    label: text(json['label']),
    zone,
    seat: json['seat'] === 'opponent' ? 'opponent' : 'human',
    on: text(json['on']),
    position: whole(json['position']),
    bound: text(json['bound']) ?? [pool, filter].filter((part) => part !== null).join(' '),
    pool,
    filter,
    overlap: Array.isArray(json['overlap'])
      ? json['overlap'].filter((key): key is string => typeof key === 'string')
      : [],
    comes: { tokens: count(comes['tokens']), wounds: count(comes['wounds']) },
    pick: text(json['pick']),
    pickName: text(json['pickName']),
    siteNumber: whole(json['siteNumber']) ?? (named ? Number(named[1]) : null),
  };
}

/** The slots of an info event (its `slots`), in the scenario's order. */
export function readPuzzleSlots(value: unknown): PuzzleSlot[] {
  return (Array.isArray(value) ? value : []).flatMap((item: unknown) => {
    const read = slot(item);
    return read ? [read] : [];
  });
}

/** What a slot is called on the page: its label, else its name. */
export function slotLabel(slot: PuzzleSlot): string {
  return slot.label ?? slot.id;
}

const ordinal = (n: number) => {
  const tens = n % 100;
  const suffix = tens >= 11 && tens <= 13 ? 'th' : (['th', 'st', 'nd', 'rd'][n % 10] ?? 'th');
  return `${n}${suffix}`;
};

const PLACES: Readonly<Record<string, string>> = {
  characters: 'Characters',
  support: 'Support',
  minions: 'Minions',
  hand: 'Hand',
  deck: 'Deck',
  discard: 'Discard',
  deadPile: 'Dead pile',
  site: 'Site',
};

/** Where a slot's card goes, in a word or two: "Support", "Deck, 3rd", "On frodo", "Their hand". */
export function slotPlace(slot: PuzzleSlot): string {
  let place: string;
  if (slot.zone === 'deck') place = `Deck, ${ordinal(slot.position ?? 1)}`;
  else if (slot.zone === 'attached') place = `On ${slot.on ?? '?'}`;
  else if (slot.zone === 'stacked') place = `Under ${slot.on ?? '?'}`;
  else if (slot.zone === 'site') place = slot.siteNumber === null ? 'Site' : `Site ${slot.siteNumber}`;
  else place = PLACES[slot.zone] ?? slot.zone;
  return slot.seat === 'opponent' && slot.zone !== 'site' ? `Theirs: ${place}` : place;
}

/** The rows of the table a slot can be shown in, as the game page names them. */
export type SlotRow =
  'supportOpponent' | 'charactersOpponent' | 'minions' | 'charactersPlayer' | 'supportPlayer' | 'hand';

/**
 * The row of the table a slot is shown in, or null for a place the table has no row for (a draw deck's place, a pile,
 * on a card, a site, the opponent's hand): those are shown in the slots strip.
 */
export function slotRow(slot: PuzzleSlot): SlotRow | null {
  const mine = slot.seat === 'human';
  switch (slot.zone) {
    case 'characters':
      return mine ? 'charactersPlayer' : 'charactersOpponent';
    case 'support':
      return mine ? 'supportPlayer' : 'supportOpponent';
    case 'minions':
      return 'minions';
    case 'hand':
      return mine ? 'hand' : null;
    default:
      return null;
  }
}

/** A slot's search: within its bound (the pool and its filter, each by itself), labelled by its label. */
export function slotSearch(slot: PuzzleSlot): SearchBound & { exact: boolean } {
  const { filter, exact } = slotFilter(slot.pool, slot.filter, slot.zone, slot.siteNumber);
  return { filter, exact, label: slotLabel(slot) };
}

/** What a slot comes with, in a word or two ("3 tokens, 1 wound"), or null for nothing. */
export function comesText(comes: SlotComes): string | null {
  const parts = [
    ...(comes.tokens > 0 ? [`${comes.tokens} token${comes.tokens === 1 ? '' : 's'}`] : []),
    ...(comes.wounds > 0 ? [`${comes.wounds} wound${comes.wounds === 1 ? '' : 's'}`] : []),
  ];
  return parts.length === 0 ? null : parts.join(', ');
}

/** The slots still without a card, by what the page calls them. */
export function emptySlots(slots: readonly PuzzleSlot[], picks: Readonly<Record<string, unknown>>): string[] {
  return slots.filter((one) => !picks[one.id]).map(slotLabel);
}
