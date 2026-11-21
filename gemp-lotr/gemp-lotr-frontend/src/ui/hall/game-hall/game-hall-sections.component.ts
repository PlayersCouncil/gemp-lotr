import { NgTemplateOutlet } from '@angular/common';
import { Component, inject } from '@angular/core';
import { HallBoard } from '../../../core/hall/board/hall-board.service';
import { HALL_SECTION_COLUMNS, HALL_SECTION_TITLES, HallSectionId } from '../../../core/hall/board/hall-board-models';
import { HallSectionSettings } from '../../../core/hall/board/hall-section-settings.service';
import { JoinDialog } from '../../../core/hall/play/join-dialog.service';
import { HallPrizes, HallQueueSectionRow } from '../../../core/hall/queues/hall-queue-models';
import {
  ABANDON_TOURNAMENT,
  DRAFT_FORMAT_TITLE,
  GO_TO_DRAFT,
  JOIN_QUEUE,
  JOIN_TOURNAMENT,
  LEAVE_QUEUE,
  PRIZE_BREAKDOWN,
  REGISTER_DECK,
  SHOW_PRIZES,
  START_NOW,
} from '../../../core/hall/queues/hall-queue-messages';
import { splitPrice } from '../../../core/hall/queues/hall-queue-parsers';
import { HallQueuesService } from '../../../core/hall/queues/hall-queues.service';
import { tableAgeTitle } from '../../../core/hall/tables/hall-table-parsers';
import { HallTableRow } from '../../../core/hall/tables/hall-table-models';
import { HallTablesService } from '../../../core/hall/tables/hall-tables.service';
import { PageNavigation } from '../../../core/platform/page-navigation';

/**
 * The Game Hall tab's sections (hall.html #tablesDiv, hallUi.js drawQueue / drawTournament / drawTable, initTable):
 * Waiting, Playing and Finished Tables, then World Championship Queues and Scheduled Events while they have rows, then
 * Recurring Events. Each header is a button that opens or closes its section (kept in the hallSettings cookie) and
 * shows its count; each section a table of its rows with the legacy columns, and each row its buttons.
 *
 * DOM contract: `gd-game-hall-sections.gd-hall-sections` holding, per section shown, `button.gd-hall-sections__header`
 * (`data-section` = waiting, playing, finished, wc, scheduled, recurring; `aria-expanded`; `aria-controls`) with
 * `span.gd-hall-sections__triangle`, the title and `span.gd-hall-sections__count` "(N)"; then, while open,
 * `div.gd-hall-sections__content` (`id`) holding `table.gd-hall-sections__table` (`data-section`), a header row of `th`
 * (widths from the legacy `<th width>`), and a `tr.gd-hall-sections__row` per row (`data-kind` = table, queue,
 * tournament, event; `data-key`; `data-played` when the viewer sits there or is signed up; `data-invited` for an
 * invitation for the viewer; `data-bold` for the World Championship). In the rows: `i.gd-hall-sections__note` (a
 * table's note), `span.gd-hall-sections__age` (a waiting table's timer, `title` its opening time),
 * `button.gd-hall-sections__hint` (an event name opening its draft format, a prize label opening its breakdown, a
 * tournament's player count opening its players), `span.gd-hall-sections__price` (gold and silver, each followed by
 * `span.gd-hall-sections__coin[data-coin]`), and the actions: `button.gd-hall-sections__button` (`data-action` = join,
 * leave, join-queue, leave-queue, start, ready-check, prizes, go-to-draft, register-deck, abandon, late-join;
 * `disabled` when waiting for others) or `a.gd-hall-sections__button` (`data-action` = play, watch; `target=_blank`).
 */
