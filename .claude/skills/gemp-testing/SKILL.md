---
name: gemp-testing
description: >
  The Gemp-LotR test rig: what VirtualTableScenario is, where its code lives, how a test drives a game with it, how
  to run tests quickly, the behaviours of the rig and engine that catch people out, and the rules for adding to the
  rig. Use this skill whenever writing, running or debugging a test that uses VirtualTableScenario, when a test
  needs something the rig cannot yet do, or when changing anything under virtualtable/ or test framework/. For
  what a card's tests must cover and how they are laid out, use the test-generation skill alongside this one.
user-invocable: false
---

# The Gemp-LotR test rig

A test plays a real game of LotR TCG through a headless client that sits in both seats. The rig is that client: it
answers the engine's decisions for both players, reads the game's state, and can rearrange the board directly.

For every method, with its signature, read `${CLAUDE_SKILL_DIR}/references/vts-api.md`. That file is generated
from the rig's source by `card-test-campaign/scripts/gen_vts_api.py`; never edit it by hand.

## Where the rig lives

The rig used to be one class in the test sources. Its machinery has since moved into the server proper, because the
scenario and tutorial system drives games the same way:

| Piece | Where | What it is |
|---|---|---|
| `VirtualTable` | `gemp-lotr-server/src/main/java/com/gempukku/lotro/virtualtable/` | The table: holds the game, answers decisions, looks cards up by alias, walks the game through its opening. Abstract. |
| `Actions`, `Choices`, `Decisions`, `GameProcedures`, `GameProperties`, `CardProperties`, `PileProperties`, `Skirmishes`, `ZoneManipulation` | same folder | Interfaces of default methods that `VirtualTable` implements: partial classes in all but name. `TableBase` holds the handful of accessors they are built on. |
| `VirtualTableScenario` | `gemp-lotr-server/src/test/java/com/gempukku/lotro/framework/` | The unit-test table: two made-up players, every named card put in both decks, the shared card library, Race to Mount Doom set-up. This is the `scn` in every test. |
| `TestConstants`, `Assertions`, `AdHocEffects` | same test folder | Canned sites, Ring-bearers, Rings, Maps and formats; zone assertions; ad hoc modifiers and effects. Test sources only. |
| `LoadedLibraries` | same test folder | No part of the rig: the base class of the server tests that play no game (leagues, tournaments, merchants, packs, formats). Three static fields, `_cardLibrary`, `_formatLibrary`, `_productLibrary`, loaded once as the server loads them. It took the place of `at/AbstractAtTest`. |
| `ScenarioTable` | `gemp-lotr-server/src/main/java/com/gempukku/lotro/scenario/` | The other subclass of `VirtualTable`: a table laid out from a scenario file, for tutorials and puzzles, with a person in one seat. |

The consequence to keep in mind: **anything added under `virtualtable/` ships in the server and becomes a public
method of `ScenarioTable`.** Read `${CLAUDE_SKILL_DIR}/references/changing-the-rig.md` before changing anything
there.

## How a test goes

```java
var scn = GetScenario();                       // 1. a table, from this file's Get...Scenario() method
var elrond = scn.GetFreepsCard("elrond");      // 2. the cards, by the names the scenario gave them
var runner = scn.GetShadowCard("runner");
scn.MoveAlliesToTable(elrond);                 // 3. arrange the board by cheating
scn.StartGame();                               // 4. fellowship phase of turn 1, at site 1
scn.MoveMinionsToTable(runner);                //    (minions go down after StartGame or SkipToSite, before the skip into the phase that reads them;
                                               //    the FreepsPass() out of fellowship already sends the Shadow player's first decision)
scn.SkipToPhase(Phase.MANEUVER);               // 5. get to the moment under test
scn.FreepsUseCardAction(elrond);               // 6. act, as a player would
assertEquals(1, scn.GetWoundsOn(elrond));      // 7. assert
assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
```

The constructor builds two identical decks from the alias map, so both players own a copy of every card:
`GetFreepsCard("x")` and `GetShadowCard("x")` are different physical cards. Each stays in its owner's draw deck
until moved, so a helper added for one test is there to be found in every test of a card that searches a deck.
The constructor also runs the bid (player 1 bids 1 and goes first) and rests there, before any hand is dealt.
`StartGame()` skips starting fellowships and mulligans, puts the opening hands the game then deals back on the
decks (`StartGame(false)` leaves them in hand: 94_28) and removes the bid burden, so a test starts with 0
burdens, an empty twilight pool, and only the cards it placed itself. Defaults when a constructor argument is
left out: King-block sites, Frodo 10_121 as Ring-bearer, The Ruling Ring (strength +1: a Frodo of 3 reads 4), the `multipath` format. Site 1 of the
King-block sites is Edoras Hall 7_330: each [rohan] possession played there costs 1 less.

"Freeps" methods act for player 1 and "Shadow" methods for player 2. On player 2's turn the roles are the other
way round: player 2 is the one moving a fellowship, so the `Shadow…` methods are then the Free Peoples player's.
`AwaitingFellowshipPhaseActions()` and `AwaitingShadowPhaseActions()` are fixed to their seats: on player 2's
turn use `ShadowDecisionAvailable("Play Fellowship action or Pass")` and
`FreepsDecisionAvailable("Play Shadow action or Pass")`.

