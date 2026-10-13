#!/usr/bin/env python3
"""Finds out whether a card's tests would notice if the card were wrong.

    mutate.py <id> [--out <name>]           makes its own mistakes and lists the survivors
    mutate.py <id> --replace <old> <new> [--nth N]   makes one mistake of yours, in the Nth place the old text occurs
                                            within the card's definition (counted from the card's own first line,
                                            not the file's), and names the tests that noticed it
    mutate.py <id> --replace <old> <new> --every     the same mistake in every place the old text occurs, as one mistake
    mutate.py <id> --replace <a> <b> --replace <c> <d>
                                            several changes made together as one mistake, one after another, each in
                                            the first place its old text occurs (or in every place, with --every)
    mutate.py <id> --all                    lists every mistake and what noticed it
    mutate.py <id> --replace <old> <new> --with-parked
                                            runs the parked (@Ignore) tests too, and says which tests fail and the
                                            first lines of why: with the correction a finding proposes as the
                                            "mistake", a sound parked test is one that passes here and fails
                                            against the card as it is.  Only failing tests are listed: a correction
                                            under which nothing fails prints one line, with the number of tests run.
                                            The report is the "As a correction" line alone, with no count of
                                            mistakes noticed and survived over it.
                                            It also goes on where a test fails against the card as it is (without it
                                            the tool stops there), which is how a probe that fails today is tried
                                            under a correction
    mutate.py <id> --show                   lists the mistakes it would make, and runs nothing
    mutate.py <id> ... --test <file.java>   runs the tests of that file (a mechanics class, say) in place of the
                                            card's own test file, with any of the above: for a test parked on this
                                            card's definition that is kept somewhere else.  The card then needs no
                                            test file of its own
    mutate.py <id> ... --printed            also shows what the tests wrote to standard output in each run (a probe's
                                            readings of the game under a correction), under the line that reports
                                            the run
    --timeout <seconds>                     how long one run of the card's tests may take (180): a run that does not
                                            return is stopped and reported, so a parked test whose failure is a hang
                                            cannot hold the tool (such a test is written @Test(timeout = 20000))

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
# A type word, and the beginnings of other filter words that already imply it.
IMPLIED = (("companion", ("signet(", "unbound", "ringbound", "ring-bound")),
           ("minion", ("inskirmishagainst(", "assignedtoskirmish(", "assignedagainst(")),
           ("event", ("timeword(",)))

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


_title_types = None


def types_of_title(title: str) -> dict:
    """How many cards of each card type bear a title: {"minion": 14, "follower": 1} for Saruman."""
    global _title_types
    if _title_types is None:
        _title_types = {}
        for card in g.card_index().values():
            t = re.sub(r"[^a-z]", "", (card.field("title", "") or "").lower())
            kind = re.sub(r"[^a-z]", "", (card.field("type", "") or "").lower())
            if t and kind:
                _title_types.setdefault(t, {})
                _title_types[t][kind] = _title_types[t].get(kind, 0) + 1
    return _title_types.get(re.sub(r"[^a-z]", "", title.lower()), {})


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

    def beside(i, pattern):
        """Whether a line matching the pattern sits in the same block as line i (same indentation, no shallower
        line between)."""
        indent = len(lines[i]) - len(lines[i].lstrip())
        for step in (-1, 1):
            j = i + step
            while 0 <= j < len(lines):
                text = lines[j]
                if text.strip():
                    d = len(text) - len(text.lstrip())
                    if d < indent:
                        break
                    if d == indent and re.search(pattern, text, re.I):
                        return True
                j += step
        return False

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
        # Two numbers that have never made a difference in play: the ceiling an incrementPer...Limit counts
        # towards (the requirement's own limit is what refuses), and a count beside a remembered selection.
        if m and ((m.group(2).lower() == "limit" and beside(i, r"^\s*type\s*:\s*incrementPer"))
                  or (m.group(2).lower() == "count" and beside(i, r"^\s*select\s*:\s*memory\("))):
            continue
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
        # Not "forced" (it only tells other cards whether a discard was imposed) and not "reveal" (on a move it
        # only writes a line in the game log): neither has ever changed how the card under test plays.
        if m and m.group(2).lower() in ("optional", "shuffle", "showall"):
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

            # Wider: each word dropped in turn.  Not a type word that another word of the filter already
            # implies: only companions have a signet or are unbound, only minions skirmish a companion, only
            # events have a timeword.
            squashed = [re.sub(r"\s", "", t.lower()) for t in terms]

            def implied(t):
                others = squashed[:t] + squashed[t + 1:]
                return any(o.startswith(prefixes) for o in others for word, prefixes in IMPLIED if squashed[t] == word)

            if len(terms) > 1:
                for t in range(len(terms)):
                    if implied(t):
                        continue
                    kept = ",".join(terms[:t] + terms[t + 1:])
                    add(i, "%s: '%s' dropped from %s" % (m.group(2), terms[t].strip(), value), rewritten(kept))
            # Narrower: the commonest way a filter is written too tight is with the card's own culture or with
            # "companion" where the text says neither.
            low = inner.lower()
            plain = not any(w in low for w in ("self", "bearer", "memory(", "storedculture", "choose(")) and m.group(2) != "where"
            named = {re.sub(r"[^a-z]", "", w) for w in re.split(r"[,()]", low)}
            if plain and culture and "culture(" not in low and not (named & one_culture):
                add(i, "%s: narrowed to culture(%s): %s" % (m.group(2), culture, value), rewritten(inner + ",culture(%s)" % culture))
            # A title on its own picks out every card of that title.  Where the title is borne by cards of more
            # than one type, the tight version is the type most of them have (Saruman: minions, and one follower).
            titled = re.search(r"(?i)\b(?:name|title)\(([^()]+)\)", inner)
            title_types = types_of_title(titled.group(1)) if titled else {}
            if plain and not any(w in low for w in KIND_WORDS):
                if len(title_types) > 1:
                    commonest = max(title_types, key=title_types.get)
                    add(i, "%s: narrowed to the commonest type of that title (%s): %s" % (m.group(2), commonest, value),
                        rewritten(inner + "," + commonest))
                if set(title_types) != {"companion"} and not any(o.startswith(IMPLIED[0][1]) for o in squashed):
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


FRAME = re.compile(r"^\s+at ([\w.$]+)\.([\w$<>]+)\(([\w$]+\.java):(\d+)\)")


def failures(out: str, test_class: str, test_file: Path = None) -> dict:
    """What JUnit printed about each failing test, cut down to what says why: {test name: [lines]}.  The lines are
    the exception with its message, its deepest cause where it has one, and where it was thrown: the rig's or the
    game's frames nearest the throw, and the line of the test file that led there, with what that line says.  The
    test file is found by its class's name under the server module's tests unless it is given."""
    found = {}
    test = test_file or g.SERVER / "src" / "test" / "java" / Path(*test_class.split(".")).with_suffix(".java")
    source = test.read_text(encoding="utf-8", errors="replace").split("\n") if test.is_file() else []
    blocks = re.split(r"(?m)^\d+\) (\w+)\(.*\)\n", out.split("\nFAILURES!!!")[0])
    own = test_class.rsplit(".", 1)[-1] + ".java"
    for name, block in zip(blocks[1::2], blocks[2::2]):
        lines = block.split("\n")
        first_frame = next((n for n, line in enumerate(lines) if FRAME.match(line)), len(lines))
        said = [line.strip() for line in lines[:first_frame] if line.strip()][:3]
        causes = [line.strip() for line in lines if line.startswith("Caused by: ")]
        if causes:
            said.append(causes[-1][:300])
        frames = []
        for line in lines[first_frame:]:
            m = FRAME.match(line)
            if not m:
                break  # the first cause's own frames start here
            if m.group(1).startswith(("org.junit.", "java.", "jdk.", "sun.")):
                continue
            frames.append("at %s.%s (%s:%s)" % (m.group(1).rsplit(".", 1)[-1], m.group(2), m.group(3), m.group(4)))
            if m.group(3) == own:
                if 0 < int(m.group(4)) <= len(source):
                    frames[-1] += "   " + source[int(m.group(4)) - 1].strip()[:160]
                break  # the test's own line: nothing below it says more
        if len(frames) > 4:
            frames = frames[:3] + ["..."] + frames[-1:]
        found[name] = [line[:300] for line in said] + frames
    return found


LIMIT = 180  # seconds one run of a card's tests may take (--timeout)


def without_ignores(text: str) -> str:
    """A test file with every @Ignore taken out, whether it has a line to itself or shares one with @Test.  A line
    that held nothing else is left empty, so that a failure's line number is still the line in the file itself."""
    out = []
    for line in text.split("\n"):
        if line.lstrip().startswith("@") and re.search(r"@Ignore\b", line):
            line = re.sub(r'@Ignore\b(?:[ \t]*\((?:"(?:[^"\\]|\\.)*"|[^()"])*\))?', "", line)
            line = line if line.strip() else ""
        out.append(line)
    return "\n".join(out)


