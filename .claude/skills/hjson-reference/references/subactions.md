# Subactions (Effects)
Defined in `gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/ `EffectAppenderFactory.java
Subactions are the meat of card game text, the more-or-less atomic causes that can be either a cost or an effect.  Subactions occur and then are done evaluating; if they are to have an ongoing effect they must spawn a `Modifier` or `Trigger`, or else store information to `Memory` or In-Zone Data for another subaction to act on later.

## Control Flow Subactions
Subactions which are used to manipulate how other subactions are performed.

* 
Choice
    Makes `player` choose an option from `texts`, which enacts the corresponding effect from `effects`, optionally saving the chosen text in `memorize`.  Choosing the first text activates the first effect, the second text the second effect, and so on.  

    If `requires` is provided, it must provide 1 requirement per item that will 

    determine if the choice should be made available to the player at all.

    The `effects` provided can include a nested array, which will automatically be wrapped in a Multiple.
    See Pippin, Hobbit of Some Intelligence).

    * player
      default: you
    * texts [ ]
      required
    * effects [ ]
      required
    * requires [ ]
      optional
    * memorize
      optional
* CostToEffect
  A two-tier effect that must fully perform all effects listed in `cost` to perform the given `effect`, assuming that all `requires` requirements are met.
  This can often be used to get around wonky evaluations, such as a play effect that plays cards from memory (when memory clearly hasn't yet been set up in advance and cannot be truly evaluated until it is), and other such sequential prerequisites that are not true cost/effect pairs.  See Arwen's Fate).
    * cost [ ]
      optional
    * effect [ ]
      optional, but one of `cost` or `effect` must have at least 1 entry
    * requires [ ]
      optional
* DoWhile
  Executes the given `effect` array once, then `check`s the given condition.  If met, the `effect`s will be executed once more.  Continues until no longer met.
  See Death They Cried).
    * check
      required
    * effect [ ]
      required
* FilterCardsInMemory
  Accesses the cards previously stored in `memory` by a previous effect and tests whether they match the given `filter`; cards which match are stored at a new `memorizeMatching`* location and cards which do not match are stored at `memorizeNotMatching`.
    * filter
      required
    * memory
      required
    * memorizeMatching
      optional
    * memorizeNotMatching
      optional
* ForEachPlayer
  Performs an `effect` once on each player.  See Isengard Ruined).
    * effect [ ]
      required
* If
    Performs a conditional `check` of all listed requirements.  If they are all `true`, the first set of effects are executed; if `false` the second group.  Either group can be omitted if convenient.
    See Isengard Smith).
    * check [ ]
      required
    * true [ ]
      optional
    * false [ ]
      optional
* Multiple
  Executes each of the child `effects` one at a time, treating the group as a single action.  This is mostly useful for adding multiple sub-effects to other effects that do not natively support that, such as `Choice`.
  See The Palantir of Orthanc, Seventh Seeing-stone).
    * effects [ ]
      required
* RefreshCard
  Effectively turns the given card off and on again for the purposes of the game engine.  This is used by CopyCard to ensure it can properly duplicate the effects of newly-played cards, but this shouldn't have a more regular gameplay use.
    * select
      default: self
* Repeat
  Performs the actions in `effect` a certain number of `times`.
  See Restless Axe).
    * times
      required
    * effect [ ]
      required
* Optional
    Prompts the given `player` to choose whether or not they wish to invoke the given `effect` with the given `text.  `This is usually used to implement "you may" sorts of actions.
    See Foul Creation). 
    * player
      default: you
    * text
      required
    * requires [ ]
      optional
    * cost [ ]
        optional
    * effect [ ]
      required
* Preventable
    An action with a prevention clause such as "The X player may do Y to prevent this".  The given `player` is given the option to pay `cost` as prompted by `text`; if they do not or cannot, then `effect` is applied.
    If the `instead` array is provided, then the cost instead pays for the option of a (usually more lenient) action to occur `instead` of `effect`.
 See Boromir's Gauntlets) and Lying Counsel).
    * text
      required
      This should take the form of a yes-no question in the form of "Would you like to X to prevent Y?" or "Would you like to pay X to do Y instead of Z?"
    * player
      required
    * effect [ ]
      required
    * cost [ ]
        required
    * instead [ ]
      optional
* SendMessage
  Sends the given `text` into the chat log to be seen by all players, including in the replay.
  This works as a good "do nothing" effect when you are required to put an effect somewhere but don't have anything to do.
    * text
      required
      This text uses substitution as described in `Text Replacement`.

## Ongoing Subactions
These effects spawn an ongoing `Modifier`, usually with a set duration.  Unlike Modifiers themselves which apply to all cards that match a particular `Filter`, Subactions will usually utilize a `Selector` to restrict the impact to more specific cards.  
For example, "Make an Elf strength +2."  A Modifier could be created to make all Elves strength +2, but if only a certain Elf is to be boosted, then somehow that must be narrowed down and added to a memory location, and then passed in to a `ModifyStrength` Modifier with an appropriate memory filter.  The `ApplyModifyStrength` effect encapsulates both parts of this operation without needing any nesting.
Remember that Gemp will attempt to avoid giving the player useless selections; if there is 1 and only 1 valid target for a selection, that selection will happen automatically, which to the player will often look like no selection was offered at all.

* AddModifier
 Adds the provided `modifier until` the given time frame.
 It may be tempting to use this instead of the more specific effects below, but they will almost always be the simpler solution.  See Parry).
    * modifier
      required
    * until
      default: end(current)
* AddKeyword
 Applies `keyword` (+ `amount` if numeric) to `count` cards matching `select,` until the given time frame.  Optionally stores the chosen cards at the given `memorize` location.
 See The Balrog's Sword).
    * count
      How many cards will have keywords added to them.
      default: 1
    * select
    required
    * keyword
      required
  can optionally use `fromMemory()
  `can optionally include the amount as "keyword+X"
    * amount
      default 1
  for numeric keywords (damage +X)
  will override any amount in the `keyword` field
    * until
      default: end(current)
    * memorize
      optional
* AddTrigger
    Adds the described `trigger` to the game state, separate from this card, which will `optional`ly last `until` the given time frame, triggering the provided `cost`s and `effect`s given that the `requires` requirements are met.
    See Beyond All Darkness). 
    * trigger
      required
    * until
      default: end(current)
    * optional
      default: false
    * requires [ ]
      optional
    * cost [ ]
      optional
    * effect [ ]
      optional, but one of `cost` or `effect` must have at least 1
* AlterOverwhelmMultiplier
  Alters the default 2x overwhelm threshold to `multiplier` for 1 `select` card `until` the given duration expires.  Optionally stores the chosen cards at the given `memorize` location.  See Coat of Mail).
  If you are attempting to prevent overwhelming at all, see `DisableOverwhelm`.
    * select
      required
    * until
      default: end(current)
    * multiplier
      default: 3; integer only
    * memorize
    optional
* DisableArcheryTotalContribution
  Removes the ability for `count` cards matching `select` from contributing to their respective archery total count, and optionally stores the selected card at the given `memorize` location.  This only disables the contribution from the "archer" keyword, not from other sources. See Aragorn's Bow.)
    * count
      default: 1
    * select
      required
    * memorize
      optional
* DisableSkirmishAssignment
  Removes the ability for 1 `select` card from being assigned to any skirmishes `until` the given duration expires.
  See Get Back).
    * select
      required
    * until
      default: end(current)
