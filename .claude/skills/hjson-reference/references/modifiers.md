# Modifiers
Defined in `gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/modifier/ `ModifierSourceFactory.java
Modifiers are alterations to cards, either permanent (see Lord of Moria)) or temporary (see Here is Good Rock)).  When adding temporary modifiers, this is usually done with the `AddModifier` subaction, although there are also helper subactions for common modifiers, such as `ModifyStrength` or `AddKeyword`.
In general, although the names of modifiers are case-insensitive, the names of their parameters are not.

* AddActivated
  Adds a new activated phase ability (described by `phase, requires, cost, effect, limitPerPhase, limitPerTurn`) to cards matching `filter`.  See Steadfast Champion).
  See `Activated` under `Game Text Types`, as the implementation is intended to match that.
 If `player` is specified, the action will be made available to that player.  If not, the action will default to being available to whoever owns the cards matching `filter`.
    * filter
      required
    * phase [ ]
      required
    * player
      optional
    * requires [ ]
      optional
    * cost [ ]
      optional
    * effect [ ]
      Technically optional, but there must be at least 1 subaction defined between `cost` and effect
    * limitPerPhase
      default: 0
      Integer only.
    * limitPerTurn
    default: 0
 Integer only.
    * text
      optional
* AddKeyword
  Adds the given `keyword` to cards matching `filter`, which is only applied if the `requires` requirements are met.  Keywords such as "damage+1" can have their amount provided with +, or it can be provided in `amount` instead.  
    * filter
      required
    * keyword
      required
    * requires [ ]
      optional
    * amount
      default: 1
* AddKeywordFromCards
  Synchronizes all cards matching `filter` to always have all the keywords on all cards matching `from`, so long as the given `requires` requirements are met.  Optionally restricts the effect to only copy `terrain` or `unloaded` keywords.
    * filter
      required
    * from
      required
    * requires [ ]
      optional
    * terrain
      default: false
    * unloaded
      default: false
* AddNoTwilightForCompanionMove
 Companions matching `filter` add 0 twilight when moving instead of the default 1.  Used by the obsolete Second Edition and the nascent PC concealed keyword.
    * filter
      required
* AddCulture
  Adds a `culture` to cards matching `filter`, so long as the given `requires` requirements are met.  See V3 Gondor Calls For Aid!.
    * filter
      required
    * culture
      required
    * requires [ ]
      optional
* AddRace
  Adds a `race` to cards matching `filter`, so long as the given `requires` requirements are met.  See V3 Merry, Master Hobytla.
    * filter
      required
    * race
      required
    * requires [ ]
      optional
* AddSignet
 Adds a `signet` to cards matching `filter`, so long as the given `requires` requirements are met. See Horn of the Mark).
    * filter
      required
    * signet
      required
    * requires [ ]
      optional
* AlliesTakeArcheryFireWoundsInsteadOfCompanions
  Makes allies eligible to participate in archery fire, and disallows companions from taking archery fire.  See Come Down).
* AllyCanParticipateInArcheryFireAndSkirmishes
  Permits an ally matching `filter` to be assigned archery fire wounds (and add to the fellowship archery total if they are an archer) and be assigned to skirmishes by either side, so long as the given `requires` requirements are met.
  The skirmish assignment clause will only work on unhasty allies if the source is a Shadow card or a Gandalf culture card.
    * filter
      required
    * requires [ ]
      optional
* AllyCanParticipateInArcheryFire
  Permits an ally matching `filter` to be assigned archery fire wounds (and add to the fellowship archery total if they are an archer), so long as the given `requires` requirements are met..
    * filter
      required
    * requires [ ]
      optional
* AllyCanParticipateInSkirmishes
  Permits an ally matching `filter` to be assigned to skirmishes by either side, so long as `requires` is fulfilled.  This will only work on unhasty allies if the source is a Shadow card or a Gandalf culture card.
    * filter
      required
    * requires [ ]
      optional
* AllyMayNotParticipateInArcheryFireOrSkirmishes
  Prevents allies matching `filter` from participating in archery fire or skirmishes, so long as the `requires`* requirements are met.  This should override any of the above ally-enabling effects.
    * filter
      required
    * requires [ ]
      optional
