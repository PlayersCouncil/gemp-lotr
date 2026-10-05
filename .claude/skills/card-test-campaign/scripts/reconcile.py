#!/usr/bin/env python3
"""Compares what a Decipher card is in the reviewed spreadsheet with what it is in its HJSON definition.

    reconcile.py <ids...>           e.g. 1_40, set:1, file:set01-Elven, seg:decipher
    reconcile.py --all              every Decipher card
    reconcile.py --all --tsv        the same, as tab-separated rows (id, name, field, sheet, definition)

The spreadsheet is the independent source: it was compiled from two card databases and reviewed by hand.  A
difference here is a candidate finding, not a verdict; either side can be the one that is wrong.  Only the fields
that say what the card is are compared (name, cost, type, numbers, game text), never how it is implemented.
"""
import re
import sys
import unicodedata

import gempcards as g

CULTURES = {
    "ringwraith": "wraith", "gollum_shadow": "gollum", "gollum_freeps": "gollum", "moria_balrog": "moria",
}
BLOCKS = {"": "fellowship", "F": "fellowship", "T": "towers", "K": "king"}
CHARACTER_TYPES = {"Companion", "Minion", "Ally", "Follower"}


def fold(value) -> str:
    """A value with case, accents and surrounding space taken out of the comparison."""
    if value is None:
        return ""
    t = unicodedata.normalize("NFKD", str(value))
    t = "".join(c for c in t if not unicodedata.combining(c))
    return re.sub(r"\s+", " ", t).strip().lower()


def as_list(value) -> list:
    if value is None:
        return []
    if isinstance(value, (list, tuple)):
        return [str(v) for v in value]
    return [str(value)]


def number(value):
    if value is None or value == "":
        return None
    try:
        return int(str(value).replace("+", ""))
    except ValueError:
        return str(value)


