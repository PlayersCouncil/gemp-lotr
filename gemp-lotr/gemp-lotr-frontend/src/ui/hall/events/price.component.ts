import { Component, input } from '@angular/core';
import { Price } from '../../../core/hall/events/events-models';

/**
 * A price (common.js formatPrice): the gold, the gold coin, a space, the silver in two digits and the silver coin.
 *
 * DOM contract: `gd-price.gd-price` (`aria-label` "10 gold 50 silver") holding `span.gd-price__gold` and
 * `span.gd-price__silver`, each its number followed by its coin (`span.gd-price__coin`, `data-coin` gold / silver).
 */
@Component({
  selector: 'gd-price',
  styleUrl: './price.component.css',
  host: { class: 'gd-price', '[attr.aria-label]': 'price().gold + " gold " + price().silver + " silver"' },
  template: `<span class="gd-price__gold" aria-hidden="true"
      >{{ price().gold }}<span class="gd-price__coin" data-coin="gold"></span></span
    >{{ ' '
    }}<span class="gd-price__silver" aria-hidden="true"
      >{{ price().silver }}<span class="gd-price__coin" data-coin="silver"></span
    ></span>`,
})
export class PriceComponent {
  readonly price = input.required<Price>();
}
