import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { ApiError } from '../../../api/api-error';
import { GempHttp } from '../../../api/gemp-http.service';
import { Notifier } from '../../../notifier/notifier';
import { gamePageUrl } from '../../../platform/app-paths';
import { LegacyCookies } from '../../../platform/legacy-cookies';
import { PageNavigation } from '../../../platform/page-navigation';
import { SessionService } from '../../../session/session.service';
import { gameKey, HallReadyOffers } from '../../ready/hall-ready-offers.service';
import { errorDetail, tableCallFailure } from '../../tables/hall-call';
import { RESULT_READY } from '../../tables/hall-tables-messages';
import { TableResult } from '../../tables/hall-table-models';
import {
  byAuthor,
  DIRTY_DRAFT_QUESTION,
  NOT_OFFICIAL,
  NOT_VERIFIED,
  playedTimes,
  PLAY,
  PLAY_AGAIN,
  scenarioOpenFailed,
  SCENARIO_NOT_STARTED,
  SCENARIO_OPENED,
  SCENARIO_STARTED,
  scenarioStartFailed,
  SETTING_UP,
  START_OVER,
  UNREACHABLE,
  unreadableSummary,
} from '../play-messages';
import {
  buildScenarioTree,
  findFolder,
  folderGroups,
  folderLines,
  nextIds,
  parseScenarioListing,
  ScenarioEntry,
  ScenarioFolder,
  ScenarioFolderLine,
  ScenarioGroup,
  ScenarioListing,
  statusOf,
} from './scenario-list';
import { asText } from '../../json-values';

/** The folder last looked at (PlayScenario). */
export const SCENARIO_FOLDER_COOKIE = 'scenario-folder';
/** Where scenarios are written (My Account > My Scenarios). */
export const MY_SCENARIOS_ROUTE = 'account/scenarios';

/** The scenario list's calls (ScenarioRequestHandler). */
@Injectable()
export class ScenarioListApi {
  private readonly http = inject(GempHttp);

  list(): Observable<unknown> {
    return this.http.getJson<unknown>('/scenarios');
  }

  start(id: string): Observable<{ gameId?: unknown; error?: unknown } | null> {
    return this.http.postJson('/scenarios/start', { id });
  }

  /** POST /scenarios/author/open: the scenario in the authoring tool (a game with the authoring panel beside it). */
  openForAuthoring(id: string): Observable<{ gameId?: unknown } | null> {
    return this.http.postJson('/scenarios/author/open', { id });
  }
}

/** A scenario's row: its tag, its notes and its button. */
export interface ScenarioRow {
  readonly scenario: ScenarioEntry;
  readonly status: ReturnType<typeof statusOf>;
  /** "By x" / "Not an official scenario", "Not verified: …", "Played N times", joined with " · " by the view. */
  readonly notes: readonly string[];
  /** "Play", "Play Again" or "Start Over" (the one in progress: starting gives it up). */
  readonly button: string;
}

/**
 * Play ▸ Scenarios & Tutorials (PlayScenario): the scenarios in their folders, the player's progress on each, the game
 * in progress to carry on with, the authors' link to My Scenarios and the files with mistakes. Starting one opens its
 * game in a tab reserved on the click (a tab opened from the answer would be blocked), or offers it.
 */
@Injectable()
export class PlayScenario {
  private readonly api = inject(ScenarioListApi);
  private readonly cookies = inject(LegacyCookies);
  private readonly navigation = inject(PageNavigation);
  private readonly offers = inject(HallReadyOffers);
  private readonly notifier = inject(Notifier);
  private readonly participantId = inject(SessionService).participantId;

  readonly listing = signal<ScenarioListing | null>(null);
  /** The list could not be loaded: "The list of scenarios could not be loaded." stands in for it. */
  readonly listFailed = signal(false);
  readonly result = signal<TableResult>({ kind: 'ready', message: RESULT_READY });
  /** From a click on a start button until the server has answered (the buttons are disabled). */
  readonly starting = signal(false);
  private readonly wantedFolder = signal<string | null>(this.cookies.read(SCENARIO_FOLDER_COOKIE));
  /** What the result box was last made to say about the unreadable files. */
  private unreadableSaid: string | null = null;

  private readonly tree = computed(() => buildScenarioTree(this.listing()?.scenarios ?? []));

  /** The folder shown: the one chosen (or last time's) if it is still there, else the first. */
  readonly selectedFolder = computed<ScenarioFolder | null>(() => {
    const tree = this.tree();
    const wanted = this.wantedFolder();
    const found = wanted === null ? null : findFolder(tree, wanted);
    return found === null || found === tree ? (tree.children[0] ?? null) : found;
  });
  readonly folders = computed<ScenarioFolderLine[]>(() => folderLines(this.tree()));
  readonly groups = computed<ScenarioGroup[]>(() => {
    const selected = this.selectedFolder();
    return selected === null ? [] : folderGroups(selected);
  });

  /** The game in progress to Continue, unless it is the author's draft (which has its own way back). */
  readonly current = computed(() => {
    const listing = this.listing();
    const current = listing?.current ?? null;
    if (current === null || (listing?.authoring !== null && listing?.authoring?.gameId === current.gameId)) {
      return null;
    }
    const scenario = listing?.scenarios.find((s) => s.id === current.id);
    return { gameId: current.gameId, title: scenario?.title ?? current.id };
  });

