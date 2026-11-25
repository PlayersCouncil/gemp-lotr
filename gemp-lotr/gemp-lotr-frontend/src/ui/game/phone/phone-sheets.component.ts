import { afterNextRender, Component, effect, ElementRef, inject, Injector, untracked, viewChild } from '@angular/core';
import { noEffectReason } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { chatLines, GameSheets } from '../../../core/game/phone/game-sheets';
import { Sheet } from '../../../core/game/phone/sheet';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { ModalSheetDirective } from './modal-sheet.directive';
import { ScrollSettledDirective } from './scroll-settled.directive';
import { PhoneCardSheetComponent } from './sheets/phone-card-sheet.component';
import { PhoneChatSheetComponent } from './sheets/phone-chat-sheet.component';
import { PhoneConfirmSheetComponent } from './sheets/phone-confirm-sheet.component';
import { PhoneInfoSheetComponent } from './sheets/phone-info-sheet.component';
import { PhoneMenuSheetComponent } from './sheets/phone-menu-sheet.component';
import { PhonePilesSheetComponent } from './sheets/phone-piles-sheet.component';
import { PhoneSettingsSheetComponent } from './sheets/phone-settings-sheet.component';
import { SwipeDownDirective } from './swipe-down.directive';

/**
 * The phone layout's sheets (G7 of trymarz/gemp-lotr-private#27), one over another (GameSheets): a card's details,
 * a player's piles, the menu, the chat with the game's messages, the settings (one column, finger-sized rows), the
 * game's information and the confirmations to concede or cancel; and over them all, a card's actions to choose from.
 * Closing (✕, ‹ when it goes back, Escape on the page, the backdrop) shows the sheet below as it was, scrolled to the
 * same place. Phones get them from the bottom; tablets in the middle. This is the
 * sheets' frame; what each shows is its own component (sheets/).
 *
 * DOM contract: `gd-phone-sheets` holding, while one is open, `dialog.gd-phone-sheet` (a modal: `data-sheet` the kind,
 * `data-depth` how many are open, `aria-label` its title) with `.gd-phone-sheet__backdrop`, and `.gd-phone-sheet__panel`
 * holding
 * `.gd-phone-sheet__head` (`h2`, `button[data-phone=sheet-close]` with `data-back` when it goes back) and
 * `.gd-phone-sheet__body`. The card's details: `img.gd-phone-detail__image` (the picture only) and the attached
 * cards' `gd-phone-cards`. Piles: `section[data-pile]` each. The menu: `button[data-menu]`. The actions:
 * `.gd-phone-sheet[data-sheet=actions]` with `button[data-action-id]`.
 */
@Component({
  selector: 'gd-phone-sheets',
  styleUrl: './phone-sheets.component.css',
  imports: [
    ModalSheetDirective,
    ScrollSettledDirective,
    SwipeDownDirective,
    PhoneCardSheetComponent,
    PhonePilesSheetComponent,
    PhoneMenuSheetComponent,
    PhoneChatSheetComponent,
    PhoneSettingsSheetComponent,
    PhoneInfoSheetComponent,
    PhoneConfirmSheetComponent,
  ],
  template: `
    @if (top(); as entry) {
      <dialog
        gdModalSheet
        class="gd-phone-sheet"
        [attr.data-sheet]="entry.sheet.kind"
        [attr.data-depth]="sheets.entries().length"
        [attr.aria-label]="title(entry.sheet)"
      >
        <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -->
        <div class="gd-phone-sheet__backdrop" (click)="sheets.back()"></div>
        <!--
          A card's details, the piles, the game info, the settings and the menu: a swipe down does what the corner button
          does, ✕ or ‹ (the owner, 2026-11-18)
        -->
        <div
          class="gd-phone-sheet__panel"
          [gdSwipeDown]="swipeCloses(entry.sheet.kind)"
          gdSwipeDownScroller=".gd-phone-sheet__body"
          (gdSwipedDown)="sheets.back()"
        >
          <div class="gd-phone-sheet__head">
            <h2>{{ title(entry.sheet) }}</h2>
            <button
              type="button"
              class="gd-phone-sheet__close"
              data-phone="sheet-close"
              [attr.data-back]="sheets.canGoBack() ? '' : null"
              [attr.aria-label]="sheets.canGoBack() ? 'Back' : 'Close'"
              (click)="back($event)"
            >
              {{ sheets.canGoBack() ? '‹' : '✕' }}
            </button>
          </div>
          <div #body class="gd-phone-sheet__body" (gdScrollSettled)="keep()">
            @let sheet = entry.sheet;
            <!-- One sheet's content, by its kind (an @if chain narrows the sheet's type for each) -->
            @if (sheet.kind === 'card') {
              <gd-phone-card-sheet [sheet]="sheet" />
            } @else if (sheet.kind === 'piles') {
              <gd-phone-piles-sheet [sheet]="sheet" />
            } @else if (sheet.kind === 'menu') {
              <gd-phone-menu-sheet />
            } @else if (sheet.kind === 'chat') {
              <gd-phone-chat-sheet />
            } @else if (sheet.kind === 'settings') {
              <gd-phone-settings-sheet [sheet]="sheet" />
            } @else if (sheet.kind === 'info') {
              <gd-phone-info-sheet />
            } @else if (sheet.kind === 'confirm') {
              <gd-phone-confirm-sheet [sheet]="sheet" />
            }
          </div>
        </div>
      </dialog>
    }
    @if (decisions.menu(); as menu) {
      <dialog gdModalSheet class="gd-phone-sheet" data-sheet="actions" aria-label="Choose an action">
        <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -->
        <div class="gd-phone-sheet__backdrop" (click)="decisions.closeMenu()"></div>
        <div class="gd-phone-sheet__panel">
          <div class="gd-phone-sheet__head">
            <h2>Choose an action</h2>
            <button
              type="button"
              class="gd-phone-sheet__close"
              data-phone="sheet-close"
              aria-label="Close"
              (click)="decisions.closeMenu()"
            >
              ✕
            </button>
          </div>
          <div class="gd-phone-sheet__body">
            <div class="gd-phone-menu">
              @for (action of menu.actions; track action.actionId) {
                <button
                  type="button"
                  class="gd-phone-menu__item"
                  [attr.data-action-id]="action.actionId"
                  [attr.data-no-effect]="action.noEffect"
                  (click)="decisions.chooseAction(action.actionId)"
                >
                  <span [innerHTML]="action.text"></span>
                  <!-- An action that would do nothing: dimmed, with why (game.html's noEffectAction) -->
                  @if (action.noEffect; as why) {
                    <span class="gd-phone-menu__reason">{{ ' (' + noEffectReason(why) + ')' }}</span>
                  }
                </button>
              }
            </div>
          </div>
        </div>
      </dialog>
    }
  `,
})
export class PhoneSheetsComponent {
  protected readonly sheets = inject(GameSheets);
  protected readonly decisions = inject(GameDecisions);
  protected readonly noEffectReason = noEffectReason;
  private readonly session = inject(GameSession);
  private readonly log = inject(GameLog);
  private readonly viewState = inject(GameViewState);
  private readonly body = viewChild<ElementRef<HTMLElement>>('body');

