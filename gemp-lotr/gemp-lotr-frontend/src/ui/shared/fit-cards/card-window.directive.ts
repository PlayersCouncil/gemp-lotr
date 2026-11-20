import { DOCUMENT } from '@angular/common';
import { afterNextRender, afterRenderEffect, DestroyRef, Directive, ElementRef, inject, input } from '@angular/core';
import { cardWindowSize } from './card-window';
import { pageRight } from '../page-room';
import { FitCard, parsePx } from './fit-cards';

/**
 * Set on the dialog it sizes (its stylesheet then takes `--dialog-width`/`--dialog-height` as they are, centred), and
 * on the box while its cards are laid out at the planned height (its stylesheet may put them at its top: the room kept
 * for buttons to come is then below them).
 */
export const CARD_WINDOW_ATTRIBUTE = 'data-card-window';
/** What the window's cards' box gets beyond the plan, against fractions of a pixel in the window's own sizes. */
const SLACK = 1;

/**
 * Sizes the dialog a box of cards is in to its cards (cardWindowSize): when the dialog opens, and when its cards change
 * while the player hasn't moved or resized it, it gets the size at which its cards are as large as allowed in as few
 * rows as fit, centred in the room (the window less `--gd-page-right`); the box is then told to lay its cards out at
 * that height (`--gd-card-max-height`, `--gd-card-fit: contain`, `--gd-card-label`, `data-card-window`: the box also has gdFitCards), so a
 * player who makes the dialog smaller makes them smaller. Moved or resized by the player, the dialog keeps what they
 * did. Not in the compact layout, whose dialogs fit the screen.
 *
 * Usage, on the fitCards box inside a `gd-dialog`: `[gdCardWindow]="cards"` (null: the dialog keeps the size its
 * stylesheet gives it), `[gdCardWindowLabel]` (room for a line of text under each row, px) and
 * `[gdCardWindowActions]` (room kept for the dialog's row of buttons while it has none yet, px).
 */
@Directive({ selector: '[gdCardWindow]' })
export class CardWindowDirective {
  readonly gdCardWindow = input.required<readonly FitCard[] | null>();
  readonly gdCardWindowLabel = input(0);
  readonly gdCardWindowActions = input(0);

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly document = inject(DOCUMENT);
  /** What the dialog was sized for since it opened (null: not yet). */
  private planned: string | null = null;

  constructor() {
    const destroyRef = inject(DestroyRef);
    // The box gets its size once its dialog opens, and none once it closes
    afterNextRender(() => {
      const view = this.document.defaultView;
      const Observer = view?.ResizeObserver;
      if (!view || !Observer) return;
      // In the next frame: resizing the dialog from the observer would resize the box it observes (and fitCards' too)
      // within the same frame, which the browser reports as an error
      let frame: number | null = null;
      const observer = new Observer(() => {
        frame ??= view.requestAnimationFrame(() => {
          frame = null;
          this.apply();
        });
      });
      observer.observe(this.host);
      destroyRef.onDestroy(() => {
        observer.disconnect();
        if (frame !== null) view.cancelAnimationFrame(frame);
      });
    });
    afterRenderEffect({ write: () => this.apply() });
  }

  private apply(): void {
    const cards = this.gdCardWindow();
    const label = this.gdCardWindowLabel();
    const actions = this.gdCardWindowActions();
    const view = this.document.defaultView;
    const dialog = this.host.closest('dialog');
    if (!view || !dialog) return;
    if (cards === null || dialog.closest('[data-layout="compact"]')) {
      this.release(dialog);
      return;
    }
    const box = this.host.getBoundingClientRect();
    if (!dialog.open || box.width === 0 || box.height === 0) {
      this.planned = null;
      return;
    }
    const key = JSON.stringify([cards, label]);
    if (this.planned !== null) {
      const touched = dialog.hasAttribute('data-placed') || dialog.style.width !== '' || dialog.style.height !== '';
      if (key === this.planned || touched) return;
    } else {
      // Opened afresh: a size the player gave it before goes too
      dialog.style.removeProperty('width');
      dialog.style.removeProperty('height');
    }
    this.planned = key;

    const outer = dialog.getBoundingClientRect();
    const style = view.getComputedStyle(dialog);
    const noActions = dialog.querySelector('[gdDialogActions]') === null;
    const size = cardWindowSize({
      cards,
      gap: parsePx(view.getComputedStyle(this.host).getPropertyValue('--gd-card-gap'), 5),
      label,
      room: { width: view.innerWidth - pageRight(this.document), height: view.innerHeight },
      chrome: { width: outer.width - box.width, height: outer.height - box.height + (noActions ? actions : 0) },
    });
    if (size.cardHeight === null) {
      this.unsetCards();
    } else {
      this.host.style.setProperty('--gd-card-max-height', `${size.cardHeight}px`);
      this.host.style.setProperty('--gd-card-fit', 'contain');
      this.host.style.setProperty('--gd-card-label', `${label}px`);
      this.host.setAttribute(CARD_WINDOW_ATTRIBUTE, '');
    }
    // The dialog's own width and height (its content box) grow by what the cards' box lacks
    const width = parsePx(style.width, 0) + size.width + SLACK - box.width;
    const height = parsePx(style.height, 0) + size.height + SLACK - box.height + (noActions ? actions : 0);
    dialog.style.setProperty('--dialog-width', `${width}px`);
    dialog.style.setProperty('--dialog-height', `${height}px`);
    dialog.setAttribute(CARD_WINDOW_ATTRIBUTE, '');
  }

  private unsetCards(): void {
    for (const property of ['--gd-card-max-height', '--gd-card-fit', '--gd-card-label']) {
      this.host.style.removeProperty(property);
    }
    this.host.removeAttribute(CARD_WINDOW_ATTRIBUTE);
  }

  /** Back to the size its stylesheet gives it. */
  private release(dialog: HTMLElement): void {
    if (!dialog.hasAttribute(CARD_WINDOW_ATTRIBUTE)) return;
    dialog.removeAttribute(CARD_WINDOW_ATTRIBUTE);
    dialog.style.removeProperty('--dialog-width');
    dialog.style.removeProperty('--dialog-height');
    this.unsetCards();
    this.planned = null;
  }
}
