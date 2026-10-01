#!/usr/bin/env python3
"""
migrate_changelog_to_patchnotes.py - one-shot conversion of the old Server Info > GEMP Change Log page
(gemp-lotr-async/src/main/web/includes/info/changeLog.html, a single <pre> of hand-written entries) into the
patch notes folder, one Markdown file per dated entry (see gemp-lotr-async/src/main/web/patchnotes/README.md).

The old page was preformatted text with a little inline HTML (coloured <span>s, links, one <ul> and one <img>).
Every word and every piece of that HTML is kept; only the layout is translated to Markdown:

  - "- text" lines at the left margin become list items;
  - a line that merely continues a hard-wrapped line (the 2011-2013 entries wrap near column 120) is joined to it;
  - any other line break is kept as a Markdown hard break, and an indented line (the "Game Text:" blocks of
    errata notes) keeps its indentation as em spaces;
  - Markdown punctuation that would otherwise be read as formatting (*, _, `, a leading #, >, + or "1.") is
    backslash-escaped so the text shows exactly as before;
  - the <ul>...</ul> block is kept verbatim as an HTML block.

Entry headers are the <b>date</b> lines.  Several entries on one day ("2023 May 13 C/B/A", "2021 December 20 - B")
become YYYY-MM-DD-<letter>.md, a header with a name after the date ("2022 November 04 - Hobbit Fixes by Phallen")
becomes YYYY-MM-DD-<name>.md with that name as its title, and same-day entries get an `order:` so they keep the old
page's top-to-bottom order.

Usage:
  python scripts/migrate_changelog_to_patchnotes.py [changeLog.html] [output folder]
  (git show <commit>:gemp-lotr/gemp-lotr-async/src/main/web/includes/info/changeLog.html > /tmp/changeLog.html
   recovers the source once the page has been removed.)

Only the Python standard library is required.
"""

import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
WEB = os.path.join(HERE, "..", "gemp-lotr-async", "src", "main", "web")
DEFAULT_SOURCE = os.path.join(WEB, "includes", "info", "changeLog.html")
DEFAULT_OUT = os.path.join(WEB, "patchnotes")

MONTHS = {
    "jan": 1, "feb": 2, "mar": 3, "apr": 4, "may": 5, "jun": 6, "jul": 7, "aug": 8, "sep": 9, "sept": 9,
    "oct": 10, "nov": 11, "dec": 12,
}
MONTH_WORD = r"(January|February|March|April|May|June|July|August|September|October|November|December|" \
             r"Jan\.|Feb\.|Mar\.|Apr\.|June|Jun\.|July|Jul\.|Aug\.|Sept\.|Sep\.|Oct\.|Nov\.|Dec\.)"
HEADER = re.compile(
    r"^<b>\s*(?:(?P<y1>\d{4}) " + MONTH_WORD.replace("(", "(?P<m1>", 1) + r" (?P<d1>\d{1,2})"
    r"|(?P<d2>\d{1,2}) " + MONTH_WORD.replace("(", "(?P<m2>", 1) + r" (?P<y2>\d{4}))"
    r"\s*(?:-\s*)?(?P<rest>[^<]*?)\s*</b>\s*$")

WRAP_WIDTH = 95          # a line at least this long followed by a plain line was hard-wrapped
INDENT_UNIT = 3          # the old page indented nested lines by 3 spaces
EMSP = "&emsp;&emsp;"    # one indentation level


def month_number(word):
    return MONTHS[word.lower().rstrip(".")[:3]]


def escape_inline(text):
    """Escapes Markdown emphasis/code characters outside HTML tags, so the old text renders literally."""
    out = []
    for part in re.split(r"(<[^>]+>)", text):
        if part.startswith("<") and part.endswith(">"):
            out.append(part)
        else:
            part = re.sub(r"([\\*`])", r"\\\1", part)
            # an underscore inside a word (card ids such as V1_52) is never emphasis; escape only the others
            part = re.sub(r"(?<![A-Za-z0-9])_|_(?![A-Za-z0-9])", r"\\_", part)
            out.append(part)
    return "".join(out)


def escape_line_start(text):
    """Escapes what would start a block (heading, quote, list, thematic break) at the start of a paragraph line."""
    if re.match(r"^(#{1,6}(\s|$)|>|[+*](\s|$)|-(\s|$)|=+\s*$|-{3,}\s*$)", text):
        return "\\" + text
    m = re.match(r"^(\d{1,9})([.)])(\s|$)", text)
    if m:
        return m.group(1) + "\\" + text[len(m.group(1)):]
    return text


def indent_of(line):
    expanded = line.expandtabs(INDENT_UNIT)
    return len(expanded) - len(expanded.lstrip(" "))


