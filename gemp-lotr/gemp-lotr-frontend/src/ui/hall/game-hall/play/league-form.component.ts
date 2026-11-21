import { Component, DestroyRef, ElementRef, effect, inject, output, signal, untracked, viewChild } from '@angular/core';
import { provideCardInteractions } from '../../../../core/cards/card-interactions';
import { EventsApi } from '../../../../core/hall/events/events-api.service';
import * as eventWords from '../../../../core/hall/events/events-messages';
import { LeagueDetail } from '../../../../core/hall/events/events-models';
import { LeagueResults } from '../../../../core/hall/events/league-results.service';
import { pageMarkup } from '../../../../core/hall/hall-dialogs.service';
import { LeagueTableFlow } from '../../../../core/hall/play/league-table.service';
import * as words from '../../../../core/hall/play/play-messages';
import { HallDeckChoice } from '../../../../core/hall/tables/hall-table-models';
import { HallTablesService } from '../../../../core/hall/tables/hall-tables.service';
import { PageNavigation } from '../../../../core/platform/page-navigation';
import { CardInfoComponent } from '../../../shared/card-info/card-info.component';
import { MarkupComponent } from '../../../shared/chat/markup.component';
import { DialogComponent } from '../../../shared/dialog/dialog.component';
import { EventRowComponent } from '../../events/event-row.component';
import { EventsButtonComponent } from '../../events/events-button.component';
import { EVENTS_AUTO_ZOOM_COOKIE, EventsCardActions } from '../../events/events-tab.component';
import { LeagueDetailComponent } from '../../events/league-detail.component';
import { PriceComponent } from '../../shared/price.component';
import { DeckPickerComponent } from './deck-picker.component';
import { fieldValue, HelpToggles } from './play-parts';
import { ResultBoxComponent } from './result-box.component';

/**
 * Play ▸ Open League Table (CreateLeagueTable, #create-league-table): the heading with its (i), the blurb, the League
 * (the leagues the player is in), the deck, Create Table and the result; then "Join Leagues": the next steps after a
 * join, the hint, and the Events tab's league list (its rows and league detail, with their join question), which
 * joins a league: the new league is chosen and the next steps offered (LeagueTableFlow.leagueJoined). The list has its
 * own state here (LeagueResults), as the legacy panel had its own LeagueResultsUI. `created` closes the overlay.
 *
 * DOM contract: `gd-league-form.gd-play-form` holding `div.gd-play-form__inner` (`h1` (with `button.gd-play-form__info`), the help text,
 * `div.gd-play-form__blurb`, the League row (`select[data-field=league]`), `gd-deck-picker`,
 * `button.gd-play-form__create`, `gd-result-box`), `h1.gd-play-form__join-heading`, then
 * `div.gd-play-form__league-panel` holding `div.gd-play-form__next-steps` (`data-error` for a failed join; its
 * message, `div.gd-play-form__next-buttons` of `button[data-step=create-table]`, `button[data-step=go-to-draft]` and
 * `a[data-step=deckbuilder]`, and the Limited hint), `div.gd-play-form__hint` and `div.gd-play-form__league-list` (a
 * `gd-event-row[data-kind=league]` each, with `gd-league-detail` when open); the join question
 * `gd-dialog[data-dialog=league-join]` and `gd-card-info`.
 */
