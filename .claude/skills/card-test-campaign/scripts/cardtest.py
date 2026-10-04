#!/usr/bin/env python3
"""Compiles and runs card tests without going through Maven each time.

    cardtest.py setup                    builds every module once and records the classpath (a minute or so)
    cardtest.py run <what...>            compiles just those test files and runs them (a few seconds)
    cardtest.py suite [cards|all|<pkg>]  recompiles all the tests and runs a whole tree of them
    cardtest.py probe <file.java>        compiles and runs a throwaway JUnit class kept anywhere (/tmp, say), for
                                         finding out what the game does without adding a test to the repository

<what> is a blueprint id (1_40), a test file's path, or a selector (set:1, file:set01-Elven, seg:vset).

`run` compiles into a folder of its own (--out <name>, default "default"), so several people or agents can each
run their own cards at once without treading on each other; give each a different --out.  It needs Java 21 and a
finished `setup`.  Card definitions are read from the source tree as each test starts, so an edited .hjson file
takes effect at once; an edit to engine or rig source needs `setup` (or `mvn -q compile` in that module) again.
"""
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

import gempcards as g

TARGET = g.SERVER / "target"
CP_FILE = TARGET / "cardtest.classpath"
NOISE = re.compile(r"Loaded JSON card file|Picked up JAVA_TOOL_OPTIONS|^JUnit version|^SLF4J|^WARNING: .*(agent|Mockito|instrument)|"
                   r"^OpenJDK 64-Bit Server VM warning|^WARNING: A Java agent|^WARNING: If a serviceability|"
                   r"^WARNING: Dynamic loading")


def env():
    e = dict(os.environ)
    e.pop("JAVA_TOOL_OPTIONS", None)
    return e


def classpath(out_dir=None) -> str:
    if not CP_FILE.is_file():
        raise SystemExit("No classpath yet.  Run: cardtest.py setup")
    deps = CP_FILE.read_text().strip().split(os.pathsep)
    # Use each sibling module's freshly compiled classes rather than the jar Maven installed, so that a
    # `mvn compile` in a module is enough for a change there to be picked up.
    local = []
    for module in ("gemp-lotr-logic", "gemp-lotr-common", "gemp-lotr-cards"):
        classes = g.MODULES / module / "target" / "classes"
        if classes.is_dir():
            local.append(str(classes))
            deps = [d for d in deps if ("/" + module + "/") not in d.replace(os.sep, "/")]
    parts = ([str(out_dir)] if out_dir else []) + [str(TARGET / "test-classes"), str(TARGET / "classes")] + local + deps
    return os.pathsep.join(parts)


def setup():
    subprocess.check_call(["mvn", "-q", "-B", "-DskipTests", "install"], cwd=g.MODULES)
    TARGET.mkdir(exist_ok=True)
    subprocess.check_call(["mvn", "-q", "-B", "dependency:build-classpath", "-Dmdep.outputFile=" + str(CP_FILE)],
                          cwd=g.SERVER)
    print("Built.  Classpath written to " + g.rel(CP_FILE))


def run_junit(classes, cp) -> int:
    if not classes:
        print("Nothing to run.")
        return 0
    proc = subprocess.Popen(["java", "-cp", cp, "org.junit.runner.JUnitCore"] + classes, cwd=g.SERVER, env=env(),
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors="replace")
    for line in proc.stdout:
        if NOISE.search(line):
            continue
        # A stack trace is useful down to the test's own frame and not below it.
        if re.match(r"^\s+at (org\.junit|java\.base|jdk\.internal|sun\.reflect)", line):
            continue
        sys.stdout.write(line)
    return proc.wait()


def resolve_files(what) -> list:
    tests = g.test_index()
    files = []
    for w in what:
        p = Path(w)
        if w.endswith(".java") and (p.is_file() or (g.REPO / w).is_file()):
            files.append(p if p.is_file() else g.REPO / w)
            continue
        for card_id in g.resolve_ids([w]):
            if card_id in tests:
                files.append(tests[card_id])
            else:
                print("note: %s has no test file" % card_id, file=sys.stderr)
    return files


def run(argv) -> int:
    out_name = "default"
    if "--out" in argv:
        i = argv.index("--out")
        out_name = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    files = resolve_files(argv)
    if not files:
        print("No test files matched.")
        return 1
    out_dir = TARGET / "cardtest" / out_name
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True)
    cp = classpath(out_dir)
    javac = subprocess.run(["javac", "-nowarn", "-proc:none", "-encoding", "UTF-8", "-d", str(out_dir), "-cp", cp]
                           + [str(f) for f in files], cwd=g.SERVER, env=env(), capture_output=True, text=True)
    if javac.returncode != 0:
        sys.stdout.write("\n".join(l for l in (javac.stdout + javac.stderr).split("\n") if not NOISE.search(l)))
        print("\nDid not compile.")
        return 2
    return run_junit([g.test_class_name(f.resolve()) for f in files], cp)


def probe(argv) -> int:
    """Compiles and runs any JUnit class, wherever its file is: for trying something out without adding a test."""
    out_name = "probe"
    if "--out" in argv:
        i = argv.index("--out")
        out_name = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    files = [Path(a).resolve() for a in argv]
    out_dir = TARGET / "cardtest" / (out_name + "-probe")
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True)
    cp = classpath(out_dir)
    javac = subprocess.run(["javac", "-nowarn", "-proc:none", "-encoding", "UTF-8", "-d", str(out_dir), "-cp", cp]
                           + [str(f) for f in files], cwd=g.SERVER, env=env(), capture_output=True, text=True)
    if javac.returncode != 0:
        sys.stdout.write("\n".join(l for l in (javac.stdout + javac.stderr).split("\n") if not NOISE.search(l)))
        print("\nDid not compile.")
        return 2
    classes = []
    for f in files:
        package = re.search(r"^\s*package\s+([\w.]+)\s*;", f.read_text(encoding="utf-8"), re.M)
        classes.append((package.group(1) + "." if package else "") + f.stem)
    return run_junit(classes, cp)


def suite(argv) -> int:
    which = argv[0] if argv else "cards"
    subprocess.check_call(["mvn", "-q", "-B", "-o", "test-compile"], cwd=g.SERVER, env=env())
    root = TARGET / "test-classes"
    if which == "all":
        base = root
    elif which == "cards":
        base = root / "com" / "gempukku" / "lotro" / "cards"
    else:
        base = root / Path(*which.split("."))
    classes, empty = [], []
    for p in sorted(base.rglob("*.class")):
        if "$" in p.name:
            continue
        if not re.search(r"(Tests?|AtTest)$", p.stem):
            continue
        if p.stem.startswith("Abstract"):
            continue
        source = (g.SERVER / "src" / "test" / "java" / p.relative_to(root)).with_suffix(".java")
        if source.is_file() and not re.search(r"^\s*@(Test|ParameterizedTest)\b", source.read_text(encoding="utf-8", errors="replace"), re.M):
            empty.append(source.name)
            continue
        classes.append(".".join(p.relative_to(root).with_suffix("").parts))
    print("%d test classes under %s" % (len(classes), which))
    if empty:
        print("%d classes have no active test and were left out: %s" % (len(empty), ", ".join(empty)))
    return run_junit(classes, classpath())


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    if argv[1] == "setup":
        setup()
        return 0
    if argv[1] == "run":
        return run(argv[2:])
    if argv[1] == "probe":
        return probe(argv[2:])
    if argv[1] == "suite":
        return suite(argv[2:])
    print(__doc__)
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
