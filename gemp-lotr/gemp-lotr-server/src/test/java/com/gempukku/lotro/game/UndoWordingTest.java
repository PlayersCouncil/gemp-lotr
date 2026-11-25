package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * What the players read about a place the game can be taken back to: what was done there, in a few words
 * (RestorePoints), and why going back to it is more than undoing a choice of one's own (InformationLedger.assess).
 * These go on buttons and in single lines, so they are short; and what is said to the other players never names a
 * card they could not see, nor says which card it was that was seen.
 */
public class UndoWordingTest {
    private static LotroGameMediator.RestorePointView latest(MediatorSitting sitting, String player) {
        return sitting.mediator.getRestorePoints(player).get(0);
    }

    @Test
    public void WhatWasDoneIsSaidInAFewWords() {
        MediatorSitting sitting = new MediatorSitting(Boards.ONE_COMPANION, Boards.ONE_COMPANION);
        String first = sitting.toStartingFellowships();
        sitting.pick(first);
        assertEquals("Played Merry", latest(sitting, first).label());
        sitting.pressDone(first);
        assertEquals("Pressed DONE", latest(sitting, first).label());

        String second = sitting.other(first);
        sitting.pick(second);
        sitting.pressDone(second);
        sitting.answer(first, "0");
        sitting.answer(second, "0");
        String fellowship = sitting.asked();
        assertTrue(sitting.waiting(), sitting.asked(fellowship, "Play Fellowship action"));
        sitting.answer(fellowship, "");
        assertEquals("Passed", latest(sitting, fellowship).label());
    }

    @Test
    public void ACardPlayedFromHandIsSaidByItsName() {
        MediatorSitting sitting = new MediatorSitting();
        String player = sitting.toFirstTurn();
        String play = sitting.playOfACardThatStays(player);
        if (play == null)
            return;     //(this hand has nothing to play that stays: nothing to say about it)
        String[] actions = sitting.parameter(player, "actionId");
        String text = sitting.parameter(player, "actionText")[List.of(actions).indexOf(play)];
        sitting.answer(player, play);
        assertEquals("Played " + text.substring("Play ".length()).replaceAll("<[^>]*>", ""), latest(sitting, player).label());
    }

    @Test
    public void ACardChosenWhereTheOthersCannotSeeIsNotNamedToThem() {
        MediatorSitting sitting = new MediatorSitting();
        String player = sitting.toFirstTurn();
        PhysicalCard inHand = sitting.mediator.getGame().getGameState().getHand(player).get(0);
        PhysicalCard onTable = sitting.mediator.getGame().getGameState().getRingBearer(player);
        assertEquals(Zone.HAND, inHand.getZone());

        //The labeller, given two choices of a card as the game would give them: one from the hand, one from the table
        RestorePoints points = new RestorePoints(new GameJournal(GameJournal.PlayerKeys.NAMES));
        points.setGame(sitting.mediator.getGame());
        for (PhysicalCard card : List.of(inHand, onTable)) {
            CardsSelectionDecision choice = new CardsSelectionDecision(1, "Choose a card", Set.of(card), 1, 1) {
                @Override
                public void decisionMade(String result) { }
            };
            points.decisionAnswering(player, choice);
            points.decisionAnswered(player, choice, String.valueOf(card.getCardId()));
        }
        RestorePoints.Point hidden = points.all().get(0);
        assertEquals("Chose " + inHand.getBlueprint().getTitle(), hidden.label());
        assertEquals("Chose a card", hidden.publicLabel());
        RestorePoints.Point open = points.all().get(1);
        assertEquals("Chose " + onTable.getBlueprint().getTitle(), open.label());
        assertEquals("on the table everybody sees it", open.label(), open.publicLabel());
    }

    @Test
    public void WhyAPlaceIsNotOnesOwnToUndoIsSaidByKindAndNeverByCard() {
        MediatorSitting sitting = new MediatorSitting(Boards.ONE_COMPANION, Boards.ONE_COMPANION);
        String first = sitting.toStartingFellowships();
        String second = sitting.other(first);
        int picked = sitting.mediator.journalSize();
        sitting.pick(first);
        assertEquals(List.of(), sitting.mediator.assessRevert(first, picked).reasons());

        //The other player's card on the table: one choice of theirs to make again, and a card of theirs seen
        sitting.pressDone(first);
        sitting.pick(second);
        assertEquals(List.of("Hidden information revealed: opponent's card seen", "Opponent replays 1 choice"),
                sitting.mediator.assessRevert(first, picked).reasons());

        //The hands dealt: cards drawn as well, and the decks shuffled for it
        sitting.pressDone(second);
        List<String> reasons = sitting.mediator.assessRevert(first, picked).reasons();
        assertTrue(reasons.toString(), reasons.get(0).startsWith("Hidden information revealed: "));
        assertTrue(reasons.toString(), reasons.get(0).contains("card drawn") && reasons.get(0).contains("opponent's card seen"));
        assertTrue(reasons.toString(), reasons.contains("Opponent replays 2 choices"));
        assertTrue(reasons.toString(), reasons.contains("Deck shuffled"));
        for (String reason : reasons)
            assertFalse(reason, reason.contains("Merry") || reason.contains("Frodo") || reason.contains(first) || reason.contains(second));
    }
}
