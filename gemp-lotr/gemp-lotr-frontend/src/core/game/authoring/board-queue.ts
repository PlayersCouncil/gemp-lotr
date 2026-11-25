/**
 * The Board tab's model (the legacy scenarioAuthor.js gathered / gather / forgetCard / renameGathered / pruneGathered
 * / boardView / boardAliases / cardChange / putSites / ringRow / addCard / dropCard): the changes to the board an
 * author gathers before sending them, how a change coalesces with the one before it, the board as it will be once
 * they are sent, and the parts of the scenario a change to its cards, its sites or its Ring writes. Pure: every
 * function answers a new value and changes nothing it is given.
 */
import { AuthoringBoard, BoardCard, Draft, DraftSeat, DraftStart, LaidSite } from './authoring-state';
import { GatheredBoard, GatheredCard } from './authoring-ui-store';
import { skirmishesWithout, startParts, startSetup, startWithout } from './board-start';
import { splitTerms } from './terms';

/** A change to the board as the Board tab gathers it ("wound", ["aragorn", 1]). */
export type BoardOp = GatheredBoard['ops'][number];
export type BoardArg = BoardOp['args'][number];

export const BOARD_SEATS: readonly DraftSeat[] = ['freeps', 'shadow'];
/** Where a card can be moved to, in a seat ("freeps.deck.top"). */
export const ZONES = [
  'characters',
  'support',
  'minions',
  'hand',
  'deck.top',
  'deck.bottom',
  'discard',
  'deadPile',
  'removed',
] as const;
/** Where a card can be, in the order the Board tab lists them, with what each is called there. */
export const PLACED: readonly (readonly [zone: string, label: string])[] = [
  ['characters', 'Characters'],
  ['minions', 'Minions'],
  ['support', 'Support area'],
  ['attached', 'On another card'],
  ['stacked', 'Stacked on another card'],
  ['hand', 'Hand'],
  ['deck', 'Deck, top first'],
  ['discard', 'Discard pile, top first'],
  ['deadPile', 'Dead pile'],
  ['removed', 'Removed from the game'],
];
/** The places whose cards come in an order that can be changed: order(seat.zone, card, card). */
export const ORDERED: readonly string[] = ['hand', 'deck', 'discard', 'deadPile'];
/** Where an added card can be put (the add-card place select): "" is the bottom of the deck. */
export const ADD_PLACES: readonly (readonly [zone: string, label: string])[] = [
  ['', 'deck (bottom)'],
  ['hand', 'hand'],
  ['characters', 'characters'],
  ['support', 'support'],
  ['minions', 'minions'],
  ['discard', 'discard'],
  ['deadPile', 'deadPile'],
];
/** ...and, when there is a card on the table to put it on, these (with the card as its host). */
export const ON_PLACES: readonly (readonly [zone: string, label: string])[] = [
  ['attach', 'attached to…'],
  ['stack', 'stacked on…'],
];

/**
 * What a card of the scenario may be called in it: the server's rule (ScenarioParser, renameCard). The legacy
 * add-card form refused dashes, which renaming allowed; one rule now, the server's.
 */
export const ALIAS_RULE = /^[A-Za-z][A-Za-z0-9_-]*$/;
export const ALIAS_RULE_TEXT = 'Letters, digits, dashes and underscores, starting with a letter';
/** The longest name the rename field takes. */
export const ALIAS_MAX = 40;

const TOTALS = ['twilight', 'burdens', 'threats'] as const;
type Total = (typeof TOTALS)[number];
const isTotal = (name: string): name is Total => (TOTALS as readonly string[]).includes(name);
/** The changes that are about one card, named first. */
const OF_A_CARD = ['wound', 'heal', 'tokens', 'move', 'modify', 'unmodify'];
/** The changes that put a card (first) on another (second). */
const ON_A_CARD = ['attach', 'stack'];
/** The changes of who controls a site (named first, by its number). */
const isControl = (name: string) => name === 'control' || name === 'liberate';

/** A number as the legacy panel read one (parseInt, nothing for what isn't one). */
function int(value: BoardArg | undefined): number {
  const read = parseInt(String(value), 10);
  return Number.isNaN(read) ? 0 : read;
}

const empty = (draft: string): GatheredBoard => ({ draft, ops: [], added: [], removed: [] });

