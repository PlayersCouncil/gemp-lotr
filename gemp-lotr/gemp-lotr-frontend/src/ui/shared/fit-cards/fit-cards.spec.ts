import {
  cardWidth,
  fitCardHeight,
  FitCard,
  FitCardsInput,
  parseFit,
  parsePx,
  rowsNeeded,
  stackWidth,
} from './fit-cards';

const upright = (n: number): FitCard[] => Array.from({ length: n }, () => ({ horizontal: false }));
const cards = (...horizontal: boolean[]): FitCard[] => horizontal.map((h) => ({ horizontal: h }));

describe('fit-cards sizing', () => {
  it('gives upright cards the card proportions and landscape cards the row height', () => {
    expect(cardWidth(false, 140)).toBe(100);
    expect(cardWidth(false, 114)).toBe(81);
    expect(cardWidth(true, 114)).toBe(114);
  });

  it('counts the copies behind a card at a fifth of their width each', () => {
    // 137px cards: 27px of each copy shows
    expect(stackWidth({ horizontal: false, behind: [false, false] }, 192)).toBe(137 + 2 * 27);
    expect(stackWidth({ horizontal: false }, 192)).toBe(137);
    expect(rowsNeeded([{ horizontal: false, behind: [false] }, ...upright(1)], 140, 230, 5)).toBe(1);
    expect(rowsNeeded([{ horizontal: false, behind: [false, false] }, ...upright(1)], 140, 230, 5)).toBe(2);
  });

  it('counts the rows the cards wrap into', () => {
    // 100px cards with 5px gaps: three fit in 310px (100 + 5 + 100 + 5 + 100)
    expect(rowsNeeded(upright(3), 140, 310, 5)).toBe(1);
    expect(rowsNeeded(upright(4), 140, 310, 5)).toBe(2);
    expect(rowsNeeded(upright(7), 140, 310, 5)).toBe(3);
    expect(rowsNeeded(cards(false, true, false), 140, 310, 5)).toBe(2);
    expect(rowsNeeded(upright(1), 140, 99, 5)).toBe(Infinity);
  });

  describe('fitCardHeight', () => {
    const base: FitCardsInput = {
      width: 372,
      height: 472,
      cards: upright(12),
      gap: 5,
      maxHeight: 200,
      fit: 'contain',
    };

    it('uses the largest height when everything fits', () => {
      expect(fitCardHeight({ ...base, cards: upright(2) })).toBe(200);
      expect(fitCardHeight({ ...base, cards: [] })).toBe(200);
    });

    it('contain: the largest row height at which all cards fit in the box', () => {
      const height = fitCardHeight(base);
      // 4 per row at 125px (89px cards), 3 rows: 3 * 125 + 2 * 5 = 385 <= 472
      expect(height).toBe(125);
      const fits = (h: number) => {
        const rows = rowsNeeded(base.cards, h, base.width, base.gap);
        return rows * h + (rows - 1) * base.gap <= base.height;
      };
      expect(fits(height)).toBe(true);
      expect(fits(height + 1)).toBe(false);
    });

    it('rows: the legacy NormalCardGroup sizes (fewest rows, each as high as the box allows)', () => {
      // The legacy collection box at 1440x900 with 12 cards: 3 rows of 154px don't fit 4 a row, so 4 rows of
      // (472 - 3 * 5) / 4 = 114.25px, a fraction as on the legacy page (cards are 81px wide either way)
      expect(fitCardHeight({ ...base, fit: 'rows' })).toBe(114.25);
      // 18 cards: 4 rows hold only 16, so 5 rows of 90.4px (5 a row)
      expect(fitCardHeight({ ...base, fit: 'rows', cards: upright(18) })).toBe(90.4);
      expect(fitCardHeight({ ...base, fit: 'rows', cards: upright(3) })).toBe(200);
    });

    it('rows: one row when two rows of it would not fit, centred by the theme (the legacy deck piles)', () => {
      // The legacy Shadow pile at 1440x900 (a 736x389 area): four cards at the 200px maximum
      expect(fitCardHeight({ ...base, width: 736, height: 389, fit: 'rows', cards: upright(4) })).toBe(200);
      // The Free Peoples pile: six cards, one with two copies behind it, need two rows of (389 - 5) / 2 = 192px
      const pile = [{ horizontal: false, behind: [false, false] }, ...upright(5)];
      expect(fitCardHeight({ ...base, width: 736, height: 389, fit: 'rows', cards: pile })).toBe(192);
      // Ten cards in a wide, low box: one row, as high as their total width allows (120 / (856.8 / 691) = 96.8)
      expect(fitCardHeight({ ...base, width: 736, height: 120, fit: 'rows', cards: upright(10) })).toBe(96);
    });

    it('none: always the largest height (the box scrolls)', () => {
      expect(fitCardHeight({ ...base, fit: 'none', cards: upright(40) })).toBe(200);
    });

    it('never goes below 1px, even in a box too small for any card', () => {
      expect(fitCardHeight({ ...base, width: 0, height: 0 })).toBe(1);
      expect(fitCardHeight({ ...base, width: 0, height: 0, fit: 'rows' })).toBe(1);
    });
  });

  it('reads the custom properties', () => {
    expect(parsePx(' 200px', 5)).toBe(200);
    expect(parsePx('', 5)).toBe(5);
    expect(parsePx(null, 5)).toBe(5);
    expect(parseFit(' rows')).toBe('rows');
    expect(parseFit('none')).toBe('none');
    expect(parseFit('')).toBe('contain');
    expect(parseFit('stretch')).toBe('contain');
  });
});
