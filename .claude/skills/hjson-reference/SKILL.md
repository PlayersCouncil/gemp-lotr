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

`cardInfo` is the block at the head of a definition (`//id`, `parent`, `variant`, `variantPath`, `revision`, `image`,
`collInfo`, `rarity`). It is not a source of truth, and it is kept true to the rest of the definition (Q-0145): on a
PC erratum `//id` is the erratum's own id and revision (`54_192.1`, never `4_192.1`: Q-0158), `parent` is the
original's id, `variant` ERRATA, `variantPath` errata/pc and `collInfo` / `rarity` the original's; on a revised
V-set card `variant` is ERRATA and `variantPath` errata/pc. `cardinfo.py <id>` (campaign scripts) lists where a
block departs, kind by kind; its docstring has every kind's convention.

An erratum is written out as the Decipher definitions are (Q-0158): `unique:` on every card, `side:` on every card
with a culture, values with the Decipher capitals (`type: Companion`, `race: Uruk-hai`, `itemclass: Hand weapon`,
`keywords: Support Area`), and the keys `keywords:` and `timewords:`. The game reads the older, shorter style the
same (the side from the culture, not unique by default, any capitals); the Hobbit sets still use it throughout.

---

## Game Text Types (Effect Types)

Each entry in `effects: []` has a `type` field. The type determines the structure.

### `noEffectUnless` (the no-effect flag)

`noEffectUnless` (on Activated, ActivatedInDiscard, ActivatedFromStacked, ActivatedInDrawDeck, Event, ResponseEvent, Trigger, Response, and the AddActivated modifier): one requirement or a list, written exactly like `requires`. It never withholds the action: the action is offered as usual, and while any of these requirements fails the decision flags it `noEffect: effect`, so the client shows it differently and the player may still take it. It is where a guard goes that used to keep an event, or an ability with a cost, from being used to no purpose (Q-0034 forbids withholding those). The requirements are checked in order in the action's own context when it is put before a player, so one may `memorize` what the next reads (Breaching Shot 102_21).

Do not declare what the game already sees. An action whose every effect is modifyStrength, addKeyword, alterOverwhelmMultiplier, wound, heal, exert, exhaust, discard, cancelSkirmish, removeBurdens, removeThreats or drawCards (alone or inside if, choice, costToEffect, multiple, optional, preventable, repeat) is flagged by itself when none has anything to act on. An ability that has reached its limit, or whose capped bonus is spent, is flagged `limit` by itself (see "Limits" under Activated). A skirmish ability or Skirmish event that only gives strength, damage or overwhelm protection for the current phase is flagged `skirmish` when it reaches nobody in the skirmish being fought. Nothing is judged where a cost could make what the effect needs (an exertion before a heal), where a select reads `memory(...)`, or where a cost is of a kind the judge does not know: a false flag is worse than a missing one. One reading of memory is seen: an event, or an action with a cost, whose first effect is `RevealTopCardsOfDrawDeck` or `RevealBottomCardsOfDrawDeck` and whose other effects only deal with the cards revealed (a `ChooseArbitraryCards` from them; `PutCardsFromDeckIntoHand`, `PutCardsFromDeckOnTopOfDeck`, `PutCardsFromDeckOnBottomOfDeck` or `DiscardCardsFromDrawDeck` whose `select` has `memory(<them>)` as a part and no `shuffle`; an `If` on `MemoryMatches` of them; a `Repeat` of `ForEachInMemory` of them) is flagged `effect` by itself while that deck is empty (Q-0163). It takes no `noEffectUnless` and no `HasCardInDrawDeck`.

An ability or optional trigger with NO cost that would do nothing is not a flag matter: by Q-0034 it is not offered. The engine withholds it without a word in the definition wherever its effects can say that they will do nothing (Q-0224, `IdleOffers`; card-test-campaign `shapes.md`, "An optional trigger or ability that costs nothing", lists the kinds of effect that can and cannot); a `requires` is written only where the effect is of a kind that cannot.

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

A cap written inside the effect, as opposed to on the ability, leaves the ability on offer whatever it costs: `limitPerCardThisPhase` on ModifyStrength, or a `CardAffectedLimitPerPhase` / `CardPhaseLimit` value as the amount of ModifyStrength or AddKeyword or the count of DrawCards. Once the cap is spent for everyone the effect could reach, the action is flagged `limit` (where the rest of it can be judged: see `noEffectUnless` above). The flag is the whole action's: it is set only when every other effect of the action would do nothing as well, and a spent cap is read on those three effects alone (and on an `If` whose one check is a `PerPhaseLimit` / `PerTurnLimit`). A cap on anything else is not read and the action reads `false` at it: Sting 1_313 and 51_313, a `CardPhaseLimit` as the amount of RemoveTwilight after a reveal that still happens (`mechanics/NoEffectFlagTests`).

`limitPerCardThisPhase` on ModifyStrength and ModifyResistance is counted for the SOURCE card for the phase, whoever the bonus is given to (F-0125, CR 4.2 "limit"): a "(limit +3)" cannot be given in full to each companion in turn. A `CardPhaseLimit` may take a `Conditional` as its `limit` ("limit +3 unless you can spot ..."); the no-effect judge reads such a cap as it stands, so the ability is flagged `limit` once it is spent. A limit that falls below what the card has already given leaves nothing to give and takes nothing back (F-0267).

A printed "(limit -N)" on a play's twilight cost is the count's own `limit` inside the `discount`, a cap on each play: `discount: { type: ForEachYouCanSpot, filter: ..., limit: 2, multiplier: -1 }` (Uruk Regular 54_192; Ketura on Q-0152: a restriction on a discount is not read as a strength bonus limit is). A `CardPhaseLimit` or `CardAffectedLimitPerPhase` is not written in a `discount` or `maxDiscount`, nor in the `discount` of a `Playable` requirement: the engine works a discount out every time it asks what can be played, and each asking would spend the limit. A `CardPhaseLimit` takes an amount of zero or more; an amount below zero is not capped.

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
On a site, an optional Trigger with no `player` is offered to the Free Peoples player (whose fellowship it is), not to the site's owner or the Shadow player: "the (first) Shadow player may ..." on a site wants `player: shadow`, or a required Trigger round an `Optional` effect with `player: shadow` (Courtyard Parapet 63_188, F-0288).

The limit fields are as under Activated, "Limits": `limitPerPhase` / `limitPerTurn` withhold a Trigger at its limit; under `effectLimitPerPhase` / `effectLimitPerTurn` an optional Trigger with a cost is still offered at its limit, flagged, and a required one, or one with no cost, is not. The limit is counted in the phase the trigger goes off in unless `phase` names one. On a required Trigger write the limit as the field, not as an `If` on `PerPhaseLimit` inside the effect: one event that raises the trigger twice (two minions losing one skirmish) then has the second dropped once the first has taken the use, where the `If` leaves it to be ordered and to do nothing (Dwarven Axe 1_9, F-0199; F-0145).

