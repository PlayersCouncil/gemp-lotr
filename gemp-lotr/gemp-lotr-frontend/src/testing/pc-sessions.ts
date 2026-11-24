import { readServerResponse } from './files';

/** GET /argonauth/session for a PC account with `count` aliases (Alias1, Alias2, ...), none played; `extra` changes it. */
export function sessionWithAliases(count: number, extra: Record<string, unknown> = {}): string {
  const base = JSON.parse(readServerResponse('argonauth-session-aliases.json')) as Record<string, unknown>;
  const aliases = Array.from({ length: count }, (_, i) => ({
    id: 900001 + i,
    name: `Alias${i + 1}`,
    current: false,
    busy: false,
  }));
  return JSON.stringify({ ...base, aliases, ...extra });
}
