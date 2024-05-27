package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.modifier.RequirementCondition;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.YesNoDecision;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.FreePeoplePlayerMayNotAssignCharacterModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.PaidAssignmentCostModifier;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AssignmentCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "text", "requires", "cost");

        String text = FieldUtils.getString(value.get("text"), "text");
        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(value.get("requires"), "requires");

        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for AssignmentCost effect");

        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        blueprint.appendInPlayModifier(
                new ModifierSource() {
                    @Override
                    public Modifier getModifier(ActionContext actionContext) {
                        return new FreePeoplePlayerMayNotAssignCharacterModifier(actionContext.getSource(),
                                new AndCondition(
                                        RequirementCondition.createCondition(requirements, actionContext),
                                        new Condition() {
                                            @Override
                                            public boolean isFullfilled(LotroGame game) {
                                                return !game.getModifiersQuerying().assignmentCostWasPaid(game, actionContext.getSource());
                                            }
                                        }
                                ), actionContext.getSource());
                    }
                });

        DefaultActionSource payAssignmentCostActionSource = new DefaultActionSource();
        payAssignmentCostActionSource.setText("Pay assignment cost");

        for (Requirement requirement : requirements) {
            payAssignmentCostActionSource.addPlayRequirement(requirement);
        }
        payAssignmentCostActionSource.addPlayRequirement(
                new Requirement() {
                    @Override
                    public boolean accepts(ActionContext actionContext) {
                        LotroGame game = actionContext.getGame();
                        GameState gameState = game.getGameState();

                        return TriggerConditions.freePlayerStartedAssigning(game, actionContext.getEffectResult())
                                && (gameState.isNormalSkirmishes() || (
                                gameState.isFierceSkirmishes() && game.getModifiersQuerying().hasKeyword(game, actionContext.getSource(), Keyword.FIERCE)));
                    }
                }
        );

        payAssignmentCostActionSource.addEffect(
                new EffectAppender() {
                    @Override
                    public void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        String currentPlayerId = actionContext.getGame().getGameState().getCurrentPlayerId();

                        SubAction optionalDecisionAction = new SubAction(action);
                        optionalDecisionAction.appendCost(
                                new PlayoutDecisionEffect(currentPlayerId,
                                        new YesNoDecision(GameUtils.SubstituteText(text, actionContext)) {
                                            @Override
                                            protected void yes() {
                                                ActionContext delegate = new DelegateActionContext(actionContext,
                                                        currentPlayerId, actionContext.getGame(), actionContext.getSource(),
                                                        actionContext.getEffectResult(), actionContext.getEffect());
                                                for (EffectAppender costAppender : costAppenders) {
                                                    costAppender.appendEffect(true, optionalDecisionAction, delegate);
                                                }

                                                optionalDecisionAction.appendEffect(
                                                        new AddUntilEndOfPhaseModifierEffect(
                                                                new PaidAssignmentCostModifier(actionContext.getSource(), null, actionContext.getSource())));
                                            }
                                        }));

                        action.appendEffect(new StackActionEffect(optionalDecisionAction));
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        for (EffectAppender costAppender : costAppenders) {
                            if (!costAppender.isPlayableInFull(actionContext))
                                return false;
                        }

                        return true;
                    }
                }
        );
    }
}
