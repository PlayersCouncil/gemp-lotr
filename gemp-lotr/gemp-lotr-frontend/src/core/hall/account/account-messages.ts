/** The My Account tab's wording, as the legacy tab has it (account.html and the files named on each part). */

import { firstText } from '../first-text';

// ---- the tab and its sub-tabs (account.html) ----

export const ACCOUNT_SUB_TABS = [
  { slug: 'overview', label: 'My Account' },
  { slug: 'history', label: 'My Game History' },
  { slug: 'stats', label: 'My Stats' },
  { slug: 'scenarios', label: 'My Scenarios' },
] as const;

// ---- My Account: the overview (account.html) ----

/** "My Account: <name>". */
export const ACCOUNT_HEADING = 'My Account: ';
export const MIGRATE_TROPHIES = 'Migrate Trophies to My Cards';
/** migrate(): the browser's confirm, as the legacy page asked it (two spaces before "Are"). */
export const MIGRATE_QUESTION =
  "This will permanently migrate all of your cards from 'Trophies' to 'My Cards' THIS CANNOT BE UNDONE, EVEN MANUALLY.  Are you sure you want to continue?";

export const IGNORES_HEADING = 'Ignored players';
export const IGNORES_HELP =
  'Tables with a player you ignore are hidden from you, and yours from them. (Exception: league events and ' +
  'tournaments cannot block other players.) After you refresh, their chat messages are also hidden. You can also ' +
  'click on usernames in the main hall chat to ignore/unignore other players.';
export const IGNORE_LABEL = 'Ignore a player:';
export const IGNORE_PLACEHOLDER = 'Choose or type a player';
/** The field's maxlength. */
export const IGNORE_NAME_MAX = 30;
export const IGNORE_BUTTON = 'Ignore';
export const IGNORE_REMOVE = 'Remove';
export const ignoreRemoveLabel = (name: string) => `Stop ignoring ${name}`;
export const IGNORES_EMPTY = 'You are not ignoring anyone.';
export const IGNORE_NAME_MISSING = 'Type the name of the player to ignore.';
/** AccountIgnores.errorMap. */
export const IGNORES_UNREACHABLE = 'Could not reach the server.';
export const IGNORES_NOT_LOGGED_IN = 'You are not logged in.';
export const IGNORES_FAILED = 'Could not load or change your ignore list. Try again later.';

// ---- My Game History (gameHistory.html, gameHistoryUi.js) ----

export const GAME_HISTORY_HEADING = 'My Game History';
export const FILTER_LABELS = {
  format: 'Format',
  opponent: 'Opponent',
  event: 'Event',
  from: 'From',
  to: 'To',
} as const;
export const ANY_FORMAT = 'Any format';
export const OPPONENT_PLACEHOLDER = 'Name or start of name';
export const OPPONENT_MAX = 30;
export const EVENT_PLACEHOLDER = 'League, tournament or "casual"';
export const EVENT_MAX = 255;
/** The first event suggestion: its value and its label. */
export const CASUAL_EVENT = { value: 'casual', label: 'Casual games' } as const;
export const APPLY_FILTERS = 'Apply';
export const CLEAR_FILTERS = 'Clear';
export const PAGER = {
  first: { text: '« First', label: 'First page' },
  previous: { text: '‹ Previous', label: 'Previous page' },
  next: { text: 'Next ›', label: 'Next page' },
  last: { text: 'Last »', label: 'Last page' },
} as const;
export const pageOf = (page: number, pages: number) => `Page ${page} of ${pages}`;

export const HISTORY_LOADING = 'Loading…';
export const FROM_AFTER_TO = "The 'From' date is after the 'To' date.";
export const NO_MATCHING_GAMES = 'No games match these filters.';
export const NO_GAMES_YET = 'You have not finished any games yet.';
/** The table's empty row (renderRows emptyText). */
export const NO_GAMES_ROW = 'No games yet.';
export const historyShowing = (first: number, last: number, matching: number, filtered: boolean, total: number) =>
  `Showing ${first}–${last} of ${matching}${matching === 1 ? ' game' : ' games'}` +
  `${filtered ? ` matching the filters (${total} in all)` : ''}.`;
export const HISTORY_UNREACHABLE = 'Could not reach the server. Check your connection and try again.';
export const HISTORY_BAD_FILTERS = 'The server could not use those filters.';
export const HISTORY_NOT_LOGGED_IN = 'You are not logged in.';
export const HISTORY_FAILED = 'Could not load your game history. Please try again later.';

/** The table's columns: their headers and header tooltips (renderRows). */
export const HISTORY_COLUMNS = [
  { key: 'date', title: 'Date', hint: 'The day the game ended, in server time (UTC)' },
  { key: 'length', title: 'Length', hint: null },
  { key: 'result', title: 'Result', hint: null },
  {
    key: 'reason',
    title: 'Reason',
    hint: 'Why the losing player lost: a timeout, a concession, a corrupted Ring-bearer and so on',
  },
  { key: 'opponent', title: 'Opponent', hint: null },
  { key: 'format', title: 'Format', hint: null },
  { key: 'event', title: 'Event', hint: null },
  { key: 'deck', title: 'My Deck', hint: null },
  { key: 'replay', title: 'Replay', hint: null },
] as const;
export const RESULT_TITLES = { W: 'Won', L: 'Lost' } as const;
export const endedTitle = (endTime: string | null, local: string) =>
  `Ended ${endTime ? `${endTime} server time (UTC); ` : ''}${local} your time`;
