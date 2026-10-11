#!/usr/bin/env python3
"""Writes the test of a card's Play clause (P1): the card played from hand by legal play, what it cost and where
it ended up (the Card Test Standard, "Clause types", `Play`).  Ketura on Q-0220, I: "generate".

    gen_play.py <ids...>            prints, for each card, the test method and its ledger lines, or why the card
                                    is left to its author
    gen_play.py <ids...> --write    puts the test into the card's test file, after the stats test, and the P1
                                    clause and its claim into the card's ledger.  No other test is touched; the
                                    skeleton's commented-out placeholder, which is this test unwritten, gives way
                                    to it, and a card the file still calls "card" is given a name
    gen_play.py <id> --alias <name> that name, in place of one made from the card's title
    gen_play.py --census [<what...>]
                                    counts, over the cards given (default: every Decipher card), how many it
                                    writes and how many it leaves, by the reason

<ids> are blueprint ids or selectors (1_40, set:1, file:set01-Elven).  Exit status: 0 when every card given was
written, 3 when any was left to its author, 1 on a mistake in the command.  Read-only without --write.

What the test expects comes from the card's sheet row: its cost, its type and, for a minion, its site number.  A
card no sheet has (the Hobbit sets) is read from its definition's printed fields, and the output says so.  The
definition's effects are read only to decide whether the card is one the generator can do.

It writes for a companion, an ally, a minion, a follower, and a condition, possession or artifact that is played
to the support area, where nothing in the card's text or definition bears on its being played.  The test stands
on the file's own GetScenario() with nothing in play but the Ring-bearer: a Free Peoples card is played in the
fellowship phase of turn 1, a Shadow card in the Shadow phase at site 2 (where a minion whose site number is
above 2 pays the roaming penalty).  It reads the card in hand and offered, the pool before and after, the zone the
card ends in (and, for a card that could be borne, that the Ring-bearer does not bear it), and what the game then
waits for.

It leaves to the author, saying which:

  - a card played on a bearer ("Bearer must be ...", "Plays on ..."): the PlayOn clause's test plays it;
  - a requirement or an extra cost to play ("To play, spot ...", "To play, exert ..."): the ToPlay or ExtraCost
    clause's test plays it;
  - a cost that is no plain number, or that the card's own text changes or speaks of ("twilight cost", a
    discount, "roaming"), and toil;
  - a text that says when or whether the card may be played, or that acts from the hand;
  - a card that answers its own play or its being in play (a "played", "enters play" or continuously checked
    trigger or response, "about to add twilight"): the test of that clause has an offer or a result to read;
  - a card that cannot be played beside the scenario's Ring-bearer (a Frodo), a card with no test file, a
    GetScenario() it cannot read or that seats a Race to Mount Doom modifier, a site under the play whose text
    is about costs (Edoras Hall), a sheet row that disagrees with the definition on cost, type or site number,
    and a card that is no character whose definition does not say its side (the Hobbit sets).

For such a card the author writes P1's test by hand, as before: it is nearly always the test of the clause that
got in the way, which claims P1 as well.  With --write it also refuses a card whose ledger already gives P1 to
another test, and never writes over a test.
"""
import re
import sys

import gempcards as g

SUPPORT_TYPES = {"condition", "possession", "artifact", "follower"}
PLAYED = {"companion", "ally", "minion"} | SUPPORT_TYPES

# Top-level effects of a definition that have no part in the card being played from hand.
QUIET_EFFECTS = {"activated", "modifier", "trigger", "response", "aidcost", "activatedfromstacked",
                 "activatedindiscard", "activatedindrawdeck", "extrapossessionclass", "displayableinformation", "killedtrigger",
                 "selfremovedfromplaytrigger", "discardedfromplaytrigger", "stackedonmodifier"}
