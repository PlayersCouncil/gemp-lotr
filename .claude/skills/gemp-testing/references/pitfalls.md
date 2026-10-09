# Things that catch people out

The behaviours of the rig and the engine that have surprised authors in this campaign, grouped by what they are about. The gemp-testing skill lists every entry by its first words; read the groups your cards' tests will touch before writing them, and the whole file when a test does something you cannot explain.

Each of these has been checked against the engine as it stands on this branch.

## How the engine asks

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

**A board changed under a pending decision.** The game works out what a decision offers when it sends it. After a cheat that changes the board while a player is at a choice of phase actions, call `FreepsRefreshDecision()` / `ShadowRefreshDecision()` before reading what is on offer; they fail unless that player is at such a choice.

**A number that can only be one number is not asked for** (F-0147). Where an effect or a cost asks for a number and only one can be named (X from 1 to 1, "how many to spot" with none to spot, "remove X tokens" with none there), the game takes it. A test of such a board does not answer the question, and an answer written "to be safe" goes to the NEXT decision (the symptom is a wrong number later, not an error). To read the bounds of an X, arrange a board where two numbers can be named.

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
that (G-0029); a refused assignment is never shown by the rig throwing.

**One minion to a companion, and the minions left over.** The Free Peoples player may assign one minion to each
companion (more takes defender): `FreepsAssignToMinions` with two minions in one group is refused by the game
with a bare `DecisionResultInvalidException`. A minion he leaves unassigned gives the Shadow player an
assignment decision of his own, whoever else is assigned, the Ring-bearer included, and that is how a companion
comes to face two: `ShadowAssignToMinions(companion, secondMinion)` after the Free Peoples player's assignment.
`ShadowDeclineAssignments()` passes that decision; with no minion left over he is not asked.

**A check on a player who is not being asked anything answers false.** The availability and card-choice checks
return false, `FreepsGetAvailableActions()` returns an empty list, and the raw readers of a decision's
parameters return null, when that player has no pending decision. The methods that *answer* a decision still
fail, as they should, the plain pass methods apart (see "Answers that are refused" below), and since G-0030 they
say why: "Shadow Player was to answer "45 47", but is not being asked anything.  Free Peoples Player is being
asked "Play Fellowship action or Pass", offering [...]", or "<card> is not offering <player> a play" with what is
on offer. So do `FreepsGetChoiceMin()`, `FreepsGetChoiceMax()` and `FreepsGetSelectableCount()` on a decision
that has no such thing. A refused answer leaves whatever was pending still pending. Read the message before
anything else: it names the decision the test is really at.

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
included. Do not write one "to be safe". Nor is `FreepsChooseCard(card)` like this: it answers with the card's id,
and at a choice of actions the game reads a number as the place of an action in its list, so a card choice
written for a choice the game made by itself (one candidate) takes whatever action has that number, or fails
only if there is none. (The older, wordier `FreepsPassCurrentPhaseAction()` and
`ShadowPassCurrentPhaseAction()` were removed from the rig on 2026-10-05 at Ketura's word; old tests you read in
git history may still show them.)

## What is offered, and what an offer does

**Costs gate an action; effects are only attempted.** An action is offered when its requirements are met and its
costs can be paid, whether or not its effect can do anything: Sam's "exert Sam to remove a burden" is offered with
no burdens to remove. To test that an effect finds nothing to do, use the action, check the cost was paid, and
check the game is back at the phase's actions. An event is the same: with nothing for its effect to act on it
is still offered, and playing it pays for it and discards it. An ability at its limit goes by the kind of
limit (Q-0093). A limit on uses ("limit once per turn") withholds it, cost or no cost. A limit on what its effect
gives ("(limit +3)", "add a threat (limit 1)") leaves an ability with a cost on offer (Q-0041): the cost is paid
and nothing else happens; with no cost it is not offered at that limit either.

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

**A response is offered once per use.** After a response cancels an ability or an event, no response to it is
offered again (Q-0043), so a pass or decline written to answer a second offer answers the next decision instead.
Optional triggers are still offered.

**Two at once, one use.** A required trigger with a limit on uses fires once when one event raises it twice (two companions exerting together): no ordering question is asked for the second.

