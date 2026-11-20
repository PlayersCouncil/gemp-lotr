import { LoginApi } from './login-api.service';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FakeNotifier, provideFakeNotifier, settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { Notifier } from '../notifier/notifier';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { PlayerInfo } from '../session/player-info.service';
import {
  CANNOT_CONNECT,
  CANNOT_CONTACT,
  INVALID_LOGIN,
  INVALID_NEW_LOGIN,
  LOGIN_TAKEN,
  MAINTENANCE,
  PASSWORD_RESET_PROMPT,
  PASSWORD_UPDATED,
  PASSWORDS_DIFFER,
  PERMANENTLY_BANNED,
  TEMPORARILY_BANNED,
} from './login-messages';
import { LOGIN_SERVICES } from './login-services';
import { LoginService, registrationError } from './login.service';

class FakeNavigation {
  readonly visited: string[] = [];
  go(url: string): void {
    this.visited.push(url);
  }
}

describe('registrationError', () => {
  it('asks for the same password twice, and nothing else', () => {
    expect(registrationError('secret', 'secret')).toBeNull();
    expect(registrationError('', '')).toBeNull();
    expect(registrationError('secret', 'Secret')).toBe(PASSWORDS_DIFFER);
    expect(registrationError('secret', '')).toBe(PASSWORDS_DIFFER);
  });
});

