# Deck builder visual check

> **Note:** `run.mjs` and `scenarios.mjs` still drive both pages through the legacy element IDs (`#collection-display`,
> `#deckListBut`, ...), which the Angular page no longer has since its first view was replaced by `src/ui`; until the
> scenarios are given the new UI's selectors (as `stubbed.mjs` does), use `stubbed.mjs` below.

Compares the Angular deck builder (`/gemp-lotr/ng/deckbuilder/`) with the legacy one (`/gemp-lotr/deckBuild.html`) in a
real browser (Chromium, via Playwright). Every scenario in `scenarios.mjs` runs on a fresh copy of each page. That
covers the collection filters and paging, building a deck, formats, every dialog, card information and the hover
zoom, at several window sizes. Both pages must then:

- look the same: full-page screenshots, with at most `--tolerance` pixels (default 60) differing by more than 2 colour
  levels (anti-aliasing of rounded corners and icons varies by a few pixels between two loads of the same page);
- ask the server for the same collection page and the same deck stats.

It writes `report/index.html` (screenshots of both pages and the differences in red) and exits with 1 if anything
differs. It is not part of the Maven build: run it by hand after changing the deck builder or the legacy files it uses.

## Running it

Needs a running Gemp server that serves the current build of the Angular page (rebuild with Maven or `ng build`, then
restart the server: it caches static files). Use a test server and a test player: the check saves a deck called
"Visual check deck" for that player, which the deck list scenarios open.

```
cd gemp-lotr/gemp-lotr-frontend/visual-check
npm ci
npx playwright install chromium
GEMP_USER=tester GEMP_PASSWORD=secret npm run check -- --server http://localhost:17001
```

| Option | Default | |
|---|---|---|
| `--server` | `$GEMP_SERVER` or `http://localhost:17001` | The Gemp server |
| `--sizes` | `1280x800,1366x768,1920x1080` | Window sizes |
| `--only` | all | Comma-separated scenario names |
| `--mutating` | off | Also run the scenarios that change the account (saving, renaming and copying decks) |
| `--workers` | 4 | Scenarios run in parallel |
| `--tolerance` | 60 | Differing pixels allowed |
| `--out` | `report` | Report folder |

For a fuller check, give the test player a few packs and a selection pack in "My cards" ("my-cards" and "selection-pack"
show them), and have some library decks (decks of the "Librarian" player, for "open-library-deck").

## Things to know

- **One login for all pages.** The check logs the player in once and shares that session. Logging the same player in
  several times at once can hang the whole server: `DbPlayerDAO.loginUser` holds a database connection while
  `doesPlayerHaveTrophies` asks the pool for another, so enough simultaneous logins take every connection and wait
  forever (found while building this check; it is a server bug, not a deck builder one).
- **Collection answers in order.** The legacy page showed whichever collection answer arrived last, so an older request
  answered late replaced the newer page (fixed in the Angular page, which keeps only the newest). The check delivers
  collection answers in request order on both pages, so the comparison doesn't depend on the server's timing.
- **Fixed legacy bugs are not compared.** The scenarios avoid the few states where the Angular page differs on purpose
  because a legacy bug was fixed (listed in the deck builder's issue, e.g. stale collection counts after "New deck", or
  a disabled button left highlighted). Opening packs is not part of the check: it uses up the test player's packs, and
  each page would open a different one.
- **Not covered:** the Race to Mount Doom slot (needs a running RTMD league with the test player in it).

## Without a server: `stubbed.mjs`

`stubbed.mjs` screenshots the legacy page and the Angular one in the classic theme (`?theme=classic`) side by side with no Gemp server: both are served from disk (the legacy web root and the Angular build output) and the
server's answers come from `src/testing/server-responses` plus a generated collection. Card images are replaced by
labelled placeholders on both pages. It compares nothing by itself; look at the pairs in `report-stubbed/`. Scenarios: `collection`, `second-page`, `shadow-filter`, `deck` (a few cards, copies and sites in the deck, with the
stubbed stats), `zoom-deck` and `zoom-collection` (the hover zoom), `card-info`, and the dialogs: `deck-list`,
`library`, `import`, `import-typed`, `notes`, `selection` (the selection pack's "Choose one"), `delivery` ("New items",
from a stubbed pack delivery), `pane-collection`, `pane-deck` and `info-button` (the tab bar and the cards' ⓘ, for a
theme that shows them; the legacy side shows the page as loaded), `confirm` and `prompt` (deleting and renaming a deck; the legacy page used the
browser's own dialogs, which screenshots don't show, so its side shows only the page behind). A scenario can answer
server requests its own way (`answers`).

```
npm run build                      # in gemp-lotr-frontend
cd visual-check && node stubbed.mjs --sizes 1440x900,1280x800
node stubbed.mjs --angular '/gemp-lotr/ng/deckbuilder/?theme=modern' --sizes 390x844 --touch   # a phone
```

Playwright comes from `visual-check/node_modules` (`npm ci`). Where it is only installed globally, as in some
containers, make it resolvable with `npm link playwright` in `visual-check` (it must find a browser for that version; a
global install usually brings its own).
