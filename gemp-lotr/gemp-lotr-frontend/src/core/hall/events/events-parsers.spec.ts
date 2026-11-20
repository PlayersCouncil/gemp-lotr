import { readServerResponse } from '../../../testing/files';
import {
  parseCalendar,
  parseEventHistory,
  parseEventHistoryMonths,
  parseEventPrizes,
  parseLeagueDetail,
  parseLeagueList,
  parseTournamentDetail,
  parseTournamentList,
  price,
  safeUrl,
  soloDraftUrl,
  tournamentReportUrl,
} from './events-parsers';

const json = (name: string) => JSON.parse(readServerResponse(name)) as unknown;

describe('events parsers', () => {
  it('writes prices as the legacy page did', () => {
    expect(price(1050)).toEqual({ gold: 10, silver: '50' });
    expect(price(5)).toEqual({ gold: 0, silver: '05' });
  });

  it('reads the calendar, leaving out events of an unknown kind', () => {
    const month = parseCalendar(json('events-calendar.json'));
    expect(month.from).toBe('2026-10-26');
    expect(month.eventAdmin).toBe(true);
    expect(month.events.map((event) => event.kind)).toEqual([
      'league',
      'tournament',
      'tournament',
      'projected',
      'projected',
    ]);
    const [league, scheduled, started, projected, broken] = month.events;
    expect(league).toMatchObject({
      id: '1700000001',
      joined: true,
      cost: { gold: 10, silver: '50' },
      scheduleId: 'weekly',
      series: [
        { name: 'Serie 1', start: '2026-11-01', end: '2026-11-14', format: 'Fellowship Block', limited: false },
        { name: 'Finals', start: '2026-11-15', end: '2026-11-29', format: null, limited: false },
      ],
    });
    // the description's script is dropped
    expect(JSON.stringify(league)).not.toContain('alert');
    expect(scheduled).toMatchObject({
      startTime: '18:00',
      started: false,
      queue: { type: 'CONSTRUCTED' },
      prizes: null,
    });
    expect(started).toMatchObject({ startTime: '12:30', started: true, cost: null, queue: null, joined: true });
    expect(projected).toMatchObject({ scheduleName: 'Weekly Leagues', createdOn: '2026-11-24', error: null });
    expect(broken).toMatchObject({ error: 'Unknown sealed template' });
    expect(parseCalendar('nonsense').events).toEqual([]);
  });

  it('reads prizes as a label and lines, never the HTML', () => {
    expect(parseEventPrizes({ prizes: '<div>x</div>' })).toBeNull();
    expect(
      parseEventPrizes({ prizesLabel: 'Prize Breakdown', prizesLines: '["4+ Players", "1 Event Award"]' }),
    ).toEqual({
      label: 'Prize Breakdown',
      lines: ['4+ Players', '1 Event Award'],
    });
    expect(parseEventPrizes({ prizesLabel: 'No prizes', prizesLines: [] })).toEqual({ label: 'No prizes', lines: [] });
    const queue = new DOMParser().parseFromString(
      '<queue prizesLabel="No prizes" prizesLines="oops"/>',
      'application/xml',
    );
    expect(parseEventPrizes(queue.documentElement)).toEqual({ label: 'No prizes', lines: [] });
  });

  it('reads the event history', () => {
    expect(parseEventHistoryMonths(json('event-history-months.json'))).toEqual(['2026-10', '2026-08', '2025-12']);
    const leagues = parseEventHistory(json('event-history-league.json'), '2026-10', 'league');
    expect(leagues.isAdmin).toBe(false);
    expect(leagues.events[1]).toEqual({
      id: '1690000002',
      name: null,
      startDate: '2026-09-20',
      endDate: '2026-10-04',
      format: null,
      playerCount: null,
      rounds: null,
      adminLinks: [],
    });
    const tournaments = parseEventHistory(json('event-history-tournament.json'), '2026-10', 'tournament');
    expect(tournaments.events[0]!.adminLinks).toEqual([
      {
        label: 'Report',
        text: 'daily-pc_movie-20261031',
        url: '/gemp-lotr-server/tournament/daily-pc_movie-20261031/report/html',
      },
    ]);
    // an unsafe address is plain text; an entry with nothing to show is left out
    expect(tournaments.events[1]!.adminLinks).toEqual([{ label: 'Report', text: 'evil', url: null }]);
  });

  it('only lets same-site paths and http(s) addresses be links', () => {
    expect(safeUrl('/gemp-lotr/x')).toBe('/gemp-lotr/x');
    expect(safeUrl('https://example.org')).toBe('https://example.org');
    expect(safeUrl('//evil.example')).toBeNull();
    expect(safeUrl('javascript:alert(1)')).toBeNull();
  });

  it('reads the league list', () => {
    expect(parseLeagueList(readServerResponse('leagues.xml'))).toEqual([
      { code: '1700000001', name: 'Constructed League', start: '2026-11-01', end: '2026-12-01' },
      { code: '1700000002', name: 'Race to Mount Doom', start: '2026-11-01', end: '2026-12-01' },
      { code: '1700000003', name: 'Another Race', start: '2026-11-01', end: '2026-12-01' },
    ]);
    expect(parseLeagueList('<other/>')).toBeNull();
    expect(() => parseLeagueList('<leagues')).toThrow();
  });

  it('reads a league, its series and standings sorted', () => {
    const league = parseLeagueDetail(readServerResponse('league-detail.xml'))!;
    expect(league).toMatchObject({
      code: '1700000001',
      cost: { gold: 10, silver: '50' },
      member: false,
      joinable: true,
      draftable: false,
      rtmd: null,
    });
    expect(league.description[0]).toMatchObject({ kind: 'element', tag: 'p' });
    expect(league.standings.map((standing) => standing.player)).toEqual(['alice', 'carol', 'bob']);
    expect(league.standings[0]).toEqual({
      standing: 1,
      player: 'alice',
      points: 4,
      gamesPlayed: 2,
      opponentWin: '75.00%',
      level: null,
    });
    const [first, finals] = league.series;
    expect(first).toMatchObject({
      name: 'Serie 1',
      maxMatches: '10',
      formatCode: 'fotr_block',
      formatAvailable: true,
      collection: 'All cards',
      matches: [
        { winner: 'alice', loser: 'bob' },
        { winner: 'carol', loser: 'alice' },
      ],
    });
    expect(first!.standings.map((standing) => standing.player)).toEqual(['alice', 'carol']);
    expect(finals).toMatchObject({ formatAvailable: false, limited: true, matches: [], standings: [] });
    expect(parseLeagueDetail('<leagues/>')).toBeNull();
  });

  it("reads a Race to Mount Doom league's path", () => {
    const league = parseLeagueDetail(readServerResponse('league-detail-rtmd.xml'))!;
    expect(league.rtmd).toEqual({
      pathLength: 3,
      playerPosition: 2,
      cumulative: true,
      advancementMode: 'SCORE',
      advanceFactor: 2,
      metaSites: [
        { position: 1, blueprintId: '90_1', visualBlueprintId: '1_331', name: 'Weathertop' },
        { position: 2, blueprintId: '90_7', visualBlueprintId: null, name: 'Moria' },
        { position: 3, blueprintId: '90_9', visualBlueprintId: null, name: null },
      ],
    });
    expect(league.standings[0]!.level).toBe('2');
  });

  it('reads tournaments, with deck links once finished', () => {
    expect(parseTournamentList(readServerResponse('tournaments.xml'))).toEqual([
      { id: 'daily-pc_movie-20261120', name: 'Daily PC-Movie', start: '2026-11-20' },
      { id: 'casual-pc_fotr_block1794232985000', name: 'Casual PC-Fellowship', start: '' },
    ]);
    const playing = parseTournamentDetail(readServerResponse('tournament-playing.xml'))!;
    expect(playing).toMatchObject({ round: '2', stage: 'Playing Games', finished: false, playingGames: true });
    expect(playing.standings.map((standing) => [standing.player, standing.deckUrl])).toEqual([
      ['alice', null],
      ['bob', null],
    ]);
    const finished = parseTournamentDetail(readServerResponse('tournament-finished.xml'))!;
    expect(finished.standings[0]).toEqual({
      standing: 1,
      player: 'Gandalf the Grey',
      points: 3,
      gamesPlayed: 3,
      medianScore: '3',
      cumulativeScore: '9',
      opponentWin: '40.00%',
      deckUrl: '/gemp-lotr-server/tournament/daily-pc_movie-20261031/deck/Gandalf%20the%20Grey/html',
    });
    expect(finished.playingGames).toBe(false);
  });

  it("builds the server pages' addresses", () => {
    expect(tournamentReportUrl('wc 1')).toBe('/gemp-lotr-server/tournament/wc%201/report/html');
    expect(soloDraftUrl('1700000001')).toBe('/gemp-lotr/soloDraft.html?eventId=1700000001');
  });
});
