#!/usr/bin/env python3
"""Checks card test files against the Card Test Standard (see the test-generation skill).

    lint.py <what...>               what is a blueprint id, a test file, or a selector (set:1, seg:vset, ...)
    lint.py <what...> --quiet       one line per file
    lint.py --summary <what...>     counts only, by rule
    lint.py --form <file.java> [--only <Test,Test>]
                                    only the rules about how a test is written (bookend, reach, exception, adhoc,
                                    assert, ignore, style, helper, field, scenario, in-file, alias, same-blueprint,
                                    conditional, not-asked, not-in-hand), for a mechanics class or a card's file
                                    that has no ledger yet; with --only, only what lies inside the tests named (a
                                    lone test added to an old file)

Exit status is 0 when every file given is free of errors.  Warnings do not fail a file.

A line can be let off one rule by ending it with a comment that says why:   // lint-ok: <reason>
Each of those is counted and shown to the reviewer, so it is a request, not a way round.
"""
import re
import sys
from pathlib import Path

import gempcards as g

# The checks a clause of each type has to list at the very least.  The standard says which further checks a
# clause's wording calls for; those are the analyst's to list and the reviewer's to confirm.
CORE = {
    "Play": {"+"},
    "Keyword": {"K"},
    "ToPlay": {"+", "-"},
    "ExtraCost": {"+", "-", "C"},
    "PlayOn": {"F"},
    "Activated": {"+", "-"},
    "Event": {"+", "-"},
    "Effect": {"+"},
    "Trigger": {"+", "-"},
    "Response": {"+", "-"},
    "Modifier": {"+", "-"},
    "Restriction": {"+", "-"},
    "Choice": {"B"},
    "Ruling": {"+"},
    "Rule": {"+"},
    "Interaction": {"+"},
    "FormatRule": {"+", "-"},
}
# Card types that a player plays from hand and that then stay in play.  Events are played by their Event clause.
PLAYED = {"companion", "ally", "minion", "possession", "artifact", "condition", "follower"}
CODES = {"+", "-", "C", "C-", "F", "D", "L", "O", "S", "R", "B", "Z", "K"}

LEDGER_LINE = re.compile(r"^\s*([A-Z]\d+)\s+([A-Za-z]+)\s+\[([^\]]*)\]\s*(.*?)\s*$")
WAIVER_LINE = re.compile(r"^\s+no\s+(C-|[+\-CFDLOSRBZK])\s*:\s*\S")
# "waits + F: G-0031 <why>": checks the rig cannot yet show.  The card is parked on that rig request until it can.
WAITS_LINE = re.compile(r"^\s+waits\s+((?:C-|[+\-CFDLOSRBZK])(?:\s+(?:C-|[+\-CFDLOSRBZK]))*)\s*:\s*(G-\d{4})\b")
CLAIM_LINE = re.compile(r"^\s+((?:C-|[+\-CFDLOSRBZK])(?:\s+(?:C-|[+\-CFDLOSRBZK]))*)\s*->\s*([A-Za-z_]\w*)\s*$")
# The same with something after the test's name: read as the claim it is, and reported by its line.
CLAIM_AND_WORDS = re.compile(r"^\s+((?:C-|[+\-CFDLOSRBZK])(?:\s+(?:C-|[+\-CFDLOSRBZK]))*)\s*->\s*([A-Za-z_]\w*)\s+(\S.*?)\s*$")
TAG_LINE = re.compile(r"^\s*//\s*((?:[A-Z]\d+\s*:\s*[^;]+;?\s*)+)$")
METHOD = re.compile(r"^\s*(public|protected|private)\s+(?:static\s+)?([\w<>\[\], .]+?)\s+(\w+)\s*\(([^)]*)\)")
# The same with its parameters running on to the lines below (94_5: two league placements, one to a line).
METHOD_OPENS = re.compile(r"^\s*(public|protected|private)\s+(?:static\s+)?([\w<>\[\], .]+?)\s+(\w+)\s*\([^)]*$")
# A field of the class: a declaration at the class's own depth that is no method.
FIELD = re.compile(r"^\s*(?:(?:public|protected|private|static|final|volatile|transient)\s+)*[\w<>\[\], .?]+?\s+(\w+)\s*(?:=|;)")

FORBIDDEN = [
    (r"\bscn\s*\.\s*(game|gameState|userFeedback)\s*\(\s*\)", "reaches past the rig into the engine; ask for a rig method instead"),
    (r"\bgetModifiersQuerying\b", "reaches past the rig into the engine; ask for a rig method instead"),
    (r"\b(PlayerDecided|FreepsDecided|ShadowDecided)\s*\(", "answers a decision with a raw string; use the Choose/Accept/Decline/Pass methods"),
    (r"\b\w*GetADParam\w*\s*\(|\bGetAwaitingDecision\w*\s*\(|\b(Freeps|Shadow)GetAwaitingDecision\s*\(", "reads a decision's raw parameters; use the Choices getters"),
    (r"\bnew\s+VirtualTableScenario\s*\(", "builds a table inside a test; tables come from a Get...Scenario() method"),
    (r"\bThread\s*\.\s*sleep\b|\bnew\s+Random\b|\bMath\s*\.\s*random\b", "a test may not wait or roll dice"),
    (r"\bsetAccessible\b|java\.lang\.reflect", "reflection is a way round the rig; ask for a rig method instead"),
]
# Looked for in the line as written (the others are tried against the line with its strings emptied).
FORBIDDEN_AS_WRITTEN = [
    (r'\b(Freeps|Shadow)Choose\s*\(\s*""\s*\)', "answers a decision with an empty string; FreepsDeclineChoosing() and "
                                                "ShadowDeclineChoosing() choose nothing, and fail where nothing may be chosen"),
]
JUSTIFY = [
    (r"\b(ApplyAdHocAction|\w*ExecuteAdHocEffect\w*|ApplyAdHoc\w*AutoDiscard|carryOutEffectInPhaseActionByPlayer)\s*\(",
     "an ad hoc effect needs its reason on the same line (// lint-ok: ...)"),
]
ACTS = re.compile(r"\b(Freeps|Shadow)(PlayCard|UseCardAction|TransferCard|DiscardToHeal|Choose\w*|Accept\w*|Decline\w*|"
                  r"Pass\w*|Assign\w*|Resolve\w*)\s*\(|\b(Pass\w+Actions|BothPass\w*|PassResponses|PassCurrentPhaseActions)\s*\(")
