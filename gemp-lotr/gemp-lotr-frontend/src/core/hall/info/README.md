# core/hall/info: the Server Info tab (part 4.3)

Ported from `includes/info.html` (the tab, its sub-tabs, its inline script, Server Stats and About inline),
`includes/info/patchNotes.html`, `contribute.html`, `js/gemp-022/statsUi.js`, `js/gemp-022/patchNotesUi.js`
(`css/gemp-001/patchNotes.css` for its look), `js/gemp-022/shareLinks.js`, the info links of `hallLinks.js`, and on
the server `ServerStatsRequestHandler`, `PatchNotesRequestHandler`, `PatchNotesLibrary`, `PatchNote`,
`PatchNoteRenderer`, `CultureIcons`, `ShareRequestHandler` and `SharePages`. Wording is in `info-messages.ts`.

Server Info is public: a visitor reads every sub-tab; no call here needs a login.

## Sub-tabs and routes (`info-routes.ts`)

| Legacy                                                     | Route                       | Source                                   |
| ---------------------------------------------------------- | --------------------------- | ---------------------------------------- |
| Server Stats (`#info/stats`)                               | `info/stats`                | info.html `#server-stats` + `statsUi.js` |
| Patch Notes (`#info/patch-notes`, `#patch-notes[/<slug>]`) | `info/patch-notes[/<slug>]` | `patchNotes.html` + `patchNotesUi.js`    |
| Contribute (`#info/contribute`)                            | `info/contribute`           | `includes/info/contribute.html`          |
| About (`#info/about`)                                      | `info/about`                | info.html `#about`                       |

Not used and not ported: `includes/info/tournamentResults.html` (nothing loads it: the Events tab has its own
`#tournamentResults` element and `tournamentResultsUi.js` is loaded by the pages themselves) and
`includes/info/liberapay.html` (a styled Liberapay button nothing includes; Contribute has its own link).

## The prose pages (phase 2: Angular templates)

