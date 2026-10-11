package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.YesNoDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.function.Predicate;

public class PreventableEffectAppender extends DelayedAppender {
    private PlayerSource preventingPlayer;
    private String preventText;
    private Predicate<ActionContext> preventingPredicate;
    private EffectAppender[] costAppenders;
    private EffectAppender[] effectAppenders;
    private EffectAppender[] insteadAppenders;

    public PreventableEffectAppender(PlayerSource preventingPlayer, String preventText, Predicate<ActionContext> preventingPredicate,
                                     EffectAppender[] costAppenders, EffectAppender[] effectAppenders, EffectAppender[] insteadAppenders) {
        this.preventingPlayer = preventingPlayer;
        this.preventText = preventText;
        this.preventingPredicate = preventingPredicate;
        this.costAppenders = costAppenders;
        this.effectAppenders = effectAppenders;
        this.insteadAppenders = insteadAppenders;
    }

    @Override
    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        String textToUse = GameUtils.substituteText(preventText, actionContext);
        String preventingPlayerId = preventingPlayer.getPlayer(actionContext);

        DelegateActionContext preventCostActionContext = new DelegateActionContext(actionContext, preventingPlayerId, actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(), actionContext.getEffect());

        // A prevention is put to a player only when what it would prevent is going to happen (Ketura on
        // Q-0114): with nothing to draw, a draw past the Rule of 4, nobody left to wound or discard, nobody is
        // asked and nothing is paid.  The effects are still appended, and do the nothing they would have done.
        if (preventingPredicate.test(actionContext) && areCostsPlayable(preventCostActionContext)
                && !EffectForecast.isCertainToDoNothing(actionContext, effectAppenders)) {
            SubAction subAction = new SubAction(action);
            subAction.appendEffect(new PlayoutDecisionEffect(preventingPlayerId, new YesNoDecision(textToUse) {
                @Override
                protected void yes() {
                    actionContext.getGame().getGameState().sendMessage(GameUtils.substituteText(preventingPlayerId + " chooses to prevent.", actionContext));
                    for (EffectAppender costAppender : costAppenders)
                        costAppender.appendEffect(false, subAction, preventCostActionContext);

                    subAction.appendEffect(new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            // If the prevention was not carried out, need to do the original action anyway
                            if (!subAction.wasCarriedOut()) {
                                game.getGameState().sendMessage(GameUtils.substituteText(preventingPlayerId + " attempted to prevent, but could not carry it out.", actionContext));
                                for (EffectAppender effectAppender : effectAppenders)
                                    effectAppender.appendEffect(false, subAction, actionContext);
                            } else {
                                for (EffectAppender insteadAppender : insteadAppenders) {
                                    insteadAppender.appendEffect(false, subAction, preventCostActionContext);
                                }
                            }
                        }

                        @Override
                        public boolean wasCarriedOut() {
                            return true;
                        }
                    });
                }

                @Override
                protected void no() {
                    actionContext.getGame().getGameState().sendMessage(GameUtils.substituteText(preventingPlayerId + " decides not to prevent.", actionContext));
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(false, subAction, actionContext);
                }
            }));
            return new StackActionEffect(subAction);
        } else {
            SubAction subAction = new SubAction(action);
            for (EffectAppender effectAppender : effectAppenders)
                effectAppender.appendEffect(false, subAction, actionContext);
            return new StackActionEffect(subAction);
        }
    }

    // As a branch of a choice it keeps the choice asked: what is paid to prevent it, and what is done in its
    // place, are another player's to choose.
    @Override
    public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
        return false;
    }

    // What would happen if it were not prevented.  Where preventing it puts something else in its place,
    // nothing is said: that is the other player's to bring about.
    @Override
    public EffectForecast forecast(ActionContext actionContext) {
        if (insteadAppenders != null && insteadAppenders.length > 0)
            return EffectForecast.UNKNOWN;
        return EffectForecast.together(actionContext, effectAppenders);
    }

    private boolean areCostsPlayable(ActionContext actionContext) {
        for (EffectAppender costAppender : costAppenders) {
            if (!costAppender.isPlayableInFull(actionContext)) return false;
        }
        return true;
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        for (EffectAppender effectAppender : effectAppenders) {
            if (!effectAppender.isPlayableInFull(actionContext)) return false;
        }

        return true;
    }
}
