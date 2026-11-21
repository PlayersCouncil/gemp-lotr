import { inject, Injectable, Provider } from '@angular/core';
import { CardActions } from '../../../core/cards/card-actions';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';

/** The hover zoom cookie the card code asks for (the Help and Server Info pages draw no hover zoom). */
export const HALL_PAGES_AUTO_ZOOM_COOKIE = 'autoZoomInHall';

/**
 * Cards on Help and Server Info (a format's lists, a patch note's card links, a card share link): what the legacy
 * hall's `.cardHint` did, the card shown in the card information. A card is shown as a hint was ("SPECIAL" zone).
 */
@Injectable()
export class HallCards {
  private readonly views = inject(CardViews);
  private readonly info = inject(CardInfoService);
  /** The name a card was drawn with, which heads its card information (the legacy preview's `data-title`). */
  private readonly titles = new Map<string, string>();

  view(blueprintId: string, title: string | null = null): CardView<string> {
    if (title !== null) {
      this.titles.set(blueprintId, title);
    }
    return this.views.create({ ref: blueprintId, blueprintId, zone: 'SPECIAL' });
  }

  /** `title`: what heads the card information (the card's name); none: "Card Information". */
  show(blueprintId: string, title: string | null = null): void {
    this.info.show(this.view(blueprintId), title);
  }

  /** A card drawn on the page was used: headed by the name it was drawn with. */
  showDrawn(blueprintId: string): void {
    this.show(blueprintId, this.titles.get(blueprintId) ?? null);
  }
}

/** Using a card (a thumbnail) shows it. */
@Injectable()
export class HallCardActions extends CardActions {
  private readonly cards = inject(HallCards);

  activate(ref: unknown): void {
    if (typeof ref === 'string') {
      this.cards.showDrawn(ref);
    }
  }
}

/** What a hall tab that shows cards provides on its route. */
export function provideHallCards(): Provider[] {
  return [
    HallCards,
    ...provideCardInteractions({ actions: HallCardActions, autoZoomCookie: HALL_PAGES_AUTO_ZOOM_COOKIE }),
  ];
}
