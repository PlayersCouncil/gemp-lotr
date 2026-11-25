/** Reading the server's JSON answers, whose values are typed `unknown` until checked. */

/** A string, number or boolean as text; anything else (missing, null, an object) as `fallback`. */
export function asText(value: unknown, fallback = ''): string {
  if (typeof value === 'string') {
    return value;
  }
  return typeof value === 'number' || typeof value === 'boolean' ? String(value) : fallback;
}

/** As asText, but null for a missing or null value. */
export function asTextOrNull(value: unknown): string | null {
  return value === null || value === undefined ? null : asText(value);
}

/** `value` unless it is empty, then `fallback` (the legacy `a || b` on text). */
export function orIfEmpty(value: string | null | undefined, fallback: string): string {
  return value === null || value === undefined || value === '' ? fallback : value;
}
