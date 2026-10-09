#!/bin/bash
# Brings the commits card batches made in the second working tree (branch cards-wip, /home/claude/work/cards) onto
# the campaign branch, which an engine pass may be working in at the same time.
#   integrate.sh          rebase cards-wip onto the branch, then move the branch up to it (fast-forward only)
#   integrate.sh --renew  afterwards, before the next batch: put cards-wip at the branch's head and rebuild there
# The fast-forward changes files in the main tree; git refuses it only if one of those files has uncommitted
# changes there (an engine agent mid-item): then wait for that agent's commit and run this again.
set -e
WORK=${GEMP_WORK:-/home/claude/work}
BRANCH=feature/card-test-overhaul
if [ "$1" = "--renew" ]; then
  cd "$WORK/cards" && git checkout -q cards-wip && git reset -q --hard "$BRANCH" && git log --oneline -1
  python3 .claude/skills/card-test-campaign/scripts/cardtest.py setup | tail -1
  exit 0
fi
cd "$WORK/cards"
[ -z "$(git status --porcelain)" ] || { echo "the second tree has uncommitted changes:"; git status --short | head; exit 1; }
N=$(git rev-list --count "$BRANCH"..cards-wip)
[ "$N" != "0" ] || { echo "nothing to integrate"; exit 0; }
if ! git rebase -q "$BRANCH" 2>/tmp/integrate.err; then
  # The two indexes are made files: take the branch's and make them again once the rebase is through.
  echo "rebase stopped:"; git status --short | grep -E "^(UU|AA|DU|UD)" | head; tail -3 /tmp/integrate.err; exit 2
fi
cd "$WORK/repo"
git merge -q --ff-only cards-wip 2>/tmp/integrate.err || { echo "the branch could not be moved up (a file in the main tree is being changed):"; tail -3 /tmp/integrate.err; exit 3; }
echo "integrated $N commit(s); branch at $(git rev-parse --short=9 $BRANCH)"
