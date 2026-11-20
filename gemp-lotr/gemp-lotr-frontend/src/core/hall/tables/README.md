# core/hall/tables: the Game Hall tab's tables (part 4.2a)

Ported from `js/gemp-022/hallUi.js` (drawTable, drawHall's `<newGame>`, openOrOffer / showReadyModal, PlaySound,
renderTableAge), `js/gemp-022/tables/` (TableFlow, CreateTable, CreateUnrankedTable, CreateBotTable, JoinTable,
SelectDeck, DeckManager, Deck, FormatManager), the table calls of `communication.js`, and the server's
`HallRequestHandler` (createTable, createSoloTable, joinTable, leaveTable, marshalException),
`HallCommunicationChannel` (visitTable, notifyAboutTables), `DeckRequestHandler.getAllFormats` (HallTimers) and
`GameTimer.HALL_TIMERS`. The mobile branch's `src/HallMobile/hallState.js` was the model for `HallTablesState`.
No UI here: the sections, the Play popup and the Join dialog are 4.2's ui part.

## Table lists

- Three sections: **Waiting Tables** (status WAITING), **Playing Tables** (PLAYING), **Finished Tables** (FINISHED);
  their headers show a count, "(3)". A table of any other status is drawn nowhere. (Queues with
  `displayInWaitingTables` and running tournaments also have rows in Waiting / Playing: 4.2b.)
- Order (`HallTablesState`): rows in the order they came into their section. A snapshot starts over; `add` appends;
  `update` replaces the row in place, or moves it to the end of its new section when the status changed; `remove`
  takes it away. The server sends every attribute again on an update (it compares the whole map), so an update
  replaces the table. There is no other sorting or grouping (no per-tournament groups for game tables).
- Section open / closed state (the `hallSettings` cookie) is the view's (4.2 ui).

## Table data (`<table>`, `HallTable`)

`gameId` (empty while waiting), `status`, `statusDescription` (shown as sent), `format` (display name), `tournament`
("Casual" or the league / tournament name), `userDescription` (sanitized by the server: no markup, ≤ 100 chars; on an
invite-only table the invitee's name; "(New Player)" flagging is the server's), `players` (comma-separated), `playing`
(the viewer sits there), `watchable`, `isPrivate`, `isInviteOnly`, `invitedYou` (per viewer), `winner` (finished
games), `createdAt` (epoch ms, server clock). Glacial tables are hidden by the server from everyone but their players
and admins.

- Row: format; tournament name plus an italic note (`tableNote`): "Private match for user 'x'." / "Match for user
  'x'." (invite-only), "Private match: [desc]" / "[desc]", "Private."; the status with, for a waiting table, its timer
  "01:30" (`ageOf`, ticking every second on the server clock: our clock plus `HallComms.serverClockOffset`; tooltip
  "Open since … (server time)", `tableAgeTitle`); players ", "-joined; the action cell.
- Action (`tableAction`): waiting and seated → **Leave Table**; waiting, not invite-only or invited → **Join Table**;
  playing and seated → **Play Match**, watchable → **Spectate** (both links to `/gemp-lotr/game.html?gameId=`
  - participantId, new tab: `HallTableRow.gameUrl`); finished → the winner's name. The view marks rows the viewer
    sits at (`played`) and invitations for the viewer (`privateForPlayer`); updated rows flashed (view).

## Play popup (Create Table): Casual and Bot (4.2a); League and Tournament (4.2b)

- Opening (`openPlay`): a visitor gets the "Not logged in" dialog (`HallSession.requireLogin`); otherwise every result
  box goes back to "Ready." and formats, timers and decks are fetched again (`loadOptions`). With "Open this flow by
  default" (cookie `unranked-table-default-flow`) the Casual form opens at once (view, `casualDefaults`).
- Formats: POST `/deck/formats`, those with `hall: true`, in `order`. A failure is shown inline: "Could not load the
  list of formats: …" (`optionsError`). Timers: its `HallTimers` (code, name, minutes, describeLimits sentence),
  labelled "Default (45m/6m)", "Championship (20m/10m)" ("WC"), "Glacial (1d/1d)"; the page's own five when the server
  sends none. The Game Timer (i) shows the server's sentence, else the general one (`TIMER_HELP`).
