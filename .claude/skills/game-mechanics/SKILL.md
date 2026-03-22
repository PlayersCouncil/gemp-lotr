---
name: game-mechanics
description: >
  LotR TCG game mechanics and rules reference, cross-referenced against Comprehensive Rules 4.2.
  Use this skill when reasoning about game rules, turn structure, combat resolution, twilight pool,
  card types, keywords, or any game mechanic question. Trigger when: discussing skirmish resolution,
  overwhelm, archery, assignment, sanctuary healing, threats, burdens, initiative, spotting, stacking,
  uniqueness, or any rules-level question about how the game works.
user-invocable: false
---

# LotR TCG Game Mechanics Reference

Cross-referenced against Comprehensive Rules 4.2.

For the full unabridged rules text, read `${CLAUDE_SKILL_DIR}/references/comprehensive-rules-4.2.wiki.txt`.

---

## Turn Structure

### Start of Turn
- Remove all tokens from the twilight pool
- Perform any "start of turn" actions (each only once per turn)
- Sanctuary healing happens here (see Sanctuaries)

### Phase Order
```
Fellowship → Shadow → Maneuver → Archery → Assignment → Skirmish(s) → Regroup
```

### Phase Details

**Fellowship Phase** (FP player only):
- Play companions, possessions, conditions, artifacts; use Fellowship actions
- Playing FP cards adds their twilight cost to the pool
- Rule of 4: cannot draw/take into hand more than 4 cards during fellowship phase (does not apply to start-of-turn draws)
- At end, FP player must move (unless at sanctuary — see Sanctuaries)
- Movement: Shadow player places next site, add site's shadow number + (1) per companion to twilight pool

**Shadow Phase** (Shadow player only):
- Play minions, possessions, conditions, artifacts; use Shadow actions
- Playing Shadow cards removes their twilight cost from the pool
- Cannot play a Shadow card if pool can't cover the cost
- Can exert another player's character to pay costs for your Shadow cards

**Maneuver Phase** (alternating):
- Both players may use Maneuver actions via action procedure (alternate until both pass consecutively)
- If no minions in play after Shadow phase, skip directly to Regroup

**Archery Phase** (alternating):
- Fellowship archery total = number of FP archers + modifiers
- Shadow archery total = number of Shadow archers + modifiers
- Archery actions may be used (action procedure)
- Then archery fire: each side assigns archery wounds to opposing characters
- If no minions, phase is skipped

**Assignment Phase** (special flow):
- FP player assigns companions to minions first
- Then Shadow player assigns remaining unassigned minions to companions
- Assignment actions can be used between these steps
- Keywords affect assignment: defender +X (extra assignments), lurker (can't be normally assigned), ambush (forces assignment with twilight cost), stealth (can avoid), unhasty (FP can't assign unless at home site or Gandalf card allows)

**Skirmish Phase** (per skirmish):
- FP player chooses resolution order
- During each skirmish, both players may use Skirmish actions (action procedure)
- Resolution: compare total strength of each side
  - Higher strength wins; each loser takes 1 wound (+ damage bonus wounds)
  - **Tie: Shadow side wins**
  - **Overwhelm**: strength ≥ 2× opponent's → loser is killed outright. Str > 0 overwhelms str 0. If both sides are 0, Shadow wins but does not overwhelm.
- If all characters on one side removed mid-skirmish before resolution, other side wins
- If all characters on one side removed before skirmish begins, that skirmish doesn't occur
- A character removed mid-skirmish is neither winning nor losing (unless it was the last on its side)
- After all normal skirmishes, fierce skirmishes occur (fierce minions get reassigned)
- Skirmish phase ends after all win/loss triggered actions resolve; characters are then unassigned

**Regroup Phase** (alternating):
- Both players may use Regroup actions (action procedure)
- Reconcile: FP player may discard 1 card from hand, then draws to 8 or discards down to 8
- Shadow player discards all minions from play
- Movement decision: FP player may move again (up to move limit, default 2) — if so, return to Shadow phase. If not, Shadow reconciles and minions are discarded.

### Site 1
- Game starts at site 1 (first player chooses from their adventure deck)
- No twilight added for initial placement
- Not considered a "move" for triggers

### Site 9
- No Fellowship phase — skips straight to Shadow phase
- FP wins if Ring-bearer survives all skirmish phases (no Regroup on final turn)

