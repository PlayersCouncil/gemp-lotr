#!/usr/bin/env python3
"""Twins: Decipher cards whose game text is the same but for names and cultures (Q-0220: H).

    twins.py                        every group: its cards, and the game text of the first.  A card with a row in
                                    status.tsv has its status after its title: its ledger is there to start from.
    twins.py <id> ...               the group of each card named, with the game text of every card in it
    twins.py --queue <file>         a queue of card ids (a JSON list, as orchestrator/queue-*.json in the state;
                                    `-` reads standard input, JSON or bare ids) reordered so that the cards of a
                                    group are adjacent and, where it can be done, in one batch.  Prints the list
                                    as JSON; what it moved is said on standard error.  The file is not changed.
    twins.py --queue <file> --write the same, written back to the file
    --batch N                       how many cards a batch takes off the front of a queue (4, as close.py cuts
                                    them); 0 makes a group adjacent and looks at no batch

Why: one author works a batch's cards one after another, so the second card of a group can start from the first's
ledger and tests if both are in his batch, and cannot if the other is being written at the same time in another
batch.  Run it over a queue when the queue is written, and again after a queue has been changed by hand or a batch
of other than N cards has been taken off it (the batches are counted from the front).

What a twin is.  Two Decipher cards (sets 0 to 19) that both have game text and effects, and whose texts are the
same once these are taken out: the card's own title and the first word of it; every [culture]; and every
capitalised word, or run of them, that does not begin a sentence.  So a race, another card's title, "Shadow" and
"Free Peoples" count as names, and every number and every other word must agree ("an Elf" is no twin of
"a Dwarf": the article differs).  It is the key of the pattern search that Ketura took under Q-0220: H
(`orchestrator/trials/patterns.md` in the state, section 4; `patterns-searches/order_and_twins.py`, "the same text
but for names and cultures"), which found 153 cards in 65 groups on 2026-10-10; the listing's last line counts
them again as the definitions stand.  The definitions of a group are NOT compared: twins by text may be built
differently, and each card's own definition is still read and tested.

Reordering.  A group's cards in the queue are brought to the place of the first of them, in the order the queue
had them; every other card keeps its order.  Then, batch by batch from the front: a group that would be cut by the
end of a batch waits, the next cards that fit are taken ahead of it, and it begins the next batch.  A group larger
than a batch begins one and runs over.  A group's card that is not in the queue is not put in it.  Running it on
its own output changes nothing.

Read-only over the repository; `--write` is the one thing that writes, and writes only the queue file named.
"""
import csv
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gempcards as g  # noqa: E402

# A capitalised word, or a run of them, that begins neither the text nor a sentence nor what follows a colon.
_NAME = re.compile(r"(?<![.!?:] )(?<!^)\b[A-ZÉÚÓ][\w'éúûóëâíáä\-]+(?: [A-ZÉÚÓ][\w'éúûóëâíáä\-]+)*")
_CULTURE = re.compile(r"\[[a-z\- ]+\]", re.I)


def text_key(title: str, gametext: str) -> str:
    """A game text with everything a twin may differ in taken out."""
    text = re.sub(r"\s+", " ", g.plain_text(gametext).strip())
    names = {title}
    first = title.split(",")[0].split(" ")[0]
    if len(first) > 3:
        names.add(first)
    for name in sorted(names, key=len, reverse=True):
        if name:
            text = re.sub(re.escape(name), "NAME", text)
    text = _CULTURE.sub("[C]", text)
    return _NAME.sub("Name", text)


class Twins:
    """The groups among the Decipher definitions that have game text and effects."""

    def __init__(self):
        self.title, self.text, self.group_of = {}, {}, {}
        by_key = defaultdict(list)
        for card_id, card in g.card_index().items():
            if g.segment_of(card_id) != "decipher":
                continue
            try:
                d = card.parsed()
            except Exception as e:  # a definition hjson cannot read is no twin of anything
                print("twins.py: %s is not read (%s)" % (card_id, str(e)[:70]), file=sys.stderr)
                continue
            gametext = str(d.get("gametext", "") or "")
            if not gametext.strip() or not d.get("effects"):
                continue
            title = str(d.get("title", ""))
            subtitle = d.get("subtitle")
            self.title[card_id] = title + (", " + str(subtitle) if subtitle else "")
            self.text[card_id] = g.plain_text(gametext)
            by_key[text_key(title, gametext)].append(card_id)
        self.groups = sorted((sorted(ids, key=g.id_sort_key) for ids in by_key.values() if len(ids) > 1),
                             key=lambda ids: g.id_sort_key(ids[0]))
        for n, ids in enumerate(self.groups):
            for card_id in ids:
                self.group_of[card_id] = n

    def reorder(self, queue, batch=4):
        """The queue with each group's cards together and, where it can be done, inside one batch of `batch`.
        Returns (the new list, the groups it holds two or more cards of, those of them a batch's end still cuts)."""
        units, placed, seen = [], {}, set()
        for card_id in queue:
            n = self.group_of.get(card_id)
            if n is None or card_id in seen:
                units.append([card_id])  # no twin, or an id the queue holds twice: left where it is
            elif n in placed:
                placed[n].append(card_id)
            else:
                placed[n] = [card_id]
                units.append(placed[n])
            seen.add(card_id)
        together = [u for u in units if len(u) > 1]
        if batch <= 0:
            return [c for u in units for c in u], together, []
        out, cut = [], []
        while units:
            room = batch - len(out) % batch
            take = 0
            if room < batch and len(units[0]) > room:
                take = next((i for i, u in enumerate(units) if len(u) <= room), 0)
            unit = units.pop(take)
            if len(unit) > 1 and len(out) // batch != (len(out) + len(unit) - 1) // batch:
                cut.append(unit)
            out += unit
        return out, together, cut


