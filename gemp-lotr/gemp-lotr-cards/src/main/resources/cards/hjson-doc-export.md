
Exported from the main document on 2026-02-23: https://docs.google.com/document/d/1s26gfFIx6olaXD8ZY1se6dymssvF-Ty6xXpJsFy_dvc/edit?tab=t.0

# HJSON Card Definitions
Data-driven JSON cards have a number of benefits over their pure Java predecessors, including:

1. Being able to be hot-reloaded at runtime without restarting the server, leading to much faster and less troublesome bug fixes
2. More approachable syntax, which lowers the technical floor for contributors
3. Because cards cannot`* define their own special snowflake logic, all functionality must be shared.  
    1. This leads to centralized repositories of processes, instead of re-implementing the wheel a dozen times across a dozen cards.
    2. This also means that card effects which affect those processes can be made to do so by a change to one place, instead of tracking down every location where the process occurs.
For all its benefits, JSON data files do have a few problems, most notably in identifying what all the hidden values are, which is buried in the code with no good way of rooting them out except by hand.  That work has been done below for your use.
As usual, the fastest way to make new cards is to pull from cards with effects similar to the new card you are creating, and this documentation is best read while referencing cards which exist that utilize these specific functions to give context to their use: see the PC github here.

# HJSON Dialect
Gemp utilizes a dialect of JSON called HJSON, for "human JSON", which adds the following useful features:

* Comments, using #, //, or /* */ as you like
* Commas are only required when necessary and are otherwise ignored
* Quotes are only required when the data within them contains { } or is only numeric
HJSON is entirely syntax sugar on top of regular JSON.  The library used converts it to regular JSON before parsing it as normal.  You can find links to syntax highlighting plugins for many common text editors here: https://hjson.github.io/users.html

# Top-level Blueprint 
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/ `LotroCardBlueprintBuilder.java
Defines the top-level fields for each card definition.  These are the fields which define a card in its entirety.
 

* title
  Name of the card
* subtitle
  Subtitle of a card, appended to the name if provided
* unique
  If the card is restricted to a single copy in play at a time.  Accepted values at this time:
    * true
    * false
* side
  Whether a card is Free Peoples or Shadow.  Usually this value is inferred from the culture, but in the case of [Gollum] this must be provided manually.  Sites and The One Ring do not define this field.
  See `Side` for a list of accepted values.
* culture
  Which culture a card is associated with, which will then set the `side` unless the culture is [Gollum].  Sites and The One Ring do not define this field.
  See `Culture` for a list of accepted values.
* type
  Which top-level card type this card is, which will influence which of the following fields will apply.
  See `CardType` for a list of accepted values.
* race
  Which race a character card is.
  See `Race` for a list of accepted values.
* itemclass
  Which item class or classes an Artifact or Possession is.
  See `Item Class` for a list of accepted values.
* keyword | keywords
  Which keyword or keywords a card is. Both plural and non plural are the same and accept an array.
  See `Keyword` for a list of accepted values.
* timeword
  For events, which phase this event can be activated in.
* twilight
  The twilight cost of the card. Must be an integer.
* strength
  The strength stat of the card.  Must be an integer.  If on an attached card, the number can be positive or negative.
* vitality
  The vitality stat of the card.  Must be an integer.  If on an attached card, the number can be positive or negative.
* resistance
  The resistance stat of the card.  Must be an integer.  If on an attached card, the number can be positive or negative.
* signet
  For companions, the signet of the card.
  See `Signet` for a list of accepted values.
* block
  For sites, the site block of the card.
  See `SitesBlock` for a list of accepted values.
* site
  For sites or minions, the site number of the card.  Must be an integer.  For sites, do not provide "1T" or similar, instead use the `block` field.
* allyhome [ ]
  The home site, only valid for allies.  As allies can have multiple home sites, this is an array of block-site pairs, like so:
  [ \
    {
            block: Fellowship
                site: 3
            }
    ] \
An abbreviated form can also be used, replacing the full object definition with a string like "3F"
* direction
  Direction of the arrow on site cards, which theoretically determines which direction to source the next site from in a multiplayer match.
  Accepted values (case-insensitive):
    * LEFT
    * RIGHT
