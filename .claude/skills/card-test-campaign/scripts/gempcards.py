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


def state_dir() -> Path:
    """Where the campaign keeps its bookkeeping (statuses, findings, ledgers): $GEMP_CAMPAIGN_STATE, or
    the folder named in a file .campaign-state at the repository root, or "Claude outputs/card-tests" under the repository, or the cloud workspace's outputs folder.  Not in git."""
    if os.environ.get("GEMP_CAMPAIGN_STATE"):
        return Path(os.environ["GEMP_CAMPAIGN_STATE"])
    # A clone may be pointed at a state folder of its own (a file .campaign-state at its root holding the
    # path), so that two clones working the same cards do not write each other's ledgers.
    marker = REPO / ".campaign-state"
    if marker.is_file():
        return Path(marker.read_text(encoding="utf-8").strip())
    local = REPO / "Claude outputs" / "card-tests"
    cloud = Path("/mnt/user-data/outputs/card-tests")
    if not local.is_dir() and cloud.is_dir():
        return cloud
    return local


class locked:
    """One writer at a time for the campaign's registers and for git: two batches are landed by two agents at once,
    and each numbers findings, writes status.tsv and commits.  `with g.locked():` waits its turn.  A script that
    already holds the lock passes that on to the scripts it runs (GEMP_LOCK_HELD), so they do not wait on it."""

    def __enter__(self):
        self.handle = None
        if os.environ.get("GEMP_LOCK_HELD"):
            return self
        import fcntl
        folder = state_dir()
        folder.mkdir(parents=True, exist_ok=True)
        self.handle = open(folder / ".lock", "w")
        fcntl.flock(self.handle, fcntl.LOCK_EX)
        os.environ["GEMP_LOCK_HELD"] = "1"
        return self

    def __exit__(self, *exc):
        if self.handle is not None:
            import fcntl
            os.environ.pop("GEMP_LOCK_HELD", None)
            fcntl.flock(self.handle, fcntl.LOCK_UN)
            self.handle.close()
        return False


def ledger_path(card_id: str) -> Path:
    """A card's ledger: its text cut into clauses, the checks each calls for, and the tests that claim them.
    Working material for authors and reviewers, kept beside the campaign's other bookkeeping and out of the tests."""
    return state_dir() / "ledgers" / card_id.split("_")[0] / (card_id + ".txt")


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
        """The definition with its `effects` and `target` taken out: what the card says, without how it was
        implemented.  (`target` is how "Bearer must be..." is implemented.)"""
        out, skipping = [], False
        for depth, line in self._top_level():
            if depth == 1:
                if skipping:
                    skipping = False
                    if line.strip().rstrip(",") in ("}", "]"):
                        continue
                held = re.match(r"^(\s*)(effects|target)\s*:", line)
                if held:
                    out.append(held.group(1) + held.group(2) + ": <withheld>")
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
# The generator sheets: what each card is, independently of its definition
# ----------------------------------------------------------------------------------------------------------------

def sheets_dir() -> Path:
    """Where the generator sheets are: gen_en_standard.tsv (Decipher cards), V1.tsv, V2.tsv, V3.tsv and
    master_errata.tsv.  $GEMP_CARD_SHEETS, or generator_sheets/ at the root of the repository.  Not in git."""
    override = os.environ.get("GEMP_CARD_SHEETS")
    return Path(override) if override else REPO / "generator_sheets"


def _read_sheet(path: Path) -> list:
    if not path.is_file():
        return []
    with open(path, encoding="utf-8", newline="") as f:
        reader = csv.reader(f, delimiter="\t", quoting=csv.QUOTE_NONE)
        header = next(reader)
        return [{h: (r[i] if i < len(r) else "") for i, h in enumerate(header)} for r in reader]


def _sheet_set(value: str):
    """A sheet's set_num as Gemp numbers it: 1 is 1, V1 is 101."""
    value = (value or "").strip()
    if re.match(r"^[Vv]\d+$", value):
        return 100 + int(value[1:])
    return int(value) if value.isdigit() else None


_tsv = None
_errata = None


def tsv_rows() -> dict:
    """The sheet rows of cards as first released, by blueprint id: Decipher's cards and the V-sets."""
    global _tsv
    if _tsv is not None:
        return _tsv
    rows = {}
    standard = tsv_path() if tsv_path().is_file() else sheets_dir() / "gen_en_standard.tsv"
    for name, path in [("gen_en_standard", standard)] + [(v, sheets_dir() / (v + ".tsv")) for v in ("V1", "V2", "V3")]:
        for row in _read_sheet(path):
            card_set = _sheet_set(row.get("set_num"))
            if card_set is None or not (row.get("card_num") or "").strip().isdigit():
                continue
            row["_sheet"] = name
            # The standard sheet lists a card's alternate images as extra rows; the first row is the card.
            rows.setdefault("%d_%d" % (card_set, int(row["card_num"])), row)
            # Forth the Three Hunters! was printed three ways under one collector's number, one in each of three
            # cultures.  The sheet has them as three rows of 15_60; Gemp gives each its own id.
            if card_set == 15 and int(row["card_num"]) == 60:
                alias = {"dwarven": "15_204", "elven": "15_205", "gondor": "15_206"}.get((row.get("culture") or "").strip().lower())
                if alias:
                    rows.setdefault(alias, row)
    _tsv = rows
    return rows


