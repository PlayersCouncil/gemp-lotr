# Gemp server responses used by the unit tests

- Captured from a real server (this branch, fresh database) on 2026-11-01:
  `deck.xml`, `deck-missing.xml`, `deck-list.xml`, `collection-default.xml`, `convert.xml`, `stats.html`.
  The deck was saved with the name `Test + deck & co` and notes containing `+` and `&`, to check encoding.
- Captured from the local server on 2026-11-08: `formats.json` (`/deck/formats`), which `visual-check/stubbed.mjs` uses.
- Written by hand from the server code, because a fresh database has no leagues, packs or limited collections:
  `collection-with-packs.xml` (CollectionRequestHandler.getCollection), `collection-types.xml`
  (CollectionRequestHandler.getCollectionTypes), `leagues.xml` (LeagueRequestHandler, league list) and
  `pack-delivery.xml` (DeliveryRequestHandler.serializeCollectionsAsXML).
- `player.json`: GET /player (PlayerInfoRequestHandler, `Player.GetUserInfo` serialized by `JsonUtils.Serialize`),
  written by hand from the server code, for a player with trophies.
- Written by hand from the server code, for the login page: `login-ok.xml` (the body of a successful POST /login or
  /register: `writeXmlResponse(null, ...)` in GempukkuHttpRequestHandler writes `<result>OK</result>`; a 202 or an
  error has an empty body) and `server-status.json` (GET /status, StatusRequestHandler, with made-up counts) and `server-status.txt` (the line the
  legacy page got from GET / for the same counts, which the login page words itself).
- Written by hand from the server code, for the hall (a fresh database has no tables or queues): `hall.xml` (GET
  /hall, HallRequestHandler.getHall with SerializeHallInfoVisitor: `<hall channelNumber currency motd serverTime
serverTimeMs>` with `add` queue, tournament and table rows and a `<newGame>`; attributes from
  HallServer.processHall, values made up), `hall-update.xml` (POST /hall/update: `update` / `remove` rows, `shutdown`,
  two `<newGame>`s, one already announced) and `hall-update-empty.xml` (an update with no changes).
- Written by hand from the server code, for the hall chat: `chat.xml` (GET /chat/Game%20Hall,
  ChatRequestHandler.serializeChatRoomData: `<chat roomName>` with `<message from date>` holding server markup
  (MarkdownParser output for players), and the `<user>` list with the viewer's own entry first (`self`,
  `incognito`), a `role`, and an old-style "+ " prefix), `chat-update.xml` (a poll's answer) and, for
  PlayerInfoRequestHandler, `ignores.json` (GET /player/ignores), `ignore-added.json` (POST /player/ignores/add) and
  `ignore-refused.json` (the same for a name that is no player: still 200, `error` true, IgnoreListService's message).
- Written by hand from the server code, for the hall's tables (4.2a): `hall-tables-update.xml` (POST /hall/update with
  invite-only `add`s, one for the viewer (`invitedYou`), and a table that `update`s to FINISHED with a `winner`),
  `hall-table-ok.xml` (what POST /hall, /hall/solo, /hall/{id} and /hall/{id}/leave answer on success:
  `writeXmlResponse(null)`), `hall-table-error.xml` (their refusal, still status 200: `marshalException`) and
  `hall-formats.json` (POST /deck/formats, DeckRequestHandler.getAllFormats: `Formats` keyed by code, one not offered
  in the hall, and `HallTimers` from GameTimer.HALL_TIMERS with their describeLimits sentences).
- Written by hand from the server code, for the Events tab (a fresh database has no leagues, tournaments or
  schedules; values made up): `events-calendar.json` (GET /calendar, CalendarRequestHandler: a league with two series
  and a description holding a script, a scheduled tournament whose queue is open, a started one, two projected
  leagues (event admin), one with an error, and an event of an unknown kind), `event-history-months.json` (GET
  /eventHistory/months), `event-history-league.json` and `event-history-tournament.json` (GET /eventHistory,
  EventHistoryService.getMonthEvents; the tournament month for an admin, with a report link and an unsafe one),
  `league-detail.xml` and `league-detail-rtmd.xml` (GET /league/{code}, LeagueRequestHandler.getLeagueInformation:
  series with matches and standings, one with a retired format; an RTMD league's path), `tournaments.xml` (GET
  /tournament, getCurrentTournaments), `tournament-playing.xml` and `tournament-finished.xml` (GET /tournament/{id},
  getTournamentInfo) and `hall-format-definition.json` (GET /hall/format/{code}/json, FormatDefinitions.describe, with
  sitesName).
- Written by hand from the server code, for the Game Hall tab's queues and tournaments: `hall-queues.xml` (GET
  /hall: queues from HallCommunicationChannel.visitTournamentQueue — a World Championship queue shown in Waiting
  Tables, a recurring one with `prizesLabel` / `prizesLines` (the prizes as data, from the server change on
  `feature/hall-angular`), a player-made live draft with only the old `prizes` HTML and a ready check running — and
  tournaments from visitTournament: playing, a solo draft deck-building with 9 players and `timeRemaining`, and one
  open to late joins), `tournament-formats.json` (GET /tournament/tournamentFormats,
  JSONDefs.PlayerMadeTournamentAvailableFormats), `scenarios.json` (GET /scenarios, ScenarioRequestHandler.list,
  serialized with nulls), `delivery-announcement.json` (GET /delivery/announcements, DBDefs.Announcement with its
  content rendered by MarkdownParser) and `delivery-league.json` (GET /delivery/league, DBDefs.Transfer list).