# Those that do, with the kind of thing each is and what the author is told.
PLAY_EFFECTS = {
    "toplay": ("toplay", "it has a requirement to play: the ToPlay clause's test plays it (not playable, then playable) and claims P1"),
    "extracost": ("toplay", "it has an extra cost to play: the ExtraCost clause's test plays it and claims P1"),
    "exerttargetextracost": ("toplay", "it has an extra cost to play: the ExtraCost clause's test plays it and claims P1"),
    "modifyowncost": ("cost", "its own text changes its twilight cost: the test of that clause reads the cost at two counts and claims P1"),
    "discount": ("cost", "its own text offers a discount as it is played: the test of that clause plays it and claims P1"),
    "inhandtrigger": ("hand", "it has a text that acts from the hand"),
    "playedinotherphase": ("when", "its text says when it may be played: the test of that clause plays it and claims P1"),
    "copycard": ("copy", "it copies another card's text"),
}
# What a trigger or a response answers.  The ones here cannot be set off by the card's own play from hand, on a
# table that holds nothing else but the Ring-bearer; any other is taken to be one that can.
QUIET_TRIGGERS = {"winsskirmish", "losesskirmish", "startofphase", "endofphase", "startofturn", "endofturn", "moves",
                  "movesfrom", "movesto", "assignedagainst", "assignedtoskirmish", "startofskirmishinvolving",
                  "afterallskirmishes", "killed", "abouttobekilled", "abouttotakewound", "takeswound", "exerted",
                  "heals", "discarded", "abouttodiscard", "transferred", "losesinitiative", "addsburden",
                  "removesburden", "addsthreat", "playerdrawscard", "abouttodrawcard", "abouttobeoverwhelmed",
                  "reconciles", "replacessite", "usesspecialability", "sitecontrolled", "revealscardfromtopofdrawdeck",
                  "cancelledskirmish", "abouttoheal", "abouttoexert", "freeplayerstartedassigning", "putsonring",
                  "takesoffring", "removedfromplay", "abouttomoveto", "skirmishabouttoend", "liberatessite",
                  "abouttoaddburden", "newringbearer", "abouttotakecontrolofsite", "hindered", "abouttohinder",
                  "beforethreatwounds", "exertsforspecialability", "addsculturetoken", "revealscardfromdrawdeck"}

# Words of a game text that say something of the card's own play, each with the kind of thing it is: where the
# definition has an effect of that kind the definition's reason is given and the text's is not said again.
TEXT_GUARDS = [
    ("bearer", r"\bbearer must be\b|\bplays on\b",
     "its text says what bears it and its definition names no bearer: the PlayOn clause's test plays it and claims P1"),
    ("toplay", r"\bto play,", "its text has a \"To play, ...\" that its definition has not: the test of that clause plays it and claims P1"),
    ("cost", r"\btwilight cost\b", "its text speaks of a twilight cost: the test of that clause reads the cost and claims P1"),
    ("roaming", r"\broaming\b", "its text speaks of roaming: the test of that clause reads what the play costs and claims P1"),
    ("when", r"\b(?:may|can) (?:not |only )?be played\b|\bcannot be played\b|\bcan't be played\b",
     "its text says when or whether it may be played: the test of that clause plays it and claims P1"),
    ("toil", r"\btoil\b", "it has toil, which may pay part of its cost: the test that plays it reads what was exerted"),
]
SUPPORT_SENTENCE = re.compile(r"\bplays to your support area\b", re.I)

WORDS = ["Nothing", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
         "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty"]
STOP = {"the", "of", "a", "an", "and", "in", "to", "for", "is", "at", "on", "by", "with", "from", "his", "her", "my",
        "our", "your", "their", "its", "we", "you", "i", "it", "he", "she", "they", "them", "us", "me", "be", "not",
        "are", "was", "were", "what", "who", "that", "this", "there", "here", "no", "all", "do", "does", "will", "shall"}
JAVA_WORDS = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
              "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
              "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
              "protected", "public", "return", "short", "static", "super", "switch", "this", "throw", "throws",
              "transient", "try", "var", "void", "volatile", "while", "true", "false", "null", "scn", "twilight"}

SCENARIO_HEAD = re.compile(r"^\t(?:public|protected)\s+VirtualTableScenario\s+GetScenario\s*\(\s*\)")
SKELETON_NOTE = "Uncomment any @Test markers"


def _read(path):
    """(the lines of a file, the line end it uses)."""
    with open(path, encoding="utf-8", newline="") as f:
        text = f.read()
    eol = "\r\n" if "\r\n" in text else "\n"
    return text.replace("\r\n", "\n").split("\n"), eol


def _write(path, lines, eol):
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(eol.join(lines))


class Left(Exception):
    """The card is left to its author, for the reasons given."""
    def __init__(self, reasons):
        super().__init__("; ".join(reasons))
        self.reasons = list(reasons)


# ----------------------------------------------------------------------------------------------------------------
# The card: what its sheet row and its definition say of its being played
# ----------------------------------------------------------------------------------------------------------------

def _int(value):
    value = str(value if value is not None else "").strip()
    return int(value) if re.match(r"^\d+$", value) else None


def _fold(value) -> str:
    import unicodedata
    t = unicodedata.normalize("NFKD", str(value or ""))
    return "".join(c for c in t if not unicodedata.combining(c)).strip().lower()