`addTrigger` (the effect that gives a trigger to a card until some time) takes `limitPerPhase: N` too: it limits how often the added trigger goes off in a phase, counted on the card that added it (the event), like a card's own trigger's `limitPerPhase`. A required trigger raised by the same event beside one that takes the last use is dropped, not ordered (F-0145). Use it for "If a <character> loses/wins this skirmish ..." where several characters may match: that sentence is one effect, once, however many lose (Ketura on Q-0113; Ancient Enmity 1_29, 51_29).

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
| `InHandTrigger` | Triggers that work from hand |
| `KilledTrigger` | Self-reacts to being killed |
| `SelfRemovedFromPlayTrigger` | Triggers on own discard/hinder |
| `CopyCard` | Copies another card's game text: its actions, triggers and modifiers, looked up live, and no keyword. `self` in the copied text is the copying card (shapes.md, F-0381) |
| `DeckBuildingRestriction` | Deck validation rules (maps) |
| `AidCost` | Follower aid cost |
| `DisplayableInformation` | text: a line the client shows in the card's info under "Extra information"; `{stored}` in it is the value the card has stored on itself (`Named race is {stored}`, 69_38) |

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
| `BarredFromArcheryFire` | A character a card says may not participate in archery fire (Streets of Edoras 4_340); used as `Ally,not(BarredFromArcheryFire)` for an ally who can be sent archery wounds |
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
| `Name(X)` / `Title(X)` | title | Matches card title, with accents, spaces and capitals passed over (`Names.SanitizeName`): `name(Théoden)` finds Theoden, Last of His House 102_55, whose title has no accent |
| `Subtitle(X)` | subtitle | Matches the card's subtitle, passed over the same way: `bearer,or(subtitle(Of Many Colours),subtitle(Agent of the Dark Lord))` (Saruman's Staff 4_174, F-0426) |
| `Race(memory(X))` | memory | Race of memorized card |
| `Side(X)` | side | FP or Shadow side |
| `Signet(X)` | signet | Has signet X |
| `SiteNumber(X)` | int/range | Site number matches |
| `RegionNumber(X)` | int/range | Region number matches |
| `AttachedTo(filter)` | filter | Attached to matching card. A HINDERED bearer matches only where the filter is one word that names it as a card (`self`, `bearer`, `memory(...)`, `ring-bearer`, `any`), never by a type, race, culture, title, keyword or number (Q-0108): see below |
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
| `AllyHome(block,N)` | block, int | Ally one of whose homes is site N of that block, wherever the fellowship is. The block is `F`, `T`, `K`, `H`, or the name `Fellowship`, `Towers`, `King`, `Hobbit`, `TWO_TOWERS`: any other word (`tower`) loads without complaint and matches nothing |
| `Timeword(X)` | phase | Event phase matches: the phases printed on the card, except that an event printed with several phases is, while it is being played or priced for its play (a `played` trigger, a `modifyCost`), an event of the phase it is played in alone (F-0292) |
| `ReplaceableFromDiscard(filter)` | filter | Performing player's discard pile holds a card matching `filter` with this card's title and another subtitle: what `ReplaceCardInPlayWithCardInDiscard` can act on. Use it in both the requirement and the choice of the card to replace |

---

`PlayableOn(<filter>)` (function filter, engine pass 7): a card in a hand or a pile that attaches and could be played NOW on a card the inner filter accepts. Use it wherever a text plays items "on" named bearers and the action should be withheld, or X capped, when nothing fits (F-0140, Q-0094): `HasCardInDiscard` with `filter: item,PlayableOn(memory(playedMinion))`.

`TransferableTo(<filter>)` (function filter, engine pass 21, F-0324): a card in play that a card's effect could transfer NOW to a card the inner filter accepts: nothing says it cannot be transferred ("Possessions cannot be transferred from this companion", Warrior of Rohan 17_104), and one of those cards is an eligible bearer for it other than the one it has (its own "Bearer must be ...", one item of a class, no "cannot bear" in the way, not hindered). It is the choice `Transfer` makes of its card where it has `checkTarget: true` and a `where` that is a `choose(...)`; every `Transfer`, whatever its fields, leaves out a card that cannot be transferred (F-0340, engine pass 24), so no definition writes that. Write it in the requirement beside such an effect, with the effect's `where` filter inside it, so that "you may transfer ... to another eligible bearer" is withheld when a yes would move nothing (Q-0034): `CanSpot` with `filter: possession,attachedTo(memory(dyingCompanion)),transferableTo(character,not(memory(dyingCompanion)))` (Race Text 94_4). Where the `where` names its bearer outright (`self`, `bearer`, a memory) the effect narrows nothing by that bearer, `checkTarget` or no: "Transfer a [rohan] possession that Gamling can bear to him" writes the word in the choice itself, `select: choose(culture(rohan),possession,transferableTo(self))` with `where: self` (Gamling 18_99, F-0364), and with no cost and nothing he could take the game then withholds the ability with no requirement written (Q-0224). It does not read the allies' rules of the Free Peoples player's own fellowship-phase transfer.

`AssignableToSkirmishAgainst(<filter>)` (function filter): a character that the text of a card could assign NOW to a card the inner filter accepts. It asks what `AssignFPCharacterToSkirmish` goes on to ask with the flags that effect has when the definition names none (`ignoreExistingAssignments: false`, `ignoreDefender: true`, `ignoreAllyLocation: false`): both characters unassigned (CR 4.2 "assignment phase": "You cannot do this unless both of them are unassigned"), the ally at home or made to take part, nothing saying either cannot be assigned. Defender +X is not counted for either player (F-0355, engine pass 24): it is for the Free Peoples player's own assigning, which no definition asks about. An effect written with `ignoreExistingAssignments: true` (Mordor Assassin 7_284) has no word that asks its question.

`AssignableToSkirmishAgainstIgnoringAllyLocation(<filter>)` (function filter, engine pass 21, F-0283): `AssignableToSkirmishAgainst` asked of an ally as if a card had already made him participate in skirmishes. Where his home is does not count; everything else that stops an assignment does (a card that bars him from skirmishes, an assignment he or the other character already has, "cannot be assigned", a minion that is not fierce in the fierce round). It is what `AssignFPCharacterToSkirmish` goes on to ask once an `EnableParticipationInSkirmishes` before it has been carried out. Write it in both the requirement and the choice of "spot an ally to make that ally participate in skirmishes and assign this minion to skirmish that ally", so that the ability is withheld when it could assign the minion to nobody (Q-0034) and offers only the allies it could: `CanSpot` with `filter: ally,AssignableToSkirmishAgainstIgnoringAllyLocation(self)`, and `select: choose(ally,AssignableToSkirmishAgainstIgnoringAllyLocation(self))` (Dunlending Warrior 4_18, Dunlending Ravager 4_15).

## Value Resolution

Numeric fields (`amount`, `count`, `times`, `value`) accept literals or evaluated expressions.

### Literals
- Integer: `3`
- Range: `"0-3"` (player chooses); `any` is 0 or more and `anyNonZero` 1 or more ("discard X cards from hand to ..." as an optional trigger's only cost starts at 1: shapes.md, F-0300)
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
| `CardPhaseLimit` | limit, amount | Cumulative phase cap, counted on the card's own phase counter; leaves the ability on offer likewise. The amount is zero or more; not for a play's `discount` (see Activated, "Limits") |
| `FromMemory` | memory | Integer from memory |
| `Subtract` | firstNumber, secondNumber | Math |
| `Sum` | source[] | Add values together |
| `Min` / `Max` | firstNumber, secondNumber | Math |
| `Range` | from, to | Dynamic range |
| `LeastDiscountToPlay` | filter, fromZone (DISCARD, HAND, or both as a list), max, player? | The least X from 0 to `max` at which some card matching `filter` in that player's discard pile or hand (as `fromZone` says) can be played at twilight cost -X, by the same check a play makes (roaming, the card's own cost text and standing modifiers counted). Use it as the `from:` of the ChooseANumber in "Remove X ... to play ... from your discard pile; its twilight cost is -X" (102_39, 8_50; F-0067). Where no X up to `max` will do it answers `max`; the ability's `requires: Playable` with the full discount is what withholds it then |

