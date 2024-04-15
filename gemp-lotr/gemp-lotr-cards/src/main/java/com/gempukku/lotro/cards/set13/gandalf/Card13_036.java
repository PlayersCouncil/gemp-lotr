package com.gempukku.lotro.cards.set13.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.ExtraPlayCost;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.RevealHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.modifiers.cost.DiscardFromPlayExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 0
 * Type: Artifact • Palantir
 * Resistance: -2
 * Game Text: Bearer must be Gandalf. To play this, if you can spot The Palantir of Orthanc, discard it. At the start of
 * the fellowship phase, you may add a burden to reveal a Shadow player's hand. He or she must choose a revealed minion
 * and discard it from hand.
 */
public class Card13_036 extends AbstractAttachableFPPossession {
    public Card13_036() {
        super(0, 0, 0, Culture.GANDALF, CardType.ARTIFACT, PossessionClass.PALANTIR, "The Palantír of Orthanc", "Recovered Seeing Stone", true);
    }

    @Override
    public int getResistance() {
        return -2;
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.gandalf;
    }

    @Override
    public boolean skipUniquenessCheck() {
        return true;
    }

    @Override
    public List<? extends ExtraPlayCost> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        if (PlayConditions.canSpot(game, Filters.name(getTitle()))) {
            return Collections.singletonList(
                    new DiscardFromPlayExtraPlayCostModifier(self, self, 1, null, Filters.name(getTitle())));
        }
        return super.getExtraCostToPlay(game, self);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(final String playerId, LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.FELLOWSHIP)) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new AddBurdenEffect(self.getOwner(), self, 1));
            action.appendEffect(
                    new ChooseOpponentEffect(playerId) {
                        @Override
                        protected void opponentChosen(final String opponentId) {
                            action.appendEffect(
                                    new RevealHandEffect(self, playerId, opponentId) {
                                        @Override
                                        protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {
                                            action.appendEffect(
                                                    new ChooseAndDiscardCardsFromHandEffect(action, opponentId, true, 1, CardType.MINION));
                                        }
                                    });
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
