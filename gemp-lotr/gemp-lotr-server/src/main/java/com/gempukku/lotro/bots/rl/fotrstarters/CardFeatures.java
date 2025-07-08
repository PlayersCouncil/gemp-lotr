package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gempukku.lotro.common.Timeword.*;

public class CardFeatures {
    private static LotroCardBlueprintLibrary library = null;
    public static final String PASS = "pass";

    private CardFeatures() {

    }

    public static void init(LotroCardBlueprintLibrary library) {
        CardFeatures.library = library;
    }

    public static double[] getCardFeatures(String blueprintId, int woundsPlaced) throws CardNotFoundException {
        if (library != null) {
            try {
                LotroCardBlueprint blueprint = library.getLotroCardBlueprint(blueprintId);

                List<Double> features = new ArrayList<>();

                features.add(blueprint.getSide() == Side.SHADOW ? 1.0 : 0.0);
                features.add(blueprint.getSide() == Side.FREE_PEOPLE ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.COMPANION ? 1.0 : 0.0);
                features.add(blueprint.canStartWithRing() ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.ALLY ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.MINION ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.POSSESSION || blueprint.getCardType() == CardType.ARTIFACT ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.CONDITION ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.FOLLOWER ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(FELLOWSHIP) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(SHADOW) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(MANEUVER) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(ARCHERY) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(ASSIGNMENT) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(SKIRMISH) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(REGROUP) ? 1.0 : 0.0);
                features.add(blueprint.getCardType() == CardType.EVENT && blueprint.hasTimeword(RESPONSE) ? 1.0 : 0.0);

                features.add((double) blueprint.getTwilightCost());
                features.add((double) blueprint.getStrength());
                features.add((double) blueprint.getVitality());
                features.add((double) blueprint.getSiteNumber());

                features.add((double) woundsPlaced);

                return features.stream().mapToDouble(Double::doubleValue).toArray();
            } catch (CardNotFoundException e) {
                if (blueprintId.equals(PASS)) {
                    return new double[22];
                } else {
                    throw e;
                }
            }
        } else {
            throw new IllegalStateException("Blueprint library not initialized");
        }
    }

    public static double[] getFpAssignedCardFeatures(String blueprintId, int woundsPlaced, int numberOfMinions, int minionStrength) throws CardNotFoundException {
        // TODO items
        if (library != null) {
            LotroCardBlueprint blueprint = library.getLotroCardBlueprint(blueprintId);

            List<Double> features = new ArrayList<>();

            features.add(blueprint.getCardType() == CardType.COMPANION ? 1.0 : 0.0);
            features.add(blueprint.canStartWithRing() ? 1.0 : 0.0);
            features.add((double) blueprint.getTwilightCost());
            features.add((double) blueprint.getStrength());
            features.add((double) blueprint.getVitality());
            features.add((double) woundsPlaced);

            features.add(((double) numberOfMinions));
            features.add(((double) minionStrength));

            return features.stream().mapToDouble(Double::doubleValue).toArray();
        } else {
            throw new IllegalStateException("Blueprint library not initialized");
        }
    }

    public static double[] getAssignmentFeatures(Map<String, List<String>> assignmentMap, Map<String, Integer> woundsMap,
                                                 int numberOfUnassignedMinions, int strengthOfUnassignedMinions) throws CardNotFoundException {
        List<Double> tbr = new ArrayList<>();

        List<String> companions = assignmentMap.keySet().stream().filter(s -> {
            try {
                return library.getLotroCardBlueprint(s).getCardType().equals(CardType.COMPANION);
            } catch (CardNotFoundException e) {
                return false;
            }
        }).collect(Collectors.toList());
        List<String> allies = assignmentMap.keySet().stream().filter(s -> {
            try {
                return library.getLotroCardBlueprint(s).getCardType().equals(CardType.ALLY);
            } catch (CardNotFoundException e) {
                return false;
            }
        }).collect(Collectors.toList());

        companions.sort((o1, o2) -> {
            try {
                return Integer.compare(library.getLotroCardBlueprint(o1).getStrength(), library.getLotroCardBlueprint(o2).getStrength());
            } catch (CardNotFoundException e) {
                return 0;
            }
        });
        allies.sort((o1, o2) -> {
            try {
                return Integer.compare(library.getLotroCardBlueprint(o1).getStrength(), library.getLotroCardBlueprint(o2).getStrength());
            } catch (CardNotFoundException e) {
                return 0;
            }
        });

        while (companions.size() < 9) {
            companions.add(PASS);
        }
        while (allies.size() < 4) {
            allies.add(PASS);
        }

        companions.forEach(companion -> {
            if (companion.equals(PASS)) {
                double[] nullVector = new double[8];
                tbr.addAll(Arrays.stream(nullVector).boxed().collect(Collectors.toList()));
            } else {
                int strength = assignmentMap.get(companion).stream().mapToInt(value -> {
                    try {
                        return library.getLotroCardBlueprint(value).getStrength();
                    } catch (CardNotFoundException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }).sum();

                try {
                    double[] thisAssignment = getFpAssignedCardFeatures(companion, woundsMap.get(companion), assignmentMap.get(companion).size(), strength);
                    tbr.addAll(Arrays.stream(thisAssignment).boxed().collect(Collectors.toList()));
                } catch (CardNotFoundException e) {
                    double[] nullVector = new double[8];
                    tbr.addAll(Arrays.stream(nullVector).boxed().collect(Collectors.toList()));
                }
            }
        });

        allies.forEach(ally -> {
            if (ally.equals(PASS)) {
                double[] nullVector = new double[8];
                tbr.addAll(Arrays.stream(nullVector).boxed().collect(Collectors.toList()));
            } else {
                int strength = assignmentMap.get(ally).stream().mapToInt(value -> {
                    try {
                        return library.getLotroCardBlueprint(value).getStrength();
                    } catch (CardNotFoundException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }).sum();

                try {
                    double[] thisAssignment = getFpAssignedCardFeatures(ally, woundsMap.get(ally), assignmentMap.get(ally).size(), strength);
                    tbr.addAll(Arrays.stream(thisAssignment).boxed().collect(Collectors.toList()));
                } catch (CardNotFoundException e) {
                    double[] nullVector = new double[8];
                    tbr.addAll(Arrays.stream(nullVector).boxed().collect(Collectors.toList()));
                }
            }
        });

        tbr.add((double) numberOfUnassignedMinions);
        tbr.add((double) strengthOfUnassignedMinions);

        return tbr.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