def _effects(parsed) -> list:
    e = parsed.get("effects")
    if e is None:
        return []
    return [x for x in (e if isinstance(e, list) else [e]) if isinstance(x, dict)]


def _trigger_types(effect) -> list:
    t = effect.get("trigger")
    return [str(x.get("type", "?")).lower() if isinstance(x, dict) else "?" for x in (t if isinstance(t, list) else [t]) if x is not None]


def _keywords(parsed) -> list:
    k = parsed.get("keywords") or parsed.get("keyword") or []
    return [str(x).lower() for x in (k if isinstance(k, list) else [k])]


class Card:
    """What the generator needs of a card: the printed fields it expects by (the sheet's where there is a sheet)
    and the reasons, if any, why the card's play is not a plain one."""

    def __init__(self, card_id):
        self.id = card_id
        self.definition = g.card_index().get(card_id)
        if self.definition is None:
            raise Left(["no card definition"])
        d = self.definition
        self.name = g.full_name(d)
        self.title = d.field("title", "?")
        self.type = _fold(d.field("type"))
        # A definition may leave its side out (the Hobbit sets do): a minion is a Shadow card and a companion or an
        # ally a Free Peoples one by their types; for any other type the side has to be written.
        side = _fold(d.field("side")) or {"minion": "shadow", "companion": "free peoples", "ally": "free peoples"}.get(self.type, "")
        self.shadow = side == "shadow"
        self.unique = _fold(d.field("unique")) == "true"
        self.row, _ = g.sheet_for(card_id)
        self.from_sheet = self.row is not None
        self.reasons = []
        self.support_sentence = False
        if self.type not in PLAYED:
            raise Left(["it is %s: only a card that is played from hand and stays in play has a Play clause"
                        % ({"event": "an event, which is played by its Event clause"}.get(self.type, "a " + (self.type or "card of no type")))])
        self.cost = _int(d.field("twilight"))
        self.site = _int(d.field("site")) if self.type == "minion" else None
        self.text = g.plain_text(d.field("gametext", ""))
        if self.from_sheet:
            row = self.row
            if _fold(row.get("card_type")) != self.type:
                self.reasons.append("the sheet has it as %s and the definition as %s: a difference to settle first"
                                    % (row.get("card_type") or "nothing", d.field("type")))
            if _int(row.get("twilight")) != self.cost:
                self.reasons.append("the sheet's twilight cost is %s and the definition's %s: a difference to settle first"
                                    % (row.get("twilight") or "nothing", d.field("twilight")))
            if self.type == "minion" and _int(row.get("site")) != self.site:
                self.reasons.append("the sheet's site number is %s and the definition's %s: a difference to settle first"
                                    % (row.get("site") or "nothing", d.field("site")))
            self.cost = _int(row.get("twilight"))
            if self.type == "minion":
                self.site = _int(row.get("site"))
            sheet_text = g.plain_text(row.get("game_text"))
            if sheet_text:
                self.text = self.text + " " + sheet_text
        if not side:
            self.reasons.append("its definition does not say which side it is of")
        if self.cost is None:
            self.reasons.append("its twilight cost is no plain number")
        if self.type == "minion" and self.site is None:
            self.reasons.append("it has no plain site number")
        self._read_definition()

    def _read_definition(self):
        try:
            parsed = self.definition.parsed()
        except Exception as problem:  # a definition the hjson package cannot read is not one to write a test from
            self.reasons.append("its definition could not be read (%s)" % str(problem)[:60])
            return
        said, kinds = set(), set()

        def say(reason, kind=None):
            if kind:
                kinds.add(kind)
            if reason not in said:
                said.add(reason)
                self.reasons.append(reason)
        if "target" in parsed:
            say("it is played on a bearer: the PlayOn clause's test plays it, reads its cost and the bonus it gives, and claims P1", "bearer")
        if any(k.startswith("toil") for k in _keywords(parsed)):
            say(TEXT_GUARDS[-1][2], "toil")
        for effect in _effects(parsed):
            kind = str(effect.get("type", "?")).lower()
            if kind in PLAY_EFFECTS:
                say(PLAY_EFFECTS[kind][1], PLAY_EFFECTS[kind][0])
            elif kind not in QUIET_EFFECTS:
                say("its definition has an effect the generator does not know (%s)" % kind)
            if kind in ("trigger", "response"):
                loud = sorted(set(t for t in _trigger_types(effect) if t not in QUIET_TRIGGERS))
                if loud:
                    say("it has a %s that its own play or its being in play may set off (%s): the test of that clause "
                        "plays it, reads what is then asked or done, and claims P1" % (kind, ", ".join(loud)))
        for kind, pattern, reason in TEXT_GUARDS:
            if kind not in kinds and re.search(pattern, self.text, re.I):
                say(reason, kind)
        if self.type in ("condition", "possession", "artifact") and "target" not in parsed:
            if not any(k.replace("_", " ").startswith("support area") for k in _keywords(parsed)):
                say("it names no bearer and has not the Support Area keyword: where it is played to is the author's to read")
        self.support_sentence = bool(SUPPORT_SENTENCE.search(self.text))

    @property
    def roaming(self) -> int:
        """The roaming penalty at site 2, where a minion played on the first turn is played."""
        return 2 if self.type == "minion" and self.site is not None and self.site > 2 else 0


