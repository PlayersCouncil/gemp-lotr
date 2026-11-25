import { Component, input, model } from '@angular/core';

/** The (i) toggles' tooltip (TableFlow.bindInfoToggles). */
export const INFO_TOGGLE_TITLE = 'What is this?';

/**
 * An (i) that shows or hides a help text beside it (the legacy `.info-toggle`, TableFlow.bindInfoToggles and
 * toggleInfo): a click, Enter or Space toggles `open`; the text is the page's, `hidden` while closed.
 *
 * DOM contract: `gd-errata-info-toggle` holding `button.gd-errata-info-toggle` ("i", `title` "What is this?",
 * `aria-expanded`, `aria-controls` = the text's id, `data-info-toggle`).
 */
@Component({
  selector: 'gd-errata-info-toggle',
  styleUrl: './errata-info-toggle.component.css',
  host: { style: 'display: contents' },
  template: `<button
    type="button"
    class="gd-errata-info-toggle"
    data-info-toggle
    [title]="title"
    [attr.aria-expanded]="open()"
    [attr.aria-controls]="controls()"
    (click)="open.set(!open())"
  >
    i
  </button>`,
})
export class ErrataInfoToggleComponent {
  readonly open = model(false);
  /** The id of the text it shows. */
  readonly controls = input.required<string>();
  protected readonly title = INFO_TOGGLE_TITLE;
}
