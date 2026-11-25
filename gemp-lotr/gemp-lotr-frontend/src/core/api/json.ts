/**
 * Reading values whose shape is not known yet (a server's JSON, something stored) without the type any: JSON.parse's
 * result is any, and Array.isArray makes an unknown value any[], so that everything read from either goes unchecked.
 */

/** JSON text as a value still to be checked. Throws on text that isn't JSON, as JSON.parse does. */
export function parseJson(text: string): unknown {
  return JSON.parse(text) as unknown;
}

/** A value as a list, its items still to be checked; an empty list when it isn't one. */
export function listOf(value: unknown): readonly unknown[] {
  return Array.isArray(value) ? (value as unknown[]) : [];
}

/** The strings of a list; whatever else is in it, or a value that is no list, gives none. */
export function stringsOf(value: unknown): string[] {
  return listOf(value).filter((item): item is string => typeof item === 'string');
}