### Cheating and legal play

The rig does three things: it stands in for the client (legal play), it inspects the game, and it rewrites the
game (cheating). `MoveCompanionsToTable`, `AttachCardsTo`, `AddWoundsToChar`, `SetTwilight` and the rest of
`ZoneManipulation` put things where you say without paying costs or checking requirements. Use them freely to
arrange a board, and never for the thing a test is about: if a test is about an ability exerting a character, the
ability does the exerting and the test reads the wound.

Several names can be given to the same blueprint (`put("runner1", "1_178"); put("runner2", "1_178");`), which is
how a test gets two copies of a card. An ad hoc modifier applies to cards wherever they are, including the draw
deck and discard pile. A card test gives no card a culture, a race, or a keyword that no printed card of its kind
has (Q-0213; pitfalls, "Making a card something it is not").

No card is invented for a test, and no definition text is stapled onto a card to stand in for one: a test shows
what a card that exists does (Ketura, 2026-10-11: `lookup.py Q-0241`). The one exception she names is a guard for
a thing no card should be able to do (discarding The One Ring, hindering a Map): a mechanics test lays it with the
staplers `AdHocEffects` already has (`ApplyAdHocAction`, the `ExecuteAdHocEffect` family), on a card with no usual
text (Frodo, or a plain minion, by side), so that the test shows its own artifice and holds the whole of it. No
rig method is added for stapling definition text. (What a card test may do with an ad hoc effect is in the
Standard, "Only the rig".)

Cheating bypasses rules that legal play enforces. Uniqueness is one: a second copy of a unique card cannot be
*played* while the first is in play, but can be cheated in. Item class is another: a character can be stuffed with
two hand weapons by `AttachCardsTo`, and will then not be offered as a bearer for a third by legal play. A card
test does neither: no more copies of a title in play than its dots allow (one of a unique title, two of a two-dot
title, counted by title whatever the subtitle, hindered copies included: Q-0239), and items cheated only onto
bearers their text allows, one of each class to a bearer (Q-0213: for "bears 3 cards" or "each weapon", items of
different classes).

A site is given to a player with `FreepsTakeControlOfSite()` or `ShadowTakeControlOfSite()`: the lowest-numbered
site that nobody controls and every marker has passed. `FreepsTakeControlOfSite(site)` and its twin give a named
site. Either player, at any time. They are cheats like the rest: no decision is answered, nothing that answers
"take control of a site" is set off, and a decision already pending does not change what it offers. They fail the
test when no site can be controlled, or when the card named is not a site on the adventure path, is already
controlled, or is at or ahead of a player's marker.

## Running tests

All of this needs Java 21 and Maven, and the working directory does not matter.

```
python3 .claude/skills/card-test-campaign/scripts/cardtest.py setup        # once per checkout: builds everything
python3 .claude/skills/card-test-campaign/scripts/cardtest.py run 1_40     # one card: about five seconds
python3 .claude/skills/card-test-campaign/scripts/cardtest.py run file:set01-Elven --out elven
python3 .claude/skills/card-test-campaign/scripts/cardtest.py suite cards  # every card test: under half a minute
python3 .claude/skills/card-test-campaign/scripts/cardtest.py suite        # cards, mechanics, definitions and the rig's own tests
python3 .claude/skills/card-test-campaign/scripts/cardtest.py suite all    # every test of the server module
```

Which is run when (Q-0233): `run <id>` while working a card; `suite` with no word after a change to
definitions, card tests, mechanics classes or the documents' examples, since it runs everything the campaign
works on (`cards/`, `mechanics/`, `definitions/`, `virtualtable/`) and leaves out the server's other tests
(leagues, tournaments, the hall, packs, formats, scenarios, bots), which no card or rule reaches; `suite all`
after any change to the engine, the rig or the framework (an engine item ends on it, and so does a chunk), since
the scenario tests and the league and format tests run the same classes. `suite <package>` runs one package
and every package under it (`suite com.gempukku.lotro.mechanics`, or `suite com.gempukku.lotro.mechanics.hinder`).

`mechanics/` is sorted by subject (engine pass 33): `keywords/`, `hinder/`, `choices/`, `actions/`, `skirmishes/`,
`costs/`, `wounds/`, `ring/`, `sites/`, `deck/`, `inplay/`, `game/`. A new class goes in the folder of its subject; a
new folder only when none fits. `cardtest.py run` takes a mechanics class by its name (`run HinderedByNoCardTests`).

`cardtest.py probe /tmp/x/Probe.java` compiles and runs a throwaway JUnit class kept outside the repository, for
finding out what the game does in some situation without adding a test. Its public class is named as its file is
(`public class Probe` in `Probe.java`), or it does not compile. To print what a player is being asked
there: `scn.FreepsGetAwaitingDecision()` (null when nothing is asked) and its `getText()`.

`run` compiles only the files asked for, into a folder named by `--out`, so several agents can run their own cards
at the same time. Card definitions are read from `gemp-lotr-cards/src/main/resources/` as each test starts, so an
edited `.hjson` takes effect on the next run with no rebuild. An edit to engine or rig source needs `setup` again
(or `mvn -q compile` in the module that changed).

