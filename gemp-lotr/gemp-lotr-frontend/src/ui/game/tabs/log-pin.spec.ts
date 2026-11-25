import { LogEdge, LogPin, PIN_TOLERANCE, REPIN_DELAY } from './log-pin';

/** A box jsdom lays out as told: `content` px of lines in a box `height` px high (0: hidden), scrolled to `scrollTop`. */
class FakeBox {
  content = 1000;
  height = 100;
  private top = 0;
  readonly element = document.createElement('div');

  constructor() {
    const element = this.element;
    Object.defineProperty(element, 'clientHeight', { get: () => this.height });
    Object.defineProperty(element, 'scrollHeight', { get: () => (this.height === 0 ? 0 : this.content) });
    Object.defineProperty(element, 'scrollTop', {
      get: () => (this.height === 0 ? 0 : this.top),
      set: (value: number) => {
        if (this.height > 0) this.top = Math.max(0, Math.min(value, this.content - this.height));
      },
    });
  }

  get max(): number {
    return this.content - this.height;
  }
}

describe('LogPin', () => {
  let box: FakeBox;
  let edge: LogEdge;
  let reported: (number | null)[];
  let pin: LogPin;

  beforeEach(() => {
    vi.useFakeTimers();
    box = new FakeBox();
    edge = 'end';
    reported = [];
    pin = new LogPin(
      box.element,
      () => edge,
      (place) => reported.push(place),
    );
  });

  afterEach(() => {
    pin.dispose();
    vi.useRealTimers();
  });

  /** Lines added: `px` more content, drawn. */
  const lines = (px: number, force = false) => {
    pin.drawing();
    box.content += px;
    pin.drawn(force);
  };
  const scrollTo = (top: number) => {
    box.element.scrollTop = top;
    pin.scrolled();
  };

  describe('oldest first (classic)', () => {
    it('starts at the newest lines and follows them as they come', () => {
      pin.restore(null);
      expect(box.element.scrollTop).toBe(box.max);
      vi.advanceTimersByTime(REPIN_DELAY);
      lines(50);
      expect(box.element.scrollTop).toBe(box.max);
      expect(pin.pinned).toBe(true);
    });

    it('counts as at the newest lines within the tolerance, and leaves a player who scrolled up where they are', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      scrollTo(box.max - PIN_TOLERANCE);
      expect(pin.pinned).toBe(true);
      expect(reported.at(-1)).toBeNull();
      scrollTo(box.max - PIN_TOLERANCE - 1);
      expect(pin.pinned).toBe(false);
      expect(reported.at(-1)).toBe(box.max - PIN_TOLERANCE - 1);
      const top = box.element.scrollTop;
      lines(200);
      expect(box.element.scrollTop).toBe(top);
      // Back at the newest lines: followed again
      scrollTo(box.max);
      lines(30);
      expect(box.element.scrollTop).toBe(box.max);
    });

    it('decides before the lines are drawn: lines that came after the player left the end are not followed', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      // The content grew without a scroll (a layout the page didn't draw): still pinned
      box.content += 500;
      lines(10);
      expect(box.element.scrollTop).toBe(box.max);
    });

    it('a warning pins the log again', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      scrollTo(100);
      lines(40, true);
      expect(pin.pinned).toBe(true);
      expect(box.element.scrollTop).toBe(box.max);
      expect(reported.at(-1)).toBeNull();
    });

    it('goes back to the newest lines a frame later, and after the timer when no frame comes', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      lines(10);
      // The scroll the page made, then the box grew under it (images, layout): not the player leaving
      box.content += 120;
      pin.scrolled();
      expect(pin.pinned).toBe(true);
      vi.advanceTimersByTime(REPIN_DELAY);
      expect(box.element.scrollTop).toBe(box.max);
    });

    it('takes a frame where there is one, once however many lines come', () => {
      const frames: FrameRequestCallback[] = [];
      const view = box.element.ownerDocument.defaultView!;
      const saved = view.requestAnimationFrame;
      view.requestAnimationFrame = (callback) => frames.push(callback);
      try {
        pin.restore(null);
        lines(10);
        lines(10);
        expect(frames).toHaveLength(1);
        box.content += 70;
        frames[0](0);
        expect(box.element.scrollTop).toBe(box.max);
        // The timer then does nothing more
        box.content += 70;
        vi.advanceTimersByTime(REPIN_DELAY);
        expect(box.element.scrollTop).toBe(box.max - 70);
      } finally {
        view.requestAnimationFrame = saved;
      }
    });

    it('repins after an image loads only while pinned', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      box.content += 80;
      pin.repin();
      expect(box.element.scrollTop).toBe(box.max);
      scrollTo(20);
      box.content += 80;
      pin.repin();
      expect(box.element.scrollTop).toBe(20);
    });

    it('when resized, a pinned log goes back to its newest lines', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      box.height = 60;
      pin.resized();
      expect(box.element.scrollTop).toBe(box.max);
    });

    it('hidden (its tab closed): nothing moves it, and shown again it is at the newest lines, or where the player left it', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      box.height = 0;
      pin.resized();
      pin.scrolled();
      lines(300);
      expect(pin.pinned).toBe(true);
      box.height = 100;
      pin.resized();
      expect(box.element.scrollTop).toBe(box.max);

      scrollTo(250);
      box.height = 0;
      pin.resized();
      pin.resized();
      lines(300);
      box.height = 100;
      pin.resized();
      expect(box.element.scrollTop).toBe(250);
      expect(pin.pinned).toBe(false);
    });

    it('takes up the place kept over a reload, also once its box is laid out', () => {
      pin.restore(120);
      expect(box.element.scrollTop).toBe(120);
      expect(pin.pinned).toBe(false);
      lines(50);
      expect(box.element.scrollTop).toBe(120);

      const hidden = new FakeBox();
      hidden.height = 0;
      const other = new LogPin(
        hidden.element,
        () => 'end',
        () => undefined,
      );
      other.restore(300);
      hidden.height = 100;
      other.resized();
      expect(hidden.element.scrollTop).toBe(300);
      other.dispose();
    });

    it("doesn't take the box's scrolling for the player's before the place kept over a reload is taken up", () => {
      box.element.scrollTop = 0;
      pin.scrolled();
      pin.resized();
      box.height = 0;
      pin.resized();
      expect(reported).toEqual([]);
      expect(pin.pinned).toBe(true);
    });
  });

  describe('newest first (modern)', () => {
    beforeEach(() => (edge = 'start'));

    it('stays at the top while pinned', () => {
      pin.restore(null);
      expect(box.element.scrollTop).toBe(0);
      vi.advanceTimersByTime(REPIN_DELAY);
      box.element.scrollTop = 3;
      lines(50);
      expect(box.element.scrollTop).toBe(0);
    });

    it('keeps what an unpinned player reads where it is as lines come above it, and reports it from the oldest lines', () => {
      pin.restore(null);
      vi.advanceTimersByTime(REPIN_DELAY);
      scrollTo(400);
      expect(pin.pinned).toBe(false);
      expect(reported.at(-1)).toBe(box.max - 400);
      lines(120);
      expect(box.element.scrollTop).toBe(520);
      // Shown again after lines came while hidden
      box.height = 0;
      pin.resized();
      lines(60);
      box.height = 100;
      pin.resized();
      expect(box.element.scrollTop).toBe(580);
    });

    it('takes up a place kept over a reload from the oldest lines', () => {
      box.content = 1200;
      pin.restore(300);
      expect(box.element.scrollTop).toBe(box.max - 300);
    });
  });
});