export const winnerReasonTitle = (reason: string) => `Winner: ${reason}`;
/** An empty Reason or My Deck cell. */
export const NO_VALUE = '—';
export const CASUAL_EVENT_CELL = 'Casual';
export const REPLAY_LINK = 'Replay';
export const REPLAY_TITLE = 'Opens the replay in a new tab';
export const REPLAY_NOT_STORED = 'not stored';

// ---- My Stats (yourStats.html, playerStatsUi.js) ----

export const STATS_INTRO = 'All times are GMT based.';
export const CASUAL_STATS_HEADING = 'Casual statistics';
export const COMPETITIVE_STATS_HEADING = 'Competitive statistics';
export const STATS_HEADERS = ['Format name', 'Deck name', '# of wins', '# of losses', '% of wins'] as const;
export const NO_STATS = 'You have not played any games counting for this statistics';
/** Not in the legacy page, which showed nothing when the call failed. */
export const STATS_FAILED = 'Could not load your stats. Please try again later.';

// ---- My Scenarios (myScenarios.html, scenarioShelf.js MyScenariosUI) ----

export const SCENARIOS_HEADING = 'My Scenarios';
/** The intro's two paragraphs; the parts in `bold` are drawn in bold. */
export const SCENARIOS_INTRO: readonly (readonly { text: string; bold?: true }[])[] = [
  [
    {
      text:
        'Scenarios are games of one player against a scripted opponent, set up and played out as their author wrote ' +
        'them: tutorials that teach the game a step at a time, puzzles with a position to find the way out of, and ' +
        'sandboxes to try things in. Everybody plays them from ',
    },
    { text: 'Play > Play Scenario or Tutorial', bold: true },
    { text: '.' },
  ],
  [
    {
      text:
        'You can write your own here. A new one opens in the authoring tool, where you set the table up and play it ' +
        'through as you want the player to, and the tool writes the script. Your scenarios are offered to you alone. ' +
        'When one is ready, submit it for review: an admin plays it, and either approves it, making it a public ' +
        'scenario that every player is offered, or sends it back with the changes they would like. To improve a ' +
        'public scenario, copy it with ',
    },
    { text: 'Copy Public', bold: true },
    { text: ': approved, your version replaces it.' },
  ],
];

/** ScenarioShelfCommon.KINDS. */
export const SCENARIO_KIND_HEADINGS: Readonly<Record<string, string>> = {
  tutorial: 'Tutorials',
  puzzle: 'Puzzles',
  sandbox: 'Sandboxes',
};
export const UNREADABLE_KIND = 'Unreadable';
export const NOT_IN_A_CAMPAIGN = 'Not in a campaign';

export const NEW_SCENARIO_LABEL = 'New scenario:';
export const NEW_TITLE_PLACEHOLDER = 'Title';
export const NEW_TITLE_LABEL = "The new scenario's title";
export const NEW_TITLE_MAX = 80;
export const NEW_ID_PLACEHOLDER = 'id (optional)';
export const NEW_ID_LABEL = "The new scenario's id";
export const NEW_ID_HINT =
  "What the scenario is known by, and its file's name: small letters, digits and dashes. Empty: made from the title.";
export const NEW_ID_MAX = 60;
export const CREATE = 'Create';
export const CREATE_HINT = 'Start a new scenario in the authoring tool, in a new tab';
export const COPY_PUBLIC = 'Copy Public...';
export const COPY_PUBLIC_HINT =
  'Take a copy of one of the public scenarios to change. Submitted and approved, your version replaces it.';
export const NEW_NAME_MISSING = 'Give the new scenario a title (or an id).';

/** The draft line before its bold title, and after it. */
export const DRAFT_OPEN_BEFORE = 'You have a draft open in the authoring tool: ';
export const draftOpenAfter = (test: boolean, dirty: boolean) =>
  `${test ? ' (being played as a player would)' : ''}${dirty ? ', with changes that have not been saved.' : '.'}`;
export const CONTINUE_WRITING = 'Continue writing';
export const CONTINUE_WRITING_HINT = 'Back to the draft, in a new tab';
export const CLOSE_DRAFT = 'Close it';
export const CLOSE_DRAFT_HINT = 'Give the draft up. Anything not saved is lost.';

export const NO_OWN_FOLDER = "This server has nowhere to keep players' own scenarios (scenarios.user.path is not set).";
export const NO_SCENARIOS_YET = 'You have no scenarios yet. Create one above, or copy a public one to change.';

// a row's tags (text, kind, hint)
export const TAG_IN_REVIEW = 'In review';
export const inReviewHint = (when: string) =>
  `Submitted ${when}. It cannot be changed until an admin has approved or rejected it.`;
