import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CardRef, CardRegistry } from '../cards/card-registry';
import { DeckService } from '../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../core/session/session.service';
import { CardClicksService } from './card-clicks.service';

describe('CardClicksService', () => {
  let clicks: CardClicksService;
  let registry: CardRegistry;
  let deck: {
    addFromCollection: ReturnType<typeof vi.fn>;
    removeSlotCard: ReturnType<typeof vi.fn>;
    removeDrawCard: ReturnType<typeof vi.fn>;
  };
  let info: CardRef[];
  let packs: CardRef[];

  beforeEach(() => {
    deck = { addFromCollection: vi.fn(), removeSlotCard: vi.fn(), removeDrawCard: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: DeckService, useValue: deck },
      ],
    });
    clicks = TestBed.inject(CardClicksService);
    registry = TestBed.inject(CardRegistry);
    info = [];
    packs = [];
    clicks.cardInfoRequested$.subscribe((ref) => info.push(ref));
    clicks.packClicked$.subscribe((ref) => packs.push(ref));
    TestBed.runInInjectionContext(() => clicks.listen());
  });

  afterEach(() => document.body.replaceChildren());

  /** A card element like gempCard renders, registered with `ref`; returns its click area. */
  function card(ref: CardRef): HTMLElement {
    const element = document.createElement('div');
    element.className = 'card';
    const area = document.createElement('div');
    area.className = 'borderOverlay actionArea';
    element.append(area);
    document.body.append(element);
    registry.set(element, ref);
    return area;
  }

  const collectionCard: CardRef = {
    kind: 'collection',
    role: 'cardInCollection',
    blueprintId: '1_12',
    item: { kind: 'card', blueprintId: '1_12', count: 4, side: 'FREE_PEOPLE', group: 'fp' },
  };
  const drawCard: CardRef = {
    kind: 'draw',
    blueprintId: '1_12',
    card: { id: 7, blueprintId: '1_12', side: 'FREE_PEOPLE' },
  };

  const fire = (target: Element, type: string, init: MouseEventInit = {}) => {
    const event = new MouseEvent(type, { bubbles: true, cancelable: true, ...init });
    target.dispatchEvent(event);
    return event;
  };

  it('adds a clicked collection card and removes a clicked deck card', () => {
    fire(card(collectionCard), 'click');
    expect(deck.addFromCollection).toHaveBeenCalledWith('1_12', 'FREE_PEOPLE');
    fire(card(drawCard), 'click');
    expect(deck.removeDrawCard).toHaveBeenCalledWith(7);
    fire(card({ kind: 'slot', slot: 'sites', blueprintId: '1_319', card: { id: 3, blueprintId: '1_319' } }), 'click');
    expect(deck.removeSlotCard).toHaveBeenCalledWith('sites', 3);
  });

  it('does nothing for copies behind a card, the Race card and placeholders', () => {
    fire(
      card({ kind: 'behind', blueprintId: '1_12', card: { id: 8, blueprintId: '1_12', side: 'FREE_PEOPLE' } }),
      'click',
    );
    fire(card({ kind: 'race', blueprintId: '90_7' }), 'click');
    fire(card({ ...collectionCard, role: 'placeholderInCollection' } as CardRef), 'click');
    expect(deck.addFromCollection).not.toHaveBeenCalled();
    expect(deck.removeDrawCard).not.toHaveBeenCalled();
  });

  it('passes pack clicks on for opening', () => {
    fire(card({ ...collectionCard, role: 'packInCollection' } as CardRef), 'click');
    expect(packs.length).toBe(1);
  });

  it('asks for card details on shift-click and right-click, without the context menu', () => {
    const area = card(collectionCard);
    fire(area, 'click', { shiftKey: true });
    const menu = fire(area, 'contextmenu', { button: 2 });
    expect(info).toEqual([collectionCard, collectionCard]);
    expect(menu.defaultPrevented).toBe(true);
    expect(deck.addFromCollection).not.toHaveBeenCalled();
    // Elsewhere the browser's menu stays
    expect(fire(document.body, 'contextmenu', { button: 2 }).defaultPrevented).toBe(false);
  });

  it('asks for card details when a card is dragged 20px up, and then ignores the click', () => {
    const area = card(collectionCard);
    expect(fire(area, 'mousedown', { button: 0, clientY: 100 }).defaultPrevented).toBe(true);
    fire(area, 'mouseup', { clientY: 80 });
    fire(area, 'click');
    expect(info).toEqual([collectionCard]);
    expect(deck.addFromCollection).not.toHaveBeenCalled();

    fire(area, 'mousedown', { button: 0, clientY: 100 });
    fire(area, 'mouseup', { clientY: 90 });
    fire(area, 'click');
    expect(deck.addFromCollection).toHaveBeenCalledTimes(1);
  });

  it('leaves links and other elements alone', () => {
    const link = document.createElement('a');
    document.body.append(link);
    expect(fire(link, 'click').defaultPrevented).toBe(false);
    expect(fire(document.body, 'click').defaultPrevented).toBe(false);
  });
});
