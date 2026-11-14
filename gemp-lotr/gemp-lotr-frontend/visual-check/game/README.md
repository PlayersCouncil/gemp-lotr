# Game screen check

The game screen's counterpart of the deck builder's visual check (`../README.md`), for the Angular port of `game.html`
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
each), `--tolerance`, `--out` (default `report-game`), `--pages` (default `legacy,legacy`), `--legacy` and `--angular`
(the pages' paths), `--inject <name>` (a bug of `injections.mjs` in page B), `--real-images` (card pictures from the
image server instead of placeholders), `--verbose` (every decision and answer of both players), `--record` (see
"Recordings").

The report is `report-game/index.html`: each scenario with its steps, both screenshots of each and the differences in
red, and what no scenario covers.

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
```

To look into a difference, `--boxes "<legacy selector> | <angular selector>"` prints the box of every match on each
page, and `--eval "<expression>"` what an expression gives in each; with `--only`, `--sizes` and `--step`. The report
is `report-game-table/index.html` (both screenshots and the differing pixels of each state that differs).

It needs a build of the app in the legacy web root (`npx ng build --configuration development`, or the `frontend`
Docker service's). The decisions, the dialogs and the gestures are compared by the game check above, which plays real
games: from G3 on with `--pages legacy,angular`.

Result on 2026-11-14: 291 of 291 the same.

## Coverage

`coverage.mjs` lists what the scenarios must cover together: every kind of game event and decision the server sends
(read from `GameEvent.java` and `AwaitingDecisionType.java`), every zone a card can be shown in (`Zone.java`), and every
gesture and control of the game page (`GAME_CONTROLS`: the decision buttons, card gestures, tabs, each setting, the
piles, Escape, the spectator and replay modes and the replay's buttons). A run of every scenario records what page A
was sent and what the check used, and fails on anything left (`GAP` lines and the report). `NOT_EXPECTED` lists only
what can't happen, each with its reason (cards in the void are never sent to a page; event `CM` is never created).

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
`skirmish`, `transfer`, `shards` and `rescue`.

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

The page doesn't poll while a decision waits for the player. Conceding then used to leave the page unaware that the
game had ended; now the page asks once more after its concede is answered, and closes the open decision when the game
ends (`concede`; legacy `gameUi.js`, owner's decision of 2026-11-13). The server answers a cancel request with a
warning, "You can't cancel this game", even when the opponent may still agree.

Result on 2026-11-12, both pages `game.html`: 54 of 54 checks the same (18 scenarios at 3 window sizes, about 9
minutes), 10 items not covered (so the run fails until they are); the 17 recordings replay in jsdom; the self-test
catches 6 of 6 deliberate bugs.

Masks are drawn over everything, so a clock under an open dialog also hides that part of the dialog.
