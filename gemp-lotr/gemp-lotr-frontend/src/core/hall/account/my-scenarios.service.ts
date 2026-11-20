import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { AuthoringApi } from '../../game/authoring/authoring-api.service';
import { ScenarioApi } from '../../game/scenario/scenario-api.service';
import { gamePageUrl } from '../../platform/app-paths';
import { PageNavigation, ReservedTab } from '../../platform/page-navigation';
import { SessionService } from '../../session/session.service';
import { AccountApi } from './account-api.service';
import {
  CANCEL,
  CLOSE_DRAFT_TEXT,
  CLOSE_DRAFT_TITLE,
  CLOSE_DRAFT_YES,
  COPIED,
  copiedAs,
  DELETE_YES,
  DELETED,
  deleteText,
  deleteTitle,
  DIRTY_DRAFT_TITLE,
  DIRTY_DRAFT_YES,
  dirtyDraftText,
  NEW_NAME_MISSING,
  NOT_DONE,
  NOT_LOADED,
  NOT_OPENED,
  NOT_RESUMED,
  NOT_STARTED_BECAUSE,
  OPENED_IN_NEW_TAB,
  OPENING_AUTHORING,
  PUBLIC_NOT_LOADED,
  SETTING_UP,
  STARTED_IN_NEW_TAB,
  SUBMITTED,
} from './account-messages';
import { AuthoringDraft, MyScenariosAnswer, OwnScenario, PublicScenario } from './account-models';
import {
  groupShelf,
  OwnScenarioRow,
  ownScenarioRow,
  PublicScenarioRow,
  publicScenarioRow,
  scenarioSlug,
  shelfError,
  ShelfKindGroup,
} from './scenario-shelf';
import { firstText } from './first-text';

/** The status line's tone (its `data-kind`): nothing, under way, done, failed. */
export type ShelfSayKind = '' | 'pending' | 'success' | 'error';

export interface ShelfSaying {
  readonly text: string;
  readonly kind: ShelfSayKind;
}

/**
 * A question in the shelf's own box (ScenarioConfirm.ask; `submit`: ScenarioSubmit.ask, with what to check first
 * and what submitting does: the authoring tool's dialog, `gd-game-author-submit`, says the same). `lines` are
 * paragraphs. The answer is given with `answer`, from the click, so a tab can be opened on it.
 */
export type ShelfQuestion =
  | {
      readonly kind: 'confirm';
      readonly title: string;
      readonly lines: readonly string[];
      readonly yes: string;
      readonly no: string;
      /** The yes button is drawn as something that cannot be taken back. */
      readonly danger: boolean;
    }
  | { readonly kind: 'submit'; readonly title: string | null };

/** The Copy Public dialog: loading, the public scenarios grouped, or why they could not be loaded. */
export type CopyPublicDialog =
  | { readonly state: 'loading' }
  | { readonly state: 'loaded'; readonly groups: readonly ShelfKindGroup<PublicScenarioRow>[] }
  | { readonly state: 'failed'; readonly message: string };

/** A game the browser gave no tab for: the hall's "Your game is ready" prompt, with its link. */
export interface BlockedGame {
  readonly gameId: string;
  readonly url: string;
}

/**
 * My Account > My Scenarios (scenarioShelf.js MyScenariosUI, ScenarioShelfCommon): the player's own scenarios by kind
 * and campaign, the draft they have open in the authoring tool, and what can be done with each: open it in the
 * authoring tool (the Angular game page of the draft's game, which docks the authoring panel), play it, copy it,
 * submit it for review, delete it; a new one; a copy of a public one.
 *
 * Opening a game (play, edit, continue writing) reserves a tab at once, on the click, and points it at the game once
 * the server has answered (PageNavigation.reserveTab). So every call that opens one must come straight from a click
 * (or from `answer`, itself a click). One shelf change at a time (`busy`); after each, the list is fetched again.
 */
@Injectable()
export class MyScenarios {
  private readonly api = inject(AccountApi);
  private readonly authoringApi = inject(AuthoringApi);
  private readonly scenarioApi = inject(ScenarioApi);
  private readonly navigation = inject(PageNavigation);
  private readonly participantId = inject(SessionService).participantId;

  private readonly answer_ = signal<MyScenariosAnswer | null>(null);
  private readonly saying_ = signal<ShelfSaying>({ text: '', kind: '' });
  private readonly question_ = signal<ShelfQuestion | null>(null);
  private readonly copyPublic_ = signal<CopyPublicDialog | null>(null);
  private readonly blocked_ = signal<BlockedGame | null>(null);
  private readonly picked_ = signal<string | null>(null);
  private pendingAnswer: { yes: () => void; no: () => void } | null = null;
  /** ScenarioShelfCommon.highlight: the copy to pick out once the list shows it. */
  private highlight: string | null = null;
  private busy = false;
  private started = false;

