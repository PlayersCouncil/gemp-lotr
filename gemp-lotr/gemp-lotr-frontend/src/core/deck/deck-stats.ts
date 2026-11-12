/**
 * The deck stats (the server's /deck/stats answer) as data. The server writes a small HTML fragment
 * (DeckRequestHandler.getDeckStats): `<b>` labels, `<font color='red|green|yellow'>` around the format's verdict and
 * its messages, and `<br>` between lines. The messages themselves are not escaped (e.g. "9<60."), so this reads only
 * those few tags and keeps everything else as text; the page never renders the server's markup.
 */

/** What a part of a line says about the deck: the legacy page coloured them red, green and yellow. */
export type StatsTone = 'error' | 'valid' | 'warning';

export interface StatsSegment {
  text: string;
  /** A label (bold on the legacy page), e.g. "Free People" or the format's name. */
  strong: boolean;
  tone: StatsTone | null;
}

/** One line of the stats; an empty line (no segments) was a blank line on the legacy page. */
export interface StatsLine {
  segments: StatsSegment[];
}

export interface DeckStats {
  lines: StatsLine[];
}

export const EMPTY_STATS: DeckStats = { lines: [] };

const TONES: Record<string, StatsTone> = { red: 'error', green: 'valid', yellow: 'warning' };

/** The tags the server writes; any other `<` is text. */
const TAG = /^<(\/?)(b|font|br)\b([^<>]*?)\/?>/i;

const ENTITIES: Record<string, string> = { amp: '&', lt: '<', gt: '>', quot: '"', apos: "'", nbsp: ' ' };

function decodeEntities(text: string): string {
  return text.replace(/&(#x[0-9a-f]+|#\d+|[a-z]+);/gi, (entity, name: string) => {
    if (name.startsWith('#x') || name.startsWith('#X')) {
      return String.fromCodePoint(parseInt(name.slice(2), 16));
    }
    if (name.startsWith('#')) {
      return String.fromCodePoint(parseInt(name.slice(1), 10));
    }
    return ENTITIES[name.toLowerCase()] ?? entity;
  });
}

/** The innermost coloured `<font>` decides. */
function innerTone(tones: readonly (StatsTone | null)[]): StatsTone | null {
  for (let i = tones.length - 1; i >= 0; i--) {
    const tone = tones[i];
    if (tone) {
      return tone;
    }
  }
  return null;
}

function toneOf(attributes: string): StatsTone | null {
  const color = /color\s*=\s*['"]?([a-z]+)/i.exec(attributes)?.[1]?.toLowerCase();
  return color ? (TONES[color] ?? null) : null;
}

/**
 * Reads the stats fragment into lines. As in HTML, a `<br>` ends a line, so the line break that ends the fragment
 * doesn't start an empty line; whitespace is kept as the server wrote it.
 */
export function parseDeckStats(html: string): DeckStats {
  const lines: StatsLine[] = [{ segments: [] }];
  let strong = 0;
  const tones: (StatsTone | null)[] = [];
  let text = '';

  const flush = () => {
    if (text) {
      lines.at(-1)?.segments.push({
        text: decodeEntities(text),
        strong: strong > 0,
        tone: innerTone(tones),
      });
      text = '';
    }
  };

  let rest = html;
  while (rest) {
    const tag = rest.startsWith('<') ? TAG.exec(rest) : null;
    if (!tag) {
      const next = rest.indexOf('<', 1);
      const end = next === -1 ? rest.length : next;
      text += rest.slice(0, end);
      rest = rest.slice(end);
      continue;
    }
    flush();
    const whole = tag[0];
    const [, closing, name = '', attributes = ''] = tag;
    switch (name.toLowerCase()) {
      case 'br':
        lines.push({ segments: [] });
        break;
      case 'b':
        strong = Math.max(0, strong + (closing ? -1 : 1));
        break;
      case 'font':
        if (closing) {
          tones.pop();
        } else {
          tones.push(toneOf(attributes));
        }
        break;
    }
    rest = rest.slice(whole.length);
  }
  flush();
  if (lines.length > 1 && lines.at(-1)?.segments.length === 0) {
    lines.pop();
  }
  return lines.length === 1 && lines[0]?.segments.length === 0 ? EMPTY_STATS : { lines };
}
