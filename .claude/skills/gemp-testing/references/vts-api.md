# VirtualTableScenario (VTS) API Reference

## Test Lifecycle

Every Gemp unit test follows this flow:

```
1. Define card aliases      →  HashMap<String, String> cards
2. Construct VTS            →  new VirtualTableScenario(cards, sites, frodo, ring)
3. Retrieve card references →  scn.GetFreepsCard("alias"), scn.GetShadowCard("alias")
4. Cheat cards into position →  scn.MoveCompanionsToTable(card), scn.MoveCardsToHand(card)
5. Start the game           →  scn.StartGame()
6. Advance to relevant phase →  scn.SkipToPhase(Phase.MANEUVER)
7. Perform actions & choices →  scn.FreepsPlayCard(card), scn.FreepsChooseCard(card)
8. Assert results           →  assertEquals(expected, scn.GetStrength(card))
```

Steps 3-4 happen before `StartGame()`. The constructor handles bidding and deck setup automatically. `StartGame()` skips starting fellowship selection, mulligans, and removes the bid burden. After `StartGame()`, you're in the Fellowship phase of turn 1.

**Critical:** Cheating (step 4) places cards without paying costs or checking legality. This is intentional — tests should only exercise the specific mechanic under test, not re-prove that card deployment works. However, Shadow minions must be cheated AFTER `SkipToSite()` if you use it, because site traversal discards minions.

---

## Setup

### Constants

```java
// Player IDs
String P1 = "Free Peoples Player"
String P2 = "Shadow Player"

// Canned site decks (benign sites with no passive effects)
HashMap FellowshipSites   // Set 1 sites 1-9
HashMap TowersSites       // Set 4 sites 1-9
HashMap KingSites         // Sets 7-10 sites 1-9 (DEFAULT if null)
HashMap ShadowsSites      // Sets 11-18 sites 1-9

// Ring-bearers
String FOTRFrodo  = "1_290"   // str 3, vit 4, res 10. Fellowship: exert Frodo-signet companion to heal
String MDFrodo    = "10_121"  // DEFAULT if null
String GimliRB    = "9_4"     // str 6, vit 3, res 5, damage +1
String GaladrielRB = "9_14"  // str 3, vit 3, res 4
String SmeagolRB  = "9_30"   // str 3, vit 4, res 8. Moves kill unbound companions
String SamRB      = "13_156" // str 3, vit 4, res 5

// One Rings
String RulingRing       = "1_2"   // +1 str. Response: wound→burden in skirmish (DEFAULT)
String IsildursBaneRing = "1_1"   // +1 str, +1 vit. Response: wound→2 burdens
String ATARRing         = "4_1"   // +2 vit. Skirmish: add burden to wear
String GreatRing        = "19_1"  // Maneuver: wear. Skirmish: burden for +3 str

// Formats
String Fellowship = "fotr_block"  // Sets 1-3, RB skirmish cancelable
String Multipath  = "multipath"   // Sets 1-10 (DEFAULT)
String PCMovie    = "pc_movie"    // Sets 1-10 + V1/V2
String Shadows    = "expanded"    // Sets 1-19, Shadows path
```

### Constructors

```java
// Most common — cards + sites + ring-bearer + ring
new VirtualTableScenario(cards, sites, frodo, ring)

// With RTMD meta-sites (pass null for whichever side doesn't have one)
new VirtualTableScenario(cards, sites, frodo, ring, freepsMetaSiteId, shadowMetaSiteId)

// With custom bid handler (for meta-sites that alter bidding)
new VirtualTableScenario(cards, sites, frodo, ring, freepsMetaSiteId, shadowMetaSiteId, bidHandler)

// With format override
new VirtualTableScenario(cards, sites, frodo, ring, format)

// Minimal — just cards, all other params use defaults
new VirtualTableScenario(cards)
```

Meta-site cards are auto-registered during construction and accessible as `scn.GetFreepsCard("mod")` or `scn.GetShadowCard("mod")`.

### Card Retrieval

```java
scn.GetFreepsCard("alias")   // By test alias — throws if not found
scn.GetShadowCard("alias")
scn.GetRingBearer()           // The FP Ring-bearer
scn.GetShadowRingBearer()
scn.GetRing()                 // The One Ring (FP)
```

