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

