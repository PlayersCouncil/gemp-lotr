import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CANNOT_CONTACT, FORGOT_PASSWORD, PASSWORDS_DIFFER, messageText } from '../../core/login/login-messages';
import { INVALID_NEW_LOGIN, PERMANENTLY_BANNED, TOO_MANY_ATTEMPTS } from '../../core/login/login-messages';
import { LOGIN_SERVICES } from '../../core/login/login-services';
import { Notifier } from '../../core/notifier/notifier';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { PageNavigation } from '../../core/platform/page-navigation';
import { FakeNotifier, provideFakeNotifier, settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { LoginPageComponent } from './login-page.component';

class FakeNavigation {
  readonly visited: string[] = [];
  go(url: string): void {
    this.visited.push(url);
  }
}

describe('LoginPageComponent', () => {
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: PageNavigation, useClass: FakeNavigation },
        ...LOGIN_SERVICES,
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const statusRequest = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/status' && r.method === 'GET');
  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server' + path && r.method === 'POST');
  const fail = (request: TestRequest, status: number) => request.flush('', { status, statusText: 'Error' });
  const bodyOf = (request: TestRequest) => (request.request.body as HttpParams).toString();

  const sessionRequest = () =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET');

  /** The page, its status line answered (or failed, with `status`), and its session (`test` mode unless given). */
  async function render(status?: number, session = 'argonauth-session-test.json') {
    const fixture = TestBed.createComponent(LoginPageComponent);
    if (status) {
      fail(statusRequest(), status);
    } else {
      statusRequest().flush(readServerResponse('server-status.json'));
    }
    sessionRequest().flush(readServerResponse(session));
    await settle();
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const query = <T extends Element = HTMLElement>(selector: string) => el.querySelector<T>(selector);
    const input = (name: string) => query<HTMLInputElement>(`.gd-login__input[name="${name}"]`);
    const button = (action: string) => query<HTMLButtonElement>(`.gd-button[data-action="${action}"]`);
    const type = async (name: string, value: string) => {
      const field = input(name)!;
      field.value = value;
      field.dispatchEvent(new Event('input'));
      await fixture.whenStable();
    };
    const stable = async () => {
      await settle();
      await fixture.whenStable();
    };
    return { fixture, el, query, input, button, type, stable };
  }

  it('lays out the banner, the panel and the information (DOM contract)', async () => {
    const { el, query } = await render();
    expect(el.classList).toContain('gd-login');
    expect(el.dataset['mode']).toBe('login');
    expect(Array.from(el.children).map((child) => child.localName + '.' + child.className)).toEqual([
      'div.gd-login__brand',
      'h1.gd-login__banner',
      'div.gd-login__content',
    ]);
    expect(query('.gd-login__brand')!.getAttribute('aria-hidden')).toBe('true');
    expect(query('h1.gd-login__banner')!.textContent).toBe('GEMP: Play the Lord of the Rings TCG');
    const content = query('.gd-login__content')!;
    expect(Array.from(content.children).map((child) => child.localName + '.' + child.className)).toEqual([
      'section.gd-login__panel',
      'section.gd-login__info',
    ]);
    const panel = query('.gd-login__panel')!;
    expect(Array.from(panel.children).map((child) => child.localName + '.' + child.className)).toEqual([
      'p.gd-login__status',
      'div.gd-login__error',
      'form.gd-login__form',
    ]);
    expect(query('.gd-login__status')!.textContent).toBe(readServerResponse('server-status.txt'));
    expect(query('.gd-login__error')!.getAttribute('role')).toBe('alert');
    expect(query('.gd-login__error')!.textContent!.trim()).toBe('');
  });

  it('draws the login form: fields with labels and autocomplete, Login, the register button and the help link', async () => {
    const { query, input, button } = await render();
    const form = query<HTMLFormElement>('form.gd-login__form')!;
    expect(form.dataset['mode']).toBe('login');
    expect(form.hasAttribute('data-busy')).toBe(false);
    expect(form.getAttribute('aria-busy')).toBe('false');
    expect(query('.gd-login__intro')!.textContent!.replace(/\s+/g, ' ').trim()).toBe('Login below, or Register');
    expect(button('register')!.type).toBe('button');
    const fields = Array.from(form.querySelectorAll<HTMLLabelElement>('label.gd-login__field'));
    expect(fields.map((f) => [f.dataset['field'], f.querySelector('.gd-login__label')!.textContent])).toEqual([
      ['login', 'Login: '],
      ['password', 'Password: '],
    ]);
    expect([input('login')!.type, input('login')!.autocomplete]).toEqual(['text', 'username']);
    expect([input('password')!.type, input('password')!.autocomplete]).toEqual(['password', 'current-password']);
    expect(input('password-repeated')).toBeNull();
    const submit = button('submit')!;
    expect([submit.type, submit.textContent!.trim(), submit.disabled]).toEqual(['submit', 'Login', false]);
    expect(submit.closest('.gd-login__actions')).not.toBeNull();
    expect(button('login')).toBeNull();
    const forgot = query<HTMLAnchorElement>('.gd-login__forgot > a')!;
    expect(forgot.href).toBe(FORGOT_PASSWORD.url);
    expect(forgot.textContent).toBe(FORGOT_PASSWORD.text);
    expect(forgot.querySelector('.gd-login__contact')!.textContent).toBe('ketura');
  });

  it('shows the information about the game and the shout-out, as the legacy page', async () => {
    const { query } = await render();
    const about = query('.gd-login__info > p.gd-login__about')!;
    expect(about.textContent!.replace(/\s+/g, ' ').trim()).toBe(
      'The Lord of the Rings TCG was published by Decipher, Inc from 2001-2007.GEMP is a platform for playing ' +
        "this excellent game for free in your browser.Maintained by the unofficial Player's Council.",
    );
    expect(Array.from(about.querySelectorAll('a')).map((a) => a.href)).toEqual([
      'https://wiki.lotrtcgpc.net/wiki/The_Lord_of_the_Rings_TCG',
      'https://lotrtcgpc.net/',
    ]);
    const shoutout = query('.gd-login__info > .gd-login__shoutout')!;
    expect(Array.from(shoutout.children).map((child) => child.className)).toEqual([
      'gd-login__force',
      'gd-login__shoutout-text',
      'gd-login__force',
    ]);
    expect(Array.from(shoutout.querySelectorAll('a')).map((a) => a.href)).toEqual([
      'https://gemp.starwarsccg.org/gemp-swccg/',
      'https://www.starwarsccg.org/',
    ]);
  });

  it('logs in with what was typed when the form is submitted', async () => {
    const { query, type, button, stable } = await render();
    await type('login', 'frodo');
    await type('password', 'ring');
    button('submit')!.click();
    const request = expectPost('/login');
    expect(bodyOf(request)).toBe('login=frodo&password=ring');
    request.flush(readServerResponse('login-ok.xml'));
    await stable();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('{"name":"frodo","type":"u"}');
    await stable();
    expect((TestBed.inject(PageNavigation) as unknown as FakeNavigation).visited).toEqual(['/gemp-lotr/ng/']);
    expect(query('.gd-login__error')!.textContent!.trim()).toBe('');
  });

  it('submits on Enter in the password field: the fields are in one form with one submit button', async () => {
    const { query, input, type } = await render();
    const form = query<HTMLFormElement>('form.gd-login__form')!;
    expect(input('password')!.form).toBe(form);
    expect(form.querySelectorAll('[type=submit]')).toHaveLength(1);
    await type('login', 'frodo');
    await type('password', 'ring');
    // What the browser does on Enter in a field (implicit submission): submit with the form's submit button
    form.requestSubmit(query<HTMLButtonElement>('[type=submit]')!);
    fail(expectPost('/login'), 401);
  });

  it('shows the message of a failed attempt, and the login form again, empty', async () => {
    const { query, input, type, button, stable } = await render();
    await type('login', 'frodo');
    await type('password', 'wrong');
    button('submit')!.click();
    fail(expectPost('/login'), 401);
    await stable();
    expect(query('.gd-login__error')!.textContent!.trim()).toBe('Invalid username or password. Try again.');
    expect([input('login')!.value, input('password')!.value]).toEqual(['', '']);
  });

  it('draws line breaks and links of a message', async () => {
    const { query, input, type, button, stable } = await render();
    button('register')!.click();
    await stable();
    await type('login', 'x');
    button('submit')!.click();
    fail(expectPost('/register'), 400);
    await stable();
    const error = query('.gd-login__error')!;
    expect(error.querySelectorAll('br')).toHaveLength(1);
    expect(error.textContent!.replace(/\s+/g, ' ').trim()).toBe(
      messageText(INVALID_NEW_LOGIN).replace(/\s+/g, ' ').trim(),
    );
    expect(input('login')!.value).toBe('x');

    // A ban hides the form and links to the Discord
    button('login')!.click();
    await stable();
    await type('login', 'banned');
    button('submit')!.click();
    fail(expectPost('/login'), 403);
    await stable();
    expect(query('.gd-login__form')).toBeNull();
    expect(error.textContent!.trim()).toBe(messageText(PERMANENTLY_BANNED));
    const link = error.querySelector<HTMLAnchorElement>('a')!;
    expect([link.textContent, link.href]).toEqual(['the PC Discord', 'https://lotrtcgpc.net/discord']);
  });

  it('switches to the register form and back', async () => {
    const { el, query, input, button, stable } = await render();
    button('register')!.click();
    await stable();
    expect(el.dataset['mode']).toBe('register');
    expect(query<HTMLFormElement>('.gd-login__form')!.dataset['mode']).toBe('register');
    expect(query('.gd-login__intro')).toBeNull();
    expect(query('.gd-login__forgot')).toBeNull();
    expect(
      Array.from(el.querySelectorAll<HTMLElement>('label.gd-login__field')).map((f) => f.dataset['field']),
    ).toEqual(['login', 'password', 'password-repeated']);
    expect(input('password')!.autocomplete).toBe('new-password');
    expect([input('password-repeated')!.type, input('password-repeated')!.autocomplete]).toEqual([
      'password',
      'new-password',
    ]);
    expect(button('submit')!.textContent!.trim()).toBe('Register');
    expect(button('login')!.type).toBe('button');

    button('login')!.click();
    await stable();
    expect(el.dataset['mode']).toBe('login');
    expect(input('password-repeated')).toBeNull();
    expect(button('submit')!.textContent!.trim()).toBe('Login');
  });

  it('registers with what was typed, after checking the passwords match', async () => {
    const { query, type, button, stable } = await render();
    button('register')!.click();
    await stable();
    await type('login', 'newbie');
    await type('password', 'secret');
    await type('password-repeated', 'Secret');
    button('submit')!.click();
    await stable();
    expect(query('.gd-login__error')!.textContent!.trim()).toBe(messageText(PASSWORDS_DIFFER));
    await type('password-repeated', 'secret');
    button('submit')!.click();
    const request = expectPost('/register');
    expect(bodyOf(request)).toBe('login=newbie&password=secret');
    fail(request, 409);
    await stable();
    expect(query('.gd-login__error')!.textContent!.trim()).toBe(
      'User with this login already exists in the system. Try a different one.',
    );
  });

  it('has no password reset: a 202 leaves for the hall like any login', async () => {
    const { type, button, stable } = await render();
    await type('login', 'reset');
    button('submit')!.click();
    expectPost('/login').flush('', { status: 202, statusText: 'Accepted' });
    await stable();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('{"name":"reset","type":"u"}');
    await stable();
    expect((TestBed.inject(PageNavigation) as unknown as FakeNavigation).visited).toEqual(['/gemp-lotr/ng/']);
    expect(button('submit')!.textContent!.trim()).toBe('Login');
  });

  it('says a claimed account is entered through its PC Account (423), where the other refusals go', async () => {
    const { query, type, button, stable } = await render();
    await type('login', 'frodo');
    button('submit')!.click();
    fail(expectPost('/login'), 423);
    await stable();
    expect(query('.gd-login__error')!.textContent!.trim()).toBe(
      'That account is linked to a PC Account, please use that to log in.',
    );
    expect(query('.gd-login__form')).not.toBeNull();
  });

  it('says to wait after too many attempts (429)', async () => {
    const { query, type, button, stable } = await render();
    await type('login', 'frodo');
    button('submit')!.click();
    fail(expectPost('/login'), 429);
    await stable();
    expect(query('.gd-login__error')!.textContent!.trim()).toBe(messageText(TOO_MANY_ATTEMPTS));
  });

  it('disables the buttons and marks the form busy while a call is under way', async () => {
    const { query, type, button, stable } = await render();
    await type('login', 'frodo');
    button('submit')!.click();
    await stable();
    const form = query<HTMLFormElement>('.gd-login__form')!;
    expect(form.hasAttribute('data-busy')).toBe(true);
    expect(form.getAttribute('aria-busy')).toBe('true');
    expect([button('submit')!.disabled, button('register')!.disabled]).toEqual([true, true]);
    // A second submit (Enter again) sends nothing
    form.requestSubmit();
    const request = expectPost('/login');
    fail(request, 401);
    await stable();
    expect(form.hasAttribute('data-busy')).toBe(false);
    expect([button('submit')!.disabled, button('register')!.disabled]).toEqual([false, false]);
  });

  it('tells the Notifier when the server cannot be reached for the status line', async () => {
    const { query } = await render(500);
    expect((TestBed.inject(Notifier) as FakeNotifier).dialogs).toEqual([`alert: ${CANNOT_CONTACT}`]);
    expect(query('.gd-login__status')!.textContent).toBe('');
  });
});

