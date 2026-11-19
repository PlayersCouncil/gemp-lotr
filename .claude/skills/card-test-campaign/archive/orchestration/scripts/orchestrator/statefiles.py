#!/usr/bin/env python3
"""Prints the files argument for device_commit_files: the pending bundle and every state file whose content has
changed since it was last sent.  Content hashes, not times: the outputs folder is a network mount whose
modification times cannot be trusted.

    statefiles.py [--no-ledgers | --ledgers-of <id,id,...>]   list what to send (and remember the list)
    statefiles.py --mark                                      record that the last list was sent
"""
import hashlib, json, os, sys, time
from pathlib import Path
root = Path("/mnt/user-data/outputs/card-tests")
seen_file = Path("/home/claude/work/state_hashes.json")
listed_file = Path("/home/claude/work/last_listed.json")
seen = json.loads(seen_file.read_text()) if seen_file.exists() else {}
if "--mark" in sys.argv:
    listed = json.loads(listed_file.read_text()) if listed_file.exists() else {}
    seen.update(listed)
    seen_file.write_text(json.dumps(seen))
    if Path("/home/claude/work/pending_sync").exists():
        os.replace("/home/claude/work/pending_sync", "/home/claude/work/last_synced")
    print("marked", len(listed), "files")
    sys.exit(0)
only = None
if "--ledgers-of" in sys.argv:
    only = set(sys.argv[sys.argv.index("--ledgers-of") + 1].split(","))
out, listed = [], {}
if Path("/home/claude/work/pending_sync").exists():
    out.append({"stagedPath": str(root / "transport/cto.bundle"), "devicePath": "X:\\Projects\\gemp-lotr\\Claude outputs\\card-tests\\transport\\cto.bundle"})
for p in sorted(root.rglob("*")):
    if not p.is_file() or "transport" in p.parts or p.name in ("tracker.json", "tracker.html"):
        continue
    rel = p.relative_to(root)
    if "ledgers" in rel.parts:
        if "--no-ledgers" in sys.argv or (only is not None and p.stem not in only):
            continue
    digest = hashlib.sha1(p.read_bytes()).hexdigest()
    if seen.get(str(rel)) != digest:
        listed[str(rel)] = digest
        out.append({"stagedPath": str(p), "devicePath": "X:\\Projects\\gemp-lotr\\Claude outputs\\card-tests\\" + "\\".join(rel.parts)})
listed_file.write_text(json.dumps(listed))
# The outputs folder writes through to its store a few seconds after a file is closed, and the tool that carries
# files to the device reads the store: give the last write time to land.
time.sleep(8)
print(len(out), "files"); print(json.dumps(out[:50]))
