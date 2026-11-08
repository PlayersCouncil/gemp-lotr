// The states the visual check compares. Each scenario runs on a freshly loaded page, once on deckBuild.html and once
// on the Angular deck builder; both must end up looking the same and asking the server for the same collection page
// and deck stats.
//
//   name       unique id (used for --only and in the report)
//   run(page, $, which)  what the player does; `$` gives the selectors of the page it runs on (`$.nextPage`,
//              `$.culture('ELVEN')`: controls.mjs), `which` is 'legacy' or 'angular' for the few steps that differ
//   keepMouse  leave the mouse where the scenario put it (hover states); otherwise it is moved to the corner first
//   answers    replies to the prompt/confirm dialogs, in order (true = OK, false = Cancel, text for prompts); alerts are
//              always just accepted
//   mutating   changes the account (saves decks, ...): only run with --mutating
//   note       why the scenario exists, shown in the report

import { CONTROLS } from './controls.mjs';

/** The selectors of one page: `$.nextPage`, `$.culture('ELVEN')`, ... (see controls.mjs). */
export function selectors(which) {
  return new Proxy(
    {},
    {
      get: (_, name) => {
        const control = CONTROLS[name];
        if (!control) throw new Error(`No control ${String(name)} in controls.mjs`);
        return typeof control === 'function' ? (...args) => control(...args)[which] : control[which];
      },
    },
  );
}

const collectionCard = (page, $, i) => page.locator($.collectionCards).nth(i);
const pause = (page, ms = 800) => page.waitForTimeout(ms);

/** Where the thumb of a range input is (the legacy slider's handle is an element of its own): a 16px box. */
async function rangeThumb(page, selector) {
  const input = page.locator(selector);
  const box = await input.boundingBox();
  const [value, min, max] = await input.evaluate((el) => [
    el.valueAsNumber,
    Number(el.min || 0),
    Number(el.max || 100),
  ]);
  const thumb = 16;
  const x = box.x + ((value - min) / (max - min || 1)) * (box.width - thumb);
  return { x, y: box.y + box.height / 2 - thumb / 2, width: thumb, height: thumb };
}

async function hover(page, locator, dx = 20, dy = 20) {
  const box = await locator.boundingBox();
  await page.mouse.move(box.x + dx, box.y + dy);
}

/** The page size slider's handle: the legacy slider's element, or the thumb of the Angular range input. */
async function sliderHandle(page, $, which) {
  return which === 'legacy' ? page.locator(`${$.pageSize} a`).boundingBox() : rangeThumb(page, $.pageSize);
}

async function moveToCentre(page, box) {
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
}

/** Moves the mouse onto the element and presses without releasing, for pressed states. */
async function holdMouse(page, selector) {
  await page.hover(selector);
  await page.mouse.down();
}

/** The deck the deck list scenarios open; created by run.mjs before the check (FIXTURE_DECK). */
export const FIXTURE_DECK = {
  name: 'Visual check deck',
  targetFormat: 'fotr_block',
  notes: 'Created by the visual check',
  contents:
    '1_290|1_2|1_319,1_327,1_331,1_337,1_343,1_349,1_354,1_356,1_360|' +
    '1_12,1_12,1_13,1_17,1_30,1_45,1_50,1_76,1_89,1_131,1_131,1_133,1_143,1_165,1_178,1_191',
};

