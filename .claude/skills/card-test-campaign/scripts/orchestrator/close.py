#!/usr/bin/env python3
"""The orchestrator's part of a landing, in two commands and two steps with tools between them.

Every step the orchestrator takes re-reads its whole context, so a landing is made of as few steps as it can be:

  1.  close.py <folder>
          When a pipeline's notification has come.  Brings a second-tree batch onto the branch (integrate.sh),
          marks an `at` batch done, runs postland.py (usage row, tracker, bundle, state zip), brings the second
          tree up to the branch and builds it if nothing else is at work there, and prints: what to carry, what
          to run on her machine, and what comes next with its launch arguments.  Give it ten minutes
          (`timeout 600`): the tracker and the build are slow on a cold machine.
  2.  In ONE step, together: carry the files it named (device_commit_files, force), publish the tracker (the
      page and tracker-data.js from <work>/publish/, to the same artifact), and launch the next pipeline, unless
      this turn carries a message of Ketura's (then schedule the launch a minute ahead and end the turn).
  3.  On her machine: the apply-sync.py line it printed.  It answers with the head.
  4.  close.py --launched <new folder> <run id> <a,b,c> [--tree repo|cards] --synced <head>
          Tracks the new run, takes its cards off the queues, records that the sync arrived (postland.py --mark)
          and rewrites the "Running now" block of HANDOFF.md.
      close.py --synced <head>     if nothing was launched.      close.py --now     only the block.

If step 1 says the batch could NOT be brought onto the branch, nothing else was done: read what it says
(integrate.sh's header has the three cases) and run it again when the cause is gone.
"""
import datetime
import json
import os
import re
import subprocess
import sys
from pathlib import Path

WORK = Path(os.environ.get("GEMP_WORK", "/home/claude/work"))
REPO, CARDS = WORK / "repo", WORK / "cards"
STATE = Path(os.environ.get("GEMP_CAMPAIGN_STATE", "/mnt/user-data/outputs/card-tests"))
HERE = Path(__file__).resolve().parent
BRANCH = "feature/card-test-overhaul"
TRACK = WORK / "workflows.json"
SECOND_TREE_NOTE = (
    "You work in a second working tree, /home/claude/work/cards (branch cards-wip, its own build): use only that "
    "path and never /home/claude/work/repo, where an engine pass is rebuilding the game.")
AT_NOTE = (
    " Where a mechanics class exists already and `lint.py --form` faults tests that were in it before this batch, "
    "use `--only <the batch's tests>` and leave the older tests as they are (a later fixer has them).")


def run(*cmd, cwd=REPO, timeout=900):
    return subprocess.run([str(c) for c in cmd], cwd=cwd, capture_output=True, text=True, timeout=timeout)


def tracked():
    return json.loads(TRACK.read_text()) if TRACK.exists() else {}


def tree_of(folder, t):
    return t.get("tree") or ("repo" if folder.startswith("engine-pass") else "cards")


def at_work(tree, but=None):
    return [f for f, t in tracked().items() if not t.get("landed") and f != but and tree_of(f, t) == tree]


def head(ref=BRANCH, cwd=REPO):
    return run("git", "rev-parse", "--short=9", ref, cwd=cwd).stdout.strip()


def at_queue():
    """(done keys, the next key or None) of the `at` conversion."""
    q = STATE / "orchestrator/at-convert/queue.json"
    if not q.is_file():
        return [], None
    done = run(sys.executable, HERE / "at_batches.py", "--done").stdout.strip().replace("done:", "").split()
    done = [d for d in done if d != "(none)"]
    running = [f[3:] for f in at_work("cards") + at_work("repo") if f.startswith("at-")]
    left = [k for k in json.loads(q.read_text()) if k not in done and k not in running]
    return done, (left[0] if left else None)


def card_queues():
    out = {}
    for p in sorted((STATE / "orchestrator").glob("queue-*.json")):
        try:
            out[p.stem] = json.loads(p.read_text())
        except Exception:
            out[p.stem] = []
    return out