---

## Duration Resolution

For `until` field on ongoing effects:
- `end(current)` — end of current phase (default)
- `end(Skirmish)` — end of skirmish phase
- `start(Regroup)` — start of regroup (a bare `Regroup` is the same)
- `EndOfTurn` — end of turn
- `EndOfGame` / `Permanent` — never expires

These are the only forms. A `start(...)` or `end(...)` whose word is not a phase is refused and the card is not loaded: the turn is not a phase, so the end of the turn is `EndOfTurn`, never `end(turn)` (which used to be read, silently, as `end(current)`). The `oldPhase` and `newPhase` of a `DuplicateActionFromPhase` modifier are refused the same way (`definitions/UnknownPhaseInADefinitionTests`, among the server module's tests).

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
| `CanAddBurdens` | player? | A burden could be added by this card now: false while the Ring-bearer is hindered or a `CantAddBurdens` reaches the card. For a standing text that must know ahead whether a burden it adds later can be added (69_38, F-0308) |
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
| `HasCardInRemoved` | player?, filter?, count? | Cards removed from the game, in ONE player's pile: `player` defaults to `you`. A text that names no owner writes an `or` over players (13_50, F-0375) |
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

No requirement and no value reads the Rule of 4 (whether a player may still draw this fellowship phase). Barrows of Edoras 4_337 stands "not yet used this phase" in for it: an `Or` of its other requirement and `PerPhaseLimit` 1, with an `IncrementPerPhaseLimit` as the first effect (F-0429; Q-0249, open).

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

Raised in that order within one move. `MovesFrom` and `Moves` are answered before the player marker is moved: a `Location`, site number or region in their blocks reads the site being left. `AboutToMoveTo` and `MovesTo` read the site arrived at, and the move's twilight is added last (CR 4.2 "moving your fellowship"; Q-0184).

### Timing
`StartOfTurn`, `StartOfPhase(phase?)`, `EndOfPhase(phase?)`, `EndOfTurn`

### Before (can prevent)
`AboutToTakeWound(filter, source?)`, `AboutToBeKilled(filter)`, `AboutToBeOverwhelmed(filter)`, `AboutToDiscard(filter, source?, opponent?, memorizeSource?, memorizeDiscarding?)`, `AboutToExert(filter)`, `AboutToHeal(filter)`, `AboutToAddBurden(filter)`, `AboutToHinder(filter, source?, player?)`, `AboutToRestore(filter, source?, player?)`, `AboutToDrawCard(player)`, `BeforeThreatWounds(filter)`, `BeforeToil(filter)`, `FPStartedAssigning`

On `AboutToDiscard`, `source` filters the CARD that causes the discard ("a Free Peoples card is about to discard"; a site has no side) and `opponent: true` asks that the PLAYER carrying it out is not this card's owner ("the Free Peoples player / an opponent is about to discard"), whatever the source: a site's text, or a card that makes the other player do the discarding (Deceit 68_29, F-0296).

### After
`Played(filter, on?, from?, memorize?)`, `TakesWound(filter, source?, memorize?)`, `Exerted(filter, source?, memorize?)`, `Heals(filter, player?, memorize?)`, `Killed(filter, killer?, cause?, inSkirmish?, memorize?)`, `Discarded(filter, source, fromZone?, memorize?)`, `WinsSkirmish(filter, against?, memorize?, memorizeLoser?)`, `LosesSkirmish(filter, against?, overwhelm?, memorize?)`, `AssignedAgainst(filter, against?, side?, memorize*)`, `AssignedToSkirmish(filter, side?, memorize?)`, `StartOfSkirmishInvolving(filter, against?)`, `AfterAllSkirmishes`, `AddsBurden(filter?, player?)`, `AddsThreat(filter?)`, `AddsTwilight(filter?, cause?)`, `PlayerDrawsCard(player, orTakesIntoHand?)`, `PutsOnRing`, `TakesOffRing`, `Reconciles(player?)`, `Transferred(filter?, to?)`, `SiteControlled(player?, filter?)`, `SiteLiberated(player?, filter?)`, `Hindered(filter?)`, `Restored(filter?)`, `RemovedFromPlay(filter?)`, `FPDecidedToMove`, `FPDecidedToStay`, `FPDecidedIfMoving`, `ConstantlyCheck(requires)`, `CancelledSkirmish(filter?)`

`Discarded`'s `source` is a filter on the card that did the discarding. With it written, `source: any` included, a discard the engine emits with no source card is refused: a kill, the discard of the minions as the turn ends, and the cards that go with a card stacked, returned to hand, shuffled in or put on a deck from play (Gnawing, Biting, Hacking, Burning 6_63, whose "a regroup action discards" rests on it). Without `source` the trigger answers all of those but the kill: a character that was killed was not discarded, though a killed minion is placed in the discard pile (CR 4.2 "killed": "Cards that trigger when a character is killed will not trigger when a character is discarded, and vice-versa"; F-0444, mended by engine pass 26). A text that says "killed or discarded" writes a `Killed` trigger beside the `Discarded` one, as a list (Last Days 12_71, Throne of Minas Tirith 17_36), and is then offered once for either. `RevealedCardFromHand(filter?, player?)`: `player` is compared with the OWNER of the card that caused the reveal, not with whoever is made to reveal (Eye of Barad-dûr 5_96; Q-0251, open).

`PlayerDrawsCard(player, orTakesIntoHand?)` answers a card DRAWN and nothing else: a card taken into hand from a discard pile, a stack or the removed pile, or from a draw deck by a look, a reveal or a search, is not drawn (F-0376, mended by engine pass 26). A text that says "draws a card (or takes a card into hand)" writes `orTakesIntoHand: true` (Verily I Come 2_94, Betrayal of Isengard 3_29 and 12_25); "draws a card" alone writes nothing (Morgul on the March 7_195). A card taken IN PLACE of a draw is the card drawn (Q-0205: B): Race Text 94_28's is made so by the engine, and a `PutCardsFromDeckIntoHand` that follows the `preventEffect` of an `AboutToDrawCard` trigger says `inPlaceOfDraw: true` (The Mirror of Galadriel 15_22).

`Restored(filter?, source?, player?, memorize?)` and `Hindered(filter?, source?, player?, memorize?)`: with no `source` written, every restore or hinder is answered, the one no card makes included, which is every reconcile's restore (F-0361, mended by engine pass 24). A `source` is a condition on the card that did it, and a reconcile's restore does not meet it: "when this is restored" writes none. No definition uses `Restored` now: Andúril 7_79 and Shadowfax 4_100 had it for "Discard ... he bears" (F-0360), which is a standing discard, a `ConstantlyCheck` (Q-0223; below), and answers a restore without it. `AboutToRestore` and `AboutToHinder` need a `filter`, and let a restore or hinder by no card through whatever their `source` says (as built).

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
In an `AddActivated`'s `requires`, `cost` and `effect`, `self` and "you" are the card that GAINS the ability and its owner (or the `player` the modifier names), not the card the modifier is on (name that one: `hasAttached(name(...))`). `text:` is what the player is shown for the action: it names the card it is on (F-0309, F-0351, F-0352).

### Prevention Modifiers
`CantTakeWounds(filter, requires?)`, `CantTakeMoreWoundsThan(filter, amount?, phase?, requires?)`, `CantBeExerted(filter, by?, requires?)`, `CantHeal(filter, player?, requires?)`, `CantBeDiscarded(filter, by?, player?, requires?)`, `CantBeOverwhelmed(filter, requires?)`, `CantBeOverwhelmedMultiplier(filter, multiplier?, requires?)`, `CantBeAssignedToSkirmish(filter, player?, requires?)`, `CantBeAssignedToSkirmishAgainst(fpCharacter, minion, side?, requires?)`, `CantBeTransferred(filter, requires?)`, `CantBear(filter, cardFilter?, requires?)`, `CantPlayCards(filter, requires?)`, `CantPlayPhaseEvents(phase, player?, requires?)`, `CantPlayPhaseEventsOrPhaseSpecialAbilities(phase, player?, requires?)`, `CantUseSpecialAbilities(filter?, phase?, player?, requires?)`, `CantMove(requires?)`, `CantCancelSkirmish(filter?, requires?)`, `CantPreventWounds(requires?)`, `CantRemoveBurdens(filter?, requires?)`, `CantRemoveThreats(filter?, requires?)`, `CantTakeArcheryWounds(filter, requires?)`, `CantPlaySite(player, requires?)`

`CantHeal` with no `player` stops every heal of the cards, whoever makes it ("bearer cannot heal"); with `player` it stops that player's heals alone, and the other player heals the same cards as before ("you may heal no more than ...": `player: you` on a card's own modifier, `player: owner` on a meta-site; F-0319). `Heals` takes `player` too: whose heals the trigger answers.

