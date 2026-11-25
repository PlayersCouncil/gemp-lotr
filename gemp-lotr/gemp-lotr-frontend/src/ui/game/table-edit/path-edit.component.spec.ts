import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EMPTY_FILTER } from '../../../core/card-filter/filter-fields';
import { CardSearch, SearchBound, SearchPage } from '../../../core/game/table-edit/card-search';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { providePathTesting, SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { PathEditComponent } from './path-edit.component';
import { PathPopups } from './path-popups';
import { SearchWindowComponent } from './search-window.component';

describe('PathEditComponent', () => {
  let edit: FakeTableEdit;
  let popups: PathPopups;
  const bound = signal<SearchBound | null>(null);
  const open = vi.fn((to: SearchBound | null) => bound.set(to));

  function render() {
    edit = fakeTableEdit();
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        ...providePathTesting(),
        { provide: TableEdit, useValue: edit },
        {
          provide: CardSearch,
          useValue: {
            fields: signal(EMPTY_FILTER),
            blockOptions: signal([]),
            setOptions: signal([]),
            hiddenCultureGroups: signal(new Set()),
            isPackProduct: signal(false),
            previousDisabled: signal(true),
            nextDisabled: signal(true),
            sliderDisabled: signal(true),
            pageSizes: { min: 6, max: 48, start: 18 },
            loading: signal(false),
            page: signal<SearchPage | null>(null),
            bound,
            omitted: signal([]),
            open,
          },
        },
      ],
    });
    popups = TestBed.inject(PathPopups);
    const fixture = TestBed.createComponent(PathEditComponent);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('draws the site chooser, and the search window only while a site is searched for, held to its number', () => {
    const { fixture, el } = render();
    expect(el.querySelector('gd-site-chooser')).not.toBeNull();
    expect(el.querySelector('gd-edit-search')).toBeNull();
    popups.openChooserAt(3, { left: 10, top: 20, right: 30, bottom: 40 }, null, false);
    popups.openSearch();
    fixture.detectChanges();
    expect(el.querySelector('dialog[data-dialog="edit-search"]')?.textContent).toContain('Site 3');
    expect(open).toHaveBeenLastCalledWith({ filter: 'cardType:SITE siteNumber:3', label: 'Site 3' });
    const window = fixture.debugElement.query((node) => node.componentInstance instanceof SearchWindowComponent);
    (window.componentInstance as SearchWindowComponent).closed.emit();
    expect(popups.search()).toBeNull();
    fixture.detectChanges();
    expect(el.querySelector('gd-edit-search')).toBeNull();
  });

  it('lays a site found at the number (refused: the window stays); unnumbered sites are searched for unbound', async () => {
    const { fixture } = render();
    edit.board.update((board) => board && { ...board, cards: [] });
    popups.openChooserAt(5, { left: 10, top: 20, right: 30, bottom: 40 }, null, false);
    popups.openSearch();
    fixture.detectChanges();
    expect(open).toHaveBeenLastCalledWith({ filter: 'cardType:SITE', label: 'Sites' });
    const window = fixture.debugElement.query((node) => node.componentInstance instanceof SearchWindowComponent);
    const found = { blueprintId: '1_350', name: 'Mount Doom', type: 'site', culture: null, site: true };
    edit.apply.mockResolvedValueOnce(false);
    (window.componentInstance as SearchWindowComponent).chosen.emit(found);
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'site', number: 5, blueprint: '1_350', seat: 'freeps' }], {
      x: 30,
      y: 20,
    });
    expect(popups.search()).not.toBeNull();
    (window.componentInstance as SearchWindowComponent).chosen.emit(found);
    await fixture.whenStable();
    expect(popups.search()).toBeNull();
    // (closed meanwhile: nothing laid)
    edit.apply.mockClear();
    await (fixture.componentInstance as unknown as { chosen(card: typeof found): Promise<void> }).chosen(found);
    expect(edit.apply).not.toHaveBeenCalled();
  });
});
