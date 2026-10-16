#!/usr/bin/env python3
"""The census: what a card's game text names, set beside what its definition names.

    census.py <ids...>              e.g. 4_290, set:7, file:set13-Uruk-hai, seg:decipher
    census.py --all                 every definition that has game text
    census.py --all --tsv           the same, as tab-separated rows (id, name, check, what was found)

Nothing here knows what a card does.  It asks only whether a word of the text has any counterpart in the
definition, three ways (Q-0220: B; `orchestrator/trials/patterns.md` in the campaign's state, entry B, is where
the checks were tried and what they found):

  phrase   "[culture] <kind>" in the text ("a [rohan] ally", "another [men] Man", "your [uruk-hai] lurker") and no
           ONE filter of the definition that holds both the culture and the kind (Supplies of the Mark 4_290 healed
           any ally; Furious Hillman 14_10 spotted any [men] card).
  kind     a kind of card the text names (companion, ally, minion, a race, unbound, exhausted, wounded, mounted, a
           possession, a condition, an artifact, a hand or ranged weapon) and no filter of the definition has the
           word (A Promise 12_128 never asked whether Frodo or Sam was exhausted).
  done     a thing the text speaks of doing or reading (exert, heal, a burden, a threat, roaming, killed, spot ...)
           and nothing of that family anywhere in the definition's effects, target or keywords (Pressing On 7_325
           had no exertion; Long Prepared 7_109 no "roaming").

Where text and definition agree it says nothing.  A match is a place to read, not a fault: the words may be
carried by something this cannot see (a trigger that only companions can set off, a rule of the game).  `lint.py`
gives each match as a warning of kind `census`; the author reads the definition against the text there, files a
finding (`hjson-logic`) where the definition is at fault, and where it is not says in the ledger's Notes what
carries the word.  A definition with no `gametext:` (the Hobbit sets) cannot be read and is passed over, and so
is a Race to Mount Doom modifier, whose text is of deck building, the bid and the rules of the race, in words
of its own.
"""
import json
import re
import sys

import gempcards as g

CULTURES = {"dwarven", "elven", "gandalf", "gondor", "rohan", "shire", "gollum", "isengard", "moria", "sauron", "wraith",
            "ringwraith", "dunland", "raider", "men", "orc", "urukhai", "esgaroth", "smaug", "spider", "troll",
            "fallenrealms"}
# How a culture is written between brackets in game text, where that is not the word a filter uses.
CULTURE_ALIASES = {"ringwraith": "wraith", "uruk-hai": "urukhai", "uruk_hai": "urukhai", "fallen realms": "fallenrealms"}
# Cultures whose bare name in a filter is the RACE (FilterFactory): only culture(...) is the culture.
ALSO_A_RACE = ("orc", "urukhai", "troll", "spider", "wraith")

# The filter words whose argument is no filter (a title, a memory's name), and so holds no kind of card.
NO_FILTER_INSIDE = {"name", "title", "memory", "signet", "nameinstackedon", "namefrommemory", "storedculture",
                    "culturefrommemory", "racefrommemory", "titlefrommemory", "printedtwilightcostfrommemory"}
# The keys of a definition whose string is not a filter: a memory's name, words shown to a player, a type.
NOT_A_FILTER_KEY = ("memor", "text", "prevent", "type", "keyword")


