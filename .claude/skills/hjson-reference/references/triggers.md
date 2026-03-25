# Triggers
Defined in `gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/trigger/ `TriggerCheckerFactory.java
Triggers describe the timing that an action "listens for" before that action can be taken, especially using verbiage such as When and Each Time.  

## Site Movement Triggers
Here are triggers that respond to different parts of the movement process, in order of when they occur.

* MovesFrom
  Triggers before the fellowship leaves a site matching `filter`, which represents "When you move from X" text.
  While this move cannot be prevented, the action can evaluate final actions at the last instant before the fellowship has left the current site.
    * filter
      default: any
* Moves
  Triggers as the fellowship moves with a site-agnostic viewpoint, which represents "When the fellowship moves" text.
  While this move cannot be prevented, the action can evaluate e.g. the state of the Twilight Pool before the Shadow Number of the next site kicks in, and before fellowship twilight is calculated, etc.
* AboutToMoveTo
  Triggers before the fellowship moves to a site matching `filter`, which represents "When the fellowship is about to move to X", which is the last possible moment to change the site being moved to.  This is the only before trigger for this process.
    * filter
      default: any
* MovesTo
  Triggers as the fellowship moves to a site matching `filter`, which represents "When you move to X" text.
  This should predominately be where triggers based on the new site should be evaluated, though be aware that movement twilight has not yet been added.
    * filter
      default: any

## Timing-based Triggers
These events trigger based on the advancement of the game to various points, especially the start and end of phases.


* StartOfTurn
  Triggers at the start of the turn (just after players have swapped roles). This is before the fellowship phase, but after sanctuary healing (if applicable).  
* StartOfPhase
  Triggers as a `phase` begins, representing "At the start of the X phase" actions. 
    * phase
      default: null (which will cause this to trigger at the start of every phase)
* EndOfPhase
  Triggers as a `phase` ends.
    * phase
      default: null (which will cause this to trigger at the end of every phase)
* EndOfTurn
  Triggers at the end of the turn (before players have swapped roles).

## Before Triggers
Before triggers occur before the action they are keyed to, which means that they can prevent the action from occurring (if needed).

* AboutToAddBurden
  Triggers before one or more burdens sourced from a card matching `filter` are added, optionally storing the card source at the given `memorize` location.
    * filter
      required
    * memorize
      optional
* AboutToAddTwilight
  Triggers before twilight is added from a card matching `filter`.  Note that this triggers on site movement, ambush, and card actions, but does <span style="text-decoration:underline;">not</span> trigger from paying a card's twilight cost.
    * filter
      required
* AboutToBeKilled
  Triggers before a character matching `filter` is killed.  Optionally stores the dying characters to the given `memorize` location.
    * filter
      required
    * memorize
      optional
* AboutToBeOverwhelmed
  Triggers before a character matching `filter` is overwhelmed in a skirmish.
    * filter
      required