Overwhelm is decided character by character (Q-0044). On a minion, `CantBeOverwhelmedMultiplier` and the `AlterOverwhelmMultiplier` subaction protect that minion only, measured against its own strength; other minions in the skirmish are overwhelmed at double the side's total. The `AboutToBeOverwhelmed` trigger and `LosesSkirmish` with `overwhelm: true` match only the characters actually overwhelmed, not a minion spared beside them.

### Other Modifiers
`AllyCanParticipateInArcheryFireAndSkirmishes(filter, requires?)`, `AllyCanParticipateInArcheryFire(filter, requires?)`, `AllyCanParticipateInSkirmishes(filter, requires?)`, `AllyMayNotParticipateInArcheryFireOrSkirmishes(filter, requires?)`, `AlliesTakeArcheryFireWoundsInsteadOfCompanions(requires?)`, `DisableGameText(filter, requires?)`, `DoesNotAddToArcheryTotal(filter, requires?)`, `ExtraCostToAssign(minion, cost, fpCharacter?, requires?)`, `ExtraCostToPlay(filter, cost, requires?)`, `FPCultureCount(amount, requires?)`, `HasToMoveIfAble(requires?)`, `ShadowHasInitiative(requires?)`, `SkipPhase(phase, requires?)`, `CanPlayStackedCards(filter, on, requires?)`, `CanBearExtraItems(filter, itemclass, amount?, requires?)`, `ItemClassSpot(class, requires?)`, `ModifyRaceSpotCount(race, requires?)`

"The Free Peoples player must <cost> to assign <minion> to skirmish <fpCharacter>" is the modifier `ExtraCostToAssign` (Q-0221; the effect type `AssignmentCost` is gone). The cost's effects say `player: fp`. It is paid as the Free Peoples player makes the assignment, by his own assigning or by a card's; while it cannot be paid he may not make that assignment, and unpaid (prevented) the assignment is not made. The Shadow player's assignments are never charged. No bar or trigger is written beside it (shapes.md).

An ally who "participates in archery fire" is counted for the fellowship archery total if an archer and may be given archery wounds (`AllyCanParticipateInArcheryFire`, `...AndSkirmishes`). "Allies take wounds from archery fire instead of companions" is `AlliesTakeArcheryFireWoundsInsteadOfCompanions`, which takes no filter: every ally, at home or away, takes the minions' archery wounds while one can, a wound none can take goes to a companion as usual, and no ally is counted for the total who would not be anyway (Q-0136, Q-0137). An ally under `AllyMayNotParticipateInArcheryFireOrSkirmishes` takes none of them (Q-0147): a definition that sends archery wounds to allies and has no cost requires `Ally,not(BarredFromArcheryFire)`. That modifier beats every `AllyCanParticipate...` modifier, every `EnableParticipation...` effect and `ignoreAllyLocation` on `AssignFpCharacterToSkirmish`, whatever card they are on (Q-0150): the engine answers it, and a definition adds nothing.

---

## Subactions (Effects)

Used in `cost: []` and `effect: []` arrays.

### Control Flow
| Subaction | Key Params | Purpose |
|-----------|-----------|---------|
| `Choice` | player?, texts[], effects[], requires?[], memorize? | Choose one of N options. `memorize` holds the text of the option taken, asked or not (read it with `MemoryLike`). `requires` is parallel to `texts`, one requirement object per option (`{type: AlwaysAvailable}` for one always offered), each object on its own lines: a one-line `{ type: X }` inside an array does not parse. The one option that can be carried out in full is taken unasked. As an effect with no such option, every option is put to the player, unless each is certain to do nothing (nobody to exert, no site to liberate): then nothing is asked (F-0116). An option that holds other effects says so from what it holds (engine pass 26): an `If` by its check, a `CostToEffect` by whether its cost can be paid, a play by whether a card can be played, a list by each effect in turn; one with an `Optional`, a `Repeat` or a `Preventable` in it, or that begins by remembering something, keeps the question. Write each option as the effect itself (`wound` with `times: 2`), not as a chooser for an effect after the `Choice`: it is by the option's own effect that the game knows whether it can be done in full (F-0310) |
| `CostToEffect` | cost[], effect[], requires? | Sequential cost-then-effect |
| `If` | check[], true[], false[] | Conditional execution |
| `Optional` | player?, text, cost?[], effect[], requires? | "You may" prompt. With a `requires` that is not met: not asked, and the effects after it are carried out (F-0379). With a `cost`: not asked when the cost cannot be paid, and the effect is not carried out when the cost is not paid (F-0306) |
| `Preventable` | player, text, cost[], effect[], instead?[] | "X may Y to prevent Z" |
| `Multiple` | effects[] | Group subactions |
| `Repeat` | times, effect[] | Do N times |
| `DoWhile` | check, effect[] | Do the effect, then again while the check holds. The check must go false by what the effect did (`MemoryMatches` on what it memorized), or the game hangs (shapes.md, F-0322) |
| `ForEachPlayer` | effect[] | Once per player |
| `SendMessage` | text | Log text (also "do nothing") |

`Exert` with `times: N` and a `choose(...)` selection offers only characters able to exert N times: outright when it is a cost; as an effect while anyone can do it in full, falling back to anyone able to exert when nobody can (F-0131). Do not add a vitality filter to say so. "Wound X twice" is `type: Wound` with `times: 2`, never an Exert (F-0148). A `Wound` with `times: N` and a `choose(...)` selection offers anyone able to take a wound, who takes what he can; only as an option of a `Choice` that was open because it can be done in full does it offer those able to take all N alone (Q-0209). Do not add a vitality filter there either.

