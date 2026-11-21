import { Component, input, output } from '@angular/core';
import { HallDeckChoice, HallDeckOption } from '../../../../core/hall/tables/hall-table-models';
import { CHOOSE_DECK, NO_DECKS_YET } from '../../../../core/hall/tables/hall-tables-messages';
import {
  CHOOSE_LIBRARY_DECK,
  deckLabel,
  HELP_LIBRARY,
  SELECT_LIBRARY_DECK,
  YOUR_DECK,
} from '../../../../core/hall/play/play-messages';
import { fieldValue, HelpToggles } from './play-parts';

/**
 * A flow's deck picker (SelectDeck, `.player-deck`): "Your Deck" and, unless `library` is off, "Select Library
 * Deck" with its (i). Choosing a deck in one clears the other. `fixedFirstOption` is a first line kept at the top of
 * the player's list with no deck behind it (the bot's "Random FotR Starter"). `deckChange` reports a deck picked by
 * hand (or null).
 *
 * DOM contract: `gd-deck-picker.gd-play-form__deck` holding `div.gd-play-form__row` (a `div.gd-play-form__label` and
 * `select.gd-play-form__field[data-deck=player]`), and with the library a second row
 * (`select.gd-play-form__field[data-deck=library]`, `button.gd-play-form__info`) and its
 * `div.gd-play-form__info-text`. Each option's value is the deck's name ("" for none); its text "[Format] - name".
 */
@Component({
  selector: 'gd-deck-picker',
  host: { class: 'gd-play-form__deck' },
  template: `
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">{{ label() }}</div>
      <select class="gd-play-form__field" data-deck="player" (change)="picked($event, 'player')">
        <option value="" [selected]="!isPlayer()">{{ firstOption() }}</option>
        @for (deck of playerDecks(); track deck.name) {
          <option [value]="deck.name" [selected]="isPlayer() && value()?.name === deck.name">
            {{ deckLabel(deck.formatName, deck.name) }}
          </option>
        }
      </select>
    </div>
    @if (library()) {
      <div class="gd-play-form__row">
        <div class="gd-play-form__label">
          {{ libraryLabel }}
          <button type="button" class="gd-play-form__info" title="What is this?" (click)="help.toggle('library')">
            i
          </button>
        </div>
        <select class="gd-play-form__field" data-deck="library" (change)="picked($event, 'library')">
          <option value="" [selected]="isPlayer() || value() === null">{{ chooseLibrary }}</option>
          @for (deck of libraryDecks(); track deck.name) {
            <option [value]="deck.name" [selected]="!isPlayer() && value()?.name === deck.name">
              {{ deckLabel(deck.formatName, deck.name) }}
            </option>
          }
        </select>
      </div>
      @if (help.shown('library')) {
        <div class="gd-play-form__info-text">{{ libraryHelp }}</div>
      }
    }
  `,
})
export class DeckPickerComponent {
  readonly label = input(YOUR_DECK);
  readonly playerDecks = input.required<readonly HallDeckOption[]>();
  readonly libraryDecks = input<readonly HallDeckOption[]>([]);
  readonly library = input(true);
  readonly fixedFirstOption = input<string | null>(null);
  readonly value = input<HallDeckChoice | null>(null);
  readonly deckChange = output<HallDeckChoice | null>();

  protected readonly help = new HelpToggles();
  protected readonly libraryLabel = SELECT_LIBRARY_DECK;
  protected readonly chooseLibrary = CHOOSE_LIBRARY_DECK;
  protected readonly libraryHelp = HELP_LIBRARY;
  protected readonly deckLabel = deckLabel;

  protected isPlayer(): boolean {
    return this.value()?.source !== 'library';
  }

  protected firstOption(): string {
    return this.fixedFirstOption() ?? (this.playerDecks().length > 0 ? CHOOSE_DECK : NO_DECKS_YET);
  }

  protected picked(event: Event, source: 'player' | 'library'): void {
    const name = fieldValue(event);
    this.deckChange.emit(name === '' ? null : { name, source });
  }
}
