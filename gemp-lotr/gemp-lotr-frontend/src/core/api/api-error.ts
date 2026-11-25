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
    options?: { cause?: unknown; said?: string | null },
  ) {
    super(message, options);
    this.name = 'ApiError';
    this.said = options?.said ?? null;
  }

  /**
   * What the server said went wrong, where its answer says so in words: the `error` of a JSON body (`{"error": "..."}`,
   * as the scenario authoring calls answer a refusal); null otherwise.
   */
  readonly said: string | null;

  static from(error: unknown): ApiError {
    if (error instanceof ApiError) {
      return error;
    }
    if (error instanceof HttpErrorResponse) {
      return new ApiError(error.status, `Gemp server call failed (HTTP ${error.status}): ${error.url ?? ''}`, {
        cause: error,
        said: saidIn(error.error as unknown),
      });
    }
    return new ApiError(INVALID_RESPONSE_STATUS, 'Unreadable response from the Gemp server', { cause: error });
  }

  is(status: number): boolean {
    return this.status === status;
  }
}

/** The `error` text of a JSON answer body (a string, as the calls ask for text; or already parsed), if it has one. */
function saidIn(body: unknown): string | null {
  let value = body;
  if (typeof body === 'string') {
    try {
      value = JSON.parse(body) as unknown;
    } catch {
      return null;
    }
  }
  const said = value !== null && typeof value === 'object' ? (value as { error?: unknown }).error : undefined;
  return typeof said === 'string' && said !== '' ? said : null;
}

/** What to tell the player about a failed call: the message for its status, or else the generic one. */
export function errorMessage(error: unknown, byStatus: Readonly<Record<number, string>> = {}): string {
  return byStatus[ApiError.from(error).status] ?? GENERIC_COMMUNICATION_ERROR;
}