Sites are retrieved differently:
```java
scn.GetFreepsSite(3)           // Site 3 from FP adventure deck (by number)
scn.GetShadowSite(3)
scn.GetCurrentSite()           // The site the fellowship is currently at
scn.GetSite(3)                 // Site 3 from the adventure path (already placed)
```

### Game Start

```java
scn.StartGame()                        // Standard: skip fellowships, reset hands, remove bid burden
scn.StartGame(false)                   // Don't reset drawn hands
scn.StartGame(true, false)             // Reset hands but DON'T skip starting fellowship selection
scn.StartGame(site1Card)               // On Shadows paths only, prompt the Shadow player to play the given site 1
```

After `StartGame()`, you are in the Fellowship phase. Hands contain only what you explicitly placed via `MoveCardsToHand()` before calling `StartGame()`.

---

## Zone Manipulation (Cheating)

All of these bypass costs, requirements, and legality. Call before `StartGame()` unless you have a specific reason not to.

### Characters
```java
scn.MoveCompanionsToTable(card1, card2, ...)    // FP companions → in play
scn.MoveMinionsToTable(card1, card2, ...)       // Shadow minions → in play
// String-alias variants:
scn.MoveCompanionsToTable("aragorn", "gimli")
scn.MoveMinionsToTable("runner", "troll")
```

### Attachments & Stacking
```java
scn.AttachCardsTo(bearer, card1, card2, ...)    // Attach to a character
scn.StackCardsOn(target, card1, card2, ...)     // Stack on a card
```

### Hand
```java
scn.MoveCardsToHand(card1, card2, ...)
scn.MoveCardsToFreepsHand("alias1", "alias2")   // By alias, if you will not reference them again
scn.MoveCardsToShadowHand("alias1", "alias2")
```

### Support Area
```java
scn.MoveCardsToSupportArea(card1, card2, ...)
scn.MoveFreepsCardsToSupportArea("alias")
scn.MoveShadowCardsToSupportArea("alias")
```

### Discard & Dead
```java
scn.MoveCardsToDiscard(card1, card2, ...)
scn.MoveCardsToFreepsDiscard("alias")
scn.MoveCardsToShadowDiscard("alias")
scn.MoveCardsToDeadPile(card1, ...)
```

### Draw Deck
```java
scn.MoveCardsToTopOfDeck(card1, ...)
scn.MoveCardsToBottomOfDeck(card1, ...)
scn.MoveCardsToTopOfFreepsDeck("alias")
scn.MoveCardsToTopOfShadowDeck("alias")
scn.ShuffleCardsIntoFreepsDeck(card1, ...)
scn.ShuffleCardsIntoShadowDeck(card1, ...)
scn.ShuffleFreepsDeck()
scn.ShuffleShadowDeck()
```

### Drawing Cards
```java
scn.FreepsDrawCards(3)    // Draw 3 cards
scn.ShadowDrawCards(1)
```

### Other
```java
scn.MoveCardToAdventurePath(card)       // Place as next site on adventure path
scn.MoveCardsToAdventureDeck(card, ...) // Back to adventure deck
scn.MoveCardToZone(card, Zone.X)        // Raw teleport to any zone
```

---

## Game Flow

### Phase Order
```
Fellowship → Shadow → Maneuver → Archery → Assignment → Skirmish(s) → Regroup
```
Fellowship is FP-only. Shadow is Shadow-only. All other phases alternate FP/Shadow until both pass.

### Skipping to a Phase
```java
scn.SkipToPhase(Phase.MANEUVER)   // Auto-pass through all earlier phases
scn.SkipToPhase(Phase.ARCHERY)    // Skipping past this point WILL FAIL if an archer is on the table (archery wound assignment blocks), so avoid using archers unless needed
scn.SkipToPhase(Phase.ASSIGNMENT)
scn.SkipToPhase(Phase.REGROUP)
```
`SkipToPhase` handles Fellowship→move→Shadow→etc automatically. It tries up to 20 pass rounds. Required triggers are auto-accepted; optional triggers or reacts will cause it to fail — place problematic cards after skipping.