def convert_body(lines):
    """Old <pre> lines of one entry -> Markdown."""
    out = []                 # emitted Markdown lines
    in_list = False          # the current block is (inside) a list item
    in_html = False          # inside the verbatim <ul> block
    prev_raw = None          # previous non-blank raw line of the current block (None after a blank line)
    pending_blank = False

    def blank():
        if out and out[-1] != "":
            out.append("")

    for raw in lines:
        line = raw.rstrip()
        stripped = line.strip()

        if in_html:
            out.append(line)
            if stripped.lower().startswith("</ul"):
                in_html = False
                out.append("")
            continue
        if stripped.lower().startswith("<ul"):
            blank()
            out.append(line)
            in_html = not stripped.lower().endswith("</ul>")
            in_list = False
            prev_raw = None
            continue

        if stripped == "":
            prev_raw = None
            pending_blank = True
            continue

        if pending_blank:
            blank()
            pending_blank = False

        indent = indent_of(line)
        if indent == 0 and (stripped.startswith("- ") or stripped == "-"):
            # a new list item
            if out and out[-1] != "" and not in_list:
                out.append("")          # a list after a paragraph line needs a blank line to start
            out.append("- " + escape_line_start(escape_inline(stripped[2:].strip())))
            in_list = True
            prev_raw = line
            continue

        if indent == 0 and stripped.startswith("<img"):
            blank()
            out.append(stripped)
            out.append("")
            in_list = False
            prev_raw = None
            continue

        level = (indent + INDENT_UNIT - 1) // INDENT_UNIT
        body = escape_inline(stripped)
        if level > 0:
            body = EMSP * level + body
        else:
            body = escape_line_start(body)

        if prev_raw is None:
            # first line of a block after a blank line
            if in_list and level > 0:
                out.append("  " + body)       # another paragraph of the same list item
            else:
                in_list = False
                out.append(body)
        else:
            joined = level == 0 and len(prev_raw.rstrip()) >= WRAP_WIDTH
            if joined:
                out.append(("  " if in_list else "") + body)                     # soft break: same line
            else:
                out[-1] = out[-1] + "\\"                                          # hard break
                out.append(("  " if in_list else "") + body)
        prev_raw = line

    while out and out[-1] == "":
        out.pop()
    return "\n".join(out) + "\n"


def slugify(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def yaml_quote(text):
    return '"' + text.replace("\\", "\\\\").replace('"', '\\"') + '"'


def parse_entries(source):
    m = re.search(r"<pre[^>]*>(.*)</pre>", source, re.S)
    if not m:
        sys.exit("no <pre> block found")
    entries = []
    current = None
    for line in m.group(1).split("\n"):
        h = HEADER.match(line.strip())
        if h:
            if h.group("y1"):
                y, mo, d = int(h.group("y1")), month_number(h.group("m1")), int(h.group("d1"))
            else:
                y, mo, d = int(h.group("y2")), month_number(h.group("m2")), int(h.group("d2"))
            current = {"date": "%04d-%02d-%02d" % (y, mo, d), "rest": h.group("rest").strip(),
                       "header": line.strip(), "lines": []}
            entries.append(current)
        elif current is not None:
            current["lines"].append(line)
        elif line.strip():
            sys.exit("text before the first dated header: " + line)
    return entries


def main():
    source_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_SOURCE
    out_dir = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUT
    with open(source_path, encoding="utf-8") as f:
        entries = parse_entries(f.read())

    per_date = {}
    for e in entries:
        per_date.setdefault(e["date"], []).append(e)

    os.makedirs(out_dir, exist_ok=True)
    written = []
    for date, group in per_date.items():
        for position, e in enumerate(group):
            rest = e["rest"]
            title = None
            if rest == "":
                suffix = "" if len(group) == 1 else "-" + chr(ord("a") + len(group) - 1 - position)
            elif len(rest) == 1:
                suffix = "-" + rest.lower()
            else:
                suffix = "-" + slugify(rest)
                title = rest
            name = date + suffix
            if name in written:
                sys.exit("duplicate file name " + name)
            written.append(name)
            front = ["---", "date: " + date]
            if title:
                front.append("title: " + yaml_quote(title))
            if len(group) > 1:
                # the old page listed same-day entries newest first
                front.append("order: %d" % (len(group) - position))
            front.append("---")
            body = convert_body(e["lines"])
            with open(os.path.join(out_dir, name + ".md"), "w", encoding="utf-8", newline="\n") as f:
                f.write("\n".join(front) + "\n\n" + body)
    print("wrote %d entries to %s" % (len(written), out_dir))


if __name__ == "__main__":
    main()
