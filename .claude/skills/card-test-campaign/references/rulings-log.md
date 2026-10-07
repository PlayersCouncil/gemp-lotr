# Ketura's rulings

What Ketura has said about how the game is to be read and how the campaign is to be run, as she said it. This is
the first thing an expectation may rest on, ahead of the Comprehensive Rules and the card's text. Add to it
whenever she answers a question; do not paraphrase an answer into something broader than what she said.

Each entry: the date, what was asked or what it was about, what she said, and which cards it bears on.

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

## What gets tested

**2026-10-04. Isolation and interactions.** Cards are by and large tested in isolation. But wherever a ruling's
explanation uses a pair of cards interacting as its example, there should be a test that the interaction goes as
the ruling says.

**2026-10-04. Mechanic tests.** A set of tests that a particular card happened to uncover, but which is not
*because* of that card, may deserve to be tests of the mechanic itself, agnostic of any card, in their own folder
structure as a sibling to the card tests.

**2026-10-04. The old `at` tests.** They were written before the rig (or occasionally stubbornly after it) and are
to be converted: to rig tests in the appropriate card's file, as some already have been, or to card-agnostic
feature or mechanic tests.

## Sources

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

## Rulings on findings

**2026-10-04. Reprints keep signets (F-0002).** Boromir, Defender of Minas Tirith (12_43) and Pippin, Hobbit of
Some Intelligence are exact reprints of earlier cards that had a signet, and were ruled to have those signets
too. The ruling is in Section Three. A difference between a sheet and a definition is to be checked against
Section Three before it is called an error.

**2026-10-04. Timewords (F-0003).** "Timewords are an implementation detail and not a game mechanic." A
definition that lists a timeword the card does not print, as the way to let an event be played in another phase,
is acceptable. Bears on: 12_114 For the Mark, and any event playable outside its own phase.

**2026-10-04. Two cards, one printed title (F-0004).** Orc Pursuer (5_108 and 7_303) "was a fuckup on Decipher's
part, and they were ruled to be two separate titles so you could have 4x of each." White Hand Attacker (17_120
and 18_124) is "a directly equivalent circumstance", though it never got an official ruling: sets 17 and 18 were
the work of one of the last people not laid off, and rulings by then were nonexistent. The "(2)" on the later
card's title never appears to players.

**2026-10-04. Exhausting the exhausted (F-0005).** "Exhausted" is a state in the original game, but "exhaust" as
a verb on Decipher's cards never appears as a cost. The PC has ruled that you may not exhaust an exhausted card.
The ruling had not been implemented; it is to be treated as an engine bug and implemented.
(What was found when implementing it: the rule has been in the exhaust effect since 74b4a685d, 2025-06-28, where
a character who cannot exert is not a candidate for exhausting. The card that prompted the finding, Ulaire Toldea
103_79, got round it by nesting its cost inside an `If` in its effect, where a cost does not decide whether a
trigger is offered. The definition was fixed; the engine needed nothing. `mechanics/ExhaustTests` holds the rule.)

**2026-10-04. Errata with no tests (F-0006).** On some errata, repeat errata especially, the tests were
commented out rather than written, under time pressure. They are to be written.

**2026-10-04. "A card stacked here" (F-0008).** On Deepest Delvings (101_2) the text is right and the
implementation, which only took [dwarven] cards, was not: likely a copy-paste from a similar Dwarven condition.

**2026-10-04. Goblin Runner's "may" (F-0009).** Technically incorrect and deliberate: "it is *absolutely* a QOL
thing to not make everyone click on Runner every time they play it." The twilight is added without asking.
Rule this kind of thing a quality-of-life implementation, not a bug.

**2026-10-04. Ambush instances (F-0010).** Also technically incorrect and deliberate: making the player accept
each constituent ambush trigger one by one would be very poor to play, "nobody is ever going to want two of
them but not three", and a blind eye was turned. Separate instances of ambush are offered as one trigger for
their sum.

**2026-10-04. Race to Mount Doom.** Sets 91 to 94 are within the audit. Set 90's cards are purely visual, with
no text, and need no tests.

**2026-10-04. The Hobbit sets (30 to 33).** Left until very last: there is no text for them, their definitions
are more abbreviated and less structured because the PC does not manage them, and they would have to be
transcribed from the card images first.

