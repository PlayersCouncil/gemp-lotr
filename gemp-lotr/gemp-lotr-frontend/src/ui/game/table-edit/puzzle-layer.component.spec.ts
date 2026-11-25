import { TestBed } from '@angular/core/testing';
import { CardSearch, FoundCard } from '../../../core/game/table-edit/card-search';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakePuzzle, FakePuzzle, fakeTableEdit, FakeTableEdit, PUZZLE_SLOTS } from '../../../testing/table-edit';
import { PuzzleLayerComponent } from './puzzle-layer.component';
import { PuzzlePopups } from './puzzle-popups';

const PARAPET: FoundCard = { blueprintId: '5_87', name: 'Parapet', type: 'CONDITION', culture: 'rohan', site: false };

describe('PuzzleLayerComponent', () => {
  let edit: FakeTableEdit;
  let puzzle: FakePuzzle;
  let popups: PuzzlePopups;

  function render(slots: unknown[] = PUZZLE_SLOTS) {
    edit = fakeTableEdit({ level: 'puzzle', tools: [] });
    puzzle = fakePuzzle(slots);
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        PuzzlePopups,
        RecentCards,
        { provide: TableEdit, useValue: edit },
        { provide: PuzzleBackend, useValue: puzzle },
        { provide: CardSearch, useValue: {} },
      ],
    });
    // (the search window and the chooser have specs of their own: here, what the layer gives them)
    TestBed.overrideComponent(PuzzleLayerComponent, {
      set: {
        imports: [],
        template: `<i class="bound">{{ bound()?.pool }}|{{ bound()?.filter }}|{{ bound()?.label }}|{{ heading() }}|{{ said() }}</i>
          @for (filled of strip(); track filled.slot.id) {<b>{{ filled.slot.id }}</b>}`,
      },
    });
    popups = TestBed.inject(PuzzlePopups);
    const fixture = TestBed.createComponent(PuzzleLayerComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el, layer: fixture.componentInstance as unknown as { chosen(found: FoundCard): Promise<void> } };
  }

  afterEach(() => localStorage.clear());

  it("holds the search window to the open slot's bound, titled by it, with its refusal", () => {
    const { fixture, el } = render();
    expect(el.querySelector('.bound')?.textContent).toBe('|||Search|');
    popups.openSearch('condition');
    edit.problem.set({ text: 'Not that one.', at: null });
    fixture.detectChanges();
    expect(el.querySelector('.bound')?.textContent).toBe(
      'set:1-6|culture:ROHAN cardtype:CONDITION|a Rohan condition|Fill: a Rohan condition|Not that one.',
    );
  });

  it('puts a card found in the slot, remembers it and closes; a refusal keeps the window open', async () => {
    const { layer } = render();
    await layer.chosen(PARAPET);
    expect(edit.apply).not.toHaveBeenCalled();
    popups.openSearch('condition');
    await layer.chosen(PARAPET);
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'pick', slot: 'condition', blueprint: '5_87' }], null);
    expect(TestBed.inject(RecentCards).list()[0]).toEqual({ blueprintId: '5_87', name: 'Parapet', type: 'CONDITION' });
    expect(popups.search()).toBeNull();
    popups.openSearch('condition');
    edit.apply.mockResolvedValueOnce(false);
    await layer.chosen(PARAPET);
    expect(popups.search()).toBe('condition');
  });

  it('lists in the strip the slots the table has no row for', () => {
    const { el } = render([...PUZZLE_SLOTS, { id: 'tale1', zone: 'deck', position: 3 }, { id: 'site2', zone: 'site' }]);
    expect([...el.querySelectorAll('b')].map((b) => b.textContent)).toEqual(['tale1', 'site2']);
  });
});
