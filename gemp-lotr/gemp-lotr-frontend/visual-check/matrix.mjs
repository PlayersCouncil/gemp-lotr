// Every control of the deck builder against every gesture it can get, in every state it appears in (`--matrix`). What
// the manifest below lists is generated into scenarios, which run.mjs compares like any other. run.mjs also lists what
// is really on both pages in each state (`crawl`) and fails on anything that has no test here, or is only on one page.

import { CONTROLS } from './controls.mjs';
import { FIXTURE_DECK, pause } from './scenarios.mjs';

const CULTURES = [
  'DWARVEN',
  'ELVEN',
  'GANDALF',
  'GONDOR',
  'ROHAN',
  'SHIRE',
  'GOLLUM',
  'DUNLAND',
  'ISENGARD',
  'MORIA',
  'RAIDER',
  'WRAITH',
  'SAURON',
  'MEN',
  'ORC',
  'URUK_HAI',
  'ESGAROTH',
  'GUNDABAD',
  'MIRKWOOD',
  'SMAUG',
  'SPIDER',
  'TROLL',
];

async function centre(locator) {
  const box = await locator.boundingBox({ timeout: 5000 });
  return { x: box.x + box.width / 2, y: box.y + box.height / 2 };
}

async function moveTo(page, target) {
  const { x, y } = await centre(target);
  await page.mouse.move(x, y);
  return { x, y };
}

/** With the mouse, at the element's centre: a real press lands on whatever is there, even on a disabled control. */
const click = async (page, target, options) => {
  await moveTo(page, target);
  await page.mouse.down(options);
  await page.mouse.up(options);
};

const withShift = async (page, action) => {
  await page.keyboard.down('Shift');
  await action();
  await page.keyboard.up('Shift');
};

const GESTURES = {
  hover: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 700);
  },
  leave: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 300);
    await page.mouse.move(1, 1);
    await pause(page, 300);
  },
  /** Held down (the pressed look), then released (which is also the click). */
  press: async (page, target) => {
    await moveTo(page, target);
    await page.mouse.down();
    await pause(page, 300);
    await page.mouse.up();
    await pause(page, 800);
  },
  rightClick: async (page, target) => {
    await click(page, target, { button: 'right' });
    await pause(page, 500);
  },
  shiftClick: async (page, target) => {
    await withShift(page, () => click(page, target));
    await pause(page, 500);
  },
  // Both presses at one point (the first may close or replace what was under the mouse), as quickly as a person does
  doubleClick: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.click(x, y);
    await page.waitForTimeout(120);
    await page.mouse.down({ clickCount: 2 });
    await page.mouse.up({ clickCount: 2 });
    await pause(page, 800);
  },
  focus: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await pause(page, 300);
  },
  enter: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Enter');
    await pause(page, 800);
  },
  space: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Space');
    await pause(page, 800);
  },
  escape: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Escape');
    await pause(page, 500);
  },
  type: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.type('gandalf');
    await pause(page, 300);
    await page.keyboard.press('Tab');
    await pause(page, 800);
  },
  arrows: async (page, target) => {
    await target.focus({ timeout: 5000 });
    for (const key of ['ArrowRight', 'ArrowRight', 'ArrowLeft', 'Home', 'End']) {
      await page.keyboard.press(key);
      await pause(page, 500);
    }
  },
  dragSideways: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.down();
    await page.mouse.move(x - 60, y, { steps: 6 });
    await pause(page, 300);
    await page.mouse.up();
    await pause(page, 800);
  },
  dragUp: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.down();
    await page.mouse.move(x, y - 40, { steps: 4 });
    await page.mouse.up();
    await pause(page, 800);
  },
  hoverThenShift: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 800);
    await page.keyboard.down('Shift');
    await page.keyboard.up('Shift');
    await pause(page, 300);
  },
  eachOption: async (page, target) => {
    const values = await target.locator('option').evaluateAll((options) => options.map((o) => o.value));
    for (const value of values) {
      // Choosing one can change the list: the lists themselves are compared after each step
      const available = await target.evaluate(
        (select, wanted) => [...select.options].some((option) => option.value === wanted && !option.disabled),
        value,
      );
      if (!available) continue;
      await target.selectOption(value, { timeout: 5000 });
      await pause(page, 300);
    }
  },
  tap: async (page, target) => {
    const { x, y } = await centre(target);
    await page.touchscreen.tap(x, y);
    await pause(page, 800);
  },
};

