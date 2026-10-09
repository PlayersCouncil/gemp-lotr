#!/usr/bin/env python3
"""Checks the cardInfo block of card definitions against the rest of each definition and against what the block
is for that kind of card.

    cardinfo.py <ids...>            e.g. 54_192, set:54, file:set04-Isengard-errata, seg:errata
    cardinfo.py --all               every definition
    cardinfo.py --all --tsv         the same, as tab-separated rows (id, name, field, found, wanted)
    cardinfo.py --survey            what each field is, kind by kind: where the conventions below were read from

cardInfo is not a source of truth and no test reads it (Q-0145): it is kept true to the rest of the definition, so
that the rework it was written ahead of has little to put right.  The conventions are the definitions' own, kind by
kind (Decipher sets, the Hobbit sets, PC errata, V-sets, Race to Mount Doom), as they stood on 2026-10-08:

  Decipher, sets 0 to 19   image decipher/LOTR<ss><nnn>.jpg, collInfo <set><rarity><number>, rarity; nothing else.
                           (A printed card with no definition under its own number is named by the definitions
                           that stand in for it: 15_204 to 15_206 all say 15C60.)
  Hobbit, sets 30 to 33    where there is a block: image hobbit/HDG<set><number>.jpg, collInfo the id, rarity _.
  PC errata, sets 50 to 69 //id <id>.<revision> (143 older blocks have <parent>.<revision>, which is let pass:
                           see ERRATA_ID_MAY_NAME_THE_ORIGINAL), parent the original's id, variant ERRATA,
                           variantPath errata/pc, revision 1 or more, an image under errata/ that names this card
                           (errata/<id>.jpg) or the original (LOTR-EN<ss>E<nnn>...), collInfo and rarity the
                           original's.
  V-sets, sets 100 to 103  //id <id> or <id>.<revision>; image under sets/ naming the set and number; collInfo
                           V<n><rarity><number>, the rarity being _ where the set has none.  Where the block says
                           more (all of V3 does): parent the card itself, variant ORIGINAL at revision 0 and
                           ERRATA after it, variantPath promos/pc for ORIGINAL and errata/pc for ERRATA.
  RTMD, sets 90 to 94      //id <parent>.0, variant ORIGINAL, revision 0, collInfo the parent, rarity _; parent
                           the id and variantPath decipher/rtmd in sets 91 to 94, the race's own name for the card
                           (RT_1) and variantPath rtmd in set 90.

The number an image name carries after the card's number is not compared with `revision:`; that is the business of
the revision markers (see `reconcile.py --report`).
"""
import re
import sys

import gempcards as g

FIELDS = ("//id", "parent", "variant", "variantPath", "revision", "image", "collInfo", "rarity")
PLAIN = ("image", "collInfo", "rarity")
V_FOLDERS = {100: None, 101: "sets/vset1/", 102: "sets/V2/", 103: "sets/V3/"}
# Q-0145 called "//id: 4_192.1" on 54_192 a copy-paste error, and engine pass 17 made it 54_192.1.  But the question
# said the card's neighbours had 54_x.1, and most did not: 143 errata of sets 1 to 9 still name their original there
# (as the RTMD cards of set 90 name RT_1, not 90_1).  Until Ketura says which is meant, both pass.  False: <id> only.
ERRATA_ID_MAY_NAME_THE_ORIGINAL = True


def kind_of(card_id: str) -> str:
    """Which convention a card's cardInfo follows: decipher, hobbit, errata, vset, rtmd or other."""
    return g.segment_of(card_id)


def read(card) -> dict:
    """The fields of a definition's cardInfo block as written, the commented-out `//id` included, or None if the
    definition has no block."""
    info, inside = None, False
    for depth, line in card._top_level():
        t = line.strip()
        if depth == 1:
            if inside:
                break
            if re.match(r"^cardInfo\s*:\s*\{$", t):
                info, inside = {}, True
        elif inside and depth == 2:
            m = re.match(r"^(//\s*)?(\w+)\s*:\s*(.*?)\s*$", t)
            if m:
                info[("//" if m.group(1) else "") + m.group(2)] = m.group(3)
    return info


