import { effect, inject, Injectable, untracked } from '@angular/core';
import { CardInfoService } from '../../cards/card-info.service';
import { PageCues } from '../../platform/page-cues';
import { soundsOnArrival } from '../clock/game-attention.service';
import { decisionKey } from '../session/game-restore';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { GameCardRef } from '../table/game-card-views';
import { GameSheets } from './game-sheets';
import { PhoneSettings } from './phone-settings';

/**
 * What the phone layout does on its own with the game's events (G7 of trymarz/gemp-lotr-private#27):
 * - a card's details asked for (a long press) open as a sheet over the one shown, instead of the computer's dialog;
 * - answering a decision closes every sheet;
 * - a decision coming makes the phone buzz and plays a sound ("Your move", both on unless the player turned them off).
 * Nothing outside the phone layout. Page-scoped.
 */
@Injectable()
export class PhoneBridge {
  private readonly session = inject(GameSession);
  private readonly sheets = inject(GameSheets);
  private readonly phone = inject(PhoneSettings);
  private readonly info = inject(CardInfoService);
  private readonly cues = inject(PageCues);
  private readonly settings = inject(GameSettings);

  constructor() {
    effect(() => {
      const card = this.info.card();
      const device = this.phone.device();
      untracked(() => {
        if (!card || device === null) return;
        const ref = card.ref as Partial<GameCardRef> | null | undefined;
        this.info.close();
        this.sheets.open({
          kind: 'card',
          cardId: typeof ref?.cardId === 'string' ? ref.cardId : null,
          blueprintId: card.blueprintId,
        });
      });
    });

    let waiting: string | null = null;
    let first = true;
    effect(() => {
      const pending = this.session.model().decision;
      const loaded = this.session.model().eventCount > 0;
      untracked(() => {
        if (!loaded) return;
        const key = pending ? decisionKey(pending.decision) : null;
        const before = waiting;
        waiting = key;
        // The first state after opening (or a reload) is no news
        if (first) {
          first = false;
          return;
        }
        if (this.phone.device() === null || this.session.replaying() || key === before) return;
        if (key === null) {
          this.sheets.closeAll();
        } else if (pending && soundsOnArrival(pending.decision, this.settings.alwaysDropDown())) {
          if (this.phone.moveVibrate()) this.cues.vibrate();
          if (this.phone.moveSound()) this.cues.play('awaitAction');
        }
      });
    });
  }
}
