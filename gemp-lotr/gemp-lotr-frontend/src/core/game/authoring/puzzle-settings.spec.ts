import { draftState, puzzleState } from '../../../testing/authoring';
import { Draft } from './authoring-state';
import {
  filterValue,
  judgeAt,
  kindChoices,
  momentTakesPhase,
  optionsWithTool,
  optionsWithUndo,
  outcomeTextWithScore,
  outcomeWithJudge,
  outcomeWithScore,
  puzzleLike,
  readsCard,
  rulesWithForbid,
  scoreForm,
  scoreValue,
  toolsValue,
  toolTicks,
  undoByKind,
  undoOptions,
  verdictText,
  optionsWithOtherSeat,
  otherSeatOptions,
} from './puzzle-settings';
import { SandboxTools } from './puzzle-state';
import { rulesWithSeat } from './scenario-settings';

const TOOLS: SandboxTools = {
  names: ['add', 'copy', 'move', 'modify', 'counters'],
  notBuilt: ['modify'],
  said: true,
  allowed: ['add', 'counters'],
};

const draft = (extra: Partial<Draft> = {}): Draft => ({ id: 'test', steps: [], ...extra });

describe('puzzle-settings: kinds and a sandbox’s tools (es 4)', () => {
  it('offers the kinds the server names, or the three', () => {
    expect(kindChoices(draftState())).toEqual(['tutorial', 'puzzle', 'sandbox']);
    expect(kindChoices(draftState({ kinds: ['tutorial', 'sandbox'] }))).toEqual(['tutorial', 'sandbox']);
  });

  it('ticks the tools in force, and shows the ones not built', () => {
    expect(toolTicks(TOOLS)).toEqual([
      { name: 'add', on: true, notBuilt: false },
      { name: 'copy', on: false, notBuilt: false },
      { name: 'move', on: false, notBuilt: false },
      { name: 'modify', on: false, notBuilt: true },
      { name: 'counters', on: true, notBuilt: false },
    ]);
  });

  it('writes the tools in the server’s order; every one ticked is the field left out, none is false', () => {
    expect(toolsValue(TOOLS, 'move', true)).toBe('add, move, counters');
    expect(toolsValue(TOOLS, 'add', false)).toBe('counters');
    expect(toolsValue({ ...TOOLS, allowed: ['add'] }, 'add', false)).toBe(false);
    const all = { ...TOOLS, allowed: ['add', 'copy', 'move', 'modify'] };
    expect(toolsValue(all, 'counters', true)).toBeNull();
  });

  it('sends the options it has with the tools changed (es: the whole options object)', () => {
    const options = { autoPass: 'fellowship', tools: 'add, counters' };
    expect(optionsWithTool(draft({ options }), TOOLS, 'move', true)).toEqual({
      autoPass: 'fellowship',
      tools: 'add, move, counters',
    });
    expect(optionsWithTool(draft({ options: { tools: 'add' } }), { ...TOOLS, allowed: ['add'] }, 'add', false)).toEqual(
      { tools: false },
    );
    const every = { ...TOOLS, allowed: ['add', 'copy', 'move', 'modify'] };
    expect(
      optionsWithTool(draft({ options: { tools: 'add, copy, move, modify' } }), every, 'counters', true),
    ).toBeNull();
  });
});

describe('puzzle-settings: take-backs (ep P6)', () => {
  it('says what the kind gives: off in a puzzle, on otherwise', () => {
    expect(undoByKind('puzzle')).toBe('off');
    expect(undoByKind('tutorial')).toBe('on');
    expect(undoByKind(undefined)).toBe('on');
    expect(undoOptions('puzzle').map(([value, label]) => [value, label])).toEqual([
      ['', 'default (off)'],
      ['on', 'on'],
      ['off', 'off'],
    ]);
    expect(undoOptions('sandbox')[0]![1]).toBe('default (on)');
  });

  it('writes undo on or off, and leaves it out for the default', () => {
    expect(optionsWithUndo(draft({ options: { autoAccept: false } }), 'off')).toEqual({
      autoAccept: false,
      undo: 'off',
    });
    expect(optionsWithUndo(draft(), 'on')).toEqual({ undo: 'on' });
    expect(optionsWithUndo(draft({ options: { undo: 'on', autoAccept: false } }), '')).toEqual({ autoAccept: false });
    expect(optionsWithUndo(draft({ options: { undo: 'on' } }), '')).toBeNull();
  });
});

