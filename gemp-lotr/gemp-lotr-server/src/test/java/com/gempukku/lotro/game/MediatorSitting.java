package com.gempukku.lotro.game;

import com.gempukku.lotro.bots.AssignmentLegality;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A game at a mediator, with two players who answer at random and somebody counting how it ends. */
class MediatorSitting {
    static final String FORMAT = "fotr_block";
    static final List<String> PLAYERS = List.of("Alice", "Bob");

    final LotroGameMediator mediator;
    final Map<String, RandomDecisionBot> bots = new LinkedHashMap<>();
    final List<String> endings = new ArrayList<>();

    MediatorSitting() {
        LotroDeck aragorn = DeckSerialization.buildDeckFromContents("Aragorn Starter", Boards.ARAGORN_STARTER, FORMAT, "Aragorn Starter");
        LotroDeck gandalf = DeckSerialization.buildDeckFromContents("Gandalf Starter", Boards.GANDALF_STARTER, FORMAT, "Gandalf Starter");
        LotroGameParticipant[] participants = {new LotroGameParticipant("Alice", aragorn), new LotroGameParticipant("Bob", gandalf)};
        mediator = new LotroGameMediator("1", LoadedLibraries._formatLibrary.getFormat(FORMAT), participants, LoadedLibraries._cardLibrary,
                new GameTimer(false, "Test", 60 * 80, 60 * 5), true, false, false, "Casual", new MarkdownParser(), null, false, null, null);
        for (String player : PLAYERS)
            bots.put(player, new RandomDecisionBot(player));
        mediator.addGameResultListener(new GameResultListener() {
            @Override
            public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                endings.add(winnerPlayerId + " won: " + winReason);
            }

            @Override
            public void gameCancelled() {
                endings.add("cancelled");
            }
        });
    }

    String asked() {
        for (String player : PLAYERS)
            if (mediator.awaitingDecision(player) != null)
                return player;
        return null;
    }

    /**
     * Has whoever is being asked answer, that many times over or until the game ends.
     * @return How many answers were given.
     */
    int play(int answers) {
        int given = 0;
        while (given < answers && !mediator.isFinished()) {
            String player = asked();
            if (player == null)
                break;
            AwaitingDecision decision = mediator.awaitingDecision(player);
            String answer = bots.get(player).chooseAction(mediator.getGame().getGameState(), decision, new AssignmentLegality(mediator.getGame()));
            //The bots' door: it takes an answer by name, and puts a refused one right
            mediator.botAnswered(player, decision.getAwaitingDecisionId(), answer);
            given++;
        }
        return given;
    }

    String board() {
        return Boards.of(mediator.getGame());
    }

    /** What each player is being asked, as the journal would know it. */
    String waiting() {
        StringBuilder sb = new StringBuilder();
        for (String player : PLAYERS) {
            AwaitingDecision decision = mediator.awaitingDecision(player);
            sb.append(player).append('=').append(decision == null ? "-" : GameJournal.fingerprint(decision)).append(' ');
        }
        return sb.toString();
    }

    /** Gives a player's answer to what they are being asked. */
    void answer(String player, String answer) {
        mediator.botAnswered(player, mediator.awaitingDecision(player).getAwaitingDecisionId(), answer);
    }

    /** The first value of one of the parameters of what a player is being asked. */
    String[] parameter(String player, String name) {
        return mediator.awaitingDecision(player).getDecisionParameters().get(name);
    }

    String other(String player) {
        return player.equals(PLAYERS.get(0)) ? PLAYERS.get(1) : PLAYERS.get(0);
    }
}