describe('LoginService', () => {
  let backend: HttpTestingController;
  let service: LoginService;
  let navigation: FakeNavigation;
  let notifier: FakeNotifier;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        LoginApi,
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
        { provide: PageNavigation, useClass: FakeNavigation },
        ...LOGIN_SERVICES,
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    service = TestBed.inject(LoginService);
    navigation = TestBed.inject(PageNavigation) as unknown as FakeNavigation;
    notifier = TestBed.inject(Notifier) as FakeNotifier;
  }

  afterEach(() => backend.verify());

  const expectPost = (path: string): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server' + path && r.method === 'POST');
  const fail = (request: TestRequest, status: number) => request.flush('', { status, statusText: 'Error' });

  function fillLogin(login = 'frodo', password = 'ring') {
    service.setLogin(login);
    service.setPassword(password);
  }

  /** Answers a successful login/register call, then the player refresh that follows it. */
  async function succeed(request: TestRequest, done: Promise<void>) {
    request.flush(readServerResponse('login-ok.xml'));
    await settle();
    expect(navigation.visited).toEqual([]);
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('{"name":"frodo","type":"u"}');
    await done;
  }

  it('starts on an empty login form', () => {
    setUp();
    expect(service.mode()).toBe('login');
    expect(service.submitLabel()).toBe('Login');
    expect(service.formShown()).toBe(true);
    expect(service.error()).toBeNull();
    expect(service.busy()).toBe(false);
    expect(service.redirectTarget()).toBeNull();
  });

  it('loads the status line, and alerts like the legacy page when it cannot', async () => {
    setUp();
    const status = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/status' && r.method === 'GET');
    let loaded = service.loadStatus();
    status().flush(readServerResponse('server-status.json'));
    await loaded;
    expect(service.serverStatus()).toBe(readServerResponse('server-status.txt'));
    loaded = service.loadStatus();
    fail(status(), 500);
    await loaded;
    expect(notifier.dialogs).toEqual([`alert: ${CANNOT_CONTACT}`]);
  });

  it('logs in, refreshes the player and goes to the hall', async () => {
    setUp();
    const players = TestBed.inject(PlayerInfo);
    const before = players.whoAmI();
    fail(
      backend.expectOne((r) => r.url === '/gemp-lotr-server/player'),
      401,
    );
    await before;
    expect(players.state().status).toBe('anonymous');

    fillLogin();
    const done = service.submit();
    expect(service.busy()).toBe(true);
    const request = expectPost('/login');
    expect(request.request.body.toString()).toBe('login=frodo&password=ring');
    await succeed(request, done);
    expect(players.player()?.name).toBe('frodo');
    expect(service.redirectTarget()).toBe('/gemp-lotr/ng/');
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
    expect(service.busy()).toBe(false);
  });

  it('goes back to the place the visitor came from', async () => {
    setUp('?return=%2Fevents%2Fleagues');
    fillLogin();
    const done = service.logIn();
    await succeed(expectPost('/login'), done);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/events/leagues']);
  });

  it('still goes to the hall when the player refresh fails', async () => {
    setUp();
    fillLogin();
    const done = service.logIn();
    expectPost('/login').flush(readServerResponse('login-ok.xml'));
    await settle();
    fail(
      backend.expectOne((r) => r.url === '/gemp-lotr-server/player'),
      500,
    );
    await done;
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
    expect(notifier.dialogs).toEqual([]);
  });

  it('ignores a second submit while busy', async () => {
    setUp();
    fillLogin();
    const first = service.logIn();
    await service.logIn();
    await service.submit();
    const request = expectPost('/login');
    fail(request, 401);
    await first;
    expect(service.busy()).toBe(false);
  });

  it('shows the login form again, empty, after a wrong password (401)', async () => {
    setUp();
    fillLogin();
    const done = service.logIn();
    fail(expectPost('/login'), 401);
    await done;
    expect(service.error()).toBe(INVALID_LOGIN);
    expect(service.mode()).toBe('login');
    expect([service.login(), service.password()]).toEqual(['', '']);
    expect(service.formShown()).toBe(true);
    expect(navigation.visited).toEqual([]);
  });

  it.each([
    [403, PERMANENTLY_BANNED],
    [409, TEMPORARILY_BANNED],
  ])('hides the form when banned (%i)', async (status, message) => {
    setUp();
    fillLogin();
    const done = service.logIn();
    fail(expectPost('/login'), status);
    await done;
    expect(service.error()).toBe(message);
    expect(service.formShown()).toBe(false);
  });

  it('says the server is down for maintenance (503), keeping the form', async () => {
    setUp();
    fillLogin();
    const done = service.logIn();
    fail(expectPost('/login'), 503);
    await done;
    expect(service.error()).toBe(MAINTENANCE);
    expect(service.login()).toBe('frodo');
    expect(service.formShown()).toBe(true);
  });

  it('alerts when there is no connection (0) or another failure', async () => {
    setUp();
    fillLogin();
    let done = service.logIn();
    expectPost('/login').error(new ProgressEvent('error'), { status: 0, statusText: '' });
    await done;
    done = service.logIn();
    fail(expectPost('/login'), 500);
    await done;
    expect(notifier.dialogs).toEqual([`alert: ${CANNOT_CONNECT}`, `alert: ${CANNOT_CONTACT}`]);
    expect(service.error()).toBeNull();
    expect(service.busy()).toBe(false);
  });

  it('asks for a new password after a reset (202), then sets it through register', async () => {
    setUp();
    fillLogin('frodo', '');
    const done = service.logIn();
    expectPost('/login').flush('', { status: 202, statusText: 'Accepted' });
    await done;
    expect(service.mode()).toBe('register');
    expect(service.passwordReset()).toBe(true);
    expect(service.submitLabel()).toBe('Update Password');
    expect(service.login()).toBe('frodo');
    expect(service.password()).toBe('');
    expect(service.error()).toBe(PASSWORD_RESET_PROMPT);
    expect(navigation.visited).toEqual([]);

    service.setPassword('new');
    service.setPasswordRepeated('new');
    const registered = service.submit();
    const request = expectPost('/register');
    expect(request.request.body.toString()).toBe('login=frodo&password=new');
    await succeed(request, registered);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('switches to an empty register form, clearing the old message, and back', async () => {
    setUp();
    fillLogin();
    const done = service.logIn();
    fail(expectPost('/login'), 503);
    await done;
    service.setLogin('frodo');
    service.showRegister();
    expect(service.mode()).toBe('register');
    expect(service.submitLabel()).toBe('Register');
    expect(service.error()).toBeNull();
    expect(service.login()).toBe('');
    service.setLogin('sam');
    service.showLogin();
    expect(service.mode()).toBe('login');
    expect(service.login()).toBe('');
  });

  it('registers and goes to the hall', async () => {
    setUp();
    service.showRegister();
    service.setLogin('new+user');
    service.setPassword('pw&1');
    service.setPasswordRepeated('pw&1');
    const done = service.submit();
    const request = expectPost('/register');
    expect(request.request.body.toString()).toBe('login=new%2Buser&password=pw%261');
    await succeed(request, done);
    expect(navigation.visited).toEqual(['/gemp-lotr/ng/']);
  });

  it('sends nothing when the passwords differ', async () => {
    setUp();
    service.showRegister();
    service.setLogin('sam');
    service.setPassword('a');
    service.setPasswordRepeated('b');
    await service.submit();
    expect(service.error()).toBe(PASSWORDS_DIFFER);
    expect(service.busy()).toBe(false);
  });

  it.each([
    [400, INVALID_NEW_LOGIN],
    [409, LOGIN_TAKEN],
    [503, MAINTENANCE],
  ])('maps a failed registration (%i) to its message, keeping the form', async (status, message) => {
    setUp();
    service.showRegister();
    service.setLogin('x');
    const done = service.register();
    fail(expectPost('/register'), status);
    await done;
    expect(service.error()).toBe(message);
    expect(service.login()).toBe('x');
    expect(service.formShown()).toBe(true);
    expect(navigation.visited).toEqual([]);
  });

  it('says the password was updated when registering answers 202', async () => {
    setUp();
    service.showRegister();
    const done = service.register();
    expectPost('/register').flush('', { status: 202, statusText: 'Accepted' });
    await done;
    expect(service.error()).toBe(PASSWORD_UPDATED);
    expect(navigation.visited).toEqual([]);
  });
});
