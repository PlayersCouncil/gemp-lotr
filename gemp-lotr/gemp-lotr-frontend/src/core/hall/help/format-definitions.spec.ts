import { readServerResponse } from '../../../testing/files';
import {
  cardTooltip,
  collectorLabel,
  findFormat,
  foldText,
  formatAnchor,
  formatFacts,
  formatLists,
  groupBySet,
  hasNoLists,
  navMeta,
  otherRules,
  parseFormatDefinitions,
  setsText,
} from './format-definitions';
import { FormatCard, FormatDefinition } from './format-definitions-models';

const formats = parseFormatDefinitions(JSON.parse(readServerResponse('format-definitions.json')));

function format(code: string): FormatDefinition {
  const found = formats.find((entry) => entry.code === code);
  if (!found) throw new Error(code);
  return found;
}

describe('format definitions (GET /hall/formats/json)', () => {
  it('reads every format in the order sent, with what Serialize left out as null or the default', () => {
    expect(formats.map((entry) => entry.code)).toEqual(['pc_fotr_block', 'rev_tow_sta', 'pc_playtest']);
    const fotr = format('pc_fotr_block');
    expect(fotr).toMatchObject({
      name: 'Fellowship Block (PC)',
      anchor: 'format-pc_fotr_block',
      description: "The first three sets, with the Player's Council's errata.",
      setSummary: 'Cards from sets 1-3, V1',
      sites: 'Fellowship',
      cancelRingBearerSkirmish: true,
      errata: { pc: true, playtest: false, pcCardsLegal: false },
      restrictedNames: [],
    });
    expect(fotr.lists.banned[2]).toEqual({
      id: '101_5',
      name: '·Éowyn, Lady of Rohan',
      collInfo: null,
      set: 101,
      setLabel: 'V1',
      setName: null,
    });
    const playtest = format('pc_playtest');
    expect(playtest.description).toBeNull();
    expect(playtest.setSummary).toBeNull();
    expect(format('rev_tow_sta').restrictedNames).toEqual(['Gandalf']);
  });

  it('reads a damaged answer as no formats, and skips entries without a code', () => {
    expect(parseFormatDefinitions(null)).toEqual([]);
    expect(parseFormatDefinitions({ formats: 'x' })).toEqual([]);
    const [only] = parseFormatDefinitions({
      formats: [{ name: 'x' }, { code: 'c', lists: { banned: [{}, { id: '1_1' }] } }],
    });
    expect(only).toMatchObject({ code: 'c', name: 'c', anchor: 'format-c', ruleOfFour: true, mulliganRule: true });
    expect(only?.lists.banned).toEqual([
      { id: '1_1', name: '1_1', collInfo: null, set: null, setLabel: null, setName: null },
    ]);
    expect(only?.lists.valid).toEqual([]);
  });

  it('finds a format by its code or by the part of its anchor a #format- link gives', () => {
    const odd = parseFormatDefinitions({ formats: [{ code: 'a.b', anchor: formatAnchor('a.b') }] });
    expect(formatAnchor('a.b c')).toBe('format-a-b-c');
    expect(findFormat(odd, 'a.b')?.code).toBe('a.b');
    expect(findFormat(odd, 'a-b')?.code).toBe('a.b');
    expect(findFormat(formats, 'pc_movie')).toBeNull();
  });

  it('gives the facts: sets, sites, the cancel, the errata and the other rules', () => {
    expect(setsText('Cards from sets 1-3, V1')).toBe('1-3, V1');
    expect(setsText('Cards from set 4')).toBe('4');
    expect(setsText('Odd')).toBe('Odd');
    expect(formatFacts(format('pc_fotr_block'))).toEqual([
      { kind: 'text', label: 'Sets', text: '1-3, V1' },
      { kind: 'text', label: 'Sites', text: 'Fellowship' },
      { kind: 'text', label: 'Ring-bearer skirmish cancel', text: 'Yes' },
      { kind: 'errata', label: 'Errata', link: { text: 'PC Errata', format: 'pc_fotr_block' }, text: '' },
    ]);
    expect(formatFacts(format('rev_tow_sta')).slice(2)).toEqual([
      { kind: 'text', label: 'Ring-bearer skirmish cancel', text: 'No' },
      {
        kind: 'errata',
        label: 'Errata',
        link: { text: 'PC Errata', format: null },
        text: ' versions are legal alongside the originals',
      },
      {
        kind: 'rules',
        label: 'Other rules',
        rules: [
          'The game ends after Regroup actions are made (instead of at the start of Regroup)',
          'Discard piles are public information for both sides',
        ],
      },
    ]);
    expect(formatFacts(format('pc_playtest'))).toEqual([
      { kind: 'text', label: 'Sites', text: 'King' },
      { kind: 'text', label: 'Ring-bearer skirmish cancel', text: 'Yes' },
      {
        kind: 'errata',
        label: 'Errata',
        link: { text: 'PC Errata', format: 'pc_playtest' },
        text: ', plus playtest errata',
      },
      {
        kind: 'rules',
        label: 'Other rules',
        rules: ['Each deck includes a Map', 'A player who controls 5 sites wins', 'No rule of 4', 'No mulligan'],
      },
    ]);
    const playtestOnly = { ...format('pc_playtest'), errata: { pc: false, playtest: true, pcCardsLegal: false } };
    expect(formatFacts(playtestOnly).find((fact) => fact.kind === 'errata')).toEqual({
      kind: 'errata',
      label: 'Errata',
      link: null,
      text: 'playtest errata',
    });
    expect(otherRules(format('pc_fotr_block'))).toEqual([]);
  });

  it('gives the lists with cards in order, their cards by set, and says when there are none', () => {
    const lists = formatLists(format('rev_tow_sta'));
    expect(lists.map((list) => [list.key, list.title, list.note])).toEqual([
      ['limit2', 'Limit 2', 'up to two copies per deck'],
      ['limit3', 'Limit 3', 'up to three copies per deck'],
      ['valid', 'Also legal', "cards from outside the format's sets"],
    ]);
    expect(
      groupBySet(format('pc_fotr_block').lists.banned).map((row) => [row.label, row.name, row.cards.length]),
    ).toEqual([
      ['1', 'The Fellowship of the Ring', 1],
      ['2', 'Mines of Moria', 1],
      ['V1', null, 1],
      ['9', 'Reflections', 1],
    ]);
    const unknownSet: FormatCard = { id: 'x', name: 'x', collInfo: null, set: null, setLabel: null, setName: null };
    expect(groupBySet([unknownSet])[0]?.label).toBe('?');
    expect(hasNoLists(format('pc_playtest'))).toBe(true);
    expect(hasNoLists(format('rev_tow_sta'))).toBe(false);
    expect(hasNoLists({ ...format('pc_playtest'), restrictedNames: ['Gandalf'] })).toBe(false);
  });

  it("labels a card with its collector's info (the V-sets' underscores as spaces), else its set and number", () => {
    const [ottar, , eowyn, unknown] = format('pc_fotr_block').lists.banned;
    expect(ottar && cardTooltip(ottar)).toBe('·Ottar, Man of Laketown (1U44)');
    expect(eowyn && collectorLabel(eowyn)).toBe('V1 5');
    expect(collectorLabel({ ...unknownSet(), collInfo: ' V1_5 ' })).toBe('V1 5');
    expect(unknown && cardTooltip(unknown)).toBe('9_99x (9 99)');
    expect(cardTooltip(unknownSet())).toBe('x');
  });

  it('folds case and accents, and gives the contents list its X / R counts', () => {
    expect(foldText('Éowyn')).toBe('eowyn');
    expect(navMeta(format('pc_fotr_block'))).toEqual({ text: 'X4 R1', title: '4 X-listed, 1 R-listed' });
    expect(
      navMeta({ ...format('pc_fotr_block'), lists: { ...format('pc_fotr_block').lists, restricted: [] } }),
    ).toEqual({
      text: 'X4',
      title: '4 X-listed',
    });
    expect(navMeta(format('rev_tow_sta'))).toBeNull();
  });
});

function unknownSet(): FormatCard {
  return { id: 'x', name: 'x', collInfo: null, set: null, setLabel: null, setName: null };
}
