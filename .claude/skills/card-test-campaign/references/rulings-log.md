# Ketura's rulings

What Ketura has said about how the game is to be read and how the campaign is to be run, as she said it. This is
the first thing an expectation may rest on, ahead of the Comprehensive Rules and the card's text. Add to it
whenever she answers a question; do not paraphrase an answer into something broader than what she said.

Each entry: the date (at its start, or in brackets at its end), what was asked or what it was about, what she said, and which cards it bears on.

The entries are grouped by subject. `rulings-index.md` beside this file lists every entry by its first words and the numbers it settles; `scripts/lookup.py "<words or a number>"` prints whole entries. A new ruling goes under its subject, with its date; then run `scripts/lookup.py --index` to rebuild the index.

## How the campaign is run

**2026-10-04. Gaps in what a test can do.** They are added to the test rig, never hidden in one card's suite as
ad hoc test functions.

**2026-10-04. Candidate bugs.** Park them for her to rule on rather than fixing them. "Most HJSON bugs have
historically been correctly identified by you, but sometimes you take a correct implementation with an obscure
ruling as an incorrect implementation." Once it is clear how often that happens she will have them fixed without
parking.

**2026-10-04. Questions.** Ask whenever there is one; do not guess at niche rulings or historical minutiae.

**2026-10-04. Earlier iterations of PC cards.** Every iteration of a PC card is kept, with a view to a future mode
that shows how a card used to be handled. Their tests are kept in step with the live ones but stay switched off
until that mode exists.

**2026-10-04. Errata with no tests (F-0006).** On some errata, repeat errata especially, the tests were
commented out rather than written, under time pressure. They are to be written.

**2026-10-04. Race to Mount Doom.** Sets 91 to 94 are within the audit. Set 90's cards are purely visual, with
no text, and need no tests.

**2026-10-04. The Hobbit sets (30 to 33).** Left until very last: there is no text for them, their definitions
are more abbreviated and less structured because the PC does not manage them, and they would have to be
transcribed from the card images first.

**2026-10-04. The ledger.** She does not want the ledger block in each card's test file: "It's encoded and
esoteric; presumably this is used by the author/reviewer, but it should be elsewhere and not in the final
product." It is kept in a file of its own outside the tests.

**How the campaign is run from here (Ketura, 2026-10-06, replacing the earlier rules they touch).**
- *Rig additions need no approval.* "If you're adding a feature to the test rig ... you can add it. The only
  consideration I want you to do is to only do so in a case where there is actual use; no adding features if you
  don't have an existing practical use case for them. Also inform me after the fact in rig-requests, but they no
  longer need to be proposals, just informationals." An author still does not edit the rig: a rig need is
  reported and the orchestrator has it built.
- *The stock mutation run is cut.* "Let's go ahead and cut the remaining mutation step." Authors no longer run
  `mutate.py <id>`. What stays: one `mutate.py --replace ... --with-parked` to show that a parked test passes
  under the correction its finding proposes.
- *One commit for each batch, after its review.* "Let's reduce the frequency of commits to once per agent per
  batch, happening after the reviews have completed."
- *Reports to Ketura are short:* the Q-, F- and G- items that need her for the work to go on, and lists of the
  cards finished and in progress. *[2026-10-06, evening]*

**Findings are fixed without waiting.** "Each of the F- issues that have been brought up have overwhelmingly
been actual issues. Continue to give me a breakdown of fixes being made so I can intervene, but you don't need
to wait for my approval to fix cards; the text should do what it says it does, and I think you only need to
ask if you find an obvious intentional break from the text rather than what appears to be an error or typo.
Or if you are uncertain, of course." Findings are tracked as `fixed` (not yet looked at by her) and
`fixed-approved` (she has said so). *[2026-10-07, later afternoon]*

**Every fix through F-0155 is approved** (Ketura, 2026-10-07): the findings fixed under a like ruling or
under the new rule up to that number are `fixed-approved`. *[2026-10-07, later afternoon]*

**Approvals of 2026-10-08.** Every finding fixed without her word up to F-0195 is fixed with approval (2026-10-08), and every card whose
tests have nothing parked is approved. *[2026-10-08]*

**Code review:** "On RingBearerRule, the private filter is not the way to go. Instead add it to be a sibling of
Filters.ringBearer." Done: Filters.theOneRing (f7dfee394). Card-type filters a rule needs go in Filters, not in
the rule. *[2026-10-08]*

**Landing is a stage of the pipeline, and reports name numbers only (Ketura, 2026-10-08, evening).** A lander
agent follows the reviewer: it files and numbers what the batch found, applies the corrections, works the
documentation and standard issues into the skills at once, and commits. "Ideally it's wrapping and handling all
the updates itself, and then you only have a very few things to put in here so your context doesn't bloat.
Ideally you don't transcribe things that are in files I can read; just listing the Q- or F- or G- id's for me to
go look up is sufficient." The tracker is refreshed at every landing.

**After the errata: Race to Mount Doom lightly, then the `at` tests, then the Decipher sets (Ketura, 2026-10-08,
night).** On RTMD: "These tests were done relatively recently, and so less effort should be required. I think that
formatting and such may still be required, but these three sets should overall require less attention." On the
`at` tests, which come next and before the Decipher sets: "we're going to tackle the `at` tests and get those all
converted and in place, either in the appropriate mechanical folders or within the cards they are most associated
with. These tests are going to be a bit gnarly and opaque, so it may make more sense to just note what bug or
behavior or interaction the test is handling, and then take that as a design directive when instantiating a
wholesale new test." So an `at` test is not ported line by line: what it guards is written down, and a new test is
written from that, in `mechanics/` or in the file of the card it is most about.

## Sources, sheets, revisions and errata

**2026-10-04. Comprehensive Rules 4.2** should contain all the official rulings.

**2026-10-04. The reviewed spreadsheet** (`gen_en_standard.tsv`) is the combination of at least two card
databases and has been combed for errors since. It is what the card images are regenerated from, and it was the
input from which the Decipher definitions and their stat tests were first generated.

**2026-10-04. The generator sheets.** `V1.tsv`, `V2.tsv` and `V3.tsv` are the V-sets; `master_errata.tsv` was put
together from the twenty or so sheets used over the years. "Many times there were errata *candidates* which did
not go out, or which were delayed between releases, and so the information here is likely only about 95%
accurate. If an agent comes across a discrepancy, let's flag it but it may not necessarily be an error." The
latest revision of a V-set card may be in the V-set sheet or may be counted as an erratum.

**2026-10-04. Comprehensive Rules 5.0 excerpts** have the entries for hinder and the other PC-specific mechanics.

