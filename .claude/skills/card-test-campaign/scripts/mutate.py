#!/usr/bin/env python3
"""Finds out whether a card's tests would notice if the card were wrong.

    mutate.py <id> [--out <name>]           makes its own mistakes and lists the survivors
    mutate.py <id> --replace <old> <new> [--nth N]   makes one mistake of yours, in the Nth place the old text occurs
    mutate.py <id> --all                    lists every mistake and what noticed it
    mutate.py <id> --replace <old> <new> --with-parked
                                            runs the parked (@Ignore) tests too, and says which tests fail: with
                                            the correction a finding proposes as the "mistake", a sound parked
                                            test is one that passes here and fails against the card as it is
    mutate.py <id> --show                   lists the mistakes it would make, and runs nothing

Makes small, deliberate mistakes in a copy of the card's definition, one at a time (a number one off either way, a
word dropped from a filter, a filter narrowed to the card's own culture or to companions, a phase changed, a cost
or a requirement removed, "may" turned into "must"), and runs the card's tests against each.  A mistake that no test notices is a *survivor*: either the tests have a gap there, or
the mistake makes no difference to how the card plays.  Deciding which is the reviewer's job; this only finds them.

This is a check on tests that already exist.  It is run after the tests are written and never before: the
definition must not be where a test's expectations come from.

Nothing in the repository is changed.  Each mistake is made in a throwaway copy of the resources folder under
target/cardtest/, and the tests are run from there.  --show lists the mistakes without running anything.
"""
import concurrent.futures
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

import cardtest
import gempcards as g

NUMBER = re.compile(r"^(\s*)([A-Za-z]+)(\s*:\s*)(-?\d+)\s*$")
RANGE = re.compile(r"^(\s*)([A-Za-z]+)(\s*:\s*)(\d+)-(\d+)\s*$")
UNTIL = re.compile(r"^(\s*)(until)(\s*:\s*)(?:(start|end)\()?(fellowship|shadow|maneuver|archery|assignment|skirmish|regroup)\)?\s*$", re.I)
PHASES = ["fellowship", "shadow", "maneuver", "archery", "assignment", "skirmish", "regroup"]
FLAG = re.compile(r"^(\s*)(optional|forced|shuffle|showAll|reveal)(\s*:\s*)(true|false)\s*$", re.I)
PHASE = re.compile(r"^(\s*)(phase)(\s*:\s*)(fellowship|shadow|maneuver|archery|assignment|skirmish|regroup)\s*$", re.I)
FILTER = re.compile(r"^(\s*)(filter|select|on|against|target|bearer|from|to|where)(\s*:\s*)(.+?)\s*$")
DEFAULTED = re.compile(r"^(\s*)(on|where|fromZone|against)\s*:\s*\S")
KIND_WORDS = ("companion", "minion", "ally", "possession", "condition", "artifact", "event", "site", "follower",
              "self", "bearer", "memory(", "weapon", "item", "ring", "character")
BLOCK = re.compile(r"^(\s*)(requires|cost|until|limitPerPhase|limitPerTurn)\s*:")
OTHER_PHASE = {"fellowship": "maneuver", "shadow": "maneuver", "maneuver": "regroup", "archery": "maneuver",
               "assignment": "maneuver", "skirmish": "maneuver", "regroup": "maneuver"}


def split_terms(text: str) -> list:
    """Splits on the commas that are not inside brackets."""
    out, depth, cur = [], 0, ""
    for c in text:
        if c in "([":
            depth += 1
        elif c in ")]":
            depth -= 1
        if c == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += c
    out.append(cur)
    return out


_culture_words = None


def words_of_one_culture(culture: str) -> set:
    """Races, titles and keywords every card of which is of the given culture.  Narrowing "dwarf" to [dwarven]
    makes no card behave differently, because there is no Dwarf of another culture to leave out; likewise
    "tentacle" and [moria]."""
    global _culture_words
    if _culture_words is None:
        _culture_words = {}
        for card in g.card_index().values():
            c = (card.field("culture", "") or "").strip().lower()
            words = [card.field("race", ""), card.field("title", "")]
            m = re.search(r"^\s*keywords\s*:\s*(\[.*?\]|[^\n\[]*)", card.raw, re.S | re.M)
            if m:
                words += re.split(r"[\n,\[\]]", m.group(1))
            for word in words:
                word = re.sub(r"[^a-z]", "", (word or "").lower())
                if word:
                    _culture_words.setdefault(word, set()).add(c)
    return {w for w, cultures in _culture_words.items() if cultures == {culture}}


