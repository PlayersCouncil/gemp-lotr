/** Test-only helpers for reading files from disk (the unit tests run in Node). */
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

/** gemp-lotr-async/src/main/web, the legacy client's web root (tests run from gemp-lotr-frontend). */
export const LEGACY_WEB_DIR = resolve(process.cwd(), '../gemp-lotr-async/src/main/web');

/** A saved server response from src/testing/server-responses. */
export function readServerResponse(name: string): string {
  return readFileSync(resolve(process.cwd(), 'src/testing/server-responses', name), 'utf8');
}

export function readServerXml(name: string): Document {
  return new DOMParser().parseFromString(readServerResponse(name), 'application/xml');
}

/** A file of the legacy client, by its path under the web root (e.g. "js/gemp-022/hobbit.js"). */
export function readLegacyWebFile(path: string): string {
  return readFileSync(resolve(LEGACY_WEB_DIR, path), 'utf8');
}
