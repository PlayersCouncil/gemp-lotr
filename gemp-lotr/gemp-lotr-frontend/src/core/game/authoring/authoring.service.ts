import { computed, DestroyRef, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { Observable } from 'rxjs';
import { Notifier } from '../../notifier/notifier';
import { gamePageUrl } from '../../platform/app-paths';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageExit } from '../../platform/page-exit';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { DecisionGate, DecisionGates } from '../decision/decision-gate';
import { ScenarioStep } from '../scenario/scenario-events';
import { ScenarioGuide, TriedPlace } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { PendingDecision } from '../state/game-model';
import { AuthoringApi, AuthoringSettings, DraftPatch, SolutionOp } from './authoring-api.service';
import { AuthoringResult, AuthoringState, AuthoringTab, DraftState, DraftStep, hasDraft } from './authoring-state';
import { AuthoringUi, AuthoringUiStore } from './authoring-ui-store';
import { problemOf, SavedNotice, savedNotice } from './notices';
import { settle } from './selection';
import { blockGoto, insertOp, rewriteOps, StepOp, stepsGoto } from './step-ops';
import { isThisGame, settingsOf, stepsOf } from './steps';
import { lastCheckpoint } from './timeline';
import { checkReport, offScriptReport } from './scenario-settings';

/** The panel docked at the window's right (px), and folded away to a strip. */
export const PANEL_WIDTH = 430;
export const FOLDED_WIDTH = 30;
/** The opponent's decision about to be answered with no game event to say so: looked at again this often (ms)... */
export const SETTLE_MS = 700;
/** ...at most this many times in a row with nothing changed. */
export const SETTLE_TRIES = 10;
/** The game sent something: the state is looked at once this has passed with nothing more (ms). */
export const MOVED_MS = 250;
/** The author answered on the table: the state is looked at after these (ms from the answer). */
export const ANSWERED_MS = [500, 1700] as const;

/** What the Step tab's send of its form came to: everything went (or there was nothing), or the server refused it. */
export type StepFlushResult = { went: true } | { went: false; note: string };

/**
 * What the Step tab tells the panel (it attaches them, `attachStepTab`): without it, there is never anything unsent
 * and nobody is in the middle of anything.
 */
export interface StepTabHooks {
  /**
   * Sends what the form has not sent yet, and says how that went: `note` is what to say of a refused step with the
   * save's result ("Step 3's last change is not in the file: ..."). `regardless`: for a save, which goes ahead
   * either way (navigation does not go ahead after a refusal).
   */
  flush(regardless: boolean): Promise<StepFlushResult>;
  /** Whether the author is in the middle of something there that turning to another step would lose (typing). */
  engaged(): boolean;
  /** The form is to be made afresh from the next state (a change to the steps has been sent). */
  reset(): void;
  /** The page is going to a fresh game: what the form has not sent is kept for the next page's form. */
  carry?(): void;
}

/** A message of a step, as the Step tab and the guide know it: the step (from 1), which of a block's (from 1; 0 none), which message (from 0). */
export interface MessageAt {
  step: number;
  sub: number;
  index: number;
}

const NO_STEP_TAB: StepTabHooks = {
  flush: () => Promise.resolve({ went: true }),
  engaged: () => false,
  reset: () => undefined,
};

/** What the panel shows: nothing yet (no state), the draft gone or moved on, a test game's way back, or the tabs. */
export type AuthoringView = 'none' | 'elsewhere' | 'test' | 'author';

/** The question asked on the way to the Game Hall with unsaved changes: whether an answer is on its way, what failed. */
export interface LeaveQuestion {
  busy: boolean;
  said: string | null;
}

/** The panel's dock: how much of the window's right it takes, folded or not, or floating (a test game). */
export interface AuthoringDock {
  width: number;
  folded: boolean;
  floating: boolean;
}

const isResult = (value: unknown): value is AuthoringResult =>
  value !== null && typeof value === 'object' && (value as { gameId?: unknown }).gameId != null;

/**
 * The scenario authoring panel's brain, on the page of a draft's game (the scenario info says `author`, or
 * `draftTest`; never in a replay): the server's state of the draft and of its game, kept fresh as the game moves; the
 * calls the panel makes and what each answer means (a fresh game to go to, events to ask for, the state); what is
 * selected; what the notices say; docking; leaving. The tabs (ui/game/authoring) only show it and call it. It has the
 * scenario guide behave as a draft's game needs (ScenarioAuthorHooks). Page-scoped; it does nothing in any other game.
 */