def statuses() -> dict:
    out = {}
    path = g.state_dir() / "status.tsv"
    if path.is_file():
        with open(path, encoding="utf-8") as fh:
            for row in csv.reader(fh, delimiter="\t"):
                if len(row) > 1 and row[0] != "id":
                    out[row[0]] = row[1]
    return out


def read_queue(name: str) -> list:
    raw = sys.stdin.read() if name == "-" else Path(name).read_text(encoding="utf-8")
    try:
        ids = json.loads(raw)
    except ValueError:
        ids = raw.split()
    if not isinstance(ids, list) or not all(isinstance(i, str) and re.match(r"^\d+_\d+$", i) for i in ids):
        raise SystemExit("twins.py: %s is not a list of card ids" % name)
    return ids


def main(argv):
    args = list(argv[1:])
    if "-h" in args or "--help" in args:
        print(__doc__)
        return 0

    def take(flag):
        if flag not in args:
            return None
        i = args.index(flag)
        if i + 1 >= len(args):
            raise SystemExit("twins.py: %s wants a value" % flag)
        value = args[i + 1]
        del args[i:i + 2]
        return value

    queue_file, batch = take("--queue"), int(take("--batch") or 4)
    write = "--write" in args
    if write:
        args.remove("--write")
    twins = Twins()

    if queue_file:
        if args:
            raise SystemExit("twins.py: with --queue, nothing else is named (%s)" % " ".join(args))
        if write and queue_file == "-":
            raise SystemExit("twins.py: --write wants a file")
        queue = read_queue(queue_file)
        out, together, cut = twins.reorder(queue, batch)
        assert sorted(out) == sorted(queue)
        note = lambda line: print(line, file=sys.stderr)  # noqa: E731
        moved = sum(1 for a, b in zip(queue, out) if a != b)
        note("%d cards; %d group(s) with two or more cards in the queue; %s" % (
            len(queue), len(together), "%d places hold another card now" % moved if moved else "nothing moved"))
        for unit in together:
            was = [queue.index(c) for c in unit]
            if was != list(range(was[0], was[0] + len(unit))):
                note("  brought together: " + " ".join(unit))
        for unit in cut:
            note("  NOT in one batch of %d: %s" % (batch, " ".join(unit)))
        status = statuses()
        inside = set(queue)
        for card_id in out:
            n = twins.group_of.get(card_id)
            others = [c for c in twins.groups[n] if c not in inside] if n is not None else []
            if others:
                note("  %s has twins outside this queue: %s" % (card_id, ", ".join(
                    "%s (%s)" % (c, status.get(c, "not worked")) for c in others)))
        if write:
            Path(queue_file).write_text(json.dumps(out), encoding="utf-8")
            note("written: " + queue_file)
        else:
            print(json.dumps(out))
        return 0

    status = statuses()

    def named(card_id):
        return "%s %s%s" % (card_id, twins.title[card_id], " (%s)" % status[card_id] if card_id in status else "")

    if args:
        for card_id in g.resolve_ids(args):
            n = twins.group_of.get(card_id)
            if n is None:
                print("%s: no twin" % card_id)
                continue
            print("%s: a group of %d" % (card_id, len(twins.groups[n])))
            for other in twins.groups[n]:
                print("    %s\n        %s" % (named(other), twins.text[other]))
        return 0

    for ids in twins.groups:
        print(" | ".join(named(c) for c in ids))
        print("        " + twins.text[ids[0]])
    cards = sum(len(ids) for ids in twins.groups)
    worked = sum(1 for ids in twins.groups for c in ids if c in status)
    print("%d cards in %d groups; %d of the cards have a row in status.tsv" % (cards, len(twins.groups), worked))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
