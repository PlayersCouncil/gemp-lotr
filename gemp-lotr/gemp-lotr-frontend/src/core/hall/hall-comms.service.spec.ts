import { HallApi } from './hall-api.service';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { HallComms, NEXT_POLL_DELAY_MS } from './hall-comms.service';
import { NOT_LOGGED_IN, OPENED_ELSEWHERE, RESUBSCRIBED } from './hall-messages';
import { HallUpdate } from './hall-models';

const HALL = '/gemp-lotr-server/hall';
const UPDATE = '/gemp-lotr-server/hall/update';

describe('HallComms', () => {
  let backend: HttpTestingController;
  let comms: HallComms;
  let updates: HallUpdate[];
  let games: string[];

  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(1794232985000 - 2000);
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        HallApi,
        HallComms,
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    comms = TestBed.inject(HallComms);
    updates = [];
    games = [];
    comms.updates$.subscribe((update) => updates.push(update));
    comms.startedGames$.subscribe((game) => games.push(game));
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  const signUp = (): TestRequest => backend.expectOne((r) => r.url === HALL && r.method === 'GET');
  const update = (): TestRequest => backend.expectOne((r) => r.url === UPDATE && r.method === 'POST');
  const fail = (req: TestRequest, status: number) => req.flush('', { status, statusText: 'Error' });
  const state = () => comms.connection().state;
  const message = () => comms.connection().detail?.message;

  /** Starts and answers the sign-up, then waits for the first update poll. */
  function connect(): void {
    comms.start();
    signUp().flush(readServerResponse('hall.xml'));
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
  }

  it('signs up, then long-polls the channel it was given', () => {
    expect(state()).toBe('connecting');
    comms.start();
    const req = signUp();
    expect(req.request.params.get('participantId')).toBe('alice');
    req.flush(readServerResponse('hall.xml'));

    expect(state()).toBe('connected');
    expect(comms.connection().lastUpdate).toBe('2026-11-09 14:03:05');
    expect(comms.serverTime()).toBe('2026-11-09 14:03:05');
    expect(comms.currency()).toBe(1234);
    expect(comms.motd()).toBe('Welcome <b>back</b>!');
    expect(comms.serverClockOffset()).toBe(2000);
    expect(updates.map((u) => u.full)).toEqual([true]);
    expect(games).toEqual(['g-9']);

    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS - 1);
    backend.expectNone(UPDATE);
    vi.advanceTimersByTime(1);
    const poll = update();
    expect(poll.request.body.get('channelNumber')).toBe('7');
    poll.flush(readServerResponse('hall-update.xml'));

    expect(state()).toBe('shutdown');
    expect(comms.shutdown()).toBe(true);
    expect(comms.currency()).toBe(1200);
    expect(comms.motd()).toBe('Welcome <b>back</b>!');
    expect(updates.map((u) => u.full)).toEqual([true, false]);
    // g-9 was already offered
    expect(games).toEqual(['g-9', 'g-11']);

    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update().flush(readServerResponse('hall-update-empty.xml'));
    expect(state()).toBe('connected');
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update();
    comms.stop();
  });

  it('retries lost answers with backoff, turns red after 5 failures, and recovers', () => {
    connect();
    const delays = [1000, 2000, 4000, 8000, 16000, 30000, 30000];
    delays.forEach((delay, i) => {
      fail(update(), 503);
      expect(state()).toBe(i < 4 ? 'reconnecting' : 'disconnected');
      vi.advanceTimersByTime(delay - 1);
      backend.expectNone(UPDATE);
      vi.advanceTimersByTime(1);
    });
    expect(message()).toBe(
      'Lost contact with the server (the server answered with error 503). Still retrying every 30 s; ' +
        'if it does not come back, reload the page.',
    );
    expect(comms.connection().detail?.action).toBe('reload');
    update().flush(readServerResponse('hall-update-empty.xml'));
    expect(state()).toBe('connected');
    expect(comms.connection().detail).toBeNull();

    // the count starts again
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update().error(new ProgressEvent('error'));
    expect(state()).toBe('reconnecting');
    expect(message()).toBe('Lost contact with the server (could not reach the server). Retrying in 1 s.');
    vi.advanceTimersByTime(1000);
    update();
    comms.stop();
  });

  it('gives up on an answer after 20 s and retries', () => {
    connect();
    const poll = update();
    vi.advanceTimersByTime(20_000);
    expect(poll.cancelled).toBe(true);
    expect(message()).toBe('Lost contact with the server (the server did not answer in time). Retrying in 1 s.');
    vi.advanceTimersByTime(1000);
    update();
    comms.stop();
  });

  it('retries an unreadable answer', () => {
    connect();
    update().flush('<html>proxy error</html');
    expect(message()).toContain('could not be read');
    vi.advanceTimersByTime(1000);
    // well-formed, but not the hall's answer
    update().flush('<html>proxy error</html>');
    expect(message()).toContain('could not be read');
    vi.advanceTimersByTime(2000);
    update();
    comms.stop();
  });

  it('signs up once when started again while polling, and polls no more once stopped from an update', () => {
    comms.start();
    comms.start();
    signUp().flush(readServerResponse('hall.xml'));
    // e.g. a game started and the page was left
    comms.updates$.subscribe(() => comms.stop());
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update().flush(readServerResponse('hall-update-empty.xml'));
    vi.advanceTimersByTime(60_000);
    backend.expectNone(() => true);
  });

  it('signs up again with a fresh snapshot when the channel was dropped (410), and says so', () => {
    connect();
    fail(update(), 410);
    expect(state()).toBe('reconnecting');
    vi.advanceTimersByTime(1000);
    signUp().flush(readServerResponse('hall.xml'));
    expect(state()).toBe('connected');
    expect(message()).toBe(RESUBSCRIBED);
    expect(updates.map((u) => u.full)).toEqual([true, true]);
    // the running game is not offered again
    expect(games).toEqual(['g-9']);

    // the message stays while connected
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update().flush(readServerResponse('hall-update-empty.xml'));
    expect(message()).toBe(RESUBSCRIBED);
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update();
    comms.stop();
  });

  it.each([
    [401, 'loggedout', NOT_LOGGED_IN, 'login'],
    [409, 'disconnected', OPENED_ELSEWHERE, 'reload'],
    [400, 'disconnected', 'The Game Hall stopped updating: the server answered with error 400.', 'reload'],
  ])('stops on %s', (status, expectedState, expectedMessage, action) => {
    connect();
    fail(update(), status);
    expect(state()).toBe(expectedState);
    expect(comms.connection().detail).toEqual({ message: expectedMessage, action });
    expect(comms.loggedOut()).toBe(status === 401);
    expect(comms.isRunning).toBe(false);
    vi.advanceTimersByTime(60_000);
    backend.expectNone(() => true);
  });

  it('stops cleanly: the request in flight and the pending poll are dropped; start() signs up again', () => {
    comms.start();
    const first = signUp();
    comms.stop();
    expect(first.cancelled).toBe(true);

    connect();
    const poll = update();
    poll.flush(readServerResponse('hall-update-empty.xml'));
    comms.stop();
    vi.advanceTimersByTime(60_000);
    backend.expectNone(() => true);

    comms.start();
    expect(state()).toBe('connecting');
    signUp();
    comms.stop();
  });

  it('keeps polling when a subscriber throws', () => {
    comms.updates$.subscribe(() => {
      throw new Error('drawing failed');
    });
    comms.start();
    signUp().flush(readServerResponse('hall.xml'));
    // rxjs reports the subscriber's error on a timer of its own
    expect(() => vi.advanceTimersByTime(0)).toThrow('drawing failed');
    vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    update();
    comms.stop();
  });

  it('shows "Not logged in" for a visitor, without polling', () => {
    comms.markLoggedOut();
    expect(state()).toBe('loggedout');
    expect(comms.connection().detail).toEqual({ message: null, action: 'login' });
    expect(comms.spoken()).toBe('Not logged in.');
    backend.expectNone(() => true);
  });
});
