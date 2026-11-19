import { TestBed } from '@angular/core/testing';
import { CardViews } from '../cards/card-views.service';
import { FoilSetting } from '../platform/foil-setting';
import { deliveredCards, selectionChoiceCards } from './shown-cards';

describe('shown cards', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [CardViews, { provide: FoilSetting, useValue: { overlayImage: () => 'foil.png' } }],
    }),
  );

  const options = () => TestBed.inject(CardViews);

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
