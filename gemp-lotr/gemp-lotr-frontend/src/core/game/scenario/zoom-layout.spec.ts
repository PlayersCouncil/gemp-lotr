import { ScenarioZoom } from './scenario-events';
import { cardShape, layoutZoom } from './zoom-layout';

const zoom = (magnify: number, ...rects: [number, number, number, number][]): ScenarioZoom => ({
  blueprint: '1_290',
  image: null,
  magnify,
  focus: rects.map((rect) => ({ name: null, rect })),
});

describe('layoutZoom', () => {
  it('draws the card as tall as the window allows, with the bubble beside it, the two centred together', () => {
    // A card half as wide as it is tall, in a 2000 by 456 window: 400 tall, 200 wide; then 28px, then the bubble
    expect(layoutZoom(zoom(1), 0.5, 300, 2000, 456)).toEqual({
      frame: { left: (2000 - 528) / 2, top: 28, width: 200, height: 400 },
      card: { left: 0, top: 0, width: 200, height: 400 },
      dim: null,
      marks: [],
      focus: [],
    });
  });

  it('draws it narrower in a narrow window, keeping its shape, and never smaller than a card can be read', () => {
    const layout = layoutZoom(zoom(1), 0.5, 300, 800, 2000);
    // Room beside the bubble: 800 - 300 - 28 - 40
    expect(layout.frame).toEqual({ left: 20, top: (2000 - 864) / 2, width: 432, height: 864 });
    // However small the window, a card of some size
    expect(layoutZoom(zoom(1), 0.5, 300, 100, 50).frame).toMatchObject({ width: 60, height: 120 });
    expect(layoutZoom(zoom(1), 2, 300, 100, 50).frame).toMatchObject({ width: 160, height: 80 });
  });

  it('magnified, slides the card so the part in focus is in sight, darkens the rest and outlines it', () => {
    const layout = layoutZoom(zoom(2, [5, 60, 12, 10]), 0.5, 300, 2000, 456);
    // Twice the frame; the part's middle (11%, 65%) as near the frame's middle as the card's edges allow
    expect(layout.card).toEqual({ left: 0, top: -320, width: 400, height: 800 });
    expect(layout.dim).toBe('M0 0H200V400H0ZM20 160H68V240H20Z');
    expect(layout.marks).toEqual([{ left: 17, top: 157, width: 54, height: 86 }]);
    expect(layout.focus).toEqual([{ left: 736 + 20, top: 28 + 160, width: 48, height: 80 }]);
  });

  it('gives a part out of sight no outline, and darkens the card all the same', () => {
    const layout = layoutZoom(zoom(4, [0, 0, 10, 10], [95, 95, 5, 5]), 0.5, 300, 2000, 456);
    expect(layout.card).toMatchObject({ left: -300, top: -600 });
    expect(layout.dim).toBe('M0 0H200V400H0Z');
    expect(layout.marks).toEqual([]);
    expect(layout.focus).toEqual([]);
  });

  it("takes a card's shape until its picture has loaded: upright, or a site on its side", () => {
    expect(cardShape(false)).toBeCloseTo(357 / 497);
    expect(cardShape(true)).toBeCloseTo(497 / 355);
  });
});
