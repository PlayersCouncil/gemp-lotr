package com.gempukku.lotro.cards.set5.isengard;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CompletePhysicalCardVisitor;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ReturnCardsToHandEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 4
 * Vitality: 3
 * Site: 3
 * Game Text: Maneuver: Spot 4 Free Peoples cultures and exert Grima to return an unbound companion, and all cards
 * on him or her, to their owners’ hands.
 */
public class Card5_051 extends AbstractMinion {
    public Card5_051() {
        super(2, 4, 3, 3, Race.MAN, Culture.ISENGARD, "Gríma", "Chief Counselor", true);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.MANEUVER, self, 0)
                && PlayConditions.canSelfExert(self, game)) {
            CountCulturesVisitor visitor = new CountCulturesVisitor();
            game.getGameState().iterateActiveCards(visitor);
            if (visitor.getCultureCount() >= 4) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.appendCost(
                        new SelfExertEffect(action, self));
                action.appendEffect(
                        new ChooseActiveCardEffect(self, playerId, "Choose companion", Filters.unboundCompanion) {
                            @Override
                            protected void cardSelected(LotroGame game, PhysicalCard card) {
                                Set<PhysicalCard> cardsToReturn = new HashSet<>();

                                action.insertEffect(
                                        new ReturnCardsToHandEffect(self, Filters.or(Filters.sameCard(card), Filters.attachedTo(Filters.sameCard(card)))));
                            }
                        });
                return Collections.singletonList(action);
            }
        }
        return null;
    }

    private static class CountCulturesVisitor extends CompletePhysicalCardVisitor {
        private final Set<Culture> _cultures = new HashSet<>();

        @Override
        protected void doVisitPhysicalCard(PhysicalCard physicalCard) {
            if (physicalCard.getBlueprint().getSide() == Side.FREE_PEOPLE)
                _cultures.add(physicalCard.getBlueprint().getCulture());
        }

        public int getCultureCount() {
            return _cultures.size();
        }
    }
}