describe('puzzle-settings: the judge (ep P1)', () => {
  it('writes the moment as the file does', () => {
    expect(judgeAt({ moment: 'endOf', phase: 'archery', turn: null })).toBe('endOf(archery)');
    expect(judgeAt({ moment: 'startOf', phase: 'regroup', turn: 2 })).toBe('startOf(regroup), turn(2)');
    expect(judgeAt({ moment: 'endOfTurn', phase: 'archery', turn: null })).toBe('endOfTurn');
    expect(judgeAt({ moment: 'skirmishResolution', phase: '', turn: 3 })).toBe('skirmishResolution, turn(3)');
    expect(judgeAt({ moment: '', phase: 'archery', turn: 1 })).toBeNull();
    expect(momentTakesPhase('endOf')).toBe(true);
    expect(momentTakesPhase('endOfTurn')).toBe(false);
  });

  const WIN = [{ type: 'canSpot', filter: 'alias(frodo)' }];
  const OUTCOME = { winMessages: ['Won.'], judge: { at: 'endOf(archery)', win: WIN } };

  it('changes the moment and keeps the requirements and the rest of the outcome as the draft has them', () => {
    expect(outcomeWithJudge(draft({ outcome: OUTCOME }), 'startOf(regroup)')).toEqual({
      winMessages: ['Won.'],
      judge: { at: 'startOf(regroup)', win: WIN },
    });
    // (a judge with no requirements is the moment alone)
    expect(outcomeWithJudge(draft({ outcome: { judge: 'endOfTurn' } }), 'endOf(archery)')).toEqual({
      judge: 'endOf(archery)',
    });
  });

  it('sends requirements typed as HJSON text, and none as the moment alone', () => {
    const text = '{\n\ttype: canSpot\n\tfilter: alias(frodo)\n}';
    expect(outcomeWithJudge(draft(), 'startOf(regroup)', text)).toEqual({
      judge: { at: 'startOf(regroup)', win: text },
    });
    expect(outcomeWithJudge(draft({ outcome: OUTCOME }), 'endOf(archery)', '  ')).toEqual({
      winMessages: ['Won.'],
      judge: 'endOf(archery)',
    });
  });

  it('takes the judge out, and the outcome with it when nothing is left', () => {
    expect(outcomeWithJudge(draft({ outcome: OUTCOME }), null)).toEqual({ winMessages: ['Won.'] });
    expect(outcomeWithJudge(draft({ outcome: { judge: 'endOfTurn' } }), null)).toBeNull();
  });
});

describe('puzzle-settings: the score (ep P7)', () => {
  it('reads the state’s score into its row', () => {
    expect(scoreForm(null)).toEqual({ what: '', of: '', better: 'higher', label: '', text: '' });
    expect(
      scoreForm({ value: 'strength:smeagol', valueText: null, better: 'higher', label: 'Sméagol’s strength' }),
    ).toEqual({ what: 'strength', of: 'smeagol', better: 'higher', label: 'Sméagol’s strength', text: '' });
    expect(scoreForm({ value: '{burdens}', valueText: null, better: 'lower', label: null })).toEqual({
      what: 'burdens',
      of: '',
      better: 'lower',
      label: '',
      text: '',
    });
    const custom = '{\n\ttype: forEachWound\n\tfilter: companion\n}';
    expect(scoreForm({ value: null, valueText: custom, better: 'higher', label: 'Wounds' }).what).toBe('custom');
    expect(scoreForm({ value: null, valueText: custom, better: 'higher', label: null }).text).toBe(custom);
    expect(scoreForm({ value: null, valueText: null, better: 'higher', label: null }).text).toBe('');
    expect(readsCard('strength', puzzleState().scoreValues.perCard)).toBe(true);
    expect(readsCard('burdens', puzzleState().scoreValues.perCard)).toBe(false);
  });

  it('writes the value without braces, better only when lower, the label when there is one (the notes’ patch)', () => {
    const form = { what: 'wounds', of: 'aragorn', better: 'lower' as const, label: 'Wounds', text: '' };
    expect(outcomeWithScore(draft({ outcome: { judge: 'endOfTurn' } }), form)).toEqual({
      judge: 'endOfTurn',
      score: { value: 'wounds:aragorn', better: 'lower', label: 'Wounds' },
    });
    expect(scoreValue(draft(), { what: 'burdens', of: ' ', better: 'higher', label: ' ', text: '' })).toEqual({
      value: 'burdens',
    });
    expect(scoreValue(draft(), { what: 'hand', of: 'human', better: 'higher', label: '', text: '' })).toEqual({
      value: 'hand:human',
    });
  });

  it('keeps a custom value as the draft has it, and takes the score out', () => {
    const value = { type: 'forEachWound', filter: 'companion' };
    const had = draft({ outcome: { score: { value }, winMessages: ['Won.'] } });
    expect(outcomeWithScore(had, { what: 'custom', of: '', better: 'higher', label: 'Wounds', text: 'x' })).toEqual({
      score: { value, label: 'Wounds' },
      winMessages: ['Won.'],
    });
    expect(scoreValue(draft(), { what: 'custom', of: '', better: 'higher', label: '', text: '' })).toEqual({
      value: '',
    });
    expect(outcomeWithScore(had, { what: '', of: '', better: 'higher', label: '', text: '' })).toEqual({
      winMessages: ['Won.'],
    });
    expect(outcomeWithScore(draft({ outcome: { score: { value: 'twilight' } } }), scoreForm(null))).toBeNull();
  });

  it('writes a custom value typed as HJSON into the outcome’s text, the rest of the outcome as JSON', () => {
    const had = draft({ outcome: { judge: 'endOfTurn', score: { value: 'twilight' }, winMessages: ['Won "it".'] } });
    const text = outcomeTextWithScore(had, {
      what: 'custom',
      of: '',
      better: 'lower',
      label: 'Wounds',
      text: '{\n\ttype: forEachWound\n\tfilter: companion\n}\n',
    });
    expect(text).toBe(
      '{\n"judge": "endOfTurn"\n"winMessages": ["Won \\"it\\"."]\n"score": {\n"value": {\n\ttype: forEachWound\n\tfilter: companion\n}\n"better": "lower"\n"label": "Wounds"\n}\n}',
    );
    expect(
      outcomeTextWithScore(draft(), { what: 'custom', of: '', better: 'higher', label: '', text: '{ a: 1 }' }),
    ).toBe('{\n"score": {\n"value": { a: 1 }\n}\n}');
  });
});

