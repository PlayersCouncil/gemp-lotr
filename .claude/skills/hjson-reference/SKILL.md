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

### `noEffectUnless` (the no-effect flag)

`noEffectUnless` (on Activated, ActivatedInDiscard, ActivatedFromStacked, ActivatedInDrawDeck, Event, ResponseEvent, Trigger, Response, and the AddActivated modifier): one requirement or a list, written exactly like `requires`. It never withholds the action: the action is offered as usual, and while any of these requirements fails the decision flags it `noEffect: effect`, so the client shows it differently and the player may still take it. It is where a guard goes that used to keep an event, or an ability with a cost, from being used to no purpose (Q-0034 forbids withholding those). The requirements are checked in order in the action's own context when it is put before a player, so one may `memorize` what the next reads (Breaching Shot 102_21).

Do not declare what the game already sees. An action whose every effect is modifyStrength, addKeyword, alterOverwhelmMultiplier, wound, heal, exert, exhaust, discard, cancelSkirmish, removeBurdens, removeThreats or drawCards (alone or inside if, choice, costToEffect, multiple, optional, preventable, repeat) is flagged by itself when none has anything to act on. An ability that has reached its limit, or whose capped bonus is spent, is flagged `limit` by itself (see "Limits" under Activated). A skirmish ability or Skirmish event that only gives strength, damage or overwhelm protection for the current phase is flagged `skirmish` when it reaches nobody in the skirmish being fought. Nothing is judged where a cost could make what the effect needs (an exertion before a heal), where a select reads `memory(...)`, or where a cost is of a kind the judge does not know: a false flag is worse than a missing one.

An ability or optional trigger with NO cost that would do nothing is not a flag matter: by Q-0034 it is withheld with a `requires`.

### Activated (Phase Special Abilities)
Standard "**Phase:** Do X" text. Most common game text type.
```hjson
{
  type: Activated
  phase: [Fellowship]         // required, array
  requires: [ ... ]           // optional conditions
  cost: [ ... ]               // subactions paid before effect
  effect: [ ... ]             // subactions performed after cost
  limitPerPhase: 1            // "(limit once per phase)": uses; withheld at the limit. 0 = unlimited
  limitPerTurn: 1             // "(limit once per turn)": uses; withheld at the limit. 0 = unlimited
  effectLimitPerPhase: 3      // "(limit +3)": what the effect gives; with a cost, on offer at the limit, flagged
  effectLimitPerTurn: 6       // "(limit +6)" counted over a turn
  text: "Log message"         // optional
}
```
**Variants:** `ActivatedFromStacked` (+ `stackedOn` filter), `ActivatedInDiscard`, `ActivatedInDrawDeck` — same structure but activate from non-play zones.

**Limits.** A card says one of two things with the word "limit", and the definition says which by the field it uses (Ketura on Q-0082 and Q-0093: "there's a nuance between 'limit +3' and 'limit once', with the former being a cumulative addition of multiple invocations, and the latter being a limit on the number of times the ability can invoke at all"; "the first kind should be offered but effectless, while the second should halt being offered once the invocation limit is reached"):

- **A limit on uses**: "(limit once per turn)", "(limit once per phase)", "limit N times". Write `limitPerPhase` / `limitPerTurn`. At its limit the action is withheld, whatever it costs: the limit is a requirement, and a use is counted as the first of the action's costs. This is the default: a definition that says only `limitPer...` means a limit on uses, which is what most cards print.
- **A limit on what the effect gives**: "(limit +3)", and a bare number of the things the effect adds or does ("to add a threat (limit 1)": Ketura, "Dark Tidings is more like '+1' than 'once'; it is limiting the number of threats that can be added the way that Scimitar limits the strength bonus"). Write `effectLimitPerPhase` / `effectLimitPerTurn`, as the number of uses that still give something (Orc Scimitar 1_269, "+1 (limit +3)", is `effectLimitPerPhase: 3`). On an ability that has a `cost` and is the player's to use or not (Activated, an optional Trigger, a Response) the action stays on offer at its limit (Ketura on Q-0041: "Technically speaking it should be offered"). Its costs are paid as written; a use is counted once they are paid, so a use whose cost fails spends nothing and one that is cancelled after its costs is spent; at the limit the log says the card has reached its limit and none of its effects is carried out. The decision flags the action `noEffect: limit`. With no cost, and on a required Trigger, this kind withholds the action as the other does (Q-0034).

