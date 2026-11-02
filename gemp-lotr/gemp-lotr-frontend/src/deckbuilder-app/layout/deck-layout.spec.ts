import { computeDeckLayout } from './deck-layout';

/** Expected values were measured on the legacy deck builder at 1280x800 (deck pane content 842x778). */
describe('computeDeckLayout', () => {
  it('matches the legacy positions without Map or Race', () => {
    const layout = computeDeckLayout(842, 778, false, false);
    expect(layout.manageDecks).toEqual({ left: 5, top: 5, width: 842, height: 23 });
    expect(layout.ringBearer).toEqual({ left: 5, top: 33, width: 110, height: 144 });
    expect(layout.ring).toEqual({ left: 120, top: 33, width: 110, height: 144 });
    expect(layout.map).toBeNull();
    expect(layout.race).toBeNull();
    expect(layout.sites).toEqual({ left: 5, top: 182, width: 225, height: 596 });
    expect(layout.drawDeck).toEqual({ left: 235, top: 33, width: 607, height: 690 });
    expect(layout.freePeoples).toEqual({ left: 0, top: 0, width: 607, height: 345 });
    expect(layout.shadow).toEqual({ left: 0, top: 345, width: 607, height: 345 });
    expect(layout.stats).toEqual({ left: 235, top: 728, width: 607, height: 70 });
  });

  it('adds a row for Map and Race and shortens the sites', () => {
    const layout = computeDeckLayout(842, 778, true, true);
    expect(layout.map).toEqual({ left: 5, top: 177, width: 110, height: 144 });
    expect(layout.race).toEqual({ left: 120, top: 177, width: 110, height: 144 });
    expect(layout.sites).toEqual({ left: 5, top: 326, width: 225, height: 452 });
  });

  it('caps the sites column at 250px on tall windows', () => {
    expect(computeDeckLayout(1400, 1200, false, false).sites.width).toBe(250);
  });
});