**2026-10-04. The ledger.** She does not want the ledger block in each card's test file: "It's encoded and
esoteric; presumably this is used by the author/reviewer, but it should be elsewhere and not in the final
product." It is kept in a file of its own outside the tests.

## Rulings on questions

**2026-10-05. When the Ring-bearer's skirmish stopped being cancellable (Q-0001).** The rule came about from set
9, Reflections, "which has several non-Frodo ring-bearers who warp the format if they are able to have their
skirmishes canceled, so it was with the release of set 9 that the rule was added." King block is sets 7, 8 and
10; "there's not really a good reason for King Block itself to permit Ring-bearer skirmish canceling. I think
nobody noticed because King Block itself is a rounding error of usage; it's far more common to play Movie Block
(1-10), which does indeed have the proper RB canceling rule." Format-aware cancel tests use `fotr_block` (allows)
and `multipath` or `movie` (refuses), and leave the King block formats out until F-0014 is settled.

**2026-10-05. Ambush instances and Desert Wind Whisper (Q-0007).** "This is a case where I don't think it matters
if they're combined or not. A minion with 'ambush (2)' and 'ambush (1)' would get +2 strength and +1 strength, I
think. So the QOL combination doesn't affect the end result."

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

**2026-10-05. A cheat for allies (G-0004).** "There's nothing wrong with a MoveAlliesToTable function. Go for it;
you can enforce the card type if you like."

**2026-10-05. Hospitality of the Dwarves, "no more than 1 wound until the regroup phase" (101_1).** "Each dwarf
has their own per-phase limit of 1 wound." The limit is one wound in each phase while the effect lasts, not one
in all.

**2026-10-05. X may be zero (101_5 One Dwarf in Moria).** "X may indeed be zero. The only time that X cannot be
zero is in the case of 'exerting X times', which has an explicit ruling, but every other case has no such guard
rail." Bears on: every card that says "X" of anything but exertions.

**2026-10-05. Who orders simultaneous actions (G-0005).** "After all other procedures have been followed, the
Free Peoples player is the 'tiebreaker' on deciding timing of two simultaneous actions." So only the Free Peoples
player is ever asked to order required responses, and Shadow twins of `FreepsResolveRuleFirst` and
`FreepsResolveActionOrder` would be invalid.

**2026-10-05. Copies in hidden zones (G-0003).** That a choice listed by blueprint cannot tell two copies apart is
a limitation of the engine: it cannot distinguish two blueprint ids of non-physical cards.

**2026-10-05. Freeps logic in Shadow twins (G-0006).** Many of the rig's twins were produced by copy and paste,
"and it's therefore been a common occurrence for the freeps logic to be lurking inside the shadow twin." A Shadow
method that asks about the Free Peoples side is to be fixed when found.

## Rulings on sheet discrepancies (2026-10-05)

