import { inject, Injectable } from '@angular/core';
import { SessionStore } from '../../platform/session-store';
import { Decision } from '../protocol/game-events';
import { GameModel, LocalChoices } from '../state/game-model';
import { choicesFit } from '../state/local-choices';

/**
 * What only the page holds about a game, kept so that a reload (or coming back in the same tab) shows the game exactly
 * as it was left (D10 of trymarz/gemp-lotr-private#27). The game itself comes back from the server (GET /game/{id}
 * sends all of it and the waiting decision); this keeps the rest: the player's choices not sent yet, and the view's own
 * state (open dialogs, tab, order of the hand…), which the core keeps without reading it.
 */
export interface SavedGame {
  /** The waiting decision the choices belong to, or null. */
  decision: string | null;
  choices: LocalChoices;
  /** The view's state, as the view gave it. */
  view: Readonly<Record<string, unknown>>;
  /** Game messages the page had shown (the server gives back only its last 50). */
  messages: readonly { kind: 'message' | 'warning'; text: string }[];
}

/** A decision told apart by what it asks (its id is reused, its arrival differs after a reload). */
export function decisionKey(decision: Decision): string {
  return JSON.stringify([decision.participantId, decision.type, decision.text, decision.params]);
}

const key = (gameId: string) => `gemp.game.${gameId}`;

@Injectable()
export class GameRestore {
  private readonly store = inject(SessionStore);

  save(gameId: string, model: GameModel, view: Readonly<Record<string, unknown>>): void {
    const saved: SavedGame = {
      decision: model.decision ? decisionKey(model.decision.decision) : null,
      choices: model.local,
      view,
      messages: model.messages,
    };
    this.store.write(key(gameId), saved);
  }

  saved(gameId: string): SavedGame | null {
    return this.store.read(key(gameId)) as SavedGame | null;
  }

  /**
   * The model after a reload with what the page held before it: the choices, if the same decision still waits and they
   * still fit it; the messages the server no longer sends back, before its own.
   */
  restore(gameId: string, model: GameModel): GameModel {
    const saved = this.saved(gameId);
    if (!saved) return model;
    let restored = model;
    if (model.decision && saved.decision === decisionKey(model.decision.decision) && choicesFit(model, saved.choices)) {
      restored = { ...restored, local: saved.choices };
    }
    return { ...restored, messages: mergeMessages(saved.messages, model.messages) };
  }

  forget(gameId: string): void {
    this.store.remove(key(gameId));
  }
}

/**
 * The page's earlier messages followed by the server's: the server sends back its last messages, which end what the
 * page had; the part they share is kept once.
 */
export function mergeMessages<T extends { kind: string; text: string }>(
  earlier: readonly T[],
  server: readonly T[],
): T[] {
  const same = (a: T, b: T) => a.kind === b.kind && a.text === b.text;
  // The longest end of `earlier` that is a start of `server`
  for (let overlap = Math.min(earlier.length, server.length); overlap > 0; overlap--) {
    const tail = earlier.slice(earlier.length - overlap);
    if (
      tail.every((message, i) => {
        const other = server[i];
        return other !== undefined && same(message, other);
      })
    ) {
      return [...earlier, ...server.slice(overlap)];
    }
  }
  // Nothing shared (e.g. more than the server keeps happened while away): both, in order
  return [...earlier, ...server];
}
