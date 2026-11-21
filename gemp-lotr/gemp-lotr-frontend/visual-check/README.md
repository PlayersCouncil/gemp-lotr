# Visual checks

Playwright checks that compare the Angular pages with the legacy pages they replace, in a real browser. They are not
part of the Maven build: run them by hand after a change (`npm install` here once). Every command runs from this folder
and writes its report to a `report-*` folder here.

- **`deckbuilder/`**: the deck builder (`/gemp-lotr/ng/deckbuilder/` against `deckBuild.html`): `npm run check`,
  `check:stubbed`, `check:local`, `check:matrix`, `check:phone`, `check:selftest`, `check:legacy-decks`,
  `check:deploy`. See [`deckbuilder/README.md`](deckbuilder/README.md).
- **`game/`**: the game screen (`/gemp-lotr/ng/game` against `game.html`): `npm run check:game`, `check:game:table`,
  `check:game:devices`, `check:game:phone`, `check:game:modern`, `check:game:phones` and more. See
  [`game/README.md`](game/README.md).
- **`game/tutorial.mjs`**: tutorials (scripted games) played through on the Angular game page, their bubbles, menu
  and panels: `npm run check:game:tutorial`, against a game check server kept up. See
  [`game/README.md`](game/README.md#tutorials-scripted-games).
- **`game/author.mjs`**: the scenario authoring panel docked beside the Angular game page, writing drafts of its own:
  `npm run check:game:author`, against a game check server kept up. See
  [`game/README.md`](game/README.md#the-authoring-checks).
- **`hall/`**: the hall and the login page (`/gemp-lotr/ng/hall`, `/gemp-lotr/ng/login` and every tab against
  `hall-legacy.html` and `login-legacy.html`), stubbed, and the old addresses (`hall.html#...`, the start page):
  `npm run check:hall`. See [`hall/README.md`](hall/README.md).
- **`npm run check:all`** (`tools/check-all.mjs`) runs the important checks of all of them.
- **`npm run check:failed`** (`check:all --failed`) runs again only what failed in the last `check:all`: the parts
  that passed are left out, and each failed part runs only its failed scenarios (or `--checks` parts). It builds the
  Angular app first, not the server: after a Java change, run `check:all`. Its reports then hold only what ran again.
- **`node tools/failed-checks.mjs <report>`** prints the `--only`/`--checks` arguments that run one check again on only
  what failed in its report, e.g.
  `npm run check:game -- --out report-game $(node tools/failed-checks.mjs report-game)`.

Shared by all of them:

| File              | What it is                                                                            |
| ----------------- | ------------------------------------------------------------------------------------- |
| `accepted.mjs`    | The differences the owner has accepted (only the owner adds to it)                    |
| `browser.mjs`     | Which browser a check runs in (`CHECK_BROWSER`, `CHECK_BROWSER_EXECUTABLE`)           |
| `compare.mjs`     | Comparing two screenshots, or where controls are                                      |
| `stub-server.mjs` | Serving the pages from disk, without a server (a check can add answers of its own)    |
| `gestures.mjs`    | The mouse and keyboard gestures every check makes                                     |
| `touch.mjs`       | Touch on a phone (tap, long press, drag)                                              |
| `local-server/`   | The throwaway server the checks start (compose files, test data)                      |
| `tools/`          | Building the check server, `check:all`, mutation testing, the tunnel, measuring tools |
