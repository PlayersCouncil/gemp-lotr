import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { GameEvent, GameUpdate } from '../protocol/game-events';
import { parseGameUpdate, parseReplay } from '../protocol/game-xml';
import {
  GameNotices,
  readGameNotices,
  readRestorePoints,
  readUndoReply,
  RestorePoints,
  UndoAnswer,
  UndoReply,
} from '../undo/undo-protocol';

function update(doc: Document): GameUpdate {
  const parsed = parseGameUpdate(doc);
  if (!parsed) throw new ApiError(INVALID_RESPONSE_STATUS, 'The game server answered something else than a game');
  return parsed;
}

/** The newest game in the player's history (GET /gameHistory), as far as the game's end needs it. */
export interface LatestGame {
  opponent: string;
  /** The player's own recording of it, or null when none was kept. */
  recordingId: string | null;
  /** When it ended (epoch ms), or null. */
  endMs: number | null;
}

/** The first `historyEntry` of a game history, or null when there is none. */
export function parseLatestGame(doc: Document): LatestGame | null {
  const entry = doc.getElementsByTagName('historyEntry')[0];
  if (!entry) return null;
  const end = Number(entry.getAttribute('endMs'));
  return {
    opponent: entry.getAttribute('opponent') ?? '',
    recordingId: entry.getAttribute('gameRecordingId'),
    endMs: entry.hasAttribute('endMs') && Number.isFinite(end) ? end : null,
  };
}

/**
 * Every server call the game page makes, typed (the legacy communication.js's game calls). The participant, the
 * cache buster and ApiError come from GempHttp's interceptors. Statuses the page reacts to: 0 (no connection), 401
 * (not logged in), 403 (a private game), 409 (the game open elsewhere), 410 (removed for inactivity).
 */
@Injectable()
export class GameApi {
  private readonly http = inject(GempHttp);

  /** startGameSession: the whole game as the player may see it, and the decision waiting for them. */
  open(gameId: string): Observable<GameUpdate> {
    return this.http.getXml(`/game/${encodeURIComponent(gameId)}`).pipe(map(update));
  }

  /** updateGameState: what happened since the channel's last answer; the server holds it until something happens. */
  poll(gameId: string, channel: string): Observable<GameUpdate> {
    return this.http.postXml(`/game/${encodeURIComponent(gameId)}`, { channelNumber: channel }).pipe(map(update));
  }

  /** gameDecisionMade: the player's answer, and what happened after it. */
  answer(gameId: string, channel: string, decisionId: string, value: string): Observable<GameUpdate> {
    return this.http
      .postXml(`/game/${encodeURIComponent(gameId)}`, { channelNumber: channel, decisionId, decisionValue: value })
      .pipe(map(update));
  }

  /** getGameCardModifiers: what affects a card, as the server writes it (HTML). */
  cardInfo(gameId: string, cardId: string): Observable<string> {
    return this.http.getText(`/game/${encodeURIComponent(gameId)}/cardInfo`, { cardId });
  }

  concede(gameId: string): Observable<void> {
    return this.http.postText(`/game/${encodeURIComponent(gameId)}/concede`).pipe(map(() => undefined));
  }

  /** Asks the opponent to cancel the game (it ends only if they ask too). */
  cancel(gameId: string): Observable<void> {
    return this.http.postText(`/game/${encodeURIComponent(gameId)}/cancel`).pipe(map(() => undefined));
  }

  /** The player's newest game in their history: where the replay of the game that just ended is found. */
  latestGame(): Observable<LatestGame | null> {
    return this.http.getXml('/gameHistory', { start: '0', count: '1' }).pipe(map(parseLatestGame));
  }

  /** Where the player may take the game back to, and how things stand with requests (undo.js getRestorePoints). */
  restorePoints(gameId: string): Observable<RestorePoints> {
    return this.http.getJson<unknown>(`/game/${encodeURIComponent(gameId)}/restorePoints`).pipe(map(readRestorePoints));
  }

  /**
   * Asks for the game to be taken back to a place (its entryCount; null for the player's last choice). `ownOnly`: the
   * Undo button's, refused rather than asked of the opponent if it is no longer the player's own business.
   */
  undo(gameId: string, entryCount: number | null, ownOnly: boolean): Observable<UndoReply> {
    return this.http
      .postJson<unknown>(`/game/${encodeURIComponent(gameId)}/undo`, {
        entryCount: entryCount === null ? '' : String(entryCount),
        ownOnly: String(ownOnly),
      })
      .pipe(map(readUndoReply));
  }

  /** Answers a request: approve, decline, declineAll (this and every later one), or cancel (the asker withdraws). */
  undoReply(gameId: string, requestId: number, reply: UndoAnswer): Observable<UndoReply> {
    return this.http
      .postJson<unknown>(`/game/${encodeURIComponent(gameId)}/undoReply`, { requestId: String(requestId), reply })
      .pipe(map(readUndoReply));
  }

  /** Declines every request in this game without asking the player (or stops doing so). */
  autoDecline(gameId: string, on: boolean): Observable<void> {
    return this.http
      .postJson<unknown>(`/game/${encodeURIComponent(gameId)}/autoDecline`, { on: String(on) })
      .pipe(map(() => undefined));
  }

  /**
   * The game's notices after number `after`: a long poll of its own, apart from the game's, so that a request or the
   * game's end reaches a player who is looking at a decision. `after` -1 answers at once with where to start from.
   */
  notices(gameId: string, after: number): Observable<GameNotices> {
    return this.http
      .postJson<unknown>(`/game/${encodeURIComponent(gameId)}/notices`, { after: String(after) })
      .pipe(map(readGameNotices));
  }

  /** getReplay: every event of a finished game. replayId is "player$recording", sent as it is. */
  replay(replayId: string): Observable<readonly GameEvent[]> {
    return this.http.getXml(`/replay/${replayId}`).pipe(
      map((doc) => {
        const events = parseReplay(doc);
        if (!events) throw new ApiError(INVALID_RESPONSE_STATUS, 'The server answered something else than a replay');
        return events;
      }),
    );
  }
}
