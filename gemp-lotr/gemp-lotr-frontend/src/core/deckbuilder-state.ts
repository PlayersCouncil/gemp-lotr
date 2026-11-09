import { Provider } from '@angular/core';
import { DeckbuilderActions } from './actions/deckbuilder-actions.service';
import { provideCardInteractions } from './cards/card-interactions';
import { CollectionBrowser } from './collection/collection-browser.service';
import { CollectionCounts } from './collection/collection-counts.service';
import { CollectionQuery } from './collection/collection-query.service';
import { DeckChanges } from './deck/deck-changes.service';
import { DeckDraft } from './deck/deck-draft.service';
import { DeckEditing } from './deck/deck-editing.service';
import { DeckFormatSelection } from './deck/deck-format-selection.service';
import { DeckLibraryService } from './deck/deck-library.service';
import { DeckOpener } from './deck/deck-opener.service';
import { DeckSaver } from './deck/deck-saver.service';
import { DeckStartup } from './deck/deck-startup.service';
import { DeckStatsRefresher } from './deck/deck-stats-refresher.service';
import { PackDeliveryService } from './delivery/pack-delivery.service';
import { FormatsService } from './formats/formats.service';
import { PacksService } from './packs/packs.service';
import { PageLifetime } from './platform/page-lifetime.service';

/** Shared with the legacy page. */
export const DECKBUILDER_AUTO_ZOOM_COOKIE = 'autoZoomInDeckbuilder';

/**
 * The deck builder's state, for the providers of its page: it is created when the page opens and destroyed when the
 * page closes, so a later visit starts fresh. (Not a route's providers: Angular keeps those for the whole app.)
 */
export function provideDeckbuilderState(): Provider[] {
  return [
    PageLifetime,
    DeckDraft,
    DeckFormatSelection,
    DeckStatsRefresher,
    DeckChanges,
    DeckEditing,
    DeckOpener,
    DeckSaver,
    DeckStartup,
    DeckLibraryService,
    CollectionBrowser,
    CollectionQuery,
    CollectionCounts,
    FormatsService,
    PackDeliveryService,
    PacksService,
    ...provideCardInteractions({ actions: DeckbuilderActions, autoZoomCookie: DECKBUILDER_AUTO_ZOOM_COOKIE }),
  ];
}
