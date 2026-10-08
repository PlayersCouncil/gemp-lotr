#!/usr/bin/env python3
"""The orchestrator's gate: checks finished cards and commits each one on its own.

    land.py <id...> [--note "<a line for every commit's body>"] [--parked] [--check]
    land.py <id...> --batch <label> [--with <path>]... [--note ...]   one commit for the whole batch

For each card, in turn: the conformance check must find no errors, the card's tests must pass (parked tests
apart), no `F-0000` or `Q-0000` placeholder may be left in the test file, and the ledger file must exist.  A card
that clears all four has its test file committed as `Card tests: <id> <name>`, and its status set to ready, or to
parked when a test is parked on a finding or question (the note says on what), or with --parked for a card that
could not be --parked also lets through coverage errors, a check no test can claim yet, and no other kind).  A card that does not is reported and left alone; the
others still land.

--check does everything except commit and set the status.

With --batch the cards that clear the gate are committed together in ONE commit, "Card tests: <label>: <ids>",
with a line for each card in its body; each --with <path> (a card definition corrected for one of these cards,
say) goes into the same commit.  This is the campaign's way since 2026-10-06: one commit for each batch, after
its review.  A card that does not clear the gate is reported and left out; the others still land.

Only the test file is committed (and, with --batch, the --with paths).  The ledger is campaign bookkeeping and stays out of git.  The lines ending
every commit message are read from the file named by $GEMP_COMMIT_TRAILER, if set.
"""
import csv
import os
import re
import subprocess
import sys

import gempcards as g

HERE = os.path.dirname(os.path.abspath(__file__))


def script(name, *args):
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE="1")
    return subprocess.run([sys.executable, os.path.join(HERE, name)] + list(args), capture_output=True, text=True, env=env)


def git(*args):
    return subprocess.run(["git"] + list(args), cwd=g.REPO, capture_output=True, text=True)


def baseline_counts():
    path = g.state_dir() / "baseline.tsv"
    if not path.is_file():
        return {}
    with open(path, encoding="utf-8", newline="") as f:
        return {r["id"]: r.get("tests", "") for r in csv.DictReader(f, delimiter="\t")}


def land(card_id, note, force_parked, check_only, before, batch=None):
    card = g.card_index().get(card_id)
    test = g.test_index().get(card_id)
    if card is None or test is None:
        return "no such card or no test file"
    name = g.full_name(card)
    source = test.read_text(encoding="utf-8")

    lint = script("lint.py", card_id)
    unclaimed = []
    if lint.returncode != 0:
        # A card parked on a rig request has checks no test can claim yet.  With --parked those coverage errors,
        # and only those, are let through and named in the commit body.
        errors = re.findall(r"^\s*error\s+(\S+)\s+line \d+\s+(.*)$", lint.stdout, re.M)
        if not (force_parked and errors and all(kind == "coverage" for kind, _ in errors)):
            return "conformance check:\n" + lint.stdout.strip()
        unclaimed = [text.strip() for _, text in errors]
    placeholder = re.search(r"\b[FQG]-0000\b", source)
    if placeholder:
        return "placeholder %s still in the test file" % placeholder.group(0)
    ledger = g.ledger_path(card_id)
    if not ledger.is_file():
        return "no ledger file at %s" % ledger
    ledger_text = ledger.read_text(encoding="utf-8")
    placeholder = re.search(r"\b[FQG]-0000\b", ledger_text)
    if placeholder:
        return "placeholder %s still in the ledger file" % placeholder.group(0)
    run = script("cardtest.py", "run", card_id, "--out", "land")
    ok = re.search(r"^OK \((\d+) tests?\)", run.stdout, re.M)
    if not ok:
        return "tests:\n" + "\n".join(run.stdout.strip().split("\n")[-25:])

    tests = len(re.findall(r"^\s*@Test\b", source, re.M))
    parked = re.findall(r'@Ignore\("([FQG]-\d{4})', source)
    # A check the rig cannot show yet ("waits <check>: G-0029 ...") parks the card as a parked test does (Ketura,
    # 2026-10-08: a card held by a rig request does not move on as ready).
    waiting = re.findall(r"^\s+waits\s+[^:\n]+:\s*(G-\d{4})", ledger_text, re.M)
    clauses = len(re.findall(r"^[PCR]\d+\s", ledger_text, re.M))
    was = before.get(card_id, "")
    body = "%d tests over the %d clauses of its ledger" % (tests, clauses)
    if was not in ("", str(tests)):
        body += " (%s before)" % was
    body += "."
    if parked:
        body += " Parked on %s." % ", ".join(sorted(set(parked)))
    if waiting:
        body += " Waiting on %s." % ", ".join(sorted(set(waiting)))
    if unclaimed:
        body += " Not finished: %s." % "; ".join(unclaimed)
    if note:
        body += "\n\n" + note
    # A card with a test parked on a finding or question lands as "parked" (Ketura, 2026-10-08: the tracker is to
    # show such cards as parked); the note says what is parked.  --parked also lands a card that could not be finished.
    status = "parked" if (force_parked or parked or waiting) else "ready"
    parked = parked + waiting
    if check_only:
        return "would land as %s: %s" % (status, body.split("\n")[0])

    rel = g.rel(test)
    if batch is not None:
        # The commit and the status are the caller's, once every card of the batch has been through the gate.
        batch.append({"id": card_id, "name": name, "rel": rel, "body": body, "status": status, "parked": parked,
                      "force_parked": force_parked})
        return "would land as %s: %s" % (status, body.split("\n")[0])
    git("add", "--", rel)
    if not git("diff", "--cached", "--quiet", "--", rel).returncode:
        outcome = "test file unchanged, nothing to commit"
    else:
        message = "Card tests: %s %s\n\n%s\n" % (card_id, name, body)
        trailer = os.environ.get("GEMP_COMMIT_TRAILER")
        if trailer and os.path.isfile(trailer):
            message += "\n" + open(trailer, encoding="utf-8").read().strip() + "\n"
        done = git("commit", "-q", "-m", message, "--", rel)
        if done.returncode != 0:
            return "git: " + (done.stderr or done.stdout).strip()
        outcome = "committed " + git("rev-parse", "--short=9", "HEAD").stdout.strip()
    args = ["set", status, card_id]
    if not parked and not force_parked:
        args += ["--note", ""]  # a note left from an earlier landing ("1 test(s) parked on ...") no longer holds
    if parked:
        args += ["--note", "%d test(s) or check(s) parked on %s" % (len(parked), ", ".join(sorted(set(parked))))]
        for found in sorted(set(parked)):
            if found.startswith("F-"):
                args += ["--finding", found]
                break
    script("tracker.py", *args)
    return "%s, %s: %s" % (outcome, status, body.split("\n")[0])


