# Requirements
Defined in `gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/requirement/ `RequirementFactory.java
Requirements are essentially true-false functions that are either fulfilled or unfulfilled.  Various other subactions will define requirements that must be in place for an action to be valid (especially `Modifiers`), or perhaps changing behavior depending on whether requirements are fulfilled or not.

## Operation Requirements
Meta-level operations that modify the output of other requirements or value evaluations.

* And
  True if all child `requires` requirements are true.  This is typically only useful in conjunction with other operation requirements or other contexts where exactly 1 requirement is expected, so you can expand it into multiple.  In most situations however, you can simply define multiple requirements in an array and avoid this requirement entirely.
    * requires
      optional (but why?)
* Not
 True if all child `requires` requirements fail.  Effectively inverts their output, so that "can spot an exhausted companion" becomes "cannot spot an exhausted companion", etc.  Note that for convenience several Can requirements have Cant versions, making this unnecessary in those cases.
    * requires
      optional (but why?)
* Or
  True if at least one of the child `requires` requirements is true, even if all others fail.
  Note that this operation has short-circuiting, meaning that if a child requirement succeeds, none of the subsequent children will be evaluated.  This <span style="text-decoration:underline;">probably</span> doesn't have any actionable ramifications…but keep it in mind.
    * requires
      optional (but why?)
* IsEqual
  True if `firstNumber` and `secondNumber` are equal.
    * firstNumber
      required
    * secondNumber
      required
* IsNotEqual
    True if `firstNumber` and `secondNumber` are unequal.
    * firstNumber
      required
    * secondNumber
      required
* IsGreaterThan
  True if `firstNumber` is greater than `secondNumber`.
    * firstNumber
      required
    * secondNumber
      required
* IsGreaterThanOrEqual
  True if `firstNumber` is greater than or equal to `secondNumber`.
    * firstNumber
      required
    * secondNumber
      required
* IsLessThan
    True if `firstNumber` is less than `secondNumber`.
    * firstNumber
      required
    * secondNumber
      required
* IsLessThanOrEqual
  True if `firstNumber` is less than or equal to `secondNumber`.
    * firstNumber
      required
    * secondNumber
      required

## Simple Requirements

* AlwaysAvailable
  This requirement should be treated as always true.  This is only useful when passing in requirements for a Choice effect, in which it can be used to indicate that the given choice has no conditional requirements.

## Function Requirements

* CanMove
  True if the current Free Peoples player's move count is less than the move limit, i.e. they can voluntarily choose to move again if they so desire.
* CanSelfBePlayed
  True if this card can be played.  This is normally handled automatically when playing from hand, but if self-playing from other contexts (from discard, from a stack, etc), then this may need used to verify all the fiddly sub-requirements like uniqueness and so on.
* CanSpot
    True if at least `count` cards are in play and active that match `filter`. Optionally includes currently `hindered` cards in the count.
    * count
    default: 1; integer only
    * hindered
    default: false
    * filter
      required
* CantSpot
  True if the above `CanSpot` requirement would return false.
    * count
      default: 1; integer only
    * filter
      required
* CanSpotBurdens
  True if the current Ring-bearer has at least `amount` burdens.
    * amount
      default: 1; integer only
* CantSpotBurdens
  True if the above `CanSpotBurdens` requirement would return false.
    * amount
      default: 1; integer only
* CanSpotCultureTokens
  True if the total amount of tokens matching `culture` on all cards matching `filter` is at least `amount`. Optionally stores how many were counted into the given `memorize` location.
  If `culture` is omitted, this counts culture tokens of all kinds.
    * culture
      optional
    * filter
      default: any
    * amount
      default: 1
    * memorize
      optional
* CanSpotFPCultures
  True if the total number of cultures on spottable Free Peoples cards is at least `amount`.  
    * amount
      required; integer only
* CantSpotFPCultures
  True if there are fewer than `amount` total Free Peoples cultures on cards that are in play and active.  See Mordor Pillager).
    * amount
      required; integer only
* CanSpotDifferentCultures
  True if you can spot at least `count` cultures among cards matching `filter`.   
    * filter
      default: any
    * count
    required; integer only
* CanSpotSameCulture
  True if you can spot at least `count` cards of the same culture among cards matching `filter`.   
    * filter
      default: any
    * count
    required; integer only