### Skipping to a Site
```java
scn.SkipToSite(5)   // Traverse from current site to site 5
```
Each traversal swaps FP/Shadow roles internally. After even traversals, roles return to normal. **Cheat Shadow minions AFTER this call**, not before — traversal discards them.

### Phase Passing
```java
// Named phase passes (all aliases for PassCurrentPhaseActions):
scn.PassManeuverActions()
scn.PassArcheryActions()
scn.PassAssignmentActions()
scn.PassSkirmishActions()       // After this, skirmish resolves and wounds are applied
scn.PassRegroupActions()

// Single-player passes:
scn.FreepsPassCurrentPhaseAction()   // Only FP passes
scn.ShadowPassCurrentPhaseAction()   // Only Shadow passes

// Both pass:
scn.PassCurrentPhaseActions()   // Current decider first, then other
```

### Movement Decisions
```java
scn.FreepsChooseToMove()   // Move again (at regroup movement decision)
scn.FreepsChooseToStay()   // Stay at current site
```

### Convenience Shortcuts
```java
scn.SkipToAssignments()        // SkipToPhase(ASSIGNMENT) + PassCurrentPhaseActions
scn.SkipToArcheryWounds()      // SkipToPhase(ARCHERY) + PassCurrentPhaseActions
scn.SkipToMovementDecision()   // All the way to regroup movement prompt
scn.SkipToShadowAssignments()  // SkipToAssignments + FreepsDeclineAssignments
scn.SkipPastAllAssignments()   // Skip all assignment sub-phases
```

---

## Playing & Using Cards

### Availability Checks
```java
scn.FreepsPlayAvailable(card)     // "Play [name]" in current actions?
scn.ShadowPlayAvailable(card)
scn.FreepsActionAvailable(card)   // "Use [name]" in current actions?
scn.ShadowActionAvailable(card)
scn.FreepsActionAvailable("text") // Any action containing arbitrary text?
scn.ShadowActionAvailable("text")
scn.FreepsTransferAvailable(card)
scn.FreepsAnyActionsAvailable()   // Any action at all?
scn.ShadowAnyActionsAvailable()
```

### Performing Actions
```java
scn.FreepsPlayCard(card)          // Play card from hand
scn.ShadowPlayCard(card)
scn.FreepsUseCardAction(card)     // Activate an ability ("Use")
scn.ShadowUseCardAction(card)
scn.FreepsTransferCard(card)      // Transfer a possession
scn.FreepsDiscardToHeal(card)     // Discard unique from hand to heal in-play copy
```

---

## Decisions & Choices

### Optional Triggers
```java
scn.FreepsHasOptionalTriggerAvailable()          // An optional trigger is pending?
scn.FreepsHasOptionalTriggerAvailable("text")    // Optional trigger with specific text?
scn.ShadowHasOptionalTriggerAvailable()

scn.FreepsAcceptOptionalTrigger()     // Accept (answer "0")
scn.FreepsDeclineOptionalTrigger()    // Decline (answer "")
scn.ShadowAcceptOptionalTrigger()
scn.ShadowDeclineOptionalTrigger()
```
Note: `FreepsDeclineOptionalTrigger()` is frequently used to decline the Ruling Ring's wound-to-burden after a skirmish loss.

### Choosing Cards
```java
scn.FreepsChooseCard(card)         // Select one card from choices
scn.FreepsChooseCards(card1, card2) // Select multiple
scn.ShadowChooseCard(card)
scn.FreepsChooseAnyCard()          // Pick the first available card

// Verify card is/isn't in choices:
scn.FreepsHasCardChoiceAvailable(card)          // true if selectable
scn.FreepsHasCardChoicesAvailable(card1, card2) // ALL must be available
scn.FreepsHasCardChoicesNotAvailable(card1, card2) // NONE must be available
scn.ShadowHasCardChoiceAvailable(card)
// etc.
```

### General Choices
```java
scn.FreepsChoose("2")       // Choose by text/number (for X values, multiple choice, etc.)
scn.ShadowChoose("option")
scn.FreepsChooseYes()
scn.FreepsChooseNo()
scn.ShadowChooseYes()
scn.ShadowChooseNo()
```

### Choosing Among Triggers
When multiple cards trigger simultaneously:
```java
scn.FreepsChooseOptionalTrigger(card)   // Pick which card's trigger to resolve
scn.ShadowChooseOptionalTrigger(card)
```