The requirement `Playable` with `fromZone: DISCARD` (`filter`, `discount?`, `player?`) asks whether a matching card in that discard pile could be played now. On an EVENT it counts the event's own twilight cost as already gone from the pool (engine pass 21, F-0058), as `PlayCardFromDiscard` does when it is asked beforehand: a play from the discard pile is treated as a cost, and both must be payable (F-0046). It is how an event says so where its play stands inside an `If` or a `Choice`, which the engine does not ask beforehand: `requires: { type: Or, requires: [ { type: Not, requires: { type: location, filter: regionNumber(3) } }, { type: Playable, fromZone: DISCARD, filter: culture(gollum),minion } ] }` (Little Snuffler 13_52: "If the fellowship is in region 3, play a [gollum] minion from your discard pile"). The filter word `Playable` and `HasCardInDiscard` do not count the event's cost; with `fromZone` DECK, HAND or STACKED the requirement does not either (no event with a cost of its own uses them: Above the Battlement 7_262, cost 0, has `fromZone: STACKED` with `filter: besieger,stackedOn(controlledSite)` beside a `fromZone: DISCARD` one, in an `Or`, since F-0465).

"Play any number of / up to N ... from your discard pile" as an optional trigger or ability needs a `HasCardInDiscard` requirement for what it plays (with `PlayableOn` where the bearers are named), so that it is not offered with nothing to play (Q-0086).

Inside a `preventable`, a cost is the preventing player's to choose unless it says otherwise: for "the Free Peoples player may make you X to prevent this", give the cost `player: owner`. `ChooseANumber` takes a `Conditional` for `from`.

An effect whose text picks "a card", "a Shadow card" or "a Free Peoples card" and nothing more specific may pick a HINDERED card: give it `override: hindered` (Q-0098). Anything more specific cannot reach a hindered card, and hinder choices leave hindered cards out (Q-0060).

A play in a `cost` (`PlayCardFromHand`, `PlayCardFromDiscard`, `PlayCardFromDrawDeck`, `PlayCardFromStacked`, `PlayCardFromDeadPile`) is paid only if the card is still in play when its play is complete, that is after everything that answered the play has finished. A card discarded, killed, returned to hand or stacked before then, by a standing text, by its own "when you play" or by a response, leaves the cost unpaid: the action's effect is not carried out, and the twilight and the other costs already paid stay paid. The same holds for a play as an effect that an "if you do" depends on. The action is still offered where the removal could be foreseen (Ketura on F-0139). `extraEffects` on a play are carried out after it whether the card stayed or not.

`Played` answers a play that has been announced, and a play is announced only for a card that is still in play after the standing texts (`ConstantlyCheck` triggers) have acted on it. A card discarded or killed as it came into play sets off no `Played` trigger, required or optional, its own or another card's, and no `ResponseEvent` or response ability with a `Played` trigger. A card's own "when you play this ..., or discard it" is an `EntersPlay` trigger (below), resolved before the play is announced.

`ConstantlyCheck` triggers are gathered for the entering step of every play, before the play is announced. A standing "discard ..." or "is discarded if ..." is written as one, never as a `Played` trigger.

Every `ConstantlyCheck` has a requirement that holds only while it has something to do, or it is carried out on every action and blips in the client (Ketura on Q-0229: "ConstantlyCheck in general should be carefully restricted like this lest it be a nuisance to play with"). For a standing discard of other cards that is a `CanSpot` of what it discards, as on Shadowfax 17_24: `trigger: {type: ConstantlyCheck}`, `requires: {type: canSpot, filter: another,possession,attachedTo(bearer)}`, `effect: {type: discard, select: all(other,possession,attachedTo(bearer))}`. "Discard <X> he bears" beside "cannot bear <X>" is that discard and a `cantBear` modifier, two effects (shapes.md, Q-0223). A change that lasts the turn is kept quiet by `limitPerTurn: 1` or a `PerTurnLimit` requirement.

A `ConstantlyCheck` with a `Location` anywhere in its `requires` (the trigger's own or the effect's, inside a `Not` or an `Or` as well) is a standing check on where the fellowship is: "discard this possession when at an underground site", "is discarded if not at a marsh". The engine does not look at it from the moment a move puts the marker on the next site until that move's twilight has been added, so the card's text still counts in the twilight (CR 4.2 "moving your fellowship"; Decipher on Bill the Pony 3_106, F-0233). The definition says nothing for this. A check on the site written as a filter instead (`CanSpot` with `currentSite`) does not wait: write `Location`.

`PlayedCardThisPhase` counts a permanent from the moment its play is announced, so a `Played` trigger that requires `PlayedCardThisPhase` with `min: 2, max: 2` sees the play it is answering (Ill Wind 103_67). The play is taken out of the count when it is complete if the card is no longer in play, and a card that was never announced is never counted. Events are always counted.

`limitPerSite: 1` is "(limit once per site)": a limit on uses, counted by the engine for the site the fellowship is at, cleared when the fellowship moves to another site and at the end of the turn, so it is once for each site in a turn (Q-0084, F-0146). Anything that happens as the fellowship arrives belongs to the site arrived at. As a requirement and an effect: `PerSiteLimit(limit?)` and `IncrementPerSiteLimit(limit?)`; prefer the field. Do not build it from per-phase limits and `MoveCountMinimum`.

A required Trigger with a limit on uses (`limitPerPhase`, `limitPerTurn`, `limitPerSite`, or a `PerPhaseLimit` / `PerTurnLimit` / `PerSiteLimit` written directly in `requires`) is carried out no more often than the limit even when one event raises it several times at once (two companions exerting together): the later ones are dropped (F-0145). A limit requirement nested inside `Not` / `Or` is not seen by that check: write the limit with the field.

`AddKeywordFromCards` takes `snapshot: true`: the keywords, with their numbers, are taken once, when the modifier is made, and given exactly so until the modifier ends, whatever becomes of the `from` cards (killed, hindered, changed). Use it inside an `AddModifier` for a text that copies keywords for a time ("until the regroup phase, each of your other Men gain each of the unloaded keywords on that Man", Q-0096). Leave it off a standing `Modifier`, which is read as the cards stand.

`LeastDiscountToPlay` takes `fromZone` as `DISCARD`, `HAND`, or both as a list (`[HAND, DISCARD]`) where the text plays the card "from hand or from your discard pile": the least X that brings a matching card within the pool from any of them (Endless Night 103_96). Use it as the `from` of the X that buys the discount, whether X is a number named (`ChooseANumber`) or a count of cards chosen as a cost. It takes no `multiplier` or `divider`: where each card chosen buys N, the count's `from` is a `sum` of it and N-1 with `divider: N` ("-2 for each Orc revealed": `sum` of it and 1, `divider: 2`, with `max` twice the Orcs in hand; Power and Terror 2_70, F-0392). For "play X ... from your discard pile on ...", bound X with a `range` whose `to` is `ForEachInDiscard` over `<kind>,PlayableOn(<bearers>)` (Gondor Calls For Aid! 103_83, Q-0094).

