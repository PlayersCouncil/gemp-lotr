package com.gempukku.lotro.cards.set18.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Event • Skirmish
 * Game Text: Exert a knight to make him or her strength +3.
 */
public class Card18_051 extends AbstractEvent {
    public Card18_051() {
        super(Side.FREE_PEOPLE, 0, Culture.GONDOR, "For Gondor!", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Keyword.KNIGHT);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Keyword.KNIGHT) {
                    @Override
                    protected void forEachCardExertedCallback(PhysicalCard character) {
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new StrengthModifier(self, character, 3)));
                    }
                });
        return action;
    }
}
