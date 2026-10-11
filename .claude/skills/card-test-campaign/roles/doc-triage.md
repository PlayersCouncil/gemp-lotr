# Brief: the suggestions from before there were landers

Until 2026-10-08 what authors and reviewers found wrong, unclear or missing in the documents was collected in one
file for the orchestrator to work through, and mostly was not. Landers now deal with each batch's suggestions as
it lands. You clear what is left: `/mnt/user-data/outputs/card-tests/orchestrator/doc-suggestions-pending.txt`
(about 110 KB: err-b51_172 to err-b58_103; its first lines list follow-ups, which are yours too).

Read `.claude/skills/card-test-campaign/roles/lander.md`, step 6 and "What you may and may not touch": those rules
are yours, with these differences.

- Read the pending file once, in two or three steps, and list the suggestions by subject before changing anything:
  the same thing was often reported by two or three agents, and a thing reported twice independently counts as
  shown. A single report is applied only if you verify it (`cardtest.py probe <file.java> --out doc-triage`, ten
  probes at most in all; nothing else is running tests) or a ruling settles it (`lookup.py`).
- Check each against the documents as they are now before writing (`lookup.py <words> --in pitfalls`, `--in
  standard`, `--in rig`, `--in hjson`): much has been applied piecemeal, and the engine pass before you has just
  changed some of it (read `/mnt/user-data/outputs/card-tests/orchestrator/engine-pass-16/` first: a suggestion
  about a rig method that pass built, or a behaviour it changed, is out of date).
- The files agents read through may grow by forty lines in all for this whole clearing, not ten: prefer
  `pitfalls.md` and `shapes.md`.
- Of the follow-ups at the head of the file, do the ones that are documents (the Q-0015 entry of the rulings log
  pointing forward to Q-0034; the shape for a reveal before "to" with an empty draw deck; the shape that says what
  separates capped X from uncapped X; the stray backslashes in game texts, replaced by a space in each
  definition). Leave the rest in the list.
- When you are done, rewrite the pending file to hold only what is left for somebody else (the follow-ups not
  done, with one line each), and write `/mnt/user-data/outputs/card-tests/orchestrator/batches/doc-triage.md`:
  what was applied and where, what was not and why (grouped, not line by line), and what went to
  `orchestrator/proposals.md`.
- Commit with
  `python3 .claude/skills/card-test-campaign/scripts/landing.py commit "Skills: the suggestions of err-b51_172 to err-b58_103 worked in" <paths...>`.
  If you changed definitions (the backslashes), run `cardtest.py suite 2>&1 | grep -E "^OK|^Tests run" | tail -3`
  (the cards, the mechanics, the definitions and the rig's own tests: what a definition can reach)
  first; it must end `OK`.

Return a few lines: how many applied, not applied and passed up; the commit; anything the orchestrator must act on.