* CanBearExtraItems
  Increases the `amount` of items that characters matching `filter` can bear of a given `itemclass`, so long as the given `requires` requirements are met.  This is intended for increases only; if you intend to decrease consider using `CantBear`, below.
    * filter
      required
    * itemclass
      required
    * amount
      default: 1
    * requires [ ]
      optional
* CancelKeywordBonus
  Removes a `keyword` on cards matching `filter` that were sourced `from` a given source, so long as the `requires` requirements are met.  See Pale Crown).
    * filter
      required
    * from
      required, filter
    * keyword
      default: none (meaning any)
    * requires [ ]
      optional
* CancelStrengthBonus
  Removes strength bonuses on cards sourced `from` a given source, so long as the `requires` requirements are fulfilled.  See Parry).
    * filter
      required
    * from
      required, filter
    * requires [ ]
      optional
* CanPlayStackedCards
  Permits cards matching `filter` which are stacked `on` given cards to be played as if from hand, so long as the `requires` requirements are fulfilled.  See `Sindri, Dwarven Lord)`.
    * filter
      required
    * on
      required, filter
    * requires [ ]
      optional
* CantBear
  Prevents cards matching `filter` from bearing cards matching `cardFilter`, so long as the given `requires` requirements are met`.  `See the various tentacles, such as `Reaching Tentacle)`, although keep in mind this is not the only way this could be used.
    * filter
      required
    * cardFilter
      default: any
    * requires [ ]
      optional
* CantBeAssignedToSkirmish
  Prevents cards matching `filter` from being assigned to skirmishes by the given `player` so long as the `requires` requirements are met.
  If `player` is omitted, neither player can assign.
    * filter
      required
    * player
      optional
    * requires [ ]
      optional
* CantBeAssignedToSkirmishAgainst
  Prevents a Free Peoples character matching the `fpCharacter` filter from being assigned to a certain kind of `minion` so long as the `requires` requirements are met.  If `side` is defined, only one side is barred from performing the assignment; otherwise neither side is permitted.
    * fpCharacter
      required, filter
    * minion
      required, filter
    * side
      optional
    * requires [ ]
      optional
* CantBeDiscarded
  Prevents cards matching `filter` from being discarded `by` a particular source or `player` so long as the `requires` requirements are met.  See `Pippin, Friend to Frodo)`.
    * filter
      required
    * by
      default: any
    * player
      optional
    * requires [ ]
      optional
* CantBeExerted
  Prevents a card matching `filter` from being exerted `by` a particular source so long as the `requires` requirements are met.
    * filter
      required
    * by
      default: any
      Remember that this is a regular card filter, and not a side.  If you want shadow, you MUST put "side(shadow)" and not "shadow", which is a valid filter (for events which activate during the shadow phase).
    * requires [ ]
      optional
* CantBeHindered
    Prevents cards matching `filter` from being hindered `by` a particular source or `player` so long as the `requires` requirements are met.  
    * filter
      required
    * by
      default: any
    * player
      optional
    * requires [ ]
      optional
* CantBeOverwhelmed
  Prevents cards matching `filter` from being overwhelmed by any level of strength, so long as the given `requires` requirements are met.  See The Tale of the Great Ring).
    * filter
      required
    * requires [ ]
      optional
* CantBeOverwhelmedMultiplier
  Prevents a character matching `filter` from being overwhelmed unless their strength is outmatched by the `multiplier` factor (rather than the default 2x), so long as the `requires` requirements are met.
  If wishing to outright prevent a character from being overwhelmed, use `CantBeOverwhelmed`, above.
    * filter
      required
    * multiplier
      default: 3
      Integer only.
    * requires [ ]
      optional
* CantBeTransferred
  Prevents cards matching `filter` from being transferred from this card, so long as the `requires` requirements are met.  This likely needs to be updated to generalize where cards can't be transferred from.  See Warrior of Rohan).
    * filter
      required
    * requires [ ]
      optional
* CantCancelSkirmish
  Prevents characters matching `filter` from canceling skirmishes, so long as the `requires` requirements are met.  Omit `filter` to prevent all skirmishes from being canceled.
    * filter
      default: any
    * requires [ ]
      optional
* CantDiscardCardsFromHandOrTopOfDrawDeck
  Prevents cards matching `filter` from discarding cards from your hand or the top of your draw deck, so long as the `requires` requirements are met.
    * filter
      required
    * requires [ ]
      optional
* CantHeal
  Prevents cards matching `filter` from healing, so long as the `requires` requirements are met.
    * filter
      required
    * requires [ ]
      optional
