# Brief: author

You are writing the unit tests for a few cards of the Lord of the Rings TCG as implemented in Gemp, as part of a
campaign to test every card the same way. The cards and a name for your working folder are given at the end of
this brief.

Before anything else, read these three files in full, in one step (three `Read` calls side by side: a single
`cat` of all three is cut off by the output limit). One `Read` returns at most about 25,000 tokens, and `gemp-testing/SKILL.md` (and soon `test-generation/SKILL.md`) is longer than that: when a Read stops short of the file's last line, read on from that line with `offset`, in the same step if you can, until you have the end. The second half of the rig's file is the table of helper cards. Everything below assumes them:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard. What a card's tests must cover and how the
  file is laid out.
- `.claude/skills/gemp-testing/SKILL.md`: the test rig, and an index of the behaviours that catch people out.
  `.claude/skills/gemp-testing/references/vts-api.md` lists every rig method by interface. It is long: search it
  for what you need (`grep -n -i wound vts-api.md`) and do not read it through.
- `.claude/skills/card-test-campaign/references/rulings-index.md`: every ruling of Ketura's (she knows this game's
  rulings better than any document) and every shape of card text the campaign has already settled, each listed
  by its first words.

The index is for finding, not for applying. Once you have the packets of your cards, look up every entry that
bears on their text, and every pitfall group their tests will touch, in as few steps as you can:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py Q-0098
    python3 .claude/skills/card-test-campaign/scripts/lookup.py "support area" hindered
    python3 .claude/skills/card-test-campaign/scripts/lookup.py prevent --in pitfalls

An entry is printed only when it contains every term given, so look up separate subjects with separate
commands (chained with `;` in one step), not with one command of many terms.

An expectation rests on an entry's own words, never on its line in an index. A shape the index lists is settled:
do not raise it as a question again, and where the game departs from it, that is a finding. When a test does
something you cannot explain, read `.claude/skills/gemp-testing/references/pitfalls.md` through before deciding
the game is wrong.

## What done looks like

For each card: its test file and its ledger file meet the standard, `lint.py <id>` reports no errors,
`cardtest.py run <id>` passes (apart from tests parked on a finding or question), and you have reported back as
described at the end. A card with a test or two parked on a finding or question is still `READY`: say what is
parked. `PARKED` is for a card you could not finish, and your report then says exactly what is holding it.

The ledger is a file of its own, at the path `packet.py <id>` prints as "Ledger file". Nothing of it goes into
the test file: no ledger comment, no clause ids, no tags above tests. The test file is what Ketura reads, and she
wants it to read as plain tests.

## The two stages, in this order

The value of these tests is that they are worked out from what the card says, not from how it was implemented.
So the order matters and is not a formality.

**Stage 1, without the implementation.** Run

    python3 .claude/skills/card-test-campaign/scripts/packet.py <id> --blind --terms

(for several cards, each into a file of its own, `> /tmp/<your folder name>/<id>.packet.txt`, and the files read
side by side: printed in one step the packets overrun the output limit and the middle of them is cut out) and
from that alone write the card's ledger file: the clauses, their types, the checks each one's wording calls
for (the claim lines come later, when the tests exist). Then, for each check, write one line: the situation, and
what the text and rules say happens. Save this as `/tmp/<your folder name>/<id>.plan.md` before going on. Do not open the card's `.hjson` file or its existing test
file during this stage. If the text leaves a real doubt about what should happen, write the doubt down as a
question with both readings; do not settle it by guessing, and do not settle it in stage 2 by seeing what the game
does.

A `grep` of a `.hjson` file prints whatever lines match, and a line range of one (`sed -n`, a `Read` with an
offset) whatever cards lie there, those of your other cards and their originals among them. One card's block is
read with its packet (`--only-withheld`, in stage 2), never out of the file; and to look for a helper or a
printed text in stage 1, use `packet.py --find <regex> --skip <your batch's ids>`
(printed fields and game text with ids, nothing of the implementation, your batch and its originals left out) and
`packet.py --defs <ids> --skip <your batch's ids>` for a chosen helper's definition. You may read another card's finished test file of the same shape, so long as
that card is neither one of your batch nor the Decipher original of one. Only those are shut: another card that
shares a title with one of yours (a second Denethor) may be read as a helper like any other.

