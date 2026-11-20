import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { LocalSettings } from '../../platform/local-settings.service';
import { TimerHandle, Timers } from '../../platform/timers';
import { loadErrorMessage } from '../hall-load-error';
import { findFormat } from './format-definitions';
import { FormatDefinition, FormatDefinitionsLoad } from './format-definitions-models';
import { FormatDefinitionsApi } from './format-definitions-api.service';
import { filterFormats, FormatFilter } from './format-filter';
import { FORMAT_DEFINITIONS_SUBJECT } from './help-messages';

/** localStorage: "1" when the lists show as card images (GempFormatDefinitions.IMAGES_KEY). */
export const FORMAT_IMAGES_KEY = 'gemp.formatDefinitions.images';
/** The filter waits this long after the last key (applyFilter's timer). */
export const FILTER_DELAY_MS = 120;
/** A revealed format is highlighted this long (GempFormatHelp.reveal). */
export const HIGHLIGHT_MS = 4000;

/** A format to bring into view; `seq` tells two reveals of the same format apart. */
export interface FormatReveal {
  readonly code: string;
  readonly seq: number;
}

/**
 * Help › Format Definitions (formatDefinitions.js GempFormatDefinitions, TableFlow.js GempFormatHelp): the formats, the
 * filter, the "Card images" choice, and which format a link opens. Scrolling (the reveal, the contents list following
 * the reading position) is the view's: it scrolls to `reveal()` and reports the format at the top with `setActive`.
 */
@Injectable()
export class FormatDefinitionsService {
  private readonly api = inject(FormatDefinitionsApi);
  private readonly settings = inject(LocalSettings);
  private readonly timers = inject(Timers);

  private loadSeq = 0;
  private revealSeq = 0;
  private filterTimer: TimerHandle | null = null;
  private highlightTimer: TimerHandle | null = null;
  /** The format a link asked for before the formats arrived (GempFormatHelp.pending). */
  private pending: string | null = null;

  readonly load = signal<FormatDefinitionsLoad>({ kind: 'loading' });
  readonly formats = computed<readonly FormatDefinition[]>(() => {
    const load = this.load();
    return load.kind === 'ready' ? load.formats : [];
  });
  /** The lists show as card images rather than names. */
  readonly images = signal(this.settings.read(FORMAT_IMAGES_KEY) === '1');
  /** The filter box's text, as typed. */
  readonly query = signal('');
  private readonly appliedQuery = signal('');
  readonly filter = computed<FormatFilter>(() => filterFormats(this.formats(), this.appliedQuery()));
  /** The format to scroll to. */
  readonly reveal = signal<FormatReveal | null>(null);
  /** The format highlighted for a moment after a reveal. */
  readonly highlighted = signal<string | null>(null);
  /** The format marked in the contents list (the one revealed, or at the top of the panel). */
  readonly active = signal<string | null>(null);

  /** Fetches the definitions (the page did so each time the sub-tab was shown); a link waiting for them is revealed. */
  async fetch(): Promise<void> {
    const seq = ++this.loadSeq;
    this.load.set({ kind: 'loading' });
    let formats: FormatDefinition[];
    try {
      formats = await firstValueFrom(this.api.formatDefinitions());
    } catch (error) {
      if (seq === this.loadSeq) {
        this.load.set({
          kind: 'error',
          message: loadErrorMessage(ApiError.from(error).status, FORMAT_DEFINITIONS_SUBJECT),
        });
      }
      return;
    }
    if (seq !== this.loadSeq) {
      return;
    }
    this.load.set({ kind: 'ready', formats });
    if (this.pending !== null) {
      this.open(this.pending);
    }
  }

  /**
   * Brings a format into view (a `help/formats/<code>` link, the Play popup's (i), the contents list): a filter that
   * hides it is cleared first, it is marked in the contents list and highlighted for a moment. Before the formats
   * arrive it waits for them; a code no format has does nothing.
   */
  open(code: string): void {
    const format = findFormat(this.formats(), code);
    if (format === null) {
      this.pending = this.load().kind === 'ready' ? null : code;
      return;
    }
    this.pending = null;
    if (!this.filter().visible.has(format.code)) {
      this.clearFilter();
    }
    this.active.set(format.code);
    this.reveal.set({ code: format.code, seq: ++this.revealSeq });
    this.timers.clear(this.highlightTimer);
    this.highlighted.set(format.code);
    this.highlightTimer = this.timers.after(HIGHLIGHT_MS, () => {
      this.highlightTimer = null;
      this.highlighted.set(null);
    });
  }

  /** The format now at the top of the panel (the view's scroll-spy). */
  setActive(code: string): void {
    this.active.set(code);
  }

  /** The filter box changed: the formats are filtered a moment after the last key. */
  setQuery(value: string): void {
    this.query.set(value);
    this.timers.clear(this.filterTimer);
    this.filterTimer = this.timers.after(FILTER_DELAY_MS, () => {
      this.filterTimer = null;
      this.appliedQuery.set(value);
    });
  }

  clearFilter(): void {
    this.timers.clear(this.filterTimer);
    this.filterTimer = null;
    this.query.set('');
    this.appliedQuery.set('');
  }

  /** "Card images": remembered on the device (when storage allows; else until the page reloads). */
  setImages(on: boolean): void {
    this.images.set(on);
    this.settings.write(FORMAT_IMAGES_KEY, on ? '1' : '0');
  }
}
