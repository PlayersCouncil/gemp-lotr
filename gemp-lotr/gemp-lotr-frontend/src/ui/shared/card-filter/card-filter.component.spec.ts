import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardFilterModel, CardFilterPager } from '../../../core/card-filter/card-filter-model';
import { EMPTY_FILTER, FilterFields } from '../../../core/card-filter/filter-fields';
import { CardFilterComponent } from './card-filter.component';

/**
 * The filter panel on a model of its own (the game page's search has one; the deck builder's is tested with its own
 * state in ui/deckbuilder/collection/collection-filter.component.spec.ts).
 */
describe('CardFilterComponent (shared)', () => {
  const fields = signal<FilterFields>(EMPTY_FILTER);
  const model = {
    fields,
    blockOptions: signal([{ value: '', label: 'Any' }]),
    setOptions: signal([{ value: '', label: 'Any' }]),
    hiddenCultureGroups: signal(new Set()),
    isPackProduct: signal(false),
    setField: vi.fn(),
    toggleCulture: vi.fn(),
    resetAdvanced: vi.fn(),
    resetAll: vi.fn(),
  };
  const pager = {
    previousDisabled: signal(false),
    nextDisabled: signal(true),
    sliderDisabled: signal(false),
    pageSizes: { min: 6, max: 48, start: 18 },
    previousPage: vi.fn(),
    nextPage: vi.fn(),
    setPageSize: vi.fn(),
  };

  function render(withPager: boolean, inputs: { omit?: string[]; resets?: boolean } = {}) {
    TestBed.configureTestingModule({
      providers: [
        { provide: CardFilterModel, useValue: model },
        ...(withPager ? [{ provide: CardFilterPager, useValue: pager }] : []),
      ],
    });
    const fixture = TestBed.createComponent(CardFilterComponent);
    for (const [name, value] of Object.entries(inputs)) fixture.componentRef.setInput(name, value);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('leaves out the fields asked, and the resets, and works the pager it is given', () => {
    const el = render(true, { omit: ['product', 'rarity', 'cardType'], resets: false });
    const names = [...el.querySelectorAll<HTMLElement>('label.gd-field')].map((field) => field.dataset['field']);
    expect(names).not.toContain('product');
    expect(names).not.toContain('cardType');
    expect(names).toContain('name');
    expect(el.querySelector('.gd-filter__resets')).toBeNull();
    const slider = el.querySelector<HTMLInputElement>('.gd-pager__size')!;
    expect([slider.min, slider.max, slider.value]).toEqual(['6', '48', '18']);
    el.querySelector<HTMLButtonElement>('.gd-pager__previous')!.click();
    expect(pager.previousPage).toHaveBeenCalled();
    expect(el.querySelector<HTMLButtonElement>('.gd-pager__next')!.disabled).toBe(true);
    slider.value = '30';
    slider.dispatchEvent(new Event('input'));
    expect(pager.setPageSize).toHaveBeenCalledWith(30);
    const name = el.querySelector<HTMLInputElement>('.gd-field[data-field="name"] input')!;
    name.value = 'legolas';
    name.dispatchEvent(new Event('change'));
    expect(model.setField).toHaveBeenCalledWith('name', 'legolas');
  });

  it('has no pager without one, and the resets by default', () => {
    const el = render(false);
    expect(el.querySelector('.gd-pager')).toBeNull();
    expect(el.querySelector('.gd-filter__resets')).not.toBeNull();
    el.querySelector<HTMLButtonElement>('[data-action="reset-all"]')!.click();
    expect(model.resetAll).toHaveBeenCalled();
  });
});
