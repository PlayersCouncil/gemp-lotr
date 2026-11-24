import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { PageNavigation } from '../../../core/platform/page-navigation';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { PcAccountBlockComponent } from './pc-account-block.component';

describe('PcAccountBlockComponent', () => {
  let backend: HttpTestingController;

  async function render(fixture: string, queryString = '') {
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: queryString }],
    });
    backend = TestBed.inject(HttpTestingController);
    const component = TestBed.createComponent(PcAccountBlockComponent);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET')
      .flush(readServerResponse(fixture));
    await settle();
    await component.whenStable();
    const el = component.nativeElement as HTMLElement;
    const stable = async () => {
      await settle();
      await component.whenStable();
    };
    return { el, stable, query: (selector: string) => el.querySelector<HTMLElement>(selector) };
  }

  afterEach(() => backend.verify());

  it('shows nothing in test mode', async () => {
    const { el } = await render('argonauth-session-test.json', '?pc=connected');
    expect(el.children.length).toBe(0);
    expect(el.hasAttribute('data-kind')).toBe(false);
  });

  it('gives a signed-in PC account the alias list and Manage PC Account, in a new tab', async () => {
    const { el, query } = await render('argonauth-session.json');
    expect(el.dataset['kind']).toBe('pc');
    const section = query('section')!;
    expect(section.getAttribute('aria-labelledby')).toBe(query('h2')!.id);
    expect(query('h2')!.textContent).toBe('PC Account');
    expect(query('gd-alias-list')!.dataset['place']).toBe('account');
    expect(query('.gd-alias-list__signed-in')!.textContent).toBe('Signed in as Frodo Baggins');
    const manage = query('a.gd-pc-account-block__manage') as HTMLAnchorElement;
    expect([manage.textContent!.trim(), manage.href, manage.target, manage.rel]).toEqual([
      'Manage PC Account',
      'https://auth.lotrtcgpc.net/',
      '_blank',
      'noopener',
    ]);
    expect(query('.gd-pc-account-block__connect')).toBeNull();
  });

  it('offers a password login that is not linked Connect PC Account, saying what it is', async () => {
    const { el, query } = await render('argonauth-session-password.json');
    expect(el.dataset['kind']).toBe('password');
    expect(query('.gd-pc-account-block__about')!.textContent).toContain('A PC Account is one sign-in');
    const connect = query('button.gd-pc-account-block__connect')!;
    expect(connect.textContent!.trim()).toBe('Connect PC Account');
    expect(connect.classList).toContain('gd-button');
    expect(query('gd-pc-form')).toBeNull();
    expect(query('gd-alias-list')).toBeNull();
    expect(query('.gd-pc-account-block__connected')).toBeNull();
  });

  it("connects with the account's password: refused, it says why; accepted, the page leaves for the sign-in", async () => {
    const { query, stable } = await render('argonauth-session-password.json');
    const visited: string[] = [];
    vi.spyOn(TestBed.inject(PageNavigation), 'go').mockImplementation((url) => void visited.push(url));
    (query('button.gd-pc-account-block__connect') as HTMLButtonElement).click();
    await stable();
    const form = query('gd-pc-form form')!;
    expect([form.dataset['form'], form.getAttribute('aria-label')]).toEqual(['connect', 'Connect PC Account']);
    const field = query('input[name="password"]') as HTMLInputElement;
    expect([field.placeholder, field.getAttribute('aria-label')]).toEqual(['Password', 'Password']);
    expect(query('.gd-pc-form__label')).toBeNull();
    expect(query('.gd-pc-form__note')!.textContent).toBe(
      'Once connected, this account is entered through your PC Account; its password no longer logs in.',
    );
    expect(query('[data-action="submit"]')!.textContent!.trim()).toBe('Connect');
    expect(query('button.gd-pc-account-block__connect')).toBeNull();
    const type = async (value: string) => {
      const input = query('input[name="password"]') as HTMLInputElement;
      expect(input.type).toBe('password');
      input.value = value;
      input.dispatchEvent(new Event('input'));
      (query('gd-pc-form form') as HTMLFormElement).requestSubmit();
      await stable();
    };

    await type('wrong');
    const refused = backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/connect' && r.method === 'POST');
    expect(refused.request.body.toString()).toBe('password=wrong');
    refused.flush('{"error":"bad-credentials"}', { status: 401, statusText: 'Unauthorized' });
    await stable();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session')
      .flush(readServerResponse('argonauth-session-password.json'));
    await stable();
    expect(query('.gd-pc-form__error')!.textContent).toBe('Wrong password.');
    expect((query('input[name="password"]') as HTMLInputElement).value).toBe('');

    await type('right');
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/connect')
      .flush(readServerResponse('argonauth-connect.json'));
    await stable();
    expect(visited).toEqual([JSON.parse(readServerResponse('argonauth-connect.json')).next]);
  });

  it('closes the Connect form with Cancel', async () => {
    const { query, stable } = await render('argonauth-session-password.json');
    (query('button.gd-pc-account-block__connect') as HTMLButtonElement).click();
    await stable();
    (query('[data-action="cancel"]') as HTMLButtonElement).click();
    await stable();
    expect(query('gd-pc-form')).toBeNull();
    expect(query('button.gd-pc-account-block__connect')).not.toBeNull();
  });

  it('tells a password login that is linked where its other aliases are', async () => {
    const { query } = await render('argonauth-session-password-linked.json');
    expect(query('.gd-pc-account-block__connected')!.textContent).toBe(
      'This account is connected to a PC Account. Sign in with the PC Account to see all your Gemp profiles.',
    );
    expect(query('.gd-pc-account-block__connect')).toBeNull();
  });

  it.each([
    ['?pc=connected', 'argonauth-session-password-linked.json', 'This account is now connected to your PC Account.'],
    ['?pc=linked-elsewhere', 'argonauth-session-password.json', 'This account already belongs to another PC Account.'],
    ['?pc=failed', 'argonauth-session-password.json', 'PC Account sign-in failed. Try again.'],
    ['?pc=cancelled', 'argonauth-session-password.json', 'PC Account sign-in was cancelled.'],
  ])('says how a Connect ended (%s)', async (queryString, fixture, line) => {
    const { query } = await render(fixture, queryString);
    const notice = query('.gd-pc-account-block__notice')!;
    expect(notice.textContent).toBe(line);
    expect(notice.getAttribute('role')).toBe('status');
  });

  it("says Connect's not-allowed on top when the session asked for again is a PC account's", async () => {
    const { query, stable } = await render('argonauth-session-password.json');
    (query('button.gd-pc-account-block__connect') as HTMLButtonElement).click();
    await stable();
    (query('gd-pc-form form') as HTMLFormElement).requestSubmit();
    await stable();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/connect')
      .flush('{"error":"not-allowed"}', { status: 403, statusText: 'Forbidden' });
    await stable();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session')
      .flush(readServerResponse('argonauth-session-aliases.json'));
    await stable();
    expect(query('gd-pc-form')).toBeNull();
    expect(query('gd-alias-list')).not.toBeNull();
    const refusal = query('.gd-pc-account-block__refusal')!;
    expect([refusal.textContent, refusal.getAttribute('role')]).toEqual([
      'Only an account logged in with its password can be connected.',
      'alert',
    ]);
  });
});