All four fields are on Activated and its variants, Trigger, Response and the AddActivated modifier, and count uses of the card, not of the ability: two limited abilities of one card share a counter.

Sorting a text: "once", "twice" and "N times" count uses; "+N" counts what is given; a bare "(limit N)" or "(limit N per phase)" counts the things the effect adds or does (threats, tokens, heals, cards played) and is the second kind. Where a text is hard to sort, ask.

Write a limit with a field. A hand-written `PerPhaseLimit` / `PerTurnLimit` requirement with an `IncrementPer...Limit` withholds the action at its limit whatever it costs, as `limitPerPhase` / `limitPerTurn` do; it is for where a field cannot say what is meant (`perPlayer`, a modifier, a limit shared by two abilities in some other way). An effect that plays a card still withholds the action while nothing can be played, at its limit or not.

A cap written inside the effect, as opposed to on the ability, leaves the ability on offer whatever it costs: `limitPerCardThisPhase` on ModifyStrength, or a `CardAffectedLimitPerPhase` / `CardPhaseLimit` value as the amount of ModifyStrength or AddKeyword or the count of DrawCards. Once the cap is spent for everyone the effect could reach, the action is flagged `limit` (where the rest of it can be judged: see `noEffectUnless` above).

`limitPerCardThisPhase` on ModifyStrength and ModifyResistance is counted for the SOURCE card for the phase, whoever the bonus is given to (F-0125, CR 4.2 "limit"): a "(limit +3)" cannot be given in full to each companion in turn. A `CardPhaseLimit` may take a `Conditional` as its `limit` ("limit +3 unless you can spot ..."); the no-effect judge reads such a cap as it stands, so the ability is flagged `limit` once it is spent.

`ActivatedInDiscard` and `ActivatedInDrawDeck` take `specialAbility: false` for a card's permission to be played from that pile when its text has no bold phase word and colon ("If you have initiative ..., you may play this minion from your discard pile"). Such a play is not a special ability: nothing that answers, bans or counts special abilities sees it (Ketura, 2026-10-06: a special ability "would count, if it's got the bold text and a colon"). A labelled ability ("Shadow: ... to play this minion from your discard pile") leaves the flag off.

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
The limit fields are as under Activated, "Limits": `limitPerPhase` / `limitPerTurn` withhold a Trigger at its limit; under `effectLimitPerPhase` / `effectLimitPerTurn` an optional Trigger with a cost is still offered at its limit, flagged, and a required one, or one with no cost, is not. The limit is counted in the phase the trigger goes off in unless `phase` names one.

### Response (Response: If X is about to happen)
```hjson
{
  type: Response
  trigger: { type: AboutToTakeWound, filter: bearer }
  requires: [ ... ]
  cost: [ ... ]
  effect: [ ... ]
  limitPerPhase: 1            // or limitPerTurn: 1 for "(limit once per turn)"; effectLimitPer... for "(limit +N)"
}
```
The limit fields are as under Activated, "Limits". A Response is not offered to a special ability whose effect has been cancelled, or to an event that has been cancelled (Q-0043); an optional Trigger still is. Build a printed "Response:" as `type: Response` even where its cost could be paid twice: do not make it an optional Trigger to avoid a second offer.

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

`SelfRemovedFromPlayTrigger` (fields `method`, `optional`, `source`, `requires`, `cost`, `effect`) is a card's own trigger for having been discarded from play (`method: discarded`), hindered (`method: hinder`), or either (no `method`). It answers after the fact, when the card is already in the discard pile or already hindered. Its requirements, costs and effects are carried out as the removed card and its owner: "you" and `your` are the owner, an optional one is offered to the owner, and `self` and `another` are counted from the removed card, whoever removed it and with whatever card (F-0121). `source:` is a card filter that the card which did the removing must match (`source: side(Free Peoples)`, `source: pipe`); with no `source:` the trigger answers ANY discard from play, the end-of-turn discard of minions and a card lost with its bearer included (Q-0081). A `source:` is a condition on the card that did the removing and is not met by a removal no card did. A killed character goes to the discard pile but was not discarded (CR 4.2 "killed"): the trigger stays unset. Do not name a player on the effect to steer it (`player: shadow`): that was a workaround and is not needed.

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

