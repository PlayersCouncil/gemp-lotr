package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gempukku.lotro.common.Timeword.*;

public class CardFeatures {
    private static LotroCardBlueprintLibrary library = null;

    private CardFeatures() {

    }

    public static void init (LotroCardBlueprintLibrary library) {
        CardFeatures.library = library;
    }

    public static double[] getCardFeatures(String blueprintId, int woundsPlaced) throws CardNotFoundException {
        if (library != null) {
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
        } else {
            throw new IllegalStateException("Blueprint library not initialized");
        }
    }

    public static double[] getFpAssignedCardFeatures(String blueprintId, int woundsPlaced, int numberOfMinions, int minionStrength) throws CardNotFoundException {
        if (library != null) {
            double[] standard = getCardFeatures(blueprintId, woundsPlaced);
            double[] additionalInfo = new double[2];
            additionalInfo[0] = numberOfMinions;
            additionalInfo[1] = minionStrength;

            double[] extended = Arrays.copyOf(standard, standard.length + additionalInfo.length);
            System.arraycopy(additionalInfo, 0, extended, standard.length, additionalInfo.length);

            return extended;
        } else {
            throw new IllegalStateException("Blueprint library not initialized");
        }
    }
}