/**
 * What has been gathered for this draft: what the panel remembers if it was gathered in it, else nothing (it was
 * another draft's, or what was remembered is not understood).
 */
export function gatheredOf(saved: GatheredBoard | null | undefined, draft: string): GatheredBoard {
  if (saved?.draft !== draft) return empty(draft);
  const list = <T>(value: readonly T[] | undefined): readonly T[] =>
    Array.isArray(value) ? (value as readonly T[]) : [];
  const kept = { draft, ops: list(saved.ops), added: list(saved.added), removed: list(saved.removed) };
  const start: unknown = saved.start;
  return start !== null && typeof start === 'object' && !Array.isArray(start) ? { ...kept, start } : kept;
}

export function gatheredCount(gathered: GatheredBoard): number {
  return gathered.ops.length + gathered.added.length + gathered.removed.length + startParts(gathered).length;
}

/** A change to the board as the server reads it: "wound(aragorn, 1)". */
export function opText(op: BoardOp): string {
  return `${op.name}(${op.args.join(', ')})`;
}

/**
 * Gathers one change to the board. A change that undoes or goes over the one before it takes its place:
 * - the same pile put in another order, or the same card moved again: the last one;
 * - a wound and a healing of the same card, one after the other: the two net out (and go if they come to nothing);
 * - tokens on the same card, one after the other: added up;
 * - a total (twilight, burdens, threats) set: every earlier setting of it goes, and so does this one if it sets it
 *   to what the board has;
 * - a seat's marker put on a site: every earlier one of that seat's goes, and so does this one if the marker is
 *   there;
 * - a site controlled or freed: every earlier change of who controls it goes, and so does this one if that is how
 *   the site is.
 * (`board` null: nothing is compared with the board, as for the board the scenario starts with.)
 */
export function gather(
  gathered: GatheredBoard,
  board: AuthoringBoard | null,
  name: string,
  args: readonly BoardArg[],
): GatheredBoard {
  let ops = [...gathered.ops];
  const last = ops.at(-1);
  const target = args[0] ?? '';
  const same = last?.name === name && String(last.args[0]).toLowerCase() === String(target).toLowerCase();
  const signed = (op: BoardOp) => (op.name === 'heal' ? -1 : 1) * int(op.args[1]);
  let op: BoardOp = { name, args };
  if (name === 'order' || name === 'move') {
    if (same) ops.pop();
  } else if (name === 'wound' || name === 'heal') {
    if (last !== undefined && (last.name === 'wound' || last.name === 'heal') && last.args[0] === args[0]) {
      const net = signed(last) + signed(op);
      ops.pop();
      if (net === 0) return { ...gathered, ops };
      op = { name: net > 0 ? 'wound' : 'heal', args: [target, Math.abs(net)] };
    }
  } else if (name === 'tokens') {
    if (same) {
      op = { name, args: [target, int(last.args[1]) + int(args[1])] };
      ops.pop();
    }
  } else if (isTotal(name)) {
    // (a total is set to what it is to be, whatever it was set to before: and not at all if that is what it is)
    ops = ops.filter((each) => each.name !== name);
    if (board !== null && board[name] === parseInt(String(args[0]), 10)) return { ...gathered, ops };
  } else if (name === 'marker') {
    ops = ops.filter((each) => !(each.name === 'marker' && each.args[0] === target));
    if (board?.markers?.[target as DraftSeat] === int(args[1])) return { ...gathered, ops };
  } else if (name === 'control' || name === 'liberate') {
    ops = ops.filter((each) => !(isControl(each.name) && String(each.args[0]) === String(target)));
    const laid = board?.path?.find((site) => String(site.number) === String(target));
    if (laid !== undefined && laid.controlledBy === (name === 'control' ? args[1] : null)) return { ...gathered, ops };
  }
  ops.push(op);
  return { ...gathered, ops };
}

/** What has been gathered about a card dropped (it is being taken out, or put in again as another). */
export function forgetCard(gathered: GatheredBoard, alias: string): GatheredBoard {
  const kept: BoardOp[] = [];
  for (const op of gathered.ops) {
    if (OF_A_CARD.includes(op.name) && op.args[0] === alias) continue;
    if (ON_A_CARD.includes(op.name) && (op.args[0] === alias || op.args[1] === alias)) continue;
    if (op.name === 'order' && op.args.indexOf(alias) > 0) {
      const rest = op.args.filter((arg, at) => at === 0 || arg !== alias);
      if (rest.length < 2) continue;
      kept.push({ name: 'order', args: rest });
      continue;
    }
    kept.push(op);
  }
  return startWithout({ ...gathered, ops: kept }, (each) => each !== alias);
}

