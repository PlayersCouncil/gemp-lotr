import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { FormData } from '../../api/form-codec';
import { GempHttp } from '../../api/gemp-http.service';
import { parseXml } from '../../api/xml';
import {
  GameHistoryFilterOptions,
  GameHistoryFilters,
  GameHistoryPage,
  MyScenariosAnswer,
  PlayerStats,
  PublicScenario,
  ScenarioCopy,
} from './account-models';
import {
  parseGameHistory,
  parseGameHistoryFilters,
  parseMyScenarios,
  parsePlayerStats,
  parsePublicScenarios,
  parseScenarioCopy,
} from './account-parsers';

/**
 * The My Account tab's own calls (communication.js getGameHistory, getGameHistoryFilters, getPlayerStats,
 * migrateTrophies, scenarioShelf). The ignore list's calls are the chat's (ChatApi), the authoring tool's are
 * AuthoringApi's and starting a scenario is ScenarioApi's. Failures arrive as ApiError; an answer that cannot be read
 * as ApiError with INVALID_RESPONSE_STATUS. An XML answer with another root element is null (the legacy page drew
 * nothing for it).
 */
@Injectable()
export class AccountApi {
  private readonly http = inject(GempHttp);

  /**
   * GET /gameHistory?start&count[&format][&opponent][&event][&from][&to]: a page of the player's games, newest
   * first; only the filters given are sent. 400 for filters the server can't use (its words in the `message` header).
   */
  gameHistory(start: number, count: number, filters: GameHistoryFilters): Observable<GameHistoryPage | null> {
    const data: FormData = { start: String(start), count: String(count) };
    for (const key of ['format', 'opponent', 'event', 'from', 'to'] as const) {
      if (filters[key] !== '') {
        data[key] = filters[key];
      }
    }
    return this.http.getText('/gameHistory', data).pipe(map((text) => readXml(text, parseGameHistory)));
  }

  /** GET /gameHistory/filters: the formats and events the filters suggest. */
  gameHistoryFilters(): Observable<GameHistoryFilterOptions> {
    return this.http.getText('/gameHistory/filters').pipe(map((text) => parseGameHistoryFilters(readJson(text))));
  }

  /** GET /playerStats: wins and losses by format and deck, casual and competitive. */
  playerStats(): Observable<PlayerStats | null> {
    return this.http.getText('/playerStats').pipe(map((text) => readXml(text, parsePlayerStats)));
  }

  /** POST /collection/migrate: the Trophies collection into My Cards. 410 when there is nothing to migrate. */
  migrateTrophies(): Observable<void> {
    return this.http.postText('/collection/migrate').pipe(map(() => undefined));
  }

  /** GET /scenarios/mine: the player's own scenarios, how many they may keep, and the draft they have open. */
  myScenarios(): Observable<MyScenariosAnswer> {
    return this.http.getText('/scenarios/mine').pipe(map((text) => parseMyScenarios(readJson(text))));
  }

  /** GET /scenarios/public: the public scenarios the player may copy. */
  publicScenarios(): Observable<PublicScenario[]> {
    return this.http.getText('/scenarios/public').pipe(map((text) => parsePublicScenarios(readJson(text))));
  }

  /** POST /scenarios/mine/copy: a copy of one of the player's own ("Copy of …"). */
  copy(path: string): Observable<ScenarioCopy> {
    return this.post('mine/copy', { path }).pipe(map(parseScenarioCopy));
  }

  /** POST /scenarios/mine/copyPublic: a copy of a public scenario, linked to it. */
  copyPublic(id: string): Observable<ScenarioCopy> {
    return this.post('mine/copyPublic', { id }).pipe(map(parseScenarioCopy));
  }

  /** POST /scenarios/mine/delete. */
  delete(path: string): Observable<void> {
    return this.post('mine/delete', { path }).pipe(map(() => undefined));
  }

  /** POST /scenarios/mine/submit: up for review; it is locked until an admin approves or rejects it. */
  submit(path: string): Observable<void> {
    return this.post('mine/submit', { path }).pipe(map(() => undefined));
  }

  /** Something that cannot be done is a 400 whose `{error}` is the server's reason (ApiError.said). */
  private post(what: string, data: FormData): Observable<unknown> {
    return this.http.postText(`/scenarios/${what}`, data).pipe(map(readJson));
  }
}

function readJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch (cause) {
    throw new ApiError(INVALID_RESPONSE_STATUS, 'The answer is not JSON', { cause });
  }
}

function readXml<T>(text: string, parse: (doc: Document) => T): T {
  let doc: Document;
  try {
    doc = parseXml(text);
  } catch (cause) {
    throw new ApiError(INVALID_RESPONSE_STATUS, 'The answer is not XML', { cause });
  }
  return parse(doc);
}
