# Rig requests

Things a test needed to do and the rig could not. Each says what was needed, the method proposed, and the
interface it belongs in. Nothing here is added to the rig until Ketura has agreed it, because the rig is shared
with the scenario system.

## Open

**Ketura, 2026-10-06:** rig features no longer need a proposal. "If you're adding a feature to the test rig ...
you can add it. The only consideration I want you to do is to only do so in a case where there is actual use;
no adding features if you don't have an existing practical use case for them. Also inform me after the fact in
rig-requests, but they no longer need to be proposals, just informationals." Entries from here on say what was
added and what uses it. Approved the same day: G-0017 ("Definitely add FreepsTakeControlOfSite ... Be sure to
fix the workarounds used while this function didn't exist"), G-0019 ("Absolutely, go ahead and add means for
parsing the message log"), G-0020, G-0022, G-0023. G-0021: "I need a more clear proposal here" (answered in the
entry). All of these are being built on 2026-10-06 (rig pass); each entry moves to Done with its commit.

**Numbering (2026-10-08).** G-0019 and G-0020 were each given out twice. The earlier pair (the game log, Maps) keep
their numbers, which forty-odd ledgers cite; the later pair are G-0027 and G-0028 (built since, under Done). No test
file cites either number. G-0029 to G-0031 were found the same day in the doc-suggestion notes of earlier batches, where
they had been left as remarks without a number.

None open: G-0027 to G-0031 were built in engine pass 16 (2026-10-08) and are the first five entries under Done.

## Considered and not filed (2026-10-08)

Asked for in earlier batches' notes, each by one author who said no test was held by it and who had another way.
Not numbered and not to be built unless a test turns out to need one. For Ketura to overrule.

- `FreepsAwaitingSkirmishChoice()`: "the Free Peoples player is being asked which skirmish to resolve next"
  (v2-b12). Asked for again by a lander, with 112 such readings in the suite: built in engine pass 24 as G-0047.
- `FreepsGetDecisionText()` / `ShadowGetDecisionText()`: the text of the pending decision (v3 batches). Wanted in
  probes only; G-0030's messages would give the same to anyone debugging.
- `FreepsGetRevealedCardsAmong(cards...)`: which of some cards are among those revealed (v3 batches). The check was
  written with the readers of G-0013.
- A cheat that lays a site on the adventure path (v1-b62). Today: Pathfinder 1_110 with a ranger, in the regroup
  phase. To look at again when site cards come under test in the Decipher sets.
- A count of copies of a card in a zone, for "one of two copies is gone" without assuming which (58_48). Today:
  the discard count and the second question still appearing.

## Done

### G-0066 A decision that asks only to be confirmed: FreepsIsAskedOnlyToConfirm() / ShadowIsAskedOnlyToConfirm() and FreepsConfirm() / ShadowConfirm() (built 2026-11-18)

From the undo work (branch undo/done-ask): Ketura decided that a starting fellowship no longer ends by itself once nothing more can go into it. After a pick, with nothing more that fits, the game asks "Starting fellowship - Nothing more can be added: press DONE", an ARBITRARY_CARDS decision with no cards, min 0 and max 0, marked with the parameter `PlayerPlaysStartingFellowshipGameProcess.CONFIRM_ONLY` ("confirmOnly" = "true"); only the empty answer is taken. A test has to read that the player is asked only that, which is a decision parameter (barred to tests by the Standard), and answer it in a way that shows it was that ask and not a choice that still offered cards (`FreepsDeclineChoosing()` answers either). For information.

**Built.** In `virtualtable/Decisions`: `FreepsIsAskedOnlyToConfirm()`, `ShadowIsAskedOnlyToConfirm()` and the player-neutral `IsAskedOnlyToConfirm(player)`, true where the pending decision carries `confirmOnly` = `true`. In `virtualtable/Choices`: `FreepsConfirm()`, `ShadowConfirm()` and `Confirm(player)`, which Verify that reading and then give the empty answer through `PlayerDecided`, failing and answering nothing otherwise. Tested in `virtualtable/ConfirmOnlyTests` (2 tests). Used by `mechanics/game/StartingFellowshipTests` and by every card and mechanics test that plays a starting fellowship to its limit (the sweep that followed the engine change). The scenario system calls none of it.

### G-0065 SkipToSite cannot pass Esgaroth 30_54 (site 6) on the Hobbit path: its start-of-regroup offer is left pending: SkipCurrentSite declines a start-of-regroup optional trigger for either seat (built 2026-11-05, engine pass 78)

Batch hobb-b30_44 (Bilbo 30_43 to 30_46, the Hobbit table).

What the tests needed: a board at Erebor 30_55, site 7 of `HobbitSites`, the Hobbit path's first mountain, for Smaug 30_66 ("must be at a mountain"). `SkipToSite(n)` for n of 6 or more fails at site 6 with "SkipCurrentSite expected to end between turns or in the Fellowship phase, but ended in REGROUP" (probed: 2 to 5 succeed; 6, 7 and 8 fail). The cause: Esgaroth 30_54 has an optional "At the start of the regroup phase, you may play a [esgaroth] or [dwarven] ally from your draw deck" trigger, offered whatever the deck holds. On player 2's turn `SkipToPhaseInverted(Phase.REGROUP)` leaves the offer pending (a trigger at the start of the target phase is left pending), so `SkipCurrentSite`'s `ShadowPass()`/`FreepsPass()` pair answers the offer and one regroup decision, and the skip ends in REGROUP.

Proposed: in `GameProcedures.SkipCurrentSite` (virtualtable), after each `SkipToPhase(Phase.REGROUP)` and `SkipToPhaseInverted(Phase.REGROUP)`, decline any pending start-of-regroup optional trigger for either seat (`if (FreepsHasOptionalTriggerAvailable()) FreepsDeclineOptionalTrigger();` and the Shadow twin) before the passes. No new signature. It reaches every Hobbit-set test past site 6.

Meanwhile: the Smaug tests of 30_44, 30_45 and 30_46 play the move themselves: `SkipToSite(5)`, then at site 6 `SkipToPhase(Phase.REGROUP)`, `FreepsDeclineOptionalTrigger()`, `PassCurrentPhaseActions()`, `PassReconcile(scn.P2())`, `FreepsChooseToMove()`, which reaches Erebor. No check waits on it; no card is parked.

Built as proposed (a private `DeclineStartOfPhaseOptionalTriggers()` after each skip to REGROUP). Now used by virtualtable/HobbitTableTests (`SkipToSite(7)` reaches Erebor, Bard 30_22 not played) and by the Smaug tests of 30_44, 30_45 and 30_46 (`SkipToSite(6)`, `SkipToPhase(Phase.SHADOW)`).

### G-0064 The Hobbit table: HobbitDraft, HobbitSites, BilboRB, HobbitRing, and the Ring slot left empty in the HDG format (built 2026-11-04, engine pass 77)

From engine pass 77, item hobbit-rig (Ketura, 2026-11-04; notes-hobbit.txt, "What the rig lacked"): the Hobbit batches need a table on the Hobbit Draft Game's rules, where Bilbo is the Ring-bearer and The One Ring 30_48 is a [shire] artifact of the draw deck, not a card in the Ring slot. For information.

**Built.** Test sources only, nothing under `virtualtable/`: in `framework/TestConstants` the constants `HobbitSites` (30_49 to 30_57), `BilboRB` (30_46), `HobbitRing` (30_48) and the format `HobbitDraft` ("hobbit"); in `framework/VirtualTableScenario`, a table whose format's site path is HOBBIT defaults to `HobbitSites` and `BilboRB` and leaves the Ring slot empty (a ringID given is refused), as the format needs no One Ring in a deck. Tested in `virtualtable/HobbitTableTests` (6 tests); the Hobbit batches are its users. The scenario system calls none of it.

### G-0063 The card info's "Affecting card" lines: GetAffectingCard(card) (built 2026-10-21, engine pass 59)

From engine pass 59, item modifier-source (Ketura, answers/2026-10-21-f.md): a test has to read which card the card info names for each modifier, to show a borne card's printed bonus named for that card and not "System". For information.

**Built.** In `virtualtable/CardProperties`: `GetAffectingCard(card)`, the lines the card info lists under "Affecting card" (`ModifiersQuerying.getModifiersAffecting`), each "<source's title>: <text>" or "System: <text>" for a modifier with no source card, sorted. Used by `mechanics/inplay/AffectingCardListTests` (4 tests); tested in `virtualtable/AffectingCardReadingTests` (1 test). The scenario system calls none of it.

### G-0062 The loop's bar and what the clients are sent: FreepsActionLoopBanned(card) / ShadowActionLoopBanned(card), and MarkGameEvents() / GetGameEventsSinceMark(player) (built 2026-10-15, engine pass 51)

From engine pass 51, item loop-run (Q-0329, the voluntary-loop step): a test has to read that an ability a loop used is barred after the run (her "loop-banned" flag on the action), and to count what a run of 99 iterations sends the clients and the replay. For information.

**Built.** In `virtualtable/Actions`: `FreepsActionLoopBanned(card)` / `ShadowActionLoopBanned(card)`, with `(card, text)` twins, over `CardActionLoopBanned(player, card, text)`, the reading of the decision's `loopBanned` word for that card's one action (or the one whose description has the words), failing as `CardActionNoEffect` fails where the card offers no such action or several. In the test framework, `VirtualTableScenario.MarkGameEvents()` joins for each seat a channel to the game as `GameRecorder.recordGame` joins a replay's (a `GameCommunicationChannel` that is no live connection), dropping any joined before and not counting the board it is sent on joining; `GetGameEventsSinceMark(player)` gives every event that seat has been sent since, oldest first. It is test-side because joining a channel under a seat's name is only for a table with no person at it. Used by `mechanics/actions/VoluntaryLoopTests` (and the first pair by `Card_05_029_Tests`); tested in `virtualtable/LoopBanReadingTests` (1 test) and `virtualtable/GameEventMarkTests` (2 tests). The scenario system calls none of them.

### G-0061 The loop step's answers: FreepsNameLoopIterations(n), ShadowAgreeToLoop(), ShadowLowerLoop(m), with their twins (built 2026-10-15, engine pass 51)

From engine pass 51, item loop-detect (Q-0329, the voluntary-loop step): a test has to answer the loop step's two prompts, the Free Peoples player's number and the Shadow player's agreement or lower number. For information.

**Built.** In `virtualtable/Choices`: `FreepsNameLoopIterations(int)` / `ShadowNameLoopIterations(int)` over `NameLoopIterations(player, n)`, which answers the number prompt ("LOOP DETECTED: ..."); `ShadowAgreeToLoop()` / `FreepsAgreeToLoop()` over `AgreeToLoop(player)`, which takes "Agree: ..."; `ShadowLowerLoop(int)` / `FreepsLowerLoop(int)` over `LowerLoop(player, m)`, which takes "Choose a lower number: ..." and names the number, and where the number named was 1 (the lower number can only be 0, and is not asked) takes 0 and fails on any other. Each fails, answering nothing, where its prompt is not pending. Used by `mechanics/actions/VoluntaryLoopTests`, `FiniteRepeatsAreNoLoopTests` and `Card_05_029_Tests.AtAnduinBanksTheBurdenLoopIsFoundAtItsThirdRound`; tested in `virtualtable/LoopAnswerTests` (4 tests). The scenario system calls none of them.

### G-0060 SetFreepsHand / SetShadowHand / SetFreepsDrawDeck / SetShadowDrawDeck / MoveCardsToDiscard left a pending choice of phase actions offering what it did before them: each now has that choice worked out afresh as it finishes (built 2026-10-14, engine pass 40)

From engine pass 39's proposals-1 report, item 2 (The Great River 3_118's author: after a skipper, a card dealt into hand on the way and cheated into the discard pile still read as offered from the hand, and `FreepsUseCardAction` played it from there), queued as engine pass 40, item rig-refresh. For information.

**Built.** In `virtualtable/Decisions`, `RefreshPendingPhaseActions()`: every player at a choice of phase actions ("Play ... action or Pass", "Choose action to play or Pass") has it worked out afresh and sent again, as `RefreshDecision(player)` does; any other decision (a choice of cards, a trigger, a response) is left as it was, and with none pending it does nothing. It is declared in `TableBase` so that `ZoneManipulation` can call it, and `RefreshDecision` now shares its two private steps. `SetHand`, `SetDrawDeck` (which the four twins call) and `MoveCardsToDiscard` (which `MoveCardsToFreepsDiscard` / `MoveCardsToShadowDiscard` call) call it last. No other cheat changed: `MoveCardsToHand`, `MoveCompanionsToTable`, `AddWoundsToChar` and the rest still want `FreepsRefreshDecision()`. `suite all` passed with no test of any card or mechanic changing (12430 tests): every reading after these cheats came out the same, and the `FreepsRefreshDecision()` calls already written after them are now redundant and left in place. The scenario system calls none of the five. Tested in `virtualtable/DecisionRefreshTests` (four new tests: a card cheated from hand into the discard pile, a hand and a draw deck set at the fellowship choice, the Shadow hand set at the Shadow choice, and a pending choice of cards left untouched by all five).

