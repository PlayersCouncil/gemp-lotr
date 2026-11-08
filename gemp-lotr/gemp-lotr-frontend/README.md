# gemp-lotr-frontend

The new Angular frontend, which replaces the jQuery client in `gemp-lotr-async/src/main/web` one page at a time.
The first page being ported is the **deck builder**. The hall's Deck-builder button opens it; `deckBuild.html` is still there.

| | |
|---|---|
| URL (hall button) | `/gemp-lotr/ng/deckbuilder/` (e.g. http://localhost:17001/gemp-lotr/ng/deckbuilder/) |
| Legacy deck builder (unchanged, still reachable by URL) | `/gemp-lotr/deckBuild.html` |
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

The server reads static files from disk (its file cache lasts only one connection), so a rebuild is served at once,
without a restart.

## Deploying: the `frontend` Docker service

The build output is not in git, so after a `git pull` something has to run `ng build`. In the Docker setup that's the
`frontend` service (`docker/docker-compose.yml`, `docker/frontend-watch.sh`). It checks the frontend sources every 5
seconds (and the legacy card data files the card images are generated from) and, when they change, builds into `web/ng/.deckbuilder-build/` while the old build stays live, then swaps
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

| Folder | What it holds |
|---|---|
| `src/deckbuilder-app/deckbuilder-app.*` | The deck builder's root component (`DeckbuilderApp`, element `<deckbuilder-app>`), its page frame template and its providers (`deckbuilder-app.config.ts`); `src/main.ts` starts it. Named for the deck builder so it can sit beside other pages in the coming whole-site Angular frontend. |
| `src/core/` | State, rules and server calls, with no view code: nothing in it imports from the view folders, and it has no templates or styles, and only `core/platform/` touches the browser (`DOCUMENT`, `window`, cookies); `core-boundary.spec.ts` checks all of this. |
| `src/core/api/` | `DeckbuilderApi`: every server call the deck builder makes, typed. Sent via `GempHttp`, which adds `participantId`, encodes form data strictly (a `+` stays a `+`), turns failures into `ApiError` and spots the opened-pack header. `parsers.ts` reads the server's XML. |
| `src/core/cards/` | Card images. `card-blueprint.ts` is a port of the legacy `Card.js` rules; the image tables still come from the legacy files (`PC_Cards.js`, `hobbit.js`, `set40.js`, `cards/CardImages.js`, `cards/PackImages.js`): `scripts/extract-card-data.mjs` turns them into `src/generated/card-data.json` (not committed) before every `npm run build`, `npm start` and `npm test`, and `card-data.ts` provides it. Add new card images in the legacy files, as before; `card-data.parity.spec.ts` fails if the JSON doesn't match them. `card-ref.ts` is what a card on the page stands for; `CardInfoService` holds the card shown in the card information. |
| `src/core/collection/` | The collection pane's state: `card-filter.ts` (filter fields, Block/Set lists, culture buttons and the filter string, ported from `CardFilter.js`) and `CollectionService` (collection type, paging, the current page from `/collection/{type}`). |
| `src/core/formats/` | `FormatsService`: the formats from `/deck/formats`, fetched once. |
| `src/core/deck/` | The deck being edited: `deck-model.ts` (slots, piles, extra copies, the `deckContents` string), `format-options.ts` (the format list) and `DeckService` (format switching, Map/Race slots, deck stats, unsaved changes and the save prompts, New/Save/Rename/Copy/Convert). `DeckLibraryService`: the deck list and library, import and notes. |
| `src/core/delivery/`, `src/core/packs/` | `PackDeliveryService`: the opened-pack "New items" contents. `PacksService`: opening packs, the selection-pack choice and the open "New items" dialogs; `shown-cards.ts` gives their cards. |
| `src/core/actions/` | `DeckbuilderActions`: what using a card means (add it, remove it, open a pack, show its details), whatever gesture the view maps to it. |
| `src/core/notifier/` | `Notifier`: messages and questions to the player (the legacy alert/confirm/prompt), all asynchronous. The view provides it: the new UI's `ui/shared/notifier/DialogNotifier` (shown by `gd-notifier`), the old view's `deckbuilder-app/notifier/WindowNotifier` (the browser's dialogs). Tests use `src/testing/fake-notifier.ts`. |
| `src/core/session/` | `SessionService`: the optional `?participantId=` (admins acting as a player). |
| `src/core/platform/` | The browser, behind services: cookies, the page's query string, image preloading, opening links, and `ThemeService` (see Themes below). |
| `src/themes/<name>/` | A theme's CSS (`theme.css`); its images and other files go in `public/themes/<name>/`. See Themes below. |
| `src/ui/` | The new UI, being built (open it with `?ui=next`; see "The new UI" below): `ui/deckbuilder/` (the page frame, the deck side, the collection filter and cards, the card information, the hover zoom, the dialogs) and `ui/shared/` (the card and its hover reporting, `fitCards`, the dialog, the notifier). Markup and interaction only; the themes draw it. `ui-boundary.spec.ts` checks it imports only core and ui and has no styles. |
| `src/deckbuilder-app/layout/` | The page frame: `page-layout.ts` reproduces the legacy `jquery.layout` panes (deck, collection, resizer bar), and `deck-layout.ts` is the legacy `layoutUI` positioning of the deck slots. |
| `src/deckbuilder-app/cards/` | Card display: `gempCard` (the legacy card element with its errata, foil, count, border and meta-site layers), `gemp-card-display` (the large card of the info dialog and the hover zoom), `card-group-layout.ts` (the `NormalCardGroup` rows and the `VerticalBarGroup` sites column) and `CardRegistry` (what each card element stands for). |
| `src/deckbuilder-app/collection/` | The collection pane: the filter (`gempCardFilter`) and the cards of the current page (`gempCollectionDisplay`). |
| `src/deckbuilder-app/deck/` | The deck pane (`gempDeckArea`: slots, piles, stats), the top toolbar, and `CardClicksService`, the page-wide card mouse handling (click activates the card; shift-click, right-click or dragging up asks for card details; both go to `DeckbuilderActions`). |
| `src/deckbuilder-app/dialogs/` | The dialogs: deck list and library, import, notes, card information, the hover zoom (`AutoZoomService`), the selection-pack chooser and the "New items" popups. |
| `src/deckbuilder-app/shared/` | jQuery UI look-alikes, so the dark-hive theme applies: `gempUiButton` (button classes and hover/pressed states), `gempUiSlider` (the slider) and `gemp-ui-dialog` (the dialog: centred, draggable, resizable, modal overlay, Escape). |
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

## Themes

The deck builder has two themes, `classic` (the legacy page's look) and `modern`. Each is a stylesheet built from
`src/themes/<name>/theme.css` into `theme-<name>.css` next to the app (`angular.json`: `inject: false`, so it is not
linked by default). `ThemeService` (`src/core/platform/theme.service.ts`) picks one:

1. `?theme=classic` or `?theme=modern` in the address, which is also remembered;
2. otherwise the `deckbuilderTheme` cookie (the player's choice, made in the toolbar's theme picker);
3. otherwise the device: phones and tablets (a coarse pointer and a screen whose short side is under 1100px, the rule
   of the legacy `mobileView.js`) get `modern`, everything else `classic`.

Unknown names are ignored. The app starts only once the theme's stylesheet has loaded (an app initializer), so the page
never shows unstyled. Switching loads the new stylesheet first and removes the old one after; only the CSS changes, so
the deck being edited stays as it is.

Scoping: only one theme's stylesheet is on the page at a time, so theme CSS is written without a theme prefix.
`<html data-theme="...">` is also set, for the rare rule that must know the theme from outside a theme stylesheet.
The theme files are not hashed (their names are fixed so they can be linked at runtime); the server revalidates them
with an ETag, like the legacy stylesheets.

## The new UI and its DOM contract

`src/ui` replaces the current view (`src/deckbuilder-app`) step by step. Until it is complete the current view stays the
default; `?ui=next` (temporary, `src/ui-switch.ts`) starts the new one instead, with the legacy stylesheets removed so
the themes are seen on their own, and with its own providers (`ui/deckbuilder/deckbuilder-page.config.ts`), whose
Notifier is `DialogNotifier`: messages and questions in the page's own dialog instead of the browser's.

The rules:

- **Layers.** `core/` holds state, rules and server calls; `ui/` holds semantic markup and interaction; `themes/<name>/`
  holds every visual rule, layout included. `ui/` components have no styles (only `display: contents` on a few hosts)
  and no pixel sizes: anything a theme sizes comes from CSS, with custom properties where the markup needs a number
  (e.g. `--gd-card-height` from `fitCards`).
- **Markup.** Native elements (`button`, `select`, `input type=range`, `label`, `dialog`); class names
  `gd-<block>__<element>`; state in `data-*` attributes (`data-role`, `data-orientation`, `data-active`, ...) and ARIA,
  never in class names. Templates hold every affordance any theme needs; a theme hides what it doesn't use.
- **Interaction** is theme-neutral and each component handles its own events (no page-wide listeners).

| Element | Contract |
|---|---|
| `.gd-page` (`<deckbuilder-app>`) | `data-collection` = open, closed; `--gd-collection-width` once resized (the theme clamps it). Children: `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`; drag or arrow keys resize, Enter toggles; `data-dragging`) with `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection`. |
| `.gd-filter` | `.gd-pager` (`button.gd-pager__previous`/`__next` with `data-icon`, `input[type=range].gd-pager__size`); `.gd-filter__fields` with `.gd-filter__basic` and `.gd-filter__advanced` (`.gd-filter__advanced-title`); each field `label.gd-field` (`data-field`, `data-kind` = select/text/numeric, `data-wide`) with `.gd-field__label` and `.gd-field__control`; `.gd-filter__resets` (`button.gd-button[data-action]`); `.gd-cultures` > `.gd-cultures__group[data-group]` > `label.gd-culture` (`data-culture`, `data-active`, `data-icon-kind`) with a visually hidden `input.gd-culture__input` and `img.gd-culture__icon`. |
| `.gd-collection` | `.gd-collection__cards` (the `fitCards` box, `data-count`) with a `button.gd-card` per item, packs first. |
| `button.gd-card` | `data-role` = card, pack, selection, placeholder, copy (another copy of a draw deck card, drawn behind it); `data-orientation` = vertical, horizontal; `data-errata`, `data-foil`, `data-borderless`. Inside: `img.gd-card__image`, and when they apply `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay`, `.gd-card__count`. Click/Enter/Space uses the card; shift-click, right-click, a long press (touch) or dragging it up 20px (mouse) shows its details. Mouse hover and presses are reported to `CardHover` (for the hover zoom). |
| `[gdFitCards]` | Sets `--gd-card-height` (px) on its box so the cards fit, from the box's size and the theme's `--gd-card-max-height` (default 200px), `--gd-card-gap` (5px) and `--gd-card-fit`: `contain` (largest that fits), `rows` or `none` (always the maximum; the box scrolls). Each entry is a `FitCard` (landscape or not, and the copies drawn behind it, a fifth of each showing). It also sets `--gd-card-offset` (px): the space above the rows when centred, as the legacy groups placed them; a theme may move the cards by it (moving, not padding, so the box's size stays). `rows` sizes like the legacy NormalCardGroup: it tries 1, 2, 3... rows, each `(box height - gaps) / rows` high (capped at the maximum), and keeps the first that holds every card, so cards can come out a little smaller than with `contain`; one row is used when two rows of it wouldn't fit, and the height can have a fraction as it did there. The classic theme uses it so the collection and the deck match the legacy page. |
| `dialog.gd-card-info` | Modal native dialog; `--gd-card-info-zoom` (0.4 to 1). `.gd-card-info__header` (`.gd-card-info__title`, `button.gd-card-info__close`), `input[type=range].gd-card-info__zoom` (`autofocus`), `.gd-card-info__card` (`data-orientation`, `data-flipped` = back/upside-down, `data-flippable`, `data-foil`) with `img.gd-card-info__image`, `__overlay`, `__foil`; `a.gd-card-info__wiki`. Clicking the card flips it; any other click, the close button or Escape closes it. |
| `.gd-deck` | `gd-deck-toolbar.gd-toolbar`, a `section.gd-slot` per slot, a `section.gd-pile` per draw deck side, `gd-deck-stats.gd-stats`. |
| `.gd-toolbar` | `role=toolbar`. `.gd-toolbar__deck-actions` with `button.gd-button[data-action]` (new, save, rename, copy, import, library, deck-list, convert-errata; `title`/`aria-label`), `select.gd-toolbar__format`, `button.gd-button[data-action=notes]`, `.gd-toolbar__deck-name` (`data-modified`, legacy "*name - modified" text), `button.gd-button[data-action=auto-zoom]` (`aria-pressed`; absent on touch devices), `select.gd-toolbar__collection`, `select.gd-toolbar__theme`. The theme draws an icon per `data-action`. |
| `section.gd-slot` | `data-slot` = ring-bearer, ring, map, race, sites; `hidden` when the format has none; `data-count`. `.gd-slot__label` (a `button` that selects for the slot; a `div` for the display-only race) with `.gd-slot__title` and `.gd-slot__hint`; the Map's `a.gd-slot__link`; `.gd-slot__cards` with a `button.gd-card` per card (a fitCards box, except the sites, which get `--gd-slot-count`). Clicking a card removes it; clicking a site also shows the sites in the collection, as before. |
| `section.gd-pile` | `data-side` = free-peoples, shadow; `data-count` (copies included). `.gd-pile__cards` (fitCards box) with a `.gd-stack` per card: its other copies (`data-role=copy`, oldest first), then the card. |
| `.gd-stats` | `aria-live=polite`; a `.gd-stats__line` per line (empty = blank line) with `strong` labels and `span` parts, `data-tone` = error, valid, warning. Rendered from data (`DeckService.stats`, parsed from the server's fragment by `core/deck/deck-stats.ts`); the server's markup never reaches the page. |
| `dialog.gd-dialog` | Every dialog but the card information (`ui/shared/dialog`, `gd-dialog`): a native `<dialog>`, modal unless said below. `data-dialog` = deck-list, library, import, notes, selection, delivery, alert, confirm, prompt; `data-placed` once moved (dragging the title bar) or pinned (pressing its own edge, so a CSS `resize` grows it right and down), with `--gd-dialog-left`/`--gd-dialog-top` (px); `--gd-dialog-layer` (grows each time a dialog opens or is pressed: the stacking of non-modal ones). `header.gd-dialog__header` (`h2.gd-dialog__title`, `button.gd-dialog__close`), `.gd-dialog__body`, then `.gd-dialog__actions` where there are actions. Closes on the close button or Escape. Focuses an `autofocus` control, else the content's first control, else the actions' first; import, selection and delivery focus the dialog itself, as the legacy ones did. Opened again, it is placed afresh. |
| Deck list, library | `ul.gd-deck-list` > `li.gd-deck-list__item`: a `button.gd-button` per `data-action` (open, rename, share, delete; library: open, deck-list; `title` names it), then `.gd-deck-list__name` with `b.gd-deck-list__format` ("[format]") and the name as text. |
| Import (not modal), notes | `.gd-import` with `textarea.gd-import__text` and `button.gd-button[data-action=import]` ("Import"; empty again each time it is opened from the toolbar). `textarea.gd-notes__text` (kept on change and on close). |
| Selection, delivery (not modal) | `.gd-shown-cards` (fitCards box, `data-count`) with a `button.gd-card` per card: selection choices (`data-role=card`; using one picks it), delivered packs and cards (`pack`, `card`, with counts). A "New items" dialog stays on the page, closed, once shown, keeping its size. |
| Notifier (`gd-notifier`) | `p.gd-notifier__message` (line breaks kept), `input.gd-notifier__input` (prompt; focused, selected; Enter is OK), `.gd-dialog__actions` with `button.gd-button[data-action=ok]` and, for confirm and prompt, `[data-action=cancel]`. Escape and the close button are Cancel. One at a time, in the order asked. |
| `div.gd-zoom` | The hover zoom: `aria-hidden`, `hidden` when nothing is zoomed; `--gd-zoom-left`/`-top`/`-width`/`-height` (px; placed and sized as the legacy AutoZoom did), `data-orientation`, `data-flipped` (back; Shift), `data-foil`; `img.gd-zoom__image`, `__overlay`, `__foil`. Shown after the mouse rests 500ms on a card (at once within 500ms of the last zoom); hidden by a press, when the toolbar's magnifier is off (`AutoZoomSetting`, legacy cookie) or the card information is open. |

`visual-check/stubbed.mjs` screenshots the legacy page and the new UI side by side without a Gemp server (both served
from disk, the server's answers stubbed); see its header.

## Tests

```
npm test               # watch mode
npm test -- --watch=false
```

## Comparing with the legacy deck builder

`visual-check/` holds a Playwright check that loads `deckBuild.html` and this page side by side in a real browser,
goes through the deck builder's features on both, and compares screenshots and server requests. See
`visual-check/README.md`. It is separate from the Maven build and needs a running server.

