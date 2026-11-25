import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { decision, draftState, position, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { sentForm } from '../../../testing/sent-form';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { parseJson } from '../../api/json';
import { CardInfoService } from '../../cards/card-info.service';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageExit } from '../../platform/page-exit';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { GameDecisions } from '../decision/game-decisions.service';
import { provideGameState } from '../game-state';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { ScenarioRoom } from '../scenario/scenario-room';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { AuthoringApi } from './authoring-api.service';
import { AuthoringState, DraftState } from './authoring-state';
import { AUTHOR_UI_KEY } from './authoring-ui-store';
import {
  ANSWERED_MS,
  Authoring,
  FOLDED_WIDTH,
  MOVED_MS,
  PANEL_WIDTH,
  SETTLE_MS,
  SETTLE_TRIES,
  StepFlushResult,
} from './authoring.service';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

const INFO = {
  type: 'info',
  id: 'test',
  kind: 'tutorial',
  title: 'A Test',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Tutorial',
  autoAccept: true,
  autoDismissDeckReveal: true,
  animationSpeed: null,
  bubbleGap: null,
  resumedAt: null,
  campaign: null,
  author: true,
  draftTest: false,
};

const xmlJson = (value: unknown) =>
  JSON.stringify(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const S = (event: unknown) => `<ge type="S" scenario="${xmlJson(event)}"/>`;
const TABLE =
  '<ge type="P" participantId="asdf" allParticipantIds="asdf,~Scenario" discardPublic="false"/>' +
  '<ge type="PCIP" cardId="7" blueprintId="1_290" participantId="asdf" zone="FREE_CHARACTERS"/>';
const ACTIONS =
  '<ge type="D" id="3" decisionType="CARD_ACTION_CHOICE" participantId="asdf" text="Play">' +
  '<parameter name="cardId" value="7"/><parameter name="blueprintId" value="inPlay"/>' +
  '<parameter name="actionId" value="12"/><parameter name="actionText" value="Use"/></ge>';

describe('Authoring', () => {
  let authoring: Authoring;
  let session: GameSession;
  let guide: ScenarioGuide;
  let backend: HttpTestingController;
  let notifier: FakeNotifier;
  let cues: { go: ReturnType<typeof vi.fn> };
  let hall: { go: ReturnType<typeof vi.fn>; openHere: ReturnType<typeof vi.fn>; elsewhere: ReturnType<typeof signal> };
  let exit: { onLeave: ReturnType<typeof vi.fn>; beacon: ReturnType<typeof vi.fn>; leaving: (() => void) | null };

  function setUp() {
    cues = { go: vi.fn() };
    hall = { go: vi.fn(), openHere: vi.fn(), elsewhere: signal<string | null>(null) };
    exit = {
      leaving: null,
      beacon: vi.fn(() => true),
      onLeave: vi.fn((leaving: () => void) => {
        exit.leaving = leaving;
        return () => (exit.leaving = null);
      }),
    };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: cues },
        { provide: HallTab, useValue: hall },
        { provide: PageExit, useValue: exit },
      ],
    });
    authoring = TestBed.inject(Authoring);
    session = TestBed.inject(GameSession);
    guide = TestBed.inject(ScenarioGuide);
    backend = TestBed.inject(HttpTestingController);
    notifier = TestBed.inject(FakeNotifier);
  }

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    setUp();
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  /** Opens game 7 of a draft (the scenario says its player writes it, or with `info` changed). */
  function open(info: Partial<typeof INFO> = {}, events = ACTIONS) {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(`<gameState cn="1">${TABLE}${S({ ...INFO, ...info })}${events}</gameState>`);
    TestBed.tick();
  }

  const asked = (what: string, method = 'POST') => backend.match((r) => r.url === AUTHOR + what && r.method === method);

  /** The state asked for, answered with this one. */
  async function answerState(state: AuthoringState = draftState()) {
    const [request] = asked('state', 'GET');
    expect(request).toBeDefined();
    request!.flush(JSON.stringify(state));
    await settle();
    TestBed.tick();
  }

  /** The one call to `what`, answered; what it sent comes back. */
  async function answer(what: string, body: unknown, status = 200): Promise<TestRequest> {
    const requests = asked(what);
    expect(requests).toHaveLength(1);
    const request = requests[0]!;
    request.flush(JSON.stringify(body), { status, statusText: status === 200 ? 'OK' : 'No' });
    await settle();
    TestBed.tick();
    return request;
  }

  const form = (request: TestRequest, name: string) => sentForm(request.request).get(name);

  /** Open, with the draft's state taken. */
  async function ready(state: DraftState = draftState(), info: Partial<typeof INFO> = {}) {
    open(info);
    await answerState(state);
  }

  it('does nothing in an ordinary game, nor in a replay', () => {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(`<gameState cn="1">${TABLE}${ACTIONS}</gameState>`);
    TestBed.tick();
    expect(authoring.active()).toBe(false);
    expect(asked('state', 'GET')).toEqual([]);
    expect(authoring.dock()).toEqual({ width: 0, folded: false, floating: false });
    expect(authoring.view()).toBe('none');
    expect(authoring.restart(false)).toBe(false);
  });

  it("takes the draft's state once the game says it is a draft's, and docks", async () => {
    open();
    expect(authoring.active()).toBe(true);
    expect(authoring.writing()).toBe(true);
    expect(authoring.view()).toBe('none');
    expect(authoring.dock()).toEqual({ width: PANEL_WIDTH, folded: false, floating: false });
    await answerState();
    expect(authoring.view()).toBe('author');
    expect(authoring.here()).toBe(true);
    expect(authoring.steps()).toHaveLength(3);
    // (the step the game is at is selected)
    expect(authoring.ui()).toMatchObject({ selected: 1, selectedUid: 's1', atUid: 's1' });
    expect(parseJson(sessionStorage.getItem(AUTHOR_UI_KEY)!)).toMatchObject({ selected: 1 });
    authoring.fold();
    expect(authoring.dock()).toEqual({ width: FOLDED_WIDTH, folded: true, floating: false });
    authoring.fold(false);
    expect(authoring.dock().folded).toBe(false);
  });

  it('floats in a test game, and is no author there', async () => {
    open({ author: false, draftTest: true });
    expect(authoring.writing()).toBe(false);
    expect(authoring.dock()).toEqual({ width: 0, folded: false, floating: true });
    await answerState(draftState({ mode: 'test', testFrom: 2 }));
    expect(authoring.view()).toBe('test');
    expect(authoring.dock().floating).toBe(true);
  });

  it('says when the draft is gone, or has moved on to another game', async () => {
    await ready(draftState({ gameId: '8' }));
    expect(authoring.view()).toBe('elsewhere');
    expect(authoring.here()).toBe(false);
    authoring.setState({ session: false });
    expect(authoring.view()).toBe('elsewhere');
    expect(authoring.draft()).toBeNull();
    expect(authoring.steps()).toEqual([]);
    expect(authoring.lastCheckpoint(true)).toBe(1);
  });

  it('takes nothing again from a state that is the same as the last', async () => {
    await ready();
    const first = authoring.state();
    authoring.setState(draftState());
    expect(authoring.state()).toBe(first);
    authoring.setState(draftState({ dirty: true }));
    expect(authoring.state()).not.toBe(first);
  });

  it("looks again shortly while the opponent's decision is about to be answered, ten times at most unchanged", async () => {
    const settling = draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow' }) } });
    await ready(settling);
    for (let i = 0; i < SETTLE_TRIES; i++) {
      vi.advanceTimersByTime(SETTLE_MS);
      await answerState(settling);
    }
    vi.advanceTimersByTime(SETTLE_MS);
    expect(asked('state', 'GET')).toEqual([]);
    // (held for the author: nothing more to wait for)
    authoring.setState(draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow', held: true }) } }));
    vi.advanceTimersByTime(SETTLE_MS);
    expect(asked('state', 'GET')).toEqual([]);
  });

  it('does not look again while a call of its own is on its way (its answer brings the state)', async () => {
    const settling = draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow' }) } });
    await ready(settling);
    void authoring.settings({ recording: false });
    vi.advanceTimersByTime(SETTLE_MS);
    expect(asked('state', 'GET')).toEqual([]);
    expect(authoring.busy()).toBe(1);
    await answer('settings', result(draftState({ recording: false })));
    expect(authoring.busy()).toBe(0);
  });

  it('looks again once the game has sent something and settled', async () => {
    await ready();
    session.refresh();
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush('<update cn="2"><ge type="GPC" phase="SHADOW"/></update>');
    TestBed.tick();
    expect(asked('state', 'GET')).toEqual([]);
    vi.advanceTimersByTime(MOVED_MS);
    await answerState(draftState({ position: position({ phase: 'shadow' }) }));
    expect(authoring.draft()?.position.phase).toBe('shadow');
  });

  it('looks again after the author answers on the table, and a little later', async () => {
    await ready();
    TestBed.inject(GameDecisions).cardClicked('7', { x: 0, y: 0 }, 5000);
    TestBed.tick();
    backend.match((r) => r.url === GAME && r.method === 'POST');
    vi.advanceTimersByTime(ANSWERED_MS[0] + MOVED_MS);
    await answerState();
    vi.advanceTimersByTime(ANSWERED_MS[1] - ANSWERED_MS[0] + MOVED_MS);
    await answerState();
    vi.advanceTimersByTime(5000);
    expect(asked('state', 'GET')).toEqual([]);
  });

  it('goes to the fresh game a change answers with, remembering its view (a full page load)', async () => {
    await ready();
    authoring.showTab('board');
    void authoring.goTo(3);
    await settle();
    const request = await answer('goto', result(draftState({ gameId: '9' }), { gameId: '9', rebuilt: true }));
    expect(form(request, 'step')).toBe('3');
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    expect(authoring.gone()).toBe(true);
    expect(guide.leaving()).toBe(true);
    expect(parseJson(sessionStorage.getItem(AUTHOR_UI_KEY)!)).toMatchObject({ tab: 'board' });
    // (nothing more is asked once gone)
    authoring.refresh();
    expect(asked('state', 'GET')).toEqual([]);
  });

  it('takes the state of a change the game in hand could take, and asks the game for what it was sent', async () => {
    await ready();
    void authoring.settings({ askOpponent: false });
    await answer('settings', result(draftState({ askOpponent: false }), { events: true }));
    expect(authoring.draft()?.askOpponent).toBe(false);
    vi.advanceTimersByTime(100);
    expect(backend.match((r) => r.url === GAME && r.method === 'POST')).toHaveLength(1);
  });

  it("says what went wrong in the server's words, until something goes right", async () => {
    await ready();
    void authoring.goTo(2, 1);
    await settle();
    await answer('goto', { error: 'No such step.' }, 400);
    expect(authoring.problem()).toBe('No such step.');
    authoring.refresh();
    await answerState(draftState({ dirty: true }));
    expect(authoring.problem()).toBe('No such step.');
    void authoring.settings({ recording: true });
    await answer('settings', result(draftState()));
    expect(authoring.problem()).toBeNull();
    authoring.refresh();
    asked('state', 'GET')[0]!.flush('', { status: 403, statusText: 'No' });
    await settle();
    expect(authoring.problem()).toBe('Writing scenarios is not open to this account.');
    authoring.dismissProblem();
    expect(authoring.problem()).toBeNull();
  });

  it("sends the Step tab's form first, and goes nowhere when the server refused it", async () => {
    await ready();
    let flushed: StepFlushResult = { went: false, note: 'Step 1 is not sent.' };
    const flush = vi.fn((regardless: boolean) => Promise.resolve(regardless ? flushed : flushed));
    const reset = vi.fn();
    const detach = authoring.attachStepTab({ flush, engaged: () => false, reset });
    for (const go of [
      () => authoring.goTo(2),
      () => authoring.stepHere(),
      () => authoring.stepBefore(),
      () => authoring.retake(),
      () => authoring.startTest(1),
      () => authoring.insertStep(0, { pause: true }),
    ]) {
      await go();
    }
    expect(flush).toHaveBeenCalledTimes(6);
    expect(backend.match((r) => r.url.startsWith(AUTHOR))).toEqual([]);
    flushed = { went: true };
    void authoring.stepHere();
    await settle();
    await answer('here', result(draftState()));
    expect(reset).toHaveBeenCalled();
    expect(authoring.ui().tab).toBe('step');
    detach();
    void authoring.goTo(2);
    await settle();
    expect(asked('goto')).toHaveLength(1);
  });

  it('writes a step before the one in hand, takes an answer again, tests, and goes back to writing', async () => {
    await ready();
    for (const [call, what] of [
      [() => authoring.stepBefore(), 'before'],
      [() => authoring.retake(), 'retake'],
      [() => authoring.startTest(2), 'test'],
      [() => authoring.resume(), 'resume'],
    ] as const) {
      void call();
      await settle();
      await answer(what, result(draftState()));
    }
    // (any call, made as the panel makes them)
    void authoring.send(TestBed.inject(AuthoringApi).check());
    await settle();
    await answer('check', { passed: true, report: null, warnings: [] });
    void authoring.send(TestBed.inject(AuthoringApi).source(), false);
    asked('source', 'GET')[0]!.flush('{"source":"x"}');
    await settle();
    expect(authoring.busy()).toBe(0);
  });

  describe('changing which steps there are', () => {
    it('puts a step in, turns to it in the Step tab, and plays to it again where the game has been', async () => {
      await ready(draftState({ position: position({ step: 2 }) }));
      void authoring.insertStep(1, { pause: true });
      await settle();
      const request = await answer('edit', result(draftState()));
      expect(parseJson(form(request, 'patch')!)).toEqual({
        steps: [{ op: 'insert', before: 's2', step: { pause: true } }],
      });
      expect(form(request, 'goto')).toBe('2');
      expect(authoring.ui()).toMatchObject({ tab: 'step', selected: 2, hold: false });
      void authoring.insertStep(3, { when: 'freeps, action' }, true);
      await settle();
      const live = await answer('edit', result(draftState()));
      expect(form(live, 'goto')).toBeNull();
      expect((parseJson(form(live, 'patch')!) as { steps: unknown[] }).steps[0]).toMatchObject({
        before: null,
        awaiting: true,
      });
    });

    it('puts the selection back when the server refuses the change', async () => {
      await ready();
      authoring.select(1);
      void authoring.changeSteps([{ op: 'delete', uid: 's3' }], 3, 3);
      await settle();
      expect(authoring.ui()).toMatchObject({ selected: 3, hold: true });
      await answer('edit', { error: 'No.' }, 400);
      expect(authoring.ui()).toMatchObject({ selected: 2, selectedUid: 's2', sub: null, hold: false });
    });

    it("changes a block's steps: in one the game has gone through, at the block; in the one it is in, live", async () => {
      await ready(draftState({ position: position({ step: 3 }) }));
      void authoring.changeBlock([{ op: 'delete', uid: 's3k1' }], 2, 0);
      await settle();
      expect(authoring.ui()).toMatchObject({ selected: 3, subAt: 0, hold: true });
      const live = await answer('edit', result(draftState({ position: position({ step: 3 }), dirty: true })));
      expect(form(live, 'goto')).toBeNull();
      expect(authoring.ui()).toMatchObject({ selected: 3, sub: 's3k1', hold: false });
      authoring.setState(draftState({ position: position({ step: 4 }) }));
      void authoring.changeBlock([{ op: 'delete', uid: 's3k1' }], 2, null);
      await settle();
      expect(form(await answer('edit', { error: 'No.' }, 400), 'goto')).toBe('3');
      expect(authoring.ui().hold).toBe(false);
    });

    it('rewrites from where the game is, once the author has said so', async () => {
      await ready(draftState({ position: position({ step: 2 }) }));
      notifier.answers.push(false);
      await authoring.rewriteFromHere();
      expect(notifier.dialogs).toEqual([
        'confirm: Take steps 2 to 3 out of the script, messages and all, and write them again by playing?',
      ]);
      expect(asked('edit')).toEqual([]);
      notifier.answers.push(true);
      void authoring.rewriteFromHere();
      await settle();
      const request = await answer('edit', result(draftState()));
      expect(parseJson(form(request, 'patch')!)).toEqual({
        steps: [
          { op: 'delete', uid: 's3' },
          { op: 'delete', uid: 's2' },
        ],
      });
      expect(form(request, 'goto')).toBe('2');
      // (past the end there is nothing to rewrite)
      authoring.setState(draftState({ position: position({ step: 4, frontier: true }) }));
      await authoring.rewriteFromHere();
      expect(notifier.dialogs).toHaveLength(2);
    });

    it('does nothing to the steps with no draft', async () => {
      open();
      expect(await authoring.changeSteps([], 1, null)).toBe(false);
      expect(await authoring.changeBlock([], 0, null)).toBe(false);
      await authoring.insertStep(0, {});
      await authoring.rewriteFromHere();
      authoring.select(0);
      expect(asked('edit')).toEqual([]);
    });
  });

  describe('the scenario guide, for a draft', () => {
    it("starts again by the script playing itself: to the start, or the last checkpoint at or before the game's step", async () => {
      const steps = [{}, { checkpoint: true }, {}];
      await ready(draftState({ draft: { id: 'x', steps }, position: position({ step: 3 }) }));
      guide.restart(true);
      await settle();
      expect(form(await answer('goto', result(draftState())), 'step')).toBe('2');
      guide.restart();
      await settle();
      expect(form(await answer('goto', result(draftState())), 'step')).toBe('1');
      expect(backend.match((r) => r.url.includes('/scenarios/start'))).toEqual([]);
    });

    it('starts a test again from where it began', async () => {
      await ready(draftState({ mode: 'test', testFrom: 2 }), { author: false, draftTest: true });
      guide.restart();
      await settle();
      expect(form(await answer('test', result(draftState())), 'step')).toBe('2');
      authoring.setState(draftState({ mode: 'test' }));
      guide.restart();
      await settle();
      expect(form(await answer('test', result(draftState())), 'step')).toBe('1');
    });

    it("fixes the draft's own settings for its game", async () => {
      await ready(
        draftState({ draft: { id: 'x', kind: 'puzzle', options: { autoAccept: false, animationSpeed: 1 } } }),
      );
      expect(TestBed.inject(GameSettings).fixed()).toEqual({
        autoAccept: false,
        autoDismissDeckReveal: false,
        animationSpeed: 1,
      });
    });

    it('lets nothing on the table answer the decision while the author picks something there', async () => {
      await ready();
      authoring.startPicking('Click what the message is about.');
      expect(authoring.told()).toBe('Click what the message is about.');
      expect(guide.picking()).toBe(true);
      expect(guide.allows({ kind: 'card', cardId: '7' })).toBe(false);
      authoring.stopPicking();
      expect(authoring.told()).toBeNull();
      expect(guide.allows({ kind: 'card', cardId: '7' })).toBe(true);
    });

    it('leaves a draft being written as it is (nothing conceded), asking about unsaved changes first', async () => {
      await ready();
      await guide.leave();
      expect(notifier.dialogs).toEqual([]);
      expect(hall.go).toHaveBeenCalledWith('');
      expect(backend.match((r) => r.url === `${GAME}/concede`)).toEqual([]);
      authoring.setState(draftState({ dirty: true }));
      expect(authoring.unsaved()).toBe(true);
      guide.backToHall('#account');
      expect(hall.go).toHaveBeenCalledTimes(1);
      expect(authoring.leaveQuestion()).toEqual({ busy: false, said: null });
      authoring.stay();
      expect(authoring.leaveQuestion()).toBeNull();
    });

    it('concedes a test game when it is left, as a player would', async () => {
      await ready(draftState({ mode: 'test', dirty: true }), { author: false, draftTest: true });
      expect(authoring.unsaved()).toBe(false);
      notifier.answers.push(true);
      await guide.leave();
      expect(notifier.dialogs).toEqual(['confirm: Leave this scenario? You can start it again from the Game Hall.']);
      expect(backend.match((r) => r.url === `${GAME}/concede`)).toHaveLength(1);
    });

    it('tells the server when the page goes away with a draft open, unless it is going on to the next game', async () => {
      await ready();
      exit.leaving!();
      expect(exit.beacon).toHaveBeenCalledTimes(1);
      authoring.go('9');
      exit.leaving!();
      expect(exit.beacon).toHaveBeenCalledTimes(1);
    });

    it('takes its say away when the page closes', async () => {
      await ready();
      TestBed.resetTestingModule();
      expect(exit.leaving).toBeNull();
    });
  });

  describe('saving and leaving', () => {
    const saved = {
      path: 'tutorials/test.hjson',
      id: 'test',
      playable: true,
      verified: true,
      errors: [],
      warnings: [],
      report: null,
    };

    it('saves, says how it went, and keeps the whole report only when there is more to read', async () => {
      await ready(draftState({ dirty: true }));
      void authoring.save();
      await settle();
      await answer('save', result(draftState(), { saved }));
      expect(authoring.saved()).toEqual({
        good: true,
        why: false,
        stays: false,
        text: 'Saved to tutorials/test.hjson. Scenario validated.',
      });
      expect(authoring.report()).toBeNull();
      authoring.dismissSaved();
      void authoring.save();
      await settle();
      await answer('save', result(draftState(), { saved: { ...saved, warnings: ['w'] } }));
      expect(authoring.report()).toContain('Warnings:\nw');
      // (no longer news once the draft is changed again)
      authoring.setState(draftState({ dirty: true, revision: 2 }));
      expect(authoring.saved()).toBeNull();
      // (a rename's answer says nothing of a save)
      authoring.afterSave(result(draftState()), null);
      expect(authoring.saved()).toBeNull();
    });

    it('asks first where saving would lose comments', async () => {
      await ready(draftState({ commentsLost: 2 }));
      notifier.answers.push(false);
      await authoring.save();
      expect(notifier.dialogs[0]).toContain('The file has 2 lines of comment');
      expect(asked('save')).toEqual([]);
      authoring.setState(draftState({ commentsLost: 1 }));
      notifier.answers.push(true);
      void authoring.save();
      await settle();
      expect(notifier.dialogs[1]).toContain('1 line of comment');
      expect(asked('save')).toHaveLength(1);
    });

    it("saves the rest when the Step tab's step was refused, and says so, keeping the error", async () => {
      await ready(draftState({ dirty: true }));
      authoring.attachStepTab({
        flush: (regardless) => {
          authoring.problem.set('Bad When.');
          return Promise.resolve(regardless ? { went: false, note: 'Step 1 is not in the file.' } : { went: true });
        },
        engaged: () => false,
        reset: () => undefined,
      });
      void authoring.save();
      await settle();
      await answer('save', result(draftState(), { saved }));
      expect(authoring.saved()?.good).toBe(false);
      expect(authoring.saved()?.text).toContain('Step 1 is not in the file.');
      expect(authoring.problem()).toBe('Bad When.');
      expect(authoring.report()).toContain('Step 1 is not in the file.');
    });

    it('saves and goes: the draft is closed, and the page goes back to the hall', async () => {
      await ready(draftState({ dirty: true }));
      authoring.askBeforeLeaving();
      void authoring.saveAndExit();
      await settle();
      expect(authoring.leaveQuestion()).toEqual({ busy: true, said: null });
      await answer('save', result(draftState(), { saved }));
      await answer('close', { ok: true });
      expect(hall.go).toHaveBeenCalledWith();
      expect(authoring.gone()).toBe(true);
      expect(authoring.leaveQuestion()).toBeNull();
      expect(authoring.unsaved()).toBe(false);
    });

    it("stays when the Step tab's step was refused, the rest saved", async () => {
      await ready(draftState({ dirty: true }));
      authoring.attachStepTab({
        flush: () => Promise.resolve({ went: false, note: 'Step 1 is not in the file.' }),
        engaged: () => false,
        reset: () => undefined,
      });
      authoring.askBeforeLeaving();
      void authoring.saveAndExit();
      await settle();
      await answer('save', result(draftState(), { saved }));
      expect(asked('close')).toEqual([]);
      expect(authoring.leaveQuestion()).toEqual({
        busy: false,
        said: 'The rest is saved. Step 1 is not in the file. Fix it and save again, or discard it by leaving with Discard changes.',
      });
    });

    it('says in the question what failed, saving or closing', async () => {
      await ready(draftState({ dirty: true, commentsLost: 1 }));
      notifier.answers.push(false);
      await authoring.saveAndExit();
      expect(asked('save')).toEqual([]);
      notifier.answers.push(true);
      void authoring.saveAndExit();
      await settle();
      await answer('save', { error: 'Disk full.' }, 500);
      expect(authoring.leaveQuestion()).toEqual({ busy: false, said: 'Disk full.' });
      void authoring.discardAndExit();
      await settle();
      asked('close')[0]!.flush('', { status: 502, statusText: 'x' });
      await settle();
      expect(authoring.leaveQuestion()).toEqual({ busy: false, said: 'The server answered 502.' });
      authoring.problem.set(null);
      void authoring.discardAndExit();
      await settle();
      asked('close')[0]!.error(new ProgressEvent('x'), { status: 0 });
      await settle();
      expect(authoring.leaveQuestion()?.said).toBe('The server could not be reached.');
    });

    it('discards: closes the draft and goes', async () => {
      await ready(draftState({ dirty: true }));
      void authoring.discardAndExit();
      await settle();
      await answer('close', { ok: true });
      expect(hall.go).toHaveBeenCalled();
    });
  });

  describe('the selection and the tabs', () => {
    it("selects a step, or one of a block's, and turns to the Step tab", async () => {
      await ready();
      authoring.select(2, 's3k2');
      expect(authoring.ui()).toMatchObject({ tab: 'step', selected: 3, selectedUid: 's3', sub: 's3k2' });
    });

    it("follows the game, unless told not to; turned on again, it turns to the game's step", async () => {
      await ready();
      authoring.setFollow(false);
      authoring.select(2);
      authoring.setState(draftState({ position: position({ step: 2 }) }));
      expect(authoring.ui().selected).toBe(3);
      authoring.setFollow(true);
      expect(authoring.ui()).toMatchObject({ follow: true, selected: 2, jump: false });
    });

    it('waits for the author to be done in the Step tab before following the game', async () => {
      await ready();
      let engaged = true;
      authoring.attachStepTab({
        flush: () => Promise.resolve({ went: true }),
        engaged: () => engaged,
        reset: () => undefined,
      });
      authoring.showTab('step');
      authoring.setState(draftState({ position: position({ step: 2 }) }));
      expect(authoring.ui().selected).toBe(1);
      authoring.catchUp();
      expect(authoring.ui().selected).toBe(1);
      engaged = false;
      authoring.catchUp();
      expect(authoring.ui().selected).toBe(2);
      // (picking is being busy too)
      authoring.startPicking('Pick.');
      authoring.setState(draftState({ position: position({ step: 3 }) }));
      expect(authoring.ui().selected).toBe(2);
      authoring.stopPicking();
      expect(authoring.ui().selected).toBe(3);
      // (nothing due: catching up changes nothing)
      authoring.catchUp();
      expect(authoring.ui().selected).toBe(3);
    });

    it('calls for the Opponent tab when the opponent is newly held for the author, until it is shown', async () => {
      await ready();
      expect(authoring.wanted()).toBe(false);
      const held = draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow', held: true }) } });
      authoring.setState(held);
      expect(authoring.wanted()).toBe(true);
      expect([...authoring.attention()]).toEqual(['opponent']);
      authoring.showTab('opponent');
      expect([...authoring.attention()]).toEqual([]);
      authoring.showTab('timeline');
      authoring.setState({ ...held, revision: 2 });
      expect([...authoring.attention()]).toEqual([]);
    });

    it('remembers what the tabs ask it to', async () => {
      await ready();
      authoring.remember({ where: 'setup' });
      expect(authoring.ui().where).toBe('setup');
    });

    it('says what went wrong on the last page, carried across', async () => {
      sessionStorage.setItem(AUTHOR_UI_KEY, JSON.stringify({ carried: 'Some changes were not made.' }));
      TestBed.resetTestingModule();
      setUp();
      open();
      expect(authoring.problem()).toBe('Some changes were not made.');
      expect(authoring.ui().carried).toBeNull();
    });
  });

  describe('the notices', () => {
    it("puts away the server's notice, the script's problems so far, and the parts left out of the file", async () => {
      await ready(draftState({ notice: 'Stopped at 2.', problems: ['a', 'b'] }));
      authoring.readNotice();
      expect(authoring.noticeRead()).toBe('Stopped at 2.');
      authoring.readProblems();
      expect(authoring.problemsRead()).toBe(2);
      void authoring.seenLeftOut();
      await settle();
      expect(form(await answer('settings', result(draftState())), 'seenLeftOut')).toBe('true');
    });

    it('reads nothing with no draft', () => {
      authoring.readNotice();
      authoring.readProblems();
      expect(authoring.noticeRead()).toBeNull();
      expect(authoring.problemsRead()).toBe(0);
    });
  });

  describe('changes made as several calls', () => {
    it('goes to the fresh game once they all have been made, carrying what went wrong', async () => {
      await ready();
      authoring.holdArrivals();
      void authoring.edit({ top: { cards: {} } });
      await answer('edit', result(draftState({ gameId: '9' }), { gameId: '9', rebuilt: true }));
      expect(cues.go).not.toHaveBeenCalled();
      expect(authoring.releaseArrivals('The rest were not made.')).toBe(true);
      expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
      expect(parseJson(sessionStorage.getItem(AUTHOR_UI_KEY)!)).toMatchObject({ carried: 'The rest were not made.' });
      authoring.holdArrivals();
      expect(authoring.releaseArrivals()).toBe(false);
    });
  });

  it('closes the draft and goes back to the hall, or says why it could not', async () => {
    await ready();
    void authoring.close();
    await settle();
    asked('close')[0]!.flush('{"error":"Not now."}', { status: 400, statusText: 'x' });
    await settle();
    expect(authoring.problem()).toBe('Not now.');
    expect(hall.go).not.toHaveBeenCalled();
    void authoring.close();
    await settle();
    await answer('close', { ok: true });
    expect(hall.go).toHaveBeenCalled();
  });

  it('leaves the room the panel takes to the panel to give (it is the view that docks)', async () => {
    await ready();
    expect(TestBed.inject(ScenarioRoom).reservedRight()).toBe(0);
  });

  it("puts the Step tab's message up, and again whenever its step is; tries a bubble only on its own step", async () => {
    const attach = vi.spyOn(guide, 'attachAuthor');
    await ready();
    const hooks = attach.mock.calls[0]![0];
    const preview = vi.spyOn(guide, 'preview').mockReturnValue(true);
    authoring.focusOn({ step: 2, sub: 0, index: 1 });
    expect(preview).toHaveBeenCalledWith(2, 0, 1);
    preview.mockClear();
    hooks.stepShown!({ step: 1, sub: 0 } as never);
    hooks.stepShown!({ step: 2, sub: 1 } as never);
    expect(preview).not.toHaveBeenCalled();
    hooks.stepShown!({ step: 2, sub: 0 } as never);
    expect(preview).toHaveBeenCalledWith(2, 0, 1);
    authoring.focusOn(null);
    hooks.stepShown!({ step: 2, sub: 0 } as never);
    expect(preview).toHaveBeenCalledTimes(1);
    const tried = vi.spyOn(guide, 'tryPlace').mockReturnValue(true);
    expect(authoring.tryPlace({ step: 2, sub: 0, index: 1 }, { side: 'left' })).toBe(false);
    vi.spyOn(guide, 'step').mockReturnValue({ step: 2, sub: 0 } as never);
    expect(authoring.tryPlace({ step: 2, sub: 0, index: 1 }, { side: 'left' })).toBe(true);
    expect(authoring.tryPlace({ step: 2, sub: 1, index: 1 }, { side: 'left' })).toBe(false);
    expect(authoring.tryPlace({ step: 1, sub: 0, index: 1 }, { gap: '3' })).toBe(false);
    expect(tried).toHaveBeenCalledTimes(1);
  });

  it('has the Step tab keep what it has not sent when the page goes to a fresh game', async () => {
    await ready();
    const carry = vi.fn();
    authoring.attachStepTab({
      flush: () => Promise.resolve({ went: true }),
      engaged: () => false,
      reset: vi.fn(),
      carry,
    });
    authoring.go('9');
    expect(carry).toHaveBeenCalled();
  });
});
