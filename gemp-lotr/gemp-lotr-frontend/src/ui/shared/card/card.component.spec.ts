import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DeckbuilderActions } from '../../../core/actions/deckbuilder-actions.service';
import { CardView } from '../../../core/cards/card-view';
import { CardComponent, DRAG_UP_FOR_INFO, LONG_PRESS_MS } from './card.component';

const CARD: CardView = {
  ref: { kind: 'shown', blueprintId: '1_2', zone: 'COLLECTION', role: '' },
  role: 'card',
  imageUrl: 'https://i.lotrtcgpc.net/decipher/LOTR01002.jpg',
  horizontal: false,
  errata: false,
  foilImage: null,
  border: true,
  count: 3,
  overlayImage: null,
};

@Component({
  imports: [CardComponent],
  template: `<button [gdCard]="card()"></button>`,
})
class Host {
  readonly card = signal<CardView>(CARD);
}

/** jsdom has no PointerEvent: a MouseEvent with the pointer fields. */
function pointer(type: string, init: { x?: number; y?: number; pointerType?: string; button?: number } = {}): Event {
  const event = new MouseEvent(type, {
    bubbles: true,
    clientX: init.x ?? 0,
    clientY: init.y ?? 0,
    button: init.button ?? 0,
  });
  Object.assign(event, { pointerType: init.pointerType ?? 'mouse', pointerId: 1 });
  return event;
}

