#!/usr/bin/env python3
"""Puts together Ketura's report on the old at/ tests from the survey's .tsv files:
<state>/orchestrator/at-survey/REPORT.md, one line per old test method: its name, one sentence on what it checks
(cards by name), and where its replacement goes.  Run after any survey group has finished; postland.py runs it."""
import csv
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gempcards as g

REPO = Path(g.REPO)
AT = REPO / "gemp-lotr/gemp-lotr-server/src/test/java/com/gempukku/lotro/at"
KIND = {"rule": "a rule", "card": "a card", "language": "the definition language", "server": "the server, not the game"}


def main():
    folder = g.state_dir() / "orchestrator" / "at-survey"
    if not folder.is_dir():
        return 0
    files = sorted((p for p in AT.rglob("*.java") if re.search(r"@Test\b", p.read_text(encoding="utf-8", errors="replace"))),
                   key=lambda p: (p.parent != AT, p.name))   # the files of at/ first, then at/effects/
    total = sum(len(re.findall(r"@Test\b", p.read_text(encoding="utf-8", errors="replace"))) for p in files)
    done, out, waiting = 0, [], []
    for p in files:
        count = len(re.findall(r"@Test\b", p.read_text(encoding="utf-8", errors="replace")))
        tsv = folder / (p.stem + ".tsv")
        if not tsv.is_file():
            waiting.append("%s (%d)" % (p.stem, count))
            continue
        rows = [r for r in csv.reader(tsv.open(encoding="utf-8"), delimiter="\t", quoting=csv.QUOTE_NONE) if len(r) >= 6]
        done += len(rows)
        out.append("\n## %s: %d test%s%s\n" % (p.stem, len(rows), "" if len(rows) == 1 else "s",
                                             "" if len(rows) == count else " (the file has %d)" % count))
        for r in rows:
            r = r + [""] * (8 - len(r))
            _, method, kind, covered, home, new_name, _cards, sentence = r[:8]
            home = home.strip()
            if home in ("", "-") or home.startswith("?"):
                fate = "no replacement proposed" if home in ("", "-") else "its home is not settled"
            else:
                state = {"covered": "already covered by", "part": "partly covered; the rest goes to", "new": "goes to"}.get(covered, "goes to")
                fate = "%s %s%s" % (state, Path(home).stem, (" as " + new_name) if new_name and new_name != "-" and covered != "covered" else "")
            out.append("- **%s**: %s\n  [%s; %s]" % (method, sentence.strip() or "(no sentence written)", KIND.get(kind, kind), fate))
    total = max(total, done + sum(int(re.search(r"\((\d+)\)", w).group(1)) for w in waiting))   # methods already converted are gone from at/
    head = ["# The old at/ tests: what each one checks",
            "",
            "One line for each of the %d test methods in the %d files of `at/`, written by the survey that comes before their conversion "
            "(Ketura, 2026-10-09: \"a report of what exactly all the at tests appear to be testing\"). Each line: the old "
            "method's name; one sentence on what it checks, cards by name; then in brackets what kind of thing it is and where its "
            "replacement goes. %d of %d done so far." % (total, len(files), done, total)]
    if waiting:
        head += ["", "Still to be surveyed: " + ", ".join(waiting) + "."]
    (folder / "REPORT.md").write_text("\n".join(head + out) + "\n", encoding="utf-8")
    print("at report: %d of %d methods" % (done, total))
    return 0


if __name__ == "__main__":
    sys.exit(main())
