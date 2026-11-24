import { Component, computed, inject } from '@angular/core';
import { FoundCard } from '../../../core/game/table-edit/card-search';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { slotLabel, slotRow, slotSearch } from '../../../core/game/table-edit/puzzle-slots';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { PuzzleChooserComponent } from './puzzle-chooser.component';
import { PuzzlePopups } from './puzzle-popups';
import { PuzzleSlotComponent } from './puzzle-slot.component';
import { SearchWindowComponent } from './search-window.component';

/**
 * What the puzzle page draws over the table while its slots are being filled: the chooser of a slot, the search
 * window held to a slot's bound (its label shown, its pool and filter in the label's title; the server searches
 * inside them, so the person's filters can only narrow it: CardSearch, slotSearch), and the slots strip: the slots whose place the table shows no row for (a place
 * in a draw deck, a pile, on a card, a site, the opponent's hand), each labelled with where its card goes, at the top
 * right under the scenario's menu, so that no slot is out of reach. The slots in the table's rows are those rows'
 * (gd-game-cards). Drawn by gd-table-edit for a puzzle.
 *
 * DOM contract: `gd-puzzle-layer` holding `gd-puzzle-chooser`, `gd-edit-search` (the slot's, `data-for=slot`) and,
 * while held with such slots, `section.gd-puzzle-strip` (`aria-label` "Slots") with a `gd-puzzle-slot` per slot.
 */
@Component({
  selector: 'gd-puzzle-layer',
  styleUrl: './puzzle-layer.component.css',
  imports: [PuzzleChooserComponent, PuzzleSlotComponent, SearchWindowComponent],
  template: `
    <gd-puzzle-chooser />
    <gd-edit-search
      data-for="slot"
      [open]="searched() !== null"
      [bound]="bound()"
      [heading]="heading()"
      [said]="said()"
      (chosen)="chosen($event)"
      (closed)="popups.closeSearch()"
    />
    @if (edit.editing() && strip().length > 0) {
      <section class="gd-puzzle-strip" aria-label="Slots">
        @for (filled of strip(); track filled.slot.id) {
          <gd-puzzle-slot [slot]="filled" [place]="true" />
        }
      </section>
    }
  `,
})
export class PuzzleLayerComponent {
  protected readonly popups = inject(PuzzlePopups);
  protected readonly edit = inject(TableEdit);
  private readonly puzzle = inject(PuzzleBackend);
  private readonly recent = inject(RecentCards);

  /** The slots shown away from their place. */
  protected readonly strip = computed(() => this.puzzle.filled().filter((filled) => slotRow(filled.slot) === null));
  /** The slot the search window fills, while it is open. */
  protected readonly searched = computed(() => {
    const id = this.popups.search();
    return this.puzzle.slots().find((slot) => slot.id === id) ?? null;
  });
  protected readonly bound = computed(() => {
    const slot = this.searched();
    return slot ? slotSearch(slot) : null;
  });
  protected readonly heading = computed(() => {
    const slot = this.searched();
    return slot ? `Fill: ${slotLabel(slot)}` : 'Search';
  });
  /** A refusal of a card chosen in the window: said in it. */
  protected readonly said = computed(() => (this.searched() ? (this.edit.problem()?.text ?? null) : null));

  /** A card found: put in the slot; the window closes once it is in. */
  protected async chosen(found: FoundCard): Promise<void> {
    const slot = this.searched();
    if (!slot) return;
    if (await this.edit.apply([{ op: 'pick', slot: slot.id, blueprint: found.blueprintId }], null)) {
      this.recent.use({ blueprintId: found.blueprintId, name: found.name, type: found.type });
      this.popups.closeSearch();
    }
  }
}
