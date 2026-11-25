/**
 * The scenario's two seats (`freeps`, `shadow`, as the edits name them) and the players the page knows: the person
 * plays their seat (the scenario's `humanSeat`) under their own name (the page's `participantId`), the other seat is
 * the other player of the game. Pure.
 */
import { EditSeat, otherSeat } from './edit-ops';

export interface EditSeats {
  /** The person's seat. */
  human: EditSeat;
  /** The player of each seat, as the page names players (an empty name: not known yet). */
  players: Readonly<Record<EditSeat, string>>;
}

/**
 * The seats of a game: the person's from the scenario, the other player the game's other one. The page's own player
 * (`participantId`) sits in the seat the page looks from (`looked`: the person's own, unless the table is flipped, when
 * the page is the other seat's player's).
 */
export function editSeats(
  model: { participantId: string | null; players: readonly string[] },
  humanSeat: EditSeat,
  looked: EditSeat = humanSeat,
): EditSeats {
  const own = model.participantId ?? '';
  const other = model.players.find((player) => player !== own) ?? '';
  const players = { [looked]: own, [otherSeat(looked)]: other } as Record<EditSeat, string>;
  return { human: humanSeat, players };
}

/** The seat a player of the page plays, or null for a name that is neither. */
export function seatOf(seats: EditSeats, player: string): EditSeat | null {
  if (player === '') return null;
  if (seats.players.freeps === player) return 'freeps';
  return seats.players.shadow === player ? 'shadow' : null;
}

/** Both seats, the person's first. */
export function bothSeats(seats: EditSeats): readonly EditSeat[] {
  return [seats.human, otherSeat(seats.human)];
}
