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
| `TestConstants`, `Assertions`, `AdHocEffects` | same test folder | Canned sites, Ring-bearers, Rings and formats; zone assertions; ad hoc modifiers and effects. Test sources only. |
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
scn.MoveMinionsToTable(runner);                //    (minions go down after StartGame or SkipToSite)
scn.SkipToPhase(Phase.MANEUVER);               // 5. get to the moment under test
scn.FreepsUseCardAction(elrond);               // 6. act, as a player would
assertEquals(1, scn.GetWoundsOn(elrond));      // 7. assert
assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
```

The constructor builds two identical decks from the alias map, so both players own a copy of every card:
`GetFreepsCard("x")` and `GetShadowCard("x")` are different physical cards. It also runs the bid (player 1 bids 1
and goes first) and puts the opening hands back on the decks. `StartGame()` skips starting fellowships and
mulligans and removes the bid burden, so a test starts with 0 burdens, an empty twilight pool, and only the cards
it placed itself. Defaults when a constructor argument is left out: King-block sites, Frodo 10_121 as
Ring-bearer, The Ruling Ring, the `multipath` format.

"Freeps" methods act for player 1 and "Shadow" methods for player 2. On player 2's turn the roles are the other
way round: player 2 is the one moving a fellowship, so the `Shadow…` methods are then the Free Peoples player's.

### Cheating and legal play

The rig does three things: it stands in for the client (legal play), it inspects the game, and it rewrites the
game (cheating). `MoveCompanionsToTable`, `AttachCardsTo`, `AddWoundsToChar`, `SetTwilight` and the rest of
`ZoneManipulation` put things where you say without paying costs or checking requirements. Use them freely to
arrange a board, and never for the thing a test is about: if a test is about an ability exerting a character, the
ability does the exerting and the test reads the wound.

Several names can be given to the same blueprint (`put("runner1", "1_178"); put("runner2", "1_178");`), which is
how a test gets two copies of a card. An ad hoc modifier applies to cards wherever they are, including the draw
deck and discard pile, which is the way to make a card something it is not for the sake of a filter.

Cheating bypasses rules that legal play enforces. Uniqueness is one: a second copy of a unique card cannot be
*played* while the first is in play, but can be cheated in. Item class is another: a character can be stuffed with
two hand weapons by `AttachCardsTo`, and will then not be offered as a bearer for a third by legal play.

## Running tests

All of this needs Java 21 and Maven, and the working directory does not matter.

```
python3 .claude/skills/card-test-campaign/scripts/cardtest.py setup        # once per checkout: builds everything
python3 .claude/skills/card-test-campaign/scripts/cardtest.py run 1_40     # one card: about five seconds
python3 .claude/skills/card-test-campaign/scripts/cardtest.py run file:set01-Elven --out elven
python3 .claude/skills/card-test-campaign/scripts/cardtest.py suite cards  # every card test: under half a minute
```

`cardtest.py probe /tmp/x/Probe.java` compiles and runs a throwaway JUnit class kept outside the repository, for
finding out what the game does in some situation without adding a test.

`run` compiles only the files asked for, into a folder named by `--out`, so several agents can run their own cards
at the same time. Card definitions are read from `gemp-lotr-cards/src/main/resources/` as each test starts, so an
edited `.hjson` takes effect on the next run with no rebuild. An edit to engine or rig source needs `setup` again
(or `mvn -q compile` in the module that changed).

`mvn test` in `gemp-lotr-server` still runs everything the slow way, and is what a build runs.

## Things that catch people out

Each of these has been checked against the engine as it stands on this branch.

**The engine asks once.** The choices in a decision are worked out when the decision is sent. Cheating a card
onto the table while a decision is pending does not change what that decision offers. To see a card become
playable, complete the pending decision with a real action and look at the next one.

**One candidate is chosen for you; none is skipped.** When a choice has exactly as many valid candidates as it
needs, the engine makes it without asking. When it has none, that part of the effect is skipped. So a test that
means to inspect a choice has to supply one more valid candidate than the choice needs, and at least one invalid
one beside them. The same thing hides self-targeting: Celeborn's "exert Celeborn to heal an [elven] ally" heals
Celeborn himself when no other Elven ally is wounded.

**Costs gate an action; effects are only attempted.** An action is offered when its requirements are met and its
costs can be paid, whether or not its effect can do anything: Sam's "exert Sam to remove a burden" is offered with
no burdens to remove. To test that an effect finds nothing to do, use the action, check the cost was paid, and
check the game is back at the phase's actions.

**Choosing which skirmish to resolve is always asked.** Even when only one skirmish is left, the Free Peoples
player is asked to choose it. `FreepsAssignAndResolve` answers that for the skirmish it sets up;
after other skirmishes, call `FreepsResolveSkirmish(companion)`.

**Exerting is not wounding.** A character with one vitality left cannot exert and so cannot pay an exertion cost,
but can still be wounded.

**Allies live in the support area.** `MoveAlliesToTable` puts allies there, where an ally played legally goes.
`MoveCompanionsToTable` takes companions only and fails the test if handed anything else, as `MoveAlliesToTable`
does if handed anything but an ally.

**Set the twilight pool before the Shadow phase starts.** Because the engine asks once, a pool set while the
Shadow player's decision is already pending does not make a minion playable. Set it during the fellowship phase,
then pass. The move then adds one for each companion plus the new site's shadow number, so a test that needs an
exact pool adds those in: `// 10 set + 1 companion + 2 for the site`.

