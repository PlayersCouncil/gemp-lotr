#!/usr/bin/env python3
"""Looks things up in the rules without having to hold the documents in mind.

    rules.py toc                    the documents' sections, with line numbers
    rules.py term <name>            a glossary entry, e.g. "prevent", "fierce", "damage +x", "hinder"
    rules.py section <name>         any section by its heading, e.g. "archery fire"
    rules.py card <id or name>      every individual ruling on a card and every mention of it elsewhere
    rules.py terms-for <id>         the glossary entries whose terms appear in a card's game text
    rules.py grep <regex>           matching lines, each with the section it is in
    rules.py interactions           every place outside the individual rulings where a ruling names a card

    rules.py council                the Rules Councils' rulings since 4.2 (LR 1-8, MR 1-2): title and status
    rules.py council <LR 6 | word>  one of them in full

Two documents are searched.  Comprehensive Rules 4.2 is Decipher's last, and holds the individual card rulings.
The Comprehensive Rules 5.0 excerpts are the Players' Council's: they cover what the PC added or changed (hinder,
restore, relentless, items, two- and three-dot uniqueness, when the game ends at site 9), and where the two
disagree, 5.0 is the one in force.  Results from 5.0 are marked and come first.

The Legacy Rules Team's rulings (LR) bind Decipher's cards and the Modern Rules Team's (MR) the Players'
Council's; each is a wiki page with the question, the options and the decision.  Read the status line: some are
decided and enacted, some are still under discussion and decide nothing yet.  `card` and `grep` search them too.
"""
import re
import sys
import unicodedata

import gempcards as g

PC_RULES_FILE = g.SKILLS / "game-mechanics" / "references" / "comprehensive-rules-5.0-excerpts.wiki.txt"

COUNCIL_DIR = g.SKILLS / "game-mechanics" / "references" / "council-rulings"
BOLD = "'" * 3

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


def plain(text: str) -> str:
    """Text with its accents dropped and its apostrophes made straight, capitals kept."""
    t = unicodedata.normalize("NFKD", text)
    return "".join(c for c in t if not unicodedata.combining(c)).replace("\u2019", "'").replace("\u2018", "'")


def squash(text: str) -> str:
    """Letters and digits only, for finding one text inside another whatever the punctuation and markup."""
    return re.sub(r"[^a-z0-9]+", "", plain(text).lower())


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
        self._card_paragraphs = None
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

    def paragraph(self, section, i):
        """The first and last line of the paragraph line i stands in, inside its section."""
        lo = i
        while lo > section[2] + 1 and self.lines[lo - 1].strip():
            lo -= 1
        hi = i
        while hi + 1 < section[3] and self.lines[hi + 1].strip():
            hi += 1
        return lo, hi

    def card_paragraphs(self):
        """Each paragraph outside the individual rulings that names some card with the Card mark, as (section,
        first line, last line, its words with every marked card and culture taken out and accents dropped).  The
        examples of a glossary entry are these, and one may name a second card in plain words."""
        if self._card_paragraphs is None:
            self._card_paragraphs, seen = [], set()
            for _, i in self.mentions():
                sec = self.section_at(i)
                if sec is None:
                    continue
                lo, hi = self.paragraph(sec, i)
                if lo in seen:
                    continue
                seen.add(lo)
                bare = re.sub(r"\{\{(?:Card|CI?)\|[^}]*\}\}", " ", "\n".join(self.lines[lo:hi + 1]))
                self._card_paragraphs.append((sec, lo, hi, plain(bare)))
        return self._card_paragraphs


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


_council = None


