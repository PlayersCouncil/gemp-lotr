#!/bin/bash
# Rebuilds the orchestrator's working folder in a fresh session.  Before running it, on Ketura's machine (device
# shell, in her repository; reads objects only, touches no working tree):
#   git bundle create "Claude outputs/card-tests/transport/full.bundle" origin/master..feature/card-test-overhaul
#   and zip "Claude outputs/card-tests" (without transport/) as state/... and generator_sheets/ as generator_sheets/...
#   into "Claude outputs/card-tests/transport/state.zip"
# then stage both files into this workspace and pass the folder they arrived in:
#   bootstrap.sh "/mnt/user-data/uploads/gemp-lotr/Claude outputs/card-tests/transport" <session link for the commit trailer>
set -e
IN="$1"; LINK="$2"
WORK=${GEMP_WORK:-/home/claude/work}
mkdir -p "$WORK" && cd "$WORK"
[ -d repo ] || git clone -q --single-branch --branch master https://github.com/PlayersCouncil/gemp-lotr.git repo
cd repo
git fetch -q "$IN/full.bundle" feature/card-test-overhaul:feature/card-test-overhaul
git checkout -q feature/card-test-overhaul
HEAD=$(git rev-parse HEAD)
mkdir -p "$WORK/unz" /mnt/user-data/outputs/card-tests/transport && cd "$WORK/unz"
python3 -I -c "import zipfile,sys; zipfile.ZipFile(sys.argv[1]).extractall('.')" "$IN/state.zip"
cp -a state/. /mnt/user-data/outputs/card-tests/
rm -rf "$WORK/repo/generator_sheets" && cp -a generator_sheets "$WORK/repo/generator_sheets"
cd "$WORK"
echo "$HEAD" > last_synced            # her branch is where the bundle was made
[ -f workflows.json ] || echo '{}' > workflows.json
mkdir -p analysis/progress analysis/fixes publish
printf 'Co-Authored-By: Claude <noreply@anthropic.com>\nClaude-Session: %s\n' "$LINK" > trailer.txt
# Everything just unpacked is what her machine already has: remember it, so the first sync carries only what changes.
python3 - <<'PY'
import hashlib, json
from pathlib import Path
state = Path("/mnt/user-data/outputs/card-tests")
seen = {"/".join(p.relative_to(state).parts): hashlib.sha1(p.read_bytes()).hexdigest()
        for p in state.rglob("*") if p.is_file() and "transport" not in p.parts}
Path("/home/claude/work/state_hashes.json").write_text(json.dumps(seen))
print(len(seen), "state files recorded as already on her machine")
PY
pip install -q hjson --break-system-packages
cd "$WORK/repo" && python3 .claude/skills/card-test-campaign/scripts/cardtest.py setup | tail -1
echo "clone at $HEAD; now: cardtest.py suite all (about three minutes), and write the commit trailer's model line"
