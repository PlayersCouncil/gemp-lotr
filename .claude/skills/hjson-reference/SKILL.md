---
name: hjson-reference
description: >
  HJSON card definition reference for Gemp-LotR. Condensed DSL reference covering blueprint fields,
  effect types, filters, selectors, values, modifiers, requirements, triggers, and subactions.
  Use this skill when authoring or editing HJSON card definitions, looking up filter syntax,
  modifier types, trigger types, subaction types, or any HJSON DSL question.
  Trigger when: the user mentions HJSON, card definitions, filters, modifiers, triggers, subactions,
  effects arrays, or any card DSL element. Also trigger when debugging HJSON parse errors or
  looking up available HJSON constructs.
user-invocable: false
---

# HJSON Card Definition Reference

Condensed from hjson-doc-export.md. Organized for practical card authoring.

For full exhaustive documentation, read the relevant topical reference in `${CLAUDE_SKILL_DIR}/references/`:

| File | Contents |
|------|----------|
| `enums.md` | All enum values (CardType, Culture, Keyword, Phase, Race, Zone, etc.) |
| `filters.md` | Selectors, all filter types (simple/operation/function), spot overrides |
| `values.md` | Value resolution (literal, simple evaluated, math, stat, complex evaluated) |
| `modifiers.md` | All ~60 modifier types with full parameter documentation |
| `requirements.md` | All ~50 requirement types with full parameter documentation |
| `triggers.md` | All ~50 trigger types with full parameter documentation |
| `subactions.md` | All ~100+ subaction types (control flow, ongoing, discrete) |
| `memory-text.md` | Text substitution, In-Zone Data, memorization system |

The full monolith is also available at `hjson-doc-export.md` if needed.

---

## Top-Level Blueprint Fields

```hjson
{
  title: "Card Name"
  subtitle: "Optional Subtitle"        // appended to title
  unique: true                          // true/false (dot before name)
  side: Free Peoples                    // Free Peoples | Shadow (usually inferred from culture)
  culture: Gondor                       // see Culture enum
  type: Companion                       // see CardType enum
  race: Man                             // see Race enum
  twilight: 3                           // integer cost
  strength: 7
  vitality: 3
  resistance: 6                         // companions only
  signet: Aragorn                        // Aragorn | Frodo | Gandalf | Theoden
  site: 4                               // minion site number or site card number
  block: King                            // sites only: Fellowship | Towers | King | Shadows
  direction: LEFT                        // sites only: LEFT | RIGHT
  itemclass: Hand Weapon                 // possessions/artifacts: see Possession Class enum
  keywords: [Ranger, Valiant]            // see Keyword enum; numeric: "damage+1"
  timeword: Skirmish                     // events only: which phase
  target: "culture(gondor),man"          // bearer filter for attachable cards
  allyhome: ["3F", "6F"]                 // ally home sites (block-site pairs)
  gametext: "Game text as printed"       // HTML formatting allowed
  lore: "Lore text in quotes"
  effects: [ ... ]                       // array of game text effects (see below)
}
```

---

## Game Text Types (Effect Types)

Each entry in `effects: []` has a `type` field. The type determines the structure.

### Activated (Phase Special Abilities)
Standard "**Phase:** Do X" text. Most common game text type.
```hjson
{
  type: Activated
  phase: [Fellowship]         // required, array
  requires: [ ... ]           // optional conditions
  cost: [ ... ]               // subactions paid before effect
  effect: [ ... ]             // subactions performed after cost
  limitPerPhase: 1            // 0 = unlimited
  limitPerTurn: 1             // 0 = unlimited
  text: "Log message"         // optional
}
```
**Variants:** `ActivatedFromStacked` (+ `stackedOn` filter), `ActivatedInDiscard`, `ActivatedInDrawDeck` — same structure but activate from non-play zones.