def now_block():
    """What is running and what is waiting, from the files: the part of HANDOFF.md nobody should write by hand."""
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%MZ")
    lines = ["<!-- now:begin (written by close.py at every landing and launch: do not edit by hand) -->",
             "## Running now (%s)" % stamp, ""]
    t = tracked()
    states = {r["name"]: r for r in json.loads(run(sys.executable, HERE / "wf.py", "running").stdout.strip() or "[]")}
    for folder, v in t.items():
        if v.get("landed"):
            continue
        s = states.get(folder, {})
        of = v.get("cards", [])
        lines.append("- `%s`: run %s, the %s tree%s. %s%s" % (
            folder, v["run"], "main" if tree_of(folder, v) == "repo" else "second",
            ", launched " + v["at"] if v.get("at") else "", s.get("state", "?"),
            " (%d of %d done)" % (len(s.get("done", [])), len(of)) if folder.startswith("engine-pass") and of else ""))
    if not any(not v.get("landed") for v in t.values()):
        lines.append("- Nothing is running.")
    last = (WORK / "last_synced").read_text().strip()[:9] if (WORK / "last_synced").exists() else "?"
    ahead = run("git", "rev-list", "--count", "%s..cards-wip" % BRANCH, cwd=CARDS).stdout.strip() if CARDS.is_dir() else "0"
    lines.append("- The branch is at %s; the last head carried to Ketura is %s%s." % (
        head(), last, "" if ahead in ("", "0") else "; the second tree holds %s commit(s) the branch has not taken" % ahead))
    done, nxt = at_queue()
    if done or nxt:
        total = len(json.loads((STATE / "orchestrator/at-convert/queue.json").read_text()))
        lines.append("- The `at` conversion: %d of %d batches done; next `%s`." % (len(done), total, nxt or "none left"))
    queues = {k: v for k, v in card_queues().items() if v}
    if queues:
        lines.append("- Card queues: " + "; ".join("%s %d (next %s)" % (k, len(v), " ".join(v[:4])) for k, v in queues.items()) + ".")
    qtext = (STATE / "questions.md").read_text(encoding="utf-8") if (STATE / "questions.md").is_file() else ""
    open_part = qtext.split("## Open", 1)[-1].split("## Answered", 1)[0]
    asked = re.findall(r"^### ([QF]-\d{4})", open_part, re.M)
    by = {}
    for line in (STATE / "findings.tsv").read_text(encoding="utf-8").split("\n")[1:]:
        cells = line.split("\t")
        if len(cells) > 5:
            by.setdefault(cells[5], []).append(cells[0])
    lines.append("- Open with Ketura: %s. Findings: %s." % (
        " ".join(sorted(asked)) or "no question",
        "; ".join("%d %s%s" % (len(by[k]), k, " (" + " ".join(by[k]) + ")" if len(by[k]) <= 8 else "")
                  for k in ("candidate", "question", "confirmed", "fixed") if by.get(k)) or "none waiting"))
    parked = []
    for line in (STATE / "status.tsv").read_text(encoding="utf-8").split("\n")[1:]:
        cells = line.split("\t")
        if len(cells) > 1 and cells[1] == "parked":
            parked.append("%s (%s)" % (cells[0], (cells[2] or cells[3][:50]) if len(cells) > 3 else ""))
    lines.append("- Parked cards: %s." % (", ".join(parked) or "none"))
    props = STATE / "orchestrator/proposals.md"
    if props.is_file():
        n = sum(1 for x in props.read_text(encoding="utf-8").split("\n") if " | " in x and not x.startswith("#"))
        lines.append("- `orchestrator/proposals.md`: %d line(s) from landers and engine agents not yet gone through." % n)
    lines += ["<!-- now:end -->"]
    return "\n".join(lines)


def write_now():
    path = STATE / "HANDOFF.md"
    block = now_block()
    text = path.read_text(encoding="utf-8") if path.is_file() else "# Card test campaign: notes for the next orchestrator\n"
    if "<!-- now:begin" in text and "<!-- now:end -->" in text:
        a = text.index("<!-- now:begin")
        b = text.index("<!-- now:end -->") + len("<!-- now:end -->")
        text = text[:a] + block + text[b:]
    else:
        first, _, rest = text.partition("\n")
        text = first + "\n\n" + block + "\n" + rest
    path.write_text(text, encoding="utf-8")
    return block


