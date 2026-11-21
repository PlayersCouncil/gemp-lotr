import { Component, computed, inject, signal } from '@angular/core';
import { BoardSite, DraftSeat, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { BOARD_SEATS } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { CardHoverDirective } from '../widgets/card-hover.directive';
import { CardSearchComponent } from '../widgets/card-search.component';
import { BoardLaidComponent } from './board-laid.component';
import { BoardRenameComponent } from './board-rename.component';

/** A site of a path as the tab shows it, with the names it has in the scenario (the opponent's too, while the same). */
interface PathRow {
  site: BoardSite;
  at: number;
  names: readonly string[];
  last: boolean;
}

/**
 * The Board tab's adventure path, a site to a row: "Site N", its card (shown large under the mouse; a click opens its
 * information), its id and its names in the scenario, each with ✎ to rename it; Change finds another site by name
 * (this number's first), × takes the last site off (while there are others), + Site N+1 adds one. Every change is
 * made at once. Both seats usually have the same path, changed together while "the same sites for both seats" is
 * ticked; unticked, each seat's is shown and changed on its own. Above them, the path on the table with the markers
 * and who controls each site (gd-author-board-laid). A path whose sites have no printed numbers (the Shadows block
 * on) is a list: its sites are numbered as listed (#N), the first listed is laid first (an up arrow puts a site
 * before the one above, keeping its name), sites without a number are found first, and + Site adds one at the end.
 *
 * DOM contract: `gd-author-board-path` holding `.gd-author-board__heading`, `gd-author-check[data-option=same-sites]`,
 * then per seat shown (a `.gd-author-board-path__seat` heading, `data-seat`, when apart) a `.gd-author-board-path__site`
 * per site (`data-seat`, `data-site` its number) with `.gd-author-board-path__name` (`.gd-author-board-path__number`,
 * `span.cardHint[value]`, `.gd-author-board-path__id`, `.gd-author-board-path__first` on a list's first, a `.gd-author-board-path__alias` per name with
 * `button[data-action=rename]`, or `gd-author-board-rename`) and `button[data-action]` (change, raise, drop-site) or
 * `gd-author-card-search`; then `button[data-action=add-site]` (or its search). A seat's rows have
 * `data-unnumbered` on such a path.
 */
@Component({
  selector: 'gd-author-board-path',
  styleUrl: './board-path.component.css',
  imports: [
    AuthorButtonDirective,
    AuthorCheckComponent,
    BoardLaidComponent,
    BoardRenameComponent,
    CardHoverDirective,
    CardSearchComponent,
  ],
  template: `
    <div class="gd-author-board__heading">The adventure path</div>
    <gd-author-board-laid />
    <gd-author-check
      data-option="same-sites"
      label="the same sites for both seats"
      hint="Unticked, each seat's path is changed on its own"
      [checked]="!changes.sitesApart()"
      (changed)="changes.samePath($event)"
    />
    @for (seat of seats(); track seat) {
      @if (changes.sitesApart()) {
        <div class="gd-author-board-path__seat" [attr.data-seat]="seat">
          {{ seat === 'freeps' ? 'Free Peoples' : 'Shadow' }}
        </div>
      }
      @for (row of rows(seat); track row.at) {
        <div
          class="gd-author-board-path__site"
          [attr.data-seat]="seat"
          [attr.data-site]="row.site.number"
          [attr.data-unnumbered]="listed(seat) ? '' : null"
        >
          <div class="gd-author-board-path__name">
            @if (listed(seat)) {
              <span
                class="gd-author-board-path__number"
                title="Listed {{
                  row.site.number
                }}: these sites have no printed numbers. The first listed is laid first; each later one is chosen in play."
                >#{{ row.site.number }}</span
              >
            } @else {
              <span class="gd-author-board-path__number">Site {{ row.site.number }}</span>
            }
            <span class="cardHint" [attr.value]="row.site.id" [gdCardHover]="row.site.id">{{
              row.site.name ?? '(no such card)'
            }}</span>
            <span class="gd-author-board-path__id">{{ row.site.id }}</span>
            @if (listed(seat) && row.at === 0) {
              <span class="gd-author-board-path__first" title="Laid as site 1 (unless setup.path says another)"
                >laid first</span
              >
            }
            @for (name of row.names; track name) {
              @if (renaming() === name) {
                <gd-author-board-rename [alias]="name" (closed)="renaming.set(null)" />
              } @else {
                <span
                  class="gd-author-board-path__alias"
                  [title]="'Its name in the scenario: choose(' + name + '), card:' + name"
                  >{{ name }}</span
                >
                <button
                  gdAuthorButton="inline"
                  data-action="rename"
                  title="Give this site another name in the scenario. Every part of the scenario that says the old name is changed to say the new one."
                  (click)="renaming.set(name)"
                >
                  ✎
                </button>
              }
            }
          </div>
          @if (finding() === seat + row.at) {
            <gd-author-card-search
              [sites]="true"
              [site]="listed(seat) ? 0 : row.site.number"
              placeholder="name of a site"
              (picked)="put(seat, row.at, $event)"
              (cancelled)="finding.set(null)"
            />
          } @else {
            <div class="gd-author-board-path__controls">
              <button
                gdAuthorButton
                data-action="change"
                title="Find a site by its name"
                (click)="finding.set(seat + row.at)"
              >
                Change
              </button>
              @if (listed(seat) && row.at > 0) {
                <button
                  gdAuthorButton
                  data-action="raise"
                  title="Put before the one above (the first listed is laid first); it keeps its name"
                  (click)="changes.raiseSite(seat, row.at)"
                >
                  ↑
                </button>
              }
              @if (row.last && row.at > 0) {
                <button
                  gdAuthorButton="danger"
                  data-action="drop-site"
                  title="Take the last site off the path"
                  (click)="changes.changeSite(seat, row.at, null)"
                >
                  ×
                </button>
              }
            </div>
          }
        </div>
      }
      @let next = changes.path(seat).length;
      @if (finding() === seat + next) {
        <gd-author-card-search
          [sites]="true"
          [site]="listed(seat) ? 0 : next + 1"
          placeholder="name of a site"
          (picked)="put(seat, next, $event)"
          (cancelled)="finding.set(null)"
        />
      } @else {
        <button
          gdAuthorButton
          data-action="add-site"
          title="Find a site by its name"
          (click)="finding.set(seat + next)"
        >
          {{ listed(seat) ? '+ Site' : '+ Site ' + (next + 1) }}
        </button>
      }
    }
  `,
})
export class BoardPathComponent {
  protected readonly changes = inject(BoardChanges);
  /** The seat and place on the path a site is being found for ("freeps2"). */
  protected readonly finding = signal<string | null>(null);
  /** The site name being changed. */
  protected readonly renaming = signal<string | null>(null);
  protected readonly seats = computed<readonly DraftSeat[]>(() =>
    this.changes.sitesApart() ? BOARD_SEATS : ['freeps'],
  );

  protected rows(seat: DraftSeat): PathRow[] {
    const path = this.changes.path(seat);
    const whose = this.changes.sitesApart() ? [seat] : BOARD_SEATS;
    return path.map((site, at) => ({
      site,
      at,
      last: at === path.length - 1,
      names: whose.map((one) => this.changes.path(one)[at]?.alias).filter((name): name is string => !!name),
    }));
  }

  /** The seat's path is a list of sites without numbers. */
  protected listed(seat: DraftSeat): boolean {
    return this.changes.unnumbered(seat);
  }

  protected put(seat: DraftSeat, at: number, found: FoundCard): void {
    this.finding.set(null);
    void this.changes.changeSite(seat, at, found.id);
  }
}
