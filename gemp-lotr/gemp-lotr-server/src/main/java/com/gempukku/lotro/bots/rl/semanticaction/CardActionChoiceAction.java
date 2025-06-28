package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.annotation.JSONType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

@JSONType(typeName = "CardActionChoiceAction")
public class CardActionChoiceAction implements SemanticAction {
    private final String actionText;

    public CardActionChoiceAction(String answer, AwaitingDecision decision) {
        if (answer == null || answer.isEmpty()) {
            actionText = null;
        } else {
            String[] actionTexts = decision.getDecisionParameters().get("actionText");
            actionText = actionTexts[Integer.parseInt(answer)];
        }
    }

    public CardActionChoiceAction(String actionText) {
        this.actionText = actionText;
    }

    public String getActionText() {
        return actionText;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE) {
            throw new IllegalArgumentException("Wrong decision type.");
        }

        if (actionText == null) {
            return "";
        }

        String[] actionTexts = decision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++) {
            if (actionTexts[i].equals(actionText)) {
                return String.valueOf(i);
            }
        }
        throw new IllegalArgumentException("Option not found.");
    }
}
