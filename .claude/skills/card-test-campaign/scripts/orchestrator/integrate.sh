#!/bin/bash
# Brings the commits batches made in the second working tree (branch cards-wip, /home/claude/work/cards) onto the
# campaign branch, which an engine pass may be working in at the same time.
#   integrate.sh          bring the second tree's landed commits onto the branch (see the three cases below)
#   integrate.sh --renew  afterwards, before the next batch: put cards-wip at the branch's head and rebuild there
# What can happen, and what this does about each:
#   1. The usual: cards-wip is rebased onto the branch and the branch is moved up to it (fast-forward only).
#   2. The rebase stops on a conflict.  In text a script makes (rulings-index.md, the pitfall index in
#      gemp-testing/SKILL.md) either side will do: resolve_made.py takes one, the rebase goes on, and the indexes
#      are made again and committed.  Anywhere else it stays stopped (exit 2) and is yours: in the Card Test
#      Standard's one-line row `F` (test-generation/SKILL.md), which both sides change often, BOTH sides' words are
#      kept; then `git add`, `GIT_EDITOR=true git rebase --continue`, and run this again.
#   3. The fast-forward is refused (exit 3): it changes files in the main tree, and git refuses if one of them has
#      uncommitted changes there (an engine agent in the middle of an item).  Wait for that agent's commit and run
#      this again.  If by then the NEXT batch is at work in the second tree, the landed commits are cherry-picked
#      onto the branch in the main tree instead; the later rebase drops them from cards-wip as already applied.
# "At work in the second tree": its working tree is not clean, OR workflows.json tracks a run in the tree `cards`
# that has not landed, finished or died (`wf.py at-work cards`).  A clean tree alone is no sign: an author still
# reading has changed nothing, and a rebase under him moves the files he is reading (2026-10-11, dec-b15_86).
# For the same reason --renew refuses (exit 4) while such a run is tracked.
set -e
WORK=${GEMP_WORK:-/home/claude/work}
BRANCH=feature/card-test-overhaul
HERE=.claude/skills/card-test-campaign/scripts
# The runs that may be at work in the second tree, by workflows.json and their journals (see the header).
# If it cannot be asked, the tree is taken to be in use: a cherry-pick in place of a rebase costs nothing.
BUSY=$(python3 "$WORK/repo/$HERE/orchestrator/wf.py" at-work cards 2>/dev/null) || BUSY="? (wf.py at-work failed)"
BUSY=$(echo $BUSY)
if [ "$1" = "--renew" ]; then
  [ -z "$BUSY" ] || { echo "not renewed: the second tree is in use ($BUSY, by workflows.json)"; exit 4; }
  cd "$WORK/cards" && git checkout -q cards-wip
  # Never throw away a landed batch: if cards-wip holds commits the branch has not taken yet (the fast-forward was
  # refused while an engine agent had one of their files open), bring the branch's new commits in under them instead.
  if [ "$(git rev-list --count "$BRANCH"..cards-wip)" != "0" ]; then
    git rebase -q "$BRANCH" && echo "cards-wip keeps $(git rev-list --count "$BRANCH"..cards-wip) commit(s) the branch has not taken; rebased onto $(git rev-parse --short=9 $BRANCH)"
  else
    git reset -q --hard "$BRANCH" && git log --oneline -1
  fi
  python3 $HERE/cardtest.py setup | tail -1
  exit 0
fi
cd "$WORK/cards"
[ "$(git rev-list --count "$BRANCH"..cards-wip)" != "0" ] || { echo "nothing to integrate"; exit 0; }
if [ -n "$(git status --porcelain)" ] || [ -n "$BUSY" ]; then
  # Case 3, second half: a batch is at work here (its tree is not clean, or a run is tracked in it).
  PICK=$(git cherry "$BRANCH" cards-wip | awk '$1=="+"{print $2}')
  [ -n "$PICK" ] || { echo "nothing to integrate (the branch already has every commit of cards-wip)"; exit 0; }
  # Ask before picking anything: a pick that stops on its second commit leaves the first to be undone under an
  # engine agent's feet.  If a file these commits change has uncommitted changes in the main tree, wait.
  TOUCHED=$(for c in $PICK; do git diff-tree --no-commit-id --name-only -r "$c"; done | sort -u)
  cd "$WORK/repo"
  OPEN=$(git status --porcelain | cut -c4- | sort -u | comm -12 - <(echo "$TOUCHED"))
  if [ -n "$OPEN" ]; then
    echo "the branch could not be moved up (case 3 above: a file in the main tree is being changed):"; echo "$OPEN" | sed 's/^/\t/' | head -5; exit 3
  fi
  if git cherry-pick $PICK >/tmp/integrate.err 2>&1; then
    echo "integrated $(echo $PICK | wc -w) commit(s) by cherry-pick (at work in the second tree: ${BUSY:-uncommitted changes}); branch at $(git rev-parse --short=9 $BRANCH)"
    exit 0
  fi
  git cherry-pick --abort 2>/dev/null || true
  echo "a batch is at work in the second tree and its landed commits could not be picked onto the branch:"; tail -3 /tmp/integrate.err; exit 3
fi
REMADE=0
if ! git rebase -q "$BRANCH" >/tmp/integrate.err 2>&1; then
  while [ -d "$(git rev-parse --git-path rebase-merge)" ] || [ -d "$(git rev-parse --git-path rebase-apply)" ]; do
    STUCK=$(git diff --name-only --diff-filter=U)
    [ -n "$STUCK" ] || { echo "rebase stopped with nothing in conflict:"; tail -3 /tmp/integrate.err; exit 2; }
    for f in $STUCK; do
      python3 $HERE/orchestrator/resolve_made.py "$f" || { echo "rebase stopped (case 2 above); in conflict:"; git status --short | grep -E "^(UU|AA|DU|UD)" | head; exit 2; }
      git add "$f"; REMADE=1
    done
    GIT_EDITOR=true git rebase --continue >/dev/null 2>>/tmp/integrate.err || true
  done
fi
if [ "$REMADE" = "1" ]; then
  python3 $HERE/lookup.py --index >/dev/null
  CHANGED=$(git status --porcelain | awk '{print $2}')
  if [ -n "$CHANGED" ]; then
    python3 $HERE/landing.py commit "Skills: the indexes made again after bringing the second tree's batch onto the branch" $CHANGED | tail -1
  fi
fi
N=$(git rev-list --count "$BRANCH"..cards-wip)
cd "$WORK/repo"
git merge -q --ff-only cards-wip 2>/tmp/integrate.err || { echo "the branch could not be moved up (case 3 above: a file in the main tree is being changed):"; tail -3 /tmp/integrate.err; exit 3; }
echo "integrated $N commit(s); branch at $(git rev-parse --short=9 $BRANCH)"