`mvn test` in `gemp-lotr-server` still runs everything the slow way, and is what a build runs.

## Things that catch people out

The behaviours of the rig and the engine that have surprised authors are in
`${CLAUDE_SKILL_DIR}/references/pitfalls.md`, each checked against the engine as it stands on this branch. They
are grouped as below, each entry listed by its first words. Before writing a card's tests, read the groups those
tests will touch (`python3 .claude/skills/card-test-campaign/scripts/lookup.py "<words>" --in pitfalls` prints
the entries that contain the words).
When a test does something you cannot explain, read the whole file before deciding anything is wrong: most
surprises in this campaign were one of these.

- **How the engine asks:** The engine asks once · One candidate is chosen for you; none is skipped · A board changed under a pending decision: an action offered though its cost cannot be paid, or withheld though it now can · A number that can only be one number is not asked for · A choice with nothing on any branch is not asked · A forced discard from hand that a bar stops asks for no card (F-0514, engine pass 46) · "Wound each" and "wound every" wound one card at a time · A stack from play asks where first, then what · Choosing which skirmish to resolve is always asked · A skirmish against a lurker is not on offer while another waits · Who is offered for assignment · An assignment a card makes another player make is his only where he had a choice · The moment after the assignments is the choice of the next skirmish · One minion to a companion, and the minions left over · A minion the Free Peoples player must pay to assign is paid for ahead of his assigning · `IsCharAssigned` and `IsCharAssignedAgainst` read only the skirmishes still waiting · A check on a player who is not being asked anything answers false · A number with no upper bound · A question for a number may come filled in · Answers that are refused when they do not fit what is asked · `FreepsChoose(text)` takes no action: at a choice of actions it fails · A loop of both players' actions is put to them at its third round · A loop of one player's actions is put to him at its fourth use.
- **What is offered, and what an offer does:** Costs gate an action; effects are only attempted · A requirement beside a cost can hide the cost's own filters · An event's random discard from hand does not count the event itself · An offer that costs nothing and would do nothing is not made · A choice between two things to do offers what can be done in full · A "wound X twice" branch of a choice is not offered to a character with 1 vitality left · A play inside an `If` does not gate its action · The no-effect flag · An action's own exertions count against what it then needs · A prevention is asked only when its effect will happen · A card that cannot be transferred is not offered to a `Transfer` effect · A response event is not played by another card's effect · A response is offered once per use · Two at once, one use · A Response ability is a special ability used · A meta-site's optional trigger is offered to every player unless its definition names one.
- **Reading and answering an offer:** A cost that is a choice ("Exert bearer or discard 2 cards from hand") is a multiple choice whose own text names neither option · "Is an optional trigger pending" is a broad question · Toil is a choice of cards, asked only when there is something to choose · Muster is one optional trigger for all of a player's muster characters · A yes/no in the middle of a trigger is not an optional trigger · An ability with wording of its own, and a card with several · Using a card's action · Two copies of one card · A choice from a pile, and a showing, are read by blueprint · Several cards placed beneath a draw deck are asked for one at a time · Two offers of one response · When a wound or an overwhelm kills and something answers it · A card's standing check is a required action, ordered with the others of its moment and looked at again as it is carried out · A response window can come before the target · A condition played by another card's effect asks where it goes before its own cost · The entries of a required-response ordering are actions, not choices · A candidate a helper's own text refuses shows nothing of the card under test · "Is about to end" comes after the skirmish has resolved.
- **Getting to the moment: set-up, the skippers and turns:** Set the twilight pool before the Shadow phase starts · No end of turn is sent before turn 1 (F-0327) · Both players act in most phases · A skirmish whose only Free Peoples character is killed mid-skirmish is over at once · No minions, no combat · Minions do not survive a turn · Site 9 ends the game · Player 2 on site 9 with the game still going · The game ends when it ends · A starting fellowship · Archers get in the way · `SkipToPhase` declines optional triggers it meets on the way · Two required start-of-turn triggers stop `StartGame()` short · An optional trigger at the start of the turn is the first thing asked · `SkipToSite` plays whole turns, reconciling included · A reconcile on the way discards from the front of the hand · A reconcile restores first · A card's "reconcile your hand" asks what the regroup reconcile asks, in the middle of the phase · "Each time you reconcile" is answered after the hand is made up · A deck's order is set after `StartGame()` · Only the table-laying cheats take aliases · The Ruling Ring interrupts skirmishes · A companion beaten by double his strength is killed, not wounded · A second assignment or skirmish phase in one turn takes a second move · Who places archery wounds · Allies who take archery wounds instead of companions · An ally in a skirmish · One blueprint under two site keys: each key names a copy · `FreepsDeclineSanctuaryHealing()` declines the sanctuary heals still asked, and nothing else · Sanctuary healing is asked at any site laid as site 3 or 6, and only when a companion is wounded · Whose site · At the move limit, the regroup phase ends the turn · The move limit never reads below 1 · `until: end(regroup)` ends as a regroup move is finished, not with the turn · An effect with no `until` ends with the phase it began in, and each skirmish is a phase of its own · `SkipToOtherPlayersTurn()` straight after `SkipToMovementDecision()` throws · A card discarded for where the fellowship is goes when the move is finished · Inside an "each time the fellowship moves" trigger the marker is still on the site left · A draw deck runs short in a long file · A random or whole-hand effect takes whatever is there · The fierce round reads as the first does · A bonus "until the regroup phase" from a skirmish trigger is gone once the last skirmish resolves · Before `StartGame()` the burdens read 1 · A skirmish, or any later phase, on player 2's turn: `SkipToPhaseInverted`.
- **Cards, owners and zones:** Exerting is not wounding · Allies live in the support area · An ally is at his home site, not the fellowship's · A play read as not offered for a card that is not in hand shows nothing · `AddBurdens`, `RemoveBurdens` and `GetBurdens` are the current player's · Threats stop at the number of companions · Threat wounds are asked for one at a time, and the threats are gone before the first · Hunter and enduring change a strength a board may lean on · The Rule of 9 counts the dead pile, copy by copy · `MoveCardsToDeadPile` puts a card in its OWNER's dead pile · Damage bonuses add up, and "loses all damage bonuses" shuts later ones out · The wounds of a lost skirmish are placed in rounds, one on every loser; a loser killed early leaves his own later wounds over · A site is controlled lowest first and only behind every marker; it is liberated highest first · The other player's Free Peoples cards are not active on your turn · An event is in the void while it is being played · An event printed with several phases is an event of the phase it is played in (F-0292) · `GetVitality` is the vitality left · `GetResistance` is the resistance left · A borne card's text bonus adds to its printed bonus · The card info names a borne card's printed bonus for that card · Culture tokens are of the card's own culture · A stacked card is not in play · The Ring is attached to the Ring-bearer · A card that passes through a zone · A minion that is killed was not discarded, and no card's "is discarded" trigger answers the kill · The removed-from-game pile · What an ally bears is in the support area · A follower's Aid is an optional trigger at the start of the maneuver phase · Whose support area · "A site in a support area" is a controlled site, either player's · `assertInHand` reads the zone, not whose hand · The twilight cost of a card depends on where it is · A twilight cost never reads below 0, so a discount as large as the card's cost hides a larger one · "When you play X, you may remove N threats to make its twilight cost -N" asks for N as the card is played · Whether a minion is roaming is read as a keyword · A keyword a card gives itself while it can spot that keyword is kept once gained · Two standing texts that undo each other both lapse · "Unable to gain game text keywords" keeps what the card had when it was laid · Making a card something it is not · An ad hoc modifier aimed at one card goes on after that card is in play · An ad hoc effect is run in place of a player's answer · A value modified below zero counts as zero · "Add 1 to the number of X you can spot" raises its owner's count alone.
- **A card that leaves play as it is played:** A card that leaves play as it comes into play was not played · A card played "exhausted" is exhausted as it comes into play (F-0500, engine pass 37; F-0501, engine pass 38) · A play as a cost can come to nothing · The loss of initiative comes with the play · An event that needs initiative is not counted toward it (F-0464, built by engine pass 28) · A card that discards itself as it is played was not played either.
- **Hidden zones, reveals, shuffles and the game log:** Searching a deck shows the deck first · "Look at an opponent's hand" is a showing to the looker alone · A bar on looking at or revealing a hand is asked by each definition word for itself, and one word does not ask it · A look at the top cards of an EMPTY draw deck still shows, with nothing in it · Whether a deck was shuffled · "You may play each <kind> revealed" asks once for each such card, a decline included · A "play ... from your draw deck" action with no cost is offered whatever the deck holds, and a look that plays nothing is a use · The Rule of 4 · A player barred from taking cards into hand draws only when reconciling · A card being shown is listed as not selectable · A card made known by the game log alone · A hand played revealed is read from what each seat is sent · A shuffle into a draw deck always shuffles.
- **Hinder, The One Ring and Maps:** A wound a worn Ring turns into a burden is a wound paid (F-0507, engine pass 42) · The Ring-bearer is the rules' to protect, hindered or not · A guard for a thing no card should be able to do is stapled on, never an invented card · A hindered card that is shuffled into a draw deck is hindered no longer · A hindered card is still borne · A card on a hindered bearer is not borne by "a companion" · The rig reads a hindered card's printed numbers · The One Ring is never discarded · No cheat puts The One Ring on: the Ring's own text does, and which Ring decides how · Maps and meta-sites cannot be hindered · A fellowship with no unhindered companion is not asked to move again (F-0315).
- **Tables and formats:** A Shadows-block site path · "Replace the fellowship's current site with a site from your adventure deck" wants a Shadows-block path · The sites of `TowersSites` have texts that reach a test · The sites of `KingSites` have texts that reach a test · A battleground or a forest site needs no cheat · A Hobbit-set card (sets 30 to 33) stands on the usual table for now · Both players hold the same nine sites · A table in a format that uses Maps · Validating a deck.
- **No test cards, and the test booster pack:** No card is invented for a test · A test of whether a definition is LOADED · A burden that cannot be added · Who heals · A card that names something as it is played has stored nothing when it is cheated into play · `transferableTo(<filter>)` · A player's account name · A search of another player's draw deck · A test that passes alone and fails in its class (or the reverse) points at a definition that keeps state · The test booster pack · An event played "ignoring all costs" pays nothing in its own text either (F-0316).

