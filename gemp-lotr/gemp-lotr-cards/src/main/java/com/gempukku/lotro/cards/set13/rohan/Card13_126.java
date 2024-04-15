package com.gempukku.lotro.cards.set13.rohan;

import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ReinforceTokenEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 2
 * Type: Possession • Mount
 * Strength: +1
 * Game Text: Bearer must be a [ROHAN] Man. When you play Firefoot, you may reinforce a [ROHAN] token. At the start of
 * the maneuver phase, if bearer is Eomer, you may remove 2 [ROHAN] tokens to make him defender +1 and strength +1
 * until the regroup phase.
 */
public class Card13_126 extends AbstractAttachableFPPossession {
    public Card13_126() {
        super(2, 1, 0, Culture.ROHAN, PossessionClass.MOUNT, "Firefoot", "Mearas of The Mark", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ROHAN, Race.MAN);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ReinforceTokenEffect(self, playerId, Token.ROHAN));
            return Collections.singletonList(action);
        }
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.MANEUVER)
                && self.getAttachedTo().getBlueprint().getTitle().equals(Names.eomer)
                && PlayConditions.canRemoveTokensFromAnything(game, Token.ROHAN, 2)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.ROHAN, 1, Filters.any));
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.ROHAN, 1, Filters.any));
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new KeywordModifier(self, self.getAttachedTo(), Keyword.DEFENDER, 1), Phase.REGROUP));
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new StrengthModifier(self, self.getAttachedTo(), 1), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
    }
}
