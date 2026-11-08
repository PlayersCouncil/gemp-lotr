import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BlockFilter } from '../../../core/api/models';
import { CollectionService } from '../../../core/collection/collection.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CollectionFilterComponent } from './collection-filter.component';

const BLOCKS: BlockFilter[] = [
  { filter: 'fotr_block', name: 'Fellowship Block', setFilters: { 'The Fellowship of the Ring': '1' } },
  { filter: 'hobbit', name: 'Hobbit', setFilters: { 'Main Deck': '30' } },
];

describe('CollectionFilterComponent', () => {
  let collection: CollectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    collection = TestBed.inject(CollectionService);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render() {
    const fixture = TestBed.createComponent(CollectionFilterComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const field = <T extends HTMLElement = HTMLSelectElement>(name: string, control = 'select') =>
      el.querySelector<T>(`.gd-field[data-field="${name}"] ${control}`)!;
    return { fixture, el, field };
  }

  describe('DOM contract', () => {
    it('has the pager, the fields in the legacy order, the resets and the cultures', async () => {
      const { el } = await render();
      expect(el.classList).toContain('gd-filter');
      expect(el.querySelector('.gd-pager > button.gd-pager__previous[data-icon="previous"]')).not.toBeNull();
      expect(el.querySelector('.gd-pager > .gd-pager__track > input[type="range"].gd-pager__size')).not.toBeNull();
      expect(el.querySelector('.gd-pager > button.gd-pager__next[data-icon="next"]')).not.toBeNull();
      const fields = (part: string) =>
        Array.from(el.querySelectorAll<HTMLElement>(`.gd-filter__${part} label.gd-field`)).map((f) => [
          f.dataset['field'],
          f.dataset['kind'],
          f.querySelector('.gd-field__label')!.textContent,
        ]);
      expect(fields('basic')).toEqual([
        ['product', 'select', 'Product:'],
        ['rarity', 'select', 'Rarity:'],
        ['block', 'select', 'Block:'],
        ['set', 'select', 'Set:'],
        ['side', 'select', 'Side:'],
        ['sort', 'select', 'Sort by:'],
        ['cardType', 'select', 'Card Type:'],
      ]);
      expect(fields('advanced').map(([name, kind]) => `${name}:${kind}`)).toEqual([
        'name:text',
        'gametext:text',
        'keyword:select',
        'phase:select',
        'race:select',
        'itemClass:select',
        'twilight:numeric',
        'siteNumber:numeric',
        'strength:numeric',
        'signet:select',
        'vitality:numeric',
        'resistance:numeric',
      ]);
      const wide = Array.from(el.querySelectorAll<HTMLElement>('.gd-field[data-wide]')).map((f) => f.dataset['field']);
      expect(wide).toEqual(['cardType', 'name', 'gametext']);
      expect(el.querySelector('.gd-filter__advanced-title a')!.getAttribute('href')).toContain('CardSearch');
      expect(
        Array.from(el.querySelectorAll<HTMLElement>('.gd-filter__resets button.gd-button')).map(
          (b) => b.dataset['action'],
        ),
      ).toEqual(['reset-advanced', 'reset-all']);
      const cultures = el.querySelectorAll<HTMLElement>('.gd-cultures__group label.gd-culture');
      expect(cultures.length).toBe(22);
      expect(cultures[0].dataset['culture']).toBe('DWARVEN');
      expect(cultures[0].dataset['iconKind']).toBe('culture');
      expect(cultures[0].querySelector('input[type="checkbox"].gd-culture__input')).not.toBeNull();
      expect(cultures[0].querySelector<HTMLImageElement>('img.gd-culture__icon')!.src).toContain('dwarven.svg');
    });

    it('has no visual styles of its own', async () => {
      const { el } = await render();
      expect(el.querySelectorAll('[style]').length).toBe(0);
    });
  });

  it('sets the filter fields', async () => {
    const { fixture, field } = await render();
    const side = field('side');
    side.value = 'SHADOW';
    side.dispatchEvent(new Event('change'));
    const name = field<HTMLInputElement>('name', 'input');
    name.value = 'Gandalf';
    name.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(collection.fields().side).toBe('SHADOW');
    expect(collection.fields().name).toBe('Gandalf');
  });

  it('shows the value of a numeric filter only once a comparison is chosen', async () => {
    const { fixture, field } = await render();
    const value = field<HTMLInputElement>('strength', 'input[type="number"]');
    expect(value.hidden).toBe(true);
    const compare = field('strength');
    compare.value = compare.options[1].value;
    compare.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(value.hidden).toBe(false);
    value.value = '7';
    value.dispatchEvent(new Event('change'));
    expect(collection.fields().strengthValue).toBe('7');
  });

  it('disables what packs cannot be filtered by when the product is packs', async () => {
    const { fixture, el, field } = await render();
    collection.setField('product', 'pack');
    await fixture.whenStable();
    expect(field('product').disabled).toBe(false);
    expect(field<HTMLInputElement>('name', 'input').disabled).toBe(false);
    expect(field('rarity').disabled).toBe(true);
    expect(field<HTMLInputElement>('gametext', 'input').disabled).toBe(true);
    expect(field('twilight').disabled).toBe(true);
    expect(el.querySelectorAll('.gd-field select:disabled').length).toBe(16);
  });

  it('pages, sizes pages and resets', async () => {
    const { fixture, el } = await render();
    const resetAll = vi.spyOn(collection, 'resetAll');
    const resetAdvanced = vi.spyOn(collection, 'resetAdvanced');
    const next = vi.spyOn(collection, 'nextPage');
    const size = vi.spyOn(collection, 'setPageSize');
    collection.nextDisabled.set(false);
    collection.sliderDisabled.set(false);
    await fixture.whenStable();
    el.querySelector<HTMLButtonElement>('.gd-pager__next')!.click();
    expect(next).toHaveBeenCalled();
    expect(el.querySelector<HTMLButtonElement>('.gd-pager__previous')!.disabled).toBe(true);
    const slider = el.querySelector<HTMLInputElement>('.gd-pager__size')!;
    slider.value = '30';
    slider.dispatchEvent(new Event('input'));
    expect(size).toHaveBeenCalledWith(30);
    el.querySelector<HTMLButtonElement>('[data-action="reset-advanced"]')!.click();
    el.querySelector<HTMLButtonElement>('[data-action="reset-all"]')!.click();
    expect(resetAdvanced).toHaveBeenCalled();
    expect(resetAll).toHaveBeenCalled();
  });

  it('toggles cultures, and hides the groups the format does not use', async () => {
    const { fixture, el } = await render();
    const dwarven = el.querySelector<HTMLElement>('.gd-culture[data-culture="DWARVEN"]')!;
    dwarven.querySelector<HTMLInputElement>('input')!.click();
    await fixture.whenStable();
    expect(collection.fields().cultures).toContain('DWARVEN');
    expect(dwarven.hasAttribute('data-active')).toBe(true);
    collection.updateFormat('fotr_block', BLOCKS.slice(0, 1));
    await fixture.whenStable();
    const hidden = Array.from(el.querySelectorAll<HTMLElement>('.gd-cultures__group[hidden]')).map(
      (g) => g.dataset['group'],
    );
    expect(hidden).toContain('culture_hobbit');
    expect(hidden).not.toContain('culture_freeps_base1');
    collection.updateFormat('hobbit', BLOCKS.slice(1));
    await fixture.whenStable();
    expect(el.querySelector<HTMLElement>('[data-group="culture_hobbit"]')!.hidden).toBe(false);
  });
});
