import { asText, asTextOrNull, orIfEmpty } from './json-values';

describe("reading the server's JSON values", () => {
  it('takes strings, numbers and booleans as text, anything else as the fallback', () => {
    expect(asText('Gondor')).toBe('Gondor');
    expect(asText(12)).toBe('12');
    expect(asText(false)).toBe('false');
    expect(asText(null)).toBe('');
    expect(asText({ a: 1 }, '?')).toBe('?');
    expect(asText(undefined, '-')).toBe('-');
  });

  it('gives null for a missing value, and the fallback for empty text', () => {
    expect(asTextOrNull(undefined)).toBeNull();
    expect(asTextOrNull(null)).toBeNull();
    expect(asTextOrNull(3)).toBe('3');
    expect(orIfEmpty('', 'none')).toBe('none');
    expect(orIfEmpty(null, 'none')).toBe('none');
    expect(orIfEmpty('x', 'none')).toBe('x');
  });
});
