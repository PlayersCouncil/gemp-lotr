#!/usr/bin/env python3
"""
add_promo.py - one-shot bookkeeping for a new Player's Council promo (alt-art) card.

Given the blueprint ID of the card being promo'd, this script:

  1. Finds the card definition (title/subtitle/type) in gemp-lotr-cards.
  2. Picks the next free fake ID in that set (X_Z, Z = highest number seen anywhere + 1),
     checking card definitions, blueprintMapping.txt, PC_Cards.js and PC-Packs.hjson for collisions.
  3. Appends the alias to gemp-lotr-cards/src/main/resources/blueprintMapping.txt
  4. Appends the image URL to gemp-lotr-async/src/main/web/js/gemp-022/PC_Cards.js
  5. Adds the promo to the requested packs in gemp-lotr-cards/src/main/resources/product/PC-Packs.hjson
  6. If the base card is a site, marks the new ID horizontal in Card.isBlueprintHorizontal
     (gemp-lotr-async/src/main/web/js/gemp-022/cards/Card.js)
  7. Optionally renames/rotates the image file and uploads it with scp.

Usage examples:

  python scripts/add_promo.py 4_267 --comment "Eomer, TMOR FA - 2026-06 WC Circuit" --image C:\\promos\\eomer.jpg
  python scripts/add_promo.py 4_267 --comment "..." --image eomer.jpg --upload gemp@test:/var/www/i/promos/
  python scripts/add_promo.py --find "Rosie"            # look up a blueprint ID by title
  python scripts/add_promo.py 4_267 --check             # just report the next free ID and collisions
  python scripts/add_promo.py 4_267 --comment "..." --dry-run

Only the Python standard library is required.  If Pillow is installed the image's orientation is
checked (and can be rotated with --rotate); otherwise that step is skipped with a note.
"""

import argparse
import os
import re
import shutil
import subprocess
import sys
import unicodedata

IMAGE_HOST = "https://i.lotrtcgpc.net/promos/"
DEFAULT_PACKS = ["(S)PC Promo Art Selection", "Random PC Full Art"]

ID_RE = re.compile(r"^(\d+)_(\d+)$")


# ----------------------------------------------------------------------------------------------
# Repo layout
# ----------------------------------------------------------------------------------------------

def find_repo_root(start):
    """Walk upwards from `start` until a directory containing gemp-lotr-cards is found."""
    cur = os.path.abspath(start)
    while True:
        if os.path.isdir(os.path.join(cur, "gemp-lotr-cards")) and os.path.isdir(os.path.join(cur, "gemp-lotr-async")):
            return cur
        parent = os.path.dirname(cur)
        if parent == cur:
            sys.exit("Could not locate the gemp-lotr repo root (a directory containing gemp-lotr-cards and gemp-lotr-async). "
                     "Run this script from inside the repo or pass --root.")
        cur = parent


class Paths:
    def __init__(self, root):
        self.root = root
        self.cards_dir = os.path.join(root, "gemp-lotr-cards", "src", "main", "resources", "cards")
        self.mapping = os.path.join(root, "gemp-lotr-cards", "src", "main", "resources", "blueprintMapping.txt")
        self.packs = os.path.join(root, "gemp-lotr-cards", "src", "main", "resources", "product", "PC-Packs.hjson")
        self.pc_cards_js = os.path.join(root, "gemp-lotr-async", "src", "main", "web", "js", "gemp-022", "PC_Cards.js")
        self.card_js = os.path.join(root, "gemp-lotr-async", "src", "main", "web", "js", "gemp-022", "cards", "Card.js")

        for p in (self.cards_dir, self.mapping, self.packs, self.pc_cards_js, self.card_js):
            if not os.path.exists(p):
                sys.exit("Expected file/folder not found: " + p)


# ----------------------------------------------------------------------------------------------
# Text helpers - every file is rewritten with the line endings it already uses
# ----------------------------------------------------------------------------------------------

def read_text(path):
    with open(path, "r", encoding="utf-8", newline="") as f:
        return f.read()


def write_text(path, text):
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(text)


def eol_of(text):
    return "\r\n" if "\r\n" in text else "\n"


def strip_id_suffix(bp):
    """1_366T / 1_366* -> 1_366"""
    return bp.rstrip("*").rstrip("T")


def normalise(s):
    """Case/accent-insensitive comparison key for --find."""
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c)).lower()


# ----------------------------------------------------------------------------------------------
# Card definitions
# ----------------------------------------------------------------------------------------------

CARD_START_RE = re.compile(r"^\s*(?://\s*)?(\d+_\d+):\s*\{")


