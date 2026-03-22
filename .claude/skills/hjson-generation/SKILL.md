---
name: hjson-generation
description: >
  Guide for translating English LotR TCG game text into HJSON card definitions. Bridges game mechanics
  understanding with the HJSON DSL. Use this skill when creating new HJSON card definitions from
  English card text, converting game text to HJSON effects, or figuring out the correct HJSON
  structure for a specific game text pattern. Trigger when: the user asks to implement a card,
  translate game text to HJSON, or create a new card definition from English text.
---

# Translating Game Text to HJSON

How to read English card text and produce the correct HJSON structure. This bridges game mechanics and the HJSON DSL reference.

For HJSON field details, see the `hjson-reference` skill. For game rules context, see the `game-mechanics` skill.

---

## The Translation Pipeline

Read game text → identify structural pattern → map to effect type → fill in filters/values/subactions.

Every card follows this flow:
1. **Blueprint fields** from the card's physical properties (title, stats, type, race, etc.)
2. **`effects: []`** from the card's game text, one entry per distinct ability/rule

---

## Step 1: Identify the Game Text Pattern

Game text falls into a small number of structural categories. Learn to recognize them instantly.

### Phase Actions → `Activated`
**Signal words:** A bold phase name followed by a colon.
```
"Maneuver: Exert Aragorn to make him strength +3."
"Fellowship: Add (2) to play this card from your discard pile."
```
Structure: `{ type: Activated, phase: [Maneuver], cost: [...], effect: [...] }`

### Passive/Continuous Effects → `Modifier`
**Signal words:** "is" (without a triggering condition), "has", implicit always-on phrasing.
```
"For each other [moria] Orc you can spot, Ancient Chieftain is strength +1."
"Bearer is strength +2 and damage +1."
"While bearer is a Dwarf, bearer is strength +1."
```
Structure: `{ type: Modifier, modifier: { ... } }`

The "while" condition becomes a `requires` on the modifier itself, NOT on the effect wrapper.

### Reactive Triggers → `Trigger`
**Signal words:** "Each time", "When" (followed by a game event happening).
```
"Each time you play a [moria] weapon, add (1)."
"When you play this companion, heal another companion."
```
Structure: `{ type: Trigger, optional: true/false, trigger: { ... }, effect: [...] }`

**Mandatory vs optional:** If the text says "may" or implies player choice, `optional: true`. If it just happens, `optional: false`.

### Responses → `Response` or `ResponseEvent`
**Signal words:** "Response:", "If ... is about to", "about to".
```
"Response: If a [moria] Orc is about to take a wound, discard this to prevent that wound."
"Response: If a companion is about to take a wound, exert a [gondor] companion to prevent it."
```
Permanent cards use `Response`; event cards use `ResponseEvent`.

### Play Requirements → `ToPlay` / `ExtraCost`
**Signal words:** "To play, spot...", "To play, exert..."
```
"To play, spot 2 Hobbits."        → ToPlay (requirement check only)
"To play, exert a [gondor] companion."  → ExtraCost (cost paid)
```
`ToPlay` = spotting/state requirements (no payment). `ExtraCost` = costs beyond twilight.

### Self-Discount → `ModifyOwnCost` / `Discount`
**Signal words:** "The twilight cost of this card is -1 for each...", "This card is twilight cost -1."
```
"The twilight cost of this card is -1 for each Hobbit companion you can spot."
```
Structure: `{ type: ModifyOwnCost, filter: self, amount: { type: forEachYouCanSpot, filter: hobbit,companion, multiplier: -1 } }`

---

## Step 2: Decompose Costs and Effects

Most abilities have **costs** (before the word "to") and **effects** (after "to" or the main action).

```
"Exert Legolas to make him strength +3 until the regroup phase."
 ^^^^^^^^^^^^^^   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   cost: [exert]    effect: [modifyStrength]
```

### Cost vs Effect Placement

Subactions in `cost: []` are mandatory prerequisites; if any can't be performed, the whole ability is blocked. Subactions in `effect: []` resolve after costs are paid.

Key rule: **costs use `choose()` selectors** (player picks), **effects targeting the same card use `memory()`** to refer back.

```hjson
// "Exert a [gondor] companion to heal that companion."
cost: { type: exert, select: choose(culture(gondor),companion), memorize: chosenComp }
effect: { type: heal, select: memory(chosenComp) }
```

### "Self" as Implicit Target

