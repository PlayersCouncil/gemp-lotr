import { TestBed } from '@angular/core/testing';
import { CardImageService } from '../cards/card-image.service';
import { pileStacks, raceCardView, slotCards } from './deck-cards';
import { addDrawCopy, addToSlot, EMPTY_DECK } from './deck-model';

describe('deck cards', () => {
  const options = () => ({ images: TestBed.inject(CardImageService), foilImage: '/gemp-lotr/images/holo.jpg' });

  it('shows the cards of a slot and the sites, each removable through its CardRef', () => {
    let deck = addToSlot(EMPTY_DECK, 'ringBearer', '1_290');
    deck = addToSlot(deck, 'sites', '1_319');
    deck = addToSlot(deck, 'sites', '1_320');
    const [ringBearer] = slotCards(deck, 'ringBearer', options());
    expect(ringBearer).toEqual(
      expect.objectContaining({ role: 'card', horizontal: false, border: true, count: null, overlayImage: null }),
    );
    expect(ringBearer.ref).toEqual(expect.objectContaining({ kind: 'slot', slot: 'ringBearer', blueprintId: '1_290' }));
    const sites = slotCards(deck, 'sites', options());
    expect(sites.map((site) => [site.ref.blueprintId, site.horizontal])).toEqual([
      ['1_319', true],
      ['1_320', true],
    ]);
    expect(slotCards(deck, 'ring', options())).toEqual([]);
  });

  it('stacks the copies of a draw deck card behind it, oldest first, by side', () => {
    let deck = EMPTY_DECK;
    for (const [id, side] of [
      ['1_2', 'FREE_PEOPLE'],
      ['1_1', 'SHADOW'],
      ['1_2', 'FREE_PEOPLE'],
      ['1_3', 'FREE_PEOPLE'],
      ['1_2*', 'FREE_PEOPLE'],
      ['1_2', 'FREE_PEOPLE'],
    ] as const) {
      deck = addDrawCopy(deck, id, side);
    }
    const free = pileStacks(deck, 'FREE_PEOPLE', options());
    expect(free.map((stack) => [stack.card.ref.blueprintId, stack.behind.length])).toEqual([
      ['1_2', 2],
      ['1_3', 0],
      ['1_2*', 0],
    ]);
    const [first] = free;
    expect(first.card.ref.kind).toBe('draw');
    expect(first.behind.map((copy) => [copy.role, copy.ref.kind])).toEqual([
      ['copy', 'behind'],
      ['copy', 'behind'],
    ]);
    expect(first.behind[0].ref.kind === 'behind' && first.behind[0].ref.card.id).toBeLessThan(
      first.behind[1].ref.kind === 'behind' ? first.behind[1].ref.card.id : 0,
    );
    expect(free[2].card.foilImage).toBe('/gemp-lotr/images/holo.jpg');
    expect(pileStacks(deck, 'SHADOW', options()).map((stack) => stack.card.ref.blueprintId)).toEqual(['1_1']);
  });

  it('shows the Race meta-site with its modifier over it, or nothing outside RTMD', () => {
    expect(raceCardView(null, options())).toBeNull();
    const race = raceCardView({ blueprintId: '1_319', overlayBlueprintId: '90_1' }, options())!;
    expect(race.ref).toEqual({ kind: 'race', blueprintId: '1_319', overlayBlueprintId: '90_1' });
    expect(race.overlayImage).toBe(TestBed.inject(CardImageService).imageUrl('90_1'));
    expect(raceCardView({ blueprintId: '1_319', overlayBlueprintId: null }, options())!.overlayImage).toBeNull();
  });
});
