import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { flushLegacyCardFiles } from '../../testing/legacy-cards';
import { LegacyCardDataService } from '../core/cards/legacy-card-data.service';
import { DeckService } from '../core/deck/deck.service';
import { PAGE_QUERY_STRING } from '../core/session/session.service';
import { CardInfoComponent, CardInfoService } from './card-info.component';
import { DeckDialogsService } from './deck-dialogs.component';
import { PacksService } from './pack-dialogs.component';

describe('deck builder dialogs', () => {
  let backend: HttpTestingController;
  let deck: DeckService;
  let dialogs: string[];
  let answers: (string | boolean)[];

  beforeEach(async () => {
    dialogs = [];
    answers = [];
    vi.spyOn(window, 'alert').mockImplementation((m) => dialogs.push(`alert: ${m}`));
    vi.spyOn(window, 'confirm').mockImplementation((m) => (dialogs.push(`confirm: ${m}`), answers.shift() === true));
    vi.spyOn(window, 'prompt').mockImplementation(
      (m) => (dialogs.push(`prompt: ${m}`), (answers.shift() as string) ?? null),
    );
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    backend = TestBed.inject(HttpTestingController);
    const loading = TestBed.inject(LegacyCardDataService).load();
    flushLegacyCardFiles(backend);
    await loading;
    deck = TestBed.inject(DeckService);
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

  describe('DeckDialogsService', () => {
    let service: DeckDialogsService;
    beforeEach(() => (service = TestBed.inject(DeckDialogsService)));

    it('offers to save before listing the decks, then lists them', () => {
      deck.addFromCollection('1_12', 'FREE_PEOPLE');
      answers = [false];
      service.openDeckList();
      take('/deck/list').flush(readServerResponse('deck-list.xml'));
      expect(dialogs[0]).toContain('confirm: Your deck has been modified.');
      expect(service.deckList()?.kind).toBe('decks');
      expect(service.deckList()!.decks.length).toBeGreaterThan(0);
    });

    it('renames a listed deck, and the open deck with it', () => {
      service.openDeckList();
      take('/deck/list').flush(readServerResponse('deck-list.xml'));
      const entry = service.deckList()!.decks[0];
      deck.setupDeck(
        { targetFormat: null, notes: '', ringBearer: null, ring: null, map: null, sites: [], drawDeck: [] },
        entry.name,
      );
      backend.match(() => true);
      answers = ['Brand new name'];
      service.renameDeck(entry);
      const rename = take('/deck/rename');
      expect(params(rename)).toEqual({ oldDeckName: entry.name, deckName: 'Brand new name' });
      rename.flush(readServerResponse('deck.xml'));
      expect(service.deckList()!.decks[0].name).toBe('Brand new name');
      expect(deck.deckName()).toBe('Brand new name');
    });

    it('deletes a deck after confirming; the open deck becomes a new one', () => {
      service.openDeckList();
      take('/deck/list').flush(readServerResponse('deck-list.xml'));
      const entry = service.deckList()!.decks[0];
      deck.setupDeck(
        { targetFormat: null, notes: '', ringBearer: null, ring: null, map: null, sites: [], drawDeck: [] },
        entry.name,
      );
      backend.match(() => true);
      answers = [false];
      service.deleteDeck(entry);
      backend.expectNone((req) => req.url.endsWith('/deck/delete'));
      answers = [true];
      service.deleteDeck(entry);
      take('/deck/delete').flush('<ok/>');
      expect(deck.deckName()).toBeNull();
      take('/deck/list').flush(readServerResponse('deck-list.xml'));
    });

    it('opens a library deck as unsaved, under its name', () => {
      service.openLibrary();
      take('/deck/libraryList').flush(readServerResponse('deck-list.xml'));
      expect(service.deckList()?.kind).toBe('library');
      service.openDeck(service.deckList()!.decks[0]);
      take('/deck/library').flush(readServerResponse('deck.xml'));
      expect(deck.deckName()).toBe(service.deckList()!.decks[0].name);
      expect(deck.contentsDirty()).toBe(true);
      backend.match(() => true);
    });

    it('starts the import dialog empty each time', () => {
      service.openImport();
      const round = service.importRound();
      service.openImport();
      expect(service.importRound()).toBe(round + 1);
      expect(service.importOpen()).toBe(true);
    });
  });

  describe('importing a decklist', () => {
    it('puts the recognised cards into the deck, by group', () => {
      deck.importDecklist('1x The One Ring\nRing-bearer: Frodo');
      const req = take('/collection/import/');
      expect(params(req)).toEqual({ decklist: '1x the one ring~frodo~' });
      req.flush(
        '<collection><card blueprintId="1_2" count="1" group="ring"/><card blueprintId="1_290" count="1" group="ringBearer"/>' +
          '<card blueprintId="1_12" count="2" group="fp" side="FREE_PEOPLE"/><card blueprintId="1_319" count="1" group="site"/></collection>',
      );
      expect(deck.deckContents()).toBe('1_290|1_2|1_319|1_12,1_12');
      expect(deck.label()).toEqual({ text: 'Imported Deck (unsaved)', modified: false });
      expect(deck.contentsDirty()).toBe(true);
      backend.match(() => true);
    });

    it('alerts when the decklist is too long', () => {
      deck.importDecklist('x');
      take('/collection/import/').flush('', { status: 414, statusText: 'URI Too Long' });
      expect(dialogs).toEqual(['alert: Deck too large to import.']);
    });
  });

  describe('PacksService', () => {
    let packs: PacksService;
    beforeEach(() => {
      vi.useFakeTimers({ toFake: ['setTimeout'] });
      packs = TestBed.inject(PacksService);
    });
    afterEach(() => vi.useRealTimers());

    it('opens a pack, reloads the collection and shows the contents', () => {
      packs.packClicked('FotR - League Starter', null);
      const open = take('/collection/default');
      expect(params(open)).toEqual({ pack: 'FotR - League Starter' });
      open.flush('<ok/>');
      backend.expectOne((req) => req.url.includes('/collection/default') && req.method === 'GET');
      vi.advanceTimersByTime(1000);
      take('/delivery/packContents').flush(readServerResponse('pack-delivery.xml'));
      expect(packs.deliveries().size).toBeGreaterThan(0);
      const [name, cards] = [...packs.deliveries().entries()][0];
      expect(cards.length).toBeGreaterThan(0);
      packs.closeDelivery(name);
      expect(packs.deliveries().has(name)).toBe(false);
    });

    it('asks which card of a selection pack to take', () => {
      packs.packClicked('(S)FotR - Starter', ['a', 'b']);
      expect(packs.selection()).toEqual({ packId: '(S)FotR - Starter', choices: ['a', 'b'] });
      packs.choose('b');
      expect(packs.selection()).toBeNull();
      const req = take('/collection/default');
      expect(params(req)).toEqual({ pack: '(S)FotR - Starter', selection: 'b' });
      req.flush('', { status: 404, statusText: 'Not Found' });
      expect(dialogs).toEqual([
        'alert: You have no pack of this type in your collection or that selection is not available for this pack.',
      ]);
    });
  });

  describe('CardInfoComponent', () => {
    it('sizes the dialog for the card, the zoom setting and the wiki link', async () => {
      const fixture = TestBed.createComponent(CardInfoComponent);
      TestBed.inject(CardInfoService).show({ kind: 'shown', blueprintId: '2_16', zone: 'player', role: '' });
      await fixture.whenStable();
      const el = fixture.nativeElement as HTMLElement;
      const dialog = el.querySelector<HTMLElement>('.ui-dialog')!;
      // As measured on the legacy page: a 372x519 card, 397px wide (25px padding), 589px tall (with the wiki link)
      expect(dialog.style.width).toBe('397px');
      const card = el.querySelector<HTMLElement>('#card-image-content > gemp-card-display > div')!;
      expect([card.style.width, card.style.height]).toEqual(['372px', '519px']);
      expect(el.querySelector<HTMLAnchorElement>('.card-info-footer a')!.href).toBe(
        'http://wiki.lotrtcgpc.net/wiki/LOTR02016',
      );
      expect(el.querySelector('.ui-dialog-titlebar-close .ui-icon')!.textContent).toBe('');
    });
  });
});