* target
  What kinds of card this card can be attached to; this represents a "Bearer must be an X" sort of clause.
  Accepts a filter; see `Filters`.
* ~~requires~~
  This top-level entry is now deprecated.  It has been replaced with the ToPlay effect type.
* effects
  An array of card effects making up the meat of a card's game text.  See `Subactions (Effects)` as well as, well, the rest of this document.
* gametext
  The literal game text as printed on the card.  Should follow card generator rules for formatting; when in doubt just use standard HTML.
* lore
  The literal lore text as printed on the card.  Do not provide italic HTML tags, but the quotes should be provided (as standard quotes and not smart quotes; those will be generated automatically).
* promotext
  Any promotional text printed on a card's face, such as "`dAgent Exclusive`" and such.  
* alts
  Currently unused.  Will eventually house information on alternate versions, such as Tengwar, Masterworks, Promos, etc
* cardinfo
  Currently unused.  Will eventually house meta information for the card, such as collector's info, versioning, etc

# Game Text Types
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/`EffectFieldProcessor.java
Defines the different kinds of effects which can be created

* Activated
    A standard bold-text phase special ability that requires manual activation by its owner during the action procedure of that phase.  See Gandalf's Pipe).
    * phase [ ]
      An array of phases where this ability can be manually activated.  Must contain at least 1.
    * requires [ ]
      0 or more requirements that must be fulfilled for this action to be available.
    * cost [ ]
      0 or more costs that must be paid before this action's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * limitPerPhase
      default: 0
  How many times this ability may be invoked per phase.  Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerPhaseLimit` and `limitPerPhase`)
    * limitPerTurn
      default: 0
  How many times this ability may be invoked per turn. Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerTurnLimit` and `limitPerTurn`)
    * text 
      Text to be displayed in the log relating to this action when relevant.
* ActivatedFromStacked
  An action that can be activated while the card is stacked on another card (which is not activatable by default as such cards are inactive unless their text specifies otherwise).  See Dunlending Looter).
    * phase [ ]
      An array of phases where this ability can be manually activated.  Must contain at least 1.
    * requires [ ]
      0 or more requirements that must be fulfilled for this action to be available.
    * stackedOn
      default: any
      An optional filter which restricts whether this action is available based on what this card is stacked on.  e.g. "If this is stacked on a [Dwarven] condition…"
    * cost [ ]
      0 or more costs that must be paid before this action's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * limitPerPhase
      default: 0
  How many times this ability may be invoked per phase.  Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerPhaseLimit` and `limitPerPhase`)
    * limitPerTurn
      default: 0
  How many times this ability may be invoked per turn. Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerTurnLimit` and `limitPerTurn`)
    * text
      Text to be displayed in the log relating to this action when relevant.
* ActivatedInDiscard
 A manually-triggered ability that can only be activated while the card is in the discard pile.  See Gollum, Plotting Deceiver).
    * phase [ ]
      An array of phases where this ability can be manually activated.  Must contain at least 1.
    * requires [ ]
      0 or more requirements that must be fulfilled for this action to be available.
    * cost [ ]
      0 or more costs that must be paid before this action's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * limitPerPhase
      default: 0
  How many times this ability may be invoked per phase.  Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerPhaseLimit` and `limitPerPhase`)
    * limitPerTurn
      default: 0
  How many times this ability may be invoked per turn. Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerTurnLimit` and `limitPerTurn`)
    * text
      Text to be displayed in the log relating to this action when relevant.
* ActivatedInDrawDeck
 A manually-triggered ability that can only be activated while the card is in the draw deck. 
    * phase [ ]
      An array of phases where this ability can be manually activated.  Must contain at least 1.
    * requires [ ]
      0 or more requirements that must be fulfilled for this action to be available.
    * cost [ ]
      0 or more costs that must be paid before this action's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * limitPerPhase
      default: 0
  How many times this ability may be invoked per phase.  Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerPhaseLimit` and `limitPerPhase`)
    * limitPerTurn
      default: 0
  How many times this ability may be invoked per turn. Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerTurnLimit` and `limitPerTurn`)
    * text
      Text to be displayed in the log relating to this action when relevant.
* AidCost
 The Aid activation cost as used by Followers*.  See Ori, Dwarven Chronicler).
    * cost [ ]
      required
      1 or more costs which must be paid at the start of the Maneuver phase for the associated Follower to be attached to a character.
* AssignmentCost
  An optional cost which prevents the Free Peoples player from assigning the associated character from being assigned to a skirmish unless paid.  Under the hood this is a wrapper for the FreePeoplePlayerMayNotAssignCharacter modifier.
  See Morgul Spawn).
    * requires [ ]
      0 or more requirements that must be fulfilled for this cost to be necessary.
    * cost [ ]
      required
  1 or more costs which must be paid by the Free Peoples player for them to be able to assign this minion. 
    * text
      required
      The prompt given to the Free Peoples player when assignments begin.  Should take the form of a yes/no question, i.e. "Would you like to exert a companion to be able to assign X?" 
* CopyCard
 Any card which copies the game text wholesale of another card.  See Silinde, Elf of Mirkwood).
    * filter
      required
* DeckBuildingRestriction
  Alters the requirements for what is considered a valid deck. At least `min` cards, no more than `max` cards matching `filter` must be contained within the given `pile`, else the given `text` will be displayed in the deckbuilder and the deck will be disallowed within relevant formats.  Used by PC Map cards.
    * filter
      What cards to restrict.  Note that many filters are unavailable to this operation, including anything that depends upon an active game context to execute.  This effectively limits it to basic enum comparisons.
    * pile
      Which pile to validate.  If null, this will validate all piles as one group.
      See Pile Type for a list of accepted values.
    * min
      Integer value only.  The minimum required cards.  
    * max
      Integer value only.  The maximum allowed cards.  
    * text
      required.
      This text will be displayed in the deckbuilder for the user to know what rule they should be following.  The full message will be:
 Deck does not meet restrictions placed by [card name]: [`text`]. Found [# of cards matching filter]: [list of cards matching filter].
 Thus, text might be something like "Only site cards from Fellowship block", or "No more than 10 unique cards" (repeating the values of min and max if needed).
* DisplayableInformation
  A logging action that outputs the given `text` to the action log in chat.  This is particularly intended to be used with any action that has stored information in the In-Zone Data, especially `StoreKeyword`.  
    * text
      required 
      The string "{stored}" will be automatically replaced with any data stored in this card's In-Zone Data.
* Discount
 A complicated set of methods for calculating variable twilight cost self-discounts on play.  For discounting <span style="text-decoration:underline;">other</span> cards, instead use the `Play` effect.
    * max
      default: 1000 (i.e. unlimited)
      The maximum discount that can be subtracted from this card's twilight cost.
    * memorize
      The location to store the end result in.  <span style="text-decoration:underline;">Which</span> end result varies depending on function.  This value will also get stored in this card's In-Zone Data. See below for specifics.
    * discount
    One nested object of type:
        * PerDiscardFromHand
          Discounts the card -1 per card discarded from hand which matches `filter`.
          Stores the total number of discarded cards (pre-multiplier) to `memory`/IZD.
            * filter
              default: any
        * PerExert
        For more complicated versions of toil; any number of characters matching `filter` may be exerted to discount by the given `multiplier`.  See Sauron, Dark Lord of Mordor).  Stores the total number of exerted minions to `memory`/IZD.
            * multiplier
              required.`* * 
            * filter
              default: any
              Which cards may be exerted.
        * PerThreatRemoved
        Discounts the card -1 per threat removed.  See Host of Udun).  Stores the total number of removed threats to `memory`/IZD.
        * IfDiscardFromPlay
        All-or-nothing, either `count` cards matching `filter` are discarded from play, or the `max` discount does not apply at all.  See Mountain-troll).
 Stores "yes" to `memory`/IZD if discard threshold was met, else "no".
            * filter
              default: any
            * count
              required.`* * 
        * IfRemoveFromDiscard
          If `count` cards matching `filter` are removed from the game from the discard pile, then the `max` discount is applied.  See Cunningly Hidden).
          Stores "yes" to `memory`/IZD if removal threshold was met, else "no".
            * count
              required. 
            * filter
              default: any
* Event
 Used specifically for Event cards.  This should be the effect triggered when the event is played.  If a response event, do not use this: see `ResponseEvent`.
    * requires [ ]
      0 or more requirements that must be fulfilled for this event to be playable.
    * cost [ ]
      0 or more costs that must be paid before this event's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * requiresRanger
      default: false
      Whether or not this event requires a Ranger.
  See Uruk Scout), Orc Scout), and Wrath of Harad)
* ExertTargetExtraCost
  Used for cards that exert their to-play target.  See Windows in a Stone Wall).  
* ExtraCost
 Used for costs to play a card, such as "To play, exert a Hobbit."  
    * requires [ ]
      0 or more requirements that must be fulfilled for this event to be playable.
    * cost [ ]
      1 or more costs which must be paid for this card to be played. 
    * skipValidate
      default: false
      For 'costs' that are optional and so shouldn't actually impact the card's ability to be played.  See the threats added on Plotting).
* ExtraPossessionClass
 Used for cards that have "May be borne in addition to one other hand weapon."  See Knife of the Galadhrim).
    * attachedTo
      default: any
  If set, the extra-attachment effect will only be active if bearer matches this filter.
* InHandTrigger
 Used when cards have triggers that can activate while otherwise dormant in hand.  See The Witch-king, Black Lord).
    * trigger [ ]
      1 or more after-triggers 
    * requires [ ]
      0 or more requirements that must be fulfilled for this trigger to be activatable.
    * cost [ ]
      0 or more costs that must be paid before this trigger's effects may be executed.
    * effect [ ] 
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
* KilledTrigger
 Used for cards that self-react to being killed.  See Freca, Hungry Savage).
    * optional
      default: false
    * requires [ ]
      0 or more requirements that must be fulfilled for this trigger to be activatable.
    * cost [ ]
      0 or more costs that must be paid before this trigger's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
* Modifier
    Used for abilities that are passive and "always-on", such as "X are Y", or "Dwarves are `damage+1.`"*  See the `Modifiers` section.
    * modifier
      required
* ModifierInDiscard
  Used for `Modifiers` only active while this card is in the discard pile.  See Softly Up Behind).
    * modifier
      required
* ModifyOwnCost
 Used by any card with text like "When X, this card is twilight cost -1".  See Elite Rider)
    * amount
      required.
      How much to subtract from this card's usual twilight cost.
    * requires [ ]
      0 or more requirements that must be fulfilled for this discount to be applied.
    * on
      filter
  Used for attachable cards that are only discounted when played on certain targets.  See Asfaloth).
* PermanentSiteModifier
  Used by sites specifically to contain permanently-on modifiers that are not constrained by requiring the Fellowship to first arrive at it.  See the PC version of Doorway to Doom.
    * modifier
      required
* PlayedInOtherPhase
 Used for any permanent card (usually Rohan) which can be played outside the normal Fellowship/Shadow phases. See Erkenbrand, Master of Westfold).
    * phase
      required.  Only 1 phase per clause.
    * requires [ ]
      0 or more requirements that must be fulfilled for the out-of-phase play to be available.
    * cost [ ]
      0 or more costs that must be paid before this out-of-phase play may be executed.
* Response
 Used for Response: abilities on permanents (for events, see `ResponseEvent`, below).
    * trigger [ ]
      required.
      If more than 1 is set, all triggers will duplicate the defined costs, effects, etc.
    * requires [ ]
      0 or more requirements that must be fulfilled for this trigger to activate.
    * cost [ ]
      0 or more costs that must be paid before this trigger may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * text
      optional
    * limitPerPhase
      default: 0
      How many times this ability may be invoked per phase.  Note that this is a cap on the number of discrete usages, not a numeric limit on any cumulative effects (for those, see `incrementPerPhaseLimit` and `limitPerPhase`)
    * phase
      optional.  Only needed if `limitPerPhase` is set.
* ResponseEvent
 Used by Response events.  For normal phase events, see `Event`.
    * trigger [ ]
      required
      If more than 1 is set, all triggers will duplicate the defined costs, effects, etc.
    * requires [ ]
      0 or more requirements that must be fulfilled for this trigger to activate.
    * cost [ ]
      0 or more costs that must be paid before this trigger may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * text
      optional
* SelfRemovedFromPlayTrigger
 Triggers when this card is going to be removed from play by a card matching `source` using a given `method`. This is equivalent to the Discarded/Hindered trigger, except that it works properly for tracking removal of the self.  For tracking any other card's discard, use that trigger instead.
    * optional
      default: false
      Remember that required triggers activate before optional triggers.
    * source
      default: any
    * method
      *"discarded" or "hindered"; if not provided that means "both"
    * requires [ ]
      0 or more requirements that must be fulfilled for this action to trigger.
    * cost [ ]
      0 or more costs that must be paid before this action's effects may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
* StackedOnModifier
  Affects the game with the given `modifier` so long as the current card is stacked `on` a card matching the given filter.  See Toss Me).
    * modifier
      required
    * on
      required.  Filter.
* Trigger
    Any non-response non-special-ability triggered action (optional or otherwise), such as "Each time you win a skirmish..."
    * trigger [ ]
      required
      If more than 1 is set, all triggers will duplicate the defined costs, effects, etc.
    * optional
      required
    * requires [ ]
      0 or more requirements that must be fulfilled for this trigger to activate.
    * cost [ ]
      0 or more costs that must be paid before this trigger may be executed.
    * effect [ ]
      0 or more effects that will be executed after all requirements are met and costs are paid.  There must be at least 1 subaction, whether it goes in the `cost` or `effect` bucket doesn't matter.
    * player
      optional.  If set, the given player is set as the source of this action.
    * limitPerTurn
      default: 0
    * limitPerPhase
      default: 0
    * phase
      optional.  Only needed if `limitPerPhase` is set.
    * text
      optional
* ToPlay
  Requirements that must be met for this card to be played.
    * requires
      required


# Duration Resolution
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/appender/resolver/`TimeResolver.java
Controls how long an ongoing modifier is supposed to last.  Only used for modifiers added by effects such as `AddModifier`.

