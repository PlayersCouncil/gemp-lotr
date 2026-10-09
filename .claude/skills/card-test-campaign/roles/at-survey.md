# Brief: surveying an old `at` test file

The folder `gemp-lotr/gemp-lotr-server/src/test/java/com/gempukku/lotro/at/` holds the game's oldest tests: 46
files (18 of them in its subfolder `effects/`), some 336 test methods. Most were written before the test rig (`VirtualTableScenario`) existed, on a base class
of their own (`AbstractAtTest`), and drive the engine by hand; a few (`HinderTests`, `RestoreTests`,
`UniquenessTests`, `RelentlessTests`, `ConcealedExposedAtTest`, `PregameDecisionTests`) already use the rig. All
of them are to be replaced by tests written to the Card Test Standard, and the folder emptied.

Ketura's direction (2026-10-08): "we're going to tackle the `at` tests and get those all converted and in place,
either in the appropriate mechanical folders or within the cards they are most associated with. These tests are
going to be a bit gnarly and opaque, so it may make more sense to just note what bug or behavior or interaction
the test is handling, and then take that as a design directive when instantiating a wholesale new test."

So an `at` test is NOT ported line by line. You do the first half of that: for each test method of the files
you are given, you write down what it guards and where its replacement belongs. Somebody else writes the new
tests from what you write, without reading the old method again unless they must. You change nothing in the
repository.

Your files are named at the end of this brief. Read first, in one step: the `at` files you are given; in
`.claude/skills/test-generation/SKILL.md` the sections "The checks" (the table and no more), "Rules every test
follows" and "Tests that are not about a card" (where a test of a rule goes, and how such a class is named); and
`.claude/skills/card-test-campaign/references/rulings-index.md`. Look other things up as you need them:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py <number or words>
    python3 .claude/skills/card-test-campaign/scripts/packet.py <id> --blind      # a card's printed text and rulings
    python3 .claude/skills/card-test-campaign/scripts/rules.py term <word>        # the rules documents

Every step you take re-reads everything you have read so far: read a long file once, in slices if it is long, and
do several things in one step where you can.

Do NOT run tests, probes or any Java, and do not build: the machine is small, an engine pass is building on it,
and it has restarted twice under the load. You work from reading: the old method, the cards' texts, the rules.
Where only a run could tell you what an old board is waiting on, say that in the directive and leave it to the
author who writes the replacement.

## For each test method

Work out, from the method and the cards it names (`packet.py <id> --blind` gives a card's text; an id like
`"1_231"` is set 1, card 231), and write down:

1. **What it guards**, in one to three sentences of the game's own language: the rule, the card's behaviour, the
   interaction of two cards, or the bug it was written for. Say which of those it is. If a comment or the method's
   name gives an issue number or a history, keep it. Do not describe the test's lines ("calls playerDecided with
   0"); describe what would be broken in the game if the test failed.
2. **The cards** it puts in play, by id and name, and which of them the test is ABOUT (often one; sometimes none,
   when any card of a kind would do).
3. **Its kind**, one word:
   - `rule`: a rule of the game, true whatever cards show it (the Rule of 4, roaming, how wounds are assigned,
     uniqueness, a keyword). Its replacement is a test in `mechanics/`.
   - `card`: what one card does (or one card against another). Its replacement belongs in that card's own test
     file, `Card_<set>_<number>_Tests` (find the file with `packet.py <id> --blind`, which prints its path whether
     or not the file has tests yet).
   - `language`: a field or effect of the card definition language shown on an arbitrary card (a requirement, a
     modifier, a trigger shape), with no one card it is about. Its replacement is a `mechanics/` test of the rule
     behind it if there is one; if there is none, say so: it may be a test of the builder and not of the game.
   - `server`: nothing a player would see (the order of a player list, a serialisation). Not a test of the game:
     it moves beside the class it tests.
4. **Whether something already covers it.** Look in `mechanics/` (the file names say what each is about) and in
   the test file of the card it is about, where that file has tests. If a test there already asserts the same
   thing, name it (file and method) and say `covered`; if it asserts part, say which part is left. Be exact: a
   test that merely uses the same card covers nothing. Use `grep -n` on method names and the card's id, then read
   the one or two methods that look right.
