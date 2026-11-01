import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp, SERVER_URL } from './gemp-http.service';
import {
  CollectionItem,
  CollectionPage,
  CollectionTypeInfo,
  Deck,
  DeckSummary,
  FormatsReadout,
  League,
} from './models';
import {
  parseCollectionPage,
  parseCollectionTypes,
  parseDeck,
  parseDeckList,
  parseImportedCards,
  parseLeagues,
} from './parsers';

/**
 * Every server call the deck builder makes, typed. Mirrors the legacy communication.js functions used by
 * deckBuildingUi.js (named in each comment); requests and server behaviour are unchanged.
 * Failures arrive as ApiError; the statuses the legacy page handled specially are noted per method.
 */
@Injectable({ providedIn: 'root' })
export class DeckbuilderApi {
  private readonly http = inject(GempHttp);

  // --- Decks ---

  /** getDecks: the player's decks, sorted by format then name. */
  listDecks(): Observable<DeckSummary[]> {
    return this.http.getXml('/deck/list').pipe(map(parseDeckList));
  }

  /** getDeck: null if the player has no deck with that name. */
  getDeck(deckName: string): Observable<Deck | null> {
    return this.http.getXml('/deck', { deckName }).pipe(map(parseDeck));
  }

  /** saveDeck: creates or overwrites. 400 = the server could not build a deck from the contents. */
  saveDeck(deckName: string, targetFormat: string, notes: string, deckContents: string): Observable<void> {
    return this.http
      .postXml('/deck', { deckName, targetFormat, notes, deckContents })
      .pipe(map(() => undefined));
  }

  /** renameDeck: answers the renamed deck. 404 = no deck with the old name. */
  renameDeck(oldDeckName: string, deckName: string): Observable<Deck | null> {
    return this.http.postXml('/deck/rename', { oldDeckName, deckName }).pipe(map(parseDeck));
  }

  /** deleteDeck */
  deleteDeck(deckName: string): Observable<void> {
    return this.http.postXml('/deck/delete', { deckName }).pipe(map(() => undefined));
  }

  /** shareDeck: a public URL showing the deck as a web page. */
  shareDeckUrl(deckName: string): Observable<string> {
    return this.http.getText('/deck/share', { deckName });
  }

  /**
   * getDeckStats: an HTML fragment with the FP/Shadow counts and the format validation result, shown as-is.
   * 400 = the server could not build a deck from the contents.
   */
  getDeckStatsHtml(
    deckContents: string,
    targetFormat: string,
    collectionName: string,
    leagueCode: string | null,
  ): Observable<string> {
    return this.http.postText('/deck/stats', { targetFormat, collectionName, deckContents, leagueCode });
  }

  /** getFormats: all formats keyed by code. 400 = the legacy page alerted "Could not retrieve formats." */
  getFormats(): Observable<FormatsReadout> {
    return this.http.postJson<FormatsReadout>('/deck/formats', { includeEvents: false }, { participant: false });
  }

  /** convertErrata: the same deck with the format's errata versions swapped in. 400 = invalid contents. */
  convertErrata(targetFormat: string, deckContents: string): Observable<Deck | null> {
    return this.http.postXml('/deck/convert', { targetFormat, deckContents }).pipe(map(parseDeck));
  }

  // --- Library (decks owned by the "Librarian" account) ---

  /** getLibraryDecks: starter decks first, then championship decks. */
  listLibraryDecks(): Observable<DeckSummary[]> {
    return this.http.getXml('/deck/libraryList').pipe(map(parseDeckList));
  }

  /** getLibraryDeck */
  getLibraryDeck(deckName: string): Observable<Deck | null> {
    return this.http.getXml('/deck/library', { deckName }).pipe(map(parseDeck));
  }

  /** The page the legacy library dialog opened in a new tab. */
  libraryDeckHtmlUrl(deckName: string): string {
    return `${SERVER_URL}/deck/libraryHtml?deckName=${encodeURIComponent(deckName)}`;
  }

  // --- Collections ---

  /** getCollectionTypes: the limited (league/tournament) collections the player can build from. */
  getCollectionTypes(): Observable<CollectionTypeInfo[]> {
    return this.http.getXml('/collection').pipe(map(parseCollectionTypes));
  }

  /**
   * getCollection: one page of a collection ("default" = all cards, "permanent" = My cards, "trophy", or a type from
   * getCollectionTypes), filtered and sorted by the server. 404 = "You don't have collection of that type."
   */
  getCollection(collectionType: string, filter: string, start: number, count: number): Observable<CollectionPage> {
    return this.http
      .getXml(this.collectionPath(collectionType), { filter, start, count })
      .pipe(map(parseCollectionPage));
  }

  /** importCollection: the cards the server recognised in a pasted decklist. 414 = the decklist is too long. */
  importDecklist(decklist: string): Observable<CollectionItem[]> {
    return this.http.getXml('/collection/import/', { decklist }).pipe(map(parseImportedCards));
  }

  /** openPack: the contents arrive through PackDeliveryService. 404 = the pack could not be opened. */
  openPack(collectionType: string, packBlueprintId: string): Observable<void> {
    return this.http
      .postXml(this.collectionPath(collectionType), { pack: packBlueprintId })
      .pipe(map(() => undefined));
  }

  /** openSelectionPack: takes the chosen card from a selection pack. 404 = the pack could not be opened. */
  openSelectionPack(collectionType: string, packBlueprintId: string, selection: string): Observable<void> {
    return this.http
      .postXml(this.collectionPath(collectionType), { pack: packBlueprintId, selection })
      .pipe(map(() => undefined));
  }

  // --- Leagues ---

  /** getLeagues: active leagues; the deck builder uses the RTMD ones the player is a member of. */
  getLeagues(): Observable<League[]> {
    return this.http.getXml('/league').pipe(map(parseLeagues));
  }

  /** The Race to Mount Doom leagues the player is in (what the legacy format dropdown lists). */
  getMemberRtmdLeagues(): Observable<League[]> {
    return this.getLeagues().pipe(map((leagues) => leagues.filter((l) => l.type === 'RTMD' && l.member)));
  }

  /** The collection type goes into the path unencoded, as in the legacy client: the server matches the raw path. */
  private collectionPath(collectionType: string): string {
    return `/collection/${collectionType}`;
  }
}
