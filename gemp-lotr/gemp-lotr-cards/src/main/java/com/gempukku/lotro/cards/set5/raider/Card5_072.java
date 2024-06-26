package com.gempukku.lotro.cards.set5.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CompletePhysicalCardVisitor;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 5
 * Type: Minion • Man
 * Strength: 11
 * Vitality: 2
 * Site: 4
 * Game Text: Southron. Ambush (2). Maneuver: Exert this minion and spot 4 free peoples cultures to exert
 * every companion.
 */
public class Card5_072 extends AbstractMinion {
    public Card5_072() {
        super(5, 11, 2, 4, Race.MAN, Culture.RAIDER, "Desert Stalker");
        addKeyword(Keyword.SOUTHRON);
        addKeyword(Keyword.AMBUSH, 2);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.MANEUVER, self, 0)
                && PlayConditions.canSelfExert(self, game)) {
            CountCulturesVisitor visitor = new CountCulturesVisitor();
            game.getGameState().iterateActiveCards(visitor);
            if (visitor.getCultureCount() >= 4) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.appendCost(
                        new SelfExertEffect(action, self));
                action.appendEffect(
                        new ExertCharactersEffect(action, self, CardType.COMPANION));
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