def iter_hjson_files(cards_dir):
    for dirpath, _, files in os.walk(cards_dir):
        for name in files:
            if name.endswith(".hjson"):
                yield os.path.join(dirpath, name)


def scan_card_definitions(cards_dir):
    """Returns {blueprintId: {file, line, title, subtitle, type, commented}} for every card block,
    including commented-out ones (their IDs are still reserved)."""
    cards = {}
    for path in iter_hjson_files(cards_dir):
        lines = read_text(path).splitlines()
        for i, line in enumerate(lines):
            m = CARD_START_RE.match(line)
            if not m:
                continue
            bp = m.group(1)
            commented = line.lstrip().startswith("//")
            info = {"file": path, "line": i + 1, "title": None, "subtitle": None, "type": None, "commented": commented}
            # Look ahead inside the block for the fields we care about
            for look in lines[i + 1:i + 80]:
                stripped = look.strip()
                if stripped.startswith("//"):
                    stripped = stripped[2:].strip()
                if CARD_START_RE.match(look):
                    break
                for key in ("title", "subtitle", "type"):
                    if info[key] is None and stripped.startswith(key + ":"):
                        info[key] = stripped[len(key) + 1:].strip().strip('"')
                if info["title"] and info["type"]:
                    break
            # A live definition wins over a commented one for display purposes
            if bp not in cards or (cards[bp]["commented"] and not commented):
                cards[bp] = info
    return cards


# ----------------------------------------------------------------------------------------------
# Other bookkeeping files
# ----------------------------------------------------------------------------------------------

def scan_mapping(path):
    """Returns (aliases {promoId: baseId}, all ids mentioned)."""
    aliases = {}
    ids = set()
    for line in read_text(path).splitlines():
        s = line.strip()
        if not s or s.startswith("#"):
            continue
        parts = [p.strip() for p in s.split(",")]
        if len(parts) != 2:
            continue
        a, b = strip_id_suffix(parts[0]), strip_id_suffix(parts[1])
        aliases[a] = b
        ids.update([a, b])
    return aliases, ids


PC_CARDS_ENTRY_RE = re.compile(r"""^\s*(?://\s*)?'(\d+_\d+)'\s*:\s*'([^']*)'""")


def scan_pc_cards(path):
    """Returns {blueprintId: url} for every entry, commented ones included."""
    entries = {}
    for line in read_text(path).splitlines():
        m = PC_CARDS_ENTRY_RE.match(line)
        if m:
            entries[m.group(1)] = m.group(2)
    return entries


PACK_ITEM_RE = re.compile(r"^\s*(?:#\s*)?\d+x(\d+_\d+)")


def scan_packs(path):
    ids = set()
    for line in read_text(path).splitlines():
        m = PACK_ITEM_RE.match(line)
        if m:
            ids.add(m.group(1))
    return ids


# ----------------------------------------------------------------------------------------------
# ID allocation
# ----------------------------------------------------------------------------------------------

def next_free_id(set_no, cards, mapping_ids, pc_card_ids, pack_ids):
    highest = 0
    sources = {}
    for bp in set(cards) | mapping_ids | pc_card_ids | pack_ids:
        m = ID_RE.match(bp)
        if not m or int(m.group(1)) != set_no:
            continue
        n = int(m.group(2))
        if n > highest:
            highest = n
            sources[n] = bp
    return highest + 1, highest


def collisions_for(bp, cards, mapping_ids, pc_card_ids, pack_ids):
    found = []
    if bp in cards:
        found.append("card definition in " + os.path.relpath(cards[bp]["file"]) + ":" + str(cards[bp]["line"]))
    if bp in mapping_ids:
        found.append("blueprintMapping.txt")
    if bp in pc_card_ids:
        found.append("PC_Cards.js")
    if bp in pack_ids:
        found.append("PC-Packs.hjson")
    return found


# ----------------------------------------------------------------------------------------------
# Image naming
# ----------------------------------------------------------------------------------------------

def default_image_name(set_no, card_no, variant, pc_card_urls):
    """LOTR-EN04U267.0_card.jpg; the revision counts how many promo images already exist for that base card."""
    stem = "LOTR-EN%02d%s%03d." % (set_no, variant, card_no)
    existing = [u for u in pc_card_urls.values() if os.path.basename(u).startswith(stem)]
    revs = []
    for u in existing:
        m = re.search(re.escape(stem) + r"(\d+)_card", u)
        if m:
            revs.append(int(m.group(1)))
    rev = (max(revs) + 1) if revs else 0
    return "%s%d_card.jpg" % (stem, rev)