5. **The directive for the new test**: the home (the file's path; for `mechanics/`, an existing file's name if one
   fits, or a new name in the manner of the others, `<WhatTheRuleIs>Tests`), a name for the test in the manner of
   the Standard (what it shows, in words), and in two to five lines the board, what is done, and what is read:
   enough that a competent author could write it without opening the old method. Where the old test asserts
   something the rules or a ruling of Ketura's now say otherwise (the rulings index), say so and give the
   directive by the ruling. Where you cannot tell what the test is for, or it seems to assert something wrong,
   say exactly that: do not invent a purpose.

Several methods that guard one thing from different sides (the same rule with a companion, an ally, a minion) are
ONE directive with the cases listed, if a single test class would hold them; say which methods it stands for.

## Settled already (do not raise these again)

- A replacement that is about one card goes into that card's own file as a LONE test, whether or not the card's
  set has been worked: it is a kickstart for the card's own batch. Ketura wants above all "the tricky ones, the
  ones about specific interactions between cards"; say in the directive when a method is one of those.
- A Hobbit-set card (sets 30 to 33): its file goes under `cards/unofficial/hobbit/set<NN>/` (a new folder).
- A commented-out test of the archived set 40 with no rule behind it: home `-`, dropped.
- The bid before the game is read against the Ring-bearer's resistance IN FORCE, his own text and the Ring's
  included (Ketura on Galadriel 9_14: "the game text on the ring and Ring-bearer are active enough for resistance
  calculations during the bidding process, otherwise Galadriel would never get to bid much at all").
- An old method that only shows a card doing what it says, where the card's landed tests already assert it, is
  `covered` and simply goes: do not look for a hidden purpose (Ketura on hisFirstSeriousCheck: "just basic card
  effect testing").
- "Instead" converts a cost and does not prevent or cancel it: the cost is paid in the other currency and what it
  buys happens (`lookup.py instead`).
- New `mechanics/` class names are reconciled across all the surveys afterwards: propose one, and look at
  `/mnt/user-data/outputs/card-tests/orchestrator/at-survey/*.tsv` for names earlier groups proposed, and reuse
  one where it fits.

## What you write

Two files, in `/mnt/user-data/outputs/card-tests/orchestrator/at-survey/` (create the folder), for EACH `at` file
you are given:

- `<FileName>.md`: a short paragraph on the file as a whole (what it is about, whether it is on the rig already,
  how much of it is covered elsewhere), then one entry per directive, headed `### <home file>: <test name>`, with
  the five points above in that order, each a line or a short paragraph, and the old method names it replaces.
- `<FileName>.tsv`: one line per OLD test method, tab-separated, no header:
  `<at file>	<method>	<kind>	<covered|part|new>	<home file's path>	<new test name>	<ids of the cards it is about, comma-separated, or ->	<one sentence for Ketura>`

  The last column is what Ketura asked for by name (2026-10-09): "a report of what exactly all the at tests appear
  to be testing, with just a brief one-sentence summary of what interaction, mechanic, or cards are being
  checked. Many of those tests have very opaque names and use raw blueprint IDs that don't communicate what is
  happening or why very well." So: ONE sentence in plain words that says what is checked, naming every card by
  its NAME with the id after it in brackets ("Gimli, Dwarf of Erebor (1_13)"), never by id alone, and saying the
  rule or interaction in the game's words ("a minion that is not roaming pays no roaming penalty when it is played
  to the fellowship's own site"). It is read by her on a phone, apart from everything else you write: it must
  stand on its own. No tab and no line break inside it.

A test that is COMMENTED OUT (it does not run) still gets its line, since the count is by `@Test`: begin its
sentence "(Commented out: does not run.)", and where it only shows a word of the definition language on a card
that no longer exists, with no rule behind it, give it the home `-` and say so; do not invent a replacement.

Every `@Test` method of the file is on exactly one line of the `.tsv`: count them
(`grep -c '@Test' <file>`) and count your lines before you finish.

## What you return

A few lines: for each file, how many methods, and how many of each kind and of covered / part / new; and anything
the orchestrator must decide (a test whose purpose you could not tell, a test that asserts something a ruling
contradicts, a home that is unclear). No directives: they are in the files.
