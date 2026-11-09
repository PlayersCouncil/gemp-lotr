import { TestBed } from '@angular/core/testing';
import { LegacyCardScripts, loadLegacyCardScripts } from '../../testing/legacy-cards';
import * as blueprint from './card-blueprint';
import { CardImageService } from './card-image.service';

/**
 * Runs the legacy Card.js against the TypeScript port on many blueprint IDs. If someone changes Card.js (a new
 * horizontal range, a remade errata, ...) without updating card-blueprint.ts, this fails.
 */

/** The checks below run tens of thousands of comparisons; give them room when other test files run alongside. */
const HEAVY_TEST_TIMEOUT = 60_000;

/** The legacy code links the Future Prize image relative to the page; the port uses the same image, absolutely. */
const normalise = (url: string): string => (url.startsWith('images/') ? `/gemp-lotr/${url}` : url);

const SETS = [
  ...range(0, 20),
  ...range(30, 33),
  40,
  ...range(50, 94),
  ...range(100, 103),
  150,
  151,
  400,
  404,
  450,
  600,
];
const CARD_NUMBERS = [
  ...range(1, 20),
  39,
  43,
  44,
  ...range(46, 50),
  ...range(55, 73),
  85,
  87,
  94,
  96,
  97,
  ...range(109, 151),
  162,
  178,
  182,
  ...range(185, 215),
  ...range(227, 237),
  263,
  266,
  273,
  ...range(284, 289),
  302,
  309,
  ...range(318, 370),
  416,
  469,
];
const MARKERS = ['', '*', 'T', '^', 'T*'];
const ZONES = [undefined, 'DECK', 'SUPPORT', 'TOP_OF_DISCARD'];

function range(from: number, to: number): number[] {
  return Array.from({ length: to - from + 1 }, (_, i) => from + i);
}

function sampleBlueprintIds(legacy: LegacyCardScripts): string[] {
  const ids = new Set<string>();
  for (const table of Object.values(legacy.tables)) {
    Object.keys(table).forEach((id) => ids.add(id));
  }
  for (const setNo of SETS) {
    for (const cardNo of CARD_NUMBERS) {
      for (const marker of MARKERS) {
        ids.add(marker === 'T*' ? `${setNo}_${cardNo}T*` : `${setNo}_${cardNo}${marker}`);
      }
    }
  }
  return [...ids];
}

describe('card-blueprint parity with the legacy Card.js', () => {
  let legacy: LegacyCardScripts;
  let ids: string[];
  let tables: blueprint.CardImageTables;

  beforeAll(() => {
    legacy = loadLegacyCardScripts();
    ids = sampleBlueprintIds(legacy);
    tables = legacy.tables as unknown as blueprint.CardImageTables;
  });

  it('checks a meaningful number of blueprint IDs', () => {
    expect(ids.length).toBeGreaterThan(50_000);
  });

  it(
    'matches the static helpers',
    () => {
      const { Card } = legacy;
      const mismatches: string[] = [];
      const check = (what: string, id: string, expected: unknown, actual: unknown) => {
        if (expected !== actual && mismatches.length < 20) {
          mismatches.push(`${what}(${id}): legacy ${String(expected)}, port ${String(actual)}`);
        }
      };
      for (const id of ids) {
        check('strip', id, Card.StripBlueprintId(id), blueprint.stripBlueprintId(id));
        check('foil', id, Card.GetFoil(id), blueprint.isFoil(id));
        check('alternateImage', id, Card.GetAlternateImage(id), blueprint.isAlternateImage(id));
        check('placeholder', id, Card.isPlaceholder(id), blueprint.isPlaceholder(id));
        check('errata', id, Card.isErrata(id), blueprint.isErrata(id));
        check('incomplete', id, Card.isIncomplete(id), blueprint.isIncomplete(id));
        check('blueprintHorizontal', id, Card.isBlueprintHorizontal(id), blueprint.isBlueprintHorizontal(id));
        for (const zone of ZONES) {
          check(`horizontal[${zone}]`, id, Card.isHorizontal(id, zone), blueprint.isHorizontal(id, zone));
        }
        check('imageUrl', id, normalise(Card.getImageUrl(id)), blueprint.getImageUrl(tables, id));
        check('imageUrl[tengwar]', id, normalise(Card.getImageUrl(id, true)), blueprint.getImageUrl(tables, id, true));
        check(
          'imageUrl[ignoreErrata]',
          id,
          normalise(Card.getImageUrl(id, false, true)),
          blueprint.getImageUrl(tables, id, false, true),
        );
      }
      expect(mismatches).toEqual([]);
    },
    HEAVY_TEST_TIMEOUT,
  );

  describe('CardImageService matches new Card(...)', () => {
    let service: CardImageService;

    beforeAll(() => {
      service = TestBed.inject(CardImageService);
    });

    it(
      'for every sampled blueprint ID and zone',
      () => {
        const mismatches: string[] = [];
        for (const id of ids) {
          for (const zone of ZONES) {
            const card = new legacy.Card(id, undefined, undefined, zone);
            const image = service.resolve(id, zone);
            const expected = {
              bareBlueprint: card.bareBlueprint,
              imageUrl: normalise(card.imageUrl),
              backSideImageUrl: card.backSideImageUrl,
              foil: card.foil,
              tengwar: Boolean(card.tengwar),
              incomplete: card.incomplete,
              errata: Boolean(card.hasErrata()),
              horizontal: card.horizontal,
              pack: card.isPack(),
              placeholder: card.isPlaceholder(),
              hasWiki: card.hasWiki,
              // Deliberately https here; the legacy link is http
              wikiUrl: card.getWikiLink().replace('http://', 'https://'),
            };
            const actual = Object.fromEntries(Object.keys(expected).map((k) => [k, image[k as keyof typeof image]]));
            if (JSON.stringify(actual) !== JSON.stringify(expected) && mismatches.length < 10) {
              mismatches.push(`${id} [${zone}]: legacy ${JSON.stringify(expected)}\n  port ${JSON.stringify(actual)}`);
            }
          }
        }
        expect(mismatches).toEqual([]);
      },
      HEAVY_TEST_TIMEOUT,
    );

    it('for the "rules" card', () => {
      expect(service.resolve('rules').imageUrl).toBe(new legacy.Card('rules').imageUrl);
    });
  });
});