# ----------------------------------------------------------------------------------------------------------------
# The card's test file: its scenario, the name the card goes by, and where the test goes
# ----------------------------------------------------------------------------------------------------------------

_constants = None


def test_constants() -> dict:
    """The canned site paths and Ring-bearers of the test framework's TestConstants: {"FellowshipSites": {"site1":
    "1_319", ...}, "FOTRFrodo": "1_290", ...}."""
    global _constants
    if _constants is None:
        _constants = {}
        source = (g.FRAMEWORK_DIR / "TestConstants.java").read_text(encoding="utf-8")
        for name, body in re.findall(r"HashMap<String,\s*String>\s+(\w+)\s*=\s*new HashMap<>\(\)\s*\{\{(.*?)\}\};", source, re.S):
            _constants[name] = dict(re.findall(r'put\(\s*"(site\d)"\s*,\s*"(\d+_\d+)"\s*\)', body))
        for name, value in re.findall(r'\bString\s+(\w+)\s*=\s*"([^"]*)"\s*;', source):
            _constants[name] = value
    return _constants


def _without_comments(text: str) -> str:
    """Java source with its line comments taken out (a comment may hold a quote or a comma of its own)."""
    out = []
    for line in text.split("\n"):
        quoted, cut = False, len(line)
        for i, ch in enumerate(line):
            if ch == '"' and (i == 0 or line[i - 1] != "\\"):
                quoted = not quoted
            elif not quoted and line.startswith("//", i):
                cut = i
                break
        out.append(line[:cut])
    return "\n".join(out)


def _arguments(text: str) -> list:
    """The arguments of a call, split at the commas that belong to it."""
    text = re.sub(r"<[\w\s,.?]*>", "<>", _without_comments(text))
    out, depth, start, quoted = [], 0, 0, False
    for i, ch in enumerate(text):
        if ch == '"' and (i == 0 or text[i - 1] != "\\"):
            quoted = not quoted
        if quoted:
            continue
        if ch in "({[":
            depth += 1
        elif ch in ")}]":
            depth -= 1
        elif ch == "," and depth == 0:
            out.append(text[start:i])
            start = i + 1
    out.append(text[start:])
    return [a.strip() for a in out]


