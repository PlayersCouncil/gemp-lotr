#!/usr/bin/env python3
"""What the orchestrator runs when a batch's lander has finished: everything that is left, in one step.

    postland.py [<folder>...]   for each folder: its usage row and the mark that it has landed; then the tracker's
                                data, the bundle of new commits and one zip of every state file that has changed.
                                Prints the two files to carry to Ketura's machine and where they go.
    postland.py --mark          once apply-sync.py on her machine has answered with the new head: remembers that
                                the bundle and the zip arrived, so the next ones hold only what is newer.

Content hashes decide what has changed, not times: the outputs folder's modification times cannot be trusted.
"""
import hashlib, json, os, subprocess, sys, zipfile
from pathlib import Path

WORK = Path(os.environ.get("GEMP_WORK", "/home/claude/work"))
REPO = WORK / "repo"
STATE = Path(os.environ.get("GEMP_CAMPAIGN_STATE", "/mnt/user-data/outputs/card-tests"))
SCRIPTS = REPO / ".claude/skills/card-test-campaign/scripts"
BRANCH = "feature/card-test-overhaul"
DEVICE = "X:\\Projects\\gemp-lotr\\Claude outputs\\card-tests\\transport\\"
SKIP = {"tracker.json", "tracker-page.html", ".lock"}


def run(*cmd, cwd=REPO):
    return subprocess.run(list(cmd), cwd=cwd, capture_output=True, text=True)


def digest(path):
    return hashlib.sha1(path.read_bytes()).hexdigest()


def mark():
    seen_file, listed_file = WORK / "state_hashes.json", WORK / "last_listed.json"
    seen = json.loads(seen_file.read_text()) if seen_file.exists() else {}
    listed = json.loads(listed_file.read_text()) if listed_file.exists() else {}
    seen.update(listed)
    seen_file.write_text(json.dumps(seen))
    if (WORK / "pending_sync").exists():
        os.replace(WORK / "pending_sync", WORK / "last_synced")
    print("marked %d state file(s); last synced %s" % (len(listed), (WORK / "last_synced").read_text().strip()[:9]))


def main(argv):
    if "--mark" in argv:
        return mark()
    folders = [a for a in argv[1:] if not a.startswith("--")]
    tracked_file = WORK / "workflows.json"
    tracked = json.loads(tracked_file.read_text()) if tracked_file.exists() else {}
    for folder in folders:
        if folder in tracked and not tracked[folder].get("landed"):
            row = run(sys.executable, str(SCRIPTS / "orchestrator/wf.py"), "usage", folder).stdout.strip()
            if row:
                with open(STATE / "usage.tsv", "a", encoding="utf-8") as f:
                    f.write(row + "\n")
            run(sys.executable, str(SCRIPTS / "orchestrator/wf.py"), "landed", folder)

    built = run(sys.executable, str(SCRIPTS / "tracker.py"), "html", str(STATE / "tracker.html"))
    print((built.stdout.strip().split("\n") or [""])[-1] or built.stderr.strip()[-300:])
    # The published page is sent from the working folder: the page (unchanged between landings) and its data.
    publish = WORK / "publish"
    publish.mkdir(exist_ok=True)
    for name in ("tracker-page.html", "tracker-data.js"):
        if (STATE / name).is_file():
            (publish / name).write_bytes((STATE / name).read_bytes())

    transport = STATE / "transport"
    transport.mkdir(parents=True, exist_ok=True)
    last = (WORK / "last_synced").read_text().strip() if (WORK / "last_synced").exists() else "scenario/authoring-4"
    head = run("git", "rev-parse", BRANCH).stdout.strip()
    carry = []
    if run("git", "rev-parse", last).stdout.strip() != head:
        made = run("git", "bundle", "create", "-q", str(transport / "cto.bundle"), "%s..%s" % (last, BRANCH))
        if made.returncode != 0:
            print("bundle: " + made.stderr.strip())
            return 1
        (WORK / "pending_sync").write_text(head + "\n")
        count = run("git", "rev-list", "--count", "%s..%s" % (last, BRANCH)).stdout.strip()
        print("bundle: %s commit(s), head %s" % (count, head[:9]))
        carry.append("cto.bundle")
    else:
        print("bundle: nothing new (head %s)" % head[:9])

    seen_file = WORK / "state_hashes.json"
    seen = json.loads(seen_file.read_text()) if seen_file.exists() else {}
    listed = {}
    with zipfile.ZipFile(transport / "state-delta.zip", "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr(".sync-head", head)   # apply-sync.py waits for the zip that belongs to this head
        for p in sorted(STATE.rglob("*")):
            if not p.is_file() or "transport" in p.parts or p.name in SKIP:
                continue
            rel = "/".join(p.relative_to(STATE).parts)
            d = digest(p)
            if seen.get(rel) != d:
                listed[rel] = d
                z.write(p, rel)
    print("state: %d changed file(s) in state-delta.zip" % len(listed))
    carry.append("state-delta.zip")
    # The script that applies all this on her machine travels with it whenever it has changed.
    applier = SCRIPTS / "orchestrator/apply-sync.py"
    if seen.get("transport/apply-sync.py") != digest(applier):
        (transport / "apply-sync.py").write_bytes(applier.read_bytes())
        listed["transport/apply-sync.py"] = digest(applier)
        carry.append("apply-sync.py")
    (WORK / "last_listed.json").write_text(json.dumps(listed))
    # The outputs folder writes through to its store some seconds after a file is closed, and the tool that carries
    # files to her machine reads the store: carried at once, the PREVIOUS zip or bundle goes across.  apply-sync.py
    # notices (it says NOT UNPACKED, or EXPECTED): carry the files again.
    import time
    time.sleep(12)
    print("carry (device_commit_files, force): " + json.dumps(
        [{"stagedPath": str(transport / name), "devicePath": DEVICE + name} for name in carry]))
    print("then on her machine: python3 \"$HOME/mnt/gemp-lotr/Claude outputs/card-tests/transport/apply-sync.py\" %s" % head[:9])


if __name__ == "__main__":
    sys.exit(main(sys.argv) or 0)
