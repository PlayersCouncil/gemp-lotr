package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.Map;

public interface EffectAppender extends PreEvaluateAble {
    void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext);

    boolean isPlayableInFull(ActionContext actionContext);

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }

    /**
     * Whether this appender, written as the cost of a costToEffect that stands among an action's effects, has
     * the action withheld while it cannot be paid.  Such a cost is not asked ahead as a rule, since most of
     * them read what the action remembers as it is carried out (CostToEffect.isPlayabilityCheckedForEffect).
     * An assignment says yes (AssignFpCharacterToSkirmish): "Exert Éowyn and assign a minion to the Ring-bearer
     * to ..." (17_96) is not offered where, once she has exerted, no minion could be assigned and paid for
     * (F-0486).
     */
    default boolean isPlayabilityCheckedAsCostOfEffect() {
        return false;
    }

    /**
     * The threats this appender will add when it is carried out as a cost, as far as can be told beforehand;
     * zero for everything that adds none, and for anything that only adds them under a condition or by choice.
     */
    default int getThreatsAddedAsCost(ActionContext actionContext) {
        return 0;
    }

    /**
     * The wounds this appender will place when it is carried out as a cost, as far as can be told before the
     * action is taken: exertions and wounds on cards it names outright (the card itself, its bearer, a memory
     * that is already filled) or cannot avoid (a choice with exactly as many candidates as must be chosen).
     * They are added to the tally, card by card.  Nothing is added where the player has a real choice of whom
     * to exert, or where the wounds depend on a condition.  The action's later costs, and any card it would then
     * play, are judged with these wounds in place (DefaultActionSource.isValid).
     */
    default void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
    }

    /**
     * The wounds one paying of this appender as a cost would place, added to the tally: what
     * tallyWoundsPlacedAsCost says for certain and, where whoever pays has a real choice of whom to exert, the
     * choice that leaves the most still able to pay (those with the most vitality left).  A repeat among costs
     * asks it (Repeat): a repeated cost can be paid only if every repetition can (Ketura on F-0425; CR 4.2
     * "cost"), so each repetition is asked with the wounds of those before it in place.  Exert is taught the
     * choice, being the one cost a printed card repeats (Morgul Regiment 7_197); every other appender says what
     * is certain, and a repeated cost of another kind is judged on that.
     */
    default void tallyWoundsOfPayingOnce(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
        tallyWoundsPlacedAsCost(actionContext, tally);
    }

    default boolean canPreEvaluate() {
        return true;
    }

    /**
     * What can be told of this effect before the action it belongs to is taken, for judging whether the action
     * will do nothing (the no-effect flag).  Asked while the action is only on offer: no cost has been paid and
     * nothing has been chosen.  It must change nothing, and in particular must never work out an amount, since
     * an amount with a limit spends the limit as it is worked out.  An effect that cannot tell says UNKNOWN.
     */
    default EffectForecast forecast(ActionContext actionContext) {
        return EffectForecast.UNKNOWN;
    }

    /**
     * Whether this effect, were it carried out now, is certain to do nothing.  A choice asks it of each of its
     * branches as the choice comes up, when none of them can be carried out in full (Choice): nobody is asked to
     * pick between things none of which will happen.
     *
     * An effect answers for itself by its forecast, asked as it is when the effect is about to be carried out
     * (EffectForecast.isCertainToDoNothing).  An effect that holds others answers from what it holds: an "if" by
     * the side its check picks, a cost and what it buys by whether the cost can be paid, several effects by each
     * of them in turn, a play by whether there is a card to play.  One that holds others and has not been taught
     * to say (a "you may", a repeat, a prevention) answers false, and the choice it is a branch of is asked.
     */
    default boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
        return EffectForecast.isCertainToDoNothing(actionContext, new EffectAppender[]{this});
    }
}
