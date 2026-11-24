import { TestBed } from '@angular/core/testing';
import { CardView } from '../../../core/cards/card-view';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { fakeTableEdit } from '../../../testing/table-edit';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { signal } from '@angular/core';
import { EditPopups, TableEditGestures } from './edit-popups';

const view = (ref: unknown) => ({ blueprintId: '1_50', ref }) as CardView;

describe('EditPopups and TableEditGestures', () => {
  let popups: EditPopups;
  let gestures: TableEditGestures;
  const edit = fakeTableEdit();
  const phone = signal(false);

  beforeEach(() => {
    edit.mode.set('edit');
    phone.set(false);
    TestBed.configureTestingModule({
      providers: [
        EditPopups,
        TableEditGestures,
        { provide: TableEdit, useValue: edit },
        { provide: CompactLayout, useValue: { gameActive: phone } },
      ],
    });
    popups = TestBed.inject(EditPopups);
    gestures = TestBed.inject(TableEditGestures);
  });

  it('opens one popup at a time, and gives the focus back to what opened it if asked', () => {
    const spot = document.createElement('button');
    document.body.append(spot);
    popups.openChooser({ seat: 'freeps', zone: 'hand' }, spot);
    expect(popups.chooser()?.anchor).toEqual({ left: 0, top: 0, right: 0, bottom: 0 });
    popups.openMenu('7', view({ cardId: '7' }), { element: spot, point: null });
    expect(popups.chooser()).toBeNull();
    popups.closeMenu(true);
    expect(document.activeElement).toBe(spot);
    popups.openChooser({ seat: 'freeps', zone: 'hand' }, spot);
    popups.openSearch({ seat: 'freeps', zone: 'hand' });
    expect(popups.chooser()).toBeNull();
    expect(popups.search()).toEqual({ seat: 'freeps', zone: 'hand' });
    popups.closeSearch();
    popups.closeChooser(true);
    expect(popups.search()).toBeNull();
    spot.remove();
  });

  it("takes a game card's right-click for the card menu while the table is edited, and nothing else", () => {
    const element = document.createElement('button');
    expect(gestures.menu(view({ cardId: '7' }), { element, point: { x: 1, y: 2 } })).toBe(true);
    expect(popups.menu()).toMatchObject({ cardId: '7', point: { x: 1, y: 2 } });
    expect(gestures.menu(view({ found: {} }), { element, point: null })).toBe(false);
    phone.set(true);
    expect(gestures.menu(view({ cardId: '8' }), { element, point: null })).toBe(false);
    phone.set(false);
    edit.mode.set('play');
    expect(gestures.menu(view({ cardId: '8' }), { element, point: null })).toBe(false);
  });

  it("leaves a puzzle's cards their details: only its slots are filled", () => {
    edit.level.set('puzzle');
    expect(gestures.menu(view({ cardId: '7' }), { element: document.createElement('button'), point: null })).toBe(
      false,
    );
    expect(popups.menu()).toBeNull();
  });
});
