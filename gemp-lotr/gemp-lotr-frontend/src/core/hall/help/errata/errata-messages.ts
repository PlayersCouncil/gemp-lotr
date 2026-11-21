/** The wording of Help › PC Errata (includes/help/pc-errata.html, js/gemp-022/pcErrataUi.js), as the legacy hall has it. */

export const ERRATA_TITLE = 'PC Errata';

// ---- loading (PcErrataUI.load) ----
/** What the "could not load" sentences name (neutralErrorMap). */
export const ERRATA_SUBJECT = 'the errata list';
export const ERRATA_LOADING = 'Loading the errata…';

/** The (i) text's first line (renderSummary). */
export function errataSummary(total: number, errata: number, revisions: number): string {
  return `${total} cards: ${errata} PC errata of Decipher cards${revisions ? ` and ${revisions} revised V-set cards` : ''}.`;
}

// ---- the View chips (PcErrata.SHOWS) and the filters (renderFilters) ----
export const VIEW_GROUP_LABEL = 'View';
export const SHOW_ALL = 'All';
export const SHOW_RECENT = 'Latest';
export const TITLE_FIELD = 'Title';
export const TITLE_PLACEHOLDER = 'Title';
export const SET_FIELD = 'Set';
export const FORMAT_FIELD = 'Format';
export const ALL_SETS = 'All sets';
export const ALL_FORMATS = 'All formats';

/** "1 – The Fellowship of the Ring (12)": a set in the Set list. */
export function setOptionLabel(set: string, name: string | null, count: number): string {
  return `${set}${name ? ` – ${name}` : ''} (${count})`;
}

/** "PC-Movie Block (40)": a format in the Format list (and a link's value the lists leave out). */
export function countedLabel(label: string, count: number): string {
  return `${label} (${count})`;
}

// ---- the status (renderStatus) ----
export function statusText(shown: number, total: number): string {
  return shown === total ? `${total} cards` : `${shown} of ${total} cards`;
}

export const noErrataInFormat = (format: string) => `No PC errata are in effect in ${format}.`;
export const NO_MATCHES = 'No errata match these filters.';
/** Put before the status when a link names a card that has no errata (applyRoute). */
export const unknownCard = (card: string) => `Card ${card} has no PC errata. `;

// ---- the table (renderTable, row) ----
export const TABLE_LABEL = 'PC errata';
export const CARD_COLUMN = 'Card';
export const CULTURE_COLUMN = 'Culture';
export const TITLE_COLUMN = 'Title';
export const REVISIONS_COLUMN = '#';
export const CARD_COLUMN_TITLE = "The collector's number; sort by set and card number";
export const REVISIONS_COLUMN_TITLE = "How many times the Player's Council has revised the card; sort by it";
/** The other columns' tooltip: "Sort by " and the sort's name in lower case (PcErrata.SORTS). */
export const sortTitle = (sortName: string) => `Sort by ${sortName.toLowerCase()}`;
export const SORT_NAMES = {
  num: "Collector's number",
  culture: 'Culture',
  name: 'Title',
  rev: 'Number of PC revisions',
} as const;
export const SORT_ASCENDING_MARK = ' ▲';
export const SORT_DESCENDING_MARK = ' ▼';
export const NEW_BADGE = 'New';
export const NEW_BADGE_TITLE = 'In the latest batch of errata';
export const PLAYTEST_BADGE = 'playtest';

/** A row's card cell tooltip: "The Fellowship of the Ring, card 45" ("Set 101, card 5" without a set name). */
export function rowTitle(set: string, setName: string | null, cardNum: number): string {
  return `${setName === null || setName === '' ? `Set ${set}` : setName}, card ${cardNum}`;
}

// ---- the viewer (pc-errata.html .pc-errata-viewer, head, frame, note) ----
export const VIEWER_LABEL = 'Errata viewer';
export const VIEWER_ROLE = 'card viewer';
export const PREVIOUS_LABEL = 'Previous card';
export const PREVIOUS_TITLE = 'Previous card (↑ or k)';
export const NEXT_LABEL = 'Next card';
export const NEXT_TITLE = 'Next card (↓ or j)';
export const PREVIOUS_MARK = '▲';
export const NEXT_MARK = '▼';
/** "3 of 40" (updateCounter). */
export const counterText = (index: number, count: number) => (count ? `${index + 1} of ${count}` : '0 of 0');
export const FLIP_GROUP_LABEL = 'Which version the card shows';
export const ORIGINAL = 'Original';
export const ORIGINAL_TITLE = 'The card as Decipher printed it (O)';
export const PC_ERRATA = 'PC errata';
export const PC_ERRATA_TITLE = "The card as the Player's Council errata has it (O)";
export const ENLARGE = 'Enlarge';
export const CLOSE_ENLARGED = 'Close';
export const SHARE_LABEL = 'Share';
export const SHARE_ARIA_LABEL = 'Copy a share link to the errata on show';
export const EMPTY_HINT = 'Change the search, or pick another set, format or view.';
export const revisionBadge = (revision: number) => `PC revision ${revision}`;

/** The heading's "1R45 · Gandalf Companion" (head). */
export function frameMeta(coll: string, culture: string | null, type: string | null): string {
  const kind = [culture, type].filter((part) => !!part).join(' ');
  return `${coll}${kind ? ` · ${kind}` : ''}`;
}

/** Its tooltip: "The Fellowship of the Ring (set 1), card 45". */
export function frameMetaTitle(set: string, setName: string | null, cardNum: number): string {
  return `${setName ? `${setName} (set ${set})` : `Set ${set}`}, card ${cardNum}`;
}

export const CARD_TITLE = 'Click (or press O) to see the other version; right-click to zoom';
export const FIGURE_CARD_TITLE = 'Right-click to zoom';
export const NO_IMAGE = 'No image';
export const NO_ORIGINAL_IMAGE = 'No image of the original';
export const STAT_ARROW = '→';
export const STAT_ARROW_LABEL = 'to';
export const ENLARGED_ARROW = '→';
export const ENLARGED_ARROW_COLUMN = '↓';
export const OLD_TEXT = 'Old';
export const NEW_TEXT = 'New';
export const SAME_TEXT = 'Game text (unchanged)';
export const ONLY_TEXT = 'Game text';
export const SAME_HINT =
  'The game text and stats read the same; the change is elsewhere on the card (compare the images).';
export const NAME_STAT = 'Name';
export const TYPE_STAT = 'Type';

/** The card preview's title for each side (PcErrata.previewArgs). */
export const previewTitle = (name: string, original: boolean) => `${name}${original ? ' (original)' : ' (PC errata)'}`;