* DisableWounds
  Removes the ability for a `select` card to take any wounds `until` the given duration expires.  The chosen card is optionally stored in the given  `memorize` location. This is for "cannot take wounds" formulations, such as on Barrow-wight Stalker).
    * select
      required
    * until
      default: end(current)
    * memorize
      optional
* DisableWoundsOver
  Removes the ability for 1 card matching `filter` to take more than X `wounds` during the given `phase`, `until` the given duration expires.  Optionally stores the chosen cards at the given `memorize` location.  See Armor).
    * select
      *(selector)
    * wounds
      default: 1; integer only
    * memorize
      optional
    * phase
      defaults to the current phase that the action was invoked.
    * until
      default: end(current)
* EnableParticipationInArcheryFireAndSkirmishes
  Permits `count` cards matching `select` to participate in both archery fire and assignments/skirmishes, `until` the given duration expires, so long as the given `requires` requirements are met. See Horn of Boromir).
  Note that this enforces the rules relating to the "unhasty" keyword, so unless this effect is on a Shadow card or Gandalf culture card, this will not affect unhasty characters.
    * select
    Note that this does not automatically restrict to the Ally card type, so be sure your filter includes this restriction yourself.
    * until
      default: end(current)
    * count
      default: 1
    * requires [ ]
      optional
* EnableParticipationInSkirmishes
  Permits `count` cards matching the given `filter` to participate in assignments/skirmishes but NOT archery fire, `until` the given duration expires.  Optionally stores the chosen cards at the given `memorize` location. See Dunlending Ravager).
  Note that this enforces the rules relating to the "unhasty" keyword, so unless this effect is on a Shadow card or Gandalf culture card, this will not affect unhasty allies.
    * select
    Note that this does not automatically restrict to the Ally card type, so be sure your filter includes this restriction yourself.
    * until
      default: end(current)
    * count
      default: 1
    * memorize
      optional
* ModifyArcheryTotal
  Alters the archery total of the given `side` by the given `amount until `the given duration expires.  Can be positive or negative.
    * amount
      required
    * side
      required
    * until
      default: end(current)
* ModifyResistance
  Causes the current player to `select count `cards to have their resistance modified by `amount` until the given duration expires.  Optionally stores the selected cards in the given `memorize` location.  If `limitPerCardThisPhase` is set, total modification will be capped.  See Fearsome Dunlending).
    * amount
      required; can be positive or negative
    * count
      default: 1
    * select
      required
    * until
      default: end(current)
    * limitPerCardThisPhase
      default: -1; integer only
      This is mutually exclusive with the `until` field; if this is set that field should be unset.  
    * memorize
      optional
* ModifySiteNumber
  Causes the current player to `select count `cards to have their resistance modified by `amount` until the given duration expires.  Optionally stores the selected cards in the given `memorize` location.  See Anborn, Skilled Huntsman).
    * amount
      required; can be positive or negative
    * count
      default: 1
    * select
      required
    * until
      default: end(current)
    * memorize
      optional
* ModifyStrength
    Causes the current player to `select count `cards to have their strength modified by `amount` until the given duration expires.  Optionally stores the selected cards in the given `memorize` location.  If `limitPerCardThisPhase` is set, total modification will be capped.  See Gimli, Son of Gloin).
    * amount
      required; can be positive or negative
    * count
      default: 1
    * select
      required
    * until
      default: end(current)
    * limitPerCardThisPhase
      default: -1; integer only
      This is mutually exclusive with the `until` field; if this is set that field should be unset.  
    * memorize
      optional
* RemoveKeyword
  Causes the current player to `select count `cards to lose the given `keyword` (optionally sourced `FromMemory`) `until` the given duration expires.  The effect is continuous and the target will be unable to gain that keyword again for the duration of the effect.  Optionally `memoriz`es the selected card.
  See You Cannot Pass!)
    * count
      default: 1
    * select
      required
    * keyword
    optional, but either this or `keywordFromMemory` must be defined
    * keywordFromMemory
      optional, but either this or `keyword` must be defined
    * until
      default: end(current)
    * memorize
      optional
* RemoveText
  Causes the current player to `select count` cards and disables their use of game text (abilities, triggers, and keywords, except for card type, race, Ring-bearer, roaming, and Ring-bound for the RB) and keywords `until` the given duration expires, optionally `memoriz`ing the chosen cards.
  See Phial of Galadriel, Star-glass).
    * count
      default: 1
    * select
      required
    * until
      default: end(current)
    * memorize
      optional
* 

## Discrete Subactions
In contrast with Ongoing Effects, these subactions are instant one-time actions, meaning that they perform an action (adding a token, moving a card to a different zone, etc) that is not continually maintained by an enforcing modifier.
The primary difference is that discrete effects can be undone by an opposite discrete effect: if I add a burden, but you remove a burden, my action has been effectively canceled out with no recourse.  In contrast, a modifier that adds fierce and a modifier that removes fierce can coexist, one canceling out the other, but if the remove-fierce modifier expires, the add-fierce modifier resumes exerting its influence on the game state.
 

* AddBurdens
 Causes `player` to add `amount` burdens to the current Ring-bearer.  See The Twilight World).
    * amount
      default: 1
    * player
      default: you
* AddThreats
  Adds `amount` threats to the current Free Peoples player, sourced from `player`.See Numenor's Pride).
    * amount
      default: 1
    * player
      default: you
* AddCultureTokens
  Adds `count culture` tokens to `select` cards.  Optionally stores the chosen cards at the given `memorize` location.
  See Garrison of Osgiliath).
    * count
      default: 1
    * culture
      required
    * select 
      required
    * memorize
      optional
* AddTwilight
  Adds the given `amount` of twilight to the twilight pool.  Optionally stores the resolved amount at the given `memorize` location.
  See Gimli, Dwarf of Erebor).
    * amount
      default: 1
      If a range is provided, the player will be required to choose which amount to add, which choice then gets used for `memorize` below.
 Don't use negative values.  Instead, use the `RemoveTwilight` effect, below.  
    * memorize
      optional
* AppendCardIdsToWhileInZone
  Accesses the given `memory` location and stores all card IDs stored there into this card's In-Zone Data.  Note that this is an append operation, so any information previously stored to will not be overwritten.  Also note that this assumes the In-zone Data is a string, so if other operations have been performed to store non-string data, this will no doubt result in a crash.
    * memory
      required
* AssignThreatWounds
  Causes threat wounds to immediately be triggered as if a character had died, forcing the Free Peoples player to assign them to their companions.
* AssignFPCharacterToSkirmish
  Makes the given `player` assign a valid `fpCharacter` against` a valid minion`, and then optionally memorizes the minion and/or FP character.  See Return to its Master).
  If `ignoreExistingAssignments` is true, this assignment will occur even if an assignment has already been made to that Free Peoples character.
    * player
    default: you
    * fpCharacter
      selector
      Note that the filter does not automatically restrict to the Companion card type, so be sure your filter includes this restriction yourself.
    * minion
      selector
      This selector is restricted to only "choose" and "self".  
  Note that the filter does not automatically restrict to the Minion card type, so be sure your filter includes this restriction yourself.
    * ignoreExistingAssignments
      default: false
    * memorizeMinion
    * memorizeFPCharacter
* CancelAllAssignments
  Cancels all currently active skirmish assignments and aborts the currently active skirmish.  See Return to its Master).
* CancelEvent
  Cancels the event that is currently being resolved.  This only makes sense to include as part of a Response or similar trigger that reacts to an event of a certain type being played.  See Relentless).