/** What can be done to each kind of control. A gesture left out has its reason in NOT_APPLICABLE. */
const KINDS = {
  button: ['hover', 'leave', 'press', 'rightClick', 'shiftClick', 'doubleClick', 'focus', 'enter', 'space'],
  select: ['hover', 'focus', 'rightClick', 'eachOption'],
  text: ['hover', 'press', 'focus', 'type', 'escape'],
  range: ['hover', 'focus', 'arrows', 'dragSideways'],
  // Following a link would leave the page: where it leads is compared as part of the page's state
  link: ['hover', 'focus'],
  card: ['hover', 'leave', 'press', 'rightClick', 'shiftClick', 'doubleClick', 'dragUp', 'hoverThenShift'],
  culture: ['hover', 'leave', 'press', 'rightClick', 'focus', 'space'],
  bar: ['hover', 'leave', 'dragSideways', 'rightClick', 'doubleClick'],
};

/** Run on a touch screen as well, on these. */
const TOUCH_KINDS = new Set(['button', 'card']);

const waitForPage = async (page, ms = 1200) => {
  await page.waitForTimeout(ms);
  await page.settleNow();
};

const openDeckList = async (page, $) => {
  await page.click($.deckList);
  await waitForPage(page);
};

/** The states to test in: each is reached from the page as it opens. */
export const STATES = {
  empty: async () => {},
  deck: async (page, $) => {
    await openDeckList(page, $);
    await page.locator($.deckListItem, { hasText: FIXTURE_DECK.name }).first().locator($.openDeck).click();
    await waitForPage(page, 1500);
    await page.keyboard.press('Escape');
    await waitForPage(page, 300);
  },
  myCards: async (page, $) => {
    await page.selectOption($.collection, 'permanent');
    await waitForPage(page, 1500);
  },
  closed: async (page, $) => {
    await page.click($.toggleCollection);
    await waitForPage(page, 500);
  },
  deckList: openDeckList,
  library: async (page, $) => {
    await page.click($.library);
    await waitForPage(page);
  },
  importDialog: async (page, $) => {
    await page.click($.importDeck);
    await waitForPage(page, 500);
  },
  notes: async (page, $) => {
    await page.click($.notes);
    await waitForPage(page, 500);
  },
  cardInfo: async (page, $) => {
    await page.locator($.firstCard).first().click({ button: 'right' });
    await waitForPage(page, 500);
  },
  selection: async (page, $) => {
    await page.selectOption($.collection, 'permanent');
    await waitForPage(page, 1500);
    await page.locator($.selectionPacks).first().click();
    await waitForPage(page, 1000);
  },
};

const entries = (kind, states, names, extra = {}) =>
  names.map((control) => ({
    control,
    kind,
    states,
    ...extra,
    target: extra.target ?? ((page, $) => page.locator($[control]).first()),
  }));

const FILTER_STATES = ['empty'];
const SELECTS = [
  'product',
  'rarity',
  'block',
  'set',
  'side',
  'sort',
  'cardType',
  'keyword',
  'phase',
  'race',
  'itemClass',
  'signet',
  'twilightCompare',
  'siteNumberCompare',
  'strengthCompare',
  'vitalityCompare',
  'resistanceCompare',
];
const DECK_STATES = ['empty', 'deck'];

/** What is tested, and where. The `control` names are those of controls.mjs. */
export const MANIFEST = [
  ...entries('button', DECK_STATES, [
    'newDeck',
    'renameDeck',
    'copyDeck',
    'importDeck',
    'library',
    'deckList',
    'convertErrata',
    'notes',
  ]),
  // Not offered on a touch screen
  ...entries('button', DECK_STATES, ['autoZoom'], { noTouch: true }),
  ...entries('button', ['empty'], ['saveDeck']),
  ...entries('button', ['deck'], ['saveDeck'], { mutating: true }),
  ...entries('select', DECK_STATES, ['format', 'collection']),
  ...entries('button', FILTER_STATES, ['previousPage', 'nextPage', 'resetAdvanced', 'resetAll']),
  ...entries('range', FILTER_STATES, ['pageSize'], {
    // The owner had the handle changed to stay inside its bar, so the same distance gives another value: the
    // page-size-slider scenario drags to the same value on both pages instead
    without: ['dragSideways'],
    // The legacy slider's handle is what takes the focus and the mouse; the Angular range input is both
    target: (page, $, which) => page.locator(which === 'legacy' ? `${$.pageSize} a` : $.pageSize).first(),
  }),
  ...entries('select', FILTER_STATES, SELECTS),
  ...entries('text', FILTER_STATES, ['name', 'gametext']),
  ...CULTURES.map((code) => ({
    control: `culture ${code}`,
    kind: 'culture',
    states: FILTER_STATES,
    target: (page, $) => page.locator($.culture(code)).first(),
  })),
  ...entries('link', FILTER_STATES, ['filterWiki']),
  ...entries('button', ['empty'], ['ringBearerSlot', 'ringSlot', 'sitesSlot']),
  ...entries('bar', ['empty'], ['separator']),
  ...entries('button', ['empty', 'closed'], ['toggleCollection']),
  ...[0, 1, 2, 3, 4, 5, 6].map((i) => ({
    control: `item ${i}`,
    kind: 'card',
    states: ['myCards'],
    target: (page, $) => page.locator($.firstCard).nth(i),
  })),
  ...entries('card', ['deck'], ['ringBearerCard', 'ringCard', 'drawCard']),
  ...entries('card', ['deck'], ['siteCards']),
  ...entries('button', ['deckList', 'library', 'importDialog', 'notes'], ['dialogClose']),
  ...entries('button', ['deckList', 'library'], ['openDeck']),
  ...entries('button', ['deckList'], ['deckRename', 'deckShare', 'deckDelete']),
  ...entries('button', ['library'], ['libraryShow']),
  ...entries('text', ['importDialog'], ['importText']),
  ...entries('button', ['importDialog'], ['importButton']),
  ...entries('text', ['notes'], ['notesText']),
  ...entries('range', ['cardInfo'], ['cardInfoZoom'], {
    target: (page, $, which) => page.locator(which === 'legacy' ? `${$.cardInfoZoom} a` : $.cardInfoZoom).first(),
  }),
  ...entries('card', ['cardInfo'], ['cardInfoCard']),
  ...entries('button', ['cardInfo'], ['cardInfoClose']),
  ...entries('link', ['cardInfo'], ['cardInfoWiki']),
  ...entries('card', ['selection'], ['selectionChoice']),
];

