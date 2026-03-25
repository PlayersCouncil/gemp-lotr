# Selections
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/appender/resolver/`CardResolver.java
Functions for resolving which card to select when many are valid.  The parameters usually contain a filter (see next section).
Selectors differ from `Filters` in that most actions are performed against single targets which need narrowed down.  For instance, "make an Elf strength +2" has a filter of "elf", but this is insufficient to determine `which`* Elf is to be affected, and so a Selector is employed to further narrow it down to a single option (in this case probably "choose(elf)").  Most targeted Effects will utilize Selectors, and most Triggers will utilize Filters, while Modifiers can go either way depending on context.

* all(filter`*)
  Affects every card that matches the provided `filter`.
* bearer
  Matches the card this card is attached to.  Be warned that this can be used even in contexts where attachment doesn't make sense, resulting in silent errors (such as a companion erroneously checking if `bearer` bears a condition, rather than `self`).
* choose(filter`*)
  The controlling player is presented with a choice prompt, selecting a certain number of cards which match `filter`.  The exact number will be defined by the action using this selector.
* memory(location`*)
  Uses the cards stored at the provided memory `location`.  If the number of cards stored is outside the size range set by the action, the operation will fail.
* random(count`*)
  Randomly selects `count` cards.  Only currently supported for randomly selecting from hand or the adventure deck.
* self
 Shorthand for the card whose game text defines the current action.  Be warned that this can be used in contexts which don't make sense, such as a possession accidentally boosting `self`'s strength by +2, rather than `bearer`.

# Filters
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/filter/`FilterFactory.java
Filters are ways of describing which classes of cards should be affected by a given modifier, trigger, or effect.  Filters can only be used in their raw form when the number of specified cards does not matter–for instance, a trigger of "each time you play a pipe" does not care about narrowing things down to the specific pipe, only that the trigger should activate if `any` pipe enters play.
Filters are case insensitive comma-separated lists of terms.  For example: "`unbound, or(elf, man), companion, not(exhausted)`" specifies "an unbound Elf or Man companion who is not exhausted".  Any number of filter clauses may be added to the filter definition, and in the case of operation filters (or/and/not), the filters may be nested in any combination.
Not to be confused with `Selectors`, which almost always contain a filter but define how to refine a filter down to a single choice.

## Simple Filters
Simple filters do not contain any parameters and are for the most part one-word (or one-phrase) constructions.
 

* All Card Types
* All Keywords
* All Item Classes
* All Races
* Another | Other
  Alias for not(self)
* Any
  Matches all cards.  Frequently used as a default value.
* AttachedToInSameRegion
  Matches sites this card is attached to that are in the fellowship's current region
* Bearer
  Matches the card this card is attached to
* CanBeDiscarded
  Matches cards which can be discarded by the currently acting player (i.e. cards which are not protected by "cannot be discarded by X" defenses).
* CanBeReturnedToHand
  Matches cards which can be returned to hand by the current card source (i.e. cards which are not protected by "cannot be returned to hand by X" defenses).
* CanExert
  Matches cards which can be exerted by the current card source.
* CanWound
  Matches cards which are not blocked from being wounded by the current card souce.
* Character
  Matches all Companions, Minions, and Allies (but not Followers)
* ControlledByOtherPlayer
  Matches sites currently controlled by a player other than the performing player
* ControlledByShadowPlayer
  Matches sites currently controlled by everyone except the current Free Peoples player.
* ControlledSite | SiteYouControl
  Matches sites controlled by the performing player.  Only use this if you are trying to verify the existence of a literal controlled site; if you are just counting use the `ControlsSite` requirement or `ForEachSiteYouControl` value.
* CultureInDeadPile
  Matches cards sharing a culture with a card in the dead pile.
* CultureWithTokens
  Matches cards sharing a culture with a culture token on a card.
* CurrentSite
  Matches the fellowship's current site only
* CurrentSiteNumber
  Matches cards with the same site number as the fellowship's current site (works for both Sites and Minions).  If matching against an inactive site in a player's adventure deck, this will only match Movie Block cards with the same number (Shadow sites do not have a number until played).
* EvenTwilight
  Matches all cards with an even-numbered printed twilight cost.
* Exhausted
  Matches cards with 1 vitality
* GettingDiscarded
  Matches cards that are about to be discarded.  This is only coherent as part of an `AboutToDiscard` trigger.
* HasRace
  Matches cards with a Race property (i.e. not Gollum or Tom Bombadil).
* Hinderable
  Matches cards that are unhindered and are valid targets for a hinder action (meaning that the card is active, in a valid zone, and is not a site, among other restrictions).
* Hindered
  Matches cards that are currently hindered.
* IdInStored
  Matches specific cards previously stored to the `WhileInZoneData` (using the `StoreWhileInZone` effect)
* InFierceSkirmish
  Matches cards currently assigned to the currently active fierce skirmish
* InPlay
  Matches cards in the following zones:
    * FREE_CHARACTERS
    * SHADOW_CHARACTERS
    * SUPPORT
    * ATTACHED
    * ADVENTURE_PATH
    
    Note that this `DOES NOT` evaluate whether a card is active, so this will produce the Free Peoples player's Shadow cards, etc.
* InSameRegion
  Matches cards with a site number that is within the fellowship's current region (works for both Sites and Minions)
* InSkirmish
  Matches cards that are in the currently active skirmish
* Item
  Matches all Artifacts and Possessions
* Mounted
  Matches all cards with a Mount attached or otherwise have the Mounted status manually added
* NextSite
  Matches the site with a site number 1 higher than the fellowship's current site.
* NextSiteNumber
    Matches cards with the same site number as the fellowship's next site (works for both Sites and Minions).  If matching against an inactive site in a player's adventure deck, this will only match Movie Block cards with the same number (Shadow sites do not have a number until played).
* NotAssignedToSkirmish
  Matches all cards which are not assigned to a skirmish and not in the currently active skirmish
* OddTwilight
  Matches all cards with an odd-numbered printed twilight cost.
* OnePerBearer | LimitOnePerBearer|Limit1PerBearer
  Matches cards that do not already bear a card with the same title as the currently executing card, which is used for cards which say "Limit 1 per bearer" 
* Playable
  Matches cards which could be legally played.  This is intended to be used with cards that are not currently in play.
* PlayableFromDeadPile
  Matches cards which could be legally played even if already in the dead pile.  This is intended to be used with cards already in the dead pile, but be warned that if used on a unique card outside the dead pile it will ignore its dead brother.
* RingBearer | `Ring-Bearer
  Matches the Ring-bearer for the currently active Free Peoples player
* RingBound | `Ring-bound
  Matches all companions with the Ring-bound keyword
* Self
  Matches the card source of the current action
* SiteHasSiteNumber
  Matches all sites that have a printed site number, i.e. Movie block site and played Shadows sites but not Shadows block sites that have not yet been played.  Once played, even Shadows sites have a site number assigned.
* SiteInCurrentRegion
  Matches all sites currently in the fellowship's current region
* SkirmishLoser
  Matches the losing character in a skirmish.  This is only coherent for triggers that watch for skirmish results.
* StoredCulture
    Matches cards with cultures shared by the culture stored in the current card's In-Zone Data (stored there using the `StoreCulture` effect).
* StoredKeyword
    Matches cards sharing a keyword stored in the current card's In-Zone Data (stored there using the `StoreKeyword` effect).
* StoredTitle
  Matches cards sharing a title stored in the current card's In-Zone Data (stored there using the `StoreWhileInZone` and `MemorizeTitle` effects).
* Unbound
  Matches all companions that do NOT have the Ring-bound keyword.
* Uncontrolled
  Matches all sites which are not currently controlled.  Given that the current site can never be controlled, the usage of this filter seems dubious.
* Unique
  Matches all cards with a blueprint marked as unique
* Unwounded
  Matches all cards with 0 wound tokens on them.  By itself, this includes non-character cards such as Conditions.
* Weapon
  Matches all cards with the Hand Weapon or Ranged Weapon item classes.
* Wounded
  Matches all cards with 1 or more wound tokens.
* Your
  Matches all cards owned by the player performing the current action

## Operation Filters
Commonly-used helper functions that can combine several filters into one.
 

* And
    * [0]: filters (comma-delimited)
    Applies a boolean AND to all provided `filters`; in other words, matches all cards which match all of the sub-filters.  For example, if the filter is `and(culture(Gondor),possession)`, only cards which both match the Gondor culture and have a card type of Possession will match this filter.
* Not
    * [0]: filter
    Applies a boolean NOT to the provided `filter`, i.e. inverting it.  For example, not(Dwarf) will cause the filter to match everything <span style="text-decoration:underline;">except</span> cards with the race of Dwarf.
* Or
    * [0]: filters (comma-delimited)
    Applies a boolean OR to all provided `filters`; in other words, matches any cards which match at least 1 of the sub-filters.  For example, `or(culture(Gondor),possession)` will cause a Gondor Condition and a Sauron Possession to both match the filter.  

## Function Filters
These filters require parameters to be passed in, in the form of "name(param1,param2)" etc.  Parameters are not labeled or numbered, just passed as-is.  Parameter names below are for clarity.
 Due to this setup, there are no default values provided for any parameters; every parameter must be provided every time (unless otherwise noted).

* AllyHome
    * [0]: block
    * [1]: number
    Matches all allies currently at their home site of `number` so long as it is in `block`.
* AllyInRegion
    * [0]: block
    * [1]: number
    Matches all allies who share a native `block` with the current site and have a home site number within the fellowship's current region `number`.
* AssignableToSkirmishAgainst
    * [0]: filter
    Matches all cards which are eligible to skirmish against the cards matching `filter`.
* AssignedToSkirmish
    * [0]: filter
    Matches all cards which are currently assigned to (or actively skirmishing against) the cards matching `filter`.
* AttachedTo
    * [0]: filter
    Matches all cards currently attached to the cards matched by `filter`.
* CardTypeFromMemory
    * [0]: memory
    Matches all cards which share a card type with any card in the given `memory` location.
* Culture
    * [0]: culture
    Matches all cards with the given `culture`.
* CultureFromMemory
    * [0]: memory
    Matches all cards which share a culture with any card in the given `memory` location.
* HasAttached
    * [0]: filter
    Matches all cards with at least 1 attached card that matches `filter`.
* HasAttachedCount
    * [0]: count
    * [1]: filter
    Matches all cards with at least `count` attached cards that match `filter`.
* HasAnyCultureTokens
    * [0]: count 
        default: 1

    Matches all cards with at least `count` culture tokens (default: 1) of any culture.
* HasCultureToken
    * [0]: culture
    Matches all cards with at least 1 culture token of the given `culture`.
* HasCultureTokenCount
    * [0]: count
    * [1]: culture
    Matches all cards with at least `count` culture tokens of the given `culture`.
* HasStacked
    * [0]: filter
    Matches all cards with at least 1 stacked card that matches `filter`.
* HasStackedCount
    * [0]: count
    * [1]: filter
    Matches all cards with at least `count` stacked cards that match `filter`.
* HighestRaceCount
    * [0]: filter
    Tallies up the race that has the most cards matching `filter`, and then matches all cards with that race.
* HighestStrength
    * [0]: filter
    Matches all the cards tied for highest strength which otherwise match filter
* LowestStrength
    * [0]: filter
    Matches all the cards tied for lowest strength which otherwise match filter
* HighestTwilight
    * [0]: filter
    Matches all the cards tied for highest twilight which otherwise match `filter. `This takes into account current twilight modifiers.
* LowestTwilight
    * [0]: filter
    Matches all the cards tied for lowest twilight which otherwise match `filter. `This takes into account current twilight modifiers.
* InSkirmishAgainst
    * [0]: filter
    Matches all cards in the currently active skirmish against cards that match `filter`.
* InSkirmishAgainstAtLeast
    * [0]: count
    * [1]: filter
        Matches all cards in the currently active skirmish against at least `count` cards that match `filter`.
* MaxResistance
    * [0]: value
    Matches all cards with a resistance stat no greater than `value`.
* MinResistance
    * [0]: value
    Matches all cards with a resistance stat no less than `value`.
* MaxStrength
    * [0]: value
    Matches all cards with a current strength no greater than `value`. 
* MinStrength
    * [0]: value
    Matches all cards with a current strength no less than `value`. 
* MatchesTwilight
    * [0]: value OR memory
    Matches all cards with a printed twilight cost which is exactly `value` or the value stored in `memory`.  When referencing the memory value, call it like `MatchesTwilight(memory(X))`.  If the memory value is not set, it defaults to 100.
* MaxTwilight
    * [0]: value OR memory
    Matches all cards with a printed twilight cost which is at most `value` or the value stored in `memory`.  When referencing the memory value, call it like `MaxTwilight(memory(X))`.  If the memory value is not set, it defaults to 100.
* MinTwilight
    * [0]: value OR memory
    Matches all cards with a printed twilight cost which is at least `value` or the value stored in `memory`.  When referencing the memory value, call it like `MinTwilight(memory(X))`.  If the memory value is not set, it defaults to 0.
* MaxVitality
    * [0]: value
    Matches all cards with a current vitality no greater than `value`. 
* MinVitality
    * [0]: value
    Matches all cards with a current vitality no less than `value`. 
* Memory
    * [0]: memory
    Matches all cards previously stored in the specified `memory` location.
* Name | Title
    * [0]: title
    Matches all cards with the given `title`.  Case-insensitive, spaces are ignored, and accents are stripped.
* NameFromMemory | TitleFromMemory
    * [0]: memory
    Matches all cards which share a title with one of the cards stored in the given `memory` location.
* NameInStackedOn
    * [0]: filter
    Matches all cards which share a title with one of the cards stacked on cards matching `filter`.
* OwnerControls
    * [0]: filter
    Matches all cards with an owner matching the player who currently controls a card matching `filter`. This is most useful for ensuring sites with "while you control this site" text do not affect the other side. See Nurn).
