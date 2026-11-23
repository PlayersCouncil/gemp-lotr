import { storedList } from '../../platform/session-store';
import { PileName } from '../table/game-table';

/** What a sheet of the phone layout shows. */
export type Sheet =
  | { kind: 'menu' }
  /** A player's piles, scrolled to one of them (`focus`) */
  | { kind: 'piles'; player: string; focus: PileName | null }
  /** A card's details: a card of the game by its id, or a card shown only for a decision or named in a text */
  | { kind: 'card'; cardId: string | null; blueprintId: string }
  | { kind: 'chat' }
  | { kind: 'log' }
  | { kind: 'settings'; tab: 'game' | 'display' }
  | { kind: 'info' }
  | { kind: 'confirm'; what: 'concede' | 'cancel' };

/** Where a sheet was scrolled: its own body, and each row of cards in it, in order. */
export interface SheetScroll {
  top: number;
  rows: readonly number[];
}

export interface SheetEntry {
  sheet: Sheet;
  scroll: SheetScroll;
}

const SHEET_KINDS = ['menu', 'piles', 'card', 'chat', 'log', 'settings', 'info', 'confirm'];
const PILES: readonly string[] = ['discard', 'deadPile', 'adventureDeck', 'removedPile', 'deck', 'revealedHand'];

/** A sheet as kept before a reload, if it is one (anything else is dropped). */
export function readSheet(value: unknown): SheetEntry | null {
  const entry = value as { sheet?: Record<string, unknown>; scroll?: { top?: unknown; rows?: unknown } } | null;
  const sheet = entry?.sheet;
  if (!sheet || typeof sheet['kind'] !== 'string' || !SHEET_KINDS.includes(sheet['kind'])) return null;
  const top = typeof entry.scroll?.top === 'number' ? entry.scroll.top : 0;
  const rows = storedList(entry.scroll?.rows)?.filter((n): n is number => typeof n === 'number') ?? [];
  const scroll = { top, rows };
  switch (sheet['kind']) {
    case 'piles':
      if (typeof sheet['player'] !== 'string') return null;
      return {
        sheet: {
          kind: 'piles',
          player: sheet['player'],
          focus: PILES.includes(sheet['focus'] as string) ? (sheet['focus'] as PileName) : null,
        },
        scroll,
      };
    case 'card':
      if (typeof sheet['blueprintId'] !== 'string') return null;
      return {
        sheet: {
          kind: 'card',
          cardId: typeof sheet['cardId'] === 'string' ? sheet['cardId'] : null,
          blueprintId: sheet['blueprintId'],
        },
        scroll,
      };
    case 'settings':
      return { sheet: { kind: 'settings', tab: sheet['tab'] === 'display' ? 'display' : 'game' }, scroll };
    case 'confirm':
      return { sheet: { kind: 'confirm', what: sheet['what'] === 'cancel' ? 'cancel' : 'concede' }, scroll };
    default:
      return { sheet: { kind: sheet['kind'] } as Sheet, scroll };
  }
}