describe('puzzle-settings: forbid and the pool (ep P10, P2)', () => {
  it('writes what is forbidden in the server’s order, every other rule as it is', () => {
    const choices = ['shuffle', 'reveal'];
    expect(rulesWithForbid(draft({ rules: { oneSided: true } }), choices, [], 'reveal', true)).toEqual({
      oneSided: true,
      forbid: 'reveal',
    });
    expect(rulesWithForbid(draft({ rules: { forbid: 'reveal' } }), choices, ['reveal'], 'shuffle', true)).toEqual({
      forbid: 'shuffle, reveal',
    });
    expect(
      rulesWithForbid(draft({ rules: { oneSided: true, forbid: 'shuffle' } }), choices, ['shuffle'], 'shuffle', false),
    ).toEqual({ oneSided: true });
    expect(rulesWithForbid(draft({ rules: { forbid: 'shuffle' } }), choices, ['shuffle'], 'shuffle', false)).toBeNull();
  });

  it('keeps forbid when a seat’s rules are changed and nothing else is left', () => {
    expect(rulesWithSeat(draft({ rules: { forbid: 'shuffle' } }), 'freeps', { hand: 0 })).toEqual({
      forbid: 'shuffle',
    });
  });

  it('takes a blank filter as none', () => {
    expect(filterValue('  set:1-6 ')).toBe('set:1-6');
    expect(filterValue('   ')).toBeNull();
  });

  it('shows a puzzle’s forms for a puzzle, or a draft with a puzzle’s parts', () => {
    expect(puzzleLike(draft())).toBe(false);
    expect(puzzleLike(draft({ kind: 'puzzle' }))).toBe(true);
    expect(puzzleLike(draft({ pool: 'set:1' }))).toBe(true);
    expect(puzzleLike(draft({ slots: { a: 'hand' } }))).toBe(true);
    expect(puzzleLike(draft({ solutions: [{ name: 'a' }] }))).toBe(true);
    expect(puzzleLike(draft({ solutions: [] }))).toBe(false);
  });

  it('says what a filter finds, or that it was refused', () => {
    expect(verdictText({ kind: 'count', total: 57 })).toEqual({ text: '57 cards', tone: 'good' });
    expect(verdictText({ kind: 'count', total: 1 })).toEqual({ text: '1 card', tone: 'good' });
    expect(verdictText({ kind: 'count', total: 0 })).toEqual({ text: 'no card', tone: 'error' });
    expect(verdictText({ kind: 'refused', why: 'no' })).toEqual({ text: 'refused', tone: 'error' });
    expect(verdictText({ kind: 'busy' })).toEqual({ text: 'looking…', tone: 'quiet' });
    expect(verdictText({ kind: 'none' })).toBeNull();
  });

  it("sets who plays a sandbox's other seat: the script is the key left out (ef-notes.md A)", () => {
    const draft: Draft = { ...draftState().draft, kind: 'sandbox', options: { tools: 'add', otherSeat: 'bot' } };
    expect(optionsWithOtherSeat(draft, 'person')).toEqual({ tools: 'add', otherSeat: 'person' });
    expect(optionsWithOtherSeat(draft, 'script')).toEqual({ tools: 'add' });
    expect(optionsWithOtherSeat({ ...draft, options: { otherSeat: 'bot' } }, 'script')).toBeNull();
    expect(otherSeatOptions(['script', 'bot', 'person', 'odd']).map((one) => one.slice(0, 2))).toEqual([
      ['script', 'script'],
      ['bot', 'bot'],
      ['person', 'you'],
      ['odd', 'odd'],
    ]);
  });
});
