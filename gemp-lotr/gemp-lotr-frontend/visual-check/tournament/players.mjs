// The players of the tournament check (see README.md). The first is the admin, whose browser runs the Tournament
// Admin page; the others only ever talk to the server over HTTP, the way the game check plays its opponent.
// No name is the beginning of another: the check finds a player's row on the admin page by the name.

export const ADMIN = 'TourneyAdmin';
/** Sign up for the scheduled tournaments, as invited. */
export const GUESTS = ['TourneyGuestOne', 'TourneyGuestTwo'];
/** Fill the player-made sealed queue and confirm its ready check. */
export const SEALED_PLAYERS = ['SealedAlpha', 'SealedBravo', 'SealedCharlie', 'SealedDelta'];
/** Signs up for the scheduled sealed tournament uninvited, and is taken out of its queue. */
export const QUEUE_CRASHER = 'CrasherInQueue';
/** Joins the scheduled sealed tournament late, uninvited, and is dropped from it. */
export const LATE_CRASHER = 'CrasherComingLate';
/** Waits alone in a player-made queue while an admin sets another tournament's stage. */
export const WAITER = 'WaitingPlayer';

/** Every player, for the database's first start: 'a' is an admin, 'u' a user (Player.Type). */
export const TOURNAMENT_PLAYERS = [
  { name: ADMIN, type: 'au' },
  ...[...GUESTS, ...SEALED_PLAYERS, QUEUE_CRASHER, LATE_CRASHER, WAITER].map((name) => ({ name, type: 'u' })),
];

/** The players who join something that takes a deck. */
export const DECK_PLAYERS = [...GUESTS, WAITER];
