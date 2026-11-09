import { TestBed } from '@angular/core/testing';
import { CardImageService } from '../cards/card-image.service';
import { deliveredCards, selectionChoiceCards } from './shown-cards';

describe('shown cards', () => {
  const options = () => ({ images: TestBed.inject(CardImageService), foilImage: 'foil.png' });

  it('draws selection choices like collection cards, picked when used', () => {
    const [card] = selectionChoiceCards(['1_2'], options());
    expect(card).toMatchObject({
      ref: { kind: 'shown', blueprintId: '1_2', zone: 'selection', choosable: true },
      role: 'card',
      border: true,
      count: null,
      overlayImage: null,
    });
  });

  it('draws delivered packs and cards with their counts and a border, foils with the foil layer', () => {
    const cards = deliveredCards(
      [
        { blueprintId: 'FotR - Booster', count: 2, pack: true },
        { blueprintId: '1_13*', count: 1, pack: false },
      ],
      options(),
    );
    expect(cards.map((c) => [c.role, c.count, c.border, c.errata, c.foilImage])).toEqual([
      ['pack', 2, true, false, null],
      ['card', 1, true, false, 'foil.png'],
    ]);
    expect(cards[1].ref).toEqual({ kind: 'shown', blueprintId: '1_13*', zone: 'player', choosable: false });
  });
});
