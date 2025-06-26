package com.gempukku.lotro.bots.rl.semanticaction;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

public class CardActionChoiceAction implements SemanticAction {
    private final boolean pass;
    private final String actionText;

    public CardActionChoiceAction(String answer, AwaitingDecision decision) {
        if (answer.isEmpty()) {
            pass = true;
            actionText = null;
        } else {
            pass = false;
            String[] actionTexts = decision.getDecisionParameters().get("actionText");
            actionText = actionTexts[Integer.parseInt(answer)];
        }
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE) {
            throw new IllegalArgumentException("Wrong decision type.");
        }

        if (pass) {
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