Side-only choices and hindered cards, as written in a definition (Q-0098): `override: hindered` on a Discard, `hindered: true` on ChooseActiveCards, CanSpot and ForEachYouCanSpot, and the same word on the requirement that gates the offer. A place beside the side ("on bearer", "borne by a companion") is not "more specific". Not where the effect is itself a hinder (Q-0060), not where the text names a card type, culture, keyword, race or title, and not for tokens. A restore, and a card in a hand, deck, discard pile or stack, need nothing.

What a card's effect is forbidden ("burdens cannot be added", "threats cannot be removed by Free Peoples cards") binds the card whether or not it is hindered when the effect is carried out: a card that hinders itself as a cost still carries its ability out (CR 5.0) and still cannot do what it could not (F-0151). What applies TO a hindered card (its text, keywords, bonuses) is still nothing.

`EntersPlay` is for a card's own "When you play this ..., <do or pay something> or discard it" (Dunlending Rampager 5_1, Stampeding Chief 17_49) and for nothing else. It answers the entering of the card it is written on, after the standing texts (`ConstantlyCheck`) have acted on the card and before its play is announced, so the choices in the text are made before any `Played` trigger or response is gathered. If the card is discarded there it was not played: no `Played` trigger of any card, no response, a play of it as a cost is unpaid and it is not counted by `PlayedCardThisPhase`. If it stays, the play is announced and answered as usual. Fields: `fromZone` only (`hand`, `discard`, `deck`, `stacked`, `dead`), for "if you play this from your hand"; it has no `filter`. A "when you play this" that leaves the card in play (hinders it, exerts it, fetches something) stays a `Played` trigger with `filter: self`. The sentence in the F-0139 paragraph, "A card's own 'when you play this ..., or discard it' stays a `Played` trigger", is reversed by this.

`support` is a "support card": a card in a support area, which is a card lying in the support zone or borne, at any depth, by a card that is (Ketura on Q-0099). An ally and a site a player controls lie there, so what they bear is a support card; what a companion or a minion bears is not. Never write `or(support,AttachedTo(support))`. `zone(support)` is the zone alone, the card itself lying in the support area: use it for "transfer this from your support area" and "if this is in your support area" (`self,zone(support)`), with `fromSupport: true` on the Transfer. On a `UsesSpecialAbility` trigger, `fromZone: SUPPORT` means the card was a support card in the wide sense when its ability was used; every other `fromZone` value is the zone alone.

`support(<whose>)` is "in <that player's> support area", of a card other than the one whose text it is: `support(you)` (also `support(your)`), `support(opponent)`, `support(free people)`, `support(shadow)`, `support(owner)`. A card is in the support area of the player in whose support area the card at the bottom of its chain of bearers lies, whoever owns it (Ketura on Q-0101): the Shadow player's condition on your ally is in YOUR support area; a card on a site a player controls is in that player's. A stacked card is in nobody's, and so is anything a companion or a minion bears. The plain `support` is "in a support area", anybody's ("all conditions in all players' support areas": `all(condition,support)`). Do not add `your` or a side to stand for the area: write "a condition from the Free Peoples support area" as `condition,support(free people)`, which reaches the Shadow player's own condition on an ally, as the text does. `zone(support)` is still the card itself lying in the support zone ("transfer this from your support area", "if this is in your support area").

`attachedTo(<filter>)` is "borne by <such a card>", and a hindered bearer has no title, culture, type, race or number to be such a card by (CR 5.0 "hinder"; Ketura on Q-0108). The engine leaves a hindered bearer out of every `attachedTo` that names a kind: `attachedTo(companion)`, `attachedTo(culture(rohan),man)`, `attachedTo(name(Boromir))`, `attachedTo(or(ally,and(unbound,companion)))`, `attachedTo(memory(loser),companion)`. Do not write `not(hindered)` for it. A hindered bearer is still found where the filter is ONE word that names him as a card: `self`, `bearer`, `memory(...)`, `ring-bearer`, `any` ("a Free Peoples item on bearer": `attachedTo(bearer)`; "each attached follower": `attachedTo(any)`). Any second word beside it, of whatever sort, leaves him out again (`attachedTo(memory(chosenCompanion),not(hindered))` does so on purpose), and so does every other single word: `hasAttached(self)` is no such word (for the bearer of this card write `bearer`), nor are `hindered` and `side(...)`, which no definition writes there.

`hasAttachedCount(N,<filter>)` counts the cards a card bears that the filter accepts, hindered cards left out. Where the text counts "cards" and names nothing more ("bearing 3 or more cards"), write `hasAttachedCount(3,any,override(hindered))`: the last word has the count take in hindered cards, as `override: hindered` does on an effect (Ketura on Lying Counsel 15_85). `override(...)` is read by `hasAttachedCount` alone. Not for a count that names a type, a class or a culture: those do not see a hindered card (Q-0098). Among the values, `forEachHasAttached` takes `hindered: true`, as `forEachYouCanSpot` does: for "each card he or she bears" and "each card borne by that character" (Fords of Isen 13_191, Ulaire Cantea 17_139); a count over `attachedTo(...)` written with `forEachYouCanSpot` takes the same field (Vengeance 4_208).

`shuffleCardsFromDiscardIntoDrawDeck` and `shuffleCardsFromHandIntoDrawDeck` shuffle the deck once the effect is carried out, whether or not any card was chosen or found (Ketura on Q-0102). So an optional trigger whose effect is "shuffle up to N ... from your discard pile into your draw deck" always does something: do not give it a `HasCardInDiscard` requirement. With no card chosen, or none still in play, `shuffleCardsFromPlayIntoDrawDeck` shuffles the draw deck of the player it is for (`player`, default `you`) all the same, with or without `includeStacked` (Ketura on Q-0112, as for the discard pile, Q-0102). A card that should not be used with nothing to shuffle in says so with a `requires` of its own (Mûmakil Commander 18_71: `canSpot` on the same filter as its `select`).

`PlayCardFromDiscard`, `PlayCardFromStacked`, `PlayCardFromDrawDeck`, `PlayCardFromHand` and the filter word `Playable`: a response event is never offered or counted by these, in the check before the costs or in the choice after them, unless it also has the current phase's timeword (F-0186). A filter needs no `not(timeword(response))` for it.

Costs are checked in the order written, each on the board the costs before it will leave, as far as that is certain: an `exert`, `wound` or `exhaust` cost of `self`, `bearer`, a memory the trigger filled, or a `choose(...)` with exactly as many candidates as it needs counts as done when the next cost, and any card the action then plays (PlayCardFromDiscard, FromHand, FromStacked, FromDeadPile), is asked whether it can be paid or played (F-0184). No requirement or field is needed for it. Where the player really chooses who exerts, the board is judged as it stands. Twilight the action removes first is still declared with `removedTwilight`.

A `preventable` puts its question to its player only when its `effect` is going to do something (Ketura on Q-0114). Just before asking, each effect inside is asked whether it is certain to do nothing on the board as it stands: no card to draw, a draw past the Rule of 4, nobody in the memory it selects from or nobody there who can still be reached, burdens that cannot be added, threats at their limit, no site to control, no skirmish it could cancel. Then nobody is asked, the cost is not paid, and the effect is carried out for the nothing it does. An effect that will happen in part (draw 3 with one card in the deck) is still asked. Do not wrap a `preventable` in `If` / `HasCardInMemory` to keep it from asking about nothing; the ones already written that way are harmless. What it cannot see, and what still needs an `If` of the card's own: an amount that comes to zero (`amount` or `times` counting something there is none of), and the effects that give no answer: `liberateSite`, `shuffleHandIntoDrawDeck`, `assignFpCharacterToSkirmish`, `stackCardsFromPlay`, `addModifier`, `addTwilight`, `addCultureTokens`. Do not fill a memory and read it inside the same `preventable` effect block: the question is asked before any of the block is carried out.

