package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.PayTwilightCostEffect;
import com.gempukku.lotro.logic.effects.TransferPermanentEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collection;

public class TransferPermanentAction extends ActivateCardAction {
    private final PhysicalCard _transferredCard;

    public TransferPermanentAction(final PhysicalCard card, String performingPlayer, Filter filter) {
        super(card, performingPlayer);
        _transferredCard = card;
        setText("Transfer " + GameUtils.getFullName(_transferredCard));

        appendCost(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.TRANSFERS_FOR_FREE))
                            insertCost(new PayTwilightCostEffect(card));
                    }
                });
        appendEffect(
                new ChooseActiveCardsEffect(null, card.getOwner(), "Choose target to attach to", 1, 1, filter) {
                    @Override
                    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> target) {
                        if (!target.isEmpty()) {
                            appendEffect(new TransferPermanentEffect(card, target.iterator().next()));
                        }
                    }
                });
    }

    @Override
    public Type getType() {
        return Type.TRANSFER;
    }
}