### Numeric Bounds on Current Choice
```java
scn.FreepsGetChoiceMin()   // Minimum acceptable answer
scn.FreepsGetChoiceMax()   // Maximum acceptable answer
```

### Raw Decision Answers
```java
scn.FreepsDecided("answer")   // Send raw answer string
scn.ShadowDecided("answer")
scn.FreepsDecided(0)           // Send integer answer
```

### Decision Inspection
```java
scn.FreepsDecisionAvailable("text")   // Current decision contains text?
scn.ShadowDecisionAvailable("text")
scn.FreepsAnyDecisionsAvailable()     // Any decision at all?

// Phase-specific checks:
scn.AwaitingFellowshipPhaseActions()
scn.AwaitingShadowPhaseActions()
scn.AwaitingFreepsManeuverPhaseActions()
// ... etc for all phases
```

---

## Querying Game State

### Twilight Pool
```java
scn.GetTwilight()          // Current pool amount
scn.SetTwilight(5)         // Override pool (cheating)
```

### Burdens & Threats
```java
scn.GetBurdens()
scn.AddBurdens(2)
scn.RemoveBurdens(1)

scn.GetThreats()
scn.GetThreatLimit()
scn.AddThreats(1)
scn.RemoveThreats(1)
```

### Phase & Turn
```java
scn.GetCurrentPhase()      // Phase enum
scn.GetCurrentSiteNumber() // 1-9
scn.GetMoveCount()         // Times moved this turn
scn.GetMoveLimit()         // Current move limit (default 2)
scn.GameIsFinished()       // Game over?
```

### Players
```java
scn.GetCurrentPlayer()     // Whose turn
scn.GetOffPlayer()         // Not their turn
scn.GetDecidingPlayer()    // Who has an active decision
```

### Initiative & Archery
```java
scn.FreepsHasInitiative()
scn.ShadowHasInitiative()
scn.GetFreepsArcheryTotal()
scn.GetShadowArcheryTotal()
```

### Ring State
```java
scn.RBWearingOneRing()     // Ring-bearer wearing the ring?
```

### Culture Count
```java
scn.GetFreepsCultureCount()   // Distinct spottable FP cultures
scn.GetShadowCultureCount()
```

---

## Querying Card State

All of these reflect current in-game modifiers, not just printed stats.

```java
scn.GetStrength(card)
scn.GetVitality(card)
scn.GetResistance(card)
scn.GetTwilightCost(card)
scn.GetOverwhelmMultiplier(card)
scn.GetMinionSiteNumber(card)
```

### Wounds
```java
scn.GetWoundsOn(card)
scn.FreepsGetWoundsOn("alias")     // By card alias
scn.IsExhausted(card)              // Vitality == 1
scn.AddWoundsToChar(card, 2)       // Cheating
scn.RemoveWoundsFromChar(card, 1)  // Cheating
```

### Keywords & Properties
```java
scn.HasKeyword(card, Keyword.DAMAGE)
scn.GetKeywordCount(card, Keyword.DAMAGE)   // For damage+X, archer+X, etc.
scn.HasTimeword(card, Timeword.FELLOWSHIP)
scn.IsType(card, CardType.COMPANION)
scn.IsRace(card, Race.ELF)
scn.IsHindered(card)
scn.HinderCard(card)       // Cheating
scn.RestoreCard(card)      // Cheating
```

### Tokens
```java
scn.GetCultureTokensOn(card)
scn.AddTokensToCard(card, 2)
scn.RemoveTokensFromCard(card, 1)
```

### Attachments & Control
```java
scn.GetAttachedCards(card)    // List of cards attached to this card
scn.GetStackedCards(card)     // List of cards stacked on this card
scn.IsSiteControlled(card)
scn.FreepsControls(card)
scn.ShadowControls(card)
```

---

## Skirmishes