class Read:
    """One definition as the checks read it: its game text plain, and what it does (effects, target, keywords)."""

    def __init__(self, card):
        self.card = card
        self.d = json.loads(json.dumps(card.parsed()))
        self.text = g.plain_text(str(self.d.get("gametext", "") or ""))
        self.does = {k: v for k, v in self.d.items() if k.lower() in ("effects", "target", "keywords", "keyword")}
        # Not the printed fields (culture, race, cost, strength): they would answer for a word no effect holds.
        self.json = json.dumps(self.does, ensure_ascii=False).lower()

    def effects(self):
        e = self.d.get("effects")
        return [] if e is None else e if isinstance(e, list) else [e]

    def filters(self):
        """Every string of the effects and the target that may be a filter, lower case with its spaces out."""
        out = []
        for node in _dicts({k: v for k, v in self.d.items() if k.lower() in ("effects", "target")}):
            for k, v in node.items():
                if isinstance(v, str) and not k.lower().startswith(NOT_A_FILTER_KEY):
                    out.append(v)
        if isinstance(self.d.get("target"), str):
            out.append(self.d["target"])
        return out

    def titles(self):
        """The card's own title and every title its definition names: where the text has one of them, its words
        name a card and no kind of card."""
        named = re.findall(r"\b(?:name|title)\(\s*([^()]+?)\s*\)", json.dumps(self.does, ensure_ascii=False), re.I)
        return {t for t in [g.plain_text(str(self.d.get("title", "") or ""))] + named if len(t.split()) > 1}

    def filter_words(self):
        got = set()
        for f in self.filters():
            _filter_words(f, got)
        return {w.replace("()", "") for w in got}


def _dicts(node):
    if isinstance(node, dict):
        yield node
        for v in node.values():
            yield from _dicts(v)
    elif isinstance(node, list):
        for v in node:
            yield from _dicts(v)


def _split_top(s):
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    out.append(cur)
    return [x.strip() for x in out if x.strip()]


def _filter_words(s, out):
    """The words of one filter, those inside its brackets included: `choose(your,culture(rohan),ally)` is choose(),
    your, culture(), rohan and ally."""
    for part in _split_top(str(s)):
        m = re.match(r"^([A-Za-z][\w \-']*?)\s*\((.*)\)$", part, re.S)
        if m:
            name = m.group(1).strip().lower().replace(" ", "")
            out.add(name + "()")
            if name not in NO_FILTER_INSIDE:
                _filter_words(m.group(2), out)
        else:
            w = part.strip().lower()
            if w and not re.match(r"^[\d\-+<>=]+$", w):
                out.add(w.replace(" ", ""))


def _culture(written):
    c = written.lower().strip()
    return CULTURE_ALIASES.get(c, c).replace("-", "").replace(" ", "")


# ----------------------------------------------------------------------------------------------------------------
# phrase: "[culture] <kind>" in the text wants one filter that holds both words
# ----------------------------------------------------------------------------------------------------------------
PHRASE_KINDS = {
    "companion": ("companion",), "companions": ("companion",), "ally": ("ally",), "allies": ("ally",),
    "minion": ("minion",), "minions": ("minion",), "man": ("man",), "men": ("man",), "elf": ("elf",),
    "elves": ("elf",), "dwarf": ("dwarf",), "dwarves": ("dwarf",), "hobbit": ("hobbit",), "hobbits": ("hobbit",),
    "orc": ("orc",), "orcs": ("orc",), "uruk-hai": ("urukhai", "uruk-hai", "uruk_hai"), "wizard": ("wizard",),
    "wraith": ("wraith",), "wraiths": ("wraith",), "troll": ("troll",), "trolls": ("troll",),
    "possession": ("possession",), "possessions": ("possession",), "condition": ("condition",),
    "conditions": ("condition",), "artifact": ("artifact",), "artifacts": ("artifact",), "event": ("event",),
    "events": ("event",), "weapon": ("weapon", "handweapon", "rangedweapon"), "mount": ("mount",), "mounts": ("mount",),
    "archer": ("archer",), "archers": ("archer",), "tracker": ("tracker",), "trackers": ("tracker",),
    "character": ("character", "companion", "minion", "ally"), "characters": ("character", "companion", "minion", "ally"),
    "item": ("item", "possession", "artifact"), "items": ("item", "possession", "artifact"),
    "knight": ("knight",), "ranger": ("ranger",), "rangers": ("ranger",), "hunter": ("hunter",), "hunters": ("hunter",),
    "lurker": ("lurker",), "fortification": ("fortification",), "tale": ("tale",), "engine": ("engine",),
    "follower": ("follower",), "followers": ("follower",), "skirmish": ("skirmish",), "nazgûl": ("nazgul",),
}


