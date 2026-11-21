import { Component, inject, signal } from '@angular/core';
import {
  ACCOUNT_HEADING,
  IGNORE_BUTTON,
  IGNORE_LABEL,
  IGNORE_NAME_MAX,
  IGNORE_PLACEHOLDER,
  IGNORE_REMOVE,
  ignoreRemoveLabel,
  IGNORES_EMPTY,
  IGNORES_HEADING,
  IGNORES_HELP,
  MIGRATE_TROPHIES,
} from '../../../core/hall/account/account-messages';
import { AccountOverview } from '../../../core/hall/account/account-overview.service';
import { PlayerSearch } from '../../../core/hall/play/player-picker';
import { siteImage } from '../../../core/platform/app-paths';
import { PlayerPickerComponent } from '../shared/player-picker.component';

/**
 * My Account > My Account (account.html #account): the heading with the player's name, the pocket at the right (gold
 * and silver, hall.css .pocket), "Migrate Trophies to My Cards" for a player who still has trophies, and the ignored
 * players: the help, the "Ignore a player:" form (the hall's player picker), the status line and the list.
 *
 * DOM contract: `gd-account-overview.gd-account-overview` holding `h1.gd-account-overview__heading` (the name in
 * `span.gd-account-overview__name`), `div.gd-account-overview__pocket` (the gold, `img`, the silver,
 * `img`; empty before the hall's first answer), `button.gd-account-overview__migrate` (only with trophies),
 * `section.gd-account-overview__ignores` with `h2`, `p.gd-account-overview__help`, `form.gd-account-overview__add`
 * (`label`, `gd-player-picker`, `button[type=submit]`), `div.gd-account-overview__status` (`role=status`),
 * `ul.gd-account-overview__list` of `li` (`span.gd-account-overview__ignored`, `button.gd-account-overview__remove`
 * with `aria-label`) and `p.gd-account-overview__empty` (once the list has come and is empty).
 */
@Component({
  selector: 'gd-account-overview',
  styleUrl: './account-overview.component.css',
  imports: [PlayerPickerComponent],
  host: { class: 'gd-account-overview' },
  template: `
    <h1 class="gd-account-overview__heading">
      {{ heading }}<span class="gd-account-overview__name">{{ overview.name() }}</span>
    </h1>
    <div class="gd-account-overview__pocket">
      @if (overview.pocket(); as pocket) {
        {{ pocket.gold }}<img [src]="gold" alt="gold" />&ngsp;{{ pocket.silver }}<img [src]="silver" alt="silver" />
      }
    </div>
    @if (overview.migrateShown()) {
      <div>
        <button type="button" class="gd-account-overview__migrate" (click)="overview.migrateTrophies()">
          <span>{{ migrateLabel }}</span>
        </button>
      </div>
    }
    <section class="gd-account-overview__ignores" aria-labelledby="gd-account-ignores-heading">
      <h2 id="gd-account-ignores-heading">{{ ignoresHeading }}</h2>
      <p class="gd-account-overview__help">{{ ignoresHelp }}</p>
      <form class="gd-account-overview__add" autocomplete="off" (submit)="add($event)">
        <label for="gd-account-ignore-name">{{ ignoreLabel }}</label>
        <gd-player-picker
          fieldId="gd-account-ignore-name"
          [placeholder]="ignorePlaceholder"
          [maxLength]="ignoreMax"
          [suggest]="suggest"
          [(value)]="name"
        />
        <button type="submit">{{ ignoreButton }}</button>
      </form>
      <div class="gd-account-overview__status" role="status" aria-live="polite">{{ overview.status() }}</div>
      <ul class="gd-account-overview__list">
        @for (ignored of overview.ignored() ?? []; track ignored) {
          <li>
            <span class="gd-account-overview__ignored">{{ ignored }}</span>
            <button
              type="button"
              class="gd-account-overview__remove"
              [attr.aria-label]="removeLabel(ignored)"
              (click)="overview.remove(ignored)"
            >
              {{ removeText }}
            </button>
          </li>
        }
      </ul>
      @if (overview.ignored()?.length === 0) {
        <p class="gd-account-overview__help gd-account-overview__empty">{{ empty }}</p>
      }
    </section>
  `,
})
export class AccountOverviewComponent {
  protected readonly overview = inject(AccountOverview);
  private readonly search = inject(PlayerSearch);

  protected readonly name = signal('');
  protected readonly suggest = (term: string) => this.search.suggest(term);
  protected readonly gold = siteImage('gold.png');
  protected readonly silver = siteImage('silver.png');
  protected readonly heading = ACCOUNT_HEADING;
  protected readonly migrateLabel = MIGRATE_TROPHIES;
  protected readonly ignoresHeading = IGNORES_HEADING;
  protected readonly ignoresHelp = IGNORES_HELP;
  protected readonly ignoreLabel = IGNORE_LABEL;
  protected readonly ignorePlaceholder = IGNORE_PLACEHOLDER;
  protected readonly ignoreMax = IGNORE_NAME_MAX;
  protected readonly ignoreButton = IGNORE_BUTTON;
  protected readonly removeText = IGNORE_REMOVE;
  protected readonly removeLabel = ignoreRemoveLabel;
  protected readonly empty = IGNORES_EMPTY;

  /** "Ignore": the field is emptied unless the name was refused. */
  protected async add(event: Event): Promise<void> {
    event.preventDefault();
    if (await this.overview.add(this.name())) {
      this.name.set('');
    }
  }
}
