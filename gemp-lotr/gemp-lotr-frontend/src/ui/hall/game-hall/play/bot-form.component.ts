import { Component, computed, effect, inject, output, signal, untracked } from '@angular/core';
import { pageMarkup } from '../../../../core/hall/hall-dialogs.service';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import * as words from '../../../../core/hall/play/play-messages';
import { HallDeckChoice } from '../../../../core/hall/tables/hall-table-models';
import { RANDOM_STARTER } from '../../../../core/hall/tables/hall-tables-messages';
import { BOT_DECK_FLOW, HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { MarkupComponent } from '../../../shared/chat/markup.component';
import { DeckPickerComponent } from './deck-picker.component';
import { FormatInfoComponent } from './format-info.component';
import { fieldValue, HelpToggles } from './play-parts';
import { ResultBoxComponent } from './result-box.component';

/**
 * Play ▸ Play Against Bots (CreateBotTable, #create-bot-table): the blurb, the format (Fellowship Block until Unlock
 * Formats), the bot's deck (a random FotR starter or one of the player's), the player's deck, Create Table and the
 * result. `created` reports a table created (the overlay closes; the game opens from the hall).
 *
 * DOM contract: `gd-bot-form.gd-play-form` holding `h1`, `div.gd-play-form__blurb`, the format row (with
 * `button.gd-play-form__unlock` until unlocked and `select[data-field=format]`, disabled until then), its format (i),
 * `gd-deck-picker[data-deck-for=bot]` (no library), `gd-deck-picker[data-deck-for=player]`,
 * `button.gd-play-form__create` and `gd-result-box`.
 */
@Component({
  selector: 'gd-bot-form',
  imports: [DeckPickerComponent, FormatInfoComponent, MarkupComponent, ResultBoxComponent],
  host: { class: 'gd-play-form', 'data-heading': 'outer' },
  template: `
    <h1>{{ w.BOT_HEADING }}</h1>
    <div class="gd-play-form__blurb"><gd-markup [nodes]="blurb" /></div>
    <div class="gd-play-form__row">
      <div class="gd-play-form__label">
        {{ w.TABLE_FORMAT }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('format')">
          i
        </button>
      </div>
      @if (!overlay.botFormatsUnlocked()) {
        <button type="button" class="gd-play-form__unlock" (click)="overlay.unlockBotFormats()">
          {{ w.UNLOCK_FORMATS }}
        </button>
      }
      <select
        class="gd-play-form__field"
        data-field="format"
        [disabled]="!overlay.botFormatsUnlocked()"
        (change)="format.set(value($event))"
      >
        @for (option of tables.formats(); track option.code) {
          <option [value]="option.code" [selected]="option.code === format()">{{ option.name }}</option>
        }
      </select>
    </div>
    @if (help.shown('format')) {
      <gd-format-info [info]="tables.formatInfo(format() || null)" (helpLink)="helpLink.emit($event)" />
    }
    <gd-deck-picker
      data-deck-for="bot"
      [label]="w.BOT_DECK"
      [library]="false"
      [fixedFirstOption]="randomStarter"
      [playerDecks]="tables.playerDecks()"
      [value]="botDeck()"
      (deckChange)="botDeck.set($event)"
    />
    <gd-deck-picker
      data-deck-for="player"
      [playerDecks]="tables.playerDecks()"
      [libraryDecks]="tables.libraryDecks()"
      [value]="deck()"
      (deckChange)="picked($event)"
    />
    <button type="button" class="gd-play-form__create" [disabled]="tables.busy()" (click)="create()">
      {{ w.CREATE_TABLE_BUTTON }}
    </button>
    <gd-result-box [result]="tables.results().bot" />
  `,
})
export class BotFormComponent {
  protected readonly tables = inject(HallTablesService);
  protected readonly overlay = inject(PlayOverlay);
  readonly created = output();
  readonly helpLink = output<string>();

  protected readonly w = words;
  protected readonly blurb = pageMarkup(words.BOT_BLURB);
  protected readonly help = new HelpToggles();
  protected readonly randomStarter = RANDOM_STARTER;
  protected readonly value = fieldValue;

  protected readonly format = signal(this.overlay.botFormat(null));
  protected readonly deck = signal<HallDeckChoice | null>(null);
  protected readonly botDeck = signal<HallDeckChoice | null>(null);
  private deckByHand = false;
  private readonly codes = computed(() => this.tables.formats().map((f) => f.code));

  constructor() {
    effect(() => {
      this.codes();
      this.tables.playerDecks();
      this.tables.libraryDecks();
      untracked(() => {
        if (!this.deckByHand && this.deck() === null) {
          this.deck.set(this.tables.initialDeck(BOT_DECK_FLOW, this.format()));
        }
      });
    });
  }

  protected picked(deck: HallDeckChoice | null): void {
    this.deck.set(deck);
    this.deckByHand = true;
  }

  protected async create(): Promise<void> {
    const close = await this.tables.createBot({
      format: this.format(),
      deck: this.deck(),
      botDeck: this.botDeck()?.name ?? '',
    });
    if (close) {
      this.created.emit();
    }
  }
}
