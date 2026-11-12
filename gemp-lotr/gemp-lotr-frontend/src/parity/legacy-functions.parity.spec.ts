import { EMPTY_FILTER, FilterFields } from '../core/collection/filter-fields';
import { buildFilterString } from '../core/collection/filter-string';
import { blockOptions, setOptions } from '../core/collection/filter-block-sets';
import { BlockFilter } from '../core/api/models';
import { formatDecklist } from '../core/deck/deck-import';
import { parseDeckStats, StatsTone } from '../core/deck/deck-stats';
import { choices } from '../testing/parity/choices';
import { cardWidth, FitCard, fitCards, rowLayout } from '../ui/shared/fit-cards/fit-cards';
import { FakeGempServer } from '../testing/parity/fake-gemp-server';
import { LegacyPage, openLegacyPage } from '../testing/parity/legacy-page';

/**
 * The legacy deck builder's own functions, called on thousands of generated inputs next to their Angular ports: the
 * filter string the collection is asked with, the decklist clean-up before an import, how the deck stats HTML reads
 * once on the page, and where the card groups put their cards. A failure names the seed and the input.
 */
const CASES = Number(process.env['PARITY_CASES'] ?? 500);
/** The card groups and the format lists run the legacy page's own DOM code, so fewer of those. */
const GROUP_CASES = Math.ceil(CASES / 5);
const FORMAT_CASES = Math.ceil(CASES / 10);

