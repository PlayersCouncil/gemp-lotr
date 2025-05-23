package com.gempukku.lotro.draft3.evaluator;

import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.ReplayMetadata;
import com.gempukku.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Reads all replay summaries, filters the one we are interested in, and prints analysis to console.
 * Intended use:
 *      1) analyze draft games, figure out pick rates of cards so the draft bots have something to based their picks on,
 *      2) analyze draft games, figure out play rates of cards, so we can cut bad cards from draft pools.
 * Should be use only when needed to alter draft json files, not at every server startup.
 */
public class Evaluator {
    static class Assignment {
        public final Map<String, Integer> winningMap = new HashMap<>();
        public final Map<String, Integer> losingMap = new HashMap<>();
        public final Map<String, Integer> deckCount = new HashMap<>();
        public int gamesAnalyzed = 0;
        public final String format;
        public final String tournament;

        public Assignment(String format, String tournament) {
            this.format = format;
            this.tournament = tournament.toLowerCase();
        }
    }

    /**
     * Call this to analyze the games - ran for ~ 10 minutes locally in docker
     * @param library library instance
     */
    public static void readFiles(LotroCardBlueprintLibrary library) {
        Path summaryDir = Paths.get(AppConfig.getProperty("application.root"), "replay", "summaries");

        List<Assignment> assignmentList = new ArrayList<>();
        // Define what we care for
        assignmentList.add(new Assignment("Limited - FOTR", "draft"));
        assignmentList.add(new Assignment("Limited - TTT", "draft"));
        assignmentList.add(new Assignment("Limited - Hobbit", "draft"));

        System.out.println("Game history reading started at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));
        // Load decks from the past
        try (Stream<Path> paths = Files.walk(summaryDir)) {
            paths.filter(Files::isRegularFile) // Keep only regular files
                    .filter(path -> path.toString().endsWith(".json")) // Filter only JSON files
                    .forEach(path -> analyzeSummary(path, assignmentList)); // Process each JSON file
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Game history reading finished at " + new SimpleDateFormat("HH.mm.ss").format(new Date()));

        FotrDraftCardEvaluator fotrDraftCardEvaluator = new FotrDraftCardEvaluator(library);
        TttDraftCardEvaluator tttDraftCardEvaluator = new TttDraftCardEvaluator(library);
        HobbitDraftCardEvaluator hobbitDraftCardEvaluator = new HobbitDraftCardEvaluator(library);
        // Send the loaded data to card evaluators -> they convert card counts to values from 0.0 to 1.0
        // This is what bots pick according to in FotR drafts
        var fotrValues = fotrDraftCardEvaluator.getValuesMap(assignmentList.getFirst().winningMap, assignmentList.get(0).losingMap, assignmentList.getFirst().gamesAnalyzed);
        // Those are values to base fotr draft pools cuts on
        var fotrPlayRates = fotrDraftCardEvaluator.getPlayRateMap(assignmentList.getFirst().deckCount, assignmentList.getFirst().gamesAnalyzed);

        // This is what bots pick according to in TTT drafts
        var tttValues = tttDraftCardEvaluator.getValuesMap(assignmentList.get(1).winningMap, assignmentList.get(1).losingMap, assignmentList.get(1).gamesAnalyzed);
        // Those are values to base TTT draft pools cuts on
        var tttPlayRates = tttDraftCardEvaluator.getPlayRateMap(assignmentList.get(1).deckCount, assignmentList.get(1).gamesAnalyzed);

        var hobbitValues = hobbitDraftCardEvaluator.getValuesMap(assignmentList.get(2).winningMap, assignmentList.get(2).losingMap, assignmentList.get(2).gamesAnalyzed);

        // Data from solo drafts do not contain rare one ring cards, don't forget to add some value manually after
        System.out.println("===FOTR VALUES");
        System.out.println("Games analyzed: " + assignmentList.get(0).gamesAnalyzed);
        printJsonStyle(fotrValues); // For json file
        System.out.println("===FOTR PLAY RATES");
        // Regular draft - all rares included, U/C with play rate lower than 0.01 are cut
        // Power draft - R with play rate lower than 0.02 and U/C with play rate lower than 0.05 are cut
        printHumanReadable(fotrPlayRates, library);

        System.out.println("===TTT VALUES");
        System.out.println("Games analyzed: " + assignmentList.get(1).gamesAnalyzed);
        printJsonStyle(tttValues);
        System.out.println("===TTT PLAY RATES");
        printHumanReadable(tttPlayRates, library);

        System.out.println("===HOBBIT VALUES");
        System.out.println("Games analyzed: " + assignmentList.get(2).gamesAnalyzed);
        printJsonStyle(hobbitValues);
        printHumanReadable(hobbitValues, library);
    }

    private static void analyzeSummary(Path jsonFile, List<Assignment> assignmentList) {
        try {
            // Read JSON file into a string
            String json = Files.readString(jsonFile);

            // Convert JSON to object
            ReplayMetadata summary = JsonUtils.Convert(json, ReplayMetadata.class);
            assignmentList.forEach(assignment -> {
                // Look at only games assignment cares about
                if (summary.GameReplayInfo != null && assignment.format.equals(summary.GameReplayInfo.format_name) && summary.GameReplayInfo.tournament.toLowerCase().contains(assignment.tournament)) {
                    ReplayMetadata.DeckMetadata winningDeck = summary.Decks.get(summary.GameReplayInfo.winner);
                    ReplayMetadata.DeckMetadata losingDeck = summary.Decks.get(summary.GameReplayInfo.loser);

                    if (winningDeck != null && losingDeck != null) {
                        assignment.gamesAnalyzed++;

                        assignment.deckCount.merge(winningDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                        if (winningDeck.Ring != null) {
                            assignment.deckCount.merge(winningDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                        }
                        Set<String> winningCardSet = new HashSet<>(winningDeck.DrawDeck);
                        winningCardSet.forEach(card -> assignment.deckCount.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                        winningDeck.AdventureDeck.forEach(siteCard -> assignment.deckCount.merge(siteCard.replace("*", "").replace("T", ""), 1, Integer::sum));

                        assignment.deckCount.merge(losingDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                        if (losingDeck.Ring != null) {
                            assignment.deckCount.merge(losingDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                        }
                        Set<String> losingCardSet = new HashSet<>(losingDeck.DrawDeck);
                        losingCardSet.forEach(card -> assignment.deckCount.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                        losingDeck.AdventureDeck.forEach(siteCard -> assignment.deckCount.merge(siteCard.replace("*", "").replace("T", ""), 1, Integer::sum));

                        assignment.winningMap.merge(winningDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                        if (winningDeck.Ring != null) {
                            assignment.winningMap.merge(winningDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                        }
                        winningDeck.DrawDeck.forEach(card -> assignment.winningMap.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                        winningDeck.AdventureDeck.forEach(siteCard -> assignment.winningMap.merge(siteCard.replace("*", "").replace("T", ""), 1, Integer::sum));

                        assignment.losingMap.merge(losingDeck.RingBearer.replace("*", "").replace("T", ""), 1, Integer::sum);
                        if (losingDeck.Ring != null) {
                            assignment.losingMap.merge(losingDeck.Ring.replace("*", "").replace("T", ""), 1, Integer::sum);
                        }
                        losingDeck.DrawDeck.forEach(card -> assignment.losingMap.merge(card.replace("*", "").replace("T", ""), 1, Integer::sum));
                        losingDeck.AdventureDeck.forEach(siteCard -> assignment.losingMap.merge(siteCard.replace("*", "").replace("T", ""), 1, Integer::sum));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace(); // Handle file reading exceptions
        }
    }

    private static void printJsonStyle(Map<String, Double> toPrint) {
//        {"card": "1_90" , "value": 0.9832224243314389  },
        toPrint.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .forEach(stringDoubleEntry -> {
                    String s = "{\"card\": \"" +
                            stringDoubleEntry.getKey() +
                            "\", \"value\": " +
                            stringDoubleEntry.getValue() +
                            "},";
                    System.out.println(s);
                });
    }

    private static void printMapStyle(Map<String, Double> toPrint) {
//        tbr.put("4_179", 0.8343093340676018);
        toPrint.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .forEach(stringDoubleEntry -> {
                    String s = "tbr.put(\"" +
                            stringDoubleEntry.getKey() +
                            "\", " +
                            stringDoubleEntry.getValue() +
                            ");";
                    System.out.println(s);
                });
    }

    private static void printHumanReadable(Map<String, Double> toPrint, LotroCardBlueprintLibrary library) {
//        33_44 Lavastone Giant: 0.8333787095017696
        toPrint.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .forEach(stringDoubleEntry -> {
                    try {
                        String s = stringDoubleEntry.getKey() + " "
                                + library.getLotroCardBlueprint(stringDoubleEntry.getKey()).getFullName() + ": " +
                                stringDoubleEntry.getValue();
                        System.out.println(s);
                    } catch (CardNotFoundException ignore) {

                    }
                });
    }
}
