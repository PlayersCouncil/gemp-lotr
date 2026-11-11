import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DeckDraft } from '../../core/deck/deck-draft.service';
import { DeckOpener } from '../../core/deck/deck-opener.service';
import { DeckSaver } from '../../core/deck/deck-saver.service';
import { PacksService } from '../../core/packs/packs.service';
import { signal } from '@angular/core';
import { CompactLayout } from '../../core/platform/compact-layout.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { FakeNotifier, provideFakeNotifier } from '../../testing/fake-notifier';
import { DeckbuilderPageComponent, readSplit, RESIZE_STEP, SPLIT_COOKIE } from './deckbuilder-page.component';

const COLLECTIONS = '<collections><collection format="fotr_block" name="Sealed" type="s1"/></collections>';

describe('DeckbuilderPageComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(DeckbuilderPageComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el, separator: el.querySelector<HTMLElement>('.gd-page__separator')! };
  }

  describe('its state belongs to the page', () => {
    const answerStartup = () => {
      const backend = TestBed.inject(HttpTestingController);
      backend.expectOne((r) => r.url.endsWith('/deck/formats')).flush('{"Formats":{}}');
      backend.expectOne((r) => r.url.endsWith('/collection')).flush(COLLECTIONS);
      backend.expectOne((r) => r.url.endsWith('/league')).flush('<leagues/>');
    };
    const formatLabels = (el: HTMLElement) =>
      [...el.querySelectorAll<HTMLOptionElement>('select.gd-toolbar__format option')].map((o) => o.label || o.text);

    it('starts fresh when the page is left and opened again', async () => {
      const first = await render();
      answerStartup();
      await first.fixture.whenStable();
      const once = formatLabels(first.el);
      TestBed.inject(HttpTestingController).match(() => true);
      first.fixture.destroy();

      const second = await render();
      answerStartup();
      await second.fixture.whenStable();
      expect(formatLabels(second.el)).toEqual(once);
    });

    describe('work still running when the page closes', () => {
      const backend = () => TestBed.inject(HttpTestingController);
      const parts = (fixture: ComponentFixture<DeckbuilderPageComponent>) => fixture.debugElement.injector;

      it('a save reaches the server, but its answer no longer tells the player anything', async () => {
        const { fixture } = await render();
        const injector = parts(fixture);
        injector.get(DeckDraft).deckName.set('Mine');
        await injector.get(DeckSaver).saveCurrentDeck();
        fixture.destroy();
        const [save] = backend().match((r) => r.url.endsWith('/deck') && r.method === 'POST');
        expect(save.cancelled).toBe(false);
        save.flush('<ok/>');
        expect(TestBed.inject(FakeNotifier).dialogs).toEqual([]);
      });

      it('a deck being opened is cancelled', async () => {
        const { fixture } = await render();
        parts(fixture).get(DeckOpener).loadDeck('Mine');
        fixture.destroy();
        expect(backend().match((r) => r.url.endsWith('/deck') && r.method === 'GET')[0].cancelled).toBe(true);
      });

      it('does not fetch opened packs afterwards, which would use them up unseen', async () => {
        vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
        try {
          const fixture = TestBed.createComponent(DeckbuilderPageComponent);
          TestBed.tick();
          backend().match((r) => r.url.endsWith('/delivery/packContents'));
          parts(fixture).get(PacksService).packClicked('FotR - Booster', null);
          backend()
            .match((r) => r.url.includes('/collection/'))[0]
            ?.flush('<ok/>');
          fixture.destroy();
          vi.advanceTimersByTime(60_000);
          expect(backend().match((r) => r.url.endsWith('/delivery/packContents'))).toEqual([]);
        } finally {
          vi.useRealTimers();
        }
      });
    });

    it('does not keep the deck of the previous visit', async () => {
      const first = await render();
      first.fixture.debugElement.injector.get(DeckDraft).deckName.set('Left behind');
      first.fixture.destroy();
      const second = await render();
      expect(second.fixture.debugElement.injector.get(DeckDraft).deckName()).toBeNull();
    });
  });

  it('lays out the deck, the separator and the collection (DOM contract)', async () => {
    const { el } = await render();
    expect(el.classList).toContain('gd-page');
    expect(el.dataset['collection']).toBe('open');
    expect(Array.from(el.children).map((child) => child.className || child.localName)).toEqual([
      'gd-page__deck',
      'gd-page__separator',
      'gd-page__collection',
      'gd-deck-list-dialog',
      'gd-import-dialog',
      'gd-notes-dialog',
      'gd-pack-dialogs',
      'gd-card-info',
      'gd-auto-zoom',
      'gd-notifier',
    ]);
    expect(el.querySelector('section.gd-page__deck > gd-deck.gd-deck')).not.toBeNull();
    expect(el.querySelector('gd-auto-zoom > .gd-zoom[hidden]')).not.toBeNull();
    expect(el.querySelector('section.gd-page__collection > gd-collection-filter.gd-filter')).not.toBeNull();
    expect(el.querySelector('section.gd-page__collection > gd-collection-grid.gd-collection')).not.toBeNull();
    expect(el.querySelector('gd-card-info dialog.gd-card-info')).not.toBeNull();
    expect(el.querySelector('.gd-page__separator[role="separator"] > button.gd-page__toggle')).not.toBeNull();
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe('');
  });

  it('starts the deck builder: formats, collection and deck list are asked for', async () => {
    await render();
    const urls = TestBed.inject(HttpTestingController)
      .match(() => true)
      .map((request) => request.request.url);
    expect(urls.some((url) => url.endsWith('/deck/formats'))).toBe(true);
  });

  it('closes and opens the collection with the toggle', async () => {
    const { fixture, el } = await render();
    const toggle = el.querySelector<HTMLButtonElement>('.gd-page__toggle')!;
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    toggle.click();
    await fixture.whenStable();
    expect(el.dataset['collection']).toBe('closed');
    expect(el.querySelector<HTMLElement>('.gd-page__collection')!.hidden).toBe(true);
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    toggle.click();
    await fixture.whenStable();
    expect(el.dataset['collection']).toBe('open');
  });

  describe('pressing the toggle with the mouse', () => {
    async function pressable() {
      const { fixture, el } = await render();
      const separator = el.querySelector<HTMLElement>('.gd-page__separator')!;
      const toggle = el.querySelector<HTMLButtonElement>('.gd-page__toggle')!;
      vi.spyOn(el, 'getBoundingClientRect').mockReturnValue({ right: 1440 } as DOMRect);
      vi.spyOn(separator, 'getBoundingClientRect').mockReturnValue({ left: 1030 } as DOMRect);
      const pointer = (target: HTMLElement, type: string, x: number, timeStamp = 0) => {
        const event = new MouseEvent(type, { clientX: x, button: 0, bubbles: true, cancelable: true });
        Object.assign(event, { pointerId: 1 });
        Object.defineProperty(event, 'timeStamp', { value: timeStamp });
        target.dispatchEvent(event);
      };
      const press = async (timeStamp: number) => {
        pointer(toggle, 'pointerdown', 1035, timeStamp);
        pointer(separator, 'pointerup', 1035, timeStamp);
        await fixture.whenStable();
      };
      return { fixture, el, separator, toggle, pointer, press };
    }

    it('toggles once the button is released, if the pointer did not move', async () => {
      const { el, press } = await pressable();
      await press(1000);
      expect(el.dataset['collection']).toBe('closed');
      await press(2000);
      expect(el.dataset['collection']).toBe('open');
    });

    it('ignores the second press of a double click, while the legacy pane was still sliding', async () => {
      const { el, press } = await pressable();
      await press(1000);
      await press(1120);
      expect(el.dataset['collection']).toBe('closed');
      await press(1400);
      expect(el.dataset['collection']).toBe('open');
    });

    it('drags the separator when the pointer moves, without toggling', async () => {
      const { fixture, el, separator, toggle, pointer } = await pressable();
      pointer(toggle, 'pointerdown', 1035);
      await fixture.whenStable();
      expect(separator.hasAttribute('data-dragging')).toBe(false);
      pointer(separator, 'pointermove', 975);
      await fixture.whenStable();
      expect(separator.hasAttribute('data-dragging')).toBe(true);
      expect(el.querySelector('.gd-page__drag-preview')).not.toBeNull();
      pointer(separator, 'pointerup', 975);
      await fixture.whenStable();
      expect(separator.hasAttribute('data-dragging')).toBe(false);
      expect(el.dataset['collection']).toBe('open');
    });

    it('does not drag a closed collection, but still opens it', async () => {
      const { fixture, el, separator, toggle, pointer, press } = await pressable();
      await press(1000);
      pointer(toggle, 'pointerdown', 1035, 2000);
      pointer(separator, 'pointermove', 975, 2000);
      await fixture.whenStable();
      expect(separator.hasAttribute('data-dragging')).toBe(false);
      pointer(separator, 'pointerup', 1035, 2000);
      await fixture.whenStable();
      expect(el.dataset['collection']).toBe('open');
    });
  });

  it('resizes the collection with the arrow keys, and Enter closes it', async () => {
    const { fixture, el, separator } = await render();
    const collection = el.querySelector<HTMLElement>('.gd-page__collection')!;
    vi.spyOn(collection, 'getBoundingClientRect').mockReturnValue({ width: 410 } as DOMRect);
    const key = (k: string) => separator.dispatchEvent(new KeyboardEvent('keydown', { key: k, cancelable: true }));
    key('ArrowLeft');
    await fixture.whenStable();
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe(`${410 + RESIZE_STEP}px`);
    key('ArrowRight');
    key('ArrowRight');
    await fixture.whenStable();
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe(`${410 - RESIZE_STEP}px`);
    key('Enter');
    await fixture.whenStable();
    expect(el.dataset['collection']).toBe('closed');
  });

  it('resizes the collection when the dragged separator is released, keeping what the theme allowed', async () => {
    const { fixture, el, separator } = await render();
    vi.spyOn(el, 'getBoundingClientRect').mockReturnValue({ right: 1440 } as DOMRect);
    vi.spyOn(separator, 'getBoundingClientRect').mockReturnValue({ left: 1030 } as DOMRect);
    const pointer = (type: string, x: number) => {
      const event = new MouseEvent(type, {
        clientX: x,
        button: 0,
        bubbles: true,
        cancelable: true,
      });
      Object.assign(event, { pointerId: 1 });
      separator.dispatchEvent(event);
    };
    pointer('pointerdown', 1030);
    await fixture.whenStable();
    expect(separator.hasAttribute('data-dragging')).toBe(true);
    pointer('pointermove', 900);
    await fixture.whenStable();
    // Only a preview moves during the drag
    expect(el.style.getPropertyValue('--gd-drag-width')).toBe('540px');
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe('');
    const preview = el.querySelector<HTMLElement>('.gd-page__drag-preview')!;
    // The theme keeps the preview at 700px from the right (its maximum): that is the width kept
    vi.spyOn(preview, 'getBoundingClientRect').mockReturnValue({ right: 740 } as DOMRect);
    pointer('pointerup', 300);
    await fixture.whenStable();
    expect(separator.hasAttribute('data-dragging')).toBe(false);
    expect(el.querySelector('.gd-page__drag-preview')).toBeNull();
    expect(el.style.getPropertyValue('--gd-drag-width')).toBe('');
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe('700px');
  });

  it('keeps a quick drag released before the page was drawn again', async () => {
    const { fixture, el, separator } = await render();
    vi.spyOn(el, 'getBoundingClientRect').mockReturnValue({ right: 1440 } as DOMRect);
    vi.spyOn(separator, 'getBoundingClientRect').mockReturnValue({ left: 1030 } as DOMRect);
    // The preview is drawn where the drag put it (jsdom has no layout)
    const box = HTMLElement.prototype.getBoundingClientRect;
    vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockImplementation(function (this: HTMLElement) {
      return this.classList.contains('gd-page__drag-preview')
        ? ({ right: 1440 - parseInt(el.style.getPropertyValue('--gd-drag-width')) } as DOMRect)
        : box.call(this);
    });
    const pointer = (type: string, x: number) => {
      const event = new MouseEvent(type, { clientX: x, button: 0, bubbles: true, cancelable: true });
      Object.assign(event, { pointerId: 1 });
      separator.dispatchEvent(event);
    };
    // As Firefox delivers a fast drag: all of it before the next change detection
    pointer('pointerdown', 1030);
    pointer('pointermove', 950);
    pointer('pointerup', 900);
    await fixture.whenStable();
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe('540px');
    expect(el.querySelector('.gd-page__drag-preview')).toBeNull();
  });

  it('leaves the collection as it was when a drag is cancelled', async () => {
    const { fixture, el, separator } = await render();
    vi.spyOn(el, 'getBoundingClientRect').mockReturnValue({ right: 1440 } as DOMRect);
    vi.spyOn(separator, 'getBoundingClientRect').mockReturnValue({ left: 1030 } as DOMRect);
    const pointer = (type: string, x: number) => {
      const event = new MouseEvent(type, { clientX: x, button: 0, bubbles: true, cancelable: true });
      Object.assign(event, { pointerId: 1 });
      separator.dispatchEvent(event);
    };
    pointer('pointerdown', 1030);
    pointer('pointermove', 900);
    await fixture.whenStable();
    pointer('pointercancel', 900);
    await fixture.whenStable();
    expect(el.querySelector('.gd-page__drag-preview')).toBeNull();
    expect(el.style.getPropertyValue('--gd-collection-width')).toBe('');
  });

  describe('the compact layout (the modern theme on a portrait or narrow screen)', () => {
    let written: string[];

    beforeEach(() => {
      TestBed.overrideProvider(CompactLayout, { useValue: { active: signal(true) } });
      written = [];
      vi.spyOn(document, 'cookie', 'set').mockImplementation((cookie: string) => void written.push(cookie));
    });

    afterEach(() => vi.restoreAllMocks());

    /** The page 816px high from the top of the window, its separator 16px high and at 400px. */
    function placed(el: HTMLElement, separator: HTMLElement) {
      vi.spyOn(el, 'getBoundingClientRect').mockReturnValue({ top: 0, height: 816 } as DOMRect);
      vi.spyOn(separator, 'getBoundingClientRect').mockReturnValue({ top: 400 } as DOMRect);
      Object.defineProperty(separator, 'offsetHeight', { value: 16 });
      return (type: string, y: number) => {
        const event = new MouseEvent(type, { clientY: y, button: 0, bubbles: true, cancelable: true });
        Object.assign(event, { pointerId: 1 });
        separator.dispatchEvent(event);
      };
    }

    it('stacks the deck above the collection, half and half, with the collection always shown and its bar', async () => {
      const { el, separator } = await render();
      expect(el.dataset['layout']).toBe('compact');
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.5');
      expect(separator.getAttribute('aria-orientation')).toBe('horizontal');
      expect(separator.getAttribute('aria-valuenow')).toBe('50');
      expect(el.querySelector('.gd-page__toggle')).toBeNull();
      const collection = el.querySelector<HTMLElement>('.gd-page__collection')!;
      expect(collection.hidden).toBe(false);
      expect(collection.firstElementChild!.matches('gd-collection-menu.gd-collection-bar')).toBe(true);
    });

    it('moves the split with the dragged separator, within its limits, and remembers it when let go', async () => {
      const { fixture, el, separator } = await render();
      const pointer = placed(el, separator);
      pointer('pointerdown', 400);
      pointer('pointermove', 200);
      await fixture.whenStable();
      expect(separator.hasAttribute('data-dragging')).toBe(true);
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.25');
      expect(written).toEqual([]);
      pointer('pointermove', 0);
      await fixture.whenStable();
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.15');
      pointer('pointerup', 600);
      await fixture.whenStable();
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.75');
      expect(separator.hasAttribute('data-dragging')).toBe(false);
      expect(written).toHaveLength(1);
      expect(written[0]).toMatch(new RegExp(`^${SPLIT_COOKIE}=75; expires=.*; path=/gemp-lotr; SameSite=Lax$`));
    });

    it('moves the split with Up and Down, and remembers it', async () => {
      const { fixture, el, separator } = await render();
      const key = (k: string) => separator.dispatchEvent(new KeyboardEvent('keydown', { key: k, cancelable: true }));
      key('ArrowDown');
      await fixture.whenStable();
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.55');
      key('ArrowUp');
      key('ArrowUp');
      await fixture.whenStable();
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.45');
      expect(written.at(-1)).toMatch(new RegExp(`^${SPLIT_COOKIE}=45;`));
      // Enter doesn't close the collection, Left and Right do nothing
      key('Enter');
      key('ArrowLeft');
      await fixture.whenStable();
      expect(el.dataset['collection']).toBe('open');
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.45');
    });

    it('starts from the remembered split', async () => {
      vi.spyOn(document, 'cookie', 'get').mockReturnValue(`other=1; ${SPLIT_COOKIE}=30`);
      const { el } = await render();
      expect(el.style.getPropertyValue('--gd-split')).toBe('0.3');
    });

    it('reads a remembered split within its limits, and ignores one that is not a number', () => {
      expect(readSplit(`${SPLIT_COOKIE}=30`)).toBe(30);
      expect(readSplit(`${SPLIT_COOKIE}=99`)).toBe(85);
      expect(readSplit(`${SPLIT_COOKIE}=5`)).toBe(15);
      expect(readSplit(`${SPLIT_COOKIE}=half`)).toBe(50);
      expect(readSplit('')).toBe(50);
    });
  });

  it('is not compact in the classic layout: no data-layout, the toggle and a vertical separator', async () => {
    const { el, separator } = await render();
    expect(el.hasAttribute('data-layout')).toBe(false);
    expect(el.style.getPropertyValue('--gd-split')).toBe('');
    expect(separator.getAttribute('aria-orientation')).toBe('vertical');
    expect(separator.hasAttribute('aria-valuenow')).toBe(false);
    expect(el.querySelector('.gd-page__toggle')).not.toBeNull();
    expect(el.querySelector('gd-collection-menu')).toBeNull();
  });
});
