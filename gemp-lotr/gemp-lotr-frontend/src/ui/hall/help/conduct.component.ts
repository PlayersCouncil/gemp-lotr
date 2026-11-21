import { Component } from '@angular/core';
import { HallArticleComponent } from './hall-article.component';

/**
 * Help › Code of Conduct (includes/help/coc.html): the rules, the penalties and the appeals. Static text.
 *
 * DOM contract: `gd-conduct` holding `gd-hall-article` with the page's `h2` sections, paragraphs and lists.
 */
@Component({
  selector: 'gd-conduct',
  imports: [HallArticleComponent],
  host: { class: 'gd-conduct' },
  templateUrl: './conduct.component.html',
})
export class ConductComponent {}