@Component({
  selector: 'gd-league-form',
  imports: [
    CardInfoComponent,
    DeckPickerComponent,
    DialogComponent,
    EventRowComponent,
    EventsButtonComponent,
    LeagueDetailComponent,
    MarkupComponent,
    PriceComponent,
    ResultBoxComponent,
  ],
  providers: [
    EventsApi,
    LeagueResults,
    provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE }),
  ],
  host: { class: 'gd-play-form' },
  template: `
    <div class="gd-play-form__inner">
      <h1 #form>
        {{ w.LEAGUE_HEADING }}
        <button type="button" class="gd-play-form__info" [title]="w.INFO_TOGGLE_TITLE" (click)="help.toggle('league')">
          i
        </button>
      </h1>
      @if (help.shown('league')) {
        <div class="gd-play-form__info-text">{{ w.HELP_LEAGUE }}</div>
      }
      <div class="gd-play-form__blurb"><gd-markup [nodes]="blurb" /></div>
      <div class="gd-play-form__row">
        <div class="gd-play-form__label">{{ w.LEAGUE }}</div>
        <select #leagueField class="gd-play-form__field" data-field="league" (change)="leaguePicked(value($event))">
          @for (option of flow.options(); track option.value) {
            <option [value]="option.value" [disabled]="option.disabled" [selected]="option.value === flow.league()">
              {{ option.label }}
            </option>
          }
        </select>
      </div>
      <gd-deck-picker
        [label]="w.DECK"
        [playerDecks]="tables.playerDecks()"
        [libraryDecks]="tables.libraryDecks()"
        [value]="deck()"
        (deckChange)="deckPicked($event)"
      />
      <button type="button" class="gd-play-form__create" (click)="create()">{{ w.CREATE_TABLE_BUTTON }}</button>
      <gd-result-box [result]="flow.result()" />
    </div>

    <h1 class="gd-play-form__join-heading">{{ w.JOIN_LEAGUES_HEADING }}</h1>
    <div class="gd-play-form__league-panel">
      @if (flow.nextSteps(); as next) {
        <div class="gd-play-form__next-steps" [attr.data-error]="next.isError ? '' : null">
          <div>{{ next.message }}</div>
          @if (next.steps.length > 0) {
            <div class="gd-play-form__next-buttons">
              @for (step of next.steps; track step.kind) {
                @switch (step.kind) {
                  @case ('create-table') {
                    <button
                      type="button"
                      class="gd-play-form__button"
                      data-step="create-table"
                      (click)="createIn(step.leagueCode)"
                    >
                      {{ w.NEXT_CREATE_TABLE }}
                    </button>
                  }
                  @case ('go-to-draft') {
                    <button type="button" class="gd-play-form__button" data-step="go-to-draft" (click)="open(step.url)">
                      {{ w.NEXT_GO_TO_DRAFT }}
                    </button>
                  }
                  @case ('deckbuilder') {
                    <a
                      class="gd-play-form__button"
                      data-step="deckbuilder"
                      [href]="step.url"
                      target="_blank"
                      rel="noopener"
                    >
                      {{ w.NEXT_OPEN_DECKBUILDER }}
                    </a>
                  }
                }
              }
            </div>
          }
          @if (next.hint; as hint) {
            <div class="gd-play-form__hint">{{ hint }}</div>
          }
        </div>
      }
      <div class="gd-play-form__hint">{{ w.LEAGUE_LIST_HINT }}</div>
      <div class="gd-play-form__league-list">
        @switch (leagues.list().kind) {
          @case ('error') {
            <div class="gd-play-form__league-error">{{ listError() }}</div>
          }
          @case ('loaded') {
            @for (row of leagues.rows(); track row.key) {
              <gd-event-row
                kind="league"
                [key]="row.key"
                [drawer]="row.drawer"
                [title]="row.title()"
                [date]="row.date()"
              >
                @if (row.drawer.content(); as content) {
                  @if (content.kind === 'data') {
                    <gd-league-detail [league]="asLeague(content.data)" />
                  }
                }
              </gd-event-row>
            } @empty {
              <i>{{ noLeagues }}</i>
            }
          }
        }
      </div>
    </div>
    <gd-dialog
      name="league-join"
      [heading]="e.JOIN_DIALOG_TITLE"
      [open]="leagues.joinQuestion() !== null"
      (closed)="leagues.answerJoin(false)"
    >
      @if (leagues.joinQuestion(); as question) {
        <p>{{ e.JOIN_QUESTION_BEFORE_PRICE }}<gd-price [price]="question.cost" />{{ e.JOIN_QUESTION_AFTER_PRICE }}</p>
      }
      <div class="gd-dialog__actions" gdDialogActions>
        <button gdEventsButton data-action="yes" (click)="leagues.answerJoin(true)">{{ e.JOIN_YES }}</button>
        <button gdEventsButton data-action="no" (click)="leagues.answerJoin(false)">{{ e.JOIN_NO }}</button>
      </div>
    </gd-dialog>
    <gd-card-info />
  `,
})
export class LeagueFormComponent {
  protected readonly flow = inject(LeagueTableFlow);
  protected readonly tables = inject(HallTablesService);
  protected readonly leagues = inject(LeagueResults);
  private readonly navigation = inject(PageNavigation);
  readonly created = output();

  protected readonly w = words;
  protected readonly e = eventWords;
  protected readonly blurb = pageMarkup(words.LEAGUE_BLURB);
  protected readonly help = new HelpToggles();
  protected readonly value = fieldValue;
  protected readonly noLeagues = eventWords.NO_CURRENT_LEAGUES;
  protected readonly deck = signal<HallDeckChoice | null>(null);
  private deckByHand = false;
  private readonly form = viewChild<ElementRef<HTMLElement>>('form');
  private readonly leagueField = viewChild<ElementRef<HTMLSelectElement>>('leagueField');

  constructor() {
    void this.flow.show();
    void this.leagues.loadList();
    const joined = this.leagues.joined$.subscribe((code) => void this.flow.leagueJoined(code));
    inject(DestroyRef).onDestroy(() => joined.unsubscribe());
    // the deck remembered for the league chosen, once the lists have arrived
    effect(() => {
      const league = this.flow.league();
      this.tables.playerDecks();
      this.tables.libraryDecks();
      untracked(() => {
        if (!this.deckByHand) {
          this.deck.set(this.flow.deckFor(league));
        }
      });
    });
  }

  protected listError(): string | null {
    const state = this.leagues.list();
    return state.kind === 'error' ? state.text : null;
  }

  protected asLeague(data: unknown): LeagueDetail {
    return data as LeagueDetail;
  }

  /** A league picked by hand brings back its deck unless the deck was picked by hand (restoreUnlessChosen). */
  protected leaguePicked(code: string): void {
    this.flow.league.set(code);
  }

  protected deckPicked(deck: HallDeckChoice | null): void {
    this.deck.set(deck);
    this.deckByHand = deck !== null;
  }

  /** "Create a table in this league": chosen, its deck, and the form scrolled into view. */
  protected createIn(code: string): void {
    this.deckByHand = false;
    this.flow.createTableIn(code);
    this.leagueField()?.nativeElement.focus();
    this.form()?.nativeElement.scrollIntoView({ block: 'start' });
  }

  protected open(url: string): void {
    this.navigation.openInNewTab(url);
  }

  protected async create(): Promise<void> {
    if (await this.flow.submit(this.deck())) {
      this.created.emit();
    }
  }
}
