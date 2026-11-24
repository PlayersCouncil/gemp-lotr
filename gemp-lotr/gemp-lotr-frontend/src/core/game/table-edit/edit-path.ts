/**
 * The adventure path changed by hand (Ketura: "The site path + icon should include an option for 'From Adventure
 * Deck…' where you pick the corresponding deck from either player and get a picker"): the next site not yet laid
 * (the path's open spot), the search a site is found by, the sites of an adventure deck to pick from, the `site`
 * edits, and a laid site's menu (each seat's marker, control, liberate, Replace…). Pure.
 */
import { SearchBound } from './card-search';
import { EditBoard, EditCard, EditPathSite } from './edit-board';
import { EditOp, EditSeat, EditTool } from './edit-ops';
import { bothSeats, EditSeats } from './edit-seats';

/** The highest site number. */
export const LAST_SITE = 9;

/** The lowest site number not laid on the path, or null when every one is. */
export function nextSite(board: EditBoard | null): number | null {
  const laid = new Set((board?.path ?? []).map((site) => site.number));
  for (let number = 1; number <= LAST_SITE; number++) if (!laid.has(number)) return number;
  return null;
}

/** The site laid at a number, or null. */
export function laidSite(board: EditBoard | null, number: number): EditPathSite | null {
  return board?.path?.find((site) => site.number === number) ?? null;
}

/** Whether the game's sites have numbers printed on them (an unnumbered site says 0): a site's search is bound by it. */
export function sitesNumbered(board: EditBoard | null): boolean {
  return (board?.cards ?? []).some((card) => card.type.toUpperCase() === 'SITE' && (card.printed ?? 0) > 0);
}

/** The search for a site at a number: sites of that number where the game's sites have numbers, else every site. */
export function siteBound(number: number, numbered: boolean): SearchBound {
  return numbered
    ? { filter: `cardType:SITE siteNumber:${number}`, label: `Site ${number}` }
    : { filter: 'cardType:SITE', label: 'Sites' };
}

/**
 * A seat's adventure deck as the board lists it, in printed number then name order; null where the board does not
 * list it (the other seat's, without the `piles` tool).
 */
export function deckSites(board: EditBoard | null, seat: EditSeat, human: EditSeat): EditCard[] | null {
  if (!board || (!board.hiddenPilesOpen && seat !== human)) return null;
  return board.cards
    .filter((card) => card.zone === 'adventureDeck' && card.owner === seat && card.type.toUpperCase() === 'SITE')
    .sort((a, b) => (a.printed ?? 0) - (b.printed ?? 0) || a.name.localeCompare(b.name));
}

/** Lays a site of an adventure deck (by its name) at the number. */
export function siteFromDeck(number: number, card: EditCard): EditOp {
  return { op: 'site', number, card: card.alias };
}

/** Lays the seat's site of that printed number, from its adventure deck (a deck the page is not shown). */
export function siteFromSeat(number: number, seat: EditSeat): EditOp {
  return { op: 'site', number, from: seat };
}

/** Lays a new site card of the blueprint (the person's) at the number. */
export function siteOfBlueprint(number: number, blueprint: string): EditOp {
  return { op: 'site', number, blueprint };
}

/** A seat in a site's menu: its player's name, and whether it is the seat's already (its marker, its control). */
export interface SiteSeat {
  seat: EditSeat;
  label: string;
  here: boolean;
}

/** A laid site's menu: each part empty (or false) where not offered. */
export interface SiteMenu {
  number: number;
  /** Put a seat's marker here (the `markers` tool). */
  markers: readonly SiteSeat[];
  /** Give the site to a seat (the `markers` tool; the server says when a marker is not past it). */
  control: readonly SiteSeat[];
  /** Take the control away: a controlled site. */
  liberate: boolean;
  /** Another site here (the `sites` tool; the server refuses a controlled one). */
  replace: boolean;
}

/**
 * The menu of a site on the path (or controlled: it lies in its controller's support area then, still numbered);
 * null for any other card. Seats are named as `names` says (the players' names on the page).
 */
export function siteMenu(
  board: EditBoard,
  card: EditCard,
  may: (tool: EditTool) => boolean,
  seats: EditSeats,
  names: Readonly<Record<EditSeat, string>>,
): SiteMenu | null {
  const number = card.site ?? null;
  if (card.type.toUpperCase() !== 'SITE' || number === null) return null;
  const markers = may('markers');
  const controlledBy = laidSite(board, number)?.controlledBy ?? null;
  return {
    number,
    markers: markers
      ? bothSeats(seats).map((seat) => ({ seat, label: names[seat], here: board.markers?.[seat] === number }))
      : [],
    control: markers ? bothSeats(seats).map((seat) => ({ seat, label: names[seat], here: controlledBy === seat })) : [],
    liberate: markers && controlledBy !== null,
    replace: may('sites'),
  };
}

/** Whether a site's menu has anything to edit. */
export function siteMenuHasEdits(menu: SiteMenu): boolean {
  return menu.markers.length > 0 || menu.control.length > 0 || menu.liberate || menu.replace;
}