### G-0059 FreepsAcceptOptionalTrigger() / ShadowAcceptOptionalTrigger() sent "0" whatever was asked: refused, saying what is asked, unless a choice of actions with an action on offer is pending (built 2026-10-12, engine pass 32)

From `orchestrator/proposals-done.md` (dec-b8_57, the reviewer of 8_113: Card_08_092_Tests' old threat test answered the showing of a draw deck with a second accept and passed), queued as engine pass 32, item rig. For information.

**Built.** In `virtualtable/Decisions`, both methods go through one private `AcceptOptionalTrigger(player)`, which asks `DecisionGuard.requiredActionChoice` (package-private, beside G-0034's guard) before it answers "0": the player must be asked something, it must be a choice of actions (CARD_ACTION_CHOICE: an optional trigger or response, a phase's actions; ACTION_CHOICE: the order of required responses), and at least one action must be on offer. Anywhere else the call fails, saying what each player is being asked, and answers nothing. `FreepsDeclineOptionalTrigger()` is untouched. The whole suite (`suite all`, 11067 tests) passed with the guard in place and, run again with a counter, showed that every accept the suite reaches (of some 810 calls in the test sources) is answered at an "Optional ..." decision: no test was relying on the old answer, so none had to be mended (Card_08_092_Tests had been mended by its batch). Tested in `virtualtable/RigFailureTests.AcceptingAnOptionalTriggerWhereNoActionIsOnOfferIsRefusedSayingWhatIsAskedAndNothingIsAnswered`. The scenario system calls neither method.

### G-0058 A draw deck of exactly the cards named, or an empty one: SetFreepsDrawDeck(cards...) / SetShadowDrawDeck(cards...) (built 2026-10-12, engine pass 32)

From `orchestrator/proposals-done.md` (dec-b7_345: Catapult 8_32 needed a second Get...Scenario() for "the opponent's draw deck is empty"; dec-b9_1: Seeing Stone of Orthanc 9_38's empty decks had to be extended by hand as aliases were added), queued as engine pass 32, item rig. For information.

**Built.** In `virtualtable/ZoneManipulation`, beside `SetFreepsHand` and `SetFreepsAdventureDeck` (G-0054): `void SetFreepsDrawDeck(PhysicalCardImpl... cards)` and `void SetShadowDrawDeck(PhysicalCardImpl... cards)` over `void SetDrawDeck(String player, PhysicalCardImpl... cards)`. The deck is left holding exactly the cards named, the first named on top (none: an empty deck); they are taken from wherever they lie. The deck's other cards are set aside in no zone, as G-0054's sites are: not in a pile, not in play, not removed from the game, so no discard-pile or removed-pile count sees them; a cheat puts one back by its name. Refuses a card of the other player's and a site. Called before `StartGame()` the deck keeps those cards, but the deal's shuffle orders them. Used by `Card_08_032_Tests.WithTheOpponentsDrawDeckEmptyTheCardsAreDiscardedFromHandAndNoShadowCardIsDiscarded`, which now uses the file's one table (`SetShadowDrawDeck()` after `StartGame()`) and no longer needs `GetEmptyShadowDeckScenario()`. Tested in `virtualtable/DrawDeckSettingTests` (4 tests).

### G-0055 A card of player 2's cheated into play before his turn has its standing text started twice (GameState.addCardToZone): the game starts a card's text as it comes into play only if the card is active, and a card laid out of turn is started once, when a turn begins in which it is active (built 2026-10-11, engine pass 28)

**Ketura, 2026-10-11:** "Good catch. A for sure; the gamestate doesn't want to run that for inactive cards even in real play."

**Built**, as A. `GameState.addCardToZone` starts the text of a card coming into play only if `isCardInPlayActive(card)`, which is what the turn's own start-up (`startAffectingCardsForCurrentPlayer`) asks; a site is started as before. No rig method changed (a sentence in `MoveCardToZone`'s Javadoc). His Ranger of the South 18_56 now reads 5 on player 1's turn (it read 7 there, with a token on player 1's active card to spot) and 7 on his own (it read 9). The same held for the Shadow cards of the player whose turn it is: player 1's own Orc Bowmen 1_264, laid on his turn, added 1 to the minion archery total there and 2 on player 2's turn.

Rig tests `virtualtable/CardLaidOutOfTurnTests` (four: a card laid on its owner's turn is in force at once, in each seat; player 2's Free Peoples card laid before `StartGame()`; the same laid during player 1's turn; a Shadow card laid on its owner's own turn). No landed test had the doubled number written into it: the whole suite passes unchanged, and a run of it with the engine made to log every card laid while not active found 37 tests that lay one, 14 of them going on to its owner's turn, none reading a number its text changes (the list is in `orchestrator/engine-pass-28/activestart.md`). The pitfall's work-around ("lay his cards after `SkipToOtherPlayersTurn()`") is taken out.

What the request said, as filed:

From engine pass 26, item `mech-c` (its report, "Met on the way"), filed by the orchestrator.

**What happens.** A card of player 2's that a test puts into play before his own turn (before `StartGame()`, or during player 1's turn) has its standing text started when it is put down, though it is not active then, and started AGAIN when his turn begins. On his turn its modifiers count twice: his Ranger of the South 18_56 read strength 9 for one [gondor] token, not 7 (laid on his own turn it reads 7).

**Why.** `GameState.addCardToZone` starts a card's text whenever the phase lets cards affect the game, without asking whether the card is active. Real play never puts an inactive card into play, so no game meets it; a test's cheat does. So it is a fault of the rig's way in, but the line that would be changed is in the engine's `GameState`, which the scenario system you are designing also goes through.

**What is done meanwhile.** The pitfalls now tell authors to lay player 2's cards on his own turn. Nothing was changed in the code.

**What is asked.** One of:
- **A. Mend it where it is**: `addCardToZone` starts a card's text only if the card is active (its owner's side is the one whose turn it is, as the turn's own start-up decides it). A test in `virtualtable/` both ways. Then a search of the landed tests for a number read on player 2's turn from a card cheated in early, in case one has the doubled number written into it (I do not know that any has).
- **B. Mend it in the rig only**: the rig's "move to table" methods refuse, or defer, a card of the player whose turn it is not, and say why. No engine line changes.
- **C. Leave it** as a documented pitfall.

The orchestrator's recommendation: A, because a silently doubled number is the kind of thing a test should never be able to assert; but the line is in code your scenario system shares, so it waits for your word.

### G-0057 The number a question for a number comes filled in with (F-0419, Shelob, Menace 18_34): FreepsGetChoiceDefault / ShadowGetChoiceDefault, null where the question fills none in (built 2026-10-11, engine pass 26)

From engine pass 26, item definitions (F-0419, Shelob, Menace 18_34: "you may add threats up to the number of burdens you can spot"; Ketura, 2026-10-11: "it should offer a choice, 0-N, ideally with a prompt defaulting to N").

