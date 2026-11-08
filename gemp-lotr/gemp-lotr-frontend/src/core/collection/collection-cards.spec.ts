import { TestBed } from '@angular/core/testing';
import { CardImageService } from '../cards/card-image.service';
import { collectionCards } from './collection-cards';

describe('collectionCards', () => {
  const options = () => ({
    images: TestBed.inject(CardImageService),
    available: (_: string, count: number) => count - 1,
    foilImage: '/gemp-lotr/images/holo.jpg',
  });

  it('is empty before a page has loaded', () => {
    expect(collectionCards(null, options())).toEqual([]);
  });

  it('puts packs first and gives each item its role, image, border and count', () => {
    const cards = collectionCards(
      {
        total: 5,
        items: [
          { kind: 'card', blueprintId: '1_2*', count: 3, side: 'FREE_PEOPLE', group: 'fp' },
          { kind: 'pack', blueprintId: 'FotR - Booster', count: 2, selection: null },
          { kind: 'card', blueprintId: '404_1', count: 1, side: null, group: null },
          { kind: 'card', blueprintId: '1_319', count: 1, side: null, group: null },
          { kind: 'pack', blueprintId: '(S)FotR - Starter', count: 1, selection: ['1_1'] },
        ],
      },
      options(),
    );
    expect(cards.map((card) => [card.role, card.ref.blueprintId, card.count, card.border])).toEqual([
      ['pack', 'FotR - Booster', 2, false],
      ['selection', '(S)FotR - Starter', 1, false],
      ['card', '1_2*', 2, true],
      ['placeholder', '404_1', 1, true],
      ['card', '1_319', 0, true],
    ]);
    expect(cards.map((card) => card.ref.kind === 'collection' && card.ref.role)).toEqual([
      'packInCollection',
      'selectionInCollection',
      'cardInCollection',
      'placeholderInCollection',
      'cardInCollection',
    ]);
    expect(cards[2].foilImage).toBe('/gemp-lotr/images/holo.jpg');
    expect(cards[2].imageUrl).toBe('https://i.lotrtcgpc.net/decipher/LOTR01002.jpg');
    expect(cards[3].foilImage).toBeNull();
    expect(cards[4].horizontal).toBe(true);
  });
});
