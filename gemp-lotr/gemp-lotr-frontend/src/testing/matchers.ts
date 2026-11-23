/**
 * Vitest's asymmetric matchers, typed: vitest types them any, so they would fit anywhere unchecked. As unknown they
 * still go into toEqual and toHaveBeenCalledWith, which take any value.
 */

/** Any instance of the class (expect.any). */
export function instanceOf(type: abstract new (...args: never[]) => unknown): unknown {
  return expect.any(type) as unknown;
}

/** An object with at least these fields (expect.objectContaining). */
export function containing(fields: Record<string, unknown>): unknown {
  return expect.objectContaining(fields) as unknown;
}
