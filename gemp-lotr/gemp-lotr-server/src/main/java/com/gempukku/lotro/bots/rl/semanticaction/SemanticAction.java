package com.gempukku.lotro.bots.rl.semanticaction;

import com.alibaba.fastjson2.annotation.JSONType;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

@JSONType(seeAlso = {MultipleChoiceAction.class, IntegerChoiceAction.class, ChooseFromArbitraryCardsAction.class,
        CardSelectionAction.class, CardActionChoiceAction.class, AssignMinionsAction.class, ActionChoiceAction.class})
public interface SemanticAction {
    // Converts this semantic action into a concrete action string for the given decision and game state
    String toDecisionString(AwaitingDecision decision, GameState gameState);
}
