import { listOf, stringsOf } from '../../api/json';
import { parseServerMarkup, RICH_MARKUP } from '../../chat/chat-markup';
import { PatchNote, PatchNoteFilter, PatchNotesMonth, PatchNotesPage } from './patch-notes-models';

/** Ported from js/gemp-022/patchNotesUi.js (PatchNotes) and the server's PatchNote.toJson. */

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function text(value: unknown): string | null {
  return typeof value === 'string' ? value : null;
}

function int(value: unknown, fallback: number): number {
  return typeof value === 'number' && Number.isInteger(value) ? value : fallback;
}

/** The updates shown at a time (PatchNotes.PAGE_SIZE). */
export const PATCH_NOTES_PAGE_SIZE = 5;
/** The filter's chips until the server's list arrives (PatchNote.TAGS). */
export const PATCH_NOTE_TAGS: readonly string[] = ['Card Fixes', 'PC Updates', 'User Interface', 'Announcement'];

function parseNote(value: unknown): PatchNote | null {
  const json = object(value);
  const slug = text(json['slug']);
  if (slug === null || slug === '') {
    return null;
  }
  const summary = text(json['summary']);
  const summaryHtml = text(json['summaryHtml']);
  return {
    slug,
    kind: json['kind'] === 'announcement' ? 'announcement' : 'note',
    date: text(json['date']) ?? '',
    title: text(json['title']) === '' ? null : text(json['title']),
    summary,
    tags: stringsOf(json['tags']),
    // summaryHtml: the summary with its culture icons, escaped by the server; else plain text
    summaryMarkup: summaryHtml
      ? parseServerMarkup(summaryHtml, RICH_MARKUP)
      : summary
        ? [{ kind: 'text', text: summary }]
        : null,
    body: parseServerMarkup(text(json['html']) ?? '', RICH_MARKUP),
  };
}

/** GET /patchnotes's answer. */
export function parsePatchNotesPage(json: unknown): PatchNotesPage {
  const data = object(json);
  const notes = listOf(data['notes'])
    .map(parseNote)
    .filter((note): note is PatchNote => note !== null);
  const filters: PatchNoteFilter[] = listOf(data['filters']).map((entry: unknown) => {
    const filter = object(entry);
    return { tag: text(filter['tag']) ?? '', count: int(filter['count'], 0) };
  });
  return {
    total: int(data['total'], notes.length),
    start: int(data['start'], 0),
    count: int(data['count'], PATCH_NOTES_PAGE_SIZE),
    tag: text(data['tag']),
    all: int(data['all'], 0),
    filters,
    notes,
  };
}

/** GET /patchnotes/months's answer, newest first. */
export function parsePatchNotesMonths(json: unknown): PatchNotesMonth[] {
  return listOf(object(json)['months'])
    .map((entry: unknown) => {
      const month = object(entry);
      return {
        month: text(month['month']) ?? '',
        first: text(month['first']) ?? '',
        count: int(month['count'], 0),
      };
    })
    .filter((month) => month.month !== '' && month.first !== '');
}

/** "#patch-notes" → newest; "#patch-notes/2023-12-09" → that note, in lower case; anything else null (parseHash). */
export function parsePatchNotesSlug(value: string | null | undefined): string | null {
  const match = /^[A-Za-z0-9-]+$/.exec(value ?? '');
  return match ? match[0].toLowerCase() : null;
}
