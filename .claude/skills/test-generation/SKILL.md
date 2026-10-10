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

P1 Play       [+]          Played from hand for (4), to the support area.
     +  ->  CannotBePlayedUntilGandalfIsSpotted
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
  "and": left whole, a claim for either half satisfies the conformance check for both. Every word of the
  game text is in exactly one `C` clause, keyword lines included; reminder text in brackets is left out. The
  conformance check joins the `C` clauses and compares them with the definition's game text.
- **`P1` is the card being played.** Every companion, ally, minion, possession, artifact, condition and follower
  has one: the card is played from hand by legal play, and the test reads what it cost and where it ended up. An
  event is played by its own `Event` clause and needs no `P1`. Nor does The One Ring, which is never played (69_1).
- **`R` clauses are everything that is not printed on the card**: a ruling that names it, an interaction from a
  rules example, a rule that differs by format. Their text says where they come from.
- A clause line is `ID Type [checks] text`. Long text continues on following lines, indented.
- **Under each clause, one line per test that claims any of its checks**: `checks -> TestMethodName`. A test may
  claim checks of several clauses and a check may be claimed by several tests. Every check a clause lists must be
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
  all why a check the wording seems to call for was left off. Each may run onto following lines. Put `Sources:`
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
| `C-` | With a cost that cannot be paid, the action is not offered. |
| `F` | Who or what can be chosen or affected is exactly who the wording says. Valid candidates: at least one more than is needed, differing from each other in everything the wording does *not* mention (for "a Dwarf", or a keyword with no type word, "your archers", a companion and an ally: with no ally `all(your,archer,or(companion,minion))` passes, 94_13; for "a character" or "borne by a character", a companion, an ally and a minion, the three kinds of character: with no ally on the table `or(companion,minion)` passes, 61_100; for "each minion" or "another minion", minions of two cultures and two races: beside Orcs alone, `minion` narrowed to `orc` passes, 60_91; for a bare side word, "2 Free Peoples cards" from a deck, a card of each type the place can hold, a companion, a possession and an event: beside companions alone, `side(freeps)` narrowed to `companion` passes, 68_82; for a keyword or item class alone, "a pipeweed", "a pipe", cards of two cultures wherever printed cards have them: beside [shire] ones alone, `pipeweed` narrowed to `culture(shire)` passes, 92_4, and a card of the other culture whose own text stays out of the way is looked for before the culture is left out), all offered or affected. Invalid candidates: one for each thing the wording excludes, each failing *only* the thing it is there for, none offered or affected. For a phrase of two words ("a [sauron] minion") that is a card for each word: a [sauron] card that is no minion, and a minion of another culture; for an item class ("bearing a hand weapon") one of them is a weapon of another class on a bearer of the right kind (a ranged weapon), where a card in play can bear one, since the game has a `weapon` filter one word wider and a possession that is no weapon cannot tell the two apart (65_148); for a number with two bounds, a card on each side. For "an X skirmishing a Y" that is an X assigned to another Y whose skirmish has not begun, not an X fighting something else. For "the Ring-bearer" it is a Ring-bound companion who is not the Ring-bearer (Sam), not just any companion; a Ring-bearer who is not Frodo is no longer asked for on a card that says "the Ring-bearer" (Q-0213: A: no definition has ever written Frodo for him; the tests that have one stay, and no Frodo stands beside him, Q-0159: B). Where it is the text that names Frodo ("Bearer must be Frodo", "If Frodo dies") and a test wants a Frodo who is not the Ring-bearer, he is Frodo, Frenzied Fighter 13_149, the only Frodo who can be in play beside another Ring-bearer (51_313, 51_311; Q-0212). Where the text names its target more than once ("make that Dwarf A, or make that Dwarf B"), the candidate not chosen is read under each. Where the same phrase stands in several clauses, or in a condition or a trigger and not a choice ("a site bearing a weather"), each clause has its own invalid candidate. A spot requirement owes its valid candidates as a choice does, on one board or across the tests: for "spot an Elf companion", Elf companions of two titles, or `name(Legolas)` passes (91_12). Where a response picks what it saves from several cards affected at once, an invalid candidate is among them (for "your [sauron] condition is about to be discarded", a [sauron] possession in the same discard). Where a trigger names who did the thing ("exerted by a Free Peoples card", "uses a special ability"), one valid doer is neither the card itself nor its bearer, and one is no character where the wording allows (a possession's ability). Where the wording names the PLAYER who does it ("the Free Peoples player is about to discard", "discarded by an opponent"), one valid doer is that player acting by no card of his side (a site's text: Heights of Isengard 11_244), which for a card-worded text is an invalid one; and the other player made to do it by that player's card (Introspection 12_29) is on the table too, read as shapes.md has it (F-0296; Q-0171). A race against a culture, and a culture against a race, is shown with a PRINTED card that has the one without the other (Pippin, Trollslayer 103_31 is a [gondor] Hobbit; Sméagol, Once Like Us 103_116 a [shire] companion who is no Hobbit; Merry, Master Holbytla 103_85 a [rohan] Hobbit; Khamûl 103_61 a [raider] Nazgûl: gemp-testing's helper table has them by pair), never with an ad hoc modifier: no test gives a card a culture or a race (Q-0213: B). Where no printed card has the one without the other, no candidate is asked for at all: that is always so for Elf and [elven], for Dwarf and [dwarven], for a Free Peoples Wizard and for a Free Peoples Wraith. The slip itself (F-0019, and the reverse, F-0291) is read off the definition: lint warns `race-culture` where a definition goes by the race and its text names the culture, or the reverse, and that is a finding to file, not a board to lay. Nor is a keyword given to a card no printed card with that keyword resembles (an Orc made a Southron, an ally made valiant; Q-0213: C): where every printed card with the keyword is of one culture, race or type, that word cannot be told from the keyword, which is a line in Notes. So is a type word beside something only that type has (a signet, a timeword). Where no board can isolate an excluded thing ("your" tale, when the opponent's is inactive anyway), put the nearest candidate down and say so in Notes. Where the text picks cards out by title and a type word ("these characters: Celeborn, Elrond, Galadriel, Saruman"), an invalid candidate is a card of that title and another type (a follower named Saruman). Where the text names a title with no type word ("spot Saruman"), the valid candidates are cards of that title of each type and each culture there is (Saruman the minion and Saruman the follower, 17_38; Sméagol is [gollum] and [shire]: grep the definitions). That holds for a title that pays a cost or is acted on as it does for one spotted or played ("exert Gollum": each of the two Gollums, [gollum] and [wraith], pays in a test of his own, 60_20); lint warns `title-kinds` where every card of such a title that the file names is of one kind. Where a word is in one filter of the card and not in another ("a unique [elven] companion", then "all Free Peoples archers"), the valid candidates of the second vary in it. Where the effect picks its cards by something read from the card chosen for the cost ("exert a companion to heal another companion with the same signet"), the cost is paid in the tests by cards that differ in it, or a filter fixed to one value (`signet(aragorn)`) passes (92_32). Where the text names two places a card is taken from ("from play or from hand"), the candidates are laid out in each. An "or" inside the noun phrase ("a [gandalf] or [elven] event") is F's business, one valid candidate for each side of it, and needs no `B`. Where the card is not unique and its text says "here", "this condition", "this minion" or the like, a second copy is in play wherever it could change a reading: the test shows which copy a card went to or came from (`ShadowChooseOptionalTrigger(card)` where each copy makes its own offer), that a standing modifier tied to those words does not stack (a numeric modifier with no such words does stack, and two copies are no gap there), that a trigger fires once for its own copy. Where an effect acts on the card itself, or on "that <card>" a trigger has just named, another card of its kind stands beside it and is read as untouched (for "each time a companion is played, that companion ...", a companion already in play, or `all(companion)` passes, 91_9); where a second copy cannot change a reading, the untouched control is a different card. What the wording does not mention includes whose a card is and where it stands: for a bare race word ("a Man", Q-0058) one valid candidate is the opponent's, where a card of his of that race can be in play at that moment (no minion is in a fellowship phase: then Notes says none can be, and no board is made for it) (but a player's own Shadow cards are inactive on his turn, so for a "Shadow condition" chosen by a Free Peoples ability they are no candidates at all); for "a minion" or "an Uruk-hai" on a Skirmish event, one valid candidate is not assigned to a skirmish. Where the excluded thing is part of a condition inside a modifier ("in a skirmish involving an [isengard] minion"), the candidates are in skirmishes, not merely on the table. For "bearer" or "self" with a requirement ("if bearer is an archer"), the nearest invalid candidate is another card that meets the requirement. That holds where the requirement is the condition of a trigger and not of an ability: for "While Boromir is the Ring-bearer, at the start of each skirmish involving him" it is the real Ring-bearer in a skirmish of his own while Boromir is a plain companion (59_31). A title that has one card type only ("spot Gandalf") needs one card of the title, and its invalid candidate is a card of that culture with another title. Never put two cards of one unique title in play at once, whatever their types (Ketura, 2026-10-08: the cheats "short-circuit more-or-less legitimate setups"; Elrond the ally and Elrond the companion each get their own board). Before calling a candidate impossible to put on a table, look for a way: an ally's home may be on another block's sites (Gondorian Merchant 7_97, the only [gondor] ally, is at 3T). |
| `D` | The effect is still in force just before the boundary the text names and gone just after. "Just before" is in the phase that precedes the boundary in a full turn, with the thing the effect touches going on: for "until the regroup phase", during a skirmish that follows one already resolved, and then at the first regroup decision. An effect that holds a shorter-lived part inside it (a trigger lasting until regroup that limits something phase by phase) is read across two phases, or "one in all" and "one in each phase" cannot be told apart. |
| `L` | A limit holds: it works up to the limit and does nothing beyond it. Which reading shows it goes by the kind of limit (Ketura on Q-0093). A limit on uses ("limit once per turn", "limit once per phase", "limit N times") withholds the ability at its limit, cost or no cost: the test reads it not offered, with its cost still payable and something on the board the effect could still have acted on. A limit on what the effect gives ("(limit +3)", or a bare number of things added: "to add a threat (limit 1)") leaves an ability or optional trigger that has a cost on offer at its limit (Q-0041): the test takes it once more and reads the cost paid and everything else as it was, with something on the board the effect could still have acted on. With no cost, and for a required trigger, it is not offered or does not happen under either kind. Where a limit caps what an X buys ("discard X cards to make … strength +X (limit +3)"), X itself is not capped: a test shows it, with one use beyond the cap or by reading the choice's maximum. A cap inside an effect ("takes no more than 1 wound") is a limit too; one that renews (each phase, each turn) is shown renewing. |
| `O` | "May" asks, and declining leaves everything as it was. Listed when the text says "may". |
| `S` | A value that depends on a count is right at two different counts, and at its cap if it has one. Where the count is an X the player chooses, zero is one of the counts where the action still does something at zero (One Dwarf in Moria heals whatever X is). Not where X is a number of exertions, and not where X of zero would do nothing at all: such a use is deliberately not offered (rulings log, Q-0015 and Q-0034: on an ability or optional trigger with nothing else to pay; an event is offered whatever its X can do, and no test turns on its X of zero), and the `-` is that it is not. Where X is a number of cards moved or tokens spent, and a board can arise on which what was paid and what the board lost are different numbers (a bearer who dies takes his other items with him), one test uses that board. |
| `R` | "Each time" happens a second time, on the same subject where the wording allows: two wounds on one minion tell "for each wound" from "for each minion wounded", which two minions wounded once cannot. |
| `B` | Each alternative of an "or" or "choose one" is taken in some test. Where a board that could arise makes one alternative impossible to carry out in full, a test shows the other is then what happens: a player may only choose what can be fully performed. |
| `Z` | Cards that move end up where the text says: attached to the chosen bearer, stacked, in the dead pile, on top of the deck. For a destination the text names. Where a card arrives somewhere and is taken out again in one effect, it is read there in between. A plain discard is read where it is paid or done, under `C` or `+`, and does not call for `Z`. |
| `K` | The keyword, with its number if it has one, is on the card. Claimed by the stats test (`K -> ElrondStatsAndKeywordsAreCorrect`). |