# ----------------------------------------------------------------------------------------------
# Edits
# ----------------------------------------------------------------------------------------------

def edit_mapping(text, comment, new_id, base_id):
    eol = eol_of(text)
    if not text.endswith(eol):
        text += eol
    return text + "#" + comment + eol + new_id + "," + base_id + eol


def edit_pc_cards(text, comment, new_id, url):
    eol = eol_of(text)
    lines = text.split(eol)
    # The object literal ends with the last line that is just "}" (possibly followed by ";" or blank lines)
    close_idx = None
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip() in ("}", "};"):
            close_idx = i
            break
    if close_idx is None:
        sys.exit("Could not find the closing brace of the PCCards object in PC_Cards.js")
    insert = ["\t//" + comment, "\t'" + new_id + "': '" + url + "',"]
    # Drop whitespace-only lines before the closing brace and put back exactly one blank line
    while close_idx > 0 and lines[close_idx - 1].strip() == "":
        del lines[close_idx - 1]
        close_idx -= 1
    lines[close_idx:close_idx] = insert + [""]
    return eol.join(lines)


def edit_packs(text, pack_names, comment, new_id):
    eol = eol_of(text)
    lines = text.split(eol)
    for pack in pack_names:
        name_idx = None
        for i, line in enumerate(lines):
            if line.strip() == "name: " + pack:
                name_idx = i
                break
        if name_idx is None:
            sys.exit("Pack not found in PC-Packs.hjson: " + pack)
        # find "items: [" then the matching "]" at the same indentation
        items_idx = None
        for i in range(name_idx, min(name_idx + 10, len(lines))):
            if lines[i].strip().startswith("items:"):
                items_idx = i
                break
        if items_idx is None:
            sys.exit("Pack '%s' has no items: [ line" % pack)
        indent = lines[items_idx][:len(lines[items_idx]) - len(lines[items_idx].lstrip())]
        close_idx = None
        for i in range(items_idx + 1, len(lines)):
            if lines[i].strip() == "]" and lines[i].startswith(indent) and len(lines[i]) - len(lines[i].lstrip()) == len(indent):
                close_idx = i
                break
        if close_idx is None:
            sys.exit("Could not find the end of the items list for pack " + pack)
        item_indent = indent + "\t"
        lines[close_idx:close_idx] = [item_indent + "#" + comment, item_indent + "1x" + new_id]
    return eol.join(lines)


def is_single_paren_group(expr):
    """True if expr is entirely wrapped in one pair of parentheses, e.g. "(a || b)" but not "(a) || (b)"."""
    if not (expr.startswith("(") and expr.endswith(")")):
        return False
    depth = 0
    for i, ch in enumerate(expr):
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0 and i != len(expr) - 1:
                return False
    return depth == 0


def edit_card_js_horizontal(text, set_no, card_no):
    """Add `|| cardNo == N` to the isBlueprintHorizontal branch for this set (or add a branch)."""
    eol = eol_of(text)
    lines = text.split(eol)
    fn_start = None
    for i, line in enumerate(lines):
        if "static isBlueprintHorizontal(" in line:
            fn_start = i
            break
    if fn_start is None:
        sys.exit("Card.isBlueprintHorizontal not found in Card.js")

    branch_re = re.compile(r"^\s*if\s*\((.*)\)\s*$")
    fallback_idx = None
    for i in range(fn_start, len(lines)):
        line = lines[i]
        if line.strip() == "return false;":
            fallback_idx = i
            break
        m = branch_re.match(line)
        if not m:
            continue
        set_numbers = [int(x) for x in re.findall(r"setNo\s*==\s*(\d+)", m.group(1))]
        if set_no in set_numbers:
            ret = lines[i + 1]
            rm = re.match(r"^(\s*)return\s+(.*);\s*$", ret)
            if not rm:
                sys.exit("Unexpected line after the setNo == %d branch in isBlueprintHorizontal: %r" % (set_no, ret))
            if re.search(r"cardNo\s*==\s*%d\b" % card_no, rm.group(2)):
                return text, "already marked horizontal"
            expr = rm.group(2).strip()
            if not is_single_paren_group(expr):
                expr = "(" + expr + ")"
            lines[i + 1] = "%sreturn %s || cardNo == %d;" % (rm.group(1), expr, card_no)
            return eol.join(lines), "extended the setNo == %d branch" % set_no
    if fallback_idx is None:
        sys.exit("Could not find the `return false;` fallback in isBlueprintHorizontal")
    indent = lines[fallback_idx][:len(lines[fallback_idx]) - len(lines[fallback_idx].lstrip())]
    lines[fallback_idx:fallback_idx] = [
        indent + "if (setNo == %d)" % set_no,
        indent + "    return (cardNo == %d);" % card_no,
        "",
    ]
    return eol.join(lines), "added a new setNo == %d branch" % set_no


