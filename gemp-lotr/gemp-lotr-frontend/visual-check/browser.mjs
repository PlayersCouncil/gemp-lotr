// The browser every check runs in: Chromium, or the one CHECK_BROWSER names (chromium, firefox or webkit).
import { chromium, firefox, webkit } from 'playwright';

const BROWSERS = { chromium, firefox, webkit };

export const BROWSER_NAME = process.env.CHECK_BROWSER || 'chromium';

export function launchBrowser() {
  const type = BROWSERS[BROWSER_NAME];
  if (!type) {
    throw new Error(`Unknown CHECK_BROWSER ${BROWSER_NAME}: use chromium, firefox or webkit.`);
  }
  return type.launch();
}
