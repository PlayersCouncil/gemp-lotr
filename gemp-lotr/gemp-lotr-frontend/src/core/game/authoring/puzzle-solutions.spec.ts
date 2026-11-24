import { puzzleState } from '../../../testing/authoring';
import { Draft } from './authoring-state';
import { PuzzleSolution } from './puzzle-state';
import {
  answerLines,
  answersCount,
  freshSolutionName,
  picksLine,
  recordPicks,
  reportVerdicts,
  solutionNameProblem,
  solutionsWith,
  solvedBy,
  verdictLine,
} from './puzzle-solutions';

const PUBLISHED: PuzzleSolution = {
  name: 'published',
  note: 'Parapet, then Aldor.',
  picks: { condition: '5_87', ally: '4_262' },
  pickNames: { condition: 'Parapet', ally: 'Aldor, Soldier of Edoras' },
  answers: ['use(condition)', 'choose(ally)'],
  text: '',
};

const DRAFT: Draft = {
  id: 'test',
  solutions: [
    { name: 'published', note: 'Parapet, then Aldor.', picks: { condition: '5_87', ally: '4_262' }, answers: ['a'] },
    { name: 'other', picks: { condition: '5_79', ally: '4_262' } },
  ],
};

describe('puzzle-solutions (ep P4)', () => {
  it('checks a name, and makes a fresh one', () => {
    expect(solutionNameProblem('dagger', ['published'])).toBeNull();
    expect(solutionNameProblem('2nd', [])).toMatch(/^Letters/);
    expect(solutionNameProblem('published', ['published'])).toMatch(/already/);
    expect(solutionNameProblem('published', ['published'], 'published')).toBeNull();
    expect(freshSolutionName([])).toBe('solution1');
    expect(freshSolutionName(['published', 'solution2'])).toBe('solution3');
    expect(freshSolutionName(['solution2'])).toBe('solution3');
  });

  it('reads what a Check’s report says of each solution', () => {
    const report = [
      'Scenario x: PASSED (proved: solution, required: solution)',
      'Decisions:',
      '  1. [step 1] Play action or Pass',
      ' Solution published: solved (judged at endOf(archery): won)',
      ' Solution wrong: the solution does not fit the game',
    ].join('\n');
    const verdicts = reportVerdicts(report);
    expect([...verdicts]).toEqual([
      ['published', 'solved (judged at endOf(archery): won)'],
      ['wrong', 'the solution does not fit the game'],
    ]);
    expect(reportVerdicts(null).size).toBe(0);
    expect(solvedBy('solved (x)')).toBe(true);
    expect(solvedBy('the solution does not fit')).toBe(false);
  });

  it('says the game in hand’s verdict for the solution it is played with', () => {
    const judged = puzzleState({
      playingWith: 'published',
      verdict: { won: true, score: 42, moment: 'endOf(archery)' },
    });
    expect(verdictLine(judged, 'published')).toBe('won at endOf(archery), scoring 42');
    expect(verdictLine(judged, 'other')).toBeNull();
    const failed = puzzleState({ playingWith: 'a', verdict: { won: false, score: null, moment: 'endOfTurn' } });
    expect(verdictLine(failed, 'a')).toBe('failed at endOfTurn');
    expect(verdictLine(puzzleState({ playingWith: 'a' }), 'a')).toBeNull();
  });

  it('says its picks by name, and how many answers it has', () => {
    expect(picksLine(PUBLISHED)).toBe('condition: Parapet, ally: Aldor, Soldier of Edoras');
    expect(picksLine({ ...PUBLISHED, pickNames: {} })).toBe('condition: 5_87, ally: 4_262');
    expect(answersCount(PUBLISHED)).toBe('2 answers');
    expect(answersCount({ answers: ['pass'] })).toBe('1 answer');
  });

  it('sends Record no picks when they are the game in hand’s, else every slot’s', () => {
    const inHand = { condition: '5_87', ally: '4_262' };
    expect(recordPicks(['condition', 'ally'], inHand, {})).toBeNull();
    expect(recordPicks(['condition', 'ally'], inHand, { condition: '5_87' })).toBeNull();
    expect(recordPicks(['condition', 'ally'], inHand, { condition: '5_79' })).toEqual({
      condition: '5_79',
      ally: '4_262',
    });
    // (a game played with its slots empty: what is chosen, the empty ones left for the server to say)
    expect(recordPicks(['condition', 'ally'], {}, { ally: '4_262' })).toEqual({ ally: '4_262' });
    expect(recordPicks([], {}, {})).toBeNull();
  });

  it('sends every solution as the file has it, with one changed', () => {
    expect(solutionsWith(DRAFT, 'published', { name: 'key' })[0]).toEqual({ ...DRAFT.solutions![0], name: 'key' });
    expect(solutionsWith(DRAFT, 'published', { note: ' ' })[0]).toEqual({
      name: 'published',
      picks: { condition: '5_87', ally: '4_262' },
      answers: ['a'],
    });
    expect(solutionsWith(DRAFT, 'other', { note: ' A try ' })[1]).toEqual({ ...DRAFT.solutions![1], note: 'A try' });
    expect(solutionsWith(DRAFT, 'other', { pick: { slot: 'condition', blueprint: '5_87' } })).toEqual([
      DRAFT.solutions![0],
      { name: 'other', picks: { condition: '5_87', ally: '4_262' } },
    ]);
    expect(solutionsWith(DRAFT, 'other', { answers: answerLines(' use(condition)\n\n choose(ally) \n') })[1]).toEqual({
      ...DRAFT.solutions![1],
      answers: ['use(condition)', 'choose(ally)'],
    });
    expect(solutionsWith({ id: 'x' }, 'a', { name: 'b' })).toEqual([]);
  });
});
