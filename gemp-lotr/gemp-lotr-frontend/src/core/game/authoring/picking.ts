/**
 * Picking something on the table for a message to be about (the legacy scenarioAuthor.js pick, thingAt, partSelect,
 * skirmishSideOf): the anchors a click can give, which thing under the pointer wins, and the card-part and list
 * editing of an anchor field. Pure: what is where on the page is measured by the view (ui/game/authoring/
 * table-picker.service.ts) and handed in.
 */
import { Rect } from '../scenario/bubble-placement';
import { Skirmish } from '../state/game-model';
import { AuthoringBoard } from './authoring-state';
import { splitTerms } from './terms';

/** The printed parts of a card an anchor can be about: `card:frodo:strength`. */
export const PARTS = [
  'strength',
  'vitality',
  'resistance',
  'siteNumber',
  'wounds',
  'cost',
  'title',
  'culture',
  'type',
  'text',
  'art',
  'stats',
  'signet',
  'shadowNumber',
] as const;

/**
 * The places of the table and the pieces of the page a click can pick, besides the cards, the sites on the
 * adventure path (`ui:site:3`) and the assignments (`ui:assignment:frodo`).
 */
export const PLACES = [
  'zone:self.hand',
  'zone:self.characters',
  'zone:self.support',
  'zone:opp.characters',
  'zone:opp.support',
  'zone:minions',
  'pile:self.deck',
  'pile:self.discard',
  'pile:self.deadPile',
  'pile:self.adventureDeck',
  'pile:self.removed',
  'pile:self.hand',
  'pile:opp.deck',
  'pile:opp.discard',
  'pile:opp.deadPile',
  'pile:opp.adventureDeck',
  'pile:opp.removed',
  'pile:opp.hand',
  'ui:twilightPool',
  'ui:phase',
  'ui:initiative',
  'ui:ruleof4',
  'ui:clock',
  'ui:advPath',
  'ui:alertBox',
  'ui:gameLog',
  'ui:skirmish',
  'ui:skirmish:selfStrength',
  'ui:skirmish:selfDamage',
  'ui:skirmish:freeps',
  'ui:skirmish:shadow',
  'ui:skirmish:oppStrength',
  'ui:skirmish:oppDamage',
  'ui:stats',
  'ui:stats:archery',
  'ui:stats:move',
  'ui:chat',
  'ui:settings',
  'ui:dialog',
  'ui:cardInfo',
  'ui:button:pass',
  'ui:button:done',
  'ui:button:reset',
] as const;

/** What the author is told while picking (the panel's notice). */
export const PICK_TOLD = 'Click what the message is about. Esc cancels.';

/** Something a click could pick: its anchor, where it takes clicks (null: not on the screen), and where it shows. */
export interface PickCandidate {
  anchor: string;
  rect: Rect | null;
  /** Where it is drawn round while the pointer is on it (what the anchor points at); `rect` if not given. */
  box?: Rect | null;
}

/** What the pointer is on: its anchor, and the box drawn round it. */
export interface PickHit {
  anchor: string;
  box: Rect;
}

function holds(rect: Rect, x: number, y: number): boolean {
  return x >= rect.left && x <= rect.left + rect.width && y >= rect.top && y <= rect.top + rect.height;
}

/**
 * Of the things under a point, the smallest: a card wins over its row, a row's strength box over the skirmish. Of
 * things the same size, the first given.
 */
export function smallestAt(x: number, y: number, candidates: Iterable<PickCandidate>): PickHit | null {
  let best: PickHit | null = null;
  let least = Infinity;
  for (const candidate of candidates) {
    const rect = candidate.rect;
    if (!rect || !holds(rect, x, y)) continue;
    const area = rect.width * rect.height;
    if (area < least) {
      least = area;
      best = { anchor: candidate.anchor, box: candidate.box ?? rect };
    }
  }
  return best;
}

/** The scenario's names of the cards on the table by their game card id; and the other way about. */
export function aliasesOf(board: AuthoringBoard | null | undefined): Record<string, string> {
  return Object.fromEntries((board?.cards ?? []).map((card) => [String(card.cardId), card.alias]));
}

export function cardIdsOf(board: AuthoringBoard | null | undefined): Record<string, string> {
  return Object.fromEntries((board?.cards ?? []).map((card) => [card.alias, String(card.cardId)]));
}

/**
 * What a card under the pointer is as an anchor: `card:<alias>` for one of the scenario's cards, `ui:site:<n>` for a
 * site on the adventure path; null for anything else. `cardId` as the page has it (a card drawn for its action
 * alone is `extra<id>`).
 */
export function cardAnchor(
  cardId: string,
  aliases: Readonly<Record<string, string>>,
  site: number | null,
): string | null {
  const id = cardId.replace(/^extra/, '');
  const alias = aliases[id];
  if (alias !== undefined) return `card:${alias}`;
  return site !== null ? `ui:site:${site}` : null;
}

/** For `card:<alias>`: the side of the skirmish being fought that card is on, or null if it is not in it. */
export function fightingSide(
  anchor: string,
  cardIds: Readonly<Record<string, string>>,
  skirmish: Skirmish | null,
): 'freeps' | 'shadow' | null {
  const match = /^card:([A-Za-z0-9_-]+)$/.exec(anchor.trim());
  const alias = match?.[1];
  const id = alias === undefined ? undefined : cardIds[alias];
  if (id === undefined || !skirmish) return null;
  if (skirmish.characterId === id) return 'freeps';
  return skirmish.minionIds.includes(id) ? 'shadow' : null;
}

/** The anchor that points at whichever card fights on that side of the skirmish being fought. */
export function fightingAnchor(side: 'freeps' | 'shadow'): string {
  return `ui:skirmish:${side}`;
}

/** For a field that holds one card (`card:frodo`, `card:frodo:strength`): the card and the part; null otherwise. */
export function cardPartOf(text: string): { alias: string; part: string } | null {
  const value = text.trim();
  if (value.includes(',')) return null;
  const [kind, alias, part = ''] = value.split(':');
  return kind === 'card' && alias !== undefined && alias !== '' ? { alias, part } : null;
}

/** The field's card with another part of it (`''`: the whole card); null if the field holds no one card. */
export function withPart(text: string, part: string): string | null {
  const card = cardPartOf(text);
  return card ? `card:${card.alias}${part === '' ? '' : `:${part}`}` : null;
}

/** A list of anchors (comma-separated) with one more, if it isn't in it already. */
export function addAnchor(list: string, anchor: string): string {
  const have = splitTerms(list);
  if (!have.includes(anchor)) have.push(anchor);
  return have.join(', ');
}

/** Where the label of what the pointer is on goes: above its box, kept in the window. */
export function labelPlace(box: Rect): { left: number; top: number } {
  return { left: box.left, top: Math.max(0, box.top - 22) };
}
