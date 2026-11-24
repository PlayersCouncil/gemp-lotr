import { computed, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Notifier } from '../../notifier/notifier';
import { gamePageUrl } from '../../platform/app-paths';
import { PageCues } from '../../platform/page-cues';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { SessionService } from '../../session/session.service';
import { DecisionGate, DecisionGates } from '../decision/decision-gate';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { GameUndo } from '../undo/game-undo.service';
import { EditBoard, EditLevel, EditMode } from './edit-board';
import { EditOp, EditTool } from './edit-ops';
import { TABLE_EDIT_BACKENDS, TableEditBackend } from './table-edit-backend';

/** A place of the window (px), where something was asked for: a refusal is said there. */
export interface EditPoint {
  x: number;
  y: number;
}

/** The last refusal, in the server's words, and where it was asked for (null: by the edit bar). */
export interface EditProblem {
  text: string;
  at: EditPoint | null;
}

/** How long a refusal stays up (ms). */
export const PROBLEM_MS = 8000;
/** Said when the server gives no words of its own. */
export const NOT_DONE = 'That could not be done.';
/** Asked before Reset. */
export const RESET_QUESTION = 'Reset the table to how it opened? Everything changed and played is lost.';
export const RESET_WORDS = { title: 'Reset', ok: 'Reset', cancel: 'Keep' } as const;

type Said = Pick<EditBoard, 'mode' | 'tools' | 'canStop' | 'canStart'>;

/**
 * The table changed by hand: whether the game is played or being edited (`mode`), who may change it and how much
 * (`level`, `tools`), Stop / Start / Reset, and the edits themselves (`apply`). The on-table controls (the edit bar,
 * the open spots, the chooser, the search window, the card menu) all talk to this, and this talks to the backend of
 * the level (TableEditBackend: the sandbox's, SandboxBackend). The scenario's info event says the mode, level, tools
 * and whether Stop and Start can be pressed; an answer of the backend says it too, a moment sooner. `board` is the
 * table as the backend knows it (what the page's game view does not have: aliases, owners, hand-put modifiers, the
 * Modify… vocabulary), read when edit mode begins, after each change and after the game is taken back.
 *
 * In edit mode nobody is asked anything: this is one of the page's decision gates and refuses every answer (and the
 * page does not show the waiting decision: decisionView). The game itself shows each edit, through its own events,
 * which are asked for at once after it (GameSession.refresh). Page-scoped.
 */
@Injectable()
export class TableEdit implements DecisionGate {
  private readonly guide = inject(ScenarioGuide);
  private readonly session = inject(GameSession);
  private readonly undo = inject(GameUndo);
  private readonly lifetime = inject(PageLifetime);
  private readonly notifier = inject(Notifier);
  private readonly cues = inject(PageCues);
  private readonly acting = inject(SessionService);
  private readonly backends: readonly TableEditBackend[] = inject(TABLE_EDIT_BACKENDS, { optional: true }) ?? [];

  /** What the backend's last answer said, until the scenario's next info event says it again. */
  private readonly said = signal<Said | null>(null);
  private readonly info = computed(() => (this.guide.playing() ? this.guide.info() : null));
  /** The game has ended (its scenario, or the game itself): nothing more is changed. */
  private readonly over = computed(
    () => this.guide.end() !== null || this.session.model().ended || this.board()?.over === true,
  );

  /** Who may change the table: nobody, a puzzle's solver, a sandbox's player (an author: later). */
  readonly level = computed<EditLevel>(() => this.info()?.level ?? 'none');
  /** The backend of the level, if the page has one. */
  private readonly backend = computed(() => this.backends.find((backend) => backend.level === this.level()) ?? null);
  readonly mode = computed<EditMode>(() =>
    this.level() === 'none' ? 'play' : (this.said()?.mode ?? this.info()?.mode ?? 'play'),
  );
  readonly tools = computed<readonly EditTool[]>(() => this.said()?.tools ?? this.info()?.tools ?? []);
  readonly canStop = computed(
    () => this.level() !== 'none' && !this.over() && (this.said()?.canStop ?? this.info()?.canStop ?? false),
  );
  readonly canStart = computed(
    () => this.level() !== 'none' && !this.over() && (this.said()?.canStart ?? this.info()?.canStart ?? false),
  );
  /** The table is being changed by hand now: the controls are live, and nobody is asked anything. */
  readonly editing = computed(() => this.mode() === 'edit' && this.backend() !== null && !this.over());
  /** A change is on its way to the backend. */
  readonly busy = signal(false);
  /** The last refusal, in the backend's words. */
  readonly problem = signal<EditProblem | null>(null);
  /** The table as the backend knows it, or null before it has been read. */
  readonly board = signal<EditBoard | null>(null);

