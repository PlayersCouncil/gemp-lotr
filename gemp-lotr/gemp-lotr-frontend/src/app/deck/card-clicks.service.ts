import { DOCUMENT } from '@angular/common';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { CardRef, CardRegistry } from '../cards/card-registry';
import { DeckService } from '../core/deck/deck.service';

/** How far (px) a card has to be dragged upwards to show its details. */
const DRAG_UP_FOR_INFO = 20;

/**
 * The page-wide card mouse handling of the legacy deckBuildingUi.js (its click, contextmenu, mousedown and mouseup
 * handlers on <body>), acting on the card's click area (".actionArea"):
 *  - click on a collection card adds it to the deck; on a deck card removes it (one copy);
 *  - shift-click, right-click, or dragging a card 20px upwards asks for the card's details (shown from Phase 6);
 *  - pressing on a card doesn't select text or move the focus.
 */
@Injectable({ providedIn: 'root' })
export class CardClicksService {
  private readonly registry = inject(CardRegistry);
  private readonly deck = inject(DeckService);
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cardInfo = new Subject<CardRef>();
  private readonly packClicks = new Subject<CardRef & { kind: 'collection' }>();

  /** A card's details were asked for (card info dialog, Phase 6). */
  readonly cardInfoRequested$ = this.cardInfo.asObservable();
  /** A pack or selection pack in the collection was clicked (opening packs, Phase 6). */
  readonly packClicked$ = this.packClicks.asObservable();

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
    if (!target.classList.contains('actionArea')) {
      return true;
    }
    const ref = this.registry.get(target.closest('.card'));
    if (!this.successfulDrag && ref) {
      if (event.shiftKey || event.button > 0) {
        this.cardInfo.next(ref);
        return false;
      }
      this.act(ref);
    }
    return false;
  }

  private act(ref: CardRef): void {
    switch (ref.kind) {
      case 'collection':
        if (ref.role === 'cardInCollection') {
          this.deck.addFromCollection(ref.blueprintId, ref.item.kind === 'card' ? ref.item.side : null);
        } else if (ref.role === 'packInCollection' || ref.role === 'selectionInCollection') {
          this.packClicks.next(ref);
        }
        break;
      case 'slot':
        this.deck.removeSlotCard(ref.slot, ref.card.id);
        break;
      case 'draw':
        this.deck.removeDrawCard(ref.card.id);
        break;
      default:
      // Copies behind a card and the Race card do nothing
    }
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
        this.cardInfo.next(this.dragged.ref);
        this.successfulDrag = true;
      }
      this.dragged = null;
      event.preventDefault();
    }
  }
}
