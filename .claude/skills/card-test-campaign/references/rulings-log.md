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
