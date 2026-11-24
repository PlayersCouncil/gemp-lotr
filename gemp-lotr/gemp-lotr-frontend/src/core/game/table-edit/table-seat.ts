/**
 * Flip table and who plays the other seat (Ketura: "a bot mode, and a 'flip table' mode that presents the table from
 * the other side for the author to control"), as the scenario's info event says them (ef-notes Part 2 E): the seat
 * this page looks from (`seat`), who plays the seat the person does not sit in (`otherSeat`: the script, Gemp's bot,
 * or the person), the choices on offer, the seat the game waits on, and whether the table can be flipped. Pure.
 *
 * Two things are kept apart, as the server keeps them: who answers the other seat's decisions (a setting of the
 * sandbox) and which seat one page shows (the flip). A flipped page is that seat's player's page as far as the game can
 * tell (its first event names that player), so the table is drawn from there by itself; what this file adds is what
 * the page must know beyond that: whether what it is asked is the person's to answer, and where the person's attention
 * is wanted when the game waits on the seat they are not looking from.
 */
import { EditSeat, otherSeat } from './edit-ops';

/** Who plays the seat the person does not sit in. */
export type OtherSeat = 'script' | 'bot' | 'person';
export const OTHER_SEATS: readonly OtherSeat[] = ['script', 'bot', 'person'];

/** Each choice in a word (the edit bar's select). */
export const OTHER_SEAT_LABELS: Readonly<Record<OtherSeat, string>> = { script: 'Script', bot: 'Bot', person: 'Me' };
/** Each choice in a sentence (its title). */
export const OTHER_SEAT_TITLES: Readonly<Record<OtherSeat, string>> = {
  script: "The scenario's script plays it, passing wherever it says nothing",
  bot: "Gemp's bot plays it (a weak player)",
  person: 'You play it: flip the table to answer for it',
};

/** A seat in a word or two. */
export const SEAT_NAMES: Readonly<Record<EditSeat, string>> = { freeps: 'Free Peoples', shadow: 'Shadow' };

/** What of the scenario's info this needs (ScenarioInfo; the new fields optional, as older literals lack them). */
export interface SeatInfo {
  humanSeat: EditSeat;
  mode?: 'play' | 'edit';
  author?: boolean;
  seat?: EditSeat;
  otherSeat?: OtherSeat | null;
  otherSeats?: readonly OtherSeat[];
  waitingOn?: EditSeat | null;
  canFlip?: boolean;
}

/** A choice of who plays the other seat, from the server's word (any case), or null. */
export function readOtherSeat(value: unknown): OtherSeat | null {
  const word = typeof value === 'string' ? value.toLowerCase() : null;
  return OTHER_SEATS.find((seat) => seat === word) ?? null;
}

/** The choices on offer, in the server's order, each once. */
export function readOtherSeats(value: unknown): OtherSeat[] {
  if (!Array.isArray(value)) return [];
  const seats = value.map(readOtherSeat).filter((seat): seat is OtherSeat => seat !== null);
  return [...new Set(seats)];
}

/** A seat from the server's word, or null. */
export function readSeat(value: unknown): EditSeat | null {
  return value === 'freeps' || value === 'shadow' ? value : null;
}

/** The seat the page shows the table from: the person's own unless flipped. */
export function lookedFrom(info: SeatInfo | null): EditSeat {
  return info?.seat ?? info?.humanSeat ?? 'freeps';
}

/** Whether the page shows the table from the seat the person does not sit in. */
export function isFlipped(info: SeatInfo | null): boolean {
  return info !== null && lookedFrom(info) !== info.humanSeat;
}

/**
 * Whether what this page is asked is the person's to answer. Unflipped, always. Flipped onto a seat the script or the
 * bot plays, never: the page shows the game as that seat sees it, and the server would refuse the answer (the script
 * or the bot would race it). An author's table (no `otherSeat`) takes the decisions the tool has left for the author,
 * which the authoring panel knows (its own gate).
 */
export function answersHere(info: SeatInfo | null): boolean {
  if (!info || !isFlipped(info)) return true;
  return info.otherSeat === 'person' || info.otherSeat === null || info.otherSeat === undefined;
}

/**
 * The seat the game waits on while this page looks from the other one, when the person plays it (their own seat
 * always; the other one under `person`): the page says so and offers the flip. Null otherwise, in edit mode (nobody
 * is asked anything) and at an author's table (the authoring panel says who is asked; the server does not keep the
 * info's `waitingOn` current there).
 */
export function waitingElsewhere(info: SeatInfo | null): EditSeat | null {
  if (!info?.canFlip || info.author || info.mode === 'edit') return null;
  const waiting = info.waitingOn ?? null;
  if (waiting === null || waiting === lookedFrom(info)) return null;
  return waiting === info.humanSeat || info.otherSeat === 'person' ? waiting : null;
}

/** What the page says where the decision would be: "Shadow to choose". */
export function waitingText(seat: EditSeat): string {
  return `${SEAT_NAMES[seat]} to choose`;
}

/** The Flip button's title: where it turns the table to. */
export function flipTitle(info: SeatInfo | null): string {
  const to = otherSeat(lookedFrom(info));
  return to === info?.humanSeat ? `Back to your side (${SEAT_NAMES[to]})` : `Look from ${SEAT_NAMES[to]}'s side`;
}

/** The edit bar's word for the seat looked from, when flipped ("As Shadow"); null unflipped. */
export function lookingText(info: SeatInfo | null): string | null {
  return isFlipped(info) ? `As ${SEAT_NAMES[lookedFrom(info)]}` : null;
}
