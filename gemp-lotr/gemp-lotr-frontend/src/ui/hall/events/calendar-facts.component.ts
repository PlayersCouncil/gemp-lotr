import { Component, input } from '@angular/core';
import { CalendarFacts } from '../../../core/hall/events/calendar-details';
import { SERIES_HEADERS } from '../../../core/hall/events/events-messages';
import { MarkupComponent } from '../../shared/chat/markup.component';
import { PriceComponent } from './price.component';

/**
 * A calendar event's facts (calendarUi.js renderFacts), in the preview's drawer where the league's or tournament's
 * own detail cannot be had: the facts in a row ("Runs: …", "Cost: …"), the description, and the serie schedule.
 *
 * Inputs: `facts` (required).
 *
 * DOM contract: `gd-calendar-facts.gd-calendar-facts` holding `div.gd-calendar-facts__facts` of
 * `div.gd-calendar-facts__fact` (`span.gd-calendar-facts__label`, then the value or `gd-price`),
 * `gd-markup.gd-calendar-facts__description`, `table.gd-calendar-facts__series`.
 */
@Component({
  selector: 'gd-calendar-facts',
  styleUrl: './calendar-facts.component.css',
  imports: [MarkupComponent, PriceComponent],
  host: { class: 'gd-calendar-facts' },
  template: `
    <div class="gd-calendar-facts__facts">
      @for (fact of facts().facts; track fact.label) {
        <div class="gd-calendar-facts__fact">
          <span class="gd-calendar-facts__label">{{ fact.label }}</span>
          @if (fact.kind === 'price') {
            <gd-price [price]="fact.value" />
          } @else {
            <span>{{ fact.value }}</span>
          }
        </div>
      }
    </div>
    @if (facts().description.length > 0) {
      <gd-markup class="gd-calendar-facts__description" [nodes]="facts().description" />
    }
    @if (facts().series.length > 0) {
      <table class="gd-calendar-facts__series">
        <thead>
          <tr>
            @for (header of headers; track $index) {
              <th>{{ header }}</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (serie of facts().series; track serie.number) {
            <tr>
              <td>{{ serie.number }}</td>
              <td>{{ serie.name }}</td>
              <td>{{ serie.format }}</td>
              <td>{{ serie.dates }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class CalendarFactsComponent {
  readonly facts = input.required<CalendarFacts>();
  protected readonly headers = SERIES_HEADERS;
}
