package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.UnpaidCostsContext;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.LinkedList;
import java.util.List;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();

    private final List<EffectAppender> costs = new LinkedList<>();
    private final List<EffectAppender> effects = new LinkedList<>();

    private PlayerSource playingPlayer;
    private boolean requiresRanger;
    private String text;
    private boolean specialAbility = true;

    public void setPlayingPlayer(PlayerSource playingPlayer) {
        this.playingPlayer = playingPlayer;
    }

    public void setRequiresRanger(boolean requiresRanger) {
        this.requiresRanger = requiresRanger;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSpecialAbility(boolean specialAbility) {
        this.specialAbility = specialAbility;
    }

    public void addPlayRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void addCost(EffectAppender effectAppender) {
        costs.add(effectAppender);
    }

    public void addEffect(EffectAppender effectAppender) {
        effects.add(effectAppender);
    }

    @Override
    public PlayerSource getPlayer() {
        return playingPlayer;
    }

    @Override
    public boolean requiresRanger() {
        return requiresRanger;
    }

    @Override
    public boolean isSpecialAbility() {
        return specialAbility;
    }

    @Override
    public boolean isValid(ActionContext actionContext) {
        if (playingPlayer != null && !playingPlayer.getPlayer(actionContext).equals(actionContext.getPerformingPlayer()))
            return false;

        // The action is being asked whether it can be used, and none of its costs has been paid: what they
        // will have done to the board by the time its effects are carried out is there for a requirement to ask.
        final ActionContext checkContext = costs.isEmpty() ? actionContext
                : new UnpaidCostsContext(actionContext, this::getThreatsAddedByCosts);

        for (Requirement requirement : requirements) {
            if (!requirement.accepts(checkContext))
                return false;
        }
        return true;
    }

    private int getThreatsAddedByCosts(ActionContext actionContext) {
        int result = 0;
        for (EffectAppender cost : costs)
            result += cost.getThreatsAddedAsCost(actionContext);
        return result;
    }

    @Override
    public void createAction(CostToEffectAction action, ActionContext actionContext) {
        if (text != null)
            action.setText(GameUtils.substituteText(text, actionContext));

        for (EffectAppender cost : costs)
            cost.appendEffect(true, action, actionContext);

        for (EffectAppender actionEffect : effects)
            actionEffect.appendEffect(false, action, actionContext);
    }
}
