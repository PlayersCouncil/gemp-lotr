package com.gempukku.lotro.cards.set15.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Bearer must be Gollum. Skirmish: If Gollum is not assigned to a skirmish, discard this condition to have
 * him replace a minion skirmishing a companion.
 */
public class Card15_041 extends AbstractAttachable {
    public Card15_041() {
        super(Side.SHADOW, CardType.CONDITION, 1, Culture.GOLLUM, null, "Controlled By The Ring");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.gollum;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 0)
                && PlayConditions.canSpot(game, Filters.hasAttached(self), Filters.notAssignedToSkirmish)
                && PlayConditions.canSelfDiscard(self, game)) {

            final PhysicalCard gollum = self.getAttachedTo();

            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose minion to replace", CardType.MINION, Filters.inSkirmishAgainst(CardType.COMPANION)) {
                        @Override
                        protected void cardSelected(LotroGame game, final PhysicalCard minion) {
                            action.appendEffect(
                                    new UnrespondableEffect() {
                                        @Override
                                        protected void doPlayEffect(LotroGame game) {
                                            game.getGameState().replaceInSkirmishMinion(gollum, minion);
                                        }
                                    });
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
