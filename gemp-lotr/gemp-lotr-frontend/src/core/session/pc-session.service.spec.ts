import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { provideGempHttp } from '../api/gemp-interceptors';
import { SessionChannel } from '../platform/hall-tab.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { NO_PC_SESSION } from './pc-account';
import { NAME_TAKEN, NOT_LINKED, UNAVAILABLE } from './pc-account-messages';
import { PcSessionService } from './pc-session.service';

class FakeNavigation {
  readonly visited: string[] = [];
  go(url: string): void {
    this.visited.push(url);
  }
}

class FakeChannel {
  announced = 0;
  announce(): void {
    this.announced++;
  }
}

describe('PcSessionService', () => {
  let backend: HttpTestingController;
  let pc: PcSessionService;
  let navigation: FakeNavigation;
  let channel: FakeChannel;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
        { provide: PageNavigation, useClass: FakeNavigation },
        { provide: SessionChannel, useClass: FakeChannel },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    pc = TestBed.inject(PcSessionService);
    navigation = TestBed.inject(PageNavigation) as unknown as FakeNavigation;
    channel = TestBed.inject(SessionChannel) as unknown as FakeChannel;
  }

  afterEach(() => backend.verify());

  const expectSession = (): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET');
  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth' + path && r.method === 'POST');
  const refuse = (request: TestRequest, status: number, code: string) =>
    request.flush(JSON.stringify({ error: code }), { status, statusText: 'Refused' });

  async function loaded(fixture = 'argonauth-session-aliases.json') {
    const done = pc.load();
    expectSession().flush(readServerResponse(fixture));
    await done;
  }

  it('asks for the session once, shared, and again on refresh', async () => {
    setUp();
    expect(pc.session()).toBeNull();
    expect(pc.kind()).toBeNull();
    const first = pc.load();
    const second = pc.load();
    expectSession().flush(readServerResponse('argonauth-session.json'));
    expect(await first).toBe(await second);
    expect(pc.kind()).toBe('pc');
    const again = pc.refresh();
    expectSession().flush(readServerResponse('argonauth-session-nobody.json'));
    await again;
    expect(pc.kind()).toBe('nobody');
  });

  it('is test mode with nobody when the server gives no session, and keeps one it had', async () => {
    setUp();
    const first = pc.load();
    expectSession().flush('', { status: 404, statusText: 'Not Found' });
    await expect(first).resolves.toBe(NO_PC_SESSION);
    expect(pc.kind()).toBe('test');

    const known = pc.refresh();
    expectSession().flush(readServerResponse('argonauth-session-password.json'));
    await known;
    const failed = pc.refresh();
    expectSession().error(new ProgressEvent('error'));
    await failed;
    expect(pc.session()?.player).toBe('Samwise');
  });

  it('enters an alias: the other tabs are told, and the page goes to the return place', async () => {
    setUp('?return=%2Fevents%2Fleagues');
    await loaded();
    const result = pc.enter(pc.session()!.aliases[1]);
    const request = expectPost('/enter');
    expect(request.request.body.toString()).toBe('playerId=900002');
    request.flush(readServerResponse('argonauth-session.json'));
    expect(await result).toBeNull();
    expect(channel.announced).toBe(1);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/events/leagues']);
  });

  it('creates an alias, which enters it: to the hall without a return place', async () => {
    setUp();
    await loaded();
    const result = pc.create('Merry');
    expectPost('/create').flush(readServerResponse('argonauth-session.json'));
    expect(await result).toBeNull();
    expect(pc.session()?.player).toBe('Underhill');
    expect(channel.announced).toBe(1);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('links an account: the list is the answer, and the page stays', async () => {
    setUp();
    await loaded('argonauth-session-no-aliases.json');
    const result = pc.link('Merry', 'secret');
    expectPost('/link').flush(readServerResponse('argonauth-session-aliases.json'));
    expect(await result).toBeNull();
    expect(pc.session()?.aliases.length).toBe(3);
    // the answer is the session: asked for again, it is the same
    expect(await pc.load()).toBe(pc.session());
    expect(channel.announced).toBe(0);
    expect(navigation.visited).toEqual([]);
  });

  it('gives a refusal its wording and asks for the session again', async () => {
    setUp();
    await loaded();
    const result = pc.enter(pc.session()!.aliases[0]);
    refuse(expectPost('/enter'), 403, 'not-linked');
    await settle();
    expectSession().flush(readServerResponse('argonauth-session-no-aliases.json'));
    expect(await result).toEqual({ code: 'not-linked', message: NOT_LINKED });
    await settle();
    expect(pc.session()?.aliases).toEqual([]);
    expect(navigation.visited).toEqual([]);

    const taken = pc.create('Frodo');
    refuse(expectPost('/create'), 409, 'name-taken');
    await settle();
    expectSession().flush(readServerResponse('argonauth-session-no-aliases.json'));
    expect(await taken).toEqual({ code: 'name-taken', message: NAME_TAKEN });

    const down = pc.link('a', 'b');
    expectPost('/link').flush('<html>Bad Gateway</html>', { status: 503, statusText: 'Unavailable' });
    await settle();
    expectSession().flush(readServerResponse('argonauth-session-no-aliases.json'));
    expect(await down).toEqual({ code: 'unavailable', message: UNAVAILABLE });
  });

  it('sends create and link through a fresh PC sign-in on 401 reauth, coming back with the form open', async () => {
    setUp('?return=%2Fdeckbuilder');
    await loaded();
    const create = pc.create('Merry');
    refuse(expectPost('/create'), 401, 'reauth');
    expect(await create).toBeNull();
    const link = pc.link('a', 'b');
    refuse(expectPost('/link'), 401, 'reauth');
    expect(await link).toBeNull();
    expect(navigation.visited).toEqual([
      '/gemp-lotr-server/argonauth/login?then=create&return=%2Fdeckbuilder',
      '/gemp-lotr-server/argonauth/login?then=link&return=%2Fdeckbuilder',
    ]);
  });

  it('goes straight to the page for a password login', () => {
    setUp('?return=%2Faccount');
    pc.enterLoggedIn();
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/account']);
  });

  it('logs out: the other tabs are told and the browser goes where the server says, else the login page', async () => {
    setUp();
    const all = pc.logout('all');
    const request = expectPost('/logout');
    expect(request.request.body.toString()).toBe('scope=all');
    request.flush(readServerResponse('argonauth-logout-all.json'));
    await all;
    const alias = pc.logout('alias');
    expectPost('/logout').flush('', { status: 500, statusText: 'Error' });
    await alias;
    expect(channel.announced).toBe(2);
    expect(navigation.visited).toEqual([
      'https://auth.lotrtcgpc.net/logout?post_logout_redirect_uri=https%3A%2F%2Fplay.lotrtcgpc.net%2Fgemp-lotr%2Fng%2Flogin',
      '/gemp-lotr/ng/login',
    ]);
  });
});
