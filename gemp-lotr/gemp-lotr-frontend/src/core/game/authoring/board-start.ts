/**
 * Where the story starts (setup.startAt, setup.twilight, setup.skirmishes, setup.controlled, setup.path): the Board
 * tab's "Where the story starts" as the draft says it, as it will be once what has been gathered is sent, what a
 * change to one of its parts gathers (and what it takes with it), and the setup it writes. Pure: every function
 * answers a new value and changes nothing it is given.
 */
import { AuthoringBoard, DraftSeat, DraftStart, DraftState } from './authoring-state';
import { GatheredBoard, StartChange } from './authoring-ui-store';
import { splitTerms } from './terms';

export type StartPart = keyof StartChange;
type Op = GatheredBoard['ops'][number];
type Bag = Readonly<Record<string, unknown>>;

/** The parts, in the order the gathered list says them (and the writer writes startAt's). */
export const START_PARTS: readonly StartPart[] = [
  'site',
  'opponentSite',
  'phase',
  'turn',
  'moves',
  'twilight',
  'skirmishes',
  'controlled',
  'path',
];
export const START_PHASES: readonly string[] = [
  'fellowship',
  'shadow',
  'maneuver',
  'archery',
  'assignment',
  'skirmish',
  'regroup',
];
/** The phases a start may have skirmishes already assigned in. */
export const SKIRMISH_PHASES: readonly string[] = ['assignment', 'skirmish'];
const SEATS: readonly DraftSeat[] = ['freeps', 'shadow'];

/** The start of a game: what every part is when the file says nothing. */
export const START_DEFAULT: DraftStart = {
  site: 1,
  opponentSite: 1,
  opponentSiteSet: false,
  phase: 'fellowship',
  turn: 1,
  twilight: 0,
  moves: 0,
  movesSet: false,
  late: false,
  skirmishes: {},
  controlled: {},
  path: [],
  markersTogether: false,
  phases: START_PHASES,
  lastSite: 9,
};

/** Where the draft's story starts (as the server says; the start of a game if it says nothing). */
export function startOf(state: DraftState): DraftStart {
  return state.start ?? START_DEFAULT;
}

/** The moves made this turn when the file does not say: none in the Fellowship phase (its move ends it), else one. */
export function defaultMoves(phase: string): number {
  return phase === 'fellowship' ? 0 : 1;
}

/** Where the story starts as it will be once what has been gathered is sent. */
export interface StartView {
  site: number;
  /** Where the Shadow seat's marker is (level with the fellowship when the markers keep together). */
  opponentSite: number;
  /** What the file is to say of it (null: nothing, site 1). */
  ownOpponentSite: number | null;
  phase: string;
  turn: number;
  twilight: number;
  moves: number;
  /** The moves are not the phase's default. */
  movesSet: boolean;
  skirmishes: Readonly<Record<string, readonly string[]>>;
  controlled: Partial<Record<DraftSeat, readonly string[]>>;
  path: readonly string[];
  markersTogether: boolean;
  phases: readonly string[];
  lastSite: number;
  /** Nothing earlier in the turn happens: the game is carried on from here. */
  late: boolean;
  /** The parts something gathered changes. */
  changed: ReadonlySet<StartPart>;
}

const int = (value: string | number | undefined) => {
  const read = parseInt(String(value), 10);
  return Number.isNaN(read) ? 0 : read;
};

/**
 * What a site of the setup is called by (a number, "2", or a name) as its number on the path: setup.path's place
 * first, then the seats' sites' places. Null for a name the scenario does not have.
 */
export function siteNumberer(board: AuthoringBoard | null, path: readonly string[]): (entry: string) => number | null {
  return (entry) => {
    if (/^\d+$/.test(entry.trim())) return parseInt(entry, 10);
    const at = path.indexOf(entry);
    if (at >= 0) return at + 1;
    for (const seat of SEATS) {
      const site = board?.sites[seat].find((one) => one.alias === entry);
      if (site) return site.number;
    }
    return null;
  };
}