**The Rules Councils' own rulings.** Ketura has supplied the eight Legacy Rules Team rulings (Decipher's cards)
and two Modern Rules Team rulings (the PC's) made since CR 4.2. `rules.py council` lists them and
`rules.py card` and `rules.py grep` search them. Look there before asking: Q-0016 had been ruled already (LR 6).
Their status lines are not all current (MR 1 and MR 2 say "Discussion" above a dated decision); a page with a
decision section is decided. *[2026-10-05, afternoon]*

**2026-10-05. What a card looks like today.** "The URL of the card can be pulled to inspect the actual text on
the card image used today as a further piece of information."

**Sheet discrepancies, card by card (2026-10-05).** Which side was right, card by card. Where the definition was wrong it has been corrected (findings F-0024 to
F-0030); where the sheet was, the difference is in `known-differences.tsv` or was a matter of revisions.

- **51_245 Desperate Measures:** "top 8" is correct.
- **52_45 Too Much Attention:** "top 5" is correct.
- **57_190 Morgul Destroyer:** +3 is correct.
- **60_11 Galadriel, Lady Redeemed:** the text should have the starting fellowship discount; it was not removed.
- **68_134 Doorway to Doom:** "2", not "two". "Game text should always use numerals where possible for space
  reasons, particularly on sites like this."
- **102_15 Watching and Waiting:** "remove (3)" is correct.
- **53_106 Bill the Pony:** should have the Pony item class; it was added to the engine and never put on the one
  card that has it.
- **54_245 Southron Archer:** no limit is correct; it was made unique instead of having its text altered.
- **102_75, 102_76 (the maps):** they have no culture; they are neutral.
- **The Bladetusk cards (103_41 to 103_43):** printed with a strength and vitality "rather than cram even more
  game text in", but "from a mechanical perspective it doesn't gain them until the text is triggered, as
  implemented."
- **Revisions.** Several differences were one revision set beside another: 53_120 ("heal a companion" is revision
  2, the most recent), 58_57 ("assigned to a skirmish" is revision 1; the definition's text looks like the base
  card's; revision 2 is different again), 60_17 ("twice to discard 3" is revision 1, "hinder" is revision 2),
  51_80 (revision 1 had vitality 2, revision 2 has 4), 53_103 (revision 3 is Maneuver, revision 4 Response),
  101_26 and 101_28 (revision 0 and revision 1; 101_28 was Maneuver for 1 and is Shadow for 0), 102_51
  ("remove 2 tokens" is revision 1). "Make sure this check is lining up revision numbers."
- **Rath Dinen** is in the errata sheet as V0U1.0. **Forth the Three Hunters!** was printed by Decipher three ways
  with the same collector's info, one in each culture, identical otherwise; "a perpetual headache in all systems
  that needed a way of handling their unique ID situation."
- The V2 errata were missing from the master sheet and have been added (most add Valiant to various Elves).

**The sheets (2026-10-05, evening).** 58_57 Corsair Marauder: the sheet's row is the revision 1 that was
released, and the definition "is an old candidate and needs altered to match the sheet." 67_7: helper text
"should be removed from the game text (so it isn't interpreted as a secondary cumulative effect). In print those
are italicized and ignored." Of the wording-only differences, 60_8 keeps its wording ("so players right-clicking
on it see the rounding-down ruling"), 53_17 is revision 2 with revision 1 missing from the file, and "all other
cards in this section should be altered to use the wording in the sheet." `PC_Cards.js` shows which revision's
image is in use and is one more source. On revision markers: a later revision was copied from the one before,
so a live block mislabelled with the earlier number is the common fault; look in the comments for the old
revision before concluding anything. *[2026-10-05, evening]*

**Corsair Marauder (58_57) and the history of a card's revisions.** "The current implementation is good, it just
needs to have its revision number updated." The earlier implementation "should be put into a comment above it in
the HJSON, as should any of the other missing revisions, so that the file has a one-stop-shop for history." So
an errata definition file carries every earlier revision of a card as a commented-out block above the live one,
oldest first, and the live block's `revision:` is the number of the revision released. *[2026-10-05, night]*

**2026-10-04. Reprints keep signets (F-0002).** Boromir, Defender of Minas Tirith (12_43) and Pippin, Hobbit of
Some Intelligence are exact reprints of earlier cards that had a signet, and were ruled to have those signets
too. The ruling is in Section Three. A difference between a sheet and a definition is to be checked against
Section Three before it is called an error.

**2026-10-04. Two cards, one printed title (F-0004).** Orc Pursuer (5_108 and 7_303) "was a fuckup on Decipher's
part, and they were ruled to be two separate titles so you could have 4x of each." White Hand Attacker (17_120
and 18_124) is "a directly equivalent circumstance", though it never got an official ruling: sets 17 and 18 were
the work of one of the last people not laid off, and rulings by then were nonexistent. The "(2)" on the later
card's title never appears to players.

**2026-10-05. Keys written twice (F-0017).** "Almost certainly a migration or automation error. In repeat cases,
reduce them to one, and if they do not match we'll have to investigate the text to see which is correct."

**2026-10-04. Timewords (F-0003).** "Timewords are an implementation detail and not a game mechanic." A
definition that lists a timeword the card does not print, as the way to let an event be played in another phase,
is acceptable. Bears on: 12_114 For the Mark, and any event playable outside its own phase.

**A Decipher ruling on the original card does not reach an erratum that rewrote the sentence, by default**
(Q-0111, Dwarven Axe 51_9): "our errata is our own set of rulings". But: "many errata happen to work around
the ruling made by Decipher, but not all. I concur with B being your default, but do bring it up when it
happens, because I sense that there are some where we agree with the ruling." So each such case is reported
to her, with the default taken. *[2026-10-07, night]*

**Q-0115, Galadriel 51_45:** "Decipher's ruling was on 'free', not Galadriel per se. Since the errata does not
say 'free' it's irrelevant. B." A Decipher ruling about a WORD does not reach an erratum that dropped the word. *[2026-10-08]*

**Northern Ithilien's errata 57_359** existed only to work around the engine fault now fixed and will be
reverted once F-0139 is deployed: its limit is not a concern. *[2026-10-07, later afternoon]*

**A backslash in a title, lore or game text is no fault.** "This is due to that being the shorthand used in our
nanDECK script for newlines. Anywhere that it's found in titles, lore, or game text, it can be replaced with a
space; it doesn't need its own finding report." *[2026-10-08, afternoon]*

## Formats

**2026-10-04. Formats and correctness.** Formats are by and large a table-validation concept: players may not
show up at a table with a deck that violates the format, but with a few exceptions format has very little effect
on whether play is correct. "A card from set 1 doesn't care that the site path has cards from set 18 in it by and
large." Tests need not hold a card to the format it was printed for.

**2026-10-04. The exceptions.** A card that explicitly mentions sites of a certain block has to be tested on that
block. And rules that differ by format have to be tested in a format-aware way where a card touches them. Her
example: the rule that the Ring-bearer's skirmish cannot be cancelled was introduced in the King era, so
Fellowship- and Towers-era formats do not have it, and cards that cancel a Ring-bearer's skirmish need tests under
both sets of rules. She expects the list of such rules to be short.

**2026-10-05. When the Ring-bearer's skirmish stopped being cancellable (Q-0001).** The rule came about from set
9, Reflections, "which has several non-Frodo ring-bearers who warp the format if they are able to have their
skirmishes canceled, so it was with the release of set 9 that the rule was added." King block is sets 7, 8 and
10; "there's not really a good reason for King Block itself to permit Ring-bearer skirmish canceling. I think
nobody noticed because King Block itself is a rounding error of usage; it's far more common to play Movie Block
(1-10), which does indeed have the proper RB canceling rule." Format-aware cancel tests use `fotr_block` (allows)
and `multipath` or `movie` (refuses), and leave the King block formats out until F-0014 is settled.

**Historical formats are left alone (F-0014).** "We won't touch these formats. There is one card that can cancel
a RB skirmish, and there's no reason to alter the historical format." King block's settings stand. *[2026-10-05, evening]*

## What gets tested, and the no-effect flag

**2026-10-04. Isolation and interactions.** Cards are by and large tested in isolation. But wherever a ruling's
explanation uses a pair of cards interacting as its example, there should be a test that the interaction goes as
the ruling says.

**2026-10-04. Mechanic tests.** A set of tests that a particular card happened to uncover, but which is not
*because* of that card, may deserve to be tests of the mechanic itself, agnostic of any card, in their own folder
structure as a sibling to the card tests.

**2026-10-04. The old `at` tests.** They were written before the rig (or occasionally stubbornly after it) and are
to be converted: to rig tests in the appropriate card's file, as some already have been, or to card-agnostic
feature or mechanic tests.

**The no-effect flag (new engine feature).** "On decisions sent to a player, let's add a flag of 'no effect',
meaning the action is determined to have no effect ahead of time. In addition, for skirmish actions only I want
that flag to be able to detect that it will have no effect on a currently-skirmishing character, for the
extremely common case of 'I used my hobbit pump on a gondor skirmisher'. ... such actions highlight with a
different color, which will permit players to make such actions should they deem it strategically necessary."
The campaign then checks the flag "in cases like Breaching Shot and Trail of Savagery, where we had for the
player's convenience disallowed the card use in some legal-but-pointless situations. I don't want us to test
literally every card for this flag, just some mechanic tests on common use cases and the occasional conversion
of convenience guards." It does not replace the Q-0034 rule: an optional action with no cost that would do
nothing is still withheld; an event, or an action with a cost, that would do nothing is offered and flagged. *[2026-10-06, evening]*

**The no-effect flag, what it covers (Q-0073 follow-up).** The no-effect
flag: abilities that play a card from a pile stay withheld when nothing can be played; a skirmish action is
flagged for missing the skirmish only when it is a pump or a cancel that reaches nobody fighting ("If it actually
results in something hitting the table, it's fine to leave noeffect off of it"); auto-pass is left alone for now. *[2026-10-06, evening]*

## How card text is read

**2026-10-05. Cards are code (Q-0012).** "In general I hold to the philosophy that cards are code: the text should
do *what it says*, not *what the coder meant*; if the author meant something else they should have *written*
something else." Library of Rivendell (101_11) says "your tale or artifact" with no side, so a Shadow artifact in
hand may be stacked: "This was unintended, but it's legal 'code', so it ships as-is. If someone doesn't like it,
we'll issue a formal errata." She adds that this "does sometimes conflict with the QOL ruling, and I admit it's a
bit subjective where and when it gets applied." So: a literal reading the game already takes is not a finding,
however odd; a quality-of-life shortcut that departs from the text is still a question.

**2026-10-05. Followers are not characters (F-0023).** A filter that goes by name alone where the text says
"these characters" is to have "character" added.

**2026-10-05. Race and culture are different filters (F-0019).** "Bearer must be a unique [elven] companion"
filtered by the race Elf is "an implementation error ... moot in gameplay today, but someday a non-[Elven] Elf
could be printed or vice versa." To be fixed where found.

**2026-10-05. "Can't" beats "can" (Q-0011).** "This falls under our modern 'can't > can', and so 'lose > gain'."
A character who has lost a keyword until some time cannot be given it again by another card before then.

**An effect with no stated duration lasts to the end of the current phase (Q-0018).** On Boromir, Redeemed
(101_20): "Since it does not specify a duration, it defaults to 'the end of the current phase', which is long
enough to assign but not long enough to e.g. use Swordarm of the White Tower on him in the Skirmish phase."
This holds for a trigger's effect as for a phase action's. The definition's "until the regroup phase" was left
over from an earlier revision and has been changed. *[2026-10-05, afternoon]*

**"Here" on a site is the site's title (Q-0027).** "Yes, if the Farmer is out on site 1, you should get double
the benefit, but only on site 1. 'here' is supposed to be a synonym for '[title]'." An ally who has a site's
game text (Hobbit Farmer, Uruviel, Silinde) therefore makes the site's "here" offer too, while the fellowship
is at that site. Bag End Study (101_57) was right; Lorien Throne Room (101_61) required the site itself and is
corrected to go by its name (F-0043). *[2026-10-05, evening]*

**"One" stands for the noun before it (Q-0028).** On Rivendell Gateway (101_59), "the intent is (when game text
is at such a premium as it is for sites) 'one' is a stand-in for 'an unbound companion'." *[2026-10-05, evening]*

**A branch in parentheses is mandatory when it is available (F-0047, Q-0035).** "When there are parenthesis like
this, it's actually mandatory to use the parenthesis over the portion outside the parenthesis, so the player
doesn't have a choice", and "Decipher ruled that a branch in parenthesis is mandatory to choose if it's
available." "Exert a valiant companion (or spot 2 valiant Elves)": with two valiant Elves to spot the game spots
and never offers the exertion, and is right. "Wound a minion (or wound a minion twice if each of those Elves
bears a ranged weapon)": if two valiant Elves bearing ranged weapons can be spotted the minion is wounded twice;
the player is not asked which Elves and cannot choose the lesser result. *[2026-10-06]*

**"Spot X to Y" does nothing without X, even as one branch of a choice (F-0053).** "When you're given a choice,
you have to pick one that you're 'fully capable' of performing. But when an effect cannot be fully performed,
you 'do what you can and ignore the rest'. That can lead to some unexpected edge cases. That said, 'spot X to Y'
should clearly not resolve if X isn't around, even if it's one branch of a choice. This is clearly an engine bug." *[2026-10-06]*

**What a special ability is (Q-0039).** "It would count, if it's got the bold text and a colon." A labelled
ability ("Shadow: ...") used from the discard pile or while stacked is a minion's special ability and Gandalf,
Lathspell answers it; the exertion of a minion that is not in play does nothing. A permission with no phase word
("you may play this minion from your discard pile") is not a special ability and is not answered. *[2026-10-06]*

