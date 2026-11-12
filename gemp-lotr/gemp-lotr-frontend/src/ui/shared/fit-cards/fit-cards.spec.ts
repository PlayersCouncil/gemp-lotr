import { choices } from '../../../testing/parity/choices';
import {
  cardWidth,
  fitCardHeight,
  fitCards,
  FitCard,
  FitCardsInput,
  parseFit,
  parsePx,
  rowLayout,
  rowSnaps,
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

  it('rows: rounds each row down to a whole pixel, as the legacy groups placed them', () => {
    // 81px cards, four to a 372px row: rows start at 0, 119.25 and 238.5, placed at 0, 119 and 238
    expect(rowLayout(upright(9), 114.25, 372, 5).map(({ row, x }) => `${row}:${x}`)).toEqual([
      '0:0',
      '0:86',
      '0:172',
      '0:258',
      '1:0',
      '1:86',
      '1:172',
      '1:258',
      '2:0',
    ]);
    expect(rowSnaps(upright(9), 114.25, 372, 5)).toEqual([0, 0, 0, 0, -0.25, -0.25, -0.25, -0.25, -0.5]);
  });

  describe('contain and none, against a plain simulation of the rows', () => {
    /** Cards side by side with `gap` between them, a new row when the next doesn't fit; Infinity if one never fits. */
    function rowsBySimulation(fitCards: readonly FitCard[], rowHeight: number, width: number, gap: number): number {
      let rows = 1;
      let used = 0;
      for (const card of fitCards) {
        const copies = (card.behind ?? []).map((landscape) => Math.floor(cardWidth(landscape, rowHeight) * 0.2));
        const w = cardWidth(card.horizontal, rowHeight) + copies.reduce((sum, copy) => sum + copy, 0);
        if (w > width) {
          return Infinity;
        }
        if (used === 0) {
          used = w;
        } else if (used + gap + w <= width) {
          used += gap + w;
        } else {
          rows++;
          used = w;
        }
      }
      return rows;
    }

    const height = (rows: number, rowHeight: number, gap: number) => rows * rowHeight + (rows - 1) * gap;

    it('picks the largest row height at which every card fits, and centres the rows', () => {
      const pick = choices(23);
      for (let i = 0; i < 3000; i++) {
        const input: FitCardsInput = {
          width: pick.below(700),
          height: pick.below(700),
          cards: Array.from({ length: 1 + pick.below(25) }, () => {
            const horizontal = pick.chance(0.2);
            return pick.chance(0.3)
              ? { horizontal, behind: [horizontal, horizontal].slice(pick.below(2)) }
              : { horizontal };
          }),
          gap: pick.of([0, 3, 5, 10]),
          maxHeight: pick.of([60, 200, 497, 0.5]),
          fit: pick.of(['contain', 'none'] as const),
        };
        const max = Math.max(1, Math.floor(input.maxHeight));
        const fits = (h: number) =>
          height(rowsBySimulation(input.cards, h, input.width, input.gap), h, input.gap) <= input.height;
        let expected = input.fit === 'none' ? max : 1;
        if (input.fit === 'contain') {
          for (let h = max; h >= 1; h--) {
            if (fits(h)) {
              expected = h;
              break;
            }
          }
        }
        const rows = rowsBySimulation(input.cards, expected, input.width, input.gap);
        const offset = Math.max(0, Math.floor((input.height - height(rows, expected, input.gap)) / 2));
        expect(fitCards(input), `case ${i}: ${JSON.stringify(input)}`).toEqual({ height: expected, offset });
      }
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
