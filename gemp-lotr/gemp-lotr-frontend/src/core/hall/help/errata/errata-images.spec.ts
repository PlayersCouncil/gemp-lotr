import { CardImageTables } from '../../../cards/card-blueprint';
import { readServerResponse } from '../../../../testing/files';
import { parseErrataCatalog } from './errata-catalog';
import { aspectOf, errataImages, errataPreview, LANDSCAPE, PORTRAIT } from './errata-images';

const tables: CardImageTables = {
  fixedImages: {},
  set40: {},
  hobbit: {},
  packBlueprints: {},
  PCCards: {
    '51_45': 'https://i.lotrtcgpc.net/errata/LOTR-EN01E045.1_card.jpg',
    '103_19': 'https://i.lotrtcgpc.net/sets/vset3/LOTR-ENV3E019.1_card.jpg',
    '101_5': 'https://i.lotrtcgpc.net/sets/vset1/LOTR-ENV1S005.png',
  },
};

describe('errata images', () => {
  const { entries } = parseErrataCatalog(JSON.parse(readServerResponse('errata.json')));
  const [gandalf, , , site, , wraith, pipe] = entries;

  it("shows an errata beside its original's own image (imageUrls)", () => {
    expect(errataImages(tables, gandalf!)).toEqual({
      after: 'https://i.lotrtcgpc.net/errata/LOTR-EN01E045.1_card.jpg',
      before: 'https://i.lotrtcgpc.net/decipher/LOTR01045.jpg',
    });
  });

  it('finds a revised V-set card’s first image by its name, or none', () => {
    expect(errataImages(tables, wraith!)).toEqual({
      after: 'https://i.lotrtcgpc.net/sets/vset3/LOTR-ENV3E019.1_card.jpg',
      before: 'https://i.lotrtcgpc.net/sets/vset3/LOTR-ENV3S019.0_card.jpg',
    });
    expect(errataImages(tables, pipe!).before).toBeNull();
  });

  it('opens either side in the card preview, with its title (previewArgs)', () => {
    expect(errataPreview(tables, gandalf!, 'before')).toEqual({
      blueprintId: '1_45',
      imageUrl: 'https://i.lotrtcgpc.net/decipher/LOTR01045.jpg',
      title: 'Gandalf, The Grey Wizard (original)',
    });
    expect(errataPreview(tables, gandalf!, 'after')?.title).toBe('Gandalf, The Grey Wizard (PC errata)');
    expect(errataPreview(tables, pipe!, 'before')).toBeNull();
  });

  it('takes sites as landscape until their image says otherwise', () => {
    expect(aspectOf(site!)).toBe(LANDSCAPE);
    expect(aspectOf(gandalf!)).toBe(PORTRAIT);
    expect(aspectOf(null)).toBe(PORTRAIT);
  });
});
