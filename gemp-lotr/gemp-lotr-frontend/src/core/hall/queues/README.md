# core/hall/queues and core/hall/board: the Game Hall tab's rows, queues and tournaments (part 4.2b)

Ported from `js/gemp-022/hallUi.js` (drawHall, drawQueue, showPrizes, the ready check — updateReadyCheck,
showReadyCheckDialog, tickReadyCheck, confirmReadyCheck, endReadyCheck —, openOrOffer / showReadyModal /
closeReadyModal / pruneReadyModals, drawTournament, the section headers, initTable / updateHallSettings and the
`hallSettings` cookie, addWcQueuesSection / addScheduledQueuesSection / insertEventSection, processResponse,
the TournamentSystem chat callback), `common.js` formatPrice, `GameHall.js` (the `.prizeHint` and `.draftFormatInfo`
hints) and the queue / tournament calls of `communication.js`. Server side: `HallRequestHandler` (joinQueue,
leaveQueue, startQueue, confirmReadyCheckQueue, joinTournamentLate, registerLimitedTournamentDeck,
dropFromTournament, marshalException / marshalResponse), `HallCommunicationChannel` (visitTournamentQueue,
visitTournament), `TournamentService.processTournamentsForHall`, the queues' getStartCondition /
getPairingDescription, `Tournament.Stage`, `PrizeSummary`. Wording: `hall-queue-messages.ts`.

## The board (`board/`, `HallBoard`)

- The one subscriber to the poll for rows. Each answer is applied queues first, then tournaments, then tables (the
  legacy drawHall order); a snapshot clears everything first. Every row takes its place from one counter
  (`RowOrder`), so the sections that hold several kinds show them in arrival order: Waiting Tables holds queue rows
  (displayInWaitingTables) and tables; Playing Tables holds running tournaments and tables.
- Sections, in drawing order (`HALL_SECTION_ORDER`): Waiting Tables, Playing Tables, Finished Tables, World
  Championship Queues, Scheduled Events, Recurring Events. WC and Scheduled exist only while they have rows. Headers
  "Title (N)" count every row (`counts`). Columns and widths: `HALL_SECTION_COLUMNS` (queues: Format 10, Collection 8,
  Event Name 30, Starts / Next Start 16, System 16, Cost 8, Prizes 12; tables: Format 20, Table Info 40, Status 10,
  Players 20, Actions / Winner 10).
- A queue's section is fixed when it is added (wc, else recurring, else scheduled, else none) and its row is redrawn
  in place on update; its Waiting Tables row is redrawn in place, added at the end when it comes, removed when the
  flag goes. A tournament's row is redrawn in place, or added. Rows keys: `queue-waiting:<id>`, `tournament:<id>`,
  `table:<id>` (the legacy `data-hall-key`).
- Open / closed (`HallSectionSettings`): cookie `hallSettings`, "|"-separated "1"/"0", default `1|1|0|0|0|0|0|0`,
  slots waiting, playing, finished, wc, scheduled, (retired), recurring, (retired). Read when the hall opens, and for
  WC / Scheduled again each time they reappear; a toggle writes every section on the page, other slots kept.

## Queues (`<queue>`, `HallQueue`)

Attributes: cost, collection, format (name), type (lower-cased: constructed, sealed, solodraft, table_solodraft,
table_draft), queue (name), playerCount, playerList, prizes (legacy HTML, never read as HTML) with `prizesLabel` and
`prizesLines` (JSON array of plain text; `parsePrizes`: a missing or malformed attribute gives no lines and the label
"Prize Breakdown" for old markup, else its text), system, start, signedUp, joinable, startable,
readyCheckSecsRemaining (-1 = none), confirmedReadyCheck, wc, draftCode, recurring, scheduled,
displayInWaitingTables.

- Section row (`queueSectionRow`): format; collection, or for Sealed / Draft events the type's name ("Table Draft");
  the event name (a table draft's name is a button "Show the draft format" opening
  `/gemp-lotr-server/deck/draftHtml?draftCode=` in a new tab); start; system; cost as gold + two-digit silver with
  their coin images (`splitPrice`); prizes (the label, a button opening "Prizes details" when there are lines).