describe('CardComponent', () => {
  let actions: { activate: ReturnType<typeof vi.fn>; showInfo: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    actions = { activate: vi.fn(), showInfo: vi.fn() };
    TestBed.configureTestingModule({ providers: [{ provide: DeckbuilderActions, useValue: actions }] });
  });

  afterEach(() => vi.useRealTimers());

  async function render(card: Partial<CardView> = {}) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.card.set({ ...CARD, ...card });
    await fixture.whenStable();
    const el = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button')!;
    return { fixture, el };
  }

  describe('DOM contract', () => {
    it('is a native button with the card image, its role, orientation and count', async () => {
      const { el } = await render();
      expect(el.type).toBe('button');
      expect(el.className).toBe('gd-card');
      expect(el.dataset['role']).toBe('card');
      expect(el.dataset['orientation']).toBe('vertical');
      expect(el.querySelector<HTMLImageElement>('img.gd-card__image')!.src).toBe(CARD.imageUrl);
      expect(el.querySelector('.gd-card__count')!.textContent).toBe('3');
      expect(el.getAttribute('aria-label')).toBe('1_2 (3)');
      for (const attribute of ['data-errata', 'data-foil', 'data-borderless']) {
        expect(el.hasAttribute(attribute), attribute).toBe(false);
      }
      expect(el.querySelector('.gd-card__errata, .gd-card__foil, .gd-card__overlay')).toBeNull();
      // No visual styles: the theme draws it
      expect(el.getAttribute('style')).toBeNull();
    });

    it('shows the layers that apply', async () => {
      const { el } = await render({
        role: 'pack',
        horizontal: true,
        errata: true,
        foilImage: '/gemp-lotr/images/holo.jpg',
        border: false,
        count: null,
        overlayImage: 'https://example.com/modifier.jpg',
      });
      expect(el.dataset['role']).toBe('pack');
      expect(el.dataset['orientation']).toBe('horizontal');
      expect(el.hasAttribute('data-errata')).toBe(true);
      expect(el.hasAttribute('data-foil')).toBe(true);
      expect(el.hasAttribute('data-borderless')).toBe(true);
      expect(el.querySelector('.gd-card__errata')).not.toBeNull();
      expect(el.querySelector<HTMLImageElement>('.gd-card__foil')!.src).toContain('holo.jpg');
      expect(el.querySelector<HTMLImageElement>('.gd-card__overlay')!.src).toBe('https://example.com/modifier.jpg');
      expect(el.querySelector('.gd-card__count')).toBeNull();
      expect(el.getAttribute('aria-label')).toBe('1_2');
    });
  });

  describe('gestures', () => {
    it('click (or Enter/Space, which click a button) activates the card', async () => {
      const { el } = await render();
      el.click();
      expect(actions.activate).toHaveBeenCalledWith(CARD.ref);
      expect(actions.showInfo).not.toHaveBeenCalled();
    });

    it('shift-click shows the details', async () => {
      const { el } = await render();
      el.dispatchEvent(new MouseEvent('click', { bubbles: true, shiftKey: true }));
      expect(actions.showInfo).toHaveBeenCalledWith(CARD.ref);
      expect(actions.activate).not.toHaveBeenCalled();
    });

    it('right-click (or the context-menu key) shows the details, without the browser menu', async () => {
      const { el } = await render();
      const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
      el.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(true);
      expect(actions.showInfo).toHaveBeenCalledWith(CARD.ref);
    });

    it('a long press on a touch screen shows the details, and the click that ends it does nothing else', async () => {
      const { el } = await render();
      vi.useFakeTimers();
      el.dispatchEvent(pointer('pointerdown', { pointerType: 'touch', x: 50, y: 50 }));
      vi.advanceTimersByTime(LONG_PRESS_MS - 1);
      expect(actions.showInfo).not.toHaveBeenCalled();
      vi.advanceTimersByTime(1);
      expect(actions.showInfo).toHaveBeenCalledWith(CARD.ref);
      el.dispatchEvent(pointer('pointerup', { pointerType: 'touch', x: 50, y: 50 }));
      const contextMenu = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
      el.dispatchEvent(contextMenu);
      el.click();
      expect(actions.showInfo).toHaveBeenCalledTimes(1);
      expect(actions.activate).not.toHaveBeenCalled();
      // The next tap is a normal one again
      el.dispatchEvent(pointer('pointerdown', { pointerType: 'touch' }));
      el.dispatchEvent(pointer('pointerup', { pointerType: 'touch' }));
      el.click();
      expect(actions.activate).toHaveBeenCalledTimes(1);
    });

    it('a touch that moves (a scroll) or ends early is no long press', async () => {
      const { el } = await render();
      vi.useFakeTimers();
      el.dispatchEvent(pointer('pointerdown', { pointerType: 'touch', x: 50, y: 50 }));
      el.dispatchEvent(pointer('pointermove', { pointerType: 'touch', x: 50, y: 70 }));
      vi.advanceTimersByTime(LONG_PRESS_MS * 2);
      el.dispatchEvent(pointer('pointerdown', { pointerType: 'touch', x: 50, y: 50 }));
      el.dispatchEvent(pointer('pointerup', { pointerType: 'touch', x: 50, y: 50 }));
      vi.advanceTimersByTime(LONG_PRESS_MS * 2);
      expect(actions.showInfo).not.toHaveBeenCalled();
    });

    it('dragging a card upwards with the mouse shows the details (the legacy gesture)', async () => {
      const { el } = await render();
      el.dispatchEvent(pointer('pointerdown', { x: 50, y: 100 }));
      el.dispatchEvent(pointer('pointerup', { x: 50, y: 100 - DRAG_UP_FOR_INFO + 1 }));
      el.click();
      expect(actions.showInfo).not.toHaveBeenCalled();
      expect(actions.activate).toHaveBeenCalledTimes(1);

      el.dispatchEvent(pointer('pointerdown', { x: 50, y: 100 }));
      el.dispatchEvent(pointer('pointerup', { x: 50, y: 100 - DRAG_UP_FOR_INFO }));
      el.click();
      expect(actions.showInfo).toHaveBeenCalledWith(CARD.ref);
      expect(actions.activate).toHaveBeenCalledTimes(1);
    });

    it('cannot be dragged as an image', async () => {
      const { el } = await render();
      const event = new Event('dragstart', { bubbles: true, cancelable: true });
      el.querySelector('img')!.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(true);
    });
  });
});
