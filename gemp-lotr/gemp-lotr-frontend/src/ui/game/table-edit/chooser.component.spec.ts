import { TestBed } from '@angular/core/testing';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { ChooserComponent } from './chooser.component';
import { EditPopups } from './edit-popups';

describe('ChooserComponent', () => {
  let edit: FakeTableEdit;
  let popups: EditPopups;
  let recent: RecentCards;
  let spot: HTMLButtonElement;

  function render(zone: 'characters' | 'hand' | 'minions' = 'characters') {
    edit = fakeTableEdit();
    TestBed.configureTestingModule({
      providers: [...provideTestCardInteractions(), EditPopups, RecentCards, { provide: TableEdit, useValue: edit }],
    });
    popups = TestBed.inject(EditPopups);
    recent = TestBed.inject(RecentCards);
    recent.use({ blueprintId: '1_178', name: 'Goblin Runner', type: 'MINION' });
    recent.use({ blueprintId: '1_50', name: 'Legolas, Greenleaf', type: 'COMPANION' });
    spot = document.createElement('button');
    document.body.append(spot);
    const fixture = TestBed.createComponent(ChooserComponent);
    popups.openChooser({ seat: 'freeps', zone }, spot);
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

  it('lists the cards used lately and those in the game that fit the place, then Search…', () => {
    const { el, items } = render();
    const menu = el.querySelector<HTMLElement>('.gd-edit-chooser')!;
    expect(menu.dataset).toMatchObject({ seat: 'freeps', zone: 'characters' });
    expect(menu.getAttribute('aria-label')).toBe('Add to Characters');
    expect(items('recent')).toEqual(['Legolas, Greenleaf']);
    expect(items('scenario')).toEqual(['Boromir, Son of Denethor', 'Frodo, Son of Drogo']);
    expect(items('search')).toEqual(['Search…']);
    expect([...el.querySelectorAll('.gd-edit-chooser__heading')].map((heading) => heading.textContent)).toEqual([
      'Recent',
      'In this scenario',
    ]);
    // (the first item takes the focus)
    expect(document.activeElement?.textContent.trim()).toBe('Legolas, Greenleaf');
  });

  it('puts the card chosen in the place, at the head of Recent, and closes; a refusal keeps it open', async () => {
    const { fixture, el, items } = render('hand');
    expect(items('recent')).toEqual(['Legolas, Greenleaf', 'Goblin Runner']);
    el.querySelector<HTMLButtonElement>('[data-chooser="recent"][data-blueprint="1_178"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenCalledWith([{ op: 'add', blueprint: '1_178', to: 'freeps.hand' }], { x: 0, y: 0 });
    expect(recent.list()[0]?.blueprintId).toBe('1_178');
    expect(popups.chooser()).toBeNull();

    popups.openChooser({ seat: 'freeps', zone: 'hand' }, spot);
    fixture.detectChanges();
    edit.apply.mockResolvedValueOnce(false);
    el.querySelector<HTMLButtonElement>('[data-chooser="scenario"][data-blueprint="1_92"]')!.click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'add', blueprint: '1_92', to: 'freeps.hand' }], { x: 0, y: 0 });
    expect(popups.chooser()).not.toBeNull();
  });

  it('offers only Search… where nothing fits, and nothing to choose while a change is on its way', () => {
    const { fixture, el, items } = render('minions');
    expect(items('recent')).toEqual(['Goblin Runner']);
    edit.board.set(null);
    edit.busy.set(true);
    fixture.detectChanges();
    expect(items('scenario')).toEqual([]);
    expect(el.querySelector<HTMLButtonElement>('[data-chooser="recent"]')!.disabled).toBe(true);
    localStorage.clear();
  });

  it('opens the search window for its place; Escape or a press outside closes it', () => {
    const { fixture, el } = render('minions');
    el.querySelector<HTMLButtonElement>('[data-chooser="search"]')!.click();
    expect(popups.search()).toEqual({ seat: 'freeps', zone: 'minions' });
    popups.openChooser({ seat: 'freeps', zone: 'minions' }, spot);
    fixture.detectChanges();
    el.querySelector('.gd-edit-chooser')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(popups.chooser()).toBeNull();
    expect(document.activeElement).toBe(spot);
    popups.openChooser({ seat: 'freeps', zone: 'minions' }, spot);
    fixture.detectChanges();
    el.querySelector('.gd-edit-backdrop')!.dispatchEvent(new Event('pointerdown'));
    expect(popups.chooser()).toBeNull();
    popups.openChooser({ seat: 'freeps', zone: 'minions' }, spot);
    fixture.detectChanges();
    const menu = new MouseEvent('contextmenu', { cancelable: true });
    el.querySelector('.gd-edit-backdrop')!.dispatchEvent(menu);
    expect(menu.defaultPrevented).toBe(true);
    expect(popups.chooser()).toBeNull();
  });
});