def check(card_id: str) -> list:
    """Where a definition's cardInfo departs from the rest of the definition or from its kind's convention, as
    (field, what is there, what it should be).  What it should be is in words where no one value follows."""
    cards = g.card_index()
    card = cards.get(card_id)
    if card is None:
        return []
    kind = kind_of(card_id)
    info = read(card)
    if kind == "other" or (info is None and kind == "hobbit"):
        return []
    if info is None:
        return [("cardInfo", "(none)", "a cardInfo block")]
    out = []
    s, n = g.split_id(card_id)

    def want(field, value):
        if info.get(field) != value:
            out.append((field, info.get(field, "(none)"), value))

    def only(*fields):
        for field in info:
            if field not in fields:
                out.append((field, info[field], "(no such field on a card of this kind)"))

    revision = info.get("revision", "0")
    if not revision.isdigit():
        out.append(("revision", revision, "a number"))
        revision = "0"
    image = info.get("image", "(none)")
    rarity = info.get("rarity", "")

    if kind == "decipher":
        only(*PLAIN)
        # A printed card with no definition under its own number is stood in for by the definitions that name it:
        # Forth the Three Hunters! 15C60 is 15_204, 15_205 and 15_206, one for each culture it can be.
        m = re.fullmatch(r"%d\D+(\d+)" % s, info.get("collInfo", ""))
        if m and "%d_%s" % (s, m.group(1)) not in cards:
            n = int(m.group(1))
        want("image", "decipher/LOTR%02d%03d.jpg" % (s, n))
        want("collInfo", "%d%s%d" % (s, rarity, n))
    elif kind == "hobbit":
        only(*PLAIN)
        want("image", "hobbit/HDG%d%d.jpg" % (s, n))
        want("collInfo", card_id)
        want("rarity", "_")
    elif kind == "errata":
        only(*FIELDS)
        base = g.base_id(card_id)
        bs, bn = g.split_id(base)
        if not (ERRATA_ID_MAY_NAME_THE_ORIGINAL and info.get("//id") == "%s.%s" % (base, revision)):
            want("//id", "%s.%s" % (card_id, revision))
        want("parent", base)
        want("variant", "ERRATA")
        want("variantPath", "errata/pc")
        if revision == "0":
            out.append(("revision", info.get("revision", "(none)"), "1 or more: an erratum is a revision"))
        if image != "errata/%s.jpg" % card_id and not re.fullmatch(r"errata/LOTR-EN%02d[ES]%03d\.\d+(_card)?\.jpg" % (bs, bn), image):
            out.append(("image", image, "errata/%s.jpg or errata/LOTR-EN%02dE%03d.<revision>_card.jpg" % (card_id, bs, bn)))
        original = read(cards[base]) if base in cards else None
        if original and "collInfo" in original and "rarity" in original:
            want("collInfo", original["collInfo"])
            want("rarity", original["rarity"])
        else:
            want("collInfo", "%d%s%d" % (bs, rarity, bn))
    elif kind == "vset":
        only(*FIELDS)
        v = s - 100
        with_revision = "%s.%s" % (card_id, revision)
        if v == 3 or "revision" in info and info.get("//id") != card_id:
            want("//id", with_revision)
        else:
            want("//id", card_id)
        folder = V_FOLDERS.get(s)
        if not re.fullmatch(r"%sLOTR-ENV%d[ES]%03d\.\d+(_card)?\.jpg" % (re.escape(folder) if folder else r"[\w/]+/", v, n), image):
            out.append(("image", image, "%sLOTR-ENV%dS%03d.<revision>_card.jpg" % (folder or "<folder>/", v, n)))
        if folder:
            want("collInfo", "V%d%s%d" % (v, rarity, n))
        said_more = [f for f in ("parent", "variant", "variantPath") if f in info]
        if said_more or v == 3:
            variant = "ORIGINAL" if revision == "0" else "ERRATA"
            want("parent", card_id)
            want("variant", variant)
            want("variantPath", "promos/pc" if variant == "ORIGINAL" else "errata/pc")
            want("revision", revision)
    elif kind == "rtmd":
        only(*FIELDS)
        parent = info.get("parent", card_id)
        want("//id", "%s.%s" % (parent, revision))
        want("variant", "ORIGINAL")
        want("revision", "0")
        want("collInfo", parent)
        want("rarity", "_")
        if s == 90:
            m = re.fullmatch(r"([A-Z]{2})_(\d+)", parent)
            if not m:
                out.append(("parent", parent, "the race's name for the card, as RT_1"))
            elif image != "race/%s/LOTR-EN%sS%03d.0_card.jpg" % (m.group(1), m.group(1), int(m.group(2))):
                out.append(("image", image, "race/%s/LOTR-EN%sS%03d.0_card.jpg" % (m.group(1), m.group(1), int(m.group(2)))))
            want("variantPath", "rtmd")
        else:
            want("parent", card_id)
            want("variantPath", "decipher/rtmd")
            want("image", "RTMD/LOTR-EN%dS%02d.0_card.jpg" % (s, n))
    return out


