import { isSignal } from '@angular/core';

/** A class's public members only (private fields would make an intersection of classes impossible). */
export type Public<T> = { [K in keyof T]: T[K] };

/**
 * Several objects as one, so a behaviour test can describe what the player gets whichever class does it. A name
 * belongs to the first object that has it, so list a class with a public member before those with a private one of the
 * same name.
 */
export function combined<T extends object>(...parts: object[]): T {
  return new Proxy({} as T, {
    get: (_, name) => {
      const owner = parts.find((part) => name in part) as Record<string | symbol, unknown> | undefined;
      const value = owner?.[name];
      return typeof value === 'function' && !isSignal(value) ? (value.bind(owner) as unknown) : value;
    },
    set: (_, name, value: unknown) => {
      const owner = parts.find((part) => name in part) as Record<string | symbol, unknown>;
      owner[name] = value;
      return true;
    },
  });
}
