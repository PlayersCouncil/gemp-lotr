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
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class AssignFpCharacterToSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "fpCharacter", "minion", "memorizeMinion", "memorizeFPCharacter", "ignoreExistingAssignments", "preventCost", "preventText", "insteadEffect");

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
        EffectAppender[] preventEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(preventCostArray, environment);
        EffectAppender[] insteadEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(insteadEffectArray, environment);

        final String minionMemory = FieldUtils.getString(effectObject.get("memorizeMinion"), "memorizeMinion", "_tempMinion");
        final String fpCharacterMemory = FieldUtils.getString(effectObject.get("memorizeFPCharacter"), "memorizeFPCharacter", "_tempFpCharacter");
        final boolean ignoreExistingAssignments = FieldUtils.getBoolean(effectObject.get("ignoreExistingAssignments"), "ignoreExistingAssignments", false);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource minionFilter = getSource(minion, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(fpCharacter,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Filterable filter = minionFilter.getFilterable(actionContext);
                            return Filters.assignableToSkirmishAgainst(assigningSide, filter, ignoreExistingAssignments, false);
                        }, fpCharacterMemory, player, "Choose character to assign to skirmish", environment));
        result.addEffectAppender(
                CardResolver.resolveCard(minion,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory(fpCharacterMemory);
                            return Filters.assignableToSkirmishAgainst(assigningSide, Filters.in(fpChar), ignoreExistingAssignments, false);
                        }, minionMemory, player, "Choose minion to assign to character", environment));

        DelayedAppender assignAppender = new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String assigningPlayer = playerSource.getPlayer(actionContext);
                final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory(fpCharacterMemory);
                final Collection<? extends PhysicalCard> minion = actionContext.getCardsFromMemory(minionMemory);
                if (fpChar.size() == 1 && minion.size() == 1) {
                    AssignmentEffect effect = new AssignmentEffect(assigningPlayer, fpChar.iterator().next(), minion.iterator().next());
                    effect.setIgnoreSingleMinionRestriction(ignoreExistingAssignments);
                    return effect;
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

        return result;
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