* CantLookOrRevealHand
  Prevents the given `player` from looking at or revealing cards in a given `hand`, so long as the `requires` requirements are met.  
    * player
      required
    * hand
      required
    * requires [ ]
      optional
* CantMove
  Prevents the Free Peoples player from choosing to move, regardless of move limit, so long as the `requires` requirements are met.
    * requires [ ]
      optional
* CantPlayCards
  Prevents all players from playing cards which match `filter`, so long as the `requires` requirements are met.  See Breeland Forest).
    * filter
      required
    * requires [ ]
      optional
* CantPlayCardsFromDeckOrDiscardPile
  Prevents any player from playing cards from their draw deck or discard pile, so long as the `requires` requirements are met.  See The Great River).
    * requires [ ]
      optional
* CantPlayCardsOn
  Prevents cards matching `filter` from being played directly `on` certain cards, so long as the `requires` requirements are met.  See Hearken to Me).
  Note that this will only prevent direct playing onto, not transfers.
    * filter
      required
    * on
      required
    * requires [ ]
      optional
* CantPlayPhaseEvents
  Prevents the given `player` from playing events during the given `phase`, so long as the given `requires` requirements are met.  See Gates of Argonath).
  If `player` is omitted, both players will be affected.
    * player
      optional
    * phase
      required
    * requires [ ]
      optional
* CantPlayPhaseEventsOrPhaseSpecialAbilities
  Prevents the given `player` from playing events or activating special abilities keyed to the given `phase`, so long as the given `requires` requirements are met. See Saruman's Snows).
  If `player` is omitted, both players will be affected.
    * player
      optional
    * phase
      required
    * requires [ ]
      optional
* CantPlaySite
  Prevents the given `player` from playing sites from their Adventure Deck, so long as the given `requires` requirements are met.  See Orkish Fiend).
    * player
      required
    * requires [ ]
      optional
* CantPreventWounds
  Prevents <span style="text-decoration:underline;">both players</span> from preventing wounds, so long as the `requires` requirements are met.
    * requires [ ]
      optional
* CantRemoveBurdens
  Prevents cards matching `filter` from removing burdens, so long as the `requires` requirements are met.
    * filter
      default: any
    * requires [ ]
      optional
* CantRemoveThreats
  Prevents cards matching `filter` from removing threats, so long as the `requires` requirements are met.
    * filter
      default: any
    * requires [ ]
      optional
* CantReplaceSite
  Prevents the given `player` from replacing sites matching `filter`, so long as the `requires` requirements are met.
  If `player` is omitted, both players are affected.
    * filter
      default: any
    * player
      optional
    * requires [ ]
      optional
* CantTakeArcheryWounds
  Prevents cards matching `filter` from being assigned archery wounds by their owner, so long as the `requires` requirements are met. 
    * filter
      required
    * requires [ ]
      optional
* CantTakeMoreWoundsThan
  Makes cards matching `filter` take no more than `amount` wounds during the given `phase`, so long as the `requires` requirements are met.
  Used for cards which cause a character to "take no more than X wounds".  This is different from preventing wounds, as it disallows wounds from even being assigned (such as from archery or threats).  See Armor).
    * filter
      required
    * phase
      optional
    * amount
      default: 1
    * requires [ ]
      optional
* CantTakeWounds
  Same as the previous entry, except that it applies to all wounds.  Makes cards matching `filter` take no wounds, so long as the `requires`* requirements are met.
    * filter
      required
    * requires [ ]
      optional
* CantTakeWoundsFromLosingSkirmish
  Negates the wounds normally awarded at the end of a lost skirmish for characters matching `filter`, so long as the `requires`* requirements are met. See Tremendous Wall).
    * filter
      required
    * requires [ ]
      optional
* CantTouchTokens
  Prevents culture tokens from being added, removed, or reinforced, so long as the `requires`* requirements are met.*  See Redhorn Pass).
    * requires [ ]
      optional
* CantUseSpecialAbilities
  Prevents the given `player` from using any special abilities on cards matching `filter` during the given `phase`, so long as the `requires`* requirements are met*.  See Isengard Warrior).
  If `player` is omitted, both players will be affected.
 If `phase` is omitted, all phases will be affected
    * filter
      default: any
    * phase
      optional
    * player
      optional
    * requires [ ]
      optional
