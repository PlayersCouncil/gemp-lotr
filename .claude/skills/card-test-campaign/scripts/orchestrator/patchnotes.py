#!/usr/bin/env python3
"""Patch notes for players, made from the campaign's findings.

Ketura's rule (rulings-log.md, "Patch notes are for players", 2026-10-12): every item is one statement of how a card
or a feature changed in play, as a player sees it on the client; nothing a player cannot see is a patch note; no
F-/Q-/G- numbers, no engine words.  Materially similar fixes go into a bucket, and a bucket of more than a handful of
cards becomes one tally line with one example.  Changes to the game as a whole come first, in a few plain sentences.

The judgement (is it seen? which bucket? the player's words) is an agent's, once per finding, kept in
<state>/patchnotes/labels.tsv.  Everything after that is this script.

    patchnotes.py todo [--limit N]
        The fixed findings that have no label yet, with their summary and outcome: what a labelling agent reads.
    patchnotes.py check
        Checks labels.tsv and buckets.tsv: known buckets, links that name exactly one card, no campaign numbers or
        engine words in a line, a short form wherever a card ends with two or more lines outside buckets.
    patchnotes.py render [--handful 5] [--all] [--title T] [--summary S] [--date YYYY-MM-DD] [--out FILE]
        The notes, as a file for gemp-lotr-async/src/main/web/patchnotes/.  Only labels not yet released, unless
        --all.  Prints to stdout without --out.
    patchnotes.py release <name>
        Marks every visible label not yet released as released in <name> (the notes file's name), so the next
        render starts after it.

labels.tsv (tab-separated, header row):
    id       a finding (F-0001), or N-<anything> for a change no finding records
    visible  y / n: can a player see the change on the client?  n rows are kept so the finding is not asked again.
    scope    card / game.  game rows are the top section.
    bucket   a slug from buckets.tsv, or empty
    cards    the blueprint ids the change reaches, ';'-separated (counted for the bucket; may be empty for game rows)
    line     the statement, card linked: "Fixed [[Old Differences]] being able to target any Dwarf or Elf, not just
             one in the skirmish."  Link an errata as [[52_91|Southern Spies]] and say it is the errata.
    short    the statement without the card, for a sub-item ("Fixed the on-play effect adding (1) and not (2)")
    released the notes file it went out in, empty until then
buckets.tsv (tab-separated, header row):
    slug     the name labels use
    tally    the line when the bucket is big, {n} for its count: "Fixed {n} cards prompting you with an optional
             trigger that would do nothing."
    example  "[[Aragorn, King in Exile]] no longer asks you to heal if there are no wounded companions with the
             Aragorn signet."
    extra    further ids the change reaches that no label names (an engine fix reaching cards not yet worked),
             ';'-separated, counted with the labels' cards
"""
import csv
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gempcards  # noqa: E402

STATE = gempcards.state_dir()
DIR = STATE / "patchnotes"
LABELS = DIR / "labels.tsv"
BUCKETS = DIR / "buckets.tsv"
LABEL_COLS = ["id", "visible", "scope", "bucket", "cards", "line", "short", "released"]
BUCKET_COLS = ["slug", "tally", "example", "extra"]
FIXED = {"fixed", "fixed-approved"}
LINK = re.compile(r"\[\[([^\[\]|\n]+)(?:\|([^\[\]\n]+))?]]")
# What a player never sees: our numbers, our tools, the engine's names.
FORBIDDEN = re.compile(r"\b[FQG]-\d{3,4}\b|\bhjson\b|\bdefinition\b|\bfilter\b|\btrigger type\b|\bCR 4\.2\b|"
                       r"\btest(s|ed)?\b|\bengine\b|\bblueprint\b|\b[A-Z][a-z]+[A-Z]\w*Effect\b|\bmemorize|"
                       r"\bcampaign\b|\bpass \d+", re.I)


def read_tsv(path, cols):
    if not path.exists():
        return []
    with open(path, encoding="utf-8", newline="") as f:
        rows = list(csv.DictReader(f, delimiter="\t"))
    for r in rows:
        for c in cols:
            r[c] = (r.get(c) or "").strip()
    return rows


def write_tsv(path, cols, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=cols, delimiter="\t", extrasaction="ignore", lineterminator="\n")
        w.writeheader()
        w.writerows(rows)


