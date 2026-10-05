#!/usr/bin/env python3
"""Looks things up in the rules without having to hold the documents in mind.

    rules.py toc                    the documents' sections, with line numbers
    rules.py term <name>            a glossary entry, e.g. "prevent", "fierce", "damage +x", "hinder"
    rules.py section <name>         any section by its heading, e.g. "archery fire"
    rules.py card <id or name>      every individual ruling on a card and every mention of it elsewhere
    rules.py terms-for <id>         the glossary entries whose terms appear in a card's game text
    rules.py grep <regex>           matching lines, each with the section it is in
    rules.py interactions           every place outside the individual rulings where a ruling names a card

Two documents are searched.  Comprehensive Rules 4.2 is Decipher's last, and holds the individual card rulings.
The Comprehensive Rules 5.0 excerpts are the Players' Council's: they cover what the PC added or changed (hinder,
restore, relentless, items, two- and three-dot uniqueness, when the game ends at site 9), and where the two
disagree, 5.0 is the one in force.  Results from 5.0 are marked and come first.
"""
import re
import sys
import unicodedata

import gempcards as g

PC_RULES_FILE = g.SKILLS / "game-mechanics" / "references" / "comprehensive-rules-5.0-excerpts.wiki.txt"

_HEADING = re.compile(r"^(={2,6})\s*(.*?)\s*\1\s*$")

# Glossary terms every card would match, or which only say what a word means.  They are left out of terms-for unless
# asked for with --all, so that the list a card gets is the list worth reading.
BASIC_TERMS = {
    "action", "active", "ally", "artifact", "bearer", "card type", "character", "companion", "condition", "culture",
    "event", "free peoples cards", "game text", "minion", "opponent", "possession", "shadow cards", "site",
    "strength", "vitality", "race", "man", "uruk-hai", "keyword", "token", "lore", "collector's info",
    "block symbol", "twilight pool", "phase action", "fellowship phase", "shadow phase(s)", "maneuver phase",
    "archery phase", "assignment phase", "skirmish phase(s)", "regroup phase", "turn sequence", "draw deck",
    "discard pile", "support area", "adventure deck", "adventure path", "kinds of cards", "building your deck",
    "setting up the game", "starting fellowship", "who goes first?", "format", "x-list", "bid", "helper text",
}

# Other forms a glossary term takes in game text.
STEMS = {
    "healing": ["heals?", "healed"], "killed": ["kills?", "dies", "die", "killing"], "exhausted": ["exhaust"],
    "overwhelmed": ["overwhelm"], "corrupted": ["corrupt"], "mounted": ["mount"], "stack": ["stacked", "stacks"],
    "losing the game": ["loses? the game"], "winning the game": ["wins? the game"], "leaving play": ["leaves play"],
    "moving your fellowship": ["fellowship moves", "moves?"], "playing a card": ["play", "plays", "played"],
    "preventing effects": ["prevent", "prevented"], "replacing sites": ["replace"], "ring-bound": ["ring-bearer"],
    "redrawing your hand": ["redraw"], "twilight cost": ["twilight cost"], "the one ring": ["one ring", "ring-bearer"],
    "hinder": ["hinders", "hindered", "hindering"], "restore": ["restores", "restored", "restoring"],
    "item": ["items"], "reconcile": ["reconciles", "reconciled"], "non-unique": [],
}


def norm(name: str) -> str:
    t = unicodedata.normalize("NFKD", name)
    t = "".join(c for c in t if not unicodedata.combining(c))
    t = t.lower().replace("&amp;", "&")
    t = re.sub(r"[^a-z0-9]+", " ", t)
    return t.strip()