# What counts as saying what the game is waiting for once a test has done its last thing.  The choice of the next
# skirmish has a reader of its own (G-0047).  Who won or lost (G-0042) says the game is over only where it is read
# as true: false is also what a game still going on answers, for either player.
BOOKEND = re.compile(r"\bAwaiting\w+PhaseActions\s*\(|\bGameIsFinished\s*\(|\b\w*DecisionAvailable\s*\(|"
                     r"\b\w*HasOptionalTriggerAvailable\s*\(|\b\w*AnyDecisionsAvailable\s*\(|"
                     r"\b\w*AwaitingSkirmishChoice\s*\(|"
                     r"\bassertTrue\s*\(\s*(?:\"[^\"]*\"\s*,\s*)?scn\s*\.\s*(?:Freeps|Shadow)Has(?:Won|Lost)\s*\(|"
                     r"\bassertEquals\s*\(\s*Phase\s*\.\s*\w+\s*,\s*scn\s*\.\s*GetCurrentPhase\s*\(")
# How a card that stays in play speaks of itself.  Where it is not unique, a second copy is what shows that the
# words mean this copy and not every copy (the Standard's F).
ITSELF = re.compile(r"\bthis (?:companion|minion|ally|follower|condition|possession|artifact|weapon|card)\b|\bhere\b", re.I)


# "... discard this condition to ...", with nothing beside it to pay: a cost no board can make unpayable while the
# card is in play to be used, so a `no C-` line under it tells the reviewer nothing.
SELF_DISCARD = re.compile(r"(?:^|(?P<lead>[:,.])\s+|\bmay\s+)discard this(?:\s+[a-z]+)?(?:\s+from play)?\s+to\b", re.I)


# How the R clause for a site left behind speaks: by the rule it stands on, or by where the fellowship then is.
SITE_LEFT = re.compile(r"unless the fellowship is at that site|behind the fellowship|left behind|CR 4\.2 \"active\"|"
                       r"Legacy Ruling 3\b|\bLR 3\b", re.I)


# The cheats that take a card out of its owner's draw deck (the ones that only reorder a deck have Deck in their names).
LEAVES_DECK = re.compile(r"\b(?:Move(?!\w*Deck)\w+|AttachCardsTo|StackCardsOn|Set(?:Freeps|Shadow)?Hand)\s*\(")


def taken_out_of_deck(lines, variable: str, alias: str) -> bool:
    """Whether a test moves a card out of the draw deck by a cheat: its variable, or its alias as a string, among
    the arguments of a call that puts cards in a hand, a discard pile, play or the like."""
    named = re.compile(r'\b%s\b|"%s"' % (re.escape(variable), re.escape(alias)))
    for line in lines:
        for call in LEAVES_DECK.finditer(line):
            arguments = line[call.end():].split(";", 1)[0]
            if "Zone.DECK" not in arguments and named.search(arguments):
                return True
    return False


def only_cost_is_discarding_itself(text: str) -> bool:
    plain = g.plain_text(text)
    m = SELF_DISCARD.search(plain)
    if not m:
        return False
    if m.group("lead") == ",":
        # "If bearer is Boromir, discard this to ...": before the comma stands a condition, not another cost.
        before = re.split(r"[:.]\s+", plain[:m.start()])[-1].strip()
        return bool(re.match(r"(?:if|while|when|each time|at the|during)\b", before, re.I))
    return True


# A race, the culture every printed card of that race has on the Free Peoples side (all of them, for Elf and
# Dwarf), and how game text writes the race.  For Elf, Dwarf, Wizard and Wraith no printed card has the one without
# the other, so no board can show which a definition goes by (Q-0213: B).
RACE_AND_CULTURE = {"elf": ("elven", r"\b(?:elf|elves)\b"), "dwarf": ("dwarven", r"\b(?:dwarf|dwarves)\b"),
                    "hobbit": ("shire", r"\bhobbits?\b"), "wizard": ("gandalf", r"\bwizards?\b"),
                    "wraith": ("gondor", r"\bwraiths?\b")}
# The lines of a definition that say which cards something reaches.
_FILTER_KEY = re.compile(r"(filter|select|target|^on$|bearer|against|where|killer|^from$|^to$)", re.I)
# What stands in a filter and is no race or culture word of the filter's own.
_NOT_THE_FILTER = re.compile(r"\b(?:name|title|memory|nameFromMemory|titleFromMemory|allyHome|signet|siteBlock|hasAttachedCount)"
                             r"\([^()]*\)", re.I)


def race_culture_slips(card, raw=None):
    """What to say of a definition that goes by a race where its text names the culture and never the race, or
    by the culture where its text names the race and never the culture (F-0019, F-0291): one message for each.
    Nothing where the two agree.  `raw` is the definition's text where it is not the card's own (an old revision)."""
    raw = card.raw if raw is None else raw
    text = re.search(r"^\s*gametext\s*:\s*(.*?)\s*$", raw, re.M)
    printed = g.plain_text(text.group(1) if text else "")
    by_race, by_culture = {}, {}
    for line in raw.split("\n"):
        m = re.match(r"\s*(\w+)\s*:\s*(.*)", line)
        if not m or not _FILTER_KEY.search(m.group(1)):
            continue
        where = "%s: %s" % (m.group(1), m.group(2).strip())
        value = _NOT_THE_FILTER.sub("", m.group(2))
        for culture in re.findall(r"\bculture\(\s*(\w+)\s*\)", value, re.I):
            by_culture.setdefault(culture.lower(), where)
        value = re.sub(r"\bculture\(\s*\w+\s*\)", "", value, flags=re.I)
        for race in re.findall(r"(?<![\w-])(%s)\b(?!-)" % "|".join(RACE_AND_CULTURE), value, re.I):
            by_race.setdefault(race.lower(), where)
    out = []

    def shown(where):
        return where if len(where) <= 70 else where[:67] + "..."

    for race, (culture, word) in sorted(RACE_AND_CULTURE.items()):
        names_culture = bool(re.search(r"\[%s\]" % culture, printed, re.I))
        names_race = bool(re.search(word, printed, re.I))
        if race in by_race and names_culture and not names_race:
            out.append("the text names [%s] and no %s, and the definition goes by the race (`%s`): the definition is at "
                       "fault, which is a finding to file (F-0019, F-0291) and not a board to lay (Q-0213: B)"
                       % (culture, race.capitalize(), shown(by_race[race])))
        if culture in by_culture and names_race and not names_culture:
            out.append("the text names %s and no [%s], and the definition goes by the culture (`%s`): the definition is "
                       "at fault, which is a finding to file (F-0019) and not a board to lay (Q-0213: B)"
                       % ("an Elf" if race == "elf" else "a " + race.capitalize(), culture, shown(by_culture[culture])))
    return out


def same_title(text: str) -> str:
    """A title as a filter compares it: no accents, no capitals."""
    import unicodedata
    return unicodedata.normalize("NFKD", g.plain_text(text or "")).encode("ascii", "ignore").decode().strip().lower()


_title_kinds = None


