"""Shared helpers for the card test campaign scripts.

Everything here is read-only with respect to the repository.  The scripts find the repository from their own
location, so they can be run from any working directory.
"""
import csv
import os
import re
import sys
from pathlib import Path


def repo_root() -> Path:
    here = Path(__file__).resolve()
    for parent in here.parents:
        if (parent / "gemp-lotr" / "pom.xml").is_file():
            return parent
    raise SystemExit("Could not find the repository root (a folder containing gemp-lotr/pom.xml).")


REPO = repo_root()
MODULES = REPO / "gemp-lotr"
CARDS_DIR = MODULES / "gemp-lotr-cards" / "src" / "main" / "resources" / "cards"
RESOURCES_DIR = MODULES / "gemp-lotr-cards" / "src" / "main" / "resources"
SERVER = MODULES / "gemp-lotr-server"
TEST_ROOT = SERVER / "src" / "test" / "java" / "com" / "gempukku" / "lotro"
CARD_TESTS = TEST_ROOT / "cards"
VT_DIR = SERVER / "src" / "main" / "java" / "com" / "gempukku" / "lotro" / "virtualtable"
FRAMEWORK_DIR = TEST_ROOT / "framework"
SKILLS = REPO / ".claude" / "skills"
RULES_FILE = SKILLS / "game-mechanics" / "references" / "comprehensive-rules-4.2.wiki.txt"


def tsv_path() -> Path:
    override = os.environ.get("GEMP_CARD_TSV")
    return Path(override) if override else REPO / "gen_en_standard.tsv"


# ----------------------------------------------------------------------------------------------------------------
# Card ids
# ----------------------------------------------------------------------------------------------------------------

def split_id(card_id: str):
    s, n = card_id.split("_")
    return int(s), int(n)


def id_sort_key(card_id: str):
    return split_id(card_id)


def segment_of(card_id: str) -> str:
    """Which part of the campaign a card belongs to."""
    s, _ = split_id(card_id)
    if s <= 19:
        return "decipher"
    if 30 <= s <= 33:
        return "hobbit"
    if 50 <= s <= 69:
        return "errata"
    if 90 <= s <= 94:
        return "rtmd"
    if 100 <= s <= 103:
        return "vset"
    return "other"


def set_label(card_id: str) -> str:
    s, _ = split_id(card_id)
    seg = segment_of(card_id)
    if seg == "errata":
        return "Errata of set %d" % (s - 50)
    if seg == "vset":
        return "V%d" % (s - 100)
    if seg == "rtmd":
        return "RTMD %d" % s
    return "Set %d" % s


# ----------------------------------------------------------------------------------------------------------------
# HJSON card definitions
# ----------------------------------------------------------------------------------------------------------------

_KEY = re.compile(r"^\s*\"?(\d+_\d+)\"?\s*:\s*\{\s*$")


def _depth_change(line: str) -> int:
    """How a line changes the nesting depth.  Only braces that open at the end of a line or close on a line of
    their own are counted, which is how every card file is laid out, and which keeps braces inside text out of it."""
    t = line.strip()
    if t.startswith("#") or t.startswith("//"):
        return 0
    if t.rstrip(",") in ("}", "]"):
        return -1
    if t.endswith("{") or t.endswith("["):
        return 1
    return 0


class CardDef:
    __slots__ = ("id", "file", "line", "raw", "_parsed")

    def __init__(self, card_id, file, line, raw):
        self.id = card_id
        self.file = file
        self.line = line
        self.raw = raw
        self._parsed = None

    @property
    def rel_file(self) -> str:
        return str(self.file.relative_to(REPO)).replace(os.sep, "/")

    @property
    def culture_file(self) -> str:
        return self.file.stem

    def parsed(self) -> dict:
        """The definition as a dictionary.  Needs the `hjson` package (pip install hjson)."""
        if self._parsed is None:
            try:
                import hjson
            except ImportError:
                raise SystemExit("This needs the hjson package: pip install hjson --break-system-packages")
            self._parsed = hjson.loads("{\n" + self.raw + "\n}")[self.id]
        return self._parsed

    def _top_level(self):
        """Each line of the definition with the nesting depth it sits at (the card's own fields are at depth 1)."""
        depth = 0
        for line in self.raw.split("\n"):
            change = _depth_change(line)
            if change < 0:
                depth += change
            yield depth, line
            if change > 0:
                depth += change

    def field(self, name, default=None):
        """A top-level field read straight from the text, without needing the hjson package."""
        pattern = re.compile(r"^\s*" + re.escape(name) + r"\s*:\s*(.*?)\s*$")
        for depth, line in self._top_level():
            if depth == 1:
                m = pattern.match(line)
                if m:
                    return m.group(1)
        return default

    def without_effects(self) -> str:
        """The definition with its `effects` taken out: what the card says, without how it was implemented."""
        out, skipping = [], False
        for depth, line in self._top_level():
            if depth == 1:
                if skipping:
                    skipping = False
                    if line.strip().rstrip(",") in ("}", "]"):
                        continue
                if re.match(r"^\s*effects\s*:", line):
                    out.append(re.match(r"^\s*", line).group(0) + "effects: <withheld>")
                    skipping = line.rstrip().endswith(("{", "["))
                    continue
            if skipping:
                continue
            out.append(line)
        return "\n".join(out)


_card_index = None


