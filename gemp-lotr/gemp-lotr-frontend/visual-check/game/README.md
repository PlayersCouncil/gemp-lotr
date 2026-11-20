# Game screen check

The game screen's counterpart of the deck builder's visual check (`../deckbuilder/README.md`), for the Angular port of `game.html`
(trymarz/gemp-lotr-private#27). It plays each scenario of `scenarios.mjs` as a **real game** on two pages, one after
the other, and compares the pages after every step:

- the screen (at most `--tolerance` pixels, default 60, differing by more than 2 colour levels), with the clocks and the
  chat's times masked in magenta;
- what the page sent the server: every decision answered (id and value), card information asked for, concede and
  cancel, chat messages. The long polls aren't compared: when they are sent depends on timing;
- the browser's own dialogs, with their text (the check accepts them);
- the game as the page shows it (`controls.mjs` `readGameState`): twilight, phase, initiative, rule of 4, the decision's
  text and buttons, open dialogs, the action menu, every player counter, and every card with its zone and assignment;
- page errors.

Page A is `game.html`. Page B is `game.html` too until the Angular game page exists: that run proves the two games
are the same and the check is stable (0 differ). Then `--pages legacy,angular` compares the port, which must provide
the DOM contract in `controls.mjs` (`ANGULAR_CONTRACT`: `data-card-id`, `data-zone`, `data-decision-*`, `data-game`,
`data-tab`, `data-pile`, ...).

## Running it

Needs only Docker, the `gemp_app` image and a server jar with `GameRandom` (`npm run check:server-build` builds one
from this checkout without touching the development jar; `CHECK_WEB_JAR` picks another one).

```
npm run check:game                                  every scenario at 1280x800, 1366x768, 1920x1080
npm run check:game -- --only opening --sizes 1366x768 --verbose
npm run check:game -- --keep                        leaves the server up (port 17201), login in .local-server/game/
npm run check:game:down                             stops it
npm run check:game:selftest                         each deliberate bug of injections.mjs must be reported
npm run check:game:record                           records every scenario for the jsdom tests (see "Recordings")
```

`local.mjs` starts a throwaway server of its own (`../local-server/compose.yml` under the project name
`gemp-game-check`, port 17201 or `GEMP_GAME_CHECK_PORT`, an in-memory database), creates the players of
`players.mjs` with a random password that is never written down, saves the test deck for each, runs `run.mjs` and
removes everything again. Neither the development server nor the deck builder's check server is touched.

Options of `run.mjs` (passed through `local.mjs`): `--only`, `--sizes`, `--workers` (at most 4, one pair of players
each; in Firefox, `CHECK_BROWSER=firefox`, 1 by default: Firefox keeps one mouse for the whole browser, so a drag on
one page was ended by another page's click; the table check too), `--tolerance`, `--out` (default `report-game`), `--pages` (default `legacy,legacy`), `--legacy` and `--angular`
(the pages' paths), `--inject <name>` (a bug of `injections.mjs` in page B), `--real-images` (card pictures from the
image server instead of placeholders), `--verbose` (every decision and answer of both players), `--record` (see
"Recordings"), `--theme modern` (the Angular page's theme) with `--geometry` (where every card and the panels around
the table are, within `--margin` px, default 2, instead of the pixels: a theme laid out as the legacy page; the game as
shown, the requests and the dialogs are compared as always). The table check has `--theme` and `--geometry` too.

The report is `report-game/index.html`: each scenario with its steps, both screenshots of each and the differences in
red, and what no scenario covers.

## Classic on every device

The owner (2026-11-16): players who keep the classic look must get exactly what `game.html` gives them, also on a phone
or a tablet. Two checks compare the legacy page and the Angular page in the classic theme on a computer (1366×768), a
tablet upright (768×1024) and sideways (1024×768), and a phone upright (390×844) and sideways (844×390); phones and
tablets as their browsers are (`device-profiles.mjs`: touch, a mobile browser that reads the viewport tag, a sharp
screen):

- `npm run check:game:devices` (`devices.mjs`, on the recorded games without a server):
  - **screens**: each recording's last state: the pixels, the game as shown, and what the browser makes of the page (its
    layout's width and height, its zoom);
  - **zoom**: whether a finger may zoom the page and what zooming does. A headless browser doesn't zoom for two
    fingers, so what decides it is compared: the viewport tag and the touch rules (`touch-action`) in effect under a
    grid of points; then a touchpad's pinch, a pan, and the browser's bars sliding away (the window taller): both pages
    end zoomed and moved alike, the zoom kept;
  - **switch**: the same, the Angular page switched from modern to classic with the red button (which opens the page
    anew: a phone gives a page that had a viewport tag no way back to none, see `PageTheme.reloadForNoViewport`).
  - `--eval "<expression>"` prints what it gives on both pages instead.
- `npm run check:game:phones` (`run.mjs --devices`, real games on the game check's own server): every scenario on both
  pages, made with a finger: a tap for a click, a long press for a right or shift click, a finger's drag for a drag;
  hovering is left out (a finger can't).

Modern on a computer is `npm run check:game:modern` (where every card and panel is, against the legacy page).

## The card mechanics check

`npm run check:game:mechanics` (`mechanics.mjs`) goes through what cards do to each other, mechanic by mechanic, in the
modern theme (the owner, 2026-11-20): each is a prepared state of a recorded game (`phone.mjs` `STATES`, the server's
events added to the last answer as the server sends them), opened on a phone and a tablet upright (the phone layout), a
computer and a phone held sideways (844x390, the computer layout on a low screen), and checked from what the page shows
of each card (`data-card-id`: its zone, `data-hindered`, its picture, numbers and tokens, where it lies):

- **hindered**: a companion, a minion, a possession in support and an attached card turned face down (FCIP): their
  backs instead of their faces, the characters' numbers hidden;
- **restored**: turned up again: numbers hidden until the stats name the card again (as game.html), then back;
- **tokens**: wounds, burdens and culture tokens, as many as put there;
- **moved**: an attached card moved to another character (MCIP): it lies by its new bearer;
- **siteControlled**: a site the opponent controls (MCIP with `controllerId`): in the opponent's support row;
- **threats**: threats and the Shadow's initiative in the status;
- **revealedHand**: the opponent's hand revealed (`handRevealed`): its cards in the opponent's piles;
- **metaSite**: an RTMD meta-site in support, drawn as game.html draws it (the visual card's picture with the
  modifier's bottom over it, `Card.CreateSplitCardDiv`), against the same card with no pre-game setup naming it.

It also checks that its list is whole, from the server's code: every attribute of a game event and of the game's stats
(`EventSerializer.java`) is a mechanic here, shown by another check, or not shown by game.html either (`FIELDS`, each
with its reason), so a field the server starts sending fails the check until it is looked at ("not covered"); and every
token kind (`Token.java`) a card can add (`AddCultureTokens` in the cards' definitions) has a picture in `images/tokens`
(the Hobbit Draft cultures have none, but no card adds them yet: printed as a note). The report holds a screenshot of
each mechanic on each device, for looking at.

## The phone check

`npm run check:game:phone` (`phone.mjs`) checks the **phone layout** (G7 of trymarz/gemp-lotr-private#27: the modern
theme on a phone and a tablet held upright, 390x844 and 768x1024 with a touch screen; held sideways both get the
computer layout, the owner 2026-11-20) on the
recorded games served without a server, as the table check does (`stub-game.mjs`, shared with it). Nothing is compared
with the legacy page; each check compares the Angular page with itself:

- **back**: a long press on a card in every place a card's details open from (the table's rows, an attached copy, the
  hand, the path, cards offered in the hand's place, an assignment, the skirmish, the player's and the opponent's
  piles from their chips, the menu's piles, an attached card's details, the hand hidden), then the details closed with
  ✕, Escape and the backdrop: the view must be what it was before the press (`readView`: the open sheets, where the
  table, each row and the sheet are scrolled, the divider, the hand's place, the cards marked for the decision; and the
  screen, the clocks masked, within 10 pixels of rendering noise). 15 places × 3 ways × 3 devices.
- **reload** (D10): the divider dragged, the hand hidden, the menu, piles and a card open and scrolled, the offered
  cards closed with ✕: after a reload (which gets the whole game in one answer, as from the server) the view's data is
  the same, and its screen within 60 pixels more than 10 shades off (the edges of rounded rings, shadows and the
  overlapping glows of offered cards are blended a few shades differently when everything is drawn anew).
- **touch**: a tap on a card not offered does nothing, a tap on an offered card chooses it, the divider drags and is
  kept per device in `gempGameSplit`, dragged to the bottom it hides the hand and dragged up brings it back, and the
  hint to drag a hidden hand up is there too. Minions stacked on a
  possession and offered by the decision (`STATES.underOffer`) show before the hand, smaller than its cards and bigger than the copy, behind a small copy of
  the possession, and a tap there chooses one (`under-offer-<device>.png`).

- **piles**: who sees a pile's cards, by the server's rules (`Zone`, `LotroFormat.discardPileIsPublic` in
  `lotrFormats.hjson`): the dead pile and the removed pile everyone; the adventure deck its owner; the discard pile
  its owner, and the opponent too in a format that makes it public (the PC formats). Checked with the opponent's
  cards in every pile, in a format of each kind.

No recorded game puts cards in the opponent's piles, so they are added to the last answer (`STATES.opponentPiles`), with
the format's discard pile made public for `STATES.opponentPilesPublic` (the P event's `discardPublic`).

- **animation**: a recorded game played from its start at the normal animation speed, sampled every frame: each
  opponent's card put into play is shown over everything and stays whole on the screen, and no card is drawn bigger
  than it is in its row (which would clip it).

- **sites**: on each device, the number over each site card on the path lies on the card's left half, where the
  card prints its own number, never over the Shadow number on its right, and clear of the players' markers; the path
  is saved as `sites-<device>.png`.

- **fold**: on each device, in a recorded game's own Free Peoples turn and its Shadow turn, the cards not active this
  turn fold away: the player's are shrunk, the opponent's hidden behind a "Show N" button. A tap on the row's button
  opens them (full size, or shown), another folds them again, and a row opened stays open after a reload.

- **archery**: on each device, with the archery totals the server sends in the archery phase (`STATES.archery`; the
  recorded games pass that phase within one answer), the status bar shows a Free Peoples pill and a Shadow pill with
  the computer layout's icons, first among the pills and not cut off (`archery-<device>.png`).

- **stack**: stacked cards (minions stacked on a possession, `STATES.underOffer`) lie on their side, and stand upright
  with the "old look for stacked cards" setting (cookie `use-old-stack`), on each device and on the computer layout of
  the same page (1366x768).

Two checks against a game check server kept up (`npm run check:game -- --only start --sizes 1366x768 --keep`, then
`npm run check:game:down`):

- `node game/card-sides.mjs`: the Shadow cards of `src/generated/card-sides.json` (which the phone's support rows sort
  by) against the side the server gives every card of "All cards" (`/collection/default`, the deck builder's Side
  filter's): 3997 of 3997 the same.
- `node game/discard-formats.mjs`: a game started in `fotr_block` and in `pc_fotr_block`; the server tells both players
  the discard pile is private in the first and public in the second. (Movie Block and Expanded need King block sites,
  which the test deck hasn't.)

```
npm run check:game:phone                                  everything (about 25 minutes)
npm run check:game:phone -- --devices phone --checks back one device, one check (back, reload, touch, piles, animation, sites, fold, archery, stack)
npm run check:game:phone -- --shots --only opening        screenshots of a recording's last state, nothing checked
```

Differences are written to `report-game-phone` (the view before and after, and the differing pixels in red).

## The table check

`npm run check:game:table` (`table.mjs`) compares the Angular game page's **table** with `game.html`, without a server:
both pages are served from disk (`../stub-server.mjs`: the legacy web root and the Angular build in it) and get a
recorded game (see "Recordings") answer by answer, as its server sent it, up to one step; the decisions are left out,
so no dialog is open and no card is offered, and each page asks for the next answer at once. Then the pages are
compared: the screen (at most `--tolerance` pixels, default 60) and the game as shown (`controls.mjs`
`readGameState`). Every step of every recording, at 1280x800, 1366x768 and 1920x1080.

On the last state of two recordings (`WITH_VIEWS`: a player's and a spectator's) it also compares what the table
doesn't show: each tab of the tab box and each player's other piles opened. Then it loads the Angular page again,
which must show exactly what it showed before (the open tab and piles are the page's own, kept per game in the tab's
session storage).

```
npm run check:game:table                                       everything (291 comparisons, about 6 minutes)
npm run check:game:table -- --only opening --sizes 1366x768    one recording, one size
npm run check:game:table -- --steps last                       only each recording's last state
npm run check:game:table -- --only opening --step 3            one step
npm run check:game:table -- --generated 100 --steps last       100 made-up games instead of the recordings
```

`--generated <count>` (from `--generated-seed`, default 1) shows games made up from a seed instead of the recordings
(`../../src/testing/game/generated-game.ts`, built with esbuild: random events that fit together, decisions of every
kind), named `generated-<seed>`; the jsdom parity test plays the same games on both pages
(`src/parity/game/generated-games.parity.spec.ts`). They found what the recorded games never showed: a turn naming no
inactive cards leaves them as they were, a skirmish's boxes stay empty until stats come after it started, an offered
inactive card's tokens are only lightly dimmed, a card's numbers stay hidden after it is turned face up until stats name
it, and the sites' places are added up as the legacy page did (six sites in 533px landed a pixel apart). Two legacy
glitches are kept out of the generated games and listed for the owner in trymarz/gemp-lotr-private#27: an opponent's
stacked card ending its flying in off its place, and a card flipped twice in one answer staying squeezed.

To look into a difference, `--boxes "<legacy selector> | <angular selector>"` prints the box of every match on each
page, and `--eval "<expression>"` what an expression gives in each; with `--only`, `--sizes` and `--step`. The report
is `report-game-table/index.html` (both screenshots and the differing pixels of each state that differs).

It needs a build of the app in the legacy web root (`npx ng build --configuration development`, or the `frontend`
Docker service's). The decisions, the dialogs and the gestures are compared by the game check above, which plays real
games: from G3 on with `--pages legacy,angular`.

Each state is shown with the decision that waited at it (`--no-decisions`: the table alone), so the decisions' dialogs,
the alert box and the cards offered are compared too. `--cookie name=value` sets a setting on both pages (e.g.
`--cookie alwaysDropDown=true`).

Result on 2026-11-15 (G4), `--pages legacy,angular --reload` at 3 window sizes: every scenario the same except
`card-info` and `card-gestures` (the card information's size slider: Chrome draws its focus ring around the range
input but not around the legacy slider's link, about 150 to 260 px) and the concede scenarios' last step after a
reload at 1280x800 and 1366x768 (the card pictures decoded again by the browser).

## Coverage

`coverage.mjs` lists what the scenarios must cover together: every kind of game event and decision the server sends
(read from `GameEvent.java` and `AwaitingDecisionType.java`), every zone a card can be shown in (`Zone.java`), and every
gesture and control of the game page (`GAME_CONTROLS`: the decision buttons, card gestures, tabs, each setting, the
piles, Escape, the spectator and replay modes and the replay's buttons). A run of every scenario records what page A
was sent and what the check used, and fails on anything left (`GAP` lines and the report). `NOT_EXPECTED` lists only
what can't happen, each with its reason (cards in the void are never sent to a page; event `CM` is never created).

## The gesture matrix

`npm run check:game:matrix` (`run.mjs --matrix`, scenarios generated by `matrix.mjs`) makes every gesture a control can
get on every control of the game page, on both pages, and compares them after each one (the deck builder's
`check:matrix`, whose gestures it uses). One real game per state: the bid (its arrows), the player's fellowship phase
(the tabs, the chat field, the switches, the counters' `+`, every pile counter, a site, Frodo, Gimli and a card of the
hand), the settings tab (every box, the slider, the timer alert fields, the foil list), the options tab, a pile's dialog
and the card information (their close buttons, the card size slider), and a replay (its buttons). Checkboxes and
switches are pressed an even number of times so the next control finds the page as before; a pile opened by a gesture
is closed again unseen. Left out, each with its reason in `NOT_APPLICABLE`: what answers the decision (a card the
decision offers, the decision's buttons), conceding, playing the replay; other scenarios press those.

`--control <names>` (comma-separated parts of step names) keeps only those gestures, for looking into one control;
the table check's `--gesture "<gesture>@card:<id>|point:<x>,<y>|<legacy selector>|<angular selector>"` (several may be
given) makes gestures on both pages of a recorded game before comparing them, without a server; `--console` prints
what the pages write to their consoles.

Differences the owner accepts go into `GAME_ACCEPTED` in `../accepted.mjs` (for this check and `check:game`), each
with the number of its decision in trymarz/gemp-lotr-private#27. An entry names the scenarios and either a pattern for
the problem's text (with, for a screen difference, at most how many pixels), or an area of the Angular page (a
selector, read again at every screenshot, e.g. `:focus`) in which at most so many differing pixels don't count. Only
the owner decides what goes in. `check:all` runs the matrix after the game scenarios. The theme button (the Angular
page's only, in the table's corner) is hidden in the screenshots (`ANGULAR_CONTRACT.screenshotStyle`).

Result on 2026-11-15, with the owner's decisions 5, 6, 7 and 19 accepted: the bid, the dialogs and the options the
same. Still differing: the typed chat line (decision 8, drawn a little narrower; it stays in the box, so about 350 px in
every later step of the table), the replay's play button reached by the keyboard (decision 24), and a few steps that
differ by timing from one run to the next (decision 15: a card's information or zoom caught while it opens).

`node tools/zoom-crop.mjs --area x,y,w,h --scale 4 --out out.png a.png b.png diff.png` cuts the same area out of a
report's screenshots and puts them side by side, enlarged, to show what differs.

## How both pages get the same game

- **The seed.** The check's server runs with `-Dgemp.game.randomSeed`: every game draws from its own `Random` with that
  seed (`GameRandom`: shuffles, random cards, the seating), a table's players are in name order, a decision lists
  cards it holds in a set in card id order, required responses in the order of their card and text, and card
  information its modifiers sorted. Tables may also use the formats the hall doesn't offer (the Debug decks below).
  Without the property the server is unchanged (`ThreadLocalRandom`, hash order, hall formats only).
- **The opponent** (`protocol.mjs` `Foe`) plays over HTTP by a fixed policy: bid 0, the first choice, the fewest cards,
  pass; a scenario's `foe` rules come first (e.g. let the player go first, play minions). When both players are asked
  at once (bids, starting fellowships, mulligans) the opponent waits for the player in the browser to answer, because
  the server's order depends on who answers first.
- **The player** in the browser makes each step with real gestures: clicks on cards, the spinner's arrows, the
  dialog's buttons, the action menu, right clicks, keys. A step ends when the next decision for the player arrived,
  the opponent has nothing to answer, no request but the long poll is open, nothing animates and the page hasn't
  changed for 500 ms.
- **The modes.** In `spectator` scenarios both sides are played over HTTP and a third player watches in the browser; in
  `replay` scenarios both sides are played over HTTP, the player concedes, and the browser opens the game's replay
  (`game.html?replayId=<player>$<recording>`, from `GET /gameHistory`). Both stop after `answers` answers in all, so
  the game stops at the same point every time.
- **One at a time.** The players (the bots, then the page) join a game one after the other, after the game can be read:
  the same order of joins on every run. (Joining at the same moment used to fail with HTTP 500, a
  `ConcurrentModificationException` in `LotroGameMediator.signupUserForGame`, which held only the read lock; fixed,
  `LotroGameMediatorSignupTest`.) Two clicks on the same card are at least 250 ms apart, as the legacy page ignores a
  second tap within 200 ms (its touch-bounce guard), and an `auto` step answers once the page has stopped moving.
- **The same pair** of players plays a scenario's game on both pages, so the names on the screens are the same. The
  game's own chat room is named after the game ("Welcome to room: Game12"), so the check shows both pages the same
  name, `Game#`. The legacy chat marks a message naming the player only once it knows the player (`GET /player`): the
  check lets that answer arrive before the game's first one on both pages.
- **Placeholders** stand in for the card pictures (as with `--stubbed` in the deck builder's check), so a picture the
  image server sent late can't make a difference. The animation speed setting is the fastest on both pages.
- Not compared: the page's title (the legacy page flashes it on a timer while a decision waits), the clocks and the
  chat's times (masked).

## Recordings

`npm run check:game:record` (`run.mjs --record`) plays every scenario on page A only and saves what the page and the
server said to each other, with the game the page held after each step (`controls.mjs` `readLegacyModel`) and the
gestures that send nothing (assignments), in `../src/testing/game-recordings/<scenario>.json` (replays aren't recorded).
They are test data of the frontend: `src/parity/game` plays each one back on the unchanged legacy game page in jsdom
(`src/testing/game`: `FakeGameServer`, `openLegacyGamePage`, `playUntil`), which must send exactly the recorded
requests through the check's gestures and hold the same game after every step. The Angular game page will be tested
the same way from G1 on. Record again after changing a scenario, the test deck or the server.

## Scenarios

`scenarios.mjs` explains each kind of step. Both players play the same deck (`players.mjs` `DECKS`): `main`,
Fellowship Block, Dwarves and Elves with Moria orcs; and four small decks in the Debug format (only the seeded server
plays it), where the hand is the whole deck once the starting fellowship is out, so a scenario knows what it can play:
`skirmish`, `transfer`, `shards`, `rescue` and `overwhelm`.

| Scenario                         | What it does                                                                                                |
| -------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `start`                          | The page as it opens: the bid                                                                               |
| `opening`                        | Bid (the spinner), starting fellowship (a card in the dialog, then Done), no mulligan                       |
| `fellowship-play`                | A companion played, a possession played and put on its bearer, a card's action, moving                      |
| `minions-and-skirmish`           | The opponent plays a minion; it is assigned to Frodo, the skirmish chosen, its action passed, Frodo's wound |
| `minions-unassigned`             | Done with a minion unassigned: the browser's confirmation                                                   |
| `skirmish-event`                 | A minion assigned, unassigned and assigned again; Axe Strike played in the skirmish                         |
| `shadow-turn`                    | The player's shadow phase: a minion played, the phase passed, reconcile                                     |
| `reconcile-discard`              | Without auto-accept: a card chosen in reconcile, Reset choice, chosen again and discarded; the discard pile |
| `settings`                       | Every setting changed; a bid of 1, choices from lists, Clear selection, Done, Escape; a chat message        |
| `card-info`                      | Card information with the server's modifiers (right click), closed by a click, another card                 |
| `card-gestures`                  | Card information by shift click and by dragging up; a card of the hand dragged to another place             |
| `hover-zoom`                     | The hover zoom on a card in play and on a site                                                              |
| `tabs`                           | Settings, options, players and chat at the bottom left                                                      |
| `piles`                          | Both players' other counters; the adventure deck, dead, discard and removed piles                           |
| `concede`                        | Cancel and concede while a decision waits (see below)                                                       |
| `concede-while-opponent-decides` | The page polls: cancel (the server's warning), concede, the feedback dialog, the deck after the game        |
| `spectator`                      | A third player watches a game both sides play over HTTP                                                     |
| `replay`                         | The replay of a short game: slower, faster, played to the end                                               |
| `effects-skirmish`               | Gimli with the Dwarven Axe and Let Them Come! beats an Orc: two required responses; For Frodo (removed)     |
| `effects-transfer`               | The Dwarven Axe transferred from Gimli to the Dwarf Guard, who then dies to an Uruk-hai (damage +1)         |
| `effects-shards`                 | The Shards of Narsil: a card stacked, then the menu of its two actions, the second chosen                   |
| `effects-rescue`                 | Merry, not assigned, is discarded to remove a [sauron] Orc from Gimli's skirmish                            |
| `shadow-overwhelmed`             | Gimli, Son of Glóin, with the Dwarven Axe exerts for +2: 10 against 5, the Shadow total turns red           |

### The new master of 2026-11-17

PlayersCouncil's master of 2026-11-17 (`patchnotes/2026-11-17.md`) changed what the game page shows. The scenarios
cover it as follows (recorded again on a server built from it, `npm run check:game:record`):

- **An action that would do nothing** (`noEffect`, a pale yellow double border): `effects-skirmish` (Sam, Son of
  Hamfast, "effect", in three Fellowship decisions), `minions-and-skirmish` and `skirmish-event` (Axe Strike in the
  hand, "skirmish"). The action menu's dimmed entry with its reason needs a card with two actions of which one does
  nothing, which none of the test decks has: the unit tests cover it (`decision-ui.spec.ts`, `decision-view.spec.ts`).
- **The Shadow overwhelmed** (`shadowOverwhelmed`, its total red): `shadow-overwhelmed`.
- **Not covered by a scenario**: The One Ring staying on the board after the Ring-bearer is killed (Frodo with the Ring
  is 4: a small deck can't bring the Shadow the twilight for twice that), and the infinite loop questions (they use
  the number and choice decisions every other scenario covers). Both are the server's: the page shows what it sends.

The page doesn't poll while a decision waits for the player. Conceding then used to leave the page unaware that the
game had ended; now the page asks once more after its concede is answered, and closes the open decision when the game
ends (`concede`; legacy `gameUi.js`, owner's decision of 2026-11-13). The server answers a cancel request with a
warning, "You can't cancel this game", even when the opponent may still agree.

Result on 2026-11-12, both pages `game.html`: 54 of 54 checks the same (18 scenarios at 3 window sizes, about 9
minutes), 10 items not covered (so the run fails until they are); the 17 recordings replay in jsdom; the self-test
catches 6 of 6 deliberate bugs.

Masks are drawn over everything, so a clock under an open dialog also hides that part of the dialog.