class TestFile:
    def __init__(self, card: Card):
        self.card = card
        self.path = g.test_index().get(card.id)
        if self.path is None:
            raise Left(["it has no test file yet: the generator writes into the file the card has"])
        self.lines, self.eol = _read(self.path)
        start = next((i for i, line in enumerate(self.lines) if SCENARIO_HEAD.match(line)), None)
        if start is None:
            raise Left(["its test file has no GetScenario() method to take a table from"])
        end = next(i for i in range(start, len(self.lines)) if self.lines[i].rstrip() == "\t}")
        body = _without_comments("\n".join(self.lines[start:end + 1]))
        self.aliases = set(re.findall(r'put\(\s*"([^"]+)"', body))
        self.alias = next((a for a, b in re.findall(r'put\(\s*"([^"]+)"\s*,\s*"(\d+_\d+)"\s*\)', body) if b == card.id), None)
        if self.alias is None:
            raise Left(["GetScenario() does not name the card (no put(\"...\", \"%s\"))" % card.id])
        call = re.search(r"new\s+VirtualTableScenario\s*\((.*)\)\s*;", body, re.S)
        if call is None:
            raise Left(["GetScenario() is not a plain `return new VirtualTableScenario(...)`"])
        args = _arguments(call.group(1))
        if len(args) > 5:
            raise Left(["GetScenario() seats a Race to Mount Doom modifier, a Map or a bid handler: a table of its own "
                        "for the play is the author's to lay"])
        self.sites = self._sites(args[1] if len(args) > 1 else "null")
        self.ring_bearer = self._ring_bearer(args[2] if len(args) > 2 else "null")

    def _sites(self, arg: str) -> dict:
        consts = test_constants()
        if arg == "null":
            return dict(consts["KingSites"])
        name = re.match(r"^(?:VirtualTableScenario\.)?(\w+Sites)$", arg)
        if name and isinstance(consts.get(name.group(1)), dict):
            return dict(consts[name.group(1)])
        if arg.startswith("new HashMap"):
            sites = {}
            for base in re.findall(r"putAll\(\s*(?:VirtualTableScenario\.)?(\w+Sites)\s*\)", arg):
                sites.update(consts.get(base, {}))
            sites.update(dict(re.findall(r'put\(\s*"(site\d)"\s*,\s*"(\d+_\d+)"\s*\)', arg)))
            if "site1" in sites and "site2" in sites:
                return sites
        raise Left(["GetScenario()'s site path could not be read (%s): the sites the play stands on are not known" % arg[:40]])

    def _ring_bearer(self, arg: str) -> str:
        consts = test_constants()
        if arg == "null":
            return consts.get("MDFrodo", "10_121")
        literal = re.match(r'^"(\d+_\d+)"$', arg)
        if literal:
            return literal.group(1)
        name = re.match(r"^(?:VirtualTableScenario\.)?(\w+)$", arg)
        if name and isinstance(consts.get(name.group(1)), str):
            return consts[name.group(1)]
        raise Left(["GetScenario()'s Ring-bearer could not be read (%s)" % arg[:40]])

    def reasons(self) -> list:
        """Why this table is not one the plain play can be read on."""
        out = []
        card, cards = self.card, g.card_index()
        bearer = cards.get(self.ring_bearer)
        if card.unique and bearer is not None and _fold(bearer.field("title")) == _fold(card.title):
            out.append("it is unique and shares its title with the scenario's Ring-bearer (%s), so it cannot be played "
                       "beside him: its play wants a table with another Ring-bearer" % g.full_name(bearer))
        under = cards.get(self.sites["site2" if card.shadow else "site1"])
        if under is None:
            out.append("the site the play stands on has no definition")
        elif re.search(r"\btwilight cost\b|\broaming\b", g.plain_text(under.field("gametext", "")), re.I):
            out.append("the site the play stands on, %s, has a text about costs (\"%s\"): its play wants another path"
                       % (g.full_name(under), g.plain_text(under.field("gametext", ""))[:70]))
        return out


def alias_from_title(title: str, taken=()) -> str:
    """A name for the card to go by in its scenario, made from its title: the noun the title turns on (the last
    word before an "of" or its like, or else the last word that is no small word)."""
    words = re.findall(r"[a-z]+", _fold(title).replace("'s ", " ").replace("'", ""))
    cut = next((i for i, w in enumerate(words) if i and w in ("of", "in", "to", "from", "for", "at", "on", "with")), len(words))
    head = [w for w in words[:cut] if w not in STOP] or [w for w in words if w not in STOP] or ["card"]
    name = head[-1]
    if name in JAVA_WORDS or name == "card":
        name = "thecard"
    while name in taken:
        name += "x"
    return name


def variable(alias: str) -> str:
    name = re.sub(r"\W", "", alias)
    if not name or name[0].isdigit() or name in JAVA_WORDS:
        return "played"
    return name[0].lower() + name[1:]


# ----------------------------------------------------------------------------------------------------------------
# The test and the ledger lines
# ----------------------------------------------------------------------------------------------------------------

def number(n: int) -> str:
    return WORDS[n] if 0 <= n < len(WORDS) else str(n)


def test_name(card: Card) -> str:
    cost = number(card.cost)
    if card.type == "companion":
        return "PlayedFromHandFor%sAsACompanion" % cost
    if card.type == "minion":
        if card.roaming:
            return "PlayedFromHandFor%sAndTwoMoreWhileRoaming" % cost
        return "PlayedFromHandFor%sAndNothingMoreWhereItIsNotRoaming" % cost
    return "PlayedFromHandFor%sToTheSupportArea" % cost


def clause_text(card: Card) -> str:
    phase = " in the Shadow phase" if card.shadow and card.type != "minion" else ""
    if card.type == "companion":
        return "Played from hand for (%d), as a companion." % card.cost
    if card.type == "minion":
        if card.roaming:
            return "Played from hand for (%d), and (2) more while roaming, as a minion." % card.cost
        return "Played from hand for (%d), as a minion; its site number is %d, so at site 2 it is not roaming." % (card.cost, card.site)
    return "Played from hand%s for (%d), to the support area." % (phase, card.cost)


