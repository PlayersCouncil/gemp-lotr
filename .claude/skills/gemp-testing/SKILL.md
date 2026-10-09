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
finding out what the game does in some situation without adding a test. To print what a player is being asked
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

- **How the engine asks:** The engine asks once · One candidate is chosen for you; none is skipped · A board changed under a pending decision · A number that can only be one number is not asked for · A choice with nothing on any branch is not asked · "Wound each" and "wound every" wound one card at a time · Choosing which skirmish to resolve is always asked · Who is offered for assignment · No method names the moment after the assignments · One minion to a companion, and the minions left over · `IsCharAssigned` reads only the skirmishes still waiting · A check on a player who is not being asked anything answers false · A number with no upper bound · Answers that are refused when they do not fit what is asked.
- **What is offered, and what an offer does:** Costs gate an action; effects are only attempted · A "wound X twice" branch of a choice is not offered to a character with 1 vitality left · A play inside an `If` does not gate its action · The no-effect flag · An action's own exertions count against what it then needs · A prevention is asked only when its effect will happen · A response event is not played by another card's effect · A response is offered once per use · Two at once, one use · A Response ability is never "used" · A discount with a limit is spent when the card is played · A meta-site's optional trigger is offered to every player unless its definition names one.
- **Reading and answering an offer:** "Is an optional trigger pending" is a broad question · A yes/no in the middle of a trigger is not an optional trigger · An ability with wording of its own, and a card with several · Using a card's action · Two copies of one card · A choice from a pile, and a showing, are read by blueprint · Two offers of one response · When a wound kills and something answers the same wound · A response window can come before the target.
- **Getting to the moment: set-up, the skippers and turns:** Set the twilight pool before the Shadow phase starts · Both players act in most phases · No minions, no combat · Minions do not survive a turn · Site 9 ends the game · The game ends when it ends · A starting fellowship · Archers get in the way · `SkipToPhase` declines optional triggers it meets on the way · Two required start-of-turn triggers stop `StartGame()` short · An optional trigger at the start of the turn is the first thing asked · `SkipToSite` plays whole turns, reconciling included · A reconcile on the way discards from the front of the hand · A reconcile restores first · A card's "reconcile your hand" asks what the regroup reconcile asks, in the middle of the phase · A deck's order is set after `StartGame()` · The Ruling Ring interrupts skirmishes · A companion beaten by double his strength is killed, not wounded · A second assignment or skirmish phase in one turn takes a second move · Who places archery wounds · Allies who take archery wounds instead of companions · An ally in a skirmish · One blueprint under two site keys gets one alias · `FreepsDeclineSanctuaryHealing()` sends five empty answers, whatever is being asked · Sanctuary healing is asked at any site laid as site 3 or 6, and only when a companion is wounded · Whose site · At the move limit, the regroup phase ends the turn · The move limit never reads below 1 · `SkipToOtherPlayersTurn()` straight after `SkipToMovementDecision()` throws · A card discarded for where the fellowship is goes when the move is finished · A draw deck runs short in a long file · A random or whole-hand effect takes whatever is there · The fierce round reads as the first does · Before `StartGame()` the burdens read 1.
- **Cards, owners and zones:** Exerting is not wounding · Allies live in the support area · A play read as not offered for a card that is not in hand shows nothing · The other player's Free Peoples cards are not active on your turn · An event is in the void while it is being played · `GetVitality` is the vitality left · Culture tokens are of the card's own culture · The Ring is attached to the Ring-bearer · A card that passes through a zone · What an ally bears is in the support area · A follower's Aid is an optional trigger at the start of the maneuver phase · Whose support area · The twilight cost of a card depends on where it is · A twilight cost never reads below 0, so a discount as large as the card's cost hides a larger one · Whether a minion is roaming is read as a keyword · Making a card something it is not · An ad hoc modifier aimed at one card goes on after that card is in play · An ad hoc effect is run in place of a player's answer · A value modified below zero counts as zero.
- **A card that leaves play as it is played:** A card that leaves play as it comes into play was not played · A play as a cost can come to nothing · The loss of initiative comes with the play · A card that discards itself as it is played was not played either.
- **Hidden zones, reveals, shuffles and the game log:** Searching a deck shows the deck first · Whether a deck was shuffled · The Rule of 4 · A card being shown is listed as not selectable · A card made known by the game log alone · A shuffle into a draw deck always shuffles.
- **Hinder, The One Ring and Maps:** The Ring-bearer is the rules' to protect, hindered or not · A hindered card that is shuffled into a draw deck is hindered no longer · A hindered card is still borne · The One Ring is never discarded · Maps and meta-sites cannot be hindered.
- **Tables and formats:** A Shadows-block site path · A battleground or a forest site needs no cheat · A table in a format that uses Maps · Validating a deck.
- **Test cards and the test booster pack:** Definitions of the tests' own · Test cards · Test Seal 0_8004 · Test Mirror 0_8005 · Test Undertow 0_8006 · Test Riptide 0_8008 · Test Fog 0_8010 · Test Archive 0_8011 · Test Whetstone 0_8013 · Test Muster 0_8014 · Test Shelf 0_8016 · Test Fork 0_8018 · The test booster pack.