describe('LoginPageComponent and the PC account', () => {
  let backend: HttpTestingController;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
        { provide: PageNavigation, useClass: FakeNavigation },
        ...LOGIN_SERVICES,
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  }

  afterEach(() => backend.verify());

  /** The page with its status line, and the session answered with `session` (a fixture), or failed with a status. */
  async function render(session: string | number | null) {
    const fixture = TestBed.createComponent(LoginPageComponent);
    const status = backend.match((r) => r.url === '/gemp-lotr-server/status');
    status.forEach((request) => request.flush(readServerResponse('server-status.json')));
    const sessions = backend.match((r) => r.url === '/gemp-lotr-server/argonauth/session');
    expect(sessions.length).toBe(1);
    if (typeof session === 'string') sessions[0].flush(readServerResponse(session));
    else if (typeof session === 'number') sessions[0].flush('', { status: session, statusText: 'Error' });
    // null: left unanswered, and done with (verify() then finds nothing open)
    await settle();
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    return { fixture, el, query: (selector: string) => el.querySelector<HTMLElement>(selector) };
  }

  it('draws neither the form nor the list until the session is known', async () => {
    setUp();
    const { el, query } = await render(null);
    expect(query('.gd-login__form')).toBeNull();
    expect(query('gd-alias-list')).toBeNull();
    expect(el.hasAttribute('data-pc')).toBe(false);
    // (the session's request, left unanswered)
  });

  it('is the page as it always was in test mode, or when the server gives no session', async () => {
    setUp('?pc=failed');
    const { el, query } = await render(404);
    expect(el.dataset['pc']).toBe('test');
    expect(query('.gd-button[data-action="register"]')).not.toBeNull();
    expect(query('[data-action="pc-login"]')).toBeNull();
    expect(query('.gd-login__notice')).toBeNull();
    expect(query('gd-alias-list')).toBeNull();
  });

  it('lays out Legacy Login and PC SSO side by side, in optional mode, for nobody', async () => {
    setUp();
    const { query, el } = await render('argonauth-session-nobody.json');
    const columns = query('.gd-login__columns')!;
    expect(Array.from(columns.children).map((child) => `${child.localName}.${child.className}`)).toEqual([
      'section.gd-login__column',
      'div.gd-login__rule',
      'section.gd-login__column',
    ]);
    const [legacy, sso] = Array.from(columns.querySelectorAll<HTMLElement>('section.gd-login__column'));
    expect([legacy.dataset['column'], sso.dataset['column']]).toEqual(['legacy', 'sso']);
    expect(legacy.querySelector('h2.gd-login__heading')!.textContent).toBe('Legacy Login');
    expect(legacy.getAttribute('aria-labelledby')).toBe(legacy.querySelector('h2')!.id);
    expect(sso.querySelector('h2.gd-login__heading')!.textContent).toBe('PC SSO');
    expect(sso.getAttribute('aria-labelledby')).toBe(sso.querySelector('h2')!.id);
    // Legacy Login: the fields, Login and the forgotten-password line; no Register, no line for new players
    const form = legacy.querySelector('form.gd-login__form')!;
    expect(form.querySelector('.gd-login__input[name="login"]')).not.toBeNull();
    expect(form.querySelector('.gd-login__input[name="password"]')).not.toBeNull();
    expect(form.querySelector('[data-action="submit"]')!.textContent!.trim()).toBe('Login');
    expect(form.querySelector('.gd-login__forgot')).not.toBeNull();
    expect(query('.gd-login__intro')).toBeNull();
    expect(query('[data-action="register"]')).toBeNull();
    expect(el.textContent).not.toContain('New players');
    // PC SSO: two links, stacked
    const links = Array.from(sso.querySelectorAll<HTMLAnchorElement>('.gd-login__sso > a.gd-button'));
    expect(links.map((link) => [link.dataset['action'], link.textContent!.trim(), link.getAttribute('href')])).toEqual([
      ['pc-login', 'Sign in with PC Account', '/gemp-lotr-server/argonauth/login'],
      ['pc-register', 'Register PC Account', '/gemp-lotr-server/argonauth/login?register=1'],
    ]);
  });

  it("carries the page's return place into both PC links", async () => {
    setUp('?return=%2Fevents%2Fleagues');
    const { query } = await render('argonauth-session-nobody.json');
    expect(query('[data-action="pc-login"]')!.getAttribute('href')).toBe(
      '/gemp-lotr-server/argonauth/login?return=%2Fevents%2Fleagues',
    );
    expect(query('[data-action="pc-register"]')!.getAttribute('href')).toBe(
      '/gemp-lotr-server/argonauth/login?register=1&return=%2Fevents%2Fleagues',
    );
  });

  it('draws no columns in test mode', async () => {
    setUp();
    const { query } = await render('argonauth-session-test.json');
    expect(query('.gd-login__columns')).toBeNull();
    expect(query('.gd-login__panel > form.gd-login__form .gd-login__intro')).not.toBeNull();
  });

  it.each([
    ['?pc=cancelled', 'PC Account sign-in was cancelled.'],
    ['?pc=failed', 'PC Account sign-in failed. Try again.'],
  ])('says how a PC sign-in ended (%s)', async (queryString, line) => {
    setUp(queryString);
    const { query } = await render('argonauth-session-nobody.json');
    expect(query('.gd-login__notice')!.textContent!.trim()).toBe(line);
    expect(query('.gd-login__notice')!.getAttribute('role')).toBe('status');
  });

  it('shows the alias list instead of the form for a signed-in PC account', async () => {
    setUp();
    const { el, query } = await render('argonauth-session-aliases.json');
    // The list shares the page's session: asked once
    expect(backend.match((r) => r.url === '/gemp-lotr-server/argonauth/session')).toEqual([]);
    expect(el.dataset['pc']).toBe('pc');
    expect(query('.gd-login__form')).toBeNull();
    const list = query('gd-alias-list')!;
    expect(list.dataset['place']).toBe('login');
    expect(list.querySelector('.gd-alias-list__signed-in')!.textContent).toBe('Signed in as Frodo Baggins');
    expect(Array.from(list.querySelectorAll('.gd-alias-list__name')).map((name) => name.textContent)).toEqual([
      'Frodo',
      'Underhill',
      'Ringbearer',
    ]);
  });

  it('shows a password login its one account', async () => {
    setUp();
    const { query } = await render('argonauth-session-password.json');
    const list = query('gd-alias-list')!;
    expect(list.dataset['kind']).toBe('password');
    expect(Array.from(list.querySelectorAll('.gd-alias-list__name')).map((name) => name.textContent)).toEqual([
      'Samwise',
    ]);
    expect(query('.gd-login__form')).toBeNull();
  });

  it('opens the form a PC sign-in came back for', async () => {
    setUp('?open=link');
    const { query } = await render('argonauth-session-aliases.json');
    expect(query('.gd-pc-form__form')!.dataset['form']).toBe('link');
  });

  it('says a refusal above the form when the session asked for again takes the alias list away', async () => {
    setUp();
    const { fixture, query } = await render('argonauth-session-aliases.json');
    const stable = async () => {
      await settle();
      await fixture.whenStable();
    };
    query('.gd-alias-list__actions [data-action="create"]')!.click();
    await stable();
    const name = query('gd-pc-form input[name="name"]') as HTMLInputElement;
    name.value = 'Merry';
    name.dispatchEvent(new Event('input'));
    (query('gd-pc-form form') as HTMLFormElement).requestSubmit();
    await stable();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/create')
      .flush('{"error":"no-pc-account"}', { status: 401, statusText: 'Unauthorized' });
    await stable();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session')
      .flush(readServerResponse('argonauth-session-nobody.json'));
    await stable();
    expect(query('gd-alias-list *')).toBeNull();
    expect(query('.gd-login__form')).not.toBeNull();
    const refusal = query('.gd-login__refusal')!;
    expect([refusal.textContent, refusal.getAttribute('role')]).toEqual([
      'Your PC Account sign-in has ended. Sign in again.',
      'alert',
    ]);
  });
});
