import { Component, input } from '@angular/core';

/**
 * A labelled row of a panel form: the label on the left (its explanation as its tooltip), the control (projected)
 * beside it; clicking the label is clicking the control.
 *
 * DOM contract: `label.gd-author-field` with `span.gd-author-field__label` (`title`) and `span.gd-author-field__control`
 * holding the control.
 */
@Component({
  selector: 'gd-author-field',
  host: { style: 'display: contents' },
  template: `
    <!-- eslint-disable-next-line @angular-eslint/template/label-has-associated-control -- it wraps the projected control -->
    <label class="gd-author-field">
      <span class="gd-author-field__label" [title]="hint()">{{ label() }}</span>
      <span class="gd-author-field__control"><ng-content /></span>
    </label>
  `,
})
export class AuthorFieldComponent {
  readonly label = input.required<string>();
  readonly hint = input('');
}
