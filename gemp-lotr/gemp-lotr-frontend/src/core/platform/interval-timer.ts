import { Injectable } from '@angular/core';

/** Stops a repeating call started by IntervalTimer.every. Calling it again does nothing. */
export type StopTimer = () => void;

/**
 * Repeating calls (the legacy pages' setInterval polling), so that core services start and stop them in one place
 * and tests drive them with fake timers.
 */
@Injectable({ providedIn: 'root' })
export class IntervalTimer {
  /** Calls `tick` every `ms` milliseconds (first call after `ms`) until the returned function is called. */
  every(ms: number, tick: () => void): StopTimer {
    let handle: ReturnType<typeof setInterval> | null = setInterval(tick, ms);
    return () => {
      if (handle !== null) {
        clearInterval(handle);
        handle = null;
      }
    };
  }
}
