import {
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink, UrlMatchResult, UrlSegment } from '@angular/router';
import { map } from 'rxjs';
import { cardTooltip, formatFacts, formatLists, hasNoLists, navMeta } from '../../../core/hall/help/format-definitions';
import { FormatCard, FormatDefinition, FormatListKey } from '../../../core/hall/help/format-definitions-models';
import { FormatDefinitionsService } from '../../../core/hall/help/format-definitions.service';
import { isHit } from '../../../core/hall/help/format-filter';
import * as words from '../../../core/hall/help/help-messages';
import { errataRoute, formatRoute } from '../../../core/hall/help/help-routes';
import { Timers } from '../../../core/platform/timers';
import { CardComponent } from '../../shared/card/card.component';
import { HallCards } from '../shared/hall-cards';
import { HallShareButtonComponent } from '../shared/hall-share-button.component';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';

/** `formats` and `formats/<code>` are one route, so following the formats (the address changes) keeps the page. */
export function formatsMatcher(segments: UrlSegment[]): UrlMatchResult | null {
  if (segments[0]?.path !== 'formats' || segments.length > 2) {
    return null;
  }
  const code = segments[1];
  return { consumed: segments, posParams: code ? { code } : {} };
}

/** A revealed format stays the one followed while the panel scrolls to it (GempFormatDefinitions.PIN_MS). */
export const PIN_MS = 3000;
/** The format at the top of the panel is the one this far below its top edge (spy). */
const SPY_OFFSET = 40;

/**
 * Help › Format Definitions (formatRules.html, formatDefinitions.js): the intro with its (i), then a sticky list of the
 * formats (a find box, "Card images", the filter's status) beside one section per format: its name and badges, a
 * link to it and a Share button, its description and facts, its card lists as names grouped by set (or thumbnails),
 * and the R-list by title. The address follows the format at the top of the panel (`help/formats/<code>`, replaced);
 * an address naming a format brings it into view. A card's name or thumbnail shows the card (click or right-click).
 *
 * DOM contract: `gd-format-definitions.gd-formats` (`--gd-formats-view-height`, the panel's inner height) holding
 * `div.gd-formats__intro` (`p` with `button.gd-formats__info` (i) (`aria-expanded`), `div.gd-formats__help` with
 * `a.gd-formats__errata-link` to Help › PC Errata, plain text on the legacy page), then
 * `div.gd-formats__layout` with either `p.gd-formats__status` (loading, `data-error` for a failure) or
 * `aside.gd-formats__side` (`div.gd-formats__tools`: `input[type=search].gd-formats__filter`,
 * `label.gd-formats__images` with its checkbox, `div.gd-formats__filter-status` (`aria-live`); `ul.gd-formats__nav`
 * of `li` (`data-active`, `hidden`) with `a` (`span.gd-formats__nav-name`, `span.gd-formats__nav-meta`)) and
 * `div.gd-formats__main` of `section.gd-format` (`id` = its anchor, `data-format`, `data-highlight`, `hidden`):
 * `header.gd-format__head` (`h2.gd-format__name`, `span.gd-format__badge` (`data-badge` = pc, playtest),
 * `a.gd-format__permalink`, `gd-hall-share-button`), `p.gd-format__description`, `dl.gd-format__facts` (`dt`, `dd`;
 * `a.gd-format__errata`, `ul.gd-format__rules`), `details.gd-format__list` (`data-list`; `summary` with
 * `span.gd-format__list-title`, `span.gd-format__count`, `span.gd-format__list-note`; `div.gd-format__list-body`
 * (`data-images`) of `div.gd-format__set-row` (`span.gd-format__set`, `div.gd-format__chips` of
 * `button.gd-format__chip` (`data-hit`)) or of `div.gd-format__thumb` (`data-hit`, `data-orientation`) holding
 * `button[gdCard]` and `span.gd-format__thumb-name`), and `p.gd-format__no-lists`.
 */
@Component({
  selector: 'gd-format-definitions',
  styleUrl: './format-definitions.component.css',
  imports: [RouterLink, HallShareButtonComponent, CardComponent],
  host: { class: 'gd-formats', '[style.--gd-formats-view-height.px]': 'viewHeight()' },
  templateUrl: './format-definitions.component.html',
})
export class FormatDefinitionsComponent {
  protected readonly service = inject(FormatDefinitionsService);
  private readonly cards = inject(HallCards);
  private readonly frame = inject(HallTabFrameComponent);
  private readonly router = inject(Router);
  private readonly timers = inject(Timers);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly words = words;
  protected readonly helpShown = signal(false);
  protected readonly viewHeight = signal<number | null>(null);

  private readonly code = toSignal(inject(ActivatedRoute).paramMap.pipe(map((params) => params.get('code'))), {
    initialValue: null,
  });
  /** The code the page itself put in the address (following the reading): it asks for no reveal. */
  private followed: string | null = null;
  private pin: { code: string; until: number } | null = null;
  private spyFrame: number | null = null;

  protected readonly entries = computed(() =>
    this.service.formats().map((format) => ({
      format,
      facts: formatFacts(format),
      lists: formatLists(format),
      noLists: hasNoLists(format),
      meta: navMeta(format),
      route: formatRoute(format.code).path,
      errataLinks: errataLinksOf(format),
    })),
  );

