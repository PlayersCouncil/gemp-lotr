package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AssignmentEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class AssignFpCharacterToSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "fpCharacter", "minion", "memorizeMinion",
                "memorizeFPCharacter", "ignoreExistingAssignments", "ignoreDefender", "ignoreAllyLocation", "preventCost", "preventText", "insteadEffect");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String fpCharacter = FieldUtils.getString(effectObject.get("fpCharacter"), "fpCharacter", "choose(any)");
        final String minion = FieldUtils.getString(effectObject.get("minion"), "minion", "choose(any)");

        JSONObject[] preventCostArray = FieldUtils.getObjectArray(effectObject.get("preventCost"), "preventCost");
        JSONObject[] insteadEffectArray = FieldUtils.getObjectArray(effectObject.get("insteadEffect"), "insteadEffect");
        final String preventText = FieldUtils.getString(effectObject.get("preventText"), "preventText");
        if ((preventCostArray.length > 0 && preventText == null) || (preventCostArray.length == 0 && preventText != null))
            throw new InvalidCardDefinitionException("preventText and preventCost have to be specified (or not) together");
        if (insteadEffectArray.length > 0 && preventCostArray.length == 0)
            throw new InvalidCardDefinitionException("preventCost is required if insteadEffect is present");
        EffectAppender[] preventEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(false, preventCostArray, environment);
        EffectAppender[] insteadEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(false, insteadEffectArray, environment);

        final String minionMemory = FieldUtils.getString(effectObject.get("memorizeMinion"), "memorizeMinion", "_tempMinion");
        final String fpCharacterMemory = FieldUtils.getString(effectObject.get("memorizeFPCharacter"), "memorizeFPCharacter", "_tempFpCharacter");
        final boolean ignoreExistingAssignments = FieldUtils.getBoolean(effectObject.get("ignoreExistingAssignments"), "ignoreExistingAssignments", false);
        final boolean ignoreDefender = FieldUtils.getBoolean(effectObject.get("ignoreDefender"), "ignoreDefender", true);
        final boolean ignoreAllyLocation = FieldUtils.getBoolean(effectObject.get("ignoreAllyLocation"), "ignoreAllyLocation", false);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource minionFilter = getSource(minion, environment);

        final EffectAppender fpCharacterResolver = CardResolver.resolveCard(fpCharacter,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Filterable filter = minionFilter.getFilterable(actionContext);
                            return Filters.assignableToSkirmishAgainst(assigningSide, filter, ignoreExistingAssignments, ignoreDefender, ignoreAllyLocation);
                        }, fpCharacterMemory, player, "Choose character to assign to skirmish", environment);

        // An assignment that is a cost ("Exert Éowyn and assign a minion to the Ring-bearer to ...", 17_96; in
        // a definition, the cost of a costToEffect among the action's effects) can be paid while there is a
        // character one of those minions can be assigned to, and that is asked of the character alone: the
        // minion is chosen once the character is, and the character is offered only where a minion can be
        // assigned to it and any cost of assigning that minion paid (ExtraAssignmentCostModifier).  The action
        // is withheld without it, and it is asked on the board as the action's own costs will leave it
        // (CostToEffect, DefaultActionSource.addEffectCheck), so that an exertion the action itself takes
        // first is not counted on again to pay for the minion (F-0486; F-0184).  An assignment that is an
        // effect is asked as before, and so is one whose character or minion is read from the action's memory,
        // which nothing can be asked of before the action is taken.
        final boolean askedAheadAsCost = cost && !CardResolver.readsMemory(fpCharacter) && !CardResolver.readsMemory(minion);
        MultiEffectAppender result = new MultiEffectAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                if (askedAheadAsCost)
                    return fpCharacterResolver.isPlayableInFull(actionContext);
                return super.isPlayableInFull(actionContext);
            }

            @Override
            public boolean isPlayabilityCheckedAsCostOfEffect() {
                return askedAheadAsCost;
            }
        };
        result.addEffectAppender(fpCharacterResolver);
        result.addEffectAppender(
                CardResolver.resolveCard(minion,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory(fpCharacterMemory);
                            return Filters.assignableToSkirmishAgainst(assigningSide, Filters.in(fpChar), ignoreExistingAssignments, ignoreDefender, ignoreAllyLocation);
                        }, minionMemory, player, "Choose minion to assign to character", environment));

        DelayedAppender assignAppender = new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String assigningPlayer = playerSource.getPlayer(actionContext);
                final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory(fpCharacterMemory);
                final Collection<? extends PhysicalCard> minion = actionContext.getCardsFromMemory(minionMemory);
                if (fpChar.size() == 1 && minion.size() == 1) {
                    final PhysicalCard assignedCharacter = fpChar.iterator().next();
                    final PhysicalCard assignedMinion = minion.iterator().next();
                    final AssignmentEffect assignment = new AssignmentEffect(assigningPlayer, assignedCharacter, assignedMinion, ignoreExistingAssignments, ignoreDefender, ignoreAllyLocation);

                    // A cost of assigning this pair ("the Free Peoples player must exert a companion to assign
                    // this minion", ExtraCostToAssign) is paid here, the pair being known, and the assignment is
                    // the effect of the payment: unpaid (a burden prevented, nobody left to exert) it is not
                    // made.  Where the assignment is itself a cost of the card's action, that cost then fails
                    // (Q-0221).
                    final CostToEffectAction payment = actionContext.getGame().getModifiersQuerying()
                            .getAssignmentCosts(actionContext.getGame(), assigningPlayer, assignedCharacter, assignedMinion);
                    if (payment == null)
                        return assignment;
                    payment.appendEffect(assignment.withCostPaid());
                    return new StackActionEffect(payment);
                } else {
                    return null;
                }
            }
        };

        if (preventEffectAppenders.length > 0) {
            result.addEffectAppender(
                    new PreventableEffectAppender(new OpponentPlayerSource(playerSource), preventText,
                            actionContext -> {
                                final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory(fpCharacterMemory);
                                final Collection<? extends PhysicalCard> minion1 = actionContext.getCardsFromMemory(minionMemory);
                                return fpChar.size() == 1 && minion1.size() == 1;
                            }, preventEffectAppenders, new EffectAppender[]{assignAppender}, insteadEffectAppenders));
        } else {
            result.addEffectAppender(assignAppender);
        }

        // Taught for Q-0224 (IdleOffers): with no character one of those minions could be assigned to, nobody is
        // assigned.
        return TaughtForecast.actsOnWhatItFinds(result, fpCharacterResolver);
    }

    private FilterableSource getSource(String filter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (filter.startsWith("choose(") && filter.endsWith(")"))
            return environment.getFilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")), environment);
        if (filter.equals("self"))
            return ActionContext::getSource;

        return environment.getFilterFactory().generateFilter(filter, environment);
        //What exactly was the point of this?  Pulling from other sources should be just fine.
        //I wish I knew what bug this was addressing.
        //throw new InvalidCardDefinitionException("The selectors for this effect have to be of 'choose' type or 'self'");
    }
}
