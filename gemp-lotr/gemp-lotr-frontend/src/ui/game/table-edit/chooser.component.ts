import { Component, computed, inject } from '@angular/core';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { cardsInGame, fits, placeLabel } from '../../../core/game/table-edit/edit-menu';
import { addTo } from '../../../core/game/table-edit/edit-ops';
import { placeBeside, Size } from '../../../core/game/table-edit/popup-place';
import { RecentCard, RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CardHoverDirective } from '../authoring/widgets/card-hover.directive';
import { EditPopups } from './edit-popups';
import { MenuKeysDirective } from './menu-keys.directive';
import { PopupPlaceDirective } from './popup-place.directive';

/**
 * The chooser of an open spot: a dropdown by the spot with the cards this person used lately (Recent, most recent
 * first), one of each card already in the game (In this scenario), each only where it fits the place, and Search…
 * (the search window). A card chosen is put in the spot's place (`add`), and goes to the head of Recent. A card named
 * is shown large while the mouse rests on it. Escape, or a press outside it, closes it.
 *
 * DOM contract: `gd-edit-chooser` holding `.gd-edit-backdrop` and `.gd-edit-chooser` (`role=menu`, `data-seat`,
 * `data-zone`; placed by gdPopupPlace) with `.gd-edit-chooser__heading`s and `button[role=menuitem]`:
 * `[data-chooser=recent]` and `[data-chooser=scenario]` (each with `data-blueprint`), then `[data-chooser=search]`;
 * for the author of a puzzle, `[data-chooser=slot]` (Open slot: a slot of the puzzle at this place, its form shown in
 * the Board tab).
 */
@Component({
  selector: 'gd-edit-chooser',
  styleUrl: './chooser.component.css',
  imports: [CardHoverDirective, MenuKeysDirective, PopupPlaceDirective],
  template: `
    @if (popups.chooser(); as open) {
      <div class="gd-edit-backdrop" (pointerdown)="close()" (contextmenu)="$event.preventDefault(); close()"></div>
      <div
        class="gd-edit-chooser"
        role="menu"
        [attr.aria-label]="'Add to ' + label()"
        [attr.data-seat]="open.place.seat"
        [attr.data-zone]="open.place.zone"
        [gdPopupPlace]="placement()"
        gdMenuKeys
        (escape)="close(true)"
      >
        @if (recent().length > 0) {
          <div class="gd-edit-chooser__heading">Recent</div>
          @for (card of recent(); track card.blueprintId) {
            <button
              type="button"
              role="menuitem"
              class="gd-edit-chooser__item"
              data-chooser="recent"
              [attr.data-blueprint]="card.blueprintId"
              [gdCardHover]="card.blueprintId"
              [disabled]="edit.busy()"
              (click)="choose(card)"
            >
              {{ card.name }}
            </button>
          }
        }
        @if (inGame().length > 0) {
          <div class="gd-edit-chooser__heading">In this scenario</div>
          @for (card of inGame(); track card.blueprintId) {
            <button
              type="button"
              role="menuitem"
              class="gd-edit-chooser__item"
              data-chooser="scenario"
              [attr.data-blueprint]="card.blueprintId"
              [gdCardHover]="card.blueprintId"
              [disabled]="edit.busy()"
              (click)="choose(card)"
            >
              {{ card.name }}
            </button>
          }
        }
        <button
          type="button"
          role="menuitem"
          class="gd-edit-chooser__item gd-edit-chooser__search"
          data-chooser="search"
          title="Find any card, with the deck builder's filters"
          (click)="search()"
        >
          Search…
        </button>
        @if (author?.slotted()) {
          <button
            type="button"
            role="menuitem"
            class="gd-edit-chooser__item gd-edit-chooser__search"
            data-chooser="slot"
            title="A slot here: the solver chooses its card (its label and bound in the Board tab)"
            [disabled]="edit.busy()"
            (click)="openSlot()"
          >
            Open slot
          </button>
        }
      </div>
    }
  `,
})
export class ChooserComponent {
  protected readonly popups = inject(EditPopups);
  protected readonly edit = inject(TableEdit);
  private readonly recents = inject(RecentCards);

  private readonly zone = computed(() => this.popups.chooser()?.place.zone ?? 'hand');
  protected readonly label = computed(() => {
    const open = this.popups.chooser();
    return open ? placeLabel(open.place) : '';
  });
  protected readonly recent = computed(() => this.recents.list().filter((card) => fits(card.type, this.zone())));
  protected readonly inGame = computed(() =>
    cardsInGame(this.edit.board()).filter((card) => fits(card.type, this.zone())),
  );
  protected readonly placement = computed(() => {
    const anchor = this.popups.chooser()?.anchor;
    return (size: Size, room: Size) => (anchor ? placeBeside(anchor, size, room) : { left: 0, top: 0 });
  });

  protected async choose(card: RecentCard): Promise<void> {
    const open = this.popups.chooser();
    if (!open) return;
    const at = { x: open.anchor.left, y: open.anchor.bottom };
    if (await this.edit.apply([addTo(card.blueprintId, open.place)], at)) {
      this.recents.use(card);
      this.popups.closeChooser();
    }
  }

  protected search(): void {
    const open = this.popups.chooser();
    if (open) this.popups.openSearch(open.place);
  }

  protected close(focusBack = false): void {
    this.popups.closeChooser(focusBack);
  }

  /** The author's: a puzzle's slot opened at this place (a fresh game; the Board tab shows its form). */
  protected readonly author = inject(AuthorBackend, { optional: true });

  protected async openSlot(): Promise<void> {
    const open = this.popups.chooser();
    if (!open || !this.author) return;
    const said = await this.author.openSlot(open.place);
    if (said === null) this.popups.closeChooser();
    else this.edit.problem.set({ text: said, at: { x: open.anchor.left, y: open.anchor.bottom } });
  }
}
