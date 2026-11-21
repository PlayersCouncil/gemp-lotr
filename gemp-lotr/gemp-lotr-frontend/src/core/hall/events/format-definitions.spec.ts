import { readServerResponse } from '../../../testing/files';
import { formatDescriptionLines, parseFormatDefinitions } from './format-definitions';

describe('format definitions', () => {
  it('reads what the format description shows, leaving out entries without a code', () => {
    const formats = parseFormatDefinitions(JSON.parse(readServerResponse('hall-format-definitions.json')));
    expect(formats).toEqual([
      {
        code: 'fotr_block',
        name: 'Fellowship Block',
        setSummary: 'Cards from sets 1-3',
        sites: 'FELLOWSHIP',
        cancelRingBearerSkirmish: true,
        winAtEndOfRegroup: false,
        discardPileIsPublic: false,
        errata: { pc: true, playtest: false, pcCardsLegal: false },
        banned: [{ id: '1_45', name: '·Sword of Doom' }],
        restricted: [],
        limit2: [],
        limit3: [],
        valid: [],
        restrictedNames: ['Gollum'],
      },
    ]);
    expect(parseFormatDefinitions(null)).toEqual([]);
  });

  it('words the description as the server page did', () => {
    const [format] = parseFormatDefinitions(JSON.parse(readServerResponse('hall-format-definitions.json')));
    expect(formatDescriptionLines(format!)).toEqual([
      { text: 'Cards from sets 1-3', cards: [] },
      { text: 'Sites: FELLOWSHIP', cards: [] },
      { text: 'Ring-bearer skirmish cancel: Yes', cards: [] },
      { text: "X-listed (can't be played): ", cards: [{ id: '1_45', name: '·Sword of Doom' }] },
      { text: 'Restricted by card name: Gollum', cards: [] },
      { text: 'Errata: PC Errata (see Help › PC Errata)', cards: [] },
    ]);
  });
});
