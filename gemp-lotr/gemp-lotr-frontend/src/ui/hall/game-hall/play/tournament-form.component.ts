import { Component, computed, effect, inject, output, signal, untracked } from '@angular/core';
import { CreateTournament } from '../../../../core/hall/play/create-tournament.service';
import { pageMarkup } from '../../../../core/hall/hall-dialogs.service';
import * as words from '../../../../core/hall/play/play-messages';
import {
  draftTimerLabel,
  draftTimerOf,
  formatsFor,
  PAIRING_TYPES,
  PairingType,
  READY_CHECKS,
  selectedFormat,
  TOURNAMENT_TYPES,
  TournamentType,
  tournamentFields,
} from '../../../../core/hall/play/tournament-form';
import { draftFormatUrl } from '../../../../core/hall/queues/hall-queue-parsers';
import { HallDeckChoice } from '../../../../core/hall/tables/hall-table-models';
import { HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { PageNavigation } from '../../../../core/platform/page-navigation';
import { MarkupComponent } from '../../../shared/chat/markup.component';
import { DeckPickerComponent } from './deck-picker.component';
import { FormatInfoComponent } from './format-info.component';
import { checked, fieldValue, HelpToggles } from './play-parts';
import { ResultBoxComponent } from './result-box.component';

/**
 * Play ▸ Create Tournament (CreateTournament, #create-tournament): the blurb, the type, and once a type is chosen the
 * format, the draft info (a live draft's format page), the number of players, the advanced settings (shown on
 * demand), the deck (Constructed), Host Tournament and the result. Which fields show, the 2-player rule and the
 * defaults are the core's (tournament-form.ts). `created` closes the overlay.
 *
 * DOM contract: `gd-tournament-form.gd-play-form` holding `div.gd-play-form__inner` with `h1`, `div.gd-play-form__blurb`, the type row
 * (`select[data-field=type]`), then while a type is chosen `div.gd-play-form__startup` with `select[data-field=format]`,
 * `button.gd-play-form__draft-info` (live drafts), `input[type=number][data-field=players]`,
 * `button.gd-play-form__advanced-toggle` and, while shown, `div.gd-play-form__advanced` (`select[data-field=pairing]`,
 * `input[data-field=deckbuilding]`, `input[type=checkbox][data-field=competitive|early-start]`,
 * `select[data-field=ready-check]`, `select[data-field=draft-timer]`), `hr`, `gd-deck-picker` (Constructed),
 * `button.gd-play-form__create` and `gd-result-box`; each (i)'s text after its row.
 */
@Component({
  selector: 'gd-tournament-form',
  imports: [DeckPickerComponent, FormatInfoComponent, MarkupComponent, ResultBoxComponent],
  host: { class: 'gd-play-form' },
  template: `
    <div class="gd-play-form__inner">
      <h1>{{ w.TOURNAMENT_HEADING }}</h1>
      <div class="gd-play-form__blurb"><gd-markup [nodes]="blurb" /></div>
      <div class="gd-play-form__row">
        <div class="gd-play-form__label">{{ w.TOURNAMENT_TYPE }}</div>
        <select class="gd-play-form__field" data-field="type" (change)="setType(value($event))">
          <option value="" disabled [selected]="form().type === ''">{{ w.SELECT_GAME_TYPE }}</option>
          @for (type of types; track type.value) {
            <option [value]="type.value" [selected]="type.value === form().type">{{ type.label }}</option>
          }
        </select>
      </div>
      @if (fields().startup) {
        <div class="gd-play-form__startup">
          <div class="gd-play-form__row">
            <div class="gd-play-form__label">
              {{ w.FORMAT }}
              <button
                type="button"
                class="gd-play-form__info"
                [title]="w.INFO_TOGGLE_TITLE"
                (click)="help.toggle('format')"
              >
                i
              </button>
            </div>
            <select class="gd-play-form__field" data-field="format" (change)="setFormat(value($event))">
              @for (format of formats(); track format.code) {
                <option [value]="format.code" [selected]="format.code === form().format">{{ format.name }}</option>
              }
            </select>
          </div>
          @if (help.shown('format') && tournament.formatInfo(); as info) {
            <gd-format-info [info]="info" (helpLink)="helpLink.emit($event)" />
          }
          @if (fields().draftInfo) {
            <div class="gd-play-form__row">
              <div class="gd-play-form__label">{{ w.DRAFT_INFO }}</div>
              <button type="button" class="gd-play-form__draft-info" (click)="openDraftFormat()">
                {{ formatName() }}
              </button>
            </div>
          }
          <div class="gd-play-form__row">
            <div class="gd-play-form__label">
              {{ w.NUMBER_OF_PLAYERS }}
              <button
                type="button"
                class="gd-play-form__info"
                [title]="w.INFO_TOGGLE_TITLE"
                (click)="help.toggle('players')"
              >
                i
              </button>
            </div>
            <input
              class="gd-play-form__field"
              data-field="players"
              type="number"
              min="1"
              [attr.max]="fields().maxPlayers"
              [value]="form().players"
              (input)="tournament.setPlayers(value($event))"
            />
          </div>
          @if (help.shown('players')) {
            <div class="gd-play-form__info-text">{{ w.HELP_PLAYERS }}</div>
          }
          <button
            type="button"
            class="gd-play-form__advanced-toggle"
            (click)="tournament.patch({ advancedShown: !form().advancedShown })"
          >
            {{ form().advancedShown ? w.HIDE_ADVANCED : w.SHOW_ADVANCED }}
          </button>
          @if (form().advancedShown) {
            <div class="gd-play-form__advanced">
              <div class="gd-play-form__row">
                <div class="gd-play-form__label">
                  {{ w.PAIRING_TYPE }}
                  <button
                    type="button"
                    class="gd-play-form__info"
                    [title]="w.INFO_TOGGLE_TITLE"
                    (click)="help.toggle('pairing')"
                  >
                    i
                  </button>
                </div>
                <select
                  class="gd-play-form__field"
                  data-field="pairing"
                  [disabled]="!fields().largeSettingsEnabled"
                  (change)="tournament.patch({ pairing: asPairing(value($event)) })"
                >
                  @for (pairing of pairings; track pairing.value) {
                    <option [value]="pairing.value" [selected]="pairing.value === form().pairing">
                      {{ pairing.label }}
                    </option>
                  }
                </select>
              </div>
              @if (help.shown('pairing')) {
                <div class="gd-play-form__info-text">{{ w.HELP_PAIRING }}</div>
              }
              @if (fields().deckbuilding) {
                <div class="gd-play-form__row">
                  <div class="gd-play-form__label">
                    {{ w.DECKBUILDING_DURATION }}
                    <button
                      type="button"
                      class="gd-play-form__info"
                      [title]="w.INFO_TOGGLE_TITLE"
                      (click)="help.toggle('deckbuilding')"
                    >
                      i
                    </button>
                  </div>
                  <input
                    class="gd-play-form__field"
                    data-field="deckbuilding"
                    type="number"
                    min="5"
                    [value]="form().deckbuilding"
                    (input)="tournament.patch({ deckbuilding: value($event) })"
                  />
                </div>
                @if (help.shown('deckbuilding')) {
                  <div class="gd-play-form__info-text">{{ w.HELP_DECKBUILDING }}</div>
                }
              }
              <div class="gd-play-form__row">
                <div class="gd-play-form__label">
                  {{ w.COMPETITIVE_MODE }}
                  <button
                    type="button"
                    class="gd-play-form__info"
                    [title]="w.INFO_TOGGLE_TITLE"
                    (click)="help.toggle('competitive')"
                  >
                    i
                  </button>
                </div>
                <input
                  type="checkbox"
                  data-field="competitive"
                  [checked]="form().competitive"
                  (change)="tournament.patch({ competitive: isChecked($event) })"
                />
                <i>{{ w.COMPETITIVE_NOTE }}</i>
              </div>
              @if (help.shown('competitive')) {
                <div class="gd-play-form__info-text">{{ w.HELP_COMPETITIVE }}</div>
              }
              <div class="gd-play-form__row">
                <div class="gd-play-form__label">
                  {{ w.EARLY_START }}
                  <button
                    type="button"
                    class="gd-play-form__info"
                    [title]="w.INFO_TOGGLE_TITLE"
                    (click)="help.toggle('early')"
                  >
                    i
                  </button>
                </div>
                <input
                  type="checkbox"
                  data-field="early-start"
                  [checked]="form().earlyStart"
                  (change)="tournament.patch({ earlyStart: isChecked($event) })"
                />
                <i>{{ w.EARLY_START_NOTE }}</i>
              </div>
              @if (help.shown('early')) {
                <div class="gd-play-form__info-text">{{ w.HELP_EARLY_START }}</div>
              }
              <div class="gd-play-form__row">
                <div class="gd-play-form__label">
                  {{ w.READY_CHECK }}
                  <button
                    type="button"
                    class="gd-play-form__info"
                    [title]="w.INFO_TOGGLE_TITLE"
                    (click)="help.toggle('ready')"
                  >
                    i
                  </button>
                </div>
                <select
                  class="gd-play-form__field"
                  data-field="ready-check"
                  [disabled]="!fields().largeSettingsEnabled"
                  (change)="tournament.patch({ readyCheck: +value($event) })"
                >
                  @for (check of readyChecks; track check.value) {
                    <option [value]="check.value" [selected]="check.value === form().readyCheck">
                      {{ check.label }}
                    </option>
                  }
                </select>
              </div>
              @if (help.shown('ready')) {
                <div class="gd-play-form__info-text">{{ w.HELP_READY_CHECK }}</div>
              }
              @if (fields().draftTimer) {
                <div class="gd-play-form__row">
                  <div class="gd-play-form__label">
                    {{ w.DRAFT_TIMER }}
                    <button
                      type="button"
                      class="gd-play-form__info"
                      [title]="w.INFO_TOGGLE_TITLE"
                      (click)="help.toggle('timer')"
                    >
                      i
                    </button>
                  </div>
                  <select
                    class="gd-play-form__field"
                    data-field="draft-timer"
                    (change)="tournament.patch({ draftTimer: value($event) })"
                  >
                    @for (timer of tournament.formats()?.draftTimerTypes ?? []; track timer) {
                      <option [value]="timer" [selected]="timer === draftTimer()">{{ timerLabel(timer) }}</option>
                    }
                  </select>
                </div>
                @if (help.shown('timer')) {
                  <div class="gd-play-form__info-text">{{ w.HELP_DRAFT_TIMER }}</div>
                }
              }
            </div>
          }
          <hr />
          @if (fields().deck) {
            <gd-deck-picker
              [playerDecks]="tables.playerDecks()"
              [libraryDecks]="tables.libraryDecks()"
              [value]="deck()"
              (deckChange)="deckPicked($event)"
            />
          }
          <button type="button" class="gd-play-form__create" [disabled]="!tournament.canSubmit()" (click)="host()">
            {{ w.HOST_TOURNAMENT }}
          </button>
          <gd-result-box [result]="tournament.result()" />
        </div>
      } @else {
        @if (tournament.result().kind !== 'ready') {
          <gd-result-box [result]="tournament.result()" />
        }
      }
    </div>
  `,
})
export class TournamentFormComponent {
  protected readonly tournament = inject(CreateTournament);
  protected readonly tables = inject(HallTablesService);
  private readonly navigation = inject(PageNavigation);
  readonly created = output();
  readonly helpLink = output<string>();

  protected readonly w = words;
  protected readonly blurb = pageMarkup(words.TOURNAMENT_BLURB);
  protected readonly help = new HelpToggles();
  protected readonly types = TOURNAMENT_TYPES;
  protected readonly pairings = PAIRING_TYPES;
  protected readonly readyChecks = READY_CHECKS;
  protected readonly timerLabel = draftTimerLabel;
  protected readonly value = fieldValue;
  protected readonly isChecked = checked;

  protected readonly form = this.tournament.form;
  protected readonly fields = computed(() => tournamentFields(this.form(), this.tournament.formats()));
  protected readonly formats = computed(() => formatsFor(this.tournament.formats(), this.form().type));
  protected readonly formatName = computed(() => selectedFormat(this.form(), this.tournament.formats())?.name ?? '');
  protected readonly draftTimer = computed(() => draftTimerOf(this.form(), this.tournament.formats()));
  protected readonly deck = signal<HallDeckChoice | null>(null);
  private deckByHand = false;

  constructor() {
    // the deck remembered for the format chosen, unless one was picked by hand (restoreUnlessChosen)
    effect(() => {
      const format = this.form().format;
      this.tables.playerDecks();
      this.tables.libraryDecks();
      untracked(() => {
        if (!this.deckByHand && format !== '') {
          this.deck.set(this.tournament.initialDeck());
        }
      });
    });
  }

  protected setType(type: string): void {
    this.help.hideAll();
    this.tournament.setType(type as TournamentType);
  }

  protected setFormat(code: string): void {
    this.tournament.setFormat(code);
  }

  protected asPairing(value: string): PairingType {
    return value as PairingType;
  }

  protected deckPicked(deck: HallDeckChoice | null): void {
    this.deck.set(deck);
    this.deckByHand = deck !== null;
  }

  /** The live draft's format page in a new tab (.draftFormatInfo). */
  protected openDraftFormat(): void {
    this.navigation.openInNewTab(draftFormatUrl(this.form().format));
  }

  protected async host(): Promise<void> {
    if (await this.tournament.submit(this.deck())) {
      this.created.emit();
    }
  }
}
