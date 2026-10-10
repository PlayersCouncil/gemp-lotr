#!/usr/bin/env python3
"""Makes the conversion batches of the old at/ tests from the survey's .tsv files.

    at_batches.py            writes <state>/orchestrator/at-convert/<key>.md for every batch, PLAN.md and queue.json
    at_batches.py --done     lists the batches whose old methods are all gone from at/ (and marks them in PLAN.md)

A batch is a handful of new homes (mechanics classes of one family, or eight to ten card files) with, for each new
test, where its directive is and which old methods it replaces.  The orchestrator's decisions that move a method
somewhere else than the survey put it are in OVERRIDES below, with their reasons in at-survey/DECISIONS.md.
"""
import csv
import json
import os
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gempcards as g

REPO = Path(g.REPO)
TESTS = "gemp-lotr/gemp-lotr-server/src/test/java/com/gempukku/lotro/"
AT = REPO / TESTS / "at"
MECH = TESTS + "mechanics/"

# method -> (status, home, new test name); None keeps the survey's.
OVERRIDES = {
    "playerIs": ("covered", MECH + "PlayerIsRequirementTests.java", None),
    "playerIsAny": ("covered", MECH + "PlayerIsRequirementTests.java", None),
    "bilboRingBearerWithConsortingAndMorgulBrute": (None, MECH + "PreventionIsAskedOnlyWhenItsEffectWillHappenTests.java", None),
    "treebeardEarthborn": (None, MECH + "InsteadTests.java", "ACostPaidInsteadIsPaidAndWhatItBuysHappens"),
    "insteadOfWoundWorksIfCantPrevent": (None, MECH + "InsteadTests.java", None),
    "betterThanNothingDoesNotAddBurden": (None, TESTS + "cards/unofficial/hobbit/set31/Card_31_019_Tests.java", None),
    "HinderedCompanionStillCountsTowardsUniqueness": (None, MECH + "UniquenessTests.java", None),
}

# mechanics class (file name) -> batch key, in the order the batches are run
FAMILIES = OrderedDict([
    ("mech-triggers", ["OptionalActionsAnsweringOneTriggerTests", "RequiredActionsAnsweringOneTriggerTests", "MusterTests",
                       "PreventionIsAskedOnlyWhenItsEffectWillHappenTests", "ACostThatIsPreventedIsNotPaidTests", "InsteadTests",
                       "AWoundThatIsNotTakenTests"]),
    ("mech-toil", ["ToilTests", "RoamingTests", "ChoosingHowManyToSpotTests"]),
    ("mech-skirmish", ["AssignmentPhaseTests", "DefenderTests", "FierceTests", "UnhastyTests", "AmbushTests", "ArcheryFireTests",
                       "AidTests", "KilledAtZeroVitalityTests", "ExhaustTests"]),
    ("mech-setup", ["StartingFellowshipTests", "CorruptionBeforeTheGameStartsTests", "FrodoBesideAnotherRingBearerTests",
                    "SiteTextHoldsOnlyWhereTheFellowshipIsTests", "LeavingPlayTests", "DiscardToHealTests", "DrawingCardsTests"]),
    ("mech-hinder-a", ["HinderedCardCannotBeSpottedTests", "HinderedCardHasNoStatsTests"]),
    ("mech-hinder-b", ["TokensOnAHinderedCardTests", "HinderedCardHasNoGameTextTests"]),
    ("mech-hinder-c", ["CardsBorneByAHinderedCardTests", "HinderedBorneCardTests", "HinderedCardStillRestrictsPlayTests",
                       "HinderedRingBearerTests", "ReconcilingRestoresHinderedCardsTests"]),
    ("mech-hinder-d", ["HinderedCharacterCannotSkirmishTests", "UniquenessTests", "RelentlessTests"]),
])
NOTES = {
    "mech-triggers": "InsteadTests is the orchestrator's class (CR 4.2 \"instead\"), for two old methods the survey put in card files: Ketura's rule is that \"instead\" converts a cost and neither prevents nor cancels (`lookup.py instead`). Both are among the tests she calls the tricky ones. Three old boards assert an offer Q-0034 says is not made (Gimli 5_7, Dervorin 7_88, Aragorn 1_365): write by the ruling. (Gandalf 2_122's draw from an empty deck IS offered, by Q-0029.)",
    "mech-toil": "Toil against roaming: try whether the game offers a toil card from a pool under the roaming penalty; a finding if the game differs from the two rules' wording. RoamingTests takes the two extra cases its directive offers. ALSO for this batch, though its file is not listed below: `mechanics/MusterTests.java` (landed by the batch mech-triggers with one test) gains the three cases `at-survey/TriggersAtTest.md` offers under its muster entry: two muster characters discard 0 to 2 cards all at once and draw as many (the CR's own example); with no card in hand there is no offer; declined, nothing changes.",
    "mech-skirmish": "DefenderTests: the negative (two minions may not be given to a companion WITHOUT defender) cannot be read with the rig today and the old test showed it by the game throwing, which no test may rely on: write the positive tests, and return that negative under `rig` with what the rig would have to read. ArcheryFireTests is a new class unless both its tests are plainly AlliesTakeArcheryWoundsTests' subject.",
    "mech-setup": "FrodoBesideAnotherRingBearerTests: Q-0159 is answered B (no Frodo is ever on the table beside another Ring-bearer); the test shows the RULE that keeps him off (he cannot be played), and its class name should say that (FrodoCannotJoinAnotherRingBearerTests, say). The bid before the game reads resistance in force (Ketura on Galadriel 9_14).",
    "mech-hinder-a": "Ketura, 2026-10-10: the three methods about a hindered card's strength, vitality and resistance STAY, on a construct of the tests' own (\"there's engine code that this was used to enforce\"): no real card can show them. Everywhere else in the family the hack on Frodo gives way to `scn.HinderCard` or a real card, as each directive says.",
    "mech-hinder-b": "The hack on Frodo gives way to `scn.HinderCard` or a real card, as each directive says. Brego 13_63's board asserts an offer Q-0034 says is not made: write by the ruling.",
    "mech-hinder-c": "HinderedRingBearerTests exists: four tests join it and its class comment is widened to burdens and corruption. ReconcilingRestoresHinderedCardsTests: two old methods move nearly as they are. Hinder does not reach inactive cards (a stacked card is not hindered): the stub about Courtesy of My Hall is dropped.",
    "mech-hinder-d": "UniquenessTests also takes the hinder suite's AnotherCardOfAHinderedUniqueCardsTitleCannotBePlayed. Unexpected Visitor 7_126's board asserts an offer Q-0034 says is not made: write by the ruling. Toldea 103_79: assign with an ability, as the old test did; whether the Shadow player may assign the freed Toldea is not settled and is NOT asserted (return it under `questions`).",
}


