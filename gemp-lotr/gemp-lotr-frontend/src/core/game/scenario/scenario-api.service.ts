import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';

/** A scenario's game started. */
export interface StartedScenario {
  gameId: string;
}

/** The server calls of a scripted game's page that aren't the game's own (see GameApi for those). */
@Injectable()
export class ScenarioApi {
  private readonly http = inject(GempHttp);

  /**
   * A step that waits to be read has been read (an opponent's, which then acts; or one put before the player's own
   * decision, which is then asked). `choice`: the button pressed, if the step ended in a choice.
   */
  acknowledge(gameId: string, step: number, choice: string | null): Observable<void> {
    return this.http
      .postText(`/game/${encodeURIComponent(gameId)}/scenarioAck`, { step: String(step), choice: choice ?? '' })
      .pipe(map(() => undefined));
  }

  /** Starts a game of a scenario (ending the one the player has, if any); from its last checkpoint if asked. */
  start(id: string, fromCheckpoint = false, picks: 'same' | null = null): Observable<StartedScenario> {
    return this.http
      .postJson<{ gameId?: unknown }>('/scenarios/start', {
        id,
        ...(fromCheckpoint ? { from: 'checkpoint' } : {}),
        // (a puzzle's game again with the cards of the last one: `picks=same`)
        ...(picks ? { picks } : {}),
      })
      .pipe(map((answer) => ({ gameId: String(answer.gameId) })));
  }
}
