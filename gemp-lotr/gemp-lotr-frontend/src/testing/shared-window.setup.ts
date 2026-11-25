/**
 * Test-only: run before every spec file (angular.json, the test target's `setupFiles`). The spec files share one
 * window (the test builder's `isolate: false`), so what one file leaves in it reaches the files after it, and the
 * order of the files follows the last run's results (failed first, then longest). Two things that made tests fail by
 * that order are kept out here (Ketura's item, 2026-11-20):
 *
 * - Animation frames. jsdom starts its frame loop with setInterval when a frame is asked for or the focused element
 *   goes away. Started under `vi.useFakeTimers()` the loop is on the fake clock; `vi.useRealTimers()` throws that
 *   clock away, jsdom still takes its loop for running, and no frame is ever called again in this window. A test that
 *   fakes only setTimeout then leaves Angular nothing to schedule change detection with, and `fixture.whenStable()`
 *   never comes back (game-page.component.spec.ts, after step-tab.component.spec.ts or a parity spec). So the
 *   window's frames run on the real clock here, apart from jsdom's loop; `vi.useFakeTimers()` still puts its own in
 *   their place while it is on.
 * - `document.cookie`. `vi.spyOn(document, 'cookie', 'get')` together with `...'set'` leaves the mocked getter on the
 *   document once both are restored: writes then go to the real cookies and reads come from the dead mock (the
 *   Angular game parity's animation-speed cookie, after theme.service.spec.ts). Whatever is left on the document is
 *   taken off after every test.
 */
const FRAME_MS = 1000 / 60;
const GUARDED = Symbol.for('gemp.shared-window.guarded');

function runFramesOnTheRealClock(): void {
  const later = globalThis.setTimeout;
  const cancel = globalThis.clearTimeout;
  const waiting = new Map<number, ReturnType<typeof setTimeout>>();
  let last = 0;
  window.requestAnimationFrame = (callback) => {
    const handle = ++last;
    waiting.set(
      handle,
      later(() => {
        waiting.delete(handle);
        callback(performance.now());
      }, FRAME_MS),
    );
    return handle;
  };
  window.cancelAnimationFrame = (handle) => {
    const timer = waiting.get(handle);
    if (timer === undefined) return;
    waiting.delete(handle);
    cancel(timer);
  };
}

// Once for the window, at the first spec file (real timers: none of them has run yet)
const marks = globalThis as { [GUARDED]?: true };
if (!marks[GUARDED]) {
  marks[GUARDED] = true;
  runFramesOnTheRealClock();
}

// Registered before the spec's own hooks, so it runs after them (hooks run as a stack)
afterEach(() => {
  if (Object.hasOwn(document, 'cookie')) delete (document as Partial<Pick<Document, 'cookie'>>).cookie;
});
