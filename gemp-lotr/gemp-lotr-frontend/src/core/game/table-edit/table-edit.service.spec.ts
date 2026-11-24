import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Observable, Subject } from 'rxjs';
import { SANDBOX_STATE } from '../../../testing/table-edit';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { ApiError } from '../../api/api-error';
import { Notifier } from '../../notifier/notifier';
import { PageCues } from '../../platform/page-cues';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { SessionService } from '../../session/session.service';
import { DecisionGates } from '../decision/decision-gate';
import { ScenarioEnd, ScenarioInfo } from '../scenario/scenario-events';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { GameUndo } from '../undo/game-undo.service';
import { EditBoard, readEditBoard } from './edit-board';
import { EditOp } from './edit-ops';
import { TABLE_EDIT_BACKENDS, TableEditBackend } from './table-edit-backend';
import { NOT_DONE, PROBLEM_MS, RESET_QUESTION, TableEdit } from './table-edit.service';

const INFO: ScenarioInfo = {
  type: 'info',
  id: 'sandbox',
  kind: 'sandbox',
  title: 'Sandbox',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Sandbox',
  settings: { autoAccept: true, autoDismissDeckReveal: false, animationSpeed: null },
  bubbleGap: null,
  resumedAt: null,
  twoSided: true,
  opening: 'board',
  campaign: null,
  author: false,
  draftTest: false,
  mode: 'edit',
  level: 'sandbox',
  tools: ['add', 'move', 'wounds'],
  canStop: false,
  canStart: true,
};

/** A backend whose answers the test gives (`answer`, `refuse`), recording what it was asked. */
class FakeBackend extends TableEditBackend {
  readonly level = 'sandbox' as const;
  readonly asked: string[] = [];
  private pending = new Subject<EditBoard>();
  private resetting = new Subject<string>();

  private call(what: string): Observable<EditBoard> {
    this.asked.push(what);
    this.pending = new Subject<EditBoard>();
    return this.pending;
  }

  state(gameId: string) {
    return this.call(`state ${gameId}`);
  }
  apply(gameId: string, ops: readonly EditOp[]) {
    return this.call(`apply ${gameId} ${JSON.stringify(ops)}`);
  }
  stop(gameId: string) {
    return this.call(`stop ${gameId}`);
  }
  start(gameId: string) {
    return this.call(`start ${gameId}`);
  }
  reset(gameId: string) {
    this.asked.push(`reset ${gameId}`);
    this.resetting = new Subject<string>();
    return this.resetting;
  }

  answer(board: Partial<EditBoard> = {}): void {
    this.pending.next({ ...readEditBoard(SANDBOX_STATE), ...board });
    this.pending.complete();
  }
  refuse(error: unknown): void {
    this.pending.error(error);
  }
  resetTo(gameId: string): void {
    this.resetting.next(gameId);
  }
  resetRefused(): void {
    this.resetting.error(new Error('no'));
  }
}

