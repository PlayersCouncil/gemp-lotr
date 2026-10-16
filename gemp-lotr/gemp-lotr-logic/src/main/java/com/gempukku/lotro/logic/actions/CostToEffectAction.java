package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.NoEffect;

import java.util.function.Supplier;

public interface CostToEffectAction extends Action {
    void appendPotentialDiscount(DiscountEffect cost);

    /**
     * Inserts the specified costs as the next costs to be executed.
     *
     * @param cost
     */
    public void insertCost(Effect... cost);

    /**
     * Appends the specified cost to the list of the costs. It will be executed after all the other costs currently in
     * the queue.
     *
     * @param cost
     */
    public void appendCost(Effect cost);

    /**
     * Inserts the specified effects as the next effects to be executedD.
     *
     * @param effect
     */
    public void insertEffect(Effect... effect);

    /**
     * Appends the specified effect to the list of the effects. It will be executed after all the other effects currently
     * in the queue.
     *
     * @param effect
     */
    public void appendEffect(Effect effect);

    void setText(String text);

    /**
     * Says what set this action off (see Action.getOfferedFor).
     * @param offeredFor The names of the cards the trigger remembered, or null.
     */
    default void setOfferedFor(String offeredFor) {
    }

    /**
     * Says which ability of its card's definition built this action (see Action.getAbility).
     * @param ability The ability.
     */
    default void setAbility(ActionSource ability) {
    }

    void setPaidToil(boolean toilPaid);

    boolean wasCarriedOut();

    /**
     * Gives this action the means of judging, when a player is about to be offered it, whether it will do
     * nothing.  The judge is asked at most once.
     * @param judge Answers why the action will do nothing, or null if it is not known to.
     */
    default void setNoEffectJudge(Supplier<NoEffect> judge) {
    }
}
