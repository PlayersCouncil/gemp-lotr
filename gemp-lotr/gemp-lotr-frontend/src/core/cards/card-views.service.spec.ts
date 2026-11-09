import { TestBed } from '@angular/core/testing';
import { FoilSetting } from '../platform/foil-setting';
import { CardImageService } from './card-image.service';
import { CardViews } from './card-views.service';

describe('CardViews', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [CardViews, { provide: FoilSetting, useValue: { overlayImage: () => 'foil.png' } }],
    }),
  );

  const views = () => TestBed.inject(CardViews);

  it('takes what it can from the blueprint and keeps what the page gave', () => {
    const ref = { mine: true };
    expect(views().create({ ref, blueprintId: '1_2', zone: 'FREE_PEOPLE' })).toEqual({
      blueprintId: '1_2',
      zone: 'FREE_PEOPLE',
      ref,
      role: 'card',
      imageUrl: 'https://i.lotrtcgpc.net/decipher/LOTR01002.jpg',
      horizontal: false,
      errata: false,
      foilImage: null,
      border: true,
      count: null,
      overlayImage: null,
      wholePixels: false,
    });
  });

  it("draws a foil with the player's foil layer, unless told not to", () => {
    expect(views().create({ ref: null, blueprintId: '1_2*', zone: null }).foilImage).toBe('foil.png');
    expect(views().create({ ref: null, blueprintId: '1_2*', zone: null, noFoil: true }).foilImage).toBeNull();
  });

  it('marks errata unless told not to', () => {
    expect(views().create({ ref: null, blueprintId: '51_2', zone: null }).errata).toBe(true);
    expect(views().create({ ref: null, blueprintId: '51_2', zone: null, noErrata: true }).errata).toBe(false);
  });

  it('gives packs no border unless told to, and resolves the overlay', () => {
    const pack = { ref: null, blueprintId: 'FotR - Booster', zone: 'pack' };
    expect(views().create(pack).border).toBe(false);
    expect(views().create({ ...pack, border: true }).border).toBe(true);
    expect(
      views().create({ ref: null, blueprintId: '1_319', zone: 'deck', overlayBlueprintId: '90_1' }).overlayImage,
    ).toBe(TestBed.inject(CardImageService).imageUrl('90_1'));
  });
});
