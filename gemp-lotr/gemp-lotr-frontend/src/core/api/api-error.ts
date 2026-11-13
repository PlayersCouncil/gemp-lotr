import { HttpErrorResponse } from '@angular/common/http';

/** The message for any failure that a call site doesn't handle itself. */
export const GENERIC_COMMUNICATION_ERROR = 'There was a problem during communication with server';

/** Status used when the server answered 200 but the body could not be read (e.g. malformed XML). */
export const INVALID_RESPONSE_STATUS = -1;

/**
 * Every failed Gemp server call surfaces as an ApiError, so callers can react to specific statuses the same way the
 * legacy client's per-call "errorMap"s did (e.g. 404 = no such collection, 400 = invalid deck, 414 = decklist too long)
 * and fall back to GENERIC_COMMUNICATION_ERROR otherwise.
 */
export class ApiError extends Error {
  constructor(
    /** HTTP status; 0 for network failures, INVALID_RESPONSE_STATUS for unreadable responses. */
    readonly status: number,
    message: string,
    options?: { cause?: unknown },
  ) {
    super(message, options);
    this.name = 'ApiError';
  }

  static from(error: unknown): ApiError {
    if (error instanceof ApiError) {
      return error;
    }
    if (error instanceof HttpErrorResponse) {
      return new ApiError(error.status, `Gemp server call failed (HTTP ${error.status}): ${error.url ?? ''}`, {
        cause: error,
      });
    }
    return new ApiError(INVALID_RESPONSE_STATUS, 'Unreadable response from the Gemp server', { cause: error });
  }

  is(status: number): boolean {
    return this.status === status;
  }
}

/** What to tell the player about a failed call: the message for its status, or else the generic one. */
export function errorMessage(error: unknown, byStatus: Readonly<Record<number, string>> = {}): string {
  return byStatus[ApiError.from(error).status] ?? GENERIC_COMMUNICATION_ERROR;
}
