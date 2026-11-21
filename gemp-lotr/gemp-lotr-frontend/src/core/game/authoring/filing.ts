/**
 * Save as and Rename (the legacy scenarioAuthor.js filingForm): where the draft's file is to go, as its author types
 * it and as the server takes it. A file is always kept under the folder of its kind and always ends in .hjson, so
 * neither is typed: both are shown either side of the name. Pure.
 */
import { DraftState } from './authoring-state';

/** The folders scenarios are kept under by kind: no path is given with one of these in front (the server puts it). */
export const KIND_FOLDERS = ['tutorials', 'puzzles', 'sandboxes'] as const;
/** How long the path of a scenario's file may be (the server's ScenarioLibrary.MAX_PATH). */
export const MAX_PATH = 120;
const ENDING = '.hjson';

/** Save as (a scenario of its own, from here on) or Rename (the same scenario, its file moved). */
export type FilingHow = 'copy' | 'rename';

/** A file's place as its author says it: without the folder of its kind in front, and without .hjson on the end. */
export function bareName(path: string | null | undefined): string {
  let bare = (path ?? '').replace(/\.hjson$/i, '');
  const slash = bare.indexOf('/');
  if (slash > 0 && (KIND_FOLDERS as readonly string[]).includes(bare.substring(0, slash).toLowerCase())) {
    bare = bare.substring(slash + 1);
  }
  return bare;
}

/**
 * What an author types for where a file goes, as a path the server will take: slashes one way, nothing but what a
 * file's name may be made of, each folder's name and the file's with a letter or a digit at either end, and .hjson on
 * the end (typed or not). Nothing usable: "".
 */
export function tidyPath(typed: string): string {
  const path = typed
    .replace(/\\/g, '/')
    .trim()
    .replace(/\.hjson$/i, '')
    .replace(/[^A-Za-z0-9_. /-]+/g, '-')
    .split('/')
    .map((name) => name.replace(/^[^A-Za-z0-9]+|[^A-Za-z0-9]+$/g, ''))
    .filter((name) => name !== '')
    .join('/');
  return path === '' ? '' : path + ENDING;
}

/** The id a copy saved at a tidy path is known by (the server's: from the file's name). */
export function copyIdOf(tidy: string): string {
  return tidy
    .replace(/^.*\//, '')
    .replace(/\.hjson$/i, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

/** The folder of the draft's kind, with its slash. */
export function folderOf(state: DraftState): string {
  return `${state.kindFolder || 'tutorials'}/`;
}

/** What the name starts as: where the file is now, or its id; a copy's with "-copy" after it. */
export function suggestedName(state: DraftState, how: FilingHow): string {
  const bare = bareName(state.file ?? state.draft.id);
  return how === 'copy' ? `${bare}-copy` : bare;
}

/** How long the typed name may be: the server's most, less the folder and the ending. */
export function nameLength(state: DraftState): number {
  return MAX_PATH - folderOf(state).length - ENDING.length;
}

/**
 * Whether the file goes among the game's own scenarios: it stays there if it is one of them (an admin opened it
 * there), or where the server has no folder for its players'; otherwise it goes in the author's own folder. A
 * scenario becomes public by being submitted and approved, not from here (the legacy form's "Public Scenario" tick
 * was never shown).
 */
export function canonicalOf(state: DraftState): boolean {
  return !state.canSaveAnywhere || state.canonical;
}

/** Where the file will be, as said under the name ("" while nothing usable is typed). */
export function filingPreview(state: DraftState, how: FilingHow, typed: string): string {
  const tidy = tidyPath(typed);
  if (tidy === '') return '';
  const own = canonicalOf(state) ? '' : 'your own folder: ';
  return `${own}${folderOf(state)}${tidy}${how === 'copy' ? `  (id: ${copyIdOf(tidy)})` : ''}`;
}
