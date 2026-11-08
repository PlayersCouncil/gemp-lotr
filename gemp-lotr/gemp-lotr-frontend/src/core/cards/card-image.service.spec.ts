import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { flushLegacyCardFiles } from '../../testing/legacy-cards';
import { CardImageService } from './card-image.service';
import { LegacyCardDataService } from './legacy-card-data.service';

/** Readable examples; card-blueprint.parity.spec.ts covers the full range against the legacy code. */
describe('CardImageService', () => {
  let service: CardImageService;

  beforeEach(async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const loading = TestBed.inject(LegacyCardDataService).load();
    flushLegacyCardFiles(TestBed.inject(HttpTestingController));
    await loading;
    service = TestBed.inject(CardImageService);
  });

  it('builds Decipher image URLs from the set and card number', () => {
    const image = service.resolve('1_2');
    expect(image.imageUrl).toBe('https://i.lotrtcgpc.net/decipher/LOTR01002.jpg');
    expect(image.backSideImageUrl).toBe('https://i.lotrtcgpc.net/decipher/LOTR00000.jpg');
    expect(image).toMatchObject({ foil: false, tengwar: false, errata: false, horizontal: false, hasWiki: true });
    expect(image.wikiUrl).toBe('http://wiki.lotrtcgpc.net/wiki/LOTR01002');
  });

  it('understands foil and tengwar markers', () => {
    expect(service.resolve('1_2*')).toMatchObject({ foil: true, bareBlueprint: '1_2' });
    expect(service.resolve('1_2T')).toMatchObject({
      tengwar: true,
      imageUrl: 'https://i.lotrtcgpc.net/decipher/LOTR01002T.jpg',
    });
  });

  it('uses remade errata images and marks errata sets', () => {
    expect(service.resolve('1_3')).toMatchObject({ errata: true, imageUrl: '/gemp-lotr/images/erratas/LOTR01003.jpg' });
    expect(service.resolve('51_7').errata).toBe(true);
  });

  it('uses the legacy image tables', () => {
    expect(service.resolve('30_1')).toMatchObject({
      imageUrl: 'https://i.lotrtcgpc.net/hobbit/HDG30001.jpg',
      hasWiki: false,
    });
  });

  it('shows sites horizontally except in zones that keep cards upright', () => {
    expect(service.resolve('1_319').horizontal).toBe(true);
    expect(service.resolve('1_319', 'DECK').horizontal).toBe(false);
  });

  it('handles set 404 placeholders and in-development sets', () => {
    expect(service.resolve('404_1')).toMatchObject({
      placeholder: true,
      imageUrl: '/gemp-lotr/images/future_prize.jpg',
    });
    expect(service.resolve('401_1').incomplete).toBe(true);
  });

  it('resolves pack images', () => {
    expect(service.resolve('(S)FotR - Starter')).toMatchObject({
      pack: true,
      imageUrl: '/gemp-lotr/images/boosters/fotr_starter_selection.png',
    });
  });
});
