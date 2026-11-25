import { listOf, parseJson, stringsOf } from './json';

describe('reading unknown JSON', () => {
  it('parses JSON text, and throws on text that is not JSON', () => {
    expect(parseJson('{"a":[1,"b"]}')).toEqual({ a: [1, 'b'] });
    expect(parseJson('null')).toBeNull();
    expect(() => parseJson('<html>')).toThrow(SyntaxError);
  });

  it('gives a list as it is, and an empty list for anything else', () => {
    const list = [1, 'two', null];
    expect(listOf(list)).toBe(list);
    for (const other of [undefined, null, 'text', 3, { length: 2 }]) {
      expect(listOf(other)).toEqual([]);
    }
  });

  it('keeps only the strings of a list', () => {
    expect(stringsOf(['a', 1, null, 'b', ['c']])).toEqual(['a', 'b']);
    expect(stringsOf('ab')).toEqual([]);
    expect(stringsOf(undefined)).toEqual([]);
  });
});
