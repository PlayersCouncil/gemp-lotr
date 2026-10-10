# Brief: converting a batch of the old `at` tests

The folder `gemp-lotr/gemp-lotr-server/src/test/java/com/gempukku/lotro/at/` holds the game's oldest tests, most
of them written before the test rig existed. They are being replaced by tests written to the Card Test Standard,
and the folder emptied. A survey has already gone through every old method and written down what it guards and
where its replacement belongs. You write the replacements for ONE batch, named at the end of this brief, and
take the old methods out. After you comes a lander, who checks your work, files what you found and commits.

What Ketura asked for, in her words:

- "These tests are going to be a bit gnarly and opaque, so it may make more sense to just note what bug or
  behavior or interaction the test is handling, and then take that as a design directive when instantiating a
  wholesale new test." So you do NOT port an old method line by line. You write a new test from its directive.
- "Just wanted to ensure that we didn't forget certain tests which cover functionality that (so far) aren't
  covered elsewhere, and also to ensure that the improved version of those tests was inserted as a kickstart for
  the associated cards." So nothing an old test guards may be lost, and a test about one card goes into that
  card's own file as a lone test: the card's own batch, later, takes it as an existing test.
- "It's the tricky ones, the ones about specific interactions between cards, that I know are important and need
  keeping. (The rest are busy work)." So a test of an interaction, of a rule, or of a bug gets your care: the
  board that really shows it, the control beside it, every reading the directive names. A plain "this card does
  what it says" test is written simply (the first board, the readings that show the effect) and not polished.

## Read first