The filters in a list are asked in order. Put exclusions ahead of `Playable`: asked of the card whose own text is doing the asking, `Playable` asks that card's requirements again and can recurse (`event, not(title(Show Them the Meaning of Haste)), Playable`).

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
| `Bearer` | Card this is attached to, found even when this card is hindered |
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
| `HasAttached(filter)` | filter | Bears matching card (active attachments only: not a hindered one; `HasAttachedCount(N,any,override(hindered))` counts hindered ones too) |
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
| `ReplaceableFromDiscard(filter)` | filter | Performing player's discard pile holds a card matching `filter` with this card's title and another subtitle: what `ReplaceCardInPlayWithCardInDiscard` can act on. Use it in both the requirement and the choice of the card to replace |

---

`PlayableOn(<filter>)` (function filter, engine pass 7): a card in a hand or a pile that attaches and could be played NOW on a card the inner filter accepts. Use it wherever a text plays items "on" named bearers and the action should be withheld, or X capped, when nothing fits (F-0140, Q-0094): `HasCardInDiscard` with `filter: item,PlayableOn(memory(playedMinion))`.

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
| `CardAffectedLimitPerPhase` | limit, source, prefix?, multiplier? | "Strength +1 (limit +3)" pattern: caps what the effect gives in a phase and leaves the ability on offer (see Activated, "Limits") |
| `CardPhaseLimit` | limit, amount | Cumulative phase cap, counted on the card's own phase counter; leaves the ability on offer likewise |
| `FromMemory` | memory | Integer from memory |
| `Subtract` | firstNumber, secondNumber | Math |
| `Sum` | source[] | Add values together |
| `Min` / `Max` | firstNumber, secondNumber | Math |
| `Range` | from, to | Dynamic range |
| `LeastDiscountToPlay` | filter, fromZone (DISCARD only), max, player? | The least X from 0 to `max` at which some card matching `filter` in that player's discard pile can be played at twilight cost -X, by the same check a play makes (roaming, the card's own cost text and standing modifiers counted). Use it as the `from:` of the ChooseANumber in "Remove X ... to play ... from your discard pile; its twilight cost is -X" (102_39, 8_50; F-0067). Where no X up to `max` will do it answers `max`; the ability's `requires: Playable` with the full discount is what withholds it then |

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
| `OpponentDoesNotControlSite` | player? | No opponent of the player controls a site |

