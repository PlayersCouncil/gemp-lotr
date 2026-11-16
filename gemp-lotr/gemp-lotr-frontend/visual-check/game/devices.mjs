// The classic theme on every kind of device (the owner, 2026-11-16: players who keep the legacy look must get exactly
// what game.html gives them, also on a phone or a tablet): the legacy game.html and the Angular game page in the
// classic theme, side by side on a computer, a tablet and a phone, each upright and sideways, on the recorded games
// served without a server (stub-game.mjs).
//
//   node game/devices.mjs [--devices pc,tablet,tablet-sideways,phone,phone-sideways] [--only <recording,...>]
//                         [--checks screens,zoom,switch] [--tolerance 60] [--out report-game-devices]
//
// The checks, each on every device:
// - screens: each recording's last state on both pages: the pixels (clocks masked), the game as shown (readGameState)
//   and what the browser makes of the page (its layout width, its zoom, the viewport tag).
// - zoom: whether a finger may zoom the page, and what zooming does. A headless browser doesn't zoom for two fingers,
//   so what decides it is compared instead: the viewport tag (zoom allowed, how far) and the touch rules
//   (touch-action) in effect under a grid of points over the screen. Then a touchpad's pinch, a pan and the browser's
//   bars sliding away (the window gets taller): both pages must end zoomed and moved alike, the zoom kept.
// - switch: the same on the Angular page switched from the modern theme to classic with the red button, without a
//   reload, against game.html.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { launchBrowser } from '../browser.mjs';
import { compareScreens } from '../compare.mjs';
import { routeToStub, STUB_ORIGIN } from '../stub-server.mjs';
import { ANGULAR_CONTRACT, LEGACY, readGameState } from './controls.mjs';
import { DEVICES } from './device-profiles.mjs';
import { asOneState, GAME_ID, inPage, routeGame, settled } from './stub-game.mjs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const RECORDINGS = path.resolve(HERE, '../../src/testing/game-recordings');

const { values: options } = parseArgs({
  options: {
    devices: { type: 'string', default: 'pc,tablet,tablet-sideways,phone,phone-sideways' },
    only: { type: 'string' },
    checks: { type: 'string', default: 'screens,zoom,switch' },
    tolerance: { type: 'string', default: '60' },
    out: { type: 'string', default: 'report-game-devices' },
    // Prints what the expression gives on both pages (each recording's last state) instead of checking
    eval: { type: 'string' },
    // CSS put into the Angular page before it is compared (to find what makes a difference)
    style: { type: 'string' },
  },
});

export { DEVICES };

const OUT = path.resolve(HERE, '..', options.out);
const TOLERANCE = Number(options.tolerance);

const recordings = () =>
  fs
    .readdirSync(RECORDINGS)
    .filter((file) => file.endsWith('.json'))
    .map((file) => ({ name: file.replace(/\.json$/, ''), ...JSON.parse(fs.readFileSync(path.join(RECORDINGS, file))) }))
    .filter((recording) => !options.only || options.only.split(',').includes(recording.name));

/** One page of the game as it stood at the recording's end, on a device; `theme` for the Angular page's cookie. */
async function open(browser, kind, device, recording, theme = 'classic') {
  const context = await browser.newContext(DEVICES[device]);
  const log = [];
  const errors = [];
  await routeToStub(context, log);
  const waiting = await routeGame(context, recording, recording.exchanges.length, log);
  await context.addCookies(
    [
      ['animation-speed', '4'],
      ['gempGameTheme', theme],
    ].map(([name, value]) => ({ name, value, domain: new URL(STUB_ORIGIN).hostname, path: '/gemp-lotr' })),
  );
  const page = await context.newPage();
  page.on('pageerror', (error) => errors.push(String(error)));
  await page.goto(
    `${STUB_ORIGIN}${kind === 'legacy' ? '/gemp-lotr/game.html' : '/gemp-lotr/ng/game'}?gameId=${GAME_ID}`,
  );
  await settled(page, waiting);
  if (kind === 'angular' && options.style) {
    await page.addStyleTag({ content: options.style });
    await page.waitForTimeout(300);
  }
  return {
    page,
    context,
    errors,
    select: kind === 'legacy' ? LEGACY : ANGULAR_CONTRACT,
    kind,
    /** From now on the page opening the game gets all of it in one answer, as from the server (for a reload). */
    async servingWhole() {
      await context.unroute(`${STUB_ORIGIN}/gemp-lotr-server/**`);
      const whole = asOneState(recording, recording.exchanges.length);
      await routeGame(context, whole, whole.exchanges.length, log);
    },
  };
}

