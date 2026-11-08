import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiError, GENERIC_COMMUNICATION_ERROR } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { PackDelivery } from '../api/models';
import { CollectionService } from '../collection/collection.service';
import { PackDeliveryService } from '../delivery/pack-delivery.service';
import { Notifier } from '../notifier/notifier';

/** The legacy page asked for the opened pack's contents a second after opening it, besides the server's notice. */
const DELIVERY_DELAY_MS = 1000;

export interface ShownCard {
  blueprintId: string;
  count: number | null;
  pack: boolean;
}

/**
 * Opening packs from the collection (deckBuildingUi.clickCardFunction), the "Choose one" dialog of selection packs,
 * and the "New items - <collection>" dialogs showing what was opened (deckbuilderDeliveryService.js).
 */
@Injectable({ providedIn: 'root' })
export class PacksService {
  private readonly api = inject(DeckbuilderApi);
  private readonly collection = inject(CollectionService);
  private readonly delivery = inject(PackDeliveryService);
  private readonly notifier = inject(Notifier);

  /** The selection pack being opened and its choices. */
  readonly selection = signal<{ packId: string; choices: string[] } | null>(null);
  /** The open "New items" dialogs, by collection name. */
  readonly deliveries = signal<ReadonlyMap<string, ShownCard[]>>(new Map());

  constructor() {
    this.delivery.deliveries$
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe((deliveries) => this.delivered(deliveries));
  }

  /** As the legacy page did on start: shows anything opened elsewhere and not yet seen. */
  start(): void {
    this.delivery.fetch();
  }

  packClicked(blueprintId: string, selection: string[] | null): void {
    if (blueprintId.startsWith('(S)')) {
      this.selection.set({ packId: blueprintId, choices: selection ?? [] });
      return;
    }
    this.api.openPack(this.collection.collectionType(), blueprintId).subscribe({
      next: () => this.opened(),
      error: (error: unknown) =>
        void this.notifier.alert(
          ApiError.from(error).is(404)
            ? 'You have no pack of this type in your collection.'
            : GENERIC_COMMUNICATION_ERROR,
        ),
    });
  }

  /** A card of the "Choose one" dialog was picked: the dialog closes and the pack is opened with it. */
  choose(blueprintId: string): void {
    const selection = this.selection();
    if (!selection) {
      return;
    }
    this.selection.set(null);
    this.api.openSelectionPack(this.collection.collectionType(), selection.packId, blueprintId).subscribe({
      next: () => this.opened(),
      error: (error: unknown) =>
        void this.notifier.alert(
          ApiError.from(error).is(404)
            ? 'You have no pack of this type in your collection or that selection is not available for this pack.'
            : GENERIC_COMMUNICATION_ERROR,
        ),
    });
  }

  closeDelivery(collectionName: string): void {
    this.deliveries.update((open) => {
      const next = new Map(open);
      next.delete(collectionName);
      return next;
    });
  }

  private opened(): void {
    setTimeout(() => this.delivery.fetch(), DELIVERY_DELAY_MS);
    this.collection.load();
  }

  /** New contents go into the collection's dialog, after anything it still shows. */
  private delivered(deliveries: PackDelivery[]): void {
    this.deliveries.update((open) => {
      const next = new Map(open);
      for (const delivery of deliveries) {
        next.set(delivery.collectionName, [
          ...(next.get(delivery.collectionName) ?? []),
          ...delivery.packs.map((p) => ({ blueprintId: p.blueprintId, count: p.count, pack: true })),
          ...delivery.cards.map((c) => ({ blueprintId: c.blueprintId, count: c.count, pack: false })),
        ]);
      }
      return next;
    });
  }
}