Which side was right, card by card. Where the definition was wrong it has been corrected (findings F-0024 to
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

## How card text is read

**2026-10-05. Cards are code (Q-0012).** "In general I hold to the philosophy that cards are code: the text should
do *what it says*, not *what the coder meant*; if the author meant something else they should have *written*
something else." Library of Rivendell (101_11) says "your tale or artifact" with no side, so a Shadow artifact in
hand may be stacked: "This was unintended, but it's legal 'code', so it ships as-is. If someone doesn't like it,
we'll issue a formal errata." She adds that this "does sometimes conflict with the QOL ruling, and I admit it's a
bit subjective where and when it gets applied." So: a literal reading the game already takes is not a finding,
however odd; a quality-of-life shortcut that departs from the text is still a question.

**2026-10-05. Costs and effects around "to" (F-0022).** On Counsel of the Wise (101_13): "Since the text says
'spot Gandalf *to*...' that is the only actual cost of the card; everything following is an effect." A definition
that writes something after the "to" as a cost, so that the card cannot be played when that thing cannot be done,
is wrong.

**2026-10-05. Reveal means shown (F-0021).** A card "revealed" from the draw deck that only the player choosing it
sees is an oversight; the opponent is to be shown it.

**2026-10-05. Looking through a draw deck (Q-0013).** CR 4.2 "draw deck": "When you finish looking through it" is
the timing. "So as per rules, we always reshuffle." Other Decipher games lacked the rule and wrote the reshuffle
on every card, and later LOTR sets fall back on that habit, but here an instruction to reshuffle after revealing
the deck is almost always redundant. A definition that searches the whole deck and leaves it in order is a bug.

**2026-10-05. The snapshot (Q-0011).** "Make all X ... until Y" on a special ability affects only what was on
the table when it was used, "as per the Snapshot Rule", as on an event.

**2026-10-05. "Can't" beats "can" (Q-0011).** "This falls under our modern 'can't > can', and so 'lose > gain'."
A character who has lost a keyword until some time cannot be given it again by another card before then.

**2026-10-05. A parenthesis that is a separate may (Q-0010).** On Three for the Elven-Kings (101_12) the heal in
parentheses "should be a separate may (definitely a bit convoluted)", and "implemented as its own 0-1 choice that
is either a click on a companion or clicking 'pass/done', not a 'do you want to heal' pester prompt."

**2026-10-05. Followers are not characters (F-0023).** A filter that goes by name alone where the text says
"these characters" is to have "character" added.

**2026-10-05. Race and culture are different filters (F-0019).** "Bearer must be a unique [elven] companion"
filtered by the race Elf is "an implementation error ... moot in gameplay today, but someday a non-[Elven] Elf
could be printed or vice versa." To be fixed where found.

**2026-10-05. Keys written twice (F-0017).** "Almost certainly a migration or automation error. In repeat cases,
reduce them to one, and if they do not match we'll have to investigate the text to see which is correct."

**2026-10-05. What a card looks like today.** "The URL of the card can be pulled to inspect the actual text on
the card image used today as a further piece of information."

## Rig requests agreed on 2026-10-05

G-0007 (asking whether a character is offered for assignment), G-0008 (the optional-trigger check by card),
G-0009 (the by-card availability check and wording of its own), G-0010 (one of several actions of a card) and
G-0011 (a shuffle counter: "I think it shouldn't harm anything") were all approved, and are in the rig.

## Rulings of 2026-10-05, afternoon

**The Rules Councils' own rulings.** Ketura has supplied the eight Legacy Rules Team rulings (Decipher's cards)
and two Modern Rules Team rulings (the PC's) made since CR 4.2. `rules.py council` lists them and
`rules.py card` and `rules.py grep` search them. Look there before asking: Q-0016 had been ruled already (LR 6).
Their status lines are not all current (MR 1 and MR 2 say "Discussion" above a dated decision); a page with a
decision section is decided.

**"The first sentence of Saruman's game text" (Q-0016; LR 6).** Saruman's Staff has no effect on Saruman, Of
Many Colours (12_54) or Saruman, Agent of the Dark Lord (12_144), whose text opens with keywords. The White
Council (101_18) "was essentially binding itself to how Saruman's Staff works", so it leaves those two alone as
well, which is what the game does. The follower Saruman, Servant of Sauron (17_38) is not named by the ruling;
the game leaves it alone too, and the ruling's reasoning (the sentence meant is the one about not skirmishing)
covers it.

**X of zero that would do nothing is not offered (Q-0015).** A deliberate departure for quality of life: "we
don't offer to 'discard 0 cards to make a user strength +0'. Besides being strategically useless and
distracting the player with options that don't actually do anything for them, this is a potential griefing
angle if the player can click on the same pointless action over and over and time out the game without getting
kicked on the decision timer." So I Would Have Gone With You to the End (101_23), Breaching Shot (102_21) and
Northern Signal-fire (103_30) are right to start X at 1. X may still be zero where the action does something
without it (One Dwarf in Moria, 101_5, heals whatever X is). A definition that offers a do-nothing X of zero is
not thereby wrong; one that withholds it is not a finding.

**An effect with no stated duration lasts to the end of the current phase (Q-0018).** On Boromir, Redeemed
(101_20): "Since it does not specify a duration, it defaults to 'the end of the current phase', which is long
enough to assign but not long enough to e.g. use Swordarm of the White Tower on him in the Skirmish phase."
This holds for a trigger's effect as for a phase action's. The definition's "until the regroup phase" was left
over from an earlier revision and has been changed.

**No "may", no asking (Q-0017).** Boromir, Redeemed's defender +1 "should definitely be an automatic required
trigger; the optional bit is indeed a holdover from the prior version." Changed to `optional: false`.

**Playing a card from the draw deck or discard pile (Q-0019).** "Playing a card from discard as an effect is
always treated as a cost" (the ruling on They Are Coming, "canonized as the standard way of treating it"). There
must be "no penalty" for failing to play from the draw deck, so the same does not hold there, and by the rules
Just a Wisp of Cloud (101_28) could be played at any time. "That said, it's a pretty shit UX to let the player
play when it's not in the draw deck if they thought they had one and did not. I believe the current
implementation threads the needle of UX and rules." So `mustExist: true` on the eight "from your draw deck or
discard pile" definitions (5_30, 7_53, 8_92, 8_118, 11_106, 17_17, 101_28, 101_37) is a deliberate departure:
with nothing playable in either pile the card is not offered, and that is asserted as the game has it.

**A tentacle killed by the wound may still be stacked (Q-0014).** "The tentacle being killed doesn't preclude
also subsequently being stacked due to another card's game text." Out of Dark Waters (101_34) stacks it from
the discard pile.

**A card's own required trigger still happens when the same loss removes the card (Q-0020).** Thrashing
Tentacle (101_33) adds its burden when the skirmish it loses kills it. "Consider something like Freca, that
produces an optional trigger *and also* lets the original card it was attached to get killed. If the required
version didn't work, the optional definitely wouldn't." (LR 2 is the related ruling on who counts as a losing
character; LR 8, still preliminary, is about "skirmish about to end" triggers on a card that is discarded
first, which is another matter.)