/** Saying yes to what the toolbar and the deck list ask: these change the saved decks. */
const ANSWERED = [
  { control: 'saveDeck', state: 'empty', answers: ['Matrix deck'] },
  { control: 'newDeck', state: 'deck', answers: [true] },
  { control: 'renameDeck', state: 'deck', answers: ['Matrix renamed'] },
  { control: 'copyDeck', state: 'deck', answers: [true] },
  { control: 'convertErrata', state: 'deck', answers: [true] },
  { control: 'deckRename', state: 'deckList', answers: ['Matrix renamed'] },
  { control: 'deckDelete', state: 'deckList', answers: [true] },
];

/** Using these really opens a pack: run one at a time, and the random contents are replayed to the second page. */
const USE_GESTURES = new Set(['press', 'doubleClick', 'tap', 'enter', 'space']);
const opensPack = (state, control, gesture) =>
  USE_GESTURES.has(gesture) &&
  (['item 0', 'item 1'].includes(control) ? state === 'myCards' : control === 'selectionChoice');

export function matrixScenarios() {
  const scenarios = [];
  const name = (state, control, gesture) => `${state}--${control.replace(' ', '-')}--${gesture}`;
  for (const entry of MANIFEST) {
    for (const state of entry.states) {
      const add = (gesture, extra = {}) =>
        scenarios.push({
          name: name(state, entry.control, gesture),
          keepMouse: true,
          ...(opensPack(state, entry.control, gesture) ? { mutating: true, replay: true } : {}),
          // Only what the controls do is compared: this list is generated, and each scenario is short
          note: `${entry.control} (${entry.kind}) in the ${state} state: ${gesture}`,
          mutating: entry.mutating ?? opensPack(state, entry.control, gesture),
          setup: STATES[state],
          run: async (page, $, which) => GESTURES[gesture](page, entry.target(page, $, which)),
          ...extra,
        });
      for (const gesture of KINDS[entry.kind]) {
        if (!entry.without?.includes(gesture)) add(gesture);
      }
      if (TOUCH_KINDS.has(entry.kind) && !entry.noTouch) {
        add('tap', { hasTouch: true });
      }
    }
  }
  for (const { control, state, answers } of ANSWERED) {
    const entry = MANIFEST.find((candidate) => candidate.control === control);
    scenarios.push({
      name: name(state, control, 'yes'),
      note: `${control} in the ${state} state, answering yes`,
      mutating: true,
      answers,
      setup: STATES[state],
      run: async (page, $, which) => GESTURES.press(page, entry.target(page, $, which)),
    });
  }
  for (const state of Object.keys(STATES)) {
    scenarios.push({
      name: name(state, 'keyboard', 'tabOrder'),
      note: `Where Tab goes in the ${state} state`,
      timing: true,
      setup: STATES[state],
      run: async (page) => {
        for (let i = 0; i < 45; i++) {
          await page.keyboard.press('Tab');
          await pause(page, 30);
        }
      },
    });
    scenarios.push({
      name: name(state, 'keyboard', 'escape'),
      note: `Escape in the ${state} state`,
      setup: STATES[state],
      run: async (page) => {
        await page.keyboard.press('Escape');
        await pause(page, 500);
      },
    });
  }
  return scenarios;
}

/** What can be used on each page. */
const INTERACTIVE = {
  legacy:
    'button, a[href], input:not([type=hidden]), select, textarea, .ui-slider-handle, .actionArea, ' +
    '.ui-layout-resizer, .ui-layout-toggler, .ui-dialog-titlebar-close, .card-group, #culture-buttons label',
  angular: 'button, a[href], input, select, textarea, [role=separator], label.gd-culture',
};