@Component({
  selector: 'gd-game-hall-sections',
  styleUrl: './game-hall-sections.component.css',
  host: { class: 'gd-hall-sections' },
  template: `
    @for (section of board.shownSections(); track section) {
      <button
        type="button"
        class="gd-hall-sections__header"
        [attr.data-section]="section"
        [attr.aria-expanded]="settings.isOpen(section)"
        [attr.aria-controls]="'gd-hall-section-' + section"
        (click)="settings.toggle(section)"
      >
        <span class="gd-hall-sections__triangle" aria-hidden="true"></span>{{ titles[section]
        }}<span class="gd-hall-sections__count">({{ board.counts()[section] }})</span>
      </button>
      @if (settings.isOpen(section)) {
        <div class="gd-hall-sections__content" [id]="'gd-hall-section-' + section">
          <table class="gd-hall-sections__table" [attr.data-section]="section">
            <tr>
              @for (column of columns[section]; track $index) {
                <th [attr.width]="column.widthPercent + '%'">{{ column.label }}</th>
              }
            </tr>
            @switch (section) {
              @case ('waiting') {
                @for (row of board.sections().waiting; track row.key) {
                  <ng-container *ngTemplateOutlet="boardRow; context: { $implicit: row }" />
                }
              }
              @case ('playing') {
                @for (row of board.sections().playing; track row.key) {
                  <ng-container *ngTemplateOutlet="boardRow; context: { $implicit: row }" />
                }
              }
              @case ('finished') {
                @for (row of board.sections().finished; track row.table.id) {
                  <ng-container *ngTemplateOutlet="tableRow; context: { $implicit: row }" />
                }
              }
              @default {
                @for (row of eventRows(section); track row.queue.id) {
                  <tr class="gd-hall-sections__row" data-kind="event" [attr.data-key]="'queue:' + row.queue.id">
                    <td>{{ row.format }}</td>
                    <td>{{ row.collection }}</td>
                    <td>
                      @if (row.name.draftFormatUrl; as url) {
                        <button type="button" class="gd-hall-sections__hint" [title]="draftTitle" (click)="open(url)">
                          {{ row.name.text }}
                        </button>
                      } @else {
                        {{ row.name.text }}
                      }
                    </td>
                    <td>{{ row.start }}</td>
                    <td>{{ row.system }}</td>
                    <td class="gd-hall-sections__cost">
                      <span class="gd-hall-sections__price"
                        >{{ price(row.cost).gold }}<span class="gd-hall-sections__coin" data-coin="gold"></span>
                        {{ price(row.cost).silver }}<span class="gd-hall-sections__coin" data-coin="silver"></span
                      ></span>
                    </td>
                    <td>
                      @if (row.prizes.lines.length > 0) {
                        <button
                          type="button"
                          class="gd-hall-sections__hint"
                          title="Prizes details"
                          (click)="queues.showPrizes(row.prizes)"
                        >
                          {{ prizeLabel(row.prizes) }}
                        </button>
                      } @else {
                        {{ row.prizes.label }}
                      }
                    </td>
                  </tr>
                }
              }
            }
          </table>
        </div>
      }
    }

    <ng-template #boardRow let-row>
      @switch (row.kind) {
        @case ('table') {
          <ng-container *ngTemplateOutlet="tableRow; context: { $implicit: row.row }" />
        }
        @case ('queue') {
          <tr
            class="gd-hall-sections__row"
            data-kind="queue"
            [attr.data-key]="row.key"
            [attr.data-played]="row.row.played ? '' : null"
            [attr.data-bold]="row.row.bold ? '' : null"
          >
            <td>{{ row.row.format }}</td>
            <td>
              {{ row.row.prefix }}
              @if (row.row.name.draftFormatUrl; as url) {
                <button type="button" class="gd-hall-sections__hint" [title]="draftTitle" (click)="open(url)">
                  {{ row.row.name.text }}
                </button>
              } @else {
                {{ row.row.name.text }}
              }
            </td>
            <td>{{ row.row.start }}</td>
            <td>{{ row.row.players }}</td>
            <td>
              @for (action of row.row.actions; track $index) {
                @switch (action.kind) {
                  @case ('join') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="join-queue"
                      (click)="queues.joinQueue(row.row.queue)"
                    >
                      {{ words.joinQueue }}
                    </button>
                  }
                  @case ('leave') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="leave-queue"
                      (click)="queues.leaveQueue(row.row.queue.id)"
                    >
                      {{ words.leaveQueue }}
                    </button>
                  }
                  @case ('start') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="start"
                      (click)="queues.startQueue(row.row.queue.id)"
                    >
                      {{ words.startNow }}
                    </button>
                  }
                  @case ('ready-check') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="ready-check"
                      [disabled]="action.confirmed"
                      (click)="queues.confirmReady(row.row.queue.id)"
                    >
                      {{ action.label }}
                    </button>
                  }
                  @case ('prizes') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="prizes"
                      (click)="queues.showPrizes(row.row.queue.prizes)"
                    >
                      {{ words.showPrizes }}
                    </button>
                  }
                }
              }
            </td>
          </tr>
        }
        @case ('tournament') {
          <tr
            class="gd-hall-sections__row"
            data-kind="tournament"
            [attr.data-key]="row.key"
            [attr.data-played]="row.row.played ? '' : null"
            [attr.data-bold]="row.row.bold ? '' : null"
          >
            <td>{{ row.row.format }}</td>
            <td>{{ row.row.info }}</td>
            <td>{{ row.row.status }}</td>
            <td>
              @if (row.row.players.count !== undefined) {
                <button
                  type="button"
                  class="gd-hall-sections__hint"
                  title="Competing Players"
                  (click)="queues.showCompetingPlayers(row.row.tournament)"
                >
                  {{ row.row.players.count }}
                </button>
              } @else {
                {{ row.row.players.list }}
              }
            </td>
            <td>
              @for (action of row.row.actions; track $index) {
                @switch (action.kind) {
                  @case ('go-to-draft') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="go-to-draft"
                      (click)="queues.goToDraft(action.url)"
                    >
                      {{ words.goToDraft }}
                    </button>
                  }
                  @case ('register-deck') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="register-deck"
                      (click)="queues.openRegisterDeck(row.row.tournament)"
                    >
                      {{ words.registerDeck }}
                    </button>
                  }
                  @case ('abandon') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="abandon"
                      (click)="queues.abandon(row.row.tournament)"
                    >
                      {{ words.abandon }}
                    </button>
                  }
                  @case ('late-join') {
                    <button
                      type="button"
                      class="gd-hall-sections__button"
                      data-action="late-join"
                      (click)="queues.joinLate(row.row.tournament)"
                    >
                      {{ words.joinTournament }}
                    </button>
                  }
                }
              }
            </td>
          </tr>
        }
      }
    </ng-template>

    <ng-template #tableRow let-row>
      <tr
        class="gd-hall-sections__row"
        data-kind="table"
        [attr.data-key]="'table:' + row.table.id"
        [attr.data-played]="row.table.playing ? '' : null"
        [attr.data-invited]="row.table.invitedYou ? '' : null"
      >
        <td>{{ row.table.format }}</td>
        <td>
          {{ row.table.tournament }}
          @if (row.note; as note) {
            - <i class="gd-hall-sections__note">{{ note }}</i>
          }
        </td>
        <td>
          {{ row.table.statusDescription }}
          @if (tables.ageOf(row.table); as age) {
            <span class="gd-hall-sections__age" [title]="ageTitle(row)">{{ age }}</span>
          }
        </td>
        <td>{{ row.table.players.join(', ') }}</td>
        <td>
          @switch (row.action) {
            @case ('leave') {
              <button
                type="button"
                class="gd-hall-sections__button"
                data-action="leave"
                (click)="tables.leave(row.table.id)"
              >
                Leave Table
              </button>
            }
            @case ('join') {
              <button
                type="button"
                class="gd-hall-sections__button"
                data-action="join"
                (click)="join.openTable(row.table.id)"
              >
                Join Table
              </button>
            }
            @case ('play') {
              <a class="gd-hall-sections__button" data-action="play" [href]="row.gameUrl" target="_blank">Play Match</a>
            }
            @case ('watch') {
              <a class="gd-hall-sections__button" data-action="watch" [href]="row.gameUrl" target="_blank">Spectate</a>
            }
            @default {
              @if (row.table.status === 'FINISHED' && row.table.winner !== null) {
                {{ row.table.winner }}
              }
            }
          }
        </td>
      </tr>
    </ng-template>
  `,
  imports: [NgTemplateOutlet],
})
export class GameHallSectionsComponent {
  protected readonly board = inject(HallBoard);
  protected readonly settings = inject(HallSectionSettings);
  protected readonly tables = inject(HallTablesService);
  protected readonly queues = inject(HallQueuesService);
  protected readonly join = inject(JoinDialog);
  private readonly navigation = inject(PageNavigation);

