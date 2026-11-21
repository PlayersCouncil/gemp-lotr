import { TestBed } from '@angular/core/testing';
import { answerWith, setUpEvents } from '../../../testing/events-server';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { LeagueDetail } from './events-models';
import { EVENTS_LEAGUE_JOIN_LISTENER } from './events-hooks';
import { EventsTab } from './events-tab.service';
import { REFRESH_AFTER_MS } from '../return-refresh';

describe('EventsTab', () => {
  let backend: ReturnType<typeof setUpEvents>['backend'];
  let clock: { now: number };
  let tab: EventsTab;

  beforeEach(() => {
    ({ backend, clock } = setUpEvents());
    tab = TestBed.inject(EventsTab);
  });

  afterEach(() => backend.verify());

  async function started(): Promise<void> {
    tab.start();
    answerWith(backend, '/league', 'leagues.xml');
    answerWith(backend, '/tournament', 'tournaments.xml');
    answerWith(backend, '/calendar', 'events-calendar.json');
    await settle();
  }

  it('starts everything once, and each completed-events panel after its live list', async () => {
    await started();
    tab.start();
    const months = backend.match((request) => request.url.endsWith('/eventHistory/months'));
    expect(months.map((request) => request.request.params.get('kind'))).toEqual(['league', 'tournament']);
    months.forEach((request) => request.flush('{"months":[]}'));
    await settle();
    expect(tab.leagueHistory.status().text).toBe('No completed leagues have been recorded yet.');
  });

  it("opens a league's or tournament's link", async () => {
    await started();
    backend
      .match((request) => request.url.endsWith('/eventHistory/months'))
      .forEach((request) => request.flush('{"months":[]}'));
    tab.open({ sub: 'leagues', code: '1700000003' });
    await settle();
    expect(tab.leagues.revealed()?.key).toBe('1700000003');
    answerWith(backend, '/league/1700000003', 'league-detail.xml');
    tab.open({ sub: 'tournaments', id: 'daily-pc_movie-20261120' });
    await settle();
    expect(tab.tournaments.revealed()?.key).toBe('daily-pc_movie-20261120');
    answerWith(backend, '/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
  });

  it('loads the calendar again after a join, and tells the hall', async () => {
    const told: string[] = [];
    TestBed.resetTestingModule();
    ({ backend, clock } = setUpEvents([
      { provide: EVENTS_LEAGUE_JOIN_LISTENER, useValue: { leagueJoined: (code: string) => told.push(code) } },
    ]));
    tab = TestBed.inject(EventsTab);
    await started();
    backend
      .match((request) => request.url.endsWith('/eventHistory/months'))
      .forEach((request) => request.flush('{"months":[]}'));
    tab.leagues.requestJoin({ code: '1700000003', cost: { gold: 0, silver: '00' } } as LeagueDetail);
    const joined = tab.leagues.answerJoin(true);
    backend.expectOne((request) => request.method === 'POST').flush('');
    await joined;
    answerWith(backend, '/calendar', 'events-calendar.json');
    expect(told).toEqual(['1700000003']);
  });

  it('refreshes the sub-tab the viewer comes back to after 10 s', async () => {
    await started();
    backend
      .match((request) => request.url.endsWith('/eventHistory/months'))
      .forEach((request) => request.flush('{"months":[]}'));
    tab.subTabShown('leagues');
    tab.subTabShown(null);
    clock.now += REFRESH_AFTER_MS;
    tab.subTabShown('leagues');
    answerWith(backend, '/league', 'leagues.xml');
    await settle();
    clock.now += REFRESH_AFTER_MS;
    tab.returns.setPageShown(false);
    clock.now += REFRESH_AFTER_MS;
    tab.returns.setPageShown(true);
    answerWith(backend, '/league', 'leagues.xml');
    await settle();
  });
  it('fetches the calendar or the live tournaments again when the viewer comes back to them after 10 s', async () => {
    await started();
    backend
      .match((request) => request.url.endsWith('/eventHistory/months'))
      .forEach((request) => request.flush('{"months":[]}'));
    for (const sub of ['calendar', 'tournaments'] as const) {
      tab.subTabShown(sub);
      tab.subTabShown(null);
      clock.now += REFRESH_AFTER_MS;
      tab.subTabShown(sub);
    }
    answerWith(backend, '/calendar', 'events-calendar.json');
    // the tournaments come back with a new name and date: the row keeps its drawer and shows them
    const drawer = tab.tournaments.rows()[0]!.drawer;
    backend
      .expectOne((request) => request.url.endsWith('/tournament'))
      .flush(
        '<tournaments><tournament id="daily-pc_movie-20261120" name="Daily PC-Movie (late)" start="2026-11-21" round="2" stage="Playing Games"/></tournaments>',
      );
    await settle();
    const row = tab.tournaments.rows()[0]!;
    expect([row.title(), row.date(), row.drawer]).toEqual(['Daily PC-Movie (late)', '2026-11-21', drawer]);
  });

  it("fetches a completed league's or tournament's details from the server", async () => {
    await started();
    backend
      .match((request) => request.url.endsWith('/eventHistory/months'))
      .forEach((request) => request.flush('{"months":["2026-10"]}'));
    await settle();
    backend
      .match((request) => request.url.endsWith('/eventHistory'))
      .forEach((request) =>
        request.flush(
          readServerResponse(
            request.request.params.get('kind') === 'league'
              ? 'event-history-league.json'
              : 'event-history-tournament.json',
          ),
        ),
      );
    await settle();
    tab.leagueHistory.rows()[0]!.drawer!.open();
    answerWith(backend, '/league/1690000001', 'league-detail.xml');
    tab.tournamentHistory.rows()[0]!.drawer!.open();
    answerWith(backend, '/tournament/daily-pc_movie-20261031', 'tournament-finished.xml');
    await settle();
    expect(tab.leagueHistory.rows()[0]!.drawer!.content()?.kind).toBe('data');
    expect(tab.tournamentHistory.rows()[0]!.drawer!.content()?.kind).toBe('data');
  });
});
