---
name: test-generation
description: >
  The Card Test Standard for Gemp-LotR: what a card's unit tests must cover and how the test file is laid out, so
  that every card is tested the same way and the tests can be checked against the card's English text and the
  rules. Use this skill when writing, reviewing or auditing any Card_*_Tests file, when deciding what a card needs
  tested, when a test disagrees with the game, or when converting old tests. Use the gemp-testing skill alongside
  it for how to drive the rig.
---

# The Card Test Standard

## What a card's tests are for

A card's tests say what the card does, in a form the game can be held to. They are worked out from **the card's
English text and the rules**, and then the game is run against them. The HJSON definition is the thing under test,
so it is never where an expectation comes from: a test that was written by reading the `effects` block proves only
that the definition does what the definition says.

That makes a disagreement between a test and the game a finding, not a broken test. When the game does something
the text does not say, the test keeps saying what the text says, and the disagreement is recorded and handed up
(see "When something is in the way").

### Where an expectation may come from, in order

1. **Ketura's rulings**, recorded in `card-test-campaign/references/rulings-log.md`.
2. **The rules.** Comprehensive Rules 4.2 is Decipher's last and holds the individual card rulings (Section
   Three) and the glossary. The Comprehensive Rules 5.0 excerpts are the Players' Council's and cover what the
   PC added or changed (hinder, restore, relentless, items, multi-dot uniqueness, when the game ends); where the
   two differ, 5.0 is in force. Look things up with `card-test-campaign/scripts/rules.py`, which searches both;
   do not rely on remembering either document.
3. **The card's text**, as the generator sheets have it: the reviewed spreadsheet for a Decipher card, the V-set
   sheets and the master errata sheet for a PC card, matched to the definition by revision number.
   `packet.py <id> --blind` prints the sheet row and the definition's own printed fields and game text, and
   nothing of how the card is implemented.

What you remember about the game is not on the list. Where the text and rules leave a real doubt about what
should happen, that is a question for Ketura, and asking is always better than guessing: obscure rulings are the
commonest reason a correct implementation gets reported as a bug.