**Rig requests agreed.** G-0012 (an optional trigger chosen by card is matched exactly), G-0013 (the methods
about revealed cards: all three parts) and G-0014 (`FreepsResolveRuleFirst()` checks that what is pending is
the required-responses ordering: "This will cause a great number of test changes downstream of this, but
approved nonetheless").

## Rulings of 2026-10-05, evening

**Historical formats are left alone (F-0014).** "We won't touch these formats. There is one card that can cancel
a RB skirmish, and there's no reason to alter the historical format." King block's settings stand.

**X counts what was actually returned (F-0016).** One Dwarf in Moria (101_5) "should only count cards
successfully returned to their owner's hands, not ones discarded after the fact, even if they were slated for
return." The definition now remembers the items chosen and wounds one minion for each.

**"From your support area" is a requirement where the card says it (Q-0022).** "If the card explicitly states
'from the support area', then it clearly can't bounce." Pale Crown (101_39) does, so its ability is not offered
once the Crown is on a Nazgul. Where a card says only "transfer this" (many fortifications), whether it may be
moved again from its bearer "is completely unknown if that was intended or not" and is case by case: ask, card
by card, and do not report it as a finding.

**Being made to play the card found (Q-0022, rider).** That a "play ... from your draw deck or discard pile"
card with something playable in the deck makes the player play it (the choice has a minimum of 1) "looks like a
bug to me": CR 4.2 "draw deck" lets a player choose not to. It is the engine's `mustExist` that does it
(F-0042).

**A discard that follows from its bearer leaving play cannot be responded to (Q-0023).** "When we're responding
to cards about to be discarded with the intent of preventing their discard, we can't usefully respond in the
case of bearer death. Where would the prevented-discard card go afterwards? ... we won't respond to cards
discarded on bearer death because there's no meaningful place to put them, usually." (V2 Haldir's wording was
changed for this.)

**Who is shown a reveal (Q-0024, Q-0025, F-0039).** A player who needs to see the card is shown it: the
opponent of a card revealed from a draw deck by the printed word "reveal" (F-0021), everyone for an opened
booster (F-0039: "it should reveal it to everyone just for fun"). The owner of a hand that cards are revealed
from gets no pop-up ("which blocks play until dismissed"), but the game log must say which cards were revealed:
"If the log isn't being populated that needs addressed." A card taken from a draw deck by type, where the text
does not say "reveal", is revealed by the log line alone: "The log has links any player can click to see what
card it's talking about."

**"You may" is asked (Q-0021).** Their Eyes Fell Upon Him (101_41) drawing without asking "wasn't intentional,
that's an oversight." Changed to an optional trigger. 68_50 and 13_75, built the same way, are for their own
phases (F-0041).

**Items on [shire] companions (Q-0026).** Bilbo's two items "can go on multiple": each goes on any [shire]
companion, the same one or another.

