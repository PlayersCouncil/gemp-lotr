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

