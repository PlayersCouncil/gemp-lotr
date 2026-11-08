# Gemp server responses used by the unit tests

- Captured from a real server (this branch, fresh database) on 2026-11-01:
  `deck.xml`, `deck-missing.xml`, `deck-list.xml`, `collection-default.xml`, `convert.xml`, `stats.html`.
  The deck was saved with the name `Test + deck & co` and notes containing `+` and `&`, to check encoding.
- Captured from the local server on 2026-11-08: `formats.json` (`/deck/formats`), which `visual-check/stubbed.mjs` uses.
- Written by hand from the server code, because a fresh database has no leagues, packs or limited collections:
  `collection-with-packs.xml` (CollectionRequestHandler.getCollection), `collection-types.xml`
  (CollectionRequestHandler.getCollectionTypes), `leagues.xml` (LeagueRequestHandler, league list) and
  `pack-delivery.xml` (DeliveryRequestHandler.serializeCollectionsAsXML).
