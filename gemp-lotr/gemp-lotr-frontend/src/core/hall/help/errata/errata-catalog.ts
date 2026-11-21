import { foldText } from '../format-definitions';
import {
  ErrataCatalog,
  ErrataCounts,
  ErrataEntry,
  ErrataFormat,
  ErrataKind,
  ErrataSide,
  ErrataStat,
} from './errata-models';

/** Reading GET /hall/errata/json (ErrataCatalog.build) into the page's models, with PcErrata.prepare's fields. */

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function text(value: unknown): string | null {
  if (typeof value === 'string') {
    return value;
  }
  return typeof value === 'number' ? String(value) : null;
}

function count(value: unknown): number {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0;
}

/** parseInt(x, 10) || 0, as prepare did. */
function integer(value: unknown): number {
  return parseInt(text(value) ?? '', 10) || 0;
}

function kindOf(value: unknown): ErrataKind {
  return value === 'playtest' || value === 'revision' ? value : 'errata';
}

function stat(value: unknown): ErrataStat {
  if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'string') {
    return value;
  }
  return null;
}

function parseSide(value: unknown): ErrataSide | null {
  if (value === null || typeof value !== 'object' || Array.isArray(value)) {
    return null;
  }
  const json = value as Json;
  const stats: Record<string, ErrataStat> = {};
  for (const [key, entry] of Object.entries(object(json['stats']))) {
    stats[key] = stat(entry);
  }
  return {
    name: text(json['name']),
    type: text(json['type']),
    stats,
    gametext: text(json['gametext']) ?? '',
  };
}

/**
 * The collector's info as printed on the card ("1U29"); the V-sets' "V3_19" (rarity still unset) as "V3 19"; without
 * any, the set and card number ("101 5"). PcErrata.collectorLabel.
 */
export function errataCollectorLabel(collInfo: string | null, set: string, cardNum: number): string {
  const info = collInfo?.trim() ?? '';
  return info ? info.replace(/_+/g, ' ') : `${set} ${cardNum}`;
}

function parseEntry(value: unknown): ErrataEntry | null {
  const json = object(value);
  const id = text(json['id']);
  if (id === null || id === '') {
    return null;
  }
  const set = text(json['set']) ?? '';
  const cardNum = integer(json['cardNum']);
  const before = parseSide(json['before']);
  let name = text(json['name']) ?? id;
  // many errata files spell names without the accents ("Lorien Elf" for "Lórien Elf"): show the printed name
  if (before?.name && foldText(before.name) === foldText(name)) {
    name = before.name;
  }
  const coll = errataCollectorLabel(text(json['collInfo']), set, cardNum);
  const formats = Array.isArray(json['formats'])
    ? json['formats'].filter((code): code is string => typeof code === 'string')
    : [];
  return {
    id,
    base: text(json['base']) ?? id,
    kind: kindOf(json['kind']),
    name,
    set,
    setNum: integer(set),
    setName: text(json['setName']),
    cardNum,
    coll,
    culture: text(json['culture']),
    cultureCode: text(json['cultureCode']),
    side: text(json['side']),
    type: text(json['type']),
    revision: count(json['revision']),
    recent: json['recent'] === true,
    formats,
    after: parseSide(json['after']),
    before,
    searchName: foldText(name),
    searchColl: foldText(coll).replace(/\s+/g, ''),
  };
}

function parseFormat(value: unknown): ErrataFormat | null {
  const json = object(value);
  const code = text(json['code']);
  if (code === null || code === '') {
    return null;
  }
  return { code, name: text(json['name']) ?? code, count: count(json['count']) };
}

function parseCounts(value: unknown, entries: readonly ErrataEntry[]): ErrataCounts {
  const json = object(value);
  return {
    total: typeof json['total'] === 'number' ? json['total'] : entries.length,
    errata: count(json['errata']),
    playtest: count(json['playtest']),
    revision: count(json['revision']),
    recent: count(json['recent']),
  };
}

/** The answer of GET /hall/errata/json; anything missing reads as empty (setData's `json || {}`). */
export function parseErrataCatalog(json: unknown): ErrataCatalog {
  const root = object(json);
  const entries = Array.isArray(root['entries'])
    ? root['entries'].map(parseEntry).filter((entry): entry is ErrataEntry => entry !== null)
    : [];
  const formats = Array.isArray(root['formats'])
    ? root['formats'].map(parseFormat).filter((format): format is ErrataFormat => format !== null)
    : [];
  return { entries, formats, counts: parseCounts(root['counts'], entries) };
}