def title_kinds() -> dict:
    """For each title that cards of more than one culture or type carry, those cards by (culture, type).  The
    Hobbit sets are left out (their turn comes last, and nothing tests them yet), as are cards that are only a
    picture (set 90)."""
    global _title_kinds
    if _title_kinds is None:
        every = {}
        for cid, c in g.card_index().items():
            if g.segment_of(cid) in ("hobbit", "other") or g.split_id(cid)[0] == 90:
                continue
            kind = ((c.field("culture", "") or "").strip().lower(), (c.field("type", "") or "").strip().lower())
            every.setdefault(same_title(c.field("title", "")), {}).setdefault(kind, []).append(cid)
        _title_kinds = {t: kinds for t, kinds in every.items() if t and len(kinds) > 1}
    return _title_kinds


class Finding:
    def __init__(self, level, rule, line, message):
        self.level, self.rule, self.line, self.message = level, rule, line, message


def strip_code(line: str) -> str:
    """A line with its string literals emptied and its trailing comment removed, for looking at the code alone."""
    out, i, n = [], 0, len(line)
    while i < n:
        c = line[i]
        if c == '"':
            j = i + 1
            while j < n and line[j] != '"':
                j += 2 if line[j] == "\\" else 1
            out.append('""')
            i = j + 1
        elif c == "'" and i + 2 < n:
            j = i + 1
            while j < n and line[j] != "'":
                j += 2 if line[j] == "\\" else 1
            out.append("''")
            i = j + 1
        elif line.startswith("//", i):
            break
        else:
            out.append(c)
            i += 1
    return "".join(out)


def split_methods(lines):
    """Each method as (name, first line index, last line index, annotations and comments just above it)."""
    methods, depth, in_block, i = [], 0, False, 0
    code = []
    for line in lines:
        text = line
        if in_block:
            if "*/" in text:
                text = text.split("*/", 1)[1]
                in_block = False
            else:
                code.append("")
                continue
        while "/*" in strip_code(text):
            before, rest = text.split("/*", 1)
            if "*/" in rest:
                text = before + rest.split("*/", 1)[1]
            else:
                text = before
                in_block = True
                break
        code.append(strip_code(text))
    while i < len(lines):
        if depth == 1:
            signature = code[i]
            if METHOD_OPENS.match(signature):
                k = i
                while ")" not in code[k] and k + 1 < len(lines) and k - i < 12:
                    k += 1
                    signature += " " + code[k].strip()
            m = METHOD.match(signature)
            if m:
                start, name = i, m.group(3)
                # Find the opening brace, then its match.
                d, j, opened = 0, i, False
                while j < len(lines):
                    d += code[j].count("{") - code[j].count("}")
                    if "{" in code[j]:
                        opened = True
                    if opened and d <= 0:
                        break
                    j += 1
                above, k = [], start - 1
                while k >= 0 and (lines[k].strip().startswith("@") or lines[k].strip().startswith("//") or not lines[k].strip()):
                    if lines[k].strip():
                        above.insert(0, (k, lines[k].strip()))
                    elif above:
                        break
                    k -= 1
                methods.append((name, start, j, above, m.group(2).strip()))
                i = j + 1
                continue
        depth += code[i].count("{") - code[i].count("}")
        i += 1
    return methods, code


def under(body, n) -> list:
    """The lines an `if` or `while` on line n of a test governs: the rest of that line, and its block (with the
    `else` that follows it) or, where it has no braces, the statement below."""
    opens = re.search(r"\b(?:if|while)\s*\(", body[n])
    depth, i = 0, opens.end()
    while i < len(body[n]):  # to the bracket that closes the condition
        depth += {"(": 1, ")": -1}.get(body[n][i], 0)
        i += 1
        if depth < 0:
            break
    rest = body[n][i:]
    out = [rest]
    if "{" not in rest:
        if not rest.strip().rstrip(";") and n + 1 < len(body):
            out.append(body[n + 1])
        if rest.strip() or "{" not in (body[n + 1] if n + 1 < len(body) else ""):
            return out
        rest = ""
    depth, k = rest.count("{") - rest.count("}"), n
    while k + 1 < len(body) and (depth > 0 or (k == n and "{" not in rest) or re.match(r"\s*\}?\s*else\b", body[k + 1])):
        k += 1
        out.append(body[k])
        depth += body[k].count("{") - body[k].count("}")
    return out


def class_fields(code, methods) -> list:
    """(line index, name) of each field of the class: what is declared at the class's own depth and is no method."""
    inside = set()
    for name, start, end, above, ret in methods:
        inside.update(range(start, end + 1))
    out, depth = [], 0
    for i, line in enumerate(code):
        if depth == 1 and i not in inside:
            m = FIELD.match(line)
            if m:
                out.append((i, m.group(1)))
        depth += line.count("{") - line.count("}")
    return out


# The moments of a test: a play, an answer, a pass or a skip moves the game on to another decision, and what is read
# between two of them is read at one decision.
MOVES_ON = re.compile(r"\b(Freeps|Shadow)(PlayCard|UseCardAction|TransferCard|DiscardToHeal|Choose\w*|Accept\w*|Decline\w*|"
                      r"Pass\w*|Assign\w*|Resolve\w*|Dismiss\w*)\s*\(|"
                      r"\b(Pass\w+Actions|BothPass\w*|PassResponses|PassCurrentPhaseActions|Skip\w+|StartGame|Dismiss\w+)\s*\(")
# An offer read as absent: nothing is offered to a player who is not being asked, whatever the card does.
NOT_OFFERED = re.compile(r"\bassertFalse\s*\(\s*scn\s*\.\s*(Freeps|Shadow)(\w*(?:Action|Play|Transfer)\w*Available)\s*\(|"
                         r"\bassertTrue\s*\(\s*scn\s*\.\s*(Freeps|Shadow)(HasCardChoiceNotAvailable)\s*\(")
# The phase-action bookends are fixed to their seats.
SEAT_OF_AWAITING = (("Freeps", re.compile(r"\bAwaiting(?:Fellowship|Freeps\w+)PhaseActions\s*\(")),
                    ("Shadow", re.compile(r"\bAwaitingShadow\w*PhaseActions\s*\(")))


def is_asked(side: str, lines) -> bool:
    """Whether some line among these shows that the player is being asked something: his phase-action bookend, a
    decision or an optional trigger of his read as there, or an offer to him read as made (he is then at a choice
    of actions, and another offer read as absent beside it means something)."""
    for line in lines:
        if any(seat == side and pattern.search(line) for seat, pattern in SEAT_OF_AWAITING):
            return True
        if re.search(r"\b%s(?:\w*DecisionAvailable|AnyDecisionsAvailable|HasOptionalTriggerAvailable|AnyActionsAvailable|"
                     r"AwaitingRequiredResponseOrder|AwaitingSkirmishChoice|GetChoiceMin|GetChoiceMax|GetSelectableCount|HasCardChoiceAvailable|"
                     r"HasBPChoiceAvailable|GetCardChoiceCount|GetBPChoiceCount)\s*\(" % side, line) \
                and not re.search(r"\bassertFalse\s*\(", line):
            return True
        if re.search(r"\bassertTrue\s*\(\s*scn\s*\.\s*%s\w*Available\s*\(" % side, line) and "NotAvailable" not in line:
            return True
    return False


