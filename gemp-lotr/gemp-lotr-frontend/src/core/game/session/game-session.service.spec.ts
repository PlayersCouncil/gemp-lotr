import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { GameApi } from '../api/game-api.service';
import { provideGameState } from '../game-state';
import { GameSession, problemFor } from './game-session.service';

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

  const expectPoll = (cn: string) =>
    backend.expectOne(
      (r) => r.url === GAME && r.method === 'POST' && r.body?.get('channelNumber') === cn && !r.body?.has('decisionId'),
    );

  beforeEach(() => {
    sessionStorage.clear();
    setUp();
  });

  afterEach(() => backend.verify());

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
    // The game is over: no more news
    expect(session.model().ended).toBe(true);
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
    expect(session.model().local).toEqual({ assignments: {}, selectedCardIds: [] });
  });

  it('leaving the page stops asking for news', () => {
    session.open('7');
    backend.expectOne((r) => r.url === GAME).flush(state('1', ''));
    const poll = expectPoll('1');
    TestBed.resetTestingModule();
    expect(poll.cancelled).toBe(true);
    backend = { verify: () => undefined } as unknown as HttpTestingController;
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
