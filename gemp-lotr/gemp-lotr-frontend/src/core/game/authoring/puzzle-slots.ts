/**
 * The Board tab's model of a puzzle's slots (ep-notes.md P2): each slot as the file has it (`slots`, id to the slot
 * or to its zone alone) beside what the state says of it (its bound, the card the game in hand has in it), and the
 * whole `slots` part a change sends (`top: {slots}`; null takes it out). A solution fills every slot, so adding or
 * taking out a slot sends the solutions too, with a pick for the new slot or without the one taken out. Renaming a
 * slot is renaming a card (`alias`). Pure.
 */
import { Draft, DraftSeat, DraftSlot, DraftSolution } from './authoring-state';
import { ALIAS_RULE } from './board-queue';
import { PuzzleSlot } from './puzzle-state';

/** A slot's row: its id, the slot as the file has it (every part spelt out), and what the state says of it. */
export interface SlotRow {
  id: string;
  slot: DraftSlot;
  info: PuzzleSlot | null;
}

/** A slot as the file has it, spelt out (`hand1: hand` is `{zone: hand}`). */
export function slotOf(value: DraftSlot | string | undefined): DraftSlot {
  if (value === undefined) return { zone: 'hand' };
  return typeof value === 'string' ? { zone: value } : value;
}

/** The draft's slots, in the file's order, each with the state's word on it. */
export function slotRows(draft: Draft, info: readonly PuzzleSlot[]): SlotRow[] {
  return Object.entries(draft.slots ?? {}).map(([id, value]) => ({
    id,
    slot: slotOf(value),
    info: info.find((one) => one.id === id) ?? null,
  }));
}

/** Where a slot can be, as its select says it (the state's slotZones, each as itself if not here). */
export const SLOT_ZONE_LABELS: Readonly<Record<string, string>> = {
  characters: 'characters',
  support: 'support area',
  minions: 'minions',
  hand: 'hand',
  deck: 'draw deck',
  discard: 'discard pile',
  deadPile: 'dead pile',
  attached: 'attached to…',
  stacked: 'stacked on…',
  site: 'a site',
};

/** Whether a zone puts the card on another card (`on` is said). */
export const onACard = (zone: string): boolean => zone === 'attached' || zone === 'stacked';

/**
 * A slot as the file writes it: only what is not as it would be if left out, in the writer's order (ScenarioWriter
 * .slot); a slot that says only where is that zone alone. `on` only on a card, `position` only in a deck; the seat
 * only when it is not the player's (`human`).
 */
export function slotWritten(slot: DraftSlot, human: DraftSeat): DraftSlot | string {
  const out: Record<string, unknown> = { zone: slot.zone };
  if (slot.seat !== undefined && slot.seat !== human && slot.zone !== 'site') out['seat'] = slot.seat;
  if (onACard(slot.zone) && slot.on) out['on'] = slot.on;
  if (slot.zone === 'deck' && slot.position !== undefined && slot.position !== 1) out['position'] = slot.position;
  if (slot.filter?.trim()) out['filter'] = slot.filter.trim();
  if (slot.tokens) out['tokens'] = slot.tokens;
  if (slot.wounds) out['wounds'] = slot.wounds;
  if (slot.label?.trim()) out['label'] = slot.label.trim();
  return Object.keys(out).length === 1 ? slot.zone : (out as unknown as DraftSlot);
}

/** The slots part with one slot as `slot` (null: taken out); a new one goes last. Null when none is left. */
export function slotsWith(
  draft: Draft,
  id: string,
  slot: DraftSlot | null,
): Readonly<Record<string, DraftSlot | string>> | null {
  const human = draft.human ?? 'freeps';
  const slots = new Map(Object.entries(draft.slots ?? {}));
  if (slot === null) slots.delete(id);
  else slots.set(id, slotWritten(slot, human));
  return slots.size === 0 ? null : Object.fromEntries(slots);
}

/** A change to a slot's parts: an undefined part is taken out. */
export type SlotChange = { [K in keyof DraftSlot]?: DraftSlot[K] | undefined };