_baseline = None


def baseline_tests() -> dict:
    """How many tests each card had before the campaign, from the campaign's baseline.tsv."""
    global _baseline
    if _baseline is None:
        _baseline = {}
        path = g.state_dir() / "baseline.tsv"
        if path.is_file():
            rows = path.read_text(encoding="utf-8").split("\n")
            head = rows[0].split("\t")
            if "id" in head and "tests" in head:
                for row in rows[1:]:
                    cells = row.split("\t")
                    if len(cells) == len(head) and cells[head.index("tests")].isdigit():
                        _baseline[cells[head.index("id")]] = int(cells[head.index("tests")])
    return _baseline


NO_EXCEPTIONS = ("a card test never relies on an exception (Ketura, 2026-10-08). Where only an exception shows a check, "
                 "the rig lacks a reading: report a rig request, take the assertion out, and write the check in the ledger "
                 "as 'waits <check>: G-0000 <what the rig lacks>', which parks the card until the rig can show it")


class Waived(set):
    """The checks a clause says it cannot have, with the ones that only wait on a rig request kept apart."""
    def __init__(self):
        super().__init__()
        self.waits = {}  # check -> the rig request it waits on


def parse_ledger(path: Path):
    """A card's ledger file as ({id: [type, checks, text, line, waived]}, {id: {check: [test names]}}, cited ids,
    [(line number, what is wrong with that line)]), or (None, None, [], []) if there is no file."""
    if not path.is_file():
        return None, None, [], []
    lines = path.read_text(encoding="utf-8").replace("\r", "").split("\n")
    clauses, claims, cited, faults = {}, {}, [], []
    # above: what the lines above an indented line of no kind of its own make of it.  Under a clause line it is
    # more of the clause's text; under a `no` or `waits` line it is that line's reason running on.
    last, in_header, above = None, False, None
    for i, line in enumerate(lines):
        m = LEDGER_LINE.match(line)
        c = CLAIM_LINE.match(line) or CLAIM_AND_WORDS.match(line)
        if m:
            clauses[m.group(1)] = [m.group(2), set(m.group(3).split()), m.group(4), i, Waived()]
            claims[m.group(1)] = {}
            last, in_header, above = m.group(1), False, "text"
        elif c and last is not None and not in_header:
            for check in c.group(1).split():
                claims[last].setdefault(check, []).append(c.group(2))
            above = "claim"
            if c.re is CLAIM_AND_WORDS:
                faults.append((i + 1, "'%s' after the test's name %s: a claim line is `<checks> -> <TestName>` and nothing "
                                      "more, and a remark goes in Notes" % (c.group(3)[:40], c.group(2))))
                above = "reason"
        elif WAITS_LINE.match(line) and last is not None and not in_header:
            w = WAITS_LINE.match(line)
            for check in w.group(1).split():
                clauses[last][4].waits[check] = w.group(2)
            above = "reason"
        elif WAIVER_LINE.match(line) and last is not None and not in_header:
            # "no -: why": a check the clause's type asks for and this clause cannot have.
            clauses[last][4].add(WAIVER_LINE.match(line).group(1))
            above = "reason"
        elif re.match(r"^\s*(LEDGER|Sources|Findings|Formats|Dropped|Notes)\b", line):
            in_header = True
        elif not line.strip():
            pass
        elif last is not None and not in_header:
            if above == "text":
                # A continuation of the clause's text.
                clauses[last][2] += " " + line.strip()
            elif above == "claim":
                faults.append((i + 1, "'%s' stands below a claim line of %s and is not a claim, a `no` or a `waits` line: "
                                      "a clause's text is written above its claims, and a remark goes in Notes"
                                      % (line.strip()[:40], last)))
                above = "reason"
            # Otherwise the reason of a `no` or `waits` line running on to another line, or more of a remark
            # already reported.
        cited += re.findall(r"\b[FQ]-\d{4}\b", line)
    return clauses, claims, cited, faults


_log_entries = None


def rulings_log_entries() -> list:
    """The rulings log's entries as (line number, heading, text).  An entry is a paragraph that opens in bold, with
    the paragraphs after it that do not, up to the next section heading; its heading is the words in bold."""
    global _log_entries
    if _log_entries is None:
        _log_entries = []
        log = g.SKILLS / "card-test-campaign" / "references" / "rulings-log.md"
        lines = log.read_text(encoding="utf-8").split("\n") if log.is_file() else []
        entry, start = None, None  # the entry being read, and the first line of the paragraph being read
        for i, line in enumerate(lines + [""]):
            if line.strip() and not line.startswith("#"):
                if start is None:
                    start = i
                continue
            if start is not None:
                paragraph = "\n".join(lines[start:i])
                opens = re.match(r"\*\*(.+?)\*\*", paragraph, re.S)
                if opens:
                    entry = [start + 1, re.sub(r"\s+", " ", opens.group(1)).strip().rstrip(".:"), ""]
                    _log_entries.append(entry)
                if entry is not None:
                    entry[2] += paragraph + "\n"
                start = None
            if line.startswith("#"):
                entry = None  # what stands under a section heading belongs to no entry above it
    return _log_entries


def squash(text: str) -> str:
    return re.sub(r"[\s.]+", "", g.plain_text(text))


_by_path = None


def _ids_by_path():
    global _by_path
    if _by_path is None:
        _by_path = {str(p.resolve()): cid for cid, p in g.test_index().items()}
    return _by_path


