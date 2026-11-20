# core/hall/play: the Play overlay and the Join dialog (part 4.2b)

Ported from `js/gemp-022/tables/` (CreateTable, TableFlow, FormatManager, DeckManager, SelectDeck, CreateBotTable,
CreateUnrankedTable, CreateLeagueTable, CreateTournament, PlayScenario, JoinTable), `playerPicker.js` and the
markup of `hall.html` `#create-table-popup` / `#join-table-popup`. Server: `TournamentRequestHandler`
(`/tournament/tournamentFormats`, `/tournament/create`), `LeagueRequestHandler` (`/league`, `/league/{code}`),
`ScenarioRequestHandler` (`/scenarios`, `/scenarios/start`, `/scenarios/author/open`), `HallRequestHandler`
(`/hall`, `/hall/players`). Every label, blurb and (i) help text is in `play-messages.ts` (blurbs with emphasis are
markup for `pageMarkup`). Casual and Bot submits live in `tables/HallTablesService` (see `tables/README.md`).

## The overlay (`PlayOverlay`)

- Play: a visitor gets the "Not logged in" dialog; otherwise formats, timers and decks are fetched again, every
  result box is "Ready.", the tournament form goes back to its defaults, the tournament formats are fetched (once),
  and the choice screen shows — or the Casual form with "Open this flow by default" (cookie
  `unranked-table-default-flow`). Title "Create Table", on a form "Create Table ▸ <Bot | Casual | League | Tournament
  | Scenarios & Tutorials>". "< Back" goes to the choice, from the choice closes. Choices, in order: Play Scenario or
  Tutorial, Play Against Bots (Beta), Open Casual Table, Open League Table, Create Tournament (`PLAY_CHOICES`, with
  subtitles and icons). One size for all screens (900 x 800 clamped to the window: view).
- Contract C1 ("Invite to table"): the Casual form, invite-only, the name pinned in the picker.
- The format (i) (`format-info.ts`): the intro sentence, the format's description, its set summary, "Sites: …",
  "Ring-bearer skirmish cancel: Yes/No", and "Compare X with the other formats: Help › Format Definitions" (route
  `help/formats/<code>`; following it closes the overlay).
- Bot: Fellowship Block until **Unlock Formats** ("Here There Be Dragons" warning); the bot's deck "Random FotR
  Starter" or one of the player's.
- Casual: invite-only disables the description (placeholder "An invite-only table's description is the invited
  player's name") and enables the picker. The picker (`PlayerSearch`, `pickerItems`): "In the hall" (the chat's
  users, bare names, never the viewer) then "Other players" (from 2 characters GET `/hall/players?prefix&limit=10`,
  cached per prefix; names pinned from outside), notes "Nobody else is in the hall. Type a name to search all
  players." and "No player's name starts with "x"."; a failed search keeps the players in the hall.

## League (`LeagueTableFlow`)

- League dropdown: the leagues of GET `/league` the player is a member of; none → one disabled line "You have not
  joined a league yet; join one below". The league from cookie `league-table-last-league`, else the one chosen, else
  the first. Deck memory per league (`league-table-decks`). Load failure: "Could not load your leagues: …".
- Create Table: "You must select a league. If you are not enrolled in one, join one below." / "You must select a
  deck. Remember that …"; POST `/hall` with the league code as the format, empty timer and description.
- Below: "Join Leagues" with the Events tab's league list (that part's component: `leagueJoined(code)` /
  `leagueJoinFailed(message)` are its hooks). After a join: the league selected, "You joined X. Next:" with
  "Create a table in this league", "Go to draft" (GET `/league/{code}` draftable: soloDraft.html?eventId=), "Open
  the deck builder" and the Limited note (a serie with `limited`). A failed join: "Could not join the league: …".

## Tournament (`CreateTournament`, `tournament-form.ts`)

- Formats from GET `/tournament/tournamentFormats` once the hall is live (failure: "Could not load the tournament
  formats: …" in the result). Types Constructed, Sealed, Live Draft (`table_draft`), Solo Draft; nothing below the
  type until one is chosen. Fields per type (`tournamentFields`): the deck (Constructed), deck-building minutes
  (Sealed 30, Live Draft 15, Solo Draft 30 by default), draft info and draft timer (Live Draft; a format's max
  players caps the count and its recommended timer is chosen). Advanced (shown / hidden): pairing (Swiss /
  Single-elimination), deck-building, Competitive, Early Start, ready check (-1 / 60 / 120 / 180), draft timer.
- Players ≤ 2: no ready check, no Early Start, pairing disabled; going back above restores what was chosen (default
  1 minute and Early Start on). Host Tournament is enabled with a type and a format.
- POST `/tournament/create` (type, deckName, deckSource, maxPlayers, formatCode, tableDraftTimer, playoff,
  deckbuildingDuration, competitive, startable, readyCheck) answers JSON "OK" or 400 `{"error"}`. Checks: "You must
  select your deck before starting a Constructed tournament.", "Enter the number of players (1 or more).". Success:
  the overlay closes and "Tournament Created" says what happens next.

## Scenarios (`PlayScenario`, `scenarios/scenario-list.ts`)

- GET `/scenarios` each time the form shows. Folders: the kind (Tutorials, Puzzles, Sandboxes), then the file's
  directories (the kind's own folder dropped, names made readable: "01-first-steps" → "First steps"); the player's
  own under "My scenarios", others' under "Other players" / owner. The column lists every folder with its count; the
  list shows the folder chosen (cookie `scenario-folder`, else the first) and the folders inside it under
  "A › B" headings.
- Rows: a tag (New, In Progress, Complete, Updated, Next for a campaign's next one) with its hint, the title, the
  summary, notes ("By x" / "Not an official scenario", "Not verified: …", "Played N times"), and **Play** / **Play
  Again** / **Start Over** (the one in progress). "You are in the middle of **X**." with **Continue** (not for the
  author's own draft). Authors: "Create your own Puzzle or Tutorial Scenarios on My Account > My Scenarios." (route
  `account/scenarios`). Files with mistakes: the list with each error, **Edit** (openable; after the question when a
  draft with unsaved changes would be dropped: POST `/scenarios/author/open`), and a line in the result box while it
  has nothing else to say.
- Starting: "Setting up the table…", a tab reserved on the click, POST `/scenarios/start` → the tab goes to the game
  page ("Started."), else the game is offered; a failure closes the tab, says why and fetches the list again. The
  small-window note shows below 1000 x 600 (`windowTooSmallForTutorial`: view).

## The Join dialog (`JoinDialog`)

Four flows, one dialog: **Join Table** (memory `join-table`, last overall), **Join Queue: X** with "Format: … ·
Starts: …" (`join-queue`, per format), **Join Tournament: X** with "Format: …" (`late-join`, per format), **Register
Deck: X** with "Choose the deck you built from this tournament's cards." (`register-deck`, per tournament). Every
outcome is shown: in the result box while the dialog is open, else in a hall dialog ("<title> failed" for a refusal);
queue joins, late joins and registrations end with their own dialog ("Joined Tournament", "Deck Registered").

## Differences from the legacy hall

- The leagues are fetched when the League form opens (the legacy page fetched them on every Play open).
- `PlayScenario.resumeDraft` was not called anywhere in the legacy page and is not ported.
