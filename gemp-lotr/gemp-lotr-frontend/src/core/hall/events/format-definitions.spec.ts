import { readServerResponse } from '../../../testing/files';
import { formatDescriptionLines, parseFormatDefinition } from './format-definitions';

const FORMAT = JSON.parse(readServerResponse('hall-format-definition.json')) as unknown;

describe('format definitions', () => {
  it('reads what the format description shows', () => {
    expect(parseFormatDefinition(FORMAT)).toEqual({
      code: 'fotr_block',
      name: 'Fellowship Block',
      setSummary: 'Cards from sets 1-3',
      sites: 'FELLOWSHIP',
      sitesName: 'Fellowship block',
      cancelRingBearerSkirmish: true,
      winAtEndOfRegroup: false,
      discardPileIsPublic: false,
      errata: { pc: true, playtest: false, pcCardsLegal: false },
      banned: [{ id: '1_45', name: '\u00B7Sword of Doom' }],
      restricted: [],
      limit2: [],
      limit3: [],
      valid: [],
      restrictedNames: ['Gollum'],
    });
    expect(parseFormatDefinition({ name: 'No code' })).toBeNull();
    expect(parseFormatDefinition(null)).toBeNull();
  });

  it('words the description as the server page did, the sites by their readable name', () => {
    const format = parseFormatDefinition(FORMAT)!;
    expect(formatDescriptionLines(format)).toEqual([
      { text: 'Cards from sets 1-3', cards: [] },
      { text: 'Sites: Fellowship block', cards: [] },
      { text: 'Ring-bearer skirmish cancel: Yes', cards: [] },
      { text: "X-listed (can't be played): ", cards: [{ id: '1_45', name: '\u00B7Sword of Doom' }] },
      { text: 'Restricted by card name: Gollum', cards: [] },
      { text: 'Errata: PC Errata (see Help \u203A PC Errata)', cards: [] },
    ]);
    expect(formatDescriptionLines({ ...format, sitesName: null })[1]?.text).toBe('Sites: FELLOWSHIP');
  });
});
