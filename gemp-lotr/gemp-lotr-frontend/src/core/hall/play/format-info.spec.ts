import { formatInfo, limitedFormatInfo, readFormatDetails } from './format-info';

describe('the format (i)', () => {
  it('says what a format allows, with its link to Help › Format Definitions', () => {
    const standard = readFormatDetails({
      code: 'standard',
      name: 'Standard',
      setSummary: 'Cards from sets 0, 7-19',
      sites: 'Shadows and onwards',
      cancelRingBearerSkirmish: false,
    });
    expect(formatInfo(standard)).toEqual({
      intro: "A 'format' is a shorthand for what cards are legal to use and what ruleset is being followed.",
      text: null,
      facts: ['Cards from sets 0, 7-19', 'Sites: Shadows and onwards', 'Ring-bearer skirmish cancel: No'],
      compare: {
        text: 'Compare Standard with the other formats: ',
        label: 'Help › Format Definitions',
        route: 'help/formats/standard',
      },
    });
    expect(formatInfo(null).text).toBe('Choose a format to see what it allows.');
    expect(limitedFormatInfo(false).text).toBe(
      'The set of packs drafted from; the deck is built from the cards drafted.',
    );
  });
});