  constructor() {
    inject(DecisionGates).add(this);

    // The scenario has said it again: its info is the truth from now on
    effect(() => {
      this.info();
      untracked(() => this.said.set(null));
    });

    // Edit mode begun, or the game read again (taken back, reopened): the table as the backend knows it
    effect(() => {
      const editing = this.editing();
      this.session.reopened();
      untracked(() => {
        if (editing) this.refresh();
      });
    });
  }

  /** Whether the tool can be used now: in edit mode, and allowed by the level. */
  may(tool: EditTool): boolean {
    return this.editing() && this.tools().includes(tool);
  }

  /** Reads the table from the backend again. */
  refresh(): void {
    const backend = this.backend();
    const gameId = this.session.openGame;
    if (!backend || !gameId) return;
    this.lifetime.read(backend.state(gameId), { next: (board) => this.board.set(board), error: () => undefined });
  }

  /** Makes the edits (in order, all or none); resolves to whether they were made. A refusal is said at `at`. */
  apply(ops: readonly EditOp[], at: EditPoint | null = null): Promise<boolean> {
    if (ops.length === 0 || !this.editing()) return Promise.resolve(false);
    return this.run((backend, gameId) => backend.apply(gameId, ops), at);
  }

  /** Stop: play leaves for edit mode. */
  stop(): Promise<boolean> {
    return this.canStop() ? this.run((backend, gameId) => backend.stop(gameId), null) : Promise.resolve(false);
  }

  /** Start: edit mode leaves for play. */
  start(): Promise<boolean> {
    return this.canStart() ? this.run((backend, gameId) => backend.start(gameId), null) : Promise.resolve(false);
  }

  /**
   * Reset: a fresh game of the scenario as it opened, which the page goes to (as to any fresh game of a scenario).
   * Asked about first, unless `ask` is false (the game is over).
   */
  async reset(ask = true): Promise<void> {
    const backend = this.backend();
    const gameId = this.session.openGame;
    if (!backend || !gameId || this.busy() || this.guide.leaving()) return;
    if (ask && !(await this.notifier.confirm(RESET_QUESTION, RESET_WORDS))) return;
    this.busy.set(true);
    this.guide.leaving.set(true);
    this.lifetime.write(backend.reset(gameId), {
      next: (fresh) => this.cues.go(gamePageUrl(fresh, this.acting.participantId)),
      error: (error: unknown) => {
        this.busy.set(false);
        this.guide.leaving.set(false);
        this.refuse(error, null);
      },
    });
  }

  /** The refusal has been read (or the person has moved on). */
  dismissProblem(): void {
    this.problem.set(null);
  }

  allows(): boolean {
    // (in edit mode the decision is not shown, so this is only ever a stray key or click)
    return !this.editing();
  }

  private run(call: (backend: TableEditBackend, gameId: string) => Observable<EditBoard>, at: EditPoint | null) {
    const backend = this.backend();
    const gameId = this.session.openGame;
    if (!backend || !gameId || this.busy()) return Promise.resolve(false);
    this.busy.set(true);
    this.problem.set(null);
    return new Promise<boolean>((resolve) => {
      this.lifetime.write(call(backend, gameId), {
        next: (board) => {
          this.busy.set(false);
          this.board.set(board);
          this.said.set({ mode: board.mode, tools: board.tools, canStop: board.canStop, canStart: board.canStart });
          this.moved();
          resolve(true);
        },
        error: (error: unknown) => {
          this.busy.set(false);
          this.refuse(error, at);
          // (a list refused part way was taken back whole: the page is told so, and reads the game again)
          this.moved();
          resolve(false);
        },
      });
    });
  }

  /** The game has changed: its events are asked for at once (a decision waits, so nothing else asks), and undo's list. */
  private moved(): void {
    this.session.refresh();
    this.undo.refresh();
  }

  private refuse(error: unknown, at: EditPoint | null): void {
    const problem: EditProblem = { text: ApiError.from(error).said ?? NOT_DONE, at };
    this.problem.set(problem);
    this.lifetime.later(() => {
      if (this.problem() === problem) this.problem.set(null);
    }, PROBLEM_MS);
  }
}
