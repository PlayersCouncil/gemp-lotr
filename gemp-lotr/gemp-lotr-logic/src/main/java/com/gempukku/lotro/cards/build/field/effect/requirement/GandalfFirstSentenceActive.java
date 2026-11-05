package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

/**
 * Whether "the first sentence of Gandalf's game text" applies: false while the switch
 * ModifierFlag.GANDALF_FIRST_SENTENCE_INACTIVE (the modifier gandalfFirstSentenceInactive) is in force.  Read by the
 * Hobbit Draft Game Gandalfs 30_25 to 30_28 to gate their first sentence, and thrown by Dark Sorcery 33_61.  The
 * Gandalfs read Saruman's switch until engine pass 79, so The White Council 101_18 and Saruman's Staff 4_174 turned
 * them off and Dark Sorcery reached Saruman's cards; Ketura (F-0037): "Those hobbit cards should have made their own
 * flag".  It is built as SarumanFirstSentenceActive is, and is left to the Hobbit cards.
 */
public class GandalfFirstSentenceActive implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return (actionContext) -> {
            final LotroGame game = actionContext.getGame();
            return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.GANDALF_FIRST_SENTENCE_INACTIVE);
        };
    }
}
