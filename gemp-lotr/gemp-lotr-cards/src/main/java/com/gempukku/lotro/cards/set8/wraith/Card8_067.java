package com.gempukku.lotro.cards.set8.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAssignCharacterToMinionEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 4
 * Type: Event • Assignment
 * Game Text: Exert an enduring [WRAITH] minion twice to assign that minion to an unbound companion.
 */
public class Card8_067 extends AbstractEvent {
    public Card8_067() {
        super(Side.SHADOW, 4, Culture.WRAITH, "Between Nazgûl and Prey", Phase.ASSIGNMENT);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, 2, Culture.WRAITH, CardType.MINION, Keyword.ENDURING);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);

        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, 2, Culture.WRAITH, CardType.MINION, Keyword.ENDURING) {
                    @Override
                    protected void forEachCardExertedCallback(PhysicalCard character) {
                        action.appendEffect(
                                new ChooseAndAssignCharacterToMinionEffect(action, playerId, character, Filters.unboundCompanion));
                    }
                });

        return action;
    }
}
