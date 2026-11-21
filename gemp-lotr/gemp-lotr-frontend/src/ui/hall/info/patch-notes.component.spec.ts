import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { INFO_ROUTES } from './info.routes';

describe('PatchNotesComponent', () => {
  let backend: HttpTestingController;

  async function open(url: string) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        provideRouter([
          { path: 'info', children: INFO_ROUTES },
          { path: 'help/formats/:code', children: [] },
        ]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    backend = TestBed.inject(HttpTestingController);
    await harness.fixture.whenStable();
    return { harness, el: harness.routeNativeElement as HTMLElement, router: TestBed.inject(Router) };
  }

  async function answer(harness: RouterTestingHarness, path = '/patchnotes', body = 'patch-notes-page.json') {
    const request = backend.expectOne((req) => req.url.endsWith(path));
    request.flush(readServerResponse(body));
    await harness.fixture.whenStable();
    harness.detectChanges();
    await harness.fixture.whenStable();
    harness.detectChanges();
    return request;
  }

  it("shows a link's updates, the linked one marked, and keeps the address on the window", async () => {
    const { harness, el, router } = await open('/info/patch-notes/2026-09-27-hall-overhaul');
    const request = await answer(harness);
    expect(request.request.params.get('from')).toBe('2026-09-27-hall-overhaul');
    await answer(harness, '/patchnotes/months', 'patch-notes-months.json');
    expect(router.url).toBe('/info/patch-notes/2026-09-27-hall-overhaul');
    const notes = Array.from(el.querySelectorAll<HTMLElement>('article.gd-note'));
    expect(notes.map((n) => [n.dataset['slug'], n.hasAttribute('data-target')])).toEqual([
      ['2026-09-27-hall-overhaul', true],
      ['announcement-12', false],
    ]);
    expect(notes[0]?.querySelector('.gd-note__title')?.textContent?.trim()).toBe('The hall overhaul');
    expect(notes[0]?.querySelector('.gd-note__date')?.textContent).toBe('2026-09-27');
    expect(notes[1]?.querySelector('.gd-note__title time')?.textContent).toBe('2023-12-09');
    expect(Array.from(notes[0]!.querySelectorAll('.gd-note__tag')).map((t) => t.textContent)).toEqual([
      'User Interface',
      'Card Fixes',
    ]);
    expect(notes[0]?.querySelector('[data-kind="summary"] img.gd-markup__image[data-icon]')).not.toBeNull();
    expect(el.querySelector('.gd-notes__range')?.textContent).toBe('Updates 6–7 of 7');
    expect(el.querySelector('.gd-notes__newer')).not.toBeNull();
    expect(el.querySelector('.gd-notes__older')).toBeNull();
    expect(el.querySelector('.gd-notes__month-title')?.textContent).toContain('September 2026');
    expect(
      Array.from(el.querySelectorAll('.gd-notes__filter')).map((f) =>
        Array.from(f.children)
          .map((c) => c.textContent?.trim())
          .join(' '),
      ),
    ).toEqual(['All 7', 'Card Fixes 3', 'PC Updates 2', 'User Interface 2', 'Announcement 1']);
  });

  it("opens a note's card, follows its hall links, and enlarges its screenshots", async () => {
    const { harness, el, router } = await open('/info/patch-notes');
    await answer(harness);
    backend.match(() => true);
    const info = harness.routeDebugElement!.injector.get(CardInfoService);
    el.querySelector<HTMLButtonElement>('.gd-markup__card')!.click();
    expect(info.card()?.blueprintId).toBe('1_5');
    const screenshot = el.querySelector<HTMLImageElement>('[data-kind="body"] img.gd-markup__image:not([data-icon])')!;
    screenshot.click();
    harness.detectChanges();
    const lightbox = el.querySelector('.gd-notes__lightbox');
    expect(lightbox?.getAttribute('aria-label')).toBe('Screenshot: The new hall');
    expect(lightbox?.querySelector('figcaption')?.textContent).toBe('The new hall');
    lightbox!.querySelector<HTMLButtonElement>('.gd-notes__lightbox-close')!.click();
    harness.detectChanges();
    expect(el.querySelector('.gd-notes__lightbox')).toBeNull();
    el.querySelector<HTMLAnchorElement>('a.gd-markup__hall-link')!.click();
    await harness.fixture.whenStable();
    expect(router.url).toBe('/help/formats/pc_movie');
  });

  it('pages to the newer updates, the newest page without a slug in the address', async () => {
    const { harness, el, router } = await open('/info/patch-notes/2026-09-27-hall-overhaul');
    await answer(harness);
    backend.match((req) => req.url.endsWith('/months'));
    el.querySelector<HTMLButtonElement>('.gd-notes__newer')!.click();
    const request = backend.expectOne((req) => req.url.endsWith('/patchnotes'));
    expect(request.request.params.get('start')).toBe('0');
    const newest = JSON.parse(readServerResponse('patch-notes-page.json'));
    newest.start = 0;
    request.flush(newest);
    await harness.fixture.whenStable();
    harness.detectChanges();
    await harness.fixture.whenStable();
    expect(router.url).toBe('/info/patch-notes');
  });

  it('says when the notes could not be loaded, with Try again', async () => {
    const { harness, el } = await open('/info/patch-notes');
    backend.expectOne((req) => req.url.endsWith('/patchnotes')).flush('', { status: 503, statusText: 'x' });
    await harness.fixture.whenStable();
    harness.detectChanges();
    const status = el.querySelector<HTMLElement>('.gd-notes__status[data-error]')!;
    expect(status.hidden).toBe(false);
    expect(status.textContent).toContain('The patch notes could not be loaded (the server answered 503).');
    status.querySelector<HTMLButtonElement>('.gd-notes__retry')!.click();
    backend.expectOne((req) => req.url.endsWith('/patchnotes'));
  });
});
