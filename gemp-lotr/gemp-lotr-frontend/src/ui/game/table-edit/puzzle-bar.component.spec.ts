import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakePuzzle, FakePuzzle, fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { PuzzleBarComponent } from './puzzle-bar.component';
import { PuzzlePopups } from './puzzle-popups';

describe('PuzzleBarComponent', () => {
  let edit: FakeTableEdit;
  let puzzle: FakePuzzle;
  let popups: PuzzlePopups;
  const info = signal<{ author: boolean; draftTest: boolean } | null>(null);
  const guide = { info, askRestart: vi.fn(), changeCards: vi.fn() };

  function render() {
    edit = fakeTableEdit({ level: 'puzzle', tools: [] });
    puzzle = fakePuzzle();
    info.set({ author: false, draftTest: false });
    TestBed.configureTestingModule({
      providers: [
        PuzzlePopups,
        { provide: TableEdit, useValue: edit },
        { provide: PuzzleBackend, useValue: puzzle },
        { provide: ScenarioGuide, useValue: guide },
      ],
    });
    popups = TestBed.inject(PuzzlePopups);
    const fixture = TestBed.createComponent(PuzzleBarComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el, submit: () => el.querySelector<HTMLButtonElement>('button[data-puzzle-bar="submit"]') };
  }

  it('says Choosing; Submit is off, saying which slots are empty, until every slot has a card', () => {
    const { fixture, el, submit } = render();
    expect(el.dataset['mode']).toBe('edit');
    expect(el.querySelector('.gd-edit-bar__mode')?.textContent).toBe('Choosing');
    expect(submit()!.disabled).toBe(true);
    expect(submit()!.title).toBe('Still empty: a Rohan condition, a Rohan ally');
    expect(el.querySelector('[data-scenario-menu="restart"]')).toBeNull();
    puzzle.picks.set({
      condition: { blueprintId: '5_87', name: 'Parapet' },
      ally: { blueprintId: '4_262', name: 'Aldor' },
    });
    fixture.detectChanges();
    expect(submit()!.disabled).toBe(false);
    expect(submit()!.title).toBe('Play with these cards');
    submit()!.click();
    expect(edit.submit).toHaveBeenCalled();
    edit.busy.set(true);
    fixture.detectChanges();
    expect(submit()!.disabled).toBe(true);
    expect(el.dataset['busy']).toBe('');
  });

  it('says a refusal of Submit under it, not one the search window says', () => {
    const { fixture, el } = render();
    edit.problem.set({ text: 'Not every slot has a card it takes.', at: null });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__problem')?.textContent).toBe('Not every slot has a card it takes.');
    popups.openSearch('ally');
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__problem')).toBeNull();
    popups.closeSearch();
    edit.problem.set({ text: 'by the slot', at: { x: 1, y: 2 } });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__problem')).toBeNull();
  });

  it('offers Start again and Change cards in play; not Change cards in a draft’s game', () => {
    const { fixture, el } = render();
    edit.mode.set('play');
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__mode')).toBeNull();
    expect(el.querySelector('button[data-puzzle-bar="submit"]')).toBeNull();
    el.querySelector<HTMLButtonElement>('[data-scenario-menu="restart"]')!.click();
    expect(guide.askRestart).toHaveBeenCalled();
    el.querySelector<HTMLButtonElement>('[data-puzzle-bar="change"]')!.click();
    expect(guide.changeCards).toHaveBeenCalled();
    info.set({ author: true, draftTest: false });
    fixture.detectChanges();
    expect(el.querySelector('[data-puzzle-bar="change"]')).toBeNull();
    info.set(null);
    fixture.detectChanges();
    expect(el.querySelector('[data-puzzle-bar="change"]')).toBeNull();
  });
});