- Waiting Tables row (`queueWaitingRow`): format; "World Championship - " for the WC, nothing for "When 2/1 players
  join", else "<system before its comma> Tournament - ", then "<Type> - " and the event name; start; players;
  buttons (`queueActions`): not signed up and joinable → **Join Queue**; signed up → **Leave Queue**, **Start Now**
  (startable), **READY CHECK - N s** (a check running) or disabled **Waiting for others - N s** once confirmed;
  **Show Prizes** whenever there is a label or lines. Signed up: marked like the viewer's table; WC: bold.
- Join Queue: a Constructed queue opens the Join dialog (deck remembered per format, flow `join-queue`); the others
  are sent at once with no deck. Success: the "Joined Tournament" dialog (`queueJoinedMessage`, per type).
- Leave Queue (POST `/hall/queue/{id}/leave`), Start Now (`/start`), Ready (`/ready`) answer through processResponse:
  an `<error>` or `<response message>` shows an "Error" / "Response" dialog and a chat warning; nothing else shows.
- "Prizes details": the lines as a list, else the label, else "No prize information available."

## The ready check (`HallQueuesService`)

- On each add / update of a queue the viewer is signed up to with readyCheckSecsRemaining > -1: the deadline is our
  clock + that many seconds. Not confirmed and not yet shown: the "Ready Check" dialog opens ("Ready Check started
  for the **name** tournament." / "Confirm you are present within N seconds.", counting down every second on our
  clock) with the fanfare. Confirmed: the dialog closes. Not signed up, no check, or the queue removed: it ends.
  A snapshot ends every check (those still running come back with it, and show their dialog again).
- Ready (dialog or row button) closes the dialog and POSTs `/ready`; OK just closes the dialog.

## Tournaments (`<tournament>`, `HallTournament`)

Attributes: collection, format, name, system, type, stage (Tournament.Stage's words), round, playerCount, playerList
("*" marks abandoned players), signedUp, abandoned, joinable, wc, timeRemaining (when the stage has a deadline).

- Playing Tables row (`tournamentRow`): format; "World Championship - " / "Tournament - " (2 players) / "Practice - "
  (no comma in the list) / "<system> Tournament - ", then "<Type> - <name>"; status "<stage> - <timeRemaining>",
  or "Playing Games - Round N", or the stage; up to 8 players the list, more a count opening "Competing Players"
  (the list and "* = abandoned"); buttons (`tournamentActions`): signed up → **Go to Draft** (while drafting:
  soloDraft.html before / during deck-building, tableDraft.html for a live draft while Drafting; a new tab, no prompt),
  **Register Deck** (limited types before the first round or paused between rounds; Join dialog, memory per
  tournament), **Abandon Tournament** (red; after "Are you sure you want to resign from the X tournament? This
  cannot be undone."; POST `/hall/tournament/{id}/leave` through processResponse); not signed up, not abandoned and
  joinable → **Join Tournament** (late join; a Constructed one opens the Join dialog, memory per format).
- A tournament added while the viewer is in it and drafting opens its draft page (`HallReadyOffers`, "Your draft is
  ready" when blocked) with the fanfare; the prompt goes when the draft is over or the tournament goes.
- "Tournament Update" dialogs: TournamentSystem chat messages while the viewer is in a tournament (`inTournament`:
  set by being signed up to anything, by joins and tournaments created; cleared by Leave Queue / Abandon), and
  "TournamentSystemTo:a;b" messages naming the viewer. Text through serverMessageMarkup (b, strong, i, em, u, br).

## "Your game / draft is ready" (`ready/HallReadyOffers`)

Keys `game:<id>` / `draft:<id>`. Opened in a new tab; blocked → a prompt with no close button: "Your game is ready" /
"Your game has started, but your browser blocked the new window." / **Open game** (drafts: draft words), until the
link is followed or the game / draft is over (its table removed or finished, missing from a snapshot). Scenarios
point a tab reserved on the click at their game (`openReserved`), and offer it when there is none.

## Differences from the legacy hall

- A Leave Queue / Start / Ready / Abandon call that fails over HTTP shows as an "Error" dialog with TableFlow's
  sentence; the legacy calls had no handler and went to the hall's "Server error" dialog.
- The ready check's dialog seconds and the ready row label are separate values, as in the legacy hall (the row's
  label is the last update's seconds; the dialog counts down).
