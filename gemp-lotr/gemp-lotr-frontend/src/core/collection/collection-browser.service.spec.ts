import { provideDeckbuilderState } from '../deckbuilder-state';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { CollectionBrowser } from './collection-browser.service';

describe('CollectionBrowser', () => {
  let browser: CollectionBrowser;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    browser = TestBed.inject(CollectionBrowser);
    backend = TestBed.inject(HttpTestingController);
  });

  const lastRequest = () => backend.match((req) => req.url.includes('/collection/')).at(-1)!.request;

  it('uses a filter for the next load without loading', () => {
    browser.useFilter('sort:name');
    expect(backend.match((req) => req.url.includes('/collection/')).length).toBe(0);
    browser.load();
    expect(lastRequest().params.get('filter')).toBe('sort:name');
  });

  it('shows a new filter from the first page', () => {
    browser.start.set(24);
    browser.showFilter('sort:culture');
    const request = lastRequest();
    expect(request.params.get('filter')).toBe('sort:culture');
    expect(request.params.get('start')).toBe('0');
  });

  it('going to the first page loads nothing by itself', () => {
    browser.start.set(24);
    browser.goToFirstPage();
    expect(browser.start()).toBe(0);
    expect(backend.match((req) => req.url.includes('/collection/')).length).toBe(0);
  });
});
