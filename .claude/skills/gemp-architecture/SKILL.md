---
name: gemp-architecture
description: >
  Gemp-LotR codebase architecture reference. Module structure, HJSON ingestion pipeline, game engine
  internals, HTTP API layer, format system, and extension points for adding new mechanics.
  Use this skill when navigating the codebase, adding new game mechanics, modifying server behavior,
  debugging card behavior, or understanding how components connect. Trigger when: the user asks about
  code architecture, module dependencies, where to add new effects/modifiers/filters, how the game
  loop works, how cards are loaded, API endpoints, format definitions, or any structural question
  about the Gemp codebase.
user-invocable: false
---

# Gemp-LotR Architecture Reference

Focused on what you need to know when adding new mechanics, card effects, or server features.

---

## Module Structure

```
gemp-lotr (parent POM)
├── gemp-lotr-common       → Enums, config, shared types (Culture, Race, Phase, Zone, CardType, etc.)
├── gemp-lotr-logic        → Game engine, card blueprint system, action/effect/modifier framework
├── gemp-lotr-cards        → HJSON card definitions only (no Java sources)
├── gemp-lotr-server       → Game mediator, hall, leagues, DB, formats, deck validation
├── gemp-lotr-async        → Netty HTTP server, request handlers, frontend assets
└── gemp-lotr-images       → Card image generation (standalone)
```

**Dependency chain:** `async → server → cards → logic → common`

| Module | You modify this when... |
|--------|------------------------|
| **common** | Adding a new enum value (keyword, culture, race, phase, zone) |
| **logic** | Adding new effect types, modifiers, filters, game mechanics, action processors |
| **cards** | Creating/editing card definitions (HJSON files) |
| **server** | Changing formats, deck validation, league rules, hall behavior, DB schema |
| **async** | Adding/modifying API endpoints, frontend JavaScript |

---

## Application Startup

**Entry point:** `LotroAsyncServer.main()` in gemp-lotr-async.

```
LotroAsyncServer.main()
  → GempukkuServer()
    → ServerBuilder.CreatePrerequisites()
    │   └─ LotroCardBlueprintLibrary (loads ALL HJSON card files)
    │   └─ LotroFormatLibrary (loads format definitions)
    ├─ DaoBuilder.CreateDatabaseAccessObjects()
    │   └─ Player, Deck, Collection, GameHistory, League, Tournament DAOs
    └─ ServerBuilder.CreateServices()
        └─ HallServer, LeagueService, TournamentService, ChatServer, etc.
  → Netty HTTP server bootstrap (port from AppConfig)
  → RootUriRequestHandler (routes to domain handlers)
```

---

## HJSON Card Ingestion Pipeline

How a card goes from `.hjson` file to playable game object:

```
HJSON file on disk
  ↓ LotroCardBlueprintLibrary.loadCardsFromFile()
  ↓ Hjson library parses HJSON → JSON string → JSONObject
  ↓
LotroCardBlueprintBuilder.buildFromJson(id, jsonObject)
  ↓ For each field: look up FieldProcessor by name, call processField()
  ↓   Simple fields: TitleFieldProcessor, StrengthFieldProcessor, etc.
  ↓   Complex field: EffectFieldProcessor handles the effects[] array
  ↓
EffectFieldProcessor
  ↓ For each effect object: look up EffectProcessor by type string
  ↓   "activated" → ActivatedEffectProcessor
  ↓   "modifier"  → ModifierEffectProcessor
  ↓   "trigger"   → TriggerEffectProcessor
  ↓   "event"     → EventEffectProcessor
  ↓   ... ~20 effect type processors
  ↓ Each processor parses its HJSON fields using:
  ↓   FilterFactory      → card filter expressions
  ↓   RequirementFactory → conditions/checks
  ↓   TriggerCheckerFactory → trigger conditions
  ↓   ModifierSourceFactory → modifier definitions
  ↓   EffectAppenderFactory → effect/subaction logic
  ↓ Results stored on BuiltLotroCardBlueprint ability lists
  ↓
BuiltLotroCardBlueprint (cached in library)
  ↓ At game time:
PhysicalCardImpl wraps blueprint + game state (zone, wounds, owner, etc.)
```

### Key Classes

| Class | Location | Role |
|-------|----------|------|
| `LotroCardBlueprintLibrary` | logic/game/ | Loads, caches, retrieves card blueprints |
| `LotroCardBlueprintBuilder` | logic/cards/build/ | Dispatches HJSON fields to processors |
| `BuiltLotroCardBlueprint` | logic/cards/build/ | Concrete blueprint — holds parsed abilities |
| `LotroCardBlueprint` | logic/game/ | Interface that game engine calls |
| `FieldProcessor` | logic/cards/build/ | Interface for parsing one HJSON field |
| `EffectFieldProcessor` | logic/cards/build/field/ | Parses the `effects[]` array |
| `PhysicalCardImpl` | logic/game/ | Runtime card instance (blueprint + state) |

