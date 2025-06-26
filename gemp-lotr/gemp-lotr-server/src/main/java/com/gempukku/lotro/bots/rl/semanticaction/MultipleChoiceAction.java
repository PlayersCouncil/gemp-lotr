package com.gempukku.lotro.bots.rl.semanticaction;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

public class MultipleChoiceAction implements SemanticAction {
    private final String chosenOption;

    public MultipleChoiceAction(String chosenOption, AwaitingDecision decision) {
        this.chosenOption = decision.getDecisionParameters().get("results")[Integer.parseInt(chosenOption)];
    }

    public MultipleChoiceAction(String actionText) {
        this.chosenOption = actionText;
    }

    public String getChosenOption() {
        return chosenOption;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE) {
            throw new IllegalArgumentException("Wrong decision type.");
        }
        String[] options = decision.getDecisionParameters().get("results");
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(chosenOption)) {
                return String.valueOf(i);
            }
        }

        throw new IllegalArgumentException("Option not found.");
    }
}