**Reading card text.** A race word with no "your" names no side: "Wraith is Wraith" (Q-0068), "Bearer must be a
Wizard" lets the opponent's Saruman bear Wizard's Stash (Q-0058). A sentence of costs with no "to" is still
costs where the next sentence is what they buy (Q-0058). An X of exertions is the player's, up to those able to
exert, whatever it can buy (Q-0069). A companion spotted as part of a cost is still "that companion" when the
effect is carried out, though paying the cost ended the skirmish (Q-0065). "Play this" on a card in play is a
transfer, from the discard pile a play (Q-0057). An exact list of sources ("from play or from hand") leaves the
others out (Q-0047). A required trigger's spot is made whenever it can be (Q-0050). "Prevent X from being
overwhelmed unless its strength is tripled" on a minion spares only that minion, by its own strength; overwhelm
is resolved minion by minion (Q-0044). *[2026-10-06, evening]*

**X of exertions is at least 1 (Q-0076).** "Exert X <cards>" is an exertion cost and X is at least 1, as with "exert X times" (Q-0076, A Wizard Knows
Better 103_40: without Saruman no companion is hindered for no exertion). X of zero by SPOTTING is still
allowed. This narrows "X may be zero" of 2026-10-05: any X of exertions is at least 1. *[late on 2026-10-06]*

**A card that makes itself a minion offers that from the support area only (Q-0077).** A card that turns itself into a minion from the support area offers that ability from the support area only
(Q-0077, the Bladetusks 103_41, 103_42, 103_43): never again while it is that minion. The requirement in
their definitions is right and is not a convenience guard to convert. *[late on 2026-10-06]*

**A sentence after such an ability that calls the card by the minion's pronoun is part of it (Q-0078).** A sentence after such an ability that calls the card by the minion's pronoun is part of the ability (Q-0078,
Bladetusk Matriarch: "She adds 1 to the Shadow archery total" holds only while she is the minion). *[late on 2026-10-06]*

**"Its skirmish" on a minion is the skirmish it is in (F-0114).** "Its skirmish" on a minion means the skirmish that minion is in: "While merely assigned to a skirmish, it is
not in an active skirmish and so that text should not be active" (F-0114, Corsair Quartermaster 103_45). A
modifier that counts over `InSkirmish` needs `self,InSkirmish` as its own filter. *[late on 2026-10-06]*

**"By another card" is a card other than THAT card (Q-0079).** "By another card" in a card's own text is a card other than THAT card, as "another X" is everywhere else
(Q-0079, Corsair Helmsman 103_46: "The wording is meant to prevent Helmsmen from responding to itself
hindering itself"). A [raider] minion that hinders itself when played is answered by a Helmsman. *[2026-10-07]*

**A spot after "to" is an effect even when nothing can be spotted** (Q-0085, Cover of Darkness, Omen of Fear
103_64): "As written I think it is A ... I can clearly see why one would want to do it without an unbound
companion, to halt a ring-bound fellowship from moving. Let's say A." *[2026-10-07, later]*

**"Remove X threats to ... a companion costing X or less" has no cap but the threats** (Q-0087, Gollum
103_66): "if the player has a reason to remove more threats (against Gondor Wraiths or something), then I
think they may." *[2026-10-07, later]*

**"The <other> player may make you X ... to prevent this": "you" chooses** (Q-0088, Ill Wind 103_67): "it
was only supposed to change the exert to a wound, not alter who selects it." *[2026-10-07, later]*

**A parenthesis with its own price the player elects to pay is the player's choice** (Q-0089, Ulaire Nertea
103_77): the wording is mandatory as printed and will be changed; "keep A". Do not ask about this shape. *[2026-10-07, later]*

**"A" is one** (F-0136, This is My Hour 103_71): "'a' in English is a synonym for 'one'; it should be
restricted to a single companion and not every companion ... change it to 'choose' instead of 'all'." *[2026-10-07, later]*

**"The second time ... each Shadow phase" means the Shadow phase** (F-0130): a phase requirement. *[2026-10-07, later]*

**"May X, and must Y for each X" is not a price** (Q-0091, The Witch-king 103_80): "worded carefully to
permit such a result, that's working as intended." *[2026-10-07, night]*

**"Copies of that card" counts the card itself and goes by title** (Q-0092, Death Take Us All 103_82). *[2026-10-07, night]*

**"A minion" on a Skirmish event that only reaches the skirmish becomes "a skirmishing minion" in the text**
(Q-0095, I Am No Man 103_84), as Q-0051 did for 102_66. *[2026-10-07, night]*

**"Is considered a [culture] <race>" adds the race** (Merry 103_85): "an addition and not a rewrite". *[2026-10-07, night]*

**Race is excepted from "unloaded keywords"** on Flame of Calenhad too (Q-0097): text gains "(except race)". *[2026-10-07, night]*

**Later still (Ketura, 2026-10-06).** Q-0071 Pippin, Trollslayer 103_31: "race *is* an unloaded keyword ... treat it
as if the text says '(except Race)' at the appropriate spot, updating the text as well." Q-0072 Tormented Revenant
103_32: "spot or add 2 threats" is the player's choice when both can be done. *[2026-10-06, evening]*

**"Resolve a skirmish involving a [rohan] Man" resolves only such a skirmish** (F-0143, Merry 103_85). *[2026-10-07, night]*

**The general shape:** "you may make <the other player> do X" is asked of "you" as a yes/no, only when X
can be done; the choices the text gives the other player stay his. A definition that never asks is an
error to fix, not a question. *[2026-10-07, late]*

**Q-0113 (F-0197), Ancient Enmity 51_29 and the original 1_29, two minions losing one skirmish to the Elf:**
"Let's interpret the If as evaluated once as a one-time trigger as if it said 'At the end of this skirmish, if
at least 1 minion lost this skirmish to that Elf...', essentially the way that Decipher *wanted* Dwarven Axe to
work. So A for both errata and original." "If a <character> loses/wins this skirmish ..." is one effect, once,
however many lose. *[2026-10-08]*

**2026-10-05. X may be zero (101_5 One Dwarf in Moria).** "X may indeed be zero. The only time that X cannot be
zero is in the case of 'exerting X times', which has an explicit ruling, but every other case has no such guard
rail." Bears on: every card that says "X" of anything but exertions.

**X counts what was actually returned (F-0016).** One Dwarf in Moria (101_5) "should only count cards
successfully returned to their owner's hands, not ones discarded after the fact, even if they were slated for
return." The definition now remembers the items chosen and wounds one minion for each. *[2026-10-05, evening]*

**"Wound X twice" is two wounds** (F-0148, Take it Down! 103_89): "that's a bug in the implementation, go
ahead and fix." An exertion in the definition where the text wounds is a definition fault, fixable at
landing. *[2026-10-07, afternoon]*

**"Choose X ... (or you may play it ...)" plays the card chosen** (F-0152, Ominous Sky 103_97): "That's a bug,
yeah, fix it." *[2026-10-07, later afternoon]*

## What is offered and what is asked

**X of zero that would do nothing is not offered (Q-0015).** A deliberate departure for quality of life: "we
don't offer to 'discard 0 cards to make a user strength +0'. Besides being strategically useless and
distracting the player with options that don't actually do anything for them, this is a potential griefing
angle if the player can click on the same pointless action over and over and time out the game without getting
kicked on the decision timer." So I Would Have Gone With You to the End (101_23), Breaching Shot (102_21) and
Northern Signal-fire (103_30) are right to start X at 1. X may still be zero where the action does something
without it (One Dwarf in Moria, 101_5, heals whatever X is). A definition that offers a do-nothing X of zero is
not thereby wrong; one that withholds it is not a finding. *[2026-10-05, afternoon]*
Since Q-0034 (2026-10-06, below: "An optional action that would do nothing is not offered: now a rule") that
last sentence holds only for an event's X. On a special ability or optional trigger with nothing else to pay, an
X of zero that would do nothing is to be withheld, and a definition that offers it is a finding (F-0067, From
Deepest Dungeon 102_39). Read that entry before resting anything on this one.

**No "may", no asking (Q-0017).** Boromir, Redeemed's defender +1 "should definitely be an automatic required
trigger; the optional bit is indeed a holdover from the prior version." Changed to `optional: false`. *[2026-10-05, afternoon]*

**"You may" is asked (Q-0021).** Their Eyes Fell Upon Him (101_41) drawing without asking "wasn't intentional,
that's an oversight." Changed to an optional trigger. 68_50 and 13_75, built the same way, are for their own
phases (F-0041). *[2026-10-05, evening]*

**"May draw a card for each" is one offer (Q-0029).** "It's better UX for it to be one big batch or not. The
vast, vast majority of the time, the player will want to draw, and they will not be amused needing to hit yes
for every individual card draw." And with an empty draw deck: "while it is technically a pester to ask them when
they have 0 cards in their deck, that is both rare and a bit of a chance for them to *realize* they have no cards
in their deck ... I'd say let it ask with 0 cards in deck." She spoke of the empty deck only. Whether the offer
should be made when the count itself is zero (four companions or fewer) was Q-0033, ruled below. *[2026-10-05, evening]*

**A draw of nothing because the count is zero is not offered (Q-0033).** "If there's 4 or fewer companions,
there's no reason to nag the player with 0 draws, yeah. Let's make it smarter." Nen Hithoel (101_63), Orthanc
Library (4_362) and Summit of Amon Hen (1_362) now require something to count before the Shadow player is asked.
The empty-deck offer of Q-0029 stays. Other "may draw a card for each X" cards are to be read the same way as
they come up: an offer with X at zero is a finding, kind hjson-logic, already ruled. *[2026-10-05, night]*

**2026-10-05. A parenthesis that is a separate may (Q-0010).** On Three for the Elven-Kings (101_12) the heal in
parentheses "should be a separate may (definitely a bit convoluted)", and "implemented as its own 0-1 choice that
is either a click on a companion or clicking 'pass/done', not a 'do you want to heal' pester prompt."

