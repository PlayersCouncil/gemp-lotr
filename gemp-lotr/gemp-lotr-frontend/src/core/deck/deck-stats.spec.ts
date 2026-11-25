import fs from 'node:fs';
import path from 'node:path';
import { EMPTY_STATS, parseDeckStats, StatsLine } from './deck-stats';

/** A line as "text" pieces: **label**, [error:...], [valid:...], [warning:...]. */
function show(line: StatsLine): string {
  return line.segments
    .map((s) => {
      const text = s.strong ? `**${s.text}**` : s.text;
      return s.tone ? `[${s.tone}:${text}]` : text;
    })
    .join('');
}

describe('parseDeckStats', () => {
  it("reads the server's stats (the stubbed answer): labels, the verdict's tone, one line per <br>", () => {
    const html = fs.readFileSync(path.resolve('src/testing/server-responses/stats.html'), 'utf8');
    expect(parseDeckStats(html).lines.map(show)).toEqual([
      '**Free People**: 4, **Shadow**: 5',
      '**Fellowship Block**: [error:Card assigned as Ring-bearer cannot bear the ring]',
      '[error:Deck contains below minimum number of cards: 9<60.]',
      '[error:Deck contains different number of Shadow and Free peoples cards.]',
      // <br><br> left a blank line
      '',
      '[error:Deck has multiple of the same site number: 2]',
      // the <br> closing the messages, then the fragment's own: one blank line, as the legacy page showed
      '',
    ]);
  });

  it('reads a valid deck, and one valid only with errata applied', () => {
    expect(
      parseDeckStats(
        "<b>Free People</b>: 30, <b>Shadow</b>: 30<br/><b>Fellowship Block</b>: <font color='green'>Valid</font><br/>",
      ).lines.map(show),
    ).toEqual(['**Free People**: 30, **Shadow**: 30', '**Fellowship Block**: [valid:Valid]']);
    expect(
      parseDeckStats(
        "<b>PC</b>: <font color='green'>Valid</font> <font color='yellow'>(with errata automatically applied)</font><br/>" +
          "<font color='yellow'>Card X is not legal</font><br/>",
      ).lines.map(show),
    ).toEqual(['**PC**: [valid:Valid] [warning:(with errata automatically applied)]', '[warning:Card X is not legal]']);
  });

  it('keeps anything that is not one of its tags as text, and decodes entities', () => {
    expect(parseDeckStats('a <i>b</i> &amp; c&lt;d &#9679; <script>x</script>').lines.map(show)).toEqual([
      'a <i>b</i> & c<d ● <script>x</script>',
    ]);
  });

  it('marks a label inside a coloured part with both', () => {
    const [line] = parseDeckStats("<font color='red'>x <b>y</b></font>").lines;
    expect(line.segments).toEqual([
      { text: 'x ', strong: false, tone: 'error' },
      { text: 'y', strong: true, tone: 'error' },
    ]);
  });

  it('reads a plain answer, and nothing as no stats', () => {
    expect(parseDeckStats('Invalid format: xyz').lines.map(show)).toEqual(['Invalid format: xyz']);
    expect(parseDeckStats('')).toBe(EMPTY_STATS);
  });
});