@Injectable()
export class Authoring {
  private readonly api = inject(AuthoringApi);
  private readonly session = inject(GameSession);
  private readonly guide = inject(ScenarioGuide);
  private readonly gates = inject(DecisionGates);
  private readonly store = inject(AuthoringUiStore);
  private readonly lifetime = inject(PageLifetime);
  private readonly notifier = inject(Notifier);
  private readonly cues = inject(PageCues);
  private readonly hall = inject(HallTab);
  private readonly exit = inject(PageExit);

  /** Whether this game is a draft's: the panel is on the page. */
  readonly active = computed(() => {
    const info = this.guide.info();
    return info !== null && (info.author || info.draftTest) && !this.session.replaying();
  });
  /** Writing the draft (not testing it). */
  readonly writing = computed(() => this.active() && this.guide.info()?.author === true);

  /** The server's state, as last seen (unchanged states are not taken again: nothing is redrawn for them). */
  readonly state = signal<AuthoringState | null>(null);
  /** The state with a draft open, or null. */
  readonly draft = computed(() => {
    const state = this.state();
    return hasDraft(state) ? state : null;
  });
  /** What went wrong with the last call, until one goes right. */
  readonly problem = signal<string | null>(null);
  /** How many calls are on their way. */
  readonly busy = signal(0);
  /** The server's notice that has been put away. */
  readonly noticeRead = signal<string | null>(null);
  /** How many of the script's problems have been put away. */
  readonly problemsRead = signal(0);
  /** How the last save went, until put away (or the draft changes again). */
  readonly saved = signal<SavedNotice | null>(null);
  /** The whole report of the last save (or check: the Scenario tab's). */
  readonly report = signal<string | null>(null);
  /** An instruction for what the author is doing now (picking something on the table). */
  readonly told = signal<string | null>(null);
  /** The author is picking something on the table for a message (the Step tab sets it). */
  readonly picking = signal(false);
  /** The page is on its way to another game, or back to the hall: nothing more is asked or said. */
  readonly gone = signal(false);
  /** Asked on the way to the Game Hall with unsaved changes (the leave dialog), or null. */
  readonly leaveQuestion = signal<LeaveQuestion | null>(null);
  /** The view the panel remembers (tab, selection, folding). */
  readonly ui = this.store.ui;
  /** The opponent has just been held for the author: the Opponent tab's button says so until it is shown. */
  private readonly calling = signal(false);

  /** Whether the state is this page's game's (else it has moved on, or there is no draft). */
  readonly here = computed(() => {
    const draft = this.draft();
    return draft !== null && isThisGame(draft, this.session.openGame);
  });
  readonly view = computed<AuthoringView>(() => {
    const state = this.state();
    if (state === null) return 'none';
    if (!this.here()) return 'elsewhere';
    return this.draft()?.mode === 'test' ? 'test' : 'author';
  });
  readonly dock = computed<AuthoringDock>(() => {
    if (!this.active()) return { width: 0, folded: false, floating: false };
    // In a player's game of the draft the table has the screen to itself, as a player's would
    const draft = this.draft();
    const floating = draft ? draft.mode === 'test' : this.guide.info()?.draftTest === true;
    if (floating) return { width: 0, folded: false, floating };
    const folded = this.ui().folded;
    return { width: folded ? FOLDED_WIDTH : PANEL_WIDTH, folded, floating };
  });
  /** The opponent's decision is held for the author. */
  readonly wanted = computed(() => this.draft()?.waiting.opponent?.held === true);
  /** The tabs that call for attention. */
  readonly attention = computed<ReadonlySet<AuthoringTab>>(
    () => new Set<AuthoringTab>(this.calling() && this.ui().tab !== 'opponent' ? ['opponent'] : []),
  );

