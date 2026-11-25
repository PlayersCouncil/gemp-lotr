import { TestBed } from '@angular/core/testing';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { EditCounter } from '../../../core/game/table-edit/edit-counters';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { CounterBoxComponent } from './counter-box.component';
import { CounterPopups } from './counter-popups';
import { instanceOf } from '../../../testing/matchers';

describe('CounterBoxComponent', () => {
  let edit: FakeTableEdit;
  let popups: CounterPopups;
  let opener: HTMLButtonElement;

  async function render(counter: EditCounter = 'twilight') {
    edit = fakeTableEdit();
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    TestBed.configureTestingModule({ providers: [CounterPopups, { provide: TableEdit, useValue: edit }] });
    popups = TestBed.inject(CounterPopups);
    opener = document.createElement('button');
    document.body.append(opener);
    const fixture = TestBed.createComponent(CounterBoxComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const box = () => el.querySelector<HTMLElement>('.gd-edit-counter__box');
    const field = () => el.querySelector<HTMLInputElement>('input[data-field="set"]')!;
    const button = (what: string) => el.querySelector<HTMLButtonElement>(`.gd-edit-counter__box ${what}`)!;
    const open = async (which: EditCounter = counter) => {
      popups.toggle(which, opener);
      fixture.detectChanges();
      await fixture.whenStable();
    };
    await open();
    return { fixture, el, box, field, button, open };
  }

  afterEach(() => opener.remove());

  it('shows nothing until a counter is opened, then its box with the counter in it to set, focused', async () => {
    const { fixture, box, field } = await render();
    expect(box()?.dataset['counter']).toBe('twilight');
    expect(box()?.querySelector('.gd-edit-counter__label')?.textContent).toBe('Twilight');
    expect(field().value).toBe('3');
    expect(document.activeElement).toBe(field());
    popups.close();
    fixture.detectChanges();
    expect(box()).toBeNull();
  });

  it('steps the counter by one at once, and stays open', async () => {
    const { fixture, box, button } = await render('burdens');
    button('[data-step="up"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'burdens', amount: 1 }], instanceOf(Object));
    button('[data-step="down"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'burdens', amount: -1 }], instanceOf(Object));
    fixture.detectChanges();
    expect(box()).not.toBeNull();
  });

  it("sets the number typed (Set or Enter), which the counter changing meanwhile doesn't overwrite", async () => {
    const { fixture, field, button, open } = await render('threats');
    expect(button('[data-step="down"]').disabled).toBe(true);
    field().value = '2';
    field().dispatchEvent(new Event('input'));
    edit.board.update((board) => board && { ...board, threats: 1 });
    fixture.detectChanges();
    expect(field().value).toBe('2');
    edit.apply.mockResolvedValueOnce(false);
    button('[data-action="set"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'threats', set: 2 }], instanceOf(Object));
    fixture.detectChanges();
    expect(field().value).toBe('2');
    field().dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await fixture.whenStable();
    fixture.detectChanges();
    expect(field().value).toBe('1');
    // Not a number from 0 to 99: no Set
    field().value = '120';
    field().dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(button('[data-action="set"]').disabled).toBe(true);
    edit.apply.mockClear();
    field().dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(edit.apply).not.toHaveBeenCalled();
    // Another counter opened: a fresh box, nothing typed
    await open('twilight');
    expect(field().value).toBe('3');
  });

  it('closes on Escape anywhere (the focus back on the number), or a press outside', async () => {
    const { fixture, el, box, open } = await render();
    document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    expect(box()).toBeNull();
    expect(document.activeElement).toBe(opener);
    document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await open();
    el.querySelector('.gd-edit-backdrop')!.dispatchEvent(new Event('pointerdown'));
    fixture.detectChanges();
    expect(box()).toBeNull();
    await open();
    const press = new MouseEvent('contextmenu', { cancelable: true });
    el.querySelector('.gd-edit-backdrop')!.dispatchEvent(press);
    expect(press.defaultPrevented).toBe(true);
    expect(popups.open()).toBeNull();
  });
});
