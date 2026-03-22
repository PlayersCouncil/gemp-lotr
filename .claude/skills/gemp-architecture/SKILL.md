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
      ├─ getTwilightCost(game, card) → base + modifiers
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

---

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