**Fixes agreed.** F-0033 (Gwaihir: "up to 2" is 0 to 2, "clearly an oversight"), F-0035 (The Dark Lord Advances:
"a clearly broken implementation"), F-0036 (Out of Dark Waters' action "should be fixed to not be available all
the time"), F-0038 (Pale Crown: "very nice find ... definitely good to fix and test for it"), F-0039 (92_29).
F-0037 (the Hobbit Gandalfs sharing Saruman's flag) is confirmed and waits for the Hobbit sets: "Those hobbit
cards should have made their own flag."

**The sheets (2026-10-05, evening).** 58_57 Corsair Marauder: the sheet's row is the revision 1 that was
released, and the definition "is an old candidate and needs altered to match the sheet." 67_7: helper text
"should be removed from the game text (so it isn't interpreted as a secondary cumulative effect). In print those
are italicized and ignored." Of the wording-only differences, 60_8 keeps its wording ("so players right-clicking
on it see the rounding-down ruling"), 53_17 is revision 2 with revision 1 missing from the file, and "all other
cards in this section should be altered to use the wording in the sheet." `PC_Cards.js` shows which revision's
image is in use and is one more source. On revision markers: a later revision was copied from the one before,
so a live block mislabelled with the earlier number is the common fault; look in the comments for the old
revision before concluding anything.

**"Here" on a site is the site's title (Q-0027).** "Yes, if the Farmer is out on site 1, you should get double
the benefit, but only on site 1. 'here' is supposed to be a synonym for '[title]'." An ally who has a site's
game text (Hobbit Farmer, Uruviel, Silinde) therefore makes the site's "here" offer too, while the fellowship
is at that site. Bag End Study (101_57) was right; Lorien Throne Room (101_61) required the site itself and is
corrected to go by its name (F-0043).

**"One" stands for the noun before it (Q-0028).** On Rivendell Gateway (101_59), "the intent is (when game text
is at such a premium as it is for sites) 'one' is a stand-in for 'an unbound companion'."

**"May draw a card for each" is one offer (Q-0029).** "It's better UX for it to be one big batch or not. The
vast, vast majority of the time, the player will want to draw, and they will not be amused needing to hit yes
for every individual card draw." And with an empty draw deck: "while it is technically a pester to ask them when
they have 0 cards in their deck, that is both rare and a bit of a chance for them to *realize* they have no cards
in their deck ... I'd say let it ask with 0 cards in deck." She spoke of the empty deck only. Whether the offer
should be made when the count itself is zero (four companions or fewer) was Q-0033, ruled below.

## Rulings of 2026-10-05, night

**A draw of nothing because the count is zero is not offered (Q-0033).** "If there's 4 or fewer companions,
there's no reason to nag the player with 0 draws, yeah. Let's make it smarter." Nen Hithoel (101_63), Orthanc
Library (4_362) and Summit of Amon Hen (1_362) now require something to count before the Shadow player is asked.
The empty-deck offer of Q-0029 stays. Other "may draw a card for each X" cards are to be read the same way as
they come up: an offer with X at zero is a finding, kind hjson-logic, already ruled.

**Bilbo, Of Bag End (101_50) had no requirement to play (F-0040).** "Implementation is wrong, needs fixing."
Fixed.

**Passes stay harmless (G-0015).** "Let's leave passes harmless for now." And the names: "we should really use
FreepsPass and ShadowPass rather than the (needlessly) wordier FreepsPassCurrentPhaseAction /
ShadowPassCurrentPhaseAction." Tests are written with `FreepsPass()` and `ShadowPass()`.

**Corsair Marauder (58_57) and the history of a card's revisions.** "The current implementation is good, it just
needs to have its revision number updated." The earlier implementation "should be put into a comment above it in
the HJSON, as should any of the other missing revisions, so that the file has a one-stop-shop for history." So
an errata definition file carries every earlier revision of a card as a commented-out block above the live one,
oldest first, and the live block's `revision:` is the number of the revision released.

## Rulings of 2026-10-06

**An optional action that would do nothing is not offered: now a rule, with two exceptions (Q-0034).** "Sure,
start treating it as a rule, except in these cases: Events. People often want to play dud events to get them
out of their hand. Situations with a cost. Oftentimes the cost may incidentally be beneficial even if the effect
fizzles, such as exerting an enduring character." So: an optional trigger or action with no cost whose whole
effect would do nothing (a draw of zero, an X of zero) is rightly withheld, and offering it is a finding. An
event is offered whenever its own requirements and costs are met, whatever its effect will do. An optional
action or trigger that has a cost ("you may exert X to ...") is offered whenever the cost can be paid, even if
the effect will do nothing; a definition that adds a requirement to withhold it is a finding (Naith Armaments
102_10 had one). Do not ask about these shapes again.

**A branch in parentheses is mandatory when it is available (F-0047, Q-0035).** "When there are parenthesis like
this, it's actually mandatory to use the parenthesis over the portion outside the parenthesis, so the player
doesn't have a choice", and "Decipher ruled that a branch in parenthesis is mandatory to choose if it's
available." "Exert a valiant companion (or spot 2 valiant Elves)": with two valiant Elves to spot the game spots
and never offers the exertion, and is right. "Wound a minion (or wound a minion twice if each of those Elves
bears a ranged weapon)": if two valiant Elves bearing ranged weapons can be spotted the minion is wounded twice;
the player is not asked which Elves and cannot choose the lesser result.