# ----------------------------------------------------------------------------------------------
# Image handling
# ----------------------------------------------------------------------------------------------

def prepare_image(src, dest_name, rotate, is_site, dry_run):
    """Copies the image to a sibling file with the canonical name, rotating if asked.  Returns the path."""
    dest = os.path.join(os.path.dirname(os.path.abspath(src)), dest_name)
    try:
        from PIL import Image  # type: ignore
    except ImportError:
        Image = None

    if Image is not None:
        with Image.open(src) as im:
            w, h = im.size
            portrait = h > w
            if is_site and portrait and not rotate:
                print("  WARNING: %s is portrait (%dx%d) but the card is a site; horizontal card images are stored "
                      "landscape with upright text.  Re-run with --rotate cw or --rotate ccw." % (src, w, h))
            if not is_site and not portrait:
                print("  WARNING: %s is landscape (%dx%d) but the card is not a site." % (src, w, h))
            if rotate:
                angle = -90 if rotate == "cw" else 90
                print("  Rotating %s %s" % (src, "clockwise" if rotate == "cw" else "counter-clockwise"))
                if not dry_run:
                    im.rotate(angle, expand=True).save(dest, quality=95)
                return dest
    else:
        print("  (Pillow not installed - skipping orientation check%s)" % ("; --rotate ignored" if rotate else ""))

    if os.path.abspath(src) != dest:
        print("  Copying %s -> %s" % (src, dest))
        if not dry_run:
            shutil.copyfile(src, dest)
    return dest


def upload_image(path, target, dry_run):
    cmd = ["scp", path, target if target.endswith("/") else target + "/"]
    print("  Uploading: " + " ".join(cmd))
    if dry_run:
        return
    result = subprocess.run(cmd)
    if result.returncode != 0:
        sys.exit("scp failed with exit code %d" % result.returncode)


