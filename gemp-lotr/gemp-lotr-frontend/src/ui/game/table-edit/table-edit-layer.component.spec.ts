import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardSearch, SearchPage } from '../../../core/game/table-edit/card-search';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { EMPTY_FILTER } from '../../../core/card-filter/filter-fields';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { providePathTesting } from '../../../testing/table-edit-piles';
import { CardPicker } from './card-picker';
import { EditPopups } from './edit-popups';
import { SearchWindowComponent } from './search-window.component';
import { TableEditLayerComponent } from './table-edit-layer.component';
import { PuzzleLayerComponent } from './puzzle-layer.component';
import { PuzzlePopups } from './puzzle-popups';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { fakePuzzle } from '../../../testing/table-edit';
import { GameSession } from '../../../core/game/session/game-session.service';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';

describe('TableEditLayerComponent', () => {
  let edit: FakeTableEdit;
  let popups: EditPopups;

  function render() {
    edit = fakeTableEdit();
    const page = signal<SearchPage | null>(null);
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        EditPopups,
        RecentCards,
        CardPicker,
        { provide: TableEdit, useValue: edit },
        ...providePathTesting(),
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
            page,
            bound: signal(null),
            omitted: signal([]),
            open: vi.fn(),
          },
        },
      ],
    });
    popups = TestBed.inject(EditPopups);
    const fixture = TestBed.createComponent(TableEditLayerComponent);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  afterEach(() => localStorage.clear());

  it("adds a card found in the search window to the window's place, within the place's card types", async () => {
    const { fixture } = render();
    popups.openSearch({ seat: 'shadow', zone: 'minions' });
    fixture.detectChanges();
    const search = fixture.debugElement.query((node) => node.componentInstance instanceof SearchWindowComponent)
      .componentInstance as SearchWindowComponent;
    expect(search.bound()).toEqual({ filter: 'cardType:MINION', label: 'Minions' });
    expect(search.heading()).toBe('Add to Minions');
    search.chosen.emit({ blueprintId: '1_178', name: 'Goblin Runner', type: 'MINION', culture: 'moria', site: false });
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'add', blueprint: '1_178', to: 'shadow.minions' }], { x: 0, y: 0 });
    expect(popups.search()).toBeNull();
    expect(TestBed.inject(RecentCards).list()[0]).toEqual({
      blueprintId: '1_178',
      name: 'Goblin Runner',
      type: 'MINION',
    });
    // (refused: it stays open, and says why in it)
    popups.openSearch({ seat: 'shadow', zone: 'minions' });
    edit.apply.mockResolvedValueOnce(false);
    edit.problem.set({ text: 'No room', at: { x: 0, y: 0 } });
    fixture.detectChanges();
    expect(search.said()).toBe('No room');
    search.chosen.emit({ blueprintId: '1_178', name: 'Goblin Runner', type: 'MINION', culture: 'moria', site: false });
    await fixture.whenStable();
    expect(popups.search()).not.toBeNull();
    search.closed.emit();
    expect(popups.search()).toBeNull();
    expect(search.heading()).toBe('Add to Minions');
  });

  it("says a control's refusal where it was asked for (the edit bar says the others)", () => {
    const { fixture, el } = render();
    edit.problem.set({ text: 'Frodo cannot be deleted', at: { x: 40, y: 50 } });
    fixture.detectChanges();
    const problem = el.querySelector<HTMLElement>('.gd-edit-problem')!;
    expect(problem.textContent.trim()).toBe('Frodo cannot be deleted');
    expect(problem.getAttribute('role')).toBe('alert');
    edit.problem.set({ text: 'The game is not stopped.', at: null });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-problem')).toBeNull();
  });

  it("draws a puzzle's own layer (its slots' chooser, search window and strip) for a puzzle only", () => {
    TestBed.overrideComponent(PuzzleLayerComponent, { set: { imports: [], template: '' } });
    TestBed.configureTestingModule({
      providers: [
        PuzzlePopups,
        { provide: PuzzleBackend, useValue: fakePuzzle() },
        { provide: GameSession, useValue: { model: () => ({ cards: {} }) } },
        PageLifetime,
      ],
    });
    const { fixture, el } = render();
    expect(el.querySelector('gd-puzzle-layer')).toBeNull();
    edit.level.set('puzzle');
    fixture.detectChanges();
    expect(el.querySelector('gd-puzzle-layer')).not.toBeNull();
  });
});