/** The sites every marker is past (a site can be controlled from the start only then). */
export function behind(view: Pick<StartView, 'site' | 'opponentSite'>): number[] {
  const last = Math.min(view.site, view.opponentSite) - 1;
  return Array.from({ length: Math.max(0, last) }, (_, at) => at + 1);
}

/** The numbers of the sites a seat controls. */
export function controlledNumbers(
  view: Pick<StartView, 'controlled' | 'path'>,
  seat: DraftSeat,
  board: AuthoringBoard | null,
): Set<number> {
  const numberOf = siteNumberer(board, view.path);
  return new Set((view.controlled[seat] ?? []).map(numberOf).filter((number): number is number => number !== null));
}

/** The seat's controlled entries with the site of that number ticked or not (an entry as written is kept as it is). */
function withControl(
  controlled: Partial<Record<DraftSeat, readonly string[]>>,
  seat: DraftSeat,
  number: number,
  on: boolean,
  numberOf: (entry: string) => number | null,
): Partial<Record<DraftSeat, readonly string[]>> {
  const next: Partial<Record<DraftSeat, readonly string[]>> = {};
  for (const one of SEATS) {
    // (a site is controlled by one seat at most: ticked for one, it is no longer the other's)
    let entries = (controlled[one] ?? []).filter((entry) => numberOf(entry) !== number || (one === seat && on));
    if (one === seat && on && !entries.some((entry) => numberOf(entry) === number)) {
      entries = [...entries, String(number)];
    }
    if (entries.length > 0) next[one] = entries;
  }
  return next;
}

const isEmpty = (bag: object) => Object.keys(bag).length === 0;

/** Skirmishes without a character that has no minion. */
function fights(skirmishes: Readonly<Record<string, readonly string[]>>): Record<string, readonly string[]> {
  return Object.fromEntries(Object.entries(skirmishes).filter(([, minions]) => minions.length > 0));
}

/** The controlled sites without a seat that has none. */
function held(
  controlled: Partial<Record<DraftSeat, readonly string[]>>,
): Partial<Record<DraftSeat, readonly string[]>> {
  return Object.fromEntries(
    SEATS.filter((seat) => (controlled[seat] ?? []).length > 0).map((seat) => [seat, controlled[seat]]),
  );
}

/**
 * Where the story starts as it will be: the draft's start with the parts gathered, and (in the setup) the changes to
 * the board that go to it: a seat's marker, the twilight, a site controlled or freed.
 */
export function startView(
  start: DraftStart,
  gathered: Pick<GatheredBoard, 'ops' | 'start'>,
  inSetup: boolean,
  board: AuthoringBoard | null = null,
): StartView {
  const change = gathered.start ?? {};
  const changed = new Set<StartPart>(START_PARTS.filter((part) => change[part] !== undefined));
  const phase = change.phase ?? start.phase;
  let site = change.site ?? start.site;
  let ownOpponentSite =
    change.opponentSite !== undefined
      ? change.opponentSite
      : start.opponentSiteSet && start.opponentSite !== 1
        ? start.opponentSite
        : null;
  let twilight = change.twilight ?? start.twilight;
  const moves =
    change.moves !== undefined
      ? (change.moves ?? defaultMoves(phase))
      : start.movesSet
        ? start.moves
        : defaultMoves(phase);
  const path = change.path ?? start.path;
  let controlled = change.controlled ?? start.controlled;
  if (inSetup) {
    const numberOf = siteNumberer(board, path);
    for (const op of gathered.ops) {
      if (op.name === 'marker' && op.args[0] === 'freeps') {
        site = int(op.args[1]);
        changed.add('site');
      } else if (op.name === 'marker' && op.args[0] === 'shadow') {
        ownOpponentSite = int(op.args[1]);
        changed.add('opponentSite');
      } else if (op.name === 'twilight') {
        twilight = int(op.args[0]);
        changed.add('twilight');
      } else if (op.name === 'control' || op.name === 'liberate') {
        const number = numberOf(String(op.args[0]));
        if (number === null) continue;
        const seat = op.name === 'control' ? (op.args[1] as DraftSeat) : 'freeps';
        controlled = withControl(controlled, seat, number, op.name === 'control', numberOf);
        changed.add('controlled');
      }
    }
  }
  const skirmishes = fights(change.skirmishes ?? start.skirmishes);
  const turn = change.turn ?? start.turn;
  const movesSet = moves !== defaultMoves(phase);
  return {
    site,
    opponentSite: start.markersTogether ? site : (ownOpponentSite ?? 1),
    ownOpponentSite,
    phase,
    turn,
    twilight,
    moves,
    movesSet,
    skirmishes,
    controlled: held(controlled),
    path,
    markersTogether: start.markersTogether,
    phases: start.phases,
    lastSite: start.lastSite,
    late: site !== 1 || phase !== 'fellowship' || turn !== 1 || twilight !== 0 || movesSet || !isEmpty(skirmishes),
    changed,
  };
}

