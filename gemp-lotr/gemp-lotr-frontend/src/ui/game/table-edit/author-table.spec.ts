import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardView } from '../../../core/cards/card-view';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { readPuzzleSlots } from '../../../core/game/table-edit/puzzle-slots';
import { RecentCards } from '../../../core/game/table-edit/recent-cards';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { fakeTableEdit, FakeTableEdit, PUZZLE_SLOTS } from '../../../testing/table-edit';
import { AuthorEditBarComponent } from './author-edit-bar.component';
import { CardMenuComponent } from './card-menu.component';
import { CardPicker } from './card-picker';
import { ChooserComponent } from './chooser.component';
import { EditPopups } from './edit-popups';
import { PuzzlePopups } from './puzzle-popups';
import { PuzzleSlotComponent } from './puzzle-slot.component';

/** The author's backend, for the controls' tests: what they read of it, and its calls recorded. */
function fakeAuthor() {
  return {
    editing: signal(true),
    inSetup: signal(false),
    slotted: signal(true),
    setWhere: vi.fn(() => true),
    openSlot: vi.fn(() => Promise.resolve<string | null>(null)),
    showSlot: vi.fn(),
  };
}

describe("the author's table controls", () => {
  let edit: FakeTableEdit;
  let author: ReturnType<typeof fakeAuthor>;
  let anchor: HTMLButtonElement;

  function provide(...extra: unknown[]) {
    edit = fakeTableEdit({ level: 'author' });
    author = fakeAuthor();
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        EditPopups,
        PuzzlePopups,
        RecentCards,
        { provide: TableEdit, useValue: edit },
        { provide: AuthorBackend, useValue: author },
        ...(extra as []),
      ],
    });
    anchor = document.createElement('button');
    document.body.append(anchor);
  }

  afterEach(() => anchor?.remove());

  describe('the edit bar', () => {
    const told = signal<string | null>(null);

    function render() {
      provide({ provide: CardPicker, useValue: { told } });
      const fixture = TestBed.createComponent(AuthorEditBarComponent);
      fixture.detectChanges();
      return { fixture, el: fixture.nativeElement as HTMLElement };
    }

    it('says Editing, and which board changes go to, which is also the switch', () => {
      const { fixture, el } = render();
      expect(el.querySelector('.gd-edit-bar__mode')?.textContent?.trim()).toBe('Editing');
      const chips = [...el.querySelectorAll<HTMLButtonElement>('.gd-edit-bar__where-button')];
      expect(
        chips.map((chip) => [chip.dataset['where'], chip.textContent?.trim(), chip.getAttribute('aria-pressed')]),
      ).toEqual([
        ['setup', 'Start', 'false'],
        ['now', 'Now', 'true'],
      ]);
      chips[0]!.click();
      expect(author.setWhere).toHaveBeenCalledWith('setup');
      author.inSetup.set(true);
      fixture.detectChanges();
      expect(chips[0]!.getAttribute('aria-pressed')).toBe('true');
      edit.busy.set(true);
      fixture.detectChanges();
      expect(chips[1]!.disabled).toBe(true);
    });

    it('is idle while the table is played, says what is being picked, and a refusal said by nothing else', () => {
      const { fixture, el } = render();
      author.editing.set(false);
      fixture.detectChanges();
      expect(el.hasAttribute('data-idle')).toBe(true);
      expect(el.querySelector('.gd-edit-bar__mode')).toBeNull();
      told.set('Attach Sting to…');
      fixture.detectChanges();
      expect(el.hasAttribute('data-idle')).toBe(false);
      expect(el.querySelector('.gd-edit-bar__mode[data-picking]')?.textContent).toBe('Attach Sting to…');
      told.set(null);
      edit.problem.set({ text: 'No.', at: null });
      fixture.detectChanges();
      expect(el.querySelector('.gd-edit-bar__problem')?.textContent).toBe('No.');
      edit.problem.set({ text: 'Here.', at: { x: 1, y: 1 } });
      fixture.detectChanges();
      expect(el.querySelector('.gd-edit-bar__problem')).toBeNull();
    });
  });

  describe("an open spot's chooser", () => {
    function render() {
      provide();
      const popups = TestBed.inject(EditPopups);
      const fixture = TestBed.createComponent(ChooserComponent);
      popups.openChooser({ seat: 'shadow', zone: 'support' }, anchor);
      fixture.detectChanges();
      return { fixture, popups, el: fixture.nativeElement as HTMLElement };
    }

    it("offers Open slot to a puzzle's author: a slot at the spot's place, the chooser closing", async () => {
      const { fixture, popups, el } = render();
      const open = el.querySelector<HTMLButtonElement>('[data-chooser="slot"]')!;
      expect(open.textContent?.trim()).toBe('Open slot');
      open.click();
      await fixture.whenStable();
      expect(author.openSlot).toHaveBeenCalledWith({ seat: 'shadow', zone: 'support' });
      expect(popups.chooser()).toBeNull();
    });

    it('says a refusal by the spot, the chooser staying; and offers no slot outside a puzzle', async () => {
      const { fixture, popups, el } = render();
      author.openSlot.mockResolvedValueOnce('A slot cannot be there.');
      el.querySelector<HTMLButtonElement>('[data-chooser="slot"]')!.click();
      await fixture.whenStable();
      expect(edit.problem()?.text).toBe('A slot cannot be there.');
      expect(popups.chooser()).not.toBeNull();
      author.slotted.set(false);
      fixture.detectChanges();
      expect(el.querySelector('[data-chooser="slot"]')).toBeNull();
    });
  });

  describe("a puzzle slot's card in the menu", () => {
    it('offers only Info and Slot…, which shows its form', () => {
      provide({ provide: CardPicker, useValue: { pickCard: vi.fn() } });
      const board = edit.board()!;
      edit.board.set({
        ...board,
        cards: [...board.cards, { ...board.cards[0]!, alias: 'ally', cardId: '90', slot: 'ally' }],
      });
      const popups = TestBed.inject(EditPopups);
      const fixture = TestBed.createComponent(CardMenuComponent);
      popups.openMenu('90', { blueprintId: '1_69', ref: { cardId: '90' } } as CardView, {
        element: anchor,
        point: null,
      });
      fixture.detectChanges();
      const el = fixture.nativeElement as HTMLElement;
      const entries = [...el.querySelectorAll<HTMLElement>('.gd-edit-menu > [data-menu]')].map(
        (one) => one.dataset['menu'],
      );
      expect(entries).toEqual(['info', 'slot']);
      el.querySelector<HTMLButtonElement>('[data-menu="slot"]')!.click();
      expect(author.showSlot).toHaveBeenCalledWith('ally');
      expect(popups.menu()).toBeNull();
    });
  });

  describe('an open slot on the table', () => {
    it("shows the slot's form when pressed (while edited), not the solver's chooser", () => {
      provide();
      const [condition] = readPuzzleSlots(PUZZLE_SLOTS);
      const fixture = TestBed.createComponent(PuzzleSlotComponent);
      fixture.componentRef.setInput('slot', { slot: condition!, card: null });
      fixture.detectChanges();
      const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-open-spot')!;
      button.click();
      expect(author.showSlot).toHaveBeenCalledWith('condition');
      expect(TestBed.inject(PuzzlePopups).chooser()).toBeNull();
      edit.mode.set('play');
      author.showSlot.mockClear();
      fixture.detectChanges();
      button.dispatchEvent(new MouseEvent('click'));
      expect(author.showSlot).not.toHaveBeenCalled();
    });
  });
});
