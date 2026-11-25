import { readServerResponse } from '../../../testing/files';
import { parseFormat } from '../help/format-definitions';
import { formatDescriptionLines } from './format-description';

const FORMAT = parseFormat(JSON.parse(readServerResponse('hall-format-definition.json')))!;

describe('format description', () => {
  it('words the description as the server page did, the sites by their readable name', () => {
    expect(FORMAT.sitesName).toBe('Fellowship block');
    expect(formatDescriptionLines(FORMAT).map((line) => [line.text, line.cards.map((card) => card.id)])).toEqual([
      ['Cards from sets 1-3', []],
      ['Sites: Fellowship block', []],
      ['Ring-bearer skirmish cancel: Yes', []],
      ["X-listed (can't be played): ", ['1_45']],
      ['Restricted by card name: Gollum', []],
      ['Errata: PC Errata (see Help \u203A PC Errata)', []],
    ]);
    expect(formatDescriptionLines({ ...FORMAT, sitesName: null })[1]?.text).toBe('Sites: FELLOWSHIP');
  });

  it('lists the rules that differ, the other card lists and the playtest errata', () => {
    const lines = formatDescriptionLines({
      ...FORMAT,
      setSummary: null,
      winAtEndOfRegroup: true,
      discardPileIsPublic: true,
      errata: { pc: false, playtest: true, pcCardsLegal: true },
      restrictedNames: [],
      lists: { ...FORMAT.lists, banned: [], valid: FORMAT.lists.banned },
    }).map((line) => line.text);
    expect(lines).toEqual([
      'Sites: Fellowship block',
      'Ring-bearer skirmish cancel: Yes',
      'The game ends after Regroup actions are made (instead of at the start of Regroup)',
      'Discard piles are public information for both sides',
      'Errata: PC Errata versions are legal alongside the originals (see Help \u203A PC Errata)',
      'Errata: playtest errata',
      'Additional valid: ',
    ]);
  });
});