* CancelSkirmish
  Cancels the currently active skirmish if it involves a character matching `filter`, optionally only fierce skirmishes.  This will not cancel the skirmish if it involves the Ring-bearer, if in a format where RB-canceling is prohibited. See Hobbit Stealth).
    * filter
      default: any
    * involving
      default: any
    * fierceOnly
    default: false
* CancelSpecialAbility
  Cancels a currently-executing special ability. This is only coherent if invoked as part of a `UsesSpecialAbility` trigger. See Warg).
* CanPlayNextAction
  Causes the current player to take the next phase action, effectively skipping all opponents.  See Baruk Khazad).  
* ChooseActiveCards
    Makes the given `player` choose `count` cards to `select`.  The given `text` is displayed and the chosen cards are stored at the given `memorize` location.  If `hindered` is true, the selection will override default behavior to include hindered cards as selectable. See Horn of Boromir).
    Memorize is required, as the only use for this effect is to make the player choose a card that will later be acted upon in a following effect.  
    * count
      default: 1
    * player
      default: you
    * select
      default: choose(any)
    * hindered
      default: false
    * memorize
      required
    * text
      required
* ChooseACulture
  Makes the current player choose a culture from a dropdown, storing the selection to the given `memorize` location.
  See Saruman, Of Many Colours).
    * memorize
      required
* ChooseAKeyword
  Prompts the current player with `text` to choose one of the provided `keywords`, storing the result at the given `memorize` location.
  Theoretically this chooses any arbitrary string rather than specifically keywords, as the results are not validated.
 See Safe Passage).
    * memorize
      required
    * keywords [ ] 
      required
      Array of keyword strings, one entry per keyword. 
    * text
      default: Choose a keyword
* ChooseANumber
  The given `player` is prompted with the given `text` to choose a number between `from` and `to` (inclusive).  If `default`* is provided, that will be the value the form is pre-filled with. The chosen number is stored at the given `memorize` location. The values involved are dynamically evaluated.
  See Terrible and Evil).
    * player
      default: you
    * text
      required
    * from
      default: 0
    * to
      default: 1
    * default
      optional. 
    * memorize
      required
* ChooseARace
  The current player is prompted to select a race, which list is generated from all races recognized by the game.  The chosen race is stored at the given `memorize` location.  See Úlairë Nertëa, Dark Horseman).
    * memorize
      required
* ChooseArbitraryCards
  Prompts `player` with `text` to choose `count` cards matching `filter` from among the cards previously stored `fromMemory`, storing the choice in `memorize. `Can show` `any cards, usually cards from the draw deck, discard pile, or otherwise from out of play which were previously stored to memory by a previous action.  See Birthday Present).
    * fromMemory
      required
    * count
      default: 1
    * filter
      default: any
      This is only needed if the storage operation was wider than this choice's options.
    * memorize
      required
    * text
      required
    * player
      default: you
* ChooseCardsFromDiscard
  The current player is presented with the given `text` prompt and then chooses `count`* cards matching `select` from their own discard pile.  The chosen cards are stored to the given `memorize` location.  See Hugin, Emissary from Laketown).
    * count
      default: 1
    * select
    default: choose(any)
    * memorize
      required
    * text
      default: Choose cards from discard.
* ChooseCardsFromDrawDeck
  The `player` is prompted with `text` to choose `count`* cards matching `filter` from the given `deck`.  If `showAll` is true, all cards in that deck are revealed to that player, which is what's supposed to happen RAW when searching the draw deck.  The chosen cards are stored to the given `memorize` location.
  See Counsel of the Wise).
    * player
    default: you
    * deck
    default: same as player
    * count
      default: 1
    * select
    default: choose(any)
    * memorize
      required
    * text
      default: Choose cards from deck.
    * showAll
      required
* ChooseCardsFromSingleStack
  The current player is prompted with `text` to choose `count` cards matching `select` currently stacked `on` a given card, storing the chosen cards at the given `memorize` location. This will display the choices in a window, rather than requiring the player to click on a tiny sliver of a card stacked on a support area condition.  See Goblin Swarms).
    * count
      default: 1
    * select
      default: choose(any)
    * on
      selector
  default: choose(any)
    * memorize
      required
    * text
      optional
* ChooseHowManyBurdensToSpot
  Prompts the current player to choose a number between 0 and the current number of burdens on the Ring-bearer.  The number chosen is stored at the given `memorize` location.
  See Enduring Evil).
    * memorize
      required
* ChooseHowManyFPCulturesToSpot
  Prompts the current player to choose a number between 0 and the current number of Free Peoples cultures.  The number chosen is stored at the given `memorize` location.
  See Corrupt).
    * memorize
      required
    * text
      optional
* ChooseHowManyToSpot
  Prompts the current player with `text` to choose a number between 0 and the current number of cards matching `filter`.  The number chosen is stored at the given `memorize` location.  See Aragorn's Pipe).
    * filter
      required
    * memorize
      required
    * text
      default: Choose how many to spot
* ChooseHowManyThreatsToSpot
  Prompts the current player to choose a number between 0 and the current number of threats.  The number chosen is stored at the given `memorize` location.  See So Polite). 
    * memorize
      required
* ChooseHowManyTwilightTokensToSpot
  Prompts the current player to choose a number between 0 and the current number of twilight tokens.  The number chosen is stored at the given `memorize` location.
  See Under the Living Earth).
    * memorize
      required
* ChooseYesOrNo
  The given `player` is prompted with the given `text`, which should be formulated in the form of a yes/no question.  The player's response is stored at the given `memorize` location, substituting the value given in `yes` for a positive answer and the value in `no` for a negative answer.  See Cave Troll's Chain).
    * text
      required
    * player
      default: you
    * memorize
      required
    * yes
      default: yes
    * no
      default: no
* CorruptRingBearer
  Automatically corrupts the Ring-bearer, winning the game for the current Shadow player.  See The Irresistible Shadow).
* Discard
    Causes `player` to `select` and discard `count` cards from play, optionally providing an `override` to target normally inactive cards. Optionally stores the discarded cards at the given `memorize` location and any stacked cards (if any) that were on the discarded card at `memorizeStackedCards`.
    See Curse From Mordor).
    * player
    default: you
    * count
      default: 1
    * select
      required
    * override
      default: NONE
    * memorize
      optional
    * memorizeStackedCards
      optional
* DiscardBottomCardsFromDeck
  The current player discards `count` cards from the bottom of the given player's `deck`, storing the discarded cards at the given `memorize` location.  If the action is `forced`, certain in-game effects may block this from resolving.
  See Beren and Lúthien).
    * deck
      default: you
    * count
      default: 1
    * forced
      required, boolean
    * memorize
      optional
* DiscardCardAtRandomFromHand
 A player's `hand` is made to discard `count` random cards.  If not `forced`, then that player chose to make that action (which matters for some protection effects).
 See The White Arrows of Lórien).
    * hand
      default: you
    * count
      default: 1
    * forced
      required, boolean
* DiscardCardsFromDeadPile
  Causes `player` to `select` and discard `count` cards from the Free Peoples dead pile.  Optionally stores the discarded cards in `memorize`.
    * player
      default: you
    * count
      default: 1
    * select
      default: choose(any)
    * memorize
      optional
