import { readServerResponse } from '../../../testing/files';
import { parseFormatDefinitions } from './format-definitions';

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
});
