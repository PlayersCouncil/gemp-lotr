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

Alongside: the tests under `at/` (written before the rig) are rewritten as card tests or as card-agnostic
mechanic tests, and the dormant test files for earlier iterations of PC cards are kept compiling in step.

A chunk is one culture file of one set (`file:set01-Elven`), or one V-set, small enough for Ketura to review in a
sitting. Nothing in a chunk is finished until she has approved it.

## Who does what

**The orchestrator** (the main session) chooses the chunk, hands cards to authors, gates what comes back, makes
every commit, sends each commit to Ketura's repository, records findings and questions, keeps the tracker, and
talks to Ketura. Only the orchestrator touches git, the campaign's state, card definitions, the engine and the rig.

**An author** takes a few cards and writes each one's test file, in two stages that must stay in order: first
working out what the card should do from its text and the rules alone, then writing and running the tests. Brief:
`roles/author.md`.

**A reviewer** takes cards an author has finished, with no knowledge of how they were written, tries to find
what is wrong with them (a ledger that misreads the card, a test that would pass whatever the card did, a finding
that is really a misread ruling), and puts right what it finds. Brief: `roles/review-and-fix.md`.

**A fixer** is only for a list from the orchestrator after the review: a ruling to apply, a definition that
changed. It works through the list, changing what it faults and nothing else. Brief: `roles/fixer.md`.

An author, reviewer or fixer works only on its own cards' test and ledger files. No role edits anything else,
commits, or decides that the game is wrong: they report, and the orchestrator and Ketura decide. Nobody checks a
reviewer's or a fixer's changes but the orchestrator's gate (`land.py`: the conformance check and the card's
tests).

### Running the pipelines

`workflows/card-batch.js` is the whole passage of a few cards as a Workflow script: an author, then a reviewer
who fixes what it finds. Run it with `args` of `{repo, cards, folder, notes}`: the repository's absolute path, the
card ids, a short unique folder name (`v1-b07`), and anything the author should know that the documents do not
say. With `fixes` (a numbered list per card) it runs a fixer on that list and nothing else. It returns each
stage's report as structured data.

**Three cards to a pipeline, three pipelines at once.** This shape was cut down on 2026-10-05 from four stages
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

Do not run `cardtest.py suite` or rebuild a module while pipelines are running: both rewrite classes the agents'
tests are loading. Rig changes, tool changes and full-suite runs go in the gaps between waves. `usage.tsv` has
what each pipeline cost; `scripts/orchestrator/wf.py usage <folder>` works it out.

