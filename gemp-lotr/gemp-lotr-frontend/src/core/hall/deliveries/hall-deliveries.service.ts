import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, map, Observable } from 'rxjs';
import { DeliveryAnnouncements, DeliveryKind } from '../../api/delivery-announcements';
import { GempHttp } from '../../api/gemp-http.service';
import { PackDelivery } from '../../api/models';
import { parsePackDelivery } from '../../api/parsers';
import { MarkupNode, parseServerMarkup } from '../../chat/chat-markup';
import { asText } from '../json-values';

/** An announcement (GET /delivery/announcements, DBDefs.Announcement; content rendered from Markdown by the server). */
export interface HallAnnouncement {
  readonly id: number | null;
  readonly title: string;
  readonly content: readonly MarkupNode[];
}

/** A Race to Mount Doom move (GET /delivery/league, DBDefs.Transfer). */
export interface LeagueNotification {
  readonly message: string;
  /** The old position's meta-site (its picture and its modifier's), greyed; null when it starts from 0. */
  readonly old: { readonly visual: string; readonly modifier: string | null } | null;
  readonly next: { readonly visual: string; readonly modifier: string | null } | null;
}

/** The dialogs' titles and words (deliveryService.js, announcementDeliveryService.js, leagueDeliveryService.js). */
export const newItemsTitle = (collection: string) => `New items - ${collection}`;
export const announcementTitle = (title: string) => `ANNOUNCEMENT - ${title}`;
export const DISMISS = 'Dismiss';
export const REMIND_LATER = 'Remind Me Later';
export const RTMD_TITLE = 'Race to Mount Doom';
export const RTMD_DEFAULT_MESSAGE = 'You have advanced!';

export function parseAnnouncement(json: unknown): HallAnnouncement | null {
  if (json === null || typeof json !== 'object') {
    return null;
  }
  const data = json as Record<string, unknown>;
  // the legacy popup took the <br/>s out of the server's rendering
  const content = asText(data['content']).replaceAll('<br/>', '');
  return {
    id: typeof data['id'] === 'number' ? data['id'] : null,
    title: asText(data['title']),
    content: parseServerMarkup(content),
  };
}

/** "oldVisual|oldMod|newVisual|newMod" (parseLeagueContents); any part may be empty. */
export function parseLeagueNotification(entry: Record<string, unknown>): LeagueNotification {
  const parts = typeof entry['contents'] === 'string' ? entry['contents'].split('|') : [];
  const part = (i: number) => (parts.length >= 4 && parts[i] ? parts[i] : null);
  const site = (visual: string | null, modifier: string | null) => (visual === null ? null : { visual, modifier });
  const old = site(part(0), part(1));
  const next = site(part(2), part(3));
  return {
    message: typeof entry['message'] === 'string' && entry['message'] !== '' ? entry['message'] : RTMD_DEFAULT_MESSAGE,
    // only the new position is shown when there is no old one
    old: next === null ? null : old,
    next,
  };
}

/** The delivery calls (DeliveryRequestHandler). Each answers 404 when nothing waits. */
@Injectable()
export class HallDeliveriesApi {
  private readonly http = inject(GempHttp);

  /** GET /delivery/misc: the packages (packs and cards) delivered; consumes them. */
  packages(): Observable<PackDelivery[]> {
    return this.http.getXml('/delivery/misc').pipe(map(parsePackDelivery));
  }

  /** GET /delivery/announcements: the current announcement, until dismissed or snoozed. */
  announcement(): Observable<HallAnnouncement | null> {
    return this.http.getJson<unknown>('/delivery/announcements').pipe(map(parseAnnouncement));
  }

  dismissAnnouncement(): Observable<string> {
    return this.http.postText('/delivery/announcements/dismiss', {});
  }

  snoozeAnnouncement(): Observable<string> {
    return this.http.postText('/delivery/announcements/snooze', {});
  }