  protected readonly titles = HALL_SECTION_TITLES;
  protected readonly columns = HALL_SECTION_COLUMNS;
  protected readonly draftTitle = DRAFT_FORMAT_TITLE;
  protected readonly words = {
    joinQueue: JOIN_QUEUE,
    leaveQueue: LEAVE_QUEUE,
    startNow: START_NOW,
    showPrizes: SHOW_PRIZES,
    goToDraft: GO_TO_DRAFT,
    registerDeck: REGISTER_DECK,
    abandon: ABANDON_TOURNAMENT,
    joinTournament: JOIN_TOURNAMENT,
  };

  protected eventRows(section: HallSectionId): readonly HallQueueSectionRow[] {
    const sections = this.board.sections();
    return section === 'wc' ? sections.wc : section === 'scheduled' ? sections.scheduled : sections.recurring;
  }

  protected price(cost: number) {
    return splitPrice(cost);
  }

  protected prizeLabel(prizes: HallPrizes): string {
    return prizes.label === '' ? PRIZE_BREAKDOWN : prizes.label;
  }

  protected ageTitle(row: HallTableRow): string {
    return row.table.createdAt === null ? '' : tableAgeTitle(row.table.createdAt);
  }

  /** A draft format page, in a new tab (GameHall.js .draftFormatInfo). */
  protected open(url: string): void {
    this.navigation.openInNewTab(url);
  }
}
