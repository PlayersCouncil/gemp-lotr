import {
  atScrollEdge,
  EdgeWheel,
  errataKeyAction,
  followPoint,
  followScroll,
  isPreviewSwipe,
  isSmoothScroll,
  rowAt,
  rowCenter,
  rowHeight,
  rowScrollTarget,
  RowGeometry,
  swipeStep,
  WheelStepper,
  wheelPixels,
} from './errata-skim';

// 20 rows of 25px under a 30px header
const geom: RowGeometry = {
  tops: Array.from({ length: 20 }, (_, i) => 30 + i * 25),
  heights: Array<number>(20).fill(25),
  headH: 30,
};
const box = (scrollTop: number) => ({ scrollTop, scrollHeight: 530, clientHeight: 230 });

describe('errata browsing', () => {
  it('turns wheel lines and pages into pixels', () => {
    expect(wheelPixels(3, 1, 400)).toBe(48);
    expect(wheelPixels(1, 2, 400)).toBe(400);
    expect(wheelPixels(100, 0, 400)).toBe(100);
  });

  it('steps one card per notch over the viewer, at most every 140ms (bindWheel)', () => {
    const wheel = new WheelStepper();
    expect(wheel.step(20, 1000)).toBe(0);
    expect(wheel.step(20, 1010)).toBe(1);
    expect(wheel.step(100, 1050)).toBe(0); // too soon: it adds up
    expect(wheel.step(1, 1160)).toBe(1);
    expect(wheel.step(-30, 1500)).toBe(0);
    expect(wheel.step(-30, 1800)).toBe(0); // a pause starts the count again
    expect(wheel.step(-15, 1810)).toBe(-1);
  });

  it('keeps going at the rows’ pace past their end (onListWheel)', () => {
    const edge = new EdgeWheel();
    expect(edge.rows(60, 1000, 25)).toBe(2);
    expect(edge.rows(15, 1100, 25)).toBe(1); // 10 left over + 15
    expect(edge.rows(-10, 1150, 25)).toBe(0); // the other way starts again
    expect(edge.rows(-20, 1160, 25)).toBe(-1);
    expect(atScrollEdge(10, 300, 530, 230)).toBe(true);
    expect(atScrollEdge(-10, 300, 530, 230)).toBe(false);
    expect(atScrollEdge(-10, 0, 530, 230)).toBe(true);
    expect(atScrollEdge(10, 0, 200, 230)).toBe(true);
  });

  it('measures the rows', () => {
    expect(rowCenter(geom, 2)).toBe(92.5);
    expect(rowAt(geom, 0)).toBe(0);
    expect(rowAt(geom, 92)).toBe(2);
    expect(rowAt(geom, 9999)).toBe(19);
    expect(rowHeight(geom)).toBe(25);
    expect(rowHeight(null)).toBe(25);
    expect(rowHeight({ tops: [0, 4], heights: [4, 4], headH: 0 })).toBe(8);
  });

  it('scrolls the current row to the middle of the window, as far as the rows go (scrollToRow)', () => {
    // row 10's middle is 292.5; the window under the header is 200px
    expect(rowScrollTarget(geom, 10, box(0))).toBe(163);
    expect(rowScrollTarget(geom, 10, box(162))).toBeNull();
    expect(rowScrollTarget(geom, 0, box(100))).toBe(0);
    expect(rowScrollTarget(geom, 19, box(0))).toBe(300);
    expect(isSmoothScroll(300, box(0), false)).toBe(true);
    expect(isSmoothScroll(300, box(0), true)).toBe(false);
    expect(isSmoothScroll(800, box(0), false)).toBe(false);
  });

  it('lets the selection ride with the rows, reaching the first and last card at the ends (followScroll)', () => {
    const start = followPoint(geom, 5, box(100));
    expect(start).toEqual({ s: 100, y: 167.5 });
    expect(followScroll(geom, start!, box(100.2))).toBeNull();
    expect(followScroll(geom, start!, box(0))?.index).toBe(0);
    expect(followScroll(geom, start!, box(300))?.index).toBe(19);
    const down = followScroll(geom, start!, box(150));
    expect(down?.index).toBeGreaterThan(5);
    const up = followScroll(geom, start!, box(50));
    expect(up?.index).toBeLessThan(5);
    // kept inside the window
    expect(followPoint(geom, 19, box(0))).toEqual({ s: 0, y: 229 });
    expect(followPoint(geom, 25, box(0))).toBeNull();
  });

  it('tells a swipe from a tap, and a swipe up on the card from a step (bindSwipe)', () => {
    expect(swipeStep(0, -40)).toBe(1);
    expect(swipeStep(0, 45)).toBe(-1);
    expect(swipeStep(-50, 10)).toBe(1);
    expect(swipeStep(60, 0)).toBe(-1);
    expect(swipeStep(30, 30)).toBe(0);
    expect(isPreviewSwipe(true, true, 5, -50)).toBe(true);
    expect(isPreviewSwipe(false, true, 5, -50)).toBe(false);
    expect(isPreviewSwipe(true, false, 5, -50)).toBe(false);
  });

  it('maps the keys, only j, k and o on a tab, Escape only while enlarged (onKey)', () => {
    expect(['ArrowDown', 'j', 'K', 'o', 'Home', 'End', 'x'].map((key) => errataKeyAction(key, false, false))).toEqual([
      'next',
      'next',
      'previous',
      'flip',
      'first',
      'last',
      null,
    ]);
    expect(errataKeyAction('ArrowDown', true, false)).toBeNull();
    expect(errataKeyAction('j', true, false)).toBe('next');
    expect(errataKeyAction('Escape', false, false)).toBeNull();
    expect(errataKeyAction('Escape', false, true)).toBe('close');
  });
});