### Adding a New HJSON Effect Type

1. Create `NewTypeGameTextProcessor.java` in `logic/cards/build/field/effect/`
2. Implement the processor interface — parse HJSON fields, populate blueprint
3. Register it in `EffectFieldProcessor`'s constructor map: `put("newtype", new NewTypeProcessor())`
4. The blueprint needs storage — add fields/getters to `BuiltLotroCardBlueprint` and the `LotroCardBlueprint` interface

### Adding a New Filter

1. Add to `FilterFactory` in `logic/cards/build/field/` — register the string alias
2. For simple filters: add a case that returns a `Filterable` lambda
3. For function filters: parse parameters and return a parameterized `Filterable`

### Adding a New Modifier Type

1. Create modifier class in `logic/modifiers/`
2. Register in `ModifierSourceFactory` — maps HJSON type string to constructor
3. Implement the modifier interface methods (affects strength? cost? keywords?)

### Adding a New Subaction/Effect

1. Create effect class in `logic/effects/` (or subdirectory)
2. Register in `EffectAppenderFactory` — maps HJSON type string to appender
3. The appender creates the effect and adds it to the action's cost or effect queue

### Adding a New Requirement

1. Create requirement class in `logic/requirements/` or add to existing
2. Register in `RequirementFactory` — maps HJSON type string to checker

### Adding a New Trigger Type

1. Create trigger checker in `logic/timing/results/` or existing trigger classes
2. Register in `TriggerCheckerFactory` — maps HJSON type string to checker

---

## Game Engine

### The Game Loop

```
DefaultLotroGame holds:
  ├─ GameState          (mutable: zones, tokens, phase, assignments)
  ├─ ModifiersLogic     (active modifiers, stat queries)
  ├─ ActionsEnvironment (action proxies, effect results)
  ├─ ActionStack        (Stack<Action> — effect execution queue)
  ├─ TurnProcedure      (main loop driver)
  ├─ UserFeedback       (decision queue — pauses for player input)
  └─ Adventure          (format-specific rules: site movement, win conditions)
```

**TurnProcedure.carryOutPendingActionsUntilDecisionNeeded()** — the heartbeat:

```
LOOP:
  1. Check state-based effects (death, initiative change)
  2. Consume EffectResults → stack triggered actions
  3. Pop Action from ActionStack → call action.nextEffect() → play effect
     └─ Effects may queue more effects or actions
  4. If ActionStack empty:
     └─ GameProcess.process(game) → chains to next process
        └─ Process may create decisions → UserFeedback → PAUSE
  5. If UserFeedback has pending decision → STOP (wait for player)
```

The loop runs until a player decision is needed, then suspends. When the player responds (`decisionMade()`), the loop resumes.

### GameProcess Chain (Phase Sequencing)

Each phase is a linked list of `GameProcess` objects:

```
BetweenTurns → StartOfTurn → FellowshipPhase → MovementPhase → ShadowPhase
  → ManeuverPhase → ArcheryPhase → AssignmentPhase → SkirmishPhase → RegroupPhase
  → EndOfTurn → BetweenTurns → ...
```

Each process:
1. `process(game)` — executes phase logic, may create decisions
2. Sets `_nextProcess` field to next in chain
3. `getNextProcess()` returns it to TurnProcedure

**PlayerPlaysPhaseActionsUntilPassesGameProcess** — the key process for action phases:
- Queries all `ActionProxy` objects for available phase actions
- Creates `CardActionSelectionDecision` — player picks an action or passes
- On pass from both players, chains to next process

### Action System

```
Action (interface)
  ├─ nextEffect(game) → returns next Effect to execute
  ├─ getType() → PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, etc.
  └─ implementations:
      ├─ CostToEffectAction — costs queue + effects queue (most abilities)
      ├─ PlayPermanentAction — play card from hand
      ├─ PlayEventAction — play event (void → resolve → discard)
      ├─ RequiredTriggerAction — mandatory trigger response
      ├─ OptionalTriggerAction — optional trigger response
      └─ SystemQueueAction — engine-internal actions

Effect (interface)
  ├─ playEffect(game) → executes the effect
  ├─ wasCarriedOut() → success tracking
  ├─ getType() → BEFORE_WOUND, BEFORE_DISCARD, etc. (for response windows)
  └─ implementations: WoundCharactersEffect, AddModifierEffect, etc.
```

### Modifier Pipeline

Modifiers are registered with duration and queried at evaluation time:

