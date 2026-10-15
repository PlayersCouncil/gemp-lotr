# Brief: author

You are writing the unit tests for a few cards of the Lord of the Rings TCG as implemented in Gemp, as part of a
campaign to test every card the same way. The cards and a name for your working folder are given at the end of
this brief.

Before anything else, read these three files in full, in one step (three `Read` calls side by side: a single
`cat` of all three is cut off by the output limit). The Standard is longer than one `Read` returns: read it in two
calls, the second from the line where the first stops (`offset`), or its end ("When something is in the way", the
mechanics, the reviewer's list) is missed. Everything below assumes them:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard. What a card's tests must cover and how the
  file is laid out.
- `.claude/skills/gemp-testing/SKILL.md`: the test rig, with an index of the behaviours that catch people out
  and one of the helper cards the campaign has found for a need (both files are looked up, not read through).
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
    python3 .claude/skills/card-test-campaign/scripts/lookup.py "who is not \[shire\]" --in helpers

An entry is printed only when it contains every term given, so look up separate subjects with separate
commands (chained with `;` in one step), not with one command of many terms. While a card is in stage 1, every
lookup takes your batch's ids as well, `lookup.py <terms> --skip <your batch's ids>`: what names one of your
cards, another version of one, or a `mechanics/` class built on one says how it is built, and is left out: the
whole entry where its first sentence does (named by its heading), else those sentences alone (a ruling is printed
whatever card it names). A broad word matches many long entries: `--max 0` lists them by their first words, to
choose from. Look again in stage 2, without `--skip`.

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
from that alone write the card's plan, `/tmp/<your folder name>/<id>.plan.md`: the clauses as the ledger will
hold them (id, type, checks, text), and under each check one line: the situation, and what the text and rules say
happens. A card's play has no clause of its own (Q-0279: B): it is tested only where
the card's text bears on it (a bearer, a requirement or an extra cost to play, a cost its text changes, a card that
answers its own play), by the test of that clause, which plays the card from hand. The plan is stage 1's one file,
saved before going on; the ledger file is written once, in stage 2, from the plan (its Sources, Findings, Dropped
and Notes lines may go in at once), and its claim lines added with `Edit` as the tests exist. Do not open the
card's `.hjson` file or its existing test file during this stage. If the text leaves a real doubt about what should happen, write the doubt down as a
question with both readings; do not settle it by guessing, and do not settle it in stage 2 by seeing what the game
does.

A `grep` of a `.hjson` file prints whatever lines match, and a line range of one (`sed -n`, a `Read` with an
offset) whatever cards lie there, those of your other cards and their originals among them. One card's block is
read with its packet (`--only-withheld`, in stage 2), never out of the file; and to look for a helper or a
printed text in stage 1 (a follower's Aid, a phrase), use `packet.py --find <regex> --skip <your batch's ids>`, never
a grep of the `.hjson` files, not even one that prints only `gametext:` lines
(printed fields and game text with ids, nothing of the implementation, your batch and its other versions left out) and
`packet.py --defs <ids> --skip <your batch's ids>` for a chosen helper's definition (it also keeps shut a helper
whose game text is a batch card's own with a word or two changed, or prints a whole sentence of one: choose another, or read it in stage 2; asked
for more than about twenty ids it overruns the output limit, so send it to a file under your folder and `Read`
that). You may read another card's finished test file of the same shape, and its ledger, so long as that card is
neither one of your batch nor another version of one (the Decipher original of an erratum, and the landed ERRATUM
of an original, whose test file and ledger stay shut until stage 2 as your own files do: 52_75 for 2_75) and the
file does not name one. `packet.py --landed <your batch's ids>` lists the test files, `mechanics/` classes and
ledgers that name one (by id, by test class, or by a name only it has, Crags of Emyn Muil), each open or shut, and
prints nothing of what they say: run it before opening any. A test file or class that names a batch card only in a
line of its scenario map (a helper laid, a site of a path: every Shadows-path file names 11_234) and never fetches
it is open; one that names it anywhere else is shut (Dimrill Dale 1_350's Notes describe Great Chasm 2_118's
definition; `CostOfAssigningTests` is built on Morgul Spawn 7_200), as the shapes and pitfalls that name a batch
card are. A ledger is judged apart from its test file: one that names a batch card is shut, since no script can
tell a board in its Notes from a description, and the test file beside it may still be open. Another card that
shares a title with one of yours (a second Denethor) may be read as a helper like any other, and a landed card
with the SAME text as yours, or a sentence of it, is the quickest source of boards and helpers (`packet.py --find`
on the first sentence of the game text: Uruk Veteran 4_201 is Uruk Trooper 4_199 word for word): its test file and
ledger are open whatever they name (Merry 17_107's ledger names Pippin 17_109, whose first sentence it prints; the
twins rule of notes-decipher.txt), and only its definition is shut (`--defs` says so).

