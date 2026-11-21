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
- **`tournament/`**: the basic life of a tournament on a real server, about four minutes: `npm run check:tournament`
  (`-- --keep` leaves its server up, `check:tournament:down` stops it). The admin's side goes through the Tournament
  Admin page in a browser, the players talk to the server over HTTP. It checks that a player-made sealed queue starts
  when its ready check is confirmed, that a scheduled tournament starts at its start time and keeps its manual
  kickoff, that an admin can take an uninvited player out of a queue and out of a running tournament for good, and
  that setting a stage kicks a tournament off without touching the queues and tournaments around it. Not part of
  `check:all`.
- **`npm run check:all`** (`tools/check-all.mjs`) runs the important checks of both.
- **`npm run check:failed`** (`check:all --failed`) runs again only what failed in the last `check:all`: the parts
  that passed are left out, and each failed part runs only its failed scenarios (or `--checks` parts). It builds the
  Angular app first, not the server: after a Java change, run `check:all`. Its reports then hold only what ran again.
- **`node tools/failed-checks.mjs <report>`** prints the `--only`/`--checks` arguments that run one check again on only
  what failed in its report, e.g.
  `npm run check:game -- --out report-game $(node tools/failed-checks.mjs report-game)`.

Shared by both:

| File              | What it is                                                                            |
| ----------------- | ------------------------------------------------------------------------------------- |
| `accepted.mjs`    | The differences the owner has accepted (only the owner adds to it)                    |
| `browser.mjs`     | Which browser a check runs in (`CHECK_BROWSER`)                                       |
| `compare.mjs`     | Comparing two screenshots, or where controls are                                      |
| `stub-server.mjs` | Serving the pages from disk, without a server                                         |
| `gestures.mjs`    | The mouse and keyboard gestures every check makes                                     |
| `touch.mjs`       | Touch on a phone (tap, long press, drag)                                              |
| `local-server/`   | The throwaway server the checks start (compose files, test data)                      |
| `tools/`          | Building the check server, `check:all`, mutation testing, the tunnel, measuring tools |
