#!/usr/bin/env python3
"""Runs on Ketura's machine, from Claude outputs/card-tests/transport/: unpacks state-delta.zip over the card-tests
folder and fetches cto.bundle into feature/card-test-overhaul.  Moves the branch ref and nothing else: no checkout,
no pull, no push, no status.  Prints one line.

    apply-sync.py [<expected head>]
"""
import subprocess, sys, time, zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
STATE = HERE.parent
REPO = STATE.parent.parent
BRANCH = "feature/card-test-overhaul"
want = sys.argv[1] if len(sys.argv) > 1 else ""


def git(*args):
    return subprocess.run(["git"] + list(args), cwd=REPO, capture_output=True, text=True)


out = []
delta = HERE / "state-delta.zip"
if delta.is_file():
    for attempt in range(6):
        try:
            with zipfile.ZipFile(delta) as z:
                names = z.namelist()
                for name in names:
                    target = STATE / name
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(z.read(name))  # written over in place: nothing is deleted
            out.append("state: %d file(s)" % len(names))
            break
        except zipfile.BadZipFile:
            time.sleep(5)  # the file is still arriving
    else:
        out.append("state: the zip never became readable")

head = git("rev-parse", "--short=9", BRANCH).stdout.strip()
bundle = HERE / "cto.bundle"
if want and head.startswith(want[:9]):
    out.append("branch already at " + head)
elif bundle.is_file():
    for attempt in range(8):
        if git("bundle", "verify", str(bundle)).returncode == 0:
            break
        time.sleep(5)
    fetched = git("fetch", str(bundle), "%s:%s" % (BRANCH, BRANCH))
    head = git("rev-parse", "--short=9", BRANCH).stdout.strip()
    out.append("branch at " + head + ("" if fetched.returncode == 0 else " (fetch said: %s)" % fetched.stderr.strip()[-200:]))
if want and not head.startswith(want[:9]):
    out.append("EXPECTED " + want)
print("; ".join(out))
