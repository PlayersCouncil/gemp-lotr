/**
 * The server's word on a filter typed for a puzzle's pool or a slot's bound (`GET /scenarios/author/bound`, ef-notes.md
 * 4: `{ok, why, total}`), as the filter field shows it: how many cards it finds, or why it will not do. Read
 * defensively: an answer not understood says nothing. Pure.
 */
import { FilterVerdict } from './puzzle-settings';

export function boundVerdict(answer: unknown): FilterVerdict {
  if (answer === null || typeof answer !== 'object') return { kind: 'none' };
  const json = answer as Readonly<Record<string, unknown>>;
  if (json['ok'] === false) {
    return { kind: 'refused', why: typeof json['why'] === 'string' && json['why'] !== '' ? json['why'] : 'Refused' };
  }
  const total = json['total'];
  return json['ok'] === true && typeof total === 'number' && Number.isFinite(total)
    ? { kind: 'count', total }
    : { kind: 'none' };
}

/** How long typing rests before what is typed is checked (ms). */
export const BOUND_TYPING_MS = 400;