### Modifier (Passive/Continuous Effects)
"While X, Y is Z" — always-on effects.
```hjson
{
  type: Modifier
  modifier: { ... }           // see Modifiers section
}
```
**Variants:** `ModifierInDiscard` (active from discard), `StackedOnModifier` (+ `on` filter), `PermanentSiteModifier` (sites, always active).

### Trigger (Each Time / When)
"Each time X happens, do Y" — reactive effects.
```hjson
{
  type: Trigger
  trigger: { type: WinsSkirmish, filter: self }  // see Triggers section
  optional: true              // required field
  requires: [ ... ]           // optional
  cost: [ ... ]               // optional
  effect: [ ... ]             // optional
  limitPerPhase: 1
  limitPerTurn: 1
  player: freeps              // optional: who performs the action
}
```

### Response (Response: If X is about to happen)
```hjson
{
  type: Response
  trigger: { type: AboutToTakeWound, filter: bearer }
  requires: [ ... ]
  cost: [ ... ]
  effect: [ ... ]
  limitPerPhase: 1
}
```

### Event / ResponseEvent
For event cards. `Event` for phase events, `ResponseEvent` for response events.
```hjson
{
  type: Event
  requires: [ ... ]
  cost: [ ... ]
  effect: [ ... ]
}
// or
{
  type: ResponseEvent
  trigger: { type: AboutToDiscard, filter: your,possession }
  requires: [ ... ]
  cost: [ ... ]
  effect: [ ... ]
}
```

### Other Effect Types

| Type | Purpose |
|------|---------|
| `ToPlay` | "To play, spot X" — play requirements |
| `ExtraCost` | "To play, exert X" — costs beyond twilight |
| `ModifyOwnCost` | "This card is twilight -1 when X" |
| `Discount` | Complex self-discount (PerExert, PerDiscardFromHand, etc.) |
| `ExtraPossessionClass` | "May be borne in addition to another hand weapon" |
| `PlayedInOtherPhase` | Permanent cards playable outside normal phase |
| `AssignmentCost` | FP must pay cost to assign this minion |
| `InHandTrigger` | Triggers that work from hand |
| `KilledTrigger` | Self-reacts to being killed |
| `SelfRemovedFromPlayTrigger` | Triggers on own discard/hinder |
| `CopyCard` | Copies another card's game text |
| `DeckBuildingRestriction` | Deck validation rules (maps) |
| `AidCost` | Follower aid cost |
| `DisplayableInformation` | Log output text |

---

## Selectors

Used for targeted effects — narrow a filter down to specific cards.

| Selector | Meaning |
|----------|---------|
| `choose(filter)` | Player picks from matching cards |
| `all(filter)` | Affects all matching cards |
| `self` | This card |
| `bearer` | Card this is attached to |
| `memory(X)` | Cards stored at memory location X |
| `random(count)` | Random selection (hand/adventure deck) |

---

## Filters

Comma-separated, case-insensitive. Commas act as AND. Example: `your,companion,elf,unwounded`

### Simple Filters (no params)
**Card types:** `Companion`, `Minion`, `Ally`, `Possession`, `Artifact`, `Condition`, `Event`, `Follower`, `Site`, `Character` (comp+minion+ally), `Item` (poss+artifact)
**Races:** `Elf`, `Man`, `Dwarf`, `Hobbit`, `Wizard`, `Orc`, `Uruk-hai`, `Nazgul`, `Troll`, `Ent`, etc.
**Keywords:** `Archer`, `Fierce`, `Enduring`, `Ranger`, `Valiant`, `Lurker`, `Stealth`, `Tale`, `Weather`, `Spell`, etc.
**Item classes:** `Hand Weapon`, `Ranged Weapon`, `Mount`, `Shield`, `Armor`, `Helm`, etc.