def clean(text: str) -> str:
    """Wiki markup turned into something that reads as plain text."""
    t = text
    t = re.sub(r"<div[^>]*>", "", t).replace("</div>", "")
    t = re.sub(r"<li[^>]*>", "", t).replace("</li>", "")
    t = re.sub(r"\{\{Card\|([^}]+)\}\}", r"«\1»", t)
    t = re.sub(r"\{\{CI?\|([^}]+)\}\}", r"[\1]", t)
    t = re.sub(r"\{\{Twilight\|([^}]+)\}\}", r"(\1)", t)
    t = re.sub(r"\{\{SL\|([^}|]+)(?:\|[^}]*)?\}\}", r"\1", t)
    t = re.sub(r"\{\{Quote\|text=", "Example: ", t)
    t = re.sub(r"\}\}", "", t)
    t = re.sub(r"\[\[#[^|\]]*\|([^\]]+)\]\]", r"\1", t)
    t = re.sub(r"\[\[([^|\]]+)\|([^\]]+)\]\]", r"\2", t)
    t = re.sub(r"\[\[([^\]]+)\]\]", r"\1", t)
    t = re.sub(r"'''(.*?)'''", r"\1", t)
    t = re.sub(r"''(.*?)''", r"\1", t)
    t = re.sub(r"<br\s*/?>", "\n", t)
    t = re.sub(r"</?(nowiki|span|small|u|b|i)[^>]*>", "", t)
    t = re.sub(r"^#\*\s*", "   - ", t, flags=re.M)
    t = re.sub(r"^#\s*", "", t, flags=re.M)
    return t


class Rules:
    """One rules document, cut into its sections."""

    def __init__(self, path, name, in_force=False):
        self.name = name
        self.in_force = in_force
        self.lines = path.read_text(encoding="utf-8").replace("\r", "").split("\n")
        self.sections = []  # (level, title, first line index, last line index exclusive)
        heads = []
        for i, line in enumerate(self.lines):
            m = _HEADING.match(line)
            if m:
                heads.append((len(m.group(1)), clean(m.group(2)).strip(), i))
            elif line.startswith("==") and "<div" in line and not line.rstrip().endswith("="):
                # A heading whose closing marks are on a later line (the section headings are laid out this way).
                level = len(line) - len(line.lstrip("="))
                heads.append((level, clean(line.lstrip("=")).strip(), i))
        for n, (level, title, start) in enumerate(heads):
            end = len(self.lines)
            for level2, _, start2 in heads[n + 1:]:
                if level2 <= level:
                    end = start2
                    break
            self.sections.append((level, title, start, end))

        def find(prefix):
            return next((s for s in self.sections if s[1].lower().startswith(prefix)), None)

        self.sec2, self.sec3 = find("section two"), find("section three")

    def label(self) -> str:
        return self.name + (" (in force where it differs from 4.2)" if self.in_force else "")

    def section_at(self, line_index: int):
        best = None
        for s in self.sections:
            if s[2] <= line_index < s[3] and (best is None or s[0] >= best[0]):
                best = s
        return best

    def glossary(self):
        if self.sec2 is None:
            return [s for s in self.sections if s[0] == 3]
        return [s for s in self.sections if s[0] == 3 and self.sec2[2] < s[2] < self.sec2[3]]

    def text(self, section) -> str:
        return clean("\n".join(self.lines[section[2] + 1:section[3]])).strip().rstrip("-").strip()

    def rulings(self):
        """The individual card rulings as (card names, ruling type, text, line number)."""
        if self.sec3 is None:
            return []
        out, row = [], []
        row_line = self.sec3[2]
        for i in range(self.sec3[2], self.sec3[3]):
            line = self.lines[i]
            if line.startswith("|-") or line.startswith("|}"):
                if row:
                    out.append(self._row(row, row_line))
                row, row_line = [], i + 1
            elif row or line.startswith("|"):
                row.append(line)
        return [r for r in out if r]

    @staticmethod
    def _row(lines, line_no):
        cells, current = [], None
        for line in lines:
            if line.startswith("|"):
                if current is not None:
                    cells.append(current)
                current = line[1:].strip()
            elif current is not None:
                current += "\n" + line
        if current is not None:
            cells.append(current)
        if len(cells) < 3:
            return None
        names = re.findall(r"\{\{Card\|([^}]+)\}\}", cells[0])
        if not names:
            return None
        kind = re.sub(r"[\[\]']", "", cells[1]).strip()
        return names, kind, clean("\n".join(cells[2:])).strip(), line_no + 1

    def mentions(self):
        """Every card named outside the individual rulings, as (card name, line index)."""
        out = []
        for i, line in enumerate(self.lines):
            if self.sec3 is not None and self.sec3[2] <= i < self.sec3[3]:
                continue
            for name in re.findall(r"\{\{Card\|([^}]+)\}\}", line):
                out.append((name, i))
        return out


