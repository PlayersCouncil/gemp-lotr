package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SarumanFirstSentenceActive implements RequirementProducer {
    private static final String TYPE = "sarumanfirstsentenceactive";

    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return (actionContext) -> {
            final LotroGame game = actionContext.getGame();
            return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.SARUMAN_FIRST_SENTENCE_INACTIVE);
        };
    }

    private static final Map<LotroCardBlueprint, Boolean> READERS = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Whether a card's definition reads the switch "the first sentence of Saruman's game text does not apply"
     * (ModifierFlag.SARUMAN_FIRST_SENTENCE_INACTIVE): whether this requirement stands anywhere in it.  The switch is
     * read by definitions alone, so with no card in play whose definition reads it, throwing it does nothing
     * (AddModifier, for the rule of Q-0224; Saruman's Staff 4_174 on the two Sarumans of Legacy Ruling 6).
     */
    public static boolean isReadBy(LotroCardBlueprint blueprint) {
        if (blueprint == null || blueprint.getJsonDefinition() == null)
            return false;
        return READERS.computeIfAbsent(blueprint, b -> names(b.getJsonDefinition()));
    }

    private static boolean names(Object definition) {
        if (definition instanceof JSONObject object) {
            if (TYPE.equalsIgnoreCase(String.valueOf(object.get("type"))))
                return true;
            for (Object value : object.values()) {
                if (names(value))
                    return true;
            }
        } else if (definition instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (names(element))
                    return true;
            }
        } else if (definition instanceof Object[] array) {
            for (Object element : array) {
                if (names(element))
                    return true;
            }
        }
        return false;
    }
}
