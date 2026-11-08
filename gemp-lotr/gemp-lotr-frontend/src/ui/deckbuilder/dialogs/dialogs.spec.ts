import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DeckSummary } from '../../../core/api/models';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckService } from '../../../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { PacksService } from '../../../core/packs/packs.service';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { DeckListDialogComponent } from './deck-list-dialog.component';
import { ImportDialogComponent } from './import-dialog.component';
import { NotesDialogComponent } from './notes-dialog.component';
import { PackDialogsComponent } from './pack-dialogs.component';

const DECKS: DeckSummary[] = [
  { name: 'First <b>deck</b>', formatName: 'Fellowship Block' },
  { name: 'Second', formatName: 'Anything Goes' },
] as DeckSummary[];

describe('deck builder dialogs', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  async function render<T>(component: Type<T>) {
    const fixture = TestBed.createComponent(component);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const dialogs = () => Array.from(el.querySelectorAll<HTMLDialogElement>('dialog.gd-dialog'));
    return { fixture, el, dialogs, stable: () => fixture.whenStable() };
  }

  describe('deck list and library', () => {
    it('lists the saved decks with open, rename, share and delete (DOM contract)', async () => {
      const library = TestBed.inject(DeckLibraryService);
      const { dialogs, stable } = await render(DeckListDialogComponent);
      expect(dialogs()[0].open).toBe(false);
      library.deckList.set({ kind: 'decks', decks: DECKS });
      await stable();
      const [dialog] = dialogs();
      expect(dialog.open).toBe(true);
      expect(dialog.dataset['dialog']).toBe('deck-list');
      expect(dialog.querySelector('.gd-dialog__title')!.textContent).toBe('Your Saved Decks');
      const items = dialog.querySelectorAll('ul.gd-deck-list > li.gd-deck-list__item');
      expect(items.length).toBe(2);
      expect(
        Array.from(items[0].querySelectorAll<HTMLButtonElement>('button.gd-button')).map((b) => [
          b.dataset['action'],
          b.title,
        ]),
      ).toEqual([
        ['open', 'Open deck'],
        ['rename', 'Rename deck'],
        ['share', 'Share deck list'],
        ['delete', 'Delete deck'],
      ]);
      // Names are text
      expect(items[0].querySelector('.gd-deck-list__name')!.textContent).toBe('[Fellowship Block] - First <b>deck</b>');
      expect(items[0].querySelector('b.gd-deck-list__format')!.textContent).toBe('[Fellowship Block]');
    });

    it('runs the entry actions', async () => {
      const library = TestBed.inject(DeckLibraryService);
      const calls: string[] = [];
      for (const method of ['openDeck', 'renameDeck', 'shareDeck', 'deleteDeck', 'showLibraryDeck'] as const) {
        vi.spyOn(library, method).mockImplementation(
          ((entry: DeckSummary) => void calls.push(`${method} ${entry.name}`)) as never,
        );
      }
      const { dialogs, stable } = await render(DeckListDialogComponent);
      library.deckList.set({ kind: 'decks', decks: DECKS });
      await stable();
      const second = dialogs()[0].querySelectorAll('li')[1];
      for (const action of ['open', 'rename', 'share', 'delete']) {
        second.querySelector<HTMLButtonElement>(`[data-action="${action}"]`)!.click();
      }
      library.deckList.set({ kind: 'library', decks: DECKS });
      await stable();
      const dialog = dialogs()[0];
      expect(dialog.dataset['dialog']).toBe('library');
      expect(dialog.querySelector('.gd-dialog__title')!.textContent).toBe('Library Decks');
      const first = dialog.querySelector('li')!;
      expect(Array.from(first.querySelectorAll<HTMLButtonElement>('button')).map((b) => b.dataset['action'])).toEqual([
        'open',
        'deck-list',
      ]);
      first.querySelector<HTMLButtonElement>('[data-action="deck-list"]')!.click();
      expect(calls).toEqual([
        'openDeck Second',
        'renameDeck Second',
        'shareDeck Second',
        'deleteDeck Second',
        'showLibraryDeck First <b>deck</b>',
      ]);
    });

    it('closing it clears the list', async () => {
      const library = TestBed.inject(DeckLibraryService);
      const { dialogs, stable } = await render(DeckListDialogComponent);
      library.deckList.set({ kind: 'decks', decks: DECKS });
      await stable();
      dialogs()[0].querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
      await stable();
      expect(library.deckList()).toBeNull();
    });
  });

  describe('import', () => {
    it('imports the pasted decklist, starting empty each time (DOM contract)', async () => {
      const library = TestBed.inject(DeckLibraryService);
      const deck = TestBed.inject(DeckService);
      const imported: string[] = [];
      vi.spyOn(deck, 'importDecklist').mockImplementation((text: string) => void imported.push(text));
      const { dialogs, stable } = await render(ImportDialogComponent);
      library.importRound.update((n) => n + 1);
      library.importOpen.set(true);
      await stable();
      const [dialog] = dialogs();
      expect(dialog.dataset['dialog']).toBe('import');
      expect(document.activeElement).toBe(dialog);
      const text = dialog.querySelector<HTMLTextAreaElement>('.gd-import textarea.gd-import__text')!;
      text.value = '1x Gandalf';
      dialog.querySelector<HTMLButtonElement>('button.gd-button[data-action="import"]')!.click();
      expect(imported).toEqual(['1x Gandalf']);
      library.importRound.update((n) => n + 1);
      await stable();
      expect(dialog.querySelector<HTMLTextAreaElement>('.gd-import__text')!.value).toBe('');
    });
  });

  describe('notes', () => {
    it('edits the notes, kept on change and when closed (DOM contract)', async () => {
      const library = TestBed.inject(DeckLibraryService);
      const deck = TestBed.inject(DeckService);
      deck.setNotes('Old notes');
      const { dialogs, stable } = await render(NotesDialogComponent);
      library.openNotes();
      await stable();
      const [dialog] = dialogs();
      expect(dialog.dataset['dialog']).toBe('notes');
      const text = dialog.querySelector<HTMLTextAreaElement>('textarea.gd-notes__text')!;
      expect(text.value).toBe('Old notes');
      text.value = 'Changed';
      text.dispatchEvent(new Event('change'));
      expect(deck.notes()).toBe('Changed');
      text.value = 'Typed, not yet changed';
      dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
      await stable();
      expect(deck.notes()).toBe('Typed, not yet changed');
      expect(library.notesOpen()).toBe(false);
    });
  });

  describe('selection pack and new items', () => {
    it("shows a selection pack's choices; using one picks it", async () => {
      const packs = TestBed.inject(PacksService);
      const chosen: string[] = [];
      vi.spyOn(packs, 'choose').mockImplementation((id: string) => void chosen.push(id));
      const { dialogs, stable } = await render(PackDialogsComponent);
      packs.selection.set({ packId: '(S)Pack', choices: ['1_1', '1_2'] });
      await stable();
      const [dialog] = dialogs();
      expect(dialog.open).toBe(true);
      expect(dialog.dataset['dialog']).toBe('selection');
      const box = dialog.querySelector<HTMLElement>('.gd-shown-cards')!;
      expect(box.dataset['count']).toBe('2');
      const cards = box.querySelectorAll<HTMLButtonElement>('button.gd-card');
      expect(Array.from(cards).map((c) => c.dataset['role'])).toEqual(['card', 'card']);
      cards[1].click();
      expect(chosen).toEqual(['1_2']);
    });

    it('shows a non-modal "New items" dialog per collection, kept on the page once closed', async () => {
      const packs = TestBed.inject(PacksService);
      const { dialogs, stable } = await render(PackDialogsComponent);
      packs.deliveries.set(
        new Map([
          [
            'permanent',
            [
              { blueprintId: 'FotR - Booster', count: 2, pack: true },
              { blueprintId: '1_12', count: 1, pack: false },
            ],
          ],
        ]),
      );
      await stable();
      const delivery = dialogs()[1];
      expect(delivery.dataset['dialog']).toBe('delivery');
      expect(delivery.querySelector('.gd-dialog__title')!.textContent).toBe('New items - permanent');
      expect(
        Array.from(delivery.querySelectorAll<HTMLElement>('.gd-card')).map((c) => [
          c.dataset['role'],
          c.querySelector('.gd-card__count')?.textContent,
        ]),
      ).toEqual([
        ['pack', '2'],
        ['card', '1'],
      ]);
      delivery.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
      await stable();
      expect(packs.deliveries().size).toBe(0);
      expect(dialogs()[1]).toBe(delivery);
      expect(delivery.open).toBe(false);
    });
  });
});
