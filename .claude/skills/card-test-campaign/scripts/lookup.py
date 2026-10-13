#!/usr/bin/env python3
"""Find whole entries in the campaign's reference files, and rebuild the index of rulings and settled shapes.

    lookup.py Q-0098                      every entry that names Q-0098
    lookup.py "support area" hindered     entries containing every term given (case does not matter)
    lookup.py prevent --in pitfalls       one file only: rulings, shapes, pitfalls, helpers, standard, rig, hjson, all
    lookup.py valiant --in helpers        the helper cards found for a need: a word of it, a card's name or an id
    lookup.py Q-0027 --skip 1_338,1_342   stage 1 of a batch: what says how one of these cards is BUILT stays shut
    lookup.py --index                     rebuild card-test-campaign/references/rulings-index.md, and the
                                          pitfall index and the helper index in the gemp-testing skill

An entry is a paragraph (or a list that stands as one block) under a heading, and is printed only when it
contains EVERY term given: look up separate subjects with separate commands. By default the rulings log, the
settled shapes, the rig's pitfalls and the helper cards are searched. A term that looks like a finding, question or rig-request
number (F-0123, Q-0045, G-0012) matches that number only; any other term is a regular expression. Where no entry
matches that way, the terms are tried again as plain words, whatever the punctuation and markup on either side,
so that an entry's first words can be given as rulings-index.md prints them: lookup.py "The Hobbit sets (30 to 33)".

A number looked up by itself (no other term, no --in) is also read in the campaign's registers, after the entries:
a finding's row of findings.tsv (its status, what was found and its outcome, which may leave something open that
no ruling says), and a question's entry in questions.md: whole while it is open, and Ketura's answer once it has one.

--skip takes the ids of your batch (separated by spaces or commas).  An entry of the shapes, the pitfalls or any
other file but the rulings log that names one of them, or another version of one (its original, its errata), is
named by its heading and not printed, and so is such a finding's row of findings.tsv: they say what a definition
holds and how it was put right, which stage 1 does not read.  The rulings log and a question's entry are printed
whatever they name: what Ketura ruled a card does is what stage 1 is written from.  Look again in stage 2, without
--skip.

The exit status is 0 whether or not anything matched: "no entry matches" is an answer, and a chain of lookups in
one step is not cut short by it.
"""
import re
import sys
from pathlib import Path

SKILLS = Path(__file__).resolve().parents[2]
REFS = SKILLS / "card-test-campaign" / "references"
FILES = {
    "rulings": REFS / "rulings-log.md",
    "shapes": REFS / "shapes.md",
    "pitfalls": SKILLS / "gemp-testing" / "references" / "pitfalls.md",
    "helpers": SKILLS / "gemp-testing" / "references" / "helpers.md",
    "standard": SKILLS / "test-generation" / "SKILL.md",
    "rig": SKILLS / "gemp-testing" / "SKILL.md",
    "hjson": SKILLS / "hjson-reference" / "SKILL.md",
}
DEFAULT = ["rulings", "shapes", "pitfalls", "helpers"]
INDEX = REFS / "rulings-index.md"


def entries(path):
    """(heading, text) for each blank-line-separated block that is not a heading or front matter."""
    text = path.read_text(encoding="utf-8")
    if text.startswith("---"):
        end = text.find("\n---", 3)
        text = text[end + 4:] if end > 0 else text
    heading, block, inside_code = "", [], False
    out = []

    def flush():
        if block and any(x.strip() for x in block):
            out.append((heading, "\n".join(block).strip("\n")))
        block.clear()

    for line in text.split("\n"):
        if line.startswith("```"):
            inside_code = not inside_code
        if not inside_code and re.match(r"#{1,4} ", line):
            flush()
            heading = line.lstrip("#").strip()
            continue
        if not inside_code and not line.strip():
            flush()
            continue
        block.append(line)
    flush()
    return out


def words(text):
    """Letters and digits only, in lower case, one space between: what is left of an entry, or of a term, when its
    punctuation and markup are gone."""
    return " ".join(re.findall(r"[a-z0-9]+", text.lower()))


class matcher:
    """Whether an entry contains a term: as the regular expression it is, or, when asked, as the plain words it is
    made of.  The index prints an entry's first words without their markup, cut short with dots, and with the
    numbers the entry settles after them in square brackets; a term copied from there is matched by its words,
    without those two additions."""

    def __init__(self, term):
        self.number = bool(re.fullmatch(r"[QFG]-\d{4}", term))
        if self.number:
            self.pattern = re.compile(r"\b" + re.escape(term) + r"\b")
        else:
            try:
                self.pattern = re.compile(term, re.IGNORECASE)
            except re.error:  # card text is full of brackets: a term that is not a valid pattern is taken literally
                self.pattern = re.compile(re.escape(term), re.IGNORECASE)
        copied = re.sub(r"\s*\[(?:[QFG]-\d{4}(?:,\s*)?)+\]\s*$", "", term)
        copied = re.sub(r"\s*\.\.\.\s*$", "", copied)
        self.words = "" if self.number else words(copied)

    def search(self, text, as_words=None):
        """as_words: the entry's plain words, where the term may match by its words as well."""
        if self.pattern.search(text):
            return True
        return bool(as_words is not None and self.words and (" " + self.words + " ") in (" " + as_words + " "))


