#!/usr/bin/env python3
"""Prints everything one card's test work starts from, so that nobody has to go and find it.

    packet.py <id>                  the full packet
    packet.py <id> --blind          the analyst's packet: what the card says, without how it was implemented
    packet.py <id> --with-test      also prints the card's current test file and ledger, and for an erratum the
                                    definition of the card it is a version of
    packet.py <id> --only-withheld  only what a blind packet held back, for the second stage: the definition in
                                    full, where sheet and definition disagree, for an erratum the original's
                                    definition, and the current test file; not the sheet, the rulings and the
                                    glossary again
    packet.py <id> --terms          also prints the text of each glossary entry the game text touches

    packet.py --find <regex> [--skip <ids>]
                                    for choosing helper cards: every card whose printed fields and game text
                                    match (id, name, culture, type, race, numbers, keywords, game text; nothing of
                                    how it is implemented), without the ids after --skip and the cards they are
                                    versions of.  The regex is tried, whatever its capitals, against the lines as
                                    printed, joined by " | ": "\\[gollum\\] condition", "^1_.*\\[gandalf\\].* Man\\b".
                                    A race or an item class stands after the type and a comma, with no label, in
                                    whatever capitals the definition has: "companion, Dwarf\\b" finds the Dwarf
                                    companions and "possession, pipe\\b" the pipes ("race: Dwarf" finds nothing).
                                    Each card prints up to three lines (the id line, "keywords:", the game text):
                                    to leave cards out, write it into the regex ("^(?!.*\\[elven\\]).*companion, Elf")
                                    or filter the id lines only (grep -E '^[0-9]' first), since grep -v on the
                                    whole output keeps the other two lines of a card it drops
    packet.py --defs <ids> [--skip <ids>]
                                    the definitions of helper cards and nothing else, without their cardInfo,
                                    lore and alternate images

--skip takes the ids of your batch (separated by spaces or commas): their definitions and their originals' stay
shut in stage 1, and neither --find nor --defs will print them.

A packet is: where the card lives, the picture the client shows for it (PC cards), what the reviewed spreadsheet
says it is (Decipher cards) or the errata sheet's row, a later row of that sheet where it has one above the
definition's revision (a row known never to have been released is named in one line and not printed:
references/unreleased-sheet-rows.tsv), the game text the definition had before a commit of the campaign's changed it, its
definition, where sheet and definition disagree (and where the definition's cardInfo is out of step with the rest
of it, it has no `side:`, or an erratum of a site points the other way from its original), every ruling that names it, then under headings of their own the rules passages that
quote another card of its title and the rulings on other cards that only have its title in their text, the
glossary entries its text touches, and its other versions.
"""
import re
import sys

import cardinfo
import gempcards as g
import reconcile
import rules as rulebook

FORMAT_HINTS = [
    (r"cancel", "Cancelling a skirmish: a Ring-bearer's skirmish may be cancelled in Fellowship, Towers and King "
                "block formats and may not in the others.  If this can cancel a skirmish the Ring-bearer is in, "
                "it needs a FormatRule clause and a test under each."),
    (r"\bsite \d|\bsites? \d|\b(fellowship|towers|king) block", "Names a site number or a block: test it on that block's path."),
]


FREE_PEOPLES_CULTURES = {"dwarven", "elven", "gandalf", "gondor", "rohan", "shire", "esgaroth"}


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


def beyond_the_sheet(card_id: str, card) -> list:
    """What the comparison with the sheet does not look at and a packet should still say of a definition: that it
    has no `side:` line, and where its cardInfo block is out of step with the rest of it (Q-0145)."""
    out = []
    culture = (card.field("culture", "") or "").strip()
    if culture and card.field("side") is None:
        # CultureFieldProcessor: every culture but [gollum] carries its side, and a card with no side does not load.
        side = "Free Peoples" if culture.lower() in FREE_PEOPLES_CULTURES else "Shadow"
        out.append("side: the definition has no `side:` line, and the game takes the side from the culture (%s, so %s). "
                   "Nothing plays differently for it: say so in the ledger's Notes and in your report, and raise no "
                   "finding." % (culture, side))
    if (card.field("type", "") or "").strip().lower() == "site":
        # No sheet has a site's direction, so an erratum's is compared with its original's (Doorway to Doom 68_134
        # had Left where 18_134 has Right: F-0302).
        mine = (card.field("direction", "") or "").strip()
        for relation, other in errata_family(card_id):
            original = g.card_index().get(other)
            if relation != "this is a version of" or original is None:
                continue
            theirs = (original.field("direction", "") or "").strip()
            if mine.lower() == theirs.lower():
                out.append("direction: `%s`, as the original %s has it (no sheet has a site's direction)." % (mine or "(none)", other))
            else:
                out.append("direction: this erratum has `%s` and the original %s has `%s`. No sheet has a site's direction "
                           "and no erratum has changed one, so one of the two definitions is wrong: the two card "
                           "pictures say which was printed. Assert the definition's value in the stats test and report "
                           "it as a finding (F-0302, Doorway to Doom 68_134)." % (mine or "(none)", other, theirs or "(none)"))
    for field, there, wanted in cardinfo.check(card_id):
        out.append("cardInfo %s: the definition has `%s` where the rest of the definition and the other cards of its kind "
                   "say `%s`. No test reads cardInfo; report it as a `data` finding (Q-0145; `cardinfo.py %s`)."
                   % (field, there, wanted, card_id))
    return out


