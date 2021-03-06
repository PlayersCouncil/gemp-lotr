package com.gempukku.lotro.cards.set13.wraith;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 6
 * Type: Minion • Nazgul
 * Strength: 11
 * Vitality: 4
 * Site: 3
 * Game Text: Fierce. When you play Ulaire Enquea, if you can spot 2 other Nazgul, you may exert 2 unbound companions
 * (or exert each companion if you can spot 6 companions).
 */
public class Card13_182 extends AbstractMinion {
    public Card13_182() {
        super(6, 11, 4, 3, Race.NAZGUL, Culture.WRAITH, Names.enquea, "Sixth of the Nine Riders", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)
                && PlayConditions.canSpot(game, 2, Filters.not(self), Race.NAZGUL)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            if (PlayConditions.canSpot(game, 6, CardType.COMPANION)) {
                action.appendEffect(
                        new ExertCharactersEffect(action, self, CardType.COMPANION));
            } else {
                action.appendEffect(
                        new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Filters.unboundCompanion));
            }
            return Collections.singletonList(action);
        }
        return null;
    }
}
