package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * True if the account name of the resolved player is one of the listed names.  Comparison ignores
 * case.  The names themselves live in the card definition, not in the engine.
 */
public class PlayerIs implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "names");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final String[] names = FieldUtils.getStringArray(object.get("names"), "names");
        if (names.length == 0)
            throw new InvalidCardDefinitionException("PlayerIs requires at least one entry in 'names'");

        final Set<String> lowercaseNames = Collections.unmodifiableSet(
                Arrays.stream(names).map(name -> name.toLowerCase(Locale.ROOT)).collect(HashSet::new, HashSet::add, HashSet::addAll));

        return PlayerScope.requirement(player,
                (actionContext, playerId) -> lowercaseNames.contains(playerId.toLowerCase(Locale.ROOT)));
    }
}
