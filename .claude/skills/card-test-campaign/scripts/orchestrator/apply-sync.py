#!/usr/bin/env python3
"""Runs on Ketura's machine, from Claude outputs/card-tests/transport/: unpacks state-delta.zip over the card-tests
folder and fetches cto.bundle into feature/card-test-overhaul.  Moves the branch ref and nothing else: no checkout,
no pull, no push, no status.  Prints one line.

    apply-sync.py [<expected head>]

Files written from the cloud take some seconds to arrive, and until they do the old ones are still here: given the
head, this waits (up to two minutes) for the zip that names that head and for the bundle that reaches it.
"""
import subprocess, sys, time, zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
STATE = HERE.parent
REPO = STATE.parent.parent
BRANCH = "feature/card-test-overhaul"
want = (sys.argv[1] if len(sys.argv) > 1 else "")[:9]
deadline = time.time() + 120


def git(*args):
    return subprocess.run(["git"] + list(args), cwd=REPO, capture_output=True, text=True)


def unpack():
    delta = HERE / "state-delta.zip"
    while True:
        try:
            with zipfile.ZipFile(delta) as z:
                names = [n for n in z.namelist() if n != ".sync-head"]
                made_for = z.read(".sync-head").decode().strip() if ".sync-head" in z.namelist() else ""
                if not want or made_for.startswith(want):
                    for name in names:
                        target = STATE / name
                        target.parent.mkdir(parents=True, exist_ok=True)
                        target.write_bytes(z.read(name))  # written over in place: nothing is deleted
                    return "state: %d file(s)" % len(names)
        except (zipfile.BadZipFile, FileNotFoundError, KeyError):
            pass
        if time.time() > deadline:
            return "state: NOT UNPACKED (the zip for %s never arrived)" % (want or "this sync")
        time.sleep(5)


def fetch():
    bundle = HERE / "cto.bundle"
    while True:
        head = git("rev-parse", "--short=9", BRANCH).stdout.strip()
        if want and head.startswith(want):
            return "branch at " + head
        said = ""
        if bundle.is_file() and git("bundle", "verify", str(bundle)).returncode == 0:
            fetched = git("fetch", str(bundle), "%s:%s" % (BRANCH, BRANCH))
            said = "" if fetched.returncode == 0 else fetched.stderr.strip()[-160:]
            head = git("rev-parse", "--short=9", BRANCH).stdout.strip()
            if not want or head.startswith(want):
                return "branch at " + head
        if time.time() > deadline:
            return "branch at %s, EXPECTED %s %s" % (head, want, said)
        time.sleep(5)


print("; ".join([unpack(), fetch()]))
