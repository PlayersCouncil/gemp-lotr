# Value Resolution
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/appender/resolver/`ValueResolver.java
Giant if-else flow for functions for defining numeric fields.  These fields may be called `amount`, `value`, `times`, `count`, or other labels as befits the situation.  In general, `count` is for physical things (cards, tokens), `amount` is for abstract bonuses, `times` is for repeat actions, and `value` is a catch-all term.  Functionally speaking these are all identical and what term is used is purely an attempt to communicate the number's usage.
Many numbers are `Literal Values`, which is to say they can be determined before the game starts and are represented as integers (1, 2, 3, etc).  However many more are `Evaluated Values`, which is to say their value can only be calculated once the game has started, due to relying on specifics of the game state.  For example, "this Dwarf is strength +1 for each possession you can spot" is an evaluated value that depends upon being able to count how many possessions are currently in play.
Complex evaluated values may be made up of multiple sub-values representing a variable maximum, minimum, range, multiplier, or other caveat.  As a result, these values are represented as complex json objects just like any other definition, with a type and fields as appropriate.  However in the case where simple numbers are used, the number literal can be used instead of the json body.

## Literal Values
These values are passed as literal values with no additional parameters.
 

* Literal integer values 
  A literal 1, 10, 100, etc.
* "`X-Y`" `integer range
 Not every value can be a range.  This is used in situations where the player makes a variable number of choices or actions.  See also the `Range` evaluated value below, for more complex scenarios where X or Y themselves need to be evaluated values.
    * X must be >= 0
    * Y must be >= 1
    * X must be &lt; Y
* Any
  Alias for a range from 0 to ~2 billion (Integer.MAX_VALUE)
* AnyNonZero
  Alias for a range from 1 to ~2 billion (Integer.MAX_VALUE)`


## Simple Evaluated Values
These values are literal strings used as aliases for more complex operations.  They have no parameters and do not need to be represented using the full json syntax.

* BurdenCount
  The total number of burdens on the active Ring-bearer
* memory(X)
  Case-sensitive.  The value X will be looked up and evaluated as a literal integer.  For more complex memory operations, see `FromMemory` below.
* MoveCount
  How many times the current Fellowship have moved this turn (0 at the start of the turn, 1 during the first Shadow phase, 2 if freeps moves during Regroup, etc)
* TwilightCount
  The current number of twilight tokens in the twilight pool.

## Math Operation Values

* Abs
  Returns the absolute `value`.
    * value
* Max
  Returns whichever of the two numbers is larger.
    * firstNumber
    * secondNumber
* Min
  Returns whichever of the two numbers is smaller
    * firstNumber
    * secondNumber
* Range
  Same as the simple integer range, except that both values can themselves be evaluated values.  
    * from
    * to
* Subtract
  Subtracts `secondNumber` from `firstNumber`, both of which can themselves be evaluated values.
    * firstNumber
    * secondNumber
* Sum
  Takes all the values from the `source` array, evaluates them, and adds them together.
    * source
    * over
    * limit
    * multiplier
    * divider

## Stat Evaluated Values
Evaluated values relating to unit stats.

* ForEachStrength
  Totals the strength on cards matching the given `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  If `filter` is <span style="text-decoration:underline;">not</span> provided (or if it's "any"), then this is a context-sensitive number that affects each impacted card differently ("Each Elf is resistance +X, where X is that Elf's strength.").
 If `filter` <span style="text-decoration:underline;">is</span> provided, then all valid targets are summed together to a single universal value ("This Elf is resistance +X, where X is the total strength of each Elf you can spot").
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachResistance
  Totals the resistance on cards matching the given `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  If `filter` is <span style="text-decoration:underline;">not</span> provided (or if it's "any"), then this is a context-sensitive number that affects each impacted card differently ("Each Elf is strength +X, where X is that Elf's resistance.").
 If `filter` <span style="text-decoration:underline;">is</span> provided, then all valid targets are summed together to a single universal value ("This Elf is strength +X, where X is the total resistance of each Elf you can spot").
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachTwilightCost
  Totals the current twilight cost of all cards matching `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.<sub> </sub>
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachVitality
  Totals the vitality on cards matching `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  If `filter` is <span style="text-decoration:underline;">not</span> provided (or if it's "any"), then this is a context-sensitive number that affects each impacted card differently ("Each Hobbit is strength +X, where X is that Hobbit's vitality.").
 If `filter` <span style="text-decoration:underline;">is</span> provided, then all valid targets are summed together to a single universal value ("This Hobbit is strength +X, where X is the total vitality of each Hobbit you can spot.").
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* PrintedStrengthFromMemory
  Retrieves the strength value of the blueprint of the card stored in `memory`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`. This is used e.g. for cards revealed from hand (which do not have a non-blueprint strength), but could theoretically be used to get the base unmodified strength of an active card.  See Grimbeorn, Beorning Chieftain).
    * memory
        required
    * over
    * limit
    * multiplier
    * divider
