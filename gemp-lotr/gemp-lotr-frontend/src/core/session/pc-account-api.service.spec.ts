import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { readServerResponse } from '../../testing/files';
import { ApiError } from '../api/api-error';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PcAccountApi } from './pc-account-api.service';

describe('PcAccountApi', () => {
  let api: PcAccountApi;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        // An admin's ?participantId= names nobody here: the player is the session cookie's
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=bob' },
      ],
    });
    api = TestBed.inject(PcAccountApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const expectPost = (path: string) =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth' + path && r.method === 'POST');
  const body = (request: { request: { body: unknown } }) => (request.request.body as HttpParams).toString();

  it('asks for the session, not cached and without a participant', async () => {
    const result = firstValueFrom(api.session());
    const request = backend.expectOne((r) => r.url === '/gemp-lotr-server/argonauth/session' && r.method === 'GET');
    expect(request.request.params.has('participantId')).toBe(false);
    expect(request.request.params.has('_')).toBe(true);
    request.flush(readServerResponse('argonauth-session.json'));
    expect((await result).blockedBy).toBe('Underhill');
  });

  it('enters an alias by its id and reads the session it answers', async () => {
    const result = firstValueFrom(api.enter(900002));
    const request = expectPost('/enter');
    expect(body(request)).toBe('playerId=900002');
    request.flush(readServerResponse('argonauth-session-password.json'));
    expect((await result).player).toBe('Samwise');
  });

  it('creates an alias by name, and links one by login and password, strictly encoded', async () => {
    const created = firstValueFrom(api.create('New_one'));
    const create = expectPost('/create');
    expect(body(create)).toBe('name=New_one');
    create.flush(readServerResponse('argonauth-session-aliases.json'));
    expect((await created).aliases.length).toBe(3);

    const linked = firstValueFrom(api.link('old+one', 'p&w'));
    const link = expectPost('/link');
    expect(body(link)).toBe('login=old%2Bone&password=p%26w');
    link.flush(readServerResponse('argonauth-session-aliases.json'));
    expect((await linked).pcAccount?.name).toBe('Frodo Baggins');
  });

  it('logs out with a scope and gives where to go next', async () => {
    const alias = firstValueFrom(api.logout('alias'));
    const request = expectPost('/logout');
    expect(body(request)).toBe('scope=alias');
    request.flush(readServerResponse('argonauth-logout-alias.json'));
    await expect(alias).resolves.toBe('/gemp-lotr/ng/login');

    const all = firstValueFrom(api.logout('all'));
    expectPost('/logout').flush('{}');
    await expect(all).resolves.toBe('');
  });

  it('connects with the password and gives where to go next', async () => {
    const result = firstValueFrom(api.connect('p&w'));
    const request = expectPost('/connect');
    expect(body(request)).toBe('password=p%26w');
    request.flush(readServerResponse('argonauth-connect.json'));
    await expect(result).resolves.toMatch(/^https:\/\/auth\.lotrtcgpc\.net\/authorize\?/);
  });

  it("gives a refusal as an ApiError with the server's code", async () => {
    const result = firstValueFrom(api.create('Frodo'));
    expectPost('/create').flush('{"error": "name-taken"}', { status: 409, statusText: 'Conflict' });
    const error = (await result.catch((e: unknown) => e)) as ApiError;
    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(409);
    expect(error.said).toBe('name-taken');
  });
});
