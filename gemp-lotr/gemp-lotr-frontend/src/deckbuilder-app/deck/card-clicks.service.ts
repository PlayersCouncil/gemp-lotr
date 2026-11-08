import { DOCUMENT } from '@angular/common';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { CardRef } from '../../core/cards/card-ref';
import { CardRegistry } from '../cards/card-registry';
import { DeckbuilderActions } from '../../core/actions/deckbuilder-actions.service';
import { CardInfoService } from '../../core/cards/card-info.service';
import { CardInfoSliderState } from '../dialogs/card-info-slider.service';

/** How far (px) a card has to be dragged upwards to show its details. */
const DRAG_UP_FOR_INFO = 20;

/**
 * The page-wide card mouse handling of the legacy deckBuildingUi.js (its click, contextmenu, mousedown and mouseup
 * handlers on <body>), acting on the card's click area (".actionArea"); what each action does is DeckbuilderActions':
 *  - click activates the card (adds it to the deck, removes it, opens a pack, ...);
 *  - shift-click, right-click, or dragging a card 20px upwards shows the card's details;
 *  - while the details are shown, any click (except on a link) closes them;
 *  - pressing on a card doesn't select text or move the focus.
 */
@Injectable({ providedIn: 'root' })
export class CardClicksService {
  private readonly registry = inject(CardRegistry);
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cardInfo = inject(CardInfoService);
  private readonly actions = inject(DeckbuilderActions);
  private readonly slider = inject(CardInfoSliderState);

  private dragged: { ref: CardRef | null; startY: number } | null = null;
  private successfulDrag = false;

  /** Starts listening on the page (once, from the app). */
  listen(): void {
    const body = this.document.body;
    const click = (event: MouseEvent) => {
      if (!this.handleClick(event)) {
        event.preventDefault();
        event.stopPropagation();
      }
    };
    const contextMenu = (event: MouseEvent) => {
      if (!this.handleClick(event)) {
        event.preventDefault();
      }
    };
    const mouseDown = (event: MouseEvent) => this.mouseDown(event);
    const mouseUp = (event: MouseEvent) => this.mouseUp(event);
    body.addEventListener('click', click);
    body.addEventListener('contextmenu', contextMenu);
    body.addEventListener('mousedown', mouseDown);
    body.addEventListener('mouseup', mouseUp);
    this.destroyRef.onDestroy(() => {
      body.removeEventListener('click', click);
      body.removeEventListener('contextmenu', contextMenu);
      body.removeEventListener('mousedown', mouseDown);
      body.removeEventListener('mouseup', mouseUp);
    });
  }

  /** clickCardFunction: false when the event was used (the default action is then prevented). */
  handleClick(event: MouseEvent): boolean {
    const target = event.target as Element | null;
    if (!target || target.tagName === 'A') {
      return true;
    }
    if (!this.successfulDrag && this.cardInfo.isOpen()) {
      // CardInfoDialog.mouseUp: any click closes the dialog, unless its zoom slider was just used
      if (!this.slider.sliding) {
        this.cardInfo.close();
      }
      return false;
    }
    if (!target.classList.contains('actionArea')) {
      return true;
    }
    const ref = this.registry.get(target.closest('.card'));
    if (!this.successfulDrag && ref) {
      if (event.shiftKey || event.button > 0) {
        this.actions.showInfo(ref);
        return false;
      }
      this.actions.activate(ref);
    }
    return false;
  }

  private mouseDown(event: MouseEvent): void {
    this.successfulDrag = false;
    const target = event.target as Element | null;
    if (target?.classList.contains('actionArea') && event.button === 0) {
      this.dragged = { ref: this.registry.get(target.closest('.card')), startY: event.clientY };
      event.preventDefault();
    }
  }

  private mouseUp(event: MouseEvent): void {
    if (this.dragged) {
      if (this.dragged.startY - event.clientY >= DRAG_UP_FOR_INFO && this.dragged.ref) {
        this.actions.showInfo(this.dragged.ref);
        this.successfulDrag = true;
      }
      this.dragged = null;
      event.preventDefault();
    }
  }
}