/** What the browser makes of the page: its layout's width, its zoom, where it is moved to, the viewport tag. */
function readViewport() {
  const v = window.visualViewport;
  const round = (n) => Math.round(n * 1000) / 1000;
  return {
    layoutWidth: document.documentElement.clientWidth,
    layoutHeight: document.documentElement.clientHeight,
    scale: round(v?.scale ?? 1),
    left: Math.round(v?.offsetLeft ?? 0),
    top: Math.round(v?.offsetTop ?? 0),
    tag: document.querySelector('meta[name="viewport"]')?.getAttribute('content') ?? null,
  };
}

async function screen(shown) {
  return shown.page.screenshot({
    mask: shown.select.masks.map((selector) => shown.page.locator(selector)),
    maskColor: '#ff00ff',
    style: shown.select.screenshotStyle,
  });
}

/** Chromium's touch input: a pinch (two fingers spreading) and a one-finger pan. */
async function cdp(page, method, params) {
  page.__cdp ??= await page.context().newCDPSession(page);
  return page.__cdp.send(method, params);
}

/**
 * The touch rules in effect under a grid of 6×8 points over the screen: for each, whether a finger may pan and zoom
 * there (the strictest touch-action of the element under it and the elements around it).
 */
function readTouchRules({ width, height }) {
  const rules = [];
  for (let row = 0; row < 8; row++) {
    for (let column = 0; column < 6; column++) {
      const x = ((column + 0.5) * width) / 6;
      const y = ((row + 0.5) * height) / 8;
      let element = document.elementFromPoint(x, y);
      const actions = new Set();
      while (element) {
        const action = getComputedStyle(element).touchAction;
        if (action && action !== 'auto') actions.add(action);
        element = element.parentElement;
      }
      const blocks = (what) =>
        [...actions].some((a) => a === 'none' || (a !== 'manipulation' && !a.split(' ').includes(what)));
      rules.push(`${blocks('pinch-zoom') ? 'no-zoom' : 'zoom'}/${actions.size ? 'restricted' : 'free'}`);
    }
  }
  return rules;
}

/** A touchpad's pinch and a pan, and then the browser's bars sliding away (the window 60px taller). */
async function zoomAndPan(page, device) {
  const { width, height } = DEVICES[device].viewport;
  const touchRules = await page.evaluate(readTouchRules, { width, height });
  const start = await page.evaluate(readViewport);
  await cdp(page, 'Input.synthesizePinchGesture', {
    x: Math.round(width / 2),
    y: Math.round(height / 2),
    scaleFactor: 2.5,
    gestureSourceType: 'mouse',
  });
  await page.waitForTimeout(400);
  const zoomed = await page.evaluate(readViewport);
  // A pan: the wheel over the zoomed page moves what it shows
  await page.mouse.move(Math.round(width / 2), Math.round(height / 2));
  await page.mouse.wheel(80, 120);
  await page.waitForTimeout(400);
  const panned = await page.evaluate(readViewport);
  await page.setViewportSize({ width, height: height + 60 });
  await page.waitForTimeout(500);
  const afterBars = await page.evaluate(readViewport);
  return { touchRules, start, zoomed, panned, afterBars };
}

const touch = (device) => Boolean(DEVICES[device].hasTouch);

function differ(a, b) {
  return Object.keys({ ...a, ...b })
    .filter((key) => JSON.stringify(a[key]) !== JSON.stringify(b[key]))
    .map((key) => `${key}: ${JSON.stringify(a[key])} vs ${JSON.stringify(b[key])}`);
}

function save(name, legacyScreen, angularScreen) {
  fs.mkdirSync(OUT, { recursive: true });
  fs.writeFileSync(path.join(OUT, `${name}-legacy.png`), legacyScreen);
  fs.writeFileSync(path.join(OUT, `${name}-angular.png`), angularScreen);
  const { image } = compareScreens(legacyScreen, angularScreen, [], true);
  if (image) fs.writeFileSync(path.join(OUT, `${name}-diff.png`), image);
}