  /** GET /delivery/league: the Race to Mount Doom moves; consumes them. */
  leagueNotifications(): Observable<LeagueNotification[]> {
    return this.http
      .getJson<unknown>('/delivery/league')
      .pipe(
        map((json) =>
          Array.isArray(json)
            ? json
                .filter((e): e is Record<string, unknown> => e !== null && typeof e === 'object')
                .map(parseLeagueNotification)
            : [],
        ),
      );
  }
}

/**
 * What the hall shows on arrival (communication.js deliveryCheck and the three delivery services): when a response
 * says something waits, the packages go into a "New items - <collection>" dialog per collection (added to it while it
 * is open), the announcement shows (one at a time; Dismiss, Remind Me Later, closing it snoozes it), and the Race to
 * Mount Doom moves show one after the other. Failures are ignored, as on the legacy page.
 */
@Injectable()
export class HallDeliveries {
  private readonly api = inject(HallDeliveriesApi);

  /** The "New items" dialogs open, by collection, in the order they opened. */
  readonly newItems = signal<readonly PackDelivery[]>([]);
  readonly announcement = signal<HallAnnouncement | null>(null);
  /** The moves waiting; the first one shows. */
  readonly leagueNotifications = signal<readonly LeagueNotification[]>([]);
  private fetching = new Set<DeliveryKind>();

  constructor() {
    const announced = inject(DeliveryAnnouncements).announced$.subscribe((kind) => void this.fetch(kind));
    inject(DestroyRef).onDestroy(() => announced.unsubscribe());
  }

  /** Fetches what a header announced (once at a time per kind; an announcement already showing is not asked again). */
  async fetch(kind: DeliveryKind): Promise<void> {
    if (this.fetching.has(kind) || (kind === 'announcement' && this.announcement() !== null)) {
      return;
    }
    this.fetching.add(kind);
    try {
      if (kind === 'package') {
        this.addItems(await firstValueFrom(this.api.packages()));
      } else if (kind === 'announcement') {
        const announcement = await firstValueFrom(this.api.announcement());
        if (announcement !== null && this.announcement() === null) {
          this.announcement.set(announcement);
        }
      } else {
        const moves = await firstValueFrom(this.api.leagueNotifications());
        this.leagueNotifications.update((waiting) => [...waiting, ...moves]);
      }
    } catch {
      // 404: nothing to deliver; the legacy page ignored every failure here
    } finally {
      this.fetching.delete(kind);
    }
  }

  private addItems(deliveries: readonly PackDelivery[]): void {
    for (const delivery of deliveries) {
      if (delivery.packs.length === 0 && delivery.cards.length === 0) {
        continue;
      }
      this.newItems.update((open) => {
        const existing = open.find((d) => d.collectionName === delivery.collectionName);
        if (existing === undefined) {
          return [...open, delivery];
        }
        const merged: PackDelivery = {
          collectionName: existing.collectionName,
          packs: [...existing.packs, ...delivery.packs],
          cards: [...existing.cards, ...delivery.cards],
        };
        return open.map((d) => (d === existing ? merged : d));
      });
    }
  }

  /** A "New items" dialog closed (its cards go). */
  closeNewItems(collectionName: string): void {
    this.newItems.update((open) => open.filter((d) => d.collectionName !== collectionName));
  }

  /** Dismiss: not shown again. */
  dismissAnnouncement(): void {
    this.announcement.set(null);
    this.api.dismissAnnouncement().subscribe({ error: () => undefined });
  }

  /** Remind Me Later, or the dialog closed any other way (the legacy close handler snoozed too). */
  snoozeAnnouncement(): void {
    this.announcement.set(null);
    this.api.snoozeAnnouncement().subscribe({ error: () => undefined });
  }

  /** The move showing was closed: the next one shows. */
  closeLeagueNotification(): void {
    this.leagueNotifications.update((waiting) => waiting.slice(1));
  }
}