**A Response ability is never "used".** A Response special ability is built as an optional trigger, which
does not report itself as an ability used, so no card that answers "a special ability is used" (5_59, 5_64,
5_114, 13_76, 51_98) is offered against one (F-0201, an engine candidate). A test of such a card uses a phase
ability of the right phase.

**A discount with a limit is spent when the card is played.** "Its twilight cost is -1 for each ... (limit -N)"
on an ability is the card's for the phase (Q-0134): asking what is on offer spends none of it, each play takes
what is left, and at the limit the ability stays on offer and plays at full cost, withheld only while the pool
cannot pay for anything it could play (`mechanics/LimitOnADiscountTests`; Uruk Regular 54_192).

## Reading and answering an offer

**"Is an optional trigger pending" is a broad question.** `FreepsHasOptionalTriggerAvailable()` is true for any
optional decision, the Ruling Ring's included. To assert that a particular card's trigger is on offer, pass the
card: `FreepsHasOptionalTriggerAvailable(card)` goes by the card each offered action belongs to, whatever its
prompt says. Where one card offers two optional actions in the same decision (Ethereal Banner 103_21's trigger
and its response, on an overwhelm), add words from the action's description:
`FreepsHasOptionalTriggerAvailable(card, text)` and `FreepsChooseOptionalTrigger(card, text)`, as
`FreepsUseCardAction(card, text)` does for phase actions.

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
version of the title); a test that reads where the chosen card went keeps one copy in the pile.

**Two offers of one response.** When the same card offers the same trigger or response twice in one decision (two Nazgul winning one skirmish), each offer's text ends with " (for <the cards its trigger memorized>)". A single offer reads as before. Choose between them by that text.

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
A companion whose own text answers his leaving play (Radagast 59_26) raises this decision whenever he is killed,
by a wound or by a card that places him in the dead pile (not when he is discarded or returned to hand), and his
text is carried out only once it is answered: as a helper to be killed in another card's test he leaves it pending.

**A response window can come before the target.** Where the Free Peoples player holds a response event that
can answer an event being played, the game may put an "Optional responses" decision to him before the played
event's target is chosen, and an answer written for the target choice then answers the response window, with no
error; the test fails some lines later. Read what is pending before answering (seen on 51_115).

## Getting to the moment: set-up, the skippers and turns

**Set the twilight pool before the Shadow phase starts.** Because the engine asks once, a pool set while the
Shadow player's decision is already pending does not make a minion playable. Set it during the fellowship phase,
then pass. The move then adds one for each companion plus the new site's shadow number, so a test that needs an
exact pool adds those in: `// 10 set + 1 companion + 2 for the site`.

**Both players act in most phases.** Fellowship is the Free Peoples player's alone and Shadow the Shadow
player's. In maneuver, archery, assignment, skirmish and regroup the Free Peoples player is asked first, then the
Shadow player, until both pass in a row. A test of a Shadow action in those phases passes for the Free Peoples
player first. `SkipToPhase` leaves the Free Peoples player with the decision. After a player's action the other
is asked next: following a Free Peoples skirmish action `FreepsPass()` does nothing (he is not being asked), and
it is `ShadowPass()` and then `FreepsPass()` that end the skirmish, so the last reading inside a skirmish is
taken before that pair. `PassSkirmishActions()` and its kin pass for whoever holds the decision first.

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
and `FreepsAssignToMinions` then fails, saying that the Free Peoples player is not being asked anything and
what the Shadow player is being asked. A trigger at the start of the target phase itself is left pending, as a
test of it needs.