def repeated_keys(lines: list) -> list:
    """(line index, key, earlier line index) wherever one block gives the same key twice.  The later one is the
    one the game reads, so a mistake made in the earlier one cannot be noticed."""
    out, stack, commented = [], [{}], False
    for i, line in enumerate(lines):
        # Whole alternatives are sometimes kept in a definition inside /* ... */: not part of the card.
        if commented:
            commented = "*/" not in line
            continue
        if line.strip().startswith("/*"):
            commented = "*/" not in line
            continue
        m = re.match(r"^\s*([A-Za-z]\w*)\s*:", line)
        if m:
            if m.group(1) in stack[-1]:
                out.append((i, m.group(1), stack[-1][m.group(1)]))
            stack[-1][m.group(1)] = i
        change = g._depth_change(line)
        for _ in range(max(0, change)):
            stack.append({})
        for _ in range(max(0, -change)):
            if len(stack) > 1:
                stack.pop()
    return out


def mistakes(lines: list, culture: str = "") -> list:
    """Every mistake worth making, as (line index, description, the new lines for the whole card)."""
    out = []
    one_culture = words_of_one_culture(culture) if culture else set()

    def add(i, what, new_lines):
        out.append((i, what, new_lines))

    in_meta, depth = 0, 0
    for i, line in enumerate(lines):
        stripped = line.strip()
        # Leave the image, rarity, lore and alternate-art bookkeeping alone: it is not how the card plays.
        if re.match(r"^\s*(cardInfo|alts)\s*:\s*\{", line):
            in_meta = 1
            continue
        if in_meta:
            in_meta += g._depth_change(line)
            continue
        if re.match(r"^\s*(gametext|lore|promotext|title|subtitle|text|helptext)\s*:", line):
            continue

        m = NUMBER.match(line)
        if m:
            n = int(m.group(4))
            for other in (n + 1, n - 1):
                if other < 0 and n >= 0:
                    continue
                new = lines[:]
                new[i] = "%s%s%s%d" % (m.group(1), m.group(2), m.group(3), other)
                add(i, "%s: %d becomes %d" % (m.group(2), n, other), new)
            continue
        m = RANGE.match(line)
        if m:
            low, high = int(m.group(4)), int(m.group(5))
            for new_low, new_high in ((low + 1, high), (low - 1, high), (low, high - 1)):
                if new_low < 0 or new_high < new_low:
                    continue
                new = lines[:]
                new[i] = "%s%s%s%d-%d" % (m.group(1), m.group(2), m.group(3), new_low, new_high)
                add(i, "%s: %d-%d becomes %d-%d" % (m.group(2), low, high, new_low, new_high), new)
            continue
        m = UNTIL.match(line)
        if m:
            # The boundary moved to its neighbours: the other end of the same phase, and the end of the phase before.
            edge, phase = (m.group(4) or "start").lower(), m.group(5).lower()
            others = ["%s(%s)" % ("end" if edge == "start" else "start", phase)]
            if phase in PHASES and PHASES.index(phase) > 0:
                others.append("end(%s)" % PHASES[PHASES.index(phase) - 1])
                if PHASES.index(phase) > 1:
                    others.append("end(%s)" % PHASES[PHASES.index(phase) - 2])
            others.append("endofturn")
            for other in others:
                new = lines[:]
                new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), other)
                add(i, "until: %s becomes %s" % (line.split(":", 1)[1].strip(), other), new)
            # ...and then on to "until removed" below.
        m = FLAG.match(line)
        if m and m.group(2).lower() in ("optional", "forced", "shuffle", "showall", "reveal"):
            flipped = "false" if m.group(4).lower() == "true" else "true"
            new = lines[:]
            new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), flipped)
            add(i, "%s: %s becomes %s" % (m.group(2), m.group(4), flipped), new)
            continue
        m = PHASE.match(line)
        if m:
            other = OTHER_PHASE[m.group(4).lower()]
            new = lines[:]
            new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), other)
            add(i, "phase: %s becomes %s" % (m.group(4), other), new)
            continue
        m = FILTER.match(line)
        if m and not stripped.endswith("{") and not stripped.endswith("["):
            value = m.group(4)
            wrapper = re.match(r"^(choose|all)\((.*)\)$", value)
            inner = wrapper.group(2) if wrapper else value
            terms = split_terms(inner)

            def rewritten(new_inner):
                new_value = "%s(%s)" % (wrapper.group(1), new_inner) if wrapper else new_inner
                new = lines[:]
                new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), new_value)
                return new

            # Wider: each word dropped in turn.
            if len(terms) > 1:
                for t in range(len(terms)):
                    kept = ",".join(terms[:t] + terms[t + 1:])
                    add(i, "%s: '%s' dropped from %s" % (m.group(2), terms[t].strip(), value), rewritten(kept))
            # Narrower: the commonest way a filter is written too tight is with the card's own culture or with
            # "companion" where the text says neither.
            low = inner.lower()
            plain = not any(w in low for w in ("self", "bearer", "memory(", "storedculture", "choose(")) and m.group(2) != "where"
            named = {re.sub(r"[^a-z]", "", w) for w in re.split(r"[,()]", low)}
            if plain and culture and "culture(" not in low and not (named & one_culture):
                add(i, "%s: narrowed to culture(%s): %s" % (m.group(2), culture, value), rewritten(inner + ",culture(%s)" % culture))
            if plain and not any(w in low for w in KIND_WORDS):
                add(i, "%s: narrowed to companions: %s" % (m.group(2), value), rewritten(inner + ",companion"))
            # "Skirmishing" loosened to "assigned to a skirmish with": true before the skirmish begins as well.
            swapped = re.sub(r"(?i)inSkirmishAgainst\(", "assignedToSkirmish(", value)
            if swapped != value:
                new = lines[:]
                new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), swapped)
                add(i, "%s: %s becomes %s" % (m.group(2), value, swapped), new)
            # A number inside the filter, as in HasAttachedCount(2,item) or maxTwilight(3): one more and one fewer.
            for n in re.finditer(r"(?<![\w-])(\d+)(?![\w-])", value):
                for other in (int(n.group(1)) + 1, int(n.group(1)) - 1):
                    if other < 0:
                        continue
                    new_value = value[:n.start(1)] + str(other) + value[n.end(1):]
                    new = lines[:]
                    new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), new_value)
                    add(i, "%s: %s becomes %s" % (m.group(2), value, new_value), new)
        m = DEFAULTED.match(line)
        if m and not stripped.endswith("{") and not stripped.endswith("["):
            add(i, "%s removed, so its default applies" % m.group(2), lines[:i] + lines[i + 1:])
        m = BLOCK.match(line)
        if m:
            # Take the whole field out, however many lines it runs to.
            end = i
            if stripped.endswith("{") or stripped.endswith("["):
                d = 1
                while d > 0 and end + 1 < len(lines):
                    end += 1
                    d += g._depth_change(lines[end])
            new = lines[:i] + lines[end + 1:]
            add(i, "%s removed" % m.group(2), new)
    return out


