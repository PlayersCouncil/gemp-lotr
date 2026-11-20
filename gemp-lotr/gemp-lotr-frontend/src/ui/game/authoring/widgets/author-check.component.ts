import { Component, input, output } from '@angular/core';

/**
 * A tick with its words after it, as the panel's options have them; its explanation is its tooltip. Says each change.
 *
 * DOM contract: `label.gd-author-check` (`title`) with `input[type=checkbox]` and the words.
 */
@Component({
  selector: 'gd-author-check',
  host: { style: 'display: contents' },
  template: `
    <label class="gd-author-check" [title]="hint()">
      <input
        type="checkbox"
        [checked]="checked()"
        [disabled]="disabled()"
        (change)="changed.emit($any($event.target).checked)"
      />
      {{ label() }}
    </label>
  `,
})
export class AuthorCheckComponent {
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly checked = input(false);
  readonly disabled = input(false);
  readonly changed = output<boolean>();
}
