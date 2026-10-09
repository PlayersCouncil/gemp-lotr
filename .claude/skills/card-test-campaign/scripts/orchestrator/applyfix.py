"""Run a correction script written for mutate.py --replace, but write the change into the definition instead.
    python3 applyfix.py <script.py> [script args...]
The script's mutate.py call is intercepted: its card, old text, new text and --nth are applied to the card's file
(CRLF kept), within the card's own block."""
import subprocess, sys, runpy
import os
# The scripts of the tree this file stands in (a batch may be working in a second tree).
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import gempcards as g
done = []
def fake_run(cmd, *a, **k):
    if any(str(x).endswith("mutate.py") for x in cmd):
        i = cmd.index("--replace"); card = cmd[2]; old, new = cmd[i + 1], cmd[i + 2]
        nth = int(cmd[cmd.index("--nth") + 1]) if "--nth" in cmd else None
        c = g.card_index()[card]
        raw = open(c.file, "rb").read().decode("utf-8"); crlf = "\r\n" in raw; t = raw.replace("\r\n", "\n")
        s = t.index("\t%s: {" % card); e = t.index("\n\t}", s)
        blk = t[s:e]
        if nth is None:
            assert blk.count(old) == 1, ("occurrences in block", blk.count(old))
            pos = blk.index(old)
        else:
            # nth counts occurrences in the whole file (mutate.py's convention): locate it, check it is in the block
            pos = -1
            for _ in range(nth):
                pos = t.index(old, pos + 1)
            assert s <= pos < e, "nth occurrence is outside the card's block"
            pos -= s
        blk = blk[:pos] + new + blk[pos + len(old):]
        t = t[:s] + blk + t[e:]
        open(c.file, "wb").write((t.replace("\n", "\r\n") if crlf else t).encode("utf-8"))
        done.append(card)
        return subprocess.CompletedProcess(cmd, 0, "applied to %s\n" % card, "")
    return real_run(cmd, *a, **k)
real_run = subprocess.run
subprocess.run = fake_run
script = sys.argv[1]; sys.argv = sys.argv[1:]
runpy.run_path(script, run_name="__main__")
print("APPLIED", done)
