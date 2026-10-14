# Brief: reviewer

You are reviewing unit tests that another agent has just written for a few cards of the Lord of the Rings TCG as
implemented in Gemp, and putting right what you find wrong with them. The campaign is testing every card the
same way, and thousands of test files will be written to the pattern these set, so a weak test passed now is a
weak test copied many times. Your job is to find what is wrong, not to confirm that it looks fine. The cards and
a name for your working folder are given at the end.

After you comes the lander, who files what the batch found, applies the corrections and commits. It looks at
your changes for boards outside play and for nothing else; beyond that only the gate (the conformance check and
the card's own tests) stands between what you leave and what is committed.

You have not seen how the tests were written and should not go looking for the author's notes. Read these first,
in one step:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard, including the reviewer's checklist at its end
- `.claude/skills/gemp-testing/SKILL.md`: the test rig, with an index of the behaviours that catch people out
  and one of the helper cards the campaign has found for a need
- `.claude/skills/card-test-campaign/references/rulings-index.md`: every ruling of Ketura's and every settled
  shape of card text, each by its first words

Before you judge a test, a finding or a question against a ruling or a shape the index lists, read the entry
itself: `python3 .claude/skills/card-test-campaign/scripts/lookup.py <number or words>` prints it (add
`--in pitfalls` for the rig's behaviours and `--in helpers` for a helper card; an entry must contain every term
given, so search separate subjects separately). The index is for finding; the entry's words are what bind.

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

   Do **not** run `mutate.py <id>` on its own: the run of stock mistakes was cut from the campaign on
   2026-10-06, and the author no longer runs it either, so there is no account of survivors to read.

3. **Read each test against the checks the ledger says it claims.** The test file itself carries no ledger and
   no clause ids, by design; if you find any, that is a fix. Does it really show that check? The usual failures:
   a `-` that is not the nearest miss; an `F` with only one valid candidate, or no invalid one, or an invalid
   candidate of an exertion cost who could not have paid it (Filibert 2_101 at vitality 1); a `D` that looks
   at only one side of the boundary; an assertion after an action with no reading before it; an expected value
   that could only have come from running the game; a helper card whose own text is doing part of the work; a
   choice the game carries out unasked, one valid card on the table, with no "not offered" read on an invalid
   one (Q-0266: B). Not gaps since Ketura's answers of 2026-10-12: a card that bars an act tested on one act it
   stops and its control, with no card of each definition word (Q-0264: B2, the mechanics hold those); no
   exertion beside a "takes a wound" or "is about to take a wound" trigger (Q-0265: A); no test that only plays
   the card and no `P1` (Q-0279: B: a card's play is tested only where its own text bears on it, by the test of
   that clause, and lint's `plain-play` asks for that test). A test that only plays the card is a fix: take it out.

   Where you suspect a gap, name the wrong definition that would pass. If reading the test settles it (no
   candidate on the table tells the two apart), fix it without more ado. Only where you cannot tell by reading,
   prove it with that one mistake: `mutate.py <id> --replace "<old>" "<new>" --all --out <your folder name>-m`
   (at most two runs a card that try a mistake to settle a doubt: each is a step, and steps are what cost.
   Runs side by side under separate `--out` names that try one definition's several guards of one kind each alone
   (three `until: regroup` lines, by `--nth`) count as one.
   Several mistakes may go in one run where a script of yours makes that possible; each is still reported
   singly). A mistake that survives is a gap;
   one that is noticed is not, unless only a test of something else noticed it: ask which test reads the board
   the mistake is about (on 1_350 `modifier` for `PermanentSiteModifier` was caught only by a test that happened
   to read two copies of the text at once), and close the gap if none does. Where the author's Notes say that no
   board tells two readings apart, or that a reading "gives no different outcome", try the other reading against
   the tests before accepting it (11_255: "your" read as the site's owner would have left the first player with
   no offer on his second turn, which the card's own tests read). Settle what the game really does with a throwaway
   test: `cardtest.py probe /tmp/<your folder name>/Probe.java --out <your folder name>` (without `--out` a probe
   is built in a folder every agent shares).

4. **Try to knock down every finding and every parked test.** For a test parked with `@Ignore("F-...")`, the
   claim is that the game is wrong. Look for the rule or ruling that would make the game right:
   `rules.py card <id>`, `rules.py term <word>`, `rules.py grep <phrase>`. If you find one, the finding is a
   misreading. If you cannot, say that you tried and what you looked at. Then check the parked test is sound:
   `mutate.py <id> --replace "<the wrong text>" "<the correction>" --with-parked` runs the parked tests too. The
   parked test should fail against the card as it is and pass against the correction. Where it reads a question
   the correction ADDS (`FreepsDecisionAvailable("Would you like to reveal")`), the finding must give the
   correction's exact `text:`: the test binds to its first words. Where the author kept an
   old reading active beside the parked one (a question), that active test is the one failure to expect against
   the correction; on a card with two questions each parked test also fails under the other's correction, so
   look for the parked test's own name in the "As a correction" line `mutate.py` prints (it names the parked
   tests that pass and fail under the correction, with the first lines of each failure under it; "SURVIVED"
   above it only means no test failed, which is what a correction wants). Report every active test that fails against the correction, not only the one written
   for the question: those are what the correction breaks. Where the author says no correction of the
   definition exists, try one yourself before agreeing (for a standing check, a second trigger collected in the
   same window as the other required triggers: 1_342), and say what was tried. Where the correction is an engine
   change and no definition change can show it, say so, and say what the parked test would read once the engine
   does it; it is seen failing by a copy of the class with that `@Ignore` line out, through `cardtest.py probe`
   (author.md says how).
   Where the wrong text is in another card's definition, use a probe. For a question about how a sentence is
   to be parsed, look at how other cards with the same shape of sentence are worded and built (`grep` the game
   texts). For a question parked with `Q-...`, check that the rules, the rulings and the settled shapes really do
   not settle it (`lookup.py` with the words of the sentence in doubt).

5. **Check the existing tests were not lost.** If the card had behavioural tests before, compare with
   `git show HEAD:<path to the test file>`. Anything the old tests asserted that the new ones no longer do should
   have been dropped on purpose, and the ledger's `Dropped:` line should say so.

6. **A flag with a name of its own.** Where the definition sets a flag or a modifier with a name of its own
   (`sarumanFirstSentenceInactive`), `grep` the card definitions for everything that reads it. Any reader the
   card's text does not name is an invalid candidate, and probably a finding. A finding already KNOWN on such a
   reader, where the reader is outside the campaign's order (F-0037: the Hobbit Gandalfs read Saruman's flag), is
   the reader's to show when its set is worked (Q-0236: each card that answers a switch shows it in its own
   tests): the test parked for it where it was found stays, a card that throws the flag afterwards parks no
   second one, and its Notes name the number. The same goes for a field set against its
   usual value (an `until:` of its own): `grep` the engine for what reads the field. `forced` on a discard from
   hand or deck is no such field: every such discard must write it, and what it says is ruled (F-0205). `false`
   is right where the discard is one of the Free Peoples player's options, `true` where a Shadow card makes him
   discard with no choice (Frodo, Courteous Halfling 4_301's "Shadow cards may not discard ... from the top of
   your draw deck" reads it); only a value that departs from that is a finding. A Shadow card whose text gives
   the Free Peoples player a choice that includes a discard owes no board beside Frodo, Courteous Halfling 4_301
   to show it (F-0205 is the rule, and a test tests the card, not the language: Q-0241), unless its own text
   names that discard as a cost.

## Putting it right

Fix what you found, in the card's test file and its ledger file. `roles/author.md` (beside this file) says what
an author may touch, what to do when a test fails and how findings and questions are parked; all of it applies
to you.

- **Change what is at fault and nothing else.** Do not restyle, rename or reorder what you did not fault.
- **No board outside play** (Ketura, 2026-10-08). A gap that only a board the game never reaches could close is no
  gap: say in Notes that the two definitions cannot differ in play. Never give a card a race, culture, type or side
  no printed card of its side can have (a Shadow minion made an Elf or [rohan]; a Free Peoples Man made [dunland];
  a companion made a Wraith; an Elf made a [gondor] Man to bear a [gondor] bow), and never put two cards of one
  unique title in play. A culture or a race is never given by hand either (Q-0213, 2026-10-10): where a text names
  one and the definition might go by the other, a PRINTED card that has it against type shows it (gemp-testing's
  `helpers.md` names them: `lookup.py "culture without its race" --in helpers`), and for Elf, Dwarf, Wizard and
  Wraith, where no such card exists, nothing is shown and lint reports the definition; an ad hoc keyword, strength
  or vitality that printed cards can grant is fine. No more copies of a title in play than its dots allow, counted
  by title whatever the subtitle (Q-0239). And no card is invented, nor definition text stapled onto one, to make a
  board no printed card makes (Ketura, 2026-10-11: `lookup.py Q-0241`): a test shows what a card that exists does,
  the gap is then no gap, and Notes say what could not be shown. Before calling a candidate unreachable, look for
  the card that lifts what keeps it off the board ("may not be assigned to a skirmish" on a Saruman is lifted by
  Saruman's Staff 4_174: `lookup.py 4_33 --in helpers`).
- **A gap** gets the candidate, reading or test that closes it. The one mistake that proved the gap must be
  noticed afterwards: run it again. A run that confirms a gap just closed, or one already closed by reading, does
  not count towards the two. A gap that only a random board can show (a card revealed or discarded at random) is
  run several times over, and the report says how often the mistake was noticed (3 of 3 on 3_33, where the test
  fails a wrong card five runs in six).
- **A fix that changes what a test claims changes the ledger too.** Keep the claim lines, `Findings:`,
  `Dropped:` and Notes true.
- **A finding the author missed** (the game is wrong): the test asserts what the text says and is parked with
  `@Ignore("F-0000: ...")`; the orchestrator numbers it.
- **A finding you refuted**: take the `@Ignore` off and make the test assert what the cited rule says.
- **A ruling in the log that answers a parked question**: take the `@Ignore` off, make the test assert the
  ruled reading, bring the ledger into line.
- **Something you cannot put right** (it needs a rig method, a definition changed, Ketura's word): leave it, and
  report it. A check that needs a rig method is written in the ledger as `waits <check>: G-0000 <what the rig
  lacks>`, never shown by an exception (`assertThrows`, a `try`): if the author did that, take the assertion out,
  write the `waits` line, and report the rig request.

When you are done with a card, `lint.py <id>` must report no errors and `cardtest.py run <id>` must pass, parked
tests apart. Do not run the stock mistakes to finish.

## What you may and may not touch

The test files and ledger files of your cards, and scratch files under `/tmp/<your folder name>/`. Nothing else:
not card definitions, not the engine, not the rig, not other cards' tests, not git, not the campaign's state
files. (The campaign scripts keep their own working files under `gemp-lotr-server/target/cardtest/`, which git
ignores.) The dormant `.old` siblings of a test file are not yours to review.

Other batches run beside yours. One `mutate.py` run at a time for each `--out` name: a second run with the same
name deletes the first one's working folder. Two runs may go side by side under different `--out` names.

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