**State filters:**
| Filter | Meaning |
|--------|---------|
| `Your` | Owned by performing player |
| `Another` / `Other` | Not self |
| `Any` | Matches everything |
| `Self` | This card |
| `Bearer` | Card this is attached to |
| `RingBearer` | The Ring-bearer |
| `Unique` | Cards marked unique |
| `Wounded` / `Unwounded` | Has/hasn't wounds |
| `Exhausted` | 1 vitality remaining |
| `InSkirmish` | In active skirmish |
| `NotAssignedToSkirmish` | Not assigned/skirmishing |
| `Unbound` / `RingBound` | Companion keyword state |
| `Weapon` | Hand Weapon or Ranged Weapon |
| `Mounted` | Has mount or mounted status |
| `Hindered` / `Hinderable` | Hinder state |
| `CurrentSite` | Fellowship's current site |
| `NextSite` | Next site on path |
| `CanExert` / `CanBeDiscarded` | Rule-aware checks |
| `Playable` | Could be legally played |
| `InPlay` | In any play zone (NOT activity-aware) |
| `SkirmishLoser` | Loser of current skirmish |

### Operation Filters
- `and(filter1, filter2)` — all must match
- `or(filter1, filter2)` — any may match
- `not(filter)` — inverts

### Function Filters (with params)
| Filter | Params | Meaning |
|--------|--------|---------|
| `Culture(X)` | culture | Matches culture X |
| `Name(X)` / `Title(X)` | title | Matches card title |
| `Race(memory(X))` | memory | Race of memorized card |
| `Side(X)` | side | FP or Shadow side |
| `Signet(X)` | signet | Has signet X |
| `SiteNumber(X)` | int/range | Site number matches |
| `RegionNumber(X)` | int/range | Region number matches |
| `AttachedTo(filter)` | filter | Attached to matching card |
| `HasAttached(filter)` | filter | Bears matching card |
| `HasAttachedCount(N,filter)` | count,filter | Bears N+ matching cards |
| `HasStacked(filter)` | filter | Has matching stacked card |
| `InSkirmishAgainst(filter)` | filter | Skirmishing against match |
| `AssignedToSkirmish(filter)` | filter | Assigned against match |
| `Zone(X)` | zone | In specific zone |
| `Memory(X)` | memory | In memory location X |
| `NameFromMemory(X)` | memory | Shares title with memorized card |
| `CultureFromMemory(X)` | memory | Shares culture with memorized card |
| `MinStrength(X)` / `MaxStrength(X)` | int | Strength bounds |
| `MinResistance(X)` / `MaxResistance(X)` | int | Resistance bounds |
| `MinTwilight(X)` / `MaxTwilight(X)` | int/memory | Twilight cost bounds |
| `HighestStrength(filter)` | filter | Tied for highest strength |
| `LowestStrength(filter)` | filter | Tied for lowest strength |
| `SiteBlock(X)` | block | Site from block X |
| `Timeword(X)` | phase | Event phase matches |

---

## Value Resolution

Numeric fields (`amount`, `count`, `times`, `value`) accept literals or evaluated expressions.

### Literals
- Integer: `3`
- Range: `"0-3"` (player chooses)
- `Any` = 0 to MAX_INT
- `AnyNonZero` = 1 to MAX_INT

### Simple Evaluated (string aliases)
`BurdenCount`, `MoveCount`, `TwilightCount`, `memory(X)`

### Complex Evaluated (object with `type`)
Common fields on most: `over` (subtract from result), `limit` (cap), `multiplier`, `divider`