def set_status(entry):
    args = ["set", entry["status"], entry["id"]]
    parked = entry["parked"]
    if not parked and not entry["force_parked"]:
        args += ["--note", ""]
    if parked:
        args += ["--note", "%d test(s) or check(s) parked on %s" % (len(parked), ", ".join(sorted(set(parked))))]
        for found in sorted(set(parked)):
            if found.startswith("F-"):
                args += ["--finding", found]
                break
    script("tracker.py", *args)


def commit_batch(label, entries, extra, note):
    """One commit for every card of the batch that cleared the gate, with the extra paths."""
    paths = [e["rel"] for e in entries] + extra
    git("add", "--", *paths)
    if not git("diff", "--cached", "--quiet", "--", *paths).returncode:
        outcome = "nothing to commit"
    else:
        message = "Card tests: %s: %s\n\n" % (label, ", ".join(e["id"] for e in entries))
        for e in entries:
            message += "%s %s: %s\n" % (e["id"], e["name"], e["body"].split("\n")[0])
        if extra:
            message += "\nWith: " + ", ".join(os.path.basename(x) for x in extra) + "\n"
        if note:
            message += "\n" + note + "\n"
        trailer = os.environ.get("GEMP_COMMIT_TRAILER")
        if trailer and os.path.isfile(trailer):
            message += "\n" + open(trailer, encoding="utf-8").read().strip() + "\n"
        done = git("commit", "-q", "-m", message, "--", *paths)
        if done.returncode != 0:
            return "git: " + (done.stderr or done.stdout).strip()
        outcome = "committed " + git("rev-parse", "--short=9", "HEAD").stdout.strip()
    for e in entries:
        set_status(e)
    return outcome


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    args = argv[1:]
    note = None
    if "--note" in args:
        i = args.index("--note")
        note = args[i + 1]
        args = args[:i] + args[i + 2:]
    label = None
    if "--batch" in args:
        i = args.index("--batch")
        label = args[i + 1]
        args = args[:i] + args[i + 2:]
    extra = []
    while "--with" in args:
        i = args.index("--with")
        extra.append(args[i + 1])
        args = args[:i] + args[i + 2:]
    force_parked = "--parked" in args
    check_only = "--check" in args
    ids = [a for a in args if not a.startswith("--")]
    before = baseline_counts()
    failed = 0
    entries = [] if (label and not check_only) else None
    for card_id in ids:
        result = land(card_id, None if entries is not None else note, force_parked, check_only, before, entries)
        held = not result.startswith(("committed", "test file unchanged", "would land"))
        failed += held
        word = "HELD  " if held else "CHECK " if check_only else "GATE  " if entries is not None else "LANDED"
        print("%s %s: %s" % (word, card_id, result))
    if entries:
        print("BATCH  %s: %s" % (label, commit_batch(label, entries, extra, note)))
    return 1 if failed else 0


if __name__ == "__main__":
    with g.locked():
        code = main(sys.argv)
    sys.exit(code)
