package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.UnpaidCostsContext;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.NoEffect;

import java.util.LinkedList;
import java.util.List;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();
    // What the definition says must hold for the action to do anything at all ("noEffectUnless").  Unlike a
    // requirement these do not withhold the action: it is offered, and flagged as having no effect.
    private final List<Requirement> noEffectUnless = new LinkedList<>();

    private final List<EffectAppender> costs = new LinkedList<>();
    private final List<EffectAppender> effects = new LinkedList<>();

    private PlayerSource playingPlayer;
    private boolean requiresRanger;
    private String text;
    private boolean specialAbility = true;
    // Whether what the effects will find can be told before the costs are paid (see NoEffectForesight).
    private boolean judgeable;
    // A special ability used in the skirmish phase, or the playing of an event: the two kinds of action that
    // may be a skirmish action, which is flagged when it reaches nobody in the skirmish being fought.
    private boolean skirmishAbility;
    private boolean event;

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

    public void setJudgeable(boolean judgeable) {
        this.judgeable = judgeable;
    }

    public void setSkirmishAbility(boolean skirmishAbility) {
        this.skirmishAbility = skirmishAbility;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public void addPlayRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void addNoEffectUnless(Requirement requirement) {
        this.noEffectUnless.add(requirement);
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

        final ActionContext checkContext = getUnpaidCostsContext(actionContext);

        for (Requirement requirement : requirements) {
            if (!requirement.accepts(checkContext))
                return false;
        }
        return true;
    }

    // The action is being asked whether it can be used, and none of its costs has been paid: what they
    // will have done to the board by the time its effects are carried out is there for a requirement to ask.
    private ActionContext getUnpaidCostsContext(ActionContext actionContext) {
        return costs.isEmpty() ? actionContext
                : new UnpaidCostsContext(actionContext, this::getThreatsAddedByCosts);
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

        // Asked only if the action is put in front of a player, and then once.
        action.setNoEffectJudge(() -> judge(actionContext));
    }

    /**
     * Judges, before the action is taken, whether it will do nothing.  The first ground that holds gives the
     * reason.  Where there is any doubt the answer is null: an action wrongly flagged is worse than one that is
     * not flagged and should have been.
     * @return Why the action will do nothing, or null if it is not known to.
     */
    private NoEffect judge(ActionContext actionContext) {
        // The definition's own word for it: something it names is missing, and without it nothing happens.
        if (!noEffectUnless.isEmpty()) {
            final ActionContext checkContext = getUnpaidCostsContext(actionContext);
            for (Requirement requirement : noEffectUnless) {
                if (!requirement.accepts(checkContext))
                    return NoEffect.EFFECT;
            }
        }

        // From here on the action is judged by what its effects forecast of themselves, which is only to be
        // trusted where no cost stands between the board as it is and the board they will find.
        if (!judgeable || hasCostsOfItsCardsPlay(actionContext))
            return null;

        final List<EffectForecast> forecasts = new LinkedList<>();
        for (EffectAppender effect : effects)
            forecasts.add(effect.forecast(actionContext));
        final EffectForecast forecast = EffectForecast.together(forecasts);

        // Every effect will do nothing: there is nobody to give it to, or nothing for it to act on.  An action
        // that is all bookkeeping says nothing either way.
        if (!forecast.isBookkeeping()) {
            if (forecast.getVerdict() == EffectForecast.Verdict.AT_LIMIT)
                return NoEffect.LIMIT;
            if (forecast.getVerdict() == EffectForecast.Verdict.NOTHING)
                return NoEffect.EFFECT;
        }

        // A skirmish action all of whose doing is strength, damage or protection from being overwhelmed, for
        // this skirmish only, and none of it for anybody who is fighting.  What it does reach is real (the
        // character is stronger until the skirmish ends) and is of no use to anybody.  Anything else an action
        // might do, a wound or a card drawn, a bonus that lasts, leaves it unflagged.
        if (forecast.getVerdict() == EffectForecast.Verdict.UNKNOWN && forecast.isSkirmishBound()
                && forecast.getReach() != null && isSkirmishAction(actionContext)) {
            final LotroGame game = actionContext.getGame();
            for (PhysicalCard reached : forecast.getReach()) {
                if (Filters.inSkirmish.accepts(game, reached))
                    return null;
            }
            return NoEffect.SKIRMISH;
        }

        return null;
    }

    // Whether the action is being offered as a skirmish action in a skirmish that is being fought: a skirmish
    // special ability, or an event with the Skirmish timeword, never a response or a trigger.
    private boolean isSkirmishAction(ActionContext actionContext) {
        final LotroGame game = actionContext.getGame();
        if (game.getGameState().getCurrentPhase() != Phase.SKIRMISH)
            return false;
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null || skirmish.isCancelled())
            return false;
        if (skirmishAbility)
            return true;
        final PhysicalCard source = actionContext.getSource();
        return event && source != null && source.getBlueprint().hasTimeword(Timeword.SKIRMISH);
    }

    // An event is paid for as it is played, and what it then costs is not all written in its own costs: its
    // card may have a cost to play of its own, another card may add one, and a discount (toil) may be taken by
    // exerting.  Those are costs this action source knows nothing of, so an event that has any is not judged.
    private static boolean hasCostsOfItsCardsPlay(ActionContext actionContext) {
        final PhysicalCard source = actionContext.getSource();
        if (source == null || source.getBlueprint().getCardType() != CardType.EVENT)
            return false;
        final LotroGame game = actionContext.getGame();
        final CostProbe probe = new CostProbe();
        game.getModifiersQuerying().appendExtraCosts(game, probe, source);
        game.getModifiersQuerying().appendPotentialDiscounts(game, probe, source);
        return probe.asked;
    }

    // Stands in for an action to find out whether anything would add a cost or a discount to it.
    private static final class CostProbe implements CostToEffectAction {
        private boolean asked;

        @Override
        public void appendPotentialDiscount(DiscountEffect cost) {
            asked = true;
        }

        @Override
        public void insertCost(Effect... cost) {
            asked = true;
        }

        @Override
        public void appendCost(Effect cost) {
            asked = true;
        }

        @Override
        public void insertEffect(Effect... effect) {
            asked = true;
        }

        @Override
        public void appendEffect(Effect effect) {
            asked = true;
        }

        @Override
        public void setText(String text) {
        }

        @Override
        public void setPaidToil(boolean toilPaid) {
        }

        @Override
        public boolean wasCarriedOut() {
            return false;
        }

        @Override
        public Type getType() {
            return Type.OTHER;
        }

        @Override
        public PhysicalCard getActionSource() {
            return null;
        }

        @Override
        public void setActionTimeword(Timeword timeword) {
        }

        @Override
        public PhysicalCard getActionAttachedToCard() {
            return null;
        }

        @Override
        public void setVirtualCardAction(boolean virtualCardAction) {
        }

        @Override
        public boolean isVirtualCardAction() {
            return false;
        }

        @Override
        public void setPerformingPlayer(String playerId) {
        }

        @Override
        public String getPerformingPlayer() {
            return null;
        }

        @Override
        public Timeword getActionTimeword() {
            return null;
        }

        @Override
        public String getText(LotroGame game) {
            return null;
        }

        @Override
        public Effect nextEffect(LotroGame game) {
            return null;
        }
    }
}