export const SCENARIOS = [
  // --- Page and collection pane ---
  {
    name: 'start',
    note: 'The page as it opens: empty deck, stats, first collection page',
    run: async () => {},
  },
  {
    name: 'culture-buttons',
    note: 'Pressed culture buttons filter the collection',
    run: async (page, $) => {
      await page.click($.culture('ELVEN'));
      await pause(page);
      await page.click($.culture('SHIRE'));
      await pause(page);
    },
  },
  {
    name: 'paging',
    note: 'Next, next, previous page',
    run: async (page, $) => {
      for (const button of [$.nextPage, $.nextPage, $.previousPage]) {
        await page.click(button);
        await pause(page, 1200);
      }
    },
  },
  {
    name: 'page-size-slider',
    note: 'Dragging the page size slider',
    run: async (page, $, which) => {
      const track = await page.locator($.pageSize).boundingBox();
      const handle =
        which === 'legacy' ? await page.locator(`${$.pageSize} a`).boundingBox() : await rangeThumb(page, $.pageSize);
      await page.mouse.move(handle.x + handle.width / 2, handle.y + handle.height / 2);
      await page.mouse.down();
      await page.mouse.move(track.x + track.width * 0.4, handle.y + 3, { steps: 3 });
      await page.mouse.move(track.x + track.width * 0.7, handle.y + 3, { steps: 3 });
      await page.mouse.up();
    },
  },
  {
    name: 'filter-packs-comparison',
    note: 'A numeric comparison, then the Packs product (card fields disabled)',
    run: async (page, $) => {
      await page.selectOption($.twilightCompare, 'GREATER_THAN');
      await page.fill($.twilightValue, '3');
      await page.press($.twilightValue, 'Tab');
      await pause(page);
      await page.selectOption($.product, 'pack');
    },
  },
  {
    name: 'filter-fields',
    note: 'Name, side, sort and a comparison',
    run: async (page, $) => {
      await page.selectOption($.strengthCompare, 'LESS_THAN_OR_EQUAL_TO');
      await page.fill($.strengthValue, '5');
      await page.press($.strengthValue, 'Enter');
      await pause(page);
      await page.fill($.name, 'gandalf the');
      await page.press($.name, 'Tab');
      await pause(page);
      await page.selectOption($.side, 'FREE_PEOPLES');
      await page.selectOption($.sort, 'twilight,name');
    },
  },
  {
    name: 'reset-advanced',
    note: '"Reset advanced filters only" keeps rarity and cultures',
    run: async (page, $) => {
      await page.selectOption($.strengthCompare, 'EQUALS');
      await page.fill($.strengthValue, '5');
      await page.press($.strengthValue, 'Enter');
      await page.selectOption($.rarity, 'R');
      await page.click($.culture('GONDOR'));
      await pause(page);
      await page.click($.resetAdvanced);
    },
  },
  {
    name: 'reset-all',
    note: '"Reset all filters"',
    run: async (page, $) => {
      await page.selectOption($.rarity, 'R');
      await page.selectOption($.block, 'fotr_block');
      await pause(page, 500);
      await page.selectOption($.set, '2');
      await page.click($.culture('GONDOR'));
      await pause(page);
      await page.click($.resetAll);
    },
  },
  {
    name: 'block-set-type',
    note: 'Block, then set, then card type',
    run: async (page, $) => {
      await page.selectOption($.block, 'king_block');
      await pause(page, 500);
      await page.selectOption($.set, { index: 2 });
      await pause(page, 500);
      await page.selectOption($.cardType, 'SITE');
    },
  },
  {
    name: 'my-cards',
    note: 'The "My cards" collection (packs, selection packs and owned cards)',
    run: async (page, $) => page.selectOption($.collection, 'permanent'),
  },
  {
    name: 'trophies',
    note: 'The "Trophies" collection',
    run: async (page, $) => page.selectOption($.collection, 'trophy'),
  },
  {
    name: 'collection-scrolled',
    note: 'The collection pane scrolled down',
    run: async (page, $) => page.locator($.collectionPane).evaluate((pane) => (pane.scrollTop = 400)),
  },
  {
    name: 'hover-states',
    keepMouse: true,
    note: 'Hovering the next-page arrow after pressing a culture button',
    run: async (page, $) => {
      await page.click($.culture('ROHAN'));
      await pause(page);
      await page.hover($.nextPage);
    },
  },
  {
    name: 'pane-resized',
    note: 'Collection pane widened by dragging the resizer, then the window resized',
    run: async (page, $) => {
      const bar = await page.locator($.separator).first().boundingBox();
      await page.mouse.move(bar.x + 3, bar.y + 100);
      await page.mouse.down();
      await page.mouse.move(bar.x - 197, bar.y + 100, { steps: 10 });
      await page.mouse.up();
      await pause(page, 500);
      const size = page.viewportSize();
      await page.setViewportSize({ width: size.width + 120, height: size.height + 100 });
    },
  },
  {
    name: 'pane-closed',
    note: 'Collection pane closed with the toggler',
    run: async (page, $) => page.click($.toggleCollection),
  },

  // --- Deck area ---
  {
    name: 'deck-add-remove',
    note: 'Adding cards (one three times), then removing a copy',
    run: async (page, $) => {
      for (const i of [0, 0, 0, 1, 2, 5]) {
        await collectionCard(page, $, i).click();
        await pause(page, 200);
      }
      await page.locator($.drawDeckCards).first().click();
      await pause(page, 1500);
    },
  },
  {
    name: 'deck-ring-bearer-ring',
    note: 'Filling the Ring-bearer and Ring slots from their filtered collections',
    run: async (page, $) => {
      await page.click($.ringBearerSlot);
      await pause(page, 1500);
      await collectionCard(page, $, 1).click();
      await pause(page, 1500);
      await page.click($.ringSlot);
      await pause(page, 1500);
      await collectionCard(page, $, 0).click();
      await pause(page, 1500);
    },
  },
  {
    name: 'deck-sites',
    note: 'Nine sites from the sites filter, then one removed',
    run: async (page, $) => {
      await page.click($.sitesSlot);
      await pause(page, 1500);
      for (let i = 0; i < 9; i++) {
        await collectionCard(page, $, i).click();
        await pause(page, 150);
      }
      await pause(page);
      await page
        .locator($.siteCards)
        .nth(3)
        .click({ force: true, position: { x: 10, y: 5 } });
      await pause(page, 1500);
    },
  },
  {
    name: 'deck-full',
    note: 'A larger deck over two collection pages, with a Ring-bearer added and removed',
    run: async (page, $) => {
      for (let i = 0; i < 12; i++) {
        await collectionCard(page, $, i).click();
        if (i % 3 === 0) await collectionCard(page, $, i).click();
        await pause(page, 80);
      }
      await page.click($.nextPage);
      await pause(page, 1200);
      for (let i = 0; i < 12; i++) {
        await collectionCard(page, $, i).click();
        await pause(page, 80);
      }
      await page.click($.ringBearerSlot);
      await pause(page, 1200);
      await collectionCard(page, $, 0).click();
      await pause(page);
      await page.locator($.ringBearerCard).click();
      await pause(page, 1500);
    },
  },
  {
    name: 'format-movie',
    note: 'A movie format shows the Map slot',
    run: async (page, $) => {
      await collectionCard(page, $, 0).click();
      await pause(page, 300);
      await page.selectOption($.format, 'pc_movie');
      await pause(page, 1500);
    },
  },
  {
    name: 'format-fellowship',
    note: 'Fellowship Block: its only block selected, later cultures hidden',
    run: async (page, $) => {
      await page.selectOption($.format, 'fotr_block');
      await pause(page, 1500);
    },
  },

  // --- Dialogs ---
  {
    name: 'deck-list',
    note: 'The deck list dialog',
    run: async (page, $) => {
      await page.click($.deckList);
      await pause(page, 1500);
    },
  },
  {
    name: 'library',
    note: 'The library dialog',
    run: async (page, $) => {
      await page.click($.library);
      await pause(page, 1500);
    },
  },
  {
    name: 'import-dialog',
    note: 'The import dialog',
    run: async (page, $) => {
      await page.click($.importDeck);
      await pause(page);
    },
  },
  {
    name: 'notes-dialog',
    note: 'The notes dialog while typing',
    run: async (page, $) => {
      await page.click($.notes);
      await pause(page, 500);
      await page.keyboard.type('Some notes');
    },
  },
  {
    name: 'notes-escape',
    note: 'Notes typed, dialog closed with Escape: the deck is marked modified',
    run: async (page, $) => {
      await page.click($.notes);
      await pause(page, 500);
      await page.keyboard.type('hello notes');
      await page.keyboard.press('Escape');
    },
  },
  {
    name: 'open-deck',
    note: 'A saved deck opened from the deck list (the dialog stays open)',
    run: async (page, $) => {
      await page.click($.deckList);
      await pause(page, 1500);
      await page.locator($.deckListItem, { hasText: FIXTURE_DECK.name }).first().locator($.openDeck).click();
      await pause(page, 2500);
    },
  },
  {
    name: 'open-library-deck',
    note: 'A library deck opened (unsaved, under its name); nothing happens on servers without library decks',
    run: async (page, $) => {
      await page.click($.library);
      await pause(page, 1500);
      const open = page.locator(`${$.deckListItem} ${$.openDeck}`);
      if ((await open.count()) > 0) {
        await open.first().click();
        await pause(page, 2500);
      }
    },
  },
  {
    name: 'import-decklist',
    note: 'A pasted decklist with notes, bullets and labels',
    run: async (page, $) => {
      await page.click($.importDeck);
      await pause(page);
      await page.fill(
        $.importText,
        "1x The One Ring, Isildur's Bane (ring)\nRing-bearer: Frodo, Son of Drogo\n* 3x Gandalf's Pipe\n" +
          '2 Ulaire Enquea [lots of text]\nstarting: Bag End\n2x Bill the Pony (fp)\n1x Weathertop\n',
      );
      await page.click($.importButton);
      await pause(page, 2500);
    },
  },
  {
    name: 'card-info',
    note: 'Card information from a right-click',
    run: async (page, $) => {
      await collectionCard(page, $, 0).click({ button: 'right' });
      await pause(page, 1500);
    },
  },
  {
    name: 'card-info-zoom-flip',
    note: 'Card information: zoom slider moved, card clicked to flip it',
    run: async (page, $) => {
      await collectionCard(page, $, 2).click({ button: 'right' });
      await pause(page);
      const slider = await page.locator($.cardInfoZoom).boundingBox();
      await page.mouse.click(slider.x + slider.width * 0.8, slider.y + slider.height / 2);
      await pause(page);
      await page.locator($.cardInfoCard).click();
      await pause(page);
    },
  },
  {
    name: 'card-info-close-drag',
    note: 'Card information closed by a click, then opened by dragging a card up',
    run: async (page, $) => {
      await collectionCard(page, $, 2).click({ button: 'right' });
      await pause(page);
      // A click on the empty deck area
      await page.mouse.click(500, 200);
      await pause(page, 500);
      const box = await collectionCard(page, $, 1).boundingBox();
      await page.mouse.move(box.x + 20, box.y + 80);
      await page.mouse.down();
      await page.mouse.move(box.x + 20, box.y + 20, { steps: 4 });
      await page.mouse.up();
      await pause(page);
    },
  },
  {
    name: 'selection-pack',
    note: 'The "Choose one" dialog of a selection pack (needs one in "My cards"; nothing is opened)',
    run: async (page, $) => {
      await page.selectOption($.collection, 'permanent');
      await pause(page, 1500);
      await page.locator($.selectionPacks).first().click();
      await pause(page, 1500);
    },
  },

  // --- Hover zoom ---
  {
    name: 'zoom',
    keepMouse: true,
    note: 'Hover zoom of a collection card',
    run: async (page, $) => {
      await hover(page, collectionCard(page, $, 5));
      await pause(page, 1500);
    },
  },
  {
    name: 'zoom-next-card-shift',
    keepMouse: true,
    note: 'Moving to the next card zooms it at once; Shift flips it',
    run: async (page, $) => {
      await hover(page, collectionCard(page, $, 0));
      await pause(page);
      await hover(page, collectionCard(page, $, 1));
      await pause(page, 100);
      await page.keyboard.down('Shift');
      await page.keyboard.up('Shift');
      await pause(page, 300);
    },
  },
  {
    name: 'zoom-off',
    keepMouse: true,
    note: 'Auto-zoom switched off: no zoom',
    run: async (page, $) => {
      await page.click($.autoZoom);
      await pause(page, 300);
      await hover(page, collectionCard(page, $, 0));
      await pause(page, 1200);
      // Switch it back on: the setting is a cookie shared by both pages
      await page.evaluate(() => {
        document.cookie = 'autoZoomInDeckbuilder=true; path=/gemp-lotr';
      });
    },
  },
  {
    name: 'zoom-deck-site',
    keepMouse: true,
    note: 'Hover zoom of a landscape site in an opened deck',
    run: async (page, $) => {
      await page.click($.deckList);
      await pause(page, 1500);
      await page.locator($.deckListItem, { hasText: FIXTURE_DECK.name }).first().locator($.openDeck).click();
      await pause(page, 2000);
      await page.keyboard.press('Escape');
      await pause(page, 300);
      await hover(page, page.locator($.siteCards).nth(2), 30, 5);
      await pause(page, 1200);
    },
  },

  // --- Interaction states: the screenshot is taken while the mouse or keyboard focus stays where the scenario left it ---
  {
    name: 'state-button-hover',
    keepMouse: true,
    note: 'Toolbar button under the mouse',
    run: async (page, $) => page.hover($.saveDeck),
  },
  {
    name: 'state-button-pressed',
    keepMouse: true,
    note: 'Toolbar button held down (never released, so nothing is saved)',
    run: async (page, $) => holdMouse(page, $.saveDeck),
  },
  {
    name: 'state-button-focus',
    keepMouse: true,
    note: 'Toolbar button with the keyboard focus',
    run: async (page, $) => page.focus($.notes),
  },
  {
    name: 'state-button-disabled-hover',
    keepMouse: true,
    note: 'The disabled previous-page arrow under the mouse',
    run: async (page, $) => page.hover($.previousPage, { force: true }),
  },
  {
    name: 'state-culture-hover',
    keepMouse: true,
    note: 'Culture button under the mouse',
    run: async (page, $) => page.hover($.culture('ELVEN')),
  },
  {
    name: 'state-culture-pressed',
    keepMouse: true,
    note: 'Culture button held down',
    run: async (page, $) => holdMouse(page, $.culture('ELVEN')),
  },
  {
    name: 'state-culture-active-hover',
    keepMouse: true,
    note: 'Selected culture button still under the mouse',
    run: async (page, $) => {
      await page.click($.culture('ELVEN'));
      await pause(page);
    },
  },
  {
    name: 'state-culture-focus',
    keepMouse: true,
    note: 'Culture button with the keyboard focus',
    run: async (page, $) => page.focus($.cultureInput('ELVEN')),
  },
  {
    name: 'state-slider-hover',
    keepMouse: true,
    note: 'Slider handle under the mouse',
    run: async (page, $, which) => moveToCentre(page, await sliderHandle(page, $, which)),
  },
  {
    name: 'state-slider-focus',
    keepMouse: true,
    note: 'Slider handle with the keyboard focus',
    run: async (page, $, which) => page.focus(which === 'legacy' ? `${$.pageSize} a` : $.pageSize),
  },
  {
    name: 'state-slider-pressed',
    keepMouse: true,
    note: 'Slider handle held down',
    run: async (page, $, which) => {
      await moveToCentre(page, await sliderHandle(page, $, which));
      await page.mouse.down();
    },
  },
  {
    name: 'state-select-hover',
    keepMouse: true,
    note: 'Format list under the mouse',
    run: async (page, $) => page.hover($.format),
  },
  {
    name: 'state-input-focus',
    keepMouse: true,
    note: 'Text field with the keyboard focus',
    run: async (page, $) => page.focus($.name),
  },
  {
    name: 'state-link-hover',
    keepMouse: true,
    note: 'Link in the Map slot under the mouse',
    run: async (page, $) => {
      await page.selectOption($.format, 'pc_movie');
      await pause(page, 1500);
      await page.hover($.mapLink);
    },
  },
  {
    name: 'state-resizer-hover',
    keepMouse: true,
    note: 'Bar between the panes under the mouse',
    run: async (page, $) => {
      const bar = await page.locator($.separator).first().boundingBox();
      await page.mouse.move(bar.x + 3, bar.y + 100);
    },
  },
  {
    name: 'state-toggler-hover',
    keepMouse: true,
    note: 'Open/close toggler under the mouse',
    run: async (page, $) => page.hover($.toggleCollection),
  },
  {
    name: 'state-resizer-dragging',
    keepMouse: true,
    note: 'Bar between the panes in the middle of a drag',
    run: async (page, $) => {
      const bar = await page.locator($.separator).first().boundingBox();
      await page.mouse.move(bar.x + 3, bar.y + 100);
      await page.mouse.down();
      await page.mouse.move(bar.x - 97, bar.y + 100, { steps: 10 });
    },
  },
  {
    name: 'state-dialog-close-hover',
    keepMouse: true,
    note: "A dialog's close button under the mouse",
    run: async (page, $) => {
      await page.click($.notes);
      await pause(page, 500);
      await page.hover($.dialogClose);
    },
  },
  {
    name: 'state-dialog-close-focus',
    keepMouse: true,
    note: "A dialog's close button with the keyboard focus",
    run: async (page, $) => {
      await page.click($.notes);
      await pause(page, 500);
      await page.focus($.dialogClose);
    },
  },
  {
    name: 'state-dialog-button-hover',
    keepMouse: true,
    note: 'A button inside a dialog under the mouse',
    run: async (page, $) => {
      await page.click($.importDeck);
      await pause(page);
      await page.hover($.importButton);
    },
  },
  {
    name: 'state-deck-item-button-hover',
    keepMouse: true,
    note: 'A button of a deck list entry under the mouse',
    run: async (page, $) => {
      await page.click($.deckList);
      await pause(page, 1500);
      await page.locator($.deckListItem, { hasText: FIXTURE_DECK.name }).first().locator($.openDeck).hover();
    },
  },

  // --- Changes the account (only with --mutating) ---
  {
    name: 'save-rename-copy',
    mutating: true,
    answers: ['xy', 'Visual check saved', 'Visual check renamed', true],
    note: 'Saving a new deck (name too short first), renaming it, copying it',
    run: async (page, $) => {
      await collectionCard(page, $, 0).click();
      await collectionCard(page, $, 3).click();
      await pause(page, 500);
      await page.click($.saveDeck);
      await pause(page, 400);
      await page.click($.saveDeck);
      await pause(page, 1500);
      await collectionCard(page, $, 1).click();
      await pause(page, 500);
      await page.click($.renameDeck);
      await pause(page, 2000);
      await page.click($.copyDeck);
      await pause(page, 500);
    },
  },
];
