import { foldText } from '../format-definitions';
import { NAME_STAT, NEW_TEXT, OLD_TEXT, ONLY_TEXT, SAME_HINT, SAME_TEXT, TYPE_STAT } from './errata-messages';
import { ErrataEntry, ErrataSide, ErrataStat } from './errata-models';

/** What changed between a card's original and its errata (pcErrataUi.js: PcErrata's diff, and PcErrataUI.note). */

/** A run of text: unchanged ('='), only in the original ('-') or only in the errata ('+'). */
export interface DiffOp {
  readonly op: '=' | '-' | '+';
  readonly text: string;
}

/** The stats in the order the readout lists them; any other stat follows (PcErrata.STAT_ORDER). */
export const STAT_ORDER: readonly string[] = ['Twilight', 'Strength', 'Vitality', 'Resistance', 'Site', 'Unique'];

const ENTITIES: Readonly<Record<string, string>> = {
  nbsp: ' ',
  amp: '&',
  lt: '<',
  gt: '>',
  quot: '"',
  '#39': "'",
};

/**
 * A game text as words, spaces and single marks (PcErrata.tokens): tags taken out, as before. Unlike the legacy page,
 * the few entities the server's text can hold (`&nbsp;` from a `_` in a card file) read as their characters, not as
 * "&nbsp;".
 */