def errata_rows() -> dict:
    """The master errata sheet, by (blueprint id of the card as first released, revision number).
    Ketura's caution about this sheet: it was assembled from some twenty sheets, some of them holding errata
    that were proposed and never released or were held back a release, and is about 95% accurate.  Where it and a
    definition disagree, that is something to flag, not necessarily an error on either side."""
    global _errata
    if _errata is not None:
        return _errata
    rows = {}
    for row in _read_sheet(sheets_dir() / "master_errata.tsv"):
        card_set = _sheet_set(row.get("set_num"))
        if card_set is None or not (row.get("card_num") or "").strip().isdigit():
            continue
        try:
            revision = int(row.get("revision") or 0)
        except ValueError:
            continue
        row["_sheet"] = "master_errata"
        rows[("%d_%d" % (card_set, int(row["card_num"])), revision)] = row
    _errata = rows
    return rows


def revision_of(card: CardDef) -> int:
    m = re.search(r"^\s*revision\s*:\s*(\d+)", card.raw, re.M)
    return int(m.group(1)) if m else 0


def base_id(card_id: str) -> str:
    """The id of the card as first released: 51_5 is an erratum of 1_5; a V-set card is revised in place."""
    s, n = split_id(card_id)
    return "%d_%d" % (s - 50, n) if 50 <= s <= 69 else card_id


def _same_text(a, b) -> bool:
    import unicodedata

    def squash(text):
        text = unicodedata.normalize("NFKD", plain_text(text or "")).encode("ascii", "ignore").decode().lower()
        return re.sub(r"[\s.,:;'\"()\-]+", "", text)
    return squash(a) == squash(b)


def revision_read_as(card_id: str):
    """(the revision the definition says it is, the revision whose sheet text it actually has), when those differ:
    a definition whose `revision:` was not moved on when its text was.  None when the marker and the text agree,
    or when no sheet row has the definition's text.  Revision 0 is the card as first released."""
    card = card_index().get(card_id)
    if card is None:
        return None
    base, marked = base_id(card_id), revision_of(card)
    text = card.field("gametext", "")
    rows = {r: row for (b, r), row in errata_rows().items() if b == base}
    if tsv_rows().get(base):
        rows.setdefault(0, tsv_rows()[base])
    def numbers(get):
        out = []
        for field in ("twilight", "strength", "vitality"):
            value = str(get(field) or "").strip()
            out.append(int(value) if re.match(r"^-?\d+$", value) else None)
        return out

    def reads_as(row):
        return _same_text(row.get("game_text"), text) and numbers(row.get) == numbers(lambda f: card.field(f, ""))
    if marked in rows and reads_as(rows[marked]):
        return None
    matches = [r for r, row in rows.items() if r != marked and reads_as(row)]
    return (marked, max(matches)) if matches else None


def sheet_for(card_id: str):
    """The sheet row that describes a definition, and a note when the match is not exact.
    Returns (row or None, note).  The row is matched on the definition's own revision number, unless the
    definition's text is word for word another revision's: then that revision's row, with a note."""
    card = card_index().get(card_id)
    if card is None:
        return None, ""
    other = revision_read_as(card_id)
    if other and other[1] > 0:
        return errata_rows()[(base_id(card_id), other[1])], ""
    if other:
        return tsv_rows()[base_id(card_id)], ""
    base, revision = base_id(card_id), revision_of(card)
    is_erratum = segment_of(card_id) == "errata"
    if revision == 0 and not is_erratum:
        # A card as first released is in its set's sheet, or, for the odd PC card with no set sheet of its own
        # (Rath Dinen, V0), in the errata sheet as revision 0.
        row = tsv_rows().get(card_id) or errata_rows().get((base, 0))
        return row, ("" if row else "no sheet has this card")
    row = errata_rows().get((base, revision))
    if row:
        return row, ""
    have = sorted(r for (b, r) in errata_rows() if b == base)
    if have:
        nearest = max([r for r in have if r <= revision] or have)
        return errata_rows()[(base, nearest)], ("the definition is revision %d; the errata sheet has revision(s) %s for this card, and "
                                                "revision %d is shown" % (revision, ", ".join(str(r) for r in have), nearest))
    original = tsv_rows().get(base)
    if original:
        return original, ("the definition is revision %d and the errata sheet has no row for this card; the card as first "
                          "released is shown" % revision)
    return None, "no sheet has this card"


def sheet_label(row) -> str:
    if row is None:
        return ""
    if row.get("_sheet") == "master_errata":
        bits = ["master errata sheet, revision %s" % row.get("revision", "?")]
        if row.get("errata_date"):
            bits.append("dated " + row["errata_date"])
        if row.get("batch"):
            bits.append("batch \"%s\"" % row["batch"])
        return ", ".join(bits)
    return {"gen_en_standard": "reviewed spreadsheet of Decipher cards"}.get(row.get("_sheet"), "%s sheet" % row.get("_sheet"))


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