**Two copies of one card.** `FreepsActionAvailable(card)`, `FreepsPlayAvailable(card)`,
`FreepsTransferAvailable(card)` and their Shadow twins answer for the physical card they are given: with two
copies in play, each is asked about separately, and a per-copy limit is tested by asking about each copy. The
overloads that take text (`FreepsActionAvailable("Use Orc Taskmaster")`) still match any action so described.
The card-choice checks are different: when a decision lists its cards by blueprint, as a search of a deck or
discard pile does, `FreepsHasCardChoiceAvailable(card)` matches by blueprint and either copy answers for the
other, so give the candidates in such a choice different blueprints.

**A check on a player who is not being asked anything answers false.** The availability and card-choice checks
return false, and the readers of a decision's parameters return null, when that player has no pending decision.
The methods that *answer* a decision still fail, as they should.

**Both players act in most phases.** Fellowship is the Free Peoples player's alone and Shadow the Shadow
player's. In maneuver, archery, assignment, skirmish and regroup the Free Peoples player is asked first, then the
Shadow player, until both pass in a row. A test of a Shadow action in those phases passes for the Free Peoples
player first. `SkipToPhase` leaves the Free Peoples player with the decision.

**No minions, no combat.** With no minion in play when the Shadow phase ends, the game goes straight to regroup.
A test that needs a maneuver phase needs a minion on the table.

**Minions do not survive a turn.** `SkipToSite(n)` plays whole turns for both players and ends in player 1's
fellowship phase at site n. Minions and anything attached to them are discarded on the way, so put Shadow cards
down after `SkipToSite`, not before.

**Site 9 ends the game.** A fellowship that survives the skirmishes at site 9 has won, so `SkipToSite(9)` returns
with the game over. To test something at site 9, `SkipToSite(8)` and then pass the fellowship phase: the
fellowship moves, and the test is in the Shadow phase at site 9.

**The game ends when it ends.** When the Ring-bearer dies or is corrupted the game stops at once and no later
phase arrives. Make the assertions that matter before the event that ends the game.

**Archers get in the way.** `SkipToPhase` passes for both players and takes the first choice on required
triggers, but it does not assign archery wounds. An archer on the table stops it, or kills the minion the test
was about. Leave archers out of a scenario unless they are the point, or put them down after skipping.

**`SkipToPhase` declines optional triggers it meets on the way.** A trigger that is offered while the phase is
still the old one (an "each time the fellowship moves" trigger, offered before the fellowship phase is left) is
answered by the skipper's next pass, which declines it. The test arrives in the new phase with nothing pending
and nothing done. To test such a trigger, pass the phase yourself with `FreepsPassCurrentPhaseAction()` and
answer the trigger; keep cards with optional triggers out of other tests' scenarios, or put them down after
skipping.

