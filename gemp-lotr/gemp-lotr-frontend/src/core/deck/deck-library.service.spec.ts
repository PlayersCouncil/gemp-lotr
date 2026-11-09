import { provideDeckbuilderState } from '../deckbuilder-state';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FakeNotifier, provideFakeNotifier, settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { DeckLibraryService } from './deck-library.service';
import { DeckDraft } from './deck-draft.service';
import { DeckEditing } from './deck-editing.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { DeckOpener } from './deck-opener.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';

describe('DeckLibraryService', () => {
  let backend: HttpTestingController;
  let draft: DeckDraft;
  let editing: DeckEditing;
  let opener: DeckOpener;
  let dialogs: string[];
  let answers: (string | boolean | null)[];

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    ({ dialogs, answers } = TestBed.inject(FakeNotifier));
    backend = TestBed.inject(HttpTestingController);
    draft = TestBed.inject(DeckDraft);
    editing = TestBed.inject(DeckEditing);
    opener = TestBed.inject(DeckOpener);
  });

  afterEach(() => vi.restoreAllMocks());

  const take = (path: string): TestRequest => backend.expectOne((req) => req.url.endsWith(path));
  const params = (req: TestRequest) => {
    const p = req.request.method === 'GET' ? req.request.params : (req.request.body as typeof req.request.params);
    return Object.fromEntries(
      p
        .keys()
        .filter((k) => k !== '_' && k !== 'participantId')
        .map((k) => [k, p.get(k)]),
    );
  };

  let service: DeckLibraryService;
  beforeEach(() => (service = TestBed.inject(DeckLibraryService)));

  it('offers to save before listing the decks, then lists them', async () => {
    editing.addFromCollection('1_12', 'FREE_PEOPLE');
    answers.push(false);
    await service.openDeckList();
    take('/deck/list').flush(readServerResponse('deck-list.xml'));
    expect(dialogs[0]).toContain('confirm: Your deck has been modified.');
    expect(service.deckList()?.kind).toBe('decks');
    expect(service.deckList()!.decks.length).toBeGreaterThan(0);
  });

  it('waits for the answer to the save prompt before listing the decks', async () => {
    const notifier = TestBed.inject(FakeNotifier);
    let answer!: (ok: boolean) => void;
    vi.spyOn(notifier, 'confirm').mockReturnValue(new Promise((resolve) => (answer = resolve)));
    editing.addFromCollection('1_12', 'FREE_PEOPLE');
    const opening = service.openDeckList();
    await settle();
    backend.expectNone((req) => req.url.endsWith('/deck/list'));
    answer(false);
    await opening;
    take('/deck/list').flush(readServerResponse('deck-list.xml'));
    expect(service.deckList()?.kind).toBe('decks');
  });

  it("saves first when the player chooses to, asking for the new deck's name", async () => {
    editing.addFromCollection('1_12', 'FREE_PEOPLE');
    answers.push(true, 'Saved deck');
    await service.openDeckList();
    expect(params(take('/deck'))).toMatchObject({ deckName: 'Saved deck', deckContents: '|||1_12' });
    take('/deck/list');
    expect(dialogs).toEqual([
      'confirm: Your deck has been modified.  Would you like to save your deck before proceeding?',
      'prompt: Enter the name of the deck',
    ]);
  });

  it('renames a listed deck, and the open deck with it', async () => {
    await service.openDeckList();
    take('/deck/list').flush(readServerResponse('deck-list.xml'));
    const entry = service.deckList()!.decks[0];
    opener.setupDeck(
      {
        targetFormat: null,
        notes: '',
        ringBearer: null,
        ring: null,
        map: null,
        sites: [],
        drawDeck: [],
      },
      entry.name,
    );
    backend.match(() => true);
    answers.push('Brand new name');
    await service.renameDeck(entry);
    const rename = take('/deck/rename');
    expect(params(rename)).toEqual({ oldDeckName: entry.name, deckName: 'Brand new name' });
    rename.flush(readServerResponse('deck.xml'));
    expect(service.deckList()!.decks[0].name).toBe('Brand new name');
    expect(draft.deckName()).toBe('Brand new name');
  });

  it('deletes a deck after confirming; the open deck becomes a new one', async () => {
    await service.openDeckList();
    take('/deck/list').flush(readServerResponse('deck-list.xml'));
    const entry = service.deckList()!.decks[0];
    opener.setupDeck(
      {
        targetFormat: null,
        notes: '',
        ringBearer: null,
        ring: null,
        map: null,
        sites: [],
        drawDeck: [],
      },
      entry.name,
    );
    backend.match(() => true);
    answers.push(false);
    await service.deleteDeck(entry);
    backend.expectNone((req) => req.url.endsWith('/deck/delete'));
    answers.push(true);
    await service.deleteDeck(entry);
    take('/deck/delete').flush('<ok/>');
    expect(draft.deckName()).toBeNull();
    take('/deck/list').flush(readServerResponse('deck-list.xml'));
  });

  it('opens a library deck as unsaved, under its name', async () => {
    await service.openLibrary();
    take('/deck/libraryList').flush(readServerResponse('deck-list.xml'));
    expect(service.deckList()?.kind).toBe('library');
    service.openDeck(service.deckList()!.decks[0]);
    take('/deck/library').flush(readServerResponse('deck.xml'));
    expect(draft.deckName()).toBe(service.deckList()!.decks[0].name);
    expect(draft.contentsDirty()).toBe(true);
    backend.match(() => true);
  });

  it('starts the import dialog empty each time', async () => {
    await service.openImport();
    const round = service.importRound();
    await service.openImport();
    expect(service.importRound()).toBe(round + 1);
    expect(service.importOpen()).toBe(true);
  });

  describe('importing a decklist', () => {
    it('puts the recognised cards into the deck, by group', () => {
      opener.importDecklist('1x The One Ring\nRing-bearer: Frodo');
      const req = take('/collection/import/');
      expect(params(req)).toEqual({ decklist: '1x the one ring~frodo~' });
      req.flush(
        '<collection><card blueprintId="1_2" count="1" group="ring"/><card blueprintId="1_290" count="1" group="ringBearer"/>' +
          '<card blueprintId="1_12" count="2" group="fp" side="FREE_PEOPLE"/><card blueprintId="1_319" count="1" group="site"/></collection>',
      );
      expect(TestBed.inject(DeckFormatSelection).contentsOf(draft.deck())).toBe('1_290|1_2|1_319|1_12,1_12');
      expect(draft.label()).toEqual({ text: 'Imported Deck (unsaved)', modified: false });
      expect(draft.contentsDirty()).toBe(true);
      backend.match(() => true);
    });

    it('alerts when the decklist is too long', () => {
      opener.importDecklist('x');
      take('/collection/import/').flush('', { status: 414, statusText: 'URI Too Long' });
      expect(dialogs).toEqual(['alert: Deck too large to import.']);
    });
  });
});
