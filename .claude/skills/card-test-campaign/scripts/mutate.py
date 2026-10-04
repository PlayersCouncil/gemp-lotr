#!/usr/bin/env python3
"""Finds out whether a card's tests would notice if the card were wrong.

    mutate.py <id> [--out <name>] [--show]

Makes small, deliberate mistakes in a copy of the card's definition, one at a time (a number one off, a word
dropped from a filter, a phase changed, a cost or a requirement removed, "may" turned into "must"), and runs the
card's tests against each.  A mistake that no test notices is a *survivor*: either the tests have a gap there, or
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

NUMBER = re.compile(r"^(\s*)(amount|count|times|limit|limitPerPhase|limitPerTurn|min|max|multiplier|value|"
                    r"twilight|strength|vitality|resistance|site|memorizedCount)(\s*:\s*)(-?\d+)\s*$")
FLAG = re.compile(r"^(\s*)(optional|forced|shuffle|showAll|player)(\s*:\s*)(true|false)\s*$", re.I)
PHASE = re.compile(r"^(\s*)(phase)(\s*:\s*)(fellowship|shadow|maneuver|archery|assignment|skirmish|regroup)\s*$", re.I)
FILTER = re.compile(r"^(\s*)(filter|select|on|against|target|bearer|from|to)(\s*:\s*)(.+?)\s*$")
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


def mistakes(lines: list) -> list:
    """Every mistake worth making, as (line index, description, the new lines for the whole card)."""
    out = []

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
            new = lines[:]
            new[i] = "%s%s%s%d" % (m.group(1), m.group(2), m.group(3), n + 1)
            add(i, "%s: %d becomes %d" % (m.group(2), n, n + 1), new)
            continue
        m = FLAG.match(line)
        if m and m.group(2).lower() in ("optional", "forced"):
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
            if len(terms) > 1:
                for t in range(len(terms)):
                    kept = ",".join(terms[:t] + terms[t + 1:])
                    new_value = "%s(%s)" % (wrapper.group(1), kept) if wrapper else kept
                    new = lines[:]
                    new[i] = "%s%s%s%s" % (m.group(1), m.group(2), m.group(3), new_value)
                    add(i, "%s: '%s' dropped from %s" % (m.group(2), terms[t].strip(), value), new)
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
    args = [a for a in argv[1:] if not a.startswith("--")]
    out_name = "default"
    if "--out" in argv:
        out_name = argv[argv.index("--out") + 1]
        args = [a for a in args if a != out_name]
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
    todo = mistakes(card_lines)
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
    javac = subprocess.run(["javac", "-nowarn", "-proc:none", "-encoding", "UTF-8", "-d", str(classes), "-cp", cp, str(test)],
                           cwd=g.SERVER, env=cardtest.env(), capture_output=True, text=True)
    if javac.returncode != 0:
        print(javac.stdout + javac.stderr)
        raise SystemExit("The test file does not compile.")
    test_class = g.test_class_name(test)

    verdict, _ = run_tests(g.SERVER, cp, test_class)
    if verdict != "survived":
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
        elif "--all" in argv:
            print("  %-9s line %d: %s%s" % (verdict, card.line + i, what, "  (" + failed + ")" if failed else ""))
    print("%d noticed, %d survived, %d made the card unloadable" % (counts["noticed"], counts["survived"], counts["invalid"]))
    shutil.rmtree(base, ignore_errors=True)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
