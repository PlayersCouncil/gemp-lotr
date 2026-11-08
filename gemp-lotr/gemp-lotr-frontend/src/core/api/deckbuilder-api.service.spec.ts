import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, Observable } from 'rxjs';
import { readServerResponse } from '../../testing/files';
import { PAGE_QUERY_STRING } from '../session/session.service';
import { DeckbuilderApi } from './deckbuilder-api.service';

/** Request bodies/params as a plain object, without the cache-busting "_" parameter. */
function sentData(req: TestRequest): Record<string, string> {
  const params = req.request.method === 'GET' ? req.request.params : (req.request.body as typeof req.request.params);
  const result: Record<string, string> = {};
  for (const key of params.keys()) {
    if (key !== '_') {
      result[key] = params.get(key)!;
    }
  }
  return result;
}

describe('DeckbuilderApi', () => {
  let api: DeckbuilderApi;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=admin-target' },
      ],
    });
    api = TestBed.inject(DeckbuilderApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  /** Starts the call, checks method/path/data, answers with `response` and returns the call's result. */
  async function call<T>(
    call$: Observable<T>,
    method: string,
    path: string,
    data: Record<string, string>,
    response: string,
  ): Promise<T> {
    const result = firstValueFrom(call$);
    const req = backend.expectOne((r) => r.url === '/gemp-lotr-server' + path);
    expect(req.request.method).toBe(method);
    expect(sentData(req)).toEqual({ participantId: 'admin-target', ...data });
    req.flush(response);
    return result;
  }

  it('lists decks', async () => {
    const decks = await call(api.listDecks(), 'GET', '/deck/list', {}, readServerResponse('deck-list.xml'));
    expect(decks).toEqual([{ name: 'Test + deck & co', formatName: 'Fellowship Block' }]);
  });

  it('loads a deck by name', async () => {
    const deck = await call(
      api.getDeck('Test + deck & co'),
      'GET',
      '/deck',
      { deckName: 'Test + deck & co' },
      readServerResponse('deck.xml'),
    );
    expect(deck!.ringBearer).toBe('1_2');
  });

  it('saves a deck', async () => {
    const data = { deckName: 'D', targetFormat: 'fotr_block', notes: 'n', deckContents: '1_2|1_1||' };
    await call(api.saveDeck('D', 'fotr_block', 'n', '1_2|1_1||'), 'POST', '/deck', data, '<ok/>');
  });

  it('renames and deletes decks', async () => {
    const renamed = await call(
      api.renameDeck('Old', 'New'),
      'POST',
      '/deck/rename',
      { oldDeckName: 'Old', deckName: 'New' },
      readServerResponse('deck.xml'),
    );
    expect(renamed).not.toBeNull();
    await call(api.deleteDeck('New'), 'POST', '/deck/delete', { deckName: 'New' }, '<result>OK</result>');
  });

  it('gets the share URL as text', async () => {
    const url = await call(
      api.shareDeckUrl('D'),
      'GET',
      '/deck/share',
      { deckName: 'D' },
      'https://example/share?id=x',
    );
    expect(url).toBe('https://example/share?id=x');
  });

  it('gets the deck stats HTML, leaving out a missing league code', async () => {
    const html = await call(
      api.getDeckStatsHtml('c', 'fotr_block', 'default', null),
      'POST',
      '/deck/stats',
      { targetFormat: 'fotr_block', collectionName: 'default', deckContents: 'c' },
      readServerResponse('stats.html'),
    );
    expect(html).toContain('<b>Free People</b>: 4');
  });

  it('sends the league code for RTMD stats', async () => {
    await call(
      api.getDeckStatsHtml('c', '1700000002', 'default', '1700000002'),
      'POST',
      '/deck/stats',
      { targetFormat: '1700000002', collectionName: 'default', deckContents: 'c', leagueCode: '1700000002' },
      '',
    );
  });

  it('gets the formats without a participantId', async () => {
    const result = firstValueFrom(api.getFormats());
    const req = backend.expectOne('/gemp-lotr-server/deck/formats');
    expect(req.request.serializeBody()).toBe('includeEvents=false');
    req.flush('{"Formats":{"fotr_block":{"code":"fotr_block","name":"Fellowship Block","order":1,"hall":true}}}');
    expect((await result).Formats['fotr_block'].name).toBe('Fellowship Block');
  });

  it('converts a deck to errata', async () => {
    const deck = await call(
      api.convertErrata('pc_fotr_block', 'c'),
      'POST',
      '/deck/convert',
      { targetFormat: 'pc_fotr_block', deckContents: 'c' },
      readServerResponse('convert.xml'),
    );
    expect(deck!.targetFormat!.formatCode).toBe('pc_fotr_block');
  });

  it('lists and loads library decks', async () => {
    await call(api.listLibraryDecks(), 'GET', '/deck/libraryList', {}, '<decks/>');
    await call(api.getLibraryDeck('Starter'), 'GET', '/deck/library', { deckName: 'Starter' }, '<deck/>');
    expect(api.libraryDeckHtmlUrl('FotR Starter #1')).toBe(
      '/gemp-lotr-server/deck/libraryHtml?deckName=FotR%20Starter%20%231',
    );
  });

  it('pages through a collection, with the collection type unencoded in the path', async () => {
    const page = await call(
      api.getCollection('1699999999:sealed', 'cardType:-THE_ONE_RING', 18, 18),
      'GET',
      '/collection/1699999999:sealed',
      { filter: 'cardType:-THE_ONE_RING', start: '18', count: '18' },
      readServerResponse('collection-default.xml'),
    );
    expect(page.total).toBe(3984);
  });

  it('lists collection types', async () => {
    const types = await call(
      api.getCollectionTypes(),
      'GET',
      '/collection',
      {},
      readServerResponse('collection-types.xml'),
    );
    expect(types).toHaveLength(2);
  });

  it('imports a decklist', async () => {
    const decklist = '1x Gandalf, Friend of the Shirefolk\n2x Bill the Pony';
    const items = await call(
      api.importDecklist(decklist),
      'GET',
      '/collection/import/',
      { decklist },
      readServerResponse('collection-with-packs.xml'),
    );
    expect(items.length).toBeGreaterThan(0);
  });

  it('opens packs and selection packs', async () => {
    await call(
      api.openPack('permanent', 'FotR - Booster'),
      'POST',
      '/collection/permanent',
      { pack: 'FotR - Booster' },
      '<result>OK</result>',
    );
    await call(
      api.openSelectionPack('permanent', '(S)FotR - Starter', '1_2'),
      'POST',
      '/collection/permanent',
      { pack: '(S)FotR - Starter', selection: '1_2' },
      '<result>OK</result>',
    );
  });

  it('lists only the RTMD leagues the player is a member of', async () => {
    const leagues = await call(api.getMemberRtmdLeagues(), 'GET', '/league', {}, readServerResponse('leagues.xml'));
    expect(leagues.map((l) => l.code)).toEqual(['1700000002']);
  });
});