Which checks a clause lists follows from its words, and the analyst is expected to justify leaving one out:

- anything before "to" in "X to Y" is a cost → `C`, and `C-` where the board can make it unpayable. A card
  discarding itself as its only cost ("discard this condition to …") can always pay: no `C-`, and no `no C-:`
  line. An exert cost's `C-` board is the one who must exert, exhausted; where the cost names a kind ("exert
  your [isengard] Man"), the only one of that kind exhausted with others able to exert beside him. A board with
  nobody of the kind shows the filter (`F`) and cannot tell an exertion from a wound (92_2). An "add (or remove, or reinforce) a [culture] token" cost: Redhorn Pass 13_194 ("Culture tokens cannot be
  added, removed, or reinforced") is its `C-` board (Scouring of the Shire 68_112; gemp-testing's helper table).
  An "add a burden" cost: no printed card stops a burden being added, but a hindered Ring-bearer does
  (CR 5.0 "hinder"), so its `C-` board is `scn.HinderCard(<the Ring-bearer>)` (103_115). Where the card must
  itself be the Ring-bearer to pay (Sméagol 59_30's regroup ability), hindering him shuts its text off and no
  board in play is left: a `no C-:` line. So too where the burden is the price of preventing something done to
  the Ring-bearer (an assignment to him, 94_26): hindered, he cannot be assigned, and the prevention is never put
  (Q-0114). Test Seal 0_8004 is the mechanics' card only (Q-0166: A). A hindered Ring-bearer is not spotted
  either: where the action also spots or counts him, the board keeps that met without him (Get On and Get Away
  54_304: Sméagol beside a hindered Galadriel)
- a noun phrase that picks things out ("a Dwarf", "each [moria] Orc", "another companion", "bearer") → `F`
- "until", "this phase", "this turn" → `D`. A phase ability that changes something and names no duration has
  one by rule (CR 4.2 "phase action": it lasts for the phase it is used in) → `D`. So has a trigger's effect:
  by Ketura's ruling (Q-0018) it lasts to the end of the current phase → `D`. Each skirmish is a phase of its
  own (CR 4.2 "skirmish phase(s)"): such an effect from one skirmish is gone in the next
- "limit", "once per", "no more than", "up to", "at most" → `L`
- an optional trigger or a special ability with no cost (a spot is no cost) → one `-` is the board on which its
  whole effect would do nothing: not offered (Q-0034; shapes, "Spot X to <effect>"). With a cost it is offered
  there and used for nothing, which is read under `F` or `+` as for an event, and is no `-`. A play of a card after
  the "to" is itself a cost (Q-0062): "add a burden to play a [shire] tale" with no tale in hand that can be
  played is not offered, and that board is its `C-`
- "may" → `O`. A response or any other special ability is the player's to use or not without the text saying
  so; list `O` for one only where declining has a consequence worth showing
- "for each", "X" → `S` (and for an X, the zero case)
- "each time" → `R`
- "or", "choose one" → `B`
- "make each…", "make all… until…" on an event or a special ability → a `Rule` clause citing CR 4.2 "event"
  (some effects affect only what was there when they were used): a test that a card which becomes one of them
  afterwards is not affected. The game writes this into each definition, not into the engine, so it is each
  card's to show. (For an EVENT it is the rule. For an ability of a card in play Ketura has ruled it card by card: a
  snapshot where the cards it names are on the table when it is used, the standing filter where its text could do
  nothing as a snapshot, as Not Feared in Sunlight 52_107, F-0217: `lookup.py "make each"`.)
- "lose…", "cannot…" for a time → a `Ruling` clause: "can't" beats "can", so what was lost is not given back
  by another card before the time is up. Where the card's own text says so ("loses fierce and cannot gain fierce
  until ...", Prized Lagan 68_110), the "cannot gain" half may stand as an `Effect` clause of its own listing
  `+ - D` in place of the `Ruling` clause; the tests owed are the same (a gain refused before the time, allowed after)
- a search of a draw deck ("from your draw deck", when the whole deck is looked through) → a `Rule` clause citing
  CR 4.2 "draw deck", whether or not the card says "shuffle": `+` is one more shuffle of that deck (or two: see
  gemp-testing) and none of the other player's, read with the shuffle count; `-` is no shuffle on any alternative
  that does not look through the deck ("or discard pile"), and none when the player declines before looking. Like
  the snapshot, this is a rule tested card by card because the game writes it into each definition
- "reveal" → the card is shown to the other player, who has a decision to dismiss. One test reads the showing
  for each player (the shown card listed and not selectable, a card not revealed absent); the others only
  dismiss (the dismiss methods fail the test when that player is being shown nothing). A reveal from a hand is
  shown to the other players only: assert once that the hand's owner is shown nothing. A card taken from a draw
  deck by its type, where the text does not say "reveal", is shown to nobody and named in the game log (Q-0025):
  one test of the taking marks the log and reads that the cards taken are named and one left in the deck is not
  (gemp-testing, "A card made known by the game log alone")
- a threshold ("if you can spot 3…") → `+` at exactly the threshold and `-` one short of it, with something
  beside the short count that nearly counts (a third ring of another culture)
- "play…on", "stack", "place…in the dead pile", "put…on top of" and the like → `Z`

### Clause types

The type says what kind of statement the clause is, and sets the checks it must list at the very least.

| Type | The clause is | At least | Notes |
|---|---|---|---|
| `Play` | the card being played from hand (`P1`) | `+` | Twilight moves by the cost (plus roaming for a minion, and say so in the comment); the card ends in play, in the support area, or on its bearer. May share a test with a `ToPlay` or `PlayOn` clause. The sentence "Plays to your support area." is a `C` clause of this type, claimed by `P1`'s test, which reads the card in the support area and on no character. |
| `Keyword` | a keyword line: "Ranger.", "Damage +1.", "Fierce." | `K` | How the keyword works is the rules' business and is tested once, as a mechanic. A keyword the text grants under a condition is a `Modifier`. A keyword the card has by rule and not in print (every Sam is Ring-bound) is an `R` clause of this type. A follower's "Aid - (N)." is of this type, its reminder in brackets left out; the Aid's cost is an `aidCost` effect and no field or keyword the stats test can read, so `K` is claimed by the stats test for its header and by a test that takes the Aid and reads the pool N higher (Robin Smallburrow 68_111). |
| `ToPlay` | "To play, spot…" | `+ -` | Show the change: not playable, then one legal play later, playable. |
| `ExtraCost` | "To play, exert…" / "add a burden" | `+ - C` | `-` is the cost being unpayable. A cost another player must pay to do something with this card while it is in play ("The Free Peoples player must add a burden to assign this minion to …") is a `Modifier` that lists `C` and `C-` as well (69_38). |
| `PlayOn` | "Bearer must be…", "Plays on…" | `F` | Two valid bearers offered, an invalid one not. |
| `Activated` | "Phase: cost to effect." on a card in play | `+ -` | For `-`, prefer in this order: a requirement in the text unmet; the cost unpayable; a phase the text does not name. Where the text has no requirement, the test of the unpayable cost claims `-` and `C-` both. The phase is the next one in which that player is asked for actions, not any phase: for a Maneuver event, the archery phase; for a Fellowship action, the maneuver phase with a minion laid so that there is one (the Free Peoples player is asked nothing in the Shadow phase). |
| `Event` | the text of an event card | `+ -` | `+` includes the event leaving the hand. `-` as for `Activated`. An event's target is not a requirement: a board with nothing to target is read under `F` or `+` (the event is played for nothing, CR 4.2 "effect"), not as the `-`. |
| `Effect` | a further sentence of an event or special ability, with no phase, requirement or cost of its own ("Then…", "If you do, …", a second sentence) | `+` | Its `-` is the first sentence's. It lists whatever else its own wording calls for. A rider "The Free Peoples player may X to prevent this." is of this type and lists `+ O C C-`: `+` is the thing prevented with the first sentence's cost still paid (CR 4.2 "cost"), `O` the other player declining, `C` and `C-` that player's cost. |
| `Trigger` | "Each time…", "When…", "At the start of…" | `+ -` | `-` is the nearest event that should not set it off. Where the trigger names a phase ("if the fellowship moves during the regroup phase"), that is the same event in another phase of the same turn; this holds for a response event as well. |
| `Response` | "Response: If…" | `+ -` | `+` shows the response offered and its effect; for a prevention, that the prevented thing did not happen. |
| `Modifier` | a standing effect: "Bearer is strength +2.", "While…, …" | `+ -` | `-` is the condition not holding, or a card just outside its scope. Read the value with and without. A modifier on the card itself or its bearer still owes F's other card of its kind, one that meets the condition and is read as untouched. Where two sentences between them take in every card of a kind ("each of your minions is +1 ... each of your opponent's minions is -1", 94_23), no card is outside both: each sentence's `-` is the other's card, read at the other's number, and the printed number is the control. |
| `Restriction` | "may not", "cannot" | `+ -` | `+` is the forbidden thing refused; `-` is the same thing allowed on the same board without this card. Without that control the refusal proves nothing. Others being offered in the very choice that leaves this one out is a control too, provided nothing else about them explains the difference. |
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
Where the reason, or a line in Notes, is that no printed card makes the board, it names the search of the
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
signet, site number or home, and each keyword with its number. A site's fields are its block (`getSiteBlock()`),
its Shadow number (`getTwilightCost()`), its direction, and its site number, which a Shadows-block site does not
print (`getSiteNumber()` is 0 there). Its header comment reproduces the card. The
expected values are the sheet's; `reconcile.py <id>` (Decipher cards) and the packet's "Sheet against
definition" section (every card) show any field where the sheet and the definition disagree (see "When the sheet
and the definition disagree" above). A strength, vitality or other bonus printed on a card that is borne (a
possession's +2) is a printed field: asserted here, and read on the bearer in the test of the Play clause, with
no clause of its own. Timewords are not a printed field: they are how a
definition says when an event may be played, and a definition may carry more of them than the card prints.
A Race to Mount Doom modifier's intensity is one: its header has an `Intensity:` line and the test reads
`getBlueprint().getIntensity()`, at the definition's value (no sheet has these cards); lint asks for both.

On a PC card the header comment already in the file often describes an earlier version of the card, above
assertions that are current. Rewrite it from the packet; never keep it on trust. The V-set and errata sheets have
no resistance column, so resistance is asserted at the definition's value. No sheet has a site's direction
either: assert the definition's value, and on an erratum compare it with the original's `direction:`; where the
two differ it is a finding, and the two card images say which was printed (Doorway to Doom 68_134, F-0302).
A definition with no `direction:` line (every Hobbit-set site) reads null: assert `assertNull(getSiteDirection())`
until the card is transcribed from its image (`Card_30_051_Tests`).

## Rules every test follows

**Its own table.** A test's first statement gets a table from a `Get...Scenario()` method. Tests share nothing.

**Arrange by cheating, act by playing.** Put the board where it needs to be with the rig's cheats, then do the
thing under test the way a player would. Never cheat the thing under test, and never play out something that is
only set-up. Where a clause counts something ("strength +2 while he bears 3 items"), changing the counted thing
is part of the act: do it by legal play in one test, which shows the modifier switching on as the count is
reached, and cheat it in the others.

**Read before and after.** An assertion after an action means little without the same reading before it. If an
ability exerts, read 0 wounds, act, read 1. Where the amount is not in the wording ("heal a minion", "wound a companion"), the target
carries more than the effect should change: one wound healed from two reads 1, which "heal two" would not.

**Give choices something to choose from.** One more valid candidate than the choice needs, and an invalid one
beside them (see "One candidate is chosen for you" in gemp-testing). Assert the offer with
`FreepsHasCardChoiceAvailable(...)` and `FreepsHasCardChoiceNotAvailable(...)` before choosing. Where the text
says neither "may" nor "up to", read the minimum of one such choice (`FreepsGetChoiceMin()`): a choice that has
wrongly become optional looks the same otherwise. A text with two choices (one for its cost, another for its
effect) has the minimum of each read, once.

**Say what the game is waiting for.** After a test's last action, assert the decision the game has come to rest
on: `AwaitingFellowshipPhaseActions()` and its kin, a named decision, a pending optional trigger, or
`GameIsFinished()`. This is what catches a trigger left dangling or a response window nobody expected, and it is
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

**Names say what happens.** `HealsOnlyAlliesWhoseHomeIsSite3`, not `TestTrigger` or `Site3Test2`.

**Known cards only.** Every card in a scenario is there for a reason, has a comment naming it, and has had its
definition read. Prefer cards with no text.

**A board a game could reach.** The cheats shorten a set-up; they do not make a board no game arrives at (Ketura
on Q-0213, 2026-10-10: `lookup.py Q-0213`). No card is given a culture or a race, and none a keyword that no
printed card of its kind has: the printed card that is the thing stands there (gemp-testing's helper table), and
where there is none the candidate is not asked for. A keyword or a number that only arranges the board (fierce
for a second skirmish, an archer, a strength that keeps someone alive) may be given by hand; a new test takes the
printed card that gives it where the helper table has one. A terrain is a real site of that terrain in the path,
never a keyword given to a site. Never two cards of one unique title in play. Items are cheated only onto bearers
their text allows, one of each class to a bearer: for "bears 3 cards" or "each weapon", items of different classes.
No culture token is put on a card that nothing gives tokens to. A site of another block may stand in a path where
its own text is what the test reads. The tests that landed with such a board before the ruling are relaid by a
pass of their own (`orchestrator/engine-pass-24/relay-list.tsv`), not by whoever next opens the file.

**Only the rig.** No `scn.game()`, `scn.gameState()`, `getModifiersQuerying`, raw `FreepsDecided("0")`, decision
parameters, reflection, sleeping or randomness. (The rig's own getters for a decision's bounds,
`FreepsGetChoiceMin()` and `FreepsGetChoiceMax()`, are the rig and are fine.) An empty answer, `FreepsChoose("")`, is a raw
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

A card whose text names sites of a particular block is tested on that block's path.

Which formats carry an erratum is deck validation, not play, and no card owes a test of it. Where a card's file
already has one (Final Strike 60_20, the only errata file that does), it stays there: an `R` clause of type
`FormatRule`, `+ -`, citing the format definitions, with one `Get...Scenario()` method per format that returns a
`DeckValidationScenario`.

The no-effect flag is not asserted for every card. It is tested as a mechanic (`mechanics/NoEffectFlagTests`, the
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
should be withheld, and offering it is a finding.

## When something is in the way

Stop work on the card, record what is in the way, and move to the next. Do not find a way round.

- **The rig cannot express a check.** Write a rig request (what the test needed, the signature proposed, the
  interface it belongs in) and leave the check untested. No helper method, no reaching past the rig, and no
  assertion that the rig or the game throws: a card test never relies on an exception (`assertThrows`, a `try`;
  Ketura, 2026-10-08). The check is written in the ledger as `waits <check>: G-0000 <what the rig lacks>`, and the
  card is parked on the request until the rig can show it.
- **The text and rules leave a real doubt.** Write the question for Ketura, with the two readings and what each
  would mean for the test, and put its id on the ledger's `Findings:` line. If an existing test already takes one
  reading, keep that test active: it is the best evidence there is of what was meant. Otherwise, if one reading
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
  outcomes. A doubt that hangs on a question already open is a rider to that question, not a new one.
  Where a general ruling in the log reaches the card by its own scope, the game departs from it on this card,
  and the card's old test asserts what the game does, it is still a question: the old test is evidence that the
  departure was meant. Park the ruling's reading, keep the old reading active, and name the other cards built
  the same way so that one answer covers them.
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
it (`FierceTests`, `RuleOfFourTests`). They follow every rule above except the ledger; the class comment cites
the rules section instead. `lint.py --form <file.java>` holds such a class to those rules (how a test is written,
with nothing of ledger, stats test or card), and with `--only <Test,Test>` a lone test added to a card's file
that has no ledger. The old tests under `at/` are rewritten into one place or the other.

A test of a server class that no player sees and no rule describes (the league-placement arithmetic, a decision
checking its own answer) is neither kind: it is plain JUnit beside the class it tests, in that class's module
(`RTMDGameInfoTest`, `MultipleChoiceAwaitingDecisionTest`), or, where it must have a game, in the server module
under the class's package and on the rig (`logic/GameUtilsAllPlayersTests`). The rules above and `--form` are for
tests of the game: only in a unit test whose subject IS a class's exception does `assertThrows` belong.

What every card of a kind obeys is a mechanic and is not retested card by card: that exerting is not taking a
wound and a wound is not an exertion, that an exhausted character cannot exert, how a keyword works.

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
3. Would the test fail if the card were wrong? Imagine the filter one word wider and one word narrower, the
   number one off, the trigger on the neighbouring event. If the test would still pass, it is not testing that.
   Reading settles most of these (is there a candidate on the table that tells the two apart?);
   `mutate.py <id> --replace <old> <new>` tries one mistake where reading does not (sparingly: the run of
   stock mistakes, `mutate.py <id>` on its own, is no longer part of the campaign); and
   `cardtest.py probe <file.java>` runs a throwaway test from `/tmp` to see what the game really does.
4. Did an expectation come from the definition or from running the game?
5. Is any helper card doing something the test does not account for?
6. For a failure: is the test wrong, or is it a finding? Check the rules before deciding, and ask if unsure.
7. Does the definition say one thing twice? Where it repeats an effect in two branches (the same blocks under
   an `If`'s true and false), tests written from the text tend to hold one copy only. A mistake that survives in
   one copy is a gap. A second, near-identical test is right where the card's text gives the second copy a
   clause of its own; `mutate.py --replace old new --nth 2` reaches the second copy. Two triggers in one list
   that differ in a phase word are this too (60_20, "a maneuver or archery special ability"): each has its own
   invalid doer, in its own phase.
