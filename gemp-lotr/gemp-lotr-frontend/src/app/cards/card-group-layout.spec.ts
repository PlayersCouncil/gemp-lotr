import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { CardRect, groupArea, GroupCard, layoutNormalGroup } from './card-group-layout';

type Rect = [number, number, number, number, number];
interface LegacyCase {
  width: number;
  height: number;
  maxCardHeight: number | null;
  cards: [number, number[]?][];
  expected: [Rect, Rect[]?][];
}

/**
 * Layouts made by the legacy NormalCardGroup in the browser (src/testing/legacy-card-group-layouts.generator.js):
 * random cards, some landscape, some with attached copies, in random areas.
 */
const legacyCases: LegacyCase[] = JSON.parse(
  readFileSync(resolve(process.cwd(), 'src/testing/legacy-card-group-layouts.json'), 'utf8'),
);

const asArray = (rect: CardRect): Rect => [rect.left, rect.top, rect.width, rect.height, rect.zIndex];

describe('layoutNormalGroup', () => {
  it('places a collection page like the legacy page at 1280x800', () => {
    // 12 upright cards in the 378x378 collection group (measured on deckBuild.html)
    const cards = Array.from({ length: 12 }, () => ({ horizontal: false }));
    const layout = layoutNormalGroup(cards, groupArea({ left: 0, top: 0, width: 378, height: 378 }), {
      maxCardHeight: 200,
    });
    expect(layout.slice(0, 5).map((l) => l.card)).toEqual([
      { left: 3, top: 3, width: 86, height: 120, zIndex: 10 },
      { left: 94, top: 3, width: 86, height: 120, zIndex: 10 },
      { left: 185, top: 3, width: 86, height: 120, zIndex: 10 },
      { left: 276, top: 3, width: 86, height: 120, zIndex: 10 },
      { left: 3, top: 128, width: 86, height: 120, zIndex: 10 },
    ]);
  });

  it('returns nothing for no cards', () => {
    expect(layoutNormalGroup([], groupArea({ left: 0, top: 0, width: 300, height: 300 }))).toEqual([]);
  });

  /**
   * The legacy code ignored that a row layout had failed (a card wider than the group at that row height): the cards
   * then overflowed the group, or were never placed at all. The port tries more rows instead.
   */
  const brokenInLegacy = (legacy: LegacyCase) =>
    legacy.expected.some(([card, attached]) =>
      [card, ...(attached ?? [])].some(
        (rect) => rect.includes(null as unknown as number) || rect[0] + rect[2] > legacy.width - 3,
      ),
    );
  const toCards = (legacy: LegacyCase): GroupCard[] =>
    legacy.cards.map(([horizontal, attached]) => ({
      horizontal: horizontal === 1,
      attached: (attached ?? []).map((h) => ({ horizontal: h === 1 })),
    }));
  const layoutOf = (legacy: LegacyCase) =>
    layoutNormalGroup(toCards(legacy), groupArea({ left: 0, top: 0, width: legacy.width, height: legacy.height }), {
      maxCardHeight: legacy.maxCardHeight,
    });

  const complete = legacyCases.filter((legacy) => !brokenInLegacy(legacy));
  it(`matches the legacy NormalCardGroup in ${complete.length} recorded layouts`, () => {
    for (const legacy of complete) {
      const actual = layoutOf(legacy).map((l) =>
        l.attached.length ? [asArray(l.card), l.attached.map(asArray)] : [asArray(l.card)],
      );
      expect(
        actual,
        `${legacy.width}x${legacy.height}, max ${legacy.maxCardHeight}, ${legacy.cards.length} cards`,
      ).toEqual(legacy.expected);
    }
  });

  const broken = legacyCases.filter(brokenInLegacy);
  it(`keeps every card inside the group where the legacy layout broke (${broken.length} recorded layouts)`, () => {
    expect(broken.length).toBeGreaterThan(0);
    for (const legacy of broken) {
      const area = groupArea({ left: 0, top: 0, width: legacy.width, height: legacy.height });
      for (const { card } of layoutOf(legacy)) {
        expect(card.left + card.width).toBeLessThanOrEqual(area.left + area.width);
        expect(card.width).toBeGreaterThan(0);
      }
    }
  });
});
