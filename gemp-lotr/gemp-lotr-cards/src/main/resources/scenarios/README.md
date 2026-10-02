# Scenarios

Scripted games: tutorials, puzzles and sandboxes. One `.hjson` file per scenario, in whatever subfolders are
convenient; campaign files (ordered lists of scenarios) go in `campaigns/`.

Every scenario file in this folder is played through headless by `ScenarioCanonTest` as part of the ordinary test
run, so a scenario that has fallen out of step with the cards or the rules fails the build.

The file format is described at the top of `ScenarioParser` (gemp-lotr-server, package `com.gempukku.lotro.scenario`).
The fullest worked example is `tutorials/basics-01-into-the-wild.hjson` here: three turns, a choice of two ways
through its last one, and a `note` on everything that is not obvious. A short one is
`gemp-lotr-server/src/test/resources/scenarios/into-the-wild-opening.hjson`.

Scenarios can be written and changed in the game itself: Play > Scenarios, then Edit (admins). What that saves is
the scenario written out afresh (`ScenarioWriter`), so anything said in a comment is lost; say it in a `note`
(on the scenario, a step or a message), which is part of the scenario and is kept. Files in this folder are in the
form the writer gives them, so that a save changes only what was changed.

To see what a scenario's messages say with the game's numbers filled in, and every decision the game asks around
them: `mvn test -pl gemp-lotr-server -Dtest=ScenarioCanonTest -DargLine="-Dscenario.explore=<part of the file name>"`.
