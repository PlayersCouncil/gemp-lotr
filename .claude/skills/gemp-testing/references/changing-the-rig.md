# Changing the rig

The interfaces under `gemp-lotr-server/src/main/java/com/gempukku/lotro/virtualtable/` are part of the server.
`ScenarioTable` extends `VirtualTable`, so every method added there is also a method of a live tutorial table
with a person connected to it. These rules come from the scenario system's side and are not negotiable from the
test side.

## What the code must be

- **No JUnit and nothing test-scoped.** A sanity check inside the rig is `Verify(condition, message)`.
  `VirtualTableScenario` turns that into a test failure; everywhere else it throws `IllegalStateException`.
- **`P1()` and `P2()`, never the `P1` and `P2` constants.** The constants, `TestConstants`, `AdHocEffects` and
  `Assertions` stay in the test sources; a scenario table seats real player names.
- **No fields in the interfaces.** State goes on `VirtualTable`, behind an accessor declared in `TableBase`.
- **No name that `ScenarioTable` already uses with a different return type**: `AliasOf`, `AliasOfCardId`,
  `AnswersFollowed`, `Apply`, `Arbitrate`, `Arranged`, `AskToPass`, `Begin`, `Card`, `CardsIn`, `Decks`,
  `Definition`, `Follow`, `FollowProblem`, `FollowedAnything`, `Holds`, `HumanPlayer`, `IsOpening`, `Journal`,
  `Observe`, `OnNote`, `OpponentPlayer`, `Player`, `Prepare`, `Problems`, `Random`, `SeatOf`, `Start`.

## What the code must do

- **Answer decisions only through `PlayerDecided`** (or `FreepsDecided` / `ShadowDecided`, which call it). The
  journal, checkpoints, the scenario director's rulings and the live step display all hang off the
  `DefaultUserFeedback` hooks it calls. A helper that calls `decision.decisionMade` itself goes round them.
- **Change the board through `GameState`'s own methods**, as `ZoneManipulation` does. A scenario step's `do:` in a
  live game is those same calls, so they have to behave on a table with a client attached.
- **No randomness of its own.** There is none in the package, and replay determinism depends on everything going
  through the game's `GameRandom`.
- **Synchronous: no locks, no waiting.** A live scenario calls these methods inside the mediator's game lock.

## What is safe

Adding a method is safe. Changing the signature or the meaning of one the scenario code calls is what breaks it.
The scenario code calls these:

- hooks: `CreateGame`, `GameName`, `BeforeGameStarts`, and the `Cards` map
- decisions: `PlayerDecided`, `FreepsDecided`, `ShadowDecided`, `FreepsAnyDecisionsAvailable`,
  `ShadowAnyDecisionsAvailable`, `GameIsFinished`
- reads: `GetCurrentPhase`, `GetCurrentSiteNumber`, `GetTwilight`, `GetBurdens`, `GetThreats`, `GetStrength`,
  `GetVitality`, `GetWoundsOn`, `GetTwilightCost`
- board changes: `MoveCardToZone`, `MoveCardsToTopOfDeck`, `MoveCardsToBottomOfDeck`, `AttachCardsTo`,
  `StackCardsOn`, `AddWoundsToChar`, `RemoveWoundsFromChar`, `AddTokensToCard`, `AddBurdens`, `RemoveBurdens`,
  `AddThreats`, `SetTwilight`

The procedures that play both seats (`StartGame`, `SkipMulligans`, the phase skippers) are not called from
scenario code, so more of those are no concern to it.

## How a change is made

1. Say what a test needed and could not do, and which interface it belongs in: reading a card goes in
   `CardProperties`, reading the game in `GameProperties`, a decision's contents in `Choices` or `Decisions`,
   moving the game along in `GameProcedures`, cheating in `ZoneManipulation`, combat in `Skirmishes`. Something
   that only makes sense with two made-up players and no client belongs in the test sources instead.
2. Propose the signature, with Javadoc. A Freeps method gets its Shadow twin and a player-neutral method that
   both call.
3. Get it agreed before writing it. The rig is shared ground and the scenario work is going on at the same time.
4. Add it with its Javadoc, add a test of the method itself if it is more than a one-line reading, run
   `cardtest.py setup` and then `suite all` (the scenario tests exercise the same classes). The rig's own tests
   are in `gemp-lotr-server/src/test/java/com/gempukku/lotro/virtualtable/`, the test-side mirror of the rig's
   package; they use plain Decipher cards whose behaviour is not in question.
5. Run `gen_vts_api.py` so the reference picks it up, and commit the rig change on its own, apart from any card.
