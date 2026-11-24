/**
 * The table as an edit level knows it (`GET scenarios/sandbox/state`, ScenarioSandbox.state and BoardView.java): what
 * the page's own game view does not have (every card's name in the scenario and owning seat, the hidden piles when they
 * are open, a card's hand-put modifiers, the cultures a token can be, the Modify… menu's vocabulary). Read
 * defensively: anything unreadable is left out. Pure.
 */
import { EDIT_TOOLS, EditSeat, EditTool, ModifierUntil } from './edit-ops';

export type EditMode = 'play' | 'edit';
/** Who is changing the table: nobody (an ordinary scenario), a solver before Submit, a sandbox's player, an author. */
export type EditLevel = 'none' | 'puzzle' | 'sandbox' | 'author';

/** A modifier put on a card by hand and still in force. */
export interface EditModifier {
  /** Counted from 1 in each game, in the order put on: `unmodify` by id takes this one off. */
  id: number;
  what: string;
  /** Null for a keyword that takes no number. */
  amount: number | null;
  until: ModifierUntil;
  /** As the card's information shows it: "Strength +2". */
  text: string;
}

export interface EditCard {
  /** Its name in the scenario (an added card's: its title in lower case, `legolas2`). */
  alias: string;
  blueprintId: string;
  name: string;
  /** The card type in capitals: COMPANION, MINION, SITE, THE_ONE_RING... */
  type: string;
  owner: EditSeat;
  /** Its card id now, as the page has it (text); null when the server gives none. */
  cardId: string | null;
  /** characters, support, minions, hand, deck, discard, deadPile, removed, attached, stacked, adventureDeck, path. */
  zone: string;
  wounds: number;
  /** Culture tokens by culture name. */
  cultureTokens: Readonly<Record<string, number>>;
  culture: string | null;
  /** The alias of the card it is attached to or stacked on. */
  on: string | null;
  modifiers: readonly EditModifier[];
  /** Made by an edit, not one of the scenario's own. */
  added: boolean;
  /** Its place in a hand or an ordered pile (0: the top of a pile); null elsewhere. (Optional for a backend's own boards.) */
  at?: number | null;
  /** A site's number on the path (a controlled one's too); null elsewhere. */
  site?: number | null;
  /** A site's printed number (0: none printed); null for a card that is not a site. */
  printed?: number | null;
}

/** A site laid on the adventure path (the state's `path`). */
export interface EditPathSite {
  number: number;
  alias: string;
  blueprintId: string;
  name: string;
  printed: number | null;
  owner: EditSeat | null;
  /** The seat controlling it, or null. */
  controlledBy: EditSeat | null;
}

/** One of the things Modify… offers. */
export interface ModifyWord {
  what: string;
  label: string;
  kind: 'stat' | 'keyword';
  /** `signed`: a stat's change, never 0; `positive`: a numbered keyword, 1 or more; `none`: no number. */
  number: 'signed' | 'positive' | 'none';
  /** The card types it means something on. */
  types: readonly string[];
}

export interface EditBoard {
  mode: EditMode;
  level: EditLevel;
  tools: readonly EditTool[];
  canStop: boolean;
  canStart: boolean;
  /** The game is over: nothing can be changed. */
  over: boolean;
  cards: readonly EditCard[];
  /** How many cards each pile of each seat holds. */
  piles: Readonly<Record<EditSeat, Readonly<Record<string, number>>>>;
  /** Every pile of both seats is listed in `cards` (the `piles` tool). */
  hiddenPilesOpen: boolean;
  twilight: number;
  burdens: number;
  threats: number;
  /** The culture names a token can be. */
  cultures: readonly string[];
  modify: { words: readonly ModifyWord[]; until: readonly ModifierUntil[] };
  /** The site each seat's marker is at. (These three are optional for a backend's own boards.) */
  markers?: Readonly<Partial<Record<EditSeat, number>>>;
  /** The site of the fellowship whose turn it is. */
  currentSite?: number | null;
  /** The sites laid on the path, by number. */
  path?: readonly EditPathSite[];
}

type Json = Record<string, unknown>;

const object = (value: unknown): Json =>
  typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as Json) : {};
const list = (value: unknown): unknown[] => (Array.isArray(value) ? value : []);
const text = (value: unknown): string | null => (typeof value === 'string' ? value : null);
const number = (value: unknown): number | null => (typeof value === 'number' && Number.isFinite(value) ? value : null);
const strings = (value: unknown): string[] => list(value).filter((item): item is string => typeof item === 'string');
const seat = (value: unknown): EditSeat | null => (value === 'freeps' || value === 'shadow' ? value : null);

export const EDIT_MODES: readonly EditMode[] = ['play', 'edit'];
export const EDIT_LEVELS: readonly EditLevel[] = ['none', 'puzzle', 'sandbox', 'author'];

export function editMode(value: unknown): EditMode {
  return value === 'edit' ? 'edit' : 'play';
}

export function editLevel(value: unknown): EditLevel {
  return EDIT_LEVELS.find((level) => level === value) ?? 'none';
}

