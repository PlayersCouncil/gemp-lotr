/**
 * What the on-table edit controls offer, worked out from the table as the level knows it (EditBoard) and the tools it
 * allows: a card's menu (Send to, Attach, Stack, Add, Modify, Copy, Delete), the edits each entry makes, and which
 * cards fit a place. Nothing here is a hard-coded list of cultures or modifiers: those come from the board. Pure.
 */
import { EditBoard, EditCard, EditModifier, ModifyWord, onTable } from './edit-board';
import {
  byId,
  CardName,
  EDIT_ZONES,
  EditOp,
  EditPlace,
  EditTool,
  EditZone,
  ModifierUntil,
  placeTarget,
} from './edit-ops';

/** What each place is called in a menu: a word or two. */
export const ZONE_LABELS: Readonly<Record<EditZone, string>> = {
  characters: 'Characters',
  support: 'Support',
  minions: 'Minions',
  hand: 'Hand',
  'deck.top': 'Deck top',
  'deck.bottom': 'Deck bottom',
  discard: 'Discard',
  deadPile: 'Dead pile',
  removed: 'Removed',
};

/** What each way a modifier lasts is called. */
export const UNTIL_LABELS: Readonly<Record<ModifierUntil, string>> = {
  phase: 'This phase',
  turn: 'This turn',
  removed: 'Until removed',
};

const CHARACTERS = ['COMPANION', 'ALLY', 'MINION'];
/** Cards that are never moved, copied or deleted by hand (a site goes by its own edit; the Ring stays with its bearer). */
const FIXED = ['SITE', 'THE_ONE_RING'];

/** Whether a card of the type can go to the zone (the server's rules for `add` and `move`; it says why it refuses). */
export function fits(type: string, zone: EditZone): boolean {
  const upper = type.toUpperCase();
  if (FIXED.includes(upper)) return false;
  switch (zone) {
    case 'characters':
      return upper === 'COMPANION';
    case 'minions':
      return upper === 'MINION';
    case 'support':
      return !['COMPANION', 'MINION', 'EVENT'].includes(upper);
    default:
      return true;
  }
}

/**
 * The bound the search for a place starts with (the deck builder's filter language, `cardType:`), and its label; null
 * where any card goes.
 */
export function placeBound(zone: EditZone): { filter: string; label: string } {
  switch (zone) {
    case 'characters':
      return { filter: 'cardType:COMPANION', label: 'Companions' };
    case 'minions':
      return { filter: 'cardType:MINION', label: 'Minions' };
    case 'support':
      return { filter: 'cardType:-COMPANION,-MINION,-EVENT,-SITE,-THE_ONE_RING', label: 'Support cards' };
    default:
      return { filter: 'cardType:-SITE,-THE_ONE_RING', label: 'No sites' };
  }
}

/** A culture's name as a menu shows it: `uruk_hai` is "Uruk-hai". */
export function cultureLabel(culture: string): string {
  const words = culture.split('_');
  return words.map((word, at) => (at === 0 ? word.charAt(0).toUpperCase() + word.slice(1) : word)).join('-');
}

/** A card of the game as an edit names it: by its card id, else by its name in the scenario. */
export function nameOf(card: EditCard): CardName {
  return (card.cardId !== null ? byId(card.cardId) : null) ?? { card: card.alias };
}

export interface MenuToken {
  culture: string;
  label: string;
  count: number;
  /** The card's own culture (it comes first). */
  own: boolean;
}

/** A card's edit menu: each part null (or empty) where it is not offered. */
export interface CardMenu {
  card: EditCard;
  /** Where Send to can put it: its owner's places but the one it is in. */
  sendTo: readonly { zone: EditZone; label: string; to: string }[];
  attach: boolean;
  stack: boolean;
  /** Add: wounds and heals (a character in play), and tokens (a card in play), own culture first. */
  add: { wounds: boolean; tokens: readonly MenuToken[] } | null;
  /** Modify: what suits the card's type, how long, and what is on it now. */
  modify: { words: readonly ModifyWord[]; until: readonly ModifierUntil[]; present: readonly EditModifier[] } | null;
  copy: boolean;
  delete: boolean;
}