def _word_in(word, filter_text):
    return re.search(r"(^|[^a-z])%s([^a-z]|$)" % re.escape(word), filter_text) is not None


def phrases(read):
    """The "[culture] <kind>" phrases of the text that no one filter of the definition holds."""
    out = []
    filters = [f.lower().replace(" ", "") for f in read.filters()]
    for m in re.finditer(r"\[([a-z\- ]+)\] ([A-Za-zû\-]+)", read.text, re.I):
        culture, kind = _culture(m.group(1)), m.group(2).lower()
        if culture not in CULTURES or kind not in PHRASE_KINDS:
            continue
        by_culture = ["culture(%s)" % culture]
        if culture not in ALSO_A_RACE:
            by_culture.append(culture)
        if culture == "wraith":
            by_culture += ["ringwraith", "culture(ringwraith)"]
        if culture == "urukhai":
            by_culture.append("culture(uruk-hai)")
        if any(any(_word_in(x, f) or x in f.replace("-", "") for x in by_culture)
               and any(_word_in(x, f.replace("-", "")) for x in PHRASE_KINDS[kind]) for f in filters):
            continue
        # What the card says of itself, or makes of itself, is in its printed fields and not in a filter.
        if re.search(r"(this|is an?|becomes? an?|make this (condition|card) an?( fierce)?) \[[a-z\- ]+\] %s"
                     % re.escape(m.group(2)), read.text, re.I):
            continue
        out.append("[%s] %s" % (m.group(1), m.group(2)))
    return sorted(set(out))


# ----------------------------------------------------------------------------------------------------------------
# kind: a kind of card the text names, and no filter with the word
# ----------------------------------------------------------------------------------------------------------------
# (how it is said of, how the text writes it, the filter words that stand for it)
KINDS = [
    ("companion", r"\bcompanions?\b", ("companion",)),
    ("ally", r"\ball(y|ies)\b", ("ally", "allyhome", "allyinregion", "allyathome")),
    ("minion", r"\bminions?\b", ("minion",)),
    ("unbound", r"\bunbound\b", ("unbound",)),
    ("Ring-bound", r"\bring-bound\b", ("ringbound", "ring-bound")),
    ("Ring-bearer", r"\bring-bearer\b", ("ringbearer", "ring-bearer", "corruptringbearer", "putonring", "makeselfringbearer")),
    ("exhausted", r"\bexhausted\b", ("exhausted",)),
    ("wounded", r"\bwounded\b", ("wounded", "unwounded")),
    ("mounted", r"\bmounted\b", ("mounted",)),
    ("possession", r"\bpossessions?\b", ("possession",)),
    ("condition", r"\bconditions?\b", ("condition",)),
    ("artifact", r"\bartifacts?\b", ("artifact",)),
    # "may be borne in addition to 1 other hand weapon" is the card's `extraPossessionClass`
    ("hand weapon", r"\bhand weapons?\b", ("handweapon", "extrapossessionclass")),
    ("ranged weapon", r"\branged weapons?\b", ("rangedweapon",)),
    ("Elf", r"\bEl(f|ves)\b", ("elf",)), ("Dwarf", r"\bDwar(f|ves)\b", ("dwarf",)),
    ("Hobbit", r"\bHobbits?\b", ("hobbit",)), ("Man", r"\bM[ae]n\b", ("man",)),
    ("Wizard", r"\bWizards?\b", ("wizard",)), ("Orc", r"\bOrcs?\b", ("orc",)),
    ("Uruk-hai", r"\bUruk-hai\b", ("urukhai", "uruk-hai", "uruk_hai")),
    ("Nazgûl", r"\bNazg[uû]l\b", ("nazgul",)), ("Troll", r"\bTrolls?\b", ("troll",)),
    ("Ent", r"\bEnts?\b", ("ent",)),
]
# Words of a text that are no kind of card where they stand.  (A card's own title, and every title its definition
# names, are taken out as well: "Orc Overseer", "another Winged Nazgul", "Cave Troll of Moria".)
NOT_A_KIND = (# what a card makes of itself for a while is in the effect that makes it so, not in a filter
              r"\b(becomes|make this(?: condition| artifact| card)?) an? [^.]*?\bminion\b", r"\bis still an? \w+",
              r"minion archery total", r"fellowship archery total", r"comes? into play exhausted", r"Cave Troll of Moria",
              r"Free Peoples player", r"Shadow player", r"Ring-bearer's", r"in the dead pile", r"Shadow number",
              r"your (opponent|hand|draw deck|discard pile|dead pile|support area|fellowship|turn|adventure deck|starting fellowship)s?",
              r"starting companions?", r"that minion's owner", r"Uruk-hai culture",
              r"\b(this|that|the) (minion|companion|ally|possession|condition|artifact|event|weapon|mount|follower)\b",
              r"Ring-bearer \(resistance \d+\)", r"\bhe or she\b", r"each other", r"wounds? (to|on)", r"is not exhausted",
              r"\b(him|her|them|it) exhausted",
              # every lurker is a minion; "exerted, wounded, or assigned" is what is done to a card, not what it is
              r"(?<=\blurker) minions?\b", r"\bbe (?:exerted,? (?:or )?)?wounded\b",
              # "make allies take wounds from archery fire instead of companions" is one modifier (Q-0136)
              r"\binstead of companions\b",
              # whoever skirmishes a companion or an ally is a minion, and nobody else is skirmished by one
              r"\bminions?(?= skirmishing\b)", r"(?<=\bskirmishing )an? minion\b")


