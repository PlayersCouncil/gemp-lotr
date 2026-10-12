#!/usr/bin/env python3
"""Runs on Ketura's machine, from Claude outputs/card-tests/transport/: fetches cto.bundle into
feature/card-test-overhaul (moves the branch ref and nothing else: no checkout, no pull, no push, no status), and
brings `Claude outputs/state-cache` (the campaign's state, a git working copy only Claude writes; branch `state`) up
to state.bundle.  An older state-delta.zip, if one came, is unpacked over the card-tests folder as before.  Prints
one line.

    apply-sync.py [<expected head> [<expected state head>]]

Files written from the cloud take some seconds to arrive, and until they do the old ones are still here: given the
heads, this waits (up to two minutes) for the bundles that reach them.
"""
import subprocess, sys, time, zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
STATE = HERE.parent
REPO = STATE.parent.parent
CACHE = STATE.parent / "state-cache"
BRANCH = "feature/card-test-overhaul"
want = (sys.argv[1] if len(sys.argv) > 1 else "")[:9]
want_state = (sys.argv[2] if len(sys.argv) > 2 else "")[:7]
deadline = time.time() + 120


def git(*args, cwd=REPO):
    return subprocess.run(["git"] + list(args), cwd=cwd, capture_output=True, text=True)


def unpack():
    delta = HERE / "state-delta.zip"
    if not delta.is_file():
        return None
    try:
        with zipfile.ZipFile(delta) as z:
            names = [n for n in z.namelist() if n != ".sync-head"]
            made_for = z.read(".sync-head").decode().strip() if ".sync-head" in z.namelist() else ""
            if want and not made_for.startswith(want):
                return None   # an old zip: the state goes by the state bundle now
            for name in names:
                target = STATE / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(z.read(name))
            delta.unlink()
            return "zip: %d file(s)" % len(names)
    except (zipfile.BadZipFile, KeyError):
        return None


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


def state():
    bundle = HERE / "state.bundle"
    while True:
        have = git("rev-parse", "--short=7", "state", cwd=CACHE).stdout.strip() if (CACHE / ".git").is_dir() else ""
        if want_state and have.startswith(want_state):
            return "state at " + have
        said = ""
        if bundle.is_file():
            if not (CACHE / ".git").is_dir():
                made = git("clone", "-q", "--branch", "state", str(bundle), str(CACHE), cwd=STATE.parent)
                said = made.stderr.strip()[-160:]
            elif git("bundle", "verify", str(bundle), cwd=CACHE).returncode == 0:
                git("fetch", "-q", str(bundle), "+state:refs/remotes/origin/state", cwd=CACHE)
                git("reset", "-q", "--hard", "origin/state", cwd=CACHE)
            have = git("rev-parse", "--short=7", "state", cwd=CACHE).stdout.strip() if (CACHE / ".git").is_dir() else ""
            if not want_state or have.startswith(want_state):
                return "state at " + (have or "?")
        if time.time() > deadline:
            return "state at %s, EXPECTED %s %s" % (have or "nothing", want_state or "?", said)
        time.sleep(5)


print("; ".join(x for x in [unpack(), state(), fetch()] if x))
