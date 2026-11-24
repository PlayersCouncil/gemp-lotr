import { TestBed } from '@angular/core/testing';
import { CardView } from '../../../core/cards/card-view';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { EditTool } from '../../../core/game/table-edit/edit-ops';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { providePathTesting, SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { CardMenuComponent } from './card-menu.component';
import { CardPicker } from './card-picker';
import { EditPopups } from './edit-popups';
import { PathPopups } from './path-popups';

/** The card menu of a site laid on the path (Bag End, card 24) or controlled (Ettenmoors, card 14, the shadow seat's). */
describe('CardMenuComponent: a site', () => {
  let edit: FakeTableEdit;
  let popups: EditPopups;
  let card: HTMLButtonElement;

  function render(cardId: string, point: { x: number; y: number } | null = { x: 40, y: 50 }, tools?: EditTool[]) {
    edit = fakeTableEdit(tools ? { tools } : {});
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        ...providePathTesting(),
        EditPopups,
        { provide: TableEdit, useValue: edit },
        { provide: CardPicker, useValue: { pickCard: vi.fn() } },
      ],
    });
    popups = TestBed.inject(EditPopups);
    card = document.createElement('button');
    document.body.append(card);
    const fixture = TestBed.createComponent(CardMenuComponent);
    popups.openMenu(cardId, { blueprintId: '1_319', ref: { cardId } } as CardView, { element: card, point });
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const entries = () =>
      [
        ...el.querySelectorAll<HTMLElement>('.gd-edit-menu:not([data-submenu]) > :is(button, .gd-edit-menu__heading)'),
      ].map((entry) =>
        entry.dataset['siteAction']
          ? `${entry.dataset['siteAction']} ${entry.dataset['seat'] ?? ''}${entry.hasAttribute('data-here') ? ' here' : ''}`.trim()
          : (entry.dataset['menu'] ?? entry.textContent.trim()),
      );
    const entry = (selector: string) => el.querySelector<HTMLButtonElement>(selector)!;
    return { fixture, el, entries, entry };
  }

  afterEach(() => card.remove());

  it("offers a laid site each seat's marker, control by each seat and Replace…, each seat by its player", () => {
    const { entries, entry } = render('24');
    expect(entries()).toEqual([
      'info',
      'Marker',
      'marker freeps',
      'marker shadow',
      'Control',
      'control freeps',
      'control shadow',
      'replace',
    ]);
    expect(entry('[data-site-action="marker"][data-seat="shadow"]').textContent.trim()).toBe('Sandbox');
  });

  it('puts a marker on it, gives it to a seat, or liberates it, and closes', async () => {
    const { fixture, entry } = render('24');
    entry('[data-site-action="marker"][data-seat="freeps"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'marker', seat: 'freeps', site: 1 }], { x: 40, y: 50 });
    expect(popups.menu()).toBeNull();
    TestBed.resetTestingModule();
    card.remove();
    const controlled = render('14');
    // (a controlled site lies in its controller's support area: on the table, it can take tokens)
    expect(controlled.entries()).toEqual([
      'info',
      'add',
      'Marker',
      'marker freeps',
      'marker shadow',
      'Control',
      'control freeps',
      'control shadow here',
      'liberate',
      'replace',
    ]);
    expect(controlled.entry('[data-site-action="control"][data-seat="shadow"]').disabled).toBe(true);
    controlled.entry('[data-site-action="control"][data-seat="freeps"]').click();
    await controlled.fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'control', site: 2, seat: 'freeps' }], { x: 40, y: 50 });
    popups.openMenu('14', { blueprintId: '1_331', ref: { cardId: '14' } } as CardView, { element: card, point: null });
    controlled.fixture.detectChanges();
    controlled.entry('[data-site-action="liberate"]').click();
    await controlled.fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'liberate', site: 2 }], expect.any(Object));
  });

  it('Replace… opens the site chooser for its number where the menu was (by the card from the keyboard)', () => {
    const { entry } = render('24');
    entry('[data-menu="replace"]').click();
    const paths = TestBed.inject(PathPopups);
    expect(paths.chooser()).toMatchObject({
      number: 1,
      anchor: { left: 40, top: 50, right: 40, bottom: 50 },
      element: card,
      replacing: true,
    });
    expect(popups.menu()).toBeNull();
    TestBed.resetTestingModule();
    card.remove();
    const keyboard = render('24', null);
    keyboard.entry('[data-menu="replace"]').click();
    // (jsdom lays nothing out: the card's box is all 0)
    expect(TestBed.inject(PathPopups).chooser()?.anchor).toEqual({ left: 0, top: 0, right: 0, bottom: 0 });
  });

  it("closes another entry's submenu when the pointer comes onto a site's entry", () => {
    const { fixture, el, entry } = render('14');
    for (const selector of [
      '[data-site-action="marker"][data-seat="freeps"]',
      '[data-site-action="control"][data-seat="freeps"]',
      '[data-site-action="liberate"]',
      '[data-menu="replace"]',
    ]) {
      entry('[data-menu="add"]').dispatchEvent(new Event('pointerenter'));
      fixture.detectChanges();
      expect(el.querySelector('[data-submenu="add"]')).not.toBeNull();
      entry(selector).dispatchEvent(new Event('pointerenter'));
      fixture.detectChanges();
      expect(el.querySelector('[data-submenu]')).toBeNull();
    }
  });

  it('offers only what the tools allow: Replace… with sites alone, nothing of the kind without either', () => {
    expect(render('24', null, ['sites']).entries()).toEqual(['info', 'replace']);
    TestBed.resetTestingModule();
    card.remove();
    expect(render('24', null, ['add']).entries()).toEqual(['info']);
  });
});
