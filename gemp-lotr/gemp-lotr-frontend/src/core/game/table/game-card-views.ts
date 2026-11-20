import { computed, inject, Injectable } from '@angular/core';
import { stripBlueprintId } from '../../cards/card-blueprint';
import { CardImageService } from '../../cards/card-image.service';
import { CardView } from '../../cards/card-view';
import { CardViews } from '../../cards/card-views.service';
import { GameSession } from '../session/game-session.service';
import { GameCard } from '../state/game-model';
import { metaSitesOf } from './pre-game';

/** What a card on the game page stands for: the server's card. */
export interface GameCardRef {
  cardId: string;
}

/**
 * The picture of each card of a game (CardView), kept while the card shows the same thing, so a card that didn't
 * change is the same object from one state of the game to the next. A hindered card shows its back. An RTMD meta-site
 * the game's pre-game setup names shows its visual card with the modifier over its lower part, as game.html draws it
 * (Card.applyMetaSiteOverlay): on the table, in the piles, zoomed and in the card information. Page-scoped.
 */
@Injectable()
export class GameCardViews {
  private readonly views = inject(CardViews);
  private readonly images = inject(CardImageService);
  private readonly session = inject(GameSession);
  private readonly cache = new Map<string, { key: string; view: CardView<GameCardRef> }>();

  /** RTMD: each meta-site modifier the pre-game setup names (its bare blueprint), with the visual card it is drawn on. */
  private readonly metaSites = computed(() => {
    const visuals: Record<string, string> = {};
    for (const pairs of Object.values(metaSitesOf(this.session.model().preGame?.metaSites ?? ''))) {
      for (const { visual, modifier } of pairs) {
        if (visual && modifier) visuals[stripBlueprintId(modifier)] = visual;
      }
    }
    return visuals;
  });

  of(card: GameCard): CardView<GameCardRef> {
    const visual = card.hindered ? null : this.metaSiteVisual(card.blueprintId);
    const key = `${card.blueprintId}|${card.zone}|${card.hindered}|${visual}`;
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
    } else {
      view = this.asMetaSite(view);
    }
    this.cache.set(card.id, { key, view });
    return view;
  }

  /** The visual card an RTMD meta-site modifier is drawn on in this game, or null when it isn't one. */
  metaSiteVisual(blueprintId: string): string | null {
    return this.metaSites()[stripBlueprintId(blueprintId)] ?? null;
  }

  /**
   * A card drawn as an RTMD meta-site when the game names it one: the visual card's picture, with the modifier's own
   * (the card's) over its lower part. Any other card as it is.
   */
  asMetaSite<Ref>(view: CardView<Ref>): CardView<Ref> {
    const visual = this.metaSiteVisual(view.blueprintId);
    if (!visual) return view;
    return { ...view, imageUrl: this.images.resolve(visual, view.zone).imageUrl, overlayImage: view.imageUrl };
  }
}
