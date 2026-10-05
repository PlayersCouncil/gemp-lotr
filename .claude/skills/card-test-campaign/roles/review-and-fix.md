# Brief: reviewer

You are reviewing unit tests that another agent has just written for a few cards of the Lord of the Rings TCG as
implemented in Gemp, and putting right what you find wrong with them. The campaign is testing every card the
same way, and thousands of test files will be written to the pattern these set, so a weak test passed now is a
weak test copied many times. Your job is to find what is wrong, not to confirm that it looks fine. The cards and
a name for your working folder are given at the end.

Nobody checks your work after you except the orchestrator's gate (the conformance check and the card's own
tests). What you leave is what is committed.

You have not seen how the tests were written and should not go looking for the author's notes. Read these first:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard, including the reviewer's checklist at its end
- `.claude/skills/gemp-testing/SKILL.md`: the test rig
- `.claude/skills/card-test-campaign/references/rulings-log.md`: what Ketura has ruled

Every step you take re-reads everything you have read so far, so steps are what cost. Read several files in one
step where you can, and do not look at the same thing twice.

## For each card

1. **Read the card before the tests.** `packet.py <id> --blind --terms`. Decide for yourself what clauses it has
   and what checks their wording calls for. Only then open the card's ledger file (the path is in the packet)
   and its test file, and compare the ledger with yours. A check the wording calls for and the ledger leaves out
   is the most valuable thing you can find, because nothing mechanical will ever find it.

2. **Run the two quick checks.**

       python3 .claude/skills/card-test-campaign/scripts/lint.py <id>
       python3 .claude/skills/card-test-campaign/scripts/cardtest.py run <id> --out <your folder name>

   Do **not** run `mutate.py <id>` on its own: the author has run the whole set of stock mistakes once, and
   running it again is the most expensive thing in the campaign for the least return. The author's account of
   the survivors is given to you below. Read each "makes no difference" claim against the definition; where you
   doubt one, test that one mistake (next step).

3. **Read each test against the checks the ledger says it claims.** The test file itself carries no ledger and
   no clause ids, by design; if you find any, that is a fix. Does it really show that check? The usual failures:
   a `-` that is not the nearest miss; an `F` with only one valid candidate, or no invalid one; a `D` that looks
   at only one side of the boundary; an assertion after an action with no reading before it; an expected value
   that could only have come from running the game; a helper card whose own text is doing part of the work.

   Where you suspect a gap, name the wrong definition that would pass, and prove it with that one mistake:
   `mutate.py <id> --replace "<old>" "<new>" --all --out <your folder name>-m`. One mistake takes seconds. A
   mistake that survives is a gap; one that is noticed is not. Settle what the game really does with a throwaway
   test: `cardtest.py probe /tmp/<your folder name>/Probe.java`.

4. **Try to knock down every finding and every parked test.** For a test parked with `@Ignore("F-...")`, the
   claim is that the game is wrong. Look for the rule or ruling that would make the game right:
   `rules.py card <id>`, `rules.py term <word>`, `rules.py grep <phrase>`. If you find one, the finding is a
   misreading. If you cannot, say that you tried and what you looked at. Then check the parked test is sound:
   `mutate.py <id> --replace "<the wrong text>" "<the correction>" --with-parked` runs the parked tests too. The
   parked test should fail against the card as it is and pass against the correction. Where the author kept an
   old reading active beside the parked one (a question), that active test is the one failure to expect against
   the correction; on a card with two questions each parked test also fails under the other's correction, so
   look for the parked test's own name missing from the failing list. Where the wrong text is in another card's
   definition, use a probe. For a question about how a sentence is to be parsed, look at how other cards with
   the same shape of sentence are worded and built (`grep` the game texts). For a question parked with `Q-...`,
   check that the rules and the rulings log really do not settle it.

5. **Check the existing tests were not lost.** If the card had behavioural tests before, compare with
   `git show HEAD:<path to the test file>`. Anything the old tests asserted that the new ones no longer do should
   have been dropped on purpose, and the ledger's `Dropped:` line should say so.

6. **A flag with a name of its own.** Where the definition sets a flag or a modifier with a name of its own
   (`sarumanFirstSentenceInactive`), `grep` the card definitions for everything that reads it. Any reader the
   card's text does not name is an invalid candidate, and probably a finding.

## Putting it right

Fix what you found, in the card's test file and its ledger file. `roles/author.md` (beside this file) says what
an author may touch, what to do when a test fails and how findings and questions are parked; all of it applies
to you.

- **Change what is at fault and nothing else.** Do not restyle, rename or reorder what you did not fault.
- **A gap** gets the candidate, reading or test that closes it. The one mistake that proved the gap must be
  noticed afterwards: run it again.
- **A fix that changes what a test claims changes the ledger too.** Keep the claim lines, `Findings:`,
  `Dropped:` and Notes true.
- **A finding the author missed** (the game is wrong): the test asserts what the text says and is parked with
  `@Ignore("F-0000: ...")`; the orchestrator numbers it.
- **A finding you refuted**: take the `@Ignore` off and make the test assert what the cited rule says.
- **A ruling in the log that answers a parked question**: take the `@Ignore` off, make the test assert the
  ruled reading, bring the ledger into line.
- **Something you cannot put right** (it needs a rig method, a definition changed, Ketura's word): leave it, and
  report it.

When you are done with a card, `lint.py <id>` must report no errors and `cardtest.py run <id>` must pass, parked
tests apart. Do not run the stock mistakes to finish.

## What you may and may not touch

The test files and ledger files of your cards, and scratch files under `/tmp/<your folder name>/`. Nothing else:
not card definitions, not the engine, not the rig, not other cards' tests, not git, not the campaign's state
files. (The campaign scripts keep their own working files under `gemp-lotr-server/target/cardtest/`, which git
ignores.) The dormant `.old` siblings of a test file are not yours to review.

Other batches run beside yours. One `mutate.py` run at a time for each `--out` name: a second run with the same
name deletes the first one's working folder.

## Reporting back

Your final message goes to the orchestrator and is all it sees. For each card:

- the card id, and a verdict: `PASS` (you changed nothing), `FIXED` (you changed something; the card is now
  right as far as you can tell), or `PARK` (something holds the card that you could not put right; say what)
- each change you made: what was wrong, the evidence (the mistake that survived, the rule, the old assertion),
  and what you did, in two or three lines
- each of the author's findings and questions: stands, or refuted with the citation
- any finding, question or rig request that is new, in the detail `roles/author.md` asks for
- the last line of `lint.py` and of `cardtest.py run`
- anything in the standard that let a bad test through, or that you had to interpret

Do not pad a clean card with minor remarks to seem thorough. If it is right, say `PASS` and stop.
