/**
 * The logged-in player, as GET /player tells it (PlayerInfoRequestHandler: Player.GetUserInfo, serialized as
 * `{"hasTrophies": bool, "name": string, "type": string}`). The type is a string of one-letter flags (Player.Type).
 */
export interface Player {
  readonly name: string;
  /** The flags as sent, e.g. "un" (a player) or "au" (an admin). */
  readonly type: string;
  readonly hasTrophies: boolean;
}

/** The letters of Player.Type on the server. */
export const PlayerType = {
  admin: 'a',
  leagueAdmin: 'l',
  playTester: 'p',
  commentator: 'c',
  unbanned: 'n',
  user: 'u',
  bot: 'b',
} as const;
export type PlayerType = (typeof PlayerType)[keyof typeof PlayerType];

/** Whether the player has the flag (Player.hasType). */
export function hasPlayerType(player: Player | null, type: PlayerType): boolean {
  return player?.type.includes(type) ?? false;
}

/**
 * Reads the answer of GET /player, or null when it isn't a player (no name). Like the legacy hall, which reads
 * `type` and `name` from it, missing optional fields get defaults.
 */
export function parsePlayer(json: unknown): Player | null {
  if (typeof json !== 'object' || json === null) {
    return null;
  }
  const { name, type, hasTrophies } = json as Record<string, unknown>;
  if (typeof name !== 'string' || name === '') {
    return null;
  }
  return { name, type: typeof type === 'string' ? type : '', hasTrophies: hasTrophies === true };
}
