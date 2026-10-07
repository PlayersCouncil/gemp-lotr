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
`GetFreepsCard("x")` and `GetShadowCard("x")` are different physical cards. Each stays in its owner's draw deck
until moved, so a helper added for one test is there to be found in every test of a card that searches a deck.
The constructor also runs the bid (player 1 bids 1 and goes first) and puts the opening hands back on the decks.
`StartGame()` skips starting fellowships and mulligans and removes the bid burden, so a test starts with 0
burdens, an empty twilight pool, and only the cards it placed itself. Defaults when a constructor argument is
left out: King-block sites, Frodo 10_121 as Ring-bearer, The Ruling Ring, the `multipath` format. Site 1 of the
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
deck and discard pile, which is the way to make a card something it is not for the sake of a filter.

Cheating bypasses rules that legal play enforces. Uniqueness is one: a second copy of a unique card cannot be
*played* while the first is in play, but can be cheated in. Item class is another: a character can be stuffed with
two hand weapons by `AttachCardsTo`, and will then not be offered as a bearer for a third by legal play.

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
Celeborn himself when no other Elven ally is wounded. An "up to N" choice is the exception: its
minimum is 0, so it asks even with one candidate left (Song of Durin's "discard up to 2 [moria] conditions").
A choice between piles goes the same way: "from your draw deck or discard pile" is not asked when only one pile
has a candidate, and a `ShadowChoose("draw deck")` then fails inside the next decision. So does a transfer:
with one legal bearer it is made without asking.

**Costs gate an action; effects are only attempted.** An action is offered when its requirements are met and its
costs can be paid, whether or not its effect can do anything: Sam's "exert Sam to remove a burden" is offered with
no burdens to remove. To test that an effect finds nothing to do, use the action, check the cost was paid, and
check the game is back at the phase's actions. An event is the same: with nothing for its effect to act on it
is still offered, and playing it pays for it and discards it. An ability at its limit goes by the kind of
limit (Q-0093). A limit on uses ("limit once per turn") withholds it, cost or no cost. A limit on what its effect
gives ("(limit +3)", "add a threat (limit 1)") leaves an ability with a cost on offer (Q-0041): the cost is paid
and nothing else happens; with no cost it is not offered at that limit either.

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
The methods that *answer* a decision still fail, as they should, the plain pass methods apart (see "Answers
that are refused" below).

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

**Who places archery wounds.** The fellowship's archery wounds are placed by the Shadow player on his minions
(`ShadowChooseCard` after `SkipToArcheryWounds()`), the minions' by the Free Peoples player on companions.

**A starting fellowship.** `StartGame()` skips it. To test "(except in your starting fellowship)": the scenario
constructor returns with player 1's "Starting fellowship" choice pending; `FreepsChooseCard(card)` plays a
companion into it and player 2's choice is then pending. End the test there: going on deals opening hands.

**Archers get in the way.** `SkipToPhase` passes for both players and takes the first choice on required
triggers, but it does not assign archery wounds. An archer on the table stops it, or kills the minion the test
was about. Leave archers out of a scenario unless they are the point, or put them down after skipping.

**`SkipToPhase` declines optional triggers it meets on the way.** A trigger that is offered while the phase is
still the old one (an "each time the fellowship moves" trigger, offered before the fellowship phase is left) is
answered by the skipper's next pass, which declines it. The test arrives in the new phase with nothing pending
and nothing done. To test such a trigger, pass the phase yourself with `FreepsPass()` and
answer the trigger; keep cards with optional triggers out of other tests' scenarios, or put them down after
skipping. In the fellowship phase the skipper passes for the Free Peoples player alone, so a trigger offered
to the Shadow player there is never answered and the skipper stops ("halted in phase FELLOWSHIP ... after 20
attempts") without a word about the pending decision. From maneuver on it passes for both, and a trigger of
the Shadow player's (Ringwraith in Twilight 101_40, at the start of maneuver) uses up a pass: the test arrives
with the Free Peoples player's pass in the new phase already spent and the Shadow player holding the decision,
and `FreepsAssignToMinions` then fails with a `NullPointerException`. A trigger at the start of the target
phase itself is left pending, as a test of it needs.

**The other player's Free Peoples cards are not active on your turn.** A copy of a possession taken from the
Shadow player's deck (`GetShadowCard`) and cheated onto your companion is inactive (CR 4.2 "active"), so no
filter sees it. To put a card the opponent owns on a companion, use a Shadow card that is borne by companions:
Spider Poison (8_28) is one, and does nothing unless its bearer is assigned to a skirmish.

**When a wound kills and something answers the same wound.** The Free Peoples player, whoever owns the trigger,
gets a "Required responses" decision listing the rule and the trigger (a Shadow card's "wins a skirmish" trigger
beside "Resolve skirmish damage" is the same decision; it is not only for deaths). The rule's entry may have no action
text at all, and it is not always listed first (King of the Dead 103_27 killed: `["Required trigger from King of
the Dead…", null]`; Ferocious Haradrim 11_80 answering a death: `[null, "Required trigger from Ferocious
Haradrim"]`). `FreepsResolveRuleFirst()` resolves the rule, the entry that belongs to no card, wherever it is
listed; `FreepsResolveActionOrder(text)` resolves the entry whose wording contains the text and passes over an
entry with no wording.
There is no Shadow twin of either and there will not be: by rule the Free Peoples player is the one who orders
simultaneous actions, whoever's they are.

**An ally in a skirmish.** An ally can be assigned at its home site (CR 4.2 "ally"). On `FellowshipSites`, Elrond
(1_40, home site 3) can skirmish once the fellowship has moved to site 3: `SkipToSite(2)` and then a move.

**Searching a deck shows the deck first.** An effect that looks through a hidden zone shows the cards before it
asks for a choice. `FreepsDismissRevealedCards()` answers the showing; then choose with
`FreepsChooseCardBPFromSelection(card)`, which picks by blueprint because cards in hidden zones are not identified
to the client.

**"Is an optional trigger pending" is a broad question.** `FreepsHasOptionalTriggerAvailable()` is true for any
optional decision, the Ruling Ring's included. To assert that a particular card's trigger is on offer, pass the
card: `FreepsHasOptionalTriggerAvailable(card)` goes by the card each offered action belongs to, whatever its
prompt says. Where one card offers two optional actions in the same decision (Ethereal Banner 103_21's trigger
and its response, on an overwhelm), add words from the action's description:
`FreepsHasOptionalTriggerAvailable(card, text)` and `FreepsChooseOptionalTrigger(card, text)`, as
`FreepsUseCardAction(card, text)` does for phase actions.

**Two required start-of-turn triggers stop `StartGame()` short.** With two cards cheated in before `StartGame()`
that each have a required trigger at the start of the turn (Elrond 1_40 and Galadriel 1_45), the game is left
asking the Free Peoples player which goes first, and the next action fails with a `NullPointerException` in
`GetCardActionId` that says nothing of why. Put the second such card down after `StartGame()`, or answer the
ordering with `FreepsResolveActionOrder(text)`.

**An ability with wording of its own, and a card with several.** `FreepsActionAvailable(card)` sees an ability
whose definition gives it its own `text:` (Library of Rivendell's "Stack your tale or artifact from play") as
well as one the game words "Use <card name>". Where a card offers several actions, name the one meant:
`FreepsActionAvailable(card, "from hand")` and `FreepsUseCardAction(card, "from hand")`.

**Who is offered for assignment.** `FreepsHasAssignmentTargetAvailable(card)` and its Shadow twin read the
assignment decision in front of the player. `FreepsCanAssign(card)` and `ShadowCanAssign(card)` read the game's
rule, whatever is being asked.

**Whether a deck was shuffled.** `GetFreepsDeckShuffleCount()` and `GetShadowDeckShuffleCount()`, read before and
after. A draw deck that has been looked through is reshuffled whether or not the card says so (CR 4.2 "draw
deck"; Ketura's ruling on Q-0013), so a card that searches a deck has this to show. A card played from the
deck onto a bearer, or an event played from it, reads two more where the definition shuffles as well: the play
itself shuffles as the card leaves the deck. A card played from the deck to the table or support area reads one.

**The Rule of 4.** No more than 4 cards are drawn or taken into hand in a player's fellowship phase. A test of a
fellowship-phase draw that repeats has to stay within four, or its fifth reading is the rule and not the card.

**The Ring is attached to the Ring-bearer.** `GetAttachedCards(ringBearer)` includes The One Ring, so "nothing
else attached" is a size of 1. `assertNotAttachedTo` is the safer reading.

**An optional trigger at the start of the turn is the first thing asked.** A card with an optional
start-of-turn or start-of-fellowship trigger, put into play before `StartGame()`, leaves its offer pending when
`StartGame()` returns. Answer it, or put the card down afterwards.

**`SkipToSite` plays whole turns, reconciling included.** A small draw deck is drawn into the players' hands on
the way. `SkipToMovementDecision` and `SkipToOtherPlayersTurn` reconcile too: the Shadow player draws up to 8
before the second move is asked about, so a test that turns on his hand at the second move empties his draw deck
first. A test that needs cards in the draw deck afterwards puts them back with `MoveCardsToTopOfDeck`. And as
the fellowship decision is already pending when it returns, a card made playable by cheating is not offered
until some legal action has been taken.

**A reconcile restores first.** By the time a reconcile's discard decision is pending, the reconciling player's
hindered cards have been restored (CR 5.0 "reconcile"; Q-0059). That is so for both players' regroup reconciles
and for "reconcile your hand" effects.

**A deck's order is set after `StartGame()`.** The game shuffles each deck to deal the opening hands, so a card
moved to the top or bottom of a draw deck before `StartGame()` does not stay there.

**A card being shown is listed as not selectable.** A real reveal from a deck or from play gives every player,
the owner included, a "Revealed card(s)" decision to dismiss. A reveal from a HAND (the whole hand, or cards at
random) gives the pop-up to the other players only: the hand's owner is shown nothing and has nothing to dismiss
(Ketura on Q-0024 and Q-0036; the game log names the cards). In the decision
`FreepsHasCardChoiceAvailable(card)` answers false:
`FreepsHasCardChoiceAvailable(card, false)` is the reading for a card that is shown. A definition that only sets
`reveal: true` on moving a card writes a line in the game log and shows nobody anything.

**A card made known by the game log alone.** A card taken from a draw deck by its type (`reveal: true` on the
taking), and a card revealed from a hand to a player who gets no pop-up, is told to the other player by a line in
the game log and nothing else. `MarkGameLog()` just before the action starts the reading;
`GameLogSinceMarkMentions(cards...)` is true when every card given is named in some message since the mark.
Assert it for the cards taken or revealed and assert it false for a like card that was not, with
`GetGameLogSinceMark().toString()` as the failure message. A card is matched by its link (blueprint id and full
name): two copies cannot be told apart, and a card the log names for another reason is named too, so where a copy
of the event being played is among the cards taken, set the mark after the play. Both readers fail the test if no
mark was set.

**Making a card something it is not.** `ApplyAdHocModifier` with `IsAdditionalCultureModifier` or
`IsAdditionalRaceModifier` gives a card a second culture or race, and filters see it. That is how to tell a
filter written with a race from one written with a culture when every card of the race has the culture.
`AddKeywordModifier` does the same for a keyword (an [isengard] minion made a tentacle; every site made a marsh
with `new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH)`).

**An ad hoc modifier aimed at one card goes on after that card is in play.** A card is given a new id each time
it enters play, and a modifier whose filter is a physical card holds the id it had when the modifier was built.
`ApplyAdHocModifier(new VitalityModifier(null, vile, null, 1))` before `MoveMinionsToTable(vile)` does nothing
and says nothing. A modifier with a general filter (a card type, a keyword) can go on at any time, and where the
card has to be played legally afterwards, build the modifier on a filter and not on the card:
`scn.GenerateShadowFilter("name(Goblin Armory)")`, which throws `InvalidCardDefinitionException`
(`com.gempukku.lotro.cards.build`) for the test method to declare.

**Answers that are refused when they do not fit what is asked.** `FreepsResolveRuleFirst()` and
`FreepsResolveActionOrder(text)` fail the test unless the Free Peoples player is being asked to order required
responses (`FreepsAwaitingRequiredResponseOrder()` reads that, for both of the game's wordings: "Required
responses" after an event, and '<effect> - Required "is about to" responses' when two required triggers answer
something about to happen, as two copies of one card do), so a required trigger that has wrongly become
optional is noticed. `FreepsResolveRuleFirst()` also fails when no rule is among the responses to be ordered
(two triggers of cards: name one with `FreepsResolveActionOrder`). `FreepsDismissRevealedCards()` and
`ShadowDismissRevealedCards()` fail unless that player is being shown cards; `DismissRevealedCards()` dismisses
for whoever is being shown some and fails if nobody is. `FreepsGetTotalRevealedCardCount()` and its Shadow twin
count the cards a player is being shown, 0 if none. `FreepsChooseOptionalTrigger(card)` fails unless that very
card is offering, and with `(card, text)` unless it is offering an action so worded. The plain pass methods are
not like this: a pass with nothing pending does nothing and says nothing (Ketura, on G-0015: passes stay
harmless), and `FreepsPass()` and `ShadowPass()` answer whatever is pending with nothing, an optional trigger
included. Do not write one "to be safe". (The older, wordier `FreepsPassCurrentPhaseAction()` and
`ShadowPassCurrentPhaseAction()` were removed from the rig on 2026-10-05 at Ketura's word; old tests you read in
git history may still show them.)

**A response is offered once per use.** After a response cancels an ability or an event, no response to it is
offered again (Q-0043), so a pass or decline written to answer a second offer answers the next decision instead.
Optional triggers are still offered.

**A Shadows-block site path.** Pass `VirtualTableScenario.Shadows` as the format and a site map of your own;
`StartGame(scn.GetFreepsSite("site1"))`; after the fellowship phase is passed the Shadow player lays the next
site with `ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"))`. `Card_13_164_Tests` is a worked example.

**A table in a format that uses Maps.** `VirtualTableScenario.PCMovie` works with every constructor: each deck is
given the Map of the path its sites are on (`FellowshipMap` 102_74, `TowersMap` 102_75, `KingMap` 102_76). The
constructor ending `(..., format, freepsMapId, shadowMapId)` names each player's Map: use it to tell the two
Maps apart, or where the sites are of a path that has no Map (Shadows, Hobbit, a mix), which otherwise throws
`IllegalArgumentException`. Both decks still hold the same sites: deck validation holds a deck to its Map, a
game does not. `GetFreepsMap()` and `GetShadowMap()` return each player's Map, or null in a format without Maps.
A Map is in no pile and has no alias; from the first decision after the bid it is in its owner's support area
(`assertInZone(Zone.SUPPORT, scn.GetFreepsMap())`). That it is laid there only after the bid is how the client
presents it and no finding (Q-0074). With no companion in the decks the game does not stop for a starting
fellowship and rests on the mulligan question.

**Validating a deck.** `DeckValidationScenario` (test sources, not in `vts-api.md`) builds a deck round the
cards a test names and validates it. Its filler is the card as the scenario's format plays it, so a deck that is
otherwise legal validates with no errors at all: `assertEquals(List.of(), dvs.validate(deck))`. The Ring-bearer,
Ring, Map and sites are the ones the test names, as named: the rig's `FellowshipSites` have Ettenmoors 1_331 as
first printed, which the PC formats refuse for its errata. `hasMapError(errors)` says whether a Map objected;
`getMapErrorSites(errors)` returns the names of the sites its objection lists, for reading that a stray site is
named and the valid ones are not.

**The twilight cost of a card depends on where it is.** `GetTwilightCost(card)` answers for where the card is:
for a card in play, the number printed on it (F-0109, Q-0073); for a card in a hand or pile, what it would cost
to play now, roaming apart; for a site, its Shadow number as it stands. To read a cost modifier, read a card in
hand, or play the card and read the pool. A transfer costs the printed number and nothing else (Q-0053, Q-0074).

**The no-effect flag.** Every action a player is offered carries a word saying whether the game expects it to do
nothing. `FreepsActionNoEffect(card)` and `ShadowActionNoEffect(card)`, and the same with words of the action's
text for a card that offers several (`ShadowActionNoEffect(easterling, "fierce")`), read it for that card's
action in the player's pending choice of actions: `"false"`, or why the game expects it to do nothing:
`"effect"`, `"limit"` or `"skirmish"`. They fail the test if the player is not being offered a choice of actions,
if the card offers no such action, or if more than one fits. A flagged action is offered and may be taken like
any other: read the flag, then take the action. An optional trigger is read the same way while its offer is
pending (`FreepsActionNoEffect(card)`, then `FreepsChooseOptionalTrigger(card)`). An ability or optional trigger
with a cost whose limit is on what its effect gives (`effectLimitPerPhase` / `effectLimitPerTurn` in the
definition) reads `"limit"` once it has given as much as the limit allows, and one whose capped bonus is spent
likewise. One with a limit on uses is not offered at its limit, so there is no flag to read. The flag is worked out when the decision is sent, so after cheating the board get a new decision
before reading it. At site 2 of `FellowshipSites` (Ettenmoors) each player
is also offered the site's own skirmish action in every skirmish. Which cards assert the flag: test-generation,
"Rulings, interactions and formats".

**A play inside an `If` does not gate its action.** The engine refuses an action it can see will play nothing
only when the play is the action's top-level effect. Wrapped in an `If`, the action is always offered and does
nothing when used (F-0036, Out of Dark Waters). A definition written that way is a finding, not a quirk to test
round.

**A card that passes through a zone.** Where an effect takes a card out of a place it has only just arrived in
(a tentacle killed by a wound and then stacked from the discard pile), read it there while the decision is
pending. A count that is 0 before and 0 after shows nothing by itself.

**The Ruling Ring interrupts skirmishes.** With the default Ring, a Ring-bearer about to take a wound in a
skirmish gets an optional response. `FreepsDeclineOptionalTrigger()` declines it. Keep the Ring-bearer out of
skirmishes that are not about him.

### Added by engine pass 7 (2026-10-07)

- **Two offers of one response.** When the same card offers the same trigger or response twice in one decision (two Nazgul winning one skirmish), each offer's text ends with " (for <the cards its trigger memorized>)". A single offer reads as before. Choose between them by that text.
- **A board changed under a pending decision.** The game works out what a decision offers when it sends it. After a cheat that changes the board while a player is at a choice of phase actions, call `FreepsRefreshDecision()` / `ShadowRefreshDecision()` before reading what is on offer; they fail unless that player is at such a choice.
- **Definitions of the tests' own.** `gemp-lotr-server/src/test/resources/testCards.hjson` holds card definitions known to every table by set-0 ids (`0_8001` on), read by `framework/TestCardLibrary`. They are for mechanic tests that need a shape no real card has. A card's own tests use real cards. The file is a test resource: after editing it, `target/test-classes` needs it (a `mvn test-compile`, or copy it there).

### A card that leaves play as it is played (F-0139, 2026-10-07)

**A card that leaves play as it comes into play was not played.** A minion played beside Watcher in the Water, a tentacle played away from a marsh, a possession played on a bearer of Shadowfax: the card goes to the discard pile, what it cost stays paid, and nothing that answers a play happens or is offered: not its own "when you play", not another card's "each time you play", not a response (CR 4.2 "discard"; `mechanics/DiscardedOnEntryTests`). The standing discard is done before the play is announced, so no "Required responses" question is asked between it and a play's own triggers, for a card that stays either. The game log has a line "<card> left play as it came into play".

**A play as a cost can come to nothing.** An ability whose cost is to play a card is offered, takes its other costs and the card's twilight, and does nothing if the card has left play by the time its play is complete, whatever removed it. Read the costs as paid, the effect as not done, and the phase's decision.

**The loss of initiative comes with the play.** When a Free Peoples card played from hand takes the hand below four, `FreepsHasInitiative()` is still true while the card's entering step is being answered and false once its play is announced; a required "lose initiative" trigger and a required "each time you play" trigger for that play come up in one "Required responses" decision.

### Added by engine pass 8 (2026-10-07)

- **A number that can only be one number is not asked for** (F-0147). Where an effect asks for a number and only one can be named (X from 1 to 1, "how many to spot" with none to spot), the game takes it. A test of such a board does not answer the question, and an answer written "to be safe" goes to the NEXT decision (the symptom is a wrong number later, not an error). To read the bounds of an X, arrange a board where two numbers can be named.
- **Two at once, one use.** A required trigger with a limit on uses fires once when one event raises it twice (two companions exerting together): no ordering question is asked for the second.
- **Test cards.** `0_8002` Test War-horn copies every keyword of an exerted minion to your other minions until the regroup phase, as a snapshot (mechanics/KeywordSnapshotTests); `0_8003` Test Waystone is the per-site limit's test card (see the comment above it in testCards.hjson).

### Added by engine pass 9 (2026-10-07)

A card discarded by its own "when you play this ..., or discard it" (the three Dunlendings of set 5, Stampeding Chief) is the same: the Free Peoples player's or the Shadow player's choice in that text is the first thing asked, before any trigger or response to the play, and if the card goes nothing answers its play. Beside the Watcher such a card is discarded first and the choice is not asked at all.

**What an ally bears is in the support area.** A "support card" is a card in a support area or borne by a card that is (`mechanics/SupportAreaTests`): a Shadow condition on an ally (Weary 4_212 plays on a companion or ally) or on a site the Shadow player controls (Pillage of Rohan 4_167, Wolves of Isengard 5_69, No Retreat 4_30; `ShadowTakeControlOfSite(scn.GetSite(n))` after `SkipToSite(n + 1)`, then `AttachCardsTo(site, card)`) is a valid candidate for "a Shadow support card", and the same card on a companion is the invalid one beside it.

### Test cards added by engine pass 10 (2026-10-07)

- `0_8004` Test Seal: "Burdens cannot be added.", the test card for the definition word `cantAddBurdens` (mechanics/CantAddBurdensTests).
- `0_8005` Test Mirror gives one minion's game text and keywords to another until the assignment phase, as a snapshot (mechanics/GameTextSnapshotTests): the test card for `DuplicateGameText`'s `snapshot: true`.

### Added by engine pass 11 (2026-10-07)

**The Ring-bearer is the rules' to protect, hindered or not.** A discard or a return to hand that reaches hindered cards (`override: hindered`: "a card", "a Free Peoples card") neither offers nor takes a hindered Ring-bearer (CR 4.2 "Ring-bearer"; F-0174; `mechanics/HinderedRingBearerTests`), so a definition needs no `not(ring bearer)` there, and with him the only candidate the effect does nothing. What another CARD forbids about cards gives a hindered card nothing: beside Uruk Follower 4_186 a hindered unbound Hobbit is offered and discarded.

**A hindered card that is shuffled into a draw deck is hindered no longer.** `IsHindered(card)` is false once the card is in the deck, as it is for a card discarded or returned to hand (CR 5.0 "hinder"; `mechanics/HinderedCardLeavingPlayTests`).

`FreepsUseCardAction(card)` and `ShadowUseCardAction(card)` take the action `FreepsActionAvailable(card)` sees, even where the game lists another of the card's actions first (a possession that another companion could bear lists "Transfer" before "Use"). A card that offers no such action has the first action listed for it taken, as before: an optional trigger or a response is accepted this way. `FreepsUseCardAction(card, "Use")` is no longer needed to get past a transfer.

A player over the hand size at a reconcile on the way discards down to it from the front of his hand, however many cards that is (`PassReconcile(player)`, which every turn-skipper uses), so a test may put any number of cards in a hand before skipping. A test that reaches "Choose cards to discard down to 8" by its own passes answers it in one go: `scn.PassReconcile(scn.P1())`, or `FreepsChooseCards(...)` with exactly `FreepsGetChoiceMin()` cards.

- `0_8006` Test Undertow ("Maneuver: Remove (1) to discard a Free Peoples card.") and `0_8007` Test Ebb-tide ("Maneuver: Remove (1) to return a Free Peoples card to its owner's hand."): Shadow conditions whose effects carry `override: hindered`, for reading what a discard or a return to hand by side alone may take (mechanics/HinderedRingBearerTests).
- `0_8008` Test Riptide ("Maneuver: Remove (1) to discard a card.", reaching hindered cards) and `0_8009` Test Maelstrom ("Maneuver: Remove (1) to discard The One Ring."): mechanics/TheOneRingIsNeverDiscardedTests.
- `0_8010` Test Fog ("Maneuver: Remove (1) to hinder a card."): mechanics/MapsAndMetaSitesCannotBeHinderedTests.

**The One Ring is never discarded.** A discard that could name it (Doom Causeway 103_127, "each time a card is
hindered, discard it") neither offers nor takes it, hindered or not; hindered there, it stays on its bearer,
hindered (`mechanics/TheOneRingIsNeverDiscardedTests`).

**Maps and meta-sites cannot be hindered,** as sites cannot: a hinder of "a card" on a PC-Movie or Race to
Mount Doom table does not offer them (`mechanics/MapsAndMetaSitesCannotBeHinderedTests`). The cheat
`HinderCard` hinders whatever it is given and asks no rule.

**Whose support area.** A card borne by a card in a support area is in THAT support area, whoever owns it
(`mechanics/WhoseSupportAreaTests`): the Shadow player's Weary on your ally is in your support area ("a
condition from the Free Peoples support area" reaches it; "a Shadow condition from an opponent's support
area", used by you, does not). For "your / an opponent's support area" the valid candidates are a card lying
there and one borne by an ally or a controlled site there; the invalid ones are the same card on a companion
and a card of the right owner borne by a card in the other player's area.

**A shuffle from a discard pile into a draw deck always shuffles.** Taking "you may shuffle up to 2 ... from
your discard pile into your draw deck" and choosing none, or with nothing there to choose, reads one more
shuffle of that deck and its count unchanged; declining the offer reads none.

**A hindered card is still borne.** "Bearing 3 or more cards" and "for each card he or she bears" count a
hindered card; "for each possession" does not. Hinder one of the counted cards with `HinderCard` and read
the count unchanged.

**A response event is not played by another card's effect.** "Play a(n) ... event from your discard pile /
stacked here / from hand" neither offers a response event nor counts one towards being usable; an event that
is a response and a phase event (Eye of Barad-dur 5_96) is offered in its phase only
(`mechanics/ResponseEventsAreNotPlayedByEffectsTests`). For "a [culture] event" a response event of that
culture is the invalid candidate beside the phase events, and a phase word in the definition's filter beside
it cannot be told from its absence: say so in Notes.

**An action's own exertions count against what it then needs.** "Exert X twice to play ... from your discard
pile" is not offered when X's exertions would leave the only candidate unplayable, and "exert A and B" is not
offered when one character with one exertion left is the only A and the only B
(`mechanics/CostsAreJudgedInTheOrderPrintedTests`). That is so where the cost names its card or only one
character can pay; where the player chooses among several, the action is offered on the board as it stands.
- `0_8011` Test Archive ("Shadow or Skirmish: Play an event from your discard pile.") and `0_8012` Test Lectern ("Shadow or Skirmish: Play an event stacked here."): Shadow conditions for mechanics/ResponseEventsAreNotPlayedByEffectsTests. The test booster pack (`product/Test-Booster.hjson`) holds a Shadow response event, Enheartened Foe 1_247, which Race Text 92_29 shows and does not let be chosen. A choice from a stack is answered with `ShadowChooseCard`, not `ShadowChooseCardBPFromSelection`.
- `0_8013` Test Whetstone ("Skirmish: Exert a Dwarf to play a [dwarven] event from your discard pile."): mechanics/CostsAreJudgedInTheOrderPrintedTests.

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
| An Aragorn who is not [gondor] | Aragorn, Defender of Rohan | 17_93 | The only Aragorn of another culture ([rohan]), and no signet; his text does nothing unless the Ring-bearer is assigned to a skirmish. Without him a `name(Aragorn)` filter narrowed to [gondor] passes every test. Aragorn signet: 1_365 (optional start-of-turn trigger, so put him down after `StartGame()`), 4_364. Gandalf signet: 1_89, 4_109. There is no Boromir outside [gondor] |
| A tentacle that stays put | Vile Tentacle | 101_36 | The only tentacle not discarded away from a marsh. Reaching (19_19) and Strong (19_20) have nothing on being played; Foul (2_58) and Huge (2_66) have optional when-played fetches, so cheat them in. No tentacle has more than 2 vitality. Away from a marsh, make every site one with an ad hoc keyword; for a marsh with a site after it that is not one, put Midgewater Marshes (1_332; each Hobbit exerts on moving there) in the site map as site 2 |
| A way to discard a [moria] condition | Song of Durin | 3_5 | Free Peoples event: "discard up to 2 [moria] conditions" |
| Other ways to discard Shadow cards | Sleep, Caradhras | 1_84 | Fellowship event: exert Gandalf to discard every condition. One [sauron] minion, condition or possession of the Free Peoples player's choosing: Stand Against Darkness 1_63 (maneuver; exert an Elf). Every Shadow card a minion bears, of whatever types, at once: Fall Back 5_34, with City Wall 5_33 cheated onto the minion. The Shadow player discarding his own [sauron] condition as a cost: Orc Veteran 3_100 |
| A Ring-bound companion who is not the Ring-bearer | Sam, Son of Hamfast | 1_311 | Strength 3, vitality 4; Ring-bound by rule. A fellowship ability that is only ever used on purpose, and a response when Frodo dies. Without him "the Ring-bearer" written as `ring-bound` passes every test |
| A Nazgûl who is not [ringwraith] | Ulaire Attea, Khamul of the East | 103_61 | [raider], strength 12, vitality 3; his text needs 3 burdens. Fierce, so keep him out of tests that run past the skirmishes |
| A twilight Nazgûl | Úlairë Nelya, Ringwraith in Twilight | 2_84 | Strength 10, vitality 3; nothing else below 2 burdens and below 2 wounds on the Ring-bearer. Úlairë Attëa 2_82 is another, cheated in (when played, may wound an ally); 2_83, 2_85 and 3_86 have optional "wins a skirmish" triggers. A Nazgûl neither twilight nor fierce: Úlairë Lemenya 1_232. Twilight and no Nazgûl: Corpse Lights 6_99 |

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

Some words cannot be told apart by any card there is: only companions have a signet, only events have a
timeword, every Aragorn and every Gandalf is a companion. A filter mistake that drops or adds such a word makes
no difference to play; say so in the ledger's Notes and move on.

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

An author reports the need and does not edit the rig; the orchestrator has it built. An addition needs no
approval from Ketura, only a real use for it in a test at hand, and is recorded afterwards in `rig-requests.md`
for her information (rulings log, 2026-10-06 evening).