/** A card has been given another name: what has been gathered says the new one. */
export function renameGathered(gathered: GatheredBoard, from: string, to: string): GatheredBoard {
  const swap = <T extends BoardArg>(alias: T): T | string => (alias === from ? to : alias);
  const ops = gathered.ops.map((op): BoardOp => {
    if (op.name === 'order') return { name: op.name, args: op.args.map((arg, at) => (at === 0 ? arg : swap(arg))) };
    if (ON_A_CARD.includes(op.name)) return { name: op.name, args: [swap(op.args[0] ?? ''), swap(op.args[1] ?? '')] };
    if (OF_A_CARD.includes(op.name)) return { name: op.name, args: [swap(op.args[0] ?? ''), ...op.args.slice(1)] };
    return op;
  });
  return { ...gathered, ops, removed: gathered.removed.map((alias) => swap(alias)) };
}

/** The cards a change names (an order's cards, both cards of an attach or a stack, the card of the others). */
function cardsNamed(op: BoardOp): readonly BoardArg[] {
  if (op.name === 'order') return op.args.slice(1);
  if (ON_A_CARD.includes(op.name)) return op.args;
  if (OF_A_CARD.includes(op.name)) return op.args.slice(0, 1);
  return [];
}

/**
 * What was gathered about cards the scenario does not have (any more: the page may have been left for another game)
 * let go of; and a card both taken out and put in again is put in.
 */
export function pruneGathered(gathered: GatheredBoard, board: AuthoringBoard): GatheredBoard {
  const added = gathered.added.map((add) => add.alias);
  const known = new Set<BoardArg>([...board.cards.map((card) => card.alias), ...added]);
  const kept = {
    ...gathered,
    removed: gathered.removed.filter((alias) => known.has(alias) && !added.includes(alias)),
    ops: gathered.ops.filter((op) => cardsNamed(op).every((alias) => known.has(alias))),
  };
  return startWithout(kept, (alias) => known.has(alias));
}

/** A card as the board will have it once what has been gathered is sent. */
export interface ViewCard extends Omit<BoardCard, 'cardId'> {
  /** Null for a card that is to be added. */
  cardId: number | null;
  /** Something has been gathered about it. */
  touched: boolean;
  /** It is to be added. */
  added: boolean;
  /** It is to be taken out. */
  removing: boolean;
}

/** A site on the adventure path as it will be. */
export interface ViewSite extends LaidSite {
  /** It is not on the path yet: a marker put there lays it. */
  toBeLaid: boolean;
  /** Who controls it is to change. */
  touched: boolean;
}

export interface BoardView {
  twilight: number;
  burdens: number;
  threats: number;
  cards: readonly ViewCard[];
  /** Where each seat's marker is to be. */
  markers: Readonly<Record<DraftSeat, number>>;
  /** The adventure path, by number, to the furthest marker. */
  path: readonly ViewSite[];
}

const atOf = (card: { at: number | null }) => card.at ?? 9999;

/**
 * The board as it will be once what has been gathered is sent: the server's board with the changes made to it.
 * A card put in under the name of one the scenario has takes its place. (A move keeps the card's seat: the tab only
 * moves a card within its own, and the server refuses another's.)
 */
