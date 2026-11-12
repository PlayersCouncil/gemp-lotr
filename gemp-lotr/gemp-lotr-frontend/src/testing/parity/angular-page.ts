/**
 * Test-only: the Angular deck builder page, whole, against a FakeGempServer, on vitest's fake timers. The counterpart
 * of legacy-page.ts, for comparing the two (src/parity).
 */
import {
  HttpBackend,
  HttpErrorResponse,
  HttpEvent,
  HttpHeaders,
  HttpParams,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { provideGempHttp } from '../../core/api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { DeckbuilderPageComponent } from '../../ui/deckbuilder/deckbuilder-page.component';
import { FakeNotifier, provideFakeNotifier } from '../fake-notifier';
import { FakeGempServer } from './fake-gemp-server';
import { DialogAnswer, SERVER_DELAY_MS } from './legacy-page';

const SERVER = '/gemp-lotr-server';

function paramsOf(params: HttpParams | null): Record<string, string> {
  return params ? Object.fromEntries(new URLSearchParams(params.toString())) : {};
}

/** Answers HttpClient from the fake server, after SERVER_DELAY_MS. */
class FakeServerBackend implements HttpBackend {
  constructor(private readonly server: FakeGempServer) {}

  handle(request: HttpRequest<unknown>): Observable<HttpEvent<unknown>> {
    return new Observable((subscriber) => {
      const timer = setTimeout(() => {
        const body = request.body instanceof HttpParams ? request.body : null;
        const answer = this.server.answer({
          method: request.method,
          path: request.url.replace(SERVER, ''),
          params: { ...paramsOf(request.params), ...paramsOf(body) },
        });
        const headers = new HttpHeaders({ 'Content-Type': answer.contentType });
        if (answer.status >= 400) {
          subscriber.error(new HttpErrorResponse({ status: answer.status, url: request.url, headers }));
        } else {
          subscriber.next(new HttpResponse({ status: answer.status, body: answer.body, url: request.url, headers }));
          subscriber.complete();
        }
      }, SERVER_DELAY_MS);
      return () => clearTimeout(timer);
    });
  }
}

export interface AngularPage {
  readonly fixture: ComponentFixture<DeckbuilderPageComponent>;
  readonly element: HTMLElement;
  /** "alert: ...", "confirm: ...", "prompt: ..." and "new tab: <address>" in the order the page showed them. */
  readonly dialogs: string[];
  readonly answers: DialogAnswer[];
  /** Lets `ms` pass: timers run, the server's answers arrive and the page is drawn again. */
  advance(ms: number): Promise<void>;
  close(): void;
}

/** Opens the page; it has started when this resolves. Needs `vi.useFakeTimers()`. */
export async function openAngularPage(server: FakeGempServer, query = ''): Promise<AngularPage> {
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      { provide: HttpBackend, useValue: new FakeServerBackend(server) },
      provideFakeNotifier(),
      { provide: PAGE_QUERY_STRING, useValue: query },
    ],
  });
  const fixture = TestBed.createComponent(DeckbuilderPageComponent);
  const notifier = TestBed.inject(FakeNotifier);
  const newTabs = vi.spyOn(window, 'open').mockImplementation((url) => {
    notifier.dialogs.push(`new tab: ${String(url)}`);
    return null;
  });
  const page: AngularPage = {
    fixture,
    element: fixture.nativeElement as HTMLElement,
    dialogs: notifier.dialogs,
    answers: notifier.answers,
    advance: async (ms) => {
      await vi.advanceTimersByTimeAsync(ms);
      fixture.detectChanges();
      await vi.advanceTimersByTimeAsync(0);
    },
    close: () => {
      fixture.destroy();
      newTabs.mockRestore();
    },
  };
  fixture.detectChanges();
  // The first requests and their answers, the collection's card images (never loaded here: the page stops waiting)
  await page.advance(5000);
  return page;
}