def council() -> list:
    """The councils' rulings as (id, title, status, lines)."""
    global _council
    if _council is None:
        _council = []
        for f in sorted(COUNCIL_DIR.glob("*_ruling_*.wiki.txt")) if COUNCIL_DIR.is_dir() else []:
            m = re.match(r"(legacy|modern)_ruling_(\d+)", f.name)
            lines = f.read_text(encoding="utf-8", errors="replace").split("\n")
            title = next((re.sub(r"^=\s*|\s*=$", "", l.strip()) for l in lines if re.match(r"^=[^=].*[^=]=\s*$", l.strip())), f.name)
            status = next((re.sub(r"Status:\s*", "", l.replace(BOLD, "")).strip() for l in lines if "Status:" in l), "status not given")
            _council.append(("%s %d" % ("LR" if m.group(1) == "legacy" else "MR", int(m.group(2))), title, status, lines))
    return _council


def council_text(lines) -> str:
    out = []
    for line in lines:
        line = re.sub(r"\{\{(?:Card|CardSplashImage|C|CI)\|([^}|]*)\}\}", r"\1", line)
        line = re.sub(r"\{\{CardText\|([^}|]*)\}\}", r"[the game text of \1]", line)
        if re.match(r"^\s*(\{\{|\|[a-z]+=|\}\}|__TOC__|----)", line):
            continue
        out.append(line.replace(BOLD, "").rstrip())
    return re.sub(r"\n{3,}", "\n\n", "\n".join(out)).strip()


def council_decision(lines) -> str:
    at = next((i for i, l in enumerate(lines) if re.match(r"^=+.*Decision.*=+\s*$", l.strip())), None)
    return council_text(lines[at:]) if at is not None else "(no decision yet: read the page with rules.py council)"


def council_names(lines) -> set:
    return {norm(n) for l in lines for n in re.findall(r"\{\{(?:Card|CardText|CardSplashImage)\|([^}|]*)\}\}", l)}


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


def council_subjects(lines) -> set:
    """The cards a council page is about: the one at its head, and those whose text it sets out."""
    return {norm(n) for l in lines for n in re.findall(r"\{\{(?:CardText|CardSplashImage)\|([^}|]*)\}\}", l)}


TITLE_ONLY = "  (names the title only: check which version it means)"
NO_MARKS = "  (the name stands in plain words, without the marks: check that the card is meant)"
OTHER_TITLE = ("Each of these names the card's title, and the words it quotes are in the game text of the card(s) it "
               "says and not in this card's. Read it for the rule it illustrates: it says nothing of this card's own "
               "text.")

_QUOTED = re.compile(r"[\"\u201c]([^\"\u201c\u201d]{12,}?)[\"\u201d]")
_title_texts = None


def quotes_another(card_id: str, passage: str) -> list:
    """The other cards of this card's title whose game text a passage quotes, where no words it quotes from a card
    of the title are this card's own.  [] where it quotes this card, quotes no card of the title, or no other card
    has the title.  A quotation cut short with dots is matched piece by piece."""
    global _title_texts
    if _title_texts is None:
        _title_texts = {}
        for cid, c in g.card_index().items():
            text = squash(g.plain_text(c.field("gametext", "") or ""))
            if text:
                _title_texts.setdefault(norm(c.field("title", "") or ""), {})[cid] = text
    me = g.card_index().get(card_id)
    if me is None:
        return []
    same = _title_texts.get(norm(me.field("title", "") or ""), {})
    mine = same.get(card_id, "")
    others = {cid: text for cid, text in same.items() if cid != card_id and text != mine}
    found = []
    for quoted in _QUOTED.findall(passage):
        pieces = [p for p in (squash(x) for x in re.split(r"\.{3}|\u2026", quoted)) if len(p) >= 10]
        if not pieces:
            continue
        if all(p in mine for p in pieces):
            return []
        found += [cid for cid, text in others.items() if all(p in text for p in pieces)]
    return sorted(set(found), key=g.id_sort_key)


def _quoting(ids: list) -> str:
    return ": quotes %s%s, not this card" % (", ".join(ids[:4]), " and %d more" % (len(ids) - 4) if len(ids) > 4 else "")
