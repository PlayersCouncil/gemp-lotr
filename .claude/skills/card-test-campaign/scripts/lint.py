#!/usr/bin/env python3
"""Checks card test files against the Card Test Standard (see the test-generation skill).

    lint.py <what...>               what is a blueprint id, a test file, or a selector (set:1, seg:vset, ...)
    lint.py <what...> --quiet       one line per file
    lint.py --summary <what...>     counts only, by rule

Exit status is 0 when every file given is free of errors.  Warnings do not fail a file.

A line can be let off one rule by ending it with a comment that says why:   // lint-ok: <reason>
Each of those is counted and shown to the reviewer, so it is a request, not a way round.
"""
import re
import sys
from pathlib import Path

import gempcards as g

# The checks a clause of each type has to list at the very least.  The standard says which further checks a
# clause's wording calls for; those are the analyst's to list and the reviewer's to confirm.
CORE = {
    "Play": {"+"},
    "Keyword": {"K"},
    "ToPlay": {"+", "-"},
    "ExtraCost": {"+", "-", "C"},
    "PlayOn": {"F"},
    "Activated": {"+", "-"},
    "Event": {"+", "-"},
    "Effect": {"+"},
    "Trigger": {"+", "-"},
    "Response": {"+", "-"},
    "Modifier": {"+", "-"},
    "Restriction": {"+", "-"},
    "Choice": {"B"},
    "Ruling": {"+"},
    "Rule": {"+"},
    "Interaction": {"+"},
    "FormatRule": {"+", "-"},
}
# Card types that a player plays from hand and that then stay in play.  Events are played by their Event clause.
PLAYED = {"companion", "ally", "minion", "possession", "artifact", "condition", "follower"}
CODES = {"+", "-", "C", "C-", "F", "D", "L", "O", "S", "R", "B", "Z", "K"}

LEDGER_LINE = re.compile(r"^\s*([A-Z]\d+)\s+([A-Za-z]+)\s+\[([^\]]*)\]\s*(.*?)\s*$")
WAIVER_LINE = re.compile(r"^\s+no\s+(C-|[+\-CFDLOSRBZK])\s*:\s*\S")
# "waits + F: G-0031 <why>": checks the rig cannot yet show.  The card is parked on that rig request until it can.
WAITS_LINE = re.compile(r"^\s+waits\s+((?:C-|[+\-CFDLOSRBZK])(?:\s+(?:C-|[+\-CFDLOSRBZK]))*)\s*:\s*(G-\d{4})\b")
CLAIM_LINE = re.compile(r"^\s+((?:C-|[+\-CFDLOSRBZK])(?:\s+(?:C-|[+\-CFDLOSRBZK]))*)\s*->\s*([A-Za-z_]\w*)\s*$")
TAG_LINE = re.compile(r"^\s*//\s*((?:[A-Z]\d+\s*:\s*[^;]+;?\s*)+)$")
METHOD = re.compile(r"^\s*(public|protected|private)\s+(?:static\s+)?([\w<>\[\], .]+?)\s+(\w+)\s*\(([^)]*)\)")

FORBIDDEN = [
    (r"\bscn\s*\.\s*(game|gameState|userFeedback)\s*\(\s*\)", "reaches past the rig into the engine; ask for a rig method instead"),
    (r"\bgetModifiersQuerying\b", "reaches past the rig into the engine; ask for a rig method instead"),
    (r"\b(PlayerDecided|FreepsDecided|ShadowDecided)\s*\(", "answers a decision with a raw string; use the Choose/Accept/Decline/Pass methods"),
    (r"\b\w*GetADParam\w*\s*\(|\bGetAwaitingDecision\w*\s*\(|\b(Freeps|Shadow)GetAwaitingDecision\s*\(", "reads a decision's raw parameters; use the Choices getters"),
    (r"\bnew\s+VirtualTableScenario\s*\(", "builds a table inside a test; tables come from a Get...Scenario() method"),
    (r"\bThread\s*\.\s*sleep\b|\bnew\s+Random\b|\bMath\s*\.\s*random\b", "a test may not wait or roll dice"),
    (r"\bsetAccessible\b|java\.lang\.reflect", "reflection is a way round the rig; ask for a rig method instead"),
]
JUSTIFY = [
    (r"\b(ApplyAdHocAction|\w*ExecuteAdHocEffect\w*|ApplyAdHoc\w*AutoDiscard)\s*\(", "an ad hoc effect needs its reason on the same line (// lint-ok: ...)"),
]
ACTS = re.compile(r"\b(Freeps|Shadow)(PlayCard|UseCardAction|TransferCard|DiscardToHeal|Choose\w*|Accept\w*|Decline\w*|"
                  r"Pass\w*|Assign\w*|Resolve\w*)\s*\(|\b(Pass\w+Actions|BothPass\w*|PassResponses|PassCurrentPhaseActions)\s*\(")
