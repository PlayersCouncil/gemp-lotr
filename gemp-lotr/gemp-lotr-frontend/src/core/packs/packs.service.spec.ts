import { provideDeckbuilderState } from '../deckbuilder-state';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FakeNotifier, provideFakeNotifier } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PacksService } from './packs.service';

describe('PacksService', () => {
  let backend: HttpTestingController;
  let dialogs: string[];

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
    ({ dialogs } = TestBed.inject(FakeNotifier));
    backend = TestBed.inject(HttpTestingController);
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
    // pack-delivery.xml: a pack, then two cards, in the order the server gave them
    expect(name).toBe('permanent');
    expect(cards).toEqual([
      { blueprintId: 'FotR - Booster', count: 2, pack: true },
      { blueprintId: '1_12', count: 1, pack: false },
      { blueprintId: '1_13*', count: 1, pack: false },
    ]);
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
