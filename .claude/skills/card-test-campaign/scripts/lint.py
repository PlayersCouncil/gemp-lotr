#!/usr/bin/env python3
"""Checks card test files against the Card Test Standard (see the test-generation skill).

    lint.py <what...>               what is a blueprint id, a test file, or a selector (set:1, seg:vset, ...)
    lint.py <what...> --quiet       one line per file
    lint.py --summary <what...>     counts only, by rule
    lint.py --form <file.java> [--only <Test,Test>]
                                    only the rules about how a test is written (bookend, reach, exception, adhoc,
                                    assert, ignore, style, helper, field, scenario, in-file, alias, same-blueprint,
                                    conditional, not-asked, not-in-hand, copies, stale, item-class), for a
                                    mechanics class or a card's file that has no ledger yet; with --only, only
                                    what lies inside the tests named (a lone test added to an old file)

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
    "Play": {"+"},  # P1, kept only on the cards of plainplay-keep.tsv (Q-0279: B)
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

# What a clause does that lasts: a strength or keyword made, gained or lost (the `duration` warning).
LASTING_CHANGE = re.compile(r"\bmakes?\b[^.]*?\b(?:strength|vitality|resistance) [+-]|\bmakes?\b[^.]*?\b(?:fierce|damage \+|"
                            r"an archer|defender \+)|\b(?:gains?|loses?)\b[^.]*?\b(?:fierce|damage|archer|ambush|hunter|bonus|"
                            r"bonuses|keywords?|game text)\b|\b(?:is|are) strength [+-]", re.I)

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


# A special ability that plays another card from the discard pile ("Shadow: Play ...", "... to play ..."): the play
# is a cost wherever it stands (Q-0062), so its `C-` is a board where nothing there can be paid for (5_95, 9_36).
PILE_PLAY = re.compile(r"(?:\bto|:)\s+play\b(?!\s+(?:this|it)\b)[^.]*?\bfrom your discard pile\b", re.I)
# A clause whose whole effect is a draw, with nothing paid for it: once the Rule of 4 is spent in its player's
# fellowship phase it is not offered (Q-0262: B).
DRAW_ONLY = re.compile(r"(?:^|[.,:]\s+)(?:you may )?draw (?:a|one|two|three|\d) cards?\.?$", re.I)
# A bar the card lays on itself: "This minion may not ...", "<its title> cannot ..." at a sentence's start.
SELF_BAR = r"(?:^|[.:]\s+)(?:this (?:minion|companion|ally|character|follower)|%s)\s+(?:may not|cannot|can't|is not|are not)\b"
# A board change by the rig.  A pending decision is not worked out again after one (pitfalls, "A board changed under
# a pending decision"), so an offer read before the next answer or refresh is the offer as it stood before it.
CHEAT = re.compile(r"\bscn\s*\.\s*(?:Move\w*To\w*|AttachCardsTo|StackCardsOn|AddWoundsToChar|RemoveWoundsFromChar|"
                   r"AddTokensToCard|RemoveTokensFromCard|AddBurdens|RemoveBurdens|AddThreats|RemoveThreats|SetTwilight|"
                   r"Set\w*Hand|Set\w*DrawDeck|ApplyAdHoc\w*|HinderCard\w*|RestoreCard\w*|\w*TakeControlOfSite)\s*\(")
# What works a pending choice of phase actions out afresh: the refresh itself, and (since engine pass 40, G-0060)
# the cheats that end with one (SetHand, SetDrawDeck and MoveCardsToDiscard, with their twins and by-name variants).
REFRESH = re.compile(r"Refresh(?:Decision|PendingPhaseActions)\s*\(|\bscn\s*\.\s*(?:Set(?:Freeps|Shadow)?(?:Hand|DrawDeck)|"
                     r"MoveCards(?:To|ToFreeps|ToShadow)Discard)\s*\(")

# How the R clause for a site left behind speaks: by the rule it stands on, or by where the fellowship then is.
SITE_LEFT = re.compile(r"unless the fellowship is at that site|behind the fellowship|left behind|CR 4\.2 \"active\"|"
                       r"Legacy Ruling 3\b|\bLR 3\b", re.I)


# A sentence that opens "If <a noun phrase> is ..." or "... are ...": a condition on the state of the thing named
# ("If the Ring-bearer is then exhausted", "If bearer is Boromir"), not on what a player does ("If you ...").
IF_IS = re.compile(r"(?:^|[.:]\s+)(?:Then,?\s+)?If\s+(?!you\b|there\b|no\b)([^.,:;()]{1,60}?)\s+(is|are)\b")
# Where such words stand between "If" and "is", what stands there is a sentence of its own and no noun phrase
# ("If Shelob wins a skirmish and there are 4 minions ...").
_A_VERB = re.compile(r"\b(?:wins?|loses?|plays?|moves?|takes?|exerts?|dies?|kills?|has|have|bears?|can|cannot|spots?|"
                     r"discards?|adds?|removes?|heals?|uses?|reveals?|draws?|would|does|do|and)\b", re.I)


def unowned_condition(kind: str, text: str):
    """The (noun phrase, verb) of a clause's "If <a noun phrase> is ..." sentence, or None.  A Trigger's or a
    Response's "If" opens the event it answers, whose nearest miss is that clause's `-`, so those are left out."""
    if kind in ("Trigger", "Response"):
        return None
    for m in IF_IS.finditer(text):
        if not _A_VERB.search(m.group(1)):
            return m.group(1), m.group(2)
    return None


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


# ----------------------------------------------------------------------------------------------------------------
# The play of a card (Q-0279: C): what a test on a bare table was the only thing to notice
# ----------------------------------------------------------------------------------------------------------------

def bears_on_play(card_id) -> list:
    """What, by gen_play.py's own reading, a card's definition and text have that bears on its play from hand (a
    requirement, a cost, a discount, a trigger on its own play, a bearer, a place its type does not give it), one
    short phrase each, or [] for a plain play.  Only a card of a type that is played from hand and stays in play is
    read.  The generator's reasons that are about its own table (a Ring-bearer of the card's title, the site under
    the play) or about a sheet to settle are not counted: they say nothing of the card."""
    import gen_play
    try:
        card = gen_play.Card(card_id)
    except gen_play.Left:
        return []
    return [reason.split(":")[0].strip() for reason in card.bears_on_play]


def plays_from_hand(lines, card_id, without=()) -> list:
    """The tests of a card's file, live and not among `without`, that play a card of that blueprint from hand by
    legal play (`FreepsPlayCard` or `ShadowPlayCard` of it, by its variable or inline by its alias)."""
    raw = "\n".join(lines)
    aliases = [a for a, b in re.findall(r'put\(\s*"([^"]+)"\s*,\s*"(\d+_\d+)"\s*\)', raw) if b == card_id]
    if not aliases:
        return []
    named = "|".join(re.escape(a) for a in aliases)
    methods, code = split_methods(lines)
    out = []
    for name, start, end, above, ret in methods:
        marks = [t for _, t in above]
        if name in without or not any(t.startswith("@Test") for t in marks) or any(re.search(r"@Ignore\b", t) for t in marks):
            continue
        body = " ".join(text for _, text in java_statements(lines[start:end + 1]))
        variables = re.findall(r'\b(\w+)\s*=\s*\w+\s*\.\s*Get(?:Freeps|Shadow)Card\(\s*"(?:%s)"\s*\)' % named, body)
        played = r'\b(?:Freeps|Shadow)PlayCard\(\s*(?:%s\w+\s*\.\s*Get(?:Freeps|Shadow)Card\(\s*"(?:%s)"\s*\))\s*\)' \
                 % ("".join(re.escape(v) + "|" for v in dict.fromkeys(variables)), named)
        if re.search(played, body):
            out.append(name)
    return out


# ----------------------------------------------------------------------------------------------------------------
# Copies of one title in play (Q-0239): as far as a test's cheats can be followed without running it
# ----------------------------------------------------------------------------------------------------------------

# The zones of Zone that are in play.
_IN_PLAY_ZONES = {"FREE_CHARACTERS", "SHADOW_CHARACTERS", "SUPPORT", "ATTACHED"}
# The cheats that put cards into play, with whose card a name given as a string is (None: cards only).
_INTO_PLAY = [(re.compile(r"^Move(?:Companions?|All(?:y|ies))ToTable$"), "Freeps"), (re.compile(r"^MoveMinions?ToTable$"), "Shadow"),
              (re.compile(r"^MoveFreepsCardsToSupportArea$"), "Freeps"), (re.compile(r"^MoveShadowCardsToSupportArea$"), "Shadow"),
              (re.compile(r"^MoveCardsToSupportArea$"), None)]
# The cheats that take cards out of play, likewise.
_OUT_OF_PLAY = [(re.compile(r"^MoveCards?To(?:Hand|Discard|DeadPile|TopOfDeck|BottomOfDeck|AdventureDeck)$|^RemoveCardZone$|^Set(?:Freeps|Shadow)Hand$"), None),
                (re.compile(r"^MoveCardsTo(?:TopOf|BottomOf)?Freeps(?:Hand|Discard|Deck)$"), "Freeps"),
                (re.compile(r"^MoveCardsTo(?:TopOf|BottomOf)?Shadow(?:Hand|Discard|Deck)$"), "Shadow")]
COPIES_WORDS = {1: "one dot: the title is unique", 2: "two dots", 3: "three dots"}


def dots(card):
    """How many cards of its title a card's dots allow in play: 1 for a unique card, 2 or 3 for a card of two or
    three dots, None for one that is not unique."""
    value = (card.field("unique", "") or "").strip().lower()
    return {"true": 1, "1": 1, "2": 2, "3": 3}.get(value)


def java_statements(lines):
    """(index of the line it starts on, its text) for each statement of a stretch of Java: comments taken out,
    cut at the semicolons that are not inside a string."""
    out, text, at, quoted = [], [], None, False
    for n, line in enumerate(lines):
        i = 0
        while i < len(line):
            c = line[i]
            if quoted:
                text.append(c)
                if c == "\\" and i + 1 < len(line):
                    text.append(line[i + 1])
                    i += 1
                elif c == '"':
                    quoted = False
            elif line.startswith("//", i):
                break
            elif c == ";":
                if at is not None:
                    out.append((at, "".join(text)))
                text, at = [], None
            else:
                if at is None and not c.isspace():
                    at = n
                if c == '"':
                    quoted = True
                text.append(c)
            i += 1
        text.append(" ")
    return out


def call_arguments(text: str, opens: int) -> list:
    """The arguments of the call whose bracket opens at text[opens], split at the commas that are its own."""
    out, depth, start, quoted = [], 0, opens + 1, False
    for i in range(opens, len(text)):
        c = text[i]
        if c == '"' and text[i - 1] != "\\":
            quoted = not quoted
        if quoted:
            continue
        if c in "({[":
            depth += 1
        elif c in ")}]":
            depth -= 1
            if depth == 0:
                out.append(text[start:i])
                break
        elif c == "," and depth == 1:
            out.append(text[start:i])
            start = i + 1
    return [a.strip() for a in out if a.strip()]


def scenario_tables(lines, methods) -> dict:
    """For each Get...Scenario() method of a test file, by its name: ({alias: blueprint id} of its deck, the
    blueprint id of its Ring-bearer or None where that cannot be read).  A method that hands on another's table
    (`GetBattlegroundScenario()` returning `GetScenario(sites)`; a `GetScenario()` returning `GetScenario(format,
    ringBearer)`) is read through to the one that builds it: that one's deck with this one's own cards over it, and
    the Ring-bearer the builder names or, where the builder takes him as a parameter, the one passed down to it.
    Of several methods of one name, the one a test calls is taken to be the one with no parameters."""
    import gen_play
    consts = gen_play.test_constants()
    found = {}  # name -> [(parameter names, own cards, the builder's Ring-bearer argument or None with no builder, [(scenario method called, arguments)])]
    for name, start, end, above, ret in methods:
        if not re.match(r"^Get\w*Scenario\w*$", name):
            continue
        body = " ".join(text for _, text in java_statements(lines[start:end + 1]))
        while True:  # type arguments have commas of their own: new HashMap<String, String>() {{ ... }}
            bare = re.sub(r"<[\w\s,.?\[\]]*>", "", body)
            if bare == body:
                break
            body = bare
        signed = re.search(r"\b%s\s*\(([^)]*)\)" % re.escape(name), body)
        params = [x.split()[-1] for x in signed.group(1).split(",") if x.split()] if signed else []
        inside = body[signed.end():] if signed else body
        bearer_arg = None
        built = re.search(r"\bnew\s+VirtualTableScenario\s*\(", inside)
        if built:
            args = call_arguments(inside, built.end() - 1)
            bearer_arg = args[2] if len(args) > 2 else "null"
        calls = [(call.group(1), call_arguments(inside, call.end() - 1)) for call in re.finditer(r"\b(Get\w*Scenario\w*)\s*\(", inside)]
        found.setdefault(name, []).append((params, dict(re.findall(r'put\(\s*"([^"]+)"\s*,\s*"(\d+_\d+)"\s*\)', inside)), bearer_arg, calls))

    def value(arg, passed):
        """The blueprint id an argument stands for: a literal, one of the rig's constants, or what was passed down."""
        literal = re.match(r'^"(\d+_\d+)"$', arg)
        named = re.match(r"^(?:VirtualTableScenario\.)?(\w+)$", arg)
        if arg == "null":
            return consts.get("MDFrodo")
        if literal:
            return literal.group(1)
        if named and named.group(1) in passed:
            return passed[named.group(1)]
        if named and isinstance(consts.get(named.group(1)), str):
            return consts[named.group(1)]
        return None

    def table(method, passed, depth=0):
        params, cards, bearer_arg, calls = method
        if bearer_arg is not None:
            return dict(cards), value(bearer_arg, passed)
        deck, bearer = {}, None
        for callee, args in calls if depth < 6 else []:
            takers = [m for m in found.get(callee, []) if m is not method and len(m[0]) == len(args)]
            if takers:
                below, bearer = table(takers[0], {param: value(arg, passed) for param, arg in zip(takers[0][0], args)}, depth + 1)
                deck.update(below)
        deck.update(cards)
        return deck, bearer

    return {name: table(min(same, key=lambda m: len(m[0])), {}) for name, same in found.items()}