def parked_tests(text: str) -> list:
    """The names of the tests that have an @Ignore among the annotations above them."""
    return [m.group(2) for m in re.finditer(r"(?m)^((?:[ \t]*(?:@\w+|//).*\n)+)[ \t]*public\s+void\s+(\w+)", text)
            if any(line.lstrip().startswith("@") and re.search(r"@Ignore\b", line) for line in m.group(1).split("\n"))]


def printed_by_tests(stdout: str) -> list:
    """What the tests themselves wrote to standard output: without JUnit's own lines (its version, its progress
    marks, everything from its "Time:" on) and the card files' loading."""
    out = []
    for line in stdout.split("\nTime: ")[0].split("\n"):
        if cardtest.NOISE.search(line) or re.match(r"(?:[EI]*\.)*(?:Locking|Unlocking) blueprint library\b", line):
            continue
        line = re.sub(r"^(?:[EI]*\.)+", "", line)  # a "." as each test starts, an "E" where one fails, on the same line
        if line.strip() and not re.fullmatch(r"[EI]+", line.strip()):
            out.append(line.rstrip())
    return out


class Ran(dict):
    """{failing test: the first lines of its failure}, with the number of tests the run counted (None where it
    printed no count) and the lines the tests wrote to standard output."""
    tests, printed = None, ()


