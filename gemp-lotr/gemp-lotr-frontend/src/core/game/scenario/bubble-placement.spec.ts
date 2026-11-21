import {
  around,
  arrowReach,
  edgePoint,
  gapPixels,
  overlap,
  placeBubble,
  PlacementInput,
  pointerFor,
  Rect,
  STOCK_GAP,
} from './bubble-placement';

const rect = (left: number, top: number, width: number, height: number): Rect => ({ left, top, width, height });

/** A 200 by 100 bubble in a 1000 by 800 window, 10px from what it points at. */
const input = (extra: Partial<PlacementInput>): PlacementInput => ({
  width: 200,
  height: 100,
  viewWidth: 1000,
  viewHeight: 800,
  anchors: [],
  gap: 10,
  side: 'auto',
  keepClear: [],
  cards: [],
  ...extra,
});

describe('bubble placement', () => {
  it('measures how much two rectangles overlap (touching is not overlapping)', () => {
    expect(overlap(rect(0, 0, 100, 100), rect(50, 60, 100, 100))).toBe(50 * 40);
    expect(overlap(rect(0, 0, 100, 100), rect(100, 0, 50, 50))).toBe(0);
    expect(overlap(rect(0, 0, 100, 100), rect(0, 300, 50, 50))).toBe(0);
  });

  it('puts a rectangle round several, and of one gives that one', () => {
    const one = rect(5, 6, 7, 8);
    expect(around([])).toBeNull();
    expect(around([one])).toBe(one);
    expect(around([one, rect(100, 1, 10, 10), rect(0, 50, 1, 1)])).toEqual(rect(0, 1, 110, 50));
  });

  it("reads a gap: hundredths of the window's shorter side, or pixels; the message's, else the scenario's, else the stock one", () => {
    // 1000 by 500: a hundredth of the shorter side is 5px
    expect(gapPixels([null, null], 1000, 500)).toBeCloseTo(Number(STOCK_GAP) * 5);
    expect(gapPixels(['3', '20px'], 1000, 500)).toBe(15);
    expect(gapPixels([null, '20px'], 1000, 500)).toBe(20);
    expect(gapPixels(['12PX'], 500, 1000)).toBe(12);
    expect(gapPixels(['', 'wide', '2'], 1000, 500)).toBe(10);
  });

  describe('about nothing in particular', () => {
    it('sits a little above the middle of the window', () => {
      expect(placeBubble(input({}))).toEqual({ side: 'none', left: 400, top: 0.38 * 800 - 50 });
      // ...but never above its top
      expect(placeBubble(input({ viewHeight: 100 }))).toMatchObject({ top: 8 });
    });

    it('moves as little as it can to leave clear what it must not cover', () => {
      const placed = placeBubble(input({ keepClear: [rect(300, 200, 400, 200)] }));
      expect(placed).toEqual({ side: 'none', left: 400, top: 464 });
      expect(overlap(rect(placed.left, placed.top, 200, 100), rect(300, 200, 400, 200))).toBe(0);
    });

    it('stays in the middle over cards it would only be a pity to cover', () => {
      // (covering a card costs less than moving off the middle)
      const placed = placeBubble(input({ cards: [rect(450, 300, 20, 20)] }));
      expect(placed.left).toBe(400);
      expect(placed.top).toBeCloseTo(254);
    });
  });

  describe('beside what it points at', () => {
    const middle = rect(400, 400, 100, 50);

    it('goes above it when there is room everywhere, level with its middle, the gap away', () => {
      expect(placeBubble(input({ anchors: [middle] }))).toEqual({ side: 'above', left: 350, top: 290 });
    });

    it('goes on whichever side costs least: not above a thing at the top of the window', () => {
      // Above would have to be pushed back onto the screen; to the right, slid down to its top, it fits
      expect(placeBubble(input({ anchors: [rect(400, 10, 100, 50)] }))).toEqual({ side: 'right', left: 510, top: 10 });
    });

    it('left to itself keeps off the cards about; on a side the message names, covers them', () => {
      const above = rect(350, 290, 200, 100);
      expect(placeBubble(input({ anchors: [middle], cards: [above] })).side).not.toBe('above');
      expect(placeBubble(input({ anchors: [middle], cards: [above], side: 'above' }))).toEqual({
        side: 'above',
        left: 350,
        top: 290,
      });
    });

    it('slides along the side it names to leave clear what it must not cover', () => {
      const wide = rect(400, 400, 400, 50);
      expect(placeBubble(input({ anchors: [wide], side: 'above' }))).toEqual({ side: 'above', left: 500, top: 290 });
      expect(placeBubble(input({ anchors: [wide], side: 'above', keepClear: [rect(500, 290, 100, 100)] }))).toEqual({
        side: 'above',
        left: 600,
        top: 290,
      });
    });

    it('goes on the side it names, as near as the window lets it', () => {
      expect(placeBubble(input({ anchors: [middle], side: 'below' }))).toEqual({ side: 'below', left: 350, top: 460 });
      expect(placeBubble(input({ anchors: [middle], side: 'left' }))).toEqual({ side: 'left', left: 190, top: 375 });
      expect(placeBubble(input({ anchors: [rect(900, 400, 50, 50)], side: 'right' }))).toEqual({
        side: 'right',
        left: 792,
        top: 375,
      });
    });

    it('pointing at several things, beside the lot of them; beside one of them when the lot has no room', () => {
      expect(placeBubble(input({ anchors: [rect(300, 400, 50, 50), rect(550, 400, 50, 50)] }))).toEqual({
        side: 'above',
        left: 350,
        top: 290,
      });
      // In opposite corners: the lot fills the window, so it goes above the one at the bottom right
      expect(placeBubble(input({ anchors: [rect(10, 10, 50, 50), rect(900, 700, 50, 50)] }))).toEqual({
        side: 'above',
        left: 750,
        top: 590,
      });
    });
  });

  it('finds where the line between the middles of two rectangles leaves the first', () => {
    const box = rect(0, 0, 100, 50);
    expect(edgePoint(box, rect(500, 0, 100, 50))).toEqual({ x: 100, y: 25 });
    expect(edgePoint(box, rect(0, -500, 100, 50))).toEqual({ x: 50, y: 0 });
    expect(edgePoint(box, rect(100, 50, 100, 50))).toEqual({ x: 100, y: 50 });
    expect(edgePoint(box, rect(25, 0, 50, 50))).toEqual({ x: 50, y: 25 });
  });

  describe('joining the bubble to what it is about', () => {
    const target = rect(400, 400, 100, 50);

    it('points at nothing it lies on, or with nothing to point at', () => {
      expect(pointerFor(rect(420, 410, 200, 100), [target], 'right', 22)).toEqual({ kind: 'none' });
      expect(pointerFor(rect(0, 0, 200, 100), [], null, 22)).toEqual({ kind: 'none' });
    });

    it('right beside the one thing, an arrow on the near edge, lined up with its middle within the corners', () => {
      expect(pointerFor(rect(510, 380, 200, 100), [target], 'right', 22)).toEqual({
        kind: 'arrow',
        edge: 'left',
        along: 45,
      });
      expect(pointerFor(rect(190, 320, 200, 100), [target], 'left', 22)).toEqual({
        kind: 'arrow',
        edge: 'right',
        along: 86,
      });
      expect(pointerFor(rect(350, 290, 200, 100), [target], 'above', 22)).toEqual({
        kind: 'arrow',
        edge: 'bottom',
        along: 100,
      });
      expect(pointerFor(rect(450, 460, 200, 100), [target], 'below', 22)).toEqual({
        kind: 'arrow',
        edge: 'top',
        along: 14,
      });
      expect(pointerFor(rect(220, 460, 200, 100), [target], 'below', 22)).toMatchObject({ along: 186 });
    });

    it('anywhere else, a line from edge to edge: further off, not level with it, or not placed beside it', () => {
      const far = pointerFor(rect(600, 375, 200, 100), [target], 'right', 22);
      expect(far).toEqual({ kind: 'lines', lines: [{ x1: 600, y1: 425, x2: 500, y2: 425 }] });
      expect(pointerFor(rect(510, 0, 200, 100), [target], 'right', 22).kind).toBe('lines');
      expect(pointerFor(rect(350, 290, 200, 100), [target], null, 22).kind).toBe('lines');
    });

    it('about several things, a line to each it does not lie on', () => {
      const pointer = pointerFor(
        rect(350, 290, 200, 100),
        [target, rect(0, 0, 50, 50), rect(360, 300, 10, 10)],
        'above',
        22,
      );
      expect(pointer.kind === 'lines' ? pointer.lines.length : 0).toBe(2);
    });
  });

  it('counts a bubble as beside its target as close as the stock gap puts it, and never closer than 22px', () => {
    expect(arrowReach(1000, 800)).toBe(22);
    expect(arrowReach(2000, 2000)).toBeCloseTo(42);
  });
});
