import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { readServerResponse } from '../../testing/files';
import { ApiError } from '../api/api-error';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { LoginApi } from './login-api.service';

describe('LoginApi', () => {
  let api: LoginApi;
  let backend: HttpTestingController;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        LoginApi,
        { provide: PAGE_QUERY_STRING, useValue: queryString },
      ],
    });
    api = TestBed.inject(LoginApi);
    backend = TestBed.inject(HttpTestingController);
  }

  afterEach(() => backend.verify());

  const expectPost = (path: string) =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server' + path && r.method === 'POST');

  it('posts login and password strictly form-encoded, with the participantId', async () => {
    setUp('?participantId=bob');
    const result = firstValueFrom(api.login('a+b&c', 'p w+%=é'));
    const request = expectPost('/login');
    expect((request.request.body as HttpParams).toString()).toBe(
      'login=a%2Bb%26c&password=p%20w%2B%25%3D%C3%A9&participantId=bob',
    );
    request.flush(readServerResponse('login-ok.xml'));
    await expect(result).resolves.toBeUndefined();
  });

  it('leaves out the participantId when the page has none', async () => {
    setUp();
    const result = firstValueFrom(api.register('newbie', 'secret'));
    const request = expectPost('/register');
    expect((request.request.body as HttpParams).toString()).toBe('login=newbie&password=secret');
    request.flush(readServerResponse('login-ok.xml'));
    await expect(result).resolves.toBeUndefined();
  });

  it.each([401, 403, 409, 429, 503, 400, 500])('turns HTTP %i into an ApiError with that status', async (status) => {
    setUp();
    const result = firstValueFrom(api.login('x', 'y'));
    expectPost('/login').flush('', { status, statusText: 'Error' });
    const error = await result.catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(status);
  });

  it('gets the status line from GET /status, not cached', async () => {
    setUp();
    const result = firstValueFrom(api.serverStatus());
    const request = backend.expectOne((r) => r.url === '/gemp-lotr-server/status' && r.method === 'GET');
    expect(request.request.params.has('_')).toBe(true);
    request.flush(readServerResponse('server-status.json'));
    await expect(result).resolves.toBe(readServerResponse('server-status.txt'));
  });
});