- **Contribute** (`contribute.html`): "How can I help?" and a list. External links, all without `target` (they left
  the hall in the legacy tab; phase 2 should open them in a new tab like the rest of the hall's outside links:
  Ketura's call): GitHub `https://github.com/PlayersCouncil/gemp-lotr`, the PC Discord `https://lotrtcgpc.net/discord`
  (three times), `https://lotrtcgpc.net/about.html`, and Liberapay `https://liberapay.com/GempukkuStudio/donate`
  around an outside image `https://liberapay.com/assets/widgets/donate.svg` (alt "Donate using Liberapay"; an image
  from another site: keep, or copy it into the site's images). No links into the hall. Nothing dynamic.
- **About** (info.html `#about`): `h1` About, paragraphs, a list of contributors (MarcinS, TBiesty, ketura,
  PhallenCassidy, dmaz; the `<ul>` sits inside a `<p>` in the legacy markup). External links without `target`:
  `https://gemp.starwarsccg.org/gemp-swccg/`, `https://lotrtcgpc.net/`, GitHub. Nothing dynamic.

## Server Stats (`server-stats*.ts`)

- "All dates are server time (UTC)." Fields: "Start date:" (a text box) and "period:" (month, week, day), button
  "Display Stats". The start day opens a month before today in the viewer's own calendar (the day kept, else the
  month's last day), unpadded "2026-2-28" (`defaultStartDay`), the period "month"; the stats of those are shown as
  the tab opens (the legacy page clicked the button).
- GET `/gemp-lotr-server/stats?startDay=&length=` (participant sent, as before; public; the server caches 5 min) →
  `{Stats: [{Format, Count, Casual}], ActivePlayers, GamesCount, BotGamesCount, StartDate, EndDate}`
  (`parseServerStats`). A start day it can't read, or another length, is a 400: the Notifier says "Invalid parameter
  entered". Any other failure is silent (the legacy page's own communication object had an empty failure handler).
  Fixture: `server-stats.json`, written from the server code.
- Shown: "Stats for <StartDate> - <EndDate>", "Active players:", "All games count:", "Bot games count: N (Not counted
  in the below stats)"; then "Competitive Games by Format" and "Casual Games by Format" (`statsTables`): columns
  "Format name", "# of games", "% of competitive" / "% of casual", "% of total"; the percentages in the viewer's
  locale with 2 decimals (`percentage`; empty for a group of 0); the most games first; headers sort ascending then
  descending (`../table-sort.ts`, renderTable; the percentages sort by the count); empty: "No competitive games in
  this period." / "No casual games in this period." Before the first answer the figures are blank.
- Coming back to the sub-tab after 10 s or more (the hall's return-refresh) calls `refresh()`: the stats shown,
  fetched again for the same dates, keeping each table's sort and what is typed in the fields; failures keep what is
  shown. "Display Stats" resets the sorts.

## Patch Notes (`patch-notes*.ts`)

- GET `/gemp-lotr-server/patchnotes?count=5&start=<n>` or `&from=<slug>`, `&tag=<tag>` (no participant) →
  `{total, start, count, tag, all, filters: [{tag, count}], notes: [{slug, kind, date, title, summary, tags,
summaryHtml, html}]}`, newest first; an unknown `from` (or one without the tag) is a 404 with `{"error"}`.
  GET `/patchnotes/months[?tag=]` → `{tag, months: [{month, first, count}]}`. (`/patchnotes/<slug>` exists on the
  server; the legacy page never used it.) Fixtures: `patch-notes-page.json`, `patch-notes-months.json`, written from
  the server code.
- An update: `article` with its title (or its date when it has none; the date also shown beside a title, as sent,
  YYYY-MM-DD), its tags, a Share button (`ShareLinks`, kind `patch-notes`, label "Share", aria "Copy a share link to
  the update|announcement of <date>"), the summary (`summaryMarkup`: summaryHtml's culture icons, else the text) and
  the body (`body`). Announcements (`kind` announcement) are drawn apart. The marked update is the one a link named.
- Markup: `parseServerMarkup(html, RICH_MARKUP)` (`core/chat/chat-markup.ts`, extended for this). PatchNoteRenderer
  keeps Jsoup's `relaxed` safelist plus del, s, ins, hr, mark, kbd, figure, figcaption, details, summary, font, with
  styles cut to colours and font styles. Read: headings, paragraphs, lists (`ol start`), quotes, code, tables
  (cell spans), rules, details/summary, sub/sup, small, mark, kbd, ins, q, cite, dl, figure; pictures (`image`:
  screenshots and the culture / twilight icons `img.patchnote-culture`, relative addresses made `/gemp-lotr/...`:
  they were relative to hall.html); cards (`card`: `span.cardHint.patchnote-card[value]`, its title the card's name;
  click, right-click, Enter or Space opens it in the card dialog); links: web (new tab), the site's own paths, and
  hall links (`hall-link`, `#format-pc_movie`, `#pc-errata`, `#errata-1_4`, `#patch-notes/<slug>`, which the view
  turns into router links with `hallRouteOf`, `../hall-links.ts`). Dropped: inline colours and font styles (the old
  change log's coloured spans; they would need theme values), `font`, `span` (their text stays), `col`/`colgroup`.
- Screenshots (pictures that are not icons nor inside a link) open in a lightbox on click: the picture over a dimmed
  page, its alt as caption, aria-label "Screenshot: <alt>", a close button "×" (aria "Close"); a click off the
  picture, the button or Esc closes it, the focus going back to the screenshot (view, phase 2).
- The window (`PatchNotesService`): 5 updates from a place in the feed. The address follows it (`routeSlug`:
  `info/patch-notes` for the newest, else `info/patch-notes/<first slug>`), replaced, not pushed. A link (`show(slug,
true)`) shows the updates from its update, marked, the page's top scrolled into view (`scrollRequest`); the
  sub-tab opened again (`show(_, false)`) shows the window left, or the newest. Slugs are lower case.
- Filter: chips All and the server's tags (default "Card Fixes", "PC Updates", "User Interface", "Announcement"
  until a page comes), each with its count; a chip shows the newest of its tag (the active one too). Kept while the
  hall's route lives, not past a reload.
- Months (the Events tab's MonthNav): the title is the first update's month ("September 2026"); "<" ("Older month
  with updates") and ">" ("Newer month with updates") step to the nearest month with updates of the filter and show
  it from its newest update; the dropdown lists the months ("Loading months…", or "Could not load the list of
  months. (the server answered N)" with Retry; empty: "No updates are tagged “X” yet." / "There are no patch notes
  yet."). The months are fetched after a page when not at hand (kept 5 min per tag); the arrows are disabled while
  they load or when there is no neighbour.
- Pager at the bottom: "‹ Newer updates", "Updates 6–10 of 23" (past the end: "23 updates"), "Older updates ›";
  nothing when there are no updates. Newer from the second page goes to the newest (no slug).
- Status: "Loading patch notes…" only if the first page takes over 300 ms; failure "The patch notes could not be
  loaded (the server answered N). " / "(no answer from the server). " with "Try again". Every failure stays on the
  page (no hall dialog). A 404 for a link's slug: under a filter, "Showing all updates: that one is not tagged “X”."
  and it is shown under All; else "There is no update named “x”. Here are the newest ones." with the newest. Empty:
  "There are no older updates." / "No updates are tagged “X” yet." / "There are no patch notes yet."
- Cache (`PatchNotesApi`): pages kept 5 min (key: tag, from or start, count); the same page asked for while on its
  way (up to 15 s) waits for that answer; failures are not kept.
- "New since you last looked": the legacy hall has no such marks and keeps no storage key for patch notes; nothing
  to port.

## Share links (`../share-links.ts`)

`/gemp-lotr/share/<kind>/<id>` (kinds patch-notes, errata, format, card) are server pages (`ShareRequestHandler`,
`SharePages`): their preview tags show where pasted, and they send the person on to the hall. The hall builds the
address (`shareUrl`: the page's origin) and copies it (`ClipboardWriter`): "Link copied" beside the button for 2 s,
or, when the browser won't copy, the link in a read-only field (aria "Share link"), selected, for 15 s. The button:
the icon `images/icons/share-from-square-solid.svg`, title "Copy a link to share (it shows a preview where you paste
it)". The server's targets are still `hall.html#...` (a server change for the switch, see the report).

## Differences from the legacy hall

- Copying falls back to showing the link, not to the deprecated copy command, where the Clipboard API is refused
  (a page not served over https).
- The months list's age is checked when a page is shown, not each time the arrows are drawn.
- Patch note colours are not drawn (see Markup).