**The other player's Free Peoples cards are not active on your turn.** A copy of a possession taken from the
Shadow player's deck (`GetShadowCard`) and cheated onto your companion is inactive (CR 4.2 "active"), so no
filter sees it. To put a card the opponent owns on a companion, use a Shadow card that is borne by companions:
Spider Poison (8_28) is one, and does nothing unless its bearer is assigned to a skirmish.

**When a wound kills and something answers the same wound.** The owner of a required trigger that answers the
wound gets a "Required responses" decision listing "Character death" and the trigger.
`FreepsResolveRuleFirst()` takes the rule first; `FreepsResolveActionOrder(text)` picks by the text of the entry.
There is no Shadow twin of either yet (rig request G-0005).

**An ally in a skirmish.** An ally can be assigned at its home site (CR 4.2 "ally"). On `FellowshipSites`, Elrond
(1_40, home site 3) can skirmish once the fellowship has moved to site 3: `SkipToSite(2)` and then a move.

**Searching a deck shows the deck first.** An effect that looks through a hidden zone shows the cards before it
asks for a choice. `FreepsDismissRevealedCards()` answers the showing; then choose with
`FreepsChooseCardBPFromSelection(card)`, which picks by blueprint because cards in hidden zones are not identified
to the client.

**"Is an optional trigger pending" is a broad question.** `FreepsHasOptionalTriggerAvailable()` is true for any
optional decision, the Ruling Ring's included. To assert that a particular card's trigger is on offer, pass the
card: `FreepsHasOptionalTriggerAvailable(card)`. **But** that overload looks for the card's name in the prompt,
and a trigger whose definition has its own `text:` ("Would you like to spot...") does not carry the name: the
check then answers false whatever is on offer, so an `assertFalse` with it proves nothing (rig request G-0008).
If `assertTrue` with the card fails on a trigger you can see is pending, use the overload that takes text, with
a few words of the prompt, for the `assertFalse` cases too.

**Two required start-of-turn triggers stop `StartGame()` short.** With two cards cheated in before `StartGame()`
that each have a required trigger at the start of the turn (Elrond 1_40 and Galadriel 1_45), the game is left
asking the Free Peoples player which goes first, and the next action fails with a `NullPointerException` in
`GetCardActionId` that says nothing of why. Put the second such card down after `StartGame()`, or answer the
ordering with `FreepsResolveActionOrder(text)`.

**The by-card availability checks have the same blind spot, for now.** `FreepsActionAvailable(card)` looks for an
action of that card worded "Use <card name>". An ability whose definition gives it its own `text:` (Library of
Rivendell's "Stack your tale or artifact from play") is never seen, and `assertFalse` with it proves nothing (rig
request G-0009). For such a card ask by wording, `FreepsActionAvailable("Stack your tale")`, and choose with
`FreepsChooseAction(text)`.

**The Rule of 4.** No more than 4 cards are drawn or taken into hand in a player's fellowship phase. A test of a
fellowship-phase draw that repeats has to stay within four, or its fifth reading is the rule and not the card.

**The Ring is attached to the Ring-bearer.** `GetAttachedCards(ringBearer)` includes The One Ring, so "nothing
else attached" is a size of 1. `assertNotAttachedTo` is the safer reading.

**An optional trigger at the start of the turn is the first thing asked.** A card with an optional
start-of-turn or start-of-fellowship trigger, put into play before `StartGame()`, leaves its offer pending when
`StartGame()` returns. Answer it, or put the card down afterwards.

**`SkipToSite` plays whole turns, reconciling included.** A small draw deck is drawn into the players' hands on
the way. A test that needs cards in the draw deck afterwards puts them back with `MoveCardsToTopOfDeck`. And as
the fellowship decision is already pending when it returns, a card made playable by cheating is not offered
until some legal action has been taken.

**A card being shown is listed as not selectable.** A real reveal gives every player, the owner included, a
"Revealed card(s)" decision to dismiss, in which `FreepsHasCardChoiceAvailable(card)` answers false:
`FreepsHasCardChoiceAvailable(card, false)` is the reading for a card that is shown. A definition that only sets
`reveal: true` on moving a card writes a line in the game log and shows nobody anything.

