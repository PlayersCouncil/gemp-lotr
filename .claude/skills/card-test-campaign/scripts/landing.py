#!/usr/bin/env python3
"""The lander's tools: numbering a question or a rig request into its register, and finishing a batch.

    landing.py question add "<title>" <body file> [--id F-0262]
        Files a question for Ketura at the top of "## Open" in questions.md and prints its number.  The title names
        the card and the doubt ("Gríma, Wormtongue 54_154: who is \"that companion\"?"); the body file holds the
        card's words, the readings, and what each would mean.  With --id the entry is headed by a finding's number
        (a candidate finding she is asked about) and no Q- number is taken.

    landing.py rig add "<title>" <body file>
        Files a rig request at the end of "## Open" in rig-requests.md and prints its number (G-0000).  The body
        says which batch and card it came from, what the test needed, and the method proposed.  The heading is
        ended with ": open" here: the title says what is wanted and nothing of its state.

    landing.py users <id...>
        Lists the other cards whose tests put one of these cards on the table as a helper.  A correction to a
        definition can break them: run their tests after applying one (finish does, with --changed).  A card the
        rig keeps under a constant (TestConstants: the Rings, the Ring-bearers, the Maps, the site paths) is found
        by the constant's name as well as by its id, and where it is what a table has when the test names none
        (Frodo 10_121, The Ruling Ring, the King-block sites), in every file that builds such a table.

    landing.py finish <folder> <id...> [--with <path>]... [--changed <id,id>] [--note "<a line for the commit>"]
        Runs the gate on the cards and commits the ones that clear it as one commit (land.py --batch), then commits
        whatever the lander changed under .claude/skills as "Skills: what <folder> turned up", rebuilding the
        rulings index first if the rulings, the shapes or the pitfalls changed.  Prints what landed and the head.
        --changed names every card whose DEFINITION was corrected (batch cards and others alike): their files
        go into the commit, and before anything is committed the tests of every other card that uses one of
        them as a helper are run; if any fails nothing is committed.  --with is for any other file.

    landing.py commit "<subject>" <path...> [--body-file <file>]
        For work that is not a batch of cards (an engine or rig change with its tests and documents): commits
        exactly the paths named, as one commit with the campaign's trailer, rebuilding the rulings and pitfall
        indexes first if a file they are made from is among them.  Prints the head.

Everything here holds the campaign's lock while it writes, so two landers can work at once.
"""
import os
import re
import subprocess
import sys
from pathlib import Path

import gempcards as g

HERE = Path(__file__).resolve().parent
WORK = Path(os.environ.get("GEMP_WORK", "/home/claude/work"))
REFERENCES = g.SKILLS / "card-test-campaign" / "references"


def git(*args):
    return subprocess.run(["git"] + list(args), cwd=g.REPO, capture_output=True, text=True)


def script(name, *args):
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE="1")
    return subprocess.run([sys.executable, str(HERE / name)] + list(args), capture_output=True, text=True, env=env)


def trailer() -> str:
    path = os.environ.get("GEMP_COMMIT_TRAILER") or str(WORK / "trailer.txt")
    if os.path.isfile(path):
        os.environ["GEMP_COMMIT_TRAILER"] = path
        return "\n" + open(path, encoding="utf-8").read().strip() + "\n"
    return ""


def highest(letter: str, *texts) -> int:
    return max([int(n) for t in texts for n in re.findall(r"\b%s-(\d{4})\b" % letter, t)] or [0])


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def take(argv, flag):
    if flag not in argv:
        return None
    i = argv.index(flag)
    value = argv[i + 1]
    del argv[i:i + 2]
    return value


def question_add(argv):
    given = take(argv, "--id")
    title, body_file = argv[0], Path(argv[1])
    body = body_file.read_text(encoding="utf-8").strip()
    path = g.state_dir() / "questions.md"
    text = read(path)
    if "## Open" not in text:
        raise SystemExit("questions.md has no '## Open' heading")
    if given:
        number = given
        if re.search(r"^### %s\b" % re.escape(number), text, re.M):
            raise SystemExit("%s already has an entry in questions.md" % number)
    else:
        number = "Q-%04d" % (1 + highest("Q", text, read(REFERENCES / "rulings-log.md")))
    head, rest = text.split("## Open", 1)
    rest = re.sub(r"\A\s*\(none\)\s*", "\n\n", rest)
    entry = "\n\n### %s %s\n\n%s\n" % (number, title.strip(), body)
    path.write_text(head + "## Open" + entry + "\n" + rest.lstrip("\n"), encoding="utf-8")
    print(number)


