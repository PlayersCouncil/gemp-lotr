import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardSearch, FoundCard, SearchBound, SearchPage } from '../../../core/game/table-edit/card-search';
import { EMPTY_FILTER } from '../../../core/card-filter/filter-fields';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { SearchWindowComponent } from './search-window.component';

const LEGOLAS: FoundCard = {
  blueprintId: '1_50',
  name: 'Legolas, Greenleaf',
  type: 'companion',
  culture: 'elven',
  site: false,
};
const ARWEN: FoundCard = {
  blueprintId: '1_30',
  name: 'Arwen, Daughter of Elrond',
  type: 'companion',
  culture: 'elven',
  site: false,
};

describe('SearchWindowComponent', () => {
  const page = signal<SearchPage | null>(null);
  const bound = signal<SearchBound | null>(null);
  const search = {
    fields: signal(EMPTY_FILTER),
    blockOptions: signal([{ value: '', label: 'Any' }]),
    setOptions: signal([{ value: '', label: 'Any' }]),
    hiddenCultureGroups: signal(new Set()),
    isPackProduct: signal(false),
    previousDisabled: signal(true),
    nextDisabled: signal(true),
    sliderDisabled: signal(false),
    pageSizes: { min: 6, max: 48, start: 18 },
    loading: signal(false),
    page,
    bound,
    omitted: signal(['product', 'rarity', 'cardType']),
    open: vi.fn((to: SearchBound | null) => bound.set(to)),
    setField: vi.fn(),
    toggleCulture: vi.fn(),
    resetAdvanced: vi.fn(),
    resetAll: vi.fn(),
    previousPage: vi.fn(),
    nextPage: vi.fn(),
    setPageSize: vi.fn(),
  };

  function render() {
    TestBed.configureTestingModule({
      providers: [...provideTestCardInteractions(), { provide: CardSearch, useValue: search }],
    });
    const fixture = TestBed.createComponent(SearchWindowComponent);
    fixture.componentRef.setInput('heading', 'Add to Characters');
    fixture.componentRef.setInput('bound', { filter: 'cardType:COMPANION', label: 'Companions' });
    fixture.componentRef.setInput('open', true);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('searches when it opens, within its bound, with the shared filter panel and Clear filters', () => {
    page.set(null);
    const { el } = render();
    expect(search.open).toHaveBeenCalledWith({ filter: 'cardType:COMPANION', label: 'Companions' });
    expect(el.querySelector('dialog[data-dialog="edit-search"] [data-dialog-title]')?.textContent).toBe(
      'Add to Characters',
    );
    const filter = el.querySelector('.gd-edit-search__filters gd-collection-filter')!;
    expect(filter.querySelector('.gd-field[data-field="cardType"]')).toBeNull();
    expect(filter.querySelector('.gd-filter__resets')).toBeNull();
    const bound = el.querySelector<HTMLElement>('.gd-edit-search__bound')!;
    expect([bound.textContent, bound.title]).toEqual(['Companions', 'cardType:COMPANION']);
    expect(el.querySelector('.gd-edit-search__count')?.textContent).toBe('');
    el.querySelector<HTMLButtonElement>('[data-action="clear"]')!.click();
    expect(search.resetAll).toHaveBeenCalled();
  });

  it('shows the cards found as cards; using one chooses it; a refusal is said in it', () => {
    page.set({ total: 40, start: 18, cards: [LEGOLAS, ARWEN] });
    const { fixture, el } = render();
    const chosen: FoundCard[] = [];
    fixture.componentInstance.chosen.subscribe((card) => chosen.push(card));
    expect(el.querySelector('.gd-edit-search__count')?.textContent).toBe('19–20 of 40');
    const cards = [...el.querySelectorAll<HTMLButtonElement>('.gd-edit-search__cards button.gd-card')];
    expect(cards.map((card) => [card.dataset['blueprint'], card.title])).toEqual([
      ['1_50', 'Legolas, Greenleaf'],
      ['1_30', 'Arwen, Daughter of Elrond'],
    ]);
    cards[1]!.click();
    expect(chosen).toEqual([ARWEN]);
    fixture.componentRef.setInput('said', 'only a companion goes among the characters');
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-search__problem')?.textContent).toBe(
      'only a companion goes among the characters',
    );
    page.set({ total: 0, start: 0, cards: [] });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-search__count')?.textContent).toBe('No cards');
  });
});
