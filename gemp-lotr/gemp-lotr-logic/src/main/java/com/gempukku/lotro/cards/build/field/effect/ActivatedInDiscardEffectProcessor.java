package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedInDiscardEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "noEffectUnless", "cost", "effect", "limitPerPhase", "limitPerTurn", "text", "specialAbility");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);
        // An ability with a cost stays on offer at its limit: the cost may be worth paying for its own sake
        // (Ketura on Q-0041).  With no cost it is withheld there, as before (Q-0034).
        final boolean offeredAtLimit = FieldUtils.getObjectArray(value.get("cost"), "cost").length > 0;
        // "specialAbility: false" marks text that lets the card be played from here and has no phase word in front
        // of it ("If you have initiative during the Shadow phase, you may play this minion from your discard
        // pile").  That is a way of playing the card and not a special ability: nothing that answers, bans or
        // counts special abilities sees it, and whatever bans playing the card does.
        final boolean specialAbility = FieldUtils.getBoolean(value.get("specialAbility"), "specialAbility", true);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            actionSource.setSkirmishAbility(phase == Phase.SKIRMISH);
            actionSource.setSpecialAbility(specialAbility);
            if (limitPerPhase > 0)
                actionSource.addLimitPerPhase(phase, limitPerPhase, offeredAtLimit);
            if (limitPerTurn > 0)
                actionSource.addLimitPerTurn(limitPerTurn, offeredAtLimit);
            actionSource.addPlayRequirement(
                    (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase));
            EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

            blueprint.appendInDiscardPhaseAction(actionSource);
        }
    }
}