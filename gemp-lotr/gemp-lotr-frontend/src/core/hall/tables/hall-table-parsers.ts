import { parseXml } from '../../api/xml';
import { gamePageUrl } from '../../platform/app-paths';
import { HallItemChange } from '../hall-models';
import { formatServerTime } from '../hall-parsers';
import { RESULT_OK, SERVER_REFUSED } from './hall-tables-messages';
import { HallTable, HallTableAction, HallTimer, TableCallResult } from './hall-table-models';

/** `value`, or `fallback` when the server sent none or an empty one. */
function orElse<T>(value: string | null | undefined, fallback: T): string | T {
  return value === null || value === undefined || value === '' ? fallback : value;
}

/** The game page (hallUi.js: ng/game?gameId=). */
export function gameUrl(gameId: string): string {
  return gamePageUrl(gameId);
}

/**
 * A table's add or update, typed (hallUi.js drawTable, the mobile hallState.js readTable). An update carries every
 * attribute again (HallCommunicationChannel sends the whole map when anything changed), so it replaces the table.
 */
export function parseHallTable(change: HallItemChange): HallTable {
  const a = change.attributes;
  const flag = (name: string) => a[name] === 'true';
  const players = a['players'] ?? '';
  const createdAt = parseInt(a['createdAt'] ?? '', 10);
  return {
    id: change.id,
    gameId: orElse(a['gameId'], null),
    status: a['status'] ?? '',
    statusDescription: a['statusDescription'] ?? '',
    format: a['format'] ?? '',
    tournament: a['tournament'] ?? '',
    description: a['userDescription'] ?? '',
    players: players.length > 0 ? players.split(',') : [],
    playing: flag('playing'),
    watchable: flag('watchable'),
    isPrivate: flag('isPrivate'),
    isInviteOnly: flag('isInviteOnly'),
    invitedYou: flag('invitedYou'),
    winner: a['winner'] ?? null,
    createdAt: Number.isNaN(createdAt) ? null : createdAt,
  };
}

/**
 * The row's button: Leave Table at the viewer's own waiting table, Join Table at another (unless invite-only for
 * someone else), Play Match at the viewer's game, Spectate at a watchable one.
 */
export function tableAction(table: HallTable): HallTableAction | null {
  if (table.status === 'WAITING') {
    if (table.playing) {
      return 'leave';
    }
    return !table.isInviteOnly || table.invitedYou ? 'join' : null;
  }
  if (table.status === 'PLAYING' && table.gameId !== null) {
    if (table.playing) {
      return 'play';
    }
    return table.watchable ? 'watch' : null;
  }
  return null;
}

/** The note after the tournament name, as the legacy hall wrote it. */
export function tableNote(table: HallTable): string | null {
  const description = table.description;
  if (description) {
    if (table.isInviteOnly) {
      return `${table.isPrivate ? 'Private match for user' : 'Match for user'} '${description}'.`;
    }
    return table.isPrivate ? `Private match: [${description}]` : `[${description}]`;
  }
  return table.isPrivate ? 'Private.' : null;
}

/** How long a table has waited: "00:07", "01:30", "59:59", then "1:00:00", "26:05:09" (GempLotrHallUI.formatAge). */
export function formatTableAge(ageMs: number): string {
  const total = Math.floor(Math.max(0, ageMs) / 1000);
  const two = (n: number) => String(n).padStart(2, '0');
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor(total / 60) % 60;
  const seconds = total % 60;
  return `${hours > 0 ? `${hours}:${two(minutes)}` : two(minutes)}:${two(seconds)}`;
}

/** The waiting timer's tooltip: "Open since 2026-11-09 14:01:40 (server time)". */
export function tableAgeTitle(createdAt: number): string {
  return `Open since ${formatServerTime(createdAt)} (server time)`;
}

/**
 * What a table call answered on HTTP 200 (TableFlow.readResponse): <error message> is a refusal; <response message>,
 * <result>OK</result>, "OK", an empty body or anything else is a success.
 */
export function parseTableCallResult(body: string): TableCallResult {
  const text = body.trim();
  if (text === '' || text === 'OK') {
    return { ok: true, message: RESULT_OK };
  }
  let doc: Document;
  try {
    doc = parseXml(text);
  } catch {
    return { ok: true, message: text };
  }
  const root = doc.documentElement;
  if (root.tagName === 'error') {
    return { ok: false, message: orElse(root.getAttribute('message'), SERVER_REFUSED) };
  }
  if (root.tagName === 'response') {
    return { ok: true, message: orElse(root.getAttribute('message'), RESULT_OK) };
  }
  return { ok: true, message: RESULT_OK };
}

/** The server's timers (JSONDefs.TimerInfo); entries without a code are left out. */
export function parseHallTimers(value: unknown): HallTimer[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value.flatMap((entry: Record<string, unknown> | null) => {
    if (entry === null || typeof entry !== 'object' || typeof entry['code'] !== 'string') {
      return [];
    }
    return [
      {
        code: entry['code'],
        name: typeof entry['name'] === 'string' ? entry['name'] : entry['code'],
        minutesPerPlayer: Number(entry['minutesPerPlayer']) || 0,
        minutesPerDecision: Number(entry['minutesPerDecision']) || 0,
        description: typeof entry['description'] === 'string' ? entry['description'] : null,
      },
    ];
  });
}

/** The timers of the page itself, when the server sends none (hall.html #unranked-timer). */
export const FALLBACK_TIMERS: readonly HallTimer[] = [
  { code: 'default', name: 'Default', minutesPerPlayer: 45, minutesPerDecision: 6, description: null },
  { code: 'blitz', name: 'Blitz!', minutesPerPlayer: 25, minutesPerDecision: 3, description: null },
  { code: 'WC', name: 'Championship', minutesPerPlayer: 20, minutesPerDecision: 10, description: null },
  { code: 'slow', name: 'Slow', minutesPerPlayer: 80, minutesPerDecision: 10, description: null },
  { code: 'glacial', name: 'Glacial', minutesPerPlayer: 1440, minutesPerDecision: 1440, description: null },
];

/** "Default (45m/6m)", "Glacial (1d/1d)"; the server's "WC" shows as "Championship" (FormatManager.timerLabel). */
export function timerLabel(timer: HallTimer): string {
  const name = timer.name === 'WC' ? 'Championship' : timer.name;
  return `${name} (${minutes(timer.minutesPerPlayer)}/${minutes(timer.minutesPerDecision)})`;
}

function minutes(total: number): string {
  if (total >= 1440 && total % 1440 === 0) {
    return `${total / 1440}d`;
  }
  if (total > 60 && total % 60 === 0) {
    return `${total / 60}h`;
  }
  return `${total}m`;
}

/** The chat user list's names, bare: the older "* name" / "+ name" prefixes go (CreateUnrankedTable.bareName). */
export function bareName(name: string | null | undefined): string {
  return (name ?? '').replace(/^[*+] /, '').trim();
}

export function sameName(a: string, b: string): boolean {
  return a.toLowerCase() === b.toLowerCase();
}
