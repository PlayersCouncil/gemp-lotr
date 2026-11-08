import { computed, DestroyRef, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { ApiError, GENERIC_COMMUNICATION_ERROR } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { Deck, GempFormat, League, MetaSite } from '../api/models';
import { CardImageService } from '../cards/card-image.service';
import { CollectionService, DEFAULT_FILTER_OVERRIDE } from '../collection/collection.service';
import { FormatsService } from '../formats/formats.service';
import { Notifier } from '../notifier/notifier';
import { formatUsesMap } from './deck-contents';
import { formatDecklist } from './deck-import';
import { DeckStats, EMPTY_STATS, parseDeckStats } from './deck-stats';
import {
  addBySide,
  addDrawCopy,
  addToSlot,
  deckCardCounts,
  deckContentsOf,
  DeckSlot,
  DeckState,
  DrawSide,
  EMPTY_DECK,
  removeDrawCopy,
  removeFromSlot,
  shownDeckBlueprints,
} from './deck-model';
import {
  FormatOption,
  hallFormatOptions,
  INITIAL_FORMAT_OPTIONS,
  limitedFormatOptions,
  optionIndexOf,
  rtmdFormatOptions,
} from './format-options';

/** How often the deck stats are refreshed after a change (deckBuildingUi.checkDirtyInterval). */
export const STATS_INTERVAL_MS = 500;

/** The predefined collection filters for the special slots (showPredefinedFilter). */
const SLOT_FILTERS: Partial<Record<DeckSlot, string>> = {
  ringBearer: 'canStartWithRing:true product:card',
  ring: 'cardType:THE_ONE_RING product:card',
  map: 'cardType:MAP product:card',
};

/** The deck name shown in the toolbar; a modified deck is shown in orange with a "*". */
export interface DeckLabel {
  text: string;
  modified: boolean;
}

/** The meta-site shown in the Race slot: the position card, with the modifier's lower part over it. */
export interface RaceCard {
  blueprintId: string;
  overlayBlueprintId: string | null;
}

/**
 * The deck being edited and everything around it, ported from the legacy deckBuildingUi.js: the slots and piles, the
 * format list (with Map and Race slots), the deck stats, unsaved-changes tracking with its prompts, and the New, Save,
 * Rename, Copy and Convert to PC Errata buttons. Messages and questions go through the Notifier.
 */
@Injectable({ providedIn: 'root' })
export class DeckService {
  private readonly api = inject(DeckbuilderApi);
  private readonly collection = inject(CollectionService);
  private readonly formatsService = inject(FormatsService);
  private readonly notifier = inject(Notifier);
  private readonly images = inject(CardImageService);
  private readonly destroyRef = inject(DestroyRef);

  readonly deck = signal<DeckState>(EMPTY_DECK);
  /** Null for a deck that was never saved. */
  readonly deckName = signal<string | null>(null);
  readonly notes = signal('');
  readonly label = signal<DeckLabel>({ text: 'New deck', modified: false });
  /** Unsaved changes. */
  readonly contentsDirty = signal(false);
  /** The server's deck stats (counts and the format's verdict), as data. */
  readonly stats = signal<DeckStats>(EMPTY_STATS);
  readonly convertErrataEnabled = signal(false);
  /** Whether the format has a Map slot. */
  readonly usesMap = signal(false);
  /** Whether the format is a Race to Mount Doom league, which shows the Race slot. */
  readonly usesRace = signal(false);

  readonly formatOptions = signal<readonly FormatOption[]>(INITIAL_FORMAT_OPTIONS);
  readonly selectedFormatIndex = signal(0);
  readonly selectedFormat = computed(
    () => this.formatOptions()[this.selectedFormatIndex()] ?? INITIAL_FORMAT_OPTIONS[0],
  );
  /** The format code for the server: an RTMD league's own format, otherwise the selected one. */
  readonly effectiveFormat = computed(() => {
    const option = this.selectedFormat();
    return option.rtmd ? (this.rtmdLeagues.get(option.value)?.formatCode ?? option.value) : option.value;
  });

  /** The current Race to Mount Doom meta-sites (the last one is shown), or null outside RTMD. */
  readonly metaSites = signal<readonly MetaSite[] | null>(null);
  readonly raceCard = computed<RaceCard | null>(() => {
    const sites = this.metaSites();
    const current = sites?.at(-1);
    if (!current) {
      return null;
    }
    return {
      blueprintId: current.visualBlueprintId ?? current.blueprintId,
      overlayBlueprintId: current.visualBlueprintId ? current.blueprintId : null,
    };
  });

  /** Where a collection card clicked now goes: a special slot after clicking it, otherwise by the card's side. */
  private selectionSlot: DeckSlot | null = null;
  private formats: Record<string, GempFormat> = {};
  private formatsInitialized = false;
  private rtmdLeagues = new Map<string, League>();
  private validationDirty = true;

  constructor() {
    // The collection counts what is on the deck side of the page, and lists the deck for "Special in Deck"
    effect(() => {
      const deck = this.deck();
      const race = this.raceCard();
      untracked(() => this.collection.deckCounts.set(deckCardCounts(deck, race ? [race.blueprintId] : [])));
    });
    this.collection.cardsInDeck = () =>
      shownDeckBlueprints(this.deck()).map((blueprintId) => this.images.resolve(blueprintId).bareBlueprint);
  }

  /**
   * Starts the page as the legacy one did: the formats, then the collection with the default filter, then the limited
   * collections and RTMD leagues; the deck stats are refreshed from then on.
   */
  init(): void {
    this.formatsService.formats$.subscribe({
      next: (formats) => {
        this.formats = formats;
        const current = this.selectedFormat().value;
        const options = hallFormatOptions(formats);
        this.formatOptions.set(options);
        this.selectedFormatIndex.set(optionIndexOf(options, current));
        this.formatChanged();
        this.showNormalFilter();
        this.formatsInitialized = true;
        this.loadCollectionTypes();
      },
      error: (error: unknown) =>
        this.tell(ApiError.from(error).is(400) ? 'Could not retrieve formats.' : GENERIC_COMMUNICATION_ERROR),
    });
    const timer = setInterval(() => {
      if (this.validationDirty) {
        this.validationDirty = false;
        this.updateDeckStats();
      }
    }, STATS_INTERVAL_MS);
    this.destroyRef.onDestroy(() => clearInterval(timer));
  }

  /** The player picked another format. */
  selectFormat(index: number): void {
    this.selectedFormatIndex.set(index);
    this.formatChanged();
  }

  // --- Toolbar ---

  async newDeck(): Promise<void> {
    if (await this.savePromptOnNew()) {
      this.deckName.set(null);
      // Fixed: the legacy page kept the previous deck's notes, which were then saved with the new deck
      this.notes.set('');
      this.clearDeck();
    }
  }

  /** Saves, asking for a name first if the deck has none. False if the player cancelled or the name was invalid. */
  async saveCurrentDeck(): Promise<boolean> {
    const name = this.deckName();
    if (name !== null) {
      this.saveDeck();
      return true;
    }
    const newName = await this.notifier.askText('Enter the name of the deck', '');
    if (newName === null) {
      return false;
    }
    if (newName.length < 3 || newName.length > 100) {
      await this.notifier.alert('Deck name has to have at least 3 characters and at most 100 characters.');
      return false;
    }
    this.deckName.set(newName);
    this.label.set({ text: newName, modified: false });
    this.saveDeck();
    return true;
  }

  async renameCurrentDeck(): Promise<void> {
    const name = this.deckName();
    if (name === null) {
      await this.saveCurrentDeck();
      return;
    }
    await this.renameDeck(name, async (newName) => {
      // Fixed: the legacy page saved the changes under the old name, re-creating the deck it had just renamed
      this.deckName.set(newName);
      if (this.contentsDirty() && (await this.notifier.confirm('Do you wish to save this deck?'))) {
        this.saveDeck();
      }
      this.deckModified(this.contentsDirty());
    });
  }

  /** Asks for a new name and renames the saved deck on the server (also used by the deck list). */
  async renameDeck(oldName: string, renamed: (newName: string) => void | Promise<void>): Promise<void> {
    const newName = await this.notifier.askText('Enter new name for the deck', oldName);
    if (newName === null) {
      return;
    }
    if (newName.length < 3 || newName.length > 100) {
      await this.notifier.alert('Deck name has to have at least 3 characters and at most 100 characters.');
      return;
    }
    this.api.renameDeck(oldName, newName).subscribe({
      next: () => void renamed(newName),
      error: (error: unknown) =>
        this.tell(
          ApiError.from(error).is(404)
            ? "Couldn't find the deck to rename on the server."
            : GENERIC_COMMUNICATION_ERROR,
        ),
    });
  }

  /** "Copy deck to new": the same cards, saved under a new name next time. */
  copyDeck(): void {
    this.deckName.set(null);
    this.deckModified(true);
  }

  convertErrata(): void {
    const notes = this.notes();
    this.api.convertErrata(this.effectiveFormat(), this.deckContents()).subscribe({
      next: (deck) => {
        if (deck) {
          this.setupDeck(deck, this.deckName());
          // Fixed: the converted deck comes back without notes, and the legacy page dropped them
          this.notes.set(notes);
        }
        this.deckModified(true);
        this.convertErrataEnabled.set(false);
      },
      error: (error: unknown) => {
        if (ApiError.from(error).is(400)) {
          this.convertErrataEnabled.set(true);
          this.tell('Error processing errata conversion.');
        } else {
          this.tell(GENERIC_COMMUNICATION_ERROR);
        }
      },
    });
  }

  /**
   * Before leaving the deck (deck list, library, import): offers to save unsaved changes. False if the player wanted
   * to save but the save did not start.
   */
  async savePrompt(): Promise<boolean> {
    return (
      !this.contentsDirty() ||
      ((await this.notifier.confirm(
        'Your deck has been modified.  Would you like to save your deck before proceeding?',
      )) &&
        (await this.saveCurrentDeck()))
    );
  }

  /** Before a new deck: OK saves, Cancel discards. False if saving was cancelled at the name prompt. */
  async savePromptOnNew(): Promise<boolean> {
    if (!this.contentsDirty()) {
      return true;
    }
    if (
      await this.notifier.confirm(
        "Your deck has been modified.  Click 'OK' to save, or 'Cancel' to discard your changes without saving.",
      )
    ) {
      return this.saveCurrentDeck();
    }
    return true;
  }

  // --- Deck list, library, import, notes ---

  /** Opens one of the player's decks (deck list's "Open deck"). */
  loadDeck(name: string): void {
    this.api.getDeck(name).subscribe({
      next: (deck) => deck && this.setupDeck(deck, name),
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  /** Opens a library deck: it keeps the library deck's name and counts as unsaved. */
  loadLibraryDeck(name: string): void {
    this.api.getLibraryDeck(name).subscribe({
      next: (deck) => {
        if (deck) {
          this.setupDeck(deck, name);
          this.deckModified(true);
        }
      },
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  /** A saved deck was renamed from the deck list: the open deck follows if it was that one. */
  deckRenamed(oldName: string, newName: string): void {
    if (this.deckName() === oldName) {
      this.deckName.set(newName);
      this.deckModified(this.contentsDirty());
    }
  }

  /** A saved deck was deleted from the deck list: if it was the open one, a new deck takes its place. */
  deckDeleted(name: string): void {
    if (this.deckName() === name) {
      this.deckName.set(null);
      this.clearDeck();
    }
  }

  /**
   * Replaces the deck with the cards the server recognises in a pasted decklist (parseDecklist). The deck has no name
   * and is shown as "Imported Deck (unsaved)".
   */
  importDecklist(text: string): void {
    this.deckName.set(null);
    this.clearDeck();
    this.api.importDecklist(formatDecklist(text)).subscribe({
      next: (items) => {
        let state = this.deck();
        for (const item of items) {
          if (item.kind !== 'card') {
            continue;
          }
          for (let copy = 0; copy < item.count; copy++) {
            if (item.group === 'ringBearer' || item.group === 'ring' || item.group === 'map') {
              state = addToSlot(state, item.group, item.blueprintId);
            } else {
              state = addBySide(state, item.blueprintId, item.side);
            }
          }
        }
        this.deck.set(state);
        this.deckModified(true);
        this.label.set({ text: 'Imported Deck (unsaved)', modified: false });
      },
      error: (error: unknown) =>
        this.tell(ApiError.from(error).is(414) ? 'Deck too large to import.' : GENERIC_COMMUNICATION_ERROR),
    });
  }

  /** The notes were edited (the notes dialog). */
  setNotes(notes: string): void {
    if (notes !== this.notes()) {
      this.notes.set(notes);
      this.deckModified(true);
    }
  }

  // --- Cards ---

  /** A collection card was clicked: it goes to the slot being filled, or by its side. */
  addFromCollection(blueprintId: string, side: string | null): void {
    const slot = this.selectionSlot;
    if (slot) {
      this.deck.update((deck) => addToSlot(deck, slot, blueprintId));
      this.showNormalFilter();
    } else {
      this.deck.update((deck) => addBySide(deck, blueprintId, side));
    }
    this.deckModified(true);
  }

  removeSlotCard(slot: DeckSlot, id: number): void {
    this.deck.update((deck) => removeFromSlot(deck, slot, id));
    this.deckModified(true);
  }

  removeDrawCard(shownId: number): void {
    this.deck.update((deck) => removeDrawCopy(deck, shownId));
    this.deckModified(true);
  }

  /**
   * A slot was clicked. An empty Ring-bearer, Ring or Map slot shows the cards that fit it; the next collection card
   * clicked goes there. Clicking the sites (even on a site) shows the sites, by site number.
   */
  slotClicked(slot: DeckSlot): void {
    if (slot === 'sites') {
      this.collection.setField('cardType', 'SITE');
      this.collection.setField('sort', 'siteNumber,name');
      return;
    }
    const filter = SLOT_FILTERS[slot];
    if (filter && this.deck()[slot].length === 0) {
      this.selectionSlot = slot;
      this.collection.setFilterOverride(filter);
    }
  }

  /** Shows a deck loaded from the server (setupDeck). */
  setupDeck(deck: Deck, name: string | null): void {
    this.clearDeck();
    this.deckName.set(name);
    this.label.set({ text: name ?? 'New deck', modified: false });
    this.notes.set(deck.notes);
    let state = EMPTY_DECK;
    for (const blueprintId of deck.ringBearer ? [deck.ringBearer] : [])
      state = addToSlot(state, 'ringBearer', blueprintId);
    for (const blueprintId of deck.ring ? [deck.ring] : []) state = addToSlot(state, 'ring', blueprintId);
    for (const blueprintId of deck.map ? [deck.map] : []) state = addToSlot(state, 'map', blueprintId);
    for (const blueprintId of deck.sites) state = addToSlot(state, 'sites', blueprintId);
    for (const card of deck.drawDeck) {
      const side = card.side;
      if (side === 'FREE_PEOPLE' || side === 'SHADOW') {
        state = addDrawCopy(state, card.blueprintId, side as DrawSide);
      }
    }
    this.deck.set(state);
    if (deck.drawDeck.length > 0) {
      // The legacy page marked the deck modified for each card it added (which can enable Convert); reset below
      this.deckModified(true);
    }

    const target = deck.targetFormat;
    if (target) {
      // A deck of a running RTMD league selects the league, otherwise its format
      const value =
        target.leagueCode && this.rtmdLeagues.has(target.leagueCode) ? target.leagueCode : target.formatCode;
      this.selectFormat(optionIndexOf(this.formatOptions(), value));
    }
    this.collection.load();
    this.deckModified(false);
  }

  /** The deckContents string for the server. */
  deckContents(): string {
    return deckContentsOf(this.deck(), formatUsesMap(this.effectiveFormat()));
  }

  // --- Internals ---

  /** Marks the deck as changed or saved (deckModified). */
  deckModified(modified: boolean): void {
    const name = this.deckName() ?? 'New deck';
    if (modified) {
      this.validationDirty = true;
      this.contentsDirty.set(true);
      this.label.set({ text: name, modified: true });
      // Adding cards to an "Anything Goes" deck allows converting it
      if (this.effectiveFormat() === 'rev_tow_sta') {
        this.convertErrataEnabled.set(true);
      }
    } else {
      this.contentsDirty.set(false);
      this.label.set({ text: name, modified: false });
    }
  }

  /** Empties the deck (the Race card stays; it isn't part of the deck). */
  private clearDeck(): void {
    this.deck.set(EMPTY_DECK);
    this.validationDirty = true;
    this.deckModified(false);
  }

  private formatChanged(): void {
    if (this.deckName() !== null) {
      this.deckModified(true);
    }
    const format = this.effectiveFormat();
    const option = this.selectedFormat();
    this.usesMap.set(formatUsesMap(format));
    const league = option.rtmd ? this.rtmdLeagues.get(option.value) : undefined;
    this.setRace(league ? league.metaSites : null);
    // PC formats and Anything Goes can be converted, though not when Anything Goes is just the page's default
    this.convertErrataEnabled.set((format.includes('pc') || format === 'rev_tow_sta') && this.formatsInitialized);
    this.collection.updateFormat(format, this.formats[format]?.blockFilters ?? null);
    this.validationDirty = true;
  }

  private setRace(metaSites: readonly MetaSite[] | null): void {
    this.metaSites.set(metaSites && metaSites.length > 0 ? metaSites : null);
    this.usesRace.set(metaSites !== null);
    // A meta-site that allows other sites makes site searches include them
    this.collection.setSiteOverride((metaSites ?? []).some((site) => site.siteOverride));
  }

  private showNormalFilter(): void {
    this.selectionSlot = null;
    this.collection.setFilterOverride(DEFAULT_FILTER_OVERRIDE);
  }

  private loadCollectionTypes(): void {
    this.api.getCollectionTypes().subscribe({
      next: (types) => {
        this.collection.limitedCollections.set(types);
        this.formatOptions.update((options) => [...options, ...limitedFormatOptions(types)]);
        this.deckModified(false);
        this.loadRtmdLeagues();
      },
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  private loadRtmdLeagues(): void {
    this.api.getMemberRtmdLeagues().subscribe({
      next: (leagues) => {
        this.rtmdLeagues = new Map(leagues.map((league) => [league.code, league]));
        this.formatOptions.update((options) => [...options, ...rtmdFormatOptions(leagues)]);
      },
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  private saveDeck(): void {
    const name = this.deckName();
    if (name === null) {
      return;
    }
    // The RTMD option's value is the league code, which the server stores so the deck stays with the league
    this.api.saveDeck(name, this.selectedFormat().value, this.notes(), this.deckContents()).subscribe({
      next: () => {
        this.deckModified(false);
        this.tell('Deck was saved.  Refresh the Game Hall to see it!');
      },
      error: (error: unknown) =>
        this.tell(ApiError.from(error).is(400) ? 'Invalid deck format.' : GENERIC_COMMUNICATION_ERROR),
    });
  }

  private updateDeckStats(): void {
    const option = this.selectedFormat();
    this.api
      .getDeckStatsHtml(
        this.deckContents(),
        this.effectiveFormat(),
        option.collectionType,
        option.rtmd ? option.value : null,
      )
      .subscribe({
        next: (html) => this.stats.set(parseDeckStats(html)),
        error: (error: unknown) =>
          this.tell(ApiError.from(error).is(400) ? 'Invalid deck for getting stats.' : GENERIC_COMMUNICATION_ERROR),
      });
  }

  /** A message that needs no answer. */
  private tell(message: string): void {
    void this.notifier.alert(message);
  }
}
