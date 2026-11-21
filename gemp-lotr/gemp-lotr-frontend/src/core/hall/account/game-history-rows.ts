import { replayUrl } from '../../game/session/game-ending';
import { GameHistoryEntry } from './account-models';
import { CASUAL_EVENT_CELL, endedTitle, NO_VALUE, RESULT_TITLES, winnerReasonTitle } from './account-messages';
import { firstText } from './first-text';

/** One row of the history table, as shown (GameHistoryUI.renderRows). */
export interface GameHistoryRow {
  /** The day the game ended, yyyy-MM-dd in server time. */
  readonly date: string;
  /** "Ended <server time> server time (UTC); <local time> your time"; null without an end. */
  readonly dateTitle: string | null;
  readonly length: string;
  readonly result: 'W' | 'L';
  readonly resultTitle: string;
  /** The loser's reason (it names the timeout, concession, corruption …), or "—". */
  readonly reason: string;
  /** "Winner: <reason>", or null. */
  readonly reasonTitle: string | null;
  readonly opponent: string;
  readonly format: string;
  /** The league or tournament, or "Casual". */
  readonly event: string;
  readonly deck: string;
  /** The Angular game page's replay address (new tab); null: "not stored". */
  readonly replayUrl: string | null;
}

function two(n: number): string {
  return `${n < 10 ? '0' : ''}${n}`;
}

/** commonUi.js formatTime(ms): "yyyy-MM-dd HH:mm" in the viewer's own time zone; "" for a missing or bad value. */
export function formatLocalTime(epochMs: number | null): string {
  if (epochMs === null || Number.isNaN(epochMs)) {
    return '';
  }
  const date = new Date(epochMs);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return (
    `${date.getFullYear()}-${two(date.getMonth() + 1)}-${two(date.getDate())} ` +
    `${two(date.getHours())}:${two(date.getMinutes())}`
  );
}

/** commonUi.js formatDuration: "45 s", "12 min" or "1 h 05 min"; "" for a missing or negative length. */
export function formatDuration(ms: number | null): string {
  if (ms === null || Number.isNaN(ms) || ms < 0) {
    return '';
  }
  const seconds = Math.round(ms / 1000);
  if (seconds < 60) {
    return `${seconds} s`;
  }
  const minutes = Math.round(seconds / 60);
  if (minutes < 60) {
    return `${minutes} min`;
  }
  return `${Math.floor(minutes / 60)} h ${two(minutes % 60)} min`;
}

/** GameHistoryUI.serverDate: the end's day in UTC from its epoch ms, else the start of its endTime text. */
export function serverDate(entry: GameHistoryEntry): string {
  if (entry.endMs !== null) {
    const date = new Date(entry.endMs);
    if (!Number.isNaN(date.getTime())) {
      return date.toISOString().substring(0, 10);
    }
  }
  return entry.endTime ? entry.endTime.substring(0, 10) : '';
}

/** A history entry as its row shows it; `playerId` makes the replay's id ("player$recording"). */
export function gameHistoryRow(entry: GameHistoryEntry, playerId: string): GameHistoryRow {
  return {
    date: serverDate(entry),
    dateTitle: entry.endMs !== null ? endedTitle(entry.endTime, formatLocalTime(entry.endMs)) : null,
    length: formatDuration(entry.durationMs),
    result: entry.result,
    resultTitle: RESULT_TITLES[entry.result],
    reason: firstText(entry.loseReason, NO_VALUE),
    reasonTitle: entry.winReason ? winnerReasonTitle(entry.winReason) : null,
    opponent: entry.opponent ?? '',
    format: entry.format ?? '',
    event: firstText(entry.event, CASUAL_EVENT_CELL),
    deck: firstText(entry.deck, NO_VALUE),
    replayUrl: entry.recordingId ? replayUrl('', `${playerId}$${entry.recordingId}`) : null,
  };
}
