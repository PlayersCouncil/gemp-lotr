package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);
}
