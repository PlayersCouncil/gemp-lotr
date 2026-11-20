import { Component, computed, inject } from '@angular/core';
import { DraftSeat } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { BOARD_SEATS, ViewSite } from '../../../../core/game/authoring/board-queue';
import { controlledNumbers } from '../../../../core/game/authoring/board-start';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { CardHoverDirective } from '../widgets/card-hover.directive';

const MARKERS: Readonly<Record<DraftSeat, { short: string; name: string }>> = {
  freeps: { short: 'FP', name: 'Free Peoples' },
  shadow: { short: 'S', name: 'Shadow' },
};

/** Who can control a site, as its select offers it. */
const CONTROL: readonly AuthorOption[] = [
  ['', 'nobody'],
  ['shadow', 'Shadow'],
  ['freeps', 'Free Peoples'],
];

/** A row: a site of the path as it will be, or the next one, which a marker put there lays. */
interface LaidRow {
  site: ViewSite;
  /** Every marker is past it: it can be controlled. */
  controllable: boolean;
  /** Who controls it (as the board will be, or as the scenario starts). */
  controlledBy: DraftSeat | null;
  /** Something gathered changes it. */
  changed: boolean;
}

/**
 * The adventure path on the table, as it will be once the changes gathered are made: a site to a row, with its
 * number, its card (shown large under the mouse), a button per seat's marker (pressed where the marker is; a click
 * puts the marker there, laying the sites up to it) and who controls it (a select once every marker is past it,
 * which gathers control or liberate), and the next site, not laid yet. Its changes are gathered with the tab's
 * others, for the board as it stands or the one the scenario starts with; for the latter, the markers and who
 * controls a site are shown as the scenario starts (the sites as the board in hand has them).
 *
 * DOM contract: `gd-author-board-laid` holding `.gd-author-board-laid__heading` (its heading) and a
 * `.gd-author-board-laid__site` per site (`data-site` its number, `data-laid` when on the path, `data-changed`) with
 * `.gd-author-board-path__number`, `span.cardHint[value]`, a `button[data-action=marker][data-seat]` per seat
 * (`data-here`, `aria-pressed`), and `.gd-author-board-laid__pick` (`gd-author-select[data-control=control]`) or `.gd-author-board-laid__held`.
 */
@Component({
  selector: 'gd-author-board-laid',
  styleUrl: './board-laid.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent, CardHoverDirective],
  template: `
    @if (markers(); as at) {
      <div class="gd-author-board-laid__heading" title="The sites laid, the markers and who controls each site">
        On the table
      </div>
      @for (row of rows(); track row.site.number) {
        <div
          class="gd-author-board-laid__site"
          [attr.data-site]="row.site.number"
          [attr.data-laid]="row.site.toBeLaid ? null : ''"
          [attr.data-changed]="row.changed ? '' : null"
        >
          <span class="gd-author-board-path__number">{{ row.site.number }}</span>
          <span
            class="cardHint gd-author-board-laid__name"
            [attr.value]="row.site.blueprintId"
            [gdCardHover]="row.site.blueprintId"
            [title]="row.site.toBeLaid ? 'Not laid yet: a marker put here lays it' : (row.site.alias ?? '')"
            >{{ row.site.name ?? row.site.blueprintId }}</span
          >
          @for (seat of seats; track seat) {
            <button
              gdAuthorButton="inline"
              data-action="marker"
              [attr.data-seat]="seat"
              [attr.data-here]="at[seat] === row.site.number ? '' : null"
              [attr.aria-pressed]="at[seat] === row.site.number"
              [title]="
                at[seat] === row.site.number
                  ? 'The ' + names[seat].name + ' marker is here'
                  : 'Put the ' + names[seat].name + ' marker here: marker(' + seat + ', ' + row.site.number + ')'
              "
              (click)="mark(seat, row.site.number)"
            >
              {{ names[seat].short }}
            </button>
          }
          @if (row.controllable) {
            <span class="gd-author-board-laid__pick">
              <gd-author-select
                data-control="control"
                label="Controlled by"
                hint="Who controls this site: control(site, seat), or liberate(site) for nobody"
                [options]="control"
                [value]="row.controlledBy ?? ''"
                (changed)="controlBy(row.site.number, $event)"
              />
            </span>
          } @else if (row.controlledBy) {
            <span class="gd-author-board-laid__held">{{ names[row.controlledBy].name }}</span>
          }
        </div>
      }
    }
  `,
})
export class BoardLaidComponent {
  protected readonly changes = inject(BoardChanges);
  protected readonly seats = BOARD_SEATS;
  protected readonly names = MARKERS;

  /** Where the markers are to be: on the board, or (for the setup) as the scenario starts. */
  protected readonly markers = computed<Readonly<Record<DraftSeat, number>> | null>(() => {
    const view = this.changes.view();
    const start = this.changes.startView();
    if (!view) return null;
    return this.changes.inSetup() && start ? { freeps: start.site, shadow: start.opponentSite } : view.markers;
  });
  protected readonly control = CONTROL;

  /**
   * The path as it will be (laid on to the furthest marker with the Free Peoples seat's sites), and the next site of
   * theirs (up to the last site).
   */
  protected readonly rows = computed<LaidRow[]>(() => {
    const view = this.changes.view();
    const board = this.changes.board();
    const at = this.markers();
    const start = this.changes.startView();
    if (!view || !board || !at || !start) return [];
    const setup = this.changes.inSetup();
    const held = BOARD_SEATS.map((seat) => [seat, controlledNumbers(start, seat, board)] as const);
    const sites = [...view.path];
    for (let number = sites.length + 1; number <= Math.min(9, Math.max(at.freeps, at.shadow) + 1); number++) {
      const own = board.sites.freeps[number - 1];
      if (own === undefined) break;
      sites.push({
        number,
        alias: own.alias,
        blueprintId: own.id,
        name: own.name,
        owner: 'freeps',
        controlledBy: null,
        toBeLaid: true,
        touched: false,
      });
    }
    const draftStart = this.changes.start();
    const before = setup
      ? { freeps: draftStart?.site ?? 1, shadow: draftStart?.opponentSite ?? 1 }
      : (board.markers ?? { freeps: 1, shadow: 1 });
    const past = Math.min(at.freeps, at.shadow);
    return sites.map((site) => ({
      site,
      controllable: site.number < past,
      controlledBy: setup ? (held.find(([, numbers]) => numbers.has(site.number))?.[0] ?? null) : site.controlledBy,
      changed:
        BOARD_SEATS.some((seat) => at[seat] === site.number && before[seat] !== site.number) ||
        (!setup && site.touched),
    }));
  });

  protected mark(seat: DraftSeat, number: number): void {
    if (this.markers()?.[seat] === number) return;
    this.changes.queue('marker', [seat, number]);
  }

  protected controlBy(number: number, seat: string): void {
    if (seat === '') this.changes.queue('liberate', [number]);
    else this.changes.queue('control', [number, seat]);
  }
}
