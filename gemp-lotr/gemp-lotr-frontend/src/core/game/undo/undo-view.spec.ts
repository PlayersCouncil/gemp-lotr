import { PendingUndo, RestorePoint } from './undo-protocol';
import { clockLine, goLabel, listGroups, listNote, requestHead, tierHelp, UNDO_BUTTON, when } from './undo-view';

const point = (entryCount: number, turn: number, phase: string): RestorePoint => ({
  entryCount,
  label: `Choice ${entryCount}`,
  turn,
  phase,
  tier: 'YELLOW',
  reasons: [],
  unavailable: null,
});

const pending: PendingUndo = {
  requestId: 1,
  requester: 'bob',
  label: 'Played Gimli',
  turn: 2,
  phase: 'Fellowship',
  tier: 'RED',
  reasons: [],
  secondsLeft: 60,
};

describe("taking a game back, in undo.js's words", () => {
  it('names a turn and its phase; before the first turn the game is being set up', () => {
    expect(when(2, 'Fellowship')).toBe('Turn 2 · Fellowship');
    expect(when(0, 'Play starting fellowship')).toBe('Setup · Play starting fellowship');
    expect(when(3, '')).toBe('Turn 3');
  });

  it("marks each tier and names going back to it: an undo when it's only the player's own choice", () => {
    expect(UNDO_BUTTON).toBe('✓ Undo');
    expect(goLabel('GREEN')).toBe('✓ Undo');
    expect(goLabel('YELLOW')).toBe('~ Revert');
    expect(goLabel('RED')).toBe('✕ Revert');
  });

  it('explains the tiers; a revert needs approval only where somebody is asked', () => {
    const asks = { green: true, yellow: true, red: true, asks: true, timeoutSeconds: 60 };
    expect(tierHelp(asks)).toEqual([
      { tier: 'GREEN', word: ' Undo. ', means: 'Only your own choice. Nobody is asked.' },
      { tier: 'YELLOW', word: ' Revert. ', means: 'Your opponent replays choices. Needs their approval.' },
      { tier: 'RED', word: ' Revert. ', means: 'Hidden information was revealed. Needs their approval.' },
    ]);
    expect(tierHelp({ ...asks, asks: false }).map((line) => line.means)).toEqual([
      'Only your own choice. Nobody is asked.',
      'Your opponent replays choices.',
      'Hidden information was revealed.',
    ]);
    expect(tierHelp(null)[1]?.means).toBe('Your opponent replays choices.');
  });

  it('divides the places by turn and phase, a heading each time either changes', () => {
    const groups = listGroups([
      point(9, 2, 'Shadow'),
      point(8, 2, 'Shadow'),
      point(7, 2, 'Fellowship'),
      point(6, 1, ''),
    ]);
    expect(groups.map((group) => [group.when, group.points.map((p) => p.entryCount)])).toEqual([
      ['Turn 2 · Shadow', [9, 8]],
      ['Turn 2 · Fellowship', [7]],
      ['Turn 1', [6]],
    ]);
    expect(listGroups([])).toEqual([]);
  });

  it('says above the list when a request is pending or there is nothing to go back to', () => {
    expect(listNote([point(1, 1, '')], pending)).toBe('A revert request is pending.');
    expect(listNote([], null)).toBe('Nothing to go back to.');
    expect(listNote([point(1, 1, '')], null)).toBeNull();
  });

  it('counts a request down, then says it is out of time', () => {
    expect(clockLine(57)).toBe('Game paused · auto-declines in 57s');
    expect(clockLine(0)).toBe('Out of time');
    expect(clockLine(-3)).toBe('Out of time');
  });

  it("heads a request as the asker's own or as another player's", () => {
    expect(requestHead(pending, true)).toEqual({ title: 'Revert requested', head: 'You asked to revert to before:' });
    expect(requestHead(pending, false)).toEqual({ title: 'Revert request', head: 'bob asks to revert to before:' });
  });
});
