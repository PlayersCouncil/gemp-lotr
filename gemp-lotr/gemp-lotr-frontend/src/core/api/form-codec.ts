import { HttpParameterCodec, HttpParams } from '@angular/common/http';

/**
 * Encodes every reserved character, like jQuery does. Angular's default codec leaves '+' (and a few others) as-is,
 * which the server would decode as a space, corrupting deck notes or imported decklists that contain a '+'.
 */
class StrictFormCodec implements HttpParameterCodec {
  encodeKey(key: string): string {
    return encodeURIComponent(key);
  }

  encodeValue(value: string): string {
    return encodeURIComponent(value);
  }

  decodeKey(key: string): string {
    return decodeURIComponent(key);
  }

  decodeValue(value: string): string {
    return decodeURIComponent(value);
  }
}

type FormValue = string | number | boolean | null | undefined;
export type FormData = Record<string, FormValue>;

/** Builds strictly encoded params, leaving out null/undefined values. */
export function toFormParams(data: FormData): HttpParams {
  let params = new HttpParams({ encoder: new StrictFormCodec() });
  for (const [key, value] of Object.entries(data)) {
    if (value !== null && value !== undefined) {
      params = params.set(key, String(value));
    }
  }
  return params;
}
