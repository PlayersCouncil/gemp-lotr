/**
 * The table as the author's level knows it (AuthorBackend): the EditBoard the on-table controls work from, made out of
 * the authoring state (the scenario's cards by their names, where each is, its wounds, tokens and hand-put modifiers;
 * the path, the markers, the Modify… vocabulary) and the page's own game view (the card ids of the sites laid and of
 * a slot's card, the culture tokens a card has, how many cards each pile holds). At the board the scenario starts
 * with, a card's wounds, tokens and modifiers are the setup's: what the controls change there. Pure.
 */
import { AuthoringBoard, BoardCard, DraftState } from '../authoring/authoring-state';
import { modifierText } from '../authoring/board-modifiers';
import { PlayerZones } from '../protocol/game-events';
import { GameCard } from '../state/game-model';
import { EditBoard, EditCard, EditMode, EditModifier, EditPathSite, ModifyWord } from './edit-board';
import { EditSeat, EditTool, ModifierUntil } from './edit-ops';
import { EditSeats } from './edit-seats';

/** What the author may do on the table (every tool but the sandbox's hidden piles: the pile windows are a sandbox's). */
export const AUTHOR_TOOLS: readonly EditTool[] = [
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
];

/**
 * The one kind of token the file has (`tokens(card, n)`, setup.tokens): culture tokens of the card's own culture. The
 * card menu shows it as one row, "Tokens".
 */
export const OWN_TOKENS = 'tokens';

/** What the author's board is made of. */
export interface AuthorTable {
  state: DraftState;
  /** Changes go to the board the scenario starts with (the Board tab's "where"). */
  inSetup: boolean;
  mode: EditMode;
  tools: readonly EditTool[];
  /** The page's game view: its cards by id, and each player's piles as the game counts them. */
  cards: Readonly<Record<string, GameCard>>;
  zones: Readonly<Record<string, PlayerZones | undefined>>;
  seats: EditSeats;
}

const UNTIL: readonly ModifierUntil[] = ['phase', 'turn', 'removed'];
const until = (value: string): ModifierUntil => UNTIL.find((one) => one === value) ?? 'removed';
const SEATS: readonly EditSeat[] = ['freeps', 'shadow'];

/** A whole number of a setup part that maps a card's name to one (setup.wounds, setup.tokens), or 0. */
export function setupCount(state: DraftState, part: 'wounds' | 'tokens', alias: string): number {
  const value = state.draft.setup?.[part]?.[alias] ?? 0;
  return Number.isFinite(value) ? value : 0;
}

/** The modifiers the controls show on a card: those in force; at the start, the setup's (numbered from 1). */
function modifiersOf(table: AuthorTable, card: BoardCard): EditModifier[] {
  if (!table.inSetup) {
    return (card.modifiers ?? []).map((one) => ({
      id: one.id,
      what: one.what,
      amount: one.amount,
      until: until(one.until),
      text: one.text,
    }));
  }
  return (table.state.start?.modifiers?.[card.alias] ?? []).map((one, at) => ({
    id: at + 1,
    what: one.what,
    amount: one.amount,
    until: until(one.until),
    text: modifierText(table.state.modify, one.what, one.amount),
  }));
}

/** A card of the scenario as the controls know it. */
function scenarioCard(table: AuthorTable, card: BoardCard): EditCard {
  const tokens = table.inSetup ? setupCount(table.state, 'tokens', card.alias) : card.tokens;
  return {
    alias: card.alias,
    blueprintId: card.blueprintId,
    name: card.name,
    type: card.type,
    owner: card.owner,
    cardId: String(card.cardId),
    zone: card.zone,
    wounds: table.inSetup ? setupCount(table.state, 'wounds', card.alias) : card.wounds,
    cultureTokens: tokens > 0 ? { [OWN_TOKENS]: tokens } : {},
    culture: OWN_TOKENS,
    on: card.on,
    modifiers: modifiersOf(table, card),
    added: false,
    at: card.at,
    site: null,
    printed: null,
    ringBearer: card.ringBearer === true,
    ring: card.ring === true,
  };
}

/** The seat whose player the page shows a card under (null: neither). */
function seatOfOwner(seats: EditSeats, owner: string | null): EditSeat | null {
  return SEATS.find((seat) => seats.players[seat] !== '' && seats.players[seat] === owner) ?? null;
}

/** The sites laid on the path, as cards the controls know (their ids the page's): a site's menu works on them. */
function pathCards(table: AuthorTable, board: AuthoringBoard): EditCard[] {
  const laid = Object.values(table.cards).filter((card) => card.zone === 'ADVENTURE_PATH' && card.index !== null);
  return (board.path ?? []).flatMap((site) => {
    const card = laid.find((one) => one.index === site.number);
    if (card === undefined) return [];
    return [
      {
        alias: site.alias ?? `site${site.number}`,
        blueprintId: site.blueprintId,
        name: site.name ?? site.blueprintId,
        type: 'SITE',
        owner: site.owner,
        cardId: card.id,
        zone: 'path',
        wounds: 0,
        cultureTokens: {},
        culture: null,
        on: null,
        modifiers: [],
        added: false,
        at: null,
        site: site.number,
        printed: site.printed ?? null,
      },
    ];
  });
}

/**
 * Each seat's sites not on the path, as the cards of its adventure deck (no card id: the page does not show them):
 * the path's chooser offers them by their names.
 */
