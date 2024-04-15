package com.gempukku.lotro.cards.set11.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Artifact • Staff
 * Vitality: +1
 * Game Text: Bearer must be a Wizard. If bearer is Gandalf, he gains muster. (At the start of the regroup phase, you
 * may discard a card from hand to draw a card.) Each time bearer wins a skirmish, choose a Shadow player who must wound
 * a minion.
 */
public class Card11_034 extends AbstractAttachableFPPossession {
    public Card11_034() {
        super(2, 0, 1, Culture.GANDALF, CardType.ARTIFACT, PossessionClass.STAFF, "Gandalf's Staff", "Ash-staff", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Race.WIZARD;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new KeywordModifier(self, Filters.and(Filters.hasAttached(self), Filters.gandalf), Keyword.MUSTER));
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, Filters.hasAttached(self))) {
            final RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new ChooseOpponentEffect(self.getOwner()) {
                        @Override
                        protected void opponentChosen(String opponentId) {
                            action.appendEffect(
                                    new ChooseAndWoundCharactersEffect(action, opponentId, 1, 1, CardType.MINION));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