* DiscardCardsFromDrawDeck
  Causes `player` to `select` and discard `count` cards from a `deck`, optionally storing the discarded cards at the given `memorize` location.  If `showAll` is true, the contents of the deck are revealed first (RAW for interacting with the deck).  If `shuffle` is true, the deck is shuffled afterwards.
  This is normally used for situations where cards were revealed from the draw deck and then later discarded, rather than arbitrary deck search-and-discard effects. In those cases, `showAll` and `shuffle` should be disabled.
 See Questions That Need Answering).
    * player
      default: you
    * deck
      default: you
    * count
      default: 1
    * select
    default: choose(any)
    * showAll
      required, boolean
    * shuffle
      required, boolean
    * memorize
      optional
* DiscardFromHand
 Causes `player` to `select` and discard `count` cards from a `hand`.  If `forced`, some cards may protect from this action.  Optionally stores the discarded cards in `memorize`.
 See Desperate Defense of the Ring).
    * player
      default: you
    * hand
      default: you
    * count
      default: 1
    * select
      default: choose(any)
    * forced
    required,boolean
    * memorize
      optional
* DiscardStackedCards
  Causes the current player to `select` and discard `count` cards `on` valid cards`.  `Optionally stores the discarded cards at the given `memorize` location.  See More to My Liking).
    * on
      filter
  default: any
    * select
      default: choose(any)
    * count
      default: 1
    * memorize
      optional
* DiscardTopCardsFromDeck
  The current player discards `count` cards off of the top of the given `deck`, optionally storing the discarded cards in the given `memorize` location.  If the action is `forced`, certain in-game effects may block this from resolving.
  See Desperate Measures).
    * deck
      default: you
    * count
      default: 1
    * forced
      required, boolean
    * memorize
      optional
* DrawCards
  Causes `player` to draw `count` cards from the top of their draw deck.  See Delving).
    * count
      default: 1
    * player
      default: you
* EndPhase
  Immediately ends the phase, which is to say it forces both players to pass any further phase actions.  See Tol Brandir).
* ExchangeCardsInHandWithCardsInDeadPile
  Causes the current player to select `countInHand` cards from hand, then select `countInDeadPile` cards from their dead pile, and then swaps both groups. See Borne Far Away).
    * selectInHand
      required
    * selectInDeadPile
      required
    * countInHand
      default: 1
    * countInDeadPile
      default: 1
* ExchangeCardsInHandWithCardsInDiscard
  Causes the current player to select `countInHand` cards from hand, then select `countInDiscard` cards from their discard pile, and then swaps both groups. See Borne Far Away).
    * selectInHand
      required
    * selectInDiscard
      required
    * countInHand
      default: 1
    * countInDiscard
      default: 1
* ExchangeCardsInHandWithCardsStacked
  Causes the current player to select `countInHand` cards from hand, then select `countInDiscard` cards from their discard pile, and then swaps both groups. See Hall of Our Fathers).
    * selectInHand
      required
    * selectInStacked
      required
    * countInHand
      default: 1
    * countInStacked
      default: 1
* ExchangeSite
  Causes the current player to select `site1` and `site2`, which are then swapped.  Sites may be in play or controlled in a support area.  See Uruk Cavern Striker).
    * site1
      required
    * site2<span style="text-decoration:underline;">
    </span>required
* Exert
    Forces `player` to `select` and exert `count` characters the given number of `times`, optionally storing the chosen characters in the given `memorize` location.  See Gimli, Son of Gloin).
    * player
    default: you
    * count
    default: 1
    * times
    default: 1
    * select
    required
    * memorize
    optional
* Exhaust
  Causes `player` to `select` and exhaust `count` characters, which is to say they are exerted as many times as possible.  It should be noted that this is undefined behavior if used as a cost rather than an effect in Decipher formats; PC formats disallow exhausting exhausted characters as a cost in the same way that exerting 0 times is not permitted as a cost.
  See Blood of Númenor).
    * player
      default: you
    * count
      default: 1
    * select
      required
    * memorize
      optional
* Fail
  A do-nothing effect which cannot be performed, best used in situations with twisty logic that need a way to make a conditionally unplayable cost.
* GetCardsFromTopOfDeck
  Cycles through cards on the top of the deck until one is found that does not match `filter`, and then stores all except the violating card at the given `memorize` location. This does not perform any side-effects: no cards are revealed, no cards are manipulated; this is only a searching mechanism for other effects to use. See Fool of a Took!).
    * filter
      required
    * memorize
      required
* Heal
 Causes `player` to `select` and heal `count` characters the given number of `times`, optionally storing the chosen cards at the given `memorize` location.  See Celeborn, Lord of Lorien).
    * count
      default: 1
    * select
    required
    * times
      default: 1
    * player
    default: you
    * memorize
      optional
* Hinder
  Causes `player` to `select count` cards to be flipped over and hindered, optionally storing the hindered cards at the given `memorize` address.
  Hindering a card temporarily disables it and leaves it unable to be spotted, targeted, or otherwise interacted with for most game purposes.  It is flipped back up when Restored, which happens automatically during reconciliation.  This mechanic was first added by the PC for set V3.
    * count
      default: 1
    * select
    required
    * player
    default: you
    * memorize
      optional
* IncrementPerPhaseLimit
  Increments the count of how many times a given phase-limited action has been invoked by 1, with the understanding that it should not be invoked more than `limit` times.  If `perPlayer` is set, a separate value is tracked per player.
  See Orc Scimitar).
 This limit is normally automatically cleared at the end of each phase so that it tracks per-phase limits (naturally), but if `phase` is provided this can instead reset at some other time period.  Providing "regroup" for instance will for many purposes act as a "per site limit", as the reset will be delayed until the regroup phase.
 This is intended to be used in conjunction with the `PerPhaseLimit` requirement and the `CardPhaseLimit` and `CardAffectedLimitPerPhase` value functions.  It has no effect otherwise if not.
    * limit
    default: 1
    * phase
      optional
    * perPlayer
    default: false
* IncrementPerTurnLimit
  Increments the count of how many times a turn-limited action has been invoked by 1, with the understanding that it should not be invoked more than `limit` times.  This limit is automatically cleared at the end of each turn.  See Orc Chieftain).
  This is intended to be used in conjunction with the `PerPhaseLimit` requirement and the `CardPhaseLimit` and `CardAffectedLimitPerPhase` value functions.  It has no effect otherwise if not.
    * limit
    default: 1; integer only
* IncrementToil
  Manually increments the number of toil exertions performed for the currently-playing toil-enabled card.  This is only coherent when executed as part of a `BeforeToil` trigger.`
  `See Ulaire Enquea, Black Threat).
* Kill
  Causers `player` to `select` and Immediately kill `count` characters, and optionally `memorize`s the selected character.. No wounds are applied.
    * count
      default: 1
    * select
    required
    * player
      default: you
    * memorize
      optional
* LiberateSite
  Causes the next eligible controlled site to be liberated.  If `controller` is set, only a site controlled by that player is eligible (this is used for "liberate a site you control" and similar wording).  Optionally `memorize`s the liberated site.
  See That Is No Orc Horn).
    * controller
    optional
    * memorize
      optional
* LookAtHand
  The current player is given the chance to look at the given `hand`.  This hand is not revealed. This effect can be blocked by other effects which protect looking at the hand.
  See Goblin Man).
    * hand
      default: you (how useless)
* LookAtRandomCardsFromHand
 Shows `count` random cards to the performing player from `hand`, optionally storing them to the specified `memorize` location.  See The Mirror of Galadriel).
    * hand
      required
    * count
      default: 1
    * memorize
      optional
* LookAtTopCardsOfDrawDeck
  Shows `count` cards from the top of a `deck` to the performing player, optionally storing them to the specified `memorize` location. See Questions That Need Answering).
    * deck
      default: you
    * count
      default: 1
    * memorize
      optional
