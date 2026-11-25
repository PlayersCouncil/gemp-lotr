import { TestBed } from '@angular/core/testing';
import { FilledSlot } from '../../../core/game/table-edit/puzzle-backend';
import { readPuzzleSlots } from '../../../core/game/table-edit/puzzle-slots';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit, PUZZLE_SLOTS } from '../../../testing/table-edit';
import { PuzzlePopups } from './puzzle-popups';
import { PuzzleSlotComponent } from './puzzle-slot.component';

describe('PuzzleSlotComponent', () => {
  let edit: FakeTableEdit;
  let popups: PuzzlePopups;
  const [condition] = readPuzzleSlots(PUZZLE_SLOTS);

  function render(filled: FilledSlot, place = false) {
    edit = fakeTableEdit({ level: 'puzzle', tools: [] });
    TestBed.configureTestingModule({
      providers: [...provideTestCardInteractions(), PuzzlePopups, { provide: TableEdit, useValue: edit }],
    });
    popups = TestBed.inject(PuzzlePopups);
    const fixture = TestBed.createComponent(PuzzleSlotComponent);
    fixture.componentRef.setInput('slot', filled);
    fixture.componentRef.setInput('place', place);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('is an open spot with its label while empty; pressing it opens its chooser, and again closes it', () => {
    const { fixture, el } = render({ slot: condition!, card: null });
    const slot = el.querySelector<HTMLElement>('.gd-puzzle-slot')!;
    expect(slot.dataset).toMatchObject({ slot: 'condition', zone: 'support', seat: 'human' });
    expect(slot.hasAttribute('data-filled')).toBe(false);
    expect(el.querySelector('.gd-puzzle-slot__label')?.textContent).toBe('a Rohan condition');
    expect(el.querySelector('.gd-puzzle-slot__comes')).toBeNull();
    expect(el.querySelector('.gd-puzzle-slot__place')).toBeNull();
    const button = el.querySelector<HTMLButtonElement>('button.gd-open-spot')!;
    expect(button.getAttribute('aria-label')).toBe('Fill a Rohan condition');
    button.click();
    fixture.detectChanges();
    expect(popups.chooser()?.slot).toBe('condition');
    expect(button.getAttribute('aria-expanded')).toBe('true');
    button.click();
    expect(popups.chooser()).toBeNull();
  });

  it('is off outside edit mode', () => {
    const { fixture, el } = render({ slot: condition!, card: null });
    edit.mode.set('play');
    fixture.detectChanges();
    const button = el.querySelector<HTMLButtonElement>('button.gd-open-spot')!;
    expect(button.disabled).toBe(true);
    (fixture.componentInstance as unknown as { press(): void }).press();
    expect(popups.chooser()).toBeNull();
  });

  it('shows the card in it (pressing it opens the chooser), what it comes with, and where it goes', () => {
    const { el } = render(
      {
        slot: { ...condition!, zone: 'deck', position: 3, comes: { tokens: 3, wounds: 1 } },
        card: { blueprintId: '5_87', name: 'Parapet' },
      },
      true,
    );
    expect(el.querySelector('.gd-puzzle-slot')?.hasAttribute('data-filled')).toBe(true);
    const card = el.querySelector<HTMLButtonElement>('button.gd-card.gd-puzzle-slot__card')!;
    expect(card.dataset['blueprint']).toBe('5_87');
    expect(card.title).toBe('a Rohan condition: Parapet');
    expect(el.querySelector('.gd-puzzle-slot__comes')?.textContent).toBe('3 tokens, 1 wound');
    expect(el.querySelector('.gd-puzzle-slot__place')?.textContent).toBe('Deck, 3rd');
    card.click();
    expect(popups.chooser()?.slot).toBe('condition');
  });
});
