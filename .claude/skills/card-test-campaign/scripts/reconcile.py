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
    row = g.tsv_rows().get(card_id)
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
    check("unique", row["unique"] == "T", bool(d.get("unique")), same=lambda a, b: a == b)

    sheet_culture = CULTURES.get(row["culture"], row["culture"])
    check("culture", sheet_culture, d.get("culture"))
    if row["culture"] == "gollum_shadow":
        check("side", "shadow", d.get("side"))
    elif row["culture"] == "gollum_freeps":
        check("side", "free peoples", d.get("side"))

    template = row["template"]
    card_type = row["card_type"]
    if template == "site":
        card_type = "Site"
    elif template == "onering":
        card_type = "The One Ring"
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
            sheet_words = sorted(fold(w) for w in re.split(r"\s+or\s+", subtype))
            words = sorted(fold(w) for w in as_list(d.get("timewords")) + as_list(d.get("timeword")))
            if sheet_words != words:
                diffs.append(("timewords", subtype, ", ".join(as_list(d.get("timewords"))) or "(none)"))
        elif card_type in ("Possession", "Artifact", "The One Ring"):
            sheet_classes = sorted(fold(c) for c in subtype.split("/"))
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
    if sheet_text != defined_text:
        diffs.append(("game text", sheet_text or "(none)", defined_text or "(none)"))
    return diffs


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    as_tsv = "--tsv" in argv
    if "--all" in argv:
        ids = [c for c in g.card_index() if g.segment_of(c) == "decipher"]
        ids.sort(key=g.id_sort_key)
    elif args:
        ids = g.resolve_ids(args)
    else:
        print(__doc__)
        return 0
    if not g.tsv_rows():
        raise SystemExit("The reviewed spreadsheet is not at %s (set GEMP_CARD_TSV to say where it is)." % g.tsv_path())
    cards = g.card_index()
    differing = 0
    missing = [c for c in ids if g.segment_of(c) == "decipher" and c not in g.tsv_rows()]
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
