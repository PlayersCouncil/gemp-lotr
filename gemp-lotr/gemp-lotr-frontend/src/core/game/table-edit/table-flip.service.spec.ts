import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { DecisionGates } from '../decision/decision-gate';
import { ScenarioEnd, ScenarioInfo } from '../scenario/scenario-events';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { GameUndo } from '../undo/game-undo.service';
import { NOT_DONE, PROBLEM_MS } from './table-edit.service';
import { FLIP_PATH, OTHER_SEAT_PATH, TableFlip } from './table-flip.service';

/** The default sandbox's info, in play, with the person giving the other seat to themselves (ef-notes Part 2 E). */
const INFO = {
  type: 'info',
  id: 'sandbox',
  kind: 'sandbox',
  humanSeat: 'freeps',
  level: 'sandbox',
  mode: 'play',
  author: false,
  seat: 'freeps',
  otherSeat: 'script',
  otherSeats: ['script', 'bot', 'person'],
  waitingOn: 'freeps',
  canFlip: true,
} as unknown as ScenarioInfo;

describe('TableFlip', () => {
  let flip: TableFlip;
  let backend: HttpTestingController;
  const info = signal<ScenarioInfo | null>(INFO);
  const end = signal<ScenarioEnd | null>(null);
  const playing = signal(true);
  const model = signal({ ended: false });
  let openGame: string | null = '12';
  const refresh = vi.fn();
  const undoRefresh = vi.fn();

  beforeEach(() => {
    info.set(INFO);
    end.set(null);
    playing.set(true);
    model.set({ ended: false });
    openGame = '12';
    refresh.mockClear();
    undoRefresh.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        // (an admin acting as somebody: the flip is the person's own, so no participantId is sent)
        { provide: PAGE_QUERY_STRING, useValue: 'participantId=admin' },
        PageLifetime,
        DecisionGates,
        { provide: ScenarioGuide, useValue: { info, end, playing } },
        {
          provide: GameSession,
          useValue: {
            model,
            refresh,
            get openGame() {
              return openGame;
            },
          },
        },
        { provide: GameUndo, useValue: { refresh: undoRefresh } },
        TableFlip,
      ],
    });
    flip = TestBed.inject(TableFlip);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const posted = (path: string) => backend.expectOne((r) => r.url === `/gemp-lotr-server${path}`);
  const form = (body: unknown) => Object.fromEntries(new URLSearchParams(String(body)));

  it('reads the seats from the info: looked from, who plays the other seat, the choices, what is waited on', () => {
    expect(flip.seat()).toBe('freeps');
    expect(flip.flipped()).toBe(false);
    expect(flip.canFlip()).toBe(true);
    expect(flip.otherSeat()).toBe('script');
    expect(flip.otherSeats()).toEqual(['script', 'bot', 'person']);
    expect(flip.answers()).toBe(true);
    expect(flip.elsewhere()).toBeNull();
    expect(flip.humanSeat()).toBe('freeps');
    expect(flip.looking()).toBeNull();
    expect(flip.flipTitle()).toBe("Look from Shadow's side");
    info.set({ ...INFO, seat: 'shadow' });
    expect(flip.looking()).toBe('As Shadow');
    expect(flip.flipTitle()).toBe('Back to your side (Free Peoples)');
    expect(flip.flipped()).toBe(true);
    expect(flip.seat()).toBe('shadow');
    // (flipped onto a seat the script plays: nothing is answered here, and the person's own seat is to choose)
    expect(flip.answers()).toBe(false);
    expect(TestBed.inject(DecisionGates).allows({ kind: 'escape' })).toBe(false);
    expect(flip.elsewhere()).toBe('freeps');
    // Over: no more choices of the other seat, nothing said to be waited on
    end.set({ type: 'end' } as ScenarioEnd);
    expect(flip.otherSeats()).toEqual([]);
    expect(flip.elsewhere()).toBeNull();
    // Not a sandbox (an author's table): the flip, no choices; a replay or no scenario: nothing
    end.set(null);
    info.set({ ...INFO, level: 'none', otherSeat: null, otherSeats: [], author: true });
    expect(flip.canFlip()).toBe(true);
    expect(flip.otherSeats()).toEqual([]);
    playing.set(false);
    expect(flip.canFlip()).toBe(false);
    expect(flip.seat()).toBe('freeps');
    expect(flip.humanSeat()).toBe('freeps');
  });

  it('flips: posts the game, and leaves reading the game again to the notice that follows', async () => {
    const done = flip.flip();
    expect(flip.busy()).toBe(true);
    const request = posted('/scenarios/sandbox/flip');
    expect(FLIP_PATH).toBe('/scenarios/sandbox/flip');
    expect(request.request.method).toBe('POST');
    expect(form(request.request.body as unknown)).toEqual({ gameId: '12' });
    request.flush({ ok: true, seat: 'shadow', state: null });
    expect(await done).toBe(true);
    expect(flip.busy()).toBe(false);
    expect(refresh).not.toHaveBeenCalled();
    // One call at a time; nothing without a game, or where the table can't be flipped
    void flip.flip();
    expect(await flip.flip()).toBe(false);
    posted(FLIP_PATH).flush({ ok: true });
    openGame = null;
    expect(await flip.flip()).toBe(false);
    info.set({ ...INFO, canFlip: false });
    expect(await flip.flip()).toBe(false);
  });

  it("gives the other seat to another player, then reads the game's news and undo's list", async () => {
    const done = flip.setOtherSeat('person');
    const request = posted('/scenarios/sandbox/otherSeat');
    expect(OTHER_SEAT_PATH).toBe('/scenarios/sandbox/otherSeat');
    expect(form(request.request.body as unknown)).toEqual({ gameId: '12', to: 'person' });
    request.flush({ ok: true, state: {} });
    expect(await done).toBe(true);
    expect(refresh).toHaveBeenCalled();
    expect(undoRefresh).toHaveBeenCalled();
    // (the one it has, or one not on offer: nothing asked)
    expect(await flip.setOtherSeat('script')).toBe(false);
    info.set({ ...INFO, otherSeats: ['script', 'person'] });
    expect(await flip.setOtherSeat('bot')).toBe(false);
  });

  it("says a refusal in the server's words for a while, or its own when the server says none", async () => {
    vi.useFakeTimers();
    try {
      const done = flip.setOtherSeat('bot');
      posted(OTHER_SEAT_PATH).flush(
        { error: 'This server has no bot to play the other seat.' },
        { status: 400, statusText: 'Bad Request' },
      );
      expect(await done).toBe(false);
      expect(flip.problem()).toBe('This server has no bot to play the other seat.');
      expect(refresh).not.toHaveBeenCalled();
      vi.advanceTimersByTime(PROBLEM_MS);
      expect(flip.problem()).toBeNull();
      const again = flip.flip();
      posted(FLIP_PATH).error(new ProgressEvent('error'));
      expect(await again).toBe(false);
      expect(flip.problem()).toBe(NOT_DONE);
    } finally {
      vi.useRealTimers();
    }
  });
});