export const TAG_CHANGES_REQUESTED = 'Changes requested';
export const changesRequestedHint = (by: string | null, when: string) =>
  `Reviewed by ${firstText(by, 'an admin')} ${when}`;
export const TAG_PUBLISHED = '★ Published';
export const publishedHint = (when: string, originTitle: string | null) =>
  `Approved ${when}: this is the public version of ${firstText(originTitle, 'it')}.`;
export const TAG_PUBLISHED_CHANGED = '★ Published; changed since';
export const publishedChangedHint = (when: string) =>
  `Approved ${when}. You have changed it since: submit it again to update the public version.`;
export const TAG_CANNOT_BE_PLAYED = 'Cannot be played';
export const TAG_DOES_NOT_PLAY_THROUGH = 'Does not play through yet';
export const DOES_NOT_PLAY_THROUGH_HINT =
  'The server plays it through and it does not finish as it says it should. It cannot be submitted until it does.';
export const versionOf = (title: string) => `A version of the public “${title}”: approved, it replaces that one`;
export const wasVersionOf = (id: string) => `Was a version of the public ${id}, which is no longer public`;
/** Between a row's notes. */
export const NOTES_SEPARATOR = ' · ';
export const CHANGES_REQUESTED_LABEL = 'Changes requested: ';
export const SENT_BACK_WITHOUT_NOTE = 'It was sent back without a note.';

// a row's buttons (text, hints)
export const EDIT = 'Edit';
export const EDIT_LOCKED_HINT = 'It is in review, and cannot be changed. Copy it to go on working on it.';
export const EDIT_HINT = 'Open it in the authoring tool, in a new tab';
export const PLAY = 'Play';
export const PLAY_HINT = 'Play it as a player would, in a new tab';
export const COPY = 'Copy';
export const copyHint = (title: string | null) => `Make a copy of it, “Copy of ${title}”, to change`;
export const SUBMIT = 'Submit...';
export const SUBMIT_LOCKED_HINT = 'It has been submitted';
export const SUBMIT_CURRENT_HINT = 'It is the public version as it stands: change it to submit it again';
export const SUBMIT_UNVERIFIED_HINT = 'It has to play through before it can be submitted';
export const SUBMIT_HINT = 'Submit it for review, to become a public scenario';
export const DELETE = 'Delete';
export const DELETE_LOCKED_HINT = 'It is in review, and cannot be deleted';
export const DELETE_HINT = 'Delete it';

// what the status line says
export const SETTING_UP = 'Setting up the table...';
export const NOT_STARTED = 'The scenario could not be started.';
export const STARTED_IN_NEW_TAB = 'Started in a new tab.';
export const OPENING_AUTHORING = 'Opening the authoring tool...';
export const OPENED_IN_NEW_TAB = 'Opened in a new tab.';
export const COPIED = 'Copied.';
export const copiedAs = (path: string | null) => `Copied as ${path}. Edit it from the list below.`;
export const SUBMITTED = 'Submitted for review. It is locked until an admin has approved or rejected it.';
export const DELETED = 'Deleted.';

/** ScenarioShelfCommon.errorOf's "otherwise" for each call. */
export const NOT_LOADED = 'Your scenarios could not be loaded';
export const NOT_STARTED_BECAUSE = 'The scenario could not be started';
export const NOT_OPENED = 'It could not be opened for writing';
export const NOT_RESUMED = 'The draft could not be gone back to';
export const NOT_DONE = 'That could not be done';
export const PUBLIC_NOT_LOADED = 'The public scenarios could not be loaded';
export const SHELF_UNREACHABLE = 'Could not reach the server.';
export const SHELF_FORBIDDEN = 'You may not do that.';
export const shelfFailed = (otherwise: string, status: number) => `${otherwise} (HTTP ${status}).`;

// the questions (ScenarioConfirm.ask) and the Copy Public dialog
export const DIRTY_DRAFT_TITLE = 'You have a draft open with unsaved changes';
export const dirtyDraftText = (title: string) =>
  `“${title}” is open in the authoring tool with changes that have not been saved. Opening another scenario closes ` +
  'it, and those changes are lost.';
export const DIRTY_DRAFT_YES = 'Close it and go on';
export const deleteTitle = (name: string) => `Delete “${name}”?`;
export const deleteText = (name: string) => `Are you sure you want to delete “${name}”?`;
export const DELETE_YES = 'Delete';
export const CLOSE_DRAFT_TITLE = 'Close the draft?';
export const CLOSE_DRAFT_TEXT = 'It has changes that have not been saved, and they are lost.';
export const CLOSE_DRAFT_YES = 'Close it';
export const CANCEL = 'Cancel';

export const COPY_PUBLIC_TITLE = 'Copy a public scenario';
export const COPY_PUBLIC_LOADING = 'Loading...';
export const COPY_PUBLIC_HELP =
  'A copy goes into your own scenarios, linked to the public one: submitted and approved, your version replaces it.';
export const COPY_PUBLIC_ROW_HINT = 'Copy it into your own scenarios';
export const byAuthor = (author: string) => `by ${author}`;
export const DISABLED_NOTE = 'disabled';