def survey_rows():
    rows = []
    folder = g.state_dir() / "orchestrator" / "at-survey"
    for f in sorted(folder.glob("*.tsv")):
        for r in csv.reader(f.open(encoding="utf-8"), delimiter="\t", quoting=csv.QUOTE_NONE):
            if len(r) < 6:
                continue
            r = (r + [""] * 8)[:8]
            at_file, method, kind, status, home, name, cards, sentence = [x.strip() for x in r]
            if method in OVERRIDES:
                s, h, n = OVERRIDES[method]
                status, home, name = s or status, h or home, n or name
            rows.append(dict(at=at_file, method=method, kind=kind, status=status, home=home, name=name, cards=cards,
                             sentence=sentence, survey=f.stem + ".md"))
    return rows


def card_key(home: str):
    m = re.search(r"Card_(V?\d+)_(\d+)_", os.path.basename(home))
    return (int(m.group(1).lstrip("V")) if m else 999, int(m.group(2)) if m else 0)


def still_there(row) -> bool:
    """Whether the old method is still in at/."""
    p = AT / row["at"]
    return p.is_file() and re.search(r"\bvoid\s+%s\s*\(" % re.escape(row["method"]), p.read_text(encoding="utf-8", errors="replace")) is not None


def main(argv):
    rows = survey_rows()
    todo = [r for r in rows if r["status"] != "covered" and r["home"] not in ("", "-")]
    gone = [r for r in rows if r["status"] == "covered" or r["home"] in ("", "-")]
    by_home = defaultdict(list)
    for r in todo:
        by_home[r["home"]].append(r)
    batches = OrderedDict()
    placed = set()
    for key, classes in FAMILIES.items():
        homes = [h for h in by_home if os.path.basename(h)[:-5] in classes]
        if homes:
            batches[key] = sorted(homes, key=lambda h: classes.index(os.path.basename(h)[:-5]))
            placed.update(homes)
    rest_mech = sorted(h for h in by_home if "/mechanics/" in h and h not in placed)
    if rest_mech:
        batches["mech-other"] = rest_mech
        placed.update(rest_mech)
    server = sorted(h for h in by_home if h not in placed and not os.path.basename(h).startswith("Card_"))
    if server:
        batches["server"] = server
        placed.update(server)
    hobbit = sorted(h for h in by_home if "/hobbit/" in h)
    if hobbit:
        batches["cards-hobbit"] = hobbit
        placed.update(hobbit)
    cards = sorted((h for h in by_home if h not in placed), key=card_key)
    for i in range(0, len(cards), 10):
        chunk = cards[i:i + 10]
        a, b = card_key(chunk[0]), card_key(chunk[-1])
        batches["cards-%02d_%d-%02d_%d" % (a[0], a[1], b[0], b[1])] = chunk

    out = g.state_dir() / "orchestrator" / "at-convert"
    out.mkdir(parents=True, exist_ok=True)
    plan = ["# Converting the old at/ tests: the batches", "",
            "Made by at_batches.py from the survey (`../at-survey/`). %d old methods become about %d tests in %d homes; %d more are "
            "covered already or dropped and are only deleted (the last batch, `sweep`)." %
            (len(todo), len({(r["home"], r["name"]) for r in todo}), len(by_home), len(gone)), ""]
    done_keys = []
    for key, homes in batches.items():
        tests = OrderedDict()
        for h in homes:
            for r in by_home[h]:
                tests.setdefault((h, r["name"]), []).append(r)
        left = sum(1 for h in homes for r in by_home[h] if still_there(r))
        if left == 0:
            done_keys.append(key)
        plan.append("- `%s`: %d homes, %d tests, replacing %d old methods%s" %
                    (key, len(homes), len(tests), sum(len(v) for v in tests.values()), "  (DONE)" if left == 0 else ""))
        lines = ["# at-convert batch `%s`" % key, ""]
        if key in NOTES:
            lines += ["**For this batch.** " + NOTES[key], ""]
        if key == "cards-hobbit":
            lines += ["**For this batch.** These are Hobbit-set cards (sets 30 to 33): no sheet has them and some have no game text in their "
                      "definition, so each test rests on the definition and the old method alone. Their files go under "
                      "`cards/unofficial/hobbit/set<NN>/` (a new folder; not under `pc/`), with a stats test in the manner of the other "
                      "sets' files as far as the definition's fields allow. Only this one test each: the Hobbit sets are worked last.", ""]
        if key == "server":
            lines += ["**For this batch.** These are not tests of the game: each goes beside the class it guards (the survey's directive "
                      "says where). PlayerListAtTest's stays in the server module on the rig, read from each player's turn.", ""]
        for h in homes:
            exists = (REPO / h).is_file()
            lines.append("## %s%s" % (h, "" if exists else "   (NEW FILE)"))
            lines.append("")
            for (hh, name), rs in tests.items():
                if hh != h:
                    continue
                surveys = sorted({r["survey"] for r in rs})
                lines.append("- **%s**" % name)
                lines.append("  - directive: in `at-survey/%s`, the entry whose \"Replaces:\" line names the old method%s below (its "
                             "heading may give another name, or several tests under one)%s" %
                             ("`, `at-survey/".join(surveys), "" if len(rs) == 1 else "s",
                              "" if len(surveys) == 1 else "; one entry in each file: they are one test"))
                for r in rs:
                    lines.append("  - replaces `at/%s`: `%s`  (%s; %s)" % (r["at"], r["method"], r["kind"], r["status"]))
                lines.append("  - what the OLD method checked (from Ketura's report; the directive, not this, says what to write): %s" % rs[0]["sentence"])
            lines.append("")
        (out / (key + ".md")).write_text("\n".join(lines) + "\n", encoding="utf-8")
    # The sweep: what is covered or dropped, and whatever else is left in at/ at the end.
    sweep = ["# at-convert batch `sweep` (LAST: run it when every other batch has landed)", "",
             "Delete from `at/` every old method below (each is covered by the test named, or was dropped by Ketura's word), then every "
             "old method that is still in `at/` for any other reason (say which, and why it was left), then the files that are empty, "
             "then `AbstractAtTest.java`, `effects/IncrementEffect.java` and the folder itself, with whatever in the test tree still "
             "names them. The whole suite must be OK afterwards.", ""]
    for r in gone:
        sweep.append("- `at/%s`: `%s`: %s" % (r["at"], r["method"],
                     ("covered by " + os.path.basename(r["home"]) + ("." + r["name"] if r["name"] not in ("", "-") else ""))
                     if r["status"] == "covered" else "dropped, no replacement"))
    (out / "sweep.md").write_text("\n".join(sweep) + "\n", encoding="utf-8")
    plan.append("- `sweep`: deletes the %d covered or dropped methods and what is left of the folder" % len(gone))
    (out / "PLAN.md").write_text("\n".join(plan) + "\n", encoding="utf-8")
    json.dump(list(batches.keys()) + ["sweep"], (out / "queue.json").open("w"))
    if "--done" in argv:
        print("done:", " ".join(done_keys) or "(none)")
    else:
        print("%d batches (+ sweep): %s" % (len(batches), " ".join(batches)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