/** Each recording's last state on both pages: pixels, the game as shown, the browser's view of the page. */
async function screensCheck(browser, devices) {
  let same = 0;
  let total = 0;
  for (const device of devices) {
    for (const recording of recordings()) {
      total++;
      const [legacy, angular] = [
        await open(browser, 'legacy', device, recording),
        await open(browser, 'angular', device, recording),
      ];
      try {
        const problems = [];
        const [a, b] = [await screen(legacy), await screen(angular)];
        const { outside } = compareScreens(a, b, [], false);
        if (outside > TOLERANCE) problems.push(`${outside} pixels differ`);
        const states = await Promise.all([legacy, angular].map((s) => inPage(s.page, readGameState, { kind: s.kind })));
        if (JSON.stringify(states[0]) !== JSON.stringify(states[1])) problems.push('the game as shown differs');
        const views = await Promise.all([legacy, angular].map((s) => s.page.evaluate(readViewport)));
        problems.push(...differ(views[0], views[1]).filter((line) => !line.startsWith('tag:')));
        problems.push(...legacy.errors.map((e) => `legacy: ${e}`), ...angular.errors.map((e) => `angular: ${e}`));
        if (problems.length === 0) same++;
        else {
          console.log(`  ✗ screens · ${device} · ${recording.name}: ${problems.join('; ')}`);
          save(`screens-${device}-${recording.name}`, a, b);
        }
      } finally {
        await legacy.context.close();
        await angular.context.close();
      }
    }
  }
  console.log(`screens: ${same} of ${total} the same`);
  return same === total;
}

/** A pinch, a pan and the bars sliding away, on both pages (touch devices); `theme`: the switch check's way in. */
async function zoomCheck(browser, devices, viaSwitch) {
  const name = viaSwitch ? 'switch' : 'zoom';
  let same = 0;
  let total = 0;
  const recording = recordings().find((r) => r.name === 'fellowship-play') ?? recordings()[0];
  for (const device of devices.filter(touch)) {
    total++;
    const legacy = await open(browser, 'legacy', device, recording);
    const angular = await open(browser, 'angular', device, recording, viaSwitch ? 'modern' : 'classic');
    try {
      if (viaSwitch) {
        // The red button: classic is remembered and the page loaded again
        await angular.servingWhole();
        await Promise.all([
          angular.page.waitForEvent('load'),
          angular.page.locator('[data-icon="theme"]').first().tap(),
        ]);
        await settled(angular.page);
        const theme = await angular.page.evaluate(() => document.documentElement.dataset.theme);
        if (theme !== 'classic') throw new Error(`the red button left the page on ${theme}`);
      }
      const [a, b] = [await zoomAndPan(legacy.page, device), await zoomAndPan(angular.page, device)];
      const problems = [];
      const rulesDiffer = a.touchRules.flatMap((rule, i) =>
        rule === b.touchRules[i] ? [] : [`#${i} ${rule} vs ${b.touchRules[i]}`],
      );
      if (rulesDiffer.length) problems.push(`touch rules differ at ${rulesDiffer.slice(0, 4).join(', ')}`);
      for (const moment of ['start', 'zoomed', 'panned', 'afterBars']) {
        problems.push(...differ(a[moment], b[moment]).map((line) => `${moment} ${line}`));
      }
      if (!(a.zoomed.scale > a.panned.scale - 0.01 && a.zoomed.scale > 1.2 * (await initialScale(device)))) {
        problems.push(`game.html itself didn't zoom (${a.zoomed.scale})`);
      }
      if (problems.length === 0) same++;
      else {
        console.log(`  ✗ ${name} · ${device}: ${problems.join('; ')}`);
        save(`${name}-${device}`, await screen(legacy), await screen(angular));
      }
    } finally {
      await legacy.context.close();
      await angular.context.close();
    }
  }
  console.log(`${name}: ${same} of ${total} zoom and move as game.html`);
  return same === total;
}

/** The zoom a page without a viewport tag starts at on the device (its screen in a 980px wide layout). */
async function initialScale(device) {
  const { width } = DEVICES[device].viewport;
  return Math.min(1, width / 980);
}

const browser = await launchBrowser();
try {
  const devices = options.devices.split(',');
  if (options.eval) {
    for (const device of devices) {
      for (const recording of recordings()) {
        for (const kind of ['legacy', 'angular']) {
          const shown = await open(browser, kind, device, recording);
          const value = await shown.page.evaluate(options.eval).catch((error) => `failed: ${error.message}`);
          console.log(`${device} ${recording.name} ${kind}: ${JSON.stringify(value)}`);
          await shown.context.close();
        }
      }
    }
    process.exit(0);
  }
  const chosen = options.checks.split(',');
  let passed = true;
  if (chosen.includes('screens')) passed = (await screensCheck(browser, devices)) && passed;
  if (chosen.includes('zoom')) passed = (await zoomCheck(browser, devices, false)) && passed;
  if (chosen.includes('switch')) passed = (await zoomCheck(browser, devices, true)) && passed;
  process.exitCode = passed ? 0 : 1;
} finally {
  await browser.close();
}