def copies_over_the_dots(test_lines, tables) -> list:
    """Where a test cheats into play more copies of one title, for one player, than the title's dots allow (CR 5.0
    "non-unique"; Ketura on Q-0239: "no cheating in more copies of a card than their uniqueness level allows"):
    (line offset, the alias that tips it over, the title, the copies then in play, the dots) for each title, once.
    A card is followed by the variable or the alias it is fetched by, into play by the cheats that put it there and
    by a play, out again by the cheats that take it away and by the test's own reading of it in a hand or a discard
    pile.  A play the game allowed is taken as the game's word that the copies before it had room for one more.
    What the game takes out of play and the test never reads is not seen."""
    cards = g.card_index()
    out, said = [], set()
    scenes, fetched = {}, {}  # scenario variable -> [deck, bearer, {(owner, alias): True} in play]; card variable -> (scenario variable, owner, alias)

    def restriction(card_id):
        card = cards.get(card_id)
        if card is None or (card.field("type", "") or "").strip().lower() not in PLAYED:
            return None, None
        return g.sanitized_title(card.field("title", "")), dots(card)

    def tokens(scene, args, owner_of_names):
        found = []
        for arg in args:
            inline = re.match(r'^(\w+)\s*\.\s*Get(Freeps|Shadow)Card\(\s*"([^"]+)"\s*\)$', arg)
            name = re.match(r'^"([^"]+)"$', arg)
            if inline and inline.group(1) == scene:
                found.append((inline.group(2), inline.group(3)))
            elif name and owner_of_names:
                found.append((owner_of_names, name.group(1)))
            elif arg in fetched and fetched[arg][0] == scene:
                found.append(fetched[arg][1:])
        return found

    def copies_of(scene, owner, title):
        deck, bearer, in_play = scenes[scene]
        there = [(token, restriction(deck.get(token[1]))[1]) for token in in_play
                 if token[0] == owner and restriction(deck.get(token[1]))[0] == title]
        if bearer and restriction(bearer)[0] == title:
            there.insert(0, ((owner, "the Ring-bearer"), restriction(bearer)[1]))
        return there

    def over(copies) -> bool:
        # The board is reached by playing the most restricted first: the k-th card played finds k copies there.
        limits = sorted(limit for _, limit in copies if limit is not None)
        return any(k >= limit for k, limit in enumerate(limits))

    for at, text in java_statements(test_lines):
        got = re.search(r"\b(\w+)\s*=\s*(Get\w*Scenario\w*)\s*\(", text)
        if got and got.group(2) in tables:
            deck, bearer = tables[got.group(2)]
            scenes[got.group(1)] = [deck, bearer, {}]
            continue
        card = re.search(r'\b(\w+)\s*=\s*(\w+)\s*\.\s*Get(Freeps|Shadow)Card\(\s*"([^"]+)"\s*\)\s*$', text)
        if card and card.group(2) in scenes:
            fetched[card.group(1)] = (card.group(2), card.group(3), card.group(4))
            continue
        # The test's own word that a card is no longer in play: read in a hand, a discard pile or another such zone.
        for read in re.finditer(r"\b(assertInZone|assertInDiscard|assertInHand)\s*\(", text):
            args = call_arguments(text, read.end() - 1)
            if read.group(1) == "assertInZone":
                if not args or args[0].split(".")[-1] in _IN_PLAY_ZONES:
                    continue
                args = args[1:]
            for arg in args:
                if arg in fetched and fetched[arg][0] in scenes:
                    scenes[fetched[arg][0]][2].pop(fetched[arg][1:], None)
        for call in re.finditer(r"\b(\w+)\s*\.\s*(\w+)\s*\(", text):
            scene, method = call.group(1), call.group(2)
            if scene not in scenes:
                continue
            deck, bearer, in_play = scenes[scene]
            args = call_arguments(text, call.end() - 1)
            coming, going, played = [], [], False
            for pattern, owner in _INTO_PLAY:
                if pattern.match(method):
                    coming = tokens(scene, args, owner)
            for pattern, owner in _OUT_OF_PLAY:
                if pattern.match(method):
                    going = tokens(scene, args, owner)
            if method == "AttachCardsTo":
                coming = tokens(scene, args[1:], None)
            elif method in ("StackCardsOn", "SetHand"):
                going = tokens(scene, args[1:], None)
            elif method == "MoveCardToZone" and len(args) == 2:
                zone = args[1].split(".")[-1]
                coming, going = (tokens(scene, args[:1], None), []) if zone in _IN_PLAY_ZONES else ([], tokens(scene, args[:1], None))
            elif method in ("FreepsPlayCard", "ShadowPlayCard"):
                coming, played = tokens(scene, args[:1], method[:6]), True
            for token in going:
                in_play.pop(token, None)
            for token in coming:
                title, limit = restriction(deck.get(token[1]))
                if title is None or token in in_play:
                    continue
                in_play[token] = True
                while played and over(copies_of(scene, token[0], title)):
                    # The game let the card be played, so an earlier copy has left play unseen: the oldest goes.
                    oldest = next((t for t, _ in copies_of(scene, token[0], title) if t in in_play and t != token), None)
                    if oldest is None:
                        break
                    del in_play[oldest]
                there = copies_of(scene, token[0], title)
                if not played and over(there) and (scene, token[0], title) not in said:
                    said.add((scene, token[0], title))
                    out.append((at, token[1], g.plain_text(cards[deck[token[1]]].field("title", "")),
                                [t[1] for t, _ in there], min(limit for _, limit in there if limit is not None)))
    return out


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


