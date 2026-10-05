#!/usr/bin/env python3
"""Works out where the campaign stands and writes the progress tracker.

    tracker.py baseline             records what every card's tests looked like before the campaign touched them
    tracker.py build                writes tracker.json and tracker.md from the baseline, the status file and a lint run
    tracker.py html <out.html>      writes the tracker page (the template with the data put in)
    tracker.py set <status> <ids..> [--note "..."] [--finding F-0001]
                                    records a card's status: todo, in-progress, ready, parked, approved
    tracker.py focus <phase> [<set>]     says which part of the campaign is being worked on, for the page to open at
    tracker.py finding add <card> <kind> <status> "<summary>" [--found "..."] [--outcome "..."]
    tracker.py finding set <F-0000> <status> [--outcome "..."] [--kind <kind>]
                                    kinds: hjson-typo hjson-logic gametext engine rig test sheet ruling data
                                    statuses: candidate question confirmed fixed not-a-bug

The campaign's state lives in one folder (--state <dir>, or $GEMP_CAMPAIGN_STATE, or "Claude outputs/card-tests"
under the repository): baseline.tsv, status.tsv, findings.tsv, focus.txt.  It is kept out of git on purpose: it is
bookkeeping about the work, not part of it.
"""
import csv
import datetime
import json
import os
import re
import sys
from pathlib import Path

import gempcards as g

STATUSES = ["todo", "in-progress", "ready", "parked", "approved"]

PHASES = [
    ("audit", "Existing behavioural tests brought up to the standard"),
    ("vset", "PC V-set cards that only have a stat check"),
    ("errata", "PC errata that only have a stat check, or no test"),
    ("decipher", "Decipher cards, one set and culture at a time"),
]


def state_dir(argv) -> Path:
    if "--state" in argv:
        return Path(argv[argv.index("--state") + 1])
    return g.state_dir()


def active_tests(text: str) -> int:
    return len(re.findall(r"^\s*@Test\b", text, re.M))


def group_of(card_id: str, card) -> str:
    """The unit a set breaks into: its culture file."""
    stem = card.culture_file
    stem = re.sub(r"^set\d+-", "", stem, flags=re.I)
    stem = re.sub(r"^V\d+[-_]", "", stem, flags=re.I)
    stem = re.sub(r"-errata$", "", stem, flags=re.I)
    stem = re.sub(r"^\d+-rtmd-", "", stem, flags=re.I)
    return stem[:1].upper() + stem[1:]


