import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { catchError, EMPTY, from, map, Observable, of, Subject, switchMap } from 'rxjs';
import { errorMessage } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { CollectionPage, CollectionTypeInfo } from '../api/models';
import { CardImageService } from '../cards/card-image.service';
import { Notifier } from '../notifier/notifier';
import { siteImage } from '../platform/app-paths';
import { ImagePreloader } from '../platform/image-preloader.service';

/** Collections every player has, before the limited ones from GET /collection. */
export const BASE_COLLECTION_TYPES: readonly { type: string; name: string }[] = [
  { type: 'default', name: 'All cards' },
  { type: 'permanent', name: 'My cards' },
  { type: 'trophy', name: 'Trophies' },
];

/** Page size slider limits, and its starting position. */
export const PAGE_SIZE_MIN = 4;
export const PAGE_SIZE_MAX = 40;
export const PAGE_SIZE_SLIDER_START = 18;
/** The page size until the slider is moved: 12, although the slider starts at 18, as players are used to. */
export const INITIAL_PAGE_SIZE = 12;

interface CollectionRequest {
  type: string;
  filter: string;
  start: number;
  count: number;
}

/**
 * Which collection is shown and which page of it: loading from GET /collection/{type}. Only the newest request is
 * shown, and a new page appears all at once when its card images have loaded.
 */
@Injectable()
export class CollectionBrowser {
  private readonly api = inject(DeckbuilderApi);
  private readonly images = inject(CardImageService);
  private readonly preloader = inject(ImagePreloader);
  private readonly notifier = inject(Notifier);

  /** The selected collection. */
  readonly collectionType = signal('default');
  /** The limited collections the player has, offered after the base ones. */
  readonly limitedCollections = signal<readonly CollectionTypeInfo[]>([]);

  /** First item of the page, and items per page. */
  readonly start = signal(0);
  readonly count = signal(INITIAL_PAGE_SIZE);
  readonly page = signal<CollectionPage | null>(null);
  readonly previousDisabled = signal(true);
  readonly nextDisabled = signal(true);
  /** The page size slider is enabled once the first page has arrived. */
  readonly sliderDisabled = signal(true);

  private filter = '';
  private readonly requests = new Subject<CollectionRequest>();

  constructor() {
    const subscription = this.requests
      .pipe(
        switchMap((request) =>
          this.api.getCollection(request.type, request.filter, request.start, request.count).pipe(
            // The new page shows when its card images are ready, all at once (a newer request still replaces it)
            switchMap((page) => this.withImagesLoaded(page)),
            catchError((error: unknown) => {
              this.notifier.tell(errorMessage(error, { 404: "You don't have collection of that type." }));
              return EMPTY;
            }),
          ),
        ),
      )
      .subscribe((page) => this.display(page));
    inject(DestroyRef).onDestroy(() => subscription.unsubscribe());
  }

  /** The filter for the next load, without loading. */
  useFilter(filter: string): void {
    this.filter = filter;
  }

  /** Shows a new filter, from the first page. */
  showFilter(filter: string): void {
    this.filter = filter;
    this.goToFirstPage();
    this.load();
  }

  goToFirstPage(): void {
    this.start.set(0);
  }

  selectCollectionType(type: string): void {
    this.collectionType.set(type);
    this.load();
  }

  /** While a page is loading the buttons are disabled, so the second click of a double click does nothing. */
  previousPage(): void {
    if (this.previousDisabled()) {
      return;
    }
    this.disableNavigation();
    this.start.update((start) => start - this.count());
    this.load();
  }

  nextPage(): void {
    if (this.nextDisabled()) {
      return;
    }
    this.disableNavigation();
    this.start.update((start) => start + this.count());
    this.load();
  }

  setPageSize(count: number): void {
    this.goToFirstPage();
    this.count.set(count);
    this.load();
  }

  /** Reloads the current page, e.g. after opening a pack. */
  load(): void {
    this.requests.next({
      type: this.collectionType(),
      filter: this.filter,
      start: this.start(),
      count: this.count(),
    });
  }

  private disableNavigation(): void {
    this.previousDisabled.set(true);
    this.nextDisabled.set(true);
  }

  /** Waits for the page's card images (and the errata layer), up to the preloader's time limit. */
  private withImagesLoaded(page: CollectionPage): Observable<CollectionPage> {
    if (!this.preloader.supported) {
      return of(page);
    }
    const urls = new Set<string>();
    for (const item of page.items) {
      const image = this.images.resolve(item.blueprintId, item.kind === 'pack' ? 'pack' : item.side);
      urls.add(image.imageUrl);
      if (item.kind === 'card' && image.errata) {
        urls.add(siteImage('errata-vertical.png'));
      }
    }
    return from(this.preloader.load(urls)).pipe(map(() => page));
  }

  private display(page: CollectionPage): void {
    this.page.set(page);
    this.previousDisabled.set(this.start() === 0);
    this.nextDisabled.set(this.start() + this.count() >= page.total);
    this.sliderDisabled.set(false);
  }
}
