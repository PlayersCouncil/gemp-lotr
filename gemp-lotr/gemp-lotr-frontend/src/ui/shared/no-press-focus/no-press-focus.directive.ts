import { Directive } from '@angular/core';

/**
 * A press with the mouse behaves as on a plain area of the page: nothing has the keyboard focus afterwards, not even this
 * element, as on the legacy page, whose equivalents were plain elements. The keyboard still reaches it with Tab.
 */
@Directive({
  selector: '[gdNoPressFocus]',
  host: { '(mousedown)': 'press($event)' },
})
export class NoPressFocusDirective {
  protected press(event: MouseEvent): void {
    event.preventDefault();
    const active = (event.currentTarget as Element).ownerDocument.activeElement;
    if (active instanceof HTMLElement) {
      active.blur();
    }
  }
}
