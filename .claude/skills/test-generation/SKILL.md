---
name: test-generation
description: >
  Guide for designing and writing unit tests for Gemp-LotR HJSON card definitions using
  VirtualTableScenario. Use this skill when creating test files for new or modified cards,
  designing test scenarios, choosing test helper cards, or deciding what aspects of a card
  need test coverage. Trigger when: the user asks to write tests for a card, asks what needs
  testing, asks about test design patterns, or is implementing a Card_V*_Tests file.
  Also trigger when reviewing or improving existing card tests.
---

# Generating Unit Tests for HJSON Cards

How to design and write tests for a card's HJSON definition. Bridges the VTS API with the card's HJSON structure.

For VTS API method signatures, see the `gemp-testing` skill. For HJSON field details, see the `hjson-reference` skill. For game rules context, see the `game-mechanics` skill.

---

## The Testing Mindset

A card's HJSON defines *what* should happen. The test verifies *that* it happens — and that it *doesn't* happen when it shouldn't. Every effect, filter, trigger, and requirement is a claim that needs evidence.

Before writing any test code: **read the card's HJSON definition**. Not just the game text — the actual HJSON. Inspect filters, triggers, requirements, selectors. The HJSON tells you exactly what needs testing.

---

## What Needs Testing

For every card, decompose its HJSON `effects` array. Each effect entry generates one or more test methods.

### Per-Effect Checklist

| HJSON Construct | What to Test |
|-----------------|-------------|
| Filter (e.g., `culture(gondor),companion`) | Valid targets pass, invalid targets rejected |
| Selector (`choose` vs `all`) | Correct number of targets affected |
| Requirement (`requires: [canSpot...]`) | Ability available when met, unavailable when not |
| Cost (`cost: [exert...]`) | Cost actually paid (wound appears, card discarded, etc.) |
| Effect (`effect: [wound...]`) | Effect actually applied to correct target |
| Trigger (`trigger: {type: played}`) | Fires on correct event, doesn't fire on wrong event |
| Value (`amount: {type: forEachYouCanSpot}`) | Scales correctly with board state |
| Duration (`until: endOfTurn`) | Effect persists through phase, expires at boundary |
| Limit (`limitPerPhase: 1`) | Works once, blocked on second attempt |
| `optional: true/false` | Optional triggers offer choice; mandatory ones don't |

### Stats Test (Always Present)

Every card test file starts with a `StatsAreCorrect` test that verifies the blueprint:

```java
@Test
public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
    var scn = GetScenario();
    var card = scn.GetFreepsCard("card");

    assertEquals("Card Title", card.getBlueprint().getTitle());
    assertEquals("Subtitle", card.getBlueprint().getSubtitle());
    assertTrue(card.getBlueprint().isUnique());
    assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
    assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
    assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
    assertEquals(Race.MAN, card.getBlueprint().getRace());
    assertEquals(4, card.getBlueprint().getTwilightCost());
    assertEquals(8, card.getBlueprint().getStrength());
    assertEquals(4, card.getBlueprint().getVitality());
    assertEquals(6, card.getBlueprint().getResistance());
    // keywords, signet, site number, itemclass as applicable
}
```

This test uses `VirtualTableScenario.FindCard()` or a constructed scenario — either works since it only reads the blueprint.

---

## Test Design Principles

### 1. Pre-Check, Action, Post-Check

Never just perform an action and assert the result. Assert the *before* state too. This documents intent and catches false positives.

```java
// BAD: Only checks result
scn.FreepsUseCardAction(card);
assertEquals(1, scn.GetWoundsOn(aragorn));

// GOOD: Documents the full transition
assertEquals(0, scn.GetWoundsOn(aragorn));    // pre-check
scn.FreepsUseCardAction(card);                // action
assertEquals(1, scn.GetWoundsOn(aragorn));    // post-check
```

If the action exerts, confirm 0 wounds before and 1 wound after. If it heals, wound the character first, confirm the wound, then heal, then confirm removal. The pre-check IS the documentation.

### 2. Bookend with Phase Assertions

After performing an action and all its knock-on effects, assert that the game has returned to the expected phase and is awaiting the correct player's input. This catches dangling decisions, unresolved triggers, and unexpected game state.