**Two required start-of-turn triggers stop `StartGame()` short.** With two cards cheated in before `StartGame()`
that each have a required trigger at the start of the turn (Elrond 1_40 and Galadriel 1_45), the game is left
asking the Free Peoples player which goes first, and the next action fails ("<card> is not offering Free
Peoples Player a play.  Free Peoples Player is being asked "Required responses" ..."). Put the second such card
down after `StartGame()`, or answer the ordering with `FreepsResolveActionOrder(text)`.

**An optional trigger at the start of the turn is the first thing asked.** A card with an optional
start-of-turn or start-of-fellowship trigger, put into play before `StartGame()`, leaves its offer pending when
`StartGame()` returns. Answer it, or put the card down afterwards.

**`SkipToSite` plays whole turns, reconciling included.** A small draw deck is drawn into the players' hands on
the way. `SkipToMovementDecision` and `SkipToOtherPlayersTurn` reconcile too: the Shadow player draws up to 8
before the second move is asked about, so a test that turns on his hand at the second move empties his draw deck
first. A test that needs cards in the draw deck afterwards puts them back with `MoveCardsToTopOfDeck`. And as
the fellowship decision is already pending when it returns, a card made playable by cheating is not offered
until some legal action has been taken.

**A reconcile on the way discards from the front of the hand.** A player over the hand size at a reconcile on the way discards down to it from the front of his hand, however many cards that is (`PassReconcile(player)`, which every turn-skipper uses), so a test may put any number of cards in a hand before skipping. A test that reaches "Choose cards to discard down to 8" by its own passes answers it in one go: `scn.PassReconcile(scn.P1())`, or `FreepsChooseCards(...)` with exactly `FreepsGetChoiceMin()` cards.

**A reconcile restores first.** By the time a reconcile's discard decision is pending, the reconciling player's
hindered cards have been restored (CR 5.0 "reconcile"; Q-0059). That is so for both players' regroup reconciles
and for "reconcile your hand" effects.

**A deck's order is set after `StartGame()`.** The game shuffles each deck to deal the opening hands, so a card
moved to the top or bottom of a draw deck before `StartGame()` does not stay there. `MoveCardsToTopOfDeck(a, b,
c)` puts each on top in turn, so the last one named is the top card.

**The Ruling Ring interrupts skirmishes.** With the default Ring, a Ring-bearer about to take a wound in a
skirmish gets an optional response. `FreepsDeclineOptionalTrigger()` declines it. Keep the Ring-bearer out of
skirmishes that are not about him.

**A companion beaten by double his strength is killed, not wounded.** A helper minion chosen only to win
overwhelms: Goblin Patrol Troop (13) kills any companion of strength 6 or less, and a minion of 8 kills Dwarf
Guard (4). The companion is then in the dead pile and `GetWoundsOn` reads 0, which looks like a wound that never
came. For a loss the loser survives keep the winner under double: Dunlending Brigand 4_10 (8) against a
companion of 5 to 7, Goblin Runner 1_178 (5) against one of 3 or 4, the Troop against Aragorn 1_89 (8).
The same the other way: a companion of twice a minion's strength kills it whatever its vitality.

**A second assignment or skirmish phase in one turn takes a second move.** To show something renew or end at the
next phase of the same name: after the skirmishes, `SkipToMovementDecision()`, `FreepsChooseToMove()`, put a
minion down (`MoveMinionsToTable`), `ShadowRefreshDecision()`, then `SkipToPhase(...)` as usual
(Card_07_080_ErrataTests). The fellowship is one site further on.

**Who places archery wounds.** The fellowship's archery wounds are placed by the Shadow player on his minions
(`ShadowChooseCard` after `SkipToArcheryWounds()`), the minions' by the Free Peoples player on companions.

**Allies who take archery wounds instead of companions.** While Come Down's archery ability is in force (4_146,
54_146: the `AlliesTakeArcheryFireWoundsInsteadOfCompanions` flag), each of the minions' archery wounds is offered
to allies only, at home or away, for as long as an ally can take one. A wound no ally can take (none in play, or
the last one killed) is placed as usual, on a companion or a participating ally (Q-0136, F-0243). The allies only
take the wounds: an archer ally away from home adds nothing to the fellowship archery total (Q-0137, F-0244).

