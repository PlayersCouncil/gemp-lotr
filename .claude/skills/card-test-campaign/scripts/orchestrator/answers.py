#!/usr/bin/env python3
"""Files a set of Ketura's answers into the registers, in one step.

    answers.py <answers file> [--set F-0313=fixed-approved,F-0217=not-a-bug] [--date 2026-10-10] [--dry]
    answers.py --approve-fixed-below F-0327 --words "<her words>" [--date ...] [--dry]

The answers file is her words exactly as she sent them, saved first (before anything else is done with them) as
`<state>/orchestrator/answers/<date>-<letter>.md`: one entry to a number, `Q-0206: <her words>`, running on over the
lines that follow until a blank line or the next number.  Lines that carry no number (a heading, a remark about
something else) are left alone: they are yours to act on.  The date is taken from the file's name.

For each number:
  Q-0000   its entry in questions.md gets `**Ketura (<date>):** <her words>` and moves from Open to the top of
           Answered.  (An entry already under Answered gets the words added, once: a second word on a settled thing.)
  F-0000   the same, if the finding has an entry of its own in questions.md (a candidate she was asked about); and
           its row in findings.tsv gets `Ketura (<date>): <her words>` in front of its outcome, which is what takes
           it off her Waiting page.  Its STATUS changes only if --set names it: whether "good to fix" makes a
           finding `confirmed` or her "keep" makes it `fixed-approved` is read from her words, by you.
  G-0000   nothing is written: a rig request is approved by queueing it.  It is listed.

What it prints at the end is what is still yours: findings whose status was not named; findings an answered
question names in its heading ("(with F-0308)"; Ketura, 2026-10-09: "if there is a Q for an F of the same issue, I only need to see the Q (the F
can be given the same approval status as the Q)": set them); numbers the rulings log does not mention yet (an
answer is a ruling for every later card: write it into references/rulings-log.md under its subject, in her words,
then `lookup.py --index`); and the work each answer asks for, which goes into a queue (an engine pass's file, a
fixer's list), never into the air.
"""
import datetime
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gempcards as g          # noqa: E402
import tracker                 # noqa: E402

REFERENCES = Path(__file__).resolve().parent.parent.parent / "references"
NUMBER = re.compile(r"^([QFG]-\d{4})\s*:\s*(.*)$")


def take(argv, flag, default=None):
    if flag not in argv:
        return default
    i = argv.index(flag)
    value = argv[i + 1]
    del argv[i:i + 2]
    return value


def parse(text: str) -> list:
    """[(number, words)] in the file's order.  An entry runs to a blank line or the next number."""
    out, cur = [], None
    for line in text.split("\n"):
        m = NUMBER.match(line.strip())
        if m:
            cur = [m.group(1), m.group(2).strip()]
            out.append(cur)
        elif not line.strip() or line.lstrip().startswith(("#", "(")):
            cur = None
        elif cur is not None:
            cur[1] += " " + line.strip()
    return [(n, w) for n, w in out]


def split_register(text: str):
    """questions.md as (before Open, the Open entries, between, the Answered entries, after): entries are the
    `### ` sections under each heading, kept word for word."""
    m_open = re.search(r"^## Open[^\n]*\n", text, re.M)
    m_ans = re.search(r"^## Answered[^\n]*\n", text, re.M)
    if not m_open or not m_ans or m_ans.start() < m_open.end():
        raise SystemExit("questions.md wants '## Open' and then '## Answered'")
    after_at = re.search(r"^## ", text[m_ans.end():], re.M)
    end = m_ans.end() + after_at.start() if after_at else len(text)

    def entries(block):
        parts = re.split(r"(?m)^(?=### )", block)
        return parts[0], [p for p in parts[1:]]

    open_lead, open_entries = entries(text[m_open.end():m_ans.start()])
    ans_lead, ans_entries = entries(text[m_ans.end():end])
    return (text[:m_open.end()], open_lead, open_entries, text[m_ans.start():m_ans.end()], ans_lead, ans_entries,
            text[end:])


def number_of(entry: str) -> str:
    m = re.match(r"### ([QFG]-\d{4})\b", entry)
    return m.group(1) if m else ""


