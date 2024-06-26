package com.gempukku.lotro.cards.set18.uruk_hai;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.effects.TakeControlOfASiteEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Uruk-Hai
 * Twilight Cost: 0
 * Type: Condition
 * Strength: +1
 * Game Text: Bearer must be an [URUK-HAI] minion. Each time bearer wins a skirmish, you may choose one:
 * discard a follower from play; heal an [URUK-HAI] minion; or control a site.
 */
public class Card18_116 extends AbstractAttachable {
    public Card18_116() {
        super(Side.SHADOW, CardType.CONDITION, 0, Culture.URUK_HAI, null, "Fury of the Evil Army", null, true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.URUK_HAI, CardType.MINION);
    }

    @Override
    public int getStrength() {
        return 1;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, Filters.hasAttached(self))) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            List<Effect> possibleEffects = new LinkedList<>();
            possibleEffects.add(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.FOLLOWER) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard a follower from play";
                        }
                    });
            possibleEffects.add(
                    new ChooseAndHealCharactersEffect(action, playerId, 1, 1, Culture.URUK_HAI, CardType.MINION) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Heal an URUK_HAI minion";
                        }
                    });
            possibleEffects.add(
                    new TakeControlOfASiteEffect(self, playerId));
            action.appendEffect(
                    new ChoiceEffect(action, playerId, possibleEffects));
            return Collections.singletonList(action);
        }
        return null;
    }
}
