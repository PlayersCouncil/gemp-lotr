import { Component, model } from '@angular/core';

/**
 * The "i" button that opens and closes the explanation of the tiers (UndoHelpComponent), round and in italics.
 *
 * DOM contract: `gd-undo-info` holding `button[data-undo=info]` (`aria-expanded`).
 */
@Component({
  selector: 'gd-undo-info',
  styleUrl: './undo-info.component.css',
  host: { class: 'gd-undo-info' },
  template: `
    <button
      type="button"
      class="gd-undo-info__button"
      data-undo="info"
      title="What the colours mean"
      [attr.aria-expanded]="open()"
      (click)="open.set(!open())"
    >
      i
    </button>
  `,
})
export class UndoInfoComponent {
  /** Whether the explanation is open. */
  readonly open = model(false);
}
