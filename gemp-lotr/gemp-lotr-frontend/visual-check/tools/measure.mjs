#!/usr/bin/env node
// Where things are on the legacy page and the Angular page, and in which font: for finding why they differ, e.g. in
// another browser. Both pages run against the stub server (stub-server.mjs), with no login.
//
//   npm run measure -- [--browser firefox] [--size 1366x768] [--build .local-server/ng/browser] [--click <control>]...
//                      [--theme classic|modern] [--screenshot <dir>] [--scheme dark|light] <selector>...
//
// --build: the Angular build to serve (default: the server's web root, ng/); .local-server/ng/browser is the one of the
// last check:local. --click: a control of controls.mjs (e.g. deckList) clicked on each page first, key:<key> pressed, or
// drag:<control>:<dx> dragged sideways as pane-resized does (from 3px into it, 100px down, in 10 steps), or
// select:<control>:<value> chosen in a list, css:<selector> clicked (on a page that has it) or rightclick:<selector>
// (a card's details, as a long press; also only on a page that has it) or hold:<selector> (a long press: Chromium with
// --touch), in order.
// --theme: the Angular page's (default classic, the legacy look). --screenshot: also saves both pages, after the clicks,
// as <dir>/legacy-<size>.png and angular-<theme>-<size>.png. --scheme: the colour scheme the device prefers.
// --touch: a phone (touch screen, mobile viewport): the steps tap instead of clicking. --requests <text>: prints the
// requests to the stub whose address has that text (e.g. collection), after the steps.
// Each selector is looked up on both pages, so a selector of one page alone shows only there.
import { parseArgs } from 'node:util';
import { chromium, firefox, webkit } from 'playwright';

const { values: options, positionals: selectors } = parseArgs({
  allowPositionals: true,
  options: {
    browser: { type: 'string', default: process.env.CHECK_BROWSER || 'chromium' },
    size: { type: 'string', default: '1366x768' },
    build: { type: 'string' },
    click: { type: 'string', multiple: true, default: [] },
    theme: { type: 'string', default: 'classic' },
    screenshot: { type: 'string' },
    scheme: { type: 'string', default: 'dark' },
    touch: { type: 'boolean', default: false },
    requests: { type: 'string' },
  },
});
if (options.build) process.env.CHECK_NG_BUILD = options.build;
const { routeToStub, STUB_ORIGIN } = await import('../stub-server.mjs');
const { CONTROLS } = await import('../controls.mjs');

const type = { chromium, firefox, webkit }[options.browser];
if (!type || (!selectors.length && !options.screenshot)) {
  console.error(
    'Usage: npm run measure -- [--browser chromium|firefox|webkit] [--size WxH] [--build dir] [--theme name] [--screenshot dir] [--scheme dark|light] <selector>...',
  );
  process.exit(2);
}
const [width, height] = options.size.split('x').map(Number);
const PAGES = { legacy: '/gemp-lotr/deckBuild.html', angular: `/gemp-lotr/ng/deckbuilder/?theme=${options.theme}` };

/** Runs in the page: each match's box (rounded to 0.01px), font, padding and border. */
function measure(selector) {
  return [...document.querySelectorAll(selector)].slice(0, 20).map((element) => {
    const box = element.getBoundingClientRect();
    const style = getComputedStyle(element);
    const r = (n) => Math.round(n * 100) / 100;
    const text = (element.textContent ?? '').trim().replace(/\s+/g, ' ').slice(0, 30);
    const edges = `padding ${style.padding} border ${style.borderWidth} ${style.boxSizing} color ${style.color} bg ${style.backgroundColor}`;
    return `x ${r(box.x)} y ${r(box.y)} w ${r(box.width)} h ${r(box.height)}  ${style.fontFamily} ${style.fontSize}/${style.lineHeight}  ${edges}  ${text}`;
  });
}

const browser = await type.launch();
for (const [which, pagePath] of Object.entries(PAGES)) {
  const context = await browser.newContext({
    viewport: { width, height },
    colorScheme: options.scheme,
    ...(options.touch ? { hasTouch: true, isMobile: options.browser !== 'firefox' } : {}),
  });
  const requests = [];
  context.on('request', (request) => {
    if (options.requests && request.url().includes(options.requests)) requests.push(decodeURIComponent(request.url()));
  });
  const log = [];
  await routeToStub(context, log);
  const page = await context.newPage();
  await page.goto(STUB_ORIGIN + pagePath, { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  for (const name of options.click) {
    if (name.startsWith('key:')) {
      await page.keyboard.press(name.slice(4));
    } else if (name.startsWith('hold:')) {
      // A finger held still on it for 700ms (Chromium's touch input), on a page that has it
      const target = page.locator(name.slice(5)).first();
      if (await target.count()) {
        const box = await target.boundingBox();
        const point = { x: box.x + box.width / 2, y: box.y + box.height / 2 };
        const cdp = await context.newCDPSession(page);
        await cdp.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [point] });
        await page.waitForTimeout(700);
        await cdp.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
      }
    } else if (name.startsWith('rightclick:')) {
      // Asks for a card's details, as a long press on a touch screen does
      const target = page.locator(name.slice(11)).first();
      if (await target.count()) await target.click({ button: 'right' });
    } else if (name.startsWith('css:')) {
      const target = page.locator(name.slice(4)).first();
      if (await target.count()) await (options.touch ? target.tap() : target.click());
    } else if (name.startsWith('select:')) {
      const [, control, value] = name.split(':');
      const list = page.locator(CONTROLS[control][which]).first();
      if (await list.isVisible()) await list.selectOption(value);
      else console.log(`(${which}: ${control} is not shown, not chosen)`);
    } else if (name.startsWith('drag:')) {
      const [, control, dx] = name.split(':');
      const box = await page.locator(CONTROLS[control][which]).first().boundingBox();
      await page.mouse.move(box.x + 3, box.y + 100);
      await page.mouse.down();
      await page.mouse.move(box.x + 3 + Number(dx), box.y + 100, { steps: 10 });
      await page.mouse.up();
    } else {
      const selector = CONTROLS[name]?.[which];
      if (!selector) throw new Error(`No control ${name} in controls.mjs`);
      // A gesture of the check (matrix.mjs) presses where the control is, whatever is over it
      const box = await page.locator(selector).first().boundingBox();
      const [x, y] = [box.x + box.width / 2, box.y + box.height / 2];
      await (options.touch ? page.touchscreen.tap(x, y) : page.mouse.click(x, y));
    }
    await page.waitForTimeout(800);
  }
  if (options.screenshot) {
    const name = `${which}${which === 'angular' ? `-${options.theme}` : ''}-${options.size}.png`;
    await page.screenshot({ path: `${options.screenshot}/${name}` });
  }
  console.log(`=== ${which} (${options.browser} ${options.size})`);
  for (const request of requests) console.log(`  request ${request}`);
  for (const selector of selectors) {
    const found = await page.evaluate(measure, selector);
    if (found.length) console.log(`${selector}\n  ${found.join('\n  ')}`);
  }
  await context.close();
}
await browser.close();
