import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PageCues } from '../../platform/page-cues';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { GameSession, POLL_DELAY_MS } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { DECISION_TITLE, GAME_TITLE, GameAttention, soundsOnArrival, TITLE_FLASH_MS } from './game-attention.service';
import { GameClocks } from './game-clocks.service';

const GAME = '/gemp-lotr-server/game/7';
const PLAYERS = (who = 'alice') =>
  `<ge type="P" participantId="${who}" allParticipantIds="alice,bob" discardPublic="false"/>`;
const DECISION = (type = 'CARD_SELECTION') =>
  `<ge type="D" id="1" decisionType="${type}" participantId="alice" text="x"><parameter name="cardId" value="1"/></ge>`;
const clocks = (alice: number, elapsed: number, limit: number) =>
  `<clocks><clock participantId="alice">${alice}</clock><clock participantId="bob">900</clock>` +
  `<clock participantId="decisionClock">${elapsed}</clock><clock participantId="decisionLimit">${limit}</clock></clocks>`;

describe('GameClocks and GameAttention', () => {
  let session: GameSession;
  let clocksService: GameClocks;
  let backend: HttpTestingController;
  let cues: { play: ReturnType<typeof vi.fn>; setTitle: ReturnType<typeof vi.fn>; away: () => boolean };
  let away = false;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    away = false;
    cues = { play: vi.fn(), setTitle: vi.fn(), away: () => away };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: PageCues, useValue: cues },
        provideGameState(),
      ],
    });
    session = TestBed.inject(GameSession);
    clocksService = TestBed.inject(GameClocks);
    TestBed.inject(GameAttention);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.match(() => true);
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  function open(body: string) {
    session.open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(`<gameState cn="1">${body}</gameState>`);
    TestBed.tick();
  }

  const tick = (seconds: number) => {
    for (let i = 0; i < seconds; i++) {
      vi.advanceTimersByTime(1000);
      TestBed.tick();
    }
  };

  it("counts the player's bank and the decision's time each second while a decision waits for them", () => {
    open(PLAYERS() + DECISION() + clocks(700, 5, 360));
    expect(clocksService.view()).toMatchObject({
      banks: { alice: 700, bob: 900 },
      decision: { elapsed: 5, limit: 360 },
    });
    tick(3);
    expect(clocksService.view()).toMatchObject({ banks: { alice: 697, bob: 900 }, decision: { elapsed: 8 } });
    expect(clocksService.view().ownBand).toBeNull();
    // Answered: the count stops, the server's next values replace it
    session.answer('1');
    tick(2);
    expect(clocksService.view().banks['alice']).toBe(697);
    backend
      .expectOne((r) => r.url === GAME && r.method === 'POST')
      .flush(`<update cn="1">${clocks(690, 0, 360)}</update>`);
    TestBed.tick();
    expect(clocksService.view()).toMatchObject({ banks: { alice: 690 }, decision: { elapsed: 0 } });
  });

  it('pulses on whole minutes and sounds as the alert rules say, unless muted', () => {
    open(PLAYERS() + DECISION() + clocks(601, 0, 0));
    tick(1);
    expect(clocksService.bankPulse()).toBe(1);
    expect(cues.play).not.toHaveBeenCalled();
    // Under 10 minutes: orange
    expect(clocksService.view().ownBand).toBeNull();
    tick(1);
    expect(clocksService.view().ownBand).toBe('warn');
    tick(29);
    expect(cues.play).toHaveBeenCalledWith('timerAlert');
    TestBed.inject(GameSettings).setTimerAudioMuted(true);
    cues.play.mockClear();
    tick(30);
    expect(cues.play).not.toHaveBeenCalled();
    expect(clocksService.bankPulse()).toBe(3);
  });

  it("calls when a decision's time runs low, and marks a clock that ran out", () => {
    open(PLAYERS() + DECISION() + clocks(500, 298, 360));
    tick(2);
    expect(clocksService.decisionPulse()).toBe(1);
    expect(cues.play).toHaveBeenCalledWith('timerAlert');
    expect(clocksService.view().decisionBand).toBe('warn');
    // A second past its limit: it ran out
    tick(61);
    expect(clocksService.view()).toMatchObject({ decisionExpired: true, decisionBand: null });
    // It stopped
    const at = clocksService.view().decision?.elapsed;
    tick(3);
    expect(clocksService.view().decision?.elapsed).toBe(at);
  });

  it('marks a bank that ran out, when it does here or when the game ends', () => {
    open(PLAYERS() + DECISION() + clocks(1, 0, 0));
    tick(2);
    expect(clocksService.view().expired).toEqual(['alice']);
  });

  it('gives a spectator no count and no cues', () => {
    open(PLAYERS('carol') + clocks(700, 0, 0));
    tick(2);
    expect(clocksService.view()).toMatchObject({ banks: { alice: 700 }, ownBand: null });
  });

  it("flashes the tab's title while a decision waits, until it is answered", () => {
    open(PLAYERS());
    expect(cues.setTitle).not.toHaveBeenCalled();
    vi.advanceTimersByTime(POLL_DELAY_MS);
    backend.expectOne((r) => r.url === GAME && r.method === 'POST').flush(`<update cn="1">${DECISION()}</update>`);
    TestBed.tick();
    vi.advanceTimersByTime(TITLE_FLASH_MS);
    expect(cues.setTitle).toHaveBeenLastCalledWith(DECISION_TITLE);
    vi.advanceTimersByTime(TITLE_FLASH_MS);
    expect(cues.setTitle).toHaveBeenLastCalledWith(GAME_TITLE);
    vi.advanceTimersByTime(TITLE_FLASH_MS);
    expect(cues.setTitle).toHaveBeenLastCalledWith(DECISION_TITLE);
    session.answer('1');
    TestBed.tick();
    expect(cues.setTitle).toHaveBeenLastCalledWith(GAME_TITLE);
    vi.advanceTimersByTime(TITLE_FLASH_MS * 3);
    expect(cues.setTitle).toHaveBeenLastCalledWith(GAME_TITLE);
  });

  it("flashes nothing while a scripted game's table is changed by hand: nobody is asked anything then", () => {
    const info = JSON.stringify({ type: 'info', id: 'sandbox', kind: 'sandbox', mode: 'edit', level: 'sandbox' });
    open(PLAYERS() + `<ge type="S" scenario="${info.replace(/"/g, '&quot;')}"/>` + DECISION());
    vi.advanceTimersByTime(TITLE_FLASH_MS * 3);
    expect(cues.setTitle).not.toHaveBeenCalledWith(DECISION_TITLE);
  });

  it('flashes nothing on a table flipped onto a seat that plays itself: the page only watches it', () => {
    const info = JSON.stringify({
      type: 'info',
      id: 'sandbox',
      kind: 'sandbox',
      mode: 'play',
      level: 'sandbox',
      humanSeat: 'freeps',
      seat: 'shadow',
      otherSeat: 'bot',
    });
    open(PLAYERS() + `<ge type="S" scenario="${info.replace(/"/g, '&quot;')}"/>` + DECISION());
    vi.advanceTimersByTime(TITLE_FLASH_MS * 3);
    expect(cues.setTitle).not.toHaveBeenCalledWith(DECISION_TITLE);
  });

  it('plays the decision sound when the page is in the background, for the decisions that had it', () => {
    away = true;
    open(PLAYERS() + DECISION());
    expect(cues.play).toHaveBeenCalledWith('awaitAction');
  });

  it('plays no sound while the player looks at the page', () => {
    open(PLAYERS() + DECISION());
    expect(cues.play).not.toHaveBeenCalled();
  });

  it('knows which decisions sound', () => {
    const d = (type: string, results: string[] = []) =>
      ({ id: '1', participantId: 'a', type, text: '', params: { results } }) as Parameters<typeof soundsOnArrival>[0];
    expect(soundsOnArrival(d('MULTIPLE_CHOICE', ['a', 'b']), false)).toBe(true);
    expect(soundsOnArrival(d('MULTIPLE_CHOICE', ['a', 'b']), true)).toBe(false);
    expect(soundsOnArrival(d('MULTIPLE_CHOICE', ['a', 'b', 'c']), false)).toBe(false);
    expect(soundsOnArrival(d('INTEGER'), false)).toBe(false);
    expect(soundsOnArrival(d('ASSIGN_MINIONS'), false)).toBe(false);
    for (const type of ['ARBITRARY_CARDS', 'CARD_ACTION_CHOICE', 'ACTION_CHOICE', 'CARD_SELECTION']) {
      expect(soundsOnArrival(d(type), false)).toBe(true);
    }
  });
});