def compare(card_id: str) -> list:
    """The differences between the two sources, as (field, spreadsheet value, definition value)."""
    row, _ = g.sheet_for(card_id)
    card = g.card_index().get(card_id)
    if row is None or card is None:
        return []
    d = card.parsed()
    diffs = []

    def check(field, sheet, definition, same=None):
        equal = same(sheet, definition) if same else fold(sheet) == fold(definition)
        if not equal:
            diffs.append((field, sheet if sheet not in (None, "") else "(none)",
                          definition if definition not in (None, "") else "(none)"))

    check("title", g.plain_text(row["title"]), g.plain_text(d.get("title")))
    check("subtitle", g.plain_text(row["subtitle"]), g.plain_text(d.get("subtitle")))
    # Uniqueness is a number of dots: none, one (unique), or the PC's two and three.
    sheet_dots = {"": 0, "T": 1, "1": 1, "2": 2, "3": 3}.get(row["unique"].strip(), row["unique"])
    defined = d.get("unique")
    defined_dots = 1 if defined is True else 0 if defined in (False, None) else defined
    check("unique", sheet_dots, defined_dots, same=lambda a, b: a == b)

    culture = (row["culture"] or "").strip().lower()
    check("culture", CULTURES.get(culture, culture), d.get("culture"))
    if culture == "gollum_shadow":
        check("side", "shadow", d.get("side"))
    elif culture == "gollum_freeps":
        check("side", "free peoples", d.get("side"))

    template = (row["template"] or "").strip().lower()
    card_type = row["card_type"]
    if template == "site":
        card_type = "Site"
    elif template == "onering":
        card_type = "The One Ring"
    elif template == "map":
        card_type = "Map"
    check("type", card_type, d.get("type"))

    subtype = row["card_subtype"]
    keywords = [fold(k) for k in as_list(d.get("keywords")) + as_list(d.get("keyword"))]
    if subtype:
        if card_type in CHARACTER_TYPES:
            check("race", subtype, d.get("race"))
        elif fold(subtype) == "support area":
            if "support area" not in keywords:
                diffs.append(("keywords", "Support Area", ", ".join(as_list(d.get("keywords"))) or "(none)"))
        elif card_type == "Event":
            # Timewords are how a definition says when an event may be played, not a property of the card, and a
            # definition may list more than the card prints (to let it be played in another phase).  Only a
            # printed timeword the definition lacks is worth a look.
            sheet_words = sorted(fold(w) for w in re.split(r"\s+or\s+", subtype))
            words = sorted(fold(w) for w in as_list(d.get("timewords")) + as_list(d.get("timeword")))
            if not set(sheet_words) <= set(words):
                diffs.append(("timewords", subtype, ", ".join(as_list(d.get("timewords"))) or "(none)"))
        elif card_type in ("Possession", "Artifact", "The One Ring"):
            parts = [c.strip() for c in subtype.split("/")]
            if len(parts) > 1 and parts[-1].lower().endswith(" weapon"):
                parts = [c if c.lower().endswith("weapon") else c + " Weapon" for c in parts]
            sheet_classes = sorted(fold(c) for c in parts)
            classes = sorted(fold(c) for c in as_list(d.get("itemclass")))
            if sheet_classes != classes:
                diffs.append(("itemclass", subtype, ", ".join(as_list(d.get("itemclass"))) or "(none)"))

    if row["twilight"] != "":
        check("twilight", number(row["twilight"]), number(d.get("twilight")), same=lambda a, b: a == b)
    for field in ("strength", "vitality", "resistance"):
        sheet, definition = number(row[field]), number(d.get(field))
        if field == "resistance" and sheet is None and card_type == "Companion":
            # Resistance was not printed on companions before Shadows; the rules gave them one after the fact.
            continue
        if sheet is not None or definition is not None:
            check(field, sheet, definition, same=lambda a, b: a == b)
    check("signet", row["signet"], d.get("signet"))

    site = row["site"]
    if template == "site":
        if site:
            m = re.match(r"^(\d)([TK]?)$", site)
            if m:
                check("site number", int(m.group(1)), number(d.get("site")), same=lambda a, b: a == b)
                check("block", BLOCKS[m.group(2)], d.get("block"))
        else:
            check("block", "shadows", d.get("block"))
    elif card_type == "Ally":
        homes = sorted(fold(h if re.search(r"[A-Za-z]$", h) else h + "F") for h in re.split(r"\s*&\s*", site) if h)
        defined = sorted(fold(h) for h in as_list(d.get("allyHome")))
        if homes != defined:
            diffs.append(("ally home", site or "(none)", ", ".join(as_list(d.get("allyHome"))) or "(none)"))
    elif site or d.get("site") is not None:
        check("site number", number(site), number(d.get("site")), same=lambda a, b: a == b)

    sheet_text, defined_text = g.plain_text(row["game_text"]), g.plain_text(d.get("gametext"))
    if squash(sheet_text) != squash(defined_text):
        diffs.append(("game text", sheet_text or "(none)", defined_text or "(none)"))
    return [x for x in diffs if (card_id, x[0]) not in known_differences()]


def squash(text: str) -> str:
    """Game text with everything taken out that makes no difference to what it says: case, accents, spacing and
    the punctuation that sheets and definitions are free with."""
    return re.sub(r"[\s.,:;'\"()\-]+", "", fold(text))


_known = None


def known_differences() -> dict:
    """Differences Ketura has ruled on and that are not to be reported again: {(id, field): why}."""
    global _known
    if _known is None:
        _known = {}
        path = g.SKILLS / "card-test-campaign" / "references" / "known-differences.tsv"
        if path.is_file():
            for line in path.read_text(encoding="utf-8").split("\n")[1:]:
                cells = line.split("\t")
                if len(cells) >= 3 and cells[0].strip():
                    _known[(cells[0].strip(), cells[1].strip())] = cells[2].strip()
    return _known


def numbers_differ(a: str, b: str) -> bool:
    return re.findall(r"\d+", str(a)) != re.findall(r"\d+", str(b))


