# core/hall/account: the My Account tab (part 4.4, My Account)

Ported from `includes/account.html` (the tab, its sub-tabs and its script: the pocket, the trophies migration,
AccountIgnores), `includes/account/gameHistory.html` with `js/gemp-022/gameHistoryUi.js` (GameHistoryUI) and the
helpers it uses from `commonUi.js` (formatTime, formatDuration), `includes/account/yourStats.html` with
`playerStatsUi.js` (PlayerStatsUI), `includes/account/myScenarios.html` with `scenario/scenarioShelf.js`
(ScenarioShelfCommon, MyScenariosUI) and `scenario/scenarioSubmit.js` (ScenarioConfirm, ScenarioSubmit), the calls in
`communication.js`, the deep links of `hallLinks.js` and `GempHallTabs.onReturn`. Server side:
`GameHistoryRequestHandler`, `PlayerStatsRequestHandler`, `PlayerInfoRequestHandler` (the ignore list),
`CollectionRequestHandler.migrateTrophies`, `ScenarioRequestHandler` (`/scenarios/mine`, `/public`, `/start`,
`/author/open|resume|close`) with `ScenarioShelf` and `ScenarioAuthoring.currentForHall`. No view here; wording is in
`account-messages.ts`. `ACCOUNT_SERVICES` (in `account-tab.service.ts`) is the route's providers, under the hall's
route (it needs `HALL_SERVICES`: the chat, the hall's poll, the hall session); `AccountTab` holds the rest.

## The tab

- Sub-tabs (vertical, at the left; `ACCOUNT_SUB_TABS`, HALL_TABS): **My Account**, **My Game History**, **My
  Stats**, **My Scenarios**. Routes (`account-routes.ts`): `account` (My Account itself), `account/history`,
  `account/stats`, `account/scenarios`. The legacy links `#account`, `#my-account` and `#account/<sub>` are mapped
  onto them by the hall's `hall-hash-routes.ts`.
- The legacy tab opened on the sub-tab last shown. Here `account` is My Account's own address, so the remembered
  sub-tab is opened instead only when the tab is entered from elsewhere (`shouldRestoreSubTab`): the tab bar's My
  Account from another tab, or the hall's own address redirected to the tab last opened. A link to `account`, a reload
  of it, and the sub-tab bar's My Account show My Account (the view's route guard).
- The sub-tab shown is kept in localStorage `gemp.account.subTab`, as the legacy sub-tab link's href (`#account`,
  `includes/account/gameHistory.html`, `.../yourStats.html`, `.../myScenarios.html`: `ACCOUNT_SUB_TAB_HREFS`), so both
  halls read each other's.
