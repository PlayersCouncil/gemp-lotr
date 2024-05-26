package com.gempukku.lotro.cards.set18.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Condition • Support Area
 * Game Text: Tale. Maneuver: Exert a [DWARVEN] companion to make the fellowship's current site gain mountain until
 * the regroup phase.
 */
public class Card18_003 extends AbstractPermanent {
    public Card18_003() {
        super(Side.FREE_PEOPLE, 0, CardType.CONDITION, Culture.DWARVEN, "Thorin's Harp");
        addKeyword(Keyword.TALE);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && PlayConditions.canExert(self, game, Culture.DWARVEN, CardType.COMPANION)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.DWARVEN, CardType.COMPANION));
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new AddKeywordModifier(self, game.getGameState().getCurrentSite(), Keyword.MOUNTAIN), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
    }
}
