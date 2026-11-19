# The campaign's orchestration, archived

The card test campaign ran from 2026-10-04 to 2026-11-05: an orchestrator session in a cloud clone launched
pipelines of sub-agents (author, reviewer, lander; engine passes between waves), landed their work as commits, and
carried the commits and the campaign's state to Ketura's machine as git bundles. That machinery is finished and is
kept here, unchanged but for pronouns, for the day another campaign (a new set, a re-audit) wants it.

- `CAMPAIGN.md`: the skill's SKILL.md as it stood while the campaign ran: order of work, roles, pipelines, the
  landing in four steps, statuses, how commits reached Ketura's repository, starting a fresh session.
- `roles/`: the briefs of the orchestrator, lander, fixer, engine-pass agent and doc triage. The author's and the
  reviewer's briefs are still live, in the skill's own `roles/`.
- `scripts/`: `land.py` (the gate), `landing.py` (the lander's tools), `tracker.py` (statuses, findings, the
  tracker), and `orchestrator/` (close.py, postland.py, apply-sync.py, answers.py, wf.py, integrate.sh,
  bootstrap.sh, suite-run.sh, cost.py, patchnotes.py and the rest).
- `workflows/`: `card-batch.js`, `engine-pass.js`, `one-agent.js`.
- `assets/tracker_template.html`: the tracker page.

## Reviving it

Move each folder back to where it was, relative to the skill root: `scripts/land.py`, `scripts/landing.py` and
`scripts/tracker.py` to `scripts/`; `scripts/orchestrator/` to `scripts/orchestrator/`; `roles/*` to `roles/`;
`workflows/` and `assets/` to the skill root. CAMPAIGN.md's paths then hold again. Two scripts that were in
`scripts/orchestrator/` were kept live and now sit in `scripts/` (`twins.py`, `applyfix.py`; also `deltest.py`):
an orchestrator script that runs one of them by its old path needs that path changed.

The campaign also kept state outside git (`Claude outputs/card-tests` on Ketura's machine, a git repository of
its own): statuses, the registers, usage, the queues and batch reports, the ledgers. The registers as they stood
at the end are copied into `../` (findings.tsv, questions.md, rig-requests.md).