_both_sides = None


def races_on_both_sides() -> list:
    """The races that cards of both sides have (Man, Hobbit, Wizard, Wraith), as a regular expression's words each:
    a bare race word in a text ("Bearer must be a Wizard") names no side (Q-0058), and for these the opponent's card
    of that race is a candidate a board can hold."""
    global _both_sides
    if _both_sides is None:
        sides = {}
        for cid, c in g.card_index().items():
            race, side = (c.field("race", "") or "").strip().lower(), (c.field("side", "") or "").strip().lower()
            if race and side:
                sides.setdefault(race, set()).add(side)
        _both_sides = sorted(r for r, s in sides.items() if len(s) > 1)
    return _both_sides


def race_word(race: str) -> str:
    return {"man": "Man|Men", "elf": "Elf|Elves", "dwarf": "Dwarf|Dwarves"}.get(race, re.escape(race.capitalize()) + "s?")


def item_classes(card) -> list:
    """The item classes printed on a card, in lower case."""
    m = re.search(r"^\s*itemclass\s*:\s*(\[.*?\]|[^\n\[]*)", card.raw, re.S | re.M | re.I) if card is not None else None
    return [w.strip().lower() for w in re.split(r"[\n,\[\]]", m.group(1)) if w.strip()] if m else []


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


