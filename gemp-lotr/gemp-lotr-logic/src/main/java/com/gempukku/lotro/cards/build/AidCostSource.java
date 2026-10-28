package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.logic.actions.CostToEffectAction;

public interface AidCostSource {
    boolean canPayAidCost(DefaultActionContext actionContext);

    void appendAidCost(CostToEffectAction action, DefaultActionContext actionContext);

    /**
     * The N of a printed "Aid - (N)": the twilight this Aid costs, kept as the definition is read so that the card
     * can be asked for it.  It is kept only where the whole of the Aid is one twilight cost of a plain number, which
     * a Free Peoples follower adds and a Shadow follower removes.  Nothing is paid by it: what is paid is the
     * definition's own cost, through appendAidCost.
     * @return That number; null for an Aid paid in anything else (a burden, a threat, an exertion, a card from
     * hand), in twilight and something else besides, or in twilight that is not a plain number.
     */
    Integer getTwilightCost();
}
