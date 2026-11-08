# gemp-lotr-frontend

The new Angular frontend, which replaces the jQuery client in `gemp-lotr-async/src/main/web` one page at a time.
The first page being ported is the **deck builder**. The hall's Deck-builder button opens it; `deckBuild.html` is still there.

|                                                         |                                                                                           |
| ------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| URL (hall button)                                       | `/gemp-lotr/ng/deckbuilder/` (e.g. http://localhost:17001/gemp-lotr/ng/deckbuilder/)      |
| Legacy deck builder (unchanged, still reachable by URL) | `/gemp-lotr/deckBuild.html`                                                               |
| Build output                                            | `../gemp-lotr-async/src/main/web/ng/` (gitignored, generated), served as `/gemp-lotr/ng/` |
| Stack                                                   | Angular 22 (standalone components, signals), Vitest                                       |

## One app, one page per route

The frontend is one Angular app, served under `/gemp-lotr/ng/`; each ported page is a lazy route of it,
`/gemp-lotr/ng/<page>/` (for now only `deckbuilder`; `/gemp-lotr/ng/` and unknown paths go to it).

- `src/main.ts` starts `AppComponent` (`src/ui/app/`, element `<gemp-app>`: just the `<router-outlet>`, with
  `display: contents`, so a page lays itself out as a child of `body`) with `ui/app/app.config.ts`: the providers every
  page shares (HttpClient, the router, the Notifier, the theme initializer).
- `ui/app/app.routes.ts`: one `loadComponent` route per page. A page's own providers go in its route's `providers`;
  services `providedIn: 'root'` (most of core) must only inject app-wide ones, which is why the Notifier is in
  `app.config.ts`.
- The server answers any GET under `/gemp-lotr/ng/` whose last path segment has no extension and that isn't a file
  with `ng/index.html` (`WebRequestHandler`), so a page's address can be reloaded or linked to. A missing asset
  (`.js`, `.png`, ...) is still a 404.

## Building with Maven (normal workflow)

Nothing extra needs installing. This module is part of the Maven reactor, and `frontend-maven-plugin` downloads Node into
`gemp-lotr-frontend/node/` (gitignored). It then runs `npm ci`, the unit tests (`npm test`, `test` phase) and the build (`npm run build`, `compile` phase);
both go through the npm scripts, so the card data is extracted first (see Card data below).
`gemp-lotr-async` depends on this module, so the Angular output is always in the web folder before the server is packaged.

```
mvn install                                  # everything, including frontend tests and build
mvn install -DskipTests                      # skips Java and frontend tests
mvn install -Dskip.frontend=true             # skips the frontend entirely (keeps the last build output)
```

On Alpine (the Docker app image), the plugin detects musl and downloads Node from unofficial-builds.nodejs.org.
That build needs `libstdc++`, which `docker/gemp_app.Dockerfile` installs.

The server reads static files from disk (its file cache lasts only one connection), so a rebuild is served at once,
without a restart.

## Deploying: the `frontend` Docker service

The build output is not in git, so after a `git pull` something has to run `ng build`. In the Docker setup that's the
`frontend` service (`docker/docker-compose.yml`, `docker/frontend-watch.sh`). It checks the frontend sources every 5
seconds (and the legacy card data files the card images are generated from) and, when they change, builds into `web/.ng-build/` while the old build stays live, then swaps
the new build in. A build that fails leaves the old one in place (see `docker logs gemp_frontend_1`). So deploying a
frontend change is just `git pull`, as with the legacy pages.

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

| Folder                                  | What it holds                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/core/`                             | State, rules and server calls, with no view code: nothing in it imports from the view folders, and it has no templates or styles, and only `core/platform/` touches the browser (`DOCUMENT`, `window`, cookies); `core-boundary.spec.ts` checks all of this.                                                                                                                                                                                                                                                                                                         |
| `src/core/api/`                         | `DeckbuilderApi`: every server call the deck builder makes, typed. Sent via `GempHttp`, which adds `participantId`, encodes form data strictly (a `+` stays a `+`), turns failures into `ApiError` and spots the opened-pack header. `parsers.ts` reads the server's XML.                                                                                                                                                                                                                                                                                            |
| `src/core/cards/`                       | Card images. `card-blueprint.ts` is a port of the legacy `Card.js` rules; the image tables come from `src/generated/card-data.json` via `card-data.ts` (see Card data below). `card-ref.ts` is what a card on the page stands for; `CardInfoService` holds the card shown in the card information.                                                                                                                                                                                                                                                                   |
| `src/core/collection/`                  | The collection pane's state: `card-filter.ts` (filter fields, Block/Set lists, culture buttons and the filter string, ported from `CardFilter.js`) and `CollectionService` (collection type, paging, the current page from `/collection/{type}`).                                                                                                                                                                                                                                                                                                                    |
| `src/core/formats/`                     | `FormatsService`: the formats from `/deck/formats`, fetched once.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `src/core/deck/`                        | The deck being edited: `deck-model.ts` (slots, piles, extra copies, the `deckContents` string), `format-options.ts` (the format list) and `DeckService` (format switching, Map/Race slots, deck stats, unsaved changes and the save prompts, New/Save/Rename/Copy/Convert). `DeckLibraryService`: the deck list and library, import and notes.                                                                                                                                                                                                                       |
| `src/core/delivery/`, `src/core/packs/` | `PackDeliveryService`: the opened-pack "New items" contents. `PacksService`: opening packs, the selection-pack choice and the open "New items" dialogs; `shown-cards.ts` gives their cards.                                                                                                                                                                                                                                                                                                                                                                          |
| `src/core/actions/`                     | `DeckbuilderActions`: what using a card means (add it, remove it, open a pack, show its details), whatever gesture the view maps to it.                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `src/core/notifier/`                    | `Notifier`: messages and questions to the player (the legacy alert/confirm/prompt), all asynchronous. The view provides it: `ui/shared/notifier/DialogNotifier` (shown by `gd-notifier`), in the page's own dialogs. Tests use `src/testing/fake-notifier.ts`.                                                                                                                                                                                                                                                                                                       |
| `src/core/session/`                     | `SessionService`: the optional `?participantId=` (admins acting as a player).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `src/core/platform/`                    | The browser, behind services: cookies, the page's query string, image preloading, opening links, and `ThemeService` (see Themes below).                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `src/themes/<name>/`                    | A theme's CSS (`theme.css`); its images and other files go in `public/themes/<name>/`. See Themes below.                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `src/ui/`                               | The view (see "The UI and its DOM contract" below): `ui/app/` (the app shell, see One app below), `ui/deckbuilder/` (the page component `DeckbuilderPageComponent`, element `<deckbuilder-app>`, the route `deckbuilder`; the page frame, the deck side, the collection filter and cards, the card information, the hover zoom, the dialogs) and `ui/shared/` (the card and its hover reporting, `fitCards`, the dialog, the notifier). Markup and interaction only; the themes draw it. `ui-boundary.spec.ts` checks it imports only core and ui and has no styles. |
| `src/testing/`                          | Test helpers and `server-responses/`, real server responses the parser tests run against.                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |

`index.html` is a plain HTML5 page (`<!doctype html>`, standards mode). The legacy page's XHTML Transitional doctype
put the browser in "limited quirks" mode, where an image or a textarea alone on a line adds no space below it for the
text's descenders. The classic theme matches it without the quirk: its images and buttons sit in flex or block boxes,
and the import dialog's textarea has `vertical-align: top`.

Settings kept in cookies (auto-zoom on/off, the card info zoom) are written with the legacy pages' path (`/gemp-lotr`),
so both deck builders share them.

No legacy stylesheet is linked: the themes draw the whole page.

`card-blueprint.parity.spec.ts` runs the legacy `Card.js` side by side with the port on over 50,000 blueprint IDs, so a
change to `Card.js` that isn't mirrored in `card-blueprint.ts` fails the build.

## Card data

The card image tables (`PC_Cards.js`, `hobbit.js`, `set40.js`, `cards/CardImages.js`, `cards/PackImages.js` in
`gemp-lotr-async/src/main/web/js/gemp-022/`) are still the legacy pages' `.js` files, and those files are the source:
add new card images there, as before. `scripts/extract-card-data.mjs` (`npm run card-data`) turns them into
`src/generated/card-data.json`, which is gitignored and regenerated on every build path:

- npm: `prestart`, `prebuild`, `prewatch` and `pretest` run `npm run card-data`;
- Maven: `pom.xml` runs `npm test` and `npm run build`, so the pre-scripts apply;
- Docker: `docker/frontend-watch.sh` runs `npm run card-data` before its `npx ng build` (which skips npm's pre-scripts),
  and also rebuilds when one of the legacy files changes.

`card-data.parity.spec.ts` fails if the JSON doesn't match the legacy files. Once the legacy pages are gone, the JSON
becomes the source: commit it, edit it directly and drop the script and its pre-scripts.

## Themes

The deck builder has two themes, `classic` (the legacy page's look) and `modern`. Each is a stylesheet built from
`src/themes/<name>/theme.css` into `theme-<name>.css` next to the app (`angular.json`: `inject: false`, so it is not
linked by default). `ThemeService` (`src/core/platform/theme.service.ts`) picks one:

1. `?theme=classic` or `?theme=modern` in the address, which is also remembered;
2. otherwise the `deckbuilderTheme` cookie (the player's choice, made in the toolbar's theme picker);
3. otherwise the device: phones and tablets (a coarse pointer and a screen whose short side is under 1100px, the rule
   of the legacy `mobileView.js`) get `modern`, everything else `classic`.

Unknown names are ignored. Each theme also has its viewport (`THEME_VIEWPORTS`): `modern` is laid out at the device's
width (`width=device-width, initial-scale=1, viewport-fit=cover`), `classic` keeps the legacy page's default. The app starts only once the theme's stylesheet has loaded (an app initializer), so the page
never shows unstyled. Switching loads the new stylesheet first and removes the old one after; only the CSS changes, so
the deck being edited stays as it is.

Scoping: only one theme's stylesheet is on the page at a time, so theme CSS is written without a theme prefix.
`<html data-theme="...">` is also set, for the rare rule that must know the theme from outside a theme stylesheet.
The theme files are not hashed (their names are fixed so they can be linked at runtime); the server revalidates them
with an ETag, like the legacy stylesheets.

### Adding a theme

1. Create `src/themes/<name>/theme.css`. It styles the `.gd-*` markup described below and nothing else; copy
   `modern` or `classic` as a starting point. Images and other files go in `public/themes/<name>/` (served as
   `/gemp-lotr/ng/themes/<name>/...`; the classic theme refers to them by that absolute path).
2. Add a bundle to `angular.json` (`projects.deckbuilder.architect.build.options.styles`):
   `{ "input": "src/themes/<name>/theme.css", "bundleName": "theme-<name>", "inject": false }`.
3. Register it in `src/core/platform/theme.service.ts`: add the name to `THEMES`, a label to `THEME_LABELS` (shown in
   the toolbar's picker) and an entry to `THEME_VIEWPORTS` (`null` for the browser's default). Change `DEFAULT_THEME` or
   `MOBILE_THEME` only if it should be picked by default.
4. Show or hide the optional affordances (the tab bar, the cards' ⓘ, ...) and set the `fitCards` properties for its
   card boxes. Check it with `npm run build` and `visual-check/stubbed.mjs --angular '/gemp-lotr/ng/deckbuilder/?theme=<name>'`.

## The UI and its DOM contract

`src/ui` is the deck builder's view; it replaced the first Angular view (`src/deckbuilder-app`, which reused the legacy
stylesheets and element IDs). Its providers are in `ui/app/app.config.ts`, whose Notifier is
`DialogNotifier`: messages and questions in the page's own dialog instead of the browser's.

The rules:

- **Layers.** `core/` holds state, rules and server calls; `ui/` holds semantic markup and interaction; `themes/<name>/`
  holds every visual rule, layout included. `ui/` components have no styles (only `display: contents` on a few hosts)
  and no pixel sizes: anything a theme sizes comes from CSS, with custom properties where the markup needs a number
  (e.g. `--gd-card-height` from `fitCards`). `core/core-boundary.spec.ts` and `ui/ui-boundary.spec.ts` enforce this.
- **Markup.** Native elements (`button`, `select`, `input type=range`, `label`, `dialog`); class names
  `gd-<block>__<element>`; state in `data-*` attributes (`data-role`, `data-orientation`, `data-active`, ...) and ARIA,
  never in class names.
- **Affordances.** Templates hold the union of the affordances any theme needs, for example the pane tab bar
  (`.gd-page__tabs`) and the cards' ⓘ spot (`.gd-card__info`); each theme shows or hides them in CSS. A theme never
  needs its own template, and a new affordance is added to the template (hidden by the themes that don't use it).
- **Interaction** is theme-neutral and each component handles its own events (no page-wide listeners).

| Element                          | Contract                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `.gd-page` (`<deckbuilder-app>`) | `data-collection` = open, closed; `data-pane` = deck, collection (the tab bar's choice, for a theme that shows one pane at a time); `--gd-collection-width` once resized (the theme clamps it). Children: `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`; drag or arrow keys resize, Enter toggles; `data-dragging`) with `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection`, and the tab bar `div.gd-page__tabs` (`role=tablist`) with a `button.gd-page__tab` per pane (`role=tab`, `data-pane`, `aria-selected`; choosing the collection also opens it).                                                                                                                                                                                                                                                                                                                                                                                                             |
| `.gd-filter`                     | `.gd-pager` (`button.gd-pager__previous`/`__next` with `data-icon`, `input[type=range].gd-pager__size`); `.gd-filter__fields` with `.gd-filter__basic` and `.gd-filter__advanced` (`.gd-filter__advanced-title`); each field `label.gd-field` (`data-field`, `data-kind` = select/text/numeric, `data-wide`) with `.gd-field__label` and `.gd-field__control`; `.gd-filter__resets` (`button.gd-button[data-action]`); `.gd-cultures` > `.gd-cultures__group[data-group]` > `label.gd-culture` (`data-culture`, `data-active`, `data-icon-kind`) with a visually hidden `input.gd-culture__input` and `img.gd-culture__icon`.                                                                                                                                                                                                                                                                                                                                                                                       |
| `.gd-collection`                 | `.gd-collection__cards` (the `fitCards` box, `data-count`) with a `button.gd-card` per item, packs first.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `button.gd-card`                 | `data-role` = card, pack, selection, placeholder, copy (another copy of a draw deck card, drawn behind it); `data-orientation` = vertical, horizontal; `data-errata`, `data-foil`, `data-borderless`. Inside: `img.gd-card__image`, and when they apply `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay`, `.gd-card__count`; always `.gd-card__info`, an ⓘ spot (part of the button) a theme may show. Click/Enter/Space uses the card; a click on `.gd-card__info`, shift-click, right-click, a long press (touch) or dragging it up 20px (mouse) shows its details. Mouse hover and presses are reported to `CardHover` (for the hover zoom).                                                                                                                                                                                                                                                                                                                                                      |
| `[gdFitCards]`                   | Sets `--gd-card-height` (px) on its box so the cards fit, from the box's size and the theme's `--gd-card-max-height` (default 200px), `--gd-card-gap` (5px) and `--gd-card-fit`: `contain` (largest that fits), `rows` or `none` (always the maximum; the box scrolls). Each entry is a `FitCard` (landscape or not, and the copies drawn behind it, a fifth of each showing). It also sets `--gd-card-offset` (px): the space above the rows when centred, as the legacy groups placed them; a theme may move the cards by it (moving, not padding, so the box's size stays). `rows` sizes like the legacy NormalCardGroup: it tries 1, 2, 3... rows, each `(box height - gaps) / rows` high (capped at the maximum), and keeps the first that holds every card, so cards can come out a little smaller than with `contain`; one row is used when two rows of it wouldn't fit, and the height can have a fraction as it did there. The classic theme uses it so the collection and the deck match the legacy page. |
| `dialog.gd-card-info`            | Modal native dialog; `--gd-card-info-zoom` (0.4 to 1). `.gd-card-info__header` (`.gd-card-info__title`, `button.gd-card-info__close`), `input[type=range].gd-card-info__zoom` (`autofocus`), `.gd-card-info__card` (`data-orientation`, `data-flipped` = back/upside-down, `data-flippable`, `data-foil`) with `img.gd-card-info__image`, `__overlay`, `__foil`; `a.gd-card-info__wiki`. Clicking the card flips it; any other click, the close button or Escape closes it.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `.gd-deck`                       | `gd-deck-toolbar.gd-toolbar`, a `section.gd-slot` per slot, a `section.gd-pile` per draw deck side, `gd-deck-stats.gd-stats`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `.gd-toolbar`                    | `role=toolbar`. `.gd-toolbar__deck-actions` with `button.gd-button[data-action]` (new, save, rename, copy, import, library, deck-list, convert-errata; `title`/`aria-label`), `select.gd-toolbar__format`, `button.gd-button[data-action=notes]`, `.gd-toolbar__deck-name` (`data-modified`, legacy "*name - modified" text), `button.gd-button[data-action=auto-zoom]` (`aria-pressed`; absent on touch devices), `select.gd-toolbar__collection`, `select.gd-toolbar__theme`. The theme draws an icon per `data-action`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `section.gd-slot`                | `data-slot` = ring-bearer, ring, map, race, sites; `hidden` when the format has none; `data-count`. `.gd-slot__label` (a `button` that selects for the slot; a `div` for the display-only race) with `.gd-slot__title` and `.gd-slot__hint`; the Map's `a.gd-slot__link`; `.gd-slot__cards` with a `button.gd-card` per card (a fitCards box, except the sites, which get `--gd-slot-count`). Clicking a card removes it; clicking a site also shows the sites in the collection, as before.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `section.gd-pile`                | `data-side` = free-peoples, shadow; `data-count` (copies included). `.gd-pile__cards` (fitCards box) with a `.gd-stack` per card: its other copies (`data-role=copy`, oldest first), then the card.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `.gd-stats`                      | `aria-live=polite`; a `.gd-stats__line` per line (empty = blank line) with `strong` labels and `span` parts, `data-tone` = error, valid, warning. Rendered from data (`DeckService.stats`, parsed from the server's fragment by `core/deck/deck-stats.ts`); the server's markup never reaches the page.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `dialog.gd-dialog`               | Every dialog but the card information (`ui/shared/dialog`, `gd-dialog`): a native `<dialog>`, modal unless said below. `data-dialog` = deck-list, library, import, notes, selection, delivery, alert, confirm, prompt; `data-placed` once moved (dragging the title bar) or pinned (pressing its own edge, so a CSS `resize` grows it right and down), with `--gd-dialog-left`/`--gd-dialog-top` (px); `--gd-dialog-layer` (grows each time a dialog opens or is pressed: the stacking of non-modal ones). `header.gd-dialog__header` (`h2.gd-dialog__title`, `button.gd-dialog__close`), `.gd-dialog__body`, then `.gd-dialog__actions` where there are actions. Closes on the close button or Escape. Focuses an `autofocus` control, else the content's first control, else the actions' first; import, selection and delivery focus the dialog itself, as the legacy ones did. Opened again, it is placed afresh.                                                                                               |
| Deck list, library               | `ul.gd-deck-list` > `li.gd-deck-list__item`: a `button.gd-button` per `data-action` (open, rename, share, delete; library: open, deck-list; `title` names it), then `.gd-deck-list__name` with `b.gd-deck-list__format` ("[format]") and the name as text.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| Import (not modal), notes        | `.gd-import` with `textarea.gd-import__text` and `button.gd-button[data-action=import]` ("Import"; empty again each time it is opened from the toolbar). `textarea.gd-notes__text` (kept on change and on close).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Selection, delivery (not modal)  | `.gd-shown-cards` (fitCards box, `data-count`) with a `button.gd-card` per card: selection choices (`data-role=card`; using one picks it), delivered packs and cards (`pack`, `card`, with counts). A "New items" dialog stays on the page, closed, once shown, keeping its size.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Notifier (`gd-notifier`)         | `p.gd-notifier__message` (line breaks kept), `input.gd-notifier__input` (prompt; focused, selected; Enter is OK), `.gd-dialog__actions` with `button.gd-button[data-action=ok]` and, for confirm and prompt, `[data-action=cancel]`. Escape and the close button are Cancel. One at a time, in the order asked.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `div.gd-zoom`                    | The hover zoom: `aria-hidden`, `hidden` when nothing is zoomed; `--gd-zoom-left`/`-top`/`-width`/`-height` (px; placed and sized as the legacy AutoZoom did), `data-orientation`, `data-flipped` (back; Shift), `data-foil`; `img.gd-zoom__image`, `__overlay`, `__foil`. Shown after the mouse rests 500ms on a card (at once within 500ms of the last zoom); hidden by a press, when the toolbar's magnifier is off (`AutoZoomSetting`, legacy cookie) or the card information is open.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

## Tests and the visual check

```
npm test                    # unit tests (Vitest), watch mode
npm test -- --watch=false   # once, as Maven runs them
npm run build               # both theme bundles: theme-classic.css, theme-modern.css
```

`visual-check/` is a Playwright check, separate from the Maven build (see `visual-check/README.md`):

- `stubbed.mjs` needs no server: after `npm run build`, `cd visual-check && node stubbed.mjs` screenshots the legacy
  page and this one side by side (`--angular '...?theme=modern' --touch` for the modern theme on a phone);
- `run.mjs` (`npm run check`) compares both pages against a running server; it still uses the legacy element IDs.
