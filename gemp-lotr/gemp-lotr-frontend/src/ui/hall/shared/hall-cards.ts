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

  view(blueprintId: string): CardView<string> {
    return this.views.create({ ref: blueprintId, blueprintId, zone: 'SPECIAL' });
  }

  show(blueprintId: string): void {
    this.info.show(this.view(blueprintId));
  }
}

/** Using a card (a thumbnail) shows it. */
@Injectable()
export class HallCardActions extends CardActions {
  private readonly cards = inject(HallCards);

  activate(ref: unknown): void {
    if (typeof ref === 'string') {
      this.cards.show(ref);
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
