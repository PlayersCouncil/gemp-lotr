import { DestroyRef, Directive, ElementRef, inject, input } from '@angular/core';
import { CardHover } from '../../../../core/cards/card-hover.service';
import { CardViews } from '../../../../core/cards/card-views.service';

/**
 * A card named in the panel (by its blueprint, `gdCardHover="1_72"`) shown large while the mouse rests on it, as the
 * page's hover zoom shows the table's cards (the legacy page's AutoZoom did it for every `.cardHint`). Clicking one
 * that is a `.cardHint` opens its information: the page's `gdCardHints`, on whatever holds it. Without the page's
 * hover zoom (a test) it does nothing.
 *
 * DOM contract: none of its own.
 */
@Directive({
  selector: '[gdCardHover]',
  host: {
    '(pointerenter)': 'entered($event)',
    '(pointerleave)': 'left()',
    '(pointerdown)': 'pressed()',
  },
})
export class CardHoverDirective {
  readonly blueprintId = input.required<string>({ alias: 'gdCardHover' });

  private readonly hover = inject(CardHover, { optional: true });
  private readonly views = inject(CardViews, { optional: true });
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.left());
  }

  protected entered(event: PointerEvent): void {
    if (event.pointerType !== 'mouse' || !this.hover || !this.views || this.blueprintId() === '') return;
    const { left, top, right, bottom } = this.host.getBoundingClientRect();
    const card = this.views.create({ ref: { hint: true }, blueprintId: this.blueprintId(), zone: 'SPECIAL' });
    this.hover.enter(this, card, { left, top, right, bottom });
  }

  protected left(): void {
    this.hover?.leave(this);
  }

  protected pressed(): void {
    this.hover?.press();
  }
}