`OpenAndPlayFromBoosterPack` lets a revealed event be chosen only if it has the timeword of the phase the pack is opened in (so never an event of another phase, nor a response event that is not also an event of that phase), and plays the chosen card without its twilight cost. Uniqueness, the Rule of 9 and "cannot be played" still hold; requirements, extra costs and roaming are ignored as before. An event so played is not asked for the `cost` of its own `event` effect either (F-0316): each entry of `cost` is left out, except one that fills a memory (`memorize`: an X or a card the effect reads back) or that holds a `costToEffect`, which is carried out as written (Q-0201, open, asks what X should be).

An effect says what it will do through `EffectAppender.forecast(actionContext)`. The forecast is asked twice: while the action is on offer (the no-effect flag), and, with `actionContext.isAtResolution()` true, at the moment the effect is about to be carried out (a `preventable` deciding whether to ask). At resolution the costs are paid and memory is filled, so a card resolver answers from memory and from any zone. `EffectForecast.foundAtResolution(actionContext, cardResolver)` is the whole answer of an effect that only acts on the cards its resolver finds. An answer given only at resolution leaves the no-effect flag as it was. A forecast must never work out an amount.

A `hinder` effect never offers or takes a site, a Map or a Race to Mount Doom meta-site (CR 5.0; Ketura on Q-0110): a definition does not except them by hand. A `discard` never offers or takes The One Ring, hindered or not, nor the Ring-bearer (CR 4.2; Ketura on Q-0109): a definition does not write `not(The One Ring)` or `not(ring bearer)` for it, and with either the only candidate the effect does nothing.

A Race to Mount Doom meta-site's `Trigger` and `Activated` are collected for every player, whoever owns the card. `player: owner` on an optional Trigger (F-0311) and `requires: { type: IsOwner }` on an Activated (F-0312, 91_10) hold one to its owner; `OwnerIsFreeps` and `OwnerIsShadow` say only which side the owner is on now, which is enough for an action of a phase where one player alone acts.

`PlayedCardThisTurn` (`filter`, `min` default 1, `max` default 100, `atCurrentSite` default false) is `PlayedCardThisPhase` over the whole turn. With `atCurrentSite: true` it counts only the cards played while the fellowship was at the site it is at now. "The first X played at <this site> each turn is -N" is a `modifyCost` requiring `{type: not, requires: {type: playedCardThisTurn, filter: X, atCurrentSite: true}}` (Deep of Helm 4_347); "the first X played each Shadow phase" keeps `playedCardThisPhase` and wants a phase requirement beside it. Both count a permanent from the moment its play is announced and drop it again if the card has left play when the play is complete.

`DuplicateGameText` takes `snapshot: true`: it remembers the `from` cards once, when the modifier is made, and gives their game text until the modifier ends, though they have since left play or their stack. Use it, with the same word on the `AddKeywordFromCards` beside it, for a text that gives another card's game text for a time: "until the end of the turn, that Southron gains the game text of that stacked Man" keeps text and keywords "even if the Man is later lost" (Ketura on F-0160, War Howdah 103_62). `filter` is still only `self` or a `memory(...)`.

`OverrideUniqueness(filter, uniqueness, loosen?, requires?)`: `uniqueness` 1 to 4 is how many cards of the title may be in play and in the owner's dead pile together; 4 is no limit at all (`PlayUtils.checkUniqueness`), so "not unique" is `uniqueness: 4` with `loosen: true` (92_24, 94_16). Without `loosen` the override restricts, and a restricting override beats a loosening one.

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
`AddBurdens(amount?, player?)`, `RemoveBurdens(amount?, player?)`, `AddThreats(amount?, player?, default?)`, `RemoveThreats(amount?)`, `AddTwilight(amount?, memorize?)`, `RemoveTwilight(amount?, memorize?)`, `AddCultureTokens(select, culture, count?, memorize?)`, `RemoveCultureTokens(select, culture?, count?, memorize?)`, `ReinforceTokens(select?, culture?, player?, times?, memorize?)`
An `AddThreats` whose `amount` is a range asks its player "Choose how many threats to add", from the range's least to its most and never more than can still be added; `default` (a number or a counted amount) is what that question comes filled in with, held to what may be answered, and with none given it fills none in. "You may add threats up to the number of burdens you can spot" is `amount: {type: range, from: 0, to: {type: forEachBurden}}` with `default: {type: forEachBurden}` (Shelob, Menace 18_34; F-0419).
`AddCultureTokens` and `ReinforceTokens` do nothing to a hindered card (Q-0070). `ReinforceTokens` chooses only among cards its `player` owns (CR 4.2 "reinforce": "any of your cards"; F-0468), so a definition never writes `your` for it.

**Card Movement:**
`Discard(select, count?, player?, memorize?)`, `ReturnToHand(select, count?, player?)`, `Transfer(select, where, checkTarget?, fromSupport?, memorizeTransferred?, memorizeTarget?)`, `StackCardsFromPlay/Hand/Deck/Discard(select, where, count?)`, `RemoveFromTheGame(select, count?, player?, memorize?)`

**Playing Cards:**
`PlayCardFromHand(select, on?, discount?, memorize?)`, `PlayCardFromDiscard(select, on?, discount?, player?, memorize?)`, `PlayCardFromDrawDeck(select, on?, discount?, shuffle, showAll, memorize?)`, `PlayCardFromDeadPile(select, discount?, memorize?)`, `PlayCardFromStacked(select, on, discount?, memorize?)`, `PlayNextSite(filter?, memorize?)`
A top-level `AddThreats` cost is known to the action's requirements, to the advance check of `PlayCardFromDeadPile`, `PlayCardFromDiscard`, `PlayCardFromHand` and `PlayCardFromStacked`, and to the filter `PlayableFromDeadPile`: a card that must itself add a threat to be played is not counted as playable when the action's own threats leave it no room. An `AddThreats` inside an `If`, `Choice` or `Optional` is not counted.

**Hand/Deck Manipulation:**
`DrawCards(count?, player?)`, `DiscardFromHand(select?, count?, hand?, forced, player?, memorize?)`, `DiscardCardAtRandomFromHand(hand?, count?, forced)`, `DiscardTopCardsFromDeck(deck?, count?, forced, memorize?)`, `PutCardsFromDeckIntoHand(select?, count?, shuffle, reveal, showAll, inPlaceOfDraw?)`, `PutCardsFromDiscardIntoHand(select?, count?, player?, discard?, memorize?, text?)`, `RevealHand(hand?, memorize?)`, `RevealTopCardsOfDrawDeck(deck?, count?, memorize?)`, `RevealCardsFromHand(hand?, select?, count?, memorize?)`, `LookAtTopCardsOfDrawDeck(deck?, count?, memorize?)`, `ShuffleCardsFromDiscardIntoDrawDeck(select?, count?)`, `ShuffleDeck(deck?)`, `PutCardsFromHandOnTopOfDeck(select?, count?, player?, hand?, reveal, text?)`, `PutCardsFromHandOnBottomOfDeck(select?, count?, player?, reveal)`

