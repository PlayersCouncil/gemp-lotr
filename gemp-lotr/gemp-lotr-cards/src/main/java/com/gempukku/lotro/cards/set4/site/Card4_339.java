package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 0
 * Type: Site
 * Site: 3T
 * Game Text: Sanctuary. Fellowship: Exert a [ROHAN] Man to play a [ROHAN] mount from your draw deck (limit once
 * per turn).
 */
public class Card4_339 extends AbstractSite {
    public Card4_339() {
        super("Stables", SitesBlock.TWO_TOWERS, 3, 0, Direction.RIGHT);
        addKeyword(Keyword.SANCTUARY);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.FELLOWSHIP, self)
                && PlayConditions.canExert(self, game, Culture.ROHAN, Race.MAN)
        && PlayConditions.checkTurnLimit(game, self, 1)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new IncrementTurnLimitEffect(self, 1));
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.ROHAN, Race.MAN));
            action.appendEffect(
                            new ChooseAndPlayCardFromDeckEffect(playerId, Culture.ROHAN, PossessionClass.MOUNT));
            return Collections.singletonList(action);
        }
        return null;
    }
}