def findings():
    with open(STATE / "findings.tsv", encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f, delimiter="\t"))


def norm(s):
    return re.sub(r"[^a-z0-9]+", "", s.lower())


_names = None


def name_index():
    """name (normalised) -> set of printed cards it names, the way the hall's LibraryCardLookup reads a link:
    an errata (sets 50-89, 150-199) counts as the card it changes."""
    global _names
    if _names is None:
        full, title = defaultdict(set), defaultdict(set)
        for cid, card in gempcards.card_index().items():
            s, n = gempcards.split_id(cid)
            printed = cid
            if 50 <= s <= 89 or 150 <= s <= 199:
                base = "%d_%d" % (s - 50, n)
                printed = base if base in gempcards.card_index() else cid
            full[norm(gempcards.full_name(card))].add(printed)
            title[norm(card.field("title", ""))].add(printed)
        _names = (full, title)
    return _names


def resolve(ref):
    """(id, problem) for what a link names."""
    ref = ref.strip()
    if re.fullmatch(r"\d{1,3}_\d{1,4}", ref):
        return (ref, None) if ref in gempcards.card_index() else (None, "no card %s" % ref)
    full, title = name_index()
    hits = full.get(norm(ref)) or title.get(norm(ref)) or set()
    if len(hits) == 1:
        return next(iter(hits)), None
    if not hits:
        return None, "no card is named '%s'" % ref
    return None, "'%s' names %d cards (%s): link one by id" % (ref, len(hits), ", ".join(sorted(hits)[:6]))


def cmd_todo(argv):
    limit = int(argv[argv.index("--limit") + 1]) if "--limit" in argv else None
    have = {r["id"] for r in read_tsv(LABELS, LABEL_COLS)}
    todo = [f for f in findings() if f["status"] in FIXED and f["id"] not in have]
    print("%d fixed finding(s) without a label" % len(todo))
    for f in todo[:limit]:
        print("\n%s  %s %s  [%s]\n  found: %s\n  outcome: %s" % (f["id"], f["card"], f["name"], f["kind"],
                                                               f["summary"], f["outcome"]))


def cmd_check(argv):
    labels = read_tsv(LABELS, LABEL_COLS)
    buckets = {b["slug"]: b for b in read_tsv(BUCKETS, BUCKET_COLS)}
    known = {f["id"] for f in findings()}
    problems = []
    seen = set()
    for r in labels:
        i = r["id"]
        where = "labels %s" % i
        if i in seen:
            problems.append("%s: labelled twice" % where)
        seen.add(i)
        if not (i in known or i.startswith("N-")):
            problems.append("%s: no such finding (a change no finding records is N-something)" % where)
        if r["visible"] not in ("y", "n"):
            problems.append("%s: visible is y or n" % where)
        if r["visible"] != "y":
            continue
        if r["scope"] not in ("card", "game"):
            problems.append("%s: scope is card or game" % where)
        if r["bucket"] and r["bucket"] not in buckets:
            problems.append("%s: no bucket '%s' in buckets.tsv" % (where, r["bucket"]))
        if not r["line"]:
            problems.append("%s: no line" % where)
        for text in (r["line"], r["short"]):
            m = FORBIDDEN.search(text)
            if m:
                problems.append("%s: '%s' is not a player's word" % (where, m.group(0)))
            for ref, _ in LINK.findall(text):
                _, why = resolve(ref)
                if why:
                    problems.append("%s: %s" % (where, why))
        if r["scope"] == "card" and not LINK.search(r["line"]):
            problems.append("%s: a card line links its card" % where)
        for c in filter(None, r["cards"].split(";")):
            if c not in gempcards.card_index():
                problems.append("%s: cards names no card %s" % (where, c))
    for slug, b in buckets.items():
        if "{n}" not in b["tally"]:
            problems.append("bucket %s: the tally has no {n}" % slug)
        for text in (b["tally"], b["example"]):
            m = FORBIDDEN.search(text)
            if m:
                problems.append("bucket %s: '%s' is not a player's word" % (slug, m.group(0)))
            for ref, _ in LINK.findall(text):
                _, why = resolve(ref)
                if why:
                    problems.append("bucket %s: %s" % (slug, why))
    # sub-items need a short form: a card left with two or more lines outside the big buckets
    _, singles = arrange([r for r in labels if r["visible"] == "y" and not r["released"]], buckets, 5)
    for key, rows in singles.items():
        if len(rows) > 1:
            for r in rows:
                if not r["short"]:
                    problems.append("labels %s: %s has %d lines, so each needs a short form"
                                    % (r["id"], key, len(rows)))
    for p in problems:
        print(p)
    print("%d label(s), %d bucket(s), %d problem(s)" % (len(labels), len(buckets), len(problems)))
    return 1 if problems else 0


