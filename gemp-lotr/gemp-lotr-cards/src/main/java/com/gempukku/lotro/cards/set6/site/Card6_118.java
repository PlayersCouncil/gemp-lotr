package com.gempukku.lotro.cards.set6.site;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Set: Ents of Fangorn
 * Twilight Cost: 3
 * Type: Site
 * Site: 6T
 * Game Text: Sanctuary. Fellowship: Spot 3 companions with the Theoden signet and discard your hand to draw 4 cards.
 */
public class Card6_118 extends AbstractSite {
    public Card6_118() {
        super("Hornburg Hall", SitesBlock.TWO_TOWERS, 6, 3, Direction.LEFT);
        addKeyword(Keyword.SANCTUARY);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.FELLOWSHIP, self)
                && PlayConditions.canSpot(game, 3, CardType.COMPANION, Signet.THEODEN)) {
            ActivateCardAction action = new ActivateCardAction(self);
            Set<PhysicalCard> hand = new HashSet<>(game.getGameState().getHand(playerId));
            action.appendCost(
                    new DiscardCardsFromHandEffect(self, playerId, hand, false));
            action.appendEffect(
                    new DrawCardsEffect(action, playerId, 4));
            return Collections.singletonList(action);
        }
        return null;
    }
}
