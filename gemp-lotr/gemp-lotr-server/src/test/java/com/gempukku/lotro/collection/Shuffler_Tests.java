package com.gempukku.lotro.collection;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Tidings of Erebor
public class Shuffler_Tests
{
    protected VirtualTableScenario Get60CardScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("1", "1_1");
                    put("2", "1_2");
                    put("3", "1_3");
                    put("4", "1_4");
                    put("5", "1_5");
                    put("6", "1_6");
                    put("7", "1_7");
                    put("8", "1_8");
                    put("9", "1_9");
                    put("10", "1_10");
                    put("11", "1_11");
                    put("12", "1_12");
                    put("13", "1_13");
                    put("14", "1_14");
                    put("15", "1_15");
                    put("16", "1_16");
                    put("17", "1_17");
                    put("18", "1_18");
                    put("19", "1_19");
                    put("20", "1_20");
                    put("21", "1_21");
                    put("22", "1_22");
                    put("23", "1_23");
                    put("24", "1_24");
                    put("25", "1_25");
                    put("26", "1_26");
                    put("27", "1_27");
                    put("28", "1_28");
                    put("29", "1_29");
                    put("30", "1_30");
                    put("31", "1_31");
                    put("32", "1_32");
                    put("33", "1_33");
                    put("34", "1_34");
                    put("35", "1_35");
                    put("36", "1_36");
                    put("37", "1_37");
                    put("38", "1_38");
                    put("39", "1_39");
                    put("40", "1_40");
                    put("41", "1_41");
                    put("42", "1_42");
                    put("43", "1_43");
                    put("44", "1_44");
                    put("45", "1_45");
                    put("46", "1_46");
                    put("47", "1_47");
                    put("48", "1_48");
                    put("49", "1_49");
                    put("50", "1_50");
                    put("51", "1_51");
                    put("52", "1_52");
                    put("53", "1_53");
                    put("54", "1_54");
                    put("55", "1_55");
                    put("56", "1_56");
                    put("57", "1_57");
                    put("58", "1_58");
                    put("59", "1_59");
                    put("60", "1_60");
                }}
        );
    }

    private class RunResult {
        public ArrayList<String> IDs;
        public ArrayList<String> CoercedIDs;
        public int HandNumber;
        public String Hash;
        public int Count;
    }

    private boolean Run = true;
    private int iterations = 10_000;

    @Test
    public void IdealShuffleTest() throws DecisionResultInvalidException, CardNotFoundException, IOException {
        if(!Run)
            return;
        //Pre-game setup
        VirtualTableScenario scn = Get60CardScenario();

        Map<String, Integer> results = new HashMap<>();

        for(int i = 0; i < iterations; ++i)
        {
            scn.ShuffleFreepsDeck();
            for(int j = 0; j < 8; j++)
            {
                scn.FreepsDrawCard();
            }

            ArrayList<String> hand = new ArrayList<>(scn.GetFreepsHand().stream()
                    .map(card -> Integer.valueOf(card.getBlueprintId().split("_")[1]))
                    .map(String::valueOf)
                    .toList());

            ArrayList<String> coercedHand = new ArrayList<>(scn.GetFreepsHand().stream()
                    .map(card -> Integer.valueOf(card.getBlueprintId().split("_")[1]))
                    .sorted()
                    .map(id -> String.format("%02d", id % 15))
                    .toList());

            String hashHand = String.join("_", new ArrayList<>(scn.GetFreepsHand().stream()
                    .map(card -> Integer.valueOf(card.getBlueprintId().split("_")[1]))
                    .map(id -> String.format("%02d", id))
                    .sorted()
                    .toList()));

            int finalI = i;
            var result = new RunResult() {{
               IDs = hand;
               CoercedIDs = coercedHand;
               HandNumber = finalI;
               Hash = hashHand;
               Count = 1;
            }};

            scn.MoveCardsToTopOfDeck(scn.GetFreepsHand().toArray(new PhysicalCardImpl[8]));

            String combo = String.join("\t", hand);
            if(results.containsKey(combo)) {
                results.put(combo, results.get(combo) + 1);
            }
            else {
                results.put(combo, 1);
            }
        }

        Path file = Paths.get("IdealShuffleResult.txt");
        System.out.println(file);
        String content = results
                .entrySet().stream()
                .map(x ->  x.getValue().toString() + "x" + x.getKey())
                .sorted()
                .collect(Collectors.joining("\n"));
        Files.write(file, Collections.singleton(content), StandardCharsets.UTF_8);

    }

    //@Test
    public void ScenarioRebootTest() throws DecisionResultInvalidException, CardNotFoundException, IOException {
        if(!Run)
            return;
        //Pre-game setup


        Map<String, Integer> results = new HashMap<>();

        for(int i = 0; i < iterations; ++i)
        {
            VirtualTableScenario scn = Get60CardScenario();
            scn.ShuffleFreepsDeck();
            for(int j = 0; j < 8; j++)
            {
                scn.FreepsDrawCard();
            }

            ArrayList<String> hand = new ArrayList<>(scn.GetFreepsHand().stream()
                    .map(card -> Integer.valueOf(card.getBlueprintId().split("_")[1]))
                    .sorted()
                    .map(String::valueOf)
                    .toList());

            String combo = String.join("|", hand);
            if(results.containsKey(combo)) {
                results.put(combo, results.get(combo) + 1);
            }
            else {
                results.put(combo, 1);
            }
        }

        Path file = Paths.get("ScenarioRebootResult.txt");
        String content = results
                .entrySet().stream()
                .map(x ->  x.getValue().toString() + "x" + x.getKey())
                .sorted()
                .collect(Collectors.joining("\n"));
        Files.write(file, Collections.singleton(content), StandardCharsets.UTF_8);

    }

    //@Test
    public void ScenarioInitReshuffleTest() throws DecisionResultInvalidException, CardNotFoundException, IOException {
        if(!Run)
            return;
        //Pre-game setup


        Map<String, Integer> results = new HashMap<>();

        for(int i = 0; i < iterations; ++i)
        {
            VirtualTableScenario scn = Get60CardScenario();
            scn.StartGame();

            ArrayList<String> hand = new ArrayList<>();

            for (String card : scn.GetFreepsHand().stream().map(PhysicalCard::getBlueprintId).sorted().toList()) {
                hand.add(card);
            }

            String combo = String.join("|", hand);
            if(results.containsKey(combo)) {
                results.put(combo, results.get(combo) + 1);
            }
            else {
                results.put(combo, 1);
            }
        }

        Path file = Paths.get("ScenarioInitReshuffle.txt");
        String content = results
                .entrySet().stream()
                .map(x -> x.getKey() + "," + x.getValue().toString())
                .collect(Collectors.joining("\n"));
        Files.write(file, Collections.singleton(content), StandardCharsets.UTF_8);

    }

}