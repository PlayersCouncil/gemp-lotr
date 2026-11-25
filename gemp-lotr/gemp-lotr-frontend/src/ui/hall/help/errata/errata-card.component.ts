import { Component, computed, ElementRef, inject, input, linkedSignal, output } from '@angular/core';

/**
 * A card image in the PC Errata viewer, drawn as the game and the deck builder draw cards (Card.CreateSimpleCardDiv:
 * rounded corners and a black border over the scan's edge, which hides its white corners), at the size the frame's
 * layout gives it (`--gd-errata-card-width`, `--gd-errata-card-height`, `--gd-errata-card-border`). Without an image,
 * or when it fails to load, the placeholder text (PcErrataUI.card).
 *
 * DOM contract: `gd-errata-card.gd-errata-card` (`data-side` = before / after; `data-missing` without an image;
 * `data-aspect` and `data-natural-height` once the image has loaded, which the layout reads) holding either
 * `img.gd-errata-card__image` (`draggable=false`, `alt`) and `div.gd-errata-card__border`, or
 * `div.gd-errata-card__missing`.
 */
@Component({
  selector: 'gd-errata-card',
  styleUrl: './errata-card.component.css',
  host: {
    class: 'gd-errata-card',
    '[attr.data-side]': 'side()',
    '[attr.data-missing]': "showImage() ? null : ''",
  },
  template: `
    @if (showImage()) {
      <img
        class="gd-errata-card__image"
        draggable="false"
        [src]="url()"
        [alt]="alt()"
        (load)="loaded($event)"
        (error)="failed.set(true)"
      />
      <div class="gd-errata-card__border"></div>
    } @else {
      <div class="gd-errata-card__missing">{{ missing() }}</div>
    }
  `,
})
export class ErrataCardComponent {
  readonly url = input<string | null>(null);
  readonly side = input.required<'before' | 'after'>();
  readonly alt = input('');
  /** The placeholder's text ("No image" / "No image of the original"). */
  readonly missing = input('');
  /** The image loaded, and its shape is on the element: the frame fits itself again. */
  readonly sized = output();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly failed = linkedSignal({ source: this.url, computation: () => false });
  protected readonly showImage = computed(() => !!this.url() && !this.failed());

  protected loaded(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (image.naturalWidth && image.naturalHeight) {
      this.host.dataset['aspect'] = String(image.naturalWidth / image.naturalHeight);
      this.host.dataset['naturalHeight'] = String(image.naturalHeight);
      this.sized.emit();
    }
  }
}
