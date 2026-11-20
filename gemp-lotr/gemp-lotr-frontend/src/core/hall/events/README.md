# core/hall/events: the Events tab (part 4.4, Events)

Ported from `includes/events.html` (the tab, its sub-tabs and their script), `js/gemp-022/calendarUi.js`
(EventCalendarUI), `eventHistoryUi.js` (EventHistoryUI, MonthNav), `eventDrawer.js` (EventDrawer),
`leagueResultsUi.js` (LeagueResultsUI) and `tournamentResultsUi.js` (TournamentResultsUI) in list mode, the calls
they make in `communication.js`, the deep links of `hallLinks.js` and `GempHallTabs.onReturn` (`src/Hall/GameHall.js`).
Server side: `CalendarRequestHandler`, `EventHistoryRequestHandler` with `EventHistoryService`,
`LeagueRequestHandler`, `TournamentRequestHandler`, `HallRequestHandler.getFormatDefinitions` (`FormatDefinitions`).
No view here; wording is in `events-messages.ts`. `EVENTS_SERVICES` (in `events-tab.service.ts`) is the route's
providers; `EventsTab` holds the rest.

## The tab

- Sub-tabs **Calendar**, **Current Leagues**, **Current Tournaments** (`EVENTS_SUB_TABS`), each a route:
  `events/calendar`, `events/leagues[/<code>]`, `events/tournaments[/<id>]` (`events-routes.ts`). The legacy links
  `#events`, `#events/<sub>`, `#league-<code>` (code `[A-Za-z0-9_-]+`) and `#tournament-<id>` (`[A-Za-z0-9_.-]+`)
  map onto them (`legacyEventsLink`). Opening a league's or tournament's link opens the list's sub-tab and that row's
  drawer, loading the list first if need be, and scrolls it into view (`revealed`; a tournament not in the live list
  leaves the list showing). The tab needs a logged-in player (the hall's frame).
- Intros: Calendar "All dates are server time (UTC). Click an event to see its details; events you have joined are
  in **bold**."; the lists "All times are server time (UTC).". Under the calendar, before any click: "Click on an
  event above to load details.". A rule separates each live list from its completed-events panel.
- Everything loads together the first time the tab opens (`EventsTab.start`): the league list, the tournament list
  and the calendar. Each completed-events panel starts once the live list above it is shown (or failed).
- **Return refresh** (`ReturnRefresh`, GempHallTabs.onReturn): a sub-tab shown again after the viewer was away from
  it for 10 s or more (another sub-tab or tab, the browser tab hidden, the window blurred) fetches its live data
  again: the calendar's month (`EventsCalendar.refresh`), the league list or the tournament list (`refreshList`).
  The view reports what is shown (`EventsTab.subTabShown`, `returns.setPageShown`). Open drawers, the selected
  event, scroll positions and the completed-events panels stay.
- No cookies or localStorage keys of its own (the tab last opened is the frame's).

## The calendar (`EventsCalendar`, `calendar-grid.ts`, `calendar-details.ts`)

- GET `/calendar?from&to` (yyyy-MM-dd, at most 400 days): `{from, to, eventAdmin, events}`. Events: `league` (id =
  code, name, leagueType, start, end, inviteOnly, cost, description (server markdown), scheduleId, joined, series
  `[{name, start, end, format, limited}]`), `tournament` (id, name, tournamentType, format, start = end = its day,
  startTime (a ZonedDateTime's text; "HH:mm" = characters 11-16), started, cost, joined, queueOpen with queueType
  and queueStart), and for event admins `projected` (scheduleId, scheduleName, eventIndex, eventName, name,
  leagueType, start, end, createdOn, error). Unknown kinds are left out.
- Opens on the current UTC month. Header: "<", "November 2026", ">", "Today", and the legend League / Tournament /
  Scheduled (not yet created). The grid shows whole weeks, Monday to Sunday (Mon ... Sun); days of other months and
  today are marked. The fetched range is the grid's (`gridRange`).
- Bars (`buildCalendarGrid`): one per event per week row, clipped to the week, marked where they continue; packed
  into lanes greedily (by start, longest first, then league / projected / tournament). A tournament's label starts
  with its time ("18:00 Name"); a continuing bar's with "◂ ". A league with several series is split into parts where
  a serie starts (the part's title "Finals starts 2026-11-15"); a continuation row adds " · <serie in progress>".
  Joined events are bold; a projected league with an error is marked; the selected bar is marked (kind and id).
  Tooltip (`eventTooltip`): "Name (start to end)" / "Name (day)" / "Name (day HH:mm UTC)", then "Cannot be created:
  …" or "Will be created on … by schedule '…'", "You are signed up", and each later serie "Serie n starts … (format)".
- Clicking a bar (`select`) shows its preview under the grid (`calendarPreview`): the list's header row (title,
  date on the right with the title "Server time (UTC)": "start to end" for a league, "day HH:mm" for a tournament)
  with "Go to League" / "Go to Tournament" in place of + Details (none for a projected league, which gets the
  "Scheduled" chip), "You are signed up", and for a scheduled tournament whose hall queue is open and the viewer not
  in it the hall's **Join Queue** (`queue`: id, name, type, start; the join flow is the Game Hall's queue join,
  4.2b). Its drawer is open: a league's detail (`LeagueResults.createDrawer`), a started tournament's
  (`TournamentResults.createDrawer`), or the facts (`calendarFacts`) for a tournament not started, a projected
  league, or a fetch that failed: Starts / Format, or Runs; Cost (gold and silver); Entry "Invite only"; Status
  ("Cannot be created: …" / "Not created yet; will appear on … (schedule '…')"); the description; the serie table
  (#, name unless it is just "Serie n", format, "start to end").
- "Go to …" (`goTo`) leads to `events/leagues/<code>` or `events/tournaments/<id>`.
- Failures: 400 "Could not load the calendar.", 401 "You must be logged in to see the calendar.", in the grid's place.
- Refresh: the month is fetched again; if the selected event changed (compared as data) its preview is built again,
  otherwise its open drawer is fetched again quietly.

## Drawers (`EventDrawer`, `event-drawer.ts`)

- "+ Details" / "− Details" (a real minus). Fetched on the first opening ("Loading details…"); later toggles only
  open and shut. A failure is shown in the pane and fetched again at the next opening. A drawer refreshed while shut
  is marked stale: it reopens with its old content and fetches again. A quiet refresh (a data refresh) that fails
  keeps the old content. An answer for content since replaced is ignored. Slide animation (250 ms, none with
  prefers-reduced-motion) is the view's.
- Messages of a failed call (`eventErrorMessage`, EventDrawer.errorMap): 0 "The server is unreachable or the
  connection dropped.", 401 "You are not logged in.", 403 "You do not have permission to see this.", 404 "It was not
  found on the server.", 410 "You were logged out after being inactive; refresh the page.", 500 "The server failed to
  produce it.", 400 "The request was rejected: <the server's error>" or "… as malformed.", others "The server
  answered with HTTP n.".
- A list refreshed in place (`updateRows`): rows still listed keep their drawer and state (title and date updated),
  open drawers fetched again quietly, shut ones marked stale; new rows added, gone ones removed, in the new order.

## Current Leagues (`LeagueResults`, `league-view.ts`)

- GET `/league`: `<leagues>` of `<league code name start end ...>`; a row per league ("start to end"; the start
  alone when they are equal). "There are no current leagues at the moment." / "Could not load the current leagues.
  <reason>". League admins also see the code in the row, text a click selects ("League code - click to select";
  view, from the player's type `l`).
- A league's drawer: GET `/league/{code}` (running or finished): `<league member joinable draftable code name desc
inviteOnly cost start end [type=RTMD pathLength cumulative advancementMode advanceFactor playerPosition]>` with
  `<metaSite position blueprintId visualBlueprintId name>`, `<serie type maxMatches start end formatType format
formatAvailable collection limited>` (`<matches><match winner loser>` (the viewer's), `<standing>`s) and
  `<leagueStanding player standing points gamesPlayed opponentWin [position]>`. Failures: "Could not load the league
  (HTTP n). <reason>"; an unreadable answer "Could not display the league."; another root "No details are available
  for this league.".
- Shown (`leagueMembership`, `serieSummary`, `standingsHalves`, `rtmdSummary`): "Cost: <gold> <silver>"; membership:
  "You are already a member of this league. " (+ "Go to draft" for a solo-draft league that has started:
  `/gemp-lotr/soloDraft.html?eventId=<code>`, new tab), or "You're not a member of this league. " with "Join
  league", or **"Invitation-only. See below for how to join."**; the description, then a rule; for RTMD "Race to
  Mount Doom", its four intro paragraphs, "Your level: **n** of m", "Advance every n points / win(s)" (n > 1), the
  path's cards (the visual card with the modifier over its bottom, the position number on it; the viewer's level
  outlined gold, earlier ones faintly in cumulative mode; click or right-click shows the card), "Cumulative mode: all
  prior meta-sites remain active"; per serie "name - start to end", "Format: Constructed <format>" (no "Constructed"
  when limited; " (retired)" when the format is gone, else clickable: "Format description"), "Collection: …"; then
  tabs **Overall results** (the standings), **Your league matches** ("Serie n", Winner / Loser) and **Serie n** each
  ("Maximum ranked matches in serie: n", its standings). Standings: Standing, (Level for RTMD overall), Player,
  Points, Games played, Opp. Win %, in two halves side by side. A drawer re-rendered keeps the tab chosen (view).
- Joining (`requestJoin` / `answerJoin`): the "League operation" dialog "Do you want to join the league by paying
  <price>?" Yes / No; POST `/league/{code}`. 409 "You don't have enough funds to join this league.". After a join
  every live drawer of that league (the list's, the calendar preview's) is fetched again and `joined$` fires: the
  calendar loads again (`EventsTab`), and the hall's Play popup should reload its league formats (4.2b).
- Format description: the legacy dialog showed GET `/hall/format/{code}` (HTML). Here `EventsApi.formatDefinition`
  picks the format from GET `/hall/formats/json` (asked once per page; `format-definitions.ts`), whose entry Help ›
  Format Definitions draws.

## Current Tournaments (`TournamentResults`)

- GET `/tournament`: `<tournaments>` of `<tournament id name format collection round stage start>`; a row per
  tournament, its start day on the right. "There are no running tournaments at the moment." / "Could not load the
  current tournaments. <reason>". Admins and league admins see the id in the row as a link to the tournament's
  report (`tournamentReportUrl`, new tab; view, from the player's type `a` / `l`).
- A tournament's drawer: GET `/tournament/{id}`: `<tournament id name format collection round stage>` with
  `<tournamentStanding player standing points gamesPlayed opponentWin medianScore cumulativeScore>`. Shown
  (`tournamentLines`): "Rounds: n" (in the list's and the calendar's drawers, not in Completed tournaments),
  "Format:", "Collection:", "Round: n" while the stage is "Playing Games"; the standings (Standing, Player, Points,
  Games played, Mod. Median Score, Cumulative Score, Opponent Win %) by standing; once "Finished" each player links
  to their deck (`/gemp-lotr-server/tournament/<id>/deck/<player>/html`, new tab). Failures: "Could not load the
  tournament (HTTP n). <reason>", "Could not display the tournament.", "No details are available for this
  tournament.".

## Completed events (`EventHistoryBrowser`, `event-months.ts`)

- One panel per kind: "Completed leagues" under Current Leagues, "Completed tournaments" under Current Tournaments.
  GET `/eventHistory/months?kind` → `{kind, months}` (newest first); GET `/eventHistory?month=yyyy-MM&kind` →
  `{month, kind, isAdmin, events: [{id, name, startDate, endDate, format, playerCount, rounds, adminLinks}]}`.
  Leagues are bucketed by the month they ended, tournaments by the month they started (the server's).
- Header: "<" ("Previous month with events"), the month title (a button, "Choose a month", opening a dropdown of the
  months grouped by year, the shown one marked; "Loading months…", "Could not load the list of months. <reason>"
  with Retry, or "No completed … have been recorded yet."; closes on a pick, Escape or a click outside), ">" ("Next
  month with events"). The arrows jump to the nearest month with events, disabled where there is none and while the
  list loads; without the list they step a calendar month, never past the current one.
- Starts (`start`) on the newest month with events up to the current UTC month, or the month the viewer navigated to
  before; "Loading completed …s…", "Loading March 2026…", "No completed leagues have been recorded yet.", "No
  completed leagues in March 2026.", "March 2026: 3 completed leagues." ("1 completed league."), "Could not load
  March 2026. <reason>" with Retry. Months are cached for the page; an answer for a month already left is cached but
  not shown.
- Table: leagues "", Name, Dates ("start – end"), Format, Players; tournaments "", Name, Date, Format, Players,
  Rounds; empty cells "—". For an admin (the answer's `isAdmin`, never the page's own idea) an Admin column: a
  tournament's report link (its id as text) or a league's code as text a click selects; addresses other than
  same-site paths and http(s) are plain text; an empty cell "—".
- "+ Details" opens a row under the event's with its detail, as the live lists show it but without membership (no
  join or draft) and without the "Rounds:" line. "Could not load the details (HTTP n). <reason>", "Could not display
  the details.", "No details are available for this league / tournament.".

## Prizes

`EventPrizes` (`{label, lines}`) and `parseEventPrizes`, the one place that reads them (`prizesLabel`, `prizesLines`;
the old HTML snippet is never read). The legacy Events tab shows no prizes, and no Events call carries them yet; the
calendar's tournaments read them if the server adds them.

## Differences from the legacy tab

- A calendar answer for a month already left is dropped (the legacy page drew it under the newer month's title).
- A calendar failure other than 400 and 401 says what went wrong in the grid's place (the legacy page passed it to
  the hall's failure handler).
- A join that fails other than with 409 says why (the legacy page's own communication object swallowed it).
- The tournament's "Round:" line shows while games are played: the legacy page compared the stage with "Playing
  games", which the server never sends ("Playing Games"), so it never showed.
- Server text is data (names, standings, the format) or MarkupNode lists (descriptions); the legacy page put most of
  it into the page as HTML.
- An unreadable answer says "The server's answer could not be read." (or the drawer's "Could not display …"); the
  legacy page did nothing.
- The format description comes from `/hall/formats/json`, which lists the hall's formats only: a league format that
  is not one has no description (`formatDefinition` gives null), and `sites` is the block's code ("FELLOWSHIP"), not
  its name as the HTML had it.
- A league or tournament listed twice gets one row.

## Left for the view (phase 2)

The tab frame and sub-tab bar, the month grid's CSS grid layout, the drawers' slide, the calendar's scroll keeper
(the page does not jump when the preview is replaced), the dialogs (join, format description), the card preview of
the RTMD cards, the league admin's code chip and the report links' player-type check, and the Join Queue button's
flow (4.2b's).
