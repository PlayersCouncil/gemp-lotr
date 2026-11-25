import { Component, computed, ElementRef, inject, input } from '@angular/core';
import { EditPlace } from '../../../core/game/table-edit/edit-ops';
import { placeLabel } from '../../../core/game/table-edit/edit-menu';
import { EditPopups } from './edit-popups';

/**
 * An open spot: a card-shaped + glowing blue at the end of a place that can take a card while the table is edited by
 * hand. Pressing it opens the chooser (Recent, In this scenario, Search…) by it; what is chosen is put in `where`. It
 * knows nothing of the table: a host (a row of the table, a hand, later a pile window) puts one where it wants it, sized
 * as its cards (`--gd-card-height`), and shows it only while TableEdit allows adding.
 *
 * DOM contract: `gd-open-spot` holding `button.gd-open-spot` (`data-seat`, `data-zone`, `aria-label` "Add to …",
 * `aria-expanded` while its chooser is open) with `.gd-open-spot__plus`.
 */
@Component({
  selector: 'gd-open-spot',
  styleUrl: './open-spot.component.css',
  template: `
    <button
      type="button"
      class="gd-open-spot"
      [attr.data-seat]="where().seat"
      [attr.data-zone]="where().zone"
      [attr.aria-label]="'Add to ' + label()"
      [attr.aria-expanded]="open()"
      [title]="'Add a card: ' + label()"
      (click)="press()"
    >
      <span class="gd-open-spot__plus" aria-hidden="true">+</span>
    </button>
  `,
})
export class OpenSpotComponent {
  /** The place a card chosen here goes to. */
  readonly where = input.required<EditPlace>();

  private readonly popups = inject(EditPopups);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly label = computed(() => placeLabel(this.where()));
  protected readonly open = computed(() => {
    const chooser = this.popups.chooser();
    return chooser !== null && chooser.place.seat === this.where().seat && chooser.place.zone === this.where().zone;
  });

  protected press(): void {
    if (this.open()) {
      this.popups.closeChooser();
    } else {
      this.popups.openChooser(this.where(), this.host.querySelector('button') ?? this.host);
    }
  }
}