* start(phase`*)
* end(phase`*)
  Can also use the shorthand "`end(current)`" (which most of the time is redundant as that is the default duration for temporary effects).
* EndOfTurn
  Such effects will expire once game roles swap between Free Peoples and Shadow.
* EndOfGame | Permanent
  Such effects will never expire (however if they are attached to Free Peoples or Shadows cards, they may temporarily go inactive when turns rotate).


# Player Resolution
Defined in /`gemp-lotr-logic`/src/main/java/com/gempukku/lotro/cards/build/field/effect/appender/resolver/`PlayerResolver.java
If-else flow for aliases of various player filters.  In all cases, these are aliases used to look up a specific player's name (i.e. "ketura" or "xXLeetmanXx"), which is the actual string value that is saved into memory locations.

* FP | `Freeps` | `Free Peoples `| `Free People
  Resolves to the current Free Peoples player.
* FromMemory(X)
  Pulls the player stored at the X memory location, probably from a `ChooseOpponent` effect.
    * [0]: memory
* Owner
  Resolves to the owner of the card this action is attached to.
* OwnerFromMemory(X)
  Refers to the referenced X memory location and gets the owner of the saved card.  This will fail if multiple cards are saved at that location, or if zero cards were saved, or if something other than a card is saved there.
    * [0]: memory
