import { HttpErrorResponse } from '@angular/common/http';
import { decision, draftState, position } from '../../../testing/authoring';
import { ApiError } from '../../api/api-error';
import { SaveReport, UncoveredDecision } from './authoring-state';
import {
  leftOutNotice,
  mayRetake,
  problemOf,
  problemsNotice,
  savedNotice,
  uncoveredNotice,
  uncoveredOf,
  unscripted,
  whereLine,
} from './notices';

const open: UncoveredDecision = {
  seat: 'human',
  after: 2,
  when: 'freeps, action',
  repeating: null,
  until: null,
  becomes: null,
  problem: null,
  block: null,
  sub: 0,
  reuses: null,
};

describe('whereLine', () => {
  it('says which step the game is at, and the turn and phase', () => {
    expect(whereLine(draftState())).toBe('Step 1 of 3 · turn 1, fellowship');
    expect(whereLine(draftState({ position: position({ step: 3, sub: 2 }) }))).toBe(
      'Step 3.2 of 3 · turn 1, fellowship',
    );
    expect(whereLine(draftState({ position: position({ inHand: false, phase: null }) }))).toBe(
      'Step 1 of 3 (waiting to come to it) · turn 1, ',
    );
    expect(whereLine(draftState({ position: position({ step: 2, inHand: false, underWay: true }) }))).toBe(
      'Step 2 of 3 (which repeats, and is under way) · turn 1, fellowship',
    );
    expect(whereLine(draftState({ position: position({ step: 3, inHand: false, underWay: true }) }))).toBe(
      'Step 3 of 3 (a block, between its decisions) · turn 1, fellowship',
    );
    expect(whereLine(draftState({ position: position({ frontier: true, step: 4, turn: 2, phase: 'regroup' }) }))).toBe(
      'Past the end of the script (3 steps) · turn 2, regroup',
    );
    expect(whereLine(draftState({ position: position({ over: true }) }))).toBe('The game is over');
  });
});

describe('problemOf', () => {
  const failed = (status: number, error: unknown = '') => ApiError.from(new HttpErrorResponse({ status, error }));

  it("says the server's own words, or what its answer's status means", () => {
    expect(problemOf(failed(400, '{"error":"No such step."}'))).toBe('No such step.');
    expect(problemOf(failed(403))).toBe('Writing scenarios is not open to this account.');
    expect(problemOf(failed(0))).toBe('The server could not be reached.');
    expect(problemOf(failed(502))).toBe('The server answered 502.');
    expect(problemOf(new Error('x'))).toBe('The server answered -1.');
  });
});

describe('unscripted', () => {
  it("is a decision waiting that no step has in hand: the player's, or the opponent's held for the author", () => {
    const free = position({ inHand: false });
    expect(unscripted(draftState({ position: free }))).toBe(true);
    expect(unscripted(draftState())).toBe(false);
    expect(unscripted(draftState({ position: position({ inHand: false, over: true }) }))).toBe(false);
    const opponent = (held: boolean) =>
      draftState({ position: free, waiting: { human: null, opponent: decision({ seat: 'shadow', held }) } });
    expect(unscripted(opponent(true))).toBe(true);
    expect(unscripted(opponent(false))).toBe(false);
  });
});

