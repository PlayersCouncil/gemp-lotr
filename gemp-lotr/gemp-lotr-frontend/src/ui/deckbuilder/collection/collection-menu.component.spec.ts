import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { CollectionQuery } from '../../../core/collection/collection-query.service';
import { provideDeckbuilderState } from '../../../core/deckbuilder-state';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CollectionMenuComponent } from './collection-menu.component';

describe('CollectionMenuComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(CollectionMenuComponent);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('has the collection menu (the collection list and the filter menu part), then the filter pane part', async () => {
    const { el } = await render();
    expect(el.classList).toContain('gd-collection-bar');
    const menu = el.querySelector<HTMLElement>('dialog.gd-drawer[data-drawer="collection"]')!;
    expect(menu.querySelector('label.gd-menu__field select.gd-toolbar__collection')).not.toBeNull();
    expect(menu.querySelector('gd-collection-filter[data-part="menu"]')).not.toBeNull();
    expect(el.lastElementChild!.matches('gd-collection-filter[data-part="pane"]')).toBe(true);
  });

  it('chooses the collection', async () => {
    const browser = TestBed.inject(CollectionBrowser);
    const select = vi.spyOn(browser, 'selectCollectionType').mockImplementation(() => undefined);
    const { el } = await render();
    const list = el.querySelector<HTMLSelectElement>('select.gd-toolbar__collection')!;
    list.value = list.options[1].value;
    list.dispatchEvent(new Event('change'));
    expect(select).toHaveBeenCalledWith(list.options[1].value);
  });

  it('says on its button how many filters in the menu narrow the cards', async () => {
    const { fixture, el } = await render();
    const burger = el.querySelector<HTMLButtonElement>('button.gd-burger')!;
    expect(burger.getAttribute('aria-label')).toBe('Collection menu');
    const query = TestBed.inject(CollectionQuery);
    query.setField('side', 'SHADOW');
    await fixture.whenStable();
    expect(burger.querySelector('.gd-burger__badge')!.textContent).toBe('1');
    expect(burger.getAttribute('aria-label')).toBe('Collection menu, 1 filter set');
    query.setField('cardType', 'COMPANION');
    await fixture.whenStable();
    expect(burger.getAttribute('aria-label')).toBe('Collection menu, 2 filters set');
  });
});
