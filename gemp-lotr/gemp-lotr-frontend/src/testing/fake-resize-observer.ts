/**
 * Test-only: a ResizeObserver for jsdom, which has none. While installed, every observer a component makes is kept,
 * and `resize()` calls each one's callback as a browser would after a size change (sizes the spec gives elements by
 * hand). `restore()` puts back what the window had.
 */
export interface FakeResizeObservers {
  /** Calls every observer's callback. */
  resize(): void;
  restore(): void;
}

export function installFakeResizeObserver(): FakeResizeObservers {
  const callbacks: (() => void)[] = [];
  const real = window.ResizeObserver;
  class FakeResizeObserver {
    private connected = true;
    constructor(private readonly callback: () => void) {
      callbacks.push(() => {
        if (this.connected) this.callback();
      });
    }
    observe(): void {
      this.connected = true;
    }
    unobserve(): void {
      // every element is watched the same way: nothing to forget
      this.connected = true;
    }
    disconnect(): void {
      this.connected = false;
    }
  }
  window.ResizeObserver = FakeResizeObserver as unknown as typeof ResizeObserver;
  return {
    resize: () => callbacks.forEach((callback) => callback()),
    restore: () => {
      window.ResizeObserver = real;
    },
  };
}
