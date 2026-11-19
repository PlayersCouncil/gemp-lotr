package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;

/**
 * {@code uniquePerPlayer: true}: only the owner's cards of the title count toward this card's uniqueness when it
 * is played (CR 4.2 "unique"; F-0655).  See {@link com.gempukku.lotro.game.LotroCardBlueprint#isUniquePerPlayer()}.
 */
public class UniquePerPlayerFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        blueprint.setUniquePerPlayer(FieldUtils.getBoolean(value, key));
    }
}
