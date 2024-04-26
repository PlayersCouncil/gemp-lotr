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
import java.util.Collections;
import java.util.List;

public class AssignFpCharacterToSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "fpCharacter", "against");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String fpCharacter = FieldUtils.getString(effectObject.get("fpCharacter"), "fpCharacter");
        final String against = FieldUtils.getString(effectObject.get("against"), "against");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        final FilterableSource minionFilter = getSource(against, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(fpCharacter,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Filterable filter = minionFilter.getFilterable(actionContext);
                            return Filters.assignableToSkirmishAgainst(assigningSide, filter);
                        }, "_tempFpCharacter", player, "Choose character to assign to skirmish", environment));
        result.addEffectAppender(
                CardResolver.resolveCard(against,
                        (actionContext) -> {
                            final String assigningPlayer = playerSource.getPlayer(actionContext);
                            Side assigningSide = GameUtils.getSide(actionContext.getGame(), assigningPlayer);
                            final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory("_tempFpCharacter");
                            return Filters.assignableToSkirmishAgainst(assigningSide, Filters.in(fpChar));
                        }, "_tempMinion", player, "Choose minion to assign to character", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final String assigningPlayer = playerSource.getPlayer(actionContext);
                        final Collection<? extends PhysicalCard> fpChar = actionContext.getCardsFromMemory("_tempFpCharacter");
                        final Collection<? extends PhysicalCard> minion = actionContext.getCardsFromMemory("_tempMinion");
                        if (fpChar.size() == 1 && minion.size() == 1) {
                            return Collections.singletonList(
                                    new AssignmentEffect(assigningPlayer, fpChar.iterator().next(), minion.iterator().next()));
                        }
                        return null;
                    }
                });

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
