import { readServerResponse, readServerXml } from '../../../testing/files';
import {
  parseAuthoringDraft,
  parseGameHistory,
  parseGameHistoryFilters,
  parseMyScenarios,
  parsePlayerStats,
  parsePublicScenarios,
  parseScenarioCopy,
} from './account-parsers';

const json = (name: string): unknown => JSON.parse(readServerResponse(name));
const xml = (text: string) => new DOMParser().parseFromString(text, 'application/xml');

describe('account parsers', () => {
  it('reads a page of the game history, with the legacy fall-backs for old entries', () => {
    const page = parseGameHistory(readServerXml('game-history.xml'))!;
    expect(page).toMatchObject({ playerId: 'alice', matching: 23, total: 57, start: 20 });
    expect(page.entries[0]).toEqual({
      result: 'W',
      winReason: 'Opponent conceded',
      loseReason: 'Concession',
      opponent: 'bob',
      deck: 'Rohan <Riders>',
      format: 'Fellowship Block',
      event: 'Weekly League',
      startMs: 1763637000000,
      endMs: 1763640300000,
      endTime: '2025-11-20 12:05:00',
      durationMs: 3300000,
      recordingId: 'rec1',
    });
    expect(page.entries[1]).toMatchObject({ result: 'L', opponent: 'carol', event: null, recordingId: null });
    // no result: lost (alice isn't the winner), the opponent is the winner
    expect(page.entries[2]).toMatchObject({ result: 'L', opponent: 'dave', endMs: null, durationMs: null });
  });

  it('takes total for count and leaves start unknown when the root does not say them', () => {
    const page = parseGameHistory(xml('<gameHistory count="4" playerId="a"/>'))!;
    expect(page).toEqual({ playerId: 'a', matching: 4, total: 4, start: null, entries: [] });
    expect(parseGameHistory(xml('<error/>'))).toBeNull();
  });

  it('reads the filters and the player stats', () => {
    expect(parseGameHistoryFilters(json('game-history-filters.json'))).toEqual({
      formats: ['Fellowship Block', 'Movie Block'],
      events: ['Weekly League', 'Daily PC-Movie'],
    });
    expect(parseGameHistoryFilters({ formats: 'no', events: [1, 'x'] })).toEqual({ formats: [], events: ['x'] });
    expect(parsePlayerStats(readServerXml('player-stats.xml'))).toEqual({
      casual: [
        { format: 'Fellowship Block', deckName: 'Gondor & friends', wins: '5', losses: '3', percentage: '62.5%' },
      ],
      competitive: [],
    });
    expect(parsePlayerStats(xml('<playerStats/>'))).toEqual({ casual: [], competitive: [] });
    expect(parsePlayerStats(xml('<x/>'))).toBeNull();
  });

  it("reads the player's scenarios and the draft", () => {
    const mine = parseMyScenarios(json('scenarios-mine.json'));
    expect(mine.limit).toBe(100);
    expect(mine.ownFolder).toBe(true);
    expect(mine.authoring).toEqual({
      gameId: '7',
      id: 'alice/tutorial-2',
      title: 'Second steps',
      over: false,
      test: true,
      dirty: true,
    });
    expect(mine.scenarios.map((s) => s.path)).toEqual([
      'tutorial-1.hjson',
      'tutorial-2.hjson',
      'puzzle.hjson',
      'broken.hjson',
    ]);
    expect(mine.scenarios[0]).toMatchObject({
      status: 'submitted',
      locked: true,
      campaign: { id: 'basics', title: 'Basics', position: 2 },
      submittedAt: 1763640000000,
    });
    expect(mine.scenarios[1]?.origin).toEqual({ id: 'tutorial-2', title: 'Second steps (public)', exists: true });
    expect(mine.scenarios[2]?.published).toBe('current');
    expect(mine.scenarios[3]).toMatchObject({
      fullId: null,
      kind: null,
      errors: ['Line 3: a } is missing', 'No steps'],
    });
    expect(parseMyScenarios({ ownFolder: false, authoring: null })).toEqual({
      scenarios: [],
      limit: 0,
      ownFolder: false,
      authoring: null,
    });
    expect(parseAuthoringDraft({ id: 'x' })).toBeNull();
    expect(parseAuthoringDraft({ gameId: 12 })?.gameId).toBe('12');
  });

  it('reads the public scenarios and a copy', () => {
    const publics = parsePublicScenarios(json('scenarios-public.json'));
    expect(publics.map((s) => [s.fullId, s.author, s.disabled])).toEqual([
      ['first', 'Ketura', false],
      ['sandbox', null, true],
    ]);
    expect(parseScenarioCopy(json('scenario-copy.json'))).toEqual({
      id: 'alice/copy-of-first-steps',
      path: 'copy-of-first-steps.hjson',
    });
  });
});
