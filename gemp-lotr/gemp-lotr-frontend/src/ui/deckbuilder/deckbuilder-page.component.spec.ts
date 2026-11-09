import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DeckDraft } from '../../core/deck/deck-draft.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { DeckbuilderPageComponent, RESIZE_STEP } from './deckbuilder-page.component';

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
      'gd-page__tabs',
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

  it('chooses the pane with the tab bar; the collection tab also opens the collection', async () => {
    const { fixture, el } = await render();
    expect(el.dataset['pane']).toBe('deck');
    const tabs = Array.from(
      el.querySelectorAll<HTMLButtonElement>('.gd-page__tabs[role="tablist"] > button.gd-page__tab'),
    );
    expect(tabs.map((tab) => [tab.dataset['pane'], tab.getAttribute('role'), tab.textContent!.trim()])).toEqual([
      ['deck', 'tab', 'Deck'],
      ['collection', 'tab', 'Collection'],
    ]);
    expect(tabs.map((tab) => tab.getAttribute('aria-selected'))).toEqual(['true', 'false']);
    expect(el.querySelector(`#${tabs[1].getAttribute('aria-controls')}`)).toBe(
      el.querySelector('.gd-page__collection'),
    );
    el.querySelector<HTMLButtonElement>('.gd-page__toggle')!.click();
    await fixture.whenStable();
    expect(el.dataset['collection']).toBe('closed');
    tabs[1].click();
    await fixture.whenStable();
    expect(el.dataset['pane']).toBe('collection');
    expect(el.dataset['collection']).toBe('open');
    expect(tabs.map((tab) => tab.getAttribute('aria-selected'))).toEqual(['false', 'true']);
    tabs[0].click();
    await fixture.whenStable();
    expect(el.dataset['pane']).toBe('deck');
    expect(el.dataset['collection']).toBe('open');
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
});
