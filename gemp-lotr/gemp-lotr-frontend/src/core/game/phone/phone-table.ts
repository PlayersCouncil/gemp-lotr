/**
 * The game's table as the phone layout shows it (G7 of trymarz/gemp-lotr-private#27): the whole adventure path first,
 * then a row per kind of card from the opponent's support area down to the player's, with the assignments and the
 * skirmish as pairs where the minions are. Every card keeps one size; the cards lying under one (attached, stacked)
 * are drawn as small copies under it. Pure: computed from the table the computer layout uses, so both lay out the same
 * cards.
 */
import { GameCard, GameModel } from '../state/game-model';
import { GameTable, TableStack } from '../table/game-table';
import { isShadowCard } from './card-sides';

/** A card of a row, with the cards lying under it (attached and stacked), in the order they came. */
export interface PhoneCard {
  card: GameCard;
  under: readonly GameCard[];
}

/** A place on the adventure path: its site card, or none yet (an empty number), and who stands there. */
export interface PhoneSite {
  number: number;
  card: GameCard | null;
  players: readonly { number: number; name: string; current: boolean }[];
  /** Where the fellowship of the player whose turn it is stands. */
  here: boolean;
  /** Not reached yet by the fellowship whose turn it is. */
  ahead: boolean;
}

export type PhoneRowKey =
  'support-opponent' | 'characters-opponent' | 'shadow' | 'characters-player' | 'support-player';

export interface PhoneRow {
  key: PhoneRowKey;
  /** Whose cards: the opponent's (them), the player's (mine), or the minions. */
  side: 'them' | 'mine' | 'minions';
  /** The row's title, with the player's name. */
  label: string;
  cards: readonly PhoneCard[];
  /** In a support row, the first Shadow card's index (a line marks where they start), or null. */
  shadowFrom: number | null;
  /**
   * How its cards not active this turn fold away (the owner, 2026-11-17): the player's shrink (yours in the other side's
   * turn), the opponent's hide behind a button; the minions never (they are always the Shadow player's in play).
   */
  fold: PhoneFold | null;
}

export type PhoneFold = 'shrink' | 'hide';

/** A row's cards not active this turn as they are shown: folded away, or shown because the player opened them. */
export interface PhoneFolding {
  how: PhoneFold;
  /** The cards that fold, by id. */
  ids: ReadonlySet<string>;
  /** Folded now: not opened by the player, and none of them offered by the waiting decision (those always show). */
  folded: boolean;
  /** One of them is offered: they show and can't be folded until the decision is answered. */
  offered: boolean;
}

/** The row's folding, or null when it has no card to fold. `marks` are the waiting decision's (GameDecisions). */
export function phoneFolding(
  cards: readonly PhoneCard[],
  how: PhoneFold | null,
  opened: boolean,
  marks: Readonly<Record<string, string>>,
): PhoneFolding | null {
  if (how === null) return null;
  const ids = new Set(cards.filter(({ card }) => card.inactive).map(({ card }) => card.id));
  if (ids.size === 0) return null;
  const offered = [...ids].some((id) => marks[id] === 'selectable' || marks[id] === 'actionable');
  return { how, ids, folded: !opened && !offered, offered };
}

/** Both sides of one fight: the skirmish being fought, or an assignment waiting for its skirmish. */
export interface PhonePair {
  kind: 'skirmish' | 'assignment';
  key: string;
  characters: readonly PhoneCard[];
  minions: readonly PhoneCard[];
}

export interface PhoneTable {
  path: readonly PhoneSite[];
  /** The rows above the fights: the opponent's support and characters, the minions not fighting. */
  above: readonly PhoneRow[];
  /** The skirmish first, then the assignments. */
  pairs: readonly PhonePair[];
  /** The player's characters and support. */
  below: readonly PhoneRow[];
  hand: readonly PhoneCard[];
}

/** The fewest places the path shows: the nine sites of a game. */
const PATH_LENGTH = 9;

const phoneCard = (stack: TableStack): PhoneCard => ({ card: stack.card, under: stack.under.map((u) => u.card) });

/** A support row: the Free Peoples cards first, then the Shadow ones, each in their order. */
function supportRow(key: PhoneRowKey, side: 'them' | 'mine', label: string, stacks: readonly TableStack[]): PhoneRow {
  const free = stacks.filter((stack) => !isShadowCard(stack.card.blueprintId));
  const shadow = stacks.filter((stack) => isShadowCard(stack.card.blueprintId));
  return {
    key,
    side,
    label,
    cards: [...free, ...shadow].map(phoneCard),
    shadowFrom: free.length > 0 && shadow.length > 0 ? free.length : null,
    fold: foldOf(side),
  };
}

function row(key: PhoneRowKey, side: PhoneRow['side'], label: string, stacks: readonly TableStack[]): PhoneRow {
  return { key, side, label, cards: stacks.map(phoneCard), shadowFrom: null, fold: foldOf(side) };
}

const foldOf = (side: PhoneRow['side']): PhoneFold | null =>
  side === 'mine' ? 'shrink' : side === 'them' ? 'hide' : null;

export function phoneTable(table: GameTable, model: GameModel): PhoneTable {
  const bottom = table.bottom;
  const opponent = model.players.find((player) => player !== bottom) ?? 'Opponent';
  const own = table.spectator ? (bottom ?? 'Player') : 'Your';
  const theirs = `${opponent}'s`;
  const ownLabel = (what: string) => (table.spectator ? `${own}'s ${what}` : `${own} ${what}`);
  const current = model.currentPlayer;
  const currentAt = current === null ? null : (model.positions[current] ?? null);

  const sites = new Map<number, TableStack>();
  for (const site of table.sites) if (site.card.index !== null) sites.set(site.card.index, site);
  const last = Math.max(PATH_LENGTH, ...sites.keys(), ...Object.values(model.positions));
  const path: PhoneSite[] = [];
  for (let number = 1; number <= last; number++) {
    path.push({
      number,
      card: sites.get(number)?.card ?? null,
      players: model.players.flatMap((name, i) =>
        model.positions[name] === number ? [{ number: i + 1, name, current: name === current }] : [],
      ),
      here: currentAt === number,
      ahead: currentAt !== null && number > currentAt,
    });
  }

  const pairs: PhonePair[] = [];
  if (table.skirmish) {
    pairs.push({
      kind: 'skirmish',
      key: 'skirmish',
      characters: table.skirmish.characters.map(phoneCard),
      minions: table.skirmish.minions.map(phoneCard),
    });
  }
  for (const assignment of table.assignments) {
    pairs.push({
      kind: 'assignment',
      key: assignment.characterId,
      characters: assignment.characters.map(phoneCard),
      minions: assignment.minions.map(phoneCard),
    });
  }

  return {
    path,
    above: [
      supportRow('support-opponent', 'them', `${theirs} support`, table.supportOpponent),
      row('characters-opponent', 'them', `${theirs} characters`, table.charactersOpponent),
      row('shadow', 'minions', 'Minions', table.shadow),
    ],
    pairs,
    below: [
      row('characters-player', 'mine', ownLabel('characters'), table.charactersPlayer),
      supportRow('support-player', 'mine', ownLabel('support'), table.supportPlayer),
    ],
    // The cards the decision shows from elsewhere go to the hand's place on their own (hand-place.ts)
    hand: table.hand.filter((stack) => stack.card.zone === 'HAND').map(phoneCard),
  };
}
