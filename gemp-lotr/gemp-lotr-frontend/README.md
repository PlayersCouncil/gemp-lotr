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
seconds (and the legacy card data files the card images are generated from) and, when they change, builds into `web/.ng-build/` while the old build stays live, then adds the
new files next to the old ones and replaces `index.html` last, so the page and what it names are always there (the
previous build's files stay until the next deploy). A build that fails leaves the old one in place (see `docker logs gemp_frontend_1`). So deploying a
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

| Folder                                  | What it holds                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/core/`                             | State, rules and server calls, with no view code: nothing in it imports from the view folders, and it has no templates or styles, and only `core/platform/` touches the browser (`DOCUMENT`, `window`, cookies); `core-boundary.spec.ts` checks all of this.                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `src/core/api/`                         | `DeckbuilderApi`: every server call the deck builder makes, typed. `GempHttp` handles the request formats (strictly encoded forms, text, XML, JSON); the interceptors in `gemp-interceptors.ts` add what every call needs (participant, no caching, `ApiError`, the opened-pack header).                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `src/core/cards/`                       | Card images. `card-blueprint.ts` is a port of the legacy `Card.js` rules; the image tables come from `src/generated/card-data.json` via `card-data.ts` (see Card data below). `CardView` is what a card on a page shows, and `CardViews` builds it from a blueprint. `CardActions` is what using a card means (each page provides its own), `CardInfoService` holds the card shown in the card information and `CardHover` the card under the mouse. A page gets all of these with `provideCardInteractions`; nothing here knows about any one page, which `core-boundary.spec.ts` checks.                                                                                                                       |
| `src/core/collection/`                  | The collection pane: `CollectionQuery` (the filter fields and their rules), `CollectionBrowser` (which collection and page, loading from `/collection/{type}`), `CollectionCounts` (copies left to add), and the `filter-*.ts` files (the lists, cultures, Block/Set lists and filter string).                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `src/core/formats/`                     | `FormatsService`: the formats from `/deck/formats`, fetched once.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `src/core/deck/`                        | The deck being edited: `DeckDraft` (its state), `DeckFormatSelection` (the format list), `DeckChanges` (what follows a change), `DeckStatsRefresher`, `DeckEditing` (cards, slots, notes), `DeckOpener` (open, import, convert), `DeckSaver` (New, Save, Rename, Copy, the save prompts), `DeckStartup` and `DeckLibraryService` (the deck list and library). `deck-model.ts` holds the pure deck rules.                                                                                                                                                                                                                                                                                                         |
| `src/core/delivery/`, `src/core/packs/` | `PackDeliveryService`: the opened-pack "New items" contents. `PacksService`: opening packs, the selection-pack choice and the open "New items" dialogs; `shown-cards.ts` gives their cards.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `src/core/actions/`                     | `DeckbuilderActions`: the deck builder's `CardActions` (add a card, remove it, open a pack), and `deckbuilder-card-ref.ts`, what a card on that page stands for.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `src/core/notifier/`                    | `Notifier`: messages and questions to the player (the legacy alert/confirm/prompt), all asynchronous. The view provides it: `ui/shared/notifier/WindowNotifier`, the browser's own dialogs, as on the legacy page. `DialogNotifier` (shown by `gd-notifier`, in the page's own dialogs) is kept for the redesign. Tests use `src/testing/fake-notifier.ts`.                                                                                                                                                                                                                                                                                                                                                      |
| `src/core/session/`                     | `SessionService`: the optional `?participantId=` (admins acting as a player).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `src/core/platform/`                    | The browser, behind services: cookies, the page's query string, image preloading, opening links, and `ThemeService` (see Themes below).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `src/styles/`                           | The global stylesheets: `themes.css` (each theme's values), `base.css` (the document, links, scrollbars) and `controls.css` (the button and slider several components use). See Themes below.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `src/ui/`                               | The view (see "The UI and its DOM contract" below): `ui/app/` (the app shell, see One app below), `ui/deckbuilder/` (the page component `DeckbuilderPageComponent`, element `<deckbuilder-app>`, the route `deckbuilder`; the page frame, the deck side, the collection filter and cards, the dialogs) and `ui/shared/` (what any page can use: the card and its hover reporting, the card information, the hover zoom, `fitCards`, the dialog, the notifier; `ui-boundary.spec.ts` keeps it free of any one page). Each component has its own stylesheet (`*.component.css`), drawn with the theme values. `ui-boundary.spec.ts` checks it imports only core and ui, has no inline styles and names no colours. |
| `src/testing/`                          | Test helpers and `server-responses/`, real server responses the parser tests run against. `parity/` runs the legacy page and the Angular page side by side (see below).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `src/parity/`                           | Specs that run the legacy `deckBuild.html` (unchanged, in jsdom) and the Angular page against the same fake server, and compare them after every gesture.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

The deck builder's state is provided by its page (`provideDeckbuilderState`), so it is created when the page opens and destroyed when it closes. `PageLifetime` decides what happens to work still running then: reads are cancelled, writes finish but their answers are ignored.

`index.html` is a plain HTML5 page (`<!doctype html>`, standards mode). The legacy page's XHTML Transitional doctype
put the browser in "limited quirks" mode, where an image or a textarea alone on a line adds no space below it for the
text's descenders. The page matches it without the quirk: its images and buttons sit in flex or block boxes,
and the import dialog's textarea has `vertical-align: top`.

Settings kept in cookies (auto-zoom on/off, the card info zoom) are written with the legacy pages' path (`/gemp-lotr`),
so both deck builders share them.

No legacy stylesheet is linked: the components' stylesheets draw the whole page.

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

The look is split in two:

- **The components' styles** (`src/ui/**/*.component.css`, scoped to their component, and the global `src/styles/base.css`
  and `controls.css`): layout, sizes and states, the same in every theme. They name no colours, fonts or images; they
  use the theme values (`var(--theme-...)`). The dialog (`ui/shared/dialog`) is the one unscoped component: it also
  draws the controls the page puts in it, so its rules all start at `.gd-dialog`.
- **The themes** (`src/styles/themes.css`): values only (colours, fonts, backgrounds, borders, radii, the icon sprite,
  scrollbar images). `classic` (the legacy page's look) is on `:root`; `modern` (flat colours and the system font, dark
  gray on every device) overrides it on `:root[data-theme='modern']`. Images go in `public/themes/<name>/`,
  served as `/gemp-lotr/ng/themes/<name>/...` and referred to by that absolute path.

`ThemeService` (`src/core/platform/theme.service.ts`) picks the theme and sets `<html data-theme="...">`:

1. `?theme=classic` or `?theme=modern` in the address, which is also remembered;
2. otherwise the `gempDeckbuilderTheme` cookie (the player's choice, made in the toolbar's theme picker): the deck
   builder's own, apart from the rest of the site. The older `gempTheme` cookie is no longer read;
3. otherwise `modern`, on every device.

Players switch between the themes at any time with the toolbar's theme picker (`button.gd-toolbar__theme`, the
lightbulb on bright red in every theme, `--theme-attention-*`, so players notice it; drawn after the deck's name so nothing the legacy page had moves): it switches to the next theme, which its
title names. The choice is remembered on the device (the cookie). It is shown while `THEME_PICKER` (`theme.service.ts`)
is true, as the owner decided; the legacy page had no picker, so the visual check leaves its area out (`EXCLUDED_AREAS` in `accepted.mjs`). With the
token's factory set to `() => false` it is gone, and a theme is chosen only with `?theme=`. With more than two themes
the button would go through them in turn; a list would then serve players better.

Switching only sets `data-theme`, so it takes effect at once and the deck being edited stays as it is. Unknown names
are ignored. The theme is set by an app initializer, before the page is drawn. A theme can also change behaviour
(`THEME_BEHAVIOUR`): `modern`'s card information is modal and its dialogs give the focus back when they close.

`src/styles/themes.spec.ts` keeps the themes switchable: every value a stylesheet uses is given by classic (apart from
the few it leaves unset on purpose, listed there), every theme sets only values that are used, and a theme's values
are on `:root` alone.

### Adding a theme

1. Add its values to `src/styles/themes.css`, under `:root[data-theme='<name>']`: every `--theme-*` value it changes
   from `classic` (a new value used by a stylesheet needs a classic value too).
2. Register it in `src/core/platform/theme.service.ts`: add the name to `THEMES` and a label to `THEME_LABELS` (shown in
   the toolbar's picker), and its behaviour to `THEME_BEHAVIOUR`. Change `DEFAULT_THEME` or `MOBILE_THEME` only if it
   should be picked by default.
3. Check it with `npm run build` and, in `visual-check`, `npm run check:stubbed -- --theme=<name>`.

A change of layout is a change to a component's stylesheet, for every theme.

## The UI and its DOM contract

`src/ui` is the deck builder's view; it replaced the first Angular view (`src/deckbuilder-app`, which reused the legacy
stylesheets and element IDs). Its providers are in `ui/app/app.config.ts`, whose Notifier is
`WindowNotifier`: the browser's own dialogs.

The rules:

- **Layers.** `core/` holds state, rules and server calls; `ui/` holds the components: markup, interaction and each
  component's own stylesheet, layout included. Templates have no inline styles (only `display: contents` on a few
  hosts) and no pixel sizes: sizes come from CSS, with custom properties where the markup needs a number (e.g.
  `--gd-card-height` from `fitCards`). Colours, fonts and images come from the theme values. `core/core-boundary.spec.ts`
  and `ui/ui-boundary.spec.ts` enforce this.
- **Markup.** Native elements (`button`, `select`, `input type=range`, `label`, `dialog`); class names
  `gd-<block>__<element>`; state in `data-*` attributes (`data-role`, `data-orientation`, `data-active`, ...) and ARIA,
  never in class names.
- **Interaction** is theme-neutral and each component handles its own events (no page-wide listeners).

| Element                          | Contract                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `.gd-page` (`<deckbuilder-app>`) | `data-collection` = open, closed; `--gd-collection-width` once resized (its stylesheet clamps it); `--gd-drag-width` and `.gd-page__drag-preview` during a drag, a preview its stylesheet places and clamps (the panes resize on release, or not at all if the drag is cancelled). Children: `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`; drag or arrow keys resize, Enter toggles; `data-dragging`) with `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `.gd-filter`                     | `.gd-pager` (`button.gd-pager__previous`/`__next` with `data-icon`, `.gd-pager__track` holding `input[type=range].gd-pager__size`); `.gd-filter__fields` with `.gd-filter__basic` and `.gd-filter__advanced` (`.gd-filter__advanced-title`); each field `label.gd-field` (`data-field`, `data-kind` = select/text/numeric, `data-wide`) with `.gd-field__label` and `.gd-field__control`; `.gd-filter__resets` (`button.gd-button[data-action]` > `.gd-button__text`); `.gd-cultures` > `.gd-cultures__group[data-group]` > `label.gd-culture` (`data-culture`, `data-active`, `data-icon-kind`) with a visually hidden `input.gd-culture__input` and `.gd-culture__face` > `img.gd-culture__icon`.                                                                                                                                                                                                                                                                                                                                |
| `.gd-collection`                 | `.gd-collection__cards` (the `fitCards` box, `data-count`) with a `button.gd-card` per item, packs first.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `button.gd-card`                 | `data-role` = card, pack, selection, placeholder, copy (another copy of a draw deck card, drawn behind it); `data-orientation` = vertical, horizontal; `data-errata`, `data-foil`, `data-borderless`. Inside: `img.gd-card__image`, and when they apply `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay`, `.gd-card__count`. Click/Enter/Space uses the card; shift-click, right-click, a long press (touch) or dragging it up 20px (mouse) shows its details. Mouse hover and presses are reported to `CardHover` (for the hover zoom).                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `[gdFitCards]`                   | Sets `--gd-card-height` (px) on its box so the cards fit, from the box's size and the box's `--gd-card-max-height` (default 200px), `--gd-card-gap` (5px) and `--gd-card-fit`: `contain` (largest that fits), `rows` or `none` (always the maximum; the box scrolls). Each entry is a `FitCard` (landscape or not, and the copies drawn behind it, a fifth of each showing). It also sets `--gd-card-offset` (px): the space above the rows when centred, as the legacy groups placed them; the box's stylesheet may move the cards by it (moving, not padding, so the box's size stays). `rows` sizes like the legacy NormalCardGroup: it tries 1, 2, 3... rows, each `(box height - gaps) / rows` high (capped at the maximum), and keeps the first that holds every card, so cards can come out a little smaller than with `contain`; one row is used when two rows of it wouldn't fit, and the height can have a fraction as it did there. The collection, the deck and the pack dialogs use it so they match the legacy page. |
| `dialog.gd-card-info`            | Modal native dialog; `--gd-card-info-zoom` (0.4 to 1). `.gd-card-info__header` (`.gd-card-info__title`, `button.gd-card-info__close`), `input[type=range].gd-card-info__zoom` (`autofocus`), `.gd-card-info__card` (`data-orientation`, `data-flipped` = back/upside-down, `data-flippable`, `data-foil`) with `img.gd-card-info__image`, `__overlay`, `__foil` and `.gd-card-info__flip`, the clickable area; `a.gd-card-info__wiki`. Clicking that area flips the card; any other click, the close button or Escape closes it.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `.gd-deck`                       | `gd-deck-toolbar.gd-toolbar`, a `section.gd-slot` per slot, a `section.gd-pile` per draw deck side, `gd-deck-stats.gd-stats`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `.gd-toolbar`                    | `role=toolbar`. `.gd-toolbar__deck-actions` with `button.gd-button[data-action]` (new, save, rename, copy, import, library, deck-list, convert-errata; `title`/`aria-label`), `select.gd-toolbar__format`, `button.gd-button[data-action=notes]`, `.gd-toolbar__deck-name` (`data-modified`, legacy "*name - modified" text), `button.gd-button[data-action=auto-zoom]` (`aria-pressed`; absent on touch devices), `select.gd-toolbar__collection`, `button.gd-button.gd-toolbar__theme[data-action=theme]` while `THEME_PICKER` is on (drawn after the deck's name; it switches to the next theme, which its `title` names). The toolbar draws an icon per `data-action`.                                                                                                                                                                                                                                                                                                                                                         |
| `section.gd-slot`                | `data-slot` = ring-bearer, ring, map, race, sites; `hidden` when the format has none; `data-count`. `.gd-slot__label` (a `button` that selects for the slot; a `div` for the display-only race) with `.gd-slot__title` and `.gd-slot__hint`; the Map's `.gd-slot__more` (the label again, unseen) holding `a.gd-slot__link`; `.gd-slot__cards` with a `button.gd-card` per card (a fitCards box, except the sites, which get `--gd-slot-count`). Clicking a card removes it; clicking a site also shows the sites in the collection, as before.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `section.gd-pile`                | `data-side` = free-peoples, shadow; `data-count` (copies included). `.gd-pile__cards` (fitCards box) with a `.gd-stack` per card: its other copies (`data-role=copy`, oldest first), then the card.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `.gd-stats`                      | `aria-live=polite`; a `.gd-stats__line` per line (empty = blank line) with `strong` labels and `span` parts, `data-tone` = error, valid, warning. Rendered from data (`DeckService.stats`, parsed from the server's fragment by `core/deck/deck-stats.ts`); the server's markup never reaches the page.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `dialog.gd-dialog`               | Every dialog but the card information (`ui/shared/dialog`, `gd-dialog`): a native `<dialog>`, modal unless said below. `data-dialog` = deck-list, library, import, notes, selection, delivery, alert, confirm, prompt; `data-placed` once moved (dragging the title bar) or pinned (pressing its own edge, so a CSS `resize` grows it right and down), with `--gd-dialog-left`/`--gd-dialog-top` (px); `--gd-dialog-layer` (grows each time a dialog opens or is pressed: the stacking of non-modal ones). `header.gd-dialog__header` (`h2.gd-dialog__title`, `button.gd-dialog__close`), `.gd-dialog__body`, then `.gd-dialog__actions` where there are actions. Closes on the close button or Escape. Focuses an `autofocus` control, else the content's first control, else the actions' first; import, selection and delivery focus the dialog itself, as the legacy ones did. Opened again, it is placed afresh.                                                                                                              |
| Deck list, library               | `ul.gd-deck-list` > `li.gd-deck-list__item`: a `button.gd-button` per `data-action` (open, rename, share, delete; library: open, deck-list; `title` names it) holding `.gd-deck-list__icon`, then `.gd-deck-list__name` with `b.gd-deck-list__format` ("[format]") and the name as text.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Import (not modal), notes        | `.gd-import` with `textarea.gd-import__text` and `button.gd-button[data-action=import]` ("Import"; empty again each time it is opened from the toolbar). `textarea.gd-notes__text` (kept on change and on close).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Selection, delivery (not modal)  | `.gd-shown-cards` (fitCards box, `data-count`) with a `button.gd-card` per card: selection choices (`data-role=card`; using one picks it), delivered packs and cards (`pack`, `card`, with counts). A "New items" dialog stays on the page, closed, once shown, keeping its size.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Notifier (`gd-notifier`)         | `p.gd-notifier__message` (line breaks kept), `input.gd-notifier__input` (prompt; focused, selected; Enter is OK), `.gd-dialog__actions` with `button.gd-button[data-action=ok]` and, for confirm and prompt, `[data-action=cancel]`. Escape and the close button are Cancel. One at a time, in the order asked.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `div.gd-zoom`                    | The hover zoom: `aria-hidden`, `hidden` when nothing is zoomed; `--gd-zoom-left`/`-top`/`-width`/`-height` (px; placed and sized as the legacy AutoZoom did), `data-orientation`, `data-flipped` (back; Shift), `data-foil`; `img.gd-zoom__image`, `__overlay`, `__foil`. Shown after the mouse rests 500ms on a card (at once within 500ms of the last zoom); hidden by a press, when the toolbar's magnifier is off (`AutoZoomSetting`, legacy cookie) or the card information is open.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

