import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Location } from '@angular/common';
import { createEnvironmentInjector, EnvironmentInjector } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Notifier } from '../../core/notifier/notifier';
import { DialogNotifier } from '../shared/notifier/dialog-notifier';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Observable, of, throwError } from 'rxjs';
import { ApiError } from '../../core/api/api-error';
import { GempHttp } from '../../core/api/gemp-http.service';
import { LoginPage } from '../../core/platform/login-page.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { DeckbuilderPageComponent } from '../deckbuilder/deckbuilder-page.component';
import { GamePageComponent } from '../game/game-page.component';
import { HallShellComponent } from '../hall/hall-shell.component';
import { HALL_SERVICES } from '../../core/hall/hall-services';
import { AppComponent } from './app.component';
import { APP_ROUTES } from './app.routes';

describe('app routes', () => {
  let loginCheck: () => Observable<string>;

  beforeEach(() => {
    loginCheck = () => of('{}');
    TestBed.configureTestingModule({
      providers: [
        provideRouter(APP_ROUTES),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    const http = TestBed.inject(GempHttp);
    const getText = http.getText.bind(http);
    vi.spyOn(http, 'getText').mockImplementation((path, ...rest) =>
      path === '/player' ? loginCheck() : getText(path, ...rest),
    );
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  let harness: RouterTestingHarness;
  beforeEach(async () => (harness = await RouterTestingHarness.create()));

  /** Opens an address as the browser gives it to the router: Location drops a trailing slash ("ng/deckbuilder/"). */
  async function open(url: string) {
    const page = await harness.navigateByUrl(TestBed.inject(Location).normalize(url));
    return { page, url: TestBed.inject(Router).url };
  }

  it('loads the deck builder at deckbuilder, with or without a trailing slash', async () => {
    expect((await open('/deckbuilder')).page).toBeInstanceOf(DeckbuilderPageComponent);
    expect((await open('/deckbuilder/')).page).toBeInstanceOf(DeckbuilderPageComponent);
  });

  it('keeps the query string (?theme=, ?participantId=)', async () => {
    const { page, url } = await open('/deckbuilder/?theme=modern&participantId=bob');
    expect(page).toBeInstanceOf(DeckbuilderPageComponent);
    expect(url).toBe('/deckbuilder?theme=modern&participantId=bob');
  });

  it('opens the Game Hall at the app root and at unknown addresses, keeping the query string', async () => {
    expect(await open('/')).toEqual({ page: expect.any(HallShellComponent), url: '/hall' });
    expect(harness.fixture.nativeElement.querySelector('gd-hall-shell gd-game-hall-tab')).not.toBeNull();
    expect((await open('/no/such/page?theme=classic')).url).toBe('/hall?theme=classic');
  });

  it('sends a visitor who is not logged in from the deck builder to the login page, without showing it', async () => {
    loginCheck = () => throwError(() => new ApiError(401, 'Not logged in'));
    const toLogin = vi.spyOn(TestBed.inject(LoginPage), 'open').mockImplementation(() => undefined);
    expect((await open('/deckbuilder')).page).toBeNull();
    expect(toLogin).toHaveBeenCalledOnce();
  });

  it('shows a visitor the hall (whose own dialog asks them to log in), not the login page', async () => {
    loginCheck = () => throwError(() => new ApiError(401, 'Not logged in'));
    const toLogin = vi.spyOn(TestBed.inject(LoginPage), 'open').mockImplementation(() => undefined);
    expect((await open('/help')).page).toBeInstanceOf(HallShellComponent);
    expect(harness.fixture.nativeElement.querySelector('gd-hall-shell gd-hall-placeholder')).not.toBeNull();
    expect(toLogin).not.toHaveBeenCalled();
  });

  it('gives the hall its own services, in its route', () => {
    const hall = APP_ROUTES.find((route) => route.path === '' && route.children);
    expect(hall?.providers).toEqual(HALL_SERVICES);
    expect(APP_ROUTES.at(-1)).toBe(hall);
  });

  it("loads the game page at game, with the page's own dialogs for its questions", async () => {
    const { page } = await open('/game');
    expect(page).toBeInstanceOf(GamePageComponent);
  });

  it("gives the game page the page's own dialogs for its questions", () => {
    const game = APP_ROUTES.find((route) => route.path === 'game');
    const injector = createEnvironmentInjector(game?.providers ?? [], TestBed.inject(EnvironmentInjector));
    expect(injector.get(Notifier)).toBeInstanceOf(DialogNotifier);
    expect(injector.get(Notifier)).toBe(injector.get(DialogNotifier));
  });

  it('shows the page in the root component (<gemp-app>, no box of its own)', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await TestBed.inject(Router).navigateByUrl('/deckbuilder');
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.style.display).toBe('contents');
    expect(el.querySelector('router-outlet + .gd-page')).not.toBeNull();
  });
});