* S | `Shadow` | ShadowPlayer
  Resolves to the first opponent, i.e. the only Shadow player in a 1v1.
* You
  Resolves to the player performing the current action.  This may or may not be the owner of the card; if a card forces another player to perform an action, then "you" in that context is that forced player.`


# Enum Resolution
Various enums are used throughout the game system.  In general, enum values are parsed in a way that is case-insensitive and that replaces spaces and hyphens with underscores.
Note that these will need to be altered in the server code for new values to be supported; this may be surprising for certain cases such as unloaded keywords or race.

## CardType

* Official:
    * Characters:
        * ALLY
        * COMPANION
        * MINION
    * Items:
        * ARTIFACT
        * POSSESSION
    * Other:
        * THE ONE RING
        * SITE
        * CONDITION
        * EVENT
        * FOLLOWER
* Unofficial:
    * Player's Council:
        * MAP
          Used to restrict deckbuilding
    * Other
        * ADVENTURE
          Unused

## Culture
If updating this enum, be sure to update `Token` as well.
 

* Decipher Free Peoples cultures`:
    * DWARVEN
    * ELVEN
    * GANDALF
    * GOLLUM
    * GONDOR
    * ROHAN
    * SHIRE
* Decipher Shadow cultures
    * DUNLAND
    * ISENGARD
    * MEN
    * MORIA
    * ORC
    * RAIDER
    * RINGWRAITH
    Alias of WRAITH
    * SAURON
    * URUK_HAI
    * WRAITH