def ledger_text_gap(card, clauses) -> str:
    """Where the C clauses part from the game text: the first sentence of the text they do not hold as it stands,
    and a clause that is not a C clause holding text of the card's (a keyword written `K1`)."""
    text = g.plain_text(card.field("gametext", "") or "")
    c_clauses = sorted((c for cid, c in clauses.items() if cid.startswith("C")), key=lambda c: c[3])
    have = squash(" ".join(c[2] for c in c_clauses))
    sentences = [x for x in re.split(r"(?<=[.!?])\s+", text) if x.strip()]
    said, at = [], 0
    for sentence in sentences:
        piece = squash(sentence)
        if have[at:at + len(piece)] == piece:
            at += len(piece)
            continue
        said.append("the first sentence they do not hold, in order, is \"%s\"" % sentence.strip())
        break
    if not said and at < len(have):
        said.append("they hold more than the text: \"%s\"" % have[at:at + 60])
    stray = [cid for cid, c in clauses.items() if not cid.startswith("C") and squash(c[2]) and squash(c[2]) in squash(text)]
    if stray:
        said.append("%s %s text of the card's, and only C clauses hold the game text (a keyword line is a C clause "
                    "typed Keyword)" % (" and ".join(sorted(stray)), "holds" if len(stray) == 1 else "hold"))
    return "; ".join(said) if said else "the difference is in punctuation or spacing the check does not show"


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
    # A possession or artifact that prints no item class reads null (the Standard, "The stats test"; 3_106, 1_55):
    # otherwise a class written into its definition passes every test.
    if card is not None and (card.field("type", "") or "").strip().lower() in ("possession", "artifact") and not item_classes(card):
        for name, start, end, above, ret in stats:
            if not any(re.search(r"\bgetPossessionClasses\s*\(", line) for line in code[start:end + 1]):
                add("error", "stats", start, "%s: the card prints no item class, and the test never reads "
                                             "assertNull(card.getBlueprint().getPossessionClasses())" % name)
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
    card_type = (card.field("type", "") or "").strip().lower() if card is not None else ""
    clauses, claims, cited, faults = parse_ledger(ledger_file) if ledger_file else (None, None, [], [])
    ledger_text = ledger_file.read_text(encoding="utf-8") if ledger_file is not None and ledger_file.is_file() else ""
    notes_text = ledger_text.partition("\nNotes:")[2]
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
                    # Always payable while the card is in play to be used: the Standard asks for no line (9_35 had one).
                    add("warning", "waived", None, "%s: a `no C-` line under a clause whose only cost is the card discarding "
                        "itself, which the Standard no longer asks for: take the line out (shapes.md, \"lint's `title-kinds` "
                        "warning, and a `no C-` it passes over\")" % cid)
                else:
                    add("warning", "waived", None, "%s: no %s, with a reason given in the ledger (for the reviewer to read)" % (cid, check))
            missing = CORE[ctype] - codes - waived
            if missing:
                add("error", "ledger", None, "%s: a %s clause must list at least %s" % (cid, ctype, " ".join(sorted(CORE[ctype]))))
            # A condition on the state of a thing named owes the thing's nearest neighbour that is in that state
            # (2_79: `ringBearer,exhausted` written `companion,exhausted` passed every test until an exhausted
            # Aragorn stood beside Frodo).  The Standard owes the candidate; this says where none is listed.
            named = unowned_condition(ctype, text) if cid.startswith("C") else None
            if named and "F" not in codes and "F" not in waived and "F" not in waived.waits:
                add("warning", "if-is", None, "%s: \"If %s %s ...\" is a condition on the state of what it names, and the clause "
                    "lists no F: the nearest invalid candidate is another card in that state while the one named is not "
                    "(the Standard, \"A condition inside a modifier; 'bearer' or 'self' with a requirement\"; 2_79, an "
                    "exhausted companion beside a Ring-bearer who is not), in a test that claims F, or a `no F:` line "
                    "under the clause says why none can stand" % (cid, named[0], named[1]))
            # A phase ability, a trigger's or an event's effect that changes something and names no duration has
            # one by rule (CR 4.2 "phase action"; Q-0018): the Standard's `D` (1_298's "make a Hobbit strength +2"
            # on a Skirmish event, and I Would Have Gone With You to the End 101_23 went without one).
            if cid.startswith("C") and "D" not in codes and "D" not in waived and "D" not in waived.waits \
                    and (ctype in ("Activated", "Trigger", "Response", "Event") or (ctype == "Effect" and card_type == "event")) \
                    and LASTING_CHANGE.search(text) and not re.search(r"\buntil\b", text, re.I):
                add("warning", "duration", None, "%s: the clause changes something and names no duration, so it lasts to "
                    "the end of the phase by rule (CR 4.2 \"phase action\"; Q-0018): a `D` reads it in force at the "
                    "phase's last decision and gone in the next (each skirmish is a phase), or a `no D:` line says why "
                    "none can be shown" % cid)
            plain = re.sub(r"\s+", " ", text)
            # A bare race word that cards of both sides have names no side (Q-0058): the opponent's card of that race
            # is a candidate, or the Notes say why none can stand (9_8's "Bearer must be a Wizard").
            if cid.startswith("C") and (ctype == "PlayOn" or "F" in codes) and card is not None:
                races = "|".join(race_word(r) for r in races_on_both_sides())
                bare = re.search(r"(?<!your )(?<!\] )(?<!Peoples )(?<!Shadow )\b(?:an?|each|every|any|another)\s+"
                                 r"(?:exhausted\s+|wounded\s+)?(%s)\b(?!\s+(?:companion|ally|allies|minion|follower)s?\b)" % races,
                                 plain) if races else None
                if bare and not re.search(r"Q-0058|opponent|other player|either side|both sides", notes_text, re.I):
                    add("warning", "race-side", None, "%s: \"%s\" names no side (Q-0058), and cards of both sides are %s: the "
                        "opponent's card of that race is a candidate (an F test, or a Notes line saying why no board can "
                        "hold one)" % (cid, bare.group(0), bare.group(1)))
            # A play from the discard pile is a cost wherever it is written (Q-0062): its `C-` is a board where
            # nothing there can be paid for (5_95, whose only board was a card of another culture).
            if cid.startswith("C") and ctype == "Activated" and PILE_PLAY.search(plain) \
                    and "C-" not in codes and "C-" not in waived and "C-" not in waived.waits:
                add("warning", "pile-cost", None, "%s: the clause plays a card from the discard pile, a cost wherever it is "
                    "written (Q-0062), and lists no C-: the board where nothing there can be paid for (shapes.md, Q-0086), "
                    "or a `no C-:` line saying why none can stand" % cid)
            # A no-cost offer whose whole effect is a draw is not made once the Rule of 4 is spent (Q-0262: B).
            if cid.startswith("C") and card_type and (card.field("side", "") or "").strip().lower() != "shadow" \
                    and ((ctype == "Trigger" and re.search(r"\byou may\b", plain, re.I)) or
                         (ctype == "Activated" and re.match(r"(?:Fellowship|[A-Z][a-z]+ or Fellowship|Fellowship or [A-Z][a-z]+): Draw\b", plain)))\
                    and DRAW_ONLY.search(plain) \
                    and not re.search(r"\b(?:shadow|maneuver|archery|assignment|skirmish|regroup) phases?\b", plain, re.I) \
                    and not re.search(r"Rule of 4|Q-0262", ledger_text):
                add("warning", "rule-of-4", None, "%s: the clause's whole effect is a draw, which may come in its player's "
                    "fellowship phase: once the Rule of 4 is spent there it is not offered (Q-0262: B; pitfalls, \"The Rule of "
                    "4\"), a `-` board the ledger does not mention" % cid)
            # A bar the card lays on itself owes another card of its kind read as not barred (the Standard's
            # Restriction row; 19_18, where `self` mutated to `minion` passed every test).
            if cid.startswith("C") and ctype == "Restriction" and card is not None and "F" not in codes \
                    and "F" not in waived and "F" not in waived.waits \
                    and re.search(SELF_BAR % re.escape(g.plain_text(card.field("title", "") or "")), plain, re.I):
                add("warning", "self-bar", None, "%s: a bar the card lays on itself, and the clause lists no F: another card "
                    "of its kind on the board, read as not barred (the Standard's Restriction row, 19_18)" % cid)
        # No Play clause is asked for since Q-0279: B (engine pass 31): a card's play is tested by the test of the
        # clause that bears on it, and the `plain-play` warning below asks for that test.
        if card is not None:
            want = squash(card.field("gametext", ""))
            text_clauses = [c for cid, c in clauses.items() if cid.startswith("C")]
            have = squash(" ".join(c[2] for c in sorted(text_clauses, key=lambda c: c[3])))
            if want != have:
                add("error", "ledger-text", None, "the C clauses, read in order, are not the card's game text word for word: "
                    + ledger_text_gap(card, clauses))

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
    # Copies of one title in play (Q-0239).  Not followed in a file that names a card which changes how many copies
    # a title may have (Race Text 94_16, "Minions are not unique"): what its tables allow is the card's own text.
    tables = scenario_tables(lines, others)
    loosened = any("OverrideUniqueness" in g.card_index()[x].raw for x in set(re.findall(r'"(\d+_\d+)"', raw)) if x in g.card_index())
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
        # An offer read as absent after a cheat, with no answer or refresh between: the pending decision still offers
        # what it did before the cheat, so the reading shows the board as it was (8_77's three "not offered").
        stale_at = None
        for n, line in enumerate(body):
            if MOVES_ON.search(line) or REFRESH.search(line):
                stale_at = None
                continue
            if CHEAT.search(line) and any(MOVES_ON.search(x) for x in body[:n]):
                stale_at = n
            m = NOT_OFFERED.search(line)
            if m and stale_at is not None and not let_off(start + n):
                add("warning", "stale", start + n, "%s: %s%s is read after a cheat on line %d with no answer or %sRefreshDecision() "
                    "between: a pending decision offers what it did before the cheat, so the reading shows the board as it "
                    "was (pitfalls, \"A board changed under a pending decision\")"
                    % (name, m.group(1) or m.group(3), m.group(2) or m.group(4), start + stale_at + 1, m.group(1) or m.group(3)))
        # The card played on a bearer chosen from several, with the choice's minimum never read (the Standard: where
        # the text says neither "may" nor "up to", a choice that has wrongly become optional looks the same otherwise).
        if card_id and any(c[0] == "PlayOn" and not re.search(r"\bmay\b|\bup to\b", c[2], re.I) for c in clauses.values()):
            mine = {v for v, a in re.findall(r'\bvar\s+(\w+)\s*=\s*scn\.Get(?:Freeps|Shadow)Card\(\s*"([^"]+)"', "\n".join(lines[start:end + 1]))
                    if alias_bp.get(a) == card_id}
            for n, line in enumerate(body):
                m = re.search(r"\b(Freeps|Shadow)PlayCard\s*\(\s*(\w+)\s*\)", line)
                if not m or m.group(2) not in mine:
                    continue
                after = body[n + 1:]
                upto = next((k for k, x in enumerate(after) if MOVES_ON.search(x)), len(after))
                asked = any(re.search(r"\b%sHasCardChoiceAvailable\s*\(" % m.group(1), x) for x in after[:upto])
                if asked and not any(re.search(r"\b%sGetChoiceMin\s*\(" % m.group(1), x) for x in after[:upto]) \
                        and not re.search(r"GetChoiceMin\s*\(", "\n".join(code)) and not let_off(start + n):
                    add("warning", "bearer-min", start + n, "%s: %s is played on a bearer chosen from several and the choice's "
                        "minimum is read nowhere in the file: where the text says neither \"may\" nor \"up to\", read "
                        "%sGetChoiceMin() at one such choice (the Standard; a choice wrongly made optional looks the same)"
                        % (name, m.group(2), m.group(1)))
                    break
        # Two items of one class cheated onto one bearer (Q-0213: H): no game reaches it unless a card's text allows it.
        named = dict(re.findall(r'\bvar\s+(\w+)\s*=\s*scn\.Get(?:Freeps|Shadow)Card\(\s*"([^"]+)"', "\n".join(lines[start:end + 1])))
        on = {}
        for n, line in enumerate(body):
            for bearer, items in re.findall(r"\bAttachCardsTo\s*\(\s*(\w+)\s*,([^;]*)\)", line):
                for item in re.findall(r"\w+", items):
                    for cl in item_classes(g.card_index().get(alias_bp.get(named.get(item, item)))):
                        on.setdefault((bearer, cl), []).append((n, item))
        for (bearer, cl), items in sorted(on.items()):
            if len(items) > 1 and not let_off(start + items[-1][0]):
                add("warning", "item-class", start + items[-1][0], "%s: %s are each a %s on %s: no game puts two items of one class on "
                    "one bearer (the Standard's \"A board a game could reach\"; Q-0213: H) unless a card's text allows it, "
                    "which this line then says (// lint-ok: ...)" % (name, " and ".join(i for _, i in items), cl, bearer))
        for offset, alias, title, there, limit in ([] if loosened else copies_over_the_dots(lines[start:end + 1], tables)):
            if not let_off(start + offset):
                add("warning", "copies", start + offset, "%s: with %s there are %d cards titled %s in play for one player (%s), "
                    "more than the dots on them allow (%s): no game reaches that board, and no test cheats in more copies "
                    "of a title than its dots allow, whatever the subtitles (CR 5.0 \"non-unique\"; Ketura on Q-0239).  Lint follows "
                    "the cheats, the plays and the zones the test reads, not what else the game takes out of play: where "
                    "an earlier copy has left by the game's own doing, say so on this line (// lint-ok: ...)"
                    % (name, alias, len(there), title, ", ".join(there), COPIES_WORDS[limit]))
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

    # The play of a card (Q-0279: C).  The stats test reads the printed numbers, and mechanics/costs/TwilightPoolTests and
    # WhereACardIsPlayedTests show once what they do in a plain play; neither sees a definition that carries
    # something about being played.  So a card that no live test plays from hand must be one whose play the
    # generator's reading calls plain.  Read on every card, whether its ledger has a Play clause or not.
    if card is not None and (card.field("type", "") or "").strip().lower() in PLAYED:
        bears = bears_on_play(card_id)
        if bears and not plays_from_hand(lines, card_id):
            add("warning", "plain-play", None, "no live test plays the card from hand, and by gen_play.py's reading its "
                "definition or text has something that bears on its play (%s): a test plays it by legal play, the test of "
                "the clause that says so (the stats test and the mechanics classes of a plain play see none of it; Q-0279: C)"
                % "; ".join(dict.fromkeys(bears)))

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
            "file (shapes, \"A site's continuous text\"; a pointer to mechanics/sites/SiteTextHoldsOnlyWhereTheFellowshipIsTests "
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
        # A ledger whose Notes name the census and each word it found has said what carries them (3_115, 1_183).
        notes = ledger_file.read_text(encoding="utf-8").partition("\nNotes:")[2] if ledger_file is not None and ledger_file.is_file() else ""
        for which, words, message in census.check(card_id):
            if re.search(r"\bcensus\b", notes, re.I) and all(re.search(r"\b%s" % re.escape(w.strip()), notes, re.I)
                                                              for w in words.split(",")):
                continue
            add("warning", "census", None, "%s: read the definition against the text there; where the definition is at "
                "fault that is a finding to file (`hjson-logic`), and where something else carries the words (a trigger "
                "only such a card can set off, a rule of the game) the ledger's Notes say what (Q-0220: B; `census.py %s`)"
                % (message, card_id))

    # A title that cards of more than one culture or type carry (Gollum is [gollum] and [wraith]; Saruman a minion
    # and a follower): with one type or one culture of it on every table, a type or culture written beside the name
    # in the definition passes every test (60_20).  Like second-copy, this reads which cards the file names at all, not
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
            if not have:
                continue
            # Owed (Q-0306): each TYPE the title has, and a second CULTURE where it has more than one; not every
            # culture (Saruman is a minion of four).
            types_missing = sorted({t for c, t in kinds} - {t for c, t in have})
            one_culture = len({c for c, t in kinds}) > 1 and len({c for c, t in have}) == 1
            if types_missing or one_culture:
                def words(kind):
                    return ("[%s] %s" % kind) if kind[0] else kind[1]
                owed = [k for k in sorted(kinds) if k not in have and (k[1] in types_missing or (one_culture and k[0] != have[0][0]))]
                others = ", ".join("%s (%s)" % (words(k), " ".join(sorted(kinds[k], key=g.id_sort_key)[:3]) + (" ..." if len(kinds[k]) > 3 else ""))
                                   for k in owed)
                lacks = " and ".join(x for x in ("no %s" % "/".join(types_missing) if types_missing else "",
                                                 "one culture" if one_culture else "") if x)
                add("warning", "title-kinds", None, "the definition names %s, and the cards of that title in this file are "
                    "%s: %s; there is also %s: where the text names the title with no culture or type word, each type "
                    "of the title and a second culture are valid candidates, each in a test of its own (the Standard's F, "
                    "Q-0306), or a culture or type written beside the name passes every test"
                    % (g.plain_text(g.card_index()[kinds[have[0]][0]].field("title", title)), ", ".join(words(k) for k in have),
                       lacks, others))

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


# The rules that are about how a test is written, whatever file it is in (--form).  `copies` is among them (engine
# pass 27): a board no game reaches is no better laid in a mechanics class or a lone test than in a card's own file
# (Q-0239), and the rule needs only the file's Get...Scenario() methods, not a card or a ledger.
FORM_RULES = {"bookend", "reach", "exception", "adhoc", "assert", "ignore", "style", "helper", "field", "scenario", "in-file",
              "alias", "same-blueprint", "conditional", "not-asked", "not-in-hand", "copies", "stale", "item-class"}


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
