import { provideDeckbuilderState } from '../../../core/deckbuilder-state';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CardSize } from '../../../core/collection/card-size';
import { BlockFilter } from '../../../core/api/models';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { CollectionQuery } from '../../../core/collection/collection-query.service';
import { FilterPins } from '../../../core/collection/filter-pins';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { CollectionFilterComponent } from './collection-filter.component';

const BLOCKS: BlockFilter[] = [
  { filter: 'fotr_block', name: 'Fellowship Block', setFilters: { 'The Fellowship of the Ring': '1' } },
  { filter: 'hobbit', name: 'Hobbit', setFilters: { 'Main Deck': '30' } },
];

describe('CollectionFilterComponent', () => {
  let collection: CollectionQuery;
  let browser: CollectionBrowser;

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
    collection = TestBed.inject(CollectionQuery);
    browser = TestBed.inject(CollectionBrowser);
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render(part?: 'pane' | 'menu') {
    const fixture = TestBed.createComponent(CollectionFilterComponent);
    if (part) {
      fixture.componentRef.setInput('part', part);
    }
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
        ['cardType', 'select', 'Card\u00a0Type:'],
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

    it('has no inline styles', async () => {
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

  describe('the form', () => {
    const control = <T extends HTMLElement>(el: HTMLElement, field: string, tag = 'select') =>
      el.querySelector<T>(`[data-field="${field}"] ${tag}`)!;
    const backendRequests = () => TestBed.inject(HttpTestingController).match((r) => r.url.includes('/collection/'));

    it('asks the server only when typed text is finished, not at each key', async () => {
      const { el, fixture } = await render();
      backendRequests();
      const name = control<HTMLInputElement>(el, 'name', 'input');
      name.value = 'Gan';
      name.dispatchEvent(new Event('input', { bubbles: true }));
      await fixture.whenStable();
      expect(backendRequests().length).toBe(0);
      expect(collection.fields().name).toBe('');
      name.dispatchEvent(new Event('change', { bubbles: true }));
      expect(collection.fields().name).toBe('Gan');
      expect(backendRequests().length).toBe(1);
    });

    it('shows a value changed elsewhere', async () => {
      const { el, fixture } = await render();
      collection.setField('side', 'SHADOW');
      collection.setField('name', 'Aragorn');
      await fixture.whenStable();
      expect(control<HTMLSelectElement>(el, 'side').value).toBe('SHADOW');
      expect(control<HTMLInputElement>(el, 'name', 'input').value).toBe('Aragorn');
      backendRequests();
    });

    it('shows the fields emptied by the reset buttons', async () => {
      const { el, fixture } = await render();
      collection.setField('rarity', 'R');
      collection.setField('name', 'Aragorn');
      await fixture.whenStable();
      el.querySelector<HTMLElement>('[data-action="reset-all"]')!.click();
      await fixture.whenStable();
      expect(control<HTMLSelectElement>(el, 'rarity').value).toBe('');
      expect(control<HTMLInputElement>(el, 'name', 'input').value).toBe('');
      backendRequests();
    });

    it('shows the block and set a new format selects, from their new lists', async () => {
      const { el, fixture } = await render();
      collection.updateFormat('fotr_block', BLOCKS.slice(0, 1));
      await fixture.whenStable();
      const block = control<HTMLSelectElement>(el, 'block');
      expect([...block.options].map((o) => o.value)).toEqual(['', 'fotr_block']);
      expect(block.value).toBe('fotr_block');
      expect(control<HTMLSelectElement>(el, 'set').value).toBe(collection.fields().set);
      backendRequests();
    });

    it('shows the number of a comparison, and empties it with the comparison', async () => {
      const { el, fixture } = await render();
      collection.setField('strengthCompare', 'EQUALS');
      collection.setField('strengthValue', '7');
      await fixture.whenStable();
      const input = control<HTMLInputElement>(el, 'strength', 'input');
      expect(input.value).toBe('7');
      collection.setField('strengthCompare', '');
      await fixture.whenStable();
      expect(input.value).toBe('');
      backendRequests();
    });

    it('limits the numbers and the texts as before', async () => {
      const { el, fixture } = await render();
      collection.setField('strengthCompare', 'EQUALS');
      await fixture.whenStable();
      const number = control<HTMLInputElement>(el, 'strength', 'input');
      expect([number.min, number.max]).toEqual(['0', '12']);
      expect(control<HTMLInputElement>(el, 'name', 'input').maxLength).toBe(100);
      expect(control<HTMLInputElement>(el, 'gametext', 'input').maxLength).toBe(100);
      backendRequests();
    });

    it('leaves the Product and Name usable for packs, and disables the rest', async () => {
      const { el, fixture } = await render();
      collection.setField('product', 'pack');
      await fixture.whenStable();
      expect(control<HTMLSelectElement>(el, 'product').disabled).toBe(false);
      expect(control<HTMLInputElement>(el, 'name', 'input').disabled).toBe(false);
      expect(control<HTMLSelectElement>(el, 'rarity').disabled).toBe(true);
      expect(control<HTMLSelectElement>(el, 'twilight').disabled).toBe(true);
      collection.setField('product', '');
      await fixture.whenStable();
      expect(control<HTMLSelectElement>(el, 'rarity').disabled).toBe(false);
      backendRequests();
    });
  });

  it('pages, sizes pages and resets', async () => {
    const { fixture, el } = await render();
    const resetAll = vi.spyOn(collection, 'resetAll');
    const resetAdvanced = vi.spyOn(collection, 'resetAdvanced');
    const next = vi.spyOn(browser, 'nextPage');
    const size = vi.spyOn(browser, 'setPageSize');
    browser.nextDisabled.set(false);
    browser.sliderDisabled.set(false);
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

  describe("the compact layout's parts", () => {
    const fields = (el: HTMLElement) =>
      Array.from(el.querySelectorAll<HTMLElement>('.gd-field')).map((field) => field.dataset['field']);

    beforeEach(() => vi.spyOn(document, 'cookie', 'set').mockImplementation(() => undefined));

    it('shows all of it, with no data-part, by default', async () => {
      const { el } = await render();
      expect(el.hasAttribute('data-part')).toBe(false);
      expect(fields(el)).toHaveLength(19);
      expect(el.querySelector('.gd-filter__pins')).toBeNull();
      expect(el.querySelector<HTMLInputElement>('.gd-field[data-field="name"] input')!.placeholder).toBe('');
    });

    it('pane: the card name, the filters kept shown and the pager', async () => {
      TestBed.inject(FilterPins).toggle('side');
      const { el, field } = await render('pane');
      expect(el.dataset['part']).toBe('pane');
      expect(fields(el)).toEqual(['side', 'name']);
      expect(field<HTMLInputElement>('name', 'input').placeholder).toBe('Card name');
      expect(el.querySelector('.gd-pager')).not.toBeNull();
      expect(el.querySelector('.gd-filter__advanced-title')).toBeNull();
      expect(el.querySelector('.gd-filter__resets')).toBeNull();
      expect(el.querySelector('.gd-cultures')).toBeNull();
      expect(el.querySelector('.gd-filter__pins')).toBeNull();
    });

    it("pane: the pager's slider sets the card size (remembered), not the number of cards on a page", async () => {
      const size = vi.spyOn(browser, 'setPageSize');
      const { el } = await render('pane');
      const slider = el.querySelector<HTMLInputElement>('.gd-pager__size')!;
      expect(slider.getAttribute('aria-label')).toBe('Card size');
      expect(slider.dataset['sets']).toBe('card-size');
      expect([slider.min, slider.max, slider.step, slider.value]).toEqual(['50', '200', '10', '100']);
      expect(slider.disabled).toBe(false);
      slider.value = '150';
      slider.dispatchEvent(new Event('input'));
      expect(TestBed.inject(CardSize).percent()).toBe(150);
      expect(size).not.toHaveBeenCalled();
    });

    it('menu: the other filters, the cultures, the resets and the choice of filters kept shown', async () => {
      TestBed.inject(FilterPins).toggle('side');
      const { el } = await render('menu');
      expect(el.dataset['part']).toBe('menu');
      expect(fields(el)).not.toContain('name');
      expect(fields(el)).not.toContain('side');
      expect(fields(el)).toHaveLength(17);
      expect(el.querySelector('.gd-pager')).toBeNull();
      expect(el.querySelector('.gd-filter__advanced-title')).not.toBeNull();
      expect(el.querySelector('.gd-filter__resets')).not.toBeNull();
      expect(el.querySelector('.gd-cultures')).not.toBeNull();
      const pins = Array.from(el.querySelectorAll<HTMLElement>('fieldset.gd-filter__pins > label.gd-pin'));
      expect(pins.map((pin) => [pin.dataset['pin'], pin.textContent!.trim()])).toEqual([
        ['product', 'Product'],
        ['rarity', 'Rarity'],
        ['block', 'Block'],
        ['set', 'Set'],
        ['side', 'Side'],
        ['sort', 'Sort by'],
        ['cardType', 'Card Type'],
        ['gametext', 'Game Text contains'],
        ['keyword', 'Keyword'],
        ['phase', 'Phase'],
        ['race', 'Race'],
        ['itemClass', 'Item Class'],
        ['twilight', 'Twilight'],
        ['siteNumber', 'Site Number'],
        ['strength', 'Strength'],
        ['signet', 'Signet'],
        ['vitality', 'Vitality'],
        ['resistance', 'Resistance'],
        ['cultures', 'Cultures'],
      ]);
      expect(pins.map((pin) => pin.querySelector('input')!.checked)).toEqual(
        pins.map((pin) => pin.dataset['pin'] === 'side'),
      );
    });

    it('moves a filter between the menu and the pane when it is kept shown or not', async () => {
      const pane = await render('pane');
      const menu = await render('menu');
      const keep = menu.el.querySelector<HTMLInputElement>('.gd-pin[data-pin="cultures"] input')!;
      keep.click();
      await pane.fixture.whenStable();
      await menu.fixture.whenStable();
      expect(pane.el.querySelector('.gd-cultures')).not.toBeNull();
      expect(menu.el.querySelector('.gd-cultures')).toBeNull();
      expect(TestBed.inject(FilterPins).isPinned('cultures')).toBe(true);
      menu.el.querySelector<HTMLInputElement>('.gd-pin[data-pin="cultures"] input')!.click();
      await pane.fixture.whenStable();
      expect(pane.el.querySelector('.gd-cultures')).toBeNull();
    });

    it('sets the query from either part', async () => {
      const { field } = await render('menu');
      const select = field('cardType');
      select.value = 'COMPANION';
      select.dispatchEvent(new Event('change'));
      expect(collection.fields().cardType).toBe('COMPANION');
    });
  });
});
