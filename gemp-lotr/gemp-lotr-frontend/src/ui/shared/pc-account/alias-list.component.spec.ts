import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { SessionChannel } from '../../../core/platform/hall-tab.service';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { PageNavigation } from '../../../core/platform/page-navigation';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { sessionWithAliases } from '../../../testing/pc-sessions';
import { AliasListComponent } from './alias-list.component';

class FakeNavigation {
  readonly visited: string[] = [];
  go(url: string): void {
    this.visited.push(url);
  }
}

describe('AliasListComponent', () => {
  let backend: HttpTestingController;
  let navigation: FakeNavigation;
  let announced: number;

  function setUp(queryString = '') {
    announced = 0;
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
        { provide: PageNavigation, useClass: FakeNavigation },
        { provide: SessionChannel, useValue: { announce: () => announced++ } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    navigation = TestBed.inject(PageNavigation) as unknown as FakeNavigation;
  }

  afterEach(() => backend.verify());

  const expectSession = (): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET');
  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth' + path && r.method === 'POST');
  const bodyOf = (request: TestRequest) => (request.request.body as HttpParams).toString();
  const refuse = (request: TestRequest, status: number, code: string) =>
    request.flush(JSON.stringify({ error: code }), { status, statusText: 'Refused' });

  /** The list (on the login page unless `place`), its session answered with `body`. */
  async function render(body: string, place: 'login' | 'account' = 'login') {
    const fixture = TestBed.createComponent(AliasListComponent);
    fixture.componentRef.setInput('place', place);
    expectSession().flush(body);
    const stable = async () => {
      await settle();
      await fixture.whenStable();
    };
    await stable();
    const el = fixture.nativeElement as HTMLElement;
    const query = <T extends HTMLElement = HTMLElement>(selector: string) => el.querySelector<T>(selector);
    const all = (selector: string) => Array.from(el.querySelectorAll<HTMLElement>(selector));
    const names = () => all('.gd-alias-list__name').map((name) => name.textContent);
    const row = (name: string) => query(`.gd-alias-list__alias[data-alias="${name}"]`)!;
    const enterOf = (name: string) => row(name).querySelector<HTMLButtonElement>('[data-action="enter"]')!;
    const button = (action: string) =>
      query<HTMLButtonElement>(`.gd-alias-list__actions [data-action="${action}"], form [data-action="${action}"]`)!;
    const type = async (field: string, value: string) => {
      const input = query<HTMLInputElement>(`.gd-pc-form__field[data-field="${field}"] input`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
      await fixture.whenStable();
    };
    const submit = async () => {
      query<HTMLFormElement>('form.gd-pc-form__form')!.requestSubmit();
      await stable();
    };
    return { fixture, el, query, all, names, row, enterOf, button, type, submit, stable };
  }

  it('shows a PC account and its aliases, each with Enter Gemp (DOM contract)', async () => {
    setUp();
    const { el, query, names, row, enterOf, all } = await render(readServerResponse('argonauth-session.json'));
    expect(el.classList).toContain('gd-alias-list');
    expect([el.dataset['place'], el.dataset['kind']]).toEqual(['login', 'pc']);
    expect(query('.gd-alias-list__signed-in')!.textContent).toBe('Signed in as Frodo Baggins');
    expect(query('.gd-alias-list__aliases')!.getAttribute('aria-label')).toBe('Your Gemp aliases');
    expect(names()).toEqual(['Frodo', 'Underhill']);
    expect(row('Underhill').hasAttribute('data-current')).toBe(true);
    expect(row('Underhill').hasAttribute('data-busy')).toBe(true);
    expect(all('.gd-alias-list__tag').map((tag) => `${tag.dataset['tag']}:${tag.textContent}`)).toEqual([
      'current:current',
      'playing:playing',
    ]);
    expect(enterOf('Underhill').textContent!.trim()).toBe('Enter Gemp');
    expect(all('.gd-alias-list__actions .gd-button').map((b) => b.textContent!.trim())).toEqual([
      'Create New Gemp Alias',
      'Link Existing Gemp Account',
      'Log Out',
    ]);
    expect(query('.gd-alias-list__pager')).toBeNull();
    expect(query('form')).toBeNull();
  });

  it("while this browser's alias is busy, disables every other Enter Gemp, and Create, saying why", async () => {
    setUp();
    const { query, enterOf, button } = await render(readServerResponse('argonauth-session.json'));
    const reason = query('.gd-alias-list__blocked')!;
    expect(reason.textContent).toBe(
      'Underhill is in a game, queue, table or tournament: finish or leave it to switch.',
    );
    expect(enterOf('Frodo').disabled).toBe(true);
    expect(enterOf('Frodo').getAttribute('aria-describedby')).toBe(reason.id);
    expect(enterOf('Underhill').disabled).toBe(false);
    expect(enterOf('Underhill').hasAttribute('aria-describedby')).toBe(false);
    expect(button('create').disabled).toBe(true);
    expect(button('create').getAttribute('aria-describedby')).toBe(reason.id);
    expect(button('link').disabled).toBe(false);
  });

  it('enters an alias: posts its id, tells the other tabs, and goes to the hall', async () => {
    setUp();
    const { el, enterOf, stable } = await render(readServerResponse('argonauth-session-aliases.json'));
    enterOf('Underhill').click();
    await stable();
    expect(el.hasAttribute('data-busy')).toBe(true);
    expect(el.getAttribute('aria-busy')).toBe('true');
    expect(enterOf('Frodo').disabled).toBe(true);
    const request = expectPost('/enter');
    expect(bodyOf(request)).toBe('playerId=900002');
    request.flush(readServerResponse('argonauth-session.json'));
    await stable();
    expect(announced).toBe(1);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('shows a refused Enter Gemp beside its alias', async () => {
    setUp();
    const { row, enterOf, stable, all } = await render(readServerResponse('argonauth-session-aliases.json'));
    enterOf('Ringbearer').click();
    refuse(expectPost('/enter'), 403, 'banned');
    await stable();
    expectSession().flush(readServerResponse('argonauth-session-aliases.json'));
    await stable();
    expect(row('Ringbearer').querySelector('.gd-alias-list__error')!.textContent).toBe('That alias is banned.');
    expect(row('Ringbearer').querySelector('.gd-alias-list__error')!.getAttribute('role')).toBe('alert');
    expect(all('.gd-alias-list__error').length).toBe(1);
    expect(navigation.visited).toEqual([]);
  });

  it('pages the list at ten', async () => {
    setUp();
    const { query, names, stable } = await render(sessionWithAliases(11));
    expect(names().length).toBe(10);
    const pager = query('.gd-alias-list__pager')!;
    const previous = pager.querySelector<HTMLButtonElement>('[data-action="previous"]')!;
    const next = pager.querySelector<HTMLButtonElement>('[data-action="next"]')!;
    expect(pager.querySelector('.gd-alias-list__page')!.textContent).toBe('Page 1 of 2');
    expect([previous.disabled, next.disabled]).toEqual([true, false]);
    next.click();
    await stable();
    expect(names()).toEqual(['Alias11']);
    expect(pager.querySelector('.gd-alias-list__page')!.textContent).toBe('Page 2 of 2');
    expect([previous.disabled, next.disabled]).toEqual([false, true]);
    previous.click();
    await stable();
    expect(names()[0]).toBe('Alias1');
  });

  it("creates an alias: checks the name first, shows the server's refusal, enters it once made", async () => {
    setUp();
    const { query, button, type, submit, stable } = await render(readServerResponse('argonauth-session-aliases.json'));
    button('create').click();
    await stable();
    expect(button('create').getAttribute('aria-expanded')).toBe('true');
    const form = query('form.gd-pc-form__form')!;
    expect(form.dataset['form']).toBe('create');
    expect(form.getAttribute('aria-label')).toBe('Create New Gemp Alias');
    const name = query<HTMLInputElement>('.gd-pc-form__field[data-field="name"] input')!;
    expect(name.maxLength).toBe(30);
    expect(query('.gd-pc-form__field[data-field="name"] .gd-pc-form__label')!.textContent).toBe('Name');
    expect(button('submit').textContent!.trim()).toBe('Create');

    await type('name', 'bad name!');
    await submit();
    expect(form.querySelector('.gd-pc-form__error')!.textContent).toBe('A name is 2 to 30 letters, digits, _ or -.');

    await type('name', 'Frodo');
    await submit();
    refuse(expectPost('/create'), 409, 'name-taken');
    await stable();
    expectSession().flush(readServerResponse('argonauth-session-aliases.json'));
    await stable();
    expect(form.querySelector('.gd-pc-form__error')!.textContent).toBe('That name is taken.');

    await type('name', 'Merry');
    await submit();
    const request = expectPost('/create');
    expect(bodyOf(request)).toBe('name=Merry');
    request.flush(readServerResponse('argonauth-session.json'));
    await stable();
    expect(announced).toBe(1);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('sends Create through a fresh PC sign-in on 401 reauth', async () => {
    setUp('?return=%2Fdeckbuilder');
    const { button, type, submit, stable } = await render(readServerResponse('argonauth-session-aliases.json'));
    button('create').click();
    await stable();
    await type('name', 'Merry');
    await submit();
    refuse(expectPost('/create'), 401, 'reauth');
    await stable();
    expect(navigation.visited).toEqual(['/gemp-lotr-server/argonauth/login?then=create&return=%2Fdeckbuilder']);
  });

  it('goes to a PC sign-in before showing a form when the last one has lapsed', async () => {
    setUp();
    const { query, button, stable } = await render(readServerResponse('argonauth-session-stale.json'));
    button('link').click();
    button('create').click();
    await stable();
    expect(query('form')).toBeNull();
    expect(navigation.visited).toEqual([
      '/gemp-lotr-server/argonauth/login?then=link',
      '/gemp-lotr-server/argonauth/login?then=create',
    ]);
  });

  it('links an account: refused, it says why; linked, the alias joins the list and nothing is entered', async () => {
    setUp();
    const { query, names, button, type, submit, stable } = await render(
      readServerResponse('argonauth-session-no-aliases.json'),
      'account',
    );
    expect(query('.gd-alias-list__empty')!.textContent).toBe(
      'No Gemp alias yet: create one, or link an account you had.',
    );
    button('link').click();
    await stable();
    expect(query('form')!.dataset['form']).toBe('link');
    expect(Array.from(query('form')!.querySelectorAll('.gd-pc-form__label')).map((label) => label.textContent)).toEqual(
      ['Username', 'Password'],
    );
    expect(query<HTMLInputElement>('input[name="password"]')!.type).toBe('password');
    await type('login', 'Sam');
    await type('password', 'wrong');
    await submit();
    refuse(expectPost('/link'), 401, 'bad-credentials');
    await stable();
    expectSession().flush(readServerResponse('argonauth-session-no-aliases.json'));
    await stable();
    expect(query('form .gd-pc-form__error')!.textContent).toBe(
      'Wrong username or password, or that account cannot be linked with a password.',
    );

    await type('password', 'right');
    await submit();
    refuse(expectPost('/link'), 409, 'linked-elsewhere');
    await stable();
    expectSession().flush(readServerResponse('argonauth-session-no-aliases.json'));
    await stable();
    expect(query('form .gd-pc-form__error')!.textContent).toBe('That account belongs to another PC Account.');

    await type('password', 'right');
    await submit();
    const request = expectPost('/link');
    expect(bodyOf(request)).toBe('login=Sam&password=right');
    request.flush(readServerResponse('argonauth-session-aliases.json'));
    await stable();
    expect(query('form')).toBeNull();
    expect(query('.gd-alias-list__empty')).toBeNull();
    expect(names()).toEqual(['Frodo', 'Underhill', 'Ringbearer']);
    expect(announced).toBe(0);
    expect(navigation.visited).toEqual([]);
  });

  it('opens the form a sign-in came back for, and closes it with Cancel', async () => {
    setUp('?open=create');
    const { query, button, stable } = await render(readServerResponse('argonauth-session-aliases.json'));
    expect(query('form')!.dataset['form']).toBe('create');
    button('cancel').click();
    await stable();
    expect(query('form')).toBeNull();
  });

  it('logs out everything', async () => {
    setUp();
    const { button, stable } = await render(readServerResponse('argonauth-session-aliases.json'));
    button('logout').click();
    await stable();
    expect(button('logout').disabled).toBe(true);
    const request = expectPost('/logout');
    expect(bodyOf(request)).toBe('scope=all');
    request.flush(readServerResponse('argonauth-logout-all.json'));
    await stable();
    expect(announced).toBe(1);
    expect(navigation.visited[0]).toMatch(/^https:\/\/auth\.lotrtcgpc\.net\/logout/);
  });

  it('shows a password login its one account, with Enter Gemp and Log Out', async () => {
    setUp('?return=%2Faccount');
    const { el, query, names, all, enterOf, button, stable } = await render(
      readServerResponse('argonauth-session-password.json'),
    );
    expect(el.dataset['kind']).toBe('password');
    expect(query('.gd-alias-list__signed-in')).toBeNull();
    expect(names()).toEqual(['Samwise']);
    expect(all('.gd-alias-list__actions .gd-button').map((b) => b.dataset['action'])).toEqual(['logout']);
    enterOf('Samwise').click();
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/account']);
    button('logout').click();
    await stable();
    expect(bodyOf(expectPost('/logout'))).toBe('scope=all');
  });

  it.each(['argonauth-session-test.json', 'argonauth-session-nobody.json'])(
    'draws nothing for anyone else (%s)',
    async (fixture) => {
      setUp();
      const { el } = await render(readServerResponse(fixture));
      expect(el.children.length).toBe(0);
      expect(el.hasAttribute('data-kind')).toBe(false);
    },
  );

  it("says a refused Enter Gemp at the list's level when the session asked for again no longer has the alias", async () => {
    setUp();
    const { query, names, enterOf, stable, button } = await render(
      readServerResponse('argonauth-session-aliases.json'),
    );
    enterOf('Ringbearer').click();
    refuse(expectPost('/enter'), 403, 'not-linked');
    await stable();
    const session = JSON.parse(readServerResponse('argonauth-session-aliases.json'));
    expectSession().flush(JSON.stringify({ ...session, aliases: session.aliases.slice(0, 2) }));
    await stable();
    expect(names()).toEqual(['Frodo', 'Underhill']);
    const refusal = query('.gd-alias-list__refusal')!;
    expect([refusal.textContent, refusal.getAttribute('role')]).toEqual([
      'That alias is no longer linked to your PC Account.',
      'alert',
    ]);
    button('link').click();
    await stable();
    expect(query('.gd-alias-list__refusal')).toBeNull();
  });
});