MENTIONS = ("The rest only mention the name: each is a ruling on another card, or a council page about another card, "
            "with this card's title somewhere in its text (often inside another card's name). Read one for whether "
            "it bears on this card; it is not a ruling on it.")


def card_rulings(arg: str):
    """What the rules say of a card, as (named, mentioned, other_title), each a list of entries ready to print.
    `named` are the rulings on the card and the places that name it in full; `mentioned` are rulings on other
    cards that only have its title in their text ("the Legolas Starter Deck", The Mirror of Galadriel) and council
    pages about another card that name it in passing; `other_title` are the passages that name its title and quote
    the text of another card of that title (the Anduin Banks of another block)."""
    is_id = bool(re.match(r"^\d+_\d+$", arg))
    keys, weak = card_keys(card_id=arg) if is_id else card_keys(name=arg)
    named, mentioned, other_title = [], [], []
    # On a Players' Council erratum the individual rulings are the original's, found by the name the two share.  An
    # "Erratum" among them is Decipher's corrected text of the original and no ruling (notes-errata.txt).
    pc_erratum = is_id and g.segment_of(arg) == "errata"
    for book, names, kind, text, line_no in all_rulings():
        normed = [norm(n) for n in names]
        hit = [n for n in normed if n in keys]
        soft = [n for n in normed if n in weak]
        if hit or soft:
            if pc_erratum and kind.strip().lower() == "erratum":
                kind += " (the original's text)"
            named.append("### Individual ruling, %s line %d: %s%s\nCards: %s\n%s"
                         % (book.name, line_no, kind, "" if hit else TITLE_ONLY, "; ".join(names), text))

    def passage(book, sec, lo, hi, at, how, note):
        text = clean("\n".join(book.lines[lo:hi + 1])).strip()
        others = quotes_another(arg, text) if is_id else []
        if pc_erratum and g.base_id(arg) in others:
            # The words quoted are the original's, and this erratum has rewritten them: still a passage about this
            # card, and whether it reaches the new wording is the question Q-0111 settles case by case.
            note += ("  (quotes the original %s in words this erratum has rewritten: whether it still binds, `lookup.py Q-0111`)"
                     % g.base_id(arg))
            others = []
        entry = "### %s \"%s\", %s line %d%s%s\n%s" % (how, sec[1], book.label(), at + 1, _quoting(others) if others else "", note, text)
        (other_title if others else named).append(entry)

    seen = set()
    for book in books():
        for name, i in book.mentions():
            n = norm(name)
            if n in keys or n in weak:
                sec = book.section_at(i)
                if sec is None or re.search(r"[XR]-List", sec[1]):
                    continue  # the banned and restricted lists name cards without saying anything about them
                lo, hi = book.paragraph(sec, i)
                if (book.name, lo) in seen:
                    continue  # a paragraph that names the card twice is printed once
                seen.add((book.name, lo))
                passage(book, sec, lo, hi, i, "Named in", "" if n in keys else TITLE_ONLY)
    # An example that names two cards sometimes marks only one of them ("You cannot use Strange-Looking Men to
    # transfer «Black Breath»"): the card's whole name in plain words, in a paragraph that marks some card.  The
    # title alone of a card that has a subtitle is not looked for: "Gandalf" and "Sam" are in every other example.
    if is_id:
        me = g.card_index()[arg]
        whole = plain(g.full_name(me))
    else:
        whole = plain(arg)
    if len(whole) >= 4:
        word = re.compile(r"(?<![A-Za-z0-9])" + re.escape(whole[0]) + "(?i:" + re.escape(whole[1:]) + r")(?![A-Za-z0-9])")
        for book in books():
            for sec, lo, hi, bare in book.card_paragraphs():
                if (book.name, lo) in seen or re.search(r"[XR]-List", sec[1]) or not word.search(bare):
                    continue
                seen.add((book.name, lo))
                passage(book, sec, lo, hi, lo, "Named without marks in", NO_MARKS)
    # A ruling on another card that speaks of this one in passing.  Where its text gives the card's full name it
    # names the card; where it only has the title as a word ("the Ring passes to Sam", but also "the Legolas
    # Starter Deck" and every card with Galadriel in its name) it is a mention, to be read as one.
    if is_id:
        title = g.card_index()[arg].field("title", "")
        if title and len(title) > 2:
            word = re.compile(r"(?<![A-Za-z])" + re.escape(title) + r"(?![A-Za-z])")
            for book, names, kind, text, line_no in all_rulings():
                normed = [norm(n) for n in names]
                if any(n in keys or n in weak for n in normed):
                    continue
                if word.search(text):
                    in_full = any(norm(n) in keys for n in re.findall(r"«([^»]+)»", text))
                    entry = "### A ruling on another card %s, %s line %d: %s\nCards: %s\n%s" % (
                        "names this card" if in_full else "has the word \"%s\" in it" % title,
                        book.name, line_no, kind, "; ".join(names), text)
                    (named if in_full else mentioned).append(entry)
    for cid, title, status, lines in council():
        names = council_names(lines)
        strong = bool(names & keys)
        if strong or (names & weak):
            about = bool(council_subjects(lines) & (keys | weak))
            entry = "### Council ruling %s, \"%s\" (%s)%s%s\n%s\n(the whole page: rules.py council %s)" % (
                cid, title, status, "" if strong else TITLE_ONLY,
                "" if about else "  (the page is about another card and names this one in passing)",
                council_decision(lines), cid)
            (named if about else mentioned).append(entry)
    return named, mentioned, other_title


