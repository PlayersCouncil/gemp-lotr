# core/hall/deliveries: what the hall shows on arrival (part 4.2b)

Ported from `communication.js` deliveryCheck, `deliveryService.js`, `announcementDeliveryService.js` and
`leagueDeliveryService.js`; server `DeliveryRequestHandler`, `LotroServerRequestHandler.processDeliveryServiceNotification`,
`CachedTransferDAO` (the announcement rendered from Markdown).

- Any server answer with `Delivery-Service-Package`, `-Announcement` or `-League` set to "true" (the interceptor in
  `core/api`, `DeliveryAnnouncements`) makes `HallDeliveries` fetch it; 404 = nothing, every failure ignored.
- Packages: GET `/delivery/misc` (consumes; `<delivery><collectionType name>` with `<pack>` / `<card blueprintId
count>`): a "New items - <collection>" dialog per collection, new items added to it while it is open.
- Announcement: GET `/delivery/announcements` ({id, title, content (server markup), start, until}; `<br/>` taken out
  as the legacy popup did): "ANNOUNCEMENT - <title>", 800 px or 95 % of the window wide, 90 % high, **Dismiss**
  (POST `/announcements/dismiss`) and **Remind Me Later** (POST `/announcements/snooze`); closing it otherwise
  snoozes. One at a time: not asked again while one shows. A link to a hall page inside it counts as Dismiss.
- Race to Mount Doom: GET `/delivery/league` (consumes; [{message, contents: "oldVisual|oldMod|newVisual|newMod"}]):
  "Race to Mount Doom", the message (else "You have advanced!"), the old meta-site greyed, an arrow, the new one
  (or the new one alone).

## Differences

- Every server answer's headers are read; the legacy page read them only on the calls wrapped in deliveryCheck
  (the hall poll among them, so in practice the same).
- Several moves show one after the other; the legacy dialog was redrawn for each, so only the last was seen.
- Dismiss only dismisses; the legacy close handler snoozed the dismissed announcement as well.
- Links to hall pages in an announcement (`#patch-notes/...`) need `parseServerMarkup` to keep in-page links: today
  it keeps only http(s) links, so such a link shows as its text (see the report).