**Stage 2, with everything.** Run `packet.py <id> --only-withheld`, which prints what the blind packet held back
(the definition in full, sheet against definition, an erratum's original, the test file) and so ends stage 1 for
that card: its plan is saved first. (`--with-test` prints the blind packet and the glossary over again in front
of it.) A long test file overruns the output limit and the middle of it is cut out of the print: where the print
says so, read the test file itself for the rest. If the card already has behavioural tests, go
through them one by one against your plan: each either claims one of your checks (keep it, adapted to the
standard), or duplicates another, or covers something your ledger missed (then your ledger was wrong: fix it), or
asserts something your plan contradicts. That last kind is a question, not a mistake to overwrite: existing tests
for PC cards were written with Ketura and often encode a ruling. Then write the test file, add to the ledger
file the line for each test under each clause it claims checks of, and run

    python3 .claude/skills/card-test-campaign/scripts/lint.py <id>
    python3 .claude/skills/card-test-campaign/scripts/cardtest.py run <id> --out <your folder name>

until both are clean. Do **not** run `mutate.py <id>` on its own: the run of stock mistakes was cut from the
campaign on 2026-10-06 (it cost about a sixth of every agent's steps and found no fault in the game). What shows
that a test would fail if the card were wrong is now your own reading of it: for each filter imagine it one word
wider and one word narrower, for each number one off, for each trigger the neighbouring event, and make sure a
valid and an invalid candidate on the table tell them apart. `mutate.py` has two uses left for you. Where you
park a test on a finding whose correction is a change to the definition, show that the parked test fails
against the card as it is and passes against the correction, with one run of
`mutate.py <id> --replace "<the wrong text>" "<the correction>" --with-parked --out <your folder name>`.
And, once a card at most, where only a changed definition can tell whether an invalid candidate is one at all (is
a Shadow possession in its owner's support area seen by a bare `possession`? A probe cannot change a definition),
one `--replace` run tries that one mistake, and the ledger's Notes say what was tried (92_26).

A parked test is run only by `mutate.py <id> --with-parked` (no mutant is needed to see it fail against the
card as it is); `cardtest.py run` leaves parked tests out. A line in ANOTHER card's definition (one already corrected that
your card's file owes a test for, or one a finding proposes to change) is tried against your tests with
`mutate.py <that card's id> --replace <old> <new> --test <your card's test file>`.

Every step you take re-reads everything you have read so far, so steps are what cost. Read several files in one
step where you can (the packets of all your cards, the helper cards you mean to use), and do not look at the same
thing twice. What you write is what takes time: write each file once and put it right with `Edit` (or `sed`),
never by writing the whole file out again, and keep the plan to one line per check.

Choose helper cards by reading their definitions (`packet.py <helper id>`), never by name. Reading a helper
card's definition is fine at any stage; it is only the card under test whose implementation stays shut in stage 1.

If an existing test asserts one reading of something you wrote down as a question, keep that test active, and
report the question with a note that the existing test takes that reading. A new question where no old test takes a reading gets one test,
of the likelier reading: active if the game already passes it, parked on the question if it does not. No test of
the other reading is written until she answers, with the one exception the Standard names: where an existing
test's reading goes against the plain words of the text, the existing test stays active AND a test of the text's
reading is written and parked on the question, ready to go live on her ruling.

## When a test fails

Decide which of three things is wrong, in this order, and say which in your report:

1. **The test.** It drove the rig wrongly, or a helper card did something you did not account for. Fix it.
2. **Your expectation.** You misread the text or a rule. Check with `rules.py term ...` and `rules.py card ...`.
   If the rules settle it, fix the plan and the test. If they do not, it is a question.
3. **The game.** The text and rules are clear and the game does something else. Leave the test asserting what the
   text says, park it with `@Ignore("F-0000: <one line>")` (the orchestrator assigns the real number; a finding
   that has its number already goes in under that number), and report
   it as a finding with: what the text says, what you expected, what happened, and your best guess at the kind
   (typo in the definition, oversight in the implementation, wrong displayed text, engine bug, rig bug).
   A finding with two causes gets a parked test for each, or half a correction passes the one.
   Where the correction changes how every positive test is driven (a "you may" built as a required trigger,
   correction `optional: true`), every test that takes or declines the offer asserts the offer and is parked;
   lint's `parked-only` warnings on `+`, `O` and `R` are then expected until landing.
   A definition that does not do what its text says is fixed at landing by the orchestrator without waiting for
   Ketura (her rule since 2026-10-07), so for every finding show the correction (the `mutate.py` line that makes
   your parked test pass with every active test) and say in one sentence whether the departure looks like an
   error or typo (the usual case) or looks DELIBERATE (a comment in the definition, a workaround for play, a
   ruling that might explain it). Only the deliberate-looking and the ones you are unsure of go to her first.
   Where the right correction is a change to the ENGINE: show a definition-only correction that makes the parked
   tests pass, if there is one (it is what can land), and name the engine change beside it; if there is none,
   give the engine lines at fault and what they would have to do (Shrouded Elf 63_23, F-0287).

Be slow to conclude the game is wrong. Most of the time it is, when you think so; but a correct implementation of
an obscure ruling looks exactly like a bug, and a wrongly reported bug costs more than a question.

## What you may and may not touch

You edit the test files and ledger files of your own cards, and files under `/tmp/<your folder name>/`. To find out what the game
does in some situation without adding a test, put a throwaway JUnit class there and run it with
`cardtest.py probe <file.java>`; use this to understand a failure, never to decide what a test should expect. Nothing else: not card
definitions, not the engine, not the rig, not other cards' tests, not git, not the campaign's state files.

If a check cannot be written with the rig's existing methods, do not write a helper method, do not reach into
the engine, and do not show it by an exception: a card test never asserts that the rig or the game throws
(`assertThrows`, a `try`; Ketura, 2026-10-08, and lint refuses it). Leave the check untested, write it in the
ledger under its clause as `waits <check>: G-0000 <what the rig lacks>` in place of a claim line, and report a
rig request: what the test needed to do, the method signature you would propose, and which rig interface it
belongs in. Such a card lands parked on the request until the rig can show the check; report it as `READY` if
everything else is done.

An EXISTING test that reaches past the rig or leans on a helper method is no exception. It is rewritten with the
rig's own methods, keeping what it shows. A reading the rig has no method for is taken out of it (the whole test,
if nothing is then left) and waits in the same way: the `waits` line, a rig request that quotes the old lines so
that whoever builds the method can put the reading back, and the test's name with the request's number on the
ledger's `Dropped:` line. The card lands parked; it is not held back with `reach` or `helper` errors in it.

Keep your scratch files under `/tmp/<your folder name>/` whatever else you have been told about scratch space:
the reviewer and the orchestrator look for probes there.

## Reporting back

Your final message is read by the orchestrator, not by a person, and is all the orchestrator sees of your work.
When the report is collected as named fields, fill the fields and add nothing outside them; the ledger and test
files are found at their usual paths and need no mention. For each card give:

- the card id and name, and `READY` or `PARKED`
- the number of tests before and after, and the output of the last `lint.py` and `cardtest.py run`
- for an existing test file: what happened to each existing test (kept, merged, dropped as duplicate, contradicted)
- findings, questions and rig requests, each with the detail asked for above
- anything in the standard or the rig documentation that was wrong, unclear or missing: this is early in the
  campaign and the documents are still being corrected by what authors run into

## Working beside other batches

`cardtest.py run` under your folder name has a folder of its own; `cardtest.py probe` shares one folder with
every other agent unless given `--out <your folder name>`. Before calling a filter word harmless because "every
card of that name or race is of that culture", look for one that is not (Aragorn, Defender of Rohan is a [rohan]
Aragorn; Ulaire Attea, Khamul of the East 103_61 is a [raider] Nazgûl; Radagast 31_17 and Saruman 32_24 are
Wizard allies; Bilbo 12_119 is a Ring-bound Hobbit companion).
