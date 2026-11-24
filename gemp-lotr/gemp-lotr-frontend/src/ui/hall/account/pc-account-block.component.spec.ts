import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
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
    return { el, query: (selector: string) => el.querySelector<HTMLElement>(selector) };
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
    const connect = query('a.gd-pc-account-block__connect')!;
    expect(connect.textContent!.trim()).toBe('Connect PC Account');
    expect(connect.getAttribute('href')).toBe('/gemp-lotr-server/argonauth/login?then=connect');
    expect(query('gd-alias-list')).toBeNull();
    expect(query('.gd-pc-account-block__connected')).toBeNull();
  });

  it('tells a password login that is linked where its other aliases are', async () => {
    const { query } = await render('argonauth-session-password-linked.json');
    expect(query('.gd-pc-account-block__connected')!.textContent).toBe(
      'This account is connected to a PC Account. Sign in with the PC Account to see all your aliases.',
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
});
