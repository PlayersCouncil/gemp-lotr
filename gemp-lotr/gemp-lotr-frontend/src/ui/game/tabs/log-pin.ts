/** Where a log's newest lines are: at its end (classic, oldest first) or at its start (modern, newest first). */
export type LogEdge = 'end' | 'start';

/** How far from the newest lines still counts as at them, in px (fractional scroll positions, borders): legacy chat.js. */
export const PIN_TOLERANCE = 5;

/** How long a repin waits at most for a frame (a page in a background tab gets none): legacy chat.js. */
export const REPIN_DELAY = 250;

/**
 * Keeps a log at its newest lines while the player reads them, and leaves a player who scrolled away to read where
 * they are (legacy chat.js `pinnedToBottom`, `repinIfPinned`, `scheduleRepin`; gameUi.js `tabsshow`, as of
 * origin/feature/tutorials).
 *
 * The log is "pinned" while the player is within {@link PIN_TOLERANCE} px of its newest lines; only the player's own
 * scrolling changes that ({@link scrolled}), so whether new lines are followed is decided before they are drawn, never
 * from where the box stands after they came. A pinned log goes back to its newest lines after lines are drawn
 * ({@link drawn}, and again a frame later), after an image in it loads, and when the box changes size
 * ({@link resized}): the window or the sheet resized, or its tab shown again (a box that was hidden has forgotten its
 * scroll position). An unpinned one gets back the place it had when it was hidden.
 *
 * An unpinned log's place is how far it is from its oldest lines (px): new lines don't move that, at either end, so
 * with the newest lines at the start what the player reads stays where it is as lines come above it (the browser's
 * scroll anchoring can't be relied on: the chat box redraws its lines in place, by index), and a place kept over a
 * reload or a closed tab still holds when lines came meanwhile. It is reported (`report`): null while pinned, so a reload can put it back ({@link restore},
 * D10).
 */
export class LogPin {
  /** Whether the log follows its newest lines. */
  pinned = true;
  private scheduled = false;
  /** Restored (or found nothing to restore): until then the box's scrolling isn't the player's. */
  private ready = false;
  /** The place to go back to once the box is laid out again (from its oldest lines). */
  private kept: number | null = null;
  /** Where it was when last laid out (from its oldest lines). */
  private last = 0;
  /** Where it stood before lines were drawn (from its oldest lines). */
  private before: number | null = null;
  private timer: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly box: HTMLElement,
    private readonly edge: () => LogEdge,
    private readonly report: (scroll: number | null) => void,
  ) {}

  /** The box has no height while it is hidden (another tab, a closed sheet) or before its first layout. */
  private laidOut(): boolean {
    return this.box.clientHeight > 0;
  }

  private atNewest(): boolean {
    const box = this.box;
    return this.edge() === 'start'
      ? box.scrollTop <= PIN_TOLERANCE
      : box.scrollHeight - box.scrollTop - box.clientHeight <= PIN_TOLERANCE;
  }

  private toNewest(): void {
    this.box.scrollTop = this.edge() === 'start' ? 0 : this.box.scrollHeight;
    this.remember();
  }

  /** How far the box is from its oldest lines (px). */
  private place(): number {
    const box = this.box;
    return this.edge() === 'start' ? box.scrollHeight - box.clientHeight - box.scrollTop : box.scrollTop;
  }

  private goTo(place: number): void {
    const box = this.box;
    box.scrollTop = this.edge() === 'start' ? box.scrollHeight - box.clientHeight - place : place;
    this.remember();
  }

  private remember(): void {
    if (this.laidOut()) this.last = this.place();
  }

  /** The player scrolled (or the page did): pinned again at the newest lines, unpinned away from them. */
  scrolled(): void {
    // A scroll the page made a moment ago, with the box still growing under it, is not the player leaving
    if (!this.ready || this.scheduled || !this.laidOut()) return;
    this.pinned = this.atNewest();
    this.remember();
    this.report(this.pinned ? null : this.place());
  }

  /** Lines are about to be drawn. */
  drawing(): void {
    this.before = this.laidOut() ? this.place() : null;
  }

  /** Lines were drawn; `force` (a warning among the new ones) pins the log. */
  drawn(force: boolean): void {
    const before = this.before;
    this.before = null;
    if (force && !this.pinned) {
      this.pinned = true;
      this.report(null);
    }
    if (!this.laidOut()) return;
    if (this.pinned) {
      this.toNewest();
      // The box can still grow after this: layout not done yet, images loading in
      this.schedule();
    } else if (before !== null) {
      // Lines came above (newest first): what the player reads stays where it is
      this.goTo(before);
    }
  }

  /** Back to the newest lines, unless the player has scrolled away from them. */
  repin(): void {
    if (this.pinned && this.laidOut() && !this.atNewest()) this.toNewest();
  }

  /** Once a frame at most, however many lines come. */
  private schedule(): void {
    if (this.scheduled) return;
    this.scheduled = true;
    const view = this.box.ownerDocument.defaultView;
    const repin = () => {
      if (!this.scheduled) return;
      this.scheduled = false;
      if (this.timer !== null) clearTimeout(this.timer);
      this.timer = null;
      this.repin();
    };
    // (a document without a window, or a test's, may have no frames at all)
    if (view && 'requestAnimationFrame' in view) view.requestAnimationFrame(repin);
    // The timer is for a page that isn't drawn (a background tab), where frames don't come
    this.timer = setTimeout(repin, REPIN_DELAY);
  }

  /** The box changed size: resized, hidden, or shown again. */
  resized(): void {
    if (!this.laidOut()) {
      if (this.ready && !this.pinned && this.kept === null) this.kept = this.last;
      return;
    }
    if (this.pinned) {
      this.repin();
    } else if (this.kept !== null) {
      const kept = this.kept;
      this.kept = null;
      this.goTo(kept);
    }
  }

  /**
   * The place kept over a reload (from the oldest lines; null: at the newest lines); the box's scrolling is the
   * player's from now on.
   */
  restore(saved: number | null): void {
    this.ready = true;
    if (saved === null) {
      this.pinned = true;
      this.drawn(false);
      return;
    }
    this.pinned = false;
    if (this.laidOut()) this.goTo(saved);
    else this.kept = saved;
  }

  /** The box is gone. */
  dispose(): void {
    this.scheduled = false;
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
  }
}
