import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { SERVER_URL } from '../platform/app-paths';
import { ApiError } from './api-error';
import { FormData, toFormParams } from './form-codec';
import { BYPASS_CACHE } from './gemp-interceptors';
import { parseXml } from './xml';

export interface RequestOptions {
  /** GET only: add a cache-busting parameter (default true). */
  noCache?: boolean;
}

/** A successful answer with its HTTP status (see GempHttp.postForStatus). */
export interface StatusResponse {
  status: number;
  body: string;
}

function contextOf(options: RequestOptions): HttpContext {
  return new HttpContext().set(BYPASS_CACHE, options.noCache !== false);
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
    return JSON.parse(text) as unknown;
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

  /**
   * POSTs like postText, but also gives the status of a successful answer, for the calls whose 2xx status means
   * something (POST /login answers 202 when the account's password was reset).
   */
  postForStatus(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<StatusResponse> {
    return this.http
      .post(SERVER_URL + path, toFormParams(data), {
        responseType: 'text',
        observe: 'response',
        context: contextOf(options),
      })
      .pipe(map((response) => ({ status: response.status, body: response.body ?? '' })));
  }

  getXml(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<Document> {
    return this.getText(path, data, options).pipe(map(toXml));
  }

  postXml(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<Document> {
    return this.postText(path, data, options).pipe(map(toXml));
  }

  getJson<T>(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<T> {
    return this.getText(path, data, options).pipe(map((text) => toJson(text) as T));
  }

  postJson<T>(path: string, data: FormData = {}, options: RequestOptions = {}): Observable<T> {
    return this.postText(path, data, options).pipe(map((text) => toJson(text) as T));
  }
}
