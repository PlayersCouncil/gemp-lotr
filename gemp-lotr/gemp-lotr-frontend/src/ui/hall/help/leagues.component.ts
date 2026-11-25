import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { siteImage } from '../../../core/platform/app-paths';
import { HallArticleComponent } from './hall-article.component';

/**
 * Help › What Are Leagues? (includes/help/leagueRules.html): what a League is, joining, Sealed decks, gold, the prizes
 * and cheating, with the site's screenshots (images/help/league-*.jpg). Static text. The legacy markup's headings
 * opened as h1 and closed as h2, and its pictures sat inside paragraphs; here each is what the browser made of it.
 *
 * DOM contract: `gd-leagues` holding `gd-hall-article` with `h1`s (ids join, sealed, gold, prizes, cheating),
 * `.gd-hall-article__emphasis`, `figure.gd-hall-article__picture`s, two `table.gd-hall-article__table`s, and
 * `a.gd-leagues__link`, the router link to Events › Current Leagues (plain text on the legacy page).
 */
@Component({
  selector: 'gd-leagues',
  imports: [HallArticleComponent, RouterLink],
  host: { class: 'gd-leagues' },
  templateUrl: './leagues.component.html',
})
export class LeaguesComponent {
  protected picture(name: string): string {
    return siteImage(`help/${name}`);
  }
}
