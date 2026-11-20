import { Timers } from '../../platform/timers';

/** GameHall.js GempHallTabs.REFRESH_AFTER_MS */
export const REFRESH_AFTER_MS = 10000;

/**
 * GempHallTabs.onReturn: a part that shows live data is refreshed when the viewer comes back to it after being away
 * for at least 10 s: back to its sub-tab or tab, or back to the browser tab or window while it is showing. A part
 * registered while hidden counts as left at that moment (its data was just fetched). The view reports what is shown
 * (`setShown`) and whether the page is in front of the viewer (`setPageShown`: not hidden, not blurred).
 */
export class ReturnRefresh {
  private readonly parts = new Map<string, { refresh: () => void; shown: boolean; leftAt: number | null }>();
  private readonly partShown = new Map<string, boolean>();
  private pageShown = true;

  constructor(private readonly timers: Timers) {}

  /** A part to refresh; it is shown if the view has said so already. */
  register(name: string, refresh: () => void): void {
    const visible = (this.partShown.get(name) ?? false) && this.pageShown;
    this.parts.set(name, { refresh, shown: visible, leftAt: visible ? null : this.timers.now() });
  }

  setShown(name: string, shown: boolean): void {
    this.partShown.set(name, shown);
    this.check();
  }

  setPageShown(shown: boolean): void {
    this.pageShown = shown;
    this.check();
  }

  /** checkReturns */
  private check(): void {
    const now = this.timers.now();
    for (const [name, part] of this.parts) {
      const shown = (this.partShown.get(name) ?? false) && this.pageShown;
      if (part.shown && !shown) {
        part.leftAt = now;
      } else if (!part.shown && shown && part.leftAt !== null && now - part.leftAt >= REFRESH_AFTER_MS) {
        part.refresh();
      }
      part.shown = shown;
    }
  }
}
