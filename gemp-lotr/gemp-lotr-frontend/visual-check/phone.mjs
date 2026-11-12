// The modern theme on a phone (`run.mjs --phone`): the compact layout, used with a finger.
//
// 1. Every control against every touch gesture, in every state it appears in. Each runs on the Angular page on a phone
//    and, as the same gesture made with the mouse, on the legacy page on a computer: what the pages ask the server, the
//    browser dialogs they open, the state of their controls (values, lists, what is disabled, the deck's name and stats,
//    the open dialog) and what the server holds afterwards must be the same. The screens aren't compared (the layouts
//    differ), nor the focus and the mouse (a finger has neither).
// 2. What only the compact layout has (its two menus, the split, the filters kept shown, the theme in the menu, the
//    card size, long presses, a phone turned sideways, a tablet, switching themes on each device): scenarios with what
//    must happen written out, on the Angular page alone.
// 3. Everything on the Angular page on a phone, in every state and with each menu open, against what 1 and 2 test.

import fs from 'node:fs';
import path from 'node:path';
import { compareScreens } from './compare.mjs';
import { CONTROLS } from './controls.mjs';
import { ANSWERED, GESTURES, INTERACTIVE, listControls, MANIFEST, opensPack, OUT_OF_STATE, STATES } from './matrix.mjs';
import { pause } from './scenarios.mjs';

/** Controls only the compact layout has, by their Angular selector (see the frontend README, DOM contract). */
export const COMPACT_ONLY = {
  /** The top half's burger: the deck's buttons, the theme, notes and the format. */
  deckMenu: '.gd-burger:has(+ dialog.gd-drawer[data-drawer="deck"])',
  /** The bottom half's burger: the collection list and the filters not kept shown. */
  collectionMenu: '.gd-burger:has(+ dialog.gd-drawer[data-drawer="collection"])',
  /** A filter's "Always show" choice, in the collection menu. */
  filterPin: 'label.gd-pin',
  /** The theme, the deck menu's first item. */
  themePicker: '.gd-toolbar__theme',
};

const DRAWER = 'dialog.gd-drawer';
const LEGACY_SIZE = { width: 1366, height: 768 };
/** The legacy page runs on a computer with a touch screen (so neither page offers the hover zoom), used with the mouse. */
export const LEGACY_CONTEXT = { viewport: LEGACY_SIZE, hasTouch: true };
/** A phone: a touch screen, a mobile browser (which reads the page's viewport). */
export const PHONE_CONTEXT = { hasTouch: true, isMobile: true };

// --- Touch ---

async function centre(locator) {
  const box = await locator.boundingBox({ timeout: 5000 });
  return { x: box.x + box.width / 2, y: box.y + box.height / 2 };
}

/** Chromium's input, for what Playwright's touchscreen can't do (hold, move). */
async function input(page, method, params) {
  page.__touchSession ??= await page.context().newCDPSession(page);
  await page.__touchSession.send(method, params);
}