```java
scn.FreepsUseCardAction(card);
scn.FreepsChooseCard(target);
// ... resolve any sub-decisions ...
assertTrue(scn.AwaitingFreepsManeuverPhaseActions());  // bookend
```

If you're in the skirmish phase, assert `AwaitingFreepsSkirmishPhaseActions()`. If the action should end the phase, assert the *next* phase. This is the most common source of test failures when a card has unexpected triggers or responses — the bookend catches them immediately.

`AwaitingXYPhaseActions()` is not only for bookending after actions — it also serves as a "nothing happened" anchor. When testing that a trigger did NOT fire or an effect was NOT applied, assert the expected phase to confirm the game moved on without pausing for a decision:

```java
// Testing that a trigger does NOT fire (e.g., owner-gating blocks it)
scn.FreepsPassCurrentPhaseAction();
scn.ShadowPassCurrentPhaseAction();
// If the trigger had fired, we'd be stuck on a wound choice — instead:
assertTrue(scn.AwaitingFreepsRegroupPhaseActions());  // confirms nothing happened
```

### 3. Combine Positive and Negative Cases

Don't write separate tests for "works on valid target" and "doesn't work on invalid target." Put both in the same scenario and verify both simultaneously.

```java
// Testing a filter that targets Dwarves
var dwarf = scn.GetFreepsCard("dwarf");    // valid
var elf = scn.GetFreepsCard("elf");        // invalid
scn.MoveCompanionsToTable(dwarf, elf);

scn.FreepsUseCardAction(card);
assertTrue(scn.FreepsHasCardChoiceAvailable(dwarf));
assertFalse(scn.FreepsHasCardChoiceAvailable(elf));
```

One test, two assertions, complete filter coverage. If the filter targets a specific class (e.g., companions), include an alternate class (e.g., ally or minion) to confirm exclusion. If it targets a race (e.g., Dwarves), include a different race (e.g., Elves).

### 4. N+1 Targets for Choice Validation

Gemp auto-resolves choices when there are exactly N valid targets for an N-target selection. This means you can't verify filter specificity if there's only one valid target — the engine skips the choice entirely.

**Always provide at least one more valid target than the minimum**, so the choice is presented to the player and you can inspect it with `HasCardChoiceAvailable` / `HasCardChoiceNotAvailable`.

```java
// BAD: Testing "choose a Dwarf" with only 1 Dwarf in play
var gimli = scn.GetFreepsCard("gimli");
scn.MoveCompanionsToTable(gimli);
scn.FreepsUseCardAction(card);
// Gimli is auto-chosen — no chance to verify filter

// GOOD: Two Dwarves → choice is presented, Elf proves filter works
var gimli = scn.GetFreepsCard("gimli");
var dwarfGuard = scn.GetFreepsCard("guard");
var legolas = scn.GetFreepsCard("legolas");  // Elf — should be excluded
scn.MoveCompanionsToTable(gimli, dwarfGuard, legolas);

scn.FreepsUseCardAction(card);
assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));      // valid
assertTrue(scn.FreepsHasCardChoiceAvailable(dwarfGuard)); // valid
assertFalse(scn.FreepsHasCardChoiceAvailable(legolas));   // invalid — filter works
scn.FreepsChooseCard(gimli);
```

This applies to ALL choice-based effects: wounds, heals, discards, plays, assigns. If your effect says "choose N cards matching filter," put N+1 valid cards and at least 1 invalid card in the scenario.

### 4b. Effect Fizzle: Costs Gate, Effects Attempt

For any action to be usable, requirements must be met and costs must be payable — but effects only need to be *attempted*. If there is no valid target for the effect, or only part of the effect can be accomplished, the effect "fizzles" but the action still resolves (costs are still paid).

This means you **cannot** test "action unavailable because the effect has no valid target." The action is available as long as the cost is payable. Instead, test that the effect fizzles: activate the action, pay the cost, then assert that the game returns to phase actions with the effect unapplied.

```java
// Exert Arwen (Aragorn signet), but only Gimli (Gandalf signet) could be healed — fizzles
scn.FreepsUseCardAction(mod);
scn.FreepsChooseCard(arwen);

// Effect fizzles — back to fellowship actions, cost paid but no heal
assertTrue(scn.AwaitingFellowshipPhaseActions());
assertEquals(1, scn.GetWoundsOn(arwen));  // cost was paid
assertEquals(1, scn.GetWoundsOn(gimli));  // not healed — wrong signet
```

