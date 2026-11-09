/**
 * The cookies the legacy pages keep settings in (jquery.cookie: values URI-encoded, kept for a year). They were set
 * from pages under /gemp-lotr/, so that is their path; this page writes with the same path to share them.
 */
const LEGACY_COOKIE_PATH = '/gemp-lotr';

export function readCookie(cookies: string, name: string): string | null {
  for (const part of cookies.split(';')) {
    const separator = part.indexOf('=');
    if (separator > 0 && part.substring(0, separator).trim() === name) {
      const value = part.substring(separator + 1).trim();
      try {
        return decodeURIComponent(value);
      } catch {
        return value;
      }
    }
  }
  return null;
}

/** saveToCookie: kept for 365 days. */
export function writeCookie(document: Document, name: string, value: string, now = new Date()): void {
  const expires = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires.toUTCString()}; path=${LEGACY_COOKIE_PATH}`;
}
