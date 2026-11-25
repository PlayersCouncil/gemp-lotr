import { EDGE, placeAtPoint, placeBeside, placeSubmenu } from './popup-place';

const room = { width: 1000, height: 800 };
const size = { width: 200, height: 300 };

describe('popup place', () => {
  it('puts a popup under its box, else over it, else on the side with more room, inside the window', () => {
    expect(placeBeside({ left: 100, top: 100, right: 180, bottom: 200 }, size, room)).toEqual({ left: 100, top: 204 });
    expect(placeBeside({ left: 100, top: 600, right: 180, bottom: 700 }, size, room)).toEqual({ left: 100, top: 296 });
    // (no room either way: the larger side, kept in the window)
    expect(placeBeside({ left: 900, top: 300, right: 980, bottom: 400 }, { width: 200, height: 500 }, room)).toEqual({
      left: 1000 - 200 - EDGE,
      top: 800 - 500 - EDGE,
    });
    expect(placeBeside({ left: 10, top: 500, right: 90, bottom: 520 }, { width: 50, height: 600 }, room).top).toBe(
      EDGE,
    );
  });

  it('puts a menu at a point, flipped where it would leave the window', () => {
    expect(placeAtPoint({ x: 100, y: 100 }, size, room)).toEqual({ left: 100, top: 100 });
    expect(placeAtPoint({ x: 900, y: 700 }, size, room)).toEqual({ left: 700, top: 400 });
  });

  it('puts a submenu beside its item, to the right or else to the left', () => {
    expect(placeSubmenu({ left: 100, top: 50, right: 250, bottom: 70 }, size, room)).toEqual({ left: 250, top: 50 });
    expect(placeSubmenu({ left: 700, top: 650, right: 850, bottom: 670 }, size, room)).toEqual({
      left: 500,
      top: 800 - 300 - EDGE,
    });
  });
});
