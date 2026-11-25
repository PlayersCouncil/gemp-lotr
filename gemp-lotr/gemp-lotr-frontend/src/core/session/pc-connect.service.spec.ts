import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { settle } from '../../testing/fake-notifier';
import { readServerResponse } from '../../testing/files';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PageNavigation } from '../platform/page-navigation';
import { SLOW_DOWN } from './pc-account-messages';
import { PcConnect } from './pc-connect.service';
import { sentForm } from '../../testing/sent-form';

describe('PcConnect', () => {
  let backend: HttpTestingController;
  let connect: PcConnect;
  let visited: string[];

  beforeEach(() => {
    visited = [];
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PageNavigation, useValue: { go: (url: string) => visited.push(url) } },
        PcConnect,
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    connect = TestBed.inject(PcConnect);
  });

  afterEach(() => backend.verify());

  const expectConnect = (): TestRequest =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/connect' && r.method === 'POST');

  it('opens, takes the password, and cancels to an empty closed form', () => {
    expect(connect.open()).toBe(false);
    connect.start();
    connect.setPassword('secret');
    expect([connect.open(), connect.password()]).toEqual([true, 'secret']);
    connect.cancel();
    expect([connect.open(), connect.password(), connect.error()]).toEqual([false, '', null]);
  });

  it('shows a refusal and empties the password; once accepted the page leaves', async () => {
    connect.start();
    connect.setPassword('secret');
    const refused = connect.submit();
    expect(connect.busy()).toBe(true);
    await connect.submit();
    expectConnect().flush('{"error":"slow-down"}', { status: 429, statusText: 'Too Many' });
    await settle();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session')
      .flush(readServerResponse('argonauth-session-password.json'));
    await refused;
    expect([connect.error(), connect.password(), connect.busy()]).toEqual([SLOW_DOWN, '', false]);

    connect.setPassword('secret');
    const accepted = connect.submit();
    expect(connect.error()).toBeNull();
    const request = expectConnect();
    expect(sentForm(request.request).toString()).toBe('password=secret');
    request.flush(readServerResponse('argonauth-connect.json'));
    await accepted;
    expect(visited).toEqual([(JSON.parse(readServerResponse('argonauth-connect.json')) as { next: unknown }).next]);
    expect(connect.error()).toBeNull();
  });
});
