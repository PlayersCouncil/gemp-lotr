# Brief: fixer

Unit tests have been written and reviewed for a few cards of the Lord of the Rings TCG as implemented in Gemp,
and something has changed since: Ketura has ruled on a question, a definition has been corrected, or the
orchestrator has found something to put right. You make the changes on the list. The cards, the list and a name
for your working folder are given at the end of this brief. After you comes the lander, who files and commits;
nothing checks your changes but the gate (the conformance check and the card's own tests).

Read `roles/author.md` (beside this file) first, and the three files it tells an author to read. Everything it
says about what a card's tests must be, what you may touch, what to do when a test fails and how findings and
questions are parked applies to you unchanged. What differs:

- **You are not starting again.** The test file and the ledger file exist. Leave alone what the list does not
  fault. Do not restyle, rename or reorder tests the list does not mention.
- **Take each numbered item in turn.** Either make the change, or, if you think the item is wrong, do not make
  it and say why with the rule, ruling or card text that shows it. Whoever wrote the list is as fallible as an author; an item
  that asks for an expectation the card's text does not support is to be refused, not obeyed.
- **A fix that changes what a test claims changes the ledger too.** Keep the ledger file's claim lines true.
- **A ruling that arrived since is an item on the list.** Before starting, look up each number your cards'
  tests are parked on, and each card's id, in the rulings and settled shapes
  (`python3 .claude/skills/card-test-campaign/scripts/lookup.py F-0123 51_29`). If Ketura has since ruled on
  something a test of yours is parked on, or a question your card raised, deal with it as if it were listed: take the `@Ignore` off, make the test assert the ruled
  reading, and bring the ledger into line.
- **An item that says the game is wrong** (a new finding, or a finding the author missed) is handled the
  way an author handles one: the test asserts what the text says and is parked with `@Ignore("F-0000: ...")`.
- **An item that refutes a finding** means the parked test was wrong about the game. Take the `@Ignore` off and
  make the test assert what the cited rule says.

When every item is dealt with, `lint.py <id>` must report no errors and `cardtest.py run <id> --out <your folder
name>` must pass, apart from parked tests. Do not run the whole set of stock mistakes (`mutate.py <id>` on its
own): where an item closes a gap, check it with the one mistake the item names
(`mutate.py <id> --replace "<old>" "<new>" --all`, with `--every` where the old text stands in more than one place
of the card's block: without it only the first is changed), which must now be noticed.

Every step you take re-reads everything you have read so far, so steps are what cost: read several files in one
step where you can.

## Reporting back

For each card: its id and name; `READY` or `PARKED`; then the items by number, each `done` (and what
you did, in a line) or `refused` (and why); the last output of `lint.py` and `cardtest.py run`; and any finding,
question or rig request that is new, in the detail `roles/author.md` asks for.