### 5. Progressive Act-Assert for Multi-Step Effects

If a card triggers after several things happen, you don't need separate tests for each step. Write one test that progressively sets up and asserts at each stage, since each step's setup is congruent with the next step's precondition.

```java
// Card triggers "each time a companion takes a wound"
// Test: first wound triggers, second wound triggers again
assertEquals(0, scn.GetWoundsOn(companion));
scn.ShadowUseCardAction(woundSource);
scn.ShadowChooseCard(companion);
assertEquals(1, scn.GetWoundsOn(companion));
// assert trigger fired, resolve it...

// Second wound — still triggers (no separate test needed)
scn.ShadowUseCardAction(woundSource);
scn.ShadowChooseCard(companion);
assertEquals(2, scn.GetWoundsOn(companion));
// assert trigger fired again...
```

Each further assertion builds on the state established by prior steps. This tests accumulation, idempotency, and repeated-fire behavior in a single method.

### 6. Arrange (Cheat) vs Act (Legitimate)

The Arrange-Act-Assert triad maps directly to VTS:
- **Arrange** = cheating (`MoveCompanionsToTable`, `AttachCardsTo`, `AddWoundsToChar`, `SetTwilight`, etc.)
- **Act** = legitimate game actions (`FreepsPlayCard`, `FreepsUseCardAction`, `SkipToPhase`, etc.)
- **Assert** = queries and assertions

**Never cheat when you want to test an action's impact.** If you're testing that an ability exerts a character, don't call `AddWoundsToChar` — use the ability and verify the wound appears. Cheating is for setting up preconditions only.

Conversely, **always cheat for preconditions** rather than playing cards legitimately. If you need Aragorn on the table, don't play him from hand — cheat him to the table. Tests should only exercise the mechanic under test.

```java
// Arrange: cheat the setup
scn.MoveCompanionsToTable(aragorn);
scn.AddWoundsToChar(aragorn, 2);   // pre-wound for heal test
scn.StartGame();

// Pre-check: confirm arranged state
assertEquals(2, scn.GetWoundsOn(aragorn));

// Act: use the card legitimately
scn.FreepsUseCardAction(healCard);

// Assert: verify behavior
assertEquals(1, scn.GetWoundsOn(aragorn));  // healed 1
```

### 7. Pass Actions for Non-Relevant Players

The game engine alternates between players in most phases. If you're testing a Shadow card during the maneuver phase, Freeps gets to act first. You must pass for them:

```java
scn.SkipToPhase(Phase.MANEUVER);
scn.FreepsPassCurrentPhaseAction();   // FP goes first in maneuver
scn.ShadowUseCardAction(shadowCard);  // Now Shadow can act
```

Forgetting this is one of the most common test failures. The action procedure for alternating phases (Maneuver, Archery, Skirmish, Regroup) always starts with the Free Peoples player.

### 8. Dismiss Revealed Cards Before Choosing

When a card effect searches the draw deck (or reveals cards from any hidden zone), the player is first shown the full set of revealed cards. This reveal popup must be dismissed before the player can make a selection from those cards. Use `FreepsDismissRevealedCards()` or `ShadowDismissRevealedCards()` before calling `FreepsChooseCardBPFromSelection()` or similar.

```java
// Card searches the draw deck for a companion
scn.FreepsUseCardAction(deckSearchCard);
scn.FreepsDismissRevealedCards();                    // dismiss the reveal popup
scn.FreepsChooseCardBPFromSelection(targetCard);     // now choose from the revealed cards
```

Forgetting the dismiss step will cause the test to fail because the game is waiting on the reveal acknowledgment, not the selection decision.

### 9. Reference Existing Tests for Similar Cards

Before writing tests from scratch, search for tests of cards with similar effects. If you're testing a "wound prevention response," look at how other wound-prevention cards are tested. The pattern may already be established.

```
// Search for tests of similar mechanics
Grep for: "aboutToTakeWound" or "preventWound" in test files
Grep for: the specific effect type you're implementing
```

---

## Choosing Test Cards

The single most important test design decision is which helper cards to include. Bad choices inject unwanted side effects that pollute your test.

