import { inject, Injectable } from '@angular/core';
import { CardImageService } from '../../cards/card-image.service';
import { CardView } from '../../cards/card-view';
import { CardViews } from '../../cards/card-views.service';
import { GameCard } from '../state/game-model';

/** What a card on the game page stands for: the server's card. */
export interface GameCardRef {
  cardId: string;
}

/**
 * The picture of each card of a game (CardView), kept while the card shows the same thing, so a card that didn't
 * change is the same object from one state of the game to the next. A hindered card shows its back. Page-scoped.
 */
@Injectable()
export class GameCardViews {
  private readonly views = inject(CardViews);
  private readonly images = inject(CardImageService);
  private readonly cache = new Map<string, { key: string; view: CardView<GameCardRef> }>();

  of(card: GameCard): CardView<GameCardRef> {
    const key = `${card.blueprintId}|${card.zone}|${card.hindered}`;
    const cached = this.cache.get(card.id);
    if (cached?.key === key) return cached.view;
    let view = this.views.create<GameCardRef>({
      ref: { cardId: card.id },
      blueprintId: card.blueprintId,
      zone: card.zone,
      wholePixels: true,
    });
    if (card.hindered) {
      view = { ...view, imageUrl: this.images.resolve(card.blueprintId, card.zone).backSideImageUrl };
    }
    this.cache.set(card.id, { key, view });
    return view;
  }
}
