import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { offTableChoices } from '../../../core/game/phone/hand-place';
import { HAND_MIN, HIDE_BELOW, PhoneSettings } from '../../../core/game/phone/phone-settings';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { DragHandlers, PointerDragDirective } from '../../shared/pointer-drag/pointer-drag.directive';
import { PreGamePanelComponent } from '../pregame/pre-game-panel.component';
import { PhoneDividerComponent } from './phone-divider.component';
import { PhoneHandComponent } from './phone-hand.component';
import { PhoneSheetsComponent } from './phone-sheets.component';
import { PhoneStatusComponent } from './phone-status.component';
import { PhoneTableComponent } from './phone-table.component';

/** Arrow keys on the divider move it by this much (px). */
const HAND_STEP = 24;
/** The least the table keeps when the divider is dragged up (px). */
const TABLE_MIN = 90;
/** The hand's head and its row's padding: the hand's cards are as high as the rest (px). */
const HAND_CHROME = 26 + 12;

/**
 * The game in the modern theme on a phone or an upright tablet (G7 of trymarz/gemp-lotr-private#27; CompactLayout):
 * the status bar and the players' chips at the top, the table scrolling up and down, the divider with the decision,
 * and the hand under it. The divider's place is kept per device in a cookie (PhoneSettings); dragged to the bottom it
 * hides the hand (shown again while a decision offers cards in its place). Held sideways the table takes the whole
 * width, the divider the left half of the bottom, and the hand is a panel in the right half that its grip slides up as
 * far as the status bar (the owner, 2026-11-16). Sheets open over everything.
 *
 * DOM contract: `gd-game-phone` with `data-device` (phone, sideways, tablet), `data-hand-hidden`, `--gd-phone-card`
 * (the table's cards' height), `--gd-phone-hand` (the hand's height), holding `gd-phone-status`, `gd-phone-table`,
 * `gd-phone-divider`, `gd-phone-hand` and `gd-phone-sheets`; before the players are known `.gd-phone-pregame` (holding
 * `gd-game-pregame`) in the table's place and no hand.
 */
@Component({
  selector: 'gd-game-phone',
  styleUrl: './phone-game.component.css',
  imports: [
    PhoneStatusComponent,
    PhoneTableComponent,
    PhoneDividerComponent,
    PhoneHandComponent,
    PhoneSheetsComponent,
    PreGamePanelComponent,
    PointerDragDirective,
  ],
  host: {
    '[attr.data-device]': 'phone.device()',
    '[attr.data-hand-hidden]': "handHidden() ? '' : null",
    '[style.--gd-phone-card]': "phone.cardHeight() + 'px'",
    '[style.--gd-phone-table-card]': "phone.cardHeight() + 'px'",
    '[style.--gd-phone-hand]': "handHeight() + 'px'",
    '[style.--gd-phone-hand-card]': "handCard() + 'px'",
  },
  template: `
    <gd-phone-status #status />
    @if (session.model().started) {
      <gd-phone-table />
    } @else if (session.model().preGame; as preGame) {
      <!-- Before the players are known (the bid): the pre-game information, scrolled up and down -->
      <div class="gd-phone-pregame"><gd-game-pregame [info]="preGame" /></div>
    }
    <gd-phone-divider
      #divider
      tabindex="0"
      [handHidden]="handHidden() && session.model().started"
      [gdPointerDrag]="handDrag"
      [attr.aria-valuenow]="handHeight()"
      aria-valuemin="0"
      (keydown)="resizeWithKeys($event)"
    />
    @if (!handHidden() && session.model().started) {
      <gd-phone-hand [gripDrag]="handDrag" />
    }
    <gd-phone-sheets />
  `,
})
export class PhoneGameComponent {
  protected readonly phone = inject(PhoneSettings);
  protected readonly session = inject(GameSession);
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

  /**
   * Dragging the divider (anywhere but its buttons and fields) or the hand's grip: the hand grows by as much as the
   * pointer goes up, wherever the divider is (held sideways it is beside the hand), and its place is kept when let go.
   */
  protected readonly handDrag = (event: PointerEvent): DragHandlers | null => {
    const target = event.target as Element | null;
    if (target?.closest('button, select, input, a, output, gd-game-replay')) return null;
    const start = event.clientY;
    const from = this.handHidden() ? 0 : this.handHeight();
    const at = (e: PointerEvent) => this.clamp(from + start - e.clientY);
    return {
      move: (e) => this.dragging.set(at(e)),
      end: (e) => {
        const height = at(e);
        this.dragging.set(null);
        this.phone.setHand(height === 0 ? 'hidden' : height);
      },
      cancel: () => this.dragging.set(null),
    };
  };

  /**
   * The divider from the keyboard, as the deck builder's separator: Up makes the hand higher, Down lower (down to
   * hidden). Only when the divider itself has the focus, not one of its buttons.
   */
  protected resizeWithKeys(event: KeyboardEvent): void {
    if (event.target !== event.currentTarget) return;
    const step = event.key === 'ArrowUp' ? HAND_STEP : event.key === 'ArrowDown' ? -HAND_STEP : 0;
    if (!step) return;
    event.preventDefault();
    // A hidden hand comes back at its least height (a step up from nothing would still be hidden)
    const height = this.clamp(this.handHidden() ? (step > 0 ? HAND_MIN : 0) : this.handHeight() + step);
    this.phone.setHand(height === 0 ? 'hidden' : height);
  }

  /**
   * The hand's height for where the pointer is: hidden near the bottom, never squeezing the table out. Held sideways
   * the hand is a panel in the right half beside the table and the divider: it may reach the status bar.
   */
  private clamp(height: number): number {
    if (height < HIDE_BELOW) return 0;
    const under = this.host.clientHeight - this.status().nativeElement.offsetHeight;
    const max =
      this.phone.device() === 'sideways' ? under : under - this.divider().nativeElement.offsetHeight - TABLE_MIN;
    return Math.round(Math.max(HAND_MIN, Math.min(max, height)));
  }
}