/** Where a card in a zone now is, as a place (the deck counts as its top). */
function zoneOf(card: EditCard): EditZone | null {
  if (card.zone === 'deck') return null;
  return EDIT_ZONES.find((zone) => zone === card.zone) ?? null;
}

/** The menu of a card, from what the level allows (`may`). */
export function cardMenu(board: EditBoard, card: EditCard, may: (tool: EditTool) => boolean): CardMenu {
  const type = card.type.toUpperCase();
  // (a site goes by its own edits; the Ring-bearer and the Ring are offered, and the server says why not: its words
  // say what to do instead)
  const fixed = type === 'SITE';
  const inPlay = onTable(card);
  const here = zoneOf(card);
  const sendTo =
    may('move') && !fixed
      ? EDIT_ZONES.filter((zone) => zone !== here && fits(type, zone)).map((zone) => ({
          zone,
          label: ZONE_LABELS[zone],
          to: placeTarget({ seat: card.owner, zone }),
        }))
      : [];
  const wounds = may('wounds') && inPlay && CHARACTERS.includes(type);
  const tokens =
    may('tokens') && inPlay
      ? [
          ...(card.culture !== null && board.cultures.includes(card.culture) ? [card.culture] : []),
          ...board.cultures.filter((culture) => culture !== card.culture),
        ].map((culture) => ({
          culture,
          label: cultureLabel(culture),
          count: card.cultureTokens[culture] ?? 0,
          own: culture === card.culture,
        }))
      : [];
  const words = board.modify.words.filter((word) => word.types.includes(type));
  return {
    card,
    sendTo,
    attach: may('attach') && !fixed && !CHARACTERS.includes(type),
    stack: may('stack') && !fixed,
    add: wounds || tokens.length > 0 ? { wounds, tokens } : null,
    modify:
      may('modify') && inPlay && (words.length > 0 || card.modifiers.length > 0)
        ? { words, until: board.modify.until, present: card.modifiers }
        : null,
    copy: may('copy') && !fixed,
    delete: may('delete') && !fixed,
  };
}

/** Whether a card's menu has anything to edit (else it is only Info). */
export function menuHasEdits(menu: CardMenu): boolean {
  return (
    menu.sendTo.length > 0 ||
    menu.attach ||
    menu.stack ||
    menu.add !== null ||
    menu.modify !== null ||
    menu.copy ||
    menu.delete
  );
}

/** The number a modifier is put on with, for its word: a stat's change (never 0), a keyword's count, or none. */
export function modifyAmount(word: ModifyWord, amount: number): number | null {
  if (word.number === 'none') return null;
  const whole = Math.trunc(amount);
  if (word.number === 'positive') return Math.max(1, whole);
  return whole === 0 ? null : whole;
}

/** Puts a modifier on the card; null when the amount means nothing (a stat changed by 0). */
export function modifyOp(card: EditCard, word: ModifyWord, amount: number, until: ModifierUntil): EditOp | null {
  const number = modifyAmount(word, amount);
  if (word.number === 'signed' && number === null) return null;
  return {
    op: 'modify',
    ...nameOf(card),
    what: word.what,
    ...(number !== null ? { amount: number } : {}),
    ...(until !== 'removed' ? { until } : {}),
  };
}

/** One card of each kind in the game (not a site or a Ring, which are not added by hand), by name. */
export function cardsInGame(board: EditBoard | null): { blueprintId: string; name: string; type: string }[] {
  const seen = new Map<string, { blueprintId: string; name: string; type: string }>();
  for (const card of board?.cards ?? []) {
    if (FIXED.includes(card.type.toUpperCase()) || seen.has(card.blueprintId)) continue;
    seen.set(card.blueprintId, { blueprintId: card.blueprintId, name: card.name, type: card.type });
  }
  return [...seen.values()].sort((a, b) => a.name.localeCompare(b.name));
}

/** The places an open spot of the table stands for, labelled. */
export function placeLabel(place: EditPlace): string {
  return ZONE_LABELS[place.zone];
}
