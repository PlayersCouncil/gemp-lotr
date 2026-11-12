import { zoomPlacement } from './zoom-placement';

describe('zoomPlacement', () => {
  it('sizes the card as the legacy CardDisplay did', () => {
    // 1440x900: 810px limit, 580.8px for the short side; CardDisplay took the smaller limit as the long side, so an
    // upright card is 416x580 (as the legacy page showed it)
    const card = { left: 600, top: 37, right: 737, bottom: 228 };
    expect(zoomPlacement(false, card, 1440, 900)).toEqual({ left: 722, top: 0, width: 416, height: 580 });
    const site = zoomPlacement(true, { left: 9, top: 226, right: 253, bottom: 400 }, 1440, 900);
    expect([site.width, site.height]).toEqual([580, 416]);
    // A huge window: the short side's 745px limit
    const big = zoomPlacement(false, { left: 0, top: 500, right: 100, bottom: 640 }, 4000, 3000);
    expect([big.width, big.height]).toEqual([534, 745]);
  });

  it('goes right of the card (over its last 15px), else left of it', () => {
    expect(zoomPlacement(false, { left: 100, top: 300, right: 200, bottom: 440 }, 1440, 900).left).toBe(185);
    // No room on the right: left of the card
    expect(zoomPlacement(false, { left: 1200, top: 300, right: 1280, bottom: 440 }, 1440, 900).left).toBe(1200 - 416);
  });

  it('centres it on the card vertically, within the window (15px from the bottom)', () => {
    // 1600x1000: 462x645
    const middle = zoomPlacement(false, { left: 100, top: 400, right: 200, bottom: 540 }, 1600, 1000);
    expect(middle.top).toBe(470 - 645 / 2);
    const low = zoomPlacement(false, { left: 100, top: 800, right: 200, bottom: 940 }, 1600, 1000);
    expect(low.top).toBe(1000 - 645 - 15);
    const high = zoomPlacement(false, { left: 100, top: 0, right: 200, bottom: 140 }, 1600, 1000);
    expect(high.top).toBe(0);
  });

  it('uses the larger side when neither has room, placed by the space there but drawn full size', () => {
    // 600x1000: 277x387; 250px on each side of the card, so the left one (the right isn't larger)
    const place = zoomPlacement(false, { left: 250, top: 0, right: 350, bottom: 140 }, 600, 1000);
    expect(place).toEqual({ left: 0, top: 0, width: 277, height: 387 });
  });
});