export function boardView(board: AuthoringBoard, gathered: GatheredBoard): BoardView {
  const added = new Set(gathered.added.map((add) => add.alias));
  const cards: ViewCard[] = board.cards
    .filter((card) => !added.has(card.alias))
    .map((card) => ({ ...card, touched: false, added: false, removing: false }));
  const view = { twilight: board.twilight, burdens: board.burdens, threats: board.threats };
  let serial = 0;
  const find = (alias: BoardArg | undefined) => cards.find((card) => card.alias === alias) ?? null;
  const cardsIn = (owner: string, zone: string | undefined) =>
    cards.filter((card) => card.owner === owner && card.zone === zone).sort((a, b) => atOf(a) - atOf(b));
  for (const add of gathered.added) {
    const placed =
      add.zone === '' ? 'deck' : add.zone === 'attach' ? 'attached' : add.zone === 'stack' ? 'stacked' : add.zone;
    cards.push({
      alias: add.alias,
      blueprintId: add.blueprintId,
      name: add.name,
      type: add.type,
      owner: add.seat,
      cardId: null,
      zone: placed,
      at: 1000 + ++serial,
      wounds: 0,
      tokens: 0,
      on: add.zone === 'attach' || add.zone === 'stack' ? add.host : null,
      added: true,
      touched: true,
      removing: false,
    });
  }
  for (const alias of gathered.removed) {
    const card = find(alias);
    if (card !== null) {
      card.removing = true;
      card.touched = true;
    }
  }
  for (const op of gathered.ops) {
    if (isTotal(op.name)) {
      view[op.name] = int(op.args[0]);
      continue;
    }
    if (op.name === 'order') {
      const [seat, zone] = String(op.args[0]).split('.');
      const there = cardsIn(seat ?? '', zone);
      const first: ViewCard[] = [];
      for (const alias of op.args.slice(1)) {
        const each = find(alias);
        if (each !== null && each.owner === seat && each.zone === zone) {
          first.push(each);
          each.touched = true;
        }
      }
      [...first, ...there.filter((each) => !first.includes(each))].forEach((each, n) => (each.at = n));
      continue;
    }
    const card = find(op.args[0]);
    if (card === null) continue;
    if (op.name === 'move') {
      const to = String(op.args[1]).split('.');
      card.zone = to[1] ?? card.zone;
      card.on = null;
      // (on the top of a pile is before every card, and before those put on the top earlier)
      card.at = to[2] === 'top' ? -1000 - ++serial : 1000 + ++serial;
      card.touched = true;
    } else if (ON_A_CARD.includes(op.name)) {
      card.zone = op.name === 'attach' ? 'attached' : 'stacked';
      card.on = String(op.args[1]);
      card.touched = true;
    } else if (op.name === 'wound') {
      card.wounds += int(op.args[1]);
      card.touched = true;
    } else if (op.name === 'heal') {
      card.wounds = Math.max(0, card.wounds - int(op.args[1]));
      card.touched = true;
    } else if (op.name === 'tokens') {
      card.tokens += int(op.args[1]);
      card.touched = true;
    }
  }
  return { ...view, cards, ...pathView(board, gathered) };
}

/**
 * The markers and the adventure path as they will be: each seat's marker where the last change put it, the path laid
 * on to the furthest marker (with the Free Peoples seat's sites, as the game lays them unless the setup says
 * otherwise), and who controls each site.
 */
function pathView(board: AuthoringBoard, gathered: GatheredBoard): Pick<BoardView, 'markers' | 'path'> {
  const markers = { freeps: board.markers?.freeps ?? 1, shadow: board.markers?.shadow ?? 1 };
  const path: ViewSite[] = (board.path ?? []).map((site) => ({ ...site, toBeLaid: false, touched: false }));
  for (const op of gathered.ops) {
    if (op.name === 'marker' && (op.args[0] === 'freeps' || op.args[0] === 'shadow'))
      markers[op.args[0]] = int(op.args[1]);
  }
  for (let number = path.length + 1; number <= Math.max(markers.freeps, markers.shadow); number++) {
    const own = board.sites.freeps[number - 1];
    path.push({
      number,
      alias: own?.alias ?? null,
      blueprintId: own?.id ?? '',
      name: own?.name ?? null,
      owner: 'freeps',
      controlledBy: null,
      toBeLaid: true,
      touched: false,
    });
  }
  for (const op of gathered.ops) {
    if (!isControl(op.name)) continue;
    const site = path.find((each) => String(each.number) === String(op.args[0]) || each.alias === op.args[0]);
    if (site === undefined) continue;
    site.controlledBy = op.name === 'control' ? (op.args[1] as DraftSeat) : null;
    site.touched = true;
  }
  return { markers, path };
}

/** The names of the sites, both seats' (site1, site1opp, or the names they have been given). */
export function siteAliases(board: AuthoringBoard | null): string[] {
  const all: string[] = [];
  for (const seat of BOARD_SEATS) for (const site of board?.sites[seat] ?? []) if (site.alias) all.push(site.alias);
  return all;
}

