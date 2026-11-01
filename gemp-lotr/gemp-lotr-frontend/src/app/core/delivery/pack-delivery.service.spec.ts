import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { PackDelivery } from '../api/models';
import { GempHttp, OPENED_PACK_HEADER } from '../api/gemp-http.service';
import { PackDeliveryService } from './pack-delivery.service';

describe('PackDeliveryService', () => {
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const expectDeliveryRequest = () =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/delivery/packContents' && r.method === 'GET');

  it('fetches and emits deliveries when asked', () => {
    const service = TestBed.inject(PackDeliveryService);
    const received: PackDelivery[][] = [];
    service.deliveries$.subscribe((d) => received.push(d));

    service.fetch();
    expectDeliveryRequest().flush(readServerResponse('pack-delivery.xml'));

    expect(received).toHaveLength(1);
    expect(received[0][0].collectionName).toBe('permanent');
  });

  it('fetches when a response announces opened packs', () => {
    TestBed.inject(PackDeliveryService);
    TestBed.inject(GempHttp).getText('/collection/default').subscribe();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/collection/default')
      .flush('<collection/>', { headers: { [OPENED_PACK_HEADER]: 'true' } });
    expectDeliveryRequest().flush('<delivery/>');
  });

  it('ignores "nothing to deliver" (404) and empty deliveries', () => {
    const service = TestBed.inject(PackDeliveryService);
    let emitted = false;
    service.deliveries$.subscribe(() => (emitted = true));

    service.fetch();
    expectDeliveryRequest().flush('', { status: 404, statusText: 'Not Found' });
    service.fetch();
    expectDeliveryRequest().flush('<delivery/>');

    expect(emitted).toBe(false);
  });

  it('does not fetch anything until something uses it', () => {
    TestBed.inject(GempHttp).getText('/collection/default').subscribe();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/collection/default')
      .flush('<collection/>', { headers: { [OPENED_PACK_HEADER]: 'true' } });
    backend.expectNone((r) => r.url.includes('/delivery/'));
  });
});