`PutCardsFromHandOnTopOfDeck`: `player` chooses the cards and, with more than one, their order on the deck. Where `count` may be 0 ("may place any number of cards"), give it a `text` that says none need be chosen and do not wrap it in an `Optional` (Q-0143, F-0277). `PutCardsFromDiscardIntoHand` takes such a `text` too (Gwaihir 101_15, F-0278); it and `ChooseCardsFromDiscard` are the two discard-pile words that ask in a definition's own text (F-0285), and the rest always ask "Choose card from discard".

`shuffle` is a required field on every effect that lists it (left out, the card does not load: write `shuffle: false`), and `shuffle: true` shuffles the deck that was searched: the deck of the player carrying the effect out, or, on the two effects that take a `deck`, the deck it names (F-0369). On `PlayCardFromDrawDeck`, `showAll: true` is the player looking through the whole deck, and the deck is then shuffled as the card played leaves it, before anything answers the play (F-0313); `shuffle: true` is still wanted, for the look that plays nothing. A play of a card that was only revealed ("reveal the top X cards ... you may play a revealed minion") writes `showAll: false`. `DiscardCardsFromDrawDeck(select?, count?, player?, deck?, showAll, shuffle, forced, memorize?)` and `RemoveCardsInDeckFromGame(select?, count?, player?, deck?, shuffle, showAll)` can search another player's `deck`: `shuffle: true` there shuffles that player's deck (F-0369, engine pass 25; it used to shuffle the searcher's own). `shuffle: false` and `{ type: ShuffleDeck, deck: <that player> }` after the effect plays the same; the Gronds write it (F-0298, 68_82 and 18_82), and it is the shape where several effects follow one look (4_107).

`PutPlayedEventOnTopOfDrawDeck(filter?)` and `PutPlayedEventOnBottomOfDrawDeck(filter?)`: `filter` is `self` (the default: the card they are written on, for an event that places itself, Still Needed 7_29) or `played` (the card whose play is being answered, for "place that event on top of your draw deck" on another card's `Played` trigger or Response: Shrouded Elf 13_23 and 63_23, F-0287); any other word and the card does not load. Left at the default on another card's trigger they act on that card and do nothing. `PutPlayedEventIntoHand` and `RemovePlayedEventFromTheGame` take `filter: played` the same way.

**Memory:**
`Memorize(filter, memory)`, `MemorizeNumber(amount, memory)`, `MemorizeInfo(info, memory)`, `MemorizeTitle(from, memory)`, `MemorizeDiscard(filter?, memory)`, `MemorizeStacked(filter?, on, memory)`, `MemorizeTopOfDeck(memory, count?)`, `FilterCardsInMemory(filter, memory, memorizeMatching?, memorizeNotMatching?)`

**Choices:**
`ChooseActiveCards(select?, count?, memorize, text, player?, hindered?)`, `ChooseArbitraryCards(fromMemory, count?, filter?, memorize, text, player?)`, `ChooseANumber(text, from?, to?, memorize, player?)`, `ChooseACulture(memorize)`, `ChooseARace(memorize)`, `ChooseAKeyword(memorize, keywords[], text?)`, `ChooseYesOrNo(text, memorize, player?)`, `ChooseHowManyToSpot(filter, memorize, text?)` (asks from 0 to all that match, so a number taken off its answer wants a `max` of 0: F-0374), `ChooseCardsFromDiscard(select?, count?, memorize, text?)`, `ChooseCardsFromDrawDeck(select?, count?, memorize, player?, deck?, showAll, text?)`

**Skirmish:**
`CancelSkirmish(filter?, involving?, fierceOnly?)`, `CancelEvent`, `CancelSpecialAbility`, `CancelAllAssignments`, `AssignFPCharacterToSkirmish(fpCharacter, minion, player?)`, `ReplaceInSkirmish(filter, with)`, `RemoveCharacterFromSkirmish(select?, player?, memorize?)`, `StartSkirmish(fpCharacter, against)`, `SetupExtraAssignmentAndSkirmishes(filter)`

**Prevention:**
`PreventWound(select, memorize?)`, `PreventAllWounds(select?)`, `PreventDiscard(select, memorize?)`, `PreventExert(select, memorize?)`, `PreventHeal(select, memorize?)`, `PreventEffect`, `PreventBurden`, `PreventTwilight`, `NegateWound(select?)`, `PreventAddingAllBurdens`

**Limits:**
`IncrementPerPhaseLimit(limit?, phase?, perPlayer?)`, `IncrementPerTurnLimit(limit?)`

**Other:**
`PutOnRing`, `TakeOffRing`, `CorruptRingBearer`, `ReconcileHand(player?)`, `EndPhase`, `MakeSelfRingBearer`, `PlaySite(number, player?, block?, filter?)`, `TakeControlOfSite(player?)`, `LiberateSite(controller?, memorize?)`, `Hinder(select, count?, player?, memorize?)`, `Restore(select, count?, player?, memorize?)`

`EndPhase` ("end your Shadow phase", Tol Brandir 1_363) does not move the game on: it bars phase events and phase special abilities and plays from hand until the end of the phase, so the player is still asked "Play Shadow action or Pass" with only the pass on offer. A test reads the offers gone, then passes.

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
- **In-Zone Data**: persistent per-card data, cleared when card changes zones. Set via `Store*` subactions. `StoreRace(memory)`, `StoreCulture(memory)` and `StoreKeyword(memory)` keep the printed word beside the name ("Uruk-Hai" for `URUK_HAI`), which is what `DisplayableInformation` and a text's `{stored}` show; `StoreWhileInZone(memory)` keeps the bare string. After a `ChooseARace`, store with `StoreRace` (F-0307).
- Retrieve memory in filters: `Memory(X)`, `NameFromMemory(X)`, `CultureFromMemory(X)`
- Retrieve memory in values: `memory(X)`, `FromMemory(memory)`, `ForEachInMemory(memory)`
- Retrieve memory in requirements: `HasCardInMemory(memory)`, `MemoryIs(memory, value)`
- A cost that selects from `memory(X)` is really checked, and the action not offered when it cannot be paid, where X is filled before costs are checked: in the `cost` of a Trigger, Response or ResponseEvent, what its `trigger` memorizes; in the `cost` of an `AddTrigger`, any memory its own costs do not memorize (the adding action has filled it). A cost that selects from memory an earlier cost of the same action fills cannot be checked beforehand and still needs `ignoreCostCheckFailure: true` on it.

---

## Player Resolution

`You`, `FP` / `Freeps` / `Free Peoples`, `Shadow` / `S`, `Owner`, `FromMemory(X)`, `OwnerFromMemory(X)`

---

## Text Substitution

In `text` fields: `{culture}` → symbol, `{you}` → performing player, `{owner}` → card owner, `{self}` → card link, `{stored}` → the value the card has stored on itself with a `Store*` subaction, in its printed word ("nothing" when it has stored none), `{memoryVar}` → stored card(s) or string (read from the memory of the action that shows the text: a value another trigger memorized is not in it and prints `NONE`; what a card named as it was played is read with `{stored}`, F-0307)
