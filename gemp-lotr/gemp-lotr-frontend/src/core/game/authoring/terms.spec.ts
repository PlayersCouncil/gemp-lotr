import {
  askedNamed,
  askedOf,
  askedTerm,
  askedText,
  askedWords,
  nextUntil,
  pickersOf,
  putAsked,
  putTerm,
  rank,
  splitTerms,
  untilFromNext,
  whenParts,
  writtenInFull,
} from './terms';

describe('splitTerms', () => {
  it('splits at the commas that are not inside parentheses, trimmed, blanks dropped', () => {
    expect(splitTerms('freeps, action , phase(fellowship), choose(a, b(c, d)),, ')).toEqual([
      'freeps',
      'action',
      'phase(fellowship)',
      'choose(a, b(c, d))',
    ]);
    expect(splitTerms('')).toEqual([]);
    expect(splitTerms(null)).toEqual([]);
    expect(splitTerms(undefined)).toEqual([]);
    expect(splitTerms('a)), b')).toEqual(['a))', 'b']);
  });
});

describe('whenParts', () => {
  it('reads a When with requirements as its terms and its requirements', () => {
    const require = [{ type: 'canSpot', filter: 'alias(frodo)' }];
    expect(
      whenParts({
        terms: 'once',
        seat: 'freeps',
        decision: 'action',
        phase: 'fellowship',
        turn: 2,
        fighting: 'frodo',
        require,
      }),
    ).toEqual({
      terms: 'once, freeps, action, phase(fellowship), turn(2), inSkirmish(frodo)',
      require: JSON.stringify(require, null, '\t'),
    });
    expect(whenParts({ text: 'next skirmish', skirmish: '', require: { type: 'x' } })?.terms).toBe(
      'text(next skirmish)',
    );
  });

  it('is nothing for a line of terms, a list, a When without requirements, or one terms cannot say', () => {
    expect(whenParts('freeps, action')).toBeNull();
    expect(whenParts(undefined)).toBeNull();
    expect(whenParts(null)).toBeNull();
    expect(whenParts([{ type: 'x' }])).toBeNull();
    expect(whenParts({ seat: 'freeps' })).toBeNull();
    expect(whenParts({ seat: 'freeps', require: { type: 'x' }, odd: 1 })).toBeNull();
  });
});

describe('the When composer', () => {
  it('tells a When written out in full from a line of terms', () => {
    expect(writtenInFull('{ seat: freeps }')).toBe(true);
    expect(writtenInFull('  [ {type: x} ]')).toBe(true);
    expect(writtenInFull('when: freeps')).toBe(true);
    expect(writtenInFull('freeps, action')).toBe(false);
    expect(writtenInFull(undefined)).toBe(false);
  });

  it('reads the pickers off a line of terms, and has none for one written out in full', () => {
    expect(pickersOf('Freeps, ACTION, phase(shadow), turn(3), text(x)')).toEqual({
      seat: 'freeps',
      kind: 'action',
      phase: 'shadow',
      turn: '3',
      role: '',
    });
    expect(pickersOf('actionchoice')).toEqual({ seat: '', kind: 'actionChoice', phase: '', turn: '', role: '' });
    expect(pickersOf(undefined)).toEqual({ seat: '', kind: '', phase: '', turn: '', role: '' });
    expect(pickersOf('{ seat: freeps }')).toBeNull();
  });

  it("puts a picker's term in its place, keeps the other terms, and orders the line as a person says it", () => {
    expect(putTerm('text(x), phase(shadow), freeps', 'turn', '2')).toBe('freeps, phase(shadow), turn(2), text(x)');
    expect(putTerm('freeps, action', 'seat', 'shadow')).toBe('shadow, action');
    expect(putTerm('freeps, action, phase(shadow)', 'phase', '')).toBe('freeps, action');
    expect(putTerm('skirmish(1), freeps', 'kind', 'assign')).toBe('freeps, assign, skirmish(1)');
    expect(putTerm('', 'phase', 'between_turns')).toBe('phase(between_turns)');
    expect([rank('human'), rank('cards'), rank('phase(x)'), rank('turn(1)'), rank('skirmish(2)'), rank('x')]).toEqual([
      0, 1, 2, 3, 4, 5,
    ]);
  });

  it('guesses what a step that repeats goes on until: the next phase, else the next turn', () => {
    expect(nextUntil('freeps, phase(fellowship), turn(2)')).toBe('phase(shadow)');
    expect(nextUntil('freeps, phase(regroup), turn(2)')).toBe('turn(3)');
    expect(nextUntil('phase(nowhere)')).toBe('');
    expect(nextUntil(null)).toBe('');
  });

  it('takes what a step repeats until from the next step that waits for something else', () => {
    const steps = [{ when: 'a' }, { when: 'a' }, {}, { when: { seat: 'x' } }];
    expect(untilFromNext(steps, 0)).toBe('{\n\t"seat": "x"\n}');
    expect(untilFromNext([{ when: 'a' }, { when: 'b' }], 0)).toBe('b');
    expect(untilFromNext(steps, 3)).toBeNull();
    expect(untilFromNext(steps, -1)).toBeNull();
    expect(untilFromNext(steps, 9)).toBeNull();
  });
});