## Choosing helper cards

A helper card with text of its own can change the game under a test. Read the definition of every card that goes
into a scenario; a name is not evidence of what a card does. Prefer cards from the same set and block as the card
under test, and prefer a card with no text over one whose text "shouldn't matter".

Some words cannot be told apart by any card there is: only companions have a signet, only events have a
timeword, every Aragorn and every Gandalf is a companion, and the Ring-bearer is Ring-bound by rule (CR 4.2
"starting fellowship"), so "an unbound companion" is never the Ring-bearer. A filter mistake that drops or adds
such a word makes no difference to play; say so in the ledger's Notes and move on.

The sites of `FellowshipSites`, for tests that turn on terrain or on where the fellowship is:

| Site | Card | Shadow number | Terrain and text |
|---|---|---|---|
| 1 | Bag End 1_319 | 0 | Fellowship: exert a Hobbit to play The Gaffer from the draw deck |
| 2 | Ettenmoors 1_331 | 2 | Plains. Skirmish: exert your companion or minion to make it stronger |
| 3 | Rivendell Valley 1_341 | 0 | Sanctuary. Fellowship: play an Elf to draw a card |
| 4 | Balin's Tomb 1_343 | 3 | Underground. Maneuver: discard your tale to heal |
| 5 | The Bridge of Khazad-dûm 1_349 | 6 | Underground. Shadow: play The Balrog |
| 6 | Galadriel's Glade 1_351 | 3 | Sanctuary. Fellowship: exert an Elf to look at an opponent's hand |
| 7 | Anduin Confluence 1_353 | 6 | River. **When the fellowship moves here, every ally is discarded** |
| 8 | Anduin Banks 1_356 | 6 | River. Adds to the minion archery total for each companion over 4 |
| 9 | Emyn Muil 1_360 | 9 | Maneuver: exert your minion to make it fierce |

