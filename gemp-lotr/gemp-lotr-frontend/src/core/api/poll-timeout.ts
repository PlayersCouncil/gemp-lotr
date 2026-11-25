import { Observable } from 'rxjs';
import { Timers } from '../platform/timers';
import { ApiError } from './api-error';

/** How long the legacy client waited for a long-poll answer (communication.js: getHall, updateHall, updateChat). */
export const POLL_TIMEOUT_MS = 20_000;

/** A long-poll got no answer in time (jQuery's "timeout"): status 0, like a lost connection. */
export class PollTimeoutError extends ApiError {
  constructor() {
    super(0, 'The Gemp server did not answer in time');
    this.name = 'PollTimeoutError';
  }
}

/** Fails `source` with PollTimeoutError (and drops the request) when it has not answered within `timeoutMs`. */
export function withPollTimeout<T>(source: Observable<T>, timers: Timers, timeoutMs = POLL_TIMEOUT_MS): Observable<T> {
  return new Observable<T>((subscriber) => {
    const timer = timers.after(timeoutMs, () => subscriber.error(new PollTimeoutError()));
    const subscription = source.subscribe({
      next: (value) => subscriber.next(value),
      error: (error: unknown) => subscriber.error(error),
      complete: () => subscriber.complete(),
    });
    return () => {
      timers.clear(timer);
      subscription.unsubscribe();
    };
  });
}