### The Golden Rule

**Read the HJSON for every card you put in a test.** Don't assume a card is "simple" based on its name. Look for:
- Keywords that affect game flow (Archer, Fierce, Damage, Enduring, Lurker)
- Triggers that fire automatically (on play, on wound, on phase start)
- Modifiers that alter stats or rules passively
- Play requirements that constrain setup

### Recommended Clean Helper Cards

**Companions (Free Peoples):**

| Card | Title | Culture | Race | Str/Vit/Res | Tw | Gotchas |
|------|-------|---------|------|-------------|-----|---------|
| 1_7 | Dwarf Guard | Dwarven | Dwarf | 4/2/6 | 1 | ToPlay: spot another Dwarf. Low stats, cheap. |
| 1_89 | Aragorn, Ranger of the North | Gondor | Man | 8/4/6 | 4 | **Ranger** keyword. Maneuver: exert for Defender+1. Very common despite not being clean. High vitality makes him hard to kill accidentally. |
| 1_50 | Legolas, Greenleaf | Elven | Elf | 6/3/6 | 2 | **ARCHER** — avoid unless testing archery. Has archery-phase ability. Will block `SkipToPhase(ARCHERY)` unless wounds are handled. |
| 1_13 | Gimli, Son of Gloin | Dwarven | Dwarf | 6/3/6 | 2 | **Damage +1** — avoid unless testing damage. Has skirmish exert ability. |

**Minions (Shadow):**

| Card | Title | Culture | Race | Str/Vit | Tw | Site | Gotchas |
|------|-------|---------|------|---------|-----|------|---------|
| 1_177 | Goblin Patrol Troop | Moria | Orc | 13/3 | 6 | 4 | **CLEANEST MINION** — zero effects, no keywords, no triggers. High strength means it wins most skirmishes. |
| 1_178 | Goblin Runner | Moria | Orc | 5/1 | 1 | 3 | Trigger: adds (2) twilight on play. Very common despite side effect. Low cost makes it easy to fit in scenarios. |

### Why Clean Cards Matter

Consider testing a condition that says "Each time a minion takes a wound, add (1)":
- If you use a minion with **Enduring**, it won't die from overwhelm — unexpected survival
- If you use a minion with **Fierce**, it gets reassigned — unexpected second skirmish
- If you use a minion with a **wound trigger**, it fires alongside your card's trigger — confusing interaction
- If you use an **Archer** companion, archery wounds happen before you expect — skipped decisions

Use 1_177 (Goblin Patrol Troop) and the test is about your card only.

### When "Unclean" Cards Are Correct

Sometimes you *need* a specific trait:
- Testing "while bearer is a Ranger" → use 1_89 (Aragorn has Ranger)
- Testing archery total modification → use 1_50 (Legolas is an Archer)
- Testing damage bonus interaction → use 1_13 (Gimli has Damage +1)

The rule: use clean cards by default, unclean cards only when the test *requires* that specific trait.

### Finding Cards for Specific Traits

When you need a card with a particular race, culture, keyword, or type:
1. **Read the HJSON** for candidate cards — don't guess from the name
2. Check for unwanted `effects`, `keywords`, `type` entries
3. Prefer cards with fewer/simpler effects
4. Note any keywords that come with the card (Ranger, Archer, Damage, etc.)

Common needs and where to look:
- **Hobbit companion**: Check set01-Shire.hjson (Merry, Pippin, Sam variants)
- **Uruk-hai minion**: Check set01-Isengard.hjson
- **Nazgul**: Check set01-Wraith.hjson
- **Elf ally**: Check set01-Elven.hjson
- **Hand weapon**: Check set01 possessions by culture
- **Clean condition**: Look for support-area conditions with simple effects

---

## Test Structure by Effect Type

### Activated (Phase Action)

```
"Maneuver: Exert self to make a companion strength +3 until the regroup phase."
```

Tests needed:
1. **Availability**: Action appears during correct phase
2. **Unavailability**: Action does NOT appear during other phases (or when requirements unmet)
3. **Cost verification**: Exertion actually happens (pre/post wound check)
4. **Effect verification**: Strength actually changes by correct amount
5. **Filter specificity**: Only valid targets are chooseable (with N+1 valid targets)
6. **Duration**: Effect persists through skirmish, expires at regroup
7. **Limit**: If `limitPerPhase` or `limitPerTurn`, second use is blocked