* SiteNumberInMemory
  Gets a card saved to `memory` and evaluates its site number, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  This works properly on both sites and minions.
    * memory
      required
    * over
    * limit
    * multiplier
    * divider
* TwilightCostInMemory
  Totals the twilight cost of all cards stored in a given `memory` location, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * memory
    required
    * over
    * limit
    * multiplier
    * divider

## Complex Evaluated Values
For nearly all complex values, the following subfields will be available.  These are explained here so that their definition need not be repeated on every value (tho they will still be listed, as not every value contains these parameters):

    * over
      default: 0
 Ignores part of the final value, effectively subtracting this value from the result (rounding up to zero).  For example, "over: 4" means that 5 will be reduced to 1, and 3 will be treated as 0.  Used in text such as "For each companion over 4, do X".  
    * limit
      default: MAX_INTEGER (~2 billion)
 Caps the result to at most this value.  
    * multiplier
      default: 1
 Multiplies the final result.  May be an explicit multiplication as dictated by the game text, but more often is used with -1 to coerce a value into the correct sign.
    * divider
        default: 1
          Divides the final result.  Be warned: division uses integer division rules.
* ArcheryTotal
  Returns the current archery total of the given `side`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  Outside of the Archery phase, this will usually be 0.
    * side
      required
    * over
    * limit
    * multiplier
    * divider
* CardAffectedLimitPerPhase
  Modifies the provided `source`, applying the given `limit` and optionally applying the given `multiplier`.  Used for cards that add a static numeric bonus multiple times up to a maximum limit, i.e. "strength +1 (limit +3)".  See Realm of Dwarrowdelf). For restricting evaluated values in a more freeform manner, see `CardPhaseLimit`, below.
    * limit
    Limit past which to restrict the bonus ("3" in the example above)
    * source
      default: 0
      Amount to increment the bonus ("1" in the example above).  
    * prefix
      optional
      Only necessary if there are multiple simultaneous bonuses being tracked on the same action, such as how Realm of Dwarrowdelf tracks strength and damage bonuses separately.  
* CardPhaseLimit
  Restricts an evaluated `amount` to a given cumulative `limit` over multiple invocations during the same phase.  See Sting).
    * limit
      default: 0
    * amount
      default: 0
* Conditional
  Formerly known as "requires" and "condition".  If the `requires` requirements are fulfilled, then the `true` value is used, else `false` is used.  Both `true` and `false` can themselves be evaluated values.
    * requires
    * true
    * false
* CurrentSiteNumber
  The fellowship's current site number, 1-9 (regardless of block), optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* ForEachBurden
  Returns the current number of burdens on the Free Peoples player's Ring-bearer, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* ForEachCulture
  Counts how many cultures can be spotted among cards that match `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`. This matches cards of all sides.
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachCultureToken
  Totals the number of `culture` tokens on cards matching `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.<sub> </sub>
    * filter
      default: any
    * culture
      optional
      If not provided, all culture tokens will be counted.
    * over
    * limit
    * multiplier
    * divider
* ForEachCultureInMemory
  Counts how many cultures can be spotted among cards stored at the given `memory` location, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * memory
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachFPCulture
  Counts how many Free Peoples cultures can be spotted, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  Unlike the generic version, this respects card effects that alter the number of cultures that can be spotted.
    * over
    * limit
    * multiplier
    * divider
* ForEachShadowCulture
  Counts how many Shadow cultures can be spotted, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  Unlike the generic version, this respects card effects that alter the number of cultures that can be spotted.
    * over
    * limit
    * multiplier
    * divider
* ForEachHasAttached
  Counts how many cards matching `filter` are attached to the currently affected card, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachHinderedYouCanSpot
  Counts how many hindered cards matching `filter` are in play, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * filter
      default: any
    * over
    * limit
    * multiplier
    * divider
* ForEachInDeadPile
  Counts how many cards matching `filter` (default: any) are in the current Free People's player's dead pile, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  
    * filter
      default: any
    * over
    * limit
    * multiplier
    * divider
* ForEachInDiscard
  Counts how many cards match `filter` in the given `discard` pile, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  See Úlairë Lemenya, Dark Enemy).
    * discard
      optional
      If not provided, both discard piles will contribute to the count.  
    * filter
    * over
    * limit
    * multiplier
    * divider
* ForEachInHand
  Counts how many cards match `filter` in a given player's `hand`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  Be warned that this does not automatically handle what information is hidden; counting how many cards your opponent has in hand is kosher but counting how many minions they have should not be done (instead, reveal the hand, store the results into memory, and use `ForEachInMemory`).
    * filter
      default: any
    * hand
      default: you
    * over
    * limit
    * multiplier
    * divider
* ForEachInMemory
  Evaluates how many cards stored in the `memory` location match the provided `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * memory
      required
    * filter
      default: any
    * over
    * limit
    * multiplier
    * divider