def lint_file(path: Path):
    out = []

    def add(level, rule, line, message):
        out.append(Finding(level, rule, line + 1 if line is not None else 0, message))

    raw = path.read_text(encoding="utf-8", errors="replace").replace("\r", "")
    lines = raw.split("\n")
    lets_off = 0

    def let_off(i):
        nonlocal lets_off
        if re.search(r"//\s*lint-ok\s*:\s*\S", lines[i]):
            lets_off += 1
            return True
        return False

    card_id = _ids_by_path().get(str(path.resolve()))
    card = g.card_index().get(card_id) if card_id else None
    if card is None:
        add("error", "card", None, "cannot tell which card this file is for, or the card has no definition")

    # Leftovers from the generated skeleton.
    for i, line in enumerate(lines):
        if re.match(r"^\s*//\s*@Test\b", line):
            add("error", "placeholder", i, "a commented-out @Test: delete the skeleton method or make it a real test")
        if "Uncomment any @Test markers" in line:
            add("error", "placeholder", i, "the skeleton's 'Uncomment' note is still here")
        if re.search(r'put\(\s*"card"\s*,', line):
            add("error", "alias", i, 'a card is still called "card": give it a name that says what it is')
        if re.search(r"put other cards in here as needed", line):
            add("error", "placeholder", i, "the skeleton's 'put other cards in here' note is still here")

    methods, code = split_methods(lines)
    tests = [m for m in methods if any(t.startswith("@Test") for _, t in m[3])]
    others = [m for m in methods if m not in tests]
    scenario_methods = [m for m in others if re.match(r"^Get\w*Scenario\w*$", m[0])]
    for name, start, end, above, ret in others:
        if any(re.match(r"^//\s*@Test\b", t) for _, t in above):
            continue  # the skeleton's placeholder, already reported
        if not re.match(r"^Get\w*Scenario\w*$", name):
            add("error", "helper", start, "'%s' is neither a test nor a Get...Scenario() method: what it does belongs in the rig" % name)
    if not scenario_methods:
        add("error", "scenario", None, "no Get...Scenario() method")
    for at, name in class_fields(code, methods):
        add("error", "field", at, "'%s' is a field: a test class has no fields (a deck kept in one is written into each "
                                  "Get...Scenario() method, a constant where it is used)" % name)
    if re.search(r"^\s*public\s+class\s+\w+\s+extends\b", raw, re.M):
        add("error", "helper", None, "a card's test class does not extend anything")

    stats = [m for m in tests if re.search(r"Stats(AndKeywords)?AreCorrect$", m[0])]
    if not stats:
        add("error", "stats", None, "no ...StatsAndKeywordsAreCorrect test")
    # A Race to Mount Doom modifier's intensity is a printed field of it (the Standard, "The stats test").
    if card is not None and (card.field("type", "") or "").strip().lower() == "metasite":
        for name, start, end, above, ret in stats:
            if not any(re.match(r"^\s*/?\*+\s*Intensity\s*:", line) for line in lines[start:end + 1]):
                add("error", "stats", start, "%s: the header has no `Intensity:` line, which a Race to Mount Doom modifier's has" % name)
            if not any(re.search(r"\bgetIntensity\s*\(", line) for line in code[start:end + 1]):
                add("error", "stats", start, "%s: never reads getBlueprint().getIntensity(), which a Race to Mount Doom "
                                             "modifier's stats test asserts at the definition's value" % name)

    for i, line in enumerate(lines):
        if re.match(r"^\s*/?\*+\s*LEDGER\b", line):
            add("error", "in-file", i, "a LEDGER comment in the test file: the ledger lives in its own file, not in the tests")
        if TAG_LINE.match(line):
            add("error", "in-file", i, "a clause tag comment in the test file: which test claims what is recorded in the ledger file")

    ledger_file = g.ledger_path(card_id) if card_id else None
    clauses, claims, cited, faults = parse_ledger(ledger_file) if ledger_file else (None, None, [], [])
    test_names = {m[0] for m in tests}
    if clauses is None:
        add("error", "ledger", None, "no ledger file (%s)" % (ledger_file if ledger_file else "unknown card"))
        clauses, claims = {}, {}
    else:
        if not clauses:
            add("error", "ledger", None, "the ledger has no clause lines (ID Type [checks] text)")
        for at, what in faults:
            add("error", "ledger", None, "ledger line %d: %s" % (at, what))
        for cid, (ctype, codes, text, line, waived) in clauses.items():
            if ctype not in CORE:
                add("error", "ledger", None, "%s: unknown clause type '%s'" % (cid, ctype))
                continue
            bad = codes - CODES
            if bad:
                add("error", "ledger", None, "%s: unknown check %s" % (cid, " ".join(sorted(bad))))
            for check in sorted(waived):
                if check in codes:
                    add("error", "ledger", None, "%s: lists %s and also says it has none" % (cid, check))
                elif check == "C-" and only_cost_is_discarding_itself(text):
                    pass  # always payable: the line needs no reading (and the Standard no longer asks for it)
                else:
                    add("warning", "waived", None, "%s: no %s, with a reason given in the ledger (for the reviewer to read)" % (cid, check))
            missing = CORE[ctype] - codes - waived
            if missing:
                add("error", "ledger", None, "%s: a %s clause must list at least %s" % (cid, ctype, " ".join(sorted(CORE[ctype]))))
        if card is not None and (card.field("type", "") or "").lower() in PLAYED and not any(c[0] == "Play" for c in clauses.values()):
            add("error", "ledger", None, "no Play clause (P1 Play [+] ...): a card that is played from hand has a test that plays it")
        if card is not None:
            want = squash(card.field("gametext", ""))
            text_clauses = [c for cid, c in clauses.items() if cid.startswith("C")]
            have = squash(" ".join(c[2] for c in sorted(text_clauses, key=lambda c: c[3])))
            if want != have:
                add("error", "ledger-text", None, "the C clauses, read in order, are not the card's game text word for word")

    # Which tests the ledger says claim what.
    claimed_tests = set()
    for cid, by_check in claims.items():
        for check, names in by_check.items():
            for name in names:
                claimed_tests.add(name)
                if name not in test_names:
                    add("error", "claim", None, "%s: the ledger gives check %s to %s, which is not a test in the file" % (cid, check, name))
    parked_tests = {m[0] for m in tests if any(t.startswith("@") and re.search(r"@Ignore\b", t) for _, t in m[3])}
    for cid, (ctype, codes, text, line, waived) in clauses.items():
        for check, names in sorted(claims.get(cid, {}).items()):
            if names and all(n in parked_tests for n in names):
                add("warning", "parked-only", None, "%s: %s is claimed only by a parked test, so nothing exercises it at present" % (cid, check))
        for check, request in sorted(waived.waits.items()):
            # A check may be shown in part by a test and wait in part (one candidate of an F the rig cannot place).
            if request == "G-0000":
                add("warning", "waiting", None, "%s: %s waits on a rig request not yet numbered (the lander numbers it)" % (cid, check))
            else:
                add("warning", "waiting", None, "%s: %s waits on %s: the card is parked until the rig can show it" % (cid, check, request))
        missing = (codes & CODES) - set(claims.get(cid, {})) - set(waived.waits)
        if missing:
            add("error", "coverage", None, "%s: no test claims %s" % (cid, " ".join(sorted(missing))))
        extra = set(claims.get(cid, {})) - codes
        if extra:
            add("error", "claim", None, "%s: tests claim %s, which the clause does not list" % (cid, " ".join(sorted(extra))))
    asked = sorted(set(re.findall(r'@Ignore\("(Q-\d{4})', raw)))
    if card_id and asked:
        # The entries that name the card, or the question itself: which of them it was says at once whether it
        # bears on the question (a note on the card's sheet row does not).
        named = re.compile(r"\b(?:%s)\b" % "|".join(re.escape(x) for x in [card_id] + asked))
        hits = [(at, heading, sorted(set(named.findall(text)))) for at, heading, text in rulings_log_entries() if named.search(text)]
        if hits:
            where = "; ".join('"%s" (line %d, names %s)' % (heading if len(heading) <= 70 else heading[:67] + "...", at, " and ".join(found))
                              for at, heading, found in hits[:4])
            add("warning", "ruled", None, "a test is parked on %s, and the rulings log names the card or the question in %s%s: "
                                          "check whether the question has been answered"
                % (" and ".join(asked), where, " and %d more entries" % (len(hits) - 4) if len(hits) > 4 else ""))
    if card_id and clauses and ledger_file is not None:
        had = baseline_tests().get(card_id, 0)
        if had > 1 and not re.search(r"^\s*Dropped\s*:", ledger_file.read_text(encoding="utf-8"), re.M):
            add("warning", "dropped", None, "the card had %d tests before the campaign and the ledger has no Dropped: line "
                                            "(what the old tests asserted that the new ones do not, or 'none')" % had)
    for name, start, end, above, ret in tests:
        if clauses and name not in claimed_tests and name not in [x[0] for x in stats]:
            add("error", "claim", start, "%s: the ledger gives this test nothing to claim" % name)
        for _, t in above:
            parks = t.startswith("@") and re.search(r"@Ignore\b", t)
            if parks and not re.search(r"\b[FQG]-\d{4}\b", t):
                add("error", "ignore", start, "%s: @Ignore must name the finding (F-0000), question (Q-0000) or rig request (G-0000) it is parked on" % name)
            if parks and not t.startswith("@Ignore"):
                add("error", "ignore", start, "%s: @Ignore shares its line with another annotation: it goes on a line of its own, "
                                              "where every tool that counts or switches on parked tests looks for it" % name)
            if re.search(r"@Test\s*\(.*expected\s*=", t):
                add("error", "exception", start, "%s: %s" % (name, NO_EXCEPTIONS))

    alias_bp = dict(re.findall(r'put\(\s*"([^"]+)"\s*,\s*"(\d+_\d+)"\s*\)', raw))
    # Inside the tests.
    for name, start, end, above, ret in tests:
        body = code[start:end + 1]
        text = "\n".join(body)
        is_stats = name in [s[0] for s in stats]
        if not re.search(r"\bGet\w*Scenario\w*\s*\(", text):
            add("error", "scenario", start, "%s: does not get its table from a Get...Scenario() method" % name)
        for offset, line in enumerate(body):
            i = start + offset
            for pat, why in FORBIDDEN:
                if re.search(pat, line) and not let_off(i):
                    add("error", "reach", i, "%s: %s" % (name, why))
            for pat, why in FORBIDDEN_AS_WRITTEN:
                if re.search(pat, lines[i]) and re.search(pat.replace('""', '"[^"]*"'), line) and not let_off(i):
                    add("error", "reach", i, "%s: %s" % (name, why))
            for pat, why in JUSTIFY:
                if re.search(pat, line) and not let_off(i):
                    add("error", "adhoc", i, "%s: %s" % (name, why))
            # An answer that is given only if the game asks, or for as long as it asks, lets the test pass whether or
            # not it does (93_4, 94_17: taken out, the test failed and showed F-0327).
            if re.search(r"\b(?:if|while)\s*\(.*\bscn\s*\.", line) and any(MOVES_ON.search(x) for x in under(body, offset)) \
                    and not let_off(i):
                add("warning", "conditional", i, "%s: an answer is given under a condition (`if` or `while` on a reading of the "
                    "table), so the test passes whether or not the game asks: a test knows what is asked, asserts it, "
                    "and answers it" % name)
            if re.search(r"\bassert(DoesNot)?Throws?\w*\s*\(|\btry\s*\{|\bcatch\s*\(", line):
                add("error", "exception", i, "%s: %s" % (name, NO_EXCEPTIONS))
            if re.search(r"\.getZone\s*\(\s*\)", line) and not let_off(i):
                add("warning", "style", i, "%s: compare zones with assertInZone and friends" % name)
            if re.search(r"assert(True|False)\s*\(.*(==|!=)", line) and not let_off(i):
                add("warning", "style", i, "%s: assertEquals reports both values where assertTrue(a == b) reports neither" % name)
        if not is_stats:
            last_act = max([n for n, line in enumerate(body) if ACTS.search(line)], default=None)
            if last_act is not None and not any(BOOKEND.search(line) for line in body[last_act:]):
                add("error", "bookend", start, "%s: after its last play or answer, never checks what the game is waiting for" % name)
        if not is_stats and not re.search(r"\bassert\w+\s*\(", text):
            add("error", "assert", start, "%s: asserts nothing" % name)
        # An offer read as absent at a moment when nothing shows that the player is being asked (pitfalls, "A check on
        # a player who is not being asked anything answers false"): 91_13, 92_24, 94_7 and 94_8's old tests.
        moves = [n for n, line in enumerate(body) if MOVES_ON.search(line)]
        for n, line in enumerate(body):
            for m in NOT_OFFERED.finditer(line):
                side, read, twin = m.group(1) or m.group(3), m.group(2) or m.group(4), bool(m.group(4))
                first = max([a for a in moves if a < n], default=-1) + 1
                last = min([a for a in moves if a > n], default=len(body))
                beside = body[first:last]
                then = body[last] if last < len(body) else ""
                if twin:
                    # The NotAvailable twin answers true at any decision that is no choice of cards.
                    asked = any(re.search(r"\b%s(?:HasCardChoiceAvailable|HasBPChoiceAvailable|GetChoiceMin|GetChoiceMax|"
                                          r"GetCardChoiceCount|GetBPChoiceCount|GetSelectableCount|\w*Revealed\w*|"
                                          r"\w*DecisionAvailable|AwaitingSkirmishChoice)\s*\(" % side, x)
                                and not re.search(r"\bassertFalse\s*\(", x) for x in beside) \
                        or bool(re.search(r"\b%s(?:Choose\w*|Decline\w*|Dismiss\w*)\s*\(" % side, then))
                else:
                    asked = is_asked(side, beside) or bool(re.search(r"\b%s(?!Pass)\w+\s*\(" % side, MOVES_ON.search(then).group(0) if then else ""))
                if not asked and not let_off(start + n):
                    add("warning", "not-asked", start + n, "%s: %s%s is read as %s with nothing beside it that shows the %s player "
                        "is being asked %s (%s): read at any other moment it comes out the same whatever the card does (pitfalls, "
                        "\"A check on a player who is not being asked anything answers false\")"
                        % (name, side, read, "true" if twin else "false", "Free Peoples" if side == "Freeps" else "Shadow",
                           "to choose cards" if twin else "anything",
                           "a card read as offered in that choice, its minimum, or the decision by its words" if twin else
                           "his Awaiting...PhaseActions(), a decision of his, or another offer to him read as made"))
        # A play read as refused for a card the test never puts in a hand (pitfalls, "A play read as not offered for a
        # card that is not in hand shows nothing": 91_15's old test read Barliman so, from the draw deck).
        rawtext_of_test = "\n".join(lines[start:end + 1])
        plays_from_a_deck = card is not None and bool(re.search(r"draw deck", g.plain_text(card.field("gametext", "") or ""), re.I))
        for refused in dict.fromkeys(re.findall(r"\bassertFalse\s*\(\s*scn\s*\.\s*(?:Freeps|Shadow)PlayAvailable\s*\(\s*(\w+)\s*\)", text)):
            alias = dict(re.findall(r'\bvar\s+(\w+)\s*=\s*scn\.Get(?:Freeps|Shadow)Card\(\s*"([^"]+)"', rawtext_of_test)).get(refused)
            one_statement = re.sub(r"\s*\n\s*", " ", rawtext_of_test).split(";")
            if alias and not plays_from_a_deck and not taken_out_of_deck(one_statement, refused, alias):
                at = next(n for n, line in enumerate(body) if re.search(r"PlayAvailable\s*\(\s*%s\s*\)" % re.escape(refused), line))
                if not let_off(start + at):
                    add("warning", "not-in-hand", start + at, "%s: %s is read as not playable, and no cheat in the test takes it out "
                        "of its owner's draw deck, where a table leaves every card: there it reads as not playable whatever "
                        "the card under test does (pitfalls, \"A play read as not offered for a card that is not in hand "
                        "shows nothing\")" % (name, refused))
        # A choice from a pile goes by blueprint: with a second copy of that blueprint still in the pile the game may
        # hand over either, and a test that then reads the named copy's zone fails at random.  A twin the test never
        # names lies in its owner's draw deck from first to last, so there is nothing to warn of where the test has
        # cheated the chosen card out of the draw deck and the card under test never chooses from one (its text has
        # no "draw deck": "from your draw deck or discard pile" lists both piles together).
        rawtext = "\n".join(lines[start:end + 1])
        var_alias = dict(re.findall(r'\bvar\s+(\w+)\s*=\s*scn\.Get(?:Freeps|Shadow)Card\(\s*"([^"]+)"', rawtext))
        from_a_deck = card is None or bool(re.search(r"draw deck", g.plain_text(card.field("gametext", "") or ""), re.I))
        for chosen in re.findall(r'Choose\w*BPFromSelection\(\s*(\w+)\s*\)', text):
            alias = var_alias.get(chosen)
            bp = alias_bp.get(alias)
            twins = sorted(a for a, b in alias_bp.items() if b == bp and a != alias) if bp else []
            moved = [t for t in twins if re.search(r'GetFreepsCard\(\s*"%s"|GetShadowCard\(\s*"%s"|\b%s\b' % (t, t, t), rawtext)]
            if twins and not moved and not from_a_deck and taken_out_of_deck(lines[start:end + 1], chosen, alias):
                continue
            if twins and not moved:
                add("warning", "same-blueprint", start, "%s: %s is chosen by blueprint, and %s %s the same blueprint (%s): unless "
                    "the twin is out of that pile, either copy may be taken and a reading of %s's zone fails at random"
                    % (name, chosen, ", ".join(twins), "has" if len(twins) == 1 else "have", bp, chosen))

        # It cuts both ways (68_82): at such a choice a card read as NOT offered fails that reading as soon as a card
        # of its blueprint is offered there.  Cards in play are read one by one (a second copy read as not offered
        # beside the first is what the Standard asks for), so this is only for a choice the test goes on to answer by
        # blueprint, which is one among cards that are not in play.
        moments = [n for n, line in enumerate(body) if MOVES_ON.search(line)]
        for n, line in enumerate(body):
            refused = [v for call in re.findall(r"HasCardChoiceNotAvailable\s*\(([^()]*)\)", line) for v in re.findall(r"\w+", call)]
            if not refused:
                continue
            first = max([a for a in moments if a < n], default=-1) + 1
            last = min([a for a in moments if a > n], default=len(body))
            by_bp = bool(re.search(r"BPFromSelection\s*\(", body[last])) if last < len(body) else False
            offered = [v for x in body[first:min(last + 1, len(body))]
                       for call in re.findall(r"(?:HasCardChoiceAvailable|BPFromSelection)\s*\(([^()]*)\)", x) for v in re.findall(r"\w+", call)]
            for no in dict.fromkeys(refused):
                same = sorted({yes for yes in offered if yes != no and var_alias.get(yes) and var_alias.get(no)
                               and alias_bp.get(var_alias[yes]) and alias_bp.get(var_alias[yes]) == alias_bp.get(var_alias[no])})
                if same and by_bp:
                    add("warning", "same-blueprint", start + n, "%s: %s is read as not offered and %s, of the same blueprint (%s), "
                        "as offered: where the choice is among cards that are not in play (a search of a deck, a discard "
                        "pile, a reveal) both are read by blueprint, and the reading of %s fails as soon as the other "
                        "holds; a card read as not offered there has a blueprint no offered card has"
                        % (name, no, ", ".join(same), alias_bp[var_alias[no]], no))

    # A card that is not unique and speaks of itself ("this condition", "here"): the Standard's F asks for a second
    # copy in play wherever it could change a reading, and no scenario can hold one with a single alias of the
    # blueprint (two reviewers found the copy missing where this check had passed: err-b58_1, err-b58_48).
    if card is not None and (card.field("type", "") or "").lower() in PLAYED \
            and (card.field("unique", "") or "false").strip().lower() == "false":
        says = ITSELF.search(g.plain_text(card.field("gametext", "")))
        copies = sorted(a for a, b in alias_bp.items() if b == card_id)
        if says and len(copies) < 2:
            add("warning", "second-copy", None, "the card is not unique and its text says \"%s\", and no scenario has two "
                "aliases of %s: a second copy is in play wherever it could change a reading (which copy a card went to "
                "or came from, a modifier tied to those words not stacking), or Notes say why none could"
                % (says.group(0).lower(), card_id))

    # A site's continuous text built as a plain `modifier` holds only while the fellowship is at the site (CR 4.2
    # "active", as LR 3 has it), and the site's own ledger owes an R clause for the same board with the site left
    # behind (shapes, "A site's continuous text").  Dimrill Dale 1_350's ledger pointed to a mechanics class instead
    # and passed; the reviewer added the test (dec-b1_350).  A `PermanentSiteModifier` owes none.
    if card is not None and clauses and (card.field("type", "") or "").strip().lower() == "site" \
            and re.search(r"^\s*type\s*:\s*modifier\s*$", card.raw, re.I | re.M) \
            and not any(cid.startswith("R") and SITE_LEFT.search(c[2]) for cid, c in clauses.items()):
        add("warning", "site-left", None, "the site's continuous text is built as a plain `modifier`, which holds only while "
            "the fellowship is at the site (CR 4.2 \"active\", LR 3), and the ledger has no R clause for the site left "
            "behind: the same board with the fellowship moved on, where the text no longer bites, in this card's own "
            "file (shapes, \"A site's continuous text\"; a pointer to mechanics/SiteTextHoldsOnlyWhereTheFellowshipIsTests "
            "does not stand for it)")

    # A race and the culture that goes with it (F-0019, F-0291).  No board is asked for any more (Q-0213: B): the
    # slip is read off the definition, and what is reported is the definition, as a finding to file.  Where the
    # text and the definition agree, nothing is said.
    if card is not None:
        for slip in race_culture_slips(card):
            add("warning", "race-culture", None, slip)

    # The census (Q-0220: B): a word of the text that nothing in the definition holds ("a [rohan] ally" healed as
    # any ally, 4_290; "exert a Hobbit companion" with no exertion, 7_325).  Like race-culture it is read off the
    # definition and asks for no board; unlike it, a match is only a place to read, since the word may be carried by
    # something the check cannot see.  Where text and definition agree, nothing is said.
    if card is not None:
        import census
        for which, words, message in census.check(card_id):
            add("warning", "census", None, "%s: read the definition against the text there; where the definition is at "
                "fault that is a finding to file (`hjson-logic`), and where something else carries the words (a trigger "
                "only such a card can set off, a rule of the game) the ledger's Notes say what (Q-0220: B; `census.py %s`)"
                % (message, card_id))

    # A title that cards of more than one culture or type carry (Gollum is [gollum] and [wraith]; Saruman a minion
    # and a follower): with one kind of it on every table, a culture or type written beside the name in the
    # definition passes every test (60_20).  Like second-copy, this reads which cards the file names at all, not
    # which of them a test puts in play.  A card that names its own title is left alone ("limit 1 per bearer",
    # "another Elf"): the other kind is then its own original or erratum.
    if card is not None:
        in_file = set(re.findall(r'"(\d+_\d+)"', raw)) | {card_id}
        own = same_title(card.field("title", ""))
        for title in sorted({same_title(t) for t in re.findall(r"\b(?:name|title)\(([^()]+)\)", card.raw, re.I)} - {own}):
            kinds = title_kinds().get(title)
            if not kinds:
                continue
            have = sorted(k for k, ids in kinds.items() if in_file & set(ids))
            if len(have) == 1:
                def words(kind):
                    return ("[%s] %s" % kind) if kind[0] else kind[1]
                others = ", ".join("%s (%s)" % (words(k), " ".join(sorted(ids, key=g.id_sort_key)[:3]) + (" ..." if len(ids) > 3 else ""))
                                   for k, ids in sorted(kinds.items()) if k != have[0])
                add("warning", "title-kinds", None, "the definition names %s, and every card of that title in this file is "
                    "%s; there is also %s: where the text names the title with no culture or type word, each kind "
                    "is a valid candidate in a test of its own (the Standard's F), or a culture or type written beside "
                    "the name passes every test" % (g.plain_text(g.card_index()[kinds[have[0]][0]].field("title", title)), words(have[0]), others))

    if card is not None:
        import cardinfo
        for field, there, wanted in cardinfo.check(card_id):
            add("warning", "cardinfo", None, "the definition's cardInfo has %s %s where the rest of the definition and the "
                "other cards of its kind say %s: put the definition right (Q-0145; `cardinfo.py %s`), no test reads it"
                % (field, there, wanted, card_id))

    if card is not None and g.sheet_for(card_id)[0] is not None:
        import reconcile
        diffs = reconcile.compare(card_id)
        if diffs and not cited:
            add("warning", "sheet", None, "the spreadsheet and the definition disagree on %s, and the LEDGER cites no finding"
                % ", ".join(d[0] for d in diffs))
    return out, lets_off