* DisableGameText
  Prevents the game text of cards matching `filter` from being used, so long as the given `requires` requirements are met.  See Helpless).
    * filter
      required
    * requires [ ]
      optional
* DoesNotAddToArcheryTotal
  Prevents cards matching `filter` from contributing to their respective side's archery total, negating the Archer keyword, so long as the given `requires` requirements are met.
  See Naith Warband).
    * filter
      required
    * requires [ ]
      optional
* DuplicateActionFromPhase
  Causes cards matching `filter` with activated abilities that are normally only allowed during `oldPhase` to be used during `newPhase`, so long as the given `requires` requirements are met.  See V3 Wizard's Pipe
    * filter
      required
    * oldPhase[]
      required
    * newPhase[]
      required
    * requires [ ]
      optional
* DuplicateGameText
  Causes cards matching `filter` to duplicate game text (including triggers, actions, but <span style="text-decoration:underline;">not</span> keywords; for that see `AddKeywordFromCards`) `from` the given cards, so long as the given `requires` requirements are met.
    * filter
      required
    * from
      required; only "self" or "memory()"
    * requires [ ]
      optional
* ExtraCostToPlay
 Adds an additional play `cost` to cards matching `filter`, so long as the `requires` requirements are met.  See Wrath of Harad).
    * filter
      required
    * cost [ ]
      required
    * requires [ ]
      optional
    * requiresRanger
      default: false
      Only used for Wrath of Harad.
* FPCultureCount
  Alters the number of Free Peoples cultures that can be spotted by `amount`, so long as the `requires` requirements are met.
  This alters the behavior of with cards that "spot X Free Peoples cultures" to do something.
    * amount
      required
    * requires [ ]
      optional
* HasToMoveIfAble
  Forces the active Free Peoples player to choose to move, so long as the `requires` requirements are met.  The move limit must still permit a move.
    * requires [ ]
      optional
* ItemClassSpot
  Increases the number of items of a given `class` that can be spotted by 1, so long as the `requires` requirements are met.   See Gimli's Pipe). Theoretically this will need expanded in the future to take a value, both positive and negative.
    * class
      required
    * requires [ ]
      optional
* ModifyArcheryTotal
  Modifies the archery total by `amount` for the given `side`, so long as the `requires` requirements are met.  Value can be either positive or negative.  If attempting to zero-out one side's total, a common workaround is to use this to modify the total by -1000.  See Double Shot).
    * amount
      required
    * side
      required
    * requires [ ]
      optional
* ModifyCost
  Alters the twilight cost of cards matching `filter` by `amount`, so long as the `requires` requirements are met.  Value can be either positive or negative.  See East Road).
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* ModifyInitiativeHandSize
  Alters the card threshold for the Free Peoples player to have initiative by `amount`.  Value can be either positive or negative.  See Streaming to the Field).
    * amount
      required
* ModifyMoveLimit
  Modifies the move limit for the currently active Free Peoples player by `amount`.  Value can be either positive or negative.
  Note that the rules require that move limit modifications are permanent, but this is not managed by the engine.  You will need to ensure that the duration of this modifier is set to EndOfTurn.  See Gift of Boats).
    * amount
      required
    * requires [ ]
      optional
* ModifyPlayOnCost
  Alters the twilight cost of cards matching `filter` played `on` a given character by `amount`, so long as the `requires` requirement is fulfilled.  Value can be either positive or negative.  See Frodo, Reluctant Adventurer).
    * filter
      required
    * on
      required
    * amount
      required
    * requires [ ]
      optional
* ModifyRaceSpotCount
  Adds 1 to the number of characters of the given `race` that can be spotted.  This will probably need updated to accept an amount in the future.  See Pippin, In the Bloom of Health).
    * race
      required
    * requires [ ]
      optional
* ModifyResistance
  Alters the resistance of cards matching `filter` by `amount`, so long as the `requires` requirements are met.  Value can be either positive or negative.  See Phial of Galadriel).
    Remember that only companions have a meaningful resistance stat.
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* ModifyRoamingPenalty
  Alters the roaming cost of roaming minions matching `filter` by `amount`, so long as the `requires` requirements are met.  Theoretically this could be either positive or negative, but this is only currently used in a negative manner (reducing the roaming cost).  See Uruk Scout).
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* ModifySanctuaryHeal
  Alters the `amount` of sanctuary healing (default of 5 wounds), so long as the `requires` requirements are met. Value can be either positive or negative.  See Rath Dinen).
    * amount
      required
    * requires [ ]
      optional