* CanSpotShadowCultures
  True if the total number of cultures on spottable Shadow cards is at least `amount`.  
    * amount
      required; integer only
* CanSpotThreats
  True if the current Free Peoples player's threat count is at least `amount.
    * amount
      default: 1; integer only
* CantSpotThreats
  True if the current Free Peoples player's threat count is fewer than `amount.
    * amount
      default: 1; integer only
* CanSpotTwilight
  True if there are at least `amount` tokens in the Twilight Pool.
    * amount
      default: 1; integer only
* CanSpotWounds
  True if there are at least `amount` total wounds on the cards matching `filter`.
    * amount
      default: 1; integer only
    * filter
      required
* CardsInDeckCount
  True if a given player's `deck` matches the exact `count` given.  Only really useful for e.g. The Irresistible Shadow).
    * deck
      default: you
    * count
      required
* CardsInHandMoreThan
  True if a given `player`'s hand has more cards than `count`.
    * player
      default: you
    * count
      required; integer only
* ControlsSite
  True if the given `player` controls at least `count` sites.
    * player
      default: you
    * count
      default 1
* DidWinSkirmish
  True if any card matching `filter` won a skirmish this turn.
    * filter
      required
* FierceSkirmish
  True if the game is in a Fierce assignment or skirmish phase.
* HasCardInAdventureDeck
  True if the given `player` has at least `count` cards matching `filter` in their Adventure Deck.
  Be warned that this does not automatically respect what information is hidden; counting how many cards your opponent has in their adventure deck is kosher but counting how many battlegrounds they have should not be done unless that information is first revealed.
    * player
      default: you
    * count
      default: 1; integer only
    * filter
      default: any
* HasCardInDeadPile
  True if the current Free Peoples player's dead pile contains at least `count` cards matching `filter`.
    * count
      default: 1; integer only
    * filter
    default: any
* HasCardInDiscard
  True if the given `player` has at least `count` cards matching `filter` in their discard pile.
  Be warned that this does not automatically respect what information is hidden; in official Decipher formats the discard pile is hidden information unless revealed.  (In PC formats discard piles are public and do not have this issue.)
    * player
      default: you
    * count
      default: 1; integer only
    * filter
      default: any
* HasCardInDrawDeck
  True if the given `player` has at least `count` cards matching `filter` in their draw deck.
  Be warned that this isn't really a game concept, as decks are normally hidden information.  Use this only as part of a secondary operation to make a primary effect work properly.
    * player
      default: you
    * count
      default: 1; integer only
    * filter
      default: any
* HasCardInHand
  True if the given `player` has at least `count` cards matching `filter` in their hand.
  Be warned that this does not automatically handle what information is hidden; counting how many cards your opponent has in hand is kosher but counting how many minions they have should not be done unless that information is revealed first.
    * player
      default: you
    * count
      default: 1; integer only
    * filter
      default: any
* HasCardInRemoved
  True if the given `player` has at least `count` cards matching `filter` in their removed-from-game pile.
    * player
      default: you
    * count
      default: 1; integer only
    * filter
      default: any
* HasCardStacked
  True if at least `count` cards matching `filter` are stacked `on` the given cards.
    * count
      default: 1; integer only
    * filter
      required
    * on
      default: any
* HasCardInMemory
  True if there are any cards at all stored at the given `memory` location.
    * memory
      required
* HasMemory
  True if there is any value data or card data stored at the given `memory` location.
    * memory
      required
* HasInZoneData
  True if any active card matching `filter` has any In-Zone Data.  
    * filter
      required
* HaveInitiative
  True if the given `side` currently has initiative.
    * side
      required
* IsAhead
  True if the current Free Peoples player is currently furthest down the site path (ties do not count).
* IsOwner
  True if the current performing player is the same as the one who owns the card this action is attached to (??).  The usage of this requirement seems dubious.
* IsSide
  True if the current evaluating player is `side` or if the currently evaluating trigger was triggered by `side`.  This can be used to ensure that only one side is affected by a site's text, or to ensure that only one side activated a trigger.  See Summit of Amon Hen) and Freca, Hungry Savage).
    * side
      required