def run_tests_in_full(cwd: Path, cp: str, test_class: str, test_file: Path = None):
    """(verdict, what to say beside it, {failing test: the first lines of its failure}); the last is a Ran."""
    try:
        proc = subprocess.run(["java", "-cp", cp, "org.junit.runner.JUnitCore", test_class], cwd=cwd, env=cardtest.env(),
                              capture_output=True, text=True, errors="replace", timeout=LIMIT)
    except subprocess.TimeoutExpired:
        return "hung", "the run did not return in %d seconds and was stopped: some test never ends" % LIMIT, Ran()
    out = proc.stdout + proc.stderr
    passed = re.search(r"^OK \((\d+) tests?\)", out, re.M)
    counted = passed or re.search(r"^Tests run: (\d+),", out, re.M)
    if passed:
        why = Ran()
    else:
        why = Ran(failures(out, test_class, test_file))
    why.tests = int(counted.group(1)) if counted else None
    why.printed = printed_by_tests(proc.stdout)
    if passed:
        return "survived", "", why
    if "CardNotFoundException" in out or "Error while loading file" in out or "InvalidCardDefinitionException" in out:
        said = re.search(r"(InvalidCardDefinitionException|CardNotFoundException|Error while loading file)[:\s]*([^\n]{0,160})", out)
        return "invalid", (said.group(1) + ": " + said.group(2).strip()) if said else "", why
    failed = re.findall(r"^\d+\) (\w+)\(", out, re.M)
    # A definition that fails to load in some other way makes every test fail before it asserts anything: when
    # every test failed and no failure is an assertion, call the change unloadable rather than noticed, and give
    # the first failure's deepest cause as the reason (a file that no longer parses says where).
    run = re.search(r"^Tests run: (\d+),\s+Failures: (\d+)", out, re.M)
    causes = re.findall(r"^\d+\) \w+\(.*\)\n([\w.$]+)", out, re.M)
    if run and run.group(1) == run.group(2) and causes and not any(
            c.endswith(("AssertionError", "ComparisonFailure", "AssertionFailedError")) for c in causes):
        first = why.get(failed[0], []) if failed else []
        reason = next((line for line in reversed(first) if line.startswith("Caused by: ")), first[0] if first else causes[0])
        return "invalid", "every test failed without an assertion: " + re.sub(r"^Caused by: ", "", reason)[:200], why
    return "noticed", ", ".join(sorted(set(failed))), why


