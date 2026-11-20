import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CANNOT_CONTACT, FORGOT_PASSWORD, PASSWORDS_DIFFER, messageText } from '../../core/login/login-messages';
import { INVALID_NEW_LOGIN, PERMANENTLY_BANNED } from '../../core/login/login-messages';
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

  const statusRequest = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/' && r.method === 'GET');
  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server' + path && r.method === 'POST');
  const fail = (request: TestRequest, status: number) => request.flush('', { status, statusText: 'Error' });
  const bodyOf = (request: TestRequest) => (request.request.body as HttpParams).toString();

  /** The page, its status line answered (or failed, with `status`). */
  async function render(status?: number) {
    const fixture = TestBed.createComponent(LoginPageComponent);
    if (status) {
      fail(statusRequest(), status);
    } else {
      statusRequest().flush(readServerResponse('server-status.txt'));
    }
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

  it('labels the button "Update Password" after a password reset', async () => {
    const { input, type, button, stable } = await render();
    await type('login', 'reset');
    button('submit')!.click();
    expectPost('/login').flush('', { status: 202, statusText: 'Accepted' });
    await stable();
    expect(button('submit')!.textContent!.trim()).toBe('Update Password');
    expect(input('login')!.value).toBe('reset');
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