def lead(text, width=150):
    """The entry's own first words: its bold lead, or its opening when the lead is too short to say anything."""
    m = re.match(r"\*\*(.+?)\*\*", text)
    s = m.group(1).strip().rstrip(".:,") if m else ""
    if len(s) < 30:
        s = re.sub(r"\*\*|\*|`", "", re.sub(r"\s+", " ", text))
        s = s if len(s) <= width else s[:width].rsplit(" ", 1)[0] + " ..."
    refs = [r for r in dict.fromkeys(re.findall(r"\b[QFG]-\d{4}\b", text)) if r not in s]
    return s + (" [" + ", ".join(refs) + "]" if refs else "")


def build_index():
    out = ["# Index of rulings and settled shapes", "",
           "Generated by `scripts/lookup.py --index` from `rulings-log.md` (what Ketura has ruled, in her words) and",
           "`shapes.md` (what those rulings and the engine's fixes mean for a definition and a test). Every entry is",
           "listed by its first words, with the numbers it settles that its first words do not name. This is for",
           "finding an entry, not for applying one: read the entry itself before an expectation rests on it",
           "(`lookup.py Q-0098`, `lookup.py \"support area\"`). Do not raise as a question anything listed here.", ""]
    for key, title in (("rulings", "Rulings (rulings-log.md)"), ("shapes", "Settled shapes (shapes.md)")):
        out += ["# " + title, ""]
        first = FILES[key].read_text(encoding="utf-8").split("\n", 1)[0].lstrip("# ").strip()
        last = None
        for heading, text in entries(FILES[key]):
            if heading == first:  # the file's own introduction
                continue
            if heading != last:
                out += ["", "## " + heading, ""]
                last = heading
            out.append("- " + lead(text))
        out.append("")
    INDEX.write_text(re.sub(r"\n{3,}", "\n\n", "\n".join(out)).rstrip("\n") + "\n", encoding="utf-8")
    print(f"wrote {INDEX} ({INDEX.stat().st_size} bytes)")


RIG_SKILL = SKILLS / "gemp-testing" / "SKILL.md"


def shut(skipped) -> set:
    """The ids given to --skip with every other version of each (packet.py's rule), or the ids alone where there
    are no definitions to read the versions from."""
    try:
        import packet
        return packet.shut(skipped)
    except BaseException:  # run from a copy of the skill with no repository round it
        return set(skipped)


def names_one_of(text: str, ids) -> str:
    """The first of the ids that the text names, or ""."""
    for card_id in sorted(ids):
        if re.search(r"(?<![\w])%s(?![\d_])" % re.escape(card_id), text):
            return card_id
    return ""


WITHHELD = "withheld in stage 1 (it names %s, an id after --skip or another version of one): read it in stage 2, without --skip"


def in_registers(number: str, skipped=()) -> str:
    """What the campaign's registers say under a finding's or a question's number, or "" where they say nothing
    (or there is no state folder to read).  A finding's row that names a card of `skipped` is named and not shown."""
    try:
        import gempcards
        state = gempcards.state_dir()
    except BaseException:  # run from a copy of the skill with no repository round it
        return ""
    if number.startswith("F-"):
        path = state / "findings.tsv"
        rows = path.read_text(encoding="utf-8").split("\n") if path.is_file() else []
        head = rows[0].split("\t") if rows else []
        for row in rows[1:]:
            cells = row.split("\t")
            if cells[0] == number and len(cells) == len(head):
                f = dict(zip(head, cells))
                named = names_one_of(row, skipped)
                if named:
                    return "== findings.tsv > %s: %s\n" % (number, WITHHELD % named)
                out = ["== findings.tsv > %s: %s %s, %s, %s (%s)" % (number, f.get("card", ""), f.get("name", ""), f.get("kind", ""),
                                                                   f.get("status", ""), f.get("date", "")), f.get("summary", "")]
                out += ["%s: %s" % (label, f[key]) for label, key in (("Found", "found"), ("Outcome", "outcome")) if f.get(key)]
                return "\n".join(out) + "\n"
        return ""
    if number.startswith("Q-"):
        path = state / "questions.md"
        text = path.read_text(encoding="utf-8") if path.is_file() else ""
        m = re.search(r"^### %s\b[^\n]*\n(.*?)(?=^#{2,3} |\Z)" % re.escape(number), text, re.S | re.M)
        if not m:
            return ""
        title = m.group(0).split("\n", 1)[0].lstrip("# ").strip()
        section = (re.findall(r"^## (.+)$", text[:m.start()], re.M) or ["?"])[-1].strip()
        body = m.group(1).strip("\n")
        if section.lower().startswith("open"):
            return "== questions.md > Open (not yet answered: nothing rests on either reading)\n%s\n\n%s\n" % (title, body)
        said = [para for para in re.split(r"\n\s*\n", body) if re.match(r"\*\*(?:Ketura|Answer|Ruled)", para.strip())]
        return "== questions.md > %s\n%s\n\n%s\n" % (section, title, "\n\n".join(said) if said else
                                                     "(the whole question and her answer are in questions.md)")
    return ""


