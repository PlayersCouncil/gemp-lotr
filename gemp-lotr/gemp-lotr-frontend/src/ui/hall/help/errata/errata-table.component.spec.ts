import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { ERRATA_SERVICES } from '../../../../core/hall/help/errata/errata-services';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { readServerResponse } from '../../../../testing/files';
import { ErrataTableComponent } from './errata-table.component';

/**
 * The rows laid out as a browser would, which jsdom doesn't: 7 rows 25px apart under a 30px header, in a box 100px
 * high that scrolls 105px.
 */
function layOut(table: HTMLElement): { box: HTMLElement; scrollTo: ReturnType<typeof vi.fn> } {
  const box = table;
  let scrollTop = 0;
  Object.defineProperty(box, 'scrollTop', {
    get: () => scrollTop,
    set: (value: number) => (scrollTop = value),
    configurable: true,
  });
  Object.defineProperty(box, 'clientHeight', { value: 100, configurable: true });
  Object.defineProperty(box, 'scrollHeight', { value: 205, configurable: true });
  Object.defineProperty(box, 'offsetParent', { value: document.body, configurable: true });
  const scrollTo = vi.fn((options: ScrollToOptions) => (scrollTop = options.top ?? scrollTop));
  box.scrollTo = scrollTo as unknown as HTMLElement['scrollTo'];
  const rect = (top: number, height: number) => ({ top, height }) as DOMRect;
  vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
    if (this === box) return rect(0, 100);
    if (this.tagName === 'THEAD') return rect(-scrollTop, 30);
    const index = this.getAttribute('data-index');
    return index === null ? rect(0, 0) : rect(30 + Number(index) * 25 - scrollTop, 25);
  });
  return { box, scrollTo };
}

describe('ErrataTableComponent', () => {
  afterEach(() => vi.restoreAllMocks());

  async function render() {
    TestBed.configureTestingModule({ providers: [provideGempHttp(), provideHttpClientTesting(), ...ERRATA_SERVICES] });
    const errata = TestBed.inject(PcErrataService);
    const fixture = TestBed.createComponent(ErrataTableComponent);
    const done = errata.start();
    TestBed.inject(HttpTestingController)
      .expectOne((req) => req.url === '/gemp-lotr-server/hall/errata/json')
      .flush(readServerResponse('errata.json'));
    await done;
    fixture.detectChanges();
    return { fixture, errata, table: fixture.nativeElement as HTMLElement };
  }

  it('heads each column with its sort, the sorted one marked (renderTable)', async () => {
    const { fixture, table } = await render();
    expect(table.querySelector('table')?.getAttribute('aria-label')).toBe('PC errata');
    const buttons = Array.from(table.querySelectorAll<HTMLButtonElement>('th button'));
    expect(buttons.map((button) => button.title)).toEqual([
      "The collector's number; sort by set and card number",
      'Sort by culture',
      'Sort by title',
      "How many times the Player's Council has revised the card; sort by it",
    ]);
    buttons[0]?.click();
    fixture.detectChanges();
    expect(buttons[0]?.textContent?.trim()).toBe('Card ▼');
    expect(table.querySelector('th')?.getAttribute('aria-sort')).toBe('descending');
    expect(table.querySelector('tr.gd-errata-table__row')?.getAttribute('data-id')).toBe('103_19');
  });

  it('marks the cards: New, playtest, the collector’s tooltip, the revisions (row)', async () => {
    const { table } = await render();
    const rows = Array.from(table.querySelectorAll('tr.gd-errata-table__row'));
    expect(rows[0]?.querySelector('[data-column=num]')?.getAttribute('title')).toBe(
      'The Fellowship of the Ring, card 45',
    );
    expect(rows[5]?.querySelector('[data-column=num]')?.getAttribute('title')).toBe('Set 101, card 5');
    expect(rows[0]?.querySelector('[data-badge=new]')?.getAttribute('title')).toBe('In the latest batch of errata');
    expect(rows[2]?.querySelector('[data-column=name]')?.textContent?.trim().replace(/\s+/g, ' ')).toBe(
      'Lórien Elfplaytest',
    );
    expect(rows[2]?.querySelector('[data-column=rev]')?.textContent).toBe('');
    expect(rows[3]?.querySelector('[data-column=culture]')?.children).toHaveLength(0);
  });

  it('names the culture when its icon fails to load', async () => {
    const { fixture, table } = await render();
    const cell = table
      .querySelectorAll('tr.gd-errata-table__row')[6]
      ?.querySelector('[data-column=culture]') as HTMLElement;
    expect(cell.title).toBe('Ringwraith');
    cell.querySelector('img')?.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    expect(cell.querySelector('img')).toBeNull();
    expect(cell.querySelector('.gd-errata-table__culture-name')?.textContent).toBe('Ringwraith');
  });
});

