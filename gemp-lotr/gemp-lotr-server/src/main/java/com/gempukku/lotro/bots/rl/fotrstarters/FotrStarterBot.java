package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.DecisionAnswerer;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.CardFromDiscardTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice.BurdenTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.GoFirstTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.MulliganTrainer;
import com.gempukku.lotro.bots.rl.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FotrStarterBot extends RandomDecisionBot implements BotPlayer {
    private final List<DecisionAnswerer> cardSelectionTrainers = List.of(
            new ReconcileTrainer(),
            new SanctuaryTrainer(),
            new ArcheryWoundTrainer(),
            new AttachItemTrainer(),
            new SkirmishOrderTrainer(),
            new HealTrainer(),
            new DiscardFromHandTrainer(),
            new ExertTrainer(),
            new DiscardFromPlayTrainer(),
            new PlayFromHandTrainer()
    );

    private final List<DecisionAnswerer> fallBackTrainers = List.of(
            new FallBackCardSelectionTrainer() // card selection
    );

    private final List<DecisionAnswerer> integerTrainers = List.of(
            new BurdenTrainer()
    );

    private final List<DecisionAnswerer> multipleChoiceTrainers = List.of(
            new GoFirstTrainer(),
            new MulliganTrainer(),
            new AnotherMoveTrainer()
    );

    private final List<DecisionAnswerer> arbitraryCardsTrainers = List.of(
            new CardFromDiscardTrainer(),
            new StartingFellowshipTrainer()
    );

    private final List<DecisionAnswerer> cardActionTrainers = List.of(
            new FellowshipCardActionTrainer(),
            new ShadowCardActionTrainer(),
            new ManeuverCardActionTrainer(),
            new SkirmishCardActionTrainer(),
            new RegroupCardActionTrainer()
    );

    private final RLGameStateFeatures features;
    private final ModelRegistry modelRegistry;

    private CardActionSelectionDecision lastDecision = null;
    private String lastAction = null;
    private int decisionRepeat = 0;

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
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)) {
            return chooseArbitraryCardsAction(gameState, decision);
        } else if (decision.getDecisionType().equals(AwaitingDecisionType.CARD_ACTION_CHOICE)) {
            return chooseCardActionChoice(gameState, decision);
        }
        return super.chooseAction(gameState, decision);
    }

    private String chooseCardActionChoice(GameState gameState, AwaitingDecision decision) {
        String[] actionIds = decision.getDecisionParameters().get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: must pass
            return "";
        }

        String chosenAnswer = null;
        for (DecisionAnswerer trainer : cardActionTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                chosenAnswer = trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
            }
        }
        // Loop prevention
        if (chosenAnswer != null) {
            if (decision.equals(lastDecision) && chosenAnswer.equals(lastAction)) {
                decisionRepeat++;
                if (decisionRepeat >= 4) {
                    decisionRepeat = 0;
                    lastAction = null;
                    return "";
                }
            } else {
                lastDecision = (CardActionSelectionDecision) decision;
                decisionRepeat = 0;
                lastAction = chosenAnswer;
            }
            return chosenAnswer;
        }

        System.out.println("Unknown card action decision: "
                + gameState.getCurrentPhase() + " - "
                + decision.getText()
                + "; actionId=" + Arrays.toString(decision.getDecisionParameters().get("actionId"))
                + "; cardId=" + Arrays.toString(decision.getDecisionParameters().get("cardId"))
                + "; blueprintId=" + Arrays.toString(decision.getDecisionParameters().get("blueprintId"))
                + "; actionText=" + Arrays.toString(decision.getDecisionParameters().get("actionText")));
        return super.chooseAction(gameState, decision);
    }

    private String chooseArbitraryCardsAction(GameState gameState, AwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        // Check if something is selectable
        if (cardIds == null || selectable == null || cardIds.length != selectable.length)
            return ""; // Invalid input
        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }
        // If nothing is selectable or max == 0, just return empty
        if (selectableIndices.isEmpty() || max == 0)
            return "";

        for (DecisionAnswerer trainer : arbitraryCardsTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
            }
        }

        // Fallback to random decision
        System.out.println("Unknown arbitrary card selection action: "
                + gameState.getCurrentPhase() + " - "
                + decision.getText()
                + " (" + decision.getDecisionParameters().get("min")[0]
                + ";" + decision.getDecisionParameters().get("max")[0] + ") "
                + "cards=" + Arrays.toString(decision.getDecisionParameters().get("blueprintId"))
                + ", selectable=" + Arrays.toString(decision.getDecisionParameters().get("selectable")));
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

        for (DecisionAnswerer trainer : cardSelectionTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
            }
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

        // Last fallback to trainer
        for (DecisionAnswerer trainer : fallBackTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
            }
        }

        // Fallback to random decision
        System.out.println("Unknown card selection action: " + gameState.getCurrentPhase() + " - " + decision.getText() + " (" + decision.getDecisionParameters().get("min")[0] + ";" + decision.getDecisionParameters().get("max")[0] + ") " + Arrays.toString(decision.getDecisionParameters().get("cardId")));
        return super.chooseAction(gameState, decision);
    }

    private String chooseIntegerAction(GameState gameState, AwaitingDecision decision) {
        for (DecisionAnswerer trainer : integerTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
            }
        }
        if (decision.getText().contains("how many to spot")) {
            return decision.getDecisionParameters().get("max")[0];
        }

        System.out.println("Unknown integer action: " + decision.getText());
        return super.chooseAction(gameState, decision);
    }

    private String chooseMultipleChoiceAction(GameState gameState, AwaitingDecision decision) {
        String[] options = decision.getDecisionParameters().get("results");
        for (DecisionAnswerer trainer : multipleChoiceTrainers) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), features, modelRegistry);
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
}