def kinds(read):
    """The kinds of card the text names that no filter of the definition has a word for."""
    if not read.effects() and not read.d.get("target"):
        return []
    words = read.filter_words()
    js = read.json.replace(" ", "")
    # A word an effect's or a modifier's own type carries (`noMoreThanOneMinionMayBeAssignedToEachSkirmish`, Caras
    # Galadhon 3_115): the definition says it in the name of what it does, with no filter to hold it.
    types = [v.lower() for node in _dicts(read.does) for k, v in node.items() if k.lower() == "type" and isinstance(v, str)]
    text = read.text
    for title in sorted(read.titles(), key=len, reverse=True):
        text = re.sub(r"(?<!\w)%s(?!\w)" % re.escape(title), " ", text, flags=re.I)
    for junk in NOT_A_KIND:
        text = re.sub(junk, " ", text, flags=re.I)
    out = []
    for name, written, stands_for in KINDS:
        if not re.search(written, text, re.I if name[0].islower() else 0):
            continue
        if any(w in words or (w + "(") in js or _word_in(w, js) or any(w in ty for ty in types) for w in stands_for):
            continue
        out.append(name)
    return out


# ----------------------------------------------------------------------------------------------------------------
# done: a thing the text speaks of doing or reading, and nothing of its family in the definition
# ----------------------------------------------------------------------------------------------------------------
# (how it is said of, how the text writes it, what would show it anywhere in the definition's effects)
DONE = [
    ("exert", r"\bexert(s|ed|ing)?\b", ("exert", "exhaust")),
    ("exhaust", r"\bexhaust(s)?\b(?! ed)", ("exhaust", "exert")),
    # wounds healed or removed are the business of "heal" (NOT_DONE)
    ("wound", r"\bwound(s|ed)?\b", ("wound", "kill")),
    ("heal", r"\bheal(s|ed)?\b", ("heal",)),
    ("discard", r"\bdiscard(s|ed)?\b(?! pile)", ("discard", "selfremovedfromplay")),
    ("draw", r"\bdraws? (a|an|\d+|X|that|the same|cards?)\b", ("drawcard", "draws", "reconcile")),
    ("burden", r"\bburdens?\b", ("burden", "corrupt", "resistance")),
    ("threat", r"\bthreats?\b", ("threat",)),
    ("twilight", r"(?<!Ambush )(?<!Toil )\((\d+|X)\)|twilight (token|pool|cost)",
     ("twilight", "modifycost", "discount", "modifyowncost", "modifyplayoncost", "toil", "printedtwilight", "cost",
      "transferforfree", "ambush")),
    ("strength", r"\bstrength\b(?! is tripled)", ("strength", "overwhelm")),
    ("vitality", r"\bvitality\b", ("vitality",)),
    ("resistance", r"\bresistance\b", ("resistance",)),
    ("liberate", r"\bliberate", ("liberate",)),
    ("control of a site", r"\b(take control of|controls? (a|an|\d+|no)) ",
     ("controlofsite", "controlssite", "controlledsite", "siteyoucontrol", "controlsite", "sitecontrol", "controlled")),
    ("cancel", r"\bcancel", ("cancel",)),
    ("stack", r"\bstack(ed|s)?\b", ("stack",)),
    ("reveal", r"\breveal", ("reveal", "stacktopcardsofdrawdeck", "lookat")),
    ("shuffle", r"\b(re)?shuffle", ("shuffle",)),
    ("into hand", r"\binto (your |his or her |their )?hand\b|to (your|his or her|their|its owner's) hand\b",
     ("intohand", "returntohand", "drawcard")),
    ("transfer", r"\btransfer", ("transfer",)),
    ("assign", r"\bassign(ed|s)?\b", ("assign",)),
    ("killed", r"\bkill(s|ed)?\b|\bdies\b|dead pile", ("kill", "deadpile", "dies", "death")),
    ("prevent", r"\bprevent", ("prevent", "negate", "canttake", "disable", "cant", "removekeyword", "overwhelm", "cancel", "placeno")),
    ("site number", r"\bsite number\b", ("sitenumber",)),
    ("archery total", r"\barchery total\b", ("archery",)),
    ("move limit", r"\bmove limit\b", ("movelimit",)),
    ("initiative", r"\binitiative\b", ("initiative",)),
    ("token", r"\b(?<!twilight )tokens?\b(?! in the twilight)", ("token", "placeno", "replacecardinplay")),
    ("overwhelm", r"\boverwhelm", ("overwhelm",)),
    ("skip", r"\bskip", ("skip",)),
    # "When you play this, you may ... to make this minion twilight cost -1" is a discount on the play itself
    ("play", r"\bplay (a|an|the|up to|any|your|\d|X|this|that)\b", ("play", "discount", "modifyowncost", "modifycost")),
    ("The One Ring put on or taken off", r"(wear|put on|take off|wearing) The One Ring", ("ring",)),
    ("fierce", r"\bfierce\b(?! skirmish)", ("fierce",)), ("defender", r"\bdefender \+", ("defender",)),
    ("damage", r"\bdamage \+|damage bonus", ("damage",)), ("archer", r"\barchers?\b", ("archer",)),
    ("roaming", r"\broaming\b", ("roaming",)), ("muster", r"\bmuster\b", ("muster",)),
    ("hunter", r"\bhunters?\b", ("hunter",)), ("unhasty", r"\bunhasty\b", ("unhasty",)),
    ("region", r"\bregion\b", ("region",)), ("signet", r"\bsignet\b", ("signet",)),
    ("spot", r"\bspot\b", ("spot", "requiresranger", "foreach", "haveinitiative", "controlssite", "ringison",
                           "memorymatches", "location", "chooseactivecards", "copycard")),
]
# A spot with no "to" after it in its sentence is the effect itself ("Exert Aragorn to spot an ally.  Until the
# regroup phase, that ally ...": Q-0085), which a definition writes as the choice of the card.  A spot that stands
# before a "to" is a requirement or a cost, and a choice does not answer for it (Vilya 3_27).
SPOT_BEFORE_TO = re.compile(r"\bspot\b[^.]*?\bto\b(?! (?:your|his or her|their|its owner's) hand)", re.I)
# Sentences that say where a card is played and who bears it (the definition holds them in `target` and the
# card's printed fields, not in anything done), and words that are a reminder of a rule.
NOT_DONE = ((r"\(resistance \d+\)", " "), (r"Plays to your support area\.", " "), (r"Plays on [^.]*\.", " "),
            (r"Bearer must be [^.]*\.", " "), (r"Limit 1 per (bearer|character|companion|minion|site)\.", " "),
            (r"\b(?-i:To) play this \w+,", " "), (r"\(if the move limit allows\)", " "), (r"\([^()]*extra round of skirmishes after fierce[^()]*\)", " "),
            # a wound healed or removed is a heal: "heal up to 3 wounds", "remove a burden or wound from a
            # companion", "wounds may not be removed from bearer", "each time a wound is removed"
            (r"\b(?:heals?|removes?)((?: only| up to| no more than| all| any| each)*(?: \d+| X| an?| one)?"
             r"(?: burdens? (?:or|and))?) wounds?\b", r"heal\1"),
            (r"\bwounds?\b((?: (?:may|can) ?not be| cannot be| is| are)? (?:removed|healed))", r"heal \1"),
            # a token discarded is a token removed
            (r"\bdiscard (an?|\d+|X) ((?:\[[a-z\- ]+\] )?tokens?)", r"remove \1 \2"))