/** The names taken: the cards the board will have (those to be taken out are free again) and the sites. */
export function boardAliases(board: AuthoringBoard, gathered: GatheredBoard): string[] {
  return [
    ...boardView(board, gathered)
      .cards.filter((card) => !card.removing)
      .map((card) => card.alias),
    ...siteAliases(board),
  ];
}

/** The name a site has when it has not been given one: site3, and site3opp for the opponent's. */
export function defaultSiteAlias(human: DraftSeat, seat: DraftSeat, number: number): string {
  return `site${number}${seat === human ? '' : 'opp'}`;
}

/** A part of the file as plain JSON. */
type Bag = Readonly<Record<string, unknown>>;
type CardsPart = Partial<Record<DraftSeat, Readonly<Record<string, string>>>>;

/** A part of the file to change, as a map (its entries in the file's order). */
const mapOf = (bag: unknown) =>
  new Map<string, unknown>(bag !== null && typeof bag === 'object' ? Object.entries(bag as Bag) : []);

/** The scenario's parts a change to its cards writes (POST edit top). */
export interface CardChange {
  /** Absent: no card is put in or taken out. */
  cards?: CardsPart;
  /** Null: the scenario has no setup left. */
  setup: Bag | null;
}

/**
 * The cards gathered to be put in and taken out, and where the story starts, as the scenario's parts: a card taken
 * out is out of its seat's cards and out of every place the setup puts it (a skirmish already assigned too); a card
 * put in is in its seat's cards and where it was put (the bottom of the deck is where the setup puts nothing); the
 * start as startSetup writes it. Null when nothing of the kind has been gathered.
 */
export function cardChange(draft: Draft, gathered: GatheredBoard, start: DraftStart | null = null): CardChange | null {
  const starting = start !== null && startParts(gathered).length > 0;
  if (gathered.added.length === 0 && gathered.removed.length === 0 && !starting) return null;
  const cards = new Map(BOARD_SEATS.map((seat) => [seat, mapOf(draft.cards?.[seat])] as const));
  const setup = mapOf(draft.setup);
  /** A part of the setup that names cards (a seat's places, attach...), as a map; it goes when it is empty. */
  const change = (name: string, how: (part: Map<string, unknown>) => void) => {
    const part = mapOf(setup.get(name));
    how(part);
    if (part.size === 0) setup.delete(name);
    else setup.set(name, Object.fromEntries(part));
  };
  const take = (alias: string) => {
    for (const seat of BOARD_SEATS) {
      cards.get(seat)?.delete(alias);
      if (!setup.has(seat)) continue;
      change(seat, (places) => {
        for (const [zone, there] of places) {
          const kept = splitTerms(String(there)).filter((each) => each !== alias);
          if (kept.length === 0) places.delete(zone);
          else places.set(zone, kept.join(', '));
        }
      });
    }
    for (const name of ['attach', 'stack', 'wounds', 'tokens']) {
      if (setup.has(name)) change(name, (named) => named.delete(alias));
    }
    if (setup.has('skirmishes')) {
      const fights = skirmishesWithout(setup.get('skirmishes'), alias);
      if (fights === null) setup.delete('skirmishes');
      else setup.set('skirmishes', fights);
    }
  };
  for (const alias of gathered.removed) take(alias);
  const seated = new Set<DraftSeat>(BOARD_SEATS.filter((seat) => draft.cards?.[seat] !== undefined));
  for (const add of gathered.added) {
    // (one that is put in under the name of a card the scenario has is that card, replaced)
    take(add.alias);
    cards.get(add.seat)?.set(add.alias, add.blueprintId);
    seated.add(add.seat);
    if (add.zone === 'attach' || add.zone === 'stack') {
      change(add.zone, (named) => named.set(add.alias, add.host));
    } else if (add.zone !== '') {
      change(add.seat, (places) => {
        const there = places.get(add.zone);
        places.set(add.zone, typeof there === 'string' && there !== '' ? `${there}, ${add.alias}` : add.alias);
      });
    }
  }
  const written: Partial<Record<DraftSeat, Record<string, string>>> = {};
  for (const seat of BOARD_SEATS) {
    if (seated.has(seat)) written[seat] = Object.fromEntries(cards.get(seat) ?? []) as Record<string, string>;
  }
  const cardsSetup = setup.size === 0 ? null : Object.fromEntries(setup);
  if (!starting) return { cards: written, setup: cardsSetup };
  // (a card taken out is in no skirmish the start has)
  const out = new Set(gathered.removed);
  const left = Object.entries(start.skirmishes)
    .filter(([character]) => !out.has(character))
    .map(([character, minions]) => [character, minions.filter((minion) => !out.has(minion))] as const);
  const setupStart = startSetup(cardsSetup, { ...start, skirmishes: Object.fromEntries(left) }, gathered);
  const cardsChanged = gathered.added.length > 0 || gathered.removed.length > 0;
  return { ...(cardsChanged ? { cards: written } : {}), setup: setupStart };
}