const same = (one: unknown, other: unknown) => JSON.stringify(one) === JSON.stringify(other);

/** The change without the parts that say what the draft says already. */
function trimmed(change: StartChange, start: DraftStart): StartChange {
  const next: Record<string, unknown> = { ...change };
  const base = startView(start, { ops: [], start: {} }, false);
  const phase = change.phase ?? start.phase;
  if (change.site === start.site) delete next['site'];
  if (change.opponentSite !== undefined) {
    const own = change.opponentSite === 1 ? null : change.opponentSite;
    if (own === base.ownOpponentSite) delete next['opponentSite'];
    else next['opponentSite'] = own;
  }
  if (change.phase === start.phase) delete next['phase'];
  if (change.turn === start.turn) delete next['turn'];
  if (change.twilight === start.twilight) delete next['twilight'];
  if (change.moves !== undefined) {
    const own = change.moves === defaultMoves(phase) ? null : change.moves;
    const was = start.movesSet ? start.moves : null;
    if (own === was || (own === null && !base.movesSet)) delete next['moves'];
    else next['moves'] = own;
  }
  if (change.skirmishes !== undefined) {
    const own = fights(change.skirmishes);
    if (same(own, base.skirmishes)) delete next['skirmishes'];
    else next['skirmishes'] = own;
  }
  if (change.controlled !== undefined) {
    const own = held(change.controlled);
    if (same(own, base.controlled)) delete next['controlled'];
    else next['controlled'] = own;
  }
  if (change.path?.join(',') === start.path.join(',')) delete next['path'];
  return next;
}

/** The changes to the board (in the setup) that say the same part as a part of the start. */
function sayingTheSame(op: Op, part: StartPart): boolean {
  if (part === 'site') return op.name === 'marker' && op.args[0] === 'freeps';
  if (part === 'opponentSite') return op.name === 'marker' && op.args[0] === 'shadow';
  if (part === 'twilight') return op.name === 'twilight';
  if (part === 'controlled') return op.name === 'control' || op.name === 'liberate';
  return false;
}

/** The part of the start a change to the board (in the setup) says, or null. */
export function startPartOf(name: string, args: readonly (string | number)[]): StartPart | null {
  return START_PARTS.find((part) => sayingTheSame({ name, args }, part)) ?? null;
}

/**
 * Gathers a part of where the story starts. What it takes with it is gathered too: skirmishes go when the start is
 * in no phase that has them, moves go back to the default in the Fellowship phase, and a site no longer behind both
 * markers is no longer controlled. In the setup, a change to the board gathered that says the same part goes (the
 * last one said is the one made). A part set to what the draft says is not gathered.
 */
