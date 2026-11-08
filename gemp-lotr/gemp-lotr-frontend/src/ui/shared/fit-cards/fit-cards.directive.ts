import { DOCUMENT } from '@angular/common';
import { afterNextRender, DestroyRef, Directive, effect, ElementRef, inject, input, signal } from '@angular/core';
import { ThemeService } from '../../../core/platform/theme.service';
import { fitCardHeight, parseFit, parsePx } from './fit-cards';

/** Set on the host: the row height the cards should take (the theme sizes the cards with it). */
export const CARD_HEIGHT_PROPERTY = '--gd-card-height';

/**
 * Sizes a box of cards: whenever the box resizes or its cards change, works out the row height at which the cards fit
 * (fitCardHeight) and sets it on the box as `--gd-card-height`. The theme decides the rest, through custom properties
 * on the box: `--gd-card-max-height` (largest row height, default 200px), `--gd-card-gap` (space between cards, default
 * 5px) and `--gd-card-fit` (`contain`, the default; `rows`, the legacy way; or `none` for always the largest and scrolling). Read again when
 * the theme changes.
 *
 * Usage: `<div class="gd-collection__cards" [gdFitCards]="horizontalFlags">` with one flag per card.
 */
@Directive({ selector: '[gdFitCards]' })
export class FitCardsDirective {
  /** One entry per card in the box: whether it is a landscape card. */
  readonly gdFitCards = input.required<readonly boolean[]>();

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
        const box = entries[entries.length - 1]?.contentRect;
        if (box) {
          this.size.set({ width: box.width, height: box.height });
        }
      });
      observer.observe(this.host);
      destroyRef.onDestroy(() => observer.disconnect());
    });
    effect(() => this.apply());
  }

  private apply(): void {
    const size = this.size();
    const horizontal = this.gdFitCards();
    // Another theme may set other sizes
    this.theme();
    if (!size || !this.window) {
      return;
    }
    const style = this.window.getComputedStyle(this.host);
    const height = fitCardHeight({
      width: size.width,
      height: size.height,
      horizontal,
      gap: parsePx(style.getPropertyValue('--gd-card-gap'), 5),
      maxHeight: parsePx(style.getPropertyValue('--gd-card-max-height'), 200),
      fit: parseFit(style.getPropertyValue('--gd-card-fit')),
    });
    this.host.style.setProperty(CARD_HEIGHT_PROPERTY, `${height}px`);
  }
}
