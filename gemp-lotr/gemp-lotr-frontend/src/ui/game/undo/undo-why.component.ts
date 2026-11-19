import { Component, input } from '@angular/core';

/**
 * What makes a place yellow or red: a line each, in bold.
 *
 * DOM contract: `gd-undo-why[data-undo=why]` with a `div` > `b` per reason.
 */
@Component({
  selector: 'gd-undo-why',
  styleUrl: './undo-why.component.css',
  host: { class: 'gd-undo-why', 'data-undo': 'why' },
  template: `
    @for (reason of reasons(); track $index) {
      <div>
        <b>{{ reason }}</b>
      </div>
    }
  `,
})
export class UndoWhyComponent {
  readonly reasons = input.required<readonly string[]>();
}