def run_tests(cwd: Path, cp: str, test_class: str):
    return run_tests_in_full(cwd, cp, test_class)[:2]


def replace_flags(argv) -> list:
    """Where each --replace stands on the command line: a "--replace" that is itself the old or new text of the one
    before it is not one."""
    out, i = [], 1
    while i < len(argv):
        if argv[i] == "--replace":
            out.append(i)
            i += 3
        else:
            i += 1
    return out


def replace_request(argv) -> tuple:
    """(pairs, nth, every) as a command line asks for them: each --replace's old and new text, the number after
    --nth (None where there is none) and whether --every is there."""
    flags = replace_flags(argv)
    for i in flags:
        if i + 2 >= len(argv):
            raise SystemExit("--replace takes two values after it: the old text and the new.")
    inside = {n for i in flags for n in range(i, i + 3)}
    others = [a for n, a in enumerate(argv) if n not in inside]
    pairs = [(argv[i + 1], argv[i + 2]) for i in flags]
    nth = int(others[others.index("--nth") + 1]) if "--nth" in others and others.index("--nth") + 1 < len(others) else None
    every = "--every" in others
    if nth is not None and (every or len(pairs) > 1):
        raise SystemExit("--nth goes with a single --replace: several are each made in the first place their old text "
                         "occurs, or in every place with --every.")
    return pairs, nth, every


def changed_definition(raw: str, card_id: str, pairs, nth=None, every=False) -> tuple:
    """One mistake, or one correction, however many changes it is made of: each pair in turn, in the definition as
    the pairs before it left it.  A place is counted within the definition (`raw` is the card's own block).
    Returns (the definition changed, the first line a change stands on counted from 0, what to say of each
    change).  applyfix.py writes a correction into the card's file by this same function, so that what was tried
    is what is applied."""
    changed, line, said = raw, None, []
    for old_text, new_text in pairs:
        if not old_text:
            raise SystemExit("--replace: the old text is empty.")
        if every:
            times = changed.count(old_text)
            if not times:
                raise SystemExit("'%s' is not in the definition of %s." % (old_text, card_id))
            at = changed.find(old_text)
            after = changed.replace(old_text, new_text)
            said.append("'%s' becomes '%s'%s" % (old_text, new_text, " (all %d occurrences)" % times if times > 1 else ""))
        else:
            at = -1
            for _ in range(nth or 1):
                at = changed.find(old_text, at + 1)
                if at < 0:
                    raise SystemExit("'%s' is not in the definition of %s %d time(s)%s." % (
                        old_text, card_id, nth or 1, " once the changes before it are made" if said else ""))
            after = changed[:at] + new_text + changed[at + len(old_text):]
            said.append("'%s' becomes '%s'%s" % (old_text, new_text, " (occurrence %d)" % nth if (nth or 1) > 1 else ""))
        line = changed[:at].count("\n") if line is None else min(line, changed[:at].count("\n"))
        changed = after
    return changed, line, said


