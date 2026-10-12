# Writing a finding's patch-note row

Patch notes are for players. Ketura, 2026-10-12: "Patch notes are for the end user, who is not technical and has
zero insight to the Q-/F-/G- system, nor Gemp itself. What they want to know is what changed from *their* side."
Her format, word for word (rulings-log.md, "Patch notes are for players" and "Patch notes, the sample"):

- "Every item should be a single statement about how a card or feature has changed in interaction. If it is not
  seeable on the client, it is not a patch note. Example: "Fixed [[Aragorn, King in Exile]] prompting the user when
  no companion could be healed.""
- "... the tallies of materially similar cards should be put together into buckets. If more than a handful of cards
  fit into the bucket, the entry should change: "Fixed 123 cards prompting the user with an optional trigger that
  would do nothing. Example: [[Aragorn, King in Exile]] no longer asks you to heal if there are no wounded
  companions with the Aragorn signet."" (A handful is 5.)
- "Straightforward implementation fixes should not belabor the fix. Example: "Fixed [[Old Differences]] being able
  to target any Dwarf or Elf, not just one in the skirmish.""
- "If one card had multiple fixes that do not end up in a bucket, they can be listed as subitems."
- "Certain changes aren't about a card, but a general client behavior. That should be described in a handful of
  concise sentences, and all of them should be bundled together at the top before card-specific fixes."
- "This may result in a large number of F- items not showing in the patch notes (or not being discernable), and
  that's okay."
- A PC errata: "[[id|Name]] (PC Errata)". Fixes to a card's displayed text alone go in a section of their own: "I'll
  be introducing those changes as "non-errata errata" that emphasize they're just visual in nature".

Her answers on the first full draft (Q-0294, 2026-10-12):

- A collector's number is "not visible anywhere except the image": `--visible n`.
- An item class is "visible in the right-click card readout": a text row, "Added missing 'Pony' item class."
- An erratum's link to its original (the parent fields of cardInfo) "is bookkeeping the user won't see": `n`.
- Errata rebuilt to her newer text "will be listed as proper errata, but they'll still be here too".
- The no-effect flag has its own New Feature line (the game row N-no-effect); a card that now shows it says "now
  marked with the no-effect border", nothing longer.
- "capitalization entries can all get put into one bucket, those don't need their own individual card lines": the
  text bucket `capitalization`, which prints as one line however few cards are in it (`--min 1`).
- One card answering the same moment more than once is a game change (F-0137), not a card fix.

## When

A change a player will see that no finding records (built on a question's answer: Q-0232's Ring left where the
Ring-bearer fell, Q-0262's withheld draw) gets a row of its own, `N-<short name>`, from whoever builds it.
Whoever sets a finding `fixed` or `fixed-approved` (the lander at landing, an engine agent closing its numbers, a
fixer) writes its row in the same step. A finding later reopened or reverted gets its row rewritten (`add` again, or
`--visible n` if nothing a player sees is left).

## How

    python3 .claude/skills/card-test-campaign/scripts/orchestrator/patchnotes.py buckets
    python3 .claude/skills/card-test-campaign/scripts/orchestrator/patchnotes.py add F-0123 --visible y \
        [--scope card|game|text] [--bucket SLUG] --cards "1_2;51_2" --line "Fixed [[...]] ..." [--short "..."]
    python3 .claude/skills/card-test-campaign/scripts/orchestrator/patchnotes.py add F-0124 --visible n

`add` checks the row and refuses it with the reasons (a link that names no card or several, our numbers or engine
words in the line, a PC errata link without "(PC Errata)", an unknown bucket). `render` shows the notes as they
stand.

- **visible.** `n` when a player could not tell: the definition changed and nothing plays differently, a fix moot
  with today's cards, a test or rig matter, a replay or server matter no screen shows. Text a player reads is
  visible: a card's displayed text, a prompt's wording, a deck builder's message.
- **scope.** `card` (most). `game` when the change is to how the game itself behaves for any card (a pop-up no
  longer shown, a kind of card that can no longer be hindered); write it as one or two plain sentences, no card
  link needed. `text` when only the card's displayed text changed.
- **bucket.** A bucket holds rows of one scope (card or text). Reuse a bucket when the fix is materially the same as its tally line says, whatever the card. Make
  one (`bucket <slug> "<tally with {n}>" "<example>"`) only when no bucket says it and more like it are likely; a
  one-off needs none. A bucket of 5 cards or fewer prints its lines one by one, so a bucket never hides a fix.
- **cards.** Every card the fix reaches: the finding's card, the original or errata fixed with it, and, for an
  engine fix, the cards the outcome names. The bucket's count is these.
- **line.** One sentence, past tense, "Fixed [[Card]] <doing what it should not>" or "<Card> no longer ..."; what
  the player saw, not why. The card's name as printed; an errata by id with "(PC Errata)" after the link; a name two
  different cards share, by id. No F-/Q-/G- numbers, no "definition", "filter", "engine", "test".
- **short.** The line without the card ("Fixed the on-play effect adding (1) and not (2)"), for when the card ends
  up with two or more lines outside buckets. `check` says when one is wanted.
