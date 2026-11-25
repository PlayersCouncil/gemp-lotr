import { Component } from '@angular/core';
import { HallArticleComponent } from '../help/hall-article.component';

/** Liberapay's own button picture, as the legacy page showed it. */
export const LIBERAPAY_BUTTON = 'https://liberapay.com/assets/widgets/donate.svg';

/**
 * Server Info › Contribute (includes/info/contribute.html): "How can I help?". Static text; its links open in the
 * same tab, as the legacy ones did.
 *
 * DOM contract: `gd-contribute` holding `gd-hall-article` with an `h1` and a list of links.
 */
@Component({
  selector: 'gd-contribute',
  imports: [HallArticleComponent],
  host: { class: 'gd-contribute' },
  templateUrl: './contribute.component.html',
})
export class ContributeComponent {
  protected readonly liberapayButton = LIBERAPAY_BUTTON;
}
