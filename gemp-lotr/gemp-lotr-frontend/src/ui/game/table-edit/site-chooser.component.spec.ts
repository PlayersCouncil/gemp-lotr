import { TestBed } from '@angular/core/testing';
import { readEditBoard } from '../../../core/game/table-edit/edit-board';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit } from '../../../testing/table-edit';
import { providePathTesting, SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { PathPopups } from './path-popups';
import { SiteChooserComponent } from './site-chooser.component';
import { instanceOf } from '../../../testing/matchers';

describe('SiteChooserComponent', () => {
  let edit: FakeTableEdit;
  let popups: PathPopups;
  let spot: HTMLButtonElement;

  function render(replacing = false) {
    edit = fakeTableEdit();
    edit.board.set(readEditBoard(SANDBOX_PILES_STATE));
    TestBed.configureTestingModule({
      providers: [...provideTestCardInteractions(), ...providePathTesting(), { provide: TableEdit, useValue: edit }],
    });
    popups = TestBed.inject(PathPopups);
    spot = document.createElement('button');
    document.body.append(spot);
    const fixture = TestBed.createComponent(SiteChooserComponent);
    popups.openChooser(3, spot, replacing);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const menu = () => el.querySelector<HTMLElement>('.gd-edit-chooser')!;
    const item = (kind: string, extra = '') =>
      el.querySelector<HTMLButtonElement>(`[data-site-chooser="${kind}"]${extra}`)!;
    const items = () =>
      [...el.querySelectorAll<HTMLButtonElement>('.gd-edit-chooser button')].map((button) => button.textContent.trim());
    const go = (kind: string, extra = '') => {
      item(kind, extra).click();
      fixture.detectChanges();
    };
    return { fixture, el, menu, item, items, go };
  }

  afterEach(() => spot.remove());

  it('offers Search… and From adventure deck… for its number; Search… opens the search for it', () => {
    const { menu, items, el, go } = render();
    expect(menu().dataset).toMatchObject({ site: '3', step: 'start' });
    expect(el.querySelector('.gd-edit-chooser__heading')?.textContent).toBe('Site 3');
    expect(items()).toEqual(['Search…', 'From adventure deck…']);
    expect(document.activeElement?.textContent.trim()).toBe('Search…');
    go('search');
    expect(popups.search()).toMatchObject({ number: 3 });
    expect(popups.chooser()).toBeNull();
  });

  it("goes to whose deck, then that deck's sites by number (the chooser's marked), and lays the one picked", async () => {
    const { fixture, menu, items, item, el, go } = render(true);
    expect(el.querySelector('.gd-edit-chooser__heading')?.textContent).toBe('Replace site 3');
    go('decks');
    expect(menu().dataset['step']).toBe('decks');
    expect(items()).toEqual(['alice', 'Sandbox', '‹ Back']);
    go('deck', '[data-seat="shadow"]');
    expect(menu().dataset['step']).toBe('shadow');
    expect(el.querySelector('.gd-edit-chooser__heading')?.textContent).toBe('Sandbox');
    expect(items()).toEqual(['3 · Rivendell Valley', "4 · Balin's Tomb", '‹ Back']);
    expect(item('site', '[data-alias="site3opp"]').hasAttribute('data-match')).toBe(true);
    expect(item('site', '[data-alias="site4opp"]').hasAttribute('data-match')).toBe(false);
    // Refused: it stays open (the refusal is said by it)
    edit.apply.mockResolvedValueOnce(false);
    item('site', '[data-alias="site4opp"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'site', number: 3, card: 'site4opp' }], instanceOf(Object));
    expect(popups.chooser()).not.toBeNull();
    item('site', '[data-alias="site3opp"]').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'site', number: 3, card: 'site3opp' }], instanceOf(Object));
    expect(popups.chooser()).toBeNull();
  });

  it("offers a deck it is not shown by its site of the number (the server finds it), and an empty deck's none", async () => {
    const { fixture, items, item, go } = render();
    edit.board.update((board) => board && { ...board, hiddenPilesOpen: false });
    go('decks');
    go('deck', '[data-seat="shadow"]');
    expect(items()).toEqual(['Site 3', '‹ Back']);
    item('from').click();
    await fixture.whenStable();
    expect(edit.apply).toHaveBeenLastCalledWith([{ op: 'site', number: 3, from: 'shadow' }], instanceOf(Object));
    popups.openChooser(3, spot);
    fixture.detectChanges();
    edit.board.update((board) => board && { ...board, cards: [] });
    go('decks');
    go('deck', '[data-seat="freeps"]');
    expect(items()).toEqual(['‹ Back']);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No sites');
  });

  it('goes back a step with Back or Left; Escape or a press outside closes it', () => {
    const { fixture, menu, go } = render();
    go('decks');
    go('deck', '[data-seat="freeps"]');
    go('back');
    expect(menu().dataset['step']).toBe('decks');
    menu().dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft', bubbles: true }));
    fixture.detectChanges();
    expect(menu().dataset['step']).toBe('start');
    menu().dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft', bubbles: true }));
    fixture.detectChanges();
    expect(menu().dataset['step']).toBe('start');
    menu().dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(popups.chooser()).toBeNull();
    expect(document.activeElement).toBe(spot);
    popups.openChooser(3, spot);
    fixture.detectChanges();
    expect(menu().dataset['step']).toBe('start');
    const press = new MouseEvent('contextmenu', { cancelable: true });
    (fixture.nativeElement as HTMLElement).querySelector('.gd-edit-backdrop')!.dispatchEvent(press);
    expect(press.defaultPrevented).toBe(true);
    expect(popups.chooser()).toBeNull();
    popups.openChooser(3, spot);
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector('.gd-edit-backdrop')!.dispatchEvent(new Event('pointerdown'));
    expect(popups.chooser()).toBeNull();
  });
});