* AboutToDiscard
  Triggers before a card matching `filter` is discarded from play by a card matching `source`.  If `opponent` is true, this will only trigger by other players performing the discard.  Optionally stores the source of the discard to `memorizeSource` and the targeted cards to `memorizeDiscarding.
    * filter
      required
    * source
    default: any
    * opponent
      default: false
    * memorizeSource
      optional
    * memorizeDiscarding
      optional
* AboutToDrawCard
  Triggers before the given `player` draws a card.
    * player
      required
* AboutToExert
  Triggers before a character matching `filter` exerts.
    * filter
      required
* AboutToHeal
  Triggers before a character matching `filter` heals.
    * filter
      required
* AboutToHinder
  Triggers before a card matching `filter` is hindered` by player `using a card matching `source`.  
    * filter
      default: any
    * source
      default: any
    * player
      optional
* AboutToRestore
  Triggers before a card matching `filter` is restored by `player` using a card matching `source`.  
    * filter
      default: any
    * source
      default: any
    * player
      optional
* AboutToTakeControlOfSite
  Triggers before a card matching `filter` causes any player to take control of a site.
    * filter
      required
* AboutToTakeWound
  Triggers before a character matching `filter` takes a wound from a card matching `source`.  Optionally stores the wounded characters to the given `memorize` location (ignoring prevented wounds).  
    * filter
      required
    * source
    default: any
    * memorize
      optional
* BeforeThreatWounds
  Triggers when a character matching `filter` is killed by `deathCause` right before threat wounds are placed, and represents the last chance to add or remove threats before the threat pool is cleared.
  If `deathCause` is omitted, all death causes are accepted.
    * filter
      required
    * deathCause
    optional
* BeforeToil
  Triggers when a card with toil matching `filter` is played, before its owner has chosen toil exertion targets.  See Ulaire Enquea, Black Threat).
    * filter
      required
* FPStartedAssigning
  Triggers after all Assignment phase actions have been taken and the Free Peoples player is just about to start assigning.  Used for "Freeps must do X to assign Y to a skirmish"; see Morgul Spawn).

## After Triggers
After triggers occur after the action they are keyed to, which means they cannot actually alter those actions.

* AddsBurden
  Triggers after a card matching `filter` causes a given `player to `add a burden.  If `player` is not provided, triggers on all players.
    * filter
      default: any
    * player
      optional
* AddsCultureToken
  Triggers after a card matching `filter` causes a given `player` to add culture tokens `on` a given card
    * filter
      default: any
    * on
      default: any
    * player
      optional
* AddsThreat
  Triggers after a card matching `filter` adds a threat.
    * filter
      default: any
* AddsTwilight
  Triggers after a card matching `filter` adds any number of twilight tokens, optionally restricted to only twilight from the given `cause`.
    * filter
      default: any
    * cause
      default: none (meaning any)
* AfterAllSkirmishes
  Triggers after all normal and fierce skirmishes have resolved.  See The Witch-king's Beast, Fell Creature).
* AssignedAgainst
  Triggers after a character matching `filter` is assigned `against` another character by the given `side`.  Optionally memorizes the assigned card and/or the card assigned against.  See Candle Corpses).
  If `side` is omitted, this will trigger for both sides.
    * filter
      default: any
    * side
      optional
    * against
      default: any
    * memorizeAgainst
      optional
    * memorizeAssigned
      optional
* AssignedToSkirmish
  Triggers after a character matching `filter` is assigned by one `side`, and optionally `memorize`s the assigned character.
  If `side` is omitted, this will trigger for both sides.
    * filter
      default: any
    * side
      optional
    * memorize
      optional
* CancelledSkirmish
  Triggers after a Free Peoples character matching `filter` has their skirmish canceled.
    * filter
      default: any
* ConstantlyCheck
  Constantly checks to see if the given `requires` requirements are fulfilled and triggers as soon as they do.
    * requires
      required
* Discarded
  Triggers after a card matching `filter` is discarded `from` the given zone by `player` using a card matching `source`, and optionally `memorize`s the discarded card.
  If `ignoreVoluntary` is true, then the trigger will ignore situations like "the Free Peoples player may discard a card from hand to prevent this".  
    * filter
      default: any
    * source
      required
    * player
      optional
    * fromZone
      optional
      At this time, only `HAND` and `DECK` are supported.  If you wish to capture cards discarded from play, omit this value (the default behavior).  
    * ignoreVoluntary
      default: false
    * memorize
      optional
* Exerted
  Triggers after a character matching `filter` is exerted by another card matching `source`, and optionally `memorize`s the exerted character.
    * filter
      default: any
    * source
      default: any
    * memorize
      optional
* ExertsForSpecialAbility
  Triggers after a card matching `filter` exerts as part of executing a special ability of a card matching `source`, and optionally `memorize`s the exerted character.
  If `itsOwn` is true, `source` is ignored and the source is checked to be the same as the exerting card.
 See Rush of Steeds).
    * filter
      default: any
    * source
      default: any
    * itsOwn
    default: false
    * memorize
      optional
* ExertsToPlay
  Triggers after a card matching `filter` exerts as part of a cost `toPlay` a given kind of card.  Optionally stores the exerting card at the given `memorize` location.  See Legolas' Bow).
    * filter
      default: any
    * toPlay
      default: any
    * memorize
      optional
* FPDecidedIfMoving
  Triggers after the Free Peoples player chooses whether to move or stay in the Regroup phase, regardless of the choice.
* FPDecidedToMove
  Triggers after the Free Peoples player chooses to move in the Regroup phase, before the Shadow phase.
* FPDecidedToStay
  Triggers after the Free Peoples player chooses to stay in the Regroup phase, before they reconcile.
* Heals
  Triggers after a character matching `filter` is healed by `player`, and optionally `memorize`s the healed character.
    * filter
      default: any
    * player
      default: you
    * memorize
      optional
* Hindered
  Triggers after a card matching `filter` in the given `zone` is hindered` by player `using a card matching `source`, and optionally `memorize`s the hindered card.  
    * filter
      default: any
    * source
      default: any
    * player
      optional
    * zone
      optional
      Out-of-play zones will result in an error. 
    * memorize
      optional
* Killed
  Triggers after a character matching `filter` is killed by a given `killer` from a given `cause`, and optionally `memorize`s the killed character.
  If `inSkirmish` is true, only kills during an active skirmish are counted.
    * filter
      default: any
    * killer
      default: any
    * inSkirmish
    default: false
    * cause
    default: none (meaning any)
    * memorize
      optional
* LosesInitiative
  Triggers after the given `side` loses initiative.  See Glimpse of Fate).
    * side
      required
* LosesSkirmish
  Triggers after a character matching `filter` loses a skirmish, optionally `against` a certain kind of character, and optionally `memorize`s the losing character.
  If `overwhelm` is true, only overwhelm losses are counted.
    * filter
      default: any
    * against
      default: any
    * overwhelm
      default: false
    * memorize
      optional
* NewRingBearer
  Triggers after a character matching `filter` becomes the new Ring-bearer after the previous one is killed.
    * filter
      default: any
* Played
    Triggers after a card is played matching `filter` by `player`,  optionally played `on` a given card, `from` a given card, from a given `fromZone` and optionally `memorize`s the played card.  `exertsRanger` can also be turned on to represent "If an event is played that spots or exerts a ranger", a la Orc Scout).
    All parameters except `filter` can be omitted.
    * filter
      required
    * from
      optional
    * fromZone
      optional
    * player
      optional
    * on
      optional
    * memorize
      optional
    * exertsRanger
      default:false
* PlayerDrawsCard
  Triggers after `player` draws a card from their draw deck or takes a card into hand.
    * player
      required
* PutsOnRing
  Triggers after the Ring-bearer "puts on" The One Ring.
* Reconciles
  Triggers after a `player` reconciles.  `player` can be omitted to react to any reconciliation.
    * player
      optional
* RemovedFromPlay
  Triggers when a card is discarded, killed, or returned to hand. (This should probably also trigger upon removal from the game, stacking, placing on the draw deck, and other situations, but the triggers to piggyback don't all exist at this time.)
    * filter
      default: any
    * memorize
      optional
* RemovesBurden
  Triggers after `player` removes a burden using a card matching `filter`.  If `player`* is omitted, both players will trigger.
    * filter
      default: any
    * player
    optional
* RemovesCultureToken
  Triggers after a card matching `filter` causes a given `player` to add culture tokens `on` a given card
    * filter
      default: any
    * on
      default: any
    * player
      optional
* ReplacesSite
  Triggers after `player` replaces the site at the given `number` position.  Both `player` and `number` may be omitted to trigger on either player or any site.
    * player
      optional
    * number
      default: 0 (any)
* Restored
  Triggers after a card matching `filter` in the given `zone` is restored by `player` using a card matching `source`, and optionally `memorize`s the restored card.  
    * filter
      default: any
    * source
      default: any
    * player
      optional
    * zone
      optional
      Out-of-play zones will result in an error. 
    * memorize
      optional
* RevealsCardFromDrawDeck
  Triggers after a card matching `filter` is revealed from a given `deck` by a card matching `source`.
    * filter
      default: any
    * source
      default: any
    * deck
      optional (defaults to both)
* RevealedCardFromHand
  Triggers if the given `player` reveals a card matching `filter` from a hand.  If `player` is omitted, both players will trigger..
    * filter
      default: any
    * player
    optional
* SiteControlled
  Triggers when the given `player` takes control of a `site` matching the given filter.
    * player
    default: null (any)
    * filter
      default: any
* SiteLiberated
  Triggers when the given `player` liberates a `site` matching the given filter.
    * player
    default: null (any)
    * filter
      default: any
* SkirmishAboutToEnd
  Triggers when a skirmish `involving`* characters matching the given filter is about to end, either through a normal resolution or via being canceled.
    * involving
      default: any
* StartOfSkirmishInvolving
  Triggers if a skirmish involving cards matching `filter` starts `against` given cards, before any skirmish actions are available.
    * filter
      required
    * against
      default: any
* TakesOffRing
  Triggers after the Ring-bearer "takes off" The One Ring, usually at the start of the Regroup Phase, but also potentially the end of the regroup phase if it was put on during Regroup (or at various other times if a card effect forced it to be taken off).
* TakesWound
  Triggers after a character optionally matching `filter` takes a wound that was not prevented or deferred any other way from a card matching `source`, and optionally `memorize`s the wounded character. If `threat` is set to true, this will only trigger on threat wounds.
    * filter
      default: any
    * source
      default: any
    * threat
      default: false
    * memorize
      optional
* Transferred
  Triggers after a card matching `filter` is transferred (optionally `to` another card).  Specifically does not trigger upon being played.  
    * filter
      default: any
    * to
      default: any
* UsesSpecialAbility
  Triggers after a card optionally matching `filter` activates a special ability during the given `phase` by the given `player`, and optionally `memorize`s that card.
  While this is technically an after trigger, it can still cancel the special ability it is reacting to; this reaction will happen after the costs/requirements have been paid but before the effect has occurred.
    * filter
      default: any
    * player
    default: null (any)
    * phase
      optional
    * fromZone
      optional
    * memorize
      optional
* WinsSkirmish
  Triggers after a character matching `filter` wins a skirmish `against` a certain kind of character, and optionally `memorize`s the winning, losing, and/or involved characters.
    * filter
      default: any
    * against
      default: any
    * memorize
      optional
    * memorizeInvolving
      optional
    * memorizeLoser
      optional

