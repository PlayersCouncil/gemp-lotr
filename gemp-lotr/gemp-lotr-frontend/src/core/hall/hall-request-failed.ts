import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { Notifier } from '../notifier/notifier';

/** hallUi.js requestFailed: a side request's failure the part does not word itself. */
export const SERVER_ERROR_TITLE = 'Server error';
export const serverErrorText = (status: number) =>
  `The server could not complete that request (error ${status}). Try again; if it keeps happening, reload the page.`;

/** Who hallRequestFailed tells: the hall's "Not logged in" dialog (HallSession) and the Notifier. */
export interface HallRequestFailedTargets {
  requireLogin(): void;
  readonly notifier: Notifier;
}

/**
 * hallUi.js requestFailed, for a side request whose part does not word a failure itself: 401 is the "Not logged in"
 * dialog; no answer or 504 nothing (the connection readout says it); an answer that could not be read nothing (the
 * legacy page's jQuery took it for a 200); any other the "Server error" message.
 */
export function hallRequestFailed(error: ApiError, targets: HallRequestFailedTargets): void {
  if (error.is(401)) {
    targets.requireLogin();
  } else if (!error.is(0) && !error.is(504) && !error.is(INVALID_RESPONSE_STATUS)) {
    targets.notifier.tell(serverErrorText(error.status));
  }
}

/**
 * The server's words for a refused request: GempukkuHttpRequestHandler sends them in the `message` header of a 400.
 * `cause` is the ApiError's (the HTTP answer); null when there are none (or they are empty).
 */
export function messageHeader(cause: unknown): string | null {
  if (cause === null || typeof cause !== 'object') {
    return null;
  }
  const headers = (cause as { headers?: { get(name: string): string | null } }).headers;
  const message = headers?.get('message') ?? null;
  return message !== null && message !== '' ? message : null;
}
