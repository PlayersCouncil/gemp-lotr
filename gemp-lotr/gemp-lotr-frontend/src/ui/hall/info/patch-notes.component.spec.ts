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
    const newest = JSON.parse(readServerResponse('patch-notes-page.json')) as { start: number };
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

  describe('the filter, the months and the states', () => {
    const page = (changes: Record<string, unknown> = {}) =>
      JSON.stringify({
        ...(JSON.parse(readServerResponse('patch-notes-page.json')) as Record<string, unknown>),
        ...changes,
      });

    async function settle(harness: RouterTestingHarness): Promise<void> {
      await harness.fixture.whenStable();
      harness.detectChanges();
      await harness.fixture.whenStable();
      harness.detectChanges();
    }

    async function shown(url = '/info/patch-notes/2026-09-27-hall-overhaul') {
      const opened = await open(url);
      await answer(opened.harness);
      return opened;
    }

    it('shows the newest updates of a tag from its chip', async () => {
      const { harness, el } = await shown();
      backend.match((req) => req.url.endsWith('/months'));
      el.querySelectorAll<HTMLButtonElement>('.gd-notes__filter')[2]!.click();
      const request = backend.expectOne((req) => req.url.endsWith('/patchnotes'));
      expect(request.request.params.get('tag')).toBe('PC Updates');
      expect(request.request.params.get('start')).toBe('0');
      request.flush(page({ start: 0, total: 0, notes: [], tag: 'PC Updates' }));
      await settle(harness);
      expect(el.querySelector('.gd-notes__filter[aria-checked="true"]')?.textContent).toContain('PC Updates');
      expect(el.querySelector('.gd-notes__empty')?.textContent).toBe('No updates are tagged “PC Updates” yet.');
      expect(el.querySelector('.gd-notes__pager')).toBeNull();
    });

    it("lists the months by year in the title's dropdown, and shows a month from its newest update", async () => {
      const { harness, el } = await shown();
      const title = el.querySelector<HTMLElement>('.gd-notes__month-title')!;
      title.click();
      harness.detectChanges();
      expect(title.getAttribute('aria-expanded')).toBe('true');
      expect(el.querySelector('.gd-notes__dropdown-status[data-loading]')?.textContent).toBe('Loading months…');
      await answer(harness, '/patchnotes/months', 'patch-notes-months.json');
      const years = Array.from(el.querySelectorAll('.gd-notes__year')).map((year) => [
        year.querySelector('.gd-notes__year-label')?.textContent,
        Array.from(year.querySelectorAll('.gd-notes__month')).map((m) => [
          m.textContent?.trim(),
          m.hasAttribute('data-selected'),
        ]),
      ]);
      expect(years).toEqual([
        [
          '2026',
          [
            ['November', false],
            ['September', true],
          ],
        ],
        ['2023', [['December', false]]],
      ]);
      el.querySelectorAll<HTMLButtonElement>('.gd-notes__month')[2]!.click();
      expect(backend.expectOne((req) => req.url.endsWith('/patchnotes')).request.params.get('from')).toBe(
        'announcement-12',
      );
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__dropdown')).toBeNull();
    });

    it('steps to the older and the newer month with updates', async () => {
      const { harness, el } = await shown();
      await answer(harness, '/patchnotes/months', 'patch-notes-months.json');
      const [older, newer] = Array.from(el.querySelectorAll<HTMLButtonElement>('.gd-notes__month-step'));
      expect([older?.disabled, newer?.disabled]).toEqual([false, false]);
      older!.click();
      await settle(harness);
      expect(backend.expectOne((req) => req.url.endsWith('/patchnotes')).request.params.get('from')).toBe(
        'announcement-12',
      );
      newer!.click();
      await settle(harness);
      expect(backend.expectOne((req) => req.url.endsWith('/patchnotes')).request.params.get('from')).toBe('2026-11-18');
    });

    it('says when the months could not be had, with Retry, and when there are none', async () => {
      const { harness, el } = await shown();
      backend.expectOne((req) => req.url.endsWith('/months')).flush('', { status: 500, statusText: 'x' });
      await settle(harness);
      // opening the dropdown asks again for months it has not got (onOpen)
      el.querySelector<HTMLElement>('.gd-notes__month-title')!.click();
      harness.detectChanges();
      backend.expectOne((req) => req.url.endsWith('/months')).flush('', { status: 500, statusText: 'x' });
      await settle(harness);
      expect(el.querySelector('.gd-notes__dropdown-status[data-error]')?.textContent).toBe(
        'Could not load the list of months. (the server answered 500)',
      );
      el.querySelector<HTMLButtonElement>('.gd-notes__dropdown .gd-notes__retry')!.click();
      backend.expectOne((req) => req.url.endsWith('/months')).flush(JSON.stringify({ tag: null, months: [] }));
      await settle(harness);
      expect(el.querySelector('.gd-notes__dropdown-status[data-empty]')?.textContent).toBe(
        'There are no patch notes yet.',
      );
    });

    it('opens and closes the dropdown from the keyboard, and closes it on a press elsewhere', async () => {
      const { harness, el } = await shown();
      backend.match((req) => req.url.endsWith('/months'));
      const title = el.querySelector<HTMLElement>('.gd-notes__month-title')!;
      const key = (name: string) => {
        title.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true }));
        harness.detectChanges();
        return el.querySelector('.gd-notes__dropdown') !== null;
      };
      expect(key('Enter')).toBe(true);
      expect(key(' ')).toBe(false);
      expect(key('Enter')).toBe(true);
      expect(key('a')).toBe(true);
      expect(key('Escape')).toBe(false);
      expect(key(' ')).toBe(true);
      el.querySelector<HTMLElement>('.gd-notes__dropdown')!.dispatchEvent(
        new MouseEvent('mousedown', { bubbles: true }),
      );
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__dropdown')).not.toBeNull();
      el.querySelector<HTMLElement>('.gd-notes__heading')!.dispatchEvent(
        new MouseEvent('mousedown', { bubbles: true }),
      );
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__dropdown')).toBeNull();
    });

    it('pages to the older updates', async () => {
      const { harness, el } = await open('/info/patch-notes');
      backend.expectOne((req) => req.url.endsWith('/patchnotes')).flush(page({ start: 0, total: 12 }));
      await settle(harness);
      backend.match((req) => req.url.endsWith('/months'));
      expect(el.querySelector('.gd-notes__range')?.textContent).toBe('Updates 1–2 of 12');
      el.querySelector<HTMLButtonElement>('.gd-notes__older')!.click();
      expect(backend.expectOne((req) => req.url.endsWith('/patchnotes')).request.params.get('start')).toBe('5');
    });

    it('says "Loading patch notes…" once the first page is slow, and why a link shows the newest instead', async () => {
      const { harness, el } = await open('/info/patch-notes/2001-01-01');
      await new Promise((resolve) => setTimeout(resolve, 350));
      harness.detectChanges();
      const status = el.querySelector<HTMLElement>('.gd-notes__status')!;
      expect(status.hidden).toBe(false);
      expect(status.textContent?.trim()).toBe('Loading patch notes…');
      backend
        .expectOne((req) => req.url.endsWith('/patchnotes'))
        .flush('{"error":"x"}', { status: 404, statusText: 'x' });
      await settle(harness);
      await answer(harness);
      expect(el.querySelector('.gd-notes__notice')?.textContent).toBe(
        'There is no update named “2001-01-01”. Here are the newest ones.',
      );
      expect(status.hidden).toBe(true);
    });

    it('closes the lightbox on a click beside the picture, not on it, or on Esc; Esc alone does nothing', async () => {
      const { harness, el } = await shown();
      backend.match((req) => req.url.endsWith('/months'));
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
      const screenshot = el.querySelector<HTMLImageElement>(
        '[data-kind="body"] img.gd-markup__image:not([data-icon])',
      )!;
      screenshot.click();
      await settle(harness);
      const lightbox = el.querySelector<HTMLElement>('.gd-notes__lightbox')!;
      expect(document.activeElement).toBe(lightbox.querySelector('.gd-notes__lightbox-close'));
      lightbox.querySelector('img')!.click();
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__lightbox')).not.toBeNull();
      lightbox.click();
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__lightbox')).toBeNull();
      // the focus goes back to the screenshot, which takes it only if it can (a screenshot in a link), as on the legacy page
      expect(document.activeElement).not.toBe(lightbox.querySelector('.gd-notes__lightbox-close'));
      screenshot.click();
      harness.detectChanges();
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
      harness.detectChanges();
      expect(el.querySelector('.gd-notes__lightbox')).toBeNull();
    });
  });
});
