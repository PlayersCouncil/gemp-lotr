import { CARD_WINDOW, cardWindowSize, CardWindowInput } from './card-window';
import { FitCard, rowsNeeded } from './fit-cards';

const upright = (count: number): FitCard[] => Array.from({ length: count }, () => ({ horizontal: false }));
const input = (cards: FitCard[], over: Partial<CardWindowInput> = {}): CardWindowInput => ({
  cards,
  gap: 5,
  label: 0,
  room: { width: 1920, height: 1080 },
  chrome: { width: 50, height: 110 },
  ...over,
});

describe('cardWindowSize', () => {
  it('shows a few cards at the tallest height, in one row', () => {
    // 3 cards of 357px (500 * 350/490, rounded down) and two gaps
    expect(cardWindowSize(input(upright(3)))).toEqual({ width: 3 * 357 + 10, height: 500, cardHeight: 500 });
  });

  it('fits the eight cards of a hand comfortably at 1920x1080: two rows of four', () => {
    // 90% of 1080, less the window's own 110px: 862px, so two rows of 440 + 5 don't fit, two of 420 do
    const size = cardWindowSize(input(upright(8)));
    expect(size.cardHeight).toBe(420);
    expect(size.height).toBe(2 * 420 + 5);
    expect(size.width).toBe(4 * 300 + 3 * 5);
  });

  it('tries smaller cards in steps of 20, and uses as few rows as fit at each', () => {
    const size = cardWindowSize(input(upright(8), { room: { width: 1366, height: 768 } }));
    // 1229 - 50 across, 691 - 110 down: 280 in two rows (285 + 280 + 5 = 565)
    expect(size.cardHeight).toBe(280);
    expect(size.height).toBe(565);
    expect((CARD_WINDOW.maxCardHeight - size.cardHeight!) % CARD_WINDOW.step).toBe(0);
    expect(rowsNeeded(upright(8), 280, size.width, 5)).toBe(2);
  });

  it('leaves room under each row for a label', () => {
    const size = cardWindowSize(input(upright(3), { label: 36, room: { width: 1366, height: 768 } }));
    // 691 - 110 = 581 down: 500 + 36 fits
    expect(size).toEqual({ width: 3 * 357 + 10, height: 536, cardHeight: 500 });
    const tight = cardWindowSize(input(upright(3), { label: 36, room: { width: 1366, height: 700 } }));
    // 630 - 110 = 520: 480 + 36
    expect(tight.cardHeight).toBe(480);
    expect(tight.height).toBe(516);
  });

  it('is at least the least width', () => {
    // 450 - 110 = 340 down: one card of 340, 242 wide
    expect(cardWindowSize(input(upright(1), { room: { width: 1920, height: 500 } }))).toEqual({
      width: CARD_WINDOW.minWidth,
      height: 340,
      cardHeight: 340,
    });
  });

  it('counts a landscape card as wide as it is high', () => {
    expect(cardWindowSize(input([{ horizontal: true }, { horizontal: false }]))).toEqual({
      width: 500 + 357 + 5,
      height: 500,
      cardHeight: 500,
    });
  });

  it('keeps the room a panel at the side takes', () => {
    const size = cardWindowSize(input(upright(8), { room: { width: 1920 - 600, height: 1080 } }));
    expect(size.width).toBeLessThanOrEqual(Math.floor(1320 * 0.9) - 50);
  });

  it("shares the cards out evenly between the rows, but takes the rows the cards' own widths make", () => {
    // Five cards in two rows: three and two, as wide as three
    const size = cardWindowSize(input(upright(5), { room: { width: 1200, height: 1080 } }));
    expect(size.cardHeight).toBe(420);
    expect(size.width).toBe(3 * 300 + 2 * 5);
    expect(rowsNeeded(upright(5), 420, size.width, 5)).toBe(2);
  });

  it('gives an empty window the least width and at most 150px', () => {
    expect(cardWindowSize(input([]))).toEqual({ width: CARD_WINDOW.minWidth, height: 150, cardHeight: null });
    expect(cardWindowSize(input([], { room: { width: 400, height: 200 } })).height).toBe(70);
  });

  it('where no card height down to the least fits, takes the largest window allowed and leaves the cards to it', () => {
    expect(cardWindowSize(input(upright(60), { room: { width: 800, height: 600 } }))).toEqual({
      width: 720 - 50,
      height: 540 - 110,
      cardHeight: null,
    });
    expect(cardWindowSize(input(upright(2), { room: { width: 40, height: 40 } }))).toEqual({
      width: 0,
      height: 0,
      cardHeight: null,
    });
  });
});
