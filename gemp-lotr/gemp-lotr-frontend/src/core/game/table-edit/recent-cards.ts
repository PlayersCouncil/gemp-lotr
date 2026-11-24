import { inject, Injectable, signal } from '@angular/core';
import { LocalStore } from '../../platform/local-store';

/** A card put on the table by hand lately. */
export interface RecentCard {
  blueprintId: string;
  name: string;
  /** Its card type (COMPANION, MINION...), for which places it fits. */
  type: string;
}

/** How many cards Recent remembers. */
export const RECENT_MAX = 12;
/** Where they are remembered, on this device (per viewer: a convenience, never needed). */
export const RECENT_KEY = 'gemp-edit-recent';

/** The cards remembered, from what storage held (anything unreadable left out), most recent first. */
export function readRecent(value: unknown): RecentCard[] {
  if (!Array.isArray(value)) return [];
  return value
    .flatMap((item: unknown) => {
      if (typeof item !== 'object' || item === null) return [];
      const { blueprintId, name, type } = item as Record<string, unknown>;
      return typeof blueprintId === 'string' && typeof name === 'string'
        ? [{ blueprintId, name, type: typeof type === 'string' ? type : '' }]
        : [];
    })
    .slice(0, RECENT_MAX);
}

/** The list with the card used now at its head (once), at most RECENT_MAX long. */
export function used(list: readonly RecentCard[], card: RecentCard): RecentCard[] {
  return [card, ...list.filter((other) => other.blueprintId !== card.blueprintId)].slice(0, RECENT_MAX);
}

/**
 * The cards this viewer put on a table lately (the chooser's Recent), most recent first, a dozen at most. Kept in the
 * browser's local storage (LocalStore), so the page works the same without it. Page-scoped.
 */
@Injectable()
export class RecentCards {
  private readonly store = inject(LocalStore);
  readonly list = signal<readonly RecentCard[]>(readRecent(this.store.read(RECENT_KEY)));

  use(card: RecentCard): void {
    this.list.set(used(this.list(), card));
    this.store.write(RECENT_KEY, this.list());
  }
}
