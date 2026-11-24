import { TestBed } from '@angular/core/testing';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakeTableEdit, SANDBOX_STATE } from '../../../testing/table-edit';
import { CardPicker } from './card-picker';

/** A card on the table at a box, and the edit controls' popup lying over part of the page. */
const PAGE = `
  <div class="gd-game__table">
    <button data-card-id="30" data-blueprint-id="1_97"></button>
    <button data-card-id="77" data-blueprint-id="1_178"></button>
  </div>
  <div class="gd-game__path"><button data-card-id="24"></button></div>
  <gd-table-edit><div class="gd-edit-menu"></div></gd-table-edit>
`;

describe('CardPicker', () => {
  let picker: CardPicker;
  let under: string[];

  beforeEach(() => {
    document.body.innerHTML = PAGE;
    under = [];
    (document as { elementsFromPoint: (x: number, y: number) => Element[] }).elementsFromPoint = () =>
      under.map((selector) => document.querySelector(selector)!);
    for (const card of document.querySelectorAll<HTMLElement>('[data-card-id]')) {
      card.getBoundingClientRect = () =>
        ({ left: 10, top: 20, width: 50, height: 70, right: 60, bottom: 90 }) as DOMRect;
    }
    TestBed.configureTestingModule({
      providers: [
        CardPicker,
        { provide: TableEdit, useValue: { ...fakeTableEdit(), board: () => readEditBoard(SANDBOX_STATE) } },
      ],
    });
    picker = TestBed.inject(CardPicker);
  });

  afterEach(() => {
    delete (document as { elementsFromPoint?: unknown }).elementsFromPoint;
    document.body.innerHTML = '';
  });

  it("takes a card of the table's rows by its id, named as the level knows it, not the card being moved", () => {
    under = ['[data-card-id="30"]'];
    expect(picker.thingAt(1, 1)).toEqual({
      anchor: '30',
      text: 'Boromir, Son of Denethor',
      box: { left: 10, top: 20, width: 50, height: 70 },
    });
    under = ['[data-card-id="77"]'];
    expect(picker.thingAt(1, 1)?.text).toBe('1_178');
    under = ['.gd-edit-menu', '[data-card-id="30"]'];
    expect(picker.thingAt(1, 1)?.anchor).toBe('30');
    under = ['[data-card-id="24"]'];
    expect(picker.thingAt(1, 1)).toBeNull();
    under = [];
    expect(picker.thingAt(1, 1)).toBeNull();
  });

  it('says what to do while it lasts; a click takes the card, Escape nothing', async () => {
    under = ['[data-card-id="30"]'];
    const picked = picker.pickCard('Attach Armor to…', '31');
    expect(picker.told()).toBe('Attach Armor to…');
    expect(picker.picking()).toBe(true);
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 5, clientY: 5 }));
    expect(picker.hover()).toMatchObject({ anchor: '30', text: 'Boromir, Son of Denethor' });
    document.body.dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 5, clientY: 5 }));
    expect(await picked).toBe('30');
    expect(picker.told()).toBeNull();

    const none = picker.pickCard('Stack Armor on…', '30');
    expect(picker.thingAt(1, 1)).toBeNull();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(await none).toBeNull();
    expect(picker.picking()).toBe(false);
  });
});