def rig_add(argv):
    title, body_file = argv[0], Path(argv[1])
    body = body_file.read_text(encoding="utf-8").strip()
    path = g.state_dir() / "rig-requests.md"
    text = read(path)
    opened = re.search(r"^## Open\s*$", text, re.M)
    following = re.search(r"^## ", text[opened.end():], re.M) if opened else None
    if not opened or not following:
        raise SystemExit("rig-requests.md has no '## Open' section followed by another")
    cut = opened.end() + following.start()   # the end of the Open section, whatever heading comes next
    number = "G-%04d" % (1 + highest("G", text))
    entry = "### %s %s: open\n\n%s\n\n" % (number, re.sub(r"(?i)(?:\s*:\s*open)+\s*$", "", title.strip()).rstrip(": "), body)
    path.write_text(text[:cut].rstrip("\n") + "\n\n" + entry + text[cut:], encoding="utf-8")
    print(number)


def rig_constants() -> tuple:
    """({blueprint id: the names of the rig's constants that hold it}, {a table's argument, counted from 0: the
    constant it takes when the test gives none}).  TestConstants keeps the Rings, the Ring-bearers and the Maps as
    one id each and the site paths as maps of nine; VirtualTableScenario's constructor says which of them stand in
    for an argument that is left out or null."""
    held = {}
    constants = (g.FRAMEWORK_DIR / "TestConstants.java").read_text(encoding="utf-8", errors="replace") \
        if (g.FRAMEWORK_DIR / "TestConstants.java").is_file() else ""
    for name, card_id in re.findall(r'\bString\s+(\w+)\s*=\s*"(\d+_\d+)"\s*;', constants):
        held.setdefault(card_id, []).append(name)
    for name, body in re.findall(r"\bHashMap<String,\s*String>\s+(\w+)\s*=\s*new\s+HashMap<>\(\)\s*\{\{(.*?)\}\}", constants, re.S):
        for card_id in re.findall(r'put\(\s*"[^"]*"\s*,\s*"(\d+_\d+)"\s*\)', body):
            held.setdefault(card_id, []).append(name)
    table = (g.FRAMEWORK_DIR / "VirtualTableScenario.java").read_text(encoding="utf-8", errors="replace") \
        if (g.FRAMEWORK_DIR / "VirtualTableScenario.java").is_file() else ""
    place = {"siteIDs": 1, "ringBearerID": 2, "ringID": 3}
    defaults = {place[arg]: name for arg, name in
                re.findall(r"if\s*\(\s*(siteIDs|ringBearerID|ringID)\s*==\s*null\s*\)\s*\{\s*\1\s*=\s*(\w+)\s*;", table)}
    return held, defaults


def table_arguments(text: str) -> list:
    """The arguments of each `new VirtualTableScenario(...)` in a test file, as written."""
    code = re.sub(r'"(?:[^"\\\n]|\\.)*"', '""', text)
    code = re.sub(r"//[^\n]*", "", code)
    out = []
    for m in re.finditer(r"\bnew\s+VirtualTableScenario\s*\(", code):
        depth, args, cur = 1, [], ""
        for c in code[m.end():]:
            if c in "([{":
                depth += 1
            elif c in ")]}":
                depth -= 1
                if depth == 0:
                    break
            if c == "," and depth == 1:
                args.append(cur.strip())
                cur = ""
            else:
                cur += c
        out.append(args + [cur.strip()])
    return out


def users(ids, how=None) -> list:
    """The cards, other than these, whose test files put one of these blueprints on a table: named by its id in a
    scenario's card map, by the rig's constant for it (VirtualTableScenario.GreatRing is 19_1), or, where it is
    what a table has when the test names none, by building such a table.  `how`, if given, is filled with the
    constants and defaults that were looked for."""
    held, defaults = rig_constants()
    names = sorted({name for i in ids for name in held.get(i, [])})
    left_out = sorted(at for at, name in defaults.items() if name in names)
    if how is not None:
        how += ["%s is the rig's %s" % (i, " and ".join(held[i])) for i in ids if i in held]
        how += ["a table that names no %s has %s" % ({1: "sites", 2: "Ring-bearer", 3: "Ring"}[at], defaults[at]) for at in left_out]
    wanted = re.compile(r'"(?:%s)"' % "|".join(re.escape(i) for i in ids) + ("|\\b(?:%s)\\b" % "|".join(names) if names else ""))
    out = []
    for card_id, path in g.test_index().items():
        if card_id in ids:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if wanted.search(text) or (left_out and any(len(args) <= at or args[at] == "null"
                                                    for args in table_arguments(text) for at in left_out)):
            out.append(card_id)
    return sorted(out, key=g.id_sort_key)


