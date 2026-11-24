import { inject, Injectable } from '@angular/core';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';
import { DeckbuilderApi } from '../../api/deckbuilder-api.service';
import { GempHttp } from '../../api/gemp-http.service';
import { GempFormat } from '../../api/models';
import { asTableCall } from './hall-call';
import { parseHallTimers } from './hall-table-parsers';
import { HallDeckOption, HallDeckSource, HallTimer, TableCallResult } from './hall-table-models';

/** POST /deck/formats as the Play popup reads it: the formats and the Casual timers. */
export interface HallTableOptionsReadout {
  readonly formats: Readonly<Record<string, GempFormat>>;
  readonly timers: readonly HallTimer[];
}

/**
 * The table calls of communication.js (createTable, createSoloTable, joinTable, leaveTable) and the lists the Play
 * and Join flows fill their dropdowns from (getFormats, getDecks, getLibraryDecks). The table calls never fail: an
 * HTTP error becomes a refusal with TableFlow.errorMap's sentence, a 400 with the server's own message if it sent one.
 */
@Injectable()
export class HallTablesApi {
  private readonly http = inject(GempHttp);
  private readonly deckbuilder = inject(DeckbuilderApi);

  /** POST /hall: a Casual table. `desc` is the invitee's name on an invite-only table. */
  createTable(form: {
    format: string;
    deckName: string;
    deckSource: HallDeckSource;
    timer: string;
    desc: string;
    isPrivate: boolean;
    isInviteOnly: boolean;
  }): Observable<TableCallResult> {
    return this.call('/hall', { ...form });
  }

  /** POST /hall/solo: a game against the bot; botDeckName "" = a random FotR starter. */
  createSoloTable(form: {
    format: string;
    deckName: string;
    deckSource: HallDeckSource;
    botDeckName: string;
    isPrivate: boolean;
  }): Observable<TableCallResult> {
    return this.call('/hall/solo', { ...form });
  }

  /** POST /hall/{tableId}: sit at a waiting table with a deck. */
  joinTable(tableId: string, deckName: string, deckSource: HallDeckSource): Observable<TableCallResult> {
    return this.call(`/hall/${encodeURIComponent(tableId)}`, { deckName, deckSource });
  }

  /** POST /hall/{tableId}/leave: stand up from a waiting table (the answer is always OK). */
  leaveTable(tableId: string): Observable<TableCallResult> {
    return this.call(`/hall/${encodeURIComponent(tableId)}/leave`, {});
  }

  /** The formats and the timers (POST /deck/formats; HallTimers comes with every answer). */
  tableOptions(): Observable<HallTableOptionsReadout> {
    return this.http.postJson<Record<string, unknown>>('/deck/formats', { includeEvents: false }).pipe(
      map((json) => ({
        formats: (json['Formats'] ?? {}) as Record<string, GempFormat>,
        timers: parseHallTimers(json['HallTimers']),
      })),
    );
  }

  /**
   * The player's decks and the Deck Library's (DeckManager.updateDecks). A list that fails is `null`, so the caller
   * keeps the one it had.
   */
  decks(): Observable<{ player: HallDeckOption[] | null; library: HallDeckOption[] | null }> {
    const list = (decks$: Observable<{ name: string; formatName: string }[]>, source: HallDeckSource) =>
      decks$.pipe(
        map((decks) => decks.map((deck) => ({ name: deck.name, formatName: deck.formatName, source }))),
        catchError(() => of(null)),
      );
    return forkJoin({
      player: list(this.deckbuilder.listDecks(), 'player'),
      library: list(this.deckbuilder.listLibraryDecks(), 'library'),
    });
  }

  private call(path: string, data: Record<string, string | boolean>): Observable<TableCallResult> {
    return asTableCall(this.http.postText(path, data));
  }
}