def survey() -> str:
    """Each field's values kind by kind, with the card's own set, number, parent and revision taken out, most
    common first: what the conventions in check() were read from."""
    by_kind = {}
    for card_id, card in g.card_index().items():
        info = read(card)
        kind = kind_of(card_id)
        if kind == "errata":
            kind = "errata of sets 1 to 9" if g.split_id(card_id)[0] < 60 else "errata of sets 10 to 19"
        elif kind in ("vset", "rtmd"):
            kind = g.set_label(card_id)
        counts = by_kind.setdefault(kind, {})
        if info is None:
            counts.setdefault("cardInfo", {}).setdefault("(none)", []).append(card_id)
            continue
        s, n = g.split_id(card_id)
        bs, bn = g.split_id(g.base_id(card_id))
        revision = info.get("revision", "0")
        for field in FIELDS:
            value = info.get(field, "(none)")
            shape = value
            for old, new in ((card_id, "<id>"), (g.base_id(card_id), "<original>"), ("%02d%03d" % (bs, bn), "<ss><nnn>"),
                             ("EN%02dE%03d" % (bs, bn), "EN<ss>E<nnn>"), ("EN%02dS%03d" % (bs, bn), "EN<ss>S<nnn>"),
                             ("ENV%dS%03d" % (s - 100, n), "ENV<v>S<nnn>"), ("ENV%dE%03d" % (s - 100, n), "ENV<v>E<nnn>"),
                             ("EN%dS%02d." % (s, n), "EN<set>S<nn>.")):
                shape = shape.replace(old, new)
            if field == "collInfo":
                shape = re.sub(r"^(V?)%d(.*?)%d$" % (bs if s < 100 else s - 100, bn), r"\1<set>\2<n>", shape)
            if field in ("//id", "image") and revision != "0":
                shape = re.sub(r"\.%s(?=$|[._])" % revision, ".<revision>", shape)
            if field == "image" and "hobbit/HDG%d%d." % (s, n) in shape:
                shape = shape.replace("HDG%d%d." % (s, n), "HDG<set><n>.")
            counts.setdefault(field, {}).setdefault(shape, []).append(card_id)
    out = []
    for kind in sorted(by_kind):
        total = sum(len(ids) for ids in by_kind[kind].get("rarity", {}).values()) + len(by_kind[kind].get("cardInfo", {}).get("(none)", []))
        out.append("== %s (%d definitions)" % (kind, total))
        for field in ("cardInfo",) + FIELDS:
            shapes = by_kind[kind].get(field)
            if not shapes or list(shapes) == ["(none)"] and field != "cardInfo":
                continue
            out.append("   %s" % field)
            ranked = sorted(shapes.items(), key=lambda kv: -len(kv[1]))
            for shape, ids in ranked[:8]:
                out.append("     %5d  %-44s %s" % (len(ids), shape, " ".join(ids) if len(ids) <= 6 else ""))
            if len(ranked) > 8:
                out.append("            and %d more values, on %d definitions" % (len(ranked) - 8, sum(len(i) for _, i in ranked[8:])))
    return "\n".join(out)


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    if "--survey" in argv:
        print(survey())
        return 0
    if "--all" in argv:
        ids = sorted(g.card_index(), key=g.id_sort_key)
    elif args:
        ids = g.resolve_ids(args)
    else:
        print(__doc__)
        return 0
    cards = g.card_index()
    departing = 0
    for card_id in ids:
        found = check(card_id)
        if not found:
            continue
        departing += 1
        name = g.full_name(cards[card_id])
        if "--tsv" in argv:
            for field, there, wanted in found:
                print("\t".join([card_id, name, field, str(there), str(wanted)]))
        else:
            print("%s  %s" % (card_id, name))
            for field, there, wanted in found:
                print("    %-12s is:        %s" % (field, there))
                print("    %-12s should be: %s" % ("", wanted))
    if "--tsv" not in argv:
        print("\n%d of %d definitions depart." % (departing, len(ids)))
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
