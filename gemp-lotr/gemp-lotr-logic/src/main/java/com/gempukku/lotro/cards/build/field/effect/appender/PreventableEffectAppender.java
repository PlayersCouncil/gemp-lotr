package com.gempukku.lotro.cards.build.field.effect.appender;

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

        if (preventingPredicate.test(actionContext) && areCostsPlayable(preventCostActionContext)) {
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
