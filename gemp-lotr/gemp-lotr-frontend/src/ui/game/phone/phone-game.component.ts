import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { offTableChoices } from '../../../core/game/phone/hand-place';
import { HAND_MIN, HIDE_BELOW, PhoneSettings } from '../../../core/game/phone/phone-settings';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { PhoneDividerComponent } from './phone-divider.component';
import { PhoneHandComponent } from './phone-hand.component';
import { PhoneSheetsComponent } from './phone-sheets.component';
import { PhoneStatusComponent } from './phone-status.component';
import { PhoneTableComponent } from './phone-table.component';

/** The least the table keeps when the divider is dragged up (px). */
const TABLE_MIN = 90;
/** The hand's head and its row's padding: the hand's cards are as high as the rest (px). */
const HAND_CHROME = 26 + 12;

/**
 * The game in the modern theme on a phone or an upright tablet (G7 of trymarz/gemp-lotr-private#27; CompactLayout):
 * the status bar and the players' chips at the top, the table scrolling up and down, the divider with the decision,
 * and the hand under it. The divider's place is kept per device in a cookie (PhoneSettings); dragged to the bottom it
 * hides the hand (shown again while a decision offers cards in its place). Sheets open over everything.
 *
 * DOM contract: `gd-game-phone` with `data-device` (phone, sideways, tablet), `data-hand-hidden`, `--gd-phone-card`
 * (the table's cards' height), `--gd-phone-hand` (the hand's height), holding `gd-phone-status`, `gd-phone-table`,
 * `gd-phone-divider`, `gd-phone-hand` and `gd-phone-sheets`.
 */
@Component({
  selector: 'gd-game-phone',
  styleUrl: './phone-game.component.css',
  imports: [PhoneStatusComponent, PhoneTableComponent, PhoneDividerComponent, PhoneHandComponent, PhoneSheetsComponent],
  host: {
    '[attr.data-device]': 'phone.device()',
    '[attr.data-hand-hidden]': "handHidden() ? '' : null",
    '[style.--gd-phone-card]': "phone.cardHeight() + 'px'",
    '[style.--gd-phone-hand]': "handHeight() + 'px'",
    '[style.--gd-phone-hand-card]': "handCard() + 'px'",
  },
  template: `
    <gd-phone-status #status />
    <gd-phone-table />
    <gd-phone-divider #divider [handHidden]="handHidden()" (dragged)="drag($event)" (dropped)="drop($event)" />
    @if (!handHidden()) {
      <gd-phone-hand />
    }
    <gd-phone-sheets />
  `,
})
export class PhoneGameComponent {
  protected readonly phone = inject(PhoneSettings);
  private readonly session = inject(GameSession);
  private readonly decisions = inject(GameDecisions);
  private readonly viewState = inject(GameViewState);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly status = viewChild.required<unknown, ElementRef<HTMLElement>>('status', { read: ElementRef });
  private readonly divider = viewChild.required<unknown, ElementRef<HTMLElement>>('divider', { read: ElementRef });

  /** The hand's height while the divider is being dragged (px; 0: hidden), or null. */
  private readonly dragging = signal<number | null>(null);

  /** A decision offering cards in the hand's place shows the hand even if it was hidden. */
  private readonly offerOpen = computed(() => {
    const offer = offTableChoices(this.decisions.view(), this.session.model());
    return offer !== null && offer.key !== this.viewState.phone().closedOffer;
  });
  protected readonly handHidden = computed(() => {
    const dragging = this.dragging();
    if (dragging !== null) return dragging === 0;
    return this.phone.handHidden() && !this.offerOpen();
  });
  protected readonly handHeight = computed(() => {
    const dragging = this.dragging();
    if (dragging !== null) return dragging;
    const hand = this.phone.hand();
    return hand === 'hidden' ? (this.offerOpen() ? this.phone.defaultHand() : 0) : hand;
  });
  protected readonly handCard = computed(() => Math.max(48, this.handHeight() - HAND_CHROME));

  protected drag(height: number): void {
    this.dragging.set(this.clamp(height));
  }

  protected drop(height: number): void {
    const clamped = this.clamp(height);
    this.dragging.set(null);
    this.phone.setHand(clamped === 0 ? 'hidden' : clamped);
  }

  /** The hand's height for where the pointer is: hidden near the bottom, never squeezing the table out. */
  private clamp(height: number): number {
    if (height < HIDE_BELOW) return 0;
    const max =
      this.host.clientHeight -
      this.status().nativeElement.offsetHeight -
      this.divider().nativeElement.offsetHeight -
      TABLE_MIN;
    return Math.round(Math.max(HAND_MIN, Math.min(max, height)));
  }
}