# The two lists the rig skill keeps of its reference files, each between a sentence that stands before it and a
# heading that stands after it: (the file, the words before, the words after).  orchestrator/resolve_made.py knows
# the same two places (its REGIONS): change them together.
MADE_LISTS = (
    ("pitfalls", "surprises in this campaign were one of these.\n\n", "\n## Choosing helper cards"),
    ("helpers", "the definition of the card it names is read all the same.\n\n", "\n## Race to Mount Doom"),
)


def build_skill_index(key, before, after):
    """The rig skill lists every pitfall, and every helper card's entry, by its first words, group by group:
    rebuild one of those lists."""
    groups, order = {}, []
    first = FILES[key].read_text(encoding="utf-8").split("\n", 1)[0].lstrip("# ").strip()
    for heading, text in entries(FILES[key]):
        if heading == first:
            continue
        for item in (text.split("\n- ") if text.startswith("- ") else [text]):
            item = item.lstrip("- ")
            m = re.match(r"\*\*(.+?)\*\*", item, re.S)  # a helper's need may run over the end of a line
            if m:
                name = re.sub(r"\s+", " ", m.group(1)).strip().rstrip(".,")
            else:
                m = re.match(r"`(0_\d+)` (Test [A-Z][\w-]+)", item)
                if not m:
                    continue
                name = m.group(2) + " " + m.group(1)
            if heading not in groups:
                groups[heading] = []
                order.append(heading)
            groups[heading].append(name)
    lines = ["- **%s:** %s." % (h, " · ".join(groups[h])) for h in order]
    skill = RIG_SKILL.read_text(encoding="utf-8")
    start = skill.index(before) + len(before)
    end = skill.index(after, start)
    RIG_SKILL.write_text(skill[:start] + "\n".join(lines) + "\n" + skill[end:], encoding="utf-8")
    print(f"rebuilt the {key} index in {RIG_SKILL} ({sum(len(v) for v in groups.values())} entries)")


def main(argv):
    if not argv or argv[0] in ("-h", "--help"):
        print(__doc__)
        return 0
    if argv[0] == "--index":
        build_index()
        for made in MADE_LISTS:
            build_skill_index(*made)
        return 0
    where, terms, limit, narrowed, skipped = DEFAULT, [], 8, False, []
    i = 0
    while i < len(argv):
        if argv[i] == "--skip":
            i += 1
            while i < len(argv) and re.fullmatch(r"\d+_\d+(?:,\d+_\d+)*,?", argv[i]):
                skipped += [x for x in argv[i].split(",") if x]
                i += 1
            if not skipped:
                print("--skip: give the ids of your batch after it (1_338,1_342)")
                return 1
        elif argv[i] == "--in":
            where = list(FILES) if argv[i + 1] == "all" else argv[i + 1].split(",")
            narrowed = True
            i += 2
        elif argv[i] == "--max":
            limit = int(argv[i + 1])
            i += 2
        else:
            terms.append(argv[i])
            i += 1
    pats = [matcher(t) for t in terms]
    hits = []
    found = [(key, heading, text) for key in where for heading, text in entries(FILES[key])]
    hits = [e for e in found if all(p.search(e[2]) for p in pats)]
    if not hits:
        # Nothing as regular expressions: the terms as plain words (a line copied from the index, brackets and all).
        hits = [e for e in found if all(p.search(e[2], words(e[2])) for p in pats)]
        if hits:
            print("(no entry matches as a regular expression; matched as plain words)\n")
    # Stage 1 of a batch: what says how one of its cards is built is named and not shown.
    skipped = shut(skipped) if skipped else set()
    withheld = [(e, names_one_of(e[2], skipped)) for e in hits if e[0] != "rulings" and names_one_of(e[2], skipped)] if skipped else []
    hits = [e for e in hits if e not in [w[0] for w in withheld]]
    # A number asked for by itself is read in the registers too.
    registers = [r for r in (in_registers(t, skipped) for t in terms if narrowed is False and len(terms) == 1 and pats[0].number) if r]
    if not hits and not withheld and registers:
        # The number is the registers' alone: that is an answer, and the line above it does not read as a miss.
        print("no entry of %s names %s; the campaign's register has it:" % (", ".join(where), terms[0]))
    elif not hits and not withheld:
        print("no entry matches", " and ".join(repr(t) for t in terms), "in", ", ".join(where))
    for key, heading, text in hits[:limit]:
        print(f"== {key} > {heading}\n{text}\n")
    if len(hits) > limit:
        print(f"... and {len(hits) - limit} more (narrow the search, or --max N):")
        for key, heading, text in hits[limit:]:
            print(f"   {key} > {heading}: {lead(text, 100)}")
    for (key, heading, text), named in withheld:
        print(f"== {key} > {heading}: {WITHHELD % named}\n")
    for register in registers:
        print(("\n" if not hits and not withheld else "") + register)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except BrokenPipeError:  # output piped to head
        sys.exit(0)
