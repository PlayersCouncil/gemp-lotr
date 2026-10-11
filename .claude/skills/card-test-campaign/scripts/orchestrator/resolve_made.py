#!/usr/bin/env python3
"""resolve_made.py <file with conflict markers>

For integrate.sh.  Two files of the skills hold text that a script makes from other files: all of
card-test-campaign/references/rulings-index.md, and the two lists inside gemp-testing/SKILL.md (the pitfall index
and the helper index).  When a batch
landed in the second working tree and something landed on the branch both changed one, the rebase stops on it,
and either side will do, since `lookup.py --index` makes it again afterwards.  This takes the first side of every
conflict in such a file, and refuses (exit 1, the file untouched) if a conflict lies anywhere else: in a part
somebody wrote, both sides' words have to be kept, by hand.
"""
import re
import sys
from pathlib import Path

REGIONS = (("surprises in this campaign were one of these.\n", "\n## Choosing helper cards"),               # lookup.py, MADE_LISTS
           ("the definition of the card it names is read all the same.\n", "\n## Race to Mount Doom"))
BLOCK = re.compile(r"^<<<<<<< [^\n]*\n(.*?)^(?:\|\|\|\|\|\|\| [^\n]*\n.*?)?^=======\n.*?^>>>>>>> [^\n]*\n", re.S | re.M)


def main(argv):
    path = Path(argv[1])
    text = path.read_text(encoding="utf-8")
    blocks = list(BLOCK.finditer(text))
    if not blocks:
        print("%s: no conflict in it" % path.name)
        return 0
    if path.name != "rulings-index.md":
        if not (path.name == "SKILL.md" and path.parent.name == "gemp-testing"):
            print("%s is not a made file: its conflict is yours" % path)
            return 1
        made = [(text.find(before), text.find(after)) for before, after in REGIONS]
        if any(start < 0 or end < 0 for start, end in made) or \
                any(not any(start <= b.start() and b.end() <= end + 1 for start, end in made) for b in blocks):
            print("%s: a conflict lies outside the pitfall index and the helper index: keep both sides' words by hand" % path)
            return 1
    path.write_text(BLOCK.sub(lambda m: m.group(1), text), encoding="utf-8")
    print("%s: %d conflict(s) in made text, the first side taken (lookup.py --index makes it again)" % (path.name, len(blocks)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