/** One of the scenario's cards made another (by its blueprint), keeping its name and its place; null if it has none. */
export function changeCardTop(draft: Draft, card: Pick<BoardCard, 'owner' | 'alias'>, blueprintId: string) {
  const own = draft.cards?.[card.owner];
  if (own?.[card.alias] === undefined) return null;
  return { cards: { ...draft.cards, [card.owner]: { ...own, [card.alias]: blueprintId } } };
}

/**
 * The seats' paths as the scenario writes them (POST edit top.sites): sites that have been given names keep them,
 * place by place, and a path is written with its names ({site1: id, ...}) if any has one that is not its own.
 */
export function sitesTop(
  board: AuthoringBoard | null,
  human: DraftSeat,
  freeps: readonly string[],
  shadow: readonly string[],
) {
  const path = (seat: DraftSeat, ids: readonly string[]) => {
    const names = ids.map((_, at) => board?.sites[seat][at]?.alias ?? defaultSiteAlias(human, seat, at + 1));
    const named = names.some((alias, at) => alias !== defaultSiteAlias(human, seat, at + 1));
    return named ? Object.fromEntries(names.map((alias, at) => [alias, ids[at]])) : ids.join(', ');
  };
  return { sites: { freeps: path('freeps', freeps), shadow: path('shadow', shadow) } };
}

/**
 * The seats' paths with the site at `at` put at `to` in each of `seats`' (the other's as it is), every site keeping
 * its name: written with the names once any is not its place's own.
 */
export function sitesReordered(
  board: AuthoringBoard,
  human: DraftSeat,
  seats: readonly DraftSeat[],
  at: number,
  to: number,
) {
  const path = (seat: DraftSeat) => {
    const sites = board.sites[seat].map((site) => ({
      alias: site.alias ?? defaultSiteAlias(human, seat, site.number),
      id: site.id,
    }));
    if (seats.includes(seat)) sites.splice(to, 0, ...sites.splice(at, 1));
    const named = sites.some((site, place) => site.alias !== defaultSiteAlias(human, seat, place + 1));
    return named
      ? Object.fromEntries(sites.map((site) => [site.alias, site.id]))
      : sites.map((site) => site.id).join(', ');
  };
  return { sites: { freeps: path('freeps'), shadow: path('shadow') } };
}

/** The seat's Ring-bearer or Ring chosen (`""`: none), as the scenario writes the part (null when no seat has one). */
export function ringTop(draft: Draft, part: 'ringBearer' | 'ring', seat: DraftSeat, value: string) {
  const now = new Map(Object.entries(draft[part] ?? {}));
  if (value === '') now.delete(seat);
  else now.set(seat, value);
  return { [part]: now.size === 0 ? null : Object.fromEntries(now) };
}

/** The ids of a seat's path, in order. */
export function pathIds(board: AuthoringBoard, seat: DraftSeat): string[] {
  return board.sites[seat].map((site) => site.id);
}

/** Whether the two seats' paths are the same sites. */
export function samePaths(board: AuthoringBoard): boolean {
  return pathIds(board, 'freeps').join(',') === pathIds(board, 'shadow').join(',');
}

/**
 * A name for a card found by name: the first word of its name that says something ("The One Ring" is "one", not
 * "the"; accents taken off first, so Palantír is "palantir"; "c" before one that starts with a digit), numbered 2,
 * 3... until it is a name not taken.
 */
