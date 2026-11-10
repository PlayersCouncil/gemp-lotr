import { BothPages, BothPagesOptions, openBothPages } from '../testing/parity/both-pages';
import { DialogAnswer } from '../testing/parity/legacy-page';
import { CONTROLS } from '../../visual-check/controls.mjs';

/**
 * Where the Angular page does not do what the legacy page did. Each test shows what each page does, so a change on
 * either side fails here. The generated sessions (generated-sessions.parity.spec.ts) stay away from exactly these.
 */
describe('parity with the legacy page: the known differences', () => {
  let pages: BothPages;

  const open = async (options?: BothPagesOptions) => (pages = await openBothPages(options));
  afterEach(() => pages.close());

  /** Chooses the first option with that value on both pages. */
  const choose = (control: string, value: string) => {
    const index = pages.legacy.options(control).findIndex((option) => option.value === value);
    expect(index, `${control} has the option "${value}"`).toBeGreaterThanOrEqual(0);
    return pages.both((page) => page.choose(control, index));
  };
  const press = (control: string, index = 0, ...answers: DialogAnswer[]) => {
    pages.answer(...answers);
    return pages.both((page) => page.press(control, index));
  };
  /** Opens a saved deck from the deck list, by its name, and closes the list. */
  const openDeck = async (name: string) => {
    await press('deckList');
    const index = pages.legacy.texts('deckListItem').findIndex((item) => item.includes(name));
    expect(index, `the deck list has "${name}"`).toBeGreaterThanOrEqual(0);
    await press('openDeck', index);
    await press('dialogClose');
  };
  const savedNotes = (saved: string, deck: string) =>
    (JSON.parse(saved) as [string, { notes: string }][]).find(([name]) => name === deck)?.[1].notes;

  describe('legacy bugs the Angular page deliberately does not copy (trymarz/gemp-lotr-private#26)', () => {
    it('a culture stops filtering once a format hides its button', async () => {
      await open({ releaseHiddenCultures: false });
      await pages.both((page) => page.press(CONTROLS.culture('MEN')));
      await choose('format', 'fotr_block');
      const { legacy, angular } = pages.observe();
      expect(legacy.hiddenCultures).toContain('MEN');
      expect(angular.hiddenCultures).toEqual(legacy.hiddenCultures);
      expect(legacy.collection).toContain(' culture:MEN ');
      expect(angular.collection).not.toContain('culture:');
    });

    it("a product chosen while a slot's own search is on is left out, not glued to the field before it", async () => {
      await open();
      await press('ringSlot');
      await choose('product', 'pack');
      const { legacy, angular } = pages.observe();
      expect(legacy.collection).toContain(' sort:namepack cardType:THE_ONE_RING product:card');
      expect(angular.collection).toContain(' sort:name cardType:THE_ONE_RING product:card');
    });

    it('every space of the game text is sent as "_", not only the first', async () => {
      await open();
      await pages.both((page) => page.type('gametext', 'one two three'));
      const { legacy, angular } = pages.observe();
      expect(legacy.collection).toContain(' gametext:one_two three ');
      expect(angular.collection).toContain(' gametext:one_two_three ');
    });

    it('a name of spaces only is left out of the search, not sent as spaces', async () => {
      await open();
      await pages.both((page) => page.type('name', '   '));
      const { legacy, angular } = pages.observe();
      expect(legacy.collection).toContain(' sort:name    cardType:-THE_ONE_RING');
      expect(angular.collection).toContain(' sort:name cardType:-THE_ONE_RING');
    });

    it('the library keeps its own title after the deck list was open, and the deck list its own', async () => {
      await open();
      await press('deckList');
      await press('dialogClose');
      await press('library');
      const legacyTitle = pages.legacy.texts('dialogTitles');
      const angularTitle = pages.angular.texts('dialogTitles');
      expect(legacyTitle).toEqual(['Your Saved Decks']);
      expect(angularTitle).toEqual(['Library Decks']);
    });

    it("a deck's name is shown as it was typed, not read as HTML", async () => {
      await open();
      await press('saveDeck', 0, 'New <b>name</b>');
      await press('collectionCards', 1);
      const { legacy, angular } = pages.observe();
      expect(legacy.deckName).toBe('*New name - modified');
      expect(angular.deckName).toBe('*New <b>name</b> - modified');
      expect(angular.saved).toEqual(legacy.saved);
    });

    it('a decklist with a line that is only "(" is imported; the legacy page gave up on it', async () => {
      await open();
      await press('importDeck');
      await pages.both((page) => page.type('importText', '(\n1 6'));
      await press('importButton');
      const { legacy, angular } = pages.observe();
      expect(legacy.errors.join()).toContain('Maximum call stack size exceeded');
      expect(legacy.requests.filter((request) => request.includes('/import/'))).toEqual([]);
      expect(angular.requests).toContain('GET /collection/import/ decklist=(~1 6~');
      expect(angular.deckName).toBe('Imported Deck (unsaved)');
    });

    it('converting a deck to the errata keeps its notes', async () => {
      await open();
      await press('notes');
      await pages.both((page) => page.type('notesText', 'Keep me'));
      await press('dialogClose');
      await press('collectionCards', 1);
      await press('convertErrata');
      await press('saveDeck', 0, 'Converted');
      const { legacy, angular } = pages.observe();
      expect(savedNotes(legacy.saved, 'Converted')).toBe('');
      expect(savedNotes(angular.saved, 'Converted')).toBe('Keep me');
    });

    it("a new deck doesn't start with the notes of the deck before it", async () => {
      await open();
      await openDeck('Fellowship');
      await press('newDeck');
      await press('collectionCards', 1);
      await press('saveDeck', 0, 'Fresh');
      const { legacy, angular } = pages.observe();
      expect(savedNotes(legacy.saved, 'Fresh')).toBe('Notes with + plus and a "quote"');
      expect(savedNotes(angular.saved, 'Fresh')).toBe('');
    });
  });

  describe('found by the generated sessions and decided by the owner', () => {
    it('with "Special in Deck" chosen, removing a site asks for the collection without it (decided 2026-11-10)', async () => {
      await open();
      await press('sitesSlot');
      await press('collectionCards', 0);
      await choose('product', 'specialForDeck');
      await press('siteCards', 0);
      const { legacy, angular } = pages.observe();
      expect(legacy.cards['siteCards']).toBe(0);
      expect(angular.cards['siteCards']).toBe(0);
      expect(legacy.collection).toContain(' blueprints:1_319 ');
      expect(angular.collection).toContain(' blueprints: ');
    });
    // Decks the legacy page saved keep working: visual-check/legacy-decks.mjs (check:legacy-decks) opens them on both
    // pages on a real server and checks that saving them on the Angular page leaves them exactly as stored
    it('notes with "&" or "<" are saved as stored; the legacy page saved them escaped once more (decided 2026-11-10)', async () => {
      const notes = 'Tom & Jerry <3';
      await open({
        prepare: (server) => server.decks.set('Cartoon', { targetFormat: 'fotr_block', notes, contents: '|||1_5' }),
      });
      await openDeck('Cartoon');
      await press('collectionCards', 1);
      await press('saveDeck');
      const { legacy, angular } = pages.observe();
      expect(savedNotes(legacy.saved, 'Cartoon')).toBe('Tom &amp; Jerry &lt;3');
      expect(savedNotes(angular.saved, 'Cartoon')).toBe(notes);
    });
  });
});
