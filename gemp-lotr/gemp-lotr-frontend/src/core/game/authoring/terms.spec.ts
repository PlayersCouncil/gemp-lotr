import { splitTerms, whenParts } from './terms';

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
