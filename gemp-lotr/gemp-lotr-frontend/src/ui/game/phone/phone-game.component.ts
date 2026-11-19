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
/** Held sideways, how far a drag goes (px) to hide the hand or show it again. */
const SIDEWAYS_SWIPE = 40;

/**
 * The game in the modern theme on a phone or an upright tablet (G7 of trymarz/gemp-lotr-private#27; CompactLayout):
 * the status bar and the players' chips at the top, the table scrolling up and down, the divider with the decision,
 * and the hand under it. The divider's place is kept per device in a cookie (PhoneSettings); dragged to the bottom it
 * hides the hand (shown again while a decision offers cards in its place). Held sideways the screen has two columns
 * (the owner, 2026-11-19): the table on the left, and a panel docked on the right with the divider on top and the hand
 * filling the rest, its cards in rows, one size; dragging down hides the hand (the decision then at the panel's
 * bottom), up shows it. Sheets open over everything.
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
  /**
   * The hand's cards' height: upright, as high as the hand allows; held sideways one size, from the card size setting
   * (the hand fills the panel; dragging only hides and shows it, the owner 2026-11-19).
   */
  protected readonly handCard = computed(() =>
    this.phone.device() === 'sideways'
      ? Math.round(((this.phone.defaultHand() - HAND_CHROME) * this.phone.cardSize()) / 100)
      : Math.max(48, this.handHeight() - HAND_CHROME),
  );

  /**
   * Dragging the divider (anywhere but its buttons and fields) or the hand's grip: the hand grows by as much as the
   * pointer goes up, wherever the divider is (held sideways it is under the hand), and its place is kept when let go.
   */
  protected readonly handDrag = (event: PointerEvent): DragHandlers | null => {
    const target = event.target as Element | null;
    if (target?.closest('button, select, input, a, output, gd-game-replay')) return null;
    const start = event.clientY;
    if (this.phone.device() === 'sideways') return this.sidewaysDrag(start);
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
    // Held sideways: Down hides the hand, Up shows it
    if (this.phone.device() === 'sideways') {
      this.phone.setHand(step < 0 ? 'hidden' : this.phone.defaultHand());
      return;
    }
    // A hidden hand comes back at its least height (a step up from nothing would still be hidden); a step down from its
    // least height hides it (the clamp would put it back at the least height, so the keys could never hide it)
    const next = this.handHidden() ? (step > 0 ? HAND_MIN : 0) : this.handHeight() + step;
    const height = step < 0 && next < HAND_MIN ? 0 : this.clamp(next);
    this.phone.setHand(height === 0 ? 'hidden' : height);
  }

  /**
   * Held sideways, dragging the divider or the hand's grip down hides the hand and up shows it again, as upright (the
   * owner, 2026-11-19: resizing the cards by it felt unnatural); nothing moves while dragging.
   */
  private sidewaysDrag(start: number): DragHandlers {
    return {
      move: () => undefined,
      end: (e) => {
        const down = e.clientY - start;
        if (down > SIDEWAYS_SWIPE && !this.handHidden()) this.phone.setHand('hidden');
        else if (down < -SIDEWAYS_SWIPE && this.handHidden()) this.phone.setHand(this.phone.defaultHand());
      },
      cancel: () => undefined,
    };
  }

  /**
   * The hand's height for where the pointer is: hidden near the bottom, never squeezing the table out. Held sideways
   * the hand is a panel in the right half over the table, standing on the divider: it may reach the status bar.
   */
  private clamp(height: number): number {
    if (height < HIDE_BELOW) return 0;
    const under = this.host.clientHeight - this.status().nativeElement.offsetHeight;
    const divider = this.divider().nativeElement.offsetHeight;
    const max = this.phone.device() === 'sideways' ? under - divider : under - divider - TABLE_MIN;
    return Math.round(Math.max(HAND_MIN, Math.min(max, height)));
  }
}
