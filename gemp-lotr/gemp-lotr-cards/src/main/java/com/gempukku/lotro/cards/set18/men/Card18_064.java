package com.gempukku.lotro.cards.set18.men;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.CountCultureTokensEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.LimitEvaluator;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 2
 * Type: Possession • Hand Weapon
 * Game Text: Bearer must be a [MEN] minion. Bearer is strength +1 for each [MEN] token you can spot (limit +5).
 */
public class Card18_064 extends AbstractAttachable {
    public Card18_064() {
        super(Side.SHADOW, CardType.POSSESSION, 2, Culture.MEN, PossessionClass.HAND_WEAPON, "Corsair Scimitar");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.MEN, CardType.MINION);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new StrengthModifier(self, Filters.hasAttached(self), null,
new LimitEvaluator(new CountCultureTokensEvaluator(Token.MEN, Filters.any), new ConstantEvaluator(5))));
}
}
