/**
 * What a page of the hall says in place of something it could not load (commonUi.js neutralErrorMap, which Help ›
 * Format Definitions and Help › PC Errata use): `subject` names it, e.g. "the format definitions". The legacy map left
 * other statuses to the hall's "Server error" dialog while the page kept saying "Loading…"; here they read as the
 * server being unavailable.
 */
export function loadErrorMessage(status: number, subject = 'this page'): string {
  switch (status) {
    case 0:
      return `Could not reach the server to load ${subject}. Check your connection and reload the page.`;
    case 400:
      return `Could not load ${subject} (the server did not understand the request).`;
    case 401:
      return `You are not logged in. Log in again to see ${subject}.`;
    case 403:
      return `You do not have access to ${subject}.`;
    case 404:
      return `${subject.charAt(0).toUpperCase()}${subject.substring(1)} could not be found.`;
    default:
      return `Could not load ${subject} right now. Please try again in a few minutes.`;
  }
}
