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

**A reviewer** takes cards an author has finished, with no knowledge of how they were written, and tries to find
what is wrong with them: a ledger that misreads the card, a test that would pass whatever the card did, a finding
that is really a misread ruling. Brief: `roles/reviewer.md`.

**A fixer** takes a reviewer's list for cards that did not pass and works through it, item by item, changing what
the list faults and nothing else, and refusing an item the card's text does not support. Brief: `roles/fixer.md`.
A second reviewer then re-checks just those items (the "Re-checking" section of the reviewer's brief). A card gets
one round of fixing; what is still wrong after it comes back to the orchestrator.

An author or fixer works only on its own cards' test and ledger files. No role edits anything else, commits, or
decides that the game is wrong: they report, and the orchestrator and Ketura decide.

### Running the pipelines

`workflows/card-batch.js` is the whole passage of a few cards as a Workflow script: author, reviewer, and where the review
asks for changes, fixer and re-check. Run it with `args` of `{repo, cards, folder, notes}`: the repository's
absolute path, the card ids, a short unique folder name (`v1-b07`), and anything the author should
know that the documents do not say. With `fixes` (a numbered list per card) it goes straight to fix and re-check.
It returns each stage's report as structured data.

**Three cards to a pipeline, several pipelines at once.** An agent carries about 100K tokens before it has
done anything (the harness, then the documents its brief has it read), and re-reads all of it on every step, so
that fixed load is most of what a card costs. Measured on V1: three cards through all four stages cost about 6M
cache-read tokens a card; one card on its own cost the same for two stages. Group cards of one culture, so
helper cards and context are shared. Six pipelines saturate two cores (the mutation check starts a JVM per
mistake) but run. Do not run `cardtest.py suite` or rebuild a module while pipelines are running: both rewrite
classes the agents' tests are loading. Rig changes, tool changes and full-suite runs go in the gaps between
waves. `usage.tsv` has what each pipeline cost; `scripts/orchestrator/wf.py usage <folder>` works it out.

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
campaign's main source of improvement: read every one, and change the standard, the briefs or the tools while
no pipeline is running.

### One card, start to finish

1. Author, blind: `packet.py <id> --blind --terms`. Write the ledger file and, for each check, the situation and
   the outcome the text calls for. Write down every doubt as a question.
2. Author, sighted: `packet.py <id> --with-test`. For an existing test file, account for every existing test: it
   claims a check, or it is redundant, or it knows something the ledger missed (then the ledger is what changes),
   or it contradicts the ledger (then it is a question; existing PC tests often encode a ruling).
3. Author: write the file; `lint.py <id>`; `cardtest.py run <id> --out <name>`; then `mutate.py <id>` to see
   what the tests would miss. A test that fails is looked at three ways before anything else: is the test wrong,
   is the expectation wrong, or is the game wrong.
4. Reviewer: `lint.py`, `cardtest.py run`, `mutate.py <id>`, then the checklist at the end of the
   test-generation skill. Every surviving mistake from `mutate.py` is either a gap (back to the author, once) or
   explained in one line.
5. Orchestrator: commit the card on its own, send the commit across, mark the card `ready` or `parked`.
6. At the end of the chunk: `cardtest.py suite all`, rebuild the tracker, and give Ketura the chunk to review.
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
(`Q-0000`) or a rig request (`G-0000`).

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

and a status: `candidate` (believed wrong, not yet confirmed by Ketura), `question`, `confirmed`, `fixed`,
`not-a-bug`. For now a candidate is parked and never fixed on the spot: Ketura rules on it first, because a
correct implementation of an obscure ruling looks exactly like a bug. When she changes that, this paragraph
changes. Of the first ten findings about cards and rules, she ruled five real errors, two deliberate departures
for the sake of play, and three accounted for by a ruling or a convention; that is the rate to expect.

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

- `roles/author.md`, `roles/reviewer.md`, `roles/fixer.md`: the briefs given to sub-agents.
- `workflows/card-batch.js`: one batch, start to finish, as a Workflow script.
- `references/rulings-log.md`: what Ketura has ruled.
- `assets/tracker_template.html`: the tracker page; `tracker.py html` fills it.