# What counts as saying what the game is waiting for once a test has done its last thing.
BOOKEND = re.compile(r"\bAwaiting\w+PhaseActions\s*\(|\bGameIsFinished\s*\(|\b\w*DecisionAvailable\s*\(|"
                     r"\b\w*HasOptionalTriggerAvailable\s*\(|\b\w*AnyDecisionsAvailable\s*\(")


class Finding:
    def __init__(self, level, rule, line, message):
        self.level, self.rule, self.line, self.message = level, rule, line, message


def strip_code(line: str) -> str:
    """A line with its string literals emptied and its trailing comment removed, for looking at the code alone."""
    out, i, n = [], 0, len(line)
    while i < n:
        c = line[i]
        if c == '"':
            j = i + 1
            while j < n and line[j] != '"':
                j += 2 if line[j] == "\\" else 1
            out.append('""')
            i = j + 1
        elif c == "'" and i + 2 < n:
            j = i + 1
            while j < n and line[j] != "'":
                j += 2 if line[j] == "\\" else 1
            out.append("''")
            i = j + 1
        elif line.startswith("//", i):
            break
        else:
            out.append(c)
            i += 1
    return "".join(out)


def split_methods(lines):
    """Each method as (name, first line index, last line index, annotations and comments just above it)."""
    methods, depth, in_block, i = [], 0, False, 0
    code = []
    for line in lines:
        text = line
        if in_block:
            if "*/" in text:
                text = text.split("*/", 1)[1]
                in_block = False
            else:
                code.append("")
                continue
        while "/*" in strip_code(text):
            before, rest = text.split("/*", 1)
            if "*/" in rest:
                text = before + rest.split("*/", 1)[1]
            else:
                text = before
                in_block = True
                break
        code.append(strip_code(text))
    while i < len(lines):
        if depth == 1:
            m = METHOD.match(code[i])
            if m and "(" in code[i]:
                start, name = i, m.group(3)
                # Find the opening brace, then its match.
                d, j, opened = 0, i, False
                while j < len(lines):
                    d += code[j].count("{") - code[j].count("}")
                    if "{" in code[j]:
                        opened = True
                    if opened and d <= 0:
                        break
                    j += 1
                above, k = [], start - 1
                while k >= 0 and (lines[k].strip().startswith("@") or lines[k].strip().startswith("//") or not lines[k].strip()):
                    if lines[k].strip():
                        above.insert(0, (k, lines[k].strip()))
                    elif above:
                        break
                    k -= 1
                methods.append((name, start, j, above, m.group(2).strip()))
                i = j + 1
                continue
        depth += code[i].count("{") - code[i].count("}")
        i += 1
    return methods, code


_baseline = None


def baseline_tests() -> dict:
    """How many tests each card had before the campaign, from the campaign's baseline.tsv."""
    global _baseline
    if _baseline is None:
        _baseline = {}
        path = g.state_dir() / "baseline.tsv"
        if path.is_file():
            rows = path.read_text(encoding="utf-8").split("\n")
            head = rows[0].split("\t")
            if "id" in head and "tests" in head:
                for row in rows[1:]:
                    cells = row.split("\t")
                    if len(cells) == len(head) and cells[head.index("tests")].isdigit():
                        _baseline[cells[head.index("id")]] = int(cells[head.index("tests")])
    return _baseline