### No Minions
- If no minions in play after all Shadow phases, Maneuver through Skirmish are all skipped → straight to Regroup

---

## Twilight Pool

- **FP cards add** their twilight cost to the pool when played
- **Shadow cards remove** their cost from the pool when played (must have enough)
- On movement: site shadow number + (1) per companion added to pool
- Cleared at start of each turn
- Twilight cost can be modified but never goes below 0
- Cards already in play that modify twilight cost only apply when the target card is coming into play

---

## Playing a Card (Procedure)

1. Card enters the **void** (not in hand, not in play, not in discard)
2. Check **requirements** (spotting, site number, etc.)
3. Pay **costs** (twilight, exertions, discards — costs happen before effects)
4. **Place** the card (events stay in void; others enter play)
5. **Respond** window (opponents may use response actions)
6. Resolve **effects** ("when you play" triggers, etc.)
7. Events go to discard pile after resolution

Transferring a possession/artifact is NOT playing it — pay twilight cost again during Fellowship phase, but "when you play" does not trigger.

---

## Characters

### Vitality & Wounds
- Wounds reduce remaining vitality; when wounds ≥ vitality, character is killed
- Wounds are always placed one at a time
- "Wound X companions" = must choose different companions
- **Exertion**: adds 1 wound; requires wounds < vitality - 1 (can't exert at 1 remaining vitality)
- **Wounding**: adds 1 wound with no restriction (kills if wounds reach vitality)
- **Exhausted**: wounds = vitality - 1 (can be wounded but can't exert)
- Healing removes 1 wound

### Strength
- Used in skirmish resolution
- Base strength + all modifiers = current strength

### Resistance
- All companions have resistance (relevant for Ring-bearer corruption)
- Ring-bearer: burdens reduce resistance; when resistance reaches 0, corrupted → FP loses

### The Ring-bearer
- Companion who bears The One Ring; gains Ring-bearer and Ring-bound keywords
- Can be any character with ringed resistance icon, or any Frodo
- If non-Frodo is Ring-bearer, cannot play any Frodo with Ring-bearer keyword
- **Cannot be discarded or returned to hand**
- **Skirmishes involving Ring-bearer cannot be cancelled**
- If Ring-bearer dies or is corrupted, game ends immediately — Shadow wins
- Can "wear" the ring (put it on) with effects depending on ring variant

### Allies
- FP characters but NOT fellowship members — played to support area
- Have a **home site** number; at their home site they participate in archery and skirmishes
- Away from home site, they normally cannot participate in combat
- Not counted as companions for Rule of 9 or companion-specific effects

---

## Card Types

### Companions
- FP characters in the fellowship
- Have strength, vitality, resistance, signet, culture, race
- Played during Fellowship phase
- Rule of 9: max 9 companions in play + dead pile combined

### Minions
- Shadow characters
- Have strength, vitality, site number, culture, race
- Played during Shadow phase; must have twilight to cover cost
- **Roaming**: if played at site lower than minion's site number, +2 twilight penalty
- Discarded during Regroup

### Possessions
- Attached to characters (companions or minions)
- Item classes: hand weapon, ranged weapon, mount, armor, helm, shield, cloak, bracers, pipe
- One item per class per bearer
- Bearer restrictions on the card (culture, race, specific character)

### Conditions
- Support area or attached to characters
- Provide ongoing effects

### Events
- One-time effects; phase keyword defines when playable
- Enter void when played, go to discard after resolution
- Not in play — cannot be spotted

### Artifacts
- Like possessions but rarer; same bearer/class rules apply

### Sites
- Adventure path (sites 1–9); have shadow number and keywords
- Shadow player places next site when FP moves to unplayed position
- Sites can be replaced by card effects; cards on old site transfer to new site
- Site numbers also determine regions: 1–3 = region 1, 4–6 = region 2, 7–9 = region 3

### MetaSite (Gemp-specific)
- RTMD modifier cards in support area permanently
- Not a real LotR TCG card type

---

## Uniqueness

- Dot before card name indicates uniqueness:
  - • (1 dot) = unique — 1 copy in play per player
  - •• (2 dot) = max 2 copies
  - ••• (3 dot) = max 3 copies
  - No dot = non-unique — unlimited
- Two cards are "the same" if same card title (regardless of subtitle) or same collector's info
- Cannot play a copy to replace one already in play
- **Discard to heal**: discard a unique card from hand to heal a copy in play
- Stacked cards don't count for uniqueness

---

## Keywords

### Combat Keywords
- **Archer** — contributes to archery total
- **Damage +X** — winner deals X additional wounds in skirmish
- **Defender +X** — can take X additional assignments
- **Fierce** — minion gets a second round of assignments/skirmishes after normal skirmishes
- **Enduring** — not killed by overwhelm; instead takes wounds equal to vitality minus 1
- **Hunter X** — gains strength bonus based on wounds/threats (varies by version)
- **Ambush (X)** — Shadow pays X twilight to assign during assignment; some trigger wound effects
- **Lurker** — cannot be assigned to skirmish normally
- **Toil X** — when playing, may exert same-culture characters to reduce twilight cost by X each (each character only once)
- **Unhasty** — FP player can't assign unless at home site or Gandalf card permits; Shadow can still assign to it

### Site Keywords
- **Sanctuary** — site 3 or site 6 (see Sanctuaries section)
- **Underground**, **Forest**, **Plains**, **River**, **Marsh**, **Mountain**, **Battleground**, **Dwelling** — terrain keywords for card interactions

### Character Keywords
- **Ring-bound** — only companions; all Frodo and Sam versions are ring-bound
- **Unbound** — any companion without Ring-bound
- **Ranger**, **Valiant**, **Knight**, **Tracker**, **Villager** — race/class subtypes for card interactions
- **Stealth** — can avoid assignment

### Other
- **Tale**, **Weather**, **Spell** — condition subtypes
- **Pipeweed**, **Pipe** — possession subtypes with specific interactions

---

## Active vs Inactive

- During your turn: your FP cards and opponents' Shadow cards are **active**
- All other cards are **inactive**
- Inactive cards cannot be used, spotted, or have their effects apply
- Dead pile cards are active during your turn (but not in play, so can't be spotted)

---

## Spotting

- "Spot X" = X must be in play and active
- "For each X you spot" = you choose how many to count (optional)
- "While you can spot X" / "you can spot" = mandatory, automatic (no choice)
- Events are never in play, can never be spotted
- Stacked cards are not in play, cannot be spotted
- Cards in dead pile are active but not in play — can't be spotted

---

## Stacking

- Stacked cards are placed **face up**, viewable by any player
- Not in play, not active, can't be spotted
- Don't count for uniqueness
- Multiple copies of same unique card can be stacked together

---

## Sanctuaries

- Site 3 and site 6 on the adventure path
- At start of turn at a sanctuary: FP may heal up to 5 wounds from companions (not allies)
  - Can distribute freely (e.g., 3 on one companion, 2 on another)
  - Choosing 0 is valid

---

## Threats

- Tokens placed on the dead pile by FP player
- **Cap**: cannot add threats if threats ≥ number of companions in play
- **Death trigger**: when a companion or ally is killed and placed in dead pile, remove ALL threats, then FP must assign that many wounds to companions (not allies), distributed however they choose

---

## Initiative

- **FP has initiative** if they have 4+ cards in hand; otherwise Shadow has initiative
- Various card effects check or interact with who has initiative

---

## Burdens

- Counter on the Ring-bearer
- Reduce resistance; when burdens ≥ resistance, Ring-bearer is corrupted → FP loses
- Added by: bidding at game start, wearing the ring, card effects
- Can be removed by card effects

---

## Starting Fellowship

- Ring-bearer placed with The One Ring + bid burdens
- May then play companions from **draw deck** (not hand), one at a time, total twilight cost ≤ 4 (Ring-bearer's cost excluded)
- No twilight tokens added to pool for starting fellowship
- "When you play" game text does trigger on starting companions
- Draw 8 cards after starting fellowship is placed

---

## Regions (Movie Block+ Only)

- Sites 1–3 = Region 1, Sites 4–6 = Region 2, Sites 7–9 = Region 3
- In Open/War of the Ring formats: +3 twilight for region 2 movement, +6 for region 3
- NOT used in Fellowship, Towers, or King block formats

---

## Timing: "When" vs "While"

- **"When"** = one-shot trigger, fires once when condition is met ("When you play this...")
- **"While"** = continuous effect, active as long as condition holds ("While bearer is a Dwarf...")

---

## Response Timing

- "About to" actions fire when something is about to happen
- Typically only one response can fire per situation — if one replaces the event ("instead"), others can no longer respond to the original situation
- Prevention: stops the event entirely. Replacement ("instead"): substitutes a different outcome.
