import { inject, Injectable } from '@angular/core';
import { firstValueFrom, map } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { Timers } from '../../platform/timers';
import { parsePatchNotesMonths, parsePatchNotesPage, PATCH_NOTES_PAGE_SIZE } from './patch-notes';
import { PatchNotesMonth, PatchNotesPage, PatchNotesWindow } from './patch-notes-models';

/** Pages and month lists already fetched are kept this long (PatchNotesUI.cache.TTL_MS): a deploy's new update shows. */
export const PATCH_NOTES_CACHE_MS = 5 * 60 * 1000;
/** A request still on its way after this long is not waited for again (PatchNotesUI.INFLIGHT_MS). */
export const PATCH_NOTES_INFLIGHT_MS = 15000;

interface Entry<T> {
  readonly at: number;
  readonly value: Promise<T>;
  settled: boolean;
}

/**
 * communication.js getPatchNotes / getPatchNoteMonths (GET /patchnotes, /patchnotes/months; public, no participant),
 * with the page's cache of pages: going back and forth asks again only after a few minutes, and the same page asked
 * for while it is on its way waits for that answer. A failure is not kept.
 */
@Injectable()
export class PatchNotesApi {
  private readonly http = inject(GempHttp);
  private readonly timers = inject(Timers);
  private readonly pages = new Map<string, Entry<PatchNotesPage>>();

  page(span: PatchNotesWindow, tag: string | null): Promise<PatchNotesPage> {
    const params: Record<string, string | number> = { count: PATCH_NOTES_PAGE_SIZE };
    if ('slug' in span && span.slug) {
      params['from'] = span.slug;
    } else {
      params['start'] = 'start' in span ? span.start : 0;
    }
    if (tag) {
      params['tag'] = tag;
    }
    const key = `${tag ? `tag:${tag}/` : ''}${params['from'] ? `from:${params['from']}` : `start:${params['start']}`}/${PATCH_NOTES_PAGE_SIZE}`;
    return this.cached(this.pages, key, () =>
      firstValueFrom(this.http.getJson<unknown>('/patchnotes', params).pipe(map(parsePatchNotesPage))),
    );
  }

  /** The months with entries of the tag (the service keeps them for PATCH_NOTES_CACHE_MS). */
  months(tag: string | null): Promise<PatchNotesMonth[]> {
    return firstValueFrom(
      this.http.getJson<unknown>('/patchnotes/months', tag ? { tag } : {}).pipe(map(parsePatchNotesMonths)),
    );
  }

  private cached<T>(cache: Map<string, Entry<T>>, key: string, fetch: () => Promise<T>): Promise<T> {
    const now = this.timers.now();
    const entry = cache.get(key);
    if (entry && (entry.settled ? now - entry.at < PATCH_NOTES_CACHE_MS : now - entry.at < PATCH_NOTES_INFLIGHT_MS)) {
      return entry.value;
    }
    const fresh: Entry<T> = { at: now, value: fetch(), settled: false };
    cache.set(key, fresh);
    fresh.value.then(
      () => {
        fresh.settled = true;
      },
      () => {
        if (cache.get(key) === fresh) {
          cache.delete(key);
        }
      },
    );
    return fresh.value;
  }
}