def card_key(r):
    """The card a card line is about: its first link."""
    m = LINK.search(r["line"])
    return m.group(0) if m else r["id"]


def arrange(rows, buckets, handful):
    """(big buckets {slug: (count, rows)}, card lines {card link: [rows]})"""
    by_bucket = defaultdict(list)
    for r in rows:
        if r["scope"] == "card" and r["bucket"]:
            by_bucket[r["bucket"]].append(r)
    big = OrderedDict()
    for slug, rs in by_bucket.items():
        cards = set()
        for r in rs:
            cards.update([c for c in r["cards"].split(";") if c] or [r["id"]])
        cards.update(filter(None, buckets.get(slug, {}).get("extra", "").split(";")))
        if len(cards) > handful:
            big[slug] = (len(cards), rs)
    singles = defaultdict(list)
    for r in rows:
        if r["scope"] == "card" and r["bucket"] not in big:
            singles[card_key(r)].append(r)
    return big, singles


def sort_name(link):
    m = LINK.match(link)
    if not m:
        return link
    return norm(m.group(2) or m.group(1))


def cmd_render(argv):
    def opt(name, default=None):
        return argv[argv.index(name) + 1] if name in argv else default

    handful = int(opt("--handful", "5"))
    labels = read_tsv(LABELS, LABEL_COLS)
    buckets = {b["slug"]: b for b in read_tsv(BUCKETS, BUCKET_COLS)}
    rows = [r for r in labels if r["visible"] == "y" and ("--all" in argv or not r["released"])]
    game = [r for r in rows if r["scope"] == "game"]
    big, singles = arrange(rows, buckets, handful)

    out = ["---"]
    if opt("--date"):
        out.append("date: " + opt("--date"))
    out.append("title: " + opt("--title", "Card fixes"))
    if opt("--summary"):
        out.append("summary: " + opt("--summary"))
    out.append("tags: Card Fixes" + (", User Interface" if game else ""))
    out += ["---", ""]
    if game:
        out += ["### Game changes", ""]
        out += ["- " + r["line"] for r in game]
        out.append("")
    if big or singles:
        out += ["### Card fixes", ""]
        for slug, (n, rs) in sorted(big.items(), key=lambda kv: -kv[1][0]):
            b = buckets[slug]
            line = b["tally"].replace("{n}", str(n))
            if b["example"]:
                line += " Example: " + b["example"]
            out.append("- " + line)
        for key in sorted(singles, key=sort_name):
            rs = singles[key]
            if len(rs) == 1:
                out.append("- " + rs[0]["line"])
            else:
                out.append("- %s:" % key)
                out += ["    - " + (r["short"] or r["line"]) for r in rs]
        out.append("")
    text = "\n".join(out)
    if opt("--out"):
        Path(opt("--out")).write_text(text, encoding="utf-8")
        print("wrote %s: %d game change(s), %d bucket(s), %d card(s)" % (opt("--out"), len(game), len(big),
                                                                       len(singles)))
    else:
        sys.stdout.write(text)


def cmd_release(argv):
    if not argv:
        raise SystemExit("release <name of the notes file>")
    labels = read_tsv(LABELS, LABEL_COLS)
    n = 0
    for r in labels:
        if r["visible"] == "y" and not r["released"]:
            r["released"] = argv[0]
            n += 1
    write_tsv(LABELS, LABEL_COLS, labels)
    print("%d label(s) marked released in %s" % (n, argv[0]))


def main():
    if len(sys.argv) < 2 or sys.argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    cmd = {"todo": cmd_todo, "check": cmd_check, "render": cmd_render, "release": cmd_release}.get(sys.argv[1])
    if not cmd:
        raise SystemExit("unknown command %s" % sys.argv[1])
    return cmd(sys.argv[2:]) or 0


if __name__ == "__main__":
    sys.exit(main())