| Type | Key Params | Meaning |
|------|-----------|---------|
| `ForEachYouCanSpot` | filter | Count matching cards in play |
| `ForEachWound` | filter | Total wounds on matching cards |
| `ForEachKeyword` | filter, keyword | Total keyword instances |
| `ForEachBurden` | — | Current burden count |
| `ForEachThreat` | — | Current threat count |
| `ForEachTwilight` | — | Current twilight pool |
| `ForEachStrength` | filter | Total strength of matches |
| `ForEachVitality` | filter | Total vitality of matches |
| `ForEachResistance` | filter | Total resistance of matches |
| `ForEachCulture` | filter | Count distinct cultures |
| `ForEachFPCulture` | — | FP cultures spottable |
| `ForEachShadowCulture` | — | Shadow cultures spottable |
| `ForEachInDiscard` | filter, discard? | Count in discard pile |
| `ForEachInHand` | filter, hand? | Count in hand |
| `ForEachInDeadPile` | filter | Count in dead pile |
| `ForEachStacked` | filter, on | Count stacked cards |
| `ForEachHasAttached` | filter | Count attachments on current card |
| `ForEachCultureToken` | filter, culture? | Count culture tokens |
| `ForEachInMemory` | memory, filter? | Count cards in memory |
| `ForEachSiteYouControl` | — | Controlled sites |
| `CurrentSiteNumber` | — | 1-9 |
| `RegionNumber` | — | 1-3 |
| `Conditional` | requires, true, false | If/else value |
| `CardAffectedLimitPerPhase` | limit, source | "Strength +1 (limit +3)" pattern |
| `CardPhaseLimit` | limit, amount | Cumulative phase cap |
| `FromMemory` | memory | Integer from memory |
| `Subtract` | firstNumber, secondNumber | Math |
| `Sum` | source[] | Add values together |
| `Min` / `Max` | firstNumber, secondNumber | Math |
| `Range` | from, to | Dynamic range |

---

## Duration Resolution

For `until` field on ongoing effects:
- `end(current)` — end of current phase (default)
- `end(Skirmish)` — end of skirmish phase
- `start(Regroup)` — start of regroup
- `EndOfTurn` — end of turn
- `EndOfGame` / `Permanent` — never expires

---

## Requirements

Boolean checks. Used in `requires: []` arrays.

### Spotting
| Requirement | Key Params | Meaning |
|------------|-----------|---------|
| `CanSpot` | filter, count? | N cards matching filter in play |
| `CantSpot` | filter, count? | Inverse of CanSpot |
| `CanSpotBurdens` | amount? | Ring-bearer has N+ burdens |
| `CanSpotThreats` | amount? | N+ threats |
| `CanSpotTwilight` | amount? | N+ twilight in pool |
| `CanSpotWounds` | filter, amount? | N+ wounds on matching cards |
| `CanSpotFPCultures` | amount | N+ FP cultures |
| `CanSpotShadowCultures` | amount | N+ Shadow cultures |
| `CanSpotCultureTokens` | culture?, filter?, amount? | Culture tokens on cards |

### Game State
| Requirement | Key Params | Meaning |
|------------|-----------|---------|
| `Phase` | phase | Current phase matches |
| `Location` | filter | Current site matches filter |
| `HaveInitiative` | side | Side has initiative |
| `CanMove` | — | FP can still move |
| `FierceSkirmish` | — | In fierce assignment/skirmish |
| `RingIsOn` | — | Ring-bearer wearing ring |
| `ControlsSite` | player?, count? | Player controls N sites |

### Cards in Zones
| Requirement | Key Params | Meaning |
|------------|-----------|---------|
| `HasCardInHand` | player?, filter?, count? | Cards in hand |
| `HasCardInDiscard` | player?, filter?, count? | Cards in discard |
| `HasCardInDrawDeck` | player?, filter?, count? | Cards in deck |
| `HasCardInDeadPile` | filter?, count? | Cards in dead pile |
| `HasCardStacked` | filter, on?, count? | Stacked cards |
| `HasCardInAdventureDeck` | player?, filter?, count? | Adventure deck cards |
| `CardsInHandMoreThan` | player?, count | Hand size check |

### Memory & Limits
| Requirement | Key Params | Meaning |
|------------|-----------|---------|
| `HasCardInMemory` | memory | Any cards at memory location |
| `HasMemory` | memory | Any data at memory location |
| `MemoryIs` | memory, value | String comparison |
| `MemoryLike` | memory, value | Substring match |
| `MemoryMatches` | memory, filter, count? | Cards in memory match filter |
| `PerPhaseLimit` | limit? | Action invoked fewer than N times |
| `PerTurnLimit` | limit? | Per-turn limit check |

