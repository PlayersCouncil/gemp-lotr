import { Component } from '@angular/core';

/**
 * A button of the Events tab, drawn as the hall's jQuery UI 1.13 buttons (.ui-button: the widget's font size, padding
 * .4em 1em, the dark-hive default / hover / active looks): the month arrows, Today, + Details, Go to League, Join
 * league, Retry. `data-variant="details"` is the details toggle's size (.event-details-button).
 *
 * DOM contract: a native `button.gd-events-button` (`type=button`), its text projected; disabled while `disabled`.
 */
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector -- a real <button>
  selector: 'button[gdEventsButton]',
  styleUrl: './events-button.component.css',
  host: { class: 'gd-events-button', type: 'button' },
  template: '<ng-content />',
})
export class EventsButtonComponent {}
