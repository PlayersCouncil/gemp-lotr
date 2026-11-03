import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DeckbuilderApp } from './deckbuilder-app';
import { PageLayoutService } from './layout/page-layout.service';

describe('DeckbuilderApp', () => {
  async function render() {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const layout = TestBed.inject(PageLayoutService);
    layout.viewportWidth.set(1280);
    layout.viewportHeight.set(800);
    const fixture = TestBed.createComponent(DeckbuilderApp);
    await fixture.whenStable();
    return { fixture, layout, el: fixture.nativeElement as HTMLElement };
  }

  it('renders the legacy page structure with the same element IDs', async () => {
    const { el } = await render();
    for (const id of [
      'deckDiv', 'manageDecks', 'ringBearerDiv', 'ringDiv', 'map-div', 'raceDiv', 'sitesDiv', 'statsDiv',
      'deckStats', 'decksRegion', 'collectionDiv', 'newDeckBut', 'formatSelect', 'collectionSelect', 'editingDeck',
    ]) {
      expect(el.querySelector('#' + id), id).not.toBeNull();
    }
    expect(el.querySelector('#editingDeck')!.textContent).toBe('New deck');
    expect((el.querySelector('#convertErrataBut') as HTMLButtonElement).disabled).toBe(true);
  });

  it('positions the panes and deck slots like the legacy layout', async () => {
    const { el } = await render();
    const deckDiv = el.querySelector<HTMLElement>('#deckDiv')!;
    expect(deckDiv.style.inset).toBe('0px 416px 0px 0px');
    expect(deckDiv.style.width).toBe('842px');
    expect(el.querySelector<HTMLElement>('#collectionDiv')!.style.width).toBe('388px');
    const sites = el.querySelector<HTMLElement>('#sitesDiv')!.style;
    expect([sites.left, sites.top, sites.width, sites.height]).toEqual(['5px', '182px', '225px', '596px']);
    expect(el.querySelector<HTMLElement>('#map-div')!.style.display).toBe('none');
  });

  it('closes and reopens the collection pane with the toggler', async () => {
    const { fixture, el } = await render();
    const toggler = el.querySelector<HTMLElement>('.ui-layout-toggler')!;
    toggler.click();
    await fixture.whenStable();
    expect(el.querySelector<HTMLElement>('#collectionDiv')!.style.display).toBe('none');
    expect(el.querySelector('.ui-layout-resizer')!.classList).toContain('ui-layout-resizer-closed');
    expect(toggler.title).toBe('Open');
    toggler.click();
    await fixture.whenStable();
    expect(el.querySelector<HTMLElement>('#collectionDiv')!.style.display).toBe('block');
  });

  it('resizes the collection pane when the resizer is dropped', async () => {
    const { fixture, el, layout } = await render();
    const resizer = el.querySelector<HTMLElement>('.ui-layout-resizer')!;
    // the bar starts at x = 1280 - 410 - 6 = 864
    resizer.dispatchEvent(new MouseEvent('mousedown', { clientX: 866, button: 0, bubbles: true }));
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 702 }));
    await fixture.whenStable();
    expect(el.querySelector('.ui-layout-resizer-dragging')).not.toBeNull();
    document.dispatchEvent(new MouseEvent('mouseup', { clientX: 702 }));
    await fixture.whenStable();
    expect(layout.page().resizerRight).toBe(574);
    expect(el.querySelector('.ui-layout-resizer-dragging')).toBeNull();
  });
});