  private readonly next = computed(() => {
    const listing = this.listing();
    return listing === null ? new Set<string>() : nextIds(listing.scenarios, listing.current);
  });

  row(scenario: ScenarioEntry): ScenarioRow {
    const listing = this.listing();
    const notes: string[] = [];
    if (!scenario.canonical) {
      notes.push(scenario.author ? byAuthor(scenario.author) : NOT_OFFICIAL);
    }
    if (!scenario.verified) {
      notes.push(NOT_VERIFIED);
    }
    if (scenario.progress !== null && scenario.progress.attempts > 1) {
      notes.push(playedTimes(scenario.progress.attempts));
    }
    const inProgress = listing?.current?.id === scenario.id;
    return {
      scenario,
      status: statusOf(scenario, listing?.current ?? null, this.next()),
      notes,
      button: inProgress ? START_OVER : scenario.progress !== null ? PLAY_AGAIN : PLAY,
    };
  }

  /** A folder in the column was chosen (remembered for a year). */
  selectFolder(key: string): void {
    this.wantedFolder.set(key);
    this.cookies.write(SCENARIO_FOLDER_COOKIE, key);
  }

  resetResult(): void {
    this.result.set({ kind: 'ready', message: RESULT_READY });
    this.unreadableSaid = null;
  }

  /** The form is shown: the list fetched again. */
  async refresh(): Promise<void> {
    try {
      this.listing.set(parseScenarioListing(await firstValueFrom(this.api.list())));
      this.listFailed.set(false);
      this.sayUnreadable();
    } catch (error) {
      this.listFailed.set(true);
      this.result.set({ kind: 'error', message: tableCallFailure(ApiError.from(error)).message });
    }
  }

  /** Play / Play Again / Start Over: a tab reserved now, pointed at the game once it exists. True when it started. */
  async start(scenario: ScenarioEntry): Promise<boolean> {
    if (this.starting()) {
      return false;
    }
    this.starting.set(true);
    this.result.set({ kind: 'pending', message: SETTING_UP });
    const tab = this.navigation.reserveTab();
    try {
      const answer = await firstValueFrom(this.api.start(scenario.id));
      this.starting.set(false);
      const gameId = answer?.gameId;
      if (gameId === null || gameId === undefined) {
        tab?.close();
        this.result.set({
          kind: 'error',
          message: typeof answer?.error === 'string' ? answer.error : SCENARIO_NOT_STARTED,
        });
        return false;
      }
      this.result.set({ kind: 'success', message: SCENARIO_STARTED });
      this.open(tab, asText(gameId));
      return true;
    } catch (error) {
      this.starting.set(false);
      tab?.close();
      const failure = ApiError.from(error);
      const status = Math.max(0, failure.status);
      this.result.set({
        kind: 'error',
        message: errorDetail(failure) ?? (status === 0 ? UNREACHABLE : scenarioStartFailed(status)),
      });
      // the list may be what was out of date
      void this.refresh();
      return false;
    }
  }

  /** Continue: the game in progress. */
  continueCurrent(): void {
    const current = this.current();
    if (current !== null) {
      this.open(this.navigation.reserveTab(), current.gameId);
    }
  }

  /**
   * Edit (a file with mistakes the authoring tool can open): after the question when a draft with unsaved changes
   * would be dropped. True when it opened.
   */
  async edit(id: string): Promise<boolean> {
    if (this.starting()) {
      return false;
    }
    const draft = this.listing()?.authoring ?? null;
    if (draft !== null && draft.dirty !== false && !(await this.notifier.confirm(DIRTY_DRAFT_QUESTION))) {
      return false;
    }
    this.starting.set(true);
    this.result.set({ kind: 'pending', message: SETTING_UP });
    const tab = this.navigation.reserveTab();
    try {
      const answer = await firstValueFrom(this.api.openForAuthoring(id));
      this.starting.set(false);
      this.result.set({ kind: 'success', message: SCENARIO_OPENED });
      this.open(tab, asText(answer?.gameId));
      return true;
    } catch (error) {
      this.starting.set(false);
      tab?.close();
      const failure = ApiError.from(error);
      this.result.set({
        kind: 'error',
        message: errorDetail(failure) ?? scenarioOpenFailed(Math.max(0, failure.status)),
      });
      return false;
    }
  }

  private open(tab: ReturnType<PageNavigation['reserveTab']>, gameId: string): void {
    this.offers.openReserved(tab, gameKey(gameId), 'game', gamePageUrl(gameId, this.participantId));
  }

  /** The unreadable files in the result box, in a line, only while it has nothing else to say (sayUnreadable). */
  private sayUnreadable(): void {
    const result = this.result();
    const quiet = result.kind === 'ready' || (this.unreadableSaid !== null && result.message === this.unreadableSaid);
    if (!quiet) {
      this.unreadableSaid = null;
      return;
    }
    const unreadable = this.listing()?.unreadable ?? [];
    if (unreadable.length === 0) {
      if (this.unreadableSaid !== null) {
        this.result.set({ kind: 'ready', message: RESULT_READY });
      }
      this.unreadableSaid = null;
      return;
    }
    const text = unreadableSummary(unreadable);
    this.result.set({ kind: 'error', message: text });
    this.unreadableSaid = text;
  }
}
