# core/hall/help: the Help tab (part 4.3)

Ported from `includes/help.html` (the tab, its sub-tabs, How Do I Play? and the Manual inline),
`includes/help/coc.html`, `leagueRules.html`, `formatRules.html`, `js/gemp-022/formatDefinitions.js`
(GempFormatDefinitions; `css/gemp-001/formatDefinitions.css` for its look), `tables/TableFlow.js` (GempFormatHelp),
the help links of `hallLinks.js` and, on the server, `HallRequestHandler.getFormatDefinitions`,
`FormatDefinitions.java` and `FormatSummary.java`. Wording is in `help-messages.ts`. PC Errata (`pc-errata.html`,
`pcErrataUi.js`, `cardPreview.js`) is another part's: see "PC Errata" below.

Help is public: a visitor who is not logged in reads every sub-tab (`HallSession.begin(false)`), and no call here
needs a login.

## Sub-tabs and routes (`help-routes.ts`)

| Legacy (`#help/<slug>`)        | Route                                 | Source                                             |
| ------------------------------ | ------------------------------------- | -------------------------------------------------- |
| How Do I Play? (`howtoplay`)   | `help/how-to-play`                    | `includes/help.html` `#howtoplay` (lines 21-195)   |
| (`#help/manual`)               | `help/how-to-play`, fragment `manual` | the same page, scrolled to `<h1 id="help-manual">` |
| Code of Conduct (`coc`)        | `help/conduct`                        | `includes/help/coc.html`                           |
| Format Definitions (`formats`) | `help/formats[/<code>]`               | `formatRules.html` (intro) + this folder (data)    |
| What Are Leagues? (`leagues`)  | `help/leagues`                        | `includes/help/leagueRules.html`                   |
| PC Errata (`pc-errata`)        | `help/errata[/<cardId>]`              | the errata part (`pc-errata.html`)                 |

`#format-<code>` → `help/formats/<code>`, `#help/<slug>` → as above, `#card-<id>` (where a card share link lands:
the card display over Help) → proposed `help?card=<id>` (`../hall-links.ts`; not decided with Ketura). `HELP_PAGES`
has the labels in order.

Not used and not ported: `includes/help/manual.html` (nothing links it; it is the same Manual as the one inline in
help.html, word for word); everything in help.html after line 197 is inside an HTML comment (an old Liberapay button,
a `#formats` block whose script called `hall.comm.getFormatRules` and threw the answer away, and old copies of the
Code of Conduct, league rules and Manual): it never ran and is not ported.

## The prose pages (phase 2: Angular templates)

They are static text; nothing in them is fetched or computed.

- **How Do I Play?** (help.html `#howtoplay`): a link to `https://lotrtcgpc.net/newstart.html` (no `target`), three
  YouTube embeds (`<iframe width=420 height=315>`: `sBUS8yt4G_8`, then `azvdgl4LScs` and `BKE_81bm9fo` side by side),
  then the Manual (`<h1 id="help-manual">`, the fragment target; "Courtesy of Drachu84 (with further changes by
  MarcinS)", an `<ol>` of nine items). The iframes are the only thing that is not plain text (third-party
  content; nothing of ours). No links into the hall: the Manual names buttons and tabs ("Deck Builder", **Play**,
  "Waiting Tables", Settings) in words only.
