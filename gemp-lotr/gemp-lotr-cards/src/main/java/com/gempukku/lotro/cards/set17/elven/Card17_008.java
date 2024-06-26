package com.gempukku.lotro.cards.set17.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.ReinforceTokenEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 3
 * Type: Event • Regroup
 * Game Text: Spot an [ELVEN] hunter and choose one: draw a card for each hunter you can spot or reinforce
 * an [ELVEN] token for each hunter you can spot.
 */
public class Card17_008 extends AbstractEvent {
    public Card17_008() {
        super(Side.FREE_PEOPLE, 3, Culture.ELVEN, "Hearth and Hall", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ELVEN, Keyword.HUNTER);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, final PhysicalCard self) {
        final int count = Filters.countActive(game, Keyword.HUNTER);
        final PlayEventAction action = new PlayEventAction(self);
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new UnrespondableEffect() {
                    @Override
                    public String getText(LotroGame game) {
                        return "Draw a card for each hunter you can spot";
                    }

                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        action.appendEffect(
                                new DrawCardsEffect(action, playerId, count));
                    }
                });
        possibleEffects.add(
                new UnrespondableEffect() {
                    @Override
                    public String getText(LotroGame game) {
                        return "Reinforce an ELVEN token for each hunter you can spot";
                    }

                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        for (int i = 0; i < count; i++)
                            action.appendEffect(
                                    new ReinforceTokenEffect(self, playerId, Token.ELVEN));
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
