import { Directive, input } from '@angular/core';

/** How a panel button stands out: `primary` (the thing to do), `danger` (it takes something away), `inline` (in a line of text). */
export type AuthorTone = '' | 'primary' | 'danger' | 'inline';

/**
 * A button of the authoring panel: `<button gdAuthorButton>` (or `gdAuthorButton="primary"`). It never submits, and
 * the panel's stylesheet draws it.
 *
 * DOM contract: `button.gd-author-button`, `data-tone` (primary, danger, inline) when it has one.
 */
@Directive({
  selector: 'button[gdAuthorButton]',
  host: {
    class: 'gd-author-button',
    type: 'button',
    '[attr.data-tone]': 'tone() || null',
  },
})
export class AuthorButtonDirective {
  readonly tone = input<AuthorTone>('', { alias: 'gdAuthorButton' });
}