export function gatherStart<K extends StartPart>(
  gathered: GatheredBoard,
  start: DraftStart,
  board: AuthoringBoard | null,
  part: K,
  value: StartChange[K],
  inSetup: boolean,
): GatheredBoard {
  const ops = inSetup ? gathered.ops.filter((op) => !sayingTheSame(op, part)) : gathered.ops;
  let change: StartChange = { ...gathered.start, [part]: value };
  let view = startView(start, { ops, start: change }, inSetup, board);
  if (!SKIRMISH_PHASES.includes(view.phase) && !isEmpty(view.skirmishes)) change = { ...change, skirmishes: {} };
  if (view.phase === 'fellowship' && view.movesSet) change = { ...change, moves: null };
  view = startView(start, { ops, start: change }, inSetup, board);
  const numberOf = siteNumberer(board, view.path);
  const past = behind(view);
  const freed: string[] = [];
  const controlled = Object.fromEntries(
    SEATS.map((seat) => {
      const kept = (view.controlled[seat] ?? []).filter((entry) => {
        const number = numberOf(entry);
        const stays = number === null || past.includes(number);
        if (!stays) freed.push(entry);
        return stays;
      });
      return [seat, kept];
    }),
  );
  const pruned = freed.length > 0;
  // (what the changes to the board said of the sites controlled is in what is gathered now)
  if (pruned) change = { ...change, controlled };
  const kept = pruned && inSetup ? ops.filter((op) => !sayingTheSame(op, 'controlled')) : ops;
  return withStart({ ...gathered, ops: kept }, trimmed(change, start));
}

/** A site ticked as controlled by a seat at the start, or not. */
export function gatherControl(
  gathered: GatheredBoard,
  start: DraftStart,
  board: AuthoringBoard | null,
  seat: DraftSeat,
  number: number,
  on: boolean,
  inSetup: boolean,
): GatheredBoard {
  const view = startView(start, gathered, inSetup, board);
  const numberOf = siteNumberer(board, view.path);
  return gatherStart(
    gathered,
    start,
    board,
    'controlled',
    withControl(view.controlled, seat, number, on, numberOf),
    inSetup,
  );
}

/** A minion put in a character's skirmish at the start (out of any other), or taken out of it. */
export function gatherFight(
  gathered: GatheredBoard,
  start: DraftStart,
  board: AuthoringBoard | null,
  character: string,
  minion: string,
  on: boolean,
): GatheredBoard {
  const view = startView(start, gathered, false, board);
  const skirmishes: Record<string, readonly string[]> = {};
  for (const [one, minions] of Object.entries(view.skirmishes))
    skirmishes[one] = minions.filter((each) => each !== minion);
  if (on) skirmishes[character] = [...(skirmishes[character] ?? []), minion];
  return gatherStart(gathered, start, board, 'skirmishes', skirmishes, false);
}

/** The site laid at a place on the path (`alias`; "" the Free Peoples seat's own). */
export function gatherPathSite(
  gathered: GatheredBoard,
  start: DraftStart,
  board: AuthoringBoard | null,
  number: number,
  alias: string,
): GatheredBoard {
  const view = startView(start, gathered, false, board);
  const own = (at: number) => board?.sites.freeps[at]?.alias ?? '';
  const path = Array.from({ length: Math.max(view.path.length, number) }, (_, at) => view.path[at] ?? own(at));
  path[number - 1] = alias === '' ? own(number - 1) : alias;
  // (what is the Free Peoples seat's own at the end is said by saying nothing)
  while (path.length > 0 && path.at(-1) === own(path.length - 1)) path.pop();
  return gatherStart(gathered, start, board, 'path', path, false);
}

function withStart(gathered: GatheredBoard, change: StartChange): GatheredBoard {
  const next = { ...gathered };
  if (isEmpty(change)) delete next.start;
  else next.start = change;
  return next;
}

/** A gathered part's ×: it is as the draft says. */
export function undoStart(gathered: GatheredBoard, part: StartPart): GatheredBoard {
  const change = Object.fromEntries(Object.entries(gathered.start ?? {}).filter(([name]) => name !== part));
  return withStart(gathered, change);
}