def report(ids) -> str:
    """The differences as a document for Ketura to skim: what is most likely to matter first."""
    import rules as rulebook
    cards = g.card_index()
    named = set()
    for book, names, kind, text, line_no in rulebook.all_rulings():
        named.update(rulebook.norm(n) for n in names)
    groups = {"numbers": [], "stats": [], "wording": [], "inexact": [], "none": [], "marker": []}
    for card_id in ids:
        row, note = g.sheet_for(card_id)
        card = cards[card_id]
        other = g.revision_read_as(card_id)
        if other:
            image = re.search(r"^\s*image\s*:\s*(\S+)", card.raw, re.M)
            groups["marker"].append("| %s | %s | %d | %d | %s | %s |" % (card_id, g.full_name(card), other[0], other[1],
                                    g.sheet_label(row), image.group(1) if image else ""))
        if row is None:
            groups["none"].append("| %s | %s |" % (card_id, g.full_name(card)))
            continue
        if note:
            groups["inexact"].append("| %s | %s | %s |" % (card_id, g.full_name(card), note))
            continue
        ruling = " A Section Three entry names this card." if rulebook.norm(g.full_name(card)) in named else ""
        for field, sheet, definition in compare(card_id):
            where = g.sheet_label(row)
            line = "| %s | %s | %s | %s | %s | %s%s |" % (card_id, g.full_name(card), field, str(sheet).replace("|", "/"),
                                                          str(definition).replace("|", "/"), where, ruling)
            if field != "game text":
                groups["stats"].append(line)
            elif numbers_differ(sheet, definition):
                groups["numbers"].append(line)
            else:
                groups["wording"].append(line)
    head = "| Card | Name | Field | Sheet | Definition | Sheet row |\n|---|---|---|---|---|---|"
    out = ["# Where the generator sheets and the definitions disagree", "",
           "Every card with a sheet row matching its definition's revision, compared field by field. A difference is a",
           "flag, not a verdict: the master errata sheet holds some errata that were proposed and never released, so",
           "either side may be the one that is out of date. Case, accents, spacing and punctuation are ignored, and so",
           "are differences already ruled on (`references/known-differences.tsv` in the campaign skill).", "",
           "## Game text where the numbers differ (%d)" % len(groups["numbers"]), "", head] + groups["numbers"] + ["",
           "## Printed fields that differ (%d)" % len(groups["stats"]), "", head] + groups["stats"] + ["",
           "## Game text that differs in wording only (%d)" % len(groups["wording"]), "", head] + groups["wording"] + ["",
           "## Definitions marked as one revision whose text is another's (%d)" % len(groups["marker"]), "",
           "The definition's `revision:` says one thing and its game text is word for word the sheet's text for another.",
           "These are compared, above, with the revision they read as. The image name is shown because it carries a",
           "revision number too.", "",
           "| Card | Name | Marked | Reads as | Sheet row it matches | Image |\n|---|---|---|---|---|---|"] + groups["marker"] + ["",
           "## Definitions whose revision has no row in the errata sheet (%d)" % len(groups["inexact"]), "",
           "These were not compared. The sheet row shown to authors for them is the nearest earlier revision, marked as such.", "",
           "| Card | Name | What the sheets have |\n|---|---|---|"] + groups["inexact"] + ["",
           "## Cards in no sheet (%d)" % len(groups["none"]), "", "| Card | Name |\n|---|---|"] + groups["none"] + [""]
    return "\n".join(out)


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    as_tsv = "--tsv" in argv
    if "--all" in argv:
        ids = [c for c in g.card_index() if g.segment_of(c) in ("decipher", "vset", "errata")]
        ids.sort(key=g.id_sort_key)
    elif args:
        ids = g.resolve_ids(args)
    else:
        print(__doc__)
        return 0
    if not g.tsv_rows():
        raise SystemExit("The generator sheets are not at %s (set GEMP_CARD_SHEETS to say where they are)." % g.sheets_dir())
    if "--report" in argv:
        print(report(ids))
        return 0
    cards = g.card_index()
    differing = 0
    missing = [c for c in ids if g.sheet_for(c)[0] is None]
    for card_id in ids:
        diffs = compare(card_id)
        if not diffs:
            continue
        differing += 1
        name = g.full_name(cards[card_id])
        if as_tsv:
            for field, sheet, definition in diffs:
                print("\t".join([card_id, name, field, str(sheet), str(definition)]))
        else:
            print("%s  %s" % (card_id, name))
            for field, sheet, definition in diffs:
                print("    %-12s sheet:      %s" % (field, sheet))
                print("    %-12s definition: %s" % ("", definition))
    if not as_tsv:
        print("\n%d of %d cards differ." % (differing, len(ids)))
        if missing:
            print("Not in the spreadsheet: " + ", ".join(missing))
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
