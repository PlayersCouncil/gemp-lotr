export const DECK_NAME_LENGTH_MESSAGE = 'Deck name has to have at least 3 characters and at most 100 characters.';

export function isValidDeckName(name: string): boolean {
  return name.length >= 3 && name.length <= 100;
}
