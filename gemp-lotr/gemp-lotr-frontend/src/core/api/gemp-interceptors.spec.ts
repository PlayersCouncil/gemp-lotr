import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { SERVER_URL } from '../platform/app-paths';
import { ApiError } from './api-error';
import { BYPASS_CACHE, OPENED_PACK_HEADER, provideGempHttp } from './gemp-interceptors';
import { DeliveryAnnouncements } from './delivery-announcements';
import { OpenedPackAnnouncements } from './opened-pack-announcements';

describe('the Gemp server interceptors', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  function setUp(queryString = '?participantId=bob') {
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: queryString }],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  }

  afterEach(() => backend.verify());

  const url = `${SERVER_URL}/x`;

  it('leaves requests to anywhere else alone', async () => {
    setUp();
    const answer = firstValueFrom(http.get('https://elsewhere.test/a', { responseType: 'text' }));
    const req = backend.expectOne('https://elsewhere.test/a');
    expect(req.request.params.keys()).toEqual([]);
    req.flush('', { status: 500, statusText: 'x' });
    await expect(answer).rejects.not.toBeInstanceOf(ApiError);
  });

  it('adds a cache-buster to GETs, and no participant even when the page names one', () => {
    setUp();
    http.get(url, { responseType: 'text' }).subscribe();
    const req = backend.expectOne((r) => r.url === url);
    expect(req.request.params.has('participantId')).toBe(false);
    expect(req.request.params.has('_')).toBe(true);
    req.flush('');
  });

  it('leaves the body of POSTs as it is, and adds no cache-buster', () => {
    setUp();
    http.post(url, new HttpParams().set('a', '1'), { responseType: 'text' }).subscribe();
    const req = backend.expectOne(url);
    expect((req.request.body as HttpParams).toString()).toBe('a=1');
    expect(req.request.params.has('_')).toBe(false);
    req.flush('');
  });

  it('can leave the cache-buster out, per request', () => {
    setUp();
    const context = new HttpContext().set(BYPASS_CACHE, false);
    http.get(url, { responseType: 'text', context }).subscribe();
    const req = backend.expectOne(url);
    expect(req.request.params.keys()).toEqual([]);
    req.flush('');
  });

  it('turns failures into ApiError with the status', async () => {
    setUp();
    const answer = firstValueFrom(http.get(url, { responseType: 'text' }));
    backend.expectOne((r) => r.url === url).flush('', { status: 404, statusText: 'x' });
    await expect(answer).rejects.toMatchObject({ name: 'ApiError', status: 404 });
  });

  it('announces opened packs from the response header, and only then', () => {
    setUp();
    const announced = vi.fn();
    TestBed.inject(OpenedPackAnnouncements).announced$.subscribe(announced);
    http.get(url, { responseType: 'text' }).subscribe();
    backend.expectOne((r) => r.url === url).flush('');
    expect(announced).not.toHaveBeenCalled();
    http.get(url, { responseType: 'text' }).subscribe();
    backend.expectOne((r) => r.url === url).flush('', { headers: { [OPENED_PACK_HEADER]: 'true' } });
    expect(announced).toHaveBeenCalledOnce();
  });

  it("announces the hall's deliveries from their headers (deliveryCheck)", () => {
    setUp();
    const announced: string[] = [];
    TestBed.inject(DeliveryAnnouncements).announced$.subscribe((kind) => announced.push(kind));
    http.get(url, { responseType: 'text' }).subscribe();
    backend.expectOne((r) => r.url === url).flush('', { headers: { 'Delivery-Service-Package': 'false' } });
    expect(announced).toEqual([]);
    http.get(url, { responseType: 'text' }).subscribe();
    backend
      .expectOne((r) => r.url === url)
      .flush('', {
        headers: {
          'Delivery-Service-Package': 'true',
          'Delivery-Service-Announcement': 'true',
          'Delivery-Service-League': 'true',
        },
      });
    expect(announced).toEqual(['package', 'announcement', 'league']);
  });
});
