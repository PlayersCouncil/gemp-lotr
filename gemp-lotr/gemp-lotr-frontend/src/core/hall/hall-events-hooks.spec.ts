import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../testing/hall';
import { readServerResponse } from '../../testing/files';
import { EVENTS_LEAGUE_JOIN_LISTENER, EVENTS_QUEUE_JOINER } from './events/events-hooks';
import { injectGameHall } from './hall-services';
import { JoinDialog } from './play/join-dialog.service';
import { LeagueTableFlow } from './play/league-table.service';

describe('the Events tab hooks the hall provides', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    vi.useFakeTimers();
    hall = setUpHall();
    TestBed.runInInjectionContext(() => injectGameHall());
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  it("joins the calendar's queue through the hall's join flow: the deck dialog for a Constructed queue", () => {
    hall.connect();
    TestBed.inject(EVENTS_QUEUE_JOINER).joinQueue({ id: 'daily', name: 'Daily FotR', type: 'CONSTRUCTED', start: 'x' });
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    expect(TestBed.inject(JoinDialog).state()).toMatchObject({ kind: 'queue', title: 'Join Queue: Daily FotR' });
  });

  it('joins a queue the hall has not listed at once when it needs no deck', () => {
    hall.connect();
    TestBed.inject(EVENTS_QUEUE_JOINER).joinQueue({
      id: 'sealed1',
      name: 'Sat Sealed',
      type: 'SEALED',
      start: 'at 18:00',
    });
    const req = hall.request('/hall/queue/sealed1');
    expect(hall.body(req)).toMatchObject({ deckName: '', deckSource: '' });
    req.flush('<result>OK</result>');
  });

  it("reloads Play > League's leagues after a join on the Events tab", async () => {
    TestBed.inject(EVENTS_LEAGUE_JOIN_LISTENER).leagueJoined('1700000003');
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml'));
    await vi.advanceTimersByTimeAsync(0);
    expect(
      TestBed.inject(LeagueTableFlow)
        .options()
        .map((o) => o.label),
    ).toEqual(['Constructed League', 'Race to Mount Doom']);
  });
});