describe('parity with the legacy page: its functions on generated inputs', () => {
  let page: LegacyPage;

  beforeAll(async () => {
    page = await openLegacyPage(new FakeGempServer());
  });
  afterAll(() => page.close());

  describe('the filter string (CardFilter.calculateDeckFilter)', () => {
    const SELECTS: Record<string, string> = {
      product: '#productSelect',
      rarity: '#raritySelect',
      side: '#sideSelect',
      sort: '#sortSelect',
      block: '#blockSelect',
      set: '#setSelect',
      cardType: '#cardTypeSelect',
      keyword: '#keywordSelect',
      phase: '#phaseSelect',
      race: '#raceSelect',
      itemClass: '#itemClassSelect',
      signet: '#signetSelect',
      twilightCompare: '#twilightCompareSelect',
      strengthCompare: '#strengthCompareSelect',
      vitalityCompare: '#vitalityCompareSelect',
      siteNumberCompare: '#siteNumberCompareSelect',
      resistanceCompare: '#resistanceCompareSelect',
    };
    const INPUTS: Record<string, string> = {
      name: '#nameInput',
      gametext: '#gametextInput',
      twilightValue: '#twilightValueInput',
      strengthValue: '#strengthValueInput',
      vitalityValue: '#vitalityValueInput',
      siteNumberValue: '#siteNumberValueInput',
      resistanceValue: '#resistanceValueInput',
    };
    /** The deck builder's predefined filters: the usual one, and those of the Ring-bearer, Ring and Map slots. */
    const OVERRIDES = [
      '',
      'cardType:-THE_ONE_RING',
      'canStartWithRing:true product:card',
      'cardType:THE_ONE_RING product:card',
      'cardType:MAP product:card',
    ];
    /**
     * At most one space in the game text: the legacy page turned only the first into "_" (a fixed bug). No name of
     * spaces only: the legacy page left those spaces in the string, as it did with a field the predefined filter sets
     * (the same fixed bug; the server reads the same search).
     */
    const TEXTS = ['', 'gandalf', 'the one', ' spaced', 'a+b&c', "frodo's", '12', '-3', 'é ü'];
    /** The value fields take numbers only, on both pages (a browser leaves anything else out). */
    const NUMBERS = ['', '0', '3', '12', '-1', '2.5'];
    const FORMATS = [null, 'rev_tow_sta', 'fotr_block', 'pc_movie'];

    const document = () => page.window.document;
    const optionValues = (selector: string) =>
      [...document().querySelectorAll<HTMLOptionElement>(`${selector} option`)].map((option) => option.value);
    const cultureCodes = () =>
      [...document().querySelectorAll('#culture-buttons label')].map((label) => label.getAttribute('for') ?? '');

    it(`is the same for ${CASES} generated filters`, { timeout: 120_000 }, () => {
      const filter = page.deckBuilder.cardFilter as unknown as {
        currentFormat: string | null;
        siteOverride: boolean;
        getCardsInDeckFunc: () => string[];
        calculateDeckFilter: (override: string) => string;
      };
      const $ = (page.window as unknown as { $: (s: string) => { val(v: string): void } }).$;
      const codes = cultureCodes();
      expect(codes.length).toBeGreaterThan(10);
      const pick = choices(7);

      for (let i = 0; i < CASES; i++) {
        const override = pick.of(OVERRIDES);
        const fields: Record<string, string> = {};
        for (const [field, selector] of Object.entries(SELECTS)) {
          fields[field] = pick.chance(0.4) ? pick.of(optionValues(selector)) : (optionValues(selector)[0] ?? '');
        }
        // A field the predefined filter sets: the legacy page glued its value onto the string (a fixed bug)
        if (override.includes('product:')) {
          fields['product'] = '';
        }
        for (const field of Object.keys(INPUTS)) {
          fields[field] = pick.chance(0.4) ? pick.of(field.endsWith('Value') ? NUMBERS : TEXTS) : '';
        }
        const cultures = codes.filter(() => pick.chance(0.15));
        const format = pick.of(FORMATS);
        const siteOverride = pick.chance(0.3);
        const inDeck = pick.chance(0.5) ? ['1_2', '1_319'] : [];

        for (const [field, selector] of Object.entries({ ...SELECTS, ...INPUTS })) {
          $(selector).val(fields[field] ?? '');
        }
        for (const label of document().querySelectorAll('#culture-buttons label')) {
          label.classList.toggle('ui-state-active', cultures.includes(label.getAttribute('for') ?? ''));
        }
        filter.currentFormat = format;
        filter.siteOverride = siteOverride;
        filter.getCardsInDeckFunc = () => inDeck;

        const legacy = filter.calculateDeckFilter(override);
        const angular = buildFilterString({ ...EMPTY_FILTER, ...fields, cultures } as FilterFields, {
          format,
          override,
          siteOverride,
          cardsInDeck: () => inDeck,
        });
        expect(angular, `case ${i}: ${JSON.stringify({ fields, cultures, format, override, siteOverride })}`).toBe(
          legacy,
        );
      }
    });
  });

  describe('the decklist clean-up before an import (parseDecklist)', () => {
    const PARTS = [
      '2x ',
      '1 ',
      'Gandalf',
      'The One Ring',
      ' (note)',
      ' [x]',
      ' (a) (b)',
      '(lead) ',
      'Ring-bearer: ',
      'RING: ',
      'Map: ',
      'Start ',
      'starting ',
      '• ',
      '* ',
      ')',
      ']',
      '[',
      "Frodo's",
      'Élan',
      '  ',
      '\t',
      ' x2',
    ];

    it(`is the same for ${CASES} generated decklists`, { timeout: 120_000 }, () => {
      const deckBuilder = page.deckBuilder as unknown as {
        clearDeck?: () => void;
        importDeckCollection?: (text: string) => void;
        parseDecklist: (text: string) => void;
      };
      const sent: { text: string | null } = { text: null };
      deckBuilder.clearDeck = () => undefined;
      deckBuilder.importDeckCollection = (text) => (sent.text = text);
      const pick = choices(11);
      let compared = 0;
      try {
        for (let i = 0; i < CASES; i++) {
          const lines = Array.from({ length: 1 + pick.below(4) }, () =>
            pick.chance(0.1) ? '' : Array.from({ length: 1 + pick.below(4) }, () => pick.of(PARTS)).join(''),
          );
          const text = lines.join('\n');
          sent.text = null;
          try {
            deckBuilder.parseDecklist(text);
          } catch (error) {
            // A line the legacy page gave up on (a fixed bug), and nothing else: the Angular page must still clean
            // it up
            expect(String(error), JSON.stringify(text)).toMatch(/Maximum call stack size exceeded/);
            expect(() => formatDecklist(text), JSON.stringify(text)).not.toThrow();
            continue;
          }
          compared++;
          expect(formatDecklist(text), `case ${i}: ${JSON.stringify(text)}`).toBe(sent.text);
        }
      } finally {
        delete deckBuilder.clearDeck;
        delete deckBuilder.importDeckCollection;
      }
      expect(compared).toBeGreaterThan(CASES * 0.6);
    });
  });

  describe('the deck stats, as the legacy page showed them ($("#deckStats").html(...))', () => {
    const TONES: Record<string, StatsTone> = { red: 'error', green: 'valid', yellow: 'warning' };
    const PARTS = [
      '<b>',
      '</b>',
      "<font color='red'>",
      '<font color="green">',
      '<font color=yellow>',
      '</font>',
      '<br>',
      '<br/>',
      '<BR>',
      '<B>',
      'Free People',
      ': ',
      '9<60.',
      'a < b',
      '&amp;',
      '&lt;',
      '&gt;',
      '&quot;',
      '&apos;',
      '&#39;',
      '&#x41;',
      '&#X42;',
      '&#x30;',
      '&#x0a;',
      '&#xe9;',
      '&#233;',
      '&nbsp;',
      '&unknown;',
      'Tom & Jerry',
      ' ',
      '\n',
      'Format',
    ];

    interface Part {
      text: string;
      strong: boolean;
      tone: StatsTone | null;
    }

    /** Joins neighbours that look the same, so both sides split their text alike. */
    const merged = (parts: Part[]) =>
      parts.reduce<Part[]>((all, part) => {
        const last = all.at(-1);
        if (last && last.strong === part.strong && last.tone === part.tone) {
          last.text += part.text;
        } else if (part.text) {
          all.push({ ...part });
        }
        return all;
      }, []);

    /** What the browser made of the HTML: its lines (a <br> ends one), each a run of text, bold or not, coloured or not. */
    function shownByBrowser(html: string): Part[][] {
      const box = page.window.document.createElement('div');
      box.innerHTML = html;
      const lines: Part[][] = [[]];
      const walk = (node: Node, strong: boolean, tone: StatsTone | null) => {
        for (const child of node.childNodes) {
          if (child.nodeType === 3) {
            lines.at(-1)!.push({ text: child.nodeValue ?? '', strong, tone });
          } else if (child.nodeType === 1) {
            const element = child as Element;
            const tag = element.tagName.toLowerCase();
            if (tag === 'br') {
              lines.push([]);
              continue;
            }
            const color = tag === 'font' ? TONES[(element.getAttribute('color') ?? '').toLowerCase()] : undefined;
            walk(element, strong || tag === 'b', color ?? tone);
          }
        }
      };
      walk(box, false, null);
      if (lines.length > 1 && lines.at(-1)!.length === 0) {
        lines.pop();
      }
      const result = lines.map(merged);
      return result.length === 1 && result[0]!.length === 0 ? [] : result;
    }

    it(`reads the same for ${CASES} generated stats`, { timeout: 120_000 }, () => {
      const pick = choices(13);
      for (let i = 0; i < CASES; i++) {
        const html = Array.from({ length: 1 + pick.below(12) }, () => pick.of(PARTS)).join('');
        const angular = parseDeckStats(html).lines.map((line) => merged(line.segments.map((s) => ({ ...s }))));
        expect(angular, `case ${i}: ${JSON.stringify(html)}`).toEqual(shownByBrowser(html));
      }
    });
  });

  describe('where a card group puts its cards (NormalCardGroup, the collection and the deck piles)', () => {
    /** The legacy groups' space between cards, and the largest card heights the deck builder gave them. */
    const PADDING = 5;
    const MAX_HEIGHTS = [200, 497];

    interface Placed {
      left: number;
      top: number;
      width: number;
    }

    /** The legacy group's layout of these cards in a box of that size: each card's copies behind it, then the card. */
    function legacyLayout(cards: readonly FitCard[], width: number, height: number, maxHeight: number): Placed[][] {
      const window = page.window as unknown as {
        $: (html: string | Element) => LegacyJq;
        eval: (code: string) => unknown;
        NormalCardGroup: new (
          container: LegacyJq,
          belongs: (card: { zone: string }) => boolean,
          createDiv: boolean,
        ) => {
          maxCardHeight: number;
          setBounds(x: number, y: number, w: number, h: number): void;
        };
      };
      interface LegacyJq {
        append(child: LegacyJq): LegacyJq;
        data(key: string, value?: unknown): unknown;
        remove(): void;
        get(index: number): HTMLElement;
      }
      const Card = window.eval('Card') as {
        new (
          blueprintId: string,
          a: null,
          b: null,
          zone: string,
          id: number,
          owner: string,
        ): { attachedCards: LegacyJq[] };
        CreateCardDiv(...args: unknown[]): LegacyJq;
      };
      const box = window.$('<div></div>');
      window.$(page.window.document.body).append(box);
      let id = 1;
      const element = (horizontal: boolean, zone: string) => {
        const card = new Card(horizontal ? '1_319' : '1_3', null, null, zone, id++, 'player');
        const div = Card.CreateCardDiv('', null, null, false, false, false, false, false);
        div.data('card', card);
        box.append(div);
        return { card, div };
      };
      const stacks = cards.map((card) => {
        const behind = (card.behind ?? []).map((landscape) => element(landscape, 'attached').div);
        const front = element(card.horizontal, 'main');
        front.card.attachedCards.push(...behind);
        return [...behind, front.div];
      });
      const group = new window.NormalCardGroup(box, (card) => card.zone === 'main', false);
      group.maxCardHeight = maxHeight;
      group.setBounds(0, 0, width, height);
      const placed = stacks.map((stack) =>
        stack.map((div) => {
          const style = div.get(0).style;
          return { left: parseFloat(style.left), top: parseFloat(style.top), width: parseFloat(style.width) };
        }),
      );
      box.remove();
      return placed;
    }

    /** The Angular page's: the row height and offset of fitCards, each stack where rowLayout puts it. */
    function angularLayout(cards: readonly FitCard[], width: number, height: number, maxHeight: number): Placed[][] {
      // The legacy group's box: 3px inside its bounds on every side
      const inner = { width: width - 6, height: height - 6 };
      const input = { ...inner, cards, gap: PADDING, maxHeight, fit: 'rows' as const };
      const fit = fitCards(input);
      const step = fit.height + PADDING;
      return rowLayout(cards, fit.height, inner.width, PADDING).map(({ row, x }, i) => {
        const card = cards[i]!;
        const top = 3 + fit.offset + Math.floor(row * step);
        let left = 3 + x;
        const stack: Placed[] = [];
        for (const landscape of card.behind ?? []) {
          const w = cardWidth(landscape, fit.height);
          stack.push({ left, top, width: w });
          left += Math.floor(w * 0.2);
        }
        stack.push({ left, top, width: cardWidth(card.horizontal, fit.height) });
        return stack;
      });
    }

    it(`is the same for ${GROUP_CASES} generated groups`, { timeout: 120_000 }, () => {
      const pick = choices(17);
      let unplaced = 0;
      for (let i = 0; i < GROUP_CASES; i++) {
        const landscapes = pick.chance(0.2);
        const cards: FitCard[] = Array.from({ length: 1 + pick.below(pick.chance(0.5) ? 6 : 30) }, () => {
          const horizontal = landscapes || pick.chance(0.1);
          const copies = pick.chance(0.3) ? pick.below(4) : 0;
          return copies ? { horizontal, behind: Array.from({ length: copies }, () => horizontal) } : { horizontal };
        });
        const width = 60 + pick.below(900);
        const height = 60 + pick.below(700);
        const maxHeight = pick.of(MAX_HEIGHTS);
        const input = `case ${i}: ${JSON.stringify({ width, height, maxHeight, cards })}`;
        const legacy = legacyLayout(cards, width, height, maxHeight);
        const angular = angularLayout(cards, width, height, maxHeight);
        const outside = (card: Placed) => Number.isNaN(card.left) || card.left + card.width > width - 3;
        // The width the legacy group compared with the box's before rounding: a fifth of each copy, and a landscape
        // card as wide as the row is high, fractions included
        const rowHeight = fitCards({
          width: width - 6,
          height: height - 6,
          cards,
          gap: PADDING,
          maxHeight,
          fit: 'rows',
        }).height;
        const unrounded = (card: FitCard) =>
          [card.horizontal, ...(card.behind ?? []).map((landscape) => landscape)].reduce(
            (sum, landscape, copy) =>
              sum + (landscape ? rowHeight : Math.floor(rowHeight * (350 / 490))) * (copy === 0 ? 1 : 0.2),
            0,
          );
        if (legacy.flat().some(outside) || cards.some((card) => unrounded(card) > width - 6)) {
          // A legacy bug the Angular page doesn't copy: a stack ended past the box's edge, and the legacy group kept
          // that layout anyway (it ignored layoutInRows' "false"), leaving the card sticking out of the box and the
          // cards after it not placed at all; or a card a fraction of a pixel wider than the box, before rounding, went
          // to the next row and left its own empty. The Angular page takes more rows, so every card is inside.
          unplaced++;
          for (const card of angular.flat()) {
            expect(card.left + card.width, input).toBeLessThanOrEqual(width - 3);
            expect(card.top, input).toBeGreaterThanOrEqual(3);
          }
          continue;
        }
        expect(angular, input).toEqual(legacy);
      }
      // That bug needs a stack wider than the box at the tried height: it is rare
      expect(unplaced).toBeLessThan(GROUP_CASES / 10);
    });
  });

  describe('the Block and Set lists of a format (CardFilter.updateFormat)', () => {
    const SET_VALUES = ['1', '2', '3', '10', '19', '40', 'pc_errata', '', 'x', '07'];

    it(`are the same for ${FORMAT_CASES} generated formats`, { timeout: 120_000 }, () => {
      const filter = page.deckBuilder.cardFilter as unknown as {
        updateFormat: (format: string, blocks: BlockFilter[]) => void;
      };
      const $ = (page.window as unknown as { $: (s: string) => { val(v: string): { change(): void } } }).$;
      const shown = (selector: string) => {
        const select = page.window.document.querySelector<HTMLSelectElement>(selector)!;
        return {
          options: [...select.options].map((option) => ({ value: option.value, label: option.text })),
          selected: select.value,
        };
      };
      const unbindBlockChanges = () =>
        (page.window as unknown as { $: (s: string) => { unbind(event: string): void } })
          .$('#blockSelect')
          .unbind('change');
      const pick = choices(19);
      let label = 0;
      for (let i = 0; i < FORMAT_CASES; i++) {
        const blocks: BlockFilter[] = Array.from({ length: pick.below(5) }, (_, b) => ({
          name: `Block ${b}`,
          filter: pick.chance(0.1) ? 'same_block' : `block_${b}`,
          setFilters: Object.fromEntries(
            Array.from({ length: pick.below(6) }, () => [`Set ${label++}`, pick.of(SET_VALUES)]),
          ),
        }));
        const input = `case ${i}: ${JSON.stringify(blocks)}`;
        // updateFormat adds a change handler to the Block list each time it runs: only this case's may answer, or each
        // case would run every earlier one's as well
        unbindBlockChanges();
        filter.updateFormat('generated', blocks);
        expect(blockOptions(blocks), input).toEqual(shown('#blockSelect'));
        for (const block of ['', ...blocks.map((b) => b.filter)]) {
          $('#blockSelect').val(block).change();
          expect(setOptions(blocks, block), `${input}, block "${block}"`).toEqual(shown('#setSelect'));
        }
      }
    });
  });
});
