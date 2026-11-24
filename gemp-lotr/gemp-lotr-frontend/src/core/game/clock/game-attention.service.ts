import { DestroyRef, effect, inject, Injectable, untracked } from '@angular/core';
import { PageCues } from '../../platform/page-cues';
import { Decision } from '../protocol/game-events';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';

/** The tab's title while the game waits for nothing from the player. */
export const GAME_TITLE = 'Game of Gemp-LotR';
/** ... and, every other half second, while it waits for a decision. */
export const DECISION_TITLE = 'Waiting for your decision';
/** How long each of the two titles shows. */
export const TITLE_FLASH_MS = 500;

/**
 * Whether a decision plays the "awaiting decision" sound when it comes (if the player is looking elsewhere): the ones
 * the legacy page did, which are the choices of cards and actions and a choice of two answers as buttons.
 */
export function soundsOnArrival(decision: Decision, alwaysDropDown: boolean): boolean {
  switch (decision.type) {
    case 'MULTIPLE_CHOICE':
      return (decision.params['results'] ?? []).length <= 2 && !alwaysDropDown;
    case 'ARBITRARY_CARDS':
    case 'CARD_ACTION_CHOICE':
    case 'ACTION_CHOICE':
    case 'CARD_SELECTION':
      return true;
    default:
      return false;
  }
}

/**
 * Getting the player's attention when the game waits for them: the tab's title flashes until they answer (also past
 * the game's end, as on the legacy page), and some decisions play a sound when the page is in the background.
 * Page-scoped.
 */
@Injectable()
export class GameAttention {
  private readonly session = inject(GameSession);
  private readonly settings = inject(GameSettings);
  private readonly cues = inject(PageCues);
  private timer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    let flashing = false;
    effect(() => {
      const model = this.session.model();
      // (a scripted game's table being changed by hand asks nobody anything: TableEdit)
      const waiting = model.scenario?.info?.mode === 'edit' ? null : model.decision;
      untracked(() => {
        // A replay shows decisions, it doesn't wait for one
        if (this.session.replaying()) return;
        if (waiting) {
          this.flash(DECISION_TITLE);
          flashing = true;
          const autoDismissed =
            waiting.decision.type === 'ARBITRARY_CARDS' &&
            waiting.decision.params['deckInspection']?.[0] === 'own' &&
            this.settings.autoDismissDeckReveal();
          if (!autoDismissed && soundsOnArrival(waiting.decision, this.settings.alwaysDropDown()) && this.cues.away()) {
            this.cues.play('awaitAction');
          }
        } else if (flashing && !this.session.model().ended) {
          flashing = false;
          this.stopFlashing();
        }
      });
    });
    inject(DestroyRef).onDestroy(() => this.stopFlashing());
  }

  /** Half a second later, this title; then the other one, in turn. */
  private flash(next: string): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = setTimeout(() => {
      this.cues.setTitle(next);
      this.flash(next === DECISION_TITLE ? GAME_TITLE : DECISION_TITLE);
    }, TITLE_FLASH_MS);
  }

  private stopFlashing(): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
    this.cues.setTitle(GAME_TITLE);
  }
}
