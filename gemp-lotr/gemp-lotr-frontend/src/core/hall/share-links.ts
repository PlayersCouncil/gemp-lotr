import { inject, Injectable, signal } from '@angular/core';
import { ClipboardWriter } from '../platform/clipboard.service';
import { PAGE_ORIGIN } from '../platform/page-location';
import { siteImage } from '../platform/app-paths';
import { TimerHandle, Timers } from '../platform/timers';

/**
 * Share links (shareLinks.js GempShareLinks): `/gemp-lotr/share/<kind>/<id>` is a server page (ShareRequestHandler,
 * SharePages) whose preview tags show where the link is pasted, and which sends a person on to the hall. The hall
 * only builds these addresses and copies them.
 */
export type ShareKind = 'patch-notes' | 'errata' | 'format' | 'card';

/** The button's tooltip, and its text unless the page gives its own ("Share"). */
export const SHARE_BUTTON_TITLE = 'Copy a link to share (it shows a preview where you paste it)';
export const SHARE_BUTTON_LABEL = 'Copy share link';
export const SHARE_ICON = siteImage('icons/share-from-square-solid.svg');
/** The note beside the button once copied; when copying failed, the link itself in a field labelled so. */
export const LINK_COPIED = 'Link copied';
export const SHARE_LINK_FIELD_LABEL = 'Share link';
/** How long each note stays (GempShareLinks.say). */
export const COPIED_NOTE_MS = 2000;
export const MANUAL_NOTE_MS = 15000;

/** The absolute share link of kind/id (GempShareLinks.url); no id: the kind's own page (patch-notes). */
export function shareUrl(origin: string, kind: ShareKind, id: string | null): string {
  return `${origin}/gemp-lotr/share/${encodeURIComponent(kind)}${id ? `/${encodeURIComponent(id)}` : ''}`;
}

/** What shows beside a share button: "Link copied", or the link to copy by hand. */
export interface ShareNote {
  readonly kind: 'copied' | 'manual';
  readonly url: string;
}

/** The key of a share button's note. */
export function shareKey(kind: ShareKind, id: string): string {
  return `${kind}/${id}`;
}

/** Copies share links and holds the note beside each button for its while. Provided by the routes that share. */
@Injectable()
export class ShareLinks {
  private readonly clipboard = inject(ClipboardWriter);
  private readonly origin = inject(PAGE_ORIGIN);
  private readonly timers = inject(Timers);
  private readonly handles = new Map<string, TimerHandle>();

  /** The notes showing, by shareKey. */
  readonly notes = signal<ReadonlyMap<string, ShareNote>>(new Map());

  url(kind: ShareKind, id: string | null): string {
    return shareUrl(this.origin, kind, id);
  }

  /** Copies the share link of kind/id; a falsy id does nothing (GempShareLinks.setId disables such a button). */
  async copy(kind: ShareKind, id: string): Promise<void> {
    if (!id) {
      return;
    }
    const url = this.url(kind, id);
    const ok = await this.clipboard.copy(url);
    this.show(shareKey(kind, id), { kind: ok ? 'copied' : 'manual', url });
  }

  noteOf(kind: ShareKind, id: string): ShareNote | null {
    return this.notes().get(shareKey(kind, id)) ?? null;
  }

  private show(key: string, note: ShareNote): void {
    this.timers.clear(this.handles.get(key) ?? null);
    this.notes.update((notes) => new Map(notes).set(key, note));
    this.handles.set(
      key,
      this.timers.after(note.kind === 'copied' ? COPIED_NOTE_MS : MANUAL_NOTE_MS, () => {
        this.handles.delete(key);
        this.notes.update((notes) => {
          const next = new Map(notes);
          next.delete(key);
          return next;
        });
      }),
    );
  }
}
