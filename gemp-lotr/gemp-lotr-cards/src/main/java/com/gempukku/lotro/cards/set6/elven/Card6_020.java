package com.gempukku.lotro.cards.set6.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.effects.DiscardCardFromDeckEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Fellowship: Spot an Elf to reveal the top card of your draw deck. Heal up to 2 companions whose culture
 * matches the revealed card. You may discard the revealed card.
 */
public class Card6_020 extends AbstractEvent {
    public Card6_020() {
        super(Side.FREE_PEOPLE, 0, Culture.ELVEN, "Must Be a Dream", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.ELF);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new RevealTopCardsOfDrawDeckEffect(self, playerId, 1) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        for (final PhysicalCard card : revealedCards) {
                            Culture cardCulture = card.getBlueprint().getCulture();
                            action.appendEffect(
                                    new ChooseAndHealCharactersEffect(action, playerId, 0, 2, CardType.COMPANION, cardCulture));
                            action.appendEffect(
                                    new PlayoutDecisionEffect(playerId,
                                            new MultipleChoiceAwaitingDecision(1, "Do you wish to discard " + GameUtils.getFullName(card), new String[]{"Yes", "No"}) {
                                                @Override
                                                protected void validDecisionMade(int index, String result) {
                                                    if (result.equals("Yes"))
                                                        action.appendEffect(
                                                                new DiscardCardFromDeckEffect(card));
                                                }
                                            }));
                        }
                    }
                });
        return action;
    }
}