# ----------------------------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("base", nargs="?", help="blueprint ID of the card being promo'd, e.g. 4_267")
    ap.add_argument("--find", metavar="TEXT", help="search card titles/subtitles and print matching blueprint IDs")
    ap.add_argument("--comment", help="label used in every file, e.g. \"Eomer, TMOR FA - 2026-06 WC Circuit\"")
    ap.add_argument("--image", help="local image file; renamed to the canonical name next to the original")
    ap.add_argument("--image-url", help="use this exact URL instead of deriving one from the canonical name")
    ap.add_argument("--variant", default="U", help="letter in the image name: U (default), H (holiday), O, ...")
    ap.add_argument("--rotate", choices=["cw", "ccw"], help="rotate the image 90 degrees before use (sites)")
    ap.add_argument("--upload", metavar="USER@HOST:/path/", help="scp the image to this folder after renaming")
    ap.add_argument("--packs", nargs="*", default=None,
                    help="packs to add the promo to (default: %s); pass --packs with no names to skip" % ", ".join(DEFAULT_PACKS))
    ap.add_argument("--check", action="store_true", help="only report the next free ID and collisions")
    ap.add_argument("--dry-run", action="store_true", help="show what would change without writing anything")
    ap.add_argument("--root", help="repo root (the folder containing gemp-lotr-cards); auto-detected by default")
    args = ap.parse_args()

    root = args.root or find_repo_root(os.path.dirname(os.path.abspath(__file__)))
    paths = Paths(root)
    os.chdir(root)

    cards = scan_card_definitions(paths.cards_dir)

    if args.find:
        key = normalise(args.find)
        hits = [(bp, c) for bp, c in cards.items()
                if c["title"] and (key in normalise(c["title"]) or (c["subtitle"] and key in normalise(c["subtitle"])))]
        hits.sort(key=lambda t: (int(t[0].split("_")[0]), int(t[0].split("_")[1])))
        for bp, c in hits:
            name = c["title"] + (", " + c["subtitle"] if c["subtitle"] else "")
            print("%-9s %-45s %-12s %s%s" % (bp, name, c["type"] or "?", os.path.relpath(c["file"]),
                                              "  (commented out)" if c["commented"] else ""))
        if not hits:
            print("No cards matched %r" % args.find)
        return

    if not args.base:
        ap.error("a base blueprint ID is required (or use --find)")
    if not args.check and not args.comment:
        ap.error("--comment is required (it is written into every file)")

    base = strip_id_suffix(args.base)
    m = ID_RE.match(base)
    if not m:
        sys.exit("Blueprint IDs look like 4_267; got %r" % args.base)
    set_no, card_no = int(m.group(1)), int(m.group(2))

    aliases, mapping_ids = scan_mapping(paths.mapping)
    pc_card_urls = scan_pc_cards(paths.pc_cards_js)
    pack_ids = scan_packs(paths.packs)

    if base in aliases and base not in cards:
        print("NOTE: %s is itself an alias for %s; promos must point at the real card, using %s." % (base, aliases[base], aliases[base]))
        base = aliases[base]
        set_no, card_no = [int(x) for x in base.split("_")]

    if base not in cards:
        sys.exit("No card definition found for %s under %s" % (base, os.path.relpath(paths.cards_dir)))
    card = cards[base]
    if card["commented"]:
        sys.exit("The only definition of %s is commented out (%s:%d)" % (base, card["file"], card["line"]))

    is_site = (card["type"] or "").strip().lower() == "site"
    full_name = card["title"] + (", " + card["subtitle"] if card["subtitle"] else "")

    new_no, highest = next_free_id(set_no, cards, mapping_ids, set(pc_card_urls), pack_ids)
    new_id = "%d_%d" % (set_no, new_no)
    existing_promos = sorted((a for a, b in aliases.items() if b == base), key=lambda s: int(s.split("_")[1]))

    print("Base card:       %s  %s  [%s]  (%s:%d)" % (base, full_name, card["type"], os.path.relpath(card["file"]), card["line"]))
    if existing_promos:
        print("Existing promos: " + ", ".join(existing_promos))
    print("Highest ID in set %d: %d  ->  new promo ID: %s" % (set_no, highest, new_id))
    clash = collisions_for(new_id, cards, mapping_ids, set(pc_card_urls), pack_ids)
    if clash:
        sys.exit("COLLISION: %s already appears in: %s" % (new_id, "; ".join(clash)))
    if args.check:
        return

    if args.image_url:
        image_name = os.path.basename(args.image_url)
        url = args.image_url
    else:
        image_name = default_image_name(set_no, card_no, args.variant, pc_card_urls)
        url = IMAGE_HOST + image_name
    print("Image:           %s" % url)

    packs = DEFAULT_PACKS if args.packs is None else args.packs
    comment = args.comment

    # --- compute all edits first so a failure leaves nothing half-written ---
    mapping_text = edit_mapping(read_text(paths.mapping), comment, new_id, base)
    pc_cards_text = edit_pc_cards(read_text(paths.pc_cards_js), comment, new_id, url)
    packs_text = edit_packs(read_text(paths.packs), packs, comment, new_id) if packs else None
    card_js_text, card_js_note = (edit_card_js_horizontal(read_text(paths.card_js), set_no, new_no) if is_site else (None, None))

    print("")
    print("Changes%s:" % (" (dry run)" if args.dry_run else ""))
    print("  %s: +%s,%s" % (os.path.relpath(paths.mapping), new_id, base))
    print("  %s: +'%s'" % (os.path.relpath(paths.pc_cards_js), new_id))
    if packs:
        print("  %s: +1x%s in %s" % (os.path.relpath(paths.packs), new_id, ", ".join(packs)))
    else:
        print("  %s: (no packs requested)" % os.path.relpath(paths.packs))
    if is_site:
        print("  %s: %s (site -> horizontal)" % (os.path.relpath(paths.card_js), card_js_note))

    if not args.dry_run:
        write_text(paths.mapping, mapping_text)
        write_text(paths.pc_cards_js, pc_cards_text)
        if packs_text is not None:
            write_text(paths.packs, packs_text)
        if card_js_text is not None and card_js_note != "already marked horizontal":
            write_text(paths.card_js, card_js_text)

    print("")
    if args.image:
        if not os.path.isfile(args.image):
            sys.exit("Image file not found: " + args.image)
        print("Image file:")
        prepared = prepare_image(args.image, image_name, args.rotate, is_site, args.dry_run)
        if args.upload:
            upload_image(prepared, args.upload, args.dry_run)
        else:
            print("  Upload %s so that it is served at %s" % (prepared, url))
    else:
        print("No --image given: make sure a file is served at %s" % url)
        if is_site:
            print("  (site card: the image must be landscape with upright text)")

    print("")
    print("Done. Review the diff, then commit.  Remember the server needs a card reload/restart to pick up the new alias.")


if __name__ == "__main__":
    main()
