package com.gempukku.lotro.cards.set13.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractFollower;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.ExtraFilters;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Follower
 * Resistance: +1
 * Game Text: To play, spot Smeagol. Aid - Add a burden. Skirmish: If bearer is not assigned to a skirmish, discard this
 * from play to play an artifact or possession from your draw deck on bearer.
 */
public class Card13_046 extends AbstractFollower {
    public Card13_046() {
        super(Side.FREE_PEOPLE, 0, 0, 0, 1, Culture.GOLLUM, "Déagol", "Fateful Finder", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.smeagol);
    }

    @Override
    public boolean canPayAidCost(LotroGame game, PhysicalCard self) {
        return true;
    }

    @Override
    public void appendAidCosts(LotroGame game, CostToEffectAction action, PhysicalCard self) {
        action.appendCost(new AddBurdenEffect(self.getOwner(), self, 1));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSpot(game, Filters.hasAttached(self), Filters.notAssignedToSkirmish)
                && PlayConditions.canSelfDiscard(self, game)) {

            final PhysicalCard attachedTo = self.getAttachedTo();

            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK)) {
                action.appendEffect(
                        new ChooseArbitraryCardsEffect(playerId, "Choose card to play", game.getGameState().getDeck(playerId),
                                Filters.and(
                                        Filters.or(CardType.ARTIFACT, CardType.POSSESSION),
                                        ExtraFilters.attachableTo(game, attachedTo)), 1, 1) {
                            @Override
                            protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                if (selectedCards.size() > 0) {
                                    PhysicalCard selectedCard = selectedCards.iterator().next();
                                    game.getActionsEnvironment().addActionToStack(PlayUtils.getPlayCardAction(game, selectedCard, 0, attachedTo, false));
                                }
                            }
                        });
            }
            return Collections.singletonList(action);
        }
        return null;
    }
}