  protected readonly top = this.sheets.top;

  constructor() {
    const injector = inject(Injector);
    // A sheet shown (opened, or shown again by going back): scrolled where it was
    let shown: Sheet | null = null;
    let depth = 0;
    effect(() => {
      const entry = this.top();
      const entries = this.sheets.entries().length;
      untracked(() => {
        if (!entry || (entry.sheet === shown && entries === depth)) {
          shown = entry?.sheet ?? null;
          depth = entries;
          return;
        }
        shown = entry.sheet;
        depth = entries;
        const scroll = entry.scroll;
        afterNextRender(
          {
            write: () => {
              const body = this.body()?.nativeElement;
              if (!body) return;
              body.scrollTop = scroll.top;
              this.rowsOf(body).forEach((row, i) => (row.scrollLeft = scroll.rows[i] ?? 0));
              if (entry.sheet.kind === 'piles' && entry.sheet.focus && scroll.top === 0) {
                body.querySelector(`[data-pile="${entry.sheet.focus}"]`)?.scrollIntoView({ block: 'start' });
              }
            },
          },
          { injector },
        );
        if (entry.sheet.kind === 'chat') this.markChatSeen();
      });
    });
    // While the chat is open, what comes is seen
    effect(() => {
      this.log.entries();
      untracked(() => {
        if (this.top()?.sheet.kind === 'chat') this.markChatSeen();
      });
    });
  }

  protected title(sheet: Sheet): string {
    switch (sheet.kind) {
      case 'card':
        return 'Card';
      case 'piles': {
        const model = this.session.model();
        return !model.spectator && sheet.player === model.participantId ? 'Your piles' : `${sheet.player}'s piles`;
      }
      case 'menu':
        return 'Menu';
      case 'chat':
        return 'Chat and game log';
      case 'log':
        return 'Game log';
      case 'settings':
        return 'Settings';
      case 'info':
        return 'Game info';
      case 'confirm':
        return sheet.what === 'concede' ? 'Concede game?' : 'Request game cancel?';
    }
  }

  /**
   * The close button: one level back. The focus goes to the sheet itself, so the sheet shown again (whose button it is
   * too) doesn't keep it on its button.
   */
  protected back(event: Event): void {
    (event.currentTarget as HTMLElement | null)?.closest('dialog')?.focus({ preventScroll: true });
    this.sheets.back();
  }

  /**
   * The sheets a swipe down goes back from (SwipeDownDirective; the owner, 2026-11-18). Not the chat and its log, whose
   * field and lines take touches, nor a confirmation, which is answered.
   */
  protected swipeCloses(kind: string): boolean {
    return ['card', 'piles', 'info', 'settings', 'menu'].includes(kind);
  }

  /** Where the sheet shown is scrolled, kept for when it is shown again (and after a reload). */
  protected keep(): void {
    const body = this.body()?.nativeElement;
    if (!body) return;
    this.sheets.keepScroll({
      top: Math.round(body.scrollTop),
      rows: this.rowsOf(body).map((row) => Math.round(row.scrollLeft)),
    });
  }

  private rowsOf(body: HTMLElement): HTMLElement[] {
    return [...body.querySelectorAll<HTMLElement>('.gd-phone-cards__scroll')];
  }

  private markChatSeen(): void {
    const seen = chatLines(this.log.entries());
    this.viewState.phone.update((phone) => (phone.chatSeen === seen ? phone : { ...phone, chatSeen: seen }));
  }
}
