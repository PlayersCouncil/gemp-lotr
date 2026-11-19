import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { GameEvent, GameUpdate } from '../protocol/game-events';
import { parseGameUpdate, parseReplay } from '../protocol/game-xml';

function update(doc: Document): GameUpdate {
  const parsed = parseGameUpdate(doc);
  if (!parsed) throw new ApiError(INVALID_RESPONSE_STATUS, 'The game server answered something else than a game');
  return parsed;
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
