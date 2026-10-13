package com.gempukku.lotro.cards.build;

import java.util.function.ToIntFunction;

/**
 * The context in which an action is asked whether it can be used.  None of its costs has been paid at that
 * point, so the threats its costs will add are still to come before any of its effects is carried out.  They are
 * worked out only if something asks for them.  Memory is the wrapped context's own, so what a requirement
 * memorizes while it is checked is there for the action that follows.
 */
public class UnpaidCostsContext extends DelegateActionContext {
    private final ActionContext delegate;
    private final ToIntFunction<ActionContext> threatsAddedByCosts;
    private Integer threats;

    public UnpaidCostsContext(ActionContext delegate, ToIntFunction<ActionContext> threatsAddedByCosts) {
        super(delegate, delegate.getPerformingPlayer());
        this.delegate = delegate;
        this.threatsAddedByCosts = threatsAddedByCosts;
    }

    @Override
    public int getUnpaidThreatCost() {
        if (threats == null)
            threats = threatsAddedByCosts.applyAsInt(delegate);
        return threats + delegate.getUnpaidThreatCost();
    }
}
