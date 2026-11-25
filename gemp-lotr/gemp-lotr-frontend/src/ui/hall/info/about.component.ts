import { Component } from '@angular/core';
import { HallArticleComponent } from '../help/hall-article.component';

/**
 * Server Info › About (info.html #about): GEMP's history and its contributors. Static text; its links open in the
 * same tab, as the legacy ones did. (The legacy list sat inside a paragraph; here it follows it.)
 *
 * DOM contract: `gd-about` holding `gd-hall-article` with an `h1`, paragraphs and the contributors' list.
 */
@Component({
  selector: 'gd-about',
  imports: [HallArticleComponent],
  host: { class: 'gd-about' },
  templateUrl: './about.component.html',
})
export class AboutComponent {}
