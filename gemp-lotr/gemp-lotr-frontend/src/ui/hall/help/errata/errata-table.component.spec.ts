import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { ERRATA_SERVICES } from '../../../../core/hall/help/errata/errata-services';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { readServerResponse } from '../../../../testing/files';
import { ErrataTableComponent } from './errata-table.component';

describe('ErrataTableComponent', () => {
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
