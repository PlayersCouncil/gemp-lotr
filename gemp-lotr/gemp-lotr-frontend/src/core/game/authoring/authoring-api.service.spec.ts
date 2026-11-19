import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PageExit } from '../../platform/page-exit';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { AuthoringApi } from './authoring-api.service';

const AUTHOR = '/gemp-lotr-server/scenarios/author/';

describe('AuthoringApi', () => {
  let api: AuthoringApi;
  let backend: HttpTestingController;
  let beacon: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    beacon = vi.fn(() => true);
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        // An admin acting as somebody else: authoring is still the logged-in player's own
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=bob' },
        { provide: PageExit, useValue: { beacon } },
        AuthoringApi,
      ],
    });
    api = TestBed.inject(AuthoringApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  /** Makes the call, answers it with `{}`, and gives what was sent: the method, the path, and the form or query. */
  async function sent(call: Observable<unknown>): Promise<[string, string, string]> {
    const answer = firstValueFrom(call);
    const [request] = backend.match((r) => r.url.startsWith(AUTHOR));
    expect(request).toBeDefined();
    const { method, url, params, body } = request!.request;
    request!.flush('{"ok":true}');
    expect(await answer).toEqual({ ok: true });
    const form =
      method === 'GET'
        ? params
            .keys()
            .filter((key) => key !== '_')
            .map((key) => `${key}=${params.get(key)}`)
            .join('&')
        : (body?.toString() ?? '');
    expect(form).not.toContain('participantId');
    return [method, url.substring(AUTHOR.length), form];
  }

  it('reads the state, the source, cards and pictures', async () => {
    expect(await sent(api.state())).toEqual(['GET', 'state', '']);
    expect(await sent(api.source())).toEqual(['GET', 'source', '']);
    expect(await sent(api.cards('fro'))).toEqual(['GET', 'cards', 'q=fro']);
    expect(await sent(api.cards('bag', true))).toEqual(['GET', 'cards', 'q=bag&sites=true']);
    expect(await sent(api.images())).toEqual(['GET', 'images', '']);
  });

  it('opens a draft, and a new one', async () => {
    expect(await sent(api.open('basics-01'))).toEqual(['POST', 'open', 'id=basics-01']);
    expect(await sent(api.open('mine', { title: 'Mine', canonical: false }))).toEqual([
      'POST',
      'open',
      'id=mine&new=true&title=Mine&canonical=false',
    ]);
  });

  it('changes the draft and goes about it', async () => {
    expect(await sent(api.edit({ steps: [{ op: 'delete', uid: 's1' }] }))).toEqual([
      'POST',
      'edit',
      `patch=${encodeURIComponent('{"steps":[{"op":"delete","uid":"s1"}]}')}`,
    ]);
    expect(await sent(api.edit({ top: { title: null } }, 3))).toEqual([
      'POST',
      'edit',
      `patch=${encodeURIComponent('{"top":{"title":null}}')}&goto=3`,
    ]);
    expect(await sent(api.goTo(4))).toEqual(['POST', 'goto', 'step=4']);
    expect(await sent(api.goTo(4, 0))).toEqual(['POST', 'goto', 'step=4']);
    expect(await sent(api.goTo(4, 2))).toEqual(['POST', 'goto', 'step=4&sub=2']);
    expect(await sent(api.test(2))).toEqual(['POST', 'test', 'step=2']);
    expect(await sent(api.resume())).toEqual(['POST', 'resume', '']);
  });

  it('answers for the opponent, changes the board, renames a card, and writes steps where the game is', async () => {
    expect(await sent(api.answer('play(runner1)', true))).toEqual(['POST', 'answer', 'term=play(runner1)&record=true']);
    expect(await sent(api.answerLeast())).toEqual(['POST', 'answer', 'least=true&record=false']);
    expect(await sent(api.apply(['wound(frodo, 1)', 'twilight(3)'], false))).toEqual([
      'POST',
      'do',
      `op=${encodeURIComponent('wound(frodo, 1), twilight(3)')}&setup=false`,
    ]);
    expect(await sent(api.alias('frodo', 'hero'))).toEqual(['POST', 'alias', 'from=frodo&to=hero']);
    expect(await sent(api.here())).toEqual(['POST', 'here', '']);
    expect(await sent(api.before())).toEqual(['POST', 'before', '']);
    expect(await sent(api.retake())).toEqual(['POST', 'retake', '']);
  });

  it('sends only the settings that change', async () => {
    expect(await sent(api.settings({ recording: false }))).toEqual(['POST', 'settings', 'recording=false']);
    expect(await sent(api.settings({ askOpponent: true, way: ['plan', 'help'] }))).toEqual([
      'POST',
      'settings',
      `askOpponent=true&way=${encodeURIComponent('plan,help')}`,
    ]);
    expect(await sent(api.settings({ way: [], seenLeftOut: true }))).toEqual([
      'POST',
      'settings',
      'way=&seenLeftOut=true',
    ]);
  });

  it('saves, copies, renames, submits, checks, tries other answers, and closes', async () => {
    expect(await sent(api.save())).toEqual(['POST', 'save', '']);
    expect(await sent(api.save({ as: 'copy', path: 'a/b.hjson', canonical: false }))).toEqual([
      'POST',
      'save',
      `as=copy&path=${encodeURIComponent('a/b.hjson')}&canonical=false`,
    ]);
    expect(await sent(api.submit())).toEqual(['POST', 'submit', '']);
    expect(await sent(api.check())).toEqual(['POST', 'check', '']);
    expect(await sent(api.offScript())).toEqual(['POST', 'offscript', '']);
    expect(await sent(api.close())).toEqual(['POST', 'close', '']);
  });

  it('says the page is going away with a beacon', () => {
    expect(api.left()).toBe(true);
    expect(beacon).toHaveBeenCalledWith('/gemp-lotr-server/scenarios/author/left', { left: 'true' });
  });

  it("fails with the server's reason", async () => {
    const answer = firstValueFrom(api.here());
    backend.expectOne(`${AUTHOR}here`).flush('{"error":"Nothing is waiting."}', { status: 400, statusText: 'Bad' });
    const error = (await answer.catch((e: unknown) => e)) as ApiError;
    expect(error.said).toBe('Nothing is waiting.');
  });
});
