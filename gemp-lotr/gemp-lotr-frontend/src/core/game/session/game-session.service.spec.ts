import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { GameApi } from '../api/game-api.service';
import { provideGameState } from '../game-state';
import { GameSession, POLL_DELAY_MS, problemFor } from './game-session.service';

const GAME = '/gemp-lotr-server/game/7';
const decision = (
  type = 'ASSIGN_MINIONS',
  params = '<parameter name="freeCharacters" value="10"/><parameter name="minions" value="20"/>',
) => `<ge type="D" id="1" decisionType="${type}" participantId="alice" text="Do it">${params}</ge>`;
const state = (cn: string, events: string) => `<gameState cn="${cn}">${events}</gameState>`;
const update = (cn: string, events: string) =>
  `<update cn="${cn}">${events}<clocks><clock participantId="alice">100</clock></clocks></update>`;

describe('GameSession', () => {
  let session: GameSession;
  let backend: HttpTestingController;

  function setUp() {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    session = TestBed.inject(GameSession);
    backend = TestBed.inject(HttpTestingController);
  }

  /** The page asks for news a moment after an answer (POLL_DELAY_MS). */
  const expectPoll = (cn: string) => {
    vi.advanceTimersByTime(POLL_DELAY_MS);
    return backend.expectOne(
      (r) => r.url === GAME && r.method === 'POST' && r.body?.get('channelNumber') === cn && !r.body?.has('decisionId'),
    );
  };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    setUp();
  });

  afterEach(() => {
    vi.advanceTimersByTime(POLL_DELAY_MS);
    // A question for news may be open (the page asks while nothing waits for the player, also after the end)
    backend.match((r) => r.url === GAME && r.method === 'POST' && !r.body?.has('decisionId'));
    backend.verify();
    vi.useRealTimers();
  });

  it('opens the game, and asks for news while no decision waits for the player', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(state('1', '<ge type="TP" count="2"/>'));
    expect(session.model().twilight).toBe(2);
    expectPoll('1').flush(update('2', '<ge type="TP" count="3"/>'));
    expect(session.model()).toMatchObject({ twilight: 3, clocks: { alice: 100 } });
    expectPoll('2').flush(update('3', decision()));
    expect(session.model().decision?.decision.type).toBe('ASSIGN_MINIONS');
    // A decision waits: no more news until it is answered
  });

  it("answers the decision: sent with the channel, and what happened next is the answer's", () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    session.answer('10 20');
    expect(session.model().decision).toBeNull();
    const answer = backend.expectOne((r) => r.url === GAME && r.method === 'POST');
    expect(answer.request.body?.get('channelNumber')).toBe('1');
    expect(answer.request.body?.get('decisionId')).toBe('1');
    expect(answer.request.body?.get('decisionValue')).toBe('10 20');
    answer.flush(update('2', '<ge type="GPC" phase="Skirmish"/>'));
    expect(session.model().phase).toBe('Skirmish');
    expectPoll('2').flush(update('3', '<ge type="EG"/>'));
    expect(session.model().ended).toBe(true);
    // The game is over, and the page still asks for news, as the legacy page did (the server sends the draw deck then)
    expectPoll('3').flush(
      update('4', '<ge type="PCIP" cardId="9" blueprintId="1_1" participantId="alice" zone="DECK"/>'),
    );
    expect(session.model().cards['9']?.zone).toBe('DECK');
    expectPoll('4').flush(update('5', decision()));
  });

  it('answers nothing when no decision waits', () => {
    session.answer('x');
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', '<ge type="EG"/>'));
    session.answer('x');
  });

  it('after conceding with a decision waiting, asks once for news, to learn the game ended', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    session.concede();
    backend.expectOne((r) => r.url === `${GAME}/concede`).flush('');
    expectPoll('1').flush(update('2', '<ge type="EG"/>'));
    expect(session.model()).toMatchObject({ ended: true, decision: null });
  });

  it('while it is asking for news already, conceding asks no more', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    const poll = expectPoll('1');
    session.concede();
    backend.expectOne((r) => r.url === `${GAME}/concede`).flush('');
    poll.flush(update('2', '<ge type="EG"/>'));
  });

  it('asks the opponent to cancel', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    session.cancel();
    backend.expectOne((r) => r.url === `${GAME}/cancel` && r.method === 'POST').flush('');
  });

  it('does nothing about a game it has not opened', () => {
    session.concede();
    session.cancel();
    session.cardInfo('5', () => {
      throw new Error('no game');
    });
  });

  it("shows a card's information as the server writes it, or nothing when it fails", () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    const shown: string[] = [];
    session.cardInfo('5', (html) => shown.push(html));
    const info = backend.expectOne((r) => r.url === `${GAME}/cardInfo`);
    expect(info.request.params.get('cardId')).toBe('5');
    info.flush('<b>Strength +1</b>');
    session.cardInfo('6', (html) => shown.push(html));
    backend.expectOne((r) => r.url === `${GAME}/cardInfo`).flush('', { status: 500, statusText: 'x' });
    expect(shown).toEqual(['<b>Strength +1</b>', '']);
  });

  it("stops at a failed call with the legacy page's message", () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush('', { status: 410, statusText: 'Gone' });
    expect(session.model().started).toBe(false);
    expect(session.problem()).toEqual(problemFor(410));
  });

  it('a failed poll, answer, concede or cancel stops the game too', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    expectPoll('1').flush('', { status: 409, statusText: 'x' });
    expect(session.problem()?.title).toBe('Concurrent access error');

    for (const failing of [
      () => {
        session.open('7');
        backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
        session.answer('x');
        backend.expectOne((r) => r.url === GAME).flush('', { status: 500, statusText: 'x' });
      },
      () => {
        session.concede();
        backend.expectOne((r) => r.url === `${GAME}/concede`).flush('', { status: 401, statusText: 'x' });
      },
      () => {
        session.cancel();
        backend.expectOne((r) => r.url === `${GAME}/cancel`).flush('', { status: 403, statusText: 'x' });
      },
    ]) {
      session.problem.set(null);
      failing();
      expect(session.problem()).not.toBeNull();
    }
  });

  it('keeps the choices not sent yet, and brings them back after a reload', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    session.assign('20', '10');
    session.keepView({ tab: 'settings' });
    expect(session.model().local.assignments).toEqual({ '20': '10' });

    // The page reloads: a new session, the server sends the whole game and the same decision again
    TestBed.resetTestingModule();
    setUp();
    expect(session.savedView('7')).toEqual({ tab: 'settings' });
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('5', '<ge type="M" message="Welcome back"/>' + decision()));
    expect(session.model().local.assignments).toEqual({ '20': '10' });
  });

  it('changes the choices: unassign, select, clear', () => {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME)
      .flush(state('1', decision('CARD_SELECTION', '<parameter name="cardId" value="4"/>')));
    session.select('4');
    expect(session.model().local.selectedCardIds).toEqual(['4']);
    session.clearSelection();
    session.assign('20', '10');
    session.unassign('20');
    expect(session.model().local).toEqual({ assignments: {}, selectedCardIds: [], markedCardIds: [], minion: null });
  });

  it('leaving the page stops asking for news', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    const poll = expectPoll('1');
    TestBed.resetTestingModule();
    expect(poll.cancelled).toBe(true);
    backend = { verify: () => undefined, match: () => [] } as unknown as HttpTestingController;
  });

  it('plays a replay event by event while it plays, each held as long as its animation, and stops at its end', () => {
    session.openReplay('alice$abc');
    expect(session.replaying()).toBe(true);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$abc')
      .flush(
        '<gameReplay><ge type="TP" count="1"/>' +
          decision('CARD_SELECTION', '<parameter name="cardId" value="4"/>') +
          '<ge type="TP" count="2"/></gameReplay>',
      );
    expect(session.replayDone()).toBe(false);
    session.togglePlay();
    expect(session.replayPlaying()).toBe(true);
    vi.advanceTimersByTime(60_000);
    // The decision asked on the way was put away before the next event (nothing is answered in a replay)
    expect(session.model()).toMatchObject({ twilight: 2, decision: null });
    expect(session.replayDone()).toBe(true);
    expect(session.busy()).toBe(false);

    // Paused, it waits; played again at its end, nothing more happens
    session.togglePlay();
    expect(session.replayPlaying()).toBe(false);
    session.togglePlay();
    vi.advanceTimersByTime(1000);
    expect(session.model().twilight).toBe(2);
  });

  it('a replay with nothing in it is done at once; its speed halves and doubles within limits', () => {
    session.togglePlay();
    expect(session.replayPlaying()).toBe(false);
    session.openReplay('x');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/replay/x').flush('<gameReplay></gameReplay>');
    expect(session.replayDone()).toBe(true);
    const start = session.speed();
    session.slower();
    expect(session.speed()).toBe(Math.min(16, start * 2));
    for (let i = 0; i < 10; i++) session.slower();
    expect(session.speed()).toBe(16);
    for (let i = 0; i < 20; i++) session.faster();
    expect(session.speed()).toBe(0.0625);
  });

  it('a replay that fails to come shows the problem', () => {
    session.openReplay('gone');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/replay/gone').flush('', { status: 404, statusText: 'x' });
    expect(session.problem()).not.toBeNull();
  });

  it('read again from nothing after a revert: drops the choices and what is on its way, and keeps the chat box', () => {
    session.open('7');
    backend
      .expectOne((r) => r.url === GAME)
      .flush(state('1', '<ge type="M" message="one"/><ge type="M" message="two"/>' + decision()));
    session.assign('20', '10');
    expect(session.model().local.assignments).toEqual({ '20': '10' });
    session.reopen('alice reverted: Played Gimli');
    expect(session.reopened()).toBe(0);
    backend
      .expectOne((r) => r.url === GAME && r.method === 'GET')
      .flush(
        state(
          '5',
          '<ge type="M" message="one"/><ge type="M" message="alice reverted: Played Gimli"/><ge type="TP" count="4"/>' +
            decision(),
        ),
      );
    expect(session.model().messages.map((m) => m.text)).toEqual(['one', 'two', 'alice reverted: Played Gimli']);
    expect(session.model()).toMatchObject({ twilight: 4, local: { assignments: {} } });
    expect(session.reopened()).toBe(1);
    // The new channel is the one answered on
    session.answer('10 20');
    expect(
      backend.expectOne((r) => r.method === 'POST' && r.body?.has('decisionId')).request.body?.get('channelNumber'),
    ).toBe('5');
  });

  it('after a revert, an answer to a request sent before it is dropped, a refusal of the old channel too', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', '<ge type="TP" count="1"/>'));
    const old = expectPoll('1');
    // The old channel refused just before the notice came: that was the revert
    session.reopen(null);
    const again = backend.expectOne((r) => r.url === GAME && r.method === 'GET');
    old.flush('', { status: 409, statusText: 'Conflict' });
    expect(session.problem()).toBeNull();
    again.flush(state('2', '<ge type="TP" count="3"/>' + decision()));
    expect(session.model().twilight).toBe(3);
  });

  it('a refusal of the old channel that came before the notice is no problem once the game is read again', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    expectPoll('1').flush('', { status: 409, statusText: 'Conflict' });
    expect(session.problem()?.status).toBe(409);
    session.reopen(null);
    expect(session.problem()).toBeNull();
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(state('2', ''));
    // Nothing waits for the player: the page asks for news on the new channel
    expectPoll('2').flush(update('3', decision()));
  });

  it('an animation running when the game is read again ends nothing of the new game', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    expectPoll('1').flush(
      update('2', '<ge type="PCIP" cardId="5" blueprintId="1_2" participantId="bob" zone="SHADOW_CHARACTERS"/>'),
    );
    expect(session.busy()).toBe(true);
    session.reopen(null);
    expect(session.busy()).toBe(false);
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(state('3', decision()));
    vi.advanceTimersByTime(60_000);
    expect(session.animation()).toBeNull();
    expect(session.model().cards['5']).toBeUndefined();
  });

  it('is read again only in a game: not in a replay, nor before a game opened', () => {
    session.reopen(null);
    session.openReplay('x');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/replay/x').flush('<gameReplay></gameReplay>');
    session.reopen(null);
    expect(session.reopened()).toBe(0);
  });

  it('counts the answers that brought events, and says when the game was taken back (GR)', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', '<ge type="TP" count="1"/>'));
    expect(session.moved()).toBe(1);
    expectPoll('1').flush(update('2', ''));
    expect(session.moved()).toBe(1);
    expectPoll('2').flush(
      update('3', '<ge type="GR" participantId="bob" tier="YELLOW" message="bob reverted: x"/>' + decision()),
    );
    expect(session.moved()).toBe(2);
    expect(session.reverted()).toEqual({ message: 'bob reverted: x', tier: 'YELLOW', count: 1 });
  });

  it('asks for news once over a waiting decision (the game ended meanwhile), and starts no asking of its own', () => {
    session.pollOnce();
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', decision()));
    session.pollOnce();
    session.pollOnce();
    // Something else happened, not the end: the decision still waits, and nothing more is asked
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST' && !r.body?.has('decisionId'))
      .flush(update('2', '<ge type="M" message="hm"/>'));
    vi.advanceTimersByTime(POLL_DELAY_MS);
    backend.expectNone((r) => r.url === GAME && r.method === 'POST');
    session.pollOnce();
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST' && !r.body?.has('decisionId'))
      .flush(update('3', '<ge type="EG"/>'));
    expect(session.model()).toMatchObject({ ended: true, decision: null });
    // With no decision waiting the page asks for news as usual (afterEach)
  });

  it('asks for nothing once when no decision waits: the usual asking is going on', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    const poll = expectPoll('1');
    session.pollOnce();
    backend.expectNone((r) => r.url === GAME && r.method === 'POST' && r !== poll.request);
  });

  it('a replay of a game taken back lays the table out again from nothing, at once, up to its GR', () => {
    session.openReplay('alice$r');
    const P = '<ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>';
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$r')
      .flush(
        `<gameReplay>${P}<ge type="M" message="before"/>` +
          '<ge type="PCIP" cardId="1" blueprintId="1_2" participantId="alice" zone="FREE_CHARACTERS"/>' +
          `${P}<ge type="M" message="again"/>` +
          '<ge type="PCIP" cardId="2" blueprintId="1_2" participantId="alice" zone="FREE_CHARACTERS"/>' +
          '<ge type="GR" participantId="alice" tier="GREEN" message="alice undid: x"/><ge type="TP" count="2"/>' +
          '</gameReplay>',
      );
    session.togglePlay();
    vi.advanceTimersByTime(60_000);
    expect(Object.keys(session.model().cards)).toEqual(['2']);
    expect(session.model().messages.map((m) => m.text)).toEqual(['again']);
    expect(session.model().twilight).toBe(2);
    expect(session.reverted()?.message).toBe('alice undid: x');
  });

  it("the legacy page's messages for each failure", () => {
    expect(problemFor(0)).toMatchObject({ title: 'Server connection error', actions: ['reload'] });
    expect(problemFor(401)).toMatchObject({ title: 'Authentication error', actions: ['mainPage'] });
    expect(problemFor(403)).toMatchObject({ title: 'Game access forbidden', actions: ['hall'] });
    expect(problemFor(500)).toMatchObject({ title: null, actions: [] });
    expect(problemFor(500).lines[0]).toContain('(500)');
  });
});

describe('GameApi', () => {
  it('reads a replay, and fails on anything else', async () => {
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }, GameApi],
    });
    const api = TestBed.inject(GameApi);
    const backend = TestBed.inject(HttpTestingController);
    const events: unknown[] = [];
    api.replay('alice$abc').subscribe((e) => events.push(e));
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/replay/alice$abc')
      .flush('<gameReplay><ge type="ES"/></gameReplay>');
    expect(events).toEqual([[{ type: 'ES' }]]);
    let failed: unknown;
    api.replay('x').subscribe({ error: (e: unknown) => (failed = e) });
    backend.expectOne((r) => r.url === '/gemp-lotr-server/replay/x').flush('<update/>');
    api.open('7').subscribe({ error: (e: unknown) => (failed = failed ?? e) });
    backend.expectOne((r) => r.url === GAME).flush('<error/>');
    expect(failed).toBeDefined();
    backend.verify();
  });
});