def active_test_count(text: str) -> int:
    return len(re.findall(r"^\s*@Test\b", text, re.M))


def parked_test_count(text: str) -> int:
    """The @Ignores among a file's annotations, whether one has a line to itself or shares it with @Test."""
    return sum(len(re.findall(r"@Ignore\b", line)) for line in text.split("\n") if line.lstrip().startswith("@"))


SHEET_FIELDS = ["title", "subtitle", "unique", "culture", "card_type", "card_subtype", "twilight", "strength",
                "vitality", "resistance", "signet", "site", "rarity"]


def print_row(row):
    """A sheet row's printed fields and game text."""
    for k in SHEET_FIELDS:
        if row.get(k):
            print("- %s: %s" % (k, g.plain_text(row[k]) if k in ("title", "subtitle") else row[k]))
    print()
    print("Game text:")
    for line in re.split(r"<br\s*/?>|\\\\|\\(?![(\[])", row["game_text"]):
        if line.strip():
            print("> " + line.strip())
    print()


def printed(card_id: str, card) -> list:
    """What is printed on a card, as the definition has it, in three lines at most: nothing of its effects."""
    def has(name):
        return (card.field(name, "") or "").strip()
    culture, kind, race = has("culture"), has("type"), has("race")
    # A race or an item class after the type: "[shire] possession, Pipe", "[gondor] possession, Hand weapon, Shield".
    m = re.search(r"^\s*itemclass\s*:\s*(\[.*?\]|[^\n\[]*)", card.raw, re.S | re.M)
    classes = [w.strip() for w in re.split(r"[\n,\[\]]", m.group(1)) if w.strip()] if m else []
    first = "%s  %s  %s%s%s%s" % (card_id, g.full_name(card), "[%s] " % culture.lower() if culture else "", kind.lower(),
                                   ", " + race if race else "", "".join(", " + c for c in classes))
    if has("unique").lower() == "true":
        first += "; unique"
    numbers = [(label, has(name)) for label, name in (("twilight", "twilight"), ("strength", "strength"), ("vitality", "vitality"),
                                                      ("resistance", "resistance"), ("site", "site"), ("signet", "signet"),
                                                      ("block", "block"), ("direction", "direction"))]
    numbers = ["%s %s" % (label, value) for label, value in numbers if value]
    if numbers:
        first += "; " + ", ".join(numbers)
    out = [first]
    m = re.search(r"^\s*keywords?\s*:\s*(\[.*?\]|[^\n\[]*)", card.raw, re.S | re.M)
    words = [w.strip() for w in re.split(r"[\n,\[\]]", m.group(1)) if w.strip()] if m else []
    if words:
        out.append("keywords: " + ", ".join(words))
    text = g.plain_text(has("gametext"))
    if text:
        out.append(text)
    return out


def take_ids(argv, flag) -> list:
    """The ids that follow a flag, up to the next option: separated by spaces or by commas."""
    if flag not in argv:
        return []
    out = []
    for a in argv[argv.index(flag) + 1:]:
        if a.startswith("--"):
            break
        out += [x for x in a.split(",") if x]
    return out


def shut(skipped) -> set:
    """The ids given to --skip with the cards they are versions of: a batch's cards and their originals."""
    cards = g.card_index()
    out = set(skipped)
    for card_id in skipped:
        m = re.search(r"^\s*parent\s*:\s*(\d+_\d+)", cards[card_id].raw, re.M) if card_id in cards else None
        if m:
            out.add(m.group(1))
    return out


def find(argv) -> int:
    i = argv.index("--find")
    if i + 1 >= len(argv) or argv[i + 1].startswith("--"):
        print("--find: give a regular expression to try against each card's printed lines")
        return 1
    try:
        wanted = re.compile(argv[i + 1], re.I)
    except re.error as e:
        print("--find: '%s' is not a regular expression (%s)" % (argv[i + 1], e))
        return 1
    skipped = shut(take_ids(argv, "--skip"))
    found, left_out = 0, []
    for card_id, card in sorted(g.card_index().items(), key=lambda kv: g.id_sort_key(kv[0])):
        lines = printed(card_id, card)
        if not wanted.search(" | ".join(lines)):
            continue
        if card_id in skipped:
            left_out.append(card_id)
            continue
        found += 1
        print(lines[0])
        for line in lines[1:]:
            print("    " + line)
    print("%d card(s) match /%s/%s" % (found, argv[i + 1], "; left out as shut: " + ", ".join(left_out) if left_out else ""))
    return 0


