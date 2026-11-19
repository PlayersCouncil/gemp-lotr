import { BothPages, BothPagesOptions, openBothPages, PACKS } from '../testing/parity/both-pages';
import { Failure, FakeGempServer } from '../testing/parity/fake-gemp-server';
import { DialogAnswer } from '../testing/parity/legacy-page';
import { Control } from '../testing/parity/page-driver';

const GENERIC = 'alert: There was a problem during communication with server';

/**
 * What both pages do with the server's answers that aren't the usual ones: every error the legacy page had a message
 * for, the generic message for the rest, and opening packs. After each gesture the two pages must ask, show and save
 * the same, and show the same messages.
 */
describe('parity with the legacy page: the server says no, or opens a pack', () => {
  let pages: BothPages;

  afterEach(() => pages.close());

  const open = async (options: BothPagesOptions = {}) => (pages = await openBothPages(options));
  const failing =
    (...failures: Failure[]) =>
    (server: FakeGempServer) =>
      server.failures.push(...failures);

  /** The same gesture on both pages, answering what it asks; then both must agree, and this is what they showed. */
  async function step(
    gesture: Control | ((page: Parameters<Parameters<BothPages['both']>[0]>[0]) => void),
    ...answers: DialogAnswer[]
  ) {
    pages.answer(...answers);
    await pages.both(typeof gesture === 'function' ? gesture : (page) => page.press(gesture));
    pages.legacy.answers.length = 0;
    pages.angular.answers.length = 0;
    const { legacy, angular } = pages.observe();
    expect(angular).toEqual(legacy);
    return legacy;
  }

  const indexOf = (control: string, text: string) => {
    const index = pages.legacy.texts(control).findIndex((item) => item.includes(text));
    expect(index, `${control} has "${text}"`).toBeGreaterThanOrEqual(0);
    return index;
  };
  const chooseOption = (control: string, value: string) => {
    const index = pages.legacy.options(control).findIndex((option) => option.value === value);
    expect(index, `${control} has the option "${value}"`).toBeGreaterThanOrEqual(0);
    return step((page) => page.choose(control, index));
  };

  describe('the messages the legacy page had for an error', () => {
    it('400 for the formats: "Could not retrieve formats."', async () => {
      await open({ prepare: failing({ method: 'POST', path: '/deck/formats', status: 400 }) });
      const { legacy, angular } = pages.observe();
      expect(angular).toEqual(legacy);
      expect(legacy.dialogs).toEqual(['alert: Could not retrieve formats.']);
    });

    it('404 for a collection: "You don\'t have collection of that type."', async () => {
      await open({ prepare: failing({ method: 'GET', path: '/collection/trophy', status: 404 }) });
      const shown = await chooseOption('collection', 'trophy');
      expect(shown.dialogs).toEqual(["alert: You don't have collection of that type."]);
    });

    it('400 for the stats: "Invalid deck for getting stats."', async () => {
      await open();
      pages.fail({ method: 'POST', path: '/deck/stats', status: 400 });
      const shown = await step((page) => page.press('collectionCards', 1));
      expect(shown.dialogs).toEqual(['alert: Invalid deck for getting stats.']);
    });

    it('400 for saving: "Invalid deck format."', async () => {
      await open({ prepare: failing({ method: 'POST', path: '/deck', status: 400 }) });
      await step((page) => page.press('collectionCards', 1));
      const shown = await step('saveDeck', 'Rejected');
      expect(shown.dialogs).toEqual(['prompt: Enter the name of the deck', 'alert: Invalid deck format.']);
    });

    it('404 for renaming in the deck list: "Couldn\'t find the deck to rename on the server."', async () => {
      await open({ prepare: failing({ method: 'POST', path: '/deck/rename', status: 404 }) });
      await step('deckList');
      const shown = await step((page) => page.press('deckRename', indexOf('deckListItem', 'Fellowship')), 'Gone deck');
      expect(shown.dialogs.at(-1)).toBe("alert: Couldn't find the deck to rename on the server.");
    });

    it('400 for converting: "Error processing errata conversion.", and Convert can be pressed again', async () => {
      await open({ prepare: failing({ method: 'POST', path: '/deck/convert', status: 400 }) });
      await step((page) => page.press('collectionCards', 1));
      const shown = await step('convertErrata');
      expect(shown.dialogs).toEqual(['alert: Error processing errata conversion.']);
      expect(shown.disabled).not.toContain('convertErrata');
    });

    it('414 for importing: "Deck too large to import."', async () => {
      await open({ prepare: failing({ method: 'GET', path: '/collection/import/', status: 414 }) });
      await step('importDeck');
      await step((page) => page.type('importText', '12\n7'));
      const shown = await step('importButton');
      expect(shown.dialogs).toEqual(['alert: Deck too large to import.']);
    });

    it('404 for a pack: "You have no pack of this type in your collection."', async () => {
      await open({
        prepare: (server) => {
          server.withPacks();
          server.failures.push({ method: 'POST', path: '/collection/default', status: 404 });
        },
      });
      const shown = await step((page) => page.press(PACKS, 0));
      expect(shown.dialogs).toEqual(['alert: You have no pack of this type in your collection.']);
    });

    it('404 for a selection: its own message, and the choice closes', async () => {
      await open({
        prepare: (server) => {
          server.withPacks();
          server.failures.push({ method: 'POST', path: '/collection/default', status: 404 });
        },
      });
      await step((page) => page.press(PACKS, 1));
      const shown = await step((page) => page.press('selectionChoice', 2));
      expect(shown.dialogs).toEqual([
        'alert: You have no pack of this type in your collection or that selection is not available for this pack.',
      ]);
      expect(shown.cards['selectionChoice']).toBe(0);
    });
  });

  describe('the generic message, for every other failure', () => {
    const DECK_CALLS: [string, Failure, (page: BothPages) => Promise<unknown>][] = [
      ['listing the decks', { method: 'GET', path: '/deck/list', status: 500 }, () => step('deckList')],
      ['listing the library', { method: 'GET', path: '/deck/libraryList', status: 500 }, () => step('library')],
      [
        'opening a deck',
        { method: 'GET', path: '/deck', status: 500 },
        async () => {
          await step('deckList');
          await step((page) => page.press('openDeck', indexOf('deckListItem', 'Fellowship')));
        },
      ],
      [
        'opening a library deck',
        { method: 'GET', path: '/deck/library', status: 500 },
        async () => {
          await step('library');
          await step((page) => page.press('openDeck', 0));
        },
      ],
      [
        'deleting a deck',
        { method: 'POST', path: '/deck/delete', status: 500 },
        async () => {
          await step('deckList');
          await step((page) => page.press('deckDelete', indexOf('deckListItem', 'Fellowship')), true);
        },
      ],
      [
        'sharing a deck',
        { method: 'GET', path: '/deck/share', status: 500 },
        async () => {
          await step('deckList');
          await step((page) => page.press('deckShare', 0));
        },
      ],
      [
        'saving a deck',
        { method: 'POST', path: '/deck', status: 500 },
        async () => {
          await step((page) => page.press('collectionCards', 1));
          await step('saveDeck', 'Lost');
        },
      ],
      ['a page of the collection', { method: 'GET', path: '/collection/default', status: 500 }, () => step('nextPage')],
    ];

    it.each(DECK_CALLS)('%s', async (_, failure, gestures) => {
      await open();
      pages.fail(failure);
      const shown: string[] = [];
      const record = pages.observe;
      pages.observe = () => {
        const seen = record();
        shown.push(...seen.legacy.dialogs);
        return seen;
      };
      await gestures(pages);
      expect(shown).toContain(GENERIC);
    });
  });

  describe('opening saved decks', () => {
    it.each([
      ['Anything Goes, with cards', 'rev_tow_sta', '|||1_5,1_6'],
      ['Anything Goes, empty', 'rev_tow_sta', '|||'],
      ['a PC format', 'pc_movie', '1_2|1_1||1_5'],
      ['a format without errata', 'fotr_block', '1_2|1_1|1_319|1_5'],
    ])('%s: the same name, cards, format and Convert button', async (_, targetFormat, contents) => {
      await open({ prepare: (server) => server.decks.set('Saved', { targetFormat, notes: 'n', contents }) });
      await step('deckList');
      const shown = await step((page) => page.press('openDeck', indexOf('deckListItem', 'Saved')));
      expect(shown.deckName).toBe('Saved');
    });
  });

  describe('opening packs', () => {
    it('a booster: the collection shows one fewer, and the new items arrive', async () => {
      await open({ prepare: (server) => server.withPacks() });
      const shown = await step((page) => page.press(PACKS, 0));
      expect(shown.dialogTitles.some((title) => title.startsWith('New items'))).toBe(true);
    });

    it('a selection pack: the choice opens, and the chosen card arrives', async () => {
      await open({ prepare: (server) => server.withPacks() });
      const choosing = await step((page) => page.press(PACKS, 1));
      expect(choosing.cards['selectionChoice']).toBe(3);
      expect(choosing.dialogTitles).toEqual(['Choose one']);
      const chosen = await step((page) => page.press('selectionChoice', 1));
      expect(chosen.cards['selectionChoice']).toBe(0);
      expect(chosen.dialogTitles.some((title) => title.startsWith('New items'))).toBe(true);
    });
  });
});