def finish(argv):
    note = take(argv, "--note")
    changed_cards = [c for c in (take(argv, "--changed") or "").split(",") if c]
    extra = []
    while "--with" in argv:
        extra.append(take(argv, "--with"))
    folder, ids = argv[0], argv[1:]
    tail = trailer()
    # A corrected definition is committed with the batch whether or not --with names its file.
    for card_id in changed_cards:
        card = g.card_index().get(card_id)
        if card is None:
            print("NOT LANDED: --changed names %s, which is no card" % card_id)
            return 2
        path = g.rel(Path(card.file))
        if path not in extra:
            extra.append(path)
    if changed_cards:
        others = [c for c in users(changed_cards) if c not in ids]
        if others:
            run = script("cardtest.py", "run", *others, "--out", folder + "-users")
            ok = re.search(r"^OK \((\d+) tests?\)", run.stdout, re.M)
            if not ok:
                failed = re.findall(r"^\d+\) (\w+)\(([\w.]+)\)", run.stdout, re.M)
                print("NOT LANDED: the correction to %s breaks tests of other cards that use it as a helper:" % ", ".join(changed_cards))
                for test, cls in failed[:20]:
                    print("  %s in %s" % (test, cls.rsplit(".", 1)[-1]))
                if not failed:
                    print("\n".join(run.stdout.strip().split("\n")[-15:]))
                return 2
            print("helper users of %s: %d card(s), %s tests pass" % (", ".join(changed_cards), len(others), ok.group(1)))
    args = list(ids) + ["--batch", folder]
    for path in extra:
        args += ["--with", path]
    if note:
        args += ["--note", note]
    landed = script("land.py", *args)
    print((landed.stdout + landed.stderr).strip())

    # What the lander wrote into the skills (pitfalls, shapes, helper rows, the standard) goes in a commit of its own.
    changed = [line[3:].strip() for line in git("status", "--porcelain", "--", ".claude/skills").stdout.split("\n") if line.strip()]
    if any(re.search(r"rulings-log\.md|shapes\.md|pitfalls\.md|gemp-testing/SKILL\.md|test-generation/SKILL\.md", c) for c in changed):
        index = script("lookup.py", "--index")
        if index.returncode != 0:
            print("lookup.py --index: " + (index.stderr or index.stdout).strip()[-300:])
        changed = [line[3:].strip() for line in git("status", "--porcelain", "--", ".claude/skills").stdout.split("\n") if line.strip()]
    if changed:
        git("add", "--", *changed)
        message = "Skills: what %s turned up\n\n%s\n%s" % (folder, "\n".join(sorted(os.path.basename(c) for c in changed)), tail)
        done = git("commit", "-q", "-m", message, "--", *changed)
        print("skills: " + ("committed %d file(s)" % len(changed) if done.returncode == 0 else "git: " + (done.stderr or done.stdout).strip()))
    print("head " + git("rev-parse", "--short=9", "HEAD").stdout.strip())
    return landed.returncode


def commit(argv):
    body_file = take(argv, "--body-file")
    subject, paths = argv[0], argv[1:]
    if not paths:
        raise SystemExit("commit: name the paths to commit")
    if any(re.search(r"rulings-log\.md|shapes\.md|pitfalls\.md", p) for p in paths):
        script("lookup.py", "--index")
        # The rulings index is made from the rulings and the shapes; the pitfall index, which stands in the
        # gemp-testing skill, from the pitfalls alone.  Each is committed only with what it is made from, so that
        # another agent's unfinished changes to that skill are not swept into this commit.
        made = [".claude/skills/card-test-campaign/references/rulings-index.md"] if any(re.search(r"rulings-log\.md|shapes\.md", p) for p in paths) else []
        if any("pitfalls.md" in p for p in paths):
            made.append(".claude/skills/gemp-testing/SKILL.md")
        paths += [m for m in made if m not in paths]
    body = ("\n\n" + Path(body_file).read_text(encoding="utf-8").strip()) if body_file else ""
    added = git("add", "--", *paths)
    if added.returncode != 0:
        print("git add: " + added.stderr.strip())
        return 1
    if not git("diff", "--cached", "--quiet", "--", *paths).returncode:
        print("nothing to commit in those paths")
        return 1
    done = git("commit", "-q", "-m", subject.strip() + body + "\n" + trailer(), "--", *paths)
    if done.returncode != 0:
        print("git: " + (done.stderr or done.stdout).strip())
        return 1
    print("committed %d path(s); head %s" % (len(paths), git("rev-parse", "--short=9", "HEAD").stdout.strip()))
    return 0


def main(argv):
    # Help wherever it is asked for: "finish --help" once landed a batch under the name "--help".
    if len(argv) < 3 or any(a in ("-h", "--help") for a in argv[1:]):
        print(__doc__)
        return 0
    if argv[1] in ("finish", "commit") and argv[2].startswith("-"):
        print("%s: the first word after it is the %s, and '%s' is an option" % (argv[1], "folder" if argv[1] == "finish" else "subject", argv[2]))
        return 1
    args = list(argv[3:])
    with g.locked():
        if argv[1:3] == ["question", "add"]:
            question_add(args)
        elif argv[1:3] == ["rig", "add"]:
            rig_add(args)
        elif argv[1] == "finish":
            return finish(list(argv[2:]))
        elif argv[1] == "commit":
            return commit(list(argv[2:]))
        elif argv[1] == "users":
            how = []
            print(" ".join(users(argv[2:], how)) or "(none)")
            if how:
                print("(%s)" % "; ".join(how))
        else:
            print(__doc__)
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
