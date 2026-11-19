import { PageLifetime } from '../platform/page-lifetime.service';
import { inject, Injectable } from '@angular/core';
import { errorMessage } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { CollectionBrowser } from '../collection/collection-browser.service';
import { FormatsService } from '../formats/formats.service';
import { Notifier } from '../notifier/notifier';
import { DeckChanges } from './deck-changes.service';
import { DeckEditing } from './deck-editing.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { DeckStatsRefresher } from './deck-stats-refresher.service';

/** Starts the page in order: the formats, then the collection, then the limited collections and RTMD leagues. */
@Injectable()
export class DeckStartup {
  private readonly api = inject(DeckbuilderApi);
  private readonly lifetime = inject(PageLifetime);
  private readonly formatsService = inject(FormatsService);
  private readonly formats = inject(DeckFormatSelection);
  private readonly changes = inject(DeckChanges);
  private readonly editing = inject(DeckEditing);
  private readonly stats = inject(DeckStatsRefresher);
  private readonly collection = inject(CollectionBrowser);
  private readonly notifier = inject(Notifier);

  init(): void {
    this.lifetime.read(this.formatsService.formats$, {
      next: (formats) => {
        this.formats.showHallFormats(formats);
        this.changes.formatChanged();
        this.editing.showNormalFilter();
        this.changes.formatsFinishedLoading();
        this.loadCollectionTypes();
      },
      error: (error: unknown) => this.notifier.tell(errorMessage(error, { 400: 'Could not retrieve formats.' })),
    });
    this.stats.start();
  }

  private loadCollectionTypes(): void {
    this.lifetime.read(this.api.getCollectionTypes(), {
      next: (types) => {
        this.collection.limitedCollections.set(types);
        this.formats.addLimitedCollections(types);
        this.changes.deckModified(false);
        this.loadRtmdLeagues();
      },
      error: (error: unknown) => this.notifier.tell(errorMessage(error)),
    });
  }

  private loadRtmdLeagues(): void {
    this.lifetime.read(this.api.getMemberRtmdLeagues(), {
      next: (leagues) => this.formats.addRtmdLeagues(leagues),
      error: (error: unknown) => this.notifier.tell(errorMessage(error)),
    });
  }
}