export function suggestAlias(name: string, taken: readonly string[]): string {
  const plain = name.normalize('NFD').replace(/[̀-ͯ]/g, '');
  const words = plain
    .toLowerCase()
    .replace(/[^a-z0-9 ]/g, '')
    .split(' ')
    .filter((word) => word.length > 0 && !['the', 'a', 'an', 'of'].includes(word));
  let stem = words[0] ?? 'card';
  if (/^[0-9]/.test(stem)) stem = `c${stem}`;
  let alias = stem;
  for (let n = 2; taken.includes(alias); n++) alias = stem + String(n);
  return alias;
}

/** What is wrong with a name for a card to be added (null: nothing). */
export function addAliasProblem(alias: string, board: AuthoringBoard | null): string | null {
  if (!ALIAS_RULE.test(alias)) {
    return "A card's name in the scenario is made of letters, digits, dashes and underscores, starting with a letter.";
  }
  if (siteAliases(board).includes(alias))
    return `${alias} is the name of a site of the scenario's: give the card another.`;
  return null;
}

/** What is wrong with a card's new name (null: nothing); `taken`: the names the scenario has. */
export function renameProblem(to: string, taken: readonly string[]): string | null {
  if (taken.includes(to)) return 'The scenario already has a card called that';
  if (!ALIAS_RULE.test(to)) return ALIAS_RULE_TEXT;
  return null;
}

/** The card of that name the board will have (not one to be taken out), which a card added under it replaces. */
export function existingCard(view: BoardView, alias: string): ViewCard | null {
  return view.cards.find((card) => card.alias === alias && !card.removing) ?? null;
}

/** The question asked before a card added under that name replaces the one that has it. */
export function replaceQuestion(existing: ViewCard, found: { name: string }): string {
  const seat = existing.owner === 'freeps' ? 'Free Peoples' : 'Shadow';
  return (
    `${existing.alias} is already ${existing.name} (${existing.blueprintId}), ${seat} seat` +
    `${existing.added ? ', waiting to be added' : ''}.\n\n` +
    `${found.name} takes its place: what says ${existing.alias} is then about it, and it goes where you chose.`
  );
}

/** A card found by name, as a card of the board types it (the enum's name: "COMPANION", "THE_ONE_RING"). */
export function boardType(foundType: string | null | undefined): string {
  return (foundType ?? '').toUpperCase();
}

/**
 * A card gathered to be added (whatever was to be done to a card of that name was to be done to the one it replaces,
 * and is let go of; it is not taken out).
 */
export function addCard(gathered: GatheredBoard, card: GatheredCard): GatheredBoard {
  const forgot = forgetCard(gathered, card.alias);
  return {
    ...forgot,
    removed: forgot.removed.filter((each) => each !== card.alias),
    added: [...forgot.added.filter((each) => each.alias !== card.alias), card],
  };
}

/** A card's ×: one that was to be added is not added after all; one the scenario has is gathered to be taken out. */
export function dropCard(gathered: GatheredBoard, card: Pick<ViewCard, 'alias' | 'added'>): GatheredBoard {
  const forgot = forgetCard(gathered, card.alias);
  if (card.added) return { ...forgot, added: forgot.added.filter((add) => add.alias !== card.alias) };
  if (forgot.removed.includes(card.alias)) return forgot;
  return { ...forgot, removed: [...forgot.removed, card.alias] };
}

/** "Keep it": a card gathered to be taken out stays. */
export function keepCard(gathered: GatheredBoard, alias: string): GatheredBoard {
  return { ...gathered, removed: gathered.removed.filter((each) => each !== alias) };
}

/** A gathered line's ×: what it gathered is let go of (a card to be added with what was gathered about it). */
export function undoRemoved(gathered: GatheredBoard, index: number): GatheredBoard {
  return { ...gathered, removed: gathered.removed.filter((_, at) => at !== index) };
}

export function undoAdded(gathered: GatheredBoard, index: number): GatheredBoard {
  const add = gathered.added[index];
  if (add === undefined) return gathered;
  const forgot = forgetCard(gathered, add.alias);
  return { ...forgot, added: forgot.added.filter((_, at) => at !== index) };
}

export function undoOp(gathered: GatheredBoard, index: number): GatheredBoard {
  return { ...gathered, ops: gathered.ops.filter((_, at) => at !== index) };
}