**Nothing reports back by itself reliably.** A finished workflow sends a notification, but the workspace can
restart or go quiet and the notification is then lost; the results are still in each run's `journal.jsonl`.
Keep a scheduled message a little way ahead at all times (the session's `send_later`) that says to check the
pipelines, land what has finished, launch the next cards and schedule the next message. That message is what
wakes a session that has gone quiet. `scripts/orchestrator/wf.py` (`track`, `status`, `show`, `landed`) reads the
journals; its paths are the session's own and need changing for a new one.

When a pipeline returns, the orchestrator gives every finding, question and rig request its number (replacing the
`F-0000` and `Q-0000` placeholders in `@Ignore` lines) and records them, then runs `land.py <ids>`, which checks
each card again and commits and marks the ones that clear. It sends the commits across and adds a row to
`usage.tsv`. What authors and reviewers report under "documentation issues" and "standard issues" is the
campaign's main source of improvement, but reading them costs the orchestrator its own context on every later
step: collect them in a file as batches land and go through them together every ten cards or so, changing the
standard, the briefs or the tools then.

### One card, start to finish

1. Author, blind: `packet.py <id> --blind --terms`. Write the ledger file and, for each check, the situation and
   the outcome the text calls for. Write down every doubt as a question.
2. Author, sighted: `packet.py <id> --with-test`. For an existing test file, account for every existing test: it
   claims a check, or it is redundant, or it knows something the ledger missed (then the ledger is what changes),
   or it contradicts the ledger (then it is a question; existing PC tests often encode a ruling).
3. Author: write the file; `lint.py <id>`; `cardtest.py run <id> --out <name>`. (The run of stock mistakes,
   `mutate.py <id>` on its own, was cut on 2026-10-06: see below.) A test that fails is looked at three ways before anything else: is the test wrong,
   is the expectation wrong, or is the game wrong.
4. Reviewer: `lint.py`, `cardtest.py run`, then the checklist at the end of the test-generation skill, with
   single mistakes (`mutate.py <id> --replace`) to prove a suspected gap. The reviewer fixes what it finds.
5. Orchestrator: once the review is done, one commit for the batch (`land.py <ids> --batch <folder>`: test
   files and any definition fixes of those cards together), send it across, mark each card `ready` or `parked`.
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
| `packet.py <id>` | Everything about one card in one place. `--blind` leaves out the implementation; `--terms` adds the rules text its wording touches; `--with-test` adds the current test file. |
| `rules.py` | Looking things up in Comprehensive Rules 4.2: `term`, `card`, `terms-for`, `grep`, `interactions`. |
| `reconcile.py` | Reviewed spreadsheet against definitions, field by field. |
| `cardtest.py` | `setup` once, then `run <ids> --out <name>` in seconds; `suite` for everything. |
| `lint.py` | The conformance check. |
| `rules.py council [LR 6]` | The Rules Councils' rulings since CR 4.2 (eight Legacy, two Modern), which `rules.py card` and `grep` also search. |
| `mutate.py <id>` | Makes deliberate mistakes in a copy of the definition and reports the ones no test notices. |
| `land.py <ids>` | The orchestrator's gate: re-runs the conformance check and the tests on each card, and commits each card that clears them on its own. `--check` to look without committing. |
| `tracker.py` | The campaign's state: statuses, findings, the tracker. |
| `gen_vts_api.py` | Rewrites the rig's API reference from source; `--check` says whether it has drifted. |

Selectors work wherever an id does: `set:1`, `file:set01-Elven`, `seg:vset`.

The generator sheets are in `generator_sheets/` at the root of the repository (or wherever `GEMP_CARD_SHEETS`
points). They are not in git.

- `gen_en_standard.tsv`: every Decipher card's current official text, Decipher's own errata included. Reviewed.
- `V1.tsv`, `V2.tsv`, `V3.tsv`: the V-sets as first released.
- `master_errata.tsv`: every PC erratum by revision, assembled from some twenty older sheets. About 95% accurate:
  some rows are errata that were proposed and never released, or held back a release.

The tools match a definition to its sheet row by the definition's revision number. `reconcile.py --all --report`
writes every disagreement as a document (`sheet-discrepancies.md` in the campaign's state); for PC cards those
are flags for Ketura, not findings.

## Where state is kept

Three places, each for one kind of thing.

- **Git, on `feature/card-test-overhaul`:** the tests, the skills, the scripts, the rulings log. One commit per
  card (`Card tests: 1_40 Elrond, Lord of Rivendell`), rig changes and tooling in commits of their own.
- **`Claude outputs/card-tests/` in Ketura's folder (not in git):** `baseline.tsv` (what every card's tests looked
  like before the campaign), `status.tsv`, `findings.tsv`, `questions.md`, `rig-requests.md`, `usage.tsv` (tokens
  spent per batch of cards), `sheet-discrepancies.md`, `focus.txt`, `ledgers/<set>/<id>.txt` (each card's ledger:
  Ketura wants these kept out of the test files), and the generated `tracker.md`, `tracker.json` and
  `tracker.html`. In the cloud workspace the working copy is `/mnt/user-data/outputs/card-tests/`, which the
  scripts find by themselves; `GEMP_CAMPAIGN_STATE` points them anywhere else.
- **The tracker artifact:** `tracker.html` published; republished from the same file after each chunk and whenever
  statuses change.

### Statuses

`todo` → `in-progress` → `ready` (committed, passes the conformance check and its tests, waiting for Ketura) →
`approved`. `parked` at any point, with a note naming what holds it: a finding (`F-0000`), a question
(`Q-0000`) or a rig request (`G-0000`). A card with ANY test parked (`@Ignore`) is `parked`, not `ready`, even when
the rest of its tests are done (Ketura, 2026-10-08: the tracker must show it); land.py sets this itself. When the
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
may rest on. An answer is a ruling for every later card, so write it down as she said it.

## Getting commits to Ketura's repository

The work happens in a cloud clone, because the tests need Java 21 and Maven and the shell on her machine has
neither. She watches the branch in her git client, so every commit is sent across as soon as it is made, without
touching her working tree:

1. In the clone: `git bundle create <outputs>/card-tests/transport/cto.bundle <last sent>..feature/card-test-overhaul`
2. Write that file into `Claude outputs/card-tests/transport/` in her folder.
3. On her machine: `git fetch "Claude outputs/card-tests/transport/cto.bundle" feature/card-test-overhaul:feature/card-test-overhaul`

`scripts/orchestrator/sync.sh` makes the bundle and `statefiles.py` lists what to carry across with it (the
state files whose content has changed, by hash; the outputs folder's modification times cannot be trusted, and a
file written there takes a few seconds to reach the store the carrying tool reads). Check the branch head on her
machine after every fetch.

That moves her local branch ref forward and nothing else. Never check anything out, pull, or push on her machine,
and never run `git status` there: her working tree is large enough on that mount that it takes minutes.

### Starting again in a fresh session

The cloud clone does not outlive the session. To rebuild it: clone `PlayersCouncil/gemp-lotr`, bundle
`origin/master..feature/card-test-overhaul` on her machine and fetch it into the clone, copy the state folder
(ledgers included) and `generator_sheets/` across, run `cardtest.py setup`, then `cardtest.py suite cards` to
confirm the starting point is green. The workspace can also restart in the middle of a session: files survive,
running processes do not, so commit and send often.

## Reference

- `roles/author.md`, `roles/review-and-fix.md`, `roles/fixer.md`: the briefs given to sub-agents.
- `workflows/card-batch.js`: one batch, start to finish, as a Workflow script.
- `references/rulings-log.md`: what Ketura has ruled.
- `assets/tracker_template.html`: the tracker page; `tracker.py html` fills it.
