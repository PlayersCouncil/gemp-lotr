package com.gempukku.lotro.cards.set18.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 3
 * Type: Event • Maneuver
 * Game Text: If the fellowship is in region 1 or region 2, exert 2 Ents to make an Ent defender +1 until the regroup
 * phase. If the fellowship is in region 3, spot a [GANDALF] companion to draw a card.
 */
public class Card18_020 extends AbstractEvent {
    public Card18_020() {
        super(Side.FREE_PEOPLE, 3, Culture.GANDALF, "Ents Marching", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return ((GameUtils.getRegion(game) == 3 && PlayConditions.canSpot(game, Culture.GANDALF, CardType.COMPANION))
                        || ((GameUtils.getRegion(game) == 1 || GameUtils.getRegion(game) == 2) && PlayConditions.canExert(self, game, 1, 2, Race.ENT)));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        if (GameUtils.getRegion(game) == 3)
            action.appendEffect(
                    new DrawCardsEffect(action, playerId, 1));
        else {
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 2, 2, Race.ENT));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose an Ent", Race.ENT) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new AddKeywordModifier(self, card, Keyword.DEFENDER, 1), Phase.REGROUP));
                        }
                    });
        }
        return action;
    }
}
