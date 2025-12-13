package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Choice implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player",  "texts", "effects", "requires", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String[] textArray = FieldUtils.getStringArray(effectObject.get("texts"), "texts");
        final JSONObject[][] effectArrayOfArrays = FieldUtils.getNestedObjectArray(effectObject.get("effects"), "effects");
        final JSONObject[] requirementArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        if (effectArrayOfArrays.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of texts and effects does not match in Choice effect.");

        //If requirements are not omitted (and they are for the vast majority of Choices), then we expect that all
        // choice options include a requirement; this can be the simple string "AlwaysAvailable" to mark choices which
        // are not conditional.
        if (requirementArray.length > 0 && requirementArray.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of requirements must either be zero or include 1 requirement per text item in Choice effect.");

        //Choices now support arrays of effects so that the developer does not need to wrap everything manually in a Multiple
        EffectAppender[] possibleEffectAppenders = environment.getEffectAppenderFactory().getNestedEffectAppenders(cost, effectArrayOfArrays, environment);
        Requirement[] possibleRequirements = environment.getRequirementFactory().getRequirements(requirementArray, environment);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                int textIndex = 0;
                List<EffectAppender> playableEffectAppenders = new LinkedList<>();
                List<String> effectTexts = new LinkedList<>();

                for (textIndex = 0; textIndex < textArray.length; ++textIndex) {
                    boolean prereqs = true;
                    if(possibleRequirements != null && possibleRequirements.length != 0) {
                        prereqs = possibleRequirements[textIndex].accepts(delegate);
                    }

                    if(prereqs && possibleEffectAppenders[textIndex].isPlayableInFull(delegate)){
                        playableEffectAppenders.add(possibleEffectAppenders[textIndex]);
                        effectTexts.add(GameUtils.substituteText(textArray[textIndex], actionContext));
                    }
                }

                if (playableEffectAppenders.size() == 1) {
                    SubAction subAction = new SubAction(action);
                    playableEffectAppenders.getFirst().appendEffect(cost, subAction, delegate);
                    actionContext.setValueToMemory(memorize, textArray[0]);
                    return new StackActionEffect(subAction);
                }

                if (playableEffectAppenders.isEmpty()) {
                    actionContext.setValueToMemory(memorize, "");

                    // Costs that are a choice should not let the player pick freely, else it effectively results in there
                    // being no cost at all.
                    if(cost)
                        return null;

                    // If we are an effect however, then we will let "no valid choices" default to the player choosing
                    // which fork to go down; this should eliminate situations like Here Lies Balin doing nothing
                    // if there is a single 1-vitality Orc on the field
                    textIndex = 0;
                    for (var possibleEffectAppender : possibleEffectAppenders) {
                        playableEffectAppenders.add(possibleEffectAppender);
                        effectTexts.add(GameUtils.substituteText(textArray[textIndex], actionContext));
                        textIndex++;
                    }
                }

                SubAction subAction = new SubAction(action);
                subAction.appendCost(
                        new PlayoutDecisionEffect(choosingPlayer,
                                new MultipleChoiceAwaitingDecision(1, "Choose action to perform", effectTexts.toArray(new String[0])) {
                                    @Override
                                    protected void validDecisionMade(int index, String result) {
                                        playableEffectAppenders.get(index).appendEffect(cost, subAction, delegate);
                                        actionContext.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                for (int i = 0; i < textArray.length; ++i) {

                    boolean prereqs = true;
                    if(possibleRequirements != null && possibleRequirements.length != 0) {
                        prereqs = possibleRequirements[i].accepts(delegate);
                    }

                    if(prereqs && possibleEffectAppenders[i].isPlayableInFull(delegate))
                        return true;
                }

                return false;
            }
        };
    }
}