```java
@Test
public void ManeuverAbilityExertsAndBoostsStrength() {
    var scn = GetScenario();
    var card = scn.GetFreepsCard("card");
    var comp1 = scn.GetFreepsCard("comp1");
    var comp2 = scn.GetFreepsCard("comp2");
    var minion = scn.GetShadowCard("minion");

    scn.MoveCompanionsToTable(card, comp1, comp2);
    scn.StartGame();
    scn.MoveMinionsToTable(minion);
    scn.SkipToPhase(Phase.MANEUVER);

    // Pre-check
    assertTrue(scn.FreepsActionAvailable(card));
    assertEquals(0, scn.GetWoundsOn(card));
    assertEquals(7, scn.GetStrength(comp1));

    // Act
    scn.FreepsUseCardAction(card);
    scn.FreepsChooseCard(comp1);

    // Post-check
    assertEquals(1, scn.GetWoundsOn(card));    // cost paid
    assertEquals(10, scn.GetStrength(comp1));   // +3 applied
    assertEquals(7, scn.GetStrength(comp2));    // other companion unchanged
    assertTrue(scn.AwaitingFreepsManeuverPhaseActions());  // bookend
}
```

### Modifier (Passive)

```
"Bearer is strength +2 while bearer is a Dwarf."
```

Tests needed:
1. **Bonus applies**: Attach to Dwarf, verify strength includes bonus
2. **Bonus doesn't apply**: Attach to non-Dwarf, verify no bonus
3. **Dynamic update**: If condition can change mid-game, bonus adjusts

Modifiers require no action — just set up the board state and check. Put both valid and invalid bearers in the same test when possible, or use separate `GetScenario()` setups if attachment constraints prevent it.

### Trigger (Each Time)

```
"Each time you play a [moria] weapon, add (1)."
```

Tests needed:
1. **Trigger fires on matching event**: Play correct card type, effect happens
2. **Trigger doesn't fire on non-matching event**: Play wrong card type, no effect
3. **Optional vs mandatory**: If `optional: true`, verify choice is presented; if false, effect just happens
4. **Multiple fires**: Trigger works repeatedly, not just once

### Response (About To)

```
"Response: If a companion is about to take a wound, discard this to prevent that wound."
```

Tests needed:
1. **Response offered**: When matching card about to be wounded, response is available
2. **Prevention works**: Wound is actually prevented (pre/post check)
3. **Cost paid**: Card is discarded (assertInDiscard)
4. **Filter specificity**: Doesn't trigger for non-matching targets (e.g., not for minions)

### ToPlay / ExtraCost

```
"To play, spot 2 Elves."  /  "To play, exert a [gondor] companion."
```

Tests needed:
1. **Playable when met**: Card appears as playable with requirements satisfied
2. **Unplayable when not met**: Card does NOT appear as playable without requirements
3. **ExtraCost paid**: For ExtraCost, the exertion/discard actually happens upon playing

---

## Duration Testing

When an effect has a duration (`until`), test both sides of the boundary:

```java
// "Make X strength +3 until the regroup phase"
scn.FreepsUseCardAction(card);
assertEquals(10, scn.GetStrength(target));  // bonus active in maneuver

// ... advance through archery, assignment, skirmish ...
assertEquals(10, scn.GetStrength(target));  // STILL active during skirmish

scn.SkipToPhase(Phase.REGROUP);
assertEquals(7, scn.GetStrength(target));   // bonus GONE at regroup
```

Common duration boundaries:
- `end(current)` → expires at end of the phase it was used in
- `start(Regroup)` → "until the regroup phase" — active through skirmish, gone at regroup start
- `endOfTurn` → survives all phases, gone next turn (test by moving to next turn if needed)

---

## Limit Testing

```java
// "Limit 1 per phase"
assertTrue(scn.FreepsActionAvailable(card));
scn.FreepsUseCardAction(card);
// ... resolve ...
assertFalse(scn.FreepsActionAvailable(card));  // blocked on second use
```

For `limitPerTurn`, verify the action is unavailable after use even in a subsequent phase of the same turn.

---

## Scaling Value Testing

For "strength +1 for each X you spot":

