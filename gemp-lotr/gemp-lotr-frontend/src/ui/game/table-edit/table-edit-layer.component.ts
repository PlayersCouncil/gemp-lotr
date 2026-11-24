import { Component, computed, inject } from '@angular/core';
import { placeBound, placeLabel } from '../../../core/game/table-edit/edit-menu';
import { addTo, EditPlace } from '../../../core/game/table-edit/edit-ops';
import { FoundCard } from '../../../core/game/table-edit/card-search';
import { placeAtPoint, Size } from '../../../core/game/table-edit/popup-place';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { PickBoxComponent } from '../authoring/pick-box.component';
import { CardMenuComponent } from './card-menu.component';
import { CardPicker } from './card-picker';
import { ChooserComponent } from './chooser.component';
import { EditPopups } from './edit-popups';
import { PopupPlaceDirective } from './popup-place.directive';
import { PuzzleLayerComponent } from './puzzle-layer.component';
import { SearchWindowComponent } from './search-window.component';
import { TablePick } from './table-pick';

/**
 * What the edit controls draw over the game page while its table is edited by hand: the chooser of an open spot, a
 * card's menu, the search window (adding to the chooser's place, within the place's card types), the box round the
 * card under the pointer while one is picked for Attach or Stack, and a refusal said where it was asked for (the edit
 * bar says the others). The open spots themselves are their hosts' (gd-game-cards).
 *
 * A puzzle's (level `puzzle`) adds `gd-puzzle-layer`: its slots' chooser, search window and strip.
 *
 * DOM contract: `gd-table-edit` holding `gd-edit-chooser`, `gd-edit-menu`, `gd-edit-search`,
 * `gd-game-author-pickbox` (while picking) and, while a refusal of a control is up, `.gd-edit-problem` (`role=alert`,
 * placed by gdPopupPlace where it was asked for) with the server's words (TableEdit takes it away after a while, or at
 * the next change).
 */
@Component({
  selector: 'gd-table-edit',
  styleUrl: './table-edit-layer.component.css',
  imports: [
    ChooserComponent,
    CardMenuComponent,
    SearchWindowComponent,
    PickBoxComponent,
    PopupPlaceDirective,
    PuzzleLayerComponent,
  ],
  providers: [{ provide: TablePick, useExisting: CardPicker }],
  template: `
    <gd-edit-chooser />
    <gd-edit-menu />
    <gd-edit-search
      [open]="popups.search() !== null"
      [bound]="bound()"
      [heading]="heading()"
      [said]="searchProblem()"
      (chosen)="chosen($event)"
      (closed)="popups.closeSearch()"
    />
    <gd-game-author-pickbox />
    @if (problem(); as problem) {
      <p class="gd-edit-problem" role="alert" [gdPopupPlace]="problem.place">
        {{ problem.text }}
      </p>
    }
    @if (edit.level() === 'puzzle') {
      <!-- A puzzle's slots: their chooser, their search window, the slots strip -->
      <gd-puzzle-layer />
    }
  `,
})
export class TableEditLayerComponent {
  protected readonly popups = inject(EditPopups);
  protected readonly edit = inject(TableEdit);
  private readonly recent = inject(RecentCards);

  protected readonly bound = computed(() => {
    const place = this.popups.search();
    return place ? placeBound(place.zone) : null;
  });
  protected readonly heading = computed(() => {
    const place = this.popups.search();
    return place ? `Add to ${placeLabel(place)}` : 'Search';
  });
  /** A refusal while the search window is open: said in it. */
  protected readonly searchProblem = computed(() =>
    this.popups.search() !== null ? (this.edit.problem()?.text ?? null) : null,
  );
  /** A refusal of a control, said where it was asked for (not while the search window says it). */
  protected readonly problem = computed(() => {
    const problem = this.edit.problem();
    if (!problem?.at || this.popups.search() !== null) return null;
    const at = problem.at;
    return { text: problem.text, place: (size: Size, room: Size) => placeAtPoint(at, size, room) };
  });

  /** A card found in the search window: put in its place; the window closes once it is there. */
  protected async chosen(found: FoundCard): Promise<void> {
    const place: EditPlace | null = this.popups.search();
    if (!place) return;
    if (await this.edit.apply([addTo(found.blueprintId, place)], { x: 0, y: 0 })) {
      this.recent.use({ blueprintId: found.blueprintId, name: found.name, type: found.type.toUpperCase() });
      this.popups.closeSearch();
    }
  }
}