**Looking through a draw deck never forces a card to be taken (F-0049, F-0051).** "Those should all be an
effective 0-N choice." Taking into hand, like playing (F-0042), has a minimum of 0 once the deck is being looked
through.

**Playing from the discard pile as an effect is a cost, the event's own twilight included (F-0046).** "It should
block until both can be paid for; rules state that effects playing from discard pile have to essentially be
treated like costs."

**"Spot X to Y" does nothing without X, even as one branch of a choice (F-0053).** "When you're given a choice,
you have to pick one that you're 'fully capable' of performing. But when an effect cannot be fully performed,
you 'do what you can and ignore the rest'. That can lead to some unexpected edge cases. That said, 'spot X to Y'
should clearly not resolve if X isn't around, even if it's one branch of a choice. This is clearly an engine bug."

**What a special ability is (Q-0039).** "It would count, if it's got the bold text and a colon." A labelled
ability ("Shadow: ...") used from the discard pile or while stacked is a minion's special ability and Gandalf,
Lathspell answers it; the exertion of a minion that is not in play does nothing. A permission with no phase word
("you may play this minion from your discard pile") is not a special ability and is not answered.

**A random reveal from a hand gives its owner no pop-up either (Q-0036).** "Doesn't need the popup, just the
event log."

**Other answers.** Q-0030: Watcher in the Water replacing a tentacle "is being removed from its assignment, which
may cancel it if it's the only minion in its skirmish". Q-0031: "[rivers] and [sites with no other terrain
keywords] gain marsh". Q-0032 (102_4): the "you may play" possession "should be optional, an implementation
oversight". Q-0037 (102_7): "The card probably shouldn't have had may on it ... Let's go ahead and fix it to
match the bad wording": the additional reinforcement is asked. Q-0038 (102_8): preventing a discard that was a
cost "is permitted, but it will cause the cost to no longer be considered paid". Q-0040 (102_77): continuous;
"we ruled that with the Shadowfax, GOTM ruling and applied it to Anduril as well." F-0044: the Shadow player
picks which tentacle Watcher replaces. F-0054: the Response type is to take a per-turn limit and the Staff is to
be a Response. G-0016 declined: "There are no cards that put more than one kind of culture token on another,
and no plans to in the PC."


## Rulings of 2026-10-06, evening

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
  cards finished and in progress.

**The no-effect flag (new engine feature).** "On decisions sent to a player, let's add a flag of 'no effect',
meaning the action is determined to have no effect ahead of time. In addition, for skirmish actions only I want
that flag to be able to detect that it will have no effect on a currently-skirmishing character, for the
extremely common case of 'I used my hobbit pump on a gondor skirmisher'. ... such actions highlight with a
different color, which will permit players to make such actions should they deem it strategically necessary."
The campaign then checks the flag "in cases like Breaching Shot and Trail of Savagery, where we had for the
player's convenience disallowed the card use in some legal-but-pointless situations. I don't want us to test
literally every card for this flag, just some mechanic tests on common use cases and the occasional conversion
of convenience guards." It does not replace the Q-0034 rule: an optional action with no cost that would do
nothing is still withheld; an event, or an action with a cost, that would do nothing is offered and flagged.

**An ability at its limit is offered, flagged no-effect (Q-0041).** "Technically speaking it should be offered,
and some cards (Beneath the Mountains) are famous for using the cost over and over in spite of the effect limit
being reached." The definitions that withhold an ability at its limit are converted when the flag exists.

**A transfer is not "coming into play" (Q-0053).** "Transferring 'is not playing' and so specifically does not
count as 'coming into play' for the purposes of having the twilight cost altered." Other cards' twilight cost
modifiers do not reach a transfer. "We're going to have to alter Asfaloth as well."

