---
name: gemp-testing
description: >
  Comprehensive guide for writing unit tests in the Gemp-LotR codebase using the VirtualTableScenario test framework.
  Use this skill whenever writing, modifying, or debugging Gemp card tests, site tests, or errata tests.
  Trigger when: the user mentions writing tests for Gemp cards, Card_V*_Tests files, VirtualTableScenario,
  test framework methods, or asks to test card behavior, site behavior, or errata changes.
  Also trigger when debugging test failures in the gemp-lotr-server test suite.
user-invocable: false
---

# Gemp-LotR Test Framework Guide

For the full VTS API reference (every method signature, all overloads), read `${CLAUDE_SKILL_DIR}/references/vts-api.md`.

## VirtualTableScenario Basics

Tests use `VirtualTableScenario` to simulate games. Constructor takes:

```java
new VirtualTableScenario(
    cardsMap,    // HashMap<String, String> — named cards (both FP + Shadow auto-sorted by side)
    sitesMap,    // HashMap<String, String> — "site1" through "site9" adventure deck
    frodoVariant, // e.g. VirtualTableScenario.FOTRFrodo
    ringVariant   // e.g. VirtualTableScenario.RulingRing or "9_1" for Binding Ring
);
```

Card IDs: official sets 1-19, errata 50-69, V-sets 101/102/103 prefix (e.g. `103_126`).
Card naming: use descriptive variable names, NOT `card` (that's the auto-generated placeholder).

### Canned Constants
- `VirtualTableScenario.FOTRFrodo` — Frodo, str 3, vit 2
- `VirtualTableScenario.RulingRing` — "1_2", str +1, optional wound-to-burden on skirmish loss
- `VirtualTableScenario.FellowshipSites` — set 1 sites 1-9
- `VirtualTableScenario.KingSites` — sets 7-10 sites 1-9
- `VirtualTableScenario.ShadowsSites` — Shadows-era sites

## Cheating vs Legal Play
The test rig has three main uses: interfacing with Gemp (replacing the visual GUI), inspecting the internals of the game state, and manipulating the internals of the game state.  The first is concerned with "legal play", while the last is about "cheating".

When setting up tests, we invariably utilize cheating to quickly and clearly establish the scenario--utilizing scn.MoveCardsToSupportArea() teleports those cards into the appropriate zone and does not check for paid costs or fulfilled requirements; these are illegal plays but given that the test at hand is not specifically testing for the correctness of those costs or requirements, this doesn't erode the test integrity.

In general, we try to quarantine cheating to before the scn.StartGame() call, but there may be situations where it is called for after that when used sparingly.

## Critical Gotchas

These are the most common sources of test failures. Read carefully.

### 1. Decisions are batched — no mid-decision updates
Gemp queries available actions ONCE when a decision is presented. If you cheat a card in while a
decision is pending, the choices do NOT retroactively update. To test play conditions becoming
satisfied, use actual game actions (`FreepsPlayCard`, `ShadowPlayCard`) that complete the current
decision first, then check the new state.

### 2. Shadow cards must be cheated in AFTER SkipToSite (when applicable)
When utilizing SkipToSite(N) to traverse the adventure path, this procedure will result in minions being discarded before final arrival.  If you ever use SkipToSite, you must ensure that Shadow minions and their attachments are cheated shadow cards in AFTER `SkipToSite(n)`:
```java
scn.StartGame();
scn.SkipToSite(7);
scn.MoveMinionsToTable(runner);        // AFTER SkipToSite
scn.AttachCardsTo(runner, paleBlade);  // AFTER SkipToSite
```

However if you do not utilize SkipToSite, you may disregard this point.

### 3. Item class restriction
One item per class per bearer (one hand weapon, one pipe, etc.). Cheating via `AttachCardsTo`
bypasses such play restrictions, but the bearer becomes ineligible for additional items of the same
class through normal play. If you put a Pipe on Frodo, he can't be targeted by another Pipe's
play action (but you could "stuff" him with several pipes via cheating).

### 4. SkipToSite swaps player roles
Each site traversal swaps FP/Shadow roles. After an even number of traversals, roles return to
normal. This is handled internally but can cause confusion with assertions.

### 5. Site 9 has no Fellowship phase
At site 9, the game skips straight to Shadow phase. "You either win or you die." Check Maneuver
actions (not Fellowship) for testing card abilities at site 9.

### 6. No minions = skip to Regroup
If no minions are spottable (including if all are hindered), maneuver, archery, and assignment and skirmish phases are
skipped entirely.

### 7. Abilities and triggers with 0 or 1 valid targets auto-resolve
In situations where there is only 1 valid target for an action, that target will be selected automatically for the action if it is required.  If there are 0 valid targets for an action, then that action will be skipped entirely (optional or required).

This means that if you want to explicitly confirm the existence or nonexistence of a valid target in a prompt, you must provide 2 or more targets lest the system auto-select the only option for you.

### 8. Exert is not wound
Exerting requires the character to not be exhausted (wounds < vitality - 1). Wounding has no
such restriction. A character at 1 remaining vitality can be wounded but not exerted.

### 9. Uniqueness is deck-building only
Not enforced in tests. You can cheat duplicate uniques onto the table.

### 10. Game ends conditions
When Frodo dies or is corrupted (burdens >= resistance), the game ends immediately. This can
prevent reaching later phases (e.g., regroup after a lethal skirmish at site 9). Plan test
assertions to occur BEFORE the game-ending event.

### 11. Action Procedure
The Fellowship phase is Free-Peoples-player-only, while the Shadow phase is Shadow-player-only, but all other phases have a back-and-forth procedure where the Free Peoples player is offered the chance to perform actions, then the Shadow player, which procedes back and forth like this until both players pass.

This means that if you are testing Shadow actions you will likely need to remember to pass the Free Peoples action at the start of a given phase.  If using SkipToPhase this will be handled for you.

## Quick API Reference

For full method listings with all overloads, see `${CLAUDE_SKILL_DIR}/references/vts-api.md`.

### Card Retrieval
```java
scn.GetFreepsCard("name")   // FP card by HashMap key
scn.GetShadowCard("name")   // Shadow card by HashMap key
scn.GetRingBearer()          // The Ring-bearer (Frodo)
scn.GetRing()                // The One Ring card
scn.GetFreepsSite(n)         // Site n from FP adventure deck
```

### Zone Manipulation (Pre-Game Setup)
```java
scn.MoveCompanionsToTable(card1, card2, ...)
scn.MoveMinionsToTable(card1, card2, ...)
scn.AttachCardsTo(bearer, card1, card2, ...)
scn.MoveCardsToSupportArea(card1, card2, ...)
scn.MoveCardsToHand(card1, card2, ...)
scn.MoveCardsToDiscard(card1, card2, ...)
```

### Game State Queries
```java
scn.GetTwilight()                    // Current twilight pool
scn.SetTwilight(amount)              // Override twilight
scn.GetStrength(card)                // Current strength (with modifiers)
scn.GetWoundsOn(card)                // Wound count
scn.GetBurdens()                     // Burden count
scn.GetCurrentSiteNumber()           // Current site (1-9)
scn.GetCurrentPhase()                // Current phase enum
scn.HasKeyword(card, Keyword.X)      // Boolean keyword check
```

### Actions and Decisions
```java
scn.FreepsActionAvailable(card)          // "Use [card name]" available?
scn.FreepsPlayAvailable(card)            // "Play [card name]" available?
scn.FreepsPlayCard(card)                 // Play from hand
scn.FreepsUseCardAction(card)            // Activate an ability
scn.FreepsHasOptionalTriggerAvailable()  // Optional trigger pending?
scn.FreepsAcceptOptionalTrigger()        // Accept
scn.FreepsDeclineOptionalTrigger()       // Decline
```

### Card Choice Validation
```java
scn.FreepsHasCardChoiceAvailable(card)              // Card is selectable
scn.FreepsHasCardChoicesAvailable(card1, card2)      // ALL are selectable
scn.FreepsHasCardChoicesNotAvailable(card1, card2)   // NONE are selectable
scn.FreepsChooseCard(card)                           // Select a card
```

### Phase Flow
```java
scn.SkipToPhase(Phase.MANEUVER)         // Auto-pass to target phase
scn.PassCurrentPhaseActions()           // Both players pass
scn.FreepsPassCurrentPhaseAction()      // Just FP passes
scn.ShadowPassCurrentPhaseAction()      // Just Shadow passes
scn.SkipToSite(n)                       // Traverse to site n
scn.FreepsChooseToMove()                // Move again at regroup
scn.FreepsChooseToStay()                // Stay at current site
```

### Skirmish Flow Pattern
```java
scn.PassAssignmentActions();
scn.FreepsAssignAndResolve(companion, minion);
scn.PassSkirmishActions();
scn.FreepsDeclineOptionalTrigger();   // Ruling Ring
```

### Decision Bookending
```java
// After actions complete, assert we're back at the expected phase:
scn.AwaitingFellowshipPhaseActions()
scn.AwaitingShadowPhaseActions()
scn.AwaitingFreepsManeuverPhaseActions()
// ... etc for all phases
```

### Assertions
```java
assertEquals(expected, actual)
assertTrue(condition)
assertFalse(condition)
assertAttachedTo(card, bearer)
assertInZone(Zone.HAND, card)
assertInDiscard(card)
assertInHand(card)
assertInPlay(card)
```

## Commonly Used Test Cards

| Card | ID | Stats | Notes |
|------|----|-------|-------|
| FOTR Frodo | (auto) | str 3, vit 2 | Ring-bearer |
| Ruling Ring | 1_2 | str +1 | Optional wound-to-burden trigger |
| Binding Ring | 9_1 | str +1, vit +1 | 3 effects all check ringIsActive |
| Goblin Runner | 1_178 | str 5, vit 1 | Basic shadow minion |
| Aragorn, Ranger | 1_89 | str 8, vit 4 | Basic FP companion |
| Uruk Savage | 1_151 | str 5, vit 3, damage+1 | Uruk-hai for Reach cost |
| Old Toby | 1_305 | Pipeweed, support area | On-play trigger (decline with FreepsDeclineOptionalTrigger) |

## RTMD (Race to Mount Doom) Meta-Site Testing

For full RTMD constructor overloads, see `${CLAUDE_SKILL_DIR}/references/vts-api.md`.

### Constructor
Meta-site modifiers are passed as blueprint IDs to VirtualTableScenario:
```java
new VirtualTableScenario(cards, sites, frodo, ring, "91_3", null);      // Freeps has mod
new VirtualTableScenario(cards, sites, frodo, ring, null, "91_3");      // Shadow has mod
```

Do NOT manually cheat RTMD modifiers via `MoveCardsToSupportArea`. They are spawned by
the game engine during init.

### Bid Handler Lambda
For modifiers that affect bidding:
```java
new VirtualTableScenario(cards, sites, frodo, ring, "91_18", null,
    scn -> {
        scn.FreepsDecided("6");
        scn.ShadowDecided("0");
        scn.FreepsDecided("0");
    }
);
```

### Two-Scenario Pattern
```java
protected VirtualTableScenario GetFreepsScenario() {
    return new VirtualTableScenario(cards, sites, frodo, ring, "91_X", null);
}
protected VirtualTableScenario GetShadowScenario() {
    return new VirtualTableScenario(cards, sites, frodo, ring, null, "91_X");
}
```

## Creating New Game Modifiers (ModifierEffect Pipeline)

When a card needs to modify a normally-static game value:

1. **`ModifierEffect` enum** — Add new entry
2. **`Modifier` interface** — Add new method with default
3. **`AbstractModifier`** — Override with default return
4. **`DelegateModifier`** — Delegate to wrapped modifier
5. **New modifier class** — Stores evaluator, returns result
6. **`ModifiersQuerying` interface** — Add query method
7. **`ModifiersLogic`** — Implement query: iterate modifiers, sum results
8. **Game process** — Replace hard-coded value with modifier query
9. **HJSON bridge class** — Reads HJSON fields, creates modifier instance
10. **`ModifierSourceFactory`** — Register with lowercase key

## Card Verification Discipline

**ALWAYS verify card properties before using them in tests.** Do not assume a card has a particular
ability, keyword, or card type based on its name. Read the actual HJSON definition. Common pitfalls:
- "Blade Tip" is a **condition**, not a possession
- Not all weapons grant damage bonuses
- Not all named characters have maneuver/archery/regroup abilities
- A card's phase actions may differ between errata versions

Every card in a test deck should serve a specific, verified purpose.
