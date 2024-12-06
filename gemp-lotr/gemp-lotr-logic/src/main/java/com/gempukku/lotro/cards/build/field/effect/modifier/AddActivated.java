package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.DefaultActionSource;
import com.gempukku.lotro.cards.build.field.effect.EffectUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.AbstractEffectAppender;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.logic.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.logic.modifiers.AddActionToCardModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AddActivated implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String text = FieldUtils.getString(object.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(object.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(object.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(object.get("limitPerTurn"), "limitPerTurn", 0);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        List<DefaultActionSource> actionSources = new LinkedList<>();

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext.getSource(), limitPerTurn);
                            }
                        });
            }
            actionSource.addPlayRequirement(
                    (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase));
            EffectUtils.processRequirementsCostsAndEffects(object, environment, actionSource);

            actionSources.add(actionSource);
        }

        return actionContext -> new AddActionToCardModifier(actionContext.getSource(), null, filterableSource.getFilterable(actionContext)) {
            @Override
            protected List<? extends ActivateCardAction> createExtraPhaseActions(LotroGame game, PhysicalCard card) {
                LinkedList<ActivateCardAction> result = new LinkedList<>();
                for (ActionSource inPlayPhaseAction : actionSources) {
                    DefaultActionContext actionContext = new DefaultActionContext(card.getOwner(), game, card, null, null);
                    if (inPlayPhaseAction.isValid(actionContext)) {
                        ActivateCardAction action = new ActivateCardAction(card);
                        inPlayPhaseAction.createAction(action, actionContext);
                        result.add(action);
                    }
                }

                return result;
            }
        };
    }
}
