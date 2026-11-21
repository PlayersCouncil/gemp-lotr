import { readServerResponse } from '../../../../testing/files';
import { parseErrataCatalog } from './errata-catalog';
import { diffSides, diffTexts, errataReadout, plainText, statChanges, statText, tokens } from './errata-diff';
import { ErrataSide } from './errata-models';

const side = (stats: ErrataSide['stats'], extra: Partial<ErrataSide> = {}): ErrataSide => ({
  name: 'A',
  type: 'Companion',
  stats,
  gametext: '',
  ...extra,
});

describe('errata diff', () => {
  const { entries } = parseErrataCatalog(JSON.parse(readServerResponse('errata.json')));

  it('splits a text into words, spaces and marks, without tags (tokens)', () => {
    expect(tokens('Exert <b>Gandalf</b>’s pipe, twice.')).toEqual([
      'Exert',
      ' ',
      'Gandalf’s',
      ' ',
      'pipe',
      ',',
      ' ',
      'twice',
      '.',
    ]);
    expect(tokens('Éowyn')).toEqual(['Éowyn']);
    expect(tokens(null)).toEqual([]);
    expect(tokens('a&nbsp;b &amp; c')).toEqual(['a', ' ', 'b', ' ', '&', ' ', 'c']);
  });

  it('diffs word by word, a change across spaces read as one (diff)', () => {
    expect(diffTexts('Exert Gandalf twice to play', 'Exert Gandalf to play')).toEqual([
      { op: '=', text: 'Exert Gandalf ' },
      { op: '-', text: 'twice ' },
      { op: '=', text: 'to play' },
    ]);
    expect(diffTexts('a b c d', 'x b y d')).toEqual([
      { op: '-', text: 'a' },
      { op: '+', text: 'x' },
      { op: '=', text: ' b ' },
      { op: '-', text: 'c' },
      { op: '+', text: 'y' },
      { op: '=', text: ' d' },
    ]);
    // "~~a~~ ~~c~~" with only a space between: one change
    expect(diffTexts('a c d', 'x y d')).toEqual([
      { op: '-', text: 'a c' },
      { op: '+', text: 'x y' },
      { op: '=', text: ' d' },
    ]);
    expect(diffTexts('', 'new')).toEqual([{ op: '+', text: 'new' }]);
    expect(diffTexts('same', 'same')).toEqual([{ op: '=', text: 'same' }]);
  });

  it('gives each side its text: the old one plain, the new one with its additions (sides, plain)', () => {
    const ops = diffTexts('draw deck', 'discard pile');
    const sides = diffSides(ops);
    expect(plainText(sides.old)).toBe('draw deck');
    expect(plainText(sides.new)).toBe('discard pile');
    expect(sides.new.every((op) => op.op !== '-')).toBe(true);
  });

  it('writes stats as the readout does (statText)', () => {
    expect([statText(undefined), statText(null), statText(true), statText(false), statText(4)]).toEqual([
      '—',
      '—',
      'yes',
      'no',
      '4',
    ]);
  });

  it('lists the name, the type, then the stats that changed, in order (statChanges)', () => {
    expect(
      statChanges(
        side({ Strength: 7, Twilight: 4, Unique: true, Extra: 1 }, { name: 'Lorien Elf', type: 'Event' }),
        side({ Twilight: 3, Strength: 7, Unique: false, Vitality: 2 }, { name: 'Lórien Elf', type: 'Condition' }),
      ),
    ).toEqual([
      { name: 'Type', from: 'Event', to: 'Condition' },
      { name: 'Twilight', from: '4', to: '3' },
      { name: 'Vitality', from: '—', to: '2' },
      { name: 'Unique', from: 'yes', to: 'no' },
      { name: 'Extra', from: '1', to: '—' },
    ]);
    expect(statChanges(side({}, { name: 'A' }), side({}, { name: 'B' }))).toEqual([
      { name: 'Name', from: 'A', to: 'B' },
    ]);
    expect(statChanges(null, side({}))).toEqual([]);
  });

  it('reads out a changed card as Old and New (note)', () => {
    const gandalf = errataReadout(entries[0]!);
    expect(gandalf.stats).toEqual([{ name: 'Strength', from: '7', to: '6' }]);
    expect(gandalf.texts.map((text) => [text.side, text.label])).toEqual([
      ['old', 'Old'],
      ['new', 'New'],
    ]);
    expect(gandalf.texts[0]?.ops).toEqual([
      { op: '=', text: 'Wise. Fellowship: Exert Gandalf twice to play a [gandalf] card from your draw deck.' },
    ]);
    expect(gandalf.texts[1]?.ops.filter((op) => op.op === '+').map((op) => op.text)).toEqual(['discard pile']);
    expect(gandalf.hint).toBeNull();
  });

  it('says when only the image changed, and shows the text alone with nothing to compare', () => {
    const site = errataReadout(entries[3]!);
    expect(site.texts).toEqual([
      {
        side: 'same',
        label: 'Game text (unchanged)',
        ops: [{ op: '=', text: 'Underground. Each minion is strength +1.' }],
      },
    ]);
    expect(site.hint).toBe(
      'The game text and stats read the same; the change is elsewhere on the card (compare the images).',
    );
    const playtest = errataReadout(entries[2]!);
    expect(playtest.texts[0]?.label).toBe('Game text (unchanged)');
    expect(playtest.hint).toBeNull();
    expect(errataReadout(entries[6]!)).toEqual({
      stats: [],
      texts: [{ side: 'same', label: 'Game text', ops: [{ op: '=', text: 'Pipe. Bearer must be a Hobbit.' }] }],
      hint: null,
    });
    expect(errataReadout(entries[4]!).stats).toEqual([{ name: 'Type', from: 'Event', to: 'Condition' }]);
  });
});
