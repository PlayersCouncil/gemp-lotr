import {
  HttpContextToken,
  HttpInterceptorFn,
  HttpParams,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, tap, throwError } from 'rxjs';
import { SERVER_URL } from '../platform/app-paths';
import { SessionService } from '../session/session.service';
import { ApiError } from './api-error';
import { OpenedPackAnnouncements } from './opened-pack-announcements';

/** Response header the server sets when opened-pack contents are waiting to be shown. */
export const OPENED_PACK_HEADER = 'Delivery-Service-Opened-Pack';

/** Send the session's participantId with this request (the default). */
export const SEND_PARTICIPANT = new HttpContextToken<boolean>(() => true);
/** GET only: add a cache-busting parameter to this request (the default). */
export const BYPASS_CACHE = new HttpContextToken<boolean>(() => true);

const toServer = (url: string) => url.startsWith(SERVER_URL);

/** Acting for another player (admins, `?participantId=`): every call says so. */
const participantInterceptor: HttpInterceptorFn = (request, next) => {
  const participantId = inject(SessionService).participantId;
  if (!toServer(request.url) || participantId === null || !request.context.get(SEND_PARTICIPANT)) {
    return next(request);
  }
  if (request.method === 'GET') {
    return next(request.clone({ params: request.params.set('participantId', participantId) }));
  }
  return next(request.clone({ body: (request.body as HttpParams).set('participantId', participantId) }));
};

const noCacheInterceptor: HttpInterceptorFn = (request, next) =>
  toServer(request.url) && request.method === 'GET' && request.context.get(BYPASS_CACHE)
    ? next(request.clone({ params: request.params.set('_', String(Date.now())) }))
    : next(request);

const openedPackInterceptor: HttpInterceptorFn = (request, next) => {
  const announcements = inject(OpenedPackAnnouncements);
  return next(request).pipe(
    tap((event) => {
      if (toServer(request.url) && event instanceof HttpResponse && event.headers.get(OPENED_PACK_HEADER) === 'true') {
        announcements.announce();
      }
    }),
  );
};

const apiErrorInterceptor: HttpInterceptorFn = (request, next) =>
  toServer(request.url)
    ? next(request).pipe(catchError((error: unknown) => throwError(() => ApiError.from(error))))
    : next(request);

/** HttpClient, with what every call to the Gemp server needs. */
export function provideGempHttp() {
  return provideHttpClient(
    withInterceptors([participantInterceptor, noCacheInterceptor, openedPackInterceptor, apiErrorInterceptor]),
  );
}
