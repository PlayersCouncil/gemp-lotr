package com.gempukku.lotro.cards.set17.uruk_hai;

import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.AddTokenEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 2
 * Type: Condition • Support Area
 * Game Text: Each time the Free Peoples player plays a possession or artifact, place an [URUK-HAI] token here.
 * Each time an [URUK-HAI] minion wins a skirmish, you may remove an [URUK-HAI] token to exert a companion.
 */
public class Card17_113 extends AbstractPermanent {
    public Card17_113() {
        super(Side.SHADOW, 2, CardType.CONDITION, Culture.URUK_HAI, "Deathly Roar");
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, Filters.owner(game.getGameState().getCurrentPlayerId()), Filters.or(CardType.POSSESSION, CardType.ARTIFACT))) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTokenEffect(self, self, Token.URUK_HAI));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, CardType.MINION, Culture.URUK_HAI)
                && PlayConditions.canRemoveTokens(game, Token.URUK_HAI, 1, CardType.CONDITION)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.URUK_HAI, 1, CardType.CONDITION));
            action.appendEffect(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, CardType.COMPANION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