**What the test needed.** To read the number a question for a number comes filled in with (the decision's `defaultValue`, which the client shows in the box before the player types). The rig read a number question's least and most (`FreepsGetChoiceMin`, `FreepsGetChoiceMax`) and nothing of what it is filled in with, so the "defaulting to N" of her ruling could not be shown by a card test.

**Built**, for your information. In `Choices`: `FreepsGetChoiceDefault()`, `ShadowGetChoiceDefault()` and the player-neutral `GetChoiceDefault(player)`. Each returns the number filled in, or null where the question fills none in (the client then shows 0), and fails, saying what each player is being asked, where that player is not being asked for a number (`DecisionGuard.requiredNumberQuestion`). A reading and nothing more: no decision is answered.

Rig test `virtualtable/DecisionReadingTests.TheNumberAQuestionIsFilledInWithIsReadAndIsNullWhereItFillsNoneIn`: Aragorn's Pipe 1_91 with two pipes to spot (0 to 2, filled in with 2; another number may still be answered), Plotting 7_67 ("add up to 9 threats": 0 to 2 with two companions, nothing filled in), and the refusals at a choice of actions and for a player who is asked nothing. Used by `Card_18_034_Tests.TheShadowPlayerNamesHowManyThreatsToAddFromNoneToTheBurdensSpottedAndNoMoreThanCanBeAdded`.

### G-0056 What a seat has been sent of where a card lies and of who is in the skirmish (Q-0232, The One Ring left where its bearer was killed): FreepsSeesCardInZone / ShadowSeesCardInZone, FreepsSeesCardBorneBy / ShadowSeesCardBorneBy and FreepsSeesCardInSkirmish / ShadowSeesCardInSkirmish, read from the channel the test table keeps for each seat (built 2026-10-11, engine pass 26)

From engine pass 26, item ringrest (Q-0232, "B+": The One Ring left where its Ring-bearer was killed). The item asks for a mechanics test of "the events sent"; the rig read nothing of what a seat is sent about a card in play or about a skirmish.

**What the test needed.** To read, as each client has it, (1) the zone a card was last sent in and (2) the card it was sent as borne by, or none: the Ring moved from Frodo to the Free Peoples characters and told to the clients as attached to no card, and later as borne by Sam; (3) whether a client has been told a card is in the skirmish being fought: the Ring is shown in the dead Ring-bearer's place there while the game has it fighting nobody, so `IsCharSkirmishing` cannot be the reading.

**Built**, for your information. In `CardProperties`: `FreepsSeesCardInZone(card, zone)`, `ShadowSeesCardInZone(card, zone)` and the player-neutral `PlayerSeesCardInZone(player, card, zone)`; `FreepsSeesCardBorneBy(card, bearer)`, `ShadowSeesCardBorneBy(card, bearer)` and `PlayerSeesCardBorneBy(player, card, bearer)`, where a null bearer asks "shown as borne by no card". In `Skirmishes`: `FreepsSeesCardInSkirmish(card)`, `ShadowSeesCardInSkirmish(card)` and `PlayerSeesCardInSkirmish(player, card)`. All answer from `SeatChannel`, as G-0037's readings do: from the channel the test table keeps for the seat and from a client joined to the seat as the reading is made, refused if the two were sent different things, and refused on a table that keeps no seat channels (a `ScenarioTable` keeps none, so nothing a live scenario game does has changed).

`SeatChannel` now keeps, for each card a seat has been sent and not since sent the removal of, the zone and the bearer it was last sent with (from the card's `PUT_CARD_INTO_PLAY` and `MOVE_CARD_IN_PLAY` events), and the cards the seat has been told are in the skirmish, kept as the client keeps them (a skirmish's start and an addition to it mark cards, a removal from it or from play unmarks one, its end unmarks all). `FreepsSeesCardInHand` is read from the same record (a card last sent in the HAND zone) and answers as before. One limit: a site as it is laid on the adventure path is not kept (the server's channel cannot send a site that has no site number yet, which is how a test's `MoveCardToAdventurePath` lays one), so `...SeesCardInZone` is refused for a site on the path. No test asks it.

Rig tests `virtualtable/SeatReadingTests` (seven now: two new, of the zone and bearer readings and of the skirmish reading, and the two refusal tests extended). Used by `mechanics/TheOneRingWhenItsBearerIsKilledTests` (five tests).

### G-0051 Reading a follower's Aid cost for a stats test: GetAidTwilightCost(follower), on a number the blueprint now keeps for an Aid that is a plain amount of twilight (built 2026-10-10, engine pass 26)

**Built.** Ketura, 2026-10-10: "Yeah that's good to add, tho I would make it `GetAidTwilightCost` to emphasize that it's only managing the case of twilight; if a more generic effect-returning function is needed I could also see `GetAidCost` as that more generic version." In the engine: `AidCost` (the effect processor) reads the number off the definition and the `AidCostSource` it builds keeps it; `LotroCardBlueprint.getAidTwilightCost()` answers with it, null for every other card and every other Aid. The number is kept where the whole Aid is ONE cost, an `AddTwilight` or a `RemoveTwilight`, with its amount written out or left out (1): 14 Free Peoples definitions add it (13_28, 13_37, 13_38, 15_12, 15_26, 15_134, 15_135, 15_137, 17_22, 18_7, 18_17, 18_52, 18_111, 68_111) and the one Shadow follower, Saruman 17_38 ("Aid - (1).", "you may remove (1)"), removes it. Nothing is kept for the other 20: a burden, a threat, an exertion, a card from hand, and Thrain 32_9, whose Aid is a spot and (2). What is PAID is unchanged: the definition's cost, as before. In `virtualtable/CardProperties`: `int GetAidTwilightCost(PhysicalCardImpl follower)`, which fails, saying so, for a card that is no follower and for an Aid that is not a plain amount of twilight. No `GetAidCost` was built: nothing at hand needs it.

Used by `Card_18_111_ErrataTests` (Robin Smallburrow 68_111, the stats test: 2). Tested in `virtualtable/AidCostReadingTests` (3 tests: the number read is the number an Aid adds, Pallando 13_37, and removes, Saruman 17_38; the refusals) and, for which definitions keep a number, in `gemp-lotr-logic`'s `cards/build/AidTwilightCostDefinitionTests` (3 tests). The Standard's Keyword row and the pitfall "A follower's Aid" name the reader.

From `orchestrator/proposals.md` (the lander of err-b68_111, Robin Smallburrow 68_111, the first errata follower); looked at in engine pass 24, item rig, and not built: it is not a rig method alone.

**What the test needed.** To assert the N of "Aid - (N)" in a follower's stats test. No blueprint field holds it and `Keyword.AID` is never set: the cost is an `aidCost` effect in the definition, which the blueprint keeps as a cost it can only check and append (`canPayAidCost`, `appendAidCosts`).

**Proposed.** In `virtualtable/CardProperties`: `int GetAidCost(PhysicalCardImpl follower)`. For the rig to read a number the engine has to keep one: `AidCost` (the effect processor) would record the twilight amount where the cost is a plain `addTwilight`, behind a new blueprint getter. 35 definitions have an `aidCost`; 14 of them add twilight, and the rest pay something that is no number of twilight (burdens, threats, an exertion, a discard from hand: Elendil's Army 18_47, "Aid - Exert a [gondor] companion"), for which the reading would have to fail or say so. That is an engine change made for a test's reading, and so Ketura's to want or not.

**Meanwhile.** Nothing is parked. The Standard's Keyword row has the stats test's header state the Aid cost and a test that pays the Aid read the twilight pool N higher (Card_18_111_ErrataTests), which shows the number by play.

### G-0053 One player's site marker read during the other player's turn (Deep Hatred 13_3, "ahead on the adventure path"): FreepsGetSiteNumber() / ShadowGetSiteNumber() (built 2026-10-10, engine pass 26)

**Built.** Ketura, 2026-10-10: "Approved." In `virtualtable/GameProperties`, as proposed: `int FreepsGetSiteNumber()` and `int ShadowGetSiteNumber()` (player 1's and player 2's marker, whoever's turn it is) over `int GetSiteNumber(String player)`, which reads `gameState().getPlayerPosition(player)` and refuses a name that is nobody's at the table. `GetCurrentSiteNumber()` is unchanged (the scenario system calls it) and has a doc comment now.

Used by `Card_13_003_Tests` (Deep Hatred): the two `// waits:` comments are gone and both markers are read at each step of the two lone tests (behind 1 against 2, level 2 and 2, ahead 3 against 2; level 1 and 1, ahead 2 against 1). Tested in `virtualtable/GameReadingTests.EachPlayersSiteMarkerIsReadWhoeverHasTheTurn`. The pitfall "Whose site" names the readers.

From the at batch `cards-12_166-13_105`, Deep Hatred 13_3 ("Discard this from play if the fellowship is ahead on the adventure path"), two lone tests in `Card_13_003_Tests` replacing `at/RequirementsAtTest.isAhead`. Not blocking: nothing is parked on it.

**What the test needed.** To read BOTH players' site markers at each step: CR 4.2 "ahead" compares a player's marker with every other player's ("at a higher site number than all other players' site markers"), and the directive asks for the two read each time (behind, level, ahead). `GetCurrentSiteNumber()` answers for the player whose turn it is and there is no reader for the other player's marker (pitfalls, "Whose site").

**Proposed.** In `virtualtable/GameProperties`, beside `GetCurrentSiteNumber()`, in the manner of G-0044's burdens: `int FreepsGetSiteNumber()` and `int ShadowGetSiteNumber()` (player 1's and player 2's marker, whoever's turn it is) and the player-neutral `int GetSiteNumber(String player)` they call, over `gameState().getPlayerPosition(player)`, which the engine already has (the rig's own `LowestPlayerPosition()` in ZoneManipulation reads it).

**Meanwhile.** The tests read the mover's marker with `GetCurrentSiteNumber()` and, for the other player, only what was read while it was his turn (player 1 at site 2 before his turn ends; both players at site 1 as the game starts is not read at all). Two comments stand where the readings would be (`// waits: G-.... no reader for the marker of the player whose turn it is not`), to become assertions when the reader exists.

### G-0054 Emptying a player's adventure deck (Savage Southron 15_92, "if the Free People's player has at least 1 site in his or her adventure deck"): SetFreepsAdventureDeck(sites...) / SetShadowAdventureDeck(sites...) (built 2026-10-10, engine pass 26)

**Built.** Ketura, 2026-10-10: "Approved." In `virtualtable/ZoneManipulation`, as proposed: `void SetFreepsAdventureDeck(PhysicalCardImpl... sites)` and `void SetShadowAdventureDeck(PhysicalCardImpl... sites)` over `void SetAdventureDeck(String player, PhysicalCardImpl... sites)`. That player's adventure deck is left holding exactly the sites named (none: an empty deck). His other sites are taken out of the deck into no zone at all (`getZone()` is null: not on the path, not in a pile, not removed from the game), so no card and no rule finds them. The sites that stay keep their order, which matters to the game (the first card of the deck says whether the path is numbered). A site set aside may be named again and goes back in, at the end. It refuses a card that is not that player's own site, and a site that is anywhere but in the deck or set aside from it: a site on the adventure path is not taken off it. Not built: the fuller cheat that lays a whole path.

Two things an author must know, both in the Javadoc and in the pitfall beside Savage Southron: a choice of actions already pending is refreshed with `ShadowRefreshDecision()` / `FreepsRefreshDecision()` to see the change; and the game lays sites from this deck when its player is the Shadow player, so a move to a site not yet laid that finds the deck without one breaks the game (a NullPointerException in the engine's own site-laying: not touched here).

Used by `Card_15_092_Tests.ManeuverAbilityIsOfferedWithOneSiteLeftInTheFreePeoplesAdventureDeckAndNotWithNone` (a new lone test in place of the `// waits:` comment: offered with one site left, not offered with none though the three Men are unwounded and the Shadow player's own deck is full). Tested in `virtualtable/AdventureDeckSettingTests` (3 tests).

From the at batch `cards-13_133-15_148`, Savage Southron 15_92 ("Maneuver: If the Free People's player has at least 1 site in his or her adventure deck, exert 2 [men] Men to randomly choose a site from that deck. Replace the fellowship's current site with that site."), a lone test in `Card_15_092_Tests` replacing `at/RequirementsAtTest.hasCardInAdventureDeck`. Not blocking: nothing is parked on it.

**What the test needed.** The other half of the condition: the board on which the Free Peoples player has NO site in his adventure deck, where the ability is not offered though two [men] Men can exert. A game reaches it when every site on the path is that player's own (he laid site 1, and the rest as the Shadow player on his opponent's turns or with his own site-playing cards). No cheat takes sites out of an adventure deck: `MoveCardToAdventurePath(card)` lays one site at the number after the fellowship's and nothing else, and `MoveCardsToAdventureDeck` only puts sites in. A site map of two sites would empty the deck too, but no game has such a deck and it makes the one replacement a twin for a twin (Q-0226).

**Proposed.** In `virtualtable/ZoneManipulation`, beside `MoveCardsToAdventureDeck`: `void SetFreepsAdventureDeck(PhysicalCardImpl... sites)` and `void SetShadowAdventureDeck(PhysicalCardImpl... sites)` over `void SetAdventureDeck(String player, PhysicalCardImpl... sites)`: that player's adventure deck is left holding exactly the sites named (none named: an empty deck), and his other sites are set aside out of every zone a card reads, as `SetHand`'s leftovers are. A cheat like the rest: nothing is played and nothing answers it. (A fuller cheat that lays a whole path, each site at its number under its owner, would give the board a game reaches; the deck alone is all this test reads.)

**Meanwhile.** The test reads the half that is offered and carried out (two of three Men exert, the site laid is one of the Free Peoples player's eight, the other seven stay in his deck, the Shadow player's site goes back to an adventure deck). A comment stands where the other half would be read (`// waits: G-.... no cheat takes the sites out of a player's adventure deck`), to become a second table when the cheat exists.

### G-0052 Reading a deck's objections line by line, whatever order they came in: DeckValidationScenario.getErrorLines(errors) (built 2026-10-10, engine pass 24)

From engine pass 24, item fixes-c (the older `mechanics/` classes brought to `lint.py --form`). For information.

**What a test had.** `mechanics/SitesFromAnyBlockTests` kept a helper method of its own, `Lines(errors)`, to set a format's objections beside the list expected: `validate()` gives the sites' objections as ONE entry of several lines, in whatever order the deck holds its sites, so `assertEquals(List.of(...), dvs.validate(deck))` cannot be written for two of them. A test class has no helper methods (the Standard, "The file"): what the helper did belongs in the rig.

**Built.** In `framework/DeckValidationScenario` (test sources), beside `getMapErrorSites`: `List<String> getErrorLines(List<String> errors)`, every line of every objection that is not empty, one to an entry and in alphabetical order. The helper's own line, moved; no test of itself (a one-line reading).

Used by `mechanics/SitesFromAnyBlockTests` (seven readings in its five tests). `pitfalls.md`, "Validating a deck", names it.

### G-0042 Which player has lost or won a finished game: FreepsHasLost() / ShadowHasLost() and FreepsHasWon() / ShadowHasWon() (built 2026-10-10, engine pass 24)

**Built (engine pass 24, item rig).** In `virtualtable/GameProperties`, beside `GameIsFinished()`: `boolean FreepsHasLost()`, `boolean ShadowHasLost()`, `boolean FreepsHasWon()`, `boolean ShadowHasWon()`, and the player-neutral `PlayerHasLost(String player)` and `PlayerHasWon(String player)` they call. All four are false while the game is going on and for a game that was cancelled. They go by the player and not by the side he had on the turn the game ended. The winner is the engine's own (`LotroGame.getWinnerPlayerId()`); at a table of two the game is over the moment one player loses and the other is then its winner, so "has lost" is "the other player has won", and no engine class changed. The booleans were built and not `GetWinner()`: a test says what it means in one reading, and the request's other use (the lander of 94_31: "say the owner won") wants the winning half. Why a game ended is still read from the game log.

Used by `mechanics/CorruptionBeforeTheGameStartsTests` (all three tests: the player who bid her Ring-bearer's resistance has lost and the other has won; the log line is kept for the reason alone) and tested itself in `virtualtable/GameReadingTests` (nobody has won or lost while the game goes on; a corruption on each player's own turn; the fellowship that lives through site 9). Card_94_031_Tests still reads `GameIsFinished()` alone: a line in `proposals.md` for a fixer.

From the at batch `mech-setup`, `mechanics/CorruptionBeforeTheGameStartsTests` (CR 4.2 "bid": a player who bids
his Ring-bearer's resistance or more "loses the game" before it starts), replacing
`at/IndividualCardAtTest.garradrielCorruptionAtStart`.

**What the test needed.** To read WHICH player has lost a finished game. `GameIsFinished()` is the rig's only
reader of a game that is over, and it does not say who lost: with two Galadriels at the table and bids of 5 and 6,
the test must show that the player who bid 6 lost and the player who bid 5 did not.

**Proposed.** In `virtualtable/GameProperties`, beside `GameIsFinished()`: `boolean FreepsHasLost()` and
`boolean ShadowHasLost()` (false while the game is going on), or one `String GetWinner()` that is null until the
game is finished. The engine already holds it (`LotroGame.getWinnerPlayerId()`).

**Meanwhile.** Nothing is left out and nothing is parked: the three tests read the game log with the rig's own
`GameLogSinceMarkSays(scn.P1() + " lost")` (G-0033), the mark set inside the bid handler. That shows it, but it
rests on the wording of the log line ("<player> lost due to: ..."); the tests should move to the reader when it
exists.

### G-0043 Telling the relentless round from the first and the fierce one: InRelentlessRound() (built 2026-10-10, engine pass 24)

**Built (engine pass 24, item rig).** In `virtualtable/GameProperties`, beside `InFierceRound()`: `boolean InRelentlessRound()`, a one-line reading of `GameState.isRelentlessSkirmishes()`: true from the game's "Relentless skirmishes" until the last relentless skirmish is over, false in the first round, in the fierce round and in every other phase.

Used by `mechanics/RelentlessTests` (all three tests: true in each relentless round beside `InFierceRound()` false, and false in the first and fierce rounds; the `// waits:` comment is gone) and tested itself in `virtualtable/AssignmentReadingTests.TheRelentlessRoundIsToldFromTheFirstAndFromTheFierceRound`.

From the at batch `mech-hinder-d`, `mechanics/RelentlessTests` (CR 5.0 "relentless"), replacing
`at/RelentlessTests.NonFierceNonRelentlessMinionsDoNotParticipateInRelentlessSkirmishes`. Not blocking: no
reading is left out and nothing is parked on it.

**What the test needed.** To say which round it is in when the third assignment phase comes. The relentless round
reads as the first round does: `GetCurrentPhase()` is `Phase.ASSIGNMENT`, the assignment actions are pending, and
`InFierceRound()` (G-0031) is false. The three tests show the round by what came before it (a fierce round just
fought, or none) and by who is on the assignment list (the relentless minion alone), which is enough to fail if the
round were missing or held the wrong minions; but nothing names it, and a test of a card whose text speaks of a
"relentless skirmish" (the rule's own example is Filibert Bolger, Wily Rascal) cannot say "this is the relentless
round" in one reading.

**Proposed.** In `virtualtable/GameProperties`, beside `InFierceRound()`: `boolean InRelentlessRound()`, a one-line
reading of `GameState.isRelentlessSkirmishes()`, which the engine already keeps. With it a rig test beside
`AssignmentReadingTests.TheFierceRoundIsToldFromTheFirst...`, and the three readings of `InFierceRound()` false in
`mechanics/RelentlessTests` gain `InRelentlessRound()` true beside them.

**Meanwhile.** `mechanics/RelentlessTests` reads `Phase.ASSIGNMENT`, `InFierceRound()` false, the assignment actions
pending and the one minion on the list in each relentless round; pitfalls.md ("The fierce round reads as the first
does") now says the relentless round reads the same way and is passed with `PassAssignmentActions()` and
`PassSkirmishActions()`.

### G-0044 One player's burdens read during the other player's turn (Thin and Stretched 1_279, a burden added as the turn ends): FreepsGetBurdens() / ShadowGetBurdens() (built 2026-10-10, engine pass 24)

**Built (engine pass 24, item rig).** In `virtualtable/GameProperties`, beside `GetBurdens()`: `int FreepsGetBurdens()` and `int ShadowGetBurdens()` (player 1's and player 2's, whoever's turn it is) and the player-neutral `GetBurdens(String player)` they call, over `gameState().getPlayerBurdens(player)`; a name that is no player's at the table is refused. `GetBurdens()` with no player is unchanged (the scenario system calls it) and now has a doc comment that says whose it reads. No cheat to lay the other player's burdens was added: no test needs one.

Used by Thin and Stretched 1_279 (`Card_01_279_Tests`, the three lone tests: the first player's burdens are read at the other player's first decision, 1 where the burden is due and 0 on the two boards where it is not; the three `// waits:` comments are gone) and by `mechanics/CorruptionBeforeTheGameStartsTests` (each player's bid on his own Ring-bearer); tested itself in `virtualtable/GameReadingTests` (across three turns, and the two bids before the game starts).

From the at batch `cards-01_262-02_42`, Thin and Stretched 1_279 ("Add a burden at the end of each turn during
which bearer was not assigned to a skirmish (and another companion was)"), three lone tests in
`Card_01_279_Tests` replacing `at/RequirementsAtTest.wasAssignedToSkirmish`. Not blocking: nothing is parked on
it.

**What the test needed.** To read the first player's burdens just after his turn has ended. The burden is added
as the turn ends, and the next thing the game asks is the OTHER player's fellowship phase. `GetBurdens()` is the
current player's (`gameState().getBurdens()`), `GetResistance(card)` goes by the same number, and `AddBurdens` /
`RemoveBurdens` likewise, so from that moment until the first player's next turn the rig reads the other player's
burdens and has no way to the first player's. The same gap is why "the other player's burdens are not counted"
(Race Text 92_25) has no board (pitfalls, "`AddBurdens`, `RemoveBurdens` and `GetBurdens` are the current
player's").

**Proposed.** In `virtualtable/GameProperties`, beside `GetBurdens()`: `int FreepsGetBurdens()` and
`int ShadowGetBurdens()` (player 1's and player 2's, whoever's turn it is), each one line over
`gameState().getPlayerBurdens(player)`, which the engine already has. A cheat to lay them
(`FreepsAddBurdens(n)` / `ShadowAddBurdens(n)`) only if a test turns out to need one: none does today.

**Meanwhile.** Each of the three tests reads 0 burdens at the first player's last decision of the turn (the
"another move" question), ends the turn, plays the other player's empty turn through with
`SkipToOtherPlayersTurn()`, and reads `GetBurdens()` as the first player's next turn begins (1, or 0 on the two
boards where no burden is due). Nothing on those boards can add or remove a burden in between, so the reading
shows the burden; what it cannot show is that the burden was there the moment the turn ended. That reading is
left out at a comment in each test (`// waits: G-.... no reader of one player's burdens during the other
player's turn`) and should be added when the reader exists.

### G-0045 The catch-all FreepsChoose(text) / ShadowChoose(text) at a choice of actions took whichever action had the words: refused, saying what is asked (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the lander of rtmd-b92_35, Race Text 92_35), built in engine pass 24, item rig. For information.

**What a test met.** `FreepsChoose(text)` and `ShadowChoose(text)` answer a multiple choice if one is pending. With none pending and actions on offer they went on to `FreepsChooseAction(text)` and took whichever action had the words in its wording, in silence. 92_35's old test answered `FreepsChoose("heal")` to a choice the game never asked (F-0116) and discarded a card to heal with instead, passing by luck.

**Built.** In `virtualtable/Choices`, the two methods now share one private catch-all: a multiple choice is answered by its words as before, a number or any other raw answer is given as before, and at a choice of actions that has any on offer (phase actions, optional triggers, required responses to order) the call fails, saying what was to be chosen and what the player is being asked with the actions on offer, and answers nothing. An action is taken by its card or with `FreepsChooseAction(text)`. Before the change the whole suite was run with a counter on that branch: no test anywhere reached it (0 calls; 124 calls give a number, 5 a card id, the rest the empty answer at a starting fellowship), so nothing had to be rewritten. The scenario system calls neither method.

Tested in `virtualtable/RigFailureTests.TheCatchAllChooseTakesNoActionAtAChoiceOfActionsAndStillAnswersAMultipleChoice`. To undo: put the `ChooseAction` branch back in `Choices.Choose`.

### G-0046 FreepsDeclineSanctuaryHealing() sent five empty answers whatever was asked: it declines the sanctuary heals still asked and nothing else (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the landers of err-b68_134, Buckland Homestead 68_139, and v0-b100_1, Rath Dínen 100_1), built in engine pass 24, item rig. For information.

**What a test met.** `FreepsDeclineSanctuaryHealing()` sent five empty answers whatever was being asked, and had no doc comment. With one heal taken only four were left to decline, and the fifth answer passed the fellowship phase: the fellowship moved. At Rath Dínen, which asks three heals, the untouched call overshot on its fourth answer. Both tests were written with `FreepsDeclineChoosing()` once for each heal left, and by the time of this pass no test called the method at all.

**Built.** In `virtualtable/Choices`: it answers "Sanctuary healing - Choose companion to heal" with no companion for as long as that is what player 1 is being asked, and stops at the first decision that is anything else, which it leaves unanswered. It fails, saying what is being asked, if no sanctuary heal is asked when it is called. No Shadow twin was added: no test at hand declines a sanctuary on player 2's turn.

Tested in `virtualtable/SanctuaryHealingTests` (all five declined; one taken and four declined; refused at a site that is no sanctuary and once the game has stopped asking). The landed tests that use `FreepsDeclineChoosing()` are unchanged and work as before.

### G-0047 Naming the choice of the next skirmish: FreepsAwaitingSkirmishChoice() / ShadowAwaitingSkirmishChoice() (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the lander of err-b69_37, Úlairë Nertëa 69_38), built in engine pass 24, item rig. For information. This was on the "Considered and not filed" list of 2026-10-08 as `FreepsAwaitingSkirmishChoice()`, asked for by one author; since then 112 readings in 49 test files match the prompt by its words.

**What a test met.** When every minion is assigned the Free Peoples player is asked "Choose next skirmish to resolve". Every other moment of a turn has a method (`AwaitingFreepsAssignmentPhaseActions()` and its kin); this one was read as `FreepsDecisionAvailable("Choose next skirmish to resolve")`.

**Built.** In `virtualtable/Decisions`: `boolean FreepsAwaitingSkirmishChoice()`, `boolean ShadowAwaitingSkirmishChoice()` (player 2 is the one asked on his own turn) and the player-neutral `AwaitingSkirmishChoice(String player)` both call: one line over `DecisionAvailable`. Tested in `virtualtable/GameReadingTests.TheChoiceOfTheNextSkirmishIsReadForThePlayerWhoseFellowshipIsFighting`, on both players' turns.

**Not done.** The 112 readings in landed tests are left as they are: they are right, and other cards' tests are not an engine item's to rewrite. `lint.py` does not yet count the new readers as a test's closing reading (its `bookend` rule) or as showing a player is asked; that is a line in `proposals.md` for the tools item, and `pitfalls.md` tells authors to keep the `...DecisionAvailable` form as a test's last line until it does.

### G-0048 A test table's site map: numbered sites go into the adventure deck first, whatever their keys, and two keys with one blueprint name its two copies (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the landers of err-b68_111, Scouring of the Shire 68_112, and err-b68_134, Doorway to Doom 68_134), built in engine pass 24, item rig. For information.

**What a test met.** (1) The test table built each adventure deck in the site map's HashMap order, and the game takes a deck's sites as unordered whenever the FIRST card of the adventure deck has no printed site number (`GameState.usesUnorderedSites`, asked again at every move). A Shadows-block site standing on a Fellowship path under a key that happened to come out first (`homestead`, `path`, `anduin`, `site9` itself) made the whole path unordered: `StartGame()` came to rest on "Choose site to play". Authors renamed keys until it worked. (2) A site map that named one blueprint under two keys gave both copies the first key: the other key named nothing and `GetFreepsSite("site1")` threw NoSuchElementException.

**Built.** In `framework/VirtualTableScenario` (test sources): (1) the sites that have a printed number go into each adventure deck ahead of those that have none, each group in the map's own order, so a path with any numbered site is laid by number for as long as one is left in the deck, whatever the keys. A path of Shadows-block sites alone is exactly as it was. The other way about, one numbered site among Shadows-block sites now always makes the path a numbered one, and G-0036's refusal (no site 1) says that the table orders the deck so. (2) The site-alias search passes over a key already given, as the card-alias search always has: two keys with one blueprint name its two copies.

Before the change the suite was run with a counter: 32 tables (8 test classes) hold a map that mixes numbered and unnumbered sites, in every one a numbered site already came first, and none changes behaviour. Tested in `virtualtable/FirstSiteTests` (three new tests: a Shadows-block site under `homestead` on a Fellowship path, through `StartGame()` and the first move; the refusal the other way about; two keys with one blueprint).

### G-0049 A helper asked to play on through the turn that ends the game: refused, saying that the game is over and who won (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the lander of rtmd-b94_29, Race Text 94_31), built in engine pass 24, item rig, with G-0042. For information.

**What a test met.** A helper that plays a turn out (`SkipToOtherPlayersTurn`, `SkipToMovementDecision`, `PassCurrentPhaseActions`) asked to play on through the turn that ends the game died of a bare `NoSuchElementException` out of `GameProperties.GetDecidingPlayer`: `SkipToSite(8)` and then `SkipToOtherPlayersTurn()`.

**Built.** `GetDecidingPlayer()`, where all of those helpers start, now fails with a sentence when nobody is being asked anything: that the game is over and who has won it ("... the game is over (Free Peoples Player has won). A helper that plays a turn out cannot play on through the turn that ends the game."), that it was cancelled, or that the game is at rest with no decision pending. Nothing is answered and nothing else changes: with a decision pending it returns the same player as before. The scenario system does not call it.

Tested in `virtualtable/GameReadingTests.TheFellowshipThatLivesThroughSiteNineHasWonAndPlayingOnPastTheEndOfTheGameIsRefusedSayingSo`.

### G-0050 DeckValidationScenario's filler is never what a format objects to, at any size (built 2026-10-10, engine pass 24)

From `orchestrator/proposals.md` (the landers of rtmd-b94_22, Race Text 94_22, and rtmd-b91_17), built in engine pass 24, item rig. For information.

**What a test met.** `DeckValidationScenario` promises that its filler is never what a format objects to. From 37 filler cards a side it was: the Free Peoples range reaches Boromir 1_97 straight after Boromir 1_96 (five of one title, eight at 50), and the Shadow range reaches Forces of Mordor 1_248, which Multipath X-lists and Expanded R-lists. 94_22's tests of 99- and 100-card decks could read only the deck-size objection, never that nothing else was said.

**Built.** In `framework/DeckValidationScenario` (test sources): the filler passes over a card whose title an earlier filler card has, a card the format has something against on its own account (`validateCard`), and a card of which the format allows fewer than four copies (R-list, restricted names, limit 2 and 3). Decks of up to 36 filler cards a side are card for card what they were; larger ones differ only by the cards passed over. The class comment now says which objections a deck still draws in Multipath (the two X-listed sites of the rig's `FellowshipSites`), and `buildDeckWithCopies` says that the deck's Ring-bearer is Frodo 1_290 and that the Ring-bearer and Ring are counted with the draw deck toward copies of a title.

Tested in `virtualtable/DeckFillerTests` (fotr_block clean at 30 to 100 a side; Multipath draws only the two sites; Expanded draws nothing R-listed).

**Not built, from the same lines.** `validateWithModifier(deck, metaSiteId)`: the three deck-building modifiers (92_1, 92_19, 94_22) have all since been rewritten to validate with the card's own overrides, in one line that `pitfalls.md` gives, and no other card of the kind exists. A legal Shadows adventure deck beside `ShadowsSites` (`TestConstants`): only 92_19 needed one and it swaps three sites in, with a comment; making `ShadowsSites` itself legal would change the path under the twelve test files that use it.

### G-0041 Whether the Free Peoples player may give several minions to one character: FreepsCanAssignAgainst(character, minions...) and ShadowCanAssignAgainst(character, minions...), on a count lifted out of the decision's handler (built 2026-10-10, engine pass 24)

**Built (engine pass 24, item defender, with F-0355).** In `virtualtable/Skirmishes` the one-minion readers of G-0029 now take one minion or several: `Boolean FreepsCanAssignAgainst(PhysicalCardImpl character, PhysicalCardImpl... minions)`, its twin `ShadowCanAssignAgainst`, and the neutral `CanBeAssignedAgainst(Side side, character, minions...)` both call (it replaces `CanBeAssignedAgainst(character, minion, side)`, which nothing else called). True when the game would take that group as one answer to the ordinary assignment: the character and each minion are that player's to assign, the Free Peoples player's count holds (one, and one more for each defender +1, the minions the character already has counted), and no restriction on the group forbids it. The count is the engine's own, lifted out of `FreePeoplePlayerAssignsMinionsGameProcess.decisionMade` into `RuleUtils.hasRoomForAssignments`, which the handler now asks too. The Shadow player has no count. With F-0355 mended the one-minion readers no longer answer true for a defender +1 companion who has both his minions.

Used by `mechanics/DefenderTests` (`ACompanionWithoutDefenderIsNotAssignedTwoMinionsByTheFreePeoplesPlayer` and `DefenderPlusOneIsOneAdditionalMinionAndNotAnyNumber`, the two refusals; the `// waits:` comment is gone) and tested itself in `virtualtable/AssignmentReadingTests.SeveralMinionsAgainstOneCharacterAreReadForEitherPlayerAndAgreeWithTheGame`.

**Ketura (2026-10-10):** "Looks good to me".

From the at batch `mech-skirmish`, `mechanics/DefenderTests` (CR 4.2 "defender +X"), replacing
`at/AssignmentAtTest.assignmentsWithDefender`, which showed the refusal by catching the game's exception.

**What the test needed.** Two refusals of the Free Peoples player's own assigning: (1) two minions given to a
companion with no defender, in one answer; (2) three minions given to a companion with defender +1 (one more, not
any number). The game refuses both when the assignment is answered (a `DecisionResultInvalidException`), and no test
may rest on that. The rig reads one character against one minion (`FreepsCanAssignAgainst(character, minion)`,
G-0029) and has nothing for several minions on one character.

**Proposed.** In `virtualtable/Skirmishes`, beside the one-minion reader:
`Boolean FreepsCanAssignAgainst(PhysicalCardImpl character, PhysicalCardImpl... minions)`, true when the game would
accept that group in the Free Peoples player's ordinary assignment. It cannot be built on `isValidAssignments`
alone: the count that refuses (minions in the answer + minions already on the character > 1 + defender) is written
only inside the decision's handler, `FreePeoplePlayerAssignsMinionsGameProcess.decisionMade`, so the engine has to
lift it to where the rig can ask. The existing one-minion readers are no substitute: they answer true for a
defender +1 companion who already has both his minions (F-0355).

**Meanwhile.** `DefenderTests.ACompanionWithDefenderPlusOneIsAssignedTwoMinionsByTheFreePeoplesPlayer` asserts the
two minions accepted and leaves the two refusals out at a `// waits:` comment with this number. A second test,
`ACompanionWhoAlreadyHasAMinionIsOfferedForAnotherOnlyIfHeHasDefender`, shows as much of "a companion without is
not" as a decision can: with one minion on each companion from an assignment action, the Free Peoples player's
assignment lists the defender +1 companion and not the other.

### G-0039 Seating a player under a chosen account name (Race Text 94_5, "the Dark Lord or a World Champion"): a VirtualTableScenario constructor that ends with the two names (built 2026-10-09, engine pass 21)

**Built (engine pass 21, item names), for the mechanic and not for 94_5.** `new VirtualTableScenario(cards, sites, ringBearer, ring, format, freepsMetaSiteId, shadowMetaSiteId, bidHandler, playerPlacements, freepsPlayerName, shadowPlayerName)`: the constructor that takes league placements, with the two names after them, null for the usual name. `P1()` and `P2()` return the names given, and everything the table keys by player goes by them (the decks, each player's cards and meta-site, the seats' channels, the ad hoc effects); placements are keyed by the names given; two seats under one name are refused. All of it is in the test sources (`framework/VirtualTableScenario`, `AdHocEffects`): nothing under `virtualtable/` in the server changed, where a scenario table already seats real names. Its own tests: `virtualtable/NamedSeatTests`.
Used by `mechanics/PlayerIsRequirementTests` (the name check, with made-up names on the tests' own cards 0_8024 to 0_8026). Card_94_005_Tests does not use it, as Ketura ruled on Q-0190 (B): the card's ledger says the two titles are data left to the mechanic test, and the card is no longer parked on this request.

**Ketura (2026-10-09), on Q-0190:** "let's do B, but with the mechanic test coverage": 94_5's own tests do not read the account names; this seating is built for a card-agnostic test of the name check in `mechanics/` (engine pass 21, item names). Nothing more is asked of her here. **Ketura (2026-10-09, evening):** "Oh and G-0039 is good to go too."

From rtmd-b94_5, Race Text 94_5: "If your opponent is the Dark Lord or a World Champion, or if they are placed in the top 10% of the current league, they cannot choose to move during the regroup phase in region 3."

**What the test needs.** To seat a player under a chosen account name. The definition knows the two titles by account name (`PlayerIs` with a list of `names`, asked of this turn's Free Peoples player), and the rig's two players are always `TestConstants.P1` = "Free Peoples Player" and `P2` = "Shadow Player", so no table has an opponent who is either. The tests it would allow: an opponent who is the Dark Lord cannot choose to move in region 3; the same for a World Champion; the modifier's owner seated under either name bars nobody.

**Proposed.** In `VirtualTableScenario`, a constructor beside the one that takes league placements, with two more arguments: `String freepsPlayerName, String shadowPlayerName`, null for the usual name. The names have to reach `P1()` and `P2()`, which today return the constants, and with them everything the rig keys by player (the decks, the meta-sites, the placements).

**Meanwhile.** Card_94_005_Tests shows the top-10% alternative from both seats and the two titles not at all. The ledger has `waits B` under C1, and 94_5 is parked on this request. Whether tests that take the two account names from the definition are wanted at all is asked of Ketura in the question on Race Text 94_5 filed with this batch (rtmd-b94_5): if her answer is no, this request is struck and the card is ready as it stands. Not to be built before that answer.

### G-0040 Reading what a card shows of itself in its card info (Ulaire Nertea 69_38, the named race): GetDisplayableInformation(card) (built 2026-10-09, engine pass 20)

From engine pass 20 (item nertea), Ulaire Nertea, Dark Horseman 69_38, F-0307: "the mentioned race might be buried in the log and it's tedious to make the player scroll through it." The card now shows its named race in its card info, the "Extra information" line the client already draws from a definition's `displayableInformation` (four older cards have one). A test had no way to read that line.

Added to `CardProperties`: `String GetDisplayableInformation(PhysicalCardImpl card)`, a one-line reading of the blueprint's own `getDisplayableInformation(card)`: the definition's text with the card's stored value in place of `{stored}`, or null where the definition has no such line. No twin: it reads a card, not a seat.

Used by `Card_19_038_ErrataTests.TheNamedRaceIsShownOnTheCardAndNamedWhenTheBurdenIsAdded` ("Named race is Hobbit") and by `mechanics/StoredValueInATextTests`.

Built with the item (engine pass 20, nertea). `Card_19_038_ErrataTests.TheNamedRaceIsShownOnTheCardAndNamedWhenTheBurdenIsAdded` uses it.

### G-0037 Whose hand the game reports as revealed, and which cards of a hand another viewer has been sent (Race Text 92_7): FreepsHandIsRevealed / ShadowHandIsRevealed and FreepsSeesCardInHand / ShadowSeesCardInHand, read from a channel the test table keeps for each seat (built 2026-10-09, engine pass 20)

From rtmd-b92_7, Race Text 92_7: "You must play with your hand revealed." (`RevealHand`, `player: owner`.)

**What the tests need.** Two readings the rig has none of.
1. Whose hand the game reports as revealed: the owner's and not the other player's, from either seat; still on turn 2, when the owner has become the Shadow player; nobody's on the same table with no modifier.
2. Which cards of a hand another viewer has actually been sent: the owner's cards in hand and none of the other player's; a card drawn into the revealed hand, as it arrives; and (no old test has it) a card that leaves the revealed hand, played or discarded, taken out of the viewer's sight.

**Proposed.** In `GameProperties`: `boolean FreepsHandIsRevealed()`, `ShadowHandIsRevealed()` and the player-neutral `HandIsRevealed(String player)`, answering from what the game stats tell every client (`GameStats.getRevealedHands()`). In `PileProperties`: `boolean FreepsSeesCardInHand(PhysicalCardImpl card)` and `ShadowSeesCardInHand(card)`: true when that seat has been sent the card as a card in a hand and has not since been sent its removal, read from a communication channel the rig keeps for each seat. It should answer for the other PLAYER's seat, which is what the text is about; the old tests show a spectator only (the game sends to both by the same lines, `GameState.sendStateToPlayer` and `addCardToZone`).

**Meanwhile.** Card_92_007_Tests has ten behaviour tests, written for issue #1095, that read all of this past the rig: `scn.game().getModifiersQuerying().isHandRevealed`, a `GameStats` built by hand, a spectator's `GameCommunicationChannel` attached to the game, and the server's XML through a private `serialize` helper. All ten pass, and none was dropped or changed. Lint refuses the file for them (13 `reach`, 1 `helper`), so **92_7 is not landed**: the file, put in form as far as that goes without the rig (the deck in the scenario methods, a scenario with no modifier, the stats test's Intensity line), stands uncommitted in the working tree, and its ledger carries the two checks as `waits`. When this is built, eight of the ten become rig readings and the card can land.

**Not for the rig.** `RevealedHandFlagIsSerializedOnlyOnTheOwnersPlayerZones` and the second half of `NoHandIsReportedRevealedWithout92_7` read the `handRevealed` attribute that `EventSerializer` writes on `playerZones`. That is the client's protocol and no behaviour of the card. Proposed: when this is built they move, as they are, to a server test beside `EventSerializer`. Say if they are to stay in the card's file instead (lint would then have to let them through).

**Ketura (2026-10-09):** "G-0037: approved", the request as written, the move of the two protocol tests included.

Built in engine pass 20 (9ad45393d). In `GameProperties`: `FreepsHandIsRevealed()`, `ShadowHandIsRevealed()` and `HandIsRevealed(String player)`, whose hand the game stats tell every client is revealed. In `PileProperties`: `FreepsSeesCardInHand(card)`, `ShadowSeesCardInHand(card)` and the player-neutral `PlayerSeesCardInHand(String player, card)` they call: true when that seat has been sent the card as a card in a hand (its own, or one revealed to it) and has not since been sent its removal. Both answer from `SeatChannel` (new, in `virtualtable/`), which holds one of the server's own `GameCommunicationChannel`s for the seat, so that what a seat is sent is the server's decision and not the rig's; it keeps the cards sent as cards in a hand and the last game stats, and nothing else of what a client is sent.

Two things about how it reads, for your information. (1) Each reading asks two channels: the one the table has kept for the seat since before the game's first step (what a client at the table is sent as things happen), and one joined to the seat at that moment and taken off again (what a client who joins or refreshes is sent). If the two differ the reading fails the test and says which was sent what. So one reading covers both roads the old tests covered separately (the three `Spectator...` tests attached their channel late or early as each needed). (2) The channels are kept only by a table that asks for them (`VirtualTable.KeepSeatChannels()`, called from the test table's `BeforeGameStarts()`). A `ScenarioTable` does not ask, and on it the readings fail saying the table keeps no channel: joining a channel under a seat's name drops a person's live connection to that seat (`GameState.addGameStateListener` -> `terminateOldListener`), so this must not happen on a table with a client attached. Nothing a live scenario game does has changed.

Seen while building, and left alone: a hand that BECOMES revealed part of the way through a game is not sent to the clients already at the table (the cards in it are sent only as they arrive, `addCardToZone`), while a client who joins or refreshes is sent all of it. No card does this (92_7's modifier is in force from set-up, and it is the only card with `RevealHand` as a modifier), so no game reaches it; a card that revealed a hand on a condition would. The rig's own test pins it (an ad hoc `RevealHandModifier` in mid-game: the reading is refused, "has not been sent Gimli ... but a client joining that seat now is sent it as one").

Rig tests virtualtable/SeatReadingTests (five). Used by Race Text 92_7, Card_92_007_Tests: every behaviour test is one of these readings now (nine, with two new ones of a card leaving the revealed hand), lint passes, and the card is landed. `RevealedHandFlagIsSerializedOnlyOnTheOwnersPlayerZones` and the XML half of `NoHandIsReportedRevealedWithout92_7` moved as they stood to game/state/EventSerializerTests (the second as `NoHandRevealedFlagIsSerializedWithout92_7`). The old `RevealedHandStillReportedAfterRolesSwap` skipped with `SkipToSite(2)`, which plays both players' turns, and so read on turn 3 with the owner the Free Peoples player again; it now reads on turn 2 (`SkipToOtherPlayersTurn()`), and again on turn 3.

### G-0038 A pack of named cards for a test to open (Race Text 92_29): UseBoosterOf(blueprintIds...) (built 2026-10-09, engine pass 20)

From rtmd-b92_27, Race Text 92_29: "Shadow: Open and reveal a sealed booster pack (limit once per turn). You may play one shadow card revealed, ignoring all costs."

**What the test needs.** To open a pack that holds a Shadow-phase event with a cost in its own text (Threat of the Unknown 1_197, "Shadow: Exert your [moria] Orc to draw 2 cards") and read whether that cost is paid (F-0316, asked of Ketura; whichever way she answers, the answer wants its test). The rig's one fixed pack (`product/Test-Booster.hjson`, through `UseTestBoosters()`) holds eight cards, and its one Shadow-phase event, Foul Things 2_59, has no cost of its own. A real pack's contents are random.

**Proposed.** In `VirtualTableScenario`, beside `UseTestBoosters()`: `void UseBoosterOf(String... blueprintIds)`. The game's pack opener then offers one pack, "Test - Booster", whose contents are exactly those cards in that order. It is a `PackOpener` of two methods set with `_game.setPackOpener`; the lander's probe did it through a subclass (/tmp/rtmd-b92_27-l/ProbeL29.java). This rather than a ninth card in Test-Booster.hjson: 92_29's own tests read that pack as eight cards, and a test that names its pack says on its face what is in it.

**Meanwhile.** Card_92_029_Tests has no test of an event's own cost. The ledger carries the check as `waits` under the clause "You may play one shadow card revealed, ignoring all costs", and the card is parked on this request; its 17 tests are active and pass.

Built in engine pass 20 (9ad45393d). `void UseBoosterOf(String... blueprintIds)` in `VirtualTableScenario`, beside `UseTestBoosters()` (test sources only; the opener is `framework/NamedPackOpener`, beside `TestPackOpener`): the game's pack opener offers one pack, "Test - Booster", whose contents are exactly the cards given, in the order given, every time it is opened; the same blueprint may be given twice; a later call, or `UseTestBoosters()`, takes its place. It fails, leaving the pack opener as it was, when no card is given or an id is no card ("UseBoosterOf was given '99_999', which is not the blueprint id of any card."). Rig tests virtualtable/NamedBoosterTests (four). No card test uses it yet: the test it is for, an event's own cost under "ignoring all costs" (Race Text 92_29, F-0316), is written with that fix (engine pass 20, the item `costs`), and 92_29 stays parked, now on F-0316. pitfalls.md, "The test booster pack", says how it is used.

### G-0036 A site map that gives the game no site 1 to lay (a Shadows-block site under site1 of a numbered path): refused by the test table's constructor, saying why (built 2026-10-09, engine pass 19)

From err-b65_17 (its lander's line in `orchestrator/proposals.md`; engine pass 19, rig). For your information.

**What was met.** The reviewer of Madril 65_64 wanted East Road 11_236 (a Shadows-block site: "Each companion is twilight cost +2") as site 1 of a Fellowship path: `putAll(FellowshipSites); put("site1", "11_236")`. The constructor threw a NullPointerException out of `CheckForCorruptionGameProcess` (`GameState.stopAffecting` on a null card) before any line of the test ran, with nothing to say why.

**Why.** The game lays each site by its printed site number unless the first card of the adventure deck has none (`GameState.usesUnorderedSites`, which goes by the deck, not the format). With a numbered site first and no site printed as site 1, `PlaySiteEffect` finds nothing to lay, both markers are put at site 1 all the same, and the next step that reads the current site falls over. A player's deck never comes to this: deck validation refuses a deck that mixes Shadows-block and Movie-block sites, and wants nine sites with no site number twice, so a numbered path always has its site 1. A test table does not go through deck validation.

**Built** (f34657405). The test table's constructor (`VirtualTableScenario`, test sources only: nothing in `virtualtable/` or the engine changed) refuses such a site map before a game is made, with an IllegalArgumentException as it already does for a site path that has no Map: 'These sites cannot be set up: the game would lay no site 1.  The first card of the adventure deck (Emyn Muil, 1_360) has a printed site number, so the game lays each site by its printed number, and none of these sites is printed as site 1.  The key "site1" holds East Road (11_236), which has no printed site number: such a site cannot stand as site 1 of a numbered path (see "A Shadows-block site path" in the gemp-testing pitfalls).' The same for a numbered path with its site 1 taken out, or with a site of another number under the key. A path the game treats as unordered is let through as before, and so is a Shadows-block site anywhere else on a numbered path.

**The other course**, laying the site as site 1 anyway, was not taken: the game's own set-up does not do that with such a deck, and a table that did would be a board no game can reach. What the reviewer wanted is to be had without it: `putAll(ShadowsSites); put("site1", "11_236")` in the default format and `StartGame(scn.GetFreepsSite("site1"))` start at East Road with its text in force (Aragorn 1_89 costs 6 there).

Rig tests virtualtable/FirstSiteTests (four). No card test was building such a table (it could not have run). pitfalls.md, "A Shadows-block site path", says the new behaviour.

### G-0035 A hand of exactly these cards: SetFreepsHand(cards...) / SetShadowHand(cards...) (built 2026-10-09, engine pass 19)

From err-b60_11, Glimpse of Fate 60_12 (passed up by its lander; engine pass 18's tools item left it for a rig item).

**What the test needed.** A hand of exactly these cards at a point late in a game. `TheLimitRenewsOnTheNextTurn` goes through two `SkipToOtherPlayersTurn()` and must then move all 17 of the scenario's aliases to the discard pile one by one to be left with a hand of exactly four; adding an alias to the scenario breaks the test.

**Proposed.** In `ZoneManipulation`: `void SetFreepsHand(PhysicalCardImpl... cards)` and `void SetShadowHand(PhysicalCardImpl... cards)`: these cards are in that player's hand, and every other card of that hand goes to the top of the draw deck. With its Javadoc and a test of itself, as `changing-the-rig.md` asks.

**Meanwhile.** Not blocking: 60_12's test is active and passes, by moving the 17 cards away. When this is built, that test is shortened to use it.

Built in engine pass 19 (f34657405). `SetFreepsHand(PhysicalCardImpl... cards)`, `SetShadowHand(...)` and the player-neutral `SetHand(String player, ...)` they call, in `ZoneManipulation`: that player's hand is exactly the cards given, in the order given (the first is the front of the hand), taken from wherever they lay; every other card of the hand goes on top of that player's draw deck, the front of the old hand uppermost (as `StartGame()` puts an opening hand back); no cards given empties the hand; the other player's hand is not touched. It fails, moving nothing, when a card given is the other player's ('Goblin Runner, a card of Shadow Player's, cannot be put in the hand of Free Peoples Player: a hand holds only its owner's cards.'). A cheat like the rest of the interface: after it, `FreepsRefreshDecision()` before reading what a pending choice of actions offers (no longer needed since engine pass 40: it refreshes that choice itself, G-0060). Rig tests virtualtable/HandSettingTests (four). Used by Glimpse of Fate 60_12, `TheLimitRenewsOnTheNextTurn` in Card_10_012_ErrataTests: the seventeen aliases moved to the discard pile one by one and the four moved back are now `scn.SetFreepsHand(valor2, filler1, filler2, filler3)`, and an alias added to that scenario no longer breaks the test.

### G-0034 Declining a choice at a decision that is not one answers it in silence: refused, naming the decision that is pending, unless cards are asked for and none need be chosen (built 2026-10-09, engine pass 18)

From engine pass 17, rig (its report, "Looked at and left alone"). `FreepsDeclineChoosing()` / `ShadowDeclineChoosing()` have the fault G-0032 mended in `ChooseCards`: one call too many answers whatever decision comes next with nothing, in silence (a pitfall warns of it). The agent tried G-0032's guard on them and the whole suite stayed OK: no test relies on it. A two-line change: fail, naming the decision that is pending, when it is not a choice that may be declined. For engine pass 18.

Built in engine pass 18 (1dfa2feab). `FreepsDeclineChoosing()` and `ShadowDeclineChoosing()` now fail unless that player is being asked to choose cards (among cards where they lie, CARD_SELECTION, or among cards the game shows, ARBITRARY_CARDS) and the least number the decision takes is 0: 'Free Peoples Player was to decline choosing, but the decision in front of that player is not a choice of cards.  Free Peoples Player is being asked "Play Fellowship action or Pass", offering [Use Bag End].  Shadow Player is not being asked anything.' Seen by probe before the change: at a choice of actions the call passed the phase, at Goblin Scimitar's "you may draw a card" it declined the trigger, at an assignment it assigned nobody and the Shadow player was asked next, each in silence. Where at least one card must be chosen the game already refused the empty answer with a bare DecisionResultInvalidException; that is now said in words as well ('... but at least 1 card must be chosen there.'). Nothing is answered where it fails. Cards that are only shown are still dismissed by it, as before. No new method on the table: the wording is `DecisionGuard.requiredChoiceThatMayBeDeclined`, package-private. Rig tests virtualtable/RigFailureTests, DecliningToChooseWhereTheGameIsNotAskingForCardsIsRefusedSayingWhatIsAskedAndNothingIsAnswered and DecliningToChooseChoosesNoCardWhereNoneNeedBeChosenAndIsRefusedWhereOneMustBe. No card test was relying on the old behaviour (suite OK, 9392 tests; no parked test calls either method), and none had a check waiting on this number; the 88 calls in 51 test files go through the guard from now on.

### G-0032 A card choice answered at a decision that is not a choice of cards: refused, naming the decision that is pending (built 2026-10-08, engine pass 17)

**Ketura (2026-10-08, night):** approved.

From the doc triage of 2026-10-08 (err-b55_7 met it; first line of `orchestrator/proposals.md`). `ChooseCards` in
virtualtable/Choices.java, behind `FreepsChooseCard` / `ShadowChooseCard` / `...ChooseCards`, answers with the
card's id whatever the pending decision is. At a choice of actions the game reads a number as an action's place in
the list (CardActionSelectionDecision.getSelectedAction), so a card choice written where the game had already
chosen by itself takes whichever action has that number, in silence: err-b55_7 saw the archery total change. A
rig defect that G-0030's guards do not cover. Proposed: fail, in G-0030's manner, when the pending decision is not
a choice of cards, naming the decision that is in fact pending. For engine pass 17.

Built in engine pass 17 (63ba19283). What it was doing, found by running it: `FreepsChooseCard` went by whether the pending decision listed blueprints, and a choice of actions lists them too, so there the card matched none and the answer was an empty one, a pass, of the phase or of an optional trigger (this is what err-b55_7 saw: the fellowship phase had been passed). With none listed the card's id went in as a number: the number chosen at a choice of a number, an assignment of nobody at an assignment, and an action's place in the list only through `ChooseCards(player, ...)` called by itself. Now `FreepsChooseCard`, `FreepsChooseCards`, `ChooseCards`, `ChooseCardBPFromSelection` (both), `ChooseCardIDFromSelection` and their Shadow twins fail unless the player is being asked to choose cards in the way the answer fits (cards where they lie, by id: CARD_SELECTION; cards the game shows, by blueprint: ARBITRARY_CARDS): 'Free Peoples Player was to choose Aragorn, Ranger of the North, but the decision in front of that player is not a choice of cards.  Free Peoples Player is being asked "Play Fellowship action or Pass", offering [Use Bag End].  Shadow Player is not being asked anything.' Nothing is answered. No new method on the table: the wording is `DecisionGuard.requiredCardChoice`, package-private. Rig tests virtualtable/RigFailureTests, ACardChosenWhereTheGameIsNotAskingForCardsIsRefusedSayingWhatIsAskedAndNothingIsAnswered and EachKindOfCardChoiceTakesACardChosenAndRefusesTheAnswerThatFitsTheOtherKind. No card test calls it by name; seven were passing by the accident and are put right (1_338, 51_107, 57_332, V2_8, 92_10, 92_13 twice: `orchestrator/engine-pass-17/rig.md` has each). `FreepsDeclineChoosing()` is left as it was, unchecked; a guard on it was tried and fails no test in the suite, if it is wanted.

### G-0033 Reading the game log for words, where the line names no card: GameLogSinceMarkSays(text) (built 2026-10-08, engine pass 17)

For your information, as any rig addition is since 2026-10-06. Q-0144 has a reveal from a hand with no card to
show tell the players by a line in the game log and by nothing else ("There were no cards in <player>'s hand for
<card> to reveal"). G-0019's `GameLogSinceMarkMentions(cards...)` asks after cards, and that line names only the
card that was played, which the line announcing the play names too: nothing in the rig could tell whether the
line was there.

Built in engine pass 17 (2dda26a78): `boolean GameLogSinceMarkSays(String text)` in virtualtable/GameProperties.java,
true when some one message sent since `MarkGameLog()` contains the words, whatever their capitals; it fails the
test, as the other two readers do, when no mark was set or no words are given. No state of its own. Rig test
virtualtable/GameLogReadingTests (TheLogIsReadForWordsWhateverTheirCapitalsAndOnlySinceTheMark). Used by Foul
Creation 51_44 and Curse Their Foul Feet! 51_36 (AgainstAnEmptyHandItMayStillBePlayedAndOnlyItsCostIsPaid in each)
and by the mechanic test mechanics/RevealingNoCardsFromAHandTests. Pitfall "A card made known by the game log
alone" says how to use it.

### G-0031 Telling the fierce round from the first: InFierceRound() (built 2026-10-08, engine pass 16)

From err-b51_172, err-b51_198, err-b52_75 and err-b56_22. After a skirmish that leaves a fierce minion alive,
`GetCurrentPhase()` is `Phase.ASSIGNMENT` again and `AwaitingFreepsAssignmentPhaseActions()` is true: nothing reads
"this is the fierce round", and lint does not take `assertEquals(Phase.X, scn.GetCurrentPhase())` as a test's closing
check. A "this phase" effect from the first assignment phase ends before the fierce one, which a test of its duration
has to show. Proposed: a reader beside the phase checks (`InFierceRound()`, name open) if the game keeps that state
where the rig can read it; if it does not, a pitfall entry and nothing more. Low priority.

Built in engine pass 16 (91ec5ac5c): the game keeps the state itself (`GameState.isFierceSkirmishes()`, set as it announces "Fierce skirmishes" and cleared when the last fierce skirmish is over), so `InFierceRound()` in virtualtable/GameProperties.java is a one-line reading and the rig holds no state of its own. Rig test virtualtable/AssignmentReadingTests (TheFierceRoundIsToldFromTheFirst...). Used by Card_02_075_ErrataTests (52_75), WhenTheLastHobbitDiesTheFreePeoplesPlayerMayAssignToBill. Pitfall entry "The fierce round reads as the first does".

### G-0030 A failure that says why, where the rig throws a NullPointerException: guards where the rig answers and where it reads a parameter (built 2026-10-08, engine pass 16)

Reported fourteen times between v1-b59 and err-b57_82, each time as a lost step, and never filed because no card
test is blocked by it. Three places:

- An answer given for a player who has no decision pending dies in `PlayerDecided` with "decision is null":
  `FreepsAssignToMinions` after the passes have fallen out of step (a `FreepsPass()` made to read a Shadow action,
  then `SkipToAssignments()`), a second `FreepsAssignToMinions` call for a second companion, `FreepsChooseToMove()`
  before the Shadow player's reconciliation, `FreepsResolveSkirmish` while a trigger, a repeated response or an
  ambush offer is pending for the other player.
- `FreepsPlayCard(card)` / `FreepsUseCardAction(card)` on a card the pending decision does not offer dies in
  `CardActionSelectionDecision.getSelectedAction` ('"result" is null').
- `FreepsGetChoiceMin()` / `FreepsGetChoiceMax()` / `FreepsGetSelectableCount()` on a decision that has no such
  parameter, and `FreepsGetAvailableActions()` with no decision, throw from the parameter readers (G-0027 is one
  case of this).

Proposed: one guard where the rig answers and one where it reads a parameter, each failing with a message that names
the player, what was attempted, and what each player is in fact being asked (the decision's text), or that the card
is not among the actions on offer (listing them). No new method. G-0015 (passes stay harmless) is untouched.

Built in engine pass 16 (91ec5ac5c). No new method on the table: the wording is in a package-private class, virtualtable/DecisionGuard.java. Where the rig answers: `PlayerDecided` refuses an answer for a player who is not being asked anything, and a null answer, before it touches anything (it used to drop the pending decision and then die, so a test that went on had lost the decision); `FreepsPlayCard`, `FreepsUseCardAction(card)`, `FreepsTransferCard`, `FreepsDiscardToHeal` and their Shadow twins refuse by the card's name, listing what is on offer. Where it reads a parameter: `FreepsGetChoiceMin/Max`, `FreepsGetSelectableCount`, `FreepsGetFirstADParam`, `GetADParamEqualsCount`, `ChooseAction` (so `FreepsChooseYes` and its kin), `ChooseCardBPFromSelection` and `ChooseCardIDFromSelection`. Each message names the player, what was attempted and what each player is being asked: 'Shadow Player was to answer "45 47", but is not being asked anything.  Free Peoples Player is being asked "Play Fellowship action or Pass", offering [Use Bag End].' `GetAvailableActions` returns an empty list for a decision that is not a choice of actions, and `GetCardActionId` null, as their Javadoc already said. Passes stay harmless (G-0015). Rig tests virtualtable/RigFailureTests, one for each of the three cases. No card test calls it by name; every test that goes wrong does.

### G-0029 Whether one character may be assigned to one minion: FreepsCanAssignAgainst and ShadowCanAssignAgainst (built 2026-10-08, engine pass 16)

From err-b52_75 (52_75). `FreepsCanAssign` and `FreepsHasAssignmentTargetAvailable` do not see a restriction on a
pair (`cantBeAssignedToSkirmishAgainst`): the game checks it only when the assignment is answered. Today the test
wraps `FreepsAssignToMinions` in `assertThrows`. Proposed: `FreepsCanAssignAgainst(character, minion)` and a Shadow
twin in virtualtable/Skirmishes.java, reading `Filters.assignableToSkirmishAgainst` / `isValidAssignments` for the
one pair; then the 52_75 test reads it and the `assertThrows` goes. For the next rig pass.

Built in engine pass 16 (91ec5ac5c): `FreepsCanAssignAgainst(character, minion)`, `ShadowCanAssignAgainst(character, minion)` and `CanBeAssignedAgainst(character, minion, side)` in virtualtable/Skirmishes.java: each of the two may be assigned by that side as the game lists them for the ordinary assignment, and `isValidAssignments` passes the one pair. Rig test virtualtable/AssignmentReadingTests, on The Balrog 6_76 ("cannot be assigned to skirmish companions or allies of strength less than 6"), where the game is also shown refusing the pair that reads false and taking the one that reads true. Used by Card_02_075_ErrataTests (52_75) in its three tests of the restriction, where the seven `assertThrows` stood; the card is parked no longer. Card_19_038_ErrataTests (Úlairë Nertëa, not yet reached by the campaign) still shows the same thing with a try/catch.

### G-0028 Running an effect whose outcome may be prevented: ExecuteAdHocEffectUnchecked, with its Freeps and Shadow twins (built 2026-10-08, engine pass 16)

(Filed as G-0020 on 2026-10-08 and renumbered the same day: G-0020 was already the seating of decks with Maps, built 2026-10-06.)

From err-b54_97 (Long I Fell 54_97): `ExecuteAdHocEffect` asserts `effect.wasCarriedOut()`, so a wound that a
prevention stops fails the assert, and a test cannot show Gandalf protected while he is not in the skirmish the event
was played in. Proposed: `ExecuteAdHocEffectUnchecked(String playerId, Effect effect)` in AdHocEffects, which does not
assert. Low priority; the wrong definition that survives meanwhile is choose(title(Gandalf),inSkirmish).

Built in engine pass 16 (91ec5ac5c): `FreepsExecuteAdHocEffectUnchecked(effect)`, `ShadowExecuteAdHocEffectUnchecked(effect)` and `ExecuteAdHocEffectUnchecked(playerId, effect)` in framework/AdHocEffects.java run the effect without asserting that it was carried out. Two things were put right in the runner that both variants share: the ad hoc effect is now the action taken whatever else the player's choice offers (it was answered with "0", which takes the first action on offer: at Bag End that was Bag End's own, and nothing said so), and a player who is not at a choice of actions is refused with a message. `ExecuteAdHocEffect`'s own assertion now says what failed and names the unchecked method. Rig tests virtualtable/AdHocEffectTests. Used by Card_04_097_ErrataTests (54_97), GandalfIsProtectedWhileHeStandsOutsideTheSkirmishItIsPlayedIn, which mutate.py shows to be the one test that notices choose(title(Gandalf),inSkirmish); the card is parked no longer.

### G-0027 Reading the maximum of a number choice that has none: FreepsGetChoiceMax() reads Integer.MAX_VALUE there (built 2026-10-08, engine pass 16)

(Filed as G-0019 on 2026-10-08 and renumbered the same day: G-0019 was already the game-log reader, built 2026-10-06.)

From fix-rulings-1008-tests (52_1, Beneath the Mountains): `FreepsGetChoiceMax()` / `ShadowGetChoiceMax()` in
virtualtable/Choices.java throw a NullPointerException when an integer decision has no maximum, because
IntegerAwaitingDecision then sets no "max" parameter. Proposed: return Integer.MAX_VALUE there, or add
`FreepsChoiceHasMax()` / `ShadowChoiceHasMax()`. Today "no cap" on a chooseANumber with no `to` is shown only by the
game accepting an answer larger than the deck. For the next rig pass.

Built in engine pass 16 (91ec5ac5c): `FreepsGetChoiceMax()` / `ShadowGetChoiceMax()` return `Integer.MAX_VALUE` where the player is choosing a number and the decision names no maximum. No new method. Rig test virtualtable/RigFailureTests (TheMaximumOfANumberWithNoUpperBound...), on Gathering Wind 10_16 beside Forces of Mordor 1_248, whose number is bounded. Used by Card_02_001_ErrataTests (52_1), DwarfAbilityDiscardsAsManyCardsAsThereAre..., which now reads that there is no cap.

### G-0026 The rig's turn-skippers answer "discard down to 8" one card at a time (built 2026-10-07, engine pass 11)

Met on Desolation of Ash 103_122. `SkipToMovementDecision`, `SkipToOtherPlayersTurn` and `SkipCurrentSite`
(GameProcedures) answer a reconcile's "discard down" with one card; when the hand is 10 or more the decision
wants several cards in one answer and the helper throws. Whether it happens depends on what earlier reconciles
drew, so a test that adds cards to a hand can fail one run in three. To build in the next engine pass: choose
the decision's minimum number of cards in one answer.

Built in engine pass 11 (43413d5dd): every reconcile in the three turn-skippers goes through `PassReconcile(player)`, which answers with the decision's minimum number of cards at once. Rig tests in virtualtable/ReconcileSkippingTests.

### G-0025 FreepsUseCardAction(card) takes the first action listed for the card, whatever its verb (built 2026-10-07, engine pass 11)

Met on Treasured Pipe 103_119. A possession borne by a companion lists "Transfer <name>" before "Use <name>"
whenever another companion could bear it, so `FreepsUseCardAction(card)` silently transfers the card and the
test's next answer goes to the wrong decision (the symptom is a NullPointerException "decision is null").
`FreepsActionAvailable(card)` already goes by the verb "Use". To build in the next engine pass: the one-argument
`FreepsUseCardAction` / `ShadowUseCardAction` prefer the action whose text begins "Use". Until then tests write
`FreepsUseCardAction(card, "Use")`, as Card_V3_119_Tests does.

Built in engine pass 11 (43413d5dd): the one-argument `FreepsUseCardAction` / `ShadowUseCardAction` take the action `CardActionAvailable(..., "Use")` recognises and fall back to the first listed. New rig methods `FindCardActionIndex` and `GetCardUseActionId`; rig tests in virtualtable/ActionAvailabilityTests.

### G-0024 Asking the pending decision again after the rig has changed the board (built 2026-10-07, engine pass 7, 41ed4433a)

Asked for by the author of 103_62 and 103_64: `ShadowRefreshDecision()` / `FreepsRefreshDecision()`, which
re-send the player's current choice of actions with its offers worked out afresh. Use at hand: a "not offered"
test cannot show the positive on the same board where nothing either player can legally do brings that player
a second decision (the assignment phase: two passes end it; in the maneuver phase another card's ability has to
be used as a keep-alive). Low priority: both cards' tests stand without it. To be built with the next rig or
engine pass, as information under the standing rule (a real use exists).

Built: `FreepsRefreshDecision()` / `ShadowRefreshDecision()` work the phase actions out afresh and send the same decision again; they fail unless that player is at a choice of phase actions. Rig test virtualtable/DecisionRefreshTests.


### G-0017 Taking control of a site for the player who is not the Shadow player this turn

`ShadowTakeControlOfSite()` exists; there is no way to give the other player a controlled site short of an ad
hoc effect that only works while his pending decision offers no action of his own (the test of Trail of
Savagery, 102_31, empties his deck first to get there, with a `lint-ok` line). A site the other player controls
is the invalid candidate for every "site you control". Proposed: `TakeControlOfSite(String player)` in
`ZoneManipulation`, setting control directly with no decision, and `FreepsTakeControlOfSite()` /
`ShadowTakeControlOfSite()` as its two callers. Wants your word, as any rig addition does.

Raised again by the authors of 102_27 and 102_34 (v2-b26, v2-b32), with a fault in the existing method:
`ShadowTakeControlOfSite()` adds its action to the pending decision and answers "0", so when the Shadow player
has any real action on offer it takes that action instead (a probe: it played an Uruk Savage from hand) and
then throws "Effect has to be played first"; no site is taken. Three cards now work round it with Down to the
Last Child (4_148) and cheated tokens. The direct setter would replace it and should fail with a message of
its own if the site is not taken.

Raised a third time by the reviewer of 102_72 (v2-b71): with no decision pending for the Shadow player (any
fellowship phase) `ShadowTakeControlOfSite()` throws a NullPointerException. Asked for with a site argument:
`TakeControlOfSite(String playerId, PhysicalCardImpl site)`, with `FreepsTakeControlOfSite(site)` and
`ShadowTakeControlOfSite(site)` on top, usable at any time like the other cheats.

**Built 2026-10-06 (fb4c74608).** `FreepsTakeControlOfSite()`, `ShadowTakeControlOfSite()` (the first site that can be controlled), the same two with a site argument, and the player-neutral `TakeControlOfSite(player[, site])`, in ZoneManipulation: a direct cheat, no decision answered, usable at any time; the named-site form refuses a site at or ahead of a player's marker. The old ad hoc method is gone. Workarounds converted in the tests of 4_199, 4_201, 17_148, 92_3, 102_27, 102_31, 102_34, 102_70, 102_72; left where control changing by legal play is what the test shows (one test each in 102_27, 102_31, 102_34).

### G-0022 Optional trigger methods by card and words

Not blocking. When one card offers an optional trigger and a response in the same "Optional responses"
decision (Ethereal Banner 103_21: its win trigger and its overwhelm response),
`FreepsHasOptionalTriggerAvailable(card)` and `FreepsChooseOptionalTrigger(card)` cannot say which is meant; the
tests fall back on the game's wording alone. Proposed, beside the by-card methods:
`FreepsHasOptionalTriggerAvailable(card, text)` and `FreepsChooseOptionalTrigger(card, text)`, with Shadow twins,
matching card and words as `FreepsUseCardAction(card, text)` does for phase actions.

**Built 2026-10-06 (fb4c74608).** `FreepsHasOptionalTriggerAvailable(card, text)`, `FreepsChooseOptionalTrigger(card, text)`, Shadow twins and player-neutral forms. Used in the three response tests of Ethereal Banner 103_21.

### G-0023 `FreepsResolveRuleFirst()` picks the first entry, which is not always the rule (a rig defect)

Met on King of the Dead, Dead Keeper (103_27). When he is killed with threats on the table the ordering decision
lists `["Required trigger from King of the Dead, Dead Keeper", null]` with blueprintId `[103_27, rules]`: the
rule is second and has no text. `FreepsResolveRuleFirst()` takes index 0, here the card's trigger, though its
Javadoc says it resolves the rule. The old test of this card called it and in fact resolved the King's trigger
first. `FreepsResolveActionOrder(null)` throws a NullPointerException unless the null entry is listed first.
Proposed: `FreepsResolveRuleFirst()` chooses the entry whose blueprintId is "rules" and fails the test if there
is none; `ChooseAction(player, name, null)` skips non-null choices. Any existing test that calls
`FreepsResolveRuleFirst()` where the rule is not listed first would then resolve in the other order and may need
its readings looked at. Holds one untested order on 103_27 (threat wounds placed before the King's healing; it
plays correctly in a probe).

**Fixed 2026-10-06 (fb4c74608).** `FreepsResolveRuleFirst()` picks the entry whose blueprintId is "rules" and fails when there is none; `ChooseAction` passes over entries with no wording. All 52 existing calls met the rule listed first, so no test changed order. King of the Dead 103_27 has its second order tested. No Shadow twin was added.


### G-0019 Reading the game log

You ruled on Q-0024 and Q-0025 that a card taken from a draw deck by type, or revealed from a hand, is made
known by the line in the game log alone. Nothing in the rig reads the log, so a definition with `reveal: true`
turned to false on such a taking (102_22, 102_42, 102_43) is noticed by no test, and neither would a log line
that stopped naming the cards. Proposed, in `GameProperties`: `boolean GameLogMentions(PhysicalCardImpl...
cards)` over the messages sent since a mark (`MarkGameLog()`), or plainly `List<String> GetGameLog()`. Wants
your word, as any rig addition does.

**Built 2026-10-06 (5c294fead).** `MarkGameLog()`, `GameLogSinceMarkMentions(cards...)`, `GetGameLogSinceMark()` in GameProperties (a card is matched by its link in the message; reading with no mark set fails the test). Used in 102_22, 102_42, 102_43 (the cards taken from the deck are named, one left behind is not; `reveal: true` made false is now noticed) and 53_87 (the randomly revealed card is named though no pop-up is shown).

### G-0020 Seating a deck that has a Map, and naming each player's Map

Holds one check on each of the three V2 Maps (102_74, 102_75, 102_76): "Before the game begins, place this Map
in your support area" has no test. Today `new VirtualTableScenario(cards, sites, frodo, ring,
VirtualTableScenario.PCMovie)` throws a NullPointerException ("Cannot invoke String.endsWith because
blueprintId is null"): the rig's decks have no Map and pc_movie wants one, so no pc_movie table can be built
at all. Proposed, in framework/VirtualTableScenario.java: a constructor taking `freepsMapId` and `shadowMapId`
after the format; and where the format uses Maps and none is named, the Map of the site map's path, so that
PCMovie works with the existing constructors. In virtualtable/GameProperties.java, beside `GetRing()`:
`GetFreepsMap()` and `GetShadowMap()`. The zone can then be read with the existing
`assertInZone(Zone.SUPPORT, card)`. To give the two seats different Maps the two decks need different site
maps, which the constructors cannot do today; with one Map for both, each copy is read in its own owner's
support area.

**Built 2026-10-06 (5c294fead).** PCMovie works with every constructor (each deck gets the Map of its site path); a constructor names each player's Map; `GetFreepsMap()` / `GetShadowMap()`. Used in the three Map files (BeforeTheGameBeginsEachPlayersMapIsInItsOwnersSupportArea), which un-parks 102_74, 102_75, 102_76.

### G-0021 DeckValidationScenario: decks the PC formats accept, and reading the Map's objection

Restated 2026-10-06 after "I need a more clear proposal here": these are three separate small helpers, not one
feature. (a) and (b) have a use today in the three Map tests and are built with G-0020; (c) has none and is
dropped until a test needs it.

Lesser, and holds nothing. (a) Filler cards the PC formats accept: every pc_movie deck the scenario builds
carries 12 or 13 "Non-errata version of an errata'd card" errors (Armor, Band of the Eye, Desperate Measures,
Ettenmoors), so "passes" can only mean "the Map does not object". (b) `List<String> getMapErrorSites(errors)`:
the sites the Map's objection names, so a test can read that the stray site is named and the valid ones are
not. (c) `boolean hasMissingMapError(errors)` for "Deck doesn't have a Map", for a mechanic test of usesMaps.

**Built 2026-10-06 (5c294fead): (a) and (b).** DeckValidationScenario fills decks with the cards as the format plays them, so a valid pc_movie deck has no errors; `getMapErrorSites(errors)` names the sites a Map objects to. Both used in the three Map files. (c) not built: no use.


### G-0001 Availability checks go by the physical card (F-0011): ca6a41f04

`FreepsActionAvailable(card)`, `FreepsPlayAvailable(card)`, `FreepsTransferAvailable(card)` and their Shadow twins
compare the physical card each offered action belongs to. `CardActionAvailable(player, card, verb)` is the
player-neutral method they call. Actions of cards outside play (the discard pile, the draw deck, the hand) carry
their card too and are matched the same way; a decision that names only blueprints is matched by blueprint.
No existing test depended on the old behaviour: the whole suite passed unchanged.

### G-0002 Checks answer false when nothing is being asked (F-0012): ca6a41f04

`GetAwaitingDecisionParam`, `GetAwaitingDecisionParams` and `GetADParam` return null when the player has no
pending decision, so `HasCardChoiceAvailable`, `HasBPChoiceAvailable` and the availability checks answer false.

### G-0004 A cheat for allies that says what it does: c46d61f37

`MoveAlliesToTable(PhysicalCardImpl...)` and `MoveAlliesToTable(String...)` put allies in the support area.
`MoveCompanionsToTable` now verifies each card is a companion, and `MoveAlliesToTable` that each is an ally.
Ketura agreed both on 2026-10-05. 82 call sites in 39 test files passed something else to
`MoveCompanionsToTable`: 73 allies, and nine minions (in the live tests of 101_37, 101_53, 1_172 and its erratum,
and in unused skeleton tests of four more files). One assertion that expected an ally in the companions' zone was
corrected (101_15). Every test passes as before.

### G-0003 Card-choice checks and two copies listed by blueprint: closed, nothing to do

Ketura, 2026-10-05: a limitation of the engine, which cannot distinguish two blueprint ids of non-physical cards.
Authors give the candidates in such a choice different blueprints.

### G-0005 Shadow twins for ordering simultaneous required responses: not wanted

Ketura, 2026-10-05: by rule the Free Peoples player is the tiebreaker on the timing of simultaneous actions, so
only that player is ever asked. Shadow twins would be invalid.

### G-0006 `ShadowCanAssign` asked about the wrong side: b404fedb3

It was `CanBeAssignedViaAction(card, Side.FREE_PEOPLE)`, Free Peoples logic left behind in a copied twin. It is
now `CanBeAssignedNormally(card, Side.SHADOW)`, the counterpart of `FreepsCanAssign`. No test called it.
Ketura agreed on 2026-10-05.

### G-0009 The by-card availability checks see an ability with its own wording: b404fedb3

`FreepsActionAvailable(card)` and its Shadow twin answer for any action of that card, among the actions of a
phase, that is not a play or a transfer of it, so an ability whose definition words it itself is seen. One test
that leaned on the blind spot (101_11) was corrected. The second half of the request, an overload taking the
card and the wording together, is still open under G-0010.

### G-0007, G-0008, G-0010, G-0011, and the rest of G-0009: 0b49e9899

All agreed by Ketura on 2026-10-05 and added together, each with a test of its own:
`FreepsHasAssignmentTargetAvailable(card)` and its Shadow twin (G-0007); `FreepsHasOptionalTriggerAvailable(card)`
and its twin now go by the card and not by its name in the prompt (G-0008); `FreepsActionAvailable(card, text)`
(G-0009) and `FreepsUseCardAction(card, text)` (G-0010), with twins, for a card that offers several actions;
`GetFreepsDeckShuffleCount()` and `GetShadowDeckShuffleCount()` (G-0011), which count in `GameState`.

### G-0012, G-0013, G-0014: done 2026-10-05

Agreed by Ketura and added together, with three tests in `DecisionReadingTests`; the whole suite (7,098 tests)
passes with them.

- G-0012: `FreepsChooseOptionalTrigger(card)` and its Shadow twin match the card exactly, and fail the test
  when that card is not offering.
- G-0013: `FreepsDismissRevealedCards()`, `ShadowDismissRevealedCards()` and `DismissRevealedCards()` fail the
  test when nobody they answer for is being shown cards. `FreepsGetTotalRevealedCardCount()` counts the cards
  being shown (it counted selectable ones, so read 0), and `ShadowGetGetTotalRevealedCardCount` is renamed
  `ShadowGetTotalRevealedCardCount`. Fifteen existing tests in five files had a dismissal of nothing: three
  on reveals from a hand (Q-0024), two on Teach Me Your Herb-lore (103_90, where the deck is not shown a second
  time once no pipe was played), ten on Race Text 92_29 (F-0039: the pack is never shown to the opponent).
- G-0014: `FreepsResolveRuleFirst()` and `FreepsResolveActionOrder(text)` fail the test unless the Free Peoples
  player is being asked to order required responses; `FreepsAwaitingRequiredResponseOrder()` reads that. No
  Shadow twin: only the Free Peoples player is ever asked. No existing test broke.

### G-0015 The pass methods do nothing, silently, when that player has nothing to answer: declined

`FreepsPassCurrentPhaseAction()` and `ShadowPassCurrentPhaseAction()` return without complaint when the player
has no decision pending. A stray pass in a test therefore reads as a decision that was there and was answered;
two tests in batch v1-b43 had one, found only because the reviewer probed the board. Proposed: Verify that a
decision is pending. This is likely to break more tests than G-0014 (the skip methods and many old tests lean
on a pass being harmless), so it wants your word first, and perhaps a tolerant twin (`...PassIfAsked()`) for
the skip methods to use.

**Ketura, 2026-10-05:** "Let's leave passes harmless for now." Tests use `FreepsPass()` / `ShadowPass()` in place
of the wordier `FreepsPassCurrentPhaseAction()` / `ShadowPassCurrentPhaseAction()` (renamed across the test
files in ef708d4b6).

### G-0016 A token of a named culture on a card: declined

`AddTokensToCard(card, count)` adds tokens of the card's own culture only. Ritual Oath of Enmity (102_5) says
"Spot a [dunland] token here", and a definition that spots a token of any culture passes every test, because
no board can put another culture's token on it. Proposed, beside the existing three:
`AddTokensToCard(card, Culture, count)`, `GetCultureTokensOn(card, Culture)`,
`RemoveTokensFromCard(card, Culture, count)`. Low priority: with today's cards the mistake makes no difference
to play. Bears on every card with "a [culture] token here" in a requirement.

**Ketura, 2026-10-06:** "Declined. There are no cards that put more than one kind of culture token on another,
and no plans to in the PC, so for now that remains unneeded."

### G-0018 The guard on ordering required responses refuses the "is about to" wording: done 2026-10-06 (6f42274cd)

`FreepsResolveActionOrder(text)` and `FreepsResolveRuleFirst()` were made to check that the ordering decision is
pending (G-0014). That check looks for "Required responses"; when two required "is about to" triggers answer
one event the game words the decision `<effect> - Required "is about to" responses`, and the methods refuse
it. Found on two copies of Uruk Shield Wall (102_35) answering one wound. A defect in G-0014's guard, not a new
ability of the rig: `FreepsAwaitingRequiredResponseOrder()` and its Shadow twin are to be true for both
wordings. Fixed: the guard is true for exactly those two wordings; a case in DecisionReadingTests shows the second.
