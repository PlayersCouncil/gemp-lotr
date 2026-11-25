import { readServerResponse } from '../../../testing/files';
import { LeagueDetail } from './events-models';
import { parseLeagueDetail, parseTournamentDetail } from './events-parsers';
import { leagueMembership, rtmdSummary, serieSummary, standingsHalves, tournamentLines } from './league-view';

const league = parseLeagueDetail(readServerResponse('league-detail.xml'))!;
const rtmd = parseLeagueDetail(readServerResponse('league-detail-rtmd.xml'))!;

describe('league view', () => {
  it('words the membership', () => {
    expect(leagueMembership(league, true)).toEqual({ kind: 'joinable' });
    expect(leagueMembership(league, false)).toEqual({ kind: 'none' });
    expect(leagueMembership(rtmd, true)).toEqual({ kind: 'member', draftUrl: null });
    const draft: LeagueDetail = { ...rtmd, draftable: true };
    expect(leagueMembership(draft, true)).toEqual({
      kind: 'member',
      draftUrl: '/gemp-lotr/soloDraft.html?eventId=1700000002',
    });
    const invite: LeagueDetail = { ...league, joinable: false, inviteOnly: true };
    expect(leagueMembership(invite, true)).toEqual({ kind: 'inviteOnly' });
  });

  it('sums up a serie', () => {
    expect(serieSummary(league.series[0]!)).toEqual({
      heading: 'Serie 1 - 2026-11-01 to 2026-11-14',
      format: 'Constructed Fellowship Block',
      formatCode: 'fotr_block',
      collection: 'All cards',
    });
    expect(serieSummary(league.series[1]!)).toMatchObject({ format: 'old_format (retired)', formatCode: null });
  });

  it('splits the standings into two halves', () => {
    const halves = standingsHalves(league.standings);
    expect(halves.map((row) => [row.left.player, row.right?.player ?? null])).toEqual([
      ['alice', 'bob'],
      ['carol', null],
    ]);
    expect(standingsHalves([])).toEqual([]);
  });

  it('marks the RTMD path for the viewer', () => {
    const summary = rtmdSummary(rtmd.rtmd!);
    expect(summary.advance).toBe('Advance every 2 points');
    expect(summary.cards.map((card) => card.mark)).toEqual(['passed', 'current', 'none']);
    expect(rtmdSummary({ ...rtmd.rtmd!, playerPosition: null, advanceFactor: 1 })).toMatchObject({ advance: null });
  });

  it("gives a tournament's lines", () => {
    const playing = parseTournamentDetail(readServerResponse('tournament-playing.xml'))!;
    expect(tournamentLines(playing, true)).toEqual({
      rounds: '2',
      format: 'PC-Movie',
      collection: 'Default',
      round: '2',
    });
    const finished = parseTournamentDetail(readServerResponse('tournament-finished.xml'))!;
    expect(tournamentLines(finished, false)).toEqual({
      rounds: null,
      format: 'PC-Movie',
      collection: 'Default',
      round: null,
    });
  });
});
