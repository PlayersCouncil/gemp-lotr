---
name: card-test-campaign
description: >
  Ketura's settled rulings on how LotR TCG card text is read in Gemp, the definition and test shapes the card test
  campaign settled, and the per-card tools (packet, lint, mutate, cardtest, rules lookup, sheet reconcile). Use this
  skill whenever writing, fixing, reviewing or testing a Gemp card definition or its Card_*_Tests file, when
  deciding what a card's text means, when looking up a ruling or a finding or question by number (Q-0000,
  F-0000, G-0000), or when asked about the card test campaign. Use test-generation (the Standard), gemp-testing
  (the rig) and hjson-reference alongside it.
---

# Card work: rulings, settled shapes and the per-card tools

The card test campaign (2026-10-04 to 2026-11-05) gave every Gemp-LotR card, Decipher, PC and Hobbit Draft Game,
a test file to the Card Test Standard (the test-generation skill), and fixed what it found on the way: 833
findings, 419 questions put to Ketura, 79 engine passes. What it learned is in this skill, and it binds anyone who
writes or tests a card now. The machinery that ran the campaign (pipelines, landings, bundles, the tracker) is
archived in `archive/orchestration/` (its README says how to revive it).

## What an expectation rests on, in order

1. **Ketura's rulings**, `references/rulings-log.md`, in Ketura's words, by subject, dated. Ketura knows this
   game's rulings better than any document.
2. **Settled shapes**, `references/shapes.md`: what those rulings and the engine's fixes mean for a definition and
   a test (the HJSON a shape needs, the board that shows it, what the engine does). A definition that departs
   from an entry is a fault. A shape listed there is not raised as a question again.
3. **The rules**: Comprehensive Rules 4.2 and the CR 5.0 excerpts, through `scripts/rules.py` (the
   game-mechanics skill has the documents), and the Rules Councils' rulings (`rules.py council`).
4. **The card's text**: the reviewed sheet for Decipher cards, the V-sheets and errata sheet for PC cards, and
   for the Hobbit Draft Game (sets 30 to 33) the card picture (`references/notes-hobbit.txt`).

Find an entry through `references/rulings-index.md` (every entry by its first words; generated) or directly:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py Q-0098
    python3 .claude/skills/card-test-campaign/scripts/lookup.py "support area" hindered
    python3 .claude/skills/card-test-campaign/scripts/lookup.py prevent --in pitfalls

A number by itself also prints the finding's row or the question's entry from the registers (the campaign state
when present, else `archive/`). An expectation rests on an entry's own words, never on its line in the index.

Per-phase notes: `references/notes-decipher.txt`, `notes-errata.txt`, `notes-rtmd.txt`, `notes-hobbit.txt`.

## One card, start to finish

The author's brief, `roles/author.md`, is the full procedure; the reviewer's, `roles/review-and-fix.md`, is how a
finished card is checked. In short:

1. **Blind**: `packet.py <id> --blind --terms`. Work out what the card should do from its text, the rules and the
   rulings alone, before reading the definition: cut the text into clauses (the ledger, test-generation's "The
   ledger"), and for each check write the board and the outcome the text calls for. Every doubt is a question.
2. **Sighted**: `packet.py <id> --only-withheld`. Read the definition and the existing tests against that. An
   existing test claims a check, is redundant, knows something the ledger missed, or contradicts it (then it is
   a question: old PC tests often encode a ruling).
3. **Write and run**: the test file, `lint.py <id> --ledger`, `cardtest.py run <id> --out <name>`. A failing test is looked
   at three ways before anything else: the test wrong, the expectation wrong, or the game wrong.
4. **Prove the tests**: `mutate.py <id> --replace <old> <new>` makes one mistake in a copy of the definition and
   shows whether a test notices; use it to settle a suspected gap you cannot settle by reading.
5. After a change to the engine or the rig, `cardtest.py suite all`.

A definition that does not do what its text says is fixed where it is found (Ketura, 2026-10-07): "the text
should do what it says it does, and I think you only need to ask if you find an obvious intentional break from
the text rather than what appears to be an error or typo. Or if you are uncertain, of course." Ask where the
departure looks deliberate (a workaround, a comment, a ruling that might explain it) or the text's meaning is in
doubt.

**Questions and new rulings.** Put a question to Ketura with the card, the two readings and what each would mean;
do not guess at a niche ruling. An answer is a ruling for every later card: add it to `rulings-log.md` under its
subject, with its date, in Ketura's words, and what it means for definitions and tests to `shapes.md` (one block
per entry, a bold lead naming the shape with its ids, then the rule; `- Build:` / `- Test:` lines inside the
block). Then `lookup.py --index`, or the entry is not found.