_books = None


def books() -> list:
    """The documents, the one in force first."""
    global _books
    if _books is None:
        _books = []
        if PC_RULES_FILE.is_file():
            _books.append(Rules(PC_RULES_FILE, "Comprehensive Rules 5.0 excerpts", in_force=True))
        _books.append(Rules(g.RULES_FILE, "Comprehensive Rules 4.2"))
    return _books


def card_keys(card_id=None, name=None):
    """The ways the rules might refer to a card: full name, collector's info, and (weaker) title alone."""
    keys, weak = set(), set()
    if card_id is not None:
        card = g.card_index().get(card_id)
        if card is None:
            raise SystemExit("No card definition for " + card_id)
        keys.add(norm(g.full_name(card)))
        m = re.search(r"collInfo\s*:\s*(\S+)", card.raw)
        if m:
            keys.add(norm(m.group(1)))
        if card.field("subtitle"):
            weak.add(norm(card.field("title")))
        else:
            keys.add(norm(card.field("title")))
    else:
        keys.add(norm(name))
    return keys, weak


def all_rulings():
    return [(book,) + r for book in books() for r in book.rulings()]


def cmd_card(arg: str, quiet_if_none=False) -> bool:
    is_id = bool(re.match(r"^\d+_\d+$", arg))
    keys, weak = card_keys(card_id=arg) if is_id else card_keys(name=arg)
    found = False
    for book, names, kind, text, line_no in all_rulings():
        normed = [norm(n) for n in names]
        hit = [n for n in normed if n in keys]
        soft = [n for n in normed if n in weak]
        if hit or soft:
            found = True
            tag = "" if hit else "  (names the title only: check which version it means)"
            print("### Individual ruling, %s line %d: %s%s" % (book.name, line_no, kind, tag))
            print("Cards: " + "; ".join(names))
            print(text)
            print()
    seen = set()
    for book in books():
        for name, i in book.mentions():
            n = norm(name)
            if n in keys or n in weak:
                sec = book.section_at(i)
                key = (book.name, i)
                if key in seen or sec is None or re.search(r"[XR]-List", sec[1]):
                    continue  # the banned and restricted lists name cards without saying anything about them
                seen.add(key)
                found = True
                lo = i
                while lo > sec[2] + 1 and book.lines[lo - 1].strip():
                    lo -= 1
                hi = i
                while hi + 1 < sec[3] and book.lines[hi + 1].strip():
                    hi += 1
                tag = "" if n in keys else "  (names the title only: check which version it means)"
                print("### Named in \"%s\", %s line %d%s" % (sec[1], book.label(), i + 1, tag))
                print(clean("\n".join(book.lines[lo:hi + 1])).strip())
                print()
    # A ruling on another card that speaks of this one by its title in passing ("the Ring passes to Sam").
    if is_id:
        title = g.card_index()[arg].field("title", "")
        if title and len(title) > 2:
            word = re.compile(r"(?<![A-Za-z])" + re.escape(title) + r"(?![A-Za-z])")
            for book, names, kind, text, line_no in all_rulings():
                normed = [norm(n) for n in names]
                if any(n in keys or n in weak for n in normed):
                    continue
                if word.search(text):
                    found = True
                    print("### A ruling on another card mentions \"%s\", %s line %d: %s" % (title, book.name, line_no, kind))
                    print("Cards: " + "; ".join(names))
                    print(text)
                    print()
    if not found and not quiet_if_none:
        print("The rules do not name this card.")
    return found