**An ally in a skirmish.** An ally can be assigned at its home site (CR 4.2 "ally"). On `FellowshipSites`, Elrond
(1_40, home site 3) can skirmish once the fellowship has moved to site 3: `SkipToSite(2)` and then a move. An ally whose home is on another
block's path needs that block's sites in the scenario (Gondorian Merchant 7_97, home 3T, on the Towers sites:
`SkipToSite(2)` and then the move). `SkipToSite(n)` stops in the fellowship phase at site n, so a
`SkipToAssignments` from there moves the fellowship on to n + 1 first.

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
(`ShadowDeclineReconciliation()`); with an empty hand nothing is asked (Radagast 59_26's tests).

**A card discarded for where the fellowship is goes when the move is finished.** "Discard ... when at an
underground site", "if underground", "if not at a marsh" (a `ConstantlyCheck` with a `Location` requirement) is
not looked at between the moving of the marker and the adding of the move's twilight. At a decision raised by a
"when the fellowship moves to" text the card is still in play; its text counts in the twilight the move adds
(Bill the Pony's -1 at the underground site that discards him); and it is in the discard pile before anyone is
asked anything further. Outside a move the check acts at once (`mechanics/AtASiteWhenTheMoveIsFinishedTests`,
F-0233).

**A draw deck runs short in a long file.** Every Free Peoples card a scenario names and a test does not place
sits in the Free Peoples draw deck. A "discard the top N cards" with few such cards can find fewer than N by a
second use, and a choice that cannot be carried out in full then disappears (CR 4.2 "fully capable"). Read the
deck's size where a discard or a draw must be possible.

**The fierce round reads as the first does.** After a skirmish that leaves a fierce minion in play,
`GetCurrentPhase()` is `Phase.ASSIGNMENT` again and `AwaitingFreepsAssignmentPhaseActions()` is true, exactly as
the first time. `InFierceRound()` tells them apart (G-0031): true from the game's "Fierce skirmishes" until the
last fierce skirmish is over. A "this phase" effect from the first assignment phase has ended by then.

## Cards, owners and zones

**Exerting is not wounding.** A character with one vitality left cannot exert and so cannot pay an exertion cost,
but can still be wounded.

**Allies live in the support area.** `MoveAlliesToTable` puts allies there, where an ally played legally goes.
`MoveCompanionsToTable` takes companions only and fails the test if handed anything else, as `MoveAlliesToTable`
does if handed anything but an ally.

**The other player's Free Peoples cards are not active on your turn.** A copy of a possession taken from the
Shadow player's deck (`GetShadowCard`) and cheated onto your companion is inactive (CR 4.2 "active"), so no
filter sees it. To put a card the opponent owns on a companion, use a Shadow card that is borne by companions:
Spider Poison (8_28) is one, and does nothing unless its bearer is assigned to a skirmish. A second copy of a
Free Peoples card is a second name for it in the scenario (`put("mail2", "1_101")`), never the Shadow player's
copy: a count of what a companion bears passes that one over. The other half is as true: your own Shadow cards
are not active on your turn, so a Shadow condition you own is no candidate for your Free Peoples ability.

**An event is in the void while it is being played.** From its play until its last decision is answered an
event, a response event included, is in `Zone.VOID`; only then is it in the discard pile. `assertInDiscard(event)`
while a choice or a prevention of its effect is still pending fails.

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

**A card that leaves play as it comes into play was not played.** A minion played beside Watcher in the Water, a tentacle played away from a marsh, a possession played on a bearer of Shadowfax: the card goes to the discard pile, what it cost stays paid, and nothing that answers a play happens or is offered: not its own "when you play", not another card's "each time you play", not a response (CR 4.2 "discard"; `mechanics/DiscardedOnEntryTests`). The standing discard is done before the play is announced, so no "Required responses" question is asked between it and a play's own triggers, for a card that stays either. The game log has a line "<card> left play as it came into play".

**A play as a cost can come to nothing.** An ability whose cost is to play a card is offered, takes its other costs and the card's twilight, and does nothing if the card has left play by the time its play is complete, whatever removed it. Read the costs as paid, the effect as not done, and the phase's decision.

**The loss of initiative comes with the play.** When a Free Peoples card played from hand takes the hand below four, `FreepsHasInitiative()` is still true while the card's entering step is being answered and false once its play is announced; a required "lose initiative" trigger and a required "each time you play" trigger for that play come up in one "Required responses" decision.

**A card that discards itself as it is played was not played either.** A card discarded by its own "when you play this ..., or discard it" (the three Dunlendings of set 5, Stampeding Chief) is the same: the Free Peoples player's or the Shadow player's choice in that text is the first thing asked, before any trigger or response to the play, and if the card goes nothing answers its play. Beside the Watcher such a card is discarded first and the choice is not asked at all.

## Hidden zones, reveals, shuffles and the game log

**Searching a deck shows the deck first.** An effect that looks through a hidden zone shows the cards before it
asks for a choice. `FreepsDismissRevealedCards()` answers the showing; then choose with
`FreepsChooseCardBPFromSelection(card)`, which picks by blueprint because cards in hidden zones are not identified
to the client. To take nothing from a choice whose minimum is 0, `FreepsDeclineChoosing()`: a
`FreepsChooseCardBPFromSelection()` with no card does not compile (two overloads fit).
Where nothing in the zone can be chosen, the showing is the only decision ("... while retrieving cards", nothing
selectable; Saved From the Fire 8_20 with no card of the culture in the deck): answer it once. `FreepsDeclineChoosing()`
answers a showing as readily as a choice and never complains, so one too few leaves "Choose cards from deck" pending
and one too many answers whatever comes next: at "Play Fellowship action or Pass" it passes the phase.

**Whether a deck was shuffled.** `GetFreepsDeckShuffleCount()` and `GetShadowDeckShuffleCount()`, read before and
after. A draw deck that has been looked through is reshuffled whether or not the card says so (CR 4.2 "draw
deck"; Ketura's ruling on Q-0013), so a card that searches a deck has this to show. A card played from the
deck onto a bearer, or an event played from it, reads two more where the definition shuffles as well: the play
itself shuffles as the card leaves the deck. A card played from the deck to the table or support area reads one.

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

## Tables and formats

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

## Test cards and the test booster pack

- **Definitions of the tests' own.** `gemp-lotr-server/src/test/resources/testCards.hjson` holds card definitions known to every table by set-0 ids (`0_8001` on), read by `framework/TestCardLibrary`. They are for mechanic tests that need a shape no real card has. A card's own tests use real cards. The file is a test resource: after editing it, `target/test-classes` needs it (a `mvn test-compile`, or copy it there).

- **Test cards.** `0_8002` Test War-horn copies every keyword of an exerted minion to your other minions until the regroup phase, as a snapshot (mechanics/KeywordSnapshotTests); `0_8003` Test Waystone is the per-site limit's test card (see the comment above it in testCards.hjson).

- `0_8004` Test Seal: "Burdens cannot be added.", the test card for the definition word `cantAddBurdens` (mechanics/CantAddBurdensTests).

- `0_8005` Test Mirror gives one minion's game text and keywords to another until the assignment phase, as a snapshot (mechanics/GameTextSnapshotTests): the test card for `DuplicateGameText`'s `snapshot: true`.

- `0_8006` Test Undertow ("Maneuver: Remove (1) to discard a Free Peoples card.") and `0_8007` Test Ebb-tide ("Maneuver: Remove (1) to return a Free Peoples card to its owner's hand."): Shadow conditions whose effects carry `override: hindered`, for reading what a discard or a return to hand by side alone may take (mechanics/HinderedRingBearerTests).

- `0_8008` Test Riptide ("Maneuver: Remove (1) to discard a card.", reaching hindered cards) and `0_8009` Test Maelstrom ("Maneuver: Remove (1) to discard The One Ring."): mechanics/TheOneRingIsNeverDiscardedTests.

- `0_8010` Test Fog ("Maneuver: Remove (1) to hinder a card."): mechanics/MapsAndMetaSitesCannotBeHinderedTests.

- `0_8011` Test Archive ("Shadow or Skirmish: Play an event from your discard pile.") and `0_8012` Test Lectern ("Shadow or Skirmish: Play an event stacked here."): Shadow conditions for mechanics/ResponseEventsAreNotPlayedByEffectsTests. The test booster pack (`product/Test-Booster.hjson`) holds a Shadow response event, Enheartened Foe 1_247, which Race Text 92_29 shows and does not let be chosen. A choice from a stack is answered with `ShadowChooseCard`, not `ShadowChooseCardBPFromSelection`.

- `0_8013` Test Whetstone ("Skirmish: Exert a Dwarf to play a [dwarven] event from your discard pile."): mechanics/CostsAreJudgedInTheOrderPrintedTests.

- `0_8014` Test Muster ("Shadow or Maneuver: Play a minion; its twilight cost is -1 for each minion you can spot (limit -2).") and `0_8015` Test Barrow (the same in the Shadow phase, from the discard pile): Shadow conditions with no cost, for mechanics/LimitOnADiscountTests.

**The test booster pack.** The test booster pack (`product/Test-Booster.hjson`) holds eight cards: beside the six it had, Drums in the
Deep 1_168 (a Shadow skirmish event: shown and not selectable when the pack is opened in the Shadow phase)
and Foul Things 2_59 (a Shadow-phase event costing 2: selectable, and played for nothing; it then plays a
[moria] minion from the discard pile at that minion's own cost). A card played from the pack is read with
`StartCardCapture()` / `GetLastCreatedCard()`.