* MakeSelfRingBearer
  Makes the card attached to this action the Ring-bearer for the current player.  No validation is performed so be careful where you attach this.  See just about every Sam) ever printed.
* Memorize
    Stores all cards matching `filter` to the given `memory` location.
    See Gimli's Helm).
    * filter
      required
    * memory
      required
* MemorizeDiscard
  Adds all cards matching `filter` in the active player's discard pile to the given `memory` location.  Optionally pares the results down to only show `uniqueTitles` to avoid duplicates.
  See Birthday Present).
    * filter
      default: any
    * memory
      required
    * uniqueTitles
      default: false
* MemorizeInfo
  Memorizes arbitrary string `info`* into the given `memory` location. See Whatever Means).
    * info
      required
    * memory
      required
* MemorizeNumber
 Stores the given `amount` in the given `memory` slot to be used by future effects, especially the `FromMemory` value.
 See Secret Sentinels).
    * amount
      required
    * memory
      required
* MemorizeStacked
  Stores cards matching `filter` that are stacked `on` certain cards into the given `memory` location.  See Restless Axe).
    * filter
      default: any
    * on
      required; filter
    * memory
      required
* MemorizeTitle
  Retrieves a card `from` an existing memory slot and extracts just the title into a new `memory` slot.
  See Destroyed Homestead).
    * from
      required
    * memory
      required
* MemorizeTopOfDeck
  Stores the contents of the top `count` cards of the current player's deck into `memory`.`
  `See Attunement).
    * memory
      required
    * count
      default: 1
* NegateWound
  Causes a wound applied to a `select` card to not be placed.  This is <span style="text-decoration:underline;">not</span> the same thing as "preventing" a wound, and is used in places where a something else is placed "instead" of a wound.
  See most copies of The One Ring).  
    * select
      default: all(any)
* PlaceNoWoundForExert
  Causes the active player to `select` a card and place no wound token for an exertion.  See Strength of Spirit).
    * select
      default: all(any)
* PlayCardFromHand
    Causes the current player to `select` a card from hand and play it, optionally `on` a kind of card with a given twilight `discount`, optionally storing the chosen card in the given `memorize` location.
    If `removedTwilight` is set, the play eligibility check will be performed assuming that much twilight will be removed before the card will play; this is to accommodate other subactions that may not yet have processed.
 If `ignoreInDeadPile` is true, the companion played will be permitted even if a card with the same title is in the dead pile.
 If `ignoreRoamingPenalty` is true, minion roaming penalties are waived.
 If `ignorePlayability` is true, the effect will not check whether the targeted card is able to be played and prevent its use even if part of an effect, which is RAW.  Only set this flag if you are 

    checking the target in another way.

    If `extraEffects` are provided, then those effects will be executed after the card has finished playing.  This is used for example on V3 Show Them the Meaning of Haste, where the event needs to be returned to hand so it can be played multiple times.
    See Sent Back)
    * select
      required
    * on
      optional; filter
    * discount
      default: 0
      negative is a discount, positive is a tax
    * removedTwilight
      default: 0; integer only 
    * ignoreInDeadPile
      default: false
    * ignoreRoamingPenalty
      default: false
    * ignorePlayability
      default: false
    * extraEffects [ ]
      optional
    * memorize
      optional
* PlayCardFromDeadPile
  Causes the current player to `select` a card from their dead pile and play it, optionally with a given twilight `discount`, optionally storing the chosen card in the given `memorize` location.
  See Meduseld).
    * select
      required
    * discount
      default: 0
      negative is a discount, positive is a tax
    * memorize
      optional
* PlayCardFromDiscard
 Causes `player` to `select` a card from their discard pile and play it, optionally `on` a kind of card with a given twilight `discount`, optionally storing the chosen card in the given `memorize` location.
 If `optional` is true, the player will not be forced to choose a card.
 If `extraEffects` are provided, then those effects will be executed after the card has finished playing.  This is often used like "play from discard by paying X, then remove this from the game".
 If `removedTwilight` is set, the play eligibility check will be performed assuming that much twilight will be removed before the card will play; this is to accommodate other subactions that may not yet have processed.
 See Goblin Scavengers).
    * player
      default: you
    * select
      required
    * on
      optional; filter
    * discount
      default: 0
      negative is a discount, positive is a tax
    * removedTwilight
      default: 0; integer only 
    * optional
      default: false
    * extraEffects [ ]
      optional
    * memorize
      optional
* PlayCardFromDrawDeck
  Causes the current player to `select` a card from their draw pile and play it, optionally `on` a kind of card with a given twilight `discount`, optionally storing the chosen card in the given `memorize` location.
  If `mustExist` is set, then this subaction will fail if no cards matching the selector are in the given draw deck.  This is most useful for cards which play something "from draw deck or discard pile", to ensure the player is not given a false option to play a card that won't be found.
 If `shuffle` is true, the deck will be shuffled afterwards.  If `showAll` is true, all other ineligible cards will also be revealed as the player chooses.  This is how the deck should be interacted with after search effects per RAW, but some cards may have the player search the deck in another subaction and not use the searching/choosing mechanism of this subaction, in which case the shuffle/display/choice should have already been handled.
 See A Wizard Is Never Late).
    * select
      required
    * on
      optional; filter
    * discount
      default: 0
      negative is a discount, positive is a tax
    * mustExist
        default: false
    * shuffle
      required; boolean
    * showAll
      required; boolean
    * memorize
      optional
* PlayCardFromStacked
  Causes the current player to `select` a card currently stacked `on` a kind of card with a given twilight `discount`, optionally storing the chosen card in the given `memorize` location.
  If `removedTwilight` is set, the play eligibility check will be performed assuming that much twilight will be removed before the card will play; this is to accommodate other subactions that may not yet have processed.
 If the `assumePlayable` flag is set, then Gemp will assume the target card will always be playable, and it will be your responsibility to ensure that unplayable cards do not get selected.  This is most useful when using this effect to play cards from memory that were filtered by another effect, where their playability is impossible to determine anyhow.
 See Goblin Swarms).
    * select
      required
    * on
      required; filter
    * discount
      default: 0
      negative is a discount, positive is a tax
    * removedTwilight
      default: 0; integer only 
    * assumePlayable
      default: false
    * memorize
      optional
* PlayNextSite
 Plays the fellowship's next site matching `filter` (replacing opponent's site if necessary).  Optionally stores the played site into the `memorize` variable.  See Pathfinder).
    * filter
      default: any
    * memorize
      optional
* PlaySite
  Makes `player` play a site from their adventure deck matching `filter` and `block` at the given site `number` position.  Replaces an existing site if necessary.  Optionally stores the played site into the given `memorize` location.  See Get On and Get Away).
    * player
      default: you
    * block
      optional
    * filter
      default: any
    * number
      required
    * memorize
      optional
* PreventAddingAllBurdens
  Prevents any number of burdens.  This is only coherent as part of an `AboutToAddBurden` trigger.
  See Sam, Samwise the Brave).
* PreventAllWounds
  Causes the current player to `select` a card.  Until the end of the current phase, all wounds that are applied to that card are prevented.  See Keep Your Forked Tongue).
    * select
      default: choose(any)
* PreventDiscard | PreventExert | PreventHeal
  Causes the current player to `select` a card (optionally `memoriz`ing it) and preventing that card from being discarded or healed.  This is only coherent when executed during a relevant before trigger (and will have effectively no effect at all on any other kind of trigger).  See Extraordinary Resilience).
    * select
      required
    * memorize
      optional
* PreventEffect | PreventBurden | PreventTwilight
  Prevents the currently-executing effect.  This is only coherent when executed during any before trigger (and will have effectively no effect at all on any other kind of trigger).
  See Ceorl, Weary Horseman).
* PreventSpecialAbility
  Cancels the currently-executing special ability.  This is only coherent when executed during a `ExertsForSpecialAbility` trigger.
* PreventWound
  Causes the current player to `select` a card (optionally `memoriz`ing it) and preventing the currently-applying wound.  This is only coherent when executed during an  `AboutToTakeWound` trigger.  If a` CantPreventWounds `modifier is in place, this action will be prevented.
  See Dwarven Bracers).
    * select
      required
    * memorize
      optional
* PutCardsFromDeckIntoHand
    Causes the current player to `select count` cards from their draw deck and take them into hand, optionally `reveal`ing the card to their opponent.  (The reveal is required RAW when it is a specific card, but is not required when the operation is indistinct, such as "take a card into hand".)
    If taking something "from draw deck or discard pile", be sure to set the lower bound on your count range to "1" rather than "0", else the player will be allowed to select the deck option when no valid cards exist.
 If `showAll` and `shuffle` are true, the entire deck contents are revealed to the player and then shuffled afterwards, both of which are normally required RAW.  However, this effect can be sometimes used in combination with other subactions that have already handled the choosing/revealing/shuffling, in which case this action can be used to silently teleport the cards into hand.
 See Risk a Little Light).
    * count
      default: 1
    * select
      default: choose(any)
    * shuffle
      required; boolean
    * reveal
      required; boolean
    * showAll
      required; boolean
* PutCardsFromDeckOnTopOfDeck
  Causes the current player to `select count` cards from their draw deck and place them on top of their draw deck, optionally `reveal`ing the card to their opponent.  (The reveal is required RAW when it is a specific card, but is not required when the operation is indistinct, such as "look at a card and place it on top of your draw deck".)
  See Back to the Light).
    * count
      default: 1
    * select
      default: choose(any)
    * reveal
      required; boolean
* PutCardsFromDeckOnBottomOfDeck
  Causes the current player to `select count` cards from a given draw `deck` and place them on the bottom of that draw deck, optionally `reveal`ing the card to their opponent.  (The reveal is required RAW when it is a specific card, but is not required when the operation is indistinct, such as "look at a card and place it on top of your draw deck".)
  See Back to the Light).
    * deck
      default: you
    * count
      default: 1
    * select
      default: choose(any)
    * reveal
      required; boolean
* PutCardsFromDiscardIntoHand
  Causes `player` to `select count` cards from the given `discard` pile and place them in their owner's hand (this cannot be used to steal cards), optionally storing the chosen cards in the given `memorize`* location.
  See A New Light).
    * count
      default: 1
    * select
      default: choose(any)
    * player
      default: you
    * discard
      required; player
    * memorize
      optional
* PutCardsFromDiscardOnBottomOfDeck
  Causes the current player to `select count` cards from their discard pile and place them on the bottom of their draw deck.
  See Goblin Sneak)`*.
    * count
      default: 1
    * select
      default: choose(any)
