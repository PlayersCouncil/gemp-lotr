#!/usr/bin/env python3
"""Looks things up in the Comprehensive Rules 4.2 without having to hold the whole document in mind.

    rules.py toc                    the document's sections, with line numbers
    rules.py term <name>            a glossary entry (Section Two), e.g. "prevent", "fierce", "damage +x"
    rules.py section <name>         any section by its heading, e.g. "archery fire"
    rules.py card <id or name>      every individual ruling (Section Three) and every mention elsewhere of a card
    rules.py terms-for <id>         the glossary entries whose terms appear in a card's game text
    rules.py grep <regex>           matching lines, each with the section it is in
    rules.py interactions           every place outside Section Three where a ruling names one or more cards
"""
import re
import sys
import unicodedata

import gempcards as g

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
    t = re.sub(r"\{\{Card\|([^}]+)\}\}", r"«\1»", t)
    t = re.sub(r"\{\{C\|([^}]+)\}\}", r"[\1]", t)
    t = re.sub(r"\{\{Twilight\|([^}]+)\}\}", r"(\1)", t)
    t = re.sub(r"\{\{SL\|([^}|]+)(?:\|[^}]*)?\}\}", r"\1", t)
    t = re.sub(r"\[\[#[^|\]]*\|([^\]]+)\]\]", r"\1", t)
    t = re.sub(r"\[\[([^|\]]+)\|([^\]]+)\]\]", r"\2", t)
    t = re.sub(r"\[\[([^\]]+)\]\]", r"\1", t)
    t = re.sub(r"'''(.*?)'''", r"\1", t)
    t = re.sub(r"''(.*?)''", r"\1", t)
    t = re.sub(r"<br\s*/?>", "\n", t)
    t = re.sub(r"</?(nowiki|span|small|u|b|i)[^>]*>", "", t)
    return t


class Rules:
    def __init__(self):
        self.lines = g.RULES_FILE.read_text(encoding="utf-8").split("\n")
        self.sections = []  # (level, title, first line index, last line index exclusive)
        heads = []
        for i, line in enumerate(self.lines):
            m = _HEADING.match(line)
            if m:
                title = clean(m.group(2)).strip()
                heads.append((len(m.group(1)), title, i))
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
        self.sec2 = next(s for s in self.sections if s[1].lower().startswith("section two"))
        self.sec3 = next(s for s in self.sections if s[1].lower().startswith("section three"))
        self.sec4 = next((s for s in self.sections if s[1].lower().startswith("section four")), None)

    def section_at(self, line_index: int):
        best = None
        for s in self.sections:
            if s[2] <= line_index < s[3] and (best is None or s[0] >= best[0]):
                best = s
        return best

    def glossary(self):
        return [s for s in self.sections if s[0] == 3 and self.sec2[2] < s[2] < self.sec2[3]]

    def text(self, section) -> str:
        return clean("\n".join(self.lines[section[2] + 1:section[3]])).strip()

    def find_sections(self, name: str):
        want = norm(name)
        exact = [s for s in self.sections if norm(s[1]) == want]
        if exact:
            return exact
        return [s for s in self.sections if want in norm(s[1])]

    def rulings(self):
        """Section Three as (card names, ruling type, text, line number)."""
        out, row = [], []
        start = self.sec3[2]
        row_line = start
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
        """Every card named outside Section Three, as (card name, line index)."""
        out = []
        for i, line in enumerate(self.lines):
            if self.sec3[2] <= i < self.sec3[3]:
                continue
            for name in re.findall(r"\{\{Card\|([^}]+)\}\}", line):
                out.append((name, i))
        return out


def card_keys(card_id=None, name=None):
    """The ways the rules might refer to a card: full name, collector's info, and (weaker) title alone."""
    keys, weak = set(), set()
    if card_id is not None:
        card = g.card_index().get(card_id)
        if card is None:
            raise SystemExit("No card definition for " + card_id)
        keys.add(norm(g.full_name(card)))
        coll = None
        m = re.search(r"collInfo\s*:\s*(\S+)", card.raw)
        if m:
            coll = m.group(1)
            keys.add(norm(coll))
        if card.field("subtitle"):
            weak.add(norm(card.field("title")))
        else:
            keys.add(norm(card.field("title")))
    else:
        keys.add(norm(name))
    return keys, weak


