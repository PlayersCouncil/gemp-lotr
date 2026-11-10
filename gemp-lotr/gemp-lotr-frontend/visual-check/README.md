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

## What is compared

Every `pause` of a scenario is a step. After the page loads, after each step and at the end, both pages must agree on:

- the screen;
- the server requests since the last step, with their contents, in order for each address. A request the page cancelled
  doesn't count, and for the collection only the newest does (only the newest answer is shown). Deck stats are sent on a
  timer, so the last one of the scenario is what is compared;
- the browser's own dialogs, with their text;
- what a screenshot doesn't show: which control has the keyboard focus, the cursor and tooltip under the mouse, each
  control's disabled state and value, the deck name, the dialog's title and (at the end) the deck stats;
- for scenarios that change the account, every saved deck afterwards, each page having started from the same ones.

Scenarios with `timing: true` depend on timers, so between steps they only compare what doesn't need waiting for.

`accepted.mjs` lists the differences the owner has accepted, each limited to one area of the page and a number of pixels.
Everything else fails, so a clean run means no difference nobody has decided on.

`npm run check:selftest` proves the check itself works: `injections.mjs` puts deliberate bugs into the Angular page, one
at a time, and each must be reported in the way it should be, and not be reported without the bug.

## All of it, and reading the results

`npm run check:all` runs everything in order on throwaway copies: the frontend's lint, types, formatting and unit tests
(`npm run verify` in the frontend), 200 generated sessions on the legacy page and the Angular page side by side
(`npm run test:parity` in the frontend, see its README), the decks saved by the legacy page (`check:legacy-decks`), the
Java server's build and web tests (`check:server-build`, which never replaces the
jar a running server uses), the self-test, the comparison, the control-and-gesture matrix and the deploy test. `--quick`
leaves out the matrix.

`npm run check:matrix` tests every control against every gesture it can get, in each state it appears in (`matrix.mjs`),
and lists what is really on both pages: a control with no test, or on one page only, fails it.

`npm run check:mutation` (not part of `check:all`, about an hour) is mutation testing of the deck builder's logic; see
the frontend README.

`npm run inspect -- summary <report>` groups a report's differences by cause, `where <report> <name>` says where on the
screen one is, and `crop <report> <name> x y w h [zoom]` makes one picture of the same area of both pages.

## On a throwaway server, without a login

`npm run check:local -- --mutating` runs the whole check against a server of its own (`local-server/compose.yml`), and
needs nothing from whoever runs it but Docker, with the `gemp_app` image and a built `web.jar`. It builds the Angular
page, starts the server on an empty in-memory database with its own port (17101, or `GEMP_CHECK_PORT`), creates the test
players with a random password that is never written down (only its hash reaches the database), adds the test data
(`local-server/seed.mjs`: packs, a selection pack, a foil, an errata card, a promised prize, trophies, library decks and
a running Race to Mount Doom league the player is in), runs `run.mjs` and removes everything again. The development
server isn't touched, and a rebuild elsewhere can't change the page during a run. The report goes to `report-local/`.

`--keep` leaves the server up afterwards (`npm run check:local:down` stops it), `--skip-build` reuses the last build, and
every other option goes to `run.mjs`.

Both this and `check:deploy` below use `gemp-lotr-async/target/web.jar`; set `CHECK_WEB_JAR` to test another server build.

## Decks saved by the legacy page

`npm run check:legacy-decks` proves that decks saved with the legacy deck builder keep working in the Angular one, on the
throwaway server (`legacy-decks.mjs`). It makes the decks a live database holds: through the server's save as the
legacy page sent them (every kind of format, a map, errata and foils, names and notes with markup, quotes, line breaks
and other alphabets, the "null" notes it saved, notes escaped once and twice as it left them when it saved a deck
again), a Race to Mount Doom deck made on the legacy page itself, and straight into the database what only an older
server wrote (a format that no longer exists, a deck without contents). Then, for every deck: what the legacy page
shows, what the Angular page shows (deck name, format, every card, stats, notes), saving it on the Angular page must
leave what the server stores exactly as it was, and the legacy page must show it as before. The legacy page showed
notes one level of escaping deeper than the server stored them; the Angular page shows them as stored. Report:
`report-legacy-decks/results.json`.

## Deploying a frontend change

`npm run check:deploy` tests what `docker/frontend-watch.sh` promises, on a throwaway copy of the repository with its own
containers (`local-server/deploy-compose.yml`, port 17102): with the Java server running, a real `git pull` must rebuild
the app and swap it in. It covers a brand-new container, a frontend change, a pull that changes no frontend file, a pull
that breaks the build and the one that fixes it, a changed `package-lock.json`, a change to the legacy card data and a
recreated container, and loads the page continuously during each swap to catch any moment players can't.

## Without a server or a login

`npm run check:stubbed` runs the same comparison against `stub-server.mjs`: both pages are served from disk and the
server's answers are stubbed, with placeholder card art. It needs a build of the Angular page and writes
`report-stubbed/index.html`. It covers the layout and the requests quickly, but not real card art, a real server's
data or the scenarios that change the account, so run the real check before relying on it.

Playwright comes from `visual-check/node_modules` (`npm ci`). Where it is only installed globally, as in some
containers, make it resolvable with `npm link playwright` in `visual-check` (it must find a browser for that version; a
global install usually brings its own).
