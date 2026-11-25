import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, map, Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import { attr, boolAttr, childElements, rootElement } from '../../api/xml';
import { APP_URL } from '../../platform/app-paths';
import { LegacyCookies } from '../../platform/legacy-cookies';
import { tableCallFailure } from '../tables/hall-call';
import { RESULT_READY, RESULT_WORKING } from '../tables/hall-tables-messages';
import { HallDeckChoice, TableResult } from '../tables/hall-table-models';
import { HallTablesApi } from '../tables/hall-tables-api.service';
import { HallTablesService } from '../tables/hall-tables.service';
import {
  leagueJoined,
  leagueJoinFailed,
  leaguesFailed,
  MUST_SELECT_LEAGUE,
  MUST_SELECT_LEAGUE_DECK,
  NEXT_LIMITED_HINT,
  NO_LEAGUE_YET,
} from './play-messages';

/** The league last played in (CreateLeagueTable). */
export const LEAGUE_COOKIE = 'league-table-last-league';
/** The deck is remembered per league ("league-table-decks"). */
export const LEAGUE_DECK_FLOW = 'league-table';

/** A league of GET /league as the League dropdown reads it (FormatManager.updateFormats). */
export interface PlayLeague {
  readonly code: string;
  readonly name: string;
  readonly start: string;
  readonly end: string;
  readonly inviteOnly: boolean;
  readonly member: boolean;
  readonly joinable: boolean;
  readonly draftable: boolean;
}

/** One line of the League dropdown; a player in no league gets one disabled line that says so. */
export interface LeagueOption {
  readonly value: string;
  readonly label: string;
  readonly disabled: boolean;
}

/** What "next steps" offer after a league was joined (CreateLeagueTable.showNextSteps). */
export type LeagueNextStep =
  | { readonly kind: 'create-table'; readonly leagueCode: string }
  | { readonly kind: 'go-to-draft'; readonly url: string }
  | { readonly kind: 'deckbuilder'; readonly url: string };

export interface LeagueNextSteps {
  readonly message: string;
  readonly isError: boolean;
  readonly steps: readonly LeagueNextStep[];
  /** A Limited league's note about its own cards, or null. */
  readonly hint: string | null;
}

/** <leagues><league code name start end inviteOnly member joinable draftable .../></leagues>. */
export function parsePlayLeagues(doc: Document): PlayLeague[] {
  const root = rootElement(doc, 'leagues');
  if (!root) {
    return [];
  }
  return childElements(root, 'league').map((league) => ({
    code: attr(league, 'code') ?? '',
    name: attr(league, 'name') ?? '',
    start: attr(league, 'start') ?? '',
    end: attr(league, 'end') ?? '',
    inviteOnly: boolAttr(league, 'inviteOnly'),
    member: boolAttr(league, 'member'),
    joinable: boolAttr(league, 'joinable'),
    draftable: boolAttr(league, 'draftable'),
  }));
}

/** GET /league/{code}: whether it can be drafted now, and whether a serie is Limited (its own cards). */
export function parseLeagueDetail(doc: Document): { draftable: boolean; limited: boolean } | null {
  const root = rootElement(doc, 'league');
  if (!root) {
    return null;
  }
  const limited = Array.from(root.getElementsByTagName('serie')).some((serie) => boolAttr(serie, 'limited'));
  return { draftable: boolAttr(root, 'draftable'), limited };
}

/** The league calls the Play overlay makes (LeagueRequestHandler). */
@Injectable()
export class PlayLeaguesApi {
  private readonly http = inject(GempHttp);

  leagues(): Observable<PlayLeague[]> {
    return this.http.getXml('/league').pipe(map(parsePlayLeagues));
  }

  league(code: string): Observable<{ draftable: boolean; limited: boolean } | null> {
    return this.http.getXml(`/league/${encodeURIComponent(code)}`).pipe(map(parseLeagueDetail));
  }
}

/**
 * Play ▸ Open League Table (CreateLeagueTable): a ranked table in a league the player has joined. Below the form the
 * Events tab's league list (its own part) lets the player join one; once joined (leagueJoined), the league is
 * selected and the next steps are offered. The league and, per league, the deck are remembered.
 */
@Injectable()
export class LeagueTableFlow {
  private readonly api = inject(PlayLeaguesApi);
  private readonly tablesApi = inject(HallTablesApi);
  private readonly tables = inject(HallTablesService);
  private readonly cookies = inject(LegacyCookies);

