package com.gempukku.lotro.cards.set7.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.ReturnCardsToHandEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 1
 * Type: Companion • Hobbit
 * Strength: 3
 * Vitality: 4
 * Resistance: 6
 * Signet: Aragorn
 * Game Text: Skirmish: If Pippin is not assigned to a skirmish, return him to your hand to wound a roaming minion
 * twice.
 */
public class Card7_324 extends AbstractCompanion {
    public Card7_324() {
        super(1, 3, 4, 6, Culture.SHIRE, Race.HOBBIT, Signet.ARAGORN, "Pippin", "Wearer of Black and Silver", true);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && Filters.notAssignedToSkirmish.accepts(game, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ReturnCardsToHandEffect(self, self));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, 2, CardType.MINION, Keyword.ROAMING));
            return Collections.singletonList(action);
        }
        return null;
    }
}
