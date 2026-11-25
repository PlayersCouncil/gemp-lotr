import { readServerXml } from '../../testing/files';
import {
  parseCollectionPage,
  parseCollectionTypes,
  parseDeck,
  parseDeckList,
  parseLeagues,
  parsePackDelivery,
} from './parsers';
import { parseXml } from './xml';

describe('server response parsers', () => {
  describe('parseDeckList', () => {
    it('reads deck names and format names, unescaping XML entities', () => {
      expect(parseDeckList(readServerXml('deck-list.xml'))).toEqual([
        { name: 'Test + deck & co', formatName: 'Fellowship Block' },
      ]);
    });

    it('returns an empty list for <decks/> or an unexpected root', () => {
      expect(parseDeckList(parseXml('<decks/>'))).toEqual([]);
      expect(parseDeckList(parseXml('<result>OK</result>'))).toEqual([]);
    });
  });

  describe('parseDeck', () => {
    it('reads a saved deck', () => {
      const deck = parseDeck(readServerXml('deck.xml'));
      expect(deck).not.toBeNull();
      expect(deck!.targetFormat).toEqual({
        formatName: 'Fellowship Block',
        formatCode: 'fotr_block',
        leagueCode: null,
      });
      expect(deck!.notes).toBe('Notes with + plus & ampersand');
      expect(deck!.ringBearer).toBe('1_2');
      expect(deck!.ring).toBe('1_1');
      expect(deck!.map).toBeNull();
      expect(deck!.sites).toEqual(['1_319', '1_327', '1_331', '1_337', '1_343', '1_349', '1_354', '1_356', '1_360']);
      // One entry per copy, in the server's order
      expect(deck!.drawDeck.map((c) => c.blueprintId)).toEqual([
        '1_7',
        '1_12',
        '1_131',
        '1_178',
        '1_234',
        '1_234',
        '1_3',
        '1_3',
        '1_139',
      ]);
      expect(deck!.drawDeck.filter((c) => c.side === 'SHADOW')).toHaveLength(5);
    });

    it('reads the errata-converted deck', () => {
      const deck = parseDeck(readServerXml('convert.xml'));
      expect(deck!.targetFormat!.formatCode).toBe('pc_fotr_block');
      expect(deck!.sites).toContain('51_331');
      expect(deck!.drawDeck.map((c) => c.blueprintId)).toContain('51_234');
    });

    it('returns null for a deck that does not exist', () => {
      expect(parseDeck(readServerXml('deck-missing.xml'))).toBeNull();
    });

    it('reads the map and the RTMD league code', () => {
      const deck = parseDeck(
        parseXml(
          '<deck><targetFormat formatCode="pc_movie" formatName="Movie Block (PC)" leagueCode="1700000002"/>' +
            '<notes/><map blueprintId="100_1"/></deck>',
        ),
      );
      expect(deck!.map).toBe('100_1');
      expect(deck!.targetFormat!.leagueCode).toBe('1700000002');
      expect(deck!.notes).toBe('');
      expect(deck!.drawDeck).toEqual([]);
    });
  });

  describe('parseCollectionPage', () => {
    it('reads the total and the cards of a page', () => {
      const page = parseCollectionPage(readServerXml('collection-default.xml'));
      expect(page.total).toBe(3984);
      expect(page.items).toEqual([
        { kind: 'card', blueprintId: '2_16', count: 4, side: 'FREE_PEOPLE', group: 'fp' },
        { kind: 'card', blueprintId: '12_3', count: 4, side: 'FREE_PEOPLE', group: 'fp' },
        { kind: 'card', blueprintId: '10_19', count: 4, side: 'SHADOW', group: 'shadow' },
      ]);
    });

    it('keeps packs and cards in server order, with selection pack contents', () => {
      const page = parseCollectionPage(readServerXml('collection-with-packs.xml'));
      expect(page.items.map((i) => `${i.kind}:${i.blueprintId}`)).toEqual([
        'pack:FotR - Booster',
        'pack:(S)FotR - Starter',
        'card:1_1',
        'card:1_2',
        'card:1_319',
        'card:404_1',
      ]);
      expect(page.items[0]).toEqual({ kind: 'pack', blueprintId: 'FotR - Booster', count: 3, selection: null });
      expect(page.items[1]).toMatchObject({ selection: ['1_1', '1_2', '1_3'] });
      expect(page.items[3]).toMatchObject({ group: 'ringBearer', count: 2 });
      // A card without side or group (the set 404 placeholder)
      expect(page.items[5]).toMatchObject({ side: null, group: null });
    });

    const xml = (body: string) => new DOMParser().parseFromString(body, 'application/xml');

    it('keeps each group the deck builder places cards by, and no other', () => {
      const groups = ['ring', 'site', 'map', 'ringBearer', 'fp', 'shadow', 'other', ''];
      const page = parseCollectionPage(
        xml(
          `<collection count="8">${groups.map((g) => `<card blueprintId="1_1" count="1" group="${g}"/>`).join('')}</collection>`,
        ),
      );
      expect(page.items.map((item) => (item.kind === 'card' ? item.group : 'pack'))).toEqual([
        'ring',
        'site',
        'map',
        'ringBearer',
        'fp',
        'shadow',
        null,
        null,
      ]);
    });

    it('counts nothing where the server gives no count', () => {
      const page = parseCollectionPage(
        xml('<collection><card blueprintId="1_1"/><pack blueprintId="P"/></collection>'),
      );
      expect(page.total).toBe(0);
      expect(page.items.map((item) => item.count)).toEqual([0, 0]);
      expect(parseCollectionPage(xml('<other/>'))).toEqual({ total: 0, items: [] });
    });
  });

  it('parseCollectionTypes reads the limited collections', () => {
    expect(parseCollectionTypes(readServerXml('collection-types.xml'))).toEqual([
      { type: '1699999999:sealed', name: 'Sealed League - FotR', format: 'fotr_block' },
      { type: 'tourney_42', name: 'Tournament draft', format: 'pc_movie' },
    ]);
  });

  it('parseLeagues reads RTMD positions and meta-sites with the legacy defaults', () => {
    const [constructed, race, otherRace] = parseLeagues(readServerXml('leagues.xml'));
    expect(constructed).toMatchObject({ type: 'CONSTRUCTED', member: true, playerPosition: null, pathLength: null });
    expect(race).toEqual({
      code: '1700000002',
      name: 'Race to Mount Doom',
      type: 'RTMD',
      member: true,
      formatCode: 'pc_movie',
      playerPosition: 3,
      pathLength: 9,
      metaSites: [
        { position: 1, blueprintId: '90_1', visualBlueprintId: '1_331', name: 'Weathertop', siteOverride: false },
        { position: 2, blueprintId: '90_7', visualBlueprintId: null, name: null, siteOverride: true },
      ],
    });
    // Not a member, so the server sends no playerPosition: the legacy page assumed 1
    expect(otherRace).toMatchObject({ member: false, playerPosition: 1, pathLength: 5, metaSites: [] });
  });

  it('parsePackDelivery reads packs and cards per collection', () => {
    expect(parsePackDelivery(readServerXml('pack-delivery.xml'))).toEqual([
      {
        collectionName: 'permanent',
        packs: [{ blueprintId: 'FotR - Booster', count: 2 }],
        cards: [
          { blueprintId: '1_12', count: 1 },
          { blueprintId: '1_13*', count: 1 },
        ],
      },
    ]);
  });

  it('rejects malformed XML', () => {
    expect(() => parseXml('<deck><card></deck>')).toThrow();
  });
});
