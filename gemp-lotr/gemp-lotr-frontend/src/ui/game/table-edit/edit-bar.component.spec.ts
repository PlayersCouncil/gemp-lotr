import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { CardPicker } from './card-picker';
import { EditBarComponent } from './edit-bar.component';

describe('EditBarComponent', () => {
  let edit: FakeTableEdit;
  const told = signal<string | null>(null);

  function render() {
    edit = fakeTableEdit();
    told.set(null);
    TestBed.configureTestingModule({
      providers: [
        { provide: TableEdit, useValue: edit },
        { provide: CardPicker, useValue: { told } },
      ],
    });
    const fixture = TestBed.createComponent(EditBarComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const button = (name: string) => el.querySelector<HTMLButtonElement>(`button[data-edit-bar="${name}"]`);
    return { fixture, el, button };
  }

  it('says Editing, with Start and Reset, in edit mode', () => {
    const { el, button } = render();
    expect(el.dataset['mode']).toBe('edit');
    expect(el.querySelector('.gd-edit-bar__mode')?.textContent).toBe('Editing');
    expect(button('stop')).toBeNull();
    button('start')!.click();
    expect(edit.start).toHaveBeenCalled();
    button('reset')!.click();
    expect(edit.reset).toHaveBeenCalledWith();
  });

  it('says Playing, with Stop when the game can stop, in play mode; nothing pressable while busy', () => {
    const { fixture, el, button } = render();
    edit.mode.set('play');
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__mode')?.textContent).toBe('Playing');
    expect(button('start')).toBeNull();
    expect(button('stop')!.disabled).toBe(true);
    expect(button('stop')!.title).toContain("phase's own prompt");
    edit.canStop.set(true);
    fixture.detectChanges();
    button('stop')!.click();
    expect(edit.stop).toHaveBeenCalled();
    edit.busy.set(true);
    fixture.detectChanges();
    expect(el.hasAttribute('data-busy')).toBe(true);
    expect(button('stop')!.disabled).toBe(true);
    expect(button('reset')!.disabled).toBe(true);
  });

  it('says what to do while a card is picked, and a refusal of its own buttons under it', () => {
    const { fixture, el, button } = render();
    told.set('Attach Armor to…');
    fixture.detectChanges();
    expect(el.querySelector('[data-picking]')?.textContent).toBe('Attach Armor to…');
    expect(button('start')).toBeNull();
    told.set(null);
    edit.problem.set({ text: 'The game is not stopped.', at: null });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__problem')?.textContent).toBe('The game is not stopped.');
    // (a control's own refusal is said by the control)
    edit.problem.set({ text: 'No.', at: { x: 1, y: 2 } });
    fixture.detectChanges();
    expect(el.querySelector('.gd-edit-bar__problem')).toBeNull();
  });
});
