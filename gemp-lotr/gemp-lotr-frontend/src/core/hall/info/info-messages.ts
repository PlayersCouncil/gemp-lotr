/** The wording of Server Info (includes/info.html, statsUi.js, patchNotesUi.js), as the legacy hall has it. */

// ---- the sub-tabs (info.html #info-tabs) ----
export const STATS_TAB = 'Server Stats';
export const PATCH_NOTES_TAB = 'Patch Notes';
export const CONTRIBUTE_TAB = 'Contribute';
export const ABOUT_TAB = 'About';

// ---- Server Stats ----
export const STATS_TIMEZONE_NOTE = 'All dates are server time (UTC).';
export const START_DATE_LABEL = 'Start date:';
export const PERIOD_LABEL = 'period:';
export const DISPLAY_STATS = 'Display Stats';
export const STATS_FOR = 'Stats for';
export const ACTIVE_PLAYERS = 'Active players:';
export const GAMES_COUNT = 'All games count:';
export const BOT_GAMES_COUNT = 'Bot games count:';
export const BOT_GAMES_NOTE = '(Not counted in the below stats)';
export const COMPETITIVE_CAPTION = 'Competitive Games by Format';
export const CASUAL_CAPTION = 'Casual Games by Format';
export const NO_COMPETITIVE_GAMES = 'No competitive games in this period.';
export const NO_CASUAL_GAMES = 'No casual games in this period.';
/** The answer to a start date the server can't read (400). */
export const INVALID_PARAMETER = 'Invalid parameter entered';
export const FORMAT_COLUMN = 'Format name';
export const COUNT_COLUMN = '# of games';
export const shareColumn = (group: string) => `% of ${group}`;
export const OF_TOTAL_COLUMN = '% of total';

// ---- Patch Notes ----
export const PATCH_NOTES_HEADING = 'Patch Notes';
export const FILTERS_LABEL = 'Show';
export const ALL_FILTER = 'All';
export const PATCH_NOTES_LOADING = 'Loading patch notes…';
export const TRY_AGAIN = 'Try again';
export const NEWER_UPDATES = '‹ Newer updates';
export const OLDER_UPDATES = 'Older updates ›';
export const OLDER_MONTH_TITLE = 'Older month with updates';
export const NEWER_MONTH_TITLE = 'Newer month with updates';
export const MONTHS_LOADING = 'Loading months…';
export const RETRY = 'Retry';
export const NO_PATCH_NOTES = 'There are no patch notes yet.';
export const NO_OLDER_UPDATES = 'There are no older updates.';
export const SHARE_LABEL = 'Share';
export const LIGHTBOX_CLOSE = 'Close';

export const noTaggedUpdates = (tag: string) => `No updates are tagged “${tag}” yet.`;
export const missingUpdate = (slug: string) => `There is no update named “${slug}”. Here are the newest ones.`;
export const showingAllUpdates = (tag: string) => `Showing all updates: that one is not tagged “${tag}”.`;
/** " (the server answered 503)" / " (no answer from the server)", after what failed. */
export const serverAnswered = (status: number) =>
  status > 0 ? `(the server answered ${status})` : '(no answer from the server)';
export const patchNotesFailed = (status: number) => `The patch notes could not be loaded ${serverAnswered(status)}. `;
export const monthsFailed = (reason: string) => `Could not load the list of months. ${reason}`;
/** "Updates 6–7 of 7", or "7 updates" past the end. */
export const updatesRange = (first: number, last: number, total: number, shown: number) =>
  shown > 0 ? `Updates ${first}–${last} of ${total}` : `${total} updates`;
export const noteShareLabel = (announcement: boolean, date: string) =>
  `Copy a share link to the ${announcement ? 'announcement' : 'update'} of ${date}`;
export const lightboxLabel = (alt: string) => (alt ? `Screenshot: ${alt}` : 'Screenshot');
