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
  getTournamentInfo) and `hall-format-definitions.json` (GET /hall/formats/json, FormatDefinitions.describeAll, cut
  down to one format and an entry without a code).