Whether and how many sites a player controls is asked with these two and the `ForEachSiteYouControl` value, never by spotting a site card with `ControlledSite`, `SiteYouControl` or `ControlledByOtherPlayer`: the count includes sites that are no card (Breached Wall's +1; Q-0054).

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
| `PerPhaseLimit` | limit?, phase?, perPlayer? | Action invoked fewer than N times. Withholds at the limit, as the `limitPerPhase` field does; prefer the field (see Activated, "Limits") |
| `PerTurnLimit` | limit? | Per-turn limit check; likewise, `limitPerTurn` |

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

`ModifyCost`, `ModifyPlayOnCost` and a card's own `ModifyOwnCost` price a card being played. A transfer by rule is not a play: it costs the printed twilight cost and none of these reach it (Q-0053, Q-0074).

### Keyword Modifiers
`AddKeyword(filter, keyword, amount?, requires?)`, `RemoveKeyword(filter, keyword, requires?)`, `RemoveAllKeywords(filter, requires?)`, `AddKeywordFromCards(filter, from, requires?)`

### Type/Identity Modifiers
`AddCulture(filter, culture, requires?)`, `AddRace(filter, race, requires?)`, `AddSignet(filter, signet, requires?)`, `AddActivated(filter, phase, cost?, effect?, requires?)`, `DuplicateGameText(filter, from, requires?)`

### Prevention Modifiers
`CantTakeWounds(filter, requires?)`, `CantTakeMoreWoundsThan(filter, amount?, phase?, requires?)`, `CantBeExerted(filter, by?, requires?)`, `CantHeal(filter, requires?)`, `CantBeDiscarded(filter, by?, player?, requires?)`, `CantBeOverwhelmed(filter, requires?)`, `CantBeOverwhelmedMultiplier(filter, multiplier?, requires?)`, `CantBeAssignedToSkirmish(filter, player?, requires?)`, `CantBeAssignedToSkirmishAgainst(fpCharacter, minion, side?, requires?)`, `CantBeTransferred(filter, requires?)`, `CantBear(filter, cardFilter?, requires?)`, `CantPlayCards(filter, requires?)`, `CantPlayPhaseEvents(phase, player?, requires?)`, `CantPlayPhaseEventsOrPhaseSpecialAbilities(phase, player?, requires?)`, `CantUseSpecialAbilities(filter?, phase?, player?, requires?)`, `CantMove(requires?)`, `CantCancelSkirmish(filter?, requires?)`, `CantPreventWounds(requires?)`, `CantRemoveBurdens(filter?, requires?)`, `CantRemoveThreats(filter?, requires?)`, `CantTakeArcheryWounds(filter, requires?)`, `CantPlaySite(player, requires?)`

Overwhelm is decided character by character (Q-0044). On a minion, `CantBeOverwhelmedMultiplier` and the `AlterOverwhelmMultiplier` subaction protect that minion only, measured against its own strength; other minions in the skirmish are overwhelmed at double the side's total. The `AboutToBeOverwhelmed` trigger and `LosesSkirmish` with `overwhelm: true` match only the characters actually overwhelmed, not a minion spared beside them.

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

`Exert` with `times: N` and a `choose(...)` selection offers only characters able to exert N times: outright when it is a cost; as an effect while anyone can do it in full, falling back to anyone able to exert when nobody can (F-0131). Do not add a vitality filter to say so. "Wound X twice" is `type: Wound` with `times: 2`, never an Exert (F-0148).

"Play any number of / up to N ... from your discard pile" as an optional trigger or ability needs a `HasCardInDiscard` requirement for what it plays (with `PlayableOn` where the bearers are named), so that it is not offered with nothing to play (Q-0086).

Inside a `preventable`, a cost is the preventing player's to choose unless it says otherwise: for "the Free Peoples player may make you X to prevent this", give the cost `player: owner`. `ChooseANumber` takes a `Conditional` for `from`.

An effect whose text picks "a card", "a Shadow card" or "a Free Peoples card" and nothing more specific may pick a HINDERED card: give it `override: hindered` (Q-0098). Anything more specific cannot reach a hindered card, and hinder choices leave hindered cards out (Q-0060).

A play in a `cost` (`PlayCardFromHand`, `PlayCardFromDiscard`, `PlayCardFromDrawDeck`, `PlayCardFromStacked`, `PlayCardFromDeadPile`) is paid only if the card is still in play when its play is complete, that is after everything that answered the play has finished. A card discarded, killed, returned to hand or stacked before then, by a standing text, by its own "when you play" or by a response, leaves the cost unpaid: the action's effect is not carried out, and the twilight and the other costs already paid stay paid. The same holds for a play as an effect that an "if you do" depends on. The action is still offered where the removal could be foreseen (Ketura on F-0139). `extraEffects` on a play are carried out after it whether the card stayed or not.

`Played` answers a play that has been announced, and a play is announced only for a card that is still in play after the standing texts (`ConstantlyCheck` triggers) have acted on it. A card discarded or killed as it came into play sets off no `Played` trigger, required or optional, its own or another card's, and no `ResponseEvent` or response ability with a `Played` trigger. A card's own "when you play this ..., or discard it" is an `EntersPlay` trigger (below), resolved before the play is announced.

`ConstantlyCheck` triggers are gathered for the entering step of every play, before the play is announced. A standing "discard ..." or "is discarded if ..." is written as one, never as a `Played` trigger.

`PlayedCardThisPhase` counts a permanent from the moment its play is announced, so a `Played` trigger that requires `PlayedCardThisPhase` with `min: 2, max: 2` sees the play it is answering (Ill Wind 103_67). The play is taken out of the count when it is complete if the card is no longer in play, and a card that was never announced is never counted. Events are always counted.

`limitPerSite: 1` is "(limit once per site)": a limit on uses, counted by the engine for the site the fellowship is at, cleared when the fellowship moves to another site and at the end of the turn, so it is once for each site in a turn (Q-0084, F-0146). Anything that happens as the fellowship arrives belongs to the site arrived at. As a requirement and an effect: `PerSiteLimit(limit?)` and `IncrementPerSiteLimit(limit?)`; prefer the field. Do not build it from per-phase limits and `MoveCountMinimum`.

A required Trigger with a limit on uses (`limitPerPhase`, `limitPerTurn`, `limitPerSite`, or a `PerPhaseLimit` / `PerTurnLimit` / `PerSiteLimit` written directly in `requires`) is carried out no more often than the limit even when one event raises it several times at once (two companions exerting together): the later ones are dropped (F-0145). A limit requirement nested inside `Not` / `Or` is not seen by that check: write the limit with the field.

`AddKeywordFromCards` takes `snapshot: true`: the keywords, with their numbers, are taken once, when the modifier is made, and given exactly so until the modifier ends, whatever becomes of the `from` cards (killed, hindered, changed). Use it inside an `AddModifier` for a text that copies keywords for a time ("until the regroup phase, each of your other Men gain each of the unloaded keywords on that Man", Q-0096). Leave it off a standing `Modifier`, which is read as the cards stand.

`LeastDiscountToPlay` takes `fromZone` as `DISCARD`, `HAND`, or both as a list (`[HAND, DISCARD]`) where the text plays the card "from hand or from your discard pile": the least X that brings a matching card within the pool from any of them (Endless Night 103_96). Use it as the `from` of the X that buys the discount, whether X is a number named (`ChooseANumber`) or a count of cards chosen as a cost. For "play X ... from your discard pile on ...", bound X with a `range` whose `to` is `ForEachInDiscard` over `<kind>,PlayableOn(<bearers>)` (Gondor Calls For Aid! 103_83, Q-0094).

Side-only choices and hindered cards, as written in a definition (Q-0098): `override: hindered` on a Discard, `hindered: true` on ChooseActiveCards, CanSpot and ForEachYouCanSpot, and the same word on the requirement that gates the offer. A place beside the side ("on bearer", "borne by a companion") is not "more specific". Not where the effect is itself a hinder (Q-0060), not where the text names a card type, culture, keyword, race or title, and not for tokens. A restore, and a card in a hand, deck, discard pile or stack, need nothing.

What a card's effect is forbidden ("burdens cannot be added", "threats cannot be removed by Free Peoples cards") binds the card whether or not it is hindered when the effect is carried out: a card that hinders itself as a cost still carries its ability out (CR 5.0) and still cannot do what it could not (F-0151). What applies TO a hindered card (its text, keywords, bonuses) is still nothing.

`EntersPlay` is for a card's own "When you play this ..., <do or pay something> or discard it" (Dunlending Rampager 5_1, Stampeding Chief 17_49) and for nothing else. It answers the entering of the card it is written on, after the standing texts (`ConstantlyCheck`) have acted on the card and before its play is announced, so the choices in the text are made before any `Played` trigger or response is gathered. If the card is discarded there it was not played: no `Played` trigger of any card, no response, a play of it as a cost is unpaid and it is not counted by `PlayedCardThisPhase`. If it stays, the play is announced and answered as usual. Fields: `fromZone` only (`hand`, `discard`, `deck`, `stacked`, `dead`), for "if you play this from your hand"; it has no `filter`. A "when you play this" that leaves the card in play (hinders it, exerts it, fetches something) stays a `Played` trigger with `filter: self`. The sentence in the F-0139 paragraph, "A card's own 'when you play this ..., or discard it' stays a `Played` trigger", is reversed by this.

`support` is a "support card": a card in a support area, which is a card lying in the support zone or borne, at any depth, by a card that is (Ketura on Q-0099). An ally and a site a player controls lie there, so what they bear is a support card; what a companion or a minion bears is not. Never write `or(support,AttachedTo(support))`. `zone(support)` is the zone alone, the card itself lying in the support area: use it for "transfer this from your support area" and "if this is in your support area" (`self,zone(support)`), with `fromSupport: true` on the Transfer. On a `UsesSpecialAbility` trigger, `fromZone: SUPPORT` means the card was a support card in the wide sense when its ability was used; every other `fromZone` value is the zone alone.

`support(<whose>)` is "in <that player's> support area", of a card other than the one whose text it is: `support(you)` (also `support(your)`), `support(opponent)`, `support(free people)`, `support(shadow)`, `support(owner)`. A card is in the support area of the player in whose support area the card at the bottom of its chain of bearers lies, whoever owns it (Ketura on Q-0101): the Shadow player's condition on your ally is in YOUR support area; a card on a site a player controls is in that player's. A stacked card is in nobody's, and so is anything a companion or a minion bears. The plain `support` is "in a support area", anybody's ("all conditions in all players' support areas": `all(condition,support)`). Do not add `your` or a side to stand for the area: write "a condition from the Free Peoples support area" as `condition,support(free people)`, which reaches the Shadow player's own condition on an ally, as the text does. `zone(support)` is still the card itself lying in the support zone ("transfer this from your support area", "if this is in your support area").

`hasAttachedCount(N,<filter>)` counts the cards a card bears that the filter accepts, hindered cards left out. Where the text counts "cards" and names nothing more ("bearing 3 or more cards"), write `hasAttachedCount(3,any,override(hindered))`: the last word has the count take in hindered cards, as `override: hindered` does on an effect (Ketura on Lying Counsel 15_85). `override(...)` is read by `hasAttachedCount` alone. Not for a count that names a type, a class or a culture: those do not see a hindered card (Q-0098). Among the values, `forEachHasAttached` takes `hindered: true`, as `forEachYouCanSpot` does: for "each card he or she bears" and "each card borne by that character" (Fords of Isen 13_191, Ulaire Cantea 17_139); a count over `attachedTo(...)` written with `forEachYouCanSpot` takes the same field (Vengeance 4_208).

`shuffleCardsFromDiscardIntoDrawDeck` and `shuffleCardsFromHandIntoDrawDeck` shuffle the deck once the effect is carried out, whether or not any card was chosen or found (Ketura on Q-0102). So an optional trigger whose effect is "shuffle up to N ... from your discard pile into your draw deck" always does something: do not give it a `HasCardInDiscard` requirement. `shuffleCardsFromPlayIntoDrawDeck` still does nothing when no card is chosen (open: Q-0112).

A `hinder` effect never offers or takes a site, a Map or a Race to Mount Doom meta-site (CR 5.0; Ketura on Q-0110): a definition does not except them by hand. A `discard` never offers or takes The One Ring, hindered or not, nor the Ring-bearer (CR 4.2; Ketura on Q-0109): a definition does not write `not(The One Ring)` or `not(ring bearer)` for it, and with either the only candidate the effect does nothing.

`PlayedCardThisTurn` (`filter`, `min` default 1, `max` default 100, `atCurrentSite` default false) is `PlayedCardThisPhase` over the whole turn. With `atCurrentSite: true` it counts only the cards played while the fellowship was at the site it is at now. "The first X played at <this site> each turn is -N" is a `modifyCost` requiring `{type: not, requires: {type: playedCardThisTurn, filter: X, atCurrentSite: true}}` (Deep of Helm 4_347); "the first X played each Shadow phase" keeps `playedCardThisPhase` and wants a phase requirement beside it. Both count a permanent from the moment its play is announced and drop it again if the card has left play when the play is complete.

`DuplicateGameText` takes `snapshot: true`: it remembers the `from` cards once, when the modifier is made, and gives their game text until the modifier ends, though they have since left play or their stack. Use it, with the same word on the `AddKeywordFromCards` beside it, for a text that gives another card's game text for a time: "until the end of the turn, that Southron gains the game text of that stacked Man" keeps text and keywords "even if the Man is later lost" (Ketura on F-0160, War Howdah 103_62). `filter` is still only `self` or a `memory(...)`.

`cantAddBurdens` (modifier): burdens cannot be added by the cards `filter` names (default `any`); `requires` as usual. As an effect the burden is simply not added; as a cost it cannot be paid, so the action is not offered. (Until 2026-10-07 the word built `cantRemoveBurdens`: F-0159.)

`Memorize` / `MemorizeActive` take `hindered: true` to take hindered cards into the memory (default false), as on `ChooseActiveCards`. `ReturnToHand` takes `override: hindered`, as `Discard` does. So for a text that names its cards by side alone (Q-0098): `override: hindered` on Discard and ReturnToHand, `hindered: true` on ChooseActiveCards, Memorize, CanSpot and ForEachYouCanSpot. "All ..." and "each ..." by side alone reach hindered cards as "a ..." does (Ketura, 2026-10-07: "Good call on all/each").

"The twilight cost of the first X played each Shadow phase is -2" is a `modifyCost` that requires `{ type: phase, phase: shadow }` beside `not playedCardThisPhase`; without the phase a card played in a skirmish by another card's text takes the discount (Great Chasm 2_118, Breeding Pit of Isengard 15_188).

`ShuffleCardsFromPlayIntoDrawDeck`: With `includeStacked: true` each card goes into its OWNER's draw deck, the cards stacked on the chosen card included, and each deck that receives a card is shuffled once; `player` says who chooses, not whose deck. (Without `includeStacked` the cards go into the deck of the player `player` names: use it only for that player's own cards.)

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
| `AddTrigger` | trigger, until?, optional?, requires?, cost?, effect? | Spawn temporary trigger. With `optional: true` it is offered only to the player who performed the action that added it |
| `AlterOverwhelmMultiplier` | select, multiplier?, until?, memorize? | Change overwhelm threshold |
| `DisableSkirmishAssignment` | select, until? | Can't be assigned |
| `DisableWounds` | select, until?, memorize? | Can't take wounds |
| `EnableParticipationInArcheryFireAndSkirmishes` | select, count?, until?, requires? | Ally combat |

