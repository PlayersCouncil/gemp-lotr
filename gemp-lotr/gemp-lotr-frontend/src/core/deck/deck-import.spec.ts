import { formatDecklist, removeNotes } from './deck-import';

describe('decklist import', () => {
  it('removes notes in parentheses and brackets', () => {
    expect(removeNotes('1x Gandalf (the grey) x')).toBe('1x Gandalf  x');
    expect(removeNotes('2 Ulaire Enquea [lots of text]')).toBe('2 Ulaire Enquea ');
    expect(removeNotes('a (b) c (d) e')).toBe('a  c  e');
    expect(removeNotes('(note) Gimli')).toBe(' Gimli');
    expect(removeNotes('Gimli (unfinished')).toBe('Gimli ');
  });

  it('stops on brackets it cannot remove (the legacy code recursed until the browser gave up)', () => {
    expect(removeNotes('(')).toBe('(');
    expect(removeNotes(')(')).toBe(')');
  });

  it('builds the request the legacy page sent for the same text', () => {
    const text =
      "1x The One Ring, Isildur's Bane (ring)\nRing-bearer: Frodo, Son of Drogo\n* 3x Gandalf's Pipe\n" +
      '2 Ulaire Enquea [lots of text]\nstarting: Bag End\n2x Bill the Pony (fp)\n1x Weathertop\n';
    expect(formatDecklist(text)).toBe(
      "1x the one ring, isildur's bane~frodo, son of drogo~3x gandalf's pipe~2 ulaire enquea~: bag end~" +
        '2x bill the pony~1x weathertop~',
    );
  });
});