- The tab needs a logged-in player (the hall's frame). My Account's own part (the ignore list) loads when the tab
  first opens, whichever sub-tab shows; every other sub-tab loads the first time it shows, and keeps what it shows
  afterwards (account.html's beforeLoad: a sub-tab's page was fetched once).
- **Return refresh** (`AccountTab.returns`, the Events tab's `ReturnRefresh`): coming back after 10 s or more fetches
  again the ignore list (the name being typed stays), the game history page shown (same page and applied filters,
  quietly; the filters' suggestions too) and My Scenarios. My Stats is not refreshed. The view reports the sub-tab
  shown (`AccountTab.subTabShown`, null when the tab is not showing) and whether the page is in front of the viewer.

## My Account (`AccountOverview`)

- "My Account: <name>" (the player's name from GET /player).
- The pocket, at the right: the player's money from the hall's poll (`<hall currency>`, `HallComms.currency`), drawn
  as gold and silver (`formatPrice`: 100 silver to the gold, silver in two digits, with `images/gold.png` and
  `images/silver.png`). Null until the hall's first answer.
- **Migrate Trophies to My Cards** shows when the player still has a Trophies collection (GET /player `hasTrophies`).
  It asks in the browser's own confirm (`MIGRATE_QUESTION`), then POST `/collection/migrate`; done, the page is
  loaded again. A failure goes to the hall's handling (below); 410 is "nothing to migrate".
- **Ignored players**: the help paragraph, "Ignore a player:" with the player picker (placeholder "Choose or type a
  player", 30 characters) and **Ignore**, a status line (a live region), the list ("<name> [Remove]", the button's
  label "Stop ignoring <name>") or "You are not ignoring anyone." once the list has come.
  - GET `/player/ignores` → `{ignored}`; POST `/player/ignores/add` | `/remove` with `name` → `{outcome, changed,
error, name, message, ignored}`, always 200: a refused name (not valid, yourself, no such player) has `error`
    true and the server's message ("There is no player called 'x'.").
  - An empty name: "Type the name of the player to ignore." with no call. After an answer the list is the answer's
    and the status line its message; the field is emptied unless the name was refused (`add` answers whether).
  - Failures: 0 "Could not reach the server.", 401 "You are not logged in.", 500 "Could not load or change your
    ignore list. Try again later.", in the status line; any other is the hall's.
  - The list is the chat's (`ChatService.ignored`), so the user list's menu and this list show each other's changes
    (the legacy `gemp:ignores-changed` event).
- The hall's handling of a side request's failure (hallUi.js requestFailed): 401 the "Not logged in" dialog
  (`HallSession.requireLogin`), 0 and 504 nothing (the readout reports it), any other the "Server error" dialog "The
  server could not complete that request (error N). Try again; if it keeps happening, reload the page." (through the
  Notifier, whose alert has no title).

## My Game History (`GameHistory`, `game-history-rows.ts`)

- Heading "My Game History". Filters (a form): Format (a list: "Any format", then GET `/gameHistory/filters`'
  `formats`, most played first), Opponent ("Name or start of name", 30), Event ("League, tournament or "casual"", 255,
  suggesting "Casual games" (value `casual`) then the `events`, most recent first), From, To (dates), **Apply**
  (also Enter) and **Clear**. The suggestions failing leaves the fields as free text, quietly.
- GET `/gameHistory?start&count=20` with the filters applied that are not empty (`format`, `opponent`, `event`,
  `from`, `to`; trimmed) → `<gameHistory count total start pageSize playerId>` of `<historyEntry winner loser
winReason loseReason formatName tournament result opponent deckName gameRecordingId startTime startMs endTime
endMs>`. `count` is the games matching, `total` all of them; the server clamps `start`.
- Apply refuses a From after To ("The 'From' date is after the 'To' date.", nothing asked); otherwise both go back to
  the first page. Clear empties the fields (the view's) and the filters.
- While a page loads: "Loading…" and every pager button disabled. Then: "Showing 21–40 of 45 games." (+ " matching
  the filters (99 in all)" with filters; "1 game"), or "No games match these filters." / "You have not finished any
  games yet."; the table's own empty row "No games match these filters." / "No games yet.". Only the latest request's
  answer is shown.
- Pagers above and below the table, shown only with more than 20: "« First", "‹ Previous", "Page n of m", "Next ›",
  "Last »" (labels "First page" ...), disabled at the ends.
- Columns (no sorting: pages come newest first): Date (the day it ended, UTC, its title "Ended <endTime> server time
  (UTC); <yyyy-MM-dd HH:mm> your time"; header title "The day the game ended, in server time (UTC)"), Length ("45 s",
  "12 min", "1 h 05 min"), Result (W / L, titles Won / Lost), Reason (the loser's reason, or "—"; title "Winner:
  <reason>"; header title about the losing player), Opponent, Format, Event (or "Casual"), My Deck (or "—"), Replay
  ("Replay", a new tab, "Opens the replay in a new tab", to the Angular game page `/gemp-lotr/ng/game?replayId=
<playerId>$<recording>`; or "not stored" in italics). Format, Event and My Deck are left-aligned; My Deck wraps.
- Failures: 0 "Could not reach the server. Check your connection and try again.", 400 the server's `message` header
  or "The server could not use those filters.", 401 "You are not logged in.", anything else "Could not load your game
  history. Please try again later."; the table shown stays, the pager comes back.
- There are no deck links in the history (the server sends a deck's name only).

## My Stats (`PlayerStatsView`)

- "All times are GMT based.", then "Casual statistics" and "Competitive statistics", each a table (Format name,
  Deck name, # of wins, # of losses, % of wins) or "You have not played any games counting for this statistics" in
  italics. GET `/playerStats` → `<playerStats><casual><entry format deckName wins losses perc/>…</casual>
<competitive>…</competitive></playerStats>`; `perc` as the server wrote it ("62.5%"). Fetched once.

## My Scenarios (`MyScenarios`, `scenario-shelf.ts`)

- Heading and the two intro paragraphs (`SCENARIOS_INTRO`, with "Play > Play Scenario or Tutorial" and "Copy
  Public" in bold).
- GET `/scenarios/mine` → `{scenarios, limit, ownFolder, authoring}` (no `participantId`: a player's scenarios are
  their own). Each scenario: `path, id, fullId, title, kind, summary, campaign {id, title, position}, playable,
verified, openable, errors, modified, status (submitted | rejected | approved | null), submittedAt, feedback,
reviewedAt, reviewedBy, locked, origin {id, title, exists}, published (current | changed | null)`; `authoring`
  `{gameId, id, title, over, test, dirty}` or null. The server sorts them (kind, campaign, title, path). Failure:
  "Your scenarios could not be loaded (HTTP n)." in the status line.
- The draft line, when one is open: "You have a draft open in the authoring tool: **<title>**" + " (being played as
  a player would)" + ", with changes that have not been saved." or "."; **Continue writing** (POST
  `/scenarios/author/resume`, its game in a new tab; failure "The draft could not be gone back to (…)" and the list
  again) and **Close it** (asked "Close the draft?" unless it is known to have no unsaved changes; POST
  `/scenarios/author/close`, then the list again, whatever it answered).
- Tools: "New scenario:" Title (80) and id (60, its placeholder the id the title makes, else "id (optional)"; title
  "What the scenario is known by …"), **Create** (also Enter in either): the id, else one made from the title
  (`scenarioSlug`), else "Give the new scenario a title (or an id)."; it opens in the authoring tool (below) as new
  (`new=true`, `title`), and done the fields are emptied (`create` answers whether). **Copy Public...**.
- The list: by kind ("Tutorials", "Puzzles", "Sandboxes", an unknown kind as sent, none "Unreadable"), then
  campaign (its title; "Not in a campaign" for the rest when a kind has campaigns too). "This server has nowhere to
  keep players' own scenarios (scenarios.user.path is not set)." when `ownFolder` is false; "You have no scenarios
  yet. Create one above, or copy a public one to change." when there are none.
- A row (`ownScenarioRow`): the title (or id, or path) and " #n" in the campaign; tags with tooltips: In review,
  Changes requested, ★ Published / ★ Published; changed since, Cannot be played (the errors) or Does not play through
  yet; the notes ("A version of the public "…": approved, it replaces that one" / "Was a version of the public …,
  which is no longer public", then the path, joined by " · "); the reviewer's note ("**Changes requested:** …", or
  "It was sent back without a note."). Buttons, each with its tooltip: **Edit** (not while locked or not openable),
  **Play** (playable), **Copy** (has an id), **Submit...** (not locked, not the current public version, plays
  through), **Delete** (not locked; drawn as dangerous).
- What they do, the status line saying how it went (`saying`: text and kind pending / success / error):
  - Play: "Setting up the table...", POST `/scenarios/start` (`id`), "Started in a new tab." or the failure.
  - Edit / Create (ScenarioShelfCommon.edit): if a draft with unsaved changes is open, asked first ("You have a draft
    open with unsaved changes", "Close it and go on" / "Cancel"); "Opening the authoring tool...", POST
    `/scenarios/author/open` (`id`, or `new, id, title`), "Opened in a new tab.", the list again; failure "It could
    not be opened for writing (…)".
  - Copy: POST `/scenarios/mine/copy` (`path`) → `{ok, id, path}`, "Copied.", the copy picked out in the list.
  - Submit: asked in the submit box (`question` kind `submit`: what to check first and what submitting does, as the
    authoring tool's `gd-game-author-submit` says it; "Submit for review" / "Cancel"); POST `/scenarios/mine/submit`
    (`path`), "Submitted for review. It is locked until an admin has approved or rejected it.".
  - Delete: asked ("Delete "…"?", "Are you sure you want to delete "…"?", "Delete" / "Cancel"); POST
    `/scenarios/mine/delete`, "Deleted.".
  - One change at a time; after each, success or not, the list again. A failure says the server's `{error}`, else
    "Could not reach the server." (0), "You may not do that." (403) or "That could not be done (HTTP n)." (an
    unreadable answer counts as 200, as jQuery saw it).
- **Opening a game**: the tab is reserved on the click itself (`PageNavigation.reserveTab`, titled "Gemp-LotR") and
  pointed at the game page `/gemp-lotr/ng/game?gameId=<id>` (+ `participantId`) once the server answers; closed if
  the call failed. Without a tab, the hall's "Your game is ready" prompt offers it (`HallReadyOffers`, whose words say
  "the new window"; the legacy shelf's said "the new tab"). The authoring tool is the Angular game page of the draft's game,
  which docks the authoring panel itself (see the frontend README, "Scenario authoring").
- **Copy Public...**: a dialog "Copy a public scenario", "Loading...", then "A copy goes into your own scenarios,
  linked to the public one: …" and the public scenarios (GET `/scenarios/public`) by kind and campaign, each with its
  id, "by <author>", "disabled" and summary, and **Copy** ("Copy it into your own scenarios"): the dialog closes,
  POST `/scenarios/mine/copyPublic` (`id`), "Copied as <path>. Edit it from the list below.", the copy picked out.
  Failure: "The public scenarios could not be loaded (…)" in the dialog.
- Questions (`question`, answered by `answer(yes)` from the click, so a tab can be opened on it): the legacy
  ScenarioConfirm box (title, paragraphs, yes and Cancel, focus on Cancel, Escape or a click outside is no).

## Differences from the legacy tab

- The pocket follows the hall's latest answer; the legacy tab drew it once, when the tab loaded (and "0 00" before
  the hall's first answer, here nothing).
- Server text (names, deck names, stats) is data; the legacy My Stats put the server's deck and format names into
  the page as HTML.
- My Game History says "Could not load your game history. Please try again later." for a status the legacy page did
  not name (it stayed on "Loading…" with the pager disabled), and for an answer with another root.
- My Stats says "Could not load your stats. Please try again later." when the call fails (the legacy page showed
  nothing): wording of the port's own.
- The ignore list is the chat's, so it is shared without an event, and loading it on My Account updates the user
  list's strike-through too.
- Routes instead of fragments; the remembered sub-tab is opened only when the tab is entered from elsewhere (above).
- The replay link's id is URL-encoded (`alice%24rec1`); the game page reads it the same.

## Left out

- **Scenario Admin** (`ScenarioAdminUI`, Admin tab) and with it the shelf's "highlight" set from Admin's Edit public
  scenario and the `gemp:scenarios-changed` event Admin sends to My Scenarios.
- Not on the legacy tab at all, and so not here: incognito (the chat's user list has it: `ChatService.setIncognito`),
  the tester flag calls (`addTesterFlag`, `removeTesterFlag`: `hallUi.js` has them but no page calls them since the
  Playtesting tab was retired) and log out (`communication.js logout` posts to `/gemp-lotr-server/logout`, which the
  server does not route; nothing calls it).
- The Users tab (hidden).

## Left for the view (phase 2)

The vertical sub-tab bar, the forms (and the player picker in the ignore field: the Game Hall's, see below), the
history table and pagers, the scenario rows and their tags' colours (scenarioShelf.css), the question and submit
boxes and the Copy Public dialog, scrolling a picked-out copy into view, and the gold and silver images.

The ignore field expects the Game Hall's picker (`core/hall/play/player-picker.ts` `PlayerSearch` on hall/gamehall):
`online()` (the players in the hall, without the viewer), `suggest(term): Promise<PickerItem[]>` (the hall's players,
and from 2 characters GET `/hall/players?prefix&limit=10`, a failure quietly offering the hall's players alone), and
a text value, handed to `AccountOverview.add(name)`.
