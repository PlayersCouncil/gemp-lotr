package com.gempukku.lotro.cards.set15.gondor;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Game Text: Ranger. Hunter 1. (While skirmishing a non-hunter character, this character is strength +1.)
 * At the start of each fellowship phase, you may add (1) to heal a ranger.
 */
public class Card15_057 extends AbstractCompanion {
    public Card15_057() {
        super(3, 6, 3, 6, Culture.GONDOR, Race.MAN, null, "Damrod", "Dúnadan of Gondor", true);
        addKeyword(Keyword.RANGER);
        addKeyword(Keyword.HUNTER, 1);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.FELLOWSHIP)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new AddTwilightEffect(self, 1));
            action.appendEffect(
                    new ChooseAndHealCharactersEffect(action, playerId, Keyword.RANGER));
            return Collections.singletonList(action);
        }
        return null;
    }
}
