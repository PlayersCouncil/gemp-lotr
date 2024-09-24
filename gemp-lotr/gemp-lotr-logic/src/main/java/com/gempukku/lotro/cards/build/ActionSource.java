package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.logic.actions.CostToEffectAction;

public interface ActionSource {
    PlayerSource getPlayer();
    boolean requiresRanger();

    boolean isValid(ActionContext actionContext);

    void createAction(CostToEffectAction action, ActionContext actionContext);
}
