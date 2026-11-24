import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EMPTY_FILTER } from '../../../core/card-filter/filter-fields';
import { CardSearch, SearchBound } from '../../../core/game/table-edit/card-search';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { SearchWindowComponent } from './search-window.component';

/** The search window's `use`: the button that hands its filters on (the authoring panel's Find…, BoundFinder). */
describe('SearchWindowComponent, handing its filters on', () => {
  const filter = signal('cardType:ALLY culture:GONDOR sort:name');
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
    page: signal({ total: 0, start: 0, cards: [] }),
    bound: signal<SearchBound | null>(null),
    omitted: signal(['product', 'rarity']),
    filter,
    open: vi.fn(),
    setField: vi.fn(),
    toggleCulture: vi.fn(),
    resetAdvanced: vi.fn(),
    resetAll: vi.fn(),
    previousPage: vi.fn(),
    nextPage: vi.fn(),
    setPageSize: vi.fn(),
  };

  function render(use: string | null) {
    TestBed.configureTestingModule({
      providers: [...provideTestCardInteractions(), { provide: CardSearch, useValue: search }],
    });
    const fixture = TestBed.createComponent(SearchWindowComponent);
    fixture.componentRef.setInput('use', use);
    fixture.componentRef.setInput('open', true);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('offers no such button by default', () => {
    const { el } = render(null);
    expect(el.querySelector('button[data-action="use"]')).toBeNull();
  });

  it('hands on the filter text without its sort', () => {
    const { fixture, el } = render('Use as bound');
    const used: string[] = [];
    fixture.componentInstance.used.subscribe((text) => used.push(text));
    const button = el.querySelector<HTMLButtonElement>('button[data-action="use"]')!;
    expect(button.textContent?.trim()).toBe('Use as bound');
    expect(button.title).toBe('cardType:ALLY culture:GONDOR');
    button.click();
    expect(used).toEqual(['cardType:ALLY culture:GONDOR']);
    filter.set('sort:name');
    fixture.detectChanges();
    expect(button.title).toBe('Any card');
  });
});
