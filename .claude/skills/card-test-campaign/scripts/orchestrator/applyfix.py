"""Run a correction script written for mutate.py --replace, but write the change into the definition instead.
    python3 applyfix.py <script.py> [script args...]
The script's mutate.py call is intercepted, however it is made (subprocess.run, call, check_call, check_output or
Popen; os.system), and what it asks mutate.py to try is made in the card's file instead: EVERY --replace pair, one
after another, with --nth or --every as given.  The change is worked out by mutate.py's own function, so a place is
counted within the card's definition as mutate.py counts it, and what is applied is what was tried.  Lines outside
the card's block are left byte for byte as they were (CRLF kept).  "APPLIED [ids]" is printed when the script ends,
whether it returns, calls sys.exit() or fails."""
import os
import re
import runpy
import shlex
import subprocess
import sys

# The scripts of the tree this file stands in (a batch may be working in a second tree).
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import gempcards as g
import mutate

done = []


def mutate_argv(cmd):
    """mutate.py's own command line out of a command that runs it (a list, or one string for a shell), or None for
    any other command and for a mutate.py run that makes no --replace."""
    try:
        if isinstance(cmd, (str, bytes)):
            cmd = shlex.split(cmd.decode() if isinstance(cmd, bytes) else cmd)
        cmd = [x if isinstance(x, str) else os.fspath(x) for x in cmd]
    except (TypeError, ValueError):  # not a command line this can read: none of mutate.py's
        return None
    at = next((n for n, x in enumerate(cmd) if isinstance(x, str) and x.endswith("mutate.py")), None)
    if at is None or "--replace" not in cmd[at:]:
        return None
    return cmd[at:]


def apply(argv) -> str:
    """Makes in the card's file the change that this mutate.py command line would try, and says what it did."""
    pairs, nth, every = mutate.replace_request(argv)
    inside = {n for i in mutate.replace_flags(argv) for n in range(i, i + 3)}
    takes_value = {n + 1 for n, a in enumerate(argv) if n not in inside and a in ("--out", "--nth", "--timeout", "--test")}
    ids = [a for n, a in enumerate(argv) if n > 0 and n not in inside and n not in takes_value and not a.startswith("--")]
    if not ids:
        raise SystemExit("applyfix: the mutate.py command names no card: %s" % " ".join(argv))
    card_id = ids[0]
    card = g.card_index().get(card_id)
    if card is None:
        raise SystemExit("applyfix: %s has no definition." % card_id)
    changed, line, said = mutate.changed_definition(card.raw, card_id, pairs, nth, every)
    raw = open(card.file, "rb").read().decode("utf-8")
    pieces = re.split(r"(?<=\n)", raw)  # each line with its own ending
    first, count = card.line - 1, len(card.raw.split("\n"))
    block = pieces[first:first + count]
    if "\n".join(p.rstrip("\r\n") for p in block) != card.raw:
        raise SystemExit("applyfix: %s is not where its file was read to have it (line %d of %s); nothing written."
                         % (card_id, card.line, card.file))
    ending = "\r\n" if block[0].endswith("\r\n") else "\n"
    last = block[-1][len(block[-1].rstrip("\r\n")):]  # the block's last line ends as it did (a file's last line may not)
    new = changed.split("\n")
    new = [x + ending for x in new[:-1]] + [new[-1] + last]
    open(card.file, "wb").write("".join(pieces[:first] + new + pieces[first + count:]).encode("utf-8"))
    g._card_index = None  # the file's cards are read again: the lines below this one have moved
    done.append(card_id)
    return "applied to %s (%s line %d): %s\n" % (card_id, g.rel(card.file), card.line + line, " and ".join(said))


def as_asked(text: str, k: dict):
    """The message as str or bytes, whichever the caller's keywords would have had the output be."""
    return text if (k.get("text") or k.get("universal_newlines") or k.get("encoding") or k.get("errors")) else text.encode("utf-8")


def captures(k: dict) -> bool:
    return bool(k.get("capture_output")) or k.get("stdout") == subprocess.PIPE


real_run, real_call, real_check_call, real_check_output = subprocess.run, subprocess.call, subprocess.check_call, subprocess.check_output
real_popen, real_system = subprocess.Popen, os.system


def fake_run(cmd, *a, **k):
    argv = mutate_argv(cmd)
    if argv is None:
        return real_run(cmd, *a, **k)
    said = apply(argv)
    if not captures(k):
        sys.stdout.write(said)
    return subprocess.CompletedProcess(cmd, 0, as_asked(said, k) if captures(k) else None,
                                       as_asked("", k) if (k.get("capture_output") or k.get("stderr") == subprocess.PIPE) else None)


def fake_call(cmd, *a, **k):
    argv = mutate_argv(cmd)
    if argv is None:
        return real_call(cmd, *a, **k)
    sys.stdout.write(apply(argv))
    return 0


def fake_check_call(cmd, *a, **k):
    argv = mutate_argv(cmd)
    if argv is None:
        return real_check_call(cmd, *a, **k)
    sys.stdout.write(apply(argv))
    return 0


def fake_check_output(cmd, *a, **k):
    argv = mutate_argv(cmd)
    if argv is None:
        return real_check_output(cmd, *a, **k)
    return as_asked(apply(argv), k)


def fake_system(cmd):
    argv = mutate_argv(cmd)
    if argv is None:
        return real_system(cmd)
    sys.stdout.write(apply(argv))
    return 0


class FakePopen(real_popen):
    """A mutate.py command is applied and stands as a process that has already ended well; anything else is run."""

    def __init__(self, cmd, *a, **k):
        argv = mutate_argv(cmd)
        if argv is None:
            super().__init__(cmd, *a, **k)
            return
        said = apply(argv)
        if not captures(k):
            sys.stdout.write(said)
        # A process that prints the same line and ends, so that communicate(), wait() and the pipes all work.
        super().__init__([sys.executable, "-c", "import sys; sys.stdout.write(sys.argv[1])", said if captures(k) else ""], *a, **k)


subprocess.run, subprocess.call, subprocess.check_call, subprocess.check_output = fake_run, fake_call, fake_check_call, fake_check_output
subprocess.Popen, os.system = FakePopen, fake_system

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] in ("-h", "--help"):
        print(__doc__)
        sys.exit(0 if len(sys.argv) > 1 else 1)
    script = sys.argv[1]
    sys.argv = sys.argv[1:]
    try:
        runpy.run_path(script, run_name="__main__")
    finally:
        print("APPLIED", done)
