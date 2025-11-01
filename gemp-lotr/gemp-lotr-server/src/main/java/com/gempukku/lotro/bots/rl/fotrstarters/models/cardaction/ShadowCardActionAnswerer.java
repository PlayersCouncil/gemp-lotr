package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShadowCardActionAnswerer extends AbstractCardActionAnswerer {
    private static final String TRIGGER = "Play Shadow action or Pass";
    private final ShadowPlayCardTrainer playTrainer = new ShadowPlayCardTrainer();
    private final ShadowUseCardTrainer useTrainer = new ShadowUseCardTrainer();
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

    public static class ShadowPlayCardTrainer extends AbstractPlayUseCardTrainer {
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
    }

    public static class ShadowUseCardTrainer extends AbstractPlayUseCardTrainer {
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
        public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
            // Do not use bridge with starters
            int id = decision.getAwaitingDecisionId();
            String text = decision.getText();
            List<String> cardIds = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("cardId")).toList());
            List<String> blueprintIds = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList());
            List<String> actions = new ArrayList<>(Arrays.stream(decision.getDecisionParameters().get("actionText")).toList());

            String bridgeText = "Use The Bridge of Khazad-dûm";
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
