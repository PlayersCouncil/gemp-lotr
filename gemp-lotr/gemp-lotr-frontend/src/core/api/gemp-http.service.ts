import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { SERVER_URL } from '../platform/app-paths';
import { ApiError } from './api-error';
import { FormData, toFormParams } from './form-codec';
import { BYPASS_CACHE, SEND_PARTICIPANT } from './gemp-interceptors';
import { parseXml } from './xml';

export interface RequestOptions {
  /** Send the session's participantId (default true; the legacy client sent it with nearly every call). */
  participant?: boolean;
  /** GET only: add a cache-busting parameter (default true). */
  noCache?: boolean;
}

function contextOf(options: RequestOptions): HttpContext {
  return new HttpContext()
    .set(SEND_PARTICIPANT, options.participant !== false)
    .set(BYPASS_CACHE, options.noCache !== false);
}

function toXml(text: string): Document {
  try {
    return parseXml(text);
  } catch (error) {
    throw ApiError.from(error);
  }
}

function toJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch (error) {
    throw ApiError.from(error);
  }
}

/**
 * The Gemp server's request formats: form-encoded parameters (strictly encoded), text, XML or JSON answers. What every
 * call needs (participant, no caching, ApiError, the opened-pack header) is in the interceptors of provideGempHttp.
 */
@Injectable({ providedIn: 'root' })
export class GempHttp {
  private readonly http = inject(HttpClient);

  getText(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<string> {
    return this.http.get(SERVER_URL + path, {
      params: toFormParams(data),
      responseType: 'text',
      context: contextOf(options),
    });
  }

  /** POSTs the data form-encoded (HttpParams bodies are sent as application/x-www-form-urlencoded). */
  postText(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<string> {
    return this.http.post(SERVER_URL + path, toFormParams(data), {
      responseType: 'text',
      context: contextOf(options),
    });
  }

  getXml(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<Document> {
    return this.getText(path, data, options).pipe(map(toXml));
  }

  postXml(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<Document> {
    return this.postText(path, data, options).pipe(map(toXml));
  }

  postJson<T>(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<T> {
    return this.postText(path, data, options).pipe(map((text) => toJson(text) as T));
  }
}
