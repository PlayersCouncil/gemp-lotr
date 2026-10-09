# Things that catch people out

The behaviours of the rig and the engine that have surprised authors in this campaign, grouped by what they are about. The gemp-testing skill lists every entry by its first words; read the groups your cards' tests will touch before writing them, and the whole file when a test does something you cannot explain.

Each of these has been checked against the engine as it stands on this branch.

## How the engine asks

**The engine asks once.** The choices in a decision are worked out when the decision is sent. Cheating a card
onto the table while a decision is pending does not change what that decision offers. To see a card become
playable, complete the pending decision with a real action and look at the next one.

**One candidate is chosen for you; none is skipped.** When a choice has exactly as many valid candidates as it
needs, the engine makes it without asking, and a `FreepsChooseCard` written for it fails, naming the decision
the game has gone on to (G-0032). When it has none, that part of the effect is skipped. So a test that
means to inspect a choice has to supply one more valid candidate than the choice needs, and at least one invalid
one beside them. The same thing hides self-targeting: Celeborn's "exert Celeborn to heal an [elven] ally" heals
Celeborn himself when no other Elven ally is wounded. An "up to N" choice is the exception: its
minimum is 0, so it asks even with one candidate left (Song of Durin's "discard up to 2 [moria] conditions").
A choice between piles goes the same way: "from your draw deck or discard pile" is not asked when only one pile
has a candidate, and a `ShadowChoose("draw deck")` then fails inside the next decision. So does a transfer:
with one legal bearer it is made without asking.

**A board changed under a pending decision.** The game works out what a decision offers when it sends it. After a cheat that changes the board while a player is at a choice of phase actions, call `FreepsRefreshDecision()` / `ShadowRefreshDecision()` before reading what is on offer; they fail unless that player is at such a choice.

**A number that can only be one number is not asked for** (F-0147). Where an effect or a cost asks for a number and only one can be named (X from 1 to 1, "how many to spot" with none to spot, "remove X tokens" with none there), the game takes it. A test of such a board does not answer the question, and an answer written "to be safe" goes to the NEXT decision (the symptom is a wrong number later, not an error). To read the bounds of an X, arrange a board where two numbers can be named.

**A choice with nothing on any branch is not asked** (F-0116, F-0293). Where a `choice` comes up as an effect and no branch can be carried out in full, the player is asked to pick among all of them (a branch may still be done in part: Dwarrowdelf Chamber 1_344 with one companion to exert), unless every branch is a single effect that is certain to do nothing: nobody to exert, heal, wound or discard, no burden or threat to remove, no site to liberate. Then nothing is asked and nothing happens, and a test of that board answers no "Choose action to perform" (assert `assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"))`; mechanics/ChoiceWithNothingOnAnyBranchTests). The question is kept by a branch of several effects, by one that holds others (`if`, `costToEffect`, `multiple`), and by one whose effect cannot tell (a play from a pile, `chooseActiveCards`: Here Lies Balin 1_19 with no Orc in play is still asked).

**"Wound each" and "wound every" wound one card at a time.** Each of the wounds has its own "is about to"
window (`<Wound X> - Optional "is about to" responses`), so a response that picks which card it saves ("prevent
a wound to an Elf") is offered one card at a time and never a choice among the cards the effect reaches: the F
check's invalid candidate "among them" cannot be shown against such an effect, which is a line in Notes. Nobody
is asked the order: cards in play that an effect takes together are taken in the order of their card ids, the
same in every run (Q-0142, F-0268). A card gets a new id as it comes into play, so that is the
order they came into play: lay them down one at a time (`MoveMinionsToTable(a); MoveMinionsToTable(b);`) and a
test may say whose window comes first. It holds for cards a player chose together too ("wound 2 Orcs"), whichever
was named first, and one effect that takes several at once (exert every, heal, hinder, discard each) names them
in that order in the game log. `mechanics/TakenTogetherInCardIdOrderTests` shows each.

**Choosing which skirmish to resolve is always asked.** Even when only one skirmish is left, the Free Peoples
player is asked to choose it. `FreepsAssignAndResolve` answers that for the skirmish it sets up;
after other skirmishes, call `FreepsResolveSkirmish(companion)`.

**Who is offered for assignment.** `FreepsHasAssignmentTargetAvailable(card)` and its Shadow twin read the
assignment decision in front of the player. `FreepsCanAssign(card)` and `ShadowCanAssign(card)` read the game's
rule, whatever is being asked. A restriction on a pair ("may not assign a character to skirmish this minion",
`cantBeAssignedToSkirmishAgainst`) shows in neither: both cards stay listed, and the game refuses the pair only
when the assignment is answered. `FreepsCanAssignAgainst(character, minion)` and `ShadowCanAssignAgainst` read
that (G-0029); a refused assignment is never shown by the rig throwing. A restriction on one card for one player
(`CantBeAssignedToSkirmish` with `player`: Bill Ferny 2_75 and 52_75, Q-0146) shows in the first two: the card is
off that player's list, and when it is the only minion the Free Peoples player is not asked to assign at all.

**No method names the moment after the assignments.** When every minion is assigned the Free Peoples player is
asked "Choose next skirmish to resolve", and that is the bookend to assert after `FreepsAssignToMinions` or
`ShadowAssignToMinions`: `assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"))`. With a
minion left unassigned the Shadow player is asked "Assign minions" first. After an assignment ACTION in the
assignment phase the bookend is the other player's `Awaiting...AssignmentPhaseActions()` (69_38).

**One minion to a companion, and the minions left over.** The Free Peoples player may assign one minion to each
companion (more takes defender): `FreepsAssignToMinions` with two minions in one group is refused by the game
with a bare `DecisionResultInvalidException`. A minion he leaves unassigned gives the Shadow player an
assignment decision of his own, whoever else is assigned, the Ring-bearer included, and that is how a companion
comes to face two: `ShadowAssignToMinions(companion, secondMinion)` after the Free Peoples player's assignment.
`ShadowDeclineAssignments()` passes that decision; with no minion left over he is not asked.

**`IsCharAssigned` reads only the skirmishes still waiting.** The game takes an assignment off its list as that
skirmish begins, so `IsCharAssigned(card)` is false for the characters of the skirmish being fought
(`IsCharSkirmishing` is true of them), while the definition word `assignedToSkirmish` counts them until the
skirmish phase ends, which is after its win and lose triggers have been carried out (CR 4.2 "skirmish
phase(s)"). A test of "assigned to a skirmish" at a win or lose trigger rests on that rule and reads the
card's effect, not `IsCharAssigned` (Lorien Protector 63_20, with two Orkish Skirmishers 18_90).

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
shows nothing and lint does not notice: `ShadowPass()` first, then read it beside `AwaitingFreeps...PhaseActions()`
(94_13's old lock-out assertions; 94_14's old reading of the Free Peoples player in the Shadow phase was dropped).

**A number with no upper bound.** `FreepsGetChoiceMax()` reads `Integer.MAX_VALUE` where a player is choosing a
number and the game sets no maximum (a `chooseANumber` with no `to`: Gathering Wind 10_16, Beneath the Mountains
52_1). That is the reading for "no cap" (G-0027).

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

**`FreepsChoose(text)` takes a phase action when no multiple choice is pending.** `FreepsChoose(text)` and
`ShadowChoose(text)` answer a multiple choice if one is in front of the player. If none is and he has actions on
offer, they go on to `FreepsChooseAction(text)` and take whichever action has the words in its wording, in
silence. So an answer written for a choice the game never asks (an "X or Y" with nothing on either branch,
F-0116) does something else: 92_35's old test answered `FreepsChoose("heal")` after an Athelas with nobody left
to heal, and discarded a card from hand to heal with instead; it failed only once the hand held other cards.
Read that the choice is pending before answering it by text (`FreepsDecisionAvailable("Choose action to
perform")`).

## What is offered, and what an offer does

**Costs gate an action; effects are only attempted.** An action is offered when its requirements are met and its
costs can be paid, whether or not its effect can do anything: Sam's "exert Sam to remove a burden" is offered with
no burdens to remove. To test that an effect finds nothing to do, use the action, check the cost was paid, and
check the game is back at the phase's actions. An event is the same: with nothing for its effect to act on it
is still offered, and playing it pays for it and discards it. An ability at its limit goes by the kind of
limit (Q-0093). A limit on uses ("limit once per turn") withholds it, cost or no cost. A limit on what its effect
gives ("(limit +3)", "add a threat (limit 1)") leaves an ability with a cost on offer (Q-0041): the cost is paid
and nothing else happens; with no cost it is not offered at that limit either.

**A "wound X twice" branch of a choice is not offered to a character with 1 vitality left.** In "add 2 burdens or
wound him twice" (Boromir 59_31) only the first of the two wounds could be placed, so the game carries out the
other branch and asks nothing (CR 4.2 "effect": a choice must be one the player is fully capable of performing;
the glossary read together, not a ruling of Ketura's). With 2 vitality left the branch is offered and kills him,
which is also what tells a wound from an exertion (F-0148).

**A play inside an `If` does not gate its action.** The engine refuses an action it can see will play nothing
only when the play is the action's top-level effect. Wrapped in an `If`, the action is always offered and does
nothing when used (F-0036, Out of Dark Waters). A definition written that way is a finding, not a quirk to test
round.

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
and the action does nothing else; a cap on anything else reads `"false"` (Sting 1_313, 51_313). One with a limit on uses is not offered at its limit, so there is no flag to read. The flag is worked out when the decision is sent, so after cheating the board get a new decision
before reading it. At site 2 of `FellowshipSites` (Ettenmoors) each player
is also offered the site's own skirmish action in every skirmish. Which cards assert the flag: test-generation,
"Rulings, interactions and formats".

**An action's own exertions count against what it then needs.** "Exert X twice to play ... from your discard
pile" is not offered when X's exertions would leave the only candidate unplayable, and "exert A and B" is not
offered when one character with one exertion left is the only A and the only B
(`mechanics/CostsAreJudgedInTheOrderPrintedTests`). That is so where the cost names its card or only one
character can pay; where the player chooses among several, the action is offered on the board as it stands.

**A prevention is asked only when its effect will happen** (Q-0114;
`mechanics/PreventionIsAskedOnlyWhenItsEffectWillHappenTests`). "X may pay to prevent this" is not put to X
when the effect cannot happen: a draw from an empty deck, the fifth draw of a fellowship phase, nobody chosen
for the effect or the one chosen out of reach (a companion who cannot exert), threats at their limit, no site
to take, a skirmish that will not be cancelled. An answer written for the question on such a board
(`ShadowChooseNo()`) fails or goes to the next decision. Read it as
`assertFalse(scn.ShadowDecisionAvailable("prevent"))` with the cost unpaid: in the phases both players act in,
`ShadowAnyDecisionsAvailable()` is true anyway, because the Shadow player is being asked for his own action.
`ShadowDecisionAvailable(text)` reads the decision's own wording: where a prevention is put as a choice of
options the word may be in an option only, and the reading is `ShadowChoiceAvailable("<the option's words>")`,
or the assertion holds whatever the game does.
An effect that will happen in part (draw 3 with one card left) is still asked. For a rider "X may Y to
prevent this", the board on which the effect cannot happen is a `-` worth one test where the card can reach it.

**A response event is not played by another card's effect.** "Play a(n) ... event from your discard pile /
stacked here / from hand" neither offers a response event nor counts one towards being usable; an event that
is a response and a phase event (Eye of Barad-dur 5_96) is offered in its phase only
(`mechanics/ResponseEventsAreNotPlayedByEffectsTests`). For "a [culture] event" a response event of that
culture is the invalid candidate beside the phase events, and a phase word in the definition's filter beside
it cannot be told from its absence: say so in Notes.
Nor is an event of another phase played that way (CR 4.2 "event"): "play a [shire] tale" from hand in the
regroup phase does not offer a Skirmish event that is a [shire] tale (Nine-fingered Frodo 10_112), and with that
alone in hand the ability is not offered (Bilbo 59_49).

**A response is offered once per use.** After a response cancels an ability or an event, no response to it is
offered again (Q-0043), so a pass or decline written to answer a second offer answers the next decision instead.
Optional triggers are still offered. A Response special ability with a cost is the other way about: once used,
it is offered again for the same play for as long as its cost can still be paid (CR 4.2 "response": "You may
respond more than once to the same situation"; Q-0034), though a second use gains nothing. That second "Optional
responses" decision stands in front of whatever comes next: decline it, or the played event stays in the void
and a reading written for after the play reads VOID (Shrouded Elf 63_23).

**Two at once, one use.** A required trigger with a limit on uses fires once when one event raises it twice (two companions exerting together): no ordering question is asked for the second.

**A Response ability is a special ability used.** A card's `type: Response` ability is the same kind of action
as a phase ability and reports itself as used once its costs are paid; its word is Response, whatever phase it
is used in. So a card that answers "a special ability is used" with no phase named (Storied Homestead 13_76,
Terrible as the Dawn 53_103) is offered against one, and a card that names a phase's abilities ("a skirmish
special ability": 5_59, 5_64, and 51_98 by Q-0119; "a regroup special ability": 5_114) is not, though the
Response was used in that phase (`mechanics/ResponseAbilityIsASpecialAbilityTests`). F-0201 said no card could
answer one; that was a misreading of the source and no fault. A cancelled "is about to" Response leaves the
situation standing, and its owner is offered it again for as long as he can pay (CR 4.2 "response"): leave him
nothing to pay with, or decline for him, before reading that the wound landed.

**A discount with a limit is spent when the card is played.** "Its twilight cost is -1 for each ... (limit -N)"
on an ability is the card's for the phase (Q-0134): asking what is on offer spends none of it, each play takes
what is left, and at the limit the ability stays on offer and plays at full cost, withheld only while the pool
cannot pay for anything it could play (`mechanics/LimitOnADiscountTests`; Uruk Regular 54_192).

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

## Reading and answering an offer

**"Is an optional trigger pending" is a broad question.** `FreepsHasOptionalTriggerAvailable()` is true for any
optional decision, the Ruling Ring's included. To assert that a particular card's trigger is on offer, pass the
card: `FreepsHasOptionalTriggerAvailable(card)` goes by the card each offered action belongs to, whatever its
prompt says. Where one card offers two optional actions in the same decision (Ethereal Banner 103_21's trigger
and its response, on an overwhelm), add words from the action's description:
`FreepsHasOptionalTriggerAvailable(card, text)` and `FreepsChooseOptionalTrigger(card, text)`, as
`FreepsUseCardAction(card, text)` does for phase actions.

**A yes/no in the middle of a trigger is not an optional trigger.** A definition's `Optional` effect for a named
player ("the first Shadow player may discard 2 cards from hand to add a burden" inside a site's required trigger,
Courtyard Parapet 63_188) asks that player a yes/no in the definition's own `text`. `ShadowHasOptionalTriggerAvailable()`
is false there: read it with `ShadowDecisionAvailable("<words of the text>")` and answer it with `ShadowChooseYes()` /
`ShadowChooseNo()` (and the Free Peoples twins). It is not asked when its cost cannot be paid.

**An ability with wording of its own, and a card with several.** `FreepsActionAvailable(card)` sees an ability
whose definition gives it its own `text:` (Library of Rivendell's "Stack your tale or artifact from play") as
well as one the game words "Use <card name>". Where a card offers several actions, name the one meant:
`FreepsActionAvailable(card, "from hand")` and `FreepsUseCardAction(card, "from hand")`. Both read a use: a
card in hand that can be played, an event included, is read with `FreepsPlayAvailable(card)`.

**Using a card's action.** `FreepsUseCardAction(card)` and `ShadowUseCardAction(card)` take the action `FreepsActionAvailable(card)` sees, even where the game lists another of the card's actions first (a possession that another companion could bear lists "Transfer" before "Use"). A card that offers no such action has the first action listed for it taken, as before: an optional trigger or a response is accepted this way. `FreepsUseCardAction(card, "Use")` is no longer needed to get past a transfer.

**Two copies of one card.** `FreepsActionAvailable(card)`, `FreepsPlayAvailable(card)`,
`FreepsTransferAvailable(card)` and their Shadow twins answer for the physical card they are given: with two
copies in play, each is asked about separately, and a per-copy limit is tested by asking about each copy. The
overloads that take text (`FreepsActionAvailable("Use Orc Taskmaster")`) still match any action so described.
The card-choice checks are different where the cards are in a pile or are only shown: see the next entry.

**A choice from a pile, and a showing, are read by blueprint.** Where a decision lists cards that are not in
play (a search of a deck, a play from a discard pile, a reveal), `FreepsHasCardChoiceAvailable(card)`,
`FreepsHasCardChoiceNotAvailable(card)` and the `(card, false)` reading of a showing match by blueprint: any
copy answers for any other, the other player's copy in his own pile or hand included. An invalid candidate
there, and a card left out of a reveal, needs a blueprint no valid or shown card has (another title, or another
version of the title); a test that reads where the chosen card went keeps one copy in the pile. The ANSWER need
not go by blueprint: `ShadowChooseCards(card)` takes that very card out of a discard pile (Mountain-troll
65_112's tests), and lint's `same-blueprint` warning is only for the `...BPFromSelection` calls. It says nothing
where the twin is never named in the test (it lies in the draw deck), the chosen card was cheated out of the draw
deck, and the card's text takes nothing from a draw deck.
It cuts both ways: a card read as NOT offered (one still in the hand, say) fails that reading as soon as a valid
candidate of its blueprint is put in the pile, so a candidate added later takes a blueprint of its own (68_82).
The other player's pile can be told apart all the same: the invalid candidate for "your discard pile" is a card
in HIS pile of a title that is in none of the chooser's piles (91_11: his Orc Bowmen, the chooser's left in the deck).

**Two offers of one response.** When the same card offers the same trigger or response twice in one decision (two Nazgul winning one skirmish), each offer's text ends with " (for <the cards its trigger memorized>)". A single offer reads as before. Choose between them by that text.

**When a wound kills and something answers the same wound.** The Free Peoples player, whoever owns the trigger,
gets a "Required responses" decision listing the rule and the trigger (a Shadow card's "wins a skirmish" trigger
beside "Resolve skirmish damage" is the same decision, and so is a Free Peoples companion's own required "each
time X wins a skirmish", Boromir 59_31: left unanswered, the only sign is that the beaten minion is still in play
after `PassSkirmishActions()`; a companion's own required "each time X loses a skirmish" raises it too, Isildur
18_54: until `FreepsResolveRuleFirst()` his wound for losing reads 0 after `PassCurrentPhaseActions()`; it is not
only for deaths). The rule's entry may have no action
text at all, and it is not always listed first (King of the Dead 103_27 killed: `["Required trigger from King of
the Dead…", null]`; Ferocious Haradrim 11_80 answering a death: `[null, "Required trigger from Ferocious
Haradrim"]`). `FreepsResolveRuleFirst()` resolves the rule, the entry that belongs to no card, wherever it is
listed; `FreepsResolveActionOrder(text)` resolves the entry whose wording contains the text and passes over an
entry with no wording.
A trigger added for the skirmish ("if that minion is overwhelmed, ...") raises the decision even when its effect
will come to nothing (Cirdan 60_8 with one [elven] event: half, rounded down, is none): `FreepsResolveRuleFirst()`
is still needed, and nothing is asked after it. So does a card's standing required "each time X wins a
skirmish" with nothing to act on (Focus 65_15 with nobody to heal, before F-0289 made it optional).
There is no Shadow twin of either and there will not be: by rule the Free Peoples player is the one who orders
simultaneous actions, whoever's they are.
A companion whose own text answers his leaving play (Radagast 59_26) raises this decision whenever he is killed,
by a wound or by a card that places him in the dead pile (not when he is discarded or returned to hand), and his
text is carried out only once it is answered: as a helper to be killed in another card's test he leaves it pending.

**A response window can come before the target.** Where the Free Peoples player holds a response event that
can answer an event being played, the game may put an "Optional responses" decision to him before the played
event's target is chosen, and an answer written for the target choice then answers the response window, with no
error; the test fails some lines later. Read what is pending before answering (seen on 51_115). It is the order
for every response to a play, not only a response event in hand: a Response ability on a card in play (Shrouded
Elf 63_23) is asked before the played event's effect chooses anything (CR 4.2 "playing a card", steps 5 and 6).

## Getting to the moment: set-up, the skippers and turns

**Set the twilight pool before the Shadow phase starts.** Because the engine asks once, a pool set while the
Shadow player's decision is already pending does not make a minion playable. Set it during the fellowship phase,
then pass. The move then adds one for each companion plus the new site's shadow number, so a test that needs an
exact pool adds those in: `// 10 set + 1 companion + 2 for the site`. A pool that is too small shows only as the rig's
`<card> is not offering Shadow Player a play.`: count the roaming penalty as well as the cost.

**The engine sends an end of turn once before turn 1 (F-0327, open).** After the mulligans `MulliganProcess` hands on
to `EndOfTurnGameProcess`, and at that moment the player who will go second counts as the Free Peoples player. A
modifier in player 2's seat with an `EndOfTurn` trigger under `OwnerIsFreeps` goes off there: `StartGame()` returns
with player 1 holding its question in BETWEEN_TURNS, and the next skipper fails on it. Read that nothing is
asked straight after `StartGame()`; never answer it with an `if`, which is what hid the fault in the old test.
Race Text 94_17, where it was found, no longer shows it: its trigger now also requires `SiteAvailableToControl`
(Q-0195), and no site can be controlled before the first turn. The engine still sends that end of turn.

**Both players act in most phases.** Fellowship is the Free Peoples player's alone and Shadow the Shadow
player's. In maneuver, archery, assignment, skirmish and regroup the Free Peoples player is asked first, then the
Shadow player, until both pass in a row. A test of a Shadow action in those phases passes for the Free Peoples
player first. `SkipToPhase` leaves the Free Peoples player with the decision. After a player's action the other
is asked next: following a Free Peoples skirmish action `FreepsPass()` does nothing (he is not being asked), and
it is `ShadowPass()` and then `FreepsPass()` that end the skirmish, so the last reading inside a skirmish is
taken before that pair. `PassSkirmishActions()` and its kin pass for whoever holds the decision first. The same
holds in the other shared phases: a test that ends on a Free Peoples maneuver ability rests on
`AwaitingShadowManeuverPhaseActions()`, and a second use by the Free Peoples player needs `ShadowPass()` first
(Elven Guardian 67_7's tests).
`PassSkirmishActions()` is the wrong call once the Free Peoples player has already passed in that skirmish (a
`FreepsPass()` made only to read what the Shadow player is offered): it passes for the Shadow player, the
skirmish resolves, and its second pass goes to whatever the Free Peoples player is asked next, the choice of
the next skirmish (DecisionResultInvalidException from a CardsSelectionDecision) or his regroup decision (the
test then rests on the Shadow player's). There `ShadowPass()` alone ends the skirmish.

**No minions, no combat.** With no minion in play when the Shadow phase ends, the game goes straight to regroup.
A test that needs a maneuver phase needs a minion on the table.
The maneuver, archery and assignment phases are each passed over when no active minion is in play
(`ManeuverGameProcess`, `ArcheryGameProcess`, `AssignmentGameProcess`), so a trigger at the start of one of them
that does something to "a minion" always has one: its `requires: CanSpot minion` cannot differ in play, and no
`-` on a board with nothing to act on (Q-0034) is owed for it (Race Text 94_11). That covers the minion only:
where the trigger needs a second card as well ("assign a minion to skirmish the Ring-bearer", Race Text 94_26), a
board on which it does nothing still arises (the Ring-bearer hindered) and the `-` is owed (F-0337).

**Minions do not survive a turn.** `SkipToSite(n)` plays whole turns for both players and ends in player 1's
fellowship phase at site n. Minions and anything attached to them are discarded on the way, so put Shadow cards
down after `SkipToSite`, not before.

**Site 9 ends the game.** A fellowship that survives the skirmishes at site 9 has won, so `SkipToSite(9)` returns
with the game over. To test something at site 9, `SkipToSite(8)` and then pass the fellowship phase: the
fellowship moves, and the test is in the Shadow phase at site 9.

**The game ends when it ends.** When the Ring-bearer dies or is corrupted the game stops at once and no later
phase arrives. Make the assertions that matter before the event that ends the game.

**A starting fellowship.** `StartGame()` skips it. To test "(except in your starting fellowship)": the scenario
constructor returns with player 1's "Starting fellowship" choice pending; `FreepsChooseCard(card)` plays a
companion into it and player 2's choice is then pending. End the test there: going on deals opening hands.
To play on instead, answer player 2's choice too (`ShadowDeclineChoosing()` takes nobody; `FreepsDeclineChoosing()`
ends player 1's early, never `FreepsChoose("")`) and then call
`StartGame(true, false)`, which leaves the starting fellowships alone; called while either choice is still
pending it answers nothing and the game has not begun (Card_91_001_Tests). While the choice is pending the game
keeps the running total of the costs played in the twilight pool: `GetTwilight()` reads it (2 after a cost-2
companion, a card offered only while the total with its cost is 4 or less), and the pool is emptied as turn 1
begins (Card_94_003_Tests).

**Archers get in the way.** `SkipToPhase` passes for both players and takes the first choice on required
triggers, but it does not assign archery wounds. An archer on the table stops it, or kills the minion the test
was about. Leave archers out of a scenario unless they are the point, or put them down after skipping. A
possession that makes its bearer an archer (a bow) is the same: `AttachCardsTo` it after `SkipToAssignments()`
(Long Battle Bow 11_89 in Little Golden Flower 65_148's tests).

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
and `FreepsAssignToMinions` then fails, saying that the Free Peoples player is not being asked anything and
what the Shadow player is being asked. A trigger at the start of the target phase itself is left pending, as a
test of it needs.
The same pass is spent with no trigger at all where the test's own `FreepsPass()` inside a skirmish left the
Shadow player holding the decision: `SkipToPhase(Phase.REGROUP)` then arrives with the Free Peoples player's
first regroup decision already passed and the Shadow player asked "Play Regroup action or Pass", so the bookend
is `AwaitingShadowRegroupPhaseActions()` (Race Text 94_11).
`SkipToOtherPlayersTurn()` does not decline what it ends on: an OPTIONAL start-of-turn trigger of the incoming
player is left pending when it returns (the phase reads BETWEEN_TURNS), for the test to take or decline
(Prized Lagan 68_110, the R check of "at the start of each turn, you may").

**Two required start-of-turn triggers stop `StartGame()` short.** With two cards cheated in before `StartGame()`
that each have a required trigger at the start of the turn (Elrond 1_40 and Galadriel 1_45), the game is left
asking the Free Peoples player which goes first, and the next action fails ("<card> is not offering Free
Peoples Player a play.  Free Peoples Player is being asked "Required responses" ..."). Put the second such card
down after `StartGame()`, or answer the ordering with `FreepsResolveActionOrder(text)`.

**An optional trigger at the start of the turn is the first thing asked.** A card with an optional
start-of-turn or start-of-fellowship trigger, put into play before `StartGame()`, leaves its offer pending when
`StartGame()` returns. Answer it, or put the card down afterwards. A card with no trigger at the start of the turn,
a minion included, may go down before `StartGame()` and is still on the table in the turn's later phases
(Card_91_013_Tests; probed): "minions go down after StartGame" is the habit of new tests, and an old file's order
is left as it is.

**`SkipToSite` plays whole turns, reconciling included.** A small draw deck is drawn into the players' hands on
the way. `SkipToMovementDecision` and `SkipToOtherPlayersTurn` reconcile too: the Shadow player draws up to 8
before the second move is asked about, so a test that turns on his hand at the second move empties his draw deck
first. A test that needs cards in the draw deck afterwards puts them back with `MoveCardsToTopOfDeck`. A test
that needs a hand of a known size afterwards sets it: `SetFreepsHand(cards...)` / `SetShadowHand(cards...)` make
that player's hand exactly those cards, in that order, and put the rest of it on top of his draw deck (G-0035;
`Card_10_012_ErrataTests`); never move the scenario's aliases away one by one. And as
the fellowship decision is already pending when it returns, a card made playable by cheating is not offered
until some legal action has been taken. Player 2's own fellowship-phase play is offered at once where his
companions, support area and hand were laid BEFORE `StartGame()`: after `SkipToOtherPlayersTurn()` read
`ShadowPlayAvailable(card)`, play with `ShadowPlayCard(card)`, and end on
`ShadowDecisionAvailable("Play Fellowship action or Pass")` (Card_94_020_Tests, Card_94_021_Tests).
The Free Peoples player reconciles at the end of his turn as well: after
`SkipToOtherPlayersTurn()` a deck of eight or fewer is all in player 1's hand (8 in hand, 0 in the deck), so a
draw of his on player 2's turn has cards put back on his deck first (Card_91_005_Tests).
`SkipToSite(n)` rests in the fellowship phase at site n, and `SkipToMovementDecision()` after it has made that
turn's move: the fellowship is at n + 1, and the regroup move then leaves site n + 1 and not n. Read
`GetCurrentSiteNumber()` on each side of the move (two old 94_25 tests named the site one too low in their
comments, and one of them was the move from site 6 into region 3 without saying so).

**A reconcile on the way discards from the front of the hand.** A player over the hand size at a reconcile on the way discards down to it from the front of his hand, however many cards that is (`PassReconcile(player)`, which every turn-skipper uses), so a test may put any number of cards in a hand before skipping. A test that reaches "Choose cards to discard down to 8" by its own passes answers it in one go: `scn.PassReconcile(scn.P1())`, or `FreepsChooseCards(...)` with exactly `FreepsGetChoiceMin()` cards.

**A reconcile restores first.** By the time a reconcile's discard decision is pending, the reconciling player's
hindered cards have been restored (CR 5.0 "reconcile"; Q-0059). That is so for both players' regroup reconciles
and for "reconcile your hand" effects.

**A card's "reconcile your hand" asks what the regroup reconcile asks, in the middle of the phase.** With fewer
than 8 cards in hand it is one card choice of 0 to 1 (`ShadowChooseCard(card)` or `ShadowDeclineReconciliation()`)
and then the draw up to 8; with an empty hand nothing is asked; with more than 8 it is one choice of exactly
hand minus 8 cards, minimum and maximum equal, with no optional discard first and no declining (`ShadowChooseCards`).
Under Foot 60_52's tests read all three.

**A deck's order is set after `StartGame()`.** The game shuffles each deck to deal the opening hands, so a card
moved to the top or bottom of a draw deck before `StartGame()` does not stay there. `MoveCardsToTopOfDeck(a, b,
c)` puts each on top in turn, so the last one named is the top card.

**The Ruling Ring interrupts skirmishes.** With the default Ring, a Ring-bearer about to take a wound in a
skirmish gets an optional response. `FreepsDeclineOptionalTrigger()` declines it. Keep the Ring-bearer out of
skirmishes that are not about him. Where a card's own text wounds the Ring-bearer in a skirmish, give the table
The Great Ring 19_1 (`VirtualTableScenario.GreatRing`): it adds no strength and, while it is not worn, answers no
wound; its maneuver and skirmish abilities are used only on purpose (Card_09_031_ErrataTests). Its erratum 69_1
has no constant: give the table `"69_1"` as the ring id. The maneuver ability of either is offered only while
the Ring is not worn (F-0303).

**A companion beaten by double his strength is killed, not wounded.** A helper minion chosen only to win
overwhelms: Goblin Patrol Troop (13) kills any companion of strength 6 or less, and a minion of 8 kills Dwarf
Guard (4). The companion is then in the dead pile and `GetWoundsOn` reads 0, which looks like a wound that never
came. For a loss the loser survives keep the winner under double: Dunlending Brigand 4_10 (8) against a
companion of 5 to 7, Goblin Runner 1_178 (5) against one of 3 or 4, the Troop against Aragorn 1_89 (8).
The same the other way: a companion of twice a minion's strength kills it whatever its vitality.

**A second assignment or skirmish phase in one turn takes a second move.** To show something renew or end at the
next phase of the same name: after the skirmishes, `SkipToMovementDecision()`, `FreepsChooseToMove()`, put a
minion down (`MoveMinionsToTable`), `ShadowRefreshDecision()`, then `SkipToPhase(...)` as usual
(Card_07_080_ErrataTests). The fellowship is one site further on. The Shadow player has reconciled inside
`SkipToMovementDecision()`: with cards in his draw deck his hand is larger at the second phase than it was at the
first, so a reading of his hand on each side of the move allows for the draw (Card_91_016_Tests reads 1, then 2).

**Who places archery wounds.** The fellowship's archery wounds are placed by the Shadow player on his minions
(`ShadowChooseCard` after `SkipToArcheryWounds()`), the minions' by the Free Peoples player on companions.

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
set-up, does it by legal play (`Card_11_232_ErrataTests`; the helper table has its row).
An ally a card says may not participate (a [rohan] ally at Streets of Edoras 4_340) takes no part whatever
other card has him participate or assigns a minion to him: he adds nothing to the archery total, is offered no
archery wound, is on neither player's assignment list, and an effect that would assign a minion to him assigns
nobody. The rest of that other card (a strength bonus) still reaches him (Q-0150, F-0281).

**One blueprint under two site keys gets one alias.** The rig names each site of an adventure deck by the FIRST
key of the site map that has its blueprint, and does not pass over a key already given (as it does for the
card map): `put("site1", "18_138"); put("ruins", "18_138")` leaves one of the two aliases unset, and
`GetFreepsSite("site1")` throws NoSuchElementException. Give every site a test names a blueprint of its own;
fillers that are never looked up may share one (`Card_18_134_ErrataTests`). `GetFreepsSite(String)` and
`GetShadowSite(String)` look for the alias and then for a title in the ADVENTURE DECK only: a site already laid
is not found by its title (NoSuchElementException, no message). Fetch a site before it is laid.

**`FreepsDeclineSanctuaryHealing()` sends five empty answers, whatever is being asked.** Once one sanctuary heal
has been taken only four are left to decline, and the fifth answer passes the fellowship phase: the fellowship
moves. To take some heals and decline the rest, choose each heal and then `FreepsDeclineChoosing()` once for each
that remains, which fails loudly if it is called once too often (`Card_18_139_ErrataTests`). At Rath Dínen
100_1, which asks three heals, the untouched call already overshoots: the fourth answer passes the fellowship
phase and, on a Shadows-block path, the fifth fails ("is not being asked anything", the Shadow player being asked
"Choose site to play"). Decline there with `FreepsDeclineChoosing()` three times (`Card_V0_001_ErrataTests`).

**Sanctuary healing is asked at any site laid as site 3 or 6, and only when a companion is wounded.** The rule
makes a sanctuary of every site numbered 3 or 6 whatever its block, a Shadows-block site the Shadow player laid
there included. A turn that begins there asks one 0-to-1 choice for each heal ("Sanctuary healing - Choose
companion to heal - remaining heals: N"); with no companion wounded nothing is asked and the turn opens on
fellowship actions. Put the wounds on in the turn before (`Card_V0_001_ErrataTests`).
Under Race Text 94_7 the modifier's owner is asked with nobody wounded as well, when a burden is on: his
Ring-bearer may be chosen for the burden alone. For player 2's sanctuary, `AddBurdens` adds to the player whose
turn it is: put them on in his own turn before, and `SkipToOtherPlayersTurn()` stops on his healing decision
(`ShadowDecisionAvailable("Sanctuary healing")`, then `ShadowChooseCard`; Card_94_007_Tests).

**Whose site.** `GetCurrentSiteNumber()` answers for the player whose turn it is: on the other player's turn it
is his site, not the one player 1's fellowship reached, and there is no reader for one player's position. Read
player 1's site before his turn ends. The card on the path is the copy of whoever laid it: from site 2 on, on
player 1's first turn, the Shadow player's. `GetCurrentSite()` and `GetSite(n)` return that card and
`GetFreepsSite(n)` a different one, so compare with `GetSite(n)` or by blueprint.

**At the move limit, the regroup phase ends the turn.** When the fellowship has moved as often as the move limit
allows, both players passing in the regroup phase ends the turn at once and the other player's "Play Fellowship
action or Pass" decision is pending; no "another move" question is asked. Below the limit the Free Peoples
player is asked "Do you want to make another move?". Either comes only after the Shadow player's reconciliation:
with cards in his hand he is first asked "Reconcile - choose card to discard or press DONE"
(`ShadowDeclineReconciliation()`); with an empty hand nothing is asked (Radagast 59_26's tests). An empty hand
does not stay empty over a second move: where his draw deck holds cards the Shadow player draws at the reconcile
inside `SkipToMovementDecision()`, so the last regroup phase asks him to reconcile again before the turn ends
and the minions are discarded (Mountain-troll 65_112's tests). The old Race to Mount Doom double-move tests reach
the second move by `SkipToPhase(Phase.REGROUP); PassCurrentPhaseActions(); FreepsChooseToMove()` over empty hands
(a one-card deck, its other copy put in the discard pile): one more card in the deck and the last call fails on
the Shadow player's reconcile. Write `SkipToMovementDecision()` for the first two (92_11, 92_14).
A mover who is barred from choosing to move (Race Text 94_5, `CantMove`) is not asked either, and
`SkipToMovementDecision()` does not stop where its name says: with a hand he is left at his own "Reconcile"
decision, still in the regroup phase with `GetMoveCount()` 1 of `GetMoveLimit()` 2; with an empty hand the
skipper runs on into the other player's fellowship phase, where an `assertFalse` of "another move" shows
nothing. Keep an undrawn card in the mover's deck and read the count, the limit and the reconcile
(Card_94_005_Tests).

**The move limit never reads below 1.** The engine floors it (`ModifiersLogic.getMoveLimit`), so from the base
of 2 a penalty of 1 and a penalty of 2 both read 1, and a "move limit is -1" card whose every board starts from 2
passes with its amount one off. Raise the limit first by legal play and read the sum: Durin's Secret 1_71 (a
Fellowship event: spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1), then 2 + 1 - 1 reads 2
and a penalty of 2 would read 1 (Card_01_260_ErrataTests, Card_91_008_Tests).

**`until: end(regroup)` ends as a regroup move is finished, not with the turn.** When the fellowship moves again
the engine runs the move and THEN ends the regroup phase (`FellowshipPlayerChoosesToMoveOrStayGameProcess`), so
an effect added earlier in the turn lapses just after the second move and one added on that move lapses at once.
A change that is to last the turn writes `until: endOfTurn`; read it in the Shadow phase after a second move
(F-0317, 93_3: the limit read 3 up to the question of another move and 2 after the move).

**`SkipToOtherPlayersTurn()` straight after `SkipToMovementDecision()` throws.** It calls
`SkipToMovementDecision()` again and passes into the "another move" question: a bare
`DecisionResultInvalidException`. To read the board at the move decision and then end the turn:
`SkipToMovementDecision()`, the assertions, `FreepsChooseToStay()`, `PassReconcile(P1())`; the other player's
"Play Fellowship action or Pass" is then pending (Card_19_024_ErrataTests).

**A card discarded for where the fellowship is goes when the move is finished.** "Discard ... when at an
underground site", "if underground", "if not at a marsh" (a `ConstantlyCheck` with a `Location` requirement) is
not looked at between the moving of the marker and the adding of the move's twilight. At a decision raised by a
"when the fellowship moves to" text the card is still in play; its text counts in the twilight the move adds
(Bill the Pony's -1 at the underground site that discards him); and it is in the discard pile before anyone is
asked anything further. Outside a move the check acts at once (`mechanics/AtASiteWhenTheMoveIsFinishedTests`,
F-0233).

**Inside an "each time the fellowship moves" trigger the marker is already on the new site.** The engine raises
"moved from", moves the marker, and only then raises "fellowship moves" and "moved to" (`MovementGameProcess`),
where CR 4.2 "moving your fellowship" lists "When the fellowship moves..." actions before the marker is moved.
At a decision raised by a `Moves` trigger `GetCurrentSiteNumber()` is the site arrived at, and a `Location`
requirement in that trigger's block reads it too; `MovesFrom` is the trigger that still reads the site left.
Lay a site threshold on such a text from this order, not from the CR's list (Race Text 92_18; shapes.md, Q-0184).

**A draw deck runs short in a long file.** Every Free Peoples card a scenario names and a test does not place
sits in the Free Peoples draw deck. A "discard the top N cards" with few such cards can find fewer than N by a
second use, and a choice that cannot be carried out in full then disappears (CR 4.2 "fully capable"). Read the
deck's size where a discard or a draw must be possible. Both players' decks hold every alias, so an alias added
to a shared `Get...Scenario()` moves every hand and deck count the file's other tests read by one (60_52: 15 and
7 became 16 and 8); where the file reads such counts, give a filler alias the new card's id instead.

**A random or whole-hand effect takes whatever is there.** Where a test sets off anything that acts on a hand or
a deck by chance or as a whole (a random look, reveal or discard from a hand; a discard "for each X revealed"; a
shuffle; the reconciles of a turn-skipper drawing from a small deck), the card under test, and every card a later
count depends on, is kept out of that hand or deck: put it somewhere fixed at the start of the scenario, the
discard pile say (`MoveCardsToDiscard(card)`), and bring it in from there when it is wanted (Ketura on F-0264:
"then it is guaranteed to not interfere with the rest of it"). A test that passes seven runs in eight is this:
the card under test in the hand The Mirror of Galadriel discarded from (F-0264, 52_44); a fifth card of the
culture left in a deck the reconciles drew from (F-0265, 58_20). Run a test that has such an effect twenty times
before it is called finished.

**The fierce round reads as the first does.** After a skirmish that leaves a fierce minion in play,
`GetCurrentPhase()` is `Phase.ASSIGNMENT` again and `AwaitingFreepsAssignmentPhaseActions()` is true, exactly as
the first time. `InFierceRound()` tells them apart (G-0031): true from the game's "Fierce skirmishes" until the
last fierce skirmish is over. A "this phase" effect from the first assignment phase has ended by then.
The assignment actions are pending again too, and nothing has passed them as `SkipToAssignments()` did the first
time: call `PassFierceAssignmentActions()` before `FreepsAssignToMinions`, or the assignment is refused with a
bare `DecisionResultInvalidException` (Card_09_031_ErrataTests).
The round comes about with nobody assigned at all: where a fierce minion is in play and both players decline to
assign, the fierce assignment phase follows, which is the cheapest way to stand just before the regroup phase
(a D check on "until the regroup phase", Prized Lagan 68_110).

**Before `StartGame()` the burdens read 1.** The constructor has bid 1 burden for player 1 and placed the
Ring-bearers; `StartGame()` then takes 1 burden off (`RemoveBurdens(1)`). A burden a card added at set-up would
show as 2 before it and 1 after (Sméagol 59_30, who adds none when he starts as the Ring-bearer, reads 1 and 0).
That holds while player 1's starting fellowship is pending, which takes a companion in the decks. `GetBurdens()`
reads the player set-up has reached: with no companion in the decks the game has gone on to the mulligan question
and it reads PLAYER 2's bid (0 on a default table; with a bid handler of 7 and 2 it reads 2, and 7 where a
companion is in the decks). After `StartGame()` it is player 1's bid less 1 on every table (7 reads 6; 0 reads
0): read a bid there (Race Text 91_18), and player 2's on his own turn, where nothing has been taken off.

**A skirmish on player 2's turn.** The drive that works: `SkipToOtherPlayersTurn(); ShadowPass();` (player 2's
fellowship moves) `MoveMinionsToTable(<player 1's minion>); SkipToPhaseInverted(Phase.ASSIGNMENT);
PassCurrentPhaseActions(); ShadowAssignToMinions(companion, minion); ShadowResolveSkirmish(companion);
ShadowPass();` and player 1, the Shadow player of that turn, then acts with the `Freeps...` methods
(Card_93_001_Tests). The skirmish decision reads "Choose action to play or Pass" for both seats on every turn,
so the bookend there is the seat's own `AwaitingFreepsSkirmishPhaseActions()` or
`AwaitingShadowSkirmishPhaseActions()`: the "Play <phase> action or Pass" reading that the fellowship and
Shadow phases need on player 2's turn matches nothing in a skirmish. Any later phase of player 2's turn is reached
the same way, `SkipToPhaseInverted(Phase.REGROUP)` and the like (`SkipToPhase` is for player 1's turns); the
bookend in the regroup phase there is `AwaitingShadowRegroupPhaseActions()`, player 2 being asked first
(Card_93_010_Tests, Card_92_012_Tests).

## Cards, owners and zones

**Exerting is not wounding.** A character with one vitality left cannot exert and so cannot pay an exertion cost,
but can still be wounded. Nor is he offered to an "exert a ..." choice, whatever its filter: an invalid candidate
there has 2 vitality left or more, or he shows nothing (beside a Goblin Runner, vitality 1, "exert a companion"
built as `choose(character,not(ally))` passed; beside Goblin Patrol Troop 1_177, vitality 3, it fails: 92_13).

**Allies live in the support area.** `MoveAlliesToTable` puts allies there, where an ally played legally goes.
`MoveCompanionsToTable` takes companions only and fails the test if handed anything else, as `MoveAlliesToTable`
does if handed anything but an ally.

**A play read as not offered for a card that is not in hand shows nothing.** `GetFreepsCard` and `GetShadowCard`
leave the card in its owner's draw deck, and there `FreepsPlayAvailable(card)` answers false whatever the card
under test does (probed: Barliman Butterbur 1_70 false from the deck and true from the hand, with nothing on the
board against him). Move the card to hand before its refusal is read, and read the same card as offered on the
control board. Race Text 91_15's old test read Barliman so; in an old file, look for the `MoveCardsToHand` that
goes with each `assertFalse(...PlayAvailable(x))`.

**`AddBurdens`, `RemoveBurdens` and `GetBurdens` are the current player's.** They reach the Ring-bearer of the
player whose turn it is (`gameState().addBurdens`), so the other player's burdens cannot be laid or read beside
them: "the other player's burdens are not counted" has no board (92_25), and no check is owed for it.

**The other player's Free Peoples cards are not active on your turn.** A copy of a possession taken from the
Shadow player's deck (`GetShadowCard`) and cheated onto your companion is inactive (CR 4.2 "active"), so no
filter sees it. To put a card the opponent owns on a companion, use a Shadow card that is borne by companions:
Spider Poison (8_28) is one, and does nothing unless its bearer is assigned to a skirmish. A second copy of a
Free Peoples card is a second name for it in the scenario (`put("mail2", "1_101")`), never the Shadow player's
copy: a count of what a companion bears passes that one over. The other half is as true: your own Shadow cards
are not active on your turn, so a Shadow condition you own is no candidate for your Free Peoples ability.
Culture tokens go with the card: a token on his card is not spotted, reinforced or removed by your cards on your
turn, and "remove an [elven] token to ..." is not offered when his is the only one (68_9, 68_11, 68_12). It is
an invalid candidate by rule, not by the card's filter. It cuts the other way after `SkipToOtherPlayersTurn()`:
your own Free Peoples cards are then the inactive ones, and a bonus from your condition reads as the bearer's
printed number whatever the card does (Brought Down from Inside 69_24): read it on its owner's turn.

**An event is in the void while it is being played.** From its play until its last decision is answered an
event, a response event included, is in `Zone.VOID`; only then is it in the discard pile. `assertInDiscard(event)`
while a choice or a prevention of its effect is still pending fails.

**An event printed with several phases is an event of the phase it is played in (F-0292).** While it is being
played, and when its cost to play is read (`GetTwilightCost` of the card in hand, the play itself), a "Skirmish
or Regroup" event is an event of the current phase alone: `timeword(skirmish)` in a `played` trigger, a response
or a `modifyCost` is false of Sword-wall 4_84 played in the regroup phase. In a hand or a pile it is what is
printed on it ("take a skirmish event into hand" finds it in any phase), and an event printed with one phase,
with or without "Response" (Eye of Barad-dûr 5_96), is never narrowed
(`mechanics/EventPrintedWithSeveralPhasesTests`).

**`GetVitality` is the vitality left.** Printed vitality and what cards add, less wounds: a character of
vitality 4 with 3 wounds reads 1. The printed number is the blueprint's, as the stats test reads it.

**Culture tokens are of the card's own culture.** `AddTokensToCard`, `RemoveTokensFromCard` and
`GetCultureTokensOn` deal only in the token of the culture of the card they are given, so the invalid candidate
for "an [elven] token" is a card of another culture carrying its own tokens.

**The Ring is attached to the Ring-bearer.** `GetAttachedCards(ringBearer)` includes The One Ring, so "nothing
else attached" is a size of 1. `assertNotAttachedTo` is the safer reading.

**A card that passes through a zone.** Where an effect takes a card out of a place it has only just arrived in
(a tentacle killed by a wound and then stacked from the discard pile), read it there while the decision is
pending. A count that is 0 before and 0 after shows nothing by itself.

**What an ally bears is in the support area.** A "support card" is a card in a support area or borne by a card that is (`mechanics/SupportAreaTests`): a Shadow condition on an ally (Weary 4_212 plays on a companion or ally) or on a site the Shadow player controls (Pillage of Rohan 4_167, Wolves of Isengard 5_69, No Retreat 4_30; `ShadowTakeControlOfSite(scn.GetSite(n))` after `SkipToSite(n + 1)`, then `AttachCardsTo(site, card)`) is a valid candidate for "a Shadow support card", and the same card on a companion is the invalid one beside it.

**A follower's Aid is an optional trigger at the start of the maneuver phase.** A follower lies in the support
area until its Aid is paid, and the offer is made for one a companion already bears as well (cheated on with
`AttachCardsTo`): a test that stops in the maneuver phase with any follower in play answers it
first (`FreepsDeclineOptionalTrigger()`; a bare `FreepsPass()` only answers the offer, and the Shadow player is
not yet asked; Frenzy of Arrows 68_79's tests). `SkipToPhase(Phase.MANEUVER)` leaves the offer pending:
`FreepsHasOptionalTriggerAvailable(follower)` reads it, and `FreepsAcceptOptionalTrigger()` pays the Aid cost and
then asks for the companion (`FreepsChooseCard`). Every Free Peoples Aid transfers "to a companion", so no ally
bears a follower by legal play. `AttachCardsTo(companion, follower)` cheats one on. Most followers give the
bearer a printed strength or resistance; see the helper table for two that leave a reading alone (Little Golden
Flower 65_148's tests).

**Whose support area.** A card borne by a card in a support area is in THAT support area, whoever owns it
(`mechanics/WhoseSupportAreaTests`): the Shadow player's Weary on your ally is in your support area ("a
condition from the Free Peoples support area" reaches it; "a Shadow condition from an opponent's support
area", used by you, does not). For "your / an opponent's support area" the valid candidates are a card lying
there and one borne by an ally or a controlled site there; the invalid ones are the same card on a companion
and a card of the right owner borne by a card in the other player's area.

**The twilight cost of a card depends on where it is.** `GetTwilightCost(card)` answers for where the card is:
for a card in play, the number printed on it (F-0109, Q-0073); for a card in a hand or pile, what it would cost
to play now, roaming apart; for a site, its Shadow number as it stands. To read a cost modifier, read a card in
hand, or play the card and read the pool. A transfer costs the printed number and nothing else (Q-0053, Q-0074).

**A twilight cost never reads below 0, so a discount as large as the card's cost hides a larger one.** Madril
65_64 costs 2 and is "twilight cost -2": with `amount: -3` he reads 0 and is played for 0 all the same, and no
test on a bare board notices. To show the size of a discount on a cheap card, raise the cost first: an ad hoc
`TwilightCostModifier(null, CardType.COMPANION, null, new ConstantEvaluator(2))` does what East Road 11_236
does ("Each companion is twilight cost +2"), and the card reads 4 without its discount and 2 with it
(`Card_15_064_ErrataTests`).

**Whether a minion is roaming is read as a keyword.** `scn.HasKeyword(minion, Keyword.ROAMING)` is true while
the minion's site number is above the current site's, and it follows a site-number modifier: Úlairë Nelya 2_84
(site number 2) reads false at site 2, and true once a card has raised her site number
(`Card_15_062_ErrataTests`, `Card_15_064_ErrataTests`).

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

**An ad hoc effect is run in place of a player's answer.** `FreepsExecuteAdHocEffect(effect)` and its Shadow
twin run the effect as that player's answer to a choice of actions, whatever else the choice offers, and fail
the test if the effect was not carried out. Where being stopped is what the test is about (a wound aimed at a
character who is to be unable to take wounds), `FreepsExecuteAdHocEffectUnchecked(effect)` does not assert, and
the test reads the board (G-0028; Long I Fell 54_97). A `WoundCharactersEffect` needs a source card, and either
call needs its reason on the line (`// lint-ok: ...`).

**A value modified below zero counts as zero** (CR 4.2 "modifier"): a site's Shadow number lowered past 0 reads
0 (Rivendell Valley stays 0 under No Stranger to the Shadows 51_108). `rules.py term` has no entry for "Shadow
number"; the rule is under "modifier".

## A card that leaves play as it is played

**A card that leaves play as it comes into play was not played.** A minion played beside Watcher in the Water, a tentacle played away from a marsh, a possession or weapon played on a bearer of Shadowfax, Greatest of the Mearas (17_24 and its erratum 67_24, a standing discard by LR 1; no other Shadowfax): the card goes to the discard pile, what it cost stays paid, and nothing that answers a play happens or is offered: not its own "when you play", not another card's "each time you play", not a response (CR 4.2 "discard"; `mechanics/DiscardedOnEntryTests`). The standing discard is done before the play is announced, so no "Required responses" question is asked between it and a play's own triggers, for a card that stays either. The game log has a line "<card> left play as it came into play".

**A play as a cost can come to nothing.** An ability whose cost is to play a card is offered, takes its other costs and the card's twilight, and does nothing if the card has left play by the time its play is complete, whatever removed it. Read the costs as paid, the effect as not done, and the phase's decision.

**The loss of initiative comes with the play.** When a Free Peoples card played from hand takes the hand below four, `FreepsHasInitiative()` is still true while the card's entering step is being answered and false once its play is announced; a required "lose initiative" trigger and a required "each time you play" trigger for that play come up in one "Required responses" decision. Initiative is read again each time the game goes round its loop: cards cheated into a hand while a decision is pending give it back at the next answer (`FreepsHasInitiative()` is true after the other player's pass), which sets up a second loss in one turn for the `R` of "each time you lose initiative" (Memories of Darkness 60_2). Assert `FreepsHasInitiative()` true between the cheat and the second loss. In a skirmish, where the only other answers are passes that end it, Sméagol 6_45 ("Skirmish: Discard a card from hand to make Sméagol strength +1"; cheat him in, his play adds a burden) discarding from five cards to four is an answer that keeps initiative, and his discard from four to three, or Ranger of Osgiliath 7_116's "Regroup: Discard 2 cards from hand", is a loss of initiative that is no play (Glimpse of Fate 60_12).

**A card that discards itself as it is played was not played either.** A card discarded by its own "when you play this ..., or discard it" (the three Dunlendings of set 5, Stampeding Chief) is the same: the Free Peoples player's or the Shadow player's choice in that text is the first thing asked, before any trigger or response to the play, and if the card goes nothing answers its play. Beside the Watcher such a card is discarded first and the choice is not asked at all.

## Hidden zones, reveals, shuffles and the game log

**Searching a deck shows the deck first.** An effect that looks through a hidden zone shows the cards before it
asks for a choice. `FreepsDismissRevealedCards()` answers the showing; then choose with
`FreepsChooseCardBPFromSelection(card)`, which picks by blueprint because cards in hidden zones are not identified
to the client. To take nothing from a choice whose minimum is 0, `FreepsDeclineChoosing()`: a
`FreepsChooseCardBPFromSelection()` with no card does not compile (two overloads fit).
Where nothing in the zone can be chosen, the showing is the only decision ("... while retrieving cards", nothing
selectable; Saved From the Fire 8_20 with no card of the culture in the deck): answer it once. `FreepsDeclineChoosing()`
answers a showing as readily as a choice, so one too few leaves "Choose cards from deck" pending. One too many
fails the test, saying what is being asked instead (G-0034; it used to pass the phase in silence): delete the line.
The showing lists every card of the zone and none of them can be chosen; the choice that follows lists only the
cards that may be taken. So "shown, and cannot be chosen" is read at the showing
(`ShadowHasCardChoiceAvailable(card, false)`, `ShadowGetTotalRevealedCardCount()`), and at the choice an invalid
card is simply absent (`ShadowHasCardChoiceNotAvailable(card)`; `(card, false)` fails there). A search of the
OTHER player's deck (Grond 68_82) is shown to the searching player alone: the deck's owner has no decision.
A take written with `showAll: false` (Race Text 91_2) has no showing to dismiss: the choice comes at once.
`FreepsChooseCard(card)` answers a choice from a deck as well, and by blueprint all the same: give the card to be
taken a blueprint no other card of that deck has (fillers of another kind), or another copy goes to hand and
`assertInZone(Zone.HAND, card)` fails.

**Whether a deck was shuffled.** `GetFreepsDeckShuffleCount()` and `GetShadowDeckShuffleCount()`, read before and
after. A draw deck that has been looked through is reshuffled whether or not the card says so (CR 4.2 "draw
deck"; Ketura's ruling on Q-0013), so a card that searches a deck has this to show. A card played out of a deck
that was looked through (`showAll: true`) shuffles it as the card leaves, before it is paid for and before its
own "when you play" text is asked (Old Toby 1_305: the count is already one more at his offer; F-0313, engine
pass 20), and the definition's `shuffle: true` shuffles again after the play: two more, whatever kind of card.
A look that plays nothing reads one, the definition's own, and is the test that notices a missing `shuffle:
true`. A card played onto a bearer and an event shuffle as they leave even where nobody looked (`showAll: false`);
a card played to the table or the support area does not (a minion revealed by From Deep in Shadow 12_38, a
starting fellowship). Read the count as more than before unless the number is the point
(mechanics/DrawDeckShuffledWhenTheLookIsOverTests).

**A "play ... from your draw deck" action with no cost is offered whatever the deck holds, and a look that plays
nothing is a use.** `PlayCardFromDrawDeck` without `mustExist` (Race Text 92_28, "Fellowship or Shadow: Play an
artifact from your draw deck (limit once per turn)") is offered with no artifact in the deck and with the deck
empty (Q-0019: no penalty for failing to play from the draw deck; Q-0104). Used with nothing to play, the deck
is shown and shuffled and the turn's `limitPerTurn` is spent. The eight `mustExist: true` cards of Q-0019 are
the exception, not this.

**The Rule of 4.** No more than 4 cards are drawn or taken into hand in a player's fellowship phase. A test of a
fellowship-phase draw that repeats has to stay within four, or its fifth reading is the rule and not the card.

**A card being shown is listed as not selectable.** A real reveal from a deck or from play gives every player,
the owner included, a "Revealed card(s)" decision to dismiss. A reveal from a HAND (the whole hand, or cards at
random) gives the pop-up to the other players only: the hand's owner is shown nothing and has nothing to dismiss
(Ketura on Q-0024 and Q-0036; the game log names the cards). A reveal from a hand that has no card to show (an
empty hand, or no card of the kind asked for) gives nobody a pop-up: there is nothing to dismiss, and the game log
says "There were no cards in <player>'s hand for <card> to reveal", or "<card> revealed no cards from <player>'s
hand" when the hand holds cards (Q-0144). In the decision
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
of the event being played is among the cards taken, set the mark after the play. For something the log says in
words, with no card to name (a reveal from a hand that had nothing to show), `GameLogSinceMarkSays("no cards in")`
is true when some one message since the mark contains the words, whatever their capitals: give the fewest words
that tell the line from every other. All three readers fail the test if no mark was set.

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
none (Q-0102, Q-0112).

## Hinder, The One Ring and Maps

**The Ring-bearer is the rules' to protect, hindered or not.** A discard or a return to hand that reaches hindered cards (`override: hindered`: "a card", "a Free Peoples card") neither offers nor takes a hindered Ring-bearer (CR 4.2 "Ring-bearer"; F-0174; `mechanics/HinderedRingBearerTests`), so a definition needs no `not(ring bearer)` there, and with him the only candidate the effect does nothing. What another CARD forbids about cards gives a hindered card nothing: beside Uruk Follower 4_186 a hindered unbound Hobbit is offered and discarded.

**A hindered card that is shuffled into a draw deck is hindered no longer.** `IsHindered(card)` is false once the card is in the deck, as it is for a card discarded or returned to hand (CR 5.0 "hinder"; `mechanics/HinderedCardLeavingPlayTests`).

**A hindered card is still borne.** "Bearing 3 or more cards" and "for each card he or she bears" count a
hindered card; "for each possession" does not. Hinder one of the counted cards with `HinderCard` and read
the count unchanged.

**The One Ring is never discarded.** A discard that could name it (Doom Causeway 103_127, "each time a card is
hindered, discard it") neither offers nor takes it, hindered or not; hindered there, it stays on its bearer,
hindered (`mechanics/TheOneRingIsNeverDiscardedTests`).

**Maps and meta-sites cannot be hindered,** as sites cannot: a hinder of "a card" on a PC-Movie or Race to
Mount Doom table does not offer them (`mechanics/MapsAndMetaSitesCannotBeHinderedTests`). The cheat
`HinderCard` hinders whatever it is given and asks no rule.

**A fellowship with no unhindered companion is not asked to move again (F-0315).** CR 5.0 "hinder": "During
the Regroup phase, if the fellowship has no unhindered companions, the Free Peoples player cannot choose to
move." With every companion of the player whose turn it is hindered, "Do you want to make another move?" is not
asked: the fellowship stays, the game log says "<player> has no unhindered companion and cannot choose to move
again.", and its player reconciles (which restores them). A card that makes the Free Peoples player move again
(`HasToMoveIfAble`: No Retreat 4_30) gives way too. So `FreepsChooseToMove()` after hindering the Ring-bearer
alone fails; for a second move with a hindered Ring-bearer, cheat in a plain companion who stays unhindered
(Dwarf Guard 1_7) and count 1 for him in the move's twilight and nothing for the hindered one (92_25). The move
of the fellowship phase is no choice and is made whoever is hindered
(`mechanics/NoUnhinderedCompanionNoRegroupMoveTests`).

## Tables and formats

**A Shadows-block site path.** Pass `VirtualTableScenario.Shadows` as the format and a site map of your own;
`StartGame(scn.GetFreepsSite("site1"))`; after the fellowship phase is passed the Shadow player lays the next
site with `ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"))`. `Card_13_164_Tests` is a worked example.
The turn-skippers cannot walk such a path, but it can be walked by hand to any site: each move is `FreepsPass()`
(a second move in the turn: `SkipToMovementDecision()`, `FreepsChooseToMove()`) and then
`ShadowChooseCardBPFromSelection(...)`, with `SkipToOtherPlayersTurn()` twice between the first player's turns;
site 6 takes three of his turns (`Card_V0_001_ErrataTests`). `MoveCardToAdventurePath` works on this path too: the
card becomes the site ahead, the next move goes to it and the Shadow player is not asked to lay one (the same
file puts the first player's own copy of a site at site 6 so). For a high site number or a later region (sites 4-6 are region 2,
7-9 region 3), where the card's text names no block's sites, it is shorter to put the Shadows-block site on a Fellowship-block
path instead: a site map of `FellowshipSites` with the card in place of site n, `SkipToSite(n - 1)`,
`MoveCardToAdventurePath(scn.GetShadowSite("siten"))`, `FreepsPass()`, and the fellowship moves to it
(`Card_11_227_ErrataTests`, site 7; `Card_04_355_ErrataTests`). `MoveCardToAdventurePath` makes the card the
site after the current one whatever key the map gave it, so one scenario with the card in place of a site no
test reaches (`putAll(FellowshipSites); remove("site9"); put("parapet", "63_188")`) serves every site number
(`Card_13_188_ErrataTests`: sites 4 to 7).
The key matters, though: the rig builds the adventure deck in the site map's HashMap order, and the path is
ordered only while the FIRST card of that deck has a site number (`GameState.usesUnorderedSites`). A key that
comes out ahead of every `siteN` (`homestead`, `redhornSite`, `redhornPass`, `anduin`, `path`; with `site9` taken
out also `buckland`, `redhorn`, `banks`, `mithlond`) makes the whole path unordered: `StartGame()` is left resting on "Choose
site to play", and the next `FreepsPass()` throws DecisionResultInvalidException from an
ArbitraryCardsSelectionDecision. If that is where a new scenario stops, rename the key (`parapet`,
`bucklandSite` and `shadowsSite` are safe either way), and never put the card under the key `site9` itself, which
comes out first (`Card_18_112_ErrataTests`: `bucklandSite` and `redhorn` in place of sites 7 and 8, `site9` kept).
A capital letter is enough to move a key (`Mithlond` is safe with `site9` taken out, `Card_18_136_ErrataTests`).
`StartGame(site)` with a site named gets past the first question, but the path is still unordered: every move
asks the Shadow player to lay a site, and `SkipToSite` halts in the fellowship phase.
A Shadows-block site cannot stand as `site1` of such a path: the game lays site 1 by its printed number and
would lay nothing, and the constructor refuses the site map with an IllegalArgumentException that says so
("These sites cannot be set up: the game would lay no site 1 ..."; virtualtable/FirstSiteTests). Where its text
is needed in the first fellowship phase, make the whole path unordered: `putAll(ShadowsSites); put("site1", id)`
(the default format will do) and `StartGame(scn.GetFreepsSite("site1"))` start at that site with its text in
force; or an ad hoc modifier that does what the site does stands in.

**A battleground or a forest site needs no cheat.** The Towers path has both at site 2: Uruk Camp 4_335
(Battleground, Plains; each Uruk-hai played must exert) and Derndingle 4_330 (Forest; move limit +1 while an
unbound Hobbit is there). `TowersSites` has neither word at site 1 (4_325) or site 3 (4_337, a sanctuary), so
`putAll(TowersSites); put("site2", "4_335")` and the fellowship's own two moves (`FreepsPass()`, then
`SkipToMovementDecision()` and `FreepsChooseToMove()`) show "while at a battleground site" off, on and off again
(`Card_15_017_ErrataTests`). The Fellowship path has forests and no battleground.

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
in a format that takes the rig's sites, and below 37 filler cards a side: `new DeckValidationScenario()` is
Multipath, where every `buildDeck` deck draws "Card is X-listed" for Emyn Muil and Ettenmoors among its sites
(and for Forces of Mordor 1_248 from 37 Shadow filler cards up), and in every format the Free Peoples filler
reaches Boromir 1_97 beside Boromir 1_96 at its 37th card ("more of the same card than allowed - Boromir (5>4)";
8>4 at 50). `fotr_block` at 30 and 30 is clean. A deck-size test of 99 or 100 cards therefore reads
`hasMinimumDeckSizeError` and not the whole list (94_22; probed at landing, rtmd-b94_22). The Ring-bearer,
Ring, Map and sites are the ones the test names, as named: the rig's `FellowshipSites` have Ettenmoors 1_331 as
first printed, which the PC formats refuse for its errata. `hasMapError(errors)` says whether a Map objected;
`getMapErrorSites(errors)` returns the names of the sites its objection lists, for reading that a stray site is
named and the valid ones are not.
A Race to Mount Doom modifier of deck building is tied to validation by its own overrides:
`dvs.validate(deck, VirtualTableScenario.FindCard("92_1").getDeckBuildingOverrides())`, which is what
`LeagueService.buildDeckValidationContext` merges for the player who has it. A context built by hand with the
number from the text (`new DeckValidationContext().setMaximumSameNameOverride(6)`) tests the format and not the
card: a wrong number in the definition passes it (the old tests of 92_1, 92_19 and 94_22, all since rewritten).
`getDeckBuildingOverrides()` hands back the blueprint's own object, the same one to every test of the run: a test
of the league's merge calls `merge` on a `new DeckValidationContext()` with the card's overrides as the argument,
never on the card's overrides (94_22). Whose deck the overrides bind ("Your deck ...") is the league's choice of
player and nothing in the definition: no card test can show it, the `-` for the other player is the same deck
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
in PC Movie; under 92_19's override no site is held to the X-list or the errata at all (Q-0185, open).
The format has no Ring-bearer rule of its own: it counts the Ring-bearer, the Ring and the sites with the draw
deck, by title, against the one copy limit (CR 4.2 "building your deck", the exception). Every deck the scenario
builds has Frodo 1_290 as Ring-bearer, so `buildDeckWithCopies("1_290", n, true)` puts n Frodos beside him and
the limit is reached at n = limit - 1 (3 of 4; 5 of 6 under 92_1, Q-0182 open).

## Test cards and the test booster pack

- **Definitions of the tests' own.** `gemp-lotr-server/src/test/resources/testCards.hjson` holds card definitions known to every table by set-0 ids (`0_8001` on), read by `framework/TestCardLibrary`. They are for mechanic tests that need a shape no real card has. A card's own tests use real cards. The file is a test resource: after editing it, `target/test-classes` needs it (a `mvn test-compile`, or copy it there).

- **Test cards.** `0_8002` Test War-horn copies every keyword of an exerted minion to your other minions until the regroup phase, as a snapshot (mechanics/KeywordSnapshotTests); `0_8003` Test Waystone is the per-site limit's test card (see the comment above it in testCards.hjson).

- `0_8004` Test Seal: "Burdens cannot be added.", the test card for the definition word `cantAddBurdens` (mechanics/CantAddBurdensTests). A card's own test of a burden that cannot be added hinders the Ring-bearer instead (`HinderCard`): while he is hindered the rules put the same modifier on (`HinderedRule`; test-generation, "anything before "to"").

- `0_8005` Test Mirror gives one minion's game text and keywords to another until the assignment phase, as a snapshot (mechanics/GameTextSnapshotTests): the test card for `DuplicateGameText`'s `snapshot: true`.

- `0_8006` Test Undertow ("Maneuver: Remove (1) to discard a Free Peoples card.") and `0_8007` Test Ebb-tide ("Maneuver: Remove (1) to return a Free Peoples card to its owner's hand."): Shadow conditions whose effects carry `override: hindered`, for reading what a discard or a return to hand by side alone may take (mechanics/HinderedRingBearerTests).

- `0_8008` Test Riptide ("Maneuver: Remove (1) to discard a card.", reaching hindered cards) and `0_8009` Test Maelstrom ("Maneuver: Remove (1) to discard The One Ring."): mechanics/TheOneRingIsNeverDiscardedTests.

- `0_8010` Test Fog ("Maneuver: Remove (1) to hinder a card."): mechanics/MapsAndMetaSitesCannotBeHinderedTests.

- `0_8011` Test Archive ("Shadow or Skirmish: Play an event from your discard pile.") and `0_8012` Test Lectern ("Shadow or Skirmish: Play an event stacked here."): Shadow conditions for mechanics/ResponseEventsAreNotPlayedByEffectsTests. The test booster pack (`product/Test-Booster.hjson`) holds a Shadow response event, Enheartened Foe 1_247, which Race Text 92_29 shows and does not let be chosen. A choice from a stack is answered with `ShadowChooseCard`, not `ShadowChooseCardBPFromSelection`.

- `0_8013` Test Whetstone ("Skirmish: Exert a Dwarf to play a [dwarven] event from your discard pile."): mechanics/CostsAreJudgedInTheOrderPrintedTests.

- `0_8014` Test Muster ("Shadow or Maneuver: Play a minion; its twilight cost is -1 for each minion you can spot (limit -2).") and `0_8015` Test Barrow (the same in the Shadow phase, from the discard pile): Shadow conditions with no cost, for mechanics/LimitOnADiscountTests.

- `0_8016` Test Shelf ("Response: If you play an event, add (1) to place that event on top of your draw deck.") and `0_8017` Test Cellar (the same, beneath): Free Peoples conditions built on `filter: played`, for mechanics/PlacingTheEventPlayedOnADrawDeckTests. Each is offered again for the same play after it is used (a cost that can be paid): decline it.

- `0_8018` Test Fork ("Fellowship: Add (1) to exert a companion or heal each companion."): a Free Peoples condition whose second branch is two effects (remember the companions, then heal those remembered), for mechanics/ChoiceWithNothingOnAnyBranchTests: a choice with such a branch always asks.

- `0_8019` Test Tourniquet ("You may not heal minions."): a Shadow condition whose `CantHeal` names its player, for mechanics/HealBarThatNamesAPlayerTests. A heal is asked about with the player who makes it: a `CantHeal` with `player` stops that player's heals alone, one without stops everybody's (Steward's Tomb 10_119), and a heal that is barred as an effect is still offered and heals nothing (Scaling Ladder 5_57 is used and discarded), while one barred as a cost is not offered (Hrethel 6_95).

- `0_8020` Test Herald ("When you play this condition, name a race. Fellowship: You may add (1) for the named race."), `0_8021` Test Tally ("Fellowship: If a burden could be added, add (1).") and `0_8022` Test Drum ("Each time the Free Peoples player starts to assign minions, add (1)."): for mechanics/StoredValueInATextTests (`{stored}` and `storeRace`), CanAddBurdensRequirementTests and FreePeoplesPlayerStartsAssigningTriggerTests. A card that names something as it is played has stored nothing when it is cheated into play: its filter on the stored value matches nobody, a `{stored}` in its texts reads "nothing" and `GetDisplayableInformation` returns the definition's line with `{stored}` still in it, so a test of such a card plays it (Ulaire Nertea 69_38)

**The test booster pack.** The test booster pack (`product/Test-Booster.hjson`) holds eight cards: beside the six it had, Drums in the
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
plays (Foul Things, We Must Have It) is paid for as ever. mechanics/IgnoringAllCostsTests.