describe('what is being asked', () => {
  it('reads a term that says what the decision is for, or is not, in any case', () => {
    expect(askedTerm('nextSkirmish')).toEqual({ asked: 'nextSkirmish', not: false });
    expect(askedTerm(' NOT( reconcile ) ')).toEqual({ asked: 'reconcile', not: true });
    expect(askedTerm('not(cards)')).toBeNull();
    expect(askedTerm('cards')).toBeNull();
    expect(askedTerm('phase(skirmish)')).toBeNull();
    expect(askedNamed('ARCHERYWOUND')).toBe('archeryWound');
    expect(askedNamed(null)).toBeNull();
    expect(askedText('sanctuary', true)).toBe('not(sanctuary)');
    expect(askedText('sanctuary', false)).toBe('sanctuary');
    expect(askedWords('not(nextSkirmish)')).toBe('not next skirmish');
    expect(askedWords('anotherMove')).toBe('another move?');
    expect(askedWords('turn(2)')).toBeNull();
  });

  it('shows the first such term, nothing for a line without one, and has no picker for a When in full', () => {
    expect(askedOf('freeps, cards, not(nextSkirmish), phase(skirmish), reconcile')).toEqual({
      asked: 'nextSkirmish',
      not: true,
    });
    expect(askedOf('freeps, cards')).toEqual({ asked: '', not: false });
    expect(askedOf(undefined)).toEqual({ asked: '', not: false });
    expect(askedOf('{ seat: freeps }')).toBeNull();
  });

  it("puts it in where the server writes a new step's When: after the kind of decision, before the phase", () => {
    expect(putAsked('freeps, cards, phase(skirmish), turn(1)', 'nextSkirmish', false)).toBe(
      'freeps, cards, nextSkirmish, phase(skirmish), turn(1)',
    );
    expect(putAsked('freeps, cards, nextSkirmish, after(gandalf)', 'nextSkirmish', true)).toBe(
      'freeps, cards, not(nextSkirmish), after(gandalf)',
    );
    // (one at a time: any other such term goes; '' takes it out)
    expect(putAsked('turn(2), reconcile, not(sanctuary), freeps', 'anotherMove', false)).toBe(
      'freeps, anotherMove, turn(2)',
    );
    expect(putAsked('freeps, choice, anotherMove', '', true)).toBe('freeps, choice');
    expect(putAsked('', 'chooseSite', false)).toBe('chooseSite');
    expect(rank('not(reconcile)')).toBe(1.5);
    // (the other pickers keep it in its place)
    expect(putTerm('freeps, cards, nextSkirmish', 'phase', 'skirmish')).toBe(
      'freeps, cards, nextSkirmish, phase(skirmish)',
    );
  });

  it('reads the kind of a When written out with requirements as its terms', () => {
    expect(whenParts({ seat: 'freeps', kind: 'not(reconcile)', require: { type: 'x' } })?.terms).toBe(
      'freeps, not(reconcile)',
    );
    expect(whenParts({ seat: 'freeps', kind: '', require: { type: 'x' } })?.terms).toBe('freeps');
  });
});