## Tools

All under `scripts/`, run with `python3` from anywhere in the repository; each prints its own help. Selectors
work wherever an id does: `set:1`, `file:set01-Elven`, `seg:vset`.

| Script | For |
|---|---|
| `packet.py <id>` | Everything about one card: sheet text (and for HDG cards the picture's transcription), definition, rules terms, rulings that name it. `--blind` leaves out the implementation, `--only-withheld` is what blind held back, `--with-test` adds the test file and ledger. `--find <regex>` and `--title <name>` find helper cards by what is printed on them; `--defs <ids>` prints definitions. |
| `lint.py <id>` | The conformance check against the Standard. `--form <file.java>` for a class with no ledger (a `mechanics/` test). |
| `cardtest.py` | `setup` once, then `run <ids> --out <name>` in seconds; `suite` (cards, mechanics, definitions, the rig), `suite all` (every server test), `probe <file.java>` for a throwaway class. |
| `mutate.py <id>` | Deliberate mistakes in a copy of the definition: `--replace <old> <new>` (`--every`), `--with-parked`, `--test <file.java>`. `applyfix.py <script>` writes what a mutate script tried into the definition. |
| `rules.py` | Comprehensive Rules 4.2, the 5.0 excerpts and the council rulings: `term`, `card`, `terms-for`, `grep`, `interactions`, `council`. |
| `reconcile.py <ids>` | A card's sheet row against its definition, field by field. |
| `cardinfo.py <ids>` | A definition's cardInfo block against the rest of it and the cards of its kind. |
| `census.py <ids>` | What the game text names against what the definition names. |
| `gen_play.py <ids>` | Whether a card's play from hand is a plain one (lint's `plain-play`). |
| `twins.py` | Decipher cards whose text is the same but for names and cultures (Q-0220: H): fix and test them together. |
| `gen_vts_api.py` | Rewrites the rig's API reference from source; `--check` for drift. |
| `lookup.py` | Whole entries of the rulings, shapes, the rig's pitfalls and helpers; `--index` rebuilds the index. |
| `deltest.py <file> <method>...` | Removes whole test methods from a Java test file. |

**Sheets.** `generator_sheets/` at the repository root (or `GEMP_CARD_SHEETS`), not in git:
`gen_en_standard.tsv` (Decipher, reviewed), `V1.tsv` to `V3.tsv`, `master_errata.tsv` (about 95% accurate: rows
Ketura has said were never released are in `references/unreleased-sheet-rows.tsv` and the tools skip them),
`CardDataHobbit_modified_final.csv` with `hobbit_card_images.tsv` (the HDG sheet and the pictures' transcription;
`references/hobbit-sheet-map.tsv` maps HDG ids to Gemp ids). The tools match a definition to its row by revision.

**Ledgers.** A ledger (`ledgers/<set>/<id>.txt`: the card's text cut into clauses, with the tests that claim each
check) is working material for tests being written afresh, kept until they are approved and then let go; it is
never committed, and nothing expects one on a clean clone (test-generation, "The ledger"). It is written to the
working folder: `GEMP_CAMPAIGN_STATE`, else the path in a `.campaign-state` file at the repository root, else
`Claude outputs/card-tests` (git-ignored). `packet.py <id>` prints the path. `lint.py <id> --ledger` holds the
tests to it; without `--ledger`, a card with no ledger gets only the rules about how a test is written. The
campaign's own ledgers are kept on Ketura's machine, outside the repository.

## Git

Never push. On Ketura's machine never check out, pull, or run `git status` (the working tree is too large on that
mount). Commit messages and comments name Ketura ("Ketura's ruling"), never "her"/"she". Work done in a cloud
clone reaches Ketura's branch as a git bundle fetched into the branch ref; `archive/orchestration/CAMPAIGN.md`
has the method.

## Reference

- `references/rulings-log.md`, `shapes.md`, `rulings-index.md`: above.
- `references/patch-notes.md`: Ketura's format for a patch-note row of a fixed finding.
- `references/known-differences.tsv`: settled differences between a sheet and a definition.
- `references/plainplay-keep.tsv`: plays kept under a `P1` clause though plain (lint reads it).
- `archive/`: the full registers (findings.tsv, questions.md, rig-requests.md) and `orchestration/`.
