import { pendingOf, readGameNotices, readRestorePoints, readUndoReply } from './undo-protocol';

describe('the undo answers of the server', () => {
  it('reads the places to go back to, the policy, the switch and a request waiting', () => {
    const read = readRestorePoints({
      points: [
        {
          entryCount: 12,
          label: 'Pressed DONE',
          turn: 0,
          phase: 'Play starting fellowship',
          tier: 'RED',
          reasons: ["Hidden information revealed: opponent's card seen"],
          unavailable: 'GameFoe0 is auto-declining reverts',
        },
        { entryCount: 11, label: 'Played Gimli', turn: 0, phase: '', tier: 'GREEN', reasons: [], unavailable: null },
      ],
      policy: { green: true, yellow: true, red: false, asks: true, timeoutSeconds: 60, greenPerPhase: 3 },
      autoDecline: true,
      declining: ['GameFoe0'],
      pending: {
        requestId: 4,
        requester: 'alice',
        label: 'Played Gimli',
        turn: '2',
        phase: 'Fellowship',
        tier: 'YELLOW',
        reasons: ['Opponent replays 1 choice'],
        waitingOn: ['bob'],
        secondsLeft: 57,
      },
    });
    expect(read.points).toEqual([
      {
        entryCount: 12,
        label: 'Pressed DONE',
        turn: 0,
        phase: 'Play starting fellowship',
        tier: 'RED',
        reasons: ["Hidden information revealed: opponent's card seen"],
        unavailable: 'GameFoe0 is auto-declining reverts',
      },
      { entryCount: 11, label: 'Played Gimli', turn: 0, phase: '', tier: 'GREEN', reasons: [], unavailable: null },
    ]);
    expect(read.policy).toEqual({ green: true, yellow: true, red: false, asks: true, timeoutSeconds: 60 });
    expect(read.autoDecline).toBe(true);
    expect(read.pending).toEqual({
      requestId: 4,
      requester: 'alice',
      label: 'Played Gimli',
      turn: 2,
      phase: 'Fellowship',
      tier: 'YELLOW',
      reasons: ['Opponent replays 1 choice'],
      secondsLeft: 57,
    });
  });

  it('reads an answer it does not understand as nothing to take back (an unknown tier counts as red)', () => {
    expect(readRestorePoints(null)).toEqual({
      points: [],
      policy: { green: false, yellow: false, red: false, asks: false, timeoutSeconds: 0 },
      autoDecline: false,
      pending: null,
    });
    const [point] = readRestorePoints({ points: [{ tier: 'BLUE', turn: 'x', reasons: 'no' }] }).points;
    expect(point).toEqual({
      entryCount: 0,
      label: '',
      turn: 0,
      phase: '',
      tier: 'RED',
      reasons: [],
      unavailable: null,
    });
  });

  it('reads the answer to an undo or a reply: done, pending, or anything else refused', () => {
    expect(readUndoReply({ status: 'done', message: null, requestId: null })).toEqual({
      status: 'done',
      message: null,
    });
    expect(readUndoReply({ status: 'pending', message: 'Asked', requestId: 3 })).toEqual({
      status: 'pending',
      message: 'Asked',
    });
    expect(readUndoReply({ status: 'odd', message: 5 })).toEqual({ status: 'refused', message: '5' });
    expect(readUndoReply('nonsense')).toEqual({ status: 'refused', message: null });
  });

  it('reads the notices with their fields as text, and where to go on from', () => {
    expect(readGameNotices({ notices: [], latest: 7 })).toEqual({ notices: [], latest: 7 });
    expect(readGameNotices({})).toEqual({ notices: [], latest: null });
    const { notices } = readGameNotices({
      notices: [{ number: 8, type: 'resync', reason: 'revert', channelNumber: 3 }],
      latest: 8,
    });
    expect(notices).toEqual([
      {
        number: 8,
        type: 'resync',
        fields: { number: '8', type: 'resync', reason: 'revert', channelNumber: '3' },
      },
    ]);
  });

  it("reads the request an undoRequested notice announces (its reasons joined by '; ')", () => {
    const notice = (reasons: string) => ({
      number: 2,
      type: 'undoRequested',
      fields: {
        requestId: '5',
        requester: 'bob',
        label: 'Played Gimli',
        turn: '1',
        phase: 'Shadow',
        tier: 'YELLOW',
        reasons,
        seconds: '60',
      },
    });
    expect(pendingOf(notice('Opponent replays 1 choice; A shuffle'))).toEqual({
      requestId: 5,
      requester: 'bob',
      label: 'Played Gimli',
      turn: 1,
      phase: 'Shadow',
      tier: 'YELLOW',
      reasons: ['Opponent replays 1 choice', 'A shuffle'],
      secondsLeft: 60,
    });
    expect(pendingOf(notice('')).reasons).toEqual([]);
  });
});
