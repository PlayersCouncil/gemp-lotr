import {
  afterRenderEffect,
  Component,
  computed,
  ElementRef,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, UrlMatchResult, UrlSegment } from '@angular/router';
import { map } from 'rxjs';
import { monthName } from '../../../core/hall/month-keys';
import * as words from '../../../core/hall/info/info-messages';
import { patchNotesRoute } from '../../../core/hall/info/info-routes';
import { parsePatchNotesSlug } from '../../../core/hall/info/patch-notes';
import { PatchNote } from '../../../core/hall/info/patch-notes-models';
import { PatchNotesService } from '../../../core/hall/info/patch-notes.service';
import { HallShareButtonComponent } from '../shared/hall-share-button.component';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';
import { PatchNotePicture, PatchNoteTextComponent } from './patch-note-text.component';

/** `patch-notes` and `patch-notes/<slug>` are one route, so the page stays while the address follows the window. */
export function patchNotesMatcher(segments: UrlSegment[]): UrlMatchResult | null {
  if (segments[0]?.path !== 'patch-notes' || segments.length > 2) {
    return null;
  }
  const slug = segments[1];
  return { consumed: segments, posParams: slug ? { slug } : {} };
}

/**
 * Server Info › Patch Notes (patchNotes.html, patchNotesUi.js): the heading, the tag filter's chips, the month
 * navigation (‹ the month's name with its dropdown ›), the status line, notices, the updates (title or date, the date,
 * tags, Share; the summary and the body), Newer / Older, and a lightbox for an enlarged screenshot. The address follows
 * the window shown (`info/patch-notes[/<slug>]`, replaced); an address naming an update shows the updates from it.
 *
 * DOM contract: `gd-patch-notes.gd-notes` (`aria-busy`) holding `h1.gd-notes__heading`, `div.gd-notes__filters`
 * (`role=radiogroup`) of `button.gd-notes__filter` (`role=radio`, `aria-checked`; `span.gd-notes__filter-label`,
 * `span.gd-notes__filter-count`), `div.gd-notes__months` (`button.gd-notes__month-step` (`data-step` = older,
 * newer), `span.gd-notes__month-wrap` with `button.gd-notes__month-title` (`aria-expanded`) and, open,
 * `div.gd-notes__dropdown` (`div.gd-notes__dropdown-status`, `button` Retry, `div.gd-notes__year` with
 * `div.gd-notes__year-label` and `button.gd-notes__month` (`data-selected`))), `div.gd-notes__status` (`role=status`;
 * `data-error`, `button.gd-notes__retry`), `p.gd-notes__notice`s, `p.gd-notes__empty`, `article.gd-note`s (`id`
 * patchnote-<slug>, `data-slug`, `data-kind`, `data-target`): `header.gd-note__head` (`h2.gd-note__title`,
 * `time.gd-note__date`, `span.gd-note__tags` of `span.gd-note__tag`, `gd-hall-share-button`), and
 * `gd-patch-note-text`s (summary, body); `div.gd-notes__pager` (`button.gd-notes__newer`, `span.gd-notes__range`,
 * `button.gd-notes__older`); and `div.gd-notes__lightbox` (`role=dialog`, `aria-modal`) with
 * `figure.gd-notes__lightbox-figure` (`img`, `figcaption`) and `button.gd-notes__lightbox-close`.
 */
@Component({
  selector: 'gd-patch-notes',
  styleUrl: './patch-notes.component.css',
  imports: [HallShareButtonComponent, PatchNoteTextComponent],
  host: {
    class: 'gd-notes',
    '[attr.aria-busy]': "notes.status().kind === 'loading'",
    '(document:keydown.escape)': 'closeLightbox()',
    '(document:mousedown)': 'outside($event)',
  },
  templateUrl: './patch-notes.component.html',
})
export class PatchNotesComponent {
  protected readonly notes = inject(PatchNotesService);
  private readonly router = inject(Router);
  private readonly frame = inject(HallTabFrameComponent);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly words = words;
  protected readonly monthsOpen = signal(false);
  protected readonly lightbox = signal<PatchNotePicture | null>(null);
  private readonly closeButton = viewChild<ElementRef<HTMLButtonElement>>('close');

  private readonly slug = toSignal(
    inject(ActivatedRoute).paramMap.pipe(map((params) => parsePatchNotesSlug(params.get('slug')))),
    { initialValue: null },
  );
  private first = true;

  /** The dropdown's months by year, newest first (MonthNav.renderList). */
  protected readonly years = computed(() => {
    const list = this.notes.monthList();
    if (list.kind !== 'ready') return [];
    const years: { year: string; months: string[] }[] = [];
    for (const key of list.months) {
      const year = key.split('-')[0] ?? key;
      let entry = years.find((candidate) => candidate.year === year);
      if (!entry) {
        entry = { year, months: [] };
        years.push(entry);
      }
      entry.months.push(key);
    }
    return years;
  });

  constructor() {
    // the address: a link shows the updates from it; the sub-tab opened shows what was left
    afterRenderEffect(() => {
      const slug = this.slug();
      untracked(() => {
        const link = !this.first || slug !== null;
        this.first = false;
        void this.notes.show(slug, link);
      });
    });
    // the page keeps the address on what it shows (replaced: no history entries)
    afterRenderEffect(() => {
      const slug = this.notes.routeSlug();
      const page = this.notes.page();
      untracked(() => {
        if (page !== null && slug !== this.slug()) {
          void this.router.navigateByUrl(patchNotesRoute(slug).path, { replaceUrl: true });
        }
      });
    });
    // the page's top: the filter and months stay in view above the update a link points to, which is first
    afterRenderEffect(() => {
      if (this.notes.scrollRequest() > 0) {
        const scroller = this.frame.scroller();
        const top = this.host.getBoundingClientRect().top - scroller.getBoundingClientRect().top + scroller.scrollTop;
        scroller.scrollTop = Math.max(0, top);
      }
    });
    afterRenderEffect(() => {
      if (this.lightbox()) this.closeButton()?.nativeElement.focus();
    });
  }

  protected monthName = monthName;

  protected shareLabel(note: PatchNote): string {
    return words.noteShareLabel(note.kind === 'announcement', note.date);
  }

  protected toggleMonths(): void {
    const open = !this.monthsOpen();
    this.monthsOpen.set(open);
    if (open) this.notes.openMonths();
  }

  protected pickMonth(key: string): void {
    this.monthsOpen.set(false);
    void this.notes.showMonth(key);
  }

  protected titleKey(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggleMonths();
    } else if (event.key === 'Escape') {
      this.monthsOpen.set(false);
    }
  }

  /** A press outside the month title and its dropdown closes the dropdown. */
  protected outside(event: MouseEvent): void {
    if (this.monthsOpen() && !(event.target as HTMLElement | null)?.closest('.gd-notes__month-wrap')) {
      this.monthsOpen.set(false);
    }
  }

  protected openPicture(picture: PatchNotePicture): void {
    this.lightbox.set(picture);
  }

  /** A click on the overlay but not on the picture, the close button or Esc closes it; the focus goes back. */
  protected lightboxClick(event: MouseEvent): void {
    if ((event.target as HTMLElement | null)?.tagName !== 'IMG') this.closeLightbox();
  }

  protected closeLightbox(): void {
    const shown = this.lightbox();
    if (!shown) return;
    this.lightbox.set(null);
    if (shown.from.isConnected) shown.from.focus();
  }
}