NO_EXCEPTIONS = ("a card test never relies on an exception (Ketura, 2026-10-08). Where only an exception shows a check, "
                 "the rig lacks a reading: report a rig request, take the assertion out, and write the check in the ledger "
                 "as 'waits <check>: G-0000 <what the rig lacks>', which parks the card until the rig can show it")


class Waived(set):
    """The checks a clause says it cannot have, with the ones that only wait on a rig request kept apart."""
    def __init__(self):
        super().__init__()
        self.waits = {}  # check -> the rig request it waits on


def parse_ledger(path: Path):
    """A card's ledger file as ({id: [type, checks, text, line]}, {id: {check: [test names]}}, cited ids), or
    (None, None, []) if there is no file."""
    if not path.is_file():
        return None, None, []
    lines = path.read_text(encoding="utf-8").replace("\r", "").split("\n")
    clauses, claims, cited = {}, {}, []
    last, in_header = None, False
    for i, line in enumerate(lines):
        m = LEDGER_LINE.match(line)
        c = CLAIM_LINE.match(line)
        if m:
            clauses[m.group(1)] = [m.group(2), set(m.group(3).split()), m.group(4), i, Waived()]
            claims[m.group(1)] = {}
            last, in_header = m.group(1), False
        elif c and last is not None and not in_header:
            for check in c.group(1).split():
                claims[last].setdefault(check, []).append(c.group(2))
        elif WAITS_LINE.match(line) and last is not None and not in_header:
            w = WAITS_LINE.match(line)
            for check in w.group(1).split():
                clauses[last][4].waits[check] = w.group(2)
        elif WAIVER_LINE.match(line) and last is not None and not in_header:
            # "no -: why": a check the clause's type asks for and this clause cannot have.
            clauses[last][4].add(WAIVER_LINE.match(line).group(1))
        elif re.match(r"^\s*(LEDGER|Sources|Findings|Formats|Dropped|Notes)\b", line):
            in_header = True
        elif not line.strip():
            pass
        elif last is not None and not in_header:
            # A continuation of the previous clause's text.
            clauses[last][2] += " " + line.strip()
        cited += re.findall(r"\b[FQ]-\d{4}\b", line)
    return clauses, claims, cited


def squash(text: str) -> str:
    return re.sub(r"[\s.]+", "", g.plain_text(text))


_by_path = None


def _ids_by_path():
    global _by_path
    if _by_path is None:
        _by_path = {str(p.resolve()): cid for cid, p in g.test_index().items()}
    return _by_path


