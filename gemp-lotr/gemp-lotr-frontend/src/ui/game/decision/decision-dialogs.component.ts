import { Component, computed, inject, linkedSignal } from '@angular/core';
import { DecisionButton, DecisionCard, decisionCard, DecisionDialog } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { GameCard } from '../../../core/game/state/game-model';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { CardComponent } from '../../shared/card/card.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { CardHintsDirective } from '../table/card-hints.directive';
import { GameCardDirective } from '../table/game-card.directive';

interface ShownCard {
  card: GameCard;
  text: string | null;
}

/** A button's answer without the card names' links: pressing it must answer, not show a card (legacy). */
export function choiceLabel(html: string): string {
  return html.replace('cardHint', '').replace('<div', "<div style='display:inline'");
}

/**
 * The dialogs a waiting decision asks in: a small one for a number (a spinner: two arrows around the value) or one
 * answer of several (a button each for two, a list with OK for more, or always, if the player set that), and a large
 * one showing cards to choose from. Neither can be closed: they close when the decision is answered.
 *
 * DOM contract: `gd-dialog` with `data-dialog` = decision or decision-cards. In the small one: `.gd-game-decision__text`,
 * then `.gd-game-spinner` holding `button[data-decision="integer-down"]`, `output[data-decision="integer"]` and
 * `button[data-decision="integer-up"]`; or `select[data-decision="choice"]`; or `button[data-decision-choice]` (the
 * answer's index) per answer. In the large one: `.gd-game-decision__cards` with a `button.gd-card` per card (see
 * GameCardDirective; `data-zone` SPECIAL). The buttons under either: `button[data-decision-button]`. Both are `inert`
 * (dimmed) while a revert request waits.
 */
