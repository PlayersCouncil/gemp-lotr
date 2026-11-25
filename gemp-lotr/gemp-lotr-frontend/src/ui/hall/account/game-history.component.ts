import { NgTemplateOutlet } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  ANY_FORMAT,
  APPLY_FILTERS,
  CLEAR_FILTERS,
  EVENT_MAX,
  EVENT_PLACEHOLDER,
  FILTER_LABELS,
  GAME_HISTORY_HEADING,
  HISTORY_COLUMNS,
  OPPONENT_MAX,
  OPPONENT_PLACEHOLDER,
  PAGER,
  REPLAY_LINK,
  REPLAY_NOT_STORED,
  REPLAY_TITLE,
} from '../../../core/hall/account/account-messages';
import { GameHistoryFilters, NO_GAME_HISTORY_FILTERS } from '../../../core/hall/account/account-models';
import { GameHistory } from '../../../core/hall/account/game-history.service';

/**
 * My Account > My Game History (gameHistory.html, gameHistoryUi.js GameHistoryUI): the filters form (Format, Opponent,
 * Event with its suggestions, From, To, Apply, Clear), a pager, the status line, the table and the pager again.
 * What is typed and not applied stays the form's; Clear empties it.
 *
 * DOM contract: `gd-account-history.gd-account-history` holding `h1`, `form.gd-account-history__filters` (a
 * `span.gd-account-history__field` per filter: `label` and `select[name=format]` / `input[name=opponent|event|from|to]`;
 * `datalist#gd-account-history-events`; `span.gd-account-history__buttons` with `button[type=submit]` Apply and
 * `button[data-action=clear]`), two `div.gd-account-history__pager` (hidden with one page; `button[data-page=first|
 * previous|next|last]` with `aria-label`, `span.gd-account-history__page`), `div.gd-account-history__status`
 * (`role=status`), and `div.gd-account-history__table` holding `table.gd-account-history__grid` once a page has come
 * (`th[title]`; a row per game: `td[data-column]`, the result in `span[data-result=W|L]`, the replay
 * `a.gd-account-history__replay` (`target=_blank`) or `i` "not stored"; or one `td.gd-account-history__empty`).
 */
