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
`/gemp-lotr/ng/<page>/`: `deckbuilder` (`/gemp-lotr/ng/` and unknown paths go to it) and `game` (the game table: see
"The game page" below; the hall opens games there, the game history replays).

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
| `src/core/collection/`                  | The collection pane: `CollectionQuery` (the filter fields and their rules), `CollectionBrowser` (which collection and page, loading from `/collection/{type}`), `CollectionCounts` (copies left to add), `FilterPins` and `CardSize` (the compact layout's); the filter itself is `core/card-filter/`.                                                                                                                                                                                                                                                                                                                                                                                                           |
| `src/core/card-filter/`                 | The card filter shared by the deck builder and the game page's card search: the `filter-*.ts` files (the fields, the lists, cultures, Block/Set lists and the filter string) and `CardFilterModel` / `CardFilterPager` / `CardFilterPins` / `CardFilterSize`, what the shared panel (`ui/shared/card-filter`) works with (each page provides its own).                                                                                                                                                                                                                                                                                                                                                           |
| `src/core/formats/`                     | `FormatsService`: the formats from `/deck/formats`, fetched once.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `src/core/deck/`                        | The deck being edited: `DeckDraft` (its state), `DeckFormatSelection` (the format list), `DeckChanges` (what follows a change), `DeckStatsRefresher`, `DeckEditing` (cards, slots, notes), `DeckOpener` (open, import, convert), `DeckSaver` (New, Save, Rename, Copy, the save prompts), `DeckStartup` and `DeckLibraryService` (the deck list and library). `deck-model.ts` holds the pure deck rules.                                                                                                                                                                                                                                                                                                         |
| `src/core/delivery/`, `src/core/packs/` | `PackDeliveryService`: the opened-pack "New items" contents. `PacksService`: opening packs, the selection-pack choice and the open "New items" dialogs; `shown-cards.ts` gives their cards.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `src/core/actions/`                     | `DeckbuilderActions`: the deck builder's `CardActions` (add a card, remove it, open a pack), and `deckbuilder-card-ref.ts`, what a card on that page stands for.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `src/core/notifier/`                    | `Notifier`: messages and questions to the player (the legacy alert/confirm/prompt), all asynchronous. The view provides it: `ui/shared/notifier/WindowNotifier`, the browser's own dialogs, as on the legacy page. `DialogNotifier` (shown by `gd-notifier`, in the page's own dialogs) is the game page's (its route's providers: e.g. the question before an assignment with a minion left unassigned). Tests use `src/testing/fake-notifier.ts`.                                                                                                                                                                                                                                                              |
| `src/core/session/`                     | `SessionService`: the optional `?participantId=` (admins acting as a player).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `src/core/game/`                        | The game page's state, without view code. `protocol/` parses the server's XML into typed `GameEvent`s and decisions; `state/` holds `GameModel` and the pure `applyEvent` and `local-choices.ts` (assignments and selections not yet sent); `api/GameApi` makes every game call; `session/GameSession` opens the game, long-polls and answers as the legacy page does, and `GameRestore` keeps the page-only state per game in the tab's session storage so a reload shows the game as it was left; `settings/GameSettings` keeps the legacy game cookies; `undo/GameUndo` takes the game back (see "The game page"). A page gets all of it with `provideGameState`.                                             |
| `src/core/chat/`                        | `ChatRoom`: one chat room (join, news every few seconds, messages sent in order) and `parseChat`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `src/core/platform/`                    | The browser, behind services: cookies, the page's query string, the tab's session storage (`SessionStore`), image preloading, opening links, and `ThemeService` (see Themes below).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
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
   the toolbar's picker), and its behaviour to `THEME_BEHAVIOUR`. Change `DEFAULT_THEME` only if it should be picked by
   default.
3. Check it with `npm run build` and, in `visual-check`, `npm run check:stubbed -- --theme=<name>`.

A change of layout is a change to a component's stylesheet, for every theme.

### Font metrics

A theme with other fonts than the legacy page's would move things on a computer (wider filter labels, taller lines).
So the few sizes that came from the legacy fonts are theme values, `--theme-metrics-*` in `themes.css`: classic leaves
them to its fonts (`auto`, `normal`), as the legacy page did, and modern pins them to the legacy page's sizes in
Chromium (the filter's label columns, the label, title, stats and dialog line heights). `npm run check:stubbed --
--theme=modern --geometry` (visual-check) checks that modern is laid out as the legacy page on a computer.

### The compact layout (phones and tablets)

The modern theme stacks the page on a portrait screen up to a tablet's width, on any screen narrower than 826px, or on
one lower than 501px (a phone held sideways, where the halves are side by side: `data-split=columns`)
(`COMPACT_QUERY` in `src/core/platform/compact-layout.service.ts`; `CompactLayout.active`). The classic theme never
does: it stays the legacy page. The theme also sets the page's viewport (`THEME_VIEWPORT`): modern lays out at the
device's width, classic has no viewport tag, as the legacy page (phones show it zoomed out). Switched to classic from
modern, the tag can't simply go: a phone would keep the device's width and the classic page would run off the screen, so
it gets what having none gives (`defaultViewport`: 980px wide, or the screen's width if wider, zoomed out to fit,
following the device as it turns). On a touch screen, modern drops the second tap of a double tap when it lands on
another control than the first (`SecondTapGuard`, `THEME_BEHAVIOUR.guardSecondTap`): the first tap closed a menu or a
dialog, or opened one, and what is under the finger isn't what the player tapped. A pack (`data-tap-once`) opens once
for a double tap. The page then has
`data-layout=compact`, and the components' stylesheets lay out for it
under `:host-context([data-layout='compact'])` (the dialogs under `[data-layout='compact'] .gd-dialog`):

- the deck above, the collection below, each scrolling on its own. The separator between them moves the split by
  dragging (a finger too) or with Up and Down; it starts at half and half and is remembered on the device
  (`gempDeckbuilderSplit` cookie, the deck's share in %, 15 to 85);
- the deck: a bar with the deck menu (theme first, in red, then the deck buttons, Notes, the hover zoom and the
  format) and the deck's name, which stays at the top; the Ring-bearer, Ring, and Map or Race side by side; the sites
  in a strip that scrolls sideways; the Free Peoples and Shadow piles, named and counted, their cards at one size;
  the stats;
- the collection: a bar with the collection menu's button and the card name, the filters kept shown and the pager
  under them, then the cards at one size. The pager's slider sets that size there (`CardSize`, 50 to 200% of the
  layout's own, remembered in the `gempDeckbuilderCardSize` cookie), not the number of cards on a page: a phone's cards
  keep their size and the pane scrolls. The collection menu holds the collection list, the other filters, the reset
  buttons and "Always show": a box per filter that keeps it shown in the bar (`FilterPins`, remembered in the
  `gempDeckbuilderFilters` cookie; none at first). Its button shows how many filters in it narrow the cards. The bar
  and the menu each have a part of the filter (`gd-collection-filter` with `part` = pane or menu), so a filter moves
  between them when it is kept shown or not;
- the menus are `gd-drawer` (`ui/shared/drawer`): a burger button and a panel from the left edge of the window;
- each dialog fits the window; the card information shows the card as large as the window allows (a long press on a
  card opens it; the release that ends the press, wherever it lands, leaves it open).

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

| Element                          | Contract                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| -------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `.gd-page` (`<deckbuilder-app>`) | `data-collection` = open, closed; `--gd-collection-width` once resized (its stylesheet clamps it); `--gd-drag-width` and `.gd-page__drag-preview` during a drag, a preview its stylesheet places and clamps (the panes resize on release, or not at all if the drag is cancelled). Children: `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`; drag or arrow keys resize, Enter toggles; `data-dragging`) with `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection`. In the compact layout: `data-layout=compact`, `--gd-split` (the deck's share, 0 to 1), the separator `aria-orientation=horizontal` with `aria-valuenow` (Up and Down move it) and no toggle, and `gd-collection-menu.gd-collection-bar` first in the collection.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `.gd-filter`                     | `.gd-pager` (`button.gd-pager__previous`/`__next` with `data-icon`, `.gd-pager__track` holding `input[type=range].gd-pager__size`); `.gd-filter__fields` with `.gd-filter__basic` and `.gd-filter__advanced` (`.gd-filter__advanced-title`); each field `label.gd-field` (`data-field`, `data-kind` = select/text/numeric, `data-wide`) with `.gd-field__label` and `.gd-field__control`; `.gd-filter__resets` (`button.gd-button[data-action]` > `.gd-button__text`); `.gd-cultures` > `.gd-cultures__group[data-group]` > `label.gd-culture` (`data-culture`, `data-active`, `data-icon-kind`) with a visually hidden `input.gd-culture__input` and `.gd-culture__face` > `img.gd-culture__icon`. A part of it in the compact layout has `data-part` = pane (the card name with its `placeholder`, the filters kept shown, the pager) or menu (the other filters, the advanced title, the resets and `fieldset.gd-filter__pins` with a `label.gd-pin[data-pin]` and checkbox per filter that can be kept shown).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `.gd-collection`                 | `.gd-collection__cards` (the `fitCards` box, `data-count`) with a `button.gd-card` per item, packs first.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `button.gd-card`                 | `data-role` = card, pack, selection, placeholder, copy (another copy of a draw deck card, drawn behind it); `data-orientation` = vertical, horizontal; `data-errata`, `data-foil`, `data-borderless`. Inside: `img.gd-card__image`, and when they apply `.gd-card__errata`, `img.gd-card__foil`, `img.gd-card__overlay`, `.gd-card__count`. Click/Enter/Space uses the card; shift-click, right-click, a long press (touch) or dragging it up 20px (mouse) shows its details. Mouse hover and presses are reported to `CardHover` (for the hover zoom).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `[gdFitCards]`                   | Sets `--gd-card-height` (px) on its box so the cards fit, from the box's size and the box's `--gd-card-max-height` (default 200px), `--gd-card-gap` (5px) and `--gd-card-fit`: `contain` (largest that fits), `rows` or `none` (always the maximum; the box scrolls). Each entry is a `FitCard` (landscape or not, and the copies drawn behind it, a fifth of each showing). It also sets `--gd-card-offset` (px): the space above the rows when centred, as the legacy groups placed them; the box's stylesheet may move the cards by it (moving, not padding, so the box's size stays). `rows` sizes like the legacy NormalCardGroup: it tries 1, 2, 3... rows, each `(box height - gaps) / rows` high (capped at the maximum), and keeps the first that holds every card, so cards can come out a little smaller than with `contain`; one row is used when two rows of it wouldn't fit, and the height can have a fraction as it did there. The collection, the deck and the pack dialogs use it so they match the legacy page. With `contain`, the box's `--gd-card-label` (px) is room for a line of text under each row.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `dialog.gd-card-info`            | Modal native dialog; `--gd-card-info-zoom` (0.4 to 1). `.gd-card-info__header` (`.gd-card-info__title`, `button.gd-card-info__close`), `input[type=range].gd-card-info__zoom` (`autofocus`), `.gd-card-info__card` (`data-orientation`, `data-flipped` = back/upside-down, `data-flippable`, `data-foil`) with `img.gd-card-info__image`, `__overlay`, `__foil` and `.gd-card-info__flip`, the clickable area; `a.gd-card-info__wiki`. Clicking that area flips the card; any other click, the close button or Escape closes it.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `.gd-deck`                       | `gd-deck-toolbar.gd-toolbar`, a `section.gd-slot` per slot, a `section.gd-pile` per draw deck side, `gd-deck-stats.gd-stats`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `.gd-toolbar`                    | `role=toolbar`. `.gd-toolbar__deck-actions` with `button.gd-button[data-action]` (new, save, rename, copy, import, library, deck-list, convert-errata; `title`/`aria-label`), `select.gd-toolbar__format`, `button.gd-button[data-action=notes]`, `.gd-toolbar__deck-name` (`data-modified`, legacy "*name - modified" text), `button.gd-button[data-action=auto-zoom]` (`aria-pressed`; absent on touch devices), `select.gd-toolbar__collection`, `button.gd-button.gd-toolbar__theme[data-action=theme]` while `THEME_PICKER` is on (drawn after the deck's name; it switches to the next theme, which its `title` names). The toolbar draws an icon per `data-action`. In the compact layout only the deck menu (`gd-drawer`, `data-drawer=deck`: `button.gd-burger`, `dialog.gd-drawer` with a `button.gd-button.gd-menu__item[data-action]` per control, named, the theme's first, and the format in `label.gd-menu__field`) and the deck's name.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `section.gd-slot`                | `data-slot` = ring-bearer, ring, map, race, sites; `hidden` when the format has none; `data-count`. `.gd-slot__label` (a `button` that selects for the slot; a `div` for the display-only race) with `.gd-slot__title` and `.gd-slot__hint`; the Map's `.gd-slot__more` (the label again, unseen) holding `a.gd-slot__link`; `.gd-slot__cards` with a `button.gd-card` per card (a fitCards box, except the sites, which get `--gd-slot-count`). Clicking a card removes it; clicking a site also shows the sites in the collection, as before.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `section.gd-pile`                | `data-side` = free-peoples, shadow; `data-count` (copies included). `.gd-pile__cards` (fitCards box) with a `.gd-stack` per card: its other copies (`data-role=copy`, oldest first), then the card.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `.gd-stats`                      | `aria-live=polite`; a `.gd-stats__line` per line (empty = blank line) with `strong` labels and `span` parts, `data-tone` = error, valid, warning. Rendered from data (`DeckService.stats`, parsed from the server's fragment by `core/deck/deck-stats.ts`); the server's markup never reaches the page.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `dialog.gd-dialog`               | Every dialog but the card information (`ui/shared/dialog`, `gd-dialog`): a native `<dialog>`, modal unless said below. `data-dialog` = deck-list, library, import, notes, selection, delivery, alert, confirm, prompt; `data-placed` once moved (dragging the title bar) or pinned (pressing its own edge, so a CSS `resize` grows it right and down), with `--gd-dialog-left`/`--gd-dialog-top` (px); `--gd-dialog-layer` (grows each time a dialog opens or is pressed: the stacking of non-modal ones); `inert` (dimmed) while its owner says nothing in it can be used (a game's decision while a revert request waits). `header.gd-dialog__header` (`h2.gd-dialog__title`, `button.gd-dialog__close`), `.gd-dialog__body`, then `.gd-dialog__actions` where there are actions. Closes on the close button or Escape. Focuses an `autofocus` control, else the content's first control, else the actions' first; import, selection and delivery focus the dialog itself, as the legacy ones did. Opened again, it is placed afresh. `data-card-window` (with `--dialog-width`/`--dialog-height`): sized to its cards by `[gdCardWindow]` on its fitCards box (`cardWindowSize` in `ui/shared/fit-cards/card-window.ts`: the largest card height up to 500px, down in steps of 20 to 80, at which every card fits in as few rows as possible within 90% of the window less `--gd-page-right`, at least 340px wide), centred in that room, the box told to lay its cards out at that height (`data-card-window`, `--gd-card-fit: contain`); sized again when its cards change, unless the player moved or resized it. |
| Deck list, library               | `ul.gd-deck-list` > `li.gd-deck-list__item`: a `button.gd-button` per `data-action` (open, rename, share, delete; library: open, deck-list; `title` names it) holding `.gd-deck-list__icon`, then `.gd-deck-list__name` with `b.gd-deck-list__format` ("[format]") and the name as text.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| Import (not modal), notes        | `.gd-import` with `textarea.gd-import__text` and `button.gd-button[data-action=import]` ("Import"; empty again each time it is opened from the toolbar). `textarea.gd-notes__text` (kept on change and on close).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Selection, delivery (not modal)  | `.gd-shown-cards` (fitCards box, `data-count`) with a `button.gd-card` per card: selection choices (`data-role=card`; using one picks it), delivered packs and cards (`pack`, `card`, with counts). A "New items" dialog stays on the page, closed, once shown, keeping its size.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Notifier (`gd-notifier`)         | `p.gd-notifier__message` (line breaks kept), `input.gd-notifier__input` (prompt; focused, selected; Enter is OK), `.gd-dialog__actions` with `button.gd-button[data-action=ok]` and, for confirm and prompt, `[data-action=cancel]`. Escape and the close button are Cancel. One at a time, in the order asked.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `div.gd-zoom`                    | The hover zoom: `aria-hidden`, `hidden` when nothing is zoomed; `--gd-zoom-left`/`-top`/`-width`/`-height` (px; placed and sized as the legacy AutoZoom did), `data-orientation`, `data-flipped` (back; Shift), `data-foil`; `img.gd-zoom__image`, `__overlay`, `__foil`. Shown after the mouse rests 500ms on a card (at once within 500ms of the last zoom); hidden by a press, when the toolbar's magnifier is off (`AutoZoomSetting`, legacy cookie) or the card information is open.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |

## The game page

`/gemp-lotr/ng/game?gameId=<id>` is the Angular port of `game.html`, built phase by phase
(trymarz/gemp-lotr-private#27). A game can be **played** on it: the table, every decision, the clocks and the tab box
behave as on the legacy page, with card information, the pile dialogs, the hover zoom, dragging cards, the animations
and replays (G4). The hall opens games on it (`hallUi.js`), and the game history replays (`gameHistoryUi.js`);
`game.html` is still there, unchanged, by its address.

- **Core** (`src/core/game/`, see Code layout): the game's state from the server's events. `table/game-table.ts` sorts
  the cards into the table's boxes (the legacy card groups' rules: rows by zone and side, what is attached to and
  stacked on each card, the assignments, the skirmish), `table/pre-game.ts` reads the pre-game information,
  `session/GameLog` keeps the chat box's lines (the room's chat and the game's messages, in the order they came) and
  `session/GameViewState` what only the view holds (the open tab, the opened piles, a card's open menu, how far the
  panels are scrolled). `decision/decision-view.ts` is what a waiting decision shows (pure: the alert box's text,
  frame and buttons, how each card is offered, the dialogs) and `decision/GameDecisions` what each gesture does
  (choosing cards, a card's action or its menu, assigning minions, the buttons, Escape, the 200 ms bounce guard,
  auto-accept, the deck reveal skipped). `clock/GameClocks` counts the player's clock and the decision's while a
  decision waits, with the legacy colour bands, pulses, expiry and alert sounds (`clock/timer-cues.ts`);
  `clock/GameAttention` flashes the tab's title and plays the decision sound. The sounds, title and navigation are
  the browser's: `core/platform/page-cues.ts`. `session/event-pacing.ts` says how long each event holds the page (the
  legacy animation queue's durations, times the speed setting) and `GameSession` applies the events one by one by it;
  a replay (`?replayId=`) is played the same way. `table/GameCardInfo` adds the server's modifiers to the card
  information.
- **Coming back (D10).** A reload, or opening the game again in the tab, shows the page exactly as it was: the
  server sends the game, and the tab's session storage keeps the rest per game: the choices not sent yet, the view's
  state above (also the open pile dialogs, the card shown in the card information and the order of dragged cards), the
  chat box's lines in their order (the room's greetings on joining again are left out), and what
  the page made of the game that the server's full state doesn't say again (each character's last numbers, the
  turn's inactive cards, the Rule of 4 count, a decision the end of the game closed).
- **Taking a game back (undo)**, as the legacy page's `js/gemp-022/undo.js` does it, with the same words and in the
  same places, so players on either page play each other (the server: `LotroGameMediator.requestUndo`, the calls in
  `GameRequestHandler`, "taking a game back"). Core: `undo/undo-protocol.ts` reads the server's JSON, `undo/undo-view.ts`
  has the words (the tiers' marks, the list's headings, the countdown), `GameApi` the calls and `undo/GameUndo` the rest:
  the notices' own long poll (running all the time, so a request or the game's end reaches a player looking at a
  decision; the end asks for the game's news once, `GameSession.pollOnce`), what can be taken back (asked whenever an
  answer brings events, `GameSession.moved`), the Undo button's place (the latest, if green; sent `ownOnly`), the list,
  a request waiting with its countdown, and `frozen` while it waits (`GameDecisions` then answers nothing; the
  decision's buttons, dialogs and menu are `inert`). The game taken back: the page doesn't load again,
  `GameSession.reopen` reads the game again from nothing on the same page (answers to requests sent before are
  dropped; the choices not sent and what the page made of the game go; the chat box keeps its lines and gets the new
  game's from its line about the revert on) and the banner says so; a replay lays its table out again at its second P
  event, at once up to its GR. "Skip extra confirmations" (`skipConfirmations`, the legacy cookie) answers a
  `confirmOnly` decision with nothing. View (`ui/game/undo`, the DOM contract in each component): the Undo button in the
  alert box's lower right (`button[data-undo=last]`; the phone's divider has it too), "Revert..." and "Auto-decline
  revert requests" in the Options tab (`data-button=undoGame`, `data-setting=undoAutoDecline`; on the phone the menu's
  `revert` and the settings), `gd-undo-list` and `gd-undo-request` (non-modal dialogs, `data-dialog` undo-list and
  undo-request) and `gd-undo-banner`, in both themes and layouts, coloured by the `--theme-game-undo-*` values. Checked
  by `visual-check/game/undo.mjs` (see its README). The Undo button lies under a decision's large card dialog on this
  page: the legacy page's card dialogs now fit their cards (`openCompactCardDialog`), which this page hasn't ported.
- **View** (`src/ui/game/`): `GamePageComponent` (element `<gemp-game>`) is the frame, a CSS grid with the legacy
  page's proportions (`game-page.component.css` explains each number). `table/` draws the cards: `gd-game-cards` is a
  row (the shared `fitCards` with the `rows` fit, cards on whole pixels of the page), `gd-game-path` the adventure
  path, `gd-game-card-layers` what a game draws on a card (tokens, strength, vitality, site number or resistance;
  sizes are shares of the card, in CSS). `panel/` holds the game state panel, the stats bar and the alert box, `tabs/`
  the tab box (chat, settings with the timer alert rules, options, players), `pregame/` the panel shown until the
  players are known, `decision/` the decisions' dialogs (a number's spinner, a choice's buttons or list, cards to
  choose from, sized to their cards as a revealed hand's is: `gdCardWindow`) and a card's menu of actions, `dialogs/` the pile dialogs, the end-of-game feedback dialog and the
  dialog for a game that can't go on (no connection, not logged in, ...). `table/gd-game-animation` plays the event
  holding the page, `table/gdReorder` moves a card dragged sideways in its row, `table/gdCardHints` shows a card named
  in a text, `panel/gd-game-replay` has the replay's buttons.
- **Theme.** The game page has its own choice (`gempGameTheme` cookie, `PAGE_THEMES` in `theme.service.ts`); a player
  who hasn't chosen gets `modern` (the owner, 2026-11-16), one who chose `classic` keeps it. Its colours are the
  `--theme-game-*` values of `themes.css`.
- **Legacy quirks kept in classic**, so the page matches `game.html`: until the players are known the timer switch is
  a plain button that does nothing; a spectator still has the Options tab; the Rule of 4 line keeps its last count
  while hidden; a character the server's stats leave out keeps the numbers it last had; Shift doesn't flip the zoomed
  card; the page asks for news after the game's end too.
- **Modern** (the default; `?theme=modern`, remembered in `gempGameTheme`): the deck builder's dark
  gray with the game's own values (`--theme-game-*` in `themes.css`), laid out as the legacy page on a computer
  (`npm run check:game:modern`: the same games, every card and panel within 2px). The red theme button
  (`GameThemeButtonComponent`, in the table's top right corner) is off until the owner accepts its place
  (`GAME_THEME_PICKER`).
- **Scenario authoring** (`core/game/authoring`, `ui/game/authoring`): in a draft's game (the scenario's info says
  `author`, or `draftTest` for a player's game of the draft; never in a replay or the phone layout) the authoring panel
  is docked at the window's right, 430px wide (30px folded; floating in a corner in a test). The `Authoring` service
  holds the server's state of the draft (`/gemp-lotr-server/scenarios/author/*`, `AuthoringApi`; never sent with a
  `participantId`), keeps it fresh as the game moves, handles every answer (a fresh game is a full page load to its
  `gameId`, the panel's view coming back from `sessionStorage["gemp-author-ui"]`) and has the scenario guide behave as
  a draft's game needs (`ScenarioGuide.attachAuthor`). The page leaves the panel its room with `--gd-page-right` on the
  document element (`ui/shared/page-room.ts`), which the page's host, the dialogs, the card information, the hover zoom,
  the animations and the replay controls keep to; it is unset, so nothing moves, when no panel is docked. The tabs are
  one component each (Timeline, Step, Opponent, Board, Scenario); the widgets they share are in
  `ui/game/authoring/widgets/`. A message's anchors are picked on the table (`TablePicker`), by the same rules that
  point the messages at them (`ui/game/scenario/scenario-anchors.ts`). Checked by `npm run check:game:author` (a real
  browser writing drafts of its own; see `visual-check/game/README.md`). The legacy panel and overlay
  (`scenarioAuthor.js`, `scenarioOverlay.js`) are gone: comments that name them say where a behaviour came from.
  The Opponent tab's standing orders and rules are in `ui/game/authoring/opponent/` (`OrdersEditor` keeps the order
  open, its unsent form and the scroll across a fresh game in `sessionStorage["gemp-author-opponent"]`); a field with
  `gdAuthorKeep` (`widgets/author-keep.directive.ts`) keeps what is being typed in it, the focus and the caret across a
  fresh game (`sessionStorage["gemp-author-keep"]`). The When composer (`step/when-pickers.component.ts`: seat, the role in a two-sided
  draft, kind, what is asked, phase, turn) works on a `WhenHost` (`step/when-host.ts`): the Step tab's form
  (StepEditor) or an opponent's order (`opponent/order-when-host.ts`, sent with Apply).
- **Phone layout** (G7, `ui/game/phone`, `core/game/phone`): the modern theme where the deck builder uses its compact
  layout (`CompactLayout`, `COMPACT_QUERY`; classic always shows the computer layout) shows the game for fingers: the
  status bar and the players' chips, the table scrolling up and down (the whole adventure path, then the opponent's
  support and characters, the minions or the skirmish and assignments as pairs, the player's characters and support;
  support rows Free Peoples first, from `src/generated/card-sides.json`, which `scripts/extract-card-data.mjs` makes
  from the card definitions by the server's rule: a card's culture decides its side, Gollum's and culture-less cards
  say it themselves; the same side as the deck builder's Side filter, compared by `visual-check/game/card-sides.mjs`), each row scrolling sideways with its cards at one size (`PhoneSettings`: 100px on a phone,
  80 sideways, 130 on a tablet, times the card size setting), the divider with the decision and its buttons (dragged;
  its place per device in `gempGameSplit`; at the bottom it hides the hand) and the hand, where cards a decision offers
  from off the table are shown under a label. A tap acts only in a decision; a long press opens a card's details.
  A player's chip opens their piles: dead pile and removed pile for everyone, the adventure deck only its owner's,
  the discard pile its owner's unless the format makes it public (`discardPublic`, as the server decides).
  Sheets (`GameSheets`) stack: closing one shows the one below as it was. The open sheets, the scrolled table and rows
  and the hand's place are kept with the game (`GameViewState`, D10). "Your move" buzzes and plays a sound
  (`gempGameMoveVibrate`, `gempGameMoveSound`, on by default). Checked by `npm run check:game:phone`.
- **Checked** (see `visual-check/game/README.md`): `npm run check:game:table` shows every state of the recorded games
  on both pages, with the decision waiting at each, pixel by pixel; `npm run check:game -- --pages legacy,angular
--reload` plays real games on both pages with the same gestures, and reloads the Angular page after every step;
  `src/parity/game/angular-game.parity.spec.ts` replays every recording on the page in jsdom, which must send exactly
  the recorded requests; `src/parity/game/generated-games.parity.spec.ts` plays games made up from a seed
  (`src/testing/game/generated-game.ts`: random events that fit together and decisions of every kind, answered with the
  page's gestures) on the legacy page and then on the Angular page in jsdom, which must send the same answers and hold
  the same game after every step (4 games with the unit tests, 200 in `test:parity`; `PARITY_GAME_SEED=<seed>
PARITY_GAMES=1` repeats one); `npm run check:game:matrix` makes every gesture on every control on both pages.
  Differences the owner accepts go into `GAME_ACCEPTED` in `visual-check/accepted.mjs`.

### Scripted games (scenarios)

A tutorial or a puzzle is a game the server plays from a script (started with `POST /scenarios/start`); the hall
opens it on this page, which shows the script's messages over the game. `game.html` has no scenario support.

- **Core** (`core/game/scenario/`): `scenario-events.ts` reads what the script tells the page (who it is and how it
  plays, each step's messages and the answers it takes, leaving the script, the ending: JSON in the game's events of
  type S); `scenario-state.ts` keeps it in the game's model (`GameModel.scenario`, null in an ordinary game);
  `scenario-plan.ts` works out what a step lets the player do next (pure);
  `ScenarioGuide` is the game as the person plays it: which message is up, whether the decision may be answered yet,
  telling the server a step was read (`ScenarioApi`), starting over and leaving. Its messages also go into the chat
  box under the opponent's name (`GameLog.say`). `bubble-placement.ts` and `zoom-layout.ts` are the geometry of a
  bubble and of a card shown large; `ScenarioRoom` the room a panel docked at the right takes.
- **Decision gates** (`core/game/decision/decision-gate.ts`): `GameDecisions` asks every gate of the page's
  `DecisionGates` before it carries out a gesture (a card, an action, a button, a number, a choice, Escape) and tells
  them after it; a gesture goes ahead only if every gate allows it, and the gate that refuses tells the player. Any
  feature can add one (a request that freezes the decision, say); `ScenarioGuide` is one: a step takes only its
  answers, and the decision waits while a message is to be read.
- **Settings a game fixes** (`GameSettings.fix`): a script fixes auto-accept, the deck reveal and the animation speed
  for its game (its text says what a click will do); the page plays by them and shows them, the player's own stay in
  their cookies, and `fix({})` gives them back. A replay keeps the watcher's own.
- **View** (`ui/game/scenario/`): `gd-game-scenario` draws over the page (the bubble, joined to what a message is
  about by an arrow or lines; outlines round it and round what is to be clicked next; a hint at a click the step
  doesn't take; a card shown large), placed each frame against the page's DOM contract (`scenario-anchors.ts`);
  `gd-game-scenario-panels` the scenario's menu (Start again, Leave, Report a Bug), its ending's panel, and the
  notices that it can't be played here: the phone layout shows only "cannot be played on mobile devices" with a way
  to the hall, a window under 1000×600 offers to carry on anyway, and a hall open in another tab. A replay of a
  scripted game shows its messages in turn, without buttons or menu.
- **Checked** by `npm run check:game:tutorial` (`visual-check/game/tutorial.mjs`, against a game check server kept up):
  the tutorials played through on this page in both themes, their bubbles, menu, panels and replay.
- **Two-sided games, the game's own opening** (e3's A3 and B10): each step event says the role the person plays at
  that moment (`ScenarioStep.role`; in a two-sided game the Free Peoples player on their own turn, the Shadow player
  on the opponent's), whose turn and which. `ScenarioGuide.role` (`scenarioRole`: the step's, else the last said, else
  the seat) is what "self" and "opp" mean on the table: `AnchorContext.humanRole` gives the skirmish anchors
  (`ui:skirmish:selfStrength` and the like) the person's side as they play it now, on the page and in the authoring
  panel's picking. A number asked for (the bid) is guided like a click: the spinner's arrow towards the number the
  step takes is outlined, then OK (`NextClicks.numbers`); OK with another number is refused beside OK. The panel shown
  until the players are known, and the log lines the game wrote before the scenario said who it is, name the scripted
  opponent as the scenario does. Nothing is outlined to click while the page asks its own question (a minion left
  unassigned).
- **Messages by their place in the file**: a message whose condition doesn't hold is left out of the step event, so
  each message says its place among its step's messages in the file (`ScenarioMessage.index`, null for an older
  recording; `messagePlace`). The guide's author hooks (`preview`, `tryPlace`) and the log's lines go by it; the
  count on the bubble ("1 / 2") counts what is said. The authoring panel's "Not said here" reads it (`said.ts`).
- **Back to checkpoint**: the menu offers it while the game has reached a checkpoint (`ScenarioState.lastCheckpoint`,
  kept between steps; a game brought back to one is at it), after the page's own question; it starts the scenario
  again `from=checkpoint`. In a draft's game being written the panel plays the script to that checkpoint (as its Start
  again plays it from the start); a test of a draft has no such item (the panel starts a test again from where it
  began). An ending's `restart` (outcome.offScript.result) is read: a game that left its script is "not how this one
  goes" when the scenario has it played again (the default), a failure when it says `fail`.

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
gestures go to both through the selectors of `visual-check/deckbuilder/controls.mjs`, and after each one the test compares what
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

`npm run check:mutation` in `visual-check` changes one thing at a time in the deck builder's and the game screen's
logic (a comparison, a
condition, a constant, a message) and runs the unit tests of that file; a change no test notices is listed in
`visual-check/report-mutation/survivors.md`. It works on copies in `visual-check/.local-server/mutation/`, so the
`frontend` container never rebuilds from a mutated file. `--files <part of a path>` limits it, `--with-parity` adds
`src/parity`.

### The visual check

`visual-check/` holds the Playwright checks, separate from the Maven build (see `visual-check/README.md`): the deck
builder's in `visual-check/deckbuilder/`, the game screen's in `visual-check/game/`.

- `npm run check:stubbed` compares both pages with no server or login, using placeholder card art;
- `deckbuilder/run.mjs` (`npm run check`) compares both pages against a running server; it still uses the legacy element
  IDs.
