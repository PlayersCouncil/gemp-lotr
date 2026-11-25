import { TestBed } from '@angular/core/testing';
import { answerWith, eventsRequest, failWith, setUpEvents } from '../../../testing/events-server';
import { FakeNotifier, settle } from '../../../testing/fake-notifier';
import { Notifier } from '../../notifier/notifier';
import { LeagueDetail } from './events-models';
import { dateRange, LeagueResults } from './league-results.service';

describe('LeagueResults', () => {
  let backend: ReturnType<typeof setUpEvents>['backend'];
  let leagues: LeagueResults;
  let notifier: FakeNotifier;

  beforeEach(() => {
    backend = setUpEvents().backend;
    leagues = TestBed.inject(LeagueResults);
    notifier = TestBed.inject(Notifier) as FakeNotifier;
  });

  afterEach(() => backend.verify());

  async function loaded(): Promise<void> {
    const done = leagues.loadList();
    answerWith(backend, '/league', 'leagues.xml');
    await done;
  }

  it('lists the current leagues as rows with their dates', async () => {
    expect(leagues.list()).toEqual({ kind: 'loading' });
    await loaded();
    expect(leagues.list()).toEqual({ kind: 'loaded' });
    expect(leagues.rows().map((row) => [row.key, row.title(), row.date()])).toEqual([
      ['1700000001', 'Constructed League', '2026-11-01 to 2026-12-01'],
      ['1700000002', 'Race to Mount Doom', '2026-11-01 to 2026-12-01'],
      ['1700000003', 'Another Race', '2026-11-01 to 2026-12-01'],
    ]);
    expect(dateRange('2026-11-01', '2026-11-01')).toBe('2026-11-01');
    expect(dateRange('2026-11-01', '')).toBe('2026-11-01');
  });

  it('shares one request among callers and shows its failure in place of the list', async () => {
    const first = leagues.loadList();
    const second = leagues.loadList();
    failWith(backend, '/league', 500);
    await Promise.all([first, second]);
    expect(leagues.list()).toEqual({
      kind: 'error',
      text: 'Could not load the current leagues. The server failed to produce it.',
    });
  });

  it("opens a league's drawer and fails inline with the status", async () => {
    await loaded();
    const row = leagues.rows()[0]!;
    row.drawer.open();
    failWith(backend, '/league/1700000001', 404);
    await settle();
    expect(row.drawer.content()).toEqual({
      kind: 'error',
      text: 'Could not load the league (HTTP 404). It was not found on the server.',
    });
  });

  it('refreshes in place: kept rows keep their drawers, open ones fetched again quietly', async () => {
    await loaded();
    const [first, second] = leagues.rows();
    first!.drawer.open();
    answerWith(backend, '/league/1700000001', 'league-detail.xml');
    await settle();
    leagues.refreshList();
    leagues.refreshList(); // while one is in flight: nothing more
    eventsRequest(backend, '/league').flush(
      '<leagues><league code="1700000002" name="Race" start="2026-11-01" end="2026-12-01"/>' +
        '<league code="1700000001" name="Constructed League" start="2026-11-01" end="2026-12-01"/></leagues>',
    );
    await settle();
    expect(leagues.rows()).toEqual([second, first]);
    expect(second!.title()).toBe('Race');
    failWith(backend, '/league/1700000001', 500);
    await settle();
    expect(first!.drawer.content()?.kind).toBe('data');
  });

  it('keeps the list when a refresh fails', async () => {
    await loaded();
    leagues.refreshList();
    failWith(backend, '/league', 0);
    await settle();
    expect(leagues.rows()).toHaveLength(3);
    expect(leagues.list()).toEqual({ kind: 'loaded' });
  });

  it('shows a league from a link: loads the list, opens the row and reveals it', async () => {
    const shown = leagues.showLeague('1700000002');
    answerWith(backend, '/league', 'leagues.xml');
    await shown;
    expect(leagues.rows()[1]!.drawer.isOpen()).toBe(true);
    expect(leagues.revealed()).toEqual({ key: '1700000002', seq: 1 });
    eventsRequest(backend, '/league/1700000002').flush('<league/>');
    await leagues.showLeague('1700000002');
    expect(leagues.revealed()).toEqual({ key: '1700000002', seq: 2 });
  });

  it('joins after Yes, re-renders every live drawer of that league and tells joined$', async () => {
    await loaded();
    const joined: string[] = [];
    leagues.joined$.subscribe((code) => joined.push(code));
    const row = leagues.rows()[0]!;
    const preview = leagues.createDrawer('1700000001', true);
    row.drawer.open();
    preview.open();
    const requests = backend.match((request) => request.url.endsWith('/league/1700000001'));
    requests.forEach((request) => request.flush(readDetail()));
    await settle();
    const detail = (row.drawer.content() as { data: LeagueDetail }).data;
    leagues.requestJoin(detail);
    expect(leagues.joinQuestion()).toEqual({ code: '1700000001', cost: { gold: 10, silver: '50' } });
    const answered = leagues.answerJoin(true);
    expect(leagues.joinQuestion()).toBeNull();
    eventsRequest(backend, '/league/1700000001', 'POST').flush('');
    await answered;
    expect(joined).toEqual(['1700000001']);
    expect(
      backend.match((request) => request.url.endsWith('/league/1700000001') && request.method === 'GET'),
    ).toHaveLength(2);
  });

  it('says why a join failed', async () => {
    leagues.requestJoin({ code: '9', cost: { gold: 0, silver: '00' } } as LeagueDetail);
    const poor = leagues.answerJoin(true);
    failWith(backend, '/league/9', 409, 'POST');
    await poor;
    leagues.requestJoin({ code: '9', cost: { gold: 0, silver: '00' } } as LeagueDetail);
    const gone = leagues.answerJoin(true);
    failWith(backend, '/league/9', 404, 'POST');
    await gone;
    leagues.requestJoin({ code: '9', cost: { gold: 0, silver: '00' } } as LeagueDetail);
    await leagues.answerJoin(false);
    expect(notifier.dialogs).toEqual([
      "alert: You don't have enough funds to join this league.",
      'alert: It was not found on the server.',
    ]);
  });
});

function readDetail(): string {
  return '<league code="1700000001" name="Constructed League" cost="1050" joinable="true" member="false"/>';
}
