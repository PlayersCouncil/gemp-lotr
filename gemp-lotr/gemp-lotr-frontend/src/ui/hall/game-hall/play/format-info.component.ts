import { Component, input, output } from '@angular/core';
import { FormatInfo } from '../../../../core/hall/play/format-info';
import { APP_URL } from '../../../../core/platform/app-paths';

/**
 * The format (i)'s text (TableFlow.renderFormatInfo): what a format is, its own description, its facts, and the link
 * to it on Help › Format Definitions (`helpLink` reports its route; the overlay closes and goes there).
 *
 * DOM contract: `gd-format-info.gd-play-form__info-text.gd-play-form__format-info` holding
 * `div.gd-play-form__format-intro`, `div.gd-play-form__format-description` (the description, or "Choose a format…"),
 * `ul.gd-play-form__format-facts` and `div.gd-play-form__format-links` with `a.gd-play-form__format-link`.
 */
@Component({
  selector: 'gd-format-info',
  host: { class: 'gd-play-form__info-text gd-play-form__format-info' },
  template: `
    <div class="gd-play-form__format-intro">{{ info().intro }}</div>
    @if (info().text; as text) {
      <div class="gd-play-form__format-description">{{ text }}</div>
    }
    @if (info().facts.length > 0) {
      <ul class="gd-play-form__format-facts">
        @for (fact of info().facts; track $index) {
          <li>{{ fact }}</li>
        }
      </ul>
    }
    @if (info().compare; as compare) {
      <div class="gd-play-form__format-links">
        {{ compare.text
        }}<a
          class="gd-play-form__format-link"
          [href]="appUrl + '/' + compare.route"
          (click)="follow($event, compare.route)"
          >{{ compare.label }}</a
        >
      </div>
    }
  `,
})
export class FormatInfoComponent {
  readonly info = input.required<FormatInfo>();
  readonly helpLink = output<string>();
  protected readonly appUrl = APP_URL;

  protected follow(event: MouseEvent, route: string): void {
    event.preventDefault();
    this.helpLink.emit(route);
  }
}