describe('ErrataTableComponent skimming (followScroll, scrollToRow, onListWheel)', () => {
  afterEach(() => vi.restoreAllMocks());

  async function laidOut() {
    TestBed.configureTestingModule({ providers: [provideGempHttp(), provideHttpClientTesting(), ...ERRATA_SERVICES] });
    const errata = TestBed.inject(PcErrataService);
    const fixture = TestBed.createComponent(ErrataTableComponent);
    const table = fixture.nativeElement as HTMLElement;
    const layout = layOut(table);
    const done = errata.start();
    TestBed.inject(HttpTestingController)
      .expectOne((req) => req.url === '/gemp-lotr-server/hall/errata/json')
      .flush(readServerResponse('errata.json'));
    await done;
    await fixture.whenStable();
    const scroll = async (top: number) => {
      layout.box.scrollTop = top;
      table.dispatchEvent(new Event('scroll'));
      await fixture.whenStable();
    };
    return { fixture, errata, table, ...layout, scroll };
  }

  it('brings the selected row to the middle of the rows, smoothly (scrollToRow)', async () => {
    const { fixture, errata, scrollTo } = await laidOut();
    errata.step(1);
    errata.step(1);
    errata.step(1);
    await fixture.whenStable();
    // row 3's middle is at 117.5; under the header the window is 70px
    expect(scrollTo).toHaveBeenLastCalledWith({ top: 53, behavior: 'smooth' });
  });

  it('leaves the rows alone for a clicked row, and for its own scroll arriving', async () => {
    const { fixture, errata, table, scrollTo, scroll } = await laidOut();
    (table.querySelectorAll('tr.gd-errata-table__row')[5] as HTMLElement).click();
    await fixture.whenStable();
    expect(scrollTo).not.toHaveBeenCalled();
    errata.step(-1);
    await fixture.whenStable();
    expect(scrollTo).toHaveBeenCalledTimes(1);
    const target = (scrollTo.mock.calls[0]?.[0] as ScrollToOptions).top ?? 0;
    // the smooth scroll reports a pixel short, then arrives: still row 4
    await scroll(target - 1);
    await scroll(target);
    expect(errata.index()).toBe(4);
  });

  it('lets the selection ride with the rows the reader scrolls, the first and last at the ends (followScroll)', async () => {
    const { errata, scroll } = await laidOut();
    await scroll(50);
    expect(errata.index()).toBe(3);
    expect(errata.touched()).toBe(true);
    await scroll(105);
    expect(errata.index()).toBe(6);
    await scroll(20);
    expect(errata.index()).toBeLessThan(6);
    await scroll(0);
    expect(errata.index()).toBe(0);
  });

  it('hands a scroll the page started over to the reader once they touch the rows', async () => {
    const { fixture, errata, table, scroll, scrollTo } = await laidOut();
    errata.last();
    await fixture.whenStable();
    expect(scrollTo).toHaveBeenCalled();
    table.dispatchEvent(new Event('pointerdown'));
    await scroll(0);
    expect(errata.index()).toBe(0);
  });

  it('keeps moving the selection with the wheel past the rows’ end, a row per row height, nudging at the last', async () => {
    const { fixture, errata, table, scroll } = await laidOut();
    const wheel = (deltaY: number) => {
      const event = new WheelEvent('wheel', { deltaY, cancelable: true });
      table.dispatchEvent(event);
      return event.defaultPrevented;
    };
    // not at an end: the rows scroll themselves
    await scroll(50);
    const at = errata.index();
    expect(wheel(30)).toBe(false);
    expect(errata.index()).toBe(at);
    await scroll(105);
    await fixture.whenStable();
    errata.select(2, { scroll: false });
    await fixture.whenStable();
    expect(wheel(60)).toBe(true);
    expect(errata.index()).toBe(4);
    expect(wheel(10)).toBe(true);
    expect(errata.index()).toBe(4);
    wheel(200);
    expect(errata.index()).toBe(6);
    wheel(100);
    expect(errata.bump()?.direction).toBe(1);
  });
});