@Component({
  selector: 'gd-account-history',
  styleUrl: './game-history.component.css',
  host: { class: 'gd-account-history' },
  template: `
    <h1>{{ heading }}</h1>
    <form class="gd-account-history__filters" autocomplete="off" (submit)="apply($event)">
      <span class="gd-account-history__field">
        <label for="gd-account-history-format">{{ labels.format }}</label>
        <select id="gd-account-history-format" name="format" [value]="form().format" (change)="set('format', $event)">
          <option value="">{{ anyFormat }}</option>
          @for (format of history.formats(); track format) {
            <option [value]="format" [selected]="format === form().format">{{ format }}</option>
          }
        </select>
      </span>
      <span class="gd-account-history__field">
        <label for="gd-account-history-opponent">{{ labels.opponent }}</label>
        <input
          id="gd-account-history-opponent"
          type="text"
          name="opponent"
          [attr.maxlength]="opponentMax"
          [placeholder]="opponentPlaceholder"
          [value]="form().opponent"
          (input)="set('opponent', $event)"
        />
      </span>
      <span class="gd-account-history__field">
        <label for="gd-account-history-event">{{ labels.event }}</label>
        <input
          id="gd-account-history-event"
          type="text"
          name="event"
          list="gd-account-history-events"
          [attr.maxlength]="eventMax"
          [placeholder]="eventPlaceholder"
          [value]="form().event"
          (input)="set('event', $event)"
        />
      </span>
      <datalist id="gd-account-history-events">
        @for (event of history.events(); track event.value) {
          <option [value]="event.value">{{ event.label ?? '' }}</option>
        }
      </datalist>
      <span class="gd-account-history__field">
        <label for="gd-account-history-from">{{ labels.from }}</label>
        <input
          id="gd-account-history-from"
          type="date"
          name="from"
          [value]="form().from"
          (input)="set('from', $event)"
        />
      </span>
      <span class="gd-account-history__field">
        <label for="gd-account-history-to">{{ labels.to }}</label>
        <input id="gd-account-history-to" type="date" name="to" [value]="form().to" (input)="set('to', $event)" />
      </span>
      <span class="gd-account-history__buttons">
        <button type="submit" class="gd-button gd-account-history__button">{{ applyLabel }}</button>
        <button type="button" class="gd-button gd-account-history__button" data-action="clear" (click)="clear()">
          {{ clearLabel }}
        </button>
      </span>
    </form>
    <ng-template #pager>
      <div class="gd-account-history__pager" [hidden]="!history.pager().shown">
        <button
          type="button"
          class="gd-button gd-account-history__button"
          data-page="first"
          [attr.aria-label]="pagerWords.first.label"
          [disabled]="history.pager().firstDisabled"
          (click)="history.first()"
        >
          {{ pagerWords.first.text }}
        </button>
        <button
          type="button"
          class="gd-button gd-account-history__button"
          data-page="previous"
          [attr.aria-label]="pagerWords.previous.label"
          [disabled]="history.pager().previousDisabled"
          (click)="history.previous()"
        >
          {{ pagerWords.previous.text }}
        </button>
        <span class="gd-account-history__page">{{ history.pager().text }}</span>
        <button
          type="button"
          class="gd-button gd-account-history__button"
          data-page="next"
          [attr.aria-label]="pagerWords.next.label"
          [disabled]="history.pager().nextDisabled"
          (click)="history.next()"
        >
          {{ pagerWords.next.text }}
        </button>
        <button
          type="button"
          class="gd-button gd-account-history__button"
          data-page="last"
          [attr.aria-label]="pagerWords.last.label"
          [disabled]="history.pager().lastDisabled"
          (click)="history.last()"
        >
          {{ pagerWords.last.text }}
        </button>
      </div>
    </ng-template>
    <ng-container *ngTemplateOutlet="pager" />
    <div class="gd-account-history__status" role="status" aria-live="polite">{{ history.status() }}</div>
    <div class="gd-account-history__table">
      @if (history.rows(); as rows) {
        <table class="gd-account-history__grid">
          <thead>
            <tr>
              @for (column of columns; track column.key) {
                <th scope="col" [attr.data-column]="column.key" [attr.title]="column.hint">{{ column.title }}</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (row of rows; track $index) {
              <tr>
                <td data-column="date">
                  <span [attr.title]="row.dateTitle">{{ row.date }}</span>
                </td>
                <td data-column="length">{{ row.length }}</td>
                <td data-column="result">
                  <span [attr.data-result]="row.result" [attr.title]="row.resultTitle">{{ row.result }}</span>
                </td>
                <td data-column="reason">
                  <span [attr.title]="row.reasonTitle">{{ row.reason }}</span>
                </td>
                <td data-column="opponent">{{ row.opponent }}</td>
                <td data-column="format">{{ row.format }}</td>
                <td data-column="event">{{ row.event }}</td>
                <td data-column="deck">
                  <div class="gd-account-history__deck">{{ row.deck }}</div>
                </td>
                <td data-column="replay">
                  @if (row.replayUrl; as url) {
                    <a
                      class="gd-account-history__replay"
                      target="_blank"
                      rel="noopener"
                      [href]="url"
                      [attr.title]="replayTitle"
                      >{{ replayLink }}</a
                    >
                  } @else {
                    <i>{{ notStored }}</i>
                  }
                </td>
              </tr>
            } @empty {
              <tr>
                <td class="gd-account-history__empty" [attr.colspan]="columns.length">{{ history.emptyRow() }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
    <ng-container *ngTemplateOutlet="pager" />
  `,
  imports: [NgTemplateOutlet],
})
export class GameHistoryComponent {
  protected readonly history = inject(GameHistory);
  /** The form as typed (not applied until Apply). */
  protected readonly form = signal<GameHistoryFilters>(NO_GAME_HISTORY_FILTERS);

  protected readonly heading = GAME_HISTORY_HEADING;
  protected readonly labels = FILTER_LABELS;
  protected readonly anyFormat = ANY_FORMAT;
  protected readonly opponentPlaceholder = OPPONENT_PLACEHOLDER;
  protected readonly opponentMax = OPPONENT_MAX;
  protected readonly eventPlaceholder = EVENT_PLACEHOLDER;
  protected readonly eventMax = EVENT_MAX;
  protected readonly applyLabel = APPLY_FILTERS;
  protected readonly clearLabel = CLEAR_FILTERS;
  protected readonly pagerWords = PAGER;
  protected readonly columns = HISTORY_COLUMNS;
  protected readonly replayLink = REPLAY_LINK;
  protected readonly replayTitle = REPLAY_TITLE;
  protected readonly notStored = REPLAY_NOT_STORED;

  protected set(field: keyof GameHistoryFilters, event: Event): void {
    const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
    this.form.update((form) => ({ ...form, [field]: value }));
  }

  protected apply(event: Event): void {
    event.preventDefault();
    this.history.apply(this.form());
  }

  protected clear(): void {
    this.form.set(NO_GAME_HISTORY_FILTERS);
    this.history.clear();
  }
}