```
ModifiersLogic
  ├─ addAlwaysOnModifier(modifier)           → permanent (while card in play)
  ├─ addUntilEndOfPhaseModifier(modifier)    → cleared at phase end
  ├─ addUntilStartOfPhaseModifier(modifier)  → cleared at phase start
  ├─ addUntilEndOfTurnModifier(modifier)     → cleared at turn end
  └─ Query methods:
      ├─ getStrength(game, card) → base + all applicable modifiers
      ├─ getTwilightCostToPlay(game, card, target, ...) → printed cost + own cost text + other cards' modifiers
      ├─ getTwilightCostToTransfer(game, card) → what a transfer pays (PayTwilightCostEffect.forTransfer)
      ├─ getKeywords(game, card) → base + added - removed
      ├─ canPlayCard(game, card) → check CantPlay modifiers
      └─ ... many more evaluation queries
```

When game code needs a card's strength, it calls `ModifiersQuerying.getStrength()`, which iterates all active `STRENGTH_MODIFIER` modifiers, checks if they apply to the card, and sums them.

### ActionProxy Pattern

Cards provide actions lazily through `ActionProxy`:

```java
interface ActionProxy {
    List<Action> getPhaseActions(playerId, game)
    List<Action> getOptionalBeforeActions(playerId, game, effect)
    List<Action> getRequiredBeforeTriggers(game, effect)
    List<Action> getOptionalBeforeTriggers(playerId, game, effect)
    List<Action> getRequiredAfterTriggers(game, effectResults)
    List<Action> getOptionalAfterTriggers(playerId, game, effectResults)
}
```

`DefaultActionsEnvironment` maintains lists of action proxies (always-on, until-phase, until-turn) and queries them when actions are needed.

### Decision System

When the game needs player input:

```
UserFeedback.sendAwaitingDecision(playerId, decision)
  → Stores decision, pauses TurnProcedure
  → HTTP long-poll returns decision to client
  → Client renders choices, user picks
  → Client sends response via GameRequestHandler
  → LotroGameMediator.playerDecisionMade(playerId, decisionId, response)
  → decision.decisionMade(response)
  → TurnProcedure resumes
```

Decision types:
- `CardActionSelectionDecision` — pick from available actions
- `ArbitraryCardsSelectionDecision` — choose cards (from hand, play, etc.)
- `MultipleChoiceAwaitingDecision` — choose from text options
- `IntegerAwaitingDecision` — pick a number
- `YesNoDecision` — binary choice

### Where Particular Rules Live