* Hobbit Draft Game Free Peoples cultures:
    * ESGAROTH
* Hobbit Draft Game Shadow cultures:
    * GUNDABAD
    * MIRKWOOD
    * SMAUG
    * SPIDER
    * TROLL
* Unused (?):
    * FALLEN_REALMS

## Keyword

* Helper keywords:
    * SUPPORT AREA
    * ROAMING
    * MOUNTED
    * HINDERED
* Numeric keywords:
 In most cases these can be listed with their numeric portion after a + or a space, i.e. "damage+1" or "toil 4"
    * DAMAGE
    * DEFENDER
    * AMBUSH
    * HUNTER
    * TOIL
* Loaded keywords:
    * ARCHER
    * CAN START WITH RING
      This is used to mark Alternate Ring-bearers, and may be replaced with an alternate mechanism in the future.
    * ENDURING
    * FIERCE
    * LURKER
    * MOUNTED
    * MUSTER
    * UNHASTY
* Site unloaded keywords:
    * BATTLEGROUND
    * DWELLING
    * FOREST
    * MARSH
    * MOUNTAIN
    * PLAINS
    * RIVER
    * SANCTUARY
    * UNDERGROUND
* Unloaded keywords:
    * BESIEGER
    * CORSAIR
    * EASTERLING
    * ENGINE
    * FORTIFICATION
    * KNIGHT
    * MACHINE
    * PIPEWEED
    * RANGER
    * RING-BOUND
    * SEARCH
    * SOUTHRON
    * SPELL
    * STEALTH
    * TALE
    * TENTACLE
    * TRACKER
    * TWILIGHT
    * VALIANT
    * VILLAGER
    * WARG RIDER
    * WEATHER
