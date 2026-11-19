#!/usr/bin/env python3
"""Writes the test rig's API reference from the rig's own source, so that the reference cannot drift from the code.

    gen_vts_api.py                  rewrites .claude/skills/gemp-testing/references/vts-api.md
    gen_vts_api.py --check          exits 1 if the file on disk is not what would be written (it has drifted)
    gen_vts_api.py --stdout         prints instead of writing

The reference is every method a test can call on a VirtualTableScenario, grouped by the source file it is declared
in, with the first sentence of its Javadoc.  A method with no Javadoc is listed without a description, which is a
prompt to write one in the source, not here.  Two things are carried over from the source besides: a Freeps... or
Shadow... method with no Javadoc of its own, whose whole body hands on to a method that takes the player
(`return HasBPChoiceAvailable(P1(), blueprintId, true);`), takes that method's description, with the values it
fixes ("Here `selectable` is true."); and where several
methods of one name would print one and the same description, each has what its Javadoc says of its own
parameters after it (the four StartGame methods).
"""
import re
import sys

import gempcards as g

OUT = g.SKILLS / "gemp-testing" / "references" / "vts-api.md"

# In the order a test tends to need them.
SOURCES = [
    ("Setting up", g.FRAMEWORK_DIR / "VirtualTableScenario.java", "test"),
    ("The table itself", g.VT_DIR / "VirtualTable.java", "main"),
    ("Constants", g.FRAMEWORK_DIR / "TestConstants.java", "test"),
    ("Putting cards where they need to be (cheating)", g.VT_DIR / "ZoneManipulation.java", "main"),
    ("Moving the game along", g.VT_DIR / "GameProcedures.java", "main"),
    ("Playing and using cards", g.VT_DIR / "Actions.java", "main"),
    ("Decisions", g.VT_DIR / "Decisions.java", "main"),
    ("Choices within a decision", g.VT_DIR / "Choices.java", "main"),
    ("Assignments and skirmishes", g.VT_DIR / "Skirmishes.java", "main"),
    ("Reading the game", g.VT_DIR / "GameProperties.java", "main"),
    ("Reading a card", g.VT_DIR / "CardProperties.java", "main"),
    ("Reading the piles", g.VT_DIR / "PileProperties.java", "main"),
    ("Assertions", g.FRAMEWORK_DIR / "Assertions.java", "test"),
    ("Ad hoc effects (test sources only)", g.FRAMEWORK_DIR / "AdHocEffects.java", "test"),
    ("Underpinnings", g.VT_DIR / "TableBase.java", "main"),
]

_METHOD = re.compile(
    r"^[ \t]*((?:(?:public|protected|private|static|default|final|abstract|synchronized)\s+)*)"   # modifiers
    r"(?:<[^>]+>\s+)?"                                                                         # generics
    r"([A-Za-z_][\w<>\[\], .?]*?)\s+"                                                          # return type
    r"([A-Za-z_]\w*)\s*"                                                                       # name
    r"\(([^)]*)\)"                                                                             # parameters
    r"\s*(?:throws\s+[\w., ]+)?\s*[{;]", re.M)
_CTOR = re.compile(r"^[ \t]*public\s+(VirtualTableScenario)\s*\(([^)]*)\)", re.M)
_CONST = re.compile(r"^[ \t]*(?:public\s+static\s+final\s+|static\s+final\s+)?"
                    r"(String|int|double|HashMap<String,\s*String>)\s+([A-Za-z_]\w*)\s*=\s*([^;{]*)", re.M)
_JAVADOC = re.compile(r"/\*\*(.*?)\*/", re.S)
_KEYWORDS = {"return", "new", "throw", "else", "if", "for", "while", "var", "try", "catch", "switch"}


def first_sentence(doc: str) -> str:
    lines = []
    for line in doc.split("\n"):
        line = re.sub(r"^\s*\*\s?", "", line).rstrip()
        if line.strip().startswith("@"):
            break
        lines.append(line.strip())
    text = " ".join(l for l in lines if l).strip()
    text = re.sub(r"\{@link\s+#?([^}]+)\}", r"`\1`", text)
    text = re.sub(r"\{@code\s+([^}]+)\}", r"`\1`", text)
    # The first full stop that ends a sentence, not the ones inside "e.g." and "i.e.".
    guarded = re.sub(r"\b(e\.g|i\.e|etc|vs)\.", lambda m: m.group(0).replace(".", "\x00"), text)
    m = re.match(r"^(.*?[.!?])(\s|$)", guarded)
    return (m.group(1) if m else guarded).replace("\x00", ".").strip()


