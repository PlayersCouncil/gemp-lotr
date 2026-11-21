import { catchError, map, Observable, of } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { RESULT_OK, statusMessage } from './hall-tables-messages';
import { parseTableCallResult } from './hall-table-parsers';
import { TableCallResult } from './hall-table-models';
import { asText } from '../json-values';

/**
 * One reading of every Play / Join / queue / tournament call's outcome (TableFlow.respond): the answer on HTTP 200 as
 * TableFlow.readResponse reads it, and any HTTP failure as a refusal with TableFlow.errorMap's sentence. The result
 * never fails.
 */
export function asTableCall(call$: Observable<string>): Observable<TableCallResult> {
  return call$.pipe(
    map(parseTableCallResult),
    catchError((error: unknown) => of(tableCallFailure(ApiError.from(error)))),
  );
}

/** A JSON answer (TableFlow.readResponse): {"error": "..."} is a refusal, {"response": "..."} or anything else OK. */
export function jsonCallResult(json: unknown): TableCallResult {
  if (json !== null && typeof json === 'object') {
    const body = json as Record<string, unknown>;
    if (body['error'] !== null && body['error'] !== undefined) {
      return { ok: false, message: asText(body['error']) };
    }
    if (body['response'] !== null && body['response'] !== undefined) {
      return { ok: true, message: asText(body['response']) };
    }
  }
  if (typeof json === 'string' && json !== '') {
    return { ok: true, message: json };
  }
  return { ok: true, message: RESULT_OK };
}

/** TableFlow.errorMap / errorDetail: a 400's JSON {"error"} or "message" header, else the status's sentence. */
export function tableCallFailure(error: ApiError): TableCallResult {
  const status = error.status < 0 ? 0 : error.status;
  if (error.status === 400) {
    const detail = errorDetail(error);
    if (detail) {
      return { ok: false, message: detail, status };
    }
  }
  return { ok: false, message: statusMessage(status), status };
}

/** The message a failed answer carries (TableFlow.errorDetail): a JSON body {"error": "..."}, else the "message" header. */
export function errorDetail(error: ApiError): string | null {
  const cause = error.cause;
  if (cause === null || typeof cause !== 'object') {
    return null;
  }
  const response = cause as { error?: unknown; headers?: { get(name: string): string | null } };
  let body = response.error;
  if (typeof body === 'string') {
    try {
      body = JSON.parse(body);
    } catch {
      body = null;
    }
  }
  if (body !== null && typeof body === 'object' && 'error' in body && body.error !== null && body.error !== undefined) {
    return asText(body.error);
  }
  const header = response.headers?.get('message');
  return header === undefined || header === null || header === '' ? null : header;
}
