package com.gempukku.lotro.game;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * What taking a game back would undo, as LotroGameMediator.assessRevert tells it to the player asking: nothing of
 * anybody else's (green), another player's choices or a shuffle (yellow), or something the asking player has seen
 * since (red).  The games are ordinary ones played from the bids.
 */
public class RevertTierTest {
    private static final Set<CardType> LAID_DOWN = Set.of(CardType.COMPANION, CardType.POSSESSION, CardType.ALLY, CardType.CONDITION, CardType.ARTIFACT);

    private static RevertAssessment.Tier tier(MediatorSitting sitting, String player, int there) {
        return sitting.mediator.assessRevert(player, there).tier();
    }

    private static boolean asked(MediatorSitting sitting, String player, String textStart) {
        AwaitingDecision decision = sitting.mediator.awaitingDecision(player);
        return decision != null && decision.getText().startsWith(textStart);
    }

    /** The bids and the choice of who goes first, answered the plainest way, up to the starting fellowships. */
    private static String toStartingFellowships(MediatorSitting sitting) {
        sitting.mediator.startGame();
        for (int i = 0; i < 20; i++) {
            for (String player : MediatorSitting.PLAYERS)
                if (asked(sitting, player, "Starting fellowship"))
                    return player;
            String player = sitting.asked();
            sitting.answer(player, "0");
        }
        throw new AssertionError("The game never came to its starting fellowships");
    }

    /** Has the player choosing a starting fellowship take the first card on offer. */
    private static void pick(MediatorSitting sitting, String player) {
        sitting.answer(player, sitting.parameter(player, "cardId")[0]);
    }

    /** Both starting fellowships finished without another card, up to the first question about a mulligan. */
    private static String toMulligans(MediatorSitting sitting) {
        for (int i = 0; i < 20; i++) {
            for (String player : MediatorSitting.PLAYERS)
                if (asked(sitting, player, "Do you wish to mulligan"))
                    return player;
            sitting.answer(sitting.asked(), "");
        }
        throw new AssertionError("The game never came to its mulligans");
    }

    @Test
    public void AStartingFellowshipPickIsTheChoosersOwnUntilTheOtherPlayerHasShownACard() {
        MediatorSitting sitting = new MediatorSitting();
        String first = toStartingFellowships(sitting);
        String second = sitting.other(first);
        int firstPick = sitting.mediator.journalSize();
        pick(sitting, first);

        assertEquals("Nothing is learned by choosing from one's own deck", RevertAssessment.Tier.GREEN, tier(sitting, first, firstPick));

        while (!asked(sitting, second, "Starting fellowship"))
            sitting.answer(first, "");
        assertEquals("...nor by saying that is all", RevertAssessment.Tier.GREEN, tier(sitting, first, firstPick));

        int secondPick = sitting.mediator.journalSize();
        pick(sitting, second);
        if (asked(sitting, second, "Starting fellowship"))
            assertEquals("The second player's own pick is theirs alone", RevertAssessment.Tier.GREEN, tier(sitting, second, secondPick));
        else
            assertEquals("With nothing left that could be added, the pick ended the fellowships and the hands were dealt at once: "
                    + "the second player has seen a hand", RevertAssessment.Tier.RED, tier(sitting, second, secondPick));
        assertEquals("The first player has now seen one of the second player's companions", RevertAssessment.Tier.RED, tier(sitting, first, firstPick));
    }

    @Test
    public void AMulliganIsSomethingSeenByWhoeverTookItAndOnlyAChoiceToTheOther() {
        MediatorSitting sitting = new MediatorSitting();
        toStartingFellowships(sitting);
        String first = toMulligans(sitting);
        String second = sitting.other(first);
        int declined = sitting.mediator.journalSize();
        sitting.answer(first, "0");
        assertEquals("No", "No", sitting.parameter(second, "results")[0]);
        assertEquals(RevertAssessment.Tier.GREEN, tier(sitting, first, declined));

        int taken = sitting.mediator.journalSize();
        sitting.answer(second, "1");

        assertEquals("A new hand is a hand of cards not seen before", RevertAssessment.Tier.RED, tier(sitting, second, taken));
        assertEquals("To the other player it is a choice somebody else made, and a shuffle; nothing they can see",
                RevertAssessment.Tier.YELLOW, tier(sitting, first, declined));
        assertTrue(sitting.mediator.assessRevert(first, declined).reasons().toString(),
                sitting.mediator.assessRevert(first, declined).reasons().size() == 2);
    }

    @Test
    public void ACardPlayedFromHandInOnesOwnFellowshipPhaseIsOnesOwnToTakeBack() {
        int found = 0;
        for (int game = 0; game < 40 && found < 5; game++) {
            MediatorSitting sitting = new MediatorSitting();
            toStartingFellowships(sitting);
            String mulligans = toMulligans(sitting);
            sitting.answer(mulligans, "0");
            sitting.answer(sitting.other(mulligans), "0");

            String player = sitting.asked();
            AwaitingDecision decision = sitting.mediator.awaitingDecision(player);
            if (decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE
                    || sitting.mediator.getGame().getGameState().getCurrentPhase() != Phase.FELLOWSHIP)
                continue;
            String[] actions = sitting.parameter(player, "actionId");
            String[] cards = sitting.parameter(player, "cardId");
            String[] texts = sitting.parameter(player, "actionText");
            String play = null;
            for (int i = 0; i < actions.length && play == null; i++) {
                PhysicalCard card = sitting.mediator.getGame().getGameState().findCardById(Integer.parseInt(cards[i]));
                if (texts[i].startsWith("Play ") && card != null && LAID_DOWN.contains(card.getBlueprint().getCardType()))
                    play = actions[i];
            }
            if (play == null)
                continue;
            found++;
            String board = sitting.board();
            int there = sitting.mediator.journalSize();
            sitting.answer(player, play);

            assertEquals("game " + game + ": whether the card is down or a bearer for it is still being asked for",
                    RevertAssessment.Tier.GREEN, tier(sitting, player, there));
            assertEquals("game " + game + ": to the other player it is somebody else's choice, of a card they had not seen",
                    RevertAssessment.Tier.RED, tier(sitting, sitting.other(player), there));

            assertEquals(null, sitting.mediator.revertTo(there));
            assertEquals("game " + game, board, sitting.board());
            assertNotNull(sitting.mediator.awaitingDecision(player));
            assertEquals("game " + game + ": taken back, there is nothing since to undo",
                    RevertAssessment.Tier.GREEN, tier(sitting, sitting.other(player), there));
        }
        assertTrue("A card to play in the first fellowship phase was found in " + found + " games", found >= 5);
    }
}
