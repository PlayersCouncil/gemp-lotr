// Touch on a phone, for every check that uses a finger (the deck builder's phone check, the game's phone checks):
// Chromium's touch input through the DevTools protocol, for what Playwright's touchscreen can't do (hold, move).

/** A phone: a touch screen, a mobile browser (which reads the page's viewport). */
export const PHONE_CONTEXT = { hasTouch: true, isMobile: true };

/** The centre of an element on the page (px). */
export async function centre(locator) {
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
