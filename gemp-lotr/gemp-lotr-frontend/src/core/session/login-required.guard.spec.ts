import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvironmentInjector, runInInjectionContext } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, RouterStateSnapshot } from '@angular/router';
import { firstValueFrom, Observable } from 'rxjs';
import { provideGempHttp } from '../api/gemp-interceptors';
import { LoginPage } from '../platform/login-page.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { loginRequired, loginRequiredUnlessReplay } from './login-required.guard';

describe('loginRequired', () => {
  const isLoginCheck = (request: { url: string }) => request.url === '/gemp-lotr-server/player';
  let toLogin: ReturnType<typeof vi.fn>;

  function start(queryString = ''): { allowed: Promise<boolean>; http: HttpTestingController } {
    toLogin = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: LoginPage, useValue: { open: toLogin } },
        { provide: PAGE_QUERY_STRING, useValue: queryString },
      ],
    });
    const result = runInInjectionContext(TestBed.inject(EnvironmentInjector), () =>
      loginRequired({} as ActivatedRouteSnapshot, { url: '/deckbuilder?theme=modern' } as RouterStateSnapshot),
    );
    return {
      allowed: firstValueFrom(result as Observable<boolean>),
      http: TestBed.inject(HttpTestingController),
    };
  }

  it('lets a logged-in player in', async () => {
    const { allowed, http } = start();
    http.expectOne(isLoginCheck).flush('{}');
    expect(await allowed).toBe(true);
    expect(toLogin).not.toHaveBeenCalled();
  });

  it('sends a visitor who is not logged in to the login page', async () => {
    const { allowed, http } = start();
    http.expectOne(isLoginCheck).flush('', { status: 401, statusText: 'Unauthorized' });
    expect(await allowed).toBe(false);
    // ... which comes back to the page they asked for
    expect(toLogin).toHaveBeenCalledExactlyOnceWith('/deckbuilder?theme=modern');
  });

  it.each([
    [403, 'Forbidden'],
    [500, 'Internal Server Error'],
    [503, 'Service Unavailable'],
  ])('still opens the page when the server answers %i', async (status, statusText) => {
    const { allowed, http } = start();
    http.expectOne(isLoginCheck).flush('', { status, statusText });
    expect(await allowed).toBe(true);
    expect(toLogin).not.toHaveBeenCalled();
  });

  it('still opens the page when the connection fails', async () => {
    const { allowed, http } = start();
    http.expectOne(isLoginCheck).error(new ProgressEvent('error'));
    expect(await allowed).toBe(true);
    expect(toLogin).not.toHaveBeenCalled();
  });

  it("asks about the session's player, whatever participantId the address names", () => {
    const { http } = start('?participantId=bob');
    expect(http.expectOne(isLoginCheck).request.params.has('participantId')).toBe(false);
  });
});

describe('loginRequiredUnlessReplay', () => {
  function check(query: Record<string, string>) {
    const toLogin = vi.fn();
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: LoginPage, useValue: { open: toLogin } }],
    });
    const route = { queryParamMap: convertToParamMap(query) } as ActivatedRouteSnapshot;
    const result = runInInjectionContext(TestBed.inject(EnvironmentInjector), () =>
      loginRequiredUnlessReplay(route, {} as RouterStateSnapshot),
    );
    return { result, toLogin, http: TestBed.inject(HttpTestingController) };
  }

  it('opens a replay link without asking whether anyone is logged in', () => {
    const { result, toLogin, http } = check({ replayId: 'alice$abc' });
    expect(result).toBe(true);
    http.expectNone((r) => r.url === '/gemp-lotr-server/player');
    expect(toLogin).not.toHaveBeenCalled();
  });

  it('still sends a visitor who is not logged in from a game to the login page', async () => {
    const { result, toLogin, http } = check({ gameId: '7' });
    const allowed = firstValueFrom(result as Observable<boolean>);
    http.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('', { status: 401, statusText: 'Unauthorized' });
    expect(await allowed).toBe(false);
    expect(toLogin).toHaveBeenCalledOnce();
  });
});
