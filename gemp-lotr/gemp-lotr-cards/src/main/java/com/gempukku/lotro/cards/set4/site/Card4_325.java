package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Type: Site
 * Site: 1T
 * Game Text: Fellowship: Exert 2 unbound companions to play Legolas from your draw deck.
 */
public class Card4_325 extends AbstractSite {
    public Card4_325() {
        super("Eastemnet Gullies", SitesBlock.TWO_TOWERS, 1, 0, Direction.LEFT);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.FELLOWSHIP, self)
                && PlayConditions.canExert(self, game, 1, 2, Filters.unboundCompanion)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Filters.unboundCompanion));
            action.appendEffect(
                    new ChooseAndPlayCardFromDeckEffect(playerId, Filters.legolas));
            return Collections.singletonList(action);
        }
        return null;
    }
}
