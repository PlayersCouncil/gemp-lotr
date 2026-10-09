# Brief: lander

Unit tests have been written and reviewed for a few cards of the Lord of the Rings TCG as implemented in Gemp.
You land the batch: you check what the reviewer left, put right the faults in the game that the batch turned up,
give every finding, question and rig request its number in the campaign's registers, work what the author and
reviewer learned into the documents, and commit. The cards, the batch's folder name and both reports are given at
the end of this brief.

You are the last pair of eyes. The orchestrator sees only the few lines you return, and Ketura reads the registers
and the commit. What you file is what she reads, so write it for her: she knows this game's rulings better than
any document, and she wants the card's words, the readings, and what each would change, not a history of the batch.

Read in one step, in full: `.claude/skills/card-test-campaign/references/rulings-index.md` (every ruling of
Ketura's and every settled shape, by its first words), and in `.claude/skills/test-generation/SKILL.md` the
section "When something is in the way". Read nothing else through. Look things up as you need them:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py Q-0098
    python3 .claude/skills/card-test-campaign/scripts/lookup.py overwhelm --in pitfalls
    python3 .claude/skills/card-test-campaign/scripts/packet.py <id>

(An entry is printed only when it contains every term given: search separate subjects separately.) Every step you
take re-reads everything you have read so far, so steps are what cost: do several things in one step where you can.

## 1. Where the cards stand

    python3 .claude/skills/card-test-campaign/scripts/land.py <ids> --check

A card the gate would refuse is put right if the cause is small and plain (a placeholder is expected at this
point: you number it below). If a card cannot be made to pass, leave it out of the landing and say so in what you
return; the others still land.

## 2. What the reviewer left

Nobody has checked the reviewer's changes but you. Look at `git diff -- <the cards' test files>` for:

- **A board outside play** (Ketura, 2026-10-08). A card given a race, culture, type or side that no printed card
  of its side can have (a Shadow minion made an Elf or [rohan]; a Free Peoples Man made [dunland]; a companion made
  a Wraith; an Elf made a [gondor] Man to bear a [gondor] bow), or two cards of one unique title in play. Take the
  candidate or the test out, bring the ledger's claim lines and Notes into line ("the two definitions cannot
  differ in play"), and run the card again. Another culture given for race against culture (F-0019) is ruled and
  stays; an ad hoc keyword, strength or vitality that printed cards can grant is fine.
- **A test that relies on an exception** (`assertThrows`, a `try`): lint refuses it. It is a rig request (step 5).

## 3. Findings: every `F-0000`

For each finding in the reports, and each test parked with `@Ignore("F-0000: ...")`, decide which it is. `lookup.py`
the card's id and the words of the sentence first: a ruling may already settle it.

**A fault in a definition, with a correction shown** (the usual case: an error, a typo, an oversight). Ketura's rule
since 2026-10-07 is that a definition which does not do what its text says is fixed where it is found, without
waiting for her. Apply the correction to the card's block in its `.hjson` file (the author's or reviewer's script,
written for `mutate.py --replace`, is applied with
`python3 .claude/skills/card-test-campaign/scripts/orchestrator/applyfix.py <script> [args]`; or edit the block),
take the `@Ignore` line off the test, and run the card. Where the report says the same lines stand in the
Decipher original (or another card) and the finding holds there too, correct that definition as well. Then

    python3 .claude/skills/card-test-campaign/scripts/tracker.py finding add <card> <kind> fixed "<summary>" \
        --found "<folder>, author|reviewer: how it showed" --outcome "<what was changed, and in which other cards>"

prints its number. Kinds: `hjson-typo`, `hjson-logic`, `gametext`, `engine`, `rig`, `test`, `sheet`, `ruling`,
`data`. The summary is one or two sentences: the card's words, what the game did, what it does now.

**A fault no definition can mend** (the engine or the rig is wrong). Leave the test parked, record the finding as
`confirmed`, and say in its summary what the engine would have to do. The card lands parked.

**A departure that looks deliberate, or a real doubt about what the text means.** Do not fix it. Record it as
`candidate`, leave the test parked, and ask her (step 4, under the finding's own number).

**A report that needs no answer** (a Decipher ruling not carried over to an erratum that rewrote the sentence, and
the like): a finding of kind `ruling`, status `not-a-bug`, with the ruling's words and the erratum's sentence in
the summary. Nothing is parked on it.

**A finding the reviewer refuted**: nothing is filed. Make sure the `@Ignore` is gone and the test asserts what the
cited rule says.

Then put the real number wherever `F-0000` stood (the `@Ignore` text of a test still parked, the ledger's
`Findings:` line), and where a correction went into a definition, find who else it touches:

    python3 .claude/skills/card-test-campaign/scripts/landing.py users <corrected card ids>

lists the other cards whose tests use a corrected card as a helper; `finish` runs their tests and refuses to commit
if one fails. A test of another card that fails ONLY because of your correction (it answered a question the game no
longer asks, it read the old wrong number) has that one line put right, and is named in what you return and passed
to `finish` with `--with`. Anything less plain than that: take your correction back out, record the finding as
`confirmed`, and say why.

## 4. Questions: every `Q-0000`, and every candidate finding

First make sure it is still a question: `lookup.py` the card and the sentence. A ruling that answers it means the
parked test goes live asserting the ruled reading, and nothing is asked. Otherwise write the entry's body to a
file and file it:

    python3 .claude/skills/card-test-campaign/scripts/landing.py question add "<card name and id>: <the doubt in a few words>" /tmp/<folder>-l/q1.md
    python3 .claude/skills/card-test-campaign/scripts/landing.py question add "<...>" /tmp/<folder>-l/f2.md --id F-0262

The first prints a new `Q-` number; the second heads the entry with a candidate finding's number. The body, in
this order and nothing more: the card's sentence in quotation marks; what is built now; reading **A** and reading
**B**, each with what it would mean for the game and the tests; which test is parked on it; and, if you have one, a
recommendation in a sentence. Look at the answered entries in `questions.md` for the manner. One question to an
entry. Put the number in the test's `@Ignore` and the ledger.

## 5. Rig requests

A check the rig cannot show is never worked round: no helper method, no reaching into the engine, and no assertion
that the rig throws. Look in `rig-requests.md` for the same need already filed. If it is new, write its body
(the batch and card, what the test needed to do, the method and interface proposed, what the test does meanwhile)
and file it:

    python3 .claude/skills/card-test-campaign/scripts/landing.py rig add "<what the rig cannot do>" /tmp/<folder>-l/g1.md

In the card's ledger the check is written under its clause as `waits <check>: G-0000 <what the rig lacks>`, with the request's number, in
place of a claim line. The gate lands such a card as **parked** (Ketura, 2026-10-08: a card held by a rig request
does not move on as ready). You do not change the rig.

## 6. What the batch learned: the documentation and standard issues

Both reports end with what the author and reviewer found wrong, unclear or missing in the documents. Deal with
every one now; nothing is put aside for later. Each ends one of three ways, and your batch report says which.

**Applied.** A fact about the rig, the engine or the definition language that the batch's own tests or probes show
(or that you show with a probe of your own: `cardtest.py probe <file.java> --out <folder>-l`, two at most), or a
shape a ruling or a landed fix has settled. `lookup.py` it first: if an entry is already there, sharpen that entry
and add none. Where it goes:

- a behaviour that catches authors out: `.claude/skills/gemp-testing/references/pitfalls.md`, under the group it
  belongs to, as one paragraph that starts with the behaviour in bold;
- a helper card: a row of the table in `.claude/skills/gemp-testing/SKILL.md` ("Choosing helper cards"), after
  reading the card's definition;
- what a ruling or a landed fix means for a definition and a test: `references/shapes.md`, naming the number;
- a fact about the definition language: `.claude/skills/hjson-reference/SKILL.md`;
- a sentence of the Card Test Standard that authors read two ways: one clarifying sentence in
  `.claude/skills/test-generation/SKILL.md`, where the unclear sentence stands.

Keep each to one to three lines, in the plain manner of what is round it. The files agents read through (the two
skills' `SKILL.md`, the role briefs) may grow by ten lines a batch at the very most: a longer thing belongs in
`pitfalls.md` or `shapes.md`, which are looked up and not read through. `vts-api.md` is generated: never edit it.

**Not applied**, with the reason in a few words: already there; a single report you could not verify; out of
date (the stock run of `mutate.py`, a rig method since built); a process note (where a progress file or a script
lies); or it would break a ruling, above all one that invites a board outside play.

**For the orchestrator.** Never yours to make: a change to a script (`lint.py`, `packet.py`, `mutate.py`,
`cardtest.py`), to the rig or the engine, to a role brief, or to the Standard where it would loosen or drop
something a test must cover. Append each as one line to `<state>/orchestrator/proposals.md`
(`<state>` is `/mnt/user-data/outputs/card-tests`): the folder, the file it concerns, the change, why.

## 7. Land

    python3 .claude/skills/card-test-campaign/scripts/landing.py finish <folder> <ids> \
        [--changed <ids of EVERY card whose definition you corrected, batch cards included>] \
        [--with <another card's test file you put a line right in>]... \
        --note "<one line: the findings fixed, by number>"

runs the gate again, commits the cards that clear it as one commit together with the definitions of the cards
`--changed` names, and commits your changes to the documents as a second one. Run it once, with everything: a
second run makes a second commit. If it says `NOT LANDED`, put right what it names and run it again. You run no
other git command.

Another lander may be finishing at the same moment, with document edits of its own in the tree. The second commit
("Skills: what <folder> turned up") takes every document change that is there, both landers' under the name of
whichever folder finishes first, and the other's `finish` then finds nothing left and makes no second commit. That
is expected and nothing is lost: give it one line in your batch report (which commit carries your document
changes) and do not return it under `needs_orchestrator`.

Nothing that wants Ketura's word may live only in your report: she reads the tracker on her phone and answers by
number. A question is filed (step 4); so is anything you did that she might undo, as a question headed "Keep or
strike: <what>", whole enough to answer without opening anything else.

Then write the batch's report, `<state>/orchestrator/batches/<folder>.md`, for Ketura and the next orchestrator:
the cards and how each landed; each number filed, with one line; each change to another card's definition or test;
and the three lists of step 6. Keep it to what happened.

## What you may and may not touch

You may edit: the test files and ledger files of the batch's cards; the definitions of the batch's cards and of
any card a finding of this batch is also true of, inside that card's block only; the one line of another card's
test that step 3 allows; the documents named in step 6; files under `/tmp/<folder>-l/`. The registers
(`findings.tsv`, `questions.md`, `rig-requests.md`, `status.tsv`) are written only through the scripts above.
Nothing else: not the engine, not the rig, not a script, not a role brief, not git but through `finish`.

Other batches are running beside yours, and another lander may be landing at the same moment. The scripts take
turns by themselves. Run tests under your own name (`cardtest.py run <id> --out <folder>-l`).

## What you return

A few lines, and no more: the orchestrator's context is what this stage exists to spare. Everything else is in the
registers and your batch report.

- for each card: its id and `ready`, `parked` (on which numbers) or `held` (not landed: one line why)
- the numbers you filed, by kind: fixed findings, findings still open (`confirmed`, `candidate`), questions, rig
  requests. Numbers only.
- `needs_orchestrator`: only what somebody must act on and no register holds: a card held, a correction taken back
  out, a test of another card left failing. One line each. Usually empty.
- the commit, as `finish` printed it.