- **Reconcile.** Every reconcile (both players' in regroup, and `ReconcileHand` effects) is a `PlayerReconcilesAction`. Its first effect restores the player's hindered cards, before the discard is asked. `RestoreRule` no longer exists. That restore has no source card (`RestoredResult.getSource()` is null): a `Restored` trigger with no `source` answers it, and one with a `source` does not (F-0361; `Hindered` reads its `source` the same way).
- **Overwhelm.** `RuleUtils.getMinionsFacingOverwhelm(game)` is the one place that decides which minions an overwhelm kills; `ResolveSkirmishEffect.getUpcomingOverwhelmed(game)` gives the characters about to be overwhelmed on either side.
- **Replacing a character.** `GameState.replaceCharacterOnTable` puts the new card in play under the old card's id; borne and stacked cards are handed to it in place and keep their ids. The old card is left in the void under a new id, and the caller removes it from there (`removeCardsFromZone`) before adding it anywhere.
- **Threats an action has still to add.** `ActionContext.getUnpaidThreatCost()` is the threats the action's own costs will add, non-zero only while the action is asked whether it can be used; pass it as `withThreatsAdded` to `Filters.playable`. `GameState.withThreatsToCome` is the scope that carries it and is zero outside a check.
- **The order of cards.** `Filters.filterActive` returns cards in the order of their card ids, and an action's card memory (`DefaultActionContext`) keeps the order it was given (F-0268). Cards an effect will take one after another never pass through a `HashSet` or a `HashMultimap`: their order differs from run to run.
- **Another move in the regroup phase.** `RuleUtils.canChooseToMoveAgain(game)` is the one reading of whether the Free Peoples player may choose to move again: no `CANT_MOVE`, the move limit not reached, and an unhindered companion in the fellowship (`RuleUtils.fellowshipHasUnhinderedCompanion`; CR 5.0 "hinder", F-0315). `FellowshipPlayerChoosesToMoveOrStayGameProcess` asks only when it is true and reads `HAS_TO_MOVE_IF_POSSIBLE` after it; the `CanMove` requirement reads the same method.
- **Who heals.** `ModifiersQuerying.canBeHealed(game, performingPlayer, card)` is asked with the player who makes the heal (the one `HealCharactersEffect` puts in its `HealResult`); `Filters.canHeal(performingPlayer)` is the filter. `CantHealModifier` with a banned player stops that player's heals alone, and with none stops every heal (F-0319).
- **Taking a card into hand from a pile.** `RuleUtils.isBarredFromTakingIntoHand(game, takingPlayer, card)` and `RuleUtils.isBarredFromDrawing(game, player)` are the two readings of `CANT_TAKE_INTO_HAND_FROM_DISCARD_OR_DECK` (Race Text 94_30; Q-0206, Q-0207). The taker is the player the definition has take the card (the `player` of `PutCardsFromDiscardIntoHand`, the action's player for `PutCardsFromDeckIntoHand`), and the bar holds only for a card that is the taker's own. `DrawOneCardEffect` asks the second of every draw that is not a reconcile's (`DrawCardsEffect` knows a reconcile by its action's type); the `DrawCards` effect's forecast says NOTHING for a barred player. The opening hand and the mulligan are dealt by `GameState.playerDrawsCard` with no effect, and are not asked.
- **What is picked inside a branch of a choice.** `ActionContext.isBranchChosenAsDoableInFull()` is true in the context `Choice` carries a branch out in when that branch was open because it can be carried out in full, false when no branch could be and they were offered all the same, and false outside a choice; `DelegateActionContext` hands it on. `Wound` is its one reader: with `times` above 1 and a `choose(...)` selection it then offers only those able to take every wound (Q-0209). `Exert` holds its choice to those able to exert every time on its own account, in a choice or out (F-0131). An effect done to a chosen card more than once that should be held the same way reads this flag.
- **Whether a branch of a choice would do anything.** `EffectAppender.isCertainToDoNothingIfChosen(actionContext)` is what `Choice` asks of each branch when none can be carried out in full: with every branch certain to do nothing, nothing is asked (F-0116). The default is the effect's own forecast, asked as at resolution (`EffectForecast.isCertainToDoNothing`). An effect that holds others overrides it (Q-0220: D): `IfEffect` (the side its check picks), `CostToEffect` (its `requires` fails or a cost cannot be paid; never where the cost can be paid, whatever it buys), `Multiple` and a nested `Choice` (each part in turn: `EffectForecast.areCertainToDoNothingIfChosen`), the five plays (`EffectForecast.findsNoCardNow`; a draw deck only with `mustExist`). `Optional`, `Repeat` and `Preventable` answer false. It is asked by `Choice` alone: the no-effect flag, `IdleOffers` and a prevention go by `forecast` as before.
- **The phase of an event printed with several.** `GameState.getPhaseOfEventBeingPlayed(card)` is the one phase such an event is an event of while it is being played (CR 4.2 "phase action"; F-0292), and null for every other card. `PlayEventAction` sets it as the play begins (`eventPlayStarted`) and clears it as the action ends (`eventPlayFinished`); `ModifiersLogic.getTwilightCostToPlay` prices a card inside `GameState.asIfBeingPlayed`, so the cost offered is the cost paid. `Filters.timeword` is the one reader: a new reading of an event's phase goes through it, never through `blueprint.hasTimeword`, unless what is printed on the card is what is wanted (whether an event may be played in this phase: `PlayUtils`, `PlayCardInPhaseRule`).
- **A card played "ignoring all costs".** `PlayUtils.getPlayCardActionIgnoringCosts` builds the play (its one caller is `OpenAndPlayFromBoosterPack`, Race Text 92_29) and `checkPlayRequirementsIgnoringCosts` says what may be chosen. For an event it asks the blueprint for `getPlayEventCardActionIgnoringCosts`: a `PlayEventAction` whose `isCostsIgnored()` is true pays no twilight, and `DefaultActionSource.createAction` leaves out of it every cost of the event's definition that `EffectUtils.canBePassedOver` allowed when the definition was read (F-0316). A cost that fills a memory, or that holds a `costToEffect`, is not left out.
- **Before the first turn.** No turn ends before turn 1 (F-0327). When the last player has answered the mulligan, `MulliganProcess` calls `signalEndOfTurn()` on `ModifiersLogic` and on `DefaultActionsEnvironment`, so that nothing a starting fellowship left "until the end of the turn" or counted "this turn" reaches the first turn (Théoden 13_137), and hands on to `BetweenTurnsProcess`. It sends no `EndOfTurnResult`: `EndOfTurnGameProcess` is reached only from `FellowshipPlayerChoosesToMoveOrStayGameProcess`, when a turn has been played. A format without the mulligan rule would go from `PlayersDrawStartingHandGameProcess` straight to `BetweenTurnsProcess` with no such clearing; no format turns the rule off.
- **One site, for a format.** `LotroFormat.validateSite(id, context)` is the one reading of a single site: deck validation (`DefaultLotroFormat.validateSites`) and the deck builder's card list (`SortAndFilterCards`, the format filter) both ask it. With a context that says `skipSiteBlockValidation` (Race Text 92_19) the site is let off the format's block and sets and still held to its X-list and errata (Q-0185, F-0342). "Any block" there is the Fellowship, Towers, King and Shadows blocks (`BLOCKS_OPENED_BY_ANY_BLOCK`): a site of another block, which is to say a Hobbit site, draws "Site does not use supported block" unless the format plays it with no override (`isOfOwnBlock`: its one block, or for a Multipath format a site of its sets; Q-0219). The list is opened by `siteOverride:true` in the filter, which the client sends only when a meta-site that `GET /league` gave it for the league chosen is marked `siteOverride`; `LeagueRequestHandler.getNonExpiredLeagues` gives the caller's own meta-sites (`RTMDLeague.getMetaSitesForPosition`, the ones `LeagueService.buildDeckValidationContext` merges) and marks those with the override. A block or set in the filter narrows sites as ever (Q-0218).
- **How many minions a character may be given.** `RuleUtils.hasRoomForAssignments(game, character, more)` is the one count of "one to one, and one more for each defender +1" (CR 4.2 "defender +X"), made against the minions the character already has (`RuleUtils.countAssignedAgainst`; F-0355). The answer to the Free Peoples player's assignment decision (`FreePeoplePlayerAssignsMinionsGameProcess`) asks it, and so does `Filters.assignableToSkirmish` where `ignoreDefender` is false, which is that decision's list of characters. An assignment a card makes passes `ignoreDefender` true and needs both characters unassigned: `AssignFpCharacterToSkirmish` by default, and the definition words `assignableToSkirmishAgainst` and `assignableToSkirmishAgainstIgnoringAllyLocation` whichever player asks. `bots/AssignmentLegality` keeps a copy of the count of its own.
- **A cost of assigning.** "The Free Peoples player must <pay> to assign <a minion>" is `ExtraAssignmentCostModifier` (the definition word `ExtraCostToAssign`; Q-0221), the twin of the extra cost to play. It refuses the Free Peoples side an assignment it charges for while the cost cannot be paid, inside `isValidAssignments` and `canBeAssignedToSkirmish`, the checks every assignment passes. `ModifiersQuerying.getAssignmentCosts(game, assigningPlayer, fpCharacter, minion)` gives the payment for one pair as an `AssignmentCostAction` (its source the card that charges), or null; its two callers are the two makers of assignments. `FreePeoplePlayerAssignsMinionsGameProcess` stacks one for each charged pair of the answer, in the order of the minions' card ids, and then makes every assignment whose payment was carried out in one `AssignmentPhaseEffect`; an unpaid minion goes back among the leftover minions. `AssignFpCharacterToSkirmish` stacks it with the `AssignmentEffect` as its effect (`withCostPaid`, which checks the assignment inside `GameState.withAssignmentPaidFor` so that a cost already paid is not asked for again). The Shadow player's assigning is not charged.
- **An offer that costs nothing and would do nothing.** `IdleOffers` (cards/build/field/effect) is the rule of Q-0224: `DefaultActionSource.isValid` asks it last, for an action its processor marked `setWithheldWhenIdle` (an optional `Trigger`, killed and removed-from-play triggers, `Activated`, a `Response` of a card in play, `AddActivated`), and `AddTrigger` asks it for an optional trigger added for a time. Not offered where nothing is paid (`paysSomething`, read from the definition when it is built) and every effect's `EffectForecast` is NOTHING, asked once as the no-effect flag asks and once as a prevention does (`isAtResolution`). `TaughtForecast` wraps an effect appender with a forecast given to this rule alone (`IdleOffers.isAskingTaught`): what is taught for this rule alone goes there; what is put in the effect's own `forecast` is heard by the no-effect flag and by `Preventable` as well (`TakeOffRing`, F-0370). `TaughtForecast.actsOnWhatItFinds(effect, resolvers...)` is the one line for an effect that acts on what a card resolver finds; `actsOnWhatItFindsInHand` is the same for a hand, and of another player's hand says nothing unless it is empty, so that no offer made or withheld tells what an opponent holds. Never asked of an event, a play, an action from a pile or a stack, or the `Optional` effect. `gemp.idleOffers` / `GEMP_IDLE_OFFERS` (`off`, `flag`, `resolution`, `taught`; the default and the mode in play is `taught`) is for tests.
- **Borne by a kind of card, and a hindered bearer.** `Filters.attachedTo(filters...)` is the one reading of "borne by <such a card>" (the definition word `attachedTo`, and the count behind `forEachHasAttached`). A hindered bearer has nothing printed on it to be found by (CR 5.0 "hinder"; Q-0108), so it is accepted only where no filter given names a kind (`Filters.namesNoKind`): each is a `PhysicalCard` or a filter made as a `NamesNoKind` (`in`, `bearerOf`, `ringBearer`, `any`). Every other filter is taken to name a kind, a joined `and`, `or` or `not` included, and `FilterFactory` joins the words of an `attachedTo(a,b)` before they arrive. A new filter that says which card is meant, and nothing of what it has, is given that type where it is made.
- **Initiative.** `ModifiersLogic.hasInitiative(game)` is the one reading: the Free Peoples player's hand, with a card played from it still counted while it lies in `Zone.VOID_FROM_HAND` (its costs being paid) and, for a permanent, until its play is announced, against four and whatever changes that number; `InitiativeChangeRule` publishes a change of side. `PlayEventEffect` moves an event to the plain void as its play is announced, so an initiative its effect asks for is read without it. A `haveInitiative` requirement asked of a card still in hand counts that card (F-0464, asked of Ketura: CR 4.2 "playing a card" does not). `mechanics/InitiativeTests`.
- **Hunter, enduring, lurker, threats, the Rule of 9.** `HunterRule` and `EnduringRule` are strength modifiers with no source card. Lurker is in `PlayoutSkirmishesGameProcess`, which builds the choice of the next skirmish from the skirmishes with no lurker in them while there is one. `ThreatRule` answers the kill of a companion or ally (`ANY_NUMBER_KILLED`) with `ThreatWoundsEffect`, the window a card may answer, and then removes the threats and asks for the wounds one at a time; `PlayConditions.getThreatLimit` and `getMaxAddThreatCount` are the limit. `PlayUtils.checkRuleOfNine` counts the companions in play, hindered ones included, and in the dead pile. Each has its class under `mechanics/`.
- **Damage bonus, sanctuaries, wearing the Ring, control of sites.** `ResolveSkirmishDamageAction` places 1 wound and one more for each `getKeywordCount(DAMAGE)` among the winners, counted as the action begins, one `WoundCharactersEffect` for all the losers at a time; a `RemoveKeyword` of damage makes that count 0 whatever is given afterwards. `SanctuaryRule` gives the keyword to every site numbered 3 or 6 and answers `START_OF_TURN` at one with a 0-to-1 heal of a companion for each of 5 (`getSanctuaryHealModifier` changes the 5). `GameState.isWearingRing` is one flag for the game: `TakeOffRingRule` clears it as a regroup phase starts and as it ends, and `MakeRingBearerEffect` when a new Ring-bearer is made. A controlled site stays among the cards in play with its site number, so `GameState.getSite(n)` still finds it: `takeControlOfCard` and `loseControlOfCard` change its controller and its zone (SUPPORT, ADVENTURE_PATH) and start and stop its controlled-site text. `TakeControlOfASiteEffect` picks the lowest uncontrolled site behind every marker and `PlayConditions.getSiteToLiberate` the highest one controlled by another than the current player (or by the player a definition's `controller` names). `PlaySiteEffect` is every laying and replacing of a site: a controlled site that is replaced loses its controller on its way to the adventure deck and the new one is controlled with the stacked cards taken across (F-0466); cards ATTACHED to a replaced site are not taken across (F-0467, open). Each has its class under `mechanics/` (`DamageBonusTests`, `SanctuaryTests`, `WearingTheOneRingTests`, `ControlOfSitesTests`).
- **The message record.** `GameState.startMessageRecord()` begins a full record of every message sent to the players from then on, and `getMessageRecord()` returns it (null if never started); the virtual table's `MarkGameLog()` is its one caller.

---

### Playing a permanent: two batches (F-0139, 2026-10-07)

Playing a permanent takes two batches of effect results. `PlayCardEffect` places the card, which is in play from that moment with its own standing texts in force, and publishes `EnteredPlayResult` (`EffectResult.Type.ENTERS_PLAY`). Nothing answers that result by name: `ConstantlyCheck` triggers, which answer every batch, and the rules that run on their own account (the death of a character with no vitality) are what act in it. When that batch has finished, the play action (`PlayPermanentAction` or `AttachPermanentAction`) carries out `PlayCardEffect.getPublishEffect()`: if the card is still in play it publishes `PlayCardResult` (`PLAY`), the only thing `Played` triggers and responses to a play answer; if the card has gone, nothing is published and the game log says "<card> left play as it came into play". When the `PLAY` batch and the action's extra effects have finished, the action calls `PlayCardEffect.completePlay()`: a play whose card is not in play then answers `wasCarriedOut()` with false, and its `PlayCardResult` is taken out of `getPhaseEffectResults()` and `getTurnEffectResults()` with `ActionsEnvironment.retractEffectResult`. Events (`PlayEventEffect`) and sites are played in one step, as before.

A card played from hand counts toward its owner's hand for initiative until the publish step (`ActionsEnvironment.noteCardEnteringPlay`, `noteCardEnteredPlay`, `countCardsEnteringPlayFromHand`, read by `ModifiersLogic.hasInitiative`), so the `InitiativeChangeResult` lands in the same batch as the `PlayCardResult`.

A rule class or trigger checker that answers effect results must test the result's type first: one that accepts any result also runs in the entering batch of every play.

### The entering step is two batches; support area; plays by turn (engine pass 9, 2026-10-07)

The entering step is two batches. `PlayCardEffect` places the card and publishes `EnteredPlayResult` (`ENTERS_PLAY`), which `ConstantlyCheck` triggers and the rules that act on their own account answer. The play action then takes `PlayCardEffect.nextStep()` until it returns null: first, if the card is still in play, a second `EnteredPlayResult` with `isOwnText()` true, which only the card's own `EntersPlay` trigger answers; then the announcing step, which publishes `PlayCardResult` (`PLAY`) for a card still in play and otherwise logs "<card> left play as it came into play". `EnteredPlayResult` carries the zone the card was played from.

`Filters.isInSupportArea(card)` walks up a card's bearers and answers whether the card at the bottom lies in Zone.SUPPORT; `Filters.inSupportArea` is the filter, and the definition language's `support`. `Filters.zone(Zone.SUPPORT)` remains the zone test. `ActivateCardAction` records both the zone and whether the card was in a support area when the action was built, and `ActivateCardResult` carries them (`getActivatedZone()`, `wasActivatedInSupportArea()`).

`DefaultActionsEnvironment.emitEffectResult` stamps every `PlayCardResult` (events and sites included) with the site the fellowship is at (`getPlayedAtSite()`), which `PlayedCardThisTurn` reads.

## HTTP API Layer

### Handler Architecture

```
HTTP Request → Netty → GempukkuHttpRequestHandler
  → RootUriRequestHandler.handleRequest()
    → Routes by URI prefix to specific handler
```

All handlers extend `LotroServerRequestHandler` which provides:
- `extractObject(context, Type.class)` — dependency injection via type map
- `getResourceOwnerSafely(request, participantId)` — authentication
- `validateAdmin(request)` — admin check

### Key Handlers

| Handler | URI Prefix | Purpose |
|---------|-----------|---------|
| `GameRequestHandler` | `/game` | Live game state, decisions, concede |
| `HallRequestHandler` | `/hall` | Lobby, create/join tables, matchmaking |
| `DeckRequestHandler` | `/deck` | CRUD decks, validation, sharing |
| `LeagueRequestHandler` | `/league` | League operations |
| `AdminRequestHandler` | `/admin` | Card reload, shutdown, MOTD |
| `CollectionRequestHandler` | `/collection` | Card inventory |
| `ChatRequestHandler` | `/chat` | Chat rooms |
| `ReplayRequestHandler` | `/replay` | Game replays |

### Long Polling

Game and chat use long polling for real-time updates:
1. Client sends request with current channel number
2. Server holds request until new events available or timeout
3. Response contains serialized game events since last poll
4. Client immediately re-polls

`GameCommunicationChannel` (per player) implements `GameStateListener` — observes all game state changes, filters by player visibility, queues `GameEvent` objects.

### Frontend ↔ Backend Mapping

**File:** `gemp-lotr-async/src/main/web/js/gemp-022/communication.js` (~1560 lines)

Key client API methods and their server endpoints:

| Client Method | HTTP | Server Handler |
|--------------|------|----------------|
| `startGameSession()` | POST `/game/{id}` | GameRequestHandler |
| `updateGameState(ch)` | POST `/game/{id}` | GameRequestHandler (long poll) |
| `gameDecisionMade(id, response)` | POST `/game/{id}` | GameRequestHandler |
| `getHall()` | GET `/hall` | HallRequestHandler |
| `createTable(format, deck, ...)` | POST `/hall` | HallRequestHandler |
| `joinTable(tableId, deck)` | POST `/hall/{id}` | HallRequestHandler |
| `getDeck(name)` | GET `/deck` | DeckRequestHandler |
| `saveDeck(name, format, contents)` | POST `/deck` | DeckRequestHandler |
| `getDeckStats(contents, format)` | POST `/deck/stats` | DeckRequestHandler |
| `getFormats()` | GET `/hall/formats/html` | HallRequestHandler |
| `concede()` | POST `/game/{id}/concede` | GameRequestHandler |

---

`Filters.supportAreaOf(card)` answers with the player in whose support area a card is (the owner of the card
at the bottom of its chain of bearers, or the controller if that card is a controlled site), or null;
`isInSupportArea`, `inSupportAreaOf(player)` and `inSupportAreaOfAnOpponentOf(player)` are built on it.
`RingBearerRule` holds the rules' protections of the Ring-bearer (no discard, no return to hand) and of The
One Ring (no discard) as modifiers with no source card, which `ModifiersLogic.getModifiersProtectingCard`
keeps in force for a hindered card. `GameState.canBeHindered` is where a site, a Map and a meta-site are
refused to every hinder.

## Format System

### Format Definition

`LotroFormatLibrary` loads format definitions from config files. Each `DefaultLotroFormat` holds:

- Valid card sets and banned/restricted lists
- Maximum copies per card name (default 4)
- `Adventure` implementation (site movement rules, win conditions)
- `SitesBlock` (Fellowship, Towers, King, Shadows)
- Flags: mulliganRule, mapsAllowed, ringBearerSkirmishCanBeCancelled, ruleOfFour, winAtRegroup, discardPileIsPublic, playtest

### Deck Validation

`DefaultLotroFormat.validateDeck(LotroDeck)` checks:
1. Ring-bearer and Ring are valid for format
2. Adventure deck has correct number of sites
3. Sites match format's block requirements
4. Draw deck size meets minimum (default 30+RB+Ring)
5. No banned cards
6. Card copies within `_maximumSameName` limit
7. Restricted card limits (limit-2, limit-3)
8. Map restrictions (if applicable)
9. Cards are from valid sets

### Adventure System

`Adventure` interface defines format-specific game rules:
- `applyAdventureRules()` — registers format rules as modifiers
- `getStartingGameProcess()` — bidding, starting fellowship
- `appendNextSiteAction()` — site movement mechanics
- Win conditions registered during `applyAdventureRules()`

`DefaultAdventure` — standard 2-player rules. Custom adventures could implement variant rulesets.

---

## Game Mediator (Server ↔ Engine Bridge)

`LotroGameMediator` sits between HTTP handlers and the game engine:

```
GameRequestHandler
  → LotroGameMediator
    → DefaultLotroGame (logic)
    → GameCommunicationChannel[] (per-player event buffers)
    → Player clocks, auto-pass settings
```

Responsibilities:
- Initialize game with format, participants, card library
- Route player decisions from HTTP to game engine
- Serialize game state changes to client-consumable events
- Manage game lifecycle (cancel, concede, timeout)
- Track per-player communication channels

---

## Database Layer

All DAOs in `gemp-lotr-server/src/main/java/com/gempukku/lotro/db/`:

| DAO | Purpose |
|-----|---------|
| `DbPlayerDAO` | Player accounts, authentication |
| `DbDeckDAO` | Saved decks |
| `DbCollectionDAO` | Card collections/inventory |
| `DbGameHistoryDAO` | Game results history |
| `DbLeagueDAO` | League definitions and standings |
| `DbTournamentDAO` | Tournament data |
| `DbTransferDAO` | Card transfers/trades |

Cached variants (`CachedPlayerDAO`, `CachedDeckDAO`) wrap DB DAOs with in-memory caching.

---

## Where to Look When...

### Adding a new card mechanic (new HJSON effect/modifier/filter)

1. **Define the HJSON syntax** you want (see `hjson-reference` skill)
2. **Register processors** in the appropriate factory:
   - New effect type → `EffectFieldProcessor` constructor
   - New modifier → `ModifierSourceFactory`
   - New filter → `FilterFactory`
   - New requirement → `RequirementFactory`
   - New trigger → `TriggerCheckerFactory`
   - New subaction → `EffectAppenderFactory`
3. **Implement the Java class** for the new mechanic
4. **Add enum values** in `gemp-lotr-common` if needed (new keyword, phase, etc.)

### Adding a new API endpoint

1. Create or modify handler in `gemp-lotr-async/handler/`
2. Register route in `RootUriRequestHandler` if new handler
3. Add client method in `communication.js`
4. Add UI integration in the appropriate frontend JS file

### Changing format/validation rules

1. Format definitions: `LotroFormatLibrary` + config files
2. Deck validation: `DefaultLotroFormat.validateDeck()`
3. Adventure rules: `Adventure` implementations
4. Format flags: `DefaultLotroFormat` constructor

### Modifying game flow (phases, turn structure)

1. `GameProcess` implementations in `logic/timing/processes/turn/`
2. `TurnProcedure` for the main loop
3. `Adventure` for format-specific phase hooks
4. `DefaultActionsEnvironment` for action proxy lifecycle

### Debugging a card behavior

1. Find the card's HJSON in `gemp-lotr-cards/src/main/resources/cards/`
2. Trace the effect type to its processor in `logic/cards/build/field/effect/`
3. Follow the processor to the Java effect/modifier/action class
4. Check `ModifiersLogic` for modifier evaluation
5. Check `DefaultActionsEnvironment` for trigger/response firing
