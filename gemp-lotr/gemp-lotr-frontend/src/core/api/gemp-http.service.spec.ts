import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { ApiError, INVALID_RESPONSE_STATUS } from './api-error';
import { GempHttp, OPENED_PACK_HEADER } from './gemp-http.service';

describe('GempHttp', () => {
  let http: GempHttp;
  let backend: HttpTestingController;

  function setUp(queryString = '') {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: queryString },
      ],
    });
    http = TestBed.inject(GempHttp);
    backend = TestBed.inject(HttpTestingController);
  }

  afterEach(() => backend.verify());

  it('GETs from the server path with a cache-busting parameter and no participantId by default', async () => {
    setUp();
    const result = firstValueFrom(http.getText('/deck/list'));
    const req = backend.expectOne((r) => r.url === '/gemp-lotr-server/deck/list');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('_')).toBe(true);
    expect(req.request.params.has('participantId')).toBe(false);
    req.flush('<decks/>');
    expect(await result).toBe('<decks/>');
  });

  it('can skip the cache-busting parameter', () => {
    setUp();
    http.getText('/x', {}, { noCache: false }).subscribe();
    const req = backend.expectOne('/gemp-lotr-server/x');
    expect(req.request.params.has('_')).toBe(false);
    req.flush('');
  });

  it('sends the participantId from the page URL, decoded once', () => {
    setUp('?participantId=Bob%20the%2BBrave');
    http.getText('/deck/list').subscribe();
    const req = backend.expectOne((r) => r.url === '/gemp-lotr-server/deck/list');
    expect(req.request.params.get('participantId')).toBe('Bob the+Brave');
    expect(req.request.urlWithParams).toContain('participantId=Bob%20the%2BBrave');
    req.flush('');
  });

  it('can leave the participantId out', () => {
    setUp('?participantId=bob');
    http.postText('/deck/formats', { includeEvents: false }, { participant: false }).subscribe();
    const req = backend.expectOne('/gemp-lotr-server/deck/formats');
    expect(req.request.serializeBody()).toBe('includeEvents=false');
    req.flush('{}');
  });

  it('POSTs form-encoded data with every reserved character encoded, leaving out null values', () => {
    setUp();
    http.postText('/deck', { deckName: 'A + B & C', notes: 'x=1;y', leagueCode: null }).subscribe();
    const req = backend.expectOne('/gemp-lotr-server/deck');
    expect(req.request.method).toBe('POST');
    expect(req.request.detectContentTypeHeader()).toBe('application/x-www-form-urlencoded;charset=UTF-8');
    expect(req.request.serializeBody()).toBe('deckName=A%20%2B%20B%20%26%20C&notes=x%3D1%3By');
    req.flush('<ok/>');
  });

  it('parses XML and JSON responses', async () => {
    setUp();
    const xml = firstValueFrom(http.postXml('/deck'));
    backend.expectOne('/gemp-lotr-server/deck').flush('<ok/>');
    expect((await xml).documentElement.tagName).toBe('ok');

    const json = firstValueFrom(http.postJson<{ a: number }>('/deck/formats'));
    backend.expectOne('/gemp-lotr-server/deck/formats').flush('{"a":1}');
    expect(await json).toEqual({ a: 1 });
  });

  it('turns HTTP failures into ApiError with the status', async () => {
    setUp();
    const result = firstValueFrom(http.getXml('/collection/nope'));
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/collection/nope')
      .flush('', { status: 404, statusText: 'Not Found' });
    const error = await result.catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(404);
    expect((error as ApiError).is(404)).toBe(true);
  });

  it('turns an unreadable response into ApiError', async () => {
    setUp();
    const result = firstValueFrom(http.postXml('/deck'));
    backend.expectOne('/gemp-lotr-server/deck').flush('<html>oops');
    const error = await result.catch((e: unknown) => e);
    expect((error as ApiError).status).toBe(INVALID_RESPONSE_STATUS);
  });

  it('announces opened-pack deliveries signalled in a response header', () => {
    setUp();
    let announcements = 0;
    http.openedPackAnnounced$.subscribe(() => announcements++);

    http.getText('/collection/default').subscribe();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/collection/default').flush('<collection/>');
    expect(announcements).toBe(0);

    http.getText('/collection/default').subscribe();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/collection/default')
      .flush('<collection/>', { headers: { [OPENED_PACK_HEADER]: 'true' } });
    expect(announcements).toBe(1);
  });
});
