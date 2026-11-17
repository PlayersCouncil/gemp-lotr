package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
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
import com.gempukku.lotro.logic.timing.FailedEffect;
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

                // The branch taken is carried out in a context that says whether it was open because it can be
                // carried out in full: what is then picked within it is an extension of the choice, and is held
                // to what can be done in full as well (Ketura on Q-0209).  Where no branch can be and they are
                // offered all the same, it says not, whatever a choice this one is a branch of had said.
                final boolean doableInFull = !playableEffectAppenders.isEmpty();
                final ActionContext branchContext = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect()) {
                    @Override
                    public boolean isBranchChosenAsDoableInFull() {
                        return doableInFull;
                    }
                };

                // Exactly one branch can be carried out in full: that is the one the player is capable of choosing
                // (CR 4.2 "effect"; Ketura on F-0310), so it is taken without a question, and it is the one
                // remembered as chosen, in the words the question would have shown.
                if (playableEffectAppenders.size() == 1) {
                    SubAction subAction = new SubAction(action);
                    playableEffectAppenders.getFirst().appendEffect(cost, subAction, branchContext);
                    actionContext.setValueToMemory(memorize, effectTexts.getFirst());
                    return new StackActionEffect(subAction);
                }

                if (playableEffectAppenders.isEmpty()) {
                    actionContext.setValueToMemory(memorize, "");

                    // Costs that are a choice should not let the player pick freely, else it effectively results in there
                    // being no cost at all.
                    if(cost)
                        return null;

                    // A choice whose every branch is certain to do nothing is not put to the player (Ketura on
                    // F-0116: "the prompt should be smarter"; F-0293).  It comes to nothing, as whichever branch
                    // was picked would have.  Each branch says for itself: a single effect by its forecast, one
                    // that holds others (an "if", a cost and what it buys, several effects, a play) from what
                    // it holds (Q-0220: D).  A branch that cannot tell keeps the question.
                    if (EffectForecast.areCertainToDoNothingIfChosen(delegate, possibleEffectAppenders))
                        return new FailedEffect();

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
                                        playableEffectAppenders.get(index).appendEffect(cost, subAction, branchContext);
                                        actionContext.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(subAction);
            }

            // Whichever option is taken: nothing only if no option would do anything.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());
                return EffectForecast.together(delegate, possibleEffectAppenders);
            }

            // As a branch of another choice: it comes to nothing only if every option of its own would.
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                ActionContext delegate = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());
                return possibleEffectAppenders.length > 0
                        && EffectForecast.areCertainToDoNothingIfChosen(delegate, possibleEffectAppenders);
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                // When every branch insists on being checked up front (e.g. all of them play a card from a zone),
                // the choice as a whole must be too; otherwise an action whose only branches are all impossible
                // (such as Fell Voices Call at The Great River) is still offered, and the "no valid choices" fallback
                // below then lets the player pick a branch that must not happen (#1080).
                for (EffectAppender possibleEffectAppender : possibleEffectAppenders) {
                    if (!possibleEffectAppender.isPlayabilityCheckedForEffect())
                        return false;
                }
                return possibleEffectAppenders.length > 0;
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
