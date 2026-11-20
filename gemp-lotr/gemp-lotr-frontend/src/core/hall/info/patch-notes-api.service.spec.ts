import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { readServerResponse } from '../../../testing/files';
import { PATCH_NOTES_CACHE_MS, PatchNotesApi } from './patch-notes-api.service';

describe('PatchNotesApi', () => {
  let backend: HttpTestingController;
  let api: PatchNotesApi;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        PatchNotesApi,
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    api = TestBed.inject(PatchNotesApi);
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('asks for a page from a place or an update, with the filter, and no participant', async () => {
    void api.page({ start: 5 }, null);
    let request = backend.expectOne((req) => req.url === '/gemp-lotr-server/patchnotes');
    expect(request.request.params.get('start')).toBe('5');
    expect(request.request.params.get('count')).toBe('5');
    expect(request.request.params.has('participantId')).toBe(false);
    request.flush(readServerResponse('patch-notes-page.json'));

    const page = api.page({ slug: '2026-09-27-hall-overhaul' }, 'Card Fixes');
    request = backend.expectOne((req) => req.url === '/gemp-lotr-server/patchnotes');
    expect(request.request.params.get('from')).toBe('2026-09-27-hall-overhaul');
    expect(request.request.params.get('tag')).toBe('Card Fixes');
    expect(request.request.params.has('start')).toBe(false);
    request.flush(readServerResponse('patch-notes-page.json'));
    expect((await page).notes).toHaveLength(2);

    void api.page({ slug: null }, null);
    expect(backend.expectOne((req) => req.url === '/gemp-lotr-server/patchnotes').request.params.get('start')).toBe(
      '0',
    );
  });

  it('keeps a page a few minutes, waits for one on its way, and forgets a failure', async () => {
    const first = api.page({ start: 0 }, null);
    const again = api.page({ start: 0 }, null);
    backend
      .expectOne((req) => req.url === '/gemp-lotr-server/patchnotes')
      .flush(readServerResponse('patch-notes-page.json'));
    expect(await again).toBe(await first);
    expect(await api.page({ start: 0 }, null)).toBe(await first);
    backend.expectNone((req) => req.url === '/gemp-lotr-server/patchnotes');

    vi.advanceTimersByTime(PATCH_NOTES_CACHE_MS);
    const fresh = api.page({ start: 0 }, null);
    backend
      .expectOne((req) => req.url === '/gemp-lotr-server/patchnotes')
      .flush('', { status: 500, statusText: 'Error' });
    await expect(fresh).rejects.toBeTruthy();
    void api.page({ start: 0 }, null);
    backend
      .expectOne((req) => req.url === '/gemp-lotr-server/patchnotes')
      .flush(readServerResponse('patch-notes-page.json'));
  });

  it('asks for the months of the filter', async () => {
    const months = api.months('PC Updates');
    const request = backend.expectOne((req) => req.url === '/gemp-lotr-server/patchnotes/months');
    expect(request.request.params.get('tag')).toBe('PC Updates');
    request.flush(readServerResponse('patch-notes-months.json'));
    expect(await months).toHaveLength(3);
    void api.months(null);
    expect(
      backend.expectOne((req) => req.url === '/gemp-lotr-server/patchnotes/months').request.params.has('tag'),
    ).toBe(false);
  });
});
