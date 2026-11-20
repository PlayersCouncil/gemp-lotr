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
  error has an empty body) and `server-status.txt` (GET /, StatusRequestHandler, with made-up counts).
- Written by hand from the server code, for the hall (a fresh database has no tables or queues): `hall.xml` (GET
  /hall, HallRequestHandler.getHall with SerializeHallInfoVisitor: `<hall channelNumber currency motd serverTime
serverTimeMs>` with `add` queue, tournament and table rows and a `<newGame>`; attributes from
  HallServer.processHall, values made up), `hall-update.xml` (POST /hall/update: `update` / `remove` rows, `shutdown`,
  two `<newGame>`s, one already announced) and `hall-update-empty.xml` (an update with no changes).
- Written by hand from the server code, for the hall chat: `chat.xml` (GET /chat/Game%20Hall,
  ChatRequestHandler.serializeChatRoomData: `<chat roomName>` with `<message from date>` holding server markup
  (MarkdownParser output for players), and the `<user>` list with the viewer's own entry first (`self`,
  `incognito`), a `role`, and an old-style "+ " prefix), `chat-update.xml` (a poll's answer) and, for
  PlayerInfoRequestHandler, `ignores.json` (GET /player/ignores) and `ignore-added.json` (POST /player/ignores/add).
- Written by hand from the server code, for the hall's tables (4.2a): `hall-tables-update.xml` (POST /hall/update with
  invite-only `add`s, one for the viewer (`invitedYou`), and a table that `update`s to FINISHED with a `winner`),
  `hall-table-ok.xml` (what POST /hall, /hall/solo, /hall/{id} and /hall/{id}/leave answer on success:
  `writeXmlResponse(null)`), `hall-table-error.xml` (their refusal, still status 200: `marshalException`) and
  `hall-formats.json` (POST /deck/formats, DeckRequestHandler.getAllFormats: `Formats` keyed by code, one not offered
  in the hall, and `HallTimers` from GameTimer.HALL_TIMERS with their describeLimits sentences).
