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
| `src/app/core/deck/` | Deck helpers, e.g. the `deckContents` string the server expects. |
| `src/app/core/delivery/` | `PackDeliveryService`: the opened-pack "New items" contents. |
| `src/app/core/session/` | `SessionService`: the optional `?participantId=` (admins acting as a player). |
| `src/app/layout/` | The page frame: `page-layout.ts` reproduces the legacy `jquery.layout` panes (deck, collection, resizer bar), and `deck-layout.ts` is the legacy `layoutUI` positioning of the deck slots. |
| `src/app/deck/` | Deck builder components, e.g. the top toolbar. |
| `src/app/shared/` | `gempUiButton`: jQuery UI button classes and hover/pressed states, so the dark-hive theme applies. |
| `src/testing/` | Test helpers and `server-responses/`, real server responses the parser tests run against. |

The page uses the legacy stylesheets where they are (linked in `src/index.html` from `/gemp-lotr/css/...`), keeping the
legacy element IDs and classes, so it looks the same and later fixes to those stylesheets apply to both deck builders.

`card-blueprint.parity.spec.ts` runs the legacy `Card.js` side by side with the port on over 50,000 blueprint IDs, so a
change to `Card.js` that isn't mirrored in `card-blueprint.ts` fails the build.

## Tests

```
npm test               # watch mode
npm test -- --watch=false
```