* Obsolete 2nd edition location keywords:
    * BREE
    * EDORAS
    * LOTHLORIEN
    * RIVENDELL
    * SHIRE
* Hobbit Draft Game keywords:
    * CUNNING
    * WISE
    * BURGLAR
* PC keywords:
    * CONCEALED
      Unused
    * EXPOSED
      Unused
    * RELENTLESS

## Kill Cause
Represents how someone was killed.

* WOUNDS
  Either wounds from losing a skirmish normally or direct wounds from any source, including threats.
* OVERWHELM
* CARD_EFFECT
  Effects that specify "kill a companion" directly

## Phase
Represents not just gameplay phases but also bookkeeping phases that players don't normally track.

* Gameplay phases:
    * FELLOWSHIP
    * SHADOW
    * MANEUVER
    * ARCHERY
    * ASSIGNMENT
    * SKIRMISH
    * REGROUP
* Bookkeeping phases:
 It should go without saying, but don't reference these phases in card definitions; these are here for reference only.
    * PUT RING-BEARER
      Consists of the bid and everything up until starting fellowships are played.
    * PLAY STARTING FELLOWSHIP
    * BETWEEN TURNS
      When the Free Peoples player chooses to stay, that regroup phase will cycle to `BETWEEN TURNS`, during which roles will rotate and cards will cycle between active/inactive.

## Pile Type
Represents the physical stacks that cards are brought in at the start of the game.  This is used by deck validation and not gameplay; for the in-game equivalents (and more) see Zone.

* FREE PEOPLES
  One-half of the Draw Deck.
* SHADOW
  The other half of the Draw Deck
* ADVENTURE
* RING-BEARER
* RING
* MAP

## Possession Class (aka* Item Class)
For most gameplay purposes these are the same as unloaded keywords, but they are stored and used in card definitions separately.
 

