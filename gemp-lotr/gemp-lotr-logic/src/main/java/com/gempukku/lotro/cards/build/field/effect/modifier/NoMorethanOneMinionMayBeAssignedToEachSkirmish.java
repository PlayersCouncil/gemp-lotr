package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoMorethanOneMinionMayBeAssignedToEachSkirmish implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> {

            return new AbstractModifier(actionContext.getSource(), "No more than one minion may be assigned to each skirmish", null,
                    RequirementCondition.createCondition(requirements, actionContext), ModifierEffect.ASSIGNMENT_MODIFIER) {
                @Override
                public boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments) {
                    for (Map.Entry<PhysicalCard, Set<PhysicalCard>> minionsAssignedToCharacter : assignments.entrySet()) {
                        PhysicalCard fp = minionsAssignedToCharacter.getKey();
                        Set<PhysicalCard> minions = minionsAssignedToCharacter.getValue();
                        List<Assignment> alreadyAssigned = game.getGameState().getAssignments();
                        if (countMinionsCurrentlyAssignedToFPChar(alreadyAssigned, fp) + minions.size() > 1)
                            return false;
                    }
                    return true;
                }

                private int countMinionsCurrentlyAssignedToFPChar(List<Assignment> assignments, PhysicalCard fp) {
                    for (Assignment assignment : assignments) {
                        if (assignment.getFellowshipCharacter() == fp)
                            return assignment.getShadowCharacters().size();
                    }
                    return 0;
                }
            };
        };
    }
}