* PutCardsFromDiscardOnTopOfDeck
  Causes the current player to `select count` cards from their discard pile and place them on the top of their draw deck.
  See On Your Doorstep).
    * count
      default: 1
    * select
      default: choose(any)
* PutCardsFromHandOnBottomOfDeck
  Causes `player` to `select count` cards from their hand and place them on the bottom of their draw deck, optionally `reveal`ing the card to their opponent.  (The reveal is required RAW when it is a specific card, but is not required when the operation is indistinct, such as "look at a card and place it on top of your draw deck".)
  See Gimli, Dwarf of Erebor).
    * player
      default: you
    * count
      default: 1
    * select
      default: choose(any)
    * reveal
      default: false
* PutCardsFromHandOnTopOfDeck
  Causes `player` to `select count` cards from the given `hand` and place them on the top of their owner's draw deck, optionally `reveal`ing the card to their opponent.  (The reveal is required RAW when it is a specific card, but is not required when the operation is indistinct, such as "look at a card and place it on top of your draw deck".  It's also not necessary if being done to an opponent's cards)
  See The Palantir of Orthanc).
    * player
      default: you
    * hand
        default: you
    * count
      default: 1
    * select
      default: choose(any)
    * reveal
      default: false
* PutCardsFromPlayOnBottomOfDeck
  Causes `player` to `select count` cards from play and place them on the bottom of their owner's draw deck.
  See Uruk Reserve).
    * player
      default: you
    * count
      default: 1
    * select
      default: choose(any)
* PutCardsFromPlayOnTopOfDeck
  Causes `player` to `select count` cards from play and place them on the top of their owner's draw deck.
  See Hidden Even From Her).
    * player
      default: you
    * count
      default: 1
    * select
      default: choose(any)
* PutOnRing
  Causes the current Ring-bearer to "put on" The One Ring.
  See Resistance Becomes Unbearable) and every copy of The One Ring itself, which always has a self-wearing clause. 