In one step, in full: `.claude/skills/test-generation/SKILL.md` (the Card Test Standard; what binds you is "Rules
every test follows", "Tests that are not about a card" and "When something is in the way"; the ledger and the
table of checks are for a card's whole treatment, which this is not);
`.claude/skills/gemp-testing/SKILL.md` (the rig; `references/vts-api.md` beside it lists every rig method:
search it, do not read it through); `.claude/skills/card-test-campaign/references/rulings-index.md` (every ruling
of Ketura's and every settled shape, by its first words). Then your batch file,
`/mnt/user-data/outputs/card-tests/orchestrator/at-convert/<your batch>.md`, and
`/mnt/user-data/outputs/card-tests/orchestrator/at-survey/DECISIONS.md` (what the survey asked and what was
answered: it overrides a directive where the two differ). Look things up as you need them:

    python3 .claude/skills/card-test-campaign/scripts/lookup.py <number or words>     # a ruling, a shape, a pitfall
    python3 .claude/skills/card-test-campaign/scripts/packet.py <id> --blind          # a card's text and rulings
    python3 .claude/skills/card-test-campaign/scripts/packet.py --defs <ids>          # definitions of helper cards
    python3 .claude/skills/card-test-campaign/scripts/rules.py term <word>            # the rules documents

Every step you take re-reads everything you have read so far: do several things in one step where you can. The
machine is small and has restarted under load: run ONE Java process at a time, and never the whole suite.

## For each test in your batch file

The batch file lists your homes (a `mechanics/` class, or a card's test file) and, under each, the tests to
write: the test's name, where its directive is (the entry of `at-survey/<File>.md` whose "Replaces:" line names the old
method; its heading may give another name, or put several tests under one), the old methods it replaces, and one
sentence on what the OLD method checked (that sentence is from Ketura's report: the directive, not it, says what
to write). The names are the survey's: where a directive offers "N tests or N passes over one board"
and the board cannot hold the passes (a card played in one changes what the next would read), write as many tests
as it takes, and say in your report which of your tests stand for which name of the batch file.

1. **Read the directive**, and the cards' texts it names (`packet.py <id> --blind`; a Hobbit-set card, sets 30 to
   33, has no sheet and often no game text, so read its definition: `packet.py --defs <id>`), and any ruling it cites.
   (A directive that reads the twilight pool or the mulligan question after a starting fellowship was written
   before the pitfall "A starting fellowship" was sharpened: `lookup.py "A starting fellowship"` first.)
   Read the old method too, once: not to copy its lines, but to see that the directive lost nothing it asserted.
   If it did (the old method reads something the directive does not), your test reads it as well, or you say why
   it should not (a ruling contradicts it; it asserted the rig, not the game).
2. **Write the test** to the Standard's rules for any test: the rig's own methods and no reaching past them; a
   check of what the game is waiting for after the last play or answer; real cards, on a board a game could
   reach (no card given a race, culture or type it cannot have; never two cards of one unique title in play; no
   Frodo beside a Ring-bearer who is not Frodo); no test that relies on an exception; a name that says what is
   shown (take the directive's unless it is wrong). Each scenario is a `Get...Scenario()` method of the class.
   - **In a `mechanics/` class** (read two that are there for the manner, `HinderedSourceTests` and one this
     conversion has landed, such as `ToilTests`; not every older class passes `lint.py --form`): the class comment cites the rules passage the class is about; the cards are plain
     Decipher cards whose own behaviour is not in question; there is no ledger. Where the class exists, your
     tests join it and its comment is widened if they widen its subject. `lint.py --form <the file>` must pass.
   - **In a card's file** (`Card_<set>_<number>_Tests`): the file is there already, usually holding only the
     stats test and the skeleton's placeholders. Add your test and its scenario; take the skeleton's placeholder
     method and its "put other cards in here" note out when you add the first real test; touch nothing else
     (not the stats test, not its header; the skeleton lacks imports a real test needs: add
     `import static com.gempukku.lotro.framework.Assertions.*;`, `import org.junit.Ignore;` if you park one, and
     whatever else your test uses (some skeletons import the common classes one by one, so `Zone`, `Phase` and
     their like are added by name), and the skeleton's commented-out sample test may go with its placeholder). No ledger, no
     landing: this is a kickstart.
     `lint.py --form <the file> --only <YourTest,YourOtherTest>` must pass. A file the batch marks NEW FILE is
     made in the manner of its neighbours (for a Hobbit-set card the batch file says how).
3. **Run it**: `python3 .claude/skills/card-test-campaign/scripts/cardtest.py run <the file> --out <your folder>`.
   When it fails, decide which of three things is wrong, in this order: the test (you drove the rig wrongly, or
   a helper card did something you did not account for: fix it); your expectation (check the rules and the
   rulings; if they settle it, fix the test; if they do not, it is a question: write the test for the reading
   the directive gives, park it with `@Ignore("Q-0000: <one line>")`, and report both readings); or the game
   (the text and the rules are clear and the game does something else: leave the test asserting what the text
   says, park it with `@Ignore("F-0000: <one line>")`, and report it as a finding with what the text says, what
   happened, and the correction if it is a definition's: the lines to change, tried with
   `mutate.py <id> --replace "<wrong>" "<right>" --with-parked --out <your folder>` where the card has a test
   file). Be slow to conclude the game is wrong; and never make a test pass by asserting what you did not
   expect.
   **Where an old board asserted something a ruling now contradicts** (the directive says "by the ruling": most
   often an offer made with nothing for it to act on, which Q-0034 says is not made): the mechanics test is
   written on a board where the thing has something to do, and stays active. The ruling's own assertion is about
   ONE CARD, and you write it as a lone test in that card's own file (you may touch that file for this, though
   your batch does not name it): the board on which the ruling bites, asserting what the ruling says. If the game
   does not do it, that test is parked with `@Ignore("F-0000: ...")` and reported as a finding with the
   definition's correction, like any other. Look the ruling up first (`lookup.py Q-0034`, and the entries near
   it): not every "may" with nothing to act on is withheld. An optional DRAW is still offered with an empty draw
   deck (Q-0029), and an action with a COST is offered and flagged, not withheld.
   **A fault met in passing**, in the definition of a card that is none of your homes (a helper that shows the
   wrong words, a sister card with the same wrong lines): report it under `findings` with the lines and the
   correction. If the board that shows it is already in your hands, you may write the lone test in that card's
   own file, parked with `@Ignore("F-0000: ...")`; if not, write none: the lander writes it when it corrects the
   card.
4. **A reading the rig cannot give** is not worked round: no helper method, no reaching into the engine, no
   exception relied on. Leave that one reading out, put a comment where it would stand
   (`// waits: G-0000 <what the rig lacks>`), and report it under `rig` with the method you would want.
5. **Take the old methods out.** When a test of yours is written and runs (or is parked as above), delete from
   `at/` the old methods the batch file says it replaces, with any import and helper only they used. An old
   method that is commented out goes with its commented `@Test` line. Tidy what a cut leaves: a blank line after
   the class brace, a doubled blank line, a note that stood above the method, an import only it used. Keep each file's line endings as they are
   (`at/TriggersAtTest.java`, `at/AssignmentAtTest.java` and `at/DiscountAtTest.java` are CRLF throughout, and others may be:
   look before you cut; a cut made in text mode would rewrite every line). Delete an
   `at` file that has no test left (never `AbstractAtTest.java` or `effects/IncrementEffect.java`: the last batch
   takes those). Then make sure what is left of each file you cut still compiles and passes:
   `cardtest.py run <the at file> --out <your folder>`. An old method whose replacement you could not write
   stays where it is, and you say so.

## What you may touch

The homes your batch names; the `at/` files your batch's old methods are in (and a file under
`src/test/resources/` that only a deleted old method loaded, which goes with it once a grep shows nothing else
loads it); scratch files under `/tmp/<your folder>/`
(or the scratchpad directory your session names, if it names one).
Nothing else: not a card's definition (show the correction, the lander applies it), not the engine, not the rig,
not a script, not a document, not git.

## What you return

For each home: the tests written (and for each, active or parked on what), the old methods deleted, and the last
line of its `lint.py --form` and `cardtest.py run`. Then: `findings` (each with the card, what the text or rule
says, what the game did, the correction or the engine lines at fault, and whether it looks like an error or
looks deliberate); `questions` (the sentence, what is built, reading A and reading B with what each would
change, which test is parked); `rig` (what could not be read, the method wanted); `left` (old methods not
converted, and why); and what in the documents was wrong, unclear or missing for this work, one line each.
