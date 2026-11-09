import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { CardImageService } from '../cards/card-image.service';
import { CollectionService } from '../collection/collection.service';
import { DeckDraft } from './deck-draft.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { deckCardCounts, EMPTY_DECK, shownDeckBlueprints } from './deck-model';
import { DeckStatsRefresher } from './deck-stats-refresher.service';

/**
 * What follows when the deck or its format changes: the "modified" mark, the Convert button, the deck stats, and what
 * the collection needs to know.
 */
@Injectable({ providedIn: 'root' })
export class DeckChanges {
  private readonly draft = inject(DeckDraft);
  private readonly formats = inject(DeckFormatSelection);
  private readonly stats = inject(DeckStatsRefresher);
  private readonly collection = inject(CollectionService);
  private readonly images = inject(CardImageService);

  readonly convertErrataEnabled = signal(false);

  private formatsLoaded = false;

  constructor() {
    // The collection counts what is on the deck side of the page, and lists the deck for "Special in Deck"
    effect(() => {
      const deck = this.draft.deck();
      const race = this.formats.raceCard();
      untracked(() => this.collection.deckCounts.set(deckCardCounts(deck, race ? [race.blueprintId] : [])));
    });
    this.collection.cardsInDeck = () =>
      shownDeckBlueprints(this.draft.deck()).map((blueprintId) => this.images.resolve(blueprintId).bareBlueprint);
  }

  /** Marks the deck as changed or saved. */
  deckModified(modified: boolean): void {
    this.draft.markModified(modified);
    if (modified) {
      this.stats.refreshSoon();
      // Adding cards to an "Anything Goes" deck allows converting it
      if (this.formats.effectiveFormat() === 'rev_tow_sta') {
        this.convertErrataEnabled.set(true);
      }
    }
  }

  /** Empties the deck (the Race card stays; it isn't part of the deck). */
  clearDeck(): void {
    this.draft.deck.set(EMPTY_DECK);
    this.stats.refreshSoon();
    this.deckModified(false);
  }

  /** The player picked another format. */
  selectFormat(index: number): void {
    this.formats.selectedFormatIndex.set(index);
    this.formatChanged();
  }

  /** From now on Anything Goes is a format the player chose, not just the page's default, so it can be converted. */
  formatsFinishedLoading(): void {
    this.formatsLoaded = true;
  }

  formatChanged(): void {
    if (this.draft.deckName() !== null) {
      this.deckModified(true);
    }
    const format = this.formats.effectiveFormat();
    // A meta-site that allows other sites makes site searches include them
    this.collection.setSiteOverride((this.formats.metaSites() ?? []).some((site) => site.siteOverride));
    this.convertErrataEnabled.set((format.includes('pc') || format === 'rev_tow_sta') && this.formatsLoaded);
    this.collection.updateFormat(format, this.formats.blockFilters(format));
    this.stats.refreshSoon();
  }
}