* PutPlayedEventIntoHand
  Causes a currently-resolving event matching `filter` to return to its owner's hand after resolving instead of the discard pile.
  If not part of the event itself, this must be part of a `Played` trigger reacting to that event.
  filter` may only be either "self", "played", or "memory(X)".  If a memory location is provided, then the system trusts that you know the given memorized card is being played (for instance, as part of an explicit PlayCardFromHand action).
  See Into Dark Tunnels) and V3 Show Them the Meaning of Haste.
    * filter
      default: self
* PutPlayedEventOnBottomOfDrawDeck
  Causes the currently-resolving event to go to the bottom of its owner's draw deck after resolving instead of the discard pile.
  If not part of the event itself, this must be part of a `Played` trigger reacting to that event.
 See Hobbitses Are Dead).
* PutPlayedEventOnTopOfDrawDeck
  Causes the currently-resolving event to go to the top of its owner's draw deck after resolving instead of the discard pile.
  If not part of the event itself, this must be part of a `Played` trigger reacting to that event.
 See Still Needed).
* PutRandomCardFromHandBeneathDrawDeck
  Causes `player` to put `count` random cards from their hand beneath their draw deck.`
  `See Knights of His House).
    * hand
      default: you
    * count
      default: 1
* PutStackedCardsIntoHand
  Causes the current player to `select count `cards currently stacked `on` certain cards into hand.`
  `See The Shards of Narsil).
    * count
      default: 1
    * select
      default: choose(any)
    * on
      default: any
* ReconcileHand
 Reconciles the hand of the given `player`.  See Under Foot).
    * player
      default: you
* ReinforceTokens
  Causes `player` to `select` a card and adds a `culture` token X `times` if one is already present, optionally `memoriz`ing the chosen card.
  If `culture` is omitted, then any culture tokens on that card will be reinforced.
 See Arod, Rohirrim Steed.
    * player
      default: you
    * culture
      optional
    * select
      default: self
    * times
      default: 1
    * memorize
      optional
* RemoveAllTokens
  Causes the current player to `select` a card and remove all tokens from it.  This removes <span style="text-decoration:underline;">all</span> tokens (wounds, burdens, culture tokens).
  See A Shadow Rises).
    * select
      default: self
* RemoveBurdens
  Causes `player` to remove `amount`* tokens from the current Ring-bearer.  See Sam, Son of Hamfast).
    * player
      default: you
    * amount
      default: 1
* RemoveCardsInDeadPileFromGame
  Causes `player` to `select` count cards in the dead pile to remove from the game, optionally `memoriz`ing the chosen card..  See Corsair Halberd).
    * count
      default: 1
    * select
      default: choose(any)
    * player
      default: you
    * memorize
      optional
* RemoveCardsInDeckFromGame
  Causes `player` to `select count `cards from a `deck` to be removed from the game.
  If `showAll` and `shuffle` are true, the entire deck contents are revealed to the player and then shuffled afterwards, both of which are normally required RAW.  However, this effect can be sometimes used in combination with other subactions that have already handled the choosing/revealing/shuffling, in which case this action can be used to silently teleport the cards into the removed-from-game pile.
 See Grond, Forged With Black Steel)
    * count
      default: 1
    * select
      default: choose(any)
    * player
      default: you
    * deck
      default: you
    * shuffle
      required; boolean
    * showAll
      required; boolean
* RemoveCardsInDiscardFromGame
  Causes `player` to `select` count cards in the dead pile to remove from the game.  See Kindreds Estranged).
    * count
      default: 1
    * select
      default: choose(any)
    * player
      default: you
* RemoveCardsInHandFromGame
  Causes `player` to `select` count cards in the dead pile to remove from the game.  See Kindreds Estranged).
    * count
      default: 1
    * select
      default: choose(any)
    * player
      default: you
    * hand
      default: you
* RemoveCharacterFromSkirmish
  Causes `player` to `select` a character in the currently-active skirmish to unassign from that skirmish, optionally `memoriz`ing the selected card.  This may end the skirmish if all characters of one side have been removed.  Naturally, this is only coherent when performed as a Skirmish-phase action.
  See Pippin, Just a Nuisance).
    * player
      default: you
    * select
      default: choose(any)
    * memorize
      optional
* RemoveCultureTokens
  Causes the current player to `select` cards which will have `count culture` tokens removed.  Optionally stores the chosen cards at the given `memorize` location.
  See Garrison of Osgiliath).
    * count
      default: 1
    * culture
      optional
    * select 
      required
    * memorize
      optional
* RemoveFromTheGame
  Causes `player` to `select` count cards from play to be removed from the game, optionally `memoriz`ing the selected card and/or cards which were stacked on it at the time of selection.
  See Our Time).
    * player
      default: you
    * count
      default: 1
    * select
      required
    * memorize
      optional
    * memorizeStackedCards
      optional
* RemovePlayedEventFromTheGame
  Causes a just-played event to be removed from the game.  `filter` must be either "self" or "played".  This is usually used within the `extraEffects` parameter for `PlayCardFromDiscard.
    * filter
      default: self
* RemoveThreats
  Removes `amount` threats from the current Free Peoples player.   See Gondorian Captain).
    * amount
      default: 1
* RemoveTokensCumulative
  Removes `count culture` tokens across cards matching `filter`.  The current player will remove tokens one at a time, meaning they can pick and choose where each token will come from until the total has been reached.
  See Dasron, Merchant From Dorwinion).
    * count
      default: 0
    * culture
      default: null (any)
    * filter
      default: any
* RemoveTwilight
  Removes the given `amount` of twilight from the twilight pool.  Optionally stores the resolved amount at the given `memorize` location.
  See The Gaffer's Pipe).
    * amount
      default: 1
      If a range is provided, the player will be required to choose which amount to remove, which choice then gets used for `memorize` below.
 Don't use negative values.  Instead, use the `AddTwilight` effect, above. 
    * memorize
      optional
* ReorderTopCardsOfDrawDeck
  Presents `player` with the top `count` cards of a `deck` and has them place each card on the top of the deck one at a time, with the last being on the very top.  See Halls of My Home).
    * count
      default: 1
    * player
      default: you
    * deck
      default: you
* ReplaceCardInPlayWithCardInDiscard
  Prompts the current player to select a card in play matching `selectInPlay` and then a card in their discard pile matching `selectInDiscard. `The card in play will then be exchanged for the one in discard, preserving all attached cards, stacked cards, and tokens, before discarding the original.  See Two Minds of It.
    * selectInplay
    required
    * selectInDiscard
    required
* ReplaceInSkirmish
  If a currently-skirmishing character matches `filter`, causes the current player to select a character to replace them `with`.
  See Counts But One). 
    * filter
      required
    * with
      required; selector
* ResetWhileInZoneData
  Deletes any data currently stored in this card's `In-Zone Data`.
* ResolveSkirmishSeparately
  Causes a Free Peoples character matching `filter` who is currently skirmishing multiple minions to resolve those skirmishes as individual sub-skirmishes.
  The sub-skirmishes will be required to resolve first before any other skirmishes may resolve.
 See V3 For My Old Gaffer.
    * filter
      required
* Restore
  Causes `player` to `select count` cards to be flipped back right-side-up and restored, optionally storing the restored cards at the given `memorize` address.
  Hindering a card temporarily disables it, and thus Restoring it causes it to resume activity on the board.  This action happens automatically during reconciliation, but can be invoked early using this entry. This mechanic was first added by the PC for set V3.
    * count
      default: 1
    * select
    required
    * player
    default: you
    * memorize
      optional
* ReturnToHand
  Causes `player` to `select count `cards to return to their owner's hands. See Grima, Chief Counselor).
    * player
      default: you
    * select
      required
    * count
      default: 1
* RevealBottomCardsOfDrawDeck
  Reveals `count` cards from the bottom of a `deck` to all players, optionally storing them in the given` memorize` entry.
  See The Underdeeps of Moria).
    * deck
      default: you
    * count
      default: 1
    * memorize
      optional
* RevealCards
    Causes the current player to `select count `cards to reveal to all players.  This is a general-purpose action that is either self-reveals a card or reveals cards chosen from a previous subaction.
    See Orc Crusher).
    * select
      default: choose(any)
    * count
      default: 1
    * memorize
      optional
* RevealCardsFromAdventureDeck
  Causes the current player to `select count `cards from the adventure deck of a given `player`.  Optionally `memorize`s the chosen cards.
  See Destroyed Homestead).
    * player
      default: you
    * select
      required
    * count
      default: 1
    * memorize
      optional
* RevealCardsFromHand
  Prompts the owner of the given `hand` to `select` and reveal `count` cards from their hand, optionally storing the chosen cards in the given `memorize` location.
  See Mallorn-trees).
    * hand
      default: you
    * select
      default: choose(any)
    * count
      default: 1
    * memorize
      optional
* RevealHand
  Reveals all cards in the given `hand` to all players, optionally `memoriz`ing them.
  See Sting).
    * hand
      default: you
    * memorize
      optional
* RevealRandomCardsFromHand
  Causes `count` cards to be revealed at random from a `hand`, optionally `memoriz`ing the revealed cards.  If `forced`, some cards may defend against this and prevent it from happening.
  See Alive and Unspoiled).
    * hand
      default: you
    * count
      default: 1
    * forced
      required; boolean
    * memorize
      optional
* RevealTopCardsOfDrawDeck
  Reveals `count` cards from the top of a `deck` to all players, optionally `memoriz`ing the revealed cards.`
  `See Halls of My Home).
    * deck
      default: you
    * count
      default: 1
    * memorize
      optional
* SetFPStrengthOverride
  Alters the current skirmish and causes the Free Peoples side to use the provided `amount` for their strength value, overriding whatever their actual total strength is.
  See One Last Surprise).
    * amount
      default: 1
* SetShadowStrengthOverride
  Alters the current skirmish and causes the Shadow side to use the provided `amount` for their strength value, overriding whatever their actual total strength is.
  See Final Triumph).
    * amount
      default: 1
