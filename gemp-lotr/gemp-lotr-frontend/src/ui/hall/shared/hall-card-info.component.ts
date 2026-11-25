import { Component, effect, inject, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardInfoComponent } from '../../shared/card-info/card-info.component';
import { HallCards } from './hall-cards';

/** A card share link's card (`#card-<id>` → `/help?card=<id>`). */
const CARD_PARAM = 'card';
const BLUEPRINT = /^\d{1,3}_\d{1,4}$/;

/**
 * The card information of a hall tab that shows cards through HallCards (Help, Server Info), and the card a card share
 * link names: a `?card=<blueprint id>` in the address shows that card over whatever sub-tab opens; closing it takes the
 * parameter away (replacing the address). Projected into the tab frame.
 *
 * DOM contract: `gd-hall-card-info` (`display: contents`) holding `gd-card-info`.
 */
@Component({
  selector: 'gd-hall-card-info',
  imports: [CardInfoComponent],
  host: { style: 'display: contents' },
  template: '<gd-card-info />',
})
export class HallCardInfoComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cards = inject(HallCards);
  private readonly info = inject(CardInfoService);
  private readonly cardParam = toSignal(this.route.queryParamMap.pipe(map((params) => params.get(CARD_PARAM))), {
    initialValue: null,
  });
  private shownFromParam = false;

  constructor() {
    effect(() => {
      const card = this.cardParam();
      if (card !== null && BLUEPRINT.test(card)) {
        untracked(() => {
          this.shownFromParam = true;
          this.cards.show(card);
        });
      }
    });
    effect(() => {
      if (this.info.card() === null && this.shownFromParam) {
        this.shownFromParam = false;
        untracked(() => {
          if (this.cardParam() !== null) {
            void this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { [CARD_PARAM]: null },
              queryParamsHandling: 'merge',
              replaceUrl: true,
            });
          }
        });
      }
    });
  }
}
