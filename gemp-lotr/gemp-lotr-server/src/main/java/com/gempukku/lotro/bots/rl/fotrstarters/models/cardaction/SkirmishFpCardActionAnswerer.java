package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

import com.gempukku.lotro.bots.rl.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.semanticaction.CardActionChoiceAction;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class SkirmishFpCardActionAnswerer extends AbstractCardActionAnswerer {
    private static final String TRIGGER = "Choose action to play or Pass";
    private final SkirmishFpPlayCardTrainer playTrainer = new SkirmishFpPlayCardTrainer();
    private final SkirmishFpUseCardTrainer useTrainer = new SkirmishFpUseCardTrainer();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return super.appliesTo(gameState, decision, playerName) && gameState.getCurrentPlayerId().equals(playerName);
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

    public static class SkirmishFpPlayCardTrainer extends AbstractPlayUseCardTrainer {
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
            return super.isStepRelevant(step) && step.fpPlayer;
        }

        @Override
        protected double[] getPassCardFeatures() throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(CardFeatures.PASS, CardFeatures.PASS, List.of(), false);
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, CardActionChoiceAction action) throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, action.getSkirmishingFpBlueprintId(),
                    action.getSkirmishingMinionBlueprintIds(), action.isSourceInSkirmish());
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, int wounds, GameState gameState, String physicalId) throws CardNotFoundException {
            Skirmish skirmish = gameState.getSkirmish();
            List<String> minions = new ArrayList<>();
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                minions.add(shadowCharacter.getBlueprintId());
            }

            boolean sourceInSkirmish = gameState.getSkirmish().getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId);

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, skirmish.getFellowshipCharacter().getBlueprintId(), minions, sourceInSkirmish);
        }
    }

    public static class SkirmishFpUseCardTrainer extends AbstractPlayUseCardTrainer {
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
            return super.isStepRelevant(step) && step.fpPlayer;
        }

        @Override
        protected double[] getPassCardFeatures() throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(CardFeatures.PASS, CardFeatures.PASS, List.of(), false);
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, CardActionChoiceAction action) throws CardNotFoundException {
            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, action.getSkirmishingFpBlueprintId(),
                    action.getSkirmishingMinionBlueprintIds(), action.isSourceInSkirmish());
        }

        @Override
        protected double[] getCardFeatures(String blueprintId, int wounds, GameState gameState, String physicalId) throws CardNotFoundException {
            Skirmish skirmish = gameState.getSkirmish();
            List<String> minions = new ArrayList<>();
            for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                minions.add(shadowCharacter.getBlueprintId());
            }

            boolean sourceInSkirmish = gameState.getSkirmish().getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId);

            return CardFeatures.getSkirmishPlayCardFeatures(blueprintId, skirmish.getFellowshipCharacter().getBlueprintId(), minions, sourceInSkirmish);
        }
    }
}