/** How the gathered list says a card to be added: "add Gandalf, ... as gandalf (freeps, hand)". */
export function addedText(add: GatheredCard): string {
  const place =
    add.zone === ''
      ? 'deck'
      : add.zone === 'attach'
        ? `on ${String(add.host)}`
        : add.zone === 'stack'
          ? `stacked on ${String(add.host)}`
          : add.zone;
  return `add ${add.name} as ${add.alias} (${add.seat}, ${place})`;
}

/** What another card can be put on: anything on the table, a card that is itself on another included. */
export function bearersOf(view: BoardView): ViewCard[] {
  return view.cards.filter(
    (card) =>
      !card.removing &&
      (card.zone === 'characters' || card.zone === 'minions' || card.zone === 'support' || card.zone === 'attached'),
  );
}

/** A card of a seat's list, where it comes in its place. */
export interface ListedCard {
  card: ViewCard;
  /** The first of its place: the place's heading comes before it ("Hand (3)"). */
  heading: string | null;
  /** Its place's cards can be put in another order (and there are several). */
  ordered: boolean;
  at: number;
  /** The aliases of its place's cards, in their order. */
  among: readonly string[];
}

/**
 * A seat's cards by where they are, in PLACED's order (a place the list does not know comes after, by its own name),
 * each hand and pile in the order its cards come.
 */
export function seatCards(view: BoardView, seat: DraftSeat): ListedCard[] {
  const places = PLACED.map(([zone, label]) => [zone, label] as [string, string]);
  const held = new Map<string, ViewCard[]>();
  for (const card of view.cards) {
    if (card.owner !== seat) continue;
    const there = held.get(card.zone);
    if (there) {
      there.push(card);
      continue;
    }
    held.set(card.zone, [card]);
    if (!places.some(([zone]) => zone === card.zone)) places.push([card.zone, card.zone]);
  }
  const listed: ListedCard[] = [];
  for (const [zone, label] of places) {
    const there = held.get(zone);
    if (there === undefined) continue;
    const ordered = ORDERED.includes(zone);
    if (ordered) there.sort((a, b) => atOf(a) - atOf(b));
    const among = there.map((card) => card.alias);
    there.forEach((card, at) =>
      listed.push({
        card,
        heading: at === 0 ? `${label} (${there.length})` : null,
        ordered: ordered && there.length > 1,
        at,
        among,
      }),
    );
  }
  return listed;
}

/** The order of a hand or pile with the card at `at` put at `to` (order(seat.zone, ...) gathers it). */
export function reordered(among: readonly string[], at: number, to: number): string[] {
  const aliases = [...among];
  aliases.splice(to, 0, ...aliases.splice(at, 1));
  return aliases;
}

/** The seat's cards of a type, as the Ring selects offer them: [alias, "alias (name)"]. */
export function ringChoices(board: AuthoringBoard, seat: DraftSeat, type: string): [string, string][] {
  return board.cards
    .filter((card) => card.owner === seat && card.type === type)
    .map((card) => [card.alias, `${card.alias} (${card.name})`]);
}

/* ---------------------------------------------------------------- what the setup can have (A3, B10) */

/**
 * Where a seat's card can be moved to: every place, but to the setup no minions on the table for the Free Peoples
 * seat (the server refuses setup.freeps.minions: minions on the table at the start are the Shadow seat's), and with
 * the game's own opening (`deckOnly`) only a deck.
 */
export function movesFor(seat: DraftSeat, inSetup: boolean, deckOnly: boolean): readonly string[] {
  if (deckOnly) return ['deck.top', 'deck.bottom'];
  return ZONES.filter((zone) => !(inSetup && seat === 'freeps' && zone === 'minions'));
}

/**
 * Where a card added can be put (it goes into the setup): no minions for the Free Peoples seat; with the game's own
 * opening (`realOpening`), only a deck; on another card only where there is one to put it on.
 */
export function addPlacesFor(
  seat: DraftSeat,
  realOpening: boolean,
  hosts: boolean,
): (readonly [zone: string, label: string])[] {
  if (realOpening) return ADD_PLACES.filter(([zone]) => zone === '');
  return [...ADD_PLACES.filter(([zone]) => !(seat === 'freeps' && zone === 'minions')), ...(hosts ? ON_PLACES : [])];
}
