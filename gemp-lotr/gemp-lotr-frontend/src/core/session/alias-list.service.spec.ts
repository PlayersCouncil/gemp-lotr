import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { sessionWithAliases } from '../../testing/pc-sessions';
import { provideGempHttp } from '../api/gemp-interceptors';
import { SessionChannel } from '../platform/hall-tab.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { ALIAS_PAGE_SIZE, AliasList } from './alias-list.service';
import { BAD_CREDENTIALS, INVALID_ALIAS_NAME, NAME_TAKEN, NOT_LINKED } from './pc-account-messages';

class FakeNavigation {
  readonly visited: string[] = [];
  go(url: string): void {
    this.visited.push(url);
  }
}

describe('AliasList', () => {
  let backend: HttpTestingController;
  let list: AliasList;
  let navigation: FakeNavigation;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
        { provide: PageNavigation, useClass: FakeNavigation },
        { provide: SessionChannel, useValue: { announce: () => undefined } },
        AliasList,
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    list = TestBed.inject(AliasList);
    navigation = TestBed.inject(PageNavigation) as unknown as FakeNavigation;
  }

  afterEach(() => backend.verify());

  const expectSession = (): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET');
  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth' + path && r.method === 'POST');
  const refuse = (request: TestRequest, status: number, code: string) =>
    request.flush(JSON.stringify({ error: code }), { status, statusText: 'Refused' });

  async function started(body: string) {
    const done = list.start();
    expectSession().flush(body);
    await done;
  }

  /** The session asked for again after a refusal. */
  async function answerRefresh(body = readServerResponse('argonauth-session-aliases.json')) {
    await settle();
    expectSession().flush(body);
    await settle();
  }

  it('pages the aliases at ten', async () => {
    setUp();
    await started(sessionWithAliases(11));
    expect(ALIAS_PAGE_SIZE).toBe(10);
    expect(list.pages()).toBe(2);
    expect(list.shown().map((alias) => alias.name)).toEqual(Array.from({ length: 10 }, (_, i) => `Alias${i + 1}`));
    list.previousPage();
    expect(list.page()).toBe(0);
    list.nextPage();
    expect(list.shown().map((alias) => alias.name)).toEqual(['Alias11']);
    list.nextPage();
    expect(list.page()).toBe(1);
  });

  it('has one page, empty, before the session and without aliases', async () => {
    setUp();
    expect(list.pages()).toBe(1);
    expect(list.blockedBy()).toBeNull();
    expect(list.canEnter({ id: 1, name: 'x', current: false, busy: false })).toBe(false);
    await started(readServerResponse('argonauth-session-no-aliases.json'));
    expect(list.shown()).toEqual([]);
    expect(list.pages()).toBe(1);
  });

  it('opens the form a sign-in came back for, only for a PC account', async () => {
    setUp('?open=link');
    await started(readServerResponse('argonauth-session-aliases.json'));
    expect(list.form()).toBe('link');
    TestBed.resetTestingModule();
    setUp('?open=create');
    await started(readServerResponse('argonauth-session-password.json'));
    expect(list.form()).toBeNull();
  });

  it('enters an alias; a refusal shows beside it', async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    const [frodo] = list.aliases();
    const refused = list.enter(frodo);
    expect(list.busy()).toBe(true);
    refuse(expectPost('/enter'), 403, 'not-linked');
    await answerRefresh();
    await refused;
    expect(list.busy()).toBe(false);
    expect(list.aliasError()).toEqual({ id: frodo.id, message: NOT_LINKED });

    const entered = list.enter(frodo);
    expect(list.aliasError()).toBeNull();
    expectPost('/enter').flush(readServerResponse('argonauth-session.json'));
    await entered;
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('while another alias is busy, enters only that one and creates none', async () => {
    setUp();
    await started(readServerResponse('argonauth-session.json'));
    const [frodo, underhill] = list.aliases();
    expect(list.blockedBy()).toBe('Underhill');
    expect(list.canEnter(frodo)).toBe(false);
    expect(list.canEnter(underhill)).toBe(true);
    await list.enter(frodo);
    list.open('create');
    expect(list.form()).toBeNull();
    const entered = list.enter(underhill);
    expectPost('/enter').flush(readServerResponse('argonauth-session.json'));
    await entered;
  });

  it("checks a new alias's name before sending it, then creates it", async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    list.open('create');
    expect(list.form()).toBe('create');
    list.setName('x');
    await list.submit();
    expect(list.formError()).toBe(INVALID_ALIAS_NAME);

    list.setName('  Merry ');
    const created = list.submit();
    const request = expectPost('/create');
    expect(request.request.body.toString()).toBe('name=Merry');
    request.flush(readServerResponse('argonauth-session.json'));
    await created;
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('shows a refused name under the form, and takes it again once changed', async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    list.open('create');
    list.setName('Frodo');
    const taken = list.submit();
    refuse(expectPost('/create'), 409, 'name-taken');
    await answerRefresh();
    await taken;
    expect(list.formError()).toBe(NAME_TAKEN);
    list.setName('Merry');
    const created = list.submit();
    expect(list.formError()).toBeNull();
    expectPost('/create').flush(readServerResponse('argonauth-session.json'));
    await created;
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('goes through a sign-in first when the last one has lapsed', async () => {
    setUp();
    await started(readServerResponse('argonauth-session-stale.json'));
    list.open('create');
    list.open('link');
    expect(list.form()).toBeNull();
    expect(navigation.visited).toEqual([
      '/gemp-lotr-server/argonauth/login?then=create',
      '/gemp-lotr-server/argonauth/login?then=link',
    ]);
  });

  it('links an account: the form closes and the page with the new alias is shown', async () => {
    setUp();
    await started(sessionWithAliases(10));
    list.open('link');
    list.setLogin('Old');
    list.setPassword('wrong');
    const refused = list.submit();
    refuse(expectPost('/link'), 401, 'bad-credentials');
    await answerRefresh(sessionWithAliases(10));
    await refused;
    expect(list.formError()).toBe(BAD_CREDENTIALS);
    expect(list.password()).toBe('');
    expect(list.login()).toBe('Old');

    list.setPassword('right');
    const linked = list.submit();
    const request = expectPost('/link');
    expect(request.request.body.toString()).toBe('login=Old&password=right');
    request.flush(sessionWithAliases(11));
    await linked;
    expect([list.form(), list.formError(), list.login()]).toEqual([null, null, '']);
    expect(list.page()).toBe(1);
    expect(list.shown().map((alias) => alias.name)).toEqual(['Alias11']);
    expect(navigation.visited).toEqual([]);
  });

  it('cancels a form, emptying it', async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    list.open('link');
    list.setLogin('a');
    list.setPassword('b');
    list.cancel();
    expect([list.form(), list.login(), list.password(), list.name()]).toEqual([null, '', '', '']);
  });

  it('logs out everything, and enters a password login straight away', async () => {
    setUp();
    await started(readServerResponse('argonauth-session-password.json'));
    list.enterLoggedIn();
    const out = list.logout();
    expect(list.busy()).toBe(true);
    await list.logout();
    const request = expectPost('/logout');
    expect(request.request.body.toString()).toBe('scope=all');
    request.flush(readServerResponse('argonauth-logout-alias.json'));
    await out;
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/', '/gemp-lotr/ng/login']);
  });

  it("keeps a refused Enter Gemp's message at the list's level when its alias has left the list", async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    const ringbearer = list.aliases()[2];
    const refused = list.enter(ringbearer);
    refuse(expectPost('/enter'), 403, 'not-linked');
    // Asked again, the session no longer has it
    await answerRefresh(
      JSON.stringify({
        ...JSON.parse(readServerResponse('argonauth-session-aliases.json')),
        aliases: JSON.parse(readServerResponse('argonauth-session-aliases.json')).aliases.slice(0, 2),
      }),
    );
    await refused;
    expect(list.aliases().map((alias) => alias.name)).toEqual(['Frodo', 'Underhill']);
    expect(list.listError()).toBe(NOT_LINKED);
    // until the next action
    list.open('link');
    expect([list.listError(), list.aliasError()]).toEqual([null, null]);
  });

  it("keeps a refused alias's message beside it while it is listed, and none at the list's level", async () => {
    setUp();
    await started(readServerResponse('argonauth-session-aliases.json'));
    const refused = list.enter(list.aliases()[0]);
    refuse(expectPost('/enter'), 403, 'banned');
    await answerRefresh();
    await refused;
    expect(list.aliasError()?.message).toBe('That alias is banned.');
    expect(list.listError()).toBeNull();
    list.cancel();
    expect(list.aliasError()).toBeNull();
  });
});