describe('TableEdit', () => {
  let edit: TableEdit;
  let backend: FakeBackend;
  const info = signal<ScenarioInfo | null>(INFO);
  const end = signal<ScenarioEnd | null>(null);
  const playing = signal(true);
  const leaving = signal(false);
  const model = signal({ ended: false });
  const reopened = signal(0);
  let openGame: string | null = '12';
  const refresh = vi.fn();
  const undoRefresh = vi.fn();
  const go = vi.fn();

  beforeEach(() => {
    vi.useFakeTimers();
    info.set(INFO);
    end.set(null);
    playing.set(true);
    leaving.set(false);
    model.set({ ended: false });
    openGame = '12';
    refresh.mockClear();
    undoRefresh.mockClear();
    go.mockClear();
    backend = new FakeBackend();
    TestBed.configureTestingModule({
      providers: [
        PageLifetime,
        DecisionGates,
        ...provideFakeNotifier(),
        { provide: ScenarioGuide, useValue: { info, end, playing, leaving } },
        {
          provide: GameSession,
          useValue: {
            model,
            reopened,
            refresh,
            get openGame() {
              return openGame;
            },
          },
        },
        { provide: GameUndo, useValue: { refresh: undoRefresh } },
        { provide: PageCues, useValue: { go } },
        { provide: SessionService, useValue: { participantId: null } },
        { provide: TABLE_EDIT_BACKENDS, useValue: backend, multi: true },
        TableEdit,
      ],
    });
    edit = TestBed.inject(TableEdit);
    TestBed.tick();
  });

  afterEach(() => vi.useRealTimers());

  it("reads the mode, the level, the tools, Stop and Start from the scenario's info, and is in edit mode", () => {
    expect(edit.level()).toBe('sandbox');
    expect(edit.mode()).toBe('edit');
    expect(edit.tools()).toEqual(['add', 'move', 'wounds']);
    expect(edit.canStop()).toBe(false);
    expect(edit.canStart()).toBe(true);
    expect(edit.editing()).toBe(true);
    expect(edit.may('add')).toBe(true);
    expect(edit.may('delete')).toBe(false);
    // Edit mode begun: the table as the backend knows it
    expect(backend.asked).toEqual(['state 12']);
    backend.answer();
    expect(edit.board()?.cards.length).toBe(6);
  });

  it('has nothing to edit in an ordinary scenario, a replay, or a level the page has no backend for', () => {
    info.set({ ...INFO, level: 'none', mode: 'edit' });
    expect(edit.level()).toBe('none');
    expect(edit.mode()).toBe('play');
    expect(edit.editing()).toBe(false);
    info.set({ ...INFO, level: 'puzzle' });
    expect(edit.editing()).toBe(false);
    info.set(INFO);
    playing.set(false);
    expect(edit.level()).toBe('none');
    expect(edit.tools()).toEqual([]);
    expect(edit.canStart()).toBe(false);
  });

  it('is in play mode, and can be stopped when the scenario says so; nothing once the game is over', () => {
    info.set({ ...INFO, mode: 'play', canStop: true, canStart: false });
    expect(edit.mode()).toBe('play');
    expect(edit.editing()).toBe(false);
    expect(edit.may('add')).toBe(false);
    expect(edit.canStop()).toBe(true);
    model.set({ ended: true });
    expect(edit.canStop()).toBe(false);
    model.set({ ended: false });
    end.set({
      type: 'end',
      result: 'ended',
      messages: [],
      message: null,
      checkpoint: null,
      restart: false,
      winner: null,
      reason: null,
      reset: true,
    });
    expect(edit.canStop()).toBe(false);
  });

  it('refuses every answer to the decision in edit mode (it is not shown), and lets them through in play', () => {
    const gates = TestBed.inject(DecisionGates);
    expect(gates.allows({ kind: 'escape' })).toBe(false);
    info.set({ ...INFO, mode: 'play' });
    expect(gates.allows({ kind: 'escape' })).toBe(true);
  });

  it("makes edits; the answer's state is the board and the mode at once, and the game's events are asked for", async () => {
    backend.answer();
    const made = edit.apply([{ op: 'add', blueprint: '1_50', to: 'freeps.characters' }], { x: 5, y: 6 });
    expect(edit.busy()).toBe(true);
    expect(backend.asked.at(-1)).toBe('apply 12 [{"op":"add","blueprint":"1_50","to":"freeps.characters"}]');
    // (one at a time)
    expect(await edit.apply([{ op: 'copy', cardId: 3 }])).toBe(false);
    backend.answer({ mode: 'edit', canStop: false, canStart: true, tools: ['add'] });
    expect(await made).toBe(true);
    expect(edit.busy()).toBe(false);
    expect(edit.tools()).toEqual(['add']);
    expect(refresh).toHaveBeenCalled();
    expect(undoRefresh).toHaveBeenCalled();
    // The scenario says it again: its info from then on
    info.set({ ...INFO });
    TestBed.tick();
    expect(edit.tools()).toEqual(['add', 'move', 'wounds']);
    expect(await edit.apply([])).toBe(false);
  });

  it("says a refusal in the server's words where it was asked for, for a while", async () => {
    backend.answer();
    const made = edit.apply([{ op: 'delete', card: 'frodo' }], { x: 5, y: 6 });
    backend.refuse(new ApiError(400, 'Bad Request', { said: 'Frodo cannot be deleted' }));
    expect(await made).toBe(false);
    expect(edit.problem()).toEqual({ text: 'Frodo cannot be deleted', at: { x: 5, y: 6 } });
    // (a list refused part way was taken back: the undo notices say so, and the page reads the game then; the old
    // channel is not asked, which would answer as if another window had it)
    expect(refresh).not.toHaveBeenCalled();
    expect(undoRefresh).toHaveBeenCalled();
    vi.advanceTimersByTime(PROBLEM_MS);
    expect(edit.problem()).toBeNull();
    const again = edit.apply([{ op: 'copy', cardId: 1 }]);
    backend.refuse(new Error('down'));
    await again;
    expect(edit.problem()?.text).toBe(NOT_DONE);
    edit.dismissProblem();
    expect(edit.problem()).toBeNull();
  });

  it('stops and starts, only when the scenario says it can', async () => {
    backend.answer();
    expect(await edit.stop()).toBe(false);
    const started = edit.start();
    expect(backend.asked.at(-1)).toBe('start 12');
    backend.answer({ mode: 'play', canStop: true, canStart: false });
    expect(await started).toBe(true);
    expect(edit.mode()).toBe('play');
    expect(edit.editing()).toBe(false);
    const stopped = edit.stop();
    expect(backend.asked.at(-1)).toBe('stop 12');
    backend.answer({ mode: 'edit', canStop: false, canStart: true });
    expect(await stopped).toBe(true);
    expect(edit.mode()).toBe('edit');
  });

  it('reads the board again when edit mode begins, or the game is read again; not without a game', async () => {
    backend.answer();
    info.set({ ...INFO, mode: 'play' });
    TestBed.tick();
    info.set({ ...INFO });
    TestBed.tick();
    expect(backend.asked).toEqual(['state 12', 'state 12']);
    backend.answer();
    reopened.set(1);
    TestBed.tick();
    expect(backend.asked.length).toBe(3);
    backend.refuse(new Error('gone'));
    openGame = null;
    edit.refresh();
    expect(backend.asked.length).toBe(3);
    await expect(edit.apply([{ op: 'copy', cardId: 1 }])).resolves.toBe(false);
  });

  it('resets after asking, and goes to the fresh game; or says why it could not', async () => {
    backend.answer();
    const notifier = TestBed.inject(Notifier) as FakeNotifier;
    notifier.answers.push(false);
    await edit.reset();
    expect(notifier.dialogs).toEqual([`confirm: ${RESET_QUESTION}`]);
    expect(backend.asked.at(-1)).toBe('state 12');
    notifier.answers.push(true);
    void edit.reset();
    await settle();
    expect(backend.asked.at(-1)).toBe('reset 12');
    expect(leaving()).toBe(true);
    backend.resetTo('13');
    expect(go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=13');
    leaving.set(false);
    void edit.reset(false);
    await settle();
    backend.resetRefused();
    expect(edit.problem()).toEqual({ text: NOT_DONE, at: null });
    expect(leaving()).toBe(false);
    expect(edit.busy()).toBe(false);
  });
});