### Comparison
`IsEqual`, `IsNotEqual`, `IsGreaterThan`, `IsGreaterThanOrEqual`, `IsLessThan`, `IsLessThanOrEqual` — all take `firstNumber`, `secondNumber`

### Logic
`And`, `Or`, `Not` — wrap other `requires` arrays

### Other
`IsSide(side)`, `DidWinSkirmish(filter)`, `LostSkirmishThisTurn(filter)`, `PlayedCardThisPhase(filter)`, `MoveCountMinimum(amount)`, `TwilightPoolLessThan(amount)`, `WasAssignedToSkirmish(filter)`, `WasPlayedFromZone(zone)`

---

## Triggers

### Movement
`MovesFrom(filter?)`, `Moves`, `AboutToMoveTo(filter?)`, `MovesTo(filter?)`

### Timing
`StartOfTurn`, `StartOfPhase(phase?)`, `EndOfPhase(phase?)`, `EndOfTurn`

### Before (can prevent)
`AboutToTakeWound(filter, source?)`, `AboutToBeKilled(filter)`, `AboutToBeOverwhelmed(filter)`, `AboutToDiscard(filter, source?)`, `AboutToExert(filter)`, `AboutToHeal(filter)`, `AboutToAddBurden(filter)`, `AboutToHinder(filter?)`, `AboutToRestore(filter?)`, `AboutToDrawCard(player)`, `BeforeThreatWounds(filter)`, `BeforeToil(filter)`, `FPStartedAssigning`

### After
`Played(filter, on?, from?, memorize?)`, `TakesWound(filter, source?, memorize?)`, `Exerted(filter, source?, memorize?)`, `Heals(filter, memorize?)`, `Killed(filter, killer?, cause?, inSkirmish?, memorize?)`, `Discarded(filter, source, fromZone?, memorize?)`, `WinsSkirmish(filter, against?, memorize?, memorizeLoser?)`, `LosesSkirmish(filter, against?, overwhelm?, memorize?)`, `AssignedAgainst(filter, against?, side?, memorize*)`, `AssignedToSkirmish(filter, side?, memorize?)`, `StartOfSkirmishInvolving(filter, against?)`, `AfterAllSkirmishes`, `AddsBurden(filter?, player?)`, `AddsThreat(filter?)`, `AddsTwilight(filter?, cause?)`, `PlayerDrawsCard(player)`, `PutsOnRing`, `TakesOffRing`, `Reconciles(player?)`, `Transferred(filter?, to?)`, `SiteControlled(player?, filter?)`, `SiteLiberated(player?, filter?)`, `Hindered(filter?)`, `Restored(filter?)`, `RemovedFromPlay(filter?)`, `FPDecidedToMove`, `FPDecidedToStay`, `FPDecidedIfMoving`, `ConstantlyCheck(requires)`, `CancelledSkirmish(filter?)`

---

## Modifiers

Used in `Modifier` effect types or via `AddModifier` subaction.

### Stat Modifiers
`ModifyStrength(filter, amount, requires?)`, `ModifyVitality(filter, amount, requires?)`, `ModifyResistance(filter, amount, requires?)`, `ModifyCost(filter, amount, requires?)`, `ModifySiteNumber(filter, amount, requires?)`, `ModifyRoamingPenalty(filter, amount, requires?)`, `ModifyArcheryTotal(amount, side, requires?)`, `ModifyMoveLimit(amount, requires?)`, `ModifyPlayOnCost(filter, on, amount, requires?)`, `ModifySanctuaryHeal(amount, requires?)`, `ModifyInitiativeHandSize(amount)`

### Keyword Modifiers
`AddKeyword(filter, keyword, amount?, requires?)`, `RemoveKeyword(filter, keyword, requires?)`, `RemoveAllKeywords(filter, requires?)`, `AddKeywordFromCards(filter, from, requires?)`

