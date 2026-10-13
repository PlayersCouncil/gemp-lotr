#!/bin/bash
# Bundles whatever feature/card-test-overhaul has that the local repository has not been given yet.
set -e
cd /home/claude/work/repo
LAST=$(cat /home/claude/work/last_synced 2>/dev/null || echo scenario/authoring-4)
HEAD=$(git rev-parse feature/card-test-overhaul)
if [ "$(git rev-parse $LAST)" = "$HEAD" ]; then echo "nothing new"; exit 0; fi
git bundle create -q /mnt/user-data/outputs/card-tests/transport/cto.bundle $LAST..feature/card-test-overhaul
echo "$HEAD" > /home/claude/work/pending_sync
echo "bundled $(git rev-list --count $LAST..feature/card-test-overhaul) commit(s), head $HEAD"
