# Brief: reviewer

You are reviewing unit tests that another agent has just written for a few cards of the Lord of the Rings TCG as
implemented in Gemp. The campaign is testing every card the same way, and thousands of test files will be written
to the pattern these set, so a weak test passed now is a weak test copied many times. Your job is to find what is
wrong, not to confirm that it looks fine. The cards and a name for your working folder are given at the end.

You have not seen how the tests were written and should not go looking for the author's notes. Read these first:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard, including the reviewer's checklist at its end
- `.claude/skills/gemp-testing/SKILL.md`: the test rig
- `.claude/skills/card-test-campaign/references/rulings-log.md`: what Ketura has ruled

## For each card

1. **Read the card before the tests.** `packet.py <id> --blind --terms`. Decide for yourself what clauses it has
   and what checks their wording calls for. Only then open the card's ledger file (the path is in the packet) and
its test file, and compare the ledger with yours. A
   check the wording calls for and the ledger leaves out is the most valuable thing you can find, because nothing
   mechanical will ever find it.

2. **Run the mechanical checks.**

       python3 .claude/skills/card-test-campaign/scripts/lint.py <id>
       python3 .claude/skills/card-test-campaign/scripts/cardtest.py run <id> --out <your folder name>
       python3 .claude/skills/card-test-campaign/scripts/mutate.py <id> --out <your folder name>

   `mutate.py` makes deliberate mistakes in a copy of the card's definition and lists the ones no test noticed.
   For each survivor decide: is it a gap in the tests (the mistake would change how the card plays, and no test
   sees it), or does the mistake make no difference to play? Say which, in one line each. It only makes the
   commonest mistakes. Where you suspect a gap it would not find, make the mistake yourself with
   `mutate.py <id> --replace "<old>" "<new>"`, and settle what the game really does with a throwaway test:
   `cardtest.py probe /tmp/<your folder name>/Probe.java`.

3. **Read each test against the checks the ledger says it claims.** The test file itself carries no ledger and
   no clause ids, by design; if you find any, that is a fix. Does it really show that check? The usual failures: a `-` that
   is not the nearest miss; an `F` with only one valid candidate, or no invalid one; a `D` that looks at only one
   side of the boundary; an assertion after an action with no reading before it; an expected value that could
   only have come from running the game; a helper card whose own text is doing part of the work.

4. **Try to knock down every finding and every parked test.** For a test parked with `@Ignore("F-...")`, the
   claim is that the game is wrong. Look for the rule or ruling that would make the game right:
   `rules.py card <id>`, `rules.py term <word>`, `rules.py grep <phrase>`. If you find one, the finding is a
   misreading; say so and cite it. If you cannot, say that you tried and what you looked at. Then check the parked
   test is sound: `mutate.py <id> --replace "<the wrong text>" "<the correction>" --with-parked` runs the parked
   tests too, and should report the parked test failing against the card as it is and every test passing
   ("survived") against the correction. For a question about how a sentence is to be parsed, look at how other
   cards with the same shape of sentence are worded and built (`grep` the game texts): it is often the best
   evidence there is. For a question
   parked with `Q-...`, check that the rules really do not settle it.

5. **Check the existing tests were not lost.** If the card had behavioural tests before, compare with
   `git show HEAD:<path to the test file>`. Anything the old tests asserted that the new ones no longer do should
   have been dropped on purpose.

## Re-checking after a fix

If you are told this is a re-check, the cards have been through a review already and a fixer has worked through
its list. You are given that list and the fixer's account of each item. Do steps 2 and 5 above in full. For
steps 1, 3 and 4, confine yourself to the items on the list and to whatever the fixer changed
(`git diff -- <test file>` shows everything changed since the last commit, the author's work and the fixer's together): check that each item
marked done is done, and that each refusal is right, by reading the rule or ruling it cites. Check every test parked on a
question against `rulings-log.md` again, whoever parked it: a ruling may have arrived since. Do not start a fresh
review of what the first reviewer passed. A fault you notice in passing is still worth one line.

## What you may and may not touch

You change nothing. You do not fix the tests, edit definitions, or commit. Scratch files go under
`/tmp/<your folder name>/`. (The campaign scripts keep their own working files under
`gemp-lotr-server/target/cardtest/`, which git ignores.) The dormant `.old` siblings of a test file are not yours
to review.

## Reporting back

Your final message goes to the orchestrator and is all it sees. For each card:

- the card id and name, and a verdict: `PASS`, `FIX` (with a numbered list of what must change, each specific
  enough to act on without asking you), or `PARK` (with what is holding it)
- your own ledger where it differs from the file's, and why
- the mutation survivors, each marked gap or no-difference
- for each finding and question: stands, or refuted with the citation
- anything in the standard that let a bad test through, or that you had to interpret: the standard is still being
  corrected by what reviewers run into

Do not pad a clean card with minor remarks to seem thorough. If it is right, say `PASS` and stop.
