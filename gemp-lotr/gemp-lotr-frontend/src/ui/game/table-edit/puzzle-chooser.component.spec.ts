import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { throwError } from 'rxjs';
import { GameSession } from '../../../core/game/session/game-session.service';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakePuzzle, FakePuzzle, fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { PuzzleChooserComponent } from './puzzle-chooser.component';
import { PuzzlePopups } from './puzzle-popups';

describe('PuzzleChooserComponent', () => {
  let edit: FakeTableEdit;
  let puzzle: FakePuzzle;
  let popups: PuzzlePopups;
  let recent: RecentCards;
  let spot: HTMLButtonElement;
  const cards = signal<Record<string, { blueprintId: string; index: number | null }>>({});

  function render(slot = 'condition') {
    edit = fakeTableEdit({ level: 'puzzle', tools: [] });
    puzzle = fakePuzzle();
    puzzle.refuse.add('4_262');
    cards.set({
      '1': { blueprintId: '5_87', index: null },
      '2': { blueprintId: '5_87', index: null },
      '3': { blueprintId: '4_365', index: null },
      '4': { blueprintId: '4_339', index: 3 },
      '5': { blueprintId: '-1_1', index: null },
    });
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        PageLifetime,
        PuzzlePopups,
        RecentCards,
        { provide: TableEdit, useValue: edit },
        { provide: PuzzleBackend, useValue: puzzle },
        { provide: GameSession, useValue: { model: () => ({ cards: cards() }) } },
      ],
    });
    popups = TestBed.inject(PuzzlePopups);
    recent = TestBed.inject(RecentCards);
    recent.use({ blueprintId: '4_262', name: 'Aldor', type: 'ALLY' });
    recent.use({ blueprintId: '1_50', name: 'Legolas, Greenleaf', type: 'COMPANION' });
    recent.use({ blueprintId: '5_79', name: 'Armory', type: 'CONDITION' });
    spot = document.createElement('button');
    document.body.append(spot);
    const fixture = TestBed.createComponent(PuzzleChooserComponent);
    popups.openChooser(slot, spot);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const items = (kind: string) =>
      [...el.querySelectorAll<HTMLButtonElement>(`[data-chooser="${kind}"]`)].map((item) => item.textContent.trim());
    return { fixture, el, items };
  }

  afterEach(() => {
    spot.remove();
    localStorage.clear();
  });

  it('lists only the cards the server says fit the slot, each asked about once; Search…; no Empty while empty', () => {
    const { el, items } = render();
    const menu = el.querySelector<HTMLElement>('.gd-edit-chooser')!;
    expect(menu.dataset['slot']).toBe('condition');
    expect(menu.getAttribute('aria-label')).toBe('a Rohan condition');
    // (Legolas, a companion, is no card for a support slot: not asked about; Aldor is refused)
    expect(items('recent')).toEqual(['Armory']);
    expect(items('scenario')).toEqual(['Card 5_87', 'Card 4_365']);
    expect(items('search')).toEqual(['Search…']);
    expect(items('empty')).toEqual([]);
    expect(puzzle.verdict.mock.calls.map(([, blueprint]) => blueprint)).toEqual(['5_79', '4_262', '5_87', '4_365']);
    expect(el.querySelector('[data-checking]')).toBeNull();
  });

  it('puts a card chosen in the slot and closes; a refusal keeps it open; Recent learns cards of known kinds', async () => {
    const { fixture, el } = render();
    el.querySelector<HTMLButtonElement>('[data-chooser="recent"][data-blueprint="5_79"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'pick', slot: 'condition', blueprint: '5_79' }], { x: 0, y: 0 });
    expect(popups.chooser()).toBeNull();
    expect(recent.list()[0]?.blueprintId).toBe('5_79');

    popups.openChooser('condition', spot);
    fixture.detectChanges();
    edit.apply.mockResolvedValueOnce(false);
    el.querySelector<HTMLButtonElement>('[data-chooser="scenario"][data-blueprint="4_365"]')!.click();
    await fixture.whenStable();
    expect(popups.chooser()?.slot).toBe('condition');
    // (a card of no known kind is not remembered)
    edit.apply.mockResolvedValueOnce(true);
    el.querySelector<HTMLButtonElement>('[data-chooser="scenario"][data-blueprint="4_365"]')!.click();
    await fixture.whenStable();
    expect(recent.list().some((card) => card.blueprintId === '4_365')).toBe(false);
  });

  it('empties a filled slot, and opens the search for it', async () => {
    const { fixture, el, items } = render();
    puzzle.picks.set({ condition: { blueprintId: '5_87', name: 'Parapet' } });
    fixture.detectChanges();
    expect(items('empty')).toEqual(['Empty']);
    el.querySelector<HTMLButtonElement>('[data-chooser="empty"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'pick', slot: 'condition', blueprint: null }], null);
    expect(popups.chooser()).toBeNull();

    popups.openChooser('condition', spot);
    fixture.detectChanges();
    el.querySelector<HTMLButtonElement>('[data-chooser="search"]')!.click();
    expect(popups.search()).toBe('condition');
    expect(popups.chooser()).toBeNull();
  });

  it("asks a site slot about the path's sites only, and closes on Escape or a press outside", () => {
    const sites = [{ id: 'site2', zone: 'site', label: 'a site 2' }];
    const { fixture, el } = render();
    puzzle.slots.set(fakePuzzle(sites).slots());
    popups.openChooser('site2', spot);
    fixture.detectChanges();
    expect(puzzle.verdict).toHaveBeenCalledWith('site2', '4_339');
    expect(puzzle.verdict).not.toHaveBeenCalledWith('site2', '4_365');
    el.querySelector<HTMLElement>('.gd-edit-chooser')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(popups.chooser()).toBeNull();
    popups.openChooser('site2', spot);
    fixture.detectChanges();
    el.querySelector<HTMLElement>('.gd-edit-backdrop')!.dispatchEvent(new Event('pointerdown'));
    expect(popups.chooser()).toBeNull();
  });

  it('lists nothing it could not ask about, and nothing with no slot open', () => {
    const { fixture, el, items } = render();
    puzzle.verdict.mockReturnValue(throwError(() => new Error('down')));
    puzzle.picks.set({ ally: { blueprintId: '4_262', name: 'Aldor' } });
    popups.closeChooser();
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-chooser')).toBeNull();
    popups.openChooser('ally', spot);
    fixture.detectChanges();
    expect(items('recent')).toEqual([]);
    expect(el.querySelector('[data-checking]')).toBeNull();
  });
});