**Making a card something it is not.** `ApplyAdHocModifier` with `IsAdditionalCultureModifier` or
`IsAdditionalRaceModifier` gives a card a second culture or race, and filters see it. That is how to tell a
filter written with a race from one written with a culture when every card of the race has the culture.

**The Ruling Ring interrupts skirmishes.** With the default Ring, a Ring-bearer about to take a wound in a
skirmish gets an optional response. `FreepsDeclineOptionalTrigger()` declines it. Keep the Ring-bearer out of
skirmishes that are not about him.

## Choosing helper cards

A helper card with text of its own can change the game under a test. Read the definition of every card that goes
into a scenario; a name is not evidence of what a card does.

| Need | Card | Id | Why |
|---|---|---|---|
| A minion that does nothing | Goblin Patrol Troop | 1_177 | Strength 13, vitality 3, cost 6, site 4, no text at all |
| A cheap minion | Goblin Runner | 1_178 | Strength 5, vitality 1; when played, may add (2): harmless if cheated onto the table |
| A sturdy companion | Aragorn, Ranger of the North | 1_89 | Strength 8, vitality 4; Ranger; a maneuver ability that is only ever used on purpose |
| A weak companion | Dwarf Guard | 1_7 | Strength 4, vitality 2; needs a Dwarf spotted to be played, so cheat it in |
| An archer, when archery is the point | Legolas, Greenleaf | 1_50 | Strength 6, vitality 3; Archer, with an archery ability |
| Damage +1, when damage is the point | Gimli, Son of Glóin | 1_13 | Strength 6, vitality 3; Damage +1, with a skirmish ability |
| A minion with damage +1 and nothing else | Uruk-hai Raiding Party | 1_158 | Strength 9, vitality 3, cost 4, site 5; Damage +1 is its whole text |
| A Shadow archer and nothing else | Goblin Marksman | 1_176 | Strength 7, vitality 1, cost 3, site 4; Archer is its whole text |
| An item that changes no number | Dwarven Armor | 1_8 | Possession, cost 0, bearer must be a Dwarf; bearer may not be overwhelmed unless strength is tripled |
| A weak minion that survives a hit | Uruk Savage | 1_151 | Strength 5, vitality 3; Damage +1 is its whole text. An unboosted strength-6 companion beats it without killing it |
| Wounds in the regroup phase | Morgul Slayer | 3_93 | Strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)." Two copies wound one companion twice in one regroup phase, which is how to read "until the regroup phase" at its boundary |
| A Shadow card a companion bears | Spider Poison | 8_28 | Possession; its bearer must exert each time it is assigned to a skirmish, and nothing else |

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

`SkipToSite(3)` followed by `SkipToPhase(Phase.MANEUVER)` puts a test in the maneuver phase at site 4. Read a
site's definition before relying on it, as for any helper card.

Prefer cards from the same set and block as the card under test, and prefer a card with no text over one whose
text "shouldn't matter".

## Race to Mount Doom

Meta-site modifiers are passed to the constructor by blueprint id and created by the engine during set-up; they
are never cheated in. The modifier card is then `scn.GetFreepsCard("mod")` or `scn.GetShadowCard("mod")`.

```java
new VirtualTableScenario(cards, sites, frodo, ring, "91_3", null);      // player 1 has the modifier
new VirtualTableScenario(cards, sites, frodo, ring, null, "91_3");      // player 2 has it
new VirtualTableScenario(cards, sites, frodo, ring, "91_18", null,      // a modifier that changes the bid
    scn -> { scn.FreepsDecided("6"); scn.ShadowDecided("0"); scn.FreepsDecided("0"); });
```

A modifier that reads the same for both sides gets a `GetFreepsScenario()` and a `GetShadowScenario()` and is
tested from each.

## When the rig cannot do what a test needs

Then the rig is what changes, not the test. A card's test class has `Get...Scenario()` methods and tests and
nothing else: no helper methods, no reaching into `scn.game()` or `scn.gameState()`, no raw decision strings. If
a check cannot be written with the methods in `vts-api.md`, write down what was needed and stop work on that card
(the card-test-campaign skill says where the request goes). A helper buried in one card's file is a capability
the next card's author will not find and will write again differently.
