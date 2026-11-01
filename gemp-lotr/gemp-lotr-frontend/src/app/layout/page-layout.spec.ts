import {
  clampEastWidth,
  computePageLayout,
  EAST_MIN_WIDTH,
  eastWidthForResizerAt,
  hasRoomForEast,
  maxEastWidth,
} from './page-layout';

/** Expected values were measured on the legacy deck builder (jquery.layout) in Chromium. */
describe('page layout', () => {
  it('matches the legacy frame at 1280x800', () => {
    expect(computePageLayout(1280, 800, EAST_MIN_WIDTH, false)).toEqual({
      eastHidden: false,
      centerRight: 416,
      centerContentWidth: 842,
      centerContentHeight: 778,
      eastContentWidth: 388,
      eastContentHeight: 778,
      resizerRight: 410,
      resizerHeight: 800,
      togglerTop: 375,
    });
  });

  it('gives the deck the whole width when the collection pane is closed', () => {
    const layout = computePageLayout(1280, 800, 500, true);
    expect(layout).toMatchObject({ eastHidden: true, centerRight: 6, centerContentWidth: 1252, resizerRight: 0 });
  });

  it('limits the collection pane to half of the width left after the resizer', () => {
    expect(maxEastWidth(1280)).toBe(637);
    expect(maxEastWidth(1100)).toBe(547);
    expect(clampEastWidth(2000, 1280)).toBe(637);
    expect(clampEastWidth(100, 1280)).toBe(EAST_MIN_WIDTH);
    expect(clampEastWidth(500.4, 1280)).toBe(500);
  });

  it('turns a dropped resizer position into a pane width within the limits', () => {
    expect(eastWidthForResizerAt(700, 1280)).toBe(574);
    expect(eastWidthForResizerAt(100, 1280)).toBe(637);
    expect(eastWidthForResizerAt(1250, 1280)).toBe(EAST_MIN_WIDTH);
  });

  it('shrinks a wide pane when the window gets narrower', () => {
    expect(computePageLayout(1100, 700, 637, false).resizerRight).toBe(547);
  });

  it('hides the collection pane when the window has no room for it', () => {
    expect(hasRoomForEast(826)).toBe(true);
    expect(hasRoomForEast(825)).toBe(false);
    expect(computePageLayout(800, 600, EAST_MIN_WIDTH, false)).toMatchObject({
      eastHidden: true,
      centerContentWidth: 772,
      resizerRight: 0,
    });
  });
});