### Type/Identity Modifiers
`AddCulture(filter, culture, requires?)`, `AddRace(filter, race, requires?)`, `AddSignet(filter, signet, requires?)`, `AddActivated(filter, phase, cost?, effect?, requires?)`, `DuplicateGameText(filter, from, requires?)`

### Prevention Modifiers
`CantTakeWounds(filter, requires?)`, `CantTakeMoreWoundsThan(filter, amount?, phase?, requires?)`, `CantBeExerted(filter, by?, requires?)`, `CantHeal(filter, requires?)`, `CantBeDiscarded(filter, by?, player?, requires?)`, `CantBeOverwhelmed(filter, requires?)`, `CantBeOverwhelmedMultiplier(filter, multiplier?, requires?)`, `CantBeAssignedToSkirmish(filter, player?, requires?)`, `CantBeAssignedToSkirmishAgainst(fpCharacter, minion, side?, requires?)`, `CantBeTransferred(filter, requires?)`, `CantBear(filter, cardFilter?, requires?)`, `CantPlayCards(filter, requires?)`, `CantPlayPhaseEvents(phase, player?, requires?)`, `CantPlayPhaseEventsOrPhaseSpecialAbilities(phase, player?, requires?)`, `CantUseSpecialAbilities(filter?, phase?, player?, requires?)`, `CantMove(requires?)`, `CantCancelSkirmish(filter?, requires?)`, `CantPreventWounds(requires?)`, `CantRemoveBurdens(filter?, requires?)`, `CantRemoveThreats(filter?, requires?)`, `CantTakeArcheryWounds(filter, requires?)`, `CantPlaySite(player, requires?)`

### Other Modifiers
`AllyCanParticipateInArcheryFireAndSkirmishes(filter, requires?)`, `AllyCanParticipateInSkirmishes(filter, requires?)`, `DisableGameText(filter, requires?)`, `DoesNotAddToArcheryTotal(filter, requires?)`, `ExtraCostToPlay(filter, cost, requires?)`, `FPCultureCount(amount, requires?)`, `HasToMoveIfAble(requires?)`, `ShadowHasInitiative(requires?)`, `SkipPhase(phase, requires?)`, `CanPlayStackedCards(filter, on, requires?)`, `CanBearExtraItems(filter, itemclass, amount?, requires?)`, `ItemClassSpot(class, requires?)`, `ModifyRaceSpotCount(race, requires?)`

---

## Subactions (Effects)

Used in `cost: []` and `effect: []` arrays.

### Control Flow
| Subaction | Key Params | Purpose |
|-----------|-----------|---------|
| `Choice` | player?, texts[], effects[], requires?[] | Choose one of N options |
| `CostToEffect` | cost[], effect[], requires? | Sequential cost-then-effect |
| `If` | check[], true[], false[] | Conditional execution |
| `Optional` | player?, text, effect[], requires? | "You may" prompt |
| `Preventable` | player, text, cost[], effect[], instead?[] | "X may Y to prevent Z" |
| `Multiple` | effects[] | Group subactions |
| `Repeat` | times, effect[] | Do N times |
| `DoWhile` | check, effect[] | Loop until check fails |
| `ForEachPlayer` | effect[] | Once per player |
| `SendMessage` | text | Log text (also "do nothing") |

### Ongoing (Spawn Modifiers)
| Subaction | Key Params | Purpose |
|-----------|-----------|---------|
| `AddModifier` | modifier, until? | Raw modifier with duration |
| `ModifyStrength` | select, amount, count?, until?, memorize? | Temp strength change |
| `ModifyResistance` | select, amount, count?, until?, memorize? | Temp resistance change |
| `ModifySiteNumber` | select, amount, count?, until?, memorize? | Temp site number change |
| `AddKeyword` | select, keyword, amount?, count?, until?, memorize? | Add keyword temporarily |
| `RemoveKeyword` | select, keyword, count?, until?, memorize? | Remove keyword temporarily |
| `RemoveText` | select, count?, until?, memorize? | Disable game text |
| `ModifyArcheryTotal` | side, amount, until? | Alter archery total |
| `AddTrigger` | trigger, until?, optional?, requires?, cost?, effect? | Spawn temporary trigger |
| `AlterOverwhelmMultiplier` | select, multiplier?, until?, memorize? | Change overwhelm threshold |
| `DisableSkirmishAssignment` | select, until? | Can't be assigned |
| `DisableWounds` | select, until?, memorize? | Can't take wounds |
| `EnableParticipationInArcheryFireAndSkirmishes` | select, count?, until?, requires? | Ally combat |

