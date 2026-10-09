package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.cards.build.LimitRequirement;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.UnpaidCostsContext;
import com.gempukku.lotro.cards.build.field.effect.appender.AbstractEffectAppender;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.logic.effects.IncrementSiteLimitEffect;
import com.gempukku.lotro.logic.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.logic.modifiers.LimitCounter;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.NoEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();
    // Those of the requirements that are limits on uses.  A required trigger asks them again when it comes to
    // be carried out (see createAction).
    private final List<LimitRequirement> limitRequirements = new LinkedList<>();
    // What the definition says must hold for the action to do anything at all ("noEffectUnless").  Unlike a
    // requirement these do not withhold the action: it is offered, and flagged as having no effect.
    private final List<Requirement> noEffectUnless = new LinkedList<>();

    private final List<EffectAppender> costs = new ArrayList<>();
    // Those of the costs that are left out when the action's card is played "ignoring all costs" (see
    // addCheckedCost and createAction).
    private final List<EffectAppender> costsThatCanBePassedOver = new ArrayList<>();
    private final List<EffectAppender> effects = new LinkedList<>();
    // The limits that do not withhold the action ("effectLimitPerPhase", "effectLimitPerTurn" on an ability that
    // has a cost and is the player's to use or not: a limit on what the effect gives, "limit +3"): at its limit
    // the action is still offered, its costs are paid, and the gate lets none of its effects through (Ketura on
    // Q-0041 and Q-0093).  A limit on uses ("limit once per turn") withholds the action.  See addLimitPerPhase.
    private final List<Limit> limitGate = new LinkedList<>();

    private PlayerSource playingPlayer;
    private boolean requiresRanger;
    private String text;
    private String setOffByMemory;
    private boolean specialAbility = true;
    // Whether what the effects will find can be told before the costs are paid (see NoEffectForesight).
    private boolean judgeable;
    // Whether the action is a reveal from a draw deck and what is then done with the cards revealed, and nothing
    // else (NoEffectForesight.isARevealAndWhatIsDoneWithTheCards), and whether its definition writes a cost.
    private boolean revealAndNothingElse;
    private boolean costWritten;
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

    /**
     * For an action that a trigger sets off: the memory in which the trigger remembers what set it off
     * ("memorize: winner").  Each action made is told the names of those cards, so that two offers of it in one
     * decision, for two winners of one skirmish, can be told apart (F-0137).
     */
    public void setSetOffByMemory(String setOffByMemory) {
        this.setOffByMemory = setOffByMemory;
    }

    public void setSpecialAbility(boolean specialAbility) {
        this.specialAbility = specialAbility;
    }

    public void setJudgeable(boolean judgeable) {
        this.judgeable = judgeable;
    }

    public void setRevealAndNothingElse(boolean revealAndNothingElse) {
        this.revealAndNothingElse = revealAndNothingElse;
    }

    public void setSkirmishAbility(boolean skirmishAbility) {
        this.skirmishAbility = skirmishAbility;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public void addPlayRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        if (requirement instanceof LimitRequirement limitRequirement)
            this.limitRequirements.add(limitRequirement);
    }

    /**
     * Limits the action to so many uses of its card in a phase.  The counter is the card's, not the ability's.
     * @param phase The phase the uses are counted in, or null for whichever phase the action is used in.
     * @param limit How many uses there may be.
     * @param offeredAtLimit False withholds the action once the limit is reached: a requirement, and a use
     *                       counted before any other cost.  That is a limit on uses, and any limit on an action
     *                       with no cost.  True leaves it on offer (the limit is on what its effect gives, and it
     *                       has a cost the player may want to pay for its own sake): its costs are paid as
     *                       written, a use is counted once they are, and at the limit nothing more is done.  It
     *                       is then flagged as doing nothing.  See EffectUtils.processLimits.
     */
    public void addLimitPerPhase(Phase phase, int limit, boolean offeredAtLimit) {
        if (offeredAtLimit) {
            limitGate.add(new Limit(phase, false, limit));
            return;
        }
        addPlayRequirement(
                (LimitRequirement) (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limit));
        addCost(
                new AbstractEffectAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limit);
                    }
                });
    }

    /**
     * Limits the action to so many uses of its card in a turn.  See addLimitPerPhase.
     */
    public void addLimitPerTurn(int limit, boolean offeredAtLimit) {
        if (offeredAtLimit) {
            limitGate.add(new Limit(null, true, limit));
            return;
        }
        addPlayRequirement(
                (LimitRequirement) (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit));
        addCost(
                new AbstractEffectAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new IncrementTurnLimitEffect(actionContext.getSource(), limit);
                    }
                });
    }

    /**
     * Limits the action to so many uses of its card at a site: "limit once per site".  The count is the card's,
     * is cleared when the fellowship moves to another site and at the end of the turn, and at the limit the
     * action is withheld: a limit on uses (Ketura on Q-0084, Q-0093 and F-0146).
     */
    public void addLimitPerSite(int limit) {
        addPlayRequirement(
                (LimitRequirement) (actionContext) -> PlayConditions.checkSiteLimit(actionContext.getGame(), actionContext.getSource(), limit));
        addCost(
                new AbstractEffectAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new IncrementSiteLimitEffect(actionContext.getSource(), limit);
                    }
                });
    }

    // One limit of the gate: so many uses of the card in a phase, or in a turn.
    private static final class Limit {
        private final Phase phase;
        private final boolean perTurn;
        private final int max;

        private Limit(Phase phase, boolean perTurn, int max) {
            this.phase = phase;
            this.perTurn = perTurn;
            this.max = max;
        }

        private LimitCounter getCounter(ActionContext actionContext) {
            final LotroGame game = actionContext.getGame();
            if (perTurn)
                return game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(actionContext.getSource());
            final Phase countedIn = (phase != null) ? phase : game.getGameState().getCurrentPhase();
            return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(actionContext.getSource(), countedIn);
        }

        private boolean isReached(ActionContext actionContext) {
            return getCounter(actionContext).getUsedLimit() >= max;
        }
    }

    // Whether a limit on the uses of the action has been reached.
    private boolean isLimitReached(ActionContext actionContext) {
        for (LimitRequirement limitRequirement : limitRequirements) {
            if (!limitRequirement.accepts(actionContext))
                return true;
        }
        return false;
    }

    // Whether the action, used now, would get nothing past its gate.  The judge and the gate both ask this.
    private boolean isAtLimit(ActionContext actionContext) {
        for (Limit limit : limitGate) {
            if (limit.isReached(actionContext))
                return true;
        }
        return false;
    }

    public void addNoEffectUnless(Requirement requirement) {
        this.noEffectUnless.add(requirement);
    }

    public void addCost(EffectAppender effectAppender) {
        costs.add(effectAppender);
    }

    /**
     * Adds a cost the action is not offered without: one of the costs written in its definition.  Whether it
     * can be paid is asked on the board as the costs before it will leave it (see isValid).
     * @param canBePassedOver Whether the cost is left out when the action's card is played "ignoring all costs":
     *                        false for a cost the rest of the text reads back, or that holds an effect of the
     *                        card (EffectUtils.canBePassedOver).
     */
    public void addCheckedCost(EffectAppender effectAppender, boolean canBePassedOver) {
        addPlayRequirement(new OrderedCheck(effectAppender::isPlayableInFull, costs.size()));
        costs.add(effectAppender);
        costWritten = true;
        if (canBePassedOver)
            costsThatCanBePassedOver.add(effectAppender);
    }

    /**
     * Has the action withheld unless the given one of its effects can be carried out: a play from a pile, which
     * is to be treated as a cost (F-0046).  It is asked on the board as all of the action's costs will leave it.
     */
    public void addEffectCheck(EffectAppender effectAppender) {
        addPlayRequirement(new OrderedCheck(effectAppender::isPlayableInFull, OrderedCheck.ALL_COSTS));
    }

    // A requirement that asks whether one of the action's own costs can be paid, or whether an effect it is
    // withheld without can be carried out.  Costs are judged in the order printed (Ketura on Q-0080), so each
    // is asked on the board as the costs before it will leave it, as far as those can tell beforehand what
    // they will do (EffectAppender.tallyWoundsPlacedAsCost).
    private static final class OrderedCheck implements Requirement {
        private static final int ALL_COSTS = -1;

        private final Requirement check;
        private final int costsBefore;

        private OrderedCheck(Requirement check, int costsBefore) {
            this.check = check;
            this.costsBefore = costsBefore;
        }

        @Override
        public boolean accepts(ActionContext actionContext) {
            return check.accepts(actionContext);
        }
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
        final GameState gameState = actionContext.getGame().getGameState();

        // The wounds the costs gone through so far will place for certain: exertions of the action's own card,
        // say.  A requirement of the definition is asked on the board as it stands; each cost, and each effect
        // that must be able to happen, is asked with the wounds of the costs before it in place (F-0184).  The
        // checks of the costs come in the order of the costs, so one pass through them does it.
        final Map<PhysicalCard, Integer> woundsToCome = new HashMap<>();
        int talliedCosts = 0;

        for (Requirement requirement : requirements) {
            if (requirement instanceof OrderedCheck ordered) {
                final int costsBefore = ordered.costsBefore == OrderedCheck.ALL_COSTS ? costs.size() : ordered.costsBefore;
                while (talliedCosts < costsBefore) {
                    final EffectAppender cost = costs.get(talliedCosts++);
                    final Map<PhysicalCard, Integer> placed = new HashMap<>();
                    gameState.withWoundsToCome(woundsToCome, () -> {
                        cost.tallyWoundsPlacedAsCost(checkContext, placed);
                        return null;
                    });
                    for (Map.Entry<PhysicalCard, Integer> entry : placed.entrySet())
                        woundsToCome.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
                if (!gameState.withWoundsToCome(woundsToCome, () -> ordered.accepts(checkContext)))
                    return false;
            } else if (!requirement.accepts(checkContext)) {
                return false;
            }
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

        if (setOffByMemory != null) {
            final Collection<? extends PhysicalCard> setOffBy = actionContext.getCardsFromMemory(setOffByMemory);
            if (setOffBy != null && !setOffBy.isEmpty())
                action.setOfferedFor(GameUtils.getAppendedTextNames(setOffBy));
        }

        // A required trigger is raised when its event happens and carried out later, after any other trigger
        // the same event raised.  If one of those was this card's too (two companions exerting at once, and a
        // card that answers each exertion once per site), the limit it passed when raised may be used up by
        // the time its turn comes: it is then not carried out (F-0145).
        if (action instanceof RequiredTriggerAction requiredTrigger && !limitRequirements.isEmpty())
            requiredTrigger.setSpentCheck(() -> isLimitReached(actionContext));

        // An event that another card plays "ignoring all costs" is not asked for the costs in its own text
        // (Ketura on F-0316), any more than for its twilight cost.  A cost its text reads back is still
        // carried out: see EffectUtils.canBePassedOver.
        final boolean costsIgnored = action instanceof PlayEventAction playEvent && playEvent.isCostsIgnored();
        for (EffectAppender cost : costs) {
            if (costsIgnored && costsThatCanBePassedOver.contains(cost))
                continue;
            cost.appendEffect(true, action, actionContext);
        }

        if (limitGate.isEmpty()) {
            for (EffectAppender actionEffect : effects)
                actionEffect.appendEffect(false, action, actionContext);
        } else {
            appendEffectsBehindLimitGate(action, actionContext);
        }

        // Asked only if the action is put in front of a player, and then once.
        action.setNoEffectJudge(() -> judge(actionContext));
    }

    // An action that stays on offer at its limit.  Once its costs are paid a use is counted, if there is one left
    // (the last of its costs, so a use that is cancelled after being paid for is spent all the same, and one
    // whose costs fail is not).  Its effects wait behind the gate: within the limit they are appended when the
    // gate is reached, and at the limit the log says so and nothing is appended.
    private void appendEffectsBehindLimitGate(CostToEffectAction action, ActionContext actionContext) {
        final boolean[] withinLimit = new boolean[1];
        action.appendCost(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        if (isAtLimit(actionContext))
                            return;
                        for (Limit limit : limitGate)
                            limit.getCounter(actionContext).incrementToLimit(limit.max, 1);
                        withinLimit[0] = true;
                    }
                });
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        if (withinLimit[0]) {
                            for (EffectAppender actionEffect : effects)
                                actionEffect.appendEffect(false, action, actionContext);
                        } else {
                            game.getGameState().sendMessage(GameUtils.getCardLink(actionContext.getSource())
                                    + " has reached its limit and does nothing");
                        }
                    }
                });
    }

    /**
     * Judges, before the action is taken, whether it will do nothing.  The first ground that holds gives the
     * reason.  Where there is any doubt the answer is null: an action wrongly flagged is worse than one that is
     * not flagged and should have been.
     * @return Why the action will do nothing, or null if it is not known to.
     */
    private NoEffect judge(ActionContext actionContext) {
        // The action is at its limit: the gate reads the same counter when the effects come up, and no cost
        // lowers a counter.
        if (isAtLimit(actionContext))
            return NoEffect.LIMIT;

        // The definition's own word for it: something it names is missing, and without it nothing happens.
        if (!noEffectUnless.isEmpty()) {
            final ActionContext checkContext = getUnpaidCostsContext(actionContext);
            for (Requirement requirement : noEffectUnless) {
                if (!requirement.accepts(checkContext))
                    return NoEffect.EFFECT;
            }
        }

        // A reveal that a cost buys, from a draw deck with nothing in it, in an action that does nothing else
        // but deal with the cards revealed: it is offered, and does nothing (Ketura on Q-0163).  This is asked
        // apart from the forecasts below.  Those say nothing of an effect that reads what the reveal remembers;
        // and the one thing a cost could do to this action is put a card in that deck, which is narrower than
        // what makes an action judgeable.  An ability or trigger with no cost is left as it was: whether that
        // is offered at all with nothing to reveal is a question of its own (Q-0034, Q-0148).
        if (revealAndNothingElse && (event || costWritten) && !hasCostsOfItsCardsPlay(actionContext)
                && effects.get(0).forecast(actionContext).getVerdict() == EffectForecast.Verdict.NOTHING)
            return NoEffect.EFFECT;

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