/** What has been gathered without where the story starts (it has been sent). */
export function withoutStart(gathered: GatheredBoard): GatheredBoard {
  const next = { ...gathered };
  delete next.start;
  return next;
}

/** The parts of the start gathered. */
export function startParts(gathered: Pick<GatheredBoard, 'start'>): StartPart[] {
  const change = gathered.start ?? {};
  return START_PARTS.filter((part) => change[part] !== undefined);
}

/** A card taken out (or not known any more) is in no skirmish gathered. */
export function startWithout(gathered: GatheredBoard, known: (alias: string) => boolean): GatheredBoard {
  const skirmishes = gathered.start?.skirmishes;
  if (skirmishes === undefined) return gathered;
  const kept: Record<string, readonly string[]> = {};
  for (const [character, minions] of Object.entries(skirmishes)) {
    if (known(character)) kept[character] = minions.filter(known);
  }
  if (same(kept, skirmishes)) return gathered;
  return withStart(gathered, { ...gathered.start, skirmishes: kept });
}

/** How the gathered list says a part of the start. */
export function startText(part: StartPart, change: StartChange): string {
  switch (part) {
    case 'site':
      return `start at site ${change.site}`;
    case 'opponentSite':
      return `Shadow marker at site ${change.opponentSite ?? 1}`;
    case 'phase':
      return `start in the ${change.phase} phase`;
    case 'turn':
      return `start on turn ${change.turn}`;
    case 'moves':
      return change.moves === null ? 'moves this turn: as usual' : `moves this turn: ${change.moves}`;
    case 'twilight':
      return `twilight ${change.twilight} at the start`;
    case 'skirmishes': {
      const fought = Object.entries(change.skirmishes ?? {});
      if (fought.length === 0) return 'no skirmishes at the start';
      return `skirmishes: ${fought.map(([character, minions]) => `${character}: ${minions.join(' + ')}`).join('; ')}`;
    }
    case 'controlled': {
      const seats = SEATS.filter((seat) => change.controlled?.[seat] !== undefined);
      if (seats.length === 0) return 'no sites controlled at the start';
      const sites = (seat: DraftSeat) => (change.controlled?.[seat] ?? []).join(', ');
      return `controlled: ${seats.map((seat) => `${seat} ${sites(seat)}`).join('; ')}`;
    }
    case 'path':
      return (change.path ?? []).length === 0
        ? "path: the Free Peoples' sites"
        : `path: ${(change.path ?? []).join(', ')}`;
  }
}

/** A part of the file that is a line of text, or nothing. */
const textOf = (value: unknown) => (typeof value === 'string' ? value : '');

/** A part of the file to change, as a map (its entries in the file's order). */
const mapOf = (bag: unknown) =>
  new Map<string, unknown>(bag !== null && typeof bag === 'object' ? Object.entries(bag as Bag) : []);

/**
 * The setup with where the story starts as gathered: startAt with the parts that are not their default (site,
 * opponentSite, phase, turn, moves, twilight); the twilight as setup.twilight when nothing else is said;
 * skirmishes as `character: a + b`, controlled as `seat: a, b`, path as one line. The setup as it is when nothing of
 * the start has been gathered.
 */
