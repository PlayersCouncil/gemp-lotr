import { MarkupNode } from '../../chat/chat-markup';

/**
 * Server Info › Patch Notes (PatchNotesRequestHandler, PatchNotesLibrary, PatchNote): the Markdown files in the web
 * root's patchnotes/ plus the past server announcements, rendered and sanitized by the server (PatchNoteRenderer),
 * newest first. Public.
 */
export type PatchNoteKind = 'note' | 'announcement';

export interface PatchNote {
  /** Its name: the file name without .md, or announcement-<id>. */
  readonly slug: string;
  readonly kind: PatchNoteKind;
  /** "YYYY-MM-DD", shown as it is. */
  readonly date: string;
  readonly title: string | null;
  readonly summary: string | null;
  readonly tags: readonly string[];
  /** The summary with its culture icons (summaryHtml), else its plain text; null without a summary. */
  readonly summaryMarkup: readonly MarkupNode[] | null;
  /** The body (html), element by element. */
  readonly body: readonly MarkupNode[];
}

/** A tag and how many entries have it (the filter's chips). */
export interface PatchNoteFilter {
  readonly tag: string;
  readonly count: number;
}

/** GET /patchnotes: a window of the feed (of the tag), from `start`. */
export interface PatchNotesPage {
  readonly total: number;
  readonly start: number;
  readonly count: number;
  readonly tag: string | null;
  /** The entries of every tag. */
  readonly all: number;
  readonly filters: readonly PatchNoteFilter[];
  readonly notes: readonly PatchNote[];
}

/** GET /patchnotes/months: a month with entries, its newest entry and how many. */
export interface PatchNotesMonth {
  /** "2026-09". */
  readonly month: string;
  readonly first: string;
  readonly count: number;
}

/** What to show: the window starting at an entry (null: the newest), or at a place in the feed. */
export type PatchNotesWindow = { readonly slug: string | null } | { readonly start: number };

/** The status line above the list (showStatus). */
export type PatchNotesStatus =
  | { readonly kind: 'loading'; readonly shown: boolean }
  | { readonly kind: 'done' }
  | { readonly kind: 'error'; readonly status: number };

/** The month dropdown's state (MonthNav.renderList). */
export type PatchNotesMonthList =
  | { readonly kind: 'loading' }
  | { readonly kind: 'error'; readonly message: string }
  | {
      readonly kind: 'ready';
      readonly months: readonly string[];
      readonly current: string | null;
      readonly emptyText: string;
    };

/** Newer / Older at the bottom (renderPagers); null when the feed is empty. */
export interface PatchNotesPager {
  readonly newer: boolean;
  readonly older: boolean;
  readonly range: string;
}