def baseline(state: Path):
    cards, tests = g.card_index(), g.test_index()
    rows = []
    for card_id in sorted(cards, key=g.id_sort_key):
        seg = g.segment_of(card_id)
        if seg in ("hobbit", "other") or g.split_id(card_id)[0] == 90:
            continue  # no text to test yet (Hobbit), or nothing but a picture (set 90)
        card = cards[card_id]
        test = tests.get(card_id)
        n = active_tests(test.read_text(encoding="utf-8", errors="replace")) if test else 0
        kind = "none" if not test else ("behavioural" if n > 1 else ("stat-only" if n == 1 else "empty"))
        if kind == "behavioural":
            phase = "audit"
        elif seg == "vset":
            phase = "vset"
        elif seg == "errata":
            phase = "errata"
        elif seg == "rtmd":
            phase = "audit"
        else:
            phase = "decipher"
        rows.append([card_id, g.full_name(card), seg, g.set_label(card_id), group_of(card_id, card), phase, kind, str(n)])
    state.mkdir(parents=True, exist_ok=True)
    with open(state / "baseline.tsv", "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter="\t", lineterminator="\n")
        w.writerow(["id", "name", "segment", "set", "group", "phase", "kind", "tests"])
        w.writerows(rows)
    print("baseline.tsv: %d cards" % len(rows))


def flat(value) -> str:
    """A value as one cell of a tab-separated file: no tabs, no line breaks, and nothing escaped (escaping is
    what once turned a single quote mark into a longer run of backslashes every time the file was rewritten)."""
    return str(value if value is not None else "").replace("\t", " ").replace("\r", " ").replace("\n", " ")


def read_tsv(path: Path) -> list:
    if not path.is_file():
        return []
    lines = path.read_text(encoding="utf-8").split("\n")
    header = lines[0].split("\t")
    return [dict(zip(header, line.split("\t") + [""] * len(header))) for line in lines[1:] if line.strip()]


def read_status(state: Path) -> dict:
    return {r["id"]: r for r in read_tsv(state / "status.tsv")}


def write_status(state: Path, status: dict):
    lines = ["\t".join(["id", "status", "findings", "note", "updated"])]
    for card_id in sorted(status, key=g.id_sort_key):
        r = status[card_id]
        lines.append("\t".join(flat(v) for v in [card_id, r["status"], r.get("findings", ""), r.get("note", ""), r.get("updated", "")]))
    (state / "status.tsv").write_text("\n".join(lines) + "\n", encoding="utf-8")


def set_status(state: Path, argv):
    status_name = argv[0]
    if status_name not in STATUSES:
        raise SystemExit("Status must be one of: " + ", ".join(STATUSES))
    note = argv[argv.index("--note") + 1] if "--note" in argv else None
    finding = argv[argv.index("--finding") + 1] if "--finding" in argv else None
    skip = set()
    for flag in ("--note", "--finding", "--state"):
        if flag in argv:
            i = argv.index(flag)
            skip.update((i, i + 1))
    ids = g.resolve_ids([a for i, a in enumerate(argv[1:], 1) if i not in skip])
    status = read_status(state)
    today = datetime.date.today().isoformat()
    for card_id in ids:
        r = status.get(card_id, {"id": card_id, "findings": "", "note": ""})
        r["status"] = status_name
        r["updated"] = today
        if note is not None:
            r["note"] = note.replace("\t", " ").replace("\n", " ")
        if finding:
            have = [f for f in r.get("findings", "").split(",") if f]
            if finding not in have:
                have.append(finding)
            r["findings"] = ",".join(have)
        status[card_id] = r
    write_status(state, status)
    print("%d card(s) now %s" % (len(ids), status_name))


FINDING_FIELDS = ["id", "date", "card", "name", "kind", "status", "summary", "found", "outcome"]
FINDING_KINDS = ["hjson-typo", "hjson-logic", "gametext", "engine", "rig", "test", "sheet", "ruling", "data"]
FINDING_STATUSES = ["candidate", "question", "confirmed", "fixed", "not-a-bug"]


def write_findings(state: Path, rows: list):
    lines = ["\t".join(FINDING_FIELDS)]
    for r in rows:
        lines.append("\t".join(flat(r.get(k, "")) for k in FINDING_FIELDS))
    (state / "findings.tsv").write_text("\n".join(lines) + "\n", encoding="utf-8")


def option(argv, flag, default=""):
    return argv[argv.index(flag) + 1] if flag in argv else default


def finding(state: Path, argv):
    """finding add <card> <kind> <status> "<summary>" [--found "..."] [--outcome "..."]
       finding set <F-0000> <status> [--outcome "..."] [--kind <kind>]"""
    rows = read_tsv(state / "findings.tsv")
    if argv[0] == "add":
        card_id, kind, status, summary = argv[1], argv[2], argv[3], argv[4]
        if kind not in FINDING_KINDS:
            raise SystemExit("Kind must be one of: " + ", ".join(FINDING_KINDS))
        if status not in FINDING_STATUSES:
            raise SystemExit("Status must be one of: " + ", ".join(FINDING_STATUSES))
        card = g.card_index().get(card_id)
        number = 1 + max([int(r["id"][2:]) for r in rows] or [0])
        row = {"id": "F-%04d" % number, "date": datetime.date.today().isoformat(), "card": card_id,
               "name": g.full_name(card) if card else "", "kind": kind, "status": status, "summary": summary,
               "found": option(argv, "--found"), "outcome": option(argv, "--outcome")}
        rows.append(row)
        write_findings(state, rows)
        print(row["id"])
    elif argv[0] == "set":
        hit = [r for r in rows if r["id"] == argv[1]]
        if not hit:
            raise SystemExit("No finding " + argv[1])
        if argv[2] not in FINDING_STATUSES:
            raise SystemExit("Status must be one of: " + ", ".join(FINDING_STATUSES))
        hit[0]["status"] = argv[2]
        if "--outcome" in argv:
            hit[0]["outcome"] = option(argv, "--outcome")
        if "--kind" in argv:
            hit[0]["kind"] = option(argv, "--kind")
        write_findings(state, rows)
        print("%s is now %s" % (argv[1], argv[2]))
    else:
        raise SystemExit(finding.__doc__)


def build(state: Path) -> dict:
    base = read_tsv(state / "baseline.tsv")
    if not base:
        raise SystemExit("No baseline.  Run: tracker.py baseline")
    status = read_status(state)
    findings = read_tsv(state / "findings.tsv")
    focus = (state / "focus.txt").read_text(encoding="utf-8").strip().split("\t") if (state / "focus.txt").is_file() else []
    tests = g.test_index()
    import lint
    cards_out = []
    for r in base:
        card_id = r["id"]
        test = tests.get(card_id)
        now_tests, passes = 0, False
        if test:
            now_tests = active_tests(test.read_text(encoding="utf-8", errors="replace"))
            problems, _ = lint.lint_file(test)
            passes = not any(p.level == "error" for p in problems)
        s = status.get(card_id, {})
        cards_out.append({
            "id": card_id, "name": r["name"], "set": r["set"], "group": r["group"], "phase": r["phase"],
            "kind": r["kind"], "was": int(r["tests"]), "now": now_tests, "lint": passes,
            "status": s.get("status", "todo"), "findings": s.get("findings", ""), "note": s.get("note", ""),
        })
    phase_order = {k: n for n, (k, _) in enumerate(PHASES)}
    # Within the audit, the PC cards come first: their tests are the ones the standard was drawn from.
    segment_order = {"vset": 0, "errata": 1, "rtmd": 2, "decipher": 3}
    cards_out.sort(key=lambda c: (phase_order[c["phase"]], segment_order[g.segment_of(c["id"])], g.id_sort_key(c["id"])))
    at_dir = g.TEST_ROOT / "at"
    legacy = []
    if at_dir.is_dir():
        for p in sorted(at_dir.glob("*.java")):
            n = active_tests(p.read_text(encoding="utf-8", errors="replace"))
            if n:
                legacy.append({"file": p.name, "tests": n})
    data = {
        "generated": datetime.datetime.now().strftime("%Y-%m-%d %H:%M"),
        "phases": [{"key": k, "title": t} for k, t in PHASES],
        "focus": focus,
        "cards": cards_out,
        "legacy": legacy,
        "dormant": len(g.dormant_tests()),
        "findings": findings,
    }
    (state / "tracker.json").write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")
    (state / "tracker.md").write_text(markdown(data), encoding="utf-8")
    return data


def markdown(data: dict) -> str:
    lines = ["# Card test campaign: where things stand", "", "Generated " + data["generated"], "",
             "| Part | Cards | To do | In progress | Ready for review | Parked | Approved |",
             "|---|---|---|---|---|---|---|"]

    def counts(cards):
        c = {s: 0 for s in STATUSES}
        for card in cards:
            c[card["status"]] += 1
        return c

    for phase in data["phases"]:
        cards = [c for c in data["cards"] if c["phase"] == phase["key"]]
        c = counts(cards)
        lines.append("| %s | %d | %d | %d | %d | %d | %d |" % (phase["title"], len(cards), c["todo"], c["in-progress"],
                                                               c["ready"], c["parked"], c["approved"]))
        if data["focus"] and data["focus"][0] == phase["key"]:
            sets = []
            for card in cards:
                if card["set"] not in sets:
                    sets.append(card["set"])
            for s in sets:
                sc = [x for x in cards if x["set"] == s]
                c = counts(sc)
                lines.append("| &nbsp;&nbsp;&nbsp;%s | %d | %d | %d | %d | %d | %d |" % (s, len(sc), c["todo"], c["in-progress"],
                                                                                      c["ready"], c["parked"], c["approved"]))
    lines.append("| Legacy `at/` tests to convert | %d tests in %d files | | | | | |"
                 % (sum(l["tests"] for l in data["legacy"]), len(data["legacy"])))
    lines.append("| Dormant test files for earlier card iterations | %d files | | | | | |" % data["dormant"])
    lines.append("")
    if len(data["focus"]) > 1:
        phase, group = data["focus"][0], data["focus"][1]
        cards = [c for c in data["cards"] if c["phase"] == phase and c["set"] == group and c["status"] != "approved"]
        if cards:
            lines += ["## %s: cards not yet approved" % group, "", "| Card | Name | Status | Tests | Standard | Findings | Note |",
                      "|---|---|---|---|---|---|---|"]
            for c in cards:
                lines.append("| %s | %s | %s | %d → %d | %s | %s | %s |" % (c["id"], c["name"], c["status"], c["was"], c["now"],
                                                                           "meets" if c["lint"] else "not yet", c["findings"], c["note"]))
            lines.append("")
    if data["findings"]:
        lines += ["## Findings", "", "| Id | Card | Kind | Status | Summary |", "|---|---|---|---|---|"]
        for f in data["findings"]:
            lines.append("| %s | %s %s | %s | %s | %s |" % (f.get("id", ""), f.get("card", ""), f.get("name", ""), f.get("kind", ""),
                                                           f.get("status", ""), f.get("summary", "")))
        lines.append("")
    return "\n".join(lines)


def html(state: Path, out: Path):
    data = build(state)
    template = (Path(__file__).resolve().parent.parent / "assets" / "tracker_template.html").read_text(encoding="utf-8")
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    out.write_text(template.replace("/*__DATA__*/null", payload), encoding="utf-8")
    print("wrote %s (%d cards, %d findings)" % (out, len(data["cards"]), len(data["findings"])))


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    state = state_dir(argv)
    cmd = argv[1]
    if cmd == "baseline":
        baseline(state)
    elif cmd == "build":
        data = build(state)
        print("tracker.json and tracker.md written to %s" % state)
        print(markdown(data).split("\n## ")[0])
    elif cmd == "html":
        html(state, Path(argv[2]))
    elif cmd == "set":
        set_status(state, argv[2:])
    elif cmd == "finding":
        args = list(argv[2:])
        if "--state" in args:
            i = args.index("--state")
            del args[i:i + 2]
        finding(state, args)
    elif cmd == "focus":
        parts = [a for a in argv[2:] if not a.startswith("--") and a != str(state)]
        (state / "focus.txt").write_text("\t".join(parts[:2]), encoding="utf-8")
        print("focus: " + " / ".join(parts[:2]))
    else:
        print(__doc__)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
