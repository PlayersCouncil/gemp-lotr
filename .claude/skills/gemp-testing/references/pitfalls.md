# Things that catch people out

The behaviours of the rig and the engine that have surprised authors in this campaign, grouped by what they are about. The gemp-testing skill lists every entry by its first words; read the groups your cards' tests will touch before writing them, and the whole file when a test does something you cannot explain.

Each of these has been checked against the engine as it stands on this branch.

## How the engine asks

**An exception at the play is a finding, and its tests park at the play.** Where the game throws as a card is
played or used, every test of it fails at the same line; where a second cause may stand behind the first, apply
a definition stand-in for one correction at a time with `mutate.py --replace --with-parked` to show which test is
each cause's, and say so in Notes (Hearth and Hall 17_8: F-0629's NullPointerException, F-0628's prompt behind it).

**The engine asks once.** The choices in a decision are worked out when the decision is sent. Cheating a card
onto the table while a decision is pending does not change what that decision offers. To see a card become
playable, complete the pending decision with a real action and look at the next one.

**A count inside a standing modifier's filter may be fixed as the modifier is made.** A parameter filter that
counts the board when its filter is made is worked out once for a standing modifier, when the modifier comes into
force, so a board laid before `StartGame()` passes whatever the count. `ForEachCultureToken` counts again too (under My Axe Is Notched 4_52 Gimli read strength 7, then 8, as wins placed tokens:
dec-b4_50). `highestRaceCount` counts again each time a
card is asked about (F-0669, engine pass 69); `higheststrength` and `loweststrength` remember their first count
(`SingleMemoryEvaluator`), harmless in the effects that use them now, each read once. Change the counted thing by legal play
while the modifier stands (Doors of Durin 13_190's companions played at the Doors) and read the modifier after it.

**One candidate is chosen for you; none is skipped.** When a choice (a cost's as well as a target's: 2 cards to discard from a hand of 2) has exactly as many valid candidates as it
needs, the engine makes it without asking, and a `FreepsChooseCard` written for it fails, naming the decision
the game has gone on to (G-0032). When it has none, that part of the effect is skipped; so is a choice among
cards an earlier effect set aside (`ChooseArbitraryCards`) when it set none aside (F-0338). With ONE card set
aside that choice is still asked, 1 of 1, of each player it names (Birthday Present 10_104: the Free Peoples
player chooses the one [shire] event, then the opponent chooses it; dec-b10_104). So a test that
means to inspect a choice has to supply one more valid candidate than the choice needs, and at least one invalid
one beside them. A parked test of a NUMBER the game reads otherwise ("X Shadow cards", 1 by the text and 3 as
built) wants one more candidate than the LARGER of the two readings, or the game takes them all unasked and the
test fails on a choice that is not there, not at the number it is parked for (Blood Runs Chill 8_3, Q-0256).
The same thing hides self-targeting: Celeborn's "exert Celeborn to heal an [elven] ally" heals
Celeborn himself when no other Elven ally is wounded. A heal's choice offers only WOUNDED characters, so an
unwounded one (the Ring-bearer, say) is no candidate: with one other wounded, the heal is made unasked and a
`FreepsChooseCard` fails at the next decision (Fates Entwined 13_148). A Fellowship-phase transfer is the same: `FreepsTransferCard(item)` with one other eligible bearer
moves it there unasked (Dwarven Axe 31_1 to the one other [dwarven] companion, Card_31_061_Tests). An ability used a second time counts its candidates afresh: once the first
use has taken one, a lone candidate left is played unasked (Saruman 5_56's play from the discard pile), and so does the next repetition of one play inside a single effect (Bolg 31_37's "play an Orc from your discard pile for each [dwarven] companion over 3": the last valid Orc is played unasked; hobb-b31_29). Any choice whose
minimum is 0 is the exception and asks even with one candidate left: an "up to N" (Song of Durin's "discard up to 2
[moria] conditions"), or each heal of Uruk-hai Healer 14_14's "you may heal an Uruk-hai for each companion" (one
wounded Uruk-hai, still asked).
A choice between piles goes the same way: "from your draw deck or discard pile" is not asked when only one pile
has a candidate, and a `ShadowChoose("draw deck")` then fails inside the next decision. That holds for the
discard pile always, and for the draw deck only where its branch says `mustExist: true`: without it a play from
a draw deck can always be tried (Q-0019), so with the one candidate in the discard pile both piles are still
offered and the test answers `FreepsChoose("discard pile")` (A Wizard Is Never Late 30_23, `Card_31_019_Tests`).
So does a transfer: with one legal bearer it is made without asking, and so does a card played on a bearer (Merry's Dagger 9_18 with Merry the only Merry beside Pippin: attached unasked, the game back at fellowship actions). A bearer who already holds a hand weapon is no candidate for a second one: lay a Shadow weapon on one of two valid bearers for another check and the hand weapon played goes to the other unasked (Gorbag's Sword 10_60, with Morgul Axe 7_186 on Cirith Ungol Warrior 10_56; dec-b10_57). With EVERY valid bearer holding a hand weapon, a second hand weapon is not offered as a play at all, and the rig says only that no play is offered: lay one more bearer for it (a second Orc Hammer 11_130; dec-b11_130). A card moved on from its bearer has that bearer as no candidate: with two minions in play, the second transfer goes to the other one unasked (Fifth Level 10_32). So does a play from a pile: with one card the
effect could play there, it is played unasked (Final Strike 10_20 lays two Gollums in the discard pile to show
the choice). So does a play from hand: a second use of Denethor 7_85's "Play a [gondor] knight" with one knight left in hand plays it unasked (Card_07_085_Tests).
Not so the choice of an `ifRemoveFromDiscard` discount ("remove from the game 4 [gollum] cards in your discard pile instead of paying the twilight cost"): with exactly four there it is still asked, "Choose cards to remove", 4 of 4 (Cunningly Hidden 13_45, empty pool; dec-b13_43), where Mountain-troll 15_112's `ifDiscardFromPlay` discards its N unasked.

**"Remove N [culture] tokens" from several cards asks a card at a time.** `removeTokensCumulative` (a cost the
rulings let take tokens from more than one card) asks "Choose card to remove a token from" for each token while
the event waits in VOID_FROM_HAND, and takes the last unasked once one card is left: with one token on each of two
cards the first is chosen and the second goes by itself, so tokens read straight after the play are not yet gone
(Make a Run For It 18_109, Card_18_109_Tests; dec-b18_107). "One card left" counts the cards still holding a token:
a card emptied partway through ends the asking, and a test that answers a second choice fails on "not a choice of
cards". It is so on an optional trigger's cost as on an event's (Fishing Boat 13_48, 1 token on Nice Fish and 3 on
Gladden Homestead: the Fish chosen, the Homestead's goes unasked; Card_13_048_Tests; dec-b13_48).

**A board changed under a pending decision: an action offered though its cost cannot be paid, or withheld though it now can.** The game works out what a decision offers when it sends it. After a cheat that changes the board while a player is at a choice of phase actions, call `FreepsRefreshDecision()` / `ShadowRefreshDecision()` before reading what is on offer; they fail unless that player is at such a choice. Wounds or discards cheated on turn 1 after `StartGame()` are the usual case: an action whose cost can no longer be paid still reads as offered, and looks like a cost-gating fault in the engine (Green Dragon Inn 1_322); used, it does nothing: no cost is paid and the card stays where it was (Thrór's Map 1_318, its Hobbits wounded after `StartGame()` so that none could exert). Five cheats need no refresh (engine pass 40): `SetFreepsHand`, `SetShadowHand`, `SetFreepsDrawDeck`, `SetShadowDrawDeck` and `MoveCardsToDiscard` (with `MoveCardsToFreepsDiscard` / `MoveCardsToShadowDiscard`) have a pending choice of phase actions worked out afresh as they finish, for either player, and leave any other decision as it was; before that, a card dealt into hand on the way and cheated into the discard pile still read as offered, and a bar on plays from the discard pile looked broken (The Great River 3_118). Every other cheat (`MoveCardsToHand`, `MoveCompanionsToTable`, `AddWoundsToChar` ...) still wants the refresh. `AddBurdens` and `AddThreats` are among them: in a fellowship phase after `StartGame()` a "To play, remove 2 burdens or 2 threats" is judged on the counts before the cheat until the refresh, so a "not playable" reading taken there passes for nothing (Tom Bombadil, The Master 9_52, Card_09_052_Tests). `SetTwilight` is too: Southron Explorer 4_250's "remove (5)" regroup ability is not offered after `SetTwilight(5)` at the Shadow player's pending regroup decision until `ShadowRefreshDecision()` (Card_07_315_Tests, dec-b7_314). lint's read-before counts the refresh as the game moving on: read what a cheat laid (`assertInZone(Zone.DECK, card)` for a card put beneath the draw deck) before the refresh, not after it (Card_33_025_Tests, hobb-b32_42). It hides an offer as well: a minion cheated in at the Free Peoples player's pending regroup decision is not counted in it, so a site's "your minion" read on HIS side (a site's "you" is each player, Q-0140) needs the minion laid before `SkipToPhase(Phase.REGROUP)` (Osgiliath Channel 8_120). A play is the same: a companion cheated onto the table in the middle of a fellowship phase is no bearer for a card in hand until the refresh, and `FreepsPlayAvailable` reads false with no other sign (Frodo, Frenzied Fighter 13_149 for Sting 5_116, Card_05_116_Tests); so is a card put into the hand by `MoveCardsToHand` after `StartGame()`, which `FreepsPlayCard` cannot play until the refresh (Rohirrim Shield 5_91, Card_05_091_Tests). A minion's own Shadow ability is the same: a minion cheated in once the Shadow player is already at "Play Shadow action or Pass" is offered nothing (`ShadowGetAvailableActions()` empty, `AwaitingShadowPhaseActions()` true, so lint's `not-asked` is silent and a "not offered" reading passes for nothing); lay it before the move into the Shadow phase (Morgul Squealer 8_77, Card_08_077_Tests). A minion so cheated in is no bearer either: a possession in the Shadow hand that needs him reads "not offered" (`ShadowPlayAvailable` false), and lint's `stale` does not flag it; lay the minions before the `FreepsPass()` that opens the Shadow phase (Sharku's Warg 5_59, Card_05_059_Tests); so is an event whose extra cost exerts such a minion: Under the Watching Eye 1_281's "exert a [sauron] tracker" read not playable until the trackers went down before that pass (Card_01_281_Tests); so is a card whose "To play, spot" needs him: Tower of Orthanc 3_71's "To play, spot an [isengard] minion" read not playable with the minion cheated in after that pass (Card_03_071_Tests). lint's `stale` warning (engine pass 39) names an offer read as absent after a cheat with no answer or refresh between (a skipper's passes are answers: a minion cheated in during the fellowship phase and then skipped past with `SkipToPhase` is counted in the next decision, Card_33_019_Tests); since engine pass 41 the five cheats above count as a refresh. Since engine pass 50 it also names a card's action taken (`FreepsPlayCard`, `FreepsUseCardAction` ...), or read as offered, after a cheat that moved cards (`Move...To...`, `AttachCardsTo`, `StackCardsOn`) with no answer or refresh between: after a skipper's draws the card may or may not have been dealt into the hand the decision was worked out from, and the test passes or fails by chance (Card_01_175_Tests and Card_04_346_Tests, `SkipToSite` then `MoveCardsToHand`, failed about one run in ten). Engine pass 50 put the refresh into the 333 landed tests of that shape.

**A number that can only be one number is not asked for** (F-0147). Where an effect or a cost asks for a number and only one can be named (X from 1 to 1, "how many to spot" with none to spot, "remove X tokens" with none there), the game takes it. Burdens that may not be removed count as none: "remove up to N burdens" as an effect under a bar on removing them (Black Breath 1_207 on the Ring-bearer) asks nothing and does nothing (F-0512, engine pass 45; mechanics/ring/ABarOnRemovingBurdensTests). A test of such a board does not answer the question, and an answer written "to be safe" goes to the NEXT decision (the symptom is a wrong number later, not an error). To read the bounds of an X, arrange a board where two numbers can be named. A choice of CARDS whose count comes to 0, as an effect, is not asked either, from a hand or a stack as from play (a forced `discardFromHand` of X, X = 0: The Dark Lord Advances 3_87, F-0560), and an `exert`, `wound` or `heal` effect of X times with X 0 chooses nobody (Q-0337, engine pass 53; mechanics/choices/AnEffectDoneNoTimesChoosesNobodyTests). As a COST such a choice is still asked, a selection of 0 to 0 over a hand that is not empty.

**A choice with nothing on any branch is not asked** (F-0116, F-0293). Where a `choice` comes up as an effect and no branch can be carried out in full, the player is asked to pick among all of them (a branch may still be done in part: Dwarrowdelf Chamber 1_344 with one companion to exert), unless every branch is a single effect that is certain to do nothing: nobody to exert, heal, wound or discard, no burden or threat to remove, no site to liberate. Then nothing is asked and nothing happens, and a test of that board answers no "Choose action to perform" (assert `assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"))`; mechanics/choices/ChoiceWithNothingOnAnyBranchTests). Since engine pass 26 (Q-0220: D) a branch that holds others says so too (mechanics/choices/ChoiceWithABranchThatHoldsOthersTests): an `if` by the side its check picks (a failed check with no `false` is nothing); a `costToEffect` by its cost alone, so one that cannot be paid for (or whose own `requires` fails) is nothing and one that can never is, whatever it buys (Arwen's Fate 1_93: Aragorn exerts for an Arwen who is not there); a play by whether a card can be played (a play from a draw deck only with `mustExist`); several effects by each in turn. The question is still kept by a branch with an `optional`, a `repeat` or a `preventable` in it, by one that first remembers something for its next effect (`memorize`, `memorizeActive`), and by one whose effect cannot tell. Since engine pass 32 a branch also answers with what it was taught for the rule of the next section (an empty pool to remove twilight from, no card in a hand to discard or place, no card to choose), and an assignment with nobody to assign answers (Duality 13_47 with no Shadow card in hand and an empty pool asks nothing); a discard or placing of a WHOLE hand still keeps the question (Q-0255).

**A forced discard from hand that a bar stops asks for no card (F-0514, engine pass 46).** Under Frodo, Courteous Halfling 4_301's bar (three unbound companions) or Frodo, Little Master 19_28's, a Shadow card's `discardFromHand` with `forced: true` offers no card to choose, whoever was to choose it (Tower of Barad-dur 3_104 and The Dark Lord Advances 3_87 the Free Peoples player, Alive and Unspoiled 1_120 and The Great Goblin 31_36 the Shadow player), and discards none: the game goes straight on, so a test answers nothing there and reads the next decision (mechanics/deck/ABarOnDiscardingFromHandTests). Alive and Unspoiled's "You may discard 1 revealed card" is not asked there either (its `optional` requires `canDiscardFromHand`, F-0515, engine pass 47). A `forced: false` discard ("may discard", Dunlending Rampager 5_1) is still asked. The random discard (Hand of Sauron 3_90) and the discards from the top of the deck (Desperate Measures 1_245, Uruk-hai Sword 1_160) asked nothing before. A forced discard from the top of a barred deck as a COST cannot be paid, so its action is not offered: Corsair Ruffian 10_39's "you may ... discard the top card of the Free Peoples player's draw deck to add 2 [raider] tokens" was offered under the bar and did nothing on a yes until engine pass 48, which has `discardTopCardsFromDeck` ask the bar of the deck's owner in its cost check, as its effect did (mechanics/deck/ABarOnDiscardingFromTheTopOfADrawDeckTests).

**"Wound each" and "wound every" wound one card at a time.** Each of the wounds has its own "is about to"
window (`<Wound X> - Optional "is about to" responses`), so a response that picks which card it saves ("prevent
a wound to an Elf") is offered one card at a time and never a choice among the cards the effect reaches: the F
check's invalid candidate "among them" cannot be shown against such an effect, which is a line in Notes. The
wounds of a LOST SKIRMISH are not such an effect: every loser is wounded together, in one window naming them all
(`Wound A, B, C - Optional "is about to" responses`), and a prevention that names a kind is then asked "Choose
characters to prevent effect to" among the matching losers only, so its `F` lays several losers, valid and
invalid, in one skirmish (Hides 4_19: two [dunland] Men, a Man of another culture and Saruman 4_33). Once one wound is prevented, the
same response is offered again in that window for the losers' wounds still to come, so a test reads that second
offer (and declines it) before it reads the wounds (They Feel The Precious 15_183, dec-b15_182); where no
loser can fail the filter ("your minion": every loser is the Shadow player's), two losers are still laid and the
choice read, as that is what tells `choose` from `all` (Saruman's Laboratory 6_120, Nan Curunír 5_119). That
board is for minions only: a skirmish has one Free Peoples character (CR 4.2 "assignment phase"), so a response
to a wound on one kind of FREE PEOPLES character ("an unbound Hobbit", Skinbark 6_35) meets each candidate
alone in its own window, and its `F` "among them" is a line in Notes (dec-b11_78 found no board). A bare race
word that a printed minion has is no such kind: "a Hobbit" reaches Ted Sandyman 18_75 (Q-0058), so its `F` lays
him beside a minion who is no Hobbit, both losing to one companion (Nice Imitation 2_106). Nobody
is asked the order: cards in play that an effect takes together are taken in the order of their card ids, the
same in every run (Q-0142, F-0268). A card gets a new id as it comes into play, so that is the
order they came into play: lay them down one at a time (`MoveMinionsToTable(a); MoveMinionsToTable(b);`) and a
test may say whose window comes first. It holds for cards a player chose together too ("wound 2 Orcs"), whichever
was named first, and one effect that takes several at once (exert every, heal, hinder, discard each) names them
in that order in the game log. `mechanics/actions/TakenTogetherInCardIdOrderTests` shows each.

**A stack from play asks where first, then what.** `StackCardsFromPlay` resolves its `where` ("Choose card to stack
on") before its `select` ("Choose cards to stack"), whatever the text's word order: "stack your [sauron] minion on a
site you control" asks the site, then the minion (Their Marching Companies 8_107, Card_08_107_Tests). Where the `where`
finds no card (no site he controls), the choice of what to stack is not asked and nothing is stacked, and the effect
says it will do nothing: an action with a cost is offered and flagged (F-0579, engine pass 61; Troll of Gorgoroth
8_108, mechanics/inplay/StackingTests). As a cost, a stack with nowhere to go is not offered at all.
`stackCardsFromDiscard` asks in the same order: the card to stack on as a card choice ("Choose card to stack on"),
then the card from the discard pile as an arbitrary-cards choice ("Choose card from discard"); with one place to
stack on the first is skipped, so read the order on a board of two (Vengeful Savage 17_62, Card_17_062_Tests).

**Choosing which skirmish to resolve is always asked.** Even when only one skirmish is left, the Free Peoples
player is asked to choose it. `FreepsAssignAndResolve` answers that for the skirmish it sets up;
after other skirmishes, call `FreepsResolveSkirmish(companion)`. Not where the minion it assigns has printed ambush: the
ambush offer comes up as the assignment is made and `FreepsAssignAndResolve` then fails choosing the skirmish. Call
`FreepsAssignToMinions`, answer the offer (`ShadowDeclineOptionalTrigger()`), then `FreepsResolveSkirmish` (Card_05_076_Tests; dec-b5_74).

**A skirmish against a lurker is not on offer while another waits** (CR 4.2 "lurker"). At "Choose next skirmish
to resolve" a companion with a lurker among his minions is no choice until every skirmish without one is resolved,
in the fierce round too: `FreepsResolveSkirmish(companion)` for him fails there. Resolve the others first, or lay
no lurker beside a skirmish whose order the test cares about (mechanics/keywords/LurkerTests).

**Who is offered for assignment.** `FreepsHasAssignmentTargetAvailable(card)` and its Shadow twin read the
assignment decision in front of the player. `FreepsCanAssign(card)` and `ShadowCanAssign(card)` read the game's
rule, whatever is being asked. A restriction on a pair ("may not assign a character to skirmish this minion",
`cantBeAssignedToSkirmishAgainst`) shows in neither: both cards stay listed, and the game refuses the pair only
when the assignment is answered, even with `minion: any` (a bearer no player may assign to any skirmish: Caught In A Sack
31_57, where `FreepsCanAssign(bearer)` reads true). `FreepsCanAssignAgainst(character, minions...)` and `ShadowCanAssignAgainst` read
that, for one minion or for several given in one answer (G-0029, G-0041); a refused assignment is never shown by
the rig throwing. A restriction on one card for one player
(`CantBeAssignedToSkirmish` with `player`: Bill Ferny 2_75 and 52_75, Q-0146) shows in the first two: the card is
off that player's list, and when it is the only minion the Free Peoples player is not asked to assign at all.
Defender is counted against the minions a character already has (F-0355, engine pass 24): for a defender +1
companion `FreepsCanAssignAgainst(aragorn, m1, m2)` is true and `(aragorn, m1, m2, m3)` false, and once he has two
`FreepsCanAssign(aragorn)` is false, while `ShadowCanAssignAgainst` has no such count (`mechanics/keywords/DefenderTests`).
Defender is the Free Peoples player's own when he assigns: an assignment a card makes, either player's, needs
both characters unassigned, and an action written with `assignableToSkirmishAgainst` is not offered for a
companion who has a minion, whatever his defender bonus.

**An assignment ability with a cost is offered with nobody to assign, and paid for nothing.** "Exert this minion
to assign it to a companion bearing an artifact" (Beastly Olog-hai 11_108, `assignFpCharacterToSkirmish` with
`fpCharacter: choose(...)` in `effect`) is offered with no such companion, or with the minion already assigned: the
exertion is paid, nobody is assigned and no prevention is asked (Q-0034; Lurtz 18_118 likewise). Read the wound,
`IsCharAssigned` false and the next decision. A companion already assigned is not among the choices.
CR 4.2 "assignment phase" ("You cannot do this unless both of them are unassigned", binding on Assignment:
abilities by Q-0362) bars the ASSIGNMENT, not the offer of an ability with a cost (Q-0034; F-0665): Southron
Wanderer 4_258 with the Ring-bearer or itself already assigned (dec-b4_256).

**An assignment a card makes another player make is his only where he had a choice** (Q-0251 with Q-0171;
F-0513, engine pass 45). "Make the Free Peoples player assign this minion to an unwounded companion" (Barbaric Uruk
12_135) with one unwounded companion is chosen for him, and the game log, "each time the Free Peoples player
assigns" and ambush all read it as the Shadow player's; with two to choose between it is his. What may be assigned
is still judged for him (`mechanics/skirmishes/AnAssignmentACardMakesAnotherPlayerMakeTests`).

**The moment after the assignments is the choice of the next skirmish.** When every minion is assigned the Free Peoples player is
asked "Choose next skirmish to resolve", and that is the bookend to assert after `FreepsAssignToMinions` or
`ShadowAssignToMinions`: `assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"))`.
`FreepsAwaitingSkirmishChoice()` reads the same moment by name, and `ShadowAwaitingSkirmishChoice()` on player 2's
turn (engine pass 24); `lint.py` takes either form as a test's closing reading, and as showing that the player is
being asked. With a
minion left unassigned the Shadow player is asked "Assign minions" first. After an assignment ACTION in the
assignment phase the bookend is the other player's `Awaiting...AssignmentPhaseActions()` (69_38). Where assignment actions
have already assigned every minion, nobody is asked "Assign minions": `FreepsDeclineAssignments()` there fails
(DecisionResultInvalidException from CardsSelectionDecision); pass the assignment actions and read
`FreepsAwaitingSkirmishChoice()` (Card_04_015_Tests, dec-b4_15).

**One minion to a companion, and the minions left over.** The Free Peoples player may assign one minion to each
companion (more takes defender): `FreepsAssignToMinions` with two minions in one group is refused by the game
with a bare `DecisionResultInvalidException`. His several pairs go in ONE call as well,
`FreepsAssignToMinions(new PhysicalCardImpl[]{c1, m1}, new PhysicalCardImpl[]{c2, m2})` (Card_11_122_Tests; the
generated stub lacks `import com.gempukku.lotro.game.PhysicalCardImpl;`, so add it): his
assignment is one answer, and a second call with one pair is refused with the same bare exception. A minion he leaves unassigned gives the Shadow player an
assignment decision of his own, whoever else is assigned, the Ring-bearer included, and that is how a companion
comes to face two: `ShadowAssignToMinions(companion, secondMinion)` after the Free Peoples player's assignment.
Several minions to one companion go in ONE call, `ShadowAssignToMinions(companion, m1, m2, m3)` (Card_04_019_Tests):
the companion repeated in the array form is refused, and a second call finds the Shadow player no longer asked. Leftovers to SEVERAL companions go in that one call too, by the array form with one group per companion,
`ShadowAssignToMinions(new PhysicalCardImpl[]{c1, m1}, new PhysicalCardImpl[]{c2, m2})` (Card_13_167_Tests; dec-b13_166).
`ShadowDeclineAssignments()` passes that decision; with no minion left over he is not asked. A minion played from a pile in
answer to his assignment (Ranks Without Number 4_170) is no leftover either: leftovers are made only as he finishes
(CR 4.2 "assignment phase"), so the Shadow player is not asked and the choice of skirmish comes next (Card_04_170_Tests). Left unanswered, it
makes the next Free Peoples step (`FreepsResolveSkirmish` and the like) fail with a bare message naming the Shadow
player's pending "Assign minions" decision (`SkipToPhase(Phase.REGROUP)` answers it by itself, so minions laid
after `StartGame()` and left unassigned stand in the regroup phase, a fierce one unwounded too: Card_05_024_Tests, Card_09_028_Tests; dec-b5_24; Smaug 30_66 on the Hobbit table, Card_30_066_Tests); an ambush minion he assigned answers first with its "Ambush - add N"
offer (below, "One `ShadowDeclineOptionalTrigger()`"; Card_11_071_Tests, Card_11_072_Tests; dec-b11_69). One the
Free Peoples player assigns offers its ambush to the Shadow player straight after `FreepsAssignToMinions`, ahead of
`ShadowDeclineAssignments()` (Horror of Harad 14_13, Card_12_085_Tests; dec-b12_82).
Walking several skirmishes, a Ring-bearer with the Ruling Ring (`SamRB` as well as Frodo) who loses one holds up the next
`FreepsResolveSkirmish` with its "Use The One Ring" offer: decline it first ("The Ruling Ring interrupts skirmishes"; dec-b13_146).
Defender shows in the Free Peoples player's own decision without any refusal: a companion who already has a
minion from an assignment action is on his list only if he has defender (`FreepsHasAssignmentTargetAvailable`;
`mechanics/keywords/DefenderTests`, with Unexpected Visitor 7_126 and Reckless Pride 7_13 as the assignment actions).
A defender assigned to two minions in one answer is two assignments to an `assignedAgainst` trigger, one against
each minion (Q-0323: B, engine pass 49; Weary 4_212 exerts twice), and one to `assignedToSkirmish` (Wold of Rohan 4_336).
An `assignedToSkirmish` trigger with no `player` answers an assignment made by EITHER player, the Shadow player's of a leftover minion included: Alfrid 32_25's "Each time a [esgaroth] ally is assigned to a skirmish" exerts once for the Free Peoples player's assignment and again for the Shadow player's (Card_32_025_Tests; hobb-b32_18).
Two minions on one companion are often double his strength: he is then killed, not wounded ("A companion beaten by
double his strength", below; dec-b7_35).

**A minion the Free Peoples player must pay to assign is paid for ahead of his assigning.** "The Free Peoples
player must exert a companion to assign this minion to a skirmish" (`extraCostToAssign`: Morgul Regiment 7_197,
Morgul Spawn 7_200, Morgul Spearman 7_201, Ulaire Nertea 69_38; Q-0221, and Q-0283, engine pass 30). As he starts
his own assigning, after `SkipToAssignments()`, each such minion whose cost can be paid in full, and that he
could then assign, is offered to him as an optional trigger on the minion ("Optional responses":
`FreepsHasOptionalTriggerAvailable(minion)`, `FreepsChooseOptionalTrigger(minion)`, `FreepsDeclineOptionalTrigger()`):
the choice of who exerts is asked as he takes it (a lone candidate exerts unasked), and the others are offered
again, judged afresh, until he passes. Only then is "Assign minions" asked. A minion paid for is his to assign;
one not paid for is not: charged against every character it is off his list (`FreepsHasAssignmentTargetAvailable`,
`FreepsCanAssign` false), and charged against some (69_38) it stays on it with `FreepsCanAssignAgainst(character,
minion)` false for those. Nothing is paid after his answer, and a payment he does not use is lost. A cost not
paid in full (a burden prevented) leaves the minion unpaid and not offered again. A card that has him assign
(Éowyn 17_96, The End Comes 1_169, Shadowy Mount 12_171), which comes before his own assigning, charges him once
the minion is chosen and cannot take a minion he could not pay for. Where the charged minion is the card's only
candidate it is taken unasked, and the first thing asked is who exerts: the choice then reads the companions, not
the minions (Alcarin 5_31). Before his assigning (in the assignment phase's actions) `FreepsCanAssign` reads a
card's rule: true while the cost can be paid. The Shadow player's own assignments are never charged
(`mechanics/skirmishes/CostOfAssigningTests`). A cost that is repeated (Morgul Regiment 7_197, an exertion "for each Nazgûl
you can spot") can be paid only if every repetition can (F-0425, engine pass 28): with fewer exertions left among
the companions than Nazgûl the Regiment is not offered and nobody exerts.

**`IsCharAssigned` and `IsCharAssignedAgainst` read only the skirmishes still waiting.** The game takes an assignment off its list as that
skirmish begins, so `IsCharAssigned(card)` and `IsCharAssignedAgainst(card, opponent)` (the same list) are false for the characters of the skirmish being fought
(`IsCharSkirmishing` is true of them), while the definition word `assignedToSkirmish` counts them until the
skirmish phase ends, which is after its win and lose triggers have been carried out (CR 4.2 "skirmish
phase(s)"). Each skirmish is a skirmish phase of its own ("At this point, characters who were involved in that
skirmish are no longer assigned"), so a lurker's count of unassigned companions rises as each earlier skirmish
ends (Emboldened Orc 11_119: 12 in the first skirmish, 13 in its own). A test of "assigned to a skirmish" at a win or lose trigger rests on that rule and reads the
card's effect, not `IsCharAssigned` (Lorien Protector 63_20, with two Orkish Skirmishers 18_90). Once his own
skirmish is over the word counts him no longer, in the same turn: Counts But One 8_4 ("If Gimli is not assigned to
a skirmish") is not offered in a skirmish fought before Gimli's and is offered in one fought after it, which is
the control for the first reading (Card_08_004_Tests, three skirmishes on one table).

**A check on a player who is not being asked anything answers false.** The availability and card-choice checks
return false, `FreepsGetAvailableActions()` returns an empty list, and the raw readers of a decision's
parameters return null, when that player has no pending decision. The methods that *answer* a decision still
fail, as they should, the plain pass methods apart (see "Answers that are refused" below), and since G-0030 they
say why: "Shadow Player was to answer "45 47", but is not being asked anything.  Free Peoples Player is being
asked "Play Fellowship action or Pass", offering [...]", or "<card> is not offering <player> a play" with what is
on offer. So do `FreepsGetChoiceMin()`, `FreepsGetChoiceMax()` and `FreepsGetSelectableCount()` on a decision
that has no such thing. A refused answer leaves whatever was pending still pending. Read the message before
anything else: it names the decision the test is really at.
The `...NotAvailable` twins answer TRUE there, and at any decision that is no choice of cards:
`FreepsHasCardChoiceNotAvailable(frodo)` read at "Play Fellowship action or Pass" passes whatever the card does
(94_7's old `NotOfferedToNonOwner`). Read beside it what the player IS being asked and the thing left undone.
The commonest shape: `FreepsActionAvailable(...)` or `FreepsAnyActionsAvailable()` read as false straight after the
Free Peoples player's own action in a phase both players act in. The Shadow player is the one asked there, so it
shows nothing: `ShadowPass()` first, then read it beside `AwaitingFreeps...PhaseActions()`
(94_13's old lock-out assertions; 94_14's old reading of the Free Peoples player in the Shadow phase was dropped).
lint warns `not-asked` where an offer is read as absent (or the `...NotAvailable` twin as true) and nothing between
the test's last play, answer or skip and its next shows that player being asked: his own `Awaiting...PhaseActions()`,
a decision of his by its words, another offer to him read as made, or his answer coming next (a pass shows nothing).
A response EVENT held by a player who is asked nothing at that moment (Wariness 1_161 against a Stealth CONDITION
played in the fellowship phase: no "Optional responses" comes up) has its `-` read at the next decision the game
stops at, the other player's (`AwaitingFellowshipPhaseActions()`), with the event still in hand: a wrong offer
would stand in front of it (Card_01_161_Tests; Orc Ambusher 1_261 for a response ability).
So with a Response ability whose cost nothing can pay (Rare Good Ballast 5_114 with no Sméagol or Gollum in play):
no response window opens, `FreepsHasOptionalTriggerAvailable(card)` is false whatever the definition does, and the `C-`
reads `FreepsAnyDecisionsAvailable()` false beside `ShadowAnyDecisionsAvailable()` true (Card_05_114_Tests, dec-b5_114).
A Shadow OPTIONAL TRIGGER at the start of a phase the Free Peoples player opens is read as not offered by
`AwaitingFreeps...PhaseActions()` at that phase: start-of-phase triggers are asked before anyone's phase actions, so a
pending one would hold the decision (Half-troll of Far Harad 14_12, beside its offered board where the Shadow player is asked).

**A number with no upper bound.** `FreepsGetChoiceMax()` reads `Integer.MAX_VALUE` where a player is choosing a
number and the game sets no maximum (a `chooseANumber` with no `to`: Gathering Wind 10_16, Beneath the Mountains
52_1). That is the reading for "no cap" (G-0027).

**A question for a number may come filled in.** `FreepsGetChoiceDefault()` / `ShadowGetChoiceDefault()` read the
number the game fills in before the player answers (what the client shows in the box), or null where it fills
none in (G-0057). "How many to spot" comes filled in with all that can be spotted, a `chooseANumber` with its
`default` (0 where none is written), "Choose how many threats to add" with the `default` of its `addThreats` and
otherwise with nothing. It is a suggestion and binds nobody: read it only where the text or a ruling says what it
should be ("threats up to the number of burdens you can spot" comes filled in with the most, held to what can
still be added: Shelob, Menace 18_34, F-0419), and answer another number in the same test.

**Answers that are refused when they do not fit what is asked.** `FreepsResolveRuleFirst()` and
`FreepsResolveActionOrder(text)` fail the test unless the Free Peoples player is being asked to order required
responses (`FreepsAwaitingRequiredResponseOrder()` reads that, for both of the game's wordings: "Required
responses" after an event, and '<effect> - Required "is about to" responses' when two required triggers answer
something about to happen, as two copies of one card do), so a required trigger that has wrongly become
optional is noticed. `FreepsResolveRuleFirst()` also fails when no rule is among the responses to be ordered
(two triggers of cards: name one with `FreepsResolveActionOrder`). There are no Shadow twins (G-0005): two
copies of one Shadow card firing the same required trigger at once are not ordered by anyone, and their own
questions simply follow one after the other (two Vengeful Wild Men 17_63 at the start of the maneuver phase,
Card_17_063_Tests). Nor is the order of one card's required trigger fired twice at once fixed: A Defiled Charge 17_67's
choice, one per mounted Orc assigned in one answer, asked of Gimli first in one run and of Aragorn first in the
next, so a test that answers by companion name fails at random (Card_17_067_Tests answers both alike). `FreepsDismissRevealedCards()` and
`ShadowDismissRevealedCards()` fail unless that player is being shown cards; `DismissRevealedCards()` dismisses
for whoever is being shown some and fails if nobody is. `FreepsGetTotalRevealedCardCount()` and its Shadow twin
count the cards a player is being shown, 0 if none. They count the cards of ANY decision that lists cards out
of play, a real choice from a discard pile or a draw deck too (2 at Gandalf 17_17's choice from his discard
pile), so a 0 read there is no "nothing was shown": read it before the choice, or at the next thing asked.
`FreepsChooseOptionalTrigger(card)` fails unless that very
card is offering, and with `(card, text)` unless it is offering an action so worded. The plain pass methods are
not like this: a pass with nothing pending does nothing and says nothing (Ketura, on G-0015: passes stay
harmless), and `FreepsPass()` and `ShadowPass()` answer whatever is pending with nothing, an optional trigger
included. Do not write one "to be safe". A card choice is refused like the rest since G-0032:
`FreepsChooseCard(card)`, `FreepsChooseCards`, `ChooseCards`, `...ChooseCardBPFromSelection`,
`...ChooseCardIDFromSelection` and their Shadow twins fail unless that player is being asked to choose cards,
with 'Free Peoples Player was to choose <card>, but the decision in front of that player is not a choice of
cards.  Free Peoples Player is being asked "Play Fellowship action or Pass", offering [...]'. That is where a
test is when the game chose the one candidate by itself: delete the line (it used to pass the phase in silence,
decline a trigger, or give the card's id as a number). `FreepsDeclineChoosing()` and its twin are checked the
same way since G-0034: they fail unless that player is being asked to choose cards and may choose none ('... was
to decline choosing, but the decision in front of that player is not a choice of cards', or 'but at least 1 card
must be chosen there'). A pass is `FreepsPass()`, a trigger declined `FreepsDeclineOptionalTrigger()`. (The older, wordier `FreepsPassCurrentPhaseAction()` and
`ShadowPassCurrentPhaseAction()` were removed from the rig on 2026-10-05 at Ketura's word; old tests you read in
git history may still show them.)

**`FreepsChoose(text)` takes no action: at a choice of actions it fails.** `FreepsChoose(text)` and
`ShadowChoose(text)` answer a multiple choice if one is in front of the player, and give a number or a raw answer
where one is asked. With no multiple choice pending and actions on offer they fail, saying what is asked, and
answer nothing (engine pass 24). They used to go on to `FreepsChooseAction(text)` and take whichever action had
the words in its wording, in silence, so an answer written for a choice the game never asks (an "X or Y" with
nothing on either branch, F-0116) did something else: 92_35's old test answered `FreepsChoose("heal")` after an
Athelas with nobody left to heal, and discarded a card from hand to heal with instead. An action is taken by its
card, or by its words with `FreepsChooseAction(text)`. Still read that the choice is pending before answering it
by text (`FreepsDecisionAvailable("Choose action to perform")`): with no action on offer the answer goes to
whatever is asked.

**A loop of both players' actions is put to them at its third round** (CR 4.2 "loops"; Q-0329, engine pass 51).
Where the two players take the same two actions in turn (the same ability of the same card, each his own) in a
skirmish or in the maneuver, archery, assignment or regroup actions, with no pass between, and the board at the
start of the third round is the board at the start of the second, the player who opened the round is not offered
his actions: the Free Peoples player is asked how many more times the loop runs (`FreepsNameLoopIterations`), and
the Shadow player agrees or lowers it (`ShadowAgreeToLoop`, `ShadowLowerLoop`). A round that spends anything (a
wound, a card, twilight, a token) never matches, and an ability with a limit is never part of a loop. The loop is
then run inside the answer that settles it, before anybody is asked anything: N rounds if the Shadow player agreed
(he takes the last action) or M if he lowered (the Free Peoples player takes it), with the opener's action once
more at the end where the rounds end on the wrong player; a choice inside the round is made as it was in the
second round played by hand. The next decision is the other player's actions, and each looping ability (that card's,
not its other abilities nor another copy's) is still offered, flagged, to the end of the phase or skirmish: read it
with `FreepsActionLoopBanned(card)`; `FreepsActionAvailable` reads it as offered, and taking it is refused.
The prompts are HTML (Q-0329, engine pass 52): a text read with `FreepsDecisionAvailable` names each card as the
game log's card hint, `<div class='cardHint' value='5_29'>·Sméagol, Slinker</div>`, and the number as `<b>6</b>`.
`mechanics/actions/VoluntaryLoopTests`, `FiniteRepeatsAreNoLoopTests`. A round whose other half is the other
player's response INSIDE the first action and then his pass (Hrethel 6_95 against a Warg 5_65; F-0565, engine pass 57)
is found and put the same way; its run replays the response and counts the pass, a run ending on the pass leaves the
opener to act with that pass made (his pass ends the window), and only the opener's ability is barred: the response
stays on offer to anything else.

**A loop of one player's actions is put to him at its fourth use** (CR 4.2 "loops"; Ketura, answers/2026-10-16-c.md;
engine pass 53). In the fellowship phase or a Shadow phase, where one ability (or two abilities, one after the
other) has been used three times running with the board the same at the start of each round and after the third
(the order of the draw deck left out: a look through it shuffles), he is not offered his actions a fourth time: he
is asked "LOOP DETECTED: Choose a number of times to repeat this action." (`OnePlayerLoopTracker.PROMPT`, 0 to 99,
filled in with 3; answer it with `FreepsChoose("5")`). The round is run that many times inside the answer, the last
round shown, and each ability of the round is barred to the end of the phase (`FreepsActionLoopBanned`). A file that
uses a cost-free ability four times or more in one phase on an unchanged board meets the prompt (Sam 1_310, The
Bridge of Khazad-dûm 1_349 with no Balrog). `mechanics/actions/OnePlayerLoopTests`.

**`assignFPCharacterToSkirmish` with a choice on each side asks for the character first, then the minion**,
whatever order the text names them in: "assign a [gundabad] or [wraith] minion to a companion" (Sauron 32_45) is
answered `ShadowChooseCard(companion)` and then `ShadowChooseCard(minion)`, and the prevention after that
(Card_32_045_Tests; hobb-b32_44).

**A "Spot X doubts (burdens)" built as `chooseHowManyBurdensToSpot` asks for X before the effect looks for a
target.** An event whose target finds nothing (F-0665) still puts the number to its player: answer it
(`ShadowChoose("2")`) before reading the event in the discard pile (Dissension 32_50,
WithNoDwarvenCompanionDissensionIsPlayedForNothing; hobb-b32_44).

## What is offered, and what an offer does

**Costs gate an action; effects are only attempted.** An action is offered when its requirements are met and its
costs can be paid, whether or not its effect can do anything: Sam's "exert Sam to remove a burden" is offered with
no burdens to remove. To test that an effect finds nothing to do, use the action, check the cost was paid, and
check the game is back at the phase's actions. An event is the same: with nothing for its effect to act on it
is still offered, and playing it pays for it and discards it. An ability at its limit goes by the kind of
limit (Q-0093). A limit on uses ("limit once per turn") withholds it, cost or no cost. A limit on what its effect
gives ("(limit +3)", "add a threat (limit 1)") leaves an ability with a cost on offer (Q-0041): the cost is paid
and nothing else happens; with no cost it is not offered at that limit either. A "spot" written as the effect's own `choose(...)` is
therefore no requirement: an event so built is played with nothing to spot (Honed 8_6, F-0573); "Spot X to" wants a
`spot` cost (or a requirement).

**A requirement beside a cost can hide the cost's own filters.** Where the definition also has a requirement (a
`canSpot` count of 2), a board that leaves the cost unpayable may fail the requirement first, and shows nothing of
the cost's filter: Elessar's Edict 8_33's `C-` passed over both of its cost's [gondor] filters. Give each branch
of the cost a board the requirement lets through (a [sauron] Wraith minion, an exhausted [gondor] Man; dec-b8_28).

**An event's random discard from hand does not count the event itself** (F-0498, We Must Have It 5_30; engine
pass 39). `discardCardAtRandomFromHand` judges its cost against the hand without the card being played, as a chosen
`discardFromHand` leaves it out of its choice (CR 4.2 "playing a card": "You cannot discard The Card to pay a cost"):
with the event alone in hand it is not offered, and no definition needs a `hasCardInHand` `not(self)` for it (5_30's
is gone). `mechanics/costs/TheCardIsNotDiscardedToPayItsOwnCostTests`.

**A character at a "takes no more than N wounds" cap is not offered when a later wound is chosen.** Wounds cannot
be assigned to a character who cannot take them (CR 4.2 "wound"), so once the cap is reached he is left out of the
choice: a test that chooses him fails on a refused answer. Read him absent (`ShadowHasCardChoiceNotAvailable`) and
choose another (Pippin's Armor 7_113 under two Orkish Skirmishers 18_90, Card_07_113_Tests; Armor 1_92's family).

**An offer that costs nothing and would do nothing is not made** (Q-0034, Q-0224, Q-0231; engine pass 26). The
game withholds an optional trigger, a special ability or Response of a card in play, and an ability one card
gives another, where nothing is paid for it (a spot is no cost) and every one of its effects says it will do
nothing: nobody to heal, no token to reinforce, nobody a minion could be assigned to, a hand that card may not
discard from, an empty twilight pool to "remove (1)" from, no card in hand to stack, discard or place on a deck,
no such card in a discard pile to place beneath a deck, a condition "transferred from your support area" that
has left it, the fellowship's next site where none can be played (on a numbered path, the player's own site of
that number lies there already; engine pass 36, F-0496). Such a card has no "Optional responses" to decline and no action to find: a helper's "when you
play this, you may ..." is simply not asked on a board where it has nothing to do (Brego 4_263 on an unwounded
bearer, Bill the Pony 13_143 with no [shire] token), so `FreepsDeclineOptionalTrigger()` there fails with nobody
being asked. Read it with `assertFalse(scn.FreepsHasOptionalTriggerAvailable(card))` and what the game is
waiting for. An action with a cost, an event, a play, an optional draw from an empty deck and a "you may" asked
INSIDE another action are offered as before. Nor is a count from the board that comes to zero ("you may add (1)
for each site you control"): with no requirement on the definition it is offered at zero, so a test of ANOTHER
clause of that card lays the count above 0 and answers the offer, or it stops there (Isengard Scimitar 5_54's
bearer test, before F-0700). Which kinds of effect can say they will do nothing, and which
cannot: card-test-campaign `shapes.md`, "An optional trigger or ability that costs nothing";
`mechanics/actions/AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests`. `GEMP_IDLE_OFFERS=off` in the environment
of a `cardtest.py run` or `probe` turns the rule off for that run.

**A choice between two things to do offers what can be done in full** (F-0310; CR 4.2 "effect"). Of "<do X> or
<do Y>" as an effect the game offers the branches that can be performed in full, and carries out a lone one with
no question (its decision text is "Choose action to perform": read a lone branch carried out unasked as
`assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"))`, Card_17_049_Tests). Where none can be but something can be done, every branch is offered and as much of the one chosen
as can be done is done, a branch that can do nothing at all included (Make Haste 10_109, a pool of 1 and no
minion: both offered, "remove (2)" takes the 1; Nine-fingered Frodo 10_112, no burden and 1 threat); so too "remove 2 tokens from a machine" with the only machine holding 1 (Armory 5_79): `removeCultureTokens` as an effect chooses among cards holding the full count while there are any, and otherwise among any holding a token, which gives up what it has; a cost still wants the full count (F-0702, engine pass 72; mechanics/inplay/RemovingMoreTokensThanACardHoldsTests); where nothing can be done on any, nothing is asked (F-0116). A plain "remove (N)" twilight effect after a cost, with no choice round it, takes what the pool has: Shadow of the Wood 15_35's "Place this companion in the dead pile to remove (5)" with 3 in the pool leaves 0 (Card_15_035_Tests). Here Lies Balin 1_19: no
Orc, nothing asked; one Orc of 1 vitality, both offered; two such Orcs, "wound 2 Orcs" unasked; one Orc of 3
vitality, "wound 1 Orc twice" unasked (mechanics/choices/ChoiceOfWhatCanBeDoneInFullTests). A choice's `memorize` holds
the text of the branch taken, asked or not. A `discard` of `all(<filter>)` that matches nothing counts as done in full and
stays on offer beside a branch that can act (Guarded 7_101 before F-0589: "Discard all roaming minions" taken with
none in play discarded nothing and spent the condition). An `assignFPCharacterToSkirmish` branch is doable in full
while some character it names can be assigned against some minion it names: the pair, so an assigned minion does
not count (Pull of the Ring 18_133; F-0624, before which it was never doable; mechanics/choices/
ChoiceWithAnAssignmentBranchTests). One whose character or minion reads `memory(...)` is asked part by part, as before. A branch that is a
`repeat` (or holds an `optional` or `preventable`) cannot say it will do nothing, so it stays on offer beside a
branch that can act: a finding, mended by an `If` round the choice (Dwarven Stratagem 17_3, F-0626; shapes.md, F-0116).

**A "wound X twice" branch of a choice is not offered to a character with 1 vitality left.** In "add 2 burdens or
wound him twice" (Boromir 59_31) only the first of the two wounds could be placed, so the game carries out the
other branch and asks nothing (CR 4.2 "effect": a choice must be one the player is fully capable of performing;
Ketura's ruling on F-0310, in the entry above). With 2 vitality left the branch is offered and kills him,
which is also what tells a wound from an exertion (F-0148). Where X is chosen and the branch is open because
some X can take both wounds, an X with 1 vitality left is not offered for it either: whom to wound is an
extension of the choice (Q-0209: B; Stand Against Darkness 51_63, a [sauron] minion of 1 vitality beside ones of
2 and 3), and a lone X able to take both takes them with no question. Where no branch can be done in full and
both are offered all the same, any X may be picked and takes the wound it can. A "wound X twice" that is no
branch of a choice offers every X able to take a wound, one with 1 vitality left among them, as it always
did (mechanics/choices/ChoiceOfWhatCanBeDoneInFullTests has all three). "Exert a minion twice" as an EFFECT offers only the minions
able to exert twice (F-0131); with none able to, the game does as much as it can (CR 4.2 "effect"), and a lone
minion able to exert at all exerts once, unasked (Card_07_014_Tests, Slaked Thirsts 7_14; dec-b7_12).

**An empty draw deck closes a "draw a card" branch of a choice.** In "choose one: draw a card; or take a [gandalf] skirmish
event from your discard pile into hand" (Librarian 18_22) the game takes the event unasked when the draw deck is empty: the
draw cannot be done in full, so it is no branch (CR 4.2 "effect"). This is a branch of a choice, not an optional
draw on its own, which Q-0029 keeps on offer with an empty deck (Card_18_022_Tests; dec-b18_20).

**A play inside an `If` does not gate its action.** The engine refuses an action it can see will play nothing
only when the play is the action's top-level effect. Wrapped in an `If`, the action is always offered and does
nothing when used (F-0036, Out of Dark Waters). A definition written that way is a finding, not a quirk to test
round. Its correction is a requirement of the definition's own for the play: on an event, `Playable` with
`fromZone: DISCARD`, which counts the event's own cost as gone from the pool (Little Snuffler 13_52, F-0058: not
offered in region 3 with the pool one short of event and minion together, or with no [gollum] minion in the
discard pile; `mechanics/costs/EventThatPlaysFromADiscardPileNeedsBothCostsTests`).
A play inside a `repeat` IS gated: Our Inspiration 15_131 (a `repeat` over `forEachYouCanSpot` of a
`playCardFromDiscard`, no requirement) is not offered with no possession in the discard pile that can be played,
and is offered where one of two counted plays can be made (Card_15_131_Tests); write no requirement for it.
A branch of a `choice` is another matter: an `If` there whose check fails makes the branch "not doable in full", so
it is not offered and the other branch is carried out unasked (Gandalf 13_33, "unless you spot 2 other companions
and discard 2 [gandalf] cards": Card_13_033_Tests, one other companion; shapes.md, F-0310).

**The no-effect flag.** Every action a player is offered carries a word saying whether the game expects it to do
nothing. `FreepsActionNoEffect(card)` and `ShadowActionNoEffect(card)`, and the same with words of the action's
text for a card that offers several (`ShadowActionNoEffect(easterling, "fierce")`), read it for that card's
action in the player's pending choice of actions: `"false"`, or why the game expects it to do nothing:
`"effect"`, `"limit"` or `"skirmish"`. They fail the test if the player is not being offered a choice of actions,
if the card offers no such action, or if more than one fits. A flagged action is offered and may be taken like
any other: read the flag, then take the action. An optional trigger is read the same way while its offer is
pending (`FreepsActionNoEffect(card)`, then `FreepsChooseOptionalTrigger(card)`). An ability or optional trigger
with a cost whose limit is on what its effect gives (`effectLimitPerPhase` / `effectLimitPerTurn` in the
definition) reads `"limit"` once it has given as much as the limit allows. A cap written inside the effect
reads `"limit"` once spent only where it is on a strength bonus, a keyword's amount or a number of cards drawn
and the action does nothing else; a cap on anything else reads `"false"` (Sting 1_313, 51_313). One with a limit on uses is not offered at its limit, so there is no flag to read. An assignment a card makes with nobody it could assign reads `"effect"` (Lurtz 18_118 with no unbound companion; engine pass 32). An event, or an action with a cost, whose effects are a
reveal from a draw deck and what is done with the cards revealed reads `"effect"` while that deck is empty
(Q-0163; `mechanics/deck/RevealFromAnEmptyDrawDeckTests`; shapes, "A reveal that a cost buys"). The flag is worked out when the decision is sent, so after cheating the board get a new decision
before reading it (`FreepsRefreshDecision()`). At site 2 of `FellowshipSites` (Ettenmoors) each player
is also offered the site's own skirmish action in every skirmish. "Take off The One Ring" says that it will
do nothing while the Ring is not on: an ability with nothing else it can do then reads `"effect"`, and `"false"`
once the Ring is on (O Elbereth! Gilthoniel! 2_108; F-0370, engine pass 25; two tests of `mechanics/actions/NoEffectFlagTests`). Which cards assert the flag: test-generation,
"Rulings, interactions and formats".

**An action's own exertions count against what it then needs.** "Exert X twice to play ... from your discard
pile" is not offered when X's exertions would leave the only candidate unplayable, and "exert A and B" is not
offered when one character with one exertion left is the only A and the only B
(Grimir 51_17's own tests; `mechanics/costs/CostsAreJudgedInTheOrderPrintedTests`). That is so where the cost names
its card or only one character can pay; where the player chooses among several, the action is offered on the
board as it stands (no printed card of that shape plays an event, so that has no test). An assignment that is
part of the price is judged the same way (F-0486, engine pass 28): "Exert Éowyn and assign a minion to the
Ring-bearer to ..." (17_96) is not offered where her own exertion is the last any companion has and the one
minion left for the Ring-bearer is one he must exert a companion to assign.

**A prevention is asked only when its effect will happen** (Q-0114;
`mechanics/actions/PreventionIsAskedOnlyWhenItsEffectWillHappenTests`). "X may pay to prevent this" is not put to X
when the effect cannot happen: a draw or a reveal from an empty deck, the fifth draw of a fellowship phase, nobody chosen
for the effect or the one chosen out of reach (a companion who cannot exert), threats at their limit, no site
to take, a skirmish that will not be cancelled, a culture token added or reinforced under Redhorn Pass 13_194
(F-0612, engine pass 66; Card_11_217_Tests). An answer written for the question on such a board
(`ShadowChooseNo()`) fails or goes to the next decision. Read it as
`assertFalse(scn.ShadowDecisionAvailable("prevent"))` with the cost unpaid: in the phases both players act in,
`ShadowAnyDecisionsAvailable()` is true anyway, because the Shadow player is being asked for his own action.
`ShadowDecisionAvailable(text)` reads the decision's own wording: where a prevention is put as a choice of
options the word may be in an option only, and the reading is `ShadowChoiceAvailable("<the option's words>")`,
or the assertion holds whatever the game does.
An effect that will happen in part (draw 3 with one card left) is still asked. For a rider "X may Y to
prevent this", the board on which the effect cannot happen is a `-` worth one test where the card can reach it.
A token REMOVED under Redhorn Pass would still be asked (`RemoveCultureTokens` says nothing of the bar); no card
has such a prevention.

**A card that cannot be transferred is not offered to a `Transfer` effect.** A possession borne by Warrior of
Rohan 17_104 (`CantBeTransferred`) is never among the cards a card's `Transfer` takes or offers, with
`checkTarget` or without (F-0340, engine pass 24); a possession may still be transferred TO him. With nothing
else to take, an ability with a cost is offered all the same (Q-0034): the cost is paid, nobody is asked for a
card or a bearer, and the flag reads `"false"`. Strange-looking Men 11_100 are Men: they are offered as bearers
of an Armor themselves (Q-0058). `mechanics/inplay/CardThatCannotBeTransferredTests`. A follower's Aid asks the
same (F-0363, engine pass 26): see "A follower's Aid" below.
It is the same where the card can be transferred and no character matches the bearer after "to": Spider Poison
8_28 in a skirmish of Gollum's, not Shelob's, is offered, its threat or burden paid, and nobody is asked for a
bearer (`RemovingAThreatTransfersItToTheCompanionSkirmishingShelobAndToNobodyInAnotherSkirmish`).
"Another eligible bearer" turns away a character who already bears an item of the same class: under Surrendered
Weapons 18_104 a Man bearing an Armor (or a Heavy Chain) is not offered a Heavy Chain 4_278. That is the item-class
rule, not a side restriction; such a bearer is the invalid candidate to lay beside the eligible ones (Card_18_104_Tests).
A transfer that is a trigger's COST (Primitive Brand 17_47: "you may transfer this possession to another [men] minion.
If you do, stack a [men] minion from play") wants another bearer at every win: stacking the old bearer leaves none, so
a board of two wins keeps a third such minion out of the skirmishes (dec-b17_44).

**A response event is not played by another card's effect.** "Play a(n) ... event from your discard pile /
stacked here / from hand" neither offers a response event nor counts one towards being usable; an event that
is a response and a phase event (Eye of Barad-dur 5_96) is offered in its phase only, which no test reads:
no printed card plays a [sauron] event in a skirmish phase (engine pass 27). For "a [culture] event" a response
event of that culture is the invalid candidate beside the phase events (`Card_V2_052_Tests`: Now for Ruin
102_52, whose filter names no phase, beside Unyielding 10_74, so the refusal read is the game's own), and a
phase word in the definition's filter beside it cannot be told from its absence: say so in Notes. Race Text 92_29's booster pack is in `mechanics/actions/ResponseEventsAreNotPlayedByEffectsTests`.
Nor is an event of another phase played that way (CR 4.2 "event"): "play a [shire] tale" from hand in the
regroup phase does not offer a Skirmish event that is a [shire] tale (Nine-fingered Frodo 10_112), and with that
alone in hand the ability is not offered (Bilbo 59_49).

**A response is offered once per use.** After a response cancels an ability or an event, no response to it is
offered again (Q-0043), so a pass or decline written to answer a second offer answers the next decision instead.
Optional triggers are still offered. A Response special ability with a cost is the other way about: once used,
it is offered again for the same play for as long as its cost can still be paid (CR 4.2 "response": "You may
respond more than once to the same situation"; Q-0034), even after its own effect has discarded the card it
answered (Iorlas 7_107: re-offered with the engine gone while a [gondor] token is left; Card_07_107_Tests). A second use gains nothing after a cancel, but an effect
that can be had twice is had twice: Uruk Bloodlust 1_144's re-offer, one for each winner it memorized, heals a
second wound on the same winner (dec-b1_141). The offers are collected again after each use, so a mistake
in what such a trigger reads of its winner (`not(fierce)` on Uruk-hai Rampage 1_159) is noticed by a two-winner
test through the ORDER of the offers, not the board it is about: that board is the same winner, already holding
what the mistake reads, offered again with the cost payable (Card_01_159_Tests, dec-b1_159). That second "Optional
responses" decision stands in front of whatever comes next: decline it, or the played event stays in the void
and a reading written for after the play reads VOID (Shrouded Elf 63_23). Not so after a PREVENTION: a Response
that prevents what it answers ("... is about to ..., exert X to prevent this") leaves nothing to answer, and the
game goes straight on though the cost could be paid again (Ceorl 4_264 against a site taken, Sam 4_316 against a
burden: the regroup actions, "Choose next skirmish to resolve"). A decline written there answers the wrong decision.
"Nothing to answer" is in that window only: where it still holds another wound the Response may prevent (the losers
of one skirmish, wounded together), it is offered again for that one, and not once none is left; a damage +1
winner's second wound is a window of its own (His Terrible Servants 2_77, Card_02_077_Tests).
Once the cost can no longer be paid, the next wound of the same effect is placed at once, inside the rig call that
answered the last offer: a reading meant for "between the two wounds" already shows the second (Lurtz 19_33 under
Defend It and Hope 4_115, Card_19_033_Tests). Where the cost cannot be paid at all, the Response is never offered
and the wound (a kill, if it is the last) is placed inside the call that resolved the skirmish: read the zones after
`PassSkirmishActions()` (Éowyn 4_271 with one card in hand, Card_04_271_Tests, dec-b4_269).
A Response to "is about to be overwhelmed" that only adds strength (Footman's Armor 7_93) is offered again while
its bearer is still doubled (CR 4.2, the ruling on the card) and not once he is not, though the cost could be
paid: the skirmish then resolves at once and the bonus ends with it, so the bonus itself is read only between two
offers, on a board where one use is not enough (`Card_07_093_Tests`). An optional TRIGGER there goes the same way
(engine pass 56, F-0566): an optional trigger is otherwise offered once in an "is about to" window, but in the
skirmish's resolution window a change of either side's strength is a return to the start of step 3 (3A), and the
triggers used before it are offered again (Fishing Boat 13_48, `AnAboutToBeOverwhelmedTriggerAtEachReturnToStepThreeTests`).
A response EVENT with another copy in hand goes the same way: once one copy is played, "Optional responses" comes
back with the other copy for the same death, and `ShadowDeclineOptionalTrigger()` answers it before the next
skirmish can be chosen (Beyond Dark Mountains 4_141; probed at the landing of dec-b4_78). A Response that ASSIGNS goes the same way: after
Mordor Assassin 7_284's "remove a threat to assign this minion to the Ring-bearer", while a threat is left and a
[sauron] Orc is still in the skirmish, a second "Optional responses" stands in front of the threat wounds and is
declined (Card_07_284_Tests).

**Two at once, one use.** A required trigger with a limit on uses fires once when one event raises it twice (two companions exerting together): no ordering question is asked for the second.

**A Response ability is a special ability used.** A card's `type: Response` ability is the same kind of action
as a phase ability and reports itself as used once its costs are paid; its word is Response, whatever phase it
is used in. So a card that answers "a special ability is used" with no phase named (Storied Homestead 13_76,
Terrible as the Dawn 53_103, and Boromir's Cloak 51_98 by Q-0203) is offered against one, and a card that names
a phase's abilities ("a skirmish special ability": 5_59, 5_64; "a regroup special ability": 5_114) is not, though the
Response was used in that phase (`mechanics/actions/ResponseAbilityIsASpecialAbilityTests`). F-0201 said no card could
answer one; that was a misreading of the source and no fault. A cancelled "is about to" Response leaves the
situation standing, and its owner is offered it again for as long as he can pay (CR 4.2 "response"): leave him
nothing to pay with, or decline for him, before reading that the wound landed.

**A "reveal this card from hand" trigger is looked for only among the cards of the side its owner plays that turn.** `OptionalTriggersFromHandRule` filters each player's hand by his side of the turn, so a Shadow card's hand trigger (The Witch-king, Black Lord 12_183; Orc Crusher 13_112) is never offered on its owner's own turn, when his own companion is played: lay the companion as the other player's. Ketura kept it so (Q-0382: A, 2026-10-28: "a pester without a point").

**A meta-site's optional trigger is offered to every player unless its definition names one.** The engine
collects a Race to Mount Doom modifier's optional triggers for each player, whoever owns it, and `requires:
OwnerIsFreeps` is true for both: once the owner declines, the other player is offered the same trigger as its
"you" (F-0311: he took a card from his own deck on 91_2, drew on 91_3, killed the owner's companion on 93_3). Only
the decline shows it, and a test that accepts never does: every "you may" on a modifier has a test that declines
and reads `assertFalse(scn.ShadowHasOptionalTriggerAvailable())` before its bookend. The correction is `player:
owner` on the Trigger (shapes.md, F-0311). A modifier's phase action (`Activated`) goes the same way: it is
offered to whichever player may act in the phase, and with no owner requirement the other player takes it and
pays from his own hand (F-0312, 91_10). Read him as not offered on a board where he CAN pay: with an empty hand
he is not offered whoever owns the modifier, and the reading shows nothing (91_10's old test). Which actions are
meant for both players is in shapes.md (F-0312). The `-` of "your fellowship" (the modifier across the table,
nobody offered) wants the same: the mover's side holds what the cost takes (93_3: a companion to kill), or a
definition without its `OwnerIsFreeps` offers nothing either and passes.
A site's optional trigger with no `player:` line is offered first to the Free Peoples player of the turn, so a
`player: free people` taken out is noticed by the decline test alone: every "the Free Peoples player may" on a
site has one (Osgiliath Reclaimed 11_252, by `mutate.py --replace`, dec-b11_212).
**A site's phase ability is offered to each player who may act in that phase.** CR 4.2 says so outright only of
Shadow abilities on sites; in the regroup or skirmish phase both players are asked, each paying from his own cards:
Mere of Dead Faces 11_246's "Regroup: Exert Sméagol or Gollum" is the Shadow player's too, with his Gollum, and
each side's test of it lays the other side's payer able to pay (dec-b11_240).
**An optional trigger with several effects is offered while any one of them can act.** Where the first effect has
nothing to do and a later one still has, the offer is made and taken does the rest; lay that board beside the
Q-0034 one where neither can act, or a definition that gates the whole offer on the first effect (it moved into
`cost`) survives every test (found on Home and Hearth 12_122 in dec-b1_119, whose trigger Q-0314 has since made
required: shapes.md, "You may <do X>. Then, <Y>").

**A `multiple` is checked for playability in full, inside a `choice` too.** A branch of a `choice` that is a
`multiple` (and a plain list there, which `EffectAppenderFactory.wrapArray` makes one) is playable only if all of it
is, so a branch of a reveal and what follows it is not playable with that deck empty, and with every branch so the
whole action is withheld, even one a cost buys (Q-0163). Put the reveal alone in the branch and what follows after
the choice, under an `If` on `HasCardInMemory` (The Palantír of Orthanc 4_166, F-0690; dec-b4_164).

## Reading and answering an offer

**The phase-action readings of each player's OWN phase have no player in the name.** The Shadow player at his
Shadow phase is `AwaitingShadowPhaseActions()` and the Free Peoples player at the fellowship phase is
`AwaitingFellowshipPhaseActions()`; there is no `AwaitingShadowShadowPhaseActions()` or
`AwaitingFreepsFellowshipPhaseActions()`. Every shared phase names both: `AwaitingShadowRegroupPhaseActions()`,
`AwaitingFreepsSkirmishPhaseActions()` and the rest (dec-b15_109).

**A cost that is a choice ("Exert bearer or discard 2 cards from hand") is a multiple choice whose own text names
neither option.** `FreepsDecisionAvailable("Exert bearer")` is false while it is pending. Read the options whole
with `assertEquals(List.of("Exert bearer", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices())` and answer
with `FreepsChoose(option)`; with one option payable nothing is asked (Elven Sword 4_64, Card_04_064_Tests). Show that
by which cost was paid (wounds and threats read before and after), not by an absent option: once the cost is paid the
decision on screen is the effect's own choice, so `assertFalse(ShadowDecisionAvailable(<option>))` shows little (7_166). An
option that names a culture comes through as HTML (`Discard a <span class='cultureHint' ...> Raider</span> card from
hand`), so neither reads it: count the options with `ShadowGetMultipleChoiceCount()`, read each with
`ShadowChoiceAvailable(<words>)` and answer with `ShadowChooseOption(<words>)`, which match on part of the text
(Ships of Great Draught 8_65): take a fragment with no culture word in it ("minion" for "Discard a [sauron] minion",
Rank and File 10_96). An EFFECT that is a choice (`type: choice` with its `texts:`) is asked the same way:
its options are the definition's `texts`, read with `FreepsChoiceAvailable("Liberate a site")` and answered with
`FreepsChoose(<the whole text>)` (Andúril 17_27, "liberate a site or discard a condition borne by a companion"). The Shadow player's
answer is `ShadowChoose(<text>)`; there is no `ShadowChooseMultipleChoiceOption` (Dark Swooping Shadows 10_58 in `Card_16_005_Tests`).

**"Is an optional trigger pending" is a broad question.** `FreepsHasOptionalTriggerAvailable()` is true for any
optional decision, the Ruling Ring's included. To assert that a particular card's trigger is on offer, pass the
card: `FreepsHasOptionalTriggerAvailable(card)` goes by the card each offered action belongs to, whatever its
prompt says. Where one card offers two optional actions in the same decision (Ethereal Banner 103_21's trigger
and its response, on an overwhelm), add words from the action's description:
`FreepsHasOptionalTriggerAvailable(card, text)` and `FreepsChooseOptionalTrigger(card, text)`, as
`FreepsUseCardAction(card, text)` does for phase actions. A response EVENT in hand that answers an "is about to"
window is read and played the same way, by the card: `FreepsHasOptionalTriggerAvailable(card)` and
`FreepsChooseOptionalTrigger(card)` (Intimidate 1_76, dec-b1_71). So is one at a win (Nobody Tosses a Dwarf 1_23 at a Dwarf's
win, Card_09_010_Tests) and a Response ability of a card in play (Ring of Retribution 9_9, dec-b9_2).

**One `ShadowDeclineOptionalTrigger()` (or `FreepsDeclineOptionalTrigger()`) declines every offer still open at that "Optional responses" decision.** Where
one card makes two optional triggers at once (an ambush minion's ambush and its own "when assigned" trigger) or
several copies each leave an ambush offer, a single decline passes on all of them, and a second decline fails with
the player "not being asked anything". Take the ones wanted first, by card and words, then decline once. The words
the game gives them: "Ambush - add N" for the keyword, "Optional trigger from <card>" for a trigger with no `text:`
of its own, so `ShadowHasOptionalTriggerAvailable(card, "Ambush")` and `(card, "Optional trigger")` tell them apart
(Desert Runner 7_133, Card_07_133_Tests.EachCopyAssignedToACompanionWoundsItsOwnCompanion; dec-b7_133).
With NO offer open, the decline answers whatever decision the player has instead: at the start of the maneuver phase
it passes the Free Peoples player's maneuver actions, and the next `FreepsUseCardAction` fails. A follower's Aid is
offered only where its cost can be paid (Roäc 32_8 with no Thorin in play): decline it only on a board that offers it.

**Two optional triggers of ONE card that answer the same event are two offers, each made once (F-0617, engine pass 67).**
Each `Trigger` entry of a definition has its own "used" mark (card id, blueprint, the entry's number), so at Shelob,
Menace 18_34's win taking "Play Gollum" leaves "Add threats" on offer and the other way round; the one taken is not
offered again for that win. The conditions listed in one entry (`trigger: [a, b]`) share its mark: still one offer. A "you may" trigger and a
Response of one card on the same win (Banner of Elbereth 6_14; 6_51 and 6_90 print the same Response) are two offers too,
but a Response that discards the card takes the trigger's offer with it; the trigger taken first leaves the Response on offer (F-0389).
One `FreepsDeclineOptionalTrigger()` there declines both (Card_06_090_Tests.DecliningTheExertionExertsNobody).

**Toil is a choice of cards, asked only when there is something to choose** (CR 4.2 "toil"; mechanics/keywords/ToilTests).
A toil card is offered when the pool covers its cost less the toil number for each character of its culture its
player could exert (an exhausted one is not counted, nor one of vitality 1, who cannot exert at all), and any roaming penalty on top: exertions never pay the
penalty. Played, it asks for the characters to exert (`ShadowGetChoiceMin()` is the number of exertions the pool is
short by, 0 when it pays in full; the maximum is every candidate; `ShadowDeclineChoosing()` exerts nobody). Where
the exertions owed are all the candidates there are, they are made without a question; with NONE owed (the pool
pays in full) the choice is still asked, minimum 0, even of a single candidate, and it comes ahead of the event's
own choice of target, which may offer the same cards (Gathering Strength 12_66, dec-b12_64). A toil card played ON a
bearer (Berserker Torch 12_136, the only Shadow one) is held to the same arithmetic on each bearer it could go on,
and its "not offered" reading one short is asserted like a minion's (F-0350, fixed by engine pass 23).
A FREE PEOPLES toil card (ToilTests plays only Shadow ones) adds its cost to the pool, so nothing is short: the
choice's minimum is always 0, its maximum every [culture] character that can exert, an ally among them, and it is
asked even of one candidate; read it with `FreepsGetChoiceMin()`/`FreepsGetChoiceMax()` and answer with
`FreepsChooseCards(...)` (Elven Marksmanship 11_17: 6 less 2 for each exertion; Card_11_017_Tests).
Played ON a bearer (Protecting the Hall 11_149), the bearer is asked first and the toil exertions after it
(Card_11_149_Tests).
A trigger that answers a toil exertion ("each time you exert him to play ...", Legolas' Bow 11_23) is put before
the card lands: at the offer the card is on its way (`VOID_FROM_HAND`) and its cost is not yet in the pool, so read
where it lies and the pool after the offer is answered.
A card in play that takes more off while the toil card is played (Úlairë Enquëa 12_175, a Response on `beforeToil`
with `incrementToil`) IS counted in the offer (Q-0381: A, engine pass 70; `ToilRule`, mechanics/keywords/
ToilResponseTests): once for each use in a row its cost can pay, and the toil candidates after those wounds, so an
unwounded Enquëa alone takes 3X off a [wraith] event's cost. A play offered on the Response's account and then
played with the Response declined is a cost that fails: what the pool holds is paid and the event is discarded.

**Muster is one optional trigger for all of a player's muster characters** (CR 4.2 "muster";
mechanics/keywords/MusterTests). At the start of the regroup phase a player with a muster character and a card in hand has
one offer, "Use Muster" (`ShadowHasOptionalTriggerAvailable(card, "Use Muster")`), the Free Peoples player's first. Murderous Uruk 11_195, used in many files as a plain [uruk-hai] Uruk-hai, has muster: a test that reaches the regroup phase with it in play and a Shadow card in hand answers the offer first (Card_15_173_Tests).
Taken, it is one choice of 0 to N cards from hand, N the number of his muster characters, and as many are drawn
after. With an empty hand there is no offer. `SkipToPhase(Phase.REGROUP)` rests on the offer: a test that only
passes through takes or declines it. `SkipToSite(n)` stops on it too and fails ("expected to end between turns or
in the Fellowship phase, but ended in REGROUP"), since its turns draw a hand: put the muster character, or the
card that gives muster, down after the skip (Phial of Galadriel 13_155, Card_13_155_Tests; a probe, dec-b17_22).
`SkipToMovementDecision()` called before the regroup phase stops on it too, at the Free Peoples player's "Play
Regroup action or Pass", and the move answer after it is refused: `SkipToPhase(Phase.REGROUP)`, decline the offer,
then `SkipToMovementDecision()` (Celeborn 13_11 at a forest, Card_13_011_Tests; dec-b13_9).

**A yes/no in the middle of a trigger is not an optional trigger.** A definition's `Optional` effect for a named
player ("the first Shadow player may discard 2 cards from hand to add a burden" inside a site's required trigger,
Courtyard Parapet 63_188) asks that player a yes/no in the definition's own `text`. `ShadowHasOptionalTriggerAvailable()`
is false there: read it with `ShadowDecisionAvailable("<words of the text>")` and answer it with `ShadowChooseYes()` /
`ShadowChooseNo()` (and the Free Peoples twins). It is not asked when its cost cannot be paid. The Decipher
original 13_188 is NOT built so: its trigger is a plain optional one with `player: shadow`, read with
`ShadowHasOptionalTriggerAvailable(site)` and answered with `ShadowAcceptOptionalTrigger()` (`Card_13_188_Tests`).

**`AcceptOptionalTrigger()` is no assertion that a trigger was offered.** It answers "0" to any choice of actions,
and a definition's `choice` of branches ("Choose action to perform") is one: called where no optional trigger is
pending, it silently takes the first branch. Assert `ShadowHasOptionalTriggerAvailable()` first (Card_31_019_Tests).

**An ability with wording of its own, and a card with several.** `FreepsActionAvailable(card)` sees an ability
whose definition gives it its own `text:` (Library of Rivendell's "Stack your tale or artifact from play") as
well as one the game words "Use <card name>". Where a card offers several actions, name the one meant:
`FreepsActionAvailable(card, "from hand")` and `FreepsUseCardAction(card, "from hand")`. Both read a use: a
card in hand that can be played, an event included, is read with `FreepsPlayAvailable(card)`.
The words are matched by a case-blind "contains", first offer first: where one ability's text lies inside
another's (Gorgoroth Berserker 8_96's "Exert this minion twice to take control of a site" inside its "Remove a
threat and exert ..."), the shorter words read both, and single out the shorter only on a board where the longer
is shown not offered (`Card_08_096_Tests`, dec-b8_91).
A card's own "you may play this ... from your discard pile" is a use too (Gollum, Plotting Deceiver 7_58: "Use
Gollum, Plotting Deceiver"): `ShadowPlayAvailable(card)` is false for the card lying in the pile, and
`ShadowActionAvailable(card)` and `ShadowUseCardAction(card)` read and take it (`Card_07_058_Tests`).
A response EVENT whose definition gives its play a `text:` (Beyond Dark Mountains 4_141, "Play in response to
death of <the character>"; and four more: 4_37, Unheeded 8_115 ("Wound {exertedMinion}"), 10_3, 13_122) is
read by neither at its "Optional responses" decision:
`ShadowPlayAvailable(card)` and `ShadowActionAvailable(card)` are both false there, though `ShadowPlayCard(card)`
plays it. The two-argument `ShadowActionAvailable(card, text)` and `ShadowUseCardAction(card, text)` do read and
take it, by words of its own text: where that text names a memory (4_37's "Make {winner} Fierce and +4 Strength")
the name tells one winner's offer from another's (`Card_04_037_Tests`, dec-b4_37). Or read the offer with `ShadowHasOptionalTriggerAvailable(card)` (probed at the landing of dec-b4_78), and
take it with `FreepsChooseOptionalTrigger(card)` where it is the Free Peoples player's (Unheeded answering
Grishnákh 5_100's exertion, `Card_05_100_Tests`, dec-b5_100). A
response event with no such text is read with `ShadowPlayAvailable` (Ravage the Defeated 4_32; Gondor Will See It
Done 2_34, `FreepsPlayAvailable`, Card_10_119_Tests). That read is an offer, not what the game is waiting for:
lint's bookend check wants `ShadowDecisionAvailable("Optional responses")` after it where the test ends on the
offer (Declined Business 18_65, dec-b18_64). Where a response event must NOT be offered, no window opens at all:
read the decision that follows (`AwaitingShadowManeuverPhaseActions()` and the like) beside `ShadowPlayAvailable(card)`
false, which alone is false at any choice of phase actions (Card_01_194_Tests; Haunting Her Steps 4_155, dec-b4_155).

**Using a card's action.** `FreepsUseCardAction(card)` and `ShadowUseCardAction(card)` take the action `FreepsActionAvailable(card)` sees, even where the game lists another of the card's actions first (a possession that another companion could bear lists "Transfer" before "Use"). A card that offers no such action has the first action listed for it taken, as before: an optional trigger or a response is accepted this way. `FreepsUseCardAction(card, "Use")` is no longer needed to get past a transfer. Two abilities of one card that exclude each other by a requirement (Frodo's Cloak 4_303: "At sites 1[T] to
4[T], ... At any other site, ...") are both "Use", and the first listed is taken, so a second wrongly on offer leaves
every result the same: where one is offered, read `FreepsGetActionCount()` as well, on the board where the other must
not be (Card_04_303_Tests; mutate.py survivors there before the count was read).

**Two copies of one card.** `FreepsActionAvailable(card)`, `FreepsPlayAvailable(card)`,
`FreepsTransferAvailable(card)` and their Shadow twins answer for the physical card they are given: with two
copies in play, each is asked about separately, and a per-copy limit is tested by asking about each copy. The
overloads that take text (`FreepsActionAvailable("Use Orc Taskmaster")`) still match any action so described.
Two copies of a required trigger at one moment that is no "is about to" (Bent on Discovery 1_206's end of the
Shadow phase) are not put to their player to order: the copy put into play first is carried out first
(Card_01_206_Tests), so a test can tell the copies apart by placing them in order.
The card-choice checks are different where the cards are in a pile or are only shown: see the next entry.

**A choice from a pile, and a showing, are read by blueprint.** Where a decision lists cards that are not in
play (a search of a deck, a play from a discard pile, a reveal), `FreepsHasCardChoiceAvailable(card)`,
`FreepsHasCardChoiceNotAvailable(card)` and the `(card, false)` reading of a showing match by blueprint: any
copy answers for any other, the other player's copy in his own pile or hand included. An invalid candidate
there, and a card left out of a reveal, needs a blueprint no valid or shown card has (another title, or another
version of the title); a test that reads where the chosen card went keeps one copy in the pile. The ANSWER goes
by the kind of decision (`ChooseCardsHoweverAsked` in the rig's Choices.java): where the pile's cards are asked as
a card selection by id, `ShadowChooseCards(card)` takes that very card (Mountain-troll 65_112's tests); where they
are shown as arbitrary cards, it answers by blueprint and the FIRST copy listed goes, whichever copy was named
(Morgul Blade 1_216's `transferFromDiscard`: `ShadowChooseCards(tip2)` moved `tip`; the test names the first).
A play from a discard pile by `playCardFromDiscard` is asked as "Choose card from discard", singular
(`ShadowDecisionAvailable("Choose card from discard")`, Card_17_049_Tests). A play from a STACK by
`playCardFromStacked` is asked as "Choose card to play", a card selection by id: `FreepsChooseCard(card)` answers
it and `FreepsChooseCardBPFromSelection` fails (Treebeard, Earthborn 4_103, dec-b4_103).
lint's `same-blueprint` warning is only for the `...BPFromSelection` calls. It says nothing
where the twin is never named in the test (it lies in the draw deck), the chosen card was cheated out of the draw
deck, and the card's text takes nothing from a draw deck.
It cuts both ways (and `same-blueprint` warns of this too, at a choice the test goes on to answer by blueprint):
a card read as NOT offered (one still in the hand, say) fails that reading as soon as a valid
candidate of its blueprint is put in the pile, so a candidate added later takes a blueprint of its own (68_82).
The other player's pile can be told apart all the same: the invalid candidate for "your discard pile" is a card
in HIS pile of a title that is in none of the chooser's piles (91_11: his Orc Bowmen, the chooser's left in the deck).
For "your draw deck", where both decks hold every alias, move the chooser's own valid cards out of his deck and
leave the other player's in his: nothing offered then shows that only the chooser's deck is searched (Card_11_259_Tests).
A choice among cards that lie in no zone at all (94_31's "site from outside the game") is read and answered by
blueprint id, `ShadowHasBPChoiceAvailable("1_361")` and `ShadowChooseCardBPFromSelection("1_361")`, and
`ShadowGetChoiceMin()` reads its minimum as for any choice of cards (Card_94_031_Tests).
To read that a reveal of the OTHER player's hand leaves the revealer's own hand unshown, the revealer's card must be
of a title that hand lacks: his copy of a card that is also in it reads as shown (Card_01_086_Tests).

**Several cards placed beneath a draw deck are asked for one at a time.** `putCardsFromDiscardOnBottomOfDeck`
with more than one card to place, `all(...)` included, asks its player which goes first (a choice of one, minimum
1) and places the last unasked: Breeding Pit 1_122 with two copies in the discard pile asks once
(`ShadowChooseCards(copy)`; `Card_01_122_Tests`). Read no order between identical copies.
With a count smaller than the pile the cards are first chosen together ("Choose card from discard", minimum and
maximum the count, `FreepsChooseCards(a, b)`), and only then is the order asked (Blunt The Knives! 33_4, 2 of 3).
`putCardsFromDeckOnBottomOfDeck` asks the same way, as a choice among cards shown ("Choose card to put beneath
draw deck", `FreepsChooseCardBPFromSelection(card)`): The Mirror of Galadriel 15_22 with two cards left asks once
and with three twice; the first chosen lies highest and the last, unasked, is the bottom card
(`GetFreepsBottomOfDeck()`, `GetFromBottomOfFreepsDeck(2)`; `Card_15_022_Tests`). Before it, a
`chooseArbitraryCards` whose filter takes none of the cards looked at is still put, as a choice of 0 to 0 with
all of them shown: `FreepsDeclineChoosing()`. `putCardsFromDeckOnTopOfDeck` asks the same way ("Choose card to put
on top of deck"): Back to the Light 18_6 with two cards set aside asks once; the first chosen is put down first
and the last, unasked, lies on top (`GetFreepsTopOfDeck()`, `GetFromTopOfFreepsDeck(2)`; `Card_18_006_Tests`).
`putCardsFromHandOnTopOfDeck` asks it too, after the choice of cards from hand: Nenya 9_20 with two chosen asks
once (`FreepsChooseCard(first)`), and the order read is the player's own answer (`Card_09_020_Tests`). With exactly
N cards in hand for "place N cards from hand" the choice of cards is skipped and only the order is asked (N-1
questions): a `FreepsChooseCards` there is refused with a bare `DecisionResultInvalidException` (Gandalf, Manager of
Wizards 7_37, `Card_07_037_Tests`; dec-b7_35). Leave other
cards in the deck under them, or a reading of "on top" cannot be told from one of "anywhere in the deck".
`putCardsFromHandOnBottomOfDeck` asks the hand's owner the same way ("Choose card to put beneath draw deck",
`FreepsChooseCard(card)`): the first chosen goes beneath first, and the last, unasked, is the bottom card (Beyond All
Darkness 8_68, `Card_08_068_Tests`). With a fixed count smaller than the hand, the cards are chosen first, all in one answer ("Choose cards from hand", minimum and maximum the count, `FreepsChooseCards(a, b, c)`), and only then is the order asked (Glóin 15_6 with 3, `Card_15_006_Tests`; dec-b15_5). That order step lists the chosen cards as a selection and takes `FreepsChooseCard(card)` (Glóin) or `FreepsChooseCardBPFromSelection(card)` (Wise Advice 33_18, 2 from a hand of 8, `Card_33_018_Tests`); the first chosen lies highest. While the choice waits the hand count is unchanged, so a `-` board ("the hand is
kept") reads `FreepsDecisionAvailable(...)` false and the state the game then waits in, not the count alone.

**Two offers of one response.** When the same card offers the same trigger or response twice in one decision (two Nazgul winning one skirmish), each offer's text ends with " (for <the cards its trigger memorized>)". A single offer reads as before. Choose between them by that text. Where the trigger memorizes nothing, the offers read alike and come in no fixed order: Vilya 33_17 answering the two heals of one Lore of Imladris 30_13 put Balin's offer first in four runs of five, and `FreepsAcceptOptionalTrigger()` takes whichever is first. Read such a pair by its sum and difference, or lay a board where one heal is made at a time (`Card_33_017_Tests.TwoCompanionsHealedAtOnceAreEachOffered`; hobb-b33_16). The same holds for a response EVENT in hand whose trigger memorizes (Sack of the Shire 9_40: "Play Sack of the Shire (for Uruk-hai Raiding Party)"): "that minion" is picked by the offer taken, and the event's own choice is never asked, so read `ShadowActionAvailable("Play X (for Y)")` and `ShadowGetActionCount()` and take one with `ShadowChooseAction(text)`, not a card choice.

**When a wound or an overwhelm kills and something answers it.** (A companion killed while threats are on the table turns them into wounds first, asked one at a time: "Threat wounds are asked for one at a time", under Cards, owners and zones; lay no more threats than the test spends, Card_15_047_Tests.) The Free Peoples player, whoever owns the trigger,
gets a "Required responses" decision listing the rule and the trigger (but a trigger on the KILL that an overwhelm
makes raises none: Goblin Spear 1_182's burdens for an overwhelmed Hobbit are added, or Sam 4_316's Response to them
offered, straight after `PassSkirmishActions()`; the Spear's kill by a skirmish WOUND is ordered by
`FreepsResolveRuleFirst()`: Card_01_182_Tests, Card_04_316_Tests; a Shadow card's "wins a skirmish" trigger
beside "Resolve skirmish damage" is the same decision, the minion's own required "each time this minion wins a
skirmish" included, in the fierce round as well (Tower Lieutenant 1_280: `FreepsResolveRuleFirst()` after the passes
that end each skirmish, `PassFierceSkirmishActions()` in the fierce round, Card_01_280_Tests), and so is a Free Peoples companion's own required "each
time X wins a skirmish", Boromir 59_31: left unanswered, the only sign is that the beaten minion is still in play
after `PassSkirmishActions()`; a companion's own required "each time X loses a skirmish" raises it too, Isildur
18_54: until `FreepsResolveRuleFirst()` his wound for losing reads 0 after `PassCurrentPhaseActions()`; so does a hand weapon's required "each time a
minion loses a skirmish to bearer", Dwarven Axe 1_9 on the winner: the beaten minion stays in play until it is
answered, Card_02_005_Tests; so does a support-area card's required trigger on another character's win, Stand
Together 12_131's "each time a Hobbit wins a skirmish": muster reads absent after `PassSkirmishActions()` until
`FreepsResolveRuleFirst()`, Card_12_131_Tests. Any required win or lose trigger, on any card, waits on it; it is not
only for deaths). Two copies of a card with such a required trigger make it one decision of three entries, the
rule and each copy's trigger; after `FreepsResolveRuleFirst()` both triggers are carried out and no second
ordering is asked (Sudden Fury 12_132, two copies and a [shire] companion who loses). The rule's entry is not always listed first (King of the Dead 103_27 killed with a threat out: `["Required
trigger from King of the Dead…", "Assign threat wounds"]`; Ferocious
Haradrim 11_80 answering a death: the threat wounds first). `FreepsResolveRuleFirst()` resolves the rule, the entry
that belongs to no card, wherever it is listed; `FreepsResolveActionOrder(text)` resolves the entry whose wording
contains the text. A skirmish whose losing side was already removed (step 2C) raises no "Resolve skirmish damage"
entry (4A, engine pass 56): a required win or loss trigger of that skirmish (Dwarven Axe 1_9, Treebeard 15_38) is
carried out with nothing to order. A death with NO threat on the dead pile raises no threat entry at all (F-0567, engine pass 56):
a card's lone required trigger on that death is carried out with nothing to order, so no `FreepsResolveRuleFirst()`.
A trigger that names who kills (`killed` with `killer`: "if X kills a companion", CR 4.2's definition, Q-0328: B) is
answered for a kill by X's own text or by losing a skirmish X is in (every minion of it is a killer), not for another
minion's wound in X's skirmish; a required one answers the wound that kills as well (engine pass 49; mechanics/wounds/WhoKillsACharacterTests).
A trigger added for the skirmish ("if that minion is overwhelmed, ...") raises the decision even when its effect
will come to nothing (Cirdan 60_8 with one [elven] event: half, rounded down, is none): `FreepsResolveRuleFirst()`
is still needed, and nothing is asked after it. An event's added "if <he> loses the skirmish" (`addTrigger`
`losesSkirmish`) is the same decision beside "Resolve skirmish damage" (Where Now The Horse 17_103: the spotted
companion stays in play until `FreepsResolveRuleFirst()` after `PassSkirmishActions()`, Card_17_103_Tests). So does a card's standing required "each time X wins a
skirmish" with nothing to act on (Focus 65_15 with nobody to heal, before F-0289 made it optional), and a
minion's own required "each time this minion wins a skirmish" whose `requires` is met and whose choice finds nothing
(Mûmak Commander 7_152 beating an ally, with no companion to kill: Card_07_152_Tests), and a possession's required
win trigger on the winning minion whose repeat count is zero (Banner of Isengard 6_59, an exertion for each site the
Shadow player controls, with none controlled; nobody killed: Card_12_107_Tests).
An OPTIONAL trigger raises no such decision: a Shadow card's optional "each time a companion loses a skirmish" (Gríma, Betrayer of Rohan 12_68) is offered to the Shadow player straight after `PassSkirmishActions()`, the loser's wound already placed, and `FreepsResolveRuleFirst()` there fails (Card_12_068_Tests; the required trigger of Harrying Hillman 12_69 does need it, Card_12_069_Tests).
A required trigger whose `requires` is unmet is not listed at all: no ordering is asked and the rule's wound
lands alone (The Mouth of Sauron 103_102 with neither a threat nor a burden possible, F-0154). A test knows which
board it is on and asserts it: `FreepsResolveRuleFirst()` under an `if` passes whichever the game does.
There is no Shadow twin of either and there will not be: by rule the Free Peoples player is the one who orders
simultaneous actions, whoever's they are.
A companion whose own text answers his leaving play (Radagast 59_26) raises this decision whenever he is killed,
by a wound or by a card that places him in the dead pile (not when he is discarded or returned to hand), and his
text is carried out only once it is answered: as a helper to be killed in another card's test he leaves it pending.
Which is taken first can change what the trigger does, except where it measures the loser: Treebeard 15_38's
"the losing character's strength" is the strength it had as the skirmish resolved, whichever is first and though
the rule's wound has killed or strengthened it (`memorizeLoserStrength`; F-0480, engine pass 30). A SITE's standing "each time a companion is killed" (Slag Mounds 7_363) raises it on every death,
as `[null, "Required trigger from Slag Mounds"]`, and its burden is added only after `FreepsResolveRuleFirst()`. And an exert of "X minions" with X
above the number able to exert asks nobody and exerts every one that can. Two required "is about to" triggers on one kill (Rest While You Can 4_171 beside The Dead City 7_56 on
Sméagol 6_45) are ordered under another wording, 'Kill - <card> - Required "is about to" responses', which
`FreepsDecisionAvailable("Required responses")` does not match: `FreepsAwaitingRequiredResponseOrder()` reads both.

**A card's standing check is a required action, ordered with the others of its moment and looked at again as it
is carried out.** "While ..., the move limit is +1" and "discard this if ..." are `ConstantlyCheck` triggers. Alone,
one is carried out with nobody asked. Beside another required trigger of the same moment (Blade Tip 1_209's wound
at the start of the fellowship phase; Silinde 1_60's copy of the site's text on the move there) the Free Peoples
player gets "Required responses" with the check listed under its card's name
(`FreepsResolveActionOrder("Rivendell Waterfall")`), and it is collected again for whatever is carried out ahead
of it: after a wound that kills, the next ordering lists the death (an entry with no text) and the card twice, one copy in play or not, and the second entry does nothing once the first has run (`FreepsResolveRuleFirst()`, Sauron's Defenses 1_274). What the check gives is not
there until it is carried out (the move limit reads 2 at the turn's sanctuary-healing decision and while an
ordering waits, the ranger standing at the site), and its requirements are run again at that moment, so a check
whose condition has ended by then does nothing: right for a standing discard, and for a "+1 for this turn" the
player's own doing (the death taken ahead of the Waterfall's check leaves the limit at 2; the check taken first
gives 3). That is no finding (F-0389, dropped by Ketura: what follows from the order in which a player takes his
own required triggers is his, "so that it's their fault and not the rules"): a test takes the order that gives
the text's result, and none is parked for the other. Read what the check gives after the ordering is answered;
`Card_01_342_Tests`.

**A response window can come before the target.** Where the Free Peoples player holds a response event that
can answer an event being played, the game may put an "Optional responses" decision to him before the played
event's target is chosen, and an answer written for the target choice then answers the response window, with no
error; the test fails some lines later. Read what is pending before answering (seen on 51_115). It is the order
for every response to a play, not only a response event in hand: a Response ability on a card in play (Shrouded
Elf 63_23) is asked before the played event's effect chooses anything (CR 4.2 "playing a card", steps 5 and 6).
So is an "each time X is played" TRIGGER, which is answered and carried out before the card's own effects: read
what the event changes only after the trigger is answered (Stout and Sturdy 1_315's heal after Aragorn 11_53's).
A trigger's CHOICE comes first too: under White Hand Slayer 17_131 ("each time a player plays an event"), the
Slayer's choice of a character to exert is asked before Ferocity 4_151 chooses its Uruk-hai, and a card id written
for the event's choice is refused with a bare DecisionResultInvalidException that names neither (Card_17_131_Tests).

**A condition played by another card's effect asks where it goes before its own cost.** Saruman's Frost 1_135
played from the discard pile by A Fell Voice on the Air 3_52 first asks the site to attach to ("Choose target to
attach to", every site on the path), and only then its own "to play, exert an [isengard] minion". Answer the target
first (Card_03_052_Tests, dec-b3_50). A plain play from hand asks in the same order: Banished 4_139
asks its Free Peoples Man bearer first and its "To play, exert Saruman or an [isengard] Man" second (dec-b4_139).

**The entries of a required-response ordering are actions, not choices.** While `FreepsAwaitingRequiredResponseOrder()`
is true, read what is to be ordered with `FreepsGetActionCount()` and `FreepsActionAvailable("<card title>")` (each
entry carries its card's name: "Elrond", "Dwarven Heart", "The One Ring", "Gimli's Helm"), and name the first with
`FreepsResolveActionOrder("<card title>")`. `FreepsChoiceAvailable(text)` reads only a multiple choice's answers and is
false there. Two required answers to one "is about to take a wound" are put to the player again for every wound of
the same skirmish (`mechanics/actions/RequiredActionsAnsweringOneTriggerTests`). It comes wherever two required
triggers answer one moment, wounds or none: a minion played at Midgewater Moors 1_333 with a required when-played
trigger of its own (Goblin Runner 1_178: `FreepsResolveActionOrder("Midgewater Moors")`, Card_03_111_Tests).
Any REQUIRED `winsSkirmish` trigger, a skirmish event's added one ("If that Nazgûl wins this skirmish, ...": Unhindered
8_82) or a card's in play (Seeking New Foes 7_249, asked once even with two copies in play: Card_07_249_Tests),
is such a pair with the loser's wound: after the passes that end the skirmish the wound is not yet placed; read
`FreepsAwaitingRequiredResponseOrder()` and answer `FreepsResolveRuleFirst()` (Card_08_082_Tests; an optional one,
Mastered By Madness 8_73, never meets it).

**A candidate a helper's own text refuses shows nothing of the card under test.** A Hobbit already assigned is
not offered to a second Orc Assassin 1_262's "assign a Hobbit to skirmish this minion" at any site, so reading him
refused at Caras Galadhon 3_115 showed nothing of the site's bar (dec-b1_286, reviewer). Read the same candidate on
the control board: if it is refused there too, the reading belongs to the helper; drop it. A helper's own "To play" requirement is such a text: Dwarf Guard 1_7 in hand
with no Dwarf in play is refused by any play ability whatever its filter, so lay what it needs (Grimir 1_17) and read
`FreepsPlayAvailable` on it before reading it not offered (1_364, dec-b1_357, reviewer: shown by a mutation).

**"Is about to end" comes after the skirmish has resolved.** It is step 5 of Ketura's skirmish procedure as amended
(2026-10-20, binding; Q-0332; shapes.md, "The skirmish procedure"), and the engine has it there: `SkirmishPhaseAction` queues the
`SkirmishAboutToEndResult` behind `ResolveSkirmishEffect`, so the loser's wounds are on (or he is dead) when the
response is offered, and a cancel or the removal of a side's last character reaches it with no wound placed
(mechanics/skirmishes/TheSkirmishIsAboutToEndAtStepSixTests, named by the 2026-10-16 numbering; each step of the
procedure in mechanics/skirmishes/SkirmishProcedureTests). Who was involved is the skirmish's own record
(`Skirmish.getInvolvedFellowshipCharacter`, `getInvolvedShadowCharacters`): those it began with, a character who
replaced another in the place of the one replaced (F-0563, engine pass 57), and still those removed or killed in it;
the trigger's filter is asked of those cards as they are then: a minion killed by his loser's wound bears nothing any more (Q-0332: B), and
is still of his race.

**A card played by an effect that then does more to it answers its own "when you play" first.** Crazy Cob 31_49 plays a Spider from hand and makes it strength +2: Old Tomnoddy 31_53 played so offers his own trigger before the +2 is laid, so his strength read before that offer is answered is 8, not 10. Where his offer is withheld (nothing it could pay for), the +2 is on at once. Answer the played card's offer before reading what the rest of the effect did.

## Getting to the moment: set-up, the skippers and turns

**A tie goes to the Shadow side.** The Free Peoples character loses a skirmish unless his strength is HIGHER than the minions' total, and is overwhelmed at double or less (`fpStrength <= shadowStrength`, `fpStrength * 2 <= shadowStrength` in `ResolveSkirmishEffect`); a skirmish of strength 0 against 0 is his loss too. A board that wants him to win needs one point more (dec-b12_68).

**Set the twilight pool before the Shadow phase starts, and count what the move then adds.** Because the engine asks once, a pool set while the
Shadow player's decision is already pending does not make a minion playable. Set it during the fellowship phase,
then pass, or set it in the Shadow phase and refresh the decision (the method below: the simplest for a `C-` board
one twilight short of a play that is an ability's cost, 7_281). The move then adds one for each companion plus the new site's shadow number, so a test that needs an
exact pool adds those in: `// 10 set + 1 companion + 2 for the site`. A pool read as "unchanged" is read after that pass, never taken to be the number set (Corsair Boatswain 10_37). No region twilight (CR 4.2's +3 / +6) is
added on the ordered `FellowshipSites`, `TowersSites` or `KingSites` paths for a move to a numbered site; an unordered path adds it, and so does a move to a Shadows-block site (no printed number) laid on a numbered path (`MovementGameProcess`: Valley of the Silverlode 11_261 as site 4 added its 3 and 3 more; dec-b11_164). A pool that is too small shows only as the rig's
`<card> is not offering Shadow Player a play.`: count the roaming penalty as well as the cost.
To read both sides of a cost in one Shadow phase, set the pool there and refresh: `SetTwilight(3)`,
`ShadowRefreshDecision()`, not offered; `SetTwilight(4)`, `ShadowRefreshDecision()`, offered. No sum for the move is
then needed (mechanics/keywords/RoamingTests, ToilTests).
A Free Peoples card's cost is ADDED to the pool, never paid from it: a pool set "to afford" a Free Peoples play
does nothing (Radagast's Herb Bag 18_26 is played from a pool of 0). Set it only where a text reads it (Keep Your
Forked Tongue 4_96 spots 3 twilight tokens), or to read the cost added.

**No end of turn is sent before turn 1 (F-0327).** After the mulligans `MulliganProcess` ends what the starting
fellowships left "until the end of the turn" and hands on to `BetweenTurnsProcess`: no `EndOfTurnResult` is sent,
and the log has no "End of turn" line, before the first turn (mechanics/game/NoEndOfTurnBeforeTheFirstTurnTests). Until
engine pass 21 the whole end-of-turn step ran there, with the player who goes second counting as the Free Peoples
player: an `EndOfTurn` trigger on a card of his in play (Race Text 94_3 lets conditions into a starting
fellowship) or on a meta-site went off, and `StartGame()` could return with a question pending in BETWEEN_TURNS.
A test of such a card still reads that nothing is asked straight after `StartGame()`, and never answers a stray
question with an `if`, which is what hid the fault in the old test of Race Text 94_17.

**Both players act in most phases.** Fellowship is the Free Peoples player's alone and Shadow the Shadow
player's. In maneuver, archery, assignment, skirmish and regroup the Free Peoples player is asked first, then the
Shadow player, until both pass in a row. A test of a Shadow action in those phases passes for the Free Peoples
player first. `SkipToPhase` leaves the Free Peoples player with the decision, but not when it is called with the
Shadow player's decision pending in an earlier shared phase (after a `FreepsPass()` in the maneuver phase): then it
stops at the Shadow player's regroup decision, a `FreepsPass()` after it does nothing, and a `SetTwilight` there is
not seen until `ShadowRefreshDecision()` (Dark Tidings 7_130, dec-b7_129). After a player's action the other
is asked next: following a Free Peoples skirmish action `FreepsPass()` does nothing (he is not being asked), and
it is `ShadowPass()` and then `FreepsPass()` that end the skirmish, so the last reading inside a skirmish is
taken before that pair. After the LAST skirmish (a fierce one included) the regroup phase has already started when
the passes return, its start-of-phase triggers carried out, so a reading "just before regroup" is taken inside that
skirmish (A Shadow Rises 11_216's wounds, dec-b11_211). `PassSkirmishActions()` and its kin pass for whoever holds the decision first. The same
holds in the other shared phases: a test that ends on a Free Peoples maneuver ability rests on
`AwaitingShadowManeuverPhaseActions()`, and one on a Free Peoples regroup ability or event on
`AwaitingShadowRegroupPhaseActions()` (Glamdring 1_75; Knocked on the Head 4_308, where `AwaitingFreepsRegroupPhaseActions()` is false); a second use by the Free Peoples player needs `ShadowPass()`
first (Elven Guardian 67_7's tests). After a Free Peoples regroup event and `ShadowPass()` he is asked for regroup actions
again, not the move: `FreepsChooseToMove()` there fails, and `SkipToMovementDecision()` reaches the move (Safe Paths 6_44, dec-b6_42). The other way about, a Shadow event or special ability in those phases (with
any triggers it sets off) hands the next decision to the Free Peoples player's own phase actions (a Shadow
regroup ability rests on `AwaitingFreepsRegroupPhaseActions()`, New Authority 11_37, and a Shadow Assignment ability on
`AwaitingFreepsAssignmentPhaseActions()`, Threatening Warg 31_26), so `assertFalse(scn.FreepsAnyDecisionsAvailable())` after
it reads nothing about the event (it does after a Shadow-phase event, Dangerous Gamble 3_76): rest on
`AwaitingFreepsManeuverPhaseActions()` and its kin (They Will Never Stop Hunting You 3_84).
`PassSkirmishActions()` is the wrong call once the Free Peoples player has already passed in that skirmish (a
`FreepsPass()` made only to read what the Shadow player is offered): it passes for the Shadow player, the
skirmish resolves, and its second pass goes to whatever the Free Peoples player is asked next, the choice of
the next skirmish (DecisionResultInvalidException from a CardsSelectionDecision) or his regroup decision (the
test then rests on the Shadow player's). There `ShadowPass()` alone ends the skirmish. A third pass written by
hand after `FreepsPass()` and `ShadowPass()` fails the same way, with the error from `ChooseActiveCardsEffect`,
which names no pass (Card_17_133_Tests, dec-b17_133).

**A skirmish whose only Free Peoples character is killed mid-skirmish is over at once.** Killed by a Shadow skirmish event where his was the only skirmish, he leaves no skirmish to pass: the next decision is the Free Peoples player's regroup actions (`AwaitingFreepsRegroupPhaseActions()`, Card_01_255_Tests's ally test), not a skirmish action. The same holds for the minion side: a Skirmish event that discards the only minion of the skirmish (Staff Asunder 2_27) ends it at once, and the game rests at `AwaitingFreepsRegroupPhaseActions()` where that was the last skirmish (Card_02_027_Tests). So does a minion that discards itself as the cost of its own Skirmish ability while it is the only minion of its skirmish (Orc Fanatic 7_300): to read what the ability did to a target, assign a second minion to that skirmish (Card_07_300_Tests, Card_07_271_Tests). Where other skirmishes are still to come, the game rests at the Free Peoples player's "Choose next skirmish to resolve", where `PassSkirmishActions()` is refused: go on with `FreepsResolveSkirmish(...)` (Bold Easterling 15_73, Card_15_073_Tests). So too in the assignment phase: where the only Free Peoples character assigned is killed by an assignment trigger (Southron Fanatic 10_49 wounding a vitality-1 ally), the Shadow player gets no leftover-minion assignment, no skirmish is fought, and the game rests at `AwaitingFreepsRegroupPhaseActions()` (Card_10_049_Tests.AManAllyAssignedToItIsWoundedAndWithOneVitalityLeftIsKilled). So too where the companion removes himself (Sméagol, Slippery Sneak 8_27 discarding himself): and the next reading is taken after the start-of-regroup triggers have run, so a card that acts "at the start of the regroup phase" has already acted by then (Held Ground 7_181 had already removed its 3 threats: Card_07_181_Tests, dec-b7_181).

**No minions, no combat.** With no minion in play when the Shadow phase ends, the game goes straight to regroup.
A test that needs a maneuver phase needs a minion on the table: one stacked on a site or a card is not in play and
does not count (Dunlending Looter 4_11 lays Goblin Patrol Troop 1_177 beside its stacked Looters, as Uruk Trooper 4_199).
The maneuver, archery and assignment phases are each passed over when no active minion is in play
(`ManeuverGameProcess`, `ArcheryGameProcess`, `AssignmentGameProcess`), so a trigger at the start of one of them
that does something to "a minion" always has one: its `requires: CanSpot minion` cannot differ in play, and no
`-` on a board with nothing to act on (Q-0034) is owed for it (Race Text 94_11). That covers the minion only:
where the trigger needs a second card as well ("assign a minion to skirmish the Ring-bearer", Race Text 94_26), a
board on which it does nothing still arises (the Ring-bearer hindered) and the `-` is owed (F-0337).
A maneuver EVENT aimed at "a minion" meets a board with none only as a second copy, played after the first has
killed the only minion in the same phase (Quick As May Be 4_53, `WithNoMinionLeftInPlayItIsStillPlayedAndDoesNothing`).
With a minion in play left UNASSIGNED, `SkipToAssignments()`, `FreepsDeclineAssignments()` and
`ShadowDeclineAssignments()` do not reach the regroup phase: the game is still in the assignment phase at the
Free Peoples "Play Assignment action" choice, and passing it asks him to assign again (probe, dec-b9_44-l).
`SkipToPhase(Phase.REGROUP)` reaches the regroup phase with the minions unassigned and no skirmish fought, so
they stay in play unwounded and the Hobbit table's Bilbo is not overwhelmed (Card_09_044_Tests, Card_32_040_Tests), and a
trigger at the start of the regroup phase is left pending there for the test to answer: the plain way to test one with
minions in play (Dervorin 7_88).
With a FIERCE minion in play, the two declines end the first assignment only: the fierce assignment phase
follows, at the Free Peoples "Play Assignment action or Pass" choice, and `PassCurrentPhaseActions()` then a second
`FreepsDeclineAssignments()` / `ShadowDeclineAssignments()` pair reach regroup (Shelob 8_26 in Card_15_042_Tests,
whose other boards, Gollum 5_24 and Goblin Runner 1_178 unassigned with no fierce minion, reach regroup on two declines).

**Minions do not survive a turn.** `SkipToSite(n)` plays whole turns for both players and ends in player 1's
fellowship phase at site n. Minions and anything attached to them are discarded on the way, so put Shadow cards
down after `SkipToSite`, not before. The same holds for a stay: after `FreepsChooseToStay()` the turn ends and
the minions go to the discard pile with their wounds, so the `-` board of an "each time the fellowship moves"
trigger reads the offer absent and the minion in the discard pile, not its wounds (Card_05_056_Tests).
With the Free Peoples hand empty the reconcile asks nothing, so the next turn starts at once and the twilight pool
is emptied by rule: a pool read after `FreepsChooseToStay()` is 0 whatever the card did. Leave a card in that hand
so the turn waits at the reconcile (Deep Hatred 13_3's stay board, hobb-b33_49).

**Site 9 ends the game.** A fellowship that survives the skirmishes at site 9 has won, so `SkipToSite(9)` returns
with the game over. To test something at site 9, `SkipToSite(8)` and then pass the fellowship phase: the
fellowship moves, and the test is in the Shadow phase at site 9. If that Shadow phase ends with no minion in
play the game is over at once and no regroup decision arrives (a probe: the Shadow player passes, the phase
reads REGROUP, `GameIsFinished()`, nobody asked): a test there that plays no minion rests on the Shadow player's
decision or on `GameIsFinished()`, never on `AwaitingFreepsRegroupPhaseActions()` (Card_01_363_Tests). So is a fellowship that comes through its last skirmish at site 9 in the default format: `GameIsFinished()` is true as soon as that skirmish resolves, the minion still in play, and no regroup phase comes (Card_04_360_Tests). No wound of that skirmish is placed, so a response to a loser's wound is never offered there: a lost skirmish at site 9 wants the PCMovie table (Card_06_120_Tests.GetPCScenario; a probe showed no decision, no wound). In the PC
formats (`winAtEndOfRegroup: true` in lotrFormats.hjson; the Hobbit format has none, so no regroup phase is played at site 9 on the Hobbit table either, and a "sites 7 to 9" regroup bound is read at 7 and 8 only; `"pc_fotr_block"` as the scenario's format) the
regroup phase at site 9 IS played (the five-argument constructor with `VirtualTableScenario.PCMovie` as its last argument does it:
Card_07_363_Tests.GetRegroupAtSite9Scenario), so a regroup text at site 9 is a board in play there (Speak "Friend" and
Enter 2_26 is offered, and lays no site 10), and the game is won when both have passed, as the Shadow player
is about to reconcile, his hand untouched (`mechanics/game/ReconcilingTests`, `mechanics/sites/PlayingSitesTests`).
Where a CARD holds the win back to the reconcile in the default format (Caverns of Isengard 5_120), the Shadow
player IS asked to reconcile once both have passed (`ShadowDecisionAvailable("Reconcile")`), and the game ends
only on his answer (`ShadowDeclineReconciliation()`): `GameIsFinished()` read straight after `ShadowPass()` is
false (Card_05_120_Tests). A skipper that plays on through the winning
turn finds nobody being asked anything and fails, saying that the game is over and who won it:
`SkipToOtherPlayersTurn()` from player 1's fellowship phase at site 8 (it was a bare `NoSuchElementException`
until engine pass 24). Under a modifier that
keeps the game going at site 9 (Race Text 94_31) `SkipToSite(9)` returns in the REGROUP phase of the turn that
reached site 9, not in a fellowship phase, with player 1 asked for regroup actions (Card_94_031_Tests). The rig
reads that a game is over (`GameIsFinished()`) and for whom it ended well or badly (`FreepsHasWon()`,
`ShadowHasWon()`, `FreepsHasLost()`, `ShadowHasLost()`: by the player, not the side he had that turn; G-0042);
why is in the game log ("The game ends when it ends", below).

**Player 2 on site 9 with the game still going.** Player 1 wins at site 9 a turn before player 2 can stand
there, so a text that bites only at site 9 ("your fellowship cannot win until ...", 94_31) is shown for player 2
by putting him a site ahead: `SkipToSite(6); SkipToOtherPlayersTurn(); ShadowPass(); SkipToMovementDecision();
ShadowChooseToMove(); SkipToOtherPlayersTurn(); SkipToOtherPlayersTurn();` has him move twice on his sixth turn
and leaves him at site 8 in the fellowship phase of his seventh; his move then reaches site 9 before player 1 has
(Card_01_362_Tests, the last test). On a plain table `SkipToPhaseInverted(Phase.REGROUP); PassCurrentPhaseActions();`
in place of `SkipToMovementDecision()` leaves player 1's reconcile unanswered and `ShadowChooseToMove()` fails
("Free Peoples Player is being asked Reconcile"); that form passes only on Card_94_031_Tests' own table.

**The game ends when it ends.** When the Ring-bearer dies or is corrupted the game stops at once and no later
phase arrives. Make the assertions that matter before the event that ends the game. Who lost is read with
`FreepsHasLost()` / `ShadowHasLost()` (and `FreepsHasWon()` / `ShadowHasWon()`; all four false while the game
goes on; `mechanics/ring/CorruptionBeforeTheGameStartsTests`). One of the four asserted TRUE is a closing reading for
`lint.py`, as `GameIsFinished()` is; read as false it is not, since a game still going on answers the same. WHY is read from
the game log, in the game's own words: `MarkGameLog()` before the act that ends it, then
`GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted")` (`mechanics/hinder/HinderedRingBearerTests`,
Card_01_252_Tests): neither `GameIsFinished()` nor who lost tells a corruption from any other end.

**A starting fellowship.** `StartGame()` skips it. To test "(except in your starting fellowship)": the scenario
constructor returns with player 1's "Starting fellowship" choice pending; `FreepsChooseCard(card)` plays a
companion into it; while a companion left in the deck fits under the total of 4, player 1 chooses on (Merry
1_303 at 1 after Pippin 1_307 at 1: `FreepsDecisionAvailable("Starting fellowship")` still true,
Card_01_307_Tests). Once none fits, the choice does not end by itself: a player who has put a card in is asked
only to press DONE ("Starting fellowship - Nothing more can be added: press DONE", no card offered), which
`FreepsIsAskedOnlyToConfirm()` reads and `FreepsConfirm()` answers (G-0066), and player 2's choice is pending only
after that answer. A player who never had a companion that fits is not asked (mechanics/game/StartingFellowshipTests).
End the test there: going on deals opening hands.
To play on instead, answer player 2's choice too (`ShadowDeclineChoosing()` takes nobody; `FreepsDeclineChoosing()`
ends player 1's early, never `FreepsChoose("")`) and then call
`StartGame(true, false)`, which leaves the starting fellowships alone; called while either choice is still
pending it answers nothing and the game has not begun (Card_91_001_Tests). While the choice is pending the game
keeps the running total of the costs played in the twilight pool: `GetTwilight()` reads it (2 after a cost-2
companion, a card offered only while the total with its cost is 4 or less), and the pool is emptied as turn 1
begins (Card_94_003_Tests). The total begins again at 0 as the next player's choice begins and the LAST player's
total is still in the pool at the mulligan question: a pool read as 0 there, after `ShadowDeclineChoosing()`,
shows nothing. A pending "Starting fellowship" choice is a showing of cards: `FreepsGetTotalRevealedCardCount()`
reads 4 while player 1 chooses and `ShadowGetTotalRevealedCardCount()` 4 once player 2 does (each player's own
choice; both 0 once it is answered: probe, dec-b8_34), so a reveal count read while one is pending shows nothing
of a reveal (Faramir 8_34). Read "no tokens for a starting fellowship" in the first fellowship phase, with player 2 having
played somebody too (mechanics/game/StartingFellowshipTests). Every rig table gives both players the same deck, so a
directive's "the game goes on to the mulligan question" is: player 1 asked nothing
(`FreepsAnyDecisionsAvailable()` false, after `FreepsConfirm()` where his fellowship was full), player 2's
"Starting fellowship" pending, and the mulligan question only after his answer. While a player chooses, only HIS cards on the table are in force, and `GetBurdens()` is
his: Éomer 4_267 reads 2 with his own player's Elite Rider 4_265 down and 3 again once the other player is
choosing. Read a cost, a spot or a resistance while its player is the one being asked. A starting companion's
"To play, add a threat" is paid there, as its "When you play" is used (Q-0305), but `GetThreats()` reads 0 once
player 2 is choosing: read the threat after `StartGame(true, false)` (Cursed of Erech 10_26, Card_10_026_Tests). Site 1's text is not in
force while either player chooses, and its keyword is not counted (CR 4.2 "starting fellowship"; F-0470;
mechanics/sites/SiteTextIsNotActiveWhileStartingFellowshipsArePlayedTests): at East Road 1_320 a companion in the draw
deck reads its printed cost, and 2 more in the first fellowship phase. The cost of the
companion whose play ends the first player's choice cannot be read from the pool, which reads 0 again as the
next choice begins: read `GetTwilightCost(card)` of a card still in the draw deck while its owner chooses, which
answers with a starting-fellowship discount in force (Gondorian Knight 5_35 reads 2 before Alcarin 5_31 is chosen and 1 after;
Card_05_031_Tests). A discount a card gives its OWN cost (`modifyOwnCost`) is not always in that reading: Elite Rider
4_265 reads 2 as the choice begins, but once one Rider is chosen the second reads 3 and is charged 2 (he is
offered at a total of 2, and after him Merry 1_302 at 1 is not; Card_04_265_Tests): read such a cost by the offers. A starting fellowship
is Free Peoples cards: where a card lets other card types in
(Race Text 94_3), a Shadow card of those types in the draw deck is never offered, whatever the running total
(Q-0189, F-0343; mechanics/game/StartingFellowshipIsFreePeoplesCardsTests).

**Archers get in the way.** `SkipToPhase` passes for both players and takes the first choice on required
triggers, but it does not assign archery wounds. An archer on the table stops it, or kills the minion the test
was about. Leave archers out of a scenario unless they are the point, or put them down after skipping. A
possession that makes its bearer an archer (a bow) is the same: `AttachCardsTo` it after `SkipToAssignments()`
(Long Battle Bow 11_89 in Little Golden Flower 65_148's tests), or for a regroup-phase test after
`SkipToPhase(Phase.REGROUP)` and before the first pass (Elven Bow 1_41 in Isengard Smith 3_60's tests). An archer MINION wanted in the skirmishes goes
down between the two phases: `SkipToPhase(Phase.ASSIGNMENT)` with another minion on the table, `MoveMinionsToTable(archer)`,
`PassAssignmentActions()`; after `SkipToAssignments()` the assignment already asked does not list it
(Goblin Archer 1_172 in mechanics/keywords/DamageBonusTests). A Free Peoples archer COMPANION (Legolas 1_50) fires
too, and so does one laid only to be spotted (Legolas 1_51 as the Elf of Durin's Secret 1_71: take Haldir 1_48, no archer; Card_06_041_Tests), and so does an archer Elf ALLY at his home site (Orophin 1_56, Rúmil 1_57, home 6F; Celeborn 1_34 is the Elf ally who is no archer, dec-b12_11; away from home he adds nothing to the archery total, CR 4.2 "ally": Card_13_026_Tests): his wound is placed unasked only while exactly one minion is on the table, so any second minion goes down
after `SkipToPhase(Phase.ASSIGNMENT)` the same way, and the one minion down reads a wound (Border Defenses 1_32). That wound is silent: a skirmish board
that only wants an Elf takes an Elf who is no archer (helpers.md), or a bonus against a wounded minion (Valor 4_87)
and a count of exertions read wrong with no error (dec-b7_21).
An archer minion that must be on the table BEFORE the archery phase (for his own maneuver ability) is got past it
by `SkipToArcheryWounds()` and `FreepsChooseCard(companion)` (Lurtz 1_127 in Lurtz's Sword 2_43's R test; Lurtz
19_33 before `SkipToAssignments()`). A skipper stopped so fails with "halted in phase ARCHERY", from a bare
DecisionResultInvalidException that names no archer: that message means this paragraph.

**`SkipToPhase` declines optional triggers it meets on the way.** A trigger that is offered while the phase is
still the old one (an "each time the fellowship moves" trigger, offered before the fellowship phase is left) is
answered by the skipper's next pass, which declines it. The test arrives in the new phase with nothing pending
and nothing done. To test such a trigger, pass the phase yourself with `FreepsPass()` and
answer the trigger; keep cards with optional triggers out of other tests' scenarios, or put them down after
skipping. In the fellowship phase the skipper passes for the Free Peoples player alone, so a trigger offered
to the Shadow player there is never answered and the skipper stops ("halted in phase FELLOWSHIP ... after 20
attempts") without a word about the pending decision. From maneuver on it passes for both, and a trigger of
the Shadow player's (Ringwraith in Twilight 101_40, at the start of maneuver; Howdah 13_94, at the start of archery, on the way to `SkipToPhase(Phase.ASSIGNMENT)` or `SkipToAssignments()`: dec-b13_97) uses up a pass: the test arrives
with the Free Peoples player's pass in the new phase already spent and the Shadow player holding the decision,
and `FreepsAssignToMinions` then fails, saying that the Free Peoples player is not being asked anything and
what the Shadow player is being asked. A trigger at the start of the target phase itself is left pending, as a
test of it needs.
So the `-` of "at the start of each skirmish" is not read through a skipper (an offer declined on the way looks
like none made): stop in the maneuver phase and step with `PassCurrentPhaseActions()`, reading at the start of
archery and of assignment that the Free Peoples player is asked first with pool and wounds untouched (Card_15_159_Tests).
The same pass is spent with no trigger at all where the test's own `FreepsPass()` inside a skirmish left the
Shadow player holding the decision: `SkipToPhase(Phase.REGROUP)` then arrives with the Free Peoples player's
first regroup decision already passed and the Shadow player asked "Play Regroup action or Pass", so the bookend
is `AwaitingShadowRegroupPhaseActions()` (Race Text 94_11).
So too where the Shadow player holds an assignment-phase decision (the test's `FreepsPass()` at the assignment
actions, with minions unassigned): `SkipToPhase(Phase.REGROUP)` arrives at the Shadow player's "Play Regroup action
or Pass", and a `FreepsRefreshDecision()` then fails. To reach the Free Peoples player's first regroup decision,
write `ShadowPass(); FreepsDeclineAssignments(); ShadowDeclineAssignments();` (Card_07_015_Tests, dec-b7_12).
And in the maneuver phase: with the Shadow player holding a maneuver decision (the test's `FreepsPass()` after a
Shadow ability), `SkipToAssignments()` passes over the Free Peoples player's "Assign minions" and rests at the
Shadow player's leftover assignment; write `ShadowPass()` first (Card_12_097_Tests, dec-b12_94).
`SkipToOtherPlayersTurn()` does not decline what it ends on: an OPTIONAL start-of-turn trigger of the incoming
player is left pending when it returns (the phase reads BETWEEN_TURNS), for the test to take or decline
(Prized Lagan 68_110, the R check of "at the start of each turn, you may"). An optional "at the start of your
fellowship phase" trigger of player 2's is left pending the same way, with the phase reading FELLOWSHIP and
`ShadowHasOptionalTriggerAvailable()` true (a site's: Card_11_235_Tests, Card_11_242_Tests).
Nor does it get past an optional "at the start of the regroup phase" trigger of player 2's on player 2's own turn
(both decks hold the same Ring-bearer, Frodo 11_164): it leaves player 2 at his regroup actions. Write
`SkipToPhaseInverted(Phase.REGROUP)`, `ShadowDeclineOptionalTrigger()`, then `SkipToOtherPlayersTurn()`
(Card_11_164_Tests.TheOfferIsMadeAgainAtTheStartOfHisNextRegroupPhase; dec-b11_164).
Player 1's own optional start-of-regroup offer stops it the same way: called from the Shadow phase after a
second move, with a muster companion and a card in hand, it rests at the Free Peoples player's "Play Regroup
action or Pass" at that site, and a second call reaches player 2's fellowship phase (Card_13_030_Tests, dec-b13_30).

**A required start-of-turn trigger cheated in before `StartGame()` fires on turn 1, and two stop it short.** With two cards cheated in before `StartGame()`
that each have a required trigger at the start of the turn (Elrond 1_40 and Galadriel 1_45), the game is left
asking the Free Peoples player which goes first, and the next action fails ("<card> is not offering Free
Peoples Player a play.  Free Peoples Player is being asked "Required responses" ..."). Put the second such card
down after `StartGame()`, or answer the ordering with `FreepsResolveActionOrder(text)`. One such card alone is
carried out inside `StartGame()` unasked: Dwarven Heart 1_10 ("At the start of each of your turns, exert bearer")
cheated onto its bearer before it has exerted him when `StartGame()` returns, a wound the test did not lay; attach it
afterwards (Card_03_096_Tests; Card_06_075_Tests, dec-b6_74).

**An optional trigger at the start of the turn is the first thing asked.** A card with an optional
start-of-turn or start-of-fellowship trigger, put into play before `StartGame()`, leaves its offer pending when
`StartGame()` returns. Answer it, or put the card down afterwards. A card with no trigger at the start of the turn,
a minion included, may go down before `StartGame()` and is still on the table in the turn's later phases
(Card_91_013_Tests; probed): "minions go down after StartGame" is the habit of new tests, and an old file's order
is left as it is. `SkipToSite(n)` likewise arrives at site n with a start-of-fellowship offer pending and the scenario's small
draw deck drawn out by the reconciles; `SetFreepsHand()` and `SetFreepsDrawDeck(...)` under that offer keep it
(Card_03_001_Tests). Whether the offer is made at all is worked out once, as the phase begins: where it hangs on the
deck (`mustExist`), lay the deck after `StartGame()` and BEFORE the skip, the first card given on top, with eight
filler cards above the ones wanted for the turn-1 reconcile to draw (player 2's the same before turn 3, when he
draws too): Great East Road 33_56 at site 2, eight Goblin Patrol Troop 1_177 (Card_33_056_Tests, hobb-b33_12). `SkipToOtherPlayersTurn()` does the same: it stops at the incoming player's fellowship phase
with a site's optional start-of-fellowship offer pending, undeclined (East Wall of Rohan 15_190, turns 3 and 4:
Card_15_190_Tests, dec-b15_187). A card put down afterwards is not in the decision already pending: before its own fellowship
ability is read or used, call `FreepsRefreshDecision()` ("A board changed under a pending decision"; Galadriel
3_17, Card_03_017_Tests). Wounds or tokens cheated before `StartGame()` for such a trigger are read before `StartGame()`
as well: lint's `read-before` counts `StartGame()` as the game moving on, though the offer is all that is pending
(Card_07_081_Tests, dec-b7_77).

**`SkipToSite` plays whole turns, reconciling included.** A small draw deck is drawn into the players' hands on
the way. Call `StartGame()` first: every skipper called before the first turn fails at once with "<the skipper> was
called before the first turn began: call StartGame() first" (engine pass 44, RigFailureTests; dec-b11_259). `SkipToMovementDecision` and `SkipToOtherPlayersTurn` reconcile too: the Shadow player draws up to 8
before the second move is asked about (a hand emptied by `SetShadowHand()` earlier in that regroup phase is
refilled as well, before any "if the fellowship moves" effect puts cards in it: Safe Paths 6_44, dec-b6_42), so a test that turns on his hand at the second move empties his draw deck
first, and a reading that the Shadow player's hand is untouched across a regroup move goes by his discard pile or
the zones, not `GetShadowHandCount` (dec-b12_11). A test that needs cards in the draw deck afterwards puts them back with `MoveCardsToTopOfDeck`. A test
that needs a hand of a known size afterwards sets it: `SetFreepsHand(cards...)` / `SetShadowHand(cards...)` make
that player's hand exactly those cards, in that order, and put the rest of it on top of his draw deck, the whole hand where no card is named (Erebor 30_55's Shadow draw; G-0035;
`Card_10_012_ErrataTests`); never move the scenario's aliases away one by one. A hand count, or the maximum of a
choice from the hand ("any number of cards from hand"), read after the skip counts the cards drawn on the way
as well: with no `SetFreepsHand` it comes out above the cards the test put there (dec-b7_117, 7_117 and 7_120). The cards drawn on the way are the
scenario's own aliases, so a test that reads what is offered from a hand after the skip sets that hand first: a
cheaper card drawn on the way can make an offer the test meant to withhold (Dark Approach 12_162 was offered with
too little twilight for the Nazgûl named, for Lemenya 1_232 drawn on the way; Ghastly Host 7_179's "play a Nazgûl
from hand" was offered with none put in hand: dec-b7_177; Not Alone 12_39's move trigger, whose cost is a [gollum] event
from hand, was offered on its C- board for one drawn on the way: dec-b12_38). The cards named are taken from
wherever they are, play included: a companion cheated onto the table and named there goes back to hand, and the
companion count and the twilight a move adds drop with him (dec-b9_12, West Road 7_337). And as
the fellowship decision is already pending when it returns, a card made playable by cheating is not offered
until some legal action has been taken. Player 2's own fellowship-phase play is offered at once where his
companions, support area and hand were laid BEFORE `StartGame()`: after `SkipToOtherPlayersTurn()` read
`ShadowPlayAvailable(card)`, play with `ShadowPlayCard(card)`, and end on
`ShadowDecisionAvailable("Play Fellowship action or Pass")` (Card_94_020_Tests, Card_94_021_Tests).
`MoveCompanionsToTable(scn.GetShadowCard(x))` before `StartGame()` puts x in player 2's OWN fellowship, where he
can exert for a cost on that turn (Card_11_242_Tests, the last test).
The Free Peoples player reconciles at the end of his turn as well: after
`SkipToOtherPlayersTurn()` a deck of eight or fewer is all in player 1's hand (8 in hand, 0 in the deck), so a
draw of his on player 2's turn has cards put back on his deck first (Card_91_005_Tests). From a deck of nine,
which card is left is chance once the deck has been shuffled: a card of his that was NOT played is read on a
later turn as not in play (`assertNotInZone(Zone.FREE_CHARACTERS, card)`), never as still in the draw deck, and
one wanted in a deck then is put there with `MoveCardsToTopOfDeck` (Card_11_235_Tests, the three-turn test).
`SkipToSite(n)` rests in the fellowship phase at site n, and `SkipToMovementDecision()` after it has made that
turn's move: the fellowship is at n + 1, and the regroup move then leaves site n + 1 and not n. Read
`GetCurrentSiteNumber()` on each side of the move (two old 94_25 tests named the site one too low in their
comments, and one of them was the move from site 6 into region 3 without saying so). So does `SkipToPhase` to any
phase after the fellowship phase: a site's text in its own turn's maneuver, archery or skirmish phase wants
`SkipToSite(n - 1)` and then the skip (Card_01_354_Tests).

**A reconcile on the way discards from the front of the hand.** A player over the hand size at a reconcile on the way discards down to it from the front of his hand, however many cards that is (`PassReconcile(player)`, which every turn-skipper uses), so a test may put any number of cards in a hand before skipping. A test that reaches "Choose cards to discard down to 8" by its own passes answers it in one go: `scn.PassReconcile(scn.P1())`, or `FreepsChooseCards(...)` with exactly `FreepsGetChoiceMin()` cards. A card put in hand before `StartGame()` (with `false` too) can be lost so on the turns skipped before it is played: put it in hand on the turn it is played, then `FreepsRefreshDecision()` (Card_13_003_Tests, dec-b13_1).

**A reconcile restores first.** By the time a reconcile's discard decision is pending, the reconciling player's
hindered cards have been restored (CR 5.0 "reconcile"; Q-0059). That is so for both players' regroup reconciles
and for "reconcile your hand" effects. A restored card's standing discard has been carried out by then as well:
Andúril 7_79 has discarded the Bow taken up while it was hindered (Q-0223; `mechanics/hinder/HinderedBorneCardTests`).
A card with a standing "Discard <X> he bears" (4_94, 4_100, 7_79, 8_69, 17_24, 67_24, 102_77) discards an X the rig
lays beside it at the next action of the game: lay the X only where the test is about its going.

**A card's "reconcile your hand" asks what the regroup reconcile asks, in the middle of the phase.** With fewer
than 8 cards in hand it is one card choice of 0 to 1 (`ShadowChooseCard(card)` or `ShadowDeclineReconciliation()`)
and then the draw up to 8; with an empty hand nothing is asked; with more than 8 it is one choice of exactly
hand minus 8 cards, minimum and maximum equal, with no optional discard first and no declining (`ShadowChooseCards`).
Under Foot 60_52's tests read all three. With exactly 8 the one discard is still offered, and a card is drawn
for it if it is taken; a draw deck with too few cards gives what it has. `mechanics/game/ReconcilingTests` has each
hand size for both players, the Shadow player's reconcile in each regroup phase and the Free Peoples player's
only when he stays, and Careful Study 7_19 ("Regroup: ... discard 3 cards from hand to reconcile your hand") as
the plain card that brings a reconcile about: a card owes no test of the steps.

**"Each time you reconcile" is answered after the hand is made up.** The offer comes once the reconciling
player has discarded (or declined) and drawn to 8, so with the offer pending a hand laid at 6 reads 8 and the
draw deck two shorter; read both there and again after the answer. The other player's reconcile, which comes
first in the regroup phase, sets off nothing: read the offer as absent at the "another move" question. A board
for two reconciles in one regroup phase: Careful Study 7_19 (an Elf spotted, 3 cards discarded) mid-phase, and a
stay for the second. The tests of the companion with this text in set 13 lay both.

**A deck's order is set after `StartGame()`.** The game shuffles each deck to deal the opening hands, so a card
moved to the top or bottom of a draw deck before `StartGame()` does not stay there. `MoveCardsToTopOfDeck(a, b,
c)` puts each on top in turn, so the last one named is the top card. A skipper that plays whole turns
(`SkipToSite`, `SkipToOtherPlayersTurn`) then draws from that deck, so a card cheated into it before the skip is
likely in hand after it: lay a draw deck the test reads after the skip (City of the Dead 8_118's tests).
`GetFromTopOf...Deck(n)` and `GetFromBottomOf...Deck(n)` count from 1: 1 is the top (bottom) card itself, 2 the
one next to it; 0 is out of range (`PileProperties`; Card_01_200_Tests, dec-b1_194).

**Only the table-laying cheats take aliases.** `MoveCompanionsToTable` and `MoveMinionsToTable` take alias strings
as well as cards; `AttachCardsTo`, `MoveCardsToSupportArea`, `MoveCardsToDiscard` and `MoveCardsToHand` take cards
only (`scn.GetFreepsCard("x")`), and a string there does not compile. `MoveCardsToFreepsHand` and
`MoveCardsToShadowHand` are the reverse, alias strings only, and so are `MoveCardsToFreepsDiscard` and
`MoveCardsToShadowDiscard` (dec-b15_178); for cards already fetched use `MoveCardsToHand`, which
puts each card in its OWNER's hand, a Shadow card's in the Shadow hand (Card_03_049_Tests, dec-b3_45).

**The Ruling Ring interrupts skirmishes.** With the default Ring, a Ring-bearer about to take a wound in a
skirmish gets an optional response. `FreepsDeclineOptionalTrigger()` declines it. Its strength +1 is printed on
the Ring and holds at all times, not only in his own skirmish: Frodo 1_290 reads 4 in every phase (dec-b18_99), and so does any Frodo a scenario names as Ring-bearer (Old Bilbo's Heir 1_289, printed 3: dec-b5_13). Keep the Ring-bearer out of
skirmishes that are not about him. A board whose "other skirmish" must resolve with a wound puts a companion who is not the Ring-bearer in it
(dec-b7_290: a minion on Frodo read 0 wounds after the lost skirmish). Where a card's own text wounds the Ring-bearer in a skirmish, or a test has Frodo lose a skirmish and then goes on to `FreepsResolveSkirmish` the next (the Ring's "Use The One Ring" offer is the pending decision, not the choice of skirmish: dec-b17_93), give the table
The Great Ring 19_1 (`VirtualTableScenario.GreatRing`): it adds no strength and, while it is not worn, answers no
wound; its maneuver and skirmish abilities are used only on purpose (Card_09_031_ErrataTests). Its erratum 69_1
has no constant: give the table `"69_1"` as the ring id. The maneuver ability of either is offered only while
the Ring is not worn (F-0303).

**A companion beaten by double his strength is killed, not wounded, and so is a minion.** A helper minion chosen only to win
overwhelms: Goblin Patrol Troop (13) kills any companion of strength 6 or less, and a minion of 8 kills Dwarf
Guard (4). The companion is then in the dead pile and `GetWoundsOn` reads 0, which looks like a wound that never
came. For a loss the loser survives keep the winner under double: Dunlending Brigand 4_10 (8) against a
companion of 5 to 7, Isengard Servant 3_58 (7, a Regroup ability only) against one of 4 to 6 such as Gimli
1_13 (Card_15_009_Tests), Goblin Runner 1_178 (5) against one of 3 or 4, the Troop against Aragorn 1_89 (8).
The same the other way: a companion of twice a minion's strength kills it whatever its vitality, and that
includes a minion the card under test has made weaker (Uruk Savage 1_151 at 5 - 3 against Arwen 1_30, 6: 1_104).
Where D reads the weakened minion after its skirmish, pick one that stays above half: Goblin Patrol Troop 1_177 at 13 - 3.
A HELPER that lowers him does it too: First Level 7_92 borne by Southron Explorer 4_250 (5 - 1) lets Aragorn 1_89's 8
kill him in a skirmish he was meant to survive; put such a card on him only after his skirmish (dec-b7_121, 7_122).
A companion meant for the FIERCE round must lose the first without being overwhelmed: killed, he is gone, and a fierce
assignment that names him is refused with a bare `DecisionResultInvalidException` (Crazed Hillman 12_60 at 7 kills Sam
1_311 at 3; the Pursuer 4_190 at 5 only wounds him: Card_12_060_Tests, dec-b12_60).
A board copied from a sibling card whose bonus is damage where yours is strength can cross the line: Whatever End
15_10's Gimli at 9 against the Pursuer's 5 becomes 10 under No Quicker Path 15_65's +1, which kills the Pursuer
and leaves no fierce round (dec-b15_59). Add up the strengths again on every borrowed board.

**A second assignment or skirmish phase in one turn takes a second move.** To show something renew or end at the
next phase of the same name: after the skirmishes, `SkipToMovementDecision()`, `FreepsChooseToMove()`, put a
minion down (`MoveMinionsToTable`), `ShadowRefreshDecision()`, then `SkipToPhase(...)` as usual
(Card_07_080_ErrataTests). `SkipToMovementDecision()` does not resolve skirmishes: from "Choose next skirmish to
resolve" it fails ('halted in phase ASSIGNMENT'), so resolve each first with `FreepsResolveSkirmish(companion)` and
`PassSkirmishActions()` (Card_16_003_Tests; dec-b16_1). The fellowship is one site further on, and the game
waits at the Shadow phase of that site: `AwaitingShadowPhaseActions()` is the reading that ends a test on its
regroup move, which lint's bookend wants (Card_18_042_Tests; dec-b18_41). The Shadow player has reconciled inside
`SkipToMovementDecision()`: with cards in his draw deck his hand is larger at the second phase than it was at the
first, so a reading of his hand on each side of the move allows for the draw (Card_91_016_Tests reads 1, then 2). Ending that regroup with `PassCurrentPhaseActions()` in place of the skipper leaves the Shadow player's
reconcile unanswered, and `FreepsChooseToMove()` then fails ('Shadow Player is being asked Reconcile'): use the
skipper (dec-b4_164).
The same phase on the owner's NEXT turn (the R of "At the start of the <phase>", on turn 3): take the minions off
with `MoveCardsToDiscard`, `SkipToOtherPlayersTurn()` twice (player 2's turn, with no minion, has no archery or
skirmish), then lay them again (Card_14_004_Tests.TheArcheryPhaseOfTheOwnersNextTurnMakesEachCompanionAnArcherAgain; dec-b11_22).

**Who places archery wounds.** The fellowship's archery wounds are placed by the Shadow player on his minions
(`ShadowChooseCard` after `SkipToArcheryWounds()`), the minions' by the Free Peoples player on companions.
With a fellowship archery total above 0 and more than one minion to take it, `SkipToAssignments()` (and
`SkipToPhase(Phase.ASSIGNMENT)`) halts in the archery phase with a `DecisionResultInvalidException`: call
`SkipToArcheryWounds()` and `ShadowChooseCard(minion)` first (Aragorn's Bow 1_90).
An archer ally that a card lets take part in archery fire (Bard 30_22 under Baïn 32_15, Esgaroth Volunteers 32_17
under their own ability) adds to that total the same way, and `GetFreepsArcheryTotal()` read before and after
(0, then 1) shows the participation itself (hobb-b32_49).
With ONE minion in play the wound is placed on him unasked, and the archery phase ends straight into the
assignment phase (Hand Axe 2_10).
With both totals above 0 the Free Peoples player is asked FIRST (the minions' wounds on his companions), then the
Shadow player (the fellowship's on his minions): `FreepsChooseCard`, then `ShadowChooseCard` (ArcheryFireGameProcess;
Card_01_238_Tests). Both totals are taken once, as archery fire begins.

**The archery totals can be read in any phase.** `GetFreepsArcheryTotal()` and `GetShadowArcheryTotal()` give the
totals as they stand now, in the Shadow, skirmish or regroup phase as well, so a change "until the regroup phase"
is read as it is made and again in a later phase, with no archery phase between (Riddermark Tactician 13_133).

**Allies who take archery wounds instead of companions.** While Come Down's archery ability is in force (4_146,
54_146: the `AlliesTakeArcheryFireWoundsInsteadOfCompanions` flag), each of the minions' archery wounds is offered
to allies only, at home or away, for as long as an ally can take one. A wound no ally can take (none in play, or
the last one killed) is placed as usual, on a companion or a participating ally (Q-0136, F-0243). The allies only
take the wounds: an archer ally away from home adds nothing to the fellowship archery total (Q-0137, F-0244).
An ally a card bars from archery fire (a [rohan] ally at Streets of Edoras 4_340) is passed over: the wound goes
to another ally or, none being able to take it, as usual, and the ability is not offered while every ally in play
is barred (Q-0147, F-0275).

**An ally in a skirmish.** An ally can be assigned at its home site (CR 4.2 "ally"). On `FellowshipSites`, Elrond
(1_40, home site 3) can skirmish once the fellowship has moved to site 3: `SkipToSite(2)` and then a move. An ally whose home is on another
block's path needs that block's sites in the scenario (Gondorian Merchant 7_97, home 3T, on the Towers sites:
`SkipToSite(2)` and then the move). `SkipToSite(n)` stops in the fellowship phase at site n, so a
`SkipToAssignments` from there moves the fellowship on to n + 1 first.
No ally's home is a Shadows-block or King-block site (every `allyHome` is F, T or a Hobbit-set H): there an ally
skirmishes only by another card's text. One Whom Men Would Follow 1_109, played in the maneuver phase as
set-up, does it by legal play (`Card_11_232_ErrataTests`; helpers.md has its entry).
An ally a card says may not participate (a [rohan] ally at Streets of Edoras 4_340) takes no part whatever
other card has him participate or assigns a minion to him: he adds nothing to the archery total, is offered no
archery wound, is on neither player's assignment list, and an effect that would assign a minion to him assigns
nobody. The rest of that other card (a strength bonus) still reaches him (Q-0150, F-0281).

**One blueprint under two site keys: each key names a copy.** `put("site1", "18_138"); put("ruins", "18_138")`
puts two copies in each adventure deck, and `GetFreepsSite("site1")` and `GetFreepsSite("ruins")` are those two
(engine pass 24; until then the two keys were given one alias between them and looking up the other threw
NoSuchElementException). Which of two copies the GAME lays is not the test's to say: `StartGame(site)` and
`ShadowChooseCardBPFromSelection(site)` choose by blueprint, so read the site laid with `GetCurrentSite()` and
not by the alias (`virtualtable/FirstSiteTests`). `GetFreepsSite(String)` and
`GetShadowSite(String)` look for the alias and then for a title in the ADVENTURE DECK only: a site already laid
is not found by its title (NoSuchElementException, no message). Fetch a site before it is laid. Site 1 of a
numbered path is laid while the table is built, before the aliases are given out, so it has none:
`GetFreepsSite("site1")` throws; read it with `GetCurrentSite()` or `GetSite(1)`
(dec-b11_164; `GetFreepsSite("site2")` after the move answers because the copy laid is the Shadow player's).
`GetSite(n)` for a site not yet laid on the path answers null, and a reading of it (`HasKeyword`) then throws a
NullPointerException deep in ModifiersLogic: move there first and read `GetCurrentSite()` (dec-b15_25, 15_26).
The title is matched as a part of it, in lower case with the spaces out (`"mountdoom"` finds Mount Doom), so a
string that is no key of the map still answers with a site and nothing fails: fetch by the key, and give each
key the name of the site it holds (the old Card_15_193_Tests had Expanding Marshland under "Fangorn Glade").

**`FreepsDeclineSanctuaryHealing()` declines the sanctuary heals still asked, and nothing else.** It answers
"Sanctuary healing - Choose companion to heal" with no companion for as long as that is what player 1 is asked,
and stops at the first decision that is anything else: all five heals, the four left once one has been taken,
the three of Rath Dínen 100_1; the fellowship phase that follows is not passed. It fails if no sanctuary heal is
being asked when it is called (engine pass 24, `virtualtable/SanctuaryHealingTests`). Until then it sent five
empty answers whatever was asked, and the fifth passed the fellowship phase once a heal had been taken: the
landed tests that take some heals and decline the rest do it with `FreepsDeclineChoosing()` once for each that
remains (`Card_18_139_ErrataTests`, `Card_V0_001_ErrataTests`), which works as before. There is no Shadow twin:
on player 2's turn decline each with `ShadowDeclineChoosing()`. A required start-of-turn trigger of the same player
(Grimbeorn 14_6's heal) is ordered against the sanctuary heal first: answer "Required responses" with
`FreepsResolveActionOrder("Required trigger from")`, then call this (Card_14_006_Tests). The order is asked whether or not a
companion is wounded: with none wounded, nothing follows it and this is not called (Card_06_015_Tests,
Card_06_018_Tests, dec-b6_15).

**Sanctuary healing is asked at any site laid as site 3 or 6, and only when a companion is wounded.** The rule
makes a sanctuary of every site numbered 3 or 6 whatever its block, a Shadows-block site the Shadow player laid
there included. A turn that begins there asks one 0-to-1 choice for each heal ("Sanctuary healing - Choose
companion to heal - remaining heals: N"); with no companion wounded nothing is asked and the turn opens on
fellowship actions. Put the wounds on in the turn before (`Card_V0_001_ErrataTests`).
So a test that plays on into player 1's next turn with a companion still wounded, the fellowship at site 3 of
`FellowshipSites`, has `SkipToOtherPlayersTurn()` stop on that heal: `AwaitingFellowshipPhaseActions()` is false
until `FreepsDeclineSanctuaryHealing()` (Card_07_163_Tests, the turn-3 test; dec-b7_161).
`SkipToSite(3)` or `SkipToSite(6)` with a companion wounded stops on that heal as well, and a `FreepsPass()` sent
then moves nothing and fails nowhere: decline the heal first (Card_11_058_Tests, the turn-3 test; dec-b11_58).
A fellowship that only ARRIVES at a sanctuary is not healed in that turn, an ally is never among the choices, and
each player is asked at the start of his own turn for his own companions. The rule is shown in
mechanics/sites/SanctuaryTests (CR 4.2 "sanctuary", sentence by sentence): a card owes no test of it.
Under Race Text 94_7 the modifier's owner is asked with nobody wounded as well, when a burden is on: his
Ring-bearer may be chosen for the burden alone. For player 2's sanctuary, `AddBurdens` adds to the player whose
turn it is: put them on in his own turn before, and `SkipToOtherPlayersTurn()` stops on his healing decision
(`ShadowDecisionAvailable("Sanctuary healing")`, then `ShadowChooseCard`; Card_94_007_Tests).

**Whose site.** `GetCurrentSiteNumber()` (an `int`; `GetCurrentSite().getSiteNumber()` is an `Integer`, and
`assertEquals(5, ...)` of it does not compile, ambiguous between `long` and `Object`: dec-b12_38) answers for the player whose turn it is: on the other player's turn it
is his site, not the one player 1's fellowship reached. So after `FreepsChooseToStay()` it reads the next
mover's site (player 2's site 1 on turn 2; hobb-b33_12). One player's marker, whoever's turn it is, is
`FreepsGetSiteNumber()` (player 1's) and `ShadowGetSiteNumber()` (player 2's) (G-0053, engine pass 26): where a
text compares the two markers ("ahead on the adventure path", CR 4.2 "ahead"), read both at each step
(Card_13_003_Tests). Player 1's first move leaves him ahead of player 2's marker at site 1; to have him behind on
a later turn, let player 2 move twice first: `FreepsPass(); SkipToOtherPlayersTurn(); ShadowPass();
SkipToMovementDecision(); ShadowChooseToMove(); SkipToOtherPlayersTurn();` leaves player 1 at site 2 and player 2 at
site 3 on turn 3, and a card that must not be caught up in the moves is put in play after it (dec-b13_1). The card on the path is the copy of whoever laid it: from site 2 on, on
player 1's first turn, the Shadow player's. `GetCurrentSite()` and `GetSite(n)` return that card and
`GetFreepsSite(n)` a different one, so compare with `GetSite(n)` or by blueprint. `GetSite(n)` is null for a site not
yet laid (on `FellowshipSites`, any site past the fellowship's), and the null fails further on with no rig
message (`GetTwilightCost`: a NullPointerException in `ModifiersLogic.getCurrentTwilightCost`; dec-b4_29);
`GetFreepsSite(n)` and `GetShadowSite(n)` fall back to the player's adventure deck. A site's own trigger offer is
read on that card too: `FreepsHasOptionalTriggerAvailable(GetFreepsSite(3))` reads false while the trigger of
the copy laid there is offered, so read it on `GetCurrentSite()` after the move (Card_03_119_Tests). It also gives a site's
trigger on "the fellowship" a board that tells the mover from the site's owner: read both players' burdens
(Card_03_116_Tests). A site's `player: free people` trigger with `on: your,companion` takes "your" as the Free
Peoples player of the turn, not the site's owner: the second player at the first player's Trollshaw Forest 11_260
is the one offered the draw (Card_11_260_Tests). The same holds with no `player:` line: Starkhorn 12_193's
`filter: your,dwarf` on the Shadow player's copy is the Free Peoples player's Dwarf, and the draw is offered to him
alone (Card_12_193_Tests, dec-b12_190).
`GetSite(n)` is null for a site not yet laid (site 6 on turn 1), and `HasKeyword(null, ...)` then throws: read a
site's terrain off the path only once the fellowship has been there. Which player a site's optional trigger is
offered to: "A meta-site's optional trigger is offered to every player", its last paragraph.

**At the move limit, the regroup phase ends the turn.** When the fellowship has moved as often as the move limit
allows, both players passing in the regroup phase ends the turn at once and the other player's "Play Fellowship
action or Pass" decision is next; no "another move" question is asked. As the turn ends (at the limit, or after
`FreepsChooseToStay()` below it) the Shadow player reconciles first, still in the REGROUP phase, where his hand
holds cards ("Reconcile - choose card to discard or press DONE", `ShadowDeclineReconciliation()`), and then the
Free Peoples player: with cards in hand he is asked
"Reconcile" (`FreepsDeclineReconciliation()`) before the next turn's decision can be read (Card_07_355_Tests). An
`endOfTurn` trigger comes after both (Card_06_026_Tests: Free Peoples hand empty, Shadow hand not). `SkipToMovementDecision()` called at the limit stops there too, at that "Reconcile" and not at the other player's fellowship decision (Card_08_099_Tests, the limit lowered to 1); where neither hand holds a card there is nothing to reconcile, and it goes on to the other player's "Play Fellowship action or Pass", so a `FreepsChooseToStay()` after it fails (Card_15_052_Tests). An "at the end of each turn" trigger (`endOfTurn`) is offered only after that reconcile: on turn 1 with an empty hand `FreepsChooseToStay()` leads straight to its "Optional responses", while on turn 3 the hand turn 1's reconcile drew is reconciled first (Citadel of Minas Tirith 3_40's tests). Below the limit the Free Peoples
player is asked "Do you want to make another move?". Either comes only after the Shadow player's reconciliation:
with cards in his hand he is first asked "Reconcile - choose card to discard or press DONE"
(`ShadowDeclineReconciliation()`); with an empty hand nothing is asked (Radagast 59_26's tests). An empty hand
does not stay empty over a second move: where his draw deck holds cards the Shadow player draws at the reconcile
inside `SkipToMovementDecision()`, so the last regroup phase asks him to reconcile again before the turn ends
and the minions are discarded (Mountain-troll 65_112's tests). So a test of a regroup-move trigger that reads or spends the Shadow hand empties his draw deck first, `SetShadowDrawDeck()` with no cards, or the reconcile before the move refills the hand (Card_04_213_Tests). The old Race to Mount Doom double-move tests reach
the second move by `SkipToPhase(Phase.REGROUP); PassCurrentPhaseActions(); FreepsChooseToMove()` over empty hands
(a one-card deck, its other copy put in the discard pile): one more card in the deck and the last call fails on
the Shadow player's reconcile. Write `SkipToMovementDecision()` for the first two (92_11, 92_14).
A mover who is barred from choosing to move (Race Text 94_5, `CantMove`; Eastfold 4_331's Shadow action) is not asked either, and
`SkipToMovementDecision()` does not stop where its name says: with a hand he is left at his own "Reconcile"
decision, still in the regroup phase with `GetMoveCount()` 1 of `GetMoveLimit()` 2; with an empty hand the
skipper runs on into the other player's fellowship phase, where an `assertFalse` of "another move" shows
nothing. With player 1 as the mover, a card put in his hand (`MoveCardsToHand`) is enough to stop the skipper
at his "Reconcile" (Shores of Nen Hithoel 1_359's tests).
A Shadow Response to his own regroup reconcile (Ring of Rancor 9_44, "If a player reconciles") stops
`SkipToMovementDecision()` short too: at the Shadow player's "Optional responses", ahead of "another move";
answer it (`ShadowDeclineOptionalTrigger()`) and the question follows (Card_09_044_Tests).
Keep an undrawn card in the mover's deck and read the count, the limit and the reconcile
(Card_94_005_Tests).
At that reconcile after `FreepsChooseToStay()` the regroup phase is still the current phase and the pool is
unchanged; the phase ends, and an end-of-regroup trigger is asked, only after `FreepsDeclineReconciliation()`, by
when the pool reads 0 (a probe of dec-b4_236; Card_04_237_Tests reads its stay board there).
The trigger `FPDecidedToStay` (Very Nice Friends 7_76, "If you do not move again this turn", its only user) fires
both ways: at a "no" to another move, and when the turn ends at the move limit with no question asked. Either
way it comes before the mover's own reconcile, so a hand it discards is drawn afresh (Card_07_076_Tests; whether
that order is meant is Q-0253).

**The move limit never reads below 1.** The engine floors it (`ModifiersLogic.getMoveLimit`), so from the base
of 2 a penalty of 1 and a penalty of 2 both read 1, and a "move limit is -1" card whose every board starts from 2
passes with its amount one off. Raise the limit first by legal play and read the sum: Durin's Secret 1_71 (a
Fellowship event: spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1), then 2 + 1 - 1 reads 2
and a penalty of 2 would read 1 (Card_01_260_Tests and Card_01_142_Tests for "for this turn"; Card_91_008_Tests).
A card that prints "(to a minimum of 1)" (Company of Haradrim 5_71) says what the engine does anyway: no board tells
its floor from the rule's, so the ledger lists no check for it and says so in Notes.

**`until: end(regroup)` ends as a regroup move is finished, not with the turn.** When the fellowship moves again
the engine runs the move and THEN ends the regroup phase (`FellowshipPlayerChoosesToMoveOrStayGameProcess`), so
an effect added earlier in the turn lapses just after the second move and one added on that move lapses at once.
A change that is to last the turn writes `until: endOfTurn`; read it in the Shadow phase after a second move
(F-0317, 93_3: the limit read 3 up to the question of another move and 2 after the move).

**An effect with no `until` ends with the phase it began in, and each skirmish is a phase of its own.** An
`addKeyword` (or any effect taking `until`) with none lasts to `end(current)` (Q-0018): a keyword gained in a
skirmish is gone as that skirmish ends, before the next skirmish or the regroup phase. So a "gains X until <a
later phase>" written with no `until` passes every reading taken in the phase it began in; read it in the later
phase (Stewards' Legacy 13_75, F-0511). The Shadow phase is the Shadow player's alone, so his `ShadowPass` there ends it: read "still in force" at a Shadow decision before that pass (Assault Commander 13_158). And a `-` taken as a skirmish wound shows nothing of a mistaken trigger
whose effect has that default: take the neighbouring event in a phase that lasts as well, a wound in the regroup
phase by Morgul Slayer 3_93 (Card_13_075_Tests.DenethorWoundedInALostSkirmishAndByAWoundInTheRegroupPhaseHasNoMuster).
A standing `constantlyCheck` whose condition is laid before `StartGame()` fires as the game starts, and its
modifier with no `until` held the whole turn there; only the condition brought about by a play in the fellowship
phase showed the lapse (Riding Like the Wind 11_155, F-0608: the second mount played, the limit read after the move).

**`SkipToOtherPlayersTurn()` straight after `SkipToMovementDecision()` throws.** It calls
`SkipToMovementDecision()` again and passes into the "another move" question: a bare
`DecisionResultInvalidException`. To read the board at the move decision and then end the turn:
`SkipToMovementDecision()`, the assertions, `FreepsChooseToStay()`, `PassReconcile(P1())`; the other player's
"Play Fellowship action or Pass" is then pending (Card_19_024_ErrataTests). The skipper begins with
`SkipToMovementDecision()` whatever the state, so call it only from before the turn's move decision: after
`FreepsChooseToStay()` it ran on into player 2's SHADOW phase (dec-b9_16).

**A card discarded for where the fellowship is goes when the move is finished.** "Discard ... when at an
underground site", "if underground", "if not at a marsh" (a `ConstantlyCheck` with a `Location` requirement) is
not looked at between the moving of the marker and the adding of the move's twilight. At a decision raised by a
"when the fellowship moves to" text the card is still in play; its text counts in the twilight the move adds
(Bill the Pony's -1 at the underground site that discards him); and it is in the discard pile before anyone is
asked anything further. Outside a move the check acts at once (`mechanics/sites/AtASiteWhenTheMoveIsFinishedTests`,
F-0233).

**Inside an "each time the fellowship moves" trigger the marker is still on the site left.** The engine raises
"moved from", then "fellowship moves", then moves the marker, then raises "moved to" and adds the move's twilight
(`MovementGameProcess`), as CR 4.2 "moving your fellowship" lists the steps (Q-0184; engine pass 21, before which
the marker moved first). At a decision raised by a `Moves` trigger `GetCurrentSiteNumber()` is the site being
left, `GetMoveCount()` does not yet count the move, the text of the site left is the one in force, and a
`Location` requirement in that trigger's block reads that site (`mechanics/sites/StepsOfAMoveTests`; Race Text 92_18).
The pool is the one from before the move too: "each time the fellowship moves, remove (2)" (Aragorn 3_38) on
turn 1 removes nothing from an empty pool, and the move's twilight is then added in full. `SetTwilight(3)`
first, and spell the sum: 3 - 2 + 2 for the site + 2 companions.
A `moves` trigger that ADDS twilight has its `-` board in the one non-move, the Free Peoples player staying at
the regroup move decision, only with a card in his hand: with an empty hand the stay ends the turn at once and
the pool reads 0 (5 at the decision, 0 after `FreepsChooseToStay()`, as The Ring Draws Them 3_83's `no -:` line
found); with one card (`SetFreepsHand(card)` before the stay) he is asked "Reconcile" first, and the pool read at
`FreepsDecisionAvailable("Reconcile")` is the pool of the decision (probed: 3 and 3; Card_03_102_Tests,
StayingAtTheRegroupDecisionAddsNothing; dec-b3_102). An effect that outlasts the turn, as wounds do (1_281),
needs no hand.

**A draw deck runs short in a long file.** Every Free Peoples card a scenario names and a test does not place
sits in the Free Peoples draw deck. A "discard the top N cards" with few such cards can find fewer than N by a
second use, and a choice that cannot be carried out in full then disappears (CR 4.2 "fully capable"). Read the
deck's size where a discard or a draw must be possible. Both players' decks hold every alias, so an alias added
to a shared `Get...Scenario()` moves every hand and deck count the file's other tests read by one (60_52: 15 and
7 became 16 and 8); where the file reads such counts, give a filler alias the new card's id instead. So a deck
count is read before and after the act, never as the number of cards the test means to have put there (a deck
read as "one filler" held all 8 aliases left unplaced: dec-b7_92).
A discard-pile count moves with everything else the act sends there: where "discard a condition" takes the Free
Peoples player's own, `GetFreepsDiscardCount()` is one more than the cards a deck-discard cost milled (11_118).
A test that EMPTIES a deck by moving the aliases left in it to the discard pile one by one is caught the same
way: the new alias stays in the "empty" deck (Card_09_038_Tests gained two cards for one test and two other
tests' lists had to name them). `SetFreepsDrawDeck()` / `SetShadowDrawDeck()` with no card empties a deck, and with
cards makes it exactly those, top first; the rest are set aside in no pile (engine pass 32; Card_08_032_Tests).
Call it after `StartGame()`, whose deal shuffles the deck. Read `GetFreepsDeckCount()` as 0 before anything rests
on the empty deck.
The Shadow player's deck runs short too: the turns `SkipToSite` plays draw a small one into his hand (the
entry on `SkipToSite`, above), so a board taken from an erratum's file whose text never read that deck can find nothing in it
(Card_01_023_Tests puts four cards back before "discard 3 cards from the top of his or her draw deck").
`SkipToOtherPlayersTurn` is the same: a small scenario's second player can start his first turn with nothing in
his deck, so a draw read there wants `SetShadowDrawDeck(...)` first (Card_11_260_Tests, on a Shadows-block path).
`SkipToSite(n)` too, in a single test: the turns it plays draw the Shadow deck down, and `GetShadowTopOfDeck()` can
return null; put the card to be drawn back with `MoveCardsToTopOfDeck` after the skip (Card_11_206_Tests, dec-b11_203).
The Free Peoples deck is drawn down the same way by the reconciles of the turns skipped: `SkipToSite(6)` with four
aliases left in it left one card, and a draw at the next move drew nothing; refill it with `MoveCardsToTopOfDeck`
after the skip (Card_17_013_Tests, dec-b17_12).
A short scenario runs short as well. `SetFreepsHand(...)` after a skipper puts the rest of the hand back on the
draw deck, and once a test moves the last of those into hand or play a "draw a card" draws nothing and the hand
reads one short: keep one named card in the deck and read it in hand after the draw (Card_01_060_Tests, Gimli).
A single test that lays every alias on the table empties the deck the same way, so that a draw trigger looks like
it never fired (Card_01_206_Tests keeps two spare aliases in the Shadow deck for its six-card board), and so does
one that moves all of a player's aliases into his hand, on turn 1 as well (Card_12_192_Tests, two fillers added).
A "draw N" wants N or more cards left in that deck, and one more to tell N from N+1: each alias is one copy, so a
small scenario adds filler aliases for it (Card_11_207_Tests' four Goblin Patrol Troops 1_177 for "draw 3 cards").
A deck of exactly the cards to be drawn passes a draw of MORE: lay one card beyond what the effect draws and read
it still in the deck after (Seeking Its Master 1_276's `count: 2` survived until a second Orc War Band lay beneath
the first: `MoveCardsToTopOfDeck(spare, drawn)`, dec-b1_276).
A second Shadow phase in one test is drawn short the same way: the reconcile at the regroup decision draws the Shadow
hand up to 8, so a large board can leave his deck empty when the second phase's draw comes (Orc Ambusher 33_48's
eleven-card board with six minions laid drew nothing, which read like a limit that had not renewed: hobb-b33_49).
**A board copied from an erratum's file rests on the erratum's numbers.** Where the original differs in a number the
board leans on (burdens, resistance, vitality, strength), re-count each copied test under the original's: Boromir
9_31 adds 3 burdens at resistance 6 where 59_31 adds 2 at 7, so the erratum's fierce-round test corrupts or kills him.

**A random or whole-hand effect takes whatever is there.** Where a test sets off anything that acts on a hand or
a deck by chance or as a whole (a random look, reveal or discard from a hand; a discard "for each X revealed"; a
shuffle; the reconciles of a turn-skipper drawing from a small deck), the card under test, and every card a later
count depends on, is kept out of that hand or deck: put it somewhere fixed at the start of the scenario, the
discard pile say (`MoveCardsToDiscard(card)`), and bring it in from there when it is wanted (Ketura on F-0264:
"then it is guaranteed to not interfere with the rest of it"). A test that passes seven runs in eight is this:
the card under test in the hand The Mirror of Galadriel discarded from (F-0264, 52_44); a fifth card of the
culture left in a deck the reconciles drew from (F-0265, 58_20). A `SkipToSite(n)` board deals the Shadow hand
from a deck that holds every scenario card, so a "not offered" reading of a cost that plays a card from hand wants
every card that could pay it put in the discard pile at the start (Burning of Westfold 4_144: an Uruk Savage drawn
made the ability payable; dec-b4_139). Run a test that has such an effect twenty times
before it is called finished. Where the text then acts on THAT card ("reveal a card at random ... discard that
card"), a hand of one card, or of copies of one, cannot tell it from another card taken at random: lay several
different cards, read which was shown (`FreepsHasCardChoiceAvailable(card, false)` for each, before the
dismissal), and assert of each that it went if it was shown and stayed if it was not. True of a right card on
every run; with six cards a random discard in its place fails five runs in six
(Card_03_033_Tests.DiscardsTheCardRevealedAndNotAnotherCardOfTheHand). That the pick is RANDOM no board shows (a
pick of the same card every time reads the same): read which card went and that the others stayed, run it the
twenty times, and say in Notes that the chance itself is not shown (Card_15_050_Tests). A card placed that may be
either of two is read `assertTrue(List.of(troop, runner).contains(placed))`, then `assertInHand(placed == troop ?
runner : troop)`: an `||` inside `assertTrue` draws lint's style warning (Card_07_237_Tests). Where what hangs on the card shown is an
OFFER and not where a card went, no answer stands under an `if` (lint's `conditional`): the test ends on two
value assertions and answers nothing, `assertEquals(shown, scn.ShadowPlayAvailable(card));
assertEquals(!shown, scn.AwaitingFellowshipPhaseActions());`
(Card_05_096_Tests.WhenOneCardOfTheHandIsRevealedAtRandomTheEyeIsOfferedOnlyIfItWasTheCardShown).
Where it is a NUMBER to choose (the bounds of a heal), the test reads the bounds and ends on the pending decision's
text, `assertTrue(scn.FreepsDecisionAvailable("Choose cards to heal"))`, which lint's bookend rule takes
(Card_03_043_Tests.TheNumberHealedIsTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand).
A random DISCARD that shows nothing ("discards 2 cards at random from hand", Tower Lieutenant 1_280) is read by
counts: the hand down by N, the discard pile up by N, the draw deck unchanged and none of the hand's cards in it,
and another player's hand untouched (Card_01_280_Tests). Its cards go one at a time, and a trigger on a discard from
hand is asked after EACH, before the next goes: at the first offer the hand of 3 reads 2, at the second 1
(Card_03_111_Tests.TwoCardsDiscardedAtRandomAtOnceOfferItTwice).

**The fierce round reads as the first does.** After a skirmish that leaves a fierce minion in play,
`GetCurrentPhase()` is `Phase.ASSIGNMENT` again and `AwaitingFreepsAssignmentPhaseActions()` is true, exactly as
the first time. The `Phase` enum has no fierce values: `Phase.FIERCE_ASSIGNMENT` does not compile (dec-b15_43). `InFierceRound()` tells them apart (G-0031): true from the game's "Fierce skirmishes" until the
last fierce skirmish is over, and already true straight after the `PassSkirmishActions()` that ends the last normal skirmish, the place to read a D of "until the regroup phase" (Card_18_060_Tests). It is no reading of what the game is
waiting for: lint's bookend check wants `AwaitingFreepsAssignmentPhaseActions()` beside it (dec-b17_68). A "this phase" effect from the first assignment phase has ended by then.
A text whose bonus is "if in a fierce skirmish" owes a card in NO skirmish chosen during another's fierce skirmish:
a definition that reads the phase gives it the bonus (shapes.md, F-0550; Card_02_037_Tests).
The assignment actions are pending again too, and nothing has passed them as `SkipToAssignments()` did the first
time: call `PassFierceAssignmentActions()` before `FreepsAssignToMinions`, or the assignment is refused with a
bare `DecisionResultInvalidException` (Card_09_031_ErrataTests), or one that says only that the Shadow player is
being asked something; `PassCurrentPhaseActions()` does the same (Card_12_085_Tests, dec-b12_82).
Once the fierce assignment is made (and any trigger on it answered), the game rests at the Free Peoples
player's "Choose next skirmish to resolve", read as `FreepsDecisionAvailable("Choose next skirmish to resolve")`,
not at `AwaitingFreepsSkirmishPhaseActions()` (Card_11_219_Tests).
In either round the Free Peoples player may assign a companion to one minion only (more with defender: "Who is
offered for assignment"), so a board with several minions on one companion gives him one and has the Shadow
player assign the rest after him, `ShadowAssignToMinions(companion, m2, m3)` (Card_04_181_Tests).
In the first round it is the other way about: `SkipToAssignments()` is `SkipToPhase(Phase.ASSIGNMENT)` and then
the assignment actions passed, so it rests at "Assign minions"; a `PassAssignmentActions()` after it passes the
assignment itself by, and a later `FreepsAssignToMinions` is refused (dec-b10_23, Shelob 10_23).
After `SkipToArcheryWounds()` and the wounds placed (any trigger they set off answered), the game stands at the
assignment actions, not "Assign minions": read `AwaitingFreepsAssignmentPhaseActions()` there (Card_32_053_Tests, hobb-b32_53).
Called while a phase's action choice is still pending (the Shadow player's archery actions, after a `FreepsPass()`),
`SkipToAssignments()` passes the Free Peoples assignment too and rests at the Shadow player's "Assign minions";
`ShadowPass()` first (Card_12_075_Tests, dec-b12_72).
The round comes about with nobody assigned at all: where a fierce minion is in play and both players decline to
assign, the fierce assignment phase follows, which is the cheapest way to stand just before the regroup phase
(a D check on "until the regroup phase", Prized Lagan 68_110). It comes too where other minions fought and the
fierce one was left unassigned (Great Hill Troll 8_102): a reading "at the first regroup decision" after the last
normal skirmish wants `SkipToPhase(Phase.REGROUP)`, not a pass (Card_07_267_Tests). A minion fierce only by its
own text counts as well: Morgul Hound 7_192 with 2 wounds on the Ring-bearer (Card_07_302_Tests, dec-b7_302).
The fierce assignment phase is an assignment phase: a "start of each assignment phase" trigger is offered
again there, a same-turn R board needing no second move (Card_13_059_Tests.TheFierceAssignmentPhaseOffersItAgain).
That fierce assignment phase opens on each player's "Play Assignment action or Pass": a second
`FreepsDeclineAssignments()` straight after the first pair lands on that decision and does nothing, so
`PassCurrentPhaseActions()` comes before the fierce round's declines (dec-b7_67, Card_07_068_Tests).
It is also the cheap board for the `R` of a character's own "each time <he> wins a skirmish": he beats a printed fierce minion (Uruk Pursuer 4_190) in both rounds of one turn, and the trigger is offered again in the second, with no second turn walked (Card_18_001_Tests, dec-b11_262).
That round is an assignment phase for triggers too: "at the start of each assignment phase" (`startOfPhase`
`assignment`) is offered again as it begins (Uruk-hai Healer 14_14, probe of dec-b14_13-l).

**A bonus "until the regroup phase" from a skirmish trigger is gone once the last skirmish resolves.** The regroup
phase begins at once, so a reading straight after the only (or last) skirmish shows the printed number, as if the
trigger never fired. That holds for a trigger on the last skirmish's own win too: the bonus on the Troll that
Olog-hai of Mordor 8_105 plays after the final fierce skirmish is gone as the trigger finishes, so its D reading
comes from an earlier win (Card_08_105_Tests). Read it with another skirmish still waiting ("Choose next skirmish to resolve"), in the first round or the
fierce one (a second firing in the fierce round wants a second fierce minion whose skirmish follows: Tom 31_58
with Wicked Spider 31_55, Card_31_058_Tests), and read it gone in the regroup phase (Card_01_263_Tests.EachLossToASauronOrcAddsAnotherPlusOneUntilTheRegroupPhase).
A REQUIRED win trigger of a card in play meets the loser's wound as a required-response ordering: answer
`FreepsResolveRuleFirst()` after the passes, or the wound stays unplaced and the skirmish looks unresolved
("The entries of a required-response ordering are actions"; Card_11_098_Tests).
The relentless round (CR 5.0 "relentless"; a third round after the fierce one, or a second where no minion is
fierce) reads as the first does too: `Phase.ASSIGNMENT`, the assignment actions
pending, `InFierceRound()` FALSE. `InRelentlessRound()` names it (G-0043): true from the game's "Relentless
skirmishes" until the last relentless skirmish is over, and false in the first round and in the fierce one. It is
passed with `PassAssignmentActions()` and `PassSkirmishActions()` (`mechanics/keywords/RelentlessTests`); the
`Fierce` ones do the same thing (each is `PassCurrentPhaseActions()`), so either works and the name is for the
reader (Card_02_101_Tests passes the relentless assignment with `PassFierceAssignmentActions()`).
The additional phase of The Witch-king's Beast 12_184 (`setupExtraAssignmentAndSkirmishes`) is his alone and is
neither round: both readings are false in it, and no keyword makes it (Q-0267). With a relentless minion in play
the two are one round, `InRelentlessRound()` true, with the relentless minions and The Witch-king in it.

**A standing modifier on "each minion skirmishing X" stops reaching a minion the moment that minion's skirmish resolves.**
`inSkirmishAgainst(...)` reads only the skirmish being fought, so a minion assigned but not yet skirmishing, and one
whose skirmish is over, read the printed number again in later skirmishes (Ancient Roads 6_49: Gollum 5_24 reads 3,
then 5 in his own skirmish, then 3 again; Card_06_049_Tests).

**Before `StartGame()` the burdens read 1.** The constructor has bid 1 burden for player 1 and placed the
Ring-bearers; `StartGame()` then takes 1 burden off (`RemoveBurdens(1)`). A burden a card added at set-up would
show as 2 before it and 1 after (a Ring-bearer-capable companion who adds none when he starts as the Ring-bearer
reads 1 and 0; one who is no Ring-bearer and pays his burden reads 2 and 1).
That holds while player 1's starting fellowship is pending, which takes a companion in the decks. `GetBurdens()`
reads the player set-up has reached: with no companion in the decks the game has gone on to the mulligan question
and it reads PLAYER 2's bid (0 on a default table; with a bid handler of 7 and 2 it reads 2, and 7 where a
companion is in the decks). After `StartGame()` it is player 1's bid less 1 on every table (7 reads 6; 0 reads
0): read a bid there (Race Text 91_18), and player 2's on his own turn, where nothing has been taken off.
`FreepsGetBurdens()` and `ShadowGetBurdens()` (G-0044) read each player's own burdens wherever set-up or the turn
has got to: before `StartGame()` they are the two bids as bid.

**A skirmish, or any later phase, on player 2's turn: `SkipToPhaseInverted`.** The drive that works: `SkipToOtherPlayersTurn(); ShadowPass();` (player 2's
fellowship moves) `MoveMinionsToTable(<player 1's minion>); SkipToPhaseInverted(Phase.ASSIGNMENT);
PassCurrentPhaseActions(); ShadowAssignToMinions(companion, minion); ShadowResolveSkirmish(companion);
ShadowPass();` and player 1, the Shadow player of that turn, then acts with the `Freeps...` methods
(Card_93_001_Tests). The skirmish decision reads "Choose action to play or Pass" for both seats on every turn
(on player 1's turn too, `ShadowDecisionAvailable("Skirmish")` reads false while the Shadow player is asked for
his skirmish action; the choice of the next skirmish is `FreepsAwaitingSkirmishChoice()`, dec-b1_83),
so the bookend there is the seat's own `AwaitingFreepsSkirmishPhaseActions()` or
`AwaitingShadowSkirmishPhaseActions()`: the "Play <phase> action or Pass" reading that the fellowship and
Shadow phases need on player 2's turn matches nothing in a skirmish. Any later phase of player 2's turn is reached
the same way, `SkipToPhaseInverted(Phase.REGROUP)` and the like (`SkipToPhase` is for player 1's turns: on
player 2's it fails with "Attempting to skip to phase REGROUP, halted in phase SHADOW"); the
bookend in the regroup phase there is `AwaitingShadowRegroupPhaseActions()`, player 2 being asked first
(Card_93_010_Tests, Card_92_012_Tests).

**The default board gives the Shadow player initiative.** After StartGame the Free Peoples hand holds no cards, so
`FreepsHasInitiative()` is false until four are laid there. A Shadow "if you have initiative" card is read under
initiative on the bare board; its other side wants four cards in the Free Peoples hand, its threshold three
(Card_07_139_Tests, Card_07_140_Tests). Cards `MoveCardsToHand` put there before `StartGame()` stay, and the
dealt hand goes back on the deck; `StartGame(false)` keeps the dealt hand beside them (3 laid read 5,
Card_07_205_Tests, dec-b7_203), so for an exact hand count use `StartGame()` and read the count. That empty hand
is turn 1's alone: after any `SkipToSite` the reconciles have refilled it, so a test that turns on initiative lays
the hand with `SetFreepsHand(...)` (with no arguments it empties it), not `MoveCardsToHand` (Card_07_271_Tests).

**"Each time a companion exerts" on a MINION is answered only while the minion is in play.** A fellowship-phase
board copied from a condition's tests (Demoralized 11_114) has no minion on the table. Exert the companion in the
maneuver phase (Mumak Commander 15_86's ability), the skirmish phase (Gimli 1_13's own) or the regroup phase (an
ally's ability, Calaglin 3_10) instead (Card_11_181_Tests).

## Cards, owners and zones

**A getter's player word is not always in one place.** Some put `Freeps`/`Shadow` first (`FreepsGetBurdens()`,
`ShadowGetChoiceMin()`), some after `Get` (`GetFreepsCard`, `GetFreepsSite`), and some have none (`GetWoundsOn`,
`GetResistance`): `GetFreepsBurdens()` does not compile (dec-b11_126). Take the name from `vts-api.md`, not by analogy.

**`name(...)` in a definition ignores accents, capitals and spaces.** `Filters.name` compares both sides through
`Names.SanitizeName`, so `name(Theoden)` takes Théoden as `name(Théoden)` does (Ride With Me 13_134 writes one in its
requirement and the other in its choice; dec-b13_134). A definition's accentless name is no finding.

**A draw deck holds only its owner's cards.** `SetFreepsDrawDeck` and `SetShadowDrawDeck` refuse the other
player's copies: lay the Shadow deck from `GetShadowCard` copies (each scenario alias is dealt to both players;
dec-b12_7).

**There is no `assertInDeck`.** A card in a draw deck is read with `assertInZone(Zone.DECK, card)`, as `assertInHand`
and `assertInDiscard` are themselves written (`Card_06_116_Tests`). These zone checks, `assertAttachedTo` among them,
are static methods of `framework/Assertions`, not `scn` methods (there is no `scn.IsAttachedTo`): the file needs
`import static com.gempukku.lotro.framework.Assertions.*;` (dec-b7_328's first compile failed for lack of it).
`assertAttachedTo(card, bearer)` takes the borne card first and one card a call: it is no varargs
`(bearer, cards...)` as `AttachCardsTo` is (dec-b15_83).

**Exerting is not wounding.** A character with one vitality left cannot exert and so cannot pay an exertion cost,
but can still be wounded. Nor is he offered to an "exert a ..." choice, whatever its filter: an invalid candidate
there has 2 vitality left or more, or he shows nothing (beside a Goblin Runner, vitality 1, "exert a companion"
built as `choose(character,not(ally))` passed; beside Goblin Patrol Troop 1_177, vitality 3, it fails: 92_13).
For "exert ... twice" he needs 3 left: Filibert Bolger 2_101 (vitality 2) read as not offered to his own "exert a
Hobbit companion twice" showed nothing of `companion`; Bilbo 1_284, a Hobbit ally of vitality 3, does (dec-b1_49).
An "exhaust a <kind>" choice leaves out those already exhausted too: Dunlending Brigand 4_10 (vitality 1) was not
offered beside a Man of vitality 4, who was exhausted unasked (Damrod 4_114, dec-b4_112).
The other way, an effect that WOUNDS read only on characters with vitality to spare cannot be told from an
exertion (`type: wound` made `type: exert` survived): lay one the wound kills, as Dunlending Brigand 4_10 (minion,
vitality 1, no text) beside the rest of "wound every minion" (Card_01_111_Tests, dec-b1_106). Likewise an EFFECT
that exerts, read only on characters with vitality to spare, cannot be told from a wound (`type: exert` made `type:
wound` survived): lay one with a single vitality left of the kind it reaches, read not offered (a choice) or not
exerted and still in play ("each ... must exert"): Ent Avenger 6_27 at 3 wounds beside the Gandalfs of No Ordinary
Storm 1_130, Merry 1_302 at 3 wounds among the Hobbits of Saruman's Chill 1_134 (dec-b1_130).

**Allies live in the support area.** `MoveAlliesToTable` puts allies there, where an ally played legally goes.
`MoveCompanionsToTable` takes companions only and fails the test if handed anything else, as `MoveAlliesToTable`
does if handed anything but an ally.

**An ally is at his home site, not the fellowship's** (F-0518, engine pass 48). A text that names a character's
place (`atSite(...)`: "while bearer is at a plains site") reads an ally at each of his home sites laid on the path:
Guma 4_277 is at a plains at Westemnet Hills with the fellowship at White Rocks, and the Rohirrim Herdsman 7_247 is
at Barrows of Edoras with the fellowship at Westemnet Hills. A home not yet laid (site 4 while the fellowship is at
site 2), another block's path and a Shadows path leave him at no site. A board for "an ally at the terrain" lays
his home site as that terrain; one for "not" lays the fellowship at the terrain and the ally's home elsewhere
(mechanics/sites/WhereACharacterIsTests).

**A play read as not offered for a card that is not in hand shows nothing.** `GetFreepsCard` and `GetShadowCard`
leave the card in its owner's draw deck, and there `FreepsPlayAvailable(card)` answers false whatever the card
under test does (probed: Barliman Butterbur 1_70 false from the deck and true from the hand, with nothing on the
board against him). Move the card to hand before its refusal is read, and read the same card as offered on the
control board. Race Text 91_15's old test read Barliman so; in an old file, look for the `MoveCardsToHand` that
goes with each `assertFalse(...PlayAvailable(x))`. lint warns `not-in-hand` where no cheat in the test takes the
card out of its draw deck (not for a card whose own text plays from a draw deck).
The reverse catches out too: after `SkipToSite(n)` the reconciles have DRAWN scenario aliases into both hands, and
`MoveCardsToHand` before `StartGame()` does not make the hand only those cards, so a "play a <kind> from hand" read
as withheld can be offered for a card drawn on the way: `SetShadowHand(...)` after the skip (Card_15_181_Tests, dec-b15_178).

**`AddBurdens`, `RemoveBurdens` and `GetBurdens` are the current player's.** They reach the Ring-bearer of the
player whose turn it is (`gameState().addBurdens`). One player's burdens are READ whoever's turn it is with
`FreepsGetBurdens()` and `ShadowGetBurdens()` (player 1's and player 2's; G-0044, engine pass 24); they are LAID
only for the player whose turn it is, so the other player's are laid on his own turn or come of his bid. "The
other player's burdens are not counted" landed without a board on that account, and no check was owed for it
(92_25); a later card that needs one lays the other player's burdens a turn earlier and reads both. Player 2's
Ring-bearer and burdens stay in play on player 1's turn: `StartGame(); SkipToOtherPlayersTurn(); AddBurdens(3);
SkipToOtherPlayersTurn();` is back in player 1's fellowship phase with `ShadowGetBurdens()` 3 and
`FreepsGetBurdens()` 0 (Sauron 9_48, "for each burden you spot", counts only the first: Card_09_048_Tests).
Threats are the same: each player's dead pile has its own, and `AddThreats` and `GetThreats()` are the current
player's (`GameState` keeps them by player). Nothing clears them at the end of a turn, but `GetThreats()` read in player 2's
turn reads player 2's dead pile: 0 after a turn ended on 2 of player 1's (Card_07_317_Tests, dec-b7_314).
A burden added as a turn ENDS (Thin and Stretched 1_279, "at the end of each turn") is not read by `GetBurdens()`
at the next decision, which is the other player's: read 0 at the mover's last decision ("another move"), end the
turn, and read `FreepsGetBurdens()` at the other player's first decision (Card_01_279_Tests).
`GetThreats()` is the current player's too (`GameState.getThreats` reads `getCurrentPlayerId()`), and the rig has
no per-player threat reading.

**Threats stop at the number of companions** (CR 4.2 "threats": none can be added while there are as many
threats as companions in play). A board that is to read N threats needs N companions: the control in
`Card_00_060_Tests` (War Towers 7_173's 1 threat, then the 2 of Sméagol 7_71's assignment ability) stands on three.
A companion whose own cost is a threat ("To play, add a threat": Cursed of Erech 10_26) is not counted while he
is being paid for: beside the Ring-bearer alone with 1 threat he is not offered, and with none he is
(`Card_10_026_Tests`). Threats laid by `AddThreats` after `StartGame()` are not in the pending choice of fellowship
actions: call `FreepsRefreshDecision()` before reading such a play (it read as playable at the limit: Card_08_041_Tests).

**The rig has no count of companions in play.** Nothing like a companion count is in vts-api.md; a board that turns
on "you can spot N companions" reads them by `assertInZone(Zone.FREE_CHARACTERS, ...)` over every companion, and an
ally beside them in `Zone.SUPPORT` (Card_11_081_Tests, Fletcher of Harad's six companions).

**Threat wounds are asked for one at a time, and the threats are gone before the first.** When a companion is
killed with threats on the table (with none, the rule does nothing and asks nothing: F-0567), the Shadow player first has 'Before threat wounds are placed - Optional "is
about to" responses' where a card answers it (Mordor Assassin 7_284: `ShadowHasOptionalTriggerAvailable(card)`).
Then `GetThreats()` reads 0 at once, and the Free Peoples player is asked "Choose characters to wound" once for
each threat there was, a choice of one companion each time (minimum 1). A threat removed in that window as a cost
is therefore read in the wounds asked for (2 threats, one removed: one question and one wound; declined: two),
never as "2 to 1" (`Card_07_284_Tests`). So too for an "about to be killed" trigger whose cost removes a threat and
whose effect asks nothing more (Legions of Morgul 7_283): the kill and the threat wounds follow inside the same rig
call, and `GetThreats()` reads 0 straight after `ShadowChooseOptionalTrigger`; Held Ground 7_181's test reads 1
there only because its choice of possessions is still pending. A companion a card's text places in the dead pile is killed and sets them
off (Saved From the Fire 8_20: the wounds are asked for before the deck is shown); a killed minion and a companion
discarded from play do not (mechanics/wounds/ThreatsTests). So a trigger that needs N threats and kills a companion
fires a second time only once the threats are laid again: N+1 companions and an `AddThreats` between the
skirmishes (Mûmak Commander 7_152, Card_07_152_Tests). An event whose effect so kills a companion with threats on the table (Breached
7_266, a companion placed in the dead pile) lies in `Zone.VOID` until the last threat wound is placed
(Card_07_266_Tests).

**Hunter and enduring change a strength a board may lean on.** A hunter has its printed strength everywhere but
in its own skirmish, and +X there only against a character who is no hunter (`GetKeywordCount(card,
Keyword.HUNTER)` is the X); hunters are on both sides. An enduring character is +2 for each wound on it, an
exertion's included, and loses it as a wound is healed: a helper that is wounded or exerts is stronger for it
(mechanics/keywords/HunterTests, EnduringTests).

**The Rule of 9 counts the dead pile, copy by copy.** With nine companions in play and in the dead pile together,
no companion is offered from hand whatever else he needs, and other cards are played as ever
(mechanics/game/RuleOfNineTests). `MoveCompanionsToTable` and `MoveCardsToDeadPile` do not ask.

**`MoveCardsToDeadPile` puts a card in its OWNER's dead pile.** A Shadow player's copy goes to his own pile, not
to "the dead pile" of a Decipher text (the Free Peoples player's, Q-0131); `assertInZone(Zone.DEAD, ...)` does not
tell the piles apart, and `GetFreepsDeadCount` does (Card_10_061_Tests, the Shadow player's Boromir 1_97; dec-b10_61).

**Damage bonuses add up, and "loses all damage bonuses" shuts later ones out** (CR 4.2 "damage bonus").
`GetKeywordCount(card, Keyword.DAMAGE)` is a character's bonus: his own, what a card in play gives him and what an
event gave him for the skirmish, added together. The winners place one wound and one more for each damage +1
among them, so two minions of damage +1 place three; a loser's bonus places nothing. So check D's skirmish for an
ability that grants damage +1 ends with 2 wounds on the minion beaten (Card_05_082_Tests). After "loses all damage
bonuses" the count reads 0, and a bonus given later does not land though its event is played and paid for
(Seek and Hide 3_112, then Lurtz's Battle Cry 1_128). The rule is shown in mechanics/keywords/DamageBonusTests: a card that
gives or takes a bonus reads the count, on the bearer and on a card outside the scope (the Standard, "Tests that are
not about a card": Ranger's Sword 1_112; Goblin Spearman 2_65), and fights a skirmish only where its own text or a
check D needs one (Card_05_082_Tests above), not the arithmetic again.

**The wounds of a lost skirmish are placed in rounds, one on every loser; a loser killed early leaves his own later wounds over.**
`ResolveSkirmishDamageAction` wounds each loser once, then each again, 1 + the winners' damage bonuses times in
all, and a kill is answered between two rounds: a loser who dies early has the later rounds "left over", which is
what More Yet to Come 10_3 assigns elsewhere. The share is that loser's own: the event reads and spends only the
wounds of the minion whose kill it answers, and the rounds go on for the losers still in play (a second minion
in that skirmish takes 2 whether the event is played or declined; F-0475, mended by engine pass 26). Two losers
killed by the same wound each leave their own, and a copy of the event assigns one minion's. An overwhelm has no
such action and leaves nothing over (CR 4.2 "overwhelmed"). A card that reads or spends something a whole
skirmish shares is tested with a second loser in the skirmish, read as untouched (`Card_10_003_Tests`).
A round no response can answer is carried out at once after the one answered: with a prevention taken on the first
of two wounds, the loser already has the second when the game next waits, so read the wounds after both rounds
(Messenger's Mount 12_72, Card_12_072_Tests, dec-b12_72).

**A site is controlled lowest first and only behind every marker; it is liberated highest first** (CR 4.2
"control", "liberate"). An effect that takes control of a site takes the lowest-numbered site that nobody
controls and that EVERY player's marker has moved beyond, and asks nothing; on the first turn of a game there is
none (the other marker is on site 1). An ability that takes control as its EFFECT and has a cost (Southron
Explorer 4_250: exert and remove (5)) is offered all the same and its cost is paid for nothing. The site is then
in the controller's support area (`assertInZone(Zone.SUPPORT, site)`, `ShadowControls(site)`), `GetSite(n)` still
answers with it, and it keeps its keywords (Uruk Camp 4_335 controlled is "a battleground you control") and not
its other text. It is still counted by "for each <terrain> site you can spot" (Brutality 11_180, Card_11_180_Tests). A liberation takes the highest-numbered site that an OPPONENT of the liberator controls, passing
over a site of his own, and asks nothing; the site goes back on the path, and what was stacked on it and what was
played on it goes to the discard pile. With no such site it liberates nothing, its cost paid. With no `controller` the engine takes a site controlled by anyone
but the player whose turn it is. A Shadow card's "liberate a site (you control)" is his own site, of his choice
(Q-0370: A, engine pass 70): its `liberateSite` writes `select: choose(controlledSite)`, which offers each of his
sites (one is taken unasked) and never the opponent's (Ted Sandyman 18_75, Land Had Changed 17_114, White Hand
Enforcer 17_125, You Do Not Know Pain 17_138). A card that replaces
a controlled site (Úlairë Nelya 1_233 by "an opponent's site": a controlled site is still its owner's) leaves the
new site controlled, with the stacked cards on it once each (F-0466). Being still its owner's, a controlled site is
the invalid candidate of "your sites on the adventure path" (Led Astray 11_45): only `zone(adventure path)` keeps
it out of a `choose(your,site)`, and only a controlled site tells the two apart. The same holds for a COUNT "on the
adventure path": a controlled site of the terrain is the board that notices `zone(adventure path)` left out
(Subterranean Homestead 13_8, dec-b13_5; a site not yet on the path does not). A card PLAYED on a site that is replaced,
controlled or not, lies on the new site afterwards (CR 4.2 "site": "all cards played on or stacked on the old site
are moved to the new site"; F-0467, mended by engine pass 26): it is the same card and never leaves play
(`assertAttachedTo(card, newSite)`, `PlayerSeesCardBorneBy`), and its "this site" is the new one from then on
(Saruman's Frost 1_135 on a current site that Úlairë Nelya 11_222 replaces still takes 2 from each Hobbit).
An `exchangeSite` (Uruk Cavern Striker 15_174) swaps the two sites' numbers and gives up the site handed over: it goes
back on the path, nobody's, in the taken site's place, and the controller holds as many sites as before (F-0720,
engine pass 75); what is stacked or played on the given site moves with its controller to the site taken, as on a
replacement (Q-0404: B, engine pass 76). Any Free Peoples site on the path may be spotted, the current one included
(Q-0403: B): the fellowship then stands at the site given, whose text is the one in force from then on.
mechanics/sites/ControlOfSitesTests, mechanics/sites/PlayingSitesTests.

**The other player's Free Peoples cards are not active on your turn.** A copy of a possession taken from the
Shadow player's deck (`GetShadowCard`) and cheated onto your companion is inactive (CR 4.2 "active"), so no
filter sees it. To put a card the opponent owns on a companion, use a Shadow card that is borne by companions:
Spider Poison (8_28) is one, and does nothing unless its bearer is assigned to a skirmish. A second copy of a
Free Peoples card is a second name for it in the scenario (`put("mail2", "1_101")`), never the Shadow player's
copy: a count of what a companion bears passes that one over. The other half is as true: your own Shadow cards
are not active on your turn, so a Shadow condition you own is no candidate for your Free Peoples ability: a
Shadow condition laid on your companion is the Shadow player's copy (`GetShadowCard`), never `GetFreepsCard`, or
a branch that would act on it goes unoffered and the choice is not asked (Andúril 17_27's Weary 4_212).
Culture tokens go with the card: a token on his card is not spotted, reinforced or removed by your cards on your
turn, and "remove an [elven] token to ..." is not offered when his is the only one (68_9, 68_11, 68_12). It is
an invalid candidate by rule, not by the card's filter. It cuts the other way after `SkipToOtherPlayersTurn()`:
your own Free Peoples cards are then the inactive ones, and a bonus from your condition reads as the bearer's
printed number whatever the card does (Brought Down from Inside 69_24): read it on its owner's turn.
The Free Peoples player's cards are active on his turn whoever acts: where the Shadow player uses a Free Peoples-worded
ability (a site's Maneuver or Regroup text), the Free Peoples player's cards in play are active and not the actor's, so a
`your` there owes that board (Balin's Tomb 1_343: `choose(your,tale)` widened survived until his tale was laid; dec-b1_336).
A card that is not active has no text in force either, whenever a test lays it (G-0055, engine pass 28): the game
starts a card's text as the card comes into play only if the card is active, and once as each turn begins in
which it is. His Free Peoples card, cheated in before `StartGame()` or during your turn, changes no number on
your turn and counts once on his: his Ranger of the South 18_56 reads +2 there for a [gondor] token, and his
Lord of Moria 1_21 gives your Dwarf Guard no damage bonus on your turn. So his cards may go down with the rest of
the board, at any time, and a number of his is read on his own turn
(`virtualtable/CardLaidOutOfTurnTests`). The one way left to start a card's text out of turn is `RestoreCard`,
which starts it whatever the card: restore his hindered card on his own turn (a reconcile never restores a card
that is not active).

**An event is in the void while it is being played, and while another player's choice in its effect waits.**
From its play until its last decision is answered, whoever's decision that is, an event, a response event
included, is in `Zone.VOID` (The Board Is Set 7_32 while the Shadow player picks remove (2) or cancel); only
then is it in the discard pile. `assertInDiscard(event)` while a choice or a prevention of its effect is still
pending fails. An event whose effect begins a skirmish
(A Dark Shape Sprang 10_19) lies in the void through the whole of that skirmish: read it in the discard pile
once the skirmish has resolved and the suspended phase has resumed. A response to that event's effect is carried
out while it is still in the void, so it is not in the discard pile a response reads ("each Shadow card discarded
in this way"): a control card "already in the discard pile" is a separate card put there (Uruk Spy 1_155 after
Saruman's Reach 1_137). An event that plays a minion from a pile stays in the void while that minion's own "when you play" trigger waits,
and is in the discard pile only once it is answered (Foul Things 2_59 with Goblin Scavengers 1_179). A number
prompt AHEAD of a choice with nothing to choose is still asked: Corrupt 7_175 (`chooseHowManyFPCulturesToSpot`, then
`choose(nazgul)`) with no Nazgul in play asks the number and waits in the void until it is answered (dec-b7_173).

**An event printed with several phases is an event of the phase it is played in (F-0292).** While it is being
played, and when its cost to play is read (`GetTwilightCost` of the card in hand, the play itself), a "Skirmish
or Regroup" event is an event of the current phase alone: `timeword(skirmish)` in a `played` trigger, a response
or a `modifyCost` is false of Sword-wall 4_84 played in the regroup phase. In a hand or a pile it is what is
printed on it ("take a skirmish event into hand" finds it in any phase), and an event printed with one phase,
with or without "Response" (Eye of Barad-dûr 5_96), is never narrowed
(`mechanics/actions/EventPrintedWithSeveralPhasesTests`).

**`GetVitality` is the vitality left.** Printed vitality and what cards add, less wounds: a character of
vitality 4 with 3 wounds reads 1. The printed number is the blueprint's, as the stats test reads it.

**`GetResistance` is the resistance left.** Printed resistance and what cards add, less the burdens, and for every
companion, not the Ring-bearer alone: with 3 burdens Frodo 1_290 (resistance 10) reads 7 and Sam 1_311 (5) reads 2
(`mechanics/ring/RingBearerSuccessionTests`). So burdens lay a "resistance N or more" threshold exactly: two put
resistance-7 Dwarves (Gimli 11_8, Grimir 11_10) at 5 and Farin 11_7 (6) at 4 (Card_11_014_Tests). An ally's is 0,
and no printed card raises it; a value modified below zero counts as zero (CR 4.2 "modifier"), so a
"resistance -X" text has no ally to lay as an invalid candidate (Card_12_151_Tests' Notes).

**A borne card's text bonus adds to its printed bonus.** "If bearer is an Uruk-hai, it is strength +2" on a possession printed
+1 (Uruk Axe 10_102) makes the Uruk-hai bearer +3 in all: the text's `modifyStrength` stands beside the printed number,
which it does not replace (Card_10_102_Tests reads 9 + 1 + 2 = 12, and 7 + 1 = 8 for a bearer who is no Uruk-hai).

**The card info names a borne card's printed bonus for that card** (engine pass 59). `GetAffectingCard(card)` reads
the card info's "Affecting card" lines ("Glamdring: Strength +2", "System: Ring-Bound" for a rule's). The printed
numbers of borne cards are one rules modifier with no source card (`StatModifiersRule`), shown one line per borne
card that adds something, none for a 0; the game still reads the sum (`mechanics/inplay/AffectingCardListTests`).

**A card that changes zone loses every token on it, wounds included.** `GameState.removeFromZone` strips them,
so a card made a minion and moved back to the support area at the start of the regroup phase arrives clean, and
its own "at the start of the regroup phase, remove all tokens" cannot be told from its absence (A Shadow Rises
11_216, dec-b11_211: the trigger moved to another phase SURVIVED).

**Culture tokens are of the card's own culture.** `AddTokensToCard`, `RemoveTokensFromCard` and
`GetCultureTokensOn` deal only in the token of the culture of the card they are given, so the invalid candidate
for "an [elven] token" is a card of another culture carrying its own tokens. Put tokens only on a card that
some card gives tokens to (Q-0213: I): a possession with tokens no card puts there is a board no game reaches
(F-0073 was found on one, and is moot in play).
A reinforcement is chosen among every active card that already has a token of the culture, EITHER player's (CR
4.2 "reinforce" says "a card that already has a token" and then "any of your cards"; Ketura, 2026-10-11, is
asking the rules council, and until it answers the choice is not held to one's own cards: F-0468, mended by
engine pass 26 and reverted by pass 28). Another player's card is among them only where both players have such a
card active: at Morannon Plains 18_137, which names no culture, and in the [gollum] culture, the one culture with
cards on both sides (Larder 8_23 against Gladden Homestead 13_49 and Nice Fish 15_46). Several tokens are chosen
for one at a time, and a card that leaves play keeps none of its tokens. The rule is shown in
`mechanics/inplay/CultureTokensTests`: a card's own file reads its own candidates and count and no more, and never
asserts that another player's card with such a token is NOT offered.
**Ithilien Trap 4_126, the usual "[gondor] card with no token" for a reinforce choice, has its own optional trigger
when a [gondor] Man wins a skirmish.** On a board where Aragorn or another [gondor] Man wins, its offer comes in the
same optional-responses decision as the card under test: take the card by `FreepsChooseOptionalTrigger(card)`, not
`FreepsAcceptOptionalTrigger()`, and decline the Trap's offer after (`Card_17_036_Tests`, `Card_18_058_Tests`).
A kind of token taken off to the last is kept at a count of 0 (`GameState.getTokens`), and the card info no longer
lists it (`getTokensHeld`, engine pass 59; `mechanics/inplay/ATokenCountedZeroIsNotListedTests`).  No filter tells
that 0 from no count at all: `hasAnyCultureTokens(0)`, like `hasToken(token, 0)`, is true of every card (F-0578,
engine pass 60: Grond 58_103 at X 0 did nothing on a Grond that never had a token; its file tests both).

**Uniqueness is counted across both players.** `PlayUtils.checkUniqueness` counts every active card of the
title, the other player's included: an opponent's card of a unique title on the table bars your own copy. The one
exception is a definition with `uniquePerPlayer: true`, which counts only its owner's cards (CR 4.2 "unique", one
per player; F-0655, engine pass 70): The Palantír of Orthanc 13_36 alone, played beside the Shadow player's 3_67 or
4_166 (`mechanics/inplay/UniquenessTests`).

**A stacked card is not in play** (CR 4.2 "stack"). It cannot be spotted, does not count for uniqueness (a copy
of a stacked unique card is played, and a third copy stacked beside it), is no companion for a move's twilight
and no minion to be discarded as the turn ends, and its text is out of force unless it says "while stacked".
Stacked by legal play: The Shards of Narsil 3_44 (any [gondor] card from hand, fellowship phase, no cost; (1) to
take one back) and Teeth of Mordor 5_110 (a [sauron] Orc from play, regroup phase, (3)). `mechanics/inplay/StackingTests`.
"A card stacked on <a kind of card>" is found only under an ACTIVE card of that kind (F-0424: B, engine pass 28):
a card stacked on the other player's own Free Peoples card on this turn, or on a hindered card, pays no cost, is
not offered to be chosen and is not counted. Lay his stack before `StartGame()` and read the ability as not offered.
"If you spot a card stacked on a [dwarven] condition" (Battle Tested 7_3, 4_45, 7_14) is read so too: such a
condition of his, active, with any card on it; the stacked card itself is not spotted, and nothing is asked (dec-b7_3).
A stacked card that another card lets be played (Sindri 9_10's `canPlayStackedCards`) is read and played as one
in hand: `FreepsPlayAvailable(card)` and `FreepsPlayCard(card)` (Card_09_010_Tests, StackingTests; dec-b9_2).

**The Ring is attached to the Ring-bearer.** `GetAttachedCards(ringBearer)` includes The One Ring, so "nothing
else attached" is a size of 1. `assertNotAttachedTo` is the safer reading.

**A card that passes through a zone.** Where an effect takes a card out of a place it has only just arrived in
(a tentacle killed by a wound and then stacked from the discard pile), read it there while the decision is
pending. A count that is 0 before and 0 after shows nothing by itself.

**A minion that is killed was not discarded, and no card's "is discarded" trigger answers the kill.** A killed
minion is placed in the discard pile, and until engine pass 26 the definition language's `discarded` trigger
answered it like any discard from play (F-0444, mended): CR 4.2 "killed", "Cards that trigger when a character is
killed will not trigger when a character is discarded, and vice-versa." Wildman's Oath 17_66 ("Each time a [men]
minion is discarded during the maneuver phase, you may stack it here") is not offered a Man of Bree 11_90 killed
by the wound of Defend It and Hope 4_115 there, and is offered him discarded by What Are They? 1_119 (a ranger
spotted; he is roaming at site 2): the pair of legal plays for the two sides. A text that says "killed or
discarded" has a `killed` trigger beside its `discarded` one (Last Days 12_71, Throne of Minas Tirith 17_36) and
is offered ONCE for a kill: a definition with `discarded` alone under such a text no longer answers a kill, and
one with both that offers twice is a finding too (read the second offer after ACCEPTING the first, with
something left for it to do: two offers in one window are declined by one pass). A card's OWN "when this is
discarded" told the two apart already (Q-0081): `SelfRemovedFromPlayTrigger` refuses a kill, so "When <this> is
killed or discarded" wants a `killedTrigger` beside it, with `player: owner` on a Shadow card (without it the offer
goes to the Free Peoples player; Narzug 30_36, F-0747; 8_396 has it). A `source:` filter on the trigger, `source: any` included,
still refuses every discard the engine emits with no source card: the discard of the minions as the turn ends,
and the cards that go with a card that is stacked, returned to hand, shuffled in or put on a deck from play.
Gnawing, Biting, Hacking, Burning 6_63 is built so, and Iron Fist of the Orc 6_64 and Scaffolding 6_73 the same
way (read, not run): it is `source: any`, not its `phase: regroup`, that keeps the end-of-turn discard off it
(the phase still reads regroup then). mechanics/wounds/KilledIsNotDiscardedTests.

**The removed-from-game pile.** `scn.MoveCardToZone(card, Zone.REMOVED)` lays a card there (a cheat) and
`assertInZone(Zone.REMOVED, cards...)` reads it. Two printed cards put cards there by play: Defiled 13_105
(four [orc] cards in its owner's discard pile, in place of its twilight cost) and Gollum, Her Sneak 13_50 (a
Déagol in his owner's discard pile, to play himself from it). Déagol 13_46 is a Free Peoples follower: in the
Shadow player's deck he is `GetShadowCard`, and the Free Peoples player's own copy is the other player's
(Card_13_050_Tests, Card_13_105_Tests).

**What an ally bears is in the support area.** A "support card" is a card in a support area or borne by a card that is (`mechanics/inplay/SupportAreaTests`): a Shadow condition on an ally (Weary 4_212 plays on a companion or ally) or on a site the Shadow player controls (Pillage of Rohan 4_167, Wolves of Isengard 5_69, No Retreat 4_30; `ShadowTakeControlOfSite(scn.GetSite(n))` after `SkipToSite(n + 1)`, then `AttachCardsTo(site, card)`) is a valid candidate for "a Shadow support card", and the same card on a companion is the invalid one beside it.

**A follower's Aid is an optional trigger at the start of the maneuver phase.** A follower lies in the support
area until its Aid is paid, and the offer is made for one a companion already bears as well (cheated on with
`AttachCardsTo`), but only while its Aid cost can be paid: Ori 30_18 (discard a [dwarven] card from hand) borne
with an empty hand makes no offer, while Dori 30_8 (exert Bilbo) does (hobb-b32_1). A test that stops in the maneuver
phase with any follower in play answers it first (`FreepsDeclineOptionalTrigger()`; a bare `FreepsPass()` only answers the offer, and the Shadow player is
not yet asked; Frenzy of Arrows 68_79's tests). `SkipToPhase(Phase.MANEUVER)` leaves the offer pending:
`FreepsHasOptionalTriggerAvailable(follower)` reads it, and `FreepsAcceptOptionalTrigger()` pays the Aid cost and
then asks for the companion (`FreepsChooseCard`); a trigger that answers the Aid's cost (Bilbo, Collector of Treasures 30_44 on a discard from hand) is offered between the two, before the bearer is chosen (Card_30_044_Tests). While it is pending `AwaitingFreepsManeuverPhaseActions()` already reads true, but `FreepsPlayAvailable(card)` reads false: answer the offer before reading a play (Card_17_009_Tests). With two followers' Aids offered at once, `FreepsChooseOptionalTrigger(follower)` picks one, and the other's is
still pending after it (Gwaihir 17_20 beside Faramir's Company 18_49, Card_18_049_Tests). `FreepsHasOptionalTriggerAvailable(follower)` cannot tell the Aid
from the follower's own "when you transfer this" trigger: give the offer's words, `(follower, "Aid")` for the Aid
("Use <card> Aid") and `(follower, "Optional trigger")` for the trigger (Card_18_017_Tests). A text the game never
uses reads false whatever the card does, so a "not offered" read with a text stands only beside a read of true with
the same text on the board that offers it (Ori 30_18, Card_30_018_Tests). Once the Aid is
resolved, a bare `FreepsHasOptionalTriggerAvailable(follower)` reads the transfer trigger alone (Card_13_028_Tests,
Card_13_144_Tests). The N of a printed "Aid - (N)" is read off the card, wherever
it lies, with `GetAidTwilightCost(follower)` (G-0051, engine pass 26; for the stats test): a Free Peoples Aid
adds it and a Shadow one (Saruman 17_38) removes it. It fails, saying so, for an Aid that is not a plain amount
of twilight (a burden, a threat, an exertion, a card from hand, or a spot beside the twilight, as Thráin 32_9's "Spot
Gandalf and add (2)"; hobb-b32_3): for those the header carries the cost. Every Free Peoples Aid transfers "to a companion", so no ally
bears a follower by legal play. `AttachCardsTo(companion, follower)` cheats one on. Most followers give the
bearer a printed strength or resistance; see helpers.md ("A follower that changes no number") for two that leave a reading alone (Little Golden
Flower 65_148's tests). The rule itself is in `mechanics/keywords/AidTests`: in a second maneuver phase of the turn the
offer is made again for a follower already borne, and declined it costs nothing and leaves him borne (CR 4.2
"aid": borne "for the rest of the turn"). He goes back to the support area as the turn ends: when the turn ends at
the move limit that is after `PassCurrentPhaseActions()`, `PassReconcile(scn.P2())`, `PassReconcile(scn.P1())`,
with the game at the other player's "Play Fellowship action or Pass", where the bearer is an inactive card (read
the return by zone, not by his strength). So a follower cheated on before `SkipToSite(n)` is back in the support
area when the skip ends: cheat him on after it (Card_15_135_Tests). `SkipToAssignments()` from the fellowship phase
passes over the Aid offer of a follower already borne and arrives at the assignments with him still borne (same file).
While the follower has nobody to go to the Aid is offered all the same and reads `"effect"` on the no-effect flag
(`FreepsActionNoEffect(follower)`); taken, its cost is paid, nobody is asked for a character and nothing moves
(F-0363; Ketura: "they can still attempt to pay it and get nothing out of it"). That is at Streets of Bree 18_140
("followers cannot be transferred to a character"; laid with `MoveCardToAdventurePath`), under any other
`CantBeTransferred` that reaches the follower, and in a second maneuver phase when the follower's bearer is its
owner's only character (Q-0244). Anywhere else it reads `"false"`. `mechanics/keywords/AidTests`.
A SHADOW follower (Saruman 17_38) in the support area or borne by a minion has its Aid offered to the Shadow player
as the maneuver phase starts, and `SkipToPhase(Phase.ASSIGNMENT)` or `SkipToAssignments()` called from the Shadow phase passes over it out of step (dec-b4_159): it rests with the
Shadow player at "Play Assignment action or Pass", and a following `SkipToAssignments()` answers the Free Peoples
"Assign minions" with nothing. Use `SkipToPhase(Phase.MANEUVER)`, `ShadowDeclineOptionalTrigger()`, then
`SkipToAssignments()` (Card_17_038_Tests; probed in dec-b19_18). With Free Peoples followers in play as well, a
test that stops in the maneuver phase answers both offers in turn, `FreepsDeclineOptionalTrigger()` and then
`ShadowDeclineOptionalTrigger()`, before the Free Peoples player's maneuver actions are offered (Card_18_053_Tests). Once another start-of-maneuver optional trigger
has resolved (Uruk Village Assassin 15_176's), every Aid declined before it is offered again, the Free Peoples one and
then the Shadow one, and each is declined a second time before `AwaitingFreepsManeuverPhaseActions()` reads true
(Card_15_176_Tests, dec-b15_174).

**Whose support area.** A card borne by a card in a support area is in THAT support area, whoever owns it
(`mechanics/inplay/WhoseSupportAreaTests`): the Shadow player's Weary on your ally is in your support area ("a
condition from the Free Peoples support area" reaches it; "a Shadow condition from an opponent's support
area", used by you, does not). For "your / an opponent's support area" the valid candidates are a card lying
there and one borne by an ally or a controlled site there; the invalid ones are the same card on a companion
and a card of the right owner borne by a card in the other player's area.

**"A site in a support area" is a controlled site, either player's.** A site is in a support area only once a
player controls it (CR 4.2 "control"), and a site in ANY player's support area is active (CR 4.2 "active"), so a
site the Free Peoples player took control of on his opponent's turn counts too. Lay it with
`ShadowTakeControlOfSite()` or `FreepsTakeControlOfSite()` once the markers have passed it (Fleet-footed 10_10).

**`assertInHand` reads the zone, not whose hand.** `assertInHand` (as every `assertInZone`) passes for a card in
either player's hand, so it cannot show "to their owners' hands": read `GetFreepsHand().contains(card)` and
`GetShadowHand().contains(card)`, with each hand's count before and after (Gríma, Chief Counselor 5_51; dec-b4_92).

**The twilight cost of a card depends on where it is.** `GetTwilightCost(card)` answers for where the card is:
for a card in play, the number printed on it (F-0109, Q-0073); for a card in a hand or pile, what it would cost
to play now, roaming apart (the pool after a play from hand below the card's site number does take the
roaming +2: write it into the spelled-out sum, Card_13_171_Tests); for a site, its Shadow number as it stands. A "Shadow number" modifier on "each site"
is so read on the sites laid, the Free Peoples player's and the Shadow player's copy (`GetShadowSite(n)`) alike
(Card_12_037_Tests, Card_02_121_Tests). To read a cost modifier, read a card in
hand, or play the card and read the pool. A `modifyOwnCost` with a `canSpot` requirement is seen in hand (Éomer 7_227
at 2 beside a [rohan] Man, Card_07_227_Tests); one that requires the starting fellowship is not (Elite Rider 7_225
in the draw deck reads 3): read that one by the play. A transfer costs the printed number and nothing else (Q-0053, Q-0074).
A filter's `minTwilight(N)` reads the PRINTED cost of a card in hand, not that number: Rohirrim Soldier 15_138
(printed 3, 2 in hand beside a [rohan] Man) passes Éowyn 19_26's `minTwilight(3)` (Q-0356 open on the reading).
A Free Peoples card's play ADDS its cost to the pool and needs none in it: an invalid Free Peoples candidate "that
can be paid for" wants no `SetTwilight` (Card_11_259_Tests); only a Shadow card wants a pool laid for it.
A plain play (the printed cost added or removed, roaming, where each type goes) is shown once by
`mechanics/costs/TwilightPoolTests` and `WhereACardIsPlayedTests`, and no card test only plays its card (Q-0279: B,
engine pass 31): a card is played from hand in the test of the clause of its text that bears on its play.

**A twilight cost never reads below 0, so a discount as large as the card's cost hides a larger one.** Madril
65_64 costs 2 and is "twilight cost -2": with `amount: -3` he reads 0 and is played for 0 all the same, and no
test on a bare board notices. To show the size of a discount on a cheap card, raise the cost first: an ad hoc
`TwilightCostModifier(null, CardType.COMPANION, null, new ConstantEvaluator(2))` does what East Road 11_236
does ("Each companion is twilight cost +2"), and the card reads 4 without its discount and 2 with it
(`Card_15_064_ErrataTests`). For a COMPANION a printed card does it, and is what a game has: The Misadventure of
Mr. Underhill 1_129, in the Shadow player's support area (`Card_15_064_Tests`; helpers, "A companion's twilight
cost raised"). For a MINION a printed card does it too: Chaotic Clash 17_68 ([orc] support-area
condition, cost 0, "To play, spot no minions. Each time you play a minion, it is twilight cost +1"), put in the
Shadow support area, reaches a minion played from hand and one played from the discard pile; its "each companion
is strength -1" needs an [orc] minion in play. Gollum (2) played at "twilight cost -2" beside it costs 1 and his
roaming penalty, so a pool one short withholds the play and -1 or -3 reads another number (`Card_10_019_Tests`). The roaming penalty is added AFTER the cost is floored at 0, so a discount larger than the cost does not eat into it: Fat Spider 31_51 (3) at -4 while roaming costs 0 + 2, not 1 (`Card_31_054_Tests`). The Clash is also a Shadow condition: beside another, a "discard a condition" (Ancestral Knowledge 30_1) asks which, so the test chooses (`Card_32_064_Tests`).
For a SHADOW POSSESSION: Taurnil 5_13 bearing a ranged weapon (+2; helpers, "A Shadow possession's twilight cost
raised"; `Card_01_193_Tests`).
For a FREE PEOPLES EVENT no printed card does it (Some Who Resisted 3_45 reaches maneuver events only), and no
[elven] Skirmish event costs more than 2: the ad hoc `TwilightCostModifier(null, CardType.EVENT, null, new
ConstantEvaluator(1))` is the accepted fallback (Q-0213: E; Elladan 14_2's -2 read 3 to 1, `Card_14_002_Tests`).

**"When you play X, you may remove N threats to make its twilight cost -N" asks for N as the card is played.**
The card is offered when the pool and the threats together cover it (roaming counted), and the number asked for
runs from what the pool is short by up to the threats there are (`ShadowGetChoiceMin()`, `ShadowGetChoiceMax()`;
Host of Udûn 7_282 with the pool 1 short and 2 threats: 1 to 2). Where only one number can be named (the pool 1
short and 1 threat) nothing is asked (F-0147), so a test that reads the bounds lays one threat more than is owed
(`Card_07_282_Tests`).
A discount that is all or nothing ("you may discard 5 [orc] minions from play to make it twilight cost -10",
Mountain-troll 15_112) is asked as a yes/no: shapes.md, "When you play this minion, you may discard N".

**Whether a minion is roaming is read as a keyword.** `scn.HasKeyword(minion, Keyword.ROAMING)` is true while
the minion's site number is above the current site's, and it follows a site-number modifier: Úlairë Nelya 2_84
(site number 2) reads false at site 2, and true once a card has raised her site number
(`Card_15_062_ErrataTests`, `Card_15_064_ErrataTests`). A site-number modifier on "each [<culture>] Orc" reaches a minion in HAND too: `GetMinionSiteNumber` reads it
there, and played at the lowered number it pays its cost with no roaming +2 (Unmistakable Omen 15_118, Card_15_118_Tests).

**A keyword a card gives itself while it can spot that keyword is kept once gained** (Sentry Uruk 15_170, Q-0276;
Ravaging Wild Man 15_91, Q-0290; `spotsItself`). The other card may go and the Sentry stays fierce; alone from the start it has nothing. What it
holds is worked out at each step of the game and whenever it is asked about, so a cheat that takes the keyword
away and gives it back with no step between (`ApplyAdHocModifier` and a read) finds it still held, where any
step in between (a pass) loses it. Every other "while you can spot" leaves out the card's own grant, as before.

**Two standing texts that undo each other both lapse** (CR 4.2 "loops"; F-0536, engine pass 50). Frodo, Weary From
the Journey 15_144 skirmishing Black Land Observer 15_101 or Wandering Hillman 15_94 at 8 or 9 before his +2 gets no
+2, and the minion no +3. Where one of the two answers the same either way (Frodo at 7, or at 10 and up) both read
as printed. The same holds against Uruk Infantry 15_175 (Frodo at 8 or 9), Black Land Commander 15_100 (10 or 11) and
Black Land Spy 15_105 (11 or 12). `mechanics/modifiers/AnInvoluntaryLoopEndsWithNoEffectTests`.

**"Unable to gain game text keywords" keeps what the card had when it was laid** (`cantGainKeywords`, Into the Caves
17_100; Q-0282). Nothing is gained from any card, its own text included, and no more of a keyword it has (damage +1
stays +1); a keyword it had and lost by its own text may come back up to what it had. Roaming is no game text keyword.

**Making a card something it is not.** `ApplyAdHocModifier` with `IsAdditionalCultureModifier` or
`IsAdditionalRaceModifier` gives a card a second culture or race, `AddKeywordModifier` a keyword, and filters see
them. The rig can; a card test no longer does (Ketura on Q-0213, 2026-10-10). No card is given a culture or a
race: a race is told from its culture by a printed card that has the one without the other (helpers.md,
"A culture without its race"), and where none exists (Elf, Dwarf, a Free Peoples Wizard, a Free Peoples
Wraith) it is not told at all: `lint.py` reports a definition that goes by the one where its text names the other
(`race-culture`), and that is a finding to file. No keyword is given to a card no printed card with it resembles
(an Orc made a Southron, an ally made valiant, an [isengard] minion made a tentacle). That is what is given by
hand: a game does reach a valiant ally, by two printed cards (a Man ally under Flame of Calenhad 103_86, a Hobbit
ally under Royal Guard 102_54), and a valiant Ring-bound companion beside Erkenbrand 102_47, so a test that wants
one lays those (helpers.md, "A valiant ally"); a Southron who is no Man is a Bladetusk made a minion (helpers.md).
No site is given a terrain:
lay a real site of it ("A battleground or a forest site needs no cheat"; helpers.md, "A tentacle that stays put",
has the marshes). What stays
is the keyword or number that only arranges a board (fierce, archer, damage +1, a strength), and a new test takes
the printed card that gives it where helpers.md has one.

**An ad hoc modifier aimed at one card goes on after that card is in play.** A card is given a new id each time
it enters play, and a modifier whose filter is a physical card holds the id it had when the modifier was built.
`ApplyAdHocModifier(new VitalityModifier(null, vile, null, 1))` before `MoveMinionsToTable(vile)` does nothing
and says nothing. A modifier with a general filter (a card type, a keyword) can go on at any time, and where the
card has to be played legally afterwards, build the modifier on a filter and not on the card:
`scn.GenerateShadowFilter("name(Goblin Armory)")`, which throws `InvalidCardDefinitionException`
(`com.gempukku.lotro.cards.build`) for the test method to declare.
On a probe, `OverrideUniquenessModifier`'s four-argument constructor RESTRICTS (the limit is capped by it); to
lift a card's uniqueness pass `loosen` true as a fifth argument (dec-b13_30). In a card test a lifted uniqueness
is a board outside play (Q-0239).

**An ad hoc effect is run in place of a player's answer.** `FreepsExecuteAdHocEffect(effect)` and its Shadow
twin run the effect as that player's answer to a choice of actions, whatever else the choice offers, and fail
the test if the effect was not carried out. Where being stopped is what the test is about (a wound aimed at a
character who is to be unable to take wounds), `FreepsExecuteAdHocEffectUnchecked(effect)` does not assert, and
the test reads the board (G-0028; Long I Fell 54_97). A `WoundCharactersEffect` needs a source card, and either
call needs its reason on the line (`// lint-ok: ...`), as `carryOutEffectInPhaseActionByPlayer` does. Where `helpers.md` names a printed card for the same board, the printed card is laid instead, also in a file started from an erratum's that used the ad hoc effect (Orkish Skirmisher 18_90 for a wound to a companion in no skirmish: Long I Fell 4_97, dec-b4_97).

**A value modified below zero counts as zero** (CR 4.2 "modifier"): a site's Shadow number lowered past 0 reads
0 (Rivendell Valley stays 0 under No Stranger to the Shadows 51_108). `rules.py term` has no entry for "Shadow
number"; the rule is under "modifier". So a discount of N is read at a site whose number is above N, or N and
N + 1 read alike: A Talent for Not Being Seen 1_316's -2 is told from -3 at Balin's Tomb (3), not Ettenmoors (2).

**"Add 1 to the number of X you can spot" raises its owner's count alone** (F-0502, engine pass 39). The count is
the action's performing player's (`Filters.countSpottable` with a spotting player): Merry 17_107's +1 Ent is not in
the Shadow player's Urgency 19_17 count. The bonus still reaches only a count whose filter is the bare race or class.

## A card that leaves play as it is played

**A card that leaves play as it comes into play was not played.** A minion played beside Watcher in the Water, a tentacle played away from a marsh, a possession or weapon played on a bearer of Shadowfax, Greatest of the Mearas (17_24 and its erratum 67_24, a standing discard by LR 1; no other Shadowfax): the card goes to the discard pile, what it cost stays paid, and nothing that answers a play happens or is offered: not its own "when you play", not another card's "each time you play", not a response (CR 4.2 "discard"; `mechanics/inplay/DiscardedOnEntryTests`). The standing discard is done before the play is announced, so no "Required responses" question is asked between it and a play's own triggers, for a card that stays either. The game log has a line "<card> left play as it came into play".

**A card played "exhausted" is exhausted as it comes into play (F-0500, engine pass 37; F-0501, engine pass 38).** A play with `exhausted: true` (Gothmog 18_80, Spied From Above 6_84, Peril 6_105, Mountain-troll 65_112, Race Text 93_9), and a character another card says comes into play exhausted (the `ComesIntoPlayExhausted` modifier: Goblin Domain 1_175, Blood of Númenor 2_31, Race Text 91_9, Not at Home 30_37), is placed with its wounds before anything answers the play, and nobody is asked to order anything against it: Úlairë Nertëa 103_77 makes no offer for it, and a choice of who exerts does not list it. A wound count read after the play reads the same either way; the reading that tells is something that answers the play, beside a control played fresh in the same phase (Card_18_080_Tests). Where the modifier reaches every card that could answer the play (Blood of Númenor 2_31 on a [sauron] Orc, with Úlairë Nertëa's possessions all "Bearer must be a [sauron] Orc"), no fresh control can share the phase: the control is a test of its own without the card (Card_02_031_Tests). The wounds are laid, not taken: no "each time ... exerts" or "is wounded" answers them. Úlairë Nertëa 103_77 is fierce: laid before the assignment phase, it brings a fierce assignment round the test must pass through (Card_06_084_Tests lays it in the Shadow phase and passes the round; Card_18_080_Tests cheats it in during the regroup phase).

**A play as a cost can come to nothing.** An ability whose cost is to play a card is offered, takes its other costs and the card's twilight, and does nothing if the card has left play by the time its play is complete, whatever removed it. Read the costs as paid, the effect as not done, and the phase's decision.

**The loss of initiative comes with the play.** When a Free Peoples card played from hand takes the hand below four, `FreepsHasInitiative()` is still true while the card's entering step is being answered and false once its play is announced; a required "lose initiative" trigger and a required "each time you play" trigger for that play come up in one "Required responses" decision. Initiative is read again each time the game goes round its loop: cards cheated into a hand while a decision is pending give it back at the next answer (`FreepsHasInitiative()` is true after the other player's pass), which sets up a second loss in one turn for the `R` of "each time you lose initiative" (Memories of Darkness 60_2). Assert `FreepsHasInitiative()` true between the cheat and the second loss. The reverse holds too: a cheat that cuts a hand of four or more to three (`SetFreepsHand` of three after `SkipToPhase`) is itself a loss of initiative at the next answer, and sets off "each time you lose initiative" there (Fell Deeds Awake 10_73 offered its trigger on a play from three to two); set the hand before StartGame, or before the board gives the trigger anything to act on. In a skirmish, where the only other answers are passes that end it, Sméagol 6_45 ("Skirmish: Discard a card from hand to make Sméagol strength +1"; cheat him in, his play adds a burden) discarding from five cards to four is an answer that keeps initiative, and his discard from four to three, or Ranger of Osgiliath 7_116's "Regroup: Discard 2 cards from hand", is a loss of initiative that is no play (Glimpse of Fate 60_12).

**An event that needs initiative is not counted toward it (F-0464, built by engine pass 28).** A Free Peoples event that says "If you have initiative, ..." wants four cards in hand BESIDE itself (CR 4.2 "playing a card": "not counting The Card"): from a hand of exactly four with itself among them it is not offered, though `FreepsHasInitiative()` reads true there, and from five it is; "If the Shadow has initiative" (So Fair, So Desperate 8_111) is offered from four with itself among them and not from five. Its `-` is the hand of exactly four and its `+` the hand of five (8_111 the other way round); each of the five such events has that lone test in its file. A cost that is smaller with initiative is read the same way, judged and paid: Light the Beacons 7_43 played from exactly four cards exerts Gandalf three times. An initiative the EFFECT asks for is read without the event as before (All Save One 7_31 played from four cards gives the lesser bonus), an ability's is checked before its cost is paid (Madril 7_110 discards from four to two and still wounds; Legolas 7_25's "discard 4 cards from hand" from exactly four takes them unasked and still wounds, hand 0: Card_07_025_Tests), and a card in play counts the whole hand. `mechanics/game/InitiativeTests`.

**A card that discards itself as it is played was not played either.** A card discarded by its own "when you play this ..., or discard it" (the three Dunlendings of set 5, Stampeding Chief) is the same: the Free Peoples player's or the Shadow player's choice in that text is the first thing asked, before any trigger or response to the play, and if the card goes nothing answers its play. Beside the Watcher such a card is discarded first and the choice is not asked at all.

## Hidden zones, reveals, shuffles and the game log

**Searching a deck shows the deck first.** An effect that looks through a hidden zone shows the cards before it
asks for a choice, and so does a play from the draw deck (`playCardFromDrawDeck`: Éomer 12_112's "You may inspect
the contents of your deck" comes before "Choose cards from deck", Card_01_317_Tests). `FreepsDismissRevealedCards()` answers the showing; then choose with
`FreepsChooseCardBPFromSelection(card)`, which picks by blueprint because cards in hidden zones are not identified
to the client. The showing lists every card as not selectable, the one that will be taken too (it reads `(card, false)` there:
Wizard Staff 31_18, hobb-b31_15); the choice after it leaves a card that does not match out altogether, so there it reads `FreepsHasCardChoiceNotAvailable(card)`, not `FreepsHasCardChoiceAvailable(card,
false)` (Long-stemmed Pipe 17_21, dec-b17_18). A `FreepsChooseCardBPFromSelection` sent while the showing is still pending is taken without a
complaint and chooses nothing: the card stays in the deck and the test fails later, far from the cause (Éomer 12_112). A play from the draw deck that repeats (`repeat`) shows the deck again each time: each repetition wants its own `FreepsDismissRevealedCards()` before its choice (Dwarven Song 31_2, hobb-b31_1). To take nothing from a choice whose minimum is 0, `FreepsDeclineChoosing()`: a
`FreepsChooseCardBPFromSelection()` with no card does not compile (two overloads fit).
Where nothing in the zone can be chosen, the showing is the only decision ("... while retrieving cards", nothing
selectable; Saved From the Fire 8_20 with no card of the culture in the deck): answer it once. So too a play from the draw deck that is a cost branch with no `mustExist`: with nothing playable the ability is still offered, the deck is shown and shuffled once, nothing is played and the effect after "to" is not carried out (so Beorn's House 31_44 was, hobb-b31_25; since engine pass 79 its branch has `mustExist` and the ability is not offered then, Q-0419). Every card of the
scenario's map is in both decks, so such a board moves out every helper that matches the filter, not only the ones
the test names (an [uruk-hai] hunter left in the deck kept "Choose cards from deck" pending: Grishnákh 17_71, dec-b17_68). It lists the
deck's cards as not selectable, so `(card, false)` reads them there (Shelob, Eater of Light 8_25, dec-b8_23). `FreepsDeclineChoosing()`
answers a showing as readily as a choice, so one too few leaves "Choose cards from deck" pending. One too many
fails the test, saying what is being asked instead (G-0034; it used to pass the phase in silence): delete the line.
`FreepsAcceptOptionalTrigger()` answers only a choice of actions (an optional trigger, the order of required
responses, a phase's actions) and fails, saying what is asked, at a showing or any other decision (engine pass 32;
it used to send "0" to whatever was asked): a showing is answered by `FreepsDismissRevealedCards()`.
The showing lists every card of the zone and none of them can be chosen; the choice that follows lists only the
cards that may be taken. So "shown, and cannot be chosen" is read at the showing
(`ShadowHasCardChoiceAvailable(card, false)`, `ShadowGetTotalRevealedCardCount()`), and at the choice an invalid
card is simply absent (`ShadowHasCardChoiceNotAvailable(card)`; `(card, false)` fails there). A search of the
OTHER player's deck (Grond 68_82) is shown to the searching player alone: the deck's owner has no decision.
A take written with `showAll: false` (Race Text 91_2) has no showing to dismiss: the choice comes at once.
Where a test says a player's own deck is shown to him alone, it reads the other side too: `assertEquals(0,
scn.ShadowGetTotalRevealedCardCount())` beside the Free Peoples count (Card_01_319_Tests; a comment with no assertion
under it shows nothing, dec-b1_319).

**"Look at an opponent's hand" is a showing to the looker alone.** Once the cost is paid, a `lookAtHand`
(Galadriel's Glade 1_351, "Fellowship: Exert an Elf to look at an opponent's hand") shows the hand to the player
who looks: read each card with `FreepsHasCardChoiceAvailable(card, false)` and the number with
`FreepsGetTotalRevealedCardCount()`, and answer with `FreepsDismissRevealedCards()`. The hand's owner is asked
nothing, and his hand keeps its count (`Card_01_067_Tests`).

**A bar on looking at or revealing a hand is asked by each definition word for itself, and one word does not ask
it** (F-0391, fixed in the definition by engine pass 26). `cantLookOrRevealHand` (No Business of Ours 2_44 and
52_44) is asked by `revealHand`, `lookAtHand`, `lookAtRandomCardsFromHand` and `revealRandomCardsFromHand`.
`revealCardsFromHand` asks nothing of it: it is the word for cards a player reveals from his OWN hand, and no
definition gives it another player's `hand:` any more. Storm of Argument 3_6 did, and showed a Shadow card past
No Business; it is now written `revealRandomCardsFromHand` (`forced: true`, `hand: shadow`, `count: 1`), as
Friends of Old 3_16, His First Serious Check 3_33 and Might of Númenor 3_43 write the same sentence. A
definition met that gives `revealCardsFromHand` a hand that is not the acting player's is a finding. Before two
cards that do a barred act are called the same check, read which word each is written with: one printed card of
each word is stopped under the bar in `mechanics/deck/ABarOnLookingAtOrRevealingAHandTests` (Q-0264: B2). Under the
bar the action is still offered and its cost paid; nothing is shown, the hand keeps its count, and an X read off
the card revealed is 0.

**A look at the top cards of an EMPTY draw deck still shows, with nothing in it.** `LookAtTopCardsOfDrawDeck`
(Questions That Need Answering 1_81) gives its player "Cards on top of deck" with no card: the count of cards
shown reads 0 and `FreepsDismissRevealedCards()` answers it (there it does not fail, though no card is shown);
until it is answered the event lies in the void. This is the game as built, with Q-0104's "showing nothing" the
nearest ruling and none on the card itself; the play is not flagged no-effect (`Card_01_081_Tests`). A REVEAL of
an empty deck is not so: `revealTopCardsOfDrawDeck` with nothing to reveal shows nothing and asks nothing, and the
game goes straight on (Fool of a Took! 1_170's second copy, `Card_01_170_Tests`): no dismiss is called.
Where a finding turns a reveal into a look (Forewarned 6_17, F-0705), the active tests answer the showing with
`DismissRevealedCards()`, which passes under either build; only the parked ones read who `BeingShownCards()`.
`FreepsChooseCard(card)` answers a choice from a deck as well, and by blueprint all the same: give the card to be
taken a blueprint no other card of that deck has (fillers of another kind), or another copy goes to hand and
`assertInZone(Zone.HAND, card)` fails.

**Whether a deck was shuffled.** `GetFreepsDeckShuffleCount()` and `GetShadowDeckShuffleCount()`, read before and
after. The count is already above 0 after `StartGame()` and a `Set...DrawDeck` cheat, so "no shuffle" is the count read
into a variable just before the act and compared after it, never `assertEquals(0, ...)` (Card_12_038_Tests, dec-b12_38). By the rules a draw deck that has been looked through is reshuffled whether or not the card says so (CR 4.2
"draw deck"; Ketura's ruling on Q-0013), so a card that searches a deck has this to show; in the engine that
shuffle is the definition's own `shuffle: true` (`shuffle: false` on Hobbiton Party Field 2_115 was noticed). A card played out of a deck
that was looked through (`showAll: true`) shuffles it as the card leaves, before it is paid for and before its
own "when you play" text is asked (Old Toby 1_305: the count is already one more at his offer; F-0313, engine
pass 20), and the definition's `shuffle: true` shuffles again after the play: two more, whatever kind of card. Where a trigger answers that card's play (Twisted Gold of Dragon 32_54's own "each time you play Smaug"), the second
shuffle comes only once the trigger is answered: while it is pending the count reads one more, so read it after (Card_32_054_Tests).
A look that plays nothing reads one, the definition's own, and is the test that notices a missing `shuffle:
true`, whether the play is an EFFECT or a COST ("Play Gollum from your draw deck ... to add a threat", Captured
by the Ring 7_53: the shuffle is made ahead of the failed play, and the cost is still unpaid; F-0494, engine pass 35). A card played OUT OF a deck onto a bearer, and an event played out of a deck, shuffle it as they leave even where nobody looked (`showAll: false`; an event played from hand that plays a card out of the deck counts as that card does);
a card played to the table or the support area does not (a minion revealed by From Deep in Shadow 12_38, a
starting fellowship). Two possessions played onto a bearer out of one reveal of the top cards read `shuffles + 3`:
one as each leaves, and the definition's own (Éomer, Keeper of Oaths 8_87). On a card's own board read the EXACT count wherever the board fixes it: `> shuffles` after a
card played out of a looked-through deck passes with the definition's `shuffle: true` gone, and `shuffles + 2`
does not (The Prancing Pony 11_256; mechanics/deck/DrawDeckShuffledWhenTheLookIsOverTests). A reveal or look at the TOP
cards is no search and writes `shuffle: false` (Q-0013): where a test reads the deck after one, read the count
unchanged too, since the top card's order notices a stray shuffle only by chance (Master Proudfoot 1_301).

**"You may play each <kind> revealed" asks once for each such card, a decline included.** Éomer, Keeper of Oaths
8_87 is a `Repeat` whose `times` counts the revealed [rohan] possessions, round a 0-1 choice: with two revealed,
playing none takes two `FreepsDeclineChoosing()`, and that second prompt is no fault (the text gives each its own
"may"). The `Repeat`'s filter is a second place the phrase stands: lay a revealed card each neighbouring filter
would count (a [gondor] possession, a [rohan] artifact) and decline, so a wrong count shows as one prompt too many.

**A "play ... from your draw deck" action with no cost is offered whatever the deck holds, and a look that plays
nothing is a use.** `PlayCardFromDrawDeck` without `mustExist` (Race Text 92_28, "Fellowship or Shadow: Play an
artifact from your draw deck (limit once per turn)") is offered with no artifact in the deck and with the deck
empty (Q-0019: no penalty for failing to play from the draw deck; Q-0104). Used with nothing to play, the deck
is shown and shuffled and the turn's `limitPerTurn` is spent. The eight `mustExist: true` cards of Q-0019 are
the exception, not this. A choice between a draw deck and another pile is the same where the deck's branch has no
`mustExist`: The Bridge of Khazad-dûm 1_349 ("Play The Balrog from your draw deck or hand") is offered with no
Balrog anywhere, and used so it looks through the deck and shuffles it. The choice of the card to play is 0 to 1
whatever the text says, `mustExist` or not (Q-0019, the engine's comment in `PlayCardFromDrawDeck`): read it as 0
where it is read, and file nothing; the Standard's minimum for a text with no "may" does not reach it (15_51).
Where the deck holds nothing the choice can take, no choice is asked at all: after `FreepsDismissRevealedCards()`
the action is over, so there is no `FreepsDeclineChoosing()` and no reading of the shown cards as not selectable
(so Goblin Town 30_52 and Esgaroth 30_54 were, hobb-b30_52, before engine pass 79 gave them `mustExist`).
As a branch of a `choice` such a play counts as doable in full on every board, the card's unique title in play
and none in the deck included (so Blunt The Knives! 33_4's "play Balin from your draw deck" was, before engine pass
79 gave the branch `mustExist`, Q-0418).
NOT so on a Hobbit-set card (sets 30 to 33): there a play from the draw deck, alone, beside another pile or as a
`choice` branch, with nothing it could play, is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418,
2026-11-05), and a definition without `mustExist: true` on it is a finding fixed at landing (Great East Road 33_56,
F-0799). Since engine pass 79 every such definition in sets 30 to 33 has it (the list is in the ruling on
Q-0418/Q-0419); a play inside an `If` and a take into hand with a cost carry a `requires` instead (shapes.md).

**The Rule of 4.** No more than 4 cards are drawn or taken into hand in a player's fellowship phase. A test of a
fellowship-phase draw that repeats has to stay within four, or its fifth reading is the rule and not the card.
Once the four are spent an optional trigger or a no-cost ability whose whole effect is a draw is not offered at
all (Q-0262: B; the fifth Old Toby 1_305 asks nothing), where an empty draw deck alone still asks (Q-0029).
The move at the end of the fellowship phase is part of it: a draw from a "when your fellowship moves from" trigger
counts, and is withheld once four are drawn (Moria Guardroom 11_247, Card_11_247_Tests).
`FreepsAcceptOptionalTrigger()` also answers a phase's choice of actions, so where the trigger is withheld it takes
whatever action is first on offer: read `FreepsHasOptionalTriggerAvailable` before each accept of a run (dec-b19_2).

**A player barred from taking cards into hand draws only when reconciling.** At a table where a player holds
Race Text 94_30 (`CantTakeCardsIntoHandFromDeckOrDiscardPile`), a card drawn is a card taken into hand from the
draw deck (Q-0206): every "draw N" that player makes draws nothing, its costs paid, and the play reads `"effect"`
on the no-effect flag. His reconciles draw as ever (the regroup one and a card's "reconcile your hand"), and the
opening hand is dealt. The bar is read for the player the text has draw or take, whoever's card it is on, and
only for a card of that player's own (Q-0207): his opponent's A New Light 17_15 returns his minion to his hand;
his opponent's Change of Plans 1_99 has him shuffle his hand in and draw none (Q-0216, kept unless struck).
Nobody is asked to prevent a draw that is barred, and an optional trigger or a no-cost ability whose whole effect
is his draw is not offered to him (Q-0262: B). `mechanics/deck/BarOnTakingCardsIntoHandTests`.

**A card being shown is listed as not selectable.** A real reveal from a deck or from play gives every player,
the owner included, a "Revealed card(s)" decision to dismiss. A reveal from a HAND (the whole hand, or cards at
random) gives the pop-up to the other players only: the hand's owner is shown nothing and has nothing to dismiss
(Ketura on Q-0024 and Q-0036; the game log names the cards). A reveal from a hand that has no card to show (an
empty hand, or no card of the kind asked for) gives nobody a pop-up: there is nothing to dismiss, and the game log
says "There were no cards in <player>'s hand for <card> to reveal", or "<card> revealed no cards from <player>'s
hand" when the hand holds cards (Q-0144; since F-0420 no printed card comes to the second line: Mallorn-trees
1_54 is not offered with no [elven] card in hand). In the decision
`FreepsHasCardChoiceAvailable(card)` answers false:
`FreepsHasCardChoiceAvailable(card, false)` is the reading for a card that is shown. A definition that only sets
`reveal: true` on moving a card writes a line in the game log and shows nobody anything.
An event that reveals its player's hand and then chooses a target ("reveal your hand and make a minion ...") asks
the other player's dismissal of the hand first, and the choice after it (Inspiration 11_36).

**A card made known by the game log alone.** A card taken from a draw deck by its type (`reveal: true` on the
taking), and a card revealed from a hand to a player who gets no pop-up, is told to the other player by a line in
the game log and nothing else. `MarkGameLog()` just before the action starts the reading;
`GameLogSinceMarkMentions(cards...)` is true when every card given is named in some message since the mark.
Assert it for the cards taken or revealed and assert it false for a like card that was not, with
`GetGameLogSinceMark().toString()` as the failure message. A card is matched by its link (blueprint id and full
name): two copies cannot be told apart, and a card the log names for another reason is named too, so where a copy
of the event being played is among the cards taken, set the mark after the play. For something the log says in
words, with no card to name (a reveal from a hand that had nothing to show), `GameLogSinceMarkSays("no cards in")`
is true when some one message since the mark contains the words, whatever their capitals: give the fewest words
that tell the line from every other. All three readers fail the test if no mark was set. A take followed by a discard at random from the same hand (Not Easily Avoided 18_32) may discard the card taken, which the log then names for the discard: a `reveal: false` mistake is caught only on the runs where the card taken stays (four in five on 18_32's board), so run such a test several times, and say so in the ledger where no board keeps it in hand for certain.

**A hand played revealed is read from what each seat is sent** (G-0037; Race Text 92_7 is the one card with
such a hand). `FreepsHandIsRevealed()` / `ShadowHandIsRevealed()` answer whose hand the game stats tell every
client is revealed; `FreepsSeesCardInHand(card)` / `ShadowSeesCardInHand(card)` answer whether that player's seat
has been sent the card as a card in a hand (his own, or one revealed to him) and no removal since. Each reads the
seat's channel at the table and a client joined at that moment, and fails the test if the two differ: a hand
revealed part-way through a game by an ad hoc modifier is refused so (the cards already in it were sent to
nobody at the table). Never read `getModifiersQuerying().isHandRevealed` or attach a channel in a test.

**A shuffle into a draw deck always shuffles.** Taking "you may shuffle up to 2 ... from your discard pile
into your draw deck", or "shuffle any number of ... in play into your draw deck", and choosing none, or with
nothing there to choose, reads one more shuffle of that deck and its count unchanged; declining an offer reads
none (Q-0102, Q-0112). A shuffle of exactly one behind a cost does the same: Grimir 1_17 ("Exert Grimir to
shuffle a [dwarven] event from your discard pile into your draw deck", `shuffleCardsFromDiscardIntoDrawDeck`
with a `choose(...)`) is offered with no such event in the pile (Q-0034: it has a cost); taken, he exerts,
nobody is asked for a card, and the deck is shuffled once more with its count unchanged (`Card_01_017_Tests`).
So does an event's shuffle of one card FROM PLAY with no target there (F-0665: still playable): The Fortunes of
Balin's Folk 15_4 with no [dwarven] condition holding a token shuffles the deck all the same, and Restless Axe 4_55
is built alike; do not read that deck's shuffle count as unchanged on that board (`Card_15_004_Tests`).

## Hinder, The One Ring and Maps

**A wound a worn Ring turns into a burden is a wound paid (F-0507, engine pass 42).** `negateWound` (every Ring's "add a burden instead", Held 6_109, Force of Uruk-hai 11_184) places no wound and sets off nothing that answers a wound taken, but the wound counts as done: a cost or a prevention's price paid that way is paid and what it buys happens (Faramir 17_28; Morgul Brute 7_188's burden is prevented by the Ring-bearer's wound paid as burdens: `mechanics/actions/InsteadTests`). `preventWound` is a prevention, and a cost prevented is still unpaid. What is offered is unchanged: a wound cost on a character who cannot take one is not offered, Ring or no Ring.

**The Ring-bearer is the rules' to protect, hindered or not.** A discard or a return to hand that reaches hindered cards (`override: hindered`: "a card", "a Free Peoples card") neither offers nor takes a hindered Ring-bearer (CR 4.2 "Ring-bearer"; F-0174), so a definition needs no `not(ring bearer)` there, and with him the only candidate the effect does nothing. The engine's effects refuse him for themselves as well as the definition words' choices do: `DiscardCardsFromPlayEffect` (when it has a source card), and `ReturnCardsToHandEffect` since engine pass 27 (`mechanics/hinder/HinderedRingBearerTests`, which aims each at him). What another CARD forbids about cards gives a hindered card nothing: beside Uruk Follower 4_186 a hindered unbound Hobbit is discarded.

**A guard for a thing no card should be able to do is stapled on, never an invented card** (Ketura, 2026-10-11, Q-0241; mechanics classes only). The manner is in `mechanics/ring/TheOneRingIsNeverDiscardedTests`: once the board is laid, `scn.ApplyAdHocAction(new AbstractActionProxy() {...})` whose `getPhaseActions` gives the one player, in the one phase, an `ActivateCardAction(troop, playerId)` on a card with no text (Goblin Patrol Troop 1_177; Frodo for the Free Peoples side) with the engine's own effect appended, aimed at the cards by the test; then `ShadowUseCardAction(troop)` and the board is read. The filters handed to `DiscardCardsFromPlayEffect` are joined with AND (several cards: `Filters.or(a, b)`), it asks no bar when its source is null, and it and `ReturnCardsToHandEffect` reach hindered cards only with `SpotOverride.INCLUDE_HINDERED`. The line needs its reason (`// lint-ok: ...`).

**A hindered card that is shuffled into a draw deck is hindered no longer.** `IsHindered(card)` is false once the card is in the deck, as it is for a card discarded or returned to hand (CR 5.0 "hinder"; `mechanics/hinder/HinderedCardLeavingPlayTests`).

**A hindered card is still borne.** "Bearing 3 or more cards" and "for each card he or she bears" count a
hindered card; "for each possession" does not. Hinder one of the counted cards with `HinderCard` and read
the count unchanged.

**A card on a hindered bearer is not borne by "a companion".** Nor by "a Hobbit", "a [rohan] Man" or "Gandalf":
a hindered bearer is found by no kind and no title (CR 5.0 "hinder"; Q-0108), so "discard a condition borne by a
Hobbit" does not offer the condition on a hindered Hobbit, and it is offered again once he is restored. The
engine does this for every `attachedTo(<a kind>)`; no definition needs `not(hindered)` for it. What names the
bearer as a card still finds what he bears: "bearer", a companion the ability has chosen, the Ring-bearer, "each
attached follower" (`mechanics/hinder/BorneByAHinderedBearerTests`). So does a text that only rules a kind of bearer out
(`attachedTo(not(ally))`, Q-0252): Asfaloth 1_31 is discarded underground from a hindered companion as from any
other, and stays on a hindered ally.

**The rig reads a hindered card's printed numbers.** `GetStrength`, `GetVitality`, `GetResistance` and
`GetTwilightCost` of a hindered Aragorn 1_89 read 8, 4, 6 and 4: nothing that applies to him is counted, and
the print is not hidden. So a hindered card's own number shows nothing about hinder. Read what the rule changes:
the choice, spot or count that leaves him out (before, hindered, and again once `RestoreCard` has brought him
back), or, for a bonus he should have missed, his number after he is restored
(`mechanics/hinder/HinderedCardCannotBeSpottedTests`).
Keywords go the same way for what is printed and not for what was given: `HasKeyword` of a hindered Aragorn
reads RANGER true (the engine answers from the print before it looks at hinder; a restore may pick a hindered
card by a printed trait, CR 5.0 "restore") and a defender +1 he gave himself false. `GetKeywordCount` goes the
same way: a hindered Gimli 1_13 bearing Gimli's Battle Axe 7_9 at 3 threats reads DAMAGE 1, his print without the
axe's +1, and 2 again once restored, so a given bonus can be read there (Card_07_009_Tests). Read a hindered card's
printed keyword by what it does: the archery total for an archer (Legolas 1_50: 1, hindered 0), another card's
spot (`mechanics/hinder/HinderedCardHasNoGameTextTests`). A bonus given "until ..." is back when the card is restored
before then (as built; Q-0222 asks).
What a borne card gives is not its hindered bearer's either: Aragorn's Bow 1_90 on a hindered Aragorn reads the
fellowship archery total 0 (1 again on restoring), and a bonus that a borne card's ability aims at its hindered
bearer is never laid (Gwemegil 1_47 used by discarding two cards: Arwen reads 6 + 2 once `RestoreCard` has brought
her back). That is not Q-0222's bonus, which was laid before the hindering (`mechanics/hinder/CardsBorneByAHinderedCardTests`).

**The One Ring is never discarded.** A discard that could name it (Doom Causeway 103_127, "each time a card is
hindered, discard it") neither offers nor takes it, hindered or not; hindered there, it stays on its bearer,
hindered. The engine's discard aimed straight at it leaves it too (`mechanics/ring/TheOneRingIsNeverDiscardedTests`).
Nor does it go to the discard pile with a Ring-bearer who is killed (Q-0232): it stays in play, in his zone
(`assertInZone(Zone.FREE_CHARACTERS, ring)`), borne by nobody, while his other cards are discarded; so the discard
pile counts one fewer than "everything he bore", and `GetAttachedCards` of the dead Ring-bearer is empty. Killed as
the fellowship character of a skirmish, he leaves it drawn in his place there (`FreepsSeesCardInSkirmish(ring)`),
fighting nobody (`IsCharSkirmishing(ring)` is false). It lies there while Sam 1_311's response is on offer and is
taken up from there; with no new Ring-bearer the game ends with it there
(`mechanics/ring/TheOneRingWhenItsBearerIsKilledTests`). A card's test asserts none of this for itself.

**No cheat puts The One Ring on: the Ring's own text does, and which Ring decides how.** `GreatRing` (19_1) is
put on by its own Maneuver action at no cost (`FreepsUseCardAction(scn.GetRing())`, Card_V2_065_Tests) and
`ATARRing` (4_1) by a Skirmish action for a burden; `RulingRing` (1_2) and `IsildursBaneRing` (1_1) only in
answer to a wound. `IsildursBaneRing` answers a wound in any phase, so a test with it lays an archer (Goblin
Marksman 1_176), accepts the Ring's offer in the archery phase and leaves the archer out of the assignments with
`ShadowDeclineAssignments()` (Card_02_108_Tests). `RulingRing` answers a wound in a skirmish and no other (an
archery wound offers nothing): the Ring-bearer loses a skirmish, or Orc Soldier 1_271 wounds him in one, and the
offer is accepted there (Card_01_002_Tests). Three printed cards put ANY Ring on outside a skirmish, for no
burden: Saruman's Reach 1_137 in the maneuver phase ([isengard] event, cost 0, exert an Uruk-hai: the Free Peoples
player chooses between exerting 2 companions and the Ring, and with the Ring-bearer the only companion nothing is
asked and it goes on; Card_04_001_Tests reads maneuver and archery wounds with the Ring worn that way),
Resistance Becomes Unbearable 2_79 (Maneuver: a twilight Nazgûl exerts to exert the Ring-bearer, and it goes on if
he is then exhausted) and Narchost 7_362 (Shadow, 8 burdens spotted). `RBWearingOneRing()` reads it; every one
comes off at the regroup phase (1_1 put on IN a regroup phase: at the end of that phase).
The Ring of Rings 11_1 (given as the string) answers a wound in any phase too, for 1 burden. Two Rings, each
given as the string, are put on by a Maneuver action with a cost: Such a Weight to Carry 7_2 for a burden, and
The Binding Ring 9_1 for an exertion of its bearer (9_1 prints strength +1 and vitality +1, so Frodo, Son of
Drogo reads 4 and 5 and a minion of 8 overwhelms him: Card_01_224_Tests gives him Hobbit Sword 1_299). A companion who
becomes the Ring-bearer is not wearing the Ring though the one who died was: to read that, keep another skirmish
waiting, since the regroup phase that follows would have taken any Ring off (mechanics/ring/WearingTheOneRingTests,
which holds what the rules say of a Ring worn; what a Ring does while worn is its own file's).

**Maps and meta-sites cannot be hindered,** as sites cannot: a hinder of "a card" on a PC-Movie or Race to
Mount Doom table does not offer them, and the engine's hinder aimed straight at them leaves them
(`mechanics/hinder/MapsAndMetaSitesCannotBeHinderedTests`). The cheat `HinderCard` hinders whatever it is given and asks
no rule.

**A fellowship with no unhindered companion is not asked to move again (F-0315).** CR 5.0 "hinder": "During
the Regroup phase, if the fellowship has no unhindered companions, the Free Peoples player cannot choose to
move." With every companion of the player whose turn it is hindered, "Do you want to make another move?" is not
asked: the fellowship stays, the game log says "<player> has no unhindered companion and cannot choose to move
again.", and its player reconciles (which restores them). A card that makes the Free Peoples player move again
(`HasToMoveIfAble`: No Retreat 4_30) gives way too. So `FreepsChooseToMove()` after hindering the Ring-bearer
alone fails; for a second move with a hindered Ring-bearer, cheat in a plain companion who stays unhindered
(Dwarf Guard 1_7) and count 1 for him in the move's twilight and nothing for the hindered one (92_25). The move
of the fellowship phase is no choice and is made whoever is hindered
(`mechanics/hinder/NoUnhinderedCompanionNoRegroupMoveTests`).

## Tables and formats

**A Decipher-block site goes under the key of its own printed number.** The game finds the site at a position by
the number printed on it (`GameState.getSite`), not by the map's key: White Rocks 4_346, a site 4, put in as `site3`
of a Towers path leaves no site 3, and the move there throws a NullPointerException in `movePlayerToNextSite` (dec-b11_6).

**A Shadows-block site path.** Pass `VirtualTableScenario.Shadows` as the format and a site map of your own;
`StartGame(scn.GetFreepsSite("site1"))`; after the fellowship phase is passed the Shadow player lays the next
site with `ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"))`. `Card_13_164_Tests` is a worked example:
it also lays cards while the Shadow player's decision is pending (`ShadowRefreshDecision()` before a play is read
or made) and walks to the first player's second turn with a site controlled (`SkipToOtherPlayersTurn()` twice,
`ShadowTakeControlOfSite(site1)`, then the move), the board for a site that is no longer on the adventure path.
The turn-skippers (`SkipToSite`, `SkipCurrentSite`) cannot walk such a path, but it can be walked by hand to any site: each move is `FreepsPass()`
(a second move in the turn: `SkipToMovementDecision()`, `FreepsChooseToMove()`) and then
`ShadowChooseCardBPFromSelection(...)`, with `SkipToOtherPlayersTurn()` twice between the first player's turns;
site 6 takes three of his turns (`Card_V0_001_ErrataTests`). `SkipToOtherPlayersTurn()` goes through wherever
the turn's own move was made by hand first or the next site already lies (the second player follows along the
sites the first player's moves laid), so the start of the first player's SECOND fellowship phase on a given site
is `StartGame(another site)`, `FreepsPass()`, the Shadow player lays the site, the board is laid there, and
`SkipToOtherPlayersTurn()` twice: the way to a site's "at the start of your fellowship phase" text that reads what
no first turn has, a dead pile (`Card_11_255_Tests`, whose last test reads five turn starts, both seats).
The same walk is the board for a site's "at the start of the <phase>" text answered again, by the other seat:
`SkipToOtherPlayersTurn(); ShadowPass();` takes player 2's fellowship onto the site player 1's move laid, then
`SkipToPhaseInverted(<phase>)` leaves its start-of-phase trigger pending for player 1 (Isenwash, `Card_13_193_Tests`).
`MoveCardToAdventurePath` works on this path too: the
card becomes the site ahead, the next move goes to it and the Shadow player is not asked to lay one (the same
file puts the first player's own copy of a site at site 6 so). A site so laid ahead is a site in play, so it is the
cheap way to build a count of sites ("while you can spot 3 underground sites": Card_13_120_Tests lays one ahead
for two, and a site replaced makes three). For a high site number or a later region (sites 4-6 are region 2,
7-9 region 3), where the card's text names no block's sites, it is shorter to put the Shadows-block site on a Fellowship-block
path instead: a site map of `FellowshipSites` with the card in place of site n, `SkipToSite(n - 1)`,
`MoveCardToAdventurePath(scn.GetShadowSite("siten"))`, `FreepsPass()`, and the fellowship moves to it
(`Card_11_227_ErrataTests`, site 7; `Card_04_355_ErrataTests`). `MoveCardToAdventurePath` makes the card the
site after the current one whatever key the map gave it, so one scenario with the card in place of a site no
test reaches (`putAll(FellowshipSites); remove("site9"); put("parapet", "63_188")`) serves every site number
(`Card_13_185_Tests`, sites 3, 4 and 7, with no other card in its maps; `Card_13_188_ErrataTests`: sites 4 to 7).
So only ONE site can stand ahead of the fellowship in a phase: a count of sites on the path that wants two Shadows-block
sites ahead (two dwellings) spreads them over two tests, each at its own site number (`Card_13_152_Tests`).
The cheat gives the site its number before it reaches the path, as a site played is given it, so a site whose
text reads its own number as it comes into force is laid like any other: Mount Doom 15_193 threw a
NullPointerException until engine pass 28 (F-0484; `virtualtable/AdventurePathLayingTests`).
The key no longer matters (engine pass 24): the path is ordered only while the FIRST card of the adventure deck
has a site number (`GameState.usesUnorderedSites`), and the rig now puts the sites that have a printed number
into each adventure deck ahead of those that have none, so a Shadows-block site on a numbered path may stand
under any key, `site9` included. (The deck used to be built in the site map's HashMap order, and a key that came
out ahead of every `siteN`, `homestead` or `path` say, made the whole path unordered: `StartGame()` was left
resting on "Choose site to play". The landed scenarios chose their keys round that and are unchanged, and a
ledger Note that calls a capitalised key needed, 68_136's "Mithlond", is out of date: 17_147 and 18_136 use lowercase.) The other
way about, one numbered site among Shadows-block sites makes the path a numbered one, and with no site 1 in it
the table is refused when it is built, saying so (`virtualtable/FirstSiteTests`).
On an unordered path the constructor rests on the first player's "Choose site to play" with all nine offered
(`FreepsGetBPChoiceCount()`), the Shadow player's choice at each move is among the sites left in his deck, his
copy of a site already laid included, and the move adds 3 more twilight for a site in region 2 and 6 in region
3; a numbered path adds none in any of the three Movie block formats for a numbered site, but does for a Shadows-block site laid on it (above, "Set the twilight pool") (`mechanics/sites/PlayingSitesTests`, which also
has the next site played by a card, a site replaced on each kind of path, and no site 10).
`StartGame(site)` with a site named gets past the first question, but the path is still unordered: every move
asks the Shadow player to lay a site, and `SkipToSite` halts in the fellowship phase.
On a numbered path nobody is asked "Choose site to play" at all: each player's deck has one site of each number
and the next is laid without a question, so an old test's helper that answers it under an `if` answers nothing
and goes (Card_01_342_Tests had two).
A Shadows-block site cannot stand as `site1` of such a path: the game lays site 1 by its printed number and
would lay nothing, and the constructor refuses the site map with an IllegalArgumentException that says so
("These sites cannot be set up: the game would lay no site 1 ..."; virtualtable/FirstSiteTests). Where its text
is needed in the first fellowship phase, make the whole path unordered: `putAll(ShadowsSites); put("site1", id)`
(the default format will do) and `StartGame(scn.GetFreepsSite("site1"))` start at that site with its text in
force; or an ad hoc modifier that does what the site does stands in.

**"Replace the fellowship's current site with a site from your adventure deck" wants a Shadows-block path.** On
a numbered path the site under the fellowship on player 1's first turn is the Shadow player's own, and his
adventure deck holds no other site of that number: Úlairë Nelya 11_222's ability is offered, he exerts, and
nothing is replaced. On a Shadows-block path the Shadow player holds the sites not yet laid:
`ShadowUseCardAction(nelya)`, `ShadowChooseCardBPFromSelection(site)`, and `GetCurrentSite()` is the new one
(Card_13_021_Tests). The choice is read by blueprint, so the other player's copy of a site is a candidate NOT
offered only while the chooser's own copy of that blueprint is out of his adventure deck (laid on the path, say);
with his own copy still in the deck it reads as offered (Card_13_120_Tests, Card_11_143_Tests). The only Free
Peoples card that replaces the current site is There's Another Way 12_40, in the regroup phase. `VirtualTableScenario.ShadowsSites` is a ready site map for such a path, nine different sites
under `site1` to `site9` (Fangorn Glade 11_239, Abandoned Mine Shaft 13_185, ... ; Sirannon Ruins 18_138,
`site5`, has no text): `StartGame(scn.GetFreepsSite("site1"))`, `scn.GetShadowSite("site2")`
(Card_13_133_Tests). Its terrain matters to a terrain word: `site1` (Fangorn Glade) is a forest and `site6`
(Buckland Homestead 11_230) a dwelling, so a test of "at a forest" that starts there is already at one. Savage Southron 15_92 lays a site of the FREE PEOPLES player's adventure deck, picked at
random: the game shows the pick to each player first (`FreepsDismissRevealedCards()`,
`ShadowDismissRevealedCards()`), and a test reads whose site lies there and that the others are still in his
deck, never which (one time in eight it is the twin of the site replaced: Q-0226; Card_15_092_Tests).
The replacing is the Shadow player's (F-0715, engine pass 73): Many Miles 13_21 and Riddermark Tactician 13_133
read it, and a bar on the Free Peoples player replacing the current site (Orkish Camp 15_113) does not stop it.
`SetFreepsAdventureDeck(sites...)` and `SetShadowAdventureDeck(sites...)` leave a player's adventure deck holding
exactly the sites named, none named being an empty deck (G-0054, engine pass 26): the board for "if the Free
Peoples player has at least 1 site in his or her adventure deck". The other sites are set aside in no zone
(`assertNotInZone(Zone.ADVENTURE_DECK, ...)` reads them); follow it with `ShadowRefreshDecision()` where a choice
of actions is already pending, and let no move follow that needs a site from a deck left without one (the game
throws).

**The sites of `TowersSites` have texts that reach a test.** Site 1, Eastemnet Gullies 4_325: "Fellowship: Exert 2
unbound companions to play Legolas from your draw deck" (a Legolas in the deck of a player with two unbound
companions who can exert is in its reach). Site 2, Eastfold 4_331: plains; a Shadow action that needs 3 [isengard]
trackers (it bars another move: put a card in the mover's hand, or `SkipToMovementDecision()` runs on into the
other player's turn; the pitfall "A mover who is barred from choosing to move"). Site 3, Barrows of Edoras 4_337: a sanctuary; "Fellowship: Place your hand beneath your draw deck and draw
4 cards", on offer to the mover with a card in hand and, since F-0429, with none while he may still draw a card
(Q-0249: `CanDraw`): `FreepsActionAvailable(<site 3>)` is true there, and a test that counts the fellowship actions on offer at
site 3 counts it. Site 4, White Rocks 4_346: a battleground; its Shadow number is +1 for each mounted companion, so a test that mounts companions and moves to site 4 counts them in the pool.
Site 5, Deeping Wall 4_348: a battleground; "Shadow: Play Saruman from your draw deck". Site 6, Caves of Aglarond
4_352: underground, a sanctuary; a Maneuver action that makes your Uruk-hai fierce. Site 7, Helm's Tower 102_71: a
battleground; each character damage +1 while 6 companions can be spotted. Site 8, Ring of Isengard 4_358: "Shadow:
Play a [dunland] minion to draw a card". Site 9, Palantír Chamber 4_363: "Shadow: Remove 2 burdens to play a minion
from your discard pile", on offer at site 9 with 2 burdens and a minion in the discard pile (dec-b12_18).
A Towers site under test may itself be one of these nine, and the sentence naming it is then withheld in stage 1:
compare its number and title with `VirtualTableScenario.TowersSites`; where it is there, read it with
`GetFreepsSite(n)` and do not lay it again, and expect the other eight sites' texts on the same path.

**The sites of `KingSites` have texts that reach a test.** Site 1, Edoras Hall 7_330: each [rohan] possession is
twilight cost -1 while the fellowship is there. Site 2, King's Tent 7_335: a Shadow action (initiative and a minion of
his). Site 3, The Dimholt 8_117: underground, a sanctuary; "Fellowship: Add 3 threats to play an enduring companion
from your draw deck". Site 5, Pelennor Flat 7_345: at the start of each maneuver phase there the Free Peoples player
must choose to discard his hand or add 2 burdens (asked with an empty hand too: Q-0255), so a test in site 5's
maneuver phase answers that first. Site 6, Minas Tirith Seventh Circle 7_350: a regroup move from it heals each
[elven] companion. Site 9, Dagorlad 7_360: the move to it adds 3 threats (up to the limit), so a test that reads
threats at site 9 counts them in (Card_07_360_Tests). That trigger is required, so another card's required trigger on the
move to site 9 makes an ordering decision first: `FreepsAwaitingRequiredResponseOrder()`, then
`FreepsResolveActionOrder("<title>")` (Barren Land 10_79, Card_10_079_Tests). A test of a card whose text names no block, that needs three or more controlled sites
and then a maneuver or regroup phase, takes `FellowshipSites` and so stays clear of Pelennor Flat (Card_08_107_Tests, Card_08_108_Tests).

**A battleground or a forest site needs no cheat.** The Towers path has both at site 2: Uruk Camp 4_335
(Battleground, Plains; each Uruk-hai played must exert) and Derndingle 4_330 (Forest; move limit +1 while an
unbound Hobbit is there). `TowersSites` has neither word at site 1 (4_325) or site 3 (4_337, a sanctuary), so
`putAll(TowersSites); put("site2", "4_335")` (in a SECOND `HashMap` argument, after the card map and in place of
`TowersSites`; inside the card map it does not compile: Card_12_052_Tests) and the fellowship's own two moves (`FreepsPass()`, then
`SkipToMovementDecision()` and `FreepsChooseToMove()`) show "while at a battleground site" off, on and off again
(`Card_15_017_ErrataTests`, which reads one site of each word only; the Standard's two sites per terrain word, Uruk
Camp + White Rocks and Trollshaw Forest + Rivendell Waterfall, are laid in `Card_11_021_Tests` and `Card_15_017_Tests`). An Uruk-hai with a "When you play" trigger of its own, played there, puts the FREE
PEOPLES player (the site's owner) to ordering the two required triggers: `FreepsAwaitingRequiredResponseOrder()`,
then `FreepsResolveActionOrder("Uruk Camp")` (Card_11_189_Tests). The Fellowship path has neither: `FellowshipSites` has no forest (1_351 is a
sanctuary only) and no battleground. Rivendell Waterfall 1_342 (Forest, Sanctuary), put in as site 3, is one that
`SkipToSite` reaches; with Derndingle 4_330 it gives two forests that differ in the sanctuary (Card_02_035_Tests).
Plain `TowersSites` already has a battleground and its nearest miss on one path: Eastfold 4_331 (Plains, no
battleground) at site 2 and White Rocks 4_346 (Battleground only) at site 4, reached by `SkipToSite(3)` and the
Shadow phase after it (Card_11_106_Tests). Underground likewise: Balin's Tomb 1_343 (Underground only) is
`FellowshipSites`' site 4, and The Dimholt 8_117 (Underground, Sanctuary) `KingSites`' site 3, reached by
`SkipToSite(2)` and a move; with Ettenmoors 1_331 (Plains) as the `-`, "a battleground or underground site" is laid
on three paths and no cheat (Card_18_088_Tests).

**A Hobbit-set card (sets 30 to 33) stands on the Hobbit table.** `new VirtualTableScenario(cards, null, null, null,
HobbitDraft)` is laid on the Hobbit Draft Game format's own rules (engine pass 77): the Hobbit path `HobbitSites`
(30_49 to 30_57, by printed number), the Ring-bearer `BilboRB` (Bilbo, Reliable Companion 30_46; the other Bilbos
30_43 to 30_45 by id), and NO card in the Ring slot: `GetRing()` is null and a ringID given is refused. The HDG's One
Ring is `HobbitRing` 30_48, a [shire] artifact of the draw deck: name it in the card map and put it on Bilbo by
playing it (cost 0, Bilbo its only bearer) or with `AttachCardsTo(scn.GetRingBearer(), ring)`; it is then worn by its
own Skirmish action, and the game's rule takes it off at the start of the regroup phase as any Ring
(virtualtable/HobbitTableTests). The rules come from the format, not the sites: Hobbit sites at a Multipath table
keep Frodo and The Ruling Ring. The path's sites all have texts that reach a test (`TestConstants.HobbitSites`
lists them; its only battleground is site 9, Battle of the Five Armies 30_57; Northern Slopes 32_49 is the other printed site-9 battleground, and a battleground below site 9 is made with Watchful Orc 30_40's Shadow ability, as Card_32_027_Tests, Card_32_032_Tests and Card_32_033_Tests do). The seven older files under `cards/unofficial/hobbit/` predate the
table and stand on `FOTRFrodo` and `RulingRing` until their batch moves them (Card_30_051_Tests now stands on the Hobbit table). The Free Peoples Pack's other sites 1 and 2 (Bilbo's Kitchen 30_58, Trollshaw Forest 30_59) are laid by giving the five-argument constructor a full nine-site map (`site1` to `site9`, 30_49 to 30_57 but for the one swapped) as a SECOND `HashMap` argument after the card map, then `null, null, HobbitDraft` (`new VirtualTableScenario(cards, sites, null, null, HobbitDraft)`; the sites put into the card map with `cards, null, null, HobbitDraft` fail at construction with "Unable to create game, due to either ring-bearer or ring being invalid cards": hobb-b32_18); no helper method may build it, so each file writes the map out (Card_30_058_Tests, Card_32_018_Tests). Trollshaw Forest 30_59's "each player may" is a REQUIRED start-of-maneuver trigger that asks each player in turn a yes/no (`forEachPlayer` + `optional`), the fellowship's player first, and asks a player only with a hand weapon he could play in his draw deck (engine pass 79): read it with `FreepsDecisionAvailable("hand weapon")` and `FreepsChooseYes()`/`No()`, then the Shadow side's, not as an optional trigger (Card_30_059_Tests, Card_32_018_Tests). A Hobbit site 1 prints no Shadow number (Bag End 30_49, Prancing Pony 33_55): its definition's `twilight: 0` stands for the blank, and the stats test asserts `0` from `getTwilightCost()` (Card_30_049_Tests, Card_33_055_Tests). Esgaroth 30_54 (site 6)
offers an optional trigger at the start of the regroup phase; `SkipToSite` declines it on both turns (G-0065, engine
pass 78), but a test that stops in that regroup phase itself declines it before `PassRegroupActions()`
(`FreepsDeclineOptionalTrigger()`); the offer is a play from the draw deck with `mustExist` (F-0804, engine pass 79): it is not made with no [esgaroth] or [dwarven] ally the player could play in the draw deck, so a test that declines it lays such an ally there first (`MoveCardsToBottomOfDeck(dain)` before the move: Card_32_002_Tests, Card_30_027_Tests), and one with none reads no offer (Card_30_022_Tests); `SkipToMovementDecision()` called there does not pass it either, so Dale 30_64
reached by the regroup move from site 6 is `SkipToPhase(REGROUP)`, `FreepsDeclineOptionalTrigger()`,
`PassCurrentPhaseActions()`, `ShadowDeclineReconciliation()`, `FreepsChooseToMove()` (Card_30_064_Tests). Erebor (site 7) with a minion that must stay at a mountain laid there is
`SkipToSite(6)`, `SkipToPhase(Phase.SHADOW)`, the minion (Card_30_044_Tests; Card_30_027_Tests plays the move by hand, as
was needed before). Mirkwood 30_53 (site 5) has a REQUIRED "discard Gandalf" trigger on the move to it, fired with no Gandalf in play, so another card's required trigger on that move ("moves to site 5", "each time the fellowship moves") leaves a "Required responses" order for the Free Peoples player: `FreepsResolveActionOrder("<card>")` (Card_31_029_Tests, hobb-b31_29); the other site 5s, Old Forest Road 30_62 and Forest River 31_46, have no such trigger. Watchful Orc 30_40's "make each site on the adventure path gain battleground ... until the end of the turn" reaches only the sites laid when it is used: a site the fellowship moves to later that turn is no battleground, so a board that wants one there uses a second Orc at that site (Card_30_051_Tests, hobb-b30_66). The Troll Hoard 30_50 (site 2) makes an optional offer of its own at the start of each player's fellowship phase there, but only with a hand weapon that player could play in his draw deck (`mustExist`, F-0800, engine pass 79): with none (no hand weapon in the map, or no bearer for it in play) nothing is asked and no decline is written; a `FreepsDeclineOptionalTrigger()` with nothing pending answers the phase's actions instead and moves the game on (Card_30_060_Tests failed so). A board that wants the offer lays the deck BEFORE `SkipToSite(2)`, eight cards with no text on top for the reconciles to draw (Card_30_050_Tests); a start-of-turn trigger answered at site 2 then leaves the offer pending and `AwaitingFellowshipPhaseActions()` reads false. A deck laid after the offer is worked out does not change it. An alternate site of a Free Peoples Pack goes under its key after the path: `putAll(VirtualTableScenario.HobbitSites); put("site3", "30_60");` (Card_30_060_Tests to Card_30_063_Tests). A site of another block cannot be laid on the Hobbit path: with Heights of Isengard 11_244 as `site2` of a full nine-site map at a `HobbitDraft` table the game stops on the move to site 2 (a NullPointerException, no site card there: the path places sites by printed number and a Shadows-block site has none), so a site-doer board is not shown for a Hobbit card (Card_33_034_Tests' ledger, hobb-b33_30). A Bilbo under test who is the
Ring-bearer by id is read with `scn.GetRingBearer()`, in the stats test too, its blueprint id asserted (Card_30_043_Tests). Bilbo's strength 3 is overwhelmed by most Hobbit minions (6 or more, a damage +1 minion of 7 too) while The One Ring is not worn, and the Ring-bearer's death ends the game without a word: both players' decisions read null and `GameIsFinished()` is true (hobb-b30_41). A minion that must beat Bilbo and leave him alive is Dark Bats 32_30 (strength 4, vitality 2; its Shadow ability left unused), not Orkish Aggressor 30_39 (6), which kills him (Card_31_021_Tests, hobb-b31_21). Thrór's Key 32_11 in play removes (2) on every move from site 6 or 7 (a required trigger), so a test reading the pool across the move to site 7 or 8 counts it; it fires before the new site's Shadow number is added (CR 4.2), so with the pool at 0 it removes nothing (Card_32_048_Tests, hobb-b32_48). Dain Ironfoot 32_2 in play makes an optional offer at the start of every regroup phase while a [dwarven] follower is in play (Dori 30_8 on Thorin), so `AwaitingFreepsRegroupPhaseActions()` reads false there: read `FreepsHasOptionalTriggerAvailable(dain)` instead (Card_32_035_Tests, hobb-b32_11). Gandalf, Leader of Dwarves 30_25 stops `SkipToSite`: his optional "Each time you reconcile, you may draw 2 cards" is left pending in the regroup phase and the skipper fails ("SkipCurrentSite expected to end between turns or in the Fellowship phase, but ended in REGROUP"); Gandalf, The Grey 30_27 does not, so a board that skips with a Gandalf in play lays him (hobb-b31_25).

**A kind the Hobbit sets do not print is a Decipher card on the Hobbit table.** The Hobbit sets print no [wraith] Orc and no Free Peoples condition, so a Hobbit card's test that wants one lays a Decipher card on `HobbitDraft` (Morgul Hound 7_192, Morgul Mongrel 7_194; There and Back Again 1_317 on Bilbo), as for a Decipher Elrond (Lore of Imladris 30_13): Card_32_058_Tests, Card_32_061_Tests (hobb-b32_58). Nor do they print a Free Peoples card that makes the Shadow player discard his own condition: Introspection 12_29 with Gandalf 30_26 is laid for it (Card_33_034_Tests, hobb-b33_30).

**"The fellowship's next site" is not on the table in the regroup phase.** The rig lays a site only as the
fellowship moves to it, and the second player is never ahead, so a `nextSite` filter matches nothing until a
site is laid: by a legal play (Pathfinder 1_110, a ranger spotted) or with `MoveCardToAdventurePath` of the
site, the board where the other player had moved on ahead (Card_13_114_Tests, dec-b13_114).

**Both players hold the same nine sites.** The rig gives both seats the one site map, so a card that plays "your"
site where the other player's lies replaces it with its twin, a card of the same title: read whose it is, not
what it is (`assertSame(freepsSite2, scn.GetSite(2))`, each player's site fetched with `GetFreepsSite(2)` and
`GetShadowSite(2)` before it is laid; `Card_01_110_Tests`). Whether a twin may replace a twin at all is Q-0226
(CR 4.2 "site": "You can't replace a site card with the same site card"; the game allows it): a new test that
replaces a site reads the game as built, stays active, and names that question on the ledger's `Findings:`
line (also where the twin is only among the sites offered and no test takes it: Traveled Leader 12_34), with Q-0238 where the wording is "(replacing opponent's site if necessary)" (Notes alone are not enough). The board for "(replacing
opponent's site if necessary)" that a game reaches: the first player at site 3 (for a Fellowship action
`SkipToSite(3)`, Card_02_026_Tests; for a Regroup one `SkipToSite(2)`, `FreepsPass()` and
`SkipToPhase(Phase.REGROUP)`, Card_05_023_Tests; a card that also answers "each time the fellowship moves",
as set 7's threat conditions do, is cheated into play only after those moves, or each move asks it first:
Card_07_024_Tests, Card_07_325_Tests, dec-b7_323), the second
player's site 4 laid with `MoveCardToAdventurePath` (as where that player played his next site himself); for a
site 1's own action that plays site 2 (Hobbiton Woods 2_116), the first player lays his own site 2 with it,
`SkipToOtherPlayersTurn()`, and the second player uses the same site 1 on his first turn (Card_02_116_Tests); the
nearest miss is a second copy played when the player's own site already lies next, which on a numbered path
plays nothing (Card_02_026_Tests, Card_05_023_Tests; whether it may replace his own site on a Shadows-block path is Q-0238).
A numbered path's site laid by a move, by `FreepsPass()` or by `SkipToSite(n)`, is the SHADOW player's copy, on
every move of either player and on the Fellowship and King paths alike (probed at the landing of dec-b5_100):
`assertSame(scn.GetShadowSite("site5"), scn.GetCurrentSite())`, never `GetFreepsSite`, which then answers with the
copy still in the Free Peoples adventure deck. So that deck holds a site of each title on the path, and a card that
reads it against the path (Destroyed Homestead 15_76) finds a match there unless it is narrowed (`SetFreepsAdventureDeck`).
A site put in the CARD map (`put("trollshaw", "1_334")`, not the site map) goes into BOTH adventure decks as well,
so each player holds two sites of that number: a plain `FreepsPass()` then stops at the Shadow player's "Choose
site to play" and not at his Shadow phase, `SkipToSite(n)` halts on that choice ("halted in phase FELLOWSHIP"),
and "your site of the same number" can pick up the extra copy, so an ability read as offered may be offered on it
(dec-b2_55). `GetFreepsSite(int)` and `GetShadowSite(int)` then answer with the laid site of that number if it
is the player's, else with the FIRST copy of that number in his adventure deck, whichever that is: fetch such a
site by its key (`GetFreepsSite("site2")`; dec-b7_232). Set both decks before any move that lays a site. Give each
player nine sites, one of each number (`SetFreepsAdventureDeck`, `SetShadowAdventureDeck`), then lay the card-map
site with `MoveCardToAdventurePath`, which takes it even after the reset has taken it out (Card_01_222_Tests).
The site map is the constructor's SECOND argument, a HashMap of its own after the card map: `new HashMap<>() {{
putAll(VirtualTableScenario.FellowshipSites); put("site2", "4_330"); remove("site9"); put("bree", "18_140"); }}`, then
the Ring-bearer and the Ring; `putAll` of the sites into the card map does not compile (Card_11_161_Tests, Card_13_185_Tests).

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
otherwise legal validates with no errors at all: `assertEquals(List.of(), dvs.validate(deck))`. That is so only
in a format that takes the rig's sites: `new DeckValidationScenario()` is
Multipath, where every `buildDeck` deck draws "Card is X-listed" for Emyn Muil and Ettenmoors among its sites;
`fotr_block` is clean. The filler itself draws no objection at any size up to 100 a side (engine pass 24,
`virtualtable/DeckFillerTests`): it passes over a card whose title an earlier filler card has (Boromir 1_97
after Boromir 1_96, which made five of one title at the 37th card) and a card the format X-lists or allows fewer
than four of (Forces of Mordor 1_248 in Multipath and Expanded). 94_22's deck-size tests of 99 and 100 cards were
written before that and read `hasMinimumDeckSizeError` alone. The filler knows nothing of the cards a test
names: name a card of its ranges (1_88 to 1_119, 1_239 to 1_283), or one of the same TITLE as a card there
(Aragorn, King in Exile 1_365 beside the filler's 1_89), and its copies are added to. To read the copy objection
alone, give both filler counts 0: `buildDeck(cards, null, 0, 0)` is the named cards and nothing more, and reads
`hasMaxSameNameError` only (Card_01_365_Tests). The Ring-bearer,
Ring, Map and sites are the ones the test names, as named: the rig's `FellowshipSites` have Ettenmoors 1_331 as
first printed, which the PC formats refuse for its errata. `hasMapError(errors)` says whether a Map objected;
`getMapErrorSites(errors)` returns the names of the sites its objection lists, for reading that a stray site is
named and the valid ones are not.
`getErrorLines(errors)` returns every line of every objection, one to an entry and in alphabetical order (the
sites' objections come as one entry of several lines, in whatever order the deck holds its sites): set it beside
`List.of(...)` of the objections expected (`mechanics/sites/SitesFromAnyBlockTests`).
A Race to Mount Doom modifier of deck building is tied to validation by its own overrides:
`dvs.validate(deck, VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides())`, which is what
`LeagueService.buildDeckValidationContext` merges for the player who has it. A context built by hand with the
number from the text (`new DeckValidationContext().setMaximumSameNameOverride(6)`) tests the format and not the
card: a wrong number in the definition passes it (the old tests of 92_1, 92_19 and 94_22, all since rewritten).
`getDeckBuildingOverrides()` hands back the blueprint's own object, the same one to every test of the run: a test
of the league's merge calls `merge` on a `new DeckValidationContext()` with the card's overrides as the argument,
never on the card's overrides (`mechanics/game/DeckBuildingOverridesMergeTests`, 94_22's two merge tests until engine
pass 24). Whose deck the overrides bind ("Your draw deck ...") is the league's choice
of player and nothing in the definition: no card test can show it, the `-` for the other player is the same deck
validated with no context, and the ledger's Notes say so.
`hasSiteBlockError(errors)` matches the four block objections and nothing else ("same block", "not allowed in
Multipath", "does not use supported block", "cannot mix Movie block and Shadows block sites"): not the Map's
(`hasMapError`: a mixed Movie deck under a Map in PC Movie draws both, so read both), not "Card not from valid
set", "Card is X-listed", "Deck combines both Shadows and Movie Block sites" or the counting rules. Nine Shadows
sites in Multipath or PC Movie read false with no override at all (refused by set and by the Map, not by block),
so an `assertFalse` of it is no proof that a deck validates: where the deck can be made wholly legal, read
`assertEquals(List.of(), ...)` with the override and `assertNotEquals` without (92_19).
The rig's `ShadowsSites` is not a legal adventure deck: six of its nine have Shadow number 0 and three of one
number is the most ("Deck contains 6 sites with twilight number of 0.", in Expanded too). 92_19 puts 11_240,
11_231 and 11_250 at sites 6 to 8. Slopes of Amon Hen 1_361, like Ettenmoors 1_331, is refused as first printed
in PC Movie (its errata has seven sites: 1_331, 1_333, 1_361, 3_120, 4_355, 7_332, 7_359). Under 92_19's override
(`skipSiteBlockValidation`) a site is let off the format's block, its sets and the Map, and is still held to the
format's X-list and errata (Q-0185: A; `mechanics/sites/SitesFromAnyBlockTests`): a mixed deck read as wholly legal
with the override wants Bree Gate 1_327 or Ettenmoors 51_331 at 2, and in Multipath no Emyn Muil 1_360 at 9.
The override opens the Fellowship, Towers, King and Shadows blocks and no other (Q-0219: B): a Hobbit site (sets 30
to 33) under it draws "Site does not use supported block: <site>" in every format that does not play the Hobbit
sites with no override (The Hobbit Draft Game, Limited - Hobbit, Anything Goes and Debug do), PC Movie and
Multipath included, where without the override the same site draws "Card not from valid set".
The format has no Ring-bearer rule of its own: it counts the Ring-bearer, the Ring and the sites with the draw
deck, by title, against the one copy limit (CR 4.2 "building your deck", the exception). Every deck the scenario
builds has Frodo 1_290 as Ring-bearer, so `buildDeckWithCopies("1_290", n, true)` puts n Frodos beside him and
the limit is reached at n = limit - 1 (3 of 4; 5 of 6 under 92_1, Q-0182 open).
What the deck builder LISTS for such a player is no card's test: the list asks the format the same question of
each site as validation does (`LotroFormat.validateSite(id, context)`), and is tested by filter string in
`game/SortAndFilterCardsTest` (`format:pc_movie cardType:SITE siteOverride:true`; F-0342).

## No test cards, and the test booster pack

- **No card is invented for a test** (Ketura, 2026-10-11; Q-0241). A card test uses printed cards. A guard for a thing no card should be able to do is laid with the ad hoc staplers on a text-less card (above, "A guard for a thing no card should be able to do is stapled on"). Nothing else is invented: a definition word no printed card uses has no test, and a word one card uses is tested as far as that card goes, in that card's own file. The table's card library is the printed cards and nothing else: the tests' own definitions (30 cards with set-0 ids from `0_8001`, in a test resource) and the tests that laid them were deleted by engine pass 27; the words left with no card and no test are listed in Q-0274.

- **A test of whether a definition is LOADED** (a word the builder must refuse) plays no game and is no mechanic test: it is plain JUnit in the server module, in `gemp-lotr-server/src/test/java/com/gempukku/lotro/definitions/` (`UnknownPhaseInADefinitionTests`, `PlacingTheEventPlayedOnADrawDeckDefinitionTests`, `PlayerIsRequirementDefinitionTests`; Q-0233: C), where its helper methods may stay. These three are kept as guards of the loader (Q-0273: keep): no printed card can show a definition refused, so each writes its definitions in the test itself, and they reach no library and no table. `cardtest.py suite` runs them with the cards, the mechanics and the rig's own tests. None is added for a shape no card has.

- **A burden that cannot be added.** No printed card stops a burden being added. A card's own test of one hinders the Ring-bearer (`HinderCard`): while he is hindered the rules put the modifier on (`HinderedRule`; test-generation, "anything before "to"").

- **Who heals.** A heal is asked about with the player who makes it: a `CantHeal` with `player` stops that player's heals alone (Race Text 92_35, the one card that names one), one without stops everybody's (Steward's Tomb 10_119; mechanics/wounds/HealBarThatNamesAPlayerTests), and a heal that is barred as an effect is still offered and heals nothing (Scaling Ladder 5_57 is used and discarded), while one barred as a cost is not offered (Hrethel 6_95). It is asked about with the card whose text heals as well: a `CantHeal` with `by` bars only those healers, and a heal made by no card is barred whatever `by` says (Spirit of Dread 16_5's `by: not(wraith)`, Q-0357; mechanics/wounds/HealBarExceptingSomeHealersTests).

- **A card that names something as it is played has stored nothing when it is cheated into play**: its filter on the stored value matches nobody, a `{stored}` in its texts reads "nothing" and `GetDisplayableInformation` returns the definition's line with `{stored}` still in it, so a test of such a card plays it (Ulaire Nertea 69_38).

- **`transferableTo(<filter>)`** asks what a `Transfer` with `checkTarget: true` would then find, and moves nothing: a possession on Warrior of Rohan 17_104 is never accepted; a bearer who has an item of its class, whom another card keeps from bearing it, or who is hindered is no place for it (Race Text 94_4's own tests, F-0324; Gamling 18_99 is the other card with the word).

- **A player's account name.** A table seats a player under a chosen name with the last two arguments of the `VirtualTableScenario` constructor that takes league placements (G-0039; virtualtable/NamedSeatTests); every other table's players are "Free Peoples Player" and "Shadow Player", who meet no card's list. A card whose definition lists real accounts (Race Text 94_5, the one card with `PlayerIs`) is not tested by those names (Q-0190: B), the name check itself is tested once, card-agnostic, in mechanics/actions/PlayerIsRequirementTests: the requirement built from definition text written in the test and asked directly at a named table, the one exception to "no definition text made up for a test" (Q-0271: C).

- **A search of another player's draw deck** writes `shuffle: false` and a `ShuffleDeck` of the deck named, as the Gronds do (18_82, 68_82; shapes, "A search of another player's draw deck"). `shuffle: true` beside a `deck:` that names the other player shuffles the deck named as well (F-0369), and no printed card writes it.

- **A test that passes alone and fails in its class (or the reverse) points at a definition that keeps state.** A card's definition is built once and shared by every game of the JVM, so a list an effect builds with the definition and then adds to as it is carried out is never emptied. One was found and mended: Into the Caves 17_100, whose `removeKeyword` with `keywordFromMemory` added each keyword named to the definition's own set, so the minion of every later use, in any test run after, lost them all (F-0487; engine pass 28 builds the keywords of each use afresh, and a search of every appender found no other write from a use to what the definition holds). Such a test is a finding against the engine, not a test to rearrange.

**The test booster pack.** The test booster pack (`product/Test-Booster.hjson`) holds eight cards: beside the six it had (among them a
Shadow response event, Enheartened Foe 1_247, which Race Text 92_29 shows and does not let be chosen), Drums in the
Deep 1_168 (a Shadow skirmish event: shown and not selectable when the pack is opened in the Shadow phase)
and Foul Things 2_59 (a Shadow-phase event costing 2: selectable, and played for nothing; it then plays a
[moria] minion from the discard pile at that minion's own cost). A card played from the pack is read with
`StartCardCapture()` / `GetLastCreatedCard()`. Opening it (92_29) is three decisions: the owner picks the product
(`ShadowChooseCardBPFromSelection("Test - Booster")`, a choice of exactly 1), the other player is shown all eight
and dismisses, and the owner's choice ("Booster pack contents", 0 to 1) is taken by blueprint id or left with
`ShadowDeclineChoosing()`, not a pass. `ShadowHasBPChoiceAvailable(id, false)` is true only for a card shown and
NOT selectable (false for a selectable one). An event played from the pack ends in its player's discard pile,
where `GetLastCreatedCard()` still finds it. The pack has no Shadow-phase event with a cost in its own text.
For a card the fixed pack does not hold, `scn.UseBoosterOf("1_197", "1_177")` in place of `UseTestBoosters()`
(G-0038): the one pack offered, still chosen as "Test - Booster", holds exactly those cards in that order
(`ShadowGetBPChoices()` reads them in order), and it fails for an id that is no card (virtualtable/NamedBoosterTests).

**An event played "ignoring all costs" pays nothing in its own text either (F-0316).** From 92_29's pack, Threat
of the Unknown 1_197 ("Exert your [moria] Orc to draw 2 cards") draws 2 with no Orc exerted and with none on the
table. A cost the text reads back is still carried out: The Eye of Sauron 2_87 exerts no Orc and still asks "how
many burdens to spot", and All Life Flees 18_132 still removes its (X) (Q-0201, open). A card the event then
plays (Foul Things, We Must Have It) is paid for as ever. mechanics/costs/IgnoringAllCostsTests.