  constructor() {
    void this.service.fetch();
    effect(() => {
      const code = this.code();
      untracked(() => {
        if (code !== null && code !== this.followed) {
          this.service.open(code);
        }
        this.followed = null;
      });
    });
    // a reveal brings its format to the top of the panel, smoothly, after it is drawn
    afterRenderEffect(() => {
      const reveal = this.service.reveal();
      if (reveal === null) return;
      const anchor = untracked(() => this.service.formats().find((format) => format.code === reveal.code)?.anchor);
      const section = anchor ? this.host.querySelector<HTMLElement>(`[id="${anchor}"]`) : null;
      this.pin = { code: reveal.code, until: this.timers.now() + PIN_MS };
      section?.scrollIntoView({ block: 'start', behavior: 'smooth' });
    });
    // the lists holding a card the filter found open (and stay open)
    afterRenderEffect(() => {
      this.service.filter();
      for (const list of Array.from(this.host.querySelectorAll<HTMLDetailsElement>('details[data-has-hits]'))) {
        list.open = true;
      }
    });
    this.watchScroller();
  }

  /** The contents list or a permalink: the format revealed (even when the address already names it). */
  protected go(code: string): void {
    if (code !== this.code()) {
      this.followed = code;
    }
    this.service.open(code);
  }

  protected toggleHelp(event: Event): void {
    event.preventDefault();
    this.helpShown.update((shown) => !shown);
  }

  protected visible(code: string): boolean {
    return this.service.filter().visible.has(code);
  }

  protected hit(code: string, list: FormatListKey, card: FormatCard): boolean {
    return isHit(this.service.filter(), code, list, card.id);
  }

  protected hasHits(code: string, list: FormatListKey): boolean {
    return (this.service.filter().hits.get(code)?.get(list)?.size ?? 0) > 0;
  }

  protected tooltip(card: FormatCard): string {
    return cardTooltip(card);
  }

  protected showCard(card: FormatCard, event?: Event): void {
    event?.preventDefault();
    this.cards.show(card.id, card.name);
  }

  protected cardView(card: FormatCard) {
    return this.cards.view(card.id, card.name);
  }

  protected filterInput(event: Event): void {
    this.service.setQuery((event.target as HTMLInputElement).value);
  }

  protected imagesChange(event: Event): void {
    this.service.setImages((event.target as HTMLInputElement).checked);
  }

  /** The panel's height for the sticky list, and the scroll-spy (GempFormatDefinitions.watchScroller). */
  private watchScroller(): void {
    const scroller = this.frame.scroller();
    const view = scroller.ownerDocument.defaultView;
    const fit = () => {
      const style = view?.getComputedStyle(scroller);
      const padding = style ? (parseFloat(style.paddingTop) || 0) + (parseFloat(style.paddingBottom) || 0) : 0;
      const height = scroller.clientHeight - padding;
      if (height > 0) this.viewHeight.set(height);
    };
    const observer = typeof ResizeObserver === 'function' ? new ResizeObserver(fit) : null;
    observer?.observe(scroller);
    const letGo = () => {
      this.pin = null;
    };
    const onScroll = () => {
      if (this.spyFrame !== null || !view) return;
      this.spyFrame = view.requestAnimationFrame(() => {
        this.spyFrame = null;
        this.spy();
      });
    };
    const handGestures = ['wheel', 'touchstart', 'keydown', 'mousedown'];
    for (const type of handGestures) scroller.addEventListener(type, letGo, { passive: true });
    scroller.addEventListener('scroll', onScroll, { passive: true });
    inject(DestroyRef).onDestroy(() => {
      observer?.disconnect();
      for (const type of handGestures) scroller.removeEventListener(type, letGo);
      scroller.removeEventListener('scroll', onScroll);
      if (this.spyFrame !== null) view?.cancelAnimationFrame(this.spyFrame);
    });
  }

  /**
   * The format at the top of the panel is marked in the list and put in the address, or the format just revealed,
   * while the panel scrolls to it and while it stays in view (GempFormatDefinitions.spy).
   */
  private spy(): void {
    const box = this.frame.scroller().getBoundingClientRect();
    let current: string | null = null;
    for (const section of Array.from(this.host.querySelectorAll<HTMLElement>('section.gd-format:not([hidden])'))) {
      if (section.getBoundingClientRect().bottom > box.top + SPY_OFFSET) {
        current = section.dataset['format'] ?? null;
        break;
      }
    }
    if (this.pin) {
      const pinned = this.host.querySelector<HTMLElement>(`section.gd-format[data-format="${this.pin.code}"]`);
      const rect = pinned && !pinned.hidden ? pinned.getBoundingClientRect() : null;
      const inView = rect !== null && rect.height > 0 && rect.top < box.bottom && rect.bottom > box.top;
      if (rect !== null && (this.timers.now() < this.pin.until || inView)) {
        current = this.pin.code;
      } else {
        this.pin = null;
      }
    }
    if (current === null) return;
    this.service.setActive(current);
    if (current !== this.code()) {
      this.followed = current;
      void this.router.navigateByUrl(formatRoute(current).path, { replaceUrl: true });
    }
  }
}

/** The facts' errata link's route (PC Errata filtered to the format, or all of it). */
function errataLinksOf(format: FormatDefinition): Record<string, string | null> {
  const route = errataRoute(format.errata.pc ? format.code : null);
  return { path: route.path, format: route.queryParams['format'] ?? null };
}