def test_method(card: Card, alias: str) -> str:
    v = variable(alias)
    side = "Shadow" if card.shadow else "Freeps"
    zone = {"companion": "FREE_CHARACTERS", "minion": "SHADOW_CHARACTERS"}.get(card.type, "SUPPORT")
    out = ["\t@Test",
           "\tpublic void %s() throws DecisionResultInvalidException, CardNotFoundException {" % test_name(card),
           "\t\tvar scn = GetScenario();",
           "",
           "\t\tvar %s = scn.Get%sCard(\"%s\");" % (v, side, alias),
           "\t\tscn.MoveCardsToHand(%s);" % v,
           "",
           "\t\tscn.StartGame();"]
    if card.shadow:
        total = card.cost + card.roaming
        out += ["\t\tscn.SetTwilight(%d);" % (10 if total <= 10 else 20),
                "\t\tscn.FreepsPass();",
                "",
                "\t\tint twilight = scn.GetTwilight();",
                "\t\tassertEquals(2, scn.GetCurrentSiteNumber());",
                "\t\tassertInZone(Zone.HAND, %s);" % v,
                "\t\tassertTrue(scn.ShadowPlayAvailable(%s));" % v,
                "",
                "\t\tscn.ShadowPlayCard(%s);" % v,
                ""]
        if card.type == "minion" and card.roaming:
            out.append("\t\t// %d to play + 2 roaming: its site number is %d and the fellowship is at site 2" % (card.cost, card.site))
        elif card.type == "minion":
            out.append("\t\t// %d to play and no roaming penalty: its site number is %d and the fellowship is at site 2" % (card.cost, card.site))
        elif card.cost == 0:
            out.append("\t\t// It costs nothing: a cost of 1 would leave one token fewer.")
        out.append("\t\tassertEquals(twilight%s, scn.GetTwilight());" % (" - %d" % total if total else ""))
    else:
        out += ["",
                "\t\tassertInZone(Zone.HAND, %s);" % v,
                "\t\tassertEquals(0, scn.GetTwilight());",
                "\t\tassertTrue(scn.FreepsPlayAvailable(%s));" % v,
                "",
                "\t\tscn.FreepsPlayCard(%s);" % v,
                ""]
        if card.cost == 0:
            out.append("\t\t// It costs nothing: a cost of 1 would read 1.")
        out.append("\t\tassertEquals(%d, scn.GetTwilight());" % card.cost)
    out.append("\t\tassertInZone(Zone.%s, %s);" % (zone, v))
    if card.type in ("condition", "possession", "artifact"):
        # In the support area and on no character: the Ring-bearer is the only character in play.
        out.append("\t\tassertNotAttachedTo(%s, scn.GetRingBearer());" % v)
    out.append("\t\tassertTrue(scn.Awaiting%sPhaseActions());" % ("Shadow" if card.shadow else "Fellowship"))
    out.append("\t}")
    return "\n".join(out)


def ledger_lines(card: Card) -> list:
    return ["P1 Play       [+]          " + clause_text(card), "     +  ->  " + test_name(card)]


def generate(card_id: str, alias=None):
    """(the card, its test file, the name it goes by there, the test method) for a card the generator can do;
    raises Left with the reasons for one it cannot."""
    card = Card(card_id)
    reasons = list(card.reasons)
    file = None
    try:
        file = TestFile(card)
        reasons += file.reasons()
    except Left as left:
        reasons += left.reasons
    if reasons:
        raise Left(reasons)
    name = file.alias
    if name == "card":
        name = alias or alias_from_title(card.title, file.aliases)
    return card, file, name, test_method(card, name)


# ----------------------------------------------------------------------------------------------------------------
# Writing: the test into the file, the clause and its claim into the ledger
# ----------------------------------------------------------------------------------------------------------------

def _method_span(lines, name):
    """(first line, last line) of the method of that name with the annotations and comments that stand on it, or None."""
    at = next((i for i, line in enumerate(lines) if re.match(r"^\t(?://\s*)?public void %s\s*\(" % re.escape(name), line)), None)
    if at is None:
        return None
    start = at
    while start > 0 and re.match(r"^\t(@|//)", lines[start - 1]):
        start -= 1
    end = next(i for i in range(at, len(lines)) if lines[i].rstrip() == "\t}")
    return start, end