def cmd_card(arg: str, quiet_if_none=False) -> bool:
    named, mentioned, other_title = card_rulings(arg)
    for entry in named:
        print(entry + "\n")
    if other_title:
        print(("" if named else "No ruling names this card.  ") + "Rules passages that quote another card of this title.  " + OTHER_TITLE + "\n")
        for entry in other_title:
            print(entry + "\n")
    if mentioned:
        print(("" if named or other_title else "No ruling names this card.  ") + MENTIONS + "\n")
        for entry in mentioned:
            print(entry + "\n")
    if not named and not mentioned and not other_title and not quiet_if_none:
        print("The rules do not name this card.")
    return bool(named or mentioned or other_title)


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
        # Always case-insensitive; a leading grep-style flag (-i and the like) is taken as meant and dropped.
        words = [a for a in args if not re.match(r"^-[a-zA-Z]+$", a)]
        pat = re.compile(" ".join(words), re.I)
        found = 0
        for book in books():
            for i, line in enumerate(book.lines):
                if pat.search(line):
                    found += 1
                    sec = book.section_at(i)
                    print("%s %5d [%s] %s" % ("5.0" if book.in_force else "4.2", i + 1, sec[1] if sec else "", clean(line).strip()[:300]))
        for cid, title, status, lines in council():
            for i, line in enumerate(lines):
                if pat.search(line):
                    found += 1
                    print("%s %5d [%s; %s] %s" % (cid, i + 1, title, status, council_text([line])[:300]))
        if not found:
            print("No line of either rules document or of the councils' rulings matches /%s/ (case-insensitive)." % " ".join(words))
    elif cmd == "council":
        want = norm(" ".join(args))
        if not want:
            for cid, title, status, lines in council():
                print("%-5s %-45s %s" % (cid, title, status))
            return 0
        hits = [c for c in council() if norm(c[0]).replace(" ", "") == want.replace(" ", "")] \
            or [c for c in council() if want in norm(c[1]) or want in norm(" ".join(c[3]))]
        for cid, title, status, lines in hits:
            print("## %s: %s (%s)" % (cid, title, status))
            print(council_text(lines))
            print()
        if not hits:
            print("No council ruling matches '%s'.  rules.py council lists them." % " ".join(args))
            return 1
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
