import { Component, DestroyRef, ElementRef, inject, input } from '@angular/core';
import { DeckbuilderActions } from '../../../core/actions/deckbuilder-actions.service';
import { CardView } from '../../../core/cards/card-view';
import { CardHover } from './card-hover';

/** How long a touch must be held on a card to show its details. */
export const LONG_PRESS_MS = 500;
/** A touch that moves further than this (px) is a scroll, not a long press. */
export const LONG_PRESS_SLOP = 10;
/** How far (px) a card has to be dragged upwards with the mouse to show its details (the legacy gesture). */
export const DRAG_UP_FOR_INFO = 20;

/**
 * A card: a native button with the card image and its layers. Using it (click, Enter, Space) does what the card is
 * for (DeckbuilderActions.activate: add it to the deck, open the pack, ...); asking for its details (shift-click,
 * right-click or the context-menu key, a long press on a touch screen, or dragging it upwards with the mouse) shows
 * them (DeckbuilderActions.showInfo). The mouse coming onto it, leaving it and pressing on it are reported to CardHover
 * (for the hover zoom).
 *
 * DOM contract: `button.gd-card` with `data-role` (card, pack, selection, placeholder), `data-orientation`
 * (vertical, horizontal), and `data-errata`, `data-foil`, `data-borderless` when they apply; inside,
 * `img.gd-card__image`, then `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay` and `.gd-card__count`
 * when they apply, and always `.gd-card__info`: an ⓘ spot (part of the button, not a button of its own) that a theme
 * may show; a click on it shows the details instead of using the card. The theme sizes it (with `--gd-card-height` from fitCards) and draws the border and layers.
 */
@Component({
  selector: 'button[gdCard]',
  host: {
    type: 'button',
    class: 'gd-card',
    '[attr.data-role]': 'card().role',
    '[attr.data-orientation]': "card().horizontal ? 'horizontal' : 'vertical'",
    '[attr.data-errata]': "card().errata ? '' : null",
    '[attr.data-foil]': "card().foilImage ? '' : null",
    '[attr.data-borderless]': "card().border ? null : ''",
    '[attr.aria-label]': 'label()',
    '(click)': 'onClick($event)',
    '(contextmenu)': 'onContextMenu($event)',
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
    <span class="gd-card__info" aria-hidden="true" title="Card details"></span>
  `,
})
export class CardComponent {
  readonly card = input.required<CardView>({ alias: 'gdCard' });

  private readonly actions = inject(DeckbuilderActions);
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
  /**
   * The press started on the ⓘ spot. Kept from the press, since a mouse press captures the pointer and its click then
   * targets the card itself.
   */
  private pressOnInfo = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.cancelPress();
      // A card removed while hovered (e.g. clicked out of the deck) is no longer under the mouse
      this.hover.leave(this);
    });
  }

  protected label(): string {
    const card = this.card();
    return card.count === null ? card.ref.blueprintId : `${card.ref.blueprintId} (${card.count})`;
  }

  protected onClick(event: MouseEvent): void {
    const onInfo = this.pressOnInfo || isInfoSpot(event.target);
    this.pressOnInfo = false;
    if (this.swallowClick) {
      this.swallowClick = false;
      event.preventDefault();
      return;
    }
    if (event.shiftKey || onInfo) {
      this.actions.showInfo(this.card().ref);
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
    this.actions.showInfo(this.card().ref);
  }

  protected onPointerEnter(event: PointerEvent): void {
    if (event.pointerType === 'mouse') {
      const { left, top, right, bottom } = this.host.getBoundingClientRect();
      this.hover.enter(this, this.card(), { left, top, right, bottom });
    }
  }

  protected onPointerLeave(): void {
    this.hover.leave(this);
  }

  protected onPointerDown(event: PointerEvent): void {
    this.cancelPress();
    this.swallowClick = false;
    this.pressOnInfo = isInfoSpot(event.target);
    if (event.pointerType === 'mouse') {
      this.hover.press();
    }
    if (event.button !== 0) {
      return;
    }
    const mouse = event.pointerType === 'mouse';
    if (mouse) {
      // So the release comes here even when the card was dragged up and out of itself
      (event.currentTarget as Element).setPointerCapture?.(event.pointerId);
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
      this.actions.showInfo(this.card().ref);
    }
  }

  protected cancelPress(): void {
    if (this.press?.timer) {
      clearTimeout(this.press.timer);
    }
    this.press = null;
  }

  private longPress(): void {
    this.press = null;
    this.swallowClick = true;
    this.actions.showInfo(this.card().ref);
  }
}

function isInfoSpot(target: EventTarget | null): boolean {
  return target instanceof Element && target.closest('.gd-card__info') !== null;
}