function deckCards(board: AuthoringBoard): EditCard[] {
  const laid = new Set((board.path ?? []).map((site) => site.alias));
  return SEATS.flatMap((seat) =>
    board.sites[seat].flatMap((site) =>
      site.alias === null || laid.has(site.alias)
        ? []
        : [
            {
              alias: site.alias,
              blueprintId: site.id,
              name: site.name ?? site.id,
              type: 'SITE',
              owner: seat,
              cardId: null,
              zone: 'adventureDeck',
              wounds: 0,
              cultureTokens: {},
              culture: null,
              on: null,
              modifiers: [],
              added: false,
              at: null,
              site: null,
              printed: site.printed ?? site.number,
            },
          ],
    ),
  );
}

/** The page's zone a slot's card is in (the game's names). */
const SLOT_ZONES: Readonly<Record<string, readonly string[]>> = {
  characters: ['FREE_CHARACTERS'],
  support: ['SUPPORT'],
  minions: ['SHADOW_CHARACTERS'],
  hand: ['HAND'],
  deck: ['DECK'],
  discard: ['DISCARD'],
  deadPile: ['DEAD'],
  attached: ['ATTACHED'],
  stacked: ['STACKED'],
};

/**
 * The cards in a puzzle's slots (the game in hand's picks), each found on the page by its card and where its slot
 * puts it (the state names a slot's card by its slot, not by a card id): marked with its slot, so that its menu
 * offers the slot's form and no edit.
 */
function slotCards(table: AuthorTable, known: ReadonlySet<string>): EditCard[] {
  const human = table.seats.human;
  const taken = new Set(known);
  const found: EditCard[] = [];
  for (const slot of table.state.puzzle?.slots ?? []) {
    if (slot.pick === null) continue;
    const seat: EditSeat = slot.seat === 'human' ? human : human === 'freeps' ? 'shadow' : 'freeps';
    const zones = SLOT_ZONES[slot.zone] ?? [];
    const card = Object.values(table.cards).find(
      (one) =>
        !taken.has(one.id) &&
        one.blueprintId === slot.pick &&
        zones.includes(one.zone) &&
        seatOfOwner(table.seats, one.owner) === seat,
    );
    if (card === undefined) continue;
    taken.add(card.id);
    found.push({
      alias: slot.id,
      blueprintId: card.blueprintId,
      name: slot.pickName ?? card.blueprintId,
      type: '',
      owner: seat,
      cardId: card.id,
      zone: slot.zone,
      wounds: 0,
      cultureTokens: {},
      culture: null,
      on: slot.on,
      modifiers: [],
      added: false,
      slot: slot.id,
    });
  }
  return found;
}

/** How many cards each pile of a seat holds: as the game counts them, else the scenario's cards there. */
function pilesOf(table: AuthorTable, board: AuthoringBoard): EditBoard['piles'] {
  const counted = (seat: EditSeat): Record<string, number> => {
    const zones = table.zones[table.seats.players[seat]];
    if (zones !== undefined) {
      const named: [string, number | null][] = [
        ['hand', zones.hand],
        ['deck', zones.deck],
        ['discard', zones.discard],
        ['deadPile', zones.dead],
        ['removed', zones.removed],
        ['adventureDeck', zones.adventureDeck],
      ];
      return Object.fromEntries(named.filter((entry): entry is [string, number] => entry[1] !== null));
    }
    const piles: Record<string, number> = {};
    for (const card of board.cards) {
      if (card.owner !== seat) continue;
      piles[card.zone] = (piles[card.zone] ?? 0) + 1;
    }
    return piles;
  };
  return { freeps: counted('freeps'), shadow: counted('shadow') };
}

function pathOf(board: AuthoringBoard): EditPathSite[] {
  return (board.path ?? []).map((site) => ({
    number: site.number,
    alias: site.alias ?? `site${site.number}`,
    blueprintId: site.blueprintId,
    name: site.name ?? site.blueprintId,
    printed: site.printed ?? null,
    owner: site.owner,
    controlledBy: site.controlledBy,
  }));
}

function words(table: AuthorTable): ModifyWord[] {
  return (table.state.modify?.words ?? []).map((word) => ({
    what: word.what,
    label: word.label,
    kind: word.kind === 'stat' ? 'stat' : 'keyword',
    number: word.number === 'signed' || word.number === 'positive' ? word.number : 'none',
    types: word.types,
  }));
}

/** The author's table; null until the draft's board has been laid out. */
export function authorBoard(table: AuthorTable): EditBoard | null {
  const board = table.state.board;
  if (board === null) return null;
  const cards = board.cards.map((card) => scenarioCard(table, card));
  const known = new Set(cards.flatMap((card) => (card.cardId === null ? [] : [card.cardId])));
  const modifyUntil = (table.state.modify?.until ?? []).filter((one): one is ModifierUntil =>
    (UNTIL as readonly string[]).includes(one),
  );
  return {
    mode: table.mode,
    level: 'author',
    tools: table.tools,
    canStop: false,
    canStart: false,
    over: table.state.position.over,
    cards: [...cards, ...pathCards(table, board), ...deckCards(board), ...slotCards(table, known)],
    piles: pilesOf(table, board),
    // (the author's board lists every card of the scenario's, wherever it is)
    hiddenPilesOpen: true,
    twilight: board.twilight,
    burdens: board.burdens,
    threats: board.threats,
    cultures: [OWN_TOKENS],
    modify: { words: words(table), until: modifyUntil.length > 0 ? modifyUntil : UNTIL },
    markers: board.markers ?? {},
    currentSite: board.currentSite ?? null,
    path: pathOf(board),
  };
}