**"Control(s) a site" goes by the count of controlled sites (Q-0054).** "All the cards you mention and those
using the same language should be retrieving the count of controlled sites, going from 0 -> 1 and
enabling/disabling their text accordingly." A card that needs the site itself (stack on it, liberate it) still
needs a real one.

**Maps are information only (Q-0055).** PC-Movie runs under Multipath rules: any legal site path, cards of that
path only. "All the Map does is announce which path the player is using, and it is revealed in the player's
support area before the game begins, so it is information available during bidding. Maps therefore have no
ongoing in-game effect."

**Controlled sites are not discardable (Q-0056).** A card that discards "a card" from a support area does not
reach a controlled site.

**Hinder and restore.** A card hindered in the middle of its own ability still carries the ability out, and an
effect that names its bearer still finds him (F-0099: "the -1 strength should still apply"; the old stub
HinderingACompanionMidAbilityAlsoCancelsTheAbility "was the old design but it was altered"). But a token is not
added to a card hindered before the token arrives (Q-0070: "can't > can; tokens cannot be added > continue to
execute"; "the same logic is what makes Anduril, Legend Remade successfully block a wound"). A reconcile
restores the player's hindered cards before the discard (Q-0059, CR 5.0; the engine is to be changed). Cards
already hindered stay out of hinder choices (Q-0060: "for QOL purposes let's continue to leave hindered cards
out of the filter"). Shadowfax 103_5: "Minions skirmishing bearer gain no strength or keyword bonuses from
Shadow cards borne by minions skirmishing bearer" (Q-0061).

**Playing a card as an effect, from any pile, is a cost (Q-0062, Q-0066).** From hand: "it should enforce a card
being there and playable before playing"; where an effect plays a card several times, "we should at least
confirm that the first play is possible". The dead pile is "covered for the same reasons that the discard pile
is", and I Call on You "should properly withhold itself if the threat cost of the wraith to be played cannot be
paid". Replacing a character from the discard pile: "treat this as if playing from discard for rules purposes;
if it's not in the discard don't offer it." The replaced minion "simply 'becomes' another persona, without
skipping a beat or interrupting any prior tracked effects"; "another" means another subtitle (Q-0067).

**Responses and triggers.** "Each time ..., you may" is a trigger, not a response (Q-0069 on Great Gate: "it
should be a trigger as the text uses"). A response stops being offered once what it answers has been cancelled
or prevented (Q-0043: "normally when responses react to a trigger, they should stop reacting if that trigger is
prevented by a prior trigger"). An optional trigger added by an event belongs to the player who played it
(F-0083).

**Reading card text.** A race word with no "your" names no side: "Wraith is Wraith" (Q-0068), "Bearer must be a
Wizard" lets the opponent's Saruman bear Wizard's Stash (Q-0058). A sentence of costs with no "to" is still
costs where the next sentence is what they buy (Q-0058). An X of exertions is the player's, up to those able to
exert, whatever it can buy (Q-0069). A companion spotted as part of a cost is still "that companion" when the
effect is carried out, though paying the cost ended the skirmish (Q-0065). "Play this" on a card in play is a
transfer, from the discard pile a play (Q-0057). An exact list of sources ("from play or from hand") leaves the
others out (Q-0047). A required trigger's spot is made whenever it can be (Q-0050). "Prevent X from being
overwhelmed unless its strength is tripled" on a minion spares only that minion, by its own strength; overwhelm
is resolved minion by minion (Q-0044).

**Card by card.** Q-0042 Breaching Shot may reach an ally away from home ("they are breaching the wall"). Q-0045
Wolf Pit as built. Q-0046 Royal Guard: only the keyword lasts. Q-0048 Now for Wrath: "your", and the text gains
the word. Q-0049 Now for Ruin as built. Q-0051 Trapped in its Gaze: this skirmish ("it should have been worded
'to make a skirmishing twilight minion'"). Q-0052 Eastemnet Hills: from hand, Ketura updates the wording.
Q-0063 Haste: the title restriction stays. Q-0064 Sticky Strands may be pulled from another companion.

**Later the same evening (Ketura, 2026-10-06).** The reviewer's single mistakes and the `--with-parked` run stay
"for now". Q-0051 Trapped in its Gaze 102_66: "go ahead and change the text + implementation" to "a skirmishing
twilight minion". F-0067 From Deepest Dungeon 102_39: "X should only be permitted where it makes the Balrog
playable, since we are playing from discard and thus must be able to play it." F-0087 Gorgoroth Keeper 10_86:
"Do a requirement for now; I'm not certain an engine-level addition is warranted." F-0090 (the deck restriction's
min read from its max field): "go ahead and fix."

**Later still (Ketura, 2026-10-06).** Q-0071 Pippin, Trollslayer 103_31: "race *is* an unloaded keyword ... treat it
as if the text says '(except Race)' at the appropriate spot, updating the text as well." Q-0072 Tormented Revenant
103_32: "spot or add 2 threats" is the player's choice when both can be done. F-0109 and Q-0073, the twilight cost
of a card in play: it is the number printed on the card; neither another card's text nor its own changes it once
it is in play ("For now, we will follow the rules as written, but I'm intending to change that written rule"; "I'll
put it on the docket for us to remove that rule in PC formats, but don't take that into account"). The no-effect
flag: abilities that play a card from a pile stay withheld when nothing can be played; a skirmish action is
flagged for missing the skirmish only when it is a pump or a cancel that reaches nobody fighting ("If it actually
results in something hitting the table, it's fine to leave noeffect off of it"); auto-pass is left alone for now.

**On the details pass 4 settled (Ketura, 2026-10-06, Q-0074).** A minion spared from an overwhelm by its
protection takes the ordinary wounds for losing and is not "overwhelmed" for other texts: "Very good." Winning by
controlling five sites "is from the very obsolete 2nd edition; it's not worth considering in the future." Sticky
Strands' timing as built: "good". A transfer does not take the card's own cost text into account either: "Based
on a strict reading of the twilight cost rule, transfers should not take self-discounts into account. It's stupid
and asinine, but it's RAW." A Map reaching the support area after the bid is "just a detail about how it's
presented in the client ... the information is there all the same".

**Late on 2026-10-06 (Ketura, on Q-0075 to Q-0078 and F-0112 to F-0116).**
- "Exert X <cards>" is an exertion cost and X is at least 1, as with "exert X times" (Q-0076, A Wizard Knows
  Better 103_40: without Saruman no companion is hindered for no exertion). X of zero by SPOTTING is still
  allowed. This narrows "X may be zero" of 2026-10-05: any X of exertions is at least 1.
- A card that turns itself into a minion from the support area offers that ability from the support area only
  (Q-0077, the Bladetusks 103_41, 103_42, 103_43): never again while it is that minion. The requirement in
  their definitions is right and is not a convenience guard to convert.
- A sentence after such an ability that calls the card by the minion's pronoun is part of the ability (Q-0078,
  Bladetusk Matriarch: "She adds 1 to the Shadow archery total" holds only while she is the minion).
- "Each time your condition is hindered or discarded" answers the card's own hindering or discard (Q-0075, One
  Ill Turn Deserves Another 103_38).
- Reinforcing more than one token: "Should let multiple tokens be selected one at a time" (F-0113). Write it as
  a `Repeat` round a `ReinforceTokens` with no `times`; `times` on ReinforceTokens is only for "reinforce a
  token twice" (the same card).
- "Its skirmish" on a minion means the skirmish that minion is in: "While merely assigned to a skirmish, it is
  not in an active skirmish and so that text should not be active" (F-0114, Corsair Quartermaster 103_45). A
  modifier that counts over `InSkirmish` needs `self,InSkirmish` as its own filter.
- A choice whose every branch has nothing left to act on is not put to the player: "the prompt should be
  smarter" (F-0116, Savage Fury 103_39).
- Repeated lines in deck validation errors are "fine as-is" (F-0112): not a finding.

**2026-10-07 (Ketura, on Q-0079, F-0120, F-0121).**
- "By another card" in a card's own text is a card other than THAT card, as "another X" is everywhere else
  (Q-0079, Corsair Helmsman 103_46: "The wording is meant to prevent Helmsmen from responding to itself
  hindering itself"). A [raider] minion that hinders itself when played is answered by a Helmsman.
- "Each time this <card> is hindered" is answered after the hindering, not when it is about to happen
  (F-0120, Desert Wind Scout 103_48): "If the after-hinder works, I'm fine with switching to that; I think I
  did it as a before-trigger because I was concerned that the after-trigger would fail to connect due to its
  source being inactive due to the hinder."
- A card's own hindered-from-play (and discarded-from-play) trigger is carried out as that card and its owner,
  not as the card and player that removed it: "fix it in the engine" (F-0121).
