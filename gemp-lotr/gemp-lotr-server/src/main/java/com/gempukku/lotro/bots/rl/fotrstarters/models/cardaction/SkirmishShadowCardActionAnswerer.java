package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.semanticaction.CardActionChoiceAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkirmishShadowCardActionAnswerer extends AbstractCardActionAnswerer {
    private static final String TRIGGER = "Choose action to play or Pass";
    private final SkirmishShadowPlayCardTrainer playTrainer = new SkirmishShadowPlayCardTrainer();
    private final SkirmishShadowUseCardTrainer useTrainer = new SkirmishShadowUseCardTrainer();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return super.appliesTo(gameState, decision, playerName) && !gameState.getCurrentPlayerId().equals(playerName);
    }

    @Override
    protected String getTextTrigger() {
        return TRIGGER;
    }

    @Override
    protected AbstractPlayUseCardTrainer getPlayTrainer() {
        return playTrainer;
    }

    @Override
    protected AbstractPlayUseCardTrainer getUseTrainer() {
        return useTrainer;
    }

    @Override
    protected AbstractPlayUseCardTrainer getHealTrainer() {
        return null;
    }

    @Override
    protected AbstractPlayUseCardTrainer getTransferTrainer() {
        return null;
    }

    public static class SkirmishShadowPlayCardTrainer extends AbstractPlayUseCardTrainer {
        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return true;
        }

        @Override
        protected boolean isUseTrainer() {
            return false;
        }

        @Override
        protected boolean isTransferTrainer() {
            return false;
        }

        @Override
        protected boolean isHealTrainer() {
            return false;
        }

        @Override
        public boolean isStepRelevant(LearningStep step) {
            return super.isStepRelevant(step) && !step.fpPlayer;
        }

        @Override
        protected double[] getPassCardFeatures() throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(CardFeatures.PASS, CardFeatures.PASS, false, List.of(), List.of(), false);
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, CardActionChoiceAction action) throws CardNotFoundException {
            List<Boolean> canExert = new ArrayList<>();
            for (Integer remainingVitalityOnSkirmishingMinion : action.getRemainingVitalityOnSkirmishingMinions()) {
                canExert.add(remainingVitalityOnSkirmishingMinion > 1);
            }

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, action.getSkirmishingFpBlueprintId(), false,
                    action.getSkirmishingMinionBlueprintIds(), canExert, action.isSourceInSkirmish());
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, int wounds, GameState gameState, String physicalId) throws CardNotFoundException {
            Skirmish skirmish = gameState.getSkirmish();
            List<String> minions = new ArrayList<>();
            List<Boolean> canExert = new ArrayList<>();
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                minions.add(shadowCharacter.getBlueprintId());
                canExert.add(gameState.getWounds(shadowCharacter) + 1 < shadowCharacter.getBlueprint().getVitality());
            }

            boolean sourceInSkirmish = false;
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                if (shadowCharacter.getCardId() == Integer.parseInt(physicalId)) {
                    sourceInSkirmish = true;
                }
            }

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, skirmish.getFellowshipCharacter().getBlueprintId(), false, minions, canExert, sourceInSkirmish);
        }
    }

    public static class SkirmishShadowUseCardTrainer extends AbstractPlayUseCardTrainer {
        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return false;
        }

        @Override
        protected boolean isUseTrainer() {
            return true;
        }

        @Override
        protected boolean isTransferTrainer() {
            return false;
        }

        @Override
        protected boolean isHealTrainer() {
            return false;
        }

        @Override
        public boolean isStepRelevant(LearningStep step) {
            return super.isStepRelevant(step) && !step.fpPlayer;
        }

        @Override
        protected double[] getPassCardFeatures() throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(CardFeatures.PASS, CardFeatures.PASS, false, List.of(), List.of(), false);
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, CardActionChoiceAction action) throws CardNotFoundException {
            List<Boolean> canExert = new ArrayList<>();
            for (Integer remainingVitalityOnSkirmishingMinion : action.getRemainingVitalityOnSkirmishingMinions()) {
                canExert.add(remainingVitalityOnSkirmishingMinion > 1);
            }

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, action.getSkirmishingFpBlueprintId(), false,
                    action.getSkirmishingMinionBlueprintIds(), canExert, action.isSourceInSkirmish());
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, int wounds, GameState gameState, String physicalId) throws CardNotFoundException {
            Skirmish skirmish = gameState.getSkirmish();
            List<String> minions = new ArrayList<>();
            List<Boolean> canExert = new ArrayList<>();
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                minions.add(shadowCharacter.getBlueprintId());
                canExert.add(gameState.getWounds(shadowCharacter) + 1 < shadowCharacter.getBlueprint().getVitality());
            }

            boolean sourceInSkirmish = false;
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                if (shadowCharacter.getCardId() == Integer.parseInt(physicalId)) {
                    sourceInSkirmish = true;
                }
            }

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, skirmish.getFellowshipCharacter().getBlueprintId(), false, minions, canExert, sourceInSkirmish);
        }

        @Override
        public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
            // Do not use Ettenmoors
            int id = decision.getAwaitingDecisionId();
            String text = decision.getText();
            List<String> cardIds = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("cardId")).toList());
            List<String> blueprintIds = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList());
            List<String> actions = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("actionText")).toList());

            String bridgeText = "Use Ettenmoors";
            int indexOfBridge = actions.indexOf(bridgeText);
            if (indexOfBridge >= 0) {
                cardIds.remove(indexOfBridge);
                blueprintIds.remove(indexOfBridge);
                actions.remove(indexOfBridge);
            }

            AwaitingDecision filteredDecision = new CardActionSelectionDecision(id, text, cardIds, blueprintIds, actions) {
                @Override
                public void decisionMade(String result) throws DecisionResultInvalidException {

                }
            };

            String filteredAnswer = super.getAnswer(gameState, filteredDecision, playerName, features, modelRegistry);

            if (filteredAnswer.isEmpty() || indexOfBridge < 0 || Integer.parseInt(filteredAnswer) < indexOfBridge) {
                return filteredAnswer;
            } else {
                return String.valueOf(Integer.parseInt(filteredAnswer) + 1);
            }
        }
    }
}
