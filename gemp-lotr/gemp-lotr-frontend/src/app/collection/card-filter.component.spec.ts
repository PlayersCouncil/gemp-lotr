import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BlockFilter } from '../core/api/models';
import { CollectionService } from '../core/collection/collection.service';
import { PAGE_QUERY_STRING } from '../core/session/session.service';
import { CardFilterComponent } from './card-filter.component';

@Component({
  imports: [CardFilterComponent],
  template: `<div id="filter-main" gempCardFilter></div>`,
})
class Host {}

const BLOCKS: BlockFilter[] = [
  {
    filter: 'fotr_block',
    name: 'Fellowship Block',
    setFilters: { 'The Fellowship of the Ring': '1' },
  },
  { filter: 'hobbit', name: 'Hobbit', setFilters: { 'Main Deck': '30' } },
];

describe('CardFilterComponent', () => {
  let collection: CollectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    collection = TestBed.inject(CollectionService);
  });

  afterEach(() => {
    // Answer or drop the collection requests the interactions caused
    TestBed.inject(HttpTestingController).match(() => true);
  });

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const q = <T extends HTMLElement>(selector: string) => el.querySelector<T>(selector)!;
    return { fixture, el, q };
  }

  it('has the legacy filter table and buttons', async () => {
    const { el, q } = await render();
    expect(Array.from(el.querySelectorAll('label.filterLabel')).map((l) => l.textContent)).toEqual([
      'Product:',
      'Rarity:',
      'Block:',
      'Set:',
      'Side:',
      'Sort by:',
      'Card Type:',
      'Name contains:',
      'Game Text contains:',
      'Keyword:',
      'Phase:',
      'Race:',
      'Item Class:',
      'Twilight:',
      'Site Number:',
      'Strength:',
      'Signet:',
      'Vitality:',
      'Resistance:',
    ]);
    expect(q<HTMLSelectElement>('#sortSelect').value).toBe('name');
    expect(q('#previousPage').className).toContain('ui-button-icon-only');
    expect(q('#countSlider').className).toContain('ui-slider');
    expect(el.querySelectorAll('#culture-buttons label').length).toBe(22);
    expect(q('#labelDWARVEN').classList).toContain('ui-corner-left');
    expect(q('#labelTROLL').classList).toContain('ui-corner-right');
    expect(q('#labelTROLL').classList).not.toContain('culture');
  });

  it('fills Block and Set from the format and hides the cultures it lacks', async () => {
    collection.updateFormat('custom', BLOCKS);
    const { fixture, el, q } = await render();
    await fixture.whenStable();
    expect(Array.from(q<HTMLSelectElement>('#blockSelect').options).map((o) => o.value)).toEqual([
      '',
      'fotr_block',
      'hobbit',
    ]);
    expect(Array.from(q<HTMLSelectElement>('#setSelect').options).map((o) => o.value)).toEqual(['', '1', '30']);
    expect(q('#culture_hobbit').style.display).toBe('');
    expect(q('#culture_shadows').style.display).toBe('none');
    expect(el.querySelector('#culture_freeps_movie2')!.getAttribute('style')).toBeNull();
  });

  it('toggles a culture by its button', async () => {
    const { fixture, q } = await render();
    q('#labelELVEN').click();
    await fixture.whenStable();
    expect(collection.fields().cultures).toEqual(['ELVEN']);
    expect(q('#labelELVEN').classList).toContain('ui-state-active');
    expect(q('#labelELVEN').getAttribute('aria-pressed')).toBe('true');
    expect(q<HTMLInputElement>('#ELVEN').checked).toBe(true);

    q('#resetAllFiltersButton').click();
    await fixture.whenStable();
    expect(q('#labelELVEN').classList).not.toContain('ui-state-active');
    expect(q<HTMLInputElement>('#ELVEN').checked).toBe(false);
  });

  it('does not toggle when the mouse moved while pressed', async () => {
    const { fixture, q } = await render();
    const label = q('#labelELVEN');
    label.dispatchEvent(new MouseEvent('mousedown', { clientX: 5, clientY: 5, bubbles: true }));
    label.dispatchEvent(new MouseEvent('mouseup', { clientX: 9, clientY: 5, bubbles: true }));
    label.click();
    await fixture.whenStable();
    expect(collection.fields().cultures).toEqual([]);
  });

  it('shows the number once a comparison is chosen', async () => {
    const { fixture, q } = await render();
    const value = q<HTMLInputElement>('#twilightValueInput');
    expect(value.hidden).toBe(true);
    const compare = q<HTMLSelectElement>('#twilightCompareSelect');
    compare.value = 'EQUALS';
    compare.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(value.hidden).toBe(false);
  });

  it('disables the card fields when looking for packs', async () => {
    const { fixture, q } = await render();
    const product = q<HTMLSelectElement>('#productSelect');
    product.value = 'pack';
    product.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    for (const id of ['#raritySelect', '#blockSelect', '#setSelect', '#sideSelect', '#sortSelect', '#cardTypeSelect']) {
      expect(q<HTMLSelectElement>(id).disabled, id).toBe(true);
    }
    expect(q<HTMLInputElement>('#gametextInput').disabled).toBe(true);
    expect(q<HTMLInputElement>('#nameInput').disabled).toBe(false);
    expect(q<HTMLSelectElement>('#productSelect').disabled).toBe(false);
  });
});