`SkipToSite(n)` ends in player 1's fellowship phase at site n, with player 2's marker on site n too, and any skip on from there (`SkipToPhase`,
`SkipToAssignments`) or a `FreepsPass()` moves the fellowship to site n + 1: `SkipToSite(3)` and then
`SkipToPhase(Phase.MANEUVER)` is the maneuver phase at site 4. So a test of a site's own text (its move trigger,
its maneuver, archery, skirmish or regroup phase) wants `SkipToSite(n - 1)`, with the minions laid there, and then
the skip (Card_01_341_Tests; Nan Curunír 5_119 at site 8 from `SkipToSite(7)`, dec-b5_119). Read a
site's definition before relying on it, as for any helper card. No site of this path is underground below
site 4, where a [moria] minion stops roaming: for a roaming minion underground (beside The Balrog 2_52, which is
discarded anywhere else) take `KingSites`, whose site 3 is The Dimholt 8_117 (underground, a sanctuary):
`SkipToSite(2)` and a pass (Card_02_052_Tests).

The cards the campaign has found for a need (a minion that does nothing, a Hobbit who is not [shire], a printed
card that wounds in the regroup phase) are in `${CLAUDE_SKILL_DIR}/references/helpers.md`: for each, what the card
is and what of its own text gets in a board's way. They are grouped as below, each entry listed by the need it
meets. Before choosing a helper, look the need up
(`python3 .claude/skills/card-test-campaign/scripts/lookup.py "<words of the need, a name or an id>" --in helpers`
prints the entries that contain the words); the definition of the card it names is read all the same.