def terms_for(card_id: str, include_basic=False):
    """The glossary entries a card's text touches, as (book, section)."""
    card = g.card_index()[card_id]
    text = g.plain_text(card.field("gametext", "")).lower()
    keywords = (card.field("keywords", "") or "").lower()
    # The card's own name counts: the glossary has entries for Frodo, Sam and Gollum/Smeagol as characters.
    title = norm(card.field("title", ""))
    hay = text + " " + keywords + " " + title
    out = []
    for book in books():
        for sec in book.glossary():
            term = sec[1].lower()
            if not include_basic and term in BASIC_TERMS:
                continue
            pat = re.escape(term)
            pat = pat.replace(r"\+x", r"\+\s*\d+").replace(r"\ x", r"\s*\(?\d+\)?")
            pat = re.sub(r"\\\(s\\\)", "s?", pat)
            variants = [pat] + [re.escape(part.strip()) for part in term.split("/") if "/" in term]
            variants += STEMS.get(term, [])
            if not (term.endswith("ing") or term.endswith("ed") or term.endswith("s")):
                variants.append(pat + r"(?:s|ed|ing|es)")
            if re.search(r"(?<![a-z])(?:" + "|".join(variants) + r")(?![a-z])", hay):
                out.append((book, sec))
    return out


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    cmd, args = argv[1], argv[2:]
    if cmd == "toc":
        for book in books():
            print("== " + book.label())
            for level, title, start, end in book.sections:
                if level <= 3 or "--all" in args:
                    print("%5d  %s%s" % (start + 1, "  " * (level - 2), title))
    elif cmd in ("term", "section"):
        name = " ".join(args)
        want = norm(name)
        any_hit = False
        for book in books():
            pool = book.glossary() if cmd == "term" else book.sections
            hits = [s for s in pool if norm(s[1]) == want] or [s for s in pool if want in norm(s[1])]
            for s in hits:
                any_hit = True
                print("## %s  (%s, lines %d-%d)" % (s[1], book.label(), s[2] + 1, s[3]))
                print(book.text(s))
                print()
        if not any_hit:
            print("Nothing headed '%s'.  Try: rules.py grep '%s'" % (name, name))
            return 1
    elif cmd == "card":
        cmd_card(" ".join(args))
    elif cmd == "terms-for":
        full = "--full" in args
        for book, s in terms_for(args[0], include_basic="--all" in args):
            if full:
                print("## %s  (%s, lines %d-%d)" % (s[1], book.label(), s[2] + 1, s[3]))
                print(book.text(s))
                print()
            else:
                print("%-28s %s, lines %d-%d" % (s[1], book.name, s[2] + 1, s[3]))
    elif cmd == "grep":
        pat = re.compile(" ".join(args), re.I)
        for book in books():
            for i, line in enumerate(book.lines):
                if pat.search(line):
                    sec = book.section_at(i)
                    print("%s %5d [%s] %s" % ("5.0" if book.in_force else "4.2", i + 1, sec[1] if sec else "", clean(line).strip()[:300]))
    elif cmd == "interactions":
        for book in books():
            by_para = {}
            for name, i in book.mentions():
                sec = book.section_at(i)
                by_para.setdefault((sec[1] if sec else "", i), []).append(name)
            for (title, i), names in sorted(by_para.items(), key=lambda kv: kv[0][1]):
                print("%s %5d [%s] %s" % ("5.0" if book.in_force else "4.2", i + 1, title, "; ".join(names)))
    else:
        print(__doc__)
        return 1
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
