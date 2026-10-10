---
name: card-test-campaign
description: >
  How the campaign to give every Gemp-LotR card comprehensive, standardised unit tests is run: the order of work,
  the roles sub-agents play, the tools, where state is kept, how commits reach Ketura's repository, how findings
  and questions are recorded, and how to pick the campaign up in a fresh session. Use this skill when continuing
  the card test campaign, working a chunk of cards, acting as a campaign author or reviewer, updating the progress
  tracker or the findings tally, or when asked where the campaign stands.
---

# The card test campaign

The aim is a test file for every card that meets the Card Test Standard (the test-generation skill), written the
same way for all of them, with every error found along the way recorded by kind. PC cards were mostly well tested
already; thousands of Decipher cards had nothing but a stat check.

Read the gemp-testing and test-generation skills before doing any work on a card. This skill is about how the
work is organised.

## Order of work

1. **Existing behavioural tests are brought up to the standard**: V-sets, then PC errata, then Race to Mount
   Doom, then the Decipher cards that already had more than a stat check.
2. **PC V-set cards** that only have a stat check.
3. **PC errata** that only have a stat check or no test.
4. **Decipher cards**, one set at a time and one culture file at a time within a set.

As it is being worked (Ketura, 2026-10-08): the V-sets (done), the PC errata, then Race to Mount Doom, whose
tests are recent and want less attention (layout and the standard's form more than new coverage), then the tests
under `at/`, then the Decipher sets, the Hobbit sets last.

The tests under `at/` (written before the rig; 46 files, 336 methods) are not ported line by line. For each, what
it guards is written down (the bug, the behaviour, the interaction), and that is the brief for a new test written
to the standard, in `mechanics/` if it is about a rule or, as a lone test, in the file of the card it is most
about, where it is a start for that card's own batch. A survey wrote the directives and a report for Ketura;
batches of them are converted and landed (`roles/orchestrator.md`, "The old `at` tests"). The dormant test files
for earlier iterations of PC cards are kept compiling in step.

A chunk is one culture file of one set (`file:set01-Elven`), or one V-set, small enough for Ketura to review in a
sitting. Nothing in a chunk is finished until she has approved it.

## Who does what

**The orchestrator** (the main session) chooses the chunk, launches the batches, carries each landed batch to
Ketura's repository, publishes the tracker, changes the engine, the rig and the tools between waves, and talks to
Ketura. It does not read the agents' reports: its context is re-read on every step for the whole session, so
whatever a batch puts into it is paid for until the session ends. A batch comes back to it as a few lines.
Brief: `roles/orchestrator.md`, the craft of it (what the work costs and why it is paced as it is, a landing in
four steps, the two working trees, writing an engine pass's queue, filing Ketura's answers, what to do after a
restart or a compacted context, and what has bitten). Read it whole before the first launch of a session.

**An author** takes a few cards and writes each one's test file, in two stages that must stay in order: first
working out what the card should do from its text and the rules alone, then writing and running the tests. Brief:
`roles/author.md`.

**A reviewer** takes cards an author has finished, with no knowledge of how they were written, tries to find
what is wrong with them (a ledger that misreads the card, a test that would pass whatever the card did, a finding
that is really a misread ruling), and puts right what it finds. Brief: `roles/review-and-fix.md`.

**A fixer** is only for a list from the orchestrator after the review: a ruling to apply, a definition that
changed. It works through the list, changing what it faults and nothing else. Brief: `roles/fixer.md`.

**A lander** is the last stage of every batch (Ketura, 2026-10-08). It looks at the reviewer's changes for
boards outside play, applies the corrections the batch proved to the definitions, gives every finding, question
and rig request its number in the registers, works what the author and reviewer learned into the documents, and
commits. It returns the cards' statuses and the numbers it filed, and writes a report of the batch into the
campaign's state. Brief: `roles/lander.md`.

An author, reviewer or fixer works only on its own cards' test and ledger files, and reports; none of them edits
anything else or decides that the game is wrong. The lander decides what Ketura's standing rules already decide
(an error in a definition is fixed where it is found) and asks her the rest. Only the orchestrator changes the
engine, the rig, the scripts and the briefs. Nothing checks a lander's work but the gate (`land.py`: the
conformance check and the card's tests, and the tests of every card that uses a corrected card as a helper) and
Ketura's reading of the registers and the commit.

### Running the pipelines

`workflows/card-batch.js` is the whole passage of a few cards as a Workflow script: an author, a reviewer who
fixes what it finds, and a lander. Run it with `args` of `{repo, cards, folder, notes}`: the repository's absolute
path, the card ids, a short unique folder name (`err-b59_16`), and anything the agents should know that the
documents do not say (for a phase of the campaign, a line pointing at its notes file, `references/notes-errata.txt`).
With `fixes` (a numbered list per card) a fixer works that list in place of the author and reviewer, and the
lander follows it too. It returns the lander's few lines and nothing else; every stage's whole report is in the
run's `journal.jsonl` (`scripts/orchestrator/wf.py show <folder>`), for the day something has to be looked into.

**Two pipelines at once** (Ketura, 2026-10-07, after the usage limit cut runs off: the meter is not to be spent
well before a window ends). No launch in the last 20 minutes of a five-hour usage window; after a usage-limit
failure, do not relaunch: tell her. A run that was cut off is picked up with the Workflow tool's
`resumeFromRunId` and the same script and args: the stages that finished come back from the journal at once and
the rest run. Never launch from a turn that carries a message of hers (the harness passes her message to every
agent as its instruction, over the script's): launch from a notification's turn, or schedule one a minute ahead.

**Three cards to a pipeline.** This shape was cut down on 2026-10-05 from four stages
(author, review, fix, re-check) run six at a time, after measuring 34 cards (`cost-breakdown.md` in the
campaign's state; `scripts/orchestrator/cost.py` produces it):

- An agent carries 54K tokens before it has read a word and about 100K once it has read its brief's documents,
  and re-reads all of it on every step. Steps are what cost: about 57 a card over four agents, 9M cache-read
  tokens a card.
- 2026-10-06: the stock mistakes are no longer run at all (Ketura: "cut the remaining mutation step"). Over the
  last 42 cards before the cut, 17% of authors' and of reviewers' steps ran `mutate.py`; the author's stock run
  led to a closed test gap on 9 cards, reviewers' single mistakes proved 16 more, and none of the 35 findings
  came from it. What is left: `--with-parked` to show a parked test against its correction, and at most two
  single mistakes a card for a reviewer who cannot settle a suspected gap by reading. History:
- The stock mistakes (`mutate.py <id>` on its own) took 54% of all agent time and were run three times a card.
  They found no fault in the game; they led authors to close 14 test gaps in 34 cards, and the two re-runs found
  about 3 more. So they are run once, by the author, and the kinds of mistake that only ever made no difference
  have been taken out of the set. Reviewers and fixers make single mistakes with `--replace`, which is how most
  of the reviewers' gaps were proved anyway.
- The separate fixer and re-check cost 23% of the tokens. The re-check found 3 leftover gaps in 12 runs.
- Two cores carry about 7 cards an hour whether three pipelines run or six; six cost about 28% more tokens a
  card, the agents waiting on each other's Java processes.

**To look at again after 20 cards through the cut-down pipeline** (Ketura, 2026-10-05), with `cost.py` and the
reports: whether to drop the stock mistakes altogether (how many gaps did they lead authors to close in those 20,
and were they gaps a real card can show?), and whether to review only cards whose author raised a finding, a
question or a rig request (in the first 34, all three faults reviewers found in the game were on such cards, and
about 9 test gaps were on cards that would have been skipped).

Do not run `cardtest.py suite` or rebuild a module while pipelines are running IN THE SAME WORKING TREE: both
rewrite classes the agents' tests are loading. To run card batches while an engine pass has the main tree, give
them a second working tree with a build of its own (`git worktree add -b cards-wip /home/claude/work/cards
feature/card-test-overhaul`, copy `generator_sheets/` in, `cardtest.py setup` there, and pass that path as `repo`).
The registers are shared and locked, so numbering holds. When such a batch has landed,
`scripts/orchestrator/integrate.sh` rebases its commits onto the campaign branch and moves the branch up to them;
before the next batch, bring the second tree up to the branch and build it again. Rig changes, tool changes and full-suite runs go in the gaps between waves. `usage.tsv` has
what each pipeline cost; `scripts/orchestrator/wf.py usage <folder>` works it out.

**Nothing reports back by itself reliably.** A finished workflow sends a notification, but the workspace can
restart or go quiet and the notification is then lost; the results are still in each run's `journal.jsonl`.
Keep a scheduled message a little way ahead at all times (the session's `send_later`) that says to check the
pipelines, land what has finished, launch the next cards and schedule the next message. That message is what
wakes a session that has gone quiet. `scripts/orchestrator/wf.py` (`track`, `status`, `show`, `landed`) reads the
journals, which it finds by itself in a new session.

**When a pipeline returns**, the orchestrator takes four steps and reads nothing (`scripts/orchestrator/close.py`
has them in its header, `roles/orchestrator.md` the reasons):

1. `close.py <folder>`: brings a batch made in the second tree onto the branch, then `postland.py` (the batch's
   usage row, the tracker's data, a bundle of the new commits and one zip of the state files that changed), and
   prints what to carry, what to run on her machine and what to launch next.
2. In one step: carries the files it names to `Claude outputs/card-tests/transport/` on her machine, publishes the
   tracker (the page and `tracker-data.js` from the working folder's `publish/`, to the same artifact), and
   launches the next batch from the queue.
3. On her machine: `python3 ".../transport/apply-sync.py" <head>`, which unpacks the state and fetches the bundle
   into her branch, and answers with the head.
4. `close.py --launched <new folder> <run> <ids> --synced <head>`: tracks the new run, records that the sync
   arrived, and rewrites the "Running now" block of `HANDOFF.md`.

Then it tells Ketura what landed in a line: the cards, and the numbers filed. She reads the registers; nothing in
them is repeated to her.

What the lander could not settle comes back under `needs_orchestrator`, and is the only thing to look into. What
authors and reviewers report under "documentation issues" and "standard issues" used to pile up in a file for the
orchestrator to go through; the lander now deals with each at its own landing (`roles/lander.md`, step 6), and
passes on only what is not its to change, as lines in `orchestrator/proposals.md` in the state. Go through that
file between waves, with the tools and the briefs open.

### One card, start to finish

1. Author, blind: `packet.py <id> --blind --terms`. Write the ledger file and, for each check, the situation and
   the outcome the text calls for. Write down every doubt as a question.
2. Author, sighted: `packet.py <id> --only-withheld`. For an existing test file, account for every existing test: it
   claims a check, or it is redundant, or it knows something the ledger missed (then the ledger is what changes),
   or it contradicts the ledger (then it is a question; existing PC tests often encode a ruling).
3. Author: write the file; `lint.py <id>`; `cardtest.py run <id> --out <name>`. (The run of stock mistakes,
   `mutate.py <id>` on its own, was cut on 2026-10-06: see below.) A test that fails is looked at three ways before anything else: is the test wrong,
   is the expectation wrong, or is the game wrong.
4. Reviewer: `lint.py`, `cardtest.py run`, then the checklist at the end of the test-generation skill, with
   single mistakes (`mutate.py <id> --replace`) to prove a suspected gap. The reviewer fixes what it finds.
5. Lander: numbers and files what the batch found, applies the corrections, and makes one commit for the batch
   (`landing.py finish`, which runs `land.py <ids> --batch <folder>`: test files and definition fixes together) and
   one for the documents. The gate marks each card `ready` or `parked`. The orchestrator sends it across.
6. At the end of the chunk: `cardtest.py suite all`, rebuild the tracker, and give Ketura the chunk to review.
   While pipelines are in flight, do not run the suite (or `mvn`) in the main clone: it compiles every test file,
   the half-written ones included, and rewrites the classes the agents are running against. Run
   `scripts/orchestrator/suite-run.sh` instead, which tests the committed head in a second clone. For a small
   engine change that cannot wait for a gap, compile the changed classes with `javac` to a scratch folder
   (classpath from `cardtest.classpath()`) and copy them into `gemp-lotr-logic/target/classes`; do the proper
   `mvn install` at the next gap. `workflows/one-agent.js` runs one agent on a brief file in the background, for
   work that is not a card batch (data clean-up, a report).
7. Ketura approves, or asks for changes. Approved cards leave the tracker's lists.

## Tools

All under `scripts/`, run with `python3` from anywhere in the repository. Each prints its own help.

| Script | For |
|---|---|
| `packet.py <id>` | Everything about one card in one place. `--blind` leaves out the implementation; `--terms` adds the rules text its wording touches; `--with-test` adds the current test file and ledger and, for an erratum, the original's definition. `--only-withheld` is the second stage's packet: only what the blind one held back (the definition in full, sheet against definition, the original's definition, the test file). It also prints the picture the client shows, a sheet row of a later revision than the definition's (not one listed as never released in `references/unreleased-sheet-rows.tsv`), the game text a definition had before a commit of the campaign's changed it, a reminder written in `<i>` and not `<helper>`, and for an erratum of a site its direction beside the original's. `packet.py --find <regex> --skip <ids>` lists cards by what is printed on them, race or item class after the type (for choosing helpers; the batch's cards and their originals left out), and `--defs <ids>` prints definitions without cardInfo, lore and alternate images. |
| `rules.py` | Looking things up in Comprehensive Rules 4.2: `term`, `card`, `terms-for`, `grep`, `interactions`. |
| `reconcile.py` | Reviewed spreadsheet against definitions, field by field. |
| `cardinfo.py <ids>` | A definition's cardInfo block against the rest of the definition and the other cards of its kind (Q-0145: no test reads it, and it is kept true); `--all`, `--survey`. `packet.py` and `lint.py` print what it finds for a card. |
| `cardtest.py` | `setup` once, then `run <ids> --out <name>` in seconds; `suite` for everything. |
| `lint.py` | The conformance check. `lint.py --form <file.java> [--only <Test,Test>]` applies only the rules about how a test is written, for a class under `mechanics/` or a card's file with no ledger (`--only`: the tests named, a lone test added to an old file). |
| `rules.py council [LR 6]` | The Rules Councils' rulings since CR 4.2 (eight Legacy, two Modern), which `rules.py card` and `grep` also search. |
| `mutate.py <id>` | Makes deliberate mistakes in a copy of the definition and reports the ones no test notices. `--replace <old> <new>` makes one of yours (several pairs, or `--every`, for a mistake in several places); `--with-parked` runs the parked tests too. A run that does not return is stopped (`--timeout`, 180 seconds). |
| `land.py <ids>` | The gate: re-runs the conformance check and the tests on each card, and commits the cards that clear them (`--batch <folder>`: as one commit). `--check` to look without committing. |
| `landing.py` | The lander's tools: `question add` and `rig add` number an entry into its register; `users <id>` lists the cards whose tests use a card as a helper (by its id, by the rig's constant for it, or as what a table has when none is named); `finish` runs the gate and makes the batch's commits. |
| `tracker.py` | The campaign's state: statuses, findings, the tracker. |
| `gen_vts_api.py` | Rewrites the rig's API reference from source; `--check` says whether it has drifted. |

Selectors work wherever an id does: `set:1`, `file:set01-Elven`, `seg:vset`.

The generator sheets are in `generator_sheets/` at the root of the repository (or wherever `GEMP_CARD_SHEETS`
points). They are not in git.

- `gen_en_standard.tsv`: every Decipher card's current official text, Decipher's own errata included. Reviewed.
- `V1.tsv`, `V2.tsv`, `V3.tsv`: the V-sets as first released.
- `master_errata.tsv`: every PC erratum by revision, assembled from some twenty older sheets. About 95% accurate:
  some rows are errata that were proposed and never released, or held back a release. A row Ketura has said was
  never released goes into `references/unreleased-sheet-rows.tsv` (id, revision, her words), and the tools then
  leave it out: `reconcile.py` and the packet read the sheet without it.

The tools match a definition to its sheet row by the definition's revision number. `reconcile.py --all --report`
writes every disagreement as a document (`sheet-discrepancies.md` in the campaign's state); for PC cards those
are flags for Ketura, not findings.

## Where state is kept

Three places, each for one kind of thing.

- **Git, on `feature/card-test-overhaul`:** the tests, the skills, the scripts, the rulings log. One commit per
  batch (`Card tests: err-b58_103: 58_103, 59_4, 59_14`, with the definitions corrected at that landing), the
  lander's changes to the documents in a commit beside it, rig changes and tooling in commits of their own.
- **`Claude outputs/card-tests/` in Ketura's folder (not in git):** `baseline.tsv` (what every card's tests looked
  like before the campaign), `status.tsv`, `findings.tsv`, `questions.md`, `rig-requests.md`, `usage.tsv` (tokens
  spent per batch of cards), `sheet-discrepancies.md`, `focus.txt`, `ledgers/<set>/<id>.txt` (each card's ledger:
  Ketura wants these kept out of the test files), the generated `tracker.md`, `tracker.html` and
  `tracker-data.js`, and `orchestrator/`: the queue, each batch's report (`batches/<folder>.md`), what landers
  passed up (`proposals.md`), and the suggestions from before there were landers (`doc-suggestions-pending.txt`).
  Every script that writes a register takes the campaign's lock first (`.lock`), so two landers can work at once. In the cloud workspace the working copy is `/mnt/user-data/outputs/card-tests/`, which the
  scripts find by themselves; `GEMP_CAMPAIGN_STATE` points them anywhere else.
- **The tracker:** a page and its data in two files. `tracker.py html <state>/tracker.html` writes the page and
  `tracker-data.js` beside it (statuses, findings, and the open questions and rig requests word for word), so a
  landing changes only the data. In her folder the page opens from disk; the published artifact is the same page
  (`tracker-page.html`) with the same data file, republished at every landing.

### Statuses

`todo` → `in-progress` → `ready` (committed, passes the conformance check and its tests, waiting for Ketura) →
`approved`. `parked` at any point, with a note naming what holds it: a finding (`F-0000`), a question
(`Q-0000`) or a rig request (`G-0000`). A card with ANY test parked (`@Ignore`) is `parked`, not `ready`, even when
the rest of its tests are done (Ketura, 2026-10-08: the tracker must show it), and so is a card with any check
waiting on a rig request (a `waits <check>: G-0000` line in its ledger; a card test never shows a check by an
exception, and a card held by the rig does not move on as ready). land.py sets this itself. When the
last parked test of a card goes live, the card goes back to `ready`.

### Findings

Every error the campaign turns up gets a row in `findings.tsv` through `tracker.py finding add`, with what kind
of mistake it was:

| Kind | Means |
|---|---|
| `hjson-typo` | A wrong number, word or field in a definition |
| `hjson-logic` | The definition's implementation does not do what the text says: an oversight |
| `gametext` | The text shown to players is wrong but the card plays correctly |
| `engine` | The rules code is wrong |
| `rig` | The test rig is wrong |
| `test` | An existing test was wrong, empty, or tested nothing |
| `sheet` | The reviewed spreadsheet is wrong |
| `ruling` | Whether it is an error at all depends on a ruling |
| `data` | Anything else in the data files (formats, mappings) |

and a status:

| Status | Meaning |
|---|---|
| `candidate` | Believed wrong and NOT fixed: waiting on Ketura. Only for the two cases below |
| `question` | Hangs on an open question |
| `confirmed` | Known to be a fault (by her word, or plainly by the text) and not yet fixed: waiting for an engine or rulings pass |
| `fixed` | Mended without waiting for her word on it. She has not yet looked |
| `fixed-approved` | Mended, and she has said it is a fault (before the fix or after) |
| `not-a-bug` | She ruled it right as it is |

**The rule since 2026-10-07 (Ketura):** "you don't need to wait for my approval to fix cards; the text should do
what it says it does, and I think you only need to ask if you find an obvious intentional break from the text
rather than what appears to be an error or typo. Or if you are uncertain, of course." Of the first 150 findings
nearly all were real faults. So a definition that does not do what its text says is fixed where it is found (at
landing for a definition, in the next engine pass for the engine), recorded as `fixed`, and listed for her in the
report of that turn so that she can step in; it becomes `fixed-approved` when she says so. A finding stays a
`candidate`, unfixed, only where the departure from the text looks deliberate (a workaround for play, a comment
in the definition, a ruling that might explain it) or where you are not sure what the text means: then ask.
A question of what the text MEANS is still a question (`Q-`), asked as before.

### Questions and rulings

A question for Ketura goes in `questions.md` as `Q-0000` with the card, the two readings, and what each would
mean. Her answer is copied into `references/rulings-log.md`, which is in git and is the first thing an expectation
may rest on. An answer is a ruling for every later card, so write it down as she said it, under its subject, with
its date. What a ruling then means for definitions and tests, once the campaign has learned it (the words a
definition needs, the board that shows it, what the engine now does), goes in `references/shapes.md`. After
changing either, run `scripts/lookup.py --index`: agents read `references/rulings-index.md` in full and look the
entries up from it, so an entry missing from the index is an entry nobody reads.

## Getting commits to Ketura's repository

The work happens in a cloud clone, because the tests need Java 21 and Maven and the shell on her machine has
neither. She watches the branch in her git client, so every commit is sent across as soon as it is made, without
touching her working tree:

1. In the clone: `git bundle create <outputs>/card-tests/transport/cto.bundle <last sent>..feature/card-test-overhaul`
2. Write that file into `Claude outputs/card-tests/transport/` in her folder.
3. On her machine: `git fetch "Claude outputs/card-tests/transport/cto.bundle" feature/card-test-overhaul:feature/card-test-overhaul`

`scripts/orchestrator/postland.py` makes the bundle and, with it, one zip of the state files whose content has
changed (by hash; the outputs folder's modification times cannot be trusted, and a file written there takes a
few seconds to reach the store the carrying tool reads). `apply-sync.py`, kept in her `transport/` folder, does
steps 2 and 3 for both files and prints the head: check it. (`sync.sh` and `statefiles.py` are the older way,
file by file.)

That moves her local branch ref forward and nothing else. Never check anything out, pull, or push on her machine,
and never run `git status` there: her working tree is large enough on that mount that it takes minutes.

### Starting again in a fresh session

The cloud clone does not outlive the session. `scripts/orchestrator/bootstrap.sh` rebuilds it: its first lines
say what to bundle and zip on her machine and stage here first. It clones `PlayersCouncil/gemp-lotr`, fetches
the branch from the bundle, unpacks the state folder and `generator_sheets/`, records her branch head as the
last one sent and the state files as already hers, and runs `cardtest.py setup`. Then `cardtest.py suite all`
to confirm the starting point is green, before anything is launched. `HANDOFF.md` in the state folder says what
was running and what Ketura has said that no queue holds yet: read it after `roles/orchestrator.md`. The
workspace can also restart in the middle of a session (`roles/orchestrator.md`, "After the workspace has
restarted"): files survive, running processes do not, so the landers commit and the orchestrator sends
at every landing. The scripts under `scripts/orchestrator/` take the working folder from `$GEMP_WORK`
(`/home/claude/work`) and find the session's workflow journals by themselves.

## Reference

- `roles/orchestrator.md`: the orchestrator's own brief.
- `roles/author.md`, `roles/review-and-fix.md`, `roles/fixer.md`, `roles/lander.md`: the briefs given to sub-agents.
- `scripts/orchestrator/`: `close.py` (a landing, start to finish; it runs `integrate.sh` and `postland.py`),
  `apply-sync.py` (on her machine), `answers.py` (filing a set of her answers), `wf.py` (where each run stands, and
  its reports), `integrate.sh` with `resolve_made.py` (the second working tree), `bootstrap.sh` (a fresh session),
  `suite-run.sh` (the suite on the committed head, in a second clone), `cost.py` (what a run's steps cost),
  `at_batches.py` and `at_report.py` (the `at` conversion's batches, and Ketura's report on the old tests).
- `workflows/card-batch.js`: one batch, start to finish, as a Workflow script.
- `workflows/at-survey.js` with `roles/at-survey.md`, and `workflows/at-convert.js` with `roles/at-convert.md` and
  `roles/at-land.md`: the old `at` tests, surveyed and then converted batch by batch.
- `workflows/engine-pass.js` with `roles/engine.md`: an engine pass between waves. The orchestrator writes the
  queue of items as a file in the state (`orchestrator/engine-pass-<n>.md`, one section an item); one agent takes
  each in turn, commits it with `landing.py commit`, and writes a report beside the queue. `roles/doc-triage.md`
  is the brief for clearing a backlog of documentation suggestions the same way.
- `references/rulings-log.md`: what Ketura has ruled, grouped by subject.
- `references/shapes.md`: what the rulings and the engine's fixes mean for a definition and a test.
- `references/rulings-index.md`: both of the above, entry by entry; generated by `scripts/lookup.py --index`.
- `scripts/lookup.py`: prints whole entries of the rulings, the shapes, the rig's pitfalls and the skills; for a
  number by itself also the finding's row of `findings.tsv` or the question's entry in `questions.md`.
- `assets/tracker_template.html`: the tracker page; `tracker.py html` fills it.
