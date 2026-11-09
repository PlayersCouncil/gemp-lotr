# Deck builder visual check

Compares the Angular deck builder (`/gemp-lotr/ng/deckbuilder/`) with the legacy one (`/gemp-lotr/deckBuild.html`) in a
real browser (Chromium, via Playwright). Every scenario in `scenarios.mjs` runs on a fresh copy of each page. That
covers the collection filters and paging, building a deck, formats, every dialog, card information and the hover
zoom, at several window sizes. Both pages must then:

- look the same: full-page screenshots, with at most `--tolerance` pixels (default 60) differing by more than 2 colour
  levels (anti-aliasing of rounded corners and icons varies by a few pixels between two loads of the same page);
- ask the server for the same collection page and the same deck stats.

Each scenario does the same thing on both pages through `controls.mjs`: every control the scenarios use, as a pair of
selectors, the legacy page's element IDs and the Angular page's DOM contract (the `.gd-*` markup and its `data-*`
state, see the frontend README). Both pages ask their questions in the browser's own
dialogs, and the check answers them from the scenario's `answers`. It also answers `gd-notifier` dialogs, for when the
redesign brings them back.

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
GEMP_USER=tester GEMP_PASSWORD=secret npm run check -- --theme=modern --only start,deck-list --sizes 1366x768
```

The legacy page is the classic look, so with `--theme=classic` (the default) both pages should match; with
`--theme=modern` the screenshots are expected to differ and the report is for looking at them side by side, while
the server requests must still be the same.

| Option                  | Default                                                   |                                                                                             |
| ----------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| `--server`              | `$GEMP_SERVER` or `http://localhost:17001`                | The Gemp server                                                                             |
| `--theme`               | `classic`                                                 | The Angular page's theme, `classic` or `modern` (its `?theme=`, unless `--angular` has one) |
| `--legacy`, `--angular` | `/gemp-lotr/deckBuild.html`, `/gemp-lotr/ng/deckbuilder/` | The two pages' paths                                                                        |
| `--sizes`               | `1280x800,1366x768,1920x1080`                             | Window sizes                                                                                |
| `--only`                | all                                                       | Comma-separated scenario names                                                              |
| `--mutating`            | off                                                       | Also run the scenarios that change the account (saving, renaming and copying decks)         |
| `--workers`             | 4                                                         | Scenarios run in parallel                                                                   |
| `--tolerance`           | 60                                                        | Differing pixels allowed                                                                    |
| `--out`                 | `report`                                                  | Report folder                                                                               |

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

## Without a server or a login

`npm run check:stubbed` runs the same comparison against `stub-server.mjs`: both pages are served from disk and the
server's answers are stubbed, with placeholder card art. It needs a build of the Angular page and writes
`report-stubbed/index.html`. It covers the layout and the requests quickly, but not real card art, a real server's
data or the scenarios that change the account, so run the real check before relying on it.

Playwright comes from `visual-check/node_modules` (`npm ci`). Where it is only installed globally, as in some
containers, make it resolvable with `npm link playwright` in `visual-check` (it must find a browser for that version; a
global install usually brings its own).
