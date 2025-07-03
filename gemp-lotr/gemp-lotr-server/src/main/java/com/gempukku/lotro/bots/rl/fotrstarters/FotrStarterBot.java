package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice.BurdenTrainer;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FotrStarterBot extends RandomDecisionBot implements BotPlayer {
    private final RLGameStateFeatures features;
    private final ModelRegistry modelRegistry;

    public FotrStarterBot(RLGameStateFeatures features, String playerId, ModelRegistry modelRegistry) {
        super(playerId);
        this.features = features;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public String chooseAction(GameState gameState, AwaitingDecision decision) {
        if (decision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)) {
            return chooseMultipleChoiceAction(gameState, decision);
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.INTEGER)) {
            return chooseIntegerAction(gameState, decision);
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
            return chooseCardSelectionAction(gameState, decision);
        }
        return super.chooseAction(gameState, decision);
    }

    private String chooseCardSelectionAction(GameState gameState, AwaitingDecision decision) {
        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        if (min == max && min == cardIds.size()) {
            // Need to choose all
            return String.join(",", cardIds);
        }

        if (decision.getText().contains("Reconcile")) {
            SoftClassifier<double[]> model = modelRegistry.getReconcileModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, 0);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of discard
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest discard probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId; // Never pass, always discard one card
        }
        if (decision.getText().contains("Sanctuary healing")) {
            SoftClassifier<double[]> model = modelRegistry.getSanctuaryModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    int wounds = 0;
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            wounds = gameState.getWounds(physicalCard);
                        }
                    }
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of healing this card
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest heal probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId; // Never pass, always heal at sanctuary
        }
        if (decision.getText().contains("assign archery wound to")) {
            SoftClassifier<double[]> model = modelRegistry.getArcheryModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    int wounds = 0;
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            wounds = gameState.getWounds(physicalCard);
                        }
                    }
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of wounding this card
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest archery wound probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId;
        }
        if (decision.getText().contains("Choose target to attach to")) {
            SoftClassifier<double[]> model = modelRegistry.getAttachItemModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    int wounds = 0;
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            wounds = gameState.getWounds(physicalCard);
                        }
                    }
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of attaching to this card
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId;
        }
        if (decision.getText().contains("next skirmish to resolve")) {
            SoftClassifier<double[]> model = modelRegistry.getSkirmishOrderModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    for (Assignment assignment : gameState.getAssignments()) {
                        if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId)) {
                            String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                            int wounds = 0;
                            for (PhysicalCard physicalCard : gameState.getAllCards()) {
                                if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                                    wounds = gameState.getWounds(physicalCard);
                                }
                            }
                            double[] cardVector = CardFeatures.getFpAssignedCardFeatures(blueprintId, wounds, assignment.getShadowCharacters().size(), assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum());
                            double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                            System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                            double[] probs = new double[2];
                            model.predict(extended, probs);
                            scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of choosing this card
                        }
                    }

                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId;
        }
        if (decision.getText().contains("to heal")) {
            SoftClassifier<double[]> model = modelRegistry.getHealModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    int wounds = 0;
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            wounds = gameState.getWounds(physicalCard);
                        }
                    }
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of healing this card
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the card with the highest probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            ScoredCard best = scoredCards.get(0);
            return best.cardId;
        }
        if (decision.getText().toLowerCase().contains("discard") && !decision.getText().toLowerCase().contains("reconcile")) {
            String zoneOfAllCards = null;
            for (String choice : decision.getDecisionParameters().get("cardId")) {
                for (PhysicalCard physicalCard : gameState.getAllCards()) {
                    if (physicalCard.getCardId() == Integer.parseInt(choice)) {
                        String zoneOfThisCard = physicalCard.getZone().getHumanReadable();
                        if (zoneOfAllCards == null) {
                            zoneOfAllCards = zoneOfThisCard;
                        }
                        if (!zoneOfAllCards.equals(zoneOfThisCard)) {
                            throw new IllegalArgumentException("Cards are not from the same zone");
                        }
                    }
                }
            }
            if (zoneOfAllCards != null && zoneOfAllCards.equals(Zone.HAND.getHumanReadable())) {
                SoftClassifier<double[]> model = modelRegistry.getDiscardFromHandModel();
                double[] stateVector = features.extractFeatures(gameState, decision, getName());
                List<ScoredCard> scoredCards = new ArrayList<>();

                for (String physicalId : cardIds) {
                    try {
                        String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                        double[] cardVector = CardFeatures.getCardFeatures(blueprintId, 0);
                        double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                        System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                        double[] probs = new double[2];
                        model.predict(extended, probs);
                        scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of discard
                    } catch (CardNotFoundException ignored) {

                    }
                }

                // Find the cards with the highest discard probability
                scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
                List<String> sortedIds = new ArrayList<>();
                scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
                return String.join(",", sortedIds.subList(0, max));
            }
        }
        if (decision.getText().contains("to exert")) {
            SoftClassifier<double[]> model = modelRegistry.getExertModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    int wounds = 0;
                    for (PhysicalCard physicalCard : gameState.getAllCards()) {
                        if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                            wounds = gameState.getWounds(physicalCard);
                        }
                    }
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of discard
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the cards with the highest discard probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            List<String> sortedIds = new ArrayList<>();
            scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
            return String.join(",", sortedIds.subList(0, max));
        }
        if (decision.getText().toLowerCase().contains("discard")) {
            String zoneOfAllCards = null;
            for (String choice : decision.getDecisionParameters().get("cardId")) {
                for (PhysicalCard physicalCard : gameState.getAllCards()) {
                    if (physicalCard.getCardId() == Integer.parseInt(choice)) {
                        String zoneOfThisCard = physicalCard.getZone().getHumanReadable();
                        if (zoneOfAllCards == null) {
                            zoneOfAllCards = zoneOfThisCard;
                        }
                        if (!zoneOfAllCards.equals(zoneOfThisCard)) {
                            throw new IllegalArgumentException("Cards are not from the same zone");
                        }
                    }
                }
            }
            if (zoneOfAllCards != null && zoneOfAllCards.equals(Zone.FREE_CHARACTERS.getHumanReadable())) {
                SoftClassifier<double[]> model = modelRegistry.getDiscardFromPlayModel();
                double[] stateVector = features.extractFeatures(gameState, decision, getName());
                List<ScoredCard> scoredCards = new ArrayList<>();

                for (String physicalId : cardIds) {
                    try {
                        String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                        int wounds = 0;
                        for (PhysicalCard physicalCard : gameState.getAllCards()) {
                            if (physicalCard.getCardId() == Integer.parseInt(physicalId)) {
                                wounds = gameState.getWounds(physicalCard);
                            }
                        }
                        double[] cardVector = CardFeatures.getCardFeatures(blueprintId, wounds);
                        double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                        System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                        double[] probs = new double[2];
                        model.predict(extended, probs);
                        scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of discard
                    } catch (CardNotFoundException ignored) {

                    }
                }

                // Find the cards with the highest discard probability
                scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
                List<String> sortedIds = new ArrayList<>();
                scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
                return String.join(",", sortedIds.subList(0, max));
            }
        }
        if (decision.getText().toLowerCase().contains("play from hand")) {
            SoftClassifier<double[]> model = modelRegistry.getPlayFromHandModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            List<ScoredCard> scoredCards = new ArrayList<>();

            for (String physicalId : cardIds) {
                try {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                    double[] cardVector = CardFeatures.getCardFeatures(blueprintId, 0);
                    double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                    System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                    double[] probs = new double[2];
                    model.predict(extended, probs);
                    scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of discard
                } catch (CardNotFoundException ignored) {

                }
            }

            // Find the cards with the highest discard probability
            scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
            List<String> sortedIds = new ArrayList<>();
            scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
            return String.join(",", sortedIds.subList(0, max));
        }


        // Have this last as a fallback for skirmish modifiers - choose the one skirmishing
        if (gameState.getSkirmish() != null && min == 1 && max == 1) {
            Skirmish skirmish = gameState.getSkirmish();
            for (String cardId : cardIds) {
                if (skirmish.getFellowshipCharacter().getCardId() == Integer.parseInt(cardId)) {
                    return cardId;
                }
                for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                    if (shadowCharacter.getCardId() == Integer.parseInt(cardId)) {
                        return cardId;
                    }
                }
            }
            // Skirmishing character cannot be chosen, choose whatever
            return super.chooseAction(gameState, decision);
        }

        System.out.println("Unknown card selection action: " + gameState.getCurrentPhase() + " - " + decision.getText() + " (" + decision.getDecisionParameters().get("min")[0] + ";" + decision.getDecisionParameters().get("max")[0] + ") " + Arrays.toString(decision.getDecisionParameters().get("cardId")));
        return super.chooseAction(gameState, decision);
    }

    private String chooseIntegerAction(GameState gameState, AwaitingDecision decision) {
        if (decision.getText().contains("burdens to bid")) {
            SoftClassifier<double[]> model = modelRegistry.getBurdensBidModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            if (model != null) {
                double[] probs = new double[BurdenTrainer.getUniqueBids()];
                model.predict(stateVector, probs);
                int predictedValue = sampleFromDistribution(probs);
                return String.valueOf(predictedValue);
            }
        }
        if (decision.getText().contains("how many to spot")) {
            return decision.getDecisionParameters().get("max")[0];
        }

        System.out.println("Unknown integer action: " + decision.getText());
        return super.chooseAction(gameState, decision);
    }

    private int sampleFromDistribution(double[] probabilities) {
        double r = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (r < cumulative)
                return i;
        }
        return probabilities.length - 1; // fallback
    }

    private String chooseMultipleChoiceAction(GameState gameState, AwaitingDecision decision) {
        String[] options = decision.getDecisionParameters().get("results");
        if (List.of(options).contains("Go first")) {
            SoftClassifier<double[]> model = modelRegistry.getGoFirstModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            if (model != null) {
                double[] probs = new double[2];
                model.predict(stateVector, probs);
                return probs[1] > probs[0] ? new MultipleChoiceAction("Go first").toDecisionString(decision, gameState) : new MultipleChoiceAction("Go second").toDecisionString(decision, gameState);
            }
        }
        if (decision.getText().contains("mulligan")) {
            SoftClassifier<double[]> model = modelRegistry.getMulliganModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            if (model != null) {
                double[] probs = new double[2];
                model.predict(stateVector, probs);
                return probs[1] > probs[0] ? new MultipleChoiceAction("Yes").toDecisionString(decision, gameState) : new MultipleChoiceAction("No").toDecisionString(decision, gameState);
            }
        }
        if (decision.getText().contains("another move")) {
            SoftClassifier<double[]> model = modelRegistry.getAnotherMoveModel();
            double[] stateVector = features.extractFeatures(gameState, decision, getName());
            if (model != null) {
                double[] probs = new double[2];
                model.predict(stateVector, probs);
                return probs[1] > probs[0] ? new MultipleChoiceAction("Yes").toDecisionString(decision, gameState) : new MultipleChoiceAction("No").toDecisionString(decision, gameState);
            }
        }
        if (List.of(options).contains("Heal a companion") && List.of(options).contains("Remove a Shadow condition from a companion")) {
            return new MultipleChoiceAction("Heal a companion").toDecisionString(decision, gameState);
        }
        if (List.of(options).contains("Exert Frodo") && List.of(options).contains("Exert 2 other companions")) {
            return new MultipleChoiceAction("Exert Frodo").toDecisionString(decision, gameState);
        }

        System.out.println("Unknown multiple choice action: " + decision.getText() + " - " + Arrays.toString(options));
        return super.chooseAction(gameState, decision);
    }

    private static class ScoredCard {
        String cardId;
        double score;

        ScoredCard(String cardId, double score) {
            this.cardId = cardId;
            this.score = score;
        }

        @Override
        public String toString() {
            return "ScoredCard{" +
                    "cardId='" + cardId + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
