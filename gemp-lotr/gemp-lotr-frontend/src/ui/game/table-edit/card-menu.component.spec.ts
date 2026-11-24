import { TestBed } from '@angular/core/testing';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardView } from '../../../core/cards/card-view';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { CardMenuComponent } from './card-menu.component';
import { CardPicker } from './card-picker';
import { EditPopups } from './edit-popups';

/** Boromir's card on the table (SANDBOX_STATE: card 30, a companion with a wound, two Gondor tokens, Strength +2). */
const BOROMIR = { blueprintId: '1_97', ref: { cardId: '30' } } as CardView;

describe('CardMenuComponent', () => {
  let edit: FakeTableEdit;
  let popups: EditPopups;
  let pickCard: ReturnType<typeof vi.fn>;
  let card: HTMLButtonElement;

  function render(cardId = '30', point: { x: number; y: number } | null = { x: 40, y: 50 }) {
    edit = fakeTableEdit();
    pickCard = vi.fn(() => Promise.resolve('26'));
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        EditPopups,
        { provide: TableEdit, useValue: edit },
        { provide: CardPicker, useValue: { pickCard } },
      ],
    });
    popups = TestBed.inject(EditPopups);
    card = document.createElement('button');
    document.body.append(card);
    const fixture = TestBed.createComponent(CardMenuComponent);
    popups.openMenu(cardId, { ...BOROMIR, ref: { cardId } }, { element: card, point });
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const entries = () =>
      [...el.querySelectorAll<HTMLElement>('.gd-edit-menu:not([data-submenu]) > [data-menu]')].map(
        (entry) => entry.dataset['menu'],
      );
    const entry = (name: string) => el.querySelector<HTMLButtonElement>(`[data-menu="${name}"]`)!;
    const open = (name: string) => {
      entry(name).dispatchEvent(new Event('pointerenter'));
      fixture.detectChanges();
      return el.querySelector<HTMLElement>(`[data-submenu="${name}"]`)!;
    };
    return { fixture, el, entries, entry, open };
  }

  afterEach(() => card.remove());

  it("titles the card, and offers a companion's entries; Info shows its details", () => {
    const { el, entries, entry } = render();
    expect(el.querySelector('.gd-edit-menu__title')?.textContent).toBe('Boromir, Son of Denethor');
    expect(el.querySelector('.gd-edit-menu')?.getAttribute('data-card-id')).toBe('30');
    expect(entries()).toEqual(['info', 'send', 'stack', 'add', 'modify', 'copy', 'delete']);
    const show = vi.spyOn(TestBed.inject(CardInfoService), 'show');
    entry('info').click();
    expect(show).toHaveBeenCalledWith({ ...BOROMIR, ref: { cardId: '30' } });
    expect(popups.menu()).toBeNull();
  });

  it('is only Info for a card the level does not know, and only what the tools allow', () => {
    expect(render('99').entries()).toEqual(['info']);
    TestBed.resetTestingModule();
    card.remove();
    const { fixture, entries } = render('31');
    // (nothing of the Modify list suits a possession here)
    expect(entries()).toEqual(['info', 'send', 'attach', 'stack', 'add', 'copy', 'delete']);
    edit.tools.set(['copy']);
    fixture.detectChanges();
    expect(entries()).toEqual(['info', 'copy']);
  });

  it("sends it to one of its owner's places", async () => {
    const { fixture, open } = render();
    const send = open('send');
    expect([...send.querySelectorAll('button')].map((button) => button.textContent.trim())).toEqual([
      'Hand',
      'Deck top',
      'Deck bottom',
      'Discard',
      'Dead pile',
      'Removed',
    ]);
    send.querySelector<HTMLButtonElement>('[data-zone="discard"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'move', cardId: 30, to: 'freeps.discard' }], { x: 40, y: 50 });
    expect(popups.menu()).toBeNull();
  });

  it('adds wounds and tokens by the amount stepped to, heals, and takes tokens off', async () => {
    const { fixture, open } = render();
    const add = open('add');
    const step = (way: string) => {
      add.querySelector<HTMLButtonElement>(`[data-step="${way}"]`)!.click();
      fixture.detectChanges();
    };
    const amount = add.querySelector<HTMLInputElement>('.gd-edit-menu__amount input')!;
    expect(add.querySelector<HTMLButtonElement>('[data-step="down"]')!.disabled).toBe(true);
    step('up');
    step('up');
    expect(amount.value).toBe('3');
    step('down');
    amount.value = '500';
    amount.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(amount.value).toBe('99');
    amount.value = 'x';
    amount.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(amount.value).toBe('1');
    step('up');
    const tokens = [...add.querySelectorAll<HTMLElement>('.gd-edit-menu__token')];
    const said = (token: HTMLElement) => [...token.children].map((part) => part.textContent.trim()).join(' ');
    expect(tokens.map((token) => `${token.dataset['culture']}: ${said(token)}`)).toEqual([
      'gondor: Gondor 2 + −',
      'dwarven: Dwarven 0 + −',
      'elven: Elven 0 + −',
      'shire: Shire 0 + −',
      'moria: Moria 0 + −',
      'uruk_hai: Uruk-hai 0 + −',
    ]);
    expect(tokens[1]!.querySelector<HTMLButtonElement>('[data-token="remove"]')!.disabled).toBe(true);
    add.querySelector<HTMLButtonElement>('[data-add="wound"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'wounds', cardId: 30, amount: 2 }], { x: 40, y: 50 });

    const again = () => {
      popups.openMenu('30', BOROMIR, { element: card, point: null });
      fixture.detectChanges();
      return open('add');
    };
    // (each card's menu starts its amount at 1 again)
    again().querySelector<HTMLButtonElement>('[data-add="heal"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'wounds', cardId: 30, amount: -1 }], { x: 0, y: 0 });
    again().querySelector<HTMLButtonElement>('[data-culture="gondor"] [data-token="remove"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'tokens', cardId: 30, amount: -1, culture: 'gondor' }], {
      x: 0,
      y: 0,
    });
    again().querySelector<HTMLButtonElement>('[data-culture="elven"] [data-token="add"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'tokens', cardId: 30, amount: 1, culture: 'elven' }], {
      x: 0,
      y: 0,
    });
  });

  it('puts a modifier on for a time, and takes the ones on it off', async () => {
    const { fixture, open } = render();
    let modify = open('modify');
    const what = modify.querySelector<HTMLSelectElement>('select[data-field="what"]')!;
    expect([...what.options].map((option) => option.textContent)).toEqual(['Strength', 'Resistance', 'Damage']);
    const amount = modify.querySelector<HTMLInputElement>('input[data-field="amount"]')!;
    amount.value = '-2';
    amount.dispatchEvent(new Event('change'));
    const until = modify.querySelector<HTMLSelectElement>('select[data-field="until"]')!;
    expect([...until.options].map((option) => option.textContent)).toEqual([
      'This phase',
      'This turn',
      'Until removed',
    ]);
    until.value = 'turn';
    until.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    modify.querySelector<HTMLButtonElement>('[data-action="put"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith(
      [{ op: 'modify', cardId: 30, what: 'strength', amount: -2, until: 'turn' }],
      { x: 40, y: 50 },
    );

    popups.openMenu('30', BOROMIR, { element: card, point: null });
    fixture.detectChanges();
    modify = open('modify');
    const select = modify.querySelector<HTMLSelectElement>('select[data-field="what"]')!;
    select.value = 'damage';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(modify.querySelector('input[data-field="amount"]')?.getAttribute('min')).toBe('1');
    const zero = modify.querySelector<HTMLInputElement>('input[data-field="amount"]')!;
    zero.value = '0';
    zero.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    modify.querySelector<HTMLButtonElement>('[data-action="put"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'modify', cardId: 30, what: 'damage', amount: 1 }], {
      x: 0,
      y: 0,
    });

    popups.openMenu('30', BOROMIR, { element: card, point: null });
    fixture.detectChanges();
    modify = open('modify');
    const on = modify.querySelector<HTMLElement>('.gd-edit-menu__modifier')!;
    expect(on.dataset).toMatchObject({ modifier: '1', until: 'turn' });
    expect([...on.children].map((part) => part.textContent.trim())).toEqual(['Strength +2', 'This turn', '×']);
    expect(modify.querySelector('[data-action="unmodify-all"]')).toBeNull();
    on.querySelector<HTMLButtonElement>('[data-action="unmodify"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'unmodify', id: 1 }], { x: 0, y: 0 });
  });

  it('takes every modifier off at once when it has several; the pointer on a plain entry closes a submenu', () => {
    const { fixture, el, entry, open } = render();
    const board = edit.board()!;
    edit.board.set({
      ...board,
      cards: board.cards.map((card) =>
        card.alias === 'boromir'
          ? {
              ...card,
              modifiers: [...card.modifiers, { id: 2, what: 'damage', amount: 1, until: 'removed', text: 'Damage +1' }],
            }
          : card,
      ),
    });
    fixture.detectChanges();
    for (const plain of ['info', 'stack', 'copy', 'delete']) {
      open('send');
      entry(plain).dispatchEvent(new Event('pointerenter'));
      fixture.detectChanges();
      expect(el.querySelector('[data-submenu]')).toBeNull();
    }
    const modify = open('modify');
    // (the same entry again leaves it open)
    entry('modify').dispatchEvent(new Event('pointerenter'));
    fixture.detectChanges();
    expect(modify.querySelectorAll('.gd-edit-menu__modifier')).toHaveLength(2);
    modify.querySelector<HTMLButtonElement>('[data-action="unmodify-all"]')!.click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'unmodify', cardId: 30 }], { x: 40, y: 50 });
  });

  it('copies and deletes it', () => {
    const { fixture, entry } = render();
    entry('copy').click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'copy', cardId: 30 }], { x: 40, y: 50 });
    popups.openMenu('30', BOROMIR, { element: card, point: { x: 40, y: 50 } });
    fixture.detectChanges();
    entry('delete').click();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'delete', cardId: 30 }], { x: 40, y: 50 });
  });

  it('attaches and stacks it on a card picked on the table; nothing when none is picked', async () => {
    const { fixture, entry } = render('31');
    entry('attach').click();
    await fixture.whenStable();
    expect(pickCard).toHaveBeenCalledWith('Attach Armor to…', '31');
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'attach', cardId: 31, toId: 26 }], { x: 40, y: 50 });
    popups.openMenu('31', BOROMIR, { element: card, point: null });
    fixture.detectChanges();
    pickCard.mockResolvedValueOnce(null);
    entry('stack').click();
    await fixture.whenStable();
    expect(pickCard).toHaveBeenLastCalledWith('Stack Armor on…', '31');
    expect(edit.apply).toHaveBeenCalledTimes(1);
  });

  it('goes by the keyboard: Up and Down, Right into a submenu, Left back, Escape out to the card', () => {
    const { fixture, el, entry } = render('30', null);
    const key = (target: Element, key: string) => {
      target.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true }));
      fixture.detectChanges();
    };
    expect(document.activeElement).toBe(entry('info'));
    key(document.activeElement!, 'ArrowDown');
    expect(document.activeElement).toBe(entry('send'));
    key(document.activeElement!, 'ArrowRight');
    expect(el.querySelector('[data-submenu="send"]')).not.toBeNull();
    key(el.querySelector('[data-submenu="send"] button')!, 'ArrowLeft');
    expect(el.querySelector('[data-submenu]')).toBeNull();
    expect(document.activeElement).toBe(entry('send'));
    key(entry('info'), 'ArrowRight');
    expect(el.querySelector('[data-submenu]')).toBeNull();
    key(document.activeElement!, 'Escape');
    expect(popups.menu()).toBeNull();
    expect(document.activeElement).toBe(card);
  });

  it('closes on a press outside, and goes back to a plain entry when the pointer does', () => {
    const { fixture, el, entry, open } = render();
    open('send');
    entry('copy').dispatchEvent(new Event('pointerenter'));
    fixture.detectChanges();
    expect(el.querySelector('[data-submenu]')).toBeNull();
    el.querySelector('.gd-edit-backdrop')!.dispatchEvent(new Event('pointerdown'));
    expect(popups.menu()).toBeNull();
  });
});