- **Minions: plain ones, and ones with one keyword:** A minion that does nothing · A cheap minion · A weak minion that survives a hit · A minion of vitality 4, for "exhaust" · A minion with damage +1 and nothing else · A minion with damage +1 who is no Uruk-hai (beside "each Uruk-hai ... loses all damage bonuses") · Two Wizard minions in one skirmish, with one Saruman (Q-0239) · A minion with damage +2, to tell an X of 1 from 2 · A [sauron] minion of cost 5, to read a discount of 3 above the floor of 0 · A Shadow archer and nothing else · An archer minion that lives through two archery wounds · A Man minion of the opponent's, and nothing else · A fierce minion with next to no other text (two of them, of other cultures and races, for "a minion" that loses or keeps fierce) · A hunter and nothing else, with a small number and a large one; a hunter who is no minion; a minion that is damage +1 only while it bears a possession · A minion whose own standing text gives it a keyword that is not fierce · A weak minion PRINTED fierce (a companion of strength 5 or 6 beats it or outlasts it in the fierce round) · A minion that GAINS fierce: by its own ability, and by standing text · A minion with ambush and nothing else; a second instance of ambush · A muster character with no other text; two muster companions · A toil card with no other text, and characters to exert for it · A twilight Nazgûl · The Balrog, for a card that plays "The Balrog" · A besieger; a besieger who is no Orc; a card with the word in its title only; a legal way onto a controlled site · A tentacle that stays put · A [sauron] Orc played by another card where his cost is read; an Orc who is not [sauron] · Two [wraith] Orcs of different titles with no text in the way; a [wraith] minion who is no Orc · A card that says minions are not roaming; a card that changes a site number.
- **Companions, allies and what they bear: plain ones, and ones for a keyword or a number:** A sturdy companion · A weak companion · Hand weapons for a Dwarf, one or two · An archer, when archery is the point · A Free Peoples archer in a skirmish, for a Shadow text's "if skirmishing an archer" · A Legolas who is no archer, for a text that names Legolas in a skirmish · A companion of another culture who is no archer, for "a [culture] companion wins (loses)" · A Free Peoples skirmish ability on a Ring-bound Man (Faramir 4_117), with no archer keyword · Elf companions who are no archers, for "spot N Elf companions" · Damage +1, when damage is the point · A damage bonus in print for "a Dwarf who is damage +X": a Dwarf ally with one and without; a companion with one who is no Dwarf; a bonus that moves as he exerts · A damage bonus given, and all of them taken away, by plain cards; a minion that lives through three wounds · A companion's damage bonus taken away, with no event played (the `-` of "while he is damage +X") · A companion who loses to a minion with damage +1 and takes one wound · A companion of 6 vitality by legal cards · A Hobbit companion at strength 0 by cards in play, with no event played · Defender +1 by legal play · An item that changes no number · A follower that changes no number · A follower with a plain bonus · A valiant companion and nothing else · A "fellowship companion" (set 19's Fellowship keyword), and a companion without it · A valiant ally (no ally is valiant in print; beside "each valiant companion"); a valiant Ring-bound companion · Companions by signet · A Ring-bound companion who is not the Ring-bearer · A Ring-bearer who is not Frodo · A Hobbit ally who may be assigned on turn 1 · An ally who bears a weapon, or three cards · A Bilbo of each card type, for a text that names Bilbo · A Hobbit bearing a weapon of each item type, and one bearing an item that is none · A [rohan] Man who is an ally (and a second) · An ally whose home is site 3 or 6 of a block other than the Fellowship's (beside "an ally whose home is site 6") · A [gondor] character who is not a companion; an ally who is a Man · An ally in a skirmish away from any home site (a Shadows-block path) · An ally in a skirmish at any site, with no Aragorn needed · A special ability of an ally in a phase that has minions · An artifact of a culture other than [shire] that a Hobbit can bear, with a cost above 0 · A Free Peoples shield, armor or hand weapon of cost 2, to read a discount of 1 (a cost never reads below 0) · Seven [elven] artifacts in play at once, for a count that stops at +6 ("(limit +6)", Aiglos 9_12) · The title half of "Elrond (or 2 [elven] allies)" alone (Master of Healing 3_22) · Cards for "play a <culture> artifact (condition, possession) from your draw deck", one for each place it goes · A [rohan] mount whose text reaches no test (to make a [rohan] Man "mounted", or a mount on Éomer) · A Free Peoples possession that nobody bears (support area), beside borne ones.
- **A culture without its race, a race without its culture, a card against its type:** A culture without its race, or a race without its culture: a printed card, never a modifier (Q-0213: B) · An Aragorn who is not [gondor] · A Saruman who is no minion (the follower, for a bare "Saruman" crossing type, Q-0306) · A Hobbit who is not [shire] · A [shire] character who is no Hobbit · Each Gollum and each Sméagol, by culture; a [gollum] minion of another title · A [gandalf] Wizard who is an ally; a weapon that is no possession · A Wizard in a dead pile or discard pile who cannot be played (the `C-` of "play a Wizard / Gandalf from your dead pile", Q-0062) · A [gandalf] companion who is not Gandalf; a second Ent · A [gandalf] Man (Ent Avenger 6_27 is the [gandalf] companion who is none) · A [gondor] ranger who is not unique; a [gondor] Man who is no ranger · A [gondor] artifact that someone bears · Two hand weapons on one bearer by their own text; a weapon that bars every other · A card a [rohan] Man may bear that is no possession (beside "a possession" played on him) · A Shadow card that is not what its culture mostly is · An [orc] minion who is no Orc, and other [orc] cards · A Nazgûl who is not [ringwraith] · An [uruk-hai] minion who is no Uruk-hai; an Uruk-hai of another culture than [uruk-hai] (a [sauron] one) · A Southron who is no Man; a mounted minion that is no Man · A Shadow card of twilight 5 in play that is no minion · A Shadow card a companion bears; a Shadow possession in play with no minion on the table · A Shadow minion bearing a weapon and an item that is none; a plain weapon for a [sauron] Orc · A [wraith] condition on a minion; on a companion or an ally; a character who cannot be healed · A Shadow condition played on a companion with nothing to spot or exert; one that goes on a Free Peoples Man only · A [wraith] item that is no possession; chosen Shadow cards put in the discard pile by a legal play · A pipeweed that is not [shire]; pipes of two cultures · Tales of each kind, for "each tale" · [shire] tales of each kind, for "a tale" or "a [shire] tale" · A fortification that is no condition (beside "a fortification": every other is a condition) · A [rohan] possession with no bearer (support area), and beacons to hinder for it · A corsair against its culture or type: there is none.
- **Wounds, exertions, heals, kills and discards that a printed card does:** Wounds in the regroup phase · A companion who can use a special ability both in a skirmish and in the regroup phase (check D of "cannot use special abilities until the regroup phase") · A Shadow CHARACTER whose effect exerts a character in the skirmish phase (the character doer of a bare "Shadow cards cannot exert X during a skirmish phase") · A Free Peoples wound on a minion in the maneuver phase · The only minion of a skirmish removed during it, where the companion is neither the Ring-bearer nor a signet-bearer · A minion killed, and a minion discarded and not killed, by the Free Peoples player in the maneuver phase (the two sides of "each time a minion is discarded", "is killed") · A companion discarded from play, not killed · A companion taken out of play in the fellowship phase · Something that answers a wound TAKEN (to show one was not) · A wound to a chosen companion who is in no skirmish, during another's skirmish · A wound on the Ring-bearer, phase by phase (a Ring's "about to take a wound") · A wound in an assignment phase · A Shadow special ability in the archery phase · A Shadow card that exerts a companion · Every character exerting at once, by one effect (an "is about to exert" response that picks the exertion it spares) · A Free Peoples card that exerts a companion of the player's choice: an event, an effect and not a cost; and a card with no side that does · One cost that exerts a character more than once (the R of "each time ... exerts") · A heal of a minion: by the Free Peoples player; by the Shadow player · A heal of a companion by each kind of source: a site, one event three times, the Shadow player, a delayed trigger · A heal of "a companion" with a before and an after · A heal that names several companions at once (one "is about to heal" window for all of them) · A way to discard a [moria] condition · Other ways to discard Shadow cards · A discard the Free Peoples player makes by no card of his own; a Free Peoples card that has the Shadow player discard · A doer that is no Shadow card, and a Shadow item, for "Shadow cards may not discard cards from your hand or from the top of your draw deck" · A discard the SHADOW player makes: by a keyword across card types, and by a site's text; a Shadow card that has the Free Peoples player discard his own cards; a Shadow card's discard that is not from play · A card that answers a possession discarded from play.
- **Burdens, threats, culture tokens, hinder and The One Ring:** A burden removed as a Shadow card's COST; as a Free Peoples card's EFFECT · A burden a Shadow card adds: one the Free Peoples player chooses; two at once; one a minion's own ability adds. A burden a site adds · A threat removed as a Shadow card's COST; as a Free Peoples card's EFFECT (a possession's, a condition's); as a Free Peoples card's COST; by a site, which has no side · A place where no culture token can be added, removed or reinforced · Culture tokens: a card that SPOTS one, a card that COUNTS them, and cards that carry them · A culture token placed and removed by legal play; a reinforcement of one token, and of several; a cost of 2 tokens · Any Ring put on in the maneuver phase by a printed card (a Ring's phase word read with the Ring ON) · A printed card that hinders the Ring-bearer (why a hindered Ring-bearer is a board in play) · A printed card that hinders a character who is assigned or skirmishing (where that hindering is the act) · A card that restores a Free Peoples card by legal play (where the restoring is the act).
- **Assigning, playing, drawing, revealing and stacking by a printed card:** A minion assigned by a card and not by a player's assigning · A card played only to take the hand from four to three (a loss of initiative) · A companion played, and an ally played beside it (for "each time a companion is played") · A companion played in the maneuver phase, after an event (the board for "make each ... until" read as a snapshot) · A minion's own assignment action, with no cost · A minion that comes into a skirmish already under way (the board for "each minion skirmishing ..." read as a snapshot) · A Shadow card that is no event, played in a skirmish (the invalid candidate beside "a Shadow event is played") · A minion played outside a Shadow phase (the phase next door to "played each Shadow phase", "in the Shadow phase") · A possession played on a companion outside the fellowship phase's own plays (by a card's ability, in a phase with minions) · A Shadow card that cancels a Free Peoples event (tells an event's cost from its effect: what is asked before the response is offered is cost, and what was paid stays paid) · A card moved out of ANOTHER player's discard pile (the other side of "your discard pile") · A card that answers a card DRAWN, and one that answers a card drawn or taken into hand; a draw at will; a card taken into hand that is not drawn, from the discard pile and from the draw deck · A Free Peoples card that has the SHADOW player draw, with no choice in it · A hand revealed by its own owner, on each side (the invalid doer and the invalid hand for a bar on looking at or revealing a hand) · A Free Peoples card that has the SHADOW player reveal his own hand; one card of the Shadow hand revealed at random · A Shadow card stacked on a condition where play can put it; one event that discards a condition or a minion · A [dwarven] condition that play stacks Free Peoples cards on · A card stacked by legal play: a Free Peoples card from hand; a minion from play · A reconcile in the middle of a phase · A companion's twilight cost raised (to show the size of a discount on a cheap companion) · A Shadow possession's twilight cost raised (to show the size of a discount on a weapon of cost 0 or 1) · A Shadow condition's twilight cost raised (to show the size of a discount on a cheap condition) · A card in a hand whose cost a site 1 lowers (the CR 4.2 "twilight cost" example read by a Fellowship event on turn 1).
- **Sites and moves:** Two sites whose standing texts read as one number · An underground sanctuary whose Shadow number a discount can lower ("each underground site's Shadow number is -2") · A character at a Fellowship site 2 who is not in the fellowship ("only Hobbits are in the fellowship", "each Hobbit moves to") · A card that reads the terrain of the fellowship's site (to show a site has gained or lost one) · A river at a low site number ("moves from a river", "at a river" with moves left after it) · A turn with THREE moves, so that the same minions are on the table at two moves ("each time the fellowship moves" with minions to act on; no minion is in play at the move that ends a fellowship phase) · A site taken under control, and one liberated, by legal play; a card that counts "each site you control"; a battleground to control · The next site played, and a site replaced, by legal play.

## Race to Mount Doom

Meta-site modifiers are passed to the constructor by blueprint id and created by the engine during set-up; they
are never cheated in. The modifier card is then `scn.GetFreepsCard("mod")` or `scn.GetShadowCard("mod")`.

```java
new VirtualTableScenario(cards, sites, frodo, ring, "91_3", null);      // player 1 has the modifier
new VirtualTableScenario(cards, sites, frodo, ring, null, "91_3");      // player 2 has it
new VirtualTableScenario(cards, sites, frodo, ring, "91_18", null,      // a modifier that changes the bid
    scn -> { scn.FreepsChoose("6"); scn.ShadowChoose("0"); scn.FreepsChoose("Go first"); });
new VirtualTableScenario(cards, sites, frodo, ring, format, "94_5", null, null,   // league standings, the last argument
    Map.of(VirtualTableScenario.P2, new RTMDGameInfo.LeaguePlacement(10, 100)));  // player 2 is tenth of a hundred
```

The last answer is the higher bidder's seat ("Go first" / "Go second"); assertions made inside the handler count.
A player left out of the standings has no place. The two players' account names are "Free Peoples Player" and
"Shadow Player" (the constants `P1`, `P2`) unless the constructor's last two arguments seat them under others
(G-0039: `..., bidHandler, placements, freepsPlayerName, shadowPlayerName`, null for the usual name). That was built
for the mechanic test of a condition on a player's name (`PlayerIs`): mechanics/actions/PlayerIsRequirementTests, which
builds the requirement from definition text in the test and asks it directly, no card laid (Q-0271: C, the one
exception to "no definition text made up for a test"); the rig's own test of it is virtualtable/NamedSeatTests. A card's own tests do not read the
account names in its definition (Q-0190: B, 94_5). At such a table the
players are `scn.P1()` and `scn.P2()`, never the constants, and placements are keyed by the names given.
A modifier of deck building (92_1, 92_19, 94_22: `DeckBuildingOverride`) drives no game: it is tested with
`DeckValidationScenario`, validating with the card's own overrides and never a context built by hand (pitfalls,
"Validating a deck"); its stats test takes the card from a table that has it.

