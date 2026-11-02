# gemp-lotr-frontend

The new Angular frontend, which replaces the jQuery client in `gemp-lotr-async/src/main/web` one page at a time.
The first page being ported is the **deck builder**. For now it is an opt-in preview and does not replace `deckBuild.html`.

| | |
|---|---|
| Opt-in URL | `/gemp-lotr/ng/deckbuilder/` (e.g. http://localhost:17001/gemp-lotr/ng/deckbuilder/) |
| Legacy deck builder (unchanged) | `/gemp-lotr/deckBuild.html` |
| Build output | `../gemp-lotr-async/src/main/web/ng/deckbuilder/` (gitignored, generated) |
| Stack | Angular 22 (standalone components, signals), Vitest |

## Building with Maven (normal workflow)

Nothing extra needs installing. This module is part of the Maven reactor, and `frontend-maven-plugin` downloads Node into
`gemp-lotr-frontend/node/` (gitignored). It then runs `npm ci`, the unit tests (`test` phase) and `ng build` (`compile` phase).
`gemp-lotr-async` depends on this module, so the Angular output is always in the web folder before the server is packaged.

```
mvn install                                  # everything, including frontend tests and build
mvn install -DskipTests                      # skips Java and frontend tests
mvn install -Dskip.frontend=true             # skips the frontend entirely (keeps the last build output)
```

On Alpine (the Docker app image), the plugin detects musl and downloads Node from unofficial-builds.nodejs.org.
That build needs `libstdc++`, which `docker/gemp_app.Dockerfile` installs.

The server caches static files in memory until it restarts, so restart the server after a rebuild that it should serve.
`ng serve` (below) avoids this during development.

## Developing with `ng serve`

Requires Node 24 (`node -v`). Start a Gemp server first, for example the Docker setup from `gemp-lotr/docker`.

```
cd gemp-lotr/gemp-lotr-frontend
npm ci
npm start                                    # http://localhost:4200/gemp-lotr/ng/deckbuilder/
GEMP_SERVER=http://localhost:17001 npm start # other server (this is the default)
```

`proxy.conf.mjs` forwards `/gemp-lotr-server/*` (API) and every `/gemp-lotr/*` path except the app's own (images and
legacy CSS/JS) to that server. Requests are same-origin, so the login cookie works: log in through the
regular hall at `http://localhost:4200/gemp-lotr/` and then open the deck builder URL.

## Code layout

| Folder | What it holds |
|---|---|
| `src/app/core/api/` | `DeckbuilderApi`: every server call the deck builder makes, typed. Sent via `GempHttp`, which adds `participantId`, encodes form data strictly (a `+` stays a `+`), turns failures into `ApiError` and spots the opened-pack header. `parsers.ts` reads the server's XML. |
| `src/app/core/cards/` | Card images. `card-blueprint.ts` is a port of the legacy `Card.js` rules; the image tables still come from the legacy files (`PC_Cards.js`, `hobbit.js`, `set40.js`, `cards/CardImages.js`, `cards/PackImages.js`), loaded at startup by `LegacyCardDataService`. Add new card images there, as before. |
| `src/app/core/collection/` | The collection pane's state: `card-filter.ts` (filter fields, Block/Set lists, culture buttons and the filter string, ported from `CardFilter.js`) and `CollectionService` (collection type, paging, the current page from `/collection/{type}`). |
| `src/app/core/formats/` | `FormatsService`: the formats from `/deck/formats`, fetched once. |
| `src/app/core/deck/` | The deck being edited: `deck-model.ts` (slots, piles, extra copies, the `deckContents` string), `format-options.ts` (the format list) and `DeckService` (format switching, Map/Race slots, deck stats, unsaved changes and the save prompts, New/Save/Rename/Copy/Convert). |
| `src/app/core/delivery/` | `PackDeliveryService`: the opened-pack "New items" contents. |
| `src/app/core/session/` | `SessionService`: the optional `?participantId=` (admins acting as a player). |
| `src/app/layout/` | The page frame: `page-layout.ts` reproduces the legacy `jquery.layout` panes (deck, collection, resizer bar), and `deck-layout.ts` is the legacy `layoutUI` positioning of the deck slots. |
| `src/app/cards/` | Card display: `gempCard` (the legacy card element with its errata, foil, count, border and meta-site layers), `gemp-card-display` (the large card of the info dialog and the hover zoom), `card-group-layout.ts` (the `NormalCardGroup` rows and the `VerticalBarGroup` sites column) and `CardRegistry` (what each card element stands for). |
| `src/app/collection/` | The collection pane: the filter (`gempCardFilter`) and the cards of the current page (`gempCollectionDisplay`). |
| `src/app/deck/` | The deck pane (`gempDeckArea`: slots, piles, stats), the top toolbar, and `CardClicksService`, the page-wide card mouse handling (click adds/removes; shift-click, right-click or dragging up asks for card details). |
| `src/app/dialogs/` | The dialogs: deck list and library, import, notes (`DeckDialogsService`), card information (`CardInfoService`), the hover zoom (`AutoZoomService`), pack opening, the selection-pack chooser and the "New items" popups (`PacksService`). |
| `src/app/shared/` | jQuery UI look-alikes, so the dark-hive theme applies: `gempUiButton` (button classes and hover/pressed states), `gempUiSlider` (the slider) and `gemp-ui-dialog` (the dialog: centred, draggable, resizable, modal overlay, Escape). |
| `src/testing/` | Test helpers and `server-responses/`, real server responses the parser tests run against. |

`index.html` keeps the legacy page's XHTML Transitional doctype. It puts the browser in "limited quirks" mode, where an
image alone on a line adds no space below it; with `<!doctype html>` the culture buttons are 4px taller.

Settings kept in cookies (auto-zoom on/off, the card info zoom) are written with the legacy pages' path (`/gemp-lotr`),
so both deck builders share them.

The page uses the legacy stylesheets where they are (linked in `src/index.html` from `/gemp-lotr/css/...`), keeping the
legacy element IDs and classes, so it looks the same and later fixes to those stylesheets apply to both deck builders.

`card-group-layout.spec.ts` checks the card layouts against layouts recorded from the legacy `NormalCardGroup` and
`VerticalBarGroup` (`src/testing/legacy-*-layouts.json`; the matching `*.generator.js` scripts record them again in the
legacy page's browser console).

`card-blueprint.parity.spec.ts` runs the legacy `Card.js` side by side with the port on over 50,000 blueprint IDs, so a
change to `Card.js` that isn't mirrored in `card-blueprint.ts` fails the build.

## Tests

```
npm test               # watch mode
npm test -- --watch=false
```
