package com.gempukku.lotro.cards.set7.sauron;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.StackCardFromPlayEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 3
 * Type: Minion • Orc
 * Strength: 9
 * Vitality: 2
 * Site: 5
 * Game Text: Besieger. Regroup: Discard 2 cards from hand to stack a [SAURON] Orc on a site you control (or discard
 * 1 card from hand if that Orc is a besieger).
 */
public class Card7_273 extends AbstractMinion {
    public Card7_273() {
        super(3, 9, 2, 5, Race.ORC, Culture.SAURON, "Gorgoroth Garrison");
        addKeyword(Keyword.BESIEGER);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)) {
            List<ActivateCardAction> actions = new LinkedList<>();
            if (PlayConditions.canDiscardFromHand(game, playerId, 1, Filters.any)) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Stack besieger SAURON Orc");
                action.appendCost(
                        new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Filters.any));
                action.appendEffect(
                        new ChooseActiveCardEffect(self, playerId, "Choose besieger SAURON Orc", Culture.SAURON, Race.ORC, Keyword.BESIEGER) {
                            @Override
                            protected void cardSelected(LotroGame game, final PhysicalCard minion) {
                                action.appendEffect(
                                        new ChooseActiveCardEffect(self, playerId, "Choose site you control", Filters.siteControlled(playerId)) {
                                            @Override
                                            protected void cardSelected(LotroGame game, PhysicalCard site) {
                                                action.insertEffect(
                                                        new StackCardFromPlayEffect(minion, site));
                                            }
                                        });
                            }
                        });
                actions.add(action);
            }
            if (PlayConditions.canDiscardFromHand(game, playerId, 2, Filters.any)) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Stack SAURON Orc");
                action.appendCost(
                        new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 2, Filters.any));
                action.appendEffect(
                        new ChooseActiveCardEffect(self, playerId, "Choose SAURON Orc", Culture.SAURON, Race.ORC) {
                            @Override
                            protected void cardSelected(LotroGame game, final PhysicalCard minion) {
                                action.appendEffect(
                                        new ChooseActiveCardEffect(self, playerId, "Choose site you control", Filters.siteControlled(playerId)) {
                                            @Override
                                            protected void cardSelected(LotroGame game, PhysicalCard site) {
                                                action.insertEffect(
                                                        new StackCardFromPlayEffect(minion, site));
                                            }
                                        });
                            }
                        });
                actions.add(action);
            }
            return actions;
        }
        return null;
    }
}
