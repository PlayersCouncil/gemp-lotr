/** Test-only: a repeatable source of random choices (mulberry32), so a generated test can be run again by its seed. */
export interface Choices {
  /** A whole number from 0 to limit - 1. */
  below(limit: number): number;
  of<T>(items: readonly T[]): T;
  /** True with this probability. */
  chance(probability: number): boolean;
}

export function choices(seed: number): Choices {
  let state = seed >>> 0;
  const next = () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
  return {
    below: (limit) => Math.floor(next() * limit),
    of: (items) => items[Math.floor(next() * items.length)]!,
    chance: (probability) => next() < probability,
  };
}
