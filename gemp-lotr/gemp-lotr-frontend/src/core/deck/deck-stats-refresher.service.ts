import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { errorMessage } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { Notifier } from '../notifier/notifier';
import { DeckDraft } from './deck-draft.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { DeckStats, EMPTY_STATS, parseDeckStats } from './deck-stats';

/** How soon after a change the stats are asked for; a burst of changes within it makes one request. */
export const STATS_REFRESH_DELAY_MS = 500;

/** The server's verdict on the deck (counts and the format's rules), asked for shortly after something changes. */
@Injectable()
export class DeckStatsRefresher {
  private readonly api = inject(DeckbuilderApi);
  private readonly draft = inject(DeckDraft);
  private readonly formats = inject(DeckFormatSelection);
  private readonly notifier = inject(Notifier);

  /** The server's deck stats, as data. */
  readonly stats = signal<DeckStats>(EMPTY_STATS);

  private started = false;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private request: Subscription | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.clearTimer();
      this.request?.unsubscribe();
    });
  }

  /** The page has started: the first stats, and from now on one refresh per burst of changes. */
  start(): void {
    this.started = true;
    this.schedule();
  }

  refreshSoon(): void {
    if (this.started) {
      this.schedule();
    }
  }

  private schedule(): void {
    this.timer ??= setTimeout(() => {
      this.timer = null;
      this.send();
    }, STATS_REFRESH_DELAY_MS);
  }

  private clearTimer(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }

  /** Only the newest request counts: an older answer arriving late must not replace it. */
  private send(): void {
    this.request?.unsubscribe();
    const option = this.formats.selectedFormat();
    this.request = this.api
      .getDeckStatsHtml(
        this.formats.contentsOf(this.draft.deck()),
        this.formats.effectiveFormat(),
        option.collectionType,
        option.rtmd ? option.value : null,
      )
      .subscribe({
        next: (html) => this.stats.set(parseDeckStats(html)),
        error: (error: unknown) => this.notifier.tell(errorMessage(error, { 400: 'Invalid deck for getting stats.' })),
      });
  }
}
