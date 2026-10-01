# Scenarios

Scripted games: tutorials, puzzles and sandboxes. One `.hjson` file per scenario, in whatever subfolders are
convenient; campaign files (ordered lists of scenarios) go in `campaigns/`.

Every scenario file in this folder is played through headless by `ScenarioCanonTest` as part of the ordinary test
run, so a scenario that has fallen out of step with the cards or the rules fails the build.

The file format is described at the top of `ScenarioParser` (gemp-lotr-server, package `com.gempukku.lotro.scenario`).
A worked example is `gemp-lotr-server/src/test/resources/scenarios/into-the-wild-opening.hjson`.
