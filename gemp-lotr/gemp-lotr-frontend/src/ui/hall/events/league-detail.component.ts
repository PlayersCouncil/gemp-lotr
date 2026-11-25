import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardViews } from '../../../core/cards/card-views.service';
import { LeagueDetail, LeagueStanding, RtmdMetaSite } from '../../../core/hall/events/events-models';
import * as words from '../../../core/hall/events/events-messages';
import { LeagueResults } from '../../../core/hall/events/league-results.service';
import { leagueMembership, rtmdSummary, serieSummary, standingsHalves } from '../../../core/hall/events/league-view';
import { PageNavigation } from '../../../core/platform/page-navigation';
import { MarkupComponent } from '../../shared/chat/markup.component';
import { EventsButtonComponent } from './events-button.component';
import { FormatDescriptionComponent } from './format-description.component';
import { PriceComponent } from '../shared/price.component';

/**
 * A league's detail in a drawer (leagueResultsUi.js loadedLeague, with showName false): its cost, the membership line
 * (Join league, Go to draft), the description, a Race to Mount Doom league's path, each serie's dates, format and
 * collection, and the results tabs (Overall results, Your league matches, Serie n). The tab chosen stays when the
 * league is drawn again (a refresh, a join). A format still in use opens its description; a meta-site's card shows
 * the card on a click or right-click.
 *
 * Inputs: `league` (required); `membership` (false: a finished league, no join or draft; default true).
 *
 * DOM contract: `gd-league-detail.gd-league-detail` (`data-context=history` without membership: it then takes the
 * completed-events table's cell look) holding `div.gd-league-detail__cost` (`gd-price`),
 * `div.gd-league-detail__membership` (`data-membership` = member, joinable, inviteOnly) with
 * `button.gd-events-button[data-action=join|draft]`, `gd-markup.gd-league-detail__description`, `hr`s,
 * `section.gd-league-detail__rtmd` (`h3`, `p.gd-league-detail__muted`s, `div.gd-league-detail__level`,
 * `div.gd-league-detail__path` of `button.gd-league-detail__site` (`data-mark` = current, passed, none) with
 * `img.gd-league-detail__site-image`, `img.gd-league-detail__site-overlay`, `span.gd-league-detail__site-number`),
 * `div.gd-league-detail__serie` (`data-serie`) each with `.gd-league-detail__serie-name`, the format
 * (`button.gd-league-detail__format` when it can be described) and the collection, then `div.gd-league-detail__tabs`
 * with `div[role=tablist]` of `button[role=tab]` (`aria-selected`, `data-tab` = overall, matches, serie-<n>) and the
 * `div[role=tabpanel]`; standings are `table.gd-league-detail__standings`.
 */
@Component({
  selector: 'gd-league-detail',
  styleUrl: './league-detail.component.css',
  imports: [EventsButtonComponent, MarkupComponent, FormatDescriptionComponent, NgTemplateOutlet, PriceComponent],
  host: { class: 'gd-league-detail', '[attr.data-context]': "membership() ? null : 'history'" },
  templateUrl: './league-detail.component.html',
})
export class LeagueDetailComponent {
  private readonly leagues = inject(LeagueResults);
  private readonly navigation = inject(PageNavigation);
  private readonly images = inject(CardImageService);
  private readonly views = inject(CardViews);
  private readonly info = inject(CardInfoService);

  readonly league = input.required<LeagueDetail>();
  readonly membership = input(true);

  protected readonly words = words;
  protected readonly member = computed(() => leagueMembership(this.league(), this.membership()));
  protected readonly draftUrl = computed(() => {
    const member = this.member();
    return member.kind === 'member' ? member.draftUrl : null;
  });
  protected readonly series = computed(() =>
    this.league().series.map((serie) => ({ serie, summary: serieSummary(serie) })),
  );
  protected readonly rtmd = computed(() => {
    const path = this.league().rtmd;
    return path ? rtmdSummary(path) : null;
  });
  protected readonly overall = computed(() => standingsHalves(this.league().standings));
  /** The results tab chosen: 0 Overall results, 1 Your league matches, 2 + n Serie n. */
  private readonly chosen = signal(0);
  protected readonly tab = computed(() => (this.chosen() < 2 + this.league().series.length ? this.chosen() : 0));
  protected readonly formatCode = signal<string | null>(null);

  protected choose(index: number): void {
    this.chosen.set(index);
  }

  protected halves(standings: readonly LeagueStanding[]) {
    return standingsHalves(standings);
  }

  protected join(): void {
    this.leagues.requestJoin(this.league());
  }

  protected draft(url: string): void {
    this.navigation.openInNewTab(url);
  }

  protected imageOf(blueprintId: string): string {
    return this.images.imageUrl(blueprintId);
  }

  /** The meta-site's card: the position's visual card with the modifier over its bottom. */
  protected showSite(site: RtmdMetaSite, event: Event): void {
    event.preventDefault();
    this.info.show(
      this.views.create({
        ref: null,
        blueprintId: site.visualBlueprintId ?? site.blueprintId,
        zone: null,
        overlayBlueprintId: site.visualBlueprintId ? site.blueprintId : null,
      }),
    );
  }
}