**An optional action that would do nothing is not offered: now a rule, with two exceptions (Q-0034).** "Sure,
start treating it as a rule, except in these cases: Events. People often want to play dud events to get them
out of their hand. Situations with a cost. Oftentimes the cost may incidentally be beneficial even if the effect
fizzles, such as exerting an enduring character." So: an optional trigger or action with no cost whose whole
effect would do nothing (a draw of zero, an X of zero) is rightly withheld, and offering it is a finding. An
event is offered whenever its own requirements and costs are met, whatever its effect will do. An optional
action or trigger that has a cost ("you may exert X to ...") is offered whenever the cost can be paid, even if
the effect will do nothing; a definition that adds a requirement to withhold it is a finding (Naith Armaments
102_10 had one). Do not ask about these shapes again. *[2026-10-06]*

**Fewer questions** (Q-0090): where taking an offer leaves one thing to do, do it without asking again. *[2026-10-07, later]*

**A choice with nothing on any branch is not asked (F-0116).** A choice whose every branch has nothing left to act on is not put to the player: "the prompt should be
smarter" (F-0116, Savage Fury 103_39). *[late on 2026-10-06]*

**Desolation of Ash 103_122 keeps its guard** (Q-0107, against the general rule of Q-0034): "Shadow almost never
wants to waste twilight like that and it would be a pester prompt showing 5 times or more per turn for no
reason." A guard of this kind on a card that would otherwise ask at every phase may stay; ask about others. *[2026-10-07, night]*

**An optional look through an EMPTY draw deck is still offered** (Q-0104, Hornblower Barrel 103_112): "the draw
deck emptying entirely is a rare event, and having that underlined by the ask still happening and showing
nothing would help them realize it." *[2026-10-07, night]*

**Q-0114 (F-0194), prevention clauses in general:** "In general, prevention clauses should only trigger an
ask if the thing they are preventing actually occurred, so both lack of cards to draw, prevented draws, and
rule of 4 should prevent the asking of the prevention clause. So A, yeah." A "<player> may <pay> to prevent
this" is put to that player only when the effect it would prevent is going to happen: not for a draw from
an empty deck, a draw past the Rule of 4, or a draw something else already prevents. This is one rule for
every such clause, with the earlier "to prevent NONE" cases (F-0051, F-0183, F-0185, F-0192) under it. *[2026-10-07, late]*

**2026-10-04. Goblin Runner's "may" (F-0009).** Technically incorrect and deliberate: "it is *absolutely* a QOL
thing to not make everyone click on Runner every time they play it." The twilight is added without asking.
Rule this kind of thing a quality-of-life implementation, not a bug.

**2026-10-04. Ambush instances (F-0010).** Also technically incorrect and deliberate: making the player accept
each constituent ambush trigger one by one would be very poor to play, "nobody is ever going to want two of
them but not three", and a blind eye was turned. Separate instances of ambush are offered as one trigger for
their sum.

**2026-10-05. Ambush instances and Desert Wind Whisper (Q-0007).** "This is a case where I don't think it matters
if they're combined or not. A minion with 'ambush (2)' and 'ambush (1)' would get +2 strength and +1 strength, I
think. So the QOL combination doesn't affect the end result."

**An ability at its limit is offered, flagged no-effect (Q-0041).** "Technically speaking it should be offered,
and some cards (Beneath the Mountains) are famous for using the cost over and over in spite of the effect limit
being reached." The definitions that withhold an ability at its limit are converted when the flag exists. *[2026-10-06, evening]*

**"Exert X twice"** should offer only those able to (F-0131): "It would be nice if [a vitality filter on
each card] wasn't necessary all the time." Fixed in the engine, not card by card. *[2026-10-07, later]*

**Worth Fighting For 103_120's "you may make the Shadow player shuffle" is to be asked** (Q-0106): "an oversight
in hindsight. A, but have it be a 0-1 choice instead of a 'do you want to' prompt." Whose choice the 0-to-1
is has been asked back; not built yet. *[2026-10-07, night]*

**Q-0106, Worth Fighting For 103_120, "you may make the Shadow player shuffle one of their hindered cards":**
"Oh I forgot shadow makes the choice for WFF. Never mind then, ask freeps and make shadow choose as it
should." This withdraws the 0-to-1 choice she first asked for: the Free Peoples player gets a yes/no, and
the Shadow player then picks the card. The yes/no is asked only when the Shadow player has a hindered card
(no pester prompt, as with Q-0107). Built as F-0180. *[2026-10-07, late]*

## Limits

**"Limit once" is not "limit +N"** (Q-0082, They Are Not All Accounted For 103_103): "this should be a limit
on *triggering* once per turn. I think there's a nuance between 'limit +3' and 'limit once', with the former
being a cumulative addition of multiple invocations, and the latter being a limit on the number of times the
ability can invoke at all." 103_103 stays withheld at its limit. Whether the other "limit once" abilities
that the no-effect work put on offer go back is asked as Q-0093; until answered they stay as built. *[2026-10-07, later]*

**A "+N" limit is the card's for the whole phase**: counted per source card, not per card affected (F-0125:
"supposed to be a limit on the source card, not per-target"), and cumulative over uses, not a cap on each
use (F-0135, Ulaire Nelya 103_76: "the limit should be cumulative"). *[2026-10-07, later]*

**"Limit once per site" is once for each site in a turn** (Q-0084): "the intent is not that you have to
track it across turn boundaries." A use in the fellowship phase must not spend the next site's use (F-0127). *[2026-10-07, later]*