- Decks: GET `/deck/list` and `/deck/libraryList` (`[format] - name` labels are the view's). A list that fails keeps
  the previous one. Deck memory per flow in the cookie `<flow>-decks` (`unranked-table`, `bot-table`, `join-table`):
  `{key: [name, "player"|"library"], "_": last overall}`, at most 12 keys. A flow starts with the deck remembered for
  its key (format) if still listed, else the player's first deck, else (no decks) the first library deck
  (`initialDeck`). Casual: picking a deck switches the format to the deck's when the hall offers it (`formatOfDeck`);
  picking a format by hand brings back the deck last used in it unless the deck was picked by hand
  (`rememberedDeck`; "picked by hand" is view state).
- **Casual** (`createCasual`): format, deck, timer (cookie `unranked-table-last-timer`, "default"), description,
  private, invite-only with an invitee (picked from the chat's players or searched with GET `/hall/players`: view /
  4.2b), "keep this window open". Checks, in order: "You must select a format."; invite-only with no name → "If set to
  invite-only, you must name a player …"; inviting yourself → "You cannot invite yourself; name another player."; no
  deck → "You must select a deck: one of yours, or one from the Deck Library." Then the deck, format (cookie
  `unranked-table-last-format`) and timer are remembered and POST `/hall` goes with `format, deckName, deckSource,
timer, desc` (the invitee on invite-only), `isPrivate, isInviteOnly`. Success closes the popup, or with keep-open
  says "Table created. It waits in Waiting Tables until an opponent joins." The server refuses with `<error message>`
  (no such player, playing yourself, invalid deck, "Failed to create table. Please try again later.").
- "Invite to table" in the chat user list (contract C1): the Casual form invite-only for that player
  (`requestInvite`, bare name; `invitee`).
- **Bot** (`createBot`): format (Fellowship Block, `fotr_block`, unless "unlock" — view), your deck, the bot's deck
  ("" = "Random FotR Starter"); POST `/hall/solo` with `isPrivate=false`. "You must select a deck for you to use …".
- Join Table (`openJoin`, `join`): the dialog refreshes the decks; "You must select a deck …"; POST `/hall/{id}` with
  `deckName, deckSource`; success closes it (the game opens from the hall when it starts).
- Leave Table (`leave`): POST `/hall/{id}/leave`; the row goes with the next poll answer.
- Every answer through one reading (TableFlow.readResponse, `parseTableCallResult`): `<error message>` fails;
  `<response message>`, `<result>OK</result>`, empty or "OK" succeeds. HTTP failures in words
  (TableFlow.errorMap, `statusMessage`): 0, 401, 403, 404, 409, 410, 500 have their sentences, a 400 the server's JSON
  `error` or `message` header, others "The server answered with an error (HTTP N)." Result boxes (`results`):
  ready / pending "Working…" / success / error, per flow; `busy` while one is pending.

## Started games

- `HallComms.startedGames$` gives each game once. It opens `/gemp-lotr/game.html?gameId=…` in a new tab
  (`PageNavigation.openInNewTab`); when the browser blocks it, a "Your game is ready" prompt ("Your game has started,
  but your browser blocked the new window.", "Open game") offers the link (`readyGames`) until used, or until its
  table is removed or finished, or a snapshot no longer has it. The fanfare `/gemp-lotr/src/assets/fanfare_x.mp3`
  plays (`SoundPlayer`; refused autoplay is ignored).

## Differences from the legacy hall

- The fanfare plays for each started game; the legacy hall played it once per answer (several games starting in one
  answer is the only case that differs).
- The older single-deck cookies (`<flow>-last-deck`) are not read as a fallback for the deck memory.
- Leave Table failures show in the flow's result (`results().leave`); the legacy call went to the hall's global
  "Server error" dialog.
- An update of an unknown status keeps the table (drawn nowhere); the legacy hall left the old row as it was.

## Known gaps (4.2b and the ui part)

- Queues (Recurring / Scheduled / World Championship sections, join / leave / start, ready checks, prize breakdowns),
  tournaments (Playing Tables rows, drop, late join, register deck, draft links, "Tournament Update" dialogs), the
  League and Tournament flows of the Play popup, and the invitee search (GET `/hall/players`).
- All markup, dialogs, section toggles and row animations.
