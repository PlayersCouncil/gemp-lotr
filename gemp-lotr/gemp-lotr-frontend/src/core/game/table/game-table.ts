/**
 * The game's cards as the table shows them: which box each card lies in, with what is attached to it and stacked on
 * it (the legacy page's card groups and their `cardBelongs` rules, without the drawing). Pure: computed from the
 * model, so every arrangement can be checked without a page.
 */
import { GameCard, GameModel } from '../state/game-model';
import { assignmentOf } from '../state/local-choices';

/** A card under another one: how it got there decides how much of it shows. */
export interface TableChild {
  card: GameCard;
  kind: 'attached' | 'stacked';
}

/** A card in a box, with the cards under it in the order they are drawn (each over the one before, the card last). */
export interface TableStack {
  card: GameCard;
  under: readonly TableChild[];
}

/** A site on the adventure path, with the players standing on it (their number, and whether it is their turn). */
export interface TableSite extends TableStack {
  players: readonly { number: number; current: boolean }[];
}

/** Minions waiting to fight a character: both sides of one assignment. */
export interface TableAssignment {
  /** The character's id; negative for a skirmish split into several waiting ones. */
  characterId: string;
  minions: readonly TableStack[];
  characters: readonly TableStack[];
}

export interface TableSkirmish {
  minions: readonly TableStack[];
  characters: readonly TableStack[];
}

export interface GameTable {
  /** The participant at the bottom of the table. */
  bottom: string | null;
  spectator: boolean;
  /** Whether it is the bottom player's turn: the assignments then lie between the minions and their characters. */
  bottomTurn: boolean;
  sites: readonly TableSite[];
  supportOpponent: readonly TableStack[];
  charactersOpponent: readonly TableStack[];
  shadow: readonly TableStack[];
  charactersPlayer: readonly TableStack[];
  supportPlayer: readonly TableStack[];
  /** Empty for a spectator, who has no hand row. */
  hand: readonly TableStack[];
  skirmish: TableSkirmish | null;
  assignments: readonly TableAssignment[];
}

/** The card ids in the order the page keeps them: the server's, unless the player moved cards (`order`). */
function ordered(model: GameModel, order: readonly string[]): GameCard[] {
  const known = order.filter((id) => model.cards[id]);
  const rest = model.cardOrder.filter((id) => !known.includes(id));
  // A moved card keeps its place among the ones that were there; new cards come after, as the server sent them
  return [...known, ...rest].flatMap((id) => model.cards[id] ?? []);
}

/** The table for a model. `order`: card ids in the order the player arranged them (the others follow). */
export function gameTable(model: GameModel, order: readonly string[] = []): GameTable {
  const cards = ordered(model, order);
  const bottom = model.participantId;

  const children = new Map<string, GameCard[]>();
  for (const card of cards) {
    const parent = card.attachedTo ?? card.stackedOn;
    if (parent !== null) children.set(parent, [...(children.get(parent) ?? []), card]);
  }
  // The cards under a card, deepest first: stacked ones, then attached ones, each after what lies under itself
  const under = (card: GameCard): TableChild[] => {
    const own = (children.get(card.id) ?? []).slice().sort((a, b) => a.attachedAt - b.attachedAt);
    const of = (kind: 'attached' | 'stacked') =>
      own
        .filter((child) => (kind === 'stacked' ? child.stackedOn !== null : child.stackedOn === null))
        .flatMap((child) => [...under(child), { card: child, kind }]);
    return [...of('stacked'), ...of('attached')];
  };
  const stacks = (belongs: (card: GameCard) => boolean): TableStack[] =>
    cards.filter(belongs).map((card) => ({ card, under: under(card) }));

  const assigned = new Map<string, string>();
  for (const card of cards) {
    const to = assignmentOf(model, card.id);
    if (to !== null) assigned.set(card.id, to);
  }
  // In the order the legacy page went through them: its groups were keyed by character id in an object, which lists
  // whole numbers first, ascending, then the others (the split skirmishes' negative ids) as they were added
  const characterIds = [...new Set(assigned.values())].sort((a, b) => {
    const [na, nb] = [Number(a), Number(b)];
    if (na >= 0 && nb >= 0) return na - nb;
    return na >= 0 ? -1 : nb >= 0 ? 1 : 0;
  });
  const fights = (card: GameCard) => characterIds.includes(card.id);
  const free = (card: GameCard) => !fights(card) && !card.inSkirmish;

  const playerNumbers = (site: GameCard) =>
    model.players.flatMap((player, i) =>
      site.index !== null && model.positions[player] === site.index
        ? [{ number: i + 1, current: player === model.currentPlayer }]
        : [],
    );

  return {
    bottom,
    spectator: model.spectator,
    bottomTurn: model.currentPlayer === bottom,
    sites: stacks((card) => card.zone === 'ADVENTURE_PATH')
      .sort((a, b) => (a.card.index ?? 0) - (b.card.index ?? 0))
      .map((stack) => ({ ...stack, players: playerNumbers(stack.card) })),
    supportOpponent: stacks((card) => card.zone === 'SUPPORT' && card.owner !== bottom && free(card)),
    charactersOpponent: stacks((card) => card.zone === 'FREE_CHARACTERS' && card.owner !== bottom && free(card)),
    shadow: stacks((card) => card.zone === 'SHADOW_CHARACTERS' && !assigned.has(card.id) && !card.inSkirmish),
    charactersPlayer: stacks((card) => card.zone === 'FREE_CHARACTERS' && card.owner === bottom && free(card)),
    supportPlayer: stacks((card) => card.zone === 'SUPPORT' && card.owner === bottom && free(card)),
    hand: model.spectator
      ? []
      : stacks(
          (card) =>
            (card.zone === 'HAND' && card.owner === bottom) || (card.zone === 'EXTRA' && !card.id.includes('-')),
        ),
    skirmish: model.skirmish
      ? {
          minions: stacks((card) => card.zone === 'SHADOW_CHARACTERS' && card.inSkirmish && !assigned.has(card.id)),
          characters: stacks(
            (card) => (card.zone === 'SUPPORT' || card.zone === 'FREE_CHARACTERS') && card.inSkirmish && !fights(card),
          ),
        }
      : null,
    assignments: characterIds.map((characterId) => ({
      characterId,
      minions: stacks((card) => card.zone === 'SHADOW_CHARACTERS' && assigned.get(card.id) === characterId),
      characters: stacks(
        (card) => card.id === characterId || (characterId.includes('-') && card.id.includes(characterId)),
      ),
    })),
  };
}
