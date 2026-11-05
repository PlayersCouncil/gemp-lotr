# Campaign archive

The raw record of the card test campaign (2026-10-04 to 2026-11-05), exported intact before the branch history was
condensed, so that nothing in it depends on the campaign's state repository surviving.

- `findings.tsv`: every finding (F-), with its card, what was found, and how it was closed.
- `questions.md`: every question (Q-) put to Ketura, with the case as filed and the answer as given.
- `rig-requests.md`: every request for a capability of the test rig.

These are for provenance: what was asked, what was found, in full. To write or test a card, read
`references/rulings-log.md` (Ketura's rulings) and `references/shapes.md` (what they mean for a definition and a
test), found through `references/rulings-index.md` and `scripts/lookup.py`. The per-card ledgers (which clause of a
card's text each test covers) stayed in the state repository and are not part of this export.