def sandbox(root: Path, card: g.CardDef, new_text: str) -> Path:
    """A resources folder that is the real one by symlink, except for one rewritten card file.  Returns the
    folder to run the tests from."""
    if root.exists():
        shutil.rmtree(root)
    cwd = root / "gemp-lotr-server"
    cwd.mkdir(parents=True)
    real = g.RESOURCES_DIR
    fake = root / "gemp-lotr-cards" / "src" / "main" / "resources"
    fake.mkdir(parents=True)
    rel = card.file.relative_to(real)
    here_real, here_fake = real, fake
    for part in rel.parts[:-1]:
        for entry in here_real.iterdir():
            if entry.name != part:
                os.symlink(entry, here_fake / entry.name)
        here_real, here_fake = here_real / part, here_fake / part
        here_fake.mkdir()
    for entry in here_real.iterdir():
        if entry.name != rel.parts[-1]:
            os.symlink(entry, here_fake / entry.name)
    (here_fake / rel.parts[-1]).write_text(new_text, encoding="utf-8")
    return cwd


def run_tests(cwd: Path, cp: str, test_class: str):
    proc = subprocess.run(["java", "-cp", cp, "org.junit.runner.JUnitCore", test_class], cwd=cwd, env=cardtest.env(),
                          capture_output=True, text=True, errors="replace")
    out = proc.stdout + proc.stderr
    if re.search(r"^OK \(\d+ tests?\)", out, re.M):
        return "survived", ""
    if "CardNotFoundException" in out or "Error while loading file" in out or "InvalidCardDefinitionException" in out:
        return "invalid", ""
    failed = re.findall(r"^\d+\) (\w+)\(", out, re.M)
    return "noticed", ", ".join(sorted(set(failed)))