/** A finger put down at `from`, moved to `to` in `steps`, and lifted. */
export async function touchDrag(page, from, to, { steps = 8 } = {}) {
  await input(page, 'Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [from] });
  for (let i = 1; i <= steps; i++) {
    const point = { x: from.x + ((to.x - from.x) * i) / steps, y: from.y + ((to.y - from.y) * i) / steps };
    await input(page, 'Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [point] });
    await page.waitForTimeout(16);
  }
  await input(page, 'Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
}

/**
 * A tap at the control's centre, whatever is there (also on a disabled control), scrolled into view first. Playwright
 * finds the point also on a page the phone shows zoomed out (the classic theme sets no viewport, as the legacy page).
 */
export async function tap(page, locator) {
  await locator.tap({ force: true, timeout: 5000 });
}

/**
 * A finger held still on it for 800ms, then lifted (LONG_PRESS_MS is 500): Chromium's own gesture, which, as on a
 * phone, sends no click when the finger lifts (a touch put down and lifted by hand would be a slow tap).
 */
export async function longPress(page, locator) {
  const { x, y } = await centre(locator);
  await input(page, 'Input.synthesizeTapGesture', { x, y, duration: 800, tapCount: 1, gestureSourceType: 'touch' });
}

/**
 * Makes a control usable on the Angular page on a phone, as a player would: closes a menu open over the page unless
 * the control is in it, opens the menu the control is in, and scrolls it into view.
 */
export async function reach(page, target) {
  const locator = typeof target === 'string' ? page.locator(target).first() : target;
  await locator.waitFor({ state: 'attached', timeout: 5000 });
  const where = await locator.evaluate((element, drawer) => {
    const inside = element.closest(drawer);
    return {
      menu: inside?.getAttribute('data-drawer') ?? null,
      menuOpen: inside?.open ?? false,
      open: document.querySelector(`${drawer}[open]`)?.getAttribute('data-drawer') ?? null,
    };
  }, DRAWER);
  if (where.open && where.open !== where.menu) await closeMenu(page);
  if (where.menu && !where.menuOpen) {
    await tap(page, page.locator(`.gd-burger:has(+ ${DRAWER}[data-drawer="${where.menu}"])`));
    await page.waitForTimeout(300);
  }
  await locator.scrollIntoViewIfNeeded({ timeout: 5000 });
}

/** Closes the open menu, if any, by tapping the page beside it (its backdrop). */
export async function closeMenu(page) {
  const open = page.locator(`${DRAWER}[open]`);
  if (!(await open.count())) return;
  const box = await open.boundingBox();
  const viewport = page.viewportSize();
  await page.touchscreen.tap(Math.min(viewport.width - 4, box.x + box.width + 20), viewport.height / 2);
  await page.waitForTimeout(300);
}

// --- Gestures: on the phone with a finger, on the legacy page with the mouse (as matrix.mjs makes it) ---

/**
 * A slider (`control`) moved to 40% and then 70% of its track, as the page-size-slider scenario does: with a finger on
 * the phone, with the mouse on the legacy page. The compact layout's card size slider (compact-card-size).
 */
async function slide(page, $, which, control) {
  const input = page.locator($[control]).first();
  const box = await input.boundingBox();
  // The legacy slider's element is its track; the Angular thumb's centre travels 8.92px less on each side
  const track = which === 'legacy' ? box : { ...box, x: box.x + 8.92, width: box.width - 17.84 };
  const handle =
    which === 'legacy'
      ? await centre(page.locator(`${$[control]} a`).first())
      : await input.evaluate((el, t) => {
          const share = (el.valueAsNumber - Number(el.min || 0)) / (Number(el.max || 100) - Number(el.min || 0) || 1);
          return { x: t.x + share * t.width, y: t.y + t.height / 2 };
        }, track);
  const to = (share) => ({ x: track.x + track.width * share, y: handle.y });
  if (which === 'legacy') {
    await page.mouse.move(handle.x, handle.y);
    await page.mouse.down();
    await page.mouse.move(to(0.4).x, handle.y + 3, { steps: 3 });
    await page.mouse.move(to(0.7).x, handle.y + 3, { steps: 3 });
    await page.mouse.up();
  } else {
    await touchDrag(page, handle, to(0.4), { steps: 3 });
    await touchDrag(page, to(0.4), to(0.7), { steps: 3 });
  }
  await pause(page, 800);
}

/**
 * Each touch gesture: what the finger does on the phone, and the same thing done with the mouse on the legacy page.
 * Each makes the same number of steps (pauses) on both.
 */
const TOUCH_GESTURES = {
  tap: {
    phone: async (page, target) => {
      await tap(page, target);
      await pause(page, 800);
    },
    legacy: async (page, target) => {
      const { x, y } = await centre(target);
      await page.mouse.move(x, y);
      await page.mouse.down();
      await page.mouse.up();
      await pause(page, 800);
    },
  },
  // Both taps at one point: the first may close the menu the control was in, and the second lands on what was under it
  doubleTap: {
    phone: async (page, target) => {
      const { x, y } = await centre(target);
      await page.touchscreen.tap(x, y);
      await page.waitForTimeout(120);
      await page.touchscreen.tap(x, y);
      await pause(page, 800);
    },
    legacy: GESTURES.doubleClick,
  },
  /** On a phone what the right button is on a computer: a card's details, nothing on a button. */
  longPress: {
    phone: async (page, target) => {
      await longPress(page, target);
      await pause(page, 500);
    },
    legacy: GESTURES.rightClick,
  },
  /**
   * A finger moved up from a card scrolls what it is in (it doesn't drag the card, as the mouse does on a computer: a
   * long press shows the card's details instead); on the computer, the mouse's wheel turned over it.
   */
  swipeUp: {
    phone: async (page, target) => {
      const from = await centre(target);
      await touchDrag(page, from, { x: from.x, y: from.y - 40 }, { steps: 4 });
      await pause(page, 800);
    },
    legacy: async (page, target) => {
      const { x, y } = await centre(target);
      await page.mouse.move(x, y);
      await page.mouse.wheel(0, 40);
      await pause(page, 800);
    },
  },
  /** Typed into a field after tapping it, and sent with the keyboard's Enter (a phone has no Tab). */
  typeIn: {
    phone: async (page, target) => {
      await tap(page, target);
      await page.keyboard.type('gandalf');
      await pause(page, 300);
      await page.keyboard.press('Enter');
      await pause(page, 800);
    },
    legacy: async (page, target) => {
      const { x, y } = await centre(target);
      await page.mouse.click(x, y);
      await page.keyboard.type('gandalf');
      await pause(page, 300);
      await page.keyboard.press('Enter');
      await pause(page, 800);
    },
  },
  /** Every choice of a list, in turn: a phone's own picker sets it like any other. */
  eachOption: { phone: GESTURES.eachOption, legacy: GESTURES.eachOption },
};

/** What can be done with a finger to each kind of control (matrix.mjs KINDS). */
const TOUCH_KINDS = {
  button: ['tap', 'doubleTap', 'longPress'],
  select: ['eachOption'],
  text: ['tap', 'typeIn', 'longPress'],
  range: ['slide'],
  // Following a link leaves the page: where it leads is compared as part of the page's state
  link: ['longPress'],
  card: ['tap', 'doubleTap', 'longPress', 'swipeUp'],
  culture: ['tap', 'doubleTap', 'longPress'],
};

/**
 * Left out of the comparison, each tested by a compact scenario below instead: in the compact layout the separator moves
 * the split between the halves (the legacy one resized the collection beside the deck), there is no toggle (or
 * collection closed by it), and the pager's slider sets the card size, not the number of cards on a page (owner,
 * 2026-11-12).
 */
export const COMPARED_ELSEWHERE = new Set(['separator', 'toggleCollection', 'pageSize']);
/** Not on a touch screen, on either page: the card details' zoom slider is hidden there (as the hover zoom is). */
const NOT_ON_TOUCH = new Set(['cardInfoZoom']);
const PHONE_STATES = Object.keys(STATES).filter((state) => state !== 'closed');

/**
 * Targets that pick another element on the phone: the pages list the sites in different orders (the legacy column
 * stacks them, the phone's strip lays them side by side), so the same site is used on both: the fixture deck's last,
 * which the legacy column shows whole (it lies over the others).
 */
const PHONE_TARGETS = {
  siteCards: (page, $, which) =>
    page
      .locator(
        which === 'legacy'
          ? '#sitesDiv .cardInDeck:has(img[src*="01360"]) .actionArea'
          : '.gd-slot[data-slot="sites"] .gd-card:has(img[src*="01360"])',
      )
      .first(),
};

/** The comparison scenarios (1): every manifest entry against every touch gesture. */
export function phoneScenarios() {
  const scenarios = [];
  const name = (state, control, gesture) => `${state}--${control.replace(' ', '-')}--${gesture}`;
  for (const entry of MANIFEST) {
    if (COMPARED_ELSEWHERE.has(entry.control) || NOT_ON_TOUCH.has(entry.control) || entry.noTouch) continue;
    for (const state of entry.states.filter((s) => PHONE_STATES.includes(s))) {
      for (const gesture of TOUCH_KINDS[entry.kind]) {
        const used = gesture === 'tap' || gesture === 'doubleTap';
        const opens = opensPack(state, entry.control, used ? 'press' : gesture);
        scenarios.push({
          name: name(state, entry.control, gesture),
          note: `${entry.control} (${entry.kind}) in the ${state} state: ${gesture}, on a phone`,
          ...(opens ? { replay: true } : {}),
          mutating: entry.mutating ?? opens,
          setup: STATES[state],
          run: async (page, $, which) => {
            if (gesture === 'slide') {
              if (which === 'angular') await reach(page, $[entry.control]);
              return slide(page, $, which, entry.control);
            }
            const target = (PHONE_TARGETS[entry.control] ?? entry.target)(page, $, which);
            if (which === 'legacy') return TOUCH_GESTURES[gesture].legacy(page, target);
            await reach(page, target);
            return TOUCH_GESTURES[gesture].phone(page, target);
          },
        });
      }
    }
  }
  for (const { control, state, answers } of ANSWERED) {
    const entry = MANIFEST.find((candidate) => candidate.control === control);
    scenarios.push({
      name: name(state, control, 'yes'),
      note: `${control} in the ${state} state, tapped and answered yes, on a phone`,
      mutating: true,
      answers,
      setup: STATES[state],
      run: async (page, $, which) => {
        const target = entry.target(page, $, which);
        if (which === 'legacy') return TOUCH_GESTURES.tap.legacy(page, target);
        await reach(page, target);
        return TOUCH_GESTURES.tap.phone(page, target);
      },
    });
  }
  return scenarios;
}

// --- The compact layout's own behaviour (2) ---

class Expectation extends Error {}
function expect(condition, message) {
  if (!condition) throw new Expectation(message);
}

const state = (page) =>
  page.evaluate(() => {
    const separator = document.querySelector('.gd-page__separator');
    return {
      layout: document.querySelector('[data-layout]')?.getAttribute('data-layout') ?? null,
      split: document.querySelector('[data-split]')?.getAttribute('data-split') ?? null,
      share: separator ? Number(separator.getAttribute('aria-valuenow')) : null,
      viewport: document.querySelector('meta[name=viewport]')?.getAttribute('content') ?? null,
      menus: [...document.querySelectorAll('dialog.gd-drawer[open]')].map((d) => d.getAttribute('data-drawer')),
      dialogs: [...document.querySelectorAll('dialog[open]:not(.gd-drawer)')].map(
        (d) => d.getAttribute('data-dialog') ?? d.className,
      ),
    };
  });

const cookie = async (page, name) =>
  (await page.context().cookies()).find((candidate) => candidate.name === name)?.value ?? null;

async function reload(page) {
  await page.reload();
  await page.waitForSelector(CONTROLS.firstCard.angular, { timeout: 20000 });
  await page.settleNow();
}

const deckMenu = (page) => page.locator(COMPACT_ONLY.deckMenu);
const collectionMenu = (page) => page.locator(COMPACT_ONLY.collectionMenu);
const separator = (page) => page.locator(CONTROLS.separator.angular);
const field = (page, name) => page.locator(`.gd-field[data-field="${name}"]`);
const fieldPart = (page, name) =>
  field(page, name).evaluate((el) => el.closest('[data-part]')?.getAttribute('data-part') ?? null);

/** The separator dragged by `dx`, `dy` with a finger. */
async function dragSeparator(page, dx, dy) {
  const from = await centre(separator(page));
  await touchDrag(page, from, { x: from.x + dx, y: from.y + dy }, { steps: 10 });
  await page.waitForTimeout(300);
}

// --- Switching themes, turning the device, resizing the window ---

/** The devices the theme switching is tried on: their screen, and whether they are phones (a mobile browser, touch). */
const DEVICES = {
  computer: { viewport: { width: 1366, height: 768 }, isMobile: false, hasTouch: false },
  narrowWindow: { viewport: { width: 800, height: 768 }, isMobile: false, hasTouch: false },
  phone: { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true },
  phoneSideways: { viewport: { width: 844, height: 390 }, isMobile: true, hasTouch: true },
  tablet: { viewport: { width: 820, height: 1180 }, isMobile: true, hasTouch: true },
  tabletSideways: { viewport: { width: 1180, height: 820 }, isMobile: true, hasTouch: true },
};
/** Where a computer's mouse rests while the screens are compared: on nothing that reacts to it. */
const MOUSE_REST = { x: 1, y: 1 };
/** Pixels a switched page may differ from a freshly loaded one by (the edges of anti-aliased text and corners). */
const SWITCH_TOLERANCE = 60;

/** The theme now: the page's `data-theme`. */
const themeOf = (page) => page.evaluate(() => document.documentElement.getAttribute('data-theme'));

/** Switches to the other theme with the theme button, as a player on this device would (a menu item in the compact layout). */
async function switchTheme(page, device) {
  const button = page.locator(COMPACT_ONLY.themePicker);
  if (device.hasTouch) {
    await reach(page, button);
    await tap(page, button);
  } else {
    if (await page.locator(`${COMPACT_ONLY.deckMenu}`).isVisible()) {
      await page.locator(COMPACT_ONLY.deckMenu).click();
      await page.waitForTimeout(300);
    }
    await button.click();
    await page.mouse.move(MOUSE_REST.x, MOUSE_REST.y);
  }
  await page.waitForTimeout(1000);
  await page.settleNow();
}

/** Turns the device or resizes the window. */
async function resize(page, viewport) {
  await page.setViewportSize(viewport);
  await page.waitForTimeout(800);
  await page.settleNow();
}

/** What a screenshot doesn't show of how the page is laid out. Runs in the page. */
function layoutFacts() {
  const collection = document.querySelector('.gd-page__collection')?.getBoundingClientRect();
  return {
    theme: document.documentElement.getAttribute('data-theme'),
    layout: document.querySelector('[data-layout]')?.getAttribute('data-layout') ?? 'computer',
    split: document.querySelector('[data-split]')?.getAttribute('data-split') ?? null,
    width: innerWidth,
    zoom: Math.round(visualViewport.scale * 1000) / 1000,
    pageWidth: document.documentElement.scrollWidth,
    collectionRight: collection ? Math.round(collection.right) : null,
    // Where every card is, and how large
    cards: [...document.querySelectorAll('.gd-card')]
      .map((card) => card.getBoundingClientRect())
      .filter((box) => box.width > 0)
      .map((box) => [box.x, box.y, box.width, box.height].map((n) => Math.round(n)).join(','))
      .join(' '),
  };
}

/**
 * Left out of the pixels (their boxes are compared): card art, which the browser draws a little differently once it has
 * scaled it for another zoom, and the theme button, which a tap leaves in its hover look. Runs in the page.
 */
function unsteadyAreas() {
  // In the screenshot's pixels: a page zoomed out (classic on a phone) is drawn smaller than it is laid out
  const { scale, offsetLeft, offsetTop } = visualViewport;
  const x = (n) => (n - offsetLeft) * scale;
  const y = (n) => (n - offsetTop) * scale;
  return [...document.querySelectorAll('img, .gd-toolbar__theme')]
    .map((element) => element.getBoundingClientRect())
    .filter((box) => box.width > 0)
    .map((box) => ({ left: x(box.left) - 2, top: y(box.top) - 2, right: x(box.right) + 2, bottom: y(box.bottom) + 2 }));
}

/**
 * The page must be as one freshly loaded in its theme at its size is: laid out the same (theme, layout, width, zoom) and
 * looking the same. The fresh page opens in the same browser (cookies, device), loaded with the theme's address.
 */
async function sameAsFresh(page, label) {
  const facts = await page.evaluate(layoutFacts);
  const viewport = page.viewportSize();
  const fresh = await page.context().newPage();
  try {
    await fresh.setViewportSize(viewport);
    const address = new URL(page.url());
    address.searchParams.set('theme', facts.theme);
    await fresh.goto(address.toString(), { waitUntil: 'networkidle' });
    // A narrow window on a computer has no collection, as the legacy page
    await fresh.waitForSelector(CONTROLS.firstCard.angular, { timeout: 5000 }).catch(() => undefined);
    await fresh.waitForTimeout(800);
    await fresh.mouse.move(MOUSE_REST.x, MOUSE_REST.y).catch(() => undefined);
    const expected = await fresh.evaluate(layoutFacts);
    for (const key of Object.keys(expected)) {
      expect(
        facts[key] === expected[key],
        `${label}: ${key} is ${facts[key]}, a freshly loaded page has ${expected[key]}`,
      );
    }
    expect(
      facts.collectionRight === null || facts.collectionRight <= Math.max(facts.width, facts.pageWidth),
      `${label}: the collection runs off the page (to ${facts.collectionRight}px of ${facts.pageWidth}px)`,
    );
    // The theme button just pressed keeps the focus (its ring), which a fresh page doesn't have; images finish drawing
    const ready = (target) =>
      target.evaluate(async () => {
        document.activeElement?.blur?.();
        await Promise.all([...document.images].map((image) => image.decode().catch(() => undefined)));
      });
    await ready(page);
    await ready(fresh);
    await page.waitForTimeout(300);
    const shot = await page.screenshot();
    const freshShot = await fresh.screenshot();
    const { outside } = compareScreens(shot, freshShot, await page.evaluate(unsteadyAreas), false);
    if (outside > SWITCH_TOLERANCE && page.switchShots) {
      const base = path.join(page.switchShots, `${page.scenarioName}-${label.replace(/[^\w-]+/g, '_')}`);
      fs.writeFileSync(`${base}-switched.png`, shot);
      fs.writeFileSync(`${base}-fresh.png`, freshShot);
    }
    expect(outside <= SWITCH_TOLERANCE, `${label}: ${outside} pixels differ from a freshly loaded page`);
  } finally {
    await fresh.close();
  }
}

/**
 * Switching themes on every device, turning the device or resizing the window before and after a switch: each time
 * the page must be as one freshly loaded in that theme at that size (sameAsFresh).
 */
function themeSwitchScenarios() {
  const scenarios = [];
  const other = (theme) => (theme === 'modern' ? 'classic' : 'modern');
  // [name, device, the steps: 'switch' or another device to turn or resize to]
  const plans = [];
  for (const name of Object.keys(DEVICES)) {
    plans.push([name, name, ['switch', 'switch']]);
  }
  plans.push(
    ['phone-turned-then-switched', 'phone', ['phoneSideways', 'switch', 'phone', 'switch']],
    ['phone-switched-then-turned', 'phone', ['switch', 'phoneSideways', 'switch', 'phone']],
    ['phone-sideways-switched-then-turned', 'phoneSideways', ['switch', 'phone', 'switch', 'phoneSideways']],
    ['tablet-turned-then-switched', 'tablet', ['tabletSideways', 'switch', 'tablet', 'switch']],
    ['tablet-switched-then-turned', 'tablet', ['switch', 'tabletSideways', 'switch', 'tablet']],
    ['window-narrowed-then-switched', 'computer', ['narrowWindow', 'switch', 'computer', 'switch']],
    ['window-switched-then-narrowed', 'computer', ['switch', 'narrowWindow', 'switch', 'computer']],
  );
  for (const start of ['modern', 'classic']) {
    for (const [name, deviceName, steps] of plans) {
      const device = DEVICES[deviceName];
      scenarios.push({
        name: `theme-switch--${name}--from-${start}`,
        note: `Starting in ${start} on a ${deviceName}: ${steps.join(', ')}; after each, the page is as freshly loaded`,
        device,
        startTheme: start,
        covers: [],
        run: async (page) => {
          let current = DEVICES[deviceName];
          expect((await themeOf(page)) === start, `the page opened in ${await themeOf(page)}, not ${start}`);
          await sameAsFresh(page, `opened in ${start} on a ${deviceName}`);
          for (const [i, step] of steps.entries()) {
            if (step === 'switch') {
              const before = await themeOf(page);
              await switchTheme(page, current);
              const after = await themeOf(page);
              expect(after === other(before), `step ${i + 1}: the theme button left ${after}, not ${other(before)}`);
              await sameAsFresh(page, `step ${i + 1}, switched to ${after}`);
            } else {
              current = DEVICES[step];
              await resize(page, current.viewport);
              await sameAsFresh(page, `step ${i + 1}, ${step} (${await themeOf(page)})`);
            }
            await pause(page, 100);
          }
        },
      });
    }
  }
  return scenarios;
}

/**
 * Scenarios of the compact layout alone (`angularOnly`): `run` throws, with what went wrong, when the page doesn't do
 * what it should. `covers`: the controls they test (for the coverage, 3). `size`: their own screen, if not the run's.
 */
export const COMPACT_SCENARIOS = [
  {
    name: 'compact-layout',
    note: 'The modern theme on a phone: the compact layout, the deck above the collection, the phone viewport',
    covers: [],
    run: async (page) => {
      const now = await state(page);
      expect(now.layout === 'compact', `the layout is ${now.layout}, not compact`);
      expect(now.split === 'rows', `the halves are ${now.split}, not one above the other (rows)`);
      expect(now.viewport?.includes('width=device-width'), `the viewport is ${now.viewport}`);
      expect(!(await page.locator(CONTROLS.toggleCollection.angular).isVisible()), 'the collection toggle is shown');
      expect(!(await page.locator(CONTROLS.autoZoom.angular).count()), 'the hover zoom is offered on a touch screen');
      const name = page.locator(CONTROLS.deckName.angular);
      expect(await name.isVisible(), "the deck's name is not shown");
    },
  },
  {
    name: 'compact-deck-menu',
    note: 'The deck menu: tapped open, closed by a tap beside it, by a button in it, and kept open by a long press',
    covers: ['deckMenu'],
    run: async (page) => {
      await tap(page, deckMenu(page));
      await pause(page, 500);
      expect((await state(page)).menus.join() === 'deck', 'tapping the deck menu did not open it');
      expect((await deckMenu(page).getAttribute('aria-expanded')) === 'true', 'the menu button says it is closed');
      const items = await page.locator('dialog.gd-drawer[data-drawer="deck"] button').allTextContents();
      expect(
        items[0]?.includes('theme') || items[0]?.includes('Theme'),
        `the first item is ${items[0]}, not the theme`,
      );
      await closeMenu(page);
      await pause(page, 300);
      expect((await state(page)).menus.length === 0, 'a tap beside the menu did not close it');
      await tap(page, deckMenu(page));
      await page.waitForTimeout(300);
      await longPress(page, page.locator(CONTROLS.notes.angular));
      await pause(page, 500);
      expect((await state(page)).menus.join() === 'deck', 'a long press on an item closed the menu');
      expect((await state(page)).dialogs.length === 0, 'a long press on Notes opened them');
      await tap(page, page.locator(CONTROLS.notes.angular));
      await pause(page, 800);
      const after = await state(page);
      expect(after.menus.length === 0, 'the menu stayed open after Notes was tapped');
      expect(after.dialogs.length === 1, `tapping Notes opened ${after.dialogs.join() || 'nothing'}`);
    },
  },
  {
    name: 'compact-collection-menu',
    note: 'The collection menu: opens, stays open while a list in it is used, counts the filters set in it',
    covers: ['collectionMenu'],
    run: async (page) => {
      const badge = page.locator(`${COMPACT_ONLY.collectionMenu} .gd-burger__badge`);
      expect(!(await badge.count()), 'the badge shows with no filter set');
      await reach(page, CONTROLS.rarity.angular);
      await pause(page, 300);
      expect((await state(page)).menus.join() === 'collection', 'tapping the collection menu did not open it');
      const rarity = page.locator(CONTROLS.rarity.angular);
      const choice = await rarity.locator('option').nth(1).getAttribute('value');
      await rarity.selectOption(choice);
      await pause(page, 800);
      expect((await state(page)).menus.join() === 'collection', 'choosing a rarity closed the menu');
      await closeMenu(page);
      await pause(page, 300);
      expect((await badge.textContent())?.trim() === '1', `the badge says ${await badge.textContent()}, not 1`);
      const label = await collectionMenu(page).getAttribute('aria-label');
      expect(label?.includes('1 filter set'), `the button is named ${label}`);
      await reach(page, CONTROLS.resetAll.angular);
      await tap(page, page.locator(CONTROLS.resetAll.angular));
      await pause(page, 800);
      expect(!(await badge.count()), 'the badge still shows after the filters were reset');
    },
  },
  {
    name: 'compact-filter-pins',
    note: 'Keeping a filter shown above the collection: it moves out of the menu, and stays so after a reload',
    covers: ['filterPin'],
    run: async (page) => {
      expect((await fieldPart(page, 'name')) === 'pane', 'the card name is not above the collection');
      expect((await fieldPart(page, 'rarity')) === 'menu', 'the rarity is not in the menu at first');
      const pin = page.locator(`${COMPACT_ONLY.filterPin}[data-pin="rarity"]`);
      await reach(page, pin);
      await tap(page, pin);
      await pause(page, 500);
      expect((await state(page)).menus.join() === 'collection', 'choosing a filter to show closed the menu');
      await closeMenu(page);
      expect((await fieldPart(page, 'rarity')) === 'pane', 'the pinned rarity is not above the collection');
      expect((await cookie(page, 'gempDeckbuilderFilters')) === 'rarity', 'the choice is not remembered');
      await reload(page);
      await pause(page, 300);
      expect((await fieldPart(page, 'rarity')) === 'pane', 'the pinned rarity is back in the menu after a reload');
      const again = page.locator(`${COMPACT_ONLY.filterPin}[data-pin="rarity"]`);
      await reach(page, again);
      await tap(page, again);
      await closeMenu(page);
      await pause(page, 500);
      expect((await fieldPart(page, 'rarity')) === 'menu', 'the rarity stayed above the collection when let go');
      const everyPin = await page.locator(COMPACT_ONLY.filterPin).count();
      expect(everyPin === 19, `${everyPin} filters can be kept shown, not 19 (all but the card name)`);
    },
  },
  {
    name: 'compact-theme-switch',
    note: 'The theme, first in the deck menu: switches to classic (no compact layout, no viewport) and back',
    covers: ['themePicker'],
    run: async (page) => {
      await reach(page, COMPACT_ONLY.themePicker);
      await tap(page, page.locator(COMPACT_ONLY.themePicker));
      await pause(page, 1000);
      let now = await state(page);
      expect(now.layout === null, `the classic theme is still ${now.layout}`);
      // A page loaded in classic has none; switched to, what having none gives (theme-switch scenarios compare it)
      expect(!now.viewport?.includes('device-width'), `the classic theme lays out at the device's width`);
      expect((await cookie(page, 'gempDeckbuilderTheme')) === 'classic', 'the theme is not remembered');
      // Classic, as the legacy page: the theme button in the toolbar, no menus
      expect(!(await deckMenu(page).count()), 'the classic theme has the deck menu');
      await tap(page, page.locator(COMPACT_ONLY.themePicker));
      await pause(page, 1000);
      now = await state(page);
      expect(now.layout === 'compact', `back to modern the layout is ${now.layout}`);
      expect((await cookie(page, 'gempDeckbuilderTheme')) === 'modern', 'the switch back is not remembered');
    },
  },
  {
    name: 'compact-split-drag',
    note: 'The split between the deck and the collection: dragged with a finger, within its limits, remembered',
    covers: ['separator'],
    run: async (page) => {
      expect((await state(page)).share === 50, `the split starts at ${(await state(page)).share}, not 50`);
      await dragSeparator(page, 0, -100);
      await pause(page, 300);
      const up = (await state(page)).share;
      expect(up < 50, `dragged up, the split is ${up}`);
      expect((await cookie(page, 'gempDeckbuilderSplit')) === String(up), 'the split is not remembered');
      await dragSeparator(page, 0, 2000);
      await pause(page, 300);
      expect((await state(page)).share === 85, `dragged far down, the split is ${(await state(page)).share}, not 85`);
      await dragSeparator(page, 0, -2000);
      await pause(page, 300);
      expect((await state(page)).share === 15, `dragged far up, the split is ${(await state(page)).share}, not 15`);
      await reload(page);
      expect((await state(page)).share === 15, `after a reload the split is ${(await state(page)).share}`);
      // A tablet's keyboard
      await separator(page).focus();
      await page.keyboard.press('ArrowDown');
      await pause(page, 300);
      expect((await state(page)).share === 20, `Down moved the split to ${(await state(page)).share}, not 20`);
    },
  },
  {
    name: 'compact-sideways',
    note: 'A phone held sideways: the deck left of the collection, the split dragged sideways; turned back upright',
    size: { width: 844, height: 390 },
    covers: ['separator'],
    run: async (page) => {
      let now = await state(page);
      expect(now.layout === 'compact' && now.split === 'columns', `sideways the layout is ${now.layout} ${now.split}`);
      await dragSeparator(page, -120, 0);
      await pause(page, 300);
      const left = (await state(page)).share;
      expect(left < 50, `dragged left, the split is ${left}`);
      await page.setViewportSize({ width: 390, height: 844 });
      await pause(page, 500);
      now = await state(page);
      expect(now.split === 'rows', `turned upright the halves are ${now.split}`);
      expect(now.share === left, `turning the phone changed the split to ${now.share}`);
    },
  },
  {
    name: 'compact-card-size',
    note: "The pager's slider sets the size of the collection's cards (remembered), not how many a page holds",
    covers: ['pageSize'],
    run: async (page) => {
      const slider = page.locator(CONTROLS.pageSize.angular);
      expect((await slider.getAttribute('aria-label')) === 'Card size', 'the slider is not the card size');
      const cardHeight = () =>
        page
          .locator('.gd-collection__cards > .gd-card')
          .first()
          .evaluate((el) => el.getBoundingClientRect().height);
      const before = await cardHeight();
      const cards = await page.locator('.gd-collection__cards > .gd-card').count();
      const asked = [];
      const listen = (request) => request.url().includes('/collection/') && asked.push(request.url());
      page.on('request', listen);
      await slide(page, { pageSize: CONTROLS.pageSize.angular }, 'angular', 'pageSize');
      await pause(page, 800);
      page.off('request', listen);
      const after = await cardHeight();
      const size = Number(await slider.inputValue());
      expect(size > 100, `moving the slider right left the size at ${size}%`);
      expect(Math.abs(after - (before * size) / 100) < 2, `cards ${before}px high became ${after}px at ${size}%`);
      expect(asked.length === 0, `the slider asked the server for cards: ${asked.join(', ')}`);
      expect((await page.locator('.gd-collection__cards > .gd-card').count()) === cards, 'the page holds other cards');
      expect((await cookie(page, 'gempDeckbuilderCardSize')) === String(size), 'the size is not remembered');
      await reload(page);
      await pause(page, 500);
      expect(Math.abs((await cardHeight()) - after) < 2, 'the size is not the same after a reload');
      expect(Number(await slider.inputValue()) === size, 'the slider is not where it was left after a reload');
      // The computer layout (a tablet held sideways): the slider sets the number of cards on a page again, at its size
      await page.setViewportSize({ width: 1180, height: 820 });
      await pause(page, 800);
      expect(
        (await slider.getAttribute('aria-label')) === 'Cards per page',
        'sideways the slider is not the page size',
      );
      expect((await cardHeight()) <= 200, 'the card size reaches the computer layout');
    },
  },
  {
    name: 'compact-tablet',
    note: 'A tablet: upright it uses the compact layout; sideways, the computer layout',
    size: { width: 820, height: 1180 },
    covers: [],
    run: async (page) => {
      expect((await state(page)).layout === 'compact', 'an upright tablet is not compact');
      await page.setViewportSize({ width: 1180, height: 820 });
      await pause(page, 500);
      const now = await state(page);
      expect(now.layout === null, `a tablet held sideways is ${now.layout}`);
      expect(await page.locator(CONTROLS.toggleCollection.angular).isVisible(), 'sideways the toggle is not shown');
    },
  },
  {
    name: 'compact-long-press-collection',
    note: 'A long press on a collection card shows its details, which stay open when the finger lifts',
    covers: [],
    run: async (page) => {
      await longPress(page, page.locator(CONTROLS.collectionCards.angular).first());
      await pause(page, 800);
      expect((await state(page)).dialogs.length === 1, 'a long press on a card showed no details');
      const close = page.locator(CONTROLS.cardInfoClose.angular);
      expect(await close.isVisible(), 'the card details have no close button');
      await tap(page, close);
      await pause(page, 500);
      expect((await state(page)).dialogs.length === 0, 'tapping close left the details open');
      const deck = await page.locator('.gd-page__deck .gd-card').count();
      expect(deck === 0, `the long press added a card to the deck (${deck} cards)`);
    },
  },
  {
    name: 'compact-long-press-deck',
    note: 'A long press on a card in the deck shows its details and keeps it in the deck',
    covers: [],
    run: async (page) => {
      await tap(page, page.locator(CONTROLS.collectionCards.angular).first());
      await pause(page, 800);
      const card = page.locator('.gd-page__deck .gd-card').first();
      expect(await card.count(), 'tapping a collection card did not add it to the deck');
      await reach(page, card);
      await longPress(page, card);
      await pause(page, 800);
      expect((await state(page)).dialogs.length === 1, 'a long press on a deck card showed no details');
      await tap(page, page.locator(CONTROLS.cardInfoClose.angular));
      await pause(page, 500);
      expect(
        (await page.locator('.gd-page__deck .gd-card').count()) > 0,
        'the long press took the card out of the deck',
      );
    },
  },
  {
    name: 'compact-sites',
    note: 'Tapping the empty sites strip shows the sites; a site tapped goes into the strip',
    covers: [],
    run: async (page, $) => {
      const sites = page.locator(CONTROLS.sitesSlot.angular);
      await reach(page, sites);
      await tap(page, sites);
      await pause(page, 1000);
      expect((await page.locator(CONTROLS.cardType.angular).inputValue()) === 'SITE', 'the sites are not shown');
      await reach(page, $.collectionCards);
      await tap(page, page.locator($.collectionCards).first());
      await pause(page, 800);
      expect((await page.locator(CONTROLS.siteCards.angular).count()) === 1, 'the site is not in the strip');
    },
  },
  {
    name: 'compact-deck-name-stays',
    note: "The deck's name stays in sight while the deck half scrolls",
    covers: [],
    run: async (page) => {
      await page.locator('.gd-page__deck').evaluate((deck) => deck.scrollBy(0, 400));
      await pause(page, 300);
      const name = await page.locator(CONTROLS.deckName.angular).boundingBox();
      const deck = await page.locator('.gd-page__deck').boundingBox();
      expect(name && name.y >= deck.y - 1 && name.y < deck.y + deck.height, "the deck's name scrolled away");
    },
  },
  ...themeSwitchScenarios(),
].map((scenario) => ({ ...scenario, angularOnly: true, mutating: false }));

// --- Coverage (3) ---

/** Every control the Angular page on a phone has, with the name controls.mjs or COMPACT_ONLY gives it. */
function namedOnPhone() {
  const groups = new Set([
    'collectionPane',
    'stats',
    'deckName',
    'dialogTitles',
    'firstCard',
    'collectionCards',
    'selectionPacks',
    'deckListItem',
  ]);
  return [
    ...Object.entries(CONTROLS)
      .filter(([name, control]) => typeof control !== 'function' && !groups.has(name))
      .map(([name, control]) => [name, control.angular]),
    ...Object.entries(COMPACT_ONLY),
  ];
}

/**
 * What is on the Angular page on a phone in each state, with no menu open and with each open, against what is tested.
 * `open(setup)` gives a phone page in that state. Returns one line per gap.
 */
export async function phoneCoverageGaps(open) {
  const gaps = [];
  const compared = new Set(
    phoneScenarios().map((scenario) => {
      const [state, control] = scenario.name.split('--');
      return `${state}/${control.startsWith('culture-') ? 'a culture' : control}`;
    }),
  );
  const everywhere = new Set(COMPACT_SCENARIOS.flatMap((scenario) => scenario.covers));
  const comparedSomewhere = new Set([...compared].map((key) => key.split('/')[1]));
  const tested = (state, control) =>
    everywhere.has(control) ||
    compared.has(`${state}/${control}`) ||
    (OUT_OF_STATE.has(`${state}/${control}`) && comparedSomewhere.has(control));
  for (const state of PHONE_STATES) {
    const opened = await open(STATES[state]).catch((error) => {
      gaps.push(`${state}: could not reach this state on the phone: ${error.message.split('\n')[0]}`);
      return null;
    });
    if (!opened) continue;
    const { page, close } = opened;
    const found = new Set();
    const list = async () => {
      for (const control of await page.evaluate(listControls, {
        interactive: INTERACTIVE.angular,
        named: namedOnPhone(),
        cardsOf: '.gd-card, .gd-card-info__card',
        cultureOf: '.gd-cultures',
      })) {
        found.add(control);
      }
    };
    await list();
    for (const menu of ['deck', 'collection']) {
      const burger = page.locator(`.gd-burger:has(+ ${DRAWER}[data-drawer="${menu}"])`);
      // A modal dialog over the page: its menus are tested with no dialog open
      if (!(await burger.isVisible()) || (await page.locator('dialog[open]').count())) continue;
      await tap(page, burger);
      await page.waitForTimeout(300);
      await list();
      await closeMenu(page);
    }
    await close();
    for (const control of found) {
      if (control.startsWith('unnamed')) {
        gaps.push(`${state}: the phone page has a control with no name in controls.mjs or phone.mjs: ${control}`);
      } else if (control !== 'a card' && !tested(state, control)) {
        gaps.push(`${state}: ${control} has no test on a phone`);
      }
    }
  }
  return gaps;
}