/** The tools named, in the server's order, each once; unknown names left out. */
export function editTools(value: unknown): EditTool[] {
  const named = strings(value);
  return EDIT_TOOLS.filter((tool) => named.includes(tool));
}

const UNTIL: readonly ModifierUntil[] = ['phase', 'turn', 'removed'];
const until = (value: unknown): ModifierUntil => UNTIL.find((known) => known === value) ?? 'removed';

function modifier(value: unknown): EditModifier | null {
  const json = object(value);
  const id = number(json['id']);
  const what = text(json['what']);
  if (id === null || what === null) return null;
  return {
    id,
    what,
    amount: number(json['amount']),
    until: until(json['until']),
    text: text(json['text']) ?? what,
  };
}

function counts(value: unknown): Record<string, number> {
  return Object.fromEntries(
    Object.entries(object(value)).flatMap(([name, count]) => {
      const amount = number(count);
      return amount === null ? [] : [[name, amount] as const];
    }),
  );
}

function card(value: unknown): EditCard | null {
  const json = object(value);
  const alias = text(json['alias']);
  const blueprintId = text(json['blueprintId']);
  const owner = seat(json['owner']);
  if (alias === null || blueprintId === null || owner === null) return null;
  const id = json['cardId'];
  return {
    alias,
    blueprintId,
    name: text(json['name']) ?? blueprintId,
    type: text(json['type']) ?? '',
    owner,
    cardId: typeof id === 'number' || typeof id === 'string' ? String(id) : null,
    zone: text(json['zone']) ?? '',
    wounds: number(json['wounds']) ?? 0,
    cultureTokens: counts(json['cultureTokens']),
    culture: text(json['culture']),
    on: text(json['on']),
    modifiers: list(json['modifiers']).flatMap((item) => {
      const read = modifier(item);
      return read ? [read] : [];
    }),
    added: json['added'] === true,
    at: number(json['at']),
    site: number(json['site']),
    printed: number(json['printed']),
  };
}

function pathSite(value: unknown): EditPathSite | null {
  const json = object(value);
  const at = number(json['number']);
  const alias = text(json['alias']);
  if (at === null || alias === null) return null;
  return {
    number: at,
    alias,
    blueprintId: text(json['blueprintId']) ?? '',
    name: text(json['name']) ?? alias,
    printed: number(json['printed']),
    owner: seat(json['owner']),
    controlledBy: seat(json['controlledBy']),
  };
}

function word(value: unknown): ModifyWord | null {
  const json = object(value);
  const what = text(json['what']);
  if (what === null) return null;
  const kind = json['kind'] === 'stat' ? 'stat' : 'keyword';
  const numbered = json['number'];
  return {
    what,
    label: text(json['label']) ?? what,
    kind,
    number: numbered === 'signed' || numbered === 'positive' ? numbered : 'none',
    types: strings(json['types']),
  };
}

/** The state the server sent (the state call, or the `state` of a change's answer). */
export function readEditBoard(value: unknown): EditBoard {
  const json = object(value);
  const piles = object(json['piles']);
  const modify = object(json['modify']);
  const modifyUntil = strings(modify['until']).filter((name): name is ModifierUntil =>
    (UNTIL as readonly string[]).includes(name),
  );
  return {
    mode: editMode(json['mode']),
    level: editLevel(json['level']),
    tools: editTools(json['tools']),
    canStop: json['canStop'] === true,
    canStart: json['canStart'] === true,
    over: json['over'] === true,
    cards: list(json['cards']).flatMap((item) => {
      const read = card(item);
      return read ? [read] : [];
    }),
    piles: { freeps: counts(piles['freeps']), shadow: counts(piles['shadow']) },
    hiddenPilesOpen: json['hiddenPilesOpen'] === true,
    twilight: number(json['twilight']) ?? 0,
    burdens: number(json['burdens']) ?? 0,
    threats: number(json['threats']) ?? 0,
    cultures: strings(json['cultures']),
    modify: {
      words: list(modify['words']).flatMap((item) => {
        const read = word(item);
        return read ? [read] : [];
      }),
      until: modifyUntil.length > 0 ? modifyUntil : UNTIL,
    },
    markers: Object.fromEntries(Object.entries(counts(json['markers'])).filter(([name]) => seat(name) !== null)),
    currentSite: number(json['currentSite']),
    path: list(json['path'])
      .flatMap((item) => {
        const read = pathSite(item);
        return read ? [read] : [];
      })
      .sort((a, b) => a.number - b.number),
  };
}

/** The card of the board with the page's card id, or null. */
export function cardById(board: EditBoard | null, cardId: string): EditCard | null {
  return board?.cards.find((candidate) => candidate.cardId === cardId) ?? null;
}

/** The card of the board with the name, or null. */
export function cardByAlias(board: EditBoard | null, alias: string | null): EditCard | null {
  return alias === null ? null : (board?.cards.find((candidate) => candidate.alias === alias) ?? null);
}

/** The zones of the table: a card there is in play (it can be wounded, given tokens, modified, attached to). */
export const TABLE_ZONES: readonly string[] = ['characters', 'support', 'minions', 'attached'];

export function onTable(card: EditCard): boolean {
  return TABLE_ZONES.includes(card.zone);
}