* PrintedTwilightCostFromMemory
    * [0]: memory
    Matches all cards which share a printed twilight cost with the card stored in `memory`.
* Race
    This is not for actual races (just use the race name by itself in that case).  This is invoked one of three ways, verbatim:
    * race(memory(X))
      Matches all cards with a race that matches the race of the card stored at the given `memory` location.
    * race(stored)
        Matches all cards with the race stored in the `In-Zone Data`. See Úlairë Nertëa, Dark Horseman)`.
    * race(cannotSpot)
        Matches cards who have a race that cannot be spotted on the table. See The Nine Walkers).
* RegionNumber
    * [0]: value OR range
    Matches all cards with a site number in the `value` region or in regions fitting within `range`.  Works on both Sites and Minions.
* ResistanceLessThanFilter
    * [0]: filter
    Matches all cards with a resistance stat less than the sum total resistance of all cards matching `filter`.
* Side
    * [0]: side
    Matches all cards of the given `side`.  Note that this does not filter for active or in play cards. See `Side` for a list of acceptable values.
* Signet
    * [0]: signet
    Matches all cards with the given `signet`.  See `Signet` for a list of acceptable values.
* SiteBlock
    * [0]: block
    Matches all site cards with the given `block`.  See `SitesBlock` for a list of acceptable values.  "Multipath" should be avoided.
* SiteNumber
    * [0]: value OR range
    Matches all cards with a site number equal to `value` or within `range`.  Works on both Sites and Minions.
* StrengthLessThanFilter
    * [0]: filter
    Matches all cards with a strength stat less than the sum total strength of all cards matching `filter`.
* Subtitle
    * [0]: subtitle
    Matches all cards with the given `subtitle`.  Case-insensitive, spaces are ignored, and accents are stripped.
* SubtitleFromMemory
    * [0]: memory
    Matches all cards which share a subtitle with one of the cards stored in the given `memory` location.
* Timeword
    * [0]: timeword
    Matches all cards (mostly if not entirely events) with the given `timeword` (i.e. a phase or Response).  If "current" is provided, it will match cards with a timeword matching the current phase.
* Zone
    * [0]: zone
    Matches all cards within the given `zone`.  See `Zone` for a list of acceptable values.

# Spot Overrides
By default, most effects and filters only consider "active" cards - those that are in play and currently participating in the game. However, cards can be inactive for several reasons, and some effects need to pierce this veil of inactivity to target or affect cards that would otherwise be ignored.

### When Cards Are Inactive
A card is considered inactive if any of the following apply:

<table>
  <tr>
   <td><strong>Reason</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td><code>out_of_turn</code>
   </td>
   <td>The card belongs to the side not currently acting (e.g., Free Peoples cards during Shadow phase)
   </td>
  </tr>
  <tr>
   <td><code>attached_to_inactive</code>
   </td>
   <td>The card is attached to another card that is itself inactive
   </td>
  </tr>
  <tr>
   <td><code>stacked</code>
   </td>
   <td>The card is stacked on another card (not attached - stacked cards are set aside and don't normally participate in the game)
   </td>
  </tr>
  <tr>
   <td><code>hindered</code>
   </td>
   <td>The card has been hindered and is temporarily disabled
   </td>
  </tr>
</table>

### Syntax
The `activeOverride` field accepts either a single value or an array of values:
// Single override (most common)
activeOverride: hindered
// Multiple overrides
activeOverride: [hindered, stacked]
// Include all inactive cards
activeOverride: all
// Explicit default (no overrides)
activeOverride: none
Values are case-insensitive and accept underscores or hyphens interchangeably (`out_of_turn`, `out-of-turn`, and `OUT_OF_TURN` are all valid).

### Available Values

<table>
  <tr>
   <td><strong>Value</strong>
   </td>
   <td><strong>Effect</strong>
   </td>
  </tr>
  <tr>
   <td><code>hindered</code>
   </td>
   <td>Include hindered cards
   </td>
  </tr>
  <tr>
   <td><code>stacked</code>
   </td>
   <td>Include stacked cards
   </td>
  </tr>
  <tr>
   <td><code>out_of_turn</code>
   </td>
   <td>Include cards belonging to the non-acting side
   </td>
  </tr>
  <tr>
   <td><code>attached_to_inactive</code>
   </td>
   <td>Include cards attached to inactive cards
   </td>
  </tr>
  <tr>
   <td><code>all</code>
   </td>
   <td>Include all inactive cards (equivalent to specifying all four reasons)
   </td>
  </tr>
  <tr>
   <td><code>none</code>
   </td>
   <td>Explicit default; no inactive cards are included
   </td>
  </tr>
</table>

### Examples
`An effect that can target both hindered and stacked cards:
hjson
{
    type: Discard
    select: choose(side(shadow), or(condition, possession))
    activeOverride: [hindered, stacked]
}

### Notes
