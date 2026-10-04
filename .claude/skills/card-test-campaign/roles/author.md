# Brief: author

You are writing the unit tests for a few cards of the Lord of the Rings TCG as implemented in Gemp, as part of a
campaign to test every card the same way. The cards and a name for your working folder are given at the end of
this brief.

Before anything else, read these three files in full. They are short and everything below assumes them:

- `.claude/skills/test-generation/SKILL.md`: the Card Test Standard. What a card's tests must cover and how the
  file is laid out.
- `.claude/skills/gemp-testing/SKILL.md`: the test rig and the behaviours that catch people out.
  `.claude/skills/gemp-testing/references/vts-api.md` lists every rig method.
- `.claude/skills/card-test-campaign/references/rulings-log.md`: what Ketura, who knows this game's rulings better
  than any document, has already ruled.

## What done looks like

For each card: its test file meets the standard, `lint.py <id>` reports no errors, `cardtest.py run <id>` passes
(apart from tests parked on a finding or question), and you have reported back as described at the end. Or the
card is parked, and your report says exactly what is holding it.

## The two stages, in this order

The value of these tests is that they are worked out from what the card says, not from how it was implemented.
So the order matters and is not a formality.

**Stage 1, without the implementation.** Run

    python3 .claude/skills/card-test-campaign/scripts/packet.py <id> --blind --terms

and from that alone write the card's ledger: the clauses, their types, the checks each one's wording calls for.
Then, for each check, write one line: the situation, and what the text and rules say happens. Save this as
`/tmp/<your folder name>/<id>.plan.md` before going on. Do not open the card's `.hjson` file or its existing test
file during this stage. If the text leaves a real doubt about what should happen, write the doubt down as a
question with both readings; do not settle it by guessing, and do not settle it in stage 2 by seeing what the game
does.

**Stage 2, with everything.** Run `packet.py <id> --with-test`. If the card already has behavioural tests, go
through them one by one against your plan: each either claims one of your checks (keep it, adapted to the
standard), or duplicates another, or covers something your ledger missed (then your ledger was wrong: fix it), or
asserts something your plan contradicts. That last kind is a question, not a mistake to overwrite: existing tests
for PC cards were written with Ketura and often encode a ruling. Then write the file, and run

    python3 .claude/skills/card-test-campaign/scripts/lint.py <id>
    python3 .claude/skills/card-test-campaign/scripts/cardtest.py run <id> --out <your folder name>

until both are clean. Then run `mutate.py <id> --out <your folder name>`: it makes deliberate mistakes in a copy
of the definition and lists the ones your tests did not notice. A survivor is a gap to close or a mistake that
makes no difference to play; say which for each in your report.

Choose helper cards by reading their definitions (`packet.py <helper id>`), never by name. Reading a helper
card's definition is fine at any stage; it is only the card under test whose implementation stays shut in stage 1.

If an existing test asserts one reading of something you wrote down as a question, keep that test active, and
report the question with a note that the existing test takes that reading.

## When a test fails

Decide which of three things is wrong, in this order, and say which in your report:

1. **The test.** It drove the rig wrongly, or a helper card did something you did not account for. Fix it.
2. **Your expectation.** You misread the text or a rule. Check with `rules.py term ...` and `rules.py card ...`.
   If the rules settle it, fix the plan and the test. If they do not, it is a question.
3. **The game.** The text and rules are clear and the game does something else. Leave the test asserting what the
   text says, park it with `@Ignore("F-0000: <one line>")` (the orchestrator assigns the real number), and report
   it as a finding with: what the text says, what you expected, what happened, and your best guess at the kind
   (typo in the definition, oversight in the implementation, wrong displayed text, engine bug, rig bug).

Be slow to conclude the game is wrong. Most of the time it is, when you think so; but a correct implementation of
an obscure ruling looks exactly like a bug, and a wrongly reported bug costs more than a question.

## What you may and may not touch

You edit the test files of your own cards, and files under `/tmp/<your folder name>/`. To find out what the game
does in some situation without adding a test, put a throwaway JUnit class there and run it with
`cardtest.py probe <file.java>`; use this to understand a failure, never to decide what a test should expect. Nothing else: not card
definitions, not the engine, not the rig, not other cards' tests, not git, not the campaign's state files.

If a check cannot be written with the rig's existing methods, do not write a helper method and do not reach into
the engine. Leave the check untested and report a rig request: what the test needed to do, the method signature
you would propose, and which rig interface it belongs in.

## Reporting back

Your final message is read by the orchestrator, not by a person, and is all the orchestrator sees of your work.
For each card give, in this order:

- the card id and name, and `READY` or `PARKED`
- the ledger as it ended up
- the number of tests before and after, and the output of the last `lint.py` and `cardtest.py run`
- for an existing test file: what happened to each existing test (kept, merged, dropped as duplicate, contradicted)
- findings, questions and rig requests, each with the detail asked for above
- anything in the standard or the rig documentation that was wrong, unclear or missing: this is early in the
  campaign and the documents are still being corrected by what authors run into