/** Runs in the page: the controls it has now, each with the name controls.mjs gives it, or a description if none. */
function listControls({ interactive, named, cardsOf, cultureOf }) {
  const visible = (el) => {
    const box = el.getBoundingClientRect();
    return box.width > 0 && box.height > 0 && getComputedStyle(el).visibility !== 'hidden';
  };
  const plain = (selector) => selector.replace(/:visible/g, '');
  const found = new Set();
  for (const el of document.querySelectorAll(interactive)) {
    if (!visible(el)) continue;
    if (el.closest(cardsOf)) {
      found.add('a card');
      continue;
    }
    if (el.closest(cultureOf)) {
      found.add('a culture');
      continue;
    }
    const match = named.find(([, selector]) => el.matches(plain(selector)) || el.closest(plain(selector)));
    found.add(
      match
        ? match[0]
        : `unnamed: ${el.tagName.toLowerCase()}#${el.id}.${el.className} ${el.textContent.trim().slice(0, 30)}`,
    );
  }
  return [...found];
}

/** Every named control with one selector, for `listControls`, without the groups that contain others. */
function namedControls(which) {
  const groups = [
    'collectionPane',
    'stats',
    'deckName',
    'dialogTitles',
    'firstCard',
    'collectionCards',
    'selectionPacks',
    'deckListItem',
  ];
  return Object.entries(CONTROLS)
    .filter(([name, control]) => typeof control !== 'function' && !groups.includes(name))
    .map(([name, control]) => [name, control[which]]);
}

/**
 * What is really on the page in each state, on both pages, against the manifest. `open(which, state)` gives a page in
 * that state. Returns one line per gap.
 */
export async function coverageGaps(open) {
  const gaps = [];
  const tested = new Set(
    MANIFEST.flatMap((entry) =>
      entry.states.map((state) => `${state}/${entry.control.startsWith('culture') ? 'a culture' : entry.control}`),
    ),
  );
  const CARD_STATES = new Set([
    'empty',
    'deck',
    'myCards',
    'cardInfo',
    'selection',
    'closed',
    'deckList',
    'library',
    'importDialog',
    'notes',
  ]);
  for (const state of Object.keys(STATES)) {
    const lists = {};
    for (const which of ['legacy', 'angular']) {
      const opened = await open(which, STATES[state]).catch((error) => {
        gaps.push(`${state}: could not reach this state on the ${which} page: ${error.message.split('\n')[0]}`);
        return null;
      });
      if (!opened) {
        lists[which] = [];
        continue;
      }
      const { page, close } = opened;
      lists[which] = await page.evaluate(listControls, {
        interactive: INTERACTIVE[which],
        named: namedControls(which),
        cardsOf: which === 'legacy' ? '.card, #card-image-content, #previewImage' : '.gd-card, .gd-card-info__card',
        cultureOf: which === 'legacy' ? '#culture-buttons' : '.gd-cultures',
      });
      await close();
    }
    for (const which of ['legacy', 'angular']) {
      const other = which === 'legacy' ? 'angular' : 'legacy';
      for (const control of lists[which]) {
        if (control.startsWith('unnamed')) {
          gaps.push(`${state}: ${which} page has a control with no name in controls.mjs: ${control}`);
        } else if (!lists[other].includes(control) && !control.startsWith('a ')) {
          gaps.push(`${state}: ${control} is only on the ${which} page`);
        } else if (control === 'a card' && CARD_STATES.has(state)) {
          // Cards are tested by position in myCards and by slot in deck; elsewhere they are the same components
        } else if (
          !control.startsWith('a card') &&
          !tested.has(`${state}/${control}`) &&
          !OUT_OF_STATE.has(`${state}/${control}`)
        ) {
          gaps.push(`${state}: ${control} has no test in the manifest`);
        }
      }
    }
  }
  return [...new Set(gaps)];
}

/**
 * Controls present in a state but tested in another, where they behave the same: the toolbar and the filter are tested
 * with no dialog open (a modal dialog covers them) and with the deck open or not.
 */
const OUT_OF_STATE = new Set(
  ['deck', 'myCards', 'closed', 'deckList', 'library', 'importDialog', 'notes', 'cardInfo', 'selection'].flatMap(
    (state) =>
      MANIFEST.filter((entry) => entry.states.includes('empty')).map(
        (entry) => `${state}/${entry.control.startsWith('culture') ? 'a culture' : entry.control}`,
      ),
  ),
);
// The dialog's close button: cardInfoClose in the card information, and tested in the dialogs that have one of their own
OUT_OF_STATE.add('cardInfo/dialogClose').add('selection/dialogClose');