def next_launch():
    """What to launch next, with its arguments, for the kinds of work that come off a queue."""
    engine = [f for f in at_work("repo") if f.startswith("engine-pass")]
    out = []
    done, nxt = at_queue()
    if nxt:
        if at_work("cards") and engine:
            out.append("next `at` batch: %s, when the second tree is free (%s is at work there)" % (nxt, ", ".join(at_work("cards"))))
        else:
            tree = CARDS if engine else REPO
            args = {"repo": str(tree), "batch": nxt, "note": ((SECOND_TREE_NOTE if engine else "") + AT_NOTE).strip()}
            out.append("next `at` batch: workflows/at-convert.js with args\n  %s\n  then: close.py --launched at-%s <run id> %s --tree %s --synced <head>" % (
                json.dumps(args), nxt, nxt, "cards" if engine else "repo"))
    for name, ids in card_queues().items():
        if ids and (nxt or [f for f in at_work("cards") + at_work("repo") if f.startswith("at-")]):
            out.append("%s: %d card(s) left, WAITING until the `at` conversion is finished (HANDOFF.md, \"Next\")" % (name, len(ids)))
        elif ids:
            out.append("%s: %d card(s) left; next %s (workflows/card-batch.js, three or four to a batch; HANDOFF.md says with which notes)" % (name, len(ids), " ".join(ids[:4])))
    if not engine:
        out.append("no engine pass has the main tree: batches may run there, two at a time (pass its path as repo)")
    return out


def mark(synced):
    pending = WORK / "pending_sync"
    want = pending.read_text().strip() if pending.exists() else ""
    if want and not want.startswith(synced) and not synced.startswith(want[:9]):
        print("NOT MARKED: her machine answered %s and the bundle made last ends at %s. Carry and apply again." % (synced, want[:9]))
        return False
    print(run(sys.executable, HERE / "postland.py", "--mark").stdout.strip().split("\n")[-1])
    return True


def main(argv):
    argv = list(argv[1:])

    def take(flag):
        if flag not in argv:
            return None
        i = argv.index(flag)
        v = argv[i + 1]
        del argv[i:i + 2]
        return v

    synced, tree = take("--synced"), take("--tree")
    if "--now" in argv:
        print(write_now())
        return 0
    if "--launched" in argv:
        argv.remove("--launched")
        if len(argv) < 2:
            print(__doc__)
            return 1
        folder, run_id = argv[0], argv[1]
        ids = argv[2] if len(argv) > 2 else folder.split("-", 1)[-1]
        print(run(sys.executable, HERE / "wf.py", "track", folder, run_id, ids,
                  tree or ("repo" if folder.startswith("engine-pass") else "cards")).stdout.strip())
        gone = ids.split(",")
        for p in sorted((STATE / "orchestrator").glob("queue-*.json")):
            q = json.loads(p.read_text())
            if any(c in gone for c in q):
                q = [c for c in q if c not in gone]
                p.write_text(json.dumps(q))
                print("%s: %d left" % (p.stem, len(q)))
    elif argv and not argv[0].startswith("-"):
        folder = argv[0]
        t = tracked().get(folder)
        if not t:
            print("%s is not a tracked run (workflows.json): close.py --launched tracks one" % folder)
            return 1
        second = tree_of(folder, t) == "cards"
        if second:
            r = run("bash", HERE / "integrate.sh")
            print((r.stdout.strip() or r.stderr.strip()[-400:]))
            if r.returncode != 0:
                print("NOT BROUGHT ONTO THE BRANCH (integrate.sh, exit %d): nothing else was done. Run close.py %s again when the cause is gone." % (r.returncode, folder))
                return r.returncode
        r = run(sys.executable, HERE / "postland.py", folder)
        keep = [x for x in r.stdout.split("\n") if x.startswith(("bundle", "state:", "carry", "then on her machine")) or "NOT" in x or "rror" in x]
        print("\n".join(keep) or (r.stdout + r.stderr)[-600:])
        print("publish: %s with files {\"tracker-data.js\": \"%s\"}" % (WORK / "publish/tracker-page.html", WORK / "publish/tracker-data.js"))
        if second and not at_work("cards", but=folder):
            r = run("bash", HERE / "integrate.sh", "--renew")
            print("second tree: " + (r.stdout.strip().split("\n") or [""])[-1][-160:])
        print("\nNext:")
        for line in next_launch():
            print("- " + line)
    elif not synced:
        print(__doc__)
        return 1
    if synced:
        mark(synced)
    print()
    print(write_now())
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