  /** The status line (a live region in the view). */
  readonly saying = this.saying_.asReadonly();
  /** The question being asked, if any. */
  readonly question = this.question_.asReadonly();
  /** The Copy Public dialog, while it is open. */
  readonly copyPublicDialog = this.copyPublic_.asReadonly();
  /** A game the browser would not open a tab for. */
  readonly blockedGame = this.blocked_.asReadonly();
  /** The list has come at least once. */
  readonly loaded = computed(() => this.answer_() !== null);
  /** The draft open in the authoring tool, if any ("Continue writing", "Close it"). */
  readonly draft = computed<AuthoringDraft | null>(() => this.answer_()?.authoring ?? null);
  /** The server has a folder for players' scenarios; without it the list says so instead. */
  readonly ownFolder = computed(() => this.answer_()?.ownFolder ?? true);
  /** How many scenarios a player may keep. */
  readonly limit = computed(() => this.answer_()?.limit ?? 0);
  /** The rows, by kind and campaign. */
  readonly groups = computed<ShelfKindGroup<OwnScenarioRow>[]>(() => {
    const picked = this.picked_();
    const rows = (this.answer_()?.scenarios ?? []).map((scenario) =>
      ownScenarioRow(scenario, scenario.path === picked),
    );
    return groupShelf(rows, (row) => row.scenario, true);
  });
  /** No scenarios yet (and a folder to keep them in). */
  readonly empty = computed(() => this.loaded() && this.ownFolder() && (this.answer_()?.scenarios.length ?? 0) === 0);

  /** The sub-tab's first showing. */
  start(): void {
    if (!this.started) {
      this.started = true;
      void this.refresh();
    }
  }

  /** MyScenariosUI.refresh: GET /scenarios/mine; also when the viewer comes back after a while. */
  async refresh(): Promise<void> {
    let answer: MyScenariosAnswer;
    try {
      answer = await firstValueFrom(this.api.myScenarios());
    } catch (error) {
      this.say(shelfError(error, NOT_LOADED), 'error');
      return;
    }
    this.answer_.set(answer);
    const highlight = this.highlight;
    if (highlight !== null && answer.ownFolder && answer.scenarios.some((scenario) => scenario.path === highlight)) {
      this.picked_.set(highlight);
      this.highlight = null;
    } else {
      this.picked_.set(null);
    }
  }

  /**
   * "Create" (or Enter in either field): the id typed, else one made from the title. True when it was opened, and
   * the fields are to be emptied.
   */
  create(title: string, id: string): Promise<boolean> {
    const name = id.trim() !== '' ? id.trim() : scenarioSlug(title);
    if (name === '') {
      this.say(NEW_NAME_MISSING, 'error');
      return Promise.resolve(false);
    }
    return this.edit(name, { title: title.trim(), canonical: false });
  }

  /** A row's "Edit": open it in the authoring tool. */
  editScenario(scenario: OwnScenario): Promise<boolean> {
    return scenario.fullId === null ? Promise.resolve(false) : this.edit(scenario.fullId);
  }

  /** A row's "Play": a game of it as a player would play it (ScenarioShelfCommon.play). */
  async play(scenario: OwnScenario): Promise<void> {
    if (scenario.fullId === null) {
      return;
    }
    const tab = this.navigation.reserveTab();
    this.say(SETTING_UP, 'pending');
    try {
      const started = await firstValueFrom(this.scenarioApi.start(scenario.fullId));
      this.say(STARTED_IN_NEW_TAB, 'success');
      this.openGame(tab, started.gameId);
    } catch (error) {
      tab?.close();
      this.say(shelfError(error, NOT_STARTED_BECAUSE), 'error');
    }
  }

  /** A row's "Copy": "Copy of …", picked out in the list. */
  copy(scenario: OwnScenario): Promise<void> {
    return this.post(this.api.copy(scenario.path), (copy) => {
      this.highlight = copy.path;
      return COPIED;
    });
  }

  /** A row's "Submit...": asked first (ScenarioSubmit.ask). */
  submit(scenario: OwnScenario): void {
    this.ask({ kind: 'submit', title: scenario.title }, () => {
      void this.post(this.api.submit(scenario.path), () => SUBMITTED);
    });
  }

  /** A row's "Delete": asked first (ScenarioShelfCommon.askDelete). */
  delete(scenario: OwnScenario): void {
    const name = firstText(scenario.title, scenario.path);
    this.ask(
      {
        kind: 'confirm',
        title: deleteTitle(name),
        lines: [deleteText(name)],
        yes: DELETE_YES,
        no: CANCEL,
        danger: true,
      },
      () => void this.post(this.api.delete(scenario.path), () => DELETED),
    );
  }

  /** "Continue writing": back to the draft's game, in a new tab. */
  async continueDraft(): Promise<void> {
    const tab = this.navigation.reserveTab();
    try {
      const result = await firstValueFrom(this.authoringApi.resume());
      this.openGame(tab, String(result.gameId));
    } catch (error) {
      tab?.close();
      this.say(shelfError(error, NOT_RESUMED), 'error');
      void this.refresh();
    }
  }

