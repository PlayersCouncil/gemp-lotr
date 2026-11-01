import { toFormParams } from './form-codec';

describe('toFormParams', () => {
  it('encodes reserved characters the way jQuery did (spaces aside)', () => {
    expect(toFormParams({ notes: "a+b c&d=e/f?g#h;i,j:k@l$m'n" }).toString()).toBe(
      "notes=a%2Bb%20c%26d%3De%2Ff%3Fg%23h%3Bi%2Cj%3Ak%40l%24m'n",
    );
  });

  it('keeps unicode and newlines intact', () => {
    const params = toFormParams({ decklist: 'Sméagol\n2x Bill' });
    expect(params.toString()).toBe('decklist=Sm%C3%A9agol%0A2x%20Bill');
    expect(params.get('decklist')).toBe('Sméagol\n2x Bill');
  });

  it('stringifies numbers and booleans and leaves out null/undefined', () => {
    expect(toFormParams({ start: 0, includeEvents: false, a: null, b: undefined }).toString()).toBe(
      'start=0&includeEvents=false',
    );
  });
});