* ModifySiteNumber
  Alters the site number of minions matching `filter` by `amount`, so long as the `requires` requirements are met.  Value can be either positive or negative. Remember that this will only incur a roaming penalty if this modifier is in place before the minion is played.  See Grishnakh, Orc Captain).
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* ModifyStrength
    Alters the strength of cards matching `filter` by `amount`, so long as the `requires` requirements are met. Value can be either positive or negative.  See Battle Fury).
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* ModifyVitality
  Alters the vitality of cards matching `filter` by `amount`, so long as the `requires` requirements are met. Value can be either positive or negative. See Little Golden Flower).
  This should be avoided in card design at all costs, and instead rely on permanents that visibly alter the vitality amount.
    * filter
      required
    * amount
      required
    * requires [ ]
      optional
* NoMorethanOneMinionMayBeAssignedToEachSkirmish
  Prevents…more than one minion from being assigned to each skirmish, unless the `requires` requirements are met.  See Caras Galadhon).
    * requires [ ]
      optional
* RemoveAllKeywords
  Makes cards matching `filter` lose all keywords, so long as as the `requires` requirements are met.  See Eowyn, Northwoman).
    * filter
      required
    * requires [ ]
      optional
* RemoveCardsGoingToDiscard
  Causes all cards about to enter the discard pile to be removed from the game instead, so long as as the `requires` requirements are met.  See Foot of Mount Doom).
    * requires [ ]
      optional
* RemoveKeyword
  Removes `keyword` from cards matching `filter` and prevents them from gaining the keyword again while active, so long as the `requires` requirements are met.  See Mithril-coat).
    This can be used on pseudo-keywords as well, such as roaming and mounted.  
    * filter
      required
    * keyword
      required
    * requires [ ]
      optional
* RingbearerCantTakeThreatWounds
    Prevents threat wounds from being assigned to the Ring-bearer, so long as the `requires` requirements are met.  See The Witch-king, Morgul King).
    * requires [ ]
      optional
* RingTextIsInactive
  Disables The One Ring's game text, so long as the `requires` requirements are met.  See Return to its Master).
    * requires [ ]
      optional
* SarumanFirstSentenceInactive
  Disables the first sentence of Saruman's game text, so long as the `requires` requirements are met.  Note that this is not an automated process; all Sarumen need to be coded in a way that conditionally checks for this modifier on the appropriate actions that are part of his "first sentence".  See Saruman's Staff, Wizard's Device) (and Saruman, Keeper of Isengard)).
    * requires [ ]
      optional
* ShadowHasInitiative
  Causes the Shadow side to have initiative, regardless of how many cards are in the Free Peoples player's hand, so long as the `requires` requirements are met.  See Feel His Blade).
    * requires [ ]
      optional
* SkipPhase
  Causes a particular `phase` to be skipped, so long as the `requires` requirements are met.  Note that this modifier must be in place before the phase in question for it to work; enabling it mid-phase does nothing.  See Cave Troll's Chain).
    * phase
      required
    * requires [ ]
      optional
* SkirmishesResolvedInOrderByFirstShadowPlayer
  Causes the Shadow player to determine skirmish order instead of the Free Peoples player, so long as the `requires` requirements are met. See Seeking New Foes).
    * requires [ ]
      optional
* ThreatLimit
  Alters how many threats can be on the dead pile at once  (by default, 1 per companion in play).
    * amount
      required
    * requires [ ]
      optional
* TransferForFree
  Waives the twilight cost for transferring items, so long as the `requires` requirements are met.  See Chamber of Mazarbul).
    * requires [ ]
      optional
* UnhastyCompanionCanParticipateInSkirmishes
  On Gandalf] cards, permits Unhasty companions matching `filter` to skirmish and participate in archery fire, so long as the `requires` requirements are met.  See [Lindenroot, Elder Shepard).
    * filter
      required
    * requires [ ]
      optional
* WinsAfterReconcile
  Delays the end of the game until after the regroup phase on site 9, so long as the `requires` requirements are met.  See Caverns of Isengard).
  Note that this ability has no use in Player's Council formats, as this was made a basic rule.
    * requires [ ]
      optional
    
    


