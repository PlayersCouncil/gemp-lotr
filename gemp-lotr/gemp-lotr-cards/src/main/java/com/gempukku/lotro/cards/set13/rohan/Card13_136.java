package com.gempukku.lotro.cards.set13.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.ReinforceTokenEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExhaustCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 2
 * Type: Possession • Mount
 * Strength: +1
 * Resistance: +1
 * Game Text: Bearer must be a [ROHAN] Man. When you play Snowmane, you may reinforce a [ROHAN] token.
 * Skirmish: If bearer is Theoden, you may remove 2 [ROHAN] tokens to exhaust a minion he is skirmishing.
 */
public class Card13_136 extends AbstractAttachableFPPossession {
    public Card13_136() {
        super(2, 1, 0, Culture.ROHAN, PossessionClass.MOUNT, "Snowmane", "Noble Mearas", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ROHAN, Race.MAN);
    }

    @Override
    public int getResistance() {
        return 1;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ReinforceTokenEffect(self, playerId, Token.ROHAN));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && self.getAttachedTo().getBlueprint().getTitle().equals(Names.theoden)
                && PlayConditions.canRemoveTokensFromAnything(game, Token.ROHAN, 2)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.ROHAN, 1, Filters.any));
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.ROHAN, 1, Filters.any));
            action.appendEffect(
                    new ChooseAndExhaustCharactersEffect(action, playerId, 1, 1, CardType.MINION, Filters.inSkirmishAgainst(Filters.hasAttached(self))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
