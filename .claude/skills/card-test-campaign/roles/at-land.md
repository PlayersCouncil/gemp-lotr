# Brief: landing a batch of converted `at` tests

A batch of the game's old `at` tests has just been replaced by tests written to the Card Test Standard: new tests
in `mechanics/` classes or as lone tests in cards' own files, and the old methods deleted from `at/`. You check
that work, put right the faults in the game it turned up, give every finding, question and rig request its
number, work what was learned into the documents, and commit. The batch's name and the converter's report are at
the end of this brief.

You are the last pair of eyes. The orchestrator sees only the few lines you return, and Ketura reads the
registers and the commit. She asked for these tests so that nothing the old ones guard is forgotten, above all
"the tricky ones, the ones about specific interactions between cards"; the plain ones are kickstarts for their
cards and need not be polished.

Read in one step, in full: `.claude/skills/card-test-campaign/roles/lander.md` (the lander of a batch of cards:
its steps 3, 4, 5 and 6, on findings, questions, rig requests and the documents, are yours unchanged, with the
commands they give; its steps 1, 2 and 7 are replaced by what follows);
`.claude/skills/card-test-campaign/references/rulings-index.md`; in
`.claude/skills/test-generation/SKILL.md` the sections "Rules every test follows", "Tests that are not about a
card" and "When something is in the way"; your batch file,
`/mnt/user-data/outputs/card-tests/orchestrator/at-convert/<your batch>.md`; and
`/mnt/user-data/outputs/card-tests/orchestrator/at-survey/DECISIONS.md`. Look other things up as you need them
(`lookup.py`, `packet.py <id> --blind`, `rules.py term`). The machine is small and has restarted under load: run
ONE Java process at a time, and never the whole suite.

## 1. Check the batch against its directives

`git status --short` and `git diff` show what the converter did. For each test the batch file lists:

- It is there, under the name given (or a better one, with the reason), in the home given.
- **It reads what its directive says** (the entry of `at-survey/<File>.md` headed with the test's name) and what
  DECISIONS.md says where that differs: the board, what is done, and every reading. For a test of an
  interaction, a rule or a bug, be exact: would the test fail if the thing it guards were broken? If a control
  is missing that would tell it from a test that passes for another reason, add it.
- **Nothing the old method asserted is lost**: `git show HEAD:<the at file>` has the old method. What it read and
  the new test does not is either added or named in your report with the reason.
- **The Standard's rules for any test**: the rig's own methods, a check of what the game is waiting for after
  the last play or answer, no exception relied on, real cards on a board a game could reach (no card given a
  race, culture, type or side it cannot have; never two cards of one unique title in play; no Frodo beside a
  Ring-bearer who is not Frodo). `lint.py --form <file>` for a mechanics class and
  `lint.py --form <file> --only <the batch's tests in it>` for a card's file must pass.
- The old methods the batch names are gone from `at/`, nothing else is gone from it, and what is left of each
  `at` file still passes: `cardtest.py run <the at file> --out <batch>-l`.
- Every new or changed test file runs: `cardtest.py run <files...> --out <batch>-l` (parked tests apart).

Put right what is small and plain. A test you cannot make sound comes out again, its old method goes back
(`git checkout HEAD -- <the at file>` and delete the others of the batch again, or restore the one method by
hand), and you say so.

## 2. Findings, questions, rig requests, documents

As `lander.md`, steps 3 to 6, with these differences:

- A card whose DEFINITION you correct for a finding here has, as a rule, no campaign tests but the lone one:
  apply the correction, take the `@Ignore` off, run the card's file, and run the tests of every card that uses it
  as a helper (`landing.py users <id>`, then `cardtest.py run <ids> --out <batch>-users`). A correction that
  breaks another card's test in a way you cannot put right in one line comes back out and the finding is
  recorded `confirmed`.
- There is no ledger. A reading the rig cannot give stays a comment in the test
  (`// waits: G-0031 <what the rig lacks>`, with the request's real number).
- A question's parked test carries the question's number in its `@Ignore`.

## 3. Commit

    python3 .claude/skills/card-test-campaign/scripts/landing.py commit \
        "At tests: <batch>: <n> tests in <the homes, shortly> replace <m> old methods" \
        <every test file added or changed> <every at file changed or deleted> <every definition corrected> \
        --body-file /tmp/<batch>-l/body.txt

names exactly the paths that go in (a deleted file is named too). The body lists the homes with their tests, the
old methods gone, and the findings fixed, by number. Your changes to the documents are a second commit:
`landing.py commit "Skills: what at-<batch> turned up" <the documents>`. You run no other git command that
changes anything.

Then write the batch's report, `/mnt/user-data/outputs/card-tests/orchestrator/batches/at-<batch>.md`, for Ketura
and the next orchestrator: the homes and their tests (active or parked, on which number); the old methods
deleted and any left, with why; each number filed, with one line; each definition corrected; what you added or
changed in the converter's tests; and the documentation lists of `lander.md` step 6. Anything that wants Ketura's
word is a numbered question in the register, whole enough to answer on a phone, never only a line of the report.

## What you return

A few lines, and no more: the tests landed (a count, and the names of any parked, with their numbers); the old
methods deleted (a count) and left (names); the numbers you filed, by kind (fixed findings, findings still open,
questions, rig requests); `needs_orchestrator` (only what somebody must act on and no register holds; usually
empty); and the commit, as `landing.py commit` printed it.