def write_test(file: TestFile, card: Card, alias: str, method: str) -> list:
    """Puts the test into the card's file and returns what was done, one line each."""
    lines, did = list(file.lines), []
    if file.alias != alias:
        # The skeleton's "card": lint refuses the name, and the stats test reads the card by it.
        lines = [re.sub(r'"%s"' % re.escape(file.alias), '"%s"' % alias, line) if re.search(r'\b(?:put|Get\w+Card\w*)\(\s*"%s"' % re.escape(file.alias), line) else line
                 for line in lines]
        did.append('the card is now called "%s" in the file (it was "%s")' % (alias, file.alias))
    # The skeleton's placeholder is this very test, commented out: it gives way to the real one.
    note = next((i for i, line in enumerate(lines) if SKELETON_NOTE in line), None)
    if note is not None:
        end = next((i for i in range(note, len(lines)) if lines[i].rstrip() == "\t}"), None)
        if end is not None and any(re.match(r"^\t//\s*@Test\b", line) for line in lines[note:end]):
            while note > 0 and not lines[note - 1].strip():
                note -= 1
            del lines[note:end + 1]
            did.append("the skeleton's commented-out placeholder test is taken out")
    name = test_name(card)
    span = _method_span(lines, name)
    if span is not None:
        if "\n".join(lines[span[0]:span[1] + 1]) != method:
            raise Left(["its file already has a test named %s that is not this one word for word: nothing is written over "
                        "a test (print without --write to compare; take the old one out to have this one)" % name])
        did.append("%s is already in the file, word for word" % name)
    else:
        stats = next((i for i, line in enumerate(lines) if re.match(r"^\tpublic void \w+Stats(?:AndKeywords)?AreCorrect\s*\(", line)), None)
        if stats is None:
            raise Left(["its test file has no stats test to put the Play test after"])
        end = next(i for i in range(stats, len(lines)) if lines[i].rstrip() == "\t}")
        lines[end + 1:end + 1] = [""] + method.split("\n")
        did.append("%s is put after the stats test" % name)
    text = "\n".join(lines)
    wanted = [("import static com.gempukku.lotro.framework.Assertions.*;", r"import static com\.gempukku\.lotro\.framework\.Assertions\.\*;"),
              ("import com.gempukku.lotro.common.*;", r"import com\.gempukku\.lotro\.common\.(?:\*|Zone);")]
    for line, there in wanted:
        if not re.search(there, text):
            last = max(i for i, x in enumerate(lines) if x.startswith("import "))
            static = line.startswith("import static")
            anchor = next((i for i, x in enumerate(lines) if x.startswith("import static ")), None) if static else None
            lines.insert(anchor if anchor is not None else (last + 1), line)
            text = "\n".join(lines)
            did.append("added `%s`" % line)
    _write(file.path, lines, file.eol)
    return did


def ledger_state(card: Card):
    """(the ledger's lines or None, the index of its P1 clause or None, the tests that claim P1)."""
    path = g.ledger_path(card.id)
    if not path.is_file():
        return None, None, []
    lines, _ = _read(path)
    at = next((i for i, line in enumerate(lines) if re.match(r"^P1\s+Play\b", line)), None)
    claimants = []
    if at is not None:
        for line in lines[at + 1:]:
            if line.strip() and not line.startswith((" ", "\t")):
                break
            m = re.match(r"^\s+\S.*->\s*(\w+)\s*$", line)
            if m:
                claimants.append(m.group(1))
    return lines, at, claimants


def write_ledger(card: Card) -> list:
    path = g.ledger_path(card.id)
    lines, at, claimants = ledger_state(card)
    name, did = test_name(card), []
    clause, claim = ledger_lines(card)
    if lines is None:
        path.parent.mkdir(parents=True, exist_ok=True)
        lines = ["LEDGER  %s  %s" % (card.id, card.name), "Sources:", "", clause, claim, ""]
        did.append("the ledger is begun with P1 and its claim (%s): Sources and the C clauses are the author's" % path)
    else:
        def clause_end(i):
            """The line after a clause line and the lines of text that run on from it."""
            i += 1
            while i < len(lines) and lines[i].startswith((" ", "\t")) and lines[i].strip() and "->" not in lines[i] \
                    and not re.match(r"^\s+(?:no|waits)\s", lines[i]):
                i += 1
            return i
        if at is None:
            first = next((i for i, line in enumerate(lines) if re.match(r"^[A-Z]\d+\s+[A-Za-z]+\s+\[", line)), None)
            if first is None:
                while lines and not lines[-1].strip():
                    lines.pop()
                lines += ["", clause, claim, ""]
            else:
                lines[first:first] = [clause, claim]
            did.append("P1 and its claim are put into the ledger")
        elif name not in claimants:
            lines.insert(clause_end(at), claim)
            did.append("the claim is put under the ledger's P1")
        # "Plays to your support area." is a clause of the game text, of this type, and this test's to claim.
        for i in range(len(lines) - 1, -1, -1):
            if re.match(r"^C\d+\s+Play\s+\[", lines[i]):
                end = clause_end(i)
                under = []
                for line in lines[end:]:
                    if line.strip() and not line.startswith((" ", "\t")):
                        break
                    under.append(line)
                if not any(re.search(r"->\s*%s\s*$" % re.escape(name), line) for line in under):
                    lines.insert(end, claim)
                    did.append("the claim is put under %s as well" % lines[i].split()[0])
    _write(path, lines, _read(path)[1] if path.is_file() else "\n")
    return did