/** One slot's part changed (an undefined part: taken out; a change of zone drops what the new zone has no use for). */
export function slotChanged(slot: DraftSlot, change: SlotChange): DraftSlot {
  const next: Record<string, unknown> = { ...slot, ...change };
  for (const [name, value] of Object.entries(change)) if (value === undefined) Reflect.deleteProperty(next, name);
  const zone = String(next['zone']);
  if (!onACard(zone)) Reflect.deleteProperty(next, 'on');
  if (zone !== 'deck') Reflect.deleteProperty(next, 'position');
  if (zone === 'site') Reflect.deleteProperty(next, 'seat');
  return next as unknown as DraftSlot;
}

/** A number typed for a slot (tokens, wounds, a position): what is not a number of at least `least` is none. */
export function slotNumber(typed: string, least: number): number | undefined {
  const read = Number.parseInt(typed, 10);
  return Number.isNaN(read) || read < least ? undefined : read;
}

/** The patch that changes one slot (`top: {slots}`). */
export function slotPatch(draft: Draft, id: string, change: SlotChange) {
  return { top: { slots: slotsWith(draft, id, slotChanged(slotOf(draft.slots?.[id]), change)) } };
}

/** The patch that sets a slot's bound to a filter (blank: anything the pool allows). */
export function slotBoundPatch(draft: Draft, id: string, filter: string) {
  const bound = filter.trim();
  return slotPatch(draft, id, { filter: bound === '' ? undefined : bound });
}

/** The solutions with every one's picks changed by `pick` (picks left with none are taken out of it). */
function solutionsWithPicks(
  solutions: readonly DraftSolution[],
  pick: (picks: Record<string, string>) => void,
): DraftSolution[] {
  return solutions.map((solution) => {
    const picks = { ...solution.picks };
    pick(picks);
    const next: Record<string, unknown> = { ...solution, picks };
    if (Object.keys(picks).length === 0) Reflect.deleteProperty(next, 'picks');
    return next as unknown as DraftSolution;
  });
}

/**
 * The patch that adds a slot: the slot, and (a solution fills every slot) each solution with `pick` for it, when the
 * draft has solutions. Without a pick the server refuses it, saying so.
 */
export function addSlotPatch(draft: Draft, id: string, slot: DraftSlot, pick: string | null) {
  const slots = slotsWith(draft, id, slot);
  const solutions = draft.solutions ?? [];
  const card = pick?.trim() ?? '';
  if (solutions.length === 0 || card === '') return { top: { slots } };
  return {
    top: {
      slots,
      solutions: solutionsWithPicks(solutions, (picks) => {
        picks[id] = card;
      }),
    },
  };
}

/** The patch that takes a slot out: the slot, and its pick from every solution. */
export function removeSlotPatch(draft: Draft, id: string) {
  const slots = slotsWith(draft, id, null);
  const solutions = draft.solutions ?? [];
  if (solutions.length === 0) return { top: { slots } };
  return {
    top: {
      slots,
      solutions: solutionsWithPicks(solutions, (picks) => {
        Reflect.deleteProperty(picks, id);
      }),
    },
  };
}

/**
 * What is wrong with an id for a new slot (null: nothing): a card's name, not taken by a card or another slot; a site
 * slot's is one of the scenario's sites.
 */
export function slotIdProblem(
  id: string,
  zone: string,
  taken: readonly string[],
  sites: readonly string[],
): string | null {
  if (zone === 'site') return sites.includes(id) ? null : 'A site slot is one of the scenario’s sites, by its name.';
  if (!ALIAS_RULE.test(id)) return 'Letters, digits, dashes and underscores, starting with a letter';
  if (taken.includes(id)) return `${id} is taken: a slot's id is the name its card goes by.`;
  return null;
}

/** The seats a slot can be the card of: the player's (left out), or the opponent's. */
export function slotSeatOptions(human: DraftSeat): (readonly [string, string])[] {
  const other: DraftSeat = human === 'freeps' ? 'shadow' : 'freeps';
  return [
    ['', "player's"],
    [other, "opponent's"],
  ];
}

/**
 * What to count beside a slot's bound: the pool and the filter as one filter text, which finds exactly the cards that
 * fit unless they say the same key (`overlap`: one text would then find either), when the filter's own count is
 * shown instead (`exact` false).
 */
export function boundQuery(
  slot: DraftSlot,
  info: PuzzleSlot | null,
  pool: string | null,
): { filter: string; exact: boolean } {
  const filter = slot.filter?.trim() ?? '';
  if (info !== null && info.overlap.length > 0) return { filter, exact: false };
  return { filter: [pool ?? '', filter].filter((part) => part.trim() !== '').join(' '), exact: true };
}
