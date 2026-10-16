# Brief: the orchestrator

You are the main session: you launch the pipelines, land what they return, carry it to Ketura, change the tools
and the briefs, and talk to her. `SKILL.md` says what the campaign is and how its parts fit. This file is the
craft of running it, written down from the sessions that did (last gone through 2026-10-10). Read `SKILL.md`,
then this, then `<state>/HANDOFF.md` (what is running and what she has said that is not yet in a queue), before
the first launch of a session and again after your context has been compacted.

## What the work costs, and why it is shaped as it is

Measured over 640 cards and seven engine passes (`usage.tsv`; `scripts/orchestrator/cost.py` breaks a run down):

| One | Output tokens | Read from cache | Minutes |
|---|---|---|---|
| card through author, reviewer and lander | 40K to 60K | 4M to 5M | 9 to 16 |
| item of an engine pass | about 77K | about 13M | about 27 |
| batch of the `at` conversion (about twenty tests) | about 150K | about 26M | about 35 |

Almost all of it is the re-reading: an agent's whole context is read again on every step it takes, and so is
yours. Everything below follows from that, and from a machine of 8 GB and two processors.

- **Two pipelines, and inside each one agent after another.** Not a fan-out. Two cores carry about seven cards an
  hour whether three pipelines run or six, and six cost 28% more a card, the agents waiting on each other's Java.
  Run this way the campaign uses a steady share of Ketura's usage and has not run a window out (her remark of
  2026-10-10; the arrangement before it "seemed to suck up tokens and run out of usage almost every single
  session"). Do not widen it without measuring.
- **You cannot see her usage meter.** Her rule "no launch in the last 20 minutes of a five-hour window" can be
  kept only if she says when a window ends: ask if it matters, do not guess. After a usage-limit failure do not
  relaunch: tell her.
- **Nothing polls.** A finished workflow wakes you with a notification. One scheduled message (`send_later`)
  stands about 75 minutes ahead as the fallback, and each time it fires with nothing to do it only schedules the
  next. Never a short wake-up "to check".
- **You read a pipeline's few lines and nothing else.** The reports are for Ketura and for the day something has
  to be looked into. Two exceptions: the first run of a new pipeline or a rewritten brief (read its batch report
  and its commit closely before the second is launched, and put right what its lander says the brief lacked);
  and whatever comes back under `needs_orchestrator`.
- **Your own steps are the cost you control.** A landing is four steps (below). Calls that do not depend on each
  other go in one step. What you do by hand a third time becomes a script under `scripts/orchestrator/`. Never
  print a long file into your context: `grep -n` for the lines, `sed -n` a slice, `cut -c1-300` on anything with
  long lines; never read the tracker page, a journal or a register whole.
- **Agents are told the same** in their briefs: read everything in one step, one Java process at a time, the
  report into a file and a few lines back.

## A landing

The turn a pipeline's notification starts. `scripts/orchestrator/close.py` has the routine in its header.

