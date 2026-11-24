import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { GameSession } from '../session/game-session.service';
import { BANNER_FADE_MS, BANNER_MS, GameUndo, NOTICES_RETRY_MS } from './game-undo.service';

const GAME = '/gemp-lotr-server/game/7';
const P = (me = 'alice') => `<ge type="P" participantId="${me}" allParticipantIds="alice,bob" discardPublic="false"/>`;
const DECISION =
  '<ge type="D" id="1" decisionType="CARD_SELECTION" participantId="alice" text="Choose"><parameter name="cardId" value="4"/></ge>';

const green = { entryCount: 11, label: 'Played Gimli', turn: 0, phase: 'Setup', tier: 'GREEN', reasons: [] };
const red = {
  entryCount: 12,
  label: 'Pressed DONE',
  turn: 0,
  phase: 'Setup',
  tier: 'RED',
  reasons: ['Hidden information revealed'],
  unavailable: null,
};
const policy = { green: true, yellow: true, red: true, asks: true, timeoutSeconds: 60, greenPerPhase: 3 };
const pendingJson = {
  requestId: 4,
  requester: 'bob',
  label: 'Pressed DONE',
  turn: 0,
  phase: 'Setup',
  tier: 'RED',
  reasons: ['Hidden information revealed'],
  secondsLeft: 30,
};