def cmd_card(rules: Rules, arg: str, quiet_if_none=False) -> bool:
    is_id = bool(re.match(r"^\d+_\d+$", arg))
    keys, weak = card_keys(card_id=arg) if is_id else card_keys(name=arg)
    found = False
    for names, kind, text, line_no in rules.rulings():
        normed = [norm(n) for n in names]
        hit = [n for n in normed if n in keys]
        soft = [n for n in normed if n in weak]
        if hit or soft:
            found = True
            tag = "" if hit else "  (names the title only: check which version it means)"
            print("### Individual ruling, line %d: %s%s" % (line_no, kind, tag))
            print("Cards: " + "; ".join(names))
            print(text)
            print()
    seen = set()
    for name, i in rules.mentions():
        n = norm(name)
        if n in keys or n in weak:
            sec = rules.section_at(i)
            key = (sec[2], i)
            if key in seen:
                continue
            seen.add(key)
            found = True
            lo = i
            while lo > sec[2] + 1 and rules.lines[lo - 1].strip():
                lo -= 1
            hi = i
            while hi + 1 < sec[3] and rules.lines[hi + 1].strip():
                hi += 1
            tag = "" if n in keys else "  (names the title only: check which version it means)"
            print("### Named in \"%s\", line %d%s" % (sec[1], i + 1, tag))
            print(clean("\n".join(rules.lines[lo:hi + 1])).strip())
            print()
    # A ruling on another card that speaks of this one by its title in passing ("the Ring passes to Sam").
    if is_id:
        title = g.card_index()[arg].field("title", "")
        if title and len(title) > 2:
            word = re.compile(r"(?<![A-Za-z])" + re.escape(title) + r"(?![A-Za-z])")
            for names, kind, text, line_no in rules.rulings():
                normed = [norm(n) for n in names]
                if any(n in keys or n in weak for n in normed):
                    continue
                if word.search(text):
                    found = True
                    print("### A ruling on another card mentions \"%s\", line %d: %s" % (title, line_no, kind))
                    print("Cards: " + "; ".join(names))
                    print(text)
                    print()
    if not found and not quiet_if_none:
        print("The rules do not name this card.")
    return found


def terms_for(rules: Rules, card_id: str, include_basic=False):
    card = g.card_index()[card_id]
    text = g.plain_text(card.field("gametext", "")).lower()
    keywords = (card.field("keywords", "") or "").lower()
    # The card's own name counts: the glossary has entries for Frodo, Sam and Gollum/Smeagol as characters.
    title = norm(card.field("title", ""))
    hay = text + " " + keywords + " " + title
    out = []
    for sec in rules.glossary():
        term = sec[1].lower()
        if not include_basic and term in BASIC_TERMS:
            continue
        pat = re.escape(term)
        pat = pat.replace(r"\+x", r"\+\s*\d+").replace(r"\ x", r"\s*\(?\d+\)?")
        pat = re.sub(r"\\\(s\\\)", "s?", pat)
        variants = [pat] + [re.escape(part.strip()) for part in term.split("/") if "/" in term]
        variants += STEMS.get(term, [])
        if term.endswith("ing") or term.endswith("ed"):
            pass
        elif not term.endswith("s"):
            variants.append(pat + r"(?:s|ed|ing|es)")
        if re.search(r"(?<![a-z])(?:" + "|".join(variants) + r")(?![a-z])", hay):
            out.append(sec)
    return out


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    rules = Rules()
    cmd, args = argv[1], argv[2:]
    if cmd == "toc":
        for level, title, start, end in rules.sections:
            if level <= 3 or "--all" in args:
                print("%5d  %s%s" % (start + 1, "  " * (level - 2), title))
    elif cmd in ("term", "section"):
        name = " ".join(args)
        pool = rules.glossary() if cmd == "term" else rules.sections
        want = norm(name)
        hits = [s for s in pool if norm(s[1]) == want] or [s for s in pool if want in norm(s[1])]
        if not hits:
            print("Nothing headed '%s'.  Try: rules.py grep '%s'" % (name, name))
            return 1
        for s in hits:
            print("## %s  (lines %d-%d)" % (s[1], s[2] + 1, s[3]))
            print(rules.text(s))
            print()
    elif cmd == "card":
        cmd_card(rules, " ".join(args))
    elif cmd == "terms-for":
        secs = terms_for(rules, args[0], include_basic="--all" in args)
        full = "--full" in args
        for s in secs:
            if full:
                print("## %s  (lines %d-%d)" % (s[1], s[2] + 1, s[3]))
                print(rules.text(s))
                print()
            else:
                print("%-28s lines %d-%d" % (s[1], s[2] + 1, s[3]))
    elif cmd == "grep":
        pat = re.compile(" ".join(args), re.I)
        for i, line in enumerate(rules.lines):
            if pat.search(line):
                sec = rules.section_at(i)
                print("%5d [%s] %s" % (i + 1, sec[1] if sec else "", clean(line).strip()[:300]))
    elif cmd == "interactions":
        by_para = {}
        for name, i in rules.mentions():
            sec = rules.section_at(i)
            by_para.setdefault((sec[1], i), []).append(name)
        for (title, i), names in sorted(by_para.items(), key=lambda kv: kv[0][1]):
            print("%5d [%s] %s" % (i + 1, title, "; ".join(names)))
    else:
        print(__doc__)
        return 1
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
