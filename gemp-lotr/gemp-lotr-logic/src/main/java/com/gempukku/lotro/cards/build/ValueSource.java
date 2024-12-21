package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public interface ValueSource extends PreEvaluateAble {
    Evaluator getEvaluator(ActionContext actionContext);
    default boolean canPreEvaluate() { return true; }
}
