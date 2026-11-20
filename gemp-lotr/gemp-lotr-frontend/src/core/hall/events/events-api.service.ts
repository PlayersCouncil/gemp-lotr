import { inject, Injectable } from '@angular/core';
import { map, Observable, shareReplay, tap } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { GempHttp } from '../../api/gemp-http.service';
import {
  CalendarMonth,
  CompletedEventsMonth,
  EventKind,
  LeagueDetail,
  LeagueSummary,
  TournamentDetail,
  TournamentSummary,
} from './events-models';
import {
  parseCalendar,
  parseEventHistory,
  parseEventHistoryMonths,
  parseLeagueDetail,
  parseLeagueList,
  parseTournamentDetail,
  parseTournamentList,
} from './events-parsers';
import { FormatDefinition, parseFormatDefinitions } from './format-definitions';

/**
 * The Events tab's calls (communication.js getCalendar, getEventHistoryMonths, getEventHistory, getLeagues, getLeague,
 * joinLeague, getLiveTournaments, getTournament, getFormatDefinitions). All with the participant, none cached.
 * Failures arrive as ApiError; an answer that cannot be read as ApiError with INVALID_RESPONSE_STATUS. An XML answer
 * with another root element is null (the legacy page drew nothing for it).
 */
@Injectable()
export class EventsApi {
  private readonly http = inject(GempHttp);
  private formats$: Observable<FormatDefinition[]> | null = null;

  /** GET /calendar?from&to: every event touching the days (yyyy-MM-dd, at most 400 days apart). */
  calendar(from: string, to: string): Observable<CalendarMonth> {
    return this.http.getText('/calendar', { from, to }).pipe(map((text) => parseCalendar(readJson(text))));
  }

  /** GET /eventHistory/months?kind: the months with completed events of that kind, newest first. */
  eventHistoryMonths(kind: EventKind): Observable<string[]> {
    return this.http
      .getText('/eventHistory/months', { kind })
      .pipe(map((text) => parseEventHistoryMonths(readJson(text))));
  }

  /** GET /eventHistory?month&kind: that month's completed events of that kind. */
  eventHistory(month: string, kind: EventKind): Observable<CompletedEventsMonth> {
    return this.http
      .getText('/eventHistory', { month, kind })
      .pipe(map((text) => parseEventHistory(readJson(text), month, kind)));
  }

  /** GET /league: the current leagues. */
  leagues(): Observable<LeagueSummary[] | null> {
    return this.http.getText('/league').pipe(map((text) => readXml(text, parseLeagueList)));
  }

  /** GET /league/{code}: one league, running or finished. */
  league(code: string): Observable<LeagueDetail | null> {
    return this.http
      .getText(`/league/${encodeURIComponent(code)}`)
      .pipe(map((text) => readXml(text, parseLeagueDetail)));
  }

  /** POST /league/{code}: join it, paying its cost. 409: the player could not (not enough money); 404: no such league. */
  joinLeague(code: string): Observable<void> {
    return this.http.postText(`/league/${encodeURIComponent(code)}`).pipe(map(() => undefined));
  }

  /** GET /tournament: the live tournaments. */
  liveTournaments(): Observable<TournamentSummary[] | null> {
    return this.http.getText('/tournament').pipe(map((text) => readXml(text, parseTournamentList)));
  }

  /** GET /tournament/{id}: one tournament, running or finished. */
  tournament(id: string): Observable<TournamentDetail | null> {
    return this.http
      .getText(`/tournament/${encodeURIComponent(id)}`)
      .pipe(map((text) => readXml(text, parseTournamentDetail)));
  }

  /**
   * A format's definition by code, for a league serie's "Format description" (the legacy page asked
   * GET /hall/format/{code}, which answers HTML). GET /hall/formats/json is asked once per page and shared; null when
   * the format is not among them (it lists the hall's formats only). A failure is not kept, so the next asks again.
   */
  formatDefinition(code: string): Observable<FormatDefinition | null> {
    if (this.formats$ === null) {
      const request$: Observable<FormatDefinition[]> = this.http.getText('/hall/formats/json').pipe(
        map((text) => parseFormatDefinitions(readJson(text))),
        tap({
          error: () => {
            if (this.formats$ === request$) {
              this.formats$ = null;
            }
          },
        }),
        shareReplay(1),
      );
      this.formats$ = request$;
    }
    return this.formats$.pipe(map((formats) => formats.find((format) => format.code === code) ?? null));
  }
}

function readJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch (cause) {
    throw new ApiError(INVALID_RESPONSE_STATUS, 'The answer is not JSON', { cause });
  }
}

function readXml<T>(text: string, parse: (xml: string) => T): T {
  try {
    return parse(text);
  } catch (cause) {
    throw new ApiError(INVALID_RESPONSE_STATUS, 'The answer is not XML', { cause });
  }
}
