/**
 * The numbers from the game a message's text can quote ({twilight}, {strength:aragorn}, {hand:shadow}), as the Step
 * tab's "insert a number" menu offers them, and how one goes into the text at the caret. Pure.
 */
import { AuthoringTemplates, Draft } from './authoring-state';

/** What the server's Templates.names() said when the menu was made: for a state that does not say (an older server). */
export const STOCK_TEMPLATES: AuthoringTemplates = {
  totals: ['twilight', 'burdens', 'threats', 'site', 'turn', 'hand', 'selfStrength', 'oppStrength'],
  perCard: ['strength', 'vitality', 'resistance', 'wounds', 'tokens', 'cost', 'playCost', 'title'],
  perSeat: ['hand'],
};

/** The seats a per-seat number can be for. */
export const TEMPLATE_SEATS = ['freeps', 'shadow', 'human', 'opponent'] as const;

/** What each quotes, for the menu's titles. */
const SAYS: Readonly<Record<string, string>> = {
  twilight: 'The twilight pool',
  burdens: 'Burdens on the Ring-bearer',
  threats: 'Threats',
  site: 'The site number the fellowship is at',
  turn: 'The turn number',
  hand: 'Cards in hand (the player’s; or a seat’s)',
  selfStrength: 'The player’s side’s strength in the skirmish',
  oppStrength: 'The other side’s strength in the skirmish',
  strength: 'A card’s strength now',
  vitality: 'A card’s vitality now',
  resistance: 'A character’s resistance now',
  wounds: 'Wounds on a card',
  tokens: 'Culture tokens of its own culture on a card',
  cost: 'A card’s twilight cost (no roaming penalty)',
  playCost: 'What playing a card now takes from the pool (roaming included)',
  title: 'A card’s name',
};

/** One entry of the menu: what it is called, what it asks for before it can be put in, and what it quotes. */
export interface TemplateChoice {
  name: string;
  /** none: put in as it is; card: the card is asked for; seat: the seat is. */
  needs: 'none' | 'card' | 'seat';
  title: string;
}

/** The menu: the totals, then the ones for a card, then the ones for a seat (a name for both is offered twice). */
export function templateChoices(templates: AuthoringTemplates | null | undefined): TemplateChoice[] {
  const all = templates ?? STOCK_TEMPLATES;
  const title = (name: string, needs: TemplateChoice['needs']) =>
    `${SAYS[name] ?? name}${needs === 'card' ? ': choose the card' : needs === 'seat' ? ': choose the seat' : ''}`;
  return [
    ...all.totals.map((name) => ({ name, needs: 'none' as const, title: title(name, 'none') })),
    ...all.perCard.map((name) => ({ name, needs: 'card' as const, title: title(name, 'card') })),
    ...all.perSeat.map((name) => ({ name, needs: 'seat' as const, title: title(name, 'seat') })),
  ];
}

/** The template as the text writes it: `{twilight}`, `{strength:aragorn}`. */
export function templateText(name: string, of?: string | null): string {
  return of ? `{${name}:${of}}` : `{${name}}`;
}

/** The scenario's cards a per-card number can be about: the Free Peoples seat's, then the Shadow seat's. */
export function templateCards(draft: Draft | null | undefined): string[] {
  const cards = draft?.cards ?? {};
  return [...Object.keys(cards.freeps ?? {}), ...Object.keys(cards.shadow ?? {})];
}

/** Text with a piece put in at the caret (in place of what is selected), and where the caret goes: after it. */
export function insertAt(text: string, start: number, end: number, piece: string): { text: string; caret: number } {
  const from = Math.max(0, Math.min(start, text.length));
  const to = Math.max(from, Math.min(end, text.length));
  return { text: text.slice(0, from) + piece + text.slice(to), caret: from + piece.length };
}