* Official:
    * ARMOR
    * BOX
    * BRACERS
    * BROOCH
    * CLASSLESS
    * CLOAK
    * GAUNTLETS
    * HAND WEAPON
    * HELM
    * HORN
    * MOUNT
    * PALANTIR
    * PHIAL
    * PIPE
    * RANGED WEAPON
    * RING
    * SHIELD
    * STAFF
* PC:
    * PONY

## Race

* Decipher:
    * BALROG
    * CREATURE
    * DWARF
    * ELF
    * ENT
    * HALF TROLL
    * HOBBIT
    * MAIA
    * MAN
    * NAZGUL
    * ORC
    * SPIDER
    * TREE
    * TROLL
    * URUK HAI
    * WIZARD
    * WRAITH
* Hobbit Draft Game:
    * BIRD
    * DRAGON
    * EAGLE
    * GIANT
    * GOBLIN
* PC:
    * CROW
    * WARG

## Side

* Freeps | `Free Peoples `| `Free People
* Shadow

## Signet

* ARAGORN
* FRODO
* GANDALF
* THEODEN
  The accented version of "Théoden" is also supported

## SitesBlock

* FELLOWSHIP
* TOWERS | TWO TOWERS
* KING
* SHADOWS | SHADOWS AND ONWARDS
* MULTIPATH
  Previously known as "Special".  Only valid when referenced in formats, as otherwise sites are one of the above blocks.
* SECOND ED | 2ND EDITION
* HOBBIT

## Timeword
Used by Events.

* FELLOWSHIP
* SHADOW
* MANEUVER
* ARCHERY
* ASSIGNMENT
* SKIRMISH
* REGROUP
* RESPONSE

## Token
Represents physical tokens, including Culture tokens

* BURDEN
* WOUND
* Every value contained in Culture

## Twilight Cause
Represents the method used to add twilight to the twilight pool.

* AMBUSH
  Added from an instance of the Ambush keyword
* CARD_EFFECT
  Effects that added twilight directly, as either a cost or effect
* CARD_PLAY
  The twilight added as the base play cost. NOTE: this is currently unimplemented
* MOVE
  Movement twilight, which is Site Shadow number + companion count combined

## Zone
Represents a combination of physical location on the game board and activity state of the card.  These do not line up 1:1 with what is shown on the game screen; for example there is only one "Support Area", which the game automatically knows to filter based on who owns each card, and there is only one "Attached" zone that all attached cards reside in, but they are visually rendered next to the card they are attached to (whatever zone that may be).

* Public knowledge, in play:
    * FREE CHARACTERS
    * SHADOW CHARACTERS
    * ADVENTURE PATH
    * ATTACHED
    * SUPPORT
* Public knowledge, not in play:
    * DEAD
    * STACKED
    * REMOVED
      As in, "removed from the game"
* Private knowledge, not in play:
    * ADVENTURE DECK
    * HAND
    * DISCARD
      But note that formats can have a rule toggled that makes this zone public knowledge, which is enabled for all PC formats.
* Nobody sees
    * DECK
    * VOID
      Where Events live for the duration of their lifetime before being discarded
    * VOID FROM HAND
      Where all permanent cards temporarily live while their costs are being paid; if that is interrupted they will be discarded immediately.

# Text Replacement
Most text presented to the user is presented as-is, but there are some contexts where limited replacement is performed to make the information dynamic.  (This occurs if the text is ran through `GameUtils.substituteText`, and is a fairly simple drop-in replacement if you're looking to add that functionality to any text output operation.)
Text substitutions are signaled by wrapping an `{identifier}` in curly braces.  That identifier will then be tested against several possible values for substitution as follows.  The order is important; any higher level match will cause lower level checks to be skipped.  This means that you should probably avoid using any of the following terms as variable names in `memory` operations, lest they be undisplayable.

        * Any `culture` name
    Substituted with that culture's symbol.
        * you
    Substituted with the performing player's name, or else "nobody" if the action is automatic.
        * owner
    Substituted with the currently acting card's owner's name, or else "nobody" if the action is not tied to a card.
        * opponent
          Substituted with the name of the current action's card's owner's first opponent's name.
        * freeps | free peoples
          Substituted with the current Free Peoples player's name.
        * shadow
          Substituted with the first Shadow player's name.
        * self
          Substituted with a clickable Title, Subtitle representation of the current action's card, or else "none" if it is not tied to a card.
        * A `memory` entry
        First, the memory entry will be evaluated assuming it contains a list of one or more cards.  The substitution will be a comma-separated clickable Title, Subtitle link of all cards in the list.
 If the memory entry is not card data, it will attempt to be substituted as string data.  If this causes an error, NONE will be displayed.
        * Finally, if no valid substitution was found, it will be replaced with the word NOTHING.  

# In-Zone Data
All cards contain an object buffer referred to as In-Zone Data which can be used as a flexible storage solution by card effects to track information that should persist over the course of the card's in-play lifetime, even across turns.  The idea is that the data is not reset until the card changes "zones", which is to say goes from hand to in play, or from in play to stacked or the discard pile, etc.  Technically this data can be any object, but for the purposes of most Json operations, this information should be treated as a string buffer.
Unlike memory, the data in this zone is not named, and it is up to the programmer to not store multiple different kinds of data.
Examples: 

* Úlairë Toldëa, Dark Shadow) uses this to store which companion it has already been assigned to.
* Úlairë Nertëa, Dark Horseman) stores the named race to In-zone Data.
Here are all the action operations which use In-zone Data:
    

* Game Text Type
    * DisplayableInformation
    * Discount
* Values
    * WhileInZone
* Filters
    * Race(stored)
    * StoredCulture
    * StoredKeyword
    * StoredTitle
* Requirements
    * HasInZoneData
* Subactions
    * AppendCardIdsToWhileInZone
    * ResetWhileInZoneData
    * StoreCulture
    * StoreKeyword
    * StoreRaceFromCard
    * StoreWhileInZone

# Memorization and Memory
Many operations have the option of storing a result into a provided memory location, which is essentially a user-defined variable name that is only stored for the duration of the action's execution.  Data stored in this way is temporary; you can use it to transfer information between subactions, but not to track long-term information.  
(Most long-term operations involve spawning a Modifier or Trigger keyed to a particular card, but if those are unsuitable, see In-Zone Data for a way to store arbitrary information over the entire course of a card's lifetime.)
Once data has been memorized, it can be referenced by one of several operations, particularly in filters and values.

## Storing Memory

* Triggers
 *(Many triggers have the ability to store information about the triggering source into a given memory slot, but no triggers exist which are <span style="text-decoration:underline;">about</span> memory manipulation.)
* Subactions
    *(Damn near every Subaction that involves the player making a selection has a parameter for storing the choice made, but the below are dedicated to memorization.)
    * Choice
    * Memorize
    * MemorizeDiscard
    * MemorizeInfo
    * MemorizeNumber
    * MemorizeStacked
    * MemorizeTitle
    * MemorizeTopOfDeck

## Retrieving Memory

* Players
    * FromMemory
    * OwnerFromMemory
* Values
    * memory(X)
    * FromMemory
    * ForEachInMemory
    * ForEachMatchingInMemory
    * ForEachCultureInMemory
    * ForEachKeywordOnCardInMemory
    * PrintedStrengthFromMemory
    * SiteNumberInMemory
    * TwilightCostInMemory
* Filters
    * Memory
    * CardTypeFromMemory
    * CultureFromMemory
    * NameFromMemory
    * PrintedTwilightCostFromMemory
    * Race(memory)
    * MaxTwilight(memory)
    * MinTwilight(memory)
* Selectors
    * Memory
* Requirements
    * HasCardInMemory
    * HasMemory
    * MemoryIs
    * MemoryLike
    * MemoryMatches
* Subactions
  *(Most subactions have a filter or selector, both of which have the ability to filter on data stored in memory.  The below are more centrally concerned with memory contents.)
    * AppendCardIdsToWhileInZone
    * ChooseArbitraryCards
    * FilterCardsInMemory

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
    Matches all cards (mostly if not entirely events) with the given `timeword` (i.e. a phase or Response).  
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

* If `override` is not specified, the default behavior applies (only active cards are considered).
* Not all effects support `override`. Check the specific effect documentation for availability.

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
      