## Choosing helper cards

A helper card with text of its own can change the game under a test. Read the definition of every card that goes
into a scenario; a name is not evidence of what a card does.

| Need | Card | Id | Why |
|---|---|---|---|
| A minion that does nothing | Goblin Patrol Troop | 1_177 | Strength 13, vitality 3, cost 6, site 4, no text at all. It overwhelms (kills) any companion of strength 6 or less: see "A companion beaten by double his strength" in the pitfalls |
| A cheap minion | Goblin Runner | 1_178 | Strength 5, vitality 1; when played, may add (2): harmless if cheated onto the table, and added without asking when it is played for real (F-0009). Played beside a card with its own required trigger on a minion being played (The Faithful Stone 68_50), the two come at once and the Free Peoples player orders them in a "Required responses" decision: play Goblin Patrol Troop there |
| A sturdy companion | Aragorn, Ranger of the North | 1_89 | Strength 8, vitality 4; Ranger; a maneuver ability that is only ever used on purpose |
| A weak companion | Dwarf Guard | 1_7 | Strength 4, vitality 2; needs a Dwarf spotted to be played, so cheat it in |
| An archer, when archery is the point | Legolas, Greenleaf | 1_50 | Strength 6, vitality 3; Archer, with an archery ability |
| Damage +1, when damage is the point | Gimli, Son of Glóin | 1_13 | Strength 6, vitality 3; Damage +1, with a skirmish ability |
| A minion with damage +1 and nothing else | Uruk-hai Raiding Party | 1_158 | Strength 9, vitality 3, cost 4, site 5; Damage +1 is its whole text. With vitality 2: Orthanc Warrior 4_165 (strength 7, cost 2, site 5), killed by exactly two wounds, which tells "wound twice" from one wound and from two exertions |
| A [gondor] ranger who is not unique; a [gondor] Man who is no ranger | Ranger of Osgiliath; Gondorian Knight | 7_116, 5_35 | Both Men, cost 2, strength 6, vitality 3, not unique. The Ranger has a regroup ability (discard 2 cards from hand to exhaust a minion) used only on purpose, and beats an Uruk Savage without killing it; Knight is the Knight's whole text |
| A Shadow archer and nothing else | Goblin Marksman | 1_176 | Strength 7, vitality 1, cost 3, site 4; Archer is its whole text |
| An item that changes no number | Dwarven Armor | 1_8 | Possession, cost 0, bearer must be a Dwarf; bearer may not be overwhelmed unless strength is tripled |
| A weak minion that survives a hit | Uruk Savage | 1_151 | Strength 5, vitality 3; Damage +1 is its whole text. An unboosted strength-6 companion beats it without killing it |
| A companion taken out of play in the fellowship phase | Saved From the Fire | 8_20 | Free Peoples Fellowship event, cost 3: spot Gandalf (by name) and place a companion, not the Ring-bearer, in the dead pile (a kill, as its cost) to take 0 to 3 cards of his culture from the draw deck, which is shown first: see "Searching a deck shows the deck first" in the pitfalls |
| Wounds in the regroup phase | Morgul Slayer | 3_93 | Strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)." Two copies wound one companion twice in one regroup phase, which is how to read "until the regroup phase" at its boundary |
| A Shadow card a companion bears | Spider Poison | 8_28 | Possession; its bearer must exert each time it is assigned to a skirmish, and nothing else |
| An Aragorn who is not [gondor] | Aragorn, Defender of Rohan | 17_93 | The only Aragorn of another culture ([rohan]), and no signet; his text does nothing unless the Ring-bearer is assigned to a skirmish. Without him a `name(Aragorn)` filter narrowed to [gondor] passes every test. Aragorn signet: 1_365 (optional start-of-turn trigger, so put him down after `StartGame()`), 4_364. Gandalf signet: 1_89, 4_109. There is no Boromir outside [gondor] |
| A tentacle that stays put | Vile Tentacle | 101_36 | The only tentacle not discarded away from a marsh. Reaching (19_19) and Strong (19_20) have nothing on being played; Foul (2_58) and Huge (2_66) have optional when-played fetches, so cheat them in. No tentacle has more than 2 vitality. Away from a marsh, make every site one with an ad hoc keyword; for a marsh with a site after it that is not one, put Midgewater Marshes (1_332; each Hobbit exerts on moving there) in the site map as site 2 |
| A way to discard a [moria] condition | Song of Durin | 3_5 | Free Peoples event: "discard up to 2 [moria] conditions" |
| Other ways to discard Shadow cards | Sleep, Caradhras | 1_84 | Fellowship event: exert Gandalf to discard every condition. One [sauron] minion, condition or possession of the Free Peoples player's choosing: Stand Against Darkness 1_63 (maneuver; exert an Elf). Every Shadow card a minion bears, of whatever types, at once: Fall Back 5_34, with City Wall 5_33 cheated onto the minion. The Shadow player discarding his own [sauron] condition as a cost: Orc Veteran 3_100 |
| A Hobbit who is not [shire] | Pippin, Trollslayer; Merry, Master Holbytla | 103_31, 103_85 | The only two Free Peoples ones. Pippin is [gondor] (to play, spot a [gondor] companion: cheat him in; a skirmish ability used only on purpose). Merry is [rohan] (the only [rohan] character who is no Man), and a Man as well while 3 [rohan] companions or Éowyn can be spotted; each time another [rohan] Man exerts he offers an optional trigger (exert Merry to heal that Man), so keep him out of a test that exerts one. Without one, "a Hobbit" narrowed to [shire] passes every test. The opponent's Hobbit, for a bare "Hobbit" (Q-0058), is Ted Sandyman 18_75: a [men] MINION whose race is Hobbit (strength 3, vitality 4, site 2; a maneuver ability that needs a site he controls and a regroup ability used only on purpose). He bears the [men] minions' weapons: Henchman's Dagger 18_69 (hand weapon, cost 0, strength +1) and Long Battle Bow 11_89 (ranged weapon, strength +1, archer: see "Archers get in the way") |
| A [shire] character who is no Hobbit | Goldberry, River-daughter | 9_51 | Ally, home 2F, strength 4, vitality 6, no race; her texts need a river. Tom Bombadil 9_52 is the other ally (to play, remove 2 burdens or 2 threats: cheat him in). The one COMPANION is Smeagol, Once Like Us 103_116: strength 3, vitality 4, resistance 6, Ring-bound, no race; to play, add a burden (cheat him in); an assignment ability only. He can fight and bear a follower |
| A follower that changes no number | Rohan Worker | 15_135 | Not unique, no printed strength or resistance (most followers give the bearer one); Aid (1); its texts answer a transfer to a companion who is not [rohan] (bearer exerts twice: cheat it on) and a skirmish its bearer wins. Elendil's Army 18_47 (unique, strength +2, no resistance; Aid: exert a [gondor] companion) is the one to transfer by legal play: see "A follower's Aid" in the pitfalls |
| A [gandalf] Wizard who is an ally; a weapon that is no possession | Saruman, The White; Glamdring (The Hobbit) | 32_24, 30_29 | Saruman is an ally (home 3H, cost 4, strength 8, vitality 4, wise); a Gandalf is defender +1 beside him, and his two abilities are used only on purpose. Without him "a [gandalf] Wizard" narrowed to companions passes every test (67_15, 67_24); Radagast 31_17 is the other. Glamdring 30_29 is a [gandalf] artifact hand weapon (strength +2, on Gandalf by name; an optional trigger when he wins a skirmish): it tells `weapon` from `possession`, and shares its unique title with Glamdring 1_75, so never both on one board |
| A valiant companion and nothing else | Household Guard | 5_83 | [rohan] Man, strength 6, vitality 3; Valiant is its whole text. Every valiant card is a companion, so "valiant companion" cannot be told from "valiant": a line in Notes |
| A Shadow card that is not what its culture mostly is | Morgul Creeper; Strength Born of Fear | 8_75, 1_278 | Morgul Creeper is a [wraith] Orc (strength 8, vitality 2; archery +2 only at 3 burdens): the culture without the race for "a Nazgûl". Strength Born of Fear is a [sauron] support-area condition whose skirmish ability costs (3): a [sauron] card that is no minion, for "a [sauron] minion". A [moria] minion that is no Orc: Vile Tentacle 101_36, above |
| A [gandalf] companion who is not Gandalf | Ent Avenger | 6_27 | Strength 6, vitality 4; a strength bonus only when 3 Ents can be spotted. Tells `name(Gandalf)` from `culture(gandalf)` |
| A [gandalf] Man (the Ent Avenger above is the [gandalf] companion who is none) | Grimbeorn, Beorning Chieftain; Dasron, Merchant From Dorwinion; Erland, Advisor to Brand | 14_6, 13_29, 2_21 | Grimbeorn (the only Grimbeorn; strength 4, vitality 5) is healed at the start of each turn, and his maneuver offer needs a minion in his owner's hand. Dasron (companion, strength 6, vitality 3) makes a fellowship offer only with 2 Free Peoples culture tokens to remove. Erland (ally, home 3F, strength 5, vitality 2; cheat him in, he needs Gandalf spotted) stops Shadow players looking at the hand |
| A discard the Free Peoples player makes by no card of his own; a Free Peoples card that has the Shadow player discard | Heights of Isengard; Introspection | 11_244, 12_29 | Heights of Isengard (Shadows-block site, battleground): each time a [gandalf] companion wins a skirmish the Free Peoples player may discard a condition from play; the source is a site, which has no side (path and play in Card_18_029_ErrataTests). Introspection ([gandalf] Fellowship event, cost 2, spot a [gandalf] Wizard): away from a battleground its only branch has the SHADOW player choose and discard one of his own conditions. Brooding on Tomorrow 10_14 does the same on a loss of initiative |
| A [gondor] character who is not a companion; an ally who is a Man | Gondorian Merchant | 7_97 | The only [gondor] ally; a unique Man, strength 6, vitality 1; home 3T, so he skirmishes on the Towers sites (`SkipToSite(2)` and a move). His maneuver ability needs initiative and 2 cards discarded from hand: only ever used on purpose. The ally among the candidates for "a character" or "a Man" (he may bear Flaming Brand 2_32) |
| An ally in a skirmish away from any home site (a Shadows-block path) | One Whom Men Would Follow | 1_109 | [gondor] Maneuver event, cost 0: exert Aragorn (any Aragorn; he keeps the wound), and a chosen ally is strength +2 and takes part in archery fire and skirmishes until the regroup phase. Nothing else; the ally is then assigned like a companion |
| A [gondor] artifact that someone bears | Ring of Barahir; Narsil, Blade of the Faithful | 6_55, 9_34 | Ring of Barahir goes on any Aragorn (17_93 makes its bearer a [rohan] companion); an optional start-of-turn trigger, so attach it after `StartGame()`. Narsil 9_34 goes on a [gondor] Man, the Merchant included; Narsil 17_31 on Elendil or Isildur only. The two Narsils share a unique title: never both in play. The Shards of Narsil 3_44 is a [gondor] artifact nobody bears |
| A Man minion of the opponent's, and nothing else | Dunlending Brigand | 4_10 | [dunland] Man minion, strength 8, vitality 1, no text: the valid candidate on the other side for a bare "a Man" (Q-0058). Beats a companion of strength 5 to 7 without killing him. With 1 vitality he can never exert: where the Man must be able to ("exert 2 of your Men"), the opponent's is Easterling Axeman 4_224 ([raider], strength 6, vitality 2; strength +3 and fierce only at 2 burdens) |
| An [orc] minion who is no Orc, and other [orc] cards | Half-troll of Far Harad | 14_12 | [orc] Half-troll, cost 3, strength 8, vitality 2, site 4; its only text is an optional start-of-assignment trigger that needs (2) and more companions than minions. Tells `culture(orc)` from `orc`. A cheap [orc] Orc: Scurrying Goblin 11_137 (cost 1, strength 5, vitality 1, site 4; adds (2) only when played at an underground site beside another [orc] minion). An [orc] card that is no minion: The Beckoning Shadow 12_83 (support-area condition; to play, exert an [orc] minion, so cheat it in; nothing below 5 burdens and an empty Free Peoples draw deck) |
| A wound to a chosen companion who is in no skirmish, during another's skirmish | Orkish Skirmisher | 18_90 | [orc] Orc, not unique, cost 4, strength 9, vitality 3, site 4; "Each time this minion wins a skirmish, you may exert him twice to wound a companion." Any companion may be chosen, and the trigger is carried out before that skirmish phase ends. Two copies on one companion (ad hoc defender +1, and strength enough not to be overwhelmed) give two such wounds in one skirmish (Card_13_020_ErrataTests) |
| A Shadow card that exerts a companion | Mûmak Commander, Giant Among the Swertings | 15_86 | [men] Man, unique, cost 6, strength 14, vitality 3, site 4; "Maneuver: Exert Mûmak Commander twice to exert a companion twice (except the Ring-bearer)." Two exertions of the minion and then two of one companion, by one effect |
| A Ring-bearer who is not Frodo, and a Frodo who is not the Ring-bearer | Galadriel 9_14 as Ring-bearer, Frodo 1_290 cheated in as a companion; where the test needs 4 or more burdens, Sam, Bearer of Great Need 13_156 (resistance 5) instead, since Galadriel corrupts at 4 | 9_14 or 13_156, 1_290 | Tells `name(Frodo)` from `ring bearer` on every "Frodo" or "the Ring-bearer" card |
| A Ring-bound companion who is not the Ring-bearer | Sam, Son of Hamfast | 1_311 | Strength 3, vitality 4; Ring-bound by rule. A fellowship ability that is only ever used on purpose, and a response when Frodo dies. Without him "the Ring-bearer" written as `ring-bound` passes every test |
| A Shadow special ability in the archery phase | Wreathed in Shadow | 1_238 | [wraith] support-area condition, cost 0; "Archery: Remove (1) to make the fellowship archery total -1". To play, spot a Nazgûl: cheat it in and set the pool to 1. The Shadow doer for "the Free Peoples player uses an archery special ability"; Tower Assassin 2_93 (maneuver: exert to wound an ally) is the one for maneuver |
| A Nazgûl who is not [ringwraith] | Ulaire Attea, Khamul of the East | 103_61 | [raider], strength 12, vitality 3; his text needs 3 burdens. Fierce, so keep him out of tests that run past the skirmishes |
| A twilight Nazgûl | Úlairë Nelya, Ringwraith in Twilight | 2_84 | Strength 10, vitality 3, site number 2 (so not roaming at site 2: the minion beside a roaming one); nothing else below 2 burdens and below 2 wounds on the Ring-bearer. Úlairë Attëa 2_82 is another, cheated in (when played, may wound an ally); 2_83, 2_85 and 3_86 have optional "wins a skirmish" triggers. A Nazgûl neither twilight nor fierce: Úlairë Lemenya 1_232. Twilight and no Nazgûl: Corpse Lights 6_99 |
| A fierce minion with next to no other text (two of them, of other cultures and races, for "a minion" that loses or keeps fierce) | Black Rider; White Hand Destroyer | 12_161, 17_124 | Black Rider: [wraith] Nazgûl, not unique, cost 5, strength 8, vitality 3, site 3; strength +1 for each companion. Destroyer: unique [uruk-hai] Uruk-hai, cost 5, strength 10, vitality 3, site 5; hunter 3 only for each site controlled |
| A minion that GAINS fierce: by its own ability, and by standing text | Uruk Fighter; Easterling Axeman | 1_146, 4_224 | Fighter ([isengard] Uruk-hai, strength 8, vitality 2, damage +1): "Maneuver: Spot 5 companions to make this minion fierce until the regroup phase". Axeman ([raider] Man, strength 6, vitality 2): strength +3 and fierce while 2 burdens can be spotted, which outlasts the start of the regroup phase and so shows D for "cannot gain ... until the regroup phase". Neither is unique: a second copy is the control (68_110) |
| A [rohan] Man who is an ally (and a second) | Guma, Plains Farmer; Hlafwine, Village Farmhand | 4_277, 4_281 | Each: unique, cost 1, strength 4, vitality 2, home 4T (never fighting on a Fellowship path), Villager; discarded if an opponent controls his home site. Guma: "Fellowship: Exert Guma to heal a [rohan] Man"; Hlafwine: "Skirmish: Exert Hlafwine to make a [rohan] Man strength +2"; both used only on purpose |
| A burden removed as a Shadow card's COST; as a Free Peoples card's EFFECT | Ring of Rancor; Frodo's Cloak | 9_44, 4_303 | Rancor: unique [wraith] artifact (ring), cost 0, vitality +1, on a Nazgûl; "Maneuver: Remove a burden to heal each Nazgûl" (wound the Nazgûl first); its Response answers a reconcile by returning the bearer to hand. Cloak: unique [shire] possession, cost 2, on Frodo; away from sites 1T to 4T, "Skirmish: discard this possession to remove a burden and heal Frodo" (at those sites it adds a burden to cancel his skirmish) |
| Tales of each kind, for "each tale" | Thorin's Harp; The Saga of Elendil; Scroll of Isildur; Banner of the White Tree | 18_3, 1_114, 9_36, 3_39 | Harp: [dwarven] support-area condition, not unique, cost 0. Saga: [gondor] condition borne by a [gondor] companion. Scroll: unique [gondor] artifact, cost 1, to play spot a [gondor] Man; the Ring-bearer is resistance +1 for each [gondor] artifact. Banner: [gondor] Maneuver event, cost 0, never spotted. Every tale is on a card of a Free Peoples culture |
| An [uruk-hai] minion who is no Uruk-hai; an Uruk-hai of another culture | Saruman, Master of the White Hand; Cirith Ungol Guard | 17_116, 10_81 | Saruman: [uruk-hai] Wizard, unique, cost 4, strength 8, vitality 4, site 4; each companion is resistance -1, and a skirmish ability (exert him to discard a condition borne by a companion skirmishing an [uruk-hai] minion) used only on purpose. The printed culture without the race for "an Uruk-hai": no ad hoc culture is needed. Cirith Ungol Guard (cost 3, strength 9, vitality 2, site 6) and Cirith Ungol Sentry 10_84 (cost 2, strength 8, vitality 2) are [sauron] Uruk-hai whose whole text is Damage +1 |
| A companion who loses to a minion with damage +1 and takes one wound | Mithril-coat, Dwarf-mail | 13_153 | [shire] artifact (armor), unique, cost 2; bearer must be a Ring-bound Hobbit (Sam 1_311). Each minion skirmishing bearer loses all damage bonuses. Every plain Uruk-hai has Damage +1, which gives the loser two wounds and can hide "not overwhelmed" behind a death. Its skirmish ability (exert bearer: those minions lose fierce) is used only on purpose |
| A companion of 6 vitality by legal cards | Aragorn 1_89 with The Saga of Elendil and Ranger's Cloak | 1_114, 15_68 | Vitality +1 each. Saga: unique [gondor] tale, a condition a [gondor] companion bears; "Skirmish: Discard this condition to make bearer strength +2", used only on purpose. Cloak: [gondor] possession on a [gondor] Man, resistance +1 as well; while bearer is a ranger (Aragorn 1_89 is) each minion skirmishing him is site number +2, which matters only where roaming or a site number does |
| A place where no culture token can be added, removed or reinforced | Redhorn Pass | 13_194 | Shadows-block site (mountain, Shadow number 2): "Culture tokens cannot be added, removed, or reinforced." The `C-` board for a cost of adding or removing a token: the action is not offered there (Scouring of the Shire 68_112). On a Fellowship path it stands in place of a site no test reaches (pitfall "A Shadows-block site path", and mind the key) |
| A Free Peoples wound on a minion in the maneuver phase | Defend It and Hope | 4_115 | [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion once, or twice if it is an Uruk-hai. Kills a minion of 1 vitality to leave a board with no minion to spot |
| A minion assigned by a card and not by a player's assigning | Éowyn, Northwoman; Slow-kindled Courage; Shadowy Mount | 17_96, 7_328, 12_171 | Éowyn ([rohan] Man, strength 6, vitality 3): "Assignment: Exert Éowyn and assign a minion to the Ring-bearer", the Free Peoples player's own action; the minion loses its keywords until the regroup phase. Slow-kindled Courage ([shire] condition on Merry or Pippin: cheat it on) gives each minion "Assignment: Assign this minion to bearer", which the SHADOW player uses after `FreepsPass()` (`ShadowUseCardAction(minion)`). Shadowy Mount ([wraith] mount on a Nazgûl): the Shadow player makes the FREE PEOPLES player assign bearer to an unbound companion bearing a [wraith] condition; Blade Tip 1_209 cheated onto the companion is that condition and does nothing before the next fellowship phase |
| A Hobbit ally who may be assigned on turn 1 | Bounder | 1_286 | [shire] Hobbit ally, not unique, cost 1, strength 2, vitality 2, home 2F (site 2 of `FellowshipSites`); a skirmish ability used only on purpose. The Hobbit who is no companion |
| A [wraith] item that is no possession; chosen Shadow cards put in the discard pile by a legal play | Ring of Asperity; Fell Beast | 9_42, 6_83 | Ring: unique [wraith] artifact, cost 0, strength +1, on a Nazgûl; when played it may wound an unwounded companion, and it answers a reconcile. Fell Beast: [wraith] mount, cost 0, on a Nazgûl (bearer is fierce), "To play, discard 2 cards from hand": with two other cards in hand those two are discarded |

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
timeword, every Aragorn and every Gandalf is a companion, and the Ring-bearer is Ring-bound by rule (CR 4.2
"starting fellowship"), so "an unbound companion" is never the Ring-bearer. A filter mistake that drops or adds
such a word makes no difference to play; say so in the ledger's Notes and move on.

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
tested from each. A modifier's "you may" is declined in one test with the other player read as not offered: a
meta-site's optional trigger goes to every player unless its definition names one (pitfalls, "A meta-site's
optional trigger"). A modifier's phase action goes to whoever may act in the phase: where its text says "your",
read the other player as not offered on a board where he could pay its cost (shapes.md, F-0312). The game log calls a modifier by its picture card's title ("The Shire" for 91_2), never
"Race Text 91_2": read the log for what was done, not for the modifier's name.

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