def whole_doc(doc: str) -> str:
    lines = []
    for line in doc.split("\n"):
        line = re.sub(r"^\s*\*\s?", "", line).rstrip()
        if line.strip().startswith("@"):
            break
        lines.append(line.strip())
    return " ".join(l for l in lines if l).strip()


def param_docs(doc: str) -> dict:
    """{parameter: the first sentence of what the Javadoc's @param line says of it}."""
    out = {}
    for name, said in re.findall(r"@param\s+(\w+)\s+(.*?)(?=\n\s*\*?\s*@|\Z)", doc or "", re.S):
        said = first_sentence("\n".join(re.sub(r"^\s*\*\s?", "", line) for line in said.split("\n")))
        if said:
            out[name] = said
    return out


def parameters(params: str) -> list:
    """[(type, name)] of a parameter list as written."""
    out = []
    for one in re.split(r",(?![^<]*>)", params):
        m = re.match(r"^\s*(?:final\s+)?(.+?)\s*(\.\.\.)?\s*(\w+)\s*$", one.strip())
        if m:
            out.append((re.sub(r"\s+", "", m.group(1)) + (m.group(2) or ""), m.group(3)))
    return out


_HANDS_ON = re.compile(r"^\s*(?:return\s+)?(\w+)\s*\(\s*P[12]\(\)\s*((?:,[^;{}]*)?)\)\s*;\s*\}")


def hands_on(body: str):
    """(the method called, its arguments after the player) where a method's whole body is one call that gives the
    player first, as the Freeps... and Shadow... methods do; else None."""
    m = _HANDS_ON.match(body)
    if not m:
        return None
    args, depth, cur = [], 0, ""
    for c in m.group(2).lstrip().lstrip(","):
        depth += {"(": 1, ")": -1}.get(c, 0)
        if c == "," and depth == 0:
            args.append(cur.strip())
            cur = ""
        else:
            cur += c
    return m.group(1), [a for a in args + [cur.strip()] if a]


def fill_in(found: list) -> list:
    """(signature, description) for the methods of one file, from (signature, description, name, parameters, what
    the Javadoc says of each parameter, what the body hands on to): see the two things carried over, at the top
    of this file."""
    filled = []
    for sig, doc, name, params, said, handed in found:
        if not doc and handed and re.match(r"^(?:Freeps|Shadow)\w", name):
            called, args = handed
            for _, other_doc, other_name, other_params, other_said, _ in found:
                if other_name == called and other_doc and len(other_params) == len(args) + 1 and other_params[0][0] == "String":
                    doc = re.sub(r"\b(?:the|a) given player\b", "that player", other_doc)
                    # What that method says of the parameters this one passes straight on, under this one's names.
                    mine = {n for _, n in params}
                    said = {a: other_said[o] for a, (_, o) in zip(args, other_params[1:]) if a in mine and o in other_said}
                    # ...and what it fixes for that method, where it passes a value of its own.
                    fixed = ["`%s` is %s" % (o, a) for a, (_, o) in zip(args, other_params[1:]) if re.match(r'^(?:"[^"]*"|true|false|null|-?\d+)$', a)]
                    if fixed:
                        doc += "  Here " + " and ".join(fixed) + "."
                    break
        filled.append((sig, doc, name, params, said))
    out = []
    for sig, doc, name, params, said in filled:
        same = [e for e in filled if e[2] == name and e[1] == doc]
        if doc and len(same) > 1:
            own = ["`%s`: %s" % (n, said[n]) for _, n in params if n in said]
            if own:
                doc += "  " + "  ".join(own)
        out.append((sig, doc))
    return out


def doc_before(text: str, pos: int):
    """The Javadoc that ends right before a declaration, if there is one."""
    head = text[:pos]
    end = head.rfind("*/")
    if end < 0:
        return None
    between = head[end + 2:]
    if re.sub(r"@\w+(\([^)]*\))?", "", between).strip():
        return None
    start = head.rfind("/**", 0, end)
    if start < 0 or "*/" in head[start:end]:
        return None
    return head[start + 3:end]


def squeeze(params: str) -> str:
    return re.sub(r"\s+", " ", params).strip()