**When the sheet and the definition disagree.** For a Decipher card the sheet is the reviewed source, and a
difference is a finding unless a ruling accounts for it (check `packet.py`'s rulings section first: a reprint
ruled to keep an earlier card's signet looks exactly like a wrong signet). For a PC card the master errata sheet
is only about 95% accurate, because some of its rows are errata that were proposed and never released: write the
tests to the definition's own game text, and report the difference as a flag, not as an error. Where what differs
is a printed field and the definition looks to be the slip (a title's capitals, 69_24), the stats test still
asserts the definition's value and the difference is a finding with no test parked on it: the lander who corrects
the definition changes the stats test's line and its header with it. Before flagging,
look at which sheet row the packet matched and at the rulings log's "Revisions" entry: a V-set sheet row that is
simply an earlier revision, where an errata row matches the definition, is no discrepancy. `Sources:` then cites
the errata row, and a revision number that does not line up is F-0032, already known.

**Deliberate departures.** A few things the game does are knowingly not what the text or rules say, for the
sake of how it plays: Goblin Runner adds its (2) without asking, separate instances of ambush are offered as
one, an X of zero that would do nothing is not offered, and a card that plays something "from your draw deck or
discard pile" is not offered when neither pile has anything it could play. The rulings log lists them. Something of the same kind that is not on the list is a question, not a finding.

## The file

One class per card, in the folder and under the name it already has. The class has one or more
`Get...Scenario()` methods, a stats test, and behaviour tests. Nothing else: no base class, no helper methods, no
fields, and none of the campaign's bookkeeping. What a reader finds is test names that say what happens, the
card's printed text in the stats test's header, and comments where a number or a choice of helper card needs
explaining.
A method that only returns a site or card map for several scenarios to share is a helper method too (lint
refuses it): write the map into each `Get...Scenario()`.

```java
public class Card_01_040_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "1_40");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, home site 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home site 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 1
		 * Name: Elrond, Lord of Rivendell
		 * ... every printed field, then the game text ...
		 */
		...
	}

	@Test
	public void FellowshipActionExertsElrondToDrawACardUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(elrond);
		scn.FreepsUseCardAction(elrond);
		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
```

The whole of this file is `cards/official/set01/Card_01_040_Tests.java`, and is the worked example of the standard.

### The ledger

The ledger is the card's text cut into clauses, each with a type and the checks it calls for, and under each
clause the tests that claim those checks. It is working material: how an author plans a card, how a reviewer
sees what was understood and what was tested, and what the conformance check holds the tests to. **It is not
part of the test file.** It lives in a file of its own beside the campaign's other bookkeeping, at the path
`packet.py <id>` prints (`<campaign state>/ledgers/<set>/<id>.txt`). Ketura does not want it in the finished
tests: no ledger comment, and no clause tags above the tests.

```
LEDGER  1_40  Elrond, Lord of Rivendell
Sources: sheet; neither rules document names this card for anything that bears on its tests

C1 ToPlay     [+ - B]      To play, spot Gandalf or an Elf.
     + - B  ->  CannotBePlayedUntilGandalfIsSpotted
     + B    ->  CannotBePlayedUntilAnElfIsSpotted
C2 Trigger    [+ F R]      At the start of each of your turns, heal every ally whose home is site 3.
     + F  ->  StartOfTurnHealsEveryAllyWhoseHomeIsSite3AndNoOthers
     R    ->  HealsAtTheStartOfItsOwnersTurnsAndNotTheOpponents
     no -: the only turns that are not yours are ones on which this card is not active
C3 Activated  [+ - C C-]   Fellowship: Exert Elrond to draw a card.
     + - C C-  ->  FellowshipActionExertsElrondToDrawACardUntilHeIsExhausted

Notes: Elrond is himself an ally whose home is site 3, so C2 heals him too.
```

- **`C` clauses are the game text, word for word, in order.** Cut at sentences and line breaks; a conditional
  effect in parentheses that is game text ("(and heal a companion if you can spot 3 rings)") may be cut off as an
  `Effect` clause of its own, since it has checks of its own. A sentence whose "and" joins two predicates of
  different types ("This minion may not bear possessions and is discarded if not at a marsh") is cut at the
  "and": left whole, a claim for either half satisfies the conformance check for both. So is one whose "and"
  joins two predicates of ONE type that are shown on different boards ("Saruman may not take wounds during the
  archery phase and may not be assigned to a skirmish": two Restrictions, 53_68, 3_69); one condition with two
  results ("is strength +3 and fierce", read together in every test) stays whole. A sentence that is a choice
  between two branches ("Make an Uruk-hai strength +2, or spot 5 companions to make ...", 1_139) may be cut at
  the comma before "or", the choice's `B` listed on the first. Every word of the
  game text is in exactly one `C` clause, keyword lines included; reminder text in brackets is left out (a
  `<helper>(...)</helper>` span is such reminder text, and quoted in a clause it fails the conformance check). The
  conformance check joins the `C` clauses and compares them with the definition's game text.
- **A card's play has no clause of its own** (Q-0279: B, engine pass 31). The printed cost, type and site number
  are the stats test's, and what they do in a plain play is shown once, in `mechanics/costs/TwilightPoolTests` and
  `WhereACardIsPlayedTests`: no test only plays a card. A card's play is tested where its own text bears on it,
  by the test of that clause, which plays the card from hand by legal play and reads what it cost and where it
  went: a bearer (`PlayOn`), a requirement or an extra cost to play (`ToPlay`, `ExtraCost`), a cost its text
  changes or speaks of, toil, a text about when it may be played, a card that answers its own play or its being
  in play. lint's `plain-play` reads the definition and the text for these (`gen_play.py`'s reading, which is
  cautious) and asks for a test that plays the card where it finds one and none does; where what it names is not
  a clause of the card's own play (a text about ANOTHER card's play), the card keeps a test that plays it, which
  claims a `P1 Play [+]` clause: the seven cards of `orchestrator/engine-pass-28/tools-files/plainplay-keep.tsv`
  (Silinde 1_60, Uruviel 1_67, The Witch-king 52_85, Scouring of the Shire 68_112, Mantle of the White Wizard
  103_3, Hailing from the Far South 103_52, Ominous Sky 103_97) and no others.
- **No reading of a printed cost or bonus doing its ordinary work** (Q-0279: E2, engine pass 31). A test does
  not read the pool across a card's play to show its printed cost paid, nor a bearer's strength, vitality or
  resistance to show a borne card's printed bonus (or the bearer just before it comes or after it goes), where
  nothing changes the one or the other: that is `mechanics/costs/TwilightPoolTests`' and `BorneItemsTests`'. It reads
  them where the card's own text, or another card on the table, changes the cost, the pool, the bonus or where
  the card goes, the nearest miss of that text included, and where something else moves the same number in the
  same stretch (a move, another play, a wound). The landed tests were stripped to this by engine pass 31. A card
  played from a pile by another card's effect is held there too (`TwilightPoolTests`,
  `AMinionPlayedFromTheDiscardPileRemovesItsCostAndTheRoamingPenalty`): the pool after it is not read; one short
  of it, where the play is a cost, is (1_196). So is a card played from hand as the cost of another card's ability (Grond
  8_103's [sauron] minion): the pool after it is not read, one short of it is. A helper card's play, made only to meet a requirement, is held
  too: the pool after it is not read (1_165, the troll's [moria] Orc).
- **`R` clauses are everything that is not printed on the card**: a ruling that names it, an interaction from a
  rules example, a rule that differs by format. Their text says where they come from. A rules example that names
  the card is owed even where it hinders (Gwemegil 1_47 in CR 5.0 "hinder"): Q-0237 waives only boards it names none in.
- A clause line is `ID Type [checks] text`. Long text continues on following lines, indented.
- **Under each clause, one line per test that claims any of its checks**: `checks -> TestMethodName`. A test may
  claim checks of several clauses and a check may be claimed by several tests; the checks on a claim line may
  stand in any order. Every check a clause lists must be
  claimed, and every behaviour test in the file must claim something. A test that claims nothing is either
  missing from the ledger or testing nothing the card says. A test parked with `@Ignore` claims the checks it
  would show if the reading it asserts is right; the conformance check warns where a parked test is a check's
  only claimant, because that check is not being exercised.
- `Dropped:` is for a card that had behaviour tests before: anything they asserted that the new tests do not,
  and why, or `none`. A reviewer compares the old file with the new and should find nothing this line does not
  account for. Name the kinds of card the old tests used and what stands in for each now (an archer who is not
  unique, an item of the card's own culture). An old assertion on the game's own wording of a decision need not
  be listed. A dropped assertion that belongs to the rules and not the card (the Rule of 4) is listed, so that a
  mechanic test can pick it up.
- `Sources:` says where the text came from and whether the rules name the card. `Findings:` lists any finding ids
  (`F-0000`) or questions (`Q-0000`) the card is tied to. `Notes:` is for anything a reviewer should know, above
  all why a check the wording seems to call for was left off. What was tried is said in words (what was run
  and what it showed), never by the path of a scratch file. Each may run onto following lines. Put `Sources:`
  above the clauses and `Findings:`, `Dropped:` and `Notes:` below them.

Something in the Notes that a later reader of the *test* would need (why a helper card was chosen, why a number
is what it is) belongs in the test as an ordinary comment, in plain words, with no clause ids.

### The checks

Each check is one thing a test demonstrates. A clause lists the checks its wording calls for; each listed check is
then claimed by a test.

| Check | Shows that |
|---|---|
| `+` | With everything the text asks for in place, the thing happens. State is read before and after. |
| `-` | In the *nearest* situation where the text says it should not happen, it does not: the requirement one card short, the event that almost matches, the phase next door. It has to be a situation in which the card is active, so that a mistake in the definition could make the test fail: a Free Peoples card doing nothing on the opponent's turn shows the rule "active", not the card. |
| `C` | Each cost is paid, read before and after. |
| `C-` | With a cost that cannot be paid, the action is not offered. A cost is judged payable IN FULL before it begins, a repeated cost for every repetition (F-0425, Ketura: "if you're unable to pay the cost by default, you're not supposed to be able to perform the action at all"): a cost that can be paid once and not in full is a `C-` board too. |
| `F` | Who or what can be chosen or affected is exactly who the wording says. Valid candidates: at least one more than is needed, differing from each other in everything the wording does *not* mention, all offered or affected. Invalid candidates: one for each thing the wording excludes, each failing *only* the thing it is there for, none offered or affected. What that asks for each shape of wording the campaign has met is set out under "The candidates of `F`, case by case", below this table: find the case of the wording at hand there (`lookup.py "<words>" --in standard`). |
| `D` | The effect is still in force just before the boundary the text names and gone just after. "Just before" is in the phase that precedes the boundary in a full turn, with the thing the effect touches going on: for "until the regroup phase", during a skirmish that follows one already resolved, and then at the first regroup decision. An effect that holds a shorter-lived part inside it (a trigger lasting until regroup that limits something phase by phase) is read across two phases, or "one in all" and "one in each phase" cannot be told apart. For "until the end of the turn" the last reading is in the Shadow phase after a second move, not at the first regroup decision, which `until: end(regroup)` also passes (Rohirrim Road 7_332); for a move limit, a third move offered in the regroup phase after the second move serves as well, since `end(regroup)` lapses with the second move (Elendil 9_32). |
| `L` | A limit holds: it works up to the limit and does nothing beyond it. Which reading shows it goes by the kind of limit (Ketura on Q-0093). A limit on uses ("limit once per turn", "limit once per phase", "limit N times") withholds the ability at its limit, cost or no cost: the test reads it not offered, with its cost still payable and something on the board the effect could still have acted on. A limit on what the effect gives ("(limit +3)", or a bare number of things added: "to add a threat (limit 1)") leaves an ability or optional trigger that has a cost on offer at its limit (Q-0041): the test takes it once more and reads the cost paid and everything else as it was, with something on the board the effect could still have acted on. With no cost, and for a required trigger, it is not offered or does not happen under either kind. Where a limit caps what an X buys ("discard X cards to make … strength +X (limit +3)"), X itself is not capped: a test shows it, with one use beyond the cap or by reading the choice's maximum. A cap inside an effect ("takes no more than 1 wound") is a limit too; one that renews (each phase, each turn) is shown renewing. |
| `O` | "May" asks, and declining leaves everything as it was. Listed when the text says "may". An optional trigger whose EFFECT is then a choice of "up to N" or "each may" asks twice, the offer and then a choice from 0 to N, and that is as Ketura wants it (F-0385: "click on the card to trigger it ... and then have the option to select 0-N, if one were to change one's mind"): the test reads the offer and the choice's minimum of 0, and nothing is filed. That does not reach a choice that is a COST, or a text that says "a" and not "up to" (rulings log, "What is offered and what is asked"). |
| `S` | A value that depends on a count is right at two different counts, and at its cap if it has one. Where the count is an X the player chooses, zero is one of the counts where the action still does something at zero (One Dwarf in Moria heals whatever X is). Not where X is a number of exertions, and not where X of zero would do nothing at all: such a use is deliberately not offered (rulings log, Q-0015 and Q-0034: on an ability or optional trigger with nothing else to pay; an event is offered whatever its X can do, and no test turns on its X of zero), and the `-` is that it is not. Where X is a number of cards moved or tokens spent, and a board can arise on which what was paid and what the board lost are different numbers (a bearer who dies takes his other items with him), one test uses that board. |
| `R` | "Each time" happens a second time, on the same subject where the wording allows: two wounds on one minion tell "for each wound" from "for each minion wounded", which two minions wounded once cannot. |
| `B` | Each alternative of an "or" or "choose one" is taken in some test. Where a board that could arise makes one alternative impossible to carry out in full, a test shows the other is then what happens: a player may only choose what can be fully performed. A discard of a whole hand with no card in it is a discard of none and may be chosen (Q-0255: B, "discarding 0 cards is kosher"), and so may "choose an opponent who may ..." whatever he could then do (Q-0257: A): neither is a board where the other alternative is forced. An "X or Y" COST also owes one board where both can be paid, with the player read as asked which (both offered) and one taken (Q-0308: B; tests landed before it are not redone). |
| `Z` | Cards that move end up where the text says: attached to the chosen bearer, stacked, in the dead pile, on top of the deck. For a destination the text names. Where a card arrives somewhere and is taken out again in one effect, it is read there in between. A plain discard is read where it is paid or done, under `C` or `+`, and does not call for `Z`. |
| `K` | The keyword, with its number if it has one, is on the card. Claimed by the stats test (`K -> ElrondStatsAndKeywordsAreCorrect`). |

#### The candidates of `F`, case by case

What the `F` row asks, for each shape of wording the campaign has met. A clause owes the cases its own words fall under and no others.

**Valid candidates differ in everything the wording does not mention.** For "a Dwarf", or a keyword with no type
word, "your archers", a companion and an ally: with no ally `all(your,archer,or(companion,minion))` passes, 94_13;
for "a character" or "borne by a character", a companion, an ally and a minion, the three kinds of character: with
no ally on the table `or(companion,minion)` passes, 61_100; for "each minion" or "another minion", minions of two
cultures and two races: beside Orcs alone, `minion` narrowed to `orc` passes, 60_91, and for "each minion
skirmishing <X>" two minions in ONE skirmish as well, the second put on X by the Shadow player after the Free
Peoples player's assignment (pitfalls, "One minion to a companion"): with one minion to a skirmish a definition
that takes "a minion" passes, 11_243; for a bare side word, "2 Free Peoples cards" from a deck, a card of each type
the place can hold, a companion, a possession and an event: beside companions alone, `side(freeps)` narrowed to
`companion` passes, 68_82, and "each type" is a spread like that one, a character, an item and a card that is
neither, not every type in print; a bare owner word, "each of your cards", owes that spread and a card of each
side, 94_27; for a keyword or item class alone, "a pipeweed", "a pipe", cards of two cultures wherever printed
cards have them: beside [shire] ones alone, `pipeweed` narrowed to `culture(shire)` passes, 92_4; for a culture word
on a kind a title is printed in ("a [gollum] minion"), one of another title, also inside an effect's "If that
minion is ...": beside Gollum alone `name(Gollum)` passes, 15_51 (Shelob 8_26); and a card of the
other culture whose own text stays out of the way is looked for before the culture is left out, and where printed
cards of more than one type have the keyword, one of each type ("a fortification": a condition and Stronghold of My
Fathers 102_48, a possession): beside conditions alone, `condition,fortification` passes, 10_117; for a bare `weapon`,
a possession and an artifact (Narsil 9_34 on a [gondor] Man, The Balrog's Sword 2_50 on The Balrog 12_79): beside
possessions alone, `weapon` narrowed to `possession,weapon` passes, 1_230 and 1_173; for a count or spot of an item
type ("each [gondor] artifact you can spot"), one in a support area and one borne (helpers.md, "A [gondor] artifact
that someone bears"): beside support-area cards alone, `zone(support)` added passes, 9_36; for "discard N cards from
hand", a card of another culture in the hand: beside [moria] cards alone, `choose(culture(moria))` passes, 1_196; for
a bearer by race ("Bearer must be a Hobbit"), the Ring-bearer and a Hobbit of another culture (Pippin, Trollslayer
103_31): beside [shire] Hobbits who are not the Ring-bearer, `hobbit,culture(shire)` passes, 3_108; for "bearer" in a
Modifier or Restriction on a card that a companion and an ally can both come to bear (a transfer to "a character"),
one bearer of each: with companions alone bearing it, `and(companion,hasAttached(self))` passes, 1_207; for "a
companion of each culture", a companion of each culture a printed companion has, the Hobbit sets aside (Q-0303;
Notes name a culture only they have): with three laid, a line written for the wrong culture passes, 13_189.
A kind no printed card can make a candidate drops out of the spread, and Notes say why: for "Bearer must be a Dwarf"
there is no opponent's Dwarf, as no Shadow card is a Dwarf (9_8). (For "Exert a [gondor] Man" the ally IS owed:
Gondorian Merchant 7_97 bearing Narsil 9_34 can exert, 5_39; helpers.md.)

**Invalid candidates, word by word.** For a phrase of two words ("a [sauron] minion") that is a card for each word:
a [sauron] card that is no minion, and a minion of another culture; for an item class ("bearing a hand weapon") one
of them is a weapon of another class on a bearer of the right kind (a ranged weapon), where a card in play can bear
one, since the game has a `weapon` filter one word wider and a possession that is no weapon cannot tell the two
apart (65_148); "mounted" is an item class too (bearing a possession of the class mount), and owes a companion
bearing a possession that is no mount (Gimli 1_13 with Dwarven Armor 1_8, 4_346); for a number with two bounds, a card on each side (a bound no board can reach, as site 1 of
"sites 1 to 5" for a skirmish, is stood for by the nearest reachable value inside it, site 2, and Notes says so). Where the type word has a character neighbour (companion against ally), the card of the first word that is no X
is a character where print allows (Barliman Butterbur 1_70, a [gandalf] ally, for "a [gandalf] companion"), or the
type widened to `character` passes (Alatar 13_28). For "an X skirmishing a Y" that is an X assigned
to another Y whose skirmish has not begun, not an X fighting something else. That is the candidate for the word
"skirmishing"; the word "a Y" wants its own, an X fighting a character who is no Y, read DURING that skirmish (fight
it first), or `inSkirmish` alone passes. Where Y is one character (bearer, Frodo), the first is an X assigned to him
and read before his skirmish begins (Mithril-coat 2_105); where the text acts only once his skirmish is under way (a trigger at its start), it is an X assigned to another character whose skirmish comes later, read at the start of his, since one that already fought elsewhere passes `assignedToSkirmish(character)` (Brego 4_263). Where his text can be used in a skirmish not his own (a skirmish special ability), the second is an X fighting another companion, read during that skirmish, or `inSkirmish` passes for `inSkirmishAgainst(self)` (Glorfindel 9_16). An exception
named by a phase alone ("cannot take wounds except during a skirmish") owes the card in NO skirmish while another is
fought, or `not(inSkirmish)` passes for `not(phase(skirmish))` (11_94; helpers.md, Long I Fell 4_97).

**"The Ring-bearer", and a Frodo who is not the Ring-bearer.** For "the Ring-bearer" it is a Ring-bound companion
who is not the Ring-bearer (Sam), not just any companion; a Ring-bearer who is not Frodo is no longer asked for on
a card that says "the Ring-bearer" (Q-0213: A: no definition has ever written Frodo for him; the tests that have
one stay, and no Frodo stands beside him, Q-0159: B). Where it is the text that names Frodo ("Bearer must be
Frodo", "If Frodo dies") and a test wants a Frodo who is not the Ring-bearer, he is Frodo, Frenzied Fighter 13_149,
the only Frodo who can be in play beside another Ring-bearer (51_313, 51_311; Q-0212).

**A phrase the text carries more than once.** Where the text names its target more than once ("make that Dwarf A,
or make that Dwarf B"), the candidate not chosen is read under each. Where the same phrase stands in several
clauses, or in a condition or a trigger and not a choice ("a site bearing a weather"), each clause has its own
invalid candidate. Where a trigger and the choice it leads to each carry the phrase ("If a [sauron] Orc wins a
skirmish, make that Orc ...", whose definition filters the winner and then the choice), the invalid candidates take
part in the very event the valid ones do (a [moria] Orc and a [sauron] Uruk-hai winning that same skirmish) and are
read as not offered: on a board where the trigger never fires, the choice's own filter goes unchallenged (1_247).

**A spot requirement.** A spot requirement owes its valid candidates as a choice does, on one board or across the
tests: for "spot an Elf companion", Elf companions of two titles, or `name(Legolas)` passes (91_12). A spot that
must FAIL ("moves ... without a ranger") is the same: the cards that would stop it are its valid candidates, each
read stopping it, and one that fails a word (the other player's ranger, not in the fellowship that moves) is read not stopping it (1_355).

**What a response saves; who did the thing; a card being played; the player who does it.** Where a response picks
what it saves from several cards affected at once, an invalid candidate is among them (for "your [sauron] condition
is about to be discarded", a [sauron] possession in the same discard; for a wound, the losers of one skirmish,
whose wounds are taken together where a "wound each" is not: pitfalls, "Wound each"). One that saves with no choice ("discard this
condition to prevent that") prevents the whole of that one simultaneous discard, cards not of the kind it names and
the opponent's own among them, and they are read saved (Q-0250: B, "the action is all or nothing"; Siege Engine
5_60, "one or more machines"). A singular "If a <kind> is about to be discarded" saves the one card, one at each use (Q-0313: A; shapes.md). Where a trigger names who did the thing
("exerted by a Free Peoples card", "uses a special ability"), one valid doer is neither the card itself nor its
bearer, and one is no character where the wording allows (a possession's ability). Where a condition or a trigger
turns on a card of a kind being PLAYED ("unless a Shadow event is played"), each word owes a card that fails it
alone and is played in that same window (a Shadow card that is no event, a Free Peoples event); a special ability
used there stands in for neither (10_116). Where the wording names the PLAYER who does it ("the Free Peoples player
is about to discard", "discarded by an opponent"), one valid doer is that player acting by no card of his side (a
site's text: Heights of Isengard 11_244), which for a card-worded text is an invalid one; and the other player made
to do it by that player's card (Introspection 12_29 for a discard; It Burns Us 6_110 for a reveal) is on the table
too. Where he has a choice in it he is the one doing it (Q-0171: "if the player has a choice, they're not
forced"); where he has none, the card's player is (Q-0251: "Since there's no optionality and no choice, I'd say this
is Free Peoples doing the revealing"). This reaches every act a card makes the other player do, an assignment as well as a
discard or a reveal, and either way round: Barbaric Uruk 12_135 making him assign the one unwounded companion is
the Shadow player's assignment (F-0513, Wold of Rohan 4_336; built by engine pass 45). shapes.md has the boards (F-0296).

**A race against a culture.** A race against a culture, and a culture against a race, is shown with a PRINTED card
that has the one without the other (Pippin, Trollslayer 103_31 is a [gondor] Hobbit; Sméagol, Once Like Us 103_116
a [shire] companion who is no Hobbit; Merry, Master Holbytla 103_85 a [rohan] Hobbit; Khamûl 103_61 a [raider]
Nazgûl: gemp-testing's `helpers.md` has them by pair), never with an ad hoc modifier: no test gives a card a
culture or a race (Q-0213: B). Where no printed card has the one without the other, no candidate is asked for at
all: that is always so for Elf and [elven], for Dwarf and [dwarven], for a Free Peoples Wizard and for a Free
Peoples Wraith. The slip itself (F-0019, and the reverse, F-0291) is read off the definition: lint warns
`race-culture` where a definition goes by the race and its text names the culture, or the reverse, and that is a
finding to file, not a board to lay.

**A keyword or a type word that no card can tell apart; what no board can isolate.** Nor is a keyword given to a
card no printed card with that keyword resembles (an Orc made a Southron, an ally made valiant; Q-0213: C): where
every printed card with the keyword is of one culture, race or type, that word cannot be told from the keyword,
which is a line in Notes. So is a type word beside something only that type has (a signet, a timeword). Where no
board can isolate an excluded thing ("your" tale, when the opponent's is inactive anyway), put the nearest
candidate down and say so in Notes.

**Cards picked out by title.** Where the text picks cards out by title and a type word ("these characters:
Celeborn, Elrond, Galadriel, Saruman"), an invalid candidate is a card of that title and another type (a follower
named Saruman). Where no printed card of those titles is of another type, none is laid and Notes say so; a card
whose title only begins with the name may stand as the nearest miss (Boromir, My Brother 4_111 for 7_112). Where the text names a title with no type word ("spot Saruman"), the valid candidates are cards of
that title of each type there is (companion, ally, minion, follower) and of a second culture where the title has
more than one, not of every culture (Q-0306: Saruman the [isengard] minion, the follower 17_38 and the [dunland]
minion 4_33, not each of his four minion cultures; Sméagol is [gollum] and [shire]: grep the definitions, the
V-sets' with the Decipher sets': Arwen's one ally is Lady of Rivendell 102_6). The Hobbit
sets (30 to 33) are not counted among them, not even in a Hobbit-set card's own tests unless the card needs it
(Q-0303). That holds for a title that pays a cost or is acted on as it does for
one spotted or played ("exert Gollum": each of the two Gollums, [gollum] and [wraith], pays in a test of his own,
60_20); lint warns `title-kinds` where the cards of such a title that the file names leave out a type of it or are all of one culture.
A card's own name in its own text is that card (`self`), not a title picking cards out: "Exert Elrond" on Elrond
owes `C` and `C-` and no F (1_40 above; Úlairë Nelya 11_222).

**A word in one filter and not in another; cards picked by what the cost's card is.** Where a word is in one filter
of the card and not in another ("a unique [elven] companion", then "all Free Peoples archers"), the valid
candidates of the second vary in it. Where the effect picks its cards by something read from the card chosen for
the cost ("exert a companion to heal another companion with the same signet"), the cost is paid in the tests by
cards that differ in it, or a filter fixed to one value (`signet(aragorn)`) passes (92_32).

**Two places; a card played from the place; an "or" inside the phrase.** Where the text names two places a card is
taken from ("from play or from hand"), the candidates are laid out in each. Where the card is PLAYED from the
place, the invalid candidate can be paid for there, so that only the wording keeps it out (10_19: Shelob in the
discard pile with a pool too small for her let "Gollum" widened to any minion pass; a Free Peoples card adds its
cost and needs no pool laid). An "or" inside the noun phrase
("a [gandalf] or [elven] event") is F's business, one valid candidate for each side of it, and needs no `B`. An "or"
between two noun phrases, each with its own article or a name ("Gandalf or an Elf", "a [sauron] engine or a site
you control"), is two alternatives and lists `B`, each taken alone (1_40 above; 8_95).

**A card that is not unique, and "that card": the second copy and the untouched control.** Where the card is not
unique and its text says "here", "this condition", "this minion" or the like, a second copy is in play wherever it
could change a reading: the test shows which copy a card went to or came from (`ShadowChooseOptionalTrigger(card)`
where each copy makes its own offer), that a standing modifier tied to those words does not stack (a numeric
modifier with no such words does stack, and two copies are no gap there), that a trigger fires once for its own
copy. Where an effect acts on the card itself, or on "that <card>" a trigger has just named, another card of its
kind stands beside it and is read as untouched (for "each time a companion is played, that companion ...", a
companion already in play, or `all(companion)` passes, 91_9; where the effect then takes "that <card>" out of a
PILE, "each time ... discards an [isengard] Orc, you may stack that Orc", the other card of its kind lies in that
pile when the offer is taken, or a choice from the pile passes, 6_63); where a second copy cannot change a reading,
the untouched control is a different card. For a Skirmish ability on "this minion", the copy stands in the SAME
skirmish: one in another skirmish lets `choose(minion,inSkirmish)` pass, a lone candidate being chosen unasked (7_190).

**Whose a card is and where it stands.** What the wording does not mention includes whose a card is and where it
stands: for a bare race word ("a Man", Q-0058) one valid candidate is the opponent's, where a card of his of that
race can be in play at that moment (no minion is in a fellowship phase: then Notes says none can be, and no board
is made for it) (but a player's own Shadow cards are inactive on his turn, so for a "Shadow condition" chosen by a
Free Peoples ability they are no candidates at all); for "a minion" or "an Uruk-hai" on a Skirmish event, one valid
candidate is not assigned to a skirmish, and so on a Skirmish special ability whose text names no assignment
(Noble Leaders 7_112: `assignedToSkirmish(minion)` added to its choice survived without one). A Skirmish action's choice that is not limited to the skirmish ("your
[sauron] minion", Morgul Vale 7_357) also wants one valid candidate IN the skirmish being resolved, or a choice
narrowed to `not(inSkirmish)` passes. Where "your" names a pile, the other player's card in his own pile is an invalid candidate of a title the
chooser's piles do not hold: a choice from a pile is read by blueprint (pitfalls, "A choice from a pile"). That is
owed where the definition word could name the other player's pile (a `player` field: `PlayCardFromDiscard`,
`PutCardsFromDiscardIntoHand`); a word that reads only its performer's own pile (`PlayCardFromDrawDeck`,
`PutCardsFromDeckIntoHand`, `ShuffleCardsFromDiscardIntoDrawDeck`: no `player` or deck field) owes no such board,
since no definition of it can differ there (15_51, dec-b13_53).

**A condition inside a modifier; "bearer" or "self" with a requirement.** Where the excluded thing is part of a
condition inside a modifier ("in a skirmish involving an [isengard] minion"), the candidates are in skirmishes, not
merely on the table. For "bearer" or "self" with a requirement ("if bearer is an archer"), the nearest invalid
candidate is another card that meets the requirement (lint warns `if-is` where a clause with an "If <the thing
named> is ..." sentence lists no `F`: 2_79's "If the Ring-bearer is then exhausted" wanted an exhausted companion
beside a Ring-bearer who is not). That holds where the requirement is the condition of a
trigger and not of an ability: for "While Boromir is the Ring-bearer, at the start of each skirmish involving him"
it is the real Ring-bearer in a skirmish of his own while Boromir is a plain companion (59_31). Where the requirement
stands on a Skirmish ability ("Skirmish: If bearer is an unbound Hobbit, discard this possession to ..."), a bearer who
meets it outside the skirmish is a valid candidate (the text names no skirmish), and the requirement owes the culture
spread of "Bearer must be a Hobbit" (11_171: Pippin, Trollslayer beside Merry); for "unbound" the invalid candidates
are a Ring-bound companion (Sam) and an ally of the race (Bounder 1_286: only companions are unbound, CR 4.2).

**A title of one card type; never two of one unique title; a candidate that looks impossible.** A title that has
one card type only ("spot Gandalf") needs one card of the title, and its invalid candidate is a card of that
culture with another title. That candidate fails the title alone: same culture, same type (a [gandalf] companion
who is no Gandalf, Ent Avenger 6_27; a [gandalf] ally or a companion of another culture lets
`culture(gandalf),companion` through, dec-b1_83). Never put two cards of one unique title in play at once, whatever their types (Ketura,
2026-10-08: the cheats "short-circuit more-or-less legitimate setups"; Elrond the ally and Elrond the companion
each get their own board). Before calling a candidate impossible to put on a table, look for a way: an ally's home
may be on another block's sites (Gondorian Merchant 7_97, the only [gondor] ally, is at 3T).

#### Which checks a clause lists

Which checks a clause lists follows from its words, and the analyst is expected to justify leaving one out:

- anything before "to" in "X to Y" is a cost → `C`, and `C-` where the board can make it unpayable. A card
  discarding itself as its only cost ("discard this condition to …") can always pay: no `C-`, and no `no C-:`
  line. An exert cost's `C-` board is the one who must exert, exhausted; where the cost names a kind ("exert
  your [isengard] Man"), the only one of that kind exhausted with others able to exert beside him. A board with
  nobody of the kind shows the filter (`F`) and cannot tell an exertion from a wound (92_2). A wound cost's `C-`
  is a bar on wounds or a cap reached: Armor 1_92 ("takes no more than 1 wound during each skirmish phase") after
  one wound that phase (Faramir 17_28). A `no C-` search for a wound cost takes in "no more than". An "add (or remove, or reinforce) a [culture] token" cost: Redhorn Pass 13_194 ("Culture tokens cannot be
  added, removed, or reinforced") is its `C-` board (Scouring of the Shire 68_112; gemp-testing's `helpers.md`).
  A cost of REMOVING a token "from here" has a nearer one, which is laid in its place: no token on the card while
  a second copy holds one, which shows "from here" as well (Sudden Fury 12_132).
  An "add a burden" cost: no printed card stops a burden being added, but a hindered Ring-bearer does
  (CR 5.0 "hinder"), so its `C-` board is `scn.HinderCard(<the Ring-bearer>)` (103_115). That board is a V-set
  card's: a Decipher card owes no hindered board, this one included (Q-0237). Where the card must
  itself be the Ring-bearer to pay (Sméagol 59_30's regroup ability), hindering him shuts its text off and no
  board in play is left: a `no C-:` line. The One Ring's own "add a burden to wear" is NOT such a card: its
  text stays on while its bearer is hindered, and the board holds (4_1: not offered in another companion's
  skirmish, and offered there once the cost is changed in the definition). So too where the burden is the price of preventing something done to
  the Ring-bearer (an assignment to him, 94_26): hindered, he cannot be assigned, and the prevention is never put
  (Q-0114). No card is invented for the board (Q-0166: A; Q-0241). A hindered Ring-bearer is not spotted
  either: where the action also spots or counts him, the board keeps that met without him (Get On and Get Away
  54_304: Sméagol beside a hindered Galadriel). An "assign <this character> to a minion" cost: its `C-` board
  is the character already assigned (CR 4.2 "assignment phase": both must be unassigned), by a minion's own
  assignment action after the Free Peoples player's pass (Unexpected Visitor 7_126 and its
  kin, in gemp-testing's `helpers.md`; Alcarin 5_31). A minion he must pay to assign and cannot is Q-0221's
  check, under an `R` clause typed `Rule [+ -]` that names Q-0221 (Alcarin 5_31, Éowyn 17_96): `+` the price
  paid once the minion is chosen, `-` the minion not his to name.
- a noun phrase that picks things out ("a Dwarf", "each [moria] Orc", "another companion", "bearer") → `F`. Not
  "the Ring-bearer" in a bare count of burdens ("for each burden on the Ring-bearer"): burdens lie on nobody
  else, so there is no other candidate to lay (Q-0186). A NAMED bearer, "on Frodo", is a phrase like any other (Q-0103). "Bearer" in a cost ("exert bearer") as well: the
  cost is paid once by a bearer of another kind (an ally beside the Ring-bearer), or `choose(companion,hasAttached(self))`
  passes for `bearer` (Sting 18_113). Where the cost repeats ("exert bearer twice") and no such bearer has the
  vitality, vitality given him by hand is fine, as printed cards give it (Gondorian Merchant 7_97 +2, 15_62)
- "until", "this phase", "this turn" → `D`. Not where the words stand inside a condition and bound a COUNT ("if 3
  or more [raider] minions have been played this phase", "has moved more than once this turn"): nothing is in
  force to end, so no `D`. The bound is then the threshold's `-`: beside the short count, what nearly counts was
  done just outside the window, in the phase before of the same turn for "this phase" (shapes.md, "A count "this
  phase""), on the turn before for "this turn". A count the rules themselves keep and start again each turn
  (the moves made, 92_11) owes a line in Notes and no later turn. A phase ability that changes something and names no duration has
  one by rule (CR 4.2 "phase action": it lasts for the phase it is used in) → `D`; so has an event's, an archery
  total included, read again once the phase is over (Elevated Fire 11_78). So has a trigger's effect:
  by Ketura's ruling (Q-0018) it lasts to the end of the current phase → `D`. Each skirmish is a phase of its
  own (CR 4.2 "skirmish phase(s)"): such an effect from one skirmish is gone in the next. Its "just before" is
  read in the same skirmish at the decision that follows the use, and its "just after" in the next (Elven Sword 4_64)
- "limit", "once per", "no more than", "up to", "at most" → `L`
- an optional trigger or a special ability with no cost (a spot is no cost) → one `-` is the board on which its
  whole effect would do nothing: not offered (Q-0034; shapes, "Spot X to <effect>"). The game withholds such an
  offer itself (Q-0224): the test reads it absent, and no requirement is written for it unless the offer is still
  made there (shapes, "An optional trigger or ability that costs nothing"). On a `Trigger` this `-` is owed beside
  the neighbouring-event `-`, and neither stands in for the other (Heights of Isengard 11_244). For a draw that can come in its
  player's fellowship phase the board is the Rule of 4 spent (Q-0262: B; an empty deck still asks, Q-0029). A keyword given to a card that has it
  already is not "nothing" and is offered (Q-0245: A, Band of Wild Men 4_4: "the effect still has potential to alter
  that calculus"). Where a board can have the card hold that keyword already (the ability used once, then
  offered again: Lurtz 1_127), that offer is owed and read. With a cost it is offered there and used for
  nothing, which is read under `F` or `+` as for an event, and is no `-`. A play of a card after
  the "to" is itself a cost (Q-0062): "add a burden to play a [shire] tale" with no tale in hand that can be
  played is not offered, and that board is its `C-`
- "may" → `O`. A response or any other special ability is the player's to use or not without the text saying
  so; list `O` for one only where declining has a consequence worth showing
- "for each", "X" → `S` (and for an X, the zero case). "For each X you spot" is the player's count: he decides how
  many, and where the effect is a choice of cards it is asked as a choice of 0 to N of them with no number prompt
  (Q-0248, Aragorn, Wingfoot 4_364: "as a 0-N choice of cards to wound"; shapes.md, "<do> a companion for each
  <thing>"). An X read off the card a cost acts on ("a Dwarf who is damage +X ... exert that Dwarf") is what it was
  when spotted, before the cost (Q-0256: A)
- "each time" → `R`
- "or", "choose one" → `B`; so is an "or" between two printed phases ("Fellowship or Regroup:"), each phase taken in a test
- "make each…", "make all… until…" on an event or a special ability → a `Rule` clause citing CR 4.2 "event"
  (some effects affect only what was there when they were used): a test that a card which becomes one of them
  afterwards is not affected. The game writes this into each definition, not into the engine, so it is each
  card's to show. (For an EVENT it is the rule. For an ability of a card in play Ketura has ruled it card by card: a
  snapshot where the cards it names are on the table when it is used, the standing filter where its text could do
  nothing as a snapshot, as Not Feared in Sunlight 52_107, F-0217: `lookup.py "make each"`. A trigger of a card in
  play that lays "each X is ... until ..." is read as an ability is: Madril 15_64.)
- "lose…", "cannot…" for a time → a `Ruling` clause: "can't" beats "can", so what was lost is not given back
  by another card before the time is up. Where the card's own text says so ("loses fierce and cannot gain fierce
  until ...", Prized Lagan 68_110), the "cannot gain" half may stand as an `Effect` clause of its own listing
  `+ - D` in place of the `Ruling` clause; the tests owed are the same (a gain refused before the time, allowed after)
- a search of a draw deck ("from your draw deck", when the whole deck is looked through) → a `Rule` clause citing
  CR 4.2 "draw deck", whether or not the card says "shuffle": `+` is one more shuffle of that deck (or two: see
  gemp-testing; read as that exact number, not as `>`) and none of the other player's, read with the shuffle count; `-` is no shuffle on any alternative
  that does not look through the deck ("or discard pile"), and none when the player declines before looking. Like
  the snapshot, this is a rule tested card by card because the game writes it into each definition. Where the
  card prints "Shuffle your draw deck." as a sentence, that sentence's clause owes these claims and stands for
  the Rule clause (Saved From the Fire 8_20); where the card always looks, with no other pile and nothing to
  decline first, there is no `-`, and the clause carries a `no -:` line saying so (Sudden Strike 15_51)
- "reveal" → the card is shown to the other player, who has a decision to dismiss. One test reads the showing
  for each player (the shown card listed and not selectable, a card not revealed absent); the others only
  dismiss (the dismiss methods fail the test when that player is being shown nothing). A reveal from a hand is
  shown to the other players only: assert once that the hand's owner is shown nothing. A card taken from a draw
  deck by its type, where the text does not say "reveal", is shown to nobody and named in the game log (Q-0025):
  one test of the taking marks the log and reads that the cards taken are named and one left in the deck is not
  (gemp-testing, "A card made known by the game log alone")
- a threshold ("if you can spot 3…") → `+` at exactly the threshold and `-` one short of it, with something
  beside the short count that nearly counts (a third ring of another culture)
- "play…on", "stack", "place…in the dead pile", "put…on top of" and the like → `Z`; so does "play the
  fellowship's next site" (the site laid on the path, and a site it replaces back in its owner's adventure deck)

### Clause types

The type says what kind of statement the clause is, and sets the checks it must list at the very least.

| Type | The clause is | At least | Notes |
|---|---|---|---|
| `Keyword` | a keyword line: "Ranger.", "Damage +1.", "Fierce." | `K` | How the keyword works is the rules' business and is tested once, as a mechanic. The sentence "Plays to your support area." is a clause of this type, claimed by the stats test's Support Area reading (Q-0279: B). Only where the game text prints it: a card whose sheet subtype is Support Area and whose text has no such sentence (Gladden Homestead 13_49) has no such clause. A keyword the text grants under a condition is a `Modifier`. A keyword the card has by rule and not in print (every Sam is Ring-bound) is an `R` clause of this type. A follower's "Aid - (N)." is of this type, its reminder in brackets left out; the Aid's cost is an `aidCost` effect and no keyword: where it is twilight the stats test reads the N with `scn.GetAidTwilightCost(card)` (G-0051), and where it is anything else (a burden, a threat, an exertion) nothing reads it and the header carries it. Either way `K` is claimed by the stats test and by a test that takes the Aid and reads what was paid, the pool N higher (Robin Smallburrow 68_111). An Aid cost that chooses a card ("Aid - Exert a [gondor] companion", Elendil's Army 18_47) is a cost like any other: the clause lists `C`, `C-` and `F` for its noun phrase beside `K`. |
| `ToPlay` | "To play, spot…" | `+ -` | Show the change: not playable, then one legal play later, playable. A spot of a noun phrase ("a [rohan] Man") lists `F` as well, by the bullet on noun phrases: a candidate missing each word (Brego 4_263). |
| `ExtraCost` | "To play, exert…" / "add a burden" | `+ - C` | `-` is the cost being unpayable, and that board is the cost's `C-` as well: `C-` is not listed again beside it (a ledger that does list it claims it on the same test and is not wrong). A cost another player must pay to do something with this card while it is in play ("The Free Peoples player must add a burden to assign this minion to …") is a `Modifier` that lists `C` and `C-` as well (69_38). |
| `PlayOn` | "Bearer must be…", "Plays on…" | `F` | Two valid bearers offered, an invalid one not. |
| `Activated` | "Phase: cost to effect." on a card in play | `+ -` | For `-`, prefer in this order: a requirement in the text unmet; the cost unpayable; a phase the text does not name. Where the text has no requirement, the test of the unpayable cost claims `-` and `C-` both. The phase is the next one in which that player is asked for actions, not any phase: for a Maneuver event, the archery phase; for a Fellowship action, the maneuver phase with a minion laid so that there is one (the Free Peoples player is asked nothing in the Shadow phase); for a Shadow player's Regroup ability, the maneuver phase of the same turn, not a phase of his next turn, where the roles are reversed (8_103, 58_103). A condition that stands after the "to" ("Exert Galadriel to play the fellowship's next site if it is a forest") is part of the effect and no requirement (F-0022): where it fails the ability is still offered and its cost paid for nothing (Q-0034, an ability with a cost), and that board is read under `F`, not as the `-` (3_17). A minion's own no-cost ability that assigns "this minion" owes a further `-`: the minion already assigned, with a candidate still unassigned (shapes.md, "Each minion gains this ability", F-0388; 8_95). |
| `Event` | the text of an event card | `+ -` | `+` includes the event leaving the hand. `-` as for `Activated`; "the cost" there is a cost the TEXT prints, so a pool short of the card's printed twilight cost is no `-`, and an event whose text has no requirement and no cost takes the phase (Whistling Death 11_104). An event that needs initiative does not count itself toward it (F-0464: A): its `-` is a hand of exactly four with the event among them, its `+` a fifth card (shapes.md, "If you have initiative"). An event's target is not a requirement: a board with nothing to target is read under `F` or `+` (the event is played for nothing, CR 4.2 "effect"), not as the `-`. |
| `Effect` | a further sentence of an event or special ability, with no phase, requirement or cost of its own ("Then…", "If you do, …", a second sentence) | `+` | Its `-` is the first sentence's. It lists whatever else its own wording calls for. A rider "The Free Peoples player may X to prevent this." is of this type and lists `+ O C C-`: `+` is the thing prevented with the first sentence's cost still paid (CR 4.2 "cost"), `O` the other player declining, `C` and `C-` that player's cost. It lists `-` as well where the card can reach a board on which the thing prevented cannot happen (the prevention is then not asked: Q-0114); where it can reach none, Notes say why. |
| `Trigger` | "Each time…", "When…", "At the start of…" | `+ -` | `-` is the nearest event that should not set it off. Where the trigger names a phase ("if the fellowship moves during the regroup phase"), that is the same event in another phase of the same turn; this holds for a response event as well. A requirement or threshold not met ("for each companion over 4" with four) is another `-` and does not stand in for the neighbouring event (another card of the kind played while this one is in play), which an active test reads even where the tests that would also notice it are parked (1_234). For a trigger on the card's OWN play ("When you play X"), that other card is played AFTER X, once X's trigger exists: played before, a wider filter cannot show (19_38). A COMPANION's "When you play" owes as well a `Rule` clause for CR 4.2 "starting fellowship" ("You may use 'When you play' game text on a starting companion"), though every such companion obeys it (Q-0305: "valid to check on everyone (where possible)"): he is chosen from the "Starting fellowship" choice the scenario rests at, and what the trigger does is read there (Pippin 1_307, the bid burden). Where it has nothing to act on there (no minion, no wound and an empty dead pile before the first turn: Aragorn 10_25, Erkenbrand 102_47), Notes say so and no clause is written; a text that excepts the starting fellowship has that board as its `-` instead (Gandalf 17_17). For "is killed", "is discarded" and "is wounded" the nearest event is the neighbouring word: a companion discarded from play is the `-` of "killed" (CR 4.2 "killed"; Slag Mounds 7_363), and a kill that of "discarded" (F-0444). Exerting and wounding are not owed as each other's `-`: an exertion beside "takes a wound" (Q-0265: A) and a wound beside "each time X exerts" (Q-0315: A) are each shown once, in `mechanics/wounds/` (`AWoundThatIsNotTakenTests`, `AWoundIsNoExertionTests`). For "wins a skirmish" it is the same character LOSING a skirmish: a trigger on the end of any skirmish he is in passed every other test of 11_35 and 12_35; for a trigger on the card's OWN win that loss is what is owed, and another card of its kind winning is not (Q-0309: A, Úlairë Toldëa 6_88). Where the effect acts on a card the neighbouring event does not touch ("you may heal bearer"), lay that card so the effect could reach it (bearer wounded), or the offer is withheld as doing nothing whatever set it off (Q-0224) and the `-` shows nothing (Brego 4_263). |
| `Response` | "Response: If…" | `+ -` | `+` shows the response offered and its effect; for a prevention, that the prevented thing did not happen. |
| `Modifier` | a standing effect: "Bearer is strength +2.", "While…, …" | `+ -` | `-` is the condition not holding, or a card just outside its scope. Read the value with and without. A modifier on the card itself or its bearer still owes F's other card of its kind, one that meets the condition and is read as untouched. Where two sentences between them take in every card of a kind ("each of your minions is +1 ... each of your opponent's minions is -1", 94_23), no card is outside both: each sentence's `-` is the other's card, read at the other's number, and the printed number is the control. Where a wider sentence of the same card gives the same thing wherever a narrower one's condition fails (13_155: muster to the bearer in region 1, to [shire] companions in region 2, to every character in region 3), the narrower one's `-` is its card just outside the scope, and Notes say that its condition has no board of its own (no `no` line). A site's continuous text (a plain `modifier`) also owes an `R` clause: the same board with the site left behind (CR 4.2 "active", LR 3; shapes.md, "A site's continuous text"). "Is not overwhelmed unless his strength is tripled" is of this type, `+ - F` (Boromir 1_96; Coat of Mail 51_101's ledger, which types it `Restriction`, owes the same checks). |
| `Restriction` | "may not", "cannot" | `+ -` | `+` is the forbidden thing refused; `-` is the same thing allowed on the same board without this card. Without that control the refusal proves nothing. A bar on the card itself (`minion: self`) owes, as a modifier on itself does, another card of its kind on the board read as not barred (19_18). Others being offered in the very choice that leaves this one out is a control too, provided nothing else about them explains the difference. A Restriction that names its doers or its objects by a filter ("cannot be removed by Free Peoples cards") lists `F` and owes F's spread of valid candidates as a choice does: for a bare side word, a character, an item and a card that is neither (10_47: beside an ally and a possession alone, `side(free people)` narrowed to those two kinds passed). A card that bars an ACT ("may not look at or reveal", "cannot take wounds") is tested on ONE act it stops and its control, not on every way the act is done: that each definition word doing the act asks the bar is shown in a mechanics class (Q-0264: B2; Ketura: "mechanic tests should exist that do such a thing ... which will then ensure that future cards using the bar only need to act as A"; shapes.md, "A card that bars an act", names the classes). |
| `Choice` | a sentence that is only a choice between alternatives | `B` | Usually a choice is part of another clause, which then lists `B`. |
| `Ruling` | an entry in Section Three of the rules that names this card | `+` | Reproduce the ruling's situation. |
| `Rule` | a glossary entry that bears on this card in a way its text does not show | `+` | For example, the entry "Sam": a Sam who can become the Ring-bearer when Frodo dies can only do so if Frodo was the Ring-bearer. Cite the entry. Not for rules that every card of the kind obeys, which are mechanics. |
| `Interaction` | a rules example that names this card with another | `+` | Lives in the file of the card named first; the other card's ledger mentions it under `Notes:`. Where the example's situation is also a check of the other card's own text (the nearest miss of its response: Sapling of the White Tree 59_35), that card's file tests it as that check, with any card that brings the situation about, and writes no `Interaction` clause. |
| `FormatRule` | behaviour that differs by format | `+ -` | One scenario method per format. See `references/format-rules.md`. |

A sentence that is several of these at once takes the type of its main verb and lists the checks of all of them.

**A check the type asks for and the clause cannot have** is written under the clause, in place of a claim, as
`no <check>: <why>`:

```
C2 Trigger    [+ F R]    At the start of each of your turns, heal every ally whose home is site 3.
     no -: the only turns that are not yours are ones on which this card is not active
```

The conformance check accepts it and counts it, and the reviewer reads every one. It is for a check that cannot
be shown at all, not for one that is awkward to arrange. A `no` line's reason may run on to the lines below it.
Where the reason, or a line in Notes or `Dropped:`, is that no printed card makes the board, it names the search of the
`gametext:` lines that found none, made for the shape of words wanted ("wound a companion") and not for one card.
The Hobbit sets (30 to 33) have no `gametext:` lines: search their definitions by field as well (`race:`, `type:`,
`itemclass:`) before saying none exists (67_24: Glamdring 30_29, an artifact hand weapon, was missed that way).
The check is then left out of the clause's brackets: a clause that lists a check and also has a `no` line for it
is refused.
A claim line ends at the test's name, and only claim, `no` and `waits` lines stand below one: the conformance
check reports a claim with words after it, or a remark below a claim, by its line in the ledger. Remarks go in
Notes.

**A check the rig cannot show yet** is written the same way, as `waits <check>: <the rig request> <what the rig
lacks>`. It is no waiver: the check is still owed, and the card is parked on the request until it is built.

```
C1 Modifier   [+ - F]    Bill the Pony cannot be assigned to a skirmish against a Nazgul.
     waits F: G-0000 no reading of whether one character may be assigned to one minion
```

### The stats test

`<Name>StatsAndKeywordsAreCorrect` asserts every printed field of the card against the blueprint: title,
subtitle, uniqueness, side, culture, type, race or item class, twilight cost, strength, vitality, resistance,
signet, site number or home, and each keyword with its number. A possession or artifact that prints no item class
reads null: `assertNull(getPossessionClasses())` (Bill the Pony 3_106, The Mirror of Galadriel 1_55). A site's fields are its block (`getSiteBlock()`),
its Shadow number (`getTwilightCost()`), its direction, and its site number, which a Shadows-block site does not
print (`getSiteNumber()` is 0 there). Its header comment reproduces the card. The
expected values are the sheet's; `reconcile.py <id>` (Decipher cards) and the packet's "Sheet against
definition" section (every card) show any field where the sheet and the definition disagree (see "When the sheet
and the definition disagree" above). A strength, vitality or other bonus printed on a card that is borne (a
possession's +2) is a printed field: asserted here, with no clause of its own; that it reaches the bearer is
`mechanics/inplay/BorneItemsTests`'. Its line of the header is written as the sheet prints it ("Vitality: +2") or as the bare
number ("Vitality: 2"): the landed files have both, lint takes both, and no assertion hangs on it. Timewords are not a printed field: they are how a
definition says when an event may be played, and a definition may carry more of them than the card prints.
A Race to Mount Doom modifier's intensity is one: its header has an `Intensity:` line and the test reads
`getBlueprint().getIntensity()`, at the definition's value (no sheet has these cards); lint asks for both.

On a PC card the header comment already in the file often describes an earlier version of the card, above
assertions that are current. Rewrite it from the packet; never keep it on trust. The V-set and errata sheets have
no resistance column, so resistance is asserted at the definition's value. No sheet has a site's direction
either: assert the definition's value, and on an erratum compare it with the original's `direction:`; where the
two differ it is a finding, and the two card images say which was printed (Doorway to Doom 68_134, F-0302).
It is a `LotroCardBlueprint.Direction` (`LotroCardBlueprint.Direction.LEFT`, with an import of
`com.gempukku.lotro.game.LotroCardBlueprint`; a bare `Direction` does not compile: `Card_13_185_Tests`).
A definition with no `direction:` line (every Hobbit-set site) reads null: assert `assertNull(getSiteDirection())`
until the card is transcribed from its image (`Card_30_051_Tests`).

## Rules every test follows

**Its own table.** A test's first statement gets a table from a `Get...Scenario()` method. Tests share nothing.

**Arrange by cheating, act by playing.** Put the board where it needs to be with the rig's cheats, then do the
thing under test the way a player would. Never cheat the thing under test, and never play out something that is
only set-up. Where a clause counts something ("strength +2 while he bears 3 items"), changing the counted thing
is part of the act: do it by legal play in one test, which shows the modifier switching on as the count is
reached, and cheat it in the others. That is for a standing text, which has to be seen to switch on. Where nothing the
text bars can happen in the phase the count is changed in (a bar on Shadow acts, reached by playing a companion),
the play comes ahead of the act and the act is read refused; the test without the count is the reading of it off
(4_301). A trigger or a one-off effect that counts at its moment ("add (1) for each burden", "for each Free
Peoples possession") reads the board once: its counts are cheated, at the two counts `S` asks for (92_25, 92_26).

**Read before and after.** An assertion after an action means little without the same reading before it. If an
ability exerts, read 0 wounds, act, read 1. A state the test laid by a cheat (wounds added, an item attached, a card put in a zone) is read too, before the action that changes it, and so is one the action must leave alone (a control's cheated wounds, a copy cheated onto a bearer that a declined offer leaves there): "unchanged" after means nothing without the reading before (dec-b7_354). A card cheated into a zone the action takes it from is put there early enough to be read there first (a Nazgûl cheated into the discard pile he is then played from); the one state not read before is one the action removes at once, an item discarded as soon as it is attached (Q-0307). Where the amount is not in the wording ("heal a minion", "wound a companion"), the target
carries more than the effect should change: one wound healed from two reads 1, which "heal two" would not.
Where the target's vitality holds only one wound and no printed card raises it (a Hobbit ally healing itself), its vitality is
raised by hand, `VitalityModifier` after it is in play: a number that only arranges the board (Q-0213: E; 1_297).

**Give choices something to choose from.** One more valid candidate than the choice needs, and an invalid one
beside them (see "One candidate is chosen for you" in gemp-testing). Assert the offer with
`FreepsHasCardChoiceAvailable(...)` and `FreepsHasCardChoiceNotAvailable(...)` before choosing. A choice the game
carries out unasked, because one valid card is there, shows nothing of the invalid card beside it: a second valid
candidate is laid and the invalid one read "not offered" (Q-0266: B; Ketura: "Yeah I think B is the way to go.").
That is owed by the test that claims the choice's `F`; a `-`/`C-` test whose choice goes unasked because only one
candidate can be paid for owes nothing more there (8_30).
Q-0266 is about choices of cards, an effect's own ("exert Faramir to wound that minion", 4_118) as well as a bearer's or
a target's: "<do X> or <do Y>" carried out unasked because one branch cannot be done in full
is the board F-0310 owes (shapes.md, "<Do X> or <do Y>"), and the reading there.
Where the text allows only one valid candidate ("the losing character": one skirmish has one, Blade Tip 1_209),
the invalid ones are laid all the same, the game going on unasked is the reading, and Notes say why.
A bearer named by a unique title ("Bearer must be Legolas") is such a text (Q-0239): one board for each card type
of the title that is laid, and the invalid candidate read as the play refused on a board without him (1_33).
Where the invalid candidate is the Ring-bearer and the effect a discard, the reading shows nothing either way, since
he is never discarded (Q-0109): the bearer in such a test is another (Sam bearing Sting 8_113). Where the text
says neither "may" nor "up to", read the minimum of one such choice (`FreepsGetChoiceMin()`; a play from a draw
deck is 0 to 1 by Q-0019 whatever the text says: pitfalls, "A "play ... from your draw deck" action"): a choice that has
wrongly become optional looks the same otherwise. A "may" that the game asks as a yes/no ("a minion may exert
to prevent this") is answered by the yes: the choice of who does it has its minimum read too (5_89). A text with two choices (one for its cost, another for its
effect, a card and then the bearer it is played on: Goblin Scavengers 1_179, or a bearer and then its own "when you play"
trigger's choice: Relentless Warg 17_89) has the minimum of each read, once;
a card played on a bearer has its bearer choice's minimum read even where that is its only choice (17_24, 18_26, 11_171).

**Say what the game is waiting for.** After a test's last action, assert the decision the game has come to rest
on: `AwaitingFellowshipPhaseActions()` and its kin, a named decision, a pending optional trigger, or
`GameIsFinished()`. In a phase both players act in, a Free Peoples action leaves the game on the Shadow player's
decision (`AwaitingShadowRegroupPhaseActions()`; pitfalls, "Both players act in most phases"). This is what catches a trigger left dangling or a response window nobody expected, and it is
the proof in a test that something did *not* happen. The conformance check also takes the phase read on its own
(`assertEquals(Phase.REGROUP, scn.GetCurrentPhase())`), which says less: it does not show that nothing is pending.
A test that makes no play and gives no answer after `StartGame()` (a standing modifier read in the fellowship
phase), or whose last rig call is a skip to a phase followed only by readings, owes none: there is no last
action for the game to have come to rest after. Adding one does no harm. What such a test may not do is read an
offer as ABSENT with nothing beside it that shows the player was being asked (the conformance check's `not-asked`
warning): put his `Awaiting...PhaseActions()` or his decision beside that reading.

**Expected values come from the text.** Work out what should happen, write it down, then run it. Do not run the
game and copy what it did. Where a number is a sum, the comment spells the sum:
`// 5 to play + 2 roaming + 4 tax`. The words the game uses to present a choice or an action ("Remove (4)",
"draw deck") are not expectations; take those from the game as they are.

**A reading that could not have come out otherwise shows nothing.** A before and an after that are equal, or an
assertion that would hold on that board whatever the card did, is not a check. Arrange the board so that the
right behaviour and the nearest wrong one give different readings. Where the card both takes and gives the same
amount on every board it can reach (Mordor Fiend 60_91, roaming, pays 5 and adds 5), the equal reading is a check:
its comment says what each wrong behaviour would read, and another board shows the cost by itself.

**One behaviour, shown once.** A positive and a negative case that the same board shows belong in one test.
Stages that build on each other belong in one test. Two tests that would pass or fail together are one test.

**A switch is shown once on each side.** A card that switches something on other cards (The White Council 101_18
and Saruman's Staff 4_174, on the first sentence of Saruman's text) is tested with ONE card that answers the
switch, and each card that answers a switch shows in its own tests that it does (Ketura on Q-0236: "we only need to
ensure that the *outgoing* mechanism has been enabled, and it's up to each of those individual cards to check their
side responds to the *incoming* mechanism"). shapes.md, "A ruling that a switching card has NO effect", has the
boards and the ledger's clause.

**Names say what happens.** `HealsOnlyAlliesWhoseHomeIsSite3`, not `TestTrigger` or `Site3Test2`.

**Known cards only.** Every card in a scenario is there for a reason, has a comment naming it, and has had its
definition read. Prefer cards with no text.

**Cards that exist.** A test shows what a card that exists does (Ketura, 2026-10-11: "We've got 3,000 cards that
exist, that's a big enough burden without adding cards that *don't* exist. Remember, we're trying to make the
cards robust, not *necessarily* the design language they're built in"). No card is invented: none with an id of
its own is added to any definition file for a test, and no definition text is stapled onto a card to stand in for
a card there is not. Where no printed card can make a board, the check is not shown, and Notes say what could not
be. A definition word no card uses is not tested; a word one card uses is tested as far as that card goes and no
further. The one exception is hers, and is a mechanic's and not a card's: a guard for a thing no card should be
able to do ("not being able to discard the One Ring, not being able to hinder Maps") is laid with the rig's ad-hoc
staplers (`AdHocEffects`) on a card with no usual text (Frodo, or a minion, by side), in the test itself, which
"establishes the artificial test nature of the card while also including its creation right there"
(`lookup.py Q-0241`). The one place definition TEXT is made up for a test is the account-name check (`PlayerIs`,
Race Text 94_5's): it is shown once, card-agnostic, in `mechanics/actions/PlayerIsRequirementTests`, the requirement built
from text written in the test and asked at a table seated under made-up names (Q-0271: C, "Yeah do C", on the
strength of Q-0190).

**A board a game could reach.** The cheats shorten a set-up; they do not make a board no game arrives at (Ketura
on Q-0213, 2026-10-10: `lookup.py Q-0213`). No card is given a culture or a race, and none a keyword that no
printed card of its kind has: the printed card that is the thing stands there (gemp-testing's `helpers.md`), and
where there is none the candidate is not asked for. A keyword or a number that only arranges the board (fierce
for a second skirmish, an archer, a strength that keeps someone alive) may be given by hand; a new test takes the
printed card that gives it where `helpers.md` has one. A terrain is a real site of that terrain in the path,
never a keyword given to a site. No more copies of a title in play than its dots allow, counted by title whatever
the subtitle, hindered copies included (Ketura on Q-0239: "no cheating in more copies of a card than their
uniqueness level allows"): never two cards of one unique title, never a third of a two-dot title, and a board that
needs more is no board (lint warns `copies` where a test's cheats, as far as it can follow them, lay one). Items are cheated only onto bearers
their text allows, one of each class to a bearer: for "bears 3 cards" or "each weapon", items of different classes.
No culture token is put on a card that nothing gives tokens to. A site of another block may stand in a path where
its own text is what the test reads. The tests that landed with such a board before the ruling are relaid by a
pass of their own (`orchestrator/engine-pass-24/relay-list.tsv`), not by whoever next opens the file.
WHEN a card is laid is another matter: cards that stand in play together later in the turn may be laid earlier in
it, so a minion cheated onto the table in the fellowship phase, to read a standing text on it before and after a
Free Peoples card is played there, is a shortening (Q-0228; Ketura, 2026-10-08: the cheats "short-circuit
more-or-less legitimate setups"). It is still no candidate for anything chosen or done in that phase (check `F`).

**Only the rig.** No `scn.game()`, `scn.gameState()`, `getModifiersQuerying`, raw `FreepsDecided("0")`, decision
parameters, reflection, sleeping or randomness. (The rig's own getters for a decision's bounds,
`FreepsGetChoiceMin()` and `FreepsGetChoiceMax()`, and for the number a question comes filled in with,
`FreepsGetChoiceDefault()`, are the rig and are fine.) An empty answer, `FreepsChoose("")`, is a raw
string too: `FreepsDeclineChoosing()` chooses nothing. `ApplyAdHocModifier` is allowed for arranging a board (a strength
the test needs, a keyword that printed cards of the kind have: "A board a game could reach"). Any other ad hoc effect (`carryOutEffectInPhaseActionByPlayer` among them) needs its
reason on the same line as a `// lint-ok: ...` comment, which the reviewer reads.

## Rulings, interactions and formats

`packet.py <id>` lists every individual ruling that names the card and every rules example that mentions it.
Each becomes an `R` clause and a test, unless it only restates the printed text (a "Clarification" whose wording
the spreadsheet already has): say so under `Sources:` instead. Rulings on other cards that only have this card's
title in their text (another Galadriel, "the Legolas Starter Deck"), and council pages about another card, come
under a heading of their own: read them for whether they bear on the card; they are not its rulings.

Format is mostly a matter of which decks may sit down at a table and seldom changes how a card plays, so tests do
not have to use the format a card was printed for. The exceptions are rules that a format switches on or off.
`references/format-rules.md` lists them and the wording that brings each into play; `packet.py` flags a card whose
text touches one. Such a card gets a `FormatRule` clause and is tested under both settings. The best-known case:
whether a skirmish involving the Ring-bearer can be cancelled depends on the format, so a card that cancels
skirmishes is tested with the Ring-bearer in the skirmish under a format that allows it and under one that does
not.

A card whose text names sites of a particular block is tested on that block's path. Each sentence of the card
that names a site number owes this for itself: on Hobbit Farmer 1_295 "your site 1" and "opponent's site 1" are
two filters, and the second wants the opponent's site 1 of another block under the fellowship (the second
player's Farmer on a Tower path: his action is not offered).

Which formats carry an erratum is deck validation, not play, and no card owes a test of it. Where a card's file
already has one (Final Strike 60_20, the only errata file that does), it stays there: an `R` clause of type
`FormatRule`, `+ -`, citing the format definitions, with one `Get...Scenario()` method per format that returns a
`DeckValidationScenario`.

The no-effect flag is not asserted for every card. It is tested as a mechanic (`mechanics/actions/NoEffectFlagTests`, the
common cases) and asserted in a card's own tests only in three cases: the definition declares `noEffectUnless` (a
convenience guard that was converted: Breaching Shot 102_21); the rulings log names the card for it (Trail of
Savagery 102_31); or the card has an ability or optional trigger with a cost and a limit on what its effect gives, which
therefore stays on offer at its limit (Ketura on Q-0041 and Q-0093; an `effectLimitPerPhase` or
`effectLimitPerTurn` field beside a `cost`). Such a card gets an `R` clause of type `Ruling`, checks `+ -`, citing Ketura's ruling of 2026-10-06 on
the no-effect flag, or on Q-0041 for a limit. `+` is the word read with `FreepsActionNoEffect` /
`ShadowActionNoEffect`: `"effect"` on each board the old guard shut, in the test that already takes the action
there; `"limit"` at the limit, in the test of the `L` check, which then takes the action. `-` is `"false"` on the
nearest board where the action does something: for a limit, before the first use. One assertion a test, one claim
line each; no new test is needed. A card whose tests merely happen to offer a flagged action asserts nothing about
it; neither does one with a limit on uses ("limit once per phase", a `limitPerPhase` or `limitPerTurn` field),
which is withheld at its limit and has no flag to read; and neither does one whose cap is written inside its
effect and not on the ability (a `CardPhaseLimit` or `CardAffectedLimitPerPhase` amount, `limitPerCardThisPhase`):
the game flags some of those at the cap and not others (hjson-reference, "Limits"), and which is the
mechanic's to show, not the card's. An ability
or optional trigger with no cost that is offered though it would do nothing is not a flag matter: by Q-0034 it
is withheld, and since Q-0224 the game withholds it wherever its effects can say that they will do nothing
(shapes, "An optional trigger or ability that costs nothing", has the kinds). The card's test reads the offer
absent on that board; a requirement is not written for it. Where the offer is still made, the effect is of a
kind the game cannot ask: that is a finding, which names the kind, and is corrected by a requirement as before. So is a "you may" with no cost of its own inside an
event's or ability's text that is asked when yes would do nothing: the event is still offered, the question
inside it is not (shapes, F-0379).

## When something is in the way

Stop work on the card, record what is in the way, and move to the next. Do not find a way round.

- **The rig cannot express a check.** Write a rig request (what the test needed, the signature proposed, the
  interface it belongs in) and leave the check untested. No helper method, no reaching past the rig, and no
  assertion that the rig or the game throws: a card test never relies on an exception (`assertThrows`, a `try`;
  Ketura, 2026-10-08). The check is written in the ledger as `waits <check>: G-0000 <what the rig lacks>`, and the
  card is parked on the request until the rig can show it.
- **The text and rules leave a real doubt.** Write the question for Ketura, with the two readings and what each
  would mean for the test, and put its id on the ledger's `Findings:` line. If an existing test already takes one
  reading, keep that test active: it is the best evidence there is of what was meant. (One the repository
  already had switched off with a bare `@Ignore`, and the game fails, cannot be: keep it line for line, parked
  on the question's number, beside the parked test of the other reading.) Otherwise, if one reading
  is clearly likelier, write the test for it and park it with `@Ignore("Q-0000: ...")`. Where the existing
  test's reading goes against the plain text, do both: the existing reading stays active and a test of the
  text's reading is written and parked, ready to go live on a ruling. The parked test may claim a check an active
  test already claims. **Parking is for a test the game fails**, and fails at a reading of the thing it is parked for:
  read that state before acting past it, or the test fails on the rig refusing a later answer and shows nothing of
  the finding (94_17). A test of the likelier reading that the game
  already passes stays active, with the question on the ledger's `Findings:` line: switched off, it would take
  away the one thing that notices a mistake there. A doubt that no reachable board can raise (two cards of one
  title, possible only with sets outside the campaign) is a line in Notes and not a question. So is a doubt the
  glossary settles once two entries are read together, or one whose two readings leave the players the same
  outcomes (an offer made or withheld is an outcome: Q-0034). A doubt that hangs on a question already open is a rider to that question, not a new one. It hangs on it only where that
  question's answer would settle it: the same kind of doubt about another card's sentence is reported as a question
  that names the open one, and the lander says whether it takes a number of its own (8_119 beside Q-0255).
  Where a general ruling in the log reaches the card by its own scope, the game departs from it on this card,
  and the card's old test asserts what the game does, it is still a question: the old test is evidence that the
  departure was meant. Park the ruling's reading, keep the old reading active, and name the other cards built
  the same way so that one answer covers them. Not so where shapes.md names the card ITSELF as built against the
  shape (Above the Battlement 7_262 under F-0222): that is a finding with its correction shown, the old reading is
  dropped, and the report says whose test read otherwise and when, for the landing to put to her (F-0465, Q-0254).
- **Only a card outside the campaign's order shows it.** A disagreement between game and text that takes a
  Hobbit-set card to show (those sets have no text sheet, and come last) is still a finding, filed against the
  card whose definition is at fault, with its test parked in the file of the card whose text it is. A mere doubt
  of that kind is a rider to the nearest open question, or a line in Notes.
- **The card turns on a PC mechanic that neither rules document covers.** What the card's own text says happens
  can be asserted. What the mechanic then *does* is a question for Ketura: park any assertion that depends on it
  with a `Q-`. (Hinder, restore, relentless and items are covered: `rules.py term hinder`.)
- **The game disagrees with the text.** Keep the test asserting what the text says. Park it with
  `@Ignore("F-0000: ...")`, and record the finding with what was expected, what happened, and which of these it
  looks like: a typo in the definition, an oversight in the implementation, wrong displayed text, an engine bug, a
  rig bug, a wrong spreadsheet row. Do not edit the definition or the engine.

A parked card is committed as far as it got and marked parked, with the id that is holding it. `@Ignore` stands
on a line of its own, above or below `@Test` (the tools that count and switch on parked tests look for it there, and
lint refuses it on `@Test`'s line). A parked test whose failure is that the game never returns is written
`@Test(timeout = 20000)` and keeps the limit once it is live (shapes.md, F-0322).

## Tests that are not about a card

Some behaviour belongs to the rules and not to any card: how fierce works, the Rule of 4, what happens to a
skirmish when a side is removed. A card may be how it was noticed, but the test is not that card's. These go in
`gemp-lotr-server/src/test/java/com/gempukku/lotro/mechanics/`, beside `cards/`, one class per mechanic, named for
it (`FierceTests`, `RuleOfFourTests`). The folder is sorted by subject into `keywords/`, `hinder/`, `choices/`,
`actions/`, `skirmishes/`, `costs/`, `wounds/`, `ring/`, `sites/`, `deck/`, `inplay/` and `game/` (engine pass 33,
package `com.gempukku.lotro.mechanics.<folder>`): a new class goes in the folder of its subject, and a new folder
is made only when none fits. They follow every rule above except the ledger; the class comment cites
the rules section instead. `lint.py --form <file.java>` holds such a class to those rules (how a test is written,
the `copies` warning included, with nothing of ledger, stats test or card), and with `--only <Test,Test>` a lone
test added to a card's file that has no ledger. The old tests under `at/` were rewritten into one place or the
other; the folder and its base class are gone (2026-10-10).

A test of the DEFINITION LANGUAGE (whether a definition is loaded or refused, what a blueprint keeps of a field:
a word the builder must refuse, a time that names no phase, a follower's Aid cost) plays no game and is no
mechanic test. It is plain JUnit in the server module, in
`gemp-lotr-server/src/test/java/com/gempukku/lotro/definitions/`, one class for each thing the language says,
named `...DefinitionTests` (Q-0233: C; `UnknownPhaseInADefinitionTests`, `PlayerIsRequirementDefinitionTests`).
It hands `LotroCardBlueprintLibrary.loadCardsFromFile` a few definitions written in the test itself (set-0 ids,
`0_9101` on) and reads what was kept; its helper methods may stay. What the same word then DOES in a game is a
mechanic test, in `mechanics/`, and each names the other in its class comment. `mechanics/` holds tests of the
game only. Such a class is for a word that a card which exists uses, as far as that card goes: a word no card
uses is not tested at all (Ketura, 2026-10-11; rulings log, "The campaign tests cards, not the definition
language").

A test of a server class that no player sees and no rule describes (the league-placement arithmetic, a decision
checking its own answer) is neither kind: it is plain JUnit beside the class it tests, in that class's module
(`RTMDGameInfoTest`, `MultipleChoiceAwaitingDecisionTest`), or, where it must have a game, in the server module
under the class's package and on the rig (`logic/GameUtilsAllPlayersTests`). One that needs the real card, format
or product library and no game (a league, a tournament, a merchant, a pack, a format definition) extends
`framework/LoadedLibraries`, which loads the three once. The rules above and `--form` are for
tests of the game (a class under `definitions/` and the rig's own tests under `virtualtable/` are not held to
them): only in a unit test whose subject IS a class's exception does `assertThrows` belong.

Where each kind is, and what runs it: a card's tests under `cards/`, a rule's under `mechanics/`, the definition
language's under `definitions/` and the rig's own under `virtualtable/` are what `cardtest.py suite` runs with no
word after it; everything else in the server module (leagues, tournaments, the hall, packs, formats, scenarios)
is run with them by `cardtest.py suite all` (gemp-testing, "Running tests").

What every card of a kind obeys is a mechanic and is not retested card by card: that exerting is not taking a
wound and a wound is not an exertion, that an exhausted character cannot exert, how a keyword works. So an
exertion is not owed as the nearest miss of a "takes a wound" or "is about to take a wound" trigger: it is shown
once, in `mechanics/wounds/AWoundThatIsNotTakenTests` (Q-0265: A); nor is a wound owed as the nearest miss of an
"each time X exerts" trigger: it is shown once, in `mechanics/wounds/AWoundIsNoExertionTests` (Q-0315: A). A keyword a card GRANTS ("Bearer is damage +1") is read on the
bearer and on a card outside the scope; what the keyword then does in a skirmish is the mechanic's (Ranger's Sword 1_112).

## Earlier iterations of PC cards

`Card_..._Tests.java.old` and its numbered kin hold the tests of superseded versions of PC cards, kept for a
future mode that shows how a card used to play. They are not compiled. When a live file is changed in a way that
would stop its dormant siblings compiling (a rig method renamed, say), make the same change in them. Do nothing
else to them.

## What a reviewer checks that the conformance check cannot

Run `lint.py <id>` first; it settles layout, the ledger's claims, coverage of listed checks, forbidden calls and
bookends, and that none of the ledger has leaked into the test file. Its warnings are yours to settle one by one:
`not-asked`, `not-in-hand` and `conditional` each mark a reading or an answer that may show nothing (shapes.md,
"lint's `not-asked`, `not-in-hand` and `conditional` warnings"). Then:

1. Is the ledger right? Each clause's type, and above all whether a check was left off that the wording calls
   for. Read the card, not the tests.
2. Does each test really show the check it claims? A `-` that is not the *nearest* miss, an `F` with one
   candidate, a `D` that only looks at one side of the boundary, all pass the conformance check. So does a
   card that is not unique and says "this condition" or "here" with no second copy in play in the test where
   it matters: the check warns (`second-copy`) only where no scenario has two aliases of the card at all.
   A comment that says a thing is "untouched" or "still there" with no assertion under it shows nothing either
   (lint does not read comments): take the words out, or add the reading where a rule or ruling settles it.
   Nor does lint see a pool read after a helper's play made only to meet a requirement: E2 takes it out (1_165, 9_47).
3. Would the test fail if the card were wrong? Imagine the filter one word wider and one word narrower, the
   number one off, the trigger on the neighbouring event. If the test would still pass, it is not testing that.
   Reading settles most of these (is there a candidate on the table that tells the two apart?);
   `mutate.py <id> --replace <old> <new>` tries one mistake where reading does not (sparingly: the run of
   stock mistakes, `mutate.py <id>` on its own, is no longer part of the campaign); and
   `cardtest.py probe <file.java>` runs a throwaway test from `/tmp` to see what the game really does.
4. Did an expectation come from the definition or from running the game?
5. Is any helper card doing something the test does not account for? Where a helper's own ability is what a
   test reads as NOT working (a question's reading included), a control shows it working without the card under
   test (Gwemegil 1_47's +1 against a minion that is no Troop of Uruk-hai, Card_01_143_Tests).
6. For a failure: is the test wrong, or is it a finding? Check the rules before deciding, and ask if unsure.
7. Does the definition say one thing twice? Where it repeats an effect in two branches (the same blocks under
   an `If`'s true and false), tests written from the text tend to hold one copy only. A mistake that survives in
   one copy is a gap. A second, near-identical test is right where the card's text gives the second copy a
   clause of its own; `mutate.py --replace old new --nth 2` reaches the second copy. Two triggers in one list
   that differ in a phase word are this too (60_20, "a maneuver or archery special ability"): each has its own
   invalid doer, in its own phase.
   Two guards of one thing in different places are this too (a `CanSpot self, zone(SUPPORT)` requirement beside
   `fromSupport: true`, Lost in the Woods 11_212): neither alone shows, so replace both in one mutate.py run.
   A choice whose filter has a branch of its own is this too: each branch's guard word is a filter word owed a
   board (Gwaihir 17_20's `and(wizard,canWound)` beside `and(not(wizard),canExert)`: an exhausted Wizard minion).