* KilledWithSurplusDamage
  True if a recently-killed character had unallocated wounds from a damage bonus, and moreover stores the unallocated number to the given `memorize` location.  This requirement is only coherent as part of a `KilledInSkirmish` trigger.  See More Yet to Come).
    * memorize
      optional
* Location
  True if the active fellowship's current site matches `filter`.
    * filter
      required
* LostSkirmishThisTurn
  True if any character matching `filter` lost a skirmish this turn.  See Citadel of Minas Tirith).
    * filter
      required
* MemoryIs
  True if the string data stored in the given `memory` location is the same as the given `value` (case insensitive).  This is best used for comparing the values of previous choices stored when a player selected an option in a `Choice` subaction.
    * memory
      required
    * value
      required
* MemoryLike
  True if the string data stored in the given `memory` location contains the given `value` somewhere within it as a substring (case insensitive).  This is best used for comparing the values of previous choices stored when a player selected an option in a `Choice` subaction, when the choice was between two sentences rather than a controlled yes/no.
    * memory
      required
    * value
      required
* MemoryMatches
  True if the cards stored within the given `memory` location contain at least `count` cards which matches `filter`.
    * memory
      required
    * filter
      required
    * count
      default: 1
* MoveCountMinimum
  True if the number of times the current Free Peoples player has moved is greater than or equal to `amount`.
    * amount
      required; integer only
* OpponentDoesNotControlSite
  True if no opponent of the given `player` controls a site.
    * player
      default: you
* PerPhaseLimit
  True if a phase-limited action has been invoked fewer times than a given `limit` (i.e. the action can still be legally invoked without violating a "(limit twice per phase)" or similar clause).  By default such counters are global, but `perPlayer` may be set to true to ignore instances of the action invoked by one's opponent.
  By default, this is used to track per-phase limits (naturally), but if `phase` is provided this can instead track some other time period.  Providing "regroup" for instance will for many purposes act as a "per site limit".
 This limit can be interacted with by use of the `IncrementPerPhaseLimit` subaction.
    * limit
      default: 1
    * phase
      default: null
      Most applications do not need to provide a phase.
    * perPlayer
      default: false
* PerTurnLimit
  True if a turn-limited action has been invoked fewer times than a given `limit` (i.e. the action can still be legally invoked without violating a "(limit twice per turn)" or similar clause).
  This limit can be interacted with by use of the `IncrementPerTurnLimit` subaction.
    * limit
      default: 1
* PlayableFromDiscard
  True if the given `player` is eligible to play any cards matching `filter` from the discard pile, assuming the given `discount` is applied.
    * player
      default: you
    * filter
      default: any
    * discount
      optional
* Phase
  True if the current gameplay phase is the same as the given `phase.
    * phase
      required
* PlayedCardThisPhase
  True if any cards matching `filter` were played this phase; at least `min` and at most `max`.
    * filter
      required
    * min
      default: 1
    * max
      default: 100
* RingIsActive
  True if The One Ring's text is currently available to be used, i.e. it has not been disabled by the likes of Return to its Master).
* RingIsOn
  True if any action has caused the Ring-bearer to "put on" The One Ring.
* SarumanFirstSentenceActive
  True if Saruman's first sentence of text is to be treated as active, i.e. it is has not been disabled by the likes of Saruman's Staff, Wizard's Device).
* ShadowPlayerReplacedCurrentSite
  True if the Shadow player has replaced the Fellowship's current site this turn.  See Many Miles).
* SiteAvailableToControl
  True if there are any sites available to be controlled, i.e. all players have passed such a site and it is uncontrolled on the adventure path.
* ThreatLimit
  True if the current threat limit is greater than or equal to the given `amount`..
    * amount
      required
* ThreatsRemaining
  True if the current threat limit will permit at least `amount` threats to be added. e.g. if you want to add 2 threats and there are 3 unplaced threats, this will be true, and false if there's only 1 addable threat..
    * amount
      required
* TwilightPoolLessThan
  True if the current number of twilight tokens in the Twilight Pool are fewer than the given `amount`.  
    * amount
      required; integer only
* WasAssignedToSkirmish
  True if any cards matching `filter` were assigned to a skirmish this turn.  See Thin and Stretched).
    * filter
      required
* WasPlayedFromZone
  True if the card which owns the current action was played from the given `zone`.
    * zone
    required

