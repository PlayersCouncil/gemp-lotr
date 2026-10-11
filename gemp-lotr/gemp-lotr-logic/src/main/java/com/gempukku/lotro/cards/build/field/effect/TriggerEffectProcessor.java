package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.AbstractEffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementFactory;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerChecker;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.game.state.LotroGame;
import org.json.simple.JSONObject;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "optional", "requires", "noEffectUnless", "cost", "effect", "text", "player", "limitPerTurn", "limitPerPhase", "limitPerSite", "effectLimitPerPhase", "effectLimitPerTurn", "phase");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");
        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");
        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional");
        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");

        final String player = FieldUtils.getString(value.get("player"), "player");
        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            if (playerSource != null) {
                triggerActionSource.setPlayingPlayer(playerSource);
            }
            if (text != null) {
                triggerActionSource.setText(text);
            }
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.nameWhatSetsOff(trigger, triggerActionSource);

            if (triggerChecker.isConstantCheck()) {
                // A "constantly check" trigger is collected afresh for every batch of effect results while its
                // condition holds, so further copies can be queued before the first one resolves (e.g. a wound and a
                // kill happening while the Rivendell Waterfall's check is still pending, #1091). Its requirements -
                // including any per-turn limit the first copy uses up - are therefore re-checked when it resolves,
                // and a copy whose condition no longer holds does nothing.
                final DefaultActionSource source = triggerActionSource;
                // A standing check on where the fellowship is ("discard this possession when at an underground
                // site", "is discarded if not at a marsh") waits while a move is being finished: the fellowship
                // has moved to the new site only when the twilight of the move has been added, so the card is
                // still there, and its text still counts, when that twilight is worked out.
                if (RequirementFactory.asksWhereTheFellowshipIs(trigger.get("requires"))
                        || RequirementFactory.asksWhereTheFellowshipIs(value.get("requires")))
                    triggerActionSource.addPlayRequirement(
                            actionContext -> !actionContext.getGame().getGameState().isFellowshipArriving());
                triggerActionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new AbstractEffect() {
                                    @Override
                                    public boolean isPlayableInFull(LotroGame game) {
                                        return true;
                                    }

                                    @Override
                                    protected FullEffectResult playEffectReturningResult(LotroGame game) {
                                        return new FullEffectResult(source.isValid(actionContext));
                                    }
                                };
                            }
                        });
            }

            // Q-0224 (IdleOffers): an optional trigger that pays nothing and would do nothing.
            triggerActionSource.setWithheldWhenIdle(optional);
            EffectUtils.processLimits(value, triggerActionSource, phase, optional);

            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            if (before) {
                if (optional)
                    blueprint.appendOptionalBeforeTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredBeforeTrigger(triggerActionSource);
            } else {
                if (optional)
                    blueprint.appendOptionalAfterTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredAfterTrigger(triggerActionSource);
            }
        }
    }
}
