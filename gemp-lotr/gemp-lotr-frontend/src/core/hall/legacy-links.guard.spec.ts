import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvironmentInjector, runInInjectionContext } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { legacyHallLink, startPage } from './legacy-links.guard';

describe('the old addresses', () => {
  let visited: string[];
  let backend: HttpTestingController;

  beforeEach(() => {
    visited = [];
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        { provide: PageNavigation, useValue: { go: (url: string) => visited.push(url) } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const run = <T>(guard: () => T): T => runInInjectionContext(TestBed.inject(EnvironmentInjector), guard);
  const state = (url: string) => ({ url }) as RouterStateSnapshot;
  const written = (result: unknown) =>
    result instanceof UrlTree ? TestBed.inject(Router).serializeUrl(result) : result;
  const player = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/player');

  describe('go (hall.html)', () => {
    const go = (url: string) => written(run(() => legacyHallLink({} as ActivatedRouteSnapshot, state(url))));

    it('opens the route a legacy hall link names, asking nobody to log in', () => {
      expect(go('/go#format-pc_movie')).toBe('/help/formats/pc_movie');
      expect(go('/go#events/leagues')).toBe('/events/leagues');
      expect(go('/go#errata-1_5?q=gandalf')).toBe('/help/errata/1_5?q=gandalf');
      expect(go('/go#help/manual')).toBe('/help/how-to-play#manual');
      expect(visited).toEqual([]);
    });

    it('opens the hall for no link, or one it does not know', () => {
      expect(go('/go')).toBe('/');
      expect(go('/go#')).toBe('/');
      expect(go('/go#nowhere')).toBe('/');
    });

    it('leaves the app for the legacy hall, for Admin', () => {
      expect(go('/go#admin/league')).toBe(false);
      expect(visited).toEqual(['/gemp-lotr/hall-legacy.html?admin=1#admin/league']);
    });
  });

  describe("start (the site's start page)", () => {
    const start = async (url: string, answer: () => void) => {
      const result = run(() => startPage({} as ActivatedRouteSnapshot, state(url))) as Promise<UrlTree | boolean>;
      answer();
      return written(await result);
    };
    const loggedIn = () => player().flush('{"name":"frodo","type":"u"}');
    const visitor = () => player().flush('', { status: 401, statusText: 'Unauthorized' });

    it('sends a visitor to the login page, which comes back to the place the link names', async () => {
      expect(await start('/start', visitor)).toBe('/login');
      // (asked again: nobody was there the first time)
      expect(await start('/start#events/leagues', visitor)).toBe('/login?return=%2Fevents%2Fleagues');
    });

    it('sends a logged-in player to the hall, at the place the link names', async () => {
      expect(await start('/start', loggedIn)).toBe('/');
      expect(await start('/start#league-wc2026', () => undefined)).toBe('/events/leagues/wc2026');
      expect(visited).toEqual([]);
    });

    it("opens the hall when the server doesn't say who is there", async () => {
      const failing = () => player().flush('', { status: 503, statusText: 'Service Unavailable' });
      expect(await start('/start#help', failing)).toBe('/help');
    });

    it('takes a logged-in admin to the legacy hall for an Admin link', async () => {
      expect(await start('/start#admin', loggedIn)).toBe(false);
      expect(visited).toEqual(['/gemp-lotr/hall-legacy.html?admin=1#admin']);
    });
  });
});