# The rules that are about how a test is written, whatever file it is in (--form).
FORM_RULES = {"bookend", "reach", "exception", "adhoc", "assert", "ignore", "style", "helper", "field", "scenario", "in-file",
              "alias", "same-blueprint", "conditional", "not-asked", "not-in-hand"}


def main(argv):
    only = set()
    if "--only" in argv:
        i = argv.index("--only")
        only = set(argv[i + 1].split(","))
        argv = argv[:i] + argv[i + 2:]
    args = [a for a in argv[1:] if not a.startswith("--")]
    if not args:
        print(__doc__)
        return 0
    tests = g.test_index()
    files = []
    for a in args:
        p = Path(a)
        if a.endswith(".java"):
            files.append(p.resolve() if p.is_file() else g.REPO / a)
        else:
            for card_id in g.resolve_ids([a]):
                if card_id in tests:
                    files.append(tests[card_id])
    failed, by_rule, total_off = 0, {}, 0

    def shown(f):
        """A file as the repository names it, or by its whole path where it lies outside (a probe under /tmp)."""
        try:
            return g.rel(f)
        except ValueError:
            return str(f)

    for f in files:
        if not f.is_file():
            print("FAIL: %s: no such file" % f)
            failed += 1
            continue
        findings, lets_off = lint_file(f)
        if "--form" in argv:
            findings = [x for x in findings if x.rule in FORM_RULES]
            if only:
                methods, _ = split_methods(f.read_text(encoding="utf-8", errors="replace").replace("\r", "").split("\n"))
                spans = [(start + 1, end + 1) for name, start, end, above, ret in methods if name in only]
                findings = [x for x in findings if any(a <= x.line <= b for a, b in spans)]
                missing = only - {m[0] for m in methods}
                if missing:
                    print("no such test in %s: %s" % (f.name, ", ".join(sorted(missing))))
        total_off += lets_off
        errors = [x for x in findings if x.level == "error"]
        warnings = [x for x in findings if x.level == "warning"]
        for x in findings:
            by_rule[(x.level, x.rule)] = by_rule.get((x.level, x.rule), 0) + 1
        if errors:
            failed += 1
        if "--summary" in argv:
            continue
        if "--quiet" in argv:
            print("%-4s %s (%d errors, %d warnings%s)" % ("FAIL" if errors else "ok", f.name, len(errors), len(warnings),
                                                          ", %d let off" % lets_off if lets_off else ""))
            continue
        if findings or lets_off:
            print("%s: %s" % ("FAIL" if errors else "ok", shown(f)))
            for x in sorted(findings, key=lambda x: x.line):
                print("  %-7s %-11s line %-4d %s" % (x.level, x.rule, x.line, x.message))
            if lets_off:
                print("  note    %d line(s) let off with lint-ok" % lets_off)
        else:
            print("ok: %s" % shown(f))
    if "--summary" in argv or len(files) > 1:
        print("\n%d of %d files pass.  %d line(s) let off." % (len(files) - failed, len(files), total_off))
        for (level, rule), n in sorted(by_rule.items(), key=lambda kv: -kv[1]):
            print("  %5d  %-7s %s" % (n, level, rule))
    return 1 if failed else 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