@Component({
  selector: 'gd-game-decision-dialogs',
  styleUrl: './decision-dialogs.component.css',
  imports: [DialogComponent, CardComponent, FitCardsDirective, GameCardDirective, CardHintsDirective],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="decision"
      [heading]="noTitle"
      [modal]="false"
      [closable]="false"
      [focusSelf]="!listShown()"
      [inert]="undo.frozen()"
      [open]="small() !== null"
    >
      @if (small(); as dialog) {
        <span class="gd-game-decision__text" gdCardHints [innerHTML]="dialog.text"></span><br />
        @if (dialog.kind === 'integer') {
          <span class="gd-game-spinner" role="group" aria-label="Number">
            <button
              type="button"
              class="gd-game-spinner__arrow"
              data-decision="integer-down"
              aria-label="Less"
              (click)="step(dialog, -1)"
            ></button>
            <output class="gd-game-spinner__value" data-decision="integer">{{ number() }}</output>
            <button
              type="button"
              class="gd-game-spinner__arrow"
              data-decision="integer-up"
              aria-label="More"
              (click)="step(dialog, 1)"
            ></button>
          </span>
        } @else if (dialog.dropDown) {
          <select #list autofocus data-decision="choice" aria-label="Answer" (change)="choice.set(list.selectedIndex)">
            @for (option of dialog.options; track $index) {
              <option [value]="$index" [innerHTML]="option"></option>
            }
          </select>
        } @else {
          @for (option of dialog.options; track $index) {
            @if ($index > 0) {
              {{ ' ' }}
            }
            <button
              type="button"
              class="gd-button gd-game-decision__button"
              [attr.data-decision-choice]="$index"
              (click)="decisions.answerChoice($index)"
            >
              <span [innerHTML]="label(option)"></span>
            </button>
          }
        }
      }
      @if (okFor(); as dialog) {
        <div class="gd-game-decision__actions" gdDialogActions>
          <button
            type="button"
            class="gd-button gd-game-decision__button"
            data-decision-button="OK"
            (click)="ok(dialog)"
          >
            <span>OK</span>
          </button>
        </div>
      }
    </gd-dialog>
    <gd-dialog
      name="decision-cards"
      [heading]="cards()?.title ?? ''"
      [headingMarkup]="true"
      [modal]="false"
      [closable]="false"
      [focusSelf]="true"
      [inert]="undo.frozen()"
      [open]="cards() !== null"
    >
      @if (cards(); as dialog) {
        <div class="gd-game-decision__cards" [gdFitCards]="fit()" [gdFitCardsRoundedFrom]="roundedFrom">
          @for (shown of shownCards(); track shown.card.id) {
            <button [gdCard]="views.of(shown.card)" [gdGameCard]="shown.card">
              @if (shown.text !== null) {
                <!-- A click on the text only shows the cards it names: the card is chosen by its picture, as on the legacy page -->
                <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -->
                <span
                  class="gd-game-decision__card-text"
                  gdCardHints
                  [innerHTML]="shown.text"
                  (click)="$event.stopPropagation()"
                ></span>
              }
            </button>
          }
        </div>
      }
      @if (cardButtons().length > 0) {
        <div class="gd-game-decision__actions" gdDialogActions>
          @for (button of cardButtons(); track button) {
            <button
              type="button"
              class="gd-button gd-game-decision__button"
              [attr.data-decision-button]="button"
              (click)="press(button)"
            >
              <span>{{ button }}</span>
            </button>
          }
        </div>
      }
    </gd-dialog>
  `,
})
export class DecisionDialogsComponent {
  protected readonly decisions = inject(GameDecisions);
  protected readonly undo = inject(GameUndo);
  protected readonly views = inject(GameCardViews);

  private readonly dialog = computed(() => this.decisions.view().dialog);
  protected readonly small = computed(() => {
    const dialog = this.dialog();
    return dialog && dialog.kind !== 'cards' ? dialog : null;
  });
  protected readonly cards = computed(() => {
    const dialog = this.dialog();
    return dialog?.kind === 'cards' ? dialog : null;
  });

  /**
   * The legacy dialog sized its cards from its content's height in whole pixels: the content box is this much higher
   * than the cards' box (8px above, 1em - 2px below at 13.2px).
   */
  protected readonly roundedFrom = 8 + 13.2 - 2;

  /** The small dialog has no title: a space keeps its bar as high as a title's line, as on the legacy page. */
  protected readonly noTitle = '\u00a0';
  /** The small dialog when it is answered with OK (a number, a list). */
  protected readonly okFor = computed(() => {
    const dialog = this.small();
    return dialog?.answerable && (dialog.kind === 'integer' || dialog.dropDown) ? dialog : null;
  });
  /** A list takes the focus when its dialog opens, as on the legacy page; otherwise nothing in it does. */
  protected readonly listShown = computed(() => {
    const dialog = this.small();
    return dialog?.kind === 'choice' && dialog.dropDown;
  });
  protected readonly cardButtons = computed(() => this.cards()?.buttons ?? []);

  /** The number shown: the decision's own to start with (or its least, if that isn't allowed). */
  protected readonly number = linkedSignal(() => {
    const dialog = this.small();
    if (dialog?.kind !== 'integer') return 0;
    return dialog.value < dialog.min || dialog.value > dialog.max ? dialog.min : dialog.value;
  });
  /** The answer picked in the list: the first to start with. */
  protected readonly choice = linkedSignal(() => (this.small() ? 0 : 0));

  protected readonly shownCards = computed<ShownCard[]>(() =>
    (this.cards()?.cards ?? []).map((card: DecisionCard) => ({
      card: decisionCard(card.id, card.blueprintId, card.testingText, 'SPECIAL'),
      text: card.text,
    })),
  );
  protected readonly fit = computed<FitCard[]>(() =>
    this.shownCards().map(({ card }) => ({ horizontal: this.views.of(card).horizontal })),
  );

  protected label(option: string): string {
    return choiceLabel(option);
  }

  protected step(dialog: DecisionDialog, by: number): void {
    if (dialog.kind !== 'integer') return;
    const next = this.number() + by;
    if (next >= dialog.min && next <= dialog.max) this.number.set(next);
  }

  protected ok(dialog: DecisionDialog): void {
    if (dialog.kind === 'integer') {
      this.decisions.answerNumber(String(this.number()));
    } else if (dialog.kind === 'choice') {
      this.decisions.answerChoice(this.choice());
    }
  }

  protected press(button: DecisionButton): void {
    void this.decisions.press(button);
  }
}