def done(read):
    """The things the text speaks of doing that nothing in the definition's effects, target or keywords is of."""
    js = read.json.replace(" ", "")
    text = read.text
    for junk, instead in NOT_DONE:
        text = re.sub(junk, instead, text, flags=re.I)
    out = []
    for name, written, shown_by in DONE:
        if not re.search(written, text, re.I) or any(x in js for x in shown_by):
            continue
        if name == "spot" and "choose(" in js and not SPOT_BEFORE_TO.search(text):
            continue
        out.append(name)
    return out


# ----------------------------------------------------------------------------------------------------------------
def check(card_id: str) -> list:
    """(which check, what was found, the message for an author) for each thing the text of a card names that its
    definition does not.  Empty where they agree, where the definition has no game text or is a Race to Mount
    Doom modifier, and where it cannot be read as hjson."""
    card = g.card_index().get(card_id)
    if card is None:
        return []
    try:
        read = Read(card)
    except Exception:
        return []
    if not read.text.strip() or str(read.d.get("type", "")).strip().lower() == "metasite":
        return []
    out = []
    found = phrases(read)
    if found:
        out.append(("phrase", ", ".join(found),
                    "the text says \"%s\" and no one filter of the definition holds both the culture and the kind"
                    % "\", \"".join(found)))
    found = kinds(read)
    if found:
        out.append(("kind", ", ".join(found),
                    "the text names %s and no filter of the definition has the word" % ", ".join(found)))
    found = done(read)
    if found:
        out.append(("done", ", ".join(found),
                    "the text speaks of %s and nothing of the kind is anywhere in the definition's effects"
                    % ", ".join(found)))
    return out


def main(argv):
    args = [a for a in argv[1:] if not a.startswith("--")]
    if "--all" in argv:
        ids = sorted(g.card_index(), key=g.id_sort_key)
    elif args:
        ids = g.resolve_ids(args)
    else:
        print(__doc__)
        return 0
    cards = g.card_index()
    matched = 0
    for card_id in ids:
        found = check(card_id)
        if not found:
            continue
        matched += 1
        name = g.full_name(cards[card_id])
        if "--tsv" in argv:
            for which, words, message in found:
                print("\t".join([card_id, name, which, words]))
        else:
            print("%s  %s" % (card_id, name))
            for which, words, message in found:
                print("    %-7s %s" % (which, message))
    if "--tsv" not in argv:
        print("\n%d of %d definitions have something to read." % (matched, len(ids)))
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except BrokenPipeError:
        sys.exit(0)
