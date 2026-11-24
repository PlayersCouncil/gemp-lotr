# Game screen check

The game screen's counterpart of the deck builder's visual check (`../deckbuilder/README.md`), for the Angular port of `game.html`
(trymarz/gemp-lotr-private#27). It plays each scenario of `scenarios.mjs` as a **real game** on two pages, one after
the other, and compares the pages after every step:

- the screen (at most `--tolerance` pixels, default 60, differing by more than 2 colour levels), with the clocks and the
  chat's times masked in magenta;
- what the page sent the server: every decision answered (id and value), card information asked for, concede and
  cancel, chat messages. The long polls aren't compared: when they are sent depends on timing;
- the browser's own dialogs, with their text (the check accepts them); the Angular page asks "At least one minion has
  not been assigned" in a dialog of its own (`ANGULAR_CONTRACT.unassignedQuestion`), which the check answers the same
  way after Done on an assignment and records in the same words (`run.mjs` `answerUnassignedQuestion`). It asks only
  while more minions could have been assigned (the server's `maxAssignable`), game.html about any minion left: a
  step where only game.html asked is accepted (`GAME_ACCEPTED`, Ketura 2026-11-19);
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

## Taking a game back (undo)

`npm run check:game:undo` (`undo.mjs`, against a game check server kept up, as the two checks below) plays real games
with each player on a page of their own and checks taking a game back as the players see it:

- **own**: after a choice nobody else has seen or answered, the Undo button is in the alert box and says what it
  undoes; the list has the place as green, "Undo", with what was done ("Played Gimli"). The button takes it back at
  once; both pages show the game as it was and say so, the button is gone, and the game goes on;
- **done**: a starting fellowship nothing more fits into asks only for DONE; the last card is green meanwhile and
  is taken back, offered again and chosen again, and after DONE the opponent goes on;
- **skip**: with "Skip extra confirmations" on, the page presses that DONE itself;
- **approve**: once the opponent has answered there is no Undo button and every place in the list is a revert with
  its reasons; the "i" button explains the tiers. Asked, the asker waits (and may withdraw) and the opponent is
  shown the same place, tier and reasons. Nothing in the request is the default (Enter does nothing, the first Tab
  goes to Approve), and the opponent's own decision cannot be answered while it waits. Approved, both pages show
  the game as it was at that place;
- **decline**: the asker is told, nothing changes, the game goes on;
- **decline-all**: the asker's list shows the reverts as off, with the reason; the asked player's "Auto-decline
  revert requests" switch is on (the asker's is not), and turning it off lets requests through again;
- **withdraw**: the opponent's request goes away, and their decision can be answered again;
- **concede**: the opponent concedes while this player's decision waits, and the page shows the end without the
  decision being answered first.

`UNDO` in `undo.mjs` is where each kind of page keeps each thing: `legacy` (`game.html`, by its own markup) and
`angular` (the Angular game page, by its DOM contract: `data-undo`, `data-dialog=undo-list` and `undo-request`,
`data-undo-reply`, `data-decision-button`, ...). `--page angular` runs them on the Angular page (`npm run build` first),
in its classic theme or with `--theme modern`; `--page legacy,angular` mixes them, as players on the two pages meet:
the first seat on one kind and the second on the other, then the other way round, so each check runs twice and says
which page asked and which answered. `--only own,approve` runs some of them; a screenshot of each step is saved in
`report-undo/` (`--out`; mixed, the kind of page is in each file's name).

```
node game/undo.mjs                                       game.html
node game/undo.mjs --page angular                        the Angular page, classic
node game/undo.mjs --page angular --theme modern         the Angular page, modern
node game/undo.mjs --page legacy,angular --only approve,decline,own
```

Result on 2026-11-19: 8 of 8 on `game.html`; on the Angular page 7 of 8 in classic and in modern, and 15 of 16 mixed:
`own` fails where the Angular page asks, at pressing the Undo button, which lies under the starting fellowship's card
dialog (80% of the window there; the legacy page's card dialogs now fit their cards and leave the alert box free).
Pressed by script, the rest of `own` passes on the Angular page (neither page loads again).

The game check (`run.mjs`) leaves undo's own requests out, as it does the game's long poll: the notices' long poll is
always open and asked again whenever the server lets it go, and what can be taken back is asked whenever an answer
brings events. Neither is compared, waited for or recorded (`watchTraffic`, `undoTiming`).

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

## Tutorials (scripted games)

`npm run check:game:tutorial` (`tutorial.mjs`, against a game check server kept up, as the undo check) plays the
tutorials that ship with the server (`gemp-lotr-cards/.../scenarios/tutorials`) on the Angular game page. A tutorial
is started with `POST /scenarios/start` by `GamePlayer0` alone: the opponent is played by the server's script. The
check follows the bubbles as a player would (`follow`): the bubble's Next, the choice of the guided path (`help`), a
click on what is outlined as next (on the part of it that shows, if something covers its centre), a right click on
the card a message waits to have opened and Escape to close it, and, where a step takes any answer, the first card
or button the game offers (an assignment: each minion to a companion of its own, then Done). Each check plays its own
game:

- **t1**: tutorial 1 (`basics-01`) played through to the end panel "Complete". It fails if it gets stuck (saying the
  decision's text and the bubble's, with the server's step in `t1-<theme>-stuck-step.json`), if the page logs an
  error, or if it ends any other way. On the way: while a message with Next is up nothing is outlined as next; the
  opponent is shown under the scenario's name, never "~Scenario"; the bubble stays in the window and off what is to
  be clicked; what is outlined as next is what lies at the outline's centre. The end panel's "Report it" link names
  the game's replay.
- **t2**: tutorial 2 (`basics-02-items-damage`) the same way.
- **refuse**: while a message is read, Pass is refused: the buttons under it shake (`data-nudge`) and nothing is sent;
  later, while a step is answered, a card the game would take but the step doesn't (`data-scenario="notnow"`) shows
  its hint beside it, and no decision is sent (the page's requests are watched).
- **bubble**: Space and Escape read on; dragged by its head the bubble stays where it is put; folded and unfolded
  (Space leaves a folded message be); after reading on, and after answering a step whose bubble was folded and
  moved, the next message is whole and where the page puts it.
- **menu**: "Start again" asks in the page's own dialog; Cancel changes nothing, OK opens a new game at its first
  message; "Leave" asks, then the page is the Game Hall.
- **small**: 1000×600 shows no panel; 999×700, 1200×599 and 900×550 show "This needs a bigger screen", and "Carry on
  anyway" takes it away.
- **phone**: the modern theme on a phone (`device-profiles.mjs` `phone`) shows only the "cannot be played on mobile
  devices" panel (no message, menu or outline), whose button leads to the Game Hall.
- **replay**: the replay of a finished tutorial 1 game (this run's t1 game, or one it plays itself; its id from the
  end panel's link, else from the server's thanks in the chat) shows its messages one after another as it plays,
  with no buttons, and no scenario menu.

The checks below bring their own scenarios (`tutorial-scenarios.mjs`: the engine's test fixtures, given the `allow`s a
person needs where the runner gives an `answer`): the check server has only the shipped ones, which are not added to.
Each is written as the player's own scenario with the authoring calls every server has (`scenarios/author/open` with
`new=true` and an id starting `check-tutorial-`, `edit`, `save`, `close`), started as a player starts one, and its
file deleted afterwards (on this machine from `GEMP_SCENARIOS`, default `/etc/gemp-lotr/scenarios`, else with
`scenarios/mine/delete`, which keeps a copy in the server's archive). Saved rather than tested as a draft so that it
plays as a player's game: no authoring panel, and "Back to checkpoint" starts it again from the server's file.

- **two-sided**: a two-sided game played through, the person in the first seat (Free Peoples on their own turns, Shadow
  on the opponent's: a Shadow assignment made pairing by pairing, Freca taking control of site 1, a sanctuary); each
  skirmish message's outline (`ui:skirmish:selfStrength`) is round the side the person plays then.
- **second-seat**: the same from the second seat, beginning at site 3 (Shadow first, a fierce Uruk assigned twice).
- **opening**: the game's own opening: at the bid the arrow towards 1 is outlined, OK only once the box says 1, and OK
  with 0 is refused beside OK without sending anything; then "Go first", the starting fellowship's card window, the
  mulligan, the first turn; the opponent is never "~Scenario", not even before the players are known.
- **checkpoint**: "Back to checkpoint" is not in the menu before the checkpoint (the mulligan), is after it, asks in
  the page's own dialog (Cancel changes nothing), and OK opens a new game at the checkpoint's message.
- **left-out**: a step whose middle message's condition fails says the other two, counted "1 / 2" and "2 / 2", each
  once in the chat; the one left out never shows.
- **play** (only by name, with `--scenario <id>`): any scenario the player can start played through the same way, to
  whatever end panel it shows.

Every check runs in both themes (`--theme classic` or `--theme modern` for one), phone in the modern theme only.

```
npm run check:game -- --only start --sizes 1366x768 --keep     a game check server, kept up
npm run check:game:tutorial                                    every check in both themes (about 6 minutes)
npm run check:game:tutorial -- --only t1,refuse --theme modern
npm run check:game:tutorial -- --only t2 --verbose             every move as it is made
npm run check:game:down
```

The report is `report-tutorial/` (`--out`): a screenshot of every step worth seeing (each message that points at
something, a card shown large, a choice, the page's dialogs, the end), a text file per check with what it found and
every move it made, and `summary.txt`. `--page angular` (the default) is the only page in `PAGES`: game.html's
overlay would need an entry of its own.

## The sandbox checks

`npm run check:game:sandbox` (`sandbox.mjs`, against a game check server kept up, as the tutorial check) plays the
default sandbox (`sandbox`, which ships with the server) on the Angular game page as `GamePlayer0`: the table changed
by hand (see "A table changed by hand" in the frontend's README). Each check starts a game of its own; every change is
asserted on the page and in the server's own view of the table (`GET /scenarios/sandbox/state`), and a refusal the
server is not meant to make fails the check.

- **open**: the sandbox opens in edit mode: the bar says Editing with Start (and no Stop, and no Start again in the
  menu), no decision text or buttons, an open spot in each of the six rows (each seat's characters and support, the
  minions, the hand); the context-menu key on a focused card (Frodo) opens its menu at Info, Down goes to Add (the
  Ring-bearer has no Send to), Right into its submenu, Left back, Escape closes it and gives the focus back to the card; Info shows the card's details.
- **add**: Legolas found by the search window with a name filter (bound to "Companions" for the characters) put among
  the characters; from Recent (Legolas first) into the hand; from In this scenario into the other seat's characters;
  after a reload the search still has its filter (bound to "Support cards" for the support) and "Clear filters" clears
  it; Recent is still there.
- **menu**: every entry of the card menu: Attach (Armor onto Legolas, picked on the table: the bar says what to pick,
  the box is round the card under the pointer), Add (two wounds by the stepper, a heal, three Elven tokens, two Sauron
  tokens and one taken off; his own culture listed first), Modify (Strength +2 this turn, Archer with no number until
  removed, both listed on him, one taken off by its ×), Copy, Send to (the copy to the discard pile), Stack (a Goblin
  Runner on Legolas), Delete (Legolas out of the game, the Armor he bore in the discard pile).
- **refuse**: the Ring-bearer's menu offers no Send to, Stack, Copy or Delete (the server's state calls him
  `ringBearer`); Legolas deleted behind the page's back and then deleted from his menu is refused, and the page says
  so by the card in the server's own words.
- **play**: Legolas added to the hand, Start: the bar says Playing, no open spots, the fellowship prompt offers
  Legolas, who is played; right-click is the card's details again; Stop (once the game can stop), an Armor added in
  edit mode, Start again.
- **undo**: after an edit, Start, a card played and Stop, the undo list reads "Stopped play", "Played Legolas,
  Greenleaf", "Started play", "Added Legolas, Greenleaf"; going back to before the edit leaves the page stopped, the
  card gone, the open spots back and no decision shown, and the controls work on the rebuilt game.
- **reset**: Reset asks in the page's own dialog (Cancel changes nothing), then the page is on a fresh game in edit
  mode without what was added.
- **ended**: Frodo given four wounds with the stepper, then Start: the end panel (`data-result=ended`) says "You lost"
  and "The Ring-Bearer is dead", with Reset and Leave; Reset goes to a fresh game.
- **piles**: a table set up through the server (both draw decks, hands, discard piles, the dead and removed piles):
  every pile of both seats opens from its count in the state panel (each count a link, `data-link`, and the number the
  server's `piles` says while edited), and its window shows the server's cards in the server's order (a pile's top
  first); a deck window left open shows a card put on the deck from elsewhere once Start lets the game move, and has
  no open spots in play mode.
- **pileadd**: the draw deck's window has its top spot first and its bottom spot last; Legolas by the search onto the
  top, Boromir by the search at the bottom, Legolas again from Recent onto the top (the deck reads Legolas, Legolas,
  Boromir on the server and in the window); Legolas from Recent into the other seat's hand from its window; Armor by
  the search into the discard pile from its window.
- **pilemenu**: a card in the deck window has Info, Send to, Stack, Copy and Delete (no wounds, tokens or Modify);
  Send to offers the deck's top and the hand; Legolas sent to the hand (gone from the window, on the hand's row),
  Boromir copied (the copy on the deck), the copy deleted, the window right after each.
- **path**: the path's + is for site 2; its chooser's Search… is bound to "Site 2" and lays a new Ettenmoors there;
  then From adventure deck… lays the other seat's site 3 and the person's own site 4 (the page's path shows the four
  sites in order); a laid site's Replace… ("Replace site 2") puts the person's own Ettenmoors there, and the card it
  replaced goes back to its adventure deck.
- **markers**: a site's menu has Info, each seat's marker (the one already there disabled), control for each seat and
  Replace…; control while the markers are at the site is refused in the server's words (and the page reads the game
  taken back without a "Concurrent access" error); site 2 laid and both markers moved there from its menu, site 1
  given to the other seat (it lies in its support area on the page), then liberated (back on the path).
- **counters**: the twilight pool set to 5 (Set) and stepped up and down, the burdens set to 2 (Enter) and one taken
  off, the threats set to 1 and one taken off, each where it is shown in the state panel, on the server and on the
  page; in play mode none of them is a link.
- **tools**: a sandbox of the check's own (`users/GamePlayer0/sandboxes/check-u2-tools-*.hjson` in the server's
  scenario folder, `GEMP_SCENARIOS`, written and deleted by the check; tools add, move, copy, delete) offers no hidden
  pile (the draw decks, the other seat's hand and adventure deck do not open), no path +, no counter to change and no
  marker, control or Replace… in a site's menu, while its own discard pile's window still has its open spot.

Every check runs in both themes at 1366×768 (`--theme classic` or `--theme modern` for one; `--only menu,play`;
`--verbose`). The report is `report-sandbox/` (`--out`): screenshots of the steps worth seeing and a text file per
check with every move.

## The puzzle checks

`npm run check:game:puzzle` (`puzzle.mjs`, against a game check server kept up, as the sandbox check) plays copies of
Decipher's puzzles 10, 08 and 03 (the engine's fixtures, `gemp-lotr-server/src/test/resources/scenarios/puzzles/`)
on the Angular game page as `GamePlayer3` (`GEMP_PUZZLE_PLAYER`): each copy is written into the player's own
scenarios folder (`GEMP_SCENARIOS`, default `/etc/gemp-lotr/scenarios`) under an id starting `check-puzzle-` and
deleted when its check is done. What a check changes is asserted on the page and in what the server says (the info
event's slots and picks, `POST /scenarios/picks`, `GET /scenarios`, the authoring state).

- **held**: puzzle 10 opened without picks: the page in edit mode, the bar says Choosing with Submit off (its title
  names both empty slots), both slots in the support row labelled as the file calls them, no sandbox open spots, no
  slots strip, no decision text or buttons, no card offered, no bubble, no Start again; no Undo button and no Revert;
  right-click on a card is its details (no card menu); the server's info says `mode: edit`, `level: puzzle`, 2 slots.
- **bound**: the condition slot's search window: its bound labelled "a Rohan condition", its title a line each for
  the pool (`set:1-6`), the bound (`culture:ROHAN cardtype:CONDITION`) and the place's kinds; no Card Type field
  (the bound fixes it), the Set and Block fields offered (the server searches inside the pool and the bound, each by
  itself, so they can only narrow it); Aldor (a Rohan ally) is not found; the same pick sent anyway (the page's own request
  carrying Aldor) is refused in the server's own words, shown in the window, and nothing is put in; with Aldor and
  Parapet in Recent, the condition slot's chooser offers Parapet only (the server's word on each).
- **fill**: Aldor from the search into the ally slot (Submit still off, its title names the condition); Armory, then
  Parapet over it, from the search; the ally slot emptied (Submit off again) and filled from Recent (only Aldor
  offered); a filled slot's card shows its details on right-click; Submit: a fresh game with Parapet and Aldor on the
  table, no slots, the info's picks are theirs, Change cards offered, no mode word, no Undo button.
- **solve**: puzzle 10 with the published key played through on the page (Parapet, "Make the minion archery total -3",
  two arrows on Aldor): Complete with the puzzle's own words, no score, not unlisted, buttons Play it again, Change
  cards, Back to the Game Hall; Play it again keeps both cards; Change cards (menu) asks in the page's own dialog, then
  the puzzle is held again with both slots empty.
- **fail**: Armory and Aldor, played on (pass, wounds to the first offered): failed (the game ends on Frodo's death
  before the judge, so the stock words), with Start again, Change cards, Back to the Game Hall; Start again keeps
  Armory.
- **unlisted**: puzzle 08 with its second solution left out of the copy: the support slot says "3 tokens", the hand
  slot is in the hand's row; Ithilien Trap and Defend It and Hope from the search, Submit, the left-out solution's
  answers clicked on the page (no Undo button after the first choices): Complete, and the panel says "You found a
  solution the author did not list."
- **score**: puzzle 03: eight slots in the hand's row and the site slot in the strip ("Site 2"); the site's search is
  held to the pool `set:1-5`, the bound `set:4 cardtype:SITE` and the place `cardtype:SITE sitenumber:2` (the seven
  site 2s of set 4, Derndingle and Fangorn Forest among them);
  Derndingle and eight Stout and Sturdy, Submit, then a short line (the troll assigned to Sméagol as the step asks,
  passes to the first skirmish's resolution) instead of the 362 answers of the published walkthrough: Sméagol's
  strength with its value, "Best" the same and "New best", titled "Higher is better"; Play it again: the same score,
  the best kept, no New best; `GET /scenarios` gives the best.
- **undo**: a copy of puzzle 10 with `undo: on`: Revert is offered, and after the first answers its list has places.
- **author**: a new puzzle draft with a slot and a pool: the Board tab's Find… opens the card search over the page,
  Card Type Allies and Gondor chosen, "Use as bound" sends that filter as the slot's bound (on the server's draft);
  the Scenario tab's Find… sends one as the pool.

Every check runs in both themes at 1366×768 (`--theme`, `--only`, `--verbose`, `--out`; report `report-puzzle/`).

## The table check

`npm run check:game:table` (`table.mjs`) compares the Angular game page's **table** with `game.html`, without a server:
both pages are served from disk (`../stub-server.mjs`: the legacy web root and the Angular build in it) and get a
recorded game (see "Recordings") answer by answer, as its server sent it, up to one step; the decisions are left out,
so no dialog is open and no card is offered, and each page asks for the next answer at once. Then the pages are
compared: the screen (at most `--tolerance` pixels, default 60) and the game as shown (`controls.mjs`
`readGameState`). Every step of every recording, at 1280x800, 1366x768 and 1920x1080. What the owner accepted
(`GAME_ACCEPTED` in `../accepted.mjs`, worded as the game check words a problem) doesn't count: e.g. the steps where a
dialog of cards is open, which the Angular page sizes to its cards (Ketura, 2026-11-19) and `game.html` doesn't.

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
| `minions-unassigned`             | Done with a minion unassigned: the page asks to confirm (game.html: the browser's confirm)                  |
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

## The authoring checks

`npm run check:game:author` (`author.mjs`, against a game check server kept up, as the tutorial check) writes
scenarios in the authoring panel docked beside the Angular game page, in both themes. Every check opens a new draft
of the player's own (`scenarios/author/open` with `new=true`), sets its script up over HTTP where that is not what is
being checked, works the panel as an author would, and closes the draft; none saves over a scenario the server ships.
A check fails on what it looks for, and on any page error, console error, refused authoring call it did not expect,
or dialog of the browser's own.

The checks are in `author/*.mjs`, one file per part of the panel, each exporting `checks` (`author/lib.mjs` has the
panel's selectors, the author's page and the waiting helpers); `author.mjs` puts them together.

- **dock**: the panel at the window's right, the table and the message bubble keeping to the rest; folded, the table
  takes the room back; every tab can be shown.
- **timeline**: the script's rows; ▶ on a row is a fresh game played to it, the panel back as it was.
- **leave**: leaving with changes in no file asks first; Stay stays, Discard closes the draft.
- **step-type**: a message's text typed in the Step tab is on the server soon after the typing stops, with the focus
  and the caret where they were and the bubble on the table saying it.
- **step-inflight**: typing goes on while sends are on their way (a slow server): nothing typed meanwhile is lost.
- **step-when**: the When composer's phase and turn go into the When, in their places, and to the server.
- **step-refuse**: a refused Allow sent with a message: the message goes in, the refusal is said, the Allow field keeps
  what was typed; put right, it goes.
- **step-order**: a step moved later, and one taken out after the page's own question.
- **step-block**: a step turned into a block (its Until guessed), and a step added on the block's page.
- **step-fresh**: a change to a step the game has been through brings a fresh game; the page comes back on the same
  step and tab, the focus in the field that had it.
- **step-asked**: "what is being asked" picked in the When composer and negated (`not(nextSkirmish)`): the When on
  the server and in the file's text as the server writes a new step's, and still so after a reload.
- **step-say-if**: a condition typed in a message's "Say only if": on the server and in the file; the step on the
  table put up without that message (its condition fails), the bubble not saying it, the card saying it was left out.
- **step-say-if-bad**: a mistake in "Say only if" typed with a change to the message's text: refused in the server's
  words and kept in its field, the text on the server all the same.
- **step-refuses**: a click on Pass the step does not take offers "+ refuses pass"; taken, it is on the server and in
  the file, its badge saying it is refused; two typed a line each come back as the server writes them.
- **step-template**: `{twilight}` put in from the menu beside a message's text, at the caret in the middle of typed
  text: the focus and caret kept, sent once, the bubble showing the number.
- **board-add**: a card found by name in the Board tab and added to the setup's hand: a fresh game, the card on its
  board and in the draft's `cards` and `setup`.
- **board-now**: the twilight set on the board as it stands: the table's twilight pool shows it, in the same game.
- **board-rename**: a card renamed (a bad name refused first): a fresh game, and the change gathered about it says its
  new name.
- **board-path**: a site of the path changed by name: both seats' paths, in a fresh game.
- **board-keep**: changes gathered are kept when the page loads again; Reset lets go of them.
- **board-refused**: a submit refused part-way: to the setup, the card goes in, the board's changes stay gathered and
  the problem is said on the fresh game's page; to the board as it stands, the rest is let go of and the panel says so.
- **scenario-title**: a new title goes into the game in hand (no fresh game); the panel's head and the scenario's menu
  follow; a field half typed in is left alone.
- **scenario-save**: a new draft saved from the Scenario tab (its file written, the saved notice, the star and the red
  head gone, the report), then saved as a copy through the filing form, the name tidied as the form says (an empty
  one marked, nothing sent); both files are deleted afterwards.
- **scenario-rules**: a skipped phase and a hand size added and taken back, each a fresh game back on the Scenario
  tab; with nothing left, `rules` goes.
- **scenario-outcome**: losing also when a companion is killed, and what the player is told.
- **scenario-settings**: the player's settings the draft fixes (an animation speed, the deck reveal, auto-accept), seen
  on the table's Settings tab; the player's own speed again takes the part out.
- **scenario-parts**: a part of the rest of the file applied as HJSON; a mistake in it refused in the server's words,
  the text kept, no fresh game.
- **scenario-check**: Check reports; See the file and Put that away; Submit for review asks first and Cancel submits
  nothing (submitting is never checked: it locks the file for an admin's review); Close the draft asks first.
- **opponent**: the opponent's decision held for the author ("Ask me for the opponent"): the Opponent tab asks for
  attention, a card played from it is written into the script as a step, and the game goes on.
- **opponent-pass**: with "write it into the script" unticked, Pass from the tab is the opponent's this once: the game
  goes on and the script is as it was.
- **opponent-assign**: the opponent's assignment, made in the tab (a minion to a character), is written as `assign(…)`.
- **picking**: Pick on the table from a message's anchor fields: the crosshair, the notice, the box and label round
  what the pointer is on; a click takes it (and a part of the card); Escape and a click in the panel take nothing;
  `+ Pick` adds a card under another, a site, a row, a pile, the twilight pool and a button to the outlines, each then
  drawn round where it was picked.
- **start-late**: where the story starts set in the Board tab (site 3, the maneuver phase, twilight 4): a fresh game
  with three sites on the table's path, both markers, the pool and the phase; `startAt` in the file's text; the same
  once the page loads again.
- **start-skirmishes**: skirmishes assigned at a Skirmish start: the game has them and asks which to fight; the file
  says them; they are shown once the page loads again.
- **start-markers**: markers put and a site controlled from the path on the table, to the board as it stands (the same
  game, the site in the Shadow seat's support area) and to the setup (a fresh game, `setup.controlled` in the file,
  ticked once the page loads again).
- **start-refused**: a start the server refuses: its words shown in the start, the moves typed kept and gathered.
- **start-drop**: a card in a skirmish already assigned taken out of the scenario: the draft is read, the skirmish
  without it.
- **start-shadows**: the format set to `scenario_shadows` in the Scenario tab, and a path of sites without numbers
  built in the Board tab (a site found, "no number" first; another put first): the game lays it first, its name kept.
- **opponent-orders** (`author/opponent-orders.mjs`): a standing order for the opponent's waiting decision started from
  the tab (its When from the decision), added (it then plays the runner), made `once` with a note (used up), moved up
  and deleted after the page's question, each a fresh game back on the Opponent tab with the order still open; after
  each the draft and the file's text, the other two orders exactly as they were; a reload lists the same.
- **opponent-order-refused**: an order's answer naming no card: the parser's words on the order, the text kept, no
  fresh game, the draft as it was.
- **opponent-leftovers**: the player assigns nobody; with leftovers to Sam or the Ring-bearer the runner is fought
  (the next skirmish is chosen), with none there is no fight; the draft and the file's text after each.
- **opponent-sandbox**: the sandbox fallback chosen in the Scenario tab with two of three runners ticked: the first
  ticked is played, the unticked one stays in hand, the orders untouched; the Opponent tab shows the same fallback.
- **scenario-game-lost** (`author/endings.mjs`): a heading and a text written for "Game lost" in the Scenario tab
  (the heading alone sends nothing); the draft and the file have them, and the end panel says them when Frodo dies.
- **scenario-outcome-kept**: an outcome with every part set keeps every other part when one closing message changes.
- **step-role** (`author/sides.mjs`): in a two-sided draft the When composer's role, "as Shadow" picked for a step on
  the opponent's turn: `as(shadow)` after the turn on the server and in the file; a When written out with `as` shows it;
  the Timeline heads each turn "(yours)" or "(theirs)" and marks the row; the same after a reload.
- **scenario-auto-pass-roles**: the phases passed through "for each role": the object form on the server and in the
  file after each tick, shown after a reload, and one list again after the page's question.
- **sides-play**: a two-sided draft written by playing it into the opponent's turn: the where-line, the Opponent tab
  and the Timeline say whose turn it is and the person's role; the step written there says `as(shadow)`; a fresh game
  played to it is on the opponent's turn.
- **board-opening**: how the game opens, set to the game's own opening in the Board tab after the page's question
  saying what goes: the setup keeps only its decks, the tab says there are no cards yet, a reload shows it; and back.
- **step-opening-asked**: the opening's decisions offered in "what is being asked" for a draft that plays its own
  opening, `bid` picked into the When, a Timeline group of their own before turn 1, no "+ Opening".
- **opening-play**: a draft's own opening written by playing it (the bid, going first, the mulligans, the opponent's
  from the Opponent tab) to the first turn; a fresh game played by the script to a step after it; to the setup, only
  a deck's order offered.
- **opponent-order-when**: an opponent's order's When changed with the When composer (nothing sent before Apply), then
  applied; a new order for an assignment answered "the rest to the Ring-bearer"; the same after a reload.
- **closing-say-if**: "Say only if" on a step's closing message and on a Scenario tab ending: on the server, in the
  file, and shown as the file has it after a reload.
- **step-refuses-at**: on a step that repeats, at its second decision, a click it does not take offered as
  "+ refuses 2: pass", written with its number.
- **opponent-let-go**: "it plays by its orders" chosen while the opponent's decision is held for the author: the
  opponent answers by itself at once, in the same game.
- **scenario-kind-undo** (`author/scenario.mjs`): the kind made a sandbox, a tool unticked (the file's
  `options.tools` lists the rest) and ticked again (every tool: the field left out), take-backs off (`options.undo`);
  on the server, in the file, and in the tab after a reload.
- **scenario-judge-score** (`author/endings.mjs`): judged at the end of archery of turn 2 with requirements typed as
  HJSON, scored by Frodo's strength (nothing sent until the card is chosen), lower being better, with a label, then
  by a custom value as HJSON (the outcome sent as text); on the server, in the file, after a reload.
- **opponent-eager-bot** (`author/opponent.mjs`): the eager and bot fallbacks offered, each option's tooltip saying
  what it does; chosen in the Opponent tab, on the server, in the file, after a reload.
- **board-modifiers** (`author/board.mjs`): mod… on Frodo (Strength +2 for the turn) to the board as it stands: in
  force at once and carried by a step; Archer to the setup: `setup.modifiers` in the file, its chip after a reload,
  and its × taking it out again.
- **puzzle-rules** (`author/puzzles.mjs`, Decipher's puzzle 10 as the draft): "no revealing" ticked (`rules.forbid`)
  and a pool typed, with its count beside it; a pool the server refuses: its words beside the field, what was typed
  kept, the draft as it was; after a reload.
- **puzzle-slots**: a slot's label, bound (its count) and tokens changed in the Board tab; a slot added with the
  solution's card for it found by name, the file's text with all of it, the same after a reload; the slot taken out
  of the slots and the solution after the page's question.
- **puzzle-solutions**: a solution recorded with the game's cards (the panel says it is recording; Stop), the
  published one played, Check's verdict on each in the list, one renamed, its answers written, its cards used (marked
  after a reload) and deleted after the page's question.

```
npm run check:game -- --only start --sizes 1366x768 --keep     the server, kept up
npm run check:game:author                                      every check in both themes
npm run check:game:author -- --only timeline --theme modern --verbose
npm run check:game:down
```

The report is `report-author/` (`--out`): the screenshots a check takes, and its moves and problems per check.
