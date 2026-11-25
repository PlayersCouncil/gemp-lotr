import { Component, computed, input, output } from '@angular/core';
import { DrawerContent, EventDrawer } from '../../../core/hall/events/event-drawer';
import { SERVER_TIME_TITLE } from '../../../core/hall/events/events-messages';
import { EventsButtonComponent } from './events-button.component';

/**
 * One Events-tab row with its drawer under it (eventDrawer.js EventDrawer.row): the + Details / − Details toggle, or
 * a fixed button in its place (the calendar preview's "Go to League"), the title, extras, and the date on the right
 * ("Server time (UTC)"). The drawer shows its loading line, error or empty line itself; what was fetched (or the
 * fallback) is the projected content, which the owner draws from `drawer.content()`.
 *
 * Inputs: `drawer` (required; null: always open, showing the projected content), `title`, `date`, `kind` (league, tournament, projected), `goLabel` (a fixed button
 * instead of the toggle; it emits `go`). Projected: an element with `gdRowExtras` (after the title), and the drawer's
 * content.
 *
 * DOM contract: `gd-event-row.gd-event-row` (`data-kind`, `data-key`) holding `div.gd-event-row__header`
 * (`data-expanded` while open) with `button.gd-events-button.gd-event-row__toggle` (`aria-expanded`) or
 * `button.gd-events-button.gd-event-row__go`, `span.gd-event-row__title`, `span.gd-event-row__extras`,
 * `span.gd-event-row__date` (`title`); then, once opened, `div.gd-event-row__drawer` (`hidden` while shut, `data-state` =
 * loading, error, empty, data, fallback) holding `div.gd-event-row__loading`, `div.gd-event-row__error`,
 * `i.gd-event-row__empty` or the projected content.
 */
@Component({
  selector: 'gd-event-row',
  styleUrl: './event-row.component.css',
  imports: [EventsButtonComponent],
  host: { class: 'gd-event-row', '[attr.data-kind]': 'kind()', '[attr.data-key]': 'key()' },
  template: `
    <div class="gd-event-row__header" [attr.data-expanded]="open() ? '' : null">
      @if (goLabel(); as label) {
        <button gdEventsButton class="gd-event-row__go" (click)="go.emit()">{{ label }}</button>
      } @else if (drawer(); as drawer) {
        <button
          gdEventsButton
          class="gd-event-row__toggle"
          [attr.aria-expanded]="drawer.isOpen()"
          (click)="drawer.toggle()"
        >
          {{ drawer.toggleLabel() }}
        </button>
      }
      <span class="gd-event-row__title">{{ title() }}</span>
      <span class="gd-event-row__extras"><ng-content select="[gdRowExtras]" /></span>
      <span class="gd-event-row__date" [title]="dateTitle">{{ date() }}</span>
    </div>
    @if (content(); as content) {
      <div class="gd-event-row__drawer" [hidden]="!open()" [attr.data-state]="content.kind">
        @switch (content.kind) {
          @case ('loading') {
            <div class="gd-event-row__loading">{{ content.text }}</div>
          }
          @case ('error') {
            <div class="gd-event-row__error">{{ content.text }}</div>
          }
          @case ('empty') {
            <i class="gd-event-row__empty">{{ content.text }}</i>
          }
        }
        <ng-content />
      </div>
    }
  `,
})
export class EventRowComponent {
  /** null: no fetch; the drawer is open with the projected content (the calendar's facts of a projected league). */
  readonly drawer = input.required<EventDrawer<unknown> | null>();
  readonly title = input('');
  readonly date = input('');
  readonly kind = input<string | null>(null);
  readonly key = input<string | null>(null);
  readonly goLabel = input<string | null>(null);
  readonly go = output();

  protected readonly dateTitle = SERVER_TIME_TITLE;
  protected readonly open = computed(() => this.drawer()?.isOpen() ?? true);
  protected readonly content = computed<DrawerContent<unknown> | null>(() => {
    const drawer = this.drawer();
    return drawer ? drawer.content() : { kind: 'fallback' };
  });
}
