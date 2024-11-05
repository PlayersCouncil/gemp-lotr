package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class IsAdditionalCardTypeModifier extends AbstractModifier {
    private final CardType _cardType;

    public IsAdditionalCardTypeModifier(PhysicalCard source, Filterable affectFilter, Condition condition, CardType cardType) {
        super(source, "Has additional card type - " + cardType.toString(), affectFilter, condition, ModifierEffect.ADDITIONAL_CARD_TYPE);
        _cardType = cardType;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(LotroGame game, PhysicalCard physicalCard, CardType cardType) {
        return cardType == _cardType;
    }
}