When the card refers to itself ("this card", "this companion", the card's own name), use `self` or `bearer`:
- `self` — the card with the game text
- `bearer` — the character a possession/condition is attached to

```
"Aragorn is strength +1 for each..."  → filter: self  (on Aragorn's own card)
"Bearer is strength +2."              → filter: bearer  (on a possession)
```

---

## Step 3: Map English to Filters

Filters are comma-separated AND chains. Read the English left-to-right.

| English | Filter |
|---------|--------|
| "a [gondor] companion" | `culture(gondor),companion` |
| "another Elf" | `another,elf` |
| "an unwounded [rohan] Man" | `unwounded,culture(rohan),man` |
| "a minion with strength 8 or more" | `minion,minStrength(8)` |
| "a possession or artifact" | `or(possession,artifact)` |
| "a companion bearing a ranged weapon" | `companion,hasAttached(ranged weapon)` |
| "a [moria] Orc not assigned to a skirmish" | `culture(moria),orc,notAssignedToSkirmish` |
| "the Ring-bearer" | `ringBearer` |
| "each wounded minion" | `wounded,minion` (with `all(...)` selector) |

### "Each" vs "A/An"

- "**a** companion" → `choose(companion)` — player picks one
- "**each** companion" → `all(companion)` — affects all matching
- "**a companion for each** Orc you spot" → `choose(companion)` with `count: { type: forEachYouCanSpot, filter: orc }`

### Spotting Requirements

"Spot X" in a requirement context → `{ type: canSpot, filter: X }` in `requires: []`.

"For each X you can spot" in an amount context → `{ type: forEachYouCanSpot, filter: X }` as a value.

These are different HJSON constructs despite similar English.

---

## Step 4: Map English to Values

| English | HJSON Value |
|---------|------------|
| "strength +2" | `amount: 2` |
| "strength +1 for each Orc" | `amount: { type: forEachYouCanSpot, filter: orc }` |
| "strength +1 for each wound on him" | `amount: { type: forEachWound, filter: self }` |
| "twilight cost -1 for each Hobbit" | `amount: { type: forEachYouCanSpot, filter: hobbit, multiplier: -1 }` |
| "strength +3 (limit +6)" | `amount: { type: cardAffectedLimitPerPhase, limit: 6, source: 3 }` or simpler: `amount: { type: forEachYouCanSpot, ..., limit: 6 }` |
| "add (2)" | `amount: 2` (with `AddTwilight`) |
| "remove (3)" | `amount: 3` (with `RemoveTwilight`) |
| "up to 3 wounds" | `amount: "0-3"` (range — player chooses) |

### Limits

"(limit +X)" on strength bonuses → cap the total bonus, not per-invocation. Use `CardAffectedLimitPerPhase` value type when the limit applies per affected card per phase.

"Limit 1 per phase" on an ability → `limitPerPhase: 1` on the effect wrapper.

"Limit once per turn" → `limitPerTurn: 1` on the effect wrapper.

These are different mechanisms — don't confuse them.

---

## Step 5: Map English to Subactions

### Wound/Heal Operations

| English | Subaction |
|---------|-----------|
| "wound a companion" | `{ type: wound, select: choose(companion) }` |
| "wound him twice" | `{ type: wound, select: ..., times: 2 }` |
| "wound 2 companions" | `{ type: wound, select: choose(companion), count: 2 }` |
| "exert Aragorn" | `{ type: exert, select: choose(name(Aragorn)) }` |
| "exhaust a minion" | `{ type: exhaust, select: choose(minion) }` |
| "heal a companion" | `{ type: heal, select: choose(companion) }` |
| "heal 3 wounds from bearer" | `{ type: heal, select: bearer, times: 3 }` |

**`times` vs `count`:** `times` = do the action N times to the same target. `count` = choose N different targets.

### Card Movement

| English | Subaction |
|---------|-----------|
| "discard a condition" | `{ type: discard, select: choose(condition) }` |
| "discard this card" | `{ type: discard, select: self }` |
| "return a minion to hand" | `{ type: returnToHand, select: choose(minion) }` |
| "play a companion from discard" | `{ type: playCardFromDiscard, select: choose(companion) }` |
| "place a card from hand on top of draw deck" | `{ type: putCardsFromHandOnTopOfDeck, select: choose(any), count: 1 }` |

### Ongoing Modifications

| English | Subaction |
|---------|-----------|
| "make him strength +3 until the regroup phase" | `{ type: modifyStrength, select: ..., amount: 3, until: start(Regroup) }` |
| "make him fierce until end of turn" | `{ type: addKeyword, select: ..., keyword: fierce, until: endOfTurn }` |
| "he cannot take wounds" | `{ type: disableWounds, select: ..., until: end(current) }` |

Note: `until: start(Regroup)` vs `until: end(Skirmish)` — read the English carefully. "Until the regroup phase" = lasts through skirmish, ends at start of regroup.

---

## Step 6: Control Flow Patterns

### "You may" → `Optional`
```
"You may exert Aragorn to draw a card."
```
```hjson
{ type: optional, text: "Would you like to exert Aragorn?", effect: [
    { type: costToEffect
      cost: { type: exert, select: choose(name(Aragorn)) }
      effect: { type: drawCards, count: 1 }
    }
]}
```

### "Choose one:" → `Choice`
```
"Choose one: heal a companion; or draw 2 cards."
```
```hjson
{ type: choice, texts: ["Heal a companion", "Draw 2 cards"]
  effects: [
    { type: heal, select: choose(companion) }
    { type: drawCards, count: 2 }
  ]
}
```

### "X may Y to prevent" → `Preventable`
```
"Wound a companion. The Free Peoples player may exert a companion to prevent this."
```
```hjson
{ type: preventable, player: freeps
  text: "Would you like to exert a companion to prevent the wound?"
  cost: { type: exert, select: choose(companion) }
  effect: { type: wound, select: choose(companion) }
}
```

### "If ... then" → `If`
```
"If bearer is a ranger, heal bearer."
```
```hjson
{ type: if
  check: { type: canSpot, filter: bearer,ranger }
  true: { type: heal, select: bearer }
}
```

---

## Common Translation Patterns

### Pattern: Named Character Reference

When game text says "Exert Aragorn to make Aragorn strength +3", the card IS Aragorn, so:
```hjson
cost: { type: exert, select: self }
effect: { type: modifyStrength, select: self, amount: 3 }
```
Use `self`, not `choose(name(Aragorn))`. The latter would work but is unnecessary and fragile.

### Pattern: "For each X" Strength Bonus (Passive)

```
"For each other [moria] Orc you can spot, Ancient Chieftain is strength +1."
```
This is a continuous effect (no phase, no trigger), so it's a `Modifier`:
```hjson
{
    type: modifier
    modifier: {
        type: modifyStrength
        filter: self
        amount: { type: forEachYouCanSpot, filter: another,culture(moria),orc }
    }
}
```

### Pattern: "Each time X, do Y" (Mandatory Trigger)

```
"Each time you play a [moria] weapon, add (1)."
```
```hjson
{
    type: Trigger
    optional: false
    trigger: { type: played, filter: your,culture(moria),weapon }
    effect: { type: addTwilight }
}
```

### Pattern: "Each time" with Memorize

```
"Each time a [sauron] Orc comes into play, exhaust that Orc."
```
```hjson
{
    type: Trigger
    optional: false
    trigger: { type: played, filter: culture(sauron),orc, memorize: playedOrc }
    effect: { type: exhaust, select: memory(playedOrc) }
}
```
The `memorize` on the trigger captures which card triggered it, so the effect can target that specific card.

### Pattern: Strength Bonus with Self-Exclusion via Memory

```
"Skirmish: Make a [moria] Orc strength +1 for each other [moria] Orc you spot (limit +4)."
```
```hjson
{
    type: event
    effect: {
        type: modifyStrength
        select: choose(culture(moria),orc)
        memorize: chosenOrc
        amount: { type: forEachYouCanSpot, filter: not(memory(chosenOrc)),culture(moria),orc, limit: 4 }
    }
}
```
The `memorize` on `modifyStrength` stores the chosen target, then `not(memory(chosenOrc))` excludes it from the count.

### Pattern: Multi-Race Conditional Bonus

```
"Gandalf is strength +1 for each of these races you can spot: Hobbit, Dwarf, Elf, and Man."
```
This is NOT `forEachYouCanSpot` — it's a sum of conditionals (0 or 1 per race):
```hjson
amount: {
    type: sum
    source: [
        { type: Conditional, requires: { type: canSpot, filter: hobbit,companion }, true: 1, false: 0 }
        { type: Conditional, requires: { type: canSpot, filter: dwarf,companion }, true: 1, false: 0 }
        { type: Conditional, requires: { type: canSpot, filter: elf,companion }, true: 1, false: 0 }
        { type: Conditional, requires: { type: canSpot, filter: man,companion }, true: 1, false: 0 }
    ]
}
```

### Pattern: ExtraCost + Multiple Abilities

```
"To play, exert a [gondor] companion. Plays to your support area.
 Each [sauron] Orc comes into play exhausted. Skip the archery phase.
 Discard this condition during the regroup phase."
```
This produces FOUR separate effect entries:
```hjson
effects: [
    { type: extraCost, cost: { type: exert, select: choose(culture(gondor),companion) } }
    { type: Trigger, optional: false
      trigger: { type: played, filter: culture(sauron),orc, memorize: playedOrc }
      effect: { type: exhaust, select: memory(playedOrc) } }
    { type: modifier, modifier: { type: skipPhase, phase: archery } }
    { type: Trigger, optional: false
      trigger: { type: startOfPhase, phase: regroup }
      effect: { type: discard, select: self } }
]
```
Each sentence is a separate effect. Don't try to combine unrelated abilities into one.

### Pattern: Response Event (Prevent Wound)

```
"Response: If a companion is about to take a wound, exert a [gondor] companion to prevent that wound."
```
```hjson
{
    type: responseEvent
    trigger: { type: aboutToTakeWound, filter: companion }
    cost: { type: exert, select: choose(culture(gondor),companion) }
    effect: { type: preventWound, select: choose(companion) }
}
```
Note: event cards use `responseEvent`, not `Response`. The latter is for permanent cards (conditions, possessions).

### Pattern: Conditional Modifier ("While X")

```
"While bearer is a Dwarf, bearer is strength +1 and damage +1."
```
```hjson
effects: [
    {
        type: modifier
        modifier: {
            type: modifyStrength
            filter: bearer
            amount: 1
            requires: { type: canSpot, filter: bearer,dwarf }
        }
    }
    {
        type: modifier
        modifier: {
            type: addKeyword
            filter: bearer
            keyword: damage
            amount: 1
            requires: { type: canSpot, filter: bearer,dwarf }
        }
    }
]
```
The "while" condition goes on each modifier as `requires`, not as a top-level requirement.

---

## Judgment Calls

### Single Effect vs Array

If a card has exactly one effect, HJSON allows either:
```hjson
effects: { type: modifier, ... }       // single object
effects: [ { type: modifier, ... } ]   // array with one entry
```
Both work. Use array when there are multiple, single object when there's genuinely one ability.

### Which Trigger Type?

- "Each time X" with no player choice → `Trigger` with `optional: false`
- "Each time X, you may Y" → `Trigger` with `optional: true`
- "Each time X happens" on an event card → impossible (events can't have triggers)
- "When you play this" → `Trigger` with trigger type `played` and `filter: self`
- "Response: If X is about to" → `Response` (permanent) or `ResponseEvent` (event)

### Modifier vs Trigger for "comes into play"

"Each [sauron] Orc comes into play exhausted" — this is a Trigger on `played`, NOT a modifier. It happens once when played, not continuously.

"Each [sauron] Orc is strength -2" — this IS a modifier. It applies continuously to all matching cards.

The distinction: does it describe a one-time event upon entry, or an ongoing state?

### "Discard this card" Self-Cleanup Patterns

Cards that discard themselves at a phase boundary:
```
"Discard this condition at the start of the regroup phase."
```
→ Trigger on `startOfPhase(regroup)` with `effect: discard(self)`.

Cards that discard as a cost:
```
"Discard this card to prevent that wound."
```
→ Goes in `cost: []` as `{ type: discard, select: self }`.

### Duration Phrasing

| English | HJSON `until` |
|---------|--------------|
| "until the regroup phase" | `start(regroup)` |
| "until end of turn" | `endOfTurn` |
| (no duration stated on a skirmish action) | `end(current)` (default) |
| "until the end of the archery phase" | `end(archery)` |

---

## Reading Complex Cards

For cards with multiple sentences of game text, process each sentence independently:

1. Split on sentence boundaries (periods, `<br>` tags)
2. Classify each sentence by pattern (modifier? trigger? activated? cost?)
3. Translate each to its own `effects` entry
4. Combine into the `effects: []` array

The order in the array should match the order in the game text — play requirements and extra costs first, then abilities top to bottom.

---

## Culture Symbol Shorthand

In game text, culture icons appear as `[culture]` (e.g., `[gondor]`, `[moria]`). In HJSON filters, these become `culture(gondor)`, `culture(moria)`, etc. In display text (`gametext` field), use HTML entities or the engine's symbol notation.

---

## Common Mistakes

1. **Using `choose(name(X))` when `self` suffices** — if the card IS the named character, use `self`.
2. **Putting "while" conditions on the effect wrapper instead of the modifier** — `requires` goes on the modifier object, not the `{ type: modifier }` wrapper.
3. **Confusing `times` and `count`** — `times` repeats on same target, `count` picks multiple targets.
4. **Missing `optional` field on Triggers** — it's required, not defaulted. Always specify.
5. **Using `Response` for event cards** — events use `ResponseEvent`; `Response` is for permanents.
6. **Forgetting `memorize` when cost and effect target the same thing** — if you choose in cost and need to reference in effect, memorize it.
7. **Treating "for each X" as a requirement instead of a value** — "for each" in an amount context is a `ForEachYouCanSpot` value, not a `CanSpot` requirement.
