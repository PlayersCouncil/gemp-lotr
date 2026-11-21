import { TestBed } from '@angular/core/testing';
import { answerWith, setUpEvents } from '../../../testing/events-server';
import { settle } from '../../../testing/fake-notifier';
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
});
