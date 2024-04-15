package com.gempukku.lotro.cards.set17.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Rise of Saruman
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Event • Maneuver
 * Game Text: Spot 2 Elves (or an Elf and an [ELVEN] follower) to discard a condition (or discard two conditions if you
 * can spot 4 or more Shadow conditions).
 */
public class Card17_009 extends AbstractEvent {
    public Card17_009() {
        super(Side.FREE_PEOPLE, 2, Culture.ELVEN, "Lothlórien Guides", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return (
                PlayConditions.canSpot(game, 2, Race.ELF)
                        || (PlayConditions.canSpot(game, Race.ELF) && PlayConditions.canSpot(game, CardType.FOLLOWER, Culture.ELVEN)));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        int count = PlayConditions.canSpot(game, 4, Side.SHADOW, CardType.CONDITION) ? 2 : 1;
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndDiscardCardsFromPlayEffect(action, playerId, count, count, CardType.CONDITION));
        return action;
    }
}