def card_index() -> dict:
    """Every card definition in the repository that is not commented out, by blueprint id."""
    global _card_index
    if _card_index is not None:
        return _card_index
    index = {}
    for path in sorted(CARDS_DIR.rglob("*.hjson")):
        lines = path.read_text(encoding="utf-8", errors="replace").split("\n")
        current, start, buf, depth = None, 0, [], 0
        for i, line in enumerate(lines):
            line = line.rstrip("\r")
            if current is None:
                m = _KEY.match(line)
                if m:
                    current, start, buf, depth = m.group(1), i + 1, [line], 1
            else:
                buf.append(line)
                depth += _depth_change(line)
                if depth <= 0:
                    index[current] = CardDef(current, path, start, "\n".join(buf))
                    current = None
    _card_index = index
    return index


def full_name(card: CardDef) -> str:
    title = card.field("title", "?")
    subtitle = card.field("subtitle")
    return title + (", " + subtitle if subtitle else "")


# ----------------------------------------------------------------------------------------------------------------
# The reviewed spreadsheet (Decipher cards only)
# ----------------------------------------------------------------------------------------------------------------

_tsv = None


def tsv_rows() -> dict:
    """The reviewed spreadsheet's rows by blueprint id, or an empty dictionary if the file is not there."""
    global _tsv
    if _tsv is not None:
        return _tsv
    path = tsv_path()
    rows = {}
    if path.is_file():
        with open(path, encoding="utf-8", newline="") as f:
            reader = csv.reader(f, delimiter="\t", quoting=csv.QUOTE_NONE)
            header = next(reader)
            for r in reader:
                row = {h: (r[i] if i < len(r) else "") for i, h in enumerate(header)}
                if not row.get("set_num") or not row.get("card_num"):
                    continue
                card_id = "%d_%d" % (int(row["set_num"]), int(row["card_num"]))
                # The sheet lists a card's alternate images as extra rows; the first row is the card.
                rows.setdefault(card_id, row)
    _tsv = rows
    return rows


def plain_text(text: str) -> str:
    """Game text with its markup taken out and its punctuation made uniform, for comparing two sources."""
    if text is None:
        return ""
    t = text
    t = re.sub(r"<helper>.*?</helper>", "", t, flags=re.S | re.I)
    t = re.sub(r"<br\s*/?>", " ", t, flags=re.I)
    t = re.sub(r"\\n", " ", t)
    # A bare backslash is how some definitions write a line break; before a bracket it only stops the client
    # from drawing a twilight symbol.
    t = re.sub(r"\\(?=[(\[])", "", t)
    t = t.replace("\\", " ")
    t = re.sub(r"</?[a-zA-Z][^>]*>", "", t)
    t = t.replace("&nbsp;", " ")
    t = t.replace("’", "'").replace("‘", "'").replace("“", '"').replace("”", '"')
    t = t.replace("–", "-").replace("—", "-").replace(" ", " ")
    t = re.sub(r"\s+", " ", t).strip()
    if len(t) >= 2 and t[0] == t[-1] and t[0] in "\"'":
        t = t[1:-1].strip()
    return t


# ----------------------------------------------------------------------------------------------------------------
# Test files
# ----------------------------------------------------------------------------------------------------------------

_TEST_FILE = re.compile(r"^Card_(V?)(\d+)_(\d+)_(Errata)?Tests\.java$")

_test_index = None


def test_index() -> dict:
    """The live test file for each card, by blueprint id.  Dormant copies (.old and the like) are left out."""
    global _test_index
    if _test_index is not None:
        return _test_index
    index = {}
    for path in sorted(CARD_TESTS.rglob("Card_*Tests.java")):
        m = _TEST_FILE.match(path.name)
        if not m:
            continue
        v, s, n, errata = m.group(1), int(m.group(2)), int(m.group(3)), m.group(4)
        if v:
            card_set = 100 + s
        elif errata:
            card_set = 50 + s
        else:
            card_set = s
        index["%d_%d" % (card_set, n)] = path
    _test_index = index
    return index


def dormant_tests() -> list:
    """Test files kept for a card's earlier iterations: not compiled, but kept in step with the live ones."""
    return sorted(p for p in CARD_TESTS.rglob("Card_*") if not p.name.endswith(".java"))


def rel(path: Path) -> str:
    return str(path.relative_to(REPO)).replace(os.sep, "/")


def test_class_name(path: Path) -> str:
    """The fully qualified class name of a test file."""
    relp = path.relative_to(SERVER / "src" / "test" / "java")
    return ".".join(relp.with_suffix("").parts)


def resolve_ids(args) -> list:
    """Turns what was typed on the command line (ids, or `set:1`, `file:set01-Elven`, `seg:vset`) into ids."""
    cards = card_index()
    out = []
    for a in args:
        if re.match(r"^\d+_\d+$", a):
            if a not in cards:
                print("warning: no card definition for " + a, file=sys.stderr)
            out.append(a)
        elif a.startswith("set:"):
            s = int(a[4:])
            out += [c for c in cards if split_id(c)[0] == s]
        elif a.startswith("file:"):
            stem = a[5:].lower()
            out += [c for c, d in cards.items() if d.file.stem.lower() == stem]
        elif a.startswith("seg:"):
            out += [c for c in cards if segment_of(c) == a[4:]]
        else:
            raise SystemExit("Don't know what to do with '%s' (expected an id like 1_40, set:1, file:set01-Elven or seg:vset)." % a)
    return sorted(set(out), key=id_sort_key)
