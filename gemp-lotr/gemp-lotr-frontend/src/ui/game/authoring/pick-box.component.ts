import { DOCUMENT } from '@angular/common';
import { Component, DestroyRef, effect, inject, ViewEncapsulation } from '@angular/core';
import { TablePicker } from './table-picker.service';

/** The attribute the document element has while the author picks something on the table (the crosshair). */
export const PICKING_ATTRIBUTE = 'data-author-picking';

const px = (value: number) => `${Math.round(value)}px`;

/**
 * What picking on the table draws (TablePicker): the pointer a crosshair everywhere, and round the thing under it a
 * box, with its anchor in a label above. Neither takes the pointer. Rendered by the authoring panel.
 *
 * Its stylesheet is not scoped (the crosshair is the whole page's); every other rule starts at
 * `gd-game-author-pickbox`.
 *
 * DOM contract: `gd-game-author-pickbox` holding, while the pointer is on something, `.gd-author-pickbox`
 * (`--gd-left`, `--gd-top`, `--gd-width`, `--gd-height`: the box, px) and `.gd-author-picklabel` (`--gd-left`,
 * `--gd-top`) with the anchor's text; the document element has `data-author-picking` while a pick is under way.
 */
@Component({
  selector: 'gd-game-author-pickbox',
  styleUrl: './pick-box.component.css',
  encapsulation: ViewEncapsulation.None,
  template: `
    @if (picker.hover(); as hover) {
      <div
        class="gd-author-pickbox"
        aria-hidden="true"
        [style.--gd-left]="px(hover.box.left)"
        [style.--gd-top]="px(hover.box.top)"
        [style.--gd-width]="px(hover.box.width)"
        [style.--gd-height]="px(hover.box.height)"
      ></div>
      <div class="gd-author-picklabel" [style.--gd-left]="px(hover.label.left)" [style.--gd-top]="px(hover.label.top)">
        {{ hover.anchor }}
      </div>
    }
  `,
})
export class PickBoxComponent {
  protected readonly picker = inject(TablePicker);
  protected readonly px = px;

  constructor() {
    const root = inject(DOCUMENT).documentElement;
    effect(() => root.toggleAttribute(PICKING_ATTRIBUTE, this.picker.picking()));
    inject(DestroyRef).onDestroy(() => root.removeAttribute(PICKING_ATTRIBUTE));
  }
}
