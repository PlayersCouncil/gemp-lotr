import { Component, ViewEncapsulation } from '@angular/core';

/**
 * The hall's prose pages (hall.css .article: How Do I Play?, Code of Conduct, What Are Leagues?, Contribute, About):
 * the text in a centred column at least 800px wide, at 120% of the hall's size. The page's own markup is projected,
 * so its rules are not scoped to this component; they all start at `.gd-hall-article`.
 *
 * DOM contract: `gd-hall-article.gd-hall-article` (a block) holding the page's headings, paragraphs and lists;
 * `.gd-hall-article__emphasis` (the legacy .lemph), `figure.gd-hall-article__picture` (the legacy .helpimg: an image
 * and its optional `figcaption`) and `table.gd-hall-article__table` (the legacy table.tables).
 */
@Component({
  selector: 'gd-hall-article',
  styleUrl: './hall-article.component.css',
  encapsulation: ViewEncapsulation.None,
  host: { class: 'gd-hall-article' },
  template: '<ng-content />',
})
export class HallArticleComponent {}