### Discrete (One-Time)
**Wounds/Vitality:**
`Wound(select, count?, times?, player?, memorize?)`, `Exert(select, count?, times?, player?, memorize?)`, `Exhaust(select, count?, player?, memorize?)`, `Heal(select, count?, times?, player?, memorize?)`, `Kill(select, count?, player?, memorize?)`

**Tokens:**
`AddBurdens(amount?, player?)`, `RemoveBurdens(amount?, player?)`, `AddThreats(amount?, player?)`, `RemoveThreats(amount?)`, `AddTwilight(amount?, memorize?)`, `RemoveTwilight(amount?, memorize?)`, `AddCultureTokens(select, culture, count?, memorize?)`, `RemoveCultureTokens(select, culture?, count?, memorize?)`, `ReinforceTokens(select?, culture?, player?, times?, memorize?)`

**Card Movement:**
`Discard(select, count?, player?, memorize?)`, `ReturnToHand(select, count?, player?)`, `Transfer(select, where, checkTarget?, memorize*)`, `StackCardsFromPlay/Hand/Deck/Discard(select, where, count?)`, `RemoveFromTheGame(select, count?, player?, memorize?)`

**Playing Cards:**
`PlayCardFromHand(select, on?, discount?, memorize?)`, `PlayCardFromDiscard(select, on?, discount?, player?, memorize?)`, `PlayCardFromDrawDeck(select, on?, discount?, shuffle, showAll, memorize?)`, `PlayCardFromDeadPile(select, discount?, memorize?)`, `PlayCardFromStacked(select, on, discount?, memorize?)`, `PlayNextSite(filter?, memorize?)`

**Hand/Deck Manipulation:**
`DrawCards(count?, player?)`, `DiscardFromHand(select?, count?, hand?, forced, player?, memorize?)`, `DiscardCardAtRandomFromHand(hand?, count?, forced)`, `DiscardTopCardsFromDeck(deck?, count?, forced, memorize?)`, `PutCardsFromDeckIntoHand(select?, count?, shuffle, reveal, showAll)`, `PutCardsFromDiscardIntoHand(select?, count?, player?, discard, memorize?)`, `RevealHand(hand?, memorize?)`, `RevealTopCardsOfDrawDeck(deck?, count?, memorize?)`, `RevealCardsFromHand(hand?, select?, count?, memorize?)`, `LookAtTopCardsOfDrawDeck(deck?, count?, memorize?)`, `ShuffleCardsFromDiscardIntoDrawDeck(select?, count?)`, `ShuffleDeck(deck?)`

**Memory:**
`Memorize(filter, memory)`, `MemorizeNumber(amount, memory)`, `MemorizeInfo(info, memory)`, `MemorizeTitle(from, memory)`, `MemorizeDiscard(filter?, memory)`, `MemorizeStacked(filter?, on, memory)`, `MemorizeTopOfDeck(memory, count?)`, `FilterCardsInMemory(filter, memory, memorizeMatching?, memorizeNotMatching?)`

**Choices:**
`ChooseActiveCards(select?, count?, memorize, text, player?, hindered?)`, `ChooseArbitraryCards(fromMemory, count?, filter?, memorize, text, player?)`, `ChooseANumber(text, from?, to?, memorize, player?)`, `ChooseACulture(memorize)`, `ChooseARace(memorize)`, `ChooseAKeyword(memorize, keywords[], text?)`, `ChooseYesOrNo(text, memorize, player?)`, `ChooseHowManyToSpot(filter, memorize, text?)`, `ChooseCardsFromDiscard(select?, count?, memorize, text?)`, `ChooseCardsFromDrawDeck(select?, count?, memorize, player?, deck?, showAll, text?)`

