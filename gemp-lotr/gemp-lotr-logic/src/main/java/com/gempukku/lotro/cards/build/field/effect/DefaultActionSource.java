package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
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

    public void setPlayingPlayer(PlayerSource playingPlayer) {
        this.playingPlayer = playingPlayer;
    }

    public void setRequiresRanger(boolean requiresRanger) {
        this.requiresRanger = requiresRanger;
    }

    public void setText(String text) {
        this.text = text;
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
    public boolean requiresRanger() {
        return requiresRanger;
    }

    @Override
    public boolean isValid(ActionContext actionContext) {
        if (playingPlayer != null && !playingPlayer.getPlayer(actionContext).equals(actionContext.getPerformingPlayer()))
            return false;

        for (Requirement requirement : requirements) {
            if (!requirement.accepts(actionContext))
                return false;
        }
        return true;
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
