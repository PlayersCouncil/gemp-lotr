# Rules that depend on the format

A format mostly decides which decks may sit down at a table. A card from set 1 plays the same on a set 18 site
path. The engine does consult the format for the handful of rules below, and a card whose text touches one of
them is tested under both settings, with a `FormatRule` clause in its ledger.

The format is the fifth argument of the constructor:

```java
new VirtualTableScenario(cards, sites, ringBearer, ring, VirtualTableScenario.Fellowship);
```

`TestConstants` names four formats: `Fellowship` (`fotr_block`), `Multipath` (`multipath`, the default),
`PCMovie` (`pc_movie`) and `Shadows` (`expanded`). Any format code from `lotrFormats.hjson` can be passed as a
string.

| Rule | Setting in `lotrFormats.hjson` | On in | Off in | Tested |
|---|---|---|---|---|
| A skirmish involving the Ring-bearer can be cancelled | `cancelRingBearerSkirmish` (default false) | Fellowship, Towers and King block formats and their standards, e.g. `fotr_block`, `ttt_block`, `towers_standard`, `king_block` | `multipath`, `movie`, `expanded`, `pc_movie`, `pc_expanded` | **Per card.** Any card that says "cancel a skirmish" or "cancel that skirmish" gets a `FormatRule` clause: with the Ring-bearer in the skirmish, the cancel works under one setting and is refused under the other. |
| Which path sites come from | `sites`: `FELLOWSHIP`, `TWO_TOWERS`, `KING`, `MULTIPATH`, `SHADOWS` | | | **Per card**, where the text names a site number or a block ("site 3", "a site from the King block"). Test it on that block's path. Shadows sites have no number of their own. No `FormatRule` clause is needed unless the card behaves differently on two paths. |
| The Rule of 4 (no more than 4 cards drawn or taken into hand in the fellowship phase) | `ruleOfFour` (default true) | everything except `2nd_ed` | `2nd_ed` | As a mechanic, once. A card that draws is not retested against it. |
| The Free Peoples player wins when regroup actions are over, not when regroup begins | `winAtEndOfRegroup` (default false) | PC formats: `pc_fotr_block`, `pc_movie`, `pc_expanded` | Decipher formats | As a mechanic. Per card only for regroup text that can change the outcome at site 9. |
| Discard piles are public | `discardPileIsPublic` (default false) | PC formats | Decipher formats | As a mechanic. It changes what is shown, not what a card does. |
| Controlling 5 sites wins | `winOnControlling5Sites` (default false) | `2nd_ed` | everything else | Not tested: 2nd edition is obsolete and not to be considered (Ketura, Q-0074). |
| Each deck brings a Map, which is placed in its owner's support area before the game begins; at deck validation the Map holds the adventure deck to one site path | `usesMaps` (default false) | `pc_movie`, `test_pc_movie` | everything else | With the Map cards (V2: 102_74, 102_75, 102_76). A Map announces which site path its deck uses and does nothing else in play: "All the Map does is announce which path the player is using, and it is revealed in the player's support area before the game begins, so it is information available during bidding. Maps therefore have no ongoing in-game effect, they are purely information repositories" (Ketura, Q-0055). The restriction is read with `DeckValidationScenario`, the placement with `GetFreepsMap()` and `GetShadowMap()` on a `PCMovie` table. No other card needs a `FormatRule` clause for it. `test_pc_movie` has no V2 among its sets, so no deck is legal there (F-0091 b). |
| The mulligan | `mulliganRule` (default true) | everything | nothing at present | As a mechanic. |

The engine consults these in `CancelSkirmish` and `CancelSkirmishEffect`, the draw and take-into-hand effects,
`WinConditionRule` and `PlayerReconcilesAction`, `GameState`, and the pregame and movement processes.

Errata are applied to a deck when a table is created in the hall, not by the engine. In a test, a card is whatever
blueprint id the scenario names: `1_5` is the Decipher card and `51_5` is its PC erratum, whatever the format.

The settings above were read from `lotrFormats.hjson` and from the engine on this branch. If a card's behaviour
seems to turn on the format in a way this table does not account for, that is a question for Ketura.

The Ring-bearer rule arrived with set 9, Reflections. That the King block formats (sets 7, 8 and 10) still allow
the cancel is an oversight nobody noticed because the block is hardly played (F-0014); do not use them as the
"allows it" format in a test. Use `fotr_block` for allowed and `multipath` or `movie` for refused.