  /** "Close it": the draft given up; asked first unless it is known to have no unsaved changes. */
  closeDraft(): void {
    const close = () => {
      void firstValueFrom(this.authoringApi.close())
        .catch(() => undefined)
        .then(() => this.refresh());
    };
    if (this.draft()?.dirty === false) {
      close();
      return;
    }
    this.ask(
      {
        kind: 'confirm',
        title: CLOSE_DRAFT_TITLE,
        lines: [CLOSE_DRAFT_TEXT],
        yes: CLOSE_DRAFT_YES,
        no: CANCEL,
        danger: true,
      },
      close,
    );
  }

  /** "Copy Public...": the dialog opens, and the public scenarios load into it. */
  async openCopyPublic(): Promise<void> {
    this.copyPublic_.set({ state: 'loading' });
    let scenarios: PublicScenario[];
    try {
      scenarios = await firstValueFrom(this.api.publicScenarios());
    } catch (error) {
      if (this.copyPublic_() !== null) {
        this.copyPublic_.set({ state: 'failed', message: shelfError(error, PUBLIC_NOT_LOADED) });
      }
      return;
    }
    if (this.copyPublic_() !== null) {
      this.copyPublic_.set({ state: 'loaded', groups: groupRows(scenarios) });
    }
  }

  closeCopyPublic(): void {
    this.copyPublic_.set(null);
  }

  /** A public scenario's "Copy": the dialog closes, the copy is made and picked out in the list. */
  copyPublic(scenario: PublicScenario): Promise<void> {
    this.copyPublic_.set(null);
    if (scenario.fullId === null) {
      return Promise.resolve();
    }
    return this.post(this.api.copyPublic(scenario.fullId), (copy) => {
      this.highlight = copy.path;
      return copiedAs(copy.path);
    });
  }

  /** The answer to the question shown; yes goes on, from the click. */
  answer(yes: boolean): void {
    const pending = this.pendingAnswer;
    this.pendingAnswer = null;
    this.question_.set(null);
    if (pending) {
      (yes ? pending.yes : pending.no)();
    }
  }

  /** The "Your game is ready" prompt's link was followed, or it was closed. */
  dismissBlockedGame(): void {
    this.blocked_.set(null);
  }

  // ---- ScenarioShelfCommon ----

  /**
   * ScenarioShelfCommon.edit: opens a scenario in the authoring tool, in a new tab (`created`: a new one with that id
   * and title). A draft open with unsaved changes is given up, once the player says so (droppingDraft).
   */
  private edit(id: string, created?: { title: string; canonical: boolean }): Promise<boolean> {
    const draft = this.draft();
    if (!draft?.dirty) {
      return this.openForEdit(id, created);
    }
    return new Promise((resolve) => {
      this.ask(
        {
          kind: 'confirm',
          title: DIRTY_DRAFT_TITLE,
          lines: [dirtyDraftText(firstText(draft.title, draft.id))],
          yes: DIRTY_DRAFT_YES,
          no: CANCEL,
          danger: true,
        },
        () => void this.openForEdit(id, created).then(resolve),
        () => resolve(false),
      );
    });
  }

  /** openForEdit: POST /scenarios/author/open; the tab goes to the draft's game. */
  private async openForEdit(id: string, created?: { title: string; canonical: boolean }): Promise<boolean> {
    const tab = this.navigation.reserveTab();
    this.say(OPENING_AUTHORING, 'pending');
    try {
      const result = await firstValueFrom(this.authoringApi.open(id, created));
      this.say(OPENED_IN_NEW_TAB, 'success');
      this.openGame(tab, String(result.gameId));
    } catch (error) {
      tab?.close();
      this.say(shelfError(error, NOT_OPENED), 'error');
      return false;
    }
    void this.refresh();
    return true;
  }

  /** openGame: the reserved tab goes to the game; without one, the "Your game is ready" prompt offers it. */
  private openGame(tab: ReservedTab | null, gameId: string): void {
    const url = gamePageUrl(gameId, this.participantId);
    if (!tab?.go(url)) {
      this.blocked_.set({ gameId, url });
    }
  }

  /** MyScenariosUI.post: one change at a time; said, and the list fetched again, either way. */
  private async post<T>(call$: Observable<T>, done: (answer: T) => string): Promise<void> {
    if (this.busy) {
      return;
    }
    this.busy = true;
    try {
      const answer = await firstValueFrom(call$);
      this.say(done(answer), 'success');
    } catch (error) {
      this.say(shelfError(error, NOT_DONE), 'error');
    } finally {
      this.busy = false;
    }
    await this.refresh();
  }

  /** Shows a question; `onYes` (and `onNo`) run from the answer's click. */
  private ask(question: ShelfQuestion, onYes: () => void, onNo: () => void = () => undefined): void {
    this.pendingAnswer?.no();
    this.pendingAnswer = { yes: onYes, no: onNo };
    this.question_.set(question);
  }

  private say(text: string, kind: ShelfSayKind): void {
    this.saying_.set({ text, kind });
  }
}

function groupRows(scenarios: readonly PublicScenario[]): ShelfKindGroup<PublicScenarioRow>[] {
  return groupShelf(scenarios.map(publicScenarioRow), (row) => row.scenario, false);
}
