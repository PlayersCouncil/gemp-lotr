import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { LocalSettings } from '../../platform/local-settings.service';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { readServerResponse } from '../../../testing/files';
import {
  FILTER_DELAY_MS,
  FORMAT_IMAGES_KEY,
  FormatDefinitionsService,
  HIGHLIGHT_MS,
} from './format-definitions.service';
import { HELP_SERVICES } from './help-services';

const URL = '/gemp-lotr-server/hall/formats/json';

class FakeSettings {
  readonly values = new Map<string, string>();
  read(key: string): string | null {
    return this.values.get(key) ?? null;
  }
  write(key: string, value: string): void {
    this.values.set(key, value);
  }
}

describe('FormatDefinitionsService', () => {
  let backend: HttpTestingController;
  let settings: FakeSettings;

  function service(): FormatDefinitionsService {
    return TestBed.inject(FormatDefinitionsService);
  }

  async function answer(body: string = readServerResponse('format-definitions.json')): Promise<void> {
    const request = backend.expectOne((req) => req.url === URL);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('participantId')).toBe('alice');
    request.flush(body);
    await Promise.resolve();
    await Promise.resolve();
  }

  beforeEach(() => {
    vi.useFakeTimers();
    settings = new FakeSettings();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ...HELP_SERVICES,
        { provide: LocalSettings, useValue: settings },
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('shows "Loading" until the formats arrive', async () => {
    const formats = service();
    const done = formats.fetch();
    expect(formats.load()).toEqual({ kind: 'loading' });
    await answer();
    await done;
    expect(formats.load().kind).toBe('ready');
    expect(formats.formats().map((format) => format.code)).toEqual(['pc_fotr_block', 'rev_tow_sta', 'pc_playtest']);
  });

  it("says what went wrong in the page's place (neutralErrorMap)", async () => {
    const formats = service();
    const done = formats.fetch();
    backend.expectOne((req) => req.url === URL).flush('', { status: 503, statusText: 'Unavailable' });
    await done;
    expect(formats.load()).toEqual({
      kind: 'error',
      message: 'Could not load the format definitions right now. Please try again in a few minutes.',
    });
    expect(formats.formats()).toEqual([]);
  });

  it('keeps only the latest answer when fetched again', async () => {
    const formats = service();
    const first = formats.fetch();
    const firstRequest = backend.expectOne((req) => req.url === URL);
    const second = formats.fetch();
    firstRequest.flush(JSON.stringify({ formats: [{ code: 'old' }] }));
    await first;
    expect(formats.load().kind).toBe('loading');
    await answer();
    await second;
    expect(formats.formats()).toHaveLength(3);
  });

  it('filters a moment after the last key, and remembers nothing of it', async () => {
    const formats = service();
    const done = formats.fetch();
    await answer();
    await done;
    formats.setQuery('ey');
    formats.setQuery('eowyn');
    expect(formats.query()).toBe('eowyn');
    expect(formats.filter().visible.size).toBe(3);
    vi.advanceTimersByTime(FILTER_DELAY_MS - 1);
    expect(formats.filter().visible.size).toBe(3);
    vi.advanceTimersByTime(1);
    expect([...formats.filter().visible]).toEqual(['pc_fotr_block']);
    formats.clearFilter();
    expect(formats.query()).toBe('');
    expect(formats.filter().visible.size).toBe(3);
  });

  it('opens a format: the filter hiding it cleared, marked and highlighted for a moment', async () => {
    const formats = service();
    const done = formats.fetch();
    await answer();
    await done;
    formats.setQuery('eowyn');
    vi.advanceTimersByTime(FILTER_DELAY_MS);
    formats.open('rev_tow_sta');
    expect(formats.query()).toBe('');
    expect(formats.filter().visible.has('rev_tow_sta')).toBe(true);
    expect(formats.reveal()).toEqual({ code: 'rev_tow_sta', seq: 1 });
    expect(formats.active()).toBe('rev_tow_sta');
    expect(formats.highlighted()).toBe('rev_tow_sta');
    vi.advanceTimersByTime(HIGHLIGHT_MS);
    expect(formats.highlighted()).toBeNull();

    // a visible format leaves the filter as it is; opening it again reveals it again
    formats.setQuery('pc');
    vi.advanceTimersByTime(FILTER_DELAY_MS);
    formats.open('pc_playtest');
    formats.open('pc_playtest');
    expect(formats.query()).toBe('pc');
    expect(formats.reveal()).toEqual({ code: 'pc_playtest', seq: 3 });
    formats.setActive('pc_fotr_block');
    expect(formats.active()).toBe('pc_fotr_block');
  });

  it('opens a format a link named before the formats arrived; an unknown one does nothing', async () => {
    const formats = service();
    formats.open('pc_playtest');
    const done = formats.fetch();
    await answer();
    await done;
    expect(formats.reveal()?.code).toBe('pc_playtest');
    formats.open('no_such_format');
    expect(formats.reveal()?.code).toBe('pc_playtest');
  });

  it('remembers "Card images" on the device', () => {
    settings.values.set(FORMAT_IMAGES_KEY, '1');
    const formats = service();
    expect(formats.images()).toBe(true);
    formats.setImages(false);
    expect(formats.images()).toBe(false);
    expect(settings.values.get(FORMAT_IMAGES_KEY)).toBe('0');
  });
});
