import { Injectable } from '@angular/core';

/** A scheduled callback, for Timers.clear. */
export type TimerHandle = ReturnType<typeof setTimeout>;

/**
 * The browser's clock and timers, for core's loops (the hall's long-poll, the chat's updates). Specs drive them with
 * fake timers (vi.useFakeTimers), which replace both setTimeout and Date.now.
 */
@Injectable({ providedIn: 'root' })
export class Timers {
  /** Runs `callback` once after `delayMs`. */
  after(delayMs: number, callback: () => void): TimerHandle {
    return setTimeout(callback, delayMs);
  }

  clear(handle: TimerHandle | null): void {
    if (handle !== null) {
      clearTimeout(handle);
    }
  }

  /** Epoch milliseconds of our own clock. */
  now(): number {
    return Date.now();
  }
}