### Standard Skirmish Flow
```java
// 1. Get to assignment phase
scn.SkipToPhase(Phase.ASSIGNMENT);
scn.PassAssignmentActions();

// 2. Assign and resolve
scn.FreepsAssignAndResolve(companion, minion);
// This does: assign companion↔minion, decline Shadow assignments, choose skirmish to resolve

// 3. Skirmish actions
scn.PassSkirmishActions();  // Both pass → skirmish resolves, wounds applied

// 4. Handle Ruling Ring trigger (if applicable)
scn.FreepsDeclineOptionalTrigger();  // Decline wound-to-burden
```

### Assignment (manual)
```java
scn.FreepsAssignToMinions(comp, minion1, minion2)   // Assign without auto-resolve
scn.ShadowAssignToMinions(comp, minion)
scn.FreepsDeclineAssignments()   // Pass assignment opportunity
scn.ShadowDeclineAssignments()

// Multi-group: each array is [companion, minion1, ...]
scn.FreepsAssignToMinions(new PhysicalCardImpl[]{comp1, min1}, new PhysicalCardImpl[]{comp2, min2})

scn.FreepsResolveSkirmish(companion)   // Choose which skirmish to resolve next
```

### Skirmish State Queries
```java
scn.IsCharAssigned(card)
scn.IsCharAssignedAgainst(card1, card2)
scn.IsCharSkirmishing(card)
scn.IsCharSkirmishingAgainst(card1, card2)
scn.GetActiveSkirmish()

scn.FreepsCanAssign(card)           // Normal FP assignment rules
scn.ShadowCanAssign(card)
scn.CanBeAssignedViaAction(card)    // Either side, via action
```

---

## Assertions

Import: `import static com.gempukku.lotro.framework.Assertions.*;`

```java
assertInZone(Zone.HAND, card)           // Card is in zone
assertNotInZone(Zone.DISCARD, card)     // Card is NOT in zone
assertInPlay(card)                       // Card is in any in-play zone
assertInDiscard(card)                    // Shorthand for assertInZone(Zone.DISCARD, ...)
assertInHand(card)                       // Shorthand for assertInZone(Zone.HAND, ...)
assertAttachedTo(card, bearer)           // Card attached to bearer
assertNotAttachedTo(card, bearer)        // Card NOT attached to bearer
```

All accept varargs — you can check multiple cards at once:
```java
assertInDiscard(card1, card2, card3);
```

---

## Pile Inspection

### Hand
```java
scn.GetFreepsHandCount()
scn.GetShadowHandCount()
scn.GetFreepsHand()         // List<? extends PhysicalCard>
scn.GetShadowHand()
```

### Draw Deck
```java
scn.GetFreepsDeckCount()
scn.GetShadowDeckCount()
scn.GetFreepsTopOfDeck()            // Top card (1-based)
scn.GetFromTopOfFreepsDeck(2)       // 2nd from top
scn.GetFreepsBottomOfDeck()
scn.GetFromBottomOfFreepsDeck(2)    // 2nd from bottom
```

### Discard & Dead
```java
scn.GetFreepsDiscardCount()
scn.GetShadowDiscardCount()
scn.GetFreepsDeadCount()
```

---

## Advanced: Ad-Hoc Effects

For testing mechanics without existing cards, or injecting effects/modifiers directly.

```java
// Permanent modifier for rest of game
scn.ApplyAdHocModifier(modifier)

// Permanent action proxy (invisible card with abilities)
scn.ApplyAdHocAction(actionProxy)

// Execute an arbitrary effect (finicky — only works when 0 legal actions available)
scn.FreepsExecuteAdHocEffect(effect)
scn.ShadowExecuteAdHocEffect(effect)

// Shadow takes control of a site
scn.ShadowTakeControlOfSite()

// Generate Java Filterable from HJSON filter string
scn.GenerateFreepsFilter("your,companion,elf")   // "you" = FP
scn.GenerateShadowFilter("your,minion,nazgul")   // "you" = Shadow

// Auto-discard anything matching filter (permanent, fires every trigger check)
scn.ApplyAdHocFreepsAutoDiscard("your,condition")
scn.ApplyAdHocShadowAutoDiscard("minion")
```

---

## Advanced: Blueprint Lookup (Static)

No game needed — for StatsAreCorrect tests or pre-game inspection:
```java
var bp = VirtualTableScenario.FindCard("1_50");
assertEquals("Legolas", bp.getTitle());
assertEquals(CardType.COMPANION, bp.getCardType());
```