describe('the decision no step covers', () => {
  const free = position({ inHand: false });

  it('is said where something can be done about it', () => {
    expect(uncoveredOf(draftState({ uncovered: open, position: free }))).toBe(open);
    expect(uncoveredOf(draftState({ position: free }))).toBeNull();
    expect(uncoveredOf(draftState({ uncovered: open, mode: 'test' }))).toBeNull();
    expect(uncoveredOf(draftState({ uncovered: open, position: position({ over: true }) }))).toBeNull();
    expect(uncoveredOf(draftState({ uncovered: open, position: position({ frontier: true }) }))).toBeNull();
    expect(uncoveredOf(draftState({ uncovered: open, waiting: { human: null, opponent: null } }))).toBeNull();
    const opponents = { ...open, seat: 'opponent' as const };
    expect(uncoveredOf(draftState({ uncovered: opponents }))).toBeNull();
    expect(
      uncoveredOf(draftState({ uncovered: opponents, waiting: { human: null, opponent: decision({ held: true }) } })),
    ).toBe(opponents);
  });

  it("is said in a guided scenario only where the script stopped at the player's decision", () => {
    const guided = { id: 'x', coverage: 'guided' as const };
    expect(uncoveredOf(draftState({ uncovered: open, draft: guided }))).toBeNull();
    expect(uncoveredOf(draftState({ uncovered: open, draft: guided, stopped: 'uncovered' }))).toBe(open);
  });

  it('says what is asked, what it offers, where the step goes, and the other ways', () => {
    const options = ['a', 'b', 'c', 'd', 'e', 'f'].map((term, i) => ({
      term: i >= 4 ? null : term,
      label: `L${term}`,
      alias: i === 5 ? null : i === 4 ? 'al' : null,
      id: null,
    }));
    const state = draftState({
      waiting: { human: decision({ options, where: 'turn 1, between_turns' }), opponent: null },
    });
    expect(uncoveredNotice(state, open)).toEqual({
      head: 'No step covers this decision.',
      asked:
        'You are being asked "Play Fellowship action or Pass" (turn 1, between turns). It offers: a, b, c, d, and 2 more, or Pass.',
      problem: null,
      how: 'It goes in after step 2.',
      or: {
        text: 'Or just answer it in the game.',
        title:
          'That writes the same step, with what you did as what it allows. Add the step first if it is to say something before it is answered.',
      },
    });
    const few = draftState({
      stopped: 'uncovered',
      recording: false,
      waiting: { human: decision({ options: options.slice(4), pass: undefined }), opponent: null },
    });
    const said = uncoveredNotice(few, { ...open, after: 0 });
    expect(said.head).toBe('The script stopped here: no step covers this decision.');
    expect(said.asked).toContain('. It offers: al, Lf.');
    expect(said.how).toBe('It goes in at the start of the script.');
    expect(said.or).toEqual({ text: '"Write down what I do" is off: answering it writes nothing.', title: '' });
    expect(uncoveredNotice(draftState(), open).asked).toContain('. It offers nothing but Pass.');
    expect(
      uncoveredNotice(draftState({ waiting: { human: decision({ pass: undefined }), opponent: null } }), open).asked,
    ).toMatch(/\)\.$/);
    // (what the decision is for, where a When can name it)
    const kinded = draftState({ waiting: { human: decision({ kind: 'nextSkirmish' }), opponent: null } });
    expect(uncoveredNotice(kinded, open).asked).toContain('(turn 1, fellowship; it is nextSkirmish)');
    const unknown = draftState({ waiting: { human: decision({ kind: 'oddity' }), opponent: null } });
    expect(uncoveredNotice(unknown, open).asked).toContain('(turn 1, fellowship).');
  });

  it('says where the step goes: into a block, in place of a blank step, or where a repeating step gives way', () => {
    const state = draftState();
    expect(uncoveredNotice(state, { ...open, block: 3, sub: 2 }).how).toBe(
      'It goes into the block the game is in, as step 3.2.',
    );
    expect(uncoveredNotice(state, { ...open, reuses: 4 }).how).toBe(
      'Step 4, which says nothing yet, becomes the step for it.',
    );
    expect(
      uncoveredNotice(state, { ...open, repeating: 2, until: 'phase(shadow)', becomes: 'phase(maneuver)' }).how,
    ).toBe(
      'It goes in after step 2. That step repeats until "phase(shadow)"; that becomes "phase(maneuver)", so that it ends where the new step begins.',
    );
    expect(uncoveredNotice(state, { ...open, repeating: 2 }).how).toContain('until ""; that becomes ""');
  });

  it("says when no step can be added, and the opponent's other way", () => {
    const opponents = draftState({ waiting: { human: null, opponent: decision({ seat: 'shadow', held: true }) } });
    const said = uncoveredNotice(opponents, { ...open, seat: 'opponent', problem: 'It is in a block.' });
    expect(said.asked).toMatch(/^The opponent is being asked/);
    expect(said.problem).toBe('A step cannot be added for it as things stand: It is in a block.');
    expect(said.how).toBeNull();
    expect(said.or.text).toBe('Or answer it under Opponent.');
    // (nothing asked after all: said as nothing)
    expect(uncoveredNotice(draftState({ waiting: { human: null, opponent: null } }), open).asked).toBe(
      'You are being asked "" ().',
    );
  });
});