### Discrete (One-Time)
**Wounds/Vitality:**
`Wound(select, count?, times?, player?, memorize?)`, `Exert(select, count?, times?, player?, memorize?)`, `Exhaust(select, count?, player?, memorize?)`, `Heal(select, count?, times?, player?, memorize?)`, `Kill(select, count?, player?, memorize?)`

**Tokens:**
`AddBurdens(amount?, player?)`, `RemoveBurdens(amount?, player?)`, `AddThreats(amount?, player?)`, `RemoveThreats(amount?)`, `AddTwilight(amount?, memorize?)`, `RemoveTwilight(amount?, memorize?)`, `AddCultureTokens(select, culture, count?, memorize?)`, `RemoveCultureTokens(select, culture?, count?, memorize?)`, `ReinforceTokens(select?, culture?, player?, times?, memorize?)`
`AddCultureTokens` and `ReinforceTokens` do nothing to a hindered card (Q-0070).

**Card Movement:**
`Discard(select, count?, player?, memorize?)`, `ReturnToHand(select, count?, player?)`, `Transfer(select, where, checkTarget?, memorize*)`, `StackCardsFromPlay/Hand/Deck/Discard(select, where, count?)`, `RemoveFromTheGame(select, count?, player?, memorize?)`

**Playing Cards:**
`PlayCardFromHand(select, on?, discount?, memorize?)`, `PlayCardFromDiscard(select, on?, discount?, player?, memorize?)`, `PlayCardFromDrawDeck(select, on?, discount?, shuffle, showAll, memorize?)`, `PlayCardFromDeadPile(select, discount?, memorize?)`, `PlayCardFromStacked(select, on, discount?, memorize?)`, `PlayNextSite(filter?, memorize?)`
A top-level `AddThreats` cost is known to the action's requirements, to the advance check of `PlayCardFromDeadPile`, `PlayCardFromDiscard`, `PlayCardFromHand` and `PlayCardFromStacked`, and to the filter `PlayableFromDeadPile`: a card that must itself add a threat to be played is not counted as playable when the action's own threats leave it no room. An `AddThreats` inside an `If`, `Choice` or `Optional` is not counted.

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
- A cost that selects from `memory(X)` is really checked, and the action not offered when it cannot be paid, where X is filled before costs are checked: in the `cost` of a Trigger, Response or ResponseEvent, what its `trigger` memorizes; in the `cost` of an `AddTrigger`, any memory its own costs do not memorize (the adding action has filled it). A cost that selects from memory an earlier cost of the same action fills cannot be checked beforehand and still needs `ignoreCostCheckFailure: true` on it.

---

## Player Resolution

`You`, `FP` / `Freeps` / `Free Peoples`, `Shadow` / `S`, `Owner`, `FromMemory(X)`, `OwnerFromMemory(X)`

---

## Text Substitution

In `text` fields: `{culture}` → symbol, `{you}` → performing player, `{owner}` → card owner, `{self}` → card link, `{memoryVar}` → stored card(s) or string