  private stepTab: StepTabHooks = NO_STEP_TAB;
  private started = false;
  private stateText = '';
  private settlingCount = 0;
  private wasHeld = false;
  /** The selection waits for the author to be done in the Step tab before it follows the game. */
  private followDue = false;
  /** Changes made as several calls: a fresh game is gone to once they all have (holdArrivals). */
  private holding = false;
  private arrived: string | null = null;
  /** The problem said before a save that went through (it is still so: the Step tab's step was refused). */
  private unsentProblem: string | null = null;
  private decisionBefore: PendingDecision | null = null;
  private readonly timers = new Map<string, ReturnType<typeof setTimeout>>();
  private destroyed = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.destroyed = true;
      for (const timer of this.timers.values()) clearTimeout(timer);
      this.cleanUp();
    });
    effect(() => {
      if (this.active()) untracked(() => this.start());
    });
    // The game has sent something: where it stands may have changed
    effect(() => {
      if (this.session.model().eventCount > 0 && untracked(this.active)) untracked(() => this.gameMoved());
    });
  }

  private start(): void {
    if (this.started) return;
    this.started = true;
    // What went wrong in the game the page has just left, carried across
    const carried = this.ui().carried;
    if (carried !== null) {
      this.problem.set(carried);
      this.store.update({ carried: null });
    }
    const gate: DecisionGate = {
      allows: () => {
        this.decisionBefore = this.session.model().decision;
        return true;
      },
      after: () => {
        if (this.session.model().decision !== this.decisionBefore) this.answeredHere();
      },
    };
    const removeGate = this.gates.add(gate);
    const detach = this.guide.attachAuthor({
      writing: this.writing,
      picking: this.picking.asReadonly(),
      settings: computed(() => {
        const draft = this.draft();
        return draft && this.here() ? settingsOf(draft.draft) : null;
      }),
      restart: (fromCheckpoint) => this.restart(fromCheckpoint),
      leaving: () => this.leaving(),
      stepShown: (step) => this.stepShown(step),
      // (a new title goes into the game in hand, which says nothing of it: the scenario's menu follows the draft)
      title: computed(() => {
        const draft = this.draft();
        if (!draft || !this.here() || draft.mode !== 'author') return null;
        // (a title left blank is none: the scenario is called by its id)
        return draft.draft.title?.trim() ? draft.draft.title : draft.draft.id;
      }),
    });
    // The server is told when this page goes away, so that the Game Hall does not go on offering a draft that has
    // been saved and left (going on to a fresh game of the draft is not going away)
    const stop = this.exit.onLeave(() => {
      if (!this.gone() && this.draft() !== null) this.api.left();
    });
    this.cleanUp = () => {
      removeGate();
      detach();
      stop();
    };
    this.refresh();
  }

  /** Undoes what start did (when the page goes). */
  private cleanUp: () => void = () => undefined;

  /* ---------------------------------------------------------------- calls */

  /**
   * Makes a call: counted while it is on its way; a change's answer is handled (a fresh game, events, the state) and
   * clears what was said to have gone wrong; a failure is said. Resolves to the answer, or null if it failed.
   */
  private call<T>(source: Observable<T>, change: boolean): Promise<T | null> {
    this.busy.update((count) => count + 1);
    return new Promise((resolve) => {
      const handlers = {
        next: (value: T) => {
          this.busy.update((count) => count - 1);
          // (what went wrong stays said until something the author does goes right)
          if (change) {
            this.problem.set(null);
            if (isResult(value)) this.handle(value);
          }
          resolve(value);
        },
        error: (error: unknown) => {
          this.busy.update((count) => count - 1);
          this.problem.set(problemOf(error));
          resolve(null);
        },
      };
      if (change) this.lifetime.write(source, handlers);
      else this.lifetime.read(source, handlers);
    });
  }

  /** What every change comes back with: a fresh game to go to, or the state of this one (and events to ask for). */
  private handle(result: AuthoringResult): void {
    const gameId = String(result.gameId);
    if (result.rebuilt && gameId !== this.session.openGame) {
      // (a change that is one of several is not gone from until the rest have been made)
      if (this.holding) this.arrived = gameId;
      else this.go(gameId);
      return;
    }
    this.setState(result.state);
    if (result.events) this.session.refresh();
  }

  /** To the page of another game of the draft (a full page load: the panel comes back as it was, from the ui). */
  go(gameId: string): void {
    this.stepTab.carry?.();
    this.store.remember();
    this.gone.set(true);
    // The game being left is given up by the server, and its ending is not news
    this.guide.leaving.set(true);
    this.cues.go(gamePageUrl(gameId));
  }

  /** Asks for the state. */
  refresh(): void {
    if (this.gone()) return;
    void this.call(this.api.state(), false).then((state) => {
      if (state) this.setState(state);
    });
  }

  /** Takes the server's state; one that is the same as last time changes nothing (nobody is disturbed). */
  setState(state: AuthoringState): void {
    const text = JSON.stringify(state);
    const same = text === this.stateText;
    this.stateText = text;
    this.clearTimer('settling');
    if (!hasDraft(state)) {
      this.state.set(state);
      return;
    }
    // The opponent's decision is about to be answered (by the script, or by itself), or left for the author; either
    // way something happens with no game event to say so: it is looked at again shortly
    const settling = state.waiting.opponent !== null && !state.waiting.opponent.held;
    this.settlingCount = settling ? (same ? this.settlingCount + 1 : 0) : 0;
    if (settling && this.settlingCount < SETTLE_TRIES) {
      this.later('settling', SETTLE_MS, () => {
        if (this.busy() === 0) this.refresh();
      });
    }
    if (same) return;
    this.state.set(state);
    // (how the last save went is no longer news once the draft has been changed again)
    if (state.dirty && this.saved()?.stays === false) this.saved.set(null);
    this.settle();
    const held = state.waiting.opponent?.held === true;
    if (held && !this.wasHeld && this.writing()) this.calling.set(true);
    this.wasHeld = held;
  }

  /** The game sent something: the state is looked at once it has settled (unless a call's answer will bring it). */
  private gameMoved(): void {
    this.later('moved', MOVED_MS, () => {
      if (this.busy() === 0) this.refresh();
    });
  }

  /**
   * The author has answered a decision on the table. What it led to can be the opponent being asked, or the script
   * being written, with no game event to say so: the state is looked at once the answer has had time to arrive, and
   * again a little later.
   */
  private answeredHere(): void {
    const [first, second] = ANSWERED_MS;
    this.later('answered', first, () => {
      this.gameMoved();
      this.later('answered', second - first, () => this.gameMoved());
    });
  }

  private later(name: string, ms: number, action: () => void): void {
    this.clearTimer(name);
    if (this.destroyed) return;
    this.timers.set(
      name,
      setTimeout(() => {
        this.timers.delete(name);
        action();
      }, ms),
    );
  }

  private clearTimer(name: string): void {
    const timer = this.timers.get(name);
    if (timer !== undefined) clearTimeout(timer);
    this.timers.delete(name);
  }

  /* ---------------------------------------------------------------- the selection, the tabs */

  /** Which step is selected, for the state now (see selection.ts). */
  private settle(): void {
    const draft = this.draft();
    if (!draft) return;
    const { selection, due } = settle(draft, this.ui(), this.engaged());
    this.followDue = due;
    this.store.update(selection);
  }

  /** Whether the author is in the middle of something in the Step tab that turning to another step would lose. */
  engaged(): boolean {
    return this.ui().tab === 'step' && (this.picking() || this.stepTab.engaged());
  }

  /** Turns to the step the game came to while the author was busy with another, now that they are not. */
  catchUp(): void {
    if (!this.followDue || !this.draft() || this.engaged()) return;
    this.settle();
  }

  showTab(tab: AuthoringTab): void {
    if (tab === 'opponent') this.calling.set(false);
    this.store.update({ tab });
  }

  /** Changes the view the panel remembers (for the tabs: what they gather, where the Board's changes go...). */
  remember(patch: Partial<AuthoringUi>): void {
    this.store.update(patch);
  }

  fold(folded = !this.ui().folded): void {
    this.store.update({ folded });
  }

  /** Selects a step (from 0), or one of a block's steps (its name), and turns to the Step tab. */
  select(index: number, sub: string | null = null): void {
    const draft = this.draft();
    if (!draft) return;
    this.store.update({ selected: index + 1, selectedUid: draft.uids[index] ?? null, sub, tab: 'step' });
  }

  /** "Follow the game": turned on, the step the game is at is selected at once. */
  setFollow(follow: boolean): void {
    this.store.update({ follow, jump: follow });
    this.settle();
  }

  /** The Step tab's say (see StepTabHooks); the function returned takes it away again. */
  attachStepTab(hooks: StepTabHooks): () => void {
    this.stepTab = hooks;
    return () => {
      if (this.stepTab === hooks) this.stepTab = NO_STEP_TAB;
    };
  }

  /** Picking something on the table, with what to tell the author meanwhile; ended by stopPicking. */
  startPicking(told: string): void {
    this.picking.set(true);
    this.told.set(told);
  }

  stopPicking(): void {
    this.picking.set(false);
    this.told.set(null);
    this.catchUp();
  }

  /* ---------------------------------------------------------------- changes */

  /** Sends what the Step tab has not sent (StepTabHooks.flush); what goes elsewhere goes only if that `went`. */
  async flush(regardless = false): Promise<StepFlushResult> {
    return this.stepTab.flush(regardless);
  }

  /** Changes the draft (see DraftPatch); `goto`: a fresh game at that step. Resolves to the answer, or null. */
  edit(patch: DraftPatch, goto: number | null = null): Promise<AuthoringResult | null> {
    return this.call(this.api.edit(patch, goto), true);
  }

  /** A fresh game, played by the script up to the step (from 1); `sub`: up to that one of a block's steps (from 1). */
  async goTo(step: number, sub: number | null = null): Promise<void> {
    if (!(await this.flush()).went) return;
    await this.call(this.api.goTo(step, sub), true);
  }

  /** The panel's ticks ("Write down what I do", "Ask me for the opponent"), the forks' choices, the left-out notice. */
  async settings(settings: AuthoringSettings): Promise<void> {
    await this.call(this.api.settings(settings), true);
  }

  /** A step for the decision that is waiting, written where the script stands; the Step tab turns to it. */
  async stepHere(): Promise<void> {
    if (!(await this.flush()).went) return;
    this.store.update({ jump: true, tab: 'step' });
    this.stepTab.reset();
    await this.call(this.api.here(), true);
  }

  /** A step put in before the one in hand, for the decision it has (a block's step's Insert). */
  async stepBefore(): Promise<void> {
    if (!(await this.flush()).went) return;
    this.stepTab.reset();
    await this.call(this.api.before(), true);
  }

  /** The step in hand takes what it allows, and its answer, from what its author does next. */
  async retake(): Promise<void> {
    if (!(await this.flush()).went) return;
    this.stepTab.reset();
    await this.call(this.api.retake(), true);
  }

  /** A player's game of the draft from the step. */
  async startTest(step: number): Promise<void> {
    if (!(await this.flush()).went) return;
    await this.call(this.api.test(step), true);
  }

  /** From a test, back to writing. */
  async resume(): Promise<void> {
    await this.call(this.api.resume(), true);
  }

  /**
   * Any call of AuthoringApi made as the panel makes them (counted, its answer handled, a failure said): for what
   * the tabs do that has no method here.
   */
  send<T>(source: Observable<T>, change = true): Promise<T | null> {
    return this.call(source, change);
  }

  /**
   * A change to which steps there are. One at or before where the game is brings a fresh game, at the step changed.
   * `select`: the step (from 1) to select once it is back, whichever ends up there; on a refusal the selection is put
   * back as it was.
   */
  async changeSteps(ops: readonly StepOp[], firstChanged: number, select: number | null): Promise<boolean> {
    const draft = this.draft();
    if (!draft) return false;
    const was = this.selection();
    if (select !== null) this.store.update({ selected: select, selectedUid: null, sub: null, hold: true });
    this.stepTab.reset();
    const result = await this.edit({ steps: ops }, stepsGoto(firstChanged, draft.position.step));
    if (!result) this.refused(was);
    return result !== null;
  }

  /**
   * A change to which steps a block has (the block at `index`, from 0). In a block the game has been through it brings
   * a fresh game, at the block; in the block the game is in, the game carries on if it can. `subAt`: where in the
   * block the step to turn to comes (from 0); null for the block itself.
   */
  async changeBlock(ops: readonly StepOp[], index: number, subAt: number | null): Promise<boolean> {
    const draft = this.draft();
    if (!draft) return false;
    const was = this.selection();
    this.store.update({ selected: index + 1, selectedUid: null, sub: null, subAt, hold: true });
    this.stepTab.reset();
    const result = await this.edit({ steps: ops }, blockGoto(index, draft.position.step));
    if (!result) this.refused(was);
    return result !== null;
  }

  private selection(): Pick<AuthoringUi, 'selected' | 'selectedUid' | 'sub'> {
    const { selected, selectedUid, sub } = this.ui();
    return { selected, selectedUid, sub };
  }

  /** A change to the steps was refused (what went wrong is said): the Step tab goes back to the step it was on. */
  private refused(was: Pick<AuthoringUi, 'selected' | 'selectedUid' | 'sub'>): void {
    this.store.update({ ...was, subAt: null, hold: false });
    this.stepTab.reset();
  }

  /**
   * A step put in before the step now at `index` (from 0); the Step tab turns to it. `awaiting`: it takes its answer
   * from what is done when the game comes to it.
   */
  async insertStep(index: number, step: DraftStep, awaiting = false): Promise<void> {
    if (!(await this.flush()).went) return;
    const draft = this.draft();
    if (!draft) return;
    this.store.update({ tab: 'step' });
    await this.changeSteps([insertOp(draft, index, step, awaiting)], index + 1, index + 1);
  }

  /** "Rewrite from here": the step the game is at and every one after it taken out, to be written again by playing. */
  async rewriteFromHere(): Promise<void> {
    const draft = this.draft();
    if (!draft) return;
    const from = draft.position.step;
    const last = stepsOf(draft).length;
    if (from > last) return;
    const sure = await this.notifier.confirm(
      `Take steps ${from} to ${last} out of the script, messages and all, and write them again by playing?`,
      { title: 'Rewrite from here', ok: 'Take them out', cancel: 'Keep them' },
    );
    if (!sure) return;
    this.store.update({ jump: true });
    await this.changeSteps(rewriteOps(draft, from), from, null);
  }

  /** The last checkpoint before where the game is (or, `inclusive`, at it); 1 if there is none. */
  lastCheckpoint(inclusive: boolean): number {
    const draft = this.draft();
    return draft ? lastCheckpoint(draft, inclusive) : 1;
  }

  /**
   * "Start again" from the scenario's menu: a draft's game starts again by the script playing itself (to the last
   * checkpoint), a test from where it began. False when there is no draft to do it with.
   */
  restart(fromCheckpoint: boolean): boolean {
    const draft = this.draft();
    if (!draft) return false;
    if (!this.writing()) void this.startTest(draft.testFrom ?? 1);
    else void this.goTo(fromCheckpoint ? this.lastCheckpoint(true) : 1);
    return true;
  }

  /**
   * Holds a fresh game a change answers with until the rest of the changes it is one of have been made (the Board
   * tab's several calls); `releaseArrivals` then goes to it, carrying what went wrong (`carried`) to its page.
   */
  holdArrivals(): void {
    this.holding = true;
    this.arrived = null;
  }

  /** Whether a fresh game was gone to. */
  releaseArrivals(carried: string | null = null): boolean {
    this.holding = false;
    const arrived = this.arrived;
    this.arrived = null;
    if (arrived === null) return false;
    if (carried !== null) this.store.update({ carried });
    this.go(arrived);
    return true;
  }

  /* ---------------------------------------------------------------- notices */

  dismissProblem(): void {
    this.problem.set(null);
  }

  /** The server's notice has been read. */
  readNotice(): void {
    this.noticeRead.set(this.draft()?.notice ?? null);
  }

  /** The script's problems so far have been read. */
  readProblems(): void {
    this.problemsRead.set(this.draft()?.problems.length ?? 0);
  }

  dismissSaved(): void {
    this.saved.set(null);
  }

  /** The parts of the file left out to open it have been seen: put away for good. */
  seenLeftOut(): Promise<void> {
    return this.settings({ seenLeftOut: true });
  }

  /* ---------------------------------------------------------------- saving, leaving */

  /** Whether the file may be written over, where that loses comments somebody wrote in it by hand (asked). */
  private async mayOverwrite(): Promise<boolean> {
    const lost = this.draft()?.commentsLost ?? 0;
    return (
      lost === 0 ||
      this.notifier.confirm(
        `The file has ${lost} line${lost === 1 ? '' : 's'} of comment that will not be in it after saving. ` +
          '(Notes are part of the scenario, and are kept.)\n\nSave over it?',
        { title: 'Save', ok: 'Save over it', cancel: 'Cancel' },
      )
    );
  }

  /**
   * Sends the Step tab's form, as flush does, for something that writes the draft to its file: that goes ahead
   * whether or not the server took the step. Resolves to what to say of a step that wasn't taken, or null.
   */
  private async flushForSave(): Promise<string | null> {
    const flushed = await this.flush(true);
    if (flushed.went) return null;
    this.unsentProblem = this.problem();
    return flushed.note;
  }

  /** Save: the draft to its file (asked first if comments would be lost), and how that went said. */
  async save(): Promise<void> {
    if (!(await this.mayOverwrite())) return;
    const unsent = await this.flushForSave();
    const result = await this.call(this.api.save(), true);
    if (result) this.afterSave(result, unsent);
  }

  /** What is said of a save that went through: at the top of the panel, and in full under Scenario. */
  afterSave(result: AuthoringResult, unsent: string | null): void {
    if (!result.saved) return;
    const { notice, report } = savedNotice(result.saved, unsent);
    this.report.set(notice.good && result.saved.warnings.length === 0 && this.ui().tab !== 'scenario' ? null : report);
    this.saved.set(notice);
    // (a save that goes through clears what was said to have gone wrong, which here is still so)
    if (unsent !== null) this.problem.set(this.unsentProblem);
  }

  /** Closes the draft and goes back to the Game Hall (what is not saved is lost). */
  async close(): Promise<boolean> {
    const closed = await this.call(this.api.close(), true);
    if (!closed) return false;
    this.gone.set(true);
    this.hall.go();
    return true;
  }

  /** Whether the draft has changes in no file (and this is the game it is being written in). */
  unsaved(): boolean {
    const draft = this.draft();
    return this.writing() && !this.gone() && draft !== null && draft.mode !== 'test' && draft.dirty && this.here();
  }

  /** On the way back to the Game Hall: with unsaved changes, the question is asked instead (true). */
  private leaving(): boolean {
    if (!this.unsaved()) return false;
    this.askBeforeLeaving();
    return true;
  }

  /** Asks what is to become of the unsaved changes on the way to the Game Hall (the leave dialog). */
  askBeforeLeaving(): void {
    this.leaveQuestion.set({ busy: false, said: null });
  }

  /** "Stay here". */
  stay(): void {
    this.leaveQuestion.set(null);
  }

  /** "Save and exit": saves, then closes the draft and goes; a step the server would not take keeps the author. */
  async saveAndExit(): Promise<void> {
    if (!(await this.mayOverwrite())) return;
    this.leaveQuestion.set({ busy: true, said: null });
    const unsent = await this.flushForSave();
    const result = await this.call(this.api.save(), true);
    if (!result) {
      this.leaveFailed();
      return;
    }
    if (unsent === null) {
      await this.away();
      return;
    }
    // (the step that was not taken would be lost by leaving: the rest is saved, and the author stays)
    this.leaveQuestion.set({
      busy: false,
      said: `The rest is saved. ${unsent} Fix it and save again, or discard it by leaving with Discard changes.`,
    });
    this.afterSave(result, unsent);
  }

  /** "Discard changes": closes the draft as it was last saved, and goes. */
  async discardAndExit(): Promise<void> {
    this.leaveQuestion.set({ busy: true, said: null });
    await this.away();
  }

  private async away(): Promise<void> {
    if (await this.close()) this.leaveQuestion.set(null);
    else this.leaveFailed();
  }

  private leaveFailed(): void {
    this.leaveQuestion.set({ busy: false, said: this.problem() ?? 'That could not be done.' });
  }

  /* ---------------------------------------------------------------- the Step tab's message on the screen */

  /** The message the author last worked on in the Step tab: put up again whenever its step is (legacy focusMessage). */
  readonly focusMessage = signal<MessageAt | null>(null);

  /** A message of the Step tab's has the focus: it is put up now if its step is on the screen, and again each time it is. */
  focusOn(at: MessageAt | null): void {
    this.focusMessage.set(at);
    if (at) this.guide.preview(at.step, at.sub, at.index);
  }

  /** Where a message's bubble sits, tried on the screen at once if that message of that step is up (legacy tryOnScreen). */
  tryPlace(at: MessageAt, place: TriedPlace): boolean {
    const shown = this.guide.step();
    return shown !== null && shown.step === at.step && shown.sub === at.sub && this.guide.tryPlace(at.index, place);
  }

  /** A step put on the screen: the message the author was working on in it is put up (legacy stepShown). */
  private stepShown(step: ScenarioStep): void {
    const at = this.focusMessage();
    if (at?.step === step.step && at.sub === step.sub) this.guide.preview(at.step, at.sub, at.index);
  }

  /** The draft state's steps, or none. */
  steps(): readonly DraftStep[] {
    const draft: DraftState | null = this.draft();
    return draft ? stepsOf(draft) : [];
  }

  /* ---------------------------------------------------------------- the Scenario tab's */

  /**
   * Save as (`copy`: a scenario of its own, which the draft is from here on) or Rename (the file moved; what is not
   * saved stays unsaved), to a tidy path (filing.ts). Resolves true when it was done.
   */
  async saveAs(how: 'copy' | 'rename', path: string, canonical: boolean): Promise<boolean> {
    const unsent = await this.flushForSave();
    const result = await this.call(this.api.save({ as: how, path, canonical }), true);
    if (!result) return false;
    if (how === 'copy') {
      this.afterSave(result, unsent);
      return true;
    }
    const tail = unsent === null ? '' : ` ${unsent}`;
    this.saved.set({
      good: unsent === null,
      why: false,
      stays: true,
      text: `The file is now ${result.renamed}.${tail}`,
    });
    if (unsent !== null) this.problem.set(this.unsentProblem);
    return true;
  }

  /**
   * Submits the draft's file for review (the author has been asked): saved first if it needs to be, and submitted
   * only if that save plays through with nothing left unsent (otherwise what the save says is said, and nothing is
   * submitted). Submitting closes the draft: the page goes back to the Game Hall's My Scenarios.
   */
  async submit(): Promise<boolean> {
    const draft = this.draft();
    if (!draft) return false;
    if (draft.dirty || !draft.onDisk) {
      if (!(await this.mayOverwrite())) return false;
      const unsent = await this.flushForSave();
      const result = await this.call(this.api.save(), true);
      if (!result) return false;
      const saved = result.saved;
      if (!saved || saved.errors.length > 0 || !saved.verified || unsent !== null) {
        this.afterSave(result, unsent);
        return false;
      }
    }
    if (!(await this.call(this.api.submit(), true))) return false;
    this.gone.set(true);
    this.hall.go('#account/scenarios');
    return true;
  }

  /** Check: the draft played through with nobody at the table, as the server does before it offers a scenario. */
  async check(): Promise<void> {
    const result = await this.call(this.api.check(), true);
    if (result) this.report.set(checkReport(result));
  }

  /** Try other answers: every other answer the player could give, played up to the script's next step. Slow. */
  async offScript(): Promise<void> {
    this.report.set('Trying other answers...');
    const result = await this.call(this.api.offScript(), true);
    this.report.set(result ? offScriptReport(result) : null);
  }

  /** See the file: the draft as the file it would be saved as. */
  async seeFile(): Promise<void> {
    const result = await this.call(this.api.source(), false);
    if (result) this.report.set(result.source);
  }

  /* ---------------------------------------------------------------- a puzzle's */

  /**
   * A filter for a slot's bound, handed to the panel from outside it (the table's search window, set to bound a
   * slot): the Board tab's slot form of that slot fills its Bound field with it and sends it, as if typed there.
   */
  readonly slotBoundOffer = signal<SlotBoundOffer | null>(null);
  private boundOffers = 0;

  /** Hands a slot's bound to its form (see slotBoundOffer), the panel turning to the Board tab where it is. */
  offerSlotBound(slot: string, filter: string): void {
    this.slotBoundOffer.set({ slot, filter, serial: ++this.boundOffers });
    this.showTab('board');
  }

  /** The slot form has taken the offer. */
  tookSlotBound(serial: number): void {
    if (this.slotBoundOffer()?.serial === serial) this.slotBoundOffer.set(null);
  }

  /** A puzzle's solution recorded, its recording stopped, played, used or deleted (AuthoringApi.solution). */
  async solution(
    op: SolutionOp,
    name: string | null,
    picks: Readonly<Record<string, string>> | null = null,
  ): Promise<boolean> {
    if (!(await this.flush()).went) return false;
    return (await this.call(this.api.solution(op, name, picks), true)) !== null;
  }
}

/** A filter for a slot's bound, handed to its form from outside it; `serial` tells one offer from the next. */
export interface SlotBoundOffer {
  slot: string;
  filter: string;
  serial: number;
}