- Written by hand from the server code, for Help and Server Info (4.3): `format-definitions.json` (GET
  /hall/formats/json, FormatDefinitions.describeAll serialized by JsonUtils.Serialize, which leaves nulls out: a PC
  format with an X-list holding a V-set card without collector's info and a card the library doesn't know, Anything
  Goes with PC cards legal, limits, an extra legal card and a title on the R-list, and a playtest format without
  description or set summary), `server-stats.json` (GET /stats, JSONDefs.PlayHistoryStats), `patch-notes-page.json`
  (GET /patchnotes?start=5: PatchNotesLibrary.pageJson with SerializeWithNulls; a note whose body has a card link, a
  hall link, a screenshot and a web link, and whose summary has a culture icon, as PatchNoteRenderer and CultureIcons
  write them; an announcement without title or summary) and `patch-notes-months.json` (GET /patchnotes/months).
- Written by hand from the server code, for the My Account tab (a fresh database has no games or scenarios; values
  made up): `game-history.xml` (GET /gameHistory, GameHistoryRequestHandler.getGameHistory: the second page of a
  filtered history, a won league game with a replay, a lost casual one without, and an old entry with no `result`,
  `opponent` or times), `game-history-filters.json` (GET /gameHistory/filters), `player-stats.xml` (GET /playerStats,
  PlayerStatsRequestHandler: casual entries, no competitive ones), `scenarios-mine.json` (GET /scenarios/mine,
  ScenarioShelf.mine with ScenarioAuthoring.current: one in review in a campaign, one sent back that is a version of a
  public one, one published, an unreadable one whose public scenario is gone, and a draft open with changes),
  `scenarios-public.json` (GET /scenarios/public, ScenarioShelf.publicOnes) and `scenario-copy.json` (POST
  /scenarios/mine/copy and copyPublic).
- Written by hand from the server code, for Help › PC Errata (values made up): `errata.json` (GET /hall/errata/json,
  HallRequestHandler.getErrataInfo with ErrataCatalog.build, serialized by JsonUtils.Serialize, which leaves nulls
  out): a PC errata with a stat and text change, an original with both a PC and a playtest errata whose names differ
  from it by accents only, a site whose change is only in its image, a type change (Event → Condition), a V-set
  revision with its earlier version and one without, and a hall format without errata; with the old `all` and
  `recent` maps the page doesn't read.
- Written by hand from `gemp-lotr/PC-ACCOUNT.md` ("The Server's Addresses"), the contract the client is built
  against, before the server had them (values made up), for the PC account: GET /argonauth/session as
  `argonauth-session.json` (the document's own example: a PC account playing an alias that is busy, so `blockedBy` names it),
  `argonauth-session-test.json` (`test` mode, a password login), `argonauth-session-nobody.json` (`optional`,
  nobody), `argonauth-session-password.json` and `argonauth-session-password-linked.json` (a password login, its
  account not linked and linked), `argonauth-session-aliases.json` (a PC account with three aliases and none played),
  `argonauth-session-no-aliases.json` (a PC account with none) and `argonauth-session-stale.json` (`fresh` false);
  what POST /argonauth/enter, /create and /link answer on success is a session as these. POST /argonauth/logout as
  `argonauth-logout-alias.json` and `argonauth-logout-all.json` (`next`). `argonauth-errors.json` lists every refusal
  of the document's table, by action, as `[status, code]`: each is answered with the body `{"error": "<code>"}`.
  Replace them with captured answers once the server has the addresses.