def lint_file(path: Path):
    out = []

    def add(level, rule, line, message):
        out.append(Finding(level, rule, line + 1 if line is not None else 0, message))

    raw = path.read_text(encoding="utf-8", errors="replace").replace("\r", "")
    lines = raw.split("\n")
    lets_off = 0

    def let_off(i):
        nonlocal lets_off
        if re.search(r"//\s*lint-ok\s*:\s*\S", lines[i]):
            lets_off += 1
            return True
        return False

    card_id = _ids_by_path().get(str(path.resolve()))
    card = g.card_index().get(card_id) if card_id else None
    if card is None:
        add("error", "card", None, "cannot tell which card this file is for, or the card has no definition")

    # Leftovers from the generated skeleton.
    for i, line in enumerate(lines):
        if re.match(r"^\s*//\s*@Test\b", line):
            add("error", "placeholder", i, "a commented-out @Test: delete the skeleton method or make it a real test")
        if "Uncomment any @Test markers" in line:
            add("error", "placeholder", i, "the skeleton's 'Uncomment' note is still here")
        if re.search(r'put\(\s*"card"\s*,', line):
            add("error", "alias", i, 'a card is still called "card": give it a name that says what it is')
        if re.search(r"put other cards in here as needed", line):
            add("error", "placeholder", i, "the skeleton's 'put other cards in here' note is still here")

    methods, code = split_methods(lines)
    tests = [m for m in methods if any(t.startswith("@Test") for _, t in m[3])]
    others = [m for m in methods if m not in tests]
    scenario_methods = [m for m in others if re.match(r"^Get\w*Scenario\w*$", m[0])]
    for name, start, end, above, ret in others:
        if any(re.match(r"^//\s*@Test\b", t) for _, t in above):
            continue  # the skeleton's placeholder, already reported
        if not re.match(r"^Get\w*Scenario\w*$", name):
            add("error", "helper", start, "'%s' is neither a test nor a Get...Scenario() method: what it does belongs in the rig" % name)
    if not scenario_methods:
        add("error", "scenario", None, "no Get...Scenario() method")
    if re.search(r"^\s*public\s+class\s+\w+\s+extends\b", raw, re.M):
        add("error", "helper", None, "a card's test class does not extend anything")

    stats = [m for m in tests if re.search(r"Stats(AndKeywords)?AreCorrect$", m[0])]
    if not stats:
        add("error", "stats", None, "no ...StatsAndKeywordsAreCorrect test")

    for i, line in enumerate(lines):
        if re.match(r"^\s*/?\*+\s*LEDGER\b", line):
            add("error", "in-file", i, "a LEDGER comment in the test file: the ledger lives in its own file, not in the tests")
        if TAG_LINE.match(line):
            add("error", "in-file", i, "a clause tag comment in the test file: which test claims what is recorded in the ledger file")

    ledger_file = g.ledger_path(card_id) if card_id else None
    clauses, claims, cited = parse_ledger(ledger_file) if ledger_file else (None, None, [])
    test_names = {m[0] for m in tests}
    if clauses is None:
        add("error", "ledger", None, "no ledger file (%s)" % (ledger_file if ledger_file else "unknown card"))
        clauses, claims = {}, {}
    else:
        if not clauses:
            add("error", "ledger", None, "the ledger has no clause lines (ID Type [checks] text)")
        for cid, (ctype, codes, text, line, waived) in clauses.items():
            if ctype not in CORE:
                add("error", "ledger", None, "%s: unknown clause type '%s'" % (cid, ctype))
                continue
            bad = codes - CODES
            if bad:
                add("error", "ledger", None, "%s: unknown check %s" % (cid, " ".join(sorted(bad))))
            for check in sorted(waived):
                if check in codes:
                    add("error", "ledger", None, "%s: lists %s and also says it has none" % (cid, check))
                else:
                    add("warning", "waived", None, "%s: no %s, with a reason given in the ledger (for the reviewer to read)" % (cid, check))
            missing = CORE[ctype] - codes - waived
            if missing:
                add("error", "ledger", None, "%s: a %s clause must list at least %s" % (cid, ctype, " ".join(sorted(CORE[ctype]))))
        if card is not None and (card.field("type", "") or "").lower() in PLAYED and not any(c[0] == "Play" for c in clauses.values()):
            add("error", "ledger", None, "no Play clause (P1 Play [+] ...): a card that is played from hand has a test that plays it")
        if card is not None:
            want = squash(card.field("gametext", ""))
            text_clauses = [c for cid, c in clauses.items() if cid.startswith("C")]
            have = squash(" ".join(c[2] for c in sorted(text_clauses, key=lambda c: c[3])))
            if want != have:
                add("error", "ledger-text", None, "the C clauses, read in order, are not the card's game text word for word")

    # Which tests the ledger says claim what.
    claimed_tests = set()
    for cid, by_check in claims.items():
        for check, names in by_check.items():
            for name in names:
                claimed_tests.add(name)
                if name not in test_names:
                    add("error", "claim", None, "%s: the ledger gives check %s to %s, which is not a test in the file" % (cid, check, name))
    parked_tests = {m[0] for m in tests if any(t.startswith("@Ignore") for _, t in m[3])}
    for cid, (ctype, codes, text, line, waived) in clauses.items():
        for check, names in sorted(claims.get(cid, {}).items()):
            if names and all(n in parked_tests for n in names):
                add("warning", "parked-only", None, "%s: %s is claimed only by a parked test, so nothing exercises it at present" % (cid, check))
        for check, request in sorted(waived.waits.items()):
            # A check may be shown in part by a test and wait in part (one candidate of an F the rig cannot place).
            if request == "G-0000":
                add("warning", "waiting", None, "%s: %s waits on a rig request not yet numbered (the lander numbers it)" % (cid, check))
            else:
                add("warning", "waiting", None, "%s: %s waits on %s: the card is parked until the rig can show it" % (cid, check, request))
        missing = (codes & CODES) - set(claims.get(cid, {})) - set(waived.waits)
        if missing:
            add("error", "coverage", None, "%s: no test claims %s" % (cid, " ".join(sorted(missing))))
        extra = set(claims.get(cid, {})) - codes
        if extra:
            add("error", "claim", None, "%s: tests claim %s, which the clause does not list" % (cid, " ".join(sorted(extra))))
    if card_id and re.search(r'@Ignore\("Q-', raw):
        log = g.SKILLS / "card-test-campaign" / "references" / "rulings-log.md"
        if log.is_file() and re.search(r"\b%s\b" % re.escape(card_id), log.read_text(encoding="utf-8")):
            add("warning", "ruled", None, "a test is parked on a question, and the rulings log names this card: "
                                          "check whether the question has been answered")
    if card_id and clauses and ledger_file is not None:
        had = baseline_tests().get(card_id, 0)
        if had > 1 and not re.search(r"^\s*Dropped\s*:", ledger_file.read_text(encoding="utf-8"), re.M):
            add("warning", "dropped", None, "the card had %d tests before the campaign and the ledger has no Dropped: line "
                                            "(what the old tests asserted that the new ones do not, or 'none')" % had)
    for name, start, end, above, ret in tests:
        if clauses and name not in claimed_tests and name not in [x[0] for x in stats]:
            add("error", "claim", start, "%s: the ledger gives this test nothing to claim" % name)
        for _, t in above:
            if t.startswith("@Ignore") and not re.search(r"\b[FQG]-\d{4}\b", t):
                add("error", "ignore", start, "%s: @Ignore must name the finding (F-0000), question (Q-0000) or rig request (G-0000) it is parked on" % name)
            if re.search(r"@Test\s*\(.*expected\s*=", t):
                add("error", "exception", start, "%s: %s" % (name, NO_EXCEPTIONS))

    alias_bp = dict(re.findall(r'put\(\s*"([^"]+)"\s*,\s*"(\d+_\d+)"\s*\)', raw))
    # Inside the tests.
    for name, start, end, above, ret in tests:
        body = code[start:end + 1]
        text = "\n".join(body)
        is_stats = name in [s[0] for s in stats]
        if not re.search(r"\bGet\w*Scenario\w*\s*\(", text):
            add("error", "scenario", start, "%s: does not get its table from a Get...Scenario() method" % name)
        for offset, line in enumerate(body):
            i = start + offset
            for pat, why in FORBIDDEN:
                if re.search(pat, line) and not let_off(i):
                    add("error", "reach", i, "%s: %s" % (name, why))
            for pat, why in JUSTIFY:
                if re.search(pat, line) and not let_off(i):
                    add("error", "adhoc", i, "%s: %s" % (name, why))
            if re.search(r"\bassert(DoesNot)?Throws?\w*\s*\(|\btry\s*\{|\bcatch\s*\(", line):
                add("error", "exception", i, "%s: %s" % (name, NO_EXCEPTIONS))
            if re.search(r"\.getZone\s*\(\s*\)", line) and not let_off(i):
                add("warning", "style", i, "%s: compare zones with assertInZone and friends" % name)
            if re.search(r"assert(True|False)\s*\(.*(==|!=)", line) and not let_off(i):
                add("warning", "style", i, "%s: assertEquals reports both values where assertTrue(a == b) reports neither" % name)
        if not is_stats:
            last_act = max([n for n, line in enumerate(body) if ACTS.search(line)], default=None)
            if last_act is not None and not any(BOOKEND.search(line) for line in body[last_act:]):
                add("error", "bookend", start, "%s: after its last play or answer, never checks what the game is waiting for" % name)
        if not is_stats and not re.search(r"\bassert\w+\s*\(", text):
            add("error", "assert", start, "%s: asserts nothing" % name)
        # A choice from a pile goes by blueprint: with a second copy of that blueprint still in the pile the game may
        # hand over either, and a test that then reads the named copy's zone fails at random.
        rawtext = "\n".join(lines[start:end + 1])
        var_alias = dict(re.findall(r'\bvar\s+(\w+)\s*=\s*scn\.Get(?:Freeps|Shadow)Card\(\s*"([^"]+)"', rawtext))
        for chosen in re.findall(r'Choose\w*BPFromSelection\(\s*(\w+)\s*\)', text):
            alias = var_alias.get(chosen)
            bp = alias_bp.get(alias)
            twins = sorted(a for a, b in alias_bp.items() if b == bp and a != alias) if bp else []
            moved = [t for t in twins if re.search(r'GetFreepsCard\(\s*"%s"|GetShadowCard\(\s*"%s"|\b%s\b' % (t, t, t), rawtext)]
            if twins and not moved:
                add("warning", "same-blueprint", start, "%s: %s is chosen by blueprint, and %s %s the same blueprint (%s): unless "
                    "the twin is out of that pile, either copy may be taken and a reading of %s's zone fails at random"
                    % (name, chosen, ", ".join(twins), "has" if len(twins) == 1 else "have", bp, chosen))

    # A race that shares its name with a culture (F-0019): unless the table holds a card of that culture outside the
    # race, culture(elven) written for elf passes every test. The usual board is a Man given the culture ad hoc.
    if card is not None:
        races = set()
        for line in card.raw.split("\n"):
            m = re.match(r"\s*(\w+)\s*:\s*(.*)", line)
            if m and re.search(r"(filter|select|target|^on$|bearer)", m.group(1), re.I):
                races.update(r.lower() for r in re.findall(r"(?<![\w(])(elf|dwarf|hobbit)\b", m.group(2), re.I))
        if races and not re.search(r"IsAdditional(Culture|Race)Modifier", raw):
            pairs = {"elf": "[elven]", "dwarf": "[dwarven]", "hobbit": "[shire]"}
            add("warning", "race-culture", None, "the definition chooses or counts by race (%s), and no test gives a card "
                "an additional culture or race: unless some test's table already holds a %s card that is not of that "
                "race, the culture written for the race passes every test (F-0019)"
                % (", ".join(sorted(races)), " / ".join(pairs[r] for r in sorted(races))))

    if card is not None and g.sheet_for(card_id)[0] is not None:
        import reconcile
        diffs = reconcile.compare(card_id)
        if diffs and not cited:
            add("warning", "sheet", None, "the spreadsheet and the definition disagree on %s, and the LEDGER cites no finding"
                % ", ".join(d[0] for d in diffs))
    return out, lets_off


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    if not args:
        print(__doc__)
        return 0
    tests = g.test_index()
    files = []
    for a in args:
        p = Path(a)
        if a.endswith(".java"):
            files.append(p if p.is_file() else g.REPO / a)
        else:
            for card_id in g.resolve_ids([a]):
                if card_id in tests:
                    files.append(tests[card_id])
    failed, by_rule, total_off = 0, {}, 0
    for f in files:
        findings, lets_off = lint_file(f)
        total_off += lets_off
        errors = [x for x in findings if x.level == "error"]
        warnings = [x for x in findings if x.level == "warning"]
        for x in findings:
            by_rule[(x.level, x.rule)] = by_rule.get((x.level, x.rule), 0) + 1
        if errors:
            failed += 1
        if "--summary" in argv:
            continue
        if "--quiet" in argv:
            print("%-4s %s (%d errors, %d warnings%s)" % ("FAIL" if errors else "ok", f.name, len(errors), len(warnings),
                                                          ", %d let off" % lets_off if lets_off else ""))
            continue
        if findings or lets_off:
            print("%s: %s" % ("FAIL" if errors else "ok", g.rel(f)))
            for x in sorted(findings, key=lambda x: x.line):
                print("  %-7s %-11s line %-4d %s" % (x.level, x.rule, x.line, x.message))
            if lets_off:
                print("  note    %d line(s) let off with lint-ok" % lets_off)
        else:
            print("ok: %s" % g.rel(f))
    if "--summary" in argv or len(files) > 1:
        print("\n%d of %d files pass.  %d line(s) let off." % (len(files) - failed, len(files), total_off))
        for (level, rule), n in sorted(by_rule.items(), key=lambda kv: -kv[1]):
            print("  %5d  %-7s %s" % (n, level, rule))
    return 1 if failed else 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