**Stage 2, with everything.** Run `packet.py <id> --only-withheld`, which prints what the blind packet held back
(the definition in full, sheet against definition, an erratum's original, the test file) and so ends stage 1 for
that card: its plan is saved first. (`--with-test` prints the blind packet and the glossary over again in front
of it.) A long test file overruns the output limit and the middle of it is cut out of the print: where the print
says so, read the test file itself for the rest. An entry the packet heads "A ruling on another card names this
card" is that card's ruling: where its ledger already holds it as an `R` clause (5_60's `R1`, for 8_3), cite it
under `Sources:` and do not test it again.

No test reads a printed cost or bonus doing its ordinary work (Q-0279: E2; the Standard's "No reading of a printed
cost or bonus"): not the pool across a plain play, not a bearer's strength, vitality or resistance for a borne card's
printed bonus, unless the card's text or another card on the table changes it.

No test only plays the card, and the ledger has no `P1` (Q-0279: B, engine pass 31): a plain play is shown once by
`mechanics/costs/TwilightPoolTests` and `WhereACardIsPlayedTests`, and `gen_play.py` writes no tests. Where the card's
definition or text bears on its play, lint's `plain-play` names it and asks for a test that plays the card from hand:
that is the test of the clause it names. A "Plays to your support area." sentence is a `Keyword [K]` clause claimed
by the stats test. A skeleton's commented-out placeholder that only plays the card is taken out (`Dropped:` says it
was a plain play).

If the card already has behavioural tests, go
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

A parked test is run only through `mutate.py`, and always with a `--replace`: `mutate.py <id> --replace <wrong>
<right> --with-parked` runs the card as it is with the parked tests in (that is the run that shows a parked test
failing) and then against the correction. `cardtest.py run` leaves parked tests out, and `--with-parked` with no
`--replace` would run the stock mistakes, which are cut. A line in ANOTHER card's definition (one already corrected that
your card's file owes a test for, or one a finding proposes to change) is tried against your tests with
`mutate.py <that card's id> --replace <old> <new> --test <your card's test file>`, with `--with-parked` where
your test is parked on it. A finding whose correction is in the ENGINE has no `--replace` to give: run a copy of
the class with that `@Ignore` line taken out through `cardtest.py probe`, to see the test fail at the reading it
is parked for; where the fault makes the tests of one class disturb one another (each reads what an earlier one
left, Into the Caves 17_100 under F-0487), the copy holds that one test alone. A test the game passes is not
parked (6_63's kill test, written for F-0444, passed).

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
   that has its number already goes in under that number, but where an ERRATUM's finding says the original
   "still has it", the original's copy is a finding of its own, `F-0000`, that names the erratum's number), and report
   it as a finding with: what the text says, what you expected, what happened, and your best guess at the kind
   (typo in the definition, oversight in the implementation, wrong displayed text, engine bug, rig bug).
   A finding with two causes gets a parked test for each, or half a correction passes the one. So does one
   cause with two symptoms read at different decisions (the offer; the choice between alternatives), where a
   correction of one would pass the other.
   Where the correction changes how every positive test is driven (a "you may" built as a required trigger,
   correction `optional: true`), every test that takes or declines the offer asserts the offer and is parked;
   lint's `parked-only` warnings on `+`, `O` and `R` are then expected until landing.
   A definition that does not do what its text says is fixed at landing by the orchestrator without waiting for
   Ketura (her rule since 2026-10-07), so for every finding show the correction (the `mutate.py` line that makes
   your parked test pass with every active test; where the parked test reads a question the correction ADDS,
   `FreepsDecisionAvailable("Would you like to reveal")`, the finding gives the correction's exact `text:`, since
   the test binds to its first words) and say in one sentence whether the departure looks like an
   error or typo (the usual case) or looks DELIBERATE (a comment in the definition, a workaround for play, a
   ruling that might explain it). Only the deliberate-looking and the ones you are unsure of go to her first.
   Where the right correction is a change to the ENGINE: show a definition-only correction that makes the parked
   tests pass, if there is one (it is what can land), and name the engine change beside it; if there is none,
   give the engine lines at fault and what they would have to do (Shrouded Elf 63_23, F-0287).

Be slow to conclude the game is wrong. Most of the time it is, when you think so; but a correct implementation of
an obscure ruling looks exactly like a bug, and a wrongly reported bug costs more than a question. What follows
from the ORDER in which a player takes his own required triggers is his own doing and no finding (Ketura on
F-0389): the test takes the order that gives the text's result, and none is parked for the other.

## What you may and may not touch

You edit the test files and ledger files of your own cards, and files under `/tmp/<your folder name>/`. To find out what the game
does in some situation without adding a test, put a throwaway JUnit class there and run it with
`cardtest.py probe <file.java>`; use this to understand a failure, never to decide what a test should expect. Nothing else: not card
definitions, not the engine, not the rig, not other cards' tests, not git, not the campaign's state files.
A test that is owed in ANOTHER landed card's file (shapes.md says where: a Ring's scenario in
Card_01_224_Tests) is drafted under your folder and named in your report; the lander adds it.

A test shows what a card that exists does (Ketura, 2026-10-11: `lookup.py Q-0241`). No card is invented, and no
definition text is stapled onto a card to stand in for one. Where NO printed card can make the board a check
wants, the check is not shown: its `no <check>:` line, or Notes, says what could not be shown and names the
search that found no card.

Nor is a card owed what a mechanics class holds (Ketura, 2026-10-12): a card that bars an act is tested on ONE
act it stops and its control, and a card of each definition word that does the act is the mechanics' (Q-0264:
B2); an exertion is not owed as the nearest miss of a wound trigger (Q-0265: A), nor a wound as the nearest
miss of an "each time X exerts" trigger (Q-0315: A, `mechanics/wounds/AWoundIsNoExertionTests`); the account-name check is
shown once, card-agnostic (Q-0271: C). A choice the game carries out unasked is no such thing: it shows
nothing of the invalid card, so a second valid candidate is laid and the invalid one read "not offered"
(Q-0266: B). The Standard has each where it applies.

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
Wizard allies; Bilbo 12_119 is a Ring-bound Hobbit companion). And before leaving a candidate out because its
own text keeps it off the board, look for the card that lifts that: "may not be assigned to a skirmish" on a
Saruman is lifted by Saruman's Staff 4_174, which any Saruman bears, so Saruman, Rabble-rouser 4_33 IS the
[dunland] minion who is no Man for "a [dunland] Man wins / loses / is in a skirmish" (`lookup.py 4_33 --in helpers`).
