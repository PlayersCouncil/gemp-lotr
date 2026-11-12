/**
 * The decklist text clean-up, before the
 * server recognises the cards (GET /collection/import/).
 */

/** Drops "(...)" and "[...]" notes, repeatedly, keeping the rest of the line. */
export function removeNotes(line: string): string {
  let processed = line;
  let start = line.indexOf('(');
  let end = line.indexOf(')', start);
  if (start < 0 && end < 0) {
    start = line.indexOf('[');
    end = line.indexOf(']', start);
  }
  if (start > 0) {
    processed = line.slice(0, start);
    if (end > 0) {
      processed += line.slice(end + 1);
    }
  } else if (end > 0) {
    processed = line.slice(end + 1);
  }
  if (/[()[\]]/.test(processed) && processed !== line) {
    return removeNotes(processed);
  }
  return processed;
}

/**
 * The decklist as the server expects it: one entry per non-empty line, lower case, without notes, bullets or the
 * "start"/"map:"/"ring-bearer:"/"ring:" labels, each followed by "~".
 */
export function formatDecklist(text: string): string {
  return text
    .split('\n')
    .filter((line) => line !== '')
    .map(
      (line) =>
        removeNotes(line)
          .toLowerCase()
          .replace(/[*•]/g, '')
          .replace(/starting|start|map:|ring-bearer:|ring:/g, '')
          .trim() + '~',
    )
    .join('');
}
