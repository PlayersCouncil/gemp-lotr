# The hall and the login page

Compares the Angular hall (`/gemp-lotr/ng/hall` and every tab under it) and login page (`/gemp-lotr/ng/login`) with the
legacy pages they replace, kept as `hall-legacy.html` and `login-legacy.html`, in a real browser and without a server.
It also follows the old addresses (`hall.html#...`, the site's start page) to where they now lead.

```
npm run check:hall
npm run check:hall -- --only events-calendar,login     # some scenarios (no navigation check then)
npm run check:hall -- --sizes 1366x768,1280x800 --tolerance 60 --workers 2 --out report-hall --no-navigation
CHECK_BROWSER_EXECUTABLE=/usr/bin/google-chrome npm run check:hall   # a browser already on the machine
```

It checks the Angular app as built in the server's web root (`gemp-lotr-async/src/main/web/ng`, or `CHECK_NG_BUILD`):
build it first (`npm run build` in `gemp-lotr-frontend`). The report is `report-hall/index.html`; the command exits with
1 when a scenario differs or an old address leads elsewhere.

## How it works

| File             | What it is                                                                                  |
| ---------------- | ------------------------------------------------------------------------------------------- |
| `run.mjs`        | Runs the scenarios on both pages, compares the screens, writes the report                   |
| `scenarios.mjs`  | The scenarios: where each page opens, and the steps both take                               |
| `stubs.mjs`      | The server's answers to the hall's and login page's calls, for `stub-server.mjs`            |
| `navigation.mjs` | The old addresses: where `hall.html#...` and the start page lead, as a player and a visitor |

- **Pages.** Both are served from disk by `../stub-server.mjs`; the server's answers come from
  `src/testing/server-responses` (the files the unit tests read), through `stubs.mjs`. A visitor (`stubs.visitor`) is
  refused (401) every call that needs a login. The hall's long-poll and chat answer after 4 s, as the server does when
  nothing changed. Card images are labelled placeholders on both pages.
- **Time.** Each page's clock starts at 2026-11-20 12:00 UTC (the fixtures' month) and runs on: a stopped clock would
  stop the legacy page's jQuery animations.
- **Scenarios** (`scenarios.mjs`): each opens the legacy page (`hall-legacy.html#<tab>`, the legacy hall's own link to
  a tab) and the Angular page (its route) side by side, then takes both through the same steps, each named once per
  page (a gesture of `../gestures.mjs`, or a field filled, a key, a wait). The screens are compared after loading and
  after every step (`../compare.mjs`): a screenshot differs when more than `--tolerance` pixels (60) differ by more
  than 2 shades. A step that can't be done on a page (its element not there within 5 s) is a difference, and so is an
  error on the Angular page; the legacy page's errors and calls the stubs don't answer are listed under the scenario.
- **The old addresses** (`navigation.mjs`, not a screenshot check): every legacy fragment (tabs, sub-tabs, a format's,
  league's, tournament's or errata's own link, an unknown one, Admin) is opened on the legacy hall
  (`hall-legacy.html#...`) and through `hall.html#...`; the tab and sub-tab shown must be the same (the legacy hall's
  `GempLinks.current()`, the Angular frame's `data-tab` and selected sub-tab). The start page `/gemp-lotr/#...` must
  lead a player where `hall.html` did, and a visitor to the login page, which comes back (`?return=`) to that place.
  `hall.html#...` as a visitor still opens the tab. An admin's Admin link must be
  `hall-legacy.html?admin=1#admin` and open the legacy hall on its Admin tab, starting no hall or chat of its own.
- **Accepted differences**: `HALL_ACCEPTED` in `../accepted.mjs`, areas of the Angular page where some differing
  pixels don't count (up to an entry's `maxPixels`). Only the owner (Ketura) adds to it. It holds what Ketura accepted
  on 2026-11-20, after the pixel pass: each screen at 1366x768 may differ by about what it measured then, so the check
  passes as the pages are and fails when a screen moves further from its legacy page. Other window sizes have no
  entries. `results.json` has every screenshot's whole count (`differing`).
- **Left out on purpose**: the login page's picture (a scenario's `styles`). The legacy stylesheet sized it to the
  page's content by a typo and the Angular page fills the window, as Ketura wants it; the rest of the page is compared.

## What it covers

The login page and registering; the frame as a player (the connection readout) and as a visitor (a public tab, and
the "Not logged in" dialog); the Game Hall tab's tables, queues, tournaments, World Championship and ready check, the
sections closed, a queue's prizes, Join Table on another player's table, every Play flow (scenario, bot, casual with a
refusal, league, tournament) and Back; every sub-tab of Help (a format's own link, PC Errata with a card, its original
and a filter), Events (calendar previews and the next month, a league's drawer and matches, a league's and a
tournament's own link, a completed tournament's details and the month list), Server Info (the patch notes' month list)
and My Account (the player picker, Copy Public…); the old addresses and the Admin link.

## What it doesn't cover

Logging in or registering for real, creating or joining for real (the stubs refuse those calls with 404), the hall
changing while open (the long-poll answers nothing new), the chat being used, the Admin tab itself (the legacy hall's),
window sizes other than the ones asked for, other themes (the hall is classic only), and the open and close animations
(the screenshot is taken after them). The Discord chat embed is left out: both pages use the built-in chat.