def main(argv):
    argv = list(argv[1:])
    dry = "--dry" in argv
    if dry:
        argv.remove("--dry")
    date = take(argv, "--date")
    below = take(argv, "--approve-fixed-below")
    words_for_all = take(argv, "--words")
    wanted = {}
    for pair in (take(argv, "--set") or "").split(","):
        if pair.strip():
            number, _, status = pair.strip().partition("=")
            if status not in tracker.FINDING_STATUSES:
                raise SystemExit("%s: status must be one of %s" % (pair, " ".join(tracker.FINDING_STATUSES)))
            wanted[number] = status
    if not below and (len(argv) != 1 or argv[0].startswith("-")):
        print(__doc__)
        return 1
    state = g.state_dir()
    answers = []
    if not below:
        source = Path(argv[0])
        answers = parse(source.read_text(encoding="utf-8"))
        if not date:
            m = re.search(r"\d{4}-\d{2}-\d{2}", source.name)
            date = m.group(0) if m else None
        if not answers:
            raise SystemExit("no line of %s begins with a number and a colon (Q-0206: ...)" % source)
    date = date or datetime.date.today().isoformat()
    said = "Ketura (%s)" % date

    with g.locked():
        rows = tracker.read_tsv(state / "findings.tsv")
        by_id = {r["id"]: r for r in rows}
        qpath = state / "questions.md"
        head, open_lead, opens, ans_head, ans_lead, answered, tail = split_register(qpath.read_text(encoding="utf-8"))
        report, unset, linked, rig = [], [], [], []

        if below:
            if not words_for_all:
                raise SystemExit("--approve-fixed-below wants --words \"<what she said>\"")
            limit = int(below[2:])
            hit = [r for r in rows if r["status"] == "fixed" and int(r["id"][2:]) < limit]
            for r in hit:
                r["status"] = "fixed-approved"
                r["outcome"] = ("%s: %s " % (said, words_for_all.strip()) + r["outcome"]).strip()
            report.append("%d fixed finding(s) below %s are fixed-approved: %s" % (
                len(hit), below, " ".join(r["id"] for r in hit) or "(none)"))

        moved = []
        for number, words in answers:
            line = "**%s:** %s" % (said, words)
            if number.startswith("G-"):
                rig.append(number)
                continue
            where = None
            for i, e in enumerate(opens):
                if number_of(e) == number:
                    moved.append(opens.pop(i).rstrip("\n") + "\n\n" + line + "\n\n\n")
                    where = "moved to Answered"
                    break
            else:
                for i, e in enumerate(answered):
                    if number_of(e) == number:
                        if words not in e:
                            answered[i] = e.rstrip("\n") + "\n\n" + line + "\n\n\n"
                            where = "already under Answered: her words added"
                        else:
                            where = "already under Answered with these words"
                        break
            if number.startswith("F-"):
                row = by_id.get(number)
                if not row:
                    report.append("%s: NO SUCH FINDING" % number)
                    continue
                if words not in row["outcome"]:
                    row["outcome"] = ("%s: %s " % (said, words) + row["outcome"]).strip()
                was = row["status"]
                if number in wanted:
                    row["status"] = wanted.pop(number)
                    report.append("%s: %s -> %s%s" % (number, was, row["status"], "; entry " + where if where else ""))
                else:
                    unset.append("%s (%s)" % (number, was))
                    report.append("%s: her words recorded, status left %s%s" % (number, was, "; entry " + where if where else ""))
            else:
                report.append("%s: %s" % (number, where or "NO ENTRY in questions.md"))
                if where and where.startswith("moved"):
                    for f in sorted(set(re.findall(r"\bF-\d{4}\b", moved[-1].split("\n", 1)[0]))):
                        if f in by_id and by_id[f]["status"] in ("fixed", "candidate", "question", "confirmed") \
                                and "Ketura (" not in by_id[f]["outcome"]:
                            linked.append("%s names %s (%s)" % (number, f, by_id[f]["status"]))
        for number, status in list(wanted.items()):
            row = by_id.get(number)
            if not row:
                report.append("%s: NO SUCH FINDING (--set)" % number)
                continue
            report.append("%s: %s -> %s (--set; no words of hers in this file)" % (number, row["status"], status))
            row["status"] = status

        if not dry:
            new_text = (head + open_lead + "".join(opens) + ans_head + ans_lead + "".join(moved) +
                        "".join(answered) + tail)
            qpath.write_text(new_text, encoding="utf-8")
            tracker.write_findings(state, rows)

    print("\n".join(report))
    log = (REFERENCES / "rulings-log.md").read_text(encoding="utf-8")
    missing = [n for n, _ in answers if not re.search(r"\b%s\b" % re.escape(n), log)]
    print("\nStill yours%s:" % (" (nothing was written: --dry)" if dry else ""))
    if unset:
        print("  status not named (--set): " + ", ".join(unset))
    if linked:
        print("  a finding an answered question names takes that answer's standing: " + "; ".join(linked))
    if rig:
        print("  rig requests she spoke on (queue them): " + " ".join(rig))
    if missing:
        print("  not in the rulings log yet: " + " ".join(missing) + "  (then lookup.py --index)")
    print("  the work each answer asks for goes into a queue file; then postland.py and publish")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