* ForEachKeyword
  Returns the grand total of all instances of `keyword` on active cards that match `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  Cases like "damage +2" will count as 2.  Note that this counts active modified amounts, not keywords printed on cards.
  If `filter` is <span style="text-decoration:underline;">not</span> provided (or if it's "any"), then this is a context-sensitive number that affects each impacted card differently ("Each Elf is resistance +X, where X is that Elf's strength.").
 If `filter` <span style="text-decoration:underline;">is</span> provided, then all valid targets are summed together to a single universal value ("This Elf is resistance +X, where X is the total strength of each Elf you can spot").
    * filter
      required
    * keyword
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachKeywordOnCardInMemory
  Same as `ForEachKeyword`, but referencing a card at the given `memory` address.
    * memory
      required
    * keyword
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachRace
  Counts the number of races on cards matching `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachSiteYouControl
  Counts the number of sites you control, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* ForEachStacked
  Counts the number of cards matching `filter` currently stacked `on` given cards, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * filter
      required
    * on
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachTopCardOfDeckUntilMatching
  Looks at cards from the top of the given player's `deck` until a card matching `filter` is found, then returns the total count looked at (including the matching card), optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  See Attunement).
    * filter
      required
    * deck
      default: you
    * over
    * limit
    * multiplier
    * divider
* ForEachThreat
  Returns the current number of threats on the Free Peoples player, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* ForEachTwilight
  Totals the current amount of tokens in the twilight pool, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* ForEachWound
  Totals the wounds on cards matching `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  If `filter` is <span style="text-decoration:underline;">not</span> provided (or if it's "any"), then this is a context-sensitive number that affects each impacted card differently ("Each Orc is strength +1 <span style="text-decoration:underline;">for each wound</span> on <span style="text-decoration:underline;">that</span> Orc").
 If `filter` <span style="text-decoration:underline;">is</span> provided, then all valid targets are summed together to a single universal value ("This minion is strength +1 <span style="text-decoration:underline;">for each wound</span> on <span style="text-decoration:underline;">each</span> Orc you can spot").
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* ForEachYouCanSpot
  Counts how many cards matching `filter` can be spotted by any player, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`. If `hindered` is true, the spot check will include hindered cards, which are otherwise ignored.
    * filter
      required
    * hindered
      default: false
    * over
    * limit
    * multiplier
    * divider
* FromMemory
  Retrieves the string value stored in `memory` and evaluates it as an integer, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  This is intended to retrieve values stored by the `MemorizeNumber` subaction.
    * memory
      required
    * over
    * limit
    * multiplier
    * divider
* MaxOfRaces
  Counts the race with the most instances that matches `filter`, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  See: Swarming Hillman)
    * filter
      required
    * over
    * limit
    * multiplier
    * divider
* NextSiteNumber
  The fellowship's current site number, +1, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
    * over
    * limit
    * multiplier
    * divider
* SiteNumberAfterNext
  The fellowship's current site number +2, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  See Get On and Get Away).
    * over
    * limit
    * multiplier
    * divider
* RegionNumber
  The fellowship's current region number, 1-3, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  This is valid in all formats, including those before the concept of regions was introduced.
    * over
    * limit
    * multiplier
    * divider
* TurnNumber
  Used to track what turn a card is played on, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.  This is usually used in conjunction with `MemorizeNumber`.
    * over
    * limit
    * multiplier
    * divider
* WhileInZone
  Retrieves the card's In-Zone Data and attempts to parse it as an integer, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`.
  If In-Zone Data is not set, this will default to -1.
    * over
    * limit
    * multiplier
    * divider
* WoundsTakenInCurrentPhase
  Totals all wounds taken by cards matching `filter` during the current phase, optionally ignoring the first `over`, up to `limit`, and applying the given `multiplier`/`divider`. 
    * filter
      required
    * over
    * limit
    * multiplier
    * divider

