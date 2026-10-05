# Brief: fixer

Another agent wrote the unit tests for a few cards of the Lord of the Rings TCG as implemented in Gemp, and a
reviewer has listed what must change. You make those changes. The cards, the reviewer's list and a name for your
working folder are given at the end of this brief.

Read `roles/author.md` (beside this file) first, and the three files it tells an author to read. Everything it
says about what a card's tests must be, what you may touch, what to do when a test fails and how findings and
questions are parked applies to you unchanged. What differs:

- **You are not starting again.** The test file and the ledger file exist. Leave alone what the reviewer did not
  fault. Do not restyle, rename or reorder tests the list does not mention.
- **Take each numbered item in turn.** Either make the change, or, if you think the reviewer is wrong, do not make
  it and say why with the rule, ruling or card text that shows it. A reviewer is as fallible as an author; an item
  that asks for an expectation the card's text does not support is to be refused, not obeyed.
- **A fix that changes what a test claims changes the ledger too.** Keep the ledger file's claim lines true.
- **A ruling that arrived after the review is an item on the list.** Before starting, read the end of
  `rulings-log.md`. If Ketura has since ruled on something a test of yours is parked on, or a question your card
  raised, deal with it as if the reviewer had listed it: take the `@Ignore` off, make the test assert the ruled
  reading, and bring the ledger into line.
- **A reviewer's item that says the game is wrong** (a new finding, or a finding the author missed) is handled the
  way an author handles one: the test asserts what the text says and is parked with `@Ignore("F-0000: ...")`.
- **An item that refutes a finding** means the parked test was wrong about the game. Take the `@Ignore` off and
  make the test assert what the cited rule says.

When every item is dealt with, `lint.py <id>` must report no errors and `cardtest.py run <id> --out <your folder
name>` must pass, apart from parked tests. Run `mutate.py <id> --out <your folder name>` again if you changed
what any test asserts.

## Reporting back

For each card: its id and name; `READY` or `PARKED`; then the reviewer's items by number, each `done` (and what
you did, in a line) or `refused` (and why); the last output of `lint.py` and `cardtest.py run`; and any finding,
question or rig request that is new since the author's report, in the detail `roles/author.md` asks for.

On a busy machine, run `mutate.py` for one card at a time and in the background if it has many mistakes to make;
see the reviewer's brief for why.

While a background run is going, do other work that does not need it (the ledger, reading the rules), or wait
with one long `sleep` (eight or nine minutes) before looking. Every look re-reads everything you have read so
far; checking every half minute is the most expensive thing an agent here can do.
