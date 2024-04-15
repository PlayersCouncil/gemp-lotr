package com.gempukku.lotro.cards.set18.men;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 4
 * Vitality: 3
 * Site: 4
 * Game Text: Regroup: Exert Grima to add (2). Regroup: Remove a [MEN] token to add (2). Regroup: Remove (4) to exert
 * an unbound companion.
 */
public class Card18_067 extends AbstractMinion {
    public Card18_067() {
        super(2, 4, 3, 4, Race.MAN, Culture.MEN, "Gríma", "Witless Worm", true);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)) {
            List<ActivateCardAction> actions = new LinkedList<>();
            if (PlayConditions.canSelfExert(self, game)) {
                ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Exert Grima to add (2)");
                action.appendCost(
                        new SelfExertEffect(action, self));
                action.appendEffect(
                        new AddTwilightEffect(self, 2));
                actions.add(action);
            }
            if (PlayConditions.canRemoveTokens(game, Token.MEN, 1, Filters.any)) {
                ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Remove a MEN token to add (2)");
                action.appendCost(
                        new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.MEN, 1, Filters.any));
                action.appendEffect(
                        new AddTwilightEffect(self, 2));
                actions.add(action);
            }
            if (game.getGameState().getTwilightPool() >= 4) {
                ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Remove (4) to exert an unbound companion");
                action.appendCost(
                        new RemoveTwilightEffect(4));
                action.appendEffect(
                        new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.unboundCompanion));
                actions.add(action);
            }
            return actions;
        }
        return null;
    }
}