**Limits, settled (Q-0093).** A limit that counts what the effect gives stays on offer at the limit, flagged
`limit`, its cost paid for nothing: "(limit +3)", and also "(limit 1)" where it counts things added ("Dark
Tidings is more like '+1' than 'once'; it is limiting the number of threats that can be added the way that
Scimitar limits the strength bonus"). A limit on invocations ("limit once per turn / per phase") is
withheld at the limit: "the second should halt being offered once the invocation limit is reached." *[2026-10-07, night]*

**Two companions exerting at once draw one card** for a Cirith Ungol Watcher (F-0145, 103_63, 103_92): "if we
have to, we'll set it up as a 'limit once' and 'limit 1 drawn card' haha. But that can be internal, doesn't
need to alter the text. At any rate, CUW should only draw 1 card in that case." *[2026-10-07, early morning]*

**"Limit once per site" gets an engine counter** (F-0146): "it makes sense to add an engine-level handling of
this instead of a hack. All the Decipher once-per-site limits are skirmish effects that have no issues
around boundaries." *[2026-10-07, early morning]*

**Sixth Level 8_44 "(limit 1)" is built as a limit on uses**: "Let's code Sixth Level as 'once' just for UX
sake, but I agree that it's likely a +1 situation." It is withheld at its limit. Scaling Ladder 55_57 and The
White Council 101_18 "(limit 2 per phase)" stay limits on what the effect gives. *[2026-10-07, evening]*

**Morannon 5_102 counts by the turn** (F-0157): "Per turn is the correct interpretation." *[2026-10-07, night]*

**A discard the Free Peoples player chooses is not forced (F-0205).** "So this might need to have those "forced"
fields scrutinized on the various topdeck discard effects. There is a ruling for Courteous Halfling in Section
Three: "This card only prevents Shadow cards that make you discard from hand or draw deck. If a Shadow card gives
you a choice, you may choose to discard", and then gives Dunlending Rampager as an example. Since Rager and many
other errata offer a choice, the discard is not forced; Freeps could instead choose the other bad effect, and so
CH doesn't help them in that case." So `forced: false` is right where the discard is one of the Free Peoples
player's options, and a Shadow card that makes him discard from hand or deck with no choice is `forced: true`. *[2026-10-08, afternoon]*

## Costs, and playing cards from piles and hands

**2026-10-05. Costs and effects around "to" (F-0022).** On Counsel of the Wise (101_13): "Since the text says
'spot Gandalf *to*...' that is the only actual cost of the card; everything following is an effect." A definition
that writes something after the "to" as a cost, so that the card cannot be played when that thing cannot be done,
is wrong.

**Playing a card from the draw deck or discard pile (Q-0019).** "Playing a card from discard as an effect is
always treated as a cost" (the ruling on They Are Coming, "canonized as the standard way of treating it"). There
must be "no penalty" for failing to play from the draw deck, so the same does not hold there, and by the rules
Just a Wisp of Cloud (101_28) could be played at any time. "That said, it's a pretty shit UX to let the player
play when it's not in the draw deck if they thought they had one and did not. I believe the current
implementation threads the needle of UX and rules." So `mustExist: true` on the eight "from your draw deck or
discard pile" definitions (5_30, 7_53, 8_92, 8_118, 11_106, 17_17, 101_28, 101_37) is a deliberate departure:
with nothing playable in either pile the card is not offered, and that is asserted as the game has it. *[2026-10-05, afternoon]*

**Being made to play the card found (Q-0022, rider).** That a "play ... from your draw deck or discard pile"
card with something playable in the deck makes the player play it (the choice has a minimum of 1) "looks like a
bug to me": CR 4.2 "draw deck" lets a player choose not to. It is the engine's `mustExist` that does it
(F-0042). *[2026-10-05, evening]*

**Looking through a draw deck never forces a card to be taken (F-0049, F-0051).** "Those should all be an
effective 0-N choice." Taking into hand, like playing (F-0042), has a minimum of 0 once the deck is being looked
through. *[2026-10-06]*

**Playing from the discard pile as an effect is a cost, the event's own twilight included (F-0046).** "It should
block until both can be paid for; rules state that effects playing from discard pile have to essentially be
treated like costs." *[2026-10-06]*

**Playing a card as an effect, from any pile, is a cost (Q-0062, Q-0066).** From hand: "it should enforce a card
being there and playable before playing"; where an effect plays a card several times, "we should at least
confirm that the first play is possible". The dead pile is "covered for the same reasons that the discard pile
is", and I Call on You "should properly withhold itself if the threat cost of the wraith to be played cannot be
paid". Replacing a character from the discard pile: "treat this as if playing from discard for rules purposes;
if it's not in the discard don't offer it." The replaced minion "simply 'becomes' another persona, without
skipping a beat or interrupting any prior tracked effects"; "another" means another subtitle (Q-0067). *[2026-10-06, evening]*

**"Play any number of / up to N ... from your discard pile" is withheld when nothing there can be played**
(Q-0086): "more consistent with the play-from-discard rules." *[2026-10-07, later]*

**X is capped at what can be played** in "hinder X ... to play X items from your discard pile" (Q-0094,
Gondor Calls For Aid! 103_83): "B, yeah." *[2026-10-07, night]*

**"From your support area" is a requirement where the card says it (Q-0022).** "If the card explicitly states
'from the support area', then it clearly can't bounce." Pale Crown (101_39) does, so its ability is not offered
once the Crown is on a Nazgul. Where a card says only "transfer this" (many fortifications), whether it may be
moved again from its bearer "is completely unknown if that was intended or not" and is case by case: ask, card
by card, and do not report it as a finding. *[2026-10-05, evening]*

**A transfer is not "coming into play" (Q-0053).** "Transferring 'is not playing' and so specifically does not
count as 'coming into play' for the purposes of having the twilight cost altered." Other cards' twilight cost
modifiers do not reach a transfer. "We're going to have to alter Asfaloth as well." *[2026-10-06, evening]*

**The twilight cost of a card in play is the number printed on it (F-0109, Q-0073).** F-0109 and Q-0073, the twilight cost
of a card in play: it is the number printed on the card; neither another card's text nor its own changes it once
it is in play ("For now, we will follow the rules as written, but I'm intending to change that written rule"; "I'll
put it on the docket for us to remove that rule in PC formats, but don't take that into account"). *[2026-10-06, evening]*

**A transfer does not take the card's own cost text into account either (Q-0074).** A transfer does not take the card's own cost text into account either: "Based
on a strict reading of the twilight cost rule, transfers should not take self-discounts into account. It's stupid
and asinine, but it's RAW." *[2026-10-06, evening]*

**Order of costs and effects (Q-0080).** "Costs (and effects for that matter) should always be evaluated in
the order in which they appear." So a cost that puts what a later cost needs out of reach (Raiding Schooner
103_55: hinder another Schooner, then remove 3 tokens, some of them on that Schooner) makes the ability
unpayable, and it is not offered (F-0123). The engine checks each cost against the board as it stands, so
such a card needs a requirement saying what must be left after the earlier cost. *[2026-10-07]*

**Q-0080, as the engine now stands (engine pass 13, F-0184):** exertions and wounds that an action's earlier
costs are certain to place are judged in order by the engine, for later costs and for the card the action
then plays; a definition needs no requirement for those. A cost that puts something else out of a later
cost's reach (a hinder, tokens) still needs the requirement, and a real choice of who exerts is judged on
the board as it stands. *[2026-10-07, late]*

**2026-10-04. Exhausting the exhausted (F-0005).** "Exhausted" is a state in the original game, but "exhaust" as
a verb on Decipher's cards never appears as a cost. The PC has ruled that you may not exhaust an exhausted card.
The ruling had not been implemented; it is to be treated as an engine bug and implemented.
(What was found when implementing it: the rule has been in the exhaust effect since 74b4a685d, 2025-06-28, where
a character who cannot exert is not a candidate for exhausting. The card that prompted the finding, Ulaire Toldea
103_79, got round it by nesting its cost inside an `If` in its effect, where a cost does not decide whether a
trigger is offered. The definition was fixed; the engine needed nothing. `mechanics/ExhaustTests` holds the rule.)

**A card discarded with its dying bearer IS "discarded" for a card that then plays it from the discard pile**
(Q-0105, Merry's Pipe 103_113), "but it has to be playable". The older rule against answering a discard on
bearer death (Q-0023) is about PREVENTING it: "where would the card go after being 'prevented' from
discarding? Its bearer is gone. However *replaying* can retarget and locate another eligible bearer, so
it's kosher." *[2026-10-07, night]*

**Preventing a discard that was a cost (Q-0038).** Q-0038 (102_8): preventing a discard that was a
cost "is permitted, but it will cause the cost to no longer be considered paid". *[2026-10-06]*

**"Remove N [culture] tokens" may take them from more than one card.** Corsair Marauder 58_57, "remove 2 [raider]
tokens", built with removeTokensCumulative: "58_57 is supposed to be able to take from multiple targets, yeah."
*[2026-10-08, evening]*

## Triggers, responses and the play of a card

**2026-10-05. Who orders simultaneous actions (G-0005).** "After all other procedures have been followed, the
Free Peoples player is the 'tiebreaker' on deciding timing of two simultaneous actions." So only the Free Peoples
player is ever asked to order required responses, and Shadow twins of `FreepsResolveRuleFirst` and
`FreepsResolveActionOrder` would be invalid.

**A card's own required trigger still happens when the same loss removes the card (Q-0020).** Thrashing
Tentacle (101_33) adds its burden when the skirmish it loses kills it. "Consider something like Freca, that
produces an optional trigger *and also* lets the original card it was attached to get killed. If the required
version didn't work, the optional definitely wouldn't." (LR 2 is the related ruling on who counts as a losing
character; LR 8, still preliminary, is about "skirmish about to end" triggers on a card that is discarded
first, which is another matter.) *[2026-10-05, afternoon]*

**A discard that follows from its bearer leaving play cannot be responded to (Q-0023).** "When we're responding
to cards about to be discarded with the intent of preventing their discard, we can't usefully respond in the
case of bearer death. Where would the prevented-discard card go afterwards? ... we won't respond to cards
discarded on bearer death because there's no meaningful place to put them, usually." (V2 Haldir's wording was
changed for this.) *[2026-10-05, evening]*

**Responses and triggers.** "Each time ..., you may" is a trigger, not a response (Q-0069 on Great Gate: "it
should be a trigger as the text uses"). A response stops being offered once what it answers has been cancelled
or prevented (Q-0043: "normally when responses react to a trigger, they should stop reacting if that trigger is
prevented by a prior trigger"). An optional trigger added by an event belongs to the player who played it
(F-0083). *[2026-10-06, evening]*

**"Each time your condition is hindered or discarded" answers its own (Q-0075).** "Each time your condition is hindered or discarded" answers the card's own hindering or discard (Q-0075, One
Ill Turn Deserves Another 103_38). *[late on 2026-10-06]*

**"Each time this is hindered" is answered after the hindering (F-0120).** "Each time this <card> is hindered" is answered after the hindering, not when it is about to happen
(F-0120, Desert Wind Scout 103_48): "If the after-hinder works, I'm fine with switching to that; I think I
did it as a before-trigger because I was concerned that the after-trigger would fail to connect due to its
source being inactive due to the hinder." *[2026-10-07]*

**A card's own removed-from-play trigger is carried out as that card and its owner (F-0121).** A card's own hindered-from-play (and discarded-from-play) trigger is carried out as that card and its owner,
not as the card and player that removed it: "fix it in the engine" (F-0121). *[2026-10-07]*

**Any discard from play sets off "when this is discarded (from play)"** (Q-0081): "I would expect that
end-of-turn discard to produce a triggerable response." *[2026-10-07, later]*

**A copied keyword keeps its number** (F-0126). **Two offers of one response name what each is for**
(F-0137). **A play that fits nothing is not offered** (F-0140). *[2026-10-07, later]*

**A card discarded as it comes into play** (F-0139; CR 4.2 "discard"): "a persistent thorn in our side ...
one of the only interactions we've never been able to address in the engine": Northern Ithilien 7_359 with
Watcher in the Water and Evil-smelling Fens in play, playing Gollum again and again, "draining all the
threats and turning them into burdens; the rules dictate that the auto-discard should cause Gollum's play
to no longer count as fulfilling the cost of Northern Ithilien, but the engine doesn't have a way for
after-triggers like that to reach back in time and decide it so." She wants a proposal with a breakdown
before anything is built. *[2026-10-07, later]*

**On a card that leaves play as it is played (F-0139; her answers to the proposal).** The frame is CR 4.2
"playing a card": "Step 5 is when Watcher would respond and discard the card, while it is not until Step 7
that 'The card is played', at which point we can interpret it to have fulfilled the cost in the case of
Northern Ithilien." Steps as she counts them: 4 the card is placed and is in play; 5 respond to the playing
of the card (and to losing initiative); 6 perform effects; 7 the card is played.
1. A play that is a cost is fulfilled only at step 7: "a natural reading of steps 5-7".
2. A card's own "when you play this ... or discard it" is step 5 too: "I would also expect it to cause step 7
   to not manifest, and therefore not count as executed."
3. "If anything in step 5 (a Shadowplay/Unheeded combo is the classic example) causes it to exit play before
   it has made it to step 7, I would count that as not fulfilling the cost." Any exit, by any card.
4. A play discarded on entry does not count as "played this phase": "now that one's thornier. I would think
   it would not."
5. The pointless use (Northern Ithilien beside the Watcher) "should still be offered".
6. The Watcher played away from a marsh beside another minion: "it would have to evaluate the effects on
   Watcher in the order in which they are printed, which would first discard the others and then discard
   itself".
7. "Loss of initiative is step 5": it stays with the responses to the play. *[2026-10-07, night]*

**"Played this phase" is judged at step 7 as well** (F-0139, follow-up): "the 'played this phase' should also
hold its counting until step 7, for consistency's sake." A play whose card is gone before the play
completes, by the entering step or by a response, is not in the play history. *[2026-10-07, night]*

**A play's extra effects are carried out though the card has gone** (F-0139; Ulaire Otsea 103_78's threat):
"the threat can stay, given that it is an additional effect, which puts it under 'do what you can'." *[2026-10-07, later afternoon]*

**A card discarded by its own "when you play this ..., or discard it" was not played for other cards either**
(F-0139; Dunlending Rampager and Ulaire Attea): "a self-discarding card should not trigger Attea or other
'each time you play' or 'when you play', that's pretty straightforwardly the same situation as Runner, the
only difference being where the reaction text is." This reverses item (g) of the F-0139 build. *[2026-10-07, later afternoon]*

**F-0181, Greatest Kingdom of My People 51_16, "the first time your opponent plays an Orc each turn":**
"You and the reviewer are correct on F-0181." Only the first Orc played in the turn brings the offer,
whatever was answered; a declined offer does not come again. *[2026-10-07, late]*

**F-0186, response events when a card plays "an event" from somewhere:** "Response events should not show
in such generic cases when asking for a phase event, yeah, and especially when playing an event from
discard. There is an existing bug on a RTMD card that causes a game crash if a response event is chosen
this way and played." A response event is never among the cards offered by "play a(n) ... event from
<discard pile / a stack / draw deck / hand>" unless it also has the current phase's timeword. *[2026-10-07, late]*

