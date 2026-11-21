# core/hall/help/errata: Help › PC Errata (part 4.3)

Ported from `includes/help/pc-errata.html`, `js/gemp-022/pcErrataUi.js` (PcErrata: the pure helpers; PcErrataUI: the
page; GempErrataHelp: the hash links), the card preview it opens (`cardPreview.js`, GempCardPreview), its share button
(`shareLinks.js`), and on the server `HallRequestHandler.getErrataInfo` with `ErrataCatalog.build`. Wording is in
`errata-messages.ts`. The page is public: no call needs a login.

| File                    | What it holds                                                                              |
| ----------------------- | ------------------------------------------------------------------------------------------ |
| `errata-models.ts`      | The typed answer and the page's filters, sort, options, load state                         |
| `errata-catalog.ts`     | `parseErrataCatalog` (with PcErrata.prepare's fields), `errataCollectorLabel`              |
| `errata-filter.ts`      | Matching, sorting, the View chips, the Set and Format lists, the status, links and address |
| `errata-diff.ts`        | The word diff, the stat changes and the readout under the card (PcErrataUI.note)           |
| `errata-images.ts`      | The two images of a card, what the card preview shows, the aspect before an image loads    |
| `errata-skim.ts`        | The wheel, swipe, key and row-skimming arithmetic the view feeds with its measurements     |
| `errata.service.ts`     | `PcErrataService`: the page's state                                                        |
| `errata-api.service.ts` | `ErrataApi`                                                                                |
| `errata-services.ts`    | `ERRATA_SERVICES`, for the route's providers                                               |

## The call

GET `/gemp-lotr-server/hall/errata/json` (participant sent, as `communication.js getErrata` did; `cache: false` is
the interceptors' no-caching) → `{all, recent, entries, formats, counts}`. `all` and `recent` (the library's errata map
and the pc_errata format's) are the answer's older keys and go unread, as before. Fixture: `errata.json`, written from
the server code.

- `entries`, sorted by base set, card number, id: `id` (shown blueprint), `base` (the original; = id for a V-set
  revision), `kind` errata (sets 50-69, 150-199) / playtest (70-89) / revision (a card the PC revised in place, its
  earlier version read from a commented-out block in its card file), `name` (full name), `title`, `subtitle`, `set`
  (the base set, a string), `setName`, `cardNum`, `collInfo` (the original's), `culture` ("Ringwraith" for WRAITH),
  `cultureCode` (lower-case enum name: `images/cultures/<code>.png`), `side`, `type`, `revision`, `recent` (in the
  pc_errata format's list), `formats` (hall format codes it is in effect in), `after` and `before` (`{name, type,
stats: {Twilight, Strength, Vitality, Resistance, Site, Unique}, gametext}`; stats of 0 left out but Twilight and
  Unique; `before` missing for a revision whose earlier version is gone). Nulls are left out (JsonUtils.Serialize).
- `formats`: every hall format in hall order, `{code, name, count}`; `counts`: `{total, errata, playtest, revision,
recent}`.

Loading: "Loading the errata…" in the table's place; a failure says `loadErrorMessage(status, "the errata list")`
there. The answer is kept for the page's life (PcErrataUI.cache): later visits don't fetch again; after a failure the
next visit does.

## The page

- **Intro**: "PC Errata" with an (i) toggle opening the summary ("7 cards: 5 PC errata of Decipher cards and 2
  revised V-set cards." — errata + playtest, then revisions when any), three fixed paragraphs (the red slash, the wiki
  link `https://wiki.lotrtcgpc.net/wiki/PC_Errata` in a new tab, what **#** counts). The View chips (radio group "View"):
  All (count) and Latest (count; left out when 0).
- **Filters**: Title (search, placeholder "Title", applied 150 ms after the last key), Set ("All sets", then each set
  with errata by number: "1 – The Fellowship of the Ring (12)"), Format ("All formats", the hall formats with errata
  in effect, but `rev_tow_sta` and `debug`: "PC-Movie Block (40)"). A link's set or format that the list leaves out is
  kept as an extra choice "<name or value> (count)". Status beside them (`aria-live`): "40 cards", "3 of 40 cards",
  "No PC errata are in effect in <format>." (when the format is the only filter), else "No errata match these filters.".
- **Search** (folded: lower case, no accents): the title holds the text; or the text without spaces is the collector's
  number ("1r45", "v3 19"); or it reads as a set and card number ("1.45", "1_45", "1-45", "1 45") that is the base or
  the id.
- **Table** (`aria-label` "PC errata"; every row drawn): Card (the collector's label, then a "New" badge, title "In the
  latest batch of errata", when recent; the cell's title "<set name or Set n>, card <n>"), Culture (the icon, height 18,
  `alt`/title the culture; an icon that fails to load becomes the name), Title (the name, "playtest" badge), # (the
  revision, blank for 0). Each header sorts (title "The collector's number; sort by set and card number", "Sort by
  culture", "Sort by title", "How many times the Player's Council has revised the card; sort by it"); again turns it
  round; the sorted column has " ▲"/" ▼" and `aria-sort`. Ties go by set and number. Empty: the status in one cell.
- **Viewer** (`tabindex=0`, `aria-roledescription` "card viewer", "Errata viewer"): ▲ / counter "3 of 40" / ▼
  (disabled at the ends), the Original | PC errata switch (`aria-pressed`), an (i) with the keys' help, Share (copies
  `/gemp-lotr/share/errata/<id>`; disabled with no card; shrinks to its icon, then goes, when the bar is too narrow) and
  Enlarge / Close. The frame: the name, "<coll> · <culture> <type>" (title "<set name> (set n), card n"), badges New
  and "PC revision n" (n ≥ 1); the card (PC version by default; the original on a click, O or the switch; a quick
  squeeze while it turns); then the readout: changed stats one per line ("Strength 7 → 6"; the name and type first when
  they changed), and the game text as Old (plain) and New (additions marked), or "Game text (unchanged)" (plus "The
  game text and stats read the same; the change is elsewhere on the card (compare the images)." when no stat changed
  either), or "Game text" for a card with no original. No image: "No image" / "No image of the original".
  Empty list: the status and "Change the search, or pick another set, format or view.".
- **Enlarge**: both cards side by side ("Original" → "PC errata", the arrow "→"; for sites "↓" when stacking them
  shows them bigger), the readout across below; a backdrop over the hall (a click on it closes; the wheel still steps);
  Escape closes. Opening or closing shows the card again as the PC version.
- **Images**: the PC version is `Card.getImageUrl(id)`; the original `getImageUrl(base, false, true)` (no errata
  image), or for a revision the same file with `.0` (and `E` → `S` in `LOTR-ENV<n>E`). The four cards around the
  current one are preloaded. A right-click on a card (a swipe up on it by touch or pen) opens the card preview on that
  side, the exact image, titled "<name> (original)" / "<name> (PC errata)".

## Browsing (`errata-skim.ts`, `PcErrataService`)

- Keys (not from a field; not while a dialog is open): ↓ / j next, ↑ / k previous, Home / End, O flips, Escape closes
  Enlarge. On a tab only j, k, o (the tabs use the arrows). With Alt, Ctrl or Meta nothing.
- The wheel over the viewer steps one card per 40 px, at most one per 140 ms (a 250 ms pause starts the count again);
  Ctrl+wheel is left to the browser.
- A swipe on the viewer (touch, pen or a mouse drag): 40 px up / 50 px left is next, down / right previous; the frame
  follows the finger by 35 %; a tap on the card flips it.
- Stepping slides the new frame in (240 ms; none with reduced motion) and scrolls its row to the middle of the table
  (smooth when within three windows). At an end the reel nudges (300 ms).
- Scrolling the table skims: the selection rides with the rows, closing in on the first row scrolling up and the last
  scrolling down, so the top and bottom select the first and last card; the viewer catches up at most every 150 ms,
  without sliding while a slide runs. A scroll the page started is not skimming (until 200 ms after it arrives). At the
  table's end the wheel keeps moving the selection a row per row height.
- A click on a row selects it without scrolling the table.

## Links and the address (`help/errata[/<cardId>]?q=&set=&format=&show=`)

- `#pc-errata[?…]` and `#errata-<id>[?…]` map to the route in `../../hall-hash-routes.ts` (with `sort` and `dir`,
  which applyRoute also read: `sort` = num, culture, name, rev; `dir=desc`).
- A link (a card or any of those parameters): its filters and nothing else; its sort; then its card, by its id or its
  original's (of two errata of one original, the one the filters list). If the filters leave the card out, they are
  cleared. A card without errata: the page unfiltered by card, and the status starts "Card <id> has no PC errata. "
  (until the next re-filter). A link that comes before the errata waits for them.
- The bare `help/errata` is the sub-tab: the page comes back as it was left (filters, sort, card), not enlarged.
- Once the page is used or opened from a link (`touched`), the address follows the card on show and the filters
  (`address()`, written with replaceUrl; the sort is not written, as before). The address the page wrote is not a
  new link.
- Share: kind `errata`, `/gemp-lotr/share/errata/<id>` (`../../share-links.ts`).

## Differences from the legacy page

- A link to an original with both a PC and a playtest errata, filtered to the playtest's format, shows the playtest
  errata; the legacy page showed the PC one and dropped the filters.
- `&nbsp;` and the other few entities the server's game text can hold read as their characters, not "&nbsp;".
- `help/errata` (the sub-tab and the link `#pc-errata` with no parameters, which Format Definitions uses for a format
  where PC cards are only legal) comes back to the last state; the legacy link `#pc-errata` alone cleared the filters.