def without_bookkeeping(card) -> str:
    """A definition without its cardInfo, lore, promotional text and alternate images: what plays, and its name."""
    out, skipping = [], False
    for depth, line in card._top_level():
        if depth == 1:
            if skipping:
                skipping = False
                if line.strip().rstrip(",") in ("}", "]"):
                    continue
            if re.match(r"^\s*(cardInfo|alts|lore|promotext)\s*:", line):
                skipping = line.rstrip().endswith(("{", "["))
                continue
        if skipping:
            continue
        out.append(line)
    return "\n".join(out)


def defs(argv) -> int:
    cards = g.card_index()
    skipped = shut(take_ids(argv, "--skip"))
    ids = take_ids(argv, "--defs")
    if not ids:
        print("--defs: give the ids of the cards whose definitions to print")
        return 1
    for card_id in g.resolve_ids(ids):
        card = cards.get(card_id)
        if card is None:
            print("# %s: no card definition\n" % card_id)
        elif card_id in skipped:
            print("# %s  %s: shut (one of the ids after --skip, or the card one of them is a version of)\n" % (card_id, g.full_name(card)))
        else:
            print("# %s  %s (%s line %d)" % (card_id, g.full_name(card), card.rel_file, card.line))
            print("```hjson")
            print(without_bookkeeping(card))
            print("```\n")
    return 0