describe('leftOutNotice', () => {
  const part = (extra: object) => ({
    where: 'step 3 (hello).allow',
    message: 'bad',
    was: 'x: 1',
    step: 3,
    uid: 's2',
    ...extra,
  });

  it('is nothing when nothing was left out', () => {
    expect(leftOutNotice(draftState())).toBeNull();
  });

  it("says each part by the step's number now, and the file's where that is another", () => {
    const notice = leftOutNotice(
      draftState({
        dirty: true,
        leftOut: [
          part({}),
          part({ uid: 's2', step: 2, where: 'step 2.allow' }),
          part({ uid: null, where: 'step 5 (gone)' }),
          part({ uid: null, step: 4, where: 'step 4.when' }),
          part({ uid: null, step: 0, where: 'cards' }),
        ],
      }),
    )!;
    expect(notice.head).toBe(
      '5 parts of the file were mistaken, and have been left out so that it could be opened. Saving writes the file without them.',
    );
    expect(notice.lines.map((line) => [line.where, line.step])).toEqual([
      ['step 2.allow (step 3 of the file)', 2],
      ['step 2.allow', 2],
      ['step 5 (gone) of the file, left out whole', null],
      ['step 4.when of the file', null],
      ['cards', null],
    ]);
    expect(notice.lines[0]).toMatchObject({ message: 'bad', was: 'x: 1' });
    expect(leftOutNotice(draftState({ leftOut: [part({ uid: 'gone' })] }))!.head).toBe(
      'One part of the file was mistaken, and has been left out so that it could be opened.',
    );
    expect(leftOutNotice(draftState({ dirty: true, leftOut: [part({})] }))!.head).toContain(' without it.');
  });
});

describe('the other notices', () => {
  it('says the latest of the problems not yet read, and how many more', () => {
    expect(problemsNotice(['a', 'b', 'c'], 1)).toBe('c (and 1 more before it)');
    expect(problemsNotice(['a', 'b', 'c'], 2)).toBe('c');
    expect(problemsNotice(['a'], 1)).toBeNull();
  });

  it('offers to answer a step again where the script stopped at it, with it in hand', () => {
    expect(mayRetake(draftState({ stopped: 'step' }))).toBe(true);
    expect(mayRetake(draftState({ stopped: 'uncovered' }))).toBe(false);
    expect(mayRetake(draftState({ stopped: 'step', position: position({ inHand: false }) }))).toBe(false);
    expect(mayRetake(draftState({ stopped: 'step', position: position({ over: true }) }))).toBe(false);
    expect(mayRetake(draftState({ stopped: 'step', draft: { id: 'x', steps: [{ pause: true }] } }))).toBe(false);
    expect(mayRetake(draftState({ stopped: 'step', position: position({ step: 9 }) }))).toBe(true);
  });

  it('says how a save went, and puts the whole of it in the report', () => {
    const saved: SaveReport = {
      path: 'tutorials/x.hjson',
      id: 'x',
      playable: true,
      verified: true,
      errors: [],
      warnings: [],
      report: null,
    };
    expect(savedNotice(saved, null)).toEqual({
      notice: { good: true, why: false, stays: false, text: 'Saved to tutorials/x.hjson. Scenario validated.' },
      report: 'Saved to tutorials/x.hjson.\nScenario validated.',
    });
    const moved = savedNotice(
      { ...saved, verified: false, report: 'Stops at 3.', warnings: ['w1', 'w2'], movedFrom: 'a.hjson' },
      null,
    );
    expect(moved.notice).toEqual({
      good: false,
      why: true,
      stays: false,
      text: 'Saved to tutorials/x.hjson, moved from a.hjson. It does not play through yet. 2 warnings.',
    });
    expect(moved.report).toBe(
      'Saved to tutorials/x.hjson (moved from a.hjson: that is where its kind is kept).\nIt does not play through yet:\nStops at 3.\n\nWarnings:\nw1\nw2',
    );
    const broken = savedNotice({ ...saved, errors: ['e1'], warnings: ['w'] }, 'Step 2 is not in the file.');
    expect(broken.notice.text).toBe(
      'Saved to tutorials/x.hjson. It cannot be played as it stands. 1 warning. Step 2 is not in the file.',
    );
    expect(broken.notice.good).toBe(false);
    expect(broken.report).toBe(
      'Saved to tutorials/x.hjson.\nIt cannot be played:\ne1\n\nWarnings:\nw\n\nStep 2 is not in the file.',
    );
    expect(savedNotice({ ...saved, verified: false }, null).report).toBe(
      'Saved to tutorials/x.hjson.\nIt does not play through yet:\n',
    );
  });
});