Test at least two different counts to confirm scaling, not just a fixed bonus.

```java
// With 2 matching cards in play
scn.MoveCompanionsToTable(match1, match2, nonMatch);
scn.StartGame();
assertEquals(baseStr + 2, scn.GetStrength(card));  // scales with count
```

If the HJSON uses `limit`, test the cap too — provide more matches than the limit and confirm the value doesn't exceed it.

---

## Skirmish Testing Pattern

Many cards interact with skirmishes. Standard flow:

```java
scn.SkipToPhase(Phase.ASSIGNMENT);
scn.PassAssignmentActions();
scn.FreepsAssignAndResolve(companion, minion);

// Skirmish actions phase — play events or use abilities here
scn.PassSkirmishActions();  // both pass → skirmish resolves, wounds applied

// Handle Ruling Ring optional trigger if using default ring
scn.FreepsDeclineOptionalTrigger();

// Now assert skirmish results
```

If testing a skirmish event card, play it between `FreepsAssignAndResolve` and `PassSkirmishActions`.

---

## Common Test Smells

### Smell: No Invalid Target Test
If your card targets "a Dwarf" and your test only has Dwarves in play, you haven't tested the filter. Add an Elf and assert it's not chooseable.

### Smell: Only One Valid Target
If your card says "choose a companion" and there's only one companion (plus the Ring-bearer who might be auto-excluded), the engine auto-selects. You can't call `HasCardChoiceAvailable` on an auto-resolved decision. Add another companion so the choice is real and inspectable.

### Smell: Missing Phase Bookend
If you perform an action and just assert the effect without checking `AwaitingXPhaseActions()`, you don't know if a trigger is dangling or a response window is unexpectedly open.

### Smell: Cheating Where You Should Act
If you `AddWoundsToChar()` to "test" that an exertion happened, you're not testing the exertion — you're just placing wounds. Let the ability exert the character, then check the wound.

### Smell: Testing Implementation Instead of Behavior
Don't test that the HJSON produces a specific Java object. Test that the card *behaves* correctly in a game scenario. The HJSON is the implementation; the behavior is the contract.

---

## Test File Template

```java
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_vXX;

import com.gempukku.lotro.cards.unofficial.pc.vsets.VSetTests;
import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_VXX_NNN_Tests extends VSetTests {

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
            new HashMap<>() {{
                put("card", "XX_NNN");      // card under test
                put("comp1", "1_89");        // Aragorn — helper
                put("comp2", "1_7");         // Dwarf Guard — helper
                put("minion", "1_177");      // Goblin Patrol Troop — clean minion
            }}
        );
    }

    @Test
    public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();
        var card = scn.GetFreepsCard("card");

        // Verify all blueprint fields from HJSON
    }

    @Test
    public void AbilityNameDescribesWhatIsBeingTested()
            throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();
        var card = scn.GetFreepsCard("card");
        var comp1 = scn.GetFreepsCard("comp1");
        var comp2 = scn.GetFreepsCard("comp2");
        var minion = scn.GetShadowCard("minion");

        // Arrange
        scn.MoveCompanionsToTable(card, comp1, comp2);
        scn.StartGame();
        scn.MoveMinionsToTable(minion);
        scn.SkipToPhase(Phase.MANEUVER);

        // Pre-check
        assertEquals(0, scn.GetWoundsOn(card));
        assertEquals(7, scn.GetStrength(comp1));

        // Act
        scn.FreepsUseCardAction(card);
        scn.FreepsChooseCard(comp1);

        // Post-check
        assertEquals(1, scn.GetWoundsOn(card));
        assertEquals(10, scn.GetStrength(comp1));
        assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
    }
}
```

---

## Naming Tests

Test method names should describe the *behavior* being verified, not the implementation detail:

```java
// GOOD — describes behavior
public void ExertsSelfAndMakesCompanionStrengthPlus3()
public void DoesNotTriggerOnNonMoriaWeapons()
public void BonusExpiresAtStartOfRegroupPhase()
public void CannotActivateIfAlreadyExhausted()
public void TargetsMinionButNotCompanion()
public void StrengthBonusScalesWithOrcCount()

// BAD — describes implementation
public void TestModifyStrengthSubaction()
public void TestTriggerWithFilter()
public void CheckRequirements()
public void Test1()
```