def main(argv):
    skip = set()
    with_parked = "--with-parked" in argv
    for flag, width in (("--out", 1), ("--replace", 2), ("--nth", 1)):
        if flag in argv:
            i = argv.index(flag)
            skip.update(range(i, i + width + 1))
    args = [a for n, a in enumerate(argv) if n > 0 and n not in skip and not a.startswith("--")]
    out_name = argv[argv.index("--out") + 1] if "--out" in argv else "default"
    if not args:
        print(__doc__)
        return 0
    card_id = args[0]
    card = g.card_index().get(card_id)
    test = g.test_index().get(card_id)
    if card is None or test is None:
        raise SystemExit("%s needs both a definition and a test file." % card_id)
    file_text = card.file.read_text(encoding="utf-8")
    file_lines = file_text.replace("\r", "").split("\n")
    first = card.line - 1
    card_lines = card.raw.split("\n")
    todo = mistakes(card_lines, (card.field("culture", "") or "").strip().lower())
    if "--replace" in argv:
        i = argv.index("--replace")
        old_text, new_text = argv[i + 1], argv[i + 2]
        nth = int(argv[argv.index("--nth") + 1]) if "--nth" in argv else 1
        at = -1
        for _ in range(nth):
            at = card.raw.find(old_text, at + 1)
            if at < 0:
                raise SystemExit("'%s' is not in the definition of %s %d time(s)." % (old_text, card_id, nth))
        todo = [(card.raw[:at].count("\n"), "'%s' becomes '%s'%s" % (old_text, new_text, " (occurrence %d)" % nth if nth > 1 else ""),
                 (card.raw[:at] + new_text + card.raw[at + len(old_text):]).split("\n"))]
    repeats = repeated_keys(card_lines)
    if "--replace" not in argv:
        shadowed = {earlier for _, _, earlier in repeats}
        todo = [t for t in todo if t[0] not in shadowed]
    for i, key, earlier in repeats:
        print("note: line %d gives '%s' again in the same block as line %d; the game reads the later one, so a "
              "mistake made in the earlier one cannot be noticed" % (card.line + i, key, card.line + earlier))
    if "--show" in argv:
        for i, what, _ in todo:
            print("line %d: %s" % (card.line + i, what))
        print("%d mistakes" % len(todo))
        return 0

    base = cardtest.TARGET / "cardtest" / (out_name + "-mutate")
    classes = base / "classes"
    if base.exists():
        shutil.rmtree(base)
    classes.mkdir(parents=True)
    cp = cardtest.classpath(classes)
    source = test
    if with_parked:
        # A copy of the test file with its @Ignore lines taken off, so that the parked tests run too.
        source = base / "src" / test.name
        source.parent.mkdir(parents=True)
        source.write_text(re.sub(r"(?m)^\s*@Ignore\b.*\n", "", test.read_text(encoding="utf-8")), encoding="utf-8")
    javac = subprocess.run(["javac", "-nowarn", "-proc:none", "-encoding", "UTF-8", "-d", str(classes), "-cp", cp, str(source)],
                           cwd=g.SERVER, env=cardtest.env(), capture_output=True, text=True)
    if javac.returncode != 0:
        print(javac.stdout + javac.stderr)
        raise SystemExit("The test file does not compile.")
    test_class = g.test_class_name(test)

    verdict, failing = run_tests(g.SERVER, cp, test_class)
    if with_parked:
        print("With the parked tests switched on, against the card as it is: %s" % ("all pass" if verdict == "survived" else "failing: " + failing))
    elif verdict != "survived":
        raise SystemExit("The tests do not pass against the card as it is; fix that first.")

    def one(n):
        i, what, new_card = todo[n]
        new_file = "\n".join(file_lines[:first] + new_card + file_lines[first + len(card_lines):])
        cwd = sandbox(base / ("m%03d" % n), card, new_file)
        verdict, failed = run_tests(cwd, cp, test_class)
        shutil.rmtree(base / ("m%03d" % n), ignore_errors=True)
        return n, verdict, failed

    results = {}
    with concurrent.futures.ThreadPoolExecutor(max_workers=max(1, (os.cpu_count() or 2))) as pool:
        for n, verdict, failed in pool.map(one, range(len(todo))):
            results[n] = (verdict, failed)

    print("%s  %s: %d mistakes made in the definition" % (card_id, g.full_name(card), len(todo)))
    counts = {"noticed": 0, "survived": 0, "invalid": 0}
    for n, (i, what, _) in enumerate(todo):
        verdict, failed = results[n]
        counts[verdict] += 1
        if verdict == "survived":
            print("  SURVIVED  line %d: %s" % (card.line + i, what))
        elif "--all" in argv or with_parked:
            print("  %-9s line %d: %s%s" % (verdict, card.line + i, what, "  (" + failed + ")" if failed else ""))
    print("%d noticed, %d survived, %d made the card unloadable" % (counts["noticed"], counts["survived"], counts["invalid"]))
    shutil.rmtree(base, ignore_errors=True)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
