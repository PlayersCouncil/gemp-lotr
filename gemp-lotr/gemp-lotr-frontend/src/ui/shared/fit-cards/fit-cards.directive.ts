import { DOCUMENT } from '@angular/common';
import {
  afterNextRender,
  afterRenderEffect,
  DestroyRef,
  Directive,
  ElementRef,
  inject,
  input,
  signal,
} from '@angular/core';
import { ThemeService } from '../../../core/platform/theme.service';
import { FitCard, fitCards, parseFit, parsePx, rowSnaps } from './fit-cards';

/** Set on the host: the row height the cards should take (the theme sizes the cards with it). */
export const CARD_HEIGHT_PROPERTY = '--gd-card-height';
/** Set on the host: the space above the rows when they are centred (see CardFitResult.offset); a theme may use it. */
const CARD_OFFSET_PROPERTY = '--gd-card-offset';
/** Set on each card of the box with `rows`: how far its row moves to start on a whole pixel. */
const CARD_SNAP_PROPERTY = '--gd-card-snap';

/**
 * Sizes a box of cards: whenever the box resizes or its cards change, works out the row height at which the cards fit
 * (fitCards) and sets it on the box as `--gd-card-height`, with `--gd-card-offset` (the space above the rows when
 * centred, as the legacy groups placed them). The theme decides the rest, through custom properties
 * on the box: `--gd-card-max-height` (largest row height, default 200px), `--gd-card-gap` (space between cards, default
 * 5px) and `--gd-card-fit` (`contain`, the default; `rows`, the classic layout; or `none` for always the largest and
 * scrolling). Read again when the theme changes. `gdFitCardsScale` scales the largest row height (default 1).
 *
 * Usage: `<div class="gd-collection__cards" [gdFitCards]="cards">` with one FitCard per card in the box (whether it
 * is landscape, and the copies drawn behind it).
 */
@Directive({ selector: '[gdFitCards]' })
export class FitCardsDirective {
  /** One entry per card (or stack of copies) in the box. */
  readonly gdFitCards = input.required<readonly FitCard[]>();
  /** Scales the theme's largest row height (`--gd-card-max-height`): a card size the player chose. */
  readonly gdFitCardsScale = input(1);

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly window = inject(DOCUMENT).defaultView;
  private readonly size = signal<{ width: number; height: number } | null>(null);
  private readonly theme = inject(ThemeService).theme;

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const ResizeObserverClass = this.window?.ResizeObserver;
      if (!ResizeObserverClass) {
        return;
      }
      const observer = new ResizeObserverClass((entries) => {
        const box = entries.at(-1)?.contentRect;
        if (box) {
          this.size.set({ width: box.width, height: box.height });
        }
      });
      observer.observe(this.host);
      destroyRef.onDestroy(() => observer.disconnect());
    });
    // After rendering: the cards' own positions are set on the elements, which must be there
    afterRenderEffect({ write: () => this.apply() });
  }

  private apply(): void {
    const size = this.size();
    const cards = this.gdFitCards();
    const scale = this.gdFitCardsScale();
    // Another theme may set other sizes
    this.theme();
    if (!size || !this.window) {
      return;
    }
    const style = this.window.getComputedStyle(this.host);
    const gap = parsePx(style.getPropertyValue('--gd-card-gap'), 5);
    const fit = parseFit(style.getPropertyValue('--gd-card-fit'));
    const { height, offset } = fitCards({
      width: size.width,
      height: size.height,
      cards,
      gap,
      maxHeight: parsePx(style.getPropertyValue('--gd-card-max-height'), 200) * scale,
      fit,
    });
    this.host.style.setProperty(CARD_HEIGHT_PROPERTY, `${height}px`);
    this.host.style.setProperty(CARD_OFFSET_PROPERTY, `${offset}px`);
    const snaps = fit === 'rows' ? rowSnaps(cards, height, size.width, gap) : [];
    Array.from(this.host.children).forEach((child, i) =>
      (child as HTMLElement).style.setProperty(CARD_SNAP_PROPERTY, `${snaps[i] ?? 0}px`),
    );
  }
}