describe('GameUndo', () => {
  let undo: GameUndo;
  let session: GameSession;
  let backend: HttpTestingController;

  const notices = () => backend.expectOne((r) => r.url === `${GAME}/notices`);
  const points = () => backend.expectOne((r) => r.url === `${GAME}/restorePoints` && r.method === 'GET');
  const answer = (points: unknown[], extra: Record<string, unknown> = {}) => ({
    points,
    policy,
    autoDecline: false,
    declining: [],
    pending: null,
    ...extra,
  });
  const notice = (number: number, type: string, fields: Record<string, string>) => ({
    notices: [{ number, type, ...fields }],
    latest: number,
  });

  /** The game opened for `me` (a decision waiting for alice), undo started, its first answers given. */
  function start(me = 'alice', first: unknown[] = [green]): TestRequest {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(`<gameState cn="1">${P(me)}${DECISION}</gameState>`);
    if (me !== 'alice') backend.match((r) => r.url === GAME && r.method === 'POST');
    TestBed.tick();
    undo.start('7');
    undo.start('7');
    const first_ = notices();
    expect(first_.request.body?.get('after')).toBe('-1');
    first_.flush({ notices: [], latest: 3 });
    points().flush(answer(first));
    const poll = notices();
    expect(poll.request.body?.get('after')).toBe('3');
    return poll;
  }

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    undo = TestBed.inject(GameUndo);
    session = TestBed.inject(GameSession);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  it('does nothing before it has started', () => {
    undo.refresh();
    undo.undoLatest();
    undo.openList();
    undo.ask(1);
    undo.reply('approve');
    undo.setAutoDecline(true);
    backend.expectNone(() => true);
  });

  it("starts the notices' poll where the server says, and shows the Undo button for the player's own last choice", () => {
    start();
    expect(undo.policy()).toMatchObject({ green: true, asks: true });
    expect(undo.allowed()).toBe(true);
    expect(undo.latest()?.label).toBe('Played Gimli');
    expect(undo.button()).toEqual({ title: 'Undo: Played Gimli', enabled: true });
  });

  it('has no Undo button for a revert, and an Undo button that is off, with why, when it is unavailable', () => {
    start('alice', [red]);
    expect(undo.button()).toBeNull();
    undo.refresh();
    points().flush(answer([{ ...green, unavailable: 'Only 3 undos a phase' }]));
    expect(undo.button()).toEqual({ title: 'Only 3 undos a phase', enabled: false });
    undo.refresh();
    points().flush({ ...answer([]), policy: { ...policy, green: false, yellow: false, red: false } });
    expect(undo.allowed()).toBe(false);
  });

  it("has no Undo button in a game that allows no taking back (a puzzle's), whatever its last place", () => {
    start();
    undo.refresh();
    points().flush({
      ...answer([{ ...green, unavailable: 'Taking back is off in this game' }]),
      policy: { ...policy, green: false, yellow: false, red: false },
    });
    expect(undo.latest()?.label).toBe('Played Gimli');
    expect(undo.button()).toBeNull();
  });

  it('asks again whenever the game moves; asked while an answer is on its way, once more when it has come', () => {
    start();
    undo.refresh();
    undo.refresh();
    undo.refresh();
    points().flush(answer([red]));
    // One more, for the asks that came meanwhile
    points().error(new ProgressEvent('error'));
    backend.expectNone((r) => r.url === `${GAME}/restorePoints`);
    // The game moves: asked again
    session.answer('4');
    backend
      .expectOne((r) => r.url === GAME && r.body?.has('decisionId'))
      .flush('<update cn="2"><ge type="TP" count="1"/></update>');
    TestBed.tick();
    points().flush(answer([green]));
  });

  it("the Undo button takes the last choice back at once, as the player's own only; off until its answer", () => {
    start();
    undo.undoLatest();
    const sent = backend.expectOne((r) => r.url === `${GAME}/undo`);
    expect(sent.request.body?.get('entryCount')).toBe('11');
    expect(sent.request.body?.get('ownOnly')).toBe('true');
    expect(undo.button()?.enabled).toBe(false);
    undo.undoLatest();
    backend.expectNone((r) => r.url === `${GAME}/undo`);
    sent.flush({ status: 'done', message: null, requestId: null });
    expect(undo.banner()).toBeNull();
  });

  it('says why the Undo button was refused, or could not be sent, and asks again what can be taken back', () => {
    start();
    undo.undoLatest();
    backend
      .expectOne((r) => r.url === `${GAME}/undo`)
      .flush({ status: 'refused', message: 'Your opponent has answered since', requestId: null });
    expect(undo.banner()?.message).toBe('Your opponent has answered since');
    points().flush(answer([red]));
    expect(undo.button()).toBeNull();

    undo.refresh();
    points().flush(answer([green]));
    undo.undoLatest();
    backend.expectOne((r) => r.url === `${GAME}/undo`).flush('', { status: 500, statusText: 'x' });
    expect(undo.banner()?.message).toBe('Undo could not be sent');
    points().flush(answer([green]));
    expect(undo.button()?.enabled).toBe(true);
  });

  it('opens the list fetched afresh, closes it, and asks for a place (not only the own)', () => {
    start();
    undo.openList();
    points().flush(answer([red, green]));
    expect(undo.list()?.points.map((p) => p.entryCount)).toEqual([12, 11]);
    undo.ask(12);
    const asked = backend.expectOne((r) => r.url === `${GAME}/undo`);
    expect(asked.request.body?.get('entryCount')).toBe('12');
    expect(asked.request.body?.get('ownOnly')).toBe('false');
    asked.flush({ status: 'pending', message: null, requestId: 4 });
    expect(undo.list()).toBeNull();

    undo.openList();
    points().flush(answer([red]));
    undo.closeList();
    expect(undo.list()).toBeNull();
  });

  it('says why a place was refused, or could not be asked for, or the list could not be fetched', () => {
    start();
    undo.ask(12);
    backend
      .expectOne((r) => r.url === `${GAME}/undo`)
      .flush({ status: 'refused', message: 'Too far', requestId: null });
    expect(undo.banner()?.message).toBe('Too far');
    undo.ask(12);
    backend.expectOne((r) => r.url === `${GAME}/undo`).flush('', { status: 0, statusText: 'x' });
    expect(undo.banner()?.message).toBe('The request could not be sent');
    undo.openList();
    points().flush('', { status: 500, statusText: 'x' });
    expect(undo.banner()?.message).toBe('The list could not be fetched');
  });

  it("shows the opponent's request: asked, the game stands still, and it counts down", () => {
    const poll = start();
    undo.openList();
    points().flush(answer([red]));
    poll.flush(
      notice(4, 'undoRequested', {
        requestId: '4',
        requester: 'bob',
        label: 'Pressed DONE',
        turn: '0',
        phase: 'Setup',
        tier: 'RED',
        reasons: 'Hidden information revealed',
        seconds: '60',
      }),
    );
    expect(undo.list()).toBeNull();
    expect(undo.pending()).toMatchObject({ requestId: 4, requester: 'bob', tier: 'RED' });
    expect(undo.asked()).toBe(true);
    expect(undo.mine()).toBe(false);
    expect(undo.frozen()).toBe(true);
    expect(undo.button()?.enabled).toBe(false);
    expect(undo.secondsLeft()).toBe(60);
    vi.advanceTimersByTime(3000);
    expect(undo.secondsLeft()).toBe(57);
    expect(notices().request.body?.get('after')).toBe('4');
  });

  it('answers the request; an approval the server refuses is said', () => {
    const poll = start();
    poll.flush(notice(4, 'undoRequested', { requestId: '9', requester: 'bob', tier: 'RED', seconds: '60' }));
    undo.reply('decline');
    const reply = backend.expectOne((r) => r.url === `${GAME}/undoReply`);
    expect(reply.request.body?.get('requestId')).toBe('9');
    expect(reply.request.body?.get('reply')).toBe('decline');
    reply.flush({ status: 'refused', message: 'Not now', requestId: 9 });
    expect(undo.banner()).toBeNull();
    undo.reply('approve');
    backend.expectOne((r) => r.url === `${GAME}/undoReply`).flush({ status: 'refused', message: 'Gone', requestId: 9 });
    expect(undo.banner()?.message).toBe('Gone');
  });

  it("the player's own request may be withdrawn; resolved, it goes, said unless it was done", () => {
    const poll = start();
    poll.flush(notice(4, 'undoRequested', { requestId: '9', requester: 'alice', tier: 'YELLOW', seconds: '60' }));
    expect(undo.mine()).toBe(true);
    expect(undo.asked()).toBe(false);
    notices().flush(
      notice(5, 'undoResolved', { requestId: '9', outcome: 'declined', message: 'bob declined the revert' }),
    );
    expect(undo.pending()).toBeNull();
    expect(undo.frozen()).toBe(false);
    expect(undo.banner()?.message).toBe('bob declined the revert');
    points().flush(answer([red]));
    undo.dismissBanner();
    notices().flush(notice(6, 'undoResolved', { requestId: '9', outcome: 'done', message: 'alice reverted: x' }));
    expect(undo.banner()).toBeNull();
    // The countdown of a request that has gone stops
    vi.advanceTimersByTime(5000);
    expect(undo.secondsLeft()).toBe(60);
  });

  it('the game taken back: says so with its tier, and the game is read again from nothing', () => {
    const poll = start();
    poll.flush(notice(4, 'undoRequested', { requestId: '9', requester: 'bob', tier: 'RED', seconds: '60' }));
    notices().flush(notice(5, 'resync', { reason: 'revert', message: 'bob reverted: Pressed DONE', tier: 'RED' }));
    expect(undo.pending()).toBeNull();
    expect(undo.banner()).toMatchObject({ message: 'bob reverted: Pressed DONE', tier: 'RED', fading: false });
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(`<gameState cn="9">${P()}${DECISION}</gameState>`);
    expect(session.reopened()).toBe(1);
    // ...and what can be taken back is asked again
    TestBed.tick();
    points().flush(answer([]));
  });

  it("the game's end while the player's decision waits: the game's news, once", () => {
    const poll = start();
    poll.flush(notice(4, 'resync', { reason: 'ended' }));
    backend.expectOne((r) => r.url === GAME && r.method === 'POST' && !r.body?.has('decisionId'));
  });

  it('declines every request without being asked, or stops', () => {
    start();
    undo.setAutoDecline(true);
    expect(undo.autoDeclining()).toBe(true);
    expect(backend.expectOne((r) => r.url === `${GAME}/autoDecline`).request.body?.get('on')).toBe('true');
  });

  it('a request waiting since before the page opened is shown', () => {
    start();
    undo.refresh();
    points().flush(answer([green], { pending: pendingJson }));
    expect(undo.pending()).toMatchObject({ requestId: 4, secondsLeft: 30 });
    expect(undo.latest()).toBeNull();
  });

  it('somebody watching is told of a request, but takes nothing back and answers nothing', () => {
    start('carol');
    expect(undo.playing()).toBe(false);
    expect(undo.button()).toBeNull();
    undo.refresh();
    points().flush(answer([green], { pending: pendingJson }));
    expect(undo.pending()).not.toBeNull();
    expect(undo.frozen()).toBe(false);
    expect(undo.asked()).toBe(false);
    expect(undo.mine()).toBe(false);
  });

  it('asks for notices again after a while when the poll fails', () => {
    const poll = start();
    poll.flush('', { status: 502, statusText: 'x' });
    backend.expectNone((r) => r.url === `${GAME}/notices`);
    vi.advanceTimersByTime(NOTICES_RETRY_MS);
    expect(notices().request.body?.get('after')).toBe('3');
  });

  it('the banner stays ten seconds, fades for one, and goes; pressed, it goes at once', () => {
    const poll = start();
    poll.flush(notice(4, 'undoResolved', { outcome: 'cancelled', message: 'Withdrawn' }));
    vi.advanceTimersByTime(BANNER_MS - 1);
    expect(undo.banner()?.fading).toBe(false);
    vi.advanceTimersByTime(1);
    expect(undo.banner()?.fading).toBe(true);
    vi.advanceTimersByTime(BANNER_FADE_MS);
    expect(undo.banner()).toBeNull();
    notices().flush(notice(5, 'undoResolved', { outcome: 'timedOut', message: 'Out of time' }));
    expect(undo.banner()?.message).toBe('Out of time');
    undo.dismissBanner();
    expect(undo.banner()).toBeNull();
    // An empty message says nothing
    notices().flush(notice(6, 'undoResolved', { outcome: 'failed' }));
    expect(undo.banner()).toBeNull();
  });

  it('a newer banner is not ended by the older one', () => {
    const poll = start();
    poll.flush(notice(4, 'undoResolved', { outcome: 'declined', message: 'first' }));
    vi.advanceTimersByTime(BANNER_MS / 2);
    notices().flush(notice(5, 'undoResolved', { outcome: 'declined', message: 'second' }));
    vi.advanceTimersByTime(BANNER_MS / 2 + BANNER_FADE_MS);
    expect(undo.banner()).toMatchObject({ message: 'second', fading: false });
  });

  it('the game event that marks the game as taken back (a replay too) shows the banner', () => {
    session.openReplay('alice$r');
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$r')
      .flush('<gameReplay><ge type="GR" participantId="alice" tier="" message="The game was reverted"/></gameReplay>');
    session.togglePlay();
    vi.advanceTimersByTime(1000);
    TestBed.tick();
    expect(undo.banner()).toMatchObject({ message: 'The game was reverted', tier: null });
  });
});