  readonly leagues = signal<readonly PlayLeague[]>([]);
  /** "Could not load your leagues: …" (shown with the formats' load error), else null. */
  readonly loadError = signal<string | null>(null);
  /** The league chosen in the dropdown ("" for none). */
  readonly league = signal('');
  readonly result = signal<TableResult>({ kind: 'ready', message: RESULT_READY });
  readonly nextSteps = signal<LeagueNextSteps | null>(null);

  /** The dropdown: the leagues the player is in, else the one disabled line. */
  readonly options = computed<LeagueOption[]>(() => {
    const member = this.leagues().filter((league) => league.member);
    if (member.length === 0) {
      return [{ value: '', label: NO_LEAGUE_YET, disabled: true }];
    }
    return member.map((league) => ({ value: league.code, label: league.name, disabled: false }));
  });

  /** The panel is shown: the remembered league (once the list has it), the next steps cleared, the list fetched. */
  async show(): Promise<void> {
    this.nextSteps.set(null);
    this.result.set({ kind: 'ready', message: RESULT_READY });
    await this.load(this.cookies.read(LEAGUE_COOKIE));
  }

  /** The leagues again, then the wanted league if listed, else the one chosen, else the first (applyWanted). */
  async load(wanted: string | null): Promise<void> {
    try {
      this.leagues.set(await firstValueFrom(this.api.leagues()));
      this.loadError.set(null);
    } catch (error) {
      this.loadError.set(leaguesFailed(tableCallFailure(ApiError.from(error)).message));
    }
    const values = this.options()
      .filter((option) => !option.disabled)
      .map((option) => option.value);
    const current = this.league();
    this.league.set(
      wanted && values.includes(wanted) ? wanted : current && values.includes(current) ? current : (values[0] ?? ''),
    );
  }

  /** The deck for the league chosen (restoreUnlessChosen: the view keeps a deck picked by hand). */
  deckFor(league: string): HallDeckChoice | null {
    return this.tables.initialDeck(LEAGUE_DECK_FLOW, league || null);
  }

  /** The Events tab's list joined a league (from the panel below): select it and say what to do next. */
  async leagueJoined(code: string): Promise<void> {
    await this.load(code);
    const name = this.leagues().find((league) => league.code === code)?.name ?? code;
    const steps: LeagueNextStep[] = [{ kind: 'create-table', leagueCode: code }];
    this.nextSteps.set({ message: leagueJoined(name), isError: false, steps, hint: null });
    let detail: { draftable: boolean; limited: boolean } | null = null;
    try {
      detail = await firstValueFrom(this.api.league(code));
    } catch {
      // the next steps above still apply
    }
    if (detail === null) {
      return;
    }
    if (detail.draftable) {
      steps.push({ kind: 'go-to-draft', url: `/gemp-lotr/soloDraft.html?eventId=${encodeURIComponent(code)}` });
    }
    if (detail.limited) {
      steps.push({ kind: 'deckbuilder', url: `${APP_URL}/deckbuilder/` });
    }
    this.nextSteps.set({
      message: leagueJoined(name),
      isError: false,
      steps: [...steps],
      hint: detail.limited ? NEXT_LIMITED_HINT : null,
    });
  }

  /** A join from the list failed (its onJoinError). */
  leagueJoinFailed(message: string): void {
    this.nextSteps.set({ message: leagueJoinFailed(message), isError: true, steps: [], hint: null });
  }

  /** "Create a table in this league": the league selected again (the view scrolls the form into view). */
  createTableIn(code: string): void {
    this.league.set(code);
  }

  /** Create Table. True when the overlay should close. */
  async submit(deck: HallDeckChoice | null): Promise<boolean> {
    const league = this.league();
    if (league === '') {
      this.result.set({ kind: 'error', message: MUST_SELECT_LEAGUE });
      return false;
    }
    if (deck === null) {
      this.result.set({ kind: 'error', message: MUST_SELECT_LEAGUE_DECK });
      return false;
    }
    this.tables.rememberDeck(LEAGUE_DECK_FLOW, deck, league);
    this.cookies.write(LEAGUE_COOKIE, league);
    this.result.set({ kind: 'pending', message: RESULT_WORKING });
    const result = await firstValueFrom(
      this.tablesApi.createTable({
        format: league,
        deckName: deck.name,
        deckSource: deck.source,
        timer: '',
        desc: '',
        isPrivate: false,
        isInviteOnly: false,
      }),
    );
    this.result.set({ kind: result.ok ? 'success' : 'error', message: result.message });
    return result.ok;
  }
}