## Tests and the visual check

```
npm test                    # unit tests (Vitest), watch mode
npm test -- --watch=false --coverage   # once, as Maven runs them
npm run lint                # ESLint, also part of the Maven build
npm run build               # the app, with styles.css (src/styles) and the components' styles
```

### The legacy page side by side, in the unit tests

`src/parity/` runs the legacy deck builder itself (`deckBuild.html` and its scripts, unchanged) in jsdom next to the
Angular page, each against its own `FakeGempServer` (`src/testing/parity/`), with clocks the test moves. The same
gestures go to both through the selectors of `visual-check/controls.mjs`, and after each one the test compares what
each page asked its server, shows (deck name, filter fields, cards in each place, dialogs and messages, disabled
buttons, hidden cultures) and left saved.

- `generated-sessions.parity.spec.ts`: sessions of 25 gestures chosen at random from what the legacy page shows (filters,
  cultures, paging, formats, collections, cards, slots, toolbar, every dialog). 10 run with the unit tests; `npm run
test:parity` runs 200 (part of `check:all`). A failure names its seed: `PARITY_SEED=<seed> PARITY_SESSIONS=1 npm run
test:parity` repeats it.
- `legacy-functions.parity.spec.ts`: the legacy page's own functions called on generated inputs next to their ports:
  the filter string, the decklist clean-up, the deck stats HTML as the browser shows it, and where a card group puts its
  cards (`NormalCardGroup` against `fitCards` and `rowLayout`), and the Block and Set lists. 500 of each with the unit tests (fewer groups and lists), 10,000 in
  `test:parity` (`PARITY_CASES`).
- `server-answers.parity.spec.ts`: every error message the legacy page had, the generic one for the rest, and opening
  boosters and selection packs.
- `differences.parity.spec.ts`: where the Angular page differs on purpose (fixed legacy bugs), or the difference waits for
  a decision. Each shows what both pages do, and the generated sessions stay away from exactly these.
- `startup.parity.spec.ts`: every list's options as the page opens.

### Mutation testing

`npm run check:mutation` in `visual-check` changes one thing at a time in the deck builder's logic (a comparison, a
condition, a constant, a message) and runs the unit tests of that file; a change no test notices is listed in
`visual-check/report-mutation/survivors.md`. It works on copies in `visual-check/.local-server/mutation/`, so the
`frontend` container never rebuilds from a mutated file. `--files <part of a path>` limits it, `--with-parity` adds
`src/parity`.

### The visual check

`visual-check/` is a Playwright check, separate from the Maven build (see `visual-check/README.md`):

- `npm run check:stubbed` compares both pages with no server or login, using placeholder card art;
- `run.mjs` (`npm run check`) compares both pages against a running server; it still uses the legacy element IDs.