def members(path):
    text = path.read_text(encoding="utf-8").replace("\r", "")
    out = []
    type_doc = None
    m = re.search(r"^public\s+(?:abstract\s+)?(?:class|interface)\s+\w+", text, re.M)
    if m:
        d = doc_before(text, m.start())
        if d:
            type_doc = whole_doc(d)
    if path.name == "TestConstants.java":
        for m in _CONST.finditer(text):
            d = doc_before(text, m.start())
            value = squeeze(m.group(3))
            if m.group(1).startswith("HashMap"):
                value = "sites 1 to 9"
            out.append(("%s %s = %s" % (m.group(1), m.group(2), value), first_sentence(d) if d else ""))
        return type_doc, out
    for m in _CTOR.finditer(text):
        d = doc_before(text, m.start())
        out.append(("new VirtualTableScenario(%s)" % squeeze(m.group(2)), first_sentence(d) if d else ""))
    found = []
    for m in _METHOD.finditer(text):
        mods, ret, name, params = m.group(1), m.group(2).strip(), m.group(3), m.group(4)
        if ret in _KEYWORDS or name in _KEYWORDS or "private" in mods:
            continue
        if not mods.strip() and not path.name.endswith("TableBase.java"):
            continue
        if path.name in ("VirtualTable.java", "VirtualTableScenario.java") and "public" not in mods:
            continue
        # Interface callbacks implemented inside an anonymous class are not part of the rig's surface.
        line_start = text.rfind("\n", 0, m.start()) + 1
        indent = len(text[line_start:m.start(3)]) - len(text[line_start:m.start(3)].lstrip("\t "))
        raw_indent = text[line_start:line_start + 8]
        if raw_indent.startswith("\t\t\t") or raw_indent.startswith(" " * 12):
            continue
        d = doc_before(text, m.start())
        prefix = "static " if "static" in mods else ""
        found.append(("%s%s %s(%s)" % (prefix, ret, name, squeeze(params)), first_sentence(d) if d else "", name,
                      parameters(squeeze(params)), param_docs(d), hands_on(text[m.end():m.end() + 400])))
    return type_doc, out + fill_in(found)


def pair_up(entries):
    """Freeps and Shadow twins that differ only in the name are written once."""
    by_sig = {sig: doc for sig, doc in entries}
    used, out = set(), []
    for sig, doc in entries:
        if sig in used:
            continue
        m = re.match(r"^(.*?\s)Freeps(\w*)(\(.*\))$", sig)
        twin = None
        if m:
            twin = "%sShadow%s%s" % (m.group(1), m.group(2), m.group(3))
            if twin in by_sig and twin not in used:
                used.add(twin)
                shown = "%sFreeps%s%s  ·  Shadow%s" % (m.group(1), m.group(2), m.group(3), m.group(2))
                text = doc or by_sig[twin]
                text = text.replace("the Free Peoples player", "that player").replace("Free Peoples player", "that player")
                out.append((shown, text))
                used.add(sig)
                continue
        used.add(sig)
        out.append((sig, doc))
    return out


def render() -> str:
    lines = [
        "# The test rig's API",
        "",
        "<!-- GENERATED by .claude/skills/card-test-campaign/scripts/gen_vts_api.py from the rig's source.",
        "     Do not edit by hand: change the Javadoc in the source and run the script again. -->",
        "",
        "Every method a test can call on a `VirtualTableScenario` (`scn`), grouped by the source file that declares",
        "it.  Files under `virtualtable/` are in the server's main sources and are shared with the scenario and",
        "tutorial system; files under `framework/` are in the test sources.  A `Freeps… · Shadow…` entry is two",
        "methods that do the same thing for each player.  An entry with no description has no Javadoc in the source.",
        "",
    ]
    total, undocumented = 0, 0
    for heading, path, where in SOURCES:
        type_doc, entries = members(path)
        entries = pair_up(entries)
        rel = g.rel(path)
        lines.append("## %s" % heading)
        lines.append("")
        lines.append("`%s` (%s sources)" % (rel.split("com/gempukku/lotro/")[1], where))
        if type_doc:
            lines.append("")
            lines.append(type_doc)
        lines.append("")
        for sig, doc in entries:
            total += 1
            if doc:
                lines.append("- `%s` — %s" % (sig, doc))
            else:
                undocumented += 1
                lines.append("- `%s`" % sig)
        lines.append("")
    lines.append("---")
    lines.append("%d entries, %d of them with no description in the source." % (total, undocumented))
    lines.append("")
    return "\n".join(lines)


def main(argv):
    text = render()
    if "--stdout" in argv:
        sys.stdout.write(text)
        return 0
    if "--check" in argv:
        current = OUT.read_text(encoding="utf-8") if OUT.is_file() else ""
        if current.replace("\r", "") != text:
            print("vts-api.md has drifted from the source.  Run gen_vts_api.py to rewrite it.")
            return 1
        print("vts-api.md matches the source.")
        return 0
    OUT.write_text(text, encoding="utf-8")
    print("wrote %s" % g.rel(OUT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