- **Code of Conduct** (`coc.html`): `div.article`, `h2` sections, lists. No links, no images.
- **What Are Leagues?** (`leagueRules.html`): `div.article`, `span.lemph` emphasis, `div.helpimg` figures with
  `images/help/league-standings.jpg` (with a caption) and `league-1.jpg` … `league-8.jpg` (all existing site images:
  `siteImage('help/league-1.jpg')`), two prize tables (`table.tables`), two external links with `target=_blank`
  (`https://lotrtcgpc.net/leagues.html`, the wiki's product page). Headings have ids `join`, `sealed`, `gold`,
  `prizes`, `cheating` that nothing links to. "Navigate to the Events > Current Leagues tab" is text, not a link (it
  could become a router link to `events/leagues`: Ketura's call). Legacy markup bugs to drop: each of those headings
  is opened `<h1>` and closed `</h2>`, and `<div>`s sit inside `<p>`s (the browser closes the `<p>` early).
- **Format Definitions' intro** (`formatRules.html` `.fd-intro`): the sentence "A 'format' is a shorthand …" with an
  (i) toggle (`.info-toggle`, TableFlow.bindInfoToggles) opening the list explaining X-list, R-list, Limit 2 / 3,
  Also legal and PC errata, and "Click (or right-click) a card …". "(see Help › PC Errata)" is text; it may become a
  router link to `help/errata`.

## Format Definitions (`format-definitions*.ts`, `format-filter.ts`)

- GET `/gemp-lotr-server/hall/formats/json` (participant sent, as the legacy call did; public) →
  `{"formats": [...]}`, every hall format in the Play menu's order (`FormatDefinitionsApi`,
  `parseFormatDefinitions`). Each: code, name, anchor (`format-<code>`, code made safe), description, setSummary,
  sites, the rule flags (cancelRingBearerSkirmish, winAtEndOfRegroup, discardPileIsPublic, winOnControlling5Sites,
  ruleOfFour, mulliganRule, usesMaps, playtest, deck size and copy limit, unused by the page), `errata` {pc, playtest,
  pcCardsLegal} and `lists` {banned, restricted, limit2, limit3, valid} of cards {id, name (with uniqueness dots),
  collInfo, set, setLabel, setName}, and `restrictedNames`. `JsonUtils.Serialize` leaves nulls out: they read as
  null (or the server's default for flags). Fixture: `format-definitions.json`, written from the server code.
- Loading: "Loading the format definitions…"; failure in its place: `loadErrorMessage(status, "the format
definitions")` (`../hall-load-error.ts`, neutralErrorMap). No formats: "No formats are available right now." The
  legacy sub-tab was reloaded on every visit and fetched again: `FormatDefinitionsService.fetch()` (the view decides
  when; a later fetch wins).
- One section per format: name; badges "PC errata" (title "Uses the Player's Council errata") when `errata.pc`,
  "Playtest" when `playtest`; a permalink "#" ("Link to this format") and a Share button (`ShareLinks`, kind
  `format`, label "Share", aria "Copy a share link to <name>"); the description; the facts (`formatFacts`): Sets
  (setSummary without "Cards from set(s) "), Sites, Ring-bearer skirmish cancel Yes/No, Errata (link "PC Errata" to
  `errataRoute(code)` when pc, to the whole list `errataRoute(null)` when only pcCardsLegal, "+ versions are legal
  alongside the originals", ", plus playtest errata"), Other rules (`otherRules`); then the lists
  (`formatLists`, `FORMAT_LISTS`: title, count, note; `details` open) and "R-list by title" (names as plain chips,
  not cards); with none, "No X-, R- or limited cards: every card from its sets is legal."
- Cards: name chips grouped by set (`groupBySet`: one row per set label in order, "?" if unknown, the set's name as
  the tag's tooltip), or with "Card images" thumbnails (lazy images; the legacy `new Card(id, "SPECIAL", "hint")`:
  phase 2 builds `CardView`s with `CardViews.create({blueprintId, zone: 'SPECIAL'})`). Tooltip `cardTooltip`:
  "<name> (1R29)" (`collectorLabel`: collInfo with `_` as spaces, else "<setLabel> <number>"). A click or right-click
  opens the card in the card dialog (CardInfoService).
- "Card images": localStorage `gemp.formatDefinitions.images` = "1" / "0" (`FORMAT_IMAGES_KEY`).
- Filter (`filterFormats`): a search box ("Find a format or card", aria "Find a format, or the formats that list a
  card"), applied 120 ms after the last key. Text folded (lower case, accents dropped). A format shows when its name
  or code holds the text, or (from 2 letters) a card on one of its five lists does (not the R-list titles); those
  cards are marked and their lists opened. Status "N of M formats (K by a listed card)"; the contents list hides the
  same formats.
- Contents list beside the formats (sticky): each format's name and "X3 R2" (`navMeta`, tooltip "3 X-listed,
  2 R-listed"); a click reveals the format.
- Opening a format (`open(code)`: the route `help/formats/<code>`, the Play popup's (i) — the tables part should link
  to `formatRoute(code)` —, the contents list, the permalink): a filter hiding it is cleared, it becomes `active`
  (marked in the list) and `reveal` (the view scrolls it to the top, smoothly) and is highlighted for 4 s. Asked
  before the formats arrive, it waits for them; an unknown code does nothing. The legacy page found a format by its
  anchor (`format-<x>`); `findFormat` takes the code or that anchor's part.
- Scroll-spy (view, phase 2): the format at the top of the panel is `setActive`d and its route put in the address
  (replaceUrl), except while a revealed format is pinned (3 s, or while it stays in view; let go of on wheel, touch,
  key or mouse down).

## PC Errata (another part)

Plugs in as the route `help/errata[/<cardId>]` with its filters as query parameters (`q`, `set`, `format`, `show`),
its own services added to Help's route next to `HELP_SERVICES`, and `ShareLinks` (kind `errata`) shared.
`#pc-errata[?k=v]` and `#errata-<id>[?k=v]` read as those routes in `../hall-links.ts`. Format Definitions links to
it with `errataRoute(format)` (`{format: code}`, as GempErrataHelp.show did). `loadErrorMessage` is its
neutralErrorMap too.

## Differences from the legacy hall

- A load failure with a status the legacy map did not name (e.g. 429) says the server is unavailable in the page;
  the legacy page kept saying "Loading…" and the hall's "Server error" dialog showed.