export function startSetup(setup: Bag | null, start: DraftStart, gathered: Pick<GatheredBoard, 'start'>): Bag | null {
  if (startParts(gathered).length === 0) return setup;
  const view = startView(start, { ops: [], start: gathered.start ?? {} }, false);
  const written = mapOf(setup);
  const startAt: Record<string, string | number> = {};
  if (view.site !== 1) startAt['site'] = view.site;
  if (view.ownOpponentSite !== null) startAt['opponentSite'] = view.ownOpponentSite;
  if (view.phase !== 'fellowship') startAt['phase'] = view.phase;
  if (view.turn !== 1) startAt['turn'] = view.turn;
  if (view.movesSet) startAt['moves'] = view.moves;
  written.delete('startAt');
  written.delete('twilight');
  if (!isEmpty(startAt)) {
    if (view.twilight !== 0) startAt['twilight'] = view.twilight;
    written.set('startAt', startAt);
  } else if (view.twilight !== 0) written.set('twilight', view.twilight);
  const put = (name: string, value: Bag | string) =>
    (typeof value === 'string' ? value === '' : isEmpty(value)) ? written.delete(name) : written.set(name, value);
  put(
    'skirmishes',
    Object.fromEntries(Object.entries(view.skirmishes).map(([character, minions]) => [character, minions.join(' + ')])),
  );
  put(
    'controlled',
    Object.fromEntries(
      SEATS.flatMap((seat) => {
        const sites = view.controlled[seat];
        return sites ? [[seat, sites.join(', ')]] : [];
      }),
    ),
  );
  put('path', view.path.join(', '));
  return written.size === 0 ? null : Object.fromEntries(written);
}

/** setup.skirmishes without a card taken out of the scenario (a character's skirmish goes with it). */
export function skirmishesWithout(skirmishes: unknown, alias: string): Bag | null {
  const kept = new Map<string, string>();
  for (const [character, minions] of mapOf(skirmishes)) {
    if (character === alias) continue;
    const left = String(minions)
      .split('+')
      .map((minion) => minion.trim())
      .filter((minion) => minion !== '' && minion !== alias);
    if (left.length > 0) kept.set(character, left.join(' + '));
  }
  return kept.size === 0 ? null : Object.fromEntries(kept);
}

/**
 * The setup without a site taken off a seat's path: out of the sites controlled, and setup.path laid only up to it
 * (what came after it said the places after a site that is not there). Null when no setup is left.
 */
export function setupWithoutSite(setup: Bag | null | undefined, alias: string): Bag | null {
  const written = mapOf(setup);
  const controlled = mapOf(written.get('controlled'));
  for (const [seat, sites] of controlled) {
    const left = splitTerms(String(sites)).filter((site) => site !== alias);
    if (left.length === 0) controlled.delete(seat);
    else controlled.set(seat, left.join(', '));
  }
  if (controlled.size === 0) written.delete('controlled');
  else written.set('controlled', Object.fromEntries(controlled));
  const path = splitTerms(textOf(written.get('path')));
  const at = path.indexOf(alias);
  if (at >= 0) {
    if (at === 0) written.delete('path');
    else written.set('path', path.slice(0, at).join(', '));
  }
  return written.size === 0 ? null : Object.fromEntries(written);
}

/** The cards the setup puts on the table that a skirmish at the start can have: the Free Peoples' characters (the Ring-bearer first) and the Shadow's minions. */
export interface FightCards {
  characters: readonly string[];
  minions: readonly string[];
}

/** Those cards: the setup's, with those gathered to be added there, without those gathered to be taken out. */
export function fightCards(
  draft: { setup?: Bag | undefined; ringBearer?: Partial<Record<DraftSeat, string>> },
  gathered: Pick<GatheredBoard, 'added' | 'removed'>,
): FightCards {
  const seat = (name: DraftSeat) => mapOf(mapOf(draft.setup).get(name));
  const listed = (name: DraftSeat, zone: string) => {
    const there = splitTerms(textOf(seat(name).get(zone)));
    for (const add of gathered.added) if (add.seat === name && add.zone === zone) there.push(add.alias);
    return there.filter((alias) => !gathered.removed.includes(alias));
  };
  const bearer = draft.ringBearer?.freeps;
  const characters = listed('freeps', 'characters');
  return {
    characters: [...new Set(bearer && !gathered.removed.includes(bearer) ? [bearer, ...characters] : characters)],
    minions: [...new Set(listed('shadow', 'minions'))],
  };
}