A modifier that reads the same for both sides gets a `GetFreepsScenario()` and a `GetShadowScenario()` and is
tested from each. One that says "your" or "you" is shown working for its owner in each seat (as player 2, on his own
turn: shapes.md, F-0312) and, once, not for the other player. A modifier's "you may" is declined in one test with the other player read as not offered: a
meta-site's optional trigger goes to every player unless its definition names one (pitfalls, "A meta-site's
optional trigger"). A modifier's phase action goes to whoever may act in the phase: where its text says "your",
read the other player as not offered on a board where he could pay its cost (shapes.md, F-0312). The game log calls a modifier by its picture card's title ("The Shire" for 91_2), never
"Race Text 91_2": read the log for what was done, not for the modifier's name. Its action is worded the same way
("Use The Shire"), so read and take it by card (`ShadowActionAvailable(mod)`): `ShadowActionAvailable("Use Race
Text")` and `("mod")` are false whether it is offered or not, and an `assertFalse` of either shows nothing (92_10's old test).
The old files reach the Shadow phase with `scn.FreepsPass(); scn.SkipToPhase(Phase.SHADOW);` and move minions to
the table before `StartGame()`. Both work and are left as they are; `FreepsPass()` alone arrives there (92_20, 92_22).

## When the rig cannot do what a test needs

Then the rig is what changes, not the test. A card's test class has `Get...Scenario()` methods and tests and
nothing else: no helper methods, no reaching into `scn.game()` or `scn.gameState()`, no raw decision strings. If
a check cannot be written with the methods in `vts-api.md`, write down what was needed and stop work on that card
(the card-test-campaign skill says where the request goes). A helper buried in one card's file is a capability
the next card's author will not find and will write again differently. The same goes for showing a check by an
exception: a card test never asserts that the rig throws (`assertThrows`, a `try`); the check waits in the ledger
(`waits <check>: G-0000 ...`) and the card is parked until the rig has a reading for it.

An author reports the need and does not edit the rig; the orchestrator has it built. An addition needs no
approval from Ketura, only a real use for it in a test at hand, and is recorded afterwards in `rig-requests.md`
for her information (rulings log, 2026-10-06 evening).
