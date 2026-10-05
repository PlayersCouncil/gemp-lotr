#!/usr/bin/env python3
"""Prints everything one card's test work starts from, so that nobody has to go and find it.

    packet.py <id>                  the full packet
    packet.py <id> --blind          the analyst's packet: what the card says, without how it was implemented
    packet.py <id> --with-test      also prints the card's current test file and ledger
    packet.py <id> --terms          also prints the text of each glossary entry the game text touches

A packet is: where the card lives, what the reviewed spreadsheet says it is (Decipher cards), its definition, where
the two disagree, every ruling that names it, the glossary entries its text touches, and its other versions.
"""
import re
import sys

import gempcards as g
import reconcile
import rules as rulebook

FORMAT_HINTS = [
    (r"cancel", "Cancelling a skirmish: a Ring-bearer's skirmish may be cancelled in Fellowship, Towers and King "
                "block formats and may not in the others.  If this can cancel a skirmish the Ring-bearer is in, "
                "it needs a FormatRule clause and a test under each."),
    (r"\bsite \d|\bsites? \d|\b(fellowship|towers|king) block", "Names a site number or a block: test it on that block's path."),
]


def errata_family(card_id: str):
    """A card's other versions: whatever names it as parent, and whatever it names as parent."""
    cards = g.card_index()
    me = cards[card_id]
    out = []
    m = re.search(r"^\s*parent\s*:\s*(\S+)", me.raw, re.M)
    if m and m.group(1) != card_id:
        out.append(("this is a version of", m.group(1)))
    for other_id, other in cards.items():
        if other_id == card_id:
            continue
        m = re.search(r"^\s*parent\s*:\s*(\S+)", other.raw, re.M)
        if m and m.group(1) == card_id:
            out.append(("has a version", other_id))
    return out


def active_test_count(text: str) -> int:
    return len(re.findall(r"^\s*@Test\b", text, re.M))


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    if not args:
        print(__doc__)
        return 0
    blind = "--blind" in argv
    cards = g.card_index()
    tests = g.test_index()
    for n, card_id in enumerate(g.resolve_ids(args)):
        card = cards.get(card_id)
        if card is None:
            print("# %s: no card definition" % card_id)
            continue
        if n:
            print("\n" + "=" * 100 + "\n")
        print("# Card packet: %s  %s" % (card_id, g.full_name(card)))
        print()
        print("- Part of the campaign: %s, %s (%s)" % (g.segment_of(card_id), g.set_label(card_id), card.culture_file))
        print("- Definition: %s line %d" % (card.rel_file, card.line))
        test = tests.get(card_id)
        if test:
            text = test.read_text(encoding="utf-8", errors="replace")
            n = active_test_count(text)
            print("- Test file: %s (%d active tests, the stat check included)" % (g.rel(test), n))
        else:
            print("- Test file: none yet")
        ledger = g.ledger_path(card_id)
        print("- Ledger file: %s (%s)" % (ledger, "exists" if ledger.is_file() else "not written yet"))
        revision = re.search(r"^\s*revision\s*:\s*(\d+)", card.raw, re.M)
        if revision and revision.group(1) != "0":
            print("- This is revision %s of the card: its text has been changed since it was first released." % revision.group(1))
        for relation, other in errata_family(card_id):
            other_card = cards.get(other)
            print("- Versions: %s %s%s" % (relation, other, "  " + g.full_name(other_card) if other_card else ""))
        print()

        row, sheet_note = g.sheet_for(card_id)
        if row:
            print("## The card as the sheets have it (%s)" % g.sheet_label(row))
            if sheet_note:
                print("Not an exact match: %s." % sheet_note)
            if row.get("_sheet") == "master_errata":
                print("The errata sheet is about 95% accurate: some of its rows are errata that were proposed and never "
                      "released. Where it and the definition disagree, flag it; neither is assumed right.")
            shown = ["title", "subtitle", "unique", "culture", "card_type", "card_subtype", "twilight", "strength",
                     "vitality", "resistance", "signet", "site", "rarity"]
            for k in shown:
                if row.get(k):
                    print("- %s: %s" % (k, g.plain_text(row[k]) if k in ("title", "subtitle") else row[k]))
            print()
            print("Game text:")
            for line in re.split(r"<br\s*/?>|\\\\|\\(?![(\[])", row["game_text"]):
                if line.strip():
                    print("> " + line.strip())
            print()
        else:
            print("## The card as the sheets have it\nNo sheet has this card.  The definition's own game text is all there is.\n")

        print("## The card as defined" + (" (effects withheld)" if blind else ""))
        print("```hjson")
        print(card.without_effects() if blind else card.raw)
        print("```")
        print()

        if row and not blind:
            diffs = reconcile.compare(card_id)
            print("## Sheet against definition")
            if diffs:
                for field, sheet, definition in diffs:
                    print("- %s: sheet has `%s`, definition has `%s`" % (field, sheet, definition))
            else:
                print("They agree on name, cost, type, numbers and game text.")
            if diffs:
                print("Check the rulings below before treating a difference as an error: a ruling may account for it.")
            print()

        print("## Rulings that name this card")
        if not rulebook.cmd_card(card_id, quiet_if_none=True):
            print("None in either rules document.")
            print()

        secs = rulebook.terms_for(card_id)
        print("## Glossary entries the game text touches")
        if secs:
            if "--terms" in argv:
                for book, sec in secs:
                    print("### %s  (%s, lines %d-%d)" % (sec[1], book.label(), sec[2] + 1, sec[3]))
                    print(book.text(sec))
                    print()
            else:
                print(", ".join("%s%s" % (sec[1], " (5.0)" if book.in_force else "") for book, sec in secs))
                print("(read one with: rules.py term \"<name>\")")
                print()
        else:
            print("None beyond the basics.\n")

        text = (g.plain_text(card.field("gametext", "")) or "").lower()
        hints = [msg for pat, msg in FORMAT_HINTS if re.search(pat, text)]
        if hints:
            print("## Format-sensitive rules to consider")
            for h in hints:
                print("- " + h)
            print()

        if "--with-test" in argv and ledger.is_file():
            print("## Current ledger")
            print("```")
            print(ledger.read_text(encoding="utf-8").rstrip())
            print("```")
            print()
        if "--with-test" in argv and test:
            print("## Current test file")
            print("```java")
            print(test.read_text(encoding="utf-8", errors="replace"))
            print("```")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