def main(argv):
    if "--find" in argv:
        return find(argv)
    if "--defs" in argv:
        return defs(argv)
    args = [a for a in argv[1:] if not a.startswith("--")]
    if not args:
        print(__doc__)
        return 0
    # --only-withheld: the second stage's packet, what the blind one held back and nothing it has already shown.
    only_withheld = "--only-withheld" in argv
    blind = "--blind" in argv and not only_withheld
    with_test = "--with-test" in argv or only_withheld
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
            parked = parked_test_count(text)
            print("- Test file: %s (%d active tests, the stat check included%s)"
                  % (g.rel(test), n - parked, ", and %d parked" % parked if parked else ""))
        else:
            wanted = g.new_test_path(card_id)
            print("- Test file: none yet" + ("; a new one is %s (class %s)" % (g.rel(wanted), g.test_class_name(wanted)) if wanted else ""))
        ledger = g.ledger_path(card_id)
        print("- Ledger file: %s (%s)" % (ledger, "exists" if ledger.is_file() else "not written yet"))
        revision = re.search(r"^\s*revision\s*:\s*(\d+)", card.raw, re.M)
        if revision and revision.group(1) != "0":
            print("- This is revision %s of the card: its text has been changed since it was first released." % revision.group(1))
        for relation, other in errata_family(card_id):
            other_card = cards.get(other)
            print("- Versions: %s %s%s" % (relation, other, "  " + g.full_name(other_card) if other_card else ""))
        image = g.client_image(card_id)
        if image:
            print("- Picture the client shows: %s (PC_Cards.js line %d)" % image)
        for unreleased, why in g.unreleased_rows_of(card_id):
            print("- A row of the master errata sheet is left out as never released: %s. %s"
                  % (g.sheet_label(unreleased).replace("master errata sheet, ", ""), why))
        for reminder in g.reminders_outside_helper(card.field("gametext", "")):
            elsewhere = [c for c in g.reminder_in_helper_elsewhere(reminder) if c != card_id]
            print("- Reminder text: the game text writes the reminder %s in `<i>` and not in `<helper>`. The conformance "
                  "check leaves a reminder out of the ledger's clauses only where it stands in `<helper>`, so this one is "
                  "written into its clause as printed, with a line in Notes saying so. %s"
                  % (reminder if len(reminder) <= 90 else reminder[:87] + "...)",
                     ("%d other definition(s) write the same reminder in `<helper>` (%s): the markup is a `gametext` "
                      "finding to report (F-0301, Robin Smallburrow 68_111)." % (len(elsewhere), ", ".join(sorted(elsewhere, key=g.id_sort_key)[:4]) + (" ..." if len(elsewhere) > 4 else "")))
                     if elsewhere else "No definition writes this reminder in `<helper>`, so there is none to bring it into line with."))
        print()

        row, sheet_note = g.sheet_for(card_id)
        if only_withheld:
            pass
        elif row:
            print("## The card as the sheets have it (%s)" % g.sheet_label(row))
            if sheet_note:
                print("Not an exact match: %s." % sheet_note)
            if row.get("_sheet") == "master_errata" and str(row.get("revision", "")).strip() != str(g.revision_of(card)):
                print("The definition is marked revision %d and the sheet's row is revision %s.  The numbers not lining "
                      "up is already known (F-0032, with Ketura): say so on the ledger's Sources line and do not "
                      "report it again." % (g.revision_of(card), row.get("revision", "?")))
            if row.get("_sheet") == "master_errata":
                print("The errata sheet is about 95% accurate: some of its rows are errata that were proposed and never "
                      "released. Where it and the definition disagree, flag it; neither is assumed right.")
            print_row(row)
        else:
            print("## The card as the sheets have it\nNo sheet has this card.  The definition's own game text is all there is.\n")

        for later in ([] if only_withheld else g.later_sheet_rows(card_id, row)):
            print("## A later row of the sheet (%s)" % g.sheet_label(later))
            print("The definition says `revision: %d` and the sheet has this row above it.  Either the definition is "
                  "behind the sheet, its labels and `gametext:` stale (F-0294, Shadowfax 67_24: a `gametext` finding, and "
                  "the later text is the one to test), or the row is an erratum that was never released.  The picture "
                  "the client shows says which%s: fetch it and read it, and say which in the ledger's Sources line "
                  "(`lookup.py F-0294`)." % (g.revision_of(card), " (" + image[0] + ")" if image else ""))
            print_row(later)

        before = None if only_withheld else g.gametext_before_campaign(card_id)
        if before:
            then, commits = before
            print("## The game text before the campaign changed it")
            print("The definition's `gametext:` is not what it was when the campaign began; changed by %s.  Until then it read:"
                  % ("; ".join("%s (%s, \"%s\")" % c for c in commits) or "a commit of the campaign's"))
            for line in re.split(r"<br\s*/?>|\\\\|\\(?![(\[])", then):
                if line.strip():
                    print("> " + line.strip())
            print("So where the sheet and the definition's game text agree, that may be one witness and not two: a "
                  "finding, question, note or old test from before that commit took the card by the text above "
                  "(`lookup.py F-0297`).  The picture the client shows is what was released%s."
                  % (": " + image[0] if image else ""))
            print()

        print("## The card as defined" + (" (effects withheld)" if blind else ""))
        print("```hjson")
        print(card.without_effects() if blind else card.raw)
        print("```")
        print()

        beyond = [] if blind else beyond_the_sheet(card_id, card)
        if (row or beyond) and not blind:
            diffs = reconcile.compare(card_id) if row else []
            print("## Sheet against definition")
            if diffs:
                for field, sheet, definition in diffs:
                    print("- %s: sheet has `%s`, definition has `%s`" % (field, sheet, definition))
            elif row:
                print("They agree on name, cost, type, numbers and game text.")
            if diffs:
                print("Check the rulings below before treating a difference as an error: a ruling may account for it.")
            if beyond:
                print("Outside what the sheet is compared on:")
                for line in beyond:
                    print("- " + line)
            print()

        named, mentioned, other_title = ([], [], []) if only_withheld else rulebook.card_rulings(card_id)
        if not only_withheld:
            print("## Rulings that name this card")
        for entry in named:
            print(entry + "\n")
        if not named and not only_withheld:
            print("None in either rules document.\n")
        if other_title:
            print("## Rules passages that quote another card of this title")
            print(rulebook.OTHER_TITLE + "\n")
            for entry in other_title:
                print(entry + "\n")
        if mentioned:
            print("## Rulings on other cards that only mention its name")
            print(rulebook.MENTIONS.replace("The rest only mention the name: each", "Each") + "\n")
            for entry in mentioned:
                print(entry + "\n")

        secs = [] if only_withheld else rulebook.terms_for(card_id)
        if not only_withheld:
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
        elif not only_withheld:
            print("None beyond the basics.\n")

        text = (g.plain_text(card.field("gametext", "")) or "").lower()
        hints = [] if only_withheld else [msg for pat, msg in FORMAT_HINTS if re.search(pat, text)]
        if hints:
            print("## Format-sensitive rules to consider")
            for h in hints:
                print("- " + h)
            print()

        if with_test:
            # An erratum is usually its original with a few words changed, and its definition a copy with the
            # change made: the original's definition is read beside it in stage 2 (never in a blind packet).
            for relation, other in errata_family(card_id):
                original = cards.get(other)
                if relation == "this is a version of" and original is not None:
                    print("## The original as defined: %s  %s (%s line %d)" % (other, g.full_name(original), original.rel_file, original.line))
                    print("```hjson")
                    print(original.raw)
                    print("```")
                    print()
        if with_test and ledger.is_file() and not only_withheld:
            print("## Current ledger")
            print("```")
            print(ledger.read_text(encoding="utf-8").rstrip())
            print("```")
            print()
        if with_test and test:
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
