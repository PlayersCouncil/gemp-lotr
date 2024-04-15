package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.PutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 0
 * Type: Site
 * Site: 3T
 * Game Text: Sanctuary. Fellowship: Place your hand beneath your draw deck and draw 4 cards.
 */
public class Card4_337 extends AbstractSite {
    public Card4_337() {
        super("Barrows of Edoras", SitesBlock.TWO_TOWERS, 3, 0, Direction.RIGHT);
        addKeyword(Keyword.SANCTUARY);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.FELLOWSHIP, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new PutCardsFromHandBeneathDrawDeckEffect(action, playerId, false, Filters.any));
            action.appendEffect(
                    new DrawCardsEffect(action, playerId, 4));
            return Collections.singletonList(action);
        }
        return null;
    }
}