def main(argv):
    global LIMIT
    skip = set()
    with_parked = "--with-parked" in argv
    printed = "--printed" in argv
    for i in replace_flags(argv):
        if i + 2 >= len(argv):
            raise SystemExit("--replace takes two values after it: the old text and the new.")
        skip.update(range(i, i + 3))
    for flag in ("--out", "--nth", "--timeout", "--test"):
        if flag in argv and argv.index(flag) not in skip:
            if argv.index(flag) + 1 >= len(argv):
                raise SystemExit("%s takes a value after it." % flag)
            skip.update((argv.index(flag), argv.index(flag) + 1))
    if "--timeout" in argv:
        LIMIT = int(argv[argv.index("--timeout") + 1])
    args = [a for n, a in enumerate(argv) if n > 0 and n not in skip and not a.startswith("--")]
    out_name = argv[argv.index("--out") + 1] if "--out" in argv else "default"
    if not args:
        print(__doc__)
        return 0
    card_id = args[0]
    card = g.card_index().get(card_id)
    test = g.test_index().get(card_id)
    other_test = "--test" in argv
    if other_test:
        # Another file's tests in place of the card's own: a mechanics class, or a test of this card's definition
        # that is kept in some other card's file.
        named = argv[argv.index("--test") + 1]
        test = Path(named).resolve() if Path(named).is_file() else g.REPO / named
        if not named.endswith(".java") or not test.is_file():
            raise SystemExit("--test takes the path of a test's .java file, from here or from the repository's root: "
                             "%s is not one." % named)
        if card is None:
            raise SystemExit("%s has no definition." % card_id)
    elif card is None or test is None:
        raise SystemExit("%s needs both a definition and a test file (or another file's tests, with --test)." % card_id)
    file_text = card.file.read_text(encoding="utf-8")
    file_lines = file_text.replace("\r", "").split("\n")
    first = card.line - 1
    card_lines = card.raw.split("\n")
    todo = mistakes(card_lines, (card.field("culture", "") or "").strip().lower())
    if "--replace" in argv:
        # One mistake, however many changes it is made of.
        pairs, nth, every = replace_request(argv)
        changed, line, said = changed_definition(card.raw, card_id, pairs, nth, every)
        todo = [(line, " and ".join(said), changed.split("\n"))]
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
        # A copy of the test file with its @Ignores taken out, so that the parked tests run too and a failure's
        # line number is still the line in the file itself.
        source = base / "src" / test.name
        source.parent.mkdir(parents=True)
        source.write_text(without_ignores(test.read_text(encoding="utf-8")), encoding="utf-8")
    javac = subprocess.run(["javac", "-nowarn", "-proc:none", "-encoding", "UTF-8", "-d", str(classes), "-cp", cp, str(source)],
                           cwd=g.SERVER, env=cardtest.env(), capture_output=True, text=True)
    if javac.returncode != 0:
        print(javac.stdout + javac.stderr)
        raise SystemExit("The test file does not compile.")
    test_class = g.test_class_name(test)

    verdict, failing, why = run_tests_in_full(g.SERVER, cp, test_class, test)

    def say_why(why, only=None):
        """The first lines of each failing test's failure, indented under the line that named the tests."""
        for name in sorted(why):
            if only is None or name in only:
                print("    %s: %s" % (name, why[name][0] if why[name] else "(no message)"))
                for line in why[name][1:]:
                    print("        " + line)

    def say_printed(why):
        """With --printed, what the tests of one run wrote to standard output."""
        if printed:
            print("    printed by the tests: %s" % ("" if why.printed else "nothing"))
            for line in why.printed:
                print("        " + line)

    def all_of(why):
        """"all 9 tests", or "every test" where the run printed no count."""
        return "every test" if why.tests is None else "the 1 test" if why.tests == 1 else "all %d tests" % why.tests

    parked = parked_tests(test.read_text(encoding="utf-8")) if with_parked else []
    if with_parked and verdict == "hung":
        print("With the parked tests switched on, against the card as it is: %s (a parked test whose failure is a hang is "
              "written @Test(timeout = 20000), which fails where this one hangs)" % failing)
    elif with_parked:
        print("With the parked tests switched on, against the card as it is: %s"
              % ("%s pass%s" % (all_of(why), "es" if why.tests == 1 else "") if verdict == "survived" else "failing: " + failing))
        say_why(why)
        say_printed(why)
    elif verdict == "hung":
        raise SystemExit("Against the card as it is, %s; fix that first." % failing)
    elif verdict != "survived":
        raise SystemExit("The tests do not pass against the card as it is; fix that first.")

    def one(n):
        i, what, new_card = todo[n]
        new_file = "\n".join(file_lines[:first] + new_card + file_lines[first + len(card_lines):])
        cwd = sandbox(base / ("m%03d" % n), card, new_file)
        verdict, failed, why = run_tests_in_full(cwd, cp, test_class, test)
        shutil.rmtree(base / ("m%03d" % n), ignore_errors=True)
        return n, verdict, failed, why

    results, whys = {}, {}
    with concurrent.futures.ThreadPoolExecutor(max_workers=max(1, (os.cpu_count() or 2))) as pool:
        for n, verdict, failed, why in pool.map(one, range(len(todo))):
            results[n] = (verdict, failed)
            whys[n] = why

    # A change of the caller's own with the parked tests on is a correction: what the tests do under it is reported
    # below, and a "SURVIVED" over a correction every test passes would read as a failure of it.
    as_correction = with_parked and "--replace" in argv
    if as_correction:
        print("%s  %s: a correction tried in the definition%s" % (card_id, g.full_name(card),
                                                                  ", and the tests of %s run against it" % test.name if other_test else ""))
    else:
        print("%s  %s: %d mistakes made in the definition%s" % (card_id, g.full_name(card), len(todo),
                                                               ", and the tests of %s run against each" % test.name if other_test else ""))
    counts = {"noticed": 0, "survived": 0, "invalid": 0, "hung": 0}
    for n, (i, what, _) in enumerate(todo):
        verdict, failed = results[n]
        counts[verdict] += 1
        if as_correction:
            continue
        if verdict == "survived":
            print("  SURVIVED  line %d: %s" % (card.line + i, what))
        elif "--all" in argv or with_parked or printed or "--replace" in argv:
            # One mistake of the caller's own: which tests noticed is what was asked, with or without --all.
            print("  %-9s line %d: %s%s" % (verdict, card.line + i, what, "  (" + failed + ")" if failed else ""))
        if not with_parked:
            say_printed(whys[n])
    if not as_correction:
        print("%d noticed, %d survived, %d made the card unloadable%s" % (counts["noticed"], counts["survived"], counts["invalid"],
                                                                           ", %d never returned" % counts["hung"] if counts["hung"] else ""))
    if with_parked:
        # Read as a correction: what the parked tests do under it is the point.
        for n, (i, what, _) in enumerate(todo):
            verdict, failed = results[n]
            failed_set = set(x.strip() for x in failed.split(",") if x.strip())
            if verdict == "invalid":
                print("As a correction (line %d): the card no longer loads%s." % (card.line + i, ": " + failed if failed else ""))
                continue
            if verdict == "hung":
                print("As a correction (line %d): %s." % (card.line + i, failed))
                continue
            now_pass = [t for t in parked if t not in failed_set]
            still = [t for t in parked if t in failed_set]
            active = sorted(failed_set - set(parked))
            if verdict == "survived":
                # One line with nothing under it: it says how many tests ran, so as not to read as a run that never happened.
                print("As a correction (line %d): %s pass%s%s (only a failing test is listed)."
                      % (card.line + i, all_of(whys[n]), "es" if whys[n].tests == 1 else "",
                         ", the %d parked among them" % len(parked) if parked else "; none of them is parked"))
                say_printed(whys[n])
            else:
                print("As a correction (line %d): parked tests passing: %s; parked tests still failing: %s; active tests failing: %s"
                      % (card.line + i, ", ".join(now_pass) or "none", ", ".join(still) or "none", ", ".join(active) or "none"))
                say_why(whys[n], failed_set)
                say_printed(whys[n])
    shutil.rmtree(base, ignore_errors=True)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
