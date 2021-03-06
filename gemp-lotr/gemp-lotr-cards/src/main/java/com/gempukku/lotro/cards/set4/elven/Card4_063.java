package com.gempukku.lotro.cards.set4.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPreventCardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Possession • Brooch
 * Game Text: To play, spot an Elf. Bearer must be a companion.
 * Response: If another possession borne by bearer is about to be discarded by a Shadow card, discard this possession
 * instead.
 */
public class Card4_063 extends AbstractAttachableFPPossession {
    public Card4_063() {
        super(1, 0, 0, Culture.ELVEN, PossessionClass.BROOCH, "Elven Brooch");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.ELF);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return CardType.COMPANION;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingDiscardedBy(effect, game, Side.SHADOW, CardType.POSSESSION, Filters.not(self), Filters.attachedTo(self.getAttachedTo()))) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndPreventCardEffect(self, (PreventableCardEffect) effect, playerId, "Choose possession",
                            CardType.POSSESSION, Filters.not(self), Filters.attachedTo(self.getAttachedTo())));
            return Collections.singletonList(action);
        }
        return null;
    }
}
