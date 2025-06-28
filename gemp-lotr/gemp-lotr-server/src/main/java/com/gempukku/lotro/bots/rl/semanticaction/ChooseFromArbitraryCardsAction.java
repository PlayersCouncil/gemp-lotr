package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.annotation.JSONType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

@JSONType(typeName = "ChooseFromArbitraryCardsAction")
public class ChooseFromArbitraryCardsAction implements SemanticAction {
    private final List<String> chosenBlueprintIds = new ArrayList<>();

    public ChooseFromArbitraryCardsAction(String answer, AwaitingDecision awaitingDecision) {
        String[] individualCards = answer.split(",");
        List<String> usedCards = new ArrayList<>();
        String[] blueprintIds = awaitingDecision.getDecisionParameters().get("blueprintId");
        String[] cardIds = awaitingDecision.getDecisionParameters().get("cardId");

        for (int i = 0; i < cardIds.length; i++) {
            for (String individualCard : individualCards) {
                if (cardIds[i].equals(individualCard) && !usedCards.contains(individualCard)) {
                    chosenBlueprintIds.add(blueprintIds[i]);
                    usedCards.add(individualCard);
                }
            }
        }
    }

    public ChooseFromArbitraryCardsAction(List<String> getChosenBlueprintIds) {
        this.chosenBlueprintIds.addAll(getChosenBlueprintIds);
    }

    public List<String> getChosenBlueprintIds() {
        return chosenBlueprintIds;
    }

    @Override
    public String toDecisionString(AwaitingDecision decision, GameState gameState) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS) {
            throw new IllegalArgumentException("Wrong decision type.");
        }
        String[] blueprintIds = decision.getDecisionParameters().get("blueprintId");
        String[] cardIds = decision.getDecisionParameters().get("cardId");
        List<String> usedCards = new ArrayList<>();


        List<String> picked = new ArrayList<>();
        for (String chosenBlueprintId : chosenBlueprintIds) {
            for (int i = 0; i < blueprintIds.length; i++) {
                if (blueprintIds[i].equals(chosenBlueprintId) && !usedCards.contains(cardIds[i])) {
                    picked.add(cardIds[i]);
                    usedCards.add(cardIds[i]);
                    break;
                }
            }
        }

        return String.join(",", picked);
    }
}
