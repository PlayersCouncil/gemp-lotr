// The browser every check runs in: Chromium, or the one CHECK_BROWSER names (chromium, firefox or webkit).
// CHECK_BROWSER_EXECUTABLE runs a browser already on the machine instead of Playwright's own build (where that build
// can't be downloaded); unset, Playwright picks its own as always.
import { chromium, firefox, webkit } from 'playwright';

const BROWSERS = { chromium, firefox, webkit };

export const BROWSER_NAME = process.env.CHECK_BROWSER || 'chromium';

export function launchBrowser() {
  const type = BROWSERS[BROWSER_NAME];
  if (!type) {
    throw new Error(`Unknown CHECK_BROWSER ${BROWSER_NAME}: use chromium, firefox or webkit.`);
  }
  const executablePath = process.env.CHECK_BROWSER_EXECUTABLE;
  return type.launch(executablePath ? { executablePath } : {});
}
