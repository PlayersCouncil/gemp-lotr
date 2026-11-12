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
```

`local.mjs` starts a throwaway server of its own (`../local-server/compose.yml` under the project name
`gemp-game-check`, port 17201 or `GEMP_GAME_CHECK_PORT`, an in-memory database), creates the players of
`players.mjs` with a random password that is never written down, saves the test deck for each, runs `run.mjs` and
removes everything again. Neither the development server nor the deck builder's check server is touched.

Options of `run.mjs` (passed through `local.mjs`): `--only`, `--sizes`, `--workers` (at most 4, one pair of players
each), `--tolerance`, `--out` (default `report-game`), `--pages` (default `legacy,legacy`), `--legacy` and `--angular`
(the pages' paths), `--inject <name>` (a bug of `injections.mjs` in page B), `--real-images` (card pictures from the
image server instead of placeholders), `--verbose` (every decision and answer of both players).

The report is `report-game/index.html`: each scenario with its steps, both screenshots of each and the differences in
red.

## How both pages get the same game

- **The seed.** The check's server runs with `-Dgemp.game.randomSeed`: every game draws from its own `Random` with that
  seed (`GameRandom`: shuffles, random cards, the seating), a table's players are in name order, and a decision lists
  cards it holds in a set in card id order. Without the property the server is unchanged (`ThreadLocalRandom`, hash
  order).
- **The opponent** (`protocol.mjs` `Foe`) plays over HTTP by a fixed policy: bid 0, the first choice, the fewest cards,
  pass; a scenario's `foe` rules come first (e.g. let the player go first, play minions). When both players are asked
  at once (bids, starting fellowships, mulligans) the opponent waits for the player in the browser to answer, because
  the server's order depends on who answers first.
- **The player** in the browser makes each step with real gestures: clicks on cards, the spinner's arrows, the
  dialog's buttons, the action menu, right clicks, keys. A step ends when the next decision for the player arrived,
  the opponent has nothing to answer, no request but the long poll is open, nothing animates and the page hasn't
  changed for 500 ms.
- **The same pair** of players plays a scenario's game on both pages, so the names on the screens are the same. The
  game's own chat room is named after the game ("Welcome to room: Game12"), so the check shows both pages the same
  name, `Game#`. The legacy chat marks a message naming the player only once it knows the player (`GET /player`): the
  check lets that answer arrive before the game's first one on both pages.
- **Placeholders** stand in for the card pictures (as with `--stubbed` in the deck builder's check), so a picture the
  image server sent late can't make a difference. The animation speed setting is the fastest on both pages.
- Not compared: the page's title (the legacy page flashes it on a timer while a decision waits), the clocks and the
  chat's times (masked).

## Scenarios

`scenarios.mjs` explains each kind of step. The test deck (`players.mjs`) is Fellowship Block, Dwarves and Elves with
Moria orcs, the same for both players.

| Scenario               | What it does                                                                                                |
| ---------------------- | ----------------------------------------------------------------------------------------------------------- |
| `start`                | The page as it opens: the bid                                                                               |
| `opening`              | Bid (the spinner), starting fellowship (a card in the dialog, then Done), no mulligan                       |
| `fellowship-play`      | A companion played, a possession played and put on its bearer, a card's action, moving                      |
| `minions-and-skirmish` | The opponent plays a minion; it is assigned to Frodo, the skirmish chosen, its action passed, Frodo's wound |
| `minions-unassigned`   | Done with a minion unassigned: the browser's confirmation                                                   |
| `shadow-turn`          | The player's shadow phase: a minion played, the phase passed, reconcile                                     |
| `card-info`            | Card information with the server's modifiers (right click), closed by a click, another card                 |
| `hover-zoom`           | The hover zoom on a card in play and on a site                                                              |
| `tabs`                 | Settings, options, players and chat at the bottom left                                                      |
| `piles`                | Both players' other counters, the adventure deck and a dead pile                                            |
| `concede`              | Conceding from the options tab ends the game                                                                |

Result on 2026-11-12, both pages `game.html`: 33 of 33 checks the same (11 scenarios at 3 window sizes, about 4
minutes), twice in a row; the self-test catches 6 of 6 deliberate bugs.

Masks are drawn over everything, so a clock under an open dialog also hides that part of the dialog.