# ----------------------------------------------------------------------------------------------------------------

def census(ids) -> int:
    import collections
    written, left, by_type = [], collections.Counter(), collections.Counter()
    for card_id in ids:
        d = g.card_index().get(card_id)
        if d is None or _fold(d.field("type")) not in PLAYED:
            continue
        kind = _fold(d.field("type"))
        try:
            generate(card_id)
            written.append(card_id)
            by_type[(kind, "written")] += 1
        except Left as why:
            by_type[(kind, "left")] += 1
            for reason in why.reasons:
                left[re.sub(r"\(.*?\)|\".*?\"|: .*$", "", reason).strip()[:90]] += 1
    total = len(written) + sum(n for (k, what), n in by_type.items() if what == "left")
    print("%d cards that are played from hand: %d written, %d left to their authors" % (total, len(written), total - len(written)))
    for kind in sorted(set(k for k, _ in by_type)):
        print("  %-11s %4d written  %4d left" % (kind, by_type[(kind, "written")], by_type[(kind, "left")]))
    print("Why a card is left (a card may have several reasons):")
    for reason, n in left.most_common():
        print("  %4d  %s" % (n, reason))
    return 0


def main(argv) -> int:
    args = list(argv)
    if not args or "-h" in args or "--help" in args:
        print(__doc__)
        return 0 if args else 1
    write = "--write" in args
    alias = None
    if "--alias" in args:
        at = args.index("--alias")
        if at + 1 >= len(args) or not re.match(r"^[a-z][A-Za-z0-9]*$", args[at + 1]):
            print("--alias wants a name: a lower-case letter, then letters and digits", file=sys.stderr)
            return 1
        alias = args[at + 1]
        del args[at:at + 2]
    if "--census" in args:
        rest = [a for a in args if not a.startswith("--")]
        return census(g.resolve_ids(rest or ["seg:decipher"]))
    ids = g.resolve_ids([a for a in args if not a.startswith("--")])
    if not ids:
        print("No card given.", file=sys.stderr)
        return 1
    if alias and len(ids) > 1:
        print("--alias names one card: give one id with it", file=sys.stderr)
        return 1
    status = 0
    for card_id in ids:
        try:
            card, file, name, method = generate(card_id, alias)
            did = []
            if write:
                _, at, claimants = ledger_state(card)
                others = [c for c in claimants if c != test_name(card)]
                if others:
                    raise Left(["its ledger already gives P1 to %s: the card has its Play test (print without --write to "
                                "compare the two)" % ", ".join(others)])
                did = write_test(file, card, name, method) + write_ledger(card)
        except Left as why:
            status = 3
            print("%s  %s: left to the author" % (card_id, g.full_name(g.card_index()[card_id]) if card_id in g.card_index() else "?"))
            for reason in why.reasons:
                print("  - " + reason)
            if len(ids) > 1:
                print()
            continue
        source = "its sheet row" if card.from_sheet else "its definition's printed fields (no sheet has this card)"
        if write:
            print("%s  %s: written from %s" % (card_id, card.name, source))
            for line in did:
                print("  - " + line)
            print("  - test file: " + g.rel(file.path))
        else:
            print("// %s  %s: from %s; test file %s" % (card_id, card.name, source, g.rel(file.path)))
            if file.alias != name:
                print('// the file still calls the card "%s": --write renames it "%s"' % (file.alias, name))
            print(method)
            print()
            print("\n".join(ledger_lines(card)))
        if card.support_sentence and not any(re.match(r"^C\d+\s+Play\s+\[", line) for line in (ledger_state(card)[0] or [])):
            print(("  - " if write else "// ") + 'the game text has "Plays to your support area.": that sentence is a C clause of '
                  "type Play, and this test claims its + as well (`     +  ->  %s` under it)" % test_name(card))
        if len(ids) > 1:
            print()
    return status


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
