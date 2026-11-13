import { DECK_NAME_LENGTH_MESSAGE, isValidDeckName } from './deck-name';

describe('isValidDeckName', () => {
  it.each([
    ['', false],
    ['ab', false],
    ['abc', true],
    ['x'.repeat(100), true],
    ['x'.repeat(101), false],
  ])('%j -> %s', (name, valid) => {
    expect(isValidDeckName(name)).toBe(valid);
  });

  it('has the message the player sees', () => {
    expect(DECK_NAME_LENGTH_MESSAGE).toBe('Deck name has to have at least 3 characters and at most 100 characters.');
  });
});
