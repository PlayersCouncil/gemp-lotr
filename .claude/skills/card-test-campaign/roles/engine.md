# Brief: engine and rig work

The card test campaign has turned up a fault in the game's rules code, or a thing the test rig cannot do, that no
card definition can mend. You make that one change. Your item, a name for your working folder and the file that
describes the item are given at the end of this brief.

This is the code that runs the live game for real players, and the rig is shared with the scenario system. The
change wanted is the smallest one that makes the rule right for every card it reaches, with a test that shows it.
Ketura reads the commit: she knows this game and this code better than any document.

Read in one step, in full: `.claude/skills/gemp-architecture/SKILL.md`, `.claude/skills/gemp-testing/SKILL.md`,
`.claude/skills/card-test-campaign/references/rulings-index.md`, and, for anything under `virtualtable/` or the
test framework, `.claude/skills/gemp-testing/references/changing-the-rig.md`, whose rules are not negotiable.
Then look up what your item names, in as few steps as you can:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py F-0243
    grep -P "^F-0243\t" /mnt/user-data/outputs/card-tests/findings.tsv
    python3 .claude/skills/card-test-campaign/scripts/packet.py <id> --with-test

The entry of a question (`Q-`) is in `/mnt/user-data/outputs/card-tests/questions.md`; a rig request (`G-`) in
`rig-requests.md` beside it; a card's ledger at the path its packet prints. An entry's own words bind, not its
line in an index. Every step you take re-reads everything you have read so far: read several files in one step,
and keep the output of builds and test runs to its last lines (`| tail -30`).

## How the work goes

1. **See it fail.** Each fault has a test parked on it (`@Ignore("F-0243: ...")`); take the `@Ignore` off and run
   the card (`cardtest.py run <id> --out <your folder>`). A rig request has a check waiting in a ledger
   (`waits <check>: G-0029 ...`). If the test does not fail the way the finding says, stop and find out why first.
2. **Find the rule, not the card.** The change goes where the rule lives. Nothing is keyed on a blueprint id or
   a card's title. Before changing a method, find who else calls it and which other definitions use the same
   effect, modifier or flag (`grep` the definitions): each of them will now behave differently, and your report
   names them. Where a definition has to change too (a new field, a different modifier), change every card the
   item names, originals included.
3. **If it is bigger than it looked, stop.** A change that would rewrite how a phase or a rule flows, or spread
   over more than a handful of engine classes, is not made on your own word. Write the design down instead (what
   is wrong, the change proposed, what else it touches, what could break) as your report, put back what you
   changed, and return `proposal`.
4. **Show it.** A rule of the game gets a card-agnostic test in
   `gemp-lotr-server/src/test/java/com/gempukku/lotro/mechanics/` (read one that is there for the manner: plain
   Decipher cards whose behaviour is not in question). A rig method gets its Javadoc and, if it is more than a
   one-line reading, a test of itself in the test-side `virtualtable/` folder. A card test never relies on an
   exception. Then the cards: the parked tests go live asserting what the ruling says, a `waits` line gives way
   to the test that claims the check, and each ledger's claim lines, `Findings:` line and Notes are made true.
5. **Build and run everything.** `python3 .claude/skills/card-test-campaign/scripts/cardtest.py setup | tail -3`
   rebuilds the modules; then the item's cards and `lint.py <ids>`; then the whole suite:
   `cardtest.py suite all 2>&1 | grep -E "^OK|^Tests run|^[0-9]+\) " | tail -30`. It must end `OK`. Nothing else is
   running tests while you work, so you may rebuild freely. A test elsewhere that now fails is read against the
   rules and rulings: if it asserted the old, wrong behaviour, it is put right (that line and no more) and named
   in your report; if you cannot tell which is right, that is a reason to stop, not to choose.
6. **Say what changed for authors**, briefly and where they will look: a behaviour of the engine in
   `gemp-testing/references/pitfalls.md`; what a definition must now say in
   `card-test-campaign/references/shapes.md` or `hjson-reference/SKILL.md`; for a rig method run
   `python3 .claude/skills/card-test-campaign/scripts/gen_vts_api.py`. One to three lines each. An entry that the
   change has made untrue is corrected.
7. **Close the numbers.** For each finding: `tracker.py finding set F-0243 fixed-approved --outcome "<what was
   changed, in a sentence, and which cards' tests went live>"` (Ketura has already said these are faults). For
   each rig request: in `rig-requests.md`, move its entry from `## Open` to the top of `## Done`, with the heading
   ending in what was built and the date in place of `open`, and one line on which test now uses it.
8. **Commit, then mark the cards.** One commit for the item, naming every path you changed (engine, definitions,
   tests, documents):

       python3 .claude/skills/card-test-campaign/scripts/landing.py commit "Engine pass <n>: <what the game now does> (<numbers>)" <paths...> --body-file /tmp/<your folder>/body.txt

   The body says, in a few lines, what was wrong, what changed, and which other cards behave differently now.
   Then `python3 .claude/skills/card-test-campaign/scripts/land.py <the cards whose tests went live>` runs the
   gate on each and sets its status (their files are already committed, so it commits nothing). You run no other
   git command that writes, and you never push.
9. **Anything that wants Ketura's word gets a number.** She reads the tracker on her phone, not your report, and
   answers by number. So whatever you did beyond your item's words, and whatever you want her eye on, is filed
   as a question of its own, whole enough to answer without opening anything else: what changed and why, which
   cards behave differently, and exactly what striking it would undo.

       python3 .claude/skills/card-test-campaign/scripts/landing.py question add "Keep or strike: <what>" /tmp/<your folder>/k1.md

   A change already made stays unless she strikes it: say so in the entry. A finding you met and fixed outside
   your item is recorded as `fixed` (not `fixed-approved`), which puts it on her page too. Give the numbers in
   what you return.
10. **Write the report** for Ketura and the orchestrator, `/mnt/user-data/outputs/card-tests/orchestrator/engine-pass-<n>/<your item>.md`:
   what was wrong; the change, class by class; every other card or definition that now behaves differently; the
   tests that show it; anything you looked at and left alone, and why.

## If you cannot finish

Leave the tree as you found it: `git stash push -u -m "<your folder>"` keeps your work and clears the tree (say in
what you return that the stash is there). The agent after you starts from the last commit.

## What you may and may not touch

The engine and rig sources and their tests; the definitions and the test and ledger files of the cards your item
names; the documents of step 6; `rig-requests.md` as step 7 says; files under `/tmp/<your folder>/`. The other
registers only through `tracker.py`. Not other cards' tests beyond the one line step 5 allows, not the campaign's
scripts or briefs, nothing outside the repository and the campaign's state folder.

## What you return

A few lines: `done`, `proposal` or `blocked`; the commit; the cards whose tests went live; the numbers closed; any
other card's test you corrected; and one line of anything the orchestrator must act on. The rest is in your report.