**Skirmish:**
`CancelSkirmish(filter?, involving?, fierceOnly?)`, `CancelEvent`, `CancelSpecialAbility`, `CancelAllAssignments`, `AssignFPCharacterToSkirmish(fpCharacter, minion, player?)`, `ReplaceInSkirmish(filter, with)`, `RemoveCharacterFromSkirmish(select?, player?, memorize?)`, `StartSkirmish(fpCharacter, against)`, `SetupExtraAssignmentAndSkirmishes(filter)`

**Prevention:**
`PreventWound(select, memorize?)`, `PreventAllWounds(select?)`, `PreventDiscard(select, memorize?)`, `PreventExert(select, memorize?)`, `PreventHeal(select, memorize?)`, `PreventEffect`, `PreventBurden`, `PreventTwilight`, `NegateWound(select?)`, `PreventAddingAllBurdens`

**Limits:**
`IncrementPerPhaseLimit(limit?, phase?, perPlayer?)`, `IncrementPerTurnLimit(limit?)`

**Other:**
`PutOnRing`, `TakeOffRing`, `CorruptRingBearer`, `ReconcileHand(player?)`, `EndPhase`, `MakeSelfRingBearer`, `PlaySite(number, player?, block?, filter?)`, `TakeControlOfSite(player?)`, `LiberateSite(controller?, memorize?)`, `Hinder(select, count?, player?, memorize?)`, `Restore(select, count?, player?, memorize?)`

---

## Enums Quick Reference

**CardType:** Companion, Minion, Ally, Possession, Artifact, Condition, Event, Follower, The One Ring, Site, Map
**Culture:** Dwarven, Elven, Gandalf, Gollum, Gondor, Rohan, Shire | Dunland, Isengard, Men, Moria, Orc, Raider, Sauron, Uruk-hai, Wraith (alias: Ringwraith)
**Race:** Dwarf, Elf, Ent, Hobbit, Man, Wizard, Maia | Orc, Uruk-hai, Nazgul, Troll, Spider, Creature, Balrog, Wraith
**Side:** Free Peoples, Shadow
**Signet:** Aragorn, Frodo, Gandalf, Theoden
**Phase:** Fellowship, Shadow, Maneuver, Archery, Assignment, Skirmish, Regroup
**SitesBlock:** Fellowship, Towers, King, Shadows
**Zone:** Free Characters, Shadow Characters, Attached, Support, Adventure Path, Hand, Discard, Deck, Dead, Stacked, Removed, Void

---

## Spot Overrides (activeOverride)

For targeting inactive cards: `hindered`, `stacked`, `out_of_turn`, `attached_to_inactive`, `all`, `none`

---

## Memory System

- **Memory**: temporary per-action variables. Set via `memorize` params or `Memorize*` subactions.
- **In-Zone Data**: persistent per-card data, cleared when card changes zones. Set via `Store*` subactions.
- Retrieve memory in filters: `Memory(X)`, `NameFromMemory(X)`, `CultureFromMemory(X)`
- Retrieve memory in values: `memory(X)`, `FromMemory(memory)`, `ForEachInMemory(memory)`
- Retrieve memory in requirements: `HasCardInMemory(memory)`, `MemoryIs(memory, value)`

---

## Player Resolution

`You`, `FP` / `Freeps` / `Free Peoples`, `Shadow` / `S`, `Owner`, `FromMemory(X)`, `OwnerFromMemory(X)`

---

## Text Substitution

In `text` fields: `{culture}` → symbol, `{you}` → performing player, `{owner}` → card owner, `{self}` → card link, `{memoryVar}` → stored card(s) or string
