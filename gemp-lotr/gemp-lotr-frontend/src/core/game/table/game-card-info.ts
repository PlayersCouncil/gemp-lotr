import { effect, inject, Injectable, untracked } from '@angular/core';
import { CardInfoService } from '../../cards/card-info.service';
import { CardViews } from '../../cards/card-views.service';
import { GameSession } from '../session/game-session.service';
import { GameViewState } from '../session/game-view-state';
import { GameCardRef, GameCardViews } from './game-card-views';

/** What the card information says while the server's answer is on its way. */
export const RETRIEVING = '<div>Retrieving data...</div>';

/**
 * A card of the game shown in the card information gets what affects it, as the server writes it, beside it (the
 * legacy displayCardInfo): not for a card shown only for a decision (`temp…`), a card named in a text, or in a replay.
 * The card shown is kept with the game, so a reload shows it again (D10). Page-scoped.
 */
@Injectable()
export class GameCardInfo {
  private readonly info = inject(CardInfoService);
  private readonly session = inject(GameSession);
  private readonly view = inject(GameViewState);
  private readonly gameViews = inject(GameCardViews);
  private readonly views = inject(CardViews);
  /** Until the game has come after a reload, the card kept is still to be shown again. */
  private restored = false;

  constructor() {
    effect(() => {
      const loaded = this.session.model().eventCount > 0;
      const card = this.info.card();
      untracked(() => {
        if (!this.restored) {
          if (!loaded) return;
          this.restored = true;
          this.showKept();
          return;
        }
        const ref = card?.ref as Partial<GameCardRef> | null | undefined;
        this.view.cardInfo.set(
          card ? { cardId: typeof ref?.cardId === 'string' ? ref.cardId : null, blueprintId: card.blueprintId } : null,
        );
      });
    });
    effect(() => {
      const card = this.info.card();
      untracked(() => {
        const ref = card?.ref as Partial<GameCardRef> | null | undefined;
        const cardId = ref?.cardId;
        if (!card || typeof cardId !== 'string' || cardId.startsWith('temp') || this.session.replaying()) return;
        this.info.details.set(RETRIEVING);
        this.session.cardInfo(cardId, (html) => {
          // Still the same card
          if (this.info.card() === card) this.info.details.set(html);
        });
      });
    });
  }

  private showKept(): void {
    const kept = this.view.cardInfo();
    if (!kept) return;
    const card = kept.cardId === null ? undefined : this.session.model().cards[kept.cardId];
    if (card) {
      this.info.show(this.gameViews.of(card));
    } else if (kept.cardId === null) {
      this.info.show(this.views.create({ ref: { hint: true }, blueprintId: kept.blueprintId, zone: 'SPECIAL' }));
    } else {
      // The card is gone: nothing to show
      this.view.cardInfo.set(null);
    }
  }
}
