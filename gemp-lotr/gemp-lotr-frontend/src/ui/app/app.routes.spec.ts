import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { DeckbuilderPageComponent } from '../deckbuilder/deckbuilder-page.component';
import { AppComponent } from './app.component';
import { APP_ROUTES } from './app.routes';

describe('app routes', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(APP_ROUTES),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).match(() => true));

  let harness: RouterTestingHarness;
  beforeEach(async () => (harness = await RouterTestingHarness.create()));

  async function open(url: string) {
    const page = await harness.navigateByUrl(url);
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

  it('sends the app root and unknown addresses to the deck builder', async () => {
    expect(await open('/')).toEqual({ page: expect.any(DeckbuilderPageComponent), url: '/deckbuilder' });
    expect((await open('/no/such/page?theme=classic')).url).toBe('/deckbuilder?theme=classic');
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