**F-0193, Race Text 92_29 (RTMD), "Open and reveal a sealed booster pack ... You may play one shadow card
revealed, ignoring all costs":** "Given the specifics of that card, we'll say we restrict it to shadow-phase
events only." Of the events revealed, only Shadow-phase events may be chosen; a response event, or an event
of another phase, is not offered (today any event is, and a response event played this way crashes the
game). Other card types as now. *[2026-10-07, late]*

**2026-10-05. The snapshot (Q-0011).** "Make all X ... until Y" on a special ability affects only what was on
the table when it was used, "as per the Snapshot Rule", as on an event.

**Keywords copied "until ..." are a snapshot taken when the ability is used** (Q-0096, Flame of Calenhad
103_86): "whatever keywords were on the target would get memorized and applied to everyone else." *[2026-10-07, night]*

**War Howdah 103_62 keeps the stacked Man's game text and keywords** "even if the Man is later lost": a
snapshot of both, like Q-0096. *[2026-10-07, evening]*

**A tentacle killed by the wound may still be stacked (Q-0014).** "The tentacle being killed doesn't preclude
also subsequently being stacked due to another card's game text." Out of Dark Waters (101_34) stacks it from
the discard pile. *[2026-10-05, afternoon]*

**Reinforcing more than one token is one at a time (F-0113).** Reinforcing more than one token: "Should let multiple tokens be selected one at a time" (F-0113). Write it as
a `Repeat` round a `ReinforceTokens` with no `times`; `times` on ReinforceTokens is only for "reinforce a
token twice" (the same card). *[late on 2026-10-06]*

## Hinder, the support area, sites, Maps and the Ring

**2026-10-05. Sam as Ring-bearer (Q-0008).** Resistance was fleshed out in sets 9 and 11, codifying what had been
improvised with Frodo and Sam earlier. "Resistance is a numeric stat that they always have, similar to Strength
or Vitality, but originally on Sam it was dormant and unused unless and until he became the Ring-bearer. With set
11 they expanded the concept such that every companion has a resistance that is always reduced by the number of
burdens, but which does nothing on its own if reduced to 0 unless the 0 companion is the Ring-bearer." And: "When
any character dies, as the 'leaving play' section indicates, 'Any tokens on that card are removed', and as
burdens are tokens they are thus flushed away, meaning Sam inherits the mantle of Ring-bearer with no burdens to
his name." Bears on: every Sam with "make Sam the Ring-bearer (resistance 5)".

**2026-10-05. Hinder.** `at/HinderTests.java` has an exhaustive set of tests for the hinder mechanic; read it
where the 5.0 excerpts leave a detail of hinder open.