export function tokens(text: string | null | undefined): string[] {
  const plain = (text ?? '')
    .replace(/<[^>]*>/g, '')
    .replace(/&(nbsp|amp|lt|gt|quot|#39);/g, (_, name: string) => ENTITIES[name] ?? '');
  return plain.match(/\s+|[A-Za-z0-9À-ɏ'’]+|[^\sA-Za-z0-9À-ɏ'’]/g) ?? [];
}

/** The word diff of two game texts, runs merged (PcErrata.diff). */
export function diffTexts(before: string | null | undefined, after: string | null | undefined): DiffOp[] {
  const a = tokens(before);
  const b = tokens(after);
  const n = a.length;
  const m = b.length;
  // lcs[i][j]: the longest common run of a[i..] and b[j..]
  const lcs: number[][] = [];
  for (let i = 0; i <= n; i++) {
    lcs.push(new Array<number>(m + 1).fill(0));
  }
  const at = (i: number, j: number): number => lcs[i]?.[j] ?? 0;
  for (let i = n - 1; i >= 0; i--) {
    const row = lcs[i] ?? [];
    for (let j = m - 1; j >= 0; j--) {
      row[j] = a[i] === b[j] ? at(i + 1, j + 1) + 1 : Math.max(at(i + 1, j), at(i, j + 1));
    }
  }
  const ops: { op: DiffOp['op']; text: string }[] = [];
  const push = (op: DiffOp['op'], text: string): void => {
    const last = ops[ops.length - 1];
    if (last?.op === op) {
      last.text += text;
    } else {
      ops.push({ op, text });
    }
  };
  let i = 0;
  let j = 0;
  while (i < n && j < m) {
    if (a[i] === b[j]) {
      push('=', a[i] ?? '');
      i++;
      j++;
    } else if (at(i + 1, j) >= at(i, j + 1)) {
      push('-', a[i++] ?? '');
    } else {
      push('+', b[j++] ?? '');
    }
  }
  while (i < n) {
    push('-', a[i++] ?? '');
  }
  while (j < m) {
    push('+', b[j++] ?? '');
  }
  // Changes separated only by spaces read as one change: "~~a~~ b ~~c~~ d" becomes "~~a c~~ b d". The spaces go into
  // both sides, so the removals still spell the old text and the insertions the new one.
  const merged: DiffOp[] = [];
  let k = 0;
  while (k < ops.length) {
    const first = ops[k];
    if (first?.op === '=') {
      merged.push(first);
      k++;
      continue;
    }
    let removed = '';
    let inserted = '';
    while (k < ops.length) {
      const op = ops[k];
      const next = ops[k + 1];
      if (op === undefined) {
        break;
      }
      if (op.op === '-') {
        removed += op.text;
      } else if (op.op === '+') {
        inserted += op.text;
      } else if (/^\s+$/.test(op.text) && next !== undefined && next.op !== '=') {
        removed += op.text;
        inserted += op.text;
      } else {
        break;
      }
      k++;
    }
    if (removed) {
      merged.push({ op: '-', text: removed });
    }
    if (inserted) {
      merged.push({ op: '+', text: inserted });
    }
  }
  return merged;
}

/** The diff as two texts: old = unchanged + removed, new = unchanged + added (PcErrata.sides). */
export function diffSides(ops: readonly DiffOp[]): { readonly old: DiffOp[]; readonly new: DiffOp[] } {
  return { old: ops.filter((op) => op.op !== '+'), new: ops.filter((op) => op.op !== '-') };
}

/** Ops as one plain run of text (PcErrata.plain). */
export function plainText(ops: readonly DiffOp[]): string {
  return ops.map((op) => op.text).join('');
}

/** A stat as the readout writes it (PcErrata.statText): "—" for none, "yes" / "no". */
export function statText(value: ErrataStat | undefined): string {
  if (value === undefined || value === null) {
    return '—';
  }
  if (value === true) {
    return 'yes';
  }
  if (value === false) {
    return 'no';
  }
  return String(value);
}

export interface StatChange {
  readonly name: string;
  readonly from: string;
  readonly to: string;
}

/** The stats that differ, after the name and the card type if the errata changed them (PcErrata.statChanges). */
export function statChanges(before: ErrataSide | null, after: ErrataSide | null): StatChange[] {
  const changes: StatChange[] = [];
  if (!before || !after) {
    return changes;
  }
  if (before.name && after.name && foldText(before.name) !== foldText(after.name)) {
    changes.push({ name: NAME_STAT, from: before.name, to: after.name });
  }
  // (Still Draws Breath: Event -> Condition)
  if (before.type && after.type && before.type !== after.type) {
    changes.push({ name: TYPE_STAT, from: before.type, to: after.type });
  }
  const b = before.stats;
  const a = after.stats;
  const keys = [...STAT_ORDER];
  for (const stats of [b, a]) {
    for (const key of Object.keys(stats)) {
      if (!keys.includes(key)) {
        keys.push(key);
      }
    }
  }
  for (const key of keys) {
    if (!(key in b) && !(key in a)) {
      continue;
    }
    if (statText(b[key]) !== statText(a[key])) {
      changes.push({ name: key, from: statText(b[key]), to: statText(a[key]) });
    }
  }
  return changes;
}

/** One game text of the readout: its label, and its runs (only a 'new' text marks any: its additions). */
export interface ReadoutText {
  readonly side: 'old' | 'new' | 'same';
  readonly label: string;
  readonly ops: readonly DiffOp[];
}

/** The readout of what changed under the card (PcErrataUI.note). */
export interface ErrataReadout {
  readonly stats: readonly StatChange[];
  readonly texts: readonly ReadoutText[];
  /** Said when nothing but the image changed. */
  readonly hint: string | null;
}

/**
 * What changed: the stats (and type) one per line, then the game text as Old (plain) and New (additions marked); the
 * text alone when it is the same, or when there is no original to compare with.
 */
export function errataReadout(entry: ErrataEntry): ErrataReadout {
  const stats = statChanges(entry.before, entry.after);
  const after = entry.after?.gametext ?? '';
  if (!entry.before) {
    return {
      stats,
      texts: [{ side: 'same', label: ONLY_TEXT, ops: [{ op: '=', text: tokens(after).join('') }] }],
      hint: null,
    };
  }
  const ops = diffTexts(entry.before.gametext, after);
  if (ops.some((op) => op.op !== '=')) {
    const sides = diffSides(ops);
    return {
      stats,
      texts: [
        { side: 'old', label: OLD_TEXT, ops: [{ op: '=', text: plainText(sides.old) }] },
        { side: 'new', label: NEW_TEXT, ops: sides.new },
      ],
      hint: null,
    };
  }
  return {
    stats,
    texts: [{ side: 'same', label: SAME_TEXT, ops }],
    hint: stats.length ? null : SAME_HINT,
  };
}
