import { capturePointer } from '../pointer-drag/pointer-drag.directive';
import { Component, DestroyRef, ElementRef, inject, input } from '@angular/core';
import { CardActions } from '../../../core/cards/card-actions';
import { CardView } from '../../../core/cards/card-view';
import { CardHover } from '../../../core/cards/card-hover.service';
import { CardInfoService } from '../../../core/cards/card-info.service';

/** How long a touch must be held on a card to show its details. */
export const LONG_PRESS_MS = 500;
/** A touch that moves further than this (px) is a scroll, not a long press. */
const LONG_PRESS_SLOP = 10;
/** How far (px) a card has to be dragged upwards with the mouse to show its details (the legacy gesture). */
export const DRAG_UP_FOR_INFO = 20;

/**
 * A card: a native button with the card image and its layers. Using it (click, Enter, Space) does what the card is
 * for on the page (CardActions); asking for its details (shift-click, right-click or the context-menu key, a long press
 * on a touch screen, or dragging it upwards with the mouse) shows them (CardInfoService). The mouse coming onto it,
 * leaving it and pressing on it are reported to CardHover (for the hover zoom). The page provides these with
 * provideCardInteractions.
 *
 * DOM contract: `button.gd-card` with `data-role` (card, pack, selection, placeholder), `data-orientation`
 * (vertical, horizontal), and `data-errata`, `data-foil`, `data-borderless` when they apply, `data-tap-once` on a pack
 * (a double tap opens one: SecondTapGuard); inside,
 * `img.gd-card__image`, then `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay` and `.gd-card__count`
 * when they apply. Its group sizes it with `--gd-card-height` (from fitCards); it draws its border and layers.
 */
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector -- a real <button>, so a card is focusable and clickable
  selector: 'button[gdCard]',
  styleUrl: './card.component.css',
  host: {
    type: 'button',
    class: 'gd-card',
    '[attr.data-role]': 'card().role',
    '[attr.data-orientation]': "card().horizontal ? 'horizontal' : 'vertical'",
    '[attr.data-errata]': "card().errata ? '' : null",
    '[attr.data-foil]': "card().foilImage ? '' : null",
    '[attr.data-borderless]': "card().border ? null : ''",
    '[attr.data-tap-once]': "card().role === 'pack' ? '' : null",
    '[attr.aria-label]': 'label()',
    '(click)': 'onClick($event)',
    '(contextmenu)': 'onContextMenu($event)',
    '(mousedown)': 'onMouseDown($event)',
    '(pointerdown)': 'onPointerDown($event)',
    '(pointermove)': 'onPointerMove($event)',
    '(pointerup)': 'onPointerUp($event)',
    '(pointercancel)': 'cancelPress()',
    '(pointerenter)': 'onPointerEnter($event)',
    '(pointerleave)': 'onPointerLeave()',
    '(dragstart)': '$event.preventDefault()',
  },
  template: `
    <img class="gd-card__image" [src]="card().imageUrl" alt="" draggable="false" />
    @if (card().errata) {
      <span class="gd-card__errata" aria-hidden="true"></span>
    }
    @if (card().foilImage; as foil) {
      <img class="gd-card__foil" [src]="foil" alt="" draggable="false" />
    }
    @if (card().overlayImage; as overlay) {
      <img class="gd-card__overlay" [src]="overlay" alt="" draggable="false" />
    }
    @if (card().count !== null) {
      <span class="gd-card__count">{{ card().count }}</span>
    }
  `,
})
export class CardComponent {
  readonly card = input.required<CardView>({ alias: 'gdCard' });

  private readonly actions = inject(CardActions);
  private readonly info = inject(CardInfoService);
  private readonly hover = inject(CardHover);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private press: {
    x: number;
    y: number;
    mouse: boolean;
    timer: ReturnType<typeof setTimeout> | null;
  } | null = null;
  /** The details were just shown by a gesture: the click that ends it does nothing else. */
  private swallowClick = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.cancelPress();
      // A card removed while hovered (e.g. clicked out of the deck) is no longer under the mouse
      this.hover.leave(this);
    });
  }

  protected label(): string {
    const card = this.card();
    return card.count === null ? card.blueprintId : `${card.blueprintId} (${card.count})`;
  }

  protected onClick(event: MouseEvent): void {
    if (this.swallowClick) {
      this.swallowClick = false;
      event.preventDefault();
      return;
    }
    if (event.shiftKey) {
      this.info.show(this.card());
    } else {
      this.actions.activate(this.card().ref);
    }
  }

  protected onContextMenu(event: MouseEvent): void {
    event.preventDefault();
    if (this.swallowClick) {
      // The long press already showed the details (touch browsers also send contextmenu for it)
      return;
    }
    this.info.show(this.card());
  }

  protected onPointerEnter(event: PointerEvent): void {
    if (event.pointerType === 'mouse') {
      const { left, top, right, bottom } = this.host.getBoundingClientRect();
      this.hover.enter(this, this.card(), { left, top: this.snap(top), right, bottom: this.snap(bottom) });
    }
  }

  /**
   * Pressing a card with the mouse leaves the keyboard focus where it was, or with the other buttons takes it away from
   * what had it, as the legacy page's plain elements did. The keyboard still reaches the card with Tab.
   */
  protected onMouseDown(event: MouseEvent): void {
    event.preventDefault();
    if (event.button !== 0) {
      const active = this.host.ownerDocument.activeElement;
      if (active instanceof HTMLElement) {
        active.blur();
      }
    }
  }

  protected onPointerLeave(): void {
    this.hover.leave(this);
  }

  protected onPointerDown(event: PointerEvent): void {
    this.cancelPress();
    this.swallowClick = false;
    if (event.pointerType === 'mouse') {
      this.hover.press();
    }
    if (event.button !== 0) {
      return;
    }
    const mouse = event.pointerType === 'mouse';
    if (mouse) {
      // So the release comes here even when the card was dragged up and out of itself
      capturePointer(event.currentTarget as Element, event.pointerId);
    }
    this.press = {
      x: event.clientX,
      y: event.clientY,
      mouse,
      timer: mouse ? null : setTimeout(() => this.longPress(), LONG_PRESS_MS),
    };
  }

  protected onPointerMove(event: PointerEvent): void {
    const press = this.press;
    if (press?.timer && Math.hypot(event.clientX - press.x, event.clientY - press.y) > LONG_PRESS_SLOP) {
      // Scrolling, not pressing
      clearTimeout(press.timer);
      press.timer = null;
    }
  }

  protected onPointerUp(event: PointerEvent): void {
    const press = this.press;
    this.cancelPress();
    if (press?.mouse && press.y - event.clientY >= DRAG_UP_FOR_INFO) {
      this.swallowClick = true;
      this.info.showDragged(this.card());
    }
  }

  protected cancelPress(): void {
    if (this.press?.timer) {
      clearTimeout(this.press.timer);
    }
    this.press = null;
  }

  /** A card its group puts on whole pixels (nudging it by a fraction): the zoom is placed from those. */
  private snap(edge: number): number {
    return this.card().wholePixels ? Math.round(edge) : edge;
  }

  private longPress(): void {
    this.press = null;
    this.swallowClick = true;
    // The click that ends the press may land on the dialog, which may now be over the card
    this.info.showDragged(this.card());
  }
}

/** The card an event happened on, for other components that need to tell (the class is this component's own). */
export function cardElementOf(target: EventTarget | null): Element | null {
  return target instanceof Element ? target.closest('.gd-card') : null;
}