**Hinder and restore.** A card hindered in the middle of its own ability still carries the ability out, and an
effect that names its bearer still finds him (F-0099: "the -1 strength should still apply"; the old stub
HinderingACompanionMidAbilityAlsoCancelsTheAbility "was the old design but it was altered"). But a token is not
added to a card hindered before the token arrives (Q-0070: "can't > can; tokens cannot be added > continue to
execute"; "the same logic is what makes Anduril, Legend Remade successfully block a wound"). A reconcile
restores the player's hindered cards before the discard (Q-0059, CR 5.0; the engine is to be changed). Cards
already hindered stay out of hinder choices (Q-0060: "for QOL purposes let's continue to leave hindered cards
out of the filter"). Shadowfax 103_5: "Minions skirmishing bearer gain no strength or keyword bonuses from
Shadow cards borne by minions skirmishing bearer" (Q-0061). *[2026-10-06, evening]*

**A choice by side alone may take a hindered card** (Q-0098, Cover of Darkness, Omen of Despair 103_93): "Yes,
as written that is valid. This should only apply to cards which target 'a card', 'a Shadow card', or 'a Free
Peoples card', since anything more specific is unable to pierce hinder's veil." So: where the text names
nothing but "a card" or a side, the definition's effect carries `override: hindered`; where it names a card
type, culture, keyword or anything else, hindered cards stay out. Hinder choices themselves still leave
hindered cards out (Q-0060). Fixable at landing. *[2026-10-07, afternoon]*

**"All Shadow cards borne by ..." and "each Free Peoples card borne by ..." reach hindered cards**, as "a
Shadow card" does (Q-0098): "Good call on all/each." *[2026-10-07, evening]*

**A "support card" is a card in the support area, and a card borne by one is in the support area** (Q-0099):
"'Support' is meant to be shorthand for 'in the support area'. A card attached to another is on Gemp in the
'attached zone', but ultimately that's an implementation detail; it is still 'in the support area'." *[2026-10-07, later afternoon]*

**"In / from a support area" takes in borne cards, on every card** (Q-0101): "The borne cards are physically
placed under the cards which bear them, and so by transitive property I would expect that if their bearer is
'in the support area' then they must, too." Stacked cards are not seen ("Stacked cards are definitely not
seen, but I think otherwise they should be fair game. Weary on an Ally and Pillage should both get hit").
Whose area: the one the bearer lies in ("If my opponent's Black Breath is on my Elrond, it's in my support
area, because that's where Elrond is"). *[2026-10-07, night]*

**Burdens are not "on Frodo" while Frodo is hindered** (Q-0103, For Frodo 103_107): "the burdens can still be
spotted by themselves, but ... while Frodo is hindered, he himself can't be." *[2026-10-07, night]*

**A card borne by a hindered companion is not "borne by a companion"** (Q-0108, Gorgoroth Wastes 103_124): a
hindered bearer is not found by its card type. *[2026-10-07, night]*

**The Ring-bearer and The One Ring are never discarded, for any reason** (Q-0109). *[2026-10-07, night]*

**Maps and meta-sites cannot be hindered** (Q-0110): "they don't do anything and would be a distraction ... an
explicit entry should be added for Maps and Meta-sites." *[2026-10-07, night]*

**"Bearing 3 or more cards" counts hindered cards** (Lying Counsel 15_85). *[2026-10-07, night]*

**"Control(s) a site" goes by the count of controlled sites (Q-0054).** "All the cards you mention and those
using the same language should be retrieving the count of controlled sites, going from 0 -> 1 and
enabling/disabling their text accordingly." A card that needs the site itself (stack on it, liberate it) still
needs a real one. *[2026-10-06, evening]*

**Maps are information only (Q-0055).** PC-Movie runs under Multipath rules: any legal site path, cards of that
path only. "All the Map does is announce which path the player is using, and it is revealed in the player's
support area before the game begins, so it is information available during bidding. Maps therefore have no
ongoing in-game effect." *[2026-10-06, evening]*

**Controlled sites are not discardable (Q-0056).** A card that discards "a card" from a support area does not
reach a controlled site. *[2026-10-06, evening]*

## Reveals, draw decks and shuffles

**2026-10-05. Reveal means shown (F-0021).** A card "revealed" from the draw deck that only the player choosing it
sees is an oversight; the opponent is to be shown it.

**2026-10-05. Looking through a draw deck (Q-0013).** CR 4.2 "draw deck": "When you finish looking through it" is
the timing. "So as per rules, we always reshuffle." Other Decipher games lacked the rule and wrote the reshuffle
on every card, and later LOTR sets fall back on that habit, but here an instruction to reshuffle after revealing
the deck is almost always redundant. A definition that searches the whole deck and leaves it in order is a bug.

**Who is shown a reveal (Q-0024, Q-0025, F-0039).** A player who needs to see the card is shown it: the
opponent of a card revealed from a draw deck by the printed word "reveal" (F-0021), everyone for an opened
booster (F-0039: "it should reveal it to everyone just for fun"). The owner of a hand that cards are revealed
from gets no pop-up ("which blocks play until dismissed"), but the game log must say which cards were revealed:
"If the log isn't being populated that needs addressed." A card taken from a draw deck by type, where the text
does not say "reveal", is revealed by the log line alone: "The log has links any player can click to see what
card it's talking about." *[2026-10-05, evening]*

**A random reveal from a hand gives its owner no pop-up either (Q-0036).** "Doesn't need the popup, just the
event log." *[2026-10-06]*

**2026-10-05. Copies in hidden zones (G-0003).** That a choice listed by blueprint cannot tell two copies apart is
a limitation of the engine: it cannot distinguish two blueprint ids of non-physical cards.

**"Shuffle up to N ... into your draw deck" shuffles the deck even when nothing is shuffled in** (Q-0102):
"it's more consistent for the shuffle to always happen even if you're shuffling 'in' nothing." So such an
offer always does something and is always made (F-0161 is not a bug). *[2026-10-07, night]*

**Q-0112 (F-0196), shuffling "any number" of cards from play with none chosen:** "Rest by Blind Night should
clearly be playable with no effect, but I can see the issue with Mumakil Commander. Let's say that on an engine
level, the shuffle can target 0 cards, but on a card level we'll set up Mumakil Commander to require there be a
mount in play first." The engine shuffles the deck with none shuffled in, as from a discard pile (Q-0102);
Mumakil Commander 18_71's ability is withheld until a mount of his can be spotted. *[2026-10-08]*

## The test rig

**2026-10-05. A cheat for allies (G-0004).** "There's nothing wrong with a MoveAlliesToTable function. Go for it;
you can enforce the card type if you like."

**2026-10-05. Freeps logic in Shadow twins (G-0006).** Many of the rig's twins were produced by copy and paste,
"and it's therefore been a common occurrence for the freeps logic to be lurking inside the shadow twin." A Shadow
method that asks about the Free Peoples side is to be fixed when found.

**Rig requests agreed (2026-10-05).** G-0007 (asking whether a character is offered for assignment), G-0008 (the optional-trigger check by card),
G-0009 (the by-card availability check and wording of its own), G-0010 (one of several actions of a card) and
G-0011 (a shuffle counter: "I think it shouldn't harm anything") were all approved, and are in the rig.

**Rig requests agreed.** G-0012 (an optional trigger chosen by card is matched exactly), G-0013 (the methods
about revealed cards: all three parts) and G-0014 (`FreepsResolveRuleFirst()` checks that what is pending is
the required-responses ordering: "This will cause a great number of test changes downstream of this, but
approved nonetheless"). *[2026-10-05, afternoon]*

**Passes stay harmless (G-0015).** "Let's leave passes harmless for now." And the names: "we should really use
FreepsPass and ShadowPass rather than the (needlessly) wordier FreepsPassCurrentPhaseAction /
ShadowPassCurrentPhaseAction." Tests are written with `FreepsPass()` and `ShadowPass()`. *[2026-10-05, night]*

**No two cards of one unique title in play, whatever their types.** On a second Boromir cheated in beside the
first to tell "the bearer" from "a Boromir": "Go ahead and remove that malformed board state. Also, Elrond being
unique means you have to choose one type or the other, you still can't have both just because they both exist.
The cheating functions are for us to short-circuit more-or-less legitimate setups, but putting 2 unique cards on
the table is getting too far into the weeds." And on not building such boards only to tell apart two definitions
that differ nowhere else: "This is good." *[2026-10-08, afternoon]*

**A card test never relies on an exception; a check the rig cannot show parks the card.** On Bill Ferny 52_75,
whose tests read the game refusing an assignment through `assertThrows`: "Standard card functionality tests should
never rely on exceptions, and I'd like those sorts of requests to park the card while pending the engine upgrade,
rather than moving on blithely." So: no `assertThrows` and no `try` in a card test (lint refuses them); the check
is written in the ledger as `waits <check>: G-0000 ...`, a rig request is filed, and the card lands parked on it
(G-0029 for 52_75, G-0028 for Long I Fell 54_97). *[2026-10-08, evening]*

**Rig methods ahead of their use (Ketura, 2026-10-08, night).** On `FreepsCanAssignAgainst`, built for a card
whose definition then changed: "I don't object to the function being there; there's plenty of functions which have
obvious use which just haven't been used yet, and the occasional add is fine. I just don't want us to get
distracted adding so much to the test rig before it's needed, because as we go through the entire code base we
*will* encounter every kind of mechanic known to the game, so that will shake out as a matter of course."

**A card that must not be caught up in the rest of a board is put somewhere fixed at the start.** On a test that
failed one run in eight because the card under test could be in the hand a random discard took from (F-0264,
52_44): "the way I usually handle this is to put the card I care about somewhere else, like explicitly into the
discard pile at the start of the scenario, then it is guaranteed to not interfere with the rest of it."
*[2026-10-08, night]*

## Card by card

**2026-10-04. "A card stacked here" (F-0008).** On Deepest Delvings (101_2) the text is right and the
implementation, which only took [dwarven] cards, was not: likely a copy-paste from a similar Dwarven condition.

**2026-10-05. Hospitality of the Dwarves, "no more than 1 wound until the regroup phase" (101_1).** "Each dwarf
has their own per-phase limit of 1 wound." The limit is one wound in each phase while the effect lasts, not one
in all.

**"The first sentence of Saruman's game text" (Q-0016; LR 6).** Saruman's Staff has no effect on Saruman, Of
Many Colours (12_54) or Saruman, Agent of the Dark Lord (12_144), whose text opens with keywords. The White
Council (101_18) "was essentially binding itself to how Saruman's Staff works", so it leaves those two alone as
well, which is what the game does. The follower Saruman, Servant of Sauron (17_38) is not named by the ruling;
the game leaves it alone too, and the ruling's reasoning (the sentence meant is the one about not skirmishing)
covers it. *[2026-10-05, afternoon]*

**Items on [shire] companions (Q-0026).** Bilbo's two items "can go on multiple": each goes on any [shire]
companion, the same one or another. *[2026-10-05, evening]*

**Fixes agreed.** F-0033 (Gwaihir: "up to 2" is 0 to 2, "clearly an oversight"), F-0035 (The Dark Lord Advances:
"a clearly broken implementation"), F-0036 (Out of Dark Waters' action "should be fixed to not be available all
the time"), F-0038 (Pale Crown: "very nice find ... definitely good to fix and test for it"), F-0039 (92_29).
F-0037 (the Hobbit Gandalfs sharing Saruman's flag) is confirmed and waits for the Hobbit sets: "Those hobbit
cards should have made their own flag." *[2026-10-05, evening]*

**Bilbo, Of Bag End (101_50) had no requirement to play (F-0040).** "Implementation is wrong, needs fixing."
Fixed. *[2026-10-05, night]*

**Other answers.** Q-0030: Watcher in the Water replacing a tentacle "is being removed from its assignment, which
may cancel it if it's the only minion in its skirmish". Q-0031: "[rivers] and [sites with no other terrain
keywords] gain marsh". Q-0032 (102_4): the "you may play" possession "should be optional, an implementation
oversight". Q-0037 (102_7): "The card probably shouldn't have had may on it ... Let's go ahead and fix it to
match the bad wording": the additional reinforcement is asked. Q-0040 (102_77): continuous;
"we ruled that with the Shadowfax, GOTM ruling and applied it to Anduril as well." F-0044: the Shadow player
picks which tentacle Watcher replaces. F-0054: the Response type is to take a per-turn limit and the Staff is to
be a Response. G-0016 declined: "There are no cards that put more than one kind of culture token on another,
and no plans to in the PC." *[2026-10-06]*

**Card by card.** Q-0042 Breaching Shot may reach an ally away from home ("they are breaching the wall"). Q-0045
Wolf Pit as built. Q-0046 Royal Guard: only the keyword lasts. Q-0048 Now for Wrath: "your", and the text gains
the word. Q-0049 Now for Ruin as built. Q-0051 Trapped in its Gaze: this skirmish ("it should have been worded
'to make a skirmishing twilight minion'"). Q-0052 Eastemnet Hills: from hand, Ketura updates the wording.
Q-0063 Haste: the title restriction stays. Q-0064 Sticky Strands may be pulled from another companion. *[2026-10-06, evening]*

**Later the same evening (Ketura, 2026-10-06).** The reviewer's single mistakes and the `--with-parked` run stay
"for now". Q-0051 Trapped in its Gaze 102_66: "go ahead and change the text + implementation" to "a skirmishing
twilight minion". F-0067 From Deepest Dungeon 102_39: "X should only be permitted where it makes the Balrog
playable, since we are playing from discard and thus must be able to play it." F-0087 Gorgoroth Keeper 10_86:
"Do a requirement for now; I'm not certain an engine-level addition is warranted." F-0090 (the deck restriction's
min read from its max field): "go ahead and fix." *[2026-10-06, evening]*

**On the details pass 4 settled (Ketura, 2026-10-06, Q-0074).** A minion spared from an overwhelm by its
protection takes the ordinary wounds for losing and is not "overwhelmed" for other texts: "Very good." Winning by
controlling five sites "is from the very obsolete 2nd edition; it's not worth considering in the future." Sticky
Strands' timing as built: "good". A Map reaching the support area after the bid is "just a detail about how it's
presented in the client ... the information is there all the same". *[2026-10-06, evening]*

**Writhing Tentacle 101_66 played from a stack away from a marsh stacks no tentacles**: "that's how I'd
expect it to play." *[2026-10-07, later afternoon]*

**Beast of War 17_40 is played from the discard pile at 5 threats, as its text says** (F-0060): "clearly an
implementation error." *[2026-10-07, later afternoon]*

**Driven By Her Own Cruel Will 103_105 with no Hobbit strengthened hinders no minion** (Q-0100): A. "That
skirmish" is the strengthened Hobbit's. *[2026-10-07, later afternoon]*

**Q-0116, The Mirror of Galadriel 51_55:** "I approve." *[2026-10-08]*

**Repeated deck-validation lines are fine (F-0112).** Repeated lines in deck validation errors are "fine as-is" (F-0112): not a finding. *[late on 2026-10-06]*

**Q-0130, Galadriel, Lady of the Golden Wood 53_17: A.** "A was intended, yes. This card was originally banned in
Expanded due to her ability to easily play forests on that path, and so the errata specifically is keeping its
FOTR/Movie usage while supressing its ability to spam the Shadows path (which uses unnumbered sites), which should
answer that sub-question." *[2026-10-08, afternoon]*

**F-0220, Song of Durin 53_5: fix.** "Yup, since parentheticals are required, the discard is mandatory (and you
would always prefer to permanently remove 2 conditions over temporarily hindering 1, so it's not strategically
limiting)." *[2026-10-08, afternoon]*

**Q-0129, A Royal Welcome 53_4: A.** "If the ally has no damage, then that shouldn't be offered, for the reasons
you point out. A." *[2026-10-08, afternoon]*

**Q-0127, Filibert Bolger 52_101: B.** "So long as it's marking it as noeffect, we'll go with B." *[2026-10-08, afternoon]*

**Q-0128, O Elbereth! Gilthoniel! 52_108:** "Correctly surmised; the errata is about making the ruling
unnecessary." *[2026-10-08, afternoon]*

**Q-0126, Foul Things 52_59: B.** "The Foul Things errata is unique in that it plays from discard in a way that can
target itself, which I don't think applies to any other event. The infinite loop isn't common enough to guard
against in the engine, and there's no strategic need to play the same card over and over like that, so we'll leave
this exclusion on the card itself. B." *[2026-10-08, afternoon]*

**Q-0125, Realm of Dwarrowdelf 52_12: A.** "Heh, I normally have ruled that it's useful to the player to inform them
that their deck is empty, but Realm of Dwarrowdelf is only used in a particular self-mill deck that purposefully
discards a hundred cards off the top, and so such players are very, very aware of how many cards they have left.
In this case we can go with A." *[2026-10-08, afternoon]*

**Q-0124, Beneath the Mountains 52_1 (and Ever My Heart Rises 4_46 / 54_46): B.** "Yeah, this "how many times" is an
intentional QOL addition so that their opponent is not sitting there for 10 minutes while they discard their deck
3 cards at a time, and what's worse, having to manually click pass in between every iteration. Definitely keep B.
However, the reasoning behind it is unique to these three cards (ROD, BTM, EMHR), due to the deck that they are
used in." *[2026-10-08, afternoon]*

**Q-0123, "killed by" (Orc Butchery 51_265):** "Yeah, this one is tricky without an official definition of "killed
by". I think the current loose interpretation is fine, so let's stick with B." Then: "Ah, my mistake on Q-0123 and
Q-0122. A for both." Kept as built. *[2026-10-08, afternoon]*

**Q-0122, The Number Must Be Few 51_260:** "B. The intent here is to work around Decipher's dumbass ruling that move
limit modifiers are permanent, even if the card is discarded (or heaven forbid, discarded and replayed multiple
times in the same turn). So in effect, it should make this behave as if it were a less fraught effect, such as
"men are strength +1", which obviously turns off and back on whenever the companion boundary is crossed." Then: "Ah, my mistake on Q-0123 and
Q-0122. A for both." Kept as built: the penalty comes back when a 7th companion does. *[2026-10-08, afternoon]*

**Q-0121, Úlairë Nertëa 51_234: B.** "This is a poor implementation, yeah; B is definitely an improvement." *[2026-10-08, afternoon]*

**Q-0120, Savagery to Match Their Numbers 51_139: B.** "So Savagery set the high bar for power level in FOTR block:
5 companions was a boundary almost no other cards had, and it won you not just *this* skirmish but the *next* one
as well. The intent of the errata was to make it so it was not 2 automatic wins by intentionally only getting +4
total the first skirmish and +2 on the second. B." *[2026-10-08, afternoon]*

**Q-0118, Armor 51_92, Coat of Mail 51_101, Great Shield 51_107: A.** "Yup, Man Allies count. The original was down
to Decipher's preferred templating which used race as a shorthand if the culture was mono-race--a decision that
came back to bite them as soon as [Rohan] was released and suddenly a bunch of [Gondor] cards were able to go on
that culture, most notably Flaming Brand. Our errata leans into this rather than away, permitting [Gondor] Wraiths
to use them (since they're from the time period depicted on the card). A. As for the non-shadow restriction, yes,
Strange-looking Men could move it to a Shadow Man." *[2026-10-08, afternoon]*

**Q-0119 and F-0200, Boromir's Cloak 51_98: 1B, and the Shadow restriction kept.** "As the author of the errata, I
*meant* "skirmish event or skirmish special ability". However what I *wrote* does permit for Response actions as
well. Given that it requires an engine-level alteration, let's just go with 1B for now. Heh, for question 2, it
was meant to go against Shadow abilities, and would be pointless to block one's own intentionally-used ability
that presumably helps Boromir. Let's keep the Shadow restriction and I'll update the wording." *[2026-10-08, afternoon]*

**Q-0117, Sleep, Caradhras 51_84:** "Yup, that's a more general ruling about activity in a nascent game that wasn't
clear yet that this wouldn't discard your own inactive Shadow conditions. One of the only actual Decipher
"clarifications" that were actually that instead of an errata in disguise." *[2026-10-08, afternoon]*

**Findings through F-0210 approved.** "All pending Findings through F-0210 have my approval." *[2026-10-08, afternoon]*

**Q-0131, Uruk Raider 53_74: A.** ""The dead pile" refers to the current Free Peoples dead pile, same as how threats only
count the current Free Peoples threats. A." So an unqualified "the dead pile" is the Free Peoples player's. *[2026-10-08, later afternoon]*

**Q-0132, Hand of Sauron 53_90: A.** "A is fine." An exhausted companion who bears a possession is offered, loses it
and takes no wound. *[2026-10-08, later afternoon]*

**Q-0133, Bill the Pony 53_106 (and Asfaloth 1_31, Black Steed 1_208, 3_106): A.** "But it is a continuous effect,
same as the Watcher. A." And the official ruling on 3_106 (now on the wiki): "When the fellowship moves with Bill the
Pony to an underground site, the Shadow number of that site is reduced before Bill the Pony is discarded." *[2026-10-08, later afternoon]*

**Q-0138, Gríma, Wormtongue 54_154: new text.** "Ah, good point, it should read "Exert Grima to spot an unbound
companion. Hinder a weapon borne by that companion (or all Free Peoples cards borne by them if they bear 3 or more
cards)". I will update the wording. When spotting a companion, restrict the spotting to companions either bearing a
weapon or 3+ cards and at least one unhindered Free Peoples card, just for QOL purposes." *[2026-10-08, evening]*

**Reports that need no answer are findings, not questions (Q-0139).** "If no answer is needed, it should be an F not a
Q. But I appreciate the report." A Decipher ruling not carried over (Q-0111 default B) and other report-only items are
filed as findings (kind ruling, not-a-bug), not in questions.md. *[2026-10-08, evening]*

**Q-0136, Come Down 54_146: A.** "Oh ho ho, very nice find. A is the RAW way to handle this." Archery wounds the allies
cannot take go to companions (CR 4.2 "instead"). *[2026-10-08, evening]*

**Q-0137, Come Down 54_146: A.** "Come Down doesn't say "participate in archery fire", it just says "take wounds from
archery fire", that implies to me that they don't get to contribute, just be on the receiving end. The exiting
modifiers may not cover this and may need an additional one added. A." *[2026-10-08, evening]*

**Q-0134, "(limit (N))" on a count: the card's limit for the whole phase; Legolas 54_73 loses his.** "The intent with
this one was to limit the per-invocation amount at (2), not a cumulative phase limit, but based on what we've
established, this wording is far more like the cumulative +X limit than the invocation limit. Let's remove the limit
altogether. I think the only reason for its adding was as a band-aid in Expanded, and our second iteration altered how
this works and doesn't really need the limit anymore. Plus a limit of (2) twilight removed is materially different
from a +2 cost increase, where a limit doesn't run afoul of the cumulative limit stuff. I will get a new errata
revision issued." So "(limit (N))" and "(limit -N)" read as the card's limit for the whole phase (as "(limit +N)",
F-0135), and Legolas, Dauntless Hunter has no limit. *[2026-10-08, evening]*

**Q-0135, Legolas 54_73:** "good." *[2026-10-08, evening]*

**F-0235, Ever My Heart Rises 54_46: keep.** "It doesn't need a formal window, no, because on the game board the 6
cards are going to be stacked visually, where they can be inspected, and also seen in the log. The window would be a
useless click, as you surmised." *[2026-10-08, evening]*

**Q-0140, Cavern Entrance 54_355: A** (a site's "you" is each player). *[2026-10-08, evening]*

**F-0239, Gríma, Chief Counselor 55_51: new text.** "I will issue a new text; change the text and evaluate it as:
"Exert Gríma to spot an unbound companion. Hinder a condition borne by that companion (or hinder that companion and
all Free Peoples cards they bear if you can spot 4 Free Peoples cultures)."" *[2026-10-08, evening]*

**Q-0141, Merry's Sword 57_242: "(limit X)" is per use; every [rohan] companion is counted.** "1. Yeah, the per-use limit
was intended, given that the player can increase X to get more. It prevented a 6-companion setup exerting Merry
twice to get 6 exertions. 2. Yeah I can't imagine why one would want some exertions but not the max. If it's an
enduring minion, they're just not going to use the ability on them." So a "(limit X)" where X is the use's own
paid amount caps that use only (unlike Q-0134's printed "(limit (N))"), and a "for each X you spot" count spots
all without asking. *[2026-10-08, evening]*

**Shadow Host 58_43:** "The original Shadow Host requires them to be exhausted, not just Wraiths (the errata removes
that requirement as excessive)." *[2026-10-08, evening]*

**Q-0142, "wound each / every X": a fixed order, no prompt (B).** "In any situation where there is a timing tie
of required actions, the Free Peoples player chooses the order, as you noted. However, I can see why these were
coded that way, as the vast majority of the time, the order isn't going to matter, and so the game chooses for
them. Let's go with B; I think the CardResolver should at some level be pulling the list of cards from GameState
in a way that should be able to get them in ID order." So the characters an effect reaches at once are taken in
the order of their card ids, the same in every run; no decision is added. *[2026-10-08, night]*

**Q-0143, The Palantír of Orthanc 53_67: B.** A yes/no before a choice whose minimum is 0 goes: "The first no
permits an opt-out, while giving leeway for the player to change their mind after....let's get rid of the pop-up
and just make sure the message displayed to the user is sufficiently clear." The choice (0 to N) is asked alone,
and its text says plainly that choosing none is allowed. *[2026-10-08, night]*

**Q-0144, revealing an empty hand: no pop-up, a line in the log.** Foul Creation 51_44 / 1_44 against Curse Their
Foul Feet! 51_36: "Both of these cards should behave the same....not sure which one is better UX. Let's go ahead
and leave out the pop-up and just go to the log, but ensure that the log has some sort of message to the tune of
"there were no cards in hand to reveal" or something so the user isn't confused." *[2026-10-08, night]*

**Q-0145, cardInfo is not a source of truth, and is kept true.** On errata whose cardInfo names the original's id
and `promos/pc`: "this is almost certainly a copy-paste error. The cardInfo sections are pre-emptive for a future
rework of the HJSON spec to make certain other aspects of the system obsolete, so there hasn't been a need to keep
them accurate everywhere (tho of course we would prefer them to be accurate so when that eventual rework occurs
there's not much that has to be updated). Tests don't have to be written against them but I would still prefer the
information be checked as this is doing. Don't treat them as a source of truth, just update them to match the rest
of the card definition." So: no test reads cardInfo; a mismatch with the rest of the definition is corrected
where it is met, as a `data` finding, and the tools check it. *[2026-10-08, night]*

**Q-0146, Bill Ferny 52_75: A.** "A sounds good." The erratum is built as the original 2_75 is
(`CantBeAssignedToSkirmish` for the Free Peoples player), so Bill is not on his list of minions to assign against,
where the per-pair restriction listed him and refused the answer. *[2026-10-08, night]*

**Q-0147, Streets of Edoras 4_340 with Come Down: B. "Participating in archery fire" has parts, and a bar on it
bars them all.** "Well, if we follow can't > can, then Streets should I think disallow Come Down's effect from
affecting [Rohan] allies. My intent with the ruling on Come Down was that "participation" is clearly made of a few
sub-parts, such as "adding to archery totals" and "taking archery wounds when assigned", and Come Down is
performing the second but not the first. However disallowing "participation" should disallow both sub-parts here,
meaning Come Down is ineffectual." So an ally who "may not participate in archery fire" neither adds to the
archery total nor takes archery wounds, whatever card would have him take them; the wounds go to an ally who can
take them, or to companions (F-0243). *[2026-10-08, night]*