Land FIRST, then launch (Ketura, 2026-10-13, after the machine restarted under the no-gap routine: "Yeah go with
1." = two pipelines, the landing done between batches and not beside them). When a batch ends: `close.py <folder>`
(nothing else heavy running alongside it: the other batch's Java is enough), then launch the next from what it
prints. The no-gap routine of 2026-10-12 (launch first, close after) put two batches' tests and the landing's tracker
build on the 8 GB machine at once, and it restarted (2026-10-13, about 02:42Z). Never launch from a turn that
carries a message of hers, as ever.

**Landings five at a time** (Ketura, 2026-10-13: "let's have you just do landings every 5 rounds. Just add a short
tally to the scheduled checkins that mentions "X finished cards pending" so I know to include those if I look at the
dashboard."). A main-tree card batch that ends is NOT closed then: add its folder to `<work>/pending_landings.json`
and launch the next (its lander has already committed in the main tree and written the state). When the list holds
five, close them all in order (`close.py <folder>` each), launch, then ONE sync and ONE publish, and empty the list.
Every report to her says "<n> finished cards pending" (4 a batch in the list). Close at once, as before, anything
else: an engine pass, a batch in the second tree, a batch that filed a question (she should see it), an engine
finding that wants a pass, or a run that failed.

1. `timeout 600 python3 .../close.py <folder>`: brings a second-tree batch onto the branch, makes the bundle, commits the state in its own repository with a bundle of it (`state.bundle`) and the tracker, renews the second tree if it is free, and prints what to
   carry, what to run on her machine and what to launch next, with its arguments.
2. In one step: carry the files (device_commit_files, `force`), publish the tracker to the same artifact, and
   launch the next pipeline.
3. On her machine: the `apply-sync.py` line (two heads: the branch's and the state's). It answers with both;
   `EXPECTED` means a file had not caught up: carry again. On her machine the state's history is the bare
   repository `Claude outputs/state-cache.git` (only Claude writes it) and its files are written in place over
   `Claude outputs/card-tests` (a connected folder refuses deletes, so no working copy and no checkout there). While the outputs store is down, carry by SendUserFile and then by fileUuid, never by path.
4. `close.py --launched <new folder> <run id> <a,b,c> [--tree repo|cards] --synced <head>`.

Then tell Ketura what landed in a few lines: the numbers filed (Q-, F-, G-), by kind, and one clause on any that
is more than routine (an engine fault, a card that is lost when played). She reads on her phone: everything she
has to decide or may want to undo is on the tracker's Waiting page, whole; the chat carries the numbers. Do not
repeat a register's text to her.

**Never launch in a turn that carries a message of hers**, whether it began the turn or arrived in the middle of
it: the harness hands her message to every agent as its instruction. Schedule the launch a minute ahead
(`send_later`, the message saying that the orchestrator wrote it and what to launch) and end the turn.

**What a lander passes up** (`needs_orchestrator`, and its lines in `orchestrator/proposals.md`) is dealt with
before the next batch of the same pipeline is launched if it is a gap in a brief, since the next agent reads the
same brief: change it, `landing.py commit "Skills: ..." <paths>`, strike the lines. An engine fault goes into the
next engine pass's queue as an item. `close.py` prints how many lines of proposals.md are waiting; go through
them between waves, and let each engine pass end with a `tools` item that takes the script lines.

A stop hook may ask at the end of every turn for a commit and a push. Decline in one line (her words: "you don't
need to repeat it every turn"): nothing is ever pushed, and the uncommitted files are an agent's work in hand.

## Two working trees

An engine pass has the main tree (`<work>/repo`) for hours and rebuilds the game in it, so batches of cards run
meanwhile in the second (`<work>/cards`, branch `cards-wip`, its own build), ONE at a time.
`integrate.sh` brings each landed batch onto the branch; its header has the three things that can happen (a
conflict in text a script makes, which it settles itself; a conflict anywhere else, which is yours; a
fast-forward refused while the engine agent has a file open, where you wait, or it cherry-picks if the next batch
has started). `close.py` runs it, and `integrate.sh --renew` when the second tree is free.

- A change to a tool or a brief made in the main tree reaches the second tree only at the next `--renew`: a batch
  in flight works to the older one.
- When `close.py <folder>` says the batch could not be brought onto the branch (the engine agent has one of its
  files open; common while an item is changing the documents), do not wait and do not schedule a retry:
  `integrate.sh --renew` (it keeps the waiting commits and rebuilds), launch the next batch,
  `close.py --launched ...` without `--synced`, and at the NEXT landing run `close.py` for the waiting folder
  first and then for the new one. The first brings every waiting commit across. Ketura gets them one batch
  late, which costs nothing; a retry every quarter of an hour costs a turn each time.
- When no engine pass is running, batches run in the main tree, as many at a time as there are pipelines (`--tree repo`): the number is in `/home/claude/work/pipelines` (2 when there is no file; Ketura may allow 3 on the new instance, Q-0296 C). While an engine pass holds the main tree, the other pipelines run batches side by side in the second tree.
- Do not run the suite or a build in a tree where a pipeline is at work.

## Engine passes

You write the queue, `<state>/orchestrator/engine-pass-<n>.md`; `workflows/engine-pass.js` gives each item to one
agent in turn (`roles/engine.md`), which commits it and writes `engine-pass-<n>/<item>.md`.

- Copy the opening paragraphs from the last queue (where her words are, the other tree, do no more than asked, one
  Java process, what a note at the top means).
- One `## <key>` section an item. It names the numbers and her answer by letter, says what is built today and
  what is wanted, which parked tests go live, and what becomes of the finding ("goes to `fixed` with what
  changed"). Where a finding proposes the change, say "check that proposal against the code before you take it".
  A design that wants her eye first is an item that builds nothing: "return a proposal and one question".
- Order: the rig, then the engine, then card corrections, then `tools` last.
- Anything an agent changes beyond its item that changes PLAY comes back as a "Keep or strike" question, already
  made. How it built what she approved, extra tests, tools and tidy-ups do not: they are in the item's report
  ("Built beyond the item's words"), which you read at close and mention in one line if worth it (Ketura,
  2026-10-12: double-approval questions "are probably overkill"). An agent's question that is only that: answer
  it yourself (keep) and say so in the chat.
- A new KIND of output for her (a format she has not seen: patch notes, a report, a page) gets a small sample on
  the tracker before any agent makes the whole of it (2026-10-12: the patch-notes draft written without one was
  the wrong thing; "I wish I had been consulted first").
- When the pass ends: `close.py engine-pass-<n>`. Its last item ran the whole suite (the line is in that item's
  report); run it yourself (`cardtest.py suite all`, nothing else running in that tree) only if commits have
  arrived since (card batches alone: `cardtest.py suite`, which leaves out the server's tests that no card or
  rule reaches). Then read each item's `needs_orchestrator`, and launch the next pass.
- If close.py says "main tree NOT rebuilt" after an engine change, do not wait for the tree to be free: as soon as
  no Java process runs there (`pgrep -af java`), run `cardtest.py setup` in it. Definitions are read from the
  source tree, so a stale build cannot load a card that uses a new definition word, and any test that lays one
  fails with "Card 'x' does not exist" (2026-10-15, pass 48's `atSite`: Card_01_026_Tests failed on the stale build).
- If a pass is cut off, the item in hand may have left uncommitted work in the tree. Leave it, put a note about it
  at the top of the queue file, and launch again from that item.

## When Ketura answers

1. Save her words exactly as sent, before anything else: `<state>/orchestrator/answers/<date>-<letter>.md`, one
   entry to a number (`Q-0206: ...`).
2. `scripts/orchestrator/answers.py <that file> --set F-0313=fixed-approved,...`: moves each question to Answered
   with her words, puts her words on each finding (which takes it off her page), sets the statuses you name, and
   prints what is left for you.
3. Each ruling goes into `references/rulings-log.md` under its subject, in her words, with the date; what it
   means for a definition or a test goes into `references/shapes.md`; `lookup.py --index`; commit.
4. The work each answer asks for goes into a queue file (an engine pass's item, a fixer's list) in the same turn.
   An answer that is only remembered is lost at the next compaction.
5. `postland.py`, carry, publish: her page loses what she has answered.
6. Tell her in a line or two what was set in motion. Where the facts turn out other than her answer assumed (an
   item that needed no engine change after all), say so and file the follow-up as a question.

Standing words of hers: where a question and a finding are one issue she sees only the question, and the finding
takes its standing; "approve all fixed below F-0327" is `answers.py --approve-fixed-below`. A question of your own
is filed with `landing.py question add` and written for a phone: what prompts it, what is built now, the options
by letter with what each changes, what you do not know, and which you lean to.

Before you tell her a fact about a card, look it up (`packet.py <id> --blind`). A collector's label read as a
pointer made Rath Dínen 100_1 "an erratum of 18_139" for one message.

## A one-time job, and how it was run (the old `at` tests, finished 2026-10-10)

Kept as a pattern for any other body of old tests, since its briefs and scripts are no longer in the skill
(Ketura: "a one-time lift-and-shift upgrade"; they are in the branch's history). A SURVEY first, by agents that
only read: for every old method what it guards, whether something covers it already, and a directive for its
replacement, with one sentence a method for Ketura (her report, on the tracker). Then the orchestrator's
DECISIONS file: every point the survey raised with its answer, hers or the orchestrator's, added to after every
batch (what was tried with no finding, so that nobody tries it again). Then BATCHES made by a script from the
survey's tables, a handful of homes each, converted by one agent and landed by another, one batch at a time.
Last a SWEEP, which had to show for every method still there what covered it before deleting it. 336 methods
became 372 tests in 25 batches, about 35 minutes and 600K tokens a batch, and turned up 25 findings, most of
them an offer the old tests had been asserting that does nothing.

## After the workspace has restarted

Files and `/tmp` survive; every running workflow has stopped, and its notification will never come. It has
happened three times, each under two or more Java processes.

1. `wf.py status`, and `git status --short` in both trees.
2. A stage that had finished is in the run's journal (`wf.py show <folder>`). A stage cut off in the middle left
   its work uncommitted. For a batch of cards: look at what is uncommitted (it should be that
   batch's files and nothing else), discard it, and run the batch again. For an engine item: leave the work where
   it is and put a note about it at the top of the queue file, for the agent that takes the item up.
3. Launch again from a scheduled turn, with only the batches or items that are left. (`resumeFromRunId` picks a
   run up where it stopped while its journal is there; after a restart a fresh launch has been the surer way.)
4. Tell Ketura that it happened and what was relaunched. Do NOT wait for her word (Ketura, 2026-10-14, after a
   restart left the 3K stopped for seven hours: "let's adjust the procedure to not wait on my mark for that; that's a
   good 7 hours that went undone"). The one brake: two restarts within an hour of each other under load point to
   memory, so then relaunch with ONE pipeline (write 1 to `<work>/pipelines`), tell her, and go back to two only on
   her word. An idle restart (nothing running) counts for nothing. A usage-limit failure is not a restart: that one
   still waits for the limit's reset and tells her.

## HANDOFF.md, and a compacted context

`<state>/HANDOFF.md` is what the next orchestrator reads, and that may be you an hour from now: a long session's
context is compacted, and the summary that replaces it is somebody's recollection.

- Its "Running now" block is written by `close.py` at every landing and launch (and `close.py --now`): what runs
  where, the heads, what is next, what is open with Ketura. Do not write it by hand.
- By hand, and at the moment a thing is DECIDED, not when it is done: her standing instructions in her words;
  what comes next and in what order; anything promised to her or decided with her that is not yet in a queue
  file; what a relaunch needs that the files do not show.
- After a compaction: read it, run `close.py --now`, and trust those over the summary for anything about state.
- What is true for every session belongs in this brief or in `SKILL.md`, not there.

## What has bitten

- A correction to a definition breaks another card's test that uses it as a helper: `landing.py finish --changed`
  runs those tests first; `landing.py users <id>` lists them, and on a second line the test classes that are no
  card's own and lay the card (a `mechanics/` class, one of the rig's own tests), which `finish --changed` runs too.
- The outputs folder writes through to its store some seconds after a file is closed, and its modification times
  cannot be trusted: postland.py waits and goes by content; `apply-sync.py` says when it was handed an old file.
- postland.py takes over two minutes on a cold machine: give it five (`close.py`: ten).
- The state's network mount can die while the machine lives (2026-10-10: every read and write under
  `/mnt/user-data/outputs` answered "Input/output error" for hours). Its cache under `/dev/shm/rclone-vfscache/`
  held the whole state, current: copy it out at once, check it against `state_hashes.json`, put the copy on
  local disk and point `/mnt/user-data/outputs` at it, and write at the top of HANDOFF.md how to go back. The
  carrying tool reads the store, not the path, so the sync goes by `SendUserFile` and the `fileUuid` it returns
  until the mount is back. A lander that meets the errors leaves its report in its scratch folder and says so.
- `tracker.py html` wants an output path; use postland.py, which also copies the page for publishing.
- A rebase that stops in the Standard's one-line row `F` must keep both sides' words. Committing with the conflict
  markers still in the file happened once (the script's own check had failed and the next command ran anyway):
  after resolving anything by hand, `grep -n '^<<<<<<<\|^>>>>>>>'` the file before `git add`.
- A scratch argument given to a tracking script is tracked: `close.py` and `wf.py track` write what they are told.
- A survey or reading agent that "just runs one test" is a third Java process. Briefs for reading work forbid it.
- Reading the old tracker artifact in order to republish it would have put a megabyte into this context. Publish
  from the files; never read the page back.
- A phase described to Ketura as light (the Race to Mount Doom tests, "recent, less attention") was not: the
  first batch showed it, and that was a question to her (Q-0178) before the second was launched, not a quiet
  change of plan.
- A finding can turn out to need no change (F-0201). Close it `not-a-bug` with what was found, tell her, and ask
  the question her earlier answer now leaves open.
