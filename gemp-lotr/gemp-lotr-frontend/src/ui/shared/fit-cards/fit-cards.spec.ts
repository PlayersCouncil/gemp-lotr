import { cardWidth, fitCardHeight, FitCardsInput, parseFit, parsePx, rowsNeeded } from './fit-cards';

const upright = (n: number) => Array.from({ length: n }, () => false);

describe('fit-cards sizing', () => {
  it('gives upright cards the card proportions and landscape cards the row height', () => {
    expect(cardWidth(false, 140)).toBe(100);
    expect(cardWidth(false, 114)).toBe(81);
    expect(cardWidth(true, 114)).toBe(114);
  });

  it('counts the rows the cards wrap into', () => {
    // 100px cards with 5px gaps: three fit in 310px (100 + 5 + 100 + 5 + 100)
    expect(rowsNeeded(upright(3), 140, 310, 5)).toBe(1);
    expect(rowsNeeded(upright(4), 140, 310, 5)).toBe(2);
    expect(rowsNeeded(upright(7), 140, 310, 5)).toBe(3);
    expect(rowsNeeded([false, true, false], 140, 310, 5)).toBe(2);
    expect(rowsNeeded(upright(1), 140, 99, 5)).toBe(Infinity);
  });

  describe('fitCardHeight', () => {
    const base: FitCardsInput = {
      width: 372,
      height: 472,
      horizontal: upright(12),
      gap: 5,
      maxHeight: 200,
      fit: 'contain',
    };

    it('uses the largest height when everything fits', () => {
      expect(fitCardHeight({ ...base, horizontal: upright(2) })).toBe(200);
      expect(fitCardHeight({ ...base, horizontal: [] })).toBe(200);
    });

    it('contain: the largest row height at which all cards fit in the box', () => {
      const height = fitCardHeight(base);
      // 4 per row at 125px (89px cards), 3 rows: 3 * 125 + 2 * 5 = 385 <= 472
      expect(height).toBe(125);
      const fits = (h: number) => {
        const rows = rowsNeeded(base.horizontal, h, base.width, base.gap);
        return rows * h + (rows - 1) * base.gap <= base.height;
      };
      expect(fits(height)).toBe(true);
      expect(fits(height + 1)).toBe(false);
    });

    it('rows: the legacy NormalCardGroup sizes (fewest rows, each as high as the box allows)', () => {
      // The legacy collection box at 1440x900 with 12 cards: 3 rows of 154px don't fit 4 a row, so 4 rows of 114px
      expect(fitCardHeight({ ...base, fit: 'rows' })).toBe(114);
      // 18 cards: 4 rows hold only 16, so 5 rows of 90px (5 a row)
      expect(fitCardHeight({ ...base, fit: 'rows', horizontal: upright(18) })).toBe(90);
      expect(fitCardHeight({ ...base, fit: 'rows', horizontal: upright(3) })).toBe(200);
    });

    it('none: always the largest height (the box scrolls)', () => {
      expect(fitCardHeight({ ...base, fit: 'none', horizontal: upright(40) })).toBe(200);
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
