package com.gempukku.lotro.cards.set5.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.CharacterLostSkirmishResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 3
 * Type: Possession • Mount
 * Strength: +4
 * Game Text: Bearer must be a Southron. Bearer is fierce. Each time bearer overwhelms a character, you may add (5).
 */
public class Card5_078 extends AbstractAttachable {
    public Card5_078() {
        super(Side.SHADOW, CardType.POSSESSION, 3, Culture.RAIDER, PossessionClass.MOUNT, "War Mûmak");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Keyword.SOUTHRON;
    }

    @Override
    public int getStrength() {
        return 4;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.FIERCE));
        return modifiers;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.losesSkirmishInvolving(game, effectResult, Filters.or(CardType.COMPANION, CardType.ALLY), Filters.hasAttached(self))) {
            CharacterLostSkirmishResult lostSkirmishResult = (CharacterLostSkirmishResult) effectResult;
            if (lostSkirmishResult.getSkirmishType() == CharacterLostSkirmishResult.SkirmishType.OVERWHELM) {
                OptionalTriggerAction action = new OptionalTriggerAction(self);
                action.appendEffect(
                        new AddTwilightEffect(self, 5));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
