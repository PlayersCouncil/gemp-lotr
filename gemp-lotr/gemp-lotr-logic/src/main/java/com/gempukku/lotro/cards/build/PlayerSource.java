package com.gempukku.lotro.cards.build;

public interface PlayerSource extends PreEvaluateAble {
    String getPlayer(ActionContext actionContext);

    default boolean canPreEvaluate() {
        return true;
    }
}
