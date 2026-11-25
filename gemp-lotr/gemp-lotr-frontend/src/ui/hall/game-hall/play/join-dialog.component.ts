import { Component, effect, inject, signal, untracked } from '@angular/core';
import { JoinDialog } from '../../../../core/hall/play/join-dialog.service';
import { HallDeckChoice } from '../../../../core/hall/tables/hall-table-models';
import { HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { DialogComponent } from '../../../shared/dialog/dialog.component';
import { DeckPickerComponent } from './deck-picker.component';
import { ResultBoxComponent } from './result-box.component';

/**
 * The Join dialog (JoinTable, hall.html #join-table-popup): one dialog for joining a table, a queue, a running
 * tournament, or registering a deck; its title and heading say which (and the subtitle which event), then the deck
 * (the flow's memory for its key, once the deck lists have arrived), the button and the result. It is drawn with the
 * Play overlay's form styles.
 *
 * DOM contract: `gd-join-dialog` (no box) holding `gd-dialog[data-dialog=join]` with `div.gd-play-form.gd-join`
 * holding `h1.gd-join__heading`, `div.gd-join__subtitle` (when there is one), `gd-deck-picker`,
 * `button.gd-join__submit` and `gd-result-box`.
 */
@Component({
  selector: 'gd-join-dialog',
  imports: [DeckPickerComponent, DialogComponent, ResultBoxComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="join" [heading]="join.state()?.title ?? ''" [open]="join.state() !== null" (closed)="join.close()">
      @if (join.state(); as state) {
        <div class="gd-play-form gd-join">
          <h1 class="gd-join__heading">{{ state.title }}</h1>
          @if (state.subtitle; as subtitle) {
            <div class="gd-join__subtitle">{{ subtitle }}</div>
          }
          <gd-deck-picker
            [playerDecks]="tables.playerDecks()"
            [libraryDecks]="tables.libraryDecks()"
            [value]="deck()"
            (deckChange)="picked($event)"
          />
          <button
            type="button"
            class="gd-play-form__button gd-join__submit"
            [disabled]="join.busy()"
            (click)="join.submit(deck())"
          >
            {{ state.buttonLabel }}
          </button>
          <gd-result-box [result]="join.result()" />
        </div>
      }
    </gd-dialog>
  `,
})
export class JoinDialogComponent {
  protected readonly join = inject(JoinDialog);
  protected readonly tables = inject(HallTablesService);
  protected readonly deck = signal<HallDeckChoice | null>(null);
  private byHand = false;

  constructor() {
    // each opening starts from the flow's memory; a deck list that arrives later fills it in
    effect(() => {
      const state = this.join.state();
      this.tables.playerDecks();
      this.tables.libraryDecks();
      untracked(() => {
        if (state === null) {
          this.byHand = false;
          this.deck.set(null);
        } else if (!this.byHand) {
          this.deck.set(this.join.initialDeck());
        }
      });
    });
  }

  protected picked(deck: HallDeckChoice | null): void {
    this.deck.set(deck);
    this.byHand = true;
  }
}
