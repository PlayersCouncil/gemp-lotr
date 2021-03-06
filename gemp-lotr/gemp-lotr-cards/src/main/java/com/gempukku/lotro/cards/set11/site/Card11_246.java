package com.gempukku.lotro.cards.set11.site;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractShadowsSite;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Twilight Cost: 2
 * Type: Site
 * Game Text: Marsh. Regroup: Exert your Gollum or your Smeagol to play the fellowship's next site.
 */
public class Card11_246 extends AbstractShadowsSite {
    public Card11_246() {
        super("Mere of Dead Faces", 2, Direction.LEFT);
        addKeyword(Keyword.MARSH);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canExert(self, game, Filters.gollumOrSmeagol, Filters.owner(playerId))) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gollumOrSmeagol, Filters.owner(playerId)));
            action.appendEffect(
                    new PlaySiteEffect(action, playerId, null, game.getGameState().getCurrentSiteNumber() + 1));
            return Collections.singletonList(action);
        }
        return null;
    }
}