* SetupExtraAssignmentAndSkirmishes
  Kicks off another set of Assignment/Skirmish phases involving all minions matching `filter.
  `See The Witch-king's Beast, Fell Creature).
    * filter
      required
* ShadowCantHaveInitiative
  Prevents Shadow from having initiative `until`* the given duration expires.
    * until
      default: end(current)
* ShuffleCardsFromDiscardIntoDrawDeck
  Causes the current player to `select` count cards from their discard pile to be shuffled back into their draw deck.
  See Bilbo, Retired Adventurer).
    * select
      default: choose(any)
    * count
      default: 1
* ShuffleCardsFromHandIntoDrawDeck
  Causes `player` to `select` count cards from hand to be shuffled into their draw deck, optionally `memoriz`ing the selection.
    * player
      default: you
    * select
      default: choose(any)
    * count
      default: 1
    * memorize
      optional
* ShuffleCardsFromPlayIntoDrawDeck
  Causes `player` to `select` count cards from play to be shuffled into their draw deck, optionally `memoriz`ing the selection.
  If `includeStacked` is true, cards stacked on the selection will also be shuffled in (and optionally memorized to the `memorizeStacked` entry).
 See Rally the Host).
    * player
      default: you
    * select
      default: choose(any)
    * count
      default: 1
    * includeStacked
    default: false
    * memorize
      optional
    * memorizeStacked
      optional
* ShuffleDeck
  Shuffles the given `deck.  `See Uncertain Paths).
    * deck
      default: you
* ShuffleHandIntoDrawDeck
  Causes the given `player` to shuffle their hand into their draw deck.  See Change of Plans).
    * player
      default: you
* Spot
    Prompts the current player with `text` to spot `count` cards.
    If `filter` is provided, this is an operation where the specifically spotted cards don't matter, such as with City Wall).
 If `select` is provided, this is an operation where the result is going to be `memorize`d for a later operation, and so that value must be passed.  See Uruk Guard).
    * select
      optional
    * filter
      optional
    * count
      default: 1
    * memorize
      required for `select`, ignored for filter
    * text
      optional
* StackCardsFromPlay
  Causes the current player to `select` count cards in play and `where` to stack them, then stacks those cards.
  See Dunlending Looter).
    * select
      default: choose(any)
    * where
      required
    * count
      default: 1
* StackCardsFromDeck
  Causes the current player to `select` count cards from their deck and `where` to stack them, then stacks those cards.
  If stacking something "from draw deck or discard pile", be sure to set the lower bound on your count range to "1" rather than "0", else the player will be allowed to select the deck option when no valid cards exist.
 If `showAll` and `shuffle` are true, the player will be presented with the full contents of their deck and it will be shuffled afterwards, which is RAW how the deck is to be interacted with in these cases. However if a previous operation selected the cards and handled that already, these can be false to just teleport the cards from the deck to the stack.
 See Dunlending Looter).
    * select
      default: choose(any)
    * where
      required
    * count
      default: 1
    * shuffle
      required
    * showAll
      required
* StackCardsFromDiscard
  Causes the current player to `select` count cards from their discard pile and `where` to stack them, then stacks those cards.
  See Courtesy of My Hall).
    * select
      default: choose(any)
    * where
      required
    * count
      default: 1
* StackCardsFromHand
  Causes the current player to `select` count cards from their hand and `where` to stack them, then stacks those cards, optionally `memoriz`ing the stacked cards.
  See From the Armory).
    * select
      default: choose(any)
    * where
      required
    * count
      default: 1
    * memorize
      optional
* StackPlayedEvent
  Causes the current player to select `where` to stack the event that was just played, then stacks that event there. This is only coherent when part of a `Played` trigger.
  See I Would Have Gone With You to the End).
    * where
      required
* StackTopCardsOfDrawDeck
  Causes the current player to select `where` to stack the top `count` cards of their `deck`, then stacks those cards.
  See Come Here Lad).
    * select
      default: choose(any)
    * where
      required
    * count
      default: 1
* StartSkirmish
  Creates an ad-hoc out-of-phase Skirmish phase.  Causes the current player to select a `FPCharacter` and minion to skirmish `against`; after the skirmish ends the normal game process will resume.  See A Dark Shape Sprang). 
    * fpCharacter
      required
    * against
      required
* StoreController
  Retrieves the card at the given `memory` location and stores that card's controller in this card's In-Zone Data.
    * memory
      required
* StoreCulture
  Converts a given `memory` string value into a culture stored in this card's In-Zone Data.
    * memory
      required
* StoreKeyword
  Converts a given `memory` string value into a keyword stored in this card's In-Zone Data.
    * memory
      required
* StoreOwner
  Retrieves the card at the given `memory` location and stores that card's owner in this card's In-Zone Data.
    * memory
      required
* StoreRaceFromCard
  Retrieves a card stored in a given `memory` and stores that card's race in this card's In-Zone Data.
    * memory
      required
* StoreWhileInZone
  Converts a given `memory` string value into a string stored in this card's In-Zone Data.
    * memory
      required
* TakeControlOfSite
  Causes `player` to take control of a site, if able. This enforces the standard rules of only taking the lowest-number unoccupied site, and will fail otherwise.
  See Desert Lancers).
    * player
      default: you
* TakeOffRing
  If the current Ring-bearer is "wearing" The One Ring, causes it to be "taken off".  See O Elbereth! Gilthoniel!)
* Transfer
  Causes the current player to `select` a card and `where` to transfer it to, optionally `memoriz`ing one or both of the `Transferred` card and the `Target` card.
  If `checkTarget` is true, then the card's own "bearer must be X" requirements and standard item class restrictions are enforced. If false, then this action will be able to transfer cards to "ineligible" bearers.
 If `fromSupport` is true, then the transferred card must be in the support area at time of transfer or the operation will fail.
 While "transferring" is an in-game mechanic (concerned only with transferring items between Free Peoples characters during the Fellowship phase), this subaction can be used to cause transfers not covered by those rules.
 See Citadel of the Stars) and Strange-looking Men).
    * select
      required
    * where
      required; selector
    * checkTarget
      default: false
    * fromSupport
      default: false
    * memorizeTransferred
      optional
    * memorizeTarget
      optional
* TransferFromDiscard
  Causes the current player to `select` a card in their discard pile and `where` to transfer it to, then transfers that card to that target.
  This bypasses the normal card play process, meaning that twilight and other costs/requirements will be ignored; the card is effectively teleported from one place to another.
 See Morgul Blade).
    * select
      required
    * where
      required; selector
* TransferToSupport
  Causes the current player to `select` a card to transfer to its owner's support area, optionally `memoriz`ing the card it was previously attached to.
  See Rally Point).
    * select
    * memorizeBearer
* TurnIntoMinion
  Causes the current player to `select` count cards to transform into minions with the given `strength`, `vitality`, and `keywords` until the given duration expires.  Optionally `memorize`s the chosen card.
  See Alatar Deceived).
    * select
      choose(any)
    * count
      default: 1
    * strength
      required
    * vitality
      required
    * keywords [ ]
      optional
      An array of strings, each entry representing a keyword
    * until
      default: end(current)
    * memorize
      optional
* Wound
 Causes `player` to `select count `characters to be wounded `times` times, optionally `memoriz`ing the chosen cards.
 See Bitter Hatred).
    * player
    default: you
    * select
      required
    * count
      default: 1
    * times
      default: 1
    * memorize
      optional
      