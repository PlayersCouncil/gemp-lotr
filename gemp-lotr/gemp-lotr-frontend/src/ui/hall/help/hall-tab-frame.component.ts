import { Component, computed, effect, ElementRef, inject, input, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { findHallTab } from '../../../core/hall/hall-tabs';
import { CardInfoComponent } from '../../shared/card-info/card-info.component';
import { HallSubTabsComponent } from '../hall-sub-tabs.component';
import { HallCards } from './hall-cards';

/** A card share link's card (`#card-<id>` → `/help?card=<id>`). */
const CARD_PARAM = 'card';
const BLUEPRINT = /^\d{1,3}_\d{1,4}$/;

/**
 * A hall tab with sub-tabs (help.html #helpMain, info.html #infoMain: `.hall-tab` and its vertical jQuery UI tabs):
 * the sub-tabs at the left, sticky, the sub-tab's page beside them; the whole tab scrolls (`scroller`, which the
 * pages use to bring something into view), and the card information shows over it. A `?card=<blueprint id>` in the
 * address (a card share link) shows that card over whatever sub-tab opens; closing it takes the parameter away.
 *
 * Inputs: `tab` (required), the tab's legacy slug (hall-tabs.ts: "help", "info").
 *
 * DOM contract: `gd-hall-tab-frame.gd-hall-tab-frame` (`data-tab`; the scroller) holding `gd-hall-sub-tabs`, then
 * `div.gd-hall-tab-frame__page` (`role=tabpanel`) with the routed page, and `gd-card-info`.
 */
@Component({
  selector: 'gd-hall-tab-frame',
  styleUrl: './hall-tab-frame.component.css',
  imports: [HallSubTabsComponent, RouterOutlet, CardInfoComponent],
  host: { class: 'gd-hall-tab-frame', '[attr.data-tab]': 'tab()' },
  template: `
    @if (hallTab(); as hallTab) {
      <gd-hall-sub-tabs [tabs]="hallTab.subTabs" [base]="'/' + hallTab.path" [label]="hallTab.label" />
    }
    <div class="gd-hall-tab-frame__page" role="tabpanel">
      <router-outlet />
    </div>
    <gd-card-info />
  `,
})
export class HallTabFrameComponent {
  readonly tab = input.required<string>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cards = inject(HallCards);
  private readonly info = inject(CardInfoService);
  protected readonly hallTab = computed(() => findHallTab(this.tab()));
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

  /** The element that scrolls the tab. */
  scroller(): HTMLElement {
    return this.host;
  }
}